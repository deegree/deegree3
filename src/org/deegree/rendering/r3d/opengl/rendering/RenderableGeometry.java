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

import static org.deegree.rendering.r3d.opengl.rendering.utils.BufferIO.readByteBufferFromStream;
import static org.deegree.rendering.r3d.opengl.rendering.utils.BufferIO.readFloatBufferFromStream;
import static org.deegree.rendering.r3d.opengl.rendering.utils.BufferIO.writeBufferToStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.AllocatedHeapMemory;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.geometry.SimpleGeometryStyle;
import org.deegree.rendering.r3d.opengl.rendering.utils.BufferIO;
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
public class RenderableGeometry extends SimpleGeometryStyle implements RenderableQualityModelPart {

    /**
     * 
     */
    private static final long serialVersionUID = -2746400840307665734L;

    private final static Logger LOG = LoggerFactory.getLogger( RenderableGeometry.class );

    // have a look at GL class.
    private transient int openGLType;

    private transient boolean hasNormals;

    private transient boolean hasColors;

    private transient FloatBuffer coordBuffer = null;

    // 3D, same length as renderableGeometry
    private transient FloatBuffer normalBuffer = null;

    // 4D (RGBA)
    private transient ByteBuffer colorBuffer = null;

    private transient int vertexCount;

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param vertexColors
     * @param specularColor
     * @param ambientColor
     * @param diffuseColor
     * @param emmisiveColor
     * @param shininess
     */
    public RenderableGeometry( float[] vertices, int openGLType, float[] vertexNormals, byte[] vertexColors,
                               int specularColor, int ambientColor, int diffuseColor, int emmisiveColor, float shininess ) {
        super( specularColor, ambientColor, diffuseColor, emmisiveColor, shininess );

        this.vertexCount = loadVertexBuffer( vertices );
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

        setVertexNormals( vertexNormals );
        setVertexColors( vertexColors );
    }

