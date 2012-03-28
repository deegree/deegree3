//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.tile.persistence.filesystem;

import static java.io.File.separatorChar;

import java.io.File;
import java.text.DecimalFormat;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tile.Tile;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixMetadata;

/**
 * {@link TileMatrix} implementation that is based on the directory layout of the disk cache used by <a
 * href="http://tilecache.org/">TileCache</a>. <h4>Disk layout</h4> TileCache uses a hierarchy of 7 directories to
 * organize tiles.
 * <p>
 * Example: <code>layername/01/018/782/353/786/347/862.filetype</code>.
 * </p>
 * <nl>
 * <li>1st directory: <i>layername</i></li>
 * <li>2nd directory: <i>zoomlevel</i> (in 2 digits eg. 01)</li>
 * <li>3rd-5th directory: <i>column number (x)</i>, split into thousands: x = 018782353 results in 018/782/353</li>
 * <li>6rd-7th directory + filename: <i>row number (y)</i>, split into thousands: y = 786347862 results in 786/347/862</li>
 * <li>filename suffix: <i>filetype</i>
 * <p>
 * TODO verify if this is the only layout used by TileCache
 * </p>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class TileCacheMatrix implements TileMatrix {

    private final TileMatrixMetadata metadata;

    private final File zoomLevelDir;

    private final String suffix;

    private static final GeometryFactory fac = new GeometryFactory();

    private static final DecimalFormat FORMAT_XXX = new DecimalFormat( "000" );

    /**
     * 
     * @param metadata
     * @param zoomLevelDir
     * @param suffix
     */
    TileCacheMatrix( TileMatrixMetadata metadata, File zoomLevelDir, String suffix ) {
        this.metadata = metadata;
        this.zoomLevelDir = zoomLevelDir;
        this.suffix = suffix;
    }

    @Override
    public TileMatrixMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Tile getTile( int x, int y ) {
        if ( metadata.getNumTilesX() <= x || metadata.getNumTilesY() <= y || x < 0 || y < 0 ) {
            return null;
        }
        Envelope bbox = calcEnvelope( x, y );
        File file = determineFile( x, y );
        return new FileSystemTile( bbox, file );
    }

    private File determineFile( int x, int y ) {
        StringBuilder sb = new StringBuilder();
        sb.append( FORMAT_XXX.format( x / 1000000 ) );
        sb.append( separatorChar );
        sb.append( FORMAT_XXX.format( x / 1000 % 1000 ) );
        sb.append( separatorChar );
        sb.append( FORMAT_XXX.format( x % 1000 ) );
        sb.append( separatorChar );
        sb.append( FORMAT_XXX.format( y / 1000000 ) );
        sb.append( separatorChar );
        sb.append( FORMAT_XXX.format( y / 1000 % 1000 ) );
        sb.append( separatorChar );
        sb.append( FORMAT_XXX.format( y % 1000 ) );
        sb.append( '.' );
        sb.append( suffix );
        File file = new File( zoomLevelDir, sb.toString() );
        return file;
    }

    private Envelope calcEnvelope( int x, int y ) {
        double width = metadata.getTileWidth();
        double height = metadata.getTileHeight();
        Envelope env = metadata.getSpatialMetadata().getEnvelope();
        double minx = width * x + env.getMin().get0();
        double miny = env.getMax().get1() - height * y;
        return fac.createEnvelope( minx, miny - height, minx + width, miny, env.getCoordinateSystem() );
    }
}
