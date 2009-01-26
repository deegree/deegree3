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

package org.deegree.rendering.r3d.opengl.rendering;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.vecmath.Vector3f;

import org.deegree.rendering.r3d.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.tesselation.Tesselator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>RenderableGeometry</code> class uses VertexArrays to render the coordinates of it's geometry in an openGL
 * context. For this to work the coordinates are expected to be organized as defined by the given openGLType.
 * <p>
 * Normally you might want to use a set of geometries for a single object (for example to create walls of a house). In
 * this case the easiest way to create a {@link RenderableQualityModel} (containing {@link RenderableGeometry}(ies)) is
 * the usage of the {@link Tesselator}. The {@link Tesselator} class can be used to create a sole
 * {@link RenderableGeometry} as well.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RenderableGeometry extends SimpleAccessGeometry implements Renderable {

    private final transient static Logger LOG = LoggerFactory.getLogger( RenderableGeometry.class );

    /**
     * 
     */
    private static final long serialVersionUID = -7188698491925826649L;

    /**
     * an array of integers which can be used to store gl pointers (vertexlists, displaylist etc).
     */
    protected transient int[] glBufferIDs;

    // have a look at GL class.
    private int openGLType;

    // 3D, same length as renderableGeometry
    private float[] vertexNormals = null;

    // 4D (RGBA)
    private byte[] vertexColors;

    private boolean hasNormals;

    private boolean hasColors;

    private transient FloatBuffer coordBuffer = null;

    private transient FloatBuffer normalBuffer = null;

    private transient ByteBuffer colorBuffer = null;

    /**
     * @param geometry
     * @param openGLType
     * @param vertexNormals
     * @param vertexColors
     * @param specularColor
     * @param ambientColor
     * @param diffuseColor
     * @param emmisiveColor
     * @param shininess
     */
    public RenderableGeometry( float[] geometry, int openGLType, float[] vertexNormals, byte[] vertexColors,
                               int specularColor, int ambientColor, int diffuseColor, int emmisiveColor, float shininess ) {
        super( geometry, specularColor, ambientColor, diffuseColor, emmisiveColor, shininess );
        this.openGLType = openGLType;
        switch ( openGLType ) {
        case GL.GL_TRIANGLE_FAN:
        case GL.GL_TRIANGLES:
        case GL.GL_TRIANGLE_STRIP:
        case GL.GL_QUADS:
            break;
        default:
            throw new UnsupportedOperationException( "Unknown opengl type: " + openGLType );
        }
        this.vertexNormals = vertexNormals;
        this.vertexColors = vertexColors;
        this.hasNormals = ( vertexNormals != null && vertexNormals.length > 0 );
        this.hasColors = ( vertexColors != null && vertexColors.length > 0 );
    }

    /**
     * @param geometry
     * @param openGLType
     */
    public RenderableGeometry( float[] geometry, int openGLType ) {
        this( geometry, openGLType, null, null, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 1 );
    }

    @Override
    public void render( GL context, Vector3f eye ) {
        enableArrays( context );
        context.glDrawArrays( openGLType, 0, getVertexCount() );
        disableArrays( context );
    }

    /**
     * Load the float buffers and enable the client state.
     * 
     * @param context
     */
    protected void enableArrays( GL context ) {

        if ( coordinates != null ) {
            if ( coordBuffer == null ) {
                LOG.trace( "Loading coordinates into float buffer" );
                coordBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( getGeometry() ) );
            }
            LOG.trace( "Loading coordbuffer" );
            context.glVertexPointer( 3, GL.GL_FLOAT, 0, coordBuffer );

            if ( hasNormals ) {
                if ( normalBuffer == null ) {
                    LOG.trace( "Loading normals into float buffer" );
                    normalBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( vertexNormals ) );
                }
                LOG.trace( "Loading normal buffer" );
                context.glEnableClientState( GL.GL_NORMAL_ARRAY );
                context.glNormalPointer( GL.GL_FLOAT, 0, normalBuffer );
            }
            if ( hasColors ) {
                if ( colorBuffer == null ) {
                    LOG.trace( "Loading colors into byte buffer" );
                    colorBuffer = BufferUtil.copyByteBuffer( ByteBuffer.wrap( vertexColors ) );
                }
                LOG.trace( "Loading color buffer" );
                context.glEnableClientState( GL.GL_COLOR_ARRAY );
                context.glColorPointer( 4, GL.GL_BYTE, 0, colorBuffer );
            }
        }
    }

    /**
     * @param context
     */
    public void disableArrays( GL context ) {
        LOG.trace( "Disabling client states: normal and color" );
        context.glDisableClientState( GL.GL_NORMAL_ARRAY );
        context.glDisableClientState( GL.GL_COLOR_ARRAY );
    }

    /**
     * @param geometry
     *            the originalGeometry to set
     * @param openGLType
     */
    public final void setGeometry( float[] geometry, int openGLType ) {
        setGeometry( geometry );
        this.openGLType = openGLType;
    }

    /**
     * @return the vertexNormals
     */
    public final float[] getVertexNormals() {
        return vertexNormals;
    }

    /**
     * @param vertexNormals
     *            the vertexNormals to set
     */
    public final void setVertexNormals( float[] vertexNormals ) {
        this.vertexNormals = vertexNormals;
    }

    /**
     * @return the vertexColors
     */
    public final byte[] getVertexColors() {
        return vertexColors;
    }

    /**
     * @param vertexColors
     *            the vertexColors to set
     */
    public final void setVertexColors( byte[] vertexColors ) {
        this.vertexColors = vertexColors;
    }

    /**
     * @return the openGLType
     */
    public final int getOpenGLType() {
        return openGLType;
    }
}
