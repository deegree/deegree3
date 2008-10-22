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

import org.deegree.model.geometry.Envelope;

/**
 * This interface wraps tiles and abstracts from the source of the tiles.
 * 
 * Some possible sources are in memory list of AbstractRasters, a shape file with a tile index, or a database with tile
 * information.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface TileContainer {

    /**
     * Returns all tiles that intersects the envelope.
     * 
     * @param env
     *            return List with tiles
     * @return A <code>List</code> with all intersecting tiles.
     */
    public List<AbstractRaster> getTiles( Envelope env );

    /**
     * Returns the envelope of all tiles in this container.
     * 
     * @return The envelope of all tiles.
     */
    public Envelope getEnvelope();

    /**
     * Returns the RasterEnvelope of all tiles in this container.
     * 
     * @return The raster envelope of the tiles.
     */
    public RasterEnvelope getRasterEnvelope();

}
