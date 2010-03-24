//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/tools/trunk/src/org/deegree/tools/rendering/TrackBall.java $
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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TrackBall</code> pretends that a ball encloses the 3d view. You roll this ball with the mouse. For
 * example, if you click on the center of the ball and move the ball directly right, you rotate around y. Click on edge
 * of ball and roll to get a z rotation.
 *
 * The idea isn't too hard. Start with a vector from the first mouse click to the center of the 3d view. Set the radius
 * of the ball to the smaller dimension of the 3d view. As you drag around, a second vector is determined from the
 * surface to center of the ball. Axis of rotation is cross-product of those two vectors, and the angle is the angle
 * between the vectors.
 *
 * This class was copied from a cpp file I once used in my Computer Graphic classes. I don't know who the original
 * author was, but since I only created the java representation of the working code, I would like to thank him/her.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author: rbezema $
 *
 * @version $Revision: 15531 $, $Date: 2009-01-07 15:05:43 +0100 (Mi, 07 Jan 2009) $
 *
 */
public class TrackBall extends MouseAdapter {
    private final static Logger LOG = LoggerFactory.getLogger( TrackBall.class );

    private static final float kTOL = 0.01f;

    private static final float kRAD2DEG = 180 / 3.1415927f;

    private static final float kDEG2RAD = 3.1415927f / 180;

    private float radius;

    private float[] startPoint;

    private float[] endPoint;

    private Point center;

    private float[] rotationVector;

    private float[] tbRot;

    private boolean isDragging;

    /**
     * Set the rotation matrices to the initial state (0,1,0,0)
     */
    public TrackBall() {
        radius = 0;
        startPoint = new float[4];
        endPoint = new float[4];
        center = new Point();
        rotationVector = new float[] { 0, 1, 0, 0 };
        tbRot = new float[] { 0, 1, 0, 0 };
        isDragging = false;
    }

    @Override
    public void mousePressed( MouseEvent e ) {
        if ( e.getSource() instanceof GLAutoDrawable ) {
            if ( e.getButton() == MouseEvent.BUTTON1 ) {
                if ( isDragging ) {
                    // some event was lost, lets fix it.
                    tbRot[0] = 0;
                    tbRot[1] = 1;
                    tbRot[2] = 0;
                    tbRot[3] = 0;
                }
                start( (GLAutoDrawable) e.getSource(), e.getPoint() );
                isDragging = true;
            }
        }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        if ( e.getSource() instanceof GLAutoDrawable ) {
            if ( e.getButton() == MouseEvent.BUTTON1 ) {
                if ( isDragging ) {
                    addToRotation();
                    // some event was lost, lets fix it.
                    tbRot[0] = 0;
                    tbRot[1] = 1;
                    tbRot[2] = 0;
                    tbRot[3] = 0;
                    isDragging = false;
                }
            }
        }
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
        if ( e.getSource() instanceof GLAutoDrawable ) {
            if ( isDragging ) {
                float[] rot = rollTo( e.getPoint() );
                if ( rot == null ) {
                    return;
                }
                rotateBy( rot );
                ( (GLAutoDrawable) e.getSource() ).display();
            }
        }
    }

