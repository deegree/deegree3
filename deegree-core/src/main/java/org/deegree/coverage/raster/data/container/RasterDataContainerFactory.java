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

import java.util.ServiceLoader;

import org.deegree.coverage.raster.io.RasterDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a RasterDataContainer. A RasterDataContainer wraps a RasterDataReader and controls the
 * loading/storing of the raster data.
 * 
 * @version $Revision$
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 */
public class RasterDataContainerFactory {

    private static ServiceLoader<RasterDataContainerProvider> rasterContainerLoader = ServiceLoader.load( RasterDataContainerProvider.class );

    private static Logger LOG = LoggerFactory.getLogger( RasterDataContainerFactory.class );

    /**
     * Defines how raster should be loaded/stored.
     */
    public enum LoadingPolicy {
        /** No caching, alias for MEMORY */
        NONE,
        /** Load raster right away and keep in memory */
        MEMORY,
        /** Load raster on first access and keep in memory */
        LAZY,
        /** Use caching. Load raster on first access and cache in memory */
        CACHED
    }

    private static LoadingPolicy defaultLoadingPolicy = LoadingPolicy.NONE;

    /**
     * Create a RasterDataContainer for given LoadingPolicy. The loading policy controlls if a raster should be loaded
     * immediately, on demand or cached.
     * 
     * @param reader
     * @param policy
     * @return a RasterDataContainer that wraps the given RasterDataReader
     */
    public static RasterDataContainer withLoadingPolicy( RasterDataReader reader, LoadingPolicy policy ) {
        RasterDataContainer result;
        if ( policy == null ) {
            LOG.info( "Empty policy provided, using default loading policy (Memory) instead." );
            result = getRasterDataContainer( defaultLoadingPolicy );
        } else {
            switch ( policy ) {
            case NONE:
            case MEMORY:
                result = getRasterDataContainer( LoadingPolicy.MEMORY );
                break;
            case LAZY:
                result = getRasterDataContainer( policy );
                break;
            case CACHED:
                result = getRasterDataContainer( policy );
                break;
            default:
                throw new UnsupportedOperationException( "Unsupported LoadingPolicy: " + policy.name() + "." );
            }
        }
        result.setRasterDataReader( reader );
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

    private synchronized static RasterDataContainer getRasterDataContainer( LoadingPolicy l ) {
        for ( RasterDataContainerProvider provider : rasterContainerLoader ) {
            RasterDataContainer container = provider.getRasterDataContainer( l );
            if ( container != null ) {
                return container;
            }
        }
        LOG.error( "RasterDataContainer for type " + l
                   + " not found, returning memory raster data container (the default)" );
        return new MemoryRasterDataContainer();
    }

    /**
     * Sets the default loading policy for all new raster container.
     * 
     * @param policy
     *            if <code>null</code> it will be ignored.
     */
    public synchronized static void setDefaultLoadingPolicy( LoadingPolicy policy ) {
        if ( policy != null ) {
            defaultLoadingPolicy = policy;
        }
    }

    /**
     * 
     * @return the default loading policy
     */
    public synchronized static LoadingPolicy getDefaultLoadingPolicy() {
        return defaultLoadingPolicy;
    }
}
