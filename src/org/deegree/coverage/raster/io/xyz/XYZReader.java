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
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.WorldFileAccess;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
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

    private static final Logger LOG = getLogger( XYZReader.class );

    private final static GeometryFactory factory = new GeometryFactory();

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

        GridPoint( float[] xyz ) {
            this.x = xyz[0];
            this.y = xyz[1];
            this.value = xyz[2];
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

    private File file;

    private RasterGeoReference geoReference;

    private int height;

    private int width;

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

        geoReference = options.getRasterGeoReference();

        List<GridPoint> gridPoints = new LinkedList<GridPoint>();

        String line = null;
        GridExtension gridExtension = new GridExtension();

        float prevX = 0;

        int i = 0;
        double resolution = 1;

        float[] xyzValues = new float[3];
        String separator = options.get( XYZRasterIOProvider.XYZ_SEPARATOR );
        if ( separator == null ) {
            // the regex for the given trim.
            separator = "\\s";
        }
        while ( ( line = reader.readLine() ) != null ) {
            try {
                // StringTokenizer tokenizer = new StringTokenizer( line );
                String[] xyz = line.split( separator );

                xyzValues[0] = Float.valueOf( xyz[0] );
                xyzValues[1] = Float.valueOf( xyz[1] );
                xyzValues[2] = Float.valueOf( xyz[2] );

                // float[] values = new float[] { Float.valueOf( x ), Float.valueOf( y ), Float.valueOf( z ), };

                GridPoint gridPoint = new GridPoint( xyzValues );
                // if ( !valuesProvided ) {
                // int ix = xVals.get( x ) == null ? 0 : xVals.get( x );
                // xVals.put( x, ++ix );
                // int iy = yVals.get( y ) == null ? 0 : yVals.get( y );
                // yVals.put( y, ++iy );
                if ( i == 0 ) {
                    prevX = gridPoint.x;
                    ++i;
                } else {
                    if ( i != -1 && prevX != gridPoint.x ) {
                        resolution = Math.abs( prevX - gridPoint.x );
                        i = -1;
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
        }

        if ( log.isDebugEnabled() ) {
            log.debug( String.format( "%f %f %f %f", gridExtension.maxx, gridExtension.maxy, gridExtension.minx,
                                      gridExtension.miny ) );
        }

        if ( geoReference == null ) {
            geoReference = new RasterGeoReference( RasterGeoReference.OriginLocation.CENTER, resolution, -resolution,
                                                   gridExtension.minx, gridExtension.maxy );
        }

        Envelope rasterEnvelope = factory.createEnvelope( gridExtension.minx, gridExtension.miny, gridExtension.maxx,
                                                          gridExtension.maxy, options.getCRS() );
        int[] size = geoReference.getSize( rasterEnvelope );
        width = size[0];
        height = size[1];
        RasterData data = RasterDataFactory.createRasterData( size[0], size[1], DataType.FLOAT );
        data.setNullPixel( options.getNoDataValue() );

        for ( GridPoint p : gridPoints ) {
            int[] pos = geoReference.getRasterCoordinate( p.x, p.y );
            data.setFloatSample( pos[0], pos[1], 0, p.value );
        }

        SimpleRaster simpleRaster = new SimpleRaster( data, rasterEnvelope, geoReference );

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
        this.file = filename;
        BufferedReader reader = new BufferedReader( new FileReader( filename ) );
        if ( options.readWorldFile() ) {
            try {
                RasterGeoReference geoRef = WorldFileAccess.readWorldFile( filename, options );
                options.setRasterGeoReference( geoRef );
            } catch ( IOException e ) {
                LOG.debug( "Could not read xyz world file: " + e.getLocalizedMessage(), e );
            }
        }
        return readASCIIGrid( reader, options );
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
        return readASCIIGrid( reader, options );
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public RasterGeoReference getGeoReference() {
        return geoReference;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer buffer )
                            throws IOException {
        // yes, do this
        return null;
    }

    @Override
    public boolean shouldCreateCacheFile() {
        return true;
    }

}
