//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2011 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.sqldialect.oracle.sdo;

import static org.deegree.geometry.validation.GeometryFixer.forceOrientation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.jdbc.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiGeometry.MultiGeometryType;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.GeometricPrimitive.PrimitiveType;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.patches.SurfacePatch.SurfacePatchType;
import org.deegree.geometry.primitive.segments.ArcString;
import org.deegree.geometry.primitive.segments.Circle;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.CurveSegment.CurveSegmentType;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.points.PackedPoints;
import org.deegree.geometry.standard.points.PointsArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert between Oracle JDBC STRUCT and deegree Geometry
 * 
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * 
 * @version $Revision$, $Date$
 */
public class SDOGeometryConverter {
    static final Logger LOG = LoggerFactory.getLogger( SDOGeometryConverter.class );

    public SDOGeometryConverter() {

    }

    private GeometryFactory _gf = new GeometryFactory();

    enum GeomHolderTyp {
        GEOMETRY, POINT, CURVE, POLYGON
    }

    protected static class GeomHolder {
        public final int gtype;

        public final int srid;

        public final int[] elem_info;

        public final double[] ordinates;

        public final int gtype_d;

        public final int gtype_l;

        public final int gtype_tt;

        public final int cnt_o;

        public final int cnt_e;

        protected List<Geometry> geoms;

        public ICRS crs;

        public int elemoff;

        public GeomHolderTyp last;

        public GeomHolder( final int gtype, final int srid, final double point[], final int[] elem_info,
                           final double[] ordinates, ICRS crs ) {
            this.gtype = gtype;
            this.srid = srid;
            this.crs = crs;

            this.gtype_tt = gtype % 100;

            // ensure correct range
            int tdims = gtype / 1000;
            if ( tdims < 2 || tdims > 4 )
                this.gtype_d = 2;
            else
                this.gtype_d = tdims;

            this.gtype_l = ( gtype % 1000 ) / 100;

            if ( point != null && point.length > 0 ) {
                this.elem_info = new int[] { 1, 1, 1 };
                this.ordinates = new double[this.gtype_d];
                this.ordinates[0] = point[0];
                this.ordinates[1] = point[1];
                if ( this.gtype_d > 2 )
                    this.ordinates[2] = point[2];
            } else {
                this.elem_info = elem_info;
                this.ordinates = ordinates;
            }

            geoms = new LinkedList<Geometry>();

            this.cnt_o = ( this.ordinates != null ) ? this.ordinates.length : 0;
            this.cnt_e = ( this.elem_info != null ) ? this.elem_info.length : 0;

            this.elemoff = -3;
            this.last = null;
        }

        double[] getOrdinatesEntry( int off ) {
            double[] out = new double[this.gtype_d];
            if ( this.gtype_d == 2 ) {
                out[0] = ordinates[off];
                out[1] = ordinates[off + 1];
            } else {
                for ( int i = off, j = 0; j < this.gtype_d; j++ ) {
                    out[j] = ordinates[i];
                    i++;
                }
            }
            return out;
        }

        public int nxt() {
            elemoff += 3;

            if ( elemoff >= 0 && elemoff + 1 < cnt_e )
                return elem_info[elemoff + 1];
            else
                return -1;
        }

        public void prev() {
            elemoff -= 3;
        }

        public int cnt() {
            int end;
            if ( elemoff + 4 < cnt_e )
                end = elem_info[elemoff + 3] - 1;
            else
                end = cnt_o;

            return ( end - elem_info[elemoff] + 1 ) / this.gtype_d;
        }

        public void add( GeomHolderTyp typ, Geometry elem ) {
            if ( last == null )
                last = typ;
            else if ( last != typ )
                last = GeomHolderTyp.GEOMETRY;

            this.geoms.add( elem );
        }
    }

    protected static class Triplet {
        public int a_off;

        public int b_typ;

        public int c_int;

        public Triplet( int offset, int type, int interpretation ) {
            this( offset, type, interpretation, false );
        }

        public Triplet( int offset, int type, int interpretation, boolean continueGeom ) {
            if ( continueGeom && offset > 0 )
                this.a_off = offset - 1;
            else
                this.a_off = offset;

            this.b_typ = type;
            this.c_int = interpretation;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append( "[" ).append( this.a_off ).append( ", " );
            sb.append( this.b_typ ).append( ", " ).append( this.c_int ).append( "]" );
            return sb.toString();

        }
    }

