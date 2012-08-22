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
package org.deegree.ogcwebservices.wpvs.j3d;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcwebservices.wpvs.operation.GetView;

/**
 * This class represents the view point for a WPVS request. That is, it represents the point where the observer is at,
 * and looking to a target point. An angle of view must be also given.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 * @version $Revision$ $Date$
 */
public class ViewPoint {

    private static final ILogger LOG = LoggerFactory.getLogger( ViewPoint.class );

    private static final double rad90 = Math.toRadians( 90 );

    private static final double rad180 = Math.toRadians( 180 );

    private static final double rad270 = Math.toRadians( 270 );

    private static final double rad360 = Math.toRadians( 360 );

    private CoordinateSystem crs;

    private Point3d observerPosition;

    private Point3d pointOfInterest;

    private Point3d[] footprint;

    private Point3d[] fakeFootprint;

    private Point3d[] oldFootprint;

    private double angleOfView = 0;

    private double yaw = 0;

    private double pitch = 0;

    private double terrainDistanceToSeaLevel = 0;

    private double viewerToPOIDistance = 0;

    private double farClippingPlane = 0;

    private Transform3D simpleTransform = null;

    private Transform3D viewMatrix = null;

    /**
     * @return the oldFootprint.
     */
    public Point3d[] getOldFootprint() {
        return oldFootprint;
    }

    /**
     * @return the fakeFootprint.
     */
    public Point3d[] getFakeFootprint() {
        return fakeFootprint;
    }

    /**
     * Creates a new instance of ViewPoint_Impl
     *
     * @param yaw
     *            rotation on the Z-Axis in radians of the viewer
     * @param pitch
     *            rotation on the X-Axis in radians
     * @param viewerToPOIDistance
     *            from the point of interest to the viewersposition
     * @param pointOfInterest
     *            the point of interest
     * @param angleOfView
     * @param farClippingPlane
     *            where the view ends
     * @param distanceToSealevel
     * @param crs
     *            The Coordinatesystem in which the given reside
     */
    public ViewPoint( double yaw, double pitch, double viewerToPOIDistance, Point3d pointOfInterest,
                      double angleOfView, double farClippingPlane, double distanceToSealevel, CoordinateSystem crs ) {
        this.yaw = yaw;
        this.pitch = pitch;

        this.angleOfView = angleOfView;
        this.pointOfInterest = pointOfInterest;

        this.viewerToPOIDistance = viewerToPOIDistance;

        this.farClippingPlane = farClippingPlane;

        this.terrainDistanceToSeaLevel = distanceToSealevel;

        this.crs = crs;

        simpleTransform = new Transform3D();

        viewMatrix = new Transform3D();
        observerPosition = new Point3d();

        footprint = new Point3d[4];
        fakeFootprint = new Point3d[4];
        oldFootprint = new Point3d[4];
        calcObserverPosition();

    }

    /**
     * @param request
     *            a server request.
     */
    public ViewPoint( GetView request ) {
        this( request.getYaw(), request.getPitch(), request.getDistance(), request.getPointOfInterest(),
              request.getAngleOfView(), request.getFarClippingPlane(), 0, request.getCrs() );
    }

    /**
     * @param request
     *            a server request.
     * @param distanceToSeaLevel
     */
    public ViewPoint( GetView request, double distanceToSeaLevel ) {
        this( request.getYaw(), request.getPitch(), request.getDistance(), request.getPointOfInterest(),
              request.getAngleOfView(), request.getFarClippingPlane(), distanceToSeaLevel, request.getCrs() );
    }

    /**
     * Calculates the observers position for a given pointOfInterest, distance and view direction( as semi polar
     * coordinates, yaw & pitch ). also recalculating the viewmatrix and the footprint, for they are affected by the
     * change of position.
     *
     */
    private void calcObserverPosition() {

        double z = Math.sin( pitch ) * this.viewerToPOIDistance;

        double groundLength = Math.sqrt( ( viewerToPOIDistance * viewerToPOIDistance ) - ( z * z ) );
        double x = 0;
        double y = 0;
        // -1-> if yaw is null, we're looking to the north
        if ( yaw >= 0 && yaw < rad90 ) {
            x = -1 * ( Math.sin( yaw ) * groundLength );
            y = -1 * ( Math.cos( yaw ) * groundLength );
        } else if ( yaw >= rad90 && yaw < rad180 ) {
            double littleYaw = yaw - rad90;
            y = Math.sin( littleYaw ) * groundLength;
            x = -1 * ( Math.cos( littleYaw ) * groundLength );
        } else if ( yaw >= rad180 && yaw < rad270 ) {
            double littleYaw = yaw - rad180;
            x = Math.sin( littleYaw ) * groundLength;
            y = Math.cos( littleYaw ) * groundLength;
        } else if ( yaw >= rad270 && yaw < rad360 ) {
            double littleYaw = yaw - rad270;
            y = -1 * ( Math.sin( littleYaw ) * groundLength );
            x = Math.cos( littleYaw ) * groundLength;
        }

        observerPosition.x = pointOfInterest.x + x;
        observerPosition.y = pointOfInterest.y + y;
        observerPosition.z = pointOfInterest.z + z;

        calculateViewMatrix();
        calcFootprint();
    }

