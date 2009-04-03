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

import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.nio.DirectByteBufferPool;
import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a {@link RenderableMeshFragment} and a matching {@link TextureTile}, so the mesh fragment can be
 * rendered with an applied texture.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class MeshFragmentTexture {

    // TODO not static
    private static final DirectByteBufferPool bufferPool = new DirectByteBufferPool(20 * 1024 * 1024, 200);    
    
    // TODO remove this constants
    private static final double AREA_MIN_X = 2568000;

    private static final double AREA_MIN_Y = 5606000;

    private static final Logger LOG = LoggerFactory.getLogger( MeshFragmentTexture.class );

    private final RenderableMeshFragment fragment;

    private final TextureTile texture;
    
    private final PooledByteBuffer buffer;

    final FloatBuffer texCoordsBuffer;
    
    private int textureID = -1;

    /**
     * Creates a new {@link MeshFragmentTexture} from the
     * 
     * @param geometry
     * @param texture
     */
    public MeshFragmentTexture( RenderableMeshFragment geometry, TextureTile texture ) {
        this.fragment = geometry;
        this.texture = texture;
        this.buffer = bufferPool.allocate( geometry.getData().getVertices().capacity() / 3 * 2 * 4 );
        buffer.getBuffer().order( ByteOrder.nativeOrder() );
        this.texCoordsBuffer = generateTexCoordsBuffer();
    }

    private FloatBuffer generateTexCoordsBuffer() {

        float[][] bbox = fragment.getBBox();
        float patchXMin = bbox[0][0];
        float patchYMin = bbox[0][1];
        float patchXMax = bbox[1][0];
        float patchYMax = bbox[1][1];

        float tileXMin = texture.getMinX() - (float) AREA_MIN_X;
        float tileYMin = texture.getMinY() - (float) AREA_MIN_Y;
        float tileXMax = texture.getMaxX() - (float) AREA_MIN_X;
        float tileYMax = texture.getMaxY() - (float) AREA_MIN_Y;

        if ( tileXMin > patchXMin || tileYMin > patchYMin || tileXMax < patchXMax || tileYMax < patchYMax ) {
            String msg = "Internal error. Returned texture tile is not suitable for the MeshFragment.";
            throw new IllegalArgumentException( msg );
        }

        float tileWidth = texture.getMaxX() - texture.getMinX();
        float tileHeight = texture.getMaxY() - texture.getMinY();

        // build texture coordinates buffer
        FloatBuffer vertexBuffer = fragment.getData().getVertices();
        vertexBuffer.rewind();

        FloatBuffer texCoordsBuffer = buffer.getBuffer().asFloatBuffer();
        for ( int i = 0; i < vertexBuffer.capacity() / 3; i++ ) {
            float x = vertexBuffer.get();
            float y = vertexBuffer.get();
            float z = vertexBuffer.get();
            texCoordsBuffer.put( ( x - tileXMin ) / tileWidth );
            texCoordsBuffer.put( 1.0f - ( y - tileYMin ) / tileHeight );
        }
        vertexBuffer.rewind();
        texCoordsBuffer.rewind();
        return texCoordsBuffer;
    }

    /**
     * Returns the resolution of the applied texture (meters per pixel).
     * 
     * @return the resolution of the applied texture
     */
    public float getTextureResolution() {
        return texture.getMetersPerPixel();
    }

    int getGLTextureId( GL gl ) {
        if (textureID == -1) {
            throw new RuntimeException();
        }
        return textureID;
    }
    
    public void disable (GL gl) {
        if (textureID != -1) {
            texture.disable( gl );
            textureID = -1;
        }
    }
       
    public void unload () {
        buffer.free();
    }

    public void enable( GL gl ) {
        if (textureID != -1) {
            throw new RuntimeException();
        }
        textureID = texture.enable( gl );
    }
}
