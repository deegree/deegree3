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

import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TextureTile} applied to a {@link RenderMeshFragment}, also wraps OpenGL resources (texture coordinates).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class FragmentTexture {

    private static final Logger LOG = LoggerFactory.getLogger( FragmentTexture.class );

    private final RenderMeshFragment fragment;

    private final TextureTile texture;

    private final PooledByteBuffer buffer;

    // just wrapped around buffer
    private final FloatBuffer texCoordsBuffer;

    private int textureID = -1;

    // 0: texture coordinates buffer
    private int[] glBufferObjectIds;

    private double xOffset;

    private double yOffset;

    /**
     * Creates a new {@link FragmentTexture} from the
     * 
     * @param geometry
     * @param texture
     */
    public FragmentTexture( RenderMeshFragment geometry, TextureTile texture, double xOffset, double yOffset, PooledByteBuffer buffer ) {
        this.fragment = geometry;
        this.texture = texture;
        this.buffer = buffer;
        buffer.getBuffer().order( ByteOrder.nativeOrder() );
        this.texCoordsBuffer = generateTexCoordsBuffer( xOffset, yOffset );
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    private FloatBuffer generateTexCoordsBuffer( double xOffset, double yOffset ) {

        double minX = -xOffset;
        double minY = -yOffset;

        float[][] bbox = fragment.getBBox();
        float patchXMin = bbox[0][0];
        float patchYMin = bbox[0][1];
        float patchXMax = bbox[1][0];
        float patchYMax = bbox[1][1];

        float tileXMin = texture.getMinX() - (float) minX;
        float tileYMin = texture.getMinY() - (float) minY;
        float tileXMax = texture.getMaxX() - (float) minX;
        float tileYMax = texture.getMaxY() - (float) minY;

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
     * Returns the resolution of the texture (world units per pixel).
     * 
     * @return the resolution of the texture
     */
    public float getTextureResolution() {
        return texture.getMetersPerPixel();
    }

    public TextureTile getTextureTile() {
        return texture;
    }

    int getGLTextureId() {
        if ( textureID == -1 ) {
            throw new RuntimeException();
        }
        return textureID;
    }

    int getGLVertexCoordBufferId() {
        return glBufferObjectIds[0];
    }

    public void enable( GL gl ) {
        if ( textureID == -1 ) {
            textureID = texture.enable( gl );
        }

        if ( glBufferObjectIds == null ) {
            generateTexCoordsBuffer( xOffset, yOffset );
            glBufferObjectIds = new int[1];
            gl.glGenBuffersARB( 1, glBufferObjectIds, 0 );

            // bind vertex buffer object (vertex coordinates)
            gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
            gl.glBufferDataARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, texCoordsBuffer.capacity() * 4, texCoordsBuffer,
                                GL.GL_STATIC_DRAW_ARB );
        }
    }

    public void disable( GL gl ) {
        if ( textureID != -1 ) {
            texture.disable( gl );
            textureID = -1;
        }
        if ( glBufferObjectIds != null ) {
            int[] bufferObjectIds = this.glBufferObjectIds;
            this.glBufferObjectIds = null;
            gl.glDeleteBuffersARB( bufferObjectIds.length, bufferObjectIds, 0 );
        }
    }

    public void unload() {
        buffer.free();
    }

    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return glBufferObjectIds != null && textureID != -1;
    }
}
