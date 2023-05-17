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

package org.deegree.tools.crs;

import static org.deegree.commons.tools.CommandUtils.OPT_VERBOSE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.persistence.deegree.d3.DeegreeCRSStore;
import org.deegree.cs.persistence.gml.GMLCRSStore;
import org.deegree.cs.persistence.proj4.PROJ4CRSStore;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;

/**
 * The <code>ConfigurationConverger</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
@Tool("Export the CoordinateSystems from a given Input format to a given Output format and place the result into an output File.")
public class ConfigurationConverger {

	private static final String OPT_IN_FILE = "input";

	private static final String OPT_IN_FORM = "inFormat";

	private static final String OPT_OUT_FILE = "output";

	private static final String OPT_OUT_FORM = "outFormat";

	private enum Format {

		PROJ4, DEEGREE, GML, DATABASE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		static Format fromString(String format) {
			Format result = null;
			if (format != null) {

				String f = format.toLowerCase().trim();
				if (f.contains("proj")) {
					result = PROJ4;
				}
				else if (f.contains("dee")) {
					result = DEEGREE;
				}
				else if (f.contains("gml")) {
					result = GML;
				}
				else if (f.contains("data") || f.contains("db")) {
					result = DATABASE;
				}

			}
			return result;
		}

	}

	/**
	 * Export the CoordinateSystems from a given Input format to a given Output format and
	 * place the result into an output File. If the input format is Proj4, an input File
	 * will be provided. If the -verify option is provided, then the program will check
	 * whether there is an Input-CRS that is not found in an Output-CRS. If this is so,
	 * all Input CRS's will be exported to the Output format.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
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
			init(line);
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
			printHelp(options);
		}
		catch (Exception e) {
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
	 * @throws IOException
	 * @throws TransformationException
	 * @throws IllegalArgumentException
	 * @throws UnknownCRSException
	 * @throws IOException
	 */
	private static void init(CommandLine line) throws IOException {
		String inFile = line.getOptionValue(OPT_IN_FILE);
		Format inFormat = mapToKnownFormat(line.getOptionValue(OPT_IN_FORM));

		// File inputFile = new File( inFile );

		String outFile = line.getOptionValue(OPT_OUT_FILE);
		Format outFormat = mapToKnownFormat(line.getOptionValue(OPT_OUT_FORM));

		Properties inProps = new Properties();
		inProps.put("crs.configuration", inFile);

		DSTransform prefTrans = DSTransform.HELMERT;
		CRSStore in = null;
		switch (inFormat) {
			case DEEGREE:
				in = new DeegreeCRSStore(prefTrans, null);
				break;
			case GML:
				in = new GMLCRSStore(prefTrans);
				break;
			case PROJ4:
				in = new PROJ4CRSStore(prefTrans);
				break;
			default:
				throw new IllegalArgumentException(
						"No crs provider for input format: " + inFormat + " could be determined.");

		}

		CRSExporterBase exporter = null;
		switch (outFormat) {

			case DEEGREE:
				exporter = new CRSExporterBase();
				break;
			case GML:
				break;
			case PROJ4:
				break;
			default:
				throw new IllegalArgumentException(
						"No crs provider for output format: " + outFormat + " could be determined.");

		}

		// List<CoordinateSystem> allSystems = new LinkedList<CoordinateSystem>();
		// allSystems.add( in.getCRSByCode( new CRSCodeType( "3395", "EPSG" ) ) );
		List<ICRS> allSystems = in.getAvailableCRSs();
		StringBuilder sb = new StringBuilder(allSystems.size() * 2000);
		if (exporter != null) {
			exporter.export(sb, allSystems);
			if (outFile != null && !"".equals(outFile.trim())) {
				File outputFile = new File(outFile);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
				writer.write(sb.toString());
				writer.flush();
				writer.close();
			}
			else {
				System.out.println(sb.toString());
			}
		}
		else {
			throw new UnsupportedOperationException("No exporter found for output format: " + outFormat);
		}

	}

	private static Format mapToKnownFormat(String format) {
		Format result = Format.fromString(format);
		if (result == null) {
			throw new IllegalArgumentException("Format: " + format + " is not a known format, possibleValues are:"
					+ Arrays.toString(Format.values()));
		}
		return result;
	}

	private static Options initOptions() {
		Options options = new Options();

		Option option = new Option("f", OPT_IN_FILE, true, "input file to read the crs defintions from (in inFormat).");
		option.setArgs(1);
		option.setRequired(true);
		options.addOption(option);

		option = new Option("o", OPT_OUT_FILE, true, "File to write the new defintions to");
		option.setArgs(1);
		option.setRequired(true);
		options.addOption(option);

		option = new Option("if", OPT_IN_FORM, true,
				"The expected in format, allowed are: " + Arrays.toString(Format.values()));
		option.setArgs(1);
		option.setRequired(true);
		options.addOption(option);

		option = new Option("of", OPT_OUT_FORM, true,
				"The output format, allowed are: " + Arrays.toString(Format.values()));
		option.setArgs(1);
		option.setRequired(true);
		options.addOption(option);

		CommandUtils.addDefaultOptions(options);

		return options;

	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, ConfigurationConverger.class.getCanonicalName(), null, null);
	}

}
