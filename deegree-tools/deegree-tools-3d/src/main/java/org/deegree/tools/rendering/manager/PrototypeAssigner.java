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
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypeReference;
import org.deegree.services.wpvs.exception.DatasourceException;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * The <code>PrototypeAssigner</code> is a tool to assign prototypes to existing
 * buildings.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
@Tool("Interface to the WPVS backend for assigning prototypes to QualityLevel in a world object model.")
public class PrototypeAssigner {

	private static final String DB_HOST = "hosturl";

	private static final String OPT_DB_USER = "user";

	private static final String OPT_DB_PASS = "password";

	private static final String TYPE = "type";

	private static final String QL = "qualitylevel";

	private static final String HELP = "help";

	private static final String BUILDING_ID = "id";

	private static final String PROTOTYPE_ID = "prototype";

	private static final String ROTATION = "rotation";

	private static final String WPVS_TRANSLATION_TO = "wpvs_translation";

	private static final String TRANSLATION = "translation";

	private static final String WIDTH = "width";

	private static final String HEIGHT = "height";

	private static final String DEPTH = "depth";

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

		workspace = new DefaultWorkspace(new File("nix"));

		try {
			CommandLine line = parser.parse(options, args);

			verbose = line.hasOption(OPT_VERBOSE);
			startManager(line);
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

	private static void startManager(CommandLine line)
			throws FileNotFoundException, IOException, UnsupportedOperationException, DatasourceException {

		ModelBackend<?> backend = getModelBackend(line);

		String type = line.getOptionValue(TYPE);
		if (!"building".equalsIgnoreCase(type)) {
			throw new IllegalArgumentException(TYPE + " may only be building.");
		}

		int qualityLevel = -1;
		try {
			qualityLevel = Integer.parseInt(line.getOptionValue(QL));
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(QL + " may only be a number between 0 and 5: " + e.getLocalizedMessage(),
					e);
		}

		double[] translationVector = createTranslationVector(line.getOptionValue(WPVS_TRANSLATION_TO), 2);

		backend.setWPVSTranslationVector(translationVector);

		double[] translation = createTranslationVector(line.getOptionValue(TRANSLATION), 3);

		translation[0] += translationVector[0];
		translation[1] += translationVector[1];

		String buildingID = line.getOptionValue(BUILDING_ID);

		String prototypeID = line.getOptionValue(PROTOTYPE_ID);

		float width = parseNumber(line, WIDTH);

		float height = parseNumber(line, HEIGHT);

		float depth = parseNumber(line, DEPTH);

		float rotation = parseNumber(line, ROTATION);
		long begin = System.currentTimeMillis();
		WorldRenderableObject building = (WorldRenderableObject) backend.getDeSerializedObjectForUUID(Type.BUILDING,
				buildingID);
		if (building != null) {

			PrototypeReference pr = new PrototypeReference(prototypeID, rotation,
					new float[] { (float) translation[0], (float) translation[1] }, width, height, depth);
			RenderableQualityModel modelQL = building.getQualityLevel(qualityLevel);
			if (modelQL != null) {
				modelQL.setPrototype(pr);
			}
			else {
				modelQL = new RenderableQualityModel(pr);
			}
			building.setQualityLevel(qualityLevel, modelQL);
			DataObjectInfo<WorldRenderableObject> updated = new DataObjectInfo<WorldRenderableObject>(buildingID,
					Type.BUILDING.getModelTypeName(), building.getName(), building.getExternalReference(),
					building.getBbox(), building);
			List<DataObjectInfo<WorldRenderableObject>> insert = new ArrayList<DataObjectInfo<WorldRenderableObject>>(
					1);
			insert.add(updated);
			BackendResult inserted = backend.insert(insert, Type.BUILDING);
			System.out.println("Result " + inserted + "\ntook:  " + (System.currentTimeMillis() - begin) + " millis");
		}
		else {
			System.err.println("Unable to retrieve building with id: " + buildingID);
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

	private static float parseNumber(CommandLine line, String id) {
		try {
			return Float.parseFloat(line.getOptionValue(id));
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(id + " must be a number: " + e.getLocalizedMessage(), e);
		}
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

		option = new Option("ql", QL, true,
				"defines the quality level of the data the prototype should be assigned to");
		option.setArgs(1);
		option.setArgName("[0-5]");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("id", BUILDING_ID, true, "UUID of the building to assign the prototype to.");
		option.setArgs(1);
		option.setArgName("The uuid of the building.");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("pid", PROTOTYPE_ID, true, "UUID of the prototype to assign to the building.");
		option.setArgs(1);
		option.setArgName("The uuid of the prototype.");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("tt", WPVS_TRANSLATION_TO, true,
				"A comma seperated translation vector to the nullpoint of the WPVS (see the WPVS-Config for more information).");
		option.setArgs(1);
		option.setRequired(true);
		option.setArgName("e.g. \"-2568000,-5606000\"  .");
		options.addOption(option);

		option = new Option("t", TRANSLATION, true,
				"A comma seperated 3D translation vector to the middle point of the bbox of this building.");
		option.setArgs(1);
		option.setArgName("Building origin e.g. \"2568000,5606000,18\"  .");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("r", ROTATION, true, "Z-Axis rotation in degrees.");
		option.setArgs(1);
		option.setArgName("A rotation in degrees.");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("w", WIDTH, true, "Width of the prototype (x-axis).");
		option.setArgs(1);
		option.setArgName("The width of the building.");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("h", HEIGHT, true, "Height of the prototype (z-axis).");
		option.setArgs(1);
		option.setArgName("The height of the building.");
		option.setRequired(true);
		options.addOption(option);

		option = new Option("d", DEPTH, true, "Depth of the prototype (y-axis).");
		option.setArgs(1);
		option.setArgName("The depth of the building.");
		option.setRequired(true);
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
		CommandUtils.printHelp(options, DataManager.class.getCanonicalName(), null, null);
	}

}
