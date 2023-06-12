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
package org.deegree.commons.xml;

import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.AssertionFailedError;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.jaxen.JaxenException;
import org.junit.Before;
import org.junit.Test;

/**
 * Basic tests for the {@link XMLAdapter} class.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class XMLAdapterTest {

	private static String WFS_NS = "http://www.opengis.net/wfs";

	private static String APP_NS = "http://www.deegree.org/app";

	private NamespaceBindings nsContext;

	private XMLAdapter adapter;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		adapter = new XMLAdapter(XMLAdapterTest.class.getResourceAsStream("testdocument.xml"));
		nsContext = new NamespaceBindings();
		nsContext.addNamespace("ogc", "http://www.opengis.net/ogc");
		nsContext.addNamespace("gml", "http://www.opengis.net/gml");
		nsContext.addNamespace("wfs", "http://www.opengis.net/wfs");
		nsContext.addNamespace("app", "http://www.deegree.org/app");
	}

	@Test
	public void testRootElement() throws XMLStreamException {
		assertEquals(new QName(WFS_NS, "GetFeature"), adapter.getRootElement().getQName());
	}

	@Test
	public void testTypeNameAttribute() throws JaxenException {

		OMElement root = adapter.getRootElement();
		AXIOMXPath xpath = new AXIOMXPath("wfs:Query");
		xpath.addNamespace("wfs", WFS_NS);
		OMElement queryElement = (OMElement) xpath.selectSingleNode(root);
		OMAttribute typeNameAttr = queryElement.getAttribute(new QName("typeName"));
		QName ftName = queryElement.resolveQName(typeNameAttr.getAttributeValue());
		assertEquals(new QName(APP_NS, "Philosopher"), ftName);
	}

	@Test
	public void testGetNode() {

		OMElement root = adapter.getRootElement();

		// select text node
		Object textNode = adapter.getNode(root,
				new XPath("wfs:Query/ogc:Filter/ogc:BBOX/ogc:PropertyName/text()", nsContext));
		assertEquals("app:placeOfBirth/app:Place/app:country/app:Country/app:geom", ((OMText) textNode).getText());

		// select attribute node
		Object attributeNode = adapter.getNode(root, new XPath("wfs:Query/@typeName", nsContext));
		assertEquals("app:Philosopher", ((OMAttribute) attributeNode).getAttributeValue());
	}

	@Test
	public void testGetNodeAsString() {

		OMElement root = adapter.getRootElement();

		// select text node
		String textNode = adapter.getNodeAsString(root,
				new XPath("wfs:Query/ogc:Filter/ogc:BBOX/ogc:PropertyName/text()", nsContext), null);
		assertEquals("app:placeOfBirth/app:Place/app:country/app:Country/app:geom", textNode);

		// select attribute node
		QName attributeNode = adapter.getNodeAsQName(root, new XPath("wfs:Query/@typeName", nsContext), null);
		assertEquals(QName.valueOf("{http://www.deegree.org/app}Philosopher"), attributeNode);

		// select element node
		String elementNode = adapter.getNodeAsString(root,
				new XPath("wfs:Query/ogc:Filter/ogc:BBOX/gml:Envelope/gml:coord/gml:X", nsContext), null);
		assertEquals("-1", elementNode);
	}

	@Test(expected = XMLProcessingException.class)
	public void testGetRequiredNodeAsString() {

		OMElement root = adapter.getRootElement();
		String value = adapter.getRequiredNodeAsString(root,
				new XPath("wfs:Query/ogc:Filter/ogc:BBOX/ogc:PropertyName/text()", nsContext));
		assertEquals("app:placeOfBirth/app:Place/app:country/app:Country/app:geom", value);

		adapter.getRequiredNodeAsString(root, new XPath("wfs:Query/@doesNotExist", nsContext));
	}

	@Test(expected = XMLProcessingException.class)
	public void testGetRequiredNodeAsQName() {

		OMElement root = adapter.getRootElement();
		QName value = adapter.getRequiredNodeAsQName(root, new XPath("wfs:Query/@typeName", nsContext));
		assertEquals(new QName(APP_NS, "Philosopher"), value);

		adapter.getRequiredNodeAsQName(root, new XPath("wfs:Query/@doesNotExist", nsContext));
	}

	@Test
	public void testWriteElement() throws XMLStreamException {

		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		XMLStreamWriter writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(stringWriter));
		adapter.getRootElement().serializeAndConsume(writer);
	}

	@Test
	public void testWriteElement_SimpleNamespaceBinding() throws Exception {
		ByteArrayOutputStream writeIn = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(writeIn);

		String xmlToWrite = "<test:me xmlns:test='http://deegree.org/test' ><test:abc/></test:me>";
		XMLStreamReader readFrom = createReader(xmlToWrite);

		XMLAdapter.writeElement(writer, readFrom);
		writer.close();

		assertThatXmlIsReadable(writeIn);
	}

	@Test
	public void testWriteElement_NamespaceBindingToDefaultAndPrefix() throws Exception {
		ByteArrayOutputStream writeIn = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(writeIn);

		String xmlToWrite = "<test:me xmlns:test='http://deegree.org/test' xmlns='http://deegree.org/test' ><test:abc/></test:me>";
		XMLStreamReader readFrom = createReader(xmlToWrite);

		XMLAdapter.writeElement(writer, readFrom);
		writer.close();

		assertThatXmlIsReadable(writeIn);
	}

	private XMLStreamReader createReader(String s) throws XMLStreamException, FactoryConfigurationError {
		StringReader stringReader = new StringReader(s);
		XMLStreamReader inStream = XMLInputFactory.newInstance().createXMLStreamReader(stringReader);
		inStream.nextTag();
		return inStream;
	}

	private void assertThatXmlIsReadable(ByteArrayOutputStream stringWriter) {
		try {
			String writtenXml = new String(stringWriter.toByteArray());
			new XMLAdapter(new ByteArrayInputStream(writtenXml.getBytes()));
		}
		catch (Exception e) {
			throw new AssertionFailedError("XML is not readable!");
		}
	}

}
