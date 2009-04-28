//$HeadURL$
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

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import javax.media.opengl.GL;

import org.deegree.rendering.i18n.Messages;
import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.multiresolution.MeshFragmentData;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a {@link MeshFragment} of a {@link MultiresolutionMesh} that can be rendered via JOGL.
 * <p>
 * The geometry data of a {@link RenderMeshFragment} has one of the following states:
 * <ul>
 * <li>Not loaded</li>
 * <li>Loaded to main memory (i.e. buffer objects are created and filled)</li>
 * <li>Loaded to GPU (i.e. OpenGL VBOs created and loaded to the GPU)</li>
 * <li>Loaded to main memory and GPU</li>
 * </ul>
 * </p>
 * 
 * @see FragmentTexture
 * @see MultiresolutionMesh
 * @see MeshFragment
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RenderMeshFragment implements Comparable<RenderMeshFragment> {

    private static final Logger LOG = LoggerFactory.getLogger( RenderMeshFragment.class );

    private final MeshFragment fragment;

    private MeshFragmentData data;

    // 0: vertex (coordinates) buffer
    // 1: normal buffer
    // 2: triangle buffer
    private int[] glBufferObjectIds;

    private static final int[] glTextureUnitIds = new int[32];

    static {
        int i = 0;
        glTextureUnitIds[i++] = GL.GL_TEXTURE0;
        glTextureUnitIds[i++] = GL.GL_TEXTURE1;
        glTextureUnitIds[i++] = GL.GL_TEXTURE2;
        glTextureUnitIds[i++] = GL.GL_TEXTURE3;
        glTextureUnitIds[i++] = GL.GL_TEXTURE4;
        glTextureUnitIds[i++] = GL.GL_TEXTURE5;
        glTextureUnitIds[i++] = GL.GL_TEXTURE6;
        glTextureUnitIds[i++] = GL.GL_TEXTURE7;
        glTextureUnitIds[i++] = GL.GL_TEXTURE8;
        glTextureUnitIds[i++] = GL.GL_TEXTURE9;
        glTextureUnitIds[i++] = GL.GL_TEXTURE10;
        glTextureUnitIds[i++] = GL.GL_TEXTURE11;
        glTextureUnitIds[i++] = GL.GL_TEXTURE12;
        glTextureUnitIds[i++] = GL.GL_TEXTURE13;
        glTextureUnitIds[i++] = GL.GL_TEXTURE14;
        glTextureUnitIds[i++] = GL.GL_TEXTURE15;
        glTextureUnitIds[i++] = GL.GL_TEXTURE16;
        glTextureUnitIds[i++] = GL.GL_TEXTURE17;
        glTextureUnitIds[i++] = GL.GL_TEXTURE18;
        glTextureUnitIds[i++] = GL.GL_TEXTURE19;
        glTextureUnitIds[i++] = GL.GL_TEXTURE20;
        glTextureUnitIds[i++] = GL.GL_TEXTURE21;
        glTextureUnitIds[i++] = GL.GL_TEXTURE22;
        glTextureUnitIds[i++] = GL.GL_TEXTURE23;
        glTextureUnitIds[i++] = GL.GL_TEXTURE24;
        glTextureUnitIds[i++] = GL.GL_TEXTURE25;
        glTextureUnitIds[i++] = GL.GL_TEXTURE26;
        glTextureUnitIds[i++] = GL.GL_TEXTURE27;
        glTextureUnitIds[i++] = GL.GL_TEXTURE28;
        glTextureUnitIds[i++] = GL.GL_TEXTURE29;
        glTextureUnitIds[i++] = GL.GL_TEXTURE30;
        glTextureUnitIds[i++] = GL.GL_TEXTURE31;
    }

    public RenderMeshFragment( MeshFragment fragment ) {
        this.fragment = fragment;
    }

    public int getId() {
        return fragment.id;
    }

    public float[][] getBBox() {
        return fragment.bbox;
    }

    public float getGeometricError() {
        return fragment.error;
    }

    public MeshFragmentData getData() {
        return data;
    }

    /**
     * Returns whether the geometry data is available in main memory.
     * 
     * @return true, if the geometry data is available in main memory, false otherwise
     */
    public boolean isLoaded() {
        return data != null;
    }

    /**
     * Loads the geometry data into main memory.
     * 
     * @throws IOException
     */
    public void load()
                            throws IOException {
        if ( data == null ) {
            data = fragment.loadData();
        }
    }

    /**
     * Removes the geometry data from main memory (and disables it).
     */
    public void unload() {
        if ( data != null ) {
            data.freeBuffers();
            data = null;
        }
    }

    /**
     * Returns whether fragment is ready for rendering (prepared VBOs).
     * 
     * @return true, if the fragment is ready to be rendered
     */
    public boolean isEnabled() {
        return glBufferObjectIds != null;
    }

    /**
     * Enables the fragment in the given OpenGL context, so it can be rendered.
     * 
     * @param gl
     * @throws IOException
     */
    public void enable( GL gl )
                            throws IOException {

        if ( data == null ) {
            load();
        }
        if ( glBufferObjectIds == null ) {
            glBufferObjectIds = new int[3];
            gl.glGenBuffersARB( 3, glBufferObjectIds, 0 );

            FloatBuffer vertexBuffer = data.getVertices();
            ShortBuffer indexBuffer = (ShortBuffer) data.getTriangles();
            FloatBuffer normalsBuffer = data.getNormals();

            // bind vertex buffer object (vertices)
            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
            gl.glBufferDataARB( GL.GL_ARRAY_BUFFER_ARB, vertexBuffer.capacity() * 4, vertexBuffer,
                                GL.GL_STATIC_DRAW_ARB );

            // bind vertex buffer object (normals)
            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
            gl.glBufferDataARB( GL.GL_ARRAY_BUFFER_ARB, normalsBuffer.capacity() * 4, normalsBuffer,
                                GL.GL_STATIC_DRAW_ARB );

            // bind element buffer object (triangles)
            gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
            gl.glBufferDataARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBuffer.capacity() * 2, indexBuffer,
                                GL.GL_STATIC_DRAW_ARB );
        }
    }

    /**
     * Disables the fragment in the given OpenGL context and frees the associated VBOs and texture object.
     * 
     * @param gl
     */
    public void disable( GL gl ) {
        if ( glBufferObjectIds != null ) {
            int[] bufferObjectIds = this.glBufferObjectIds;
            this.glBufferObjectIds = null;
            gl.glDeleteBuffersARB( bufferObjectIds.length, bufferObjectIds, 0 );
        }
    }

    /**
     * Renders this fragment to the given OpenGL context with optional textures.
     * 
     * @param gl
     * @param textures
     * @throws RuntimeException
     *             if the geometry data is currently not bound to VBOs
     */
    public void render( GL gl, List<FragmentTexture> textures ) {

        if ( !isEnabled() ) {
            throw new RuntimeException( "Cannot render mesh fragment, not enabled." );
        }

        // render with or without texture
        if ( textures != null && textures.size() > 0 ) {
            gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );

            // first texture (uses always-available texture unit 0)
            gl.glActiveTexture( GL.GL_TEXTURE0 );
            gl.glClientActiveTexture( GL.GL_TEXTURE0 );
            gl.glEnable( GL.GL_TEXTURE0 );
            gl.glEnable( GL.GL_TEXTURE_2D );

            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, textures.get( 0 ).getGLVertexCoordBufferId() );
            gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );

            gl.glBindTexture( GL.GL_TEXTURE_2D, textures.get( 0 ).getGLTextureId() );
            gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE );

            // second to last texture
            for ( int i = 1; i < textures.size(); i++ ) {
                int textureUnitId = getTextureUnitId( i );
                gl.glActiveTexture( textureUnitId );
                gl.glClientActiveTexture( textureUnitId );
                gl.glEnable( textureUnitId );
                gl.glEnable( GL.GL_TEXTURE_2D );

                gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, textures.get( i ).getGLVertexCoordBufferId() );
                gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );

                gl.glBindTexture( GL.GL_TEXTURE_2D, textures.get( i ).getGLTextureId() );
                gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);                
            }
        } else {
            gl.glDisable( GL.GL_TEXTURE_2D );
        }

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
        gl.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
        gl.glNormalPointer( GL.GL_FLOAT, 0, 0 );
//        gl.glDisableClientState( GL.GL_NORMAL_ARRAY );
//        gl.glNormal3f( 0, 0, 1 );

        gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
        gl.glDrawElements( GL.GL_TRIANGLES, data.getNumTriangles() * 3, GL.GL_UNSIGNED_SHORT, 0 );

        // reset non-standard OpenGL states
        if ( textures != null && textures.size() > 0 ) {
            gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
            for ( int i = 1; i < textures.size(); i++ ) {
                int textureUnitId = getTextureUnitId( i );
                gl.glActiveTexture( textureUnitId );
                gl.glDisable( GL.GL_TEXTURE_2D );
            }
            gl.glActiveTexture( GL.GL_TEXTURE0 );
        }
    }

    @Override
    public int compareTo( RenderMeshFragment o ) {
        return this.fragment.compareTo( o.fragment );
    }

    private int getTextureUnitId( int i ) {
        if ( i >= 0 && i <= glTextureUnitIds.length - 1 ) {
            return glTextureUnitIds[i];
        }
        throw new IllegalArgumentException( Messages.getMessage( "JOGL_INVALID_TEXTURE_UNIT", i ) );
    }
}