    /**
     * Convert SDO_GEOMETRY (from Oracle STRUCT) to deegree Geometry
     * 
     * <h3>NOTE 1 (polygon)</h3>
     * <ul>
     * <li>Polygons (simple, multiple or compound) will be treated to have at least one exterior or unknown ring.</li>
     * <li>When a polygon starts with interior rings the first unknown ring is treaded as exterior ring.</li>
     * <li>Each N>1 occurrence of an exterior ring is treaded as new polygon.</li>
     * </ul>
     * 
     * @param sdoStruct
     *            Database Object of type oracle.sql.STRUCT
     * @param crs
     *            SRS or null for automatic recognition from srid
     * @return Geometry
     * @throws SQLException
     */
    public Geometry toGeometry( STRUCT sdoStruct, ICRS crs )
                            throws SQLException {
        if ( sdoStruct == null ) {
            return null;
        }

        Datum data[] = sdoStruct.getOracleAttributes();

        int gtype = OracleObjectTools.fromInteger( data[0], 0 );
        int srid = OracleObjectTools.fromInteger( data[1], -1 );
        double[] point = OracleObjectTools.fromDoubleArray( (STRUCT) data[2], Double.NaN );
        int[] elemInfo = OracleObjectTools.fromIntegerArray( (ARRAY) data[3] );
        double[] ordinates = OracleObjectTools.fromDoubleArray( (ARRAY) data[4], Double.NaN );

        int gtype_d, tdims = gtype / 1000;
        if ( tdims < 2 || tdims > 4 ) {
            gtype_d = 2;
        } else {
            gtype_d = tdims;
        }

        int gtype_l = ( gtype % 1000 ) / 100;
        if ( gtype_l > 0 ) { // contains LRS?
            // create ordinates array without LRS dimension
            double[] newOrdinates = new double[( ordinates.length / gtype_d ) * ( gtype_d - 1 )];

            int j = 0;
            for ( int i = 0; i < ordinates.length; i++ ) {
                if ( i % gtype_d != ( gtype_l - 1 ) ) { // ordinate not in LRS dimension?
                    newOrdinates[j++] = ordinates[i];
                }
            }

            ordinates = newOrdinates;

            // create elemInfo array without LRS dimension
            int[] newElemInfo = new int[elemInfo.length];
            for ( int i = 0; i < newElemInfo.length; i++ ) {
                if ( i % 3 == 0 ) { 
                    // compute new offset in ordinates array
                    newElemInfo[i] = ( ( elemInfo[i] - 1 ) / gtype_d ) * ( gtype_d - 1 ) + 1;
                } else {
                    newElemInfo[i] = elemInfo[i];
                }
            }

            elemInfo = newElemInfo;

            // compute new gtype
            gtype = gtype - 1000 - gtype_l * 100;
        }

        GeomHolder sdo = new GeomHolder( gtype, srid, point, elemInfo, ordinates, crs );

        return toGeometry( sdo, crs );
    }

