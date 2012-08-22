//$Header: /deegreerepository/deegree/src/org/deegree/ogcwebservices/wfs/WFService.java,v 1.46 2007/03/14 14:43:44 mschneider Exp $
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

import static java.util.Arrays.asList;
import static java.util.Collections.disjoint;
import static org.deegree.i18n.Messages.get;
import static org.deegree.ogcbase.ExceptionCode.INVALID_UPDATESEQUENCE;
import static org.deegree.ogcbase.ExceptionCode.VERSIONNEGOTIATIONFAILED;

import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerProvider;
import org.deegree.io.datastore.LockManager;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureWithLock;
import org.deegree.ogcwebservices.wfs.operation.GetGmlObject;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.owscommon.OWSDomainType;

/**
 * This class implements access to the methods defined in the OGC WFS 1.1.0 specification.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @see OGCWebService
 */
public class WFService implements OGCWebService {

    private static final ILogger LOG = LoggerFactory.getLogger( WFService.class );

    /** Only OGC standard version currently implemented by this service. */
    public static final String VERSION = "1.1.0";

    private static final TriggerProvider TP = TriggerProvider.create( WFService.class );

    private WFSConfiguration configuration;

    // shared instance that handles all GetFeature requests to this service
    private GetFeatureHandler getFeatureHandler;

    // shared instance that handles all DescribeFeatureType requests to this service
    private DescribeFeatureTypeHandler describeFTHandler;

    // shared instance that handles all LockFeature requests to this service
    private LockFeatureHandler lockFeatureHandler;

    private GetGmlObjectHandler getGmlObjectHandler;

    /**
     * Creates a new instance of <code>WFService</code> with the given configuration.
     * 
     * @param configuration
     * @throws OGCWebServiceException
     */
    WFService( WFSConfiguration configuration ) throws OGCWebServiceException {
        this.configuration = configuration;
        this.getFeatureHandler = new GetFeatureHandler( this );
        this.describeFTHandler = new DescribeFeatureTypeHandler( this );
        this.lockFeatureHandler = new LockFeatureHandler( this );
        getGmlObjectHandler = new GetGmlObjectHandler( configuration );
    }

    /**
     * Returns the capabilities of the <code>WFService</code>.
     * 
     * @return the capabilities, this is actually a <code>WFSConfiguration</code> instance
     */
    public WFSCapabilities getCapabilities() {
        return this.configuration;
    }

    /**
     * Performs the handling of the passed OGCWebServiceEvent directly and returns the result to the calling class/
     * method.
     * 
     * @param request
     *            WFS request to perform
     * 
     * @throws OGCWebServiceException
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {

        long ts = System.currentTimeMillis();
        request = (OGCWebServiceRequest) TP.doPreTrigger( this, request )[0];

        Object response = null;
        if ( request instanceof WFSGetCapabilities ) {
            validateGetCapabilitiesRequest( (WFSGetCapabilities) request );
            response = this.configuration;
        } else if ( request instanceof GetFeatureWithLock ) {
            response = this.lockFeatureHandler.handleRequest( (GetFeatureWithLock) request );
        } else if ( request instanceof GetFeature ) {
            ( (GetFeature) request ).guessAllMissingNamespaces( this );
            response = this.getFeatureHandler.handleRequest( (GetFeature) request );
        } else if ( request instanceof DescribeFeatureType ) {
            ( (DescribeFeatureType) request ).guessMissingNamespaces( this );
            response = this.describeFTHandler.handleRequest( (DescribeFeatureType) request );
        } else if ( request instanceof Transaction ) {
            ( (Transaction) request ).guessMissingNamespaces( this );
            TransactionHandler handler = new TransactionHandler( this, (Transaction) request );
            response = handler.handleRequest();
        } else if ( request instanceof LockFeature ) {
            ( (LockFeature) request ).guessMissingNamespaces( this );
            response = this.lockFeatureHandler.handleRequest( (LockFeature) request );
        } else if ( request instanceof GetGmlObject ) {
            response = getGmlObjectHandler.handleRequest( (GetGmlObject) request );
        } else {
            String msg = "Unknown request type: " + request.getClass().getName();
            throw new OGCWebServiceException( getClass().getName(), msg );
        }

        Object o = TP.doPostTrigger( this, response )[0];
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Using lockmanager instance: " + LockManager.getInstance() );
            LOG.logDebug( "WFS processing time for request type " + request.getClass().getSimpleName() + ": ",
                          Long.toString( ( System.currentTimeMillis() - ts ) ) );
        }
        return o;
    }

    // throws exception if it's required by the spec
    private void validateGetCapabilitiesRequest( WFSGetCapabilities request )
                            throws OGCWebServiceException {
        // version negotiation
        if ( request.getAcceptVersions() != null && request.getAcceptVersions().length != 0 ) {
            Operation op = configuration.getOperationsMetadata().getGetCapabilitiesOperation();
            OWSDomainType versions = op.getParameter( "AcceptVersions" );
            List<String> vs1 = asList( versions.getValues() );
            List<String> vs2 = asList( request.getAcceptVersions() );

            if ( disjoint( vs1, vs2 ) ) {
                throw new OGCWebServiceException( get( "WFS_VERSION_NEGOTIATION_FAILED" ), VERSIONNEGOTIATIONFAILED );
            }
        }

        if ( request.getUpdateSequence() != null ) {
            // check update sequences
            String seq = configuration.getUpdateSequence();
            if ( !request.getUpdateSequence().equals( "" ) && seq.compareTo( request.getUpdateSequence() ) < 0 ) {
                throw new OGCWebServiceException( "updatesequence", get( "UPDATESEQUENCE_INVALID" ),
                                                  INVALID_UPDATESEQUENCE );
            }
        }
    }

    /**
     * Returns a clone of the <code>WFService</code> instance.
     * <p>
     * Note that the configuration of the new service will refer to the same instance.
     */
    @Override
    public Object clone() {

        WFService clone = null;
        try {
            clone = new WFService( configuration );
        } catch ( OGCWebServiceException e ) {
            // can never happen
        }
        return clone;
    }

    /**
     * Returns the <code>MappedFeatureType</code> with the given name.
     * 
     * @param name
     *            name of the feature type
     * @return the mapped feature type with the given name, or null if it is not known to this WFService instance
     */
    public MappedFeatureType getMappedFeatureType( QualifiedName name ) {
        return this.configuration.getMappedFeatureTypes().get( name );
    }

    /**
     * Returns a <code>Map</code> of the feature types that this WFS serves.
     * 
     * @return keys: feature type names, values: mapped feature types
     */
    public Map<QualifiedName, MappedFeatureType> getMappedFeatureTypes() {
        return this.configuration.getMappedFeatureTypes();
    }
}
