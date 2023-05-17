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
package org.deegree.tools.feature.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.tools.i18n.Messages;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * Imports feature datasets into a feature store.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
@Tool("Imports feature datasets into a feature store")
public class FeatureStoreLoader {

	// command line parameters
	private static final String OPT_ACTION = "action";

	private static final String OPT_WORKSPACE = "workspace";

	private static final String OPT_FS_CONFIG_FILE = "fsconfig";

	private static final String OPT_DATASET_FILE = "dataset";

	private static final String OPT_INPUT_FORMAT = "format";

	private static final String OPT_IDGEN_MODE = "idgen";

	private enum Action {

		insert, stats

	}

	private static void insert(FeatureStore fs, String datasetFile, GMLVersion gmlVersion, IDGenMode mode)
			throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException,
			FeatureStoreException {

		File f = new File(datasetFile);
		URL url = f.toURI().toURL();
		System.out.print("- Reading dataset: '" + datasetFile + "'...");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(gmlVersion, url);
		gmlReader.setApplicationSchema(fs.getSchema());
		FeatureCollection fc = gmlReader.readFeatureCollection();
		System.out.println("done.");

		FeatureStoreTransaction ta = null;
		try {
			ta = fs.acquireTransaction();
			System.out.print("- Inserting features...");
			List<String> fids = ta.performInsert(fc, mode);
			System.out.println("done.");
			for (String fid : fids) {
				System.out.println("- Inserted: " + fid);
			}
			System.out.println("\n- Insert succeeded (" + fids.size() + " features). Committing transaction.");
			ta.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error performing insert: " + e.getMessage());
			if (ta != null) {
				ta.rollback();
			}
		}
	}

	/**
	 * @param args
	 * @throws FeatureStoreException
	 * @throws UnknownCRSException
	 * @throws IOException
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 * @throws ResourceInitException
	 */
	public static void main(String[] args) throws FeatureStoreException, XMLParsingException, XMLStreamException,
			FactoryConfigurationError, IOException, UnknownCRSException, ResourceInitException {

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(initOptions());
		}

		try {
			CommandLine cmdline = new PosixParser().parse(initOptions(), args);

			Action action = null;
			try {
				action = Action.valueOf(cmdline.getOptionValue(OPT_ACTION));
			}
			catch (IllegalArgumentException e) {
				System.out.println("Unknown action '" + cmdline.getOptionValue(OPT_ACTION)
						+ "'. Call with '-help' for displaying available actions.");
				System.exit(0);
			}

			String workspace = cmdline.getOptionValue(OPT_WORKSPACE);
			String fsConfigId = cmdline.getOptionValue(OPT_FS_CONFIG_FILE);

			GMLVersion format = null;
			try {
				format = GMLVersion.valueOf(cmdline.getOptionValue(OPT_INPUT_FORMAT));
			}
			catch (IllegalArgumentException e) {
				System.out.println("Unknown input format '" + cmdline.getOptionValue(OPT_INPUT_FORMAT)
						+ "'. Call with '-help' for displaying valid formats.");
				System.exit(0);
			}

			IDGenMode idGenMode = null;
			try {
				idGenMode = IDGenMode.valueOf(cmdline.getOptionValue(OPT_IDGEN_MODE));
				if (idGenMode == IDGenMode.REPLACE_DUPLICATE) {
					throw new IllegalArgumentException();
				}
			}
			catch (IllegalArgumentException e) {
				System.out.println("Unknown id generation mode '" + cmdline.getOptionValue(OPT_IDGEN_MODE)
						+ "'. Call with '-help' for displaying valid modes.");
				System.exit(0);
			}

			String inputFileName = cmdline.getOptionValue(OPT_DATASET_FILE);

			Workspace ws = new DefaultWorkspace(new File(workspace));
			ws.initAll();

			FeatureStore fs = ws.getResource(FeatureStoreProvider.class, fsConfigId);

			switch (action) {
				case insert:
					insert(fs, inputFileName, format, idGenMode);
					break;
				case stats:
					System.out.println("TODO: Stats...");
					break;
			}
		}
		catch (ParseException exp) {
			System.err.println(Messages.getMessage("TOOL_COMMANDLINE_ERROR", exp.getMessage()));
			// printHelp( options );
		}
	}

	private static Options initOptions() {

		Options opts = new Options();

		String actionsList = "";
		Action[] actions = Action.values();
		actionsList += actions[0].toString();
		for (int i = 1; i < actions.length; i++) {
			actionsList += ", " + actions[i];
		}

		Option opt = new Option(OPT_ACTION, true, "action, one of: " + actionsList + "");
		opt.setRequired(true);
		opts.addOption(opt);

		String formatsList = "";
		GMLVersion[] formats = GMLVersion.values();
		formatsList += formats[0].name();
		for (int i = 1; i < formats.length; i++) {
			formatsList += ", " + formats[i].name();
		}

		opt = new Option(OPT_WORKSPACE, true, "workspace name");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_FS_CONFIG_FILE, true, "feature store config id");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_INPUT_FORMAT, true, "input format, one of: " + formatsList + "");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_IDGEN_MODE, true, "id generation mode, one of: GENERATE_NEW,USE_EXISTING");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_DATASET_FILE, true, "dataset filename");
		opt.setRequired(true);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);
		return opts;
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, FeatureStoreLoader.class.getSimpleName(), null, null);
	}

}
