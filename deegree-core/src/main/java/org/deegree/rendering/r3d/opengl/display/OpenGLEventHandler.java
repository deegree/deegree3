//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/tools/trunk/src/org/deegree/tools/rendering/OpenGLEventHandler.java $
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

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.JOGLUtils;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.GLUT;

/**
 * The <code>OpenGLEventHandler</code> class renders a list of DataObjects and handles opengl callback functions
 * delivered by a GLCanvas. It possesses a trackball which can roate the scene.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 15632 $, $Date: 2009-01-14 13:12:54 +0100 (Mi, 14 Jan 2009) $
 * 
 */
public class OpenGLEventHandler implements GLEventListener {

    private final static Logger LOG = LoggerFactory.getLogger( OpenGLEventHandler.class );

    private List<WorldRenderableObject> worldRenderableObjects = new ArrayList<WorldRenderableObject>();

    // The trackball
    private TrackBall trackBall;

    private float[] centroid;

    private float[] lookAt;

    private float[] eye;

    private boolean renderTestObject;

    private Envelope bbox;

    // Distance to end of scene
    private float farClippingPlane;

    private final float testObjectSize = 10f;

    private final float cubeHalf = testObjectSize * 0.5f;

    private final float sphereSize = testObjectSize - ( testObjectSize * 0.25f );

    private final float[][] cubeData = { { -cubeHalf, -cubeHalf, cubeHalf }, { cubeHalf, -cubeHalf, cubeHalf },
                                        { cubeHalf, cubeHalf, cubeHalf }, { -cubeHalf, cubeHalf, cubeHalf },
                                        { -cubeHalf, -cubeHalf, -cubeHalf }, { cubeHalf, -cubeHalf, -cubeHalf },
                                        { cubeHalf, cubeHalf, -cubeHalf }, { -cubeHalf, cubeHalf, -cubeHalf } };

    private GLU glu = new GLU();

    private GLUT glut = new GLUT();

    private int width;

    private int height;

    private float fov = 60;

    private double nearClippingPlane;

    /**
     * 
     * @param renderTestObject
     */
    public OpenGLEventHandler( boolean renderTestObject ) {
        trackBall = new TrackBall();
        centroid = new float[3];
        lookAt = new float[3];
        eye = new float[3];
        this.renderTestObject = renderTestObject;
        bbox = getDefaultBBox();
        calcViewParameters();
    }

    private Envelope getDefaultBBox() {
        return new GeometryFactory().createEnvelope( new double[] { -sphereSize, -sphereSize, -sphereSize },
                                                     new double[] { sphereSize, sphereSize, sphereSize }, null );
    }

    public Envelope getBbox() {
        return bbox;
    }

    /**
     *
     */
    public void calcViewParameters() {
        centroid = new float[] { (float) bbox.getCentroid().get0(), (float) bbox.getCentroid().get1(),
                                (float) bbox.getCentroid().get2() };
        lookAt = new float[] { centroid[0], centroid[1], centroid[2] };
        eye = calcOptimalEye( bbox );
        float dist = Vectors3f.distance( centroid, eye );
        farClippingPlane = 2 * dist;
        nearClippingPlane = 0.01 * farClippingPlane;
        trackBall.reset();
    }

    private float[] calcOptimalEye( Envelope bBox ) {
        float[] eye = new float[] { 0, 1, 1 };
        if ( bBox != null ) {
            double[] min = bBox.getMin().getAsArray();
            double[] max = bBox.getMax().getAsArray();
            double centerX = min[0] + ( ( max[0] - min[0] ) * 0.5f );
            double centerY = min[1] + ( ( max[1] - min[1] ) * 0.5f );

            // float centerZ = bBox[2] + ( ( bBox[2] - bBox[5] ) * 0.5f );
            double eyeZ = 2 * ( ( Math.max( bBox.getSpan0(), bBox.getSpan1() ) / 2 ) / Math.tan( Math.toRadians( fov * 0.5 ) ) );
            eye = new float[] { (float) centerX, (float) centerY, (float) eyeZ };
        }
        return eye;
    }

