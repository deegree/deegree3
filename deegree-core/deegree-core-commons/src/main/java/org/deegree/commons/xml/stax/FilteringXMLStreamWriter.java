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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.XPathUtils;

/**
 * ${link XMLStreamWriter} implementation that only writes out document parts that match
 * given xpaths.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class FilteringXMLStreamWriter implements XMLStreamWriter {

	private final XMLStreamWriter writer;

	private boolean isWriting = true;

	private XPathStack stack = new XPathStack();

	/**
	 * The xpaths must contain at least one xpath matching the root element, else an empty
	 * document will be written. The xpaths must be simple location paths that only use
	 * steps with qualified names (no predicates, no attribute steps).
	 * @param writer
	 * @param xpaths
	 */
	public FilteringXMLStreamWriter(XMLStreamWriter writer, List<XPath> xpaths) {
		this.writer = writer;
		for (XPath x : xpaths) {
			List<QName> names = XPathUtils.extractQNames(x);
			stack.addPath(names);
		}
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		stack.startElement(new QName(localName));
		if (!isWriting) {
			return;
		}
		isWriting = stack.shouldWrite();
		if (!isWriting) {
			return;
		}
		writer.writeStartElement(localName);
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		stack.startElement(new QName(namespaceURI, localName));
		if (!isWriting) {
			return;
		}
		isWriting = stack.shouldWrite();
		if (!isWriting) {
			return;
		}
		writer.writeStartElement(namespaceURI, localName);
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		stack.startElement(new QName(namespaceURI, localName, prefix));
		if (!isWriting) {
			return;
		}
		isWriting = stack.shouldWrite();
		if (!isWriting) {
			return;
		}
		writer.writeStartElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		stack.startElement(new QName(namespaceURI, localName));
		isWriting = stack.shouldWrite();
		if (!isWriting) {
			stack.endElement();
			return;
		}
		stack.endElement();
		writer.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		stack.startElement(new QName(prefix, localName, namespaceURI));
		isWriting = stack.shouldWrite();
		if (!isWriting) {
			stack.endElement();
			return;
		}
		stack.endElement();
		writer.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		stack.startElement(new QName(localName));
		isWriting = stack.shouldWrite();
		if (!isWriting) {
			stack.endElement();
			return;
		}
		stack.endElement();
		writer.writeEmptyElement(localName);
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		stack.endElement();
		if (isWriting) {
			writer.writeEndElement();
		}
		isWriting = stack.shouldWrite();
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}

	@Override
	public void close() throws XMLStreamException {
		writer.close();
	}

	@Override
	public void flush() throws XMLStreamException {
		writer.flush();
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeAttribute(localName, value);
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeAttribute(prefix, namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeAttribute(namespaceURI, localName, value);
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeDefaultNamespace(namespaceURI);
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeComment(data);
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeProcessingInstruction(target);
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeProcessingInstruction(target, data);
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeCData(data);
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeDTD(dtd);
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeEntityRef(name);
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		writer.writeStartDocument();
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		writer.writeStartDocument(version);
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		writer.writeStartDocument(encoding, version);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeCharacters(text);
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		if (!isWriting) {
			return;
		}
		writer.writeCharacters(text, start, len);
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return writer.getPrefix(uri);
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		if (isWriting) {
			writer.setPrefix(prefix, uri);
		}
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		if (isWriting) {
			writer.setDefaultNamespace(uri);
		}
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		if (isWriting) {
			writer.setNamespaceContext(context);
		}
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return writer.getNamespaceContext();
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return writer.getProperty(name);
	}

	// here comes the xpath matching logic
	static class Path {

		List<QName> names;

		Path(List<QName> names) {
			this.names = names;
		}

		boolean matches(List<QName> path) {
			Iterator<QName> i1 = names.iterator();
			// need to reverse-iterate over the path
			ListIterator<QName> i2 = path.listIterator(path.size());
			while (i1.hasNext() && i2.hasPrevious()) {
				if (!i1.next().equals(i2.previous())) {
					// one mis-step -> bad
					return false;
				}
			}
			// paths are identical until now -> good
			return true;
		}

	}

	static class XPathStack {

		LinkedList<QName> stack = new LinkedList<QName>();

		List<Path> paths = new ArrayList<Path>();

		void startElement(QName name) {
			stack.push(name);
		}

		void endElement() {
			stack.pop();
		}

		void addPath(List<QName> names) {
			paths.add(new Path(names));
		}

		boolean shouldWrite() {
			for (Path p : paths) {
				if (p.matches(stack)) {
					return true;
				}
			}
			return false;
		}

	}

}
