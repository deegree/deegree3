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
package org.deegree.geometry.io;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.linearization.SurfaceLinearizer;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.patches.GriddedSurfacePatch;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.PolygonPatch.PolygonPatchType;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.Arc;
import org.deegree.geometry.primitive.segments.ArcByBulge;
import org.deegree.geometry.primitive.segments.ArcByCenterPoint;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.ArcStringByBulge;
import org.deegree.geometry.primitive.segments.BSpline;
import org.deegree.geometry.primitive.segments.Bezier;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.CircleByCenterPoint;
import org.deegree.geometry.primitive.segments.Clothoid;
import org.deegree.geometry.primitive.segments.CubicSpline;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.Geodesic;
import org.deegree.geometry.primitive.segments.GeodesicString;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.primitive.segments.OffsetCurve;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.slf4j.Logger;

/**
 * Writes {@link Geometry} objects as Well-Known Text (WKT).
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WKTWriter {

    private static final Logger LOG = getLogger( WKTWriter.class );

    private Set<WKTFlag> flags;

    private CurveLinearizer linearizer;

    private CoordinateFormatter formatter;

    private int linearizedControlPoints = 5;

    /**
     * The flag is used to specify which geometric operations the database is capable of
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum WKTFlag {
        /** Export can use ENVELOPE */
        USE_ENVELOPE,
        /** Export can use 3D geometries */
        USE_3D,
        /** Export can use LINEARRING(...) */
        USE_LINEARRING,
        /** Export can use CIRCULARSTRING(...), COMPOUNDSTRING(...), ... **/
        USE_SQL_MM,
        /** If necessary, linearize curves / surface boundaries. */
        USE_LINEARIZATION,
        /** COMPOSITEGEOMETRY(), COMPOSITECURVE(), COMPOSITESOLID() */
        USE_COMPOSITES,
        /** Use deegree-WKT-extensions (object ids, properties, all geometry types) */
        USE_DKT
    }

    /**
     * Create a wkt writer with the given flags and formatter.
     * 
     * @param flags
     *            signaling the capabilities of this writer, if <code>null</code> only some geometries can be exported
     *            (all dialects are disabled).
     * @param formatter
     *            to use, if <code>null</code> the default decimal formatter with 5 decimals is used.
     */
    public WKTWriter( Set<WKTFlag> flags, CoordinateFormatter formatter ) {
        this.flags = flags;
        if ( this.flags == null ) {
            this.flags = new HashSet<WKTFlag>();
        }
        if ( formatter == null ) {
            this.formatter = new DecimalCoordinateFormatter();
        } else {
            this.formatter = formatter;
        }
    }

    /**
     * @param flags
     *            the flags to set
     */
    public void setFlags( Set<WKTFlag> flags ) {
        this.flags = flags;
    }

    /**
     * 
     * 
     * @param linearizer
     * 
     */
    public void setLinearizer( CurveLinearizer linearizer ) {
        this.linearizer = linearizer;
    }

    /**
     * @param formatter
     *            the formatter to set
     */
    public void setFormatter( CoordinateFormatter formatter ) {
        this.formatter = formatter;
    }

    /**
     * Controlpoints that can be set for the linearization prozess.
     * 
     * @param linearizedControlPoints
     *            the linearizedControlPoints to set
     */
    public void setLinearizedControlPoints( int linearizedControlPoints ) {
        this.linearizedControlPoints = linearizedControlPoints;
    }

    /**
     * 
     * 
     * @param geometry
     *            that has to be written
     * @param writer
     *            that is used
     * @throws IOException
     */
    public void writeGeometry( Geometry geometry, Writer writer )
                            throws IOException {

        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY:
            writeCompositeGeometry( (CompositeGeometry<GeometricPrimitive>) geometry, writer );
            break;
        case ENVELOPE:
            writeEnvelope( (Envelope) geometry, writer );
            break;
        case MULTI_GEOMETRY:
            writeMultiGeometry( (MultiGeometry<? extends Geometry>) geometry, writer );
            break;
        case PRIMITIVE_GEOMETRY:
            writeGeometricPrimitive( (GeometricPrimitive) geometry, writer );
            break;
        }

    }

    /**
     * Writes a geometric primitive
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeGeometricPrimitive( GeometricPrimitive geometry, Writer writer )
                            throws IOException {

        switch ( geometry.getPrimitiveType() ) {
        case Point:
            writePoint( (Point) geometry, writer );
            break;
        case Curve:
            writeCurve( (Curve) geometry, writer );
            break;
        case Surface:
            writeSurface( (Surface) geometry, writer );
            break;
        case Solid:
            writeSolid( (Solid) geometry, writer );
            break;

        }

    }

    /**
     * Writes the POINT
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writePoint( Point geometry, Writer writer )
                            throws IOException {

        writer.append( "POINT " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( '(' );
        writePointWithoutPrefix( geometry, writer );
        writer.append( ')' );

    }

    /**
     * Writes the POINT without the 'POINT()'-specific envelope. <br/>
     * It writes just the POINT-coordinates.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    private void writePointWithoutPrefix( Point geometry, Writer writer )
                            throws IOException {

        if ( flags.contains( WKTFlag.USE_3D ) ) {
            writer.append( formatter.format( geometry.get0() ) );
            writer.append( ' ' );
            writer.append( formatter.format( geometry.get1() ) );
            writer.append( ' ' );
            writer.append( formatter.format( geometry.get2() ) );

        } else {
            writer.append( formatter.format( geometry.get0() ) );
            writer.append( ' ' );
            writer.append( formatter.format( geometry.get1() ) );

        }

    }

    /**
     * 
     * @param geometry
     * @param writer
     */
    public void writeSolid( Solid geometry, Writer writer ) {

        switch ( geometry.getSolidType() ) {

        case Solid:
            // TODO
            throw new UnsupportedOperationException( "Handling solids is not implemented yet." );
        case CompositeSolid:
            throw new UnsupportedOperationException( "Handling compositeSolids is not implemented yet." );

        }

    }

    /**
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeSurface( Surface geometry, Writer writer )
                            throws IOException {

        switch ( geometry.getSurfaceType() ) {

        case Surface:
            writeSurfaceGeometry( geometry, writer );
            break;
        case Polygon:
            writePolygon( (Polygon) geometry, writer );
            break;
        case PolyhedralSurface:
            writeSurfaceGeometry( geometry, writer );
            break;
        case TriangulatedSurface:
            writeSurfaceGeometry( geometry, writer );
            break;
        case Tin:
            writeTin( (Tin) geometry, writer );
            break;
        case CompositeSurface:
            writeSurfaceGeometry( geometry, writer );
            break;
        case OrientableSurface:
            writeSurfaceGeometry( geometry, writer );
            break;

        }

    }

    /**
     * @param geometry
     */
    public void writeTin( Tin geometry, Writer writer ) {

        throw new UnsupportedOperationException( "Handling tins is not implemented yet." );

    }

    /**
     * 
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeSurfaceGeometry( Surface geometry, Writer writer )
                            throws IOException {

        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            writer.append( "SURFACE " );
            appendObjectProps( writer, geometry );
            writer.append( '(' );
            writeSurfacePatch( geometry, writer );
        } else if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
            throw new UnsupportedOperationException( "Handling SQL MM Part 3 is not implemented yet." );
        } else {
            writer.append( "MULTIPOLYGON (" );
            SurfaceLinearizer cl = new SurfaceLinearizer( new GeometryFactory() );
            LinearizationCriterion crit = new NumPointsCriterion( linearizedControlPoints );
            Surface surface = cl.linearize( geometry, crit );

            writeSurfacePatch( surface, writer );

        }

        writer.append( ')' );

    }

    /**
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeSurfacePatch( Surface geometry, Writer writer )
                            throws IOException {

        int counter = 0;
        List<? extends SurfacePatch> l = geometry.getPatches();
        for ( SurfacePatch p : l ) {
            switch ( p.getSurfacePatchType() ) {
            case GRIDDED_SURFACE_PATCH:
                GriddedSurfacePatch gsp = ( (GriddedSurfacePatch) p );
                switch ( gsp.getGriddedSurfaceType() ) {
                case GRIDDED_SURFACE_PATCH:
                    throw new UnsupportedOperationException( "Handling griddedSurfacePatch is not implemented yet." );
                case CONE:
                    throw new UnsupportedOperationException( "Handling cone is not implemented yet." );
                case CYLINDER:
                    throw new UnsupportedOperationException( "Handling cylinder is not implemented yet." );
                case SPHERE:
                    throw new UnsupportedOperationException( "Handling sphere is not implemented yet." );
                }
                break;
            case POLYGON_PATCH:
                counter++;
                writePolygonPatch( geometry.getId(), geometry.getCoordinateSystem(), geometry.getPrecision(),
                                   (PolygonPatch) p, writer );
                break;
            }
            if ( counter < l.size() ) {
                writer.append( ',' );
            }

        }

    }

    private void writePolygonPatch( String id, ICRS crs, PrecisionModel pm, PolygonPatch polyPatch, Writer writer )
                            throws IOException {
        PolygonPatchType type = polyPatch.getPolygonPatchType();
        Polygon poly = null;
        switch ( type ) {
        case POLYGON_PATCH:
        case RECTANGLE:
        case TRIANGLE:
            poly = new DefaultPolygon( id, crs, pm, polyPatch.getExteriorRing(), polyPatch.getInteriorRings() );
            break;
        }

        writePolygonWithoutPrefix( poly, writer );
    }

    /**
     * Writes the POLYGON
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writePolygon( Polygon geometry, Writer writer )
                            throws IOException {

        writer.append( "POLYGON " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writePolygonWithoutPrefix( geometry, writer );
    }

    /**
     * Writes the POLYGON without the 'POLYGON()'-specific envelope. <br/>
     * It writes just the POLYGON-coordinates.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    private void writePolygonWithoutPrefix( Surface geometry, Writer writer )
                            throws IOException {

        writer.append( "((" );
        Points points = geometry.getExteriorRingCoordinates();
        int counter = 0;
        for ( Point point : points ) {
            counter++;
            if ( counter < points.size() ) {
                writePointWithoutPrefix( point, writer );
                writer.append( ',' );
            } else {
                writePointWithoutPrefix( point, writer );
            }
        }
        writer.append( ')' );

        List<Points> interiorRings = geometry.getInteriorRingsCoordinates();
        if ( interiorRings != null ) {
            for ( Points r : interiorRings ) {
                writer.append( ",(" );
                counter = 0;
                for ( Point point : r ) {
                    counter++;
                    if ( counter < r.size() ) {
                        writePointWithoutPrefix( point, writer );
                        writer.append( ',' );
                    } else {
                        writePointWithoutPrefix( point, writer );
                    }
                }
                writer.append( ')' );
            }
        }
        writer.append( ')' );
    }

    /**
     * Writes a CURVE
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeCurve( Curve geometry, Writer writer )
                            throws IOException {

        switch ( geometry.getCurveType() ) {

        case Curve:
            writeCurveGeometry( geometry, writer );
            break;

        case LineString:
            writeLineString( (LineString) geometry, writer );
            break;

        case OrientableCurve:
            writeCurveGeometry( geometry, writer );
            break;

        case CompositeCurve:
            writeCompositeCurve( (CompositeCurve) geometry, writer );
            break;

        case Ring:
            writeRing( (Ring) geometry, writer );
            break;

        }

    }

    /**
     * Writes a COMPOSITE-/COMPOUND CURVE
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeCompositeCurve( CompositeCurve geometry, Writer writer )
                            throws IOException {

        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            writer.append( "COMPOSITECURVE " );
            appendObjectProps( writer, geometry );
            writer.append( '(' );

            // TODO implementation here; no values commited
        } else if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
            writer.append( "COMPOUNDCURVE " );
            writer.append( '(' );
            // TODO implementation here; no values commited

        } else {

            List<Curve> l = geometry.subList( 0, geometry.size() );
            int counter = 0;
            for ( Curve c : l ) {
                counter++;
                writeCurve( c, writer );
                if ( counter != l.size() ) {
                    writer.append( ',' );
                }
            }
        }
        writer.append( ')' );

    }

    /**
     * Writes a CURVE
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeCurveGeometry( Curve geometry, Writer writer )
                            throws IOException {

        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            writer.append( "CURVE " );
            appendObjectProps( writer, geometry );
            writer.append( '(' );
            writeCurveSegments( geometry, writer );
            writer.append( ')' );
        } else if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {

            // s.append( "COMPOUNDCURVE(" );
            throw new UnsupportedOperationException( "Handling curves within 'SQL-MM Part 3' is not implemented yet." );

        } else {
            CurveLinearizer cl = new CurveLinearizer( new GeometryFactory() );
            LinearizationCriterion crit = new NumPointsCriterion( linearizedControlPoints );
            Curve c = cl.linearize( geometry, crit );

            LineString ls = new DefaultLineString( c.getId(), c.getCoordinateSystem(), c.getPrecision(),
                                                   c.getControlPoints() );
            writeLineString( ls, writer );
        }
    }

    /**
     * Writes the CURVE without the 'CURVE()'-specific envelope. <br/>
     * It writes just the CURVE-coordinates.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    private void writeCurveGeometryWithoutPrefix( Curve geometry, Writer writer )
                            throws IOException {

        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
            throw new UnsupportedOperationException( "Handling curves within 'SQL-MM Part 3' is not implemented yet." );
        }
        CurveLinearizer cl = new CurveLinearizer( new GeometryFactory() );
        LinearizationCriterion crit = new NumPointsCriterion( linearizedControlPoints );
        Curve c = cl.linearize( geometry, crit );

        LineString ls = new DefaultLineString( c.getId(), c.getCoordinateSystem(), c.getPrecision(),
                                               c.getControlPoints() );
        writer.append( '(' );
        writeLineStringWithoutPrefix( ls, writer );
        writer.append( ')' );
    }

    /**
     * Writes the curvesegments.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    private void writeCurveSegments( Curve geometry, Writer writer )
                            throws IOException {
        List<CurveSegment> g = geometry.getCurveSegments();
        int counter = 0;
        for ( CurveSegment c : g ) {

            switch ( c.getSegmentType() ) {
            case ARC:
                counter++;
                writeArc( ( (Arc) c ), writer );
                break;
            case ARC_BY_BULGE:
                counter++;
                writeArcByBulge( ( (ArcByBulge) c ), writer );
                break;
            case ARC_BY_CENTER_POINT:
                counter++;
                writeArcByCenterPoint( ( (ArcByCenterPoint) c ), writer );
                break;
            case ARC_STRING:
                counter++;
                writeArcString( ( (ArcString) c ), writer );
                break;
            case ARC_STRING_BY_BULGE:
                counter++;
                writeArcStringByBulge( (ArcStringByBulge) c, writer );
                break;
            case BEZIER:
                counter++;
                writeBezier( (Bezier) c, writer );
                break;
            case BSPLINE:
                counter++;
                writeBSpline( (BSpline) c, writer );
                break;
            case CIRCLE:
                counter++;
                writeCircle( (Circle) c, writer );
                break;
            case CIRCLE_BY_CENTER_POINT:
                counter++;
                writeCircleByCenterPoint( (CircleByCenterPoint) c, writer );
                break;
            case CLOTHOID:
                counter++;
                writeClothoid( (Clothoid) c, writer );
                break;
            case CUBIC_SPLINE:
                counter++;
                writeCubicSpline( (CubicSpline) c, writer );
                break;
            case GEODESIC:
                counter++;
                writeGeodesic( (Geodesic) c, writer );
                break;
            case GEODESIC_STRING:
                counter++;
                writeGeodesicString( (GeodesicString) c, writer );
                break;
            case LINE_STRING_SEGMENT:
                counter++;
                writeLineStringSegment( ( (LineStringSegment) c ), writer );
                break;
            case OFFSET_CURVE:
                counter++;
                writeOffsetCurve( (OffsetCurve) c, writer );
                break;

            }
            if ( counter != g.size() ) {
                writer.append( ',' );
            }

        }

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeOffsetCurve( OffsetCurve curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling offsetCurve is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeGeodesicString( GeodesicString curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling geodesicString is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeGeodesic( Geodesic curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling geodesic is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeCubicSpline( CubicSpline curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling cubicSpline is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeClothoid( Clothoid curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling clothoid is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeCircleByCenterPoint( CircleByCenterPoint curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling circleByCenterPoint is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     * @throws IOException
     */
    private void writeCircle( Circle curve, Writer writer )
                            throws IOException {
        writer.append( "CIRCLE " );
        // if(flags.contains( WKTFlag.USE_DKT )){
        // appendObjectProps( writer, (Geometry) createArc );
        // }
        writer.append( '(' );
        writePointWithoutPrefix( curve.getPoint1(), writer );
        writer.append( ',' );
        writePointWithoutPrefix( curve.getPoint2(), writer );
        writer.append( ',' );
        writePointWithoutPrefix( curve.getPoint3(), writer );
        writer.append( ')' );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeBSpline( BSpline curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling bSpline is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeBezier( Bezier curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling bezier is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeArcStringByBulge( ArcStringByBulge curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling arcStringByBulge is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     * @throws IOException
     */
    private void writeArcString( ArcString curve, Writer writer )
                            throws IOException {
        writer.append( "ARCSTRING " );
        // if(flags.contains( WKTFlag.USE_DKT )){
        // appendObjectProps( writer, (Geometry) arcString );
        // }
        writer.append( '(' );
        int counter = 0;
        for ( Point p : curve.getControlPoints() ) {
            counter++;
            writePointWithoutPrefix( p, writer );
            if ( counter != curve.getControlPoints().size() ) {
                writer.append( ',' );
            }
        }
        writer.append( ')' );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeArcByCenterPoint( ArcByCenterPoint curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling arcByCenterPoint is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     */
    private void writeArcByBulge( ArcByBulge curve, Writer writer ) {
        throw new UnsupportedOperationException( "Handling arcByBulge is not implemented yet." );

    }

    /**
     * 
     * @param curve
     * @param writer
     * @throws IOException
     */
    private void writeLineStringSegment( LineStringSegment curve, Writer writer )
                            throws IOException {
        writer.append( "LINESTRINGSEGMENT " );
        // if(flags.contains( WKTFlag.USE_DKT )){
        // appendObjectProps( writer, (Geometry) createLineStringSegment );
        // }
        writer.append( '(' );
        int counter = 0;
        for ( Point p : curve.getControlPoints() ) {
            counter++;
            writePointWithoutPrefix( p, writer );
            if ( counter != curve.getControlPoints().size() ) {
                writer.append( ',' );
            }
        }
        writer.append( ')' );

    }

    /**
     * 
     * @param curve
     * @param writer
     * @throws IOException
     */
    private void writeArc( Arc curve, Writer writer )
                            throws IOException {
        writer.append( "ARC " );
        // if(flags.contains( WKTFlag.USE_DKT )){
        // appendObjectProps( writer, (Geometry) createArc );
        // }
        writer.append( '(' );
        writePointWithoutPrefix( curve.getPoint1(), writer );
        writer.append( ',' );
        writePointWithoutPrefix( curve.getPoint2(), writer );
        writer.append( ',' );
        writePointWithoutPrefix( curve.getPoint3(), writer );
        writer.append( ')' );

    }

    /**
     * Writes a LineString.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeLineString( LineString geometry, Writer writer )
                            throws IOException {

        writer.append( "LINESTRING " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( '(' );
        writeLineStringWithoutPrefix( geometry, writer );
        writer.append( ')' );
    }

    /**
     * Writes the LINESTRING without the 'LINESTRING()'-specific envelope. <br/>
     * It writes just the LINESTRING-coordinates.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    private void writeLineStringWithoutPrefix( LineString geometry, Writer writer )
                            throws IOException {

        Points points = geometry.getControlPoints();
        int counter = 0;
        for ( Point p : points ) {
            counter++;
            if ( counter < points.size() ) {
                writePointWithoutPrefix( p, writer );
                writer.append( ',' );
            } else {
                writePointWithoutPrefix( p, writer );
            }
        }

    }

    /**
     * Writes a ring.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeRing( Ring geometry, Writer writer )
                            throws IOException {

        switch ( geometry.getRingType() ) {
        case LinearRing:

            writeLinearRing( (LinearRing) geometry, writer );
            break;

        case Ring:

            writeCurveGeometry( geometry, writer );
            break;

        }

    }

    /**
     * Writes a linearRing.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeLinearRing( LinearRing geometry, Writer writer )
                            throws IOException {

        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            writer.append( "LINEARRING " );
            appendObjectProps( writer, geometry );
            LineString ls = new DefaultLineString( geometry.getId(), geometry.getCoordinateSystem(),
                                                   geometry.getPrecision(), geometry.getControlPoints() );
            writer.append( '(' );
            writeLineStringWithoutPrefix( ls, writer );
            writer.append( ')' );

        } else if ( flags.contains( WKTFlag.USE_LINEARRING ) ) {
            writer.append( "LINEARRING " );

            LineString ls = new DefaultLineString( geometry.getId(), geometry.getCoordinateSystem(),
                                                   geometry.getPrecision(), geometry.getControlPoints() );
            writer.append( '(' );
            writeLineStringWithoutPrefix( ls, writer );
            writer.append( ')' );
        } else {
            LineString ls = new DefaultLineString( geometry.getId(), geometry.getCoordinateSystem(),
                                                   geometry.getPrecision(), geometry.getControlPoints() );
            writeLineString( ls, writer );
        }
    }

    /**
     * Writes a multiGeometry.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeMultiGeometry( MultiGeometry<? extends Geometry> geometry, Writer writer )
                            throws IOException {

        switch ( geometry.getMultiGeometryType() ) {
        case MULTI_GEOMETRY:
            writeMultiGeometryGeometry( geometry, writer );
            break;
        case MULTI_POINT:
            writeMultiPoint( (MultiPoint) geometry, writer );
            break;
        case MULTI_CURVE:
            writeMultiCurve( (MultiCurve) geometry, writer );
            break;
        case MULTI_LINE_STRING:
            writeMultiLineString( (MultiLineString) geometry, writer );
            break;
        case MULTI_SURFACE:
            writeMultiSurface( (MultiSurface) geometry, writer );
            break;
        case MULTI_POLYGON:
            writeMultiPolygon( (MultiPolygon) geometry, writer );
            break;
        case MULTI_SOLID:
            writeMultiSolid( (MultiSolid) geometry, writer );
            break;

        }

    }

    /**
     * Writes a multiSolid.
     * 
     * @param geometry
     * @param writer
     */
    public void writeMultiSolid( MultiSolid geometry, Writer writer ) {
        throw new UnsupportedOperationException( "Handling multiSolids is not implemented yet." );

    }

    /**
     * Writes a multiSurface.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeMultiSurface( MultiSurface<Surface> geometry, Writer writer )
                            throws IOException {
        writer.append( "MULTIPOLYGON " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( '(' );

        for ( int i = 0; i < geometry.size(); i++ ) {
            writePolygonWithoutPrefix( geometry.get( i ), writer );
            if ( i < geometry.size() - 1 ) {
                writer.append( ',' );
            }
        }
        writer.append( ')' );
    }

    /**
     * Writes a multiCurve.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeMultiCurve( MultiCurve<Curve> geometry, Writer writer )
                            throws IOException {

        writer.append( "MULTILINESTRING " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( '(' );

        for ( int i = 0; i < geometry.size(); i++ ) {
            writeCurveGeometryWithoutPrefix( geometry.get( i ), writer );
            if ( i < geometry.size() - 1 ) {
                writer.append( ',' );
            }
        }

        writer.append( ')' );
    }

    /**
     * Writes the multiGeometry.
     * 
     * @param geometry
     * @param writer
     */
    public void writeMultiGeometryGeometry( MultiGeometry<? extends Geometry> geometry, Writer writer ) {
        throw new UnsupportedOperationException( "Handling multiGeometries is not implemented yet." );
    }

    /**
     * Writes the multiPolygon.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeMultiPolygon( MultiPolygon geometry, Writer writer )
                            throws IOException {

        writer.append( "MULTIPOLYGON " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( '(' );

        for ( int i = 0; i < geometry.size(); i++ ) {
            writePolygonWithoutPrefix( geometry.get( i ), writer );
            if ( i < geometry.size() - 1 ) {
                writer.append( ',' );
            }
        }

        writer.append( ')' );
    }

    /**
     * Writes the multiLineString.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeMultiLineString( MultiLineString geometry, Writer writer )
                            throws IOException {

        writer.append( "MULTILINESTRING " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( '(' );
        for ( int i = 0; i < geometry.size(); i++ ) {
            writer.append( '(' );
            writeLineStringWithoutPrefix( geometry.get( i ), writer );
            writer.append( ')' );
            if ( i < geometry.size() - 1 ) {
            	writer.append( ',' );
            }
        }

        writer.append( ')' );

    }

    /**
     * Writes the multiPoint.
     * 
     * @param geometry
     * @param writer
     * @throws IOException
     */
    public void writeMultiPoint( MultiPoint geometry, Writer writer )
                            throws IOException {
        writer.append( "MULTIPOINT " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( '(' );
        for ( int i = 0; i < geometry.size(); i++ ) {

            writePointWithoutPrefix( geometry.get( i ), writer );
            if ( i < geometry.size() - 1 ) {
                writer.append( ',' );
            }
        }

        writer.append( ')' );

    }

    /**
     * Writes the compositeGeometry.
     * 
     * @param geometry
     * @param writer
     */
    public void writeCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometry, Writer writer ) {
        throw new UnsupportedOperationException( "Handling compositeGeometries is not implemented yet." );

    }

    /**
     * 
     * @param writer
     * @throws IOException
     */
    public void writeCircularString( Writer writer )
                            throws IOException {

        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
            writer.append( "CIRCULARSTRING(" );
            // TODO there should be 3 points in it...same as arc in deegree-model??

            writer.append( ')' );
        }

    }

    /**
     * TODO also for 3D
     * 
     * @param envelope
     * @throws IOException
     */
    public void writeEnvelope( Envelope envelope, Writer writer )
                            throws IOException {

        Point pMax = envelope.getMax();
        Point pMin = envelope.getMin();

        double pMinX = pMin.get0();
        double pMinY = pMin.get1();
        double pMaxX = pMax.get0();
        double pMaxY = pMax.get1();

        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            writer.append( "ENVELOPE " );
            appendObjectProps( writer, envelope );
            writer.append( '(' );
            writer.append( formatter.format( pMinX ) + ' ' + formatter.format( pMinY ) + ',' );
            writer.append( formatter.format( pMaxX ) + ' ' + formatter.format( pMaxY ) );
            writer.append( ')' );

        } else if ( flags.contains( WKTFlag.USE_ENVELOPE ) ) {
            writer.append( "ENVELOPE " );
            writer.append( '(' );
            writer.append( formatter.format( pMinX ) + ' ' + formatter.format( pMinY ) + ',' );
            writer.append( formatter.format( pMaxX ) + ' ' + formatter.format( pMaxY ) );
            writer.append( ')' );
        } else {

            if ( pMin == pMax ) {
                writePoint( pMin, writer );
            } else {
                writer.append( "POLYGON ((" );

                writer.append( formatter.format( pMinX ) + ' ' + formatter.format( pMinY ) + ',' );
                writer.append( formatter.format( pMaxX ) + ' ' + formatter.format( pMinY ) + ',' );
                writer.append( formatter.format( pMaxX ) + ' ' + formatter.format( pMaxY ) + ',' );
                writer.append( formatter.format( pMinX ) + ' ' + formatter.format( pMaxY ) + ',' );
                writer.append( formatter.format( pMinX ) + ' ' + formatter.format( pMinY ) );

                writer.append( "))" );

            }
        }

    }

    /**
     * Export the given geometry to wkt, without using any {@link WKTFlag}s. The writing is done to a StringWriter,
     * which should be handled with care.
     * 
     * @param geom
     *            to export.
     * @return a wkt String representation of the given geometry, of the emtpy string if the geometry is
     *         <code>null</code>
     */
    public static String write( Geometry geom ) {
        if ( geom == null ) {
            return "";
        }
        if ( geom instanceof GeometryReference ) {
            geom = ( (GeometryReference<Geometry>) geom ).getReferencedObject();
        }
        StringWriter writer = new StringWriter();
        try {
            write( geom, writer );
        } catch ( IOException e ) {
            LOG.error( "Error while exporting geometry becauese: " + e.getLocalizedMessage(), e );
        }
        return writer.toString();
    }

    /**
     * Export the given geometry to wkt, without using any {@link WKTFlag}s. The writing is done using the given writer
     * instance, if no writer instance is given (<code>null</code>) a {@link NullPointerException} will be thrown.
     * 
     * @param geom
     *            to export, if <code>null</code> the method will return.
     * @param writer
     *            to write to.
     * @throws IOException
     *             if an error occurs while using the writer.
     * @throws NullPointerException
     *             if the writer is <code>null</code>
     */
    public static void write( Geometry geom, Writer writer )
                            throws IOException, NullPointerException {
        if ( geom == null ) {
            return;
        }
        if ( writer == null ) {
            throw new NullPointerException( "The writer may not be null." );
        }
        Set<WKTFlag> flags = new HashSet<WKTFlag>();
        int dim =geom.getCoordinateDimension();
        if (dim == 3){
            flags.add( WKTWriter.WKTFlag.USE_3D );
        }
        else{
            flags = null;
        }
        WKTWriter wktW = new WKTWriter( flags, null );
        wktW.writeGeometry( geom, writer );
    }

    /**
     * Does the work to write the standardproperties for the geometryobjects
     * <p>
     * This specification comes from the GML and is not necessary for databases but maybe necessary for an export within
     * deegree
     * 
     * @param sb
     * @param geom
     * @throws IOException
     */
    private void appendObjectProps( Writer writer, Geometry geom )
                            throws IOException {

        writer.append( '[' );
        //
        writer.append( "id='" );
        if ( geom.getId() != null ) {
            writer.append( geom.getId() );
        } else
            writer.append( "" );
        writer.append( '\'' );
        writer.append( ']' );
    }
}
