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

package org.deegree.services.wms.controller;

import static java.util.Collections.singletonList;
import static javax.imageio.ImageIO.write;
import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.services.controller.OGCFrontController.getHttpGetURL;
import static org.deegree.services.controller.exception.ControllerException.NO_APPLICABLE_CODE;
import static org.deegree.services.controller.ows.OWSException.INVALID_FORMAT;
import static org.deegree.services.controller.ows.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.services.controller.ows.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.services.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java_cup.runtime.Symbol;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.utils.templating.TemplatingLexer;
import org.deegree.feature.utils.templating.TemplatingParser;
import org.deegree.feature.utils.templating.lang.PropertyTemplateCall;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureWriter;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDEncoder;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.deegree.services.jaxb.wms.PublishedInformation;
import org.deegree.services.jaxb.wms.PublishedInformation.GetFeatureInfoFormat;
import org.deegree.services.jaxb.wms.PublishedInformation.ImageFormat;
import org.deegree.services.jaxb.wms.PublishedInformation.SupportedVersions;
import org.deegree.services.jaxb.wms.ServiceConfiguration;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.WMSException.InvalidDimensionValue;
import org.deegree.services.wms.WMSException.MissingDimensionValue;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetFeatureInfoSchema;
import org.deegree.services.wms.controller.ops.GetLegendGraphic;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.controller.plugins.FeatureInfoSerializer;
import org.deegree.services.wms.controller.plugins.ImageSerializer;
import org.deegree.services.wms.controller.security.DummyWMSSecurityManager;
import org.deegree.services.wms.controller.security.WMSSecurityManager;
import org.slf4j.Logger;

