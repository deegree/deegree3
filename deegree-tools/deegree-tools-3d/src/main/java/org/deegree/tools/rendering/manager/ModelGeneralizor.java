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

package org.deegree.tools.rendering.manager;

import static org.deegree.commons.tools.CommandUtils.OPT_VERBOSE;
import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.services.wpvs.exception.DatasourceException;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;
import org.deegree.tools.rendering.manager.buildings.generalisation.WorldObjectSimplifier;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * The <code>PrototypeAssigner</code> is a tool to create generalisation from existing
 * buildings.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
@Tool("Generates a generalization of a building by projecting the boundaries on a 2d plane and calculating a convex hull from them.")
public class ModelGeneralizor {

	private static final String DB_HOST = "hosturl";

	private static final String OPT_DB_USER = "user";

	private static final String OPT_DB_PASS = "password";

	private static final String TYPE = "type";

	private static final String TARGET_QL = "target_quality_level";

	private static final String SOURCE_QL = "source_quality_level";

	private static final String WPVS_TRANSLATION_TO = "wpvs_translation";

	private static final String SQL_WHERE = "sqlWhere";

	private static Workspace workspace;

	/**
	 * Creates the commandline parser and adds the options.
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();

		Options options = initOptions();
		boolean verbose = false;

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(options);
		}

		workspace = new DefaultWorkspace(new File("test"));

		try {
			CommandLine line = parser.parse(options, args);
			verbose = line.hasOption(OPT_VERBOSE);
			startModelGeneralizor(line);
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
			System.exit(1);
		}
		// System.exit( 0 );
	}

	private static void startModelGeneralizor(CommandLine line)
			throws FileNotFoundException, IOException, UnsupportedOperationException, DatasourceException {

		ModelBackend<?> backend = getModelBackend(line);

		String type = line.getOptionValue(TYPE);
		if (!"building".equalsIgnoreCase(type)) {
			throw new IllegalArgumentException(TYPE + " may only be building.");
		}

		int targetQL = -1;
		try {
			targetQL = Integer.parseInt(line.getOptionValue(TARGET_QL));
			// if ( targetQL > 1 || targetQL < 0 ) {
			// throw new IllegalArgumentException( TARGET_QL + " may only be 0 or 1" );
			// }
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(TARGET_QL + " may only be a number 0 or 1: " + e.getLocalizedMessage(),
					e);
		}

		int sourceQL = -1;
		try {
			sourceQL = Integer.parseInt(line.getOptionValue(SOURCE_QL));
			if (targetQL > 5 || targetQL <= 1) {
				throw new IllegalArgumentException(SOURCE_QL + " " + sourceQL + " may only be between 2 and 5");
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					SOURCE_QL + " may only be a number betwee 2 and 5: " + e.getLocalizedMessage(), e);
		}

		double[] translationVector = createTranslationVector(line.getOptionValue(WPVS_TRANSLATION_TO), 2);

		backend.setWPVSTranslationVector(translationVector);

		String sqlWhere = line.getOptionValue(SQL_WHERE);

		long begin = System.currentTimeMillis();
		List<Object> buildings = backend.getDeSerializedObjectsForSQL(Type.BUILDING, sqlWhere);
		if (!buildings.isEmpty()) {
			WorldObjectSimplifier wos = new WorldObjectSimplifier();
			List<DataObjectInfo<WorldRenderableObject>> insert = new ArrayList<DataObjectInfo<WorldRenderableObject>>(
					buildings.size());
			for (Object obj : buildings) {
				if (obj != null) {
					if (obj instanceof WorldRenderableObject) {
						WorldRenderableObject building = (WorldRenderableObject) obj;
						wos.createSimplified3DObject(building, sourceQL, targetQL);
						DataObjectInfo<WorldRenderableObject> updated = new DataObjectInfo<WorldRenderableObject>(
								building.getId(), Type.BUILDING.getModelTypeName(), building.getName(),
								building.getExternalReference(), building.getBbox(), building);
						insert.add(updated);
					}
					else {
						System.err.println("retrieved object was not a WorldRenderable object, this is strange.");
					}
				}
			}

			BackendResult inserted = backend.insert(insert, Type.BUILDING);
			System.out.println("Result " + inserted + "\ntook:  " + (System.currentTimeMillis() - begin) + " millis");
		}
		else {
			System.err.println("Unable to retrieve buildings for sql statement: " + sqlWhere);
		}
	}

	private static ModelBackend<?> getModelBackend(CommandLine line)
			throws UnsupportedOperationException, DatasourceException {
		String id = "1";
		if (workspace.getResource(ConnectionProviderProvider.class, id) == null) {
			ResourceLocation<ConnectionProvider> loc = getSyntheticProvider(id, line.getOptionValue(DB_HOST),
					line.getOptionValue(OPT_DB_USER), line.getOptionValue(OPT_DB_PASS));
			workspace.getLocationHandler().addExtraResource(loc);
		}

		return ModelBackend.getInstance(id, null);
	}

	/**
	 * @param optionValue
	 * @return
	 */
	private static double[] createTranslationVector(String optionValue, int size) {
		double[] result = new double[size];
		if (optionValue != null && !"".equals(optionValue)) {
			try {
				result = ArrayUtils.splitAsDoubles(optionValue, ",");
				if (result.length != size) {
					throw new NumberFormatException(
							"Illigal number of values, only two dimensional translations are allowed");
				}
			}
			catch (NumberFormatException e) {
				System.err.println("Translation vector " + optionValue
						+ " could not be read, please make sure it is a comma seperated list of (floating point) numbers: "
						+ e.getLocalizedMessage());
			}
		}
		return result;
	}

