//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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
package org.deegree.model.coverage.raster;

import java.util.List;

import org.deegree.model.coverage.raster.data.RasterData;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;

/**
 * This class represents a tiled AbstractRaster.
 * 
 * A TiledRaster contains multiple non-overlapping AbstractRasters.
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
    public RasterEnvelope getRasterEnvelope() {
        return tileContainer.getRasterEnvelope();
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
    public AbstractRaster getSubset( Envelope env ) {
        // checkBounds( env );

        MemoryTileContainer resultTC = new MemoryTileContainer();
        TiledRaster result = new TiledRaster( resultTC );

        for ( AbstractRaster r : tileContainer.getTiles( env ) ) {
            try {
                Geometry intersection = r.getEnvelope().intersection( env );
                
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

                resultTC.addTile( r.getSubset( subsetEnv ) );
            } catch ( IndexOutOfBoundsException e ) {
                // TODO remove after touches-bug is fixed
            }
        }
        if ( resultTC.getRasterEnvelope() == null ) {
            throw new IndexOutOfBoundsException( "no intersection between TiledRaster and requested subset" );
        }

        return result;
    }

    @Override
    public void setSubset( Envelope envelope, AbstractRaster source ) {
        for ( AbstractRaster r : getTileContainer().getTiles( envelope ) ) {
            if ( r instanceof SimpleRaster ) {
                SimpleRaster sr = (SimpleRaster) r;
                Envelope subsetEnv = sr.getEnvelope().intersection( envelope ).getEnvelope();
                sr.setSubset( subsetEnv, source );
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void setSubset( double x, double y, AbstractRaster source ) {
        RasterEnvelope srcREnv = source.getRasterEnvelope();
        RasterEnvelope dstREnv = new RasterEnvelope( x, y, srcREnv.getXRes(), srcREnv.getYRes() );
        Envelope dstEnv = dstREnv.getEnvelope( source.getColumns(), source.getRows() );
        RasterData srcData = source.getAsSimpleRaster().getRasterData();
        SimpleRaster movedRaster = new SimpleRaster( srcData, dstEnv, dstREnv );
        setSubset( dstEnv, movedRaster );
    }

    @Override
    public void setSubset( double x, double y, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubset( Envelope env, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {

        Envelope env = getEnvelope();
        List<AbstractRaster> tiles = getTileContainer().getTiles( env );
        SimpleRaster result = tiles.get( 0 ).getAsSimpleRaster().createCompatibleSimpleRaster( getRasterEnvelope(), env );

        for ( AbstractRaster r : tiles ) {
            Geometry intersec = r.getEnvelope().intersection( env );
            if ( intersec != null ) {
                if ( intersec instanceof Point ) {
                    continue;
                }
                Envelope subsetEnv = intersec.getEnvelope();
                result.setSubset( subsetEnv, r );
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
