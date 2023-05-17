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

package org.deegree.services.wcs;

import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.protocol.wcs.WCSConstants.VERSION_100;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_NS;
import static org.deegree.services.wcs.WCSProvider.IMPLEMENTATION_METADATA;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.wcs.WCSConstants;
import org.deegree.protocol.wcs.WCSConstants.WCSRequestType;
import org.deegree.protocol.wcs.WCServiceException;
import org.deegree.protocol.wcs.capabilities.GetCapabilities100KVPAdapter;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;
import org.deegree.services.jaxb.wcs.DeegreeWCS;
import org.deegree.services.jaxb.wcs.PublishedInformation;
import org.deegree.services.jaxb.wcs.PublishedInformation.AllowedOperations;
import org.deegree.services.wcs.capabilities.Capabilities100XMLAdapter;
import org.deegree.services.wcs.capabilities.Capabilities100XMLAdapter.Sections;
import org.deegree.services.wcs.capabilities.GetCapabilities100XMLAdapter;
import org.deegree.services.wcs.coverages.WCSCoverage;
import org.deegree.services.wcs.describecoverage.CoverageDescription100XMLAdapter;
import org.deegree.services.wcs.describecoverage.DescribeCoverage;
import org.deegree.services.wcs.describecoverage.DescribeCoverage100KVPAdapter;
import org.deegree.services.wcs.describecoverage.DescribeCoverage100XMLAdapter;
import org.deegree.services.wcs.getcoverage.GetCoverage;
import org.deegree.services.wcs.getcoverage.GetCoverage100KVPAdapter;
import org.deegree.services.wcs.getcoverage.GetCoverage100XMLAdapter;
import org.deegree.services.wcs.model.CoverageOptions;
import org.deegree.services.wcs.model.CoverageResult;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <a href="http://www.opengeospatial.org/standards/wcs">OpenGIS Web
 * Coverage Service</a> server protocol.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class WCSController extends AbstractOWS {

	private static int UPDATE_SEQUENCE = -1;

	private static final String COVERAGE_NOT_DEFINED = "CoverageNotDefined";

	private static final Logger LOG = LoggerFactory.getLogger(WCSController.class);

	private WCService wcsService;

	private List<String> allowedOperations = new LinkedList<String>();

	private ServiceIdentificationType identification;

	private ServiceProviderType provider;

	private DeegreeServiceControllerType mainControllerConf;

	private DeegreeServicesMetadataType mainMetadataConf;

	private static final String CONFIG_PRE = "dwcs";

	private static final String CONFIG_NS = "http://www.deegree.org/services/wcs";

	public WCSController(ResourceMetadata<OWS> metadata, Workspace workspace, Object jaxbConfig) {
		super(metadata, workspace, jaxbConfig);
	}

	@Override
	public void init(DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConf,
			Object controllerConf) {

		LOG.info("Initializing WCS.");
		UPDATE_SEQUENCE++;

		DeegreeWCS cfg = (DeegreeWCS) controllerConf;

		NamespaceBindings nsContext = new NamespaceBindings();
		nsContext.addNamespace(WCSConstants.WCS_100_PRE, WCS_100_NS);
		nsContext.addNamespace(WCSConstants.WCS_110_PRE, WCSConstants.WCS_110_NS);
		nsContext.addNamespace(CONFIG_PRE, CONFIG_NS);

		this.wcsService = new WCServiceBuilder(workspace, getMetadata()).buildService();

		PublishedInformation publishedInformation = cfg.getPublishedInformation();
		parsePublishedInformation(publishedInformation, nsContext);
		syncWithMainController(publishedInformation, serviceMetadata);

		validateAndSetOfferedVersions(publishedInformation.getSupportedVersions().getVersion());
		mainControllerConf = mainConf;
		mainMetadataConf = serviceMetadata;
	}

	/**
	 * sets the identification to the main controller or it will be synchronized with the
	 * maincontroller. sets the provider to the provider of the configured main controller
	 * or it will be synchronized with it's values.
	 * @param publishedInformation
	 */
	private void syncWithMainController(PublishedInformation publishedInformation,
			DeegreeServicesMetadataType mainMetadataConf) {
		identification = mainMetadataConf.getServiceIdentification();
		provider = mainMetadataConf.getServiceProvider();
	}

	@Override
	public void destroy() {
		// *Kaaboooom!*
	}

	private void parsePublishedInformation(PublishedInformation pubInf, NamespaceBindings nsContext)
			throws ResourceInitException {
		if (pubInf != null) {
			// mandatory
			allowedOperations.add(WCSRequestType.GetCapabilities.name());
			AllowedOperations configuredOperations = pubInf.getAllowedOperations();
			if (configuredOperations != null) {
				// if ( configuredOperations.getDescribeCoverage() != null ) {
				// if
				// }
				LOG.info("WCS specification implies support for all three operations.");
			}
			allowedOperations.add(WCSRequestType.DescribeCoverage.name());
			allowedOperations.add(WCSRequestType.GetCoverage.name());
		}
	}

	@Override
	public void doKVP(Map<String, String> param, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {
		try {
			checkRequiredKeys(param);
			WCSRequestType requestType = getRequestType(param);
			LOG.debug("Handling {} request: {}", requestType, param);
			switch (requestType) {
				case GetCoverage:
					GetCoverage coverageReq = GetCoverage100KVPAdapter.parse(param);
					doGetCoverage(coverageReq, response);
					break;
				case GetCapabilities:
					GetCapabilities capabilitiesReq = GetCapabilities100KVPAdapter.parse(param);
					doGetCapabilities(capabilitiesReq, request, response);
					break;
				case DescribeCoverage:
					DescribeCoverage describeReq = DescribeCoverage100KVPAdapter.parse(param);
					doDescribeCoverage(describeReq, response);
					break;
			}

		}
		catch (MissingParameterException e) {
			sendServiceException(new OWSException(e.getLocalizedMessage(), OWSException.MISSING_PARAMETER_VALUE),
					response);
		}
		catch (OWSException ex) {
			sendServiceException(ex, response);
		}
		catch (Throwable e) {
			sendServiceException(new OWSException("an error occured while processing a request", NO_APPLICABLE_CODE),
					response);
		}
	}

	@Override
	public void doXML(XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException {

		try {
			XMLAdapter requestDoc = new XMLAdapter(xmlStream);
			OMElement rootElement = requestDoc.getRootElement();
			String rootName = rootElement.getLocalName();

			switch (IMPLEMENTATION_METADATA.getRequestTypeByName(rootName)) {
				case GetCapabilities:
					GetCapabilities100XMLAdapter capa = new GetCapabilities100XMLAdapter(rootElement);
					doGetCapabilities(capa.parse(), request, response);
					break;
				case DescribeCoverage:
					DescribeCoverage100XMLAdapter describe = new DescribeCoverage100XMLAdapter(rootElement);
					doDescribeCoverage(describe.parse(), response);
					break;
				case GetCoverage:
					GetCoverage100XMLAdapter getCoverage = new GetCoverage100XMLAdapter(rootElement);
					doGetCoverage(getCoverage.parse(), response);
					break;
			}
		}
		catch (OWSException ex) {
			sendServiceException(ex, response);
		}
		catch (Throwable e) {
			sendServiceException(new OWSException("an error occured while processing a request", NO_APPLICABLE_CODE),
					response);
		}
	}

	private void doGetCoverage(GetCoverage coverageReq, HttpResponseBuffer response) throws IOException, OWSException {
		if (wcsService.hasCoverage(coverageReq.getCoverage())) {
			WCSCoverage coverage = wcsService.getCoverage(coverageReq.getCoverage());
			if (coverageReq.getVersion().equals(WCSConstants.VERSION_100)) {
				// do wcs 1.0.0 specific request checking.
				if (coverageReq.getRangeSet() != null && coverage.getRangeSet() != null) {
					checkRangeSet(coverage.getRangeSet(), coverageReq.getRangeSet());
				}
			}
			testIntersectingBBox(coverage, coverageReq);
			checkOutputOptions(coverageReq, coverage.getCoverageOptions());
			response.setContentType("image/" + coverageReq.getOutputFormat());

			CoverageResult result;
			try {
				result = coverage.getCoverageResult(coverageReq.getRequestEnvelope(), coverageReq.getOutputGrid(),
						coverageReq.getOutputFormat(), coverageReq.getInterpolation(), coverageReq.getRangeSet());
			}
			catch (WCServiceException e) {
				throw new OWSException("An error occured while creating the coverage result: " + e.getMessage(),
						NO_APPLICABLE_CODE);
			}
			result.write(response.getOutputStream());

		}
		else {
			throw new OWSException("The coverage " + coverageReq.getCoverage() + " is invalid", COVERAGE_NOT_DEFINED,
					"offering");
		}
	}

	/**
	 * Tests if the requested bbox intersects with the bbox of the coverage, if not, an
	 * exception must be thrown.
	 * @param coverage from the {@link WCService}
	 * @param coverageReq requested.
	 * @throws OWSException
	 */
	private static void testIntersectingBBox(WCSCoverage coverage, GetCoverage coverageReq) throws OWSException {
		Envelope rEnv = coverageReq.getRequestEnvelope();
		if (rEnv != null) {
			ICRS crs = rEnv.getCoordinateSystem();
			boolean intersects = true;
			if (crs == null) {
				// test against the default crs.
				intersects = rEnv.intersects(coverage.getEnvelope());
			}
			else {
				Iterator<Envelope> it = coverage.responseEnvelopes.iterator();
				Envelope defEnv = null;
				while (it.hasNext() && defEnv == null) {
					Envelope e = it.next();
					if (e != null) {
						ICRS eCRS = e.getCoordinateSystem();
						if (crs.equals(eCRS)) {
							defEnv = e;
						}
					}
				}
				if (defEnv == null) {
					defEnv = coverage.getEnvelope();
				}
				intersects = rEnv.intersects(defEnv);
			}
			if (!intersects) {
				throw new OWSException("Given is outside the bbox of the coverage.",
						OWSException.INVALID_PARAMETER_VALUE);
			}
		}

	}

	private static void checkRangeSet(RangeSet configuredRangeSet, RangeSet requestedRangeSet) throws OWSException {
		List<AxisSubset> reqAxis = requestedRangeSet.getAxisDescriptions();
		for (AxisSubset ras : reqAxis) {
			if (ras.getName() != null) {
				boolean hasMatch = false;
				Iterator<AxisSubset> it = configuredRangeSet.getAxisDescriptions().iterator();

				while (it.hasNext() && !hasMatch) {
					AxisSubset as = it.next();
					if (as.getName().equalsIgnoreCase(ras.getName())) {
						boolean match = false;
						try {
							match = ras.match(as, true);
						}
						catch (NumberFormatException e) {
							throw new OWSException("Following rangeset: " + ras.getName()
									+ " has an AxisDescriptions requesting a value which is not valid for the requested coverage",
									OWSException.INVALID_PARAMETER_VALUE);
						}
						if (!match) {
							throw new OWSException("Following rangeset: " + ras.getName()
									+ " has an AxisDescriptions requesting a value which is not valid for the requested coverage",
									OWSException.INVALID_PARAMETER_VALUE);
						}
					}
				}
			}
		}
	}

	private static void checkOutputOptions(GetCoverage request, CoverageOptions options) throws OWSException {
		boolean supported;
		String outputFormat = request.getOutputFormat();
		supported = options.getOutputFormats().contains(outputFormat);

		if (!supported) {
			// check for geotiff
			if (outputFormat == null || !"geotiff".equals(outputFormat.toLowerCase())) {
				throw new OWSException("Unsupported output format (" + outputFormat + ")",
						OWSException.INVALID_PARAMETER_VALUE, "FORMAT");
			}
		}
		String interpolation = request.getInterpolation();
		try {

			supported = options.getInterpolations().contains(InterpolationType.fromString(interpolation));
		}
		catch (Exception e) {
			throw new OWSException("Unsupported interpolation (" + interpolation + ")",
					OWSException.INVALID_PARAMETER_VALUE, "INTERPOLATION");
		}
		String crs = request.getOutputCRS();
		supported = options.getCRSs().contains(crs);
		if (!supported) {
			throw new OWSException("unsupported response crs (" + crs + ")", OWSException.INVALID_PARAMETER_VALUE,
					"RESPONSE CRS");
		}
	}

	private void doDescribeCoverage(DescribeCoverage describeReq, HttpResponseBuffer response)
			throws IOException, XMLStreamException, OWSException {
		response.setContentType("text/xml");
		XMLStreamWriter xmlWriter = getXMLStreamWriter(response.getWriter());
		List<WCSCoverage> coverages = new LinkedList<WCSCoverage>();
		if (describeReq.getCoverages().size() == 0) { // return all
			coverages = wcsService.getAllCoverages();
		}
		else {
			for (String reqCoverage : describeReq.getCoverages()) {
				if (wcsService.hasCoverage(reqCoverage)) {
					coverages.add(wcsService.getCoverage(reqCoverage));
				}
				else {
					throw new OWSException("Unknown coverage " + reqCoverage, COVERAGE_NOT_DEFINED, "coverage");
				}
			}
		}
		CoverageDescription100XMLAdapter.export(xmlWriter, coverages, UPDATE_SEQUENCE);
		xmlWriter.flush();
	}

	private void doGetCapabilities(GetCapabilities request, HttpServletRequest requestWrapper,
			HttpResponseBuffer response) throws IOException, XMLStreamException, OWSException {

		Set<Sections> sections = getSections(request);

		Version negotiateVersion = negotiateVersion(request);
		// if update sequence is given and matches the given update sequence an error
		// should occur
		// http://cite.opengeospatial.org/OGCTestData/wcs/1.0.0/specs/03-065r6.html#7.2.1_Key-value_pair_encoding
		if (negotiateVersion.equals(VERSION_100)) {
			String updateSeq = request.getUpdateSequence();
			int requestedUS = UPDATE_SEQUENCE - 1;
			try {
				requestedUS = Integer.parseInt(updateSeq);
			}
			catch (NumberFormatException e) {
				// nothing to do, just ignore it.
			}
			if (requestedUS == UPDATE_SEQUENCE) {
				throw new OWSException("Update sequence may not be equal than server's current update sequence.",
						WCSConstants.ExeptionCode_1_0_0.CurrentUpdateSequence.name());
			}
			else if (requestedUS > UPDATE_SEQUENCE) {
				throw new OWSException("Update sequence may not be higher than server's current update sequence.",
						WCSConstants.ExeptionCode_1_0_0.InvalidUpdateSequence.name());
			}
		}

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(IS_REPAIRING_NAMESPACES, true);

		response.setContentType("text/xml");
		XMLStreamWriter xmlWriter = getXMLStreamWriter(response.getWriter());
		if (negotiateVersion.equals(VERSION_100)) {
			Capabilities100XMLAdapter.export(xmlWriter, request, identification, provider, allowedOperations, sections,
					wcsService.getAllCoverages(), mainMetadataConf, mainControllerConf, xmlWriter, UPDATE_SEQUENCE);
		}
		else {
			// the 1.1.0
		}
		xmlWriter.writeEndDocument();
		xmlWriter.flush();
	}

	private static Set<Sections> getSections(GetCapabilities capabilitiesReq) {
		Set<String> sections = capabilitiesReq.getSections();
		Set<Sections> result = new HashSet<Sections>();
		if (!(sections.isEmpty() || sections.contains("/"))) {
			final int length = "/WCS_Capabilities/".length();
			for (String section : sections) {
				if (section.startsWith("/WCS_Capabilities/")) {
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

	private void sendServiceException(OWSException ex, HttpResponseBuffer response) throws ServletException {
		sendException(null, new WCS100ServiceExceptionReportSerializer(), ex, response);
	}

	private void checkRequiredKeys(Map<String, String> param) throws OWSException {
		try {
			ImplementationMetadata<?> imd = ((OWSProvider) getMetadata().getProvider()).getImplementationMetadata();

			String service = KVPUtils.getRequired(param, "SERVICE");
			if (!"WCS".equalsIgnoreCase(service)) {
				throw new OWSException("SERVICE " + service + " is not supported", OWSException.INVALID_PARAMETER_VALUE,
						"SERVICE");
			}
			String request = KVPUtils.getRequired(param, "REQUEST");
			if (!imd.getHandledRequests().contains(request)) {
				throw new OWSException("REQUEST " + request + " is not supported", OWSException.OPERATION_NOT_SUPPORTED,
						"REQUEST");
			}
			String version;
			if (imd.getRequestTypeByName(request) != WCSRequestType.GetCapabilities) {
				// no version required
				version = KVPUtils.getRequired(param, "VERSION");
				if (version != null && !offeredVersions.contains(Version.parseVersion(version))) {
					throw new OWSException("VERSION " + version + " is not supported",
							OWSException.VERSION_NEGOTIATION_FAILED, "VERSION");
				}
			}
		}
		catch (MissingParameterException e) {
			throw new OWSException(e.getMessage(), OWSException.MISSING_PARAMETER_VALUE);
		}
	}

	private WCSRequestType getRequestType(Map<String, String> param) throws OWSException {
		try {
			String requestName = KVPUtils.getRequired(param, "REQUEST");
			return WCSProvider.IMPLEMENTATION_METADATA.getRequestTypeByName(requestName);
		}
		catch (MissingParameterException e) {
			throw new OWSException(e.getMessage(), OWSException.MISSING_PARAMETER_VALUE);
		}
	}

	private static XMLStreamWriter getXMLStreamWriter(Writer writer) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		return new IndentingXMLStreamWriter(factory.createXMLStreamWriter(writer));
	}

	@Override
	public XMLExceptionSerializer getExceptionSerializer(Version requestVersion) {
		return new WCS100ServiceExceptionReportSerializer();
	}

}
