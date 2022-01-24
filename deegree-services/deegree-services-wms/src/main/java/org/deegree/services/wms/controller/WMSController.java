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
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.commons.utils.CollectionUtils.getStringJoiner;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.services.controller.OGCFrontController.getHttpGetURL;
import static org.deegree.services.i18n.Messages.get;
import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.XMLStreamUtils;
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
import org.deegree.protocol.wms.capabilities.GetCapabilitiesXMLAdapter;
import org.deegree.protocol.wms.featureinfo.GetFeatureInfoParser;
import org.deegree.protocol.wms.map.GetMapParser;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.protocol.wms.ops.GetFeatureInfoSchema;
import org.deegree.protocol.wms.ops.GetLegendGraphic;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.rendering.r2d.ImageSerializer;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.OwsManager;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.ExceptionSerializer;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.utils.StandardFeatureInfoContext;
import org.deegree.services.encoding.SupportedEncodings;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.wms.DeegreeWMS;
import org.deegree.services.jaxb.wms.DeegreeWMS.ExtendedCapabilities;
import org.deegree.services.jaxb.wms.DeegreeWMS.SupportedVersions;
import org.deegree.services.jaxb.wms.ExceptionFormatsType;
import org.deegree.services.jaxb.wms.ExceptionFormatsType.ExceptionFormat;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat.Serializer;
import org.deegree.services.jaxb.wms.FeatureInfoFormatsType.GetFeatureInfoFormat.XSLTFile;
import org.deegree.services.jaxb.wms.GetCapabilitiesFormatsType;
import org.deegree.services.jaxb.wms.GetCapabilitiesFormatsType.GetCapabilitiesFormat;
import org.deegree.services.jaxb.wms.GetMapFormatsType;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.capabilities.serialize.CapabilitiesManager;
import org.deegree.services.wms.controller.exceptions.ExceptionsManager;
import org.deegree.services.wms.controller.plugins.DefaultOutputFormatProvider;
import org.deegree.services.wms.controller.plugins.OutputFormatProvider;
import org.deegree.services.wms.utils.GetMapLimitChecker;
import org.deegree.services.wms.utils.SupportedEncodingsParser;
import org.deegree.style.StyleRef;
import org.deegree.style.utils.ColorQuantizer;
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

    /** The list of supported image formats. */
    private final LinkedList<String> supportedImageFormats = new LinkedList<String>();

    protected MapService service;

    protected ServiceIdentification identification;

    protected ServiceProvider provider;

    protected TreeMap<Version, WMSControllerBase> controllers = new TreeMap<Version, WMSControllerBase>();

    private Version highestVersion;

    private Map<String, List<OMElement>> extendedCaps;

    private String metadataURLTemplate;

    private FeatureInfoManager featureInfoManager;

    private CapabilitiesManager capabilitiesManager;

    private ExceptionsManager exceptionsManager;

    private OutputFormatProvider ouputFormatProvider;

    private OWSMetadataProvider metadataProvider;

    private DeegreeWMS conf;

    private final GetMapLimitChecker getMapLimitChecker = new GetMapLimitChecker();

    private SupportedEncodings supportedEncodings;

    private boolean isStrict;

    public WMSController( ResourceMetadata<OWS> metadata, Workspace workspace, DeegreeWMS jaxbConfig ) {
        super( metadata, workspace, jaxbConfig );
        capabilitiesManager = new CapabilitiesManager( isAddCapabilitiesDefaultFormatsEnabled( jaxbConfig ) );
        featureInfoManager = new FeatureInfoManager( isAddFeatureInfoDefaultFormatsEnabled( jaxbConfig ) );
        exceptionsManager = new ExceptionsManager( isAddExceptionsDefaultFormatsEnabled( jaxbConfig ), this );
        ouputFormatProvider = new DefaultOutputFormatProvider();
        initOfferedVersions( jaxbConfig.getSupportedVersions() );
    }

    public Collection<String> getSupportedImageFormats() {
        return supportedImageFormats;
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
            addSupportedCapabilitiesFormats( conf );
            addSupportedImageFormats( conf );
            addSupportedFeatureInfoFormats( conf );
            addSupportedExceptionFormats( conf );

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
                    controllers.put( VERSION_111, new WMSController111( exceptionsManager ) );
                }
                if ( v.equals( VERSION_130 ) ) {
                    controllers.put( VERSION_130, new WMSController130( capabilitiesManager, exceptionsManager ) );
                }
            }

            Iterator<Version> iter = controllers.keySet().iterator();
            while ( iter.hasNext() ) {
                highestVersion = iter.next();
            }

            ServiceConfigurationType sc = conf.getServiceConfiguration();
            int capabilitiesVersion = conf.getUpdateSequence() != null ? conf.getUpdateSequence().intValue() : 0;
            service = new MapService( sc, workspace, capabilitiesVersion );

            // after the service knows what layers are available:
            handleMetadata( conf.getMetadataURLTemplate(), conf.getMetadataStoreId() );

            String configId = getMetadata().getIdentifier().getId();
            metadataProvider = workspace.getResource( OWSMetadataProviderProvider.class, configId + "_metadata" );
            
            supportedEncodings = new SupportedEncodingsParser().parseEncodings( conf );
            isStrict = conf.isStrict() != null ? conf.isStrict() : false;
        } catch ( Exception e ) {
            throw new ResourceInitException( e.getMessage(), e );
        }

    }

    @Override
    public void doKVP( Map<String, String> map, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {
        String v = getVersionValueFromRequest( map );
        Version version;
        try {
            version = v == null ? highestVersion : Version.parseVersion( v );
        } catch ( InvalidParameterValueException e ) {
            controllers.get( highestVersion ).sendException( new OWSException( get( "WMS.VERSION_UNSUPPORTED", v ),
                                                                               OWSException.INVALID_PARAMETER_VALUE ),
                                                             response, this );
            return;
        }

        if ( isStrict ) {
            String service = map.get( "SERVICE" );
            if ( service != null && !"WMS".equalsIgnoreCase( service ) ) {
                controllers.get( version ).sendException(
                                        new OWSException( "The parameter SERVICE must be 'WMS', but is '" + service + "'",
                                                          OWSException.INVALID_PARAMETER_VALUE ), response, this );
                return;
            }
        }
        WMSRequestType req;
        String requestName = map.get( "REQUEST" );
        try {
            req = parseRequest( requestName );
        } catch ( IllegalArgumentException e ) {
            controllers.get( version ).sendException( new OWSException( get( "WMS.OPERATION_NOT_KNOWN", requestName ),
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
            if ( !supportedEncodings.isEncodingSupported( req, "KVP" ) ) {
                throw new OWSException( "GET/KVP is not supported for " + requestName + " requests.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }
            handleRequest( req, response, map, version );
        } catch ( OWSException e ) {
            if ( controllers.get( version ) == null ) {
                // happens if non capabilities request is made with unsupported version
                version = highestVersion;
            }

            LOG.debug( "The response is an exception with the message '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace of OWSException being sent", e );

            controllers.get( version ).handleException( map, req, e, response, this );
        } catch ( Exception e ) {
            LOG.debug( "OWS-Exception: {}", e.getMessage() );
            LOG.trace( e.getMessage(), e );
            controllers.get( version ).handleException( map, req, new OWSException( e.getMessage(), NO_APPLICABLE_CODE ), response, this );
        }
    }

    private WMSRequestType parseRequest( String requestName ) {
        WMSRequestType requestType = (WMSRequestType) ( (ImplementationMetadata<?>) ( (OWSProvider) getMetadata().getProvider() ).getImplementationMetadata() ).getRequestTypeByName(
                        requestName );
        if ( requestType == null ) {
            throw new IllegalArgumentException( "Request type " + requestName + "is not known." );
        }
        return requestType;
    }

    private void handleRequest( WMSRequestType req, HttpResponseBuffer response, Map<String, String> map,
                                Version version )
                            throws IOException, OWSException {
        try {
            switch ( req ) {
            case GetCapabilities:
            case capabilities:
                if ( isStrict && map.get( "SERVICE" ) == null ) {
                    throw new OWSException( get( "WMS.PARAM_MISSING", "SERVICE" ),
                                            OWSException.INVALID_PARAMETER_VALUE );
                }
                break;
            default:
                if ( isStrict && getVersionValueFromRequest( map ) == null ) {
                    throw new OWSException( get( "WMS.PARAM_MISSING", "VERSION" ),
                                            OWSException.INVALID_PARAMETER_VALUE );
                }
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

        if ( !supportedImageFormats.contains( glg.getFormat() ) ) {
            throw new OWSException( get( "WMS.UNSUPPORTED_IMAGE_FORMAT", glg.getFormat() ), OWSException.INVALID_FORMAT );
        }
        BufferedImage img = service.getLegend( glg );
        sendImage( img, response, glg.getFormat() );
    }

    private void getFeatureInfo( Map<String, String> map, final HttpResponseBuffer response, Version version )
                            throws OWSException, IOException, MissingDimensionValue, InvalidDimensionValue {
        org.deegree.protocol.wms.ops.GetFeatureInfo fi = new org.deegree.protocol.wms.ops.GetFeatureInfo( map, version );
        doGetFeatureInfo( map, response, version, fi );
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
                                                                                           service.getExtensions(),
                                                                                           isStrict );

        doGetMap( map, response, version, gm2 );
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
        if ( !supportedImageFormats.contains( gm.getFormat() ) ) {
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
        doGetCapabilities( map, response, updateSequence, req );
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {
        Version requestVersion = null;
        try {
            String requestName = xmlStream.getLocalName();
            WMSRequestType requestType = detectWmsRequestType( requestName );

            if ( !supportedEncodings.isEncodingSupported( requestType, "XML" ) ) {
                throw new OWSException( "POST/XML is not supported for " + requestName + " requests.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }

            requestVersion = parseAndCheckVersion( xmlStream );

            switch ( requestType ) {
            case GetCapabilities:
                GetCapabilitiesXMLAdapter getCapabilitiesXMLAdapter = new GetCapabilitiesXMLAdapter();
                getCapabilitiesXMLAdapter.setRootElement( new XMLAdapter( xmlStream ).getRootElement() );
                GetCapabilities getCapabilities = getCapabilitiesXMLAdapter.parse( requestVersion );
                String updateSequence = getCapabilities.getUpdateSequence();
                doGetCapabilities( new HashMap<String, String>(), response, updateSequence, getCapabilities );
                break;
            case GetMap:
                GetMapParser getMapParser = new GetMapParser();
                GetMap getMap = getMapParser.parse( xmlStream );
                Map<String, String> map = new HashMap<String, String>();
                doGetMap( map, response, VERSION_130, getMap );
                break;
            case GetFeatureInfo:
                GetFeatureInfoParser getFeatureInfoParser = new GetFeatureInfoParser();
                GetFeatureInfo getFeatureInfo = getFeatureInfoParser.parse( xmlStream );
                Map<String, String> gfiMap = new HashMap<String, String>();
                doGetFeatureInfo( gfiMap, response, VERSION_130, getFeatureInfo );
                break;
            default:
                String msg = "XML request handling is currently not supported for operation " + requestName;
                throw new UnsupportedOperationException( msg );
            }
        } catch ( OWSException e ) {
            LOG.debug( e.getMessage(), e );
            sendServiceException( response, requestVersion, e );
        } catch ( XMLStreamException e ) {
            LOG.debug( e.getMessage(), e );
            OWSException owsException = new OWSException( e.getMessage(), OWSException.NO_APPLICABLE_CODE );
            sendServiceException( response, requestVersion, owsException );
        }
    }

    @Override
    public void doSOAP( org.apache.axiom.soap.SOAPEnvelope soapDoc, HttpServletRequest request,
                        HttpResponseBuffer response, java.util.List<FileItem> multiParts,
                        org.apache.axiom.soap.SOAPFactory factory )
                            throws ServletException, IOException, org.deegree.services.authentication.SecurityException {
        Version requestVersion = null;
        try {

            OMElement body = soapDoc.getBody().getFirstElement().cloneOMElement();
            XMLStreamReader xmlStream = body.getXMLStreamReaderWithoutCaching();
            nextElement( xmlStream );

            String requestName = xmlStream.getLocalName();
            WMSRequestType requestType = detectWmsRequestType( requestName );

            if ( !supportedEncodings.isEncodingSupported( requestType, "SOAP" ) ) {
                throw new OWSException( "POST/SOAP is not supported for " + requestName + " requests.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }
            
            requestVersion = parseAndCheckVersion( xmlStream );

            if ( WMSRequestType.GetMap.equals( requestType ) ) {
                doSoapGetMap( soapDoc.getVersion(), response, xmlStream );
            } else {
                beginSoapResponse( soapDoc, response );
                switch ( requestType ) {
                case GetCapabilities:
                    GetCapabilitiesXMLAdapter getCapabilitiesXMLAdapter = new GetCapabilitiesXMLAdapter();
                    getCapabilitiesXMLAdapter.setRootElement( body );
                    GetCapabilities getCapabilities = getCapabilitiesXMLAdapter.parse( requestVersion );
                    String updateSequence = getCapabilities.getUpdateSequence();
                    doGetCapabilities( new HashMap<String, String>(), response, updateSequence, getCapabilities );
                    break;
                case GetFeatureInfo:
                    GetFeatureInfoParser getFeatureInfoParser = new GetFeatureInfoParser();
                    GetFeatureInfo getFeatureInfo = getFeatureInfoParser.parse( xmlStream );
                    Map<String, String> gfiMap = new HashMap<String, String>();
                    doGetFeatureInfo( gfiMap, response, VERSION_130, getFeatureInfo );
                    break;
                default:
                    String msg = "SOAP request handling is currently not supported for operation " + requestName;
                    throw new UnsupportedOperationException( msg );
                }
                endSOAPResponse( response );
            }

        } catch ( OWSException e ) {
            LOG.debug( e.getMessage(), e );
            sendSOAPException( soapDoc.getHeader(), factory, response, e, null, null, null, request.getServerName(),
                               request.getCharacterEncoding() );
            sendServiceException( response, requestVersion, e );
        } catch ( XMLStreamException e ) {
            LOG.debug( e.getMessage(), e );
            OWSException owsException = new OWSException( e.getMessage(), OWSException.NO_APPLICABLE_CODE );
            sendSOAPException( soapDoc.getHeader(), factory, response, owsException, null, null, null,
                               request.getServerName(), request.getCharacterEncoding() );
        } catch ( SOAPException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendImage( BufferedImage img, HttpResponseBuffer response, String format )
                            throws OWSException, IOException {
        response.setContentType( format );

        ImageSerializer serializer = imageSerializers.get( format );
        if ( serializer != null ) {
            serializer.serialize( null, img, response.getOutputStream() );
            return;
        }

        format = format.substring( format.indexOf( "/" ) + 1 );
        if ( format.equals( "x-ms-bmp" ) ) {
            format = "bmp";
        }
        if ( format.equals( "png; subtype=8bit" ) || format.equals( "png; mode=8bit" ) ) {
            img = ColorQuantizer.quantizeImage( img, 256, false, false );
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

    public CapabilitiesManager getCapabilitiesManager() {
        return capabilitiesManager;
    }

    /**
     * @return the supported encodings configured in the DeegreeWMS, should not be <code>null</code>
     */
    public SupportedEncodings getSupportedEncodings(){
        return supportedEncodings;
    }

    public FeatureInfoManager getFeatureInfoManager() {
        return featureInfoManager;
    }

    public ExceptionsManager getExceptionsManager() {
        return exceptionsManager;
    }

    private void initOfferedVersions( SupportedVersions supportedVersions ) {
        List<String> versions = null;
        if ( supportedVersions != null ) {
            versions = supportedVersions.getVersion();
        }
        if ( versions == null || versions.isEmpty() ) {
            LOG.info( "No protocol versions specified. Activating all implemented versions." );
            ImplementationMetadata<?> md = ( (OWSProvider) getMetadata().getProvider() ).getImplementationMetadata();
            versions = new ArrayList<String>( md.getImplementedVersions().size() );
            for ( Version version : md.getImplementedVersions() ) {
                versions.add( version.toString() );
            }
        }
        validateAndSetOfferedVersions( versions );
    }

    private void doGetCapabilities( Map<String, String> map, HttpResponseBuffer response, String updateSequence,
                                    GetCapabilities req )
                            throws OWSException, IOException {
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

    private void doGetMap( Map<String, String> map, HttpResponseBuffer response, Version version, GetMap gm )
                            throws OWSException, IOException {
        LinkedList<String> headers = doGetMap( gm, map, version, response.getOutputStream() );
        response.setContentType( gm.getFormat() );
        addHeaders( response, headers );
    }

    private LinkedList<String> doGetMap( GetMap getMap, Map<String, String> map, Version version, OutputStream stream )
                            throws OWSException, IOException {
        checkGetMap( version, getMap );

        RenderingInfo info = new RenderingInfo( getMap.getFormat(), getMap.getWidth(), getMap.getHeight(),
                                                getMap.getTransparent(), getMap.getBgColor(), getMap.getBoundingBox(),
                                                getMap.getPixelSize(), map, imageSerializers.get( getMap.getFormat() ) );

        RenderContext ctx = ouputFormatProvider.getRenderers( info, stream );
        LinkedList<String> headers = new LinkedList<String>();
        service.getMap( getMap, headers, ctx );
        ctx.close();

        return headers;
    }

    private void doGetFeatureInfo( Map<String, String> map, final HttpResponseBuffer response, Version version,
                                   org.deegree.protocol.wms.ops.GetFeatureInfo fi )
                            throws OWSException, IOException {
        checkGetFeatureInfo( version, fi );
        ICRS crs = fi.getCoordinateSystem();
        boolean geometries = fi.returnGeometries();
        List<String> queryLayers = map( fi.getQueryLayers(), CollectionUtils.<LayerRef> getToStringMapper() );

        RenderingInfo info = new RenderingInfo( fi.getInfoFormat(), fi.getWidth(), fi.getHeight(), false, null,
                                                fi.getEnvelope(), 0.28, map );
        String format = fi.getInfoFormat();
        info.setFormat( format );
        info.setFeatureCount( fi.getFeatureCount() );
        info.setX( fi.getX() );
        info.setY( fi.getY() );
        LinkedList<String> headers = new LinkedList<String>();
        Pair<FeatureCollection, LinkedList<String>> pair = new Pair<FeatureCollection, LinkedList<String>>(
                                                                                                            service.getFeatures( fi,
                                                                                                                                 headers ),
                                                                                                            headers );

        FeatureCollection col = pair.first;
        addHeaders( response, pair.second );
        format = format == null ? "application/vnd.ogc.gml" : format;
        response.setContentType( format );
        response.setCharacterEncoding( "UTF-8" );

        Map<String, String> fismap = new HashMap<String, String>();
        fismap.put( "LAYERS", reduce( "", queryLayers, getStringJoiner( "," ) ) );
        GetFeatureInfoSchema fis = new GetFeatureInfoSchema( fismap );

        FeatureType type = null;
        Map<String, String> nsBindings = new HashMap<String, String>();
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

    private void sendServiceException( HttpResponseBuffer response, Version requestVersion, OWSException e )
                            throws ServletException {
        ExceptionSerializer serializer = getExceptionSerializer( requestVersion );
        sendException( null, serializer, e, response );
    }

    private void addSupportedImageFormats( DeegreeWMS conf ) {
        if ( conf.getGetMapFormats() != null ) {
            GetMapFormatsType getMapFormats = conf.getGetMapFormats();
            List<String> getMapFormatList = getMapFormats.getGetMapFormat();
            for ( String getMapFormat : getMapFormatList ) {
                supportedImageFormats.add( getMapFormat );
            }

            for ( GetMapFormatsType.CustomGetMapFormat mf : getMapFormats.getCustomGetMapFormat() ) {
                ImageSerializer imageSerializer;
                try {
                    Class<?> clazz = workspace.getModuleClassLoader().loadClass( mf.getJavaClass() );
                    imageSerializer = clazz.asSubclass( ImageSerializer.class ).newInstance();
                } catch ( ClassNotFoundException e ) {
                    throw new IllegalArgumentException( "Couldn't find image serializer class: " + mf.getJavaClass(), e );
                } catch ( Exception e ) {
                    throw new IllegalArgumentException(
                                                        "Configured image serializer class doesn't implement ImageSerializer",
                                                        e );
                }

                for ( GetMapFormatsType.CustomGetMapFormat.Property p : mf.getProperty() ) {
                    try {

                        BeanUtils.setProperty( imageSerializer, p.getName(), p.getValue() );
                    } catch ( Exception e ) {
                        LOG.warn( "Error setting ImageSerializer '{}' property '{}': ", mf.getFormat(), p.getName(),
                                  e.getLocalizedMessage() );
                        LOG.trace( "Exception", e );
                    }
                }
                supportedImageFormats.add( mf.getFormat() );
                imageSerializers.put( mf.getFormat(), imageSerializer );
            }
        }
        if ( supportedImageFormats.isEmpty() ) {
            supportedImageFormats.addAll( ouputFormatProvider.getSupportedOutputFormats() );
        }
    }

    private void addSupportedFeatureInfoFormats( DeegreeWMS conf )
                            throws InstantiationException, IllegalAccessException {
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

                    for ( GetFeatureInfoFormat.Serializer.Property p : t.getSerializer().getProperty() ) {
                        try {
                            BeanUtils.setProperty( featureInfoSerializer, p.getName(), p.getValue() );
                        } catch ( Exception e ) {
                            LOG.warn( "Error setting FeatureInfoSerializer '{}' property '{}': ", t.getFormat(),
                                      p.getName(), e.getLocalizedMessage() );
                            LOG.trace( "Exception", e );
                        }
                    }

                    featureInfoManager.addOrReplaceCustomFormat( t.getFormat(), featureInfoSerializer );
                } else {
                    throw new IllegalArgumentException( "Unknown GetFeatureInfoFormat" );
                }
            }
        }
    }

    private void addSupportedCapabilitiesFormats( DeegreeWMS conf )
                            throws InstantiationException, IllegalAccessException {
        if ( conf.getGetCapabilitiesFormats() != null ) {
            for ( GetCapabilitiesFormat getCapabilitiesFormat : conf.getGetCapabilitiesFormats().getGetCapabilitiesFormat() ) {
                if ( getCapabilitiesFormat.getXSLTFile() != null ) {
                    String format = getCapabilitiesFormat.getFormat();
                    String xsltFile = getCapabilitiesFormat.getXSLTFile();
                    URL xsltUrl = metadata.getLocation().resolveToUrl( xsltFile );
                    capabilitiesManager.addOrReplaceXsltFormat( format, xsltUrl, workspace );
                }
            }
        }
    }

    private void addSupportedExceptionFormats( DeegreeWMS conf )
                            throws InstantiationException, IllegalAccessException {
        if ( conf.getExceptionFormats() != null ) {
            for ( ExceptionFormat exceptionFormat : conf.getExceptionFormats().getExceptionFormat() ) {
                if ( exceptionFormat.getXSLTFile() != null ) {
                    String format = exceptionFormat.getFormat();
                    String xsltFile = exceptionFormat.getXSLTFile();
                    URL xsltUrl = metadata.getLocation().resolveToUrl( xsltFile );
                    exceptionsManager.addOrReplaceXsltFormat( format, xsltUrl, workspace );
                }
            }
        }
    }

    private boolean isAddFeatureInfoDefaultFormatsEnabled( DeegreeWMS jaxbConfig ) {
        FeatureInfoFormatsType featureInfoFormats = jaxbConfig.getFeatureInfoFormats();
        if ( featureInfoFormats != null ) {
            Boolean enableDefaultFormats = featureInfoFormats.isEnableDefaultFormats();
            return enableDefaultFormats == null || enableDefaultFormats;
        }
        return true;
    }

    private boolean isAddCapabilitiesDefaultFormatsEnabled( DeegreeWMS jaxbConfig ) {
        GetCapabilitiesFormatsType capbilitiesFormats = jaxbConfig.getGetCapabilitiesFormats();
        if ( capbilitiesFormats != null ) {
            Boolean enableDefaultFormats = capbilitiesFormats.isEnableDefaultFormats();
            return enableDefaultFormats == null || enableDefaultFormats;
        }
        return true;
    }

    private boolean isAddExceptionsDefaultFormatsEnabled( DeegreeWMS jaxbConfig ) {
        ExceptionFormatsType exceptionsFormats = jaxbConfig.getExceptionFormats();
        if ( exceptionsFormats != null ) {
            Boolean enableDefaultFormats = exceptionsFormats.isEnableDefaultFormats();
            return enableDefaultFormats == null || enableDefaultFormats;
        }
        return true;

    }

    private WMSRequestType detectWmsRequestType( String requestName )
                            throws OWSException {
        for ( WMSRequestType wmsRequestType : WMSRequestType.values() ) {
            if ( wmsRequestType.name().equals( requestName ) )
                return wmsRequestType;
        }
        String msg = "Request type '" + requestName + "' is not supported.";
        throw new OWSException( msg, OWSException.OPERATION_NOT_SUPPORTED, "request" );
    }

    private Version parseAndCheckVersion( XMLStreamReader xmlStream )
                            throws OWSException {
        String version = XMLStreamUtils.getAttributeValue( xmlStream, "version" );
        Version parsedVersion = parseVersion( version );
        return checkVersion( parsedVersion );
    }

    private Version parseVersion( String versionString )
                            throws OWSException {
        if ( versionString != null && !"".equals( versionString ) ) {
            try {
                return Version.parseVersion( versionString );
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE, "version" );
            }
        }
        return null;
    }

    private void doSoapGetMap( SOAPVersion soapVersion, HttpResponseBuffer response, XMLStreamReader xmlStream )
                            throws OWSException, XMLStreamException, IOException, SOAPException {
        response.setContentType( "application/xop+xml" );

        GetMapParser getMapParser = new GetMapParser();
        GetMap getMap = getMapParser.parse( xmlStream );
        Map<String, String> map = new HashMap<String, String>();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        doGetMap( getMap, map, VERSION_130, stream );

        String contentId = UUID.randomUUID().toString();

        SOAPMessage message = createSoapMessage( soapVersion, contentId );

        AttachmentPart attachmentPart = createAttachment( getMap, stream, message, contentId );
        message.addAttachmentPart( attachmentPart );
        message.writeTo( response.getOutputStream() );
    }

    private SOAPMessage createSoapMessage( SOAPVersion soapVersion, String contentId )
                            throws SOAPException {
        String soapProtocol = SOAPConstants.SOAP_1_2_PROTOCOL;
        if ( isSoap11( soapVersion ) )
            soapProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;
        MessageFactory messageFactory = MessageFactory.newInstance( soapProtocol );

        SOAPMessage message = messageFactory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();

        envelope.createName( "response", CommonNamespaces.WMS_PREFIX, CommonNamespaces.WMSNS );
        Name name = envelope.createName( "response", CommonNamespaces.WMS_PREFIX, CommonNamespaces.WMSNS );

        SOAPBodyElement bodyElement = body.addBodyElement( name );
        bodyElement.setTextContent( contentId );
        return message;
    }

    private AttachmentPart createAttachment( GetMap getMap, ByteArrayOutputStream stream, SOAPMessage message,
                                             String contentId ) {
        DataSource ds = new ByteArrayDataSource( stream.toByteArray() );
        DataHandler dataHandler = new DataHandler( ds );
        AttachmentPart attachmentPart = message.createAttachmentPart( dataHandler );
        attachmentPart.setContentId( contentId );
        attachmentPart.setContentType( getMap.getFormat() );
        return attachmentPart;
    }

    private void beginSoapResponse( org.apache.axiom.soap.SOAPEnvelope soapDoc, HttpResponseBuffer response )
                            throws IOException, XMLStreamException {
        if ( isSoap11( soapDoc.getVersion() ) ) {
            beginSoap11Response( response );
        } else {
            beginSOAPResponse( response );
        }
    }

    private boolean isSoap11( SOAPVersion soapVersion ) {
        return soapVersion instanceof SOAP11Version;
    }

    private void beginSoap11Response( HttpResponseBuffer response )
                            throws IOException, XMLStreamException {
        response.setContentType( "text/xml" );
        XMLStreamWriter xmlWriter = response.getXMLWriter();
        String soapEnvNS = "http://schemas.xmlsoap.org/soap/envelope/";
        String xsiNS = "http://www.w3.org/2001/XMLSchema-instance";
        xmlWriter.writeStartElement( "soap", "Envelope", soapEnvNS );
        xmlWriter.writeNamespace( "soap", soapEnvNS );
        xmlWriter.writeNamespace( "xsi", xsiNS );
        xmlWriter.writeAttribute( xsiNS, "schemaLocation",
                                  "http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/" );
        xmlWriter.writeStartElement( soapEnvNS, "Body" );
    }

    private String getVersionValueFromRequest( Map<String, String> map ) {
        String v = map.get( "VERSION" );
        if ( v == null ) {
            v = map.get( "WMTVER" );
        }
        return v;
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
