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
package org.deegree.protocol.ows.exception;

import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.xml.stax.XMLStreamUtils;

/**
 * Reads exception report documents provided by OGC web services.
 * <p>
 * This parser is designed to be as relaxed as possible about the format of the input (in
 * order to be able to cope with broken/invalid responses as well). Respected/tested
 * exception report formats:
 * <ul>
 * <li><code>{null}ServiceExceptionReport</code>: e.g. used by WMS 1.1.1</li>
 * <li><code>{http://www.opengis.net/ogc}ServiceExceptionReport</code>: e.g. used by WFS
 * 1.0.0</li>
 * <li><code>{http://www.opengis.net/ows}ExceptionReport</code>: OWS 1.0.0</li>
 * <li><code>{http://www.opengis.net/ows}ExceptionReport</code>: OWS 1.1.0</li>
 * <li><code>{http://www.opengis.net/ows}ExceptionReport</code>: OWS 2.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class OWSExceptionReader {

	/**
	 * Checks whether the given {@link XMLStreamReader} points to an exception report and
	 * causes an {@linK OWSExceptionReport} exception (with the content of the element) if
	 * this is the case.
	 * @param xml XML stream reader, must not be <code>null</code> and point at a
	 * <code>START_DOCUMENT</code> or <code>START_ELEMENT</code> event, if the element is
	 * an exception report, it points at the corresponding <code>END_ELEMENT</code> event
	 * afterwards
	 * @throws OWSExceptionReport if the reader points at an exception report
	 * @throws XMLStreamException
	 */
	public static void assertNoExceptionReport(XMLStreamReader xmlStream)
			throws OWSExceptionReport, XMLStreamException {
		skipStartDocument(xmlStream);
		if (isExceptionReport(xmlStream.getName())) {
			throw parseExceptionReport(xmlStream);
		}
	}

	/**
	 * Checks whether the given {@link OMElement} is an exception report element and
	 * causes an {@link OWSExceptionReport} exception (with the content of the element) if
	 * this is the case.
	 * @param xml XML stream reader, must not be <code>null</code> and point at a
	 * <code>START_DOCUMENT</code> or <code>START_ELEMENT</code> event, if the element is
	 * an exception report, it points at the corresponding <code>END_ELEMENT</code> event
	 * afterwards
	 * @throws OWSExceptionReport if the reader points at an exception report
	 * @throws XMLStreamException
	 */
	public static void assertNoExceptionReport(OMElement element) throws OWSExceptionReport, XMLStreamException {
		if (isExceptionReport(element.getQName())) {
			XMLStreamReader xmlStream = element.getXMLStreamReader();
			try {
				XMLStreamUtils.skipStartDocument(xmlStream);
				throw parseExceptionReport(xmlStream);
			}
			finally {
				xmlStream.close();
			}
		}
	}

	/**
	 * Returns whether the given element name denotes an exception report.
	 * @param elName qualified name of the potential exception report element, must not be
	 * <code>null</code>
	 * @return <code>true</code> if the current element in the reader (seems to) denote an
	 * exception report, <code>false</code> otherwise
	 */
	public static boolean isExceptionReport(QName elName) {
		// this check is very lax by intention
		return elName.getLocalPart().toLowerCase().contains("exceptionreport");
	}

	/**
	 * Parses the service exception report element that the given {@link XMLStreamReader}
	 * points at.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event
	 * </ul>
	 * @param reader XML stream reader, must not be <code>null</code> and point at a
	 * <code>START_ELEMENT</code> event, points at the corresponding
	 * <code>END_ELEMENT</code> event afterwards
	 * @return the parsed {@link OWSException}, never <code>null</code>
	 * @throws XMLStreamException
	 * @throws NoSuchElementException
	 */
	public static OWSExceptionReport parseExceptionReport(XMLStreamReader reader)
			throws NoSuchElementException, XMLStreamException {
		while (reader.getEventType() != XMLStreamReader.START_ELEMENT) {
			reader.next();
		}
		// <attribute name="version" use="required">
		String version = reader.getAttributeValue(null, "version");

		// <attribute ref="xml:lang" use="optional">
		String lang = reader.getAttributeValue(null, "lang");

		List<OWSException> exceptions = new ArrayList<OWSException>();

		XMLStreamUtils.nextElement(reader);
		while (reader.isStartElement()) {
			// <element ref="ows:Exception" maxOccurs="unbounded">
			exceptions.add(parseException(reader));
			XMLStreamUtils.nextElement(reader);
		}

		return new OWSExceptionReport(exceptions, lang, version);
	}

	private static OWSException parseException(XMLStreamReader reader) throws XMLStreamException {

		// <attribute name="exceptionCode" type="string" use="required">
		String code = reader.getAttributeValue(null, "exceptionCode");
		if (code == null) {
			code = reader.getAttributeValue(null, "code");
		}

		// <attribute name="locator" type="string" use="optional">
		String locator = reader.getAttributeValue(null, "locator");

		List<String> messages = new ArrayList<String>();

		// inlined message text (pre-OWS 2.0 style)
		StringBuilder sb = new StringBuilder();
		sb.append(consumeText(reader));
		if (sb.length() != 0) {
			messages.add(sb.toString());
		}

		// every container element is assumed to be
		// <element name="ExceptionText" type="string" minOccurs="0"
		// maxOccurs="unbounded">
		while (reader.isStartElement()) {
			messages.add(consumeText(reader));
			// nested elements -> skip 'em
			while (reader.isStartElement()) {
				skipElement(reader);
				nextElement(reader);
			}
			nextElement(reader);
		}
		return new OWSException(messages, code, locator);
	}

	private static String consumeText(XMLStreamReader reader) throws XMLStreamException {
		reader.next();
		StringBuilder sb = new StringBuilder();
		while (!reader.isEndElement() && !reader.isStartElement()) {
			if (reader.isCharacters()) {
				sb.append(reader.getText().trim());
			}
			reader.next();
		}
		return sb.toString();
	}

}
