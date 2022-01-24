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
package org.deegree.geometry.validation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.validation.event.CurveDiscontinuity;
import org.deegree.geometry.validation.event.CurveSelfIntersection;
import org.deegree.geometry.validation.event.DuplicatePoints;
import org.deegree.geometry.validation.event.ExteriorRingOrientation;
import org.deegree.geometry.validation.event.GeometryValidationEvent;
import org.deegree.geometry.validation.event.InteriorRingIntersectsExterior;
import org.deegree.geometry.validation.event.InteriorRingOrientation;
import org.deegree.geometry.validation.event.InteriorRingOutsideExterior;
import org.deegree.geometry.validation.event.InteriorRingsIntersect;
import org.deegree.geometry.validation.event.InteriorRingsNested;
import org.deegree.geometry.validation.event.RingNotClosed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.IsSimpleOp;

/**
 * Performs topological validation of {@link Geometry} objects.
 * <p>
 * The tested topological properties depend on the type of geometry:
 * </p>
 * <p>
 * <h2>Points</h2> No constraints, {@link Point} geometries are always topologically valid.
 * </p>
 * <p>
 * <h2>Curves</h2>
 * <ul>
 * <li><code>CURVE_DISCONTINUITY</code>: The end point of a segment <code>n</code> does not coincide with the start
 * point of segment <code>n+1</code>.</li>
 * <li><code>CURVE_SELF_INTERSECTION</code>: Curve intersects itself.</li>
 * </ul>
 * </p>
 * <p>
 * <h2>Rings</h2>
 * The above events are also generated for {@link Ring} geometries (as they are a generally just closed {@link Curve}
 * s). Additionally, the following events may occur:
 * <ul>
 * <li><code>RING_NOT_CLOSED</code>: The end point does not coincide with the start point.</li>
 * <li><code>RING_SELF_INTERSECTION<code>: Ring intersects itself.</li>
 * </ul>
 * </p>
 * <p>
 * <h2>PolygonPatches (= planar surfaces)</h2>
 * The boundaries of a {@link PolygonPatch} are {@link Ring}s, which are tested and may generate the above events.
 * Additionally, the following events may occur:
 * <ul>
 * <li><code>EXTERIOR_RING_CCW</code>: The control points of an exterior ring (i.e. the shell) does not follow
 * counter-clockwise order.</li>
 * <li><code>INTERIOR_RING_CW</code>: The control points of an interior ring (i.e. a hole) of a {@link PolygonPatch}
 * does not follow clockwise order.</li>
 * <li><code>INTERIOR_RINGS_TOUCH</code>: Two interior rings touch.</li>
 * <li><code>INTERIOR_RINGS_INTERSECTS</code>: Two interior rings intersect.</li>
 * <li><code>INTERIOR_RINGS_WITHIN</code>: An interior ring lies inside another.</li>
 * <li><code>EXTERIOR_RING_TOUCHES_INTERIOR</code>: An interior ring touches the shell.</li>
 * <li><code>EXTERIOR_RING_INTERSECTS_INTERIOR</code>: An interior ring intersects the shell.</li>
 * <li><code>EXTERIOR_RING_WITHIN_INTERIOR</code>: The exterior ring lies inside an interior ring.</li>
 * </ul>
 * </p>
 * <p>
 * <h2>MultiGeometries</h2>
 * Members are tested individually for the above events.
 * </p>
 * 
 * @see GeometryValidationEventHandler
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryValidator {

    private static final Logger LOG = LoggerFactory.getLogger( GeometryValidator.class );

    private CurveLinearizer linearizer;

    private LinearizationCriterion crit;

    private org.deegree.geometry.GeometryFactory geomFac = new org.deegree.geometry.GeometryFactory();

    private GeometryFactory jtsFactory;

    private GeometryValidationEventHandler eventHandler;

    /**
     * Creates a new {@link GeometryValidator} which performs callbacks on the given
     * {@link GeometryValidationEventHandler} in case of errors.
     * 
     * @param eventHandler
     *            callback handler for errors, must not be <code>null</code>
     */
    public GeometryValidator( GeometryValidationEventHandler eventHandler ) {
        linearizer = new CurveLinearizer( new org.deegree.geometry.GeometryFactory() );
        crit = new NumPointsCriterion( 150 );
        jtsFactory = new GeometryFactory();
        this.eventHandler = eventHandler;
    }

    /**
     * Performs a topological validation of the given {@link Geometry}.
     * <p>
     * Contained geometry objects and geometry particles are recursively checked (e.g. the members of a
     * {@link MultiGeometry}) and callbacks to the associated {@link GeometryValidationEventHandler} are performed for
     * each detected issue.
     * 
     * @param geom
     *            geometry to be validated
     * @return true, if the geometry is valid, false otherwise (depends on the {@link GeometryValidationEventHandler}
     *         implementation)
     */
    public boolean validateGeometry( Geometry geom ) {
        return validateGeometry( geom, new ArrayList<Object>() );
    }

    private boolean validateGeometry( Geometry geom, List<Object> affectedGeometryParticles ) {

        boolean isValid = false;
        switch ( geom.getGeometryType() ) {
        case COMPOSITE_GEOMETRY: {
            isValid = validate( (CompositeGeometry<?>) geom, affectedGeometryParticles );
            break;
        }
        case ENVELOPE: {
            String msg = "Internal error: envelope 'geometries' should not occur here.";
            throw new IllegalArgumentException( msg );
        }
        case MULTI_GEOMETRY: {
            isValid = validate( (MultiGeometry<?>) geom, affectedGeometryParticles );
            break;
        }
        case PRIMITIVE_GEOMETRY: {
            isValid = validate( (GeometricPrimitive) geom, affectedGeometryParticles );
            break;
        }
        }
        return isValid;
    }

    private boolean validate( GeometricPrimitive geom, List<Object> affectedGeometryParticles ) {
        boolean isValid = true;
        switch ( geom.getPrimitiveType() ) {
        case Point: {
            LOG.debug( "Point geometry. No validation necessary." );
            break;
        }
        case Curve: {
            isValid = validateCurve( (Curve) geom, affectedGeometryParticles );
            break;
        }
        case Surface: {
            isValid = validateSurface( (Surface) geom, affectedGeometryParticles );
            break;
        }
        case Solid: {
            String msg = "Validation of solids is not available";
            throw new IllegalArgumentException( msg );
        }
        }
        return isValid;
    }

    private boolean validateCurve( Curve curve, List<Object> affectedGeometryParticles ) {
        boolean isValid = true;
        List<Object> affectedGeometryParticles2 = new ArrayList<Object>( affectedGeometryParticles );
        affectedGeometryParticles2.add( curve );
        LOG.debug( "Curve geometry. Testing for duplication of successive control points." );
        int segmentIdx = 0;
        for ( CurveSegment segment : curve.getCurveSegments() ) {
            if ( segment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                LineStringSegment lineStringSegment = (LineStringSegment) segment;
                Point lastPoint = null;
                for ( Point point : lineStringSegment.getControlPoints() ) {
                    if ( lastPoint != null ) {
                        if ( point.equals( lastPoint ) ) {
                            LOG.debug( "Found duplicate control points." );
                            if ( !fireEvent( new DuplicatePoints( curve, point, affectedGeometryParticles2 ) ) ) {
                                isValid = false;
                            }
                        }
                    }
                    lastPoint = point;
                }
            } else {
                LOG.warn( "Non-linear curve segment. Skipping check for duplicate control points." );
            }
            segmentIdx++;
        }

        LOG.debug( "Curve geometry. Testing segment continuity." );
        Point lastSegmentEndPoint = null;
        segmentIdx = 0;
        for ( CurveSegment segment : curve.getCurveSegments() ) {
            Point startPoint = segment.getStartPoint();
            if ( lastSegmentEndPoint != null ) {
                if ( startPoint.get0() != lastSegmentEndPoint.get0() || startPoint.get1() != lastSegmentEndPoint.get1() ) {
                    LOG.debug( "Found discontinuous segments." );
                    if ( !fireEvent( new CurveDiscontinuity( curve, segmentIdx, affectedGeometryParticles2 ) ) ) {
                        isValid = false;
                    }
                }
            }
            segmentIdx++;
            lastSegmentEndPoint = segment.getEndPoint();
        }

        LOG.debug( "Curve geometry. Testing for self-intersection." );
        LineString jtsLineString = getJTSLineString( curve );

        IsSimpleOp isSimpleOp = new IsSimpleOp( jtsLineString );
        boolean selfIntersection = !isSimpleOp.isSimple();
        if ( selfIntersection ) {
            LOG.debug( "Detected self-intersection." );
            Point location = getPoint( isSimpleOp.getNonSimpleLocation(), curve.getCoordinateSystem() );
            if ( !fireEvent( new CurveSelfIntersection( curve, location, affectedGeometryParticles2 ) ) ) {
                isValid = false;
            }
        }

        if ( curve instanceof Ring ) {
            LOG.debug( "Ring geometry. Testing if it's closed. " );
            if ( !curve.isClosed() ) {
                LOG.debug( "Not closed." );
                if ( !fireEvent( new RingNotClosed( (Ring) curve, affectedGeometryParticles2 ) ) ) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private boolean validateSurface( Surface surface, List<Object> affectedGeometryParticles ) {
        LOG.debug( "Surface geometry. Validating individual patches." );
        boolean isValid = true;
        List<Object> affectedGeometryParticles2 = new ArrayList<Object>( affectedGeometryParticles );
        affectedGeometryParticles2.add( surface );

        List<? extends SurfacePatch> patches = surface.getPatches();
        if ( patches.size() > 1 ) {
            LOG.warn( "Surface consists of multiple patches, but validation of inter-patch topology is not available yet." );
        }
        for ( SurfacePatch patch : surface.getPatches() ) {
            if ( !( patch instanceof PolygonPatch ) ) {
                LOG.warn( "Skipping validation of surface patch -- not a PolygonPatch." );
            } else {
                if ( !validatePatch( (PolygonPatch) patch, affectedGeometryParticles2 ) ) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private boolean validatePatch( PolygonPatch patch, List<Object> affectedGeometryParticles ) {

        boolean isValid = true;
        List<Object> affectedGeometryParticles2 = new ArrayList<Object>( affectedGeometryParticles );
        affectedGeometryParticles2.add( patch );
        LOG.debug( "Surface patch. Validating rings and spatial ring relations." );

        try {
            // validate and transform exterior ring to linearized JTS geometry
            Ring exteriorRing = patch.getExteriorRing();
            if ( !validateCurve( exteriorRing, affectedGeometryParticles2 ) ) {
                isValid = false;
            }
            LinearRing exteriorJTSRing = getJTSRing( exteriorRing );
            LOG.debug( "Surface patch. Validating exterior ring orientation." );
            boolean isClockwise = !CGAlgorithms.isCCW( exteriorJTSRing.getCoordinates() );
            if ( !fireEvent( new ExteriorRingOrientation( patch, isClockwise, affectedGeometryParticles2 ) ) ) {
                isValid = false;
            }
            Polygon exteriorJTSRingAsPolygons = jtsFactory.createPolygon( exteriorJTSRing, null );

            // validate and transform interior ring to linearized JTS geometries
            List<Ring> interiorRings = patch.getInteriorRings();
            List<LinearRing> interiorJTSRings = new ArrayList<LinearRing>( interiorRings.size() );
            List<Polygon> interiorJTSRingsAsPolygons = new ArrayList<Polygon>( interiorRings.size() );
            int interiorRingIdx = 0;
            for ( Ring interiorRing : interiorRings ) {
                if ( !validateCurve( interiorRing, affectedGeometryParticles2 ) ) {
                    isValid = false;
                }
                LinearRing interiorJTSRing = getJTSRing( interiorRing );
                LOG.debug( "Surface patch. Validating interior ring orientation." );
                interiorJTSRings.add( interiorJTSRing );
                isClockwise = !CGAlgorithms.isCCW( interiorJTSRing.getCoordinates() );
                if ( !fireEvent( new InteriorRingOrientation( patch, interiorRingIdx++, isClockwise,
                                                              affectedGeometryParticles2 ) ) ) {
                    isValid = false;
                }
                interiorJTSRingsAsPolygons.add( jtsFactory.createPolygon( interiorJTSRing, null ) );
            }

            // TODO implement more efficient algorithms for tests below
            LOG.debug( "Surface patch. Validating spatial relations between exterior ring and interior rings." );
            for ( int ringIdx = 0; ringIdx < interiorJTSRings.size(); ringIdx++ ) {
                LinearRing interiorJTSRing = interiorJTSRings.get( ringIdx );
                org.locationtech.jts.geom.Geometry intersection = interiorJTSRing.intersection( exteriorJTSRing );
                if ( !intersection.isEmpty() ) {
                    LOG.debug( "Exterior ring intersects interior ring." );
                    Point location = getPoint( intersection.getCoordinate(), null );
                    boolean singlePoint = isSinglePoint( intersection );
                    if ( !fireEvent( new InteriorRingIntersectsExterior( patch, ringIdx, location,
                                                                         affectedGeometryParticles2, singlePoint ) ) ) {
                        isValid = false;
                    }
                }
                if ( !interiorJTSRing.within( exteriorJTSRingAsPolygons ) ) {
                    LOG.debug( "Interior not within interior." );
                    if ( !fireEvent( new InteriorRingOutsideExterior( patch, ringIdx, affectedGeometryParticles2 ) ) ) {
                        isValid = false;
                    }
                }
                Polygon interiorJTSRingAsPolygon = interiorJTSRingsAsPolygons.get( ringIdx );
                if ( exteriorJTSRing.within( interiorJTSRingAsPolygon ) ) {
                    LOG.debug( "Exterior within interior." );
                    if ( !fireEvent( new InteriorRingOutsideExterior( patch, ringIdx, affectedGeometryParticles2 ) ) ) {
                        isValid = false;
                    }
                }
                if ( exteriorJTSRing.within( interiorJTSRingAsPolygon ) ) {
                    LOG.debug( "Exterior within interior." );
                    if ( !fireEvent( new InteriorRingOutsideExterior( patch, ringIdx, affectedGeometryParticles2 ) ) ) {
                        isValid = false;
                    }
                }
            }

            LOG.debug( "Surface patch. Validating spatial relations between pairs of interior rings." );
            for ( int ring1Idx = 0; ring1Idx < interiorJTSRings.size(); ring1Idx++ ) {
                for ( int ring2Idx = ring1Idx; ring2Idx < interiorJTSRings.size(); ring2Idx++ ) {
                    if ( ring1Idx == ring2Idx ) {
                        continue;
                    }
                    LinearRing interior1JTSRing = interiorJTSRings.get( ring1Idx );
                    LinearRing interior2JTSRing = interiorJTSRings.get( ring2Idx );
                    org.locationtech.jts.geom.Geometry intersection = interior1JTSRing.intersection( interior2JTSRing );
                    if ( !intersection.isEmpty() ) {
                        LOG.debug( "Interior ring intersects interior ring." );
                        Point location = getPoint( intersection.getCoordinate(), null );
                        boolean singlePoint = isSinglePoint( intersection );
                        if ( !fireEvent( new InteriorRingsIntersect( patch, ring1Idx, ring2Idx, location, singlePoint,
                                                                     affectedGeometryParticles2 ) ) ) {
                            isValid = false;
                        }
                    }
                    Polygon interior2JTSRingAsPolygon = interiorJTSRingsAsPolygons.get( ring2Idx );
                    if ( interior1JTSRing.within( interior2JTSRingAsPolygon ) ) {
                        LOG.debug( "Interior within interior." );
                        if ( !fireEvent( new InteriorRingsNested( patch, ring2Idx, ring1Idx, affectedGeometryParticles2 ) ) ) {
                            isValid = false;
                        }
                    }
                    Polygon interior1JTSRingAsPolygon = interiorJTSRingsAsPolygons.get( ring1Idx );
                    if ( interior2JTSRing.within( interior1JTSRingAsPolygon ) ) {
                        LOG.debug( "Interior within interior." );
                        if ( !fireEvent( new InteriorRingsNested( patch, ring1Idx, ring2Idx, affectedGeometryParticles2 ) ) ) {
                            isValid = false;
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            LOG.debug( "Validation interrupted: " + e.getMessage() );
        }

        return isValid;
    }

    private boolean isSinglePoint( org.locationtech.jts.geom.Geometry intersection ) {
        System.out.println (intersection);
        if ( intersection.getNumGeometries() != 1 ) {
            return false;
        }
        return intersection.getGeometryN( 0 ).getDimension() == 0;
    }

    private boolean validate( CompositeGeometry<?> geom, List<Object> affectedGeometryParticles ) {
        LOG.debug( "Composite geometry. Validating individual member geometries." );
        LOG.warn( "Composite geometry found, but validation of inter-primitive topology is not available yet." );
        boolean isValid = true;
        List<Object> affectedGeometryParticles2 = new ArrayList<Object>( affectedGeometryParticles );
        affectedGeometryParticles2.add( geom );
        for ( GeometricPrimitive geometricPrimitive : geom ) {
            if ( !validate( geometricPrimitive, affectedGeometryParticles2 ) ) {
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean validate( MultiGeometry<?> geom, List<Object> affectedGeometryParticles ) {
        LOG.debug( "MultiGeometry. Validating individual member geometries." );
        boolean isValid = true;
        List<Object> affectedGeometryParticles2 = new ArrayList<Object>( affectedGeometryParticles );
        affectedGeometryParticles2.add( geom );
        for ( Geometry member : geom ) {
            if ( !validateGeometry( member, affectedGeometryParticles2 ) ) {
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Returns a JTS geometry for the given {@link Curve} (which is linearized first).
     * 
     * @param curve
     *            {@link Curve} that consists of {@link LineStringSegment} and {@link Arc} segments only
     * @return linear JTS curve geometry
     * @throws IllegalArgumentException
     *             if the given input ring contains other segment types than {@link LineStringSegment}, {@link Arc} and
     *             {@link Circle}
     */
    private LineString getJTSLineString( Curve curve ) {

        Curve linearizedCurve = linearizer.linearize( curve, crit );
        List<Coordinate> coordinates = new LinkedList<Coordinate>();
        for ( CurveSegment segment : linearizedCurve.getCurveSegments() ) {
            for ( Point point : ( (LineStringSegment) segment ).getControlPoints() ) {
                coordinates.add( new Coordinate( point.get0(), point.get1() ) );
            }
        }
        return jtsFactory.createLineString( coordinates.toArray( new Coordinate[coordinates.size()] ) );
    }

    /**
     * Returns a JTS geometry for the given {@link Ring} (which is linearized first).
     * 
     * @param ring
     *            {@link Ring} that consists of {@link LineStringSegment}, {@link Arc} and {@link Circle} segments only
     * @return linear JTS ring geometry, null if no
     * @throws IllegalArgumentException
     *             if the given input ring contains other segment types than {@link LineStringSegment}, {@link Arc} and
     *             {@link Circle}
     */
    private LinearRing getJTSRing( Ring ring ) {

        Ring linearizedRing = (Ring) linearizer.linearize( ring, crit );
        List<Coordinate> coordinates = new LinkedList<Coordinate>();
        for ( Curve member : linearizedRing.getMembers() ) {
            for ( CurveSegment segment : member.getCurveSegments() ) {
                for ( Point point : ( (LineStringSegment) segment ).getControlPoints() ) {
                    coordinates.add( new Coordinate( point.get0(), point.get1() ) );
                }
            }
        }
        return jtsFactory.createLinearRing( coordinates.toArray( new Coordinate[coordinates.size()] ) );
    }

    private Point getPoint( Coordinate jtsCoord, ICRS crs ) {
        if ( jtsCoord == null ) {
            return null;
        }
        double[] coords = null;
        if ( jtsCoord.z != Double.NaN ) {
            coords = new double[] { jtsCoord.x, jtsCoord.y, jtsCoord.z };
        } else if ( jtsCoord.y != Double.NaN ) {
            coords = new double[] { jtsCoord.x, jtsCoord.y };
        } else {
            coords = new double[] { jtsCoord.x };
        }
        return geomFac.createPoint( null, coords, crs );
    }

    private boolean fireEvent( GeometryValidationEvent event ) {
        return eventHandler.fireEvent( event );
    }

}
