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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModelPart;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackend.Type;

/**
 * The <code>PrototypeManager</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class PrototypeManager extends BuildingManager {

	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PrototypeManager.class);

	/**
	 * @param modelBackend
	 * @param textureDir
	 * @param buildingID
	 * @param wpvsTranslationVector 2d vector to the origin of the wpvs scene
	 */
	public PrototypeManager(ModelBackend<?> modelBackend, String textureDir, String buildingID,
			double[] wpvsTranslationVector) {
		super(modelBackend, textureDir, 1, 0, buildingID, false, wpvsTranslationVector);
	}

	@Override
	public BackendResult importFromFile(File f, CommandLine commandLine) throws FileNotFoundException, IOException {
		List<WorldRenderableObject> protoTypes = readBuildingsFromFile(f, commandLine);
		BackendResult result = insertIntoBackend(protoTypes);
		flush();
		return result;
	}

	/**
	 * @param buildings
	 * @return the result of the insertion.
	 * @throws IOException
	 */
	protected BackendResult insertIntoBackend(List<WorldRenderableObject> buildings) throws IOException {
		return readAndImportPrototypes(buildings);
	}

	/**
	 * @param reader
	 * @param connect
	 * @param mappedColumns
	 * @throws IOException
	 * @throws SQLException
	 */
	private BackendResult readAndImportPrototypes(List<WorldRenderableObject> buildings) throws IOException {

		List<DataObjectInfo<RenderablePrototype>> backendInfos = new ArrayList<DataObjectInfo<RenderablePrototype>>(
				buildings.size());
		for (WorldRenderableObject building : buildings) {
			if (building != null) {
				backendInfos.add(createDataObjectInfo(building));
			}
		}

		return getDbBackend().insert(backendInfos, Type.PROTOTYPE);
	}

	/**
	 * @param building
	 * @return the DataObjectInfo which holds values of the given building.
	 */
	private DataObjectInfo<RenderablePrototype> createDataObjectInfo(WorldRenderableObject building) {

		RenderableQualityModel rqm = building.getQualityLevel(0);
		if (rqm == null) {
			LOG.info("Could not extract the quality level of the RenderablePrototype with id:" + building.getId());
			return null;
		}
		rqm = createScaledQualityModel(rqm);
		Envelope env = new GeometryFactory().createEnvelope(new double[] { 0, 0, 0 }, new double[] { 1, 1, 1 }, null);
		building.setBbox(env);
		RenderablePrototype rp = new RenderablePrototype(building.getId(), building.getTime(), building.getBbox(), rqm);
		return new DataObjectInfo<RenderablePrototype>(building.getId(), Type.PROTOTYPE.getModelTypeName(),
				building.getName(), building.getExternalReference(), building.getBbox(), rp);
	}

	/**
	 * @param rqm
	 */
	private RenderableQualityModel createScaledQualityModel(RenderableQualityModel rqm) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;
		float maxZ = Float.MIN_VALUE;
		ArrayList<RenderableQualityModelPart> qualityModelParts = rqm.getQualityModelParts();
		for (RenderableQualityModelPart rqmp : qualityModelParts) {
			if (rqmp != null) {
				RenderableGeometry geom = (RenderableGeometry) rqmp;
				FloatBuffer fb = geom.getCoordBuffer();
				fb.rewind();
				int position = fb.position();
				while (position < fb.capacity()) {
					float x = fb.get();
					float y = fb.get();
					float z = fb.get();

					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					minZ = Math.min(minZ, z);

					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
					maxZ = Math.max(maxZ, z);
					position = fb.position();
				}
			}
		}

		double scaleX = 1d / (maxX - minX);
		double scaleY = 1d / (maxY - minY);
		double scaleZ = 1d / (maxZ - minZ);

		for (RenderableQualityModelPart rqmp : qualityModelParts) {
			if (rqmp != null) {
				RenderableGeometry geom = (RenderableGeometry) rqmp;
				FloatBuffer fb = geom.getCoordBuffer();
				fb.rewind();
				int i = 0;
				while ((i + 3) <= fb.capacity()) {
					float x = fb.get(i);
					x -= minX;
					x *= scaleX;
					fb.put(i, x);

					float y = fb.get(i + 1);
					y -= minY;
					y *= scaleY;
					fb.put(i + 1, y);

					float z = fb.get(i + 2);
					z -= minZ;
					z *= scaleZ;
					fb.put(i + 2, z);
					i += 3;
				}
			}
		}
		return rqm;

	}

}
