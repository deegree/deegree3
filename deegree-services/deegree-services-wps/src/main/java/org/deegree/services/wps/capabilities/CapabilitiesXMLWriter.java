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

package org.deegree.services.wps.capabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.Metadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.wsdl.WSDL;

/**
 * Responsible for the generation of WPS GetCapabilities response documents.
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 */
public class CapabilitiesXMLWriter extends OWSCapabilitiesXMLAdapter {

	private static final String OGC_NS = "http://www.opengis.net/ogc";

	private static final String OGC_PREFIX = "ogc";

	private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

	private static final String OWS_PREFIX = "ows";

	private static final String WPS_NS = "http://www.opengis.net/wps/1.0.0";

	private static final String WPS_PREFIX = "wps";

	private static final String GML_PREFIX = "gml";

	private static final String GML_NS = "http://www.opengis.net/gml";

	private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

	private CapabilitiesXMLWriter() {
		// avoid instantiation
	}

	/**
	 * @param writer
	 * @param processes
	 * @param serviceMetadata
	 * @param serviceWSDLURL location of a WSDL document which describes the entire
	 * service, may be null
	 * @throws XMLStreamException
	 */
	public static void export100(XMLStreamWriter writer, Map<CodeType, WPSProcess> processes,
			DeegreeServicesMetadataType serviceMetadata, WSDL serviceWSDL) throws XMLStreamException {

		writer.writeStartElement(WPS_PREFIX, "Capabilities", WPS_NS);
		writer.writeNamespace(WPS_PREFIX, WPS_NS);
		writer.writeNamespace(OWS_PREFIX, OWS_NS);
		writer.writeNamespace(OGC_PREFIX, OGC_NS);
		writer.writeNamespace(GML_PREFIX, GML_NS);
		writer.writeNamespace("xlink", XLN_NS);
		writer.writeNamespace("xsi", XSI_NS);
		writer.writeAttribute("service", "WPS");
		writer.writeAttribute("version", "1.0.0");
		writer.writeAttribute("xml:lang", "en");
		writer.writeAttribute(XSI_NS, "schemaLocation",
				"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_response.xsd");

		exportServiceIdentification(writer, serviceMetadata.getServiceIdentification());
		exportServiceProvider110(writer, serviceMetadata.getServiceProvider());
		exportOperationsMetadata(writer);

		exportProcessOfferings(writer, processes);
		exportLanguages(writer);

		if (serviceWSDL.exists()) {
			writer.writeStartElement(WPS_NS, "WSDL");
			writer.writeAttribute("xlink:href", serviceWSDL.getRestURL());
			writer.writeEndElement();
		}

		writer.writeEndElement(); // Capabilities
	}

