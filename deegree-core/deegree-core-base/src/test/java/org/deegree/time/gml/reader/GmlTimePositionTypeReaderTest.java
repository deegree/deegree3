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
package org.deegree.time.gml.reader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.deegree.commons.xml.stax.XMLStreamUtils.require;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.time.position.IndeterminateValue.BEFORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import javax.xml.stream.XMLStreamReader;

import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.time.gml.reader.GmlTimePositionTypeReader;
import org.deegree.time.position.TimePosition;
import org.junit.Test;

public class GmlTimePositionTypeReaderTest {

	@Test
	public void readMinimalExample() throws Exception {
		final GMLStreamReader reader = getGmlStreamReader("time_position_minimal.gml");
		final XMLStreamReader xmlStream = reader.getXMLReader();
		final TimePosition timePosition = new GmlTimePositionTypeReader().read(xmlStream);
		assertNotNull(timePosition);
		assertNull(timePosition.getFrame());
		assertNull(timePosition.getCalendarEraName());
		assertNull(timePosition.getIndeterminatePosition());
		assertEquals("2001-05-23", timePosition.getValue());
		assertOnEndElement(xmlStream);
	}

	@Test
	public void readFullExample() throws Exception {
		final GMLStreamReader reader = getGmlStreamReader("time_position_full.gml");
		final XMLStreamReader xmlStream = reader.getXMLReader();
		final TimePosition timePosition = new GmlTimePositionTypeReader().read(xmlStream);
		assertNotNull(timePosition);
		assertEquals("http://my.big.org/TRS/calendars/japanese", timePosition.getFrame());
		assertEquals("Meiji", timePosition.getCalendarEraName());
		assertEquals(BEFORE, timePosition.getIndeterminatePosition());
		assertEquals("0025-03", timePosition.getValue());
		assertOnEndElement(xmlStream);
	}

	private void assertOnEndElement(final XMLStreamReader xmlStream) {
		require(xmlStream, END_ELEMENT);
		assertEquals("timePosition", xmlStream.getLocalName());
	}

	private GMLStreamReader getGmlStreamReader(final String exampleName) throws Exception {
		final URL url = GmlTimePositionTypeReader.class.getResource(exampleName);
		GMLStreamReader reader = GMLInputFactory.createGMLStreamReader(GML_32, url);
		skipStartDocument(reader.getXMLReader());
		return reader;
	}

}
