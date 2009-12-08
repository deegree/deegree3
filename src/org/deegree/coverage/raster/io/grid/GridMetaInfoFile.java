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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.deegree.coverage.raster.container.GriddedBlobTileContainer;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.WorldFileAccess;
import org.deegree.geometry.Envelope;

/**
 * The <code>GridMetaInfoFile</code> encapsulates the values of the extended worldfile needed to understand blobs of the
 * {@link GriddedBlobTileContainer}
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GridMetaInfoFile {

    /** defined rasterio options name of the index file. **/
    public static final String METAINFO_FILE = "grid_meta_file";

    /** standard name of the index file. **/
    public static final String METAINFO_FILE_NAME = "gridded_raster.info";

    private final RasterGeoReference geoReference;

    private final int tileRasterWidth;

    private final int tileRasterHeight;

    private final int rows;

    private final int columns;

    private final int bands;

    private final DataType dataType;

    /**
     * @return the dataType
     */
    public final DataType getDataType() {
        return dataType;
    }

    /**
     * @param geoReference
     * @param rows
     * @param columns
     * @param tileRasterWidth
     * @param tileRasterHeight
     * @param bands
     * @param dataType
     */
    public GridMetaInfoFile( RasterGeoReference geoReference, int rows, int columns, int tileRasterWidth,
                             int tileRasterHeight, int bands, DataType dataType ) {
        this.geoReference = geoReference;
        this.tileRasterWidth = tileRasterWidth;
        this.tileRasterHeight = tileRasterHeight;
        this.columns = columns;
        this.rows = rows;
        this.bands = bands;
        this.dataType = dataType;
    }

    /**
     * @return the number of tiles in the height (rows) of the grid file.
     */
    public final int rows() {
        return rows;
    }

    /**
     * @return the number of tiles in the width (columns) of the grid file.
     */
    public final int columns() {
        return columns;
    }

    /**
     * @param geoReference
     * @param rows
     * @param columns
     * @param tileRasterWidth
     * @param tileRasterHeight
     * @param dataInfo
     */
    public GridMetaInfoFile( RasterGeoReference geoReference, int rows, int columns, int tileRasterWidth,
                             int tileRasterHeight, RasterDataInfo dataInfo ) {
        this( geoReference, rows, columns, tileRasterWidth, tileRasterHeight, dataInfo.bands, dataInfo.dataType );

    }

    /**
     * Read the grid info file which is basically a world file with supplement information.
     * 
     * @param infoFile
     *            to read.
     * @param options
     *            containing information about the crs, origin location etc.
     * @return an new {@link GridMetaInfoFile} read from the given file.
     * @throws NumberFormatException
     * @throws IOException
     */
    public static GridMetaInfoFile readFromFile( File infoFile, RasterIOOptions options )
                            throws NumberFormatException, IOException {
        BufferedReader br = new BufferedReader( new FileReader( infoFile ) );
        RasterGeoReference worldFile = WorldFileAccess.readWorldFile( br, options );

        // read grid info
        int rows = Integer.parseInt( br.readLine() );
        int columns = Integer.parseInt( br.readLine() );
        int tileSamplesX = Integer.parseInt( br.readLine() );
        int tileSamplesY = Integer.parseInt( br.readLine() );
        String nl = br.readLine();
        // try to read 'new' file info
        int bands = 3;
        DataType type = DataType.BYTE;
        if ( nl != null ) {
            try {
                bands = Integer.parseInt( nl );
            } catch ( NumberFormatException e ) {
                // old file.
            }
            // datatype
            nl = br.readLine();
            if ( nl != null ) {
                try {
                    type = DataType.fromDataBufferType( Integer.parseInt( nl ) );
                } catch ( NumberFormatException e ) {
                    // old file.
                }
            }
        }

        br.close();
        return new GridMetaInfoFile( worldFile, rows, columns, tileSamplesX, tileSamplesY, bands, type );
    }

    /**
     * Write the grid info file which is basically a world file with supplement information.
     * 
     * @param filename
     *            to write to.
     * @param metaInfo
     *            to write.
     * @param options
     *            containing information about the crs, origin location etc.
     * @throws NumberFormatException
     * @throws IOException
     */
    public static void writeToFile( File filename, GridMetaInfoFile metaInfo, RasterIOOptions options )
                            throws IOException {

        PrintWriter writer = new PrintWriter( new FileWriter( filename ) );
        // begins with standard world file entries
        writer.println( metaInfo.getGeoReference().getResolutionX() );
        writer.println( metaInfo.getGeoReference().getRotationY() );
        writer.println( metaInfo.getGeoReference().getRotationX() );
        writer.println( metaInfo.getGeoReference().getResolutionY() );
        double[] origin = metaInfo.getGeoReference().getOrigin();
        writer.println( origin[0] );
        writer.println( origin[1] );
        // now infos on grid
        writer.println( metaInfo.rows );
        writer.println( metaInfo.columns );
        writer.println( metaInfo.tileRasterWidth );
        writer.println( metaInfo.tileRasterHeight );
        writer.println( metaInfo.bands );
        writer.println( DataType.toDataBufferType( metaInfo.dataType ) );

        writer.close();
    }

    /**
     * find a filename from the given options or create a 'default-file-name' in the given directory.
     * 
     * @param defaultDir
     *            to use if the options do not contain a file, a file named gridded_raster.info
     *            {@link GridMetaInfoFile#METAINFO_FILE_NAME} will be created.
     * @param options
     *            to get the file for the given grid meta info.
     * @return a file defined by the given options or the default filename in the given directory.
     */
    public static File fileNameFromOptions( String defaultDir, RasterIOOptions options ) {

        String metaFile = options == null ? null : options.get( METAINFO_FILE );
        if ( metaFile == null ) {
            metaFile = defaultDir + File.separator + METAINFO_FILE_NAME;
        }
        return new File( metaFile );
    }

    /**
     * @return the geoReference
     */
    public final RasterGeoReference getGeoReference() {
        return geoReference;
    }

    /**
     * @return the width of the raster of a single tile
     */
    public final int getTileRasterWidth() {
        return tileRasterWidth;
    }

    /**
     * @return the height of the raster of a single tile
     */
    public final int getTileRasterHeight() {
        return tileRasterHeight;
    }

    /**
     * @param location
     *            of the origin for which the resulting envelope should be calculated.
     * @return an envelope based on the number of columns/rows in the grid file and the number of tile samples.
     */
    public Envelope getEnvelope( OriginLocation location ) {
        /* rb: crs null, use the default crs */
        return geoReference.getEnvelope( location, tileRasterWidth * columns, tileRasterHeight * rows, null );
    }

    /**
     * @return the number of bands of the file.
     */
    public int getBands() {
        return bands;
    }
}
