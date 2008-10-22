//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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

package org.deegree.model.geometry.jtswrapper;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.AbstractGeometryFactory;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.composite.CompositeCurve;
import org.deegree.model.geometry.composite.CompositeSolid;
import org.deegree.model.geometry.composite.CompositeSurface;
import org.deegree.model.geometry.composite.GeometricComplex;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSolid;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.LineString;
import org.deegree.model.geometry.primitive.LinearRing;
import org.deegree.model.geometry.primitive.OrientableCurve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.SurfacePatch;
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
import org.deegree.model.geometry.primitive.curvesegments.Geodesic;
import org.deegree.model.geometry.primitive.curvesegments.GeodesicString;
import org.deegree.model.geometry.primitive.curvesegments.Knot;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.geometry.primitive.curvesegments.OffsetCurve;
import org.deegree.model.geometry.standard.curvesegments.AffinePlacement;
import org.deegree.model.gml.Angle;
import org.deegree.model.gml.Length;

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
    public CompositeCurve createCompositeCurve( String id, List<Curve> curves ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public CompositeSolid createCompositeSolid( String id, List<Solid> solids ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public CompositeSurface createCompositeSurface( String id, List<Surface> surfaces ) {
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
    public GeometricComplex createGeometricComplex( String id, List<Geometry> geometries ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public MultiCurve createMultiCurve( String id, List<Curve> curves ) {
        if ( curves == null || curves.size() == 0 ) {
            return null;
        }
        double precision = curves.get( 0 ).getPrecision();
        CoordinateSystem crs = curves.get( 0 ).getCoordinateSystem();
        int coordinateDimension = curves.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiCurve( id, curves, coordinateDimension, precision, crs );
    }

    @Override
    public MultiGeometry<Geometry> createMultiGeometry( String id, List<Geometry> geometries ) {
        if ( geometries == null || geometries.size() == 0 ) {
            return null;
        }
        double precision = geometries.get( 0 ).getPrecision();
        CoordinateSystem crs = geometries.get( 0 ).getCoordinateSystem();
        int coordinateDimension = geometries.get( 0 ).getCoordinateDimension();
        return new JTSWrapperGeometryCollection( id, precision, crs, coordinateDimension, geometries );
    }

    @Override
    public MultiPoint createMultiPoint( String id, List<Point> points ) {
        if ( points == null || points.size() == 0 ) {
            return null;
        }
        double precision = points.get( 0 ).getPrecision();
        CoordinateSystem crs = points.get( 0 ).getCoordinateSystem();
        int coordinateDimension = points.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiPoint( id, points, coordinateDimension, precision, crs );
    }

    @Override
    public MultiSolid createMultiSolid( String id, List<Solid> solids ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public MultiSurface createMultiSurface( String id, List<Surface> surfaces ) {
        if ( surfaces == null || surfaces.size() == 0 ) {
            return null;
        }
        double precision = surfaces.get( 0 ).getPrecision();
        CoordinateSystem crs = surfaces.get( 0 ).getCoordinateSystem();
        int coordinateDimension = surfaces.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiSurface( id, surfaces, coordinateDimension, precision, crs );
    }

    @Override
    public SurfacePatch createSurfacePatch( List<Curve> boundary, Class<?> type,
                                            SurfacePatch.Interpolation interpolation ) {
        if ( boundary == null || boundary.size() == 0 ) {
            return null;
        }
        // JTS just supports simple surfaces/polygons so type will be ignored
        // the same it true for interpolation; it always will be none
        return new JTSWrapperSurfacePatch( boundary );
    }

    @Override
    public SurfacePatch createSurfacePatch( List<Curve> boundary ) {
        if ( boundary == null || boundary.size() == 0 ) {
            return null;
        }
        return new JTSWrapperSurfacePatch( boundary );
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
    public Solid createSolid( String id, Surface[] outerBoundary, Surface[][] innerBoundaries, CoordinateSystem crs ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    @Override
    public Surface createSurface( String id, List<Curve> boundary, SurfacePatch.Interpolation interpolation,
                                  CoordinateSystem crs ) {
        if ( boundary == null || boundary.size() == 0 ) {
            return null;
        }
        SurfacePatch patch = createSurfacePatch( boundary, JTSWrapperSurfacePatch.class, interpolation );
        List<SurfacePatch> list = new ArrayList<SurfacePatch>( 1 );
        list.add( patch );
        return createSurface( id, list, crs );
    }

    @Override
    public Surface createSurface( String id, List<SurfacePatch> patches, CoordinateSystem crs ) {
        if ( patches == null || patches.size() == 0 ) {
            return null;
        }
        Point point = patches.get( 0 ).getBoundary().get( 0 ).getAsLineString().getPoints().get( 0 );
        // JTS does not support Surfaces (Polyons) build from different SurfacePatches, so
        // the first patch will build the complete surface
        return new JTSWrapperSurface( id, point.getPrecision(), crs, point.getCoordinateDimension(), patches.get( 0 ) );
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
    public Envelope createEnvelope( String id, SurfacePatch patch ) {
        Envelope env = patch.getBoundary().get( 0 ).getEnvelope();
        for ( int i = 1; i < patch.getBoundary().size(); i++ ) {
            env = env.merger( patch.getBoundary().get( i ).getEnvelope() );
        }
        return env;
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
}
