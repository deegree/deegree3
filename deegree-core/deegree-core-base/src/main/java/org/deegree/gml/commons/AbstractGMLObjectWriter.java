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

package org.deegree.gml.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.gml.reference.GmlXlinkStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete extensions are writers for a specific category of GML objects.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class AbstractGMLObjectWriter {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractGMLObjectWriter.class);

	protected final GMLStreamWriter gmlStreamWriter;

	protected final XMLStreamWriter writer;

	protected final String gmlNs;

	protected final GMLVersion version;

	protected final Map<String, String> prefixToNs;

	protected final Map<String, String> nsToPrefix = new HashMap<String, String>();

	protected final GmlXlinkStrategy referenceExportStrategy;

	private int prefixIndex;

	/**
	 * Creates a new {@link AbstractGMLObjectWriter} instance.
	 * @param gmlStreamWriter GML stream writer, must not be <code>null</code>
	 */
	public AbstractGMLObjectWriter(GMLStreamWriter gmlStreamWriter) {
		this.gmlStreamWriter = gmlStreamWriter;
		this.writer = gmlStreamWriter.getXMLStream();
		this.version = gmlStreamWriter.getVersion();
		this.gmlNs = version.getNamespace();
		this.prefixToNs = gmlStreamWriter.getNamespaceBindings();
		referenceExportStrategy = gmlStreamWriter.getReferenceResolveStrategy();

		if (prefixToNs != null) {
			for (Entry<String, String> prefixAndNs : prefixToNs.entrySet()) {
				nsToPrefix.put(prefixAndNs.getValue(), prefixAndNs.getKey());
			}
		}
	}

	protected void writeStartElementWithNS(String namespaceURI, String localname) throws XMLStreamException {

		if (namespaceURI == null || namespaceURI.length() == 0) {
			writer.writeStartElement(localname);
		}
		else {
			if (writer.getNamespaceContext().getPrefix(namespaceURI) == null) {
				String prefix = nsToPrefix.get(namespaceURI);
				if (prefix != null) {
					writer.writeStartElement(prefix, localname, namespaceURI);
					writer.writeNamespace(prefix, namespaceURI);
				}
				else {
					nsToPrefix.put("ns" + ++prefixIndex, namespaceURI);
					LOG.warn("No prefix for namespace '{}' configured. Using {}.", namespaceURI, "ns" + prefixIndex);
					writer.writeStartElement(prefix, localname, namespaceURI);
					writer.writeNamespace(prefix, namespaceURI);
				}
			}
			else {
				writer.writeStartElement(namespaceURI, localname);
			}
		}
	}

	protected void writeAttributeWithNS(String namespaceURI, String localname, String value) throws XMLStreamException {
		if (namespaceURI == null || namespaceURI.length() == 0) {
			writer.writeAttribute(localname, value);
		}
		else {
			String prefix = writer.getNamespaceContext().getPrefix(namespaceURI);
			if (prefix == null) {
				prefix = nsToPrefix.get(namespaceURI);
				if (prefix != null) {
					writer.setPrefix(prefix, namespaceURI);
					writer.writeNamespace(prefix, namespaceURI);
				}
				else {
					LOG.warn("No prefix for namespace '{}' configured. Depending on XMLStream auto-repairing.",
							namespaceURI);
				}
			}
			writer.writeAttribute(prefix, namespaceURI, localname, value);
		}
	}

	protected void writeEmptyElementWithNS(String namespaceURI, String localname) throws XMLStreamException {
		if (namespaceURI == null || namespaceURI.length() == 0) {
			writer.writeEmptyElement(localname);
		}
		else {
			if (writer.getNamespaceContext().getPrefix(namespaceURI) == null) {
				String prefix = nsToPrefix.get(namespaceURI);
				if (prefix != null) {
					writer.writeEmptyElement(prefix, localname, namespaceURI);
					writer.writeNamespace(prefix, namespaceURI);
				}
				else {
					LOG.warn("No prefix for namespace '{}' configured. Depending on XMLStream auto-repairing.",
							namespaceURI);
					writer.writeEmptyElement(namespaceURI, localname);
				}
			}
			else {
				writer.writeEmptyElement(namespaceURI, localname);
			}
		}
	}

	protected void endEmptyElement() throws XMLStreamException {
		// signal "end" of empty element to get rid of locally bound namespace prefixes
		writer.writeCharacters("");
	}

	protected GmlXlinkOptions getResolveStateForNextLevel(GmlXlinkOptions state) {
		return new GmlXlinkOptions(null, state.getDepth(), state.getCurrentLevel() + 1, state.getMode(),
				state.getRemoteTimeoutInMilliseconds());
	}

}
