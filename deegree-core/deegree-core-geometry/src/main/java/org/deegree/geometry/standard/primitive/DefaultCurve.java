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
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.i18n.Messages;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.points.PointsPoints;
import org.deegree.geometry.standard.points.PointsSubsequence;

import org.locationtech.jts.geom.Coordinate;

/**
 * Default implementation of {@link Curve}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultCurve extends AbstractDefaultGeometry implements Curve {

    private List<CurveSegment> segments;

    /**
     * Creates a new {@link DefaultCurve} instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param segments
     *            segments that constitute the curve (never null)
     */
    public DefaultCurve( String id, ICRS crs, PrecisionModel pm, List<CurveSegment> segments ) {
        super( id, crs, pm );
        this.segments = new ArrayList<CurveSegment>( segments );
    }

    @Override
    public int getCoordinateDimension() {
        return segments.get( 0 ).getCoordinateDimension();
    }

    @Override
    public Pair<Point, Point> getBoundary() {
        return new Pair<Point, Point>( getStartPoint(), getEndPoint() );
    }

    @Override
    public List<CurveSegment> getCurveSegments() {
        return segments;
    }

    @Override
    public Measure getLength( Unit requestedUnit ) {
        // TODO respect requested unit
        double length = ( (org.locationtech.jts.geom.LineString) getJTSGeometry() ).getLength();
        return new Measure( Double.toString( length ), null );
    }

    @Override
    public boolean isClosed() {
        return getStartPoint().equals( getEndPoint() );
    }

    @Override
    public LineString getAsLineString() {
        return new DefaultLineString( null, getCoordinateSystem(), pm, getControlPoints() );
    }

    @Override
    public CurveType getCurveType() {
        return CurveType.Curve;
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
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Curve;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public Points getControlPoints() {
        if ( segments.size() == 1 ) {
            CurveSegment segment = segments.get( 0 );
            if ( segment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                return ( (LineStringSegment) segment ).getControlPoints();
            }
            throw new IllegalArgumentException( Messages.getMessage( "CURVE_CONTAINS_NON_LINEAR_SEGMENT" ) );
        }

        List<Points> pointsList = new ArrayList<Points>( segments.size() );
        boolean first = true;
        for ( CurveSegment segment : segments ) {
            if ( segment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                if ( first ) {
                    pointsList.add( ( (LineStringSegment) segment ).getControlPoints() );
                    first = false;
                } else {
                    // starting with the second segment, skip the first point (as it *must* be identical to
                    // last point of the last segment)
                    pointsList.add( new PointsSubsequence( ( (LineStringSegment) segment ).getControlPoints(), 1 ) );
                }
            } else {
                throw new IllegalArgumentException( Messages.getMessage( "CURVE_CONTAINS_NON_LINEAR_SEGMENTS" ) );
            }
        }
        return new PointsPoints( pointsList );
    }

    @Override
    protected org.locationtech.jts.geom.LineString buildJTSGeometry() {
        CurveLinearizer linearizer = new CurveLinearizer( new GeometryFactory() );
        // TODO how to provide a linearization criterion?
        LinearizationCriterion crit = new NumPointsCriterion( 100 );
        List<Coordinate> coords = new LinkedList<Coordinate>();
        boolean first = true;
        for ( CurveSegment segment : segments ) {
            List<Coordinate> coordinates = getCoordinates( linearizer.linearize( segment, crit ) );
            if ( first ) {
                coords.addAll( coordinates );
                first = false;
            } else {
                // starting with the second segment, skip the first point (as it *must* be identical to
                // last point of the last segment)
                coords.addAll( coordinates.subList( 1, coordinates.size() ) );
            }
        }
        return jtsFactory.createLineString( coords.toArray( new Coordinate[coords.size()] ) );
    }

    private List<Coordinate> getCoordinates( LineStringSegment lsSegment ) {
        Points points = lsSegment.getControlPoints();
        List<Coordinate> coordinates = new ArrayList<Coordinate>( points.size() );
        for ( Point point : points ) {
            coordinates.add( new Coordinate( point.get0(), point.get1(), point.get2() ) );
        }
        return coordinates;
    }
}
