//$HeadURL:svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/implementation/io/XYZTextReader.java $
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
package org.deegree.coverage.raster;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.geom.RasterEnvelope;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a simple reader for text raster files.
 * 
 * The text file should contain lines with whitespace-delimited x, y, z/value coordinates. The coordinates should be
 * integer or float values as ascii text.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author:otonnhofer $
 *
 * @version $Revision:10872 $, $Date:2008-04-01 15:41:48 +0200 (Tue, 01 Apr 2008) $
 */

// TODO: implement RasterReader interface
// for that: add width and height calculation or options
public class XYZTextReader {

    // saves a point in the raster grid (eg. each line becomes a GridPoint)
    private static class GridPoint {
        /**
         * x value
         */
        public float x;
        /**
         * y value
         */
        public float y;
        /**
         * the value
         */
        public float value;

        GridPoint( float x, float y, float value ) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        public String toString() {
            return String.format( "%.2f %.2f:\t%.2f", x, y, value );
        }
    }

    // saves the extension of the raster grid
    private static class GridExtension {
        /**
         * the min x value 
         */
        public float minx = Float.MAX_VALUE;
        
        /**
         * the min y value
         */
        public float miny = Float.MAX_VALUE;

        /**
         * the max x value
         */
        public float maxx = Float.MIN_VALUE;
        
        /**
         * the max y value
         */
        public float maxy = Float.MIN_VALUE;

        /**
         * Extend the current extension to contain point <code>p</code>.
         * @param p 
         */
        public void extend( GridPoint p ) {
            minx = min( minx, p.x );
            miny = min( miny, p.y );
            maxx = max( maxx, p.x );
            maxy = max( maxy, p.y );
        }
    }

    private static Logger log = LoggerFactory.getLogger( XYZTextReader.class );

    /**
     * Creates a SimpleRaster from a text file.
     * 
     * @param filename
     *            filename of the text raster
     * @param width
     *            width of the raster in pixel/points
     * @param height
     *            height of the raster in pixel/points
     * @param res
     *            resolution of the raster
     * @return new SimpleRaster with data from file
     * @throws IOException
     */
    public static SimpleRaster readASCIIGrid( String filename, int width, int height, double res )
                            throws IOException {

        // First read all points and gather the actual extension of the raster.
        // Then create a new raster and put each point into it. The order of the points
        // is arbitrary.

        GridPoint[] gridPoints = new GridPoint[width * height];

        File gridfile = new File( filename );
        BufferedReader reader = new BufferedReader( new FileReader( gridfile ) );
        String line;
        GridExtension gridExtension = new GridExtension();

        int i = 0;

        while ( ( line = reader.readLine() ) != null ) {
            try {
                StringTokenizer tokenizer = new StringTokenizer( line );

                float[] values = new float[] { Float.valueOf( tokenizer.nextToken() ),
                                              Float.valueOf( tokenizer.nextToken() ),
                                              Float.valueOf( tokenizer.nextToken() ), };

                GridPoint gridPoint = new GridPoint( values[0], values[1], values[2] );
                gridPoints[i] = gridPoint;
                gridExtension.extend( gridPoint );
            } catch ( NoSuchElementException e ) {
                if ( log.isWarnEnabled() ) {
                    log.warn( filename + " line " + ( i + 1 ) + " does not contain 3 values" );
                }
            } catch ( NumberFormatException e ) {
                if ( log.isWarnEnabled() ) {
                    log.warn( filename + " line " + ( i + 1 ) + " is invalid" );
                }
            }
            i += 1;
        }

        if ( log.isDebugEnabled() ) {
            log.debug( String.format( "%s: %f %f %f %f", filename, gridExtension.maxx, gridExtension.maxy,
                                      gridExtension.minx, gridExtension.miny ) );
        }

        RasterEnvelope renv = new RasterEnvelope( gridExtension.minx, gridExtension.maxy, res, -res );

        RasterData data = RasterDataFactory.createRasterData( width, height, DataType.FLOAT );

        for ( GridPoint p : gridPoints ) {
            int[] pos = renv.convertToRasterCRS( p.x, p.y );
            data.setFloatSample( pos[0], pos[1], 0, p.value );
        }

        Envelope env = renv.getEnvelope( width, height );

        SimpleRaster simpleRaster = new SimpleRaster( data, env, renv );

        return simpleRaster;
    }

}
