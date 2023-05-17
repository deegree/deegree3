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
package org.deegree.test.services.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.XPath;

/**
 * This utility class allows easy xpath based validation of an XML document.
 *
 * <p>
 * It offers some assert methods that check for a given xpath expression. If the checks
 * fail, the methods will throw assert exceptions. Therefore this class should be used
 * within junit tests.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class XPathAsserter {

	private XMLAdapter adapter;

	private NamespaceContext ctxt;

	/**
	 * @param doc a reader for the xml document to validate
	 * @param url the url for the document,. used within xml error messages
	 * @param ctxt the namespace context for all xpath expressions
	 */
	public XPathAsserter(InputStream doc, String url, NamespaceContext ctxt) {
		try {
			adapter = new XMLAdapter(doc, url);
		}
		catch (XMLProcessingException e) {
			fail(e.getMessage());
		}
		this.ctxt = ctxt;
	}

	/**
	 * Test that the xpath resolves to a string node.
	 * @param xpath
	 */
	public void assertStringNode(String xpath) {
		String result = adapter.getNodeAsString(adapter.getRootElement(), new XPath(xpath, ctxt), null);
		assertNotNull("couldn't find element with xpath " + xpath, result);
		assertTrue("the string node is empty", result.length() > 0);
	}

	/**
	 * Test that the xpath resolves to a string node that is equal to the expected string.
	 * @param expected the expected value of the string node
	 * @param xpath
	 */
	public void assertStringNode(String expected, String xpath) {
		String result = adapter.getNodeAsString(adapter.getRootElement(), new XPath(xpath, ctxt), null);
		assertNotNull("couldn't find element with xpath " + xpath, result);
		assertEquals("expected another string", expected, result);
	}

	/**
	 * Test that the xpath resolves to one or more elements.
	 * @param xpath
	 */
	public void assertElements(String xpath) {
		List<OMElement> elems = adapter.getElements(adapter.getRootElement(), new XPath(xpath, ctxt));
		assertTrue("elements " + xpath + " not found", elems.size() > 0);
	}

	/**
	 * Test that the xpath resolves to the expected numbers of elements.
	 * @param expected
	 * @param xpath
	 */
	public void assertElements(int expected, String xpath) {
		List<OMElement> elems = adapter.getElements(adapter.getRootElement(), new XPath(xpath, ctxt));
		assertEquals(expected, elems.size());
	}

}
