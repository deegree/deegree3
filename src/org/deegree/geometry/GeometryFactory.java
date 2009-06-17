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

import java.util.List;

import org.deegree.commons.types.Length;
import org.deegree.crs.CRS;
import org.deegree.crs.CRSRegistry;
import org.deegree.feature.gml.Angle;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
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
import org.deegree.geometry.standard.curvesegments.AffinePlacement;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public interface GeometryFactory {

    /**
     *
     * @return name of a factory
     */
    public String getName();

    /**
     * sets the name of a factory
     *
     * @param name
     *            name of a factory
     */
    public void setName( String name );

    /**
     *
     * @return short description of a factory and provided geometry implementaion
     */
    public String getDescription();

    /**
     * sets a short description of a factory and provided geometry implementaion
     *
     * @param description
     */
    public void setDescription( String description );

    /**
     * A concrete GeometryFactory possibly supports just a subset of concrete geometries defined by deegree's geometry
     * interfaces. At least simple geometries must be supported:
     * <ul>
     * <li>Point</li>
     * <li>Curve</li>
     * <li>Surface</li>
     * <li>MultiPoint</li>
     * <li>MultiCurve</li>
     * <li>MultiSurface</li>
     * </ul>
     *
     * @return list of supported geometry classes
     */
    public List<Class<?>> getSupportedGeometries();

    /**
     * @see #getSupportedGeometries()
     * @param supportedGeometries
     *            list of supported geometry classes
     */
    public void setSupportedGeometries( List<Class<?>> supportedGeometries );

    /**
     * Each GeometryFactory should a least support creating Curves with linear interpolation. Possible values are:
     * <ul>
     * <li>linear</li>
     * <li>geodesic</li>
     * <li>circularArc3Points</li>
     * <li>circularArc2PointWithBulge</li>
     * <li>elliptical</li>
     * <li>conic</li>
     * <li>cubicSpline</li>
     * <li>polynomialSpline</li>
     * <li>rationalSpline</li>
     * </ul>
     *
     * @return list of supported curve interpolations
     */
    public List<CurveSegment.Interpolation> getSupportedCurveInterpolations();

    /**
     * @see #getSupportedCurveInterpolations()
     * @param interpolations
     */
    public void setSupportedCurveInterpolations( List<CurveSegment.Interpolation> interpolations );

    /**
     * Each GeometryFactory should a least support creating Surfaces with none interpolation. Possible values are:
     * <ul>
     * <li>none</li>
     * <li>planar</li>
     * <li>spherical</li>
     * <li>elliptical</li>
     * <li>conic</li>
     * <li>tin</li>
     * <li>bilinear</li>
     * <li>biquadratic</li>
     * <li>bicubic</li>
     * <li>polynomialSpline</li>
     * <li>rationalSpline</li>
     * <li>triangulatedSpline</li>
     * </ul>
     *
     * @return list of supported surface interpolations
     */
    public List<SurfacePatch.Interpolation> getSupportedSurfaceInterpolations();

    /**
     * @see #getSupportedSurfaceInterpolations()
     * @param interpolations
     */
    public void setSupportedSurfaceInterpolations( List<SurfacePatch.Interpolation> interpolations );

    /**
     * Creates a georeferenced point with a default precision.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param x
     *            value for first coordinate
     * @param y
     *            value for second coordinate
     * @param crs
     *            points coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Point}
     */
    public Point createPoint( String id, double x, double y, CRS crs );

    /**
     * Creates a georeferenced point with a default precision.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param x
     *            value for first coordinate
     * @param y
     *            value for second coordinate
     * @param z
     *            value for third coordinate
     * @param crs
     *            points coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Point}
     */
    public Point createPoint( String id, double x, double y, double z, CRS crs );

    /**
     * creates a georeferenced point
     *
     * @param id
     *            identifier of the new geometry instance
     * @param coordinates
     *            coordinate values
     * @param precision
     *            precision of the point coordinates in units of the used CRS. This value will be used for comparing two
     *            points to be equal or not.
     * @param crs
     *            points coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Point}
     */
    public Point createPoint( String id, double[] coordinates, double precision, CRS crs );

    /**
     * creates a georeferenced point with a default precision (
     * {@link #createPoint(String, double[], double, CRS)})
     *
     * @param id
     *            identifier of the new geometry instance
     * @param coordinates
     *            coordinate values
     * @param crs
     *            points coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Point}
     */
    public Point createPoint( String id, double[] coordinates, CRS crs );

    /**
     * Creates a {@link LineString} geometry.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system
     * @param points
     *            list of control points for the line string
     * @return created {@link LineString}
     */
    public LineString createLineString( String id, CRS crs, List<Point> points );

    /**
     * Creates a {@link Curve} from a two dimensional array of coordinates. Each field of the first dimension represents
     * one point.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param coordinates
     *            array of curve coordinates
     * @param crs
     *            curves coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Curve}
     */
    public Curve createCurve( String id, Point[][] coordinates, CRS crs );

    /**
     * Creates a segmented {@link Curve} from one or more {@link CurveSegment}s. The last {@link Point} of i'th segment
     * must equals first {@link Point} of i+1'th segment.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param segments
     *            segments a curve shall be created from
     * @param crs
     *            curves coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Curve}
     */
    public Curve createCurve( String id, CurveSegment[] segments, CRS crs );

    /**
     * Creates a {@link LineStringSegment} curve segment.
     *
     * @param points
     *            list of points to create the {@link LineStringSegment} from
     * @return created {@link CurveSegment}
     */
    public LineStringSegment createLineStringSegment( List<Point> points );

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
    public Arc createArc( Point p1, Point p2, Point p3 );

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
    public ArcByBulge createArcByBulge( Point p1, Point p2, double bulge, Point normal );

    /**
     * Creates an {@link ArcByCenterPoint} curve segment.
     *
     * @param midPoint
     * @param radius
     * @param startAngle
     * @param endAngle
     * @return created {@link ArcByCenterPoint}
     */
    public ArcByCenterPoint createArcByCenterPoint( Point midPoint, Length radius, Angle startAngle, Angle endAngle );

    /**
     * Creates an {@link ArcString} curve segment.
     *
     * @param points
     *            list of control points, must contain <code>2 * k + 1</code> points
     * @return created {@link ArcString}
     */
    public ArcString createArcString( List<Point> points );

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
    public ArcStringByBulge createArcStringByBulge( List<Point> points, double[] bulges, List<Point> normals );

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
    public Bezier createBezier( List<Point> points, int degree, Knot knot1, Knot knot2 );

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
    public BSpline createBSpline( List<Point> points, int degree, List<Knot> knots, boolean isPolynomial );

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
    public Circle createCircle( Point p1, Point p2, Point p3 );

    /**
     * Creates an {@link CircleByCenterPoint} curve segment.
     *
     * @param midPoint
     * @param radius
     * @param startAngle
     * @return created {@link CircleByCenterPoint}
     */
    public CircleByCenterPoint createCircleByCenterPoint( Point midPoint, Length radius, Angle startAngle );

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
                                    double endParameter );

    /**
     * Creates a {@link Cone} surface patch.
     *
     * @param grid
     *             the grid of control points that defines the Cone
     * @return created {@link Cone}
     */
    public Cone createCone( List<List<Point>> grid );

    /**
     * Creates a {@link Cylidner} surface patch.
     *
     * @param grid
     *              the grid of control points that defines the Cylinder
     * @return created {@link Cylinder}
     */
    public Cylinder createCylinder( List<List<Point>> grid );

    /**
     * Creates a {@link Sphere} surface patch.
     *
     * @param grid
     *              the grid of control points that defines the Sphere
     * @return created {@link Sphere}
     */
    public Sphere createSphere( List<List<Point>> grid );

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
    public CubicSpline createCubicSpline( List<Point> points, Point vectorAtStart, Point vectorAtEnd );

    /**
     * Creates a {@link Geodesic} curve segment.
     *
     * @param p1
     *            first control point
     * @param p2
     *            second control point
     * @return created {@link Geodesic}
     */
    public Geodesic createGeodesic( Point p1, Point p2 );

    /**
     * Creates a {@link GeodesicString} curve segment.
     *
     * @param points
     *            control points, at least two
     * @return created {@link GeodesicString}
     */
    public GeodesicString createGeodesicString( List<Point> points );

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
    public OffsetCurve createOffsetCurve( Curve baseCurve, Point direction, Length distance );

    /**
     * creates a {@link Surface} from an array of {@link SurfacePatch}. The passed patches must touch in a topological
     * sense to form a valid {@link Surface}
     *
     * @param id
     *            identifier of the new geometry instance
     * @param patches
     *            patches to create a surface
     * @param crs
     *            surfaces coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Surface}
     */
    public Surface createSurface( String id, List<SurfacePatch> patches, CRS crs );

    /**
     * Creates a {@link Polygon} surface.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system. If the polygon does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @param exteriorRing
     *            ring that defines the outer boundary, this may be null (see section 9.2.2.5 of GML spec)
     * @param interiorRings
     *            list of rings that define the inner boundaries, may be empty or null
     * @return created {@link Polygon}
     */
    public Polygon createPolygon( String id, CRS crs, Ring exteriorRing, List<Ring> interiorRings );

    /**
     * Creates a {@link PolygonPatch} surface patch.
     *
     * @param exteriorRing
     *            ring that defines the outer boundary, this may be null (see section 9.2.2.5 of GML spec)
     * @param interiorRings
     *            list of rings that define the inner boundaries, may be empty or null
     * @return created {@link PolygonPatch}
     */
    public PolygonPatch createPolygonPatch( Ring exteriorRing, List<Ring> interiorRings );

    /**
     * creates an {@link Envelope} with a defined precision
     *
     * @param min
     *            minimum corner coordinates
     * @param max
     *            maximum corner coordinates
     * @param precision
     *            precision of the corner coordinates in units of the used CRS. This value will be used for comparing
     *            two points to be equal or not.
     * @param crs
     *            evenlopes coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Envelope}
     */
    public Envelope createEnvelope( double[] min, double[] max, double precision, CRS crs );

    /**
     * creates an {@link Envelope} with default precision
     *
     * @param min
     *            minimum corner coordinates
     * @param max
     *            maximum corner coordinates
     * @param crs
     *            evenlopes coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Envelope}
     */
    public Envelope createEnvelope( double[] min, double[] max, CRS crs );

    /**
     * creates an {@link Envelope} with default precision
     *
     * @param minx
     *            minimum x corner coordinates
     * @param miny
     *            minimum y corner coordinates
     * @param maxx
     *            miximum x corner coordinates
     * @param maxy
     *            miximum y corner coordinates
     * @param precision
     *            precision of the corner coordinates in units of the used CRS. This value will be used for comparing
     *            two points to be equal or not.
     *
     * @param crs
     *            evenlopes coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Envelope}
     */
    public Envelope createEnvelope( double minx, double miny, double maxx, double maxy, double precision,
                                    CRS crs );

    /**
     * creates an {@link Envelope} with default precision
     *
     * @param minx
     *            minimum x corner coordinates
     * @param miny
     *            minimum y corner coordinates
     * @param maxx
     *            miximum x corner coordinates
     * @param maxy
     *            miximum y corner coordinates
     *
     * @param crs
     *            evenlopes coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @return created {@link Envelope}
     */
    public Envelope createEnvelope( double minx, double miny, double maxx, double maxy, CRS crs );

    /**
     * creates an envelope from a SurfacePatch representing a envelope by being constructed by five points: minx,miny
     * minx,maxy maxx,maxy maxx,miny minx,miny
     *
     * @param id
     *            identifier of the new geometry instance
     * @param patch
     * @return envelope created from a SurfacePatch
     */
    public Envelope createEnvelope( String id, SurfacePatch patch );

    /**
     * Creates a {@link Ring} from a list of passed {@link Curve}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system
     * @param members
     *            the <code>Curve</code>s that compose the <code>Ring</code>
     * @return created {@link Ring}
     */
    public Ring createRing( String id, CRS crs, List<Curve> members );

    /**
     * Creates a simple {@link LinearRing} from a list of passed {@link Point}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system
     * @param points
     *            the control points
     * @return created {@link Ring}
     */
    public LinearRing createLinearRing( String id, CRS crs, List<Point> points );

    /**
     * Creates an {@link OrientableCurve}.
     *
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param baseCurve
     *            base curve
     * @param isReversed
     *            set to true, if the orientation of the base curve shall be reversed
     * @return created {@link OrientableCurve}
     */
    public OrientableCurve createOrientableCurve( String id, CRS crs, Curve baseCurve, boolean isReversed );

    /**
     * Creates a {@link Triangle} surface patch.
     *
     * @param exterior
     *            ring that contains exactly four planar points, the first and last point must be coincident
     * @return created {@link Triangle}
     */
    public Triangle createTriangle( LinearRing exterior );

    /**
     * Creates a {@link Rectangle} surface patch.
     *
     * @param exterior
     *            ring that contains exactly five planar points, the first and last point must be coincident
     * @return created {@link Rectangle}
     */
    public Rectangle createRectangle( LinearRing exterior );

    /**
     * Creates an {@link OrientableSurface}.
     *
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param baseSurface
     *            base surface
     * @param isReversed
     *            set to true, if the orientation of the base surface shall be reversed
     * @return created {@link OrientableCurve}
     */
    public OrientableSurface createOrientableSurface( String id, CRS crs, Surface baseSurface,
                                                      boolean isReversed );

    /**
     * Creates a {@link PolyhedralSurface}.
     *
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param memberPatches
     *            patches that constitute the surface
     * @return created {@link PolyhedralSurface}
     */
    public PolyhedralSurface createPolyhedralSurface( String id, CRS crs, List<PolygonPatch> memberPatches );

    /**
     * Creates a {@link TriangulatedSurface}.
     *
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param memberPatches
     *            patches that constitute the surface
     * @return created {@link TriangulatedSurface}
     */
    public TriangulatedSurface createTriangulatedSurface( String id, CRS crs, List<Triangle> memberPatches );

    /**
     * Creates a {@link Tin}.
     *
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param stopLines
     * @param breakLines
     * @param maxLength
     * @param controlPoints
     * @return created {@link Tin}
     */
    public Tin createTin( String id, CRS crs, List<List<LineStringSegment>> stopLines,
                          List<List<LineStringSegment>> breakLines, Length maxLength, List<Point> controlPoints,
                          List<Triangle> patches );

    /**
     * Creates a {@link Solid}.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            solids coordinate reference system. If a point does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @param exteriorSurface
     *            the exterior surface (shell) of the solid, may be null
     * @param interiorSurfaces
     *            the interior surfaces of the solid, may be null or empty
     * @return created {@link Solid}
     */
    public Solid createSolid( String id, CRS crs, Surface exteriorSurface, List<Surface> interiorSurfaces );

    /**
     * Creates an untyped multi geometry from a list of {@link Geometry}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, if the crs it is not known {@link CRSRegistry#lookupDummyCRS(String)}
     *            shall be used instead of <code>null</code>
     * @param geometries
     * @return created {@link MultiGeometry}
     */
    public MultiGeometry<Geometry> createMultiGeometry( String id, CRS crs, List<Geometry> geometries );

    /**
     * Creates a {@link MultiPoint} from a list of passed {@link Point}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, if the crs it is not known {@link CRSRegistry#lookupDummyCRS(String)}
     *            shall be used instead of <code>null</code>
     * @param members
     *            points that constitute the collection
     * @return created {@link MultiPoint}
     */
    public MultiPoint createMultiPoint( String id, CRS crs, List<Point> members );

    /**
     * Creates a {@link MultiCurve} from a list of passed {@link Curve}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, if the crs it is not known {@link CRSRegistry#lookupDummyCRS(String)}
     *            shall be used instead of <code>null</code>
     * @param members
     *            curves that constitute the collection
     * @return created {@link MultiCurve}
     */
    public MultiCurve createMultiCurve( String id, CRS crs, List<Curve> members );

    /**
     * Creates a {@link MultiCurve} from a list of passed {@link LineString}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, if the crs it is not known {@link CRSRegistry#lookupDummyCRS(String)}
     *            shall be used instead of <code>null</code>
     * @param members
     *            curves that constitute the collection
     * @return created {@link MultiLineString}
     */
    public MultiLineString createMultiLineString( String id, CRS crs, List<LineString> members );

    /**
     * Creates a {@link MultiSurface} from a list of passed {@link Surface}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, if the crs it is not known {@link CRSRegistry#lookupDummyCRS(String)}
     *            shall be used instead of <code>null</code>
     * @param members
     *            surfaces that constitute the collection
     * @return created {@link MultiSurface}
     */
    public MultiSurface createMultiSurface( String id, CRS crs, List<Surface> members );

    /**
     * Creates a {@link MultiPolygon} from a list of passed {@link Polygon}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, if the crs it is not known {@link CRSRegistry#lookupDummyCRS(String)}
     *            shall be used instead of <code>null</code>
     * @param members
     *            polygons that constitute the collection
     * @return created {@link MultiPolygon}
     */
    public MultiPolygon createMultiPolygon( String id, CRS crs, List<Polygon> members );

    /**
     * Creates a {@link MultiSolid} from a list of passed {@link Solid}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, if the crs it is not known {@link CRSRegistry#lookupDummyCRS(String)}
     *            shall be used instead of <code>null</code>
     * @param members
     *            solids that constitute the collection
     * @return created {@link MultiSolid}
     */
    public MultiSolid createMultiSolid( String id, CRS crs, List<Solid> members );

    /**
     * Creates a {@link CompositeCurve} from a list of passed {@link Curve}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system. If the curve does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @param members
     *            curves that constitute the composited curve, each curve must end at the start point of the subsequent
     *            curve in the list
     * @return created {@link CompositeCurve}
     */
    public CompositeCurve createCompositeCurve( String id, CRS crs, List<Curve> members );

    /**
     * Creates a {@link CompositeSurface} from a list of passed {@link Surface}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system. If the surface does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @param memberSurfaces
     *            surfaces that constitute the composited surface, the surfaces must join in pairs on common boundary
     *            curves and must, when considered as a whole, form a single surface
     * @return created {@link CompositeSurface}
     */
    public CompositeSurface createCompositeSurface( String id, CRS crs, List<Surface> memberSurfaces );

    /**
     * Creates a {@link CompositeSolid} from a list of passed {@link Solid}s.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system. If the solid does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @param memberSolids
     *            solids that constitute the composited solid, the solids must join in pairs on common boundary surfaces
     *            and which, when considered as a whole, form a single solid
     * @return created {@link CompositeSolid}
     */
    public CompositeSolid createCompositeSolid( String id, CRS crs, List<Solid> memberSolids );

    /**
     * Creates a general {@link CompositeGeometry} from a list of primitive geometries.
     *
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system. If the complex does not have a CRS or it is not known
     *            {@link CRSRegistry#lookupDummyCRS(String)} shall be used instead of <code>null</code>
     * @param memberPrimitives
     * @return created {@link CompositeGeometry}
     */
    public CompositeGeometry<GeometricPrimitive> createCompositeGeometry( String id, CRS crs,
                                                                          List<GeometricPrimitive> memberPrimitives );

    /**
     * Create an envelope of a list of Doubles
     *
     * @param lowerCorner
     * @param upperCorner
     * @param crs
     * @return the envelope
     */
    public Envelope createEnvelope( List<Double> lowerCorner, List<Double> upperCorner, CRS crs );
}
