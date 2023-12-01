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

import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.MissingFormatArgumentException;

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
import org.deegree.services.wpvs.exception.DatasourceException;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;
import org.deegree.services.wpvs.io.file.FileBackend;
import org.deegree.tools.rendering.manager.buildings.BuildingManager;
import org.deegree.tools.rendering.manager.buildings.PrototypeManager;
import org.deegree.tools.rendering.manager.stage.StageManager;
import org.deegree.tools.rendering.manager.trees.TreeManager;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * The <code>DataManager</code> is the user interface to the WPVS model backend. It can
 * insert, update and delete following data models:
 * <ul>
 * <li>Trees</li>
 * <li>Buildings</li>
 * <li>Prototypes - which are buildings</li>
 * <li>Stages - which will be handled as prototype references.</li>
 * </ul>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
@Tool("The DataManager inserts, updates and deletes 3d-objects in/from the WPVS backend")
public class DataManager {

	/**
	 * The <code>Action</code> the datamanager may perform
	 *
	 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
	 *
	 */
	public enum Action {

		/**
		 * Perform import action
		 */
		IMPORT,
		/**
		 * perform delete action.
		 */
		DELETE;

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}

	}

	private static final String ACTION = "action";

	private static final String DB_HOST = "hosturl";

	private static final String OPT_FILE_BACKEND_DIR = "file_backend_dir";

	private static final String OPT_DB_USER = "user";

	private static final String OPT_DB_PASS = "password";

	private static final String OPT_FILE = "file";

	private static final String OPT_TEXTURE_DIR = "texturedir";

	private static final String TYPE = "type";

	private static final String QL = "qualitylevel";

	private static final String OPT_CREATE_LOWEST_LEVELS = "create_lowest_levels";

	private static final String OPT_WPVS_TRANSLATION_TO = "wpvs_translation";

	private static final String OPT_DELETE_SQL = "sqlWhere";

	/*
	 * VRML file handling options
	 */
	/**
	 * The commandline argument for the building id
	 */
	public static final String OPT_UUID = "uuid";

	/**
	 * The commandline argument for the translation in x direction.
	 */
	public static final String OPT_VRML_TRANSLATION_X = "vrml_xTranslation";

	/**
	 * The commandline argument for the translation in y direction.
	 */
	public static final String OPT_VRML_TRANSLATION_Y = "vrml_yTranslation";

	/**
	 * The commandline argument for the translation in z direction.
	 */
	public static final String OPT_VRML_TRANSLATION_Z = "vrml_zTranslation";

	/**
	 * The commandline argument for the flipping of y and z
	 */
	public static final String OPT_VRML_FLIP_Y_Z = "vrml_flip_y_z";

	/**
	 * The commandline argument for the maximum size of a texture.
	 */
	public static final String OPT_VRML_MAX_TEX_DIM = "vrml_texture_dimension";

	/**
	 * The commandline argument for the rotation axis definition, a comma separated list
	 * of 4 values, x,y,z,a
	 */
	public static final String OPT_VRML_ROTATION_AXIS = "vrml_rotation_axis";

	/**
	 * The commandline argument for the schemalocation of a citygml file
	 */
	public static final String OPT_CITY_GML_SCHEMA = "citygml_schema_location";

	/**
	 * The commandline argument for the building color of a citygml file
	 */
	public static final String OPT_CITY_GML_COLOR = "citygml_building_color";

	/**
	 * The commandline argument for using the opengis namespace or the citygml namespace.
	 */
	public static final String OPT_USE_OPENGIS = "use_opengis_ns";

	private static Workspace workspace;

	/**
	 * Creates the commandline parser and adds the options.
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();

		Options options = initOptions();
		boolean verbose = false;

		// for the moment, using the CLI API there is no way to respond to a
		// help argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(options);
		}

		workspace = new DefaultWorkspace(new File("nix"));

		try {
			CommandLine line = parser.parse(options, args);
			verbose = line.hasOption(CommandUtils.OPT_VERBOSE);
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

		Action action = null;
		try {
			action = Action.valueOf(line.getOptionValue(ACTION).toUpperCase().trim());
		}
		catch (Exception e) {
			throw new IllegalArgumentException(ACTION + " may only be " + Arrays.toString(Action.values()), e);
		}
		Type type = null;
		try {
			type = Type.valueOf(line.getOptionValue(TYPE).toUpperCase().trim());
		}
		catch (Exception e) {
			throw new IllegalArgumentException(TYPE + " may only be one of " + Arrays.toString(Type.values()), e);
		}

		int qualityLevel = -1;
		try {
			qualityLevel = Integer.parseInt(line.getOptionValue(QL));
		}
		catch (NumberFormatException e) {
			// nothing
		}
		double[] translationVector = createTranslationVector(line.getOptionValue(OPT_WPVS_TRANSLATION_TO));

		backend.setWPVSTranslationVector(translationVector);

		String textureDir = line.getOptionValue(OPT_TEXTURE_DIR);
		String buildingID = line.getOptionValue(OPT_UUID);

		ModelManager<?> manager;
		switch (type) {
			case TREE:
				manager = new TreeManager(backend, textureDir, translationVector);
				break;
			case STAGE:
				manager = new StageManager(backend, 6, qualityLevel, translationVector);
				break;
			case PROTOTYPE:
				manager = new PrototypeManager(backend, textureDir, buildingID, translationVector);
				break;
			default:
				if (Action.IMPORT == action && (qualityLevel < 1) || (qualityLevel > 5)) {
					throw new IllegalArgumentException(QL + " may only be an integer in the range of 1-5.");
				}
				manager = new BuildingManager(backend, textureDir, 6, qualityLevel, buildingID,
						line.hasOption(OPT_CREATE_LOWEST_LEVELS), translationVector);
		}
		if (action == Action.IMPORT) {
			if (textureDir == null) {
				System.out.println("You supplied no texture dir, using user home/textures directory instead.");
				textureDir = System.getProperty("user.home") + "/textures";
			}
			String file = line.getOptionValue(OPT_FILE);
			if (file == null) {
				throw new IllegalArgumentException("Missing parameter: " + OPT_FILE + " it is required for importing.");
			}
			File f = new File(file);
			if (!f.exists()) {
				throw new IllegalArgumentException(OPT_FILE + ": " + f.getAbsolutePath()
						+ ", did not denote an existing file, nothing to import.");
			}

			long begin = System.currentTimeMillis();

			BackendResult inserted = manager.importFromFile(f, line);

			System.out.println("Result " + inserted + "\ntook:  " + (System.currentTimeMillis() - begin) + " millis");

		}
		else {
			String sqlWhere = line.getOptionValue(OPT_DELETE_SQL);
			if (sqlWhere == null || "".equals(sqlWhere)) {
				throw new MissingFormatArgumentException("The sql where clause of the delete action is mandatory.");
			}
			System.out.println("Deleted: " + manager.delete(buildingID, type, qualityLevel, sqlWhere) + " objects.");
		}

	}

	private static ModelBackend<?> getModelBackend(CommandLine line)
			throws UnsupportedOperationException, DatasourceException {

		String testFileBackend = line.getOptionValue(DB_HOST);
		String hostURL = "1";
		String fileBackendDir = null;
		if (testFileBackend.toLowerCase().contains("filebackend")) {
			hostURL = testFileBackend;
			fileBackendDir = line.getOptionValue(OPT_FILE_BACKEND_DIR);
			if (fileBackendDir == null || "".equals(fileBackendDir)) {
				throw new IllegalArgumentException("The filebackend must be supplied with a directory.");
			}
			// TODO integrate this properly
			try {
				File objectsFile = new File(fileBackendDir + "/objects");
				File prototypesFile = new File(fileBackendDir + "/prototypes");
				FileBackend.initFiles(objectsFile);
				FileBackend.initFiles(prototypesFile);
				return new FileBackend(objectsFile, prototypesFile, null);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		else {
			if (workspace.getResource(ConnectionProviderProvider.class, hostURL) == null) {
				ResourceLocation<ConnectionProvider> loc = getSyntheticProvider(hostURL, testFileBackend,
						line.getOptionValue(OPT_DB_USER), line.getOptionValue(OPT_DB_PASS));
				workspace.getLocationHandler().addExtraResource(loc);
			}
		}
		return ModelBackend.getInstance(hostURL, workspace);
	}

	/**
	 * @param optionValue
	 * @return
	 */
	private static double[] createTranslationVector(String optionValue) {
		double[] result = new double[2];
		if (optionValue != null && !"".equals(optionValue)) {
			try {
				result = ArrayUtils.splitAsDoubles(optionValue, ",");
				if (result.length != 2) {
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

		Option option = new Option(OPT_FILE.substring(0, 1), OPT_FILE, true, "the file containing data to import");
		option.setArgs(1);
		option.setArgName("relative/absolut file location");
		options.addOption(option);

		option = new Option("ql", QL, true, "defines the quality level of the data the action should operate on");
		option.setArgs(1);
		option.setArgName("[1-5] if " + TYPE + "=prototype, '1' is expected");
		options.addOption(option);

		option = new Option("cll", OPT_CREATE_LOWEST_LEVELS, false,
				"A flag defining if the lowest levels (convexhull, protoype box ref) should be created for the buildings.");
		options.addOption(option);

		option = new Option("td", OPT_TEXTURE_DIR, true, "Directory where texture can be found.");
		option.setArgs(1);
		option.setArgName("path to the directory");
		options.addOption(option);

		option = new Option("id", OPT_UUID, true,
				"ID of the building to delete or of a vrml file, if not provided the file name will be used.");
		option.setArgs(1);
		option.setArgName("The id of the building.");
		options.addOption(option);

		option = new Option("tt", OPT_WPVS_TRANSLATION_TO, true,
				"A comma separated vector, translation vector to the nullpoint of the WPVS (see the WPVS-Config for more information).");
		option.setArgs(1);
		option.setArgName("e.g. -tt \"-2568000,-5606000\"  .");
		options.addOption(option);

		option = new Option("sw", OPT_DELETE_SQL, true, "SQL where statement which defines a where cause on the db.");
		option.setArgs(1);
		option.setArgName("a delete sql where statement, e.g. \"WHERE id='building_id'");
		options.addOption(option);

		addActionParameters(options);
		addTypeParameters(options);
		addDatabaseParameters(options);
		addVRMLParameters(options);
		addCityGMLParameters(options);

		CommandUtils.addDefaultOptions(options);

		return options;

	}

	/**
	 * @param options
	 */
	private static void addActionParameters(Options options) {
		Option option = new Option(ACTION.substring(0, 1), ACTION, true,
				"defines the action the manager should perform");
		option.setArgs(1);
		StringBuilder argNames = new StringBuilder();
		Action[] allActions = Action.values();
		for (int i = 0; i < allActions.length; ++i) {
			Action a = allActions[i];
			argNames.append(a);
			if ((i + 1) < allActions.length) {
				argNames.append("|");
			}
		}
		option.setArgName(argNames.toString());
		option.setRequired(true);
		options.addOption(option);
	}

	/**
	 * @param options
	 */
	private static void addTypeParameters(Options options) {
		Option option = new Option(TYPE.substring(0, 1), TYPE, true, "defines the type of data the manager expects");
		option.setArgs(1);
		StringBuilder argNames = new StringBuilder();
		Type[] allTypes = Type.values();
		for (int i = 0; i < allTypes.length; ++i) {
			Type a = allTypes[i];
			argNames.append(a);
			if ((i + 1) < allTypes.length) {
				argNames.append("|");
			}
		}
		option.setArgName(argNames.toString());
		option.setRequired(true);
		options.addOption(option);
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

		option = new Option("fbd", OPT_FILE_BACKEND_DIR, true, "directory to be used for the file databackend");
		option.setArgs(1);
		option.setArgName("for example /home/file_backend");
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

	/**
	 * VRML file reading parameters
	 * @param options to add the vrml options to.
	 */
	private static void addVRMLParameters(Options options) {

		Option option = new Option("tx", OPT_VRML_TRANSLATION_X, true, "Translation of the x values of the vrml file.");
		option.setArgs(1);
		option.setArgName("Easting translation.");
		options.addOption(option);

		option = new Option("ty", OPT_VRML_TRANSLATION_Y, true, "Translation of the y values of the vrml file.");
		option.setArgs(1);
		option.setArgName("Northing translation.");
		options.addOption(option);

		option = new Option("tz", OPT_VRML_TRANSLATION_Z, true, "Translation of the z values of the vrml file.");
		option.setArgs(1);
		option.setArgName("Up translation.");
		options.addOption(option);

		option = new Option("ra", OPT_VRML_ROTATION_AXIS, true, "Rotation axis, comma separated values: x,y,z,angle.");
		option.setArgs(1);
		option.setArgName("Rotation axis.");
		options.addOption(option);

		option = new Option("yz", OPT_VRML_FLIP_Y_Z, false, "Flip the y and z coordinates of the vrml file.");
		options.addOption(option);

		option = new Option("mtd", OPT_VRML_MAX_TEX_DIM, true,
				"The maximum dimension of the textures of the given vrml file, will be 'upped' to the power of two.");
		option.setArgs(1);
		option.setArgName("The maximum dimension of a texture.");
		options.addOption(option);

	}

	/**
	 * CityGML file reading parameters
	 * @param options to add the vrml options to.
	 */
	private static void addCityGMLParameters(Options options) {

		Option option = new Option("cgsl", OPT_CITY_GML_SCHEMA, true, "Local location of the citygml schema files.");
		option.setArgs(1);
		option.setArgName("File location.");
		options.addOption(option);

		option = new Option("bc", OPT_CITY_GML_COLOR, true,
				"Default color of the citygml buildings in #FFUUFF format.");
		option.setArgs(1);
		option.setArgName("Default color.");
		options.addOption(option);

		option = new Option("ogcns", OPT_USE_OPENGIS, false,
				"Optional flag for using the opengis namespace for citygml buildings.");
		options.addOption(option);

	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, DataManager.class.getCanonicalName(), null, null);
	}

}
