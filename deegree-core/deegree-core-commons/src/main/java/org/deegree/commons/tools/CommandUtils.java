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
package org.deegree.commons.tools;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class contains some static convenience methods for commandline tools.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class CommandUtils {

	/**
	 * The width of the help text, for automatic text wrap.
	 */
	public static int HELP_TEXT_WIDTH = 80;

	public static final String OPT_VERBOSE = "verbose";

	/**
	 * Prints a help message for a apache commons-cli based command line tool and
	 * <b>terminates the programm</b>.
	 * @param options the options to generate help/usage information for
	 * @param toolName the name of the command line tool
	 * @param helpMsg some further information
	 * @param otherUsageInfo an optional string to append to the usage information (e.g.
	 * for additional arguments like input files)
	 */
	public static void printHelp(Options options, String toolName, String helpMsg, String otherUsageInfo) {
		HelpFormatter formatter = new HelpFormatter();

		StringWriter helpWriter = new StringWriter();
		StringBuffer helpBuffer = helpWriter.getBuffer();
		PrintWriter helpPrintWriter = new PrintWriter(helpWriter);

		helpPrintWriter.println();

		if (helpMsg != null && helpMsg.length() != 0) {
			formatter.printWrapped(helpPrintWriter, HELP_TEXT_WIDTH, helpMsg);
			helpPrintWriter.println();
		}
		formatter.printUsage(helpPrintWriter, HELP_TEXT_WIDTH, toolName, options);
		if (otherUsageInfo != null) {
			helpBuffer.deleteCharAt(helpBuffer.length() - 1); // append additional
																// arguments
			helpBuffer.append(' ').append(otherUsageInfo).append("\n");
		}

		helpBuffer.append("\n");
		formatter.printOptions(helpPrintWriter, HELP_TEXT_WIDTH, options, 3, 5);
		System.err.print(helpBuffer.toString());
		System.exit(1);
	}

	/**
	 * Parse a command line argument as integer.
	 * @param line the parsed command line
	 * @param optName the option name
	 * @param defaultValue the default value
	 * @return the parsed integer argument or <code>defaulValue</code>, if the argument is
	 * missing
	 * @throws ParseException if the argument is not a number
	 */
	public static int getIntOption(CommandLine line, String optName, int defaultValue) throws ParseException {
		String stringValue = line.getOptionValue(optName);
		if (stringValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(stringValue);
		}
		catch (NumberFormatException ex) {
			throw new ParseException(optName + " is not a number");
		}
	}

	/**
	 * Parse a command line argument as float.
	 * @param line the parsed command line
	 * @param optName the option name
	 * @param defaultValue the default value
	 * @return the parsed float argument or <code>defaulValue</code>, if the argument is
	 * missing
	 * @throws ParseException if the argument is not a number
	 */
	public static float getFloatOption(CommandLine line, String optName, float defaultValue) throws ParseException {
		String stringValue = line.getOptionValue(optName);
		if (stringValue == null) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(stringValue);
		}
		catch (NumberFormatException ex) {
			throw new ParseException(optName + " is not a number");
		}
	}

	/**
	 * @param options
	 */
	public static void addDefaultOptions(Options options) {
		options.addOption("v", OPT_VERBOSE, false, "be verbose on errors");
		options.addOption("?", "help", false, "print (this) usage information");
	}

}
