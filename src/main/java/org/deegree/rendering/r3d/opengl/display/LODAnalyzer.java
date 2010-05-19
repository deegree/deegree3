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
 * The <code>LODAnalyzer</code> displays the used macrotriangles in a scene. It determines which macrotriangles are used
 * for the current view and makes a 2D projections in nice color.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: $, $Date: $
 */
public class LODAnalyzer extends GLCanvas implements GLEventListener {

    private static final long serialVersionUID = -2679880887972155332L;

    private static final Logger LOG = LoggerFactory.getLogger( LODAnalyzer.class );

    private GLU glu = new GLU();

    private Collection<RenderMeshFragment> currentLOD = new ArrayList<RenderMeshFragment>();

    private ViewFrustum frustum;

    private final float maxX;

    private final float maxY;

    /**
     * Adds a gl listener to this {@link LODAnalyzer}
     * 
     * @param maxX
     * @param maxY
     * @throws GLException
     */
    public LODAnalyzer( float maxX, float maxY ) throws GLException {
        setMinimumSize( new Dimension( 0, 0 ) );
        addGLEventListener( this );
        this.maxX = maxX;
        this.maxY = maxY;
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        LOG.trace( "init( GLAutoDrawable ) called" );
        GL gl = drawable.getGL();
        gl.glClearColor( 1f, 1f, 1f, 0f );
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        LOG.trace( "display( GLAutoDrawable ) called" );
        GL gl = drawable.getGL();

        gl.glClear( GL.GL_COLOR_BUFFER_BIT );
        // gl.glLoadIdentity();
        if ( frustum != null && currentLOD != null ) {
            renderLODStructure( drawable.getGL() );
        }
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

        // render macrotriangle boundaries of current LOD
        gl.glBegin( GL.GL_TRIANGLES );
        for ( RenderMeshFragment fragment : currentLOD ) {
            if ( fragment != null ) {
                setColor( gl, fragment );

                float[][] mt = fragment.getTrianglePoints();
                if ( mt != null ) {
                    gl.glVertex2f( mt[0][0] / maxX, mt[0][1] / maxY );
                    gl.glVertex2f( mt[1][0] / maxX, mt[1][1] / maxY );
                    gl.glVertex2f( mt[2][0] / maxX, mt[2][1] / maxY );
                }
            }
        }
        gl.glEnd();

        // render fragment boundaries of current LOD
        gl.glBegin( GL.GL_LINES );
        gl.glColor3f( 0.0f, 0.0f, 0.0f );
        for ( RenderMeshFragment fragment : currentLOD ) {
            if ( fragment != null ) {
                float[][] mt = fragment.getTrianglePoints();
                if ( mt != null ) {
                    gl.glVertex2f( mt[0][0] / maxX, mt[0][1] / maxY );
                    gl.glVertex2f( mt[1][0] / maxX, mt[1][1] / maxY );

                    gl.glVertex2f( mt[1][0] / maxX, mt[1][1] / maxY );
                    gl.glVertex2f( mt[2][0] / maxX, mt[2][1] / maxY );

                    gl.glVertex2f( mt[2][0] / maxX, mt[2][1] / maxY );
                    gl.glVertex2f( mt[0][0] / maxX, mt[0][1] / maxY );
                }
            }
        }
        gl.glEnd();

        // draw view frustum boundaries
        Point3d eyePos = frustum.getEyePos();
        Point2f eyePos2D = new Point2f( (float) eyePos.x / maxX, (float) eyePos.y / maxY );

        gl.glColor3f( 1.0f, 0.0f, 0.0f );
        gl.glBegin( GL.GL_LINES );
        gl.glVertex2f( eyePos2D.x, eyePos2D.y );
        gl.glVertex2f( (float) frustum.ftr.x / maxX, (float) frustum.ftr.y / maxY );

        gl.glVertex2f( eyePos2D.x, eyePos2D.y );
        gl.glVertex2f( (float) frustum.ftl.x / maxX, (float) frustum.ftl.y / maxY );
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
        gl.glScalef( width, height, 1 );
    }

    @Override
    public void displayChanged( GLAutoDrawable drawable, boolean arg1, boolean arg2 ) {
        LOG.trace( "displayChanged( GLAutoDrawable, boolean, boolean ) called" );
    }

}
