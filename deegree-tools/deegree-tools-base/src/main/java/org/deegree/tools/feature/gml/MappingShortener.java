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
package org.deegree.tools.feature.gml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.tools.i18n.Messages;

@Tool("Helps creating readable short versions of feature type / property names for mapping to db.")
public class MappingShortener {

	private static final String OPT_INPUT_FILE = "inputfile";

	private static final String OPT_RULES_FILE = "rulesfile";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Options options = initOptions();

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(options);
		}

		try {
			new PosixParser().parse(options, args);

			String inputFileName = options.getOption(OPT_INPUT_FILE).getValue();
			String rulesFileName = options.getOption(OPT_RULES_FILE).getValue();

			LinkedHashMap<String, String> rules = new LinkedHashMap<String, String>();
			BufferedReader reader = new BufferedReader(new FileReader(rulesFileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				int indexOfDelim = line.indexOf('=');
				if (indexOfDelim != -1) {
					String from = line.substring(0, indexOfDelim);
					String to = line.substring(indexOfDelim + 1, line.length());
					rules.put(from, to);
				}
			}
			System.out.println("Loaded " + rules.size() + " rule(s) from " + rulesFileName);

			reader = new BufferedReader(new FileReader(inputFileName));
			line = null;
			int maxLen = 0;
			String maxLenString = "";
			while ((line = reader.readLine()) != null) {
				String shortened = applyRules(line.toLowerCase(), rules);
				System.out.println(line + " -> " + shortened);
				if (shortened.length() > maxLen) {
					maxLen = shortened.length();
					maxLenString = shortened;
				}
			}
			System.err.println("MaxLen: " + maxLen + ", string: " + maxLenString);
		}
		catch (ParseException exp) {
			System.err.println(Messages.getMessage("TOOL_COMMANDLINE_ERROR", exp.getMessage()));
			// printHelp( options );
		}
	}

	private static String applyRules(String s, LinkedHashMap<String, String> rules) {
		String shortened = s;
		for (Entry<?, ?> rule : rules.entrySet()) {
			String from = (String) rule.getKey();
			String to = (String) rule.getValue();
			shortened = shortened.replaceAll(from, to);
		}
		return shortened;
	}

	private static Options initOptions() {

		Options opts = new Options();

		Option opt = new Option("?", "help", false, "print (this) usage information");
		opt.setArgs(0);
		opts.addOption(opt);

		opt = new Option(OPT_INPUT_FILE, true, "input filename (one feature type name / property name per line)");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_RULES_FILE, true, "rules filename (Java properties file with replacement rules)");
		opt.setRequired(true);
		opts.addOption(opt);
		return opts;
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, ApplicationSchemaTool.class.getSimpleName(), null, null);
	}

}
