//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/r3d/QualityModel.java $
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
package org.deegree.rendering.r3d;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Models a frustum volume, commonly used for view frustums (space volume visible to a viewer of a 3D scene).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class ViewFrustum {

    private double fovy;

    private double aspect;

    private double zNear;

    private double zFar;

    private Point3d eye;

    private Point3d center;

    private Vector3d up, forward, right;

    public Point3d ntl, ntr, nbl, nbr, ftl, ftr, fbl, fbr;

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

    public ViewFrustum( double fovy, double aspect, double zNear, double zFar, Point3d eye, Point3d center, Vector3d up ) {
        setPerspectiveParams( fovy, aspect, zNear, zFar );
        setCameraParams( eye, center, up );
    }

    public Point3d getEyePos() {
        return eye;
    }

    public Point3d getLookingAt() {
        return center;
    }

    public Vector3d getViewerUp() {
        return up;
    }

    public double getFOVY() {
        return fovy;
    }

    public double getZNear() {
        return zNear;
    }

    public double getZFar() {
        return zFar;
    }

    public boolean intersects( double[][] box ) {
        int classification = intersectsFrustum( box );
        if ( classification == OUTSIDE ) {
            return false;
        }
        return true;
    }

    public boolean intersects( float[][] box ) {
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

    /**
     * Sets the view frustum parameters that correspond to the perspective transformation matrix.
     * <p>
     * The parameters correspond to those of the OpenGL <code>gluPerspective()</code> function.
     * <p>
     * NOTE: When this method is called, it is necessary to call {@link #setCameraParams(Point3d, Point3d, Vector3d)} as
     * well.
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
     * @param center
     * @param up
     */
    public void setCameraParams( Point3d eye, Point3d center, Vector3d up ) {

        this.eye = eye;
        this.center = center;
        this.up = up;
        this.forward = new Vector3d( eye );
        this.forward.sub( center );
        this.forward.normalize();

        this.right = new Vector3d();
        right.cross( up, forward );
        right.normalize();

        // compute the Z axis of camera
        // this axis points in the opposite direction from
        // the looking direction
        Vector3d Z = new Vector3d( eye );
        Z.sub( center );
        Z.normalize();

        // X axis of camera with given "up" vector and Z axis
        Vector3d X = new Vector3d();
        X.cross( up, Z );
        X.normalize();

        // the real "up" vector is the cross product of Z and X
        Vector3d Y = new Vector3d();
        Y.cross( Z, X );

        // compute the centers of the near and far planes
        Point3d nc = new Point3d( eye );
        Vector3d ZnearD = new Vector3d( Z );
        ZnearD.scale( zNear );
        nc.sub( ZnearD );

        Point3d fc = new Point3d( eye );
        Vector3d ZfarD = new Vector3d( Z );
        ZfarD.scale( zFar );
        fc.sub( ZfarD );

        // compute the 4 corners of the frustum on the near plane
        Vector3d Ynh = new Vector3d( Y );
        Ynh.scale( nh );
        Vector3d Xnw = new Vector3d( X );
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
        Vector3d Yfh = new Vector3d( Y );
        Yfh.scale( fh );
        Vector3d Xfw = new Vector3d( X );
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

    public void moveX( double delta ) {
        Vector3d deltaVector = new Vector3d( right );
        deltaVector.scale( delta );
        eye.add( deltaVector );
        center.add( deltaVector );
        setCameraParams( eye, center, up );
    }

    public void moveY( double delta ) {
        Vector3d deltaVector = new Vector3d( up );
        deltaVector.scale( delta );
        eye.add( deltaVector );
        center.add( deltaVector );
        setCameraParams( eye, center, up );
    }

    public void moveZ( double delta ) {
        Vector3d deltaVector = new Vector3d( forward );
        deltaVector.scale( delta );
        eye.sub( deltaVector );
        center.sub( deltaVector );
        setCameraParams( eye, center, up );
    }

    public void rotateX( double delta ) {
        rotate( up, right, delta );
        rotate( forward, right, delta );

        center = new Point3d( eye );
        center.add( forward );

        setCameraParams( eye, center, up );
    }

    public void rotateY( double delta ) {
        rotate( right, up, delta );
        rotate( forward, up, delta );

        center = new Point3d( eye );
        center.add( forward );

        setCameraParams( eye, center, up );
    }

    public void rotateZ( double delta ) {
        rotate( right, forward, delta );
        rotate( up, forward, delta );

        setCameraParams( eye, center, up );
    }

    /**
     * Rotates the point around the given axis and angle.
     * 
     * @param axis
     * @param angle
     *            rotation angle in radians
     */
    private void rotate( Vector3d p, Vector3d axis, double angle ) {

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
        return "{eye=" + eye + ",lookingAt=" + center + ",zNear=" + zNear + ",zFar=" + zFar + ",aspect=" + aspect
               + ",forward=" + forward + "}";
    }

    public String toInitString() {
        String s = "Point3d eye = new Point3d( " + eye.x + "," + eye.y + "," + eye.z + ");\n";
        s += "Vector3d lookingAt = new Vector3d( " + center.x + "," + center.y + "," + center.z + ");\n";
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

        Point3d getPositiveVertex( double[][] box ) {
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

        Point3d getPositiveVertex( float[][] box ) {
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

        Point3d getNegativeVertex( double[][] box ) {
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

        Point3d getNegativeVertex( float[][] box ) {
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

        double distance( Point3d p ) {
            return A * p.x + B * p.y + C * p.z + D;
        }
    }
}