    @SuppressWarnings("unchecked")
    protected Geometry toGeometry( GeomHolder sdo, ICRS crs )
                            throws SQLException {
        if ( sdo.cnt_o < sdo.gtype_d || sdo.cnt_e < 3 || sdo.cnt_o % sdo.gtype_d > 0 || sdo.cnt_e % 3 > 0 )
            throw new SQLException( "Illegal Geometry" );
        else if ( sdo.gtype_tt == SDOGTypeTT.UNKNOWN )
            throw new SQLException( "Unsupported Geometry" );

        Ring ringe = null;
        List<Ring> ringi = new ArrayList<Ring>();
        Set<Ring> ringu = new HashSet<Ring>();

        int etype;
        while ( ( etype = sdo.nxt() ) != -1 ) {
            switch ( etype ) {
            case SDOEType.POINT:
                handlePoint( sdo );
                break;

            case SDOEType.LINESTRING:
                handleLine( sdo );
                break;

            case SDOEType.POLYGON_RING_INTERIOR: // see above NOTE 1
            case SDOEType.POLYGON_RING_EXTERIOR:
            case SDOEType.POLYGON_RING_UNKNOWN:
                Ring rng = handleSimpleRing( sdo );
                switch ( etype ) {
                case SDOEType.POLYGON_RING_INTERIOR:
                    ringi.add( rng );
                    break;
                case SDOEType.POLYGON_RING_EXTERIOR:
                    if ( ringe != null ) {
                        sdo.add( GeomHolderTyp.POLYGON, _gf.createPolygon( null, sdo.crs, ringe, ringi ) );
                        ringi = new ArrayList<Ring>();
                    }
                    ringe = rng;
                    break;
                case SDOEType.POLYGON_RING_UNKNOWN:
                    ringu.add( rng );
                }
                break;

            case SDOEType.COMPOUND_LINESTRING:
                sdo.add( GeomHolderTyp.CURVE, handleCompoundCurve( sdo ) );
                break;

            case SDOEType.COMPOUND_POLYGON_RING_INTERIOR: // see above NOTE 1
            case SDOEType.COMPOUND_POLYGON_RING_EXTERIOR:
            case SDOEType.COMPOUND_POLYGON_RING_UNKNOWN:
                rng = handleCompoundRing( sdo );
                switch ( etype ) {
                case SDOEType.COMPOUND_POLYGON_RING_INTERIOR:
                    ringi.add( rng );
                    break;
                case SDOEType.COMPOUND_POLYGON_RING_EXTERIOR:
                    if ( ringe != null ) {
                        sdo.add( GeomHolderTyp.POLYGON, _gf.createPolygon( null, sdo.crs, ringe, ringi ) );
                        ringi = new ArrayList<Ring>();
                    }
                    ringe = rng;
                    break;
                case SDOEType.COMPOUND_POLYGON_RING_UNKNOWN:
                    ringu.add( rng );
                }

                break;

            case SDOEType.UNKNOWN:
            default: // other / not implemented
                createUnknownException( sdo );
                break;
            }
        }

        if ( ringe != null ) {
            sdo.add( GeomHolderTyp.POLYGON, _gf.createPolygon( null, sdo.crs, ringe, ringi ) );
        }

        if ( !ringu.isEmpty() ) {
            LOG.warn( "SDO_Geometry with rings of unknown type detected. "
                      + "Please consider upgrading to the current format using "
                      + "the SDO_MIGRATE.TO_CURRENT procedure." );

            // maps to store spatial relations between rings
            Map<Ring, Set<Ring>> contained = new HashMap<Ring, Set<Ring>>();
            Map<Ring, Set<Ring>> contains = new HashMap<Ring, Set<Ring>>();

            // construct a polygon for every ring in order to be able to utilize isWithin
            List<Polygon> polygons = new ArrayList<Polygon>( ringu.size() );
            for ( Ring r : ringu ) {
                polygons.add( _gf.createPolygon( null, r.getCoordinateSystem(), r, Collections.<Ring> emptyList() ) );
            }

            // determine spatial relationships between rings
            for ( Polygon a : polygons ) {
                for ( Polygon b : polygons ) {
                    if ( a != b ) {
                        if ( a.isWithin( b ) ) {
                            Set<Ring> containedSet = contained.get( a.getExteriorRing() );
                            if ( containedSet == null ) {
                                containedSet = new HashSet<Ring>();
                                contained.put( a.getExteriorRing(), containedSet );
                            }
                            containedSet.add( b.getExteriorRing() );
                        }

                        if ( b.isWithin( a ) ) {
                            Set<Ring> containsSet = contains.get( a.getExteriorRing() );
                            if ( containsSet == null ) {
                                containsSet = new HashSet<Ring>();
                                contains.put( a.getExteriorRing(), containsSet );
                            }
                            containsSet.add( b.getExteriorRing() );
                        }
                    }
                }
            }

            // iteratively determine whether a ring is 
            // an exterior or an interior ring until all rings are processed
            while ( !ringu.isEmpty() ) {
                List<Ring> used = new ArrayList<Ring>();
                for ( Ring a : ringu ) {
                    if ( !contained.containsKey( a ) ) {
                        used.add( a );
                        ringi = new ArrayList<Ring>();

                        Set<Ring> rings = contains.get( a );
                        if ( rings != null ) {
                            for ( Ring b : rings ) {
                                Set<Ring> containedSet = contained.get( b );
                                if ( containedSet == null || containedSet.size() == 1 ) {
                                    used.add( b );
                                    ringi.add( b );
                                }
                            }
                        }

                        sdo.add( GeomHolderTyp.POLYGON, _gf.createPolygon( null, sdo.crs, a, ringi ) );
                    }
                }

                // we should have solved the problem for at least one
                // ring this time around (provided the overall problem is solvable at all)
                if ( used.isEmpty() ) {
                    throw new IllegalArgumentException(
                                                        "Illegal Geometry: failed to construct polgyons based on rings of unknown type" );
                }

                // remove already processed rings
                for ( Ring r : used ) {
                    ringu.remove( r );

                    contained.remove( r );
                    for ( Set<Ring> s : contained.values() ) {
                        s.remove( r );
                    }

                    contains.remove( r );
                    for ( Set<Ring> s : contains.values() ) {
                        s.remove( r );
                    }
                }
            }
        }

        if ( sdo.geoms.size() == 0 ) {
            // no known valid geometry
            return null;
        }

        if ( SDOGTypeTT.COLLECTION == sdo.gtype_tt ) {
            // geometry is in any case a collection
            return _gf.createMultiGeometry( null, sdo.crs, sdo.geoms );
        } else if ( SDOGTypeTT.MULTIPOINT == sdo.gtype_tt || SDOGTypeTT.MULTILINE == sdo.gtype_tt
                    || SDOGTypeTT.MULTIPOLYGON == sdo.gtype_tt || sdo.geoms.size() > 1 ) {
            // returned geometry is multi* or collection (on different types)
            List<?> ungeom = sdo.geoms;

            // LOG.debug( "MULTI: Createing of type {}", sdo.last.name() );
            if ( sdo.last == GeomHolderTyp.POINT ) {
                return _gf.createMultiPoint( null, crs, (List<Point>) ungeom );
            } else if ( sdo.last == GeomHolderTyp.CURVE ) {
                return _gf.createMultiCurve( null, crs, (List<Curve>) ungeom );
            } else if ( sdo.last == GeomHolderTyp.POLYGON ) {
                return _gf.createMultiPolygon( null, crs, (List<Polygon>) ungeom );
            } else {
                return _gf.createMultiGeometry( null, sdo.crs, sdo.geoms );
            }

        } else {
            // single type
            return sdo.geoms.get( 0 );
        }
    }

    private Curve handleCompoundCurve( GeomHolder sdo )
                            throws SQLException {
        int subelem = sdo.elem_info[sdo.elemoff + 2];

        List<CurveSegment> lst = new LinkedList<CurveSegment>();

        for ( int i = 0; i < subelem; i++ ) {
            sdo.nxt();
            handleCurveSegment( sdo, lst, ( i + 1 ) == subelem );
        }

        return _gf.createCurve( null, sdo.crs, lst.toArray( new CurveSegment[lst.size()] ) );
    }

