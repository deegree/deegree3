//$HeadURL$
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

import org.deegree.commons.utils.Pair;
import org.deegree.geometry.metadata.SpatialMetadata;

/**
 * The <code>TileMatrixMetadata</code> describes the properties of a tile matrix. It can be used to determine which
 * tiles to request from the matrix.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class TileMatrixMetadata {

    private SpatialMetadata spatialMetadata;

    private int numTilesX, numTilesY;

    private Pair<Integer, Integer> tileSize;

    private double resolution, tileWidth, tileHeight;

    /**
     * All fields must be set. The width/height of the tiles in world coordinates is calculated automatically.
     * 
     * @param spatialMetadata
     *            the envelope and coordinate system, never null
     * @param tileSize
     *            the pixel width/height of a tile
     * @param resolution
     *            the resolution of a pixel in world coordinates
     * @param numTilesX
     *            the number of tiles in x direction
     * @param numTilesY
     *            the number of tiles in y direction
     */
    public TileMatrixMetadata( SpatialMetadata spatialMetadata, Pair<Integer, Integer> tileSize, double resolution,
                               int numTilesX, int numTilesY ) {
        this.spatialMetadata = spatialMetadata;
        this.tileSize = tileSize;
        this.resolution = resolution;
        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;
        this.tileWidth = tileSize.first * resolution;
        this.tileHeight = tileSize.second * resolution;
    }

    /**
     * @return the envelope and crs, never null
     */
    public SpatialMetadata getSpatialMetadata() {
        return spatialMetadata;
    }

    /**
     * @return the width/height of a tile
     */
    public Pair<Integer, Integer> getTileSize() {
        return tileSize;
    }

    /**
     * @return the resolution of a pixel in world coordinates
     */
    public double getResolution() {
        return resolution;
    }

    /**
     * @return the number of tiles in x direction
     */
    public int getNumTilesX() {
        return numTilesX;
    }

    /**
     * @return the number of tiles in y direction
     */
    public int getNumTilesY() {
        return numTilesY;
    }

    /**
     * @return the width of a tile in world coordinates (outer edges)
     */
    public double getTileWidth() {
        return tileWidth;
    }

    /**
     * @return the height of a tile in world coordinates (outer edges)
     */
    public double getTileHeight() {
        return tileHeight;
    }

}
