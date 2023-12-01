/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.gdal;

import static java.awt.color.ColorSpace.CS_sRGB;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.lang.Math.round;
import static org.gdal.gdalconst.gdalconstConstants.CE_None;
import static org.gdal.gdalconst.gdalconstConstants.GDT_Byte;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import org.deegree.commons.gdal.pool.KeyedResource;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.gdal.gdal.Band;
import org.gdal.gdal.ColorTable;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.slf4j.Logger;

/**
 * Encapsulates access to <code>org.gdal.gdal.Dataset</code>.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class GdalDataset implements KeyedResource {

	private static final Logger LOG = getLogger(GdalDataset.class);

	private final File file;

	private final ICRS crs;

	private final Dataset dataset;

	private final Envelope datasetEnvelope;

	private final double width;

	private final double height;

	private final int datasetPixelsX;

	private final int datasetPixelsY;

	private final double unitsPerPixelX;

	private final double unitsPerPixelY;

	/**
	 * Creates a new {@link GdalDataset} from the given file (which must be supported by
	 * GDAL).
	 * @param file raster image file (format must be supported by GDAL), never
	 * <code>null</code>
	 * @param crs native CRS, can be <code>null</code> (unknown)
	 * @throws UnknownCRSException
	 * @throws IOException
	 */
	public GdalDataset(File file, ICRS crs) throws UnknownCRSException, IOException {
		this.file = file.getCanonicalFile();
		this.crs = crs;
		dataset = gdal.Open(file.getPath());
		datasetEnvelope = readEnvelope();
		width = datasetEnvelope.getSpan0();
		height = datasetEnvelope.getSpan1();
		datasetPixelsX = dataset.getRasterXSize();
		datasetPixelsY = dataset.getRasterYSize();
		unitsPerPixelX = width / (double) datasetPixelsX;
		unitsPerPixelY = height / (double) datasetPixelsY;
	}

	private Envelope readEnvelope() throws UnknownCRSException, IOException {
		double[] geoTransform = dataset.GetGeoTransform();
		int rasterXSize = dataset.getRasterXSize();
		int rasterYSize = dataset.getRasterYSize();
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
		return new DefaultEnvelope(null, crs, null, min, max);
	}

	@Override
	public void close() throws IOException {
		dataset.delete();
	}

	@Override
	public String getKey() {
		return file.toString();
	}

	public Dataset getUnderlyingDataset() {
		return dataset;
	}

	public File getFile() {
		return file;
	}

	/**
	 * Return the native CRS.
	 * @return native CRS, can be null (unknown)
	 */
	public ICRS getCrs() {
		return crs;
	}

	/**
	 * Returns the dataset's extent.
	 * @return the dataset's extent, never <code>null</code>
	 */
	public Envelope getEnvelope() {
		return datasetEnvelope;
	}

	/**
	 * Extracts the specified region.
	 * @param region region to be extracted, must not be <code>null</code>
	 * @param pixelsX width of the image
	 * @param pixelsY height of the image
	 * @param withAlpha if <code>true</code>, alpha channel be kept (if available in the
	 * file), <code>false</code> otherwise
	 * @return specified region, never <code>null</code>
	 * @throws IOException
	 */
	public BufferedImage extractRegion(Envelope region, int pixelsX, int pixelsY, boolean withAlpha)
			throws IOException {

		int numBands = dataset.GetRasterCount();
		if (numBands == 4 && !withAlpha) {
			numBands = 3;
		}
		if (region.getSpan0() <= 0 || region.getSpan1() <= 0) {
			byte[][] bands = new byte[numBands][pixelsX * pixelsY];
			return toImage(bands, pixelsX, pixelsY);
		}
		boolean clipped = false;
		Envelope readWindow = clip(region);
		if (region != readWindow) {
			clipped = true;
		}
		double offsetX = readWindow.getMin().get0() - datasetEnvelope.getMin().get0();
		double offsetY = datasetEnvelope.getMax().get1() - readWindow.getMax().get1();
		int readWindowMinX = (int) round(offsetX / (double) unitsPerPixelX);
		int readWindowMinY = (int) round(offsetY / (double) unitsPerPixelY);
		int readWindowPixelsX = (int) round(readWindow.getSpan0() / unitsPerPixelX);
		int readWindowPixelsY = (int) round(readWindow.getSpan1() / unitsPerPixelY);
		int availablePixelsX = pixelsX;
		int availablePixelsY = pixelsY;
		if (clipped) {
			availablePixelsX = (int) round(readWindow.getSpan0() * (double) pixelsX / region.getSpan0());
			availablePixelsY = (int) round(readWindow.getSpan1() * (double) pixelsY / region.getSpan1());
		}
		byte[][] bands = readRegion(dataset, readWindowMinX, readWindowMinY, readWindowPixelsX, readWindowPixelsY,
				availablePixelsX, availablePixelsY, numBands);
		if (clipped) {
			int windowMinX = (int) round(
					((readWindow.getMin().get0() - region.getMin().get0()) * (double) pixelsX / region.getSpan0()));
			int windowMinY = (int) round(
					((region.getMax().get1() - readWindow.getMax().get1()) * (double) pixelsY / region.getSpan1()));
			bands = createTileFromWindow(pixelsX, pixelsY, bands, availablePixelsX, availablePixelsY, windowMinX,
					windowMinY);
		}
		return toImage(bands, pixelsX, pixelsY);
	}

	/**
	 * Extracts the specified region.
	 * @param region region to be extracted, must not be <code>null</code>
	 * @param pixelsX width of the image
	 * @param pixelsY height of the image
	 * @param withAlpha if <code>true</code>, alpha channel be kept (if available in the
	 * file), <code>false</code> otherwise
	 * @return specified region, never <code>null</code>
	 * @throws IOException
	 */
	public byte[][] extractRegionAsByteArray(Envelope region, int pixelsX, int pixelsY, boolean withAlpha)
			throws IOException {

		int numBands = dataset.GetRasterCount();
		if (numBands == 4 && !withAlpha) {
			numBands = 3;
		}
		if (region.getSpan0() <= 0 || region.getSpan1() <= 0) {
			return new byte[numBands][pixelsX * pixelsY];
		}
		boolean clipped = false;
		Envelope readWindow = clip(region);
		if (region != readWindow) {
			clipped = true;
		}
		double offsetX = readWindow.getMin().get0() - datasetEnvelope.getMin().get0();
		double offsetY = datasetEnvelope.getMax().get1() - readWindow.getMax().get1();
		int readWindowMinX = (int) round(offsetX / (double) unitsPerPixelX);
		int readWindowMinY = (int) round(offsetY / (double) unitsPerPixelY);
		int readWindowPixelsX = (int) round(readWindow.getSpan0() / unitsPerPixelX);
		int readWindowPixelsY = (int) round(readWindow.getSpan1() / unitsPerPixelY);
		int availablePixelsX = pixelsX;
		int availablePixelsY = pixelsY;
		if (clipped) {
			availablePixelsX = (int) round(readWindow.getSpan0() * (double) pixelsX / region.getSpan0());
			availablePixelsY = (int) round(readWindow.getSpan1() * (double) pixelsY / region.getSpan1());
		}
		byte[][] bands = readRegion(dataset, readWindowMinX, readWindowMinY, readWindowPixelsX, readWindowPixelsY,
				availablePixelsX, availablePixelsY, numBands);
		if (clipped) {
			int windowMinX = (int) round(
					((readWindow.getMin().get0() - region.getMin().get0()) * (double) pixelsX / region.getSpan0()));
			int windowMinY = (int) round(
					((region.getMax().get1() - readWindow.getMax().get1()) * (double) pixelsY / region.getSpan1()));
			bands = createTileFromWindow(pixelsX, pixelsY, bands, availablePixelsX, availablePixelsY, windowMinX,
					windowMinY);
		}
		return bands;
	}

	public Dataset extractRegionAsDataset(Envelope region, int pixelsX, int pixelsY, boolean withAlpha)
			throws IOException {
		byte[][] buffer = extractRegionAsByteArray(region, pixelsX, pixelsY, withAlpha);
		Driver vrtDriver = gdal.GetDriverByName("MEM");
		Dataset ds = vrtDriver.Create("/tmp/whatever", pixelsX, pixelsY, buffer.length);
		ds.SetProjection(dataset.GetProjection());
		int i = 1;
		for (byte[] bytes : buffer) {
			Band band = ds.GetRasterBand(i);
			if (band.WriteRaster(0, 0, pixelsX, pixelsY, pixelsX, pixelsY, GDT_Byte, bytes) != CE_None) {
				throw new RuntimeException("Error writing raster band.");
			}
			i++;
		}
		return ds;
	}

	private byte[][] readRegion(Dataset dataset, int regionMinX, int regionMinY, int regionPixelsX, int regionPixelsY,
			int targetWidth, int targetHeight, int numBands) throws IOException {
		byte[][] bands = new byte[numBands][targetWidth * targetHeight];
		if (targetWidth * targetHeight > 0) {
			for (int i = 0; i < numBands; i++) {
				Band band = dataset.GetRasterBand(i + 1);
				byte[] bandBytes = bands[i];
				if (band.ReadRaster(regionMinX, regionMinY, regionPixelsX, regionPixelsY, targetWidth, targetHeight,
						GDT_Byte, bandBytes, 0, 0) != CE_None) {
					LOG.error("GDAL ReadRaster failed: " + regionMinX + "," + regionMinY + "," + regionPixelsX + ","
							+ regionPixelsY + "," + targetWidth + "," + targetHeight + "," + bandBytes.length + ","
							+ datasetPixelsX + "," + datasetPixelsY);
					return bands;
				}
			}
		}
		return bands;
	}

	private Envelope clip(Envelope region) {
		double minX = region.getMin().get0();
		double minY = region.getMin().get1();
		double maxX = region.getMax().get0();
		double maxY = region.getMax().get1();
		boolean clipped = false;
		if (datasetEnvelope.getMin().get0() > minX) {
			minX = datasetEnvelope.getMin().get0();
			clipped = true;
		}
		if (datasetEnvelope.getMin().get1() > minY) {
			minY = datasetEnvelope.getMin().get1();
			clipped = true;
		}
		if (datasetEnvelope.getMax().get0() < maxX) {
			maxX = datasetEnvelope.getMax().get0();
			clipped = true;
		}
		if (datasetEnvelope.getMax().get1() < maxY) {
			maxY = datasetEnvelope.getMax().get1();
			clipped = true;
		}
		if (!clipped) {
			return region;
		}
		Point min = new DefaultPoint(null, null, null, new double[] { minX, minY });
		Point max = new DefaultPoint(null, null, null, new double[] { maxX, maxY });
		return new DefaultEnvelope(min, max);
	}

	private byte[][] createTileFromWindow(int pixelsX, int pixelsY, byte[][] windowData, int windowSizeX,
			int windowSizeY, int offsetX, int offsetY) {
		byte[][] bands = new byte[windowData.length][pixelsX * pixelsY];
		for (int i = 0; i < bands.length; i++) {
			byte[] src = windowData[i];
			byte[] dst = bands[i];
			if (i != 3) {
				// default to white / assume that white is -1
				byte backgroundValue = -1;
				Double[] noData = new Double[1];
				dataset.GetRasterBand(i + 1).GetNoDataValue(noData);
				if (noData.length == 1 && noData[0] != null) {
					backgroundValue = noData[0].byteValue();
				}
				setDataToValue(dst, backgroundValue);
			}
			for (int y = 0; y < windowSizeY; y++) {
				for (int x = 0; x < windowSizeX; x++) {
					int targetX = offsetX + x;
					int targetY = offsetY + y;
					try {
						dst[targetX + targetY * pixelsX] = src[x + y * windowSizeX];
					}
					catch (Exception e) {
						System.out.println(targetX + ", " + targetY);
					}
				}
			}
		}
		return bands;
	}

	private void setDataToValue(byte[] dst, byte newValue) {
		for (int i = 0; i < dst.length; i++) {
			dst[i] = newValue;
		}
	}

	private BufferedImage toImage(byte[][] bands, int xSize, int ySize) {
		int numberOfBands = bands.length;
		int numBytes = xSize * ySize * numberOfBands;
		DataBuffer imgBuffer = new DataBufferByte(bands, numBytes);
		SampleModel sampleModel = new BandedSampleModel(TYPE_BYTE, xSize, ySize, numberOfBands);
		WritableRaster raster = Raster.createWritableRaster(sampleModel, imgBuffer, null);
		if (numberOfBands == 1) {
			Band band = dataset.GetRasterBand(1);
			int bufType = band.getDataType();
			int dataType = detectDataType(band, bufType);
			BufferedImage img;
			if (BufferedImage.TYPE_BYTE_INDEXED == dataType) {
				ColorTable ct = band.GetRasterColorTable();
				IndexColorModel cm = ct.getIndexColorModel(gdal.GetDataTypeSize(bufType));
				img = new BufferedImage(xSize, ySize, dataType, cm);
			}
			else {
				img = new BufferedImage(xSize, ySize, dataType);
			}
			img.setData(raster);
			return img;
		}
		ColorSpace cs = ColorSpace.getInstance(CS_sRGB);
		ColorModel cm;
		if (numberOfBands == 3) {
			cm = new ComponentColorModel(cs, false, false, ColorModel.OPAQUE, TYPE_BYTE);
		}
		else if (numberOfBands == 4) {
			cm = new ComponentColorModel(cs, true, false, ColorModel.TRANSLUCENT, TYPE_BYTE);
		}
		else {
			throw new IllegalArgumentException("Unsupported number of bands: " + numberOfBands);
		}
		return new BufferedImage(cm, raster, false, null);
	}

	private int detectDataType(Band band, int dataType) {
		if (dataType == gdalconstConstants.GDT_Byte)
			return (band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex)
					? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_GRAY;
		else if (dataType == gdalconstConstants.GDT_Int16)
			return BufferedImage.TYPE_USHORT_GRAY;
		else if (dataType == gdalconstConstants.GDT_Int32)
			return BufferedImage.TYPE_CUSTOM;
		return BufferedImage.TYPE_CUSTOM;
	}

}