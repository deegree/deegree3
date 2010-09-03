//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/tools/GeometryCallBack.java $
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

package org.deegree.rendering.r3d.opengl.tesselation;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>GeometryCallBack</code> class will be called by the {@link Tesselator} if a {@link SimpleAccessGeometry}
 * must be triangulated. This class will calculate the normals from the resulting vertices as well.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author: rbezema $
 *
 * @version $Revision: 15512 $, $Date: 2009-01-06 12:12:13 +0100 (Di, 06 Jan 2009) $
 *
 */
public class GeometryCallBack extends GLUtessellatorCallbackAdapter {

    private final transient static Logger LOG = LoggerFactory.getLogger( GeometryCallBack.class );

    private GLU glu;

    private List<Vertex> tesselatedVertices;

    private final SimpleAccessGeometry geom;

    // will be set in begin;
    private int openGLType = GL.GL_TRIANGLES;

    GeometryCallBack( SimpleAccessGeometry geom ) {
        this.geom = geom;
        glu = new GLU();
        tesselatedVertices = new LinkedList<Vertex>();
    }

    @Override
    public void begin( int openGLType ) {
        if ( LOG.isTraceEnabled() ) {
            StringBuilder sb = new StringBuilder( "Tesselation type is: " );
            switch ( openGLType ) {
            case GL.GL_TRIANGLES:
                sb.append( "triangles" );
                break;
            case GL.GL_TRIANGLE_STRIP:
                sb.append( "triangle strip" );
                break;
            case GL.GL_TRIANGLE_FAN:
                sb.append( "triangle fan" );
                break;
            case GL.GL_LINE_LOOP:
                sb.append( "line loop" );
                break;
            default:
                LOG.warn( "Don't know open gl type: " + openGLType );
                break;
            }
            LOG.trace( sb.toString() );
        }
        this.openGLType = openGLType;
    }

    @Override
    public void end() {
        LOG.trace( "Tesselation end of polygon called." );
        super.end();
    }

    @Override
    public void errorData( int arg0, Object originalVertex ) {
        super.errorData( arg0, originalVertex );
        throw new IllegalArgumentException( "Error while tesselating: " + originalVertex + " cause: "
                                            + glu.gluErrorString( arg0 ) );
        // LOG.error( );
    }

    @Override
    public void combineData( double[] coords, Object[] coordinateData, float[] weights, Object[] outData,
                             Object originalVertex ) {
        LOG.trace( "Tesselation combining data." );

        LOG.trace( "Coordinates of vertex: " + coords[0] + "," + coords[1] + "," + coords[2] );
        Vertex[] cd = new Vertex[coordinateData.length];
        for ( int i = 0; i < coordinateData.length; ++i ) {
            cd[i] = (Vertex) coordinateData[i];
        }
        outData[0] = new Vertex( coords, cd, weights );
    }

    @Override
    public void edgeFlagData( boolean arg0, Object originalVertex ) {
        // LOG.trace( "Tesselation edge flag." );
    }

    @Override
    public void vertexData( Object newVertex, Object originalVertex ) {
        if ( LOG.isTraceEnabled() ) {
            StringBuilder sb = new StringBuilder( "New tesselate vertex:\n" );
            sb.append( newVertex );
            LOG.trace( sb.toString() );
        }
        addVertex( (Vertex) newVertex );
    }

    /**
     * @return the vertices of the tesselated geometry
     */
    public List<Vertex> getTesselatedVertices() {
        return tesselatedVertices;
    }

    /**
     * @param vertex
     */
    public void addVertex( Vertex vertex ) {
        tesselatedVertices.add( vertex );
    }

    /**
     * @return the geometry object currently tesselated
     */
    public final SimpleAccessGeometry getGeometry() {
        return geom;
    }

    /**
     * Create a vertex appropriate for the tesselation of the given type of geometry.
     *
     * @param currentVertexLocation
     *            of the coordinates (the Vertex count)
     * @return the vertex used for the tesselation process.
     */
    public Vertex createNewVertex( int currentVertexLocation ) {
        float[] coords = geom.getCoordinateForVertex( currentVertexLocation );
        return new Vertex( coords, null );
    }

