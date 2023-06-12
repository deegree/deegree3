/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.coverage.tools;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterBuilder;
import org.slf4j.Logger;

/**
 * The <code>RasterOptionsParser</code> supplies methods for creating a raster from the
 * command line.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class RasterOptionsParser {

	private static final Logger LOG = getLogger(RasterOptionsParser.class);

	// input options
	/** The 'input' raster location */
	public static final String OPT_RASTER_LOCATION = "raster_location";

	private static final String OPT_INPUT_TYPE = "input_type";

	/** Should the raster directory be read recursively */
	public static final String OPT_RECURSIVE = "recursive";

	private static final String OPT_CRS = "s_srs";

	private static final String OPT_NO_DATA = "nodata_value";

	private static final String OPT_NO_DATA_TYPE = "nodata_type";

	private static final String OPT_ORIGIN = "origin_location";

	private static final String OPT_RASTER_CACHE_DIR = "cache_dir";

	/** Output directory to unify raster command line tools: output_location */
	public static final String OPT_RASTER_OUT_LOC = "output_location";

	/** Output directory abbreviation to unify raster command line tools: rod */
	public static final String OPT_RASTER_OUT_LOC_ABBREV = "rol";

	/** Output type to unify raster command line tools: output_type */
	public static final String OPT_OUTPUT_TYPE = "output_type";

	/** Output type abbreviation to unify raster command line tools: ot */
	public static final String OPT_OUTPUT_TYPE_ABBREV = "ot";

	/**
	 * Description of a raster type to unify raster command line tools: tiff|jpg|png|...
	 */
	public static final String OPT_TYPE_DESC = "tiff|jpg|png|...";

	/**
	 * Read options from the command line and create a {@link RasterIOOptions} from them.
	 * @param line
	 * @return the raster io options parsed from the command line.
	 * @throws ParseException
	 */
	public static RasterIOOptions parseRasterIOOptions(CommandLine line) throws ParseException {
		OriginLocation originLocation = getLocation(line.getOptionValue(OPT_ORIGIN));

		String inputType = line.getOptionValue(OPT_INPUT_TYPE);
		try {
			File f = getRasterLocation(line);
			if (f.isFile()) {
				inputType = FileUtils.getFileExtension(f);
			}
			else {
				if (inputType == null) {
					LOG.warn("Setting input type to all (*)");
					inputType = "*";
				}
			}
		}
		catch (IOException e) {
			throw new ParseException("The intput location does not reference a file.");
		}
		String crs = line.getOptionValue(OPT_CRS);
		String noDataValue = line.getOptionValue(OPT_NO_DATA);
		String cacheDir = line.getOptionValue(OPT_RASTER_CACHE_DIR);
		String dataType = line.getOptionValue(OPT_NO_DATA_TYPE);
		DataType noDataType = DataType.fromString(dataType);

		RasterIOOptions options = new RasterIOOptions(originLocation);
		options.add(RasterIOOptions.OPT_FORMAT, inputType);
		options.add(RasterIOOptions.CRS, crs);
		if (noDataType != DataType.UNDEFINED) {
			byte[] noDatas = RasterIOOptions.createNoData(new String[] { noDataValue }, noDataType);
			options.setNoData(noDatas);
		}
		if (cacheDir != null) {
			File cd = new File(cacheDir);
			if (cd.exists() && cd.isDirectory()) {
				options.add(RasterIOOptions.RASTER_CACHE_DIR, cacheDir);
			}
			else {
				LOG.warn(
						"Using default cache dir: " + RasterCache.DEFAULT_CACHE_DIR + " because given cache directory: "
								+ cacheDir + " does not exist or is a file (and not a directory.)");
			}
		}
		return options;
	}

	/**
	 * @param optionValue
	 */
	private static OriginLocation getLocation(String optionValue) {
		OriginLocation result = OriginLocation.CENTER;
		if ("outer".equalsIgnoreCase(optionValue)) {
			result = OriginLocation.OUTER;
		}
		return result;
	}

	/**
	 * Add the rasterio (loading) options to the given cli options.
	 * @param options
	 */
	public static void addRasterIOLineOptions(Options options) {
		Option option = new Option("rl", OPT_RASTER_LOCATION, true, "The location of (a) raster data file(s)");
		option.setArgs(1);
		option.setArgName("dir|file");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("it", OPT_INPUT_TYPE, true,
				"Type of the input raster files, only if the location is a directory");
		option.setArgs(1);
		option.setArgName(OPT_TYPE_DESC);
		// option.setRequired( true );
		options.addOption(option);

		option = new Option("s_srs", OPT_CRS, true, "The srs of the input files.");
		option.setArgs(1);
		option.setArgName("epsg:code");
		options.addOption(option);

		option = new Option("nd", OPT_NO_DATA, true, "Value to be used as no (missing) data (default 0).");
		option.setArgs(1);
		options.addOption(option);

		option = new Option("ndt", OPT_NO_DATA_TYPE, true, "Type of no data value (defaults to byte).");
		option.setArgs(1);
		option.setArgName("byte|short|...");
		options.addOption(option);

		option = new Option("ol", OPT_ORIGIN, true,
				"Origin location of the raster files, eg. center (default) or outer.");
		option.setArgs(1);
		option.setArgName("center|outer");
		options.addOption(option);

		option = new Option("r", OPT_RECURSIVE, false,
				"Search for raster files recursively in the given directory (default true).");
		options.addOption(option);

		option = new Option("rcd", OPT_RASTER_CACHE_DIR, true, "Directory to be used for caching, (default: "
				+ RasterCache.DEFAULT_CACHE_DIR.getAbsolutePath() + ").");
		option.setArgs(1);
		option.setArgName("dir");
		options.addOption(option);

		CommandUtils.addDefaultOptions(options);
	}

	/**
	 * @param line to get the raster directory and the recursive option from.
	 * @param options created from {@link #parseRasterIOOptions(CommandLine)}
	 * @return the loaded abstract coverage.
	 * @throws IOException
	 */
	public static AbstractCoverage loadCoverage(CommandLine line, RasterIOOptions options) throws IOException {
		File f = getRasterLocation(line);
		return new RasterBuilder().buildCoverage(f, line.hasOption(OPT_RECURSIVE), options);
	}

	/**
	 * Return the rasters location.
	 * @param line to get the location from.
	 * @return a File denoting a raster location, which may be a directory or a single
	 * file.
	 * @throws IOException if the location does not exist.
	 */
	public static File getRasterLocation(CommandLine line) throws IOException {
		String rasterLocation = line.getOptionValue(OPT_RASTER_LOCATION);
		File f = new File(rasterLocation);
		if (!f.exists()) {
			throw new IOException("The given raster location: " + rasterLocation + " does not exist. ");
		}
		return f;
	}

}
