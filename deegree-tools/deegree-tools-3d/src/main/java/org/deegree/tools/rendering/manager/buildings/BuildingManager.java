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

package org.deegree.tools.rendering.manager.buildings;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.deegree.commons.utils.FileUtils;
import org.deegree.rendering.r3d.model.geometry.SimpleGeometryStyle;
import org.deegree.rendering.r3d.opengl.JOGLUtils;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;
import org.deegree.tools.rendering.manager.DataManager;
import org.deegree.tools.rendering.manager.ModelManager;
import org.deegree.tools.rendering.manager.buildings.generalisation.WorldObjectSimplifier;
import org.deegree.tools.rendering.manager.buildings.importers.CityGMLImporter;
import org.deegree.tools.rendering.manager.buildings.importers.VRMLImporter;

/**
 * The <code>BuildingManager</code> imports buildings from vrml or citygml files.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class BuildingManager extends ModelManager<WorldRenderableObject> {

	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BuildingManager.class);

	private final String buildingID;

	private final int qualityLevel;

	private final int numberOfqualityLevels;

	private final boolean shouldCreateLowestlevels;

	private BuildingManager(String textureDir, ModelBackend<?> modelBackend, int numberOfQualityLevels,
			int qualityLevel, String buildingID, boolean createLowestLevels, double[] wpvsTranslationVector) {
		super(textureDir, modelBackend, wpvsTranslationVector);
		this.numberOfqualityLevels = numberOfQualityLevels;
		this.qualityLevel = qualityLevel;
		this.buildingID = buildingID;
		this.shouldCreateLowestlevels = createLowestLevels;
	}

	/**
	 * @param modelBackend the backend.
	 * @param textureDir where the referenced textures may be found.
	 * @param numberOfQualityLevels to be used for the creation of a new building.
	 * @param qualityLevel of the building(s) to be imported.
	 * @param buildingID of the vrml-building.
	 * @param createLowestLevels true if the lowest two levels should be created.
	 * @param wpvsTranslationVector 2d vector to the origin of the wpvs scene
	 */
	public BuildingManager(ModelBackend<?> modelBackend, String textureDir, int numberOfQualityLevels, int qualityLevel,
			String buildingID, boolean createLowestLevels, double[] wpvsTranslationVector) {
		this(textureDir, modelBackend, numberOfQualityLevels, qualityLevel, buildingID, createLowestLevels,
				wpvsTranslationVector);
	}

	@Override
	public BackendResult importFromFile(File f, CommandLine commandLine) throws FileNotFoundException, IOException {
		List<WorldRenderableObject> buildings = readBuildingsFromFile(f, commandLine);
		BackendResult result = insertIntoBackend(buildings);
		flush();
		return result;
	}

	/**
	 * Read the buildings from the given file.
	 * @param f
	 * @param commandLine
	 * @return the created d3 object representations.
	 * @throws IOException
	 */
	protected List<WorldRenderableObject> readBuildingsFromFile(File f, CommandLine commandLine) throws IOException {
		String extension = FileUtils.getFileExtension(f);
		// int result = 0;
		List<WorldRenderableObject> buildings = null;
		if (extension.equalsIgnoreCase("SHP")) {
			buildings = readShape(f.getAbsolutePath());
		}
		else if (extension.equalsIgnoreCase("XML") || extension.equalsIgnoreCase("GML")) {
			buildings = readGML(f.getAbsolutePath(), commandLine);
		}
		else if (extension.equalsIgnoreCase("WRL") || extension.equalsIgnoreCase("VRML")) {
			// importer = new VRMLImporter( );
			// result =
			buildings = readVRML(f.getAbsolutePath(), commandLine);
		}
		return buildings;
	}

	/**
	 * @param buildings
	 * @throws SQLException
	 * @throws IOException
	 */
	private BackendResult insertIntoBackend(List<WorldRenderableObject> buildings) throws IOException {
		if (shouldCreateLowestlevels) {
			createLowestQualityLevel(buildings);
		}
		return readAndImportBuildings(buildings);
	}

	/**
	 * @param buildings
	 */
	private void createLowestQualityLevel(List<WorldRenderableObject> buildings) {
		WorldObjectSimplifier wos = new WorldObjectSimplifier();
		for (WorldRenderableObject worldRenderableObject : buildings) {
			wos.createSimplified3DObject(worldRenderableObject, qualityLevel, 1);
		}
	}

	/**
	 * @param reader
	 * @param connect
	 * @param mappedColumns
	 * @throws IOException
	 * @throws SQLException
	 */
	private BackendResult readAndImportBuildings(List<WorldRenderableObject> buildings) throws IOException {

		List<DataObjectInfo<WorldRenderableObject>> backendInfos = new ArrayList<DataObjectInfo<WorldRenderableObject>>(
				buildings.size());
		for (WorldRenderableObject building : buildings) {
			if (building != null) {
				backendInfos.add(createDataObjectInfo(building));
			}
		}

		return getDbBackend().insert(backendInfos, Type.BUILDING);
	}

	/**
	 * @param building
	 * @return the DataObjectInfo which holds values of the given building.
	 */
	private DataObjectInfo<WorldRenderableObject> createDataObjectInfo(WorldRenderableObject building) {
		return new DataObjectInfo<WorldRenderableObject>(building.getId(), Type.BUILDING.getModelTypeName(),
				building.getName(), building.getExternalReference(), building.getBbox(), building);
	}

	/**
	 * @param f
	 * @return
	 */
	private List<WorldRenderableObject> readShape(String fileName) {
		throw new UnsupportedOperationException(
				"Shape files are currently not supported, not capable of reading file: " + fileName);
	}

	/**
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private List<WorldRenderableObject> readGML(String fileName, CommandLine commandLine) throws IOException {
		LOG.debug("Reading buildings from file: " + fileName);
		String schemaLocation = commandLine.getOptionValue(DataManager.OPT_CITY_GML_SCHEMA);

		String color = commandLine.getOptionValue(DataManager.OPT_CITY_GML_COLOR);
		SimpleGeometryStyle style = new SimpleGeometryStyle();
		if (color != null) {
			try {
				int i = Integer.decode(color);
				int diffuseColor = JOGLUtils.convertColorGLColor(new Color(i));
				style.setDiffuseColor(diffuseColor);
				style.setAmbientColor(diffuseColor);
				style.setSpecularColor(diffuseColor);
			}
			catch (NumberFormatException e) {
				LOG.warn("Could not decode color into an integer: " + color + " using white instead.");
			}
		}

		boolean useOpengis = commandLine.hasOption(DataManager.OPT_USE_OPENGIS);
		CityGMLImporter importer = new CityGMLImporter(schemaLocation,
				new float[] { (float) wpvsTranslationVector[0], (float) wpvsTranslationVector[1], 0 }, style,
				useOpengis);
		return importer.importFromFile(fileName, numberOfqualityLevels, qualityLevel);
	}

	private List<WorldRenderableObject> readVRML(String fileName, CommandLine commandLine) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		String id = buildingID;
		if (buildingID == null) {
			id = FileUtils.getFilename(new File(fileName));
			id = id.replaceAll("\\s", "_");
		}
		params.put("id", id);
		if (textureDir != null) {
			params.put(VRMLImporter.TEX_DIR, textureDir.getAbsolutePath());
		}

		String value = commandLine.getOptionValue(DataManager.OPT_VRML_TRANSLATION_X);
		if (value != null) {
			float x = Float.parseFloat(value);
			float rX = (float) (x + wpvsTranslationVector[0]);
			params.put(VRMLImporter.XTRANS, Float.toString(rX));
		}

		value = commandLine.getOptionValue(DataManager.OPT_VRML_TRANSLATION_Y);
		if (value != null) {
			float y = Float.parseFloat(value);
			float rY = (float) (y + wpvsTranslationVector[1]);
			params.put(VRMLImporter.YTRANS, Float.toString(rY));
		}

		value = commandLine.getOptionValue(DataManager.OPT_VRML_TRANSLATION_Z);
		if (value != null) {
			float z = Float.parseFloat(value);
			float rZ = z;
			params.put(VRMLImporter.ZTRANS, Float.toString(rZ));
		}

		boolean flip = commandLine.hasOption(DataManager.OPT_VRML_FLIP_Y_Z);
		params.put(VRMLImporter.INV_YZ, (flip ? "y" : "n"));

		value = commandLine.getOptionValue(DataManager.OPT_VRML_ROTATION_AXIS);
		if (value != null) {
			params.put(VRMLImporter.ROT_ANGLE, value);
		}

		params.put(VRMLImporter.MAX_TEX_DIM, commandLine.getOptionValue(DataManager.OPT_VRML_MAX_TEX_DIM));

		VRMLImporter exp = new VRMLImporter(params);
		return exp.importFromFile(fileName, numberOfqualityLevels, qualityLevel);
	}

}