/**
 * <code>WMSController</code> handles the protocol and map service globally.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(trace = "logs stack traces", debug = "logs sent exception messages, security information", warn = "logs problems with custom serializer classes", error = "logs unknown errors, problems with GetFeatureInfo templates")
public class WMSController extends AbstractOGCServiceController {

    private static final Logger LOG = getLogger( WMSController.class );

    private final static String CONFIG_SCHEMA_FILE = "/META-INF/schemas/wms/0.5.0/wms_configuration.xsd";

    private static final ImplementationMetadata<WMSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WMSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_111, VERSION_130 };
            handledNamespaces = new String[] { "" }; // WMS uses null namespace for SLD GetMap Post requests
            handledRequests = WMSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.5.0" ) };
        }
    };

    private final HashMap<String, FeatureInfoSerializer> featureInfoSerializers = new HashMap<String, FeatureInfoSerializer>();

    private final HashMap<String, ImageSerializer> imageSerializers = new HashMap<String, ImageSerializer>();

    /** The list of supported image formats. */
    public final LinkedList<String> supportedImageFormats = new LinkedList<String>();

    /** The list of supported info formats. */
    public final LinkedHashMap<String, String> supportedFeatureInfoFormats = new LinkedHashMap<String, String>();

    protected MapService service;

    private WMSSecurityManager securityManager;

    protected ServiceIdentificationType identification;

    protected ServiceProviderType provider;

    protected TreeMap<Version, WMSControllerBase> controllers = new TreeMap<Version, WMSControllerBase>();

    private Version highestVersion;

    private static <T> void instantiateSerializer( HashMap<String, T> map, String format, String className,
                                                   Class<T> clazz ) {
        try {
            // generics and reflection don't go well together
            @SuppressWarnings(value = "unchecked")
            Class<T> c = (Class<T>) Class.forName( className );
            if ( !c.isAssignableFrom( clazz ) ) {
                LOG.warn( "The serializer class '{}' does not implement the '{}' interface.", className, clazz );
            } else {
                map.put( format, c.newInstance() );
            }
        } catch ( ClassNotFoundException e ) {
            LOG.warn( "The feature info serializer class '{}' could not be found " + "on the classpath.", className );
            LOG.trace( "Stack trace: ", e );
        } catch ( InstantiationException e ) {
            LOG.warn( "The feature info serializer class '{}' could not be instantiated.", className );
            LOG.trace( "Stack trace: ", e );
        } catch ( IllegalAccessException e ) {
            LOG.warn( "The feature info serializer class '{}' could not be instantiated.", className );
            LOG.trace( "Stack trace: ", e );
        }
    }

    /**
     * @return the underlying map service
     */
    public MapService getMapService() {
        return service;
    }

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadataType serviceMetadata,
                      DeegreeServiceControllerType mainConfig )
                            throws ControllerInitException {

        init( serviceMetadata, mainConfig, IMPLEMENTATION_METADATA, controllerConf );

        identification = mainMetadataConf.getServiceIdentification();
        provider = mainMetadataConf.getServiceProvider();

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wms", "http://www.deegree.org/services/wms" );

        try {
            String additionalClasspath = "org.deegree.services.jaxb.wms";
            Unmarshaller u = getUnmarshaller( additionalClasspath, CONFIG_SCHEMA_FILE );

            XPath xp = new XPath( "wms:PublishedInformation", nsContext );
            OMElement elem = controllerConf.getElement( controllerConf.getRootElement(), xp );

            // put in the default formats
            supportedFeatureInfoFormats.put( "application/vnd.ogc.gml", "" );
            supportedFeatureInfoFormats.put( "text/xml", "" );
            supportedFeatureInfoFormats.put( "text/plain", "" );
            supportedFeatureInfoFormats.put( "text/html", "" );

            supportedImageFormats.add( "image/png" );
            supportedImageFormats.add( "image/png; subtype=8bit" );
            supportedImageFormats.add( "image/png; mode=8bit" );
            supportedImageFormats.add( "image/gif" );
            supportedImageFormats.add( "image/jpeg" );
            supportedImageFormats.add( "image/tiff" );
            supportedImageFormats.add( "image/x-ms-bmp" );

            if ( elem != null ) {
                PublishedInformation pi = (PublishedInformation) u.unmarshal( elem.getXMLStreamReaderWithoutCaching() );

                if ( pi.getGetFeatureInfoFormat() != null ) {
                    for ( GetFeatureInfoFormat t : pi.getGetFeatureInfoFormat() ) {
                        String format = t.getFormat();
                        if ( t.getFile() != null ) {
                            supportedFeatureInfoFormats.put( format,
                                                             new File( controllerConf.resolve( t.getFile() ).toURI() ).toString() );
                        } else {
                            instantiateSerializer( featureInfoSerializers, format, t.getClazz(),
                                                   FeatureInfoSerializer.class );
                        }
                    }
                }

                if ( pi.getImageFormat() != null ) {
                    for ( ImageFormat f : pi.getImageFormat() ) {
                        instantiateSerializer( imageSerializers, f.getFormat(), f.getClazz(), ImageSerializer.class );
                    }
                }

                // TODO assign overwritten metadata here
                // identification = pi.getServiceIdentification() == null ? identification :
                // pi.getServiceIdentification();
                // provider = pi.getServiceProvider() == null ? provider : pi.getServiceProvider();
                final SupportedVersions versions = pi.getSupportedVersions();
                if ( versions == null ) {
                    ArrayList<String> vs = new ArrayList<String>();
                    vs.add( "1.1.1" );
                    vs.add( "1.3.0" );
                    validateAndSetOfferedVersions( vs );
                } else {
                    validateAndSetOfferedVersions( versions.getVersion() );
                }
            } else {
                ArrayList<String> vs = new ArrayList<String>();
                vs.add( "1.1.1" );
                vs.add( "1.3.0" );
                validateAndSetOfferedVersions( vs );
            }

            for ( Version v : offeredVersions ) {
                if ( v.equals( VERSION_111 ) ) {
                    controllers.put( VERSION_111, new WMSController111() );
                }
                if ( v.equals( VERSION_130 ) ) {
                    controllers.put( VERSION_130, new WMSController130() );
                }
            }

            Iterator<Version> iter = controllers.keySet().iterator();
            while ( iter.hasNext() ) {
                highestVersion = iter.next();
            }

            xp = new XPath( "wms:ServiceConfiguration", nsContext );
            elem = controllerConf.getRequiredElement( controllerConf.getRootElement(), xp );
            ServiceConfiguration sc = (ServiceConfiguration) u.unmarshal( elem.getXMLStreamReaderWithoutCaching() );
            service = new MapService( sc, controllerConf );

            if ( sc.getSecurityManager() == null ) {
                // then do nothing and step over
            } else {
                securityManager = sc.getSecurityManager().getDummySecurityManager() != null ? new DummyWMSSecurityManager()
                                                                                           : null;
            }

            String securityLogging = securityManager != null ? "A securityManager is specified: " + securityManager
                                                            : "There is no securityManager specified. Now, there should be no credentials needed and every operation can be requested anonymous.";
            LOG.debug( securityLogging );

        } catch ( JAXBException e ) {
            // whyever they use the linked exception here...
            // http://www.jaxb.com/how/to/hide/important/information/from/the/user/of/the/api/unknown_xml_format.xml
            throw new ControllerInitException( e.getLinkedException().getMessage(), e );
        } catch ( MalformedURLException e ) {
            throw new ControllerInitException( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            throw new ControllerInitException( e.getMessage(), e );
        }

    }

    @Override
    public void destroy() {
        service.close();
    }

    @Override
    public void doKVP( Map<String, String> map, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException, SecurityException {
        String v = map.get( "VERSION" );
        if ( v == null ) {
            v = map.get( "WMTVER" );
        }
        Version version = v == null ? highestVersion : parseVersion( v );

        WMSRequestType req;
        try {
            req = IMPLEMENTATION_METADATA.getRequestTypeByName( map.get( "REQUEST" ) );
        } catch ( IllegalArgumentException e ) {
            controllers.get( version ).sendException( new OWSException( get( "WMS.OPERATION_NOT_KNOWN",
                                                                             map.get( "REQUEST" ) ),
                                                                        OPERATION_NOT_SUPPORTED ), response );
            return;
        } catch ( NullPointerException e ) {
            controllers.get( version ).sendException( new OWSException( get( "WMS.PARAM_MISSING", "REQUEST" ),
                                                                        OPERATION_NOT_SUPPORTED ), response );
            return;
        }

        try {
            handleRequest( req, response, map, version );
        } catch ( OWSException e ) {
            if ( controllers.get( version ) == null ) {
                // happens if non capabilities request is made with unsupported version
                version = highestVersion;
            }

            LOG.debug( "The response is an exception with the message '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace of OWSException being sent", e );

            controllers.get( version ).handleException( map, req, e, response, this );
        }
    }

    private void handleRequest( WMSRequestType req, HttpResponseBuffer response, Map<String, String> map,
                                Version version )
                            throws IOException, OWSException {
        try {
            switch ( req ) {
            case GetCapabilities:
            case capabilities:
                break;
            default:
                if ( controllers.get( version ) == null ) {
                    throw new OWSException( get( "WMS.VERSION_UNSUPPORTED", version ), INVALID_PARAMETER_VALUE );
                }
            }

            switch ( req ) {
            case DescribeLayer:
                throw new OWSException( get( "WMS.OPERATION_NOT_SUPPORTED_IMPLEMENTATION", req.name() ),
                                        OPERATION_NOT_SUPPORTED );
            case capabilities:
            case GetCapabilities:
                getCapabilities( map, response );
                break;
            case GetFeatureInfo:
                getFeatureInfo( map, response, version );
                break;
            case GetMap:
            case map:
                getMap( map, response, version );
                break;
            case GetFeatureInfoSchema:
                getFeatureInfoSchema( map, response );
                break;
            case GetLegendGraphic:
                getLegendGraphic( map, response );
                break;
            case DTD:
                getDtd( response );
                break;
            }
        } catch ( MissingDimensionValue e ) {
            throw new OWSException( get( "WMS.DIMENSION_VALUE_MISSING", e.name ), "MissingDimensionValue" );
        } catch ( InvalidDimensionValue e ) {
            throw new OWSException( get( "WMS.DIMENSION_VALUE_INVALID", e.value, e.name ), "InvalidDimensionValue" );
        }
    }

    /**
     * @param response
     */
    private void getDtd( HttpResponseBuffer response ) {
        InputStream in = WMSController.class.getResourceAsStream( "WMS_MS_Capabilities.dtd" );
        try {
            OutputStream out = response.getOutputStream();
            byte[] buf = new byte[65536];
            int read;
            while ( ( read = in.read( buf ) ) != -1 ) {
                out.write( buf, 0, read );
            }
        } catch ( IOException e ) {
            LOG.trace( "Could not read/write the internal DTD:", e );
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch ( IOException e ) {
                LOG.trace( "Error while closing DTD input stream:", e );
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void getLegendGraphic( Map<String, String> map, HttpResponseBuffer response )
                            throws OWSException, IOException {
        GetLegendGraphic glg = securityManager == null ? new GetLegendGraphic( map, service )
                                                      : securityManager.preprocess( new GetLegendGraphic( map, service ),
                                                                                    OGCFrontController.getContext().getCredentials() );

        if ( !supportedImageFormats.contains( glg.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", glg.getFormat() ), INVALID_FORMAT );
        }
        BufferedImage img = service.getLegend( glg );
        sendImage( img, response, glg.getFormat() );
    }

    private void runTemplate( HttpResponseBuffer response, String fiFile, GenericFeatureCollection col,
                              GetFeatureInfo fi )
                            throws UnsupportedEncodingException, IOException {
        PrintWriter out = new PrintWriter( new OutputStreamWriter( response.getOutputStream(), "UTF-8" ) );

        try {
            InputStream in;
            if ( fiFile == null ) {
                in = WMSController.class.getResourceAsStream( "html.gfi" );
            } else {
                in = new FileInputStream( fiFile );
            }

            Symbol s = new TemplatingParser( new TemplatingLexer( in ) ).parse();
            @SuppressWarnings(value = "unchecked")
            HashMap<String, Object> tmpl = (HashMap) s.value;
            StringBuilder sb = new StringBuilder();
            new PropertyTemplateCall( "start", singletonList( "*" ), false ).eval( sb, tmpl, col, fi.returnGeometries() );
            out.println( sb.toString() );
        } catch ( Exception e ) {
            if ( fiFile == null ) {
                LOG.error( "Could not load internal template for GFI response." );
            } else {
                LOG.error( "Could not load template '{}' for GFI response.", fiFile );
            }
            LOG.trace( "Stack trace:", e );
        } finally {
            out.close();
        }
        response.flushBuffer();
        return;
    }

    private void getFeatureInfo( Map<String, String> map, HttpResponseBuffer response, Version version )
                            throws OWSException, IOException, MissingDimensionValue, InvalidDimensionValue {
        GetFeatureInfo fi = securityManager == null ? new GetFeatureInfo( map, version, service )
                                                   : securityManager.preprocess( new GetFeatureInfo( map, version,
                                                                                                     service ),
                                                                                 OGCFrontController.getContext().getCredentials() );
        checkGetFeatureInfo( fi );
        Pair<GenericFeatureCollection, LinkedList<String>> pair = service.getFeatures( fi );
        GenericFeatureCollection col = pair.first;
        addHeaders( response, pair.second );
        String format = fi.getInfoFormat();
        format = format == null ? "application/vnd.ogc.gml" : format;
        response.setContentType( format );
        response.setCharacterEncoding( "UTF-8" );

        FeatureInfoSerializer serializer = featureInfoSerializers.get( format );
        if ( serializer != null ) {
            serializer.serialize( col, response.getOutputStream() );
            response.flushBuffer();
            return;
        }

        String fiFile = supportedFeatureInfoFormats.get( format );
        if ( !fiFile.isEmpty() ) {
            runTemplate( response, fiFile, col, fi );
            return;
        }

        if ( format.equalsIgnoreCase( "application/vnd.ogc.gml" ) || format.equalsIgnoreCase( "text/xml" ) ) {
            try {
                XMLStreamWriter xmlWriter = response.getXMLWriter();
                // quick hack to get better prefixes
                HashSet<String> set = new HashSet<String>();
                int cur = 0;
                for ( Feature f : col ) {
                    String ns = f.getType().getName().getNamespaceURI();
                    if ( ns != null && ns.length() > 0 && !set.contains( ns ) ) {
                        set.add( ns );
                        xmlWriter.setPrefix( "app" + cur++, ns );
                    }
                }
                xmlWriter.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
                String loc = getHttpGetURL() + "request=GetFeatureInfoSchema&layers=" + join( ",", fi.getQueryLayers() );

                // for more than just quick 'hacky' schemaLocation attributes one should use a proper WFS
                HashMap<String, String> bindings = new HashMap<String, String>();
                FeatureType type = fi.getQueryLayers().get( 0 ).getFeatureType();
                String ns = type == null ? null : type.getName().getNamespaceURI();
                if ( ns != null && ns.isEmpty() ) {
                    ns = null;
                }
                if ( ns != null ) {
                    bindings.put( ns, loc );
                }
                bindings.put( "http://www.opengis.net/wfs", "http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd" );

                new GMLFeatureWriter( GMLVersion.GML_2, xmlWriter, fi.getCoordinateSystem(), null, null, null, 0, -1,
                                      null, false, fi.returnGeometries(), null, null ).export( col, ns == null ? loc
                                                                                                              : null,
                                                                                               bindings );
            } catch ( XMLStreamException e ) {
                LOG.warn( "Error when writing GetFeatureInfo GML response '{}'.", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            } catch ( UnknownCRSException e ) {
                LOG.warn( "Could not instantiate the geometry transformer for output srs '{}'."
                          + " Aborting GetFeatureInfo response.", fi.getCoordinateSystem() );
                LOG.trace( "Stack trace:", e );
            } catch ( TransformationException e ) {
                LOG.warn( "Could transform to output srs '{}'. Aborting GetFeatureInfo response.",
                          fi.getCoordinateSystem() );
                LOG.trace( "Stack trace:", e );
            }
        }
        if ( format.equalsIgnoreCase( "text/plain" ) ) {
            PrintWriter out = new PrintWriter( new OutputStreamWriter( response.getOutputStream(), "UTF-8" ) );
            for ( Feature f : col ) {
                out.println( f.getName().getLocalPart() + ":" );
                for ( Property p : f.getProperties() ) {
                    out.println( "  " + p.getName().getLocalPart() + ": " + p.getValue() );
                }
                out.println();
            }
            out.close();
        }

        if ( format.equalsIgnoreCase( "text/html" ) ) {
            runTemplate( response, null, col, fi );
        }
    }

    private void getFeatureInfoSchema( Map<String, String> map, HttpResponseBuffer response )
                            throws IOException {
        GetFeatureInfoSchema fis = securityManager == null ? new GetFeatureInfoSchema( map )
                                                          : securityManager.preprocess( new GetFeatureInfoSchema( map ),
                                                                                        OGCFrontController.getContext().getCredentials() );
        List<FeatureType> schema = service.getSchema( fis );
        try {
            response.setContentType( "text/xml" );
            XMLStreamWriter writer = response.getXMLWriter();

            // TODO handle multiple namespaces
            String namespace = "http://www.deegree.org/app";
            if ( !schema.isEmpty() ) {
                namespace = schema.get( 0 ).getName().getNamespaceURI();
            }
            Map<String, String> nsToPrefix = new HashMap<String, String>();
            nsToPrefix.put( "app", namespace );
            new ApplicationSchemaXSDEncoder( GMLVersion.GML_2, namespace, null, nsToPrefix ).export( writer, schema );
            writer.writeEndDocument();
        } catch ( XMLStreamException e ) {
            LOG.error( "Unknown error", e );
        }
    }

    private static void addHeaders( HttpResponseBuffer response, LinkedList<String> headers ) {
        while ( !headers.isEmpty() ) {
            String s = headers.poll();
            response.addHeader( "Warning", s );
        }
    }

    protected void getMap( Map<String, String> map, HttpResponseBuffer response, Version version )
                            throws OWSException, IOException, MissingDimensionValue, InvalidDimensionValue {
        GetMap gm = new GetMap( map, version, service );
        if ( securityManager != null ) {
            gm = securityManager.preprocess( gm, OGCFrontController.getContext().getCredentials() );
        }
        checkGetMap( version, gm );
        final Pair<BufferedImage, LinkedList<String>> pair = service.getMapImage( gm );
        addHeaders( response, pair.second );
        sendImage( pair.first, response, gm.getFormat() );
    }

    private void checkGetFeatureInfo( GetFeatureInfo gfi )
                            throws OWSException {
        if ( gfi.getInfoFormat() != null && !gfi.getInfoFormat().equals( "" )
             && !supportedFeatureInfoFormats.containsKey( gfi.getInfoFormat() ) ) {
            throw new OWSException( get( "WMS.INVALID_INFO_FORMAT", gfi.getInfoFormat() ), INVALID_FORMAT );
        }
    }

    private void checkGetMap( Version version, GetMap gm )
                            throws OWSException {
        if ( !supportedImageFormats.contains( gm.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", gm.getFormat() ), INVALID_FORMAT );
        }
        try {
            // check for existence/validity
            if ( gm.getCoordinateSystem() == null ) {
                // this can happen if some AUTO SRS id was invalid
                controllers.get( version ).throwSRSException( "automatic" );
            }
            gm.getCoordinateSystem().getWrappedCRS();
        } catch ( UnknownCRSException e ) {
            // only throw an exception if a truly invalid srs is found
            // this makes it possible to request srs that are not advertised, which may be useful
            controllers.get( version ).throwSRSException( gm.getCoordinateSystem().getName() );
        }

    }

    protected void getCapabilities( Map<String, String> map, HttpResponseBuffer response )
                            throws OWSException, IOException {

        String version = map.get( "VERSION" );
        // not putting it into the bean, why should I? It's used just a few lines below...
        String updateSequence = map.get( "UPDATESEQUENCE" );
        if ( version == null ) {
            version = map.get( "WMTVER" );
        }
        GetCapabilities req = securityManager == null ? new GetCapabilities( version )
                                                     : securityManager.preprocess( new GetCapabilities( version ),
                                                                                   OGCFrontController.getContext().getCredentials() );

        Version myVersion = negotiateVersion( req );

        String getUrl = OGCFrontController.getHttpGetURL();
        String postUrl = OGCFrontController.getHttpPostURL();

        if ( service.getDynamics().isEmpty() ) {
            controllers.get( myVersion ).getCapabilities( getUrl, postUrl, updateSequence, service, response,
                                                          identification, provider, map, this );
        } else {
            // need to synchronize here as well, else the layer list may be updating right now (the service.update()
            // does not strictly need to be synchronized for this use case, but it sure is a Good Thing to do it anyway)
            synchronized ( this ) {
                service.update();
                controllers.get( myVersion ).getCapabilities( getUrl, postUrl, updateSequence, service, response,
                                                              identification, provider, map, this );
            }
        }

        response.flushBuffer(); // TODO remove this to enable validation, enable validation on a DTD basis...
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {
        throw new UnsupportedOperationException( "XML request handling is currently not supported for the wms" );
    }

    /**
     * @param img
     * @param response
     * @param format
     * @throws OWSException
     * @throws IOException
     */
    public void sendImage( BufferedImage img, HttpResponseBuffer response, String format )
                            throws OWSException, IOException {
        response.setContentType( format );

        ImageSerializer serializer = imageSerializers.get( format );
        if ( serializer != null ) {
            serializer.serialize( img, response.getOutputStream() );
            return;
        }

        format = format.substring( format.indexOf( "/" ) + 1 );
        if ( format.equals( "x-ms-bmp" ) ) {
            format = "bmp";
        }
        if ( format.equals( "png; subtype=8bit" ) || format.equals( "png; mode=8bit" ) ) {
            format = "png";
        }
        LOG.debug( "Sending in format " + format );
        if ( !write( img, format, response.getOutputStream() ) ) {
            throw new OWSException( get( "WMS.CANNOT_ENCODE_IMAGE", format ), NO_APPLICABLE_CODE );
        }
    }

    @Override
    public Pair<XMLExceptionSerializer<OWSException>, String> getExceptionSerializer( Version requestVersion ) {

        WMSControllerBase controller = requestVersion == null ? null : controllers.get( requestVersion );
        if ( controller == null ) {
            controller = controllers.values().iterator().next();
        }
        return new Pair<XMLExceptionSerializer<OWSException>, String>( controller.EXCEPTIONS, controller.EXCEPTION_MIME );
    }

    /**
     * <code>Controller</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public interface Controller {
        /**
         * @param map
         * @param req
         * @param e
         * @param response
         * @param controller
         * @throws ServletException
         */
        void handleException( Map<String, String> map, WMSRequestType req, OWSException e, HttpResponseBuffer response,
                              WMSController controller )
                                throws ServletException;

        /**
         * @param ex
         * @param response
         * @throws ServletException
         */
        void sendException( OWSException ex, HttpResponseBuffer response )
                                throws ServletException;

        /**
         * @param getUrl
         * @param postUrl
         * @param updateSequence
         * @param service
         * @param response
         * @param identification
         * @param provider
         * @param customParameters
         * @param controller
         * @throws OWSException
         * @throws IOException
         */
        void getCapabilities( String getUrl, String postUrl, String updateSequence, MapService service,
                              HttpResponseBuffer response, ServiceIdentificationType identification,
                              ServiceProviderType provider, Map<String, String> customParameters,
                              WMSController controller )
                                throws OWSException, IOException;

        /**
         * @param name
         * @throws OWSException
         */
        void throwSRSException( String name )
                                throws OWSException;
    }

}
