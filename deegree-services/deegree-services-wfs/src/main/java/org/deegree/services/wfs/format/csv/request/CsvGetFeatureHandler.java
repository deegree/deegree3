/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
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
package org.deegree.services.wfs.format.csv.request;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.format.csv.CsvFeatureWriter;
import org.deegree.services.wfs.query.QueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CsvGetFeatureHandler {

	private static final Logger LOG = LoggerFactory.getLogger(CsvGetFeatureHandler.class);

	private final WebFeatureService webFeatureService;

	public CsvGetFeatureHandler(WebFeatureService webFeatureService) {
		this.webFeatureService = webFeatureService;
	}

	public void doGetFeatureResults(GetFeature request, HttpResponseBuffer response) throws Exception {
		QueryAnalyzer analyzer = new QueryAnalyzer(request.getQueries(), webFeatureService,
				webFeatureService.getStoreManager(), webFeatureService.getCheckAreaOfUse());
		response.setCharacterEncoding(Charset.defaultCharset().name());
		response.setContentType(determineMimeType(request));
		ICRS requestedCRS = analyzer.getRequestedCRS();
		int startIndex = getStartIndex(request);
		int maxFeatures = getMaxFeatures(request);

		// TODO: Lock lock = acquireLock( request, analyzer );

		int featuresAdded = 0;
		int featuresSkipped = 0;

		Map<FeatureStore, List<Query>> fsToQueries = analyzer.getQueries();
		assertExactOneQuery(fsToQueries);

		FeatureStore featureStore = fsToQueries.keySet().iterator().next();
		List<Query> queries = fsToQueries.values().iterator().next();
		FeatureInputStream rs = featureStore.query(queries.toArray(new Query[queries.size()]));
		AppSchema schema = featureStore.getSchema();
		QName featureTypeName = queries.get(0).getTypeNames()[0].getFeatureTypeName();
		FeatureType featureType = schema.getFeatureType(featureTypeName);
		CsvFeatureWriter csvStreamWriter = new CsvFeatureWriter(response.getWriter(), requestedCRS, featureType);
		try {
			for (Feature member : rs) {
				// TODO: handle lock
				// if ( lock != null && !lock.isLocked( member.getId() ) ) {
				// continue;
				// }
				if (isLimitOfFeaturesAchieved(maxFeatures, featuresAdded)) {
					break;
				}
				if (isBeforeStartIndex(startIndex, featuresSkipped)) {
					featuresSkipped++;
				}
				else {
					csvStreamWriter.write(member);
					featuresAdded++;
				}
			}
		}
		finally {
			LOG.debug("Closing FeatureResultSet (stream)");
			rs.close();
		}
	}

	private void assertExactOneQuery(Map<FeatureStore, List<Query>> fsToQueries) {
		if (fsToQueries.size() > 1 || fsToQueries.values().iterator().next().size() > 1) {
			throw new InvalidParameterValueException(
					"Request of multiple Feature types is not supported for CSV output.");
		}
	}

	private String determineMimeType(GetFeature request) {
		String mimeType = request.getPresentationParams().getOutputFormat();
		if (mimeType != null)
			return mimeType;
		return "text/csv";
	}

	private int getMaxFeatures(GetFeature request) {
		int maxFeatureFromConfiguration = webFeatureService.getQueryMaxFeatures();
		int maxFeatures = maxFeatureFromConfiguration;
		BigInteger count = request.getPresentationParams().getCount();
		if (count != null && (maxFeatureFromConfiguration < 1 || count.intValue() < maxFeatureFromConfiguration)) {
			maxFeatures = count.intValue();
		}
		return maxFeatures;
	}

	private int getStartIndex(GetFeature request) {
		int startIndex = 0;
		if (request.getPresentationParams().getStartIndex() != null) {
			startIndex = request.getPresentationParams().getStartIndex().intValue();
		}
		return startIndex;
	}

	private boolean isBeforeStartIndex(int startIndex, int featuresSkipped) {
		return featuresSkipped < startIndex;
	}

	private boolean isLimitOfFeaturesAchieved(int maxFeatures, int featuresAdded) {
		return featuresAdded == maxFeatures;
	}

}
