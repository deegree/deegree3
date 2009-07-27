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

package org.deegree.geometry;

import java.util.Arrays;
import java.util.List;

import org.deegree.commons.types.Angle;
import org.deegree.commons.types.Length;
import org.deegree.crs.CRS;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.PolyhedralSurface;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;
import org.deegree.geometry.primitive.curvesegments.Arc;
import org.deegree.geometry.primitive.curvesegments.ArcByBulge;
import org.deegree.geometry.primitive.curvesegments.ArcByCenterPoint;
import org.deegree.geometry.primitive.curvesegments.ArcString;
import org.deegree.geometry.primitive.curvesegments.ArcStringByBulge;
import org.deegree.geometry.primitive.curvesegments.BSpline;
import org.deegree.geometry.primitive.curvesegments.Bezier;
import org.deegree.geometry.primitive.curvesegments.Circle;
import org.deegree.geometry.primitive.curvesegments.CircleByCenterPoint;
import org.deegree.geometry.primitive.curvesegments.Clothoid;
import org.deegree.geometry.primitive.curvesegments.CubicSpline;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.curvesegments.Geodesic;
import org.deegree.geometry.primitive.curvesegments.GeodesicString;
import org.deegree.geometry.primitive.curvesegments.Knot;
import org.deegree.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.geometry.primitive.curvesegments.OffsetCurve;
import org.deegree.geometry.primitive.surfacepatches.Cone;
import org.deegree.geometry.primitive.surfacepatches.Cylinder;
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.geometry.primitive.surfacepatches.Rectangle;
import org.deegree.geometry.primitive.surfacepatches.Sphere;
import org.deegree.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.geometry.primitive.surfacepatches.Triangle;
import org.deegree.geometry.standard.composite.DefaultCompositeCurve;
import org.deegree.geometry.standard.composite.DefaultCompositeGeometry;
import org.deegree.geometry.standard.composite.DefaultCompositeSolid;
import org.deegree.geometry.standard.composite.DefaultCompositeSurface;
import org.deegree.geometry.standard.curvesegments.AffinePlacement;
import org.deegree.geometry.standard.curvesegments.DefaultArc;
import org.deegree.geometry.standard.curvesegments.DefaultArcByBulge;
import org.deegree.geometry.standard.curvesegments.DefaultArcByCenterPoint;
import org.deegree.geometry.standard.curvesegments.DefaultArcString;
import org.deegree.geometry.standard.curvesegments.DefaultArcStringByBulge;
import org.deegree.geometry.standard.curvesegments.DefaultBSpline;
import org.deegree.geometry.standard.curvesegments.DefaultBezier;
import org.deegree.geometry.standard.curvesegments.DefaultCircle;
import org.deegree.geometry.standard.curvesegments.DefaultCircleByCenterPoint;
import org.deegree.geometry.standard.curvesegments.DefaultClothoid;
import org.deegree.geometry.standard.curvesegments.DefaultCubicSpline;
import org.deegree.geometry.standard.curvesegments.DefaultGeodesic;
import org.deegree.geometry.standard.curvesegments.DefaultGeodesicString;
import org.deegree.geometry.standard.curvesegments.DefaultLineStringSegment;
import org.deegree.geometry.standard.curvesegments.DefaultOffsetCurve;
import org.deegree.geometry.standard.multi.DefaultMultiCurve;
import org.deegree.geometry.standard.multi.DefaultMultiSolid;
import org.deegree.geometry.standard.multi.DefaultMultiSurface;
import org.deegree.geometry.standard.primitive.DefaultCurve;
import org.deegree.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.geometry.standard.primitive.DefaultOrientableCurve;
import org.deegree.geometry.standard.primitive.DefaultOrientableSurface;
import org.deegree.geometry.standard.primitive.DefaultPolyhedralSurface;
import org.deegree.geometry.standard.primitive.DefaultRing;
import org.deegree.geometry.standard.primitive.DefaultSolid;
import org.deegree.geometry.standard.primitive.DefaultSurface;
import org.deegree.geometry.standard.primitive.DefaultTin;
import org.deegree.geometry.standard.primitive.DefaultTriangulatedSurface;
import org.deegree.geometry.standard.surfacepatches.DefaultCone;
import org.deegree.geometry.standard.surfacepatches.DefaultCylinder;
import org.deegree.geometry.standard.surfacepatches.DefaultPolygonPatch;
import org.deegree.geometry.standard.surfacepatches.DefaultRectangle;
import org.deegree.geometry.standard.surfacepatches.DefaultSphere;
import org.deegree.geometry.standard.surfacepatches.DefaultTriangle;

