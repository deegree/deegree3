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

package org.deegree.services.wpvs.controller;

import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static org.deegree.protocol.wpvs.WPVSConstants.VERSION_040;
import static org.deegree.protocol.wpvs.WPVSConstants.WPVS_NS;
import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.ows.capabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.wpvs.WPVSConstants.WPVSRequestType;
import org.deegree.rendering.r3d.opengl.JOGLChecker;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.ows.capabilities.OWSOperation;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.main.DCPType;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.deegree.services.jaxb.wpvs.PublishedInformation;
import org.deegree.services.jaxb.wpvs.ServiceConfiguration;
import org.deegree.services.jaxb.wpvs.PublishedInformation.AllowedOperations;
import org.deegree.services.wpvs.PerspectiveViewService;
import org.deegree.services.wpvs.controller.capabilities.CapabilitiesXMLAdapter;
import org.deegree.services.wpvs.controller.getview.GetView;
import org.deegree.services.wpvs.controller.getview.GetViewKVPAdapter;
import org.deegree.services.wpvs.controller.getview.GetViewResponseParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the OpenGIS Web Perspective View Service server protocol.
 * <p>
 * Supported WPVS protocol versions:
 * <ul>
 * <li>1.0.0 (inofficial, unreleased)</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPVSController extends AbstractOGCServiceController {

    private final static Logger LOG = LoggerFactory.getLogger( WPVSController.class );

    private PerspectiveViewService service;

    private ServiceIdentificationType identification;

    private ServiceProviderType provider;

    private PublishedInformation publishedInformation;

    private final static String CONFIG_SCHEMA_FILE = "/META-INF/schemas/wpvs/0.5.0/wpvs_service_configuration.xsd";

    private final static String PUBLISHED_SCHEMA_FILE = "/META-INF/schemas/wpvs/0.5.0/wpvs_published_information.xsd";

    private List<String> allowedOperations = new LinkedList<String>();

    private static final ImplementationMetadata<WPVSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WPVSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_040 };
            handledNamespaces = new String[] { WPVS_NS };
            handledRequests = WPVSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.5.0" ) };
        }
    };

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadataType serviceMetadata,
                      DeegreeServiceControllerType mainConf )
                            throws ControllerInitException {

        init( serviceMetadata, mainConf, IMPLEMENTATION_METADATA, controllerConf );

        LOG.info( "Checking for JOGL." );
        JOGLChecker.check();
        LOG.info( "JOGL status check successful." );

        identification = serviceMetadata.getServiceIdentification();
        provider = serviceMetadata.getServiceProvider();

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wpvs", "http://www.deegree.org/services/wpvs" );
        try {
            publishedInformation = parsePublishedInformation( nsContext, controllerConf );
            ServiceConfiguration sc = parseServerConfiguration( nsContext, controllerConf );
            service = new PerspectiveViewService( controllerConf, sc, getServiceWorkspace() );
        } catch ( JAXBException e ) {
            e.printStackTrace();
            throw new ControllerInitException( e.getMessage(), e );
        } catch ( ServiceInitException e ) {
            throw new ControllerInitException( e.getMessage(), e );
        }

    }

    private PublishedInformation parsePublishedInformation( NamespaceContext nsContext, XMLAdapter controllerConf )
                            throws JAXBException {
        Unmarshaller u = getUnmarshaller( "org.deegree.services.jaxb.wpvs", PUBLISHED_SCHEMA_FILE );

        XPath xp = new XPath( "wpvs:PublishedInformation", nsContext );
        OMElement elem = controllerConf.getElement( controllerConf.getRootElement(), xp );
        PublishedInformation result = null;
        if ( elem != null ) {
            result = (PublishedInformation) u.unmarshal( elem.getXMLStreamReaderWithoutCaching() );
            if ( result != null ) {
                // mandatory
                allowedOperations.add( WPVSRequestType.GetCapabilities.name() );
                allowedOperations.add( WPVSRequestType.GetView.name() );
                AllowedOperations configuredOperations = result.getAllowedOperations();
                if ( configuredOperations != null ) {
                    if ( configuredOperations.getGetDescription() != null ) {
                        LOG.warn( "The GetDescription operation was configured, this operation is currently not supported by the WPVS." );
                        allowedOperations.add( WPVSRequestType.GetDescription.name() );
                    }
                    if ( configuredOperations.getGetLegendGraphic() != null ) {
                        LOG.warn( "The GetLegendGraphic operation was configured, this operation is currently not supported by the WPVS." );
                        allowedOperations.add( WPVSRequestType.GetLegendGraphic.name() );
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param nsContext
     * @param controllerConf
     * @throws JAXBException
     */
    private ServiceConfiguration parseServerConfiguration( NamespaceContext nsContext, XMLAdapter controllerConf )
                            throws JAXBException {

        Unmarshaller u = getUnmarshaller( "org.deegree.services.jaxb.wpvs", CONFIG_SCHEMA_FILE );

        XPath xp = new XPath( "wpvs:ServiceConfiguration", nsContext );
        OMElement elem = controllerConf.getRequiredElement( controllerConf.getRootElement(), xp );

        return (ServiceConfiguration) u.unmarshal( elem.getXMLStreamReaderWithoutCaching() );

    }

    @Override
    public void destroy() {
        // nottin yet
    }

    @Override
    public void doKVP( Map<String, String> normalizedKVPParams, HttpServletRequest request,
                       HttpResponseBuffer response, List<FileItem> multiParts )
                            throws ServletException, IOException {
        WPVSRequestType mappedRequest = null;
        String requestName = null;
        try {
            requestName = KVPUtils.getRequired( normalizedKVPParams, "REQUEST" );
        } catch ( MissingParameterException e ) {
            sendServiceException( new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE ), response );
            return;
        }
        mappedRequest = IMPLEMENTATION_METADATA.getRequestTypeByName( requestName );

        if ( mappedRequest == null ) {
            sendServiceException( new OWSException( "Unknown request: " + requestName + " is not known to the WPVS.",
                                                    OWSException.OPERATION_NOT_SUPPORTED ), response );
            return;
        }
        try {
            LOG.debug( "Incoming request was mapped as a: " + mappedRequest );
            switch ( mappedRequest ) {
            case GetCapabilities:
                sendCapabilities( normalizedKVPParams, request, response );
                break;
            case GetView:
                sendGetViewResponse( normalizedKVPParams, request, response );
                break;
            default:
                sendServiceException( new OWSException( mappedRequest + " is not implemented yet.",
                                                        OWSException.OPERATION_NOT_SUPPORTED ), response );
            }
        } catch ( Throwable t ) {
            sendServiceException( new OWSException( "An exception occurred while processing your request: "
                                                    + t.getMessage(), ControllerException.NO_APPLICABLE_CODE ),
                                  response );
        }

    }

    /**
     * @param normalizedKVPParams
     * @param request
     * @param response
     * @throws ServletException
     */
    private void sendGetViewResponse( Map<String, String> normalizedKVPParams, HttpServletRequest request,
                                      HttpResponseBuffer response )
                            throws ServletException {
        try {
            String encoding = ( request.getCharacterEncoding() == null ) ? "UTF-8" : request.getCharacterEncoding();
            GetView gvReq = GetViewKVPAdapter.create( normalizedKVPParams, encoding, service.getTranslationVector(),
                                                      service.getNearClippingPlane(), service.getFarClippingPlane() );

            // first see if the requested image typ is supported
            GetViewResponseParameters responseParameters = gvReq.getResponseParameters();
            String format = responseParameters.getFormat();
            testResultMimeType( format );

            // render the image
            BufferedImage gvResponseImage = service.getImage( gvReq );
            String ioFormat = mimeToFormat( format );
            LOG.debug( "Requested format: " + format + " was mapped to response ioformat: " + ioFormat );
            if ( gvResponseImage != null ) {
                try {
                    ImageIO.write( gvResponseImage, ioFormat, response.getOutputStream() );
                } catch ( IOException e ) {
                    throw new OWSException( "An error occurred while writing the result image to the stream because: "
                                            + e.getLocalizedMessage(), ControllerException.NO_APPLICABLE_CODE );
                }
                response.setContentLength( response.getBufferSize() );
                response.setContentType( format );

            }
        } catch ( OWSException e ) {
            sendServiceException( e, response );
        }

    }

    /**
     * @param format
     */
    private String mimeToFormat( String format ) {
        String[] split = format.split( "/" );
        String result = format;
        if ( split.length > 1 ) {
            result = split[split.length - 1];
            split = result.split( ";" );
            if ( split.length >= 1 ) {
                result = split[0];
            }
        }
        return result;
    }

    /**
     * Retrieve the imagewriter for the requested format.
     * 
     * @param format
     *            mimetype to be supported
     * @throws OWSException
     *             if no writer was found for the given format.
     */
    private void testResultMimeType( String format )
                            throws OWSException {
        Iterator<ImageWriter> imageWritersByMIMEType = ImageIO.getImageWritersByMIMEType( format );
        ImageWriter writer = null;
        if ( imageWritersByMIMEType != null ) {
            while ( imageWritersByMIMEType.hasNext() && writer == null ) {
                ImageWriter iw = imageWritersByMIMEType.next();
                if ( iw != null ) {
                    writer = iw;
                }
            }
        }
        if ( writer == null ) {
            throw new OWSException( "No imagewriter for given image format: " + format,
                                    OWSException.OPERATION_NOT_SUPPORTED );
        }
    }

    private void sendCapabilities( Map<String, String> map, HttpServletRequest request, HttpResponseBuffer response )
                            throws IOException {
        GetCapabilities req = GetCapabilitiesKVPParser.parse( map );

        DCPType wpvsDCP = new DCPType();
        DCPType dcps = mainControllerConf.getDCP();
        String getUrl = request.getRequestURL().toString();
        if ( dcps != null && dcps.getHTTPGet() != null && !"".equals( dcps.getHTTPGet().trim() ) ) {
            getUrl = dcps.getHTTPGet();
        }
        if ( !getUrl.endsWith( "?" ) ) {
            getUrl += "?";
        }
        wpvsDCP.setHTTPGet( getUrl );

        /*
         * post is currently not supported
         */
        // String postUrl = request.getRequestURL().toString();
        // if ( dcps != null && dcps.getHTTPPost() != null && !"".equals( dcps.getHTTPPost().trim() ) ) {
        // postUrl = dcps.getHTTPPost();
        // }
        // wpvsDCP.setHTTPPost( postUrl );
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( IS_REPAIRING_NAMESPACES, true );
        try {
            XMLStreamWriter xsw = factory.createXMLStreamWriter( response.getOutputStream(), "UTF-8" );
            FormattingXMLStreamWriter xmlWriter = new FormattingXMLStreamWriter( xsw );
            List<OWSOperation> operations = new ArrayList<OWSOperation>();
            List<Pair<String, List<String>>> params = new ArrayList<Pair<String, List<String>>>();
            List<Pair<String, List<String>>> constraints = new ArrayList<Pair<String, List<String>>>();
            for ( String operation : allowedOperations ) {
                operations.add( new OWSOperation( operation, wpvsDCP, params, constraints ) );
            }
            new CapabilitiesXMLAdapter().export040( xmlWriter, req, identification, provider, operations, wpvsDCP,
                                                    service.getServiceConfiguration() );
            xmlWriter.writeEndDocument();
        } catch ( XMLStreamException e ) {
            throw new IOException( e );
        }
    }

    /**
     * @param e
     * @param response
     */
    private void sendServiceException( OWSException e, HttpResponseBuffer response )
                            throws ServletException {
        LOG.error( "Unable to forfil request, sending exception.", e );
        sendException( "application/vnd.ogc.se_xml", "UTF-8", null, 200, new OWSException110XMLAdapter(), e, response );

    }

    @Override
    public Pair<XMLExceptionSerializer<OWSException>, String> getExceptionSerializer( Version requestVersion ) {
        return new Pair<XMLExceptionSerializer<OWSException>, String>( new OWSException110XMLAdapter(),
                                                                       "application/vnd.ogc.se_xml" );
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        sendServiceException( new OWSException( "Currently only Http Get requests with key value pairs are supported.",
                                                OWSException.OPERATION_NOT_SUPPORTED ), response );
    }
}
