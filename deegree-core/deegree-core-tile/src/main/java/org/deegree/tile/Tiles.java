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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.tile;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

/**
 * Utility methods.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class Tiles {

	private final static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

	/**
	 * Calculates minx, miny, maxx and maxy indices for the given envelope in the given
	 * tile matrix.
	 * @param matrix
	 * @param envelope
	 * @return null, if envelopes do not intersect
	 */
	public static long[] getTileIndexRange(TileDataLevel matrix, Envelope envelope) {
		TileMatrix md = matrix.getMetadata();

		// calc tile indices
		Envelope menvelope = md.getSpatialMetadata().getEnvelope();
		if (!menvelope.intersects(envelope)) {
			return null;
		}
		double mminx = menvelope.getMin().get0();
		double mminy = menvelope.getMin().get1();
		double minx = envelope.getMin().get0();
		double miny = envelope.getMin().get1();
		double maxx = envelope.getMax().get0();
		double maxy = envelope.getMax().get1();

		long tileminx = (int) Math.floor((minx - mminx) / md.getTileWidth());
		long tileminy = (int) Math.floor((miny - mminy) / md.getTileHeight());
		long tilemaxx = (int) Math.floor((maxx - mminx) / md.getTileWidth());
		long tilemaxy = (int) Math.ceil((maxy - mminy) / md.getTileHeight());

		// sanitize values
		tileminx = Math.max(0, tileminx);
		tileminy = Math.max(0, tileminy);
		tilemaxx = Math.max(0, tilemaxx);
		tilemaxy = Math.max(0, tilemaxy);
		tileminx = Math.min(md.getNumTilesX() - 1, tileminx);
		tileminy = Math.min(md.getNumTilesY() - 1, tileminy);
		tilemaxx = Math.min(md.getNumTilesX() - 1, tilemaxx);
		tilemaxy = Math.min(md.getNumTilesY() - 1, tilemaxy);

		long h = tileminy;
		tileminy = md.getNumTilesY() - tilemaxy - 1;
		tilemaxy = md.getNumTilesY() - h - 1;

		return new long[] { tileminx, tileminy, tilemaxx, tilemaxy };
	}

	/**
	 * Returns the envelope for the specified tile indexes in the given matrix.
	 * @param matrix tile matrix, must not be <code>null</code>
	 * @param x tile column index (counting from zero)
	 * @param y tile row index (counting from zero)
	 * @return envelope, never <code>null</code>
	 */
	public static Envelope calcTileEnvelope(TileMatrix matrix, long x, long y) {
		double width = matrix.getTileWidth();
		double height = matrix.getTileHeight();
		Envelope env = matrix.getSpatialMetadata().getEnvelope();
		double minx = width * x + env.getMin().get0();
		double miny = env.getMax().get1() - height * y;
		return GEOMETRY_FACTORY.createEnvelope(minx, miny, minx + width, miny - height, env.getCoordinateSystem());
	}

}
