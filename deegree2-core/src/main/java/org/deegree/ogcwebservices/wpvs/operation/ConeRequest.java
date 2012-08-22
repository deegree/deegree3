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

import java.awt.geom.Point2D;

import javax.media.j3d.PickConeSegment;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.SurfaceInterpolation;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;
import org.deegree.ogcwebservices.wpvs.j3d.ViewPoint;

/**
 * This class represents the geometry needed for a request cone.
 *
 * @version $Revision$
 * @author <a href="mailto:cordes@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 */
public class ConeRequest extends RequestGeometry {

    // end point of the view ray
    private Point3d endPointLine;

    /**
     * Initializes the two Geometries for the request with a cone.
     *
     * @param request
     */
	public ConeRequest(Get3DFeatureInfo request) {
		super( request );
        endPointLine = new Point3d (calcEndPoint( new ViewPoint( request.getGetViewRequestCopy() ),
                      (int)request.getDepth(),
                      (int)request.getGetViewRequestCopy().getImageDimension().getWidth(),
                      (int)request.getGetViewRequestCopy().getImageDimension().getHeight(),
                      request.getClickPoint().x, request.getClickPoint().y ) );
	}

	@Override
	public void setPickshape() {
		pickshape = new PickConeSegment( getBeginPointLine(), endPointLine, getRequest().getApexAngle() );
	}

	@Override
	public void setWfsReqGeom() throws GeometryException {

        Position[] pos = new Position[4];
        int i = 0;
        pos[i++] = GeometryFactory.createPosition(getBeginPointLine().x, getBeginPointLine().y, 0);

        Vector3d tmp = new Vector3d();
        tmp.sub(getBeginPointLine(), endPointLine);
        double length = tmp.length();
        double r = Math.tan( getRequest().getApexAngle() ) * length;

        double dX = endPointLine.x - getBeginPointLine().x;
        double dY = endPointLine.y - getBeginPointLine().y;
        double lengthGround = Math.sqrt(Math.pow( dX, 2 ) + Math.pow( dY, 2 ));

        double dif = Math.tan( getRequest().getApexAngle() ) * (lengthGround + r);
        double rot = Math.toRadians(90);
        if ( dY == 0 && dX > 0 ) {
            rot = Math.toRadians(270);
        }else if ( dX != dY ) {
            rot = Math.atan( dX / dY );
        }
        if ( dY < 0 ) {
            rot = ( rot + Math.toRadians(180) );
        } else {
            rot = ( rot + Math.toRadians(360) );
        }
        rot = Math.toRadians( 360 ) - (rot  % Math.toRadians( 360 ));

        // transformation
        Point2D.Double point2 = trans2d(getBeginPointLine().x, getBeginPointLine().y,
                                        rot, new Point2D.Double( dif, lengthGround ));
        pos[i++] = GeometryFactory.createPosition( point2.x, point2.y, 0 );

        Point2D.Double point3 = trans2d(getBeginPointLine().x, getBeginPointLine().y,
                                        rot , new Point2D.Double( -dif, lengthGround ));
        pos[i++] = GeometryFactory.createPosition(point3.x, point3.y, 0);

        pos[i++] = pos[0];

        SurfaceInterpolation si = new SurfaceInterpolationImpl( 0 );
        wfsReqGeom = GeometryFactory.createSurface( pos, new Position[0][0], si, getCrs() );
	}

	/**
	 * 3-parameter transformation
	 *
	 * @param translationX the translation in x
	 * @param translationY the translation in x
	 * @param rotation rotation of the transformation
	 * @param transPoint point to transform
	 *
	 * @return the point in the new coordinatsystem
	 */
	private Point2D.Double trans2d(double translationX, double translationY, double rotation,
                                   Point2D.Double transPoint) {

        double xTrans = transPoint.x * Math.cos( rotation ) - transPoint.y * Math.sin( rotation ) + translationX;
		double yTrans = transPoint.x * Math.sin( rotation ) + transPoint.y * Math.cos( rotation ) + translationY;
		return new Point2D.Double( xTrans, yTrans );
	}
}
