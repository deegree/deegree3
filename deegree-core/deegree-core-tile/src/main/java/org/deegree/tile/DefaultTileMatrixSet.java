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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * <code>TileMatrixSet</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class DefaultTileMatrixSet {

    private static final Logger LOG = getLogger( DefaultTileMatrixSet.class );

    private final List<TileMatrix> matrices;

    public DefaultTileMatrixSet( List<TileMatrix> matrices ) {
        this.matrices = matrices;
    }

    public Iterator<Tile> getTiles( Envelope envelope, double resolution ) {
        // what's left is to produce tile objects on the fly instead of using a list here
        List<Tile> tiles = new ArrayList<Tile>();

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

        // calc tile indices
        Envelope menvelope = md.getSpatialMetadata().getEnvelope();
        if(!menvelope.intersects(envelope )){
            return tiles.iterator();
        }
        double mminx = menvelope.getMin().get0();
        double mminy = menvelope.getMin().get1();
        double minx = envelope.getMin().get0();
        double miny = envelope.getMin().get1();
        double mmaxx = menvelope.getMax().get0();
        double mmaxy = menvelope.getMax().get1();
        double maxx = envelope.getMax().get0();
        double maxy = envelope.getMax().get1();

        int tileminx, tileminy, tilemaxx, tilemaxy;
        if ( mminx > minx ) {
            tileminx = 0;
        } else {
            tileminx = (int) Math.floor( ( minx - mminx ) / md.getTileWidth() );
        }
        if ( mminy > miny ) {
            tileminy = 0;
        } else {
            tileminy = (int) Math.floor( ( miny - mminy ) / md.getTileHeight() );
        }
        if ( mmaxx < maxx ) {
            tilemaxx = md.getNumTilesX() - 1;
        } else {
            tilemaxx = Math.max(0, (int) Math.floor( ( maxx - mminx ) / md.getTileWidth() ));
        }
        if ( mmaxy < maxy ) {
            tilemaxy = md.getNumTilesY() - 1;
        } else {
            tilemaxy = Math.max(0, (int) Math.floor( ( maxy - mminy ) / md.getTileHeight() ));
        }

        LOG.debug( "Selected tile matrix with resolution {}, from {}x{} to {}x{}.", new Object[] { md.getResolution(),
                                                                                                  tileminx, tileminy,
                                                                                                  tilemaxx, tilemaxy } );

        // fetch tiles
        for ( int x = tileminx; x <= tilemaxx; ++x ) {
            for ( int y = tileminy; y <= tilemaxy; ++y ) {
                tiles.add( matrix.getTile( x, y ) );
            }
        }

        return tiles.iterator();
    }

}