    @Override
    public void display( GLAutoDrawable theDrawable ) {
        GL gl = theDrawable.getGL();
        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
        gl.glLoadIdentity();
        glu.gluLookAt( eye[0], eye[1], eye[2], lookAt[0], lookAt[1], lookAt[2], 0, 1, 0 );
        trackBall.multModelMatrix( gl, centroid );
        float[] newEye = JOGLUtils.getEyeFromModelView( gl );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "farClippingPlane:" + farClippingPlane );
            LOG.debug( "centroid:" + centroid[0] + "," + centroid[1] + "," + centroid[2] );
            LOG.debug( "lookAt:" + lookAt[0] + "," + lookAt[1] + "," + lookAt[2] );
            LOG.debug( "eye:" + eye[0] + "," + eye[1] + "," + eye[2] );
            LOG.debug( "Eye in model space: " + Vectors3f.asString( newEye ) );
        }

        Point3d newEyeP = new Point3d( newEye[0], newEye[1], newEye[2] );
        Point3d center = new Point3d( lookAt[0], lookAt[1], lookAt[2] );
        Vector3d up = new Vector3d( 0, 0, 1 );
        ViewFrustum vf = new ViewFrustum( newEyeP, center, up, fov, (double) width / height, nearClippingPlane,
                                          farClippingPlane );
        ViewParams params = new ViewParams( vf, width, height );
        RenderContext context = new RenderContext( params );
        context.setContext( gl );

        for ( WorldRenderableObject dObj : worldRenderableObjects ) {
            dObj.render( context );
        }

        if ( renderTestObject ) {
            gl.glTranslatef( centroid[0], centroid[1], centroid[2] );
            drawCube( gl );
            glut.glutSolidSphere( sphereSize, 15, 15 );
        }
    }

    /**
     * Adds the given branch group to the scene and set the appropriate distance etc. After adding the branch group to
     * the rotation group which is controlled by the mouse rotator.
     * 
     * @param w
     *            a list of WorldRenderableObjects, can be <Code>null</Code>.
     */
    public void addDataObjectToScene( WorldRenderableObject b ) {
        if ( b != null ) {

            Envelope env = b.getBbox();
            if ( env != null ) {
                if ( isDefaultBBox() ) {
                    bbox = env;
                } else {
                    bbox = bbox.merge( env );
                }
            }
            calcViewParameters();

            worldRenderableObjects.add( b );

        }
    }

    private boolean isDefaultBBox() {
        Envelope env = getDefaultBBox();
        return ( Math.abs( bbox.getSpan0() - env.getSpan0() ) < 1E-11 )
               && ( Math.abs( bbox.getSpan1() - env.getSpan1() ) < 1E-11 )
               && ( Math.abs( bbox.getMin().get0() - env.getMin().get0() ) < 1E-11 )
               && ( Math.abs( bbox.getMin().get1() - env.getMin().get1() ) < 1E-11 )
               && ( Math.abs( bbox.getMin().get2() - env.getMin().get2() ) < 1E-11 );
    }

    /**
     * Update the view by evaluating the given key,
     * 
     * @param keyTyped
     *            r/R(reset view), all others will be ignored.
     * @return true if the view should be redrawn, false otherwise.
     */
    public boolean updateView( char keyTyped ) {
        boolean changed = true;
        if ( keyTyped == 'r' || keyTyped == 'R' ) {
            calcViewParameters();
            changed = true;
        }
        return changed;

    }

    /*
     * Draws a colored cube with cubeHalf length defined by the ObjectSize.
     */
    private void drawCube( GL gl ) {
        gl.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        gl.glDisable( GL.GL_BLEND );
        float[] color = new float[] { 1, 0, 0 };
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, color, 0 );

        gl.glBegin( GL.GL_QUADS );
        // Front face
        gl.glNormal3f( 0, 0, 1 );
        gl.glVertex3fv( cubeData[0], 0 );
        gl.glVertex3fv( cubeData[1], 0 );
        gl.glVertex3fv( cubeData[2], 0 );
        gl.glVertex3fv( cubeData[3], 0 );
        // Back face
        gl.glNormal3f( 0, 0, -1 );
        gl.glVertex3fv( cubeData[5], 0 );
        gl.glVertex3fv( cubeData[4], 0 );
        gl.glVertex3fv( cubeData[7], 0 );
        gl.glVertex3fv( cubeData[6], 0 );
        gl.glEnd();

        color = new float[] { 0, 1, 0 };
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, color, 0 );

        gl.glBegin( GL.GL_QUADS );
        // Left face
        gl.glNormal3f( -1, 0, 0 );
        gl.glVertex3fv( cubeData[4], 0 );
        gl.glVertex3fv( cubeData[0], 0 );
        gl.glVertex3fv( cubeData[3], 0 );
        gl.glVertex3fv( cubeData[7], 0 );
        // Right face
        gl.glNormal3f( 1, 0, 0 );
        gl.glVertex3fv( cubeData[1], 0 );
        gl.glVertex3fv( cubeData[5], 0 );
        gl.glVertex3fv( cubeData[6], 0 );
        gl.glVertex3fv( cubeData[2], 0 );
        gl.glEnd();

        color = new float[] { 0, 0, 1 };
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, color, 0 );
        gl.glBegin( GL.GL_QUADS );
        // Top face
        gl.glNormal3f( 0, 1, 0 );
        gl.glVertex3fv( cubeData[3], 0 );
        gl.glVertex3fv( cubeData[2], 0 );
        gl.glVertex3fv( cubeData[6], 0 );
        gl.glVertex3fv( cubeData[7], 0 );
        // Bottom face
        gl.glNormal3f( 0, -1, 0 );
        gl.glVertex3fv( cubeData[4], 0 );
        gl.glVertex3fv( cubeData[5], 0 );
        gl.glVertex3fv( cubeData[1], 0 );
        gl.glVertex3fv( cubeData[0], 0 );
        gl.glEnd();
        gl.glEnable( GL.GL_BLEND );
        gl.glPopAttrib();
    }

    @Override
    public void displayChanged( GLAutoDrawable d, boolean modeChanged, boolean deviceChanged ) {
        // nothing to do
    }

    @Override
    public void init( GLAutoDrawable d ) {
        d.setGL( new DebugGL( d.getGL() ) );
        GL gl = d.getGL();
        gl.glClearColor( 0.7f, 0.7f, 1f, 0 );

        float[] lightAmbient = { 0.4f, 0.4f, 0.4f, 1.0f };
        float[] lightDiffuse = { 0.8f, 0.8f, 0.8f, 1.0f };
        float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };
        float[] lightPosition = { 0.0f, 0.0f, 10.0f, 1.0f };

        gl.glLightfv( GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_SPECULAR, lightSpecular, 0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0 );

        gl.glEnable( GL.GL_DEPTH_TEST );
        gl.glEnable( GL.GL_LIGHT0 );
        gl.glEnable( GL.GL_LIGHTING );
        gl.glEnable( GL.GL_BLEND ); // enable color and texture blending
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );

        gl.glMatrixMode( GL.GL_MODELVIEW );
        gl.glLoadIdentity();
        gl.glEnableClientState( GL.GL_NORMAL_ARRAY );
        gl.glEnableClientState( GL.GL_VERTEX_ARRAY );

    }

    @Override
    public void reshape( GLAutoDrawable d, int x, int y, int width, int height ) {
        this.width = width;
        this.height = height;
        GL gl = d.getGL();
        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity();
        glu.gluPerspective( fov, (float) width / height, nearClippingPlane, farClippingPlane );
        gl.glMatrixMode( GL.GL_MODELVIEW );

    }

    /**
     * @return the trackBall
     */
    public TrackBall getTrackBall() {
        return trackBall;
    }

    /**
     *
     */
    public void removeAllData() {
        worldRenderableObjects.clear();
        bbox = getDefaultBBox();
        calcViewParameters();
    }

    /**
     * @return the renderTestObject
     */
    public final boolean isTestObjectRendered() {
        return renderTestObject;
    }

    /**
     * @param renderTestObject
     *            the renderTestObject to set
     */
    public final void renderTestObject( boolean renderTestObject ) {
        this.renderTestObject = renderTestObject;
    }

}
