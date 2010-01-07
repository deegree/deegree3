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

package org.deegree.coverage.raster.cache;

import static java.lang.System.currentTimeMillis;
import static org.deegree.coverage.raster.cache.RasterCache.FILE_EXTENSION;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.grid.GridFileReader;
import org.deegree.coverage.raster.io.grid.GridMetaInfoFile;
import org.deegree.coverage.raster.io.grid.GridWriter;
import org.deegree.coverage.raster.utils.Rasters;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * The <code>CacheRasterFile</code> is a grid based caching mechanism for raster readers.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class CacheRasterReader extends GridFileReader {
    private static final Logger LOG = getLogger( CacheRasterReader.class );

    private int width;

    private int height;

    /** in raster coordinates */
    private int tileWidth;

    /** in raster coordinates */
    private int tileHeight;

    private RasterReader cachedReader;

    private GridWriter gridWriter;

    private static final int TILE_SIZE = 500;

    private final ByteBuffer[][] tiles;

    private final long[][] tilesOnFile;

    // last read time from original, or cache
    private final long[][] tilesInMemory;

    private RasterGeoReference geoRef;

    private long lastReadAccess;

    private RasterCache cacheManager;

    private CacheRasterReader( CacheInfo readValues, File cacheFile, RasterReader reader, RasterCache cacheManager ) {
        this.cachedReader = reader;
        this.width = readValues.rWidth;
        this.height = readValues.rHeight;
        GridMetaInfoFile gmif = readValues.gmif;
        this.tileHeight = gmif.getTileRasterHeight();
        this.tileWidth = gmif.getTileRasterWidth();
        // be sure the georeference is outer.
        this.geoRef = gmif.getGeoReference().createRelocatedReference( OriginLocation.OUTER );

        RasterDataInfo dInfo = gmif.getDataInfo();
        int columns = gmif.columns();
        int rows = gmif.rows();

        tiles = new ByteBuffer[rows][columns];
        tilesInMemory = new long[rows][columns];

        Envelope env = geoRef.getEnvelope( width, height, null );
        gmif.setEnvelope( env );

        this.cacheManager = cacheManager;
        if ( this.cacheManager == null ) {
            // get the default cache manager
            this.cacheManager = RasterCache.getInstance();
        }
        tilesOnFile = readValues.tilesInFile;

        try {
            this.gridWriter = new GridWriter( columns, rows, env, geoRef, cacheFile, dInfo );
        } catch ( IOException e ) {
            LOG.warn( "Could not create a cache file writer because: {}. Only in memory caching is enabled.",
                      e.getLocalizedMessage() );
        }
        super.instantiate( gmif, cacheFile );
        lastReadAccess = currentTimeMillis();
    }

    /**
     * @param width
     * @param height
     * @param cacheFile
     * @param dataInfo
     */
    private CacheRasterReader( int width, int height, File cacheFile, boolean shouldUseCachefile,
                               RasterDataInfo dataInfo, RasterGeoReference geoReference, RasterCache cacheManager ) {
        CacheInfo readValues = instantiateFromFile( cacheFile );
        GridMetaInfoFile gmif = null;
        this.width = width;
        this.height = height;
        int columns = 0;
        int rows = 0;
        RasterDataInfo dInfo = dataInfo;
        if ( readValues != null ) {
            this.width = readValues.rWidth;
            this.height = readValues.rHeight;
            gmif = readValues.gmif;
            this.tileHeight = gmif.getTileRasterHeight();
            this.tileWidth = gmif.getTileRasterWidth();
            this.geoRef = gmif.getGeoReference();
            dInfo = gmif.getDataInfo();
            columns = gmif.columns();
            rows = gmif.rows();
        } else {
            int numberOfTiles = Rasters.calcApproxTiles( width, height, TILE_SIZE );
            this.tileWidth = Rasters.calcTileSize( width, numberOfTiles );
            this.tileHeight = Rasters.calcTileSize( height, numberOfTiles );
            columns = (int) Math.ceil( ( (double) width ) / tileWidth );
            rows = (int) Math.ceil( (double) height / tileHeight );
            this.geoRef = geoReference.createRelocatedReference( OriginLocation.OUTER );
            gmif = new GridMetaInfoFile( geoRef, rows, columns, tileWidth, tileHeight, dataInfo );
        }
        tiles = new ByteBuffer[rows][columns];
        tilesInMemory = new long[rows][columns];

        Envelope env = geoRef.getEnvelope( OriginLocation.OUTER, width, height, null );
        this.cacheManager = cacheManager;
        if ( this.cacheManager == null ) {
            // get the default cache manager
            this.cacheManager = RasterCache.getInstance();
        }

        if ( shouldUseCachefile ) {
            tilesOnFile = ( readValues == null ) ? new long[rows][columns] : readValues.tilesInFile;
            if ( cacheFile == null ) {
                cacheFile = this.cacheManager.createCacheFile( createId( this.width, this.height, dInfo, geoRef ) );
                // create new cacheFile
            }
            try {
                this.gridWriter = new GridWriter( columns, rows, env, geoRef, cacheFile, dInfo );
            } catch ( IOException e ) {
                LOG.warn( "Could not create a cache file writer because: {}. Only in memory caching is enabled.",
                          e.getLocalizedMessage() );
            }

        } else {
            tilesOnFile = null;
        }

        super.instantiate( gmif, cacheFile );
        lastReadAccess = currentTimeMillis();
    }

    /**
     * Create a cached raster from the given bytebuffer.
     * 
     * @param filledBuffer
     *            with values.
     * @param width
     *            of the raster for which this buffer is valid.
     * @param height
     *            of the raster for which this buffer is valid.
     * @param cacheFile
     *            to use for storage
     * @param shouldCreateCachefile
     *            if writing to disk should be enabled.
     * @param dataInfo
     *            defining the data
     * @param geoReference
     *            of the raster
     * @param cache
     *            used.
     */
    public CacheRasterReader( ByteBuffer filledBuffer, int width, int height, File cacheFile,
                              boolean shouldCreateCachefile, RasterDataInfo dataInfo, RasterGeoReference geoReference,
                              RasterCache cache ) {
        this( width, height, cacheFile, shouldCreateCachefile, dataInfo, geoReference, cache );
        createTilesFromFilledBuffer( filledBuffer, width, height );
    }

    /**
     * @param cachedReader
     * @param cacheFile
     * @param cache
     * 
     */
    public CacheRasterReader( RasterReader cachedReader, File cacheFile, RasterCache cache ) {
        this( cachedReader.getWidth(), cachedReader.getHeight(), cacheFile, cachedReader.shouldCreateCacheFile(),
              cachedReader.getRasterDataInfo(), cachedReader.getGeoReference(), cache );
        this.cachedReader = cachedReader;
        // rb: instantiate on load may not be the wanted thing.

        // if ( this.cachedReader != null && !this.cachedReader.canReadTiles() ) {
        // BufferResult result = null;
        // try {
        // result = this.cachedReader.read( new RasterRect( 0, 0, this.cachedReader.getWidth(),
        // this.cachedReader.getHeight() ), null );
        // } catch ( IOException e ) {
        // LOG.warn( "Could not create tiles from the reader because; {}", e.getLocalizedMessage(), e );
        // // e.printStackTrace();
        // }
        // if ( result != null ) {
        // createTilesFromFilledBuffer( result.getResult(), result.getRect().width, result.getRect().height );
        // }
        // result = null;
        // }
    }

    /**
     * @param cacheFile
     * @throws IOException
     * @throws NumberFormatException
     */
    private static CacheInfo instantiateFromFile( File cacheFile ) {
        CacheInfo result = null;
        if ( cacheFile != null && cacheFile.exists() ) {
            String parent = cacheFile.getParent();
            File metaInfo = GridMetaInfoFile.fileNameFromOptions( parent, FileUtils.getFilename( cacheFile ), null );
            if ( !metaInfo.exists() ) {
                LOG.warn(
                          "Instantiation from file: {}, was unsuccessful, because no info file was present. Creating new cache file.",
                          cacheFile.getAbsolutePath() );

            }
            BufferedReader reader = null;
            try {
                reader = new BufferedReader( new FileReader( metaInfo ) );
                GridMetaInfoFile gmif = GridMetaInfoFile.read( reader, null );
                if ( gmif == null ) {
                    throw new NullPointerException( "no info file could be read" );
                }
                String r = reader.readLine();
                int width = 0;
                try {
                    width = Integer.decode( r );
                } catch ( NumberFormatException n ) {
                    throw new NullPointerException( "no width could be read" );
                }
                r = reader.readLine();
                int height = 0;
                try {
                    height = Integer.decode( r );
                } catch ( NumberFormatException n ) {
                    throw new NullPointerException( "no height could be read " );
                }
                // String[] tileInfos = new String[rows];
                long current = currentTimeMillis();
                long[][] tilesInFile = new long[gmif.rows()][gmif.columns()];
                int rows = gmif.rows();
                for ( int row = 0; row < rows; ++row ) {
                    String s = reader.readLine();
                    if ( s == null || s.length() != gmif.columns() ) {
                        throw new NullPointerException( "the number of rows|columns read was not correct" );
                    }
                    for ( int col = 0; col < s.length(); ++col ) {
                        tilesInFile[row][col] = ( s.charAt( col ) == '1' ) ? current : 0;
                    }

                }
                result = new CacheInfo( gmif, width, height, tilesInFile );
            } catch ( Exception e ) {
                LOG.warn( "Instantiation from file: {}, was unsuccessful, because {}. Creating new cache file.",
                          cacheFile.getAbsolutePath(), e.getLocalizedMessage() );
            } finally {
                if ( reader != null ) {
                    try {
                        reader.close();
                    } catch ( IOException e ) {
                        // what ever.
                    }
                }
            }

        }
        return result;
    }

    /**
     * Creates a CachedRasterReader from the given cacheFile.
     * 
     * @param reader
     *            which backs the cache
     * @param cacheFile
     *            to instantiate from
     * @param cache
     *            manager to use.
     * @return a {@link CacheRasterReader} or <code>null</code> if the cacheFile could not be read.
     */
    public static CacheRasterReader createFromCache( RasterReader reader, File cacheFile, RasterCache cache ) {
        CacheInfo readValues = instantiateFromFile( cacheFile );
        if ( readValues != null ) {
            return new CacheRasterReader( readValues, cacheFile, reader, cache );
        }
        return null;
    }

    @Override
    public AbstractRaster load( File gridFile, RasterIOOptions options )
                            throws IOException {
        lastReadAccess = currentTimeMillis();
        return super.load( gridFile, options );
    }

    @Override
    protected void read( int columnId, int rowId, ByteBuffer buffer )
                            throws IOException {
        lastReadAccess = currentTimeMillis();
        super.read( columnId, rowId, buffer );
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer resultBuffer )
                            throws IOException {
        BufferResult res = null;
        lastReadAccess = currentTimeMillis();
        if ( rect != null ) {
            // check if the reader has new data
            cacheFileUpToDate();

            // now get the data in memory
            RasterRect intersection = snapToGrid( rect );
            if ( intersection != null ) {
                int[] minCRmaxCR = getIntersectingTiles( intersection );
                if ( minCRmaxCR != null ) {
                    if ( resultBuffer == null ) {
                        resultBuffer = ByteBufferPool.allocate( intersection.height * intersection.width * sampleSize,
                                                                false, false );
                    }
                    for ( int row = minCRmaxCR[1]; row < tiles.length && row <= minCRmaxCR[3]; ++row ) {
                        for ( int col = minCRmaxCR[0]; col < tiles[row].length && col <= minCRmaxCR[2]; ++col ) {
                            leaveStreamOpen( true );
                            // getTileBuffer will get a read only (copy-of the tiles[row][col]) bytebuffer.
                            ByteBuffer tileBuffer = getTileBuffer( col, row );
                            copyValuesFromTile( col, row, intersection, tileBuffer, resultBuffer );
                        }
                    }
                    leaveStreamOpen( false );
                    res = new BufferResult( intersection, resultBuffer );
                }
            }
        }
        return res;
    }

    @Override
    public boolean canLoad( File filename ) {
        return FILE_EXTENSION.equalsIgnoreCase( FileUtils.getFileExtension( filename ) );
    }

    @Override
    public Set<String> getSupportedFormats() {
        HashSet<String> result = new HashSet<String>();
        result.add( FILE_EXTENSION );
        return result;
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        throw new UnsupportedOperationException( "Loading from a stream is currently not supported" );
    }

    @Override
    public boolean shouldCreateCacheFile() {
        return false;
    }

    @Override
    public RasterGeoReference getGeoReference() {
        return geoRef;
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
    public String getDataLocationId() {
        return ( cachedReader != null ) ? cachedReader.getDataLocationId() : super.getDataLocationId();
    }

    /**
     * Return the current time millis of the last read action. This method is needed for caching.
     * 
     * @return the {@link System#currentTimeMillis()} of the last read operation.
     */
    public long lastReadAccess() {
        return lastReadAccess;
    }

    /**
     * @return the current amount of memory this cached reader has on byte buffers.
     * 
     */
    public long currentApproxMemory() {
        long result = 0;
        synchronized ( tiles ) {
            if ( tiles != null ) {
                for ( int i = 0; i < tiles.length; ++i ) {
                    for ( int j = 0; j < tiles[i].length; ++j ) {
                        if ( tiles[i][j] != null ) {
                            result += tiles[i][j].capacity();
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void dispose() {
        this.dispose( cachedReader != null && !cachedReader.shouldCreateCacheFile() );
        if ( cachedReader != null ) {
            cachedReader.dispose();
        }
    }

    /**
     * Causes the cachefile to be written (if existing) and the memory buffers to be set to null.
     * 
     * @param memoryBuffersAsWell
     *            if true the memorybuffers (the ones which don't have rasterfiles to back them up) will be deleted as
     *            well.
     * 
     * @return the amount of freed memory.
     */
    public long dispose( boolean memoryBuffersAsWell ) {
        long result = 0;
        if ( gridWriter != null ) {
            result = writeCache( true, false );
        } else {
            if ( memoryBuffersAsWell ) {
                synchronized ( tiles ) {
                    // no writer, but still delete the buffers if requested.
                    for ( int row = 0; row < tiles.length; ++row ) {
                        for ( int column = 0; column < tiles[row].length; ++column ) {
                            if ( tiles[row][column] != null ) {
                                result += tiles[row][column].capacity();
                                tiles[row][column] = null;
                                tilesInMemory[row][column] = 0;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof CacheRasterReader ) {
            final CacheRasterReader that = (CacheRasterReader) other;
            return this.file().equals( that.file() );
        }
        return false;
    }

    /**
     * Writes all current in memory byte buffers to the cache file (if existing).
     */
    public void flush() {
        writeCache( false, false );
    }

    private String createId( int width, int height, RasterDataInfo rdi, RasterGeoReference geoRef ) {
        StringBuilder sb = new StringBuilder( geoRef.toString() );
        sb.append( "_bands_" ).append( rdi.bands );
        sb.append( "_datatype_" ).append( rdi.dataType );
        sb.append( "_w_" ).append( width );
        sb.append( "_h_" ).append( height );
        String result = sb.toString();
        result = result.replaceAll( "{", "_" );
        result = result.replaceAll( "}", "_" );
        result = result.replaceAll( ":", "_" );
        result = result.replaceAll( "\\s", "_" );
        return result;
    }

    /**
     * only called from constructor, no synchronization needed.
     * 
     * @param filledBuffer
     */
    private void createTilesFromFilledBuffer( ByteBuffer filledBuffer, int width, int height ) {
        if ( filledBuffer != null ) {
            if ( tiles.length == 1 && tiles[0].length == 1 ) {
                tiles[0][0] = filledBuffer.asReadOnlyBuffer();
                tilesInMemory[0][0] = currentTimeMillis();
            } else {
                // create tiling
                RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
                RasterRect dataRect = new RasterRect( 0, 0, width, height );
                ByteBuffer origBuffer = filledBuffer.asReadOnlyBuffer();
                for ( int row = 0; row < tiles.length; ++row ) {
                    for ( int col = 0; col < tiles[row].length; ++col ) {
                        tileRect.x = col * tileWidth;
                        tileRect.y = row * tileHeight;
                        tiles[row][col] = allocateTileBuffer( false, true );
                        try {
                            copyValuesFromTile( dataRect, tileRect, origBuffer, tiles[row][col] );
                            tilesInMemory[row][col] = currentTimeMillis();
                        } catch ( IOException e ) {
                            LOG.error( "Could not create tile from buffer because: " + e.getLocalizedMessage(), e );
                        }
                    }
                }
                // remove the old buffer from memory
                filledBuffer = null;
                origBuffer = null;
            }
        }
    }

    private void copyValuesFromTile( int tileColumn, int tileRow, RasterRect dstRect, ByteBuffer srcBuffer,
                                     ByteBuffer dstBuffer )
                            throws IOException {
        RasterRect tileRect = new RasterRect( this.tileWidth * tileColumn, tileHeight * tileRow, tileWidth, tileHeight );
        copyValuesFromTile( tileRect, dstRect, srcBuffer, dstBuffer );
    }

    /**
     * Copies the data from the given source databuffer to the target databuffer.
     * 
     * @param srcRect
     * @param destRect
     * @param srcBuffer
     * @param destBuffer
     * @throws IOException
     */
    private void copyValuesFromTile( RasterRect srcRect, RasterRect destRect, ByteBuffer srcBuffer,
                                     ByteBuffer destBuffer )
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

    /**
     * 
     * @return the last access time to the cached gridfile.
     */
    private long cacheFileUpToDate() {
        long result = 0;
        if ( gridWriter != null ) {
            if ( cachedReader != null && cachedReader.file() != null ) {
                result = cachedReader.file().lastModified();
                // mark the cache tile as invalid if they were read before the last modified.
                synchronized ( tiles ) {
                    for ( int row = 0; row < tilesOnFile.length; ++row ) {
                        for ( int column = 0; column < tilesOnFile[row].length; ++column ) {
                            if ( tilesOnFile[row][column] < result ) {
                                tilesOnFile[row][column] = 0;
                            }
                            if ( tilesInMemory[row][column] < result ) {
                                tilesInMemory[row][column] = 0;
                                tiles[row][column] = null;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get a tile buffer from the cache, it will be read only.
     * 
     * @param column
     * @param row
     * @return
     */
    private ByteBuffer getTileBuffer( int column, int row ) {
        ByteBuffer result = null;
        // allocation of the buffer should not be in the synchronized block, it may cause a dead lock with the raster
        // cache.
        ByteBuffer tileBuffer = allocateTileBuffer( false, true );
        if ( row < tiles.length && column < tiles[row].length ) {
            synchronized ( tiles ) {
                if ( tiles[row][column] == null || tilesInMemory[row][column] == 0 ) {
                    // check the cache file
                    if ( tilesOnFile != null && tilesOnFile[row][column] > 0 ) {
                        try {
                            super.getTileData( column, row, tileBuffer );
                            tilesInMemory[row][column] = currentTimeMillis();
                        } catch ( IOException e ) {
                            // could not read from the tile, so get rid of the tilesOnFile
                            tilesOnFile[row][column] = 0;
                        }

                    }
                    if ( tilesOnFile == null || tilesOnFile[row][column] == 0 ) {
                        readTileFromReader( column, row, tileBuffer );
                        // this can happen if the file could not be read from the cache file.
                    }
                    tiles[row][column] = tileBuffer;
                }
                result = tiles[row][column].asReadOnlyBuffer();
            }
        }
        return result;

    }

    /**
     * @param column
     * @param row
     * @param tileBuffer
     *            may be <code>null</code>
     */
    private ByteBuffer readTileFromReader( int column, int row, ByteBuffer tileBuffer ) {
        // this method is only called from within synchronized blocks on the tiles, there for only one thread will
        // access it at a time.
        ByteBuffer result = tileBuffer;
        if ( cachedReader != null ) {
            RasterRect tileRect = new RasterRect( column * tileWidth, row * tileHeight, tileWidth, tileHeight );
            try {
                BufferResult read = cachedReader.read( tileRect, result );
                if ( read == null ) {
                    return result;
                }
                RasterRect rect = read.getRect();
                if ( !rect.equals( tileRect ) ) {
                    // the tile rect was not filled correctly with data, make the data fit the grid
                    // layout.
                    ByteBuffer src = read.getResult();
                    if ( src == result ) {
                        // create a copy
                        LOG.debug( "The rectangle did not fit, creating copy." );
                        src = ByteBuffer.allocate( tileBuffer.capacity() );
                        tileBuffer.clear();
                        src.put( tileBuffer );
                    }
                    copyValuesFromTile( rect, tileRect, src, result );
                }
                // still synchronized on the tiles, so ok.
                tilesInMemory[row][column] = currentTimeMillis();
            } catch ( IOException e ) {
                LOG.error( "Unable to read data from the reader because: {}, creating emtpy tile.",
                           e.getLocalizedMessage() );
            }
        }
        return result;
    }

    /**
     * 
     */
    private long writeCache( boolean clearBuffer, boolean readMissingTiles ) {
        long freedUpMemory = 0;
        if ( gridWriter != null ) {
            synchronized ( tiles ) {
                gridWriter.leaveStreamOpen( true );
                // synchronizing on the tiles is valid, because of re-entrance capabilities of the Thread.
                cacheFileUpToDate();
                // update the cachefile.
                RasterRect tileRect = new RasterRect( 0, 0, tileWidth, tileHeight );
                // don't use the byte buffer pool, it could initiate an endless loop.
                ByteBuffer tmpBuffer = ByteBuffer.allocate( tileWidth * tileHeight * sampleSize );
                int rows = getTileRows();
                int columns = getTileColumns();
                boolean rewriteInfo = false;
                // write row first.
                for ( int row = 0; row < rows; ++row ) {
                    for ( int column = 0; column < columns; ++column ) {
                        boolean writeSuccessul = false;
                        if ( tilesOnFile[row][column] == 0 ) {
                            try {
                                LOG.debug( "{}->{},{}) Writing: {},{}", new Object[] { file(), rows, columns, row,
                                                                                      column } );
                                // tile is not valid on file
                                if ( tiles[row][column] != null ) {
                                    // will be null if the tile was not valid.
                                    tiles[row][column].clear();
                                    writeSuccessul = gridWriter.writeTile( column, row,
                                                                           tiles[row][column].asReadOnlyBuffer() );
                                    if ( clearBuffer ) {
                                        freedUpMemory += tiles[row][column].capacity();
                                        tiles[row][column] = null;
                                        tilesInMemory[row][column] = 0;
                                    }
                                } else {
                                    if ( cachedReader != null && readMissingTiles ) {
                                        tmpBuffer.clear();
                                        readTileFromReader( column, row, tmpBuffer );
                                        tmpBuffer.clear();
                                        gridWriter.writeTile( column, row, tmpBuffer );
                                        writeSuccessul = true;
                                    }
                                }

                            } catch ( IOException e ) {
                                LOG.error( "Writing of tile {}{} failed, Reason: {}.",
                                           new Object[] { row, column, e.getLocalizedMessage(), e } );
                                writeSuccessul = false;
                            }

                        } else {
                            if ( clearBuffer ) {
                                // the tile is on file, check if in memory
                                if ( tiles[row][column] != null ) {
                                    // will be null if the tile was not valid.
                                    tiles[row][column].clear();
                                    if ( clearBuffer ) {
                                        freedUpMemory += tiles[row][column].capacity();
                                        tiles[row][column] = null;
                                        tilesInMemory[row][column] = 0;
                                    }
                                }
                            }
                        }
                        if ( writeSuccessul ) {
                            tilesOnFile[row][column] = currentTimeMillis();
                            rewriteInfo = true;
                        }
                        // next tile.
                        tileRect.x += tileWidth;
                    }
                    tileRect.x = 0;
                    tileRect.y += tileHeight;
                }
                if ( rewriteInfo ) {
                    // write cache information..
                    try {
                        writeCacheInfo();
                    } catch ( IOException e ) {
                        LOG.error( "Writing of info file failed, this will make the cachefile invalid. Reason: {}.",
                                   e.getLocalizedMessage(), e );
                    }
                }
                tmpBuffer.clear();
                tmpBuffer = null;

                gridWriter.leaveStreamOpen( false );
            }
        }
        return freedUpMemory;
    }

    /**
     * @throws IOException
     * 
     */
    private void writeCacheInfo()
                            throws IOException {
        if ( gridWriter != null && file() != null ) {
            File metaInfo = GridMetaInfoFile.fileNameFromOptions( file().getParent(), FileUtils.getFilename( file() ),
                                                                  null );
            if ( !metaInfo.exists() ) {
                metaInfo.createNewFile();
            }
            PrintWriter writer = new PrintWriter( new FileWriter( metaInfo ) );
            GridMetaInfoFile.write( writer, infoFile, null );
            // original data size.
            writer.println( width );
            writer.println( height );

            for ( int row = 0; row < tilesOnFile.length; ++row ) {
                StringBuilder sb = new StringBuilder();
                for ( int col = 0; col < tilesOnFile[row].length; ++col ) {
                    sb.append( tilesOnFile[row][col] == 0 ? 0 : 1 );
                }
                writer.println( sb.toString() );
            }
            writer.flush();
            writer.close();
        }
    }

    private static class CacheInfo {
        /**
         * @param gmif
         * @param width
         * @param height
         * @param tilesInFile
         */
        public CacheInfo( GridMetaInfoFile gmif, int width, int height, long[][] tilesInFile ) {
            this.gmif = gmif;
            this.rWidth = width;
            this.rHeight = height;
            this.tilesInFile = tilesInFile;
        }

        int rHeight = 0;

        int rWidth = 0;

        GridMetaInfoFile gmif;

        long[][] tilesInFile;
    }

}
