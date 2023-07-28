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
package org.deegree.wps.jts;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;

/**
 * The <code>XMLGeometryProcessor</code> class solves the processing problem for
 * geometries in general for a given process.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 */
public class XMLGeometryProcessor {

	private GeometryHandler handler;

	private Map<String, Object> params;

	/**
	 * Initializes the {@link XMLGeometryProcessor} with a geometry processor
	 * @param handler
	 */
	public XMLGeometryProcessor(GeometryHandler handler, Map<String, Object> params) {
		this.handler = handler;
		this.params = params;
	}

	/**
	 * Reads the input stream and applies the processing operation by writing to the
	 * output stream
	 * @param gmlReader
	 * @param gmlWriter
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	public void process(GMLStreamReader gmlReader, GMLStreamWriter gmlWriter)
			throws XMLStreamException, XMLParsingException, UnknownCRSException, TransformationException {

		XMLStreamReader xmlReader = gmlReader.getXMLReader();
		XMLStreamWriter xmlWriter = gmlWriter.getXMLStream();

		if (xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT) {
			throw new XMLStreamException("Input stream does not point to a START_ELEMENT event.");
		}
		int openElements = 0;
		boolean firstRun = true;
		while (firstRun || openElements > 0) {
			firstRun = false;
			int eventType = xmlReader.getEventType();

			switch (eventType) {
				case CDATA: {
					xmlWriter.writeCData(xmlReader.getText());
					break;
				}
				case CHARACTERS: {
					xmlWriter.writeCharacters(xmlReader.getTextCharacters(), xmlReader.getTextStart(),
							xmlReader.getTextLength());
					break;
				}
				case END_ELEMENT: {
					xmlWriter.writeEndElement();
					openElements--;
					break;
				}
				case START_ELEMENT: {
					if (gmlReader.getGeometryReader().isGeometryElement(xmlReader)) {
						Geometry inputGeometry = gmlReader.readGeometry();
						Geometry processed = handler.process(inputGeometry, params);
						gmlWriter.write(processed);

					}
					else {
						if (xmlReader.getNamespaceURI() == "" || xmlReader.getPrefix() == DEFAULT_NS_PREFIX) {
							xmlWriter.writeStartElement(xmlReader.getLocalName());
						}
						else {
							if (xmlWriter.getNamespaceContext().getPrefix(xmlReader.getPrefix()) == "") {
								// TODO handle special cases for prefix binding, see
								// http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
								xmlWriter.setPrefix(xmlReader.getPrefix(), xmlReader.getNamespaceURI());
							}
							xmlWriter.writeStartElement(xmlReader.getPrefix(), xmlReader.getLocalName(),
									xmlReader.getNamespaceURI());
						}
						// copy all namespace bindings
						for (int i = 0; i < xmlReader.getNamespaceCount(); i++) {
							String nsPrefix = xmlReader.getNamespacePrefix(i);
							String nsURI = xmlReader.getNamespaceURI(i);
							xmlWriter.writeNamespace(nsPrefix, nsURI);
						}

						// copy all attributes
						for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
							String localName = xmlReader.getAttributeLocalName(i);
							String nsPrefix = xmlReader.getAttributePrefix(i);
							String value = xmlReader.getAttributeValue(i);
							String nsURI = xmlReader.getAttributeNamespace(i);
							if (nsURI == null) {
								xmlWriter.writeAttribute(localName, value);
							}
							else {
								xmlWriter.writeAttribute(nsPrefix, nsURI, localName, value);
							}
						}
						openElements++;
					}
					break;
				}
				default: {
					break;
				}
			}
			if (openElements > 0) {
				xmlReader.next();
			}
		}
	}

}
