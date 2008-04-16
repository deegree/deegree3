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

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.coverage.raster.implementation.RasterFactory;
import org.deegree.model.geometry.primitive.Envelope;

/**
 * This TileContainer keeps all tiles (AbstractRaster) in memory.
 * 
 * Use this container for tiles with a few thousand or less tiles. The AbstractRaster should be loaded with a LAZY or
 * CACHED LoadingPolicy (see {@link RasterFactory}).
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MemoryTileContainer implements TileContainer {

    private List<AbstractRaster> tiles = new ArrayList<AbstractRaster>();

    private RasterEnvelope rasterEnvelope;

    private Envelope envelope;

    /**
     * Creates a MemoryTileContainer with given tiles.
     * 
     * @param abstractRasters
     *            one or more tiles
     */
    public MemoryTileContainer( AbstractRaster... abstractRasters ) {
        for ( AbstractRaster raster : abstractRasters ) {
            addTile( raster );
        }
    }

    /**
     * Creates a MemoryTileContainer with given tiles.
     * 
     * @param abstractRasters
     *            one or more tiles
     */
    public MemoryTileContainer( List<AbstractRaster> abstractRasters ) {
        for ( AbstractRaster raster : abstractRasters ) {
            addTile( raster );
        }
    }

    /**
     * Adds a new tile to the container.
     * 
     * @param raster
     *            new tile
     */
    public void addTile( AbstractRaster raster ) {
        if ( this.envelope == null ) {
            this.envelope = raster.getEnvelope();
        } else {
            this.envelope = this.envelope.merger( raster.getEnvelope() );
        }
        if ( this.rasterEnvelope == null ) {
            this.rasterEnvelope = raster.getRasterEnvelope();
        } else {
            this.rasterEnvelope = this.rasterEnvelope.merger( raster.getRasterEnvelope() );
        }
        tiles.add( raster );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getTiles(org.deegree.model.geometry.primitive.Envelope)
     */
    public List<AbstractRaster> getTiles( Envelope env ) {
        List<AbstractRaster> result = new ArrayList<AbstractRaster>();
        for ( AbstractRaster r : tiles ) {
            if ( env.intersects( r.getEnvelope() ) ) {
                result.add( r );
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getEnvelope()
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getRasterEnvelope()
     */
    public RasterEnvelope getRasterEnvelope() {
        return rasterEnvelope;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for ( AbstractRaster r : tiles ) {
            result.append( r.toString() );
            result.append( "\n\t" );
        }
        if ( result.length() > 0 ) {
            result.delete( result.length() - 3, result.length() );
        }
        return result.toString();
    }

}