    private Ring handleCompoundRing( GeomHolder sdo )
                            throws SQLException {
        int subelem = sdo.elem_info[sdo.elemoff + 2];

        List<CurveSegment> lst = new LinkedList<CurveSegment>();
        for ( int i = 0; i < subelem; i++ ) {
            sdo.nxt();
            handleCurveSegment( sdo, lst, ( i + 1 ) == subelem );
        }

        List<Curve> clst = new LinkedList<Curve>();
        clst.add( _gf.createCurve( null, sdo.crs, lst.toArray( new CurveSegment[lst.size()] ) ) );
        return _gf.createRing( null, sdo.crs, clst );
    }

    /**
     * Handle Point Type
     * 
     * Interpretation are handled as follow:
     * <ol>
     * <li>1) normal point (one set of coordinates)</li>
     * <li>0) oriented point (second set of coordinates is ignored)</li>
     * <li>N > 1) point cluster is treated as multiple points (N sets of coordinates)</li>
     * </ol>
     */
    private void handlePoint( GeomHolder sdo )
                            throws SQLException {
        int intpr = sdo.elem_info[sdo.elemoff + 2];
        int off = sdo.elem_info[sdo.elemoff] - 1; // from 1 based to 0 based

        if ( intpr > 1 ) {
            for ( int i = 0; i < intpr; i++ ) {
                sdo.add( GeomHolderTyp.POINT,
                         _gf.createPoint( null, sdo.getOrdinatesEntry( off + ( i * sdo.gtype_d ) ), sdo.crs ) );
            }
        } else if ( intpr == 0 || intpr == 1 ) {
            sdo.add( GeomHolderTyp.POINT, _gf.createPoint( null, sdo.getOrdinatesEntry( off ), sdo.crs ) );
        } else {
            createGeometryException( sdo );
        }
    }

    // private List<Point> getPoints( GeomHolder sdo, int offset, int cnt ) {
    // // double[] buf = new double[sdo.dims];
    // List<Point> members = new LinkedList<Point>();
    //
    // // from 1 based to 0 based array
    // int off = offset - 1;
    // for ( int i = 0; i < cnt; i++ ) {
    // members.add( _gf.createPoint( null, sdo.getOrdinatesEntry( off ), sdo.crs ) );
    // off += sdo.gtype_d;
    // }
    //
    // return members;
    // }

    private PackedPoints getPackedPoints( GeomHolder sdo, int offset, int cnt ) {
        double[] pnts = new double[sdo.gtype_d * cnt];

        System.arraycopy( sdo.ordinates, offset - 1, pnts, 0, pnts.length );

        return new PackedPoints( sdo.crs, pnts, sdo.gtype_d );
    }

    private void handleLine( GeomHolder sdo )
                            throws SQLException {
        int intpr = sdo.elem_info[sdo.elemoff + 2];
        int off = sdo.elem_info[sdo.elemoff];
        // Points pnts = _gf.createPoints( getPoints( sdo, off, sdo.cnt() ) );
        PackedPoints pnts = getPackedPoints( sdo, off, sdo.cnt() );

        if ( intpr == 1 ) {
            sdo.add( GeomHolderTyp.CURVE, _gf.createLineString( null, sdo.crs, pnts ) );
        } else if ( intpr == 2 ) {
            sdo.add( GeomHolderTyp.CURVE, _gf.createCurve( null, sdo.crs, _gf.createArcString( pnts ) ) );
        } else {
            createGeometryException( sdo );
        }
    }

    private void handleCurveSegment( GeomHolder sdo, List<CurveSegment> cseg, boolean last )
                            throws SQLException {
        int intpr = sdo.elem_info[sdo.elemoff + 2];
        int off = sdo.elem_info[sdo.elemoff];
        // Points pnts = _gf.createPoints( getPoints( sdo, off, ( sdo.cnt() + ( ( last ) ? 0 : 1 ) ) ) );
        PackedPoints pnts = getPackedPoints( sdo, off, sdo.cnt() + ( ( last ) ? 0 : 1 ) );

        if ( intpr == 1 )
            cseg.add( _gf.createLineStringSegment( pnts ) );
        else if ( intpr == 2 && pnts.size() == 3 )
            cseg.add( _gf.createArc( pnts.get( 0 ), pnts.get( 1 ), pnts.get( 2 ) ) );
        else if ( intpr == 2 )
            cseg.add( _gf.createArcString( pnts ) );
        else
            createGeometryException( sdo );
    }