	private static Options initOptions() {
		Options options = new Options();

		Option option = new Option("type", TYPE, true, "the type of object to assign a prototype to (building)");
		option.setArgs(1);
		option.setArgName("building");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("ql", TARGET_QL, true,
				"defines the quality level of the data the generalistation should be assigned to");
		option.setArgs(1);
		option.setArgName("[0,1]");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("source", SOURCE_QL, true,
				"defines the quality level of the data from which the generalistation should be constructed from");
		option.setArgs(1);
		option.setArgName("[2,5]");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("tt", WPVS_TRANSLATION_TO, true,
				"A comma seperated translation vector to the nullpoint of the WPVS (see the WPVS-Config for more information).");
		option.setArgs(1);
		option.setRequired(true);
		option.setArgName("e.g. \"-2568000,-5606000\"  .");
		options.addOption(option);

		option = new Option("sw", SQL_WHERE, true, "SQL where statement which defines a where cause on the db.");
		option.setArgs(1);
		option.setArgName("\"WHERE id='building_id'\"");
		options.addOption(option);

		addDatabaseParameters(options);

		CommandUtils.addDefaultOptions(options);
		return options;

	}

	/**
	 * Database parameters
	 * @param options to add the database option to.
	 */
	private static void addDatabaseParameters(Options options) {

		Option option = new Option("host", DB_HOST, true, "url to the database, with or without port");
		option.setArgs(1);
		option.setArgName("for example jdbc:postgresql://dbhost:5432/db_name");
		option.setRequired(true);
		options.addOption(option);

		option = new Option(OPT_DB_USER.substring(0, 1), OPT_DB_USER, true,
				"username of the database, default will be ${user.name}");
		option.setArgs(1);
		option.setArgName("for example postgres");
		options.addOption(option);

		option = new Option(OPT_DB_PASS.substring(0, 1), OPT_DB_PASS, true,
				"password of the database, default will be empty");
		option.setArgs(1);
		option.setArgName("for example my_secret_password");
		options.addOption(option);

	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, ModelGeneralizor.class.getSimpleName(), null, null);
	}

}
