//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

import org.deegree.model.coverage.raster.data.RasterData;
import org.deegree.model.coverage.raster.data.RasterDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a RasterDataContainer that loads the data on first access.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class LazyRasterDataContainer implements RasterDataContainer, RasterDataContainerProvider {

    private RasterDataReader reader;

    private RasterData raster;

    private boolean rasterLoaded = false;

    private static Logger log = LoggerFactory.getLogger( LazyRasterDataContainer.class );

    /**
     * Creates an empty LazyRasterDataContainer that loads the raster on demand and stores the raster data in memory.
     */
    public LazyRasterDataContainer() {
        // empty consturctor
    }

    /**
     * Creates a RasterDataContainer that loads the data on first access.
     * 
     * @param reader
     *            RasterReader for the raster source
     */
    public LazyRasterDataContainer( RasterDataReader reader ) {
        this.reader = reader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getRasterData()
     */
    public RasterData getRasterData() {
        if ( !rasterLoaded ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "reading: " + this.toString() );
            }
            raster = reader.read();
            rasterLoaded = true;
        }
        return raster;
    }

    @Override
    public RasterData getReadOnlyRasterData() {
        return getRasterData().asReadOnly();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getColumns()
     */
    public int getColumns() {
        return reader.getWidth();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getRows()
     */
    public int getRows() {
        return reader.getHeight();
    }

    public void setRasterDataReader( RasterDataReader reader ) {
        this.rasterLoaded = false;
        this.raster = null;
        this.reader = reader;
    }

    public RasterDataContainer getRasterDataContainer( String type ) {
        if ( type.equalsIgnoreCase( RasterDataContainerFactory.LoadingPolicy.LAZY.toString() ) ) {
            // the service loader caches provider instances, so return a new instance
            return new LazyRasterDataContainer();
        }
        return null;
    }

}