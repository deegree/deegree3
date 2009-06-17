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

package org.deegree.rendering.r3d.opengl.rendering.model.geometry;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.rendering.r3d.model.geometry.SimpleGeometryStyle;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>RenderableTexturedGeometry</code> is a {@link RenderableGeometry} which has texture coordinates assigned
 * to each vertex as well. Currently only one texture is supported.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class RenderableTexturedGeometry extends RenderableGeometry {

    /**
     *
     */
    private static final long serialVersionUID = 2809495291716138222L;

    private final transient static Logger LOG = LoggerFactory.getLogger( RenderableTexturedGeometry.class );

    private transient String texture;

    private transient FloatBuffer textureBuffer = null;

    private transient int texturePosition;

    private transient int textureOrdinatesCount;

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param style
     * @param texture
     *            to use
     * @param textureCoordinates
     *            of this data
     * @param useDirectBuffers
     *            to use direct buffers instead of heap buffers.
     */
    public RenderableTexturedGeometry( float[] vertices, int openGLType, float[] vertexNormals,
                                       SimpleGeometryStyle style, String texture, float[] textureCoordinates,
                                       boolean useDirectBuffers ) {
        super( vertices, openGLType, vertexNormals, style, useDirectBuffers );
        this.texture = texture;
        loadTextureCoordinates( textureCoordinates );
    }

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param style
     * @param texture
     *            to use
     * @param textureCoordinates
     *            of this data
     */
    public RenderableTexturedGeometry( FloatBuffer vertices, int openGLType, FloatBuffer vertexNormals,
                                       SimpleGeometryStyle style, String texture, FloatBuffer textureCoordinates ) {
        super( vertices, openGLType, vertexNormals, style );
        this.texture = texture;
        this.textureBuffer = textureCoordinates;
        textureOrdinatesCount = textureCoordinates.capacity();
        texturePosition = -1;
    }

    /**
     *
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param texture
     * @param textureCoordinates
     * @param useDirectBuffers
     *            to use direct buffers instead of heap buffers.
     */
    public RenderableTexturedGeometry( float[] vertices, int openGLType, float[] vertexNormals, String texture,
                                       float[] textureCoordinates, boolean useDirectBuffers ) {
        super( vertices, openGLType, vertexNormals, useDirectBuffers );
        this.texture = texture;
        loadTextureCoordinates( textureCoordinates );

    }

    /**
     *
     * @param coordPosition
     *            in the direct vertex buffer
     * @param vertexCount
     *            the number of vertices
     * @param openGLType
     *            the opengl type
     * @param normalPosition
     *            position in the direct normal buffer
     * @param style
     *            to be applied
     * @param textureID
     *            of the texture to use.
     * @param texturePosition
     *            in the direct texture buffer.
     */
    public RenderableTexturedGeometry( int coordPosition, int vertexCount, int openGLType, int normalPosition,
                                       SimpleGeometryStyle style, String textureID, int texturePosition ) {
        super( coordPosition, vertexCount, openGLType, normalPosition, style );
        this.texture = textureID;
        this.texturePosition = texturePosition;
        this.textureOrdinatesCount = vertexCount * 2;
    }

    @Override
    protected void enableArrays( RenderContext glRenderContext, DirectGeometryBuffer geomBuffer ) {
        super.enableArrays( glRenderContext, geomBuffer );

        if ( texturePosition >= 0 && textureBuffer == null && geomBuffer != null ) {
            textureBuffer = geomBuffer.getTextureCoordinates( texturePosition, textureOrdinatesCount );
            if ( textureBuffer == null ) {
                texturePosition = -1;
            }
        }
        if ( textureBuffer != null ) {
            glRenderContext.getContext().glEnable( GL.GL_TEXTURE_2D );
            TexturePool.loadTexture( glRenderContext, texture );
            glRenderContext.getContext().glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
            glRenderContext.getContext().glTexCoordPointer( 2, GL.GL_FLOAT, 0, textureBuffer );
        }

    }

    @Override
    public void disableArrays( RenderContext glRenderContext ) {
        super.disableArrays( glRenderContext );
        LOG.trace( "Disabling array state and texture 2d" );
        glRenderContext.getContext().glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        glRenderContext.getContext().glDisable( GL.GL_TEXTURE_2D );
    }

    /**
     * @return the texture
     */
    public final String getTexture() {
        return texture;
    }

    /**
     * @param texture
     *            the texture to set
     */
    public final void setTexture( String texture ) {
        this.texture = texture;
    }

    /**
     * @return the textureCoordinates
     */
    public final FloatBuffer getTextureCoordinates() {
        return textureBuffer;
    }

    /**
     * @param textureCoordinates
     *            the textureCoordinates to set
     */
    public final void setTextureCoordinates( float[] textureCoordinates ) {
        loadTextureCoordinates( textureCoordinates );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\ntextureID: " ).append( texture );
        if ( textureBuffer != null && textureBuffer.capacity() > 0 ) {
            sb.append( "\ntextureCoordinates:\n" );
            for ( int i = 0; ( i + 1 ) < textureBuffer.capacity(); i += 2 ) {
                sb.append( textureBuffer.get( i ) ).append( "," ).append( textureBuffer.get( i + 1 ) ).append( "\n" );
            }
        }
        return sb.toString();
    }

    /**
     * Load the texture coordinates from given float array
     *
     * @param textureCoordinates
     */
    private void loadTextureCoordinates( float[] textureCoordinates ) {
        if ( textureCoordinates == null || textureCoordinates.length == 0 ) {
            throw new IllegalArgumentException(
                                                "A Renderable Textured Geometry must have texture coordinates to work with (the textureCoordinates array may not be null or empty). " );
        }
        if ( textureCoordinates.length / 2 != getVertexCount() ) {
            throw new IllegalArgumentException( "The number of texture coordinates ("
                                                + ( textureCoordinates.length / 2 )
                                                + ") must equal the number of vertices (" + getVertexCount() + ")." );
        }
        if ( super.useDirectBuffers() ) {
            textureBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( textureCoordinates ) );
        } else {
            textureBuffer = FloatBuffer.wrap( textureCoordinates );
        }
        textureOrdinatesCount = textureCoordinates.length;
    }

    /**
     * @return the bytes this geometry occupies
     */
    @Override
    public long sizeOf() {
        long localSize = super.sizeOf();
        localSize += AllocatedHeapMemory.sizeOfString( texture, true, true );
        localSize += AllocatedHeapMemory.sizeOfBuffer( textureBuffer, true );
        return localSize;
    }

    @Override
    public int getTextureOrdinateCount() {
        return textureOrdinatesCount;
    }

    /**
     * @return the texturePosition
     */
    public final int getTexturePosition() {
        return texturePosition;
    }
}
