package org.deegree.tile.persistence.gdal;

import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.util.Collections.singletonList;
import static org.gdal.gdalconst.gdalconstConstants.GDT_Byte;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;

public class GdalUtils {

	public static SpatialMetadata getEnvelopeAndCrs(Dataset gdalDataset, String configuredCrs)
			throws UnknownCRSException {
		ICRS crs = null;
		if (configuredCrs != null) {
			crs = CRSManager.lookup(configuredCrs);
		}
		double[] geoTransform = gdalDataset.GetGeoTransform();
		int rasterXSize = gdalDataset.getRasterXSize();
		int rasterYSize = gdalDataset.getRasterYSize();
		double pixelResX = geoTransform[1];
		double pixelResY = geoTransform[5];
		double minX = geoTransform[0];
		double maxX = minX + pixelResX * rasterXSize;
		double minY = geoTransform[3];
		double maxY = minY + pixelResY * rasterYSize;
		if (minX > maxX) {
			double tmp = maxX;
			maxX = minX;
			minX = tmp;
		}
		if (minY > maxY) {
			double tmp = maxY;
			maxY = minY;
			minY = tmp;
		}
		Point min = new DefaultPoint(null, crs, null, new double[] { minX, minY });
		Point max = new DefaultPoint(null, crs, null, new double[] { maxX, maxY });
		Envelope env = new DefaultEnvelope(null, crs, null, min, max);
		return new SpatialMetadata(env, singletonList(env.getCoordinateSystem()));
	}

}
