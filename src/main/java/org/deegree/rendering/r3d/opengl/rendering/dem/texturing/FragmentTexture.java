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

import static org.slf4j.LoggerFactory.getLogger;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.slf4j.Logger;

/**
 * A {@link TextureTile} applied to a {@link RenderMeshFragment}, also wraps OpenGL resources (texture coordinates).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class FragmentTexture {

    private static final Logger LOG = getLogger( FragmentTexture.class );

    private final RenderMeshFragment fragment;

    private final TextureTile texture;

    private PooledByteBuffer texCoords;

    // just wrapped around buffer
    private FloatBuffer texCoordsBuffer;

    private int textureID = -1;

    // 0: texture coordinates buffer
    private int[] glBufferObjectIds;

    // private double xOffset;
    //
    // private double yOffset;

    /**
     * @param fragment
     * @param texture
     */
    public FragmentTexture( RenderMeshFragment fragment, TextureTile texture ) {
        this.fragment = fragment;
        this.texture = texture;
    }

    /**
     * @param fragment
     *            needed to generate the texture coordinates
     * @param texture
     * @param translationVector
     * @param buffer
     */
    public FragmentTexture( RenderMeshFragment fragment, TextureTile texture, double[] translationVector,
                            PooledByteBuffer buffer ) {
        this( fragment, texture );
        this.texCoords = buffer;
        // buffer.getBuffer().order( ByteOrder.nativeOrder() );
        this.texCoordsBuffer = generateTexCoordsBuffer( translationVector );
        // this.xOffset = xOffset;
        // this.yOffset = yOffset;
    }

    /**
     * Generate the texture coordinates for the current fragment and the current texture. The texture coordinates will
     * be put into the given direct buffer.
     * 
     * @param directTexCoordBuffer
     * @param translationVector
     */
    public void generateTextureCoordinates( PooledByteBuffer directTexCoordBuffer, double[] translationVector ) {
        this.texCoords = directTexCoordBuffer;
        this.texCoordsBuffer = generateTexCoordsBuffer( translationVector );
    }

    private FloatBuffer generateTexCoordsBuffer( double[] translationVector ) {

        double minX = -translationVector[0];
        double minY = -translationVector[1];

        double tileXMin = texture.getDataMinX() - minX;
        double tileYMin = texture.getDataMinY() - minY;
        double tileXMax = texture.getDataMaxX() - minX;
        double tileYMax = texture.getDataMaxY() - minY;
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( tileXMin + ", " + tileYMin + ", " + tileXMax + ", " + tileYMax );
        }

        double tileWidth = texture.getDataMaxX() - texture.getDataMinX();
        double tileHeight = texture.getDataMaxY() - texture.getDataMinY();

        // build texture coordinates buffer
        FloatBuffer vertexBuffer = fragment.getData().getVertices().asReadOnlyBuffer();
        vertexBuffer.rewind();

        FloatBuffer texCoordsBuffer = texCoords.getBuffer().asFloatBuffer();
        texCoordsBuffer.rewind();
        int vertices = vertexBuffer.capacity() / 3;
        for ( int i = 0; i < vertices; i++ ) {
            float x = vertexBuffer.get();
            float y = vertexBuffer.get();
            // skip z value (not relevant for texture coordinate generation)
            vertexBuffer.get();
            float texX = -1;
            float texY = -1;
            if ( x >= tileXMin && x <= tileXMax ) {
                texX = (float) ( ( x - tileXMin ) / tileWidth );
            }
            if ( y >= tileYMin && y <= tileYMax ) {
                texY = (float) ( 1 - ( y - tileYMin ) / tileHeight );
            }
            texCoordsBuffer.put( texX );
            texCoordsBuffer.put( texY );
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
    public double getTextureResolution() {
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
        // if ( textureID == -1 ) {
        // throw new RuntimeException();
        // }
        return textureID;
    }

    /**
     * @return the GL-id of the buffer object
     */
    public int getGLVertexCoordBufferId() {
        return glBufferObjectIds == null ? -1 : glBufferObjectIds[0];
    }

    /**
     * Enable this Fragment texture by generating a buffer object in the given context.
     * 
     * @param gl
     *            to generate the buffer object to.
     */
    public void enable( GL gl ) {
        if ( textureID == -1 ) {
            // will return -1 if the texture was not loaded on the gpu
            textureID = texture.enable( gl, this.fragment.getId() );
        }
        // System.out.println( "Fragment bound to: " + fragment.getId() + "| gl texture id: " + textureID );

        if ( textureID != -1 ) {

            if ( glBufferObjectIds == null ) {
                // generateTexCoordsBuffer( xOffset, yOffset );
                glBufferObjectIds = new int[1];
                gl.glGenBuffersARB( 1, glBufferObjectIds, 0 );

                // bind vertex buffer object (vertex coordinates)
                gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
                gl.glBufferDataARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, texCoordsBuffer.capacity() * 4, texCoordsBuffer,
                                    GL.GL_STATIC_DRAW_ARB );
            }
        }
    }

    /**
     * Remove the buffer object from the gpu.
     * 
     * @param gl
     */
    public void disable( GL gl ) {
        if ( textureID != -1 ) {
            // mark the texture as not needed.
            texture.disable( gl, this.fragment.getId() );
            textureID = -1;
        }
        if ( glBufferObjectIds != null ) {
            // remove the buffer objects from the context.
            int[] bufferObjectIds = this.glBufferObjectIds;
            this.glBufferObjectIds = null;
            gl.glDeleteBuffersARB( bufferObjectIds.length, bufferObjectIds, 0 );
        }
    }

    /**
     * Unload
     */
    public void unload() {
        if ( glBufferObjectIds == null && textureID == -1 ) {
            texCoords.free();
        } else {
            LOG.warn( "Trying to free a buffer which is still on the gpu, this should not happen, because no texture coordinates would be left, ignoring request, and hope for the best. " );
            if ( LOG.isDebugEnabled() ) {
                Thread.dumpStack();
            }
        }
    }

    /**
     * Clears up all data in this fragment texture, this disables the texture and frees up all OpenGL references, as
     * well as releasing the direct texture coordinates buffer.
     * 
     * @param gl
     *            openGL context to which the texture was bounded to.
     */
    public void clearAll( GL gl ) {
        LOG.debug( "Cleaning up all data in this Fragmenttexture. " );
        texture.disable( gl, this.fragment.getId() );
        texture.dispose();
        textureID = -1;

        if ( glBufferObjectIds != null ) {
            int[] bufferObjectIds = this.glBufferObjectIds;
            this.glBufferObjectIds = null;
            gl.glDeleteBuffersARB( bufferObjectIds.length, bufferObjectIds, 0 );
        }

        texCoords.free();

    }

    /**
     * 
     * @return true if this Fragment has a buffer object id and a texture id.
     */
    public boolean isEnabled() {
        return glBufferObjectIds != null && textureID != -1;
    }

    /**
     * @return true if caching was enabled for this texture.
     */
    public boolean cachingEnabled() {
        return this.texture.enableCaching();
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof FragmentTexture ) {
            final FragmentTexture that = (FragmentTexture) other;
            return this.fragment.getId() == that.fragment.getId() && this.texture.equals( that.texture );
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
        code = code * 37 + this.fragment.getId();
        long tmp = texture.hashCode();
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    /**
     * @return the id of the fragment
     */
    public Integer getId() {
        return hashCode();
    }
}
