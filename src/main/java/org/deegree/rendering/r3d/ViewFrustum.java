//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/r3d/QualityModel.java $
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
package org.deegree.rendering.r3d;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Models a frustum volume, commonly used for view frustums (space volume visible to a viewer of a 3D scene).
 * <p>
 * Offers convenient methods for view-frustum culling ({@link #intersects(double[][])}, {@link #intersects(float[][])}),
 * viewer-relative movements ({@link #moveForward(double)}, {@link #moveRight(double)}, {@link #moveUp}) as well as
 * rotations ({@link #rotateX(double)}, {@link #rotateY(double)}, {@link #rotateZ(double)}).
 * </p>
 * <p>
 * NOTE: The viewer-local coordinate system is modelled as a right-handed one, which must be taken into account when
 * using the rotation methods: the Z-axis is oriented towards the opposite viewing direction.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class ViewFrustum {

    private static final double RAD_90 = Math.toRadians( 90 );

    private static final double RAD_180 = Math.toRadians( 180 );

    private static final double RAD_270 = Math.toRadians( 270 );

    private static final double RAD_360 = Math.toRadians( 360 );

    private double fovy;

    private double aspect;

    private double zNear;

    private double zFar;

    private Point3d eye;

    private Point3d lookingAt;

    private Vector3d up, backward, right;

    /**
     * near top left
     */
    public Point3d ntl;

    /**
     * near top right
     */
    public Point3d ntr;

    /**
     * near bottom left
     */
    public Point3d nbl;

    /**
     * near bottom right
     */
    public Point3d nbr;

    /**
     * far top left
     */
    public Point3d ftl;

    /**
     * far top right
     */
    public Point3d ftr;

    /**
     * far bottom left
     */
    public Point3d fbl;

    /**
     * far bottom right
     */
    public Point3d fbr;

    private double tang;

    private double nw, nh, fw, fh;

    // the following constants and members are needed for calculations
    private static final double ANG2RAD = Math.PI / 180;

    private static final int OUTSIDE = 0;

    private static final int INTERSECT = 1;

    private static final int INSIDE = 2;

    private Plane pl[] = new Plane[6];

    private static final int TOP = 0;

    private static final int BOTTOM = 1;

    private static final int LEFT = 2;

    private static final int RIGHT = 3;

    private static final int NEARP = 4;

    private static final int FARP = 5;

    /**
     * Create a viewFrustum from the given parameters.
     * 
     * @param eye
     * @param lookingAt
     * @param up
     * @param fovy
     *            the field-of-view in y direction
     * @param aspect
     * @param zNear
     * @param zFar
     */
    public ViewFrustum( Point3d eye, Point3d lookingAt, Vector3d up, double fovy, double aspect, double zNear,
                        double zFar ) {
        setPerspectiveParams( fovy, aspect, zNear, zFar );
        setCameraParams( eye, lookingAt, up );
    }

    /**
     * Create a view Frustum by using the given roll, pitch, yaw and distance to the point of interest (looking at), to
     * calculate the eye and the up vector.
     * 
     * @param pitch
     * @param yaw
     * @param roll
     * @param distance
     * @param lookingAt
     * @param fovy
     *            the field-of-view in y direction
     * @param aspect
     * @param zNear
     * @param zFar
     */
    public ViewFrustum( double pitch, double yaw, double roll, double distance, Point3d lookingAt, double fovy,
                        double aspect, double zNear, double zFar ) {
        Point3d calculatedEye = calcObserPosition( lookingAt, pitch, yaw, distance );
        Vector3d calculatedUp = calcUp( roll, calculatedEye, lookingAt );

        setPerspectiveParams( fovy, aspect, zNear, zFar );
        setCameraParams( calculatedEye, lookingAt, calculatedUp );
    }

    /**
     * Calculate the position of the viewer regarding the yaw and the pitch.
     * 
     * @param pointOfInterest
     * @param pitch
     * @param yaw
     * @param distance
     * @return the position of the viewer.
     */
    private Point3d calcObserPosition( Point3d pointOfInterest, double pitch, double yaw, double distance ) {
        double z = Math.sin( pitch ) * distance;

        double groundLength = Math.sqrt( ( distance * distance ) - ( z * z ) );
        double x = 0;
        double y = 0;
        // -1-> if yaw is null, we're looking to the north
        if ( yaw >= 0 && yaw < RAD_90 ) {
            x = -1 * ( Math.sin( yaw ) * groundLength );
            y = -1 * ( Math.cos( yaw ) * groundLength );
        } else if ( yaw >= RAD_90 && yaw < RAD_180 ) {
            double littleYaw = yaw - RAD_90;
            y = Math.sin( littleYaw ) * groundLength;
            x = -1 * ( Math.cos( littleYaw ) * groundLength );
        } else if ( yaw >= RAD_180 && yaw < RAD_270 ) {
            double littleYaw = yaw - RAD_180;
            x = Math.sin( littleYaw ) * groundLength;
            y = Math.cos( littleYaw ) * groundLength;
        } else if ( yaw >= RAD_270 && yaw < RAD_360 ) {
            double littleYaw = yaw - RAD_270;
            y = -1 * ( Math.sin( littleYaw ) * groundLength );
            x = Math.cos( littleYaw ) * groundLength;
        }

        return new Point3d( pointOfInterest.x + x, pointOfInterest.y + y, pointOfInterest.z + z );

    }

    /**
     * The up vector only depends on the roll
     * 
     * @param roll
     * @return the cameras up-vector.
     */
    private Vector3d calcUp( double roll, Point3d eye, Point3d poi ) {

        Vector3d newUP = new Vector3d( 0, 0, 1 );
        if ( Math.abs( roll ) > 1E-10 ) {
            Matrix3d mat = new Matrix3d();
            mat.rotX( roll );
            mat.transform( newUP );
        }

        Vector3d resultUp = new Vector3d();
        Vector3d viewDir = new Vector3d();
        viewDir.sub( eye, poi );
        viewDir.normalize();
        double dot = newUP.dot( viewDir );
        if ( Math.abs( dot ) < 1E-10 ) {
            return newUP;
        }
        viewDir.scale( dot );
        resultUp.sub( newUP, viewDir );
        resultUp.normalize();
        return resultUp;

    }

    /**
     * @return the eye position
     */
    public Point3d getEyePos() {
        return eye;
    }

    /**
     * @return the poi
     */
    public Point3d getLookingAt() {
        return lookingAt;
    }

    /**
     * @return the up vector
     */
    public Vector3d getUp() {
        return up;
    }

    /**
     * @return the right vector
     */
    public Vector3d getRight() {
        return right;
    }

    /**
     * @return the backward vector
     */
    public Vector3d getBackward() {
        return backward;
    }

    /**
     * @return the field-of-view in y direction
     */
    public double getFOVY() {
        return fovy;
    }

    /**
     * @return the near clipping plane.
     */
    public double getZNear() {
        return zNear;
    }

    /**
     * @return the far clipping plane
     */
    public double getZFar() {
        return zFar;
    }

    /**
     * @param box
     * @return true if the box intersect with this viewfrustum.
     */
    public boolean intersects( double[][] box ) {
        int classification = intersectsFrustum( box );
        if ( classification == OUTSIDE ) {
            return false;
        }
        return true;
    }

    /**
     * @param box
     * @return true if the box intersect with this viewfrustum.
     */
    public boolean intersects( float[][] box ) {
        int classification = intersectsFrustum( box );
        if ( classification == OUTSIDE ) {
            return false;
        }
        return true;
    }

    /**
     * @param box
     * @return true if the box intersect with this viewfrustum.
     */
    public boolean intersects( float[] box ) {
        int classification = intersectsFrustum( box );
        if ( classification == OUTSIDE ) {
            return false;
        }
        return true;
    }

    private int intersectsFrustum( double[][] box ) {

        int result = INSIDE;
        // for each plane do ...
        for ( int i = 0; i < 6; i++ ) {
            // is the positive vertex outside?
            Point3d pVertex = pl[i].getPositiveVertex( box );
            Point3d nVertex = pl[i].getNegativeVertex( box );
            if ( pl[i].distance( pVertex ) < 0 )
                return OUTSIDE;
            // is the negative vertex outside?
            else if ( pl[i].distance( nVertex ) < 0 )
                result = INTERSECT;
        }
        return result;
    }

    private int intersectsFrustum( float[][] box ) {

        int result = INSIDE;
        // for each plane do ...
        for ( int i = 0; i < 6; i++ ) {
            // is the positive vertex outside?
            Point3d pVertex = pl[i].getPositiveVertex( box );
            Point3d nVertex = pl[i].getNegativeVertex( box );
            if ( pl[i].distance( pVertex ) < 0 )
                return OUTSIDE;
            // is the negative vertex outside?
            else if ( pl[i].distance( nVertex ) < 0 )
                result = INTERSECT;
        }
        return result;
    }

    private int intersectsFrustum( float[] box ) {
        int result = INSIDE;
        // for each plane do ...
        float[] t = new float[3];
        for ( int i = 0; i < 6; i++ ) {
            // is the positive vertex outside?
            pl[i].getPositiveVertex( box, t );
            if ( pl[i].distance( t ) < 0 ) {
                return OUTSIDE;
            }
            // is the negative vertex outside?
            pl[i].getNegativeVertex( box, t );
            if ( pl[i].distance( t ) < 0 ) {
                result = INTERSECT;
            }
        }
        return result;
    }

    /**
     * Sets the view frustum parameters that correspond to the perspective transformation matrix.
     * <p>
     * The parameters correspond to those of the OpenGL <code>gluPerspective()</code> function.
     * </p>
     * <p>
     * NOTE: When this method is called, it is necessary to call {@link #setCameraParams(Point3d, Point3d, Vector3d)}
     * afterwards, so the internal state is consistent.
     * </p>
     * 
     * @param fovy
     * @param aspect
     * @param zNear
     * @param zFar
     */
    public void setPerspectiveParams( double fovy, double aspect, double zNear, double zFar ) {

        this.fovy = fovy;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;

        // compute width and height of the near and far plane sections
        tang = Math.tan( ANG2RAD * fovy * 0.5 );
        nh = zNear * tang;
        nw = nh * aspect;
        fh = zFar * tang;
        fw = fh * aspect;
    }

    /**
     * Sets the view frustum parameters that correspond to the viewing transform.
     * <p>
     * The parameters correspond to those of the OpenGL <code>gluLookAt()</code> function.
     * 
     * @param eye
     * @param lookingAt
     * @param up
     */
    public void setCameraParams( Point3d eye, Point3d lookingAt, Vector3d up ) {

        this.eye = eye;
        this.lookingAt = lookingAt;
        this.up = up;
        this.backward = new Vector3d( eye );
        this.backward.sub( lookingAt );
        this.backward.normalize();

        this.right = new Vector3d();
        right.cross( up, backward );
        right.normalize();

        // compute the center points of the near and far planes
        Point3d nc = new Point3d( eye );
        Vector3d ZnearD = new Vector3d( backward );
        ZnearD.scale( zNear );
        nc.sub( ZnearD );

        Point3d fc = new Point3d( eye );
        Vector3d ZfarD = new Vector3d( backward );
        ZfarD.scale( zFar );
        fc.sub( ZfarD );

        // compute the 4 corners of the frustum on the near plane
        Vector3d Ynh = new Vector3d( up );
        Ynh.scale( nh );
        Vector3d Xnw = new Vector3d( right );
        Xnw.scale( nw );
        ntl = new Point3d( nc );
        ntl.add( Ynh );
        ntl.sub( Xnw );
        ntr = new Point3d( nc );
        ntr.add( Ynh );
        ntr.add( Xnw );
        nbl = new Point3d( nc );
        nbl.sub( Ynh );
        nbl.sub( Xnw );
        nbr = new Point3d( nc );
        nbr.sub( Ynh );
        nbr.add( Xnw );

        // compute the 4 corners of the frustum on the far plane
        Vector3d Yfh = new Vector3d( up );
        Yfh.scale( fh );
        Vector3d Xfw = new Vector3d( right );
        Xfw.scale( fw );
        ftl = new Point3d( fc );
        ftl.add( Yfh );
        ftl.sub( Xfw );
        ftr = new Point3d( fc );
        ftr.add( Yfh );
        ftr.add( Xfw );
        fbl = new Point3d( fc );
        fbl.sub( Yfh );
        fbl.sub( Xfw );
        fbr = new Point3d( fc );
        fbr.sub( Yfh );
        fbr.add( Xfw );

        // compute the six planes
        // the function set3Points assumes that the points
        // are given in counter clockwise order
        pl[TOP] = new Plane( ntr, ntl, ftl );
        pl[BOTTOM] = new Plane( nbl, nbr, fbr );
        pl[LEFT] = new Plane( ntl, nbl, fbl );
        pl[RIGHT] = new Plane( nbr, ntr, fbr );
        pl[NEARP] = new Plane( ntl, ntr, nbr );
        pl[FARP] = new Plane( ftr, ftl, fbl );
    }

    /**
     * Move the viewfrustum to the right (according to the view direction).
     * 
     * @param delta
     */
    public void moveRight( double delta ) {
        Vector3d deltaVector = new Vector3d( right );
        deltaVector.scale( delta );
        eye.add( deltaVector );
        lookingAt.add( deltaVector );
        setCameraParams( eye, lookingAt, up );
    }

    /**
     * Move the viewfrustum to the up (according to the view direction).
     * 
     * @param delta
     */
    public void moveUp( double delta ) {
        Vector3d deltaVector = new Vector3d( up );
        deltaVector.scale( delta );
        eye.add( deltaVector );
        lookingAt.add( deltaVector );
        setCameraParams( eye, lookingAt, up );
    }

    /**
     * Move the viewfrustum to the forward (according to the view direction).
     * 
     * @param delta
     */
    public void moveForward( double delta ) {
        Vector3d deltaVector = new Vector3d( backward );
        deltaVector.scale( -delta );
        eye.add( deltaVector );
        lookingAt.add( deltaVector );
        setCameraParams( eye, lookingAt, up );
    }

    /**
     * Rotate the viewfrustum around the x-axis (according to the view direction). pitch
     * 
     * @param delta
     */
    public void rotateX( double delta ) {
        rotate( up, right, delta );
        rotate( backward, right, delta );

        lookingAt = new Point3d( eye );
        lookingAt.sub( backward );

        setCameraParams( eye, lookingAt, up );
    }

    /**
     * Rotate the viewfrustum around the y-axis (according to the view direction). roll
     * 
     * @param delta
     */
    public void rotateY( double delta ) {
        rotate( right, up, delta );
        rotate( backward, up, delta );

        lookingAt = new Point3d( eye );
        lookingAt.sub( backward );

        setCameraParams( eye, lookingAt, up );
    }

    /**
     * Rotate the viewfrustum around the z-axis (according to the view direction). yaw
     * 
     * @param delta
     */
    public void rotateZ( double delta ) {
        rotate( right, backward, delta );
        rotate( up, backward, delta );

        setCameraParams( eye, lookingAt, up );
    }

    /**
     * Rotates the point around the given axis and angle.
     * 
     * @param axis
     * @param angle
     *            rotation angle in radians
     */
    private final void rotate( Vector3d p, Vector3d axis, double angle ) {

        double sin = Math.sin( angle );
        double cos = Math.cos( angle );

        double a11 = axis.x * axis.x * ( 1 - cos ) + cos;
        double a12 = axis.x * axis.y * ( 1 - cos ) - axis.z * sin;
        double a13 = axis.x * axis.z * ( 1 - cos ) + axis.y * sin;
        double a21 = axis.x * axis.y * ( 1 - cos ) + axis.z * sin;
        double a22 = axis.y * axis.y * ( 1 - cos ) + cos;
        double a23 = axis.y * axis.z * ( 1 - cos ) - axis.x * sin;
        double a31 = axis.x * axis.z * ( 1 - cos ) - axis.y * sin;
        double a32 = axis.y * axis.z * ( 1 - cos ) + axis.x * sin;
        double a33 = axis.z * axis.z * ( 1 - cos ) + cos;

        double[] rotated = new double[3];
        rotated[0] = p.x * a11 + p.y * a12 + p.z * a13;
        rotated[1] = p.x * a21 + p.y * a22 + p.z * a23;
        rotated[2] = p.x * a31 + p.y * a32 + p.z * a33;
        p.set( rotated );
    }

    @Override
    public String toString() {
        return "{eye=" + eye + ",lookingAt=" + lookingAt + ",up=" + up + ",zNear=" + zNear + ",zFar=" + zFar
               + ",aspect=" + aspect + ",backward=" + backward + "}";
    }

    /**
     * @return a string representation of this view frustums initialization parameters.
     */
    public String toInitString() {
        String s = "Point3d eye = new Point3d( " + eye.x + "," + eye.y + "," + eye.z + ");\n";
        s += "Point3d lookingAt = new Point3d( " + lookingAt.x + "," + lookingAt.y + "," + lookingAt.z + ");\n";
        s += "Vector3d viewerUp = new Vector3d( " + up.x + "," + up.y + "," + up.z + ");\n";
        return s;
    }

    /**
     * Used to represent one surface of the frustum.
     */
    private class Plane {

        private Vector3d n;

        private double A, B, C, D;

        Plane( Point3d p0, Point3d p1, Point3d p2 ) {

            Vector3d v = new Vector3d( p1 );
            v.sub( p0 );
            Vector3d u = new Vector3d( p2 );
            u.sub( p0 );
            n = new Vector3d();
            n.cross( v, u );
            n.normalize();

            A = n.x;
            B = n.y;
            C = n.z;

            Vector3d minusN = new Vector3d( n );
            minusN.scale( -1.0 );
            D = minusN.dot( new Vector3d( p0 ) );
        }

        final Point3d getPositiveVertex( double[][] box ) {
            Point3d p = new Point3d( box[0][0], box[0][1], box[0][2] );
            if ( n.x >= 0.0 ) {
                p.x = box[1][0];
            }
            if ( n.y >= 0.0 ) {
                p.y = box[1][1];
            }
            if ( n.z >= 0.0 ) {
                p.z = box[1][2];
            }
            return p;
        }

        final Point3d getPositiveVertex( float[][] box ) {
            Point3d p = new Point3d( box[0][0], box[0][1], box[0][2] );
            if ( n.x >= 0.0 ) {
                p.x = box[1][0];
            }
            if ( n.y >= 0.0 ) {
                p.y = box[1][1];
            }
            if ( n.z >= 0.0 ) {
                p.z = box[1][2];
            }
            return p;
        }

        final void getPositiveVertex( float[] box, float[] result ) {
            System.arraycopy( box, 0, result, 0, result.length );
            if ( n.x >= 0.0 ) {
                result[0] = box[3];
            }
            if ( n.y >= 0.0 ) {
                result[1] = box[4];
            }
            if ( n.z >= 0.0 ) {
                result[2] = box[5];
            }
        }

        final Point3d getNegativeVertex( double[][] box ) {
            Point3d p = new Point3d( box[1][0], box[1][1], box[1][2] );
            if ( n.x >= 0.0 ) {
                p.x = box[0][0];
            }
            if ( n.y >= 0.0 ) {
                p.y = box[0][1];
            }
            if ( n.z >= 0.0 ) {
                p.z = box[0][2];
            }
            return p;
        }

        final Point3d getNegativeVertex( float[][] box ) {
            Point3d p = new Point3d( box[1][0], box[1][1], box[1][2] );
            if ( n.x >= 0.0 ) {
                p.x = box[0][0];
            }
            if ( n.y >= 0.0 ) {
                p.y = box[0][1];
            }
            if ( n.z >= 0.0 ) {
                p.z = box[0][2];
            }
            return p;
        }

        final void getNegativeVertex( float[] box, float[] result ) {
            System.arraycopy( box, 3, result, 0, result.length );
            if ( n.x >= 0.0 ) {
                result[0] = box[0];
            }
            if ( n.y >= 0.0 ) {
                result[1] = box[1];
            }
            if ( n.z >= 0.0 ) {
                result[2] = box[2];
            }
        }

        final double distance( Point3d p ) {
            return A * p.x + B * p.y + C * p.z + D;
        }

        final double distance( float[] p ) {
            return A * p[0] + B * p[1] + C * p[2] + D;
        }
    }
}
