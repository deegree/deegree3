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
package org.deegree.geometry.standard.curvesegments;

import java.util.Collections;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.ArcByBulge;
import org.deegree.geometry.standard.points.PointsArray;
import org.deegree.geometry.standard.points.PointsList;

/**
 * Default implementation of {@link ArcByBulge} segments.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultArcByBulge implements ArcByBulge {

    private final Points controlPoints;

    private final double bulge;

    private final Point normal;

    /**
     * Creates a new <code>DefaultArcByBulge</code> instance from the given parameters.
     * 
     * @param p1
     *            first control point
     * @param p2
     *            second control point
     * @param bulge
     * @param normal
     */
    public DefaultArcByBulge( Point p1, Point p2, double bulge, Point normal ) {
        controlPoints = new PointsArray( p1, p2 );
        this.bulge = bulge;
        this.normal = normal;
    }

    @Override
    public Point getPoint1() {
        return controlPoints.get( 0 );
    }

    @Override
    public Point getPoint2() {
        return controlPoints.get( 1 );
    }

    @Override
    public double getBulge() {
        return bulge;
    }

    @Override
    public Point getNormal() {
        return normal;
    }

    @Override
    public double[] getBulges() {
        return new double[] { bulge };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Points getNormals() {
        return new PointsList( Collections.singletonList( normal ) );
    }

    @Override
    public int getNumArcs() {
        return 1;
    }

    @Override
    public int getCoordinateDimension() {
        return controlPoints.get( 0 ).getCoordinateDimension();
    }

    @Override
    public Points getControlPoints() {
        return controlPoints;
    }

    @Override
    public Point getStartPoint() {
        return controlPoints.get( 0 );
    }

    @Override
    public Point getEndPoint() {
        return controlPoints.get( 1 );
    }

    @Override
    public CurveSegmentType getSegmentType() {
        return CurveSegmentType.ARC_BY_BULGE;
    }
}
