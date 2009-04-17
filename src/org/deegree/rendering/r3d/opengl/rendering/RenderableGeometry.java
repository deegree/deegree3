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

import static org.deegree.rendering.r3d.opengl.rendering.utils.BufferIO.writeBufferToStream;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.AllocatedHeapMemory;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.geometry.SimpleGeometryStyle;
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

    private transient FloatBuffer coordBuffer = null;

    // 3D, same length as renderableGeometry
    private transient FloatBuffer normalBuffer = null;

    private transient int vertexCount;

    private transient boolean direct;

    private transient int coordPosition;

    private transient int normalPosition;

    private transient int numberOfOrdinates;

    private transient float[] ambientColor;

    private transient float[] diffuseColor;

    private transient float[] specularColor;

    private transient float[] emmisiveColor;

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param specularColor
     * @param ambientColor
     * @param diffuseColor
     * @param emmisiveColor
     * @param shininess
     * @param useDirectBuffers
     *            to use direct buffers instead of heap buffers.
     */
    public RenderableGeometry( float[] vertices, int openGLType, float[] vertexNormals, int specularColor,
                               int ambientColor, int diffuseColor, int emmisiveColor, float shininess,
                               boolean useDirectBuffers ) {
        super( specularColor, ambientColor, diffuseColor, emmisiveColor, shininess );
        this.direct = useDirectBuffers;
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
        vertices = null;
        setVertexNormals( vertexNormals );

        coordPosition = -1;
        normalPosition = -1;
    }

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param useDirectBuffers
     *            to use direct buffers instead of heap buffers.
     */
    public RenderableGeometry( float[] vertices, int openGLType, float[] vertexNormals, boolean useDirectBuffers ) {
        this( vertices, openGLType, vertexNormals, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0, 1, useDirectBuffers );
    }

    /**
     * Create float arrays of the int colors.
     */
    private void createColors() {
        ambientColor = getAsFloats( getAmbientColor() );
        emmisiveColor = getAsFloats( getEmmisiveColor() );
        specularColor = getAsFloats( getSpecularColor() );
        diffuseColor = getAsFloats( getDiffuseColor() );
    }

    /**
     * The float array appropriate for opengl.
     * 
     * @param color
     * @return
     */
    private float[] getAsFloats( int color ) {
        return new float[] { ( ( color >> 24 ) & 0xFF ) / 255f, ( ( color >> 16 ) & 0xFF ) / 255f,
                            ( ( color >> 8 ) & 0xFF ) / 255f, ( color & 0xFF ) / 255f, };
    }

    @Override
    public void render( GL context, ViewParams params ) {
        enableArrays( context );
        context.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        context.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT, ambientColor, 0 );
        context.glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, diffuseColor, 0 );
        context.glMaterialfv( GL.GL_FRONT, GL.GL_SPECULAR, specularColor, 0 );
        context.glMaterialfv( GL.GL_FRONT, GL.GL_EMISSION, emmisiveColor, 0 );
        context.glMaterialf( GL.GL_FRONT, GL.GL_SHININESS, getShininess() );
        context.glDrawArrays( openGLType, 0, vertexCount );
        context.glPopAttrib();
        disableArrays( context );
    }

    /**
     * Load the float buffers and enable the client state.
     * 
     * @param context
     */
    protected void enableArrays( GL context ) {

        LOG.trace( "Loading coordbuffer" );

        if ( coordPosition >= 0 ) {
            coordBuffer = MainBuffer.getCoords( coordPosition, numberOfOrdinates );
            if ( coordBuffer != null ) {
                coordPosition = -1;
            }
        }
        if ( coordBuffer != null ) {
            if ( ambientColor == null ) {
                createColors();
            }
            context.glVertexPointer( 3, GL.GL_FLOAT, 0, coordBuffer );
            if ( hasNormals ) {
                LOG.trace( "Loading normal buffer" );
                if ( normalPosition >= 0 && ( normalBuffer == null ) ) {
                    normalBuffer = MainBuffer.getNormals( normalPosition, numberOfOrdinates );
                    hasNormals = ( normalBuffer != null );
                }
                if ( normalBuffer != null ) {
                    context.glNormalPointer( GL.GL_FLOAT, 0, normalBuffer );
                }

            }
        }
    }

    /**
     * @param context
     */
    public void disableArrays( GL context ) {
        LOG.trace( "Disabling client states: normal and color" );
        context.glDisableClientState( GL.GL_COLOR_ARRAY );
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
                throw new IllegalArgumentException( "The number of vertex normals(" + ( vertexNormals.length                                                                                                                                                                                                                                                                                       )
                                                    + ") must be kongruent to 3." );
            } else if ( ( vertexNormals.length / 3 ) != vertexCount ) {
                throw new IllegalArgumentException( "The number of normals (" + ( vertexNormals.length / 3 )
                                                    + ") must equal the number of vertices (" + vertexCount + ")." );
            }
            if ( direct ) {
                this.normalBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( vertexNormals ) );
            } else {
                this.normalBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( vertexNormals ) );
            }
        }
    }

    private int loadVertexBuffer( float[] vertices ) {
        if ( vertices == null || vertices.length == 0 ) {
            throw new IllegalArgumentException(
                                                "A RenderableGeometry must have vertices to work with (the vertices array may not be null or empty). " );
        }

        if ( direct ) {
            coordBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( vertices ) );
        } else {
            coordBuffer = FloatBuffer.wrap( vertices );
        }
        numberOfOrdinates = vertices.length;
        return vertices.length / 3;
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
        out.writeBoolean( direct );
        out.writeInt( vertexCount );
        out.writeInt( openGLType );
        if ( coordBuffer != null ) {
            writeBufferToStream( coordBuffer, out );
            writeBufferToStream( normalBuffer, out );
        } else {
            writeBufferToStream( MainBuffer.getCoords( coordPosition, numberOfOrdinates ), out );
            writeBufferToStream( MainBuffer.getNormals( normalPosition, numberOfOrdinates ), out );
        }
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
        direct = in.readBoolean();
        vertexCount = in.readInt();
        numberOfOrdinates = vertexCount * 3;
        openGLType = in.readInt();

        coordPosition = MainBuffer.readCoordsFromStream( in );
        normalPosition = MainBuffer.readNormalsFromStream( in );

        hasNormals = ( normalPosition >= 0 );
    }

    /**
     * @return the bytes this geometry occupies
     */
    @Override
    public long sizeOf() {
        long localSize = super.sizeOf();
        // coordbuffer
        localSize += AllocatedHeapMemory.sizeOfBuffer( coordBuffer, true );
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

    /**
     * @return the coordBuffer
     */
    public final FloatBuffer getCoordBuffer() {
        return coordBuffer;
    }

    /**
     * @return the coordinate buffers as readonly
     */
    public FloatBuffer getReadOnlyCoordBuffer() {
        return coordBuffer.asReadOnlyBuffer();
    }

    /**
     * @return true if direct buffers should be used, false otherwise.
     */
    public boolean useDirectBuffers() {
        return direct;
    }

    /**
     * @return
     */
    public FloatBuffer getNormalBuffer() {
        return normalBuffer;
    }

    /**
     * @param coordPosition
     */
    public void setCoordPosition( int coordPosition ) {
        this.coordPosition = coordPosition;
    }

    /**
     * @param normalPosition
     */
    public void setNormPosition( int normalPosition ) {
        this.normalPosition = normalPosition;

    }

    @Override
    public int getOrdinateCount() {
        return vertexCount * 3;
    }

    @Override
    public int getTextureOrdinateCount() {
        return 0;
    }
}
