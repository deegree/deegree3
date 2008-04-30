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
import java.util.Arrays;
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
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.SurfacePatch;
import org.deegree.model.geometry.primitive.Curve.ORIENTATION;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperGeometryFactory extends AbstractGeometryFactory {

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createCompositeCurve(org.deegree.model.geometry.primitive.Curve[])
     */
    public CompositeCurve createCompositeCurve( @SuppressWarnings("unused")
    List<Curve> curves ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createCompositeSolid(org.deegree.model.geometry.primitive.Solid[])
     */
    public CompositeSolid createCompositeSolid( @SuppressWarnings("unused")
    List<Solid> solids ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createCompositeSurface(org.deegree.model.geometry.primitive.Surface[])
     */
    public CompositeSurface createCompositeSurface( @SuppressWarnings("unused")
    List<Surface> surfaces ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createCurve(org.deegree.model.geometry.primitive.Point[][],
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Curve createCurve( Point[][] coordinates, ORIENTATION orientation, CoordinateSystem crs ) {
        CurveSegment[] segments = new CurveSegment[coordinates.length];
        for ( int i = 0; i < segments.length; i++ ) {
            segments[i] = createCurveSegment( Arrays.asList( coordinates[i] ), JTSWrapperCurveSegment.class,
                                              CurveSegment.INTERPOLATION.linear );
        }
        return createCurve( segments, orientation, crs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createCurve(org.deegree.model.geometry.primitive.CurveSegment[],
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Curve createCurve( CurveSegment[] segments, ORIENTATION orientation, CoordinateSystem crs ) {
        if ( segments == null || segments.length == 0 ) {
            return null;
        }
        Point point = segments[0].getPoints().get( 0 );
        // JTS does not support Curves (LineStrings) build from different CurveSegments, so
        // the first segment will build the complete curve
        return new JTSWrapperCurve( point.getPrecision(), crs, point.getCoordinateDimension(), segments[0], orientation );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createCurve(java.util.List, java.lang.Class,
     *      org.deegree.model.geometry.primitive.CurveSegment.INTERPOLATION)
     */
    public CurveSegment createCurveSegment( List<Point> points, @SuppressWarnings("unused")
    Class type, @SuppressWarnings("unused")
    CurveSegment.INTERPOLATION interpolation ) {
        if ( points == null || points.size() == 0 ) {
            return null;
        }
        // JTS just supports simple curves so type will be ignored
        // the same it true for interpolation; it always will be linear
        return new JTSWrapperCurveSegment( points );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createCurveSegment(java.util.List)
     */
    public CurveSegment createCurveSegment( List<Point> points ) {
        if ( points == null || points.size() == 0 ) {
            return null;
        }
        return new JTSWrapperCurveSegment( points );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createGeometricComplex(org.deegree.model.geometry.Geometry[])
     */
    public GeometricComplex createGeometricComplex( @SuppressWarnings("unused")
    List<Geometry> geometries ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createMultiCurve(org.deegree.model.geometry.primitive.Curve[])
     */
    public MultiCurve<Curve> createMultiCurve( List<Curve> curves ) {
        if ( curves == null || curves.size() == 0 ) {
            return null;
        }
        double precision = curves.get( 0 ).getPrecision();
        CoordinateSystem crs = curves.get( 0 ).getCoordinateSystem();
        int coordinateDimension = curves.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiCurve( curves, coordinateDimension, precision, crs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createMultiGeometry(org.deegree.model.geometry.Geometry[])
     */
    public MultiGeometry<Geometry> createMultiGeometry( List<Geometry> geometries ) {
        if ( geometries == null || geometries.size() == 0 ) {
            return null;
        }
        double precision = geometries.get( 0 ).getPrecision();
        CoordinateSystem crs = geometries.get( 0 ).getCoordinateSystem();
        int coordinateDimension = geometries.get( 0 ).getCoordinateDimension();
        return new JTSWrapperGeometryCollection( precision, crs, coordinateDimension, geometries );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createMultiPoint(org.deegree.model.geometry.primitive.Point[])
     */
    public MultiPoint<Point> createMultiPoint( List<Point> points ) {
        if ( points == null || points.size() == 0 ) {
            return null;
        }
        double precision = points.get( 0 ).getPrecision();
        CoordinateSystem crs = points.get( 0 ).getCoordinateSystem();
        int coordinateDimension = points.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiPoint( points, coordinateDimension, precision, crs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createMultiSolid(org.deegree.model.geometry.primitive.Solid[])
     */
    public MultiSolid<Solid> createMultiSolid( @SuppressWarnings("unused")
    List<Solid> solids ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createMultiSurface(org.deegree.model.geometry.primitive.Surface[])
     */
    public MultiSurface<Surface> createMultiSurface( List<Surface> surfaces ) {
        if ( surfaces == null || surfaces.size() == 0 ) {
            return null;
        }
        double precision = surfaces.get( 0 ).getPrecision();
        CoordinateSystem crs = surfaces.get( 0 ).getCoordinateSystem();
        int coordinateDimension = surfaces.get( 0 ).getCoordinateDimension();
        return new JTSWrapperMultiSurface( surfaces, coordinateDimension, precision, crs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createPatch(org.deegree.model.geometry.primitive.Curve[],
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem,
     *      org.deegree.model.geometry.primitive.Patch.INTERPOLATION)
     */
    public SurfacePatch createSurfacePatch( List<Curve> boundary, @SuppressWarnings("unused")
    Class type, @SuppressWarnings("unused")
    SurfacePatch.INTERPOLATION interpolation ) {
        if ( boundary == null || boundary.size() == 0 ) {
            return null;
        }
        // JTS just supports simple surfaces/polygons so type will be ignored
        // the same it true for interpolation; it always will be none
        return new JTSWrapperSurfacePatch( boundary );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createSurfacePatch(java.util.List)
     */
    public SurfacePatch createSurfacePatch( List<Curve> boundary ) {
        if ( boundary == null || boundary.size() == 0 ) {
            return null;
        }
        return new JTSWrapperSurfacePatch( boundary );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createPoint(double[],
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Point createPoint( double[] coordinates, double precision, CoordinateSystem crs ) {
        return new JTSWrapperPoint( precision, crs, coordinates );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createPoint(double[],
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Point createPoint( double[] coordinates, CoordinateSystem crs ) {
        // TODO
        // set useful precision value
        return new JTSWrapperPoint( 0.00001, crs, coordinates );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createSolid(org.deegree.model.geometry.primitive.Surface[],
     *      org.deegree.model.geometry.primitive.Surface[][], org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Solid createSolid( @SuppressWarnings("unused")
    Surface[] outerboundary, @SuppressWarnings("unused")
    Surface[][] innerboundarie, @SuppressWarnings("unused")
    CoordinateSystem crs ) {
        throw new UnsupportedOperationException( "not supported by JTS(Wrapper)" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createSurface(java.util.List,
     *      org.deegree.model.geometry.primitive.SurfacePatch.INTERPOLATION,
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Surface createSurface( List<Curve> boundary, SurfacePatch.INTERPOLATION interpolation, CoordinateSystem crs ) {
        if ( boundary == null || boundary.size() == 0 ) {
            return null;
        }
        SurfacePatch patch = createSurfacePatch( boundary, JTSWrapperSurfacePatch.class, interpolation );
        List<SurfacePatch> list = new ArrayList<SurfacePatch>( 1 );
        list.add( patch );
        return createSurface( list, crs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createSurface(java.util.List,
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Surface createSurface( List<SurfacePatch> patches, CoordinateSystem crs ) {
        if ( patches == null || patches.size() == 0 ) {
            return null;
        }
        Point point = patches.get( 0 ).getBoundary().get( 0 ).getPoints().get( 0 );
        // JTS does not support Surfaces (Polyons) build from different SurfacePatches, so
        // the first patch will build the complete surface
        return new JTSWrapperSurface( point.getPrecision(), crs, point.getCoordinateDimension(), patches.get( 0 ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createEnvelope(double[], double[], double,
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Envelope createEnvelope( double[] min, double[] max, double precision, CoordinateSystem crs ) {
        Point p1 = new JTSWrapperPoint( precision, crs, min );
        Point p2 = new JTSWrapperPoint( precision, crs, max );
        // JTS envelopes just stores 2-dimensional coordinates
        return new JTSWrapperEnvelope( precision, crs, 2, p1, p2 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createEnvelope(double[], double[], double,
     *      org.deegree.model.crs.coordinatesystems.CoordinateSystem)
     */
    public Envelope createEnvelope( double[] min, double[] max, CoordinateSystem crs ) {
        // TODO
        // useful value for precision
        return createEnvelope( min, max, 0.00001, crs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.GeometryFactory#createEnvelope(org.deegree.model.geometry.primitive.SurfacePatch)
     */
    public Envelope createEnvelope( SurfacePatch patch ) {
        Envelope env = patch.getBoundary().get( 0 ).getEnvelope();
        for ( int i = 1; i < patch.getBoundary().size(); i++ ) {
            env = env.merger( patch.getBoundary().get( i ).getEnvelope() );
        }
        return env;
    }

}
