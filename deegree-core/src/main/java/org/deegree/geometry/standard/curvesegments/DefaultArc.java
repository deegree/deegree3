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

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.standard.points.PointsArray;

/**
 * Default implementation of {@link Arc} segments.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultArc implements Arc {

    protected final Points points;

    /**
     * Creates a new <code>DefaultArc</code> instance from the given parameters.
     * 
     * @param p1
     *            first control point
     * @param p2
     *            second control point
     * @param p3
     *            third control point
     */
    public DefaultArc( Point p1, Point p2, Point p3 ) {
        points = new PointsArray( p1, p2, p3 );
    }

    @Override
    public Point getPoint1() {
        return points.get( 0 );
    }

    @Override
    public Point getPoint2() {
        return points.get( 1 );
    }

    @Override
    public Point getPoint3() {
        return points.get( 2 );
    }

    @Override
    public int getCoordinateDimension() {
        return points.getDimension();
    }

    @Override
    public Points getControlPoints() {
        return points;
    }

    @Override
    public int getNumArcs() {
        return 1;
    }

    @Override
    public Point getStartPoint() {
        return points.getStartPoint();
    }

    @Override
    public Point getEndPoint() {
        return points.getEndPoint();
    }

    @Override
    public CurveSegmentType getSegmentType() {
        return CurveSegmentType.ARC;
    }
}
