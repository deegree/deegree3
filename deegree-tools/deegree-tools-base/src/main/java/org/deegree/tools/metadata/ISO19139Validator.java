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
package org.deegree.tools.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
@Tool(value = "Validates single metadata records or metadata records from directory against the ISO Schema.")
public class ISO19139Validator {

	private static final String OPT_SRC = "source";

	public enum SCHEMAVERSION {

		V2006, V2007

	}

	private static final String OPT_SCHEMA_VERSION = "schemaVersion";

	private static final String OPT_RESULT = "result";

	private static final String DEFAULT_FILENAME = "validationResult";

	private final boolean verbose;

	/**
	 * @param verbose
	 */
	public ISO19139Validator(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * @param srcOpt
	 * @param schemaOpt
	 * @param resultOpt
	 * @throws IOException
	 * @throws SAXException
	 */
	public void run(String srcOpt, String schemaOpt, String resultOpt) throws IOException, SAXException {
		File src = new File(srcOpt);
		if (!src.exists()) {
			throw new IllegalArgumentException("src does not exist: " + srcOpt + ". Check parameter " + OPT_SRC);
		}
		File result;
		if (resultOpt != null && resultOpt.length() > 0) {
			result = new File(resultOpt);
			if (!result.exists()) {
				result.createNewFile();
			}
		}
		else {
			result = File.createTempFile(DEFAULT_FILENAME, ".txt");
		}

		SCHEMAVERSION schemaVersion = SCHEMAVERSION.V2007;
		if (schemaOpt != null) {
			try {
				schemaVersion = SCHEMAVERSION.valueOf(schemaOpt);
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid argument for " + OPT_SCHEMA_VERSION + ": " + schemaOpt);
			}
		}

		String schema = "/META-INF/SCHEMAS_OPENGIS_NET/iso/19139/20070417/gmd/metadataEntity.xsd";
		if (SCHEMAVERSION.V2006.equals(schemaVersion)) {
			schema = "/META-INF/SCHEMAS_OPENGIS_NET/iso/19139/20060504/gmd/metadataEntity.xsd";
		}
		URL u = ISO19139Validator.class.getResource(schema);
		XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setFeature("http://xml.org/sax/features/validation", true);
		parser.setFeature("http://apache.org/xml/features/validation/schema", true);
		parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
				"http://www.isotc211.org/2005/gmd " + u.toExternalForm());

		final FileWriter fw = new FileWriter(result);

		File[] filesToValidate;
		if (src.isDirectory()) {
			filesToValidate = src.listFiles();
			fw.write("validate " + filesToValidate.length + " files from directory " + src);
			fw.write("\n");
		}
		else {
			filesToValidate = new File[] { src };
		}
		System.out.println("Start validation");
		int noOfValidRecords = 0;
		for (int i = 0; i < filesToValidate.length; i++) {
			FileErrorHandler feh = new FileErrorHandler(fw);
			parser.setErrorHandler(feh);
			if (filesToValidate.length > 1) {
				fw.write("validate record " + i + " of " + filesToValidate.length);
				fw.write("\n");
			}
			File fileToValidate = filesToValidate[i];
			System.out.println(fileToValidate);
			fw.write("validate file " + fileToValidate.getAbsolutePath());
			fw.write("\n");
			try {
				parser.parse(new InputSource(new FileInputStream(fileToValidate)));
			}
			catch (Exception e) {
				String msg = "Could not validate current occured: " + e.getMessage() + ". Continue with next record";
				System.err.println(msg);
				fw.write(msg);
				fw.write("\n");
				continue;
			}
			fw.flush();
			if (feh.isValid())
				noOfValidRecords++;
		}
		fw.write(noOfValidRecords + " of " + filesToValidate.length + " records are valid.");
		fw.close();
		System.out.println("Validation finished, result file: " + result.getAbsolutePath());
	}

	private void write(FileWriter fw, String level, SAXParseException arg0) {
		try {
			fw.write("[" + level + "] ");
			fw.write(arg0.getMessage());
			fw.write("\n");
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
			if (verbose)
				e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(initOptions());
		}
		boolean verbose = true;
		try {
			CommandLine cmdline = new PosixParser().parse(initOptions(), args);
			verbose = cmdline.hasOption(CommandUtils.OPT_VERBOSE);
			try {
				String srcOpt = cmdline.getOptionValue(OPT_SRC);
				String resultOpt = cmdline.getOptionValue(OPT_RESULT);
				String schemaOpt = cmdline.getOptionValue(OPT_SCHEMA_VERSION);

				ISO19139Validator v = new ISO19139Validator(verbose);
				v.run(srcOpt, schemaOpt, resultOpt);
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				if (verbose)
					e.printStackTrace();
			}
		}
		catch (ParseException exp) {
			System.err.println("Could nor parse command line:" + exp.getMessage());
			if (verbose)
				exp.printStackTrace();
		}
	}

	private static Options initOptions() {
		Options opts = new Options();

		Option opt = new Option("s", OPT_SRC, true,
				"the directory containing the metadada records or a single metadata record");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("r", OPT_RESULT, true,
				"A file to store the output of the validation. If the file exist, the result will be appended. The file will be created if not existing. If this parameter is missing a tmp file is created.");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("l", OPT_SCHEMA_VERSION, true, "the schema version to specify the schemas. Must be "
				+ SCHEMAVERSION.V2006 + " or " + SCHEMAVERSION.V2007 + ", default is " + SCHEMAVERSION.V2007);
		opt.setRequired(false);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);
		return opts;
	}

	private static void printHelp(Options options) {
		String help = "Validates single metadata records or metadata records from directory against the ISO Schema.";
		CommandUtils.printHelp(options, ISO19139Validator.class.getSimpleName(), help, null);
	}

	private class FileErrorHandler implements ErrorHandler {

		private final FileWriter fw;

		private boolean isValid = true;

		public FileErrorHandler(FileWriter fw) {
			this.fw = fw;

		}

		@Override
		public void warning(SAXParseException arg0) throws SAXException {
			isValid = false;
			write(fw, "WARN", arg0);
		}

		@Override
		public void fatalError(SAXParseException arg0) throws SAXException {
			isValid = false;
			write(fw, "FATAL", arg0);
		}

		@Override
		public void error(SAXParseException arg0) throws SAXException {
			isValid = false;
			write(fw, "ERROR", arg0);
		}

		public boolean isValid() {
			return isValid;
		}

	}

}
