/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.tools.coverage.rtb;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterBuilder;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;

/**
 * This class builds a raster tree for input files.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class RasterTreeBuilder {

	private File dstDir;

	private boolean overwriteExistingFiles = true;

	private ICRS dstSRS;

	private Envelope dstEnv;

	private int tileSize, maxTileSize;

	private InterpolationType interpolation;

	private float baseResolution = Float.NaN;

	private byte[] backgroundValue;

	private Envelope baseEnvelope = null;

	private boolean forceTileSize = false;

	private int numThreads = 4;

	private String outputFormat = "tiff";

	private RasterIOOptions rasterOptions;

	/**
	 * Creates a new RasterTreeBuilder. See the additional <code>set*()</code>-methods for
	 * further options.
	 * @param options needed for reading the raster files
	 * @param dstSRS the target crs
	 * @param dstDir the output directory of the raster tree
	 * @param maxTileSize the max tile size
	 * @param interpolation the raster interpolation
	 */
	public RasterTreeBuilder(RasterIOOptions options, ICRS dstSRS, File dstDir, int maxTileSize,
			InterpolationType interpolation) {
		super();
		this.rasterOptions = options;
		if (dstSRS == null) {
			this.dstSRS = rasterOptions.getCRS();
		}
		else {
			this.dstSRS = dstSRS;
		}
		if (this.dstSRS == null) {
			System.out.println(
					"Could not determine the target srs of the original raster, assuming crs of the source raster.");
		}
		this.dstDir = dstDir;
		this.maxTileSize = maxTileSize;
		this.interpolation = interpolation;
	}

	/**
	 * Set if existing tiles should be replaced (default is true).
	 * @param overwrite
	 */
	public void setOverwriteExistingFiles(boolean overwrite) {
		this.overwriteExistingFiles = overwrite;
	}

	/**
	 * Set the envelope of the first level.
	 * @param env
	 */
	public void setBaseEnvelope(Envelope env) {
		this.baseEnvelope = env;
	}

	/**
	 * Set the target resolution of the the first level.
	 * @param baseResolution resolution in units/px
	 */
	public void setBaseResolution(float baseResolution) {
		this.baseResolution = baseResolution;
	}

	/**
	 * Set if the maxTileSize should be forced to be the actual size.
	 * @param force
	 */
	public void setForceTileSize(boolean force) {
		this.forceTileSize = force;
	}

	/**
	 * Sets the background value.
	 * @param backgroundValue
	 */
	public void setBackgroundValue(byte[] backgroundValue) {
		this.backgroundValue = backgroundValue;
	}

	/**
	 * Sets the format for the output raster files.
	 * @param outputFormat
	 */
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	/**
	 * @param num the number of threads to use in parallel for loading and processing
	 */
	public void setNumThreads(int num) {
		this.numThreads = num;
	}

	/**
	 * Builds a raster tree for the input files-
	 * @param srcRaster array with filenames
	 * @param numOfLevels number of raster levels, use -1 for automatic calculation of the
	 * number
	 * @throws TransformationException
	 * @throws UnknownCRSException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public void buildRasterTree(AbstractRaster srcRaster, int numOfLevels)
			throws TransformationException, IllegalArgumentException, UnknownCRSException, IOException {

		if (baseEnvelope != null) {
			dstEnv = baseEnvelope;
		}
		else {
			if (srcRaster.getCoordinateSystem() == null) {
				throw new NullPointerException("The source raster has no coordinate system.");
			}
			if (dstSRS == null) {
				dstEnv = srcRaster.getEnvelope();
				dstSRS = srcRaster.getCoordinateSystem();
			}
			else {
				GeometryTransformer dstTransformer = new GeometryTransformer(dstSRS);
				dstEnv = dstTransformer.transform(srcRaster.getEnvelope(), srcRaster.getCoordinateSystem());
			}
		}

		if (forceTileSize) {
			tileSize = maxTileSize;
		}
		else {
			tileSize = TileGrid.calculateOptimalTileSize(srcRaster.getColumns(), srcRaster.getRows(), dstEnv,
					maxTileSize);
		}
		if (Float.isNaN(baseResolution)) {
			baseResolution = (float) TileGrid.calculateBaseResolution(srcRaster.getColumns(), srcRaster.getRows(),
					dstEnv, tileSize);
		}

		System.out.println("using " + tileSize + " as tile size.");
		System.out.println("using " + baseResolution + " as base resolution.");

		if (numOfLevels > 0) { // calculate n levels
			createRasterLevels(srcRaster, numOfLevels);
		}
		else {
			createRasterLevels(srcRaster);
		}
	}

	/**
	 * Create <code>n</code> raster levels.
	 * @throws UnknownCRSException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private void createRasterLevels(AbstractRaster srcRaster, int numOfLevels)
			throws IllegalArgumentException, UnknownCRSException, IOException {
		TiledRaster dstRaster;
		AbstractRaster tmpRaster = srcRaster;
		for (int i = 1; i <= numOfLevels; i++) {
			dstRaster = createRasterLevel(tmpRaster, i);
			tmpRaster = dstRaster;
		}
	}

	/**
	 * Create raster levels until the last level consists of only one tile.
	 * @throws UnknownCRSException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private void createRasterLevels(AbstractRaster srcRaster)
			throws IllegalArgumentException, UnknownCRSException, IOException {
		TiledRaster dstRaster;
		AbstractRaster tmpRaster = srcRaster;
		int i = 1;
		do { // calculate until the last level consist of one tile
			dstRaster = createRasterLevel(tmpRaster, i);
			tmpRaster = dstRaster;
			i++;
		}
		while (tmpRaster.getColumns() - 1 > tileSize || tmpRaster.getRows() - 1 > tileSize);
	}

	/**
	 * Create a single raster level.
	 * @throws UnknownCRSException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private TiledRaster createRasterLevel(AbstractRaster srcRaster, int level)
			throws IllegalArgumentException, UnknownCRSException, IOException {
		System.out.println("Generating level " + level);

		TileGrid grid = TileGrid.createTileGrid(dstEnv, tileSize, getResolutionForLevel(level));

		List<TileGrid.Tile> tiles = grid.createTileEnvelopes();

		return createRasterTiles(srcRaster, tiles, outputDir(level), numThreads);
	}

	private float getResolutionForLevel(int level) {
		// level 0: baseRes, level 1: baseRes * 2, level 2: baseRes * 4, ...
		return (float) (baseResolution * Math.pow(2, level - 1));
	}

	private TiledRaster createRasterTiles(final AbstractRaster srcRaster, List<TileGrid.Tile> tiles, File outputDir,
			int numThreads) throws IllegalArgumentException, UnknownCRSException, IOException {
		// final MemoryTileContainer tileContainer = new MemoryTileContainer();
		final RasterTransformer transf = new RasterTransformer(dstSRS);
		transf.setBackgroundValue(backgroundValue);

		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		final RasterIOOptions options = new RasterIOOptions();
		options.copyOf(this.rasterOptions);
		options.add(RasterIOOptions.CRS, dstSRS.getAlias());
		options.add(RasterIOOptions.OPT_FORMAT, outputFormat);

		for (final TileGrid.Tile tile : tiles) {
			final String tileName = tile.x + "-" + tile.y;
			// System.out.println( "*** " + tileName + " " + tile.envelope );

			String relTileFilename = tileName + "." + outputFormat;
			final File absTileFilename = new File(outputDir, relTileFilename);

			if (!overwriteExistingFiles && absTileFilename.exists()) {
				System.out.println("**skipped**");
			}
			else {
				Runnable command = new Runnable() {
					public void run() {
						try {

							String thread = Thread.currentThread().getName();
							System.out.println(thread + " transforming... " + tileName);
							AbstractRaster crop = transf.transform(srcRaster, tile.envelope, getTileSize(),
									getTileSize(), getInterpolation());
							System.out.println(thread + " saving... " + tileName);

							// long sT = currentTimeMillis();
							RasterFactory.saveRasterToFile(crop, absTileFilename);
							// long eT = currentTimeMillis() - sT;
							// System.out.println( thread + "-> Saving took: " + eT + "
							// ms." );

							System.out.println(thread + " done... " + tileName);
							if (crop.isSimpleRaster()) {
								// sT = currentTimeMillis();
								((SimpleRaster) crop).dispose();
								// eT = currentTimeMillis() - sT;
								// System.out.println( thread + "-> Disposing took: " + eT
								// + " ms." );
							}
							// reload it to save memory, close open files etc.
							// sT = currentTimeMillis();
							// crop = RasterFactory.loadRasterFromFile( absTileFilename,
							// options );
							// eT = currentTimeMillis() - sT;
							// System.out.println( thread + "-> Reloading took: " + eT + "
							// ms." );
							// sT = currentTimeMillis();
							// tileContainer.addTile( crop );
							// eT = currentTimeMillis() - sT;
							// System.out.println( thread + " -> Adding tile took: " + eT
							// + " ms." );

						}
						catch (TransformationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				executor.execute(command);
			}
		}
		shutdownExecutorAndWaitForFinish(executor);
		// srcRaster.dispose();
		TiledRaster result = (TiledRaster) new RasterBuilder().buildCoverage(outputDir, false, options);
		result.setCoordinateSystem(dstSRS);

		return result;
	}

	private void shutdownExecutorAndWaitForFinish(ExecutorService executor) {
		executor.shutdown();
		try {
			executor.awaitTermination(42, TimeUnit.DAYS);
		}
		catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private File outputDir(int level) {
		File levelDir = new File(dstDir, String.valueOf(level));
		if (!levelDir.isDirectory() && !levelDir.mkdirs()) {
			System.err.println("Could not create directory for output");
			System.exit(1);
		}
		return levelDir;
	}

	/**
	 * @return the tileSize
	 */
	public int getTileSize() {
		return tileSize;
	}

	/**
	 * @return the interpolation
	 */
	public InterpolationType getInterpolation() {
		return interpolation;
	}

	/**
	 * @return the raster io options
	 */
	public RasterIOOptions getIOOptions() {
		return this.rasterOptions;
	}

}
