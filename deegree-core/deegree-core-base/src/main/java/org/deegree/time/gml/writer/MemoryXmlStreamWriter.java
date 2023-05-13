/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.time.gml.writer;

import static javax.xml.stream.XMLOutputFactory.newInstance;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class MemoryXmlStreamWriter implements XMLStreamWriter {

	private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

	private final XMLStreamWriter writer;

	public MemoryXmlStreamWriter() {
		try {
			writer = newInstance().createXMLStreamWriter(bos);
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public String getOutput() {
		try {
			writer.flush();
			return bos.toString("UTF-8");
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
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

	public String getPrefix(String arg0) throws XMLStreamException {
		return writer.getPrefix(arg0);
	}

	public Object getProperty(String arg0) throws IllegalArgumentException {
		return writer.getProperty(arg0);
	}

	public void setDefaultNamespace(String arg0) throws XMLStreamException {
		writer.setDefaultNamespace(arg0);
	}

	public void setNamespaceContext(NamespaceContext arg0) throws XMLStreamException {
		writer.setNamespaceContext(arg0);
	}

	public void setPrefix(String arg0, String arg1) throws XMLStreamException {
		writer.setPrefix(arg0, arg1);
	}

	public void writeAttribute(String arg0, String arg1, String arg2, String arg3) throws XMLStreamException {
		writer.writeAttribute(arg0, arg1, arg2, arg3);
	}

	public void writeAttribute(String arg0, String arg1, String arg2) throws XMLStreamException {
		writer.writeAttribute(arg0, arg1, arg2);
	}

	public void writeAttribute(String arg0, String arg1) throws XMLStreamException {
		writer.writeAttribute(arg0, arg1);
	}

	public void writeCData(String arg0) throws XMLStreamException {
		writer.writeCData(arg0);
	}

	public void writeCharacters(char[] arg0, int arg1, int arg2) throws XMLStreamException {
		writer.writeCharacters(arg0, arg1, arg2);
	}

	public void writeCharacters(String arg0) throws XMLStreamException {
		writer.writeCharacters(arg0);
	}

	public void writeComment(String arg0) throws XMLStreamException {
		writer.writeComment(arg0);
	}

	public void writeDTD(String arg0) throws XMLStreamException {
		writer.writeDTD(arg0);
	}

	public void writeDefaultNamespace(String arg0) throws XMLStreamException {
		writer.writeDefaultNamespace(arg0);
	}

	public void writeEmptyElement(String arg0, String arg1, String arg2) throws XMLStreamException {
		writer.writeEmptyElement(arg0, arg1, arg2);
	}

	public void writeEmptyElement(String arg0, String arg1) throws XMLStreamException {
		writer.writeEmptyElement(arg0, arg1);
	}

	public void writeEmptyElement(String arg0) throws XMLStreamException {
		writer.writeEmptyElement(arg0);
	}

	public void writeEndDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}

	public void writeEndElement() throws XMLStreamException {
		writer.writeEndElement();
	}

	public void writeEntityRef(String arg0) throws XMLStreamException {
		writer.writeEntityRef(arg0);
	}

	public void writeNamespace(String arg0, String arg1) throws XMLStreamException {
		writer.writeNamespace(arg0, arg1);
	}

	public void writeProcessingInstruction(String arg0, String arg1) throws XMLStreamException {
		writer.writeProcessingInstruction(arg0, arg1);
	}

	public void writeProcessingInstruction(String arg0) throws XMLStreamException {
		writer.writeProcessingInstruction(arg0);
	}

	public void writeStartDocument() throws XMLStreamException {
		writer.writeStartDocument();
	}

	public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
		writer.writeStartDocument(arg0, arg1);
	}

	public void writeStartDocument(String arg0) throws XMLStreamException {
		writer.writeStartDocument(arg0);
	}

	public void writeStartElement(String arg0, String arg1, String arg2) throws XMLStreamException {
		writer.writeStartElement(arg0, arg1, arg2);
	}

	public void writeStartElement(String arg0, String arg1) throws XMLStreamException {
		writer.writeStartElement(arg0, arg1);
	}

	public void writeStartElement(String arg0) throws XMLStreamException {
		writer.writeStartElement(arg0);
	}

}