    /**
     * Calculates the field of view aka footprint, the corner points of the intersection of the field of view with the
     * terrain as follows, <br/>
     * <ul>
     * <li> f[0] = farclippingplane right side fo viewDirection </li>
     * <li> f[1] = farclippingplane left side fo viewDirection </li>
     * <li> f[2] = nearclippingplane right side fo viewDirection, note it can be behind the viewPosition </li>
     * <li> f[3] = nearclippingplane left side fo viewDirection, note it can be behind the viewPosition </li>
     * </ul>
     * <br/> the are rotated and translated according to the simpleTranform
     *
     */
    private void calcFootprint() {

        // make the aov a little bigger, therefor the footprint is larger and no visual errors can
        // be seen at the sides of the view (at the expense of a little larger/more requests)
        double halfAngleOfView = ( angleOfView + ( Math.toRadians( 6 ) ) ) * 0.5;
        if ( halfAngleOfView >= ( rad90 * 0.5 ) ) {
            halfAngleOfView = rad90 * 0.5;
        }
        if ( Math.abs( ( halfAngleOfView + rad90 ) % rad180 ) < 0.000001 ) {
            LOG.logError( "The angle of view can't be a multiple of rad180" );
            return;
        }

        double heightAboveGround = observerPosition.z - ( pointOfInterest.z - terrainDistanceToSeaLevel );
        if ( heightAboveGround < 0 ) { // beneath the ground
            LOG.logError( "the Observer is below the terrain" );
            return;
        }

        if ( pitch >= 0 ) { // the eye is looking down on the poi

            // caluclate the viewFrustums farClippinplane points
            double otherCornerOffset = farClippingPlane * Math.sin( halfAngleOfView );
            double yCornerOffset = farClippingPlane * Math.cos( halfAngleOfView );

            // farclippin plane top right
            Point3d topRight = new Point3d( otherCornerOffset, otherCornerOffset, -yCornerOffset );
            viewMatrix.transform( topRight );
            footprint[0] = findIntersectionWithTerrain( new Vector3d( topRight ) );

            // farclippin plane top left
            Point3d topLeft = new Point3d( -otherCornerOffset, otherCornerOffset, -yCornerOffset );
            viewMatrix.transform( topLeft );
            footprint[1] = findIntersectionWithTerrain( new Vector3d( topLeft ) );

            // farclippin plane bottom right
            Point3d bottomRight = new Point3d( otherCornerOffset, -otherCornerOffset, -yCornerOffset );
            viewMatrix.transform( bottomRight );
            footprint[2] = findIntersectionWithTerrain( new Vector3d( bottomRight ) );

            // farclippin plane bottom left
            Point3d bottomLeft = new Point3d( -otherCornerOffset, -otherCornerOffset, -yCornerOffset );
            viewMatrix.transform( bottomLeft );
            footprint[3] = findIntersectionWithTerrain( new Vector3d( bottomLeft ) );

        } else {
            // TODO looking up to the poi
        }
        simpleTransform.rotZ( rad360 - yaw );
        // translate to the viewersposition.
        simpleTransform.setTranslation( new Vector3d( observerPosition.x, observerPosition.y,
                                                      ( pointOfInterest.z - terrainDistanceToSeaLevel ) ) );
    }

