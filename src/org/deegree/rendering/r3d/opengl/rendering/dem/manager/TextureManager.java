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
import java.util.Collection;
import java.util.HashMap;
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

    private static final Logger LOG = LoggerFactory.getLogger( TextureManager.class );

    private final DirectByteBufferPool bufferPool;

    private final TextureTileManager tileManager;

    private final double[] translationToLocalCRS;

    private final MemoryCache memCache;

    private final GPUCache gpuCache;

    /**
     * Number of bytes to be allocated, (Normals+Vertices) * numberOfBytePerFloat
     */
    private static final int NUMBER_OF_BYTES = 2 * 4;

    /**
     * @param directByteBufferPool
     *            to be used for the textures
     * @param tileManager
     *            managing all tiles
     * @param translationToLocalCRS
     *            the translation vector
     * @param maxFragmentTexturesInMemory
     *            the number of texturetiles
     * @param maxFragmentTexturesInGPUMemory
     */
    public TextureManager( DirectByteBufferPool directByteBufferPool, TextureTileManager tileManager,
                           double[] translationToLocalCRS, int maxFragmentTexturesInMemory,
                           int maxFragmentTexturesInGPUMemory ) {
        bufferPool = directByteBufferPool;
        memCache = new MemoryCache( maxFragmentTexturesInMemory );
        this.tileManager = tileManager;
        this.translationToLocalCRS = translationToLocalCRS;
        this.gpuCache = new GPUCache( maxFragmentTexturesInGPUMemory );
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
        Map<RenderMeshFragment, FragmentTexture> meshFragmentToTexture = new HashMap<RenderMeshFragment, FragmentTexture>();

        // create texture requests for each fragment
        List<TextureRequest> requests = createTextureRequests( glRenderContext, maxProjectedTexelSize, fragments );

        // check which texture requests can be fullfilled from cache
        List<TextureRequest> fromCache = new ArrayList<TextureRequest>();
        for ( TextureRequest request : requests ) {
            FragmentTexture texture = memCache.get( request );
            if ( texture != null ) {
                meshFragmentToTexture.put( request.getFragment(), texture );
                fromCache.add( request );
            }
        }
        LOG.debug( "From cache: " + meshFragmentToTexture.size() );

        // determine remaining texture requests
        requests.removeAll( fromCache );
        LOG.debug( "To be processed: " + requests.size() );

        // produce tile requests (multiple fragments may share a tile)
        List<TextureTileRequest> tileRequests = createTileRequests( requests );
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

            TextureTile tile = tileManager.getMachingTile( tileRequest );
            PooledByteBuffer buffer = bufferPool.allocate( request.getFragment().getData().getVertices().capacity()
                                                           * NUMBER_OF_BYTES / 3 );
            FragmentTexture texture = new FragmentTexture( request.getFragment(), tile, translationToLocalCRS[0],
                                                           translationToLocalCRS[1], buffer );

            memCache.put( request, texture );
            // TODO needed?
            memCache.get( request );

            meshFragmentToTexture.put( request.getFragment(), texture );
        }
        return meshFragmentToTexture;
    }

    /**
     * Enables this TextureManager.
     * 
     * @param textures
     * @param gl
     */
    public void enable( Collection<FragmentTexture> textures, GL gl ) {
        for ( FragmentTexture fragmentTexture : textures ) {
            gpuCache.enable( fragmentTexture, gl );
        }
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
            scaledBBox[0][0] = fragmentBBox[0][0];
            scaledBBox[0][1] = fragmentBBox[0][1];
            scaledBBox[0][2] = fragmentBBox[0][2] * zScale;
            scaledBBox[1][0] = fragmentBBox[1][0];
            scaledBBox[1][1] = fragmentBBox[1][1];
            scaledBBox[1][2] = fragmentBBox[1][2] * zScale;

            double dist = VectorUtils.getDistance( scaledBBox, eyePos );
            double pixelSize = params.estimatePixelSizeForSpaceUnit( dist );
            double metersPerPixel = maxProjectedTexelSize / pixelSize;
            metersPerPixel = tileManager.getMatchingResolution( metersPerPixel );

            // check if the texture gets too large with respect to the maximum texture size
            metersPerPixel = clipResolution( metersPerPixel, fragmentBBox, glRenderContext.getMaxTextureSize() );

            // rb: note the following values are still in center.
            float minX = fragmentBBox[0][0] - (float) translationToLocalCRS[0];
            float minY = fragmentBBox[0][1] - (float) translationToLocalCRS[1];
            float maxX = fragmentBBox[1][0] - (float) translationToLocalCRS[0];
            float maxY = fragmentBBox[1][1] - (float) translationToLocalCRS[1];

            if ( LOG.isTraceEnabled() ) {
                LOG.trace( "frag bbox: " + fragmentBBox[0][0] + "," + fragmentBBox[0][1] + " | " + fragmentBBox[1][0]
                           + "," + fragmentBBox[1][1] );
                LOG.trace( "requ bbox: " + minX + "," + minY + " | " + maxX + "," + maxY );
            }

            requests.add( new TextureRequest( fragment, minX, minY, maxX, maxY, (float) metersPerPixel ) );
        }

        return requests;
    }

    private double clipResolution( double metersPerPixel, float[][] tilebbox, int maxTextureSize ) {
        // LOG.warn( "The maxTextureSize in the TextureManager is hardcoded to 1024." );
        float width = tilebbox[1][0] - tilebbox[0][0];
        float height = tilebbox[1][1] - tilebbox[0][1];
        float maxLen = width > height ? width : height;
        int textureSize = (int) Math.ceil( maxLen / (float) metersPerPixel );
        if ( textureSize > maxTextureSize ) {
            LOG.warn( "Texture size (=" + textureSize + ") exceeds maximum texture size (=" + maxTextureSize
                      + "). Meters/Pixel: " + metersPerPixel );
        }
        return metersPerPixel;
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
                            && request.getMetersPerPixel() == request2.getMetersPerPixel() ) {
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
        return "in memory: " + memCache.size() + ", in GPU: " + gpuCache.size();
    }

    /**
     * 
     * The <code>MemoryCache</code> maps texture request to their in memory counter part
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    private class MemoryCache extends LinkedHashMap<TextureRequest, FragmentTexture> {

        /**
         * 
         */
        private static final long serialVersionUID = -2046226967090513718L;

        private final int maxEntries;

        MemoryCache( int maxEntries ) {
            super( 16, 0.75f, true );
            this.maxEntries = maxEntries;
        }

        /**
         * Overrides to the needs of a cache.
         * 
         * @param eldest
         * @return true as defined by the contract in {@link LinkedHashMap}.
         */
        @Override
        protected boolean removeEldestEntry( Map.Entry<TextureRequest, FragmentTexture> eldest ) {
            if ( size() > maxEntries ) {
                eldest.getValue().unload();
                return true;
            }
            return false;
        }
    }

    /**
     * The <code>GPUCache</code> maps in memory fragment textures to their GPU counter part.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    private class GPUCache extends LinkedHashMap<FragmentTexture, FragmentTexture> {

        /**
         * 
         */
        private static final long serialVersionUID = -3224329551994159571L;

        private final int maxEntries;

        // used to communicate the GL context to #removeEldestEntry()
        private GL gl;

        GPUCache( int maxEntries ) {
            super( 16, 0.75f, false );
            this.maxEntries = maxEntries;
        }

        /**
         * Enable the given fragment texture.
         * 
         * @param fragmentTexture
         * @param gl
         */
        public void enable( FragmentTexture fragmentTexture, GL gl ) {
            this.gl = gl;
            this.put( fragmentTexture, fragmentTexture );
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
        protected boolean removeEldestEntry( Map.Entry<FragmentTexture, FragmentTexture> eldest ) {
            if ( size() > maxEntries ) {
                eldest.getValue().disable( gl );
                return true;
            }
            return false;
        }
    }

}
