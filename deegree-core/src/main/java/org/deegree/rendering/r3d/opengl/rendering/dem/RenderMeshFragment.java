//$HeadURL$
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
package org.deegree.rendering.r3d.opengl.rendering.dem;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;

import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.multiresolution.MeshFragmentData;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.deegree.rendering.r3d.opengl.JOGLUtils;
import org.deegree.rendering.r3d.opengl.rendering.ShaderProgram;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.FragmentTexture;
import org.slf4j.Logger;

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

    private static final Logger LOG = getLogger( RenderMeshFragment.class );

    private final MeshFragment fragment;

    private MeshFragmentData data;

    private final String LOCK = "lock";

    // 0: vertex (coordinates) buffer
    // 1: normal buffer
    // 2: triangle buffer
    private int[] glBufferObjectIds;

    private float[][] triangle;

    /**
     * @param fragment
     *            a MultiresolutionMesh fragment to create a renderable fragment from.
     */
    public RenderMeshFragment( MeshFragment fragment ) {
        this.fragment = fragment;
    }

    /**
     * @return the macro triangle fragment id.
     */
    public int getId() {
        return fragment.id;
    }

    /**
     * @return the bbox of the MultiresolutionMesh fragment.
     */
    public float[][] getBBox() {
        return fragment.bbox;
    }

    /**
     * 
     * @return the geometric error of the MultiresolutionMesh fragment.
     */
    public float getGeometricError() {
        return fragment.error;
    }

    /**
     * 
     * @return the actual data of the MultiresolutionMesh fragment.
     */
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
        synchronized ( LOCK ) {
            if ( data == null ) {
                data = fragment.loadData();
            }
        }
    }

    /**
     * Removes the geometry data from main memory (and disables it).
     */
    public void unload() {
        synchronized ( LOCK ) {
            if ( data != null ) {
                data.freeBuffers();
                data = null;
            }
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
     * Render the fragment data.
     * 
     * @param gl
     */
    public void render( GL gl ) {
        if ( !isEnabled() ) {
            LOG.warn( "Current fragment is not enabled, discarding it." );
            return;
        }
        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
        gl.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
        gl.glNormalPointer( GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
        gl.glDrawElements( GL.GL_TRIANGLES, data.getNumTriangles() * 3, GL.GL_UNSIGNED_SHORT, 0 );
    }

    /**
     * Renders this fragment to the given OpenGL context with given (optional) textures.
     * 
     * @param gl
     *            context to render in.
     * @param textures
     *            may be empty, or null
     * @param shaderProgram
     *            the shader program containing composite texturing code.
     * @throws RuntimeException
     *             if the geometry data is currently not bound to VBOs
     */
    public void render( GL gl, List<FragmentTexture> textures, ShaderProgram shaderProgram ) {

        if ( !isEnabled() ) {
            LOG.warn( "Current mesh fragment is not enabled, discarding it." );
            return;
        }
        if ( shaderProgram != null ) {
            enableAndRenderTextures( gl, textures, shaderProgram );
        }

        render( gl );

        if ( shaderProgram != null ) {
            disableTextureStates( gl, textures, shaderProgram );
        }
    }

    /**
     * @param textures
     * @param fragShaderProgramId
     */
    private void enableAndRenderTextures( GL gl, List<FragmentTexture> textures, ShaderProgram shaderProgram ) {
        // render with or without texture
        if ( textures != null && textures.size() > 0 && textures.get( 0 ) != null ) {
            FragmentTexture texture = textures.get( 0 );
            int glVCB = texture.getGLVertexCoordBufferId();
            int tId = texture.getGLTextureId();

            if ( glVCB != -1 && tId != -1 ) {
                // first texture (uses always-available texture unit 0)
                gl.glClientActiveTexture( GL.GL_TEXTURE0 );
                gl.glActiveTexture( GL.GL_TEXTURE0 );
                gl.glEnable( GL.GL_TEXTURE_2D );
                gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
                gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glVCB );
                gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );
                gl.glBindTexture( GL.GL_TEXTURE_2D, tId );
                gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
                gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );

                // second to last texture
                for ( int i = 1; i < textures.size(); i++ ) {
                    texture = textures.get( i );
                    if ( texture != null ) {
                        glVCB = texture.getGLVertexCoordBufferId();
                        tId = texture.getGLTextureId();
                        if ( glVCB != -1 && tId != -1 ) {
                            int textureUnitId = JOGLUtils.getTextureUnitConst( i );
                            gl.glClientActiveTexture( textureUnitId );
                            gl.glActiveTexture( textureUnitId );
                            gl.glEnable( GL.GL_TEXTURE_2D );
                            gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
                            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glVCB );
                            gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );

                            gl.glBindTexture( GL.GL_TEXTURE_2D, tId );
                            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
                            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );

                        } else {
                            LOG.warn( "No texture id or no texture coordinates for texture(" + i
                                      + "), ignoring this texture." );
                        }
                    }

                }
                // activate shader and set texSamplers, the shaderprogram is not null.
                shaderProgram.useProgram( gl );
                for ( int i = 0; i < textures.size(); i++ ) {
                    if ( textures.get( i ) != null ) {
                        int texSampler = gl.glGetUniformLocation( shaderProgram.getOGLId(), "tex" + i );
                        gl.glUniform1i( texSampler, i );
                    }

                }
            } else {
                LOG.warn( "No texture id or no texture coordinates for texture(0), this fragments textures." );
            }
        }
    }

    /**
     * @param gl
     * @param textures
     * @param shaderProgram
     */
    private void disableTextureStates( GL gl, List<FragmentTexture> textures, ShaderProgram shaderProgram ) {
        // reset non-standard OpenGL states
        if ( textures != null && textures.size() > 0 ) {
            for ( int i = textures.size() - 1; i >= 0; --i ) {
                int textureUnitId = JOGLUtils.getTextureUnitConst( i );
                gl.glClientActiveTexture( textureUnitId );
                gl.glActiveTexture( textureUnitId );
                gl.glDisable( GL.GL_TEXTURE_2D );
                gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
            }
            // gl.glActiveTexture( GL.GL_TEXTURE0 );
            // gl.glClientActiveTexture( GL.GL_TEXTURE0 );
        }
        // rb: shader program can not be null.
        shaderProgram.disable( gl );
    }

    @Override
    public int compareTo( RenderMeshFragment o ) {
        return this.fragment.compareTo( o.fragment );
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof RenderMeshFragment ) {
            final RenderMeshFragment that = (RenderMeshFragment) other;
            return this.fragment.id == that.fragment.id;
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long code = 32452843;
        code = code * 37 + this.fragment.id;
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    /**
     * @return size in bytes of this fragment.
     */
    public int size() {
        return fragment.size();
    }

    /**
     * @return true if the used direct buffer pool can allocate enough direct memory for the given data.
     */
    public boolean canAllocateEnoughMemory() {
        return this.fragment.canAllocateEnoughMemory();
    }

    /**
     * @return the vertices of the macro triangle of this Mesh fragment. The returned array will have size [3][2], never
     *         <code>null</code>.
     */
    public float[][] getTrianglePoints() {
        synchronized ( LOCK ) {
            if ( this.triangle == null ) {
                triangle = new float[3][2];

                // really dumb (and slow), just used for debugging
                float[][] bbox = getBBox();

                float[] v0 = new float[] { bbox[0][0], bbox[0][1] };
                float[] v1 = new float[] { bbox[1][0], bbox[0][1] };
                float[] v2 = new float[] { bbox[1][0], bbox[1][1] };
                float[] v3 = new float[] { bbox[0][0], bbox[1][1] };

                boolean v0found = false;
                boolean v1found = false;
                boolean v2found = false;
                boolean v3found = false;

                try {
                    load();
                } catch ( IOException e ) {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
                    } else {
                        LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
                    }
                }
                if ( !isLoaded() ) {
                    LOG.warn( "Could not determine macrotriangle corners, setting values to min." );
                    triangle[0] = triangle[1] = triangle[2] = Arrays.copyOf( bbox[0], 2 );
                }
                FloatBuffer vertices = getData().getVertices().asReadOnlyBuffer();
                vertices.rewind();
                while ( vertices.hasRemaining() ) {
                    float x = vertices.get();
                    float y = vertices.get();
                    vertices.get();
                    if ( !v0found ) {
                        v0found = ( Math.abs( v0[0] - x ) < 1E-10 ) && ( Math.abs( v0[1] - y ) < 1E-10 );
                    }
                    if ( !v1found ) {
                        v1found = ( Math.abs( v1[0] - x ) < 1E-10 ) && ( Math.abs( v1[1] - y ) < 1E-10 );
                    }
                    if ( !v2found ) {
                        v2found = ( Math.abs( v2[0] - x ) < 1E-10 ) && ( Math.abs( v2[1] - y ) < 1E-10 );
                    }
                    if ( !v3found ) {
                        v3found = ( Math.abs( v3[0] - x ) < 1E-10 ) && ( Math.abs( v3[1] - y ) < 1E-10 );
                    }
                }
                if ( v0found && v1found ) {
                    triangle[0] = Arrays.copyOf( v0, 2 );
                    triangle[1] = Arrays.copyOf( v1, 2 );
                    if ( v2found ) {
                        triangle[2] = Arrays.copyOf( v2, 2 );
                    } else if ( v3found ) {
                        triangle[2] = Arrays.copyOf( v3, 2 );
                    } else {
                        triangle[2] = new float[] { ( v1[0] - v0[0] ) / 2.0f + v0[0], v3[1] };
                    }
                } else if ( v2found && v3found ) {
                    triangle[0] = Arrays.copyOf( v2, 2 );
                    triangle[1] = Arrays.copyOf( v3, 2 );
                    if ( v0found ) {
                        triangle[2] = Arrays.copyOf( v0, 2 );
                    } else if ( v1found ) {
                        triangle[2] = Arrays.copyOf( v1, 2 );
                    } else {
                        triangle[2] = new float[] { ( v3[0] - v2[0] ) / 2.0f + v2[0], v0[1] };
                    }
                } else if ( v1found && v2found ) {
                    triangle[0] = Arrays.copyOf( v1, 2 );
                    triangle[1] = Arrays.copyOf( v2, 2 );
                    if ( v0found ) {
                        triangle[2] = Arrays.copyOf( v0, 2 );
                    } else if ( v3found ) {
                        triangle[2] = Arrays.copyOf( v3, 2 );
                    } else {
                        triangle[2] = new float[] { v0[0], ( v2[1] - v1[1] ) / 2.0f + v1[1] };
                    }
                } else if ( v3found && v0found ) {
                    triangle[0] = Arrays.copyOf( v3, 2 );
                    triangle[1] = Arrays.copyOf( v0, 2 );
                    if ( v1found ) {
                        triangle[2] = Arrays.copyOf( v1, 2 );
                    } else if ( v2found ) {
                        triangle[2] = Arrays.copyOf( v2, 2 );
                    } else {
                        triangle[2] = new float[] { v1[0], ( v3[1] - v0[1] ) / 2.0f + v0[1] };
                    }
                } else {
                    LOG.warn( "Could not determine macrotriangle corners, setting values to min." );
                    triangle[0] = triangle[1] = triangle[2] = Arrays.copyOf( v0, 2 );
                }
            }
        }
        return this.triangle;

    }
}
