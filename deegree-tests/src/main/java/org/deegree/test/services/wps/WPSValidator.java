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

package org.deegree.test.services.wps;

import java.io.File;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.test.services.OGCValidator;
import org.deegree.test.services.util.HTTPResponseValidator;
import org.deegree.test.services.util.XPathAsserter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is an integration test for OGC WPS services. It is analogous to the
 * org.deegree.services.sos.SOSValidator
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 *
 */
public class WPSValidator extends OGCValidator {

	private static final String serviceURL = "http://localhost:8080/d3_services/services?";

	private static final String schemaLocation = "file:///home/padberg/workspace/opengis/wps/1.0.0/wpsAll.xsd";

	private static final HTTPResponseValidator SERVICE_EXCEPTION = new HTTPResponseValidator() {
		{
			responseCode = 400;
			contentType = "application/vnd.ogc.se_xml";
		}
	};

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void init() throws Exception {
		NamespaceContext ctxt = CommonNamespaces.getNamespaceContext();
		ctxt.addNamespace("wps", "http://www.opengis.net/wps/1.0.0");
		ctxt.addNamespace("ows", "http://www.opengis.net/ows/1.1");
		ctxt.addNamespace("xlink", XLN_NS);
		setNSContext(ctxt);
		setSchemaDoc(schemaLocation);
	}

	/**
	 * Test a WPS GetCapabilities KVP request
	 */
	@Test
	public void testGetCapabilitiesKVP() {
		String url = serviceURL + "SERVICE=WPS&VERSION=1.0.0&REQUEST=GetCapabilities";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertElements("/wps:Capabilities/wps:ProcessOfferings/wps:Process");
		a.assertStringNode("1.0.0", "/wps:Capabilities/@version");
		a.assertStringNode("deegree 3 WPS", "/wps:Capabilities/ows:ServiceIdentification/ows:Title");
	}

	/**
	 * Test a WPS GetCapabilities KVP request w/ wrong version.
	 */
	@Test
	public void testGetCapabilitiesKVPWrongVersion() {
		String url = serviceURL + "SERVICE=WPS&ACCEPTVERSIONS=2.0.0&REQUEST=GetCapabilities";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("VersionNegotiationFailed", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

	/**
	 * Test a WPS GetCapabilities POST request
	 */
	@Test
	public void testGetCapabilitiesPOST() {
		String url = serviceURL;
		File postFile = getFile("GetCapabilities.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile);
		a.assertElements("/wps:Capabilities/wps:ProcessOfferings/wps:Process");
		a.assertStringNode("1.0.0", "/wps:Capabilities/@version");
		a.assertStringNode("deegree 3 WPS", "/wps:Capabilities/ows:ServiceIdentification/ows:Title");
	}

	/**
	 * Test a WPS GetCapabilities POST request w/ wrong version.
	 */
	// @Test
	// public void testGetCapabilitiesPOSTWrongVersion() {
	// String url = serviceURL;
	// File postFile = getFile( "GetCapabilitiesWrongVersion.xml", this.getClass() );
	// XPathAsserter a = getAsserterForValidDoc( url, postFile, SERVICE_EXCEPTION );
	// a.assertStringNode( "VersionNegotiationFailed",
	// "/ows:ExceptionReport/ows:Exception/@exceptionCode" );
	// a.assertStringNode( "1.1.0", "/ows:ExceptionReport/@version" );
	// a.assertStringNode( "/ows:ExceptionReport/ows:Exception/ows:ExceptionText" );
	// }

	/**
	 * Test an invalid WPS POST request.
	 */
	@Test
	public void testInvalidPOSTRequest() {
		String url = serviceURL;
		File postFile = getFile("InvalidRequest.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile, SERVICE_EXCEPTION);
		a.assertStringNode("OperationNotSupported", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

	/**
	 * Test a WPS DescribeProcess KVP request
	 */
	@Test
	public void testDescribeProcess() {
		String url = serviceURL + "SERVICE=WPS&IDENTIFIER=ExampleProcess&VERSION=1.0.0&REQUEST=DescribeProcess";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertStringNode("/wps:ProcessDescriptions");
	}

	/**
	 * Test a WPS KVP request with invalid REQUEST value
	 */
	@Test
	public void testInvalidParameter() {
		String url = serviceURL + "SERVICE=WPS&VERSION=1.0.0&REQUEST=GetCapa";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("OperationNotSupported", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

}
