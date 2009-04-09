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
package org.deegree.geometry.standard.primitive;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link Ring}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultRing extends AbstractDefaultGeometry implements Ring {

    /** The constituting <code>Curve</code> instances. */
    protected List<Curve> members;

    /** The segments of all member curves. */
    protected List<CurveSegment> segments = new LinkedList<CurveSegment>();

    /**
     * Creates a new <code>DefaultRing</code> instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param members
     *            the <code>Curve</code>s that compose the <code>Ring</code>
     */
    public DefaultRing( String id, CoordinateSystem crs, List<Curve> members ) {
        super( id, crs );
        this.members = members;
        for ( Curve curve : members ) {
            segments.addAll( curve.getCurveSegments() );
        }
    }

    /**
     * Creates a new <code>DefaultRing</code> instance from a closed {@link DefaultLineString}.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param singleCurve
     *            closed line string
     */
    protected DefaultRing( String id, CoordinateSystem crs, DefaultLineString singleCurve ) {
        super( id, crs );
        members = new ArrayList<Curve>( 1 );
        members.add( singleCurve );
        segments.addAll( singleCurve.getCurveSegments() );
    }

    /**
     * @param id
     * @param crs
     * @param segment
     */
    public DefaultRing( String id, CoordinateSystem crs, LineStringSegment segment ) {
        super( id, crs );
        this.members = new ArrayList<Curve>( 1 );
        this.members.add( new DefaultLineString( null, crs, segment.getControlPoints() ) );
        this.segments.add( segment );
    }

    @Override
    public RingType getRingType() {
        return RingType.Ring;
    }

    @Override
    public LineString getAsLineString() {
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
    public CurveType getCurveType() {
        return CurveType.Ring;
    }

    @Override
    public double getLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        return getStartPoint().equals( getEndPoint() );
    }

    @Override
    public List<Curve> getMembers() {
        return members;
    }

    @Override
    public Point getStartPoint() {
        return segments.get( 0 ).getStartPoint();
    }    
    
    @Override
    public Point getEndPoint() {
        return segments.get( segments.size() -1 ).getEndPoint();
    }
    
    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Curve;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public List<Point> getControlPoints() {
        List<Point> controlPoints = new ArrayList<Point>();
        for ( CurveSegment segment : segments ) {
            if ( segment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                controlPoints.addAll( ( (LineStringSegment) segment ).getControlPoints() );
            } else {
                String msg = "Cannot determine control points for curve, contains non-linear segments.";
                throw new IllegalArgumentException( msg );
            }
        }
        return controlPoints;
    }    
}
