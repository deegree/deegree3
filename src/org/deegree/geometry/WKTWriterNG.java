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
package org.deegree.geometry;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.gml.props.StandardGMLObjectProps;
import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.uom.Length;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.linearization.SurfaceLinearizer;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.points.Points;
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
import org.deegree.geometry.primitive.patches.GriddedSurfacePatch;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.Rectangle;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.patches.Triangle;
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
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultPolygon;

/**
 * Writes {@link Geometry} objects as Well-Known Text (WKT).
 * 
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WKTWriterNG {

    private static final com.vividsolutions.jts.io.WKTWriter jtsWriter = new com.vividsolutions.jts.io.WKTWriter();

    private Set<WKTFlag> flags;
    
    private Writer writer;

    /**
     * 
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
     * @param flags
     *            the flags to set
     */
    public void setFlags( Set<WKTFlag> flags ) {
        this.flags = flags;
    }

    /*
     * public void createSFS11Writer() {
     * 
     * new WKTWriter(); }
     * 
     * public void createSFS12Writer( Set<WKTFlag> falgs ) { this.flags = falgs;
     * 
     * new WKTWriter(); }
     */
    public WKTWriterNG( Set<WKTFlag> flags, Writer writer ) {
        this.flags = flags;
        this.writer = writer;
    }

    public void writeGeometry( Geometry geometry, Writer writer ) throws IOException {
        
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
     * @param geometry
     * @throws IOException 
     */
    private void writeGeometricPrimitive( GeometricPrimitive geometry, Writer writer ) throws IOException {
        

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
            throw new UnsupportedOperationException( "Handling solids is not implemented yet." );
            // writeSolid( (Solid) geometry );

        }
        

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    public void writePoint( Point geometry, Writer writer ) throws IOException {
        
        writer.append( "POINT " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writer.append( "(" );
        writePointWithoutPrefix( geometry, writer );
        writer.append( ")" );

        
    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writePointWithoutPrefix( Point geometry, Writer writer ) throws IOException {

         
        if ( flags.contains( WKTFlag.USE_3D ) ) {
            writer.append( Double.toString( geometry.get0()) );
            writer.append( ' ' );
            writer.append( Double.toString( geometry.get1() ));
            writer.append( ' ' );
            writer.append( Double.toString( geometry.get2() ));

        } else {

            writer.append( Double.toString( geometry.get0() ));
            writer.append( ' ' );
            writer.append( Double.toString( geometry.get1() ));

        }

        
    }

    /**
     * @param geometry
     */
    // TODO
    private void writeSolid( Solid geometry, Writer writer ) {
        
        switch ( geometry.getSolidType() ) {

        case Solid:
            break;
        case CompositeSolid:
            break;

        }
        

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeSurface( Surface geometry, Writer writer ) throws IOException {
        
        switch ( geometry.getSurfaceType() ) {

        case Surface:
            writeSurfaceGeometry( geometry, writer );
            break;
        case Polygon:
            writePolygon( (Polygon) geometry, writer );
            break;
        case PolyhedralSurface:
            writeSurfaceGeometry( (PolyhedralSurface) geometry, writer );
            break;
        case TriangulatedSurface:
            writeSurfaceGeometry( (TriangulatedSurface) geometry, writer );
            break;
        case Tin:
            writeTin( (Tin) geometry, writer );
            break;
        case CompositeSurface:
            writeSurfaceGeometry( (CompositeSurface) geometry, writer );
            break;
        case OrientableSurface:
            writeSurfaceGeometry( (OrientableSurface) geometry, writer );
            break;

        }

        

    }

    /**
     * @param geometry
     */
    private void writeTin( Tin geometry, Writer writer ) {
        
        // TODO an implementation
        

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeSurfaceGeometry( Surface geometry, Writer writer ) throws IOException {
        
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            writer.append( "" );
            appendObjectProps( writer, geometry );
        }
        
        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {

        } else {
            writer.append( "MULTIPOLYGON(" );
            SurfaceLinearizer cl = new SurfaceLinearizer( new GeometryFactory() );
            LinearizationCriterion crit = new NumPointsCriterion( 10 );
            Surface surface = cl.linearize( geometry, crit );

            int counter = 0;
            List<? extends SurfacePatch> l = surface.getPatches();
            for ( SurfacePatch p : l ) {
                Polygon poly;
                switch ( p.getSurfacePatchType() ) {
                case GRIDDED_SURFACE_PATCH:
                    GriddedSurfacePatch gsp = ( (GriddedSurfacePatch) p );
                    switch ( gsp.getGriddedSurfaceType() ) {
                    case GRIDDED_SURFACE_PATCH:
                        // TODO
                        break;
                    case CONE:
                        // TODO
                        break;
                    case CYLINDER:
                        // TODO
                        break;
                    case SPHERE:
                        // TODO
                        break;
                    }
                    break;
                case POLYGON_PATCH:
                    counter++;
                    PolygonPatch polyPatch = (PolygonPatch) p;

                    poly = new DefaultPolygon( surface.getId(), surface.getCoordinateSystem(), surface.getPrecision(),
                                               polyPatch.getExteriorRing(), polyPatch.getInteriorRings() );
                    writePolygonWithoutPrefix( poly, writer ) ;

                    break;
                case RECTANGLE:
                    counter++;
                    Rectangle rectangle = ( (Rectangle) p );
                    poly = new DefaultPolygon( surface.getId(), surface.getCoordinateSystem(), surface.getPrecision(),
                                               rectangle.getExteriorRing(), rectangle.getInteriorRings() );
                    writePolygonWithoutPrefix( poly, writer );
                    break;
                case TRIANGLE:
                    counter++;
                    Triangle triangle = ( (Triangle) p );
                    poly = new DefaultPolygon( surface.getId(), surface.getCoordinateSystem(), surface.getPrecision(),
                                               triangle.getExteriorRing(), triangle.getInteriorRings() );
                    writePolygonWithoutPrefix( poly, writer ) ;
                    break;
                }
                if ( counter < l.size() ) {
                    writer.append( ',' );
                }

            }
        }

        writer.append( ')' );

        

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    public void writePolygon( Polygon geometry, Writer writer ) throws IOException {
        
        writer.append( "POLYGON " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            appendObjectProps( writer, geometry );
        }
        writePolygonWithoutPrefix( geometry, writer );
        
    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writePolygonWithoutPrefix( Polygon geometry, Writer writer ) throws IOException {
        
        Ring exteriorRing = geometry.getExteriorRing();
        writer.append( '(' );
        Points points = exteriorRing.getControlPoints();
        int counter = 0;
        for ( Point point : points ) {

            counter++;
            if ( counter < points.size() ) {
                //writePointWithoutPrefix( point, s );
                writer.append( ',' );
            } else {
                //writePointWithoutPrefix( point, s ) ;
            }

        }

        writer.append( ')' );
        List<Ring> interiorRings = geometry.getInteriorRings();
        for ( Ring r : interiorRings ) {
            writer.append( ",(" );
            counter = 0;
            for ( Point point : points ) {

                counter++;
                if ( counter < points.size() ) {
                    //writePointWithoutPrefix( point, s );
                    writer.append( ',' );
                } else {
                    //writePointWithoutPrefix( point, s ) ;
                }
            }
            writer.append( ')' );
        }
        writer.append( ')' );
        
    }

    /**
     * @param geometry
     * @throws IOException 
     */
    public void writeCurve( Curve geometry, Writer writer ) throws IOException {
        
        switch ( geometry.getCurveType() ) {

        case Curve:
            writeCurveGeometry( geometry, writer  );
            break;

        case LineString:
            writeLineString( (LineString) geometry, writer );
            break;

        case OrientableCurve:
            writeCurveGeometry( (OrientableCurve) geometry, writer );
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
     * @param geometry
     * @throws IOException 
     */
    private void writeCompositeCurve( CompositeCurve geometry, Writer writer ) throws IOException {
        
        writer.append( "COMPOSITECURVE " );
        //TODO or COMPOUNDCURVE??
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            //appendObjectProps( s, geometry );
        }
        writer.append( '(' );
        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
            // TODO difference between GML vs. PostGIS vs. ...

        } else {
            
            List<Curve> l = geometry.subList( 0, geometry.size() );
            int counter = 0;
            for(Curve c : l){
                counter++;
                writeCurve( c, writer );
                if(counter != l.size()){
                    writer.append( ',' );
                }
            }
        }
        writer.append( ')' );
        

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeCurveGeometry( Curve geometry, Writer writer ) throws IOException {

        

        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            writer.append( "CURVE " );
            //appendObjectProps( s, geometry );
            writer.append( '(' );
            writeCurveSegments( geometry, writer );
            writer.append( ')' );
        } else {

            if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
                
                //s.append( "COMPOUNDCURVE(" );
                throw new UnsupportedOperationException( "Handling curves within 'SQL-MM Part 3' is not implemented yet." );
                
                
                

            } else {
                CurveLinearizer cl = new CurveLinearizer( new GeometryFactory() );
                LinearizationCriterion crit = new NumPointsCriterion( 10 );
                Curve c = cl.linearize( geometry, crit );

                LineString ls = new DefaultLineString( c.getId(), c.getCoordinateSystem(), c.getPrecision(),
                                                       c.getControlPoints() );
                writeLineString( ls, writer );
            }
        }

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeCurveGeometryWithoutPrefix( Curve geometry, Writer writer ) throws IOException {
    
        
        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
    
        } else {
            CurveLinearizer cl = new CurveLinearizer( new GeometryFactory() );
            LinearizationCriterion crit = new NumPointsCriterion( 1 );
            Curve c = cl.linearize( geometry, crit );
    
            LineString ls = new DefaultLineString( c.getId(), c.getCoordinateSystem(), c.getPrecision(),
                                                   c.getControlPoints() );
            writeLineStringWithoutPrefix( ls, writer ) ;
        }
    
        
    }

    /**
     * @param geometry
     * @param writer
     * @throws IOException 
     */
    private void writeCurveSegments( Curve geometry, Writer writer ) throws IOException {
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
                writeArcByBulge(( (ArcByBulge) c ), writer);
                break;
            case ARC_BY_CENTER_POINT:
                counter++;
                writeArcByCenterPoint(( (ArcByCenterPoint) c ), writer);
                break;
            case ARC_STRING:
                counter++;
                writeArcString(( (ArcString) c ), writer);
                break;
            case ARC_STRING_BY_BULGE:
                counter++;
                writeArcStringByBulge((ArcStringByBulge)c, writer);
                break;
            case BEZIER:
                counter++;
                writeBezier((Bezier)c, writer);
                break;
            case BSPLINE:
                counter++;
                writeBSpline((BSpline)c, writer);
                break;
            case CIRCLE:
                counter++;
                writeCircle((Circle)c, writer);
                break;
            case CIRCLE_BY_CENTER_POINT:
                counter++;
                writeCircleByCenterPoint((CircleByCenterPoint)c, writer);
                break;
            case CLOTHOID:
                counter++;
                writeClothoid((Clothoid)c, writer);
                break;
            case CUBIC_SPLINE:
                counter++;
                writeCubicSpline((CubicSpline)c, writer);
                break;
            case GEODESIC:
                counter++;
                writeGeodesic((Geodesic)c, writer);
                break;
            case GEODESIC_STRING:
                counter++;
                writeGeodesicString((GeodesicString)c, writer);
                break;
            case LINE_STRING_SEGMENT:
                counter++;
                writeLineStringSegment( ( (LineStringSegment) c ), writer );
                break;
            case OFFSET_CURVE:
                counter++;
                writeOffsetCurve((OffsetCurve)c, writer);
                break;

            }
            if ( counter != g.size() ) {
                writer.append( ',' );
            }
        }

    }

    /**
     * @param c
     * @param s
     */
    private void writeOffsetCurve( OffsetCurve c, Writer writer) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeGeodesicString( GeodesicString c, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeGeodesic( Geodesic c, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeCubicSpline( CubicSpline c, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeClothoid( Clothoid c, Writer writer) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeCircleByCenterPoint( CircleByCenterPoint c,Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeCircle( Circle c,Writer writer) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeBSpline( BSpline c, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeBezier( Bezier c,Writer writer) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param c
     * @param s
     */
    private void writeArcStringByBulge( ArcStringByBulge c, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param arcString
     * @param s
     */
    private void writeArcString( ArcString arcString, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param arcByCenterPoint
     * @param s
     */
    private void writeArcByCenterPoint( ArcByCenterPoint arcByCenterPoint, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param arcByBulge
     * @param s
     */
    private void writeArcByBulge( ArcByBulge arcByBulge, Writer writer ) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param createLineStringSegment
     * @param s
     */
    private void writeLineStringSegment( LineStringSegment createLineStringSegment, Writer writer ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param createArc
     * @param s
     */
    private void writeArc( Arc createArc, Writer writer ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    public void writeLineString( LineString geometry, Writer writer ) throws IOException {
        
        writer.append( "LINESTRING " );
        if ( flags.contains( WKTFlag.USE_DKT ) ) {
            //appendObjectProps( s, geometry );
        }
        writeLineStringWithoutPrefix( geometry, writer );
       

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeLineStringWithoutPrefix( LineString geometry, Writer writer ) throws IOException {
        writer.append( '(' );
        Points points = geometry.getControlPoints();
        int counter = 0;
        for ( Point p : points ) {
            counter++;
            if ( counter < points.size() ) {
               // writePointWithoutPrefix( p, s );
                writer.append( ',' );
            } else {
                //writePointWithoutPrefix( p, s ) ;
            }
        }
        writer.append( ')' );
        

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    public void  writeRing( Ring geometry, Writer writer ) throws IOException {
        
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
     * @param geometry
     * @throws IOException 
     */
    public void writeLinearRing( LinearRing geometry, Writer writer ) throws IOException {
        
        if ( flags.contains( WKTFlag.USE_LINEARRING ) ) {

            writer.append( "LINEARRING " );
            if ( flags.contains( WKTFlag.USE_DKT ) ) {
                //appendObjectProps( s, geometry );
            }

            LineString ls = new DefaultLineString( geometry.getId(), geometry.getCoordinateSystem(),
                                                   geometry.getPrecision(), geometry.getControlPoints() );
            writeLineStringWithoutPrefix( ls, writer ) ;

        } else {
            LineString ls = new DefaultLineString( geometry.getId(), geometry.getCoordinateSystem(),
                                                   geometry.getPrecision(), geometry.getControlPoints() );
            writeLineString( ls, writer );
        }
    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeMultiGeometry( MultiGeometry<? extends Geometry> geometry, Writer writer ) throws IOException {
        StringBuilder s = null;
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
            break;
        case MULTI_POLYGON:
            writeMultiPolygon( (MultiPolygon) geometry, writer );
            break;
        case MULTI_SOLID:
            break;

        }

        

    }

    /**
     * @param geometry
     * @throws IOException 
     */
    public void writeMultiCurve( MultiCurve geometry, Writer writer ) throws IOException {
        
        writer.append( "MULTICURVE " );

        for ( int i = 0; i < geometry.size(); i++ ) {

            writeCurveGeometryWithoutPrefix( geometry.get( i ), writer ) ;
            if ( i < geometry.size() - 1 ) {
                writer.append( ',' );

            }

        }

        writer.append( ')' );

    }

    /**
     * @param geometry
     */
    private void writeMultiGeometryGeometry( MultiGeometry<? extends Geometry> geometry, Writer writer ) {
        //TODO
    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeMultiPolygon( MultiPolygon geometry, Writer writer ) throws IOException {
        
        writer.append( "MULTIPOLYGON(" );

        for ( int i = 0; i < geometry.size(); i++ ) {

            writePolygonWithoutPrefix( geometry.get( i ), writer  );
            if ( i < geometry.size() - 1 ) {
                writer.append( ',' );
            }
        }

        writer.append( ')' );
        
    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeMultiLineString( MultiLineString geometry, Writer writer ) throws IOException {
        
        writer.append( "MULTILINESTRING(" );
        for ( int i = 0; i < geometry.size(); i++ ) {

            writeLineStringWithoutPrefix( geometry.get( i ), writer ) ;
            if ( i < geometry.size() - 1 ) {
                writer.append( ',' );

            }
        }

        writer.append( ')' );
        
    }

    /**
     * @param geometry
     * @throws IOException 
     */
    private void writeMultiPoint( MultiPoint geometry, Writer writer ) throws IOException {
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
     * @param geometry
     */
    private void writeCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometry, Writer writer ) {
        //TODO

    }

    /**
     * 
     * @param writer
     * @throws IOException
     */
    private void writeCircularString(Writer writer) throws IOException {
        
        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {
            writer.append( "CIRCULARSTRING(" );
            // TODO there should be 3 points in it

            writer.append( ')' );
        }

    }

    /**
     * TODO also for 3D
     * 
     * @param envelope
     * @throws IOException 
     */
    public void writeEnvelope( Envelope envelope, Writer writer ) throws IOException {
        
        if ( flags.contains( WKTFlag.USE_ENVELOPE ) ) {
            // ENVELOPE(...)
        } else {
            Point pMax = envelope.getMax();
            Point pMin = envelope.getMin();
            if ( pMin == pMax ) {
                writePoint( pMin, writer ) ;
            } else {
                writer.append( "POLYGON((" );
                double pMinX = pMin.get0();
                double pMinY = pMin.get1();
                double pMaxX = pMax.get0();
                double pMaxY = pMax.get1();
                writer.append( Double.toString(pMinX) + ' ' + Double.toString(pMinY) + ',' );
                writer.append( Double.toString(pMaxX) + ' ' + Double.toString(pMinY) + ',' );
                writer.append( Double.toString(pMaxX) + ' ' + Double.toString(pMaxY) + ',' );
                writer.append( Double.toString(pMinX) + ' ' + Double.toString(pMaxY) + ',' );
                writer.append( Double.toString(pMinX) + ' ' + Double.toString(pMinY) );

                writer.append( "))" );

            }
        }
        
    }

    public static String write( Geometry geom ) {

        return jtsWriter.write( ( (AbstractDefaultGeometry) geom ).getJTSGeometry() );
    }

    public static void write( Geometry geom, Writer writer )
                            throws IOException {
        jtsWriter.write( ( (AbstractDefaultGeometry) geom ).getJTSGeometry(), writer );
    }

    /**
     * Does the work to write the standardproperties for the geometryobjects
     * <p>
     * This specification comes from the GML and is not necessary for databases but maybe necessary for the export
     * within deegree
     * 
     * @param sb
     * @param geom
     * @throws IOException 
     */
    private void appendObjectProps( Writer writer, Geometry geom ) throws IOException {

        writer.append( '[' );
        if ( geom.getId() != null ) {
            writer.append( "id='" );
            writer.append( geom.getId() );
            writer.append( '\'' );
            writer.append( ',' );
        }
        StandardGMLObjectProps props = geom.getAttachedProperties(); 
        if ( props != null ) {
            int counter = 0;

            // metadataproperties
            // TODO switch to metadataproperties[]
            writer.append( "metadataproperty=(" );
            for ( Object c : props.getMetadata() ) {
                counter++;
                writer.append( '\'' );
                writer.append( c.toString() );
                writer.append( '\'' );
                if ( counter != props.getMetadata().length ) {
                    writer.append( ',' );
                }

            }
            writer.append( ')' );
            writer.append( ',' );

            // description
            writer.append( "description='" );
            if(props.getDescription() != null){
            writer.append( props.getDescription().toString() );
            }
            writer.append( '\'' );
            writer.append( ',' );

            // name
            writer.append( "name=(" );
            counter = 0;
            for ( CodeType c : props.getNames() ) {
                counter++;
                writer.append( '\'' );
                writer.append( c.toString() );
                writer.append( '\'' );
                if ( counter != props.getNames().length ) {
                    writer.append( ',' );
                }

            }
            writer.append( ')' );

        }

        writer.append( ']' );
    }

}
