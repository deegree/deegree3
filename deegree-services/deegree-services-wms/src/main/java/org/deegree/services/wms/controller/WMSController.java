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
import static org.deegree.commons.utils.CollectionUtils.getStringJoiner;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.commons.xml.CommonNamespaces.getNamespaceContext;
import static org.deegree.protocol.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.services.controller.OGCFrontController.getHttpGetURL;
import static org.deegree.services.i18n.Messages.get;
import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;
import static org.deegree.services.wms.controller.WMSProvider.IMPLEMENTATION_METADATA;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.utils.templating.TemplatingLexer;
import org.deegree.feature.utils.templating.TemplatingParser;
import org.deegree.feature.utils.templating.java_cup.runtime.Symbol;
import org.deegree.feature.utils.templating.lang.PropertyTemplateCall;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaWriter;
import org.deegree.layer.LayerRef;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreManager;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.protocol.wms.ops.GetFeatureInfoSchema;
import org.deegree.protocol.wms.ops.GetLegendGraphic;
import org.deegree.rendering.r2d.context.DefaultRenderContext;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.services.OWS;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.WebServicesConfiguration;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.wms.DeegreeWMS;
import org.deegree.services.jaxb.wms.DeegreeWMS.ExtendedCapabilities;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat.XSLTFile;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.OWSMetadataProviderManager;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.controller.plugins.FeatureInfoSerializer;
import org.deegree.services.wms.controller.plugins.ImageSerializer;
import org.deegree.services.wms.controller.plugins.XSLTFeatureInfoSerializer;
import org.deegree.services.wms.controller.security.WMSSecurityManager;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.StyleRef;
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
public class WMSController extends AbstractOWS {

