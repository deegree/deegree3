//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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

import org.deegree.model.coverage.raster.data.RasterDataReader;

/**
 * This class creates a RasterDataContainer. A RasterDataContainer wraps a RasterDataReader and controls the
 * loading/storing of the raster data.
 * 
 * @version $Revision: $
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 */
public class RasterDataContainerFactory {

    /**
     * Defines how raster should be loaded/stored.
     */
    public enum LoadingPolicy {
        /** Load raster right away and keep in memory */
        NONE,
        /** Load raster on first access and keep in memory */
        LAZY,
        /** Use caching. Load raster on fist access and cache in memory */
        CACHED
    }

    private static LoadingPolicy defaultLoadingPolicy = LoadingPolicy.NONE;

    /**
     * Create a RasterDataContainer for given LoadingPolicy. The loadign policy controlls if a raster should be loaded
     * immediately, on demand or cached.
     * 
     * @param reader
     * @param policy
     * @return a RasterDataContainer that wraps the given RasterDataReader
     */
    public static RasterDataContainer withLoadingPolicy( RasterDataReader reader, LoadingPolicy policy ) {
        RasterDataContainer result;

        // TODO make configurable
        switch ( policy ) {
        case NONE:
            result = new MemoryRasterDataContainer( reader );
            break;
        // case LAZY:
        // result = new LazyRasterDataContainer( reader );
        // break;
        // case CACHED:
        // result = new CachedRasterDataContainer( reader );
        // break;
        default:
            throw new UnsupportedOperationException( "Unsupported LoadingPolicy" );
        }

        return result;
    }

    /**
     * Creates a RasterDataContainer with the default loading policy.
     * 
     * @param reader
     * @return a RasterDataContainer that wraps the given RasterDataReader
     */
    public static RasterDataContainer withDefaultLoadingPolicy( RasterDataReader reader ) {
        return withLoadingPolicy( reader, defaultLoadingPolicy );
    }
}
