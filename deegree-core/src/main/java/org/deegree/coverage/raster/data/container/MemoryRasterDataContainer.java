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
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;
import org.deegree.coverage.raster.io.RasterDataReader;

/**
 * This class implements a RasterDataContainer that keeps RasterData in memory.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class MemoryRasterDataContainer implements RasterDataContainer, RasterDataContainerProvider {

    private RasterData raster;

    /**
     * Creates an empty RasterDataContainer that stores the raster data in memory.
     */
    public MemoryRasterDataContainer() {
        // empty consturctor
    }

    /**
     * Reads RasterData from RasterReader and wraps it in a RasterDataContainer. RasterData stays in memory.
     * 
     * @param reader
     *            RasterReader for RasterData
     */
    public MemoryRasterDataContainer( RasterDataReader reader ) {
        this.raster = reader.read();
    }

    /**
     * Wraps RasterData in a RasterDataContainer. RasterData stays in memory.
     * 
     * @param raster
     *            RasterData to wrap
     */
    public MemoryRasterDataContainer( RasterData raster ) {
        this.raster = raster;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getColumns()
     */
    public int getColumns() {
        return raster.getWidth();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getRows()
     */
    public int getRows() {
        return raster.getHeight();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getRasterData()
     */
    public RasterData getRasterData() {
        return raster;
    }

    @Override
    public RasterData getReadOnlyRasterData() {
        return getRasterData().asReadOnly();
    }

    public void setRasterDataReader( RasterDataReader reader ) {
        // this.raster = reader.read();
    }

    public RasterDataContainer getRasterDataContainer( LoadingPolicy type ) {
        if ( type == LoadingPolicy.MEMORY ) {
            // the service loader caches provider instances, so return a new instance
            return new MemoryRasterDataContainer();
        }
        return null;
    }
}