    /**
     * Calculate the normals for the tesselated geometry and return a renderable geometry created from the given
     * {@link SimpleAccessGeometry}
     *
     * @param useDirectBuffers
     *            to use direct buffers instead of heap buffers.
     *
     * @return the tesselated {@link SimpleAccessGeometry} as a {@link RenderableGeometry}
     */
    public RenderableGeometry createRenderableGeometry( boolean useDirectBuffers ) {
        return new RenderableGeometry( getTesselatedCoordinates(), getOpenGLType(), calculateNormals(),
                                       geom.getStyle(), useDirectBuffers );

    }

    /**
     * @return the coordinates of the vertices created by the tesselation process.
     */
    protected float[] getTesselatedCoordinates() {
        float[] coords = new float[tesselatedVertices.size() * 3];
        for ( int vertex = 0; vertex < tesselatedVertices.size(); ++vertex ) {
            Vertex v = tesselatedVertices.get( vertex );
            coords[vertex * 3] = v.x;
            coords[( vertex * 3 ) + 1] = v.y;
            coords[( vertex * 3 ) + 2] = v.z;
        }
        return coords;
    }

    /**
     * Calculate the normals according to the openGL type.
     *
     * @return the normals appropriate for the openGL type.
     */
    protected float[] calculateNormals() {
        // type can be triangles, triangle fan, triangle strip
        float[] normals = new float[tesselatedVertices.size() * 3];
        switch ( openGLType ) {
        case GL.GL_TRIANGLES:
            calcNormalsForTriangles( normals );
            break;
        case GL.GL_TRIANGLE_STRIP:
            calcNormalsForTriangleStrip( normals );
            break;
        case GL.GL_TRIANGLE_FAN:
            calcNormalsForTriangleFan( normals );
            break;
        default:
            LOG.warn( "Don't know open gl type: " + glu.gluGetString( openGLType ) );
            break;
        }
        return normals;
    }

    /**
     * Triangle fans are centered around the first vertex (see programming guide s.44)
     */
    private void calcNormalsForTriangleFan( float[] normals ) {
        float[] calculatedNormal = new float[3];
        for ( int vertex = 0; vertex < tesselatedVertices.size(); ++vertex ) {
            if ( vertex == 0 ) {
                calcNormal( tesselatedVertices.get( 0 ), tesselatedVertices.get( 1 ), tesselatedVertices.get( 2 ),
                            calculatedNormal );
                setNormalForVertex( normals, calculatedNormal, 0 );
                setNormalForVertex( normals, calculatedNormal, 1 );
                setNormalForVertex( normals, calculatedNormal, 2 );
            } else {
                if ( vertex + 1 < tesselatedVertices.size() ) {
                    calcNormal( tesselatedVertices.get( 0 ), tesselatedVertices.get( vertex ),
                                tesselatedVertices.get( vertex + 1 ), calculatedNormal );

                    averageNormal( normals, calculatedNormal, 0 );
                    averageNormal( normals, calculatedNormal, vertex );
                    setNormalForVertex( normals, calculatedNormal, vertex + 1 );
                } else {
                    LOG.warn( "Not enough vertices to create another triangle in the triangle strip." );
                }
            }
        }
    }

