package org.deegree.geojson;

import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;

import java.io.IOException;

/**
 * Writer for GeoJSON documents with single features.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface GeoJsonSingleFeatureWriter {

	/**
	 * Starts a single feature.
	 * <p>
	 * To finish the feature call endSingleFeature
	 * @throws IOException if GeoJSON could no be written
	 */
	void startSingleFeature() throws IOException;

	/**
	 * Ends the written feature.
	 * <p>
	 * Ensure that startSingleFeature was called before.
	 * @throws IOException if GeoJSON could no be written
	 */
	void endSingleFeature() throws IOException;

	/**
	 * Writes a new, single feature.
	 * @param feature the feature to write, never <code>null</code>
	 * @throws IOException if GeoJSON could no be written
	 * @throws TransformationException if a geometry to export cannot be transformed to
	 * CRS:84
	 * @throws UnknownCRSException if the CRS of the geometry is not supported
	 */
	void writeSingleFeature(Feature feature) throws IOException, UnknownCRSException, TransformationException;

}
