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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.services.encoding.LimitedSupportedEncodings;
import org.deegree.services.encoding.SupportedEncodings;
import org.deegree.services.encoding.UnlimitedSupportedEncodings;
import org.deegree.services.jaxb.wms.DeegreeWMS;
import org.deegree.services.jaxb.wms.RequestType;
import org.deegree.services.jaxb.wms.DeegreeWMS.SupportedRequests;

/**
 * Parses supported request and encodings from DeegreeWMS configuration.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SupportedEncodingsParser {

	/**
	 * Parse the supported request and encodings from DeegreeWMS configuration
	 * @param jaxbConfig the DeegreeWMS configuration to parse the encoding from, never
	 * <code>null</code>
	 * @return the parsed supported requests and encodings, never <code>null</code>
	 */
	public SupportedEncodings parseEncodings(DeegreeWMS jaxbConfig) {
		SupportedRequests supportedRequests = jaxbConfig.getSupportedRequests();
		if (supportedRequests != null) {
			if (isAtLeastOneRequestTypeConfigured(supportedRequests)
					|| isGlobalSupportedEncodingsConfigured(supportedRequests))
				return parseEncodings(supportedRequests);
		}
		return new UnlimitedSupportedEncodings();
	}

	private LimitedSupportedEncodings parseEncodings(SupportedRequests supportedRequests) {
		List<String> supportedEncodingsForAllRequestTypes = supportedRequests.getSupportedEncodings();
		if (isAtLeastOneRequestTypeConfigured(supportedRequests))
			return parseEncodingsWithSpecifiedRequestTypes(supportedRequests, supportedEncodingsForAllRequestTypes);
		else
			return parseEncodingWithSupportedEncodings(supportedEncodingsForAllRequestTypes);
	}

	private LimitedSupportedEncodings parseEncodingWithSupportedEncodings(
			List<String> supportedEncodingsForAllRequestTypes) {
		LimitedSupportedEncodings<WMSRequestType> limitedSupportedEncodings = new LimitedSupportedEncodings();
		for (WMSRequestType type : WMSRequestType.values()) {
			limitedSupportedEncodings.addEnabledEncodings(type,
					collectEnabledEncodings(supportedEncodingsForAllRequestTypes));
		}
		return limitedSupportedEncodings;
	}

	private LimitedSupportedEncodings parseEncodingsWithSpecifiedRequestTypes(SupportedRequests supportedRequests,
			List<String> supportedEncodingsForAllRequestTypes) {
		LimitedSupportedEncodings<WMSRequestType> limitedSupportedEncodings = new LimitedSupportedEncodings();

		for (WMSRequestType type : WMSRequestType.values()) {
			RequestType requestType = retrieveEncodings(supportedRequests, type);
			Set<String> enabledEncodingsPerRequestType = collectEnabledEncodings(requestType,
					supportedEncodingsForAllRequestTypes);
			limitedSupportedEncodings.addEnabledEncodings(type, enabledEncodingsPerRequestType);
		}

		return limitedSupportedEncodings;
	}

	private RequestType retrieveEncodings(SupportedRequests supportedRequests, WMSRequestType type) {
		switch (type) {
			case DescribeLayer:
				return supportedRequests.getDescribeLayer();
			case capabilities:
			case GetCapabilities:
				return supportedRequests.getGetCapabilities();
			case GetFeatureInfo:
				return supportedRequests.getGetFeatureInfo();
			case GetMap:
			case map:
				return supportedRequests.getGetMap();
			case GetFeatureInfoSchema:
				return supportedRequests.getGetFeatureInfoSchema();
			case GetLegendGraphic:
				return supportedRequests.getGetLegendGraphic();
			case DTD:
				return supportedRequests.getGetLegendGraphic();
			default:
				return null;
		}
	}

	private boolean isGlobalSupportedEncodingsConfigured(SupportedRequests supportedRequests) {
		List<String> supportedEncodingsForAllRequestTypes = supportedRequests.getSupportedEncodings();
		return supportedEncodingsForAllRequestTypes != null && !supportedEncodingsForAllRequestTypes.isEmpty();
	}

	private boolean isAtLeastOneRequestTypeConfigured(SupportedRequests supportedRequests) {
		return supportedRequests.getGetCapabilities() != null || supportedRequests.getGetMap() != null
				|| supportedRequests.getDescribeLayer() != null || supportedRequests.getGetFeatureInfo() != null
				|| supportedRequests.getGetFeatureInfoSchema() != null
				|| supportedRequests.getGetLegendGraphic() != null || supportedRequests.getDTD() != null;
	}

	private Set<String> collectEnabledEncodings(RequestType supportedEncodingsForThisType,
			List<String> supportedEncodingsForAllTypes) {
		Set<String> allEnabledEncodingForThisType = new HashSet<String>();
		if (supportedEncodingsForThisType != null) {
			allEnabledEncodingForThisType.addAll(supportedEncodingsForAllTypes);
			List<String> encodingsForThisType = supportedEncodingsForThisType.getSupportedEncodings();
			if (encodingsForThisType != null && encodingsForThisType.size() > 0) {
				allEnabledEncodingForThisType.addAll(encodingsForThisType);
			}
			else if (supportedEncodingsForAllTypes == null || supportedEncodingsForAllTypes.isEmpty()) {
				allEnabledEncodingForThisType.add("kvp");
				allEnabledEncodingForThisType.add("xml");
				allEnabledEncodingForThisType.add("soap");
			}
		}
		return allEnabledEncodingForThisType;
	}

	private Set<String> collectEnabledEncodings(List<String> supportedEncodingsForAllTypes) {
		Set<String> allEnabledEncodingForThisType = new HashSet<String>();
		allEnabledEncodingForThisType.addAll(supportedEncodingsForAllTypes);
		return allEnabledEncodingForThisType;
	}

}