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
package org.deegree.commons.xml.stax;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class TrimmingXMLStreamWriter implements XMLStreamWriter {

	private final XMLStreamWriter writer;

	public TrimmingXMLStreamWriter(XMLStreamWriter writer) {
		this.writer = writer;
	}

	public void close() throws XMLStreamException {
		writer.close();
	}

	public void flush() throws XMLStreamException {
		writer.flush();
	}

	public NamespaceContext getNamespaceContext() {
		return writer.getNamespaceContext();
	}

	public String getPrefix(String uri) throws XMLStreamException {
		return writer.getPrefix(uri);
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		return writer.getProperty(name);
	}

	public void setDefaultNamespace(String uri) throws XMLStreamException {
		writer.setDefaultNamespace(uri);
	}

	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		writer.setNamespaceContext(context);
	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		writer.setPrefix(prefix, uri);
	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		writer.writeAttribute(prefix, namespaceURI, localName, value);
	}

	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		writer.writeAttribute(namespaceURI, localName, value);
	}

	public void writeAttribute(String localName, String value) throws XMLStreamException {
		writer.writeAttribute(localName, value);
	}

	public void writeCData(String data) throws XMLStreamException {
		writer.writeCData(data);
	}

	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		String s = new String(text, start, len);
		writer.writeCharacters(s.trim());
	}

	public void writeCharacters(String text) throws XMLStreamException {
		writer.writeCharacters(text.trim());
	}

	public void writeComment(String data) throws XMLStreamException {
		writer.writeComment(data);
	}

	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		writer.writeDefaultNamespace(namespaceURI);
	}

	public void writeDTD(String dtd) throws XMLStreamException {
		writer.writeDTD(dtd);
	}

	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		writer.writeEmptyElement(prefix, localName, namespaceURI);
	}

	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		writer.writeEmptyElement(namespaceURI, localName);
	}

	public void writeEmptyElement(String localName) throws XMLStreamException {
		writer.writeEmptyElement(localName);
	}

	public void writeEndDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}

	public void writeEndElement() throws XMLStreamException {
		writer.writeEndElement();
	}

	public void writeEntityRef(String name) throws XMLStreamException {
		writer.writeEntityRef(name);
	}

	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		writer.writeNamespace(prefix, namespaceURI);
	}

	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		writer.writeProcessingInstruction(target, data);
	}

	public void writeProcessingInstruction(String target) throws XMLStreamException {
		writer.writeProcessingInstruction(target);
	}

	public void writeStartDocument() throws XMLStreamException {
		writer.writeStartDocument();
	}

	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		writer.writeStartDocument(encoding, version);
	}

	public void writeStartDocument(String version) throws XMLStreamException {
		writer.writeStartDocument(version);
	}

	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		writer.writeStartElement(prefix, localName, namespaceURI);
	}

	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		writer.writeStartElement(namespaceURI, localName);
	}

	public void writeStartElement(String localName) throws XMLStreamException {
		writer.writeStartElement(localName);
	}

}