    /**
     * Handle (simple) ring types of a polygon (POLYGON_RING_EXTERIOR, POLYGON_RING_INTERIOR, POLYGON_RING_UNKNOWN)
     * 
     * Interpretation are handled as follow:
     * <ol>
     * <li>1) polygon ring; connected sequence of straight lines</li>
     * <li>2) polygon ring; connected sequence of circular arcs</li>
     * <li>3) polygon ring / optimized rectangle; described as lower-left and upper-right coordinate</li>
     * <li>4) circle; three distinct points on the circle</li>
     * </ol>
     */
    private Ring handleSimpleRing( GeomHolder sdo )
                            throws SQLException {
        int intpr = sdo.elem_info[sdo.elemoff + 2];
        int off = sdo.elem_info[sdo.elemoff];
        // Points pnts = _gf.createPoints( getPoints( sdo, off, sdo.cnt() ) );
        PackedPoints pnts = getPackedPoints( sdo, off, sdo.cnt() );

        Ring rng = null;
        if ( intpr == 1 )
            rng = _gf.createLinearRing( null, sdo.crs, pnts );
        else if ( intpr == 2 || intpr == 4 ) {
            List<Curve> lc = new LinkedList<Curve>();
            if ( intpr == 2 ) {
                lc.add( _gf.createCurve( null, sdo.crs, _gf.createArcString( pnts ) ) );
            } else {
                if ( pnts.size() != 3 )
                    createGeometryException( sdo );

                lc.add( _gf.createCurve( null, sdo.crs, _gf.createCircle( pnts.get( 0 ), pnts.get( 1 ), pnts.get( 2 ) ) ) );
            }

            rng = _gf.createRing( null, sdo.crs, lc );
        } else if ( intpr == 3 ) {
            Point ll = pnts.getStartPoint();
            Point ur = pnts.getEndPoint();

            Point a = _gf.createPoint( null, ll.get0(), ll.get1(), sdo.crs );
            Point b = _gf.createPoint( null, ll.get0(), ur.get1(), sdo.crs );
            Point c = _gf.createPoint( null, ur.get0(), ur.get1(), sdo.crs );
            Point d = _gf.createPoint( null, ur.get0(), ll.get1(), sdo.crs );
            Points rngp = null;
            if ( ll.get0() < ur.get0() || ll.get1() < ur.get1() ) {
                rngp = new PointsArray( a, b, c, d, a );
            } else {
                rngp = new PointsArray( a, d, c, b, a );
            }
            rng = _gf.createLinearRing( null, sdo.crs, rngp );
        }

        if ( rng == null ) {
            createGeometryException( sdo );
        }

        return rng;
    }

    private void createUnknownException( GeomHolder sdo ) {
        sdo.prev();
        StringBuilder sb = new StringBuilder();
        sb.append( "Geometry from Type " ).append( sdo.gtype );
        sb.append( " with first ETYPE " ).append( sdo.nxt() );
        sb.append( "is not known" );
        throw new InvalidParameterValueException( sb.toString() );
    }

