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

package org.deegree.enterprise.servlet;

import static org.deegree.framework.util.CharsetUtils.getSystemCharset;
import static org.deegree.owscommon.XMLFactory.exportExceptionReportWFS;
import static org.deegree.owscommon.XMLFactory.exportExceptionReportWFS100;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.CollectionUtils;
import org.deegree.framework.util.CollectionUtils.Mapper;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureException;
import org.deegree.model.feature.FeatureTupleCollection;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.WFServiceFactory;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.XMLFactory_1_0_0;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest;
import org.deegree.ogcwebservices.wfs.operation.AugmentableGetFeature;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureWithLock;
import org.deegree.ogcwebservices.wfs.operation.GetGmlObject;
import org.deegree.ogcwebservices.wfs.operation.GmlResult;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.LockFeatureResponse;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.deegree.ogcwebservices.wfs.operation.transaction.Update;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Web servlet client for WFS.
 * <p>
 * NOTE: Currently, the <code>WFSHandler</code> is responsible for the pre- and postprocessing of virtual feature types.
 * For virtual feature types, requests and responses are transformed using an XSL-script. Virtual feature types can also
 * provide their own schema document that is sent as a response to {@link DescribeFeatureType} requests.
 * <p>
 * The heuristics that determines whether pre- or postprocessing is necessary, is not very accurate; check the methods:
 * <ul>
 * <li><code>#determineFormat(DescribeFeatureType, WFSConfiguration)</code></li>
 * <li><code>#determineFormat(GetFeature, WFSConfiguration)</code></li>
 * <li><code>#determineFormat(Transaction, WFSConfiguration)</code></li>
 * </ul>
 * <p>
 * The code for the handling of virtual features should probably be moved to the {@link WFService} class.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class WFSHandler extends AbstractOWServiceHandler {

    private static ILogger LOG = LoggerFactory.getLogger( WFSHandler.class );

    private static Map<URL, XSLTDocument> xsltCache;
    static {
        if ( xsltCache == null ) {
            xsltCache = new HashMap<URL, XSLTDocument>();
        }
    }

    private URL getGmlObjectUrl;

    public WFSHandler() {
        for ( Operation o : WFServiceFactory.getConfiguration().getOperationsMetadata().getOperations() ) {
            if ( o.getName().equals( "GetGmlObject" ) ) {
                getGmlObjectUrl = ( (HTTP) o.getDCPs()[0].getProtocol() ).getGetOnlineResources()[0];
            }
        }
    }

    /**
     * Performs the given {@link OGCWebServiceRequest} on the {@link WFService} and sends the response to the given
     * {@link HttpServletResponse} object.
     * 
     * @param request
     *            OGCWebServiceRequest to be performed
     * @param httpResponse
     *            servlet response object to write to
     * @throws ServiceException
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse httpResponse )
                            throws ServiceException {

        LOG.logDebug( "Performing request: " + request.toString() );

        try {
            WFService service = WFServiceFactory.createInstance();
            if ( request instanceof WFSGetCapabilities ) {
                performGetCapabilities( service, (WFSGetCapabilities) request, httpResponse );
            } else if ( request instanceof DescribeFeatureType ) {
                ( (DescribeFeatureType) request ).guessMissingNamespaces( service );
                performDescribeFeatureType( service, (DescribeFeatureType) request, httpResponse );
            } else if ( request instanceof GetFeature ) {
                ( (GetFeature) request ).guessAllMissingNamespaces( service );
                performGetFeature( service, (GetFeature) request, httpResponse );
            } else if ( request instanceof Transaction ) {
                ( (Transaction) request ).guessMissingNamespaces( service );
                performTransaction( service, (Transaction) request, httpResponse );
            } else if ( request instanceof LockFeature ) {
                ( (LockFeature) request ).guessMissingNamespaces( service );
                performLockFeature( service, (LockFeature) request, httpResponse );
            } else if ( request instanceof GetGmlObject ) {
                performGetGmlObject( service, (GetGmlObject) request, httpResponse );
            } else {
                assert false : "Unhandled WFS request type: '" + request.getClass().getName() + "'";
            }
        } catch ( OGCWebServiceException e ) {
            LOG.logInfo( "Error while performing WFS request.", e );
            sendVersionedException( httpResponse, e, "1.0.0".equals( request.getVersion() ) );
        } catch ( Exception e ) {
            LOG.logError( "Fatal error while performing WFS request.", e );
            sendVersionedException( httpResponse, new OGCWebServiceException( getClass().getName(), e.getMessage() ),
                                    "1.0.0".equals( request.getVersion() ) );
        }
    }

    /**
     * Performs a {@link WFSGetCapabilities} request and sends the response to the given {@link HttpServletResponse}
     * object.
     * 
     * @param service
     *            WFService instance to be used
     * @param request
     *            GetCapabilities request to be performed
     * @param httpResponse
     *            servlet response object to write to
     * @throws OGCWebServiceException
     */
    private void performGetCapabilities( WFService service, WFSGetCapabilities request, HttpServletResponse httpResponse )
                            throws OGCWebServiceException {

        WFSCapabilities capa = (WFSCapabilities) service.doService( request );

        try {
            httpResponse.setContentType( "application/xml" );
            WFSCapabilitiesDocument document = null;
            String version = request.getVersion();
            boolean use_1_1_0 = true;
            LOG.logDebug( "Version of incoming request is: " + version );
            if ( "1.0.0".compareTo( version ) >= 0 ) {
                use_1_1_0 = false;
            }
            if ( !use_1_1_0 ) {
                document = XMLFactory_1_0_0.getInstance().export( (WFSConfiguration) capa );
            } else {
                document = XMLFactory.export( capa, request.getSections() );
            }
            OutputStream os = httpResponse.getOutputStream();
            document.write( os );
            os.close();
        } catch ( IOException e ) {
            LOG.logError( "Error sending GetCapabilities response to client.", e );
        }
    }

    /**
     * Performs a {@link DescribeFeatureType} request and sends the response to the given {@link HttpServletResponse}
     * object.
     * 
     * @param service
     *            WFService instance to be used
     * @param request
     *            DescribeFeatureType request to be performed
     * @param httpResponse
     *            servlet response object to write to
     * @throws OGCWebServiceException
     */
    private void performDescribeFeatureType( WFService service, DescribeFeatureType request,
                                             HttpServletResponse httpResponse )
                            throws OGCWebServiceException {

        WFSConfiguration config = (WFSConfiguration) service.getCapabilities();
        FormatType format = determineFormat( request, config, service );

        XMLFragment schemaDoc = null;

        if ( format.getSchemaLocation() != null ) {

            // check for requested types <-> configured types
            WFSFeatureType[] featureTypes = config.getFeatureTypeList().getFeatureTypes();
            HashSet<String> set = new HashSet<String>( featureTypes.length );
            set.addAll( CollectionUtils.map( featureTypes, new Mapper<String, WFSFeatureType>() {
                public String apply( WFSFeatureType u ) {
                    return u.getName().getLocalName();
                }
            } ) );
            for ( QualifiedName name : request.getTypeNames() ) {
                if ( !set.contains( name.getLocalName() ) ) {
                    String msg = Messages.getMessage( "WFS_FEATURE_TYPE_UNKNOWN", name.getLocalName() );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
            }

            // read special schema for virtual format
            try {
                schemaDoc = new XMLFragment( format.getSchemaLocation().toURL() );
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "WFS_VIRTUAL_FORMAT_SCHEMA_READ_ERROR", format.getSchemaLocation(),
                                                  format.getValue(), e );
                LOG.logError( msg, e );
                throw new OGCWebServiceException( getClass().getName(), msg );
            }
        } else {
            // get schema from WFService
            FeatureTypeDescription ftDescription = (FeatureTypeDescription) service.doService( request );
            schemaDoc = ftDescription.getFeatureTypeSchema();
        }

        httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
        try {
            schemaDoc.write( httpResponse.getOutputStream() );
        } catch ( IOException e ) {
            LOG.logError( "Error sending DescribeFeatureType response to client.", e );
        }
    }

    /**
     * Performs a {@link GetFeature} request and sends the response to the given {@link HttpServletResponse} object.
     * 
     * @param service
     *            WFService instance to be used
     * @param request
     *            GetFeature request to be performed
     * @param httpResponse
     *            servlet response object to write to
     * @throws OGCWebServiceException
     */
    private void performGetFeature( WFService service, GetFeature request, HttpServletResponse httpResponse )
                            throws OGCWebServiceException {

        // hack: augment request if it was a KVP request with FEATUREID and no TYPENAME
        if ( request instanceof AugmentableGetFeature ) {
            ( (AugmentableGetFeature) request ).augment( (WFSConfiguration) service.getCapabilities() );
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                XMLFragment xml = XMLFactory.export( request );
                LOG.logDebug( xml.getAsPrettyString() );
            } catch ( Exception e ) {
                // nothing to do
            }
        }

        WFSConfiguration config = (WFSConfiguration) service.getCapabilities();
        FormatType formatType = determineFormat( request, config, service );

        // perform pre-processing if necessary (XSLT)
        if ( formatType.isVirtual() ) {
            request = transformGetFeature( request, formatType );
        }

        request.guessAllMissingNamespaces( service );

        // perform request on WFService
        FeatureResult result = (FeatureResult) service.doService( request );
        FeatureCollection fc = (FeatureCollection) result.getResponse();

        String format = formatType.getValue();

        if ( GetFeature.FORMAT_FEATURECOLLECTION.equals( format ) ) {
            sendBinaryResponse( fc, httpResponse );
        } else if ( AbstractWFSRequest.FORMAT_GML2_WFS100.equals( format )
                    || AbstractWFSRequest.FORMAT_XML.equals( format ) || format.startsWith( "text/xml; subtype=" ) ) {
            String schemaURL = buildSchemaURL( service, request );
            boolean suppressXLink = suppressXLinkOutput( fc );
            int depth = request.getTraverseXLinkDepth();
            if ( formatType.getOutFilter() != null ) {
                sendTransformedResponse( fc, httpResponse, schemaURL, suppressXLink, formatType,
                                         config.getDeegreeParams().printGeometryGmlIds(), depth, request );
            } else {
                sendGMLResponse( fc, httpResponse, schemaURL, suppressXLink,
                                 config.getDeegreeParams().printGeometryGmlIds(), depth, request );
            }
        } else {
            String msg = Messages.getMessage( "WFS_QUERY_UNSUPPORTED_FORMAT2", format );
            throw new OGCWebServiceException( msg );
        }
    }

    private void performGetGmlObject( WFService service, GetGmlObject request, HttpServletResponse httpResponse )
                            throws OGCWebServiceException, IOException, FeatureException, GeometryException {
        GmlResult result = (GmlResult) service.doService( request );
        OGCWebServiceException exception = result.getException();
        if ( exception != null ) {
            throw exception;
        }
        ServletOutputStream out = httpResponse.getOutputStream();
        result.writeResult( out );
        out.close();
    }

    /**
     * Performs a {@link LockFeature} request and sends the response to the given {@link HttpServletResponse} object.
     * 
     * @param service
     *            WFService instance to be used
     * @param request
     *            LockFeature request to be performed
     * @param httpResponse
     *            servlet response object to write to
     * @throws OGCWebServiceException
     */
    private void performLockFeature( WFService service, LockFeature request, HttpServletResponse httpResponse )
                            throws OGCWebServiceException {

        LockFeatureResponse response = (LockFeatureResponse) service.doService( request );
        XMLFragment responseDoc;
        try {
            if ( request.getVersion().equals( "1.0.0" ) ) {
                responseDoc = XMLFactory_1_0_0.export( response );
            } else {
                responseDoc = XMLFactory.export( response );
            }
        } catch ( Exception e ) {
            LOG.logError( "Unknown error", e );
            throw new OGCWebServiceException( this.getClass().getName(), e.getMessage() );
        }

        httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
        try {
            responseDoc.write( httpResponse.getOutputStream() );
        } catch ( IOException e ) {
            LOG.logError( "Error sending LockFeature response to client.", e );
        }
    }

    /**
     * Builds a KVP-encoded DescribeFeatureType-request that can be used to fetch the schemas for all feature types are
     * that queried in the given {@link GetFeature} request.
     * 
     * @param service
     * @param request
     * @return KVP-encoded DescribeFeatureType-request
     */
    private String buildSchemaURL( WFService service, GetFeature request ) {

        String schemaURL = null;

        WFSCapabilities capa = service.getCapabilities();
        WFSOperationsMetadata opMetadata = (WFSOperationsMetadata) capa.getOperationsMetadata();
        Operation describeFTOperation = opMetadata.getDescribeFeatureType();
        DCPType[] dcpTypes = describeFTOperation.getDCPs();
        if ( dcpTypes.length > 0 && dcpTypes[0].getProtocol() instanceof HTTP ) {
            HTTP http = (HTTP) dcpTypes[0].getProtocol();
            if ( http.getGetOnlineResources().length > 0 ) {
                URL baseURL = http.getGetOnlineResources()[0];
                String requestPart = buildDescribeFTRequest( request );
                schemaURL = baseURL.toString() + requestPart;
            }
        }
        return schemaURL;
    }

    /**
     * Builds the parameter part for a KVP-encoded DescribeFeatureType-request that fetches the necessary schemas for
     * all feature types that are queried in the given {@link GetFeature} request.
     * 
     * @param request
     * @return the URL-encoded parameter part of a KVP-DescribeFeatureType request
     */
    private String buildDescribeFTRequest( GetFeature request ) {

        Set<QualifiedName> ftNames = new HashSet<QualifiedName>();
        Map<String, URI> nsBindings = new HashMap<String, URI>();

        // get all requested feature types
        Query[] queries = request.getQuery();
        for ( Query query : queries ) {
            QualifiedName[] typeNames = query.getTypeNames();
            for ( QualifiedName name : typeNames ) {
                ftNames.add( name );
            }
        }
        Iterator<QualifiedName> ftNameIter = ftNames.iterator();
        QualifiedName qn = ftNameIter.next();
        StringBuffer typeNameSb = new StringBuffer( qn.getPrefix() );
        typeNameSb.append( ':' ).append( qn.getLocalName() );
        while ( ftNameIter.hasNext() ) {
            typeNameSb.append( ',' );
            qn = ftNameIter.next();
            typeNameSb.append( qn.getPrefix() );
            typeNameSb.append( ':' ).append( qn.getLocalName() );
        }

        // get all used namespace bindings
        for ( QualifiedName ftName : ftNames ) {
            LOG.logDebug( "for featuretype: " + ftName.getLocalName() + " found namespace binding: "
                          + ftName.getNamespace() );
            nsBindings.put( ftName.getPrefix(), ftName.getNamespace() );
        }
        StringBuffer nsParamSb = new StringBuffer( "xmlns(" );
        Iterator<String> prefixIter = nsBindings.keySet().iterator();
        String prefix = prefixIter.next();
        nsParamSb.append( prefix );
        nsParamSb.append( '=' );
        nsParamSb.append( nsBindings.get( prefix ) );
        while ( prefixIter.hasNext() ) {
            nsParamSb.append( ',' );
            prefix = prefixIter.next();
            nsParamSb.append( prefix );
            nsParamSb.append( '=' );
            nsParamSb.append( nsBindings.get( prefix ) );
        }
        nsParamSb.append( ')' );

        // build KVP-DescribeFeatureType-request
        StringBuffer sb = new StringBuffer( "SERVICE=WFS" );
        sb.append( "&VERSION=" + request.getVersion() );
        // sb.append( "&VERSION=1.1.0" );
        sb.append( "&REQUEST=DescribeFeatureType" );

        // append TYPENAME parameter
        sb.append( "&TYPENAME=" );
        sb.append( typeNameSb );

        // append NAMESPACE parameter
        sb.append( "&NAMESPACE=" );
        sb.append( nsParamSb.toString() );

        return sb.toString();
    }

    /**
     * Transforms a {@link GetFeature} request depending on the requested virtual format.
     * 
     * @param request
     *            GetFeature request to be transformed
     * @param format
     *            requested (virtual) output format
     * @return transformed GetFeature requested
     * @throws OGCWebServiceException
     *             if transformation script could not be loaded or transformation failed
     */
    private GetFeature transformGetFeature( GetFeature request, FormatType format )
                            throws OGCWebServiceException {

        if ( request instanceof GetFeatureWithLock ) {
            LOG.logDebug( "Not transforming GetFeatureWithLock request, it's not supported yet." );
            return request;
        }

        LOG.logDebug( "Transforming GetFeature request." );
        long start = System.currentTimeMillis();

        URL inFilterURL = null;
        try {
            inFilterURL = format.getInFilter().toURL();
        } catch ( MalformedURLException e1 ) {
            // never happens
        }
        XSLTDocument xsl = xsltCache.get( inFilterURL );
        if ( xsl == null ) {
            xsl = new XSLTDocument();
            try {
                xsl.load( inFilterURL );
                xsltCache.put( inFilterURL, xsl );
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "WFS_PREPROCESS_XSL_FILE_ERROR", format.getValue(),
                                                  format.getInFilter().toString(), e );
                LOG.logError( msg, e );
                throw new OGCWebServiceException( getClass().getName(), msg );
            }
        }

        XMLFragment xml = null;
        try {
            xml = XMLFactory.export( request );
            // nasty workaround to allow the Java methods called by XSL to access the namespace bindings
            String nsp = getAllNamespaceDeclarations( xml.getRootElement().getOwnerDocument() );
            Map<String, String> params = new HashMap<String, String>();
            params.put( "NSP", nsp );
            LOG.logDebug( "Namespace string given to XSL: " + nsp );
            xml = xsl.transform( xml, format.getInFilter().toASCIIString(), null, params );
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "WFS_PREPROCESS_XSL_ERROR", format.getValue(), e );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( getClass().getName(), msg );
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "Successfully transformed GetFeature request in " + ( System.currentTimeMillis() - start )
                          + " milliseconds." );
            try {
                LOG.logDebugXMLFile( "WFSHandler_GetFeature_transformed", xml );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
        return GetFeature.create( request.getId(), xml.getRootElement() );
    }

    /**
     * Sends the given {@link FeatureCollection} as GML to the given {@link HttpServletResponse} object.
     * 
     * @param fc
     *            feature collection to send
     * @param httpResponse
     *            servlet response object to write to
     * @param schemaURL
     *            URL to schema document (DescribeFeatureType request)
     * @param suppressXLinks
     *            true, if no XLinks must be used in the output, false otherwise
     * @param depth
     *            the depth of xlinks to resolve
     */
    private void sendGMLResponse( FeatureCollection fc, HttpServletResponse httpResponse, String schemaURL,
                                  boolean suppressXLinks, boolean sendGeometryIds, int depth, GetFeature request ) {

        try {
            httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
            OutputStream os = httpResponse.getOutputStream();
            GMLFeatureAdapter featureAdapter = new GMLFeatureAdapter( suppressXLinks, schemaURL, sendGeometryIds, depth );
            List<PropertyPath[]> names = new LinkedList<PropertyPath[]>();
            for ( Query q : request.getQuery() ) {
                names.add( q.getPropertyNames() );
            }
            featureAdapter.setPropertyPaths( names );
            if ( getGmlObjectUrl != null ) {
                featureAdapter.setBaseURL( getGmlObjectUrl.toExternalForm() );
            }
            featureAdapter.export( fc, os, getSystemCharset() );
        } catch ( Exception e ) {
            LOG.logError( "Error sending GetFeature response (GML) to client.", e );
        }
    }

    /**
     * Sends the given {@link FeatureCollection} as a serialized Java object to the given {@link HttpServletResponse}
     * object.
     * 
     * @param fc
     *            feature collection to send
     * @param httpResponse
     *            servlet response object to write to
     */
    private void sendBinaryResponse( FeatureCollection fc, HttpServletResponse httpResponse ) {
        try {
            OutputStream os = httpResponse.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream( os );
            oos.writeObject( fc );
            oos.flush();
        } catch ( IOException e ) {
            LOG.logError( "Error sending GetFeature response (binary) to client.", e );
        }
    }

    /**
     * Transforms a {@link FeatureCollection} to the given format using XSLT and sends it to the specified
     * {@link HttpServletResponse} object.
     * 
     * @param fc
     *            feature collection to send
     * @param schemaURL
     *            URL to schema document (DescribeFeatureType request)
     * @param httpResponse
     *            servlet response object to write to
     * @param suppressXLinks
     *            true, if no XLinks must be used in the output, false otherwise
     * @param format
     *            requested format
     * @param sendGeometryIds
     *            whether to send geometry gml ids
     * @param depth
     *            the xlink depth to resolve
     */
    private void sendTransformedResponse( FeatureCollection fc, HttpServletResponse httpResponse, String schemaURL,
                                          boolean suppressXLinks, FormatType format, boolean sendGeometryIds,
                                          int depth, GetFeature request )
                            throws OGCWebServiceException {

        GMLFeatureCollectionDocument fcgml = null;
        try {
            // export result feature collection as GML to enable transformation
            // into another (XML) format
            GMLFeatureAdapter featureAdapter = null;
            if ( "GML2".equals( format.getValue() ) ) {
                String wfsSchemaBinding = "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd";
                featureAdapter = new GMLFeatureAdapter( suppressXLinks, schemaURL, wfsSchemaBinding, sendGeometryIds,
                                                        depth );
            } else {
                featureAdapter = new GMLFeatureAdapter( suppressXLinks, schemaURL, sendGeometryIds, depth );
            }
            List<PropertyPath[]> names = new LinkedList<PropertyPath[]>();
            for ( Query q : request.getQuery() ) {
                names.add( q.getPropertyNames() );
            }
            featureAdapter.setPropertyPaths( names );
            fcgml = featureAdapter.export( fc );
        } catch ( Exception e ) {
            String msg = "Could not export feature collection to GML: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        LOG.logDebug( "Transforming GetFeature response." );
        long start = System.currentTimeMillis();

        // TODO: cache Transformer
        XSLTDocument xsl = null;
        try {
            xsl = new XSLTDocument( format.getOutFilter().toURL() );
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "WFS_POSTPROCESS_XSL_FILE_ERROR", format.getValue(),
                                              format.getOutFilter().toString(), e );
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }

        try {
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebugFile( "WFSHandler_GetFeature_result", ".xml", fcgml.getAsString() );
            }

            String type = format.getValue().split( ";" )[0];
            httpResponse.setContentType( type + "; charset=" + CharsetUtils.getSystemCharset() );

            OutputStream os = httpResponse.getOutputStream();
            xsl.transform( fcgml, os );
            os.close();
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "WFS_POSTPROCESS_XSL_ERROR", format.getValue(), e );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( getClass().getName(), msg );
        }

        LOG.logDebug( "Successfully transformed GetFeature response in " + ( System.currentTimeMillis() - start )
                      + " milliseconds." );
    }

    /**
     * Performs a {@link Transaction} request and sends the response to the given {@link HttpServletResponse} object.
     * 
     * @param service
     *            WFService instance to be used
     * @param request
     *            Transaction request to be performed
     * @param httpResponse
     *            servlet response object to write to
     * @throws OGCWebServiceException
     */
    private void performTransaction( WFService service, Transaction request, HttpServletResponse httpResponse )
                            throws OGCWebServiceException {

        WFSConfiguration config = (WFSConfiguration) service.getCapabilities();
        FormatType format = determineFormat( request, config );

        // perform pre-processing if necessary (XSLT)
        if ( format.isVirtual() ) {
            request = transformTransaction( request, format );
        }

        TransactionResponse response = (TransactionResponse) service.doService( request );

        try {
            httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
            XMLFragment document;
            if ( request.getVersion().equals( "1.0.0" ) ) {
                document = XMLFactory_1_0_0.export( response );
            } else {
                document = XMLFactory.export( response );
            }
            document.write( httpResponse.getOutputStream() );
        } catch ( IOException e ) {
            LOG.logError( "Error sending Transaction response to client.", e );
        }
    }

    /**
     * Transforms a {@link Transaction} request depending on the requested virtual format.
     * 
     * @param request
     *            Transaction request to be transformed
     * @param format
     *            requested (virtual) output format
     * @return transformed Transaction
     * @throws OGCWebServiceException
     *             if transformation script could not be loaded or transformation failed
     */
    private Transaction transformTransaction( Transaction request, FormatType format )
                            throws OGCWebServiceException {

        LOG.logDebug( "Transforming Transaction request." );
        long start = System.currentTimeMillis();

        URL inFilterURL = null;
        try {
            inFilterURL = format.getInFilter().toURL();
        } catch ( MalformedURLException e1 ) {
            // never happens
        }
        XSLTDocument xsl = xsltCache.get( inFilterURL );
        if ( xsl == null ) {
            xsl = new XSLTDocument();
            try {
                LOG.logDebug( "Read Filter ... " );
                xsl.load( inFilterURL );
                xsltCache.put( inFilterURL, xsl );
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "WFS_PREPROCESS_XSL_FILE_ERROR", format.getValue(),
                                                  format.getInFilter().toString(), e );
                LOG.logError( msg, e );
                throw new OGCWebServiceException( getClass().getName(), msg );
            }
        }

        XMLFragment xml = null;
        try {
            xml = request.getSourceDocument();
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                try {
                    LOG.logDebugXMLFile( "WFSHandler_Transaction_incoming", xml );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
        }
        // transform Transaction request
        try {
            LOG.logDebug( "start transform ..." );
            xml = xsl.transform( xml, format.getInFilter().toASCIIString(), null, null );
            LOG.logDebug( "end transform ..." );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                try {
                    LOG.logDebugXMLFile( "WFSHandler_Transaction_transformed", xml );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                }
            }
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "WFS_PREPROCESS_XSL_ERROR", format.getInFilter().toString(), e );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( getClass().getName(), msg );
        }

        try {
            request = Transaction.create( request.getId(), xml.getRootElement() );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
        }

        LOG.logDebug( "Successfully transformed Transaction request in " + ( System.currentTimeMillis() - start )
                      + " milliseconds." );

        return request;
    }

    /**
     * Determines whether the response to the given {@link GetFeature} request may use XLinks or not.
     * <p>
     * The first feature of the collection is checked; if it's {@link MappedGMLSchema} requests the suppression of
     * XLinks, xlinks are disabled, otherwise they are enabled.
     * 
     * @param fc
     * @return true, if the response document must not contain XLinks, false otherwise
     */
    private boolean suppressXLinkOutput( FeatureCollection fc ) {

        boolean suppressXLinkOutput = false;

        if ( fc instanceof FeatureTupleCollection ) {
            suppressXLinkOutput = true;
        } else if ( fc.size() > 0 ) {
            if ( fc.getFeature( 0 ).getFeatureType() instanceof MappedFeatureType ) {
                suppressXLinkOutput = ( (MappedFeatureType) fc.getFeature( 0 ).getFeatureType() ).getGMLSchema().suppressXLinkOutput();
            }
        }
        return suppressXLinkOutput;
    }

    private FormatType determineFormat( GetFeature request, WFSConfiguration config, WFService service )
                            throws OGCWebServiceException {

        Query firstQuery = request.getQuery()[0];
        QualifiedName ftName = firstQuery.getTypeNames()[0];
        WFSFeatureType wfsFT = config.getFeatureTypeList().getFeatureType( ftName );
        if ( wfsFT == null ) {
            MappedFeatureType ft = service.getMappedFeatureType( ftName );
            String msg = null;
            if ( ft == null ) {
                msg = Messages.getMessage( "WFS_FEATURE_TYPE_UNKNOWN", ftName );
            } else {
                assert !ft.isVisible();
                msg = Messages.getMessage( "WFS_FEATURE_TYPE_INVISIBLE", ftName );
            }
            throw new OGCWebServiceException( getClass().getName(), msg );
        }
        String requestedFormat = request.getOutputFormat();
        FormatType format = wfsFT.getOutputFormat( requestedFormat );
        if ( format == null ) {
            String msg = Messages.getMessage( "WFS_QUERY_UNSUPPORTED_FORMAT", requestedFormat, ftName );
            throw new OGCWebServiceException( getClass().getName(), msg );
        }
        return format;
    }

    private FormatType determineFormat( DescribeFeatureType request, WFSConfiguration config, WFService service )
                            throws OGCWebServiceException {

        // NOTE: this cannot cope with a mix of virtual and real features
        QualifiedName ftName = null;
        if ( request.getTypeNames().length > 0 ) {
            ftName = request.getTypeNames()[0];
        } else {
            // use the first ft that is available in the requested format
            for ( WFSFeatureType tp : config.getFeatureTypeList().getFeatureTypes() ) {
                if ( tp.getOutputFormat( request.getOutputFormat() ) != null ) {
                    ftName = tp.getName();
                    break;
                }
            }
        }
        LOG.logDebug( "typeName: " + ftName );

        WFSFeatureType wfsFT = config.getFeatureTypeList().getFeatureType( ftName );
        if ( wfsFT == null ) {
            MappedFeatureType ft = service.getMappedFeatureType( ftName );
            String msg = null;
            if ( ft == null ) {
                msg = Messages.getMessage( "WFS_FEATURE_TYPE_UNKNOWN", ftName );
            } else {
                assert !ft.isVisible();
                msg = Messages.getMessage( "WFS_FEATURE_TYPE_INVISIBLE", ftName );
            }
            throw new OGCWebServiceException( getClass().getName(), msg );
        }
        String requestedFormat = request.getOutputFormat();
        LOG.logDebug( "requested outputformat: " + requestedFormat );
        FormatType format = wfsFT.getOutputFormat( requestedFormat );
        if ( format == null ) {
            String msg = Messages.getMessage( "WFS_QUERY_UNSUPPORTED_FORMAT", requestedFormat, ftName );
            throw new OGCWebServiceException( getClass().getName(), msg );
        }
        return format;
    }

    private FormatType determineFormat( Transaction request, WFSConfiguration config )
                            throws OGCWebServiceException {

        FormatType format = null;

        WFSFeatureType wfsFT = config.getFeatureTypeList().getFeatureTypes()[0];

        List<TransactionOperation> list = request.getOperations();
        TransactionOperation op = list.get( 0 );
        if ( op instanceof Insert ) {
            QualifiedName qn = ( (Insert) op ).getAffectedFeatureTypes().get( 0 );
            wfsFT = config.getFeatureTypeList().getFeatureType( qn );
            if ( wfsFT == null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WFS_INSERT_UNSUPPORTED_FT", qn ) );
            }
        } else if ( op instanceof Update ) {
            QualifiedName qn = ( (Update) op ).getAffectedFeatureTypes().get( 0 );
            wfsFT = config.getFeatureTypeList().getFeatureType( qn );
            if ( wfsFT == null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WFS_UPDATE_UNSUPPORTED_FT", qn ) );
            }
        } else if ( op instanceof Delete ) {
            QualifiedName qn = ( (Delete) op ).getAffectedFeatureTypes().get( 0 );
            wfsFT = config.getFeatureTypeList().getFeatureType( qn );
            if ( wfsFT == null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WFS_DELETE_UNSUPPORTED_FT", qn ) );
            }
        }

        FormatType[] formats = wfsFT.getOutputFormats();
        for ( int i = 0; i < formats.length; i++ ) {
            format = formats[i];
            if ( format.getInFilter() != null ) {
                break;
            }
        }
        return format;
    }

    /**
     * @param httpResponse
     * @param serviceException
     * @param is100
     */
    public void sendVersionedException( HttpServletResponse httpResponse, OGCWebServiceException serviceException,
                                        boolean is100 ) {
        try {
            httpResponse.setContentType( "text/xml" );
            XMLFragment reportDocument = is100 ? exportExceptionReportWFS100( serviceException )
                                              : exportExceptionReportWFS( serviceException );
            OutputStream os = httpResponse.getOutputStream();
            reportDocument.write( os );
            os.close();
        } catch ( Exception e ) {
            LOG.logError( "Error sending exception report: ", e );
        }

    }

    @Override
    public void sendException( HttpServletResponse httpResponse, OGCWebServiceException serviceException ) {
        try {
            httpResponse.setContentType( "text/xml" );
            XMLFragment reportDocument = exportExceptionReportWFS( serviceException );
            OutputStream os = httpResponse.getOutputStream();
            reportDocument.write( os );
            os.close();
        } catch ( Exception e ) {
            LOG.logError( "Error sending exception report: ", e );
        }

    }

    private String getAllNamespaceDeclarations( Document doc ) {
        Map<String, String> nsp = new HashMap<String, String>();
        nsp = collect( nsp, doc );

        Iterator<String> iter = nsp.keySet().iterator();
        StringBuffer sb = new StringBuffer( 1000 );
        while ( iter.hasNext() ) {
            String s = iter.next();
            String val = nsp.get( s );
            sb.append( s ).append( ":" ).append( val );
            if ( iter.hasNext() ) {
                sb.append( ';' );
            }
        }
        return sb.toString();
    }

    private Map<String, String> collect( Map<String, String> nsp, Node node ) {
        NamedNodeMap nnm = node.getAttributes();
        if ( nnm != null ) {
            for ( int i = 0; i < nnm.getLength(); i++ ) {
                String s = nnm.item( i ).getNodeName();
                if ( s.startsWith( "xmlns:" ) ) {
                    nsp.put( s.substring( 6, s.length() ), nnm.item( i ).getNodeValue() );
                }
            }
        }
        NodeList nl = node.getChildNodes();
        if ( nl != null ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                collect( nsp, nl.item( i ) );
            }
        }
        return nsp;
    }

}
