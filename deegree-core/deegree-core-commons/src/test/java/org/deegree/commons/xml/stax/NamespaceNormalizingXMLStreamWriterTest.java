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

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.junit.Test;

/**
 * Tests for the {@link NamespaceNormalizingXMLStreamWriter}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class NamespaceNormalizingXMLStreamWriterTest {

	@Test
	public void testNormalizeDefaultNamespace() throws XMLStreamException {

		String input = "<a xmlns=\"http://www.deegree.org/app\">Hello</a>";
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(input));
		XMLStreamUtils.skipStartDocument(reader);

		StringWriter output = new StringWriter();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
		NamespaceBindings nsBindings = new NamespaceBindings();
		nsBindings.addNamespace("app", "http://www.deegree.org/app");
		writer = new NamespaceNormalizingXMLStreamWriter(writer, nsBindings);

		XMLAdapter.writeElement(writer, reader);
		writer.close();
		assertEquals("<app:a xmlns:app=\"http://www.deegree.org/app\">Hello</app:a>", output.toString());
	}

	@Test
	public void testNormalizeNonDefaultNamespace() throws XMLStreamException {

		String input = "<app:a xmlns:app=\"http://www.deegree.org/app\">Hello</app:a>";
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(input));
		XMLStreamUtils.skipStartDocument(reader);

		StringWriter output = new StringWriter();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
		NamespaceBindings nsBindings = new NamespaceBindings();
		nsBindings.addNamespace("", "http://www.deegree.org/app");
		writer = new NamespaceNormalizingXMLStreamWriter(writer, nsBindings);

		XMLAdapter.writeElement(writer, reader);
		writer.close();
		assertEquals("<a xmlns=\"http://www.deegree.org/app\">Hello</a>", output.toString());
	}

	@Test
	public void testNormalizeNestedElementAndAttribute() throws XMLStreamException {

		String input = "<a xmlns=\"http://www.deegree.org/app\"><b xmlns:app=\"http://www.deegree.org/app\" app:attr=\"attribute\">Hello</b></a>";
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(input));
		XMLStreamUtils.skipStartDocument(reader);

		StringWriter output = new StringWriter();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
		NamespaceBindings nsBindings = new NamespaceBindings();
		nsBindings.addNamespace("app", "http://www.deegree.org/app");
		writer = new NamespaceNormalizingXMLStreamWriter(writer, nsBindings);

		XMLAdapter.writeElement(writer, reader);
		writer.close();
		assertEquals(
				"<app:a xmlns:app=\"http://www.deegree.org/app\"><app:b app:attr=\"attribute\">Hello</app:b></app:a>",
				output.toString());
	}

}
