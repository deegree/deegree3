/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.tools.rendering.dem.filtering.filters;

import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.ByteBuffer;

import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * Uses a kernel based approach to filter the given dem raster tile. First the standard
 * deviation on the raster tile is calculated, after that a kernel will move over all
 * samples and determines the distance of the local standard deviation to the global std.
 * and determines which samples are used to (re)calculate the height of the sample in
 * dependence to it's neighbours.
 *
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author <a href="mailto:name@deegree.org">Rutger Bezema</a>
 */
public class SmoothingFilter implements DEMFilter {

	private static final Logger LOG = getLogger(SmoothingFilter.class);

	// private Raster raster;

	private static final double EPS = 1E-8;

	private SimpleRaster currentRaster;

	private int size;

	private int size2;

	private final double stdev;

	private float noData;

	private final float stdevCorr;

	private float minDiff = 20;

	private ByteBufferRasterData newKernel;

	private final int outWidth;

	private final int outHeight;

	/**
	 * @param size kernel size must be an odd value > 1
	 * @param stdevCorr correction factor for standard deviation of kernel field. A higher
	 * value will cause better saving of edges but also less error correction
	 * @param procesRaster needed to create a writeable raster and some no data values.
	 * @throws IllegalArgumentException if the size is even and not odd.
	 */
	public SmoothingFilter(int size, float stdevCorr, SimpleRaster procesRaster) {
		this.size = size;
		this.outWidth = procesRaster.getColumns() - size;
		this.outHeight = procesRaster.getRows() - size;
		if (size % 2 == 0) {
			throw new IllegalArgumentException("size must be odd");
		}
		this.noData = ByteBuffer.wrap(procesRaster.getRasterDataInfo().getNoDataPixel(new byte[0])).getFloat();
		this.stdevCorr = stdevCorr;
		this.size2 = size / 2;
		// kernel = new float[size][size];
		RasterRect kernelRect = new RasterRect(0, 0, size, size);
		newKernel = (ByteBufferRasterData) procesRaster.getRasterData()
			.createCompatibleWritableRasterData(kernelRect, null);
		// this.raster = raster;
		this.currentRaster = procesRaster;
		// this.stdev = 0;
		this.stdev = calcGlobalValues();
	}

	public SimpleRaster applyFilter() {
		RasterData rasterData = currentRaster.getReadOnlyRasterData();
		RasterData outData = rasterData.createCompatibleWritableRasterData(new RasterRect(0, 0, outWidth, outHeight),
				null);
		Envelope envelope = currentRaster.getRasterReference()
			.getEnvelope(new RasterRect(size2, size2, outWidth, outHeight), null);
		RasterGeoReference rasterReference = currentRaster.getRasterReference().createRelocatedReference(envelope);

		SimpleRaster out = new SimpleRaster(outData, envelope, rasterReference, null);

		int percentage = 0;
		int step = 10;
		double stepSize = outHeight / (double) step;
		double nextStep = stepSize;
		long currentTime = currentTimeMillis();
		for (int y = 0; y < outHeight; y++) {
			if (y > nextStep) {
				percentage += step;
				nextStep += stepSize;
				long t = Math.round((currentTimeMillis() - currentTime) / 1000d);
				currentTime = currentTimeMillis();
				LOG.info("{}. Filtered {} %, approximately {} seconds remaining",
						new Object[] { Thread.currentThread().getName(), percentage, t * ((100 - percentage) / 10) });
			}
			for (int x = 0; x < outWidth; x++) {
				// filtering should be done from half kernel size onwards.
				float val = process(size2 + x, size2 + y, rasterData);
				outData.setFloatSample(x, y, 0, val);
			}
		}
		return out;
	}

