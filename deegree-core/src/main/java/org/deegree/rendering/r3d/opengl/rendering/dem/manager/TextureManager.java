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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.vecmath.Point3d;

import org.deegree.commons.utils.math.VectorUtils;
import org.deegree.commons.utils.nio.DirectByteBufferPool;
import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.FragmentTexture;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureRequest;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTile;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the loading, unloading and caching of {@link FragmentTexture} objects and the enabling/disabling in a certain
 * GL context.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class TextureManager {

    static final Logger LOG = LoggerFactory.getLogger( TextureManager.class );

    private final DirectByteBufferPool bufferPool;

    private final TextureTileManager tileManager;

    private final double[] translationToLocalCRS;

    private final GPUCache gpuCache;

    private final int requestTimeout;

    /**
     * Number of bytes to be allocated, two floating points texture coordinates
     */
    private static final int NUMBER_OF_BYTES = 2 * 4;

    /**
     * @param directByteBufferPool
     *            to be used for the textures
     * @param tileManager
     *            managing all tiles
     * @param translationToLocalCRS
     *            the translation vector
     * @param maxFragmentTexturesInGPUMemory
     * @param requestTimeout
     *            in miliseconds
     */
    public TextureManager( DirectByteBufferPool directByteBufferPool, TextureTileManager tileManager,
                           double[] translationToLocalCRS, int maxFragmentTexturesInGPUMemory, int requestTimeout ) {
        bufferPool = directByteBufferPool;
        this.tileManager = tileManager;
        this.translationToLocalCRS = translationToLocalCRS;
        this.gpuCache = new GPUCache( maxFragmentTexturesInGPUMemory );
        this.requestTimeout = requestTimeout;
    }

    /**
     * 
     * @param unitsPerPixel
     * @return the matching resolution
     */
    public double getMatchingResolution( double unitsPerPixel ) {
        return tileManager.getMatchingResolution( unitsPerPixel );
    }

    /**
     * Retrieves view-optimized textures for the {@link RenderMeshFragment}s.
     * 
     * @param glRenderContext
     * @param maxProjectedTexelSize
     * @param fragments
     * @return view-optimized textures, not necessarily enabled
     */
    public Map<RenderMeshFragment, FragmentTexture> getTextures( RenderContext glRenderContext,
                                                                 float maxProjectedTexelSize,
                                                                 Set<RenderMeshFragment> fragments ) {
        LOG.debug( "Texturizing " + fragments.size() + " fragments" );
        Map<RenderMeshFragment, FragmentTexture> result = new HashMap<RenderMeshFragment, FragmentTexture>();

        // create texture requests for each fragment
        List<TextureRequest> requests = createTextureRequests( glRenderContext, maxProjectedTexelSize, fragments );

        // produce tile requests (multiple fragments may share a tile)
        List<TextureTileRequest> tileRequests = createTileRequests( requests );
        // System.out.println( "requests: " + requests.size() );
        // System.out.println( "tileRequests: " + tileRequests.size() );

        LOG.debug( tileRequests.size() + " tile requests" );

        // fetch texture tiles and assign textures to fragments
        for ( TextureRequest request : requests ) {

            // find corresponding texture tile request
            TextureTileRequest tileRequest = null;
            for ( TextureTileRequest minimalRequest : tileRequests ) {
                if ( minimalRequest.supersedes( request ) ) {
                    tileRequest = minimalRequest;
                    break;
                }
            }

            if ( tileRequest != null ) {

                TextureTile tile = tileManager.getMachingTile( tileRequest );
                if ( tile != null ) {
                    FragmentTexture texture = new FragmentTexture( request.getFragment(), tile );

                    int id = texture.getId();
                    FragmentTexture gpuTex = gpuCache.get( id );
                    if ( gpuTex != null && tileRequest.isFullfilled( gpuTex.getTextureTile() ) ) {
                        LOG.debug( "Using gpu cached texture." );
                        texture = gpuTex;
                    } else {
                        if ( gpuTex != null ) {
                            LOG.debug( "Found a gpu cached texture, but is was not fullfilled." );
                        }
                        int cap = ( request.getFragment().getData().getVertices().capacity() / 3 ) * NUMBER_OF_BYTES;

                        PooledByteBuffer buffer = bufferPool.allocate( cap );

                        // create the texture coordinates.
                        texture.generateTextureCoordinates( buffer, translationToLocalCRS );
                    }
                    result.put( request.getFragment(), texture );

                }
            } else {
                LOG.warn( "Found no matching tile request for request: " + request );
            }
        }
        return result;
    }

    /**
     * Enables this TextureManager.
     * 
     * @param textures
     * @param gl
     */
    public void enable( Collection<FragmentTexture> textures, GL gl ) {
        if ( textures != null && !textures.isEmpty() ) {
            // gpuCache.setContext( gl );
            for ( FragmentTexture fragmentTexture : textures ) {
                gpuCache.enable( fragmentTexture, gl );
            }
        }
    }

    /**
     * Cleans up all cached textures from this managers, which were marked as least recently used.
     * 
     * @param gl
     *            the context to which the textures were bound.
     */
    public void cleanUp( GL gl ) {
        gpuCache.cleanUp( gl );
    }

    private List<TextureRequest> createTextureRequests( RenderContext glRenderContext, float maxProjectedTexelSize,
                                                        Set<RenderMeshFragment> fragments ) {

        List<TextureRequest> requests = new ArrayList<TextureRequest>();

        float zScale = glRenderContext.getTerrainScale();

        ViewParams params = glRenderContext.getViewParams();
        ViewFrustum vf = params.getViewFrustum();
        Point3d eyePosPoint = vf.getEyePos();
        float[] eyePos = new float[] { (float) eyePosPoint.x, (float) eyePosPoint.y, (float) eyePosPoint.z };

        for ( RenderMeshFragment fragment : fragments ) {

            float[][] fragmentBBox = fragment.getBBox();

            float[][] scaledBBox = new float[2][3];
            scaledBBox[0] = Arrays.copyOf( fragmentBBox[0], 3 );
            scaledBBox[1] = Arrays.copyOf( fragmentBBox[1], 3 );
            scaledBBox[0][2] *= zScale;
            scaledBBox[1][2] *= zScale;

            double dist = VectorUtils.getDistance( scaledBBox, eyePos );
            double pixelSize = params.estimatePixelSizeForSpaceUnit( dist );
            double requiredUnitsPerPixel = maxProjectedTexelSize / pixelSize;
            // System.out.println( "clipped metersPerPixel: " + metersPerPixel );

            // rb: note the following values are still in center.
            double[] min = new double[] { fragmentBBox[0][0] - (float) translationToLocalCRS[0],
                                         fragmentBBox[0][1] - (float) translationToLocalCRS[1] };
            double[] max = new double[] { fragmentBBox[1][0] - (float) translationToLocalCRS[0],
                                         fragmentBBox[1][1] - (float) translationToLocalCRS[1] };
            double[][] fragmentBBoxWorldCoordinates = new double[][] { min, max };

            TextureRequest req = this.tileManager.createTextureRequest( glRenderContext, fragmentBBoxWorldCoordinates,
                                                                        requiredUnitsPerPixel, fragment );
            if ( req != null ) {
                requests.add( req );
            }
        }

        return requests;
    }

    private List<TextureTileRequest> createTileRequests( Collection<TextureRequest> origRequests ) {

        List<TextureTileRequest> requests = new ArrayList<TextureTileRequest>();
        for ( TextureRequest textureRequest : origRequests ) {
            requests.add( new TextureTileRequest( textureRequest ) );
        }

        List<TextureTileRequest> minimizedRequests = new ArrayList<TextureTileRequest>( requests.size() );
        for ( TextureTileRequest request : requests ) {
            boolean needed = true;
            List<TextureTileRequest> superseededRequests = new ArrayList<TextureTileRequest>();

            for ( TextureTileRequest request2 : minimizedRequests ) {
                if ( request2.supersedes( request ) ) {
                    needed = false;
                    break;
                } else if ( request.shareCorner( request2 )
                            && Math.abs( request.getUnitsPerPixel() - request2.getUnitsPerPixel() ) < 1E-8 ) {
                    superseededRequests.add( request2 );
                    request.merge( request2 );
                }
            }

            for ( TextureTileRequest textureRequest : superseededRequests ) {
                minimizedRequests.remove( textureRequest );
            }
            if ( needed ) {
                minimizedRequests.add( request );
            }
        }
        LOG.debug( "Tile requests: " + requests.size() + ", minimized: " + minimizedRequests.size() );
        return minimizedRequests;
    }

    @Override
    public String toString() {
        return "TextureManager with tileManager: " + this.tileManager.toString() + "in memory: "/* + memCache.size() */
               + ", in GPU: " + gpuCache.size();
    }

    /**
     * @return the requestTimeout
     */
    public int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * The <code>GPUCache</code> maps in memory fragment textures to their GPU counter part.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    private class GPUCache extends LinkedHashMap<Integer, FragmentTexture> {

        /**
         * 
         */
        private static final long serialVersionUID = -3224329551994159571L;

        private final int maxEntries;

        // // used to communicate the GL context to #removeEldestEntry()
        // private GL gl;

        private Set<FragmentTexture> markedAsRemoved;

        GPUCache( int maxEntries ) {
            super( maxEntries, 0.75f, false );
            this.maxEntries = maxEntries;
            this.markedAsRemoved = new HashSet<FragmentTexture>();
        }

        /**
         * @param gl
         *            context to which the textures were bound.
         * 
         */
        public void cleanUp( GL gl ) {
            // should only be called after a render cycle.
            LOG.debug( "Cleaning up {} number of LRU marked textures from gpu. Cache currently holds: {} textures ",
                       markedAsRemoved.size(), this.size() );
            for ( FragmentTexture ft : markedAsRemoved ) {

                if ( ft != null ) {
                    remove( ft.getId() );
                    if ( values().contains( ft ) ) {
                        LOG.warn( "Although removed, the give texture ({}) is still in the cache, this is strange.", ft );
                    }
                    ft.disable( gl );
                    ft.unload();
                    // remove( ft );
                }
            }
            markedAsRemoved.clear();
            LOG.debug( "After clean up, gpu cache still holds: {} textures ", this.size() );
        }

        /**
         * Enable the given fragment texture.
         * 
         * @param fragmentTexture
         * @param gl
         *            context to which the given textures are bound.
         */
        public void enable( FragmentTexture fragmentTexture, GL gl ) {
            if ( fragmentTexture.cachingEnabled() ) {
                FragmentTexture inCache = get( fragmentTexture.getId() );
                if ( inCache == null ) {
                    this.put( fragmentTexture.getId(), fragmentTexture );
                }

            }
            if ( !fragmentTexture.isEnabled() ) {
                fragmentTexture.enable( gl );
            }
        }

        /**
         * Overrides to the needs of a cache.
         * 
         * @param eldest
         * @return true as defined by the contract in {@link LinkedHashMap}.
         */
        @Override
        protected boolean removeEldestEntry( Map.Entry<Integer, FragmentTexture> eldest ) {
            if ( size() > maxEntries ) {
                markedAsRemoved.add( eldest.getValue() );
                return true;
            }
            return false;
        }
    }

}
