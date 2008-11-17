//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.model.geometry.standard.curvesegments;

import java.util.List;

import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.ArcStringByBulge;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;

/**
 * Default implementation of {@link ArcStringByBulge} segments.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultArcStringByBulge implements ArcStringByBulge {

    private List<Point> controlPoints;

    private double[] bulges;

    private List<Point> normals;

    /**
     * 
     * @param controlPoints
     *            list of {@link Point}s that describe the <code>ArcStringByBulge</code>
     * @param bulges
     * 
     * @param normals
     */
    public DefaultArcStringByBulge( List<Point> controlPoints, double[] bulges, List<Point> normals ) {
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
    public List<Point> getNormals() {
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
    public Interpolation getInterpolation() {
        return CurveSegment.Interpolation.circularArc2PointWithBulge;
    }

    @Override
    public List<Point> getControlPoints() {
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
