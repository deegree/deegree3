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

import org.deegree.test.services.util.XPathAsserter;
import org.junit.Test;

/**
 * This class contains integration tests for SOS OGC GetCapabilities requests.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class SOSGetCapabilitiesValidation extends SOSValidator {

	/**
	 * Test a SOS GetCapabilities KVP request
	 */
	@Test
	public void testGetCapabilitiesKVP() {
		String url = serviceURL + "SERVICE=SOS&REQUEST=GetCapabilities";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertElements("/sos:Capabilities/sos:Contents/sos:ObservationOfferingList/sos:ObservationOffering");
		a.assertStringNode("1.0.0", "/sos:Capabilities/@version");
		a.assertStringNode("deegree 3 SOS", "/sos:Capabilities/ows:ServiceIdentification/ows:Title");
	}

	/**
	 * Test a SOS GetCapabilities KVP request w/ wrong version.
	 */
	@Test
	public void testGetCapabilitiesKVPWrongVersion() {
		String url = serviceURL + "SERVICE=SOS&ACCEPTVERSIONS=2.0.0&REQUEST=GetCapabilities";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("VersionNegotiationFailed", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

	/**
	 * Test a SOS GetCapabilities POST request
	 */
	@Test
	public void testGetCapabilitiesPOST() {
		String url = serviceURL;
		File postFile = getFile("GetCapabilities.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile);
		a.assertElements("/sos:Capabilities/sos:Contents/sos:ObservationOfferingList/sos:ObservationOffering");
		a.assertStringNode("1.0.0", "/sos:Capabilities/@version");
		a.assertStringNode("deegree 3 SOS", "/sos:Capabilities/ows:ServiceIdentification/ows:Title");
	}

	/**
	 * Test a SOS GetCapabilities POST request with Sections
	 */
	@Test
	public void testGetCapabilitiesSections() {
		String url = serviceURL;
		File postFile = getFile("GetCapabilitiesSections.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile);
		a.assertElements(0, "/sos:Capabilities/sos:Contents");
		a.assertElements(0, "/sos:Capabilities/ows:OperationsMetadata");
		a.assertElements(0, "/sos:Capabilities/sos:Filter_Capabilities");
		a.assertElements("/sos:Capabilities/ows:ServiceIdentification");
		a.assertElements("/sos:Capabilities/ows:ServiceProvider");
	}

	/**
	 * Test a SOS GetCapabilities POST request w/ wrong version.
	 */
	@Test
	public void testGetCapabilitiesPOSTWrongVersion() {
		String url = serviceURL;
		File postFile = getFile("GetCapabilitiesWrongVersion.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile, SERVICE_EXCEPTION);
		a.assertStringNode("VersionNegotiationFailed", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

}
