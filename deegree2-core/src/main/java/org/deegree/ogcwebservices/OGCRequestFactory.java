// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/OGCRequestFactory.java,v 1.12
// 2004/08/10 17:17:02 tf Exp $
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
package org.deegree.ogcwebservices;

import static org.deegree.framework.util.CharsetUtils.getSystemCharset;
import static org.deegree.i18n.Messages.get;
import static org.deegree.ogcbase.ExceptionCode.INVALIDPARAMETERVALUE;
import static org.deegree.ogcbase.ExceptionCode.MISSINGPARAMETERVALUE;
import static org.deegree.ogcbase.ExceptionCode.OPERATIONNOTSUPPORTED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueGetCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueOperationsMetadata;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecord;
import org.deegree.ogcwebservices.csw.discovery.GetRecordById;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.GetRepositoryItem;
import org.deegree.ogcwebservices.csw.manager.Harvest;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.wass.common.CloseSession;
import org.deegree.ogcwebservices.wass.common.GetSession;
import org.deegree.ogcwebservices.wass.was.operation.DescribeUser;
import org.deegree.ogcwebservices.wass.was.operation.WASGetCapabilities;
import org.deegree.ogcwebservices.wass.wss.operation.DoService;
import org.deegree.ogcwebservices.wass.wss.operation.WSSGetCapabilities;
import org.deegree.ogcwebservices.wcs.describecoverage.DescribeCoverage;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wcts.operation.GetResourceByID;
import org.deegree.ogcwebservices.wcts.operation.GetResourceByIDDocument;
import org.deegree.ogcwebservices.wcts.operation.IsTransformable;
import org.deegree.ogcwebservices.wcts.operation.IsTransformableDocument;
import org.deegree.ogcwebservices.wcts.operation.Transform;
import org.deegree.ogcwebservices.wcts.operation.TransformDocument;
import org.deegree.ogcwebservices.wcts.operation.WCTSGetCapabilities;
import org.deegree.ogcwebservices.wcts.operation.WCTSGetCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureWithLock;
import org.deegree.ogcwebservices.wfs.operation.GetGmlObject;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wmps.operation.DescribeTemplate;
import org.deegree.ogcwebservices.wmps.operation.GetAvailableTemplates;
import org.deegree.ogcwebservices.wmps.operation.PrintMap;
import org.deegree.ogcwebservices.wmps.operation.WMPSGetCapabilities;
import org.deegree.ogcwebservices.wms.operation.DescribeLayer;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.WMSGetCapabilities;
import org.deegree.ogcwebservices.wps.capabilities.WPSGetCapabilities;
import org.deegree.ogcwebservices.wps.describeprocess.DescribeProcessRequest;
import org.deegree.ogcwebservices.wps.execute.ExecuteRequest;
import org.deegree.ogcwebservices.wpvs.operation.Get3DFeatureInfo;
import org.deegree.ogcwebservices.wpvs.operation.GetView;
import org.deegree.ogcwebservices.wpvs.operation.WPVSGetCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Factory for generating request objects for OGC Web Services.
 * <p>
 * Requests may be generated from KVP or DOM representations. Also contains methods that decide whether an incoming
 * request representation is valid for a certain service.
 * </p>
 * Currently supported services are:
 * <ul>
 * <li>CSW</li>
 * <li>WFS</li>
 * <li>WCS</li>
 * <li>WMS</li>
 * <li>WFS-G</li>
 * <li>SOS</li>
 * <li>WMPS</li>
 * <li>WSS</li>
 * <li>WAS</li>
 * <li>WPVS</li>
 * </ul>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OGCRequestFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( OGCRequestFactory.class );

    private static final String CSW_SERVICE_NAME = "CSW";

    /**
     * The service name of a getRepositoryItem request, only valid for the csw/ebrim. Fixed value:
     * "urn:x-ogc:specification:cswebrim:Service:OGC-CSW:ebRIM"
     */
    public static final String CSW_SERVICE_NAME_EBRIM = "urn:x-ogc:specification:cswebrim:Service:OGC-CSW:ebRIM";

    private static final String WFS_SERVICE_NAME = "WFS";

    private static final String WCS_SERVICE_NAME = "WCS";

    private static final String WMS_SERVICE_NAME = "WMS";

    private static final String SOS_SERVICE_NAME = "SOS";

    private static final String WPVS_SERVICE_NAME = "WPVS";

    private static final String WMPS_SERVICE_NAME = "WMPS";

    private static final String WPS_SERVICE_NAME = "WPS";

    private static final String WSS_SERVICE_NAME = "WSS";

    private static final String WAS_SERVICE_NAME = "WAS";

    private static final String WCTS_SERVICE_NAME = "WCTS";

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the content contained within the passed request.
     * 
     * @param request
     * @return the request object
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest create( ServletRequest request )
                            throws OGCWebServiceException {

        Map<String, String> result = KVP2Map.toMap( request );

        LOG.logDebug( "Request parameters: " + result );

        if ( result.size() != 0 ) {
            return createFromKVP( result );
        }
        
        XMLFragment fragment = null;
        try {
            Reader xmlReader = null;
            if ( LOG.isDebug() ) {
                LOG.logDebug( "Request's content type is " + request.getContentType() );
                LOG.logDebug( "Request's character encoding is " + request.getCharacterEncoding() );
            }

            if ( request.getContentType() != null ) {
                if ( request.getCharacterEncoding() == null ) {
                    request.setCharacterEncoding( getSystemCharset() );
                }
                xmlReader = request.getReader();
             } else {
                // DO NOT REMOVE THIS !!!!!
                // IT IS ABSOLUTELY NECESSARY TO ENSURE CORRECT CHARACTER ENCODING !!!
                xmlReader = new StringReader( getRequestContent( (HttpServletRequest) request ) );
            }
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader( xmlReader );
                String line = null;
                while ( ( line = br.readLine() ) != null ) {
                    sb.append( line );
                }
                String content = sb.toString();
                LOG.logDebugFile( "OGCRequestFactory_incoming", ".xml", content );
                xmlReader = new StringReader( content );
            }
            fragment = new XMLFragment( xmlReader, XMLFragment.DEFAULT_URL );
        } catch ( SAXException se ) {
            se.printStackTrace();
            String message = se.getMessage();
            if ( message != null ) {
                if ( message.contains( "not allowed in prolog" ) ) {
                    throw new OGCWebServiceException( "OGCRequestFactory",
                                                      "No key-value pairs were given and the request"
                                                                              + " does not contain parsable xml",
                                                      ExceptionCode.NOAPPLICABLECODE );
                }
                throw new OGCWebServiceException( "OGCRequestFactory", "Error parsing XML request: " + message,
                                                  ExceptionCode.NOAPPLICABLECODE );
            }
            throw new OGCWebServiceException( "OGCRequestFactory", "Error parsing XML request",
                                              ExceptionCode.NOAPPLICABLECODE );
        } catch ( Exception e ) {
            LOG.logError( "Error parsing XML request: " + e.getMessage(), e );
            throw new OGCWebServiceException( "OGCRequestFactory", "Error parsing XML request: " + e.getMessage() );
        }

        return createFromXML( fragment.getRootElement().getOwnerDocument() );
    }

    /**
     * DO NOT REMOVE THIS !!!!! IT IS ABSOLUTLY NECESSARY TO ENSURE CORRECT CHARACTER ENCODING !!!
     * 
     * @param request
     * @throws IOException
     */
    private static String getRequestContent( HttpServletRequest request )
                            throws IOException {
        String method = request.getMethod();

        if ( method.equalsIgnoreCase( "POST" ) ) {
            String charset = request.getCharacterEncoding();
            LOG.logDebug( "posted character encoding: ", charset );
            if ( charset == null ) {
                charset = "UTF-8";
            }
            StringBuffer req = readPost( request, charset );
            if ( charset.equalsIgnoreCase( CharsetUtils.getSystemCharset() ) ) {
                return req.toString();
            }
            if ( charset.equalsIgnoreCase( "UTF-8" ) && !charset.equalsIgnoreCase( CharsetUtils.getSystemCharset() ) ) {
                String s = new String( req.toString().getBytes(), CharsetUtils.getSystemCharset() );
                return s;
            }
            if ( !charset.equalsIgnoreCase( "UTF-8" ) && !charset.equalsIgnoreCase( CharsetUtils.getSystemCharset() ) ) {
                String s = new String( req.toString().getBytes(), "UTF-8" );
                return s;
            }
            return req.toString();
        }

        String req = request.getQueryString();
        if ( req == null ) {
            req = readPost( request, CharsetUtils.getSystemCharset() ).toString();
        }
        LOG.logDebug( "request string: ", req );

        return URLDecoder.decode( req, CharsetUtils.getSystemCharset() );

    }

    /**
     * DO NOT REMOVE THIS !!!!! IT IS ABSOLUTLY NECESSARY TO ENSURE CORRECT CHARACTER ENCODING !!!
     * 
     * @param request
     * @param charset
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static StringBuffer readPost( HttpServletRequest request, String charset )
                            throws UnsupportedEncodingException, IOException {
        java.io.Reader reader = new InputStreamReader( request.getInputStream(), charset );
        BufferedReader br = new BufferedReader( reader );
        StringBuffer req = new StringBuffer( 10000 );
        for ( String line = null; ( line = br.readLine() ) != null; ) {
            req.append( ( new StringBuilder( String.valueOf( line ) ) ).append( "\n" ).toString() );
        }

        br.close();
        return req;
    }

    /**
     * Creates an instance of an <code>AbstractOGCWebServiceRequest</code> from the passed DOM object. Supported OWS are
     * 'WMS', 'WFS', 'WCS' and 'CSW'. If a request for another service is passed or a request that isn't supported by
     * one of the listed services an exception will be thrown. <BR>
     * Notice that not all listed services will support request processing by reading the request to be performed from a
     * DOM object. In this case also an exception will be thrown even if the same request may can be performed if KVP is
     * used.
     * 
     * @param doc
     * @return the request object
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest createFromXML( Document doc )
                            throws OGCWebServiceException {

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            XMLFragment xml = new XMLFragment();
            xml.setRootElement( doc.getDocumentElement() );
            LOG.logDebug( "XML request (pretty printed): ", xml.getAsPrettyString() );
        }

        String service = XMLTools.getAttrValue( doc.getDocumentElement(), null, "service", null );
        String request = doc.getDocumentElement().getLocalName();
        service = getTargetService( service, request, doc );
        if ( "unknown".equals( service ) ) {
            throw new OGCWebServiceException( "OGCRequestFactory", "Specified service '" + service
                                                                   + "' is not a known OGC service type." );
        }
        OGCWebServiceRequest ogcRequest = null;
        if ( request == null ) {
            throw new OGCWebServiceException( "request", "Request parameter must be set!", MISSINGPARAMETERVALUE );
        } else if ( WMS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWMSRequest( request, doc );
        } else if ( WFS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWFSRequest( request, doc );
        } else if ( WCS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWCSRequest( request, doc );
        } else if ( CSW_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getCSWRequest( request, doc );
        } else if ( WPVS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWPVSRequest( request, doc );
        } else if ( WMPS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWMPSRequest( request, doc );
        } else if ( WPS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWPSRequest( request, doc );
        } else if ( WSS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWSSRequest( request, doc );
        } else if ( WAS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWASRequest( request, doc );
        } else if ( WCTS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWCTSRequest( request, doc );
        } else {
            throw new OGCWebServiceException( "OGCRequestFactory", "No handler for service " + service
                                                                   + " in OGCRequestFactory." );
        }
        return ogcRequest;
    }

    /**
     * Creates an instance of an <code>AbstractOGCWebServiceRequest</code> from the passed KVP encoded request.
     * Supported OWS are 'WMS', 'WFS', 'WCS' and 'CSW'. If a request for another service is passed or a request that
     * isn't supported by one of the listed services an exception will be thrown. <BR>
     * Notice that not all listed services will support request processing by reading the request to be performed from
     * KVPs. In this case also an exception will be thrown even if the same request may be performed if a DOM object is
     * used.
     * 
     * @param map
     * @return the request object
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest createFromKVP( Map<String, String> map )
                            throws OGCWebServiceException {

        LOG.logDebug( "KVP request: ", map );
        // request parameter given?
        String request = map.get( "REQUEST" );
        if ( request == null ) {
            LOG.logInfo( "parameter: ", map );
            throw new OGCWebServiceException( "request", get( "NO_REQUEST_PARAMETER" ), MISSINGPARAMETERVALUE );
        }
        // service parameter given?
        String service = map.get( "SERVICE" );
        if ( service == null ) {
            // a profile of a service will be treated as a service
            service = map.get( "PROFILE" );
            if ( service == null ) {
                service = getTargetService( service, request, null );
            }
        }

        OGCWebServiceRequest ogcRequest = null;
        if ( !WFS_SERVICE_NAME.equals( service ) ) {
            map.put( "SERVICE", service );
        }
        if ( WMS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWMSRequest( request, map );
        } else if ( WFS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWFSRequest( request, map );
        } else if ( WCS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWCSRequest( request, map );
        } else if ( CSW_SERVICE_NAME_EBRIM.equals( service ) || CSW_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getCSWRequest( request, map );
        } else if ( WPVS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWPVSRequest( request, map );
        } else if ( WMPS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWMPSRequest( request, map );
        } else if ( WPS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWPSRequest( request, map );
        } else if ( WSS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWSSRequest( request, map );
        } else if ( WAS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWASRequest( request, map );
        } else if ( WCTS_SERVICE_NAME.equals( service ) ) {
            ogcRequest = getWCTSRequest( request, map );
        } else {
            if ( map.get( "SERVICE" ) == null || map.get( "SERVICE" ).equals( "unknown" ) ) {
                throw new OGCWebServiceException( "service", get( "NO_SERVICE_SPECIFIED" ), MISSINGPARAMETERVALUE );
            }

            throw new OGCWebServiceException( "service", get( "UNKNOWN_SERVICE_SPECIFIED", map.get( "SERVICE" ) ),
                                              INVALIDPARAMETERVALUE );
        }
        return ogcRequest;
    }

    /**
     * Creates the corresponding WFS request object from the given parameters.
     * 
     * @param request
     * @param map
     * @return the corresponding WFS request object
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWFSRequest( String request, Map<String, String> map )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        map.put( "ID", "" + IDGenerator.getInstance().generateUniqueID() );
        if ( request.equals( "GetCapabilities" ) ) {
            ogcRequest = WFSGetCapabilities.create( map );
        } else if ( request.equals( "GetFeature" ) ) {
            ogcRequest = GetFeature.create( map );
        } else if ( request.equals( "GetFeatureWithLock" ) ) {
            ogcRequest = GetFeatureWithLock.create( map );
        } else if ( request.equals( "GetGmlObject" ) ) {
            ogcRequest = GetGmlObject.create( map );
        } else if ( request.equals( "LockFeature" ) ) {
            ogcRequest = LockFeature.create( map );
        } else if ( request.equals( "DescribeFeatureType" ) ) {
            ogcRequest = DescribeFeatureType.create( map );
        } else if ( request.equals( "Transaction" ) ) {
            ogcRequest = Transaction.create( map );
        } else {
            throw new InvalidParameterValueException( "Unknown WFS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>Document</code>. The returned request will be
     * a WFS request. The type of request is determined by also submitted request name. Known requests are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>GetFeature</li>
     * <li>GetFeatureWithLock</li>
     * <li>DescribeFeatureType</li>
     * <li>Transaction</li>
     * <li>LockFeature</li>
     * </ul>
     * <p>
     * Any other request passed to the method causes an <code>OGCWebServiceException</code> to be thrown.
     * </p>
     * 
     * @param request
     * @param doc
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWFSRequest( String request, Document doc )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = "" + IDGenerator.getInstance().generateUniqueID();
        if ( request.equals( "GetCapabilities" ) ) {
            ogcRequest = WFSGetCapabilities.create( id, doc.getDocumentElement() );
        } else if ( request.equals( "GetFeature" ) ) {
            ogcRequest = GetFeature.create( id, doc.getDocumentElement() );
        } else if ( request.equals( "GetFeatureWithLock" ) ) {
            ogcRequest = GetFeatureWithLock.create( id, doc.getDocumentElement() );
        } else if ( request.equals( "DescribeFeatureType" ) ) {
            ogcRequest = DescribeFeatureType.create( id, doc.getDocumentElement() );
        } else if ( request.equals( "Transaction" ) ) {
            ogcRequest = Transaction.create( id, doc.getDocumentElement() );
        } else if ( request.equals( "LockFeature" ) ) {
            ogcRequest = LockFeature.create( id, doc.getDocumentElement() );
        } else if ( request.equals( "GetGmlObject" ) ) {
            ogcRequest = GetGmlObject.create( id, doc.getDocumentElement() );
        } else {
            throw new OGCWebServiceException( "Unknown / unimplemented WFS request type: '" + request + "'.",
                                              OPERATIONNOTSUPPORTED );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>Document</code>. The returned request will be
     * a WSS request. The type of request is determined by also submitted request name. Known requests are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>GetSession</li>
     * <li>CloseSession</li>
     * <li>DoService</li>
     * </ul>
     * <p>
     * Any other request passed to the method causes an <code>OGCWebServiceException</code> to be thrown.
     * </p>
     * 
     * @param request
     * @param doc
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWSSRequest( String request, Document doc )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = "" + IDGenerator.getInstance().generateUniqueID();
        if ( ( "GetCapabilities" ).equals( request ) ) {
            ogcRequest = WSSGetCapabilities.create( id, doc.getDocumentElement() );
        } else if ( ( "GetSession" ).equals( request ) ) {
            ogcRequest = GetSession.create( id, doc.getDocumentElement() );
        } else if ( ( "CloseSession" ).equals( request ) ) {
            ogcRequest = CloseSession.create( id, doc.getDocumentElement() );
        } else if ( ( "DoService" ).equals( request ) ) {
            ogcRequest = DoService.create( id, doc.getDocumentElement() );
        } else {
            throw new OGCWebServiceException( "Unknown / unimplemented WSS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>key value pair</code>. The returned request
     * will be a WSS request. The type of request is determined by also submitted request name. Known requests are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>GetSession</li>
     * <li>CloseSession</li>
     * <li>DoService</li>
     * </ul>
     * <p>
     * Any other request passed to the method causes an <code>OGCWebServiceException</code> to be thrown.
     * </p>
     * 
     * @param request
     * @param kvp
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWSSRequest( String request, Map<String, String> kvp )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = "" + IDGenerator.getInstance().generateUniqueID();
        if ( ( "GetCapabilities" ).equals( request ) ) {
            ogcRequest = WSSGetCapabilities.create( id, kvp );
        } else if ( ( "GetSession" ).equals( request ) ) {
            ogcRequest = GetSession.create( id, kvp );
        } else if ( ( "CloseSession" ).equals( request ) ) {
            ogcRequest = CloseSession.create( id, kvp );
        } else if ( ( "DoService" ).equals( request ) ) {
            ogcRequest = DoService.create( id, kvp );
        } else {
            throw new OGCWebServiceException( "Unknown / unimplemented WSS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>Document</code>. The returned request will be
     * a WAS request. The type of request is determined by also submitted request name. Known requests are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>GetSession</li>
     * <li>CloseSession</li>
     * </ul>
     * <p>
     * Any other request passed to the method causes an <code>OGCWebServiceException</code> to be thrown.
     * </p>
     * 
     * @param request
     * @param doc
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWASRequest( String request, Document doc )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = "" + IDGenerator.getInstance().generateUniqueID();
        // note: DescribeUser is only supported through KVP
        if ( ( "GetCapabilities" ).equals( request ) ) {
            ogcRequest = WASGetCapabilities.create( id, doc.getDocumentElement() );
        } else if ( ( "GetSession" ).equals( request ) ) {
            ogcRequest = GetSession.create( id, doc.getDocumentElement() );
        } else if ( ( "CloseSession" ).equals( request ) ) {
            ogcRequest = CloseSession.create( id, doc.getDocumentElement() );
        } else {
            throw new OGCWebServiceException( "Unknown / unimplemented WAS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>key value pair</code>. The returned request
     * will be a WAS request. The type of request is determined by also submitted request name. Known requests are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>GetSession</li>
     * <li>CloseSession</li>
     * <li>DescribeUser</li>
     * </ul>
     * <p>
     * Any other request passed to the method causes an <code>OGCWebServiceException</code> to be thrown.
     * </p>
     * 
     * @param request
     * @param kvp
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWASRequest( String request, Map<String, String> kvp )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        kvp.put( "SERVICE", WAS_SERVICE_NAME );
        String id = "" + IDGenerator.getInstance().generateUniqueID();
        if ( ( "GetCapabilities" ).equals( request ) ) {
            ogcRequest = WASGetCapabilities.create( id, kvp );
        } else if ( ( "GetSession" ).equals( request ) ) {
            ogcRequest = GetSession.create( id, kvp );
        } else if ( ( "CloseSession" ).equals( request ) ) {
            ogcRequest = CloseSession.create( id, kvp );
        } else if ( ( "DescribeUser" ).equals( request ) ) {
            ogcRequest = new DescribeUser( id, kvp );
        } else {
            throw new OGCWebServiceException( "Unknown / unimplemented WAS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * return the type of service the passed request targets
     * 
     * @param service
     * @param request
     * @param doc
     * @return the type of service the passed request targets
     */
    public static String getTargetService( String service, String request, Document doc ) {

        if ( WMS_SERVICE_NAME.equals( service ) || isWMSRequest( request ) ) {
            return WMS_SERVICE_NAME;
        } else if ( WFS_SERVICE_NAME.equals( service ) || isWFSRequest( request, doc ) ) {
            return WFS_SERVICE_NAME;
        } else if ( WCS_SERVICE_NAME.equals( service ) || isWCSRequest( request ) ) {
            return WCS_SERVICE_NAME;
        } else if ( CSW_SERVICE_NAME_EBRIM.equals( service ) || CSW_SERVICE_NAME.equals( service )
                    || isCSWRequest( request, doc ) ) {
            return CSW_SERVICE_NAME;
        } else if ( SOS_SERVICE_NAME.equals( service ) || isSOSRequest( request ) ) {
            return SOS_SERVICE_NAME;
        } else if ( WPVS_SERVICE_NAME.equals( service ) || isWPVSRequest( request ) ) {
            return WPVS_SERVICE_NAME;
        } else if ( WMPS_SERVICE_NAME.equals( service ) || isWMPSRequest( doc, request ) ) {
            return WMPS_SERVICE_NAME;
        } else if ( WPS_SERVICE_NAME.equals( service ) || isWPSRequest( request ) ) {
            return WPS_SERVICE_NAME;
        } else if ( WAS_SERVICE_NAME.equals( service ) || isWASRequest( request ) ) {
            return WAS_SERVICE_NAME;
        } else if ( WSS_SERVICE_NAME.equals( service ) || isWSSRequest( request ) ) {
            return WSS_SERVICE_NAME;
        } else if ( WCTS_SERVICE_NAME.equals( service ) || isWCTSRequest( doc ) ) {
            return WCTS_SERVICE_NAME;
        } else {
            return "unknown";
        }

    }

    /**
     * @param doc
     *            to check
     * @return true if the namespace of the given dom-xml document equals {@link CommonNamespaces#WCTSNS}.
     */
    private static boolean isWCTSRequest( Document doc ) {
        if ( doc != null ) {
            Element root = doc.getDocumentElement();
            if ( root != null ) {
                String ns = root.getNamespaceURI();
                return CommonNamespaces.WCTSNS.toASCIIString().equals( ns )
                       || CommonNamespaces.DEEGREEWCTS.toASCIIString().equals( ns );
            }
        }
        return false;
    }

    /**
     * returns true if the request is a WMPS request
     * 
     * @param request
     *            name, e.g. 'GetCapabilities' name, e.g. 'PrintMap'
     * @return true if the request is a WMPS request
     */
    private static boolean isWMPSRequest( Document doc, String request ) {
        if ( doc != null ) {
            Element root = doc.getDocumentElement();
            if ( root != null ) {
                String ns = root.getNamespaceURI();
                if ( CommonNamespaces.WMPSNS.toASCIIString().equals( ns ) ) {
                    return true;
                }
            }
        }
        if ( ( "PrintMap".equals( request ) ) ) {
            return true;
        } else if ( ( "GetAvailableTemplates".equals( request ) ) ) {
            return true;
        } else if ( ( "DescribeTemplate".equals( request ) ) ) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the request is a WMS request
     * 
     * @param request
     * @return true if the request is a WMS request
     */
    private static boolean isWMSRequest( String request ) {
        if ( "GetMap".equals( request ) || "map".equals( request ) || "GetFeatureInfo".equals( request )
             || "feature_info".equals( request ) || "GetLegendGraphic".equals( request )
             || "GetStyles".equals( request ) || "PutStyles".equals( request ) || "DescribeLayer".equals( request ) ) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the request is a WFS request
     * 
     * @param request
     * @param doc
     * @return true if the request is a WFS request
     */
    private static boolean isWFSRequest( String request, Document doc ) {
        if ( doc != null ) {
            String s = doc.getDocumentElement().getNamespaceURI();
            if ( CommonNamespaces.WFSNS.toString().equals( s ) ) {
                return true;
            }
        } else {
            if ( "DescribeFeatureType".equals( request ) || "GetFeature".equals( request )
                 || "GetFeatureWithLock".equals( request ) || "GetGmlObject".equals( request )
                 || "Lock".equals( request ) || "Transaction".equals( request ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns true if the request is a WCS request
     * 
     * @param request
     * @return true if the request is a WCS request
     */
    private static boolean isWCSRequest( String request ) {
        if ( "GetCoverage".equals( request ) || "DescribeCoverage".equals( request ) ) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the request is a CSW request
     * 
     * @param request
     * @return true if the request is a CSW request
     */
    private static boolean isCSWRequest( String request, Document doc ) {
        if ( doc != null ) {
            String s = doc.getDocumentElement().getNamespaceURI();
            if ( CommonNamespaces.CSWNS.toString().equals( s ) || CommonNamespaces.CSW202NS.toString().equals( s ) ) {
                return true;
            }
        } else {
            if ( CatalogueOperationsMetadata.GET_RECORDS_NAME.equals( request )
                 || CatalogueOperationsMetadata.DESCRIBE_RECORD_NAME.equals( request )
                 || CatalogueOperationsMetadata.GET_RECORD_BY_ID_NAME.equals( request )
                 || CatalogueOperationsMetadata.GET_DOMAIN_NAME.equals( request )
                 || CatalogueOperationsMetadata.HARVEST_NAME.equals( request ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns true if the request is a SOS request
     * 
     * @param request
     * @return true if the request is a SOS request
     */
    private static boolean isSOSRequest( String request ) {
        if ( "GetObservation".equals( request ) || "DescribeSensor".equals( request )
             || "DescribePlatform".equals( request ) ) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the request is a WPVS request
     * 
     * @param request
     *            name, e.g. 'GetView'
     * @return true if the request is a WPVS request
     */
    private static boolean isWPVSRequest( String request ) {
        if ( "GetView".equals( request ) ) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the request is a WPS request
     * 
     * @param request
     *            name, e.g. 'GetCapabilities' name, e.g. 'DescribeProcess', e.g. 'Exceute'
     * @return true if the request is a WPS request
     */
    private static boolean isWPSRequest( String request ) {
        if ( "DescribeProcess".equals( request ) || "Execute".equals( request ) ) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the request is a WAS request
     * 
     * @param request
     *            name, e.g. 'GetSession' name, e.g. 'CloseSession', e.g. 'GetSAMLResponse'
     * @return true if and only if the request contains one of the above Strings
     */
    private static boolean isWASRequest( String request ) {
        if ( "GetSession".equals( request ) || "CloseSession".equals( request ) || "GetSAMLResponse".equals( request )
             || "DescribeUser".equals( request ) ) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the request is a WSS request
     * 
     * @param request
     *            name, e.g. 'GetSession' name, e.g. 'CloseSession', e.g. 'GetSAMLResponse'
     * @return true if and only if the request contains one of the above Strins
     */
    private static boolean isWSSRequest( String request ) {
        if ( "GetSession".equals( request ) || "CloseSession".equals( request ) || "DoService".equals( request ) ) {
            return true;
        }
        return false;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>Object</code>. the returned request will be a
     * WCS request. The type of request is determind by the the also passed 'request' parameter. Possible requests are:
     * <ul>
     * <li>GetCapabilities
     * <li>GetCoverage
     * <li>DescribeCoverage
     * </ul>
     * <p>
     * Any other request passed to the method causes an exception to be thrown.
     * </p>
     * 
     * @param request
     * @param req
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    @SuppressWarnings("unchecked")
    private static OGCWebServiceRequest getWMSRequest( String request, Object req )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = "" + IDGenerator.getInstance().generateUniqueID();
        try {
            Map<String, String> map = null;
            if ( req instanceof Map ) {
                map = (Map<String, String>) req;
                map.put( "ID", id );
            }
            if ( request.equals( "GetCapabilities" ) || "capabilities".equals( request ) ) {
                if ( map != null ) {
                    // defaulting to 1.1.1 is not possible because of spec requirements
                    // if( ( map.get( "VERSION" ) == null ) && ( map.get( "WMTVER" ) == null ) )
                    // {
                    // map.put( "VERSION", "1.3.0" );
                    // }
                    ogcRequest = WMSGetCapabilities.create( map );
                }
            } else if ( request.equals( "GetMap" ) || request.equals( "map" ) ) {
                if ( map != null ) {
                    ogcRequest = GetMap.create( map );
                } else {
                    ogcRequest = GetMap.create( id, (Document) req );
                }
            } else if ( request.equals( "GetFeatureInfo" ) || request.equals( "feature_info" ) ) {
                if ( map != null ) {
                    ogcRequest = GetFeatureInfo.create( map );
                }
            } else if ( request.equals( "GetLegendGraphic" ) ) {
                if ( map != null ) {
                    ogcRequest = GetLegendGraphic.create( map );
                }
            } else if ( request.equals( "DescribeLayer" ) ) {
                if ( map != null ) {
                    ogcRequest = DescribeLayer.create( map );
                }
            } else {
                throw new OGCWebServiceException( "Unknown WMS request type: '" + request + "'." );
            }
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>AbstractOGCWebServiceRequest</code> from the passed <code>Object</code>. the returned request
     * will be a WCS request. The type of request is determind by the the also passed 'request' parameter. Possible
     * requests are:
     * <ul>
     * <li>GetCapabilities
     * <li>GetCoverage
     * <li>DescribeCoverage
     * </ul>
     * Any other request passed to the method causes an exception to be thrown.
     * 
     * @param request
     *            a string describing the request type.
     * @param req
     *            the kvp-encoded request
     * @return one of the above mentioned Requests.
     * @throws OGCWebServiceException
     *             if the requested operation is not one of the above mentioned, or something went wrong while creating
     *             the request.
     */
    private static AbstractOGCWebServiceRequest getWCSRequest( String request, Map<String, String> req )
                            throws OGCWebServiceException {
        AbstractOGCWebServiceRequest ogcRequest = null;
        String id = Long.toString( IDGenerator.getInstance().generateUniqueID() );
        req.put( "ID", id );
        if ( request.equals( "GetCapabilities" ) ) {
            ogcRequest = WCSGetCapabilities.create( req );
        } else if ( request.equals( "GetCoverage" ) ) {
            ogcRequest = GetCoverage.create( req );
        } else if ( request.equals( "DescribeCoverage" ) ) {
            ogcRequest = DescribeCoverage.create( req );
        } else {
            throw new OGCWebServiceException( "Unknown WCS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>AbstractOGCWebServiceRequest</code> from the passed <code>Docuement</code>. the returned request
     * will be a WCS request. The type of request is determind by the the also passed 'request' parameter. Possible
     * requests are:
     * <ul>
     * <li>GetCapabilities
     * <li>GetCoverage
     * <li>DescribeCoverage
     * </ul>
     * Any other request passed to the method causes an exception to be thrown.
     * 
     * @param request
     *            a string describing the request type.
     * @param req
     *            the XML-encoded request
     * @return one of the above mentioned Requests.
     * @throws OGCWebServiceException
     *             if the requested operation is not one of the above mentioned, or something went wrong while creating
     *             the request.
     */
    private static AbstractOGCWebServiceRequest getWCSRequest( String request, Document req )
                            throws OGCWebServiceException {
        AbstractOGCWebServiceRequest ogcRequest = null;
        String id = Long.toString( IDGenerator.getInstance().generateUniqueID() );
        if ( request.equals( "GetCapabilities" ) ) {
            ogcRequest = WCSGetCapabilities.create( id, req );
        } else if ( request.equals( "GetCoverage" ) ) {
            ogcRequest = GetCoverage.create( id, req );
        } else if ( request.equals( "DescribeCoverage" ) ) {
            ogcRequest = DescribeCoverage.create( id, req );
        } else {
            throw new OGCWebServiceException( "Unknown WCS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>AbstractOGCWebServiceRequest</code> from the passed <code>Object</code>. The returned request
     * will be a <code>CSW</code> request. The type of request is determined by the the also passed 'request' parameter.
     * Allowed values for the request parameter are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>GetRecords</li>
     * <li>GetRecordsByID</li>
     * <li>DescribeRecord</li>
     * <li>GetDomain, will cause an exception to be thrown</li>
     * <li>Transaction, will cause an exception to be thrown</li>
     * <li>Harvest</li>
     * </ul>
     * 
     * Any other request passed to the method causes an exception to be thrown.
     * 
     * @param request
     *            a string describing the request type.
     * @param req
     *            the KVP-encoded request
     * @return one of the above mentioned Requests.
     * @throws OGCWebServiceException
     *             if the requested operation is not one of the above mentioned, not supported or something went wrong
     *             while creating the request.
     */
    private static AbstractOGCWebServiceRequest getCSWRequest( String request, Map<String, String> req )
                            throws OGCWebServiceException {
        AbstractOGCWebServiceRequest ogcRequest = null;
        String id = Long.toString( IDGenerator.getInstance().generateUniqueID() );
        LOG.logDebug( StringTools.concat( 200, "Creating CSW request '", request, "' with ID=", id, "/type:",
                                          req.getClass().getName() ) );

        if ( OperationsMetadata.GET_CAPABILITIES_NAME.equals( request ) ) {
            req.put( "ID", id );
            ogcRequest = CatalogueGetCapabilities.create( req );
        } else if ( CatalogueOperationsMetadata.GET_RECORDS_NAME.equals( request ) ) {
            req.put( "ID", id );
            ogcRequest = GetRecords.create( req );
        } else if ( CatalogueOperationsMetadata.GET_RECORD_BY_ID_NAME.equals( request ) ) {
            // req.put( "ID", id );
            ogcRequest = GetRecordById.create( id, req );
        } else if ( CatalogueOperationsMetadata.GET_REPOSITORY_ITEM.equals( request ) ) {
            req.put( "REQUESTID", id );
            ogcRequest = GetRepositoryItem.create( req );
        } else if ( CatalogueOperationsMetadata.DESCRIBE_RECORD_NAME.equals( request ) ) {
            req.put( "ID", id );
            ogcRequest = DescribeRecord.create( req );
        } else if ( CatalogueOperationsMetadata.GET_DOMAIN_NAME.equals( request ) ) {
            // TODO
            throw new OGCWebServiceException( CatalogueOperationsMetadata.TRANSACTION_NAME + " is not supported." );
        } else if ( CatalogueOperationsMetadata.TRANSACTION_NAME.equals( request ) ) {
            throw new OGCWebServiceException( CatalogueOperationsMetadata.TRANSACTION_NAME
                                              + " through HTTP Get is not supported." );
        } else if ( CatalogueOperationsMetadata.HARVEST_NAME.equals( request ) ) {
            req.put( "ID", id );
            ogcRequest = Harvest.create( req );
        } else {
            throw new OGCWebServiceException( "Unknown CSW request type: '" + request + "'." );
        }
        LOG.logDebug( "CSW request created: " + ogcRequest );
        return ogcRequest;
    }

    /**
     * Creates an <code>AbstractOGCWebServiceRequest</code> from the passed <code>Object</code>. The returned request
     * will be a <code>CSW</code> request. The type of request is determined by the the also passed 'request' parameter.
     * Allowed values for the request parameter are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>GetRecords</li>
     * <li>GetRecordsByID</li>
     * <li>DescribeRecord</li>
     * <li>GetDomain, will cause an exception to be thrown</li>
     * <li>Transaction</li>
     * <li>Harvest, will cause an exception to be thrown</li>
     * </ul>
     * 
     * Any other request passed to the method causes an exception to be thrown.
     * 
     * @param request
     *            a string describing the request type.
     * @param req
     *            the XML-encoded request
     * @return one of the above mentioned Requests.
     * @throws OGCWebServiceException
     *             if the requested operation is not one of the above mentioned, or something went wrong while creating
     *             the request.
     */
    private static AbstractOGCWebServiceRequest getCSWRequest( String request, Document req )
                            throws OGCWebServiceException {
        AbstractOGCWebServiceRequest ogcRequest = null;
        String id = Long.toString( IDGenerator.getInstance().generateUniqueID() );
        LOG.logDebug( StringTools.concat( 200, "Creating CSW request '", request, "' with ID=", id, "/type:",
                                          req.getClass().getName() ) );
        Element docElem = req.getDocumentElement();
        if ( OperationsMetadata.GET_CAPABILITIES_NAME.equals( request ) ) {
            ogcRequest = CatalogueGetCapabilities.create( id, docElem );
        } else if ( CatalogueOperationsMetadata.GET_RECORDS_NAME.equals( request ) ) {
            ogcRequest = GetRecords.create( id, docElem );
        } else if ( CatalogueOperationsMetadata.GET_RECORD_BY_ID_NAME.equals( request ) ) {
            ogcRequest = GetRecordById.create( id, docElem );
        } else if ( CatalogueOperationsMetadata.GET_REPOSITORY_ITEM.equals( request ) ) {
            throw new OGCWebServiceException( CatalogueOperationsMetadata.GET_REPOSITORY_ITEM + " is not supported." );
        } else if ( CatalogueOperationsMetadata.DESCRIBE_RECORD_NAME.equals( request ) ) {
            ogcRequest = DescribeRecord.create( id, docElem );
        } else if ( CatalogueOperationsMetadata.GET_DOMAIN_NAME.equals( request ) ) {
            // TODO
            throw new OGCWebServiceException( CatalogueOperationsMetadata.TRANSACTION_NAME + " is not supported." );
        } else if ( CatalogueOperationsMetadata.TRANSACTION_NAME.equals( request ) ) {
            ogcRequest = org.deegree.ogcwebservices.csw.manager.Transaction.create( id, docElem );
        } else if ( CatalogueOperationsMetadata.HARVEST_NAME.equals( request ) ) {
            throw new OGCWebServiceException( CatalogueOperationsMetadata.HARVEST_NAME
                                              + " through HTTP post is not supported." );
        } else {
            throw new OGCWebServiceException( "Unknown CSW request type: '" + request + "'." );
        }
        LOG.logDebug( "CSW request created: " + ogcRequest );
        return ogcRequest;
    }

    
    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>XML-Document</code>. the returned request will
     * be a WPVS request. The type of request is determind by the the also passed 'request' parameter. Possible requests
     * are:
     * <ul>
     * <li>GetCapabilities
     * <li>GetView
     * </ul>
     * <p>
     * Any other request passed to the method causes an exception to be thrown.
     * </p>
     * 
     * @param requestName
     *            name of the request, one of GetCapabilities or GetView
     * @param request
     *            the request as an xml document
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    @SuppressWarnings("unused")
    private static OGCWebServiceRequest getWPVSRequest( String requestName, Document request )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = Long.toString( IDGenerator.getInstance().generateUniqueID() );

        if ( OperationsMetadata.GET_CAPABILITIES_NAME.equals( requestName ) ) {
            // ogcRequest = WPVSGetCapabilities.create(id, (Document) req);
        } else if ( "GetView".equals( requestName ) ) {
            // ogcRequest = GetView.create( req );
        } else {
            throw new OGCWebServiceException( "Unknown WPVS request type: '" + requestName + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>KVP-Map</code>. the returned request will be a
     * WPVS request. The type of request is determind by the the also passed 'request' parameter. Possible requests are:
     * <ul>
     * <li>GetCapabilities
     * <li>GetView
     * </ul>
     * <p>
     * Any other request passed to the method causes an exception to be thrown.
     * </p>
     * 
     * @param requestName
     *            name of the request, one of GetCapabilities or GetView
     * @param request
     *            the actual parameters of the request
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWPVSRequest( String requestName, Map<String, String> request )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = Long.toString( IDGenerator.getInstance().generateUniqueID() );

        if ( OperationsMetadata.GET_CAPABILITIES_NAME.equals( requestName ) ) {
            request.put( "ID", id );
            ogcRequest = WPVSGetCapabilities.create( request );
        } else if ( "GetView".equals( requestName ) ) {
            ogcRequest = GetView.create( request );
        } else if ( "Get3DFeatureInfo".equals( requestName ) ) {
            ogcRequest = Get3DFeatureInfo.create( request );
        } else {
            throw new OGCWebServiceException( "Unknown WPVS request type: '" + requestName + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>AbstractOGCWebServiceRequest</code> from the passed <code>Object</code>. the returned request
     * will be a WMPS request. The type of request is determind by the the also passed 'request' parameter. Possible
     * requests are:
     * <ul>
     * <li>GetCapabilities
     * </ul>
     * Any other request passed to the method causes an exception to be thrown.
     * 
     * @param request
     * @param doc
     * @param req
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWMPSRequest( String request, Document doc )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;

        if ( request.equals( "PrintMap" ) ) {
            try {
                ogcRequest = PrintMap.create( doc.getDocumentElement() );
            } catch ( Exception e ) {
                throw new OGCWebServiceException( "Error creating a Print Map object for the request '" + request
                                                  + "'. " + e.getMessage() );
            }
        } else if ( request.equals( "GetAvailableTemplates" ) ) {
            try {
                ogcRequest = GetAvailableTemplates.create( doc.getDocumentElement() );
            } catch ( Exception e ) {
                throw new OGCWebServiceException( "Error creating a GetAvailableTemplates object for the request '"
                                                  + request + "'. " + e.getMessage() );
            }
        } else if ( request.equals( "DescribeTemplate" ) ) {
            try {
                ogcRequest = DescribeTemplate.create( doc.getDocumentElement() );
            } catch ( Exception e ) {
                throw new OGCWebServiceException( "Error creating a DescribeTemplate object for the request '"
                                                  + request + "'. " + e.getMessage() );
            }
        } else {
            throw new OGCWebServiceException( "Unknown / unimplemented WMPS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>AbstractOGCWebServiceRequest</code> from the passed <code>Object</code>. the returned request
     * will be a WMPS request. The type of request is determind by the the also passed 'request' parameter. Possible
     * requests are:
     * <ul>
     * <li>GetCapabilities
     * </ul>
     * Any other request passed to the method causes an exception to be thrown.
     * 
     * @param request
     * @param map
     * @param req
     * @return OGCWebServiceRequest
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWMPSRequest( String request, Map<String, String> map )
                            throws InconsistentRequestException, InvalidParameterValueException {
        OGCWebServiceRequest ogcRequest = null;
        map.put( "ID", "" + IDGenerator.getInstance().generateUniqueID() );
        if ( request.equals( "GetCapabilities" ) ) {
            ogcRequest = WMPSGetCapabilities.create( map );
        } else if ( request.equals( "PrintMap" ) ) {
            ogcRequest = PrintMap.create( map );
        } else if ( request.equals( "DescribeTemplate" ) ) {
            ogcRequest = DescribeTemplate.create( map );
        } else if ( request.equals( "GetAvailableTemplates" ) ) {
            ogcRequest = GetAvailableTemplates.create( map );
        } else {
            throw new InvalidParameterValueException( "Unknown WMPS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>Map</code>. The returned request will be a WPS
     * request. The type of request is determined by also submitted request name. Known requests are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>DescribeProcess</li>
     * <li>Execute</li>
     * </ul>
     * <p>
     * Any other request passed to the method causes an <code>OGCWebServiceException</code> to be thrown.
     * </p>
     * 
     * @param request
     * @param map
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWPSRequest( String request, Map<String, String> map )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        map.put( "ID", "" + IDGenerator.getInstance().generateUniqueID() );
        if ( "GetCapabilities".equals( request ) ) {
            ogcRequest = WPSGetCapabilities.create( map );
        } else if ( "DescribeProcess".equals( request ) ) {
            ogcRequest = DescribeProcessRequest.create( map );
        } else if ( "Execute".equals( request ) ) {
            ogcRequest = ExecuteRequest.create( map );
        } else {
            throw new InvalidParameterValueException( "Unknown WPS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * Creates an <code>OGCWebServiceRequest</code> from the passed <code>Document</code>. The returned request will be
     * a WPS request. The type of request is determined by also submitted request name. Known requests are:
     * <ul>
     * <li>GetCapabilities</li>
     * <li>DescribeProcess</li>
     * <li>Execute</li>
     * </ul>
     * <p>
     * Any other request passed to the method causes an <code>OGCWebServiceException</code> to be thrown.
     * </p>
     * 
     * @param request
     * @param doc
     * @return created <code>OGCWebServiceRequest</code>
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWPSRequest( String request, Document doc )
                            throws OGCWebServiceException {
        OGCWebServiceRequest ogcRequest = null;
        String id = "" + IDGenerator.getInstance().generateUniqueID();
        if ( "GetCapabilities".equals( request ) ) {
            ogcRequest = WPSGetCapabilities.create( id, doc.getDocumentElement() );
        } else if ( "DescribeProcess".equals( request ) ) {
            ogcRequest = DescribeProcessRequest.create( id, doc.getDocumentElement() );

        } else if ( "Execute".equals( request ) ) {
            ogcRequest = ExecuteRequest.create( id, doc.getDocumentElement() );

        } else {
            throw new OGCWebServiceException( "Unknown WPS request type: '" + request + "'." );
        }
        return ogcRequest;
    }

    /**
     * @param request
     *            containing the local-name of the top root element.
     * @param doc
     * @return
     * @throws IllegalArgumentException
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWCTSRequest( String request, Document doc )
                            throws OGCWebServiceException, IllegalArgumentException {
        OGCWebServiceRequest result = null;
        String requestID = Long.toString( IDGenerator.getInstance().generateUniqueID() );
        if ( "GetResourceByID".equals( request ) ) {
            result = new GetResourceByIDDocument( requestID, doc.getDocumentElement() ).getResourceById();
        } else if ( "GetCapabilities".equals( request ) ) {
            result = new WCTSGetCapabilitiesDocument( requestID, doc.getDocumentElement() ).getGetCapabilities();
        } else if ( "Transform".equals( request ) ) {
            result = new TransformDocument( requestID, doc.getDocumentElement() ).getTransformRequest();
        } else if ( "IsTransformable".equals( request ) ) {
            result = new IsTransformableDocument( requestID, doc.getDocumentElement() ).getIsTransformable();
        } else if ( "GetTransformation".equals( request ) ) {
            throw new OGCWebServiceException( "GetTransformation requests are currently not supported by the WCTS",
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
            // result = new Document(requestID, doc.getDocumentElement() ).getIsTransformable();
        } else {
            throw new OGCWebServiceException( request + " is not known to the WCTS",
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
        }
        return result;
    }

    /**
     * @param request
     * @param map
     * @return
     * @throws OGCWebServiceException
     */
    private static OGCWebServiceRequest getWCTSRequest( String request, Map<String, String> map )
                            throws OGCWebServiceException {
        OGCWebServiceRequest result = null;
        String requestID = Long.toString( IDGenerator.getInstance().generateUniqueID() );
        if ( "GetResourceByID".equals( request ) ) {
            result = GetResourceByID.create( requestID, map );
        } else if ( "GetCapabilities".equals( request ) ) {
            result = WCTSGetCapabilities.create( requestID, map );
        } else if ( "Transform".equals( request ) ) {
            result = Transform.create( requestID, map );
        } else if ( "IsTransformable".equals( request ) ) {
            result = IsTransformable.create( requestID, map );
        } else if ( "GetTransformation".equals( request ) ) {
            throw new OGCWebServiceException( "GetTransformation requests are currently not supported by the WCTS",
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
            // result = new Document(requestID, doc.getDocumentElement() ).getIsTransformable();
        } else {
            throw new OGCWebServiceException( request + " is not known to the WCTS",
                                              ExceptionCode.OPERATIONNOTSUPPORTED );
        }
        return result;
    }

}
