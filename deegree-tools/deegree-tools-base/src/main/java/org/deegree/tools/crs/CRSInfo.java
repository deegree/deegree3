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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.persistence.CRSStore;

/**
 * A utility program to inform the callee about the availability (-isAvailable param) of a
 * certain crs or to retrieve all available crs's from the deegree crs configuration.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
@Tool("Retrieve information about the availability|definition of a certain crs in deegree")
public class CRSInfo {

	/*
	 * Command line options
	 */
	private static final String OPT_IS_AV = "isAvailable";

	private static final String OPT_FILE = "file";

	private static final String OPT_VERIFY = "verify";

	private static final CRSExporterBase exporter = new CRSExporterBase();

	/**
	 * returns true if the the passed SRS is available in deegree
	 * @param srs
	 * @return <code>true</code> if the the passed SRS is available in deegree
	 */
	private boolean isAvailable(String srs) {
		ICRS crs = null;
		try {
			crs = CRSManager.lookup(srs);
		}
		catch (UnknownCRSException e) {
			return false;
		}
		return crs != null;
	}

	private List<ICRS> getAllCoordinateSystems() {
		List<ICRS> crss = new ArrayList<ICRS>();
		Collection<CRSStore> all = CRSManager.getAll();
		for (CRSStore crsStore : all) {
			crss.addAll(crsStore.getAvailableCRSs());
		}
		return crss;
	}

	private List<CRSCodeType[]> getAllCRSCodeTypes() {
		List<CRSCodeType[]> crss = new ArrayList<CRSCodeType[]>();
		Collection<CRSStore> all = CRSManager.getAll();
		for (CRSStore crsStore : all) {
			crss.addAll(crsStore.getAvailableCRSCodes());
		}
		return crss;
	}

	/**
	 * @return a list of crs's with following layout 1) crsid[0], crsid[1] ... etc.
	 */
	private List<String> getAll(boolean verify, File exportFile) {

		List<String> allCRSs = new ArrayList<String>();
		if (verify) {
			List<ICRS> avCRS = getAllCoordinateSystems();
			if (avCRS != null && avCRS.size() > 0) {
				for (ICRS crs : avCRS) {
					CRSCodeType[] ids = crs.getCodes();
					if (ids != null) {
						StringBuilder sb = new StringBuilder(300);
						for (int i = 0; i < ids.length; ++i) {
							sb.append(ids[i].getOriginal());
							if (i + 1 < ids.length) {
								sb.append(", ");
							}
						}
						allCRSs.add(sb.toString());
					}
				}
			}
			if (exportFile != null) {
				StringBuilder out = new StringBuilder(20000000);
				exporter.export(out, avCRS);
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
					bw.write(out.toString());
					bw.flush();
					bw.close();
				}
				catch (IOException e) {
					System.out.println(e);
				}
			}
		}
		else {
			List<CRSCodeType[]> allCodes = getAllCRSCodeTypes();
			for (CRSCodeType[] codes : allCodes)
				if (codes != null) {
					for (CRSCodeType code : codes) {
						allCRSs.add(code.getOriginal());
					}
				}

		}
		Collections.sort(allCRSs);
		return allCRSs;
	}

	/**
	 * @param args following parameters are supported:
	 * <ul>
	 * <li>[-isAvailable srsName]</li>
	 * <li>[-file outputfile]</li>
	 * <li>[-verify]</li>
	 * </ul>
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
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
			verbose = line.hasOption(CommandUtils.OPT_VERBOSE);
			init(line);
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
			printHelp(options);
		}
		catch (Exception e) {
			System.err
				.println("An Exception occurred while querying the crs registry, error message: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
			System.exit(1);
		}
	}

	private static void init(CommandLine line) {
		CRSInfo srsinfo = new CRSInfo();

		String availableCRS = line.getOptionValue(OPT_IS_AV);
		if (availableCRS != null && !"".equals(availableCRS.trim())) {
			System.out.println("Coordinates System: " + availableCRS + " is "
					+ ((srsinfo.isAvailable(availableCRS.trim())) ? "" : "not ") + "available in deegree");
		}
		else {
			boolean verify = line.hasOption(OPT_VERIFY);

			File exportFile = null;
			List<String> availableCRSs = srsinfo.getAll(verify, exportFile);
			if (availableCRSs != null && availableCRSs.size() > 0) {
				String file = line.getOptionValue(OPT_FILE);
				if (file != null && !"".equals(file.trim())) {
					File f = new File(file);
					boolean overwrite = true;
					if (f.exists()) {
						System.out.print("The file: " + file + " already exsists, overwrite ([y]/n): ");
						BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
						String s = "n";
						try {
							s = read.readLine();
						}
						catch (IOException e) {
							// nottin.
						}
						if (s != null && !"".equals(s.trim()) && !"y".equalsIgnoreCase(s.trim())) {
							overwrite = false;
						}

					}
					if (overwrite) {
						System.out.println("Writing to file: " + f.getAbsoluteFile());
						try {
							FileWriter fw = new FileWriter(f);

							int count = 1;
							for (String crs : availableCRSs) {
								fw.write((count++) + ")" + crs + "\n");
								// fw.write( crs );
							}
							fw.close();
						}
						catch (IOException e) {
							System.out.println("An exception occurred while trying to write to file: "
									+ f.getAbsoluteFile() + "\n message:\n" + e.getMessage());
							e.printStackTrace();
						}

						System.exit(1);
					}
					else {
						System.out
							.println("Not overwriting file: " + f.getAbsoluteFile() + ", outputting to standard out.");
					}
				}
				else {
					System.out.println("No File given (-file param) writing to standard out.");
				}
				int count = 1;
				for (String crs : availableCRSs) {
					System.out.println((count++) + ")" + crs);
				}
			}
			else {
				System.out.println("No Coordinate Systems configured, this is very strange!");
			}
		}
	}

	private static Options initOptions() {
		Options options = new Options();
		Option option = new Option("a", OPT_IS_AV, true, "Give an affirmation if the given crs is available");
		option.setArgs(1);
		options.addOption(option);

		option = new Option("f", OPT_FILE, true,
				"If [-isAvailable] is not given, write all configured crs_s to file, if not given standard out will be used");
		option.setArgs(1);
		options.addOption(option);

		options.addOption(OPT_VERIFY, false,
				"if [-isAvailable] is not given, the -verify flag can be used to verify if the provider can create all configured crs_s, thus verifying if the configuration is correct.");

		CommandUtils.addDefaultOptions(options);

		return options;

	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, CRSInfo.class.getCanonicalName(), null, null);
	}

}
