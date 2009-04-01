//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/geometry/jtswrapper/JTSWrapperGeometryFactory.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

import java.util.Arrays;
import java.util.List;

import org.deegree.commons.types.Length;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.AbstractGeometryFactory;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.composite.CompositeCurve;
import org.deegree.model.geometry.composite.CompositeGeometry;
import org.deegree.model.geometry.composite.CompositeSolid;
import org.deegree.model.geometry.composite.CompositeSurface;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.multi.MultiLineString;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiPolygon;
import org.deegree.model.geometry.multi.MultiSolid;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.GeometricPrimitive;
import org.deegree.model.geometry.primitive.LineString;
import org.deegree.model.geometry.primitive.LinearRing;
import org.deegree.model.geometry.primitive.OrientableCurve;
import org.deegree.model.geometry.primitive.OrientableSurface;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Polygon;
import org.deegree.model.geometry.primitive.PolyhedralSurface;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.Tin;
import org.deegree.model.geometry.primitive.TriangulatedSurface;
import org.deegree.model.geometry.primitive.curvesegments.Arc;
import org.deegree.model.geometry.primitive.curvesegments.ArcByBulge;
import org.deegree.model.geometry.primitive.curvesegments.ArcByCenterPoint;
import org.deegree.model.geometry.primitive.curvesegments.ArcString;
import org.deegree.model.geometry.primitive.curvesegments.ArcStringByBulge;
import org.deegree.model.geometry.primitive.curvesegments.BSpline;
import org.deegree.model.geometry.primitive.curvesegments.Bezier;
import org.deegree.model.geometry.primitive.curvesegments.Circle;
import org.deegree.model.geometry.primitive.curvesegments.CircleByCenterPoint;
import org.deegree.model.geometry.primitive.curvesegments.Clothoid;
import org.deegree.model.geometry.primitive.curvesegments.CubicSpline;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.Geodesic;
import org.deegree.model.geometry.primitive.curvesegments.GeodesicString;
import org.deegree.model.geometry.primitive.curvesegments.Knot;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.geometry.primitive.curvesegments.OffsetCurve;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.model.geometry.primitive.surfacepatches.Rectangle;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.model.geometry.primitive.surfacepatches.Triangle;
import org.deegree.model.geometry.standard.aggregate.DefaultMultiCurve;
import org.deegree.model.geometry.standard.aggregate.DefaultMultiGeometry;
import org.deegree.model.geometry.standard.aggregate.DefaultMultiLineString;
import org.deegree.model.geometry.standard.aggregate.DefaultMultiPoint;
import org.deegree.model.geometry.standard.aggregate.DefaultMultiPolygon;
import org.deegree.model.geometry.standard.aggregate.DefaultMultiSolid;
import org.deegree.model.geometry.standard.aggregate.DefaultMultiSurface;
import org.deegree.model.geometry.standard.composite.DefaultCompositeCurve;
import org.deegree.model.geometry.standard.composite.DefaultCompositeGeometry;
import org.deegree.model.geometry.standard.composite.DefaultCompositeSolid;
import org.deegree.model.geometry.standard.composite.DefaultCompositeSurface;
import org.deegree.model.geometry.standard.curvesegments.AffinePlacement;
import org.deegree.model.geometry.standard.curvesegments.DefaultArc;
import org.deegree.model.geometry.standard.curvesegments.DefaultArcByBulge;
import org.deegree.model.geometry.standard.curvesegments.DefaultArcByCenterPoint;
import org.deegree.model.geometry.standard.curvesegments.DefaultArcString;
import org.deegree.model.geometry.standard.curvesegments.DefaultArcStringByBulge;
import org.deegree.model.geometry.standard.curvesegments.DefaultBSpline;
import org.deegree.model.geometry.standard.curvesegments.DefaultBezier;
import org.deegree.model.geometry.standard.curvesegments.DefaultCircle;
import org.deegree.model.geometry.standard.curvesegments.DefaultCircleByCenterPoint;
import org.deegree.model.geometry.standard.curvesegments.DefaultClothoid;
import org.deegree.model.geometry.standard.curvesegments.DefaultCubicSpline;
import org.deegree.model.geometry.standard.curvesegments.DefaultGeodesic;
import org.deegree.model.geometry.standard.curvesegments.DefaultGeodesicString;
import org.deegree.model.geometry.standard.curvesegments.DefaultLineStringSegment;
import org.deegree.model.geometry.standard.curvesegments.DefaultOffsetCurve;
import org.deegree.model.geometry.standard.primitive.DefaultCurve;
import org.deegree.model.geometry.standard.primitive.DefaultEnvelope;
import org.deegree.model.geometry.standard.primitive.DefaultLineString;
import org.deegree.model.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.model.geometry.standard.primitive.DefaultOrientableCurve;
import org.deegree.model.geometry.standard.primitive.DefaultOrientableSurface;
import org.deegree.model.geometry.standard.primitive.DefaultPoint;
import org.deegree.model.geometry.standard.primitive.DefaultPolygon;
import org.deegree.model.geometry.standard.primitive.DefaultPolyhedralSurface;
import org.deegree.model.geometry.standard.primitive.DefaultRing;
import org.deegree.model.geometry.standard.primitive.DefaultSolid;
import org.deegree.model.geometry.standard.primitive.DefaultSurface;
import org.deegree.model.geometry.standard.primitive.DefaultTin;
import org.deegree.model.geometry.standard.primitive.DefaultTriangulatedSurface;
import org.deegree.model.geometry.standard.surfacepatches.DefaultPolygonPatch;
import org.deegree.model.geometry.standard.surfacepatches.DefaultRectangle;
import org.deegree.model.geometry.standard.surfacepatches.DefaultTriangle;
import org.deegree.model.gml.Angle;

