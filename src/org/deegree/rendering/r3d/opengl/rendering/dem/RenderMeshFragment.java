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

    public RenderMeshFragment( MeshFragment fragment ) {
        this.fragment = fragment;
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
    public void render( GL gl, List<FragmentTexture> textures) {

        if ( !isEnabled() ) {
            throw new RuntimeException( "Cannot render mesh fragment, not enabled." );
        }

        // render with or without texture
        if ( textures != null && textures.size() > 0) {
            // TODO use other textures
            gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
            
            
            gl.glActiveTexture( GL.GL_TEXTURE0);
            gl.glClientActiveTexture( GL.GL_TEXTURE0);
            gl.glEnable( GL.GL_TEXTURE_2D );

            FragmentTexture texture = textures.get( 0 );
            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, texture.getGLVertexCoordBufferId() );
            gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );

            gl.glEnable( GL.GL_TEXTURE0 );
            gl.glBindTexture( GL.GL_TEXTURE_2D, texture.getGLTextureId() );
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
            
            gl.glActiveTexture( GL.GL_TEXTURE1);
            gl.glClientActiveTexture( GL.GL_TEXTURE1);
            gl.glEnable( GL.GL_TEXTURE_2D );

            texture = textures.get( 1 );
            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, texture.getGLVertexCoordBufferId() );
            gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );

            gl.glEnable( GL.GL_TEXTURE1 );
            gl.glBindTexture( GL.GL_TEXTURE_2D, texture.getGLTextureId() );
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);            
        } else {
            gl.glDisable( GL.GL_TEXTURE_2D );
            gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        }

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
        gl.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
        gl.glNormalPointer( GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
        gl.glDrawElements( GL.GL_TRIANGLES, data.getNumTriangles() * 3, GL.GL_UNSIGNED_SHORT, 0 );        
    }

    @Override
    public int compareTo( RenderMeshFragment o ) {
        return this.fragment.compareTo( o.fragment );
    }

    public int getId() {
        return fragment.id;
    }
}
