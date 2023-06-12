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
package org.deegree.services.wfs.format.gml;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipToRequiredElement;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.gml.GMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>XMLStreamWriter</code> wrapper that pipes through all events by default, but can
 * be switched to a buffer during writing.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider/a>
 */
public class BufferableXMLStreamWriter implements XMLStreamWriter {

	private static Logger LOG = LoggerFactory.getLogger(BufferableXMLStreamWriter.class);

	private static final int MEMORY_BUFFER_SIZE_IN_BYTES = 10 * 1024 * 1024;

	private static final String BUFFER_NS = "http://www.deegree.org/buffering/wfs/service";

	private static final String BUFFER_PREFIX = "d3buffering";

	private static final String ELEMENT_NAME_CONTAINER_ROOT = "Container";

	private static final String ELEMENT_NAME_OPEN_ELEMENT = "OpenElement";

	private static final String ELEMENT_NAME_UNCLEAR_REFERENCE = "Reference";

	private final XMLStreamWriter sink;

	private StreamBufferStore buffer;

	private XMLStreamWriter activeWriter;

	private int openElements;

	private final String xLinkTemplate;

	// keeps track of the namespace bindings
	private final NamespaceBindings nsBindings = new NamespaceBindings();

	public BufferableXMLStreamWriter(XMLStreamWriter sink, String xLinkTemplate) {
		this.sink = sink;
		this.xLinkTemplate = xLinkTemplate;
		activeWriter = sink;
	}

	public boolean hasBuffered() {
		return buffer != null;
	}

	public XMLStreamWriter getSink() {
		return sink;
	}

	public void appendBufferedXML(GMLStreamWriter gmlWriter)
			throws XMLStreamException, FactoryConfigurationError, IOException {

		XMLStreamReader inStream = getBufferedXML();
		skipToRequiredElement(inStream, new QName(BUFFER_NS, ELEMENT_NAME_UNCLEAR_REFERENCE, BUFFER_PREFIX));
		boolean onContentElement = true;

		int eventType = 0;
		while ((eventType = inStream.getEventType()) != END_DOCUMENT) {
			switch (eventType) {
				case CDATA: {
					sink.writeCData(inStream.getText());
					break;
				}
				case CHARACTERS: {
					sink.writeCharacters(inStream.getTextCharacters(), inStream.getTextStart(),
							inStream.getTextLength());
					break;
				}
				case COMMENT: {
					sink.writeComment(inStream.getText());
					break;
				}
				case END_ELEMENT: {
					String localName = inStream.getLocalName();
					if (localName.equals(ELEMENT_NAME_UNCLEAR_REFERENCE)) {
						// StaX workaround: signal "end" of empty element to get rid of
						// locally bound namespace prefixes
						// otherwise sink.getPrefix () will lie right in our faces...
						sink.writeCharacters("");
					}
					else if (!localName.equals(ELEMENT_NAME_CONTAINER_ROOT)) {
						sink.writeEndElement();
					}
					break;
				}
				case START_ELEMENT: {
					if (!onContentElement) {
						String localName = inStream.getLocalName();
						String nsUri = inStream.getNamespaceURI();
						String prefix = inStream.getPrefix();
						if (nsUri == null || prefix == null) {
							sink.writeStartElement(localName);
						}
						else {
							if (sink.getPrefix(nsUri) == null) {
								sink.writeStartElement(prefix, localName, nsUri);
								sink.writeNamespace(prefix, nsUri);
							}
							else {
								sink.writeStartElement(nsUri, localName);
							}
						}
					}
					else {
						onContentElement = false;
					}
					copyNamespaceBindings(inStream);
					copyAttributes(gmlWriter, inStream);
					break;
				}
				default: {
					break;
				}
			}
			inStream.next();
		}
	}

	private void copyAttributes(GMLStreamWriter gmlWriter, XMLStreamReader inStream) throws XMLStreamException {
		for (int i = 0; i < inStream.getAttributeCount(); i++) {
			String attrLocalName = inStream.getAttributeLocalName(i);
			String nsPrefix = inStream.getAttributePrefix(i);
			String value = inStream.getAttributeValue(i);
			String nsURI = inStream.getAttributeNamespace(i);
			if (nsURI == null || nsURI.equals("")) {
				sink.writeAttribute(attrLocalName, value);
			}
			else {
				if (attrLocalName.equals("href") && nsURI.equals(XLNNS)) {
					if (value.startsWith("{") || value.endsWith("}")) {
						String objectId = value.substring(1, value.length() - 1);
						if (gmlWriter.getReferenceResolveStrategy().isObjectExported(objectId)) {
							value = "#" + objectId;
						}
						else {
							value = xLinkTemplate.replace("{}", objectId);
						}
					}
				}

				if (sink.getPrefix(nsURI) == null) {
					sink.writeNamespace(nsPrefix, nsURI);
				}

				sink.writeAttribute(nsURI, attrLocalName, value);
			}
		}
	}

