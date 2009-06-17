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
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The <code>TextureTileAnalyzer</code> displays the currently rendered texturetiles defined by the
 * MultiResolutionModel.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 *
 */
public class TextureTileAnalyzer extends GLCanvas implements GLEventListener {

    /**
     *
     */
    private static final long serialVersionUID = 3484097952567872962L;

    private static final Logger LOG = LoggerFactory.getLogger( TextureTileAnalyzer.class );

    private GLU glu = new GLU();

    private ViewFrustum frustum;

    private Collection<TextureTileRequest> textureRequests;

    TextureTileAnalyzer() throws GLException {
        setMinimumSize( new Dimension( 0, 0 ) );
        addGLEventListener( this );
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        LOG.trace( "init( GLAutoDrawable ) called" );
        GL gl = drawable.getGL();

        gl.glEnable( GL.GL_BLEND ); // enable color and texture blending
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        LOG.trace( "display( GLAutoDrawable ) called" );
        renderTextureTileStructure( drawable.getGL() );
    }

    void updateParameters( Collection<TextureTileRequest> textureRequests, ViewFrustum frustum ) {
        this.textureRequests = textureRequests;
        this.frustum = frustum;
    }

    private void renderTextureTileStructure( GL gl ) {

        gl.glColor3f( 1.0f, 1.0f, 1.0f );
        gl.glBegin( GL.GL_POLYGON );
        gl.glVertex2f( 0f, 0f );
        gl.glVertex2f( 1f, 0f );
        gl.glVertex2f( 1f, 1f );
        gl.glVertex2f( 0f, 1f );
        gl.glEnd();

        // render texture tile boundaries
        gl.glBegin( GL.GL_QUADS );
        for ( TextureTileRequest request : textureRequests ) {
            setColor( gl, request );
            gl.glVertex2f( request.getMinX() / 32768.0f, request.getMinY() / 32768.0f );
            gl.glVertex2f( request.getMaxX() / 32768.0f, request.getMinY() / 32768.0f );
            gl.glVertex2f( request.getMaxX() / 32768.0f, request.getMaxY() / 32768.0f );
            gl.glVertex2f( request.getMinX() / 32768.0f, request.getMaxY() / 32768.0f );
        }
        gl.glEnd();

        gl.glColor3f( 0.0f, 0.0f, 0.0f );
        gl.glBegin( GL.GL_LINES );
        for ( TextureTileRequest request : textureRequests ) {
            gl.glVertex2f( request.getMinX() / 32768.0f, request.getMinY() / 32768.0f );
            gl.glVertex2f( request.getMaxX() / 32768.0f, request.getMinY() / 32768.0f );

            gl.glVertex2f( request.getMaxX() / 32768.0f, request.getMinY() / 32768.0f );
            gl.glVertex2f( request.getMaxX() / 32768.0f, request.getMaxY() / 32768.0f );

            gl.glVertex2f( request.getMaxX() / 32768.0f, request.getMaxY() / 32768.0f );
            gl.glVertex2f( request.getMinX() / 32768.0f, request.getMaxY() / 32768.0f );

            gl.glVertex2f( request.getMinX() / 32768.0f, request.getMaxY() / 32768.0f );
            gl.glVertex2f( request.getMinX() / 32768.0f, request.getMinY() / 32768.0f );
        }
        gl.glEnd();

        // draw view frustum boundaries
        Point3d eyePos = frustum.getEyePos();
        Point2f eyePos2D = new Point2f( (float) eyePos.x / 32768.0f, (float) eyePos.y / 32768.0f );

        gl.glColor3f( 1.0f, 0.0f, 0.0f );
        gl.glBegin( GL.GL_LINES );
        gl.glVertex2f( eyePos2D.x, eyePos2D.y );
        gl.glVertex2d( frustum.ftr.x / 32768.0f, frustum.ftr.y / 32768.0f );

        gl.glVertex2f( eyePos2D.x, eyePos2D.y );
        gl.glVertex2d( frustum.ftl.x / 32768.0f, frustum.ftl.y / 32768.0f );
        gl.glEnd();
    }

    private void setColor( GL gl, TextureTileRequest request ) {
        if ( request.getMetersPerPixel() <= 0.1 ) {
            gl.glColor4f( 1.0f, 0.0f, 0.0f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 0.2 ) {
            gl.glColor4f( 1.0f, 0.5f, 0.0f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 0.4 ) {
            gl.glColor4f( 1.0f, 1.0f, 0.0f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 0.8 ) {
            gl.glColor4f( 0.5f, 1.0f, 0.0f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 1.6 ) {
            gl.glColor4f( 0.0f, 1.0f, 0.0f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 3.2 ) {
            gl.glColor4f( 0.0f, 1.0f, 0.5f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 6.4 ) {
            gl.glColor4f( 0.0f, 1.0f, 1.0f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 12.8 ) {
            gl.glColor4f( 0.0f, 0.5f, 1.0f, 0.2f );
        } else if ( request.getMetersPerPixel() <= 25.6 ) {
            gl.glColor4f( 0.0f, 0.0f, 1.0f, 0.2f );
        } else {
            gl.glColor4f( 0.0f, 0.0f, 0.9f, 0.2f );
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
}
