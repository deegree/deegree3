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
package org.deegree.ogcwebservices.wfs;

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.LockManager;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfigurationDocument;
import org.xml.sax.SAXException;

/**
 * Factory class for creating instances of {@link WFService}.
 *
 * TODO manage several instances of different WFServices in a pool
 *
 * @see WFService
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFServiceFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( WFServiceFactory.class );

    private static WFSConfiguration CONFIG;

    private static boolean lockManagerInitialized;

    private WFServiceFactory() {
        // prevent instantiation
    }

    /**
     *
     * @return a WFService instance.
     * @throws OGCWebServiceException
     */
    public static WFService createInstance()
                            throws OGCWebServiceException {
        if ( CONFIG == null ) {
            throw new OGCWebServiceException( WFServiceFactory.class.getName(),
                                              "configuration has not been initialized" );
        }
        synchronized ( WFServiceFactory.class ) {
            if ( !lockManagerInitialized ) {
                try {
                    LockManager.initialize( CONFIG.getDeegreeParams().getLockManagerDirectory() );
                } catch ( DatastoreException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new OGCWebServiceException( "WFSServiceFactory", e.getMessage() );
                }
                lockManagerInitialized = true;
            }
        }
        return new WFService( CONFIG );
    }

    /**
     *
     * @param wfsConfiguration
     * @return a WFSService instance created from the given configuration.
     * @throws OGCWebServiceException
     */
    public static WFService createInstance( WFSConfiguration wfsConfiguration )
                            throws OGCWebServiceException {
        LOG.logDebug( "Creating WFService instance." );
        synchronized ( WFServiceFactory.class ) {
            if ( !lockManagerInitialized ) {
                try {
                    LockManager.initialize( wfsConfiguration.getDeegreeParams().getLockManagerDirectory() );
                } catch ( DatastoreException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new OGCWebServiceException( "WFSServiceFactory", e.getMessage() );
                }
                lockManagerInitialized = true;
            }
        }
        return new WFService( wfsConfiguration );
    }

    /**
     * Sets the <code>WFSConfiguration</code>. Afterwards, all <code>WFService</code> instances returned by
     * <code>createInstance()</code> will use this configuration.
     *
     * @param config
     * @throws InvalidConfigurationException
     */
    public synchronized static void setConfiguration( WFSConfiguration config )
                            throws InvalidConfigurationException {
        validateConfiguration( config );
        CONFIG = config;
        // TODO: if service instances have already been created
        // - destroy all instances
        // - create new service instances and put them in the pool
    }

    /**
     *
     * @param serviceConfigurationURL
     * @throws InvalidConfigurationException
     * @throws IOException
     */
    public synchronized static void setConfiguration( URL serviceConfigurationURL )
                            throws InvalidConfigurationException, IOException {

        try {
            WFSConfigurationDocument cd = new WFSConfigurationDocument();
            cd.load( serviceConfigurationURL );
            setConfiguration( cd.getConfiguration() );
        } catch ( InvalidConfigurationException e ) {
            throw new InvalidConfigurationException( "WFSServiceFactory", e.getMessage() );
        } catch ( SAXException e ) {
            throw new InvalidConfigurationException( "WFSServiceFactory", e.getMessage() );
        }

    }

    /**
     * @return CONFIG
     */
    public static WFSConfiguration getConfiguration() {
        return CONFIG;
    }

    private static void validateConfiguration( WFSConfiguration config )
                            throws InvalidConfigurationException {
        String[] versions = config.getServiceIdentification().getServiceTypeVersions();
        for ( String version : versions ) {
            if ( !WFService.VERSION.equals( version ) ) {
                String msg = Messages.getMessage( "WFS_CONF_UNSUPPORTED_VERSION", version, WFService.VERSION );
                throw new InvalidConfigurationException( msg );
            }
        }
    }
}
