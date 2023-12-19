package org.deegree.featureinfo.serializing;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.FeatureInfoContext;
import org.deegree.featureinfo.FeatureInfoParams;
import org.deegree.geojson.GeoJsonFeatureWriter;
import org.deegree.geojson.GeoJsonWriter;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@link FeatureInfoSerializer} to serialize feature info result as GeoJson.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonFeatureInfoSerializer implements FeatureInfoSerializer {

	private static final Logger LOG = getLogger(GeoJsonFeatureInfoSerializer.class);

	private final boolean allowOtherCrsThanWGS84;

	private final boolean allowExportOfGeometries;

	public GeoJsonFeatureInfoSerializer(boolean allowOtherCrsThanWGS84, boolean allowExportOfGeometries) {
		this.allowExportOfGeometries = allowExportOfGeometries;
		this.allowOtherCrsThanWGS84 = allowOtherCrsThanWGS84;
	}

	@Override
	public void serialize(FeatureInfoParams params, FeatureInfoContext context) {
		ICRS crs = detectCrs(params);
		boolean skipGeometries = detectSkipGeometries(params);
		try (GeoJsonFeatureWriter geoJsonStreamWriter = new GeoJsonWriter(context.getWriter(), crs, skipGeometries)) {
			geoJsonStreamWriter.startFeatureCollection();
			FeatureCollection featureCollection = params.getFeatureCollection();
			for (Feature feature : featureCollection) {
				geoJsonStreamWriter.write(feature);
			}
			geoJsonStreamWriter.endFeatureCollection();
		}
		catch (IOException | TransformationException | UnknownCRSException e) {
			LOG.error("GeoJson GFI response could not be written", e);
		}
	}

	private boolean detectSkipGeometries(FeatureInfoParams params) {
		if (allowExportOfGeometries && params.isWithGeometries())
			return false;
		return true;
	}

	private ICRS detectCrs(FeatureInfoParams params) {
		if (allowOtherCrsThanWGS84 && params.getInfoCrs() != null) {
			return params.getInfoCrs();
		}
		return null;
	}

}
