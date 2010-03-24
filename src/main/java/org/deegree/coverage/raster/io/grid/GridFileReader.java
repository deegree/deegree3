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

package org.deegree.coverage.raster.io.grid;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.cache.ByteBufferPool;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.slf4j.Logger;

/**
 * The <code>GridFileReader</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GridFileReader extends GridReader {

    private static final Logger LOG = getLogger( GridFileReader.class );

    private FileInputStream fileAccess;

    private File gridFile;

    private boolean leaveOpen = false;

    private String dataLocationId;

    /**
     * An empty constructor used in the {@link GridRasterIOProvider}, to a location in time where no information is
     * known yet.
     */
    public GridFileReader() {
        // empty constructor, no values are known yet (GridRasterIOProvider).
    }

    /**
     * @param infoFile
     * @param gridFile
     */
    public GridFileReader( GridMetaInfoFile infoFile, File gridFile ) {
        instantiate( infoFile, gridFile );
        // TODO Auto-generated constructor stub
    }

    /**
     * @param gridFile
     * @param options
     * @throws IOException
     */
    public GridFileReader( File gridFile, RasterIOOptions options ) throws IOException {
        instantiate( gridFile, options );
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates this grid reader with the given information.
     * 
     * @param infoFile
     * @param gridFile
     */
    protected synchronized void instantiate( GridMetaInfoFile infoFile, File gridFile ) {
        super.instantiate( infoFile );
        this.gridFile = gridFile;
        // set the location id to the grid files file name.
        if ( this.dataLocationId == null && gridFile != null ) {
            this.dataLocationId = FileUtils.getFilename( this.gridFile );
        }
        // if ( gridFile != null ) {
        // // set the tiles per blob depend on the gridFile size (if not full etc.)
        // setTilesPerBlob( (int) ( gridFile.length() / getBytesPerTile() ) );
        // }
        LOG.debug( "Tiles in grid blob (" + gridFile + "): " + getTilesPerBlob() );
    }

    /**
     * Signals the gridfile reader that it should (not) close the stream after a read.
     * 
     * @param yesNo
     */
    protected void leaveStreamOpen( boolean yesNo ) {
        this.leaveOpen = yesNo;
    }

    /**
     * @param options
     * @throws IOException
     * @throws NumberFormatException
     */
    private synchronized void instantiate( File gridFile, RasterIOOptions options )
                            throws NumberFormatException, IOException {
        if ( infoFile == null && gridFile != null ) {
            this.dataLocationId = options == null ? FileUtils.getFilename( gridFile )
                                                 : options.get( RasterIOOptions.ORIGIN_OF_RASTER );
            File metaInfo = GridMetaInfoFile.fileNameFromOptions( gridFile.getParent(),
                                                                  FileUtils.getFilename( gridFile ), options );
            this.instantiate( GridMetaInfoFile.readFromFile( metaInfo, options ), gridFile );
        }
    }

    private final FileChannel getFileChannel()
                            throws FileNotFoundException {
        synchronized ( gridFile ) {
            if ( this.fileAccess == null ) {
                this.fileAccess = new FileInputStream( gridFile );
            }
            return fileAccess.getChannel();
        }
    }

    private final void closeReadStream()
                            throws IOException {
        synchronized ( gridFile ) {
            if ( this.fileAccess != null && !this.leaveOpen ) {
                this.fileAccess.close();
                this.fileAccess = null;
            }
        }
    }

    @Override
    public boolean canLoad( File filename ) {
        return filename != null
               && GridRasterIOProvider.FORMATS.contains( FileUtils.getFileExtension( filename ).toLowerCase() );
    }

    @Override
    public Set<String> getSupportedFormats() {
        return new HashSet<String>( GridRasterIOProvider.FORMATS );
    }

    @Override
    public AbstractRaster load( File gridFile, RasterIOOptions options )
                            throws IOException {
        if ( gridFile == null ) {
            throw new IOException( "No grid file given." );
        }
        if ( infoFile == null
             || ( this.gridFile != null && !this.gridFile.getAbsoluteFile().equals( gridFile.getAbsoluteFile() ) ) ) {
            instantiate( gridFile, options );
        }

        byte[] noData = options == null ? null : options.getNoDataValue();
        int expectedTilesPerBlob = (int) ( gridFile.length() / getBytesPerTile() );
        if ( getTilesPerBlob() != expectedTilesPerBlob ) {
            LOG.error( "the number of tiles in the blob are wrong." );
            super.setTilesPerBlob( expectedTilesPerBlob );
        }
        // Load the entire grid into memory
        MemoryTileContainer mtc = new MemoryTileContainer();
        for ( int rowId = 0; rowId < infoFile.rows(); ++rowId ) {
            for ( int columnId = 0; columnId < infoFile.columns(); ++columnId ) {
                SimpleRaster rasterTile = (SimpleRaster) getTile( rowId, columnId );
                if ( rasterTile != null ) {
                    if ( noData != null ) {
                        rasterTile.getRasterData().setNoDataValue( noData );
                    }
                    mtc.addTile( rasterTile );
                }
            }
        }

        return new TiledRaster( mtc );
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer resultBuffer )
                            throws IOException {

        BufferResult res = null;
        RasterRect fRect = snapToGrid( rect );
        if ( fRect != null ) {
            int[] minCRmaxCR = getIntersectingTiles( fRect );
            if ( minCRmaxCR == null ) {
                return null;
            }
            int size = fRect.width * fRect.height * sampleSize;
            if ( resultBuffer == null ) {
                resultBuffer = ByteBufferPool.allocate( size, false );
            }
            synchronized ( gridFile ) {
                FileChannel channel = getFileChannel();
                RasterRect tmpRect = new RasterRect( 0, 0, fRect.width, fRect.height );
                for ( int col = minCRmaxCR[0]; col <= minCRmaxCR[2]; ++col ) {
                    for ( int row = minCRmaxCR[1]; row <= minCRmaxCR[3]; ++row ) {
                        readValuesFromTile( col, row, fRect, channel, resultBuffer );
                    }
                }
                res = new BufferResult( tmpRect, resultBuffer );
                closeReadStream();
            }
        }
        return res;
    }

    @Override
    protected void read( int columnId, int rowId, ByteBuffer buffer )
                            throws IOException {

        int tileId = getTileId( columnId, rowId );
        long begin = System.currentTimeMillis();
        int tileInBlob = tileId % getTilesPerBlob();
        // transfer the data from the blob
        try {
            synchronized ( gridFile ) {
                FileChannel channel = getFileChannel();
                // MappedByteBuffer map = channel.map( MapMode.READ_ONLY, tileInBlob * getBytesPerTile(),
                // buffer.remaining() );
                // buffer.put( map );
                // map.

                // LOG.debug( "Tile id: {} -> pos in blob: {}", tileId, +tileInBlob );
                channel.position( tileInBlob * getBytesPerTile() );
                channel.read( buffer );
                closeReadStream();
            }
            // rewinding is not an option, buffer.rewind();
        } catch ( IOException e ) {
            LOG.error( "Error reading tile data from blob: " + e.getMessage(), e );
        }

        long elapsed = System.currentTimeMillis() - begin;
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Loading of tile ({}x{}) in {} ms.", new Object[] { infoFile.getTileRasterWidth(),
                                                                          infoFile.getTileRasterHeight(), elapsed } );
        }

    }

    /**
     * @param tileColumn
     * @param tileRow
     * @param fRect
     * @param channel
     * @param resultBuffer
     * @throws IOException
     */
    protected void readValuesFromTile( int tileColumn, int tileRow, RasterRect fRect, FileChannel channel,
                                       ByteBuffer resultBuffer )
                            throws IOException {
        RasterRect tileRect = new RasterRect( infoFile.getTileRasterWidth() * tileColumn,
                                              infoFile.getTileRasterHeight() * tileRow, infoFile.getTileRasterWidth(),
                                              infoFile.getTileRasterHeight() );
        RasterRect inter = RasterRect.intersection( tileRect, fRect );
        if ( inter != null ) {
            // rewind the buffer, to be on the right side with the limit.
            resultBuffer.rewind();

            // the tile id inside the file.
            int tileId = getTileId( tileColumn, tileRow );
            long filePos = ( ( tileId % getTilesPerBlob() ) * getBytesPerTile() );

            // the size of one line of the intersection.
            int lineSize = inter.width * sampleSize;

            // offset to the byte buffer.
            int bufferOffsetY = inter.y - fRect.y;
            int bufferOffsetX = inter.x - fRect.x;

            // offset in the file channel
            int tileOffsetX = inter.x - tileRect.x;
            int tileOffsetY = inter.y - tileRect.y;

            // keep track of the number of rows in a tile.
            int currentIntersectRow = tileOffsetY;

            // position of the buffer.
            int currentPos = 0;
            // limit of the buffer.
            int limit = 0;
            // the current file position.
            long filePosition = 0;
            // loop over the intersection rows and put them into the right place in the bytebuffer.
            // File format is as follows, tile0_0[0/tilewidth*tileheight*samplesize],
            // tile0_1[prevtilepos/tilewidth*tileheight*samplesize], so first get file position of the tile, and add
            // up the position of the intersection inside the tile, then read row-wise into the buffer.
            for ( int row = bufferOffsetY; row < ( bufferOffsetY + inter.height ); ++row, ++currentIntersectRow ) {
                currentPos = ( bufferOffsetX + ( fRect.width * row ) ) * sampleSize;
                limit = currentPos + lineSize;
                // first the limit
                resultBuffer.limit( limit );
                // then the position.
                resultBuffer.position( currentPos );
                filePosition = filePos + ( ( tileOffsetX + ( currentIntersectRow * tileRect.width ) ) * sampleSize );
                channel.position( filePosition );
                channel.read( resultBuffer );
            }
        }

    }

    @Override
    public File file() {
        return gridFile;
    }

    @Override
    public String getDataLocationId() {
        return dataLocationId;
    }

    public void dispose() {
        // nothing to do.
    }

}
