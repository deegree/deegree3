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
package org.deegree.coverage.raster.container;

import java.util.List;

import org.deegree.coverage.ResolutionInfo;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.geometry.Envelope;

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
     * Returns the RasterReference of all tiles in this container.
     * 
     * @return The raster envelope of the tiles.
     */
    public RasterGeoReference getRasterReference();

    /**
     * Return the first tile of this container. First is implementation status.
     * 
     * @return the first tile in the container
     */
    public RasterDataInfo getRasterDataInfo();

    /**
     * @return the information about the resolution
     */
    public ResolutionInfo getResolutionInfo();

}
