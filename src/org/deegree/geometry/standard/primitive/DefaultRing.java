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
package org.deegree.geometry.standard.primitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.DefaultGeometryFactory;

import com.vividsolutions.jts.geom.Coordinate;

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
    public DefaultRing( String id, CRS crs, List<Curve> members ) {
        super( id, crs );
        this.members = members;
        for ( Curve curve : members ) {
            segments.addAll( curve.getCurveSegments() );
        }
    }

    /**
     * @param id
     * @param crs
     * @param segment
     */
    public DefaultRing( String id, CRS crs, LineStringSegment segment ) {
        super( id, crs );
        this.members = new ArrayList<Curve>( 1 );
        this.members.add( new DefaultLineString( null, crs, segment.getControlPoints() ) );
        this.segments.add( segment );
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
    protected DefaultRing( String id, CRS crs, DefaultLineString singleCurve ) {
        super( id, crs );
        members = new ArrayList<Curve>( 1 );
        members.add( singleCurve );
        segments.addAll( singleCurve.getCurveSegments() );
    }

    @Override
    public boolean is3D() {
        return members.get( 0 ).is3D();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Curve;
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
        return segments.get( segments.size() - 1 ).getEndPoint();
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

    @Override
    protected com.vividsolutions.jts.geom.LinearRing buildJTSGeometry() {
        CurveLinearizer linearizer = new CurveLinearizer( new DefaultGeometryFactory() );
        // TODO how to determine a feasible linearization criterion?
        LinearizationCriterion crit = new NumPointsCriterion( 100 );
        List<Coordinate> coords = new LinkedList<Coordinate>();
        for ( CurveSegment segment : segments ) {
            LineStringSegment lsSegment = linearizer.linearize( segment, crit );
            coords.addAll( getCoordinates( lsSegment ) );
        }
        return jtsFactory.createLinearRing( coords.toArray( new Coordinate[coords.size()] ) );
    }

    private Collection<Coordinate> getCoordinates( LineStringSegment lsSegment ) {
        List<Point> points = lsSegment.getControlPoints();
        List<Coordinate> coordinates = new ArrayList<Coordinate>( points.size() );
        for ( Point point : points ) {
            coordinates.add( new Coordinate( point.getX(), point.getY(), point.getZ() ) );
        }
        return coordinates;
    }
}
