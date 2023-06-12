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
package org.deegree.tools.coverage.gridifier.index;

import java.io.File;
import java.io.IOException;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.imageio.IIORasterReader;
import org.deegree.geometry.Envelope;

/**
 *
 * The <code>TileFile</code> class describes a TileFile in the filesystem rastertree.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class TileFile {

	private int id;

	private Envelope env;

	private String dir;

	private String fileName;

	private int level;

	private float xRes;

	private float yRes;

	private OriginLocation location;

	TileFile(int id, int level, Envelope env, String dir, String fileName, float xRes, float yRes,
			OriginLocation location) {
		this.id = id;
		this.env = env;
		this.level = level;
		this.dir = dir;
		this.fileName = fileName;
		this.xRes = xRes;
		this.yRes = yRes;
		this.location = location;
	}

	/**
	 * @return the envelope in world coordinates
	 */
	public Envelope getGeoEnvelope() {
		return env;
	}

	/**
	 * @return the raster reference of this tilefile
	 */
	public RasterGeoReference getEnvelope() {
		// rb: should the axis of the crs not been taken into account?
		return new RasterGeoReference(location, xRes, yRes, env.getMin().get0(), env.getMax().get1());
	}

	boolean intersects(
			Envelope env/* float minX2, float minY2, float maxX2, float maxY2 */) {
		// float minX1 = this.minX;
		// float minY1 = this.minY;
		// float maxX1 = this.maxX;
		// float maxY1 = this.maxY;
		//
		// // special case: this node is completely inside the region of the tilefile
		// if ( minX1 >= minX2 && maxX1 <= maxX2 && minY1 >= minY2 && maxY1 <= maxY2 ) {
		// return true;
		// }
		//
		// // left or right border of the tilefile lays inside the y-band of this node
		// if ( ( minX2 >= minX1 && minX2 <= maxX1 ) || ( maxX2 <= maxX1 && maxX2 >= minX1
		// ) ) {
		// if ( minY2 <= maxY1 && maxY2 >= minY1 ) {
		// return true;
		// }
		// }
		//
		// // top or bottom border of the tilefile lays inside the x-band of this node
		// if ( ( minY2 >= minY1 && minY2 <= maxY1 ) || ( maxY2 <= maxY1 && maxY2 >= minY1
		// ) ) {
		// if ( minX2 <= maxX1 && maxX2 >= minX1 ) {
		// return true;
		// }
		// }

		return this.env.intersects(env);
	}

	/**
	 * loads the raster
	 * @param tileBaseDir
	 * @return the loaded simpleraster
	 * @throws IOException
	 */
	public AbstractRaster loadRaster(String tileBaseDir) throws IOException {
		File file = new File(getFullFileName(tileBaseDir));
		RasterReader reader = new IIORasterReader();

		RasterIOOptions options = RasterIOOptions.forFile(file, getEnvelope());
		return reader.load(file, options);
	}

	/**
	 * @param tileBaseDir
	 * @return the file name this tile is referencing
	 */
	public String getFullFileName(String tileBaseDir) {
		return tileBaseDir + dir.substring(1) + "/" + fileName;
	}

	@Override
	public String toString() {
		return "{id=" + id + "," + env + ", dir=" + dir + ",file=" + fileName + ",xRes=" + xRes + ",yRes=" + yRes + "}";
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TileFile)) {
			return false;
		}
		TileFile that = (TileFile) o;
		return that.id == this.id;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return the directory
	 */
	public String getDir() {
		return dir;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