	private float process(int x, int y, RasterData rasterData) {

		// long time = currentTimeMillis();
		RasterData subset = rasterData.getSubset(new RasterRect(x - size2, y - size2, size, size));
		// System.out.println( Thread.currentThread() + " get subset took: " + ( (
		// currentTimeMillis() - time ) ) +
		// "ms." );
		newKernel.fillWithNoData();
		newKernel.setSubset(0, 0, size, size, subset, 0, 0);

		// remove all kernel fields with a difference > minDiff to kernel center
		float centerValue = newKernel.getFloatSample(size2, size2, 0);
		for (int ky = 0; ky < size; ky++) {
			for (int kx = 0; kx < size; kx++) {
				float d = newKernel.getFloatSample(kx, ky, 0);
				if (Math.abs(centerValue - d) > minDiff) {
					newKernel.setFloatSample(kx, ky, 0, noData);
				}
			}
		}

		// remove values with largest difference from center value of kernel till
		// stdev of kernel is less than reference value
		// TODO
		// calculate a weight for kernel standard deviation that considers numbers
		// of kernel fields that has been used for calculation
		double[] kStdevCNT = calcKernelStdev(newKernel);
		int t = 0;
		int sizeSquared = size * size;
		while (stdev < kStdevCNT[0] * stdevCorr && t++ < (sizeSquared)) {
			newKernel.setFloatSample((int) kStdevCNT[3], (int) kStdevCNT[2], 0, noData);
			// kernel[(int) kStdevCNT[3]][(int) kStdevCNT[2]] = noData;
			kStdevCNT = calcKernelStdev(newKernel);
		}
		// TODO
		// remove all kernel cells that are connected directly or indirectly to kernel
		// center

		// calculate result; values will be weighted by distance of according kernel
		// fields
		// to kernel center
		double mean = 0;
		double we = 0;
		for (int ky = 0; ky < size; ky++) {
			for (int kx = 0; kx < size; kx++) {
				float value = newKernel.getFloatSample(kx, ky, 0);
				// if ( kernel[ky][kx] > noData ) {
				if (Math.abs(value - noData) > EPS) {
					double a = (size2 + 1) - ky;
					double b = (size2 + 1) - kx;
					double wt = Math.sqrt(a * a + b * b);
					if (wt == 0) {
						wt = 1;
					}
					mean += (value * (1 / wt));
					we += (1 / wt);
				}
			}
		}
		return (float) (mean / we);
	}

	/**
	 * @return { stdev, no of values use for stdev, column of field with max diff to
	 * center, row of field with max diff to center}
	 */
	private double[] calcKernelStdev(RasterData kernelView) {
		double mean = 0;
		// calculate mean
		double cnt = 0;

		// stdev just can be calculated for quantities > 1;
		// zi and zj will contain coordinates of kernel field that has the largest
		// difference to kernel center value
		int zi = 0;
		int zj = 0;
		double v = 0;
		double std = 0;
		float centerValue = kernelView.getFloatSample(size2, size2, 0);
		for (int ky = 0; ky < size; ky++) {
			for (int kx = 0; kx < size; kx++) {
				float value = kernelView.getFloatSample(kx, ky, 0);
				if (Math.abs(value - noData) > EPS) {
					double t = Math.pow(centerValue - value, 2);
					std += t;
					if (t >= v) {
						v = t;
						zi = kx;
						zj = ky;
					}
					mean += value;
					cnt++;
				}
			}
		}
		mean = mean / cnt;

		// calculate standard deviation
		if (cnt > 1) {
			// // stdev just can be calculated for quantities > 1;
			// // zi and zj will contain coordinates of kernel field that has the largest
			// // difference to kernel center value
			return new double[] { Math.sqrt(std / (cnt - 1)), cnt, zi, zj };
		}
		// return 0 if stdev can not be calculated
		return new double[] { 0, 1, -1, -1 };
	}

	private double calcGlobalValues() {
		double mean = 0;
		double w = currentRaster.getColumns();
		double h = currentRaster.getRows();
		RasterData data = this.currentRaster.getReadOnlyRasterData();
		// calculate mean
		int cnt = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// double d = raster.getValue( i, j );
				double value = data.getFloatSample(x, y, 0);
				if (Math.abs(noData - value) > EPS) {
					mean += value;
					cnt++;
				}
			}
		}
		mean /= cnt;
		// calculate standard deviation
		double stdev = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float d = data.getFloatSample(x, y, 0);
				if (d > noData) {
					stdev += Math.pow(mean - d, 2);
				}
			}
		}
		stdev = Math.sqrt(stdev / (cnt - 1));
		LOG.info("{}. Calculated mean: {}, and standard deviation: {}",
				new Object[] { Thread.currentThread().getName(), mean, stdev });
		return stdev;
	}

}
