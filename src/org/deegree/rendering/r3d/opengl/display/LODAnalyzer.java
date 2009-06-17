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
package org.deegree.rendering.r3d.opengl.display;

import java.awt.Dimension;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;

import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The <code>LODAnalyzer</code> displays the used macrotriangles in a scene. It determines which macrotriangles are
 * used for the current view and makes a 2D projections in nice color.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 *
 */
public class LODAnalyzer extends GLCanvas implements GLEventListener {

    /**
     *
     */
    private static final long serialVersionUID = -2679880887972155332L;

    private static final Logger LOG = LoggerFactory.getLogger( LODAnalyzer.class );

    private GLU glu = new GLU();

    private Collection<RenderMeshFragment> currentLOD = new ArrayList<RenderMeshFragment>();

    private ViewFrustum frustum;

    /**
     * Adds a gl listener to this {@link LODAnalyzer}
     *
     * @throws GLException
     */
    public LODAnalyzer() throws GLException {
        setMinimumSize( new Dimension( 0, 0 ) );
        addGLEventListener( this );
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        LOG.trace( "init( GLAutoDrawable ) called" );
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        LOG.trace( "display( GLAutoDrawable ) called" );
        renderLODStructure( drawable.getGL() );
    }

    /**
     * Update the {@link RenderMeshFragment}s and the view frustum, no calculations are done.
     *
     * @param currentLOD
     * @param frustum
     */
    public void updateParameters( Collection<RenderMeshFragment> currentLOD, ViewFrustum frustum ) {
        this.currentLOD = currentLOD;
        this.frustum = frustum;
    }

    private void renderLODStructure( GL gl ) {

        gl.glColor3f( 1.0f, 1.0f, 1.0f );
        gl.glBegin( GL.GL_POLYGON );
        gl.glVertex2f( 0f, 0f );
        gl.glVertex2f( 1f, 0f );
        gl.glVertex2f( 1f, 1f );
        gl.glVertex2f( 0f, 1f );
        gl.glEnd();

        // render macrotriangle boundaries of current LOD
        gl.glBegin( GL.GL_TRIANGLES );
        for ( RenderMeshFragment fragment : currentLOD ) {
            setColor( gl, fragment );
            MacroTriangle mt = new MacroTriangle( fragment );
            gl.glVertex2f( mt.p0.x / 32768.0f, mt.p0.y / 32768.0f );
            gl.glVertex2f( mt.p1.x / 32768.0f, mt.p1.y / 32768.0f );
            gl.glVertex2f( mt.p2.x / 32768.0f, mt.p2.y / 32768.0f );
        }
        gl.glEnd();

        // render fragment boundaries of current LOD
        gl.glBegin( GL.GL_LINES );
        gl.glColor3f( 0.0f, 0.0f, 0.0f );
        for ( RenderMeshFragment fragment : currentLOD ) {
            MacroTriangle mt = new MacroTriangle( fragment );
            gl.glVertex2f( mt.p0.x / 32768.0f, mt.p0.y / 32768.0f );
            gl.glVertex2f( mt.p1.x / 32768.0f, mt.p1.y / 32768.0f );

            gl.glVertex2f( mt.p1.x / 32768.0f, mt.p1.y / 32768.0f );
            gl.glVertex2f( mt.p2.x / 32768.0f, mt.p2.y / 32768.0f );

            gl.glVertex2f( mt.p2.x / 32768.0f, mt.p2.y / 32768.0f );
            gl.glVertex2f( mt.p0.x / 32768.0f, mt.p0.y / 32768.0f );
        }
        gl.glEnd();

        // draw view frustum boundaries
        Point3d eyePos = frustum.getEyePos();
        Point2f eyePos2D = new Point2f( (float) eyePos.x / 32768.0f, (float) eyePos.y / 32768.0f );

        gl.glColor3f( 1.0f, 0.0f, 0.0f );
        gl.glBegin( GL.GL_LINES );
        gl.glVertex2f( eyePos2D.x, eyePos2D.y );
        gl.glVertex2f( (float) frustum.ftr.x / 32768.0f, (float) frustum.ftr.y / 32768.0f );

        gl.glVertex2f( eyePos2D.x, eyePos2D.y );
        gl.glVertex2f( (float) frustum.ftl.x / 32768.0f, (float) frustum.ftl.y / 32768.0f );
        gl.glEnd();
    }

