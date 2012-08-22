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

import javax.media.j3d.PickSegment;
import javax.vecmath.Point3d;

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.wpvs.j3d.ViewPoint;

/**
 * This class represents the geometry needed for a request with a ray of view.
 *
 * @version $Revision$
 * @author <a href="mailto:cordes@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 */
public class LineRequest extends RequestGeometry {

    // end point of the view ray
    private Point3d endPointLine;

    /**
     * Initializes the two Geometrys for the request with a ray.
     *
     * @param request the Get3DFeatureInfo-request
     */
	public LineRequest(Get3DFeatureInfo request)  {
		super( request );
        endPointLine = new Point3d ( calcEndPoint( new ViewPoint( request.getGetViewRequestCopy() ),
                                                   (int)request.getDepth(),
                                                   (int)request.getGetViewRequestCopy().getImageDimension().getWidth(),
                                                   (int)request.getGetViewRequestCopy().getImageDimension().getHeight(),
                                                   request.getClickPoint().x, request.getClickPoint().y ) );
	}

	@Override
	public void setPickshape() {
		pickshape =  new PickSegment( getBeginPointLine(), endPointLine );
	}

	@Override
	public void setWfsReqGeom() throws GeometryException {
		Position p1 = GeometryFactory.createPosition( getBeginPointLine().x, getBeginPointLine().y, getBeginPointLine().z );
	    Position p2 = GeometryFactory.createPosition( endPointLine.x, endPointLine.y, endPointLine.z );
		wfsReqGeom = GeometryFactory.createCurve( new Position[]{ p1, p2}, getCrs() );
	}


    /**
     * @return the end point of the line
     */
    public Point3d getEndPointLine() {
        return endPointLine;
    }


}
