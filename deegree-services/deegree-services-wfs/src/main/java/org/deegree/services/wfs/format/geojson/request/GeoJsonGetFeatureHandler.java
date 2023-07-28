package org.deegree.services.wfs.format.geojson.request;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.geojson.GeoJsonFeatureWriter;
import org.deegree.geojson.GeoJsonWriter;
import org.deegree.services.wfs.query.QueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link GetFeature} requests for
 * {@link org.deegree.services.wfs.format.geojson.GeoJsonFormat}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonGetFeatureHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GeoJsonGetFeatureHandler.class);

	private final WebFeatureService webFeatureService;

	public GeoJsonGetFeatureHandler(WebFeatureService webFeatureService) {
		this.webFeatureService = webFeatureService;
	}

	public void doGetFeatureResults(GetFeature request, HttpResponseBuffer response, boolean allowOtherCrsThanWGS84)
			throws Exception {
		QueryAnalyzer analyzer = new QueryAnalyzer(request.getQueries(), webFeatureService,
				webFeatureService.getStoreManager(), webFeatureService.getCheckAreaOfUse());
		response.setCharacterEncoding(Charset.defaultCharset().name());
		response.setContentType(determineMimeType(request));
		ICRS requestedCRS = determineCrs(analyzer, allowOtherCrsThanWGS84);
		try (GeoJsonFeatureWriter geoJsonStreamWriter = new GeoJsonWriter(response.getWriter(), requestedCRS)) {
			geoJsonStreamWriter.startFeatureCollection();
			int startIndex = getStartIndex(request);
			int maxFeatures = getMaxFeatures(request);

			// TODO: Lock lock = acquireLock( request, analyzer );

			int featuresAdded = 0;
			int featuresSkipped = 0;

			for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
				FeatureInputStream rs = retrieveFeatures(fsToQueries);
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
							geoJsonStreamWriter.write(member);
							featuresAdded++;
						}
					}
				}
				finally {
					LOG.debug("Closing FeatureResultSet (stream)");
					rs.close();
				}
			}
			geoJsonStreamWriter.endFeatureCollection();
		}
	}

	private ICRS determineCrs(QueryAnalyzer analyzer, boolean allowOtherCrsThanWGS84) {
		if (allowOtherCrsThanWGS84)
			return analyzer.getRequestedCRS();
		return null;
	}

	private FeatureInputStream retrieveFeatures(Map.Entry<FeatureStore, List<Query>> fsToQueries)
			throws FeatureStoreException, FilterEvaluationException {
		FeatureStore fs = fsToQueries.getKey();
		Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
		return fs.query(queries);
	}

	private String determineMimeType(GetFeature request) {
		String mimeType = request.getPresentationParams().getOutputFormat();
		if (mimeType != null)
			return mimeType;
		return "application/geo+json";
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
