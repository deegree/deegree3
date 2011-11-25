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
 * <code>TileMatrixMetadata</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class TileMatrixMetadata {

    private SpatialMetadata spatialMetadata;

    private int width, height;

    private Pair<Integer, Integer> tileSize;

    private double resolution;

    private int numTilesX;

    private int numTilesY;

    public TileMatrixMetadata( SpatialMetadata spatialMetadata, int width, int height, Pair<Integer, Integer> tileSize,
                               double resolution, int numTilesX, int numTilesY ) {
        this.width = width;
        this.height = height;
        this.spatialMetadata = spatialMetadata;
        this.tileSize = tileSize;
        this.resolution = resolution;
        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;
    }

    public void setSpatialMetadata( SpatialMetadata spatialMetadata ) {
        this.spatialMetadata = spatialMetadata;
    }

    public SpatialMetadata getSpatialMetadata() {
        return spatialMetadata;
    }

    public void setWidth( int width ) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight( int height ) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setTileSize( Pair<Integer, Integer> tileSize ) {
        this.tileSize = tileSize;
    }

    public Pair<Integer, Integer> getTileSize() {
        return tileSize;
    }

    public void setResolution( double resolution ) {
        this.resolution = resolution;
    }

    public double getResolution() {
        return resolution;
    }

    public void setNumTilesX( int numTilesX ) {
        this.numTilesX = numTilesX;
    }

    public int getNumTilesX() {
        return numTilesX;
    }

    public void setNumTilesY( int numTilesY ) {
        this.numTilesY = numTilesY;
    }

    public int getNumTilesY() {
        return numTilesY;
    }

}
