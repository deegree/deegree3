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
 * This class contains integration tests for SOS OGC GetObservation requests.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
public class SOSGetObservationValidator extends SOSValidator {

	private static final String XPATH_FIRST_TIMEPOSITION = "/om:ObservationCollection/om:member[1]/om:Observation/om:samplingTime/gml:TimeInstant/gml:timePosition";

	private static final String XPATH_LAST_TIMEPOSITION = "/om:ObservationCollection/om:member[last()]/om:Observation/om:samplingTime/gml:TimeInstant/gml:timePosition";

	/**
	 * Test a SOS GetObservation KVP request
	 */
	@Test
	public void testGetObservation() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:temperature,urn:ogc:def:phenomenon:OGC:windspeed&TIME=2009-02-01/2009-02-02";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertStringNode("urn:ogc:object:Sensor:latlon:foobarnator", "//om:procedure/@xlink:href");
	}

	/**
	 * Test a SOS GetObservation KVP request with resultMode om:Measurement
	 */
	@Test
	public void testGetObservationAsMeasurements() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:temperature,urn:ogc:def:phenomenon:OGC:windspeed&TIME=2009-02-01/2009-02-02&RESULTMODEL=om:Measurement";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertStringNode("urn:ogc:object:Sensor:latlon:foobarnator",
				"/om:ObservationCollection/om:member[1]/om:Observation/om:procedure/@xlink:href");
		a.assertElements(48, "/om:ObservationCollection/om:member/om:Observation");
		a.assertElements(24,
				"/om:ObservationCollection/om:member/om:Observation/om:observedProperty[@xlink:href='urn:ogc:def:phenomenon:OGC:windspeed']");
		a.assertElements(24,
				"/om:ObservationCollection/om:member/om:Observation/om:observedProperty[@xlink:href='urn:ogc:def:phenomenon:OGC:temperature']");
		a.assertElements("/om:ObservationCollection/om:member");
	}

	/**
	 * Test a SOS GetObservation KVP request with resultMode om:Observation with one
	 * property
	 */
	@Test
	public void testGetObservationAsObservation() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:windspeed&TIME=2009-02-01/2009-02-02&RESULTMODEL=om:Observation";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertStringNode("urn:ogc:object:Sensor:latlon:foobarnator",
				"/om:ObservationCollection/om:member[1]/om:Observation/om:procedure/@xlink:href");
		a.assertStringNode("urn:ogc:def:phenomenon:OGC:windspeed",
				"/om:ObservationCollection/om:member[1]/om:Observation/om:observedProperty/@xlink:href");
		a.assertElements("/om:ObservationCollection/om:member");
	}

	/**
	 * Test a SOS GetObservation KVP request with resultMode om:Observation with two
	 * property
	 */
	@Test
	public void testGetObservationAsObservationComposedProperty() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:windspeed,urn:ogc:def:phenomenon:OGC:temperature&TIME=2009-02-01/2009-02-02&RESULTMODEL=om:Observation";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertStringNode("urn:ogc:object:Sensor:latlon:foobarnator",
				"/om:ObservationCollection/om:member[1]/om:Observation/om:procedure/@xlink:href");
		a.assertElements(2,
				"/om:ObservationCollection/om:member[1]/om:Observation/om:observedProperty/swe:CompositePhenomenon/swe:component");
		a.assertElements("/om:ObservationCollection/om:member");
	}

	/**
	 * Test a SOS GetObservation KVP request with invalid observedProperty
	 */
	@Test
	public void testGetObservationInvalidObservedProperty() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:windspeed,urn:ogc:def:phenomenon:OGC:invalid&TIME=2009-02-01/2009-02-02&RESULTMODEL=om:Observation";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("NoApplicableCode", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
	}

	/**
	 * Test a SOS GetObservation KVP request with invalid responseFormat
	 */
	@Test
	public void testGetObservationInvalidResponseFormat() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:windspeed,urn:ogc:def:phenomenon:OGC:invalid&TIME=2009-02-01/2009-02-02&RESPONSEFORMAT=invalid";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("InvalidParameterValue", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
		a.assertStringNode("responseFormat", "/ows:ExceptionReport/ows:Exception/@locator");
	}

	/**
	 * Test a SOS GetObservation KVP request with invalid responseMode
	 */
	@Test
	public void testGetObservationInvalidResponseMode() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:windspeed,urn:ogc:def:phenomenon:OGC:invalid&TIME=2009-02-01/2009-02-02&RESPONSEMODE=invalid";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("InvalidParameterValue", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
		a.assertStringNode("responseMode", "/ows:ExceptionReport/ows:Exception/@locator");
	}

	/**
	 * Test a SOS GetObservation KVP request with invalid resultModel
	 */
	@Test
	public void testGetObservationInvalidResultModel() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:windspeed,urn:ogc:def:phenomenon:OGC:invalid&TIME=2009-02-01/2009-02-02&RESULTMODEL=om:Invalid";
		XPathAsserter a = getAsserterForValidDoc(url, SERVICE_EXCEPTION);
		a.assertStringNode("InvalidParameterValue", "/ows:ExceptionReport/ows:Exception/@exceptionCode");
		a.assertStringNode("1.1.0", "/ows:ExceptionReport/@version");
		a.assertStringNode("/ows:ExceptionReport/ows:Exception/ows:ExceptionText");
		a.assertStringNode("resultModel", "/ows:ExceptionReport/ows:Exception/@locator");
	}

	/**
	 * Test a SOS GetObservation KVP request with an empty result
	 */
	@Test
	public void testGetObservationNoResults() {
		String url = serviceURL
				+ "SERVICE=SOS&VERSION=1.0.0&REQUEST=GetObservation&OFFERING=urn:MyOrg:offering:1&OBSERVEDPROPERTY=urn:ogc:def:phenomenon:OGC:temperature&TIME=2010-02-01";
		XPathAsserter a = getAsserterForValidDoc(url);
		a.assertStringNode("urn:ogc:def:nil:OGC:inapplicable", "/om:ObservationCollection/om:member/@xlink:href");
		a.assertElements(1, "/om:ObservationCollection/om:member");
	}

	/**
	 * Test request with TM_During
	 */
	@Test
	public void testTMDuringRequest() {
		String url = serviceURL;
		File postFile = getFile("GetObservation_TM_During.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile);
		a.assertElements(23, "/om:ObservationCollection/om:member");
		a.assertStringNode("2008-01-01T01:00:00.000Z", XPATH_FIRST_TIMEPOSITION);
		a.assertStringNode("2008-01-01T23:00:00.000Z", XPATH_LAST_TIMEPOSITION);
	}

	/**
	 * Test request with TM_Before
	 */
	@Test
	public void testTMBeforeRequest() {
		String url = serviceURL;
		File postFile = getFile("GetObservation_TM_Before.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile);
		a.assertElements(24, "/om:ObservationCollection/om:member");
		a.assertStringNode("2008-01-01T00:00:00.000Z", XPATH_FIRST_TIMEPOSITION);
		a.assertStringNode("2008-01-01T23:00:00.000Z", XPATH_LAST_TIMEPOSITION);
	}

	/**
	 * Test request with TM_After
	 */
	@Test
	public void testTMAfterRequest() {
		String url = serviceURL;
		File postFile = getFile("GetObservation_TM_After.xml", this.getClass());
		XPathAsserter a = getAsserterForValidDoc(url, postFile);
		a.assertElements(15, "/om:ObservationCollection/om:member");
		a.assertStringNode("2009-02-20T01:00:00.000Z", XPATH_FIRST_TIMEPOSITION);
		a.assertStringNode("2009-02-20T15:00:00.000Z", XPATH_LAST_TIMEPOSITION);
	}

}
