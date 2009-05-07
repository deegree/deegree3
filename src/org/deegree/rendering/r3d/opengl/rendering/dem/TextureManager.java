//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem;

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

    private final int maxTextureSize;

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
     * @param maxTextureSize
     *            the maximum width /heigth of a texture
     * @param maxFragmentTexturesInMemory
     *            the number of texturetiles
     * @param maxFragmentTexturesInGPUMemory
     */
    public TextureManager( DirectByteBufferPool directByteBufferPool, TextureTileManager tileManager,
                           double[] translationToLocalCRS, int maxTextureSize, int maxFragmentTexturesInMemory,
                           int maxFragmentTexturesInGPUMemory ) {
        bufferPool = directByteBufferPool;
        memCache = new MemoryCache( maxFragmentTexturesInMemory );
        this.tileManager = tileManager;
        this.translationToLocalCRS = translationToLocalCRS;
        this.maxTextureSize = maxTextureSize;
        this.gpuCache = new GPUCache( maxFragmentTexturesInGPUMemory );
    }

    public double getMatchingResolution( double unitsPerPixel ) {
        return tileManager.getMatchingResolution( unitsPerPixel );
    }    
    
    /**
     * Retrieves view-optimized textures for the {@link RenderMeshFragment}s.
     * 
     * @param params
     * @param maxProjectedTexelSize
     * @param fragments
     * @return view-optimized textures, not necessarily enabled
     */
    public Map<RenderMeshFragment, FragmentTexture> getTextures( ViewParams params, float maxProjectedTexelSize,
                                                                 Set<RenderMeshFragment> fragments ) {

        LOG.info( "Texturizing " + fragments.size() + " fragments" );
        Map<RenderMeshFragment, FragmentTexture> meshFragmentToTexture = new HashMap<RenderMeshFragment, FragmentTexture>();

        // create texture requests for each fragment
        List<TextureRequest> requests = createTextureRequests( params, maxProjectedTexelSize, fragments );

        // check which texture requests can be fullfilled from cache
        List<TextureRequest> fromCache = new ArrayList<TextureRequest>();
        for ( TextureRequest request : requests ) {
            FragmentTexture texture = memCache.get( request );
            if ( texture != null ) {
                meshFragmentToTexture.put( request.getFragment(), texture );
                fromCache.add( request );
            }
        }
        LOG.info( "From cache: " + meshFragmentToTexture.size() );

        // determine remaining texture requests
        requests.removeAll( fromCache );
        LOG.info( "To be processed: " + requests.size() );

        // produce tile requests (multiple fragments may share a tile)
        List<TextureTileRequest> tileRequests = createTileRequests( requests );
        LOG.info( tileRequests.size() + " tile requests" );

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

    public void enable( Collection<FragmentTexture> textures, GL gl ) {
        for ( FragmentTexture fragmentTexture : textures ) {
            gpuCache.enable( fragmentTexture, gl );
        }
    }

    private List<TextureRequest> createTextureRequests( ViewParams params, float maxProjectedTexelSize,
                                                        Set<RenderMeshFragment> fragments ) {

        List<TextureRequest> requests = new ArrayList<TextureRequest>();

        ViewFrustum vf = params.getViewFrustum();
        Point3d eyePosPoint = vf.getEyePos();
        float[] eyePos = new float[] { (float) eyePosPoint.x, (float) eyePosPoint.y, (float) eyePosPoint.z };

        for ( RenderMeshFragment fragment : fragments ) {
            float[][] fragmentBBox = fragment.getBBox();
            float[][] scaledBBox = new float[2][3];
            scaledBBox[0][0] = fragmentBBox[0][0];
            scaledBBox[0][1] = fragmentBBox[0][1];
            scaledBBox[0][2] = fragmentBBox[0][2] * params.getTerrainScale();
            scaledBBox[1][0] = fragmentBBox[1][0];
            scaledBBox[1][1] = fragmentBBox[1][1];
            scaledBBox[1][2] = fragmentBBox[1][2] * params.getTerrainScale();

            double dist = VectorUtils.getDistance( scaledBBox, eyePos );
            double pixelSize = params.estimatePixelSizeForSpaceUnit( dist );
            double metersPerPixel = maxProjectedTexelSize / pixelSize;
            metersPerPixel = tileManager.getMatchingResolution( metersPerPixel );

            // check if the texture gets too large with respect to the maximum texture size
            metersPerPixel = clipResolution( metersPerPixel, fragmentBBox );

            float minX = fragment.getBBox()[0][0] - (float) translationToLocalCRS[0];
            float minY = fragment.getBBox()[0][1] - (float) translationToLocalCRS[1];
            float maxX = fragment.getBBox()[1][0] - (float) translationToLocalCRS[0];
            float maxY = fragment.getBBox()[1][1] - (float) translationToLocalCRS[1];

            requests.add( new TextureRequest( fragment, minX, minY, maxX, maxY, (float) metersPerPixel ) );
        }

        return requests;
    }

    private double clipResolution( double metersPerPixel, float[][] tilebbox ) {

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
        LOG.info( "Tile requests: " + requests.size() + ", minimized: " + minimizedRequests.size() );
        return minimizedRequests;
    }

    private class MemoryCache extends LinkedHashMap<TextureRequest, FragmentTexture> {

        private final int maxEntries;

        MemoryCache( int maxEntries ) {
            super( 16, 0.75f, true );
            this.maxEntries = maxEntries;
        }

        protected boolean removeEldestEntry( Map.Entry<TextureRequest, FragmentTexture> eldest ) {
            if ( size() > maxEntries ) {
                eldest.getValue().unload();
                return true;
            }
            return false;
        }
    }

    private class GPUCache extends LinkedHashMap<FragmentTexture, FragmentTexture> {

        private final int maxEntries;

        // used to communicate the GL context to #removeEldestEntry()
        private GL gl;

        GPUCache( int maxEntries ) {
            super( 16, 0.75f, false );
            this.maxEntries = maxEntries;
        }

        public void enable( FragmentTexture fragmentTexture, GL gl ) {
            this.gl = gl;
            this.put( fragmentTexture, fragmentTexture );
            if ( !fragmentTexture.isEnabled() ) {
                fragmentTexture.enable( gl );
            }
        }

        protected boolean removeEldestEntry( Map.Entry<FragmentTexture, FragmentTexture> eldest ) {
            if ( size() > maxEntries ) {
                eldest.getValue().disable( gl );
                return true;
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return "in memory: " + memCache.size() + ", in GPU: " + gpuCache.size();
    }
}
