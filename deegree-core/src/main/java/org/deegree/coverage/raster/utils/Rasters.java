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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.deegree.coverage.raster.geom.RasterRect;

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

    /**
     * Copies the data from the given source databuffer to the target databuffer.
     * 
     * @param srcRect
     *            the rectangle specifying the layout of the data in the source buffer.
     * @param destRect
     *            the rectangle specifying the layout of the data in the result buffer.
     * @param srcBuffer
     *            containing the data fitting the srcRect
     * @param destBuffer
     *            which will hold the result
     * @param sampleSize
     *            size in bytes of a sample (depends on the band size and the datatype)
     * @throws IOException
     */
    public static void copyValuesFromTile( RasterRect srcRect, RasterRect destRect, ByteBuffer srcBuffer,
                                           ByteBuffer destBuffer, int sampleSize )
                            throws IOException {
        RasterRect inter = RasterRect.intersection( srcRect, destRect );
        if ( inter != null ) {
            // rewind the buffer, to be on the right side with the limit.
            srcBuffer.clear();

            // the size of one line of the intersection.
            int lineSize = inter.width * sampleSize;

            // offset to the byte buffer.
            int dstOffsetY = inter.y - destRect.y;
            int dstOffsetX = inter.x - destRect.x;

            // offset in the tile channel
            int srcOffsetX = inter.x - srcRect.x;
            int srcOffsetY = inter.y - srcRect.y;

            // keep track of the number of rows in a tile.
            int currentIntersectRow = srcOffsetY;

            // position of the buffer.
            int dstPos = 0;
            // limit of the buffer.
            int srcLimit = 0;
            // the current file position.
            int srcPos = 0;
            // loop over the intersection rows and put them into the right place in the bytebuffer.
            // get the intersection inside the tile, then read row-wise into the buffer.
            for ( int row = dstOffsetY; row < ( dstOffsetY + inter.height ); ++row, ++currentIntersectRow ) {
                srcPos = ( ( srcOffsetX + ( currentIntersectRow * srcRect.width ) ) * sampleSize );
                srcLimit = srcPos + lineSize;
                srcBuffer.limit( srcLimit );
                srcBuffer.position( srcPos );
                dstPos = ( dstOffsetX + ( destRect.width * row ) ) * sampleSize;
                // then the position.
                destBuffer.position( dstPos );
                destBuffer.put( srcBuffer );
            }
        }

    }
}
