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
package org.deegree.tools.rendering.dem.filtering;

import static java.lang.System.currentTimeMillis;
import static org.deegree.commons.tools.CommandUtils.OPT_VERBOSE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.grid.GridFileReader;
import org.deegree.coverage.raster.io.grid.GridWriter;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.coverage.raster.utils.Rasters;
import org.deegree.coverage.tools.RasterOptionsParser;
import org.deegree.geometry.Envelope;
import org.deegree.tools.rendering.dem.filtering.filters.DEMFilter;
import org.deegree.tools.rendering.dem.filtering.filters.SmoothingFilter;
import org.deegree.tools.rendering.manager.DataManager;
import org.slf4j.Logger;

/**
 * The <code>DEMRasterFilterer</code> applies a filter to a dem by using multiple threads.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
@Tool("Applies a filter to a dem, which is loaded from a raster.")
public class DEMRasterFilterer {

	static final Logger LOG = getLogger(DEMRasterFilterer.class);

	/*
	 * Command line options
	 */
	// filter options
	private static final String OPT_KERNEL_SIZE = "kernel_size";

	private static final String OPT_STANDARD_DEVIATION_CORRECTION = "std_dev_correction";

	// output options
	private static final String OPT_OUTPUT_DIR = "output_dir";

	private static final String OPT_OUTPUT_TYPE = "output_type";

	/*
	 * Member variables
	 */
	private AbstractRaster raster;

	private int kernelSize;

	private final File outputDir;

	private final String outputType;

	private File tmpGridFile;

	/** Used to determine the tiles to filter. */
	private final static int TILE_SIZE = 1000;

	private final float stdCorr;

	private byte[] noDatas;

	private DEMRasterFilterer(AbstractRaster raster, int kernelSize, float stdCorr, String cacheDir,
			String outputDirectory, String outputType) throws IOException {
		this.stdCorr = stdCorr;

		this.raster = raster;
		// first write all raster tiles to the cache dir if needed.
		RasterCache.dispose();

		if (cacheDir != null) {
			this.tmpGridFile = new File(cacheDir, "tmp_filter_file.grid");
		}

		this.kernelSize = kernelSize == -1 ? 11 : kernelSize;
		this.outputDir = new File(outputDirectory);
		if (!outputDir.exists()) {
			LOG.warn("Given output directory: {}, does not exist trying to create it.", outputDirectory);
			if (!outputDir.mkdir()) {
				throw new IOException("Could not create output directory: " + outputDirectory);
			}
		}
		else {
			if (!outputDir.isDirectory()) {
				throw new IOException("Given directory : " + outputDirectory + " is not a directory.");
			}
		}
		this.outputType = outputType;
	}

	/**
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws Exception
	 *
	 */
	private void applyFilter() throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		int processors = rt.availableProcessors();
		LOG.info("Number of processors: {}", processors);

		// calculate the rows.
		RasterGeoReference geoRef = raster.getRasterReference();
		Envelope renv = raster.getEnvelope();
		RasterRect rect = geoRef.convertEnvelopeToRasterCRS(raster.getEnvelope());

		int width = raster.getColumns();
		int height = raster.getRows();

		int numberOfTiles = Rasters.calcApproxTiles(width, height, TILE_SIZE);
		int tileWidth = Rasters.calcTileSize(width, numberOfTiles);
		int tileHeight = Rasters.calcTileSize(height, numberOfTiles);
		int columns = (int) Math.ceil(((double) width) / tileWidth);
		int rows = (int) Math.ceil((double) height / tileHeight);

		GridWriter gridWriter = new GridWriter(columns, rows, renv, geoRef, tmpGridFile, raster.getRasterDataInfo());
		FilteredResultWiter resultWriter = new FilteredResultWiter(gridWriter);

		Stack<RasterFilterer> filters = new Stack<RasterFilterer>();
		String lock = "lock";
		for (int i = 0; i < processors; ++i) {
			RasterFilterer rf = new RasterFilterer(this.raster, kernelSize, resultWriter, stdCorr, lock, filters);
			filters.push(rf);
		}
		Thread outputThread = new Thread(resultWriter, "result writer");
		outputThread.start();

		LOG.info("Tiling raster of {} x {} pixels (width x height) into {} rows and {} columns.",
				new Object[] { rect.width, rect.height, rows, columns });

		int kernelHalf = (this.kernelSize - 1) / 2;
		long totalTime = currentTimeMillis();

		for (int row = 30; row < rows; ++row) {
			long currentTime = currentTimeMillis();
			for (int col = 0; col < columns; ++col) {
				RasterFilterer filterer = null;
				while (filterer == null) {
					synchronized (lock) {
						if (filters.isEmpty()) {
							lock.wait();
						}
						else {
							filterer = filters.pop();
						}
					}
				}
				RasterRect outputRect = new RasterRect(((col * tileWidth) - kernelHalf),
						((row * tileHeight) - kernelHalf), tileWidth + this.kernelSize, tileHeight + this.kernelSize);
				filterer.setRasterInformation(outputRect);
				new Thread(filterer, "row_" + row + "_col_" + col).start();
			}

			double rPT = Math.round((Math.round((currentTimeMillis() - currentTime) / 10d) / 100d));
			if (row + 1 < rows) {
				double remain = rPT * (rows - (row + 1));
				LOG.info(
						"Filtering row: {}, took approximately: {} seconds, estimated remaining time: {} seconds "
								+ ((remain > 60) ? "( {} minutes)." : "."),
						new Object[] { (row + 1), rPT, remain, Math.round(remain / 60d) });
			}
			System.gc();
			RasterCache.dispose();
		}
		while (true) {
			synchronized (lock) {
				RasterCache.dispose();
				if (filters.size() < processors) {
					try {
						// wait for all
						lock.wait();
					}
					catch (InterruptedException e) {
						LOG.error("Could not wait for all filter threads to end because: " + e.getLocalizedMessage(),
								e);
					}
				}
				else {
					break;
				}
			}
		}

		resultWriter.stop();
		// outputThread.interrupt();
		outputThread.join();
		gridWriter.writeMetadataFile(null);

		StringBuilder sb = new StringBuilder("Processing ");
		sb.append(rows).append(" rows and ");
		sb.append(columns).append(" columns of rasters with width: ");
		sb.append(tileWidth).append(" and height: ");
		sb.append(tileHeight).append(", took: ");
		sb.append((Math.round((currentTimeMillis() - totalTime) / 10d) / 100d)).append(" seconds");
		LOG.info(sb.toString());

		// now output the filtered tiles.
		outputTiles();
	}

	/**
	 * @param tmpGridFile
	 * @throws IOException
	 */
	private void outputTiles() throws IOException {
		GridFileReader reader = new GridFileReader();

		RasterIOOptions options = new RasterIOOptions();
		options.setNoData(noDatas);
		AbstractRaster filteredRaster = reader.load(tmpGridFile, options);
		if (!raster.isSimpleRaster()) {
			List<AbstractRaster> tiles = ((TiledRaster) raster).getTileContainer().getTiles(raster.getEnvelope());

			if (tiles == null || tiles.isEmpty()) {
				throw new NullPointerException("No tiles were found, could not apply filter.");
			}
			Collections.sort(tiles, new RasterComparator(true));
			int tileNumber = 1;
			int totalTiles = tiles.size();
			int tileStep = (int) Math.floor(totalTiles / 10d);
			int percentage = 0;
			System.out
				.println("Writing " + totalTiles + " raster files to " + outputDir + " with file type: " + outputType);
			for (AbstractRaster r : tiles) {
				if (r != null && r.isSimpleRaster()) {
					ByteBufferRasterData data = (ByteBufferRasterData) ((SimpleRaster) r).getRasterData();
					RasterReader origReader = data.getReader();
					if (origReader != null) {
						// output the raster.
						Envelope env = r.getRasterReference().relocateEnvelope(OriginLocation.OUTER, r.getEnvelope());
						AbstractRaster subRaster = filteredRaster.getSubRaster(env);
						String id = origReader.getDataLocationId();
						File outputFile = new File(outputDir, id + "." + outputType);
						RasterFactory.saveRasterToFile(subRaster, outputFile);
						if (++tileNumber % tileStep == 0) {
							percentage += 10;
							System.out.println("Wrote " + percentage + "%");
						}
					}

				}

			}

		}
		else {
			ByteBufferRasterData data = (ByteBufferRasterData) ((SimpleRaster) raster).getRasterData();
			RasterReader origReader = data.getReader();
			if (origReader != null) {
				Envelope env = raster.getEnvelope();
				AbstractRaster subRaster = filteredRaster.getSubRaster(env);
				String id = origReader.getDataLocationId();
				File outputFile = new File(outputDir, id + "." + outputType);
				RasterFactory.saveRasterToFile(subRaster, outputFile);

			}

		}

	}

	/**
	 * Creates the commandline parser and adds the options.
	 * @param args passed to the tool
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();

		Options options = initOptions();
		boolean verbose = false;

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args != null && args.length > 0) {
			for (String a : args) {
				if (a != null && a.toLowerCase().contains("help") || "-?".equals(a)) {
					printHelp(options);
				}
			}
		}

		try {
			CommandLine line = parser.parse(options, args);
			verbose = line.hasOption(OPT_VERBOSE);
			startFilterer(line);
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
			printHelp(options);
		}
		catch (Exception e) {
			System.err.println("An Exception occurred while processing your raster, error message: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
			System.exit(1);
		}
		// System.exit( 0 );
	}

	private static void startFilterer(CommandLine line) throws IOException, InterruptedException, ParseException {
		// input options

		// filter options
		String t = line.getOptionValue(OPT_KERNEL_SIZE, "11");

		int kernelSize = 11;
		try {
			Integer.parseInt(t);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("The kernel size must be an odd integer: " + e.getLocalizedMessage());
		}
		if (kernelSize % 2 == 0) {
			throw new IllegalArgumentException("The kernel size must be an odd integer.");
		}

		float stdCorr = 1.5f;
		t = line.getOptionValue(OPT_STANDARD_DEVIATION_CORRECTION, "1.5f");
		try {
			stdCorr = Float.parseFloat(t);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"The Standard deviation correction must be a number: " + e.getLocalizedMessage());
		}
		RasterIOOptions options = RasterOptionsParser.parseRasterIOOptions(line);
		String inputType = options.get(RasterIOOptions.OPT_FORMAT);

		// output options
		String outputDirectory = line.getOptionValue(OPT_OUTPUT_DIR);
		String outputType = line.getOptionValue(OPT_OUTPUT_TYPE, inputType);
		String cacheDir = options.get(RasterIOOptions.RASTER_CACHE_DIR);

		AbstractCoverage raster = RasterOptionsParser.loadCoverage(line, options);
		if (!(raster instanceof AbstractRaster)) {
			throw new IllegalArgumentException(
					"Given raster location holds a multiresolution raster, filtering a multi resolution raster is not supported.");
		}

		DEMRasterFilterer filter = new DEMRasterFilterer((AbstractRaster) raster, kernelSize, stdCorr, cacheDir,
				outputDirectory, outputType);
		filter.applyFilter();
	}

	private static Options initOptions() {
		Options options = new Options();

		CommandUtils.addDefaultOptions(options);

		RasterOptionsParser.addRasterIOLineOptions(options);
		addFilterOptions(options);
		addOutputOptions(options);
		return options;

	}

	/**
	 * @param options
	 */
	private static void addFilterOptions(Options options) {
		Option option = new Option("ks", OPT_KERNEL_SIZE, true, "The kernel size of the filter (default 11).");
		option.setArgs(1);
		options.addOption(option);

		option = new Option("stdCorr", OPT_STANDARD_DEVIATION_CORRECTION, true,
				"Correction factor for the standard deviation, higher value will cause better saving of edges but less error correction (default 1.5).");
		option.setArgs(1);
		options.addOption(option);
	}

	/**
	 * @param options
	 */
	private static void addOutputOptions(Options options) {

		Option option = new Option("o", OPT_OUTPUT_DIR, true, "Directory where to output the filtered files.");
		option.setArgs(1);
		option.setRequired(true);
		options.addOption(option);

		option = new Option("ot", OPT_OUTPUT_TYPE, true, "Type of the filtered files (default is same as input).");
		option.setArgs(1);
		options.addOption(option);
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, DataManager.class.getCanonicalName(), null, null);
	}

	/**
	 *
	 * The <code>RasterFilterer</code> applies the given filter to the original raster.
	 *
	 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
	 *
	 */
	private class RasterFilterer implements Runnable {

		private AbstractRaster originalRaster;

		private RasterRect newTileRect;

		private int size;

		private float stdevCorr;

		private String lock;

		private Stack<RasterFilterer> availableFilters;

		private final FilteredResultWiter resultWriter;

		RasterFilterer(AbstractRaster originalRaster, int size, FilteredResultWiter resultWriter, float stdevCorr,
				String lock, Stack<RasterFilterer> filters) {
			this.originalRaster = originalRaster;
			this.size = size;
			this.stdevCorr = stdevCorr;
			this.lock = lock;
			this.availableFilters = filters;
			this.resultWriter = resultWriter;

		}

		/**
		 * @param rasterRect
		 */
		public void setRasterInformation(RasterRect rasterRect) {
			this.newTileRect = rasterRect;
		}

		/**
		 * Apply the given kernel to the given raster
		 */
		public void run() {
			long time = currentTimeMillis();
			Envelope env = originalRaster.getRasterReference().getEnvelope(newTileRect, null);
			SimpleRaster procesRaster = originalRaster.getSubRaster(env).getAsSimpleRaster();
			DEMFilter filter = new SmoothingFilter(size, stdevCorr, procesRaster);
			SimpleRaster filteredResult = filter.applyFilter();
			resultWriter.push(filteredResult);
			this.availableFilters.push(this);
			synchronized (lock) {
				lock.notifyAll();
			}
			LOG.info("{}. Filtering of tile took: {} seconds.", Thread.currentThread().getName(),
					(Math.round((currentTimeMillis() - time) / 10d) / 100d));

		}

	}

	/**
	 * The <code>RasterComparator</code> compares to rasters on their origin and sorts
	 * them accordingly.
	 *
	 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
	 *
	 */
	class RasterComparator implements Comparator<AbstractRaster> {

		private boolean row;

		RasterComparator(boolean row) {
			this.row = row;
		}

		@Override
		public int compare(AbstractRaster o1, AbstractRaster o2) {
			if (o1 == null) {
				return o2 == null ? 0 : 1;
			}
			if (o2 == null) {
				return -1;
			}
			double[] orig1 = o1.getRasterReference().getOrigin();
			double[] orig2 = o2.getRasterReference().getOrigin();
			int result = 0;
			if (row) {
				result = Double.compare(orig1[1], orig2[1]);
				if (result == 0) {
					result = Double.compare(orig1[0], orig2[0]);
				}
			}
			else {
				result = Double.compare(orig1[0], orig2[0]);
				if (result == 0) {
					// Invert for negative resolution.
					result = -1 * Double.compare(orig1[1], orig2[1]);
				}
			}

			return result;
		}

	}

	class FilteredResultWiter implements Runnable {

		private GridWriter gridWriter;

		private ArrayBlockingQueue<Pair<String, SimpleRaster>> outputStack;

		private boolean doRun;

		/**
		 * @param writer to use
		 *
		 */
		public FilteredResultWiter(GridWriter writer) {
			this.gridWriter = writer;
			this.outputStack = new ArrayBlockingQueue<Pair<String, SimpleRaster>>(20);
			this.doRun = true;
		}

		/**
		 * signal the thread to stop
		 */
		public void stop() {
			doRun = false;

		}

		public void push(SimpleRaster filteredResult) {
			if (filteredResult != null) {
				try {
					outputStack.put(new Pair<String, SimpleRaster>(Thread.currentThread().getName(), filteredResult));
				}
				catch (InterruptedException e) {
					LOG.error("Could not add the filtered result to the writer because: " + e.getLocalizedMessage(), e);
				}
			}
		}

		@Override
		public void run() {
			while (doRun) {
				try {
					Pair<String, SimpleRaster> filteredResult = outputStack.poll(3, TimeUnit.SECONDS);
					if (filteredResult != null && filteredResult.second != null) {
						LOG.info("Writing tile: {} to temporary file. (Queue size: {})", filteredResult.first,
								outputStack.size());
						gridWriter.write(filteredResult.second, null);
					}
				}
				catch (InterruptedException e) {
					doRun = false;
					LOG.error("Could not write the filtered result to the temporary file because: "
							+ e.getLocalizedMessage(), e);
				}
				catch (IOException e) {
					doRun = false;
					LOG.error("Could not write the filtered result to the temporary file because: "
							+ e.getLocalizedMessage(), e);
				}
			}
			// empty the queue
			while (!outputStack.isEmpty()) {
				try {
					Pair<String, SimpleRaster> filteredResult = outputStack.poll();
					if (filteredResult != null && filteredResult.second != null) {
						LOG.info("(flushing the queue) Writing tile: {} to temporary file. (Queue size: {})",
								filteredResult.first, outputStack.size());
						gridWriter.write(filteredResult.second, null);
					}
				}
				catch (IOException e) {
					LOG.error("Could not write the filtered result to the temporary file because: "
							+ e.getLocalizedMessage(), e);
				}
			}

		}

	}

}
