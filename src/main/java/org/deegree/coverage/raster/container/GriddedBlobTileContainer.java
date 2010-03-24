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

import static org.deegree.coverage.raster.io.grid.GridMetaInfoFile.METAINFO_FILE_NAME;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.grid.GridFileReader;
import org.deegree.coverage.raster.io.grid.GridMetaInfoFile;
import org.deegree.coverage.raster.io.grid.GridReader;
import org.deegree.coverage.raster.io.grid.SplittedBlobReader;
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

    /** name of a blob file. **/
    public static final String BLOB_FILE_NAME = "blob_";

    /** extension of a blob file. **/
    public static final String BLOB_FILE_EXT = ".bin";

    // how much tiles are in a blob (except for the last blob)
    // private int tilesPerBlob;

    private GridReader blobReader;

    /**
     * @param metaInfoFile
     */
    private GriddedBlobTileContainer( GridMetaInfoFile metaInfoFile ) {
        super( metaInfoFile );
    }

    /**
     * A gridded tile container which reads data from a deegree internal format. See d3_tools/RasterTreeGridifier on how
     * to create the format.
     * 
     * @param metaInfoFile
     *            encapsulating the values read from the grid meta info file.
     * @param blobDir
     * @throws IOException
     */
    public GriddedBlobTileContainer( GridMetaInfoFile metaInfoFile, File blobDir ) throws IOException {
        this( metaInfoFile );

        File[] files = blobDir.listFiles( new FileFilter() {

            @Override
            public boolean accept( File pathname ) {
                if ( !pathname.isDirectory() ) {
                    String ext = FileUtils.getFileExtension( pathname );
                    String file = FileUtils.getFilename( pathname );
                    return ( file.startsWith( BLOB_FILE_NAME ) && BLOB_FILE_EXT.equalsIgnoreCase( "." + ext ) );
                }

                return false;
            }
        } );
        if ( files != null ) {
            if ( files.length > 1 ) {
                this.blobReader = new SplittedBlobReader( blobDir, BLOB_FILE_NAME, BLOB_FILE_EXT, metaInfoFile );
            } else {
                this.blobReader = new GridFileReader( metaInfoFile, files[0] );
            }

        }

        // List<File> blobFiles = new ArrayList<File>();
        // long totalSize = 0;
        // int blobNo = 0;
        //
        // // find all blob files.
        // while ( true ) {
        // File blobFile = new File( blobDir, BLOB_FILE_NAME + blobNo + BLOB_FILE_EXT );
        // if ( !blobFile.exists() ) {
        // break;
        // }
        // blobFiles.add( blobFile );
        // blobNo++;
        // totalSize += blobFile.length();
        // }
        // LOG.debug( "Concatenated grid size (of all blob files): " + totalSize );
        //
        // blobReaders = new GridReader[blobFiles.size()];
        // for ( int i = 0; i < blobReaders.length; ++i ) {
        // blobReaders[i] = new GridFileReader( metaInfoFile, blobFiles.get( i ) );
        // }
        // tilesPerBlob = blobReaders[0].getNumberOfTiles();
        // long expectedBlobSize = metaInfoFile.rows() * metaInfoFile.columns() * blobReaders[0].getBytesPerTile();
        // if ( expectedBlobSize != totalSize ) {
        // String msg = "Size of grid (all blob file) (=" + totalSize + ") does not match the expected size (="
        // + expectedBlobSize + ").";
        // throw new IllegalArgumentException( msg );
        // }

    }

    /**
     * A gridded tile container which reads data from a deegree internal format. See d3_tools/RasterTreeGridifier on how
     * to create the format. This methods takes a single bob file instead of scanning a given directory
     * 
     * @param blobFile
     *            to read the tiles from.
     * @param metaInfoFile
     * @throws IOException
     */
    public GriddedBlobTileContainer( File blobFile, GridMetaInfoFile metaInfoFile ) throws IOException {
        this( metaInfoFile );

        if ( !blobFile.exists() ) {
            throw new IOException( "Given blobfile:" + blobFile + " does not exist." );
        }
        long totalSize = blobFile.length();
        LOG.debug( "Real blob size: " + totalSize );

        // blobReaders = new GridFileReader[1];
        blobReader = new GridFileReader( metaInfoFile, blobFile );
        // tilesPerBlob = blobReader.getNumberOfTiles();
        long expectedBlobSize = metaInfoFile.rows() * metaInfoFile.columns() * blobReader.getBytesPerTile();
        if ( expectedBlobSize != totalSize ) {
            String msg = "Size of gridfile (=" + totalSize + ") does not match the expected size (=" + expectedBlobSize
                         + ").";
            throw new IllegalArgumentException( msg );
        }

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
        if ( !metaInfoFile.exists() ) {
            throw new IOException( "Could not find the " + METAINFO_FILE_NAME + " in the given directory, " + dir );
        }
        GridMetaInfoFile readFromFile = GridMetaInfoFile.readFromFile( metaInfoFile, options );
        return new GriddedBlobTileContainer( readFromFile, dir );
    }

    @Override
    public AbstractRaster getTile( int rowId, int columnId ) {
        // int tileId = getTileId( columnId, rowId );
        // int blobNo = tileId / tilesPerBlob;
        AbstractRaster result = null;
        try {
            result = blobReader.getTile( columnId, rowId );
        } catch ( IOException e ) {
            LOG.error( "Error reading tile data from blob: " + e.getMessage(), e );
        }
        return result;
    }
}