    private void createGeometryException( GeomHolder sdo )
                            throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append( "Geometry from Type " ).append( sdo.gtype );
        sb.append( " has a invalid structure [ " );
        if ( sdo.elem_info == null ) {
            sb.append( "null " );
        } else {
            for ( int i = 0, j = sdo.elem_info.length; i < j; i++ ) {
                if ( i != 0 )
                    sb.append( ", " );

                if ( i == sdo.elemoff )
                    sb.append( "*" );

                sb.append( sdo.elem_info[i] );

                if ( i > 2 && ( i - 2 ) == sdo.elemoff )
                    sb.append( "* " );
            }
        }
        sb.append( "]" );
        throw new InvalidParameterValueException( sb.toString() );
    }

    @Deprecated
    public Object fromGeometry( OracleConnection conn, int srid, Geometry geometry )
                            throws SQLException {
        return fromGeometry( conn, srid, geometry, true );
    }

    @SuppressWarnings("unchecked")
    public Object fromGeometry( OracleConnection conn, int srid, Geometry geometry, boolean allowJTSfallback )
                            throws SQLException {

        List<Triplet> info = new LinkedList<Triplet>();
        List<Point> pnts = new LinkedList<Point>();
        int gtypett = SDOGTypeTT.UNKNOWN;

        try {
            GeometryType typ = geometry.getGeometryType();
            if ( typ == GeometryType.PRIMITIVE_GEOMETRY ) {
                gtypett = buildPrimitive( info, pnts, (GeometricPrimitive) geometry );
            } else if ( typ == GeometryType.MULTI_GEOMETRY ) {
                gtypett = buildMultiGeometry( info, pnts, (MultiGeometry<Geometry>) geometry );
            } else if ( typ == GeometryType.ENVELOPE ) {
                gtypett = buildEnvelope( info, pnts, (Envelope) geometry );
            } else {
                // COMPOSITE_GEOMETRY: and others
                throw new InvalidParameterValueException();
            }
        } catch ( InvalidParameterValueException ipve ) {
            // can not be mapped directly so try JTS if allowed
            info.clear();
            pnts.clear();
        }

        try {
            if ( info.size() == 0 && allowJTSfallback && geometry instanceof AbstractDefaultGeometry ) {
                // build geometry with JTS
                AbstractDefaultGeometry ageom = (AbstractDefaultGeometry) geometry;
                gtypett = buildJTSGeometry( info, pnts, geometry.getCoordinateSystem(), ageom.getJTSGeometry() );
            }
        } catch ( Exception ex ) {
            // geometry could not completely be build
            info.clear();
            pnts.clear();
        }

        if ( info.size() > 0 ) {
            // build geometry
            int dim = 2;
            for ( Point p : pnts ) {
                if ( p.getCoordinateDimension() > dim )
                    dim = p.getCoordinateDimension();
            }
            if ( dim > 4 )
                dim = 4;

            double[] ordinates = buildResultOrdinates( dim, pnts );
            int[] elemInfo = buildResultElemeInfo( dim, info );

            int gtyp = ( 1000 * dim ) + gtypett;
            return OracleObjectTools.toSDOGeometry( gtyp, srid, elemInfo, ordinates, conn );
        } else {
            // create error message
        }

        return null;
    }

    private double[] buildResultOrdinates( int dim, List<Point> pnts ) {
        return buildResultOrdinates( dim, pnts, 0.0d );
    }

    private double[] buildResultOrdinates( int dim, List<Point> pnts, double defaultValue ) {
        int ord_pos = 0;
        double ords[] = new double[pnts.size() * dim];
        Point ord;
        for ( int i = 0, j = pnts.size(); i < j; i++ ) {
            ord = pnts.get( i );
            ords[ord_pos++] = ord.get0();
            ords[ord_pos++] = ord.get1();

            if ( dim == 3 ) {
                if ( ord.getCoordinateDimension() > 2 )
                    ords[ord_pos++] = ord.get( 2 );
                else
                    ords[ord_pos++] = defaultValue;
            } else if ( dim > 3 ) {
                if ( ord.getCoordinateDimension() > 3 )
                    ords[ord_pos++] = ord.get( 2 );
                else
                    ords[ord_pos++] = defaultValue;
            }
        }

        return ords;
    }

    private int[] buildResultElemeInfo( int dim, List<Triplet> info ) {
        int elem_info[] = new int[info.size() * 3];
        int elem_cnt = 0;

        Triplet t;
        for ( int i = 0, j = info.size(); i < j; i++ ) {
            t = info.get( i );
            elem_info[elem_cnt++] = ( t.a_off * dim ) + 1;
            elem_info[elem_cnt++] = t.b_typ;
            elem_info[elem_cnt++] = t.c_int;
        }

        return elem_info;
    }

    private void addCoordinate( List<Point> pnts, ICRS crs, org.locationtech.jts.geom.Coordinate coord ) {
        if ( Double.isNaN( coord.z ) ) {
            pnts.add( _gf.createPoint( null, coord.x, coord.y, crs ) );
        } else {
            pnts.add( _gf.createPoint( null, coord.x, coord.y, coord.z, crs ) );
        }
    }

    private int buildJTSGeometry( List<Triplet> info, List<Point> pnts, ICRS crs,
                                  org.locationtech.jts.geom.Geometry geom ) {
        int gtyp = SDOGTypeTT.UNKNOWN;

        if ( geom instanceof org.locationtech.jts.geom.Point ) {
            buildJTSPoint( info, pnts, crs, (org.locationtech.jts.geom.Point) geom );
            gtyp = SDOGTypeTT.POINT;
        } else if ( geom instanceof org.locationtech.jts.geom.LinearRing ) {
            buildJTSLineString( info, pnts, crs, (org.locationtech.jts.geom.LineString) geom,
                                SDOEType.POLYGON_RING_EXTERIOR );
            gtyp = SDOGTypeTT.POLYGON;
        } else if ( geom instanceof org.locationtech.jts.geom.LineString ) {
            buildJTSLineString( info, pnts, crs, (org.locationtech.jts.geom.LineString) geom, SDOEType.LINESTRING );
            gtyp = SDOGTypeTT.LINE;
        } else if ( geom instanceof org.locationtech.jts.geom.Polygon ) {
            org.locationtech.jts.geom.Polygon polygon = (org.locationtech.jts.geom.Polygon) geom;
            buildJTSLineString( info, pnts, crs, polygon.getExteriorRing(), SDOEType.POLYGON_RING_EXTERIOR );
            for ( int i = 0, j = polygon.getNumInteriorRing(); i < j; i++ ) {
                buildJTSLineString( info, pnts, crs, polygon.getInteriorRingN( i ), SDOEType.POLYGON_RING_INTERIOR );
            }
            gtyp = SDOGTypeTT.POLYGON;
        } else if ( geom instanceof org.locationtech.jts.geom.MultiPoint ) {
            for ( int m = 0, n = geom.getNumGeometries(); m < n; m++ ) {
                buildJTSPoint( info, pnts, crs, (org.locationtech.jts.geom.Point) geom.getGeometryN( m ) );
            }
            gtyp = SDOGTypeTT.MULTIPOINT;
        } else if ( geom instanceof org.locationtech.jts.geom.MultiLineString ) {
            for ( int m = 0, n = geom.getNumGeometries(); m < n; m++ ) {
                buildJTSLineString( info, pnts, crs, (org.locationtech.jts.geom.LineString) geom.getGeometryN( m ),
                                    SDOEType.LINESTRING );
            }
            gtyp = SDOGTypeTT.MULTILINE;
        } else if ( geom instanceof org.locationtech.jts.geom.MultiPolygon ) {
            org.locationtech.jts.geom.Polygon polygon = null;
            for ( int m = 0, n = geom.getNumGeometries(); m < n; m++ ) {
                polygon = (org.locationtech.jts.geom.Polygon) geom.getGeometryN( m );
                buildJTSLineString( info, pnts, crs, polygon.getExteriorRing(), SDOEType.POLYGON_RING_EXTERIOR );
                for ( int i = 0, j = polygon.getNumInteriorRing(); i < j; i++ ) {
                    buildJTSLineString( info, pnts, crs, polygon.getInteriorRingN( i ), SDOEType.POLYGON_RING_INTERIOR );
                }
            }
            gtyp = SDOGTypeTT.MULTIPOLYGON;
        } else if ( geom instanceof org.locationtech.jts.geom.GeometryCollection ) {
            org.locationtech.jts.geom.Geometry subgeom = null;
            for ( int m = 0, n = geom.getNumGeometries(); m < n; m++ ) {
                subgeom = geom.getGeometryN( m );

                if ( subgeom instanceof org.locationtech.jts.geom.Point
                     || subgeom instanceof org.locationtech.jts.geom.LinearRing
                     || subgeom instanceof org.locationtech.jts.geom.LineString
                     || subgeom instanceof org.locationtech.jts.geom.Polygon
                     || subgeom instanceof org.locationtech.jts.geom.MultiPoint
                     || subgeom instanceof org.locationtech.jts.geom.MultiLineString
                     || subgeom instanceof org.locationtech.jts.geom.MultiPolygon ) {
                    // only non cascading types
                    buildJTSGeometry( info, pnts, crs, subgeom );
                } else {
                    throw new InvalidParameterValueException();
                }
            }
            gtyp = SDOGTypeTT.COLLECTION;
        } else {
            // other unknown
            throw new InvalidParameterValueException();
        }

        return gtyp;
    }

    private void buildJTSPoint( List<Triplet> info, List<Point> pnts, ICRS crs, org.locationtech.jts.geom.Point geom ) {
        info.add( new Triplet( pnts.size(), 1, 1 ) );
        addCoordinate( pnts, crs, ( (org.locationtech.jts.geom.Point) geom ).getCoordinate() );
    }

    private void buildJTSLineString( List<Triplet> info, List<Point> pnts, ICRS crs,
                                     org.locationtech.jts.geom.LineString geom, int etype ) {
        info.add( new Triplet( pnts.size(), etype, 1 ) );
        for ( org.locationtech.jts.geom.Coordinate coord : geom.getCoordinates() ) {
            addCoordinate( pnts, crs, coord );
        }
    }

    private int buildMultiGeometry( List<Triplet> info, List<Point> pnts, MultiGeometry<Geometry> geom ) {
        MultiGeometryType mtyp = geom.getMultiGeometryType();

        int gtyp = SDOGTypeTT.COLLECTION;
        if ( mtyp == MultiGeometryType.MULTI_POINT ) {
            gtyp = SDOGTypeTT.MULTIPOINT;
        } else if ( mtyp == MultiGeometryType.MULTI_CURVE || mtyp == MultiGeometryType.MULTI_LINE_STRING ) {
            gtyp = SDOGTypeTT.MULTILINE;
        } else if ( mtyp == MultiGeometryType.MULTI_SURFACE || mtyp == MultiGeometryType.MULTI_POLYGON ) {
            gtyp = SDOGTypeTT.MULTIPOLYGON;
        } else if ( mtyp == MultiGeometryType.MULTI_SOLID ) {
            // not supported
            throw new InvalidParameterValueException();
        }

        for ( Geometry geometry : geom ) {
            GeometryType typ = geometry.getGeometryType();
            if ( typ == GeometryType.PRIMITIVE_GEOMETRY ) {
                buildPrimitive( info, pnts, (GeometricPrimitive) geometry );
            } else if ( typ == GeometryType.ENVELOPE ) {
                buildEnvelope( info, pnts, (Envelope) geometry );
            } else {
                // COMPOSITE_GEOMETRY
                // MULTI_GEOMETRY inside a MULTI_GEOMETRY
                // SOLID
                // and others
                throw new InvalidParameterValueException();
            }
        }
        return gtyp;
    }

    private int buildEnvelope( List<Triplet> info, List<Point> pnts, Envelope geom ) {
        info.add( new Triplet( pnts.size(), SDOEType.POLYGON_RING_EXTERIOR, 3 ) );
        pnts.add( geom.getMin() );
        pnts.add( geom.getMax() );

        return SDOGTypeTT.POLYGON;
    }

    protected int buildPrimitive( List<Triplet> info, List<Point> pnts, GeometricPrimitive geom ) {
        PrimitiveType typ = geom.getPrimitiveType();
        int gtyp;

        if ( typ == PrimitiveType.Curve ) {
            buildCurve( info, pnts, (Curve) geom );
            gtyp = SDOGTypeTT.LINE;
        } else if ( typ == PrimitiveType.Point ) {
            info.add( new Triplet( pnts.size(), 1, 1 ) );
            pnts.add( (Point) geom );
            gtyp = SDOGTypeTT.POINT;
        } else if ( typ == PrimitiveType.Surface ) {
            buildSurface( info, pnts, (Surface) geom );
            gtyp = SDOGTypeTT.POLYGON;
        } else {
            // unsupported
            throw new InvalidParameterValueException();
        }

        return gtyp;
    }

    private void buildSurface( List<Triplet> info, List<Point> pnts, Surface geometry ) {
        for ( SurfacePatch sp : geometry.getPatches() ) {
            if ( sp.getSurfacePatchType() != SurfacePatchType.POLYGON_PATCH )
                throw new InvalidParameterValueException();

            PolygonPatch pp = (PolygonPatch) sp;

            boolean isSimple = true;
            boolean hasCicrcle = false;

            for ( Ring r : pp.getBoundaryRings() ) {
                List<CurveSegment> csegs = r.getCurveSegments();
                if ( csegs.size() > 1 ) {
                    // compund
                    isSimple = false;
                }

                for ( CurveSegment cseg : csegs ) {
                    CurveSegmentType csegt = cseg.getSegmentType();

                    if ( csegt == CurveSegmentType.CIRCLE ) {
                        // acepted simple types (only with other simple)
                        hasCicrcle = true;
                    } else if ( csegt == CurveSegmentType.ARC || csegt == CurveSegmentType.ARC_STRING
                                || csegt == CurveSegmentType.LINE_STRING_SEGMENT ) {
                        // acepted simple types
                    } else {
                        // others not supported
                        throw new InvalidParameterValueException();
                    }
                }
            }

            if ( !isSimple && hasCicrcle ) {
                // circles and compund not supported
                throw new InvalidParameterValueException();
            }

            List<CurveSegment> eseg = forceOrientation( pp.getExteriorRing(), true ).getCurveSegments();
            // handle exterior
            if ( isSimple ) {
                buildCurveSegmentSimple( info, pnts, eseg.get( 0 ), true );
            } else {
                info.add( new Triplet( pnts.size(), SDOEType.COMPOUND_POLYGON_RING_EXTERIOR, eseg.size() ) );
                for ( int i = 0, j = eseg.size(); i < j; i++ ) {
                    buildCurveSegment( info, pnts, eseg.get( i ), i > 0 );
                }
            }

            for ( Ring rint : pp.getInteriorRings() ) {
                // handle interior
                List<CurveSegment> iseg = forceOrientation( rint, false ).getCurveSegments();

                if ( isSimple ) {
                    buildCurveSegmentSimple( info, pnts, iseg.get( 0 ), false );
                } else {
                    info.add( new Triplet( pnts.size(), SDOEType.COMPOUND_POLYGON_RING_INTERIOR, eseg.size() ) );
                    for ( int i = 0, j = iseg.size(); i < j; i++ ) {
                        buildCurveSegment( info, pnts, iseg.get( i ), i > 0 );
                    }
                }
            }
        }
    }

    private void addPnts( List<Point> pnts, Points add, boolean continueGeom ) {
        int off = 0;

        // if the last geometry is continued, do not duplicate points (last == first)
        if ( continueGeom && pnts.size() > 0 && add.size() > 0 && pnts.get( pnts.size() - 1 ).equals( add.get( 0 ) ) )
            off = 1;

        for ( int i = off, j = add.size(); i < j; i++ ) {
            pnts.add( add.get( i ) );
        }
    }

    private void buildCurveSegmentSimple( List<Triplet> info, List<Point> pnts, CurveSegment geom, boolean exterior ) {
        CurveSegmentType typ = geom.getSegmentType();
        int etype = exterior ? SDOEType.POLYGON_RING_EXTERIOR : SDOEType.POLYGON_RING_INTERIOR;

        if ( typ == CurveSegmentType.ARC_STRING || typ == CurveSegmentType.ARC ) {
            info.add( new Triplet( pnts.size(), etype, 2 ) );
            addPnts( pnts, ( (ArcString) geom ).getControlPoints(), false );
        } else if ( typ == CurveSegmentType.LINE_STRING_SEGMENT ) {
            info.add( new Triplet( pnts.size(), etype, 1 ) );
            addPnts( pnts, ( (LineStringSegment) geom ).getControlPoints(), false );
        } else if ( typ == CurveSegmentType.CIRCLE ) {
            info.add( new Triplet( pnts.size(), etype, 4 ) );
            addPnts( pnts, ( (Circle) geom ).getControlPoints(), false );
        } else {
            // should never happen
            throw new InvalidParameterValueException();
        }
    }

    private void buildCurve( List<Triplet> info, List<Point> pnts, Curve geom ) {
        List<CurveSegment> segs = geom.getCurveSegments();

        // make only compound if more than one segment is available
        if ( segs.size() > 1 ) {
            info.add( new Triplet( pnts.size(), SDOEType.COMPOUND_LINESTRING, segs.size() ) );
        }

        for ( int i = 0, j = segs.size(); i < j; i++ ) {
            CurveSegment cs = segs.get( i );
            buildCurveSegment( info, pnts, cs, i > 0 );
        }
    }

    private void buildCurveSegment( List<Triplet> info, List<Point> pnts, CurveSegment geom, boolean continuedGeom ) {
        switch ( geom.getSegmentType() ) {
        case CIRCLE:
            // multiple circles in line not mapable
            if ( ( (ArcString) geom ).getControlPoints().size() > 3 )
                throw new InvalidParameterValueException();
        case ARC:
        case ARC_STRING:
            info.add( new Triplet( pnts.size(), SDOEType.LINESTRING, 2, continuedGeom ) );
            addPnts( pnts, ( (ArcString) geom ).getControlPoints(), continuedGeom );
            break;
        case LINE_STRING_SEGMENT:
            info.add( new Triplet( pnts.size(), SDOEType.LINESTRING, 1, continuedGeom ) );
            addPnts( pnts, ( (LineStringSegment) geom ).getControlPoints(), continuedGeom );
            break;

        default:
            // unmappable
            throw new InvalidParameterValueException();
        }
    }
}