    /**
     * Multiply the rotation vectors on top of the current context, the multiplication also uses the translation
     *
     * @param context
     *            to multiply the rotations upon.
     * @param worldTranslation
     *            a float[3] which contains a translation vector (normally the center of the scene ), if null or length !=
     *            3 no translation will be done.
     */
    @SuppressWarnings("null")
    public void multModelMatrix( GL context, float[] worldTranslation ) {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "worldTranslation: " + worldTranslation[0] + "," + worldTranslation[1] + ","
                       + worldTranslation[2] );
            LOG.debug( "tbRot: " + tbRot[0] + "," + tbRot[1] + "," + tbRot[2] + "," + tbRot[3] );
            LOG.debug( "rotationVector: " + rotationVector[0] + "," + rotationVector[1] + "," + rotationVector[2] + ","
                       + rotationVector[3] );
        }
        boolean translate = ( worldTranslation != null ) && ( worldTranslation.length == 3 );
        if ( translate ) {
            context.glTranslatef( worldTranslation[0], worldTranslation[1], worldTranslation[2] );
        }
        context.glRotatef( tbRot[0], tbRot[1], tbRot[2], tbRot[3] );
        context.glRotatef( rotationVector[0], rotationVector[1], rotationVector[2], rotationVector[3] );
        if ( translate ) {
            context.glTranslatef( -worldTranslation[0], -worldTranslation[1], -worldTranslation[2] );
        }
    }

    /**
     * Reset the rotation vectors
     */
    public void reset() {
        rotationVector[0] = tbRot[0] = 0;
        rotationVector[1] = tbRot[1] = 1;
        rotationVector[2] = tbRot[2] = 0;
        rotationVector[3] = tbRot[3] = 0;
    }

    /**
     * Initialize a new rotation by calculating the startposition on as well as the center and radius of the (imaginary)
     * sphere.
     *
     * @param drawable
     *            to get the window height and width from.
     * @param point
     */
    private void start( GLAutoDrawable drawable, Point point ) {
        int windowWidth = drawable.getWidth();// glutGet(GLU. GLUT_WINDOW_WIDTH);
        int windowHeight = drawable.getHeight();// glutGet(GLUT.GLUT_WINDOW_HEIGHT);

        // Adjust the window width/height so that the ball won't be filling the whole window,
        // if you solemnly want to rotate the z axis, you need to be outside of the sphere 'all-the-time'

        windowWidth -= ( windowWidth * 0.1 );
        windowHeight -= ( windowHeight * 0.1 );
        if ( windowWidth > windowHeight ) {
            radius = windowHeight * 0.5f;
        } else {
            radius = windowWidth * 0.5f;
        }

        // center of view
        center.setLocation( ( drawable.getWidth() * 0.5f ), ( drawable.getHeight() * 0.5f ) );

        // starting vector from surface of ball to center
        startPoint[0] = ( point.x - center.x );
        startPoint[1] = ( center.y - point.y );
        float xxyy = startPoint[0] * startPoint[0] + startPoint[1] * startPoint[1];
        if ( xxyy > ( radius * radius ) ) {
            startPoint[2] = 0;
        } else {
            startPoint[2] = ( (float) Math.sqrt( ( radius * radius ) - xxyy ) );
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "width: " + windowWidth );
            LOG.debug( "height: " + windowHeight );
            LOG.debug( "center: " + center );
            LOG.debug( "radius: " + radius );
            LOG.debug( "startpoint: " + startPoint[0] + "," + startPoint[1] + "," + startPoint[2] );
        }
    }

    /**
     * Calculate the current rotation while dragging, by identifying the current position relative to the start position
     * and normalizing the rotation axis calculate using simple trigonometry.
     *
     * @param mousePosition
     *            while dragging
     * @return <code>null</code> if the dragging distance was to small, otherwise the new temporary rotation axis
     *         since the last rotation.
     */
    private float[] rollTo( Point mousePosition ) {

        endPoint[0] = ( mousePosition.x - center.x );
        endPoint[1] = ( center.y - mousePosition.y );
        // below tolerance
        if ( Math.abs( endPoint[0] - startPoint[0] ) < kTOL && Math.abs( endPoint[1] - startPoint[1] ) < kTOL ) {
            return null;
        }

        // ending vector from surface of ball to center
        float xxyy = endPoint[0] * endPoint[0] + endPoint[1] * endPoint[1];
        if ( xxyy > radius * radius ) {
            endPoint[2] = 0; // outside sphere
        } else {
            endPoint[2] = ( (float) Math.sqrt( radius * radius - xxyy ) );
        }

        float[] rot = new float[4];
        // cross vectors rot = st x end
        rot[1] = startPoint[1] * endPoint[2] - startPoint[2] * endPoint[1];
        rot[2] = -1 * startPoint[0] * endPoint[2] + startPoint[2] * endPoint[0];
        rot[3] = startPoint[0] * endPoint[1] - startPoint[1] * endPoint[0];

        // using atan since sin and cos gives rotations that can flip near poles
        // cos(a) = (s . e)/(||s|| ||e||)
        // s . e
        float cosAng = startPoint[0] * endPoint[0] + startPoint[1] * endPoint[1] + startPoint[2] * endPoint[2];
        float ls = (float) Math.sqrt( startPoint[0] * startPoint[0] + startPoint[1] * startPoint[1] + startPoint[2]
                                      * startPoint[2] );
        ls = 1 / ls; // 1 / ||s||
        float le = (float) Math.sqrt( endPoint[0] * endPoint[0] + endPoint[1] * endPoint[1] + endPoint[2] * endPoint[2] );
        le = 1 / le; // 1 / ||e||
        cosAng = cosAng * ls * le;

        // sin = ||(s x e)||/(||s|| ||e||)
        float lr = 0;
        float sinAng = lr = (float) Math.sqrt( rot[1] * rot[1] + rot[2] * rot[2] + rot[3] * rot[3] ); // ||(s x e)||
        sinAng = sinAng * ls * le;
        rot[0] = (float) ( Math.atan2( sinAng, cosAng ) * kRAD2DEG );

        // Normalize rot axis
        lr = 1 / lr;
        for ( int i = 1; i < 4; i++ ) {
            rot[i] *= lr;
        }
        return rot;
    }

    /**
     * Calculate the new rotation vector, which means <code>
     * A' = A . da
     * for quaternions: let q0 <- A, and q1 <- dA.
     * Figure out: q2 = q1 + q0 (note order)
     * </code>
     * if the identity rotation was found ( cos (0.5*angle) == 1 ) the rotation vector will be set to identity
     */
    private void addToRotation() {
        // A' <- q3
        float[] q0 = rotation2Quat( rotationVector );
        float[] q1 = rotation2Quat( tbRot );

        // q2 = q1 + q0
        float[] q2 = new float[4];
        q2[0] = q1[1] * q0[2] - q1[2] * q0[1] + q1[3] * q0[0] + q1[0] * q0[3];
        q2[1] = q1[2] * q0[0] - q1[0] * q0[2] + q1[3] * q0[1] + q1[1] * q0[3];
        q2[2] = q1[0] * q0[1] - q1[1] * q0[0] + q1[3] * q0[2] + q1[2] * q0[3];
        q2[3] = q1[3] * q0[3] - q1[0] * q0[0] - q1[1] * q0[1] - q1[2] * q0[2];

        // Identity rotation is rot by 0 about an axis. "angle" in quaternion is
        // actually cos of 1/2 angle. So, if cos 1/2 angle's 1 (or within tolerance)
        // then you have an identity rotation
        if ( Math.abs( q2[3] - 1. ) < 1.0e-7 ) {
            // id rotation
            rotationVector[0] = 0;
            rotationVector[1] = 1;
            rotationVector[2] = rotationVector[3] = 0.f;
            return;
        }

        // here is non-identity rotation. cos of half-angle is non-zero, so sine is non-zero
        // Therefore, we can divide by sin(theta2) w/o fear

        // turn quat. back to angle/axis rotation
        float theta2 = (float) Math.acos( q2[3] );
        float sinTheta2 = 1 / (float) Math.sin( theta2 );
        rotationVector[0] = theta2 * 2.f * kRAD2DEG;
        rotationVector[1] = q2[0] * sinTheta2;
        rotationVector[2] = q2[1] * sinTheta2;
        rotationVector[3] = q2[2] * sinTheta2;
    }

    /**
     * Set the rotation angle while dragging
     *
     * @param r
     *            the new rotation parameters
     */

    private void rotateBy( float[] r ) {
        for ( int i = 0; i < 4; i++ ) {
            tbRot[i] = r[i];
        }
    }

    /**
     * convert GL rotation to a quaternion. GL looks like: {ang, x, y, z} and quat looks like: {{v}, cos(angle/2)} where
     * {v} is (x,y,z)/sin(angle/2)
     *
     * @param A
     *            The original opengl rotation
     * @return the quaternion
     */
    private float[] rotation2Quat( float[] A ) {
        // half angles
        float ang2 = A[0] * kDEG2RAD * 0.5f;
        float sinAng2 = (float) Math.sin( ang2 );
        float[] result = new float[4];
        result[0] = A[1] * sinAng2;
        result[1] = A[2] * sinAng2;
        result[2] = A[3] * sinAng2;
        result[3] = (float) Math.cos( ang2 );
        return result;
    }

}
