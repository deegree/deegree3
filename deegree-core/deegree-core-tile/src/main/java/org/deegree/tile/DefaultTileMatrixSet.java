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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * The <code>DefaultTileMatrixSet</code> is an implementation of the <code>TileMatrixSet</code> that selects tile
 * matrices manually based on the tile matrix metadata. It can be used in conjunction with any tile matrix
 * implementation.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class DefaultTileMatrixSet implements TileMatrixSet {

    private static final Logger LOG = getLogger( DefaultTileMatrixSet.class );

    private final List<TileMatrix> matrices;

    private TileMatrixSetMetadata metadata;

    public DefaultTileMatrixSet( List<TileMatrix> matrices, TileMatrixSetMetadata metadata ) {
        this.matrices = matrices;
        this.metadata = metadata;
    }

    @Override
    public Iterator<Tile> getTiles( Envelope envelope, double resolution ) {
        // select correct matrix
        Iterator<TileMatrix> iter = matrices.iterator();
        TileMatrix matrix = iter.next();
        TileMatrix next = matrix;
        while ( next.getMetadata().getResolution() <= resolution && iter.hasNext() ) {
            matrix = next;
            next = iter.next();
        }
        if ( next.getMetadata().getResolution() <= resolution ) {
            matrix = next;
        }
        TileMatrixMetadata md = matrix.getMetadata();
        final TileMatrix fmatrix = matrix;

        // calc tile indices
        Envelope menvelope = md.getSpatialMetadata().getEnvelope();
        if ( !menvelope.intersects( envelope ) ) {
            return Collections.<Tile> emptyList().iterator();
        }
        double mminx = menvelope.getMin().get0();
        double mminy = menvelope.getMin().get1();
        double minx = envelope.getMin().get0();
        double miny = envelope.getMin().get1();
        double maxx = envelope.getMax().get0();
        double maxy = envelope.getMax().get1();

        int tileminx = (int) Math.floor( ( minx - mminx ) / md.getTileWidth() );
        int tileminy = (int) Math.floor( ( miny - mminy ) / md.getTileHeight() );
        int tilemaxx = (int) Math.floor( ( maxx - mminx ) / md.getTileWidth() );
        int tilemaxy = (int) Math.ceil( ( maxy - mminy ) / md.getTileHeight() );

        // sanitize values
        tileminx = Math.max( 0, tileminx );
        tileminy = Math.max( 0, tileminy );
        tilemaxx = Math.max( 0, tilemaxx );
        tilemaxy = Math.max( 0, tilemaxy );
        tileminx = Math.min( md.getNumTilesX() - 1, tileminx );
        tileminy = Math.min( md.getNumTilesY() - 1, tileminy );
        tilemaxx = Math.min( md.getNumTilesX() - 1, tilemaxx );
        tilemaxy = Math.min( md.getNumTilesY() - 1, tilemaxy );

        int h = tileminy;
        tileminy = md.getNumTilesY() - tilemaxy - 1;
        tilemaxy = md.getNumTilesY() - h - 1;

        LOG.debug( "Selected tile matrix with resolution {}, from {}x{} to {}x{}.", new Object[] { md.getResolution(),
                                                                                                  tileminx, tileminy,
                                                                                                  tilemaxx, tilemaxy } );

        final int fminx = tileminx, fminy = tileminy, fmaxx = tilemaxx, fmaxy = tilemaxy;

        // fetch tiles lazily
        return new Iterator<Tile>() {
            int x = fminx, y = fminy;

            @Override
            public boolean hasNext() {
                return x <= fmaxx;
            }

            @Override
            public Tile next() {
                Tile t = fmatrix.getTile( x, y );
                if ( y == fmaxy ) {
                    y = fminy;
                    ++x;
                } else {
                    ++y;
                }
                return t;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public List<TileMatrix> getTileMatrices() {
        return matrices;
    }

    @Override
    public TileMatrixSetMetadata getMetadata() {
        return metadata;
    }

}