    /**
     * For all points (x,y,z) on a plane (the terrain), the following equation defines the plane: <code>
     * --> ax + by + cz + d = 0
     * - (a, b, c) the normal vector of the plane, here it is (0, 0, 1)
     * - d the offset of the plane (terrainDistanceToSeaLevel)
     * </code>
     * a ray can be parametrized as follows: <code>
     * R(s) = eye + s * normalized_Direction
     * -s is a scaling vector,
     * </code>
     * The intersection of each ray going from the eye through the farclippinplane's cornerpoints with the terrain can
     * be calculated as follows: <code>
     * s= (a*eye_x + b*eye_y + c*eye_z + d ) / -1* (a*norm_dir + b*norm_dir + c*norm_dir)
     * </code>
     * if the denominator == 0, we are parrallel (or strifing) the plane in either case no real intersection. if s < 0
     * or s > 1 the intersection is outside the ray_length. Applying the found s to the ray's equation results in the
     * intersectionpoint.
     *
     * @param farClippingplaneCorner
     *            one the corners of the farclippingplane of the viewfrustum
     * @return the intersection point with the given ray (observerposition and a farclippingplane cornerpoint) with the
     *         terrain)
     */
    private Point3d findIntersectionWithTerrain( final Vector3d farClippingplaneCorner ) {
        final Vector3d rayDir = new Vector3d( farClippingplaneCorner );
        rayDir.sub( observerPosition );
        final double planeDir = -terrainDistanceToSeaLevel;
        final double numerator = -( observerPosition.z + planeDir );

        if ( Math.abs( rayDir.z ) < 0.0001f ) {
            // Ray is paralell to plane
            return new Point3d( farClippingplaneCorner.x, farClippingplaneCorner.y, terrainDistanceToSeaLevel );
        }
        // Find distance to intersection
        final double s = numerator / rayDir.z;

        // If the value of s is out of [0; 1], the intersection liese before or after the line
        if ( s < 0.0f ) {
            return new Point3d( farClippingplaneCorner.x, farClippingplaneCorner.y, terrainDistanceToSeaLevel );
        }
        if ( s > 1.0f ) {
            return new Point3d( farClippingplaneCorner.x, farClippingplaneCorner.y, terrainDistanceToSeaLevel );
        }
        // Finally a real intersection
        return new Point3d( observerPosition.x + ( s * rayDir.x ), observerPosition.y + ( s * rayDir.y ),
                            terrainDistanceToSeaLevel );

    }

    /**
     * Sets the viewMatrix according to the given yaw, pitch and the calculated observerPosition.
     */
    private void calculateViewMatrix() {
        viewMatrix.setIdentity();
        viewMatrix.lookAt( observerPosition, pointOfInterest, new Vector3d( 0, 0, 1 ) );
        viewMatrix.invert();

    }

    /**
     * @return true if the near clippingplane is behind the viewposition.
     */
    public boolean isNearClippingplaneBehindViewPoint() {
        if ( pitch > 0 ) { // the eye is looking down on the poi
            // pitch equals angle between upper and viewaxis, angleOfView is centered around the
            // viewaxis
            double angleToZ = pitch + ( angleOfView * 0.5 );
            if ( Math.abs( angleToZ - rad90 ) > 0.00001 ) {
                // footprint front border distance
                if ( angleToZ > rad90 ) {
                    return true;
                }
            }
        } else {
            // TODO looking up to the poi
        }

        return false;
    }

    /**
     *
     * @return the field of view of the observer in radians
     */
    public double getAngleOfView() {
        return angleOfView;
    }

    /**
     * @param aov
     *            the field of view of the observer in radians
     */
    public void setAngleOfView( double aov ) {
        this.angleOfView = aov;
        calcFootprint();
    }

    /**
     *
     * @return the horizontal direction in radians the observer looks
     */
    public double getYaw() {
        return yaw;
    }

    /**
     *
     * @param yaw
     *            the horizontal direction in radians the observer looks
     */
    public void setYaw( double yaw ) {
        this.yaw = yaw;
        calcObserverPosition();
    }

    /**
     * @return vertical direction in radians the observer looks
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * @param pitch
     *            the vertical direction in radians the observer looks
     *
     */
    public void setPitch( double pitch ) {
        this.pitch = ( pitch % rad90 );
        calcObserverPosition();
    }

    /**
     * @return Returns the distanceToSeaLevel of the terrain beneath the viewpoint.
     */
    public double getTerrainDistanceToSeaLevel() {
        return terrainDistanceToSeaLevel;
    }

    /**
     * @param distanceToSeaLevel
     *            of the terrain beneath the viewpoint
     */
    public void setTerrainDistanceToSeaLevel( double distanceToSeaLevel ) {
        this.terrainDistanceToSeaLevel = distanceToSeaLevel;
        calcFootprint();
    }

    /**
     * @return the position of the observer, the directions he looks and his field of view in radians
     *
     */
    public Point3d getObserverPosition() {
        return observerPosition;
    }

    /**
     * @param observerPosition
     *            the position of the observer, the directions he looks and his field of view in radians
     *
     */
    public void setObserverPosition( Point3d observerPosition ) {
        this.observerPosition = observerPosition;
        calcFootprint();
        calculateViewMatrix();
    }

    /**
     * @return the point of interest to which the viewer is looking
     */
    public Point3d getPointOfInterest() {
        return pointOfInterest;
    }

    /**
     * @param pointOfInterest
     *            the directions the observer looks and his field of view in radians
     *
     */
    public void setPointOfInterest( Point3d pointOfInterest ) {
        this.pointOfInterest = pointOfInterest;
        calcObserverPosition();
    }

