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
package org.deegree.test.services;

import java.io.IOException;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.test.services.util.HTTPResponseValidator;
import org.deegree.test.services.util.XPathAsserter;
import org.junit.Assert;

/**
 * This is just a demonstration on how to use the OGCValidator, this is <b>NOT</b> a real
 * test. Read the comments!
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */

@SuppressWarnings("unused")
// ### remove this SuppressWarnning
// ### extend from OGCValidator
public class ValidatorTemplate extends OGCValidator {

	// ### some constants
	// ### you can also use OGCValidator.getProperty(String)
	private static final String serviceURL = "http://localhost:8080/deegree/services?";

	private static final String schemaLocation = "../../../../resources/schemas/opengis/XXX/1.0.0/all.xsd";

	// ### the init method
	/**
	 * @throws Exception
	 */
	// @BeforeClass //### uncomment this line
	public static void init() throws Exception {
		// ### set the namespace for this service
		NamespaceContext ctxt = CommonNamespaces.getNamespaceContext();
		ctxt.addNamespace("ows", "http://www.opengis.net/ows/1.1");
		ctxt.addNamespace("xlink", XLN_NS);
		ctxt.addNamespace("xxx", "http://www.opengis.net/xxxxx/1.0.0");
		setNSContext(ctxt);
		// ### set the used schema
		setSchemaDoc(schemaLocation);
	}

	// ### and now some tests

	/**
	 * @throws IOException
	 */
	// @Test // ### uncomment this line
	public void testGetCapabilities() throws IOException {
		// ### the request url
		String url = serviceURL + "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetCapabilities";
		// ### retrieve the URL, validate it and get an XPathAsserter
		XPathAsserter a = getAsserterForValidDoc(url);

		// ### do some checks on the result
		a.assertElements("/sos:Capabilities/sos:Contents/sos:ObservationOfferingList/sos:ObservationOffering");
		a.assertStringNode("1.0.0", "/sos:Capabilities/@version");
		a.assertStringNode("deegree 3 SOS", "/sos:Capabilities/ows:ServiceIdentification/ows:Title");
	}

	/**
	 * @throws IOException
	 */
	// @Test // ### uncomment this line
	public void testMissingParameter() throws IOException {

		String url = serviceURL + "SERVICE=SOS&VERSION=1.0.0";
		// ### the request parameter is missing
		// ### so we won't get a 200 response code

		// ### you have some options to change the HTTP response validation:
		// ### 1. ignore it
		XPathAsserter a = getAsserterForValidDoc(url, HTTPResponseValidator.NONE);
		// ### 2. change the values
		XPathAsserter b = getAsserterForValidDoc(url, new HTTPResponseValidator() {
			{
				responseCode = 400;
				contentType = "application/vnd.ogc.se_xml";
			}
		});
		// ### 3. or even override the validation methods
		XPathAsserter c = getAsserterForValidDoc(url, new HTTPResponseValidator() {
			@Override
			public void validateStatus(int status) {
				Assert.assertTrue(400 <= status && status < 500);
			}

			@Override
			public void validateHeaders() {
				validateHeader("Content-type", "application/vnd.ogc.se_xml");
				// ### or get a header and do something with it
				Assert.assertTrue(getHeader("Content-type").endsWith("xml"));
			}
		});
	}

	// ### add more tests here

}
