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
package org.deegree.model.geometry.standard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Point;

public class DefaultCurve extends AbstractDefaultGeometry implements Curve {

    private List<CurveSegment> segments;

    private Curve.Orientation orientation;

    /**
     * 
     * @param id
     * @param crs
     * @param segments
     * @param orientation
     */
    public DefaultCurve( String id, CoordinateSystem crs, List<CurveSegment> segments,
                            Curve.Orientation orientation ) {
        super( id, crs );
        this.segments = new ArrayList<CurveSegment>(segments);
        this.orientation = orientation;
    }    
    
    @Override
    public double[] getAsArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Point> getBoundary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CurveSegment> getCurveSegments() {
        return segments;
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public List<Point> getPoints() {
        List<Point> points = new LinkedList<Point>();
        for ( CurveSegment segment : segments ) {
            points.addAll( segment.getPoints() );
        }
        return points;
    }

    @Override
    public double[] getX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getZ() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }
}
