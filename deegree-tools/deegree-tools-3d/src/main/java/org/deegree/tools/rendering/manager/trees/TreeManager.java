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

package org.deegree.tools.rendering.manager.trees;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.BillBoard;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;
import org.deegree.tools.rendering.manager.ModelManager;

/**
 * The <code>TreeImporter</code> import tree definitions from a csv file.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class TreeManager extends ModelManager<BillBoard> {

	private static final GeometryFactory geomFac = new GeometryFactory();

	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeManager.class);

	private Set<String> usedTextures = new HashSet<String>();

	private Set<String> invalidTextures = new HashSet<String>();

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
		 * the height of the tree.
		 */
		HEIGHT("The height of the tree (a floating point value)."),
		/**
		 * The width of the tree.
		 */
		WIDTH("The width of the tree (a floating point value)."),
		/**
		 * The image of the tree.
		 */
		IMAGE("The image file name (not the path) of the tree, to be used for rendering."),
		/**
		 * the type of the tree.
		 */
		TYPE("A string denoting the type of the tree, e.g. platanus, sparch..."),
		/**
		 * the northing of the tree.
		 */
		NORTHING("The northing of the tree in some crs (a floating point value)."),
		/**
		 * the easting of the tree.
		 */
		EASTING("The easting of the tree in some crs (a floating point value)."),
		/**
		 *
		 */
		GROUND_LEVEL("The height of the root of the tree (terrain height), (a floating point value)."),
		/**
		 * the id of the tree.
		 */
		ID("The id of the tree (a string).");

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
	 * @param textureDir where the referenced textures may be found.
	 * @param wpvsTranslationVector 2d vector to the origin of the wpvs scene
	 */
	public TreeManager(ModelBackend<?> modelBackend, String textureDir, double[] wpvsTranslationVector) {
		super(textureDir, modelBackend, wpvsTranslationVector);

	}

	@Override
	public BackendResult importFromFile(File f, CommandLine commandLine) throws IOException {
		if (f == null || !f.exists()) {
			throw new FileNotFoundException("File may not be null, and must point to a valid csv file. ");
		}
		CSVReader reader = new CSVReader(new FileReader(f), ";", true);
		if (!"csv".equals(FileUtils.getFileExtension(f))) {
			LOG.info("File extension of the tree file was not 'csv', trying to load anyway.");
		}
		Map<Column, Integer> mappedColumns = mapColumns(reader.getColumnsNames());
		int maxLength = Column.values().length;
		for (Integer i : mappedColumns.values()) {
			maxLength = Math.max(maxLength, i);
		}
		BackendResult result = readAndImportBillboards(reader, mappedColumns, maxLength);
		LOG.info("Number of referenced textures: " + usedTextures.size());
		StringBuilder sb = new StringBuilder("Following textures were referenced:\n");
		for (String s : usedTextures) {
			sb.append((invalidTextures.contains(s) ? "Not Found: " : "")).append(s).append("\n");
		}
		LOG.info(sb.toString());

		return result;
	}

	/**
	 * @param reader
	 * @param mappedColumns
	 * @param maxLength
	 * @return
	 * @throws IOException
	 */
	private BackendResult readAndImportBillboards(CSVReader reader, Map<Column, Integer> mappedColumns, int maxLength)
			throws IOException {
		String[] values = reader.parseLine();
		List<DataObjectInfo<BillBoard>> inserts = new LinkedList<DataObjectInfo<BillBoard>>();
		while (values != null) {
			if (values.length >= maxLength) {
				String uuid = "" + Integer.parseInt(values[mappedColumns.get(Column.ID)]);
				BillBoard b = null;
				try {
					b = createBillBoard(mappedColumns, values);
				}
				catch (IllegalArgumentException e) {
					LOG.error("Line( " + reader.getLineNumber() + "):  " + e.getLocalizedMessage());
				}
				if (b != null) {
					inserts.add(createDataObjectInfo(uuid, values[mappedColumns.get(Column.TYPE)],
							values[mappedColumns.get(Column.IMAGE)], b));
				}
			}
			else {
				LOG.warn("Line( " + reader.getLineNumber() + "): not enough elements parsed: "
						+ Arrays.toString(values));
			}
			values = reader.parseLine();
		}
		ModelBackend<?> backend = getDbBackend();
		BackendResult result = backend.insert(inserts, Type.TREE);
		flush();
		return result;
	}

	/**
	 * @param building
	 * @return
	 */
	private DataObjectInfo<BillBoard> createDataObjectInfo(String uuid, String type, String name, BillBoard tree) {
		Envelope envelope = getEnvelope(tree);
		return new DataObjectInfo<BillBoard>(uuid, type, name, uuid, envelope, tree);
	}

	/**
	 * @param ps
	 * @param b
	 * @param index
	 * @throws SQLException
	 */
	private Envelope getEnvelope(BillBoard b) {
		float[] location = b.getLocation();
		float width = b.getWidth() * 0.5f;
		float height = b.getHeight();
		return geomFac.createEnvelope(new double[] { location[0] - width, location[1] - width, location[2] },
				new double[] { location[0] + width, location[1] + width, location[2] + height }, null);

	}

	/**
	 * @param mappedColumns
	 * @param values
	 * @return
	 */
	private BillBoard createBillBoard(Map<Column, Integer> mappedColumns, String[] values) {
		float height = parseFloatingPoint(values[mappedColumns.get(Column.HEIGHT)], Column.HEIGHT);
		float width = parseFloatingPoint(values[mappedColumns.get(Column.WIDTH)], Column.WIDTH);
		float northing = parseFloatingPoint(values[mappedColumns.get(Column.NORTHING)], Column.NORTHING);
		northing += wpvsTranslationVector[1];
		float easting = parseFloatingPoint(values[mappedColumns.get(Column.EASTING)], Column.EASTING);
		easting += wpvsTranslationVector[0];

		float groundLevel = parseFloatingPoint(values[mappedColumns.get(Column.GROUND_LEVEL)], Column.GROUND_LEVEL);
		String texture = values[mappedColumns.get(Column.IMAGE)];
		if (!usedTextures.contains(texture)) {
			if (!checkTextureReference(texture)) {
				invalidTextures.add(texture);
				LOG.warn("Texture: " + texture + " does not denote an image in the textureDir: "
						+ textureDir.getAbsolutePath() + " is this correct?.");
			}
			usedTextures.add(texture);
		}
		return new BillBoard(texture, new float[] { easting, northing, groundLevel }, width, height);
	}

	/**
	 * @param texture to check for.
	 */
	private boolean checkTextureReference(String texture) {
		return textureDir == null || new File(textureDir, texture).exists();
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
