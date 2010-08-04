//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.coverage.raster.cache;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.grid.GridMetaInfoFile;
import org.slf4j.Logger;

/**
 * Information about raster cache files extends the {@link GridMetaInfoFile} which extends the WorldFile file layout.
 * The Cache information supplies information about the tiles on file and the width and height of the cached raster
 * file.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CacheInfoFile extends GridMetaInfoFile {
    private static final Logger LOG = getLogger( CacheInfoFile.class );

    private final long modificationTime;

    private final int rHeight;

    private final int rWidth;

    private final boolean[][] tilesOnFile;

    /**
     * @param geoRef
     * @param rows
     * @param columns
     * @param tileRasterWidth
     * @param tileRasterHeight
     * @param rdi
     * @param width
     * @param height
     * @param tilesOnFile
     * @param modificationTime
     */
    public CacheInfoFile( RasterGeoReference geoRef, int rows, int columns, int tileRasterWidth, int tileRasterHeight,
                          RasterDataInfo rdi, int width, int height, boolean[][] tilesOnFile, long modificationTime ) {
        super( geoRef, rows, columns, tileRasterWidth, tileRasterHeight, rdi );
        this.rWidth = width;
        this.rHeight = height;
        this.tilesOnFile = tilesOnFile;
        this.modificationTime = modificationTime;
    }

    /**
     * @param gMif
     * @param width
     * @param height
     * @param tilesInFile
     */
    private CacheInfoFile( GridMetaInfoFile gMif, int width, int height, boolean[][] tilesOnFile, long modificationTime ) {
        super( gMif.getGeoReference(), gMif.rows(), gMif.columns(), gMif.getTileRasterWidth(),
               gMif.getTileRasterHeight(), gMif.getDataInfo() );
        this.rWidth = width;
        this.rHeight = height;
        this.tilesOnFile = tilesOnFile;
        this.modificationTime = modificationTime;
    }

    /**
     * @param metaInfo
     *            the file used as raster cache file, to write meta information for.
     * @param info
     *            the meta information on the raster cache file.
     * @throws IOException
     *             if the file could not be written.
     * 
     */
    public static void write( File metaInfo, CacheInfoFile info )
                            throws IOException {

        if ( metaInfo != null ) {
            if ( !metaInfo.exists() ) {
                boolean nFileCreated = metaInfo.createNewFile();
                if ( !nFileCreated ) {
                    throw new IOException( "Could not write cache info, because the file: " + metaInfo
                                           + " could not be created." );
                }
            }
            PrintWriter writer = new PrintWriter( new FileWriter( metaInfo ) );
            GridMetaInfoFile.write( writer, info, null );

            // original data size.
            writer.println( info.rWidth );
            writer.println( info.rHeight );

            for ( int row = 0; row < info.tilesOnFile.length; ++row ) {
                StringBuilder sb = new StringBuilder();
                for ( int col = 0; col < info.tilesOnFile[row].length; ++col ) {
                    sb.append( ( info.tilesOnFile[row][col] ) ? 1 : 0 );
                }
                writer.println( sb.toString() );
            }
            writer.flush();
            writer.close();
        }
    }

    /**
     * @param cacheFile
     *            to get the meta information for.
     * @return the meta information for the given raster cache file. <code>null</code> if no such file exists.
     */
    public static CacheInfoFile read( File cacheFile ) {
        CacheInfoFile result = null;
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
                boolean[][] tilesOnFile = new boolean[gmif.rows()][gmif.columns()];
                int rows = gmif.rows();
                for ( int row = 0; row < rows; ++row ) {
                    String s = reader.readLine();
                    if ( s == null || s.length() != gmif.columns() ) {
                        throw new NullPointerException( "the number of rows|columns read was not correct" );
                    }
                    for ( int col = 0; col < s.length(); ++col ) {
                        tilesOnFile[row][col] = ( s.charAt( col ) == '1' );
                    }

                }
                result = new CacheInfoFile( gmif, width, height, tilesOnFile, cacheFile.lastModified() );
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
     * @return the modificationTime
     */
    public final long getModificationTime() {
        return modificationTime;
    }

    /**
     * @return the rHeight
     */
    public final int getRasterHeight() {
        return rHeight;
    }

    /**
     * @return the rWidth
     */
    public final int getRasterWidth() {
        return rWidth;
    }

    /**
     * @return the tilesOnFile
     */
    public final boolean[][] getTilesOnFile() {
        return tilesOnFile;
    }

}
