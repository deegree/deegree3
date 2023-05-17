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

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.XMLAdapter.writeElement;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.services.jaxb.metadata.KeywordsType;
import org.deegree.services.jaxb.metadata.LanguageStringType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;

/**
 * An abstract Handler to write GetCapabilities documents
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class GetCapabilitiesHelper {

	private static final LinkedList<String> parameterValues = new LinkedList<String>();

	static {
		parameterValues.add("ServiceIdentification");
		parameterValues.add("ServiceProvider");
		parameterValues.add("OperationsMetadata");
		parameterValues.add("Filter_Capabilities");
	}

	public void exportServiceIdentification(XMLStreamWriter writer, ServiceIdentificationType identification,
			String serviceType, String serviceTypeVersion, String serviceTypeCodeSpace) throws XMLStreamException {
		writer.writeStartElement("http://www.opengis.net/ows", Sections.ServiceIdentification.toString());

		for (String oneTitle : identification.getTitle()) {
			writeElement(writer, "http://www.opengis.net/ows", "Title", oneTitle);
		}

		for (String oneAbstract : identification.getAbstract()) {
			writeElement(writer, "http://www.opengis.net/ows", "Abstract", oneAbstract);
		}

		// keywords [0,n]
		exportKeywords(writer, identification.getKeywords());
		if (serviceTypeCodeSpace != null) {
			writeElement(writer, "http://www.opengis.net/ows", "ServiceType", serviceType, null, null, "codeSpace",
					serviceTypeCodeSpace);
		}
		else {
			writeElement(writer, "http://www.opengis.net/ows", "ServiceType", serviceType);
		}
		writeElement(writer, "http://www.opengis.net/ows", "ServiceTypeVersion", serviceTypeVersion);

		// fees [1]
		String fees = identification.getFees();
		if (fees == null || fees.length() == 0) {
			identification.setFees("NONE");
		}
		fees = identification.getFees();
		// fees = fees.replaceAll( "\\W", " " );
		writeElement(writer, "http://www.opengis.net/ows", "Fees", fees);

		// accessConstraints [0,n]
		exportAccessConstraints(writer, identification);

		writer.writeEndElement();
	}

	/**
	 * Writes the parameter and attributes for the mandatory DescribeRecord operation to
	 * the output.
	 * @param writer to write the output
	 * @param owsNS the OWS namespace
	 * @throws XMLStreamException
	 */
	public void writeDescribeRecordParameters(XMLStreamWriter writer, String owsNS, String[] typeNames,
			String[] outputFormats, String schemaLanguage) throws XMLStreamException {
		if (typeNames != null && typeNames.length > 0) {
			writer.writeStartElement(owsNS, "Parameter");
			writer.writeAttribute("name", "typeName");
			for (String typeName : typeNames) {
				writeValue(writer, owsNS, typeName);
			}
			writer.writeEndElement();// Parameter
		}

		writeOutputFormat(writer, owsNS, outputFormats);

		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", "schemaLanguage");
		writeValue(writer, owsNS, schemaLanguage);
		writer.writeEndElement();// Parameter
	}

	private void writeOutputFormat(XMLStreamWriter writer, String owsNS, String[] outputFormats)
			throws XMLStreamException {
		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", "outputFormat");
		for (String outputFormat : outputFormats) {
			writeValue(writer, owsNS, outputFormat);
		}
		writer.writeEndElement();// Parameter
	}

	/**
	 * Writes the parameter and attributes for the mandatory GetCapabilities operation to
	 * the output.
	 * @param writer to write the output
	 * @param owsNS the OWS namespace
	 * @throws XMLStreamException
	 */
	public void writeGetCapabilitiesParameters(XMLStreamWriter writer, String owsNS) throws XMLStreamException {

		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", "sections");

		for (String value : parameterValues) {
			writer.writeStartElement(owsNS, "Value");
			writer.writeCharacters(value);
			writer.writeEndElement();// Value
		}
		writer.writeEndElement();// Parameter

		// Constraints...
	}

	/**
	 * Writes the parameter and attributes for the mandatory GetRecords operation to the
	 * output.
	 * @param writer
	 * @param owsNS
	 * @param typeNames the typeNames to write
	 * @param outputFormats the outputFormats to write
	 * @param outputSchemas the outputSchemas to write
	 * @param elementSetNames the elementSetNames to write, if null this parameter will be
	 * ignored, can be <code>null</code>
	 * @throws XMLStreamException
	 */
	public void writeGetRecordsParameters(XMLStreamWriter writer, String owsNS, String[] typeNames,
			String[] outputFormats, String[] outputSchemas, String[] elementSetNames) throws XMLStreamException {

		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", "typeNames");
		for (String typeName : typeNames) {
			writeValue(writer, owsNS, typeName);
		}
		writer.writeEndElement();// Parameter

		writeOutputFormat(writer, owsNS, outputFormats);

		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", "outputSchema");
		for (String outputSchema : outputSchemas) {
			writeValue(writer, owsNS, outputSchema);
		}
		writer.writeEndElement();// Parameter

		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", "resultType");
		writeValue(writer, owsNS, "hits");
		writeValue(writer, owsNS, "results");
		writeValue(writer, owsNS, "validate");
		writer.writeEndElement();// Parameter

		if (elementSetNames != null && elementSetNames.length > 0) {
			writer.writeStartElement(owsNS, "Parameter");
			writer.writeAttribute("name", "ElementSetName");
			for (String elementSetName : elementSetNames) {
				writeValue(writer, owsNS, elementSetName);
			}
			writer.writeEndElement();// Parameter
		}
	}

	/**
	 * Writes the parameter and attributes for the mandatory GetRecordById operation to
	 * the output.<br>
	 * In this case the optional transaction operation uses this writing to the output, as
	 * well.
	 * @param writer
	 * @param owsNS
	 * @param outputFormats the outpurFormats to write
	 * @param outputSchemas the outputSChemas to write
	 * @throws XMLStreamException
	 */
	public void writeGetRecordByIdParameters(XMLStreamWriter writer, String owsNS, String[] outputFormats,
			String[] outputSchemas) throws XMLStreamException {
		writeOutputFormat(writer, owsNS, outputFormats);

		writer.writeStartElement(owsNS, "Parameter");
		writer.writeAttribute("name", "outputSchema");
		for (String outputSchema : outputSchemas) {
			writeValue(writer, owsNS, outputSchema);
		}
		writer.writeEndElement(); // Parameter
	}

	/**
	 * write a list of keywords in csw 2.0.2 style.
	 * @param writer
	 * @param keywords
	 * @throws XMLStreamException
	 */
	private void exportKeywords(XMLStreamWriter writer, List<KeywordsType> keywords) throws XMLStreamException {
		if (!keywords.isEmpty()) {
			for (KeywordsType kwt : keywords) {
				if (kwt != null) {
					writer.writeStartElement("http://www.opengis.net/ows", "Keywords");
					List<LanguageStringType> keyword = kwt.getKeyword();
					for (LanguageStringType lst : keyword) {
						if (lst != null) {
							writeElement(writer, "http://www.opengis.net/ows", "Keyword", lst.getValue());
						}
						// -> keyword [1, n]
					}
					// -> type [0,1]
					// exportCodeType( writer, kwt.getType() );
					writer.writeEndElement();// WCS_100_NS, "keywords" );
				}
			}
		}
	}

	private void exportAccessConstraints(XMLStreamWriter writer, ServiceIdentificationType identification)
			throws XMLStreamException {
		List<String> accessConstraints = identification.getAccessConstraints();
		if (accessConstraints.isEmpty()) {
			accessConstraints.add("NONE");
		}
		else {
			for (String ac : accessConstraints) {
				if (!ac.isEmpty()) {
					writeElement(writer, "http://www.opengis.net/ows", "AccessConstraints", ac);
				}
			}
		}
	}

	/**
	 * Writes an empty element 'Value' in the given namespace and with the given value
	 * @param writer
	 * @param ns the namespaceURI
	 * @param value the value to write
	 * @throws XMLStreamException
	 */
	public void writeValue(XMLStreamWriter writer, String ns, String value) throws XMLStreamException {
		XMLAdapter.writeElement(writer, ns, "Value", value);
	}

	/**
	 * @param writer the writer to write the extedned capabilities, never
	 * <code>null</code>
	 * @param owsNS the namespaceURI of the ExtendedCapabilities element
	 * @param extendedCapabilities the inputStream containing the extended capabilites, if
	 * <code>null</code> nothing is exported
	 * @param varToValue an optional list of key value pairs replaced in the extended
	 * capabilities, may be <code>null</code>
	 * @throws XMLStreamException
	 */
	void exportExtendedCapabilities(XMLStreamWriter writer, String owsNS, InputStream extendedCapabilities,
			Map<String, String> varToValue) throws XMLStreamException {
		if (extendedCapabilities != null) {
			if (varToValue == null)
				varToValue = Collections.emptyMap();
			writer.writeStartElement(owsNS, "ExtendedCapabilities");
			try {
				XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(extendedCapabilities);
				reader.nextTag();
				writeTemplateElement(writer, reader, varToValue);
				writer.writeEndElement();
			}
			finally {
				IOUtils.closeQuietly(extendedCapabilities);
			}
		}
	}

	private void writeTemplateElement(XMLStreamWriter writer, XMLStreamReader inStream, Map<String, String> varToValue)
			throws XMLStreamException {

		if (inStream.getEventType() != XMLStreamConstants.START_ELEMENT) {
			throw new XMLStreamException("Input stream does not point to a START_ELEMENT event.");
		}
		int openElements = 0;
		boolean firstRun = true;
		while (firstRun || openElements > 0) {
			firstRun = false;
			int eventType = inStream.getEventType();

			switch (eventType) {
				case CDATA: {
					writer.writeCData(inStream.getText());
					break;
				}
				case CHARACTERS: {
					String s = new String(inStream.getTextCharacters(), inStream.getTextStart(),
							inStream.getTextLength());
					// TODO optimize
					for (String param : varToValue.keySet()) {
						String value = varToValue.get(param);
						s = s.replace(param, value);
					}
					writer.writeCharacters(s);

					break;
				}
				case END_ELEMENT: {
					writer.writeEndElement();
					openElements--;
					break;
				}
				case START_ELEMENT: {
					if (inStream.getNamespaceURI() == "" || inStream.getPrefix() == DEFAULT_NS_PREFIX
							|| inStream.getPrefix() == null) {
						writer.writeStartElement(inStream.getLocalName());
					}
					else {
						if (writer.getNamespaceContext().getPrefix(inStream.getPrefix()) == "") {
							// TODO handle special cases for prefix binding, see
							// http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
							writer.setPrefix(inStream.getPrefix(), inStream.getNamespaceURI());
						}
						writer.writeStartElement(inStream.getPrefix(), inStream.getLocalName(),
								inStream.getNamespaceURI());
					}
					// copy all namespace bindings
					for (int i = 0; i < inStream.getNamespaceCount(); i++) {
						String nsPrefix = inStream.getNamespacePrefix(i);
						String nsURI = inStream.getNamespaceURI(i);
						writer.writeNamespace(nsPrefix, nsURI);
					}

					// copy all attributes
					for (int i = 0; i < inStream.getAttributeCount(); i++) {
						String localName = inStream.getAttributeLocalName(i);
						String nsPrefix = inStream.getAttributePrefix(i);
						String value = inStream.getAttributeValue(i);
						String nsURI = inStream.getAttributeNamespace(i);
						if (nsURI == null) {
							writer.writeAttribute(localName, value);
						}
						else {
							writer.writeAttribute(nsPrefix, nsURI, localName, value);
						}
					}

					openElements++;
					break;
				}
				default: {
					break;
				}
			}
			if (openElements > 0) {
				inStream.next();
			}
		}
	}

}
