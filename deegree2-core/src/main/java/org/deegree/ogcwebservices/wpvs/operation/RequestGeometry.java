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

package org.deegree.ogcwebservices.wpvs.operation;

import javax.media.j3d.PickShape;
import javax.media.j3d.Transform3D;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcwebservices.wpvs.j3d.ViewPoint;

/**
 * This abstract class represents a geometry needed for Get3DFeatureInfoRequest. It contains the 3d geometry and its 2d
 * projektion. The 2d geometry is required for WFS request, the 3d geometry for the final test of intersection.
 *
 * @version $Revision$
 * @author <a href="mailto:cordes@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 */
public abstract class RequestGeometry {

    /**
     * The the feature info request
     */
    protected Get3DFeatureInfo request;

    // begin point of the view ray
    private Point3d beginPointLine;

    private CoordinateSystem crs;

    /**
     *  geometry for wfs query
     */
    protected Geometry wfsReqGeom;

    /**
     *  3d geometry: needed for final testing
     */
    protected PickShape pickshape;

    /**
     * Constructor to initialize the attributes needed for all geometries
     *
     * @param request
     *            the Get3DFeatureInfoRequest
     */
    public RequestGeometry( Get3DFeatureInfo request ) {
        this.request = request;
        crs = request.getGetViewRequestCopy().getCrs();
        beginPointLine = new ViewPoint( request.getGetViewRequestCopy() ).getObserverPosition();
    }

    /**
     *
     * @return the pickshape or <code>null</code> if it was not set.
     */
    public PickShape getPickshape() {
        return pickshape;
    }

    /**
     * @return the geometry repsonse from the wfs-request or <code>null</code> if it was not set.
     */
    public Geometry getWfsReqGeom() {
        return wfsReqGeom;
    }

    /**
     * @return the starting point of the line.
     */
    public Point3d getBeginPointLine() {
        return beginPointLine;
    }

    /**
     * @return the crs of the request.
     */
    public CoordinateSystem getCrs() {
        return crs;
    }

    /**
     * @return the request.
     */
    public Get3DFeatureInfo getRequest() {
        return request;
    }

    // abstract methods
    /**
     * sets the geometry needed for WFS request
     *
     * @throws GeometryException
     *             if something went wrong.
     */
    public abstract void setWfsReqGeom()
                                        throws GeometryException;

    /**
     * Sets the geometry needed for final test of intersection
     */
    public abstract void setPickshape();

    /**
     * Calculates a ray through the ViewPoint and the ClickPoint.
     *
     * @param viewPoint
     *            position of the request
     * @param depth
     *            far clipping plane
     * @param width
     *            of the request
     * @param height
     *            of the request
     * @param clickI
     *            x-axis
     * @param clickJ
     *            y-axis
     * @return The end point of the requestGeometry.
     */
    protected Point3d calcEndPoint( ViewPoint viewPoint, double depth, int width, int height, int clickI, int clickJ ) {
        // sets extension of the ray
        double extension = viewPoint.getFarClippingPlane();
        if ( depth > 0 && depth < extension ) {
            extension = depth;
        }
        GetView getView = request.getGetViewRequestCopy();

        // rotates the cklickpoint to the roll-angle
        double roll = getView.getRoll();
        double deltaI = -( width / 2 ) + clickI;
        double deltaJ = ( height / 2 ) - clickJ;

        double x = deltaI * Math.cos( roll ) + deltaJ * Math.sin( roll );
        double y = -deltaI * Math.sin( roll ) + deltaJ * Math.cos( roll );

        // calculates the angles to rotate the vector (vp-poi)
        double aov = viewPoint.getAngleOfView();
        double distProj = ( width / 2 ) / Math.tan( aov / 2 );

        double angleI = Math.atan( x / distProj );
        double angleJ = Math.atan( y / distProj );

        Point3d vp = viewPoint.getObserverPosition();
        Point3d poi = viewPoint.getPointOfInterest();
        Vector3d vectorVPtoPOI = new Vector3d();
        vectorVPtoPOI.sub( poi, vp );

        Transform3D trans = new Transform3D();

        // rotation at angleJ (vertikal)
        Vector3d rotJ = new Vector3d();
        Vector3d eZ = new Vector3d( 0, 0, 1 );
        rotJ.cross( vectorVPtoPOI, eZ );
        AxisAngle4d axisAngleJ = new AxisAngle4d( rotJ, angleJ );
        trans.set( axisAngleJ );
        trans.transform( vectorVPtoPOI );

        // rotation at angleI (horizontal)
        Vector3d rotI = new Vector3d();
        rotI.cross( vectorVPtoPOI, rotJ );
        AxisAngle4d axisAngleI = new AxisAngle4d( rotI, angleI );
        trans.set( axisAngleI );
        trans.transform( vectorVPtoPOI );

        // scale to extension
        vectorVPtoPOI.normalize();
        vectorVPtoPOI.scale( extension );
        Point3d endPoint = new Point3d();

        // add to the viewpoint
        endPoint.add( vp, vectorVPtoPOI );
        return endPoint;
    }
}
