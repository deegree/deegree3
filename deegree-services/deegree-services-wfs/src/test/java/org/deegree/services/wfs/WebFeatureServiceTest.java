/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wfs;

import static org.deegree.protocol.wfs.WFSRequestType.DescribeFeatureType;
import static org.deegree.protocol.wfs.WFSRequestType.GetCapabilities;
import static org.deegree.protocol.wfs.WFSRequestType.GetFeature;
import static org.deegree.protocol.wfs.WFSRequestType.GetPropertyValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.deegree.protocol.wfs.WFSRequestType;
import org.deegree.services.encoding.LimitedSupportedEncodings;
import org.deegree.services.encoding.SupportedEncodings;
import org.deegree.services.encoding.UnlimitedSupportedEncodings;
import org.deegree.services.jaxb.wfs.DeegreeWFS;
import org.deegree.services.jaxb.wfs.DeegreeWFS.SupportedRequests;
import org.deegree.services.jaxb.wfs.RequestType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class WebFeatureServiceTest {

	private final WebFeatureService webFeatureService = new WebFeatureService(null, null, null);

	@Test
	public void testParseEncodingsWithRequestTypeSpecific() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithRequestTypeSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(supportedRequests);
		Map<WFSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> capabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(capabilitiesEncodings, hasOnlyItems("xml", "soap"));

		Set<String> describeFeatureTypeEncodings = enabledEncodings.get(DescribeFeatureType);
		assertThat(describeFeatureTypeEncodings, hasOnlyItems("xml"));

		Set<String> getFeatureEncodings = enabledEncodings.get(GetFeature);
		assertThat(getFeatureEncodings, hasOnlyItems("xml"));

		Set<String> getPropertyValueEncodings = enabledEncodings.get(GetPropertyValue);
		assertThat(getPropertyValueEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithKvpForAll() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithKvpForAll();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(supportedRequests);
		Map<WFSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> capabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(capabilitiesEncodings, hasOnlyItems("kvp", "xml", "soap"));

		Set<String> describeFeatureTypeEncodings = enabledEncodings.get(DescribeFeatureType);
		assertThat(describeFeatureTypeEncodings, hasOnlyItems("kvp", "xml"));

		Set<String> getFeatureEncodings = enabledEncodings.get(GetFeature);
		assertThat(getFeatureEncodings, hasOnlyItems("kvp", "xml"));

		Set<String> getPropertyValueEncodings = enabledEncodings.get(GetPropertyValue);
		assertThat(getPropertyValueEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithKvpForAllAndNoRequestTypesSpecific() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithKvpForAllAndNoRequestTypesSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(supportedRequests);
		Map<WFSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> capabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(capabilitiesEncodings, hasOnlyItems("kvp"));

		Set<String> describeFeatureTypeEncodings = enabledEncodings.get(DescribeFeatureType);
		assertThat(describeFeatureTypeEncodings, hasOnlyItems("kvp"));

		Set<String> getFeatureEncodings = enabledEncodings.get(GetFeature);
		assertThat(getFeatureEncodings, hasOnlyItems("kvp"));

		Set<String> getPropertyValueEncodings = enabledEncodings.get(GetPropertyValue);
		assertThat(getPropertyValueEncodings, hasOnlyItems("kvp"));
	}

	@Test
	public void testParseEncodingsWithKvpForAllAndEmptyRequestTypeSpecific() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithKvpForAllAndEmptyRequestTypeSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(supportedRequests);
		Map<WFSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> capabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(capabilitiesEncodings, hasOnlyItems("kvp"));
		assertThat(capabilitiesEncodings, not(hasOnlyItems("xml", "soap")));

		Set<String> describeFeatureTypeEncodings = enabledEncodings.get(DescribeFeatureType);
		assertThat(describeFeatureTypeEncodings, hasOnlyItems("kvp"));
		assertThat(capabilitiesEncodings, not(hasOnlyItems("xml", "soap")));

		Set<String> getFeatureEncodings = enabledEncodings.get(GetFeature);
		assertThat(getFeatureEncodings.size(), is(0));

		Set<String> getPropertyValueEncodings = enabledEncodings.get(GetPropertyValue);
		assertThat(getPropertyValueEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithEmptyAndSupportedRequestTypeSpecific() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithEmptyAndSupportedRequestTypeSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(supportedRequests);
		Map<WFSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> capabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(capabilitiesEncodings, hasOnlyItems("xml", "soap"));

		Set<String> describeFeatureTypeEncodings = enabledEncodings.get(DescribeFeatureType);
		assertThat(describeFeatureTypeEncodings, hasOnlyItems("xml", "soap", "kvp"));

		Set<String> getFeatureEncodings = enabledEncodings.get(GetFeature);
		assertThat(getFeatureEncodings, hasOnlyItems("xml"));

		Set<String> getPropertyValueEncodings = enabledEncodings.get(GetPropertyValue);
		assertThat(getPropertyValueEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithEmptyRequestTypesSpecific() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithEmptyRequestTypesSpecific();

		LimitedSupportedEncodings limitedSupportedEncodings = (LimitedSupportedEncodings) webFeatureService
			.parseEncodings(supportedRequests);
		Map<WFSRequestType, Set<String>> enabledEncodings = limitedSupportedEncodings
			.getEnabledEncodingsPerRequestType();

		Set<String> capabilitiesEncodings = enabledEncodings.get(GetCapabilities);
		assertThat(capabilitiesEncodings, hasOnlyItems("kvp", "xml", "soap"));

		Set<String> getFeatureEncodings = enabledEncodings.get(GetFeature);
		assertThat(getFeatureEncodings, hasOnlyItems("kvp", "xml", "soap"));

		Set<String> describeFeatureTypeEncodings = enabledEncodings.get(DescribeFeatureType);
		assertThat(describeFeatureTypeEncodings.size(), is(0));

		Set<String> getPropertyValueEncodings = enabledEncodings.get(GetPropertyValue);
		assertThat(getPropertyValueEncodings.size(), is(0));
	}

	@Test
	public void testParseEncodingsWithoutRequestTypesSpecificAndKvp() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithoutRequestTypesSpecificAndKvp();

		SupportedEncodings unlimitedSupportedEncodings = webFeatureService.parseEncodings(supportedRequests);

		assertThat(unlimitedSupportedEncodings, CoreMatchers.instanceOf(UnlimitedSupportedEncodings.class));
	}

	@Test
	public void testParseEncodingsWithoutSupportedRequestConfiguration() {
		DeegreeWFS supportedRequests = prepareSupportedRequestsWithoutSupportedRequestConfiguration();

		SupportedEncodings unlimitedSupportedEncodings = webFeatureService.parseEncodings(supportedRequests);

		assertThat(unlimitedSupportedEncodings, CoreMatchers.instanceOf(UnlimitedSupportedEncodings.class));
	}

	private DeegreeWFS prepareSupportedRequestsWithRequestTypeSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("xml");
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("soap");

		supportedRequests.setDescribeFeatureType(new RequestType());
		supportedRequests.getDescribeFeatureType().getSupportedEncodings().add("xml");

		supportedRequests.setGetFeature(new RequestType());
		supportedRequests.getGetFeature().getSupportedEncodings().add("xml");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWFS prepareSupportedRequestsWithKvpForAll() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.getSupportedEncodings().add("kvp");

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("xml");
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("soap");

		supportedRequests.setDescribeFeatureType(new RequestType());
		supportedRequests.getDescribeFeatureType().getSupportedEncodings().add("xml");

		supportedRequests.setGetFeature(new RequestType());
		supportedRequests.getGetFeature().getSupportedEncodings().add("xml");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWFS prepareSupportedRequestsWithKvpForAllAndEmptyRequestTypeSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.getSupportedEncodings().add("kvp");

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.setDescribeFeatureType(new RequestType());
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWFS prepareSupportedRequestsWithEmptyAndSupportedRequestTypeSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();

		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("xml");
		supportedRequests.getGetCapabilities().getSupportedEncodings().add("soap");

		supportedRequests.setDescribeFeatureType(new RequestType());

		supportedRequests.setGetFeature(new RequestType());
		supportedRequests.getGetFeature().getSupportedEncodings().add("xml");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWFS prepareSupportedRequestsWithKvpForAllAndNoRequestTypesSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.getSupportedEncodings().add("kvp");
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWFS prepareSupportedRequestsWithEmptyRequestTypesSpecific() {
		SupportedRequests supportedRequests = new SupportedRequests();
		supportedRequests.setGetCapabilities(new RequestType());
		supportedRequests.setGetFeature(new RequestType());
		return mockDeegreeConfiguration(supportedRequests);
	}

	private DeegreeWFS prepareSupportedRequestsWithoutRequestTypesSpecificAndKvp() {
		return mockDeegreeConfiguration(new SupportedRequests());
	}

	private DeegreeWFS prepareSupportedRequestsWithoutSupportedRequestConfiguration() {
		return mockDeegreeConfiguration(null);
	}

	private DeegreeWFS mockDeegreeConfiguration(SupportedRequests supportedRequests) {
		DeegreeWFS mockedDeegreeWfs = mock(DeegreeWFS.class);
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