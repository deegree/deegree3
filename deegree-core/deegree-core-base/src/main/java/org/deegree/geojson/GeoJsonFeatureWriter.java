package org.deegree.geojson;

import java.io.Closeable;
import java.io.IOException;

import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;

/**
 * Writer for GeoJSON documents.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface GeoJsonFeatureWriter extends Closeable {

	/**
	 * Starts a new FeatureCollection.
	 *
	 * To finish this FeatureCollection call endFeatureCollection
	 * @throws IOException if GeoJSON could no be written
	 */
	void startFeatureCollection() throws IOException;

	/**
	 *
	 * Ends the written FeatureCollection.
	 *
	 * Ensure that start FeatureCollection was called before.
	 * @throws IOException if GeoJSON could no be written
	 */
	void endFeatureCollection() throws IOException;

	/**
	 * Writes a new Feature into the FeatureCollection.
	 * @param feature the feature to write, never <code>null</code>
	 * @throws IOException if GeoJSON could no be written
	 * @throws TransformationException if a geometry to export cannot be transformed to
	 * CRS:84
	 * @throws UnknownCRSException if the CRS of the geometry is not supported
	 */
	void write(Feature feature) throws IOException, TransformationException, UnknownCRSException;

}
