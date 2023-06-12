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

import static java.util.Collections.EMPTY_LIST;
import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.protocol.wpvs.WPVSConstants.VERSION_100;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.wpvs.WPVSConstants.WPVSRequestType;
import org.deegree.rendering.r3d.opengl.JOGLChecker;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;
import org.deegree.services.jaxb.wpvs.DeegreeWPVS;
import org.deegree.services.jaxb.wpvs.PublishedInformation;
import org.deegree.services.jaxb.wpvs.PublishedInformation.AllowedOperations;
import org.deegree.services.jaxb.wpvs.ServiceConfiguration;
import org.deegree.services.ows.OWS110ExceptionReportSerializer;
import org.deegree.services.wpvs.PerspectiveViewService;
import org.deegree.services.wpvs.controller.capabilities.CapabilitiesXMLAdapter;
import org.deegree.services.wpvs.controller.getview.GetView;
import org.deegree.services.wpvs.controller.getview.GetViewKVPAdapter;
import org.deegree.services.wpvs.controller.getview.GetViewResponseParameters;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
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
 */
public class WPVSController extends AbstractOWS {

	private final static Logger LOG = LoggerFactory.getLogger(WPVSController.class);

	private PerspectiveViewService service;

	private ServiceIdentificationType identification;

	private ServiceProviderType provider;

	private PublishedInformation publishedInformation;

	private List<String> allowedOperations = new LinkedList<String>();

	public WPVSController(ResourceMetadata<OWS> metadata, Workspace workspace, Object jaxbConfig) {
		super(metadata, workspace, jaxbConfig);
	}

	@Override
	public void init(DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConf,
			Object controllerConf) {
		LOG.info("Checking for JOGL.");
		JOGLChecker.check();
		LOG.info("JOGL status check successful.");

		identification = serviceMetadata.getServiceIdentification();
		provider = serviceMetadata.getServiceProvider();

		DeegreeWPVS cfg = (DeegreeWPVS) controllerConf;

		NamespaceBindings nsContext = new NamespaceBindings();
		nsContext.addNamespace("wpvs", "http://www.deegree.org/services/wpvs");
		try {
			publishedInformation = cfg.getPublishedInformation();
			parsePublishedInformation(nsContext, publishedInformation);
			ServiceConfiguration sc = cfg.getServiceConfiguration();
			service = new PerspectiveViewService(metadata.getLocation(), sc, workspace);
		}
		catch (ServiceInitException e) {
			throw new ResourceInitException(e.getMessage(), e);
		}
	}

	/**
	 * @return the view service
	 */
	public PerspectiveViewService getService() {
		return service;
	}

	private void parsePublishedInformation(NamespaceBindings nsContext, PublishedInformation result) {
		if (result != null) {
			// mandatory
			allowedOperations.add(WPVSRequestType.GetCapabilities.name());
			allowedOperations.add(WPVSRequestType.GetView.name());
			AllowedOperations configuredOperations = result.getAllowedOperations();
			if (configuredOperations != null) {
				if (configuredOperations.getGetDescription() != null) {
					LOG.warn(
							"The GetDescription operation was configured, this operation is currently not supported by the WPVS.");
					allowedOperations.add(WPVSRequestType.GetDescription.name());
				}
				if (configuredOperations.getGetLegendGraphic() != null) {
					LOG.warn(
							"The GetLegendGraphic operation was configured, this operation is currently not supported by the WPVS.");
					allowedOperations.add(WPVSRequestType.GetLegendGraphic.name());
				}
			}
		}
	}

	@Override
	public void destroy() {
		// nottin yet
	}