	private static void exportProcessOfferings(XMLStreamWriter writer, Map<CodeType, WPSProcess> processes)
			throws XMLStreamException {

		writer.writeStartElement(WPS_NS, "ProcessOfferings");
		for (WPSProcess process : processes.values()) {
			ProcessDefinition processDef = process.getDescription();
			writer.writeStartElement(WPS_NS, "Process");
			writer.writeAttribute(WPS_NS, "processVersion", processDef.getProcessVersion());

			// "ows:Identifier" (minOccurs="1", maxOccurs="1")
			writer.writeStartElement(OWS_NS, "Identifier");
			if (processDef.getIdentifier().getCodeSpace() != null) {
				writer.writeAttribute("codeSpace", processDef.getIdentifier().getCodeSpace());
			}
			writer.writeCharacters(processDef.getIdentifier().getValue());
			writer.writeEndElement();

			// "ows:Title" (minOccurs="1", maxOccurs="1")
			if (processDef.getTitle() != null) {
				writer.writeStartElement(OWS_NS, "Title");
				if (processDef.getTitle().getLang() != null) {
					writer.writeAttribute("xml:lang", processDef.getTitle().getLang());
				}
				writer.writeCharacters(processDef.getTitle().getValue());
				writer.writeEndElement();
			}

			// "ows:Abstract" (minOccurs="0", maxOccurs="1")
			if (processDef.getAbstract() != null) {
				writer.writeStartElement(OWS_NS, "Abstract");
				if (processDef.getAbstract().getLang() != null) {
					writer.writeAttribute("xml:lang", processDef.getAbstract().getLang());
				}
				writer.writeCharacters(processDef.getAbstract().getValue());
				writer.writeEndElement();
			}

			// "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
			if (processDef.getMetadata() != null) {
				for (Metadata metadata : processDef.getMetadata()) {
					writer.writeStartElement(OWS_NS, "Metadata");
					if (metadata.getAbout() != null) {
						writer.writeAttribute("about", metadata.getAbout());
					}
					if (metadata.getHref() != null) {
						writer.writeAttribute(XLN_NS, "href", metadata.getHref());
					}
					writer.writeEndElement();
				}
			}

			// "wps:Profile" (minOccurs="0", maxOccurs="unbounded")
			if (processDef.getProfile() != null) {
				for (String profile : processDef.getProfile()) {
					writeElement(writer, WPS_NS, "Profile", profile);
				}
			}

			// "wps:WSDL" (minOccurs="0", maxOccurs="unbounded")
			if (processDef.getWSDL() != null) {
				writeElement(writer, WPS_NS, "WSDL", XLN_NS, "href", processDef.getWSDL());
			}

			writer.writeEndElement(); // Process
		}
		writer.writeEndElement(); // ProcessOfferings
	}

	private static void exportOperationsMetadata(XMLStreamWriter writer) throws XMLStreamException {

		List<Operation> operations = new LinkedList<Operation>();

		List<DCP> dcps = null;
		try {
			DCP dcp = new DCP(new URL(OGCFrontController.getHttpGetURL()),
					new URL(OGCFrontController.getHttpPostURL()));
			dcps = Collections.singletonList(dcp);
		}
		catch (MalformedURLException e) {
			// should never happen
		}

		List<Domain> params = new ArrayList<Domain>();
		List<Domain> constraints = new ArrayList<Domain>();
		List<OMElement> mdEls = new ArrayList<OMElement>();

		operations.add(new Operation("GetCapabilities", dcps, params, constraints, mdEls));
		operations.add(new Operation("DescribeProcess", dcps, params, constraints, mdEls));
		operations.add(new Operation("Execute", dcps, params, constraints, mdEls));

		OperationsMetadata operationsMd = new OperationsMetadata(operations, params, constraints, null);

		exportOperationsMetadata110(writer, operationsMd);
	}

	private static void exportServiceIdentification(XMLStreamWriter writer, ServiceIdentificationType ident)
			throws XMLStreamException {
		writer.writeStartElement(OWS_NS, "ServiceIdentification");
		if (ident == null) {
			writeElement(writer, OWS_NS, "Title", "deegree 3 WPS");
			writeElement(writer, OWS_NS, "Abstract", "deegree 3 WPS implementation");
		}
		else {
			List<String> title = ident.getTitle();
			writeElement(writer, OWS_NS, "Title", title.isEmpty() ? "deegree 3 WPS" : title.get(0));
			List<String> _abstract = ident.getAbstract();
			writeElement(writer, OWS_NS, "Abstract",
					_abstract.isEmpty() ? "deegree 3 WPS implementation" : _abstract.get(0));
		}
		writeElement(writer, OWS_NS, "ServiceType", "WPS");
		writeElement(writer, OWS_NS, "ServiceTypeVersion", "1.0.0");
		writer.writeEndElement();
	}

	private static void exportLanguages(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(WPS_NS, "Languages");
		writer.writeStartElement(WPS_NS, "Default");
		writeElement(writer, OWS_NS, "Language", "en");
		writer.writeEndElement(); // Default
		writer.writeStartElement(WPS_NS, "Supported");
		writeElement(writer, OWS_NS, "Language", "en");
		writer.writeEndElement(); // Supported
		writer.writeEndElement(); // Languages
	}

}