    private static final Logger LOG = getLogger( WMSController.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.services.jaxb.wms";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/wms/3.2.0/wms_configuration.xsd";

    /**
     * Default GML info formats. Will only be used if not overridden by config.
     */
    public final HashSet<String> defaultGMLGFIFormats = new LinkedHashSet<String>();

    private final HashMap<String, FeatureInfoSerializer> featureInfoSerializers = new HashMap<String, FeatureInfoSerializer>();

    private final HashMap<String, ImageSerializer> imageSerializers = new HashMap<String, ImageSerializer>();

    /** The list of supported image formats. */
    public final LinkedList<String> supportedImageFormats = new LinkedList<String>();

    /** The list of supported info formats. */
    public final LinkedHashMap<String, String> supportedFeatureInfoFormats = new LinkedHashMap<String, String>();

    protected MapService service;

    private WMSSecurityManager securityManager;

    protected ServiceIdentification identification;

    protected ServiceProvider provider;

    protected TreeMap<Version, WMSControllerBase> controllers = new TreeMap<Version, WMSControllerBase>();

    private Version highestVersion;

    private Map<String, List<OMElement>> extendedCaps;

    private String metadataURLTemplate;

    private String configId;

    public WMSController( URL configURL, ImplementationMetadata<?> serviceInfo ) {
        super( configURL, serviceInfo );
        try {
            File f = new File( configURL.toURI() );
            this.configId = f.getName().substring( 0, f.getName().length() - 4 );
        } catch ( URISyntaxException e ) {
            // then no configId will be available
        }
        defaultGMLGFIFormats.add( "application/gml+xml; version=2.1" );
        defaultGMLGFIFormats.add( "application/gml+xml; version=3.0" );
        defaultGMLGFIFormats.add( "application/gml+xml; version=3.1" );
        defaultGMLGFIFormats.add( "application/gml+xml; version=3.2" );
        defaultGMLGFIFormats.add( "text/xml; subtype=gml/2.1.2" );
        defaultGMLGFIFormats.add( "text/xml; subtype=gml/3.0.1" );
        defaultGMLGFIFormats.add( "text/xml; subtype=gml/3.1.1" );
        defaultGMLGFIFormats.add( "text/xml; subtype=gml/3.2.1" );
    }

    /**
     * @return the underlying map service
     */
    public MapService getMapService() {
        return service;
    }

    private void traverseMetadataIds( Layer l, HashMap<String, String> dataMetadataIds ) {
        if ( l.getName() != null && l.getDataMetadataSetId() != null ) {
            dataMetadataIds.put( l.getName(), l.getDataMetadataSetId() );
        }
        if ( l.getChildren() != null ) {
            for ( Layer c : l.getChildren() ) {
                traverseMetadataIds( c, dataMetadataIds );
            }
        }
    }

    private void handleMetadata( String metadataURLTemplate, String storeid ) {
        this.metadataURLTemplate = metadataURLTemplate;

        if ( service.isNewStyle() ) {
            return;
        }

        HashMap<String, String> dataMetadataIds = new HashMap<String, String>();
        traverseMetadataIds( service.getRootLayer(), dataMetadataIds );
        if ( storeid != null ) {
            MetadataStoreManager mdmanager = workspace.getSubsystemManager( MetadataStoreManager.class );
            MetadataStore<ISORecord> store = mdmanager.get( storeid );
            if ( store == null ) {
                LOG.warn( "Metadata store with id {} is not available, metadata ids will not be checked.", storeid );
                return;
            }
            if ( !store.getType().equals( "iso" ) ) {
                LOG.warn( "Metadata store with id {} is not an ISO metadata store, metadata ids will not be checked.",
                          storeid );
                return;
            }

            MetadataResultSet<ISORecord> rs = null;
            try {
                for ( Entry<String, String> e : dataMetadataIds.entrySet() ) {
                    rs = store.getRecordById( singletonList( e.getValue() ), null );
                    if ( !rs.next() ) {
                        LOG.warn( "Metadata store with id {} does not have a record with id {} (referenced from layer {}).",
                                  new Object[] { storeid, e.getValue(), e.getKey() } );
                        return;
                    }

                    ISORecord rec = rs.getRecord();
                    String prefix = "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/";
                    String code = rec.getStringFromXPath( new XPath( prefix + "gmd:code/gco:CharacterString",
                                                                     getNamespaceContext() ) );
                    String codeSpace = rec.getStringFromXPath( new XPath( prefix + "gmd:codeSpace/gco:CharacterString",
                                                                          getNamespaceContext() ) );

                    Layer l = service.getLayer( e.getKey() );
                    l.setAuthorityURL( codeSpace );
                    l.setAuthorityIdentifier( code );

                    rs.close();
                }
            } catch ( Throwable e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if ( rs != null ) {
                    try {
                        rs.close();
                    } catch ( MetadataStoreException e ) {
                        // ignore
                    }
                }
            }
        }
    }

    @Override
    public void init( DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConfig,
                      ImplementationMetadata<?> md, XMLAdapter controllerConf )
                            throws ResourceInitException {

        super.init( serviceMetadata, mainConfig, IMPLEMENTATION_METADATA, controllerConf );

        identification = convertFromJAXB( mainMetadataConf.getServiceIdentification() );
        provider = convertFromJAXB( mainMetadataConf.getServiceProvider() );

        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "wms", "http://www.deegree.org/services/wms" );

        DeegreeWMS conf = (DeegreeWMS) unmarshallConfig( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, controllerConf );

        if ( conf.getExtendedCapabilities() != null ) {
            this.extendedCaps = new HashMap<String, List<OMElement>>();
            List<OMElement> caps = new ArrayList<OMElement>( conf.getExtendedCapabilities().size() );
            extendedCaps.put( "default", caps );
            for ( ExtendedCapabilities extendedCapsConf : conf.getExtendedCapabilities() ) {
                DOMSource domSource = new DOMSource( extendedCapsConf.getAny() );
                XMLStreamReader xmlStream;
                try {
                    xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( domSource );
                } catch ( Throwable t ) {
                    throw new ResourceInitException( "Error extracting extended capabilities: " + t.getMessage(), t );
                }
                caps.add( new XMLAdapter( xmlStream ).getRootElement() );
            }
        }

        try {
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

            if ( conf.getFeatureInfoFormats() != null ) {
                for ( GetFeatureInfoFormat t : conf.getFeatureInfoFormats().getGetFeatureInfoFormat() ) {
                    String format = t.getFormat();
                    defaultGMLGFIFormats.remove( format );
                    if ( t.getFile() != null ) {
                        supportedFeatureInfoFormats.put( format,
                                                         new File( controllerConf.resolve( t.getFile() ).toURI() ).toString() );
                    } else {
                        XSLTFile xsltFile = t.getXSLTFile();
                        GMLVersion version = GMLVersion.valueOf( xsltFile.getGmlVersion().toString() );
                        XSLTFeatureInfoSerializer xslt = new XSLTFeatureInfoSerializer(
                                                                                        version,
                                                                                        controllerConf.resolve( xsltFile.getValue() ),
                                                                                        workspace );
                        featureInfoSerializers.put( format, xslt );
                    }
                }
            }

            for ( String f : defaultGMLGFIFormats ) {
                supportedFeatureInfoFormats.put( f, "" );
            }

            // if ( pi.getImageFormat() != null ) {
            // for ( ImageFormat f : pi.getImageFormat() ) {
            // instantiateSerializer( imageSerializers, f.getFormat(), f.getClazz(), ImageSerializer.class );
            // }
            // }

            // TODO assign overwritten metadata here
            // identification = pi.getServiceIdentification() == null ? identification :
            // pi.getServiceIdentification();
            // provider = pi.getServiceProvider() == null ? provider : pi.getServiceProvider();
            final org.deegree.services.jaxb.wms.DeegreeWMS.SupportedVersions versions = conf.getSupportedVersions();
            if ( versions == null ) {
                ArrayList<String> vs = new ArrayList<String>();
                vs.add( "1.1.1" );
                vs.add( "1.3.0" );
                validateAndSetOfferedVersions( vs );
            } else {
                validateAndSetOfferedVersions( versions.getVersion() );
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

            ServiceConfigurationType sc = conf.getServiceConfiguration();
            service = new MapService( sc, controllerConf, workspace );

            // after the service knows what layers are available:
            handleMetadata( conf.getMetadataURLTemplate(), conf.getMetadataStoreId() );

            // if ( sc.getSecurityManager() == null ) {
            // // then do nothing and step over
            // } else {
            // securityManager = sc.getSecurityManager().getDummySecurityManager() != null ? new
            // DummyWMSSecurityManager()
            // : null;
            // }

            String securityLogging = securityManager != null ? "A securityManager is specified: " + securityManager
                                                            : "There is no securityManager specified. Now, there should be no credentials needed and every operation can be requested anonymous.";
            LOG.debug( securityLogging );

        } catch ( MalformedURLException e ) {
            throw new ResourceInitException( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            throw new ResourceInitException( e.getMessage(), e );
        }

    }

    @Override
    public void destroy() {
        if ( service != null ) {
            service.close();
        }
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
            req = (WMSRequestType) ( (ImplementationMetadata) serviceInfo ).getRequestTypeByName( map.get( "REQUEST" ) );
        } catch ( IllegalArgumentException e ) {
            controllers.get( version ).sendException( new OWSException( get( "WMS.OPERATION_NOT_KNOWN",
                                                                             map.get( "REQUEST" ) ),
                                                                        OWSException.OPERATION_NOT_SUPPORTED ),
                                                      response );
            return;
        } catch ( NullPointerException e ) {
            controllers.get( version ).sendException( new OWSException( get( "WMS.PARAM_MISSING", "REQUEST" ),
                                                                        OWSException.OPERATION_NOT_SUPPORTED ),
                                                      response );
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
                            throws IOException, OWSException, org.deegree.protocol.ows.exception.OWSException {
        try {
            switch ( req ) {
            case GetCapabilities:
            case capabilities:
                break;
            default:
                if ( controllers.get( version ) == null ) {
                    throw new OWSException( get( "WMS.VERSION_UNSUPPORTED", version ),
                                            OWSException.INVALID_PARAMETER_VALUE );
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
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.DIMENSION_VALUE_MISSING", e.name ), "MissingDimensionValue" );
        } catch ( InvalidDimensionValue e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.DIMENSION_VALUE_INVALID", e.value, e.name ), "InvalidDimensionValue" );
        }
    }

    /**
     * @param response
     */
    private static void getDtd( HttpResponseBuffer response ) {
        InputStream in = WMSController.class.getResourceAsStream( "WMS_MS_Capabilities.dtd.invalid" );
        try {
            OutputStream out = response.getOutputStream();
            byte[] buf = new byte[65536];
            int read;
            while ( ( read = in.read( buf ) ) != -1 ) {
                out.write( buf, 0, read );
            }
        } catch ( IOException e ) {
            LOG.trace( "Could not read/write the internal DTD:", e );
        } finally {
            try {
                in.close();
            } catch ( IOException e ) {
                LOG.trace( "Error while closing DTD input stream:", e );
            }
        }
    }

    private void getLegendGraphic( Map<String, String> map, HttpResponseBuffer response )
                            throws OWSException, IOException {
        GetLegendGraphic glg = securityManager == null ? new GetLegendGraphic( map )
                                                      : securityManager.preprocess( new GetLegendGraphic( map ),
                                                                                    OGCFrontController.getContext().getCredentials() );

        if ( !supportedImageFormats.contains( glg.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", glg.getFormat() ), OWSException.INVALID_FORMAT );
        }
        BufferedImage img = service.getLegend( glg );
        sendImage( img, response, glg.getFormat() );
    }

    private static void runTemplate( HttpResponseBuffer response, String fiFile, FeatureCollection col,
                                     boolean geometries )
                            throws UnsupportedEncodingException, IOException {
        PrintWriter out = new PrintWriter( new OutputStreamWriter( response.getOutputStream(), "UTF-8" ) );

        try {
            InputStream in;
            if ( fiFile == null ) {
                in = WMSController.class.getResourceAsStream( "html.gfi" );
            } else {
                in = new FileInputStream( fiFile );
            }

            Symbol s = new TemplatingParser( new TemplatingLexer( new InputStreamReader( in, "UTF-8" ) ) ).parse();
            @SuppressWarnings(value = "unchecked")
            HashMap<String, Object> tmpl = (HashMap<String, Object>) s.value;
            StringBuilder sb = new StringBuilder();
            new PropertyTemplateCall( "start", singletonList( "*" ), false ).eval( sb, tmpl, col, geometries );
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
                            throws OWSException, IOException, MissingDimensionValue, InvalidDimensionValue,
                            org.deegree.protocol.ows.exception.OWSException {

        Pair<FeatureCollection, LinkedList<String>> pair;
        String format;
        List<String> queryLayers;
        boolean geometries;
        FeatureType type = null;
        ICRS crs;
        Map<String, String> nsBindings = new HashMap<String, String>();
        if ( service.isNewStyle() ) {
            LinkedList<String> headers = new LinkedList<String>();
            org.deegree.protocol.wms.ops.GetFeatureInfo fi = new org.deegree.protocol.wms.ops.GetFeatureInfo( map,
                                                                                                              version );
            checkGetFeatureInfo( version, fi );
            crs = fi.getCoordinateSystem();
            geometries = fi.returnGeometries();
            queryLayers = map( fi.getQueryLayers(), CollectionUtils.<LayerRef> getToStringMapper() );

            RenderingInfo info = new RenderingInfo( fi.getInfoFormat(), fi.getWidth(), fi.getHeight(), false, null,
                                                    fi.getEnvelope(), 0.28, map );
            format = fi.getInfoFormat();
            info.setFormat( format );
            info.setFeatureCount( fi.getFeatureCount() );
            info.setX( fi.getX() );
            info.setY( fi.getY() );
            pair = new Pair<FeatureCollection, LinkedList<String>>( service.getFeatures( fi, headers ), headers );
        } else {
            GetFeatureInfo fi = securityManager == null ? new GetFeatureInfo( map, version, service )
                                                       : securityManager.preprocess( new GetFeatureInfo( map, version,
                                                                                                         service ),
                                                                                     OGCFrontController.getContext().getCredentials() );
            crs = fi.getCoordinateSystem();
            geometries = fi.returnGeometries();
            format = fi.getInfoFormat();
            checkGetFeatureInfo( fi );
            pair = service.getFeatures( fi );
            Mapper<String, Layer> layerNameMapper = new Mapper<String, Layer>() {
                @Override
                public String apply( Layer u ) {
                    return u.getName();
                }
            };
            queryLayers = map( fi.getQueryLayers(), layerNameMapper );
        }

        FeatureCollection col = pair.first;
        addHeaders( response, pair.second );
        format = format == null ? "application/vnd.ogc.gml" : format;
        response.setContentType( format );
        response.setCharacterEncoding( "UTF-8" );

        Map<String, String> fismap = new HashMap<String, String>();
        fismap.put( "LAYERS", reduce( "", queryLayers, getStringJoiner( "," ) ) );
        GetFeatureInfoSchema fis = new GetFeatureInfoSchema( fismap );
        List<FeatureType> schema = service.getSchema( fis );
        for ( FeatureType ft : schema ) {
            type = ft;
            if ( ft.getSchema() != null ) {
                nsBindings.putAll( ft.getSchema().getNamespaceBindings() );
            }
        }

        FeatureInfoSerializer serializer = featureInfoSerializers.get( format );
        if ( serializer != null ) {
            serializer.serialize( nsBindings, col, response.getOutputStream() );
            response.flushBuffer();
            return;
        }

        String fiFile = supportedFeatureInfoFormats.get( format );
        if ( !fiFile.isEmpty() ) {
            runTemplate( response, fiFile, col, geometries );
            return;
        }

        if ( format.equalsIgnoreCase( "application/vnd.ogc.gml" ) || format.equalsIgnoreCase( "text/xml" )
             || defaultGMLGFIFormats.contains( format.toLowerCase() ) ) {
            try {
                XMLStreamWriter xmlWriter = response.getXMLWriter();
                String loc = getHttpGetURL() + "request=GetFeatureInfoSchema&layers=" + join( ",", queryLayers );

                // for more than just quick 'hacky' schemaLocation attributes one should use a proper WFS
                HashMap<String, String> bindings = new HashMap<String, String>();
                String ns = type == null ? null : type.getName().getNamespaceURI();
                if ( ns != null && ns.isEmpty() ) {
                    ns = null;
                }
                if ( ns != null ) {
                    bindings.put( ns, loc );
                    if ( !nsBindings.containsValue( ns ) ) {
                        nsBindings.put( "app", ns );
                    }
                }
                if ( !nsBindings.containsKey( "app" ) ) {
                    nsBindings.put( "app", "http://www.deegree.org/app" );
                }
                bindings.put( "http://www.opengis.net/wfs", "http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd" );

                GMLVersion gmlVersion = GMLVersion.GML_2;
                if ( format.endsWith( "3.0" ) || format.endsWith( "3.0.1" ) ) {
                    gmlVersion = GMLVersion.GML_30;
                }
                if ( format.endsWith( "3.1" ) || format.endsWith( "3.1.1" ) ) {
                    gmlVersion = GMLVersion.GML_31;
                }
                if ( format.endsWith( "3.2" ) || format.endsWith( "3.2.1" ) ) {
                    gmlVersion = GMLVersion.GML_32;
                }

                GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlWriter );
                gmlWriter.setOutputCrs( crs );
                gmlWriter.setNamespaceBindings( nsBindings );
                gmlWriter.setExportGeometries( geometries );
                gmlWriter.getFeatureWriter().export( col, ns == null ? loc : null, bindings );
            } catch ( XMLStreamException e ) {
                LOG.warn( "Error when writing GetFeatureInfo GML response '{}'.", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            } catch ( UnknownCRSException e ) {
                LOG.warn( "Could not instantiate the geometry transformer for output srs '{}'."
                          + " Aborting GetFeatureInfo response.", crs );
                LOG.trace( "Stack trace:", e );
            } catch ( TransformationException e ) {
                LOG.warn( "Could transform to output srs '{}'. Aborting GetFeatureInfo response.", crs );
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
            runTemplate( response, null, col, geometries );
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
            if ( namespace != null && !namespace.isEmpty() ) {
                nsToPrefix.put( "app", namespace );
            }
            new GMLAppSchemaWriter( GMLVersion.GML_2, namespace, null, nsToPrefix ).export( writer, schema );
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
                            throws OWSException, IOException, MissingDimensionValue, InvalidDimensionValue,
                            org.deegree.protocol.ows.exception.OWSException {

        if ( service.isNewStyle() ) {
            org.deegree.protocol.wms.ops.GetMap gm2 = new org.deegree.protocol.wms.ops.GetMap( map, version,
                                                                                               service.getExtensions() );

            checkGetMap( version, gm2 );

            RenderingInfo info = new RenderingInfo( gm2.getFormat(), gm2.getWidth(), gm2.getHeight(),
                                                    gm2.getTransparent(), gm2.getBgColor(), gm2.getBoundingBox(),
                                                    gm2.getPixelSize(), map );
            RenderContext ctx = new DefaultRenderContext( info );
            ctx.setOutput( response.getOutputStream() );
            LinkedList<String> headers = new LinkedList<String>();
            service.getMap( gm2, headers, ctx );
            response.setContentType( gm2.getFormat() );
            ctx.close();
            addHeaders( response, headers );
        } else {
            GetMap gm = new GetMap( map, version, service );
            checkGetMap( version, gm );

            if ( securityManager != null ) {
                gm = securityManager.preprocess( gm, OGCFrontController.getContext().getCredentials() );
            }
            final Pair<BufferedImage, LinkedList<String>> pair = service.getMapImage( gm );
            addHeaders( response, pair.second );
            sendImage( pair.first, response, gm.getFormat() );
        }
    }

    private void checkGetFeatureInfo( GetFeatureInfo gfi )
                            throws OWSException {
        if ( gfi.getInfoFormat() != null && !gfi.getInfoFormat().equals( "" )
             && !supportedFeatureInfoFormats.containsKey( gfi.getInfoFormat() ) ) {
            throw new OWSException( get( "WMS.INVALID_INFO_FORMAT", gfi.getInfoFormat() ), OWSException.INVALID_FORMAT );
        }
    }

    private void checkGetFeatureInfo( Version version, org.deegree.protocol.wms.ops.GetFeatureInfo gfi )
                            throws OWSException {
        if ( gfi.getInfoFormat() != null && !gfi.getInfoFormat().equals( "" )
             && !supportedFeatureInfoFormats.containsKey( gfi.getInfoFormat() ) ) {
            throw new OWSException( get( "WMS.INVALID_INFO_FORMAT", gfi.getInfoFormat() ), OWSException.INVALID_FORMAT );
        }
        if ( service.isNewStyle() ) {
            for ( LayerRef lr : gfi.getQueryLayers() ) {
                if ( !service.hasTheme( lr.getName() ) ) {
                    throw new OWSException( "The layer with name " + lr.getName() + " is not defined.",
                                            "LayerNotDefined", "layers" );
                }
            }
            for ( StyleRef sr : gfi.getStyles() ) {
                // TODO check style availability
            }
        }
        try {
            if ( gfi.getCoordinateSystem() == null ) {
                // this can happen if some AUTO SRS id was invalid
                controllers.get( version ).throwSRSException( "automatic" );
            }
            ICRS crs = gfi.getCoordinateSystem();
            if ( crs instanceof CRSRef ) {
                ( (CRSRef) crs ).getReferencedObject();
            }
        } catch ( ReferenceResolvingException e ) {
            // only throw an exception if a truly invalid srs is found
            // this makes it possible to request srs that are not advertised, which may be useful
            controllers.get( version ).throwSRSException( gfi.getCoordinateSystem().getAlias() );
        }
    }

    private void checkGetMap( Version version, GetMap gm )
                            throws OWSException {
        if ( !supportedImageFormats.contains( gm.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", gm.getFormat() ), OWSException.INVALID_FORMAT );
        }
        try {
            // check for existence/validity
            if ( gm.getCoordinateSystem() == null ) {
                // this can happen if some AUTO SRS id was invalid
                controllers.get( version ).throwSRSException( "automatic" );
            }
            ICRS crs = gm.getCoordinateSystem();
            if ( crs instanceof CRSRef ) {
                ( (CRSRef) crs ).getReferencedObject();
            }
        } catch ( ReferenceResolvingException e ) {
            // only throw an exception if a truly invalid srs is found
            // this makes it possible to request srs that are not advertised, which may be useful
            controllers.get( version ).throwSRSException( gm.getCoordinateSystem().getAlias() );
        }

    }

    private void checkGetMap( Version version, org.deegree.protocol.wms.ops.GetMap gm )
                            throws OWSException {
        if ( !supportedImageFormats.contains( gm.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", gm.getFormat() ), OWSException.INVALID_FORMAT );
        }
        if ( service.isNewStyle() ) {
            for ( LayerRef lr : gm.getLayers() ) {
                if ( !service.hasTheme( lr.getName() ) ) {
                    throw new OWSException( "The layer with name " + lr.getName() + " is not defined.",
                                            "LayerNotDefined", "layers" );
                }
            }
            for ( StyleRef sr : gm.getStyles() ) {
                // TODO check style availability here instead of the layer
            }
        }
        try {
            // check for existence/validity
            if ( gm.getCoordinateSystem() == null ) {
                // this can happen if some AUTO SRS id was invalid
                controllers.get( version ).throwSRSException( "automatic" );
            }
            ICRS crs = gm.getCoordinateSystem();
            if ( crs instanceof CRSRef ) {
                ( (CRSRef) crs ).getReferencedObject();
            }
        } catch ( ReferenceResolvingException e ) {
            // only throw an exception if a truly invalid srs is found
            // this makes it possible to request srs that are not advertised, which may be useful
            controllers.get( version ).throwSRSException( gm.getCoordinateSystem().getAlias() );
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

        // override service metadata if available from manager
        OWSMetadataProvider metadata = null;
        if ( configId != null ) {
            OWSMetadataProviderManager mgr = workspace.getSubsystemManager( OWSMetadataProviderManager.class );
            ResourceState<OWSMetadataProvider> state = mgr.getState( configId );
            if ( state != null ) {
                metadata = state.getResource();
                if ( metadata != null ) {
                    identification = metadata.getServiceIdentification();
                    provider = metadata.getServiceProvider();
                    extendedCaps = metadata.getExtendedCapabilities();
                }
            }
        }

        if ( service.getDynamics().isEmpty() ) {
            controllers.get( myVersion ).getCapabilities( getUrl, postUrl, updateSequence, service, response,
                                                          identification, provider, map, this, metadata );
        } else {
            // need to synchronize here as well, else the layer list may be updating right now (the service.update()
            // does not strictly need to be synchronized for this use case, but it sure is a Good Thing to do it anyway)
            synchronized ( this ) {
                service.update();
                controllers.get( myVersion ).getCapabilities( getUrl, postUrl, updateSequence, service, response,
                                                              identification, provider, map, this, metadata );
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
            throw new OWSException( get( "WMS.CANNOT_ENCODE_IMAGE", format ), OWSException.NO_APPLICABLE_CODE );
        }
    }

    @Override
    public Pair<XMLExceptionSerializer<OWSException>, String> getExceptionSerializer( Version requestVersion ) {

        WMSControllerBase controller = requestVersion == null ? null : controllers.get( requestVersion );
        if ( controller == null ) {
            Iterator<WMSControllerBase> iterator = controllers.values().iterator();
            while ( iterator.hasNext() ) {
                controller = iterator.next();
            }
        }
        return new Pair<XMLExceptionSerializer<OWSException>, String>( controller.EXCEPTIONS, controller.EXCEPTION_MIME );
    }

    public List<OMElement> getExtendedCapabilities( String version ) {
        List<OMElement> list = extendedCaps.get( version );
        if ( list == null ) {
            list = extendedCaps.get( "default" );
        }
        return list;
    }

    public String getMetadataURLTemplate() {
        // TODO handle this properly in init(), needs service level dependency management
        if ( metadataURLTemplate == null ) {
            WebServicesConfiguration mgr = workspace.getSubsystemManager( WebServicesConfiguration.class );
            Map<String, List<OWS>> ctrls = mgr.getAll();
            for ( List<OWS> lists : ctrls.values() ) {
                for ( OWS o : lists ) {
                    ImplementationMetadata<?> md = o.getImplementationMetadata();
                    for ( String s : md.getImplementedServiceName() ) {
                        if ( s.equalsIgnoreCase( "csw" )
                             && md.getImplementedVersions().contains( new Version( 2, 0, 2 ) ) ) {
                            this.metadataURLTemplate = ""; // special case to use requested address
                        }
                    }
                }
            }
        }
        return metadataURLTemplate;
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
                              HttpResponseBuffer response, ServiceIdentification identification,
                              ServiceProvider provider, Map<String, String> customParameters, WMSController controller,
                              OWSMetadataProvider metadata )
                                throws OWSException, IOException;

        /**
         * @param name
         * @throws OWSException
         */
        void throwSRSException( String name )
                                throws OWSException;
    }

}
