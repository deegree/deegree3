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
package org.deegree.protocol.wfs.client;

import static junit.framework.Assert.assertNotNull;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.protocol.wfs.WFSVersion.WFS_100;
import static org.deegree.protocol.wfs.WFSVersion.WFS_110;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.party.Address;
import org.deegree.commons.ows.metadata.party.ContactInfo;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.feature.Feature;
import org.deegree.feature.types.AppSchema;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the {@link WFSClient} against various WFS server instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WFSClientTest {

	private static Logger LOG = LoggerFactory.getLogger(WFSClientTest.class);

	private static final String WFS_UTAH_DEMO_100_URL = "wfs.utahdemo100.url";

	private static final String WFS_UTAH_DEMO_110_URL = "wfs.utahdemo110.url";

	@Test
	public void testCapabilitiesExtraction100() throws Exception {

		String wfsUtahDemo100Url = TestProperties.getProperty(WFS_UTAH_DEMO_100_URL);
		if (wfsUtahDemo100Url == null) {
			LOG.warn("Skipping test, property '" + WFS_UTAH_DEMO_100_URL + "' not found in ~/.deegree-test.properties");
			return;
		}

		URL wfsCapaUrl = new URL(wfsUtahDemo100Url);
		WFSClient client = new WFSClient(wfsCapaUrl);
		assertEquals(WFS_100, client.getServiceVersion());

		// ServiceIdentification
		ServiceIdentification si = client.getIdentification();
		assertEquals("deegree 3 Utah Demo", si.getName());
		assertEquals(1, si.getTitles().size());
		assertEquals("deegree 3 Utah Demo", si.getTitles().get(0).getString());
		assertEquals(null, si.getTitles().get(0).getLanguage());
		assertEquals(1, si.getAbstracts().size());
		assertEquals("WMS and WFS demonstration with Utah data", si.getAbstracts().get(0).getString());
		assertEquals(null, si.getAbstracts().get(0).getLanguage());
		assertEquals(0, si.getKeywords().size());
		assertNull(si.getFees());

		// ServiceProvider
		assertEquals("http://www.lat-lon.de", client.getProvider().getProviderSite());

		// OperationMetadata (TODO)
	}

	@Test
	public void testCapabilitiesExtraction110() throws Exception {

		String wfsUtahDemo110Url = TestProperties.getProperty(WFS_UTAH_DEMO_110_URL);
		if (wfsUtahDemo110Url == null) {
			LOG.warn("Skipping test, property '" + WFS_UTAH_DEMO_110_URL + "' not found in ~/.deegree-test.properties");
			return;
		}

		URL wfsCapaUrl = new URL(wfsUtahDemo110Url);
		WFSClient client = new WFSClient(wfsCapaUrl);
		assertEquals(WFS_110, client.getServiceVersion());

		// ServiceIdentification
		ServiceIdentification si = client.getIdentification();
		assertNull(si.getName());
		assertEquals(1, si.getTitles().size());
		assertEquals("deegree 3 Utah Demo", si.getTitles().get(0).getString());
		assertEquals(null, si.getTitles().get(0).getLanguage());
		assertEquals(1, si.getAbstracts().size());
		assertEquals("WMS and WFS demonstration with Utah data", si.getAbstracts().get(0).getString());
		assertEquals(null, si.getAbstracts().get(0).getLanguage());
		assertEquals(0, si.getKeywords().size());
		assertNull(si.getFees());

		// ServiceProvider
		assertEquals("lat/lon GmbH", client.getProvider().getProviderName());
		assertEquals("http://www.lat-lon.de", client.getProvider().getProviderSite());
		ResponsibleParty sc = client.getProvider().getServiceContact();
		assertEquals("Andreas Schmitz", sc.getIndividualName());
		assertEquals("Software developer", sc.getPositionName());
		assertEquals("PointOfContact", sc.getRole().getCode());
		ContactInfo ci = sc.getContactInfo();
		assertEquals("http://www.deegree.org", ci.getOnlineResource().toString());
		assertEquals("24x7", ci.getHoursOfService());
		assertEquals("Do not hesitate to call", ci.getContactInstruction());
		Address add = ci.getAddress();
		assertEquals("NRW", add.getAdministrativeArea());
		assertEquals("Bonn", add.getCity());
		assertEquals("Germany", add.getCountry());
		assertEquals(1, add.getDeliveryPoint().size());
		assertEquals("Aennchenstr. 19", add.getDeliveryPoint().get(0));
		assertEquals(1, add.getElectronicMailAddress().size());
		assertEquals("info@lat-lon.de", add.getElectronicMailAddress().get(0));
		assertEquals("53177", add.getPostalCode());

		// OperationMetadata (TODO)
	}

	@Test
	public void testGetAppSchema100() throws Exception {

		String wfsUtahDemo110Url = TestProperties.getProperty(WFS_UTAH_DEMO_100_URL);
		if (wfsUtahDemo110Url == null) {
			LOG.warn("Skipping test, property '" + WFS_UTAH_DEMO_100_URL + "' not found in ~/.deegree-test.properties");
			return;
		}

		URL wfsCapaUrl = new URL(wfsUtahDemo110Url);
		WFSClient client = new WFSClient(wfsCapaUrl);
		assertEquals(WFS_100, client.getServiceVersion());

		AppSchema appSchema = client.getAppSchema();
		// TODO should be GML 2
		assertEquals(GML_31, appSchema.getGMLSchema().getVersion());
		assertEquals(18, appSchema.getFeatureTypes().length);
	}

	@Test
	public void testGetAppSchema110() throws Exception {

		String wfsUtahDemo110Url = TestProperties.getProperty(WFS_UTAH_DEMO_110_URL);
		if (wfsUtahDemo110Url == null) {
			LOG.warn("Skipping test, property '" + WFS_UTAH_DEMO_110_URL + "' not found in ~/.deegree-test.properties");
			return;
		}

		URL wfsCapaUrl = new URL(wfsUtahDemo110Url);
		WFSClient client = new WFSClient(wfsCapaUrl);
		assertEquals(WFS_110, client.getServiceVersion());

		AppSchema appSchema = client.getAppSchema();
		assertEquals(GML_31, appSchema.getGMLSchema().getVersion());
		assertEquals(18, appSchema.getFeatureTypes().length);
	}

	@Test
	public void testGetFeature110() throws Exception {

		String wfsUtahDemo110Url = TestProperties.getProperty(WFS_UTAH_DEMO_110_URL);
		if (wfsUtahDemo110Url == null) {
			LOG.warn("Skipping test, property '" + WFS_UTAH_DEMO_110_URL + "' not found in ~/.deegree-test.properties");
			return;
		}

		URL wfsCapaUrl = new URL(wfsUtahDemo110Url);
		WFSClient client = new WFSClient(wfsCapaUrl);
		assertEquals(WFS_110, client.getServiceVersion());

		GetFeatureResponse<Feature> resp = client
			.getFeatures(QName.valueOf("{http://www.deegree.org/app}SGID024_StateBoundary"), null);
		int i = 0;
		try {
			WFSFeatureCollection<Feature> wfsFc = resp.getAsWFSFeatureCollection();
			Iterator<Feature> iter = wfsFc.getMembers();
			while (iter.hasNext()) {
				Feature f = iter.next();
				assertNotNull(f);
				i++;
			}
		}
		finally {
			resp.close();
		}
		assertEquals(2, i);
	}

	@Test
	public void testGetFeature110PropertyIsEqualToFilter() throws Exception {

		String wfsUtahDemo110Url = TestProperties.getProperty(WFS_UTAH_DEMO_110_URL);
		if (wfsUtahDemo110Url == null) {
			LOG.warn("Skipping test, property '" + WFS_UTAH_DEMO_110_URL + "' not found in ~/.deegree-test.properties");
			return;
		}

		URL wfsCapaUrl = new URL(wfsUtahDemo110Url);
		WFSClient client = new WFSClient(wfsCapaUrl);
		assertEquals(WFS_110, client.getServiceVersion());

		ValueReference propName = new ValueReference(new QName("http://www.deegree.org/app", "STATE", "app"));
		PrimitiveType pt = new PrimitiveType(BaseType.STRING);
		Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>(new PrimitiveValue("Utah", pt), null);
		Operator rootOperator = new PropertyIsEqualTo(propName, literal, true, null);
		Filter filter = new OperatorFilter(rootOperator);

		GetFeatureResponse<Feature> resp = null;
		try {
			resp = client.getFeatures(QName.valueOf("{http://www.deegree.org/app}SGID024_StateBoundary"), filter);
		}
		catch (Exception t) {
			t.printStackTrace();
			throw t;
		}

		int i = 0;
		try {
			WFSFeatureCollection<Feature> wfsFc = resp.getAsWFSFeatureCollection();
			Iterator<Feature> iter = wfsFc.getMembers();
			while (iter.hasNext()) {
				Feature f = iter.next();
				assertNotNull(f);
				i++;
			}
		}
		finally {
			resp.close();
		}
		assertEquals(1, i);
	}

}
