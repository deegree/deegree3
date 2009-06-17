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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;

/**
 * A {@link TextureTile} applied to a {@link RenderMeshFragment}, also wraps OpenGL resources (texture coordinates).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class FragmentTexture {

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
     * @param geometry
     * @param texture
     * @param xOffset
     * @param yOffset
     * @param buffer
     */
    public FragmentTexture( RenderMeshFragment geometry, TextureTile texture, double xOffset, double yOffset,
                            PooledByteBuffer buffer ) {
        this.fragment = geometry;
        this.texture = texture;
        this.buffer = buffer;
        // buffer.getBuffer().order( ByteOrder.nativeOrder() );
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
            // skip z value (not relevant for texture coordinate generation)
            vertexBuffer.get();

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

    /**
     * @return the texture tile.
     */
    public TextureTile getTextureTile() {
        return texture;
    }

    /**
     * @return the opengl texture id.
     */
    public int getGLTextureId() {
        if ( textureID == -1 ) {
            throw new RuntimeException();
        }
        return textureID;
    }

    /**
     * @return the GL-id of the buffer object
     */
    public int getGLVertexCoordBufferId() {
        return glBufferObjectIds[0];
    }

    /**
     * Enable this Fragment texture by generating a buffer object in the given context.
     *
     * @param gl
     *            to generate the buffer object to.
     */
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

    /**
     * Remove the buffer object from the gpu.
     *
     * @param gl
     */
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

    /**
     * Unload
     */
    public void unload() {
        buffer.free();
    }

    /**
     *
     * @return true if this Fragment has a buffer object id and a texture id.
     */
    public boolean isEnabled() {
        return glBufferObjectIds != null && textureID != -1;
    }
}
