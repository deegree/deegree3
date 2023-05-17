/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.csw.exporthandling;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.OGC_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_100;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.filter.xml.FilterCapabilitiesExporter;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.csw.profile.EbrimProfile;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;
import org.deegree.services.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class EbrimGetCapabilitiesHandler extends OWSCapabilitiesXMLAdapter implements CapabilitiesHandler {

	private static Logger LOG = LoggerFactory.getLogger(EbrimGetCapabilitiesHandler.class);

	private static final String WRS_NS = "http://www.opengis.net/cat/wrs/1.0";

	private static final String WRS_PREFIX = "wrs";

	private static final String WRS_SCHEMA = "http://schemas.opengis.net/csw/2.0.2/profiles/ebrim/1.0/csw-ebrim.xsd";

	private static final String[] outputFormats = new String[] { "application/xml" };

	private static final String schemaLang = "http://www.w3c.org/2001/XMLSchema";

	private final LinkedList<String> supportedOperations = new LinkedList<String>();

	private final GetCapabilitiesHelper gcHelper = new GetCapabilitiesHelper();

	private final XMLStreamWriter writer;

	private final ServiceProviderType provider;

	private final Set<Sections> sections;

	private final ServiceIdentificationType identification;

	private final Version version;

	@SuppressWarnings("unused")
	private final boolean isTransactionEnabled;

	private URL extendedCapabilities;

	public EbrimGetCapabilitiesHandler(XMLStreamWriter writer, Set<Sections> sections,
			ServiceIdentificationType identification, ServiceProviderType provider, Version version,
			boolean isTransactionEnabled, URL extendedCapabilities) {
		this.writer = writer;
		this.provider = provider;
		this.sections = sections;
		this.identification = identification;
		this.version = version;
		this.isTransactionEnabled = isTransactionEnabled;
		this.extendedCapabilities = extendedCapabilities;

		supportedOperations.add(CSWRequestType.GetCapabilities.name());
		supportedOperations.add(CSWRequestType.DescribeRecord.name());
		supportedOperations.add(CSWRequestType.GetRecords.name());
		supportedOperations.add(CSWRequestType.GetRecordById.name());
		supportedOperations.add(CSWRequestType.GetRepositoryItem.name());
	}

	public void export() throws XMLStreamException {
		if (VERSION_202.equals(version) || VERSION_100.equals(version)) {
			export(writer, sections, identification, provider);
		}
		else {
			throw new InvalidParameterValueException("Supported versions are: '" + VERSION_202 + "'. Version '"
					+ version + "' instead is not supported.");
		}
	}

	private void export(XMLStreamWriter writer, Set<Sections> sections, ServiceIdentificationType identification,
			ServiceProviderType provider) throws XMLStreamException {
		writer.writeStartElement(WRS_PREFIX, "Capabilities", WRS_NS);
		writer.writeNamespace(WRS_PREFIX, WRS_NS);
		writer.writeNamespace(OWS_PREFIX, OWS_NS);
		writer.writeNamespace(OGC_PREFIX, OGCNS);
		writer.writeNamespace(XLINK_PREFIX, XLN_NS);
		writer.writeNamespace(XSINS, XSI_PREFIX);
		writer.writeAttribute("version", "2.0.2");
		writer.writeAttribute("schemaLocation", WRS_NS + " " + WRS_SCHEMA, XSINS);

		// ows:ServiceIdentification
		if (sections.isEmpty() || sections.contains(Sections.ServiceIdentification)) {
			gcHelper.exportServiceIdentification(writer, identification,
					"urn:ogc:serviceType:CatalogueService:2.0.2:HTTP:ebRIM", "2.0.2",
					"http://www.opengeospatial.org/ogcna");
		}
		// ows:ServiceProvider
		if (sections.isEmpty() || sections.contains(Sections.ServiceProvider)) {
			exportServiceProvider100Old(writer, provider);
		}
		// ows:OperationsMetadata
		if (sections.isEmpty() || sections.contains(Sections.OperationsMetadata)) {
			exportOperationsMetadata(writer, OGCFrontController.getHttpGetURL(), OGCFrontController.getHttpPostURL(),
					OWS_NS);
		}
		// mandatory
		FilterCapabilitiesExporter.export110(writer);
		writer.writeEndElement();
		writer.writeEndDocument();
	}

	protected void exportOperationsMetadata(XMLStreamWriter writer, String get, String post, String owsNS)
			throws XMLStreamException {
		writer.writeStartElement(owsNS, "OperationsMetadata");

		for (String name : supportedOperations) {
			if (!name.equals(CSWRequestType.GetRepositoryItem.name())) {
				writer.writeStartElement(owsNS, "Operation");
				writer.writeAttribute("name", name);
				exportDCP(writer, get, post, owsNS);
				if (name.equals(CSWRequestType.GetCapabilities.name())) {
					writeParam(owsNS, "AcceptVersions", "2.0.2", "1.0.0");
					gcHelper.writeGetCapabilitiesParameters(writer, owsNS);
					writer.writeEndElement();// Operation
				}
				else if (name.equals(CSWRequestType.DescribeRecord.name())) {
					writeParam(owsNS, "version", "2.0.2", "1.0.0");
					gcHelper.writeDescribeRecordParameters(writer, owsNS, null, outputFormats, schemaLang);
					writeParam(owsNS, "typeNames", "csw:Record", "rim:RegistryPackage", "rim:ExtrinsicObject",
							"rim:RegistryObject");
					writer.writeEndElement();// Operation
				}
				else if (name.equals(CSWRequestType.GetRecords.name())) {
					writeParam(owsNS, "version", "2.0.2", "1.0.0");
					gcHelper.writeGetRecordsParameters(writer, owsNS,
							new String[] { "csw:Record", "rim:RegistryPackage", "rim:ExtrinsicObject",
									"rim:RegistryObject" },
							outputFormats, new String[] { "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" }, null);
					writeParam(owsNS, "CONSTRAINTLANGUAGE", "FILTER");
					writeParam(owsNS, "ElementSetName", "brief", "summary", "full");
					writer.writeEndElement();// Operation
				}
				else if (name.equals(CSWRequestType.GetRecordById.name())) {
					writeParam(owsNS, "version", "2.0.2", "1.0.0");
					gcHelper.writeGetRecordByIdParameters(writer, owsNS, outputFormats,
							new String[] { EbrimProfile.RIM_NS });
					writeParam(owsNS, "TypeName", "csw:Record", "rim:RegistryPackage", "rim:ExtrinsicObject",
							"rim:RegistryObject");
					writer.writeEndElement();// Operation
					// } else if ( name.equals(
					// CSWebRIMRequestType.GetRepositoryItem.name() ) ) {
					// writeParam( owsNS, "version", "2.0.2", "1.0.0" );
					// writer.writeEndElement();// Operation
				}
			}
		}
		writeParam(owsNS, "service", EbrimProfile.SERVICENAME_CSW, EbrimProfile.SERVICENAME_CSW_EBRIM,
				EbrimProfile.SERVICENAME_WRS);
		// if XML and/or SOAP is supported
		writer.writeStartElement(owsNS, "Constraint");
		writer.writeAttribute("name", "PostEncoding");

		writer.writeStartElement(owsNS, "Value");
		writer.writeCharacters("XML");
		writer.writeEndElement();// Value
		writer.writeStartElement(owsNS, "Value");
		writer.writeCharacters("SOAP");
		writer.writeEndElement();// Value

		writer.writeEndElement();// Constraint

		writer.writeStartElement(owsNS, "Constraint");
		writer.writeAttribute("name", "srsName");
		writer.writeStartElement(owsNS, "Value");
		writer.writeCharacters("urn:ogc:def:crs:EPSG:4326");
		writer.writeEndElement();// Value
		writer.writeStartElement(owsNS, "Metadata");
		writer.writeAttribute(CommonNamespaces.XLNNS, "type", "simple");
		writer.writeAttribute(CommonNamespaces.XLNNS, "title", "EPSG geodetic parameters");
		writer.writeAttribute(CommonNamespaces.XLNNS, "href", "http://www.epsg-registry.org/");
		writer.writeEndElement();// Metadata
		writer.writeEndElement();// Constraint

		if (extendedCapabilities != null) {
			InputStream extCapabilites = null;
			try {
				extCapabilites = extendedCapabilities.openStream();
				gcHelper.exportExtendedCapabilities(writer, owsNS, extCapabilites, null);
			}
			catch (IOException e) {
				LOG.warn("Could not open stream for extended capabilities. Ignore it!");
			}
			finally {
				IOUtils.closeQuietly(extCapabilites);
			}
		}

		writer.writeEndElement();// OperationsMetadata
	}

	private void writeParam(String owsNS, String paramName, String... values) throws XMLStreamException {
		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", paramName);
		for (String value : values) {
			gcHelper.writeValue(writer, owsNS, value);
		}
		writer.writeEndElement();// Parameter
	}

}
