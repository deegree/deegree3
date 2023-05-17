/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.tools.coverage.converter;

import static org.deegree.commons.tools.CommandUtils.OPT_VERBOSE;
import static org.deegree.commons.tools.CommandUtils.getIntOption;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_OUTPUT_TYPE;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_OUTPUT_TYPE_ABBREV;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_RASTER_OUT_LOC;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_TYPE_DESC;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.TileContainer;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.coverage.tools.RasterOptionsParser;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;

/**
 * Takes a raster file, or a number of raster files and convert it/them to another raster
 * type.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
@Tool("Converts a raster from one type into another.")
public class RasterConverter {

	private static final String OPT_NUM_THREADS = "num_threads";

	/**
	 * a starter method to transform a given point or a serie of points read from a file.
	 * @param args
	 * @throws UnknownCRSException
	 * @throws IOException if the buffered reader could not read from the file
	 */
	public static void main(String[] args) throws UnknownCRSException, IOException {
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
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
			verbose = line.hasOption(OPT_VERBOSE);
			System.exit(init(line));
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
			printHelp(options);
		}
		catch (Throwable e) {
			System.err
				.println("An Exception occurred while converting your raster data, error message: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
			System.exit(1);
		}
	}

	/**
	 * add crs and point here if using eclipse to start.
	 * @throws TransformationException
	 * @throws IllegalArgumentException
	 * @throws UnknownCRSException
	 * @throws IOException
	 * @throws ParseException
	 * @throws InterruptedException
	 */
	private static int init(CommandLine line) throws IllegalArgumentException, TransformationException,
			UnknownCRSException, IOException, ParseException, InterruptedException {

		RasterIOOptions options = RasterOptionsParser.parseRasterIOOptions(line);

		int numThreads = getIntOption(line, OPT_NUM_THREADS, 4);

		File f = RasterOptionsParser.getRasterLocation(line);
		if (f.isFile()) {
			numThreads = 1;
		}
		String outputLoc = line.getOptionValue(OPT_RASTER_OUT_LOC);
		String ext = line.getOptionValue(OPT_OUTPUT_TYPE);
		AbstractCoverage raster = RasterOptionsParser.loadCoverage(line, options);
		RasterConverter converter = new RasterConverter();
		return converter.convert(raster, outputLoc, numThreads, ext, line.hasOption(OPT_VERBOSE));
	}

	/**
	 * @param numThreads
	 * @param outLoc
	 * @throws InterruptedException
	 * @throws IOException
	 *
	 */
	private int convert(AbstractCoverage source, String outLoc, int numThreads, String outputFormat,
			final boolean verbose) throws InterruptedException, IOException {
		List<SimpleRaster> tiles = new LinkedList<SimpleRaster>();
		getTiles(source, tiles);
		if (tiles.isEmpty()) {
			System.err.println("Found no raster tiles in source: " + source + ", hence nothing to convert.");
			return 1;
		}
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		SimpleRaster simpleRaster = tiles.get(0);
		RasterReader rasterReader = ((ByteBufferRasterData) simpleRaster.getRasterData()).getReader();
		File file = rasterReader.file();
		File outputLocation = getOutputLocation(file, outLoc, outputFormat, tiles.size() == 1);

		for (final SimpleRaster tile : tiles) {
			File outFile = null;
			ByteBufferRasterData data = (ByteBufferRasterData) tile.getRasterData();
			RasterReader origReader = data.getReader();
			final String tileName = origReader.getDataLocationId();
			if (tiles.size() == 1) {
				if (outputLocation.isFile()) {
					outFile = outputLocation.getAbsoluteFile();
				}
				else {
					outFile = createNewFile(outputLocation, tileName, outputFormat);
				}
			}
			else {
				outFile = createNewFile(outputLocation, tileName, outputFormat);
			}
			final File absTileFilename = outFile;
			System.out.println("Converting: " + tileName + " -> file: " + absTileFilename);

			Runnable command = new Runnable() {
				public void run() {
					try {
						String thread = Thread.currentThread().getName();
						System.out.println(thread + " saving... " + tileName);
						RasterFactory.saveRasterToFile(tile, absTileFilename);
						tile.dispose();
						System.out.println(thread + " done... " + tileName);
					}
					catch (Exception e) {
						System.err.println("Ar error occurred while processing tile: " + tileName + ": "
								+ e.getLocalizedMessage());
						if (verbose) {
							e.printStackTrace();
						}
					}
				}
			};
			executor.execute(command);

		}
		shutdownExecutorAndWaitForFinish(executor);
		return 0;
	}

	private File getOutputLocation(File tileFile, String reqOutputLocation, String outputFormat, boolean singleTile)
			throws IOException {
		String outDir = reqOutputLocation;
		if (tileFile == null) {
			if (outDir == null) {
				String tmpDir = System.getProperty("java.io.tmpdir");
				outDir = tmpDir;
				System.out.println(
						"No output location given and no file url to load from, writing result in tmp dir: " + tmpDir);
			}
		}
		else {
			if (outDir == null) {
				// use file dir.
				outDir = tileFile.getParent();
			}
		}
		File outputLocation = null;
		File testFile = new File(outDir);
		if (!testFile.exists()) {
			if (testFile.getParentFile().exists()) {
				// the parent exists, are we requesting a file?
				if (singleTile) {
					// only one file, maybe the parent exists?
					File parent = testFile.getParentFile();
					String ext = FileUtils.getFileExtension(testFile);
					if (ext == null) {
						ext = outputFormat;
					}
					String fName = FileUtils.getFilename(testFile);
					outputLocation = createNewFile(parent, fName, ext);
				}
				else {
					outputLocation = testFile.getParentFile();
				}
			}
		}
		else {
			// file exists
			if (testFile.isFile()) {
				if (!singleTile) {
					System.out.println(
							"Only one output file was given, but multiple input files were selected, using parent directory instead.");
				}
				else {
					outputLocation = testFile.getAbsoluteFile();
				}
			}
			else {
				outputLocation = testFile;
				// file naming is done while converting.
			}
		}
		if (outputLocation == null) {
			// stranger things happened :-)
			outputLocation = new File(System.getProperty("java.io.tmpdir"));
			System.out.println("Could not determine the output directory. Using: " + outputLocation.getAbsolutePath()
					+ " as base directory for output.");
		}
		return outputLocation;
	}

	private File createNewFile(File outputDirectory, String fName, String ext) throws IOException {

		File outputFile = new File(outputDirectory, fName + "." + ext);

		int i = 1;

		while (outputFile.exists()) {
			outputFile = new File(outputDirectory, fName + "-" + (i++) + "." + ext);
		}
		boolean suc = outputFile.createNewFile();
		if (!suc) {
			throw new IOException("Could not create new raster file at location:" + outputDirectory);
		}
		return outputFile;
	}

	/**
	 * @param source
	 * @param tiles
	 */
	private void getTiles(AbstractCoverage source, List<SimpleRaster> tiles) {
		if (source != null) {
			if (source instanceof AbstractRaster) {
				AbstractRaster rast = (AbstractRaster) source;
				if (rast.isSimpleRaster()) {
					tiles.add((SimpleRaster) source);
				}
				else if (rast instanceof TiledRaster) {
					TiledRaster tr = (TiledRaster) rast;
					TileContainer container = tr.getTileContainer();
					List<AbstractRaster> origTiles = container.getTiles(tr.getEnvelope());
					for (AbstractRaster r : origTiles) {
						if (r != null) {
							getTiles(r, tiles);
						}
					}
				}
				else {
					System.err.println("Unknown raster type: " + rast);
				}
			}
			else {
				if (source instanceof MultiResolutionRaster) {
					MultiResolutionRaster mrr = (MultiResolutionRaster) source;
					List<Double> resolutions = mrr.getResolutions();
					if (resolutions != null && !resolutions.isEmpty()) {
						for (Double res : resolutions) {
							AbstractRaster raster = mrr.getRaster(res);
							getTiles(raster, tiles);
						}
					}
				}
				else {
					System.err.println("Unknown coverage type: " + source);
				}
			}
		}
	}

	private void shutdownExecutorAndWaitForFinish(ExecutorService executor) throws InterruptedException {
		executor.shutdown();
		/** oh them tonny */
		executor.awaitTermination(42, TimeUnit.DAYS);

	}

	private static Options initOptions() {
		Options options = new Options();

		Option option = new Option(RasterOptionsParser.OPT_RASTER_OUT_LOC_ABBREV, OPT_RASTER_OUT_LOC, true,
				"the output directory for the raster tree, defaults to input dir");
		option.setArgs(1);
		option.setArgName("dir|file");
		options.addOption(option);

		option = new Option(OPT_OUTPUT_TYPE_ABBREV, OPT_OUTPUT_TYPE, true, "The output type of the rasters.");
		option.setArgs(1);
		option.setArgName(OPT_TYPE_DESC);
		option.setRequired(true);
		options.addOption(option);

		option = new Option(OPT_NUM_THREADS, "the number of threads used.");
		option.setArgs(1);
		option.setArgName("threads");
		options.addOption(option);

		RasterOptionsParser.addRasterIOLineOptions(options);

		CommandUtils.addDefaultOptions(options);

		return options;

	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, RasterConverter.class.getCanonicalName(), null, null);
	}

}
