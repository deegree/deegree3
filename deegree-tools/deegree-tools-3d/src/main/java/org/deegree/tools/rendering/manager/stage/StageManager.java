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

package org.deegree.tools.rendering.manager.stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.deegree.commons.dataaccess.CSVReader;
import org.deegree.commons.utils.FileUtils;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypeReference;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;
import org.deegree.tools.rendering.manager.ModelManager;

/**
 * The <code>StageManager</code> imports stage definitions from a csv file.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class StageManager extends ModelManager<WorldRenderableObject> {

	private static final GeometryFactory geomFac = new GeometryFactory();

	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StageManager.class);

	private Set<String> usedTextures = new HashSet<String>();

	private Set<String> invalidTextures = new HashSet<String>();

	private int qualityLevel;

	private int numberOfLevels;

	/**
	 *
	 * The <code>Column</code> enum can be used to map csv columns to the appropriate
	 * value.
	 *
	 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
	 *
	 */
	private enum Column {

		/**
		 * the height of the stage.
		 */
		HEIGHT("The height of the stage (a floating point value)."),
		/**
		 * The image of the stage.
		 */
		IMAGE("The image file name (not the path) of the stage, to be used for (future) rendering."),
		/**
		 * the type of the stage.
		 */
		TYPE("A string denoting the type of the stage, e.g. 1,2,3..."),
		/**
		 * the northing of the stage.
		 */
		NORTHING("The northing of the stage in some crs (a floating point value)."),
		/**
		 * the easting of the stage.
		 */
		EASTING("The easting of the stage in some crs (a floating point value)."),
		/**
		 *
		 */
		GROUND_LEVEL("The height of the root of the stage (terrain height), (a floating point value)."),
		/**
		 * the id of the stage.
		 */
		ID("The id of the stage (a string)."),
		/**
		 * The name of the stage
		 */
		NAME("The name of the stage."),
		/**
		 * The referenced prototype
		 */
		PROTOTYPE("The referenced prototype."),
		/**
		 * The actual stage number.
		 */
		STAGE_NR("The number.");

		private final String description;

		private Column(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		/**
		 * @return the description
		 */
		public final String getDescription() {
			return description;
		}

	}

	/**
	 * @param modelBackend containing parameters for the database
	 * @param numberOfLevels of this stage model
	 * @param qualityLevel to add the stages.
	 * @param wpvsTranslationVector 2d vector to the origin of the wpvs scene
	 */
	public StageManager(ModelBackend<?> modelBackend, int numberOfLevels, int qualityLevel,
			double[] wpvsTranslationVector) {
		super(null, modelBackend, wpvsTranslationVector);
		this.numberOfLevels = numberOfLevels;
		this.qualityLevel = qualityLevel;
	}

	@Override
	public BackendResult importFromFile(File f, CommandLine commandLine) throws IOException {
		if (f == null || !f.exists()) {
			throw new FileNotFoundException("File may not be null, and must point to a valid csv file. ");
		}
		CSVReader reader = new CSVReader(new FileReader(f), ";", true);
		if (!"csv".equals(FileUtils.getFileExtension(f))) {
			LOG.info("File extension of the stage file was not 'csv', trying to load anyway.");
		}
		Map<Column, Integer> mappedColumns = mapColumns(reader.getColumnsNames());
		int maxLength = Column.values().length;
		for (Integer i : mappedColumns.values()) {
			maxLength = Math.max(maxLength, i);
		}
		BackendResult result = readAndImportStages(reader, mappedColumns, maxLength);
		LOG.info("Number of referenced textures: " + usedTextures.size());
		// if ( LOG.isTraceEnabled() ) {
		StringBuilder sb = new StringBuilder("Following textures were referenced:\n");
		for (String s : usedTextures) {
			sb.append((invalidTextures.contains(s) ? "Not Found: " : "")).append(s).append("\n");
		}
		LOG.info(sb.toString());
		FileWriter fw = new FileWriter(new File("/tmp/missing_textures.txt"));
		for (String s : invalidTextures) {
			fw.write(s + "\n");
		}
		fw.close();

		// }
		return result;
	}

	/**
	 * @param reader
	 * @param mappedColumns
	 * @param maxLength
	 * @return
	 * @throws IOException
	 */
	private BackendResult readAndImportStages(CSVReader reader, Map<Column, Integer> mappedColumns, int maxLength)
			throws IOException {
		String[] values = reader.parseLine();
		FileWriter fw = null;
		if (LOG.isTraceEnabled()) {
			fw = new FileWriter(File.createTempFile("missing_values", ".txt"));
		}

		List<DataObjectInfo<WorldRenderableObject>> inserts = new LinkedList<DataObjectInfo<WorldRenderableObject>>();
		while (values != null) {
			if (values.length >= maxLength) {
				String uuid = values[mappedColumns.get(Column.ID)];
				WorldRenderableObject wro = null;
				try {
					wro = createWRO(mappedColumns, values);
				}
				catch (IllegalArgumentException e) {
					LOG.error("Line( " + reader.getLineNumber() + "):  " + e.getLocalizedMessage());
				}
				if (wro != null) {
					inserts.add(createDataObjectInfo(uuid, values[mappedColumns.get(Column.TYPE)],
							values[mappedColumns.get(Column.IMAGE)], wro));
				}
			}
			else {
				LOG.warn("Line( " + reader.getLineNumber() + "): not enough elements parsed: "
						+ Arrays.toString(values));
				if (fw != null) {
					fw.write("Line( " + reader.getLineNumber() + "): not enough elements parsed: "
							+ Arrays.toString(values) + "\n");
				}
			}
			values = reader.parseLine();
		}
		if (fw != null) {
			fw.flush();
			fw.close();
		}
		ModelBackend<?> backend = getDbBackend();
		BackendResult result = backend.insert(inserts, Type.STAGE);
		flush();
		return result;
	}

	/**
	 * @param building
	 * @return
	 */
	private DataObjectInfo<WorldRenderableObject> createDataObjectInfo(String uuid, String type, String name,
			WorldRenderableObject stage) {
		Envelope envelope = stage.getBbox();
		return new DataObjectInfo<WorldRenderableObject>(uuid, type, name, uuid, envelope, stage);
	}

	/**
	 * @param ps
	 * @param b
	 * @param index
	 * @throws SQLException
	 */
	private Envelope getEnvelope(float[] location, float width, float height, float depth) {
		return geomFac.createEnvelope(new double[] { location[0], location[1], location[2] },
				new double[] { location[0] + width, location[1] + depth, location[2] + height }, null);

	}

	/**
	 * @param mappedColumns
	 * @param values
	 * @return
	 */
	private WorldRenderableObject createWRO(Map<Column, Integer> mappedColumns, String[] values) {
		float height = parseFloatingPoint(values[mappedColumns.get(Column.HEIGHT)], Column.HEIGHT);
		float width = 1;
		float depth = 1;
		float northing = parseFloatingPoint(values[mappedColumns.get(Column.NORTHING)], Column.NORTHING);
		northing += wpvsTranslationVector[1];
		float easting = parseFloatingPoint(values[mappedColumns.get(Column.EASTING)], Column.EASTING);
		easting += wpvsTranslationVector[0];
		float groundLevel = parseFloatingPoint(values[mappedColumns.get(Column.GROUND_LEVEL)], Column.GROUND_LEVEL);

		String protoTypeRef = values[mappedColumns.get(Column.PROTOTYPE)];
		float[] location = new float[] { easting, northing, groundLevel };
		PrototypeReference reference = new PrototypeReference(protoTypeRef, 0, location, width, height, depth);

		RenderableQualityModel[] qualityLevels = new RenderableQualityModel[numberOfLevels];
		Envelope env = getEnvelope(location, width, height, depth);
		qualityLevels[qualityLevel] = new RenderableQualityModel(reference);

		return new WorldRenderableObject(values[mappedColumns.get(Column.ID)], null, env, qualityLevels);
	}

	private float parseFloatingPoint(String f, Column c) {
		try {
			return Float.parseFloat(f);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(c.toString() + ": " + f + " is not a floating point value.");
		}
	}

	/**
	 * @param columnsNames
	 * @return
	 */
	private Map<Column, Integer> mapColumns(String[] columnsNames) {
		if (columnsNames == null) {
			throw new IllegalArgumentException(
					"Columnnames must be specified, (they should be the first line of the csv document)");
		}
		if (columnsNames.length < Column.values().length) {
			throw new IllegalArgumentException(
					"Not enough columns specified, at least: " + Column.values().length + " columns are expected.");
		}
		Map<Column, Integer> result = new HashMap<Column, Integer>();
		for (int i = 0; i < columnsNames.length; ++i) {
			String s = columnsNames[i];
			try {
				Column c = Column.valueOf(s.toUpperCase());
				result.put(c, i);
			}
			catch (Exception e) {
				LOG.warn("Could not map: " + s + " to a known column name, column names must be one of: "
						+ Arrays.toString(Column.values()));
			}
		}
		boolean columnsCheckout = true;
		for (Column c : Column.values()) {
			if (!result.containsKey(c)) {
				LOG.warn("Missing column: " + c.name().toLowerCase() + ", " + c.getDescription());
				columnsCheckout = false;
			}

		}
		if (!columnsCheckout) {
			throw new IllegalArgumentException(
					"Your csv file misses some columns (see above messages, for more information on which columns are missing), cannot proceed without given information.");
		}

		return result;
	}

}
