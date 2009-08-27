//$HeadURL:svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/implementation/io/XYZReader.java $
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
package org.deegree.coverage.raster.io.xyz;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a simple reader for text raster files.
 * 
 * The text file should contain lines with whitespace-delimited x y z/value coordinates. The coordinates should be
 * integer or float values as ascii text.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author:otonnhofer $
 * 
 * @version $Revision:10872 $, $Date:2008-04-01 15:41:48 +0200 (Tue, 01 Apr 2008) $
 */
public class XYZReader implements RasterReader {

    /**
     * 
     * The <code>Dimensions</code> class encapsulates the dimensions of this file.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    class Dimensions {
        int width = -1;

        int height = -1;

        double res = Double.NaN;

        boolean fromOptions( RasterIOOptions options ) {
            try {
                width = options.get( "WIDTH" ) == null ? -1 : Integer.parseInt( options.get( "WIDTH" ) );
                height = options.get( "HEIGHT" ) == null ? -1 : Integer.parseInt( options.get( "HEIGHT" ) );
                res = options.get( "RES" ) == null ? Double.NaN : Double.parseDouble( options.get( "RES" ) );
            } catch ( NumberFormatException e ) {
                //
            }
            return width != -1 && height != -1 && !Double.isNaN( res );
        }
    }

    // saves a point in the raster grid (eg. each line becomes a GridPoint)
    private class GridPoint {
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

        @Override
        public String toString() {
            return String.format( "%.2f %.2f:\t%.2f", x, y, value );
        }
    }

    // saves the extension of the raster grid
    static class GridExtension {
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
         * 
         * @param p
         */
        public void extend( GridPoint p ) {
            minx = min( minx, p.x );
            miny = min( miny, p.y );
            maxx = max( maxx, p.x );
            maxy = max( maxy, p.y );
        }
    }

    private static Logger log = LoggerFactory.getLogger( XYZReader.class );

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
    private SimpleRaster readASCIIGrid( BufferedReader reader, RasterIOOptions options )
                            throws IOException {
        Dimensions dims = new Dimensions();
        boolean valuesProvided = dims.fromOptions( options );

        // First read all points and gather the actual extension of the raster.
        // Then create a new raster and put each point into it. The order of the points
        // is arbitrary.

        List<GridPoint> gridPoints = new LinkedList<GridPoint>();

        String line;
        GridExtension gridExtension = new GridExtension();

        HashMap<String, Integer> xVals = new HashMap<String, Integer>();
        HashMap<String, Integer> yVals = new HashMap<String, Integer>();

        float prevX = 0;

        int i = 0;

        while ( ( line = reader.readLine() ) != null ) {
            try {
                StringTokenizer tokenizer = new StringTokenizer( line );

                String x = tokenizer.nextToken();
                String y = tokenizer.nextToken();
                String z = tokenizer.nextToken();

                float[] values = new float[] { Float.valueOf( x ), Float.valueOf( y ), Float.valueOf( z ), };

                GridPoint gridPoint = new GridPoint( values[0], values[1], values[2] );
                if ( !valuesProvided ) {
                    int ix = xVals.get( x ) == null ? 0 : xVals.get( x );
                    xVals.put( x, ++ix );
                    int iy = yVals.get( y ) == null ? 0 : yVals.get( y );
                    xVals.put( y, ++iy );
                    if ( i == 0 ) {
                        prevX = values[0];
                    }
                    if ( i == 1 ) {
                        dims.res = Math.abs( prevX - values[0] );
                    }

                }
                gridPoints.add( gridPoint );
                gridExtension.extend( gridPoint );
            } catch ( NoSuchElementException e ) {
                if ( log.isWarnEnabled() ) {
                    log.warn( "Line " + ( i + 1 ) + " does not contain 3 values" );
                }
            } catch ( NumberFormatException e ) {
                if ( log.isWarnEnabled() ) {
                    log.warn( "Line " + ( i + 1 ) + " is invalid" );
                }
            }
            i += 1;
        }

        if ( log.isDebugEnabled() ) {
            log.debug( String.format( "%f %f %f %f", gridExtension.maxx, gridExtension.maxy, gridExtension.minx,
                                      gridExtension.miny ) );
        }

        if ( !valuesProvided ) {
            Integer[] vals = xVals.values().toArray( new Integer[0] );
            if ( vals.length > 0 ) {
                Arrays.sort( vals );
                dims.width = vals[0];
            }
            vals = yVals.values().toArray( new Integer[0] );
            if ( vals.length > 0 ) {
                Arrays.sort( vals );
                dims.height = vals[0];
            }

        }

        RasterReference renv = new RasterReference( gridExtension.minx, gridExtension.maxy, dims.res, -dims.res );

        RasterData data = RasterDataFactory.createRasterData( dims.width, dims.height, DataType.FLOAT );

        for ( GridPoint p : gridPoints ) {
            int[] pos = renv.convertToRasterCRS( p.x, p.y );
            data.setFloatSample( pos[0], pos[1], 0, p.value );
        }

        Envelope env = renv.getEnvelope( dims.width, dims.height );

        SimpleRaster simpleRaster = new SimpleRaster( data, env, renv );

        return simpleRaster;
    }

    @Override
    public boolean canLoad( File filename ) {
        return "xyz".equalsIgnoreCase( FileUtils.getFileExtension( filename ) );
    }

    @Override
    public Set<String> getSupportedFormats() {
        return new HashSet<String>( XYZRasterIOProvider.FORMATS );
    }

    @Override
    public AbstractRaster load( File filename, RasterIOOptions options )
                            throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader( filename ) );
        return readASCIIGrid( reader, options );
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
        return readASCIIGrid( reader, options );
    }

}
