//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.ogcwebservices.csw;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerProvider;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueGetCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueOperationsMetadata;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfiguration;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfigurationDocument;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecord;
import org.deegree.ogcwebservices.csw.discovery.Discovery;
import org.deegree.ogcwebservices.csw.discovery.GetDomain;
import org.deegree.ogcwebservices.csw.discovery.GetRecordById;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.GetRepositoryItem;
import org.deegree.ogcwebservices.csw.manager.Harvest;
import org.deegree.ogcwebservices.csw.manager.Manager;
import org.deegree.ogcwebservices.csw.manager.Transaction;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wfs.RemoteWFService;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.WFServiceFactory;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfigurationDocument;

/**
 * The Catalogue Service class provides the foundation for an OGC catalogue service. The Catalogue Service class
 * directly includes only the serviceTypeID attribute. In most cases, this attribute will not be directly visible to
 * catalogue clients.
 * <p>
 * The catalog service is an implementation of the OpenGIS Catalogue Service Specification 2.0.
 * </p>
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @see <a href="http://www.opengis.org/specs/">OGC Specification </a>
 */

public class CatalogueService implements OGCWebService {

    private static final ILogger LOG = LoggerFactory.getLogger( CatalogueService.class );

    private static final TriggerProvider TP = TriggerProvider.create( CatalogueService.class );

    private WFService wfsService;

    private CatalogueConfiguration serviceConfiguration;

    private static Map<URL, OGCWebService> wfsMap = new HashMap<URL, OGCWebService>();

    private static Map<String, Stack<Manager>> managerPool = new HashMap<String, Stack<Manager>>();

    private static Map<String, Stack<Discovery>> discoveryPool = new HashMap<String, Stack<Discovery>>();

    private static final String DEFAULT_VERSION = CSWPropertiesAccess.getString( "DEFAULTVERSION" );

    /**
     * Creates a new <code>CatalogService</code> instance.
     * 
     * @param config
     * 
     * @return new <code>CatalogService</code> instance.
     * @throws OGCWebServiceException
     */
    public static final CatalogueService create( CatalogueConfiguration config )
                            throws OGCWebServiceException {
        // get WFS: local or remote
        OGCWebService wfsResource = null;
        try {
            CatalogueConfigurationDocument document = new CatalogueConfigurationDocument();
            document.setSystemId( config.getSystemId() );

            URL wfsCapabilitiesFileURL = document.resolve( config.getDeegreeParams().getWfsResource().getHref().toString() );
            if ( wfsMap.get( wfsCapabilitiesFileURL ) == null ) {
                if ( wfsCapabilitiesFileURL.getProtocol().equals( "http" ) ) {
                    WFSCapabilitiesDocument capaDoc = new WFSCapabilitiesDocument();
                    capaDoc.load( wfsCapabilitiesFileURL );
                    WFSCapabilities capabilities = (WFSCapabilities) capaDoc.parseCapabilities();
                    LOG.logInfo( "Creating remote WFS with capabilities file " + wfsCapabilitiesFileURL );
                    wfsResource = new RemoteWFService( capabilities );
                } else {
                    WFSConfigurationDocument capaDoc = new WFSConfigurationDocument();
                    capaDoc.load( wfsCapabilitiesFileURL );
                    WFSConfiguration conf = capaDoc.getConfiguration();
                    LOG.logInfo( "CS-W service: Creating local WFS with capabilities file " + wfsCapabilitiesFileURL );
                    wfsResource = WFServiceFactory.createInstance( conf );
                    if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                        LOG.logDebug( "CS-W service: The localwfs was has been successfully created, it's capabilties are: "
                                      + XMLFactory.export( (WFSCapabilities) wfsResource.getCapabilities() ).getAsPrettyString() );
                    }
                }
                wfsMap.put( wfsCapabilitiesFileURL, wfsResource );
            } else {
                wfsResource = wfsMap.get( wfsCapabilitiesFileURL );
            }
        } catch ( Exception e ) {
            LOG.logError( "Error creating WFS for CSW", e );
            String msg = Messages.get( "CSW_ERROR_CREATING_WFS", e.getMessage() );
            throw new OGCWebServiceException( CatalogueService.class.getName(), msg );
        }

