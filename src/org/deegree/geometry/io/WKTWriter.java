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

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
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
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.deegree.gml.props.StandardGMLObjectProps;

/**
 * Writes {@link Geometry} objects as Well-Known Text (WKT).
 * 
 * TODO re-implement without delegating to JTS TODO add support for non-SFS geometries (e.g. non-linear curves)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WKTWriter {

    private static final com.vividsolutions.jts.io.WKTWriter jtsWriter = new com.vividsolutions.jts.io.WKTWriter();

    private StringBuilder geometryString = new StringBuilder();
    
    private Set<WKTFlag> flags = new HashSet<WKTFlag>();

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
        /** COMPOUNDCURVE */
        USE_COMPOUND,
        /** Use deegree-WKT-extensions (object ids, properties, all geometry types) */
        USE_DKT

    }

    /**
     * @return the geometryString
     */
    public StringBuilder getGeometryString() {
        return geometryString;
    }

    /**
     * @return the flags
     */
    public Set<WKTFlag> getFlags() {
        return flags;
    }

    /**
     * @param flags
     *            the flags to set
     */
    public void setFlags( Set<WKTFlag> flags ) {
        this.flags = flags;
    }

    /*public void createSFS11Writer() {

        new WKTWriter();
    }

    public void createSFS12Writer( Set<WKTFlag> falgs ) {
        this.flags = falgs;

        new WKTWriter();
    }
    */
    public WKTWriter(Set<WKTFlag> flags){
        
    }

    public void writeGeometry( Geometry geometry ) {
        //geometryString = "";
        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY:
            writeCompositeGeometry( (CompositeGeometry<GeometricPrimitive>) geometry );
            break;
        case ENVELOPE:
            writeEnvelope( (Envelope) geometry );
            break;
        case MULTI_GEOMETRY:
            writeMultiGeometry( (MultiGeometry<? extends Geometry>) geometry );
            break;
        case PRIMITIVE_GEOMETRY:
            writeGeometricPrimitive( (GeometricPrimitive) geometry );
            break;
        }
    }

    /**
     * @param geometry
     */
    private void writeGeometricPrimitive( GeometricPrimitive geometry ) {
        switch ( geometry.getPrimitiveType() ) {
        case Point:
            writePoint( (Point) geometry );
            break;
        case Curve:
            writeCurve( (Curve) geometry );
            break;
        case Surface:
            writeSurface( (Surface) geometry );
            break;
        case Solid:
            writeSolid( (Solid) geometry );
            break;
        }

    }

    /**
     * @param geometry
     */
    public StringBuilder writePoint( Point geometry ) {
        geometryString.append( "POINT");
        appendObjectProps( geometryString, geometry );
        geometryString.append( "(");
        geometryString.append( writePointWithoutPrefix( geometry ));
        geometryString.append( ")");
        
        return geometryString;
    }

    /**
     * @param geometry
     */
    private StringBuilder writePointWithoutPrefix( Point geometry ) {

        StringBuilder s = new StringBuilder();
        if ( flags.contains( WKTFlag.USE_3D ) ) {
            s.append( geometry.get0());
            s.append( " ");
            s.append( geometry.get1());
            s.append( " ");
            s.append( geometry.get2());

        } else {

            s.append( geometry.get0());
            s.append( " ");
            s.append( geometry.get1());

        }

        return s;
    }

    /**
     * @param geometry
     */
    // TODO
    private void writeSolid( Solid geometry ) {
        switch ( geometry.getSolidType() ) {

        case Solid:
            break;
        case CompositeSolid:
            break;

        }

    }

    /**
     * @param geometry
     */
    private void writeSurface( Surface geometry ) {
        switch ( geometry.getSurfaceType() ) {

        case Surface:
            writeSurfaceGeometry( geometry );
            break;
        case Polygon:
            writePolygon( (Polygon) geometry );
            break;
        case PolyhedralSurface:
            writeSurfaceGeometry( (PolyhedralSurface) geometry );
            break;
        case TriangulatedSurface:
            writeSurfaceGeometry( (TriangulatedSurface) geometry );
            break;
        case Tin:
            writeTin( (Tin) geometry );
            break;
        case CompositeSurface:
            writeSurfaceGeometry( (CompositeSurface) geometry );
            break;
        case OrientableSurface:
            writeSurfaceGeometry( (OrientableSurface) geometry );
            break;

        }

    }

    /**
     * @param geometry
     */
    private void writeTin( Tin geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private StringBuilder writeSurfaceGeometry( Surface geometry ) {
        StringBuilder geometryString = new StringBuilder("MULTIPOLYGON(");
        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {

        } else {
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
                    geometryString.append(  writePolygonWithoutPrefix( poly ));

                    break;
                case RECTANGLE:
                    counter++;
                    Rectangle rectangle = ( (Rectangle) p );
                    poly = new DefaultPolygon( surface.getId(), surface.getCoordinateSystem(), surface.getPrecision(),
                                               rectangle.getExteriorRing(), rectangle.getInteriorRings() );
                    geometryString.append( writePolygonWithoutPrefix( poly ));
                    break;
                case TRIANGLE:
                    counter++;
                    Triangle triangle = ( (Triangle) p );
                    poly = new DefaultPolygon( surface.getId(), surface.getCoordinateSystem(), surface.getPrecision(),
                                               triangle.getExteriorRing(), triangle.getInteriorRings() );
                    geometryString.append(  writePolygonWithoutPrefix( poly ));
                    break;
                }
                if ( counter < l.size() ) {
                    geometryString.append( ",");
                }

            }
        }

        geometryString.append(  ")");
        return geometryString;

    }

    /**
     * @param geometry
     */
    public StringBuilder writePolygon( Polygon geometry ) {
        geometryString.append( "POLYGON");
        geometryString.append( writePolygonWithoutPrefix( geometry ));
        return geometryString;
    }

    /**
     * @param geometry
     */
    private StringBuilder writePolygonWithoutPrefix( Polygon geometry ) {
        StringBuilder s = new StringBuilder("(");
        Ring exteriorRing = geometry.getExteriorRing();
        s.append( "(");
        Points points = exteriorRing.getControlPoints();
        int counter = 0;
        for ( Point point : points ) {

            counter++;
            if ( counter < points.size() ) {
                s.append( writePointWithoutPrefix( point ) + ", ");
            } else {
                s.append( writePointWithoutPrefix( point ));
            }

        }

        s.append( ")");
        List<Ring> interiorRings = geometry.getInteriorRings();
        for ( Ring r : interiorRings ) {
            s.append( ",(");
            counter = 0;
            for ( Point point : points ) {

                counter++;
                if ( counter < points.size() ) {
                    s.append( writePointWithoutPrefix( point ) + ", ");
                } else {
                    s.append( writePointWithoutPrefix( point ));
                }

            }
            s.append( ")");
        }
        s.append( ")");
        return s;
    }

    /**
     * @param geometry
     */
    public void writeCurve( Curve geometry ) {
        switch ( geometry.getCurveType() ) {

        case Curve:
            writeCurveGeometry( geometry );
            break;

        case LineString:
            writeLineString( (LineString) geometry );
            break;

        case OrientableCurve:
            writeCurveGeometry( (OrientableCurve) geometry );
            break;

        case CompositeCurve:
            writeCompositeCurve( (CompositeCurve) geometry );
            break;

        case Ring:
            writeRing( (Ring) geometry );
            break;

        }

    }

    /**
     * @param geometry
     */
    private void writeCompositeCurve( CompositeCurve geometry ) {
        if ( flags.contains( WKTFlag.USE_COMPOSITES ) ) {
            // TODO difference between GML vs. PostGIS vs. ...

        } else {
            writeCurveGeometry( geometry );
        }

    }

    /**
     * @param geometry
     */
    private void writeCurveGeometry( Curve geometry ) {

        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {

        } else {
            CurveLinearizer cl = new CurveLinearizer( new GeometryFactory() );
            LinearizationCriterion crit = new NumPointsCriterion( 10 );
            Curve c = cl.linearize( geometry, crit );

            LineString s = new DefaultLineString( c.getId(), c.getCoordinateSystem(), c.getPrecision(),
                                                  c.getControlPoints() );
            writeLineString( s );
        }
    }

    /**
     * @param geometry
     */
    private String writeCurveGeometryWithoutPrefix( Curve geometry ) {

        String s = "";
        if ( flags.contains( WKTFlag.USE_SQL_MM ) ) {

        } else {
            CurveLinearizer cl = new CurveLinearizer( new GeometryFactory() );
            LinearizationCriterion crit = new NumPointsCriterion( 1 );
            Curve c = cl.linearize( geometry, crit );

            LineString ls = new DefaultLineString( c.getId(), c.getCoordinateSystem(), c.getPrecision(),
                                                   c.getControlPoints() );
            s += writeLineStringWithoutPrefix( ls );
        }

        return s;
    }

    /**
     * @param geometry
     */
    public StringBuilder writeLineString( LineString geometry ) {
        geometryString.append( "LINESTRING");
        geometryString.append( writeLineStringWithoutPrefix( geometry ));
        return geometryString;

    }

    /**
     * @param geometry
     */
    private StringBuilder writeLineStringWithoutPrefix( LineString geometry ) {
        StringBuilder s = new StringBuilder("(");
        Points points = geometry.getControlPoints();
        int counter = 0;
        for ( Point p : points ) {
            counter++;
            if ( counter < points.size() ) {
                s.append( writePointWithoutPrefix( p ) + ", ");
            } else {
                s.append(  writePointWithoutPrefix( p ));
            }
        }
        s.append( ")");
        return s;

    }

    /**
     * @param geometry
     */
    public void writeRing( Ring geometry ) {
        switch ( geometry.getRingType() ) {
        case LinearRing:

            writeLinearRing( (LinearRing) geometry );
            break;

        case Ring:

            writeCurveGeometry( geometry );
            break;

        }

    }

    /**
     * @param geometry
     */
    public void writeLinearRing( LinearRing geometry ) {
        if ( flags.contains( WKTFlag.USE_LINEARRING ) ) {

        } else {
            LineString s = new DefaultLineString( geometry.getId(), geometry.getCoordinateSystem(),
                                                  geometry.getPrecision(), geometry.getControlPoints() );
            writeLineString( s );
        }
    }

    /**
     * @param geometry
     */
    private void writeMultiGeometry( MultiGeometry<? extends Geometry> geometry ) {
        switch ( geometry.getMultiGeometryType() ) {
        case MULTI_GEOMETRY:
            writeMultiGeometryGeometry( geometry );
            break;
        case MULTI_POINT:
            writeMultiPoint( (MultiPoint) geometry );
            break;
        case MULTI_CURVE:
            writeMultiCurve( (MultiCurve) geometry );
            break;
        case MULTI_LINE_STRING:
            writeMultiLineString( (MultiLineString) geometry );
            break;
        case MULTI_SURFACE:
            break;
        case MULTI_POLYGON:
            writeMultiPolygon( (MultiPolygon) geometry );
            break;
        case MULTI_SOLID:
            break;

        }

    }

    /**
     * @param geometry
     */
    public StringBuilder writeMultiCurve( MultiCurve geometry ) {
        geometryString.append( "MULTICURVE(");

        for ( int i = 0; i < geometry.size(); i++ ) {

            geometryString.append( writeCurveGeometryWithoutPrefix( geometry.get( i ) ));
            if ( i < geometry.size() - 1 ) {
                geometryString.append( ",");
            }

        }

        geometryString.append( ")");
        
        return geometryString;

    }

    /**
     * @param geometry
     */
    private void writeMultiGeometryGeometry( MultiGeometry<? extends Geometry> geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private StringBuilder writeMultiPolygon( MultiPolygon geometry ) {
        geometryString.append( "MULTIPOLYGON(");
        
        for ( int i = 0; i < geometry.size(); i++ ) {

            geometryString.append( writePolygonWithoutPrefix( geometry.get( i ) ));
            if ( i < geometry.size() - 1 ) {
                geometryString.append( ",");
            }
        }

        geometryString.append( ")");
        return geometryString;
    }

    /**
     * @param geometry
     */
    private StringBuilder writeMultiLineString( MultiLineString geometry ) {
        geometryString.append(  "MULTILINESTRING(");
        for ( int i = 0; i < geometry.size(); i++ ) {

            geometryString.append( writeLineStringWithoutPrefix( geometry.get( i ) ));
            if ( i < geometry.size() - 1 ) {
                geometryString.append( ",");
            }
        }

        geometryString.append( ")");
        return geometryString;
    }

    /**
     * @param geometry
     */
    private StringBuilder writeMultiPoint( MultiPoint geometry ) {
        geometryString.append( "MULTIPOINT(");
        for ( int i = 0; i < geometry.size(); i++ ) {

            geometryString.append( writePointWithoutPrefix( geometry.get( i ) ));
            if ( i < geometry.size() - 1 ) {
                geometryString.append( ",");
            }
        }

        geometryString.append( ")");
        return geometryString;
    }

    /**
     * @param geometry
     */
    private void writeCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometry ) {
        // TODO Auto-generated method stub

    }

    private StringBuilder writeCircularString() {
        geometryString.append( "CIRCULARSTRING(");
        // TODO there should be 3 points in it

        geometryString.append( ")");
        return geometryString;

    }

    /**
     * TODO also for 3D
     * 
     * @param envelope
     */
    public StringBuilder writeEnvelope( Envelope envelope ) {
        if ( flags.contains( WKTFlag.USE_ENVELOPE ) ) {
            // ENVELOPE(...)
        } else {
            Point pMax = envelope.getMax();
            Point pMin = envelope.getMin();
            if ( pMin == pMax ) {
                geometryString.append( this.writePoint( pMin ));
            } else {
                geometryString.append( "POLYGON((");
                double pMinX = pMin.get0();
                double pMinY = pMin.get1();
                double pMaxX = pMax.get0();
                double pMaxY = pMax.get1();
                geometryString.append( pMinX + " " + pMinY + ", ");
                geometryString.append( pMaxX + " " + pMinY + ", ");
                geometryString.append( pMaxX + " " + pMaxY + ", ");
                geometryString.append( pMinX + " " + pMaxY + ", ");
                geometryString.append( pMinX + " " + pMinY);

                geometryString.append( "))");
                

            }
        }
        return geometryString;
    }

    public static String write( Geometry geom ) {

        return jtsWriter.write( ( (AbstractDefaultGeometry) geom ).getJTSGeometry() );
    }

    public static void write( Geometry geom, Writer writer )
                            throws IOException {
        jtsWriter.write( ( (AbstractDefaultGeometry) geom ).getJTSGeometry(), writer );
    }

    private void appendObjectProps (StringBuilder sb, Geometry geom) {
        if (flags.contains( WKTFlag.USE_DKT )) {
            sb.append ('[');
            if (geom.getId() != null) {
                sb.append ("id='");
                sb.append (geom.getId());
                sb.append ('\'');
            }
            StandardGMLObjectProps props = geom.getAttachedProperties();
            // TODO description, name, metadata, ...
            sb.append (']');
        }
    }

}
