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
import java.io.IOException;

import org.deegree.coverage.raster.container.GriddedBlobTileContainer;
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

    /** name of the index file. **/
    public static final String METAINFO_FILE_NAME = "gridded_raster.info";

    private final RasterGeoReference geoReference;

    private final int rows;

    private final int columns;

    private final int tileSamplesX;

    private final int tileSamplesY;

    /**
     * @param geoReference
     * @param rows
     * @param columns
     * @param tileSamplesX
     * @param tileSamplesY
     */
    public GridMetaInfoFile( RasterGeoReference geoReference, int rows, int columns, int tileSamplesX, int tileSamplesY ) {
        this.geoReference = geoReference;
        this.rows = rows;
        this.columns = columns;
        this.tileSamplesX = tileSamplesX;
        this.tileSamplesY = tileSamplesY;
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
        br.close();
        return new GridMetaInfoFile( worldFile, rows, columns, tileSamplesX, tileSamplesY );
    }

    /**
     * @return the geoReference
     */
    public final RasterGeoReference getGeoReference() {
        return geoReference;
    }

    /**
     * @return the rows
     */
    public final int getRows() {
        return rows;
    }

    /**
     * @return the columns
     */
    public final int getColumns() {
        return columns;
    }

    /**
     * @return the tileSamplesX
     */
    public final int getTileSamplesX() {
        return tileSamplesX;
    }

    /**
     * @return the tileSamplesY
     */
    public final int getTileSamplesY() {
        return tileSamplesY;
    }

    /**
     * @param location
     *            of the origin for which the resulting envelope should be calculated.
     * @return an envelope based on the number of columns/rows in the grid file and the number of tile samples.
     */
    public Envelope getEnvelope( OriginLocation location ) {
        /* rb: crs null, use the default crs */
        return geoReference.getEnvelope( location, tileSamplesX * columns, tileSamplesY * rows, null );
    }
}