    private void setColor( GL gl, RenderMeshFragment patch ) {
        if ( patch.getGeometricError() >= 14.0 ) {
            gl.glColor3f( 0.0f, 0.0f, 0.4f );
        } else if ( patch.getGeometricError() >= 13.0 ) {
            gl.glColor3f( 0.0f, 0.0f, 0.5f );
        } else if ( patch.getGeometricError() >= 12.0 ) {
            gl.glColor3f( 0.0f, 0.0f, 0.6f );
        } else if ( patch.getGeometricError() >= 11.0 ) {
            gl.glColor3f( 0.0f, 0.0f, 0.7f );
        } else if ( patch.getGeometricError() >= 10.0 ) {
            gl.glColor3f( 0.0f, 0.0f, 0.8f );
        } else if ( patch.getGeometricError() >= 9.0 ) {
            gl.glColor3f( 0.0f, 0.0f, 0.9f );
        } else if ( patch.getGeometricError() >= 8.0 ) {
            gl.glColor3f( 0.0f, 0.0f, 1.0f );
        } else if ( patch.getGeometricError() >= 7.0 ) {
            gl.glColor3f( 0.0f, 0.5f, 1.0f );
        } else if ( patch.getGeometricError() >= 6.0 ) {
            gl.glColor3f( 0.0f, 1.0f, 1.0f );
        } else if ( patch.getGeometricError() >= 5.0 ) {
            gl.glColor3f( 0.0f, 1.0f, 0.5f );
        } else if ( patch.getGeometricError() >= 4.0 ) {
            gl.glColor3f( 0.0f, 1.0f, 0.0f );
        } else if ( patch.getGeometricError() >= 3.0 ) {
            gl.glColor3f( 0.5f, 1.0f, 0.0f );
        } else if ( patch.getGeometricError() >= 2.0 ) {
            gl.glColor3f( 1.0f, 1.0f, 0.0f );
        } else if ( patch.getGeometricError() >= 1.0 ) {
            gl.glColor3f( 1.0f, 0.5f, 0.0f );
        } else {
            gl.glColor3f( 1.0f, 0.0f, 0.0f );
        }
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        LOG.trace( "reshape( GLAutoDrawable, " + x + ", " + y + ", " + width + ", " + height + " ) called" );

        GL gl = drawable.getGL();
        gl.glViewport( x, y, width, height );

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity();
        glu.gluOrtho2D( 0, width, 0, height );

        gl.glMatrixMode( GL.GL_MODELVIEW );
        gl.glLoadIdentity();
        int posX = 0;
        int posY = 0;
        gl.glViewport( posX, posY, width, height );
        gl.glScaled( width, height, 1 );
    }

    @Override
    public void displayChanged( GLAutoDrawable drawable, boolean arg1, boolean arg2 ) {
        LOG.trace( "displayChanged( GLAutoDrawable, boolean, boolean ) called" );
    }

    private class MacroTriangle {

        Point2f p0;

        Point2f p1;

        Point2f p2;

        MacroTriangle( RenderMeshFragment fragment ) {

            // really dumb (and slow), just used for debugging
            float[][] bbox = fragment.getBBox();
            Point2f v0 = new Point2f( bbox[0][0], bbox[0][1] );
            Point2f v1 = new Point2f( bbox[1][0], bbox[0][1] );
            Point2f v2 = new Point2f( bbox[1][0], bbox[1][1] );
            Point2f v3 = new Point2f( bbox[0][0], bbox[1][1] );

            boolean v0found = false;
            boolean v1found = false;
            boolean v2found = false;
            boolean v3found = false;

            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;

            FloatBuffer vertices = fragment.getData().getVertices();
            while ( vertices.hasRemaining() ) {
                float x = vertices.get();
                float y = vertices.get();
                vertices.get();

                if ( x < minX ) {
                    minX = x;
                }
                if ( x > maxX ) {
                    maxX = x;
                }
                if ( y < minY ) {
                    minY = y;
                }
                if ( y > maxY ) {
                    maxY = y;
                }

                Point2f v = new Point2f( x, y );
                if ( v.equals( v0 ) ) {
                    v0found = true;
                } else if ( v.equals( v1 ) ) {
                    v1found = true;
                } else if ( v.equals( v2 ) ) {
                    v2found = true;
                } else if ( v.equals( v3 ) ) {
                    v3found = true;
                }
            }

            if ( v0found && v1found ) {
                p0 = v0;
                p1 = v1;
                if ( v2found ) {
                    p2 = v2;
                } else if ( v3found ) {
                    p2 = v3;
                } else {
                    p2 = new Point2f( ( v1.x - v0.x ) / 2.0f + v0.x, v3.y );
                }
            } else if ( v2found && v3found ) {
                p0 = v2;
                p1 = v3;
                if ( v0found ) {
                    p2 = v0;
                } else if ( v1found ) {
                    p2 = v1;
                } else {
                    p2 = new Point2f( ( v3.x - v2.x ) / 2.0f + v2.x, v0.y );
                }
            } else if ( v1found && v2found ) {
                p0 = v1;
                p1 = v2;
                if ( v0found ) {
                    p2 = v0;
                } else if ( v3found ) {
                    p2 = v3;
                } else {
                    p2 = new Point2f( v0.x, ( v2.y - v1.y ) / 2.0f + v1.y );
                }
            } else if ( v3found && v0found ) {
                p0 = v3;
                p1 = v0;
                if ( v1found ) {
                    p2 = v1;
                } else if ( v2found ) {
                    p2 = v2;
                } else {
                    p2 = new Point2f( v1.x, ( v3.y - v0.y ) / 2.0f + v0.y );
                }
            } else {
                p0 = v0;
                p1 = v0;
                p2 = v0;
            }
            vertices.rewind();
        }
    }
}