    private int loadVertexBuffer( float[] vertices ) {
        if ( vertices == null || vertices.length == 0 ) {
            throw new IllegalArgumentException(
                                                "A RenderableGeometry must have vertices to work with (the vertices array may not be null or empty). " );
        }

        coordBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( vertices ) );
        return vertices.length / 3;
    }

    /**
     * @param vertices
     * @param openGLType
     */
    public RenderableGeometry( float[] vertices, int openGLType ) {
        this( vertices, openGLType, null, null, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 1 );
    }

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param vertexColors
     */
    public RenderableGeometry( float[] vertices, int openGLType, float[] vertexNormals, byte[] vertexColors ) {
        this( vertices, openGLType, vertexNormals, vertexColors, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 1 );
    }

    @Override
    public void render( GL context, ViewParams params ) {
        enableArrays( context );
        context.glDrawArrays( openGLType, 0, vertexCount );
        disableArrays( context );
    }

    /**
     * Load the float buffers and enable the client state.
     * 
     * @param context
     */
    protected void enableArrays( GL context ) {

        LOG.trace( "Loading coordbuffer" );
        context.glVertexPointer( 3, GL.GL_FLOAT, 0, coordBuffer );

        if ( hasNormals ) {
            LOG.trace( "Loading normal buffer" );
            context.glEnableClientState( GL.GL_NORMAL_ARRAY );
            context.glNormalPointer( GL.GL_FLOAT, 0, normalBuffer );
        }
        if ( hasColors ) {
            LOG.trace( "Loading color buffer" );
            context.glEnableClientState( GL.GL_COLOR_ARRAY );
            context.glColorPointer( 4, GL.GL_BYTE, 0, colorBuffer );
        }
    }

    /**
     * @param context
     */
    public void disableArrays( GL context ) {
        LOG.trace( "Disabling client states: normal and color" );
        context.glDisableClientState( GL.GL_NORMAL_ARRAY );
        // context.glDisableClientState( GL.GL_COLOR_ARRAY );
    }

    /**
     * @param vertices
     *            the vertices to set
     * @param openGLType
     */
    public final void setVertices( float[] vertices, int openGLType ) {
        vertexCount = loadVertexBuffer( vertices );
        this.openGLType = openGLType;
    }

    /**
     * @return the vertexNormals
     */
    public final FloatBuffer getVertexNormals() {
        return normalBuffer;
    }

    /**
     * @param vertexNormals
     *            the vertexNormals to set
     */
    @SuppressWarnings("null")
    public final void setVertexNormals( float[] vertexNormals ) {
        this.hasNormals = ( vertexNormals != null && vertexNormals.length > 0 );
        if ( hasNormals ) {
            if ( vertexNormals.length % 3 != 0 ) {
                throw new IllegalArgumentException( "The number of vertex normals(" + ( vertexNormals.length                                                   )
                                                    + ") must be kongruent to 3." );
            } else if ( ( vertexNormals.length / 3 ) != vertexCount ) {
                throw new IllegalArgumentException( "The number of normals (" + ( vertexNormals.length / 3 )
                                                    + ") must equal the number of vertices (" + vertexCount + ")." );
            }
            this.normalBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( vertexNormals ) );
        }
    }

    /**
     * @return the vertexColors
     */
    public final ByteBuffer getVertexColors() {
        return colorBuffer;
    }

    /**
     * @param vertexColors
     *            the vertexColors to set
     */
    @SuppressWarnings("null")
    public final void setVertexColors( byte[] vertexColors ) {
        this.hasColors = ( vertexColors != null && vertexColors.length > 0 );
        if ( hasColors ) {
            if ( vertexColors.length % 4 != 0 ) {
                throw new IllegalArgumentException( "The number of vertex colors(" + ( vertexColors.length                                                       )
                                                    + ") must be kongruent to 4." );
            } else if ( ( vertexColors.length / 4 ) != vertexCount ) {
                throw new IllegalArgumentException( "The number of vertex colors(" + ( vertexColors.length / 4 )
                                                    + ") must equal the number of vertices (" + vertexCount + ")." );
            }
            this.colorBuffer = BufferUtil.copyByteBuffer( ByteBuffer.wrap( vertexColors ) );
        }
    }

    /**
     * @return the openGLType
     */
    public final int getOpenGLType() {
        return openGLType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\nopenGL type: " );
        switch ( openGLType ) {
        case GL.GL_TRIANGLE_FAN:
            sb.append( "Triangle fan" );
            break;
        case GL.GL_TRIANGLES:
            sb.append( "Triangles" );
            break;
        case GL.GL_TRIANGLE_STRIP:
            sb.append( "Triangle strip" );
            break;
        case GL.GL_QUADS:
            sb.append( "Quads" );
            break;
        }
        // sb.append( coordBuffer.toString() );
        sb.append( "\nvertices(" ).append( vertexCount ).append( "):\n" );
        int vertex = 1;
        for ( int i = 0; i + 2 < coordBuffer.capacity(); i += 3 ) {
            sb.append( vertex++ ).append( ": " );
            sb.append( coordBuffer.get( i ) ).append( "," );
            sb.append( coordBuffer.get( i + 1 ) ).append( "," );
            sb.append( coordBuffer.get( i + 2 ) );
            if ( i + 5 < coordBuffer.capacity() ) {
                sb.append( "\n" );
            }
        }
        if ( hasNormals ) {
            vertex = 1;
            sb.append( "\nnormals:\n" );
            for ( int i = 0; i + 2 < normalBuffer.capacity(); i += 3 ) {
                sb.append( vertex++ ).append( ": " );
                sb.append( normalBuffer.get( i ) ).append( "," );
                sb.append( normalBuffer.get( i + 1 ) ).append( "," );
                sb.append( normalBuffer.get( i + 2 ) );
                if ( i + 5 < normalBuffer.capacity() ) {
                    sb.append( "\n" );
                }
            }
        }

        if ( hasColors ) {
            sb.append( "\ncolors:\n" );
            vertex = 1;
            for ( int i = 0; i + 3 < colorBuffer.capacity(); i += 4 ) {
                sb.append( vertex++ ).append( ": " );
                sb.append( colorBuffer.get( i ) ).append( "," );
                sb.append( colorBuffer.get( i + 1 ) ).append( "," );
                sb.append( colorBuffer.get( i + 2 ) ).append( "," );
                sb.append( colorBuffer.get( i + 3 ) );
                if ( i + 7 < colorBuffer.capacity() ) {
                    sb.append( "\n" );
                }
            }
        }
        return sb.toString();
    }

    /**
     * Method called while serializing this object
     * 
     * @param out
     *            to write to.
     * @throws IOException
     */
    private void writeObject( java.io.ObjectOutputStream out )
                            throws IOException {
        LOG.trace( "Serializing to object stream" );
        writeBufferToStream( coordBuffer, out );
        writeBufferToStream( normalBuffer, out );
        writeBufferToStream( colorBuffer, out );

        out.writeInt( openGLType );

    }

    /**
     * Method called while de-serializing (instancing) this object.
     * 
     * @param in
     *            to create the methods from.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject( java.io.ObjectInputStream in )
                            throws IOException {
        LOG.trace( "Deserializing from object stream" );
        // float[] t = (float[]) in.readObject();
        coordBuffer = BufferIO.readFloatBufferFromStream( in );
        if ( coordBuffer == null ) {
            LOG.error( "An error occurred while de-serializing a Renderable Geometry, vertex buffer may not be null." );
            throw new IOException(
                                   "An error occurred while de-serializing a Renderable Geometry, vertex buffer may not be null." );
        }

        vertexCount = coordBuffer.capacity() / 3;
        normalBuffer = readFloatBufferFromStream( in );
        hasNormals = ( normalBuffer != null );
        colorBuffer = readByteBufferFromStream( in );
        hasColors = ( colorBuffer != null );

        openGLType = in.readInt();
    }

    /**
     * @return the bytes this geometry occupies
     */
    @Override
    public long sizeOf() {
        long localSize = super.sizeOf();
        // coordbuffer
        localSize += AllocatedHeapMemory.sizeOfBuffer( coordBuffer, true );
        // colorbuffer
        localSize += AllocatedHeapMemory.sizeOfBuffer( colorBuffer, true );
        // normal buffer
        localSize += AllocatedHeapMemory.sizeOfBuffer( normalBuffer, true );
        // hasNormals
        localSize += AllocatedHeapMemory.INT_SIZE;
        // hasColors
        localSize += AllocatedHeapMemory.INT_SIZE;
        // openGLType
        localSize += AllocatedHeapMemory.INT_SIZE;
        // vertexCount
        localSize += AllocatedHeapMemory.INT_SIZE;
        return localSize;
    }

    /**
     * @return number of vertices of this renderable geometry.
     */
    public final int getVertexCount() {
        return vertexCount;
    }
}
