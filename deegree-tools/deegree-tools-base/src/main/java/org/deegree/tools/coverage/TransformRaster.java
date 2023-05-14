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

import static org.deegree.coverage.tools.RasterCommandUtils.getInterpolationType;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.persistence.CRSManager;

/**
 * This is a command line tool to transform raster files between coordinate systems.
 * Multiple input files will be mosaiced into one transformed output file.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 *
 */
@Tool("Transforms a raster with the given crs into another crs")
public class TransformRaster {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();

		Options options = new Options();

		Option t_srs = new Option("t_srs", "the srs of the target raster");
		t_srs.setRequired(true);
		t_srs.setArgs(1);
		t_srs.setArgName("epsg code");
		options.addOption(t_srs);

		Option s_srs = new Option("s_srs", "the srs of the source raster");
		s_srs.setRequired(true);
		s_srs.setArgs(1);
		s_srs.setArgName("epsg code");
		options.addOption(s_srs);

		Option interpolation = new Option("interpolation",
				"the raster interpolation (nn: nearest neighbour, bl: bilinear");
		interpolation.setArgs(1);
		interpolation.setArgName("nn|bl");
		options.addOption(interpolation);

		Option originLocation = new Option("origin", "originlocation", true,
				"the location of the origin on the upper left pixel (default = center)");
		interpolation.setArgs(1);
		interpolation.setArgName("center|outer");
		options.addOption(originLocation);

		CommandUtils.addDefaultOptions(options);

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(options);
		}

		try {
			CommandLine line = parser.parse(options, args);

			InterpolationType interpolationType = getInterpolationType(line.getOptionValue(interpolation.getOpt()));
			OriginLocation location = getLocation(line.getOptionValue(originLocation.getOpt()));

			transformRaster(line.getArgs(), line.getOptionValue("s_srs"), line.getOptionValue("t_srs"),
					interpolationType, location);

		}
		catch (ParseException exp) {
			System.out.println("ERROR: Invalid command line:" + exp.getMessage());
		}
		System.exit(0);
	}

	/**
	 * @param optionValue
	 * @return
	 */
	private static OriginLocation getLocation(String optionValue) {
		OriginLocation result = OriginLocation.CENTER;
		if ("outer".equalsIgnoreCase(optionValue)) {
			result = OriginLocation.OUTER;
		}

		return result;
	}

	private static void transformRaster(String[] args, String srcCRS, String dstCRS, InterpolationType type,
			OriginLocation location) {
		try {
			MemoryTileContainer tileContainer = new MemoryTileContainer();
			for (int i = 0; i < args.length - 1; i++) {
				if (args[i] != null) {
					File f = new File(args[i]);
					RasterIOOptions options = RasterIOOptions.forFile(f);
					options.add(RasterIOOptions.GEO_ORIGIN_LOCATION, location.name());
					tileContainer.addTile(RasterFactory.loadRasterFromFile(f, options));
				}
			}
			AbstractRaster srcRaster = new TiledRaster(tileContainer, null);
			RasterTransformer transf = new RasterTransformer(dstCRS);
			srcRaster.setCoordinateSystem(CRSManager.getCRSRef(srcCRS));
			AbstractRaster result = transf.transform(srcRaster, type);

			RasterFactory.saveRasterToFile(result, new File(args[args.length - 1]));
		}
		catch (Exception ex) {
			System.err.println("Couldn't transform raster file: ");
			ex.printStackTrace();
			System.exit(2);
		}
	}

	private static void printHelp(Options options) {
		String msg = "This is a command line tool to transform raster files between coordinate systems."
				+ " Multiple input files will be mosaiced into one transformed output file.";
		CommandUtils.printHelp(options, "RasterTransformer", msg, "inputfile [more_inputfiles*] outputfile");
	}

}
