//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.geometry.standard.curvesegments;

import java.util.List;

import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.curvesegments.ArcString;
import org.deegree.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;

/**
 * Default implementation of {@link ArcString} segments.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultArcString implements ArcString {

    protected int numArcs;

    protected List<Point> controlPoints;

    /**
     * Creates a new <code>DefaultArcString</code> instance from the given parameters.
     * 
     * @param controlPoints
     *            interpolation points
     */
    public DefaultArcString( List<Point> controlPoints ) {
        if ( controlPoints.size() < 3 || controlPoints.size() % 2 != 1 ) {
            throw new IllegalArgumentException( "Invalid number of points." );
        }
        numArcs = controlPoints.size() / 2;
        this.controlPoints = controlPoints;
    }

    @Override
    public boolean is3D() {
        return controlPoints.get( 0 ).is3D();
    }

    @Override
    public Interpolation getInterpolation() {
        return Interpolation.circularArc3Points;
    }

    @Override
    public List<Point> getControlPoints() {
        return controlPoints;
    }

    @Override
    public int getNumArcs() {
        return numArcs;
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
        return CurveSegmentType.ARC_STRING;
    }
}