/**
 * 
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultGeometryFactory extends AbstractGeometryFactory {

    @Override
    public ArcByCenterPoint createArcByCenterPoint( Point midPoint, Length radius, Angle startAngle, Angle endAngle ) {
        return new DefaultArcByCenterPoint( midPoint, radius, startAngle, endAngle );
    }

    @Override
    public ArcString createArcString( List<Point> points ) {
        return new DefaultArcString( points );
    }

    @Override
    public ArcStringByBulge createArcStringByBulge( List<Point> points, double[] bulges, List<Point> normals ) {
        return new DefaultArcStringByBulge( points, bulges, normals );
    }

    @Override
    public CompositeCurve createCompositeCurve( String id, CoordinateSystem crs, List<Curve> memberCurves ) {
        return new DefaultCompositeCurve( id, crs, memberCurves );
    }

    @Override
    public CompositeSolid createCompositeSolid( String id, CoordinateSystem crs, List<Solid> memberSolids ) {
        return new DefaultCompositeSolid( id, crs, memberSolids );
    }

    @Override
    public CompositeSurface createCompositeSurface( String id, CoordinateSystem crs, List<Surface> memberSurfaces ) {
        return new DefaultCompositeSurface( id, crs, memberSurfaces );
    }

    @Override
    public CompositeGeometry<GeometricPrimitive> createCompositeGeometry( String id, CoordinateSystem crs,
                                                                          List<GeometricPrimitive> memberPrimitives ) {
        return new DefaultCompositeGeometry( id, crs, memberPrimitives );
    }

    @Override
    public Curve createCurve( String id, Point[][] coordinates, CoordinateSystem crs ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Curve createCurve( String id, CurveSegment[] segments, CoordinateSystem crs ) {
        return new DefaultCurve( id, crs, Arrays.asList( segments ) );
    }

    @Override
    public Envelope createEnvelope( double[] min, double[] max, double precision, CoordinateSystem crs ) {
        return new DefaultEnvelope( null, crs, new DefaultPoint( null, crs, min ), new DefaultPoint( null, crs, max ) );
    }

    @Override
    public Envelope createEnvelope( double[] min, double[] max, CoordinateSystem crs ) {
        return new DefaultEnvelope( null, crs, new DefaultPoint( null, crs, min ), new DefaultPoint( null, crs, max ) );
    }

    @Override
    public Envelope createEnvelope( double minx, double miny, double maxx, double maxy, CoordinateSystem crs ) {
        return createEnvelope( new double[] { minx, miny }, new double[] { maxx, maxy }, crs );
    }

    @Override
    public Envelope createEnvelope( List<Double> lowerCorner, List<Double> upperCorner, CoordinateSystem crs ) {
        if ( lowerCorner.size() != upperCorner.size() ) {
            throw new IllegalArgumentException( "LowerCorner must be of same dimension as upperCorner." );
        }
        double[] lc = new double[lowerCorner.size()];
        double[] uc = new double[upperCorner.size()];
        for ( int i = 0; i < lc.length; ++i ) {
            lc[i] = lowerCorner.get( i );
            uc[i] = upperCorner.get( i );
        }
        return createEnvelope( lc, uc, crs );
    }

    @Override
    public Envelope createEnvelope( double minx, double miny, double maxx, double maxy, double precision,
                                    CoordinateSystem crs ) {
        return createEnvelope( new double[] { minx, miny }, new double[] { maxx, maxy }, precision, crs );
    }

    @Override
    public Envelope createEnvelope( String id, SurfacePatch patch ) {
        throw new UnsupportedOperationException( "not yet supported" );
    }

    @Override
    public LineStringSegment createLineStringSegment( List<Point> points ) {
        return new DefaultLineStringSegment( points );
    }

    @Override
    public Point createPoint( String id, double x, double y, CoordinateSystem crs ) {
        return new DefaultPoint( id, crs, new double[] { x, y } );
    }

    @Override
    public Point createPoint( String id, double x, double y, double z, CoordinateSystem crs ) {
        return new DefaultPoint( id, crs, new double[] { x, y, z } );
    }

    @Override
    public Point createPoint( String id, double[] coordinates, double precision, CoordinateSystem crs ) {
        return new DefaultPoint( id, crs, coordinates );
    }

    @Override
    public Point createPoint( String id, double[] coordinates, CoordinateSystem crs ) {
        return new DefaultPoint( id, crs, coordinates );
    }

    @Override
    public Surface createSurface( String id, List<SurfacePatch> patches, CoordinateSystem crs ) {
        return new DefaultSurface( id, crs, patches );
    }

    @Override
    public BSpline createBSpline( List<Point> points, int degree, List<Knot> knots, boolean isPolynomial ) {
        return new DefaultBSpline( points, degree, knots, isPolynomial );
    }

    @Override
    public Arc createArc( Point p1, Point p2, Point p3 ) {
        return new DefaultArc( p1, p2, p3 );
    }

    @Override
    public ArcByBulge createArcByBulge( Point p1, Point p2, double bulge, Point normal ) {
        return new DefaultArcByBulge( p1, p2, bulge, normal );
    }

    @Override
    public Bezier createBezier( List<Point> points, int degree, Knot knot1, Knot knot2 ) {
        return new DefaultBezier( points, degree, knot1, knot2 );
    }

    @Override
    public Circle createCircle( Point p1, Point p2, Point p3 ) {
        return new DefaultCircle( p1, p2, p3 );
    }

    @Override
    public CircleByCenterPoint createCircleByCenterPoint( Point midPoint, Length radius, Angle startAngle ) {
        return new DefaultCircleByCenterPoint( midPoint, radius, startAngle );
    }

    @Override
    public Clothoid createClothoid( AffinePlacement referenceLocation, double scaleFactor, double startParameter,
                                    double endParameter ) {
        return new DefaultClothoid( referenceLocation, scaleFactor, startParameter, endParameter );
    }

    @Override
    public CubicSpline createCubicSpline( List<Point> points, Point vectorAtStart, Point vectorAtEnd ) {
        return new DefaultCubicSpline( points, vectorAtStart, vectorAtEnd );
    }

    @Override
    public Geodesic createGeodesic( Point p1, Point p2 ) {
        return new DefaultGeodesic( p1, p2 );
    }

    @Override
    public GeodesicString createGeodesicString( List<Point> points ) {
        return new DefaultGeodesicString( points );
    }

    @Override
    public OffsetCurve createOffsetCurve( Curve baseCurve, Point direction, Length distance ) {
        return new DefaultOffsetCurve( baseCurve, direction, distance );
    }

    @Override
    public Ring createRing( String id, CoordinateSystem crs, List<Curve> members ) {
        return new DefaultRing( id, crs, members );
    }

    @Override
    public LinearRing createLinearRing( String id, CoordinateSystem crs, List<Point> points ) {
        return new DefaultLinearRing( id, crs, points );
    }

    @Override
    public LineString createLineString( String id, CoordinateSystem crs, List<Point> points ) {
        return new DefaultLineString( id, crs, points );
    }

    @Override
    public OrientableCurve createOrientableCurve( String id, CoordinateSystem crs, Curve baseCurve, boolean isReversed ) {
        return new DefaultOrientableCurve( id, crs, baseCurve, isReversed );
    }

    @Override
    public Polygon createPolygon( String id, CoordinateSystem crs, Ring exteriorRing, List<Ring> interiorRings ) {
        return new DefaultPolygon( id, crs, exteriorRing, interiorRings );
    }

    @Override
    public PolygonPatch createPolygonPatch( Ring exteriorRing, List<Ring> interiorRings ) {
        return new DefaultPolygonPatch( exteriorRing, interiorRings );
    }

    @Override
    public Rectangle createRectangle( LinearRing exterior ) {
        return new DefaultRectangle( exterior );
    }

    @Override
    public Triangle createTriangle( LinearRing exterior ) {
        return new DefaultTriangle( exterior );
    }

    @Override
    public OrientableSurface createOrientableSurface( String id, CoordinateSystem crs, Surface baseSurface,
                                                      boolean isReversed ) {
        return new DefaultOrientableSurface( id, crs, baseSurface, isReversed );
    }

    @Override
    public PolyhedralSurface createPolyhedralSurface( String id, CoordinateSystem crs, List<PolygonPatch> memberPatches ) {
        return new DefaultPolyhedralSurface( id, crs, memberPatches );
    }

    @Override
    public TriangulatedSurface createTriangulatedSurface( String id, CoordinateSystem crs, List<Triangle> memberPatches ) {
        return new DefaultTriangulatedSurface( id, crs, memberPatches );
    }

    @Override
    public Tin createTin( String id, CoordinateSystem crs, List<List<LineStringSegment>> stopLines,
                          List<List<LineStringSegment>> breakLines, Length maxLength, List<Point> controlPoints ) {
        return new DefaultTin( id, crs, stopLines, breakLines, maxLength, controlPoints );
    }

    @Override
    public Solid createSolid( String id, CoordinateSystem crs, Surface exteriorSurface, List<Surface> interiorSurfaces ) {
        return new DefaultSolid( id, crs, exteriorSurface, interiorSurfaces );
    }

    @Override
    public MultiPoint createMultiPoint( String id, CoordinateSystem crs, List<Point> members ) {
        return new DefaultMultiPoint( id, crs, members );
    }

    @Override
    public MultiCurve createMultiCurve( String id, CoordinateSystem crs, List<Curve> members ) {
        return new DefaultMultiCurve( id, crs, members );
    }

    @Override
    public MultiLineString createMultiLineString( String id, CoordinateSystem crs, List<LineString> members ) {
        return new DefaultMultiLineString( id, crs, members );
    }

    @Override
    public MultiSurface createMultiSurface( String id, CoordinateSystem crs, List<Surface> members ) {
        return new DefaultMultiSurface( id, crs, members );
    }

    @Override
    public MultiPolygon createMultiPolygon( String id, CoordinateSystem crs, List<Polygon> members ) {
        return new DefaultMultiPolygon( id, crs, members );
    }

    @Override
    public MultiSolid createMultiSolid( String id, CoordinateSystem crs, List<Solid> members ) {
        return new DefaultMultiSolid( id, crs, members );
    }

    @Override
    public MultiGeometry<Geometry> createMultiGeometry( String id, CoordinateSystem crs, List<Geometry> members ) {
        return new DefaultMultiGeometry<Geometry>( id, crs, members );
    }

}
