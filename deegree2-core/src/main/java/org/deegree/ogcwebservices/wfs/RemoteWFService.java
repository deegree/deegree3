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

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument_1_0_0;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument_1_1_0;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;

/**
 * An instance of the class acts as a wrapper to a remote WFS.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class RemoteWFService implements OGCWebService {

    private static final ILogger LOG = LoggerFactory.getLogger( RemoteWFService.class );

    protected static final String GETCAPABILITIES = "GETCAPABILITIES";

    protected static final String GETFEATURE = "GETFEATURE";

    protected static final String GETFEATUREWITHLOCK = "GETFEATUREWITHLOCK";

    protected static final String DESCRIBEFEATURETYPE = "DESCRIBEFEATURETYPE";

    protected static final String TRANSACTION = "TRANSACTION";

    protected static final String LOCKFEATURE = "LOCKFEATURE";

    protected WFSCapabilities capabilities = null;

    protected Map<String, URL> addresses = new HashMap<String, URL>();

    /**
     * Creates a new instance of RemoteWFService
     *
     * @param capabilities
     * @throws OGCWebServiceException
     */
    public RemoteWFService( WFSCapabilities capabilities ) throws OGCWebServiceException {

        this.capabilities = capabilities;

        WFSOperationsMetadata om = (WFSOperationsMetadata) capabilities.getOperationsMetadata();
        Operation op = om.getGetCapabilitiesOperation();

        // get GetCapabilities address
        DCPType[] dcp = op.getDCPs();
        URL[] get = ( (HTTP) dcp[0].getProtocol() ).getGetOnlineResources();
        addresses.put( GETCAPABILITIES, get[0] );

        // get GetFeature address
        op = om.getGetFeature();
        dcp = op.getDCPs();
        boolean po = false;
        for ( int i = 0; i < dcp.length; i++ ) {
            get = ( (HTTP) dcp[i].getProtocol() ).getPostOnlineResources();
            if ( get != null && get.length > 0 ) {
                addresses.put( GETFEATURE, get[0] );
                po = true;
            }
        }
        if ( !po ) {
            String s = "WFS: " + capabilities.getServiceIdentification().getTitle() + " doesn't "
                       + "support HTTP POST for GetFeature requests";
            LOG.logDebug( s );
            throw new OGCWebServiceException( s );
        }

        // get DescribeFeatureType address
        op = om.getDescribeFeatureType();
        dcp = op.getDCPs();
        get = ( (HTTP) dcp[0].getProtocol() ).getGetOnlineResources();
        addresses.put( DESCRIBEFEATURETYPE, get[0] );

        op = om.getGetFeatureWithLock();
        if ( op != null ) {
            // get GetFeatureWithLock address
            dcp = op.getDCPs();
            po = false;
            for ( int i = 0; i < dcp.length; i++ ) {
                get = ( (HTTP) dcp[i].getProtocol() ).getPostOnlineResources();
                if ( get != null && get.length > 0 ) {
                    addresses.put( GETFEATUREWITHLOCK, get[0] );
                    po = true;
                }
            }
            if ( !po ) {
                String s = "WFS: " + capabilities.getServiceIdentification().getTitle()
                           + " doesn't support HTTP POST for GetFeatureWithLock requests";
                LOG.logDebug( s );
                throw new OGCWebServiceException( s );
            }
        }

        op = om.getTransaction();
        if ( op != null ) {
            // get Transaction address
            dcp = op.getDCPs();
            po = false;
            for ( int i = 0; i < dcp.length; i++ ) {
                get = ( (HTTP) dcp[i].getProtocol() ).getPostOnlineResources();
                if ( get != null && get.length > 0 ) {
                    addresses.put( TRANSACTION, get[0] );
                    po = true;
                }
            }
            if ( !po ) {
                String s = "WFS: " + capabilities.getServiceIdentification().getTitle()
                           + " doesn't support HTTP POST for Transaction requests";
                LOG.logDebug( s );
                throw new OGCWebServiceException( s );
            }
        }

        op = om.getLockFeature();
        if ( op != null ) {
            // get LockFeature address
            dcp = op.getDCPs();
            get = ( (HTTP) dcp[0].getProtocol() ).getGetOnlineResources();
            if ( get != null && get.length > 0 ) {
                addresses.put( LOCKFEATURE, get[0] );
            }
        }

    }

    /**
     *
     * @return capabilities
     */
    public WFSCapabilities getWFSCapabilities() {
        return capabilities;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.OGCWebService#doService(org.deegree.ogcwebservices.OGCWebServiceRequest)
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {
        Object response = null;
        if ( request instanceof GetFeature ) {
            response = handleGetFeature( (GetFeature) request );
        } else if ( request instanceof DescribeFeatureType ) {
            response = handleDescribeFeatureType( (DescribeFeatureType) request );
        } else if ( request instanceof WFSGetCapabilities ) {
            response = handleGetCapabilities( (WFSGetCapabilities) request );
        } else if ( request instanceof LockFeature ) {
            response = handleLockFeature( (LockFeature) request );
        } else if ( request instanceof Transaction ) {
            response = handleTransaction( (Transaction) request );
        }
        return response;
    }

    /**
     * performs a GetFeature request against the remote service. The method uses http-POST to call the remote WFS
     *
     * @param request
     *            get feature request
     */
    private FeatureResult handleGetFeature( GetFeature request )
                            throws OGCWebServiceException {

        URL url = addresses.get( GETFEATURE );
        StringWriter writer = new StringWriter( 1000 );
        try {
            if ( "1.0.0".equals( capabilities.getVersion() ) ) {
                XMLFactory_1_0_0.export( request ).write( writer );
            } else {
                XMLFactory.export( request ).write( writer );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "could not transform GetFeature requst to its string representation" );
        }
        String param = writer.getBuffer().toString();

        FeatureCollection result = null;
        try {
            // get map from the remote service
            NetWorker nw = new NetWorker( CharsetUtils.getSystemCharset(), url, param );
            String contentType = nw.getContentType();
            if ( contentType == null || MimeTypeMapper.isKnownMimeType( contentType ) ) {
                try {
                    InputStreamReader isr = new InputStreamReader( nw.getInputStream(), CharsetUtils.getSystemCharset() );
                    GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
                    doc.load( isr, url.toString() );
                    result = doc.parse();
                } catch ( Exception e ) {
                    throw new OGCWebServiceException( e.toString() );
                }
            } else {
                String msg = StringTools.concat( 500, "Response of the remote WFS contains unknown content type: ",
                                                 contentType, ";request: ", param );
                throw new OGCWebServiceException( "RemoteWFS:handleGetFeature", msg );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = StringTools.concat( 500, "Could not get feature from RemoteWFS: ",
                                             capabilities.getServiceIdentification().getTitle(), "; request: ", param,
                                             ';' );
            throw new OGCWebServiceException( "RemoteWFS:handleGetFeature", msg );

        }

        FeatureResult fr = new FeatureResult( request, result );
        return fr;

    }

    /**
     * Performs a describe feature type request against a remote WFS. The method uses http-GET to call the remote WFS
     *
     * @param request
     *            describe feature type request
     */
    private FeatureTypeDescription handleDescribeFeatureType( DescribeFeatureType request )
                            throws OGCWebServiceException {

        URL url = addresses.get( DESCRIBEFEATURETYPE );

        String param = request.getRequestParameter();

        String result = null;
        try {
            String us = OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() ) + param;
            URL ur = new URL( us );
            // get map from the remote service
            NetWorker nw = new NetWorker( CharsetUtils.getSystemCharset(), ur );
            byte[] b = nw.getDataAsByteArr( 20000 );
            String contentType = nw.getContentType();
            if ( MimeTypeMapper.isKnownMimeType( contentType ) ) {
                // create a WFSCapabilities instance from the result
                result = new String( b );
            } else {
                String msg = StringTools.concat( 500, "Response of the remote WFS contains unknown content type: ",
                                                 contentType, ";request: ", param );
                throw new OGCWebServiceException( "RemoteWFS:handleDescribeFeatureType", msg );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = StringTools.concat( 500, "Could not get map from RemoteWFS: ",
                                             capabilities.getServiceIdentification().getTitle(), "; request: ", param,
                                             ';' );
            throw new OGCWebServiceException( "RemoteWFS:handleDescribeFeatureType", msg );

        }

        FeatureTypeDescription ftd = null;
        try {
            XMLFragment frag = new XMLFragment( new StringReader( result ), XMLFragment.DEFAULT_URL );
            ftd = new FeatureTypeDescription( frag );
        } catch ( Exception e1 ) {
            LOG.logError( e1.getMessage(), e1 );
            throw new OGCWebServiceException( this.getClass().getName() + "Could not create response",
                                              StringTools.stackTraceToString( e1 ) );
        }

        return ftd;
    }

    private String toKVPRequest() {
        return null;
    }

    /**
     * reads the capabilities from the remote WFS by performing a GetCapabilities request against it. The method uses
     * http-GET to call the remote WFS
     *
     * @param request
     *            capabilities request
     */
    private WFSCapabilities handleGetCapabilities( WFSGetCapabilities request )
                            throws OGCWebServiceException {

        URL url = addresses.get( GETCAPABILITIES );
        String param = request.getRequestParameter();

        WFSCapabilities response = null;
        try {
            String remoteAddress = OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() );
            URL ur = new URL( remoteAddress + param );
            XMLFragment responseDoc = new XMLFragment( ur );
            String version = responseDoc.getRootElement().getAttribute( "version" );
            if ( version == null ) {
                String msg = "Cannot determine remote WFS version. No 'version' attribute.";
                throw new OGCWebServiceException( "RemoteWFS:handleGetCapabilities", msg );
            }
            if ( "1.0.0".equals( version ) ) {
                WFSCapabilitiesDocument_1_0_0 capabilitiesDoc = new WFSCapabilitiesDocument_1_0_0();
                capabilitiesDoc.setRootElement( responseDoc.getRootElement() );
                response = (WFSCapabilities) capabilitiesDoc.parseCapabilities();
            } else if ( "1.1.0".equals( version ) ) {
                WFSCapabilitiesDocument_1_1_0 capabilitiesDoc = new WFSCapabilitiesDocument_1_1_0();
                capabilitiesDoc.setRootElement( responseDoc.getRootElement() );
                response = (WFSCapabilities) capabilitiesDoc.parseCapabilities();
            } else {
                String msg = "Cannot communicate with remote WFS. Protocol version '" + version
                             + "' is not supported by RemoteWFService class.";
                throw new OGCWebServiceException( "RemoteWFS:handleGetCapabilities", msg );
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = StringTools.concat( 500, "Could not get map from RemoteWFS: ",
                                             capabilities.getServiceIdentification().getTitle(), "; request: ", param,
                                             ';' );
            throw new OGCWebServiceException( "RemoteWFS:handleGetCapabilities", msg );

        }
        return response;

    }

    /**
     * @param request
     */
    private Object handleLockFeature( LockFeature request ) {
        // FIXME
        // TODO
        return null;
    }

    /**
     * @param request
     */
    private Object handleTransaction( Transaction request ) {
        // FIXME
        // TODO
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.OGCWebService#getCapabilities()
     */
    public OGCCapabilities getCapabilities() {
        return capabilities;
    }

}
