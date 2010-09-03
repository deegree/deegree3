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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.slf4j.Logger;

/**
 * The <code>CacheRasterReader</code> is a grid based caching mechanism for raster readers.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class CacheRasterReader extends GridFileReader {
    private static final Logger LOG = getLogger( CacheRasterReader.class );

    private static final int TILE_SIZE = 500;

    private final Object LOCK = new Object();

    private final Map<Integer, TileEntry> tiles;

    private RasterReader cachedReader;

    private GridWriter gridWriter;

    private long lastReadAccess;

    private long inMemorySize;

    private RasterCache cacheManager;

    /**
     * Instantiate the map
     * 
     * @return a new map
     */
    private Map<Integer, TileEntry> instantiateTiles() {
        synchronized ( LOCK ) {
            Map<Integer, TileEntry> result = new ConcurrentHashMap<Integer, TileEntry>( getTileColumns()
                                                                                        * getTileRows() * 2 );
            CacheInfoFile infoFile = (CacheInfoFile) this.infoFile;
            boolean[][] tilesOnFile = infoFile.getTilesOnFile();
            if ( tilesOnFile == null || tilesOnFile.length != getTileRows()
                 || tilesOnFile[getTileRows() - 1].length != getTileColumns() ) {
                tilesOnFile = new boolean[getTileRows()][getTileColumns()];
            }
            for ( int r = 0; r < getTileRows(); ++r ) {
                for ( int c = 0; c < getTileColumns(); ++c ) {
                    RasterRect rect = new RasterRect( c * getTileRasterWidth(), r * getTileRasterHeight(),
                                                      getTileRasterWidth(), getTileRasterHeight() );
                    // System.out.println( rect );
                    int key = getTileId( c, r );
                    // System.out.println( "Key (" + c + "," + r + "): " + key );
                    TileEntry entry = new TileEntry( rect );
                    entry.setTileOnFile( tilesOnFile[r][c] );
                    result.put( key, entry );
                }
            }
            return result;
        }
    }

    private CacheRasterReader( CacheInfoFile readValues, File cacheFile, RasterReader reader, RasterCache cacheManager ) {
        this.cachedReader = reader;
        // GridMetaInfoFile gmif = readValues.gmif;
        this.cacheManager = cacheManager;
        if ( this.cacheManager == null ) {
            // get the default cache manager
            this.cacheManager = RasterCache.getInstance();
        }
        super.instantiate( readValues, cacheFile );
        try {
            this.gridWriter = new GridWriter( getTileColumns(), getTileRows(), getEnvelope(), getGeoReference(),
                                              cacheFile, getRasterDataInfo() );
        } catch ( IOException e ) {
            LOG.warn( "Could not create a cache file writer because: {}. Only in memory caching is enabled.",
                      e.getLocalizedMessage() );
        }
        tiles = instantiateTiles();
    }

    /**
     * @param rasterWidth
     * @param rasterHeight
     * @param cacheFile
     * @param dataInfo
     */
    private CacheRasterReader( int rasterWidth, int rasterHeight, File cacheFile, boolean shouldUseCachefile,
                               RasterDataInfo dataInfo, RasterGeoReference geoReference, RasterCache cacheManager ) {
        CacheInfoFile readValues = CacheInfoFile.read( cacheFile );
        // GridMetaInfoFile gmif = null;
        if ( readValues == null ) {
            int numberOfTiles = Rasters.calcApproxTiles( rasterWidth, rasterHeight, TILE_SIZE );
            int tileWidth = Rasters.calcTileSize( rasterWidth, numberOfTiles );
            int tileHeight = Rasters.calcTileSize( rasterHeight, numberOfTiles );
            int columns = (int) Math.ceil( ( (double) rasterWidth ) / tileWidth );
            int rows = (int) Math.ceil( (double) rasterHeight / tileHeight );
            RasterGeoReference geoRef = geoReference.createRelocatedReference( OriginLocation.OUTER );
            readValues = new CacheInfoFile( geoRef, rows, columns, tileWidth, tileHeight, dataInfo, rasterWidth,
                                            rasterHeight, null, 0 );
        }
        this.cacheManager = cacheManager;
        if ( this.cacheManager == null ) {
            // get the default cache manager
            this.cacheManager = RasterCache.getInstance();
        }

        if ( shouldUseCachefile ) {
            if ( cacheFile == null ) {
                cacheFile = this.cacheManager.createCacheFile( createId(
                                                                         readValues.getRasterWidth(),
                                                                         readValues.getRasterHeight(),
                                                                         readValues.getDataInfo(),
                                                                         readValues.getGeoReference().createRelocatedReference(
                                                                                                                                OriginLocation.OUTER ) ) );
            }
        }
        super.instantiate( readValues, cacheFile );
        if ( shouldUseCachefile ) {
            try {
                LOG.debug( "Writing to file: " + cacheFile.getAbsolutePath() );
                this.gridWriter = new GridWriter( getTileColumns(), getTileRows(), getEnvelope(), getGeoReference(),
                                                  cacheFile, getRasterDataInfo() );
            } catch ( IOException e ) {
                LOG.warn( "Could not create a cache file writer because: {}. Only in memory caching is enabled.",
                          e.getLocalizedMessage() );
            }
        }
        tiles = instantiateTiles();
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
     * only called from constructor, no synchronization needed.
     * 
     * @param filledBuffer
     */
    private void createTilesFromFilledBuffer( ByteBuffer filledBuffer, int width, int height ) {
        if ( filledBuffer != null ) {
            // create tiling
            RasterRect dataRect = new RasterRect( 0, 0, width, height );
            ByteBuffer origBuffer = filledBuffer.asReadOnlyBuffer();
            for ( int row = 0; row < getTileRows(); ++row ) {
                for ( int col = 0; col < getTileColumns(); ++col ) {
                    TileEntry entry = getEntry( col, row );
                    if ( entry != null ) {
                        ByteBuffer entryBuffer = allocateTileBuffer( false, true );
                        try {
                            Rasters.copyValuesFromTile( dataRect, entry.getRasterRect(), origBuffer, entryBuffer,
                                                        sampleSize );
                        } catch ( IOException e ) {
                            LOG.error( "Could not create tile from buffer because: " + e.getLocalizedMessage(), e );
                        }
                        this.inMemorySize += entry.setBuffer( entryBuffer );
                    }
                }
            }
            // remove the old buffer from memory
            filledBuffer = null;
            origBuffer = null;
        }

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
        CacheInfoFile readValues = CacheInfoFile.read( cacheFile );
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
        if ( rect != null ) {
            // check if the reader has new data
            if ( !cacheFileUpToDate() ) {
                clear( true );
            }
            lastReadAccess = currentTimeMillis();

            // now get the data in memory
            RasterRect intersection = snapToGrid( rect );
            if ( intersection != null ) {
                int[] minCRmaxCR = getIntersectingTiles( intersection );
                if ( minCRmaxCR != null ) {
                    if ( resultBuffer == null ) {
                        resultBuffer = ByteBufferPool.allocate( intersection.height * intersection.width * sampleSize,
                                                                false, false );
                    }
                    for ( int row = minCRmaxCR[1]; row < getTileRows() && row <= minCRmaxCR[3]; ++row ) {
                        for ( int col = minCRmaxCR[0]; col < getTileColumns() && col <= minCRmaxCR[2]; ++col ) {
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

    /**
     * Clears all memory buffers and deletes the cache file if requested. Note this method will not write any data to
     * the cachefile.
     * 
     * @param deleteCacheFile
     *            true if the cache file should be deleted as well.
     * @return the number of memory bytes freed up after cleaning.
     */
    public long clear( boolean deleteCacheFile ) {
        long result = 0;
        synchronized ( LOCK ) {
            for ( TileEntry entry : tiles.values() ) {
                if ( entry != null ) {
                    long r = entry.clear( deleteCacheFile );
                    inMemorySize -= r;
                    result += r;
                }
            }
            if ( deleteCacheFile ) {
                deleteCacheFile();
            }
        }
        return result;
    }

    /**
     * Delete the file this cache reader is using.
     * 
     * @return true if this cached reader no longer references an existing file.
     */
    public boolean deleteCacheFile() {
        boolean result = true;
        synchronized ( LOCK ) {
            super.dispose();
            File f = file();
            if ( f != null ) {
                if ( f.exists() && f.isFile() ) {
                    result = f.delete();
                    File metaInfo = GridMetaInfoFile.fileNameFromOptions( f.getParent(), FileUtils.getFilename( f ),
                                                                          null );
                    if ( metaInfo.exists() ) {
                        boolean mR = metaInfo.delete();
                        if ( !mR ) {
                            LOG.warn( "Could not delete meta info file for raster cache file: " + f.getAbsolutePath() );
                        }
                    }
                }
            }
        }
        return result;
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
        return super.getGeoReference().createRelocatedReference( OriginLocation.OUTER );
    }

    @Override
    public int getHeight() {
        return ( (CacheInfoFile) this.infoFile ).getRasterHeight();
    }

    @Override
    public int getWidth() {
        return ( (CacheInfoFile) this.infoFile ).getRasterWidth();
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
        return inMemorySize;
    }

    /**
     * @return the size of the cache file on disk.
     */
    public long cacheFileSize() {
        return ( file() != null && file().exists() ) ? file().length() : 0;
    }

    @Override
    public void dispose() {
        this.dispose( cachedReader != null && !cachedReader.shouldCreateCacheFile() );
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
        if ( cachedReader != null ) {
            // close the file in the reader as well.
            cachedReader.dispose();
            super.dispose();
        }
        long result = 0;
        if ( gridWriter != null ) {
            result = writeCache( true );
        } else {
            if ( memoryBuffersAsWell ) {
                clear( false );
            }
        }
        return result;
    }

    /**
     * Writes all current in memory byte buffers to the cache file (if existing).
     */
    public void flush() {
        writeCache( false );
    }

    /**
     * @return true if this reader can create a cachefile for it's in memory buffers.
     */
    public boolean canCreateCacheFile() {
        return gridWriter != null;
    }

    private String createId( int width, int height, RasterDataInfo rdi, RasterGeoReference geoRef ) {
        StringBuilder sb = new StringBuilder( geoRef.toString() );
        sb.append( "_bands_" ).append( rdi.bands );
        sb.append( "_datatype_" ).append( rdi.dataType );
        sb.append( "_w_" ).append( width );
        sb.append( "_h_" ).append( height );
        String result = sb.toString();
        result = result.replaceAll( "\\{", "_" );
        result = result.replaceAll( "\\}", "_" );
        result = result.replaceAll( "\\:", "_" );
        result = result.replaceAll( "\\s", "_" );
        return result;
    }

    private void copyValuesFromTile( int tileColumn, int tileRow, RasterRect dstRect, ByteBuffer srcBuffer,
                                     ByteBuffer dstBuffer )
                            throws IOException {
        int tileWidth = getTileRasterWidth();
        int tileHeight = getTileRasterHeight();
        RasterRect tileRect = new RasterRect( tileWidth * tileColumn, tileHeight * tileRow, tileWidth, tileHeight );
        Rasters.copyValuesFromTile( tileRect, dstRect, srcBuffer, dstBuffer, sampleSize );
    }

    /**
     * 
     * @return true if the last access time later then the last modification to the backed file.
     */
    private boolean cacheFileUpToDate() {
        if ( cachedReader != null && cachedReader.file() != null ) {
            return lastReadAccess > cachedReader.file().lastModified();
        }
        return true;
    }

    /**
     * Get a tile buffer from the cache, it will be read only.
     * 
     * @param column
     * @param row
     */
    private ByteBuffer getTileBuffer( int column, int row ) {
        ByteBuffer result = null;
        TileEntry entry = getEntry( column, row );
        if ( entry != null ) {

            ByteBuffer entryBuffer = null;
            synchronized ( LOCK ) {
                entryBuffer = entry.getBuffer();
            }
            if ( entryBuffer == null ) {
                // allocation of the buffer should not be in the synchronized block, it may cause a dead lock with the
                // raster cache.
                entryBuffer = allocateTileBuffer( false, true );
            }

            synchronized ( LOCK ) {
                if ( !entry.isInMemory() ) {
                    // check the cache file
                    if ( entry.isOnFile() ) {
                        try {
                            entryBuffer = super.getTileData( column, row, entryBuffer );
                        } catch ( IOException e ) {
                            // could not read from the tile, so get rid of the tilesOnFile
                            entry.setTileOnFile( false );
                        }
                    }
                    if ( !entry.isOnFile() ) {
                        // this can happen if the file could not be read from the cache file because of an IOException.
                        readTileFromReader( entry, entryBuffer );
                    }
                    this.inMemorySize += entry.setBuffer( entryBuffer );
                }
            }
            result = entryBuffer.asReadOnlyBuffer();
        }
        return result;
    }

    /**
     * @param column
     * @param row
     * @param tileBuffer
     *            may be <code>null</code>
     */
    private ByteBuffer readTileFromReader( TileEntry entry, ByteBuffer tileBuffer ) {
        // this method is only called from within synchronized blocks on the tiles, there for only one thread will
        // access it at a time.
        ByteBuffer result = tileBuffer;
        if ( cachedReader != null ) {
            RasterRect tileRect = new RasterRect( entry.getRasterRect() );
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
                        // if the result is the same instance, create a copy
                        LOG.debug( "The rectangle did not fit, creating copy." );
                        src = ByteBuffer.allocate( tileBuffer.capacity() );
                        tileBuffer.clear();
                        src.put( tileBuffer );
                    }
                    Rasters.copyValuesFromTile( rect, tileRect, src, result, sampleSize );
                }
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
    private long writeCache( boolean clearBuffer ) {
        long freedUpMemory = 0;
        if ( gridWriter != null ) {
            synchronized ( LOCK ) {
                if ( !cacheFileUpToDate() ) {
                    // the original file is newer, don't save anything and remove the memory buffers as well.
                    freedUpMemory = this.inMemorySize;
                    clear( true );
                    if ( this.inMemorySize != 0 ) {
                        LOG.warn( "After clearing the cache entry some allocated memory remains, this may not be!" );
                    }
                } else {
                    gridWriter.leaveStreamOpen( true );
                    boolean rewriteInfo = false;
                    for ( int row = 0; row < getTileRows(); ++row ) {
                        for ( int column = 0; column < getTileColumns(); ++column ) {
                            TileEntry entry = getEntry( column, row );
                            if ( entry != null ) {
                                if ( !entry.isOnFile() ) {
                                    if ( entry.isInMemory() ) {
                                        try {
                                            boolean onFile = gridWriter.writeTile( column, row, entry.getBuffer() );
                                            entry.setTileOnFile( onFile );
                                            if ( !rewriteInfo ) {
                                                rewriteInfo = onFile;
                                            }
                                        } catch ( IOException e ) {
                                            if ( LOG.isDebugEnabled() ) {
                                                LOG.debug(
                                                           "(Stack) Exception occurred while writing tile to cache file: "
                                                                                   + e.getLocalizedMessage(), e );
                                            } else {
                                                LOG.error( "Exception occurred while writing tile to cache file: "
                                                           + e.getLocalizedMessage() );
                                            }
                                        }
                                    }
                                }
                                if ( clearBuffer ) {
                                    if ( entry.isOnFile() && entry.isInMemory() ) {
                                        long mem = entry.clear( false );
                                        this.inMemorySize -= mem;
                                        freedUpMemory += mem;
                                    }
                                }
                            }
                        }
                        if ( rewriteInfo ) {
                            if ( !writeCacheInfo() ) {
                                if ( !deleteCacheFile() ) {
                                    LOG.debug( "Could not delete grid file." );
                                }
                            }
                        }
                        gridWriter.leaveStreamOpen( false );
                    }
                }
            }
        }
        return freedUpMemory;
    }

    private boolean writeCacheInfo() {
        boolean result = false;
        if ( gridWriter != null && file() != null ) {
            File metaInfo = GridMetaInfoFile.fileNameFromOptions( file().getParent(), FileUtils.getFilename( file() ),
                                                                  null );
            boolean[][] tilesOnFiles = new boolean[getTileRows()][getTileColumns()];
            for ( int row = 0; row < getTileRows(); ++row ) {
                for ( int col = 0; col < getTileColumns(); ++col ) {
                    TileEntry entry = getEntry( col, row );
                    tilesOnFiles[row][col] = ( entry != null && entry.isOnFile() );
                }
            }
            CacheInfoFile info = new CacheInfoFile( getGeoReference(), getTileRows(), getTileColumns(),
                                                    getTileRasterWidth(), getTileRasterHeight(), getRasterDataInfo(),
                                                    super.getWidth(), super.getHeight(), tilesOnFiles,
                                                    file().lastModified() );
            try {
                CacheInfoFile.write( metaInfo, info );
                result = true;
            } catch ( IOException e ) {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Writing of info file failed, this will make the cachefile invalid. Reason: {}.",
                               e.getLocalizedMessage(), e );
                } else {
                    LOG.debug( "Writing of info file failed, this will make the cachefile invalid. Reason: {}.",
                               e.getLocalizedMessage() );
                }

            }
        }
        return result;
    }

    /**
     * Get the entry for the given column and row.
     * 
     * @param column
     * @param row
     * @return the tile for the given column and row, maybe <code>null</code>
     */
    private TileEntry getEntry( int column, int row ) {
        Integer key = getTileId( column, row );
        return tiles.get( key );
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
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long result = 32452843;
        result = result * 37 + this.file().hashCode();
        return (int) ( result >>> 32 ) ^ (int) result;
    }

}
