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
package org.deegree.coverage.raster.data.container;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.io.RasterDataReader;

/**
 * This interface is for abstraction of RasterData providers. Implementations of this interface can control the loading
 * and unloading of the raster data (i.e. caching, etc).
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface RasterDataContainer {
    /**
     * Sets the source of the raster data.
     * 
     * @param reader
     *            the raster data reader for this container
     */
    public void setRasterDataReader( RasterDataReader reader );

    /**
     * Returns the RasterData
     * 
     * @return RasterData
     */
    public RasterData getRasterData();

    /**
     * Returns the RasterData as a read-only copy. Only a read-only RasterData supports thread-safe read operations.
     * 
     * @return RasterData
     */
    public RasterData getReadOnlyRasterData();

    /**
     * Returns the columns / width in pixel of the raster
     * 
     * @return columns of the raster
     */
    public int getColumns();

    /**
     * Returns the rows / height in pixel of the raster
     * 
     * @return rows of the raster
     */
    public int getRows();
}
