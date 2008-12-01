//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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
package org.deegree.model.geometry.validation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.composite.CompositeGeometry;
import org.deegree.model.geometry.linearization.CurveLinearizer;
import org.deegree.model.geometry.linearization.LinearizationCriterion;
import org.deegree.model.geometry.linearization.NumPointsCriterion;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.GeometricPrimitive;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.curvesegments.Arc;
import org.deegree.model.geometry.primitive.curvesegments.Circle;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

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
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryValidator {

    private static final Logger LOG = LoggerFactory.getLogger( GeometryValidator.class );

    private CurveLinearizer linearizer;

    private LinearizationCriterion crit;

    private GeometryFactory jtsFactory;

    private ValidationEventHandler eventHandler;

    /**
     * Creates a new {@link GeometryValidator} which performs callbacks on the given {@link ValidationEventHandler} in
     * case of errors.
     * 
     * @param eventHandler
     *            callback handler for errors
     */
    public GeometryValidator( ValidationEventHandler eventHandler ) {
        linearizer = new CurveLinearizer( GeometryFactoryCreator.getInstance().getGeometryFactory() );
        crit = new NumPointsCriterion( 150 );
        jtsFactory = new GeometryFactory();
        this.eventHandler = eventHandler;
    }

    /**
     * Performs a topological validation of the given {@link Geometry} and performs callbacks to the associated
     * {@link ValidationEventHandler}.
     * 
     * @param geom
     *            geometry to be validated
     * @return true, if the geometry is valid, false otherwise (depends on the {@link ValidationEventHandler}
     *         implementation)
     */
    public boolean validateGeometry( Geometry geom ) {
        boolean isValid = false;
        switch ( geom.getGeometryType() ) {
        case COMPOSITE_GEOMETRY: {
            isValid = validate( (CompositeGeometry<?>) geom );
            break;
        }
        case COMPOSITE_PRIMITIVE: {
            isValid = validate( (CompositeGeometry<?>) geom );
            break;
        }
        case ENVELOPE: {
            String msg = "Internal error: envelope 'geometries' should not occur here.";
            throw new IllegalArgumentException( msg );
        }
        case MULTI_GEOMETRY: {
            isValid = validate( (MultiGeometry<?>) geom );
            break;
        }
        case PRIMITIVE_GEOMETRY: {
            isValid = validate( (GeometricPrimitive) geom );
            break;
        }
        }
        return isValid;
    }

    private boolean validate( GeometricPrimitive geom ) {
        boolean isValid = true;
        switch ( geom.getPrimitiveType() ) {
        case Point: {
            LOG.debug( "Point geometry. No validation necessary." );
            break;
        }
        case Curve: {
            isValid = validateCurve( (Curve) geom );
            break;
        }
        case Surface: {
            isValid = validateSurface( (Surface) geom );
            break;
        }
        case Solid: {
            String msg = "Validation of solids is not available";
            throw new IllegalArgumentException( msg );
        }
        }
        return isValid;
    }

    private boolean validateCurve( Curve curve ) {
        boolean isValid = true;

        LOG.debug( "Curve geometry. Testing for duplicate successive control points." );
        int segmentIdx = 0;
        for ( CurveSegment segment : curve.getCurveSegments() ) {
            if ( segment.getSegmentType() == CurveSegmentType.LINE_STRING_SEGMENT ) {
                LineStringSegment lineStringSegment = (LineStringSegment) segment;
                Point lastPoint = null;
                for ( Point point : lineStringSegment.getControlPoints() ) {
                    if ( lastPoint != null ) {
                        if ( point.equals( lastPoint ) ) {
                            LOG.debug( "Found duplicate control points." );
                            if ( !eventHandler.curvePointDuplication( curve, point ) ) {
                                isValid = false;
                            }
                        }
                    }
                    lastPoint = point;
                }
            } else {
                LOG.warn( "Non-linear curve segment. Skipping check for duplicate control points." );
            }
        }

        LOG.debug( "Curve geometry. Testing segment continuity." );
        Point lastSegmentEndPoint = null;
        segmentIdx = 0;
        for ( CurveSegment segment : curve.getCurveSegments() ) {
            Point startPoint = segment.getStartPoint();
            if ( lastSegmentEndPoint != null ) {
                if ( startPoint.getX() != lastSegmentEndPoint.getX() || startPoint.getY() != lastSegmentEndPoint.getY() ) {
                    LOG.debug( "Found discontinuous segments." );
                    if ( !eventHandler.curveDiscontinuity( curve, segmentIdx ) ) {
                        isValid = false;
                    }
                }
            }
            segmentIdx++;
            lastSegmentEndPoint = segment.getEndPoint();
        }

        LOG.debug( "Curve geometry. Testing for self-intersection." );
        LineString jtsLineString = getJTSLineString( curve );
       
        boolean selfIntersection = !jtsLineString.isSimple();
        if ( selfIntersection ) {
            LOG.debug( "Detected self-intersection." );
            if ( !eventHandler.curveSelfIntersection( curve, null ) ) {
                isValid = false;
            }
        }

        if ( curve instanceof Ring ) {
            LOG.debug( "Ring geometry. Testing for self-intersection." );
            if ( selfIntersection ) {
                LOG.debug( "Detected self-intersection." );
                if ( !eventHandler.ringSelfIntersection( (Ring) curve, null ) ) {
                    isValid = false;
                }
            }
            LOG.debug( "Ring geometry. Testing if it's closed. " );
            if ( !curve.isClosed() ) {
                LOG.debug( "Not closed." );
                if ( !eventHandler.ringNotClosed( (Ring) curve ) ) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private boolean validateSurface( Surface surface ) {
        LOG.debug( "Surface geometry. Validating individual patches." );
        boolean isValid = true;

        List<SurfacePatch> patches = surface.getPatches();
        if ( patches.size() > 1 ) {
            LOG.warn( "Surface consists of multiple patches, but validation of inter-patch topology is not available yet." );
        }
        for ( SurfacePatch patch : surface.getPatches() ) {
            if ( !( patch instanceof PolygonPatch ) ) {
                LOG.warn( "Skipping validation of surface patch -- not a PolygonPatch." );
            } else {
                if ( !validatePatch( (PolygonPatch) patch ) ) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private boolean validatePatch( PolygonPatch patch ) {

        boolean isValid = true;
        LOG.debug( "Surface patch. Validating rings and spatial ring relations." );

        try {

            // validate and transform exterior ring to linearized JTS geometry
            Ring exteriorRing = patch.getExteriorRing();
            if ( !validateCurve( exteriorRing ) ) {
                isValid = false;
            }
            LinearRing exteriorJTSRing = getJTSRing( exteriorRing );
            LOG.debug( "Surface patch. Validating exterior ring orientation." );
            if ( !CGAlgorithms.isCCW( exteriorJTSRing.getCoordinates() ) ) {
                LOG.debug( "Wrong orientation." );
                if ( !eventHandler.exteriorRingCW( null, patch ) ) {
                    isValid = false;
                }
            }
            Polygon exteriorJTSRingAsPolygons =  jtsFactory.createPolygon( exteriorJTSRing, null );

            // validate and transform interior ring to linearized JTS geometries
            List<Ring> interiorRings = patch.getInteriorRings();
            List<LinearRing> interiorJTSRings = new ArrayList<LinearRing>( interiorRings.size() );
            List<Polygon> interiorJTSRingsAsPolygons = new ArrayList<Polygon>( interiorRings.size() );
            for ( Ring interiorRing : interiorRings ) {
                if ( !validateCurve( interiorRing ) ) {
                    isValid = false;
                }
                LinearRing interiorJTSRing = getJTSRing( interiorRing );
                LOG.debug( "Surface patch. Validating interior ring orientation." );
                interiorJTSRings.add( interiorJTSRing );
                if ( CGAlgorithms.isCCW( interiorJTSRing.getCoordinates() ) ) {
                    LOG.debug( "Wrong orientation." );
                    if ( !eventHandler.interiorRingCCW( null, patch ) ) {
                        isValid = false;
                    }
                }
                interiorJTSRingsAsPolygons.add( jtsFactory.createPolygon( interiorJTSRing, null ) );
            }

            // TODO implement more efficient algorithms for tests below

            LOG.debug( "Surface patch. Validating spatial relations between exterior ring and interior rings." );
            for ( int ringIdx = 0; ringIdx < interiorJTSRings.size(); ringIdx++ ) {
                LinearRing interiorJTSRing = interiorJTSRings.get( ringIdx );
                Polygon interiorJTSRingAsPolygon = interiorJTSRingsAsPolygons.get( ringIdx );
                if ( exteriorJTSRing.touches( interiorJTSRing ) ) {
                    LOG.debug( "Exterior touches interior." );
                    if ( !eventHandler.interiorRingTouchesExterior( null, patch, ringIdx ) ) {
                        isValid = false;
                    }
                }
                if ( exteriorJTSRing.intersects( interiorJTSRing ) ) {
                    LOG.debug( "Exterior intersects interior." );
                    if ( !eventHandler.interiorRingIntersectsExterior( null, patch, ringIdx ) ) {
                        isValid = false;
                    }
                }
                if ( !interiorJTSRing.within( exteriorJTSRingAsPolygons ) ) {
                    LOG.debug( "Interior not within interior." );
                    if ( !eventHandler.interiorRingOutsideExterior( null, patch, ringIdx ) ) {
                        isValid = false;
                    }
                }                
                if ( exteriorJTSRing.within( interiorJTSRingAsPolygon ) ) {
                    LOG.debug( "Exterior within interior." );
                    if ( !eventHandler.interiorRingOutsideExterior( null, patch, ringIdx ) ) {
                        isValid = false;
                    }
                }
                if ( exteriorJTSRing.within( interiorJTSRingAsPolygon ) ) {
                    LOG.debug( "Exterior within interior." );
                    if ( !eventHandler.interiorRingOutsideExterior( null, patch, ringIdx ) ) {
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
                    Polygon interior1JTSRingAsPolygon = interiorJTSRingsAsPolygons.get( ring1Idx );
                    LinearRing interior2JTSRing = interiorJTSRings.get( ring2Idx );
                    Polygon interior2JTSRingAsPolygon = interiorJTSRingsAsPolygons.get( ring2Idx );
                    if ( interior1JTSRing.touches( interior2JTSRing ) ) {
                        LOG.debug( "Interior touches interior." );
                        if ( !eventHandler.interiorRingsTouch( null, patch, ring1Idx, ring2Idx ) ) {
                            isValid = false;
                        }
                    }
                    if ( interior1JTSRing.intersects( interior2JTSRing ) ) {
                        LOG.debug( "Interior intersects interior." );
                        if ( !eventHandler.interiorRingsIntersect( null, patch, ring1Idx, ring2Idx ) ) {
                            isValid = false;
                        }
                    }
                    if ( interior1JTSRing.within( interior2JTSRingAsPolygon ) ) {
                        LOG.debug( "Interior within interior." );
                        if ( !eventHandler.interiorRingsWithin( null, patch, ring2Idx, ring1Idx ) ) {
                            isValid = false;
                        }
                    }
                    if ( interior2JTSRing.within( interior1JTSRingAsPolygon ) ) {
                        LOG.debug( "Interior within interior." );
                        if ( !eventHandler.interiorRingsWithin( null, patch, ring1Idx, ring2Idx ) ) {
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

    private boolean validate( CompositeGeometry<?> geom ) {
        LOG.debug( "Composite geometry. Validating individual member geometries." );
        LOG.warn( "Composite geometry found, but validation of inter-primitive topology is not available yet." );
        boolean isValid = true;
        for ( GeometricPrimitive geometricPrimitive : geom ) {
            if ( !validate( geometricPrimitive ) ) {
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean validate( MultiGeometry<?> geom ) {
        LOG.debug( "MultiGeometry. Validating individual member geometries." );
        boolean isValid = true;
        for ( Geometry member : geom ) {
            if ( !validateGeometry( member ) ) {
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
                coordinates.add( new Coordinate( point.getX(), point.getY() ) );
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
                    coordinates.add( new Coordinate( point.getX(), point.getY() ) );
                }
            }
        }
        return jtsFactory.createLinearRing( coordinates.toArray( new Coordinate[coordinates.size()] ) );
    }
}
