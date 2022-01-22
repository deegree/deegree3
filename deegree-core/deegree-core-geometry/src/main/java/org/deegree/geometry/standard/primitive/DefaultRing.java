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

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.CRS;
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
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.primitive.segments.GeodesicString;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.points.PointsPoints;

import org.locationtech.jts.geom.Coordinate;

/**
 * Default implementation of {@link Ring}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
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
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param members
     *            the <code>Curve</code>s that compose the <code>Ring</code>
     */
    public DefaultRing( String id, ICRS crs, PrecisionModel pm, List<Curve> members ) {
        super( id, crs, pm );
        this.members = members;
    }

    /**
     * Creates a new <code>DefaultRing</code> instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param segment
     *            the segment that composes the <code>Ring</code>
     */
    public DefaultRing( String id, ICRS crs, PrecisionModel pm, LineStringSegment segment ) {
        super( id, crs, pm );
        this.members = new ArrayList<Curve>( 1 );
        this.members.add( new DefaultLineString( null, crs, pm, segment.getControlPoints() ) );
        this.segments.add( segment );
    }

    /**
     * Creates a new <code>DefaultRing</code> instance from a closed {@link DefaultLineString}.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param singleCurve
     *            closed line string
     */
    protected DefaultRing( String id, CRS crs, PrecisionModel pm, DefaultLineString singleCurve ) {
        super( id, crs, pm );
        members = new ArrayList<Curve>( 1 );
        members.add( singleCurve );
        segments.addAll( singleCurve.getCurveSegments() );
    }

    @Override
    public int getCoordinateDimension() {
        return members.get( 0 ).getCoordinateDimension();
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
    public Pair<Point, Point> getBoundary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized List<CurveSegment> getCurveSegments() {
        if ( segments.isEmpty() ) {
            for ( final Curve curve : members ) {
                segments.addAll( curve.getCurveSegments() );
            }
        }
        return segments;
    }

    @Override
    public CurveType getCurveType() {
        return CurveType.Ring;
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
    public List<Curve> getMembers() {
        return members;
    }

    @Override
    public Point getStartPoint() {
        return getCurveSegments().get( 0 ).getStartPoint();
    }

    @Override
    public Point getEndPoint() {
        return getCurveSegments().get( getCurveSegments().size() - 1 ).getEndPoint();
    }

    @Override
    public Points getControlPoints() {
        final List<CurveSegment> segments = getCurveSegments();
        if ( segments.size() == 1 ) {
            final CurveSegment segment = segments.get( 0 );
            if ( segment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                return ( (LineStringSegment) segment ).getControlPoints();
            }
            if ( segment.getSegmentType() == CurveSegmentType.GEODESIC_STRING ) {
                return ( (GeodesicString) segment ).getControlPoints();
            }
            throw new IllegalArgumentException( Messages.getMessage( "RING_CONTAINS_NON_LINEAR_SEGMENT" ) );
        }

        List<Points> pointsList = new ArrayList<Points>( segments.size() );
        for ( CurveSegment segment : segments ) {
            if ( segment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                pointsList.add( ( (LineStringSegment) segment ).getControlPoints() );
            } else if ( segment.getSegmentType() == CurveSegmentType.GEODESIC_STRING ) {
                pointsList.add( ( (GeodesicString) segment ).getControlPoints() );
            } else {
                throw new IllegalArgumentException( Messages.getMessage( "RING_CONTAINS_NON_LINEAR_SEGMENTS" ) );
            }
        }
        return new PointsPoints( pointsList );
    }

    @Override
    protected org.locationtech.jts.geom.LinearRing buildJTSGeometry() {
        CurveLinearizer linearizer = new CurveLinearizer( new GeometryFactory() );
        // TODO how to determine a feasible linearization criterion?
        LinearizationCriterion crit = new NumPointsCriterion( 100 );
        List<Coordinate> coords = new LinkedList<Coordinate>();
        for ( final CurveSegment segment : getCurveSegments() ) {
            LineStringSegment lsSegment = linearizer.linearize( segment, crit );
            coords.addAll( getCoordinates( lsSegment ) );
        }
        return jtsFactory.createLinearRing( coords.toArray( new Coordinate[coords.size()] ) );
    }

    private Collection<Coordinate> getCoordinates( LineStringSegment lsSegment ) {
        Points points = lsSegment.getControlPoints();
        List<Coordinate> coordinates = new ArrayList<Coordinate>( points.size() );
        for ( Point point : points ) {
            coordinates.add( new Coordinate( point.get0(), point.get1(), point.get2() ) );
        }
        return coordinates;
    }
}
