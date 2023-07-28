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

package org.deegree.services.wps;

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.protocol.wps.WPSConstants.VERSION_100;
import static org.deegree.services.controller.OGCFrontController.getHttpGetURL;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.RequestUtils;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.wps.WPSConstants.WPSRequestType;
import org.deegree.protocol.wps.capabilities.GetCapabilitiesXMLAdapter;
import org.deegree.protocol.wps.describeprocess.DescribeProcessRequest;
import org.deegree.protocol.wps.describeprocess.DescribeProcessRequestKVPAdapter;
import org.deegree.protocol.wps.describeprocess.DescribeProcessRequestXMLAdapter;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.wps.DeegreeWPS;
import org.deegree.services.jaxb.wps.DefaultExecutionManager;
import org.deegree.services.ows.OWS110ExceptionReportSerializer;
import org.deegree.services.wps.capabilities.CapabilitiesXMLWriter;
import org.deegree.services.wps.describeprocess.DescribeProcessResponseXMLAdapter;
import org.deegree.services.wps.execute.ExecuteRequest;
import org.deegree.services.wps.execute.ExecuteRequestKVPAdapter;
import org.deegree.services.wps.execute.ExecuteRequestXMLAdapter;
import org.deegree.services.wps.execute.RawDataOutput;
import org.deegree.services.wps.execute.ResponseDocument;
import org.deegree.services.wps.execute.ResponseForm;
import org.deegree.services.wps.storage.OutputStorage;
import org.deegree.services.wps.storage.ResponseDocumentStorage;
import org.deegree.services.wps.storage.StorageManager;
import org.deegree.services.wps.wsdl.WSDL;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <a href="http://www.opengeospatial.org/standards/wps">OpenGIS Web
 * Processing Service</a> server protocol.
 * <p>
 * Supported WPS protocol versions:
 * <ul>
 * <li>1.0.0</li>
 * </ul>
 * </p>
 *
 * @see OGCFrontController
 * @see ProcessManager
 * @see ExecutionManager
 * @author <a href="mailto:padberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class WPService extends AbstractOWS {

	private static final Logger LOG = LoggerFactory.getLogger(WPService.class);

	private static final CodeType ALL_PROCESSES_IDENTIFIER = new CodeType("ALL");

	private ProcessManager processManager;

	private StorageManager storageManager;

	private ExecutionManager executeHandler;

	private DeegreeServicesMetadataType mainMetadataConf;

	public WPService(ResourceMetadata<OWS> metadata, Workspace workspace, Object jaxbConfig) {
		super(metadata, workspace, jaxbConfig);
	}

	@Override
	public void init(DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConf,
			Object controllerConf) {
		LOG.info("Initializing WPS.");
		DeegreeWPS sc = (DeegreeWPS) controllerConf;

		String storage = "../var/wps";
		int trackedExecutions = 100;
		int inputDiskSwitchLimit = 1024 * 1024;
		if (sc.getAbstractExecutionManager() != null) {
			LOG.info("Explicit ExecutionManager config.");
			DefaultExecutionManager execManagerConfig = (DefaultExecutionManager) sc.getAbstractExecutionManager()
				.getValue();
			if (execManagerConfig.getStorageDir() != null && !execManagerConfig.getStorageDir().isEmpty()) {
				storage = execManagerConfig.getStorageDir();
			}
			if (execManagerConfig.getTrackedExecutions() != null) {
				trackedExecutions = execManagerConfig.getTrackedExecutions().intValue();
			}
			if (execManagerConfig.getInputDiskSwitchLimit() != null) {
				inputDiskSwitchLimit = execManagerConfig.getInputDiskSwitchLimit().intValue();
			}
		}

		File storageDir = null;
		try {
			File base = metadata.getLocation().resolveToFile(".");
			storageDir = new File(storage);
			if (!storageDir.isAbsolute()) {
				storageDir = new File(base, storage).getCanonicalFile();
			}
			FileUtils.forceMkdir(storageDir);
		}
		catch (Throwable t) {
			String msg = "Storage directory error: " + t.getMessage();
			throw new ResourceInitException(msg);
		}
		storageManager = new StorageManager(storageDir, inputDiskSwitchLimit);

		this.processManager = workspace.getResourceManager(ProcessManager.class);

		validateAndSetOfferedVersions(sc.getSupportedVersions().getVersion());

		executeHandler = new ExecutionManager(this, storageManager, trackedExecutions);
		mainMetadataConf = serviceMetadata;
	}

	@Override
	public void destroy() {
		// rest should be done by workspace
	}

	@Override
	public void doKVP(Map<String, String> kvpParamsUC, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {

		LOG.trace("doKVP invoked, version: " + kvpParamsUC.get("VERSION"));

		RequestUtils.getCurrentThreadRequestParameters().set(kvpParamsUC);
		try {
			String requestName = KVPUtils.getRequired(kvpParamsUC, "REQUEST");
			WPSRequestType requestType = getRequestTypeByName(requestName);

			// check if requested version is supported and offered (except for
			// GetCapabilities)
			if (requestType != WPSRequestType.GetCapabilities) {
				checkVersion(getVersion(KVPUtils.getRequired(kvpParamsUC, "VERSION")));
			}

			switch (requestType) {
				case GetCapabilities:
					GetCapabilities getCapabilitiesRequest = GetCapabilitiesKVPParser.parse(kvpParamsUC);
					doGetCapabilities(getCapabilitiesRequest, response);
					break;
				case DescribeProcess:
					DescribeProcessRequest describeProcessRequest = DescribeProcessRequestKVPAdapter
						.parse100(kvpParamsUC);
					doDescribeProcess(describeProcessRequest, response);
					break;
				case Execute:
					ExecuteRequest executeRequest = ExecuteRequestKVPAdapter.parse100(kvpParamsUC,
							processManager.getProcesses());
					doExecute(executeRequest, response);
					break;
				case GetOutput:
					doGetOutput(kvpParamsUC.get("IDENTIFIER"), response);
					break;
				case GetResponseDocument:
					doGetResponseDocument(kvpParamsUC.get("IDENTIFIER"), response);
					break;
			}
		}
		catch (MissingParameterException e) {
			sendServiceException(new OWSException(e.getMessage(), OWSException.MISSING_PARAMETER_VALUE), response);
		}
		catch (OWSException e) {
			sendServiceException(e, response);
		}
		catch (XMLStreamException e) {
			LOG.debug(e.getMessage(), e);
		}
		catch (UnknownCRSException e) {
			LOG.debug(e.getMessage(), e);
		}
		finally {
			RequestUtils.getCurrentThreadRequestParameters().remove();
		}
	}

	@Override
	public void doXML(XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {

		LOG.trace("doXML invoked");

		RequestUtils.getCurrentThreadRequestParameters().set(getVendorSpecificParameters(request));
		try {
			WPSRequestType requestType = getRequestTypeByName(xmlStream.getLocalName());

			// check if requested version is supported and offered (except for
			// GetCapabilities)
			Version requestVersion = getVersion(xmlStream.getAttributeValue(null, "version"));
			if (requestType != WPSRequestType.GetCapabilities) {
				checkVersion(requestVersion);
			}

			switch (requestType) {
				case GetCapabilities:
					GetCapabilitiesXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesXMLAdapter();
					getCapabilitiesAdapter.load(xmlStream);
					GetCapabilities getCapabilitiesRequest = getCapabilitiesAdapter.parse100();
					doGetCapabilities(getCapabilitiesRequest, response);
					break;
				case DescribeProcess:
					DescribeProcessRequestXMLAdapter describeProcessAdapter = new DescribeProcessRequestXMLAdapter();
					describeProcessAdapter.load(xmlStream);
					DescribeProcessRequest describeProcessRequest = describeProcessAdapter.parse100();
					doDescribeProcess(describeProcessRequest, response);
					break;
				case Execute:
					// TODO switch to StaX-based parsing
					ExecuteRequestXMLAdapter executeAdapter = new ExecuteRequestXMLAdapter(
							processManager.getProcesses(), storageManager);
					executeAdapter.load(xmlStream);
					ExecuteRequest executeRequest = executeAdapter.parse100();
					doExecute(executeRequest, response);
					break;
				case GetOutput:
				case GetResponseDocument:
					String msg = "Request type '" + requestType.name() + "' is only support as KVP request.";
					throw new OWSException(msg, OWSException.OPERATION_NOT_SUPPORTED);
			}
		}
		catch (OWSException e) {
			sendServiceException(e, response);
		}
		catch (XMLStreamException e) {
			LOG.debug(e.getMessage(), e);
		}
		catch (UnknownCRSException e) {
			LOG.debug(e.getMessage());
		}
		finally {
			RequestUtils.getCurrentThreadRequestParameters().remove();
		}
	}

	@Override
	public void doSOAP(SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts, SOAPFactory factory) throws ServletException, IOException {

		LOG.trace("doSOAP invoked");
		RequestUtils.getCurrentThreadRequestParameters().set(getVendorSpecificParameters(request));
		OMElement requestElement = soapDoc.getBody().getFirstElement();
		try {
			WPSRequestType requestType = getRequestTypeByName(requestElement.getLocalName());

			// check if requested version is supported and offered (except for
			// GetCapabilities)
			Version requestVersion = getVersion(requestElement.getAttributeValue(new QName("version")));
			if (requestType != WPSRequestType.GetCapabilities) {
				checkVersion(requestVersion);
			}

			beginSOAPResponse(response);

			switch (requestType) {
				case GetCapabilities:
					GetCapabilitiesXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesXMLAdapter();
					getCapabilitiesAdapter.setRootElement(requestElement);
					// getCapabilitiesAdapter.setSystemId( soapDoc.getSystemId() );
					GetCapabilities getCapabilitiesRequest = getCapabilitiesAdapter.parse100();
					doGetCapabilities(getCapabilitiesRequest, response);
					break;
				case DescribeProcess:
					DescribeProcessRequestXMLAdapter describeProcessAdapter = new DescribeProcessRequestXMLAdapter();
					describeProcessAdapter.setRootElement(requestElement);
					// describeProcessAdapter.setSystemId( soapDoc.getSystemId() );
					DescribeProcessRequest describeProcessRequest = describeProcessAdapter.parse100();
					doDescribeProcess(describeProcessRequest, response);
					break;
				case Execute:
					// TODO switch to StaX-based parsing
					ExecuteRequestXMLAdapter executeAdapter = new ExecuteRequestXMLAdapter(
							processManager.getProcesses(), storageManager);
					executeAdapter.setRootElement(requestElement);
					// executeAdapter.setSystemId( soapDoc.getSystemId() );
					ExecuteRequest executeRequest = executeAdapter.parse100();
					ResponseForm responseForm = executeRequest.getResponseForm();
					if (responseForm != null && responseForm instanceof RawDataOutput) {
						String msg = "Response type RawDataOutput is not supported for SOAP requests.";
						throw new OWSException(msg, OWSException.OPTION_NOT_SUPPORTED);
					}

					doExecute(executeRequest, response);
					break;
				case GetOutput:
				case GetResponseDocument:
					String msg = "Request type '" + requestType.name() + "' is only support as KVP request.";
					throw new OWSException(msg, OWSException.OPERATION_NOT_SUPPORTED);
			}

			endSOAPResponse(response);

		}
		catch (OWSException e) {
			sendSOAPException(soapDoc.getHeader(), factory, response, e, null, null, null, request.getServerName(),
					request.getCharacterEncoding());
		}
		catch (XMLStreamException e) {
			LOG.debug(e.getMessage(), e);
		}
		catch (UnknownCRSException e) {
			LOG.debug(e.getMessage(), e);
		}
		finally {
			RequestUtils.getCurrentThreadRequestParameters().remove();
		}
	}

	/**
	 * Returns the underlying {@link ProcessManager} instance.
	 * @return the underlying {@link ProcessManager}, never <code>null</code>
	 */
	public ProcessManager getProcessManager() {
		return processManager;
	}

	/**
	 * Returns the associated {@link ExecutionManager} instance.
	 * @return the associated {@link ExecutionManager}, never <code>null</code>
	 */
	public ExecutionManager getExecutionManager() {
		return executeHandler;
	}

	@Override
	public XMLExceptionSerializer getExceptionSerializer(Version requestVersion) {
		return new OWS110ExceptionReportSerializer(VERSION_100);
	}

	private WPSRequestType getRequestTypeByName(String requestName) throws OWSException {
		WPSRequestType requestType = null;
		try {
			requestType = (WPSRequestType) ((ImplementationMetadata) ((OWSProvider) metadata.getProvider())
				.getImplementationMetadata()).getRequestTypeByName(requestName);
		}
		catch (IllegalArgumentException e) {
			throw new OWSException(e.getMessage(), OPERATION_NOT_SUPPORTED);
		}
		return requestType;
	}

	private Version getVersion(String versionString) throws OWSException {

		Version version = null;
		if (versionString != null) {
			try {
				version = Version.parseVersion(versionString);
			}
			catch (IllegalArgumentException e) {
				throw new OWSException(
						"Specified request version '" + versionString + "' is not a valid OGC version string.",
						OWSException.INVALID_PARAMETER_VALUE);
			}
		}
		return version;
	}

	private void doGetCapabilities(GetCapabilities request, HttpResponseBuffer response)
			throws OWSException, XMLStreamException, IOException {

		LOG.trace("doGetCapabilities invoked, request: " + request);

		// generic check if requested version is supported (currently this is only 1.0.0)
		negotiateVersion(request);

		response.setContentType("text/xml; charset=UTF-8");
		XMLStreamWriter xmlWriter = response.getXMLWriter();
		WSDL serviceWSDL = new WSDL("services" + File.separatorChar + "wps.wsdl");
		CapabilitiesXMLWriter.export100(xmlWriter, processManager.getProcesses(), mainMetadataConf, serviceWSDL);

		LOG.trace("doGetCapabilities finished");
	}

	private void doDescribeProcess(DescribeProcessRequest request, HttpResponseBuffer response) throws OWSException {

		LOG.trace("doDescribeProcess invoked, request: " + request);

		// check that all requested processes exist (and resolve special value 'ALL')
		List<WPSProcess> processes = new ArrayList<WPSProcess>();
		for (CodeType identifier : request.getIdentifiers()) {
			LOG.debug("Looking up process '" + identifier + "'");
			if (ALL_PROCESSES_IDENTIFIER.equals(identifier)) {
				processes.addAll(processManager.getProcesses().values());
				break;
			}
			WPSProcess process = processManager.getProcess(identifier);
			if (process != null) {
				processes.add(process);
			}
			else {
				throw new OWSException("InvalidParameterValue: Identifier\nNo process with id " + identifier
						+ " is registered in the WPS.", OWSException.INVALID_PARAMETER_VALUE);
			}
		}

		try {
			response.setContentType("text/xml; charset=UTF-8");
			XMLStreamWriter xmlWriter = response.getXMLWriter();

			Map<ProcessDefinition, String> processDefToWSDLUrl = new HashMap<ProcessDefinition, String>();
			for (WPSProcess process : processes) {
				ProcessDefinition processDef = process.getDescription();
				CodeType processId = new CodeType(processDef.getIdentifier().getValue(),
						processDef.getIdentifier().getCodeSpace());
				// TODO WSDL
				// if ( processIdToWSDL.containsKey( processId ) ) {
				// String wsdlURL = OGCFrontController.getHttpGetURL()
				// + "service=WPS&version=1.0.0&request=GetWPSWSDL&identifier=" +
				// processId.getCode();
				// processDefToWSDLUrl.put( processDef, wsdlURL );
				// }
			}

			DescribeProcessResponseXMLAdapter.export100(xmlWriter, processes, processDefToWSDLUrl);
			xmlWriter.flush();
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
			LOG.error("Internal error: " + e.getMessage());
			throw new OWSException("Error occured while creating response for DescribeProcess operation",
					NO_APPLICABLE_CODE);
		}
		catch (IOException e) {
			throw new OWSException("Error occured while creating response for DescribeProcess operation",
					NO_APPLICABLE_CODE);
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.error("Internal error: " + e.getMessage());
		}

		LOG.trace("doDescribeProcess finished");
	}

	private void doExecute(ExecuteRequest request, HttpResponseBuffer response) throws OWSException {

		LOG.trace("doExecute invoked, request: " + request.toString());
		long start = System.currentTimeMillis();

		CodeType processId = request.getProcessId();
		WPSProcess process = processManager.getProcess(processId);
		if (process == null) {
			String msg = "Internal error. Process '" + processId + "' not found.";
			throw new OWSException(msg, OWSException.INVALID_PARAMETER_VALUE);
		}

		try {
			if (request.getResponseForm() == null || request.getResponseForm() instanceof ResponseDocument) {
				executeHandler.handleResponseDocumentOutput(request, response, process);
			}
			else {
				executeHandler.handleRawDataOutput(request, response, process);
			}
		}
		catch (OWSException e) {
			throw e;
		}
		catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw new OWSException(e.getMessage(), NO_APPLICABLE_CODE);
		}

		long elapsed = System.currentTimeMillis() - start;
		LOG.debug("doExecute took " + elapsed + " milliseconds");

		LOG.trace("doExecute finished");
	}

	private void doGetOutput(String storedOutputId, HttpResponseBuffer response) {

		LOG.trace("doGetOutput invoked, requested stored output: " + storedOutputId);
		OutputStorage resource = storageManager.lookupOutputStorage(storedOutputId);

		if (resource == null) {
			try {
				response.sendError(404, "No stored output with id '" + storedOutputId + "' found.");
			}
			catch (IOException e) {
				LOG.debug("Error sending exception report to client.", e);
			}
		}
		else {
			resource.sendResource(response);
		}

		LOG.trace("doGetOutput finished");
	}

	private void doGetResponseDocument(String responseId, HttpResponseBuffer response) {

		LOG.trace("doGetResponseDocument invoked, requested stored response document: " + responseId);
		ResponseDocumentStorage resource = storageManager.lookupResponseDocumentStorage(responseId, getHttpGetURL());
		executeHandler.sendResponseDocument(response, resource);

		LOG.trace("doGetResponseDocument finished");
	}

	private void sendServiceException(OWSException ex, HttpResponseBuffer response) throws ServletException {
		sendException(null, new OWS110ExceptionReportSerializer(VERSION_100), ex, response);
	}

	private Map<String, String> getVendorSpecificParameters(HttpServletRequest request) {
		final String queryString = request.getQueryString();
		final String encoding = request.getCharacterEncoding() != null ? request.getCharacterEncoding() : "UTF-8";
		if (queryString == null) {
			return Collections.emptyMap();
		}

		try {
			return KVPUtils.getNormalizedKVPMap(queryString, encoding);
		}
		catch (UnsupportedEncodingException uee) {
			LOG.warn("Request parameter encoding '{}' not supported: {}", encoding, uee.getMessage());
			return Collections.emptyMap();
		}
	}

}
