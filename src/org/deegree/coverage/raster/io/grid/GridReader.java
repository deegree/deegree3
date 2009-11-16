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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.GriddedTileContainer;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.slf4j.Logger;

/**
 * The <code>GridReader</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GridReader implements RasterReader {

    private static final Logger LOG = getLogger( GridReader.class );

    private final static GeometryFactory geomFac = new GeometryFactory();

    private FileChannel fileAccess;

    private GridMetaInfoFile infoFile;

    private Envelope envelope;

    private double tileHeight;

    private double tileWidth;

    private int bytesPerTile;

    private int tilesPerBlob;

    private File gridFile;

    /**
     * An empty constructor used in the {@link GridRasterIOProvider}, to a location in time where no information is
     * known yet.
     */
    GridReader() {
        // empty constructor, no values are known yet (GridRasterIOProvider).
    }

    /**
     * Creates a new {@link GriddedTileContainer} instances.
     * 
     * @param infoFile
     *            containing the relevant information.
     * @param gridFile
     *            to be used.
     * 
     */
    public GridReader( GridMetaInfoFile infoFile, File gridFile ) {
        instantiate( infoFile, gridFile );
    }

    private synchronized void instantiate( GridMetaInfoFile infoFile, File gridFile ) {
        this.infoFile = infoFile;
        this.envelope = infoFile.getEnvelope( OriginLocation.OUTER );
        // this.envelopeWidth = envelope.getMax().get0() - envelope.getMin().get0();
        // this.envelopeHeight = envelope.getMax().get1() - envelope.getMin().get1();
        // this.envelopeWidth = envelope.getSpan0();
        // this.envelopeHeight = envelope.getSpan1();
        // this.rows = infoFile.getRows();
        // this.columns = infoFile.getColumns();
        // this.tileSamplesX = infoFile.getTileSamplesX();
        // this.tileSamplesY = infoFile.getTileSamplesY();
        this.tileWidth = envelope.getSpan0() / infoFile.getColumns();
        this.tileHeight = envelope.getSpan1() / infoFile.getRows();
        this.bytesPerTile = infoFile.getTileRasterWidth() * infoFile.getTileRasterHeight() * infoFile.getBands();
        this.gridFile = gridFile;
        this.tilesPerBlob = (int) ( gridFile.length() / bytesPerTile );
        LOG.debug( "Tiles in grid blob (" + gridFile + "): " + tilesPerBlob );
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

        int expectedTilesPerBlob = (int) ( gridFile.length() / bytesPerTile );
        if ( tilesPerBlob != expectedTilesPerBlob ) {
            LOG.error( "the number of tiles in the blob are wrong." );
            this.tilesPerBlob = expectedTilesPerBlob;
        }
        // Load the entire grid into memory
        MemoryTileContainer mtc = new MemoryTileContainer();
        for ( int rowId = 0; rowId < infoFile.getRows(); ++rowId ) {
            for ( int columnId = 0; columnId < infoFile.getColumns(); ++columnId ) {
                AbstractRaster rasterTile = getTile( rowId, columnId );
                mtc.addTile( rasterTile );
            }
        }

        return new TiledRaster( mtc );
    }

    /**
     * @param options
     * @throws IOException
     * @throws NumberFormatException
     */
    private synchronized void instantiate( File gridFile, RasterIOOptions options )
                            throws NumberFormatException, IOException {
        if ( this.infoFile == null ) {
            File metaInfo = GridMetaInfoFile.fileNameFromOptions( gridFile.getParent(), options );
            this.instantiate( GridMetaInfoFile.readFromFile( metaInfo, options ), gridFile );
        }
    }

    private int getColumnIdx( double x ) {
        double dx = x - envelope.getMin().get0();
        int columnIdx = (int) Math.floor( ( infoFile.getColumns() * dx ) / envelope.getSpan0() );
        if ( columnIdx < 0 ) {
            // signal outside
            return -1;
        }
        if ( columnIdx > infoFile.getColumns() - 1 ) {
            // signal outside
            return infoFile.getColumns();
        }
        return columnIdx;
    }

    private int getRowIdx( double y ) {
        double dy = y - envelope.getMin().get1();
        int rowIdx = (int) Math.floor( ( ( infoFile.getRows() * ( envelope.getSpan1() - dy ) ) / envelope.getSpan1() ) );
        if ( rowIdx < 0 ) {
            // signal outside
            return -1;
        }
        if ( rowIdx > infoFile.getRows() - 1 ) {
            // signal outside
            return infoFile.getRows();
        }
        return rowIdx;
    }

    /**
     * Calculates the id for a tile at a given position in the grid.
     * 
     * @param columnId
     *            column id, must be in the range [0 ... #columns - 1]
     * @param rowId
     *            row id, must be in the range [0 ... #rows - 1]
     * @return the tile's id
     */
    protected int getTileId( int columnId, int rowId ) {
        int idx = rowId * infoFile.getColumns() + columnId;
        return idx;
    }

    /**
     * Calculates the envelope for a tile at a given position in the grid.
     * 
     * @param columnId
     *            column id, must be in the range [0 ... #columns - 1]
     * @param rowId
     *            row id, must be in the range [0 ... #rows - 1]
     * @return the tile's envelope
     */
    protected Envelope getTileEnvelope( int rowId, int columnId ) {
        double xOffset = columnId * tileWidth;
        double yOffset = ( infoFile.getRows() - rowId - 1 ) * tileHeight;

        double minX = envelope.getMin().get0() + xOffset;
        double minY = envelope.getMin().get1() + yOffset;
        double maxX = minX + tileWidth;
        double maxY = minY + tileHeight;

        return geomFac.createEnvelope( minX, minY, maxX, maxY, envelope.getCoordinateSystem() );
    }

    private final synchronized FileChannel getFileChannel()
                            throws FileNotFoundException {
        if ( this.fileAccess == null ) {
            this.fileAccess = new FileInputStream( gridFile ).getChannel();
        }
        return fileAccess;
    }

    /**
     * Read a raster from the grid file at location (row,column).
     * 
     * @param rowId
     * @param columnId
     * @return the read raster or null if it could not be read.
     * @throws FileNotFoundException
     */
    public AbstractRaster getTile( int rowId, int columnId )
                            throws FileNotFoundException {
        FileChannel channel = getFileChannel();

        int tileId = getTileId( columnId, rowId );
        long begin = System.currentTimeMillis();
        Envelope tileEnvelope = getTileEnvelope( rowId, columnId );

        RasterGeoReference tileRasterReference = RasterGeoReference.create( OriginLocation.OUTER, tileEnvelope,
                                                                            infoFile.getTileRasterWidth(),
                                                                            infoFile.getTileRasterHeight() );

        ByteBufferRasterData tileData = RasterDataFactory.createRasterData( infoFile.getTileRasterWidth(),
                                                                            infoFile.getTileRasterHeight(),
                                                                            new BandType[] { BandType.RED,
                                                                                            BandType.GREEN,
                                                                                            BandType.BLUE },
                                                                            DataType.BYTE, InterleaveType.PIXEL );
        ByteBuffer buffer = tileData.getByteBuffer();
        buffer.rewind();

        int tileInBlob = tileId % tilesPerBlob;
        // set information of the read file in the raster data.
        tileData.info = tileInBlob + "_" + rowId + "," + columnId + ".png";

        // transfer the data from the blob
        try {
            LOG.debug( "Tile id: " + tileId + " -> pos in blob: " + tileInBlob );

            channel.position( tileInBlob * bytesPerTile );
            channel.read( buffer );
            buffer.rewind();

        } catch ( IOException e ) {
            LOG.error( "Error reading tile data from blob: " + e.getMessage(), e );
        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Loading of tile (" + infoFile.getTileRasterWidth() + "x" + infoFile.getTileRasterWidth() + ") in "
                   + elapsed + " ms." );

        SimpleRaster tile = new SimpleRaster( tileData, tileEnvelope, tileRasterReference );
        // try {
        // RasterFactory.saveRasterToFile( tile, new File( "/tmp/" + tileInBlob + "_" + rowId + "," + columnId
        // + ".png" ) );
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        return tile;
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return the number of tiles in this blob.
     */
    public int getNumberOfTiles() {
        return this.tilesPerBlob;
    }

    /**
     * @return the number of bytes one tile in the given grid has.
     */
    public long getBytesPerTile() {
        return this.bytesPerTile;
    }

}
