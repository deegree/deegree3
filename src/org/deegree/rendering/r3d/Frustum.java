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

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Models a frustum volume, commonly used for view frustums (space volume visible to a viewer of a 3D scene).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class Frustum {

    private float fovy;

    private float aspect;

    private float zNear;

    private float zFar;

    private Point3f eye;

    private Point3f center;

    private Vector3f up;

    // the following constants and members are needed for calculations

    private static final float ANG2RAD = (float) Math.PI / 180;

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

    public Point3f ntl, ntr, nbl, nbr, ftl, ftr, fbl, fbr;

    private float tang;

    private float nw, nh, fw, fh;

    public Frustum( float fovy, float aspect, float zNear, float zFar, Point3f eye, Point3f center, Vector3f up ) {
        setPerspectiveParams( fovy, aspect, zNear, zFar );
        setCameraParams( eye, center, up );
    }

    public Point3f getEyePos() {
        return eye;
    }

    public boolean intersects( float[][] box ) {
        int classification = intersectsFrustum( box );
        if ( classification == OUTSIDE ) {
            return false;
        }
        return true;
    }

    private int intersectsFrustum( float[][] box ) {

        int result = INSIDE;
        // for each plane do ...
        for ( int i = 0; i < 6; i++ ) {
            // is the positive vertex outside?
            Point3f pVertex = pl[i].getPositiveVertex( box );
            Point3f nVertex = pl[i].getNegativeVertex( box );
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
     * NOTE: When this method is called, it is necessary to call {@link #setCameraParams(Point3f, Point3f, Vector3f)} as
     * well.
     * 
     * @param fovy
     * @param aspect
     * @param zNear
     * @param zFar
     */
    public void setPerspectiveParams( float fovy, float aspect, float zNear, float zFar ) {

        this.fovy = fovy;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;

        // compute width and height of the near and far plane sections
        tang = (float) Math.tan( ANG2RAD * fovy * 0.5 );
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
    public void setCameraParams( Point3f eye, Point3f center, Vector3f up ) {

        this.eye = eye;
        this.center = center;
        this.up = up;

        // compute the Z axis of camera
        // this axis points in the opposite direction from
        // the looking direction
        Vector3f Z = new Vector3f( eye );
        Z.sub( center );
        Z.normalize();

        // X axis of camera with given "up" vector and Z axis
        Vector3f X = new Vector3f();
        X.cross( up, Z );
        X.normalize();

        // the real "up" vector is the cross product of Z and X
        Vector3f Y = new Vector3f();
        Y.cross( Z, X );

        // compute the centers of the near and far planes
        Point3f nc = new Point3f( eye );
        Vector3f ZnearD = new Vector3f( Z );
        ZnearD.scale( zNear );
        nc.sub( ZnearD );

        Point3f fc = new Point3f( eye );
        Vector3f ZfarD = new Vector3f( Z );
        ZfarD.scale( zFar );
        fc.sub( ZfarD );

        // compute the 4 corners of the frustum on the near plane
        Vector3f Ynh = new Vector3f( Y );
        Ynh.scale( nh );
        Vector3f Xnw = new Vector3f( X );
        Xnw.scale( nw );
        ntl = new Point3f( nc );
        ntl.add( Ynh );
        ntl.sub( Xnw );
        ntr = new Point3f( nc );
        ntr.add( Ynh );
        ntr.add( Xnw );
        nbl = new Point3f( nc );
        nbl.sub( Ynh );
        nbl.sub( Xnw );
        nbr = new Point3f( nc );
        nbr.sub( Ynh );
        nbr.add( Xnw );

        // compute the 4 corners of the frustum on the far plane
        Vector3f Yfh = new Vector3f( Y );
        Yfh.scale( fh );
        Vector3f Xfw = new Vector3f( X );
        Xfw.scale( fw );
        ftl = new Point3f( fc );
        ftl.add( Yfh );
        ftl.sub( Xfw );
        ftr = new Point3f( fc );
        ftr.add( Yfh );
        ftr.add( Xfw );
        fbl = new Point3f( fc );
        fbl.sub( Yfh );
        fbl.sub( Xfw );
        fbr = new Point3f( fc );
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
     * Used to represent one side of the frustum.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$
     */
    private class Plane {

        private Vector3f n;

        private float A, B, C, D;

        Plane( Point3f p0, Point3f p1, Point3f p2 ) {

            Vector3f v = new Vector3f( p1 );
            v.sub( p0 );
            Vector3f u = new Vector3f( p2 );
            u.sub( p0 );
            n = new Vector3f();
            n.cross( v, u );
            n.normalize();

            A = n.x;
            B = n.y;
            C = n.z;

            Vector3f minusN = new Vector3f( n );
            minusN.scale( -1.0f );
            D = minusN.dot( new Vector3f( p0 ) );
        }

        Point3f getPositiveVertex( float[][] box ) {
            Point3f p = new Point3f( box[0][0], box[0][1], box[0][2] );
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

        Point3f getNegativeVertex( float[][] box ) {
            Point3f p = new Point3f( box[1][0], box[1][1], box[1][2] );
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

        float distance( Point3f p ) {
            return A * p.x + B * p.y + C * p.z + D;
        }
    }

    public float getFOVY() {
        return fovy;
    }
}
