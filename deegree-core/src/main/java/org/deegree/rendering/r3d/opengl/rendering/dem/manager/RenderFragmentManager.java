//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem.manager;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.media.opengl.GL;

import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the loading, unloading and caching of {@link RenderMeshFragment} data and the enabling/disabling in a certain
 * GL context.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class RenderFragmentManager {

    static final Logger LOG = LoggerFactory.getLogger( RenderFragmentManager.class );

    // contains the corresponding RenderableMeshFragment object for each fragment id
    final RenderMeshFragment[] renderFragments;

    private final Cache memoryCache;

    int inMemory;

    private int inGPU;

    private int maxInMemory, maxInGPU;

    private final MultiresolutionMesh mrModel;

    /**
     * Initialize the Manager with a multiresolution model.
     * 
     * @param mrModel
     * @param maxCached
     */
    public RenderFragmentManager( MultiresolutionMesh mrModel, int maxCached ) {

        LOG.debug( "Creating RenderFragmentManager." );
        this.mrModel = mrModel;
        renderFragments = new RenderMeshFragment[mrModel.fragments.length];
        for ( int fragmentId = 0; fragmentId < mrModel.fragments.length; fragmentId++ ) {
            renderFragments[fragmentId] = new RenderMeshFragment( mrModel.fragments[fragmentId] );
        }
        this.memoryCache = new Cache( maxCached );
    }

    /**
     * Ensures that the data of the specified fragments is available in main memory or in GPU memory.
     * 
     * @param fragments
     * @throws IOException
     */
    void require( Set<RenderMeshFragment> fragments )
                            throws IOException {

        long begin = System.currentTimeMillis();

        SortedSet<RenderMeshFragment> sortedFragments = new TreeSet<RenderMeshFragment>( fragments );
        for ( RenderMeshFragment fragment : fragments ) {
            RenderMeshFragment cachedFrag = memoryCache.get( fragment.getId() );
            if ( cachedFrag != null ) {
                if ( cachedFrag.isEnabled() || cachedFrag.isLoaded() ) {
                    sortedFragments.remove( fragment );
                }
            }

        }

        int loaded = 0;
        for ( RenderMeshFragment fragment : sortedFragments ) {
            if ( !( fragment.isLoaded() || fragment.isEnabled() ) ) {
                if ( !fragment.canAllocateEnoughMemory() ) {
                    // clear up least recently used cached memory.
                    LOG.debug( "Could not allocate enough memory, try to free some frome the cache." );
                    memoryCache.freeUp( fragment.size(), fragments );
                }
                fragment.load();
                inMemory++;
                loaded++;

                if ( inMemory > maxInMemory ) {
                    maxInMemory++;
                }
            }
            // if ( !memoryCache.containsKey( fragment.getId() ) ) {
            memoryCache.put( fragment.getId(), fragment );
            // }
            // memoryCache.put( fragment.getId(), fragment );
        }
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Preparing of " + fragments.size() + " fragments (" + loaded + " new): " + elapsed + " ms" );
    }

    /**
     * Ensures that the data of the specified fragments is available in GPU memory (ready for rendering).
     * 
     * @param fragments
     * @param gl
     * @throws IOException
     */
    void requireOnGPU( Collection<RenderMeshFragment> fragments, GL gl )
                            throws IOException {

        long begin = System.currentTimeMillis();
        int loaded = 0;
        SortedSet<RenderMeshFragment> sortedFragments = new TreeSet<RenderMeshFragment>( fragments );
        for ( RenderMeshFragment fragment : sortedFragments ) {
            if ( !fragment.isEnabled() ) {
                fragment.enable( gl );
                inGPU++;
                loaded++;
                if ( inGPU > maxInGPU ) {
                    maxInGPU++;
                }
            }
        }
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Preparing (GPU) of " + fragments.size() + " fragments (" + loaded + " new): " + elapsed + " ms" );
    }

    /**
     * Notifies the manager that the specified fragments are not needed for rendering anymore.
     * <p>
     * It's up to the manager to decide to unload them from GPU and/or memory or to keep them cached.
     * </p>
     * 
     * @param fragments
     * @param gl
     */
    void release( Collection<RenderMeshFragment> fragments, GL gl ) {
        long begin = System.currentTimeMillis();
        for ( RenderMeshFragment fragment : fragments ) {
            if ( fragment.isEnabled() ) {
                inGPU--;
                fragment.disable( gl );
            }
        }
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Releasing (GPU) of " + fragments.size() + " fragments: " + elapsed + " ms" );
    }

    @Override
    public String toString() {
        String s = "Loaded fragments: memory: " + inMemory + " (max=" + maxInMemory + ") , GPU: " + inGPU + " (max="
                   + maxInGPU + ")";
        return s;
    }

    /**
     * @return the current mesh.
     */
    public MultiresolutionMesh getMultiresolutionMesh() {
        return mrModel;
    }

    /**
     * 
     * The <code>Cache</code> is a Map containing renderfragments mapped to their GLBuffer ids.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    private class Cache extends LinkedHashMap<Integer, RenderMeshFragment> {

        /**
         *
         */
        private static final long serialVersionUID = 9126150039966705148L;

        private final int maxEntries;

        Cache( int maxEntries ) {
            super( 16, 0.75f, true );
            this.maxEntries = maxEntries;
        }

        /**
         * @param size
         * @param fragments
         */
        public void freeUp( int size, Set<RenderMeshFragment> fragments ) {
            List<Integer> toBeRemoved = new LinkedList<Integer>();
            int freeMem = 0;
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Current fragments in cache: {} requested fragments: {}.", size(), fragments.size() );
            }
            for ( RenderMeshFragment fragment : values() ) {
                if ( !fragments.contains( fragment ) ) {
                    toBeRemoved.add( fragment.getId() );
                    freeMem += fragment.size();
                    if ( freeMem > size ) {
                        break;
                    }
                }
            }
            if ( freeMem < size ) {
                throw new OutOfMemoryError(
                                            "Could not free up enough memory for all meshfragments, please configure more memory for the meshfragements (WPVS: DirectIOMemory and NumberOfDEMFragmentsCached)" );
            }
            if ( !toBeRemoved.isEmpty() ) {
                for ( Integer i : toBeRemoved ) {
                    RenderMeshFragment remove = remove( i );
                    if ( remove != null ) {
                        remove.unload();
                    }
                }
            }
        }

        /**
         * Overrides to the needs of a cache.
         * 
         * @param eldest
         * @return true as defined by the contract in {@link LinkedHashMap}.
         */
        @Override
        protected boolean removeEldestEntry( Map.Entry<Integer, RenderMeshFragment> eldest ) {
            if ( size() > maxEntries ) {
                inMemory--;
                eldest.getValue().unload();
                return true;
            }
            return false;
        }
    }

}
