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

import static org.deegree.rendering.r3d.opengl.JOGLUtils.convertColorIntAsFloats;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.rendering.r3d.model.geometry.SimpleGeometryStyle;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
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
public class RenderableGeometry implements RenderableQualityModelPart {

    /**
     *
     */
    private static final long serialVersionUID = -7536310565460231026L;

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

    // private transient float[] ambientColor;

    private transient float[] diffuseColor;

    private transient float[] specularColor;

    private transient float[] emmisiveColor;

    private transient SimpleGeometryStyle style;

    /**
     * Set and check the given parameters.
     *
     * @param openGLType
     * @param style
     * @param useDirectBuffers
     */
    private RenderableGeometry( int openGLType, SimpleGeometryStyle style, boolean useDirectBuffers ) {
        this.style = style;
        this.direct = useDirectBuffers;
        this.openGLType = openGLType;
        switch ( openGLType ) {
        case GL.GL_LINE_STRIP:
        case GL.GL_TRIANGLE_FAN:
        case GL.GL_TRIANGLES:
        case GL.GL_TRIANGLE_STRIP:
        case GL.GL_QUADS:
        case GL.GL_POLYGON:
            break;
        default:
            throw new UnsupportedOperationException( "Unknown opengl type: " + openGLType );
        }
    }

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param style
     * @param useDirectBuffers
     *            to use direct buffers instead of heap buffers.
     */
    public RenderableGeometry( float[] vertices, int openGLType, float[] vertexNormals, SimpleGeometryStyle style,
                               boolean useDirectBuffers ) {
        this( openGLType, style, useDirectBuffers );
        this.vertexCount = loadVertexBuffer( vertices );
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
        this( vertices, openGLType, vertexNormals, new SimpleGeometryStyle(), useDirectBuffers );
    }

    /**
     * @param vertices
     * @param openGLType
     * @param vertexNormals
     * @param style
     */
    public RenderableGeometry( FloatBuffer vertices, int openGLType, FloatBuffer vertexNormals,
                               SimpleGeometryStyle style ) {
        this( openGLType, style, vertices.isDirect() );
        this.vertexCount = vertices.capacity() / 3;
        this.numberOfOrdinates = vertices.capacity();
        this.coordBuffer = vertices;
        this.normalBuffer = vertexNormals;
        hasNormals = ( vertexNormals != null );
        coordPosition = -1;
        normalPosition = -1;
    }

    /**
     * @param coordPosition
     * @param vertexCount
     * @param openGLType
     * @param normalPosition
     * @param style
     */
    public RenderableGeometry( int coordPosition, int vertexCount, int openGLType, int normalPosition,
                               SimpleGeometryStyle style ) {
        this( openGLType, style, true );
        this.coordPosition = coordPosition;
        this.normalPosition = normalPosition;
        hasNormals = ( this.normalPosition > -1 );
        this.vertexCount = vertexCount;
        this.numberOfOrdinates = vertexCount * 3;
    }

    /**
     * Create float arrays of the int colors.
     */
    private void createColors() {
        // ambientColor = new float[] { 0.6f, 0.6f, 0.6f, 1 };// convertColorIntAsFloats( style.getAmbientColor() );
        emmisiveColor = convertColorIntAsFloats( style.getEmmisiveColor() );
        specularColor = convertColorIntAsFloats( style.getSpecularColor() );
        diffuseColor = convertColorIntAsFloats( style.getDiffuseColor() );
    }

    @Override
    public void render( RenderContext glRenderContext ) {
        this.renderPrepared( glRenderContext, null );
    }

    public void renderPrepared( RenderContext glRenderContext, DirectGeometryBuffer geomBuffer ) {
        enableArrays( glRenderContext, geomBuffer );
        GL context = glRenderContext.getContext();
        normalBuffer.rewind();

        context.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        // context.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambientColor, 0 );
        context.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuseColor, 0 );
        context.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specularColor, 0 );
        context.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, emmisiveColor, 0 );
        context.glMaterialf( GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, style.getShininess() );
        context.glDrawArrays( openGLType, 0, vertexCount );
        context.glPopAttrib();

        disableArrays( glRenderContext );
    }

    @SuppressWarnings("unused")
    private void checkNormalLength() {
        float[] normal = new float[3];

        for ( ; normalBuffer.position() + 2 < normalBuffer.capacity(); ) {
            normal[0] = normalBuffer.get();
            normal[1] = normalBuffer.get();
            normal[2] = normalBuffer.get();
            float length = Vectors3f.length( normal );
            if ( length > 1 ) {
                System.out.println( "Normal is larger 1: " + length + ", this may not be. " );
            }
        }
        normalBuffer.rewind();
    }

    /**
     * Load the float buffers and enable the client state.
     *
     * @param glRenderContext
     * @param geomBuffer
     *            for which the coord/normal Positions are valid for.
     */
    protected void enableArrays( RenderContext glRenderContext, DirectGeometryBuffer geomBuffer ) {

        LOG.trace( "Loading coordbuffer" );

        if ( coordPosition >= 0 && geomBuffer != null ) {
            coordBuffer = geomBuffer.getCoords( coordPosition, numberOfOrdinates );
            if ( coordBuffer != null ) {
                coordPosition = -1;
            }
        }
        if ( coordBuffer != null ) {
            if ( diffuseColor == null ) {
                createColors();
            }
            glRenderContext.getContext().glVertexPointer( 3, GL.GL_FLOAT, 0, coordBuffer );
            if ( hasNormals ) {
                LOG.trace( "Loading normal buffer" );
                if ( normalPosition >= 0 && ( normalBuffer == null ) && ( geomBuffer != null ) ) {
                    normalBuffer = geomBuffer.getNormals( normalPosition, numberOfOrdinates );
                    hasNormals = ( normalBuffer != null );
                }
                if ( normalBuffer != null ) {
                    glRenderContext.getContext().glNormalPointer( GL.GL_FLOAT, 0, normalBuffer );
                }

            }
            // else {
            // context.glDisableClientState( GL.GL_NORMAL_ARRAY );
            // }
        }
    }

    /**
     * @param glRenderContext
     */
    public void disableArrays( RenderContext glRenderContext ) {
        // if ( !hasNormals ) {
        // context.glEnableClientState( GL.GL_NORMAL_ARRAY );
        // }
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
                throw new IllegalArgumentException( "The number of vertex normals(" + ( vertexNormals.length                                                                                                                                                                                                                                                                                                                                                                                                                                                     )
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
     * @return the bytes this geometry occupies
     */
    @Override
    public long sizeOf() {
        long localSize = style.sizeOf();
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
     * @return the style
     */
    public final SimpleGeometryStyle getStyle() {
        return style;
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
     * @return a floatbuffer containing the normals or <code>null</code> if normals are not loaded.
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

    /**
     * @return the coordPosition
     */
    public final int getCoordPosition() {
        return coordPosition;
    }

    /**
     * @return the normalPosition
     */
    public final int getNormalPosition() {
        return normalPosition;
    }
}
