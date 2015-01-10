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

import static javax.imageio.ImageIO.write;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.commons.utils.CollectionUtils.getStringJoiner;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.services.controller.OGCFrontController.getHttpGetURL;
import static org.deegree.services.i18n.Messages.get;
import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.featureinfo.FeatureInfoParams;
import org.deegree.featureinfo.serializing.FeatureInfoSerializer;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaWriter;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.protocol.wms.ops.GetFeatureInfoSchema;
import org.deegree.protocol.wms.ops.GetLegendGraphic;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.OwsManager;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.utils.StandardFeatureInfoContext;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.wms.DeegreeWMS;
import org.deegree.services.jaxb.wms.DeegreeWMS.ExtendedCapabilities;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat.Serializer;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat.XSLTFile;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.plugins.DefaultOutputFormatProvider;
import org.deegree.services.wms.controller.plugins.ImageSerializer;
import org.deegree.services.wms.controller.plugins.OutputFormatProvider;
import org.deegree.services.wms.utils.GetMapLimitChecker;
import org.deegree.style.StyleRef;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
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

    private final HashMap<String, ImageSerializer> imageSerializers = new HashMap<String, ImageSerializer>();

    protected MapService service;

    protected ServiceIdentification identification;

    protected ServiceProvider provider;

    protected TreeMap<Version, WMSControllerBase> controllers = new TreeMap<Version, WMSControllerBase>();

    private Version highestVersion;

    private Map<String, List<OMElement>> extendedCaps;

    private String metadataURLTemplate;

    private FeatureInfoManager featureInfoManager;
    
    private OutputFormatProvider ouputFormatProvider;

    private OWSMetadataProvider metadataProvider;

    private DeegreeWMS conf;

    private final GetMapLimitChecker getMapLimitChecker = new GetMapLimitChecker();

    public WMSController( ResourceMetadata<OWS> metadata, Workspace workspace, DeegreeWMS jaxbConfig ) {
        super( metadata, workspace, jaxbConfig );

        final boolean addDefaultFormats;
        final FeatureInfoFormatsType featureInfoFormats = jaxbConfig.getFeatureInfoFormats();
        if ( featureInfoFormats != null ) {
            final Boolean enableDefaultFormats = featureInfoFormats.isEnableDefaultFormats();
            addDefaultFormats = enableDefaultFormats == null || enableDefaultFormats;
        } else {
            addDefaultFormats = true;
        }

        featureInfoManager = new FeatureInfoManager( addDefaultFormats );
        
        ouputFormatProvider = new DefaultOutputFormatProvider();
    }
    
    public Collection<String> getSupportedImageFormats() {
        return ouputFormatProvider.getSupportedOutputFormats();
    }

    /**
     * Returns the configuration.
     * 
     * @return the configuration, after successful initialization, this is never <code>null</code>
     */
    public DeegreeWMS getConfig() {
        return conf;
    }

    /**
     * @return the underlying map service
     */
    public MapService getMapService() {
        return service;
    }

    private void handleMetadata( String metadataURLTemplate, String storeid ) {
        this.metadataURLTemplate = metadataURLTemplate;
    }

    @Override
    public void init( DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConfig,
                      Object controllerConf ) {

        identification = convertFromJAXB( serviceMetadata.getServiceIdentification() );
        provider = convertFromJAXB( serviceMetadata.getServiceProvider() );

        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "wms", "http://www.deegree.org/services/wms" );

        conf = (DeegreeWMS) controllerConf;

        if ( conf.getExtendedCapabilities() != null ) {
            this.extendedCaps = new HashMap<String, List<OMElement>>();
            List<OMElement> caps = new ArrayList<OMElement>( conf.getExtendedCapabilities().size() );
            extendedCaps.put( "default", caps );
            for ( ExtendedCapabilities extendedCapsConf : conf.getExtendedCapabilities() ) {
                DOMSource domSource = new DOMSource( extendedCapsConf.getAny() );
                XMLStreamReader xmlStream;
                try {
                    xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( domSource );
                } catch ( Exception t ) {
                    throw new ResourceInitException( "Error extracting extended capabilities: " + t.getMessage(), t );
                }
                caps.add( new XMLAdapter( xmlStream ).getRootElement() );
            }
        }

        try {
            if ( conf.getFeatureInfoFormats() != null ) {
                for ( GetFeatureInfoFormat t : conf.getFeatureInfoFormats().getGetFeatureInfoFormat() ) {
                    if ( t.getFile() != null ) {
                        featureInfoManager.addOrReplaceFormat( t.getFormat(),
                                                               metadata.getLocation().resolveToFile( t.getFile() ).toString() );
                    } else if ( t.getXSLTFile() != null ) {
                        XSLTFile xsltFile = t.getXSLTFile();
                        GMLVersion version = GMLVersion.valueOf( xsltFile.getGmlVersion().toString() );
                        featureInfoManager.addOrReplaceXsltFormat( t.getFormat(),
                                                                   metadata.getLocation().resolveToUrl( xsltFile.getValue() ),
                                                                   version, workspace );
                    } else if ( t.getSerializer() != null ) {
                        Serializer serializer = t.getSerializer();

                        FeatureInfoSerializer featureInfoSerializer;
                        try {
                            Class<?> clazz = workspace.getModuleClassLoader().loadClass( serializer.getJavaClass() );
                            featureInfoSerializer = clazz.asSubclass( FeatureInfoSerializer.class ).newInstance();
                        } catch ( ClassNotFoundException e ) {
                            throw new IllegalArgumentException( "Couldn't find serializer class", e );
                        } catch ( ClassCastException e ) {
                            throw new IllegalArgumentException(
                                                                "Configured serializer class doesn't implement FeatureInfoSerializer",
                                                                e );
                        }

                        featureInfoManager.addOrReplaceCustomFormat( t.getFormat(), featureInfoSerializer );
                    } else {
                        throw new IllegalArgumentException( "Unknown GetFeatureInfoFormat" );
                    }
                }
            }

            // if ( pi.getImageFormat() != null ) {
            // for ( ImageFormat f : pi.getImageFormat() ) {
            // instantiateSerializer( imageSerializers, f.getFormat(), f.getClazz(), ImageSerializer.class );
            // }
            // }

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
            service = new MapService( sc, workspace );

            // after the service knows what layers are available:
            handleMetadata( conf.getMetadataURLTemplate(), conf.getMetadataStoreId() );

            String configId = getMetadata().getIdentifier().getId();
            metadataProvider = workspace.getResource( OWSMetadataProviderProvider.class, configId + "_metadata" );
        } catch ( Exception e ) {
            throw new ResourceInitException( e.getMessage(), e );
        }

    }

    @Override
    public void doKVP( Map<String, String> map, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {
        String v = map.get( "VERSION" );
        if ( v == null ) {
            v = map.get( "WMTVER" );
        }
        Version version = v == null ? highestVersion : parseVersion( v );

        WMSRequestType req;
        try {
            req = (WMSRequestType) ( (ImplementationMetadata) ( (OWSProvider) getMetadata().getProvider() ).getImplementationMetadata() ).getRequestTypeByName( map.get( "REQUEST" ) );
        } catch ( IllegalArgumentException e ) {
            controllers.get( version ).sendException( new OWSException( get( "WMS.OPERATION_NOT_KNOWN",
                                                                             map.get( "REQUEST" ) ),
                                                                        OWSException.OPERATION_NOT_SUPPORTED ),
                                                      response, this );
            return;
        } catch ( NullPointerException e ) {
            controllers.get( version ).sendException( new OWSException( get( "WMS.PARAM_MISSING", "REQUEST" ),
                                                                        OWSException.OPERATION_NOT_SUPPORTED ),
                                                      response, this );
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
        GetLegendGraphic glg = new GetLegendGraphic( map );

        if ( !getSupportedImageFormats().contains( glg.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", glg.getFormat() ), OWSException.INVALID_FORMAT );
        }
        BufferedImage img = service.getLegend( glg );
        sendImage( img, response, glg.getFormat() );
    }

    private void getFeatureInfo( Map<String, String> map, final HttpResponseBuffer response, Version version )
                            throws OWSException, IOException, MissingDimensionValue, InvalidDimensionValue {

        Pair<FeatureCollection, LinkedList<String>> pair;
        String format;
        List<String> queryLayers;
        boolean geometries;
        FeatureType type = null;
        ICRS crs;
        Map<String, String> nsBindings = new HashMap<String, String>();

        LinkedList<String> headers = new LinkedList<String>();
        org.deegree.protocol.wms.ops.GetFeatureInfo fi = new org.deegree.protocol.wms.ops.GetFeatureInfo( map, version );
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

        String loc = getHttpGetURL() + "request=GetFeatureInfoSchema&layers=" + join( ",", queryLayers );

        try {
            FeatureInfoParams params = new FeatureInfoParams( nsBindings, col, format, geometries, loc, type, crs );
            featureInfoManager.serializeFeatureInfo( params, new StandardFeatureInfoContext( response ) );
            response.flushBuffer();
        } catch ( XMLStreamException e ) {
            throw new IOException( e.getLocalizedMessage(), e );
        }
    }

    private void getFeatureInfoSchema( Map<String, String> map, HttpResponseBuffer response )
                            throws IOException {
        GetFeatureInfoSchema fis = new GetFeatureInfoSchema( map );
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
                            throws OWSException, IOException, MissingDimensionValue, InvalidDimensionValue {
        org.deegree.protocol.wms.ops.GetMap gm2 = new org.deegree.protocol.wms.ops.GetMap( map, version,
                                                                                           service.getExtensions() );

        checkGetMap( version, gm2 );

        RenderingInfo info = new RenderingInfo( gm2.getFormat(), gm2.getWidth(), gm2.getHeight(), gm2.getTransparent(),
                                                gm2.getBgColor(), gm2.getBoundingBox(), gm2.getPixelSize(), map );
        RenderContext ctx = ouputFormatProvider.getRenderers( info, response.getOutputStream() );
        LinkedList<String> headers = new LinkedList<String>();
        service.getMap( gm2, headers, ctx );
        response.setContentType( gm2.getFormat() );
        ctx.close();
        addHeaders( response, headers );
    }

    private void checkGetFeatureInfo( Version version, org.deegree.protocol.wms.ops.GetFeatureInfo gfi )
                            throws OWSException {
        if ( gfi.getInfoFormat() != null && !gfi.getInfoFormat().equals( "" )
             && !featureInfoManager.getSupportedFormats().contains( gfi.getInfoFormat() ) ) {
            throw new OWSException( get( "WMS.INVALID_INFO_FORMAT", gfi.getInfoFormat() ), OWSException.INVALID_FORMAT );
        }
        for ( LayerRef lr : gfi.getQueryLayers() ) {
            if ( !service.hasTheme( lr.getName() ) ) {
                throw new OWSException( "The layer with name " + lr.getName() + " is not defined.", "LayerNotDefined",
                                        "layers" );
            }
        }
        for ( StyleRef sr : gfi.getStyles() ) {
            // TODO check style availability
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

    private void checkGetMap( Version version, org.deegree.protocol.wms.ops.GetMap gm )
                            throws OWSException {
        if ( !getSupportedImageFormats().contains( gm.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", gm.getFormat() ), OWSException.INVALID_FORMAT );
        }
        for ( LayerRef lr : gm.getLayers() ) {
            if ( !service.hasTheme( lr.getName() ) ) {
                throw new OWSException( "The layer with name " + lr.getName() + " is not defined.", "LayerNotDefined",
                                        "layers" );
            }
        }
        for ( StyleRef sr : gm.getStyles() ) {
            // TODO check style availability here instead of the layer
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
        getMapLimitChecker.checkRequestedSizeAndLayerCount( gm, conf );
    }

    protected void getCapabilities( Map<String, String> map, HttpResponseBuffer response )
                            throws OWSException, IOException {

        String version = map.get( "VERSION" );
        // not putting it into the bean, why should I? It's used just a few lines below...

        String updateSequence = map.get( "UPDATESEQUENCE" );
        if ( version == null ) {
            version = map.get( "WMTVER" );
        }
        GetCapabilities req = new GetCapabilities( version );

        Version myVersion = negotiateVersion( req );

        String getUrl = OGCFrontController.getHttpGetURL();
        String postUrl = OGCFrontController.getHttpPostURL();

        if ( metadataProvider != null ) {
            controllers.get( myVersion ).getCapabilities( getUrl, postUrl, updateSequence, service, response,
                                                          metadataProvider.getServiceIdentification(),
                                                          metadataProvider.getServiceProvider(), map, this,
                                                          metadataProvider );
        } else {
            controllers.get( myVersion ).getCapabilities( getUrl, postUrl, updateSequence, service, response,
                                                          identification, provider, map, this, null );
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
    public XMLExceptionSerializer getExceptionSerializer( Version requestVersion ) {

        WMSControllerBase controller = requestVersion == null ? null : controllers.get( requestVersion );
        if ( controller == null ) {
            Iterator<WMSControllerBase> iterator = controllers.values().iterator();
            while ( iterator.hasNext() ) {
                controller = iterator.next();
            }
        }
        return controller.exceptionSerializer;
    }

    public List<OMElement> getExtendedCapabilities( String version ) {
        Map<String, List<OMElement>> extendedCaps;
        if ( metadataProvider != null ) {
            extendedCaps = metadataProvider.getExtendedCapabilities();
        } else {
            extendedCaps = this.extendedCaps;
        }

        List<OMElement> list = extendedCaps.get( version );
        if ( list == null ) {
            list = extendedCaps.get( "default" );
        }
        return list;
    }

    public String getMetadataURLTemplate() {
        // TODO handle this properly in init(), needs service level dependency management
        if ( metadataURLTemplate == null ) {
            OwsManager mgr = workspace.getResourceManager( OwsManager.class );
            Map<String, List<OWS>> ctrls = mgr.getAll();
            for ( List<OWS> lists : ctrls.values() ) {
                for ( OWS o : lists ) {
                    ImplementationMetadata<?> md = ( (OWSProvider) o.getMetadata().getProvider() ).getImplementationMetadata();
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

    public FeatureInfoManager getFeatureInfoManager() {
        return featureInfoManager;
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
        void sendException( OWSException ex, HttpResponseBuffer response, WMSController controller )
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

    @Override
    public void destroy() {
        // nothing to do
    }

}
