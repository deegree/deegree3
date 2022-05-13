//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.geometry.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.ArcByCenterPoint;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.composite.DefaultCompositeCurve;
import org.deegree.geometry.standard.curvesegments.DefaultArc;
import org.deegree.geometry.standard.curvesegments.DefaultArcByCenterPoint;
import org.deegree.geometry.standard.curvesegments.DefaultArcString;
import org.deegree.geometry.standard.curvesegments.DefaultLineStringSegment;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.geometry.standard.primitive.DefaultCurve;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.geometry.standard.primitive.DefaultOrientableCurve;
import org.deegree.geometry.standard.primitive.DefaultRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryFixer {

    private static final Logger LOG = LoggerFactory.getLogger( GeometryFixer.class );

    /**
     * Returns a fixed version of the given {@link Ring} object.
     * 
     * @param ring
     *            ring to be repaired
     * @return repaired ring, never <code>null</code>
     */
    public static Ring fixUnclosedRing( Ring ring ) {
        Ring repaired = null;
        switch ( ring.getRingType() ) {
        case LinearRing: {
            LinearRing linearRing = (LinearRing) ring;
            Points fixedPoints = getFixedPoints( linearRing.getControlPoints(), linearRing.getStartPoint() );
            repaired = new DefaultLinearRing( ring.getId(), ring.getCoordinateSystem(), ring.getPrecision(),
                                              fixedPoints );
            break;
        }
        case Ring: {
            List<Curve> repairedCurves = new ArrayList<Curve>( ring.getMembers() );
            Curve lastCurve = repairedCurves.get( repairedCurves.size() - 1 );
            repairedCurves.set( repairedCurves.size() - 1, fixCurve( lastCurve, ring.getStartPoint() ) );
            repaired = new DefaultRing( ring.getId(), ring.getCoordinateSystem(), ring.getPrecision(), repairedCurves );
            break;
        }
        }
        repaired.setProperties( ring.getProperties() );
        return repaired;
    }

    private static Curve fixCurve( Curve curve, Point lastPoint ) {
        Curve fixedCurve = null;
        switch ( curve.getCurveType() ) {
        case Curve: {
            List<CurveSegment> fixedSegments = new ArrayList<CurveSegment>( curve.getCurveSegments() );
            CurveSegment lastSegment = fixedSegments.get( fixedSegments.size() - 1 );
            if ( lastSegment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                LineStringSegment lineString = (LineStringSegment) lastSegment;
                Points fixedPoints = getFixedPoints( lineString.getControlPoints(), lastPoint );
                LineStringSegment fixedSegment = new DefaultLineStringSegment( fixedPoints );
                fixedSegments.set( fixedSegments.size() - 1, fixedSegment );
            } else {
                LOG.warn( "Cannot fix " + lastSegment.getSegmentType() + " segments." );
            }
            fixedCurve = new DefaultCurve( curve.getId(), curve.getCoordinateSystem(), curve.getPrecision(),
                                           fixedSegments );
            break;
        }
        case LineString: {
            LineString lineString = (LineString) curve;
            Points fixedPoints = getFixedPoints( lineString.getControlPoints(), lastPoint );
            fixedCurve = new DefaultLineString( curve.getId(), curve.getCoordinateSystem(), curve.getPrecision(),
                                                fixedPoints );
            break;
        }
        case CompositeCurve: {
            CompositeCurve compositeCurve = (CompositeCurve) curve;
            List<Curve> fixedMemberCurves = new ArrayList<Curve>( compositeCurve );
            fixedMemberCurves.set( fixedMemberCurves.size() - 1,
                                   fixCurve( fixedMemberCurves.get( fixedMemberCurves.size() - 1 ), lastPoint ) );
            fixedCurve = new DefaultCompositeCurve( curve.getId(), curve.getCoordinateSystem(), curve.getPrecision(),
                                                    fixedMemberCurves );
            break;
        }
        case OrientableCurve: {
            OrientableCurve orientableCurve = (OrientableCurve) curve;
            Curve fixedBaseCurve = fixCurve( orientableCurve.getBaseCurve(), lastPoint );
            fixedCurve = new DefaultOrientableCurve( curve.getId(), curve.getCoordinateSystem(), fixedBaseCurve,
                                                     orientableCurve.isReversed() );
            break;
        }
        case Ring: {
            Ring ring = (Ring) curve;
            switch ( ring.getRingType() ) {
            case LinearRing: {
                LinearRing linearRing = (LinearRing) ring;
                Points fixedPoints = getFixedPoints( linearRing.getControlPoints(), linearRing.getStartPoint() );
                fixedCurve = new DefaultLinearRing( ring.getId(), ring.getCoordinateSystem(), ring.getPrecision(),
                                                    fixedPoints );
                break;
            }
            case Ring: {
                List<Curve> repairedCurves = new ArrayList<Curve>( ring.getMembers() );
                Curve lastCurve = repairedCurves.get( repairedCurves.size() - 1 );
                repairedCurves.set( repairedCurves.size() - 1, fixCurve( lastCurve, ring.getStartPoint() ) );
                fixedCurve = new DefaultRing( ring.getId(), ring.getCoordinateSystem(), ring.getPrecision(),
                                              repairedCurves );
                break;
            }
            }
        }
        }
        fixedCurve.setProperties( curve.getProperties() );
        return fixedCurve;
    }

    private static Points getFixedPoints( Points points, Point newLastPoint ) {
        int numPoints = points.size();
        List<Point> fixedPointsList = new ArrayList<Point>( numPoints );
        int i = 0;
        // TODO check if this works for all Points implementations
        for ( Point p : points ) {
            if ( i++ != ( numPoints - 1 ) ) {
                fixedPointsList.add( p );
            } else {
                fixedPointsList.add( newLastPoint );
            }
        }
        return new PointsList( fixedPointsList );
    }

    public static Curve invertOrientation( Curve curve ) {
        Curve fixedCurve = null;
        switch ( curve.getCurveType() ) {
        case CompositeCurve: {
            CompositeCurve compositeCurve = (CompositeCurve) curve;
            List<Curve> fixedMemberCurves = new ArrayList<Curve>( compositeCurve.size() );
            for ( Curve memberCurve : compositeCurve ) {
                fixedMemberCurves.add( invertOrientation( memberCurve ) );
            }
            Collections.reverse( fixedMemberCurves );
            fixedCurve = new DefaultCompositeCurve( curve.getId(), curve.getCoordinateSystem(), curve.getPrecision(),
                                                    fixedMemberCurves );
            break;
        }
        case Curve: {
            List<CurveSegment> fixedSegments = new ArrayList<CurveSegment>( curve.getCurveSegments().size() );
            for ( CurveSegment segment : curve.getCurveSegments() ) {
                fixedSegments.add( invertOrientation( segment ) );
            }
            Collections.reverse( fixedSegments );
            fixedCurve = new DefaultCurve( curve.getId(), curve.getCoordinateSystem(), curve.getPrecision(),
                                           fixedSegments );
            break;
        }
        case LineString: {
            LineString lineString = (LineString) curve;
            fixedCurve = new DefaultLineString( curve.getId(), curve.getCoordinateSystem(), curve.getPrecision(),
                                                invertOrientation( lineString.getControlPoints() ) );
            break;
        }
        case OrientableCurve: {
            OrientableCurve orientableCurve = (OrientableCurve) curve;
            fixedCurve = new DefaultOrientableCurve( curve.getId(), curve.getCoordinateSystem(),
                                                     invertOrientation( orientableCurve.getBaseCurve() ),
                                                     orientableCurve.isReversed() );
            break;
        }
        case Ring: {
            fixedCurve = invertOrientation( (Ring) curve );
            break;
        }
        }
        fixedCurve.setProperties( curve.getProperties() );
        return fixedCurve;
    }

    public static Ring forceOrientation( Ring ring, boolean ccw ) {
        double shoelaceSum = 0;

        final List<Curve> curves = ring.getMembers();
        for ( final Curve curve : curves ) {
            final List<CurveSegment> curveSegments = curve.getCurveSegments();
            for ( final CurveSegment curveSegment : curveSegments ) {
                final CurveSegmentType segmentType = curveSegment.getSegmentType();
                
                Points points;
                switch ( segmentType ) {
                case ARC:
                    points = ( (Arc) curveSegment ).getControlPoints();
                    break;
                case ARC_STRING:
                    points = ( (ArcString) curveSegment ).getControlPoints();
                    break;
                case LINE_STRING_SEGMENT:
                    points = ( (LineStringSegment) curveSegment ).getControlPoints();
                    break;   
                default:
                    LOG.warn( "Calculating orientation of " + segmentType.name()
                              + " segments is not implemented yet. Ring orientation remains unchanged." );
                    return ring;
                }
                
                for ( int i = 1; i < points.size(); i++ ) {
                    final Point first = points.get( i - 1 );
                    final Point second = points.get( i );

                    shoelaceSum += ( second.get0() - first.get0() ) * ( second.get1() + first.get1() );
                }
            }
        }

        return shoelaceSum > 0 == ccw ? invertOrientation( ring ) : ring;
    }

    public static Ring invertOrientation( Ring ring ) {
        Ring fixedRing = null;
        switch ( ring.getRingType() ) {
        case LinearRing: {
            LinearRing linearRing = (LinearRing) ring;
            fixedRing = new DefaultLinearRing( ring.getId(), ring.getCoordinateSystem(), ring.getPrecision(),
                                               invertOrientation( linearRing.getControlPoints() ) );
            break;
        }
        case Ring: {
            List<Curve> fixedMemberCurves = new ArrayList<Curve>( ring.getMembers().size() );
            for ( Curve memberCurve : ring.getMembers() ) {
                fixedMemberCurves.add( invertOrientation( memberCurve ) );
            }
            Collections.reverse( fixedMemberCurves );
            fixedRing = new DefaultRing( ring.getId(), ring.getCoordinateSystem(), ring.getPrecision(),
                                         fixedMemberCurves );
            break;
        }
        }
        fixedRing.setProperties( ring.getProperties() );
        return fixedRing;
    }

    private static Points invertOrientation( Points points ) {
        List<Point> fixedPointsList = new ArrayList<Point>( points.size() );
        for ( int i = points.size() - 1; i >= 0; i-- ) {
            fixedPointsList.add( points.get( i ) );
        }
        return new PointsList( fixedPointsList );
    }

    private static CurveSegment invertOrientation( CurveSegment segment ) {
        CurveSegment fixedSegment = null;
        switch ( segment.getSegmentType() ) {
        case ARC: {
            Arc arc = (Arc) segment;
            fixedSegment = new DefaultArc( arc.getPoint3(), arc.getPoint2(), arc.getPoint1() );
            break;
        }
        case ARC_BY_BULGE: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case ARC_BY_CENTER_POINT: {
            ArcByCenterPoint arc = (ArcByCenterPoint) segment;
            fixedSegment = new DefaultArcByCenterPoint( arc.getMidPoint(), arc.getRadius( null ), arc.getEndAngle(),
                                                        arc.getStartAngle() );
            break;
        }
        case ARC_STRING: {
            ArcString arc = (ArcString) segment;
            fixedSegment = new DefaultArcString( invertOrientation( arc.getControlPoints() ) );
            break;
        }
        case ARC_STRING_BY_BULGE: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case BEZIER: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case BSPLINE: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case CIRCLE: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case CIRCLE_BY_CENTER_POINT: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case CLOTHOID: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case CUBIC_SPLINE: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case GEODESIC: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case GEODESIC_STRING: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        case LINE_STRING_SEGMENT: {
            fixedSegment = new DefaultLineStringSegment(
                                                         invertOrientation( ( (LineStringSegment) segment ).getControlPoints() ) );
            break;
        }
        case OFFSET_CURVE: {
            LOG.warn( "Inverting of " + segment.getSegmentType().name() + " segments is not implemented yet." );
            fixedSegment = segment;
            break;
        }
        }
        return fixedSegment;
    }
}
