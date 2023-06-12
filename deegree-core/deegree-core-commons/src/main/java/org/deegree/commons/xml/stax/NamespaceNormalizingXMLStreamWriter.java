/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.NamespaceBindings;

/**
 * {@link XMLStreamWriter} that performs normalization of namespace bindings.
 *
 * Performed normalizations:
 * <ul>
 * <li><b>Prefixes</b>: The given namespace-prefix bindings take precedence over the
 * prefixes specified when writing namespaces.</li>
 * <li><b>Removal of redundant bindings</b>: If a namespace prefix is already bound (to
 * the same namespace) at a certain position in the document, subsequent (redundant)
 * bindings are skipped.</li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class NamespaceNormalizingXMLStreamWriter implements XMLStreamWriter {

	private final XMLStreamWriter targetWriter;

	private final NamespaceBindings nsBindings;

	public NamespaceNormalizingXMLStreamWriter(XMLStreamWriter targetWriter, NamespaceBindings nsBindings) {
		this.targetWriter = targetWriter;
		this.nsBindings = nsBindings;
	}

	@Override
	public void close() throws XMLStreamException {
		targetWriter.close();
	}

	@Override
	public void flush() throws XMLStreamException {
		targetWriter.flush();
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return targetWriter.getNamespaceContext();
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return targetWriter.getPrefix(uri);
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return targetWriter.getProperty(name);
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		String prefix = getPrefix(uri, DEFAULT_NS_PREFIX);
		if (DEFAULT_NS_PREFIX.equals(prefix)) {
			targetWriter.setDefaultNamespace(uri);
		}
		targetWriter.setPrefix(prefix, uri);
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		targetWriter.setNamespaceContext(context);
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		targetWriter.setPrefix(prefix, uri);
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		prefix = getPrefix(namespaceURI, prefix);
		targetWriter.writeAttribute(prefix, namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		targetWriter.writeAttribute(namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		targetWriter.writeAttribute(localName, value);
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		targetWriter.writeCData(data);
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		targetWriter.writeCharacters(text, start, len);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		targetWriter.writeCharacters(text);
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		targetWriter.writeComment(data);
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		targetWriter.writeDTD(dtd);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {

		String prefix = getPrefix(namespaceURI, DEFAULT_NS_PREFIX);

		if (prefix != null && prefix.equals(getPrefix(namespaceURI))) {
			// already bound -> skip binding
			return;
		}

		if (DEFAULT_NS_PREFIX.equals(prefix)) {
			targetWriter.writeDefaultNamespace(namespaceURI);
		}
		targetWriter.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		prefix = getPrefix(namespaceURI, prefix);
		targetWriter.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		targetWriter.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		targetWriter.writeEmptyElement(localName);
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		targetWriter.writeEndDocument();
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		targetWriter.writeEndElement();
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		targetWriter.writeEntityRef(name);
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		prefix = getPrefix(namespaceURI, prefix);

		if (prefix != null && prefix.equals(getPrefix(namespaceURI))) {
			// already bound -> skip binding
			return;
		}

		targetWriter.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		targetWriter.writeProcessingInstruction(target, data);
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		targetWriter.writeProcessingInstruction(target);
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		targetWriter.writeStartDocument();
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		targetWriter.writeStartDocument(encoding, version);
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		targetWriter.writeStartDocument(version);
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		prefix = getPrefix(namespaceURI, prefix);
		targetWriter.writeStartElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		targetWriter.writeStartElement(namespaceURI, localName);
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		targetWriter.writeStartElement(localName);
	}

	private String getPrefix(String namespaceURI, String defaultPrefix) {
		String prefix = nsBindings.getPrefix(namespaceURI);
		if (prefix == null) {
			prefix = defaultPrefix;
		}
		return prefix;
	}

}
