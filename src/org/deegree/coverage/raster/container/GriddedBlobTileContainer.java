//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.coverage.raster.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.WorldFileAccess;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link GriddedTileContainer} that extracts the tiles from a blob with a custom format. See
 * d3_tools/RasterTreeGridifier on how to create the format.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GriddedBlobTileContainer extends GriddedTileContainer {

    private static Logger LOG = LoggerFactory.getLogger( GriddedBlobTileContainer.class );

    /** name of the index file. **/
    public static final String METAINFO_FILE_NAME = "gridded_raster.info";

    /** name of a blob file. **/
    public static final String BLOB_FILE_NAME = "blob_";

    /** extension of a blob file. **/
    public static final String BLOB_FILE_EXT = ".bin";

    private final long bytesPerTile;

    private final int tilesPerBlob;

    private final FileChannel[] blobChannels;

    /**
     * A gridded tile container which reads data from a deegree internal format. See d3_tools/RasterTreeGridifier on how
     * to create the format.
     * 
     * @param originLocation
     * @param blobDir
     * @param envelope
     * @param rows
     * @param columns
     * @param tileSamplesX
     * @param tileSamplesY
     * @throws IOException
     */
    public GriddedBlobTileContainer( OriginLocation originLocation, File blobDir, Envelope envelope, int rows,
                                     int columns, int tileSamplesX, int tileSamplesY ) throws IOException {

        super( originLocation, envelope, rows, columns, tileSamplesX, tileSamplesY );

        bytesPerTile = tileSamplesX * tileSamplesY * 3;
        LOG.debug( "Bytes per tile: " + bytesPerTile );

        long expectedBlobSize = (long) rows * columns * bytesPerTile;
        LOG.debug( "Expected blob size: " + expectedBlobSize );

        List<File> blobFiles = new ArrayList<File>();
        long totalSize = 0;
        int blobNo = 0;
        // find all blob files.
        while ( true ) {
            File blobFile = new File( blobDir, BLOB_FILE_NAME + blobNo + BLOB_FILE_EXT );
            if ( !blobFile.exists() ) {
                break;
            }
            blobFiles.add( blobFile );
            blobNo++;
            totalSize += blobFile.length();
        }
        LOG.debug( "Real blob size: " + totalSize );

        if ( expectedBlobSize != totalSize ) {
            String msg = "Size of blobs  (=" + totalSize + ") does not match the expected size (=" + expectedBlobSize
                         + ").";
            throw new IllegalArgumentException( msg );
        }

        blobChannels = new FileChannel[blobFiles.size()];
        for ( int i = 0; i < blobChannels.length; i++ ) {
            FileInputStream fis = new FileInputStream( blobFiles.get( i ) );
            blobChannels[i] = fis.getChannel();
        }

        tilesPerBlob = (int) ( blobFiles.get( 0 ).length() / bytesPerTile );
        LOG.debug( "Tiles per (full) blob: " + tilesPerBlob );
    }

    /**
     * Reads the index file, 'gridded_raster.info' from the given directory reads the world file information as well as
     * the number of tiles.
     * 
     * @param dir
     *            to read the gridded_raster.info
     * @param options
     *            which will hold information on the file.
     * @return a {@link GriddedBlobTileContainer} instantiated with the values read from the gridded_raster.info.
     * @throws IOException
     */
    public static GriddedBlobTileContainer create( File dir, RasterIOOptions options )
                            throws IOException {

        File metaInfoFile = new File( dir, METAINFO_FILE_NAME );
        BufferedReader br = new BufferedReader( new FileReader( metaInfoFile ) );
        RasterGeoReference worldFile = WorldFileAccess.readWorldFile( br, options );

        // read grid info
        int rows = Integer.parseInt( br.readLine() );
        int columns = Integer.parseInt( br.readLine() );
        int tileSamplesX = Integer.parseInt( br.readLine() );
        int tileSamplesY = Integer.parseInt( br.readLine() );

        // convert the envelopes to OUTER, because the world files are in center.
        Envelope env = worldFile.getEnvelope( OriginLocation.OUTER, tileSamplesX * columns, tileSamplesY * rows, null );
        br.close();
        return new GriddedBlobTileContainer( OriginLocation.OUTER, dir, env, rows, columns, tileSamplesX, tileSamplesY );
    }

    @Override
    public AbstractRaster getTile( int rowId, int columnId ) {

        int tileId = getTileId( columnId, rowId );
        long begin = System.currentTimeMillis();
        Envelope tileEnvelope = getTileEnvelope( rowId, columnId );
        RasterGeoReference tileRasterReference = RasterGeoReference.create( getRasterReference().getOriginLocation(),
                                                                            tileEnvelope, tileSamplesX, tileSamplesY );

        ByteBufferRasterData tileData = RasterDataFactory.createRasterData( tileSamplesX, tileSamplesY,
                                                                            new BandType[] { BandType.RED,
                                                                                            BandType.GREEN,
                                                                                            BandType.BLUE },
                                                                            DataType.BYTE, InterleaveType.PIXEL );
        ByteBuffer buffer = tileData.getByteBuffer();
        buffer.rewind();
        int blobNo = tileId / tilesPerBlob;
        int tileInBlob = tileId % tilesPerBlob;
        // set information of the read file in the raster data.
        tileData.info = blobNo + "_" + tileInBlob + "_" + rowId + "," + columnId + ".png";
        // transfer the data from the blob
        try {
            LOG.debug( "Tile id: " + tileId + " -> blob no " + blobNo + ", pos in blob: " + tileInBlob );

            FileChannel channel = blobChannels[blobNo];
            channel.position( tileInBlob * bytesPerTile );
            channel.read( buffer );
            buffer.rewind();

        } catch ( IOException e ) {
            LOG.error( "Error reading tile data from blob: " + e.getMessage(), e );
        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Loading of tile (" + tileSamplesX + "x" + tileSamplesY + ") in " + elapsed + " ms." );

        SimpleRaster tile = new SimpleRaster( tileData, tileEnvelope, tileRasterReference );
        // try {
        // RasterFactory.saveRasterToFile( tile, new File( "/tmp/" + blobNo + "_" + tileInBlob + "_" + rowId + ","
        // + columnId + ".png" ) );
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        return tile;
    }
}
