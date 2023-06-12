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

import org.apache.commons.io.IOUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.junit.Test;
import org.xmlunit.matchers.CompareMatcher;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class FilteringXMLStreamWriterTest {

	private final static String app = "http://www.deegree.org/app";

	private final static String nix = "http://www.deegree.org/nix";

	private final static String alles = "http://www.deegree.org/alles";

	private static final NamespaceBindings nsBindings = new NamespaceBindings();

	static {
		nsBindings.addNamespace("app", app);
		nsBindings.addNamespace("nix", nix);
		nsBindings.addNamespace("alles", alles);
	}

	private void writeDocument(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartDocument();
		writer.setPrefix("app", app);
		writer.setPrefix("nix", nix);
		writer.setPrefix("alles", alles);
		writer.writeStartElement(app, "a");
		writer.writeNamespace("app", app);
		writer.writeNamespace("nix", nix);
		writer.writeNamespace("alles", alles);
		writer.writeStartElement(app, "b");
		writer.writeStartElement(nix, "c");
		writer.writeStartElement(app, "d");
		writer.writeEndElement();
		writer.writeStartElement(alles, "e");
		writer.writeCharacters("sometext");
		writer.writeEndElement();
		writer.writeStartElement(app, "b");
		writer.writeStartElement(nix, "c");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.close();
	}

	private XMLStreamWriter getWriter(List<String> paths, OutputStream stream) throws Exception {
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
		writer = new IndentingXMLStreamWriter(writer);
		List<XPath> xpaths = new ArrayList<XPath>();
		for (String s : paths) {
			xpaths.add(new XPath(s, nsBindings));
		}
		writer = new FilteringXMLStreamWriter(writer, xpaths);
		return writer;
	}

	@Test
	public void testFilteringOneXPath() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("/app:a/app:b/nix:c/app:b");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter writer = getWriter(list, bos);
		writeDocument(writer);
		String actual = bos.toString();
		String expected = IOUtils
			.toString(FilteringXMLStreamWriterTest.class.getResourceAsStream("filteringxpathone.xml"), UTF_8);
		assertThat(actual, CompareMatcher.isSimilarTo(expected).ignoreWhitespace());
	}

	@Test
	public void testFilteringMultipleXPaths() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("/app:a/app:b/nix:c/app:d");
		list.add("/app:a/app:b/nix:c/app:b");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter writer = getWriter(list, bos);
		writeDocument(writer);
		String actual = bos.toString();
		String expected = IOUtils
			.toString(FilteringXMLStreamWriterTest.class.getResourceAsStream("filteringxpathmultiple.xml"), UTF_8);
		assertThat(actual, CompareMatcher.isSimilarTo(expected).ignoreWhitespace());
	}

	@Test
	public void testFilteringMultipleXPathsWithText() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("/app:a/app:b/nix:c/alles:e");
		list.add("/app:a/app:b/nix:c/app:b");
		list.add("/app:a/app:b/nix:c/falsch:d");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter writer = getWriter(list, bos);
		writeDocument(writer);
		String actual = bos.toString();
		String expected = IOUtils.toString(
				FilteringXMLStreamWriterTest.class.getResourceAsStream("filteringxpathmultiplewithtext.xml"), UTF_8);
		assertThat(actual, CompareMatcher.isSimilarTo(expected).ignoreWhitespace());
	}

	@Test(expected = XMLStreamException.class)
	public void testFilteringOneXPathWithoutMatchingRootElement() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("/ap:a/app:c");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLStreamWriter writer = getWriter(list, bos);
		writeDocument(writer);
	}

	@Test
	public void testFilteringXPathSetPrefixBug() throws Exception {
		final XMLAdapter input = new XMLAdapter(
				FilteringXMLStreamWriterTest.class.getResourceAsStream("filtering_xpath_set_prefix.xml"));
		final List<String> list = new ArrayList<>();
		list.add("/app:a/nix:d");
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final XMLStreamWriter writer = getWriter(list, bos);
		input.getRootElement().serialize(writer);
		writer.close();
		String actual = bos.toString();
		String expected = IOUtils.toString(
				FilteringXMLStreamWriterTest.class.getResourceAsStream("filtering_xpath_set_prefix_expected.xml"),
				UTF_8);
		assertThat(actual, CompareMatcher.isSimilarTo(expected).ignoreWhitespace());
	}

}
