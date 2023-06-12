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
package org.deegree.tools.coverage;

import static org.deegree.commons.tools.CommandUtils.OPT_VERBOSE;
import static org.deegree.commons.tools.CommandUtils.getFloatOption;
import static org.deegree.commons.tools.CommandUtils.getIntOption;
import static org.deegree.coverage.tools.RasterCommandUtils.getInterpolationType;
import static org.deegree.coverage.tools.RasterCommandUtils.parseBBOX;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_OUTPUT_TYPE;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_OUTPUT_TYPE_ABBREV;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_RASTER_OUT_LOC;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_RASTER_OUT_LOC_ABBREV;
import static org.deegree.coverage.tools.RasterOptionsParser.OPT_TYPE_DESC;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.tools.RasterCommandUtils;
import org.deegree.coverage.tools.RasterOptionsParser;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.tools.coverage.rtb.RasterTreeBuilder;

/**
 * This is the commandline interface for the {@link RasterTreeBuilder}.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
@Tool("Builds a raster tree from a given set of rasters.")
public class RTBClient {

	private static final String OPT_BBOX = "bbox";

	private static final String OPT_NUM_THREADS = "num_threads";

	private static final String OPT_BACKGROUND = "background";

	private static final String OPT_INTERPOLATION = "interpolation";

	private static final String OPT_RES = "res";

	private static final String OPT_NUM_LEVELS = "num_levels";

	private static final String OPT_FORCE_SIZE = "force_size";

	private static final String OPT_TILE_SIZE = "tile_size";

	private static final String OPT_T_SRS = "t_srs";

	private static final String DEFAULT_OUTPUT_FORMAT = "tiff";

	private static final int DEFAULT_TILE_SIZE = 800;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();

		Options options = initOptions();

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
		boolean verbose = false;
		try {
			CommandLine line = parser.parse(options, args);
			verbose = line.hasOption(OPT_VERBOSE);
			RasterTreeBuilder rtb = initRTB(line);
			setAdditionalOptions(rtb, line);
			startRTB(rtb, line);
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
			printHelp(options);
		}
		catch (Throwable e) {
			System.err
				.println("An Exception occurred while building your raster tree, error message: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
			System.exit(1);
		}

		System.exit(0);
	}

	private static RasterTreeBuilder initRTB(CommandLine line) throws ParseException {

		RasterIOOptions options = RasterOptionsParser.parseRasterIOOptions(line);

		String t_srs = line.getOptionValue(OPT_T_SRS);
		ICRS tSRS = null;
		if (t_srs == null) {
			tSRS = options.getCRS();
		}
		else {
			tSRS = CRSManager.getCRSRef(t_srs);
		}

		File outDir = new File(line.getOptionValue(OPT_RASTER_OUT_LOC));

		InterpolationType interpolationType = getInterpolationType(line.getOptionValue(OPT_INTERPOLATION));

		int maxTileSize = getIntOption(line, OPT_TILE_SIZE, DEFAULT_TILE_SIZE);

		return new RasterTreeBuilder(options, tSRS, outDir, maxTileSize, interpolationType);
	}

	private static void setAdditionalOptions(RasterTreeBuilder rtb, CommandLine line) throws ParseException {
		float baseResolution = getFloatOption(line, OPT_RES, Float.NaN);
		rtb.setBaseResolution(baseResolution);

		String outputFormat = line.getOptionValue(OPT_OUTPUT_TYPE, DEFAULT_OUTPUT_FORMAT);
		rtb.setOutputFormat(outputFormat);

		rtb.setForceTileSize(line.hasOption(OPT_FORCE_SIZE));

		if (line.hasOption(OPT_BBOX)) {
			Envelope envelope = parseBBOX(line.getOptionValue(OPT_BBOX));
			rtb.setBaseEnvelope(envelope);
		}

		if (line.hasOption(OPT_BACKGROUND)) {
			rtb.setBackgroundValue(RasterCommandUtils.parseBackgroundValue(OPT_BACKGROUND));
		}

		rtb.setNumThreads(getIntOption(line, OPT_NUM_THREADS, 1));
	}

	private static void startRTB(RasterTreeBuilder rtb, CommandLine line)
			throws ParseException, TransformationException, IllegalArgumentException, UnknownCRSException, IOException {
		int numOfLevels = getIntOption(line, OPT_NUM_LEVELS, -1);
		AbstractCoverage source = RasterOptionsParser.loadCoverage(line, rtb.getIOOptions());
		if (!(source instanceof AbstractRaster)) {
			throw new IllegalArgumentException("Given raster location already contains a multiresolution raster.");
		}
		rtb.buildRasterTree((AbstractRaster) source, numOfLevels);
	}

	private static Options initOptions() {
		Options options = new Options();
		Option option = new Option(OPT_T_SRS, "the srs of the target raster (defaults to the source srs)");
		option.setArgs(1);
		option.setArgName("epsg code");
		options.addOption(option);

		option = new Option(OPT_RASTER_OUT_LOC_ABBREV, OPT_RASTER_OUT_LOC, true,
				"the output directory for the raster tree");
		option.setRequired(true);
		option.setArgs(1);
		option.setArgName("dir");
		options.addOption(option);

		option = new Option(OPT_TILE_SIZE, "the max tile size in pixel (defaults to " + DEFAULT_TILE_SIZE
				+ "). the actual tile size is calculated to reduce 'black' borders" + " (see force_size)");
		option.setArgs(1);
		option.setArgName("size");
		options.addOption(option);

		option = new Option(OPT_FORCE_SIZE, "use the given tile_size as it is, do not calculate the optimal tile size");
		options.addOption(option);

		option = new Option(OPT_BBOX, "the target bbox");
		option.setArgs(1);
		option.setArgName("x0,y0,x1,y1");
		options.addOption(option);

		option = new Option(OPT_NUM_LEVELS,
				"the number of raster levels. when omitted, generate levels until a level contains one tile.");
		option.setArgs(1);
		option.setArgName("levels");
		options.addOption(option);

		option = new Option(OPT_RES, "the target resolution for the first level in units/px.");
		option.setArgs(1);
		option.setLongOpt("base_resolution");
		option.setArgName("units/px");
		options.addOption(option);

		option = new Option(OPT_INTERPOLATION, "the raster interpolation (nn: nearest neighbour, bl: bilinear");
		option.setArgs(1);
		option.setArgName("nn|bl");
		options.addOption(option);

		option = new Option(OPT_OUTPUT_TYPE_ABBREV, OPT_OUTPUT_TYPE, true,
				"the output format (defaults to " + DEFAULT_OUTPUT_FORMAT + ")");
		option.setArgs(1);
		option.setArgName(OPT_TYPE_DESC);
		options.addOption(option);

		option = new Option(OPT_NUM_THREADS, "the number of threads used.");
		option.setArgs(1);
		option.setArgName("threads");
		options.addOption(option);

		CommandUtils.addDefaultOptions(options);
		RasterOptionsParser.addRasterIOLineOptions(options);

		return options;
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, RTBClient.class.getSimpleName(), null, null);
	}

}