	private void copyNamespaceBindings(XMLStreamReader inStream) throws XMLStreamException {
		for (int i = 0; i < inStream.getNamespaceCount(); i++) {
			String nsPrefix = inStream.getNamespacePrefix(i);
			String nsURI = inStream.getNamespaceURI(i);
			if (sink.getPrefix(nsURI) == null) {
				sink.writeNamespace(nsPrefix, nsURI);
			}
		}
	}

	public XMLStreamReader getBufferedXML() throws XMLStreamException, FactoryConfigurationError, IOException {
		if (buffer == null) {
			return null;
		}
		close();
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(buffer.getInputStream(), "UTF-8");
		XMLStreamUtils.skipStartDocument(reader);
		return reader;
	}

	public void activateBuffering() throws XMLStreamException {
		if (activeWriter == sink) {
			LOG.debug("Switching to buffered XMLStreamWriter, openElements: " + openElements);
			buffer = new StreamBufferStore(MEMORY_BUFFER_SIZE_IN_BYTES);
			XMLOutputFactory of = XMLOutputFactory.newInstance();
			activeWriter = of.createXMLStreamWriter(buffer, "UTF-8");
			writeWrapperElementWithNamespacesAndDummyLevel();
		}
	}

	private void writeWrapperElementWithNamespacesAndDummyLevel() throws XMLStreamException {
		activeWriter.writeStartElement(BUFFER_PREFIX, ELEMENT_NAME_CONTAINER_ROOT, BUFFER_NS);
		activeWriter.writeNamespace(BUFFER_PREFIX, BUFFER_NS);
		Iterator<String> namespaceIter = nsBindings.getNamespaceURIs();
		while (namespaceIter.hasNext()) {
			String ns = namespaceIter.next();
			String prefix = nsBindings.getPrefix(ns);
			activeWriter.writeNamespace(prefix, ns);
			LOG.debug(prefix + "->" + ns);
		}
		if (nsBindings.getPrefix(XSINS) == null) {
			activeWriter.writeNamespace(XSI_PREFIX, XSINS);
		}

		for (int i = 0; i < openElements; i++) {
			activeWriter.writeStartElement(BUFFER_PREFIX, ELEMENT_NAME_OPEN_ELEMENT, BUFFER_NS);
		}
		activeWriter.writeEmptyElement(BUFFER_PREFIX, ELEMENT_NAME_UNCLEAR_REFERENCE, BUFFER_NS);
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		activeWriter.writeStartElement(localName);
		openElements++;
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		activeWriter.writeStartElement(namespaceURI, localName);
		openElements++;
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		nsBindings.addNamespace(prefix, namespaceURI);
		activeWriter.writeStartElement(prefix, localName, namespaceURI);
		openElements++;
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		activeWriter.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		activeWriter.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		activeWriter.writeEmptyElement(localName);
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		activeWriter.writeEndElement();
		openElements--;
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		activeWriter.writeEndDocument();
	}

	@Override
	public void close() throws XMLStreamException {
		activeWriter.close();
	}

	@Override
	public void flush() throws XMLStreamException {
		activeWriter.flush();
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		activeWriter.writeAttribute(localName, value);
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		nsBindings.addNamespace(prefix, namespaceURI);
		activeWriter.writeAttribute(prefix, namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		activeWriter.writeAttribute(namespaceURI, localName, value);
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		nsBindings.addNamespace(prefix, namespaceURI);
		activeWriter.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		nsBindings.addNamespace(DEFAULT_NS_PREFIX, namespaceURI);
		activeWriter.writeDefaultNamespace(namespaceURI);
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		activeWriter.writeComment(data);
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		activeWriter.writeProcessingInstruction(target);
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		activeWriter.writeProcessingInstruction(target, data);
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		activeWriter.writeCData(data);
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		activeWriter.writeDTD(dtd);
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		activeWriter.writeEntityRef(name);
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		activeWriter.writeStartDocument();
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		activeWriter.writeStartDocument(version);
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		activeWriter.writeStartDocument(encoding, version);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		activeWriter.writeCharacters(text);
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		activeWriter.writeCharacters(text, start, len);
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return activeWriter.getPrefix(uri);
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		nsBindings.addNamespace(prefix, uri);
		activeWriter.setPrefix(prefix, uri);
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		nsBindings.addNamespace(DEFAULT_NS_PREFIX, uri);
		activeWriter.setDefaultNamespace(uri);
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		activeWriter.setNamespaceContext(context);
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return activeWriter.getNamespaceContext();
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return activeWriter.getProperty(name);
	}

}
