package org.deegree.featureinfo.serializing;

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

	@Override
	public void serialize(FeatureInfoParams params, FeatureInfoContext context) {
		try (GeoJsonFeatureWriter geoJsonStreamWriter = new GeoJsonWriter(context.getWriter(), null)) {
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

}
