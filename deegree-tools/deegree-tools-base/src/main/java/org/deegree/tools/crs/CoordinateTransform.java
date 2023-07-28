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

package org.deegree.tools.crs;

import static org.deegree.commons.tools.CommandUtils.OPT_VERBOSE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.slf4j.Logger;

/**
 *
 * The <code>DemoCRSTransform</code> is a sa(i)mple application for using deegree
 * coordinate systems and their transformations.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
@Tool("Convert a point or a list of points from one SRS to another.")
public class CoordinateTransform {

	private static final String OPT_S_SRS = "s_srs";

	private static final String OPT_T_SRS = "t_srs";

	private static final String OPT_INVERSE = "inverse";

	private static final String OPT_COORD = "coord";

	private static final String OPT_FILE = "file";

	private static final String OPT_COORD_SEP = "coordSep";

	private static final Logger LOG = getLogger(CoordinateTransform.class);

	private ICRS sourceCRS;

	private ICRS targetCRS;

	/**
	 * Construct a demo crs with following coordinate systems.
	 * @param sourceCRS
	 * @param targetCRS
	 */
	public CoordinateTransform(ICRS sourceCRS, ICRS targetCRS) {
		this.sourceCRS = sourceCRS;
		this.targetCRS = targetCRS;
	}

	/**
	 * This method transforms the given coordinate (in the sourceCRS) into a coordinate of
	 * the targetCRS and back.
	 * @param coordinate to be transformed.
	 * @param withInverse true if the inverse has to be calculated.
	 * @throws TransformationException
	 * @throws IllegalArgumentException
	 * @throws UnknownCRSException
	 */
	public void doTransform(double[] coordinate, boolean withInverse)
			throws IllegalArgumentException, TransformationException, UnknownCRSException {
		CoordinateTransformer ct = new CoordinateTransformer(targetCRS);

		double[] in = Arrays.copyOf(coordinate, 3);
		// point to transform
		double[] out = new double[3];

		outputPoint("The original point in crs: " + sourceCRS.getAlias() + ": ", in, sourceCRS);

		ct.transform(sourceCRS, in, out);

		outputPoint("The transformed point in crs: " + targetCRS.getAlias() + ": ", out, targetCRS);
		if (withInverse) {
			// transform back to source CRS
			ct = new CoordinateTransformer(sourceCRS);
			double[] nIn = new double[3];
			ct.transform(targetCRS, out, nIn);

			outputPoint("The inversed transformed point in crs: " + sourceCRS.getAlias() + ": ", nIn, sourceCRS);
		}

	}

	private void outputPoint(String outputString, double[] coord, ICRS currentCRS) {
		double resultX = coord[0];
		double resultY = coord[1];
		double resultZ = coord[2];
		IUnit[] allUnits = currentCRS.getUnits();
		System.out.println(outputString + resultX + allUnits[0] + ", " + resultY + allUnits[1]
				+ ((currentCRS.getDimension() == 3) ? ", " + resultZ + allUnits[2] : ""));

	}

	/**
	 * a starter method to transform a given point or a serie of points read from a file.
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();

		Options options = initOptions();
		boolean verbose = false;

		// String sourceCRS = "EPSG:25832";
		// String targetCRS = "EPSG:31466";
		// String coord = "370766.738,5685588.661";

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
			init(line);
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
			printHelp(options);
		}
		catch (Throwable e) {
			System.err
				.println("An Exception occurred while transforming your coordinate, error message: " + e.getMessage());
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
	 */
	private static void init(CommandLine line)
			throws IllegalArgumentException, TransformationException, UnknownCRSException, IOException {

		String sourceCRS = line.getOptionValue(OPT_S_SRS);
		String targetCRS = line.getOptionValue(OPT_T_SRS);
		String coord = line.getOptionValue(OPT_COORD);

		ICRS source = CRSManager.lookup(sourceCRS);
		ICRS target = CRSManager.lookup(targetCRS);

		CoordinateTransform demo = new CoordinateTransform(source, target);

		boolean inverse = line.hasOption(OPT_INVERSE);

		if ("".equals(coord)) {
			String sourceFile = line.getOptionValue(OPT_FILE);
			if (sourceFile != null && !"".equals(sourceFile.trim())) {
				String coordSep = line.getOptionValue(OPT_COORD_SEP);
				if (coordSep == null || "".equals(coordSep)) {
					LOG.info(
							"No coordinates separator given (-coordSep parameter), therefore using ' ' (a space) as separator");
					coordSep = " ";
				}
				BufferedReader br = new BufferedReader(new FileReader(sourceFile));
				String coords = br.readLine();

				int lineCount = 1;
				final int sourceDim = source.getDimension();
				List<double[]> coordinateList = new LinkedList<double[]>();
				while (coords != null) {
					if (!coords.startsWith("#")) {
						String[] coordinates = coords.split(coordSep);
						if (coordinates.length != sourceDim) {
							LOG.error(lineCount
									+ ") Each line must contain the number of coordinates fitting the dimension of the source crs ("
									+ sourceDim + ") seperated by a '" + coordSep + "'.");
						}
						else {
							double[] from = new double[3];
							from[0] = Double.parseDouble(coordinates[0].replace(",", "."));
							from[1] = Double.parseDouble(coordinates[1].replace(",", "."));
							if (sourceDim == 3) {
								from[2] = Double.parseDouble(coordinates[2].replace(",", "."));
							}
							coordinateList.add(from);
						}
					}
					coords = br.readLine();
					lineCount++;
				}
				if (coordinateList.isEmpty()) {
					throw new IllegalArgumentException("No valid points found in file: " + sourceFile);
				}
				long time = System.currentTimeMillis();
				for (double[] c : coordinateList) {
					demo.doTransform(c, inverse);
				}
				System.out
					.println("Transformation took: " + ((System.currentTimeMillis() - time) / 1000.) + " seconds");

			}
			else {
				throw new IllegalArgumentException("No coordinate(s) to transform, use the " + OPT_COORD + " or the "
						+ OPT_FILE + " parameter to define a coordinate (list).");
			}
		}
		else {
			String[] splitter = coord.split(",");
			if (splitter == null || splitter.length == 1 || splitter.length > 3) {
				throw new IllegalArgumentException(
						"A coordinate must be comma separated and may only have two or three ordinates e.g. -coord \"3.1415 , 2.7182\"");
			}
			double[] coordinate = new double[3];
			coordinate[0] = Double.parseDouble(splitter[0]);
			coordinate[1] = Double.parseDouble(splitter[1]);
			coordinate[2] = (splitter.length == 3) ? Double.parseDouble(splitter[2]) : 0;
			demo.doTransform(coordinate, inverse);
		}
	}

	private static Options initOptions() {
		Options options = new Options();
		options.addOption("i", OPT_INVERSE, false, "should an inverse operation be applied as well.");
		Option option = new Option("s", OPT_S_SRS, true, "The name of the source srs, e.g. EPSG:4326.");
		option.setArgs(1);
		option.setRequired(true);
		options.addOption(option);

		option = new Option("t", OPT_T_SRS, true, "The name of the target srs, e.g. EPSG:4326.");
		option.setArgs(1);
		option.setRequired(true);
		options.addOption(option);

		option = new Option("c", OPT_COORD, true,
				"Defines a coordinate (comma separated) in the source crs, e.g. '3.1415 , 2.7182'.");
		option.setArgs(1);
		options.addOption(option);

		option = new Option("f", OPT_FILE, true,
				"a /path/of/a_list_of_coordinates.txt containing a list of coordinate pairs/triples. If supplied the -coordSep (the separator between the ordinates will also be evalutated).");
		option.setArgs(1);
		options.addOption(option);

		option = new Option("sep", OPT_COORD_SEP, true,
				"(only valid with -sourceFile) defining a separator between the coords in the file e.g. a ';' or ',' if omitted a space is assumed.");
		option.setArgs(1);
		options.addOption(option);

		CommandUtils.addDefaultOptions(options);

		return options;

	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, CoordinateTransform.class.getCanonicalName(), null, null);
	}

}
