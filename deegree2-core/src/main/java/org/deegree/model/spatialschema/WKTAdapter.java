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
package org.deegree.model.spatialschema;

import java.util.ArrayList;

import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CoordinateSystem;

/**
 * Adapter class for exporting deegree geometries to WKT and to wrap WKT code geometries to deegree geometries.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class WKTAdapter {

    /**
     *
     *
     * @param wkt
     * @param crs
     * @return the corresponding <tt>Geometry</tt>
     * @throws GeometryException
     *             if type unsupported or conversion failed
     */
    public static Geometry wrap( String wkt, CoordinateSystem crs )
                            throws GeometryException {
        if ( wkt == null || "".equals( wkt.trim() ) ) {
            throw new NullPointerException( "No Well-Known-Text (WKT) to parse geometry from." );
        }
        Geometry geo = null;

        if ( wkt.startsWith( "POINT" ) ) {
            geo = wrapPoint( wkt, crs );
        } else if ( wkt.startsWith( "LINE" ) ) {
            geo = wrapCurve( wkt, crs );
        } else if ( wkt.startsWith( "POLY" ) ) {
            geo = wrapSurface( wkt, crs );
        } else if ( wkt.startsWith( "MULTIPOINT" ) ) {
            geo = wrapMultiPoint( wkt, crs );
        } else if ( wkt.startsWith( "MULTILINE" ) ) {
            geo = wrapMultiCurve( wkt, crs );
        } else if ( wkt.startsWith( "MULTIPOLY" ) ) {
            geo = wrapMultiSurface( wkt, crs );
        } else {
            throw new GeometryException( "Not supported Well-Known-Text: " + wkt );
        }

        return geo;
    }

    /**
     * @param geom
     *            geometry
     *
     * @return the StingBuffer containing the exported geometry
     * @throws GeometryException
     */
    public static StringBuffer export( Geometry geom )
                            throws GeometryException {

        if ( geom == null ) {
            throw new NullPointerException( "The Geometry is empty, cannot create wkt for it." );
        }
        StringBuffer sb = null;
        if ( geom instanceof Point ) {
            sb = export( (Point) geom );
        } else if ( geom instanceof Curve ) {
            sb = export( (Curve) geom );
        } else if ( geom instanceof Surface ) {
            sb = export( (Surface) geom );
        } else if ( geom instanceof MultiPoint ) {
            sb = export( (MultiPoint) geom );
        } else if ( geom instanceof MultiCurve ) {
            sb = export( (MultiCurve) geom );
        } else if ( geom instanceof MultiSurface ) {
            sb = export( (MultiSurface) geom );
        } else if ( geom instanceof MultiGeometry ) {
            sb = export( (MultiGeometry) geom );
        }

        return sb;
    }

    /**
     * exports an Envelope as a BOX3D WKT string.
     *
     * @param envelope
     * @return the StringBuffer containing the exported envelope
     */
    public static StringBuffer export( Envelope envelope ) {
        if ( envelope == null ) {
            throw new NullPointerException( "The envelope is empty, cannot create wkt for it." );
        }
        StringBuffer sb = new StringBuffer( 150 );
        sb.append( "BOX3D(" );
        int dim = envelope.getMin().getCoordinateDimension();
        double[] d = envelope.getMin().getAsArray();
        for ( int i = 0; i < dim - 1; i++ ) {
            sb.append( Double.toString( d[i] ) ).append( " " );
        }
        sb.append( Double.toString( d[dim - 1] ) ).append( "," );
        d = envelope.getMax().getAsArray();
        for ( int i = 0; i < dim - 1; i++ ) {
            sb.append( Double.toString( d[i] ) ).append( " " );
        }
        sb.append( Double.toString( d[dim - 1] ) );
        sb.append( ") " );
        return sb;
    }

    /**
     * @param point
     *            point geometry
     *
     * @return the StringBuffer containing the exported point
     */
    private static StringBuffer export( Point point ) {

        StringBuffer sb = new StringBuffer( 50 );
        sb.append( "POINT(" );
        double[] points = point.getAsArray();
        int dim = point.getCoordinateDimension();
        for ( int i = 0; i < dim - 1; i++ ) {
            sb.append( points[i] ).append( ' ' );
        }
        sb.append( points[dim - 1] );
        sb.append( ") " );

        return sb;
    }

    /**
     *
     * @param cur
     *            curve geometry
     *
     * @return the StringBuffer containing the exported curve
     *
     * @throws GeometryException
     */
    private static StringBuffer export( Curve cur )
                            throws GeometryException {

        LineString ls = cur.getAsLineString();

        StringBuffer sb = new StringBuffer( ls.getNumberOfPoints() * 30 );
        sb.append( "LINESTRING(" );

        for ( int i = 0; i < ls.getNumberOfPoints() - 1; i++ ) {
            Position pos = ls.getPositionAt( i );
            double[] positions = pos.getAsArray();
            int dim = pos.getCoordinateDimension();
            for ( int j = 0; j < dim - 1; j++ ) {
                sb.append( positions[j] + " " );
            }
            sb.append( positions[dim - 1] + "," );
        }
        Position pos = ls.getPositionAt( ls.getNumberOfPoints() - 1 );
        double[] tmp = pos.getAsArray();
        int dim = pos.getCoordinateDimension();
        for ( int j = 0; j < dim - 1; j++ ) {
            sb.append( tmp[j] + " " );
        }
        sb.append( tmp[dim - 1] + ")" );

        return sb;
    }

    /**
     *
     *
     * @param sur
     *
     * @return the StringBuffer containing the exported surface
     *
     */
    private static StringBuffer export( Surface sur ) {

        SurfaceBoundary subo = sur.getSurfaceBoundary();
        Ring exter = subo.getExteriorRing();
        Ring[] inter = subo.getInteriorRings();

        StringBuffer sb = new StringBuffer( 10000 );
        sb.append( "POLYGON((" );
        // exterior ring
        Position[] pos = exter.getPositions();
        int dim = pos[0].getCoordinateDimension();
        for ( int i = 0; i < pos.length - 1; i++ ) {
            double[] positions = pos[i].getAsArray();
            for ( int j = 0; j < dim - 1; j++ ) {
                sb.append( positions[j] + " " );
            }
            sb.append( positions[dim - 1] + "," );
        }
        double[] positions = pos[pos.length - 1].getAsArray();
        for ( int j = 0; j < dim - 1; j++ ) {
            sb.append( positions[j] + " " );
        }
        sb.append( positions[dim - 1] + ")" );
        // interior rings
        if ( inter != null ) {
            for ( int j = 0; j < inter.length; j++ ) {
                sb.append( ",(" );
                pos = inter[j].getPositions();
                for ( int i = 0; i < pos.length - 1; i++ ) {
                    double[] intPos = pos[i].getAsArray();
                    for ( int l = 0; l < dim - 1; l++ ) {
                        sb.append( intPos[l] + " " );
                    }
                    sb.append( intPos[dim - 1] + "," );//
                }
                double[] intPos = pos[pos.length - 1].getAsArray();
                for ( int l = 0; l < dim - 1; l++ ) {
                    sb.append( intPos[l] + " " );
                }
                sb.append( intPos[dim - 1] + ")" );
            }
        }
        sb.append( ")" );

        return sb;
    }

    /**
     * @param mp
     * @return the StringBuffer containing the exported multipoint
     */
    private static StringBuffer export( MultiPoint mp ) {

        StringBuffer sb = new StringBuffer( mp.getSize() * 30 );
        sb.append( "MULTIPOINT(" );
        int dim = mp.getPointAt( 0 ).getCoordinateDimension();
        for ( int i = 0; i < mp.getSize() - 1; i++ ) {
            Point pt = mp.getPointAt( i );
            double[] points = pt.getAsArray();
            for ( int j = 0; j < dim - 1; j++ ) {
                sb.append( points[j] + " " );
            }
            sb.append( points[dim - 1] );
            sb.append( "," );
        }
        Point pt = mp.getPointAt( mp.getSize() - 1 );
        double[] points = pt.getAsArray();
        for ( int j = 0; j < dim - 1; j++ ) {
            sb.append( points[j] + " " );
        }
        sb.append( points[dim - 1] + ")" );

        return sb;
    }

    /**
     *
     *
     * @param mc
     *
     * @return the StringBuffer containing the exported multi curve
     *
     * @throws GeometryException
     */
    private static StringBuffer export( MultiCurve mc )
                            throws GeometryException {

        StringBuffer sb = new StringBuffer( 10000 );
        sb.append( "MULTILINESTRING(" );

        for ( int i = 0; i < mc.getSize() - 1; i++ ) {
            String s = export( mc.getCurveAt( i ) ).toString();
            s = s.substring( 10, s.length() );
            sb.append( s ).append( "," );
        }
        String s = export( mc.getCurveAt( mc.getSize() - 1 ) ).toString();
        s = s.substring( 10, s.length() );
        sb.append( s ).append( ")" );

        return sb;
    }

    /**
     *
     *
     * @param ms
     *
     * @return the StringBuffer containing the exported multi surface
     *
     */
    private static StringBuffer export( MultiSurface ms ) {

        StringBuffer sb = new StringBuffer( 10000 );
        sb.append( "MULTIPOLYGON(" );

        for ( int i = 0; i < ms.getSize() - 1; i++ ) {
            String s = export( ms.getSurfaceAt( i ) ).toString();
            s = s.substring( 7, s.length() );
            sb.append( s ).append( "," );
        }
        String s = export( ms.getSurfaceAt( ms.getSize() - 1 ) ).toString();
        s = s.substring( 7, s.length() );
        sb.append( s ).append( ")" );

        return sb;
    }

    private static StringBuffer export( MultiGeometry multi )
                            throws GeometryException {
        StringBuffer sb = new StringBuffer( 10000 );
        sb.append( "GEOMETRYCOLLECTION(" );

        for ( int i = 0; i < multi.getSize() - 1; i++ ) {
            StringBuffer memberWKT = export( multi.getObjectAt( i ) );
            sb.append( memberWKT ).append( "," );
        }

        StringBuffer memberWKT = export( multi.getObjectAt( multi.getSize() - 1 ) );
        sb.append( memberWKT ).append( ")" );
        return sb;
    }

    /**
     * creates a Point from a WKT.
     *
     * @param wkt
     *            a Point WKT
     * @param crs
     * @return the point created from the given wkt
     * @throws GeometryException
     */
    public static Point wrapPoint( String wkt, CoordinateSystem crs )
                            throws GeometryException {
        if ( wkt == null || "".equals( wkt.trim() ) ) {
            throw new NullPointerException( "No Well-Known-Text (WKT) to parse point from." );
        }
        wkt = wkt.trim();
        wkt = wkt.substring( 6, wkt.length() - 1 );
        double[] tmp = createDoubles( wkt );// StringTools.toArrayDouble( wkt, " " );
        Position pos = GeometryFactory.createPosition( tmp );
        Point point = GeometryFactory.createPoint( pos, crs );

        return point;
    }

    /**
     * creates a Curve from a WKT.
     *
     * @param wkt
     *            linestring a WKT
     * @param crs
     * @return the curve created from the given wkt.
     * @throws GeometryException
     */
    public static Curve wrapCurve( String wkt, CoordinateSystem crs )
                            throws GeometryException {
        if ( wkt == null || "".equals( wkt.trim() ) ) {
            throw new NullPointerException( "No Well-Known-Text (WKT) to parse curve from." );
        }
        wkt = wkt.trim();
        wkt = wkt.substring( 11, wkt.length() - 1 );
        String[] points = wkt.split( "," );// StringTools.toArray( wkt, ",", false );
        Position[] pos = new Position[points.length];
        for ( int i = 0; i < points.length; i++ ) {
            double[] tmp = StringTools.toArrayDouble( points[i], " " );
            pos[i] = GeometryFactory.createPosition( tmp );
        }
        Curve curve = GeometryFactory.createCurve( pos, crs );

        return curve;
    }

    /**
     * creates a Surface
     *
     * @param wkt
     *            polygon WKT
     * @param crs
     * @return the Surface created from the given wkt.
     * @throws GeometryException
     */
    public static Surface wrapSurface( String wkt, CoordinateSystem crs )
                            throws GeometryException {
        if ( wkt == null || "".equals( wkt.trim() ) ) {
            throw new NullPointerException( "No Well-Known-Text (WKT) to parse surface from." );
        }
        wkt = wkt.trim();

        Position[] ext = null;
        ArrayList<Position[]> inn = new ArrayList<Position[]>();
        if ( wkt.indexOf( "((" ) > 0 ) {
            wkt = wkt.substring( 9, wkt.length() - 1 );
            int pos = wkt.indexOf( ")" );
            String tmp = wkt.substring( 0, pos );
            // external ring
            String[] points = tmp.split( "," );// StringTools.toArray( tmp, ",", false );
            ext = new Position[points.length];
            for ( int i = 0; i < points.length; i++ ) {
                double[] temp = StringTools.toArrayDouble( points[i], " " );
                ext[i] = GeometryFactory.createPosition( temp );
            }
            if ( pos + 3 < wkt.length() ) {
                wkt = wkt.substring( pos + 3, wkt.length() );
                while ( wkt.indexOf( ")" ) > 0 ) {
                    pos = wkt.indexOf( ")" );
                    tmp = wkt.substring( 0, pos );
                    // internal ring(s)
                    points = tmp.split( "," );// StringTools.toArray( tmp, ",", false );
                    Position[] intern = new Position[points.length];
                    for ( int i = 0; i < points.length; i++ ) {
                        double[] temp = StringTools.toArrayDouble( points[i], " " );
                        intern[i] = GeometryFactory.createPosition( temp );
                    }
                    inn.add( intern );
                    if ( pos + 3 < wkt.length() ) {
                        wkt = wkt.substring( pos + 3, wkt.length() );
                    } else {
                        break;
                    }
                }
            }
        }
        Position[][] inner = null;
        if ( inn.size() > 0 ) {
            inner = inn.toArray( new Position[inn.size()][] );
        }
        Surface sur = GeometryFactory.createSurface( ext, inner, new SurfaceInterpolationImpl(), crs );

        return sur;
    }

    /**
     * creates a MultiPoint from a WKT
     *
     * @param wkt
     *            multipoint WKT
     * @param crs
     * @return the Multipoint created from the given wkt.
     */
    public static MultiPoint wrapMultiPoint( String wkt, CoordinateSystem crs ) {
        if ( wkt == null || "".equals( wkt.trim() ) ) {
            throw new NullPointerException( "No Well-Known-Text (WKT) to parse multi point from." );
        }
        wkt = wkt.trim();
        wkt = wkt.substring( 11, wkt.length() - 1 );
        String[] coords = wkt.split( "," );// StringTools.toArray( wkt, ",", false );
        Position[] pos = new Position[coords.length];
        for ( int i = 0; i < coords.length; i++ ) {
            double[] temp = StringTools.toArrayDouble( coords[i], " " );
            pos[i] = GeometryFactory.createPosition( temp );
        }

        Point[] points = new Point[pos.length];
        for ( int i = 0; i < pos.length; i++ ) {
            points[i] = GeometryFactory.createPoint( pos[i], crs );
        }
        MultiPoint mp = GeometryFactory.createMultiPoint( points );

        return mp;
    }

    /**
     * creates a MultiCurve from a WKT
     *
     * @param wkt
     *            a WKT
     * @param crs
     * @return the multi curve created from the given wkt.
     * @throws GeometryException
     */
    public static MultiCurve wrapMultiCurve( String wkt, CoordinateSystem crs )
                            throws GeometryException {
        if ( wkt == null || "".equals( wkt.trim() ) ) {
            throw new NullPointerException( "No Well-Known-Text (WKT) to parse multi-curve from." );
        }
        ArrayList<Curve> crvs = new ArrayList<Curve>();

        wkt = wkt.trim();
        int pos = wkt.indexOf( ")" );
        String tmp = wkt.substring( 17, pos );
        String[] coords = tmp.split( "," );// StringTools.toArray( tmp, ",", false );
        Position[] posi = new Position[coords.length];
        for ( int i = 0; i < coords.length; i++ ) {
            double[] temp = StringTools.toArrayDouble( coords[i], " " );
            posi[i] = GeometryFactory.createPosition( temp );
        }
        crvs.add( GeometryFactory.createCurve( posi, crs ) );
        wkt = wkt.substring( pos + 3, wkt.length() - 1 );
        while ( wkt.indexOf( ")" ) > 0 ) {
            Position[] posi2 = new Position[coords.length];
            pos = wkt.indexOf( ")" );
            tmp = wkt.substring( 0, pos );
            coords = tmp.split( "," );// StringTools.toArray( tmp, ",", false );
            for ( int i = 0; i < coords.length; i++ ) {
                double[] temp = StringTools.toArrayDouble( coords[i], " " );
                posi2[i] = GeometryFactory.createPosition( temp );
            }
            crvs.add( GeometryFactory.createCurve( posi2, crs ) );
            if ( pos + 3 < wkt.length() ) {
                wkt = wkt.substring( pos + 3, wkt.length() );
            } else {
                break;
            }
        }

        Curve[] curves = crvs.toArray( new Curve[crvs.size()] );
        MultiCurve mc = GeometryFactory.createMultiCurve( curves );

        return mc;
    }

    /**
     * creates a MultiSurface from a WKT
     *
     * @param wkt
     *            a WKT
     * @param crs
     * @return the multi surface created from the given wkt.
     * @throws GeometryException
     */
    public static MultiSurface wrapMultiSurface( String wkt, CoordinateSystem crs )
                            throws GeometryException {
        if ( wkt == null || "".equals( wkt.trim() ) ) {
            throw new NullPointerException( "No Well-Known-Text (WKT) to parse multi-surface from." );
        }
        ArrayList<Surface> srfcs = new ArrayList<Surface>();

        wkt = wkt.substring( 13 );
        // for each polygon
        while ( wkt.indexOf( "((" ) > -1 ) {
            Position[] ext = null;
            ArrayList<Position[]> inn = new ArrayList<Position[]>();
            int pos1 = wkt.indexOf( "))" );
            String tmp = wkt.substring( 2, pos1 + 1 );
            // exterior ring
            int pos = tmp.indexOf( ")" );
            String tmp2 = tmp.substring( 0, pos );
            String[] points = tmp2.split( "," );// StringTools.toArray( tmp2, ",", false );
            ext = new Position[points.length];
            for ( int i = 0; i < points.length; i++ ) {
                double[] temp = StringTools.toArrayDouble( points[i], " " );
                ext[i] = GeometryFactory.createPosition( temp );
            }
            if ( pos + 3 < tmp.length() ) {
                tmp = tmp.substring( pos + 3, tmp.length() );
                // for each inner ring
                while ( tmp.indexOf( ")" ) > 0 ) {
                    pos = tmp.indexOf( ")" );
                    tmp2 = tmp.substring( 0, pos );
                    points = tmp2.split( "," );// StringTools.toArray( tmp2, ",", false );
                    Position[] intern = new Position[points.length];
                    for ( int i = 0; i < points.length; i++ ) {
                        double[] temp = StringTools.toArrayDouble( points[i], " " );
                        intern[i] = GeometryFactory.createPosition( temp );
                    }
                    inn.add( intern );
                    if ( pos + 3 < tmp.length() ) {
                        tmp = tmp.substring( pos + 3, tmp.length() );
                    } else {
                        break;
                    }
                }
            }
            Position[][] inner = null;
            if ( inn.size() > 0 ) {
                inner = inn.toArray( new Position[inn.size()][] );
            }
            Surface sur = GeometryFactory.createSurface( ext, inner, new SurfaceInterpolationImpl(), crs );
            srfcs.add( sur );
            wkt = wkt.substring( pos1 + 3 );
        }
        Surface[] surfaces = srfcs.toArray( new Surface[srfcs.size()] );
        MultiSurface ms = GeometryFactory.createMultiSurface( surfaces );

        return ms;
    }

    private static double[] createDoubles( String toSeparate )
                            throws GeometryException {
        if ( toSeparate == null || "".equals( toSeparate.trim() ) ) {
            throw new NullPointerException( "The given wkt geometry does not contain coordinates." );
        }
        String delimiter = " ";
        String[] doubs = toSeparate.split( delimiter );
        int cnt = 0;
        for ( int i = 0; i < doubs.length; i++ ) {
            if ( doubs[i] != null && !"".equals( doubs[i] ) ) {
                cnt++;
            }
        }
        double[] result = null;
        if ( doubs != null ) {
            result = new double[cnt];
            cnt = 0;
            for ( int i = 0; i < doubs.length; ++i ) {
                String d = doubs[i];
                if ( d != null && !"".equals( d ) ) {
                    try {
                        result[cnt++] = Double.valueOf( d );
                    } catch ( NumberFormatException e ) {
                        throw new GeometryException( "The given WKT is not valid at position: " + i + ". Message: "
                                                     + e.getLocalizedMessage() );
                    }
                }
            }
        } else {
            throw new NullPointerException( "The given wkt geometry does not contain coordinates." );
        }

        return result;
    }
}