/**
 * Augments the {@link SimpleGeometryFactory} with additional methods for building complex {@link Geometry} and
 * geometry-related objects (e.g. {@link CurveSegment})s.
 * 
 * @see SimpleGeometryFactory
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class GeometryFactory extends SimpleGeometryFactory {

    public GeometryFactory () {
        this.pm = PrecisionModel.DEFAULT_PRECISION_MODEL;
    }
    
    public GeometryFactory (PrecisionModel pm) {
        this.pm = pm;
    }

    /**
     * Creates a segmented {@link Curve} from one or more {@link CurveSegment}s. The last {@link Point} of segment
     * <code>i</code> must equal the first {@link Point} of segment <code>i+1</code>.
     * 
     * @param id
     *            identifier of the new geometry instance
     * @param segments
     *            segments a curve shall be created from
     * @param crs
     *            coordinate reference system
     * @return created {@link Curve}
     */
    public Curve createCurve( String id, CurveSegment[] segments, CRS crs ) {
        return new DefaultCurve( id, crs, pm, Arrays.asList( segments ) );
    }

    /**
     * Creates a {@link LineStringSegment} curve segment.
     * 
     * @param points
     *            points to create the {@link LineStringSegment} from
     * @return created {@link CurveSegment}
     */
    public LineStringSegment createLineStringSegment( Points points ) {
        return new DefaultLineStringSegment( points );
    }

    /**
     * Creates an {@link Arc} curve segment.
     * 
     * @param p1
     *            first control point
     * @param p2
     *            second control point
     * @param p3
     *            third control point
     * 
     * @return created {@link Arc}
     */
    public Arc createArc( Point p1, Point p2, Point p3 ) {
        return new DefaultArc( p1, p2, p3 );
    }

    /**
     * Creates an {@link ArcByBulge} curve segment.
     * 
     * @param p1
     *            first control point
     * @param p2
     *            second control point
     * @param bulge
     *            height of the arc (multiplier for the normals)
     * @param normal
     *            normal vector, in 2D only one coordinate is necessary
     * @return created {@link ArcStringByBulge}
     */
    public ArcByBulge createArcByBulge( Point p1, Point p2, double bulge, Point normal ) {
        return new DefaultArcByBulge( p1, p2, bulge, normal );
    }

    /**
     * Creates an {@link ArcByCenterPoint} curve segment.
     * 
     * @param midPoint
     * @param radius
     * @param startAngle
     * @param endAngle
     * @return created {@link ArcByCenterPoint}
     */
    public ArcByCenterPoint createArcByCenterPoint( Point midPoint, Length radius, Angle startAngle, Angle endAngle ) {
        return new DefaultArcByCenterPoint( midPoint, radius, startAngle, endAngle );
    }

    /**
     * Creates an {@link ArcString} curve segment.
     * 
     * @param points
     *            control points, must contain <code>2 * k + 1</code> points
     * @return created {@link ArcString}
     */
    public ArcString createArcString( Points points ) {
        return new DefaultArcString( points );
    }

    /**
     * Creates an {@link ArcStringByBulge} curve segment.
     * <p>
     * This variant of the arc computes the mid points of the arcs instead of storing the coordinates directly. The
     * control point sequence consists of the start and end points of each arc plus the bulge.
     * 
     * @param points
     *            list of control points, must contain at least two points
     * @param bulges
     *            heights of the arcs (multipliers for the normals)
     * @param normals
     *            normal vectors
     * @return created {@link ArcStringByBulge}
     */
    public ArcStringByBulge createArcStringByBulge( Points points, double[] bulges, Points normals ) {
        return new DefaultArcStringByBulge( points, bulges, normals );
    }

    /**
     * Creates a {@link Bezier} curve segment.
     * 
     * @param points
     *            list of control points
     * @param degree
     *            polynomial degree of the spline
     * @param knot1
     *            first of the two knots that define the spline basis functions
     * @param knot2
     *            second of the two knots that define the spline basis functions
     * @return created {@link Bezier}
     */
    public Bezier createBezier( Points points, int degree, Knot knot1, Knot knot2 ) {
        return new DefaultBezier( points, degree, knot1, knot2 );
    }

    /**
     * Creates a {@link BSpline} curve segment.
     * 
     * @param points
     *            list of control points
     * @param degree
     *            polynomial degree of the spline
     * @param knots
     *            sequence of distinct knots that define the spline basis functions
     * @param isPolynomial
     *            set to true if this is a polynomial spline, otherwise it's a rational spline
     * @return created {@link BSpline}
     */
    public BSpline createBSpline( Points points, int degree, List<Knot> knots, boolean isPolynomial ) {
        return new DefaultBSpline( points, degree, knots, isPolynomial );
    }

    /**
     * Creates a {@link Circle} curve segment.
     * 
     * @param p1
     *            first control point
     * @param p2
     *            second control point
     * @param p3
     *            third control point
     * 
     * @return created {@link Arc}
     */
    public Circle createCircle( Point p1, Point p2, Point p3 ) {
        return new DefaultCircle( p1, p2, p3 );
    }

    /**
     * Creates an {@link CircleByCenterPoint} curve segment.
     * 
     * @param midPoint
     * @param radius
     * @param startAngle
     * @return created {@link CircleByCenterPoint}
     */
    public CircleByCenterPoint createCircleByCenterPoint( Point midPoint, Length radius, Angle startAngle ) {
        return new DefaultCircleByCenterPoint( midPoint, radius, startAngle );
    }

    /**
     * Creates a {@link Geodesic} curve segment.
     * 
     * @param p1
     *            first control point
     * @param p2
     *            second control point
     * @return created {@link Geodesic}
     */
    public Geodesic createGeodesic( Point p1, Point p2 ) {
        return new DefaultGeodesic( p1, p2 );
    }

    /**
     * Creates a {@link GeodesicString} curve segment.
     * 
     * @param points
     *            control points, at least two
     * @return created {@link GeodesicString}
     */
    public GeodesicString createGeodesicString( Points points ) {
        return new DefaultGeodesicString( points );
    }

    /**
     * Creates an {@link OffsetCurve} curve segment.
     * 
     * @param baseCurve
     *            the base geometry
     * @param direction
     *            the direction of the offset
     * @param distance
     *            the distance from the base curve
     * @return created {@link GeodesicString}
     */
    public OffsetCurve createOffsetCurve( Curve baseCurve, Point direction, Length distance ) {
        return new DefaultOffsetCurve( baseCurve, direction, distance );
    }

    /**
     * Creates a {@link Surface} that consists of a number of {@link SurfacePatch} instances. The passed patches must
     * touch in a topological sense to form a valid {@link Surface}.
     * 
     * @param id
     *            identifier of the new geometry instance
     * @param patches
     *            patches to create a surface
     * @param crs
     *            coordinate reference system, may be null
     * @return created {@link Surface}
     */
    public Surface createSurface( String id, List<SurfacePatch> patches, CRS crs ) {
        return new DefaultSurface( id, crs, pm, patches );
    }

    /**
     * Creates a {@link PolygonPatch} surface patch.
     * 
     * @param exteriorRing
     *            ring that defines the outer boundary, this may be null (see section 9.2.2.5 of GML spec)
     * @param interiorRings
     *            list of rings that define the inner boundaries, may be empty or null
     * @return created {@link PolygonPatch}
     */
    public PolygonPatch createPolygonPatch( Ring exteriorRing, List<Ring> interiorRings ) {
        return new DefaultPolygonPatch( exteriorRing, interiorRings );
    }

    /**
     * Creates a {@link Ring} from a list of passed {@link Curve}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            the <code>Curve</code>s that compose the <code>Ring</code>
     * @return created {@link Ring}
     */
    public Ring createRing( String id, CRS crs, List<Curve> members ) {
        return new DefaultRing( id, crs, pm, members );
    }

    /**
     * Creates a simple {@link LinearRing} from a list of passed {@link Point}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param points
     *            control points
     * @return created {@link Ring}
     */
    public LinearRing createLinearRing( String id, CRS crs, Points points ) {
        return new DefaultLinearRing( id, crs, pm, points );
    }

    /**
     * Creates an {@link OrientableCurve}.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param baseCurve
     *            base curve
     * @param isReversed
     *            set to true, if the orientation of the base curve shall be reversed in the created geometry
     * @return created {@link OrientableCurve}
     */
    public OrientableCurve createOrientableCurve( String id, CRS crs, Curve baseCurve, boolean isReversed ) {
        return new DefaultOrientableCurve( id, crs, baseCurve, isReversed );
    }

    /**
     * Creates a {@link Triangle} surface patch.
     * 
     * @param exterior
     *            ring that contains exactly four planar points, the first and last point must be coincident
     * @return created {@link Triangle}
     */
    public Triangle createTriangle( LinearRing exterior ) {
        return new DefaultTriangle( exterior );
    }

    /**
     * Creates a {@link Rectangle} surface patch.
     * 
     * @param exterior
     *            ring that contains exactly five planar points, the first and last point must match
     * @return created {@link Rectangle}
     */
    public Rectangle createRectangle( LinearRing exterior ) {
        return new DefaultRectangle( exterior );
    }

    /**
     * Creates an {@link OrientableSurface}.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param baseSurface
     *            base surface
     * @param isReversed
     *            set to true, if the orientation of the base surface shall be reversed
     * @return created {@link OrientableCurve}
     */
    public OrientableSurface createOrientableSurface( String id, CRS crs, Surface baseSurface, boolean isReversed ) {
        return new DefaultOrientableSurface( id, crs, baseSurface, isReversed );
    }

    /**
     * Creates a {@link PolyhedralSurface}.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param memberPatches
     *            patches that constitute the surface
     * @return created {@link PolyhedralSurface}
     */
    public PolyhedralSurface createPolyhedralSurface( String id, CRS crs, List<PolygonPatch> memberPatches ) {
        return new DefaultPolyhedralSurface( id, crs, pm, memberPatches );
    }

    /**
     * Creates a {@link TriangulatedSurface}.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param memberPatches
     *            patches that constitute the surface
     * @return created {@link TriangulatedSurface}
     */
    public TriangulatedSurface createTriangulatedSurface( String id, CRS crs, List<Triangle> memberPatches ) {
        return new DefaultTriangulatedSurface( id, crs, pm, memberPatches );
    }

    /**
     * Creates a {@link Tin}.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param stopLines
     * @param breakLines
     * @param maxLength
     * @param controlPoints
     * @param patches 
     * @return created {@link Tin}
     */
    public Tin createTin( String id, CRS crs, List<List<LineStringSegment>> stopLines,
                          List<List<LineStringSegment>> breakLines, Length maxLength, Points controlPoints,
                          List<Triangle> patches ) {
        return new DefaultTin( id, crs, pm, stopLines, breakLines, maxLength, controlPoints, patches );
    }

    /**
     * Creates a {@link Clothoid} curve segment.
     * 
     * @param referenceLocation
     *            the affine mapping that places the curve defined by the Fresnel Integrals into the coordinate
     *            reference system of this object
     * @param scaleFactor
     *            the value for the constant in the Fresnel's integrals
     * @param startParameter
     *            the arc length distance from the inflection point that will be the start point for this curve segment
     * @param endParameter
     *            the arc length distance from the inflection point that will be the end point for this curve segment
     * @return created {@link Clothoid}
     */
    public Clothoid createClothoid( AffinePlacement referenceLocation, double scaleFactor, double startParameter,
                                    double endParameter ) {
        return new DefaultClothoid( referenceLocation, scaleFactor, startParameter, endParameter );
    }

    /**
     * Creates a {@link Cone} surface patch.
     * 
     * @param grid
     *            the grid of control points that defines the Cone
     * @return created {@link Cone}
     */
    public Cone createCone( List<Points> grid ) {
        return new DefaultCone( grid );
    }

    /**
     * Creates a {@link Cylinder} surface patch.
     * 
     * @param grid
     *            the grid of control points that defines the Cylinder
     * @return created {@link Cylinder}
     */
    public Cylinder createCylinder( List<Points> grid ) {
        return new DefaultCylinder( grid );
    }

    /**
     * Creates a {@link Sphere} surface patch.
     * 
     * @param grid
     *            the grid of control points that defines the Sphere
     * @return created {@link Sphere}
     */
    public Sphere createSphere( List<Points> grid ) {
        return new DefaultSphere( grid );
    }

    /**
     * Creates a {@link Clothoid} curve segment.
     * 
     * @param points
     *            control points, at least two
     * @param vectorAtStart
     *            the unit tangent vector at the start point of the spline
     * @param vectorAtEnd
     *            the unit tangent vector at the end point of the spline
     * @return created {@link Clothoid}
     */
    public CubicSpline createCubicSpline( Points points, Point vectorAtStart, Point vectorAtEnd ) {
        return new DefaultCubicSpline( points, vectorAtStart, vectorAtEnd );
    }    
    
    /**
     * Creates a {@link Solid}.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param exteriorSurface
     *            the exterior surface (shell) of the solid, may be null
     * @param interiorSurfaces
     *            the interior surfaces of the solid, may be null or empty
     * @return created {@link Solid}
     */
    public Solid createSolid( String id, CRS crs, Surface exteriorSurface, List<Surface> interiorSurfaces ) {
        return new DefaultSolid( id, crs, pm, exteriorSurface, interiorSurfaces );
    }

    /**
     * Creates a {@link MultiCurve} from a list of passed {@link Curve}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            curves that constitute the collection
     * @return created {@link MultiCurve}
     */
    public MultiCurve createMultiCurve( String id, CRS crs, List<Curve> members ) {
        return new DefaultMultiCurve( id, crs, pm, members );
    }

    /**
     * Creates a {@link MultiSurface} from a list of passed {@link Surface}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            surfaces that constitute the collection
     * @return created {@link MultiSurface}
     */
    public MultiSurface createMultiSurface( String id, CRS crs, List<Surface> members ) {
        return new DefaultMultiSurface( id, crs, pm, members );
    }

    /**
     * Creates a {@link MultiSolid} from a list of passed {@link Solid}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            solids that constitute the collection
     * @return created {@link MultiSolid}
     */
    public MultiSolid createMultiSolid( String id, CRS crs, List<Solid> members ) {
        return new DefaultMultiSolid( id, crs, pm, members );
    }    
    
    /**
     * Creates a {@link CompositeCurve} from a list of passed {@link Curve}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            curves that constitute the composited curve, each curve must end at the start point of the subsequent
     *            curve in the list
     * @return created {@link CompositeCurve}
     */
    public CompositeCurve createCompositeCurve( String id, CRS crs, List<Curve> members ) {
        return new DefaultCompositeCurve( id, crs, pm, members );
    }

    /**
     * Creates a {@link CompositeSurface} from a list of passed {@link Surface}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param memberSurfaces
     *            surfaces that constitute the composited surface, the surfaces must join in pairs on common boundary
     *            curves and must, when considered as a whole, form a single surface
     * @return created {@link CompositeSurface}
     */
    public CompositeSurface createCompositeSurface( String id, CRS crs, List<Surface> memberSurfaces ) {
        return new DefaultCompositeSurface( id, crs, pm, memberSurfaces );
    }

    /**
     * Creates a {@link CompositeSolid} from a list of passed {@link Solid}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param memberSolids
     *            solids that constitute the composited solid, the solids must join in pairs on common boundary surfaces
     *            and which, when considered as a whole, form a single solid
     * @return created {@link CompositeSolid}
     */
    public CompositeSolid createCompositeSolid( String id, CRS crs, List<Solid> memberSolids ) {
        return new DefaultCompositeSolid( id, crs, pm, memberSolids );
    }

    /**
     * Creates a general {@link CompositeGeometry} from a list of primitive geometries.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param memberPrimitives
     * @return created {@link CompositeGeometry}
     */
    public CompositeGeometry<GeometricPrimitive> createCompositeGeometry( String id, CRS crs,
                                                                          List<GeometricPrimitive> memberPrimitives ) {
        return new DefaultCompositeGeometry( id, crs, pm, memberPrimitives );
    }
}