	@Override
	public void doKVP(Map<String, String> normalizedKVPParams, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {
		WPVSRequestType mappedRequest = null;
		String requestName = null;
		try {
			requestName = KVPUtils.getRequired(normalizedKVPParams, "REQUEST");
		}
		catch (MissingParameterException e) {
			sendServiceException(new OWSException(e.getMessage(), OWSException.MISSING_PARAMETER_VALUE), response);
			return;
		}
		mappedRequest = (WPVSRequestType) ((ImplementationMetadata) ((OWSProvider) metadata.getProvider())
			.getImplementationMetadata()).getRequestTypeByName(requestName);

		if (mappedRequest == null) {
			sendServiceException(new OWSException("Unknown request: " + requestName + " is not known to the WPVS.",
					OWSException.OPERATION_NOT_SUPPORTED), response);
			return;
		}
		try {
			LOG.debug("Incoming request was mapped as a: " + mappedRequest);
			switch (mappedRequest) {
				case GetCapabilities:
					sendCapabilities(normalizedKVPParams, request, response);
					break;
				case GetView:
					sendGetViewResponse(normalizedKVPParams, request, response);
					break;
				default:
					sendServiceException(new OWSException(mappedRequest + " is not implemented yet.",
							OWSException.OPERATION_NOT_SUPPORTED), response);
			}
		}
		catch (Throwable t) {
			sendServiceException(
					new OWSException("An exception occurred while processing your request: " + t.getMessage(),
							OWSException.NO_APPLICABLE_CODE),
					response);
		}

	}

	/**
	 * @param normalizedKVPParams
	 * @param request
	 * @param response
	 * @throws ServletException
	 */
	private void sendGetViewResponse(Map<String, String> normalizedKVPParams, HttpServletRequest request,
			HttpResponseBuffer response) throws ServletException {
		try {
			String encoding = (request.getCharacterEncoding() == null) ? "UTF-8" : request.getCharacterEncoding();
			GetView gvReq = GetViewKVPAdapter.create(normalizedKVPParams, encoding, service.getTranslationVector(),
					service.getNearClippingPlane(), service.getFarClippingPlane());

			// first see if the requested image typ is supported
			GetViewResponseParameters responseParameters = gvReq.getResponseParameters();
			String format = responseParameters.getFormat();
			testResultMimeType(format);

			// render the image
			BufferedImage gvResponseImage = service.getImage(gvReq);
			String ioFormat = mimeToFormat(format);
			LOG.debug("Requested format: " + format + " was mapped to response ioformat: " + ioFormat);
			if (gvResponseImage != null) {
				try {
					ImageIO.write(gvResponseImage, ioFormat, response.getOutputStream());
				}
				catch (IOException e) {
					throw new OWSException("An error occurred while writing the result image to the stream because: "
							+ e.getLocalizedMessage(), NO_APPLICABLE_CODE);
				}
				response.setContentLength(response.getBufferSize());
				response.setContentType(format);

			}
		}
		catch (OWSException e) {
			sendServiceException(e, response);
		}

	}

	/**
	 * @param format
	 */
	private String mimeToFormat(String format) {
		String[] split = format.split("/");
		String result = format;
		if (split.length > 1) {
			result = split[split.length - 1];
			split = result.split(";");
			if (split.length >= 1) {
				result = split[0];
			}
		}
		return result;
	}

	/**
	 * Retrieve the imagewriter for the requested format.
	 * @param format mimetype to be supported
	 * @throws OWSException if no writer was found for the given format.
	 */
	private void testResultMimeType(String format) throws OWSException {
		Iterator<ImageWriter> imageWritersByMIMEType = ImageIO.getImageWritersByMIMEType(format);
		ImageWriter writer = null;
		if (imageWritersByMIMEType != null) {
			while (imageWritersByMIMEType.hasNext() && writer == null) {
				ImageWriter iw = imageWritersByMIMEType.next();
				if (iw != null) {
					writer = iw;
				}
			}
		}
		if (writer == null) {
			throw new OWSException("No imagewriter for given image format: " + format,
					OWSException.OPERATION_NOT_SUPPORTED);
		}
	}

	@SuppressWarnings("unchecked")
	private void sendCapabilities(Map<String, String> map, HttpServletRequest request, HttpResponseBuffer response)
			throws IOException {

		GetCapabilities req = GetCapabilitiesKVPParser.parse(map);

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(IS_REPAIRING_NAMESPACES, true);
		try {
			XMLStreamWriter xsw = factory.createXMLStreamWriter(response.getOutputStream(), "UTF-8");
			IndentingXMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(xsw);
			List<Operation> operations = new ArrayList<Operation>();
			List<DCP> dcps = Collections.singletonList(new DCP(new URL(OGCFrontController.getHttpGetURL()), null));
			List<Domain> params = Collections.emptyList();
			List<Domain> constraints = Collections.emptyList();
			for (String operation : allowedOperations) {
				operations.add(new Operation(operation, dcps, params, constraints, EMPTY_LIST));
			}
			OperationsMetadata operationsMd = new OperationsMetadata(operations, params, constraints, null);
			new CapabilitiesXMLAdapter().export040(xmlWriter, req, identification, provider, operationsMd,
					service.getServiceConfiguration());
			xmlWriter.writeEndDocument();
		}
		catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	/**
	 * @param e
	 * @param response
	 */
	private void sendServiceException(OWSException e, HttpResponseBuffer response) throws ServletException {
		LOG.error("Unable to forfil request, sending exception.", e);
		sendException(null, new OWS110ExceptionReportSerializer(VERSION_100), e, response);

	}

	@Override
	public XMLExceptionSerializer getExceptionSerializer(Version requestVersion) {
		return new OWS110ExceptionReportSerializer(VERSION_100);
	}

	@Override
	public void doXML(XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {

		sendServiceException(new OWSException("Currently only Http Get requests with key value pairs are supported.",
				OWSException.OPERATION_NOT_SUPPORTED), response);
	}

}
