/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2017 by:
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
package org.deegree.services.wms.utils;

import static org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.DescribeLayer;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetCapabilities;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetFeatureInfo;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.deegree.services.encoding.LimitedSupportedEncodings;
import org.deegree.services.encoding.SupportedEncodings;
import org.deegree.services.encoding.UnlimitedSupportedEncodings;
import org.deegree.services.jaxb.wms.DeegreeWMS;
import org.deegree.services.jaxb.wms.RequestType;
import org.deegree.services.jaxb.wms.DeegreeWMS.SupportedRequests;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SupportedEncodingsParserTest {

	private final SupportedEncodingsParser webFeatureService = new SupportedEncodingsParser();

	@Test
	public void testParseEncodingsWithRequestTypeSpecific() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithRequestTypeSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(deegreeWmsConfig);
		Map<WMSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> getCapabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(getCapabilitiesEncodings, hasOnlyItems("xml", "soap"));

		Set<String> describeLayerEncodings = enabledEncodings.get(DescribeLayer);
		assertThat(describeLayerEncodings, hasOnlyItems("xml"));

		Set<String> getMapEncodings = enabledEncodings.get(GetMap);
		assertThat(getMapEncodings, hasOnlyItems("xml"));
		Set<String> mapEncodings = enabledEncodings.get(map);
		assertThat(mapEncodings, hasOnlyItems("xml"));

		Set<String> getFeatureInfoEncodings = enabledEncodings.get(GetFeatureInfo);
		assertThat(getFeatureInfoEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithKvpForAll() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithKvpForAll();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(deegreeWmsConfig);
		Map<WMSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> getCapabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(getCapabilitiesEncodings, hasOnlyItems("kvp", "xml", "soap"));

		Set<String> describeLayerEncodings = enabledEncodings.get(DescribeLayer);
		assertThat(describeLayerEncodings, hasOnlyItems("kvp", "xml"));

		Set<String> getMapEncodings = enabledEncodings.get(GetMap);
		assertThat(getMapEncodings, hasOnlyItems("kvp", "xml"));
		Set<String> mapEncodings = enabledEncodings.get(map);
		assertThat(mapEncodings, hasOnlyItems("kvp", "xml"));

		Set<String> getFeatureInfoEncodings = enabledEncodings.get(GetFeatureInfo);
		assertThat(getFeatureInfoEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithKvpForAllAndNoRequestTypesSpecific() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithKvpForAllAndNoRequestTypesSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(deegreeWmsConfig);
		Map<WMSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> getCapabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(getCapabilitiesEncodings, hasOnlyItems("kvp"));

		Set<String> describeLayerEncodings = enabledEncodings.get(DescribeLayer);
		assertThat(describeLayerEncodings, hasOnlyItems("kvp"));

		Set<String> getMapEncodings = enabledEncodings.get(GetMap);
		assertThat(getMapEncodings, hasOnlyItems("kvp"));
		Set<String> mapEncodings = enabledEncodings.get(map);
		assertThat(mapEncodings, hasOnlyItems("kvp"));

		Set<String> getFeatureInfoEncodings = enabledEncodings.get(GetFeatureInfo);
		assertThat(getFeatureInfoEncodings, hasOnlyItems("kvp"));
	}

	@Test
	public void testParseEncodingsWithKvpForAllAndEmptyRequestTypeSpecific() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithKvpForAllAndEmptyRequestTypeSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(deegreeWmsConfig);
		Map<WMSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> getCapabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(getCapabilitiesEncodings, hasOnlyItems("kvp"));
		assertThat(getCapabilitiesEncodings, not(hasOnlyItems("xml", "soap")));

		Set<String> describeLayerEncodings = enabledEncodings.get(DescribeLayer);
		assertThat(describeLayerEncodings, hasOnlyItems("kvp"));
		assertThat(getCapabilitiesEncodings, not(hasOnlyItems("xml", "soap")));

		Set<String> getMapEncodings = enabledEncodings.get(GetMap);
		assertThat(getMapEncodings.size(), is(0));
		Set<String> mapEncodings = enabledEncodings.get(map);
		assertThat(mapEncodings.size(), is(0));

		Set<String> getFeatureInfoEncodings = enabledEncodings.get(GetFeatureInfo);
		assertThat(getFeatureInfoEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithEmptyAndSupportedRequestTypeSpecific() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithEmptyAndSupportedRequestTypeSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(deegreeWmsConfig);
		Map<WMSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> getCapabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(getCapabilitiesEncodings, hasOnlyItems("xml", "soap"));

		Set<String> describeLayerEncodings = enabledEncodings.get(DescribeLayer);
		assertThat(describeLayerEncodings, hasOnlyItems("xml", "soap", "kvp"));

		Set<String> getMapEncodings = enabledEncodings.get(GetMap);
		assertThat(getMapEncodings, hasOnlyItems("xml"));
		Set<String> mapEncodings = enabledEncodings.get(map);
		assertThat(mapEncodings, hasOnlyItems("xml"));

		Set<String> getFeatureInfoEncodings = enabledEncodings.get(GetFeatureInfo);
		assertThat(getFeatureInfoEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithEmptyRequestTypesSpecific() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithEmptyRequestTypesSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(deegreeWmsConfig);
		Map<WMSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> getCapabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(getCapabilitiesEncodings, hasOnlyItems("kvp", "xml", "soap"));

		Set<String> getMapEncodings = enabledEncodings.get(GetMap);
		assertThat(getMapEncodings, hasOnlyItems("kvp", "xml", "soap"));
		Set<String> mapEncodings = enabledEncodings.get(map);
		assertThat(mapEncodings, hasOnlyItems("kvp", "xml", "soap"));

		Set<String> describeLayerEncodings = enabledEncodings.get(DescribeLayer);
		assertThat(describeLayerEncodings.size(), is(0));

		Set<String> getFeatureInfoEncodings = enabledEncodings.get(GetFeatureInfo);
		assertThat(getFeatureInfoEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithoutRequestTypesSpecificAndKvp() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithoutRequestTypesSpecificAndKvp();

		SupportedEncodings unlimitedSupportedEncodings = webFeatureService.parseEncodings(deegreeWmsConfig);

		assertThat(unlimitedSupportedEncodings, CoreMatchers.instanceOf(UnlimitedSupportedEncodings.class));
	}

	@Test
	public void testParseEncodingsWithoutSupportedRequestConfiguration() {
		DeegreeWMS deegreeWmsConfig = prepareSupportedRequestsWithoutSupportedRequestConfiguration();

		SupportedEncodings unlimitedSupportedEncodings = webFeatureService.parseEncodings(deegreeWmsConfig);

		assertThat(unlimitedSupportedEncodings, CoreMatchers.instanceOf(UnlimitedSupportedEncodings.class));
	}

	private DeegreeWMS prepareSupportedRequestsWithRequestTypeSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("xml");
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("soap");

		supportedRequests.setDescribeLayer(new RequestType());
		supportedRequests.getDescribeLayer().getSupportedEncodings().add("xml");

		supportedRequests.setGetMap(new RequestType());
		supportedRequests.getGetMap().getSupportedEncodings().add("xml");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWMS prepareSupportedRequestsWithKvpForAll() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.getSupportedEncodings().add("kvp");

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("xml");
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("soap");

		supportedRequests.setDescribeLayer(new RequestType());
		supportedRequests.getDescribeLayer().getSupportedEncodings().add("xml");

		supportedRequests.setGetMap(new RequestType());
		supportedRequests.getGetMap().getSupportedEncodings().add("xml");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWMS prepareSupportedRequestsWithKvpForAllAndEmptyRequestTypeSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.getSupportedEncodings().add("kvp");

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.setDescribeLayer(new RequestType());
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWMS prepareSupportedRequestsWithEmptyAndSupportedRequestTypeSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("xml");
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("soap");

		supportedRequests.setDescribeLayer(new RequestType());

		supportedRequests.setGetMap(new RequestType());
		supportedRequests.getGetMap().getSupportedEncodings().add("xml");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWMS prepareSupportedRequestsWithKvpForAllAndNoRequestTypesSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.getSupportedEncodings().add("kvp");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWMS prepareSupportedRequestsWithEmptyRequestTypesSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.setGetMap(new RequestType());
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWMS prepareSupportedRequestsWithoutRequestTypesSpecificAndKvp() {
		return mockDeegreeConfiguration(new SupportedRequests());
	}

	private DeegreeWMS prepareSupportedRequestsWithoutSupportedRequestConfiguration() {
		return mockDeegreeConfiguration(null);
	}

	private DeegreeWMS mockDeegreeConfiguration(SupportedRequests supportedRequests) {
		DeegreeWMS mockedDeegreeWfs = mock(DeegreeWMS.class);
		when(mockedDeegreeWfs.getSupportedRequests()).thenReturn(supportedRequests);
		return mockedDeegreeWfs;
	}

	@SuppressWarnings("unchecked")
	private <T> Matcher<Collection<T>> hasOnlyItems(final T... items) {
		return new BaseMatcher<Collection<T>>() {

			@Override
			public boolean matches(Object item) {
				Collection<T> list = (Collection<T>) item;
				if (list.size() != items.length)
					return false;
				for (T expectedItem : items) {
					if (!list.contains(expectedItem))
						return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a collection containing exactly");
			}
		};
	}

}
