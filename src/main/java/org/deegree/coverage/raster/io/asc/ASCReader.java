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
package org.deegree.coverage.raster.io.asc;

import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * This class implements a simple reader for text raster files.
 * 
 * The text file should contain lines with whitespace-delimited z /value coordinates. The coordinates should be integer
 * or float values as ascii text.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ASCReader implements RasterReader {

    private static final Logger LOG = getLogger( ASCReader.class );

    private File file;

    private RasterGeoReference geoReference;

    private int height;

    private int width;

    private RasterDataInfo rasterDataInfo;

    private String dataLocationId;

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
        // ncols [number of columns]
        // nrows [number of rows]
        // xllcorner [x coordinate of lower left corner]
        // yllcorner [y coordinate of lower left corner]
        // cellsize [cell size in meters]
        // nodata_value [value which will be used if no data in grid cell; default is -9999]
        int line = 1;
        width = readInt( reader.readLine(), line++, "number of columns" );
        height = readInt( reader.readLine(), line++, "number of rows" );
        double xllCorner = readDouble( reader.readLine(), line++, "x coordinate of lower left corner" );
        double yllCorner = readDouble( reader.readLine(), line++, "y coordinate of lower left corner" );
        double resolution = readDouble( reader.readLine(), line++, "cell size in meters" );
        String nextLine = reader.readLine();
        String noData = "-9999";
        try {
            if ( nextLine.length() < 15 ) {
                Double.parseDouble( nextLine );
                // a valid value
                noData = nextLine;
                nextLine = null;
            }
        } catch ( NumberFormatException e ) {
            // no nodata value defined, assume -9999
        }
        if ( options.getNoDataValue() == null ) {
            byte[] createNoData = RasterIOOptions.createNoData( new String[] { noData }, DataType.FLOAT );
            options.setNoData( createNoData );
        }

        double outerCenterY = ( options.getRasterOriginLocation() == CENTER ? height - 1 : height ) * -resolution;

        geoReference = new RasterGeoReference( options.getRasterOriginLocation(), resolution, -resolution, xllCorner,
                                               yllCorner + outerCenterY );
        ByteBuffer buffer = ByteBuffer.allocate( width * height * DataType.FLOAT.getSize() );
        FloatBuffer fb = buffer.asFloatBuffer();

        StreamTokenizer st = new StreamTokenizer( reader );
        st.commentChar( '#' );
        st.parseNumbers();
        st.nextToken();
        st.eolIsSignificant( true );
        int type = st.ttype;
        while ( type != StreamTokenizer.TT_EOF ) {
            type = st.ttype;
            if ( type == StreamTokenizer.TT_NUMBER ) {
                fb.put( (float) st.nval );
            }
            // st.next token till end of line.
            while ( type != StreamTokenizer.TT_EOL && type != StreamTokenizer.TT_EOF ) {
                type = st.nextToken();
            }
        }

        RasterDataInfo rdi = new RasterDataInfo( new BandType[] { BandType.BAND_0 }, DataType.FLOAT,
                                                 InterleaveType.PIXEL );
        Envelope rasterEnvelope = this.geoReference.getEnvelope( width, height, options.getCRS() );
        RasterData data = RasterDataFactory.createRasterData( width, height, rdi, geoReference, buffer, true,
                                                              FileUtils.getFilename( this.file ), options );

        return new SimpleRaster( data, rasterEnvelope, geoReference );
    }

    /**
     * @param readLine
     * @param i
     * @param string
     * @return
     * @throws IOException
     */
    private double readDouble( String line, int lineNumber, String key )
                            throws IOException {
        try {
            return Double.parseDouble( line );
        } catch ( NumberFormatException e ) {
            throw new IOException( lineNumber + " expected a value denoting " + key + " but found " + line );
        }
    }

    private final int readInt( String line, int lineNumber, String key )
                            throws IOException {
        try {
            return Integer.parseInt( line );
        } catch ( NumberFormatException e ) {
            throw new IOException( lineNumber + " expected a value denoting " + key + " but found " + line );
        }
    }

    @Override
    public boolean canLoad( File filename ) {
        String ext = FileUtils.getFileExtension( filename );
        for ( String s : ASCRasterIOProvider.FORMATS ) {
            if ( s.equalsIgnoreCase( ext ) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedFormats() {
        return new HashSet<String>( ASCRasterIOProvider.FORMATS );
    }

    private void setID( RasterIOOptions options ) {
        this.dataLocationId = options != null ? options.get( RasterIOOptions.ORIGIN_OF_RASTER ) : null;
        if ( dataLocationId == null ) {
            if ( this.file != null ) {
                this.dataLocationId = FileUtils.getFilename( this.file );
            }
        }
    }

    @Override
    public AbstractRaster load( File filename, RasterIOOptions options )
                            throws IOException {
        this.file = filename;
        // try to read from cache.
        RasterCache cache = RasterCache.getInstance( options );
        setID( options );
        // File cacheFile = cache.createCacheFile( dataLocationId );
        SimpleRaster result = cache.createFromCache( null, dataLocationId );
        // the cachefiles are not backed with the xyz files.

        if ( result == null ) {
            // no cache file found or now instantiation of cache file possible.
            BufferedReader reader = new BufferedReader( new FileReader( filename ) );
            result = readASCIIGrid( reader, options );
            reader.close();
        } else {
            LOG.info( "Cache seems coherent using cachefile: {}.", cache.createCacheFile( dataLocationId ) );
        }
        return result;
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
        setID( options );
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

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return rasterDataInfo;
    }

    @Override
    public boolean canReadTiles() {
        return false;
    }

    @Override
    public String getDataLocationId() {
        return dataLocationId;
    }

    @Override
    public void dispose() {
        // nothing to do yet.
    }
}
