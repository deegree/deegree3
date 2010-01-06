//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.coverage.raster.utils;

/**
 * <code>Rasters</code> supplies handy methods for rasters.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class Rasters {

    /**
     * Calculates the size of a single tile side.
     * 
     * @param rasterSide
     *            either the width or the height of the entire raster.
     * @param numberOfTiles
     *            of the entire raster.
     * @return the size (in pixels) of a single tile side.
     */
    public final static int calcTileSize( final float rasterSide, final int numberOfTiles ) {
        double size = rasterSide / numberOfTiles;
        return (int) Math.ceil( size );
    }

    /**
     * Calculates the number of tiles fitting the given preferred size and the largest of the given raster sides.
     * Determination is based on 1.5 times the given tile size, so if the largest entire raster side (for example the
     * width) is 1600 and the preferred tile size is 1000 this method will return 2, if however the largest side would
     * have been 1400, this method would return 1.
     * 
     * @param rasterWidth
     *            of the entire raster
     * @param rasterHeight
     *            of the entire raster
     * @param preferredTileSize
     *            in pixels of the largest side.
     * @return the number of tiles which will fit the preferred size. Determination is based on the largest given side.
     */
    public final static int calcApproxTiles( final int rasterWidth, final int rasterHeight, final int preferredTileSize ) {
        int largest = Math.max( rasterWidth, rasterHeight );
        // smaller then
        if ( largest < ( 0.5 * preferredTileSize ) + preferredTileSize ) {
            return 1;
        }
        if ( largest < 2 * preferredTileSize ) {
            return 2;
        }
        int result = 3;
        while ( largest > ( result * preferredTileSize ) ) {
            result++;
        }
        return result;
    }
}
