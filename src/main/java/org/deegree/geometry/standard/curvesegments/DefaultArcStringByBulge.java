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
import org.deegree.geometry.primitive.segments.ArcStringByBulge;
import org.deegree.geometry.primitive.segments.CurveSegment;

/**
 * Default implementation of {@link ArcStringByBulge} segments.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultArcStringByBulge implements ArcStringByBulge {

    private Points controlPoints;

    private double[] bulges;

    private Points normals;

    /**
     *
     * @param controlPoints
     *            list of {@link Point}s that describe the <code>ArcStringByBulge</code>
     * @param bulges
     *
     * @param normals
     */
    public DefaultArcStringByBulge( Points controlPoints, double[] bulges, Points normals ) {
        if ( controlPoints.size() < 2 ) {
            String msg = "An ArcStringByBulge must contain at least 2 control points.";
            throw new IllegalArgumentException( msg );
        }
        if ( bulges.length != controlPoints.size() - 1 ) {
            String msg = "The number of provided bulge values for an ArcStringByBulge must be equal to the number of control points minus one.";
            throw new IllegalArgumentException( msg );
        }
        if ( bulges.length != controlPoints.size() - 1 ) {
            String msg = "The number of normal vectors for an ArcStringByBulge must be equal to the number of control points minus one.";
            throw new IllegalArgumentException( msg );
        }
        this.controlPoints = controlPoints;
        this.bulges = bulges;
        this.normals = normals;
    }

    @Override
    public double[] getBulges() {
        return bulges;
    }

    @Override
    public Points getNormals() {
        return normals;
    }

    @Override
    public int getNumArcs() {
        return controlPoints.size() -1;
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
        return controlPoints.get( controlPoints.size() - 1 );
    }

    @Override
    public CurveSegmentType getSegmentType() {
        return CurveSegmentType.ARC_STRING_BY_BULGE;
    }
}
