/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.csw;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.MISSING_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.authentication.soapauthentication.FailedAuthentication;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.capabilities.GetCapabilities202KVPAdapter;
import org.deegree.services.csw.capabilities.GetCapabilitiesVersionXMLAdapter;
import org.deegree.services.csw.describerecord.DescribeRecord;
import org.deegree.services.csw.describerecord.DescribeRecordKVPAdapter;
import org.deegree.services.csw.describerecord.DescribeRecordXMLAdapter;
import org.deegree.services.csw.exporthandling.CapabilitiesHandler;
import org.deegree.services.csw.exporthandling.DescribeRecordHandler;
import org.deegree.services.csw.exporthandling.GetRecordsHandler;
import org.deegree.services.csw.exporthandling.TransactionHandler;
import org.deegree.services.csw.getrecordbyid.DefaultGetRecordByIdHandler;
import org.deegree.services.csw.getrecordbyid.GetRecordById;
import org.deegree.services.csw.getrecordbyid.GetRecordByIdHandler;
import org.deegree.services.csw.getrecordbyid.GetRecordByIdKVPAdapter;
import org.deegree.services.csw.getrecordbyid.GetRecordByIdXMLAdapter;
import org.deegree.services.csw.getrecords.ConfiguredElementName;
import org.deegree.services.csw.getrecords.GetRecords;
import org.deegree.services.csw.getrecords.GetRecordsKVPAdapter;
import org.deegree.services.csw.getrecords.GetRecordsXMLAdapter;
import org.deegree.services.csw.getrepositoryitem.GetRepositoryItem;
import org.deegree.services.csw.getrepositoryitem.GetRepositoryItemHandler;
import org.deegree.services.csw.getrepositoryitem.GetRepositoryItemKVPAdapter;
import org.deegree.services.csw.profile.ServiceProfile;
import org.deegree.services.csw.profile.ServiceProfileManager;
import org.deegree.services.csw.transaction.Transaction;
import org.deegree.services.csw.transaction.TransactionKVPAdapter;
import org.deegree.services.csw.transaction.TransactionXMLAdapter;
import org.deegree.services.i18n.Messages;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.csw.DeegreeCSW;
import org.deegree.services.jaxb.csw.ElementName;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the
 * <a href="http://www.opengeospatial.org/standards/specifications/catalog">OpenGIS
 * Catalogue Service</a> server protocol.
 * <p>
 * Supported CSW protocol versions:
 * <ul>
 * <li>2.0.2</li>
 * </ul>
 * </p>
 *
 * @see CSWService
 * @see AbstractOWS
 * @see OGCFrontController
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class CSWController extends AbstractOWS {

	private static final Logger LOG = LoggerFactory.getLogger(CSWController.class);

	private static final String SCHEMA_LOCATION = CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA + " " + GMD_NS + " "
			+ "http://schemas.opengis.net/iso/19139/20070417/gmd/gmd.xsd";

	private MetadataStore<?> store;

	private boolean enableTransactions;

	private boolean enableInspireExtensions;

	private DescribeRecordHandler describeRecordHandler;

	private GetRecordsHandler getRecordsHandler;

	private TransactionHandler transactionHandler;

	private GetRecordByIdHandler getRecordByIdHandler;

	private ServiceProfile profile;

	private URL extendedCapabilities;

	private DeegreeServicesMetadataType mainMetadataConf;

	private DeegreeServiceControllerType mainControllerConf;

	protected CSWController(ResourceMetadata<OWS> metadata, Workspace workspace, Object jaxbConfig) {
		super(metadata, workspace, jaxbConfig);
	}

	@Override
	public void init(DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConf,
			Object controllerConf) {
		this.mainMetadataConf = serviceMetadata;
		this.mainControllerConf = mainConf;
		LOG.info("Initializing CSW controller.");
		DeegreeCSW jaxbConfig = (DeegreeCSW) controllerConf;

		String metadataStoreId = jaxbConfig.getMetadataStoreId();
		if (metadataStoreId == null) {
			LOG.info("Metadata store id is not configured. Initializing/looking up configured record stores.");
			List<ResourceIdentifier<MetadataStore<? extends MetadataRecord>>> stores = workspace
				.getResourcesOfType(MetadataStoreProvider.class);
			List<MetadataStore<? extends MetadataRecord>> availableStores = new ArrayList<MetadataStore<?>>();
			for (ResourceIdentifier<MetadataStore<? extends MetadataRecord>> id : stores) {
				MetadataStore<? extends MetadataRecord> str = workspace.getResource(id.getProvider(), id.getId());
				if (str != null) {
					availableStores.add(str);
				}
			}
			if (availableStores.size() == 0)
				throw new IllegalArgumentException(
						"There is no MetadataStore configured, ensure that exactly one store is available!");
			if (availableStores.size() > 1)
				throw new IllegalArgumentException("-----Number of MetadataStores must be one: configured are "
						+ availableStores.size() + " stores!");
			store = availableStores.get(0);
		}
		else {
			store = workspace.getResource(MetadataStoreProvider.class, metadataStoreId);
		}
		profile = ServiceProfileManager.createProfile(store);

		if (jaxbConfig.getSupportedVersions() == null) {
			validateAndSetOfferedVersions(profile.getSupportedVersions());
			LOG.info("No SupportedVersion element provided. The version is set to default 2.0.2");
		}
		else {
			validateAndSetOfferedVersions(jaxbConfig.getSupportedVersions().getVersion());
		}

		enableTransactions = jaxbConfig.isEnableTransactions() == null ? false : jaxbConfig.isEnableTransactions();
		if (enableTransactions) {
			LOG.info("Transactions are enabled.");
		}
		else {
			LOG.info("Transactions are disabled!");
		}
		// TODO: enableInspireExtensions
		if (jaxbConfig.getEnableInspireExtensions() != null) {
			enableInspireExtensions = true;
			LOG.info("Inspire is activated");
		}
		else {
			enableInspireExtensions = false;
			LOG.info("Inspire extensions are deactivated");
		}
		if (jaxbConfig.getExtendedCapabilities() != null) {
			extendedCapabilities = metadata.getLocation().resolveToUrl(jaxbConfig.getExtendedCapabilities());
			if (extendedCapabilities == null) {
				LOG.warn("Could not resolve path to extended capabilities : " + extendedCapabilities
						+ ". Ignore extended capabilities.");
			}
		}
		int maxMatches = jaxbConfig.getMaxMatches() == null ? 0 : jaxbConfig.getMaxMatches().intValue();

		Map<QName, ConfiguredElementName> elNames = new HashMap<QName, ConfiguredElementName>();
		if (jaxbConfig.getElementNames() != null) {
			List<ElementName> elementNames = jaxbConfig.getElementNames().getElementName();
			for (ElementName en : elementNames) {
				QName qName = new QName(en.getName().getNamespace(), en.getName().getValue());
				elNames.put(qName, new ConfiguredElementName(qName, en.getXPath()));
			}
		}

		describeRecordHandler = new DescribeRecordHandler();
		getRecordsHandler = new GetRecordsHandler(maxMatches, SCHEMA_LOCATION, store, elNames);
		transactionHandler = new TransactionHandler();
		getRecordByIdHandler = getGetRecordByIdHandler();
	}

	private GetRecordByIdHandler getGetRecordByIdHandler() {
		ServiceLoader<GetRecordByIdHandler> serviceLoader = ServiceLoader.load(GetRecordByIdHandler.class);
		Iterator<GetRecordByIdHandler> iterator = serviceLoader.iterator();
		if (iterator.hasNext())
			return iterator.next();
		return new DefaultGetRecordByIdHandler();
	}

	@Override
	public void doKVP(Map<String, String> normalizedKVPParams, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {
		try {
			String rootElement = KVPUtils.getRequired(normalizedKVPParams, "REQUEST");
			CSWRequestType requestType = getRequestType(rootElement);
			if (requestType == null)
				throw new OWSException(rootElement + " is not a known request type by this CSW",
						INVALID_PARAMETER_VALUE);

			Version requestVersion = getVersion(normalizedKVPParams.get("ACCEPTVERSIONS"));

			String serviceAttr = KVPUtils.getRequired(normalizedKVPParams, "SERVICE");
			if (!ArrayUtils.contains(profile.getSupportedServiceNames(), serviceAttr)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Wrong service attribute: '").append(serviceAttr);
				sb.append("' -- must be ");
				String[] serviceNames = profile.getSupportedServiceNames();
				for (int i = 0; i < serviceNames.length; i++) {
					sb.append(serviceNames[i]);
					if (i < serviceNames.length - 1) {
						sb.append(", ");
					}
				}
				sb.append('.');
				throw new OWSException(sb.toString(), OWSException.INVALID_PARAMETER_VALUE, "service");
			}

			if (requestType != CSWRequestType.GetCapabilities) {
				checkVersion(requestVersion);
			}
			boolean supportedOperation = profile.supportsOperation(requestType);
			if (!supportedOperation) {
				throw new OWSException(requestType + " is not supported by this CSW service ", INVALID_PARAMETER_VALUE);
			}
			switch (requestType) {
				case GetCapabilities:
					GetCapabilities getCapabilities = GetCapabilities202KVPAdapter.parse(normalizedKVPParams);
					doGetCapabilities(getCapabilities, response, false);
					break;
				case DescribeRecord:
					DescribeRecord descRec = DescribeRecordKVPAdapter.parse(normalizedKVPParams);
					describeRecordHandler.doDescribeRecord(descRec, response, profile);
					break;

				case GetRecords:
					GetRecords getRec = GetRecordsKVPAdapter.parse(normalizedKVPParams, "application/xml",
							"http://www.opengis.net/cat/csw/2.0.2");
					getRecordsHandler.doGetRecords(getRec, response, store);
					break;
				case GetRecordById:
					GetRecordById getRecBI = GetRecordByIdKVPAdapter.parse(normalizedKVPParams, "application/xml",
							"http://www.opengis.net/cat/csw/2.0.2");
					getRecordByIdHandler.doGetRecordById(getRecBI, response, store, profile);
					break;
				case Transaction:
					checkTransactionsEnabled(rootElement);
					Transaction trans = TransactionKVPAdapter.parse(normalizedKVPParams);
					transactionHandler.doTransaction(trans, response, store);
					break;
				case GetRepositoryItem:
					GetRepositoryItem getRepItem = GetRepositoryItemKVPAdapter.parse(normalizedKVPParams);
					new GetRepositoryItemHandler().doGetRepositoryItem(getRepItem, response);
					break;
			}

		}
		catch (OWSException e) {
			LOG.debug(e.getMessage(), e);
			sendServiceException(e, response);
		}
		catch (InvalidParameterValueException e) {
			LOG.debug(e.getMessage(), e);
			sendServiceException(new OWSException(e), response);
		}
		catch (MissingParameterException e) {
			LOG.debug(e.getMessage(), e);
			sendServiceException(new OWSException(e), response);
		}
		catch (Throwable t) {
			String msg = "An unexpected error occured: " + t.getMessage();
			LOG.debug(msg, t);
			sendServiceException(new OWSException(msg, t, NO_APPLICABLE_CODE), response);
		}
	}

	@Override
	public void doXML(XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException, SecurityException {

		response.setContentType("text/xml");

		try {
			XMLAdapter requestDoc = new XMLAdapter(xmlStream);
			OMElement rootElement = requestDoc.getRootElement();

			doXML(rootElement, response);
		}
		catch (OWSException e) {
			LOG.debug(e.getMessage(), e);
			sendServiceException(e, response);
		}
		catch (InvalidParameterValueException e) {
			LOG.debug(e.getMessage(), e);
			sendServiceException(new OWSException(e), response);
		}
		catch (MissingParameterException e) {
			LOG.debug(e.getMessage(), e);
			sendServiceException(new OWSException(e), response);
		}
		catch (Throwable t) {
			String msg = "An unexpected error occured: " + t.getMessage();
			LOG.error(msg, t);
			sendServiceException(new OWSException(msg, t, NO_APPLICABLE_CODE), response);
		}
	}

	@Override
	public void doSOAP(SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts, SOAPFactory factory) throws OMException, ServletException {
		SOAPVersion version = soapDoc.getVersion();
		try {
			if (version instanceof SOAP11Version) {
				response.setContentType("application/soap+xml");
				XMLStreamWriter xmlWriter = response.getXMLWriter();
				String soapEnvNS = "http://schemas.xmlsoap.org/soap/envelope/";
				String xsiNS = "http://www.w3.org/2001/XMLSchema-instance";
				xmlWriter.writeStartElement("soap", "Envelope", soapEnvNS);
				xmlWriter.writeNamespace("soap", soapEnvNS);
				xmlWriter.writeNamespace("xsi", xsiNS);
				xmlWriter.writeAttribute(xsiNS, "schemaLocation",
						"http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/");
				xmlWriter.writeStartElement(soapEnvNS, "Body");
			}
			else {
				beginSOAPResponse(response);
			}
			doXML(soapDoc.getBody().getFirstElement(), response);
		}
		catch (XMLStreamException e) {
			LOG.debug(e.getMessage(), e);
			sendSoapException(soapDoc, factory, response, new OWSException(e.getMessage(), e, NO_APPLICABLE_CODE),
					request, version);
		}
		catch (OWSException e) {
			LOG.debug(e.getMessage(), e);
			sendSoapException(soapDoc, factory, response, e, request, version);
		}
		catch (IOException e) {
			LOG.debug(e.getMessage(), e);
			sendSoapException(soapDoc, factory, response, new OWSException(e.getMessage(), e, NO_APPLICABLE_CODE),
					request, version);
		}
		catch (MissingParameterException e) {
			LOG.debug(e.getMessage(), e);
			sendSoapException(soapDoc, factory, response, new OWSException(e.getMessage(), e, MISSING_PARAMETER_VALUE),
					request, version);
		}
		catch (InvalidParameterValueException e) {
			LOG.debug(e.getMessage(), e);
			sendSoapException(soapDoc, factory, response, new OWSException(e.getMessage(), e, INVALID_PARAMETER_VALUE),
					request, version);
		}
		catch (FailedAuthentication e) {
			LOG.debug(e.getMessage(), e);
			sendSoapException(soapDoc, factory, response, new OWSException(e.getMessage(), e, NO_APPLICABLE_CODE),
					request, version);
		}
		catch (Throwable t) {
			String msg = "An unexpected error occured: " + t.getMessage();
			LOG.debug(msg, t);
			sendSoapException(soapDoc, factory, response, new OWSException(msg, t, NO_APPLICABLE_CODE), request,
					version);
		}
	}

	private void doXML(OMElement requestElement, HttpResponseBuffer response)
			throws OWSException, XMLStreamException, IOException {
		String rootElement = requestElement.getLocalName();
		CSWRequestType requestType = getRequestType(rootElement);
		if (requestType == null)
			throw new IllegalArgumentException(rootElement + " is not a known request type by this CSW");

		// check if requested version is supported and offered (except for
		// GetCapabilities)
		Version requestVersion = getVersion(requestElement.getAttributeValue(new QName("version")));
		if (requestType != CSWRequestType.GetCapabilities) {
			checkVersion(requestVersion);
		}

		requestVersion = profile.checkVersion(requestVersion);

		boolean supportedOperation = profile.supportsOperation(requestType);
		if (!supportedOperation) {
			throw new OWSException(requestType + " is not supported by this CSW service ", INVALID_PARAMETER_VALUE);
		}

		switch (requestType) {
			case GetCapabilities:
				GetCapabilitiesVersionXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesVersionXMLAdapter();
				getCapabilitiesAdapter.setRootElement(requestElement);
				GetCapabilities cswRequest = getCapabilitiesAdapter.parse();
				doGetCapabilities(cswRequest, response, true);
				break;
			case DescribeRecord:
				DescribeRecordXMLAdapter describeRecordAdapter = new DescribeRecordXMLAdapter();
				describeRecordAdapter.setRootElement(requestElement);
				DescribeRecord cswDRRequest = describeRecordAdapter.parse(requestVersion);
				describeRecordHandler.doDescribeRecord(cswDRRequest, response, profile);
				break;
			case GetRecords:
				GetRecordsXMLAdapter getRecordsAdapter = new GetRecordsXMLAdapter();
				getRecordsAdapter.setRootElement(requestElement);
				GetRecords cswGRRequest = getRecordsAdapter.parse(requestVersion, "application/xml",
						"http://www.opengis.net/cat/csw/2.0.2");
				getRecordsHandler.doGetRecords(cswGRRequest, response, store);
				break;
			case GetRecordById:
				GetRecordByIdXMLAdapter getRecordByIdAdapter = new GetRecordByIdXMLAdapter();
				getRecordByIdAdapter.setRootElement(requestElement);
				GetRecordById cswGRBIRequest = getRecordByIdAdapter.parse(requestVersion, "application/xml",
						"http://www.opengis.net/cat/csw/2.0.2");
				getRecordByIdHandler.doGetRecordById(cswGRBIRequest, response, store, profile);
				break;
			case Transaction:
				checkTransactionsEnabled(rootElement);
				TransactionXMLAdapter transAdapter = new TransactionXMLAdapter();
				transAdapter.setRootElement(requestElement);
				Transaction cswTRequest = transAdapter.parse(requestVersion);
				transactionHandler.doTransaction(cswTRequest, response, store);
				break;
			default:
				throw new OWSException(requestType + " as SOAP request is not supported by this CSW service yet",
						INVALID_PARAMETER_VALUE);
		}
	}

	@Override
	public void destroy() {
		LOG.debug("destroy");
	}

	private void checkTransactionsEnabled(String requestName) throws OWSException {
		if (!enableTransactions) {
			throw new OWSException(Messages.get("CSW_TRANSACTIONS_DISABLED", requestName),
					OWSException.OPERATION_NOT_SUPPORTED);
		}
	}

	/**
	 * Method for mapping the request operation to the implemented operations located in
	 * {@link CSWConstants}
	 * @param requestName
	 * @return CSWRequestType
	 * @throws OWSException
	 */
	private CSWRequestType getRequestType(String requestName) throws OWSException {
		CSWRequestType requestType = null;
		try {
			requestType = (CSWRequestType) ((ImplementationMetadata) ((OWSProvider) getMetadata().getProvider())
				.getImplementationMetadata()).getRequestTypeByName(requestName);
		}
		catch (IllegalArgumentException e) {
			throw new OWSException(e.getMessage(), OWSException.OPERATION_NOT_SUPPORTED);
		}
		return requestType;
	}

	/**
	 * Exports the correct recognized request.
	 * @param getCapabilitiesRequest
	 * @param requestWrapper
	 * @param response
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws OWSException
	 */
	private void doGetCapabilities(GetCapabilities getCapabilitiesRequest, HttpResponseBuffer response, boolean isSoap)
			throws XMLStreamException, IOException, OWSException {
		Set<Sections> sections = getSections(getCapabilitiesRequest);
		Version negotiatedVersion = null;
		if (getCapabilitiesRequest.getAcceptVersions() == null) {
			negotiatedVersion = new Version(2, 0, 2);
		}
		else {
			negotiatedVersion = negotiateVersion(getCapabilitiesRequest);
		}
		response.setContentType(profile.getAcceptFormat(getCapabilitiesRequest));
		XMLStreamWriter xmlWriter = getXMLResponseWriter(response, null);
		CapabilitiesHandler gce = profile.getCapabilitiesHandler(xmlWriter, mainMetadataConf, mainControllerConf,
				sections, mainMetadataConf.getServiceIdentification(), negotiatedVersion, enableTransactions,
				enableInspireExtensions, mainMetadataConf.getServiceProvider(), extendedCapabilities);
		gce.export();
		xmlWriter.flush();

	}

	/**
	 * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
	 * @param writer writer to write the XML to, must not be null
	 * @param schemaLocation allows to specify a value for the 'xsi:schemaLocation'
	 * attribute in the root element, must not be null
	 * @return {@link XMLStreamWriter}
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	static XMLStreamWriter getXMLResponseWriter(HttpResponseBuffer writer, String schemaLocation)
			throws XMLStreamException, IOException {

		if (schemaLocation == null) {
			return writer.getXMLWriter();
		}
		return new SchemaLocationXMLStreamWriter(writer.getXMLWriter(), schemaLocation);
	}

	private void sendServiceException(OWSException ex, HttpResponseBuffer response) throws ServletException {
		sendException(null, getExceptionSerializer(parseVersion("1.2.0")), ex, response);
	}

	/**
	 * Gets the sections described in the GetCapabilities operation.
	 * @param capabilitiesReq
	 * @return a set of type sections
	 */
	private Set<Sections> getSections(GetCapabilities capabilitiesReq) {
		Set<String> sections = capabilitiesReq.getSections();
		Set<Sections> result = new HashSet<Sections>();
		if (!(sections.isEmpty() || sections.contains("/"))) {
			final int length = "/CSW_Capabilities/".length();
			for (String section : sections) {
				if (section.startsWith("/CSW_Capabilities/")) {
					section = section.substring(length);
				}
				try {
					result.add(Sections.valueOf(section));
				}
				catch (IllegalArgumentException ex) {
					// unknown section name
					// the spec does not say what to do, so we ignore it
				}
			}
		}
		return result;
	}

	/**
	 * Parses the version.
	 * @param versionString that should be parsed
	 * @return {@link Version}
	 * @throws OWSException
	 */
	private Version getVersion(String versionString) throws OWSException {
		Version version = null;
		if (versionString != null) {
			try {
				version = Version.parseVersion(versionString);
			}
			catch (InvalidParameterValueException e) {
				throw new OWSException(e);
			}
		}
		return version;
	}

	@Override
	public XMLExceptionSerializer getExceptionSerializer(Version requestVersion) {
		return new CswExceptionReportSerializer(Version.parseVersion("1.2.0"));
	}

	private void sendSoapException(SOAPEnvelope soapDoc, SOAPFactory factory, HttpResponseBuffer response,
			OWSException e, ServletRequest request, SOAPVersion version) throws OMException, ServletException {
		XMLExceptionSerializer serializer = getExceptionSerializer(parseVersion("1.2.0"));
		sendSOAPException(soapDoc.getHeader(), factory, response, e, serializer, null, null, request.getServerName(),
				request.getCharacterEncoding());
	}

	public MetadataStore<?> getStore() {
		return store;
	}

}
