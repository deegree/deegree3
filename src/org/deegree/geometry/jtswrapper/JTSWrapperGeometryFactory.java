//$HeadURL$
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

package org.deegree.geometry.jtswrapper;

import java.util.List;

import org.deegree.commons.types.Length;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.feature.gml.Angle;
import org.deegree.geometry.AbstractGeometryFactory;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
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
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.geometry.primitive.surfacepatches.Rectangle;
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
public class JTSWrapperGeometryFactory extends AbstractGeometryFactory {

    @Override
    public CompositeCurve createCompositeCurve( String id, CoordinateSystem crs, List<Curve> curves ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public CompositeSolid createCompositeSolid( String id, CoordinateSystem crs, List<Solid> solids ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public CompositeSurface createCompositeSurface( String id, CoordinateSystem crs, List<Surface> surfaces ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Curve createCurve( String id, Point[][] coordinates, CoordinateSystem crs ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Curve createCurve( String id, CurveSegment[] segments, CoordinateSystem crs ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public LineStringSegment createLineStringSegment( List<Point> points ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public CompositeGeometry<GeometricPrimitive> createCompositeGeometry( String id, CoordinateSystem crs,
                                                                          List<GeometricPrimitive> memberPrimitives ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public MultiCurve createMultiCurve( String id, CoordinateSystem crs, List<Curve> curves ) {
        if ( curves == null || curves.size() == 0 ) {
            return null;
        }
        double precision = curves.get( 0 ).getPrecision();
        int coordinateDimension = curves.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiCurve( id, curves, coordinateDimension, precision, crs );
    }

    @Override
    public MultiGeometry<Geometry> createMultiGeometry( String id, CoordinateSystem crs, List<Geometry> geometries ) {
        if ( geometries == null || geometries.size() == 0 ) {
            return null;
        }
        double precision = geometries.get( 0 ).getPrecision();
        int coordinateDimension = geometries.get( 0 ).getCoordinateDimension();
        return new JTSWrapperGeometryCollection( id, precision, crs, coordinateDimension, geometries );
    }

    @Override
    public MultiPoint createMultiPoint( String id, CoordinateSystem crs, List<Point> points ) {
        if ( points == null || points.size() == 0 ) {
            return null;
        }
        double precision = points.get( 0 ).getPrecision();
        int coordinateDimension = points.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiPoint( id, points, coordinateDimension, precision, crs );
    }

    @Override
    public MultiSolid createMultiSolid( String id, CoordinateSystem crs, List<Solid> solids ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public MultiSurface createMultiSurface( String id, CoordinateSystem crs, List<Surface> surfaces ) {
        if ( surfaces == null || surfaces.size() == 0 ) {
            return null;
        }
        double precision = surfaces.get( 0 ).getPrecision();
        int coordinateDimension = surfaces.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiSurface( id, surfaces, coordinateDimension, precision, crs );
    }

    @Override
    public Point createPoint( String id, double x, double y, CoordinateSystem crs ) {
        return new JTSWrapperPoint( id, 0.00001, crs, new double[] { x, y } );
    }

    @Override
    public Point createPoint( String id, double x, double y, double z, CoordinateSystem crs ) {
        return new JTSWrapperPoint( id, 0.00001, crs, new double[] { x, y, z } );
    }

    @Override
    public Point createPoint( String id, double[] coordinates, double precision, CoordinateSystem crs ) {
        return new JTSWrapperPoint( id, precision, crs, coordinates );
    }

    @Override
    public Point createPoint( String id, double[] coordinates, CoordinateSystem crs ) {
        // TODO
        // set useful precision value
        return new JTSWrapperPoint( id, 0.00001, crs, coordinates );
    }

    @Override
    public Surface createSurface( String id, List<SurfacePatch> patches, CoordinateSystem crs ) {
        if ( patches == null || patches.size() == 0 ) {
            return null;
        }
        // Point point = patches.get( 0 ).getBoundaries().get( 0
        // ).getAsLineString().getControlPoints().get( 0 );
        // // JTS does not support Surfaces (Polyons) build from different SurfacePatches, so
        // // the first patch will build the complete surface
        // return new JTSWrapperSurface( id, point.getPrecision(), crs,
        // point.getCoordinateDimension(), patches.get( 0 )
        // );
        throw new UnsupportedOperationException();
    }

    @Override
    public Envelope createEnvelope( double[] min, double[] max, double precision, CoordinateSystem crs ) {
        Point p1 = new JTSWrapperPoint( null, precision, crs, min );
        Point p2 = new JTSWrapperPoint( null, precision, crs, max );
        // JTS envelopes just stores 2-dimensional coordinates
        return new JTSWrapperEnvelope( precision, crs, 2, p1, p2 );
    }

    @Override
    public Envelope createEnvelope( double[] min, double[] max, CoordinateSystem crs ) {
        // TODO
        // useful value for precision
        return createEnvelope( min, max, 0.00001, crs );
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
    public Envelope createEnvelope( double minx, double miny, double maxx, double maxy, CoordinateSystem crs ) {
        return createEnvelope( new double[] { minx, miny }, new double[] { maxx, maxy }, crs );
    }

    @Override
    public Envelope createEnvelope( double minx, double miny, double maxx, double maxy, double precision,
                                    CoordinateSystem crs ) {

        return createEnvelope( new double[] { minx, miny }, new double[] { maxx, maxy }, precision, crs );
    }

    @Override
    public Envelope createEnvelope( String id, SurfacePatch patch ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
        // Envelope env = patch.getBoundaries().get( 0 ).getEnvelope();
        // for ( int i = 1; i < patch.getBoundaries().size(); i++ ) {
        // env = env.merger( patch.getBoundaries().get( i ).getEnvelope() );
        // }
        // return env;
    }

    @Override
    public ArcString createArcString( List<Point> points ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public ArcStringByBulge createArcStringByBulge( List<Point> points, double[] bulges, List<Point> normals ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public ArcByCenterPoint createArcByCenterPoint( Point midPoint, Length radius, Angle startAngle, Angle endAngle ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public BSpline createBSpline( List<Point> points, int degree, List<Knot> knots, boolean isPolynomial ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Arc createArc( Point p1, Point p2, Point p3 ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public ArcByBulge createArcByBulge( Point p1, Point p2, double bulge, Point normal ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Bezier createBezier( List<Point> points, int degree, Knot knot1, Knot knot2 ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Circle createCircle( Point p1, Point p2, Point p3 ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public CircleByCenterPoint createCircleByCenterPoint( Point midPoint, Length radius, Angle startAngle ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Clothoid createClothoid( AffinePlacement referenceLocation, double scaleFactor, double startParameter,
                                    double endParameter ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public CubicSpline createCubicSpline( List<Point> points, Point vectorAtStart, Point vectorAtEnd ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Geodesic createGeodesic( Point p1, Point p2 ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public GeodesicString createGeodesicString( List<Point> points ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public OffsetCurve createOffsetCurve( Curve baseCurve, Point direction, Length distance ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Ring createRing( String id, CoordinateSystem crs, List<Curve> members ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public LineString createLineString( String id, CoordinateSystem crs, List<Point> points ) {
        return new JTSWrapperLineString( id, points.get( 0 ).getPrecision(), crs,
                                         points.get( 0 ).getCoordinateDimension(), points );
    }

    @Override
    public LinearRing createLinearRing( String id, CoordinateSystem crs, List<Point> points ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public OrientableCurve createOrientableCurve( String id, CoordinateSystem crs, Curve baseCurve, boolean isReversed ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Polygon createPolygon( String id, CoordinateSystem crs, Ring exteriorRing, List<Ring> interiorRings ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public PolygonPatch createPolygonPatch( Ring exteriorRing, List<Ring> interiorRings ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Rectangle createRectangle( LinearRing exterior ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Triangle createTriangle( LinearRing exterior ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public OrientableSurface createOrientableSurface( String id, CoordinateSystem crs, Surface baseSurface,
                                                      boolean isReversed ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public PolyhedralSurface createPolyhedralSurface( String id, CoordinateSystem crs, List<PolygonPatch> memberPatches ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public TriangulatedSurface createTriangulatedSurface( String id, CoordinateSystem crs, List<Triangle> memberPatches ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Solid createSolid( String id, CoordinateSystem crs, Surface exteriorSurface, List<Surface> interiorSurfaces ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public MultiLineString createMultiLineString( String id, CoordinateSystem crs, List<LineString> members ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public MultiPolygon createMultiPolygon( String id, CoordinateSystem crs, List<Polygon> members ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Tin createTin( String id, CoordinateSystem crs, List<List<LineStringSegment>> stopLines,
                          List<List<LineStringSegment>> breakLines, Length maxLength, List<Point> controlPoints ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }
}