    /**
     * The footprint in object space: <br/>f[0] = (FarclippingPlaneRight) = angleOfView/2 + viewDirection.x,
     * farclippingplaneDistance, distanceToSealevel <br/>f[1] = (FarclippingPlaneLeft) = angleOfView/2 -
     * viewDirection.x, farclippingplaneDistance, distanceToSealevel <br/>f[2] = (NearclippingPlaneRight) =
     * angleOfView/2 + viewDirection.x, nearclippingplaneDistance, distanceToSealevel <br/>f[3] =
     * (NearclippingPlaneLeft) = angleOfView/2 - viewDirection.x, nearclippingplaneDistance, distanceToSealevel
     *
     * @return footprint or rather the field of view
     */
    public Point3d[] getFootprint() {
        return footprint;
    }

    /**
     * @param distanceToSeaLevel
     *            the new height for which the footprint should be calculated
     * @return footprint or rather the field of view
     */
    public Point3d[] getFootprint( double distanceToSeaLevel ) {
        this.terrainDistanceToSeaLevel = distanceToSeaLevel;
        calcFootprint();
        return footprint;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "observerPosition: " + observerPosition + "\n" );
        sb.append( "targetPoint: " + pointOfInterest + "\n" );
        sb.append( "distance: " + this.viewerToPOIDistance + "\n" );
        sb.append( "footprint: " );
        sb.append( footprint[0] + ", " );
        sb.append( footprint[1] + ", " );
        sb.append( footprint[2] + ", " );
        sb.append( footprint[3] + "\n" );
        sb.append( "aov: " + Math.toDegrees( angleOfView ) + "\n" );
        sb.append( "yaw: " + Math.toDegrees( yaw ) + "\n" );
        sb.append( "pitch: " + Math.toDegrees( pitch ) + "\n" );
        sb.append( "distanceToSeaLevel: " + terrainDistanceToSeaLevel + "\n" );
        sb.append( "farClippingPlane: " + farClippingPlane + "\n" );

        return sb.toString();
    }

    /**
     * @return Returns the farClippingPlane.
     */
    public double getFarClippingPlane() {
        return farClippingPlane;
    }

    /**
     * @return Returns a new transform3D Object which contains the transformations to place the viewers Position and his
     *         yaw viewing angle relativ to the 0,0 coordinates and the poi.
     */
    public Transform3D getSimpleTransform() {
        return new Transform3D( simpleTransform );
    }

    /**
     * @param transform
     *            The transform to set.
     */
    public void setSimpleTransform( Transform3D transform ) {
        this.simpleTransform = transform;
    }

    /**
     * @return Returns the viewMatrix.
     */
    public Transform3D getViewMatrix() {
        return viewMatrix;
    }

    /**
     * @return the viewerToPOIDistance value.
     */
    public double getViewerToPOIDistance() {
        return viewerToPOIDistance;
    }

    /**
     * @param viewerToPOIDistance
     *            An other viewerToPOIDistance value.
     */
    public void setViewerToPOIDistance( double viewerToPOIDistance ) {
        this.viewerToPOIDistance = viewerToPOIDistance;
        calcObserverPosition();
    }

    /**
     *
     * @return the Footprint as a Surface (bbox)
     */
    public Surface getVisibleArea() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for ( Point3d point : footprint ) {
            if ( point.x < minX )
                minX = point.x;
            if ( point.x > maxX )
                maxX = point.x;
            if ( point.y < minY )
                minY = point.y;
            if ( point.y > maxY )
                maxY = point.y;
        }
        Envelope env = GeometryFactory.createEnvelope( minX, minY, maxX, maxY, crs );
        Surface s = null;
        try {
            s = GeometryFactory.createSurface( env, crs );
        } catch ( GeometryException e ) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * @return A String representation of the Footprint, so that it can be easily used in another programm e.g. deejump
     * @throws GeometryException
     *             if the footprint could not be transformed to wkt.
     */
    public String getFootPrintAsWellKnownText()
                            throws GeometryException {
        Position[] pos = new Position[footprint.length + 1];

        for ( int i = 0; i < footprint.length; ++i ) {
            Point3d point = footprint[i];
            pos[i] = GeometryFactory.createPosition( point.x, point.y, point.z );
        }
        Point3d point = footprint[0];
        pos[footprint.length] = GeometryFactory.createPosition( point.x, point.y, point.z );

        return WKTAdapter.export( GeometryFactory.createSurface( pos, null, null, crs ) ).toString();
    }

    /**
     * @return the ObserverPosition as a well known text String.
     * @throws GeometryException
     *             if the conversion fails.
     */
    public String getObserverPositionAsWKT()
                            throws GeometryException {
        return WKTAdapter.export(
                                  GeometryFactory.createPoint( observerPosition.x, observerPosition.y,
                                                               observerPosition.z, crs ) ).toString();
    }

    /**
     * @return the crs value.
     */
    public CoordinateSystem getCrs() {
        return crs;
    }

}
