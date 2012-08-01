//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.tile.persistence.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.Tiles;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreTransaction;

/**
 * <code>CachingTileStore</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class CachingTileStore implements TileStore {

    private TileStore tileStore;

    private Map<String, DefaultTileDataSet> tileMatrixSets;

    private final CacheManager cacheManager;

    private Cache cache;

    public CachingTileStore( TileStore tileStore, CacheManager cacheManager, String cacheName ) {
        this.tileStore = tileStore;
        this.cacheManager = cacheManager;
        this.cache = cacheManager.getCache( cacheName );
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        Collection<String> ids = tileStore.getTileDataSetIds();
        tileMatrixSets = new HashMap<String, DefaultTileDataSet>();
        for ( String id : ids ) {
            TileDataSet tms = tileStore.getTileDataSet( id );
            List<TileDataLevel> list = new ArrayList<TileDataLevel>();
            for ( TileDataLevel tm : tms.getTileDataLevels() ) {
                list.add( new CachingTileMatrix( tm, cache ) );
            }
            this.tileMatrixSets.put( id,
                                     new DefaultTileDataSet( list, tms.getTileMatrixSet(), tms.getNativeImageFormat() ) );
        }
    }

    @Override
    public Collection<String> getTileDataSetIds() {
        return tileMatrixSets.keySet();
    }

    @Override
    public void destroy() {
        cacheManager.shutdown();
    }

    @Override
    public SpatialMetadata getMetadata( String id ) {
        return tileStore.getMetadata( id );
    }

    @Override
    public TileDataSet getTileDataSet( String id ) {
        return tileMatrixSets.get( id );
    }

    @Override
    public Iterator<Tile> getTiles( String id, Envelope envelope, double resolution ) {
        return tileMatrixSets.get( id ).getTiles( envelope, resolution );
    }

    @Override
    public Tile getTile( String tileMatrixSet, String tileMatrix, int x, int y ) {
        TileDataLevel tm = tileMatrixSets.get( tileMatrixSet ).getTileDataLevel( tileMatrix );
        if ( tm == null ) {
            return null;
        }
        return tm.getTile( x, y );
    }

    /**
     * Removes matching objects from cache.
     * 
     * @param tileMatrixSet
     *            the id of the tile matrix set
     * @param envelope
     *            may be null, in which case all objects will be removed from the cache
     */
    public long invalidateCache( String tileMatrixSet, Envelope envelope ) {
        if ( envelope == null ) {
            int size = cache.getSize();
            cache.removeAll();
            return size;
        }
        long cnt = 0;
        for ( TileDataLevel tm : tileMatrixSets.get( tileMatrixSet ).getTileDataLevels() ) {
            long[] ts = Tiles.getTileIndexRange( tm, envelope );
            if ( ts != null ) {
                String id = tm.getMetadata().getIdentifier();
                for ( long x = ts[0]; x <= ts[2]; ++x ) {
                    for ( long y = ts[1]; y <= ts[3]; ++y ) {
                        if ( cache.remove( id + "_" + x + "_" + y ) ) {
                            ++cnt;
                        }
                    }
                }
            }
        }
        return cnt;
    }

    @Override
    public TileStoreTransaction acquireTransaction( String id ) {
        throw new UnsupportedOperationException( "CachingTileStore does not support transactions." );
    }

}
