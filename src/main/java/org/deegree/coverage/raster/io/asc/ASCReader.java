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
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
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
        StreamTokenizer st = new StreamTokenizer( reader );
        st.commentChar( '#' );
        st.parseNumbers();
        st.nextToken();
        st.wordChars( '_', '_' );
        st.eolIsSignificant( true );
        st.lowerCaseMode( true );

        // ncols [number of columns]
        // nrows [number of rows]
        // xllcorner [x coordinate of lower left corner]
        // yllcorner [y coordinate of lower left corner]
        // cellsize [cell size in meters]
        // nodata_value [value which will be used if no data in grid cell; default is -9999]
        boolean widthFirst = testColsRows( st );
        if ( widthFirst ) {
            width = readInt( st, "ncols" );
            nextLine( st );
            height = readInt( st, "nrows" );
        } else {
            height = readInt( st, "nrows" );
            nextLine( st );
            width = readInt( st, "ncols" );
        }
        nextLine( st );
        OriginLocation origLoc = getOriginLocation( st );
        String loc = origLoc == OriginLocation.OUTER ? "corner" : "center";
        double origX = readDouble( st, "xll" + loc );
        nextLine( st );
        double origY = readDouble( st, "yll" + loc );
        nextLine( st );
        double resolution = readDouble( st, "cellsize" );
        nextLine( st );
        // no data will call st.nextToken if successful.
        double noData = readNoData( st, -9999 );
        if ( options.getNoDataValue() == null ) {
            byte[] createNoData = RasterIOOptions.createNoData( new String[] { Double.toString( noData ) },
                                                                DataType.FLOAT );
            options.setNoData( createNoData );
        }
        double outerCenterY = height * resolution;
        if ( origLoc != OriginLocation.OUTER && options.getRasterOriginLocation() == OriginLocation.OUTER ) {
            // rb: read center, but the options say outer, add half a resolution to the outer.
            outerCenterY += ( 0.5 * resolution );
            origLoc = OriginLocation.OUTER;
        }

        geoReference = new RasterGeoReference( origLoc, resolution, -resolution, origX, origY + outerCenterY );

        ByteBuffer buffer = ByteBuffer.allocate( width * height * DataType.FLOAT.getSize() );

        FloatBuffer fb = buffer.asFloatBuffer();

        int type = st.ttype;
        while ( type != StreamTokenizer.TT_EOF ) {
            if ( type == StreamTokenizer.TT_NUMBER ) {
                fb.put( (float) st.nval );
                type = st.nextToken();
            } else {
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
     * @param st
     * @return the originlocation of the grid file.
     * @throws IOException
     */
    private OriginLocation getOriginLocation( StreamTokenizer st )
                            throws IOException {
        String key = retrieveKey( st, "xllcorner", false );
        if ( key == null ) {
            key = retrieveKey( st, "xllcenter", false );
            if ( key == null ) {
                throw new IOException( st.lineno()
                                       + ") Could not determine the location of the origing of the grid/asc file." );
            }
        }
        OriginLocation result = "xllcorner".equalsIgnoreCase( key ) ? OriginLocation.OUTER : OriginLocation.CENTER;
        return result;
    }

    private void nextLine( StreamTokenizer tok )
                            throws IOException {
        int type = tok.ttype;
        while ( type != StreamTokenizer.TT_EOL ) {
            tok.nextToken();
            type = tok.ttype;
            if ( type == StreamTokenizer.TT_EOF ) {
                throw new IOException( tok.lineno() + "Unexpected end of file." );
            }
        }
        // read the next token after the eol.
        tok.nextToken();

    }

    /**
     * @param st
     * @return true if the columns are first defined
     * @throws IOException
     */
    private boolean testColsRows( StreamTokenizer st )
                            throws IOException {
        String key = retrieveKey( st, "nrows", false );
        if ( key == null ) {
            key = retrieveKey( st, "ncols", false );
            if ( key == null ) {
                throw new IOException( st.lineno() + ") Could not determine the rows and columns of the grid/asc file." );
            }
        }
        return "ncols".equalsIgnoreCase( key );
    }

    private double readNoData( StreamTokenizer tok, double defaultVal )
                            throws IOException {
        String keyVal = retrieveKey( tok, "nodata_value", false );
        if ( keyVal == null || !"nodata_value".equalsIgnoreCase( keyVal ) ) {
            tok.pushBack();
            return defaultVal;
        }

        int nextToken = tok.nextToken();
        if ( nextToken != StreamTokenizer.TT_NUMBER ) {
            throw new IOException( tok.lineno() + ") Could not determine 'nodata_value' from the asc/grd file." );
        }
        tok.nextToken();
        return tok.nval;
    }

    /**
     * @param readLine
     * @param i
     * @param string
     * @return
     * @throws IOException
     */
    private double readDouble( StreamTokenizer tok, String key )
                            throws IOException {
        String keyVal = retrieveKey( tok, key, true );
        if ( keyVal == null || !key.equalsIgnoreCase( keyVal ) ) {
            throw new IOException( tok.lineno() + ") Awaited key '" + key + "' but found: " + keyVal
                                   + "'. Aborting reading from the asc/grd file." );
        }

        int nextToken = tok.nextToken();
        if ( nextToken != StreamTokenizer.TT_NUMBER ) {
            throw new IOException( tok.lineno() + ") Could not determine '" + key + "' from the asc/grd file." );
        }
        return tok.nval;
    }

    private String retrieveKey( StreamTokenizer tok, String awaitedKey, boolean required )
                            throws IOException {
        String result = null;
        int nextToken = tok.ttype;
        if ( nextToken != StreamTokenizer.TT_WORD ) {
            if ( required ) {
                throw new IOException( tok.lineno() + ") Could not determine '" + awaitedKey
                                       + "' from the asc/grd file." );
            }
        } else {
            result = tok.sval;
        }
        return result;
    }

    private final int readInt( StreamTokenizer tok, String key )
                            throws IOException {

        String keyVal = retrieveKey( tok, key, true );
        if ( keyVal == null || !key.equalsIgnoreCase( keyVal ) ) {
            throw new IOException( tok.lineno() + ") Awaited key '" + key + "' but found: " + keyVal
                                   + "'. Aborting reading from the asc/grd file." );
        }

        int nextToken = tok.nextToken();
        if ( nextToken != StreamTokenizer.TT_NUMBER ) {
            throw new IOException( tok.lineno() + ") Could not determine '" + key + "' from the asc/grd file." );
        }
        int result = (int) tok.nval;
        return result;
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

    // public static void main( String[] args )
    // throws IOException {
    // ASCReader read = new ASCReader();
    // File f = new File( "/home/rutger/raster_test/utah/raster/dem/12STF200800.asc" );
    // RasterIOOptions options = RasterIOOptions.forFile( f );
    // AbstractRaster raster = read.load( f, options );
    // RasterFactory.saveRasterToFile( raster, new File( "/dev/shm/out.tiff" ) );
    // }
}