        // initialize manager and discovery
        return new CatalogueService( config, (WFService) wfsResource );
    }

    /**
     * 
     * @param config
     * @param wfsService
     */
    private CatalogueService( CatalogueConfiguration config, WFService wfsService ) {
        this.serviceConfiguration = config;
        this.wfsService = wfsService;
    }

    /**
     * Returns the OGC-capabilities of the service.
     * 
     * @return the OGC-capabilities of the service.
     * @todo analyze incoming request! return only requested sections
     */
    public OGCCapabilities getCapabilities() {
        return this.serviceConfiguration;
    }

    /**
     * Returns the service type (CSW).
     * 
     * @return the service type (CSW).
     */
    public String getServiceTypeId() {
        return this.serviceConfiguration.getServiceIdentification().getServiceType().getCode();
    }

    /**
     * @return Version
     */
    public String getVersion() {
        return this.serviceConfiguration.getVersion();
    }

    /**
     * Method for event based request processing.
     * 
     * @param request
     *            request object containing the request
     * @return an Object which may be one of the following
     *         <ul>
     *         <li>DescribeRecordResult</li>
     *         <li>GetRecordResult</li>
     *         <li>GetRecordByIdResult</li>
     *         <li>TransactionResult</li>
     *         <li>EchoRequest</li>
     *         <li>OGCCapabilities</li>
     * @throws OGCWebServiceException
     * 
     * @todo validation of requested version against accepted versions
     * @todo return type
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {

        request = (OGCWebServiceRequest) TP.doPreTrigger( this, request )[0];

        Object response = null;

        LOG.logDebug( "Version of request: " + request.getVersion() );
        if ( request instanceof DescribeRecord ) {
            response = this.getDiscovery().describeRecordType( (DescribeRecord) request );
        } else if ( request instanceof GetDomain ) {
            throw new OGCWebServiceException( getClass().getName(), Messages.get( "CSW_GETDOMAIN_NOT_IMPLEMENTED" ) );
            // TODO is not implemented
            // response = this.getDiscovery().getDomain( (GetDomain) request );
        } else if ( request instanceof GetRecords ) {
            response = this.getDiscovery().query( (GetRecords) request );
        } else if ( request instanceof GetRecordById ) {
            response = this.getDiscovery().query( (GetRecordById) request );
        } else if ( request instanceof Transaction ) {
            Manager manager = this.getManager( request.getVersion() );
            response = manager.transaction( (Transaction) request );
            releaseManager( request.getVersion(), manager );
        } else if ( request instanceof Harvest ) {
            Manager manager = this.getManager( request.getVersion() );
            response = manager.harvestRecords( (Harvest) request );
            releaseManager( request.getVersion(), manager );
        } else if ( request instanceof CatalogueGetCapabilities ) {
            response = this.getCapabilities();
        } else if ( request instanceof GetRepositoryItem ) {
            response = this.getDiscovery().guery( (GetRepositoryItem) request );
        } else {
            throw new OGCWebServiceException( Messages.get( "CSW_INVALID_REQUEST_TYPE", request.getClass().getName() ) );
        }

        return TP.doPostTrigger( this, response )[0];
    }

    /**
     * @return Returns the discovery.
     * 
     */
    public Discovery getDiscovery() {
        try {
            return getDiscovery( DEFAULT_VERSION );
        } catch ( OGCWebServiceException e ) {
            LOG.logWarning( e.getMessage(), e );            
            return new Discovery( wfsService, serviceConfiguration );
        }
    }

    /**
     * @return Returns the discovery.
     * @throws OGCWebServiceException
     * 
     */
    public Discovery getDiscovery( String version )
                            throws OGCWebServiceException {
        if ( version == null ) {
            version = DEFAULT_VERSION;
            LOG.logDebug( "The version requested for the discovery is null, setting to default version: " + version );
        }
        Discovery discovery = null;
        LOG.logDebug( "Getting manager for version: " + version );
        if ( discoveryPool.size() == 0 ) {
            discovery = instantiateDiscovery( version );
        } else {
            if ( discoveryPool.containsKey( version ) && discoveryPool.get( version ) != null ) {
                Stack<Discovery> stack = discoveryPool.get( version );
                if ( stack.size() > 0 ) {
                    discovery = stack.pop();
                }
            } else {
                discoveryPool.put( version, new Stack<Discovery>() );
            }
            if ( discovery == null ) {
                discovery = instantiateDiscovery( version );
            }
        }
        return discovery;
    }

    /**
     * This method can be used to release the manager back into the pool of managers. If either version or manager is
     * <code>null</code> this method returns.
     * 
     * @param version
     *            this manager is created for
     * @param manager
     *            to be released.
     */
    public synchronized void releaseManager( String version, Manager manager ) {
        if ( manager == null ) {
            return;
        }
        if ( version == null || "".equals( version ) ) {
            version = DEFAULT_VERSION;
            LOG.logDebug( "The version for releasing the manager is null, setting to default version: " + version );
        }
        Stack<Manager> stack = null;
        if ( managerPool.containsKey( version ) && managerPool.get( version ) != null ) {
            stack = managerPool.get( version );
        } else {
            stack = new Stack<Manager>();
        }
        stack.push( manager );
        managerPool.put( version, stack );
    }

    /**
     * @param version
     *            to get a manager for.
     * @return the manager.
     * @throws OGCWebServiceException
     */
    public synchronized Manager getManager( String version )
                            throws OGCWebServiceException {
        if ( version == null ) {
            version = DEFAULT_VERSION;
            LOG.logDebug( "The version requested for the manager is null, setting to default version: " + version );
        }
        Manager manager = null;
        LOG.logDebug( "Getting manager for version: " + version );
        if ( managerPool.size() == 0 ) {
            manager = instantiateManager( version );
        } else {
            if ( managerPool.containsKey( version ) && managerPool.get( version ) != null ) {
                Stack<Manager> stack = managerPool.get( version );
                if ( stack.size() > 0 ) {
                    manager = stack.pop();
                }
            } else {
                managerPool.put( version, new Stack<Manager>() );
            }
            if ( manager == null ) {
                manager = instantiateManager( version );
            }
        }
        return manager;
    }

    private synchronized Manager instantiateManager( String version )
                            throws OGCWebServiceException {
        CatalogueOperationsMetadata com = (CatalogueOperationsMetadata) serviceConfiguration.getOperationsMetadata();
        Manager manager = null;
        if ( com.getHarvest() != null || com.getTransaction() != null ) {
            try {
                String className = CSWPropertiesAccess.getString( "Manager" + version );
                if ( className == null ) {
                    String msg = Messages.get( "CSW_UNSUPPORTED_VERSION", version );
                    throw new OGCWebServiceException( getClass().getName(), msg );
                }
                LOG.logDebug( "Manager class used: " + className );
                manager = (Manager) Class.forName( className ).newInstance();
                manager.init( wfsService, serviceConfiguration );
            } catch ( MissingParameterValueException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            } catch ( InstantiationException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            } catch ( IllegalAccessException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            } catch ( ClassNotFoundException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            }
        } else {
            throw new OGCWebServiceException( getClass().getName(), Messages.get( "CSW_TRANSCATIONS_ARE_NOT_DEFINED" ) );
        }
        return manager;
    }

    private synchronized Discovery instantiateDiscovery( String version )
                            throws OGCWebServiceException {
        CatalogueOperationsMetadata com = (CatalogueOperationsMetadata) serviceConfiguration.getOperationsMetadata();
        Discovery discovery = null;
        if ( com.getHarvest() != null || com.getTransaction() != null ) {
            try {
                String className = CSWPropertiesAccess.getString( "Discovery" + version );
                if ( className == null ) {
                    String msg = Messages.get( "CSW_UNSUPPORTED_VERSION", version );
                    throw new OGCWebServiceException( getClass().getName(), msg );
                }
                LOG.logDebug( "Discovery class used: " + className );
                discovery = (Discovery) Class.forName( className ).newInstance();
                discovery.init( wfsService, serviceConfiguration );
            } catch ( MissingParameterValueException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            } catch ( InstantiationException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            } catch ( IllegalAccessException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            } catch ( ClassNotFoundException e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
            }
        } else {
            throw new OGCWebServiceException( getClass().getName(), Messages.get( "CSW_TRANSCATIONS_ARE_NOT_DEFINED" ) );
        }
        return discovery;
    }

}
