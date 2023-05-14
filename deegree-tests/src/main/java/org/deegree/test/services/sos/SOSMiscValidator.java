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
package org.deegree.test.services.sos;

import java.io.File;

import org.deegree.test.services.util.HTTPResponseValidator;
import org.deegree.test.services.util.XPathAsserter;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class contains various integration tests for the SOS OGC services.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class SOSMiscValidator extends SOSValidator {

	/**
	 * Test a SOS GetObservation KVP request
	 */
	@Test
	public void testDescribeSensor() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=DescribeSensor&PROCEDURE=urn:ogc:object:Sensor:latlon:foobarnator";
		XPathAsserter a = getAsserterForValidDoc(url, new HTTPResponseValidator() {
			@Override
			public void validateHeaders() {
				String ctype = getHeader("Content-type");
				if (ctype.equals("text/xml") || ctype.equals("text/xml;subtype=sensorML/1.0.1")) {
					return;
				}
				Assert.fail("invalid Content-type (" + ctype + ")");
			}
		});
		a.assertElements("/sml:SensorML/sml:identification");
	}

	/**
	 * Test a SOS GetObservation POST request
	 */
	@Test
	public void testDescribeSensorXML() {
		String url = serviceURL;
		File postFile = getFile("DescribeSensor.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile, new HTTPResponseValidator() {
			@Override
			public void validateHeaders() {
				String ctype = getHeader("Content-type");
				if (ctype.equals("text/xml") || ctype.equals("text/xml;subtype=sensorML/1.0.1")) {
					return;
				}
				Assert.fail("invalid Content-type (" + ctype + ")");
			}
		});
		a.assertElements("/sml:SensorML/sml:identification");
	}

	/**
	 * Test a SOS KVP request with invalid REQUEST value
	 */
	@Test
	public void testInvalidParameter() {
		String url = serviceURL + "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetCapa";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("OperationNotSupported", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

	/**
	 * Test an invalid SOS POST request.
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
	 * Test a broken SOS POST request.
	 */
	@Test
	public void testBrokenPOSTRequest() {
		String url = serviceURL;
		File postFile = getFile("BrokenRequest.invalid_xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile, SERVICE_EXCEPTION);
		// TODO check for service independent exception
		a.assertStringNode("NoApplicableCode", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

}
