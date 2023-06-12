/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.services.wms.controller.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.WMS_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetCapabilities;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetFeatureInfo;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.capabilities;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.protocol.wms.WMSConstants;
import org.deegree.services.encoding.SupportedEncodings;

/**
 * Writes soap wms support as extended capabilities.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Wms130SoapExtendedCapabilitesWriter {

	private static final String SOAPWMS_XSD_LOCATION = "https://schemas.deegree.org/core/3.5/extensions/services/wms/soapwms.xsd";

	public static final String SOAPWMS_NS = "https://schemas.deegree.org/extensions/services/wms/1.3.0";

	public static final String SOAPWMS_PREFIX = "soapwms";

	/**
	 * Writes soap support as extended capabilities.
	 * @param writer to write in, never <code>null</code>
	 * @param postUrl the url used as endpoint, never <code>null</code>
	 * @throws XMLStreamException
	 */
	public void writeSoapWmsExtendedCapabilites(XMLStreamWriter writer, String postUrl,
			SupportedEncodings supportedEncodings) throws XMLStreamException {
		if (soapEncodingIsSupportedForAtLeastOneRequestType(supportedEncodings)) {
			writer.setPrefix(SOAPWMS_PREFIX, SOAPWMS_NS);
			writer.writeStartElement(SOAPWMS_NS, "ExtendedCapabilities");
			writer.writeNamespace(SOAPWMS_PREFIX, SOAPWMS_NS);
			writer.writeNamespace(WMS_PREFIX, WMSNS);
			writer.writeNamespace(XSI_PREFIX, XSINS);
			writer.writeNamespace(XLINK_PREFIX, XLNNS);

			writer.writeAttribute(XSINS, "schemaLocation", SOAPWMS_NS + " " + SOAPWMS_XSD_LOCATION);

			writer.writeStartElement(SOAPWMS_NS, "SOAP");

			writeOnlineResource(writer, postUrl);
			writeSoapVersionConstraint(writer);
			writeSupportedOperations(writer, supportedEncodings);

			writer.writeEndElement();
			writer.writeEndElement();
		}
	}

	private void writeOnlineResource(XMLStreamWriter writer, String postUrl) throws XMLStreamException {
		writer.writeStartElement(WMSNS, "OnlineResource");
		writer.writeAttribute(XLNNS, "type", "simple");
		writer.writeAttribute(XLNNS, "href", postUrl);
		writer.writeEndElement();
	}

	private void writeSoapVersionConstraint(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(SOAPWMS_NS, "Constraint");
		writer.writeAttribute("name", "SOAPVersion");
		writeValue(writer, "1.1");
		writeValue(writer, "1.2");
		writer.writeEndElement();
	}

	private void writeSupportedOperations(XMLStreamWriter writer, SupportedEncodings supportedEncodings)
			throws XMLStreamException {
		writer.writeStartElement(SOAPWMS_NS, "SupportedOperations");
		if (isGetCapabilitiesSupported(supportedEncodings))
			writeOperation(writer, "GetCapabilities");
		if (isGetMapSupported(supportedEncodings))
			writeOperation(writer, "GetMap");
		if (isGetFeatureInfoSupported(supportedEncodings))
			writeOperation(writer, "GetFeatureInfo");
		writer.writeEndElement();
	}

	private void writeValue(XMLStreamWriter writer, String value) throws XMLStreamException {
		writer.writeStartElement(SOAPWMS_NS, "Value");
		writer.writeCharacters(value);
		writer.writeEndElement();
	}

	private void writeOperation(XMLStreamWriter writer, String operationName) throws XMLStreamException {
		writer.writeStartElement(SOAPWMS_NS, "Operation");
		writer.writeAttribute("name", operationName);
		writer.writeEndElement();
	}

	private boolean isGetCapabilitiesSupported(SupportedEncodings supportedEncodings) {
		return supportedEncodings.isEncodingSupported(GetCapabilities, "SOAP")
				|| supportedEncodings.isEncodingSupported(capabilities, "SOAP");
	}

	private boolean isGetMapSupported(SupportedEncodings supportedEncodings) {
		return supportedEncodings.isEncodingSupported(GetMap, "SOAP")
				|| supportedEncodings.isEncodingSupported(map, "SOAP");
	}

	private boolean isGetFeatureInfoSupported(SupportedEncodings supportedEncodings) {
		return supportedEncodings.isEncodingSupported(GetFeatureInfo, "SOAP");
	}

	private boolean soapEncodingIsSupportedForAtLeastOneRequestType(SupportedEncodings supportedEncodings) {
		return isGetCapabilitiesSupported(supportedEncodings) || isGetMapSupported(supportedEncodings)
				|| isGetFeatureInfoSupported(supportedEncodings);
	}

}