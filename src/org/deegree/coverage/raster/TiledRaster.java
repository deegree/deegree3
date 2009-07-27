//$HeadURL$
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
package org.deegree.coverage.raster;

import java.util.List;

import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.container.TileContainer;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

/**
 * This class represents a tiled AbstractRaster.
 *
 * A TiledRaster contains multiple non-overlapping (TODO verify this) AbstractRasters
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TiledRaster extends AbstractRaster {

    private TileContainer tileContainer;

    /**
     * Creates a new TiledRaster with tiles from the given TileContainer
     *
     * @param tileContainer
     *            wraps all tiles
     */
    public TiledRaster( TileContainer tileContainer ) {
        super();
        this.tileContainer = tileContainer;
    }

    /**
     * Returns the wrapper for all tiles.
     *
     * @return The container for all tiles.
     */
    public TileContainer getTileContainer() {
        return tileContainer;
    }

    @Override
    public Envelope getEnvelope() {
        return tileContainer.getEnvelope();
    }

    @Override
    public RasterReference getRasterReference() {
        return tileContainer.getRasterReference();
    }

    // TODO: convert to TileContainer
    @Override
    public TiledRaster copy() {
        // TiledRaster result = new TiledRaster();
        // for ( AbstractRaster r : tiles ) {
        // result.addTile( r.copy() );
        // }
        // return result;
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractRaster getSubRaster( Envelope env ) {
        // checkBounds( env );

        MemoryTileContainer resultTC = new MemoryTileContainer();
        TiledRaster result = new TiledRaster( resultTC );

        for ( AbstractRaster r : tileContainer.getTiles( env ) ) {
            try {
                Geometry intersection = r.getEnvelope().intersection( env );

                // rb: is this actually needed, because the tilecontainer checks this as well?
                if ( intersection == null ) {
                    continue;
                }
                // ignore if it only touches a tile
                if ( intersection instanceof Point ) {
                    continue;
                }
                if ( intersection instanceof Curve ) {
                    continue;
                }
                Envelope subsetEnv = intersection.getEnvelope();

                resultTC.addTile( r.getSubRaster( subsetEnv ) );
            } catch ( IndexOutOfBoundsException e ) {
                // TODO remove after touches-bug is fixed
            }
        }
        if ( resultTC.getRasterReference() == null ) {
            throw new IndexOutOfBoundsException( "no intersection between TiledRaster and requested subset" );
        }

        return result;
    }

    @Override
    public void setSubRaster( Envelope envelope, AbstractRaster source ) {
        for ( AbstractRaster r : getTileContainer().getTiles( envelope ) ) {
            if ( r instanceof SimpleRaster ) {
                SimpleRaster sr = (SimpleRaster) r;
                Envelope subsetEnv = sr.getEnvelope().intersection( envelope ).getEnvelope();
                sr.setSubRaster( subsetEnv, source );
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void setSubRaster( double x, double y, AbstractRaster source ) {
        RasterReference srcREnv = source.getRasterReference();
        RasterReference dstREnv = new RasterReference( x, y, srcREnv.getXRes(), srcREnv.getYRes() );
        Envelope dstEnv = dstREnv.getEnvelope( source.getColumns(), source.getRows() );
        RasterData srcData = source.getAsSimpleRaster().getRasterData();
        SimpleRaster movedRaster = new SimpleRaster( srcData, dstEnv, dstREnv );
        setSubRaster( dstEnv, movedRaster );
    }

    @Override
    public void setSubRaster( double x, double y, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubRaster( Envelope env, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {

        Envelope env = getEnvelope();
        List<AbstractRaster> tiles = getTileContainer().getTiles( env );
        if ( tiles == null || tiles.isEmpty() ) {
            throw new NullPointerException( "The given tile container does not contain any tiles. " );
        }
        SimpleRaster result = tiles.get( 0 ).getAsSimpleRaster().createCompatibleSimpleRaster( getRasterReference(), env );

        for ( AbstractRaster r : tiles ) {
            Geometry intersec = r.getEnvelope().intersection( env );
            if ( intersec != null ) {
                if ( intersec instanceof Point ) {
                    continue;
                }
                Envelope subsetEnv = intersec.getEnvelope();
                result.setSubRaster( subsetEnv, r );
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append( "TiledRaster: " + getEnvelope() );
        result.append( "\n\t" );
        result.append( getTileContainer().toString() );
        return result.toString();
    }

}
