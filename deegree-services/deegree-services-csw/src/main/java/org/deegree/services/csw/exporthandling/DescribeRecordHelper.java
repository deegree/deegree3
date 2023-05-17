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

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.services.controller.utils.HttpResponseBuffer;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class DescribeRecordHelper {

	/**
	 * SchemaCompontent which encapsulates the requested xml schema.
	 * @param writer
	 * @param record
	 * @param typeName that corresponds to the requested {@link MetadataStore}
	 * @throws XMLStreamException
	 */
	public void exportSchemaComponent(XMLStreamWriter writer, QName typeName, InputStreamReader isr)
			throws XMLStreamException {

		writer.writeStartElement(CSW_202_NS, "SchemaComponent");

		// required, by default XMLSCHEMA
		writer.writeAttribute("schemaLanguage", "XMLSCHEMA");
		// required
		writer.writeAttribute("targetNamespace", typeName.getNamespaceURI());

		/*
		 * optional parentSchema. This is handled in the recordStore in the describeRecord
		 * operation because it is a record profile specific value.
		 */
		// writer.writeAttribute( "parentSchema", "" );

		XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(isr);
		xmlReader.nextTag();
		XMLAdapter.writeElement(writer, xmlReader);

		xmlReader.close();

		writer.writeEndElement();// SchemaComponent

	}

	/**
	 * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
	 * @param writer writer to write the XML to, must not be null
	 * @param schemaLocation allows to specify a value for the 'xsi:schemaLocation'
	 * attribute in the root element, must not be null
	 * @return {@link XMLStreamWriter}
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public XMLStreamWriter getXMLResponseWriter(HttpResponseBuffer writer, String schemaLocation)
			throws XMLStreamException, IOException {

		if (schemaLocation == null) {
			return writer.getXMLWriter();
		}
		return new SchemaLocationXMLStreamWriter(writer.getXMLWriter(), schemaLocation);
	}

}