    /**
     * Triangle strips have following use following scheme( programming guide s.44):<code>
     * first triangle consist of vertices v0,v1,v2
     * after that
     * even triangles  consist of vertices v+1,v,v+2 (reversed orientation)
     * odd triangles  consist of vertices v,v+1,v+2
     * </code>
     *
     */
    private void calcNormalsForTriangleStrip( float[] normals ) {
        float[] calculatedNormal = new float[3];
        for ( int vertex = 0; vertex < tesselatedVertices.size(); ++vertex ) {
            if ( vertex == 0 ) {
                calcNormal( tesselatedVertices.get( 0 ), tesselatedVertices.get( 1 ), tesselatedVertices.get( 2 ),
                            calculatedNormal );
                setNormalForVertex( normals, calculatedNormal, 0 );
                setNormalForVertex( normals, calculatedNormal, 1 );
                setNormalForVertex( normals, calculatedNormal, 2 );
            } else {
                if ( vertex + 2 < tesselatedVertices.size() ) {
                    if ( ( vertex + 1 ) % 2 == 0 ) {
                        // an even counted triangle uses b,a,c for the triangle

                        calcNormal( tesselatedVertices.get( vertex + 1 ), tesselatedVertices.get( vertex ),
                                    tesselatedVertices.get( vertex + 2 ), calculatedNormal );
                    } else {
                        // an odd count triangle uses a,b,c for the triangle.
                        calcNormal( tesselatedVertices.get( vertex ), tesselatedVertices.get( vertex + 1 ),
                                    tesselatedVertices.get( vertex + 2 ), calculatedNormal );
                    }
                    averageNormal( normals, calculatedNormal, vertex );
                    averageNormal( normals, calculatedNormal, vertex + 1 );
                    setNormalForVertex( normals, calculatedNormal, vertex + 2 );
                } else {
                    LOG.warn( "Not enough vertices to create another triangle in the triangle strip." );
                }
            }
        }
    }

    /**
     * Add the given normal to the already present normal 're'-normalize it.
     *
     * @param normals
     *            to use
     * @param calculatedNormal
     *            to add
     * @param vertex
     */
    private void averageNormal( float[] normals, float[] normal, int vertexIndex ) {
        int offset = vertexIndex * 3;
        if ( offset > normals.length ) {
            LOG.warn( "Given vertex: " + vertexIndex + " (offset: " + offset
                      + ") would be outside the normal array with length: " + normals.length );
            return;
        }
        normals[offset] += normal[0];
        normals[offset + 1] += normal[1];
        normals[offset + 2] += normal[2];
        double length = Math.sqrt( ( normals[offset] * normal[offset] ) + ( normal[offset + 1] * normal[offset + 1] )
                                   + ( normal[offset + 2] * normal[offset + 2] ) );
        if ( Math.abs( length - 1 ) > 1E-10 ) {
            normals[offset] /= length;
            normals[offset + 1] /= length;
            normals[offset + 2] /= length;
        }

    }

    private void setNormalForVertex( float[] normals, float[] normal, int vertexIndex ) {
        int offset = vertexIndex * 3;
        if ( offset > normals.length ) {
            LOG.warn( "Given vertex: " + vertexIndex + " (offset: " + offset
                      + ") would be outside the normal array with length: " + normals.length );
            return;
        }
        normals[offset] = normal[0];
        normals[offset + 1] = normal[1];
        normals[offset + 2] = normal[2];
    }

    /**
     * Calculate the normal for each triangle
     */
    private void calcNormalsForTriangles( float[] normals ) {
        int vertex = 0;
        float[] calculatedNormal = new float[3];
        for ( ; ( vertex + 2 ) < tesselatedVertices.size(); vertex += 3 ) {
            calcNormal( tesselatedVertices.get( vertex ), tesselatedVertices.get( vertex + 1 ),
                        tesselatedVertices.get( vertex + 2 ), calculatedNormal );
            setNormalForVertex( normals, calculatedNormal, vertex );
            setNormalForVertex( normals, calculatedNormal, vertex + 1 );
            setNormalForVertex( normals, calculatedNormal, vertex + 2 );
        }
        if ( vertex < tesselatedVertices.size() ) {
            // the last triangle is not complete, lets set the normals to the last triangle
            int lastIndices = tesselatedVertices.size() - vertex;
            LOG.warn( "The last triangle was not complete, ( missing " + lastIndices
                      + ( ( lastIndices > 1 ) ? "vertices" : "vertex" ) + "; using normal of last triangle" );
            for ( ; vertex < tesselatedVertices.size(); ++vertex ) {
                setNormalForVertex( normals, calculatedNormal, vertex );
            }
        }
    }

    private void calcNormal( Vertex a, Vertex b, Vertex c, float[] normal ) {
        Vectors3f.normalizedNormal( a.getCoords(), b.getCoords(), c.getCoords(), normal );
        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "resulting normal: " + Vectors3f.asString( normal ) );
        }
    }

    /**
     * @return the openGLType
     */
    public final int getOpenGLType() {
        return openGLType;
    }

}
