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
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CoordinateSystem;

/**
 * Factory to create geometry instances.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$, $Date$
 * 
 */
public final class GeometryFactory {

    private GeometryFactory() {
        // Hidden default constructor.
    }

    /**
     * creates a Envelope object out from two corner coordinates
     * 
     * @param minx
     *            lower x-axis coordinate
     * @param miny
     *            lower y-axis coordinate
     * @param maxx
     *            upper x-axis coordinate
     * @param maxy
     *            upper y-axis coordinate
     * @param crs
     *            The coordinate system
     * @return an Envelope with given parameters
     */
    public static Envelope createEnvelope( double minx, double miny, double maxx, double maxy, CoordinateSystem crs ) {
        Position min = createPosition( minx, miny );
        Position max = createPosition( maxx, maxy );
        return new EnvelopeImpl( min, max, crs );
    }

    /**
     * creates a Envelope object out from two corner coordinates
     * 
     * @param min
     *            lower point
     * @param max
     *            upper point
     * @param crs
     *            The coordinate system
     * @return an Envelope with given parameters
     */
    public static Envelope createEnvelope( Position min, Position max, CoordinateSystem crs ) {
        return new EnvelopeImpl( min, max, crs );
    }

    /**
     * creates an Envelope from a comma seperated String; e.g.: 10,34,15,48
     * 
     * @param bbox
     *            the boundingbox of the created Envelope
     * @param crs
     *            The coordinate system
     * @return an Envelope with given parameters
     */
    public static Envelope createEnvelope( String bbox, CoordinateSystem crs ) {
        double[] d = StringTools.toArrayDouble( bbox, ",;" );
        return createEnvelope( d[0], d[1], d[2], d[3], crs );
    }

    /**
     * creates a Position from two coordinates.
     * 
     * @param x
     *            coordinate on the x-axis
     * @param y
     *            coordinate on the y-axis
     * @return a Position defining position x, y
     */
    public static Position createPosition( double x, double y ) {
        return new PositionImpl( x, y );
    }

    /**
     * creates a Position from three coordinates.
     * 
     * @param x
     *            coordinate on the x-axis
     * @param y
     *            coordinate on the y-axis
     * @param z
     *            coordinate on the z-axis
     * @return a Position defining position x, y, z
     */
    public static Position createPosition( double x, double y, double z ) {
        return new PositionImpl( new double[] { x, y, z } );
    }

    /**
     * creates a Position from a point3d.
     * 
     * @param coordinates
     *            the coordinates to create the position from.
     * @return a Position defining position x, y, z
     */
    public static Position createPosition( Point3d coordinates ) {
        return new PositionImpl( coordinates );
    }

    /**
     * creates a Position from an array of double.
     * 
     * @param p
     *            list of points
     * @return the Position defined by the array.
     */
    public static Position createPosition( double[] p ) {
        return new PositionImpl( p );
    }

    /**
     * creates a Point from two coordinates.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param crs
     *            spatial reference system of the point geometry
     * @return a Position defining position x, y in the given CRS
     */
    public static Point createPoint( double x, double y, CoordinateSystem crs ) {
        return new PointImpl( x, y, crs );
    }

    /**
     * creates a Point from two coordinates.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @param z
     *            coordinate on the z-axis
     * @param crs
     *            spatial reference system of the point geometry
     * @return a Position defining position x, y, z in the given CRS
     */
    public static Point createPoint( double x, double y, double z, CoordinateSystem crs ) {
        return new PointImpl( x, y, z, crs );
    }

    /**
     * creates a Point from a position.
     * 
     * @param position
     *            position
     * @param crs
     *            spatial reference system of the point geometry
     * @return the Position defined by the array in the given CRS
     */
    public static Point createPoint( Position position, CoordinateSystem crs ) {
        return new PointImpl( position, crs );
    }

    /**
     * creates a Point from a wkb.
     * 
     * @param wkb
     *            geometry in Well-Known Binary (WKB) format
     * @param srs
     *            spatial reference system of the geometry
     * @return the Position defined by the WKB and the given CRS
     * @throws GeometryException
     *             if the wkb is not known or invalid
     */
    public static Point createPoint( byte[] wkb, CoordinateSystem srs )
                            throws GeometryException {
        int wkbType = -1;
        double x = 0;
        double y = 0;

        byte byteorder = wkb[0];

        if ( byteorder == 0 ) {
            wkbType = ByteUtils.readBEInt( wkb, 1 );
        } else {
            wkbType = ByteUtils.readLEInt( wkb, 1 );
        }

        if ( wkbType != 1 ) {
            throw new GeometryException( "invalid byte stream" );
        }

        if ( byteorder == 0 ) {
            x = ByteUtils.readBEDouble( wkb, 5 );
            y = ByteUtils.readBEDouble( wkb, 13 );
        } else {
            x = ByteUtils.readLEDouble( wkb, 5 );
            y = ByteUtils.readLEDouble( wkb, 13 );
        }

        return new PointImpl( x, y, srs );
    }

    /**
     * creates a CurveSegment from an array of points.
     * 
     * @param points
     *            array of Point
     * @param crs
     *            CS_CoordinateSystem spatial reference system of the curve
     * @return A curve defined by the given Points in the CRS.
     * @throws GeometryException
     *             if the point array is empty
     */
    public static CurveSegment createCurveSegment( Position[] points, CoordinateSystem crs )
                            throws GeometryException {
        return new LineStringImpl( points, crs );
    }

    /**
     * @param points
     * @param crs
     * @return a new curve segment
     * @throws GeometryException
     */
    public static CurveSegment createCurveSegment( List<Position> points, CoordinateSystem crs )
                            throws GeometryException {
        return new LineStringImpl( points.toArray( new Position[points.size()] ), crs );
    }

    /**
     * creates a Curve from an array of Positions.
     * 
     * @param positions
     *            positions
     * @param crs
     *            spatial reference system of the geometry
     * @return A curve defined by the given Points in the CRS.
     * @throws GeometryException
     *             if the point array is empty
     */
    public static Curve createCurve( Position[] positions, CoordinateSystem crs )
                            throws GeometryException {
        CurveSegment[] cs = new CurveSegment[1];
        cs[0] = createCurveSegment( positions, crs );
        return new CurveImpl( cs );
    }

    /**
     * creates a Curve from one curve segment.
     * 
     * @param segment
     *            CurveSegments
     * @return a new CurveSegment
     * @throws GeometryException
     *             if the segment is null
     */
    public static Curve createCurve( CurveSegment segment )
                            throws GeometryException {
        return new CurveImpl( new CurveSegment[] { segment } );
    }

    /**
     * creates a Curve from an array of curve segments.
     * 
     * @param segments
     *            array of CurveSegments
     * @return a new CurveSegment
     * @throws GeometryException
     *             if the segment is null or has no values
     * 
     */
    public static Curve createCurve( CurveSegment[] segments )
                            throws GeometryException {
        return new CurveImpl( segments );
    }

    /**
     * creates a Curve from an array of curve segments.
     * 
     * @param segments
     *            array of CurveSegments
     * @param crs
     * @return a new CurveSegment
     * @throws GeometryException
     *             if the segment is null or has no values
     * 
     */
    public static Curve createCurve( CurveSegment[] segments, CoordinateSystem crs )
                            throws GeometryException {
        return new CurveImpl( segments, crs );
    }

    /**
     * @param segments
     * @param crs
     * @return a new curve
     * @throws GeometryException
     */
    public static Curve createCurve( List<CurveSegment> segments, CoordinateSystem crs )
                            throws GeometryException {
        return new CurveImpl( segments.toArray( new CurveSegment[segments.size()] ), crs );
    }

    /**
     * creates a GM_Curve from an array of ordinates
     * 
     * TODO: If resources are available, think about good programming style.
     * 
     * @param ord
     *            the ordinates
     * @param dim
     *            the dimension of the ordinates
     * @param crs
     *            the spatial reference system of the geometry
     * 
     * @return the Curve defined by the given parameters
     * @throws GeometryException
     *             if the ord array is empty
     */
    public static Curve createCurve( double[] ord, int dim, CoordinateSystem crs )
                            throws GeometryException {
        Position[] pos = new Position[ord.length / dim];
        int i = 0;
        while ( i < ord.length ) {
            double[] o = new double[dim];
            for ( int j = 0; j < dim; j++ ) {
                o[j] = ord[i++];
            }
            pos[i / dim - 1] = GeometryFactory.createPosition( o );
        }
        return GeometryFactory.createCurve( pos, crs );
    }

    /**
     * creates a SurfacePatch from array(s) of Position
     * 
     * @param exteriorRing
     *            exterior ring of the patch
     * @param interiorRings
     *            interior rings of the patch
     * @param si
     *            SurfaceInterpolation
     * @param crs
     *            CS_CoordinateSystem spatial reference system of the surface patch
     * @return a Surfacepatch defined by the given Parameters
     * @throws GeometryException
     */
    public static SurfacePatch createSurfacePatch( Position[] exteriorRing, Position[][] interiorRings,
                                                   SurfaceInterpolation si, CoordinateSystem crs )
                            throws GeometryException {
        return new PolygonImpl( si, exteriorRing, interiorRings, crs );
    }

    /**
     * 
     * @param exteriorRing
     * @param interiorRings
     * @param crs
     * @return the surface path create from the given parameters.
     * @throws GeometryException
     */
    public static SurfacePatch createSurfacePatch( CurveSegment[] exteriorRing, CurveSegment[][] interiorRings,
                                                   CoordinateSystem crs )
                            throws GeometryException {
        Ring eRing = new RingImpl( exteriorRing, crs, '+' );
        Ring[] iRings = null;
        if ( interiorRings != null ) {
            iRings = new Ring[interiorRings.length];
            for ( int i = 0; i < iRings.length; i++ ) {
                iRings[i] = new RingImpl( interiorRings[i], crs, '+' );
            }
        }
        return new PolygonImpl( eRing, iRings, crs );
    }

    /**
     * 
     * @param exteriorRing
     * @param interiorRings
     * @param crs
     * @return the surfacepatch created from the given parameters.
     * @throws GeometryException
     */
    public static SurfacePatch createSurfacePatch( Curve exteriorRing, Curve[] interiorRings, CoordinateSystem crs )
                            throws GeometryException {
        CurveSegment[] e = exteriorRing.getCurveSegments();
        CurveSegment[][] i = null;
        if ( interiorRings != null ) {
            i = new CurveSegment[interiorRings.length][];
            for ( int j = 0; j < i.length; j++ ) {
                i[j] = interiorRings[j].getCurveSegments();
            }
        }
        return createSurfacePatch( e, i, crs );
    }

   
    /**
     * 
     * @param centerX x coordinate of the center of the ellipse the arc is part of 
     * @param centerY y coordinate of the center of the ellipse the arc is part of
     * @param radiusX radius in x-direction of the ellipse the arc is part of
     * @param radiusY radius in y-direction of the ellipse the arc is part of
     * @param nSeg number of segments 
     * @param start start angle of the arc
     * @param end end angle of the arc
     * @param crs
     * @return a {@link Curve} representing an arc
     * @throws GeometryException
     */
    public static Curve createCurveAsArc( double centerX, double centerY, double radiusX, double radiusY, int nSeg,
                                          double start, double end, CoordinateSystem crs )
                            throws GeometryException {
        double x;
        double _x = 0;
        double _y = 0;
        double y = 0;

        List<Position> list = new ArrayList<Position>( nSeg );
        double arc = start;
        double dS = ( end - start ) / (double) nSeg;
        double k = 360 / ( end - start );
        int j = 0;
        arc -= dS;
        while ( arc <= end+dS ) {            
            double d = ( ( arc - start ) / dS ) / k + ( start / dS ) / k;
            x = radiusX * Math.sin( ( d / (double) nSeg ) * ( Math.PI * 2.0 ) );
            y = radiusY * Math.cos( ( d / (double) nSeg ) * ( Math.PI * 2.0 ) );
            if ( j > 1 ) {
                list.add( GeometryFactory.createPosition( centerX + _x, centerY + -_y ) );
            }
            // Save the actual point coordinate to link it with the next one
            _x = x;
            _y = y;
            j++;
            arc += dS;
        }
        return createCurve( list.toArray( new Position[list.size()] ), crs );
    }

    /**
     * creates a Curve from a wkb.
     * 
     * @param wkb
     *            byte stream that contains the wkb information
     * @param crs
     *            CS_CoordinateSystem spatial reference system of the curve
     * @return the Curve defined by the WKB and the given CRS
     * @throws GeometryException
     *             if the wkb is not known or invalid
     * 
     */
    public static Curve createCurve( byte[] wkb, CoordinateSystem crs )
                            throws GeometryException {
        int wkbType = -1;
        int numPoints = -1;
        Position[] points = null;
        double x = 0;
        double y = 0;

        byte byteorder = wkb[0];

        if ( byteorder == 0 ) {
            wkbType = ByteUtils.readBEInt( wkb, 1 );
        } else {
            wkbType = ByteUtils.readLEInt( wkb, 1 );
        }

        // check if it's realy a linestrin/curve
        if ( wkbType != 2 ) {
            throw new GeometryException( "invalid byte stream for Curve" );
        }

        // read number of points
        if ( byteorder == 0 ) {
            numPoints = ByteUtils.readBEInt( wkb, 5 );
        } else {
            numPoints = ByteUtils.readLEInt( wkb, 5 );
        }

        int offset = 9;

        points = new Position[numPoints];

        // read the i-th point depending on the byteorde
        if ( byteorder == 0 ) {
            for ( int i = 0; i < numPoints; i++ ) {
                x = ByteUtils.readBEDouble( wkb, offset );
                offset += 8;
                y = ByteUtils.readBEDouble( wkb, offset );
                offset += 8;
                points[i] = new PositionImpl( x, y );
            }
        } else {
            for ( int i = 0; i < numPoints; i++ ) {
                x = ByteUtils.readLEDouble( wkb, offset );
                offset += 8;
                y = ByteUtils.readLEDouble( wkb, offset );
                offset += 8;
                points[i] = new PositionImpl( x, y );
            }
        }

        CurveSegment[] segment = new CurveSegment[1];

        segment[0] = createCurveSegment( points, crs );

        return createCurve( segment );
    }

    /**
     * creates a surface in form of an ellipse. If <code>radiusX</code> == <code>radiusY</code> a circle will be created
     * 
     * @param centerX
     * @param centerY
     * @param radiusX
     * @param radiusY
     * @param nSeg
     *            number of segments the ellipse will have
     * @param crs
     * @throws GeometryException
     */
    public static Surface createSurfaceAsEllipse( double centerX, double centerY, double radiusX, double radiusY,
                                                  int nSeg, CoordinateSystem crs )
                            throws GeometryException {
        double x;
        double _x = 0;
        double _y = 0;
        double y = 0;
        double __x = 0;
        double __y = 0;

        List<Position> list = new ArrayList<Position>();
        for ( int i = 0; i < nSeg; i++ ) {
            x = radiusX * Math.sin( ( (double) i / (double) nSeg ) * ( Math.PI * 2.0 ) );
            y = radiusY * Math.cos( ( (double) i / (double) nSeg ) * ( Math.PI * 2.0 ) );
            if ( i > 0 ) {
                list.add( GeometryFactory.createPosition( centerX + _x, centerY + -_y ) );
            } else {
                // Save the first point coordinate to link it with the last one
                __x = x;
                __y = y;
            }
            // Save the actual point coordinate to link it with the next one
            _x = x;
            _y = y;
        }
        list.add( GeometryFactory.createPosition( centerX + _x, centerY + -y ) );
        list.add( GeometryFactory.createPosition( centerX + __x, centerY + -__y ) );
        return createSurface( list.toArray( new Position[list.size()] ), null, new SurfaceInterpolationImpl(), crs );
    }

    /**
     * creates a Surface composed of one SurfacePatch from array(s) of Position
     * 
     * @param exteriorRing
     *            exterior ring of the patch
     * @param interiorRings
     *            interior rings of the patch
     * @param si
     *            SurfaceInterpolation
     * @param crs
     *            CS_CoordinateSystem spatial reference system of the surface patch
     * @return a Surface composed of one SurfacePatch from array(s) of Position
     * @throws GeometryException
     *             if the implicite orientation is not '+' or '-', or the rings aren't closed
     */
    public static Surface createSurface( Position[] exteriorRing, Position[][] interiorRings, SurfaceInterpolation si,
                                         CoordinateSystem crs )
                            throws GeometryException {
        SurfacePatch sp = new PolygonImpl( si, exteriorRing, interiorRings, crs );
        return createSurface( sp );
    }

    /**
     * creates a Surface from an array of SurfacePatch.
     * 
     * @param patch
     *            patches that build the surface
     * @return a Surface from an array of SurfacePatch.
     * @throws GeometryException
     *             if implicite the orientation is not '+' or '-'
     */
    public static Surface createSurface( SurfacePatch patch )
                            throws GeometryException {
        return new SurfaceImpl( patch );
    }

    /**
     * creates a Surface from an array of SurfacePatch.
     * 
     * @param patches
     *            patches that build the surface
     * 
     * @return a Surface from an array of SurfacePatch.
     * @throws GeometryException
     *             if implicite the orientation is not '+' or '-'
     */
    public static Surface createSurface( SurfacePatch[] patches )
                            throws GeometryException {
        return new SurfaceImpl( patches );
    }

    /**
     * creates a Surface from an array of SurfacePatch.
     * 
     * @param patches
     *            patches that build the surface
     * @param crs
     * 
     * @return a Surface from an array of SurfacePatch.
     * @throws GeometryException
     *             if implicite the orientation is not '+' or '-'
     */
    public static Surface createSurface( SurfacePatch[] patches, CoordinateSystem crs )
                            throws GeometryException {
        return new SurfaceImpl( patches, crs );
    }

    /**
     * creates a Surface from a wkb.
     * 
     * @param wkb
     *            byte stream that contains the wkb information
     * @param crs
     *            CS_CoordinateSystem spatial reference system of the curve
     * @param si
     *            SurfaceInterpolation
     * @return a Surface from a wkb.
     * @throws GeometryException
     *             if the implicite orientation is not '+' or '-' or the wkb is not known or invalid
     */
    public static Surface createSurface( byte[] wkb, CoordinateSystem crs, SurfaceInterpolation si )
                            throws GeometryException {
        int wkbtype = -1;
        int numRings = 0;
        int numPoints = 0;
        int offset = 0;
        double x = 0;
        double y = 0;

        Position[] externalBoundary = null;
        Position[][] internalBoundaries = null;

        byte byteorder = wkb[offset++];

        if ( byteorder == 0 ) {
            wkbtype = ByteUtils.readBEInt( wkb, offset );
        } else {
            wkbtype = ByteUtils.readLEInt( wkb, offset );
        }

        offset += 4;

        if ( wkbtype == 6 ) {
            return null;
        }

        // is the geometry respresented by wkb a polygon?
        if ( wkbtype != 3 ) {
            throw new GeometryException( "invalid byte stream for Surface " + wkbtype );
        }

        // read number of rings of the polygon
        if ( byteorder == 0 ) {
            numRings = ByteUtils.readBEInt( wkb, offset );
        } else {
            numRings = ByteUtils.readLEInt( wkb, offset );
        }

        offset += 4;

        // read number of points of the external ring
        if ( byteorder == 0 ) {
            numPoints = ByteUtils.readBEInt( wkb, offset );
        } else {
            numPoints = ByteUtils.readLEInt( wkb, offset );
        }

        offset += 4;

        // allocate memory for the external boundary
        externalBoundary = new Position[numPoints];

        if ( byteorder == 0 ) {
            // read points of the external boundary from the byte[]
            for ( int i = 0; i < numPoints; i++ ) {
                x = ByteUtils.readBEDouble( wkb, offset );
                offset += 8;
                y = ByteUtils.readBEDouble( wkb, offset );
                offset += 8;
                externalBoundary[i] = new PositionImpl( x, y );
            }
        } else {
            // read points of the external boundary from the byte[]
            for ( int i = 0; i < numPoints; i++ ) {
                x = ByteUtils.readLEDouble( wkb, offset );
                offset += 8;
                y = ByteUtils.readLEDouble( wkb, offset );
                offset += 8;
                externalBoundary[i] = new PositionImpl( x, y );
            }
        }

        // only if numRings is larger then one there internal rings
        if ( numRings > 1 ) {
            internalBoundaries = new Position[numRings - 1][];
        }

        if ( byteorder == 0 ) {
            for ( int j = 1; j < numRings; j++ ) {
                // read number of points of the j-th internal ring
                numPoints = ByteUtils.readBEInt( wkb, offset );
                offset += 4;

                // allocate memory for the j-th internal boundary
                internalBoundaries[j - 1] = new Position[numPoints];

                // read points of the external boundary from the byte[]
                for ( int i = 0; i < numPoints; i++ ) {
                    x = ByteUtils.readBEDouble( wkb, offset );
                    offset += 8;
                    y = ByteUtils.readBEDouble( wkb, offset );
                    offset += 8;
                    internalBoundaries[j - 1][i] = new PositionImpl( x, y );
                }
            }
        } else {
            for ( int j = 1; j < numRings; j++ ) {
                // read number of points of the j-th internal ring
                numPoints = ByteUtils.readLEInt( wkb, offset );
                offset += 4;

                // allocate memory for the j-th internal boundary
                internalBoundaries[j - 1] = new Position[numPoints];

                // read points of the external boundary from the byte[]
                for ( int i = 0; i < numPoints; i++ ) {
                    x = ByteUtils.readLEDouble( wkb, offset );
                    offset += 8;
                    y = ByteUtils.readLEDouble( wkb, offset );
                    offset += 8;
                    internalBoundaries[j - 1][i] = new PositionImpl( x, y );
                }
            }
        }

        SurfacePatch patch = createSurfacePatch( externalBoundary, internalBoundaries, si, crs );

        return createSurface( patch );
    }

    /**
     * Creates a <tt>Surface</tt> from a <tt>Envelope</tt>.
     * <p>
     * 
     * @param bbox
     *            envelope to be converted
     * @param crs
     *            spatial reference system of the surface
     * @return corresponding surface
     * 
     * @throws GeometryException
     *             if the implicite orientation is not '+' or '-'
     */
    public static Surface createSurface( Envelope bbox, CoordinateSystem crs )
                            throws GeometryException {

        Position min = bbox.getMin();
        Position max = bbox.getMax();
        Position[] exteriorRing = null;
        if ( min.getCoordinateDimension() == 2 ) {
            exteriorRing = new Position[] { min, new PositionImpl( min.getX(), max.getY() ), max,
                                           new PositionImpl( max.getX(), min.getY() ), min };
        } else {
            exteriorRing = new Position[] {
                                           min,
                                           new PositionImpl( min.getX(), max.getY(),
                                                             min.getZ() + ( ( max.getZ() - min.getZ() ) * 0.5 ) ),
                                           max,
                                           new PositionImpl( max.getX(), min.getY(),
                                                             min.getZ() + ( ( max.getZ() - min.getZ() ) * 0.5 ) ), min };
        }

        return createSurface( exteriorRing, null, new SurfaceInterpolationImpl(), crs );
    }

    /**
     * Creates a <tt>GM_Surface</tt> from the ordinates of the exterior ring and the the interior rings
     * <p>
     * 
     * @param exterior
     *            ring
     * @param interior
     *            ring
     * @param dim
     *            of the surface
     * @param crs
     *            spatial reference system of the surface
     * @return corresponding surface
     * @throws GeometryException
     *             if the implicite orientation is not '+' or '-'
     * 
     */
    public static Surface createSurface( double[] exterior, double[][] interior, int dim, CoordinateSystem crs )
                            throws GeometryException {

        // get exterior ring
        Position[] ext = new Position[exterior.length / dim];
        int i = 0;
        int k = 0;
        while ( i < exterior.length - 1 ) {
            double[] o = new double[dim];
            for ( int j = 0; j < dim; j++ ) {
                o[j] = exterior[i++];
            }
            ext[k++] = GeometryFactory.createPosition( o );
        }

        // get interior rings if available
        Position[][] in = null;
        if ( interior != null && interior.length > 0 ) {
            in = new Position[interior.length][];
            for ( int j = 0; j < in.length; j++ ) {
                in[j] = new Position[interior[j].length / dim];
                i = 0;
                while ( i < interior[j].length ) {
                    double[] o = new double[dim];
                    for ( int z = 0; z < dim; z++ ) {
                        o[z] = interior[j][i++];
                    }
                    in[j][i / dim - 1] = GeometryFactory.createPosition( o );
                }
            }
        }

        // default - linear - interpolation
        SurfaceInterpolation si = new SurfaceInterpolationImpl();
        return GeometryFactory.createSurface( ext, in, si, crs );
    }

    /**
     * Creates a {@link MultiGeometry} from an array of {@link Geometry} objects.
     * 
     * @param members
     *            member geometries
     * @param crs
     *            coordinate system
     * @return {@link MultiGeometry} that contains all given members
     */
    public static MultiGeometry createMultiGeometry( Geometry[] members, CoordinateSystem crs ) {
        return new MultiGeometryImpl( members, crs );
    }

    /**
     * Creates a {@link MultiGeometry} from an array of {@link Geometry} objects.
     * 
     * @param wkb
     *            wkb information
     * @param crs
     *            coordinate system
     * @return {@link MultiGeometry} that contains all given members
     * @throws GeometryException
     *             if the wkb is not known or invalid
     */
    public static MultiGeometry createMultiGeometry( byte[] wkb, CoordinateSystem crs )
                            throws GeometryException {
        throw new GeometryException( "Generation of MultiGeometry instances from WKB is not implemented yet." );
    }

    /**
     * creates a MultiPoint from an array of Point.
     * 
     * @param points
     *            array of Points
     * @return a MultiPoint from an array of Point.
     * 
     */
    public static MultiPoint createMultiPoint( Point[] points ) {
        return new MultiPointImpl( points );
    }

    /**
     * creates a MultiPoint from an array of Point.
     * 
     * @param points
     *            array of Points
     * @param crs
     * @return a MultiPoint from an array of Point.
     */
    public static MultiPoint createMultiPoint( Point[] points, CoordinateSystem crs ) {
        return new MultiPointImpl( points, crs );
    }

    /**
     * creates a MultiPoint from a wkb.
     * 
     * @param wkb
     *            byte stream that contains the wkb information
     * @param crs
     *            CS_CoordinateSystem spatial reference system of the curve
     * @return the MultiPoint defined by the WKB and the given CRS
     * @throws GeometryException
     *             if the wkb is not known or invalid
     * 
     */
    public static MultiPoint createMultiPoint( byte[] wkb, CoordinateSystem crs )
                            throws GeometryException {
        Point[] points = null;
        int wkbType = -1;
        int numPoints = -1;
        double x = 0;
        double y = 0;
        byte byteorder = wkb[0];

        // read wkbType
        if ( byteorder == 0 ) {
            wkbType = ByteUtils.readBEInt( wkb, 1 );
        } else {
            wkbType = ByteUtils.readLEInt( wkb, 1 );
        }

        // if the geometry isn't a multipoint throw exception
        if ( wkbType != 4 ) {
            throw new GeometryException( "Invalid byte stream for MultiPoint" );
        }

        // read number of points
        if ( byteorder == 0 ) {
            numPoints = ByteUtils.readBEInt( wkb, 5 );
        } else {
            numPoints = ByteUtils.readLEInt( wkb, 5 );
        }

        points = new Point[numPoints];

        int offset = 9;

        Object[] o = new Object[3];
        o[2] = crs;

        // read all points
        for ( int i = 0; i < numPoints; i++ ) {
            // byteorder of the i-th point
            byteorder = wkb[offset];

            // wkbType of the i-th geometry
            if ( byteorder == 0 ) {
                wkbType = ByteUtils.readBEInt( wkb, offset + 1 );
            } else {
                wkbType = ByteUtils.readLEInt( wkb, offset + 1 );
            }

            // if the geometry isn't a point throw exception
            if ( wkbType != 1 ) {
                throw new GeometryException( "Invalid byte stream for Point as " + "part of a multi point" );
            }

            // read the i-th point depending on the byteorde
            if ( byteorder == 0 ) {
                x = ByteUtils.readBEDouble( wkb, offset + 5 );
                y = ByteUtils.readBEDouble( wkb, offset + 13 );
            } else {
                x = ByteUtils.readLEDouble( wkb, offset + 5 );
                y = ByteUtils.readLEDouble( wkb, offset + 13 );
            }

            offset += 21;

            points[i] = new PointImpl( x, y, crs );
        }

        return createMultiPoint( points );
    }

    /**
     * creates a MultiCurve from an array of Curves.
     * 
     * @param curves
     * @return a MultiCurve from an array of Curves.
     */
    public static MultiCurve createMultiCurve( Curve[] curves ) {
        return new MultiCurveImpl( curves );
    }

    /**
     * creates a MultiCurve from an array of Curves.
     * 
     * @param curves
     * @param crs
     * @return a MultiCurve from an array of Curves.
     */
    public static MultiCurve createMultiCurve( Curve[] curves, CoordinateSystem crs ) {
        return new MultiCurveImpl( curves, crs );
    }

    /**
     * creates a MultiCurve from a wkb.
     * 
     * @param wkb
     *            byte stream that contains the wkb information
     * @param crs
     *            CS_CoordinateSystem spatial reference system of the curve
     * @return the MultiCurve defined by the WKB and the given CRS
     * @throws GeometryException
     *             if the wkb is not known or invalid
     */
    public static MultiCurve createMultiCurve( byte[] wkb, CoordinateSystem crs )
                            throws GeometryException {
        int wkbType = -1;
        int numPoints = -1;
        int numParts = -1;
        double x = 0;
        double y = 0;
        Position[][] points = null;
        int offset = 0;
        byte byteorder = wkb[offset++];

        if ( byteorder == 0 ) {
            wkbType = ByteUtils.readBEInt( wkb, offset );
        } else {
            wkbType = ByteUtils.readLEInt( wkb, offset );
        }

        offset += 4;

        // check if it's realy a linestring
        if ( wkbType != 5 ) {
            throw new GeometryException( "Invalid byte stream for MultiCurve" );
        }

        // read number of linestrings
        if ( byteorder == 0 ) {
            numParts = ByteUtils.readBEInt( wkb, offset );
        } else {
            numParts = ByteUtils.readLEInt( wkb, offset );
        }

        offset += 4;

        points = new Position[numParts][];

        // for every linestring
        for ( int j = 0; j < numParts; j++ ) {
            byteorder = wkb[offset++];

            if ( byteorder == 0 ) {
                wkbType = ByteUtils.readBEInt( wkb, offset );
            } else {
                wkbType = ByteUtils.readLEInt( wkb, offset );
            }

            offset += 4;

            // check if it's realy a linestring
            if ( wkbType != 2 ) {
                throw new GeometryException( "Invalid byte stream for Curve as " + " part of a MultiCurve." );
            }

            // read number of points
            if ( byteorder == 0 ) {
                numPoints = ByteUtils.readBEInt( wkb, offset );
            } else {
                numPoints = ByteUtils.readLEInt( wkb, offset );
            }

            offset += 4;

            points[j] = new Position[numPoints];

            // read the i-th point depending on the byteorde
            if ( byteorder == 0 ) {
                for ( int i = 0; i < numPoints; i++ ) {
                    x = ByteUtils.readBEDouble( wkb, offset );
                    offset += 8;
                    y = ByteUtils.readBEDouble( wkb, offset );
                    offset += 8;
                    points[j][i] = new PositionImpl( x, y );
                }
            } else {
                for ( int i = 0; i < numPoints; i++ ) {
                    x = ByteUtils.readLEDouble( wkb, offset );
                    offset += 8;
                    y = ByteUtils.readLEDouble( wkb, offset );
                    offset += 8;
                    points[j][i] = new PositionImpl( x, y );
                }
            }
        }

        CurveSegment[] segment = new CurveSegment[1];
        Curve[] curves = new Curve[numParts];

        for ( int i = 0; i < numParts; i++ ) {
            segment[0] = createCurveSegment( points[i], crs );
            curves[i] = createCurve( segment );
        }

        return createMultiCurve( curves );
    }

    /**
     * creates a MultiSurface from an array of surfaces
     * 
     * @param surfaces
     * @return a MultiSurface from an array of surfaces
     */
    public static MultiSurface createMultiSurface( Surface[] surfaces ) {
        return new MultiSurfaceImpl( surfaces );
    }

    /**
     * creates a MultiSurface from an array of surfaces
     * 
     * @param surfaces
     * @param crs
     * @return a MultiSurface from an array of surfaces
     */
    public static MultiSurface createMultiSurface( Surface[] surfaces, CoordinateSystem crs ) {
        return new MultiSurfaceImpl( surfaces, crs );
    }

    /**
     * creates a MultiSurface from a wkb
     * 
     * @param wkb
     *            geometry in Well-Known Binary (WKB) format
     * @param crs
     *            spatial reference system of the geometry
     * @param si
     *            surface interpolation
     * @return the MultiSurface defined by the WKB and the given CRS
     * @throws GeometryException
     *             if the wkb is not known or invalid
     */
    public static MultiSurface createMultiSurface( byte[] wkb, CoordinateSystem crs, SurfaceInterpolation si )
                            throws GeometryException {
        int wkbtype = -1;
        int numPoly = 0;
        int numRings = 0;
        int numPoints = 0;
        int offset = 0;
        double x = 0;
        double y = 0;
        Position[] externalBoundary = null;
        Position[][] internalBoundaries = null;
        byte byteorder = wkb[offset++];

        if ( byteorder == 0 ) {
            wkbtype = ByteUtils.readBEInt( wkb, offset );
        } else {
            wkbtype = ByteUtils.readLEInt( wkb, offset );
        }

        offset += 4;

        // is the wkbmetry a multipolygon?
        if ( wkbtype != 6 ) {
            throw new GeometryException( "Invalid byte stream for MultiSurface" );
        }

        // read number of polygons on the byte[]
        if ( byteorder == 0 ) {
            numPoly = ByteUtils.readBEInt( wkb, offset );
        } else {
            numPoly = ByteUtils.readLEInt( wkb, offset );
        }

        offset += 4;

        ArrayList<Surface> list = new ArrayList<Surface>( numPoly );

        for ( int ip = 0; ip < numPoly; ip++ ) {
            byteorder = wkb[offset];
            offset++;

            if ( byteorder == 0 ) {
                wkbtype = ByteUtils.readBEInt( wkb, offset );
            } else {
                wkbtype = ByteUtils.readLEInt( wkb, offset );
            }

            offset += 4;

            // is the geometry respresented by wkb a polygon?
            if ( wkbtype != 3 ) {
                throw new GeometryException( "invalid byte stream for Surface " + wkbtype );
            }

            // read number of rings of the polygon
            if ( byteorder == 0 ) {
                numRings = ByteUtils.readBEInt( wkb, offset );
            } else {
                numRings = ByteUtils.readLEInt( wkb, offset );
            }

            offset += 4;

            // read number of points of the external ring
            if ( byteorder == 0 ) {
                numPoints = ByteUtils.readBEInt( wkb, offset );
            } else {
                numPoints = ByteUtils.readLEInt( wkb, offset );
            }

            offset += 4;

            // allocate memory for the external boundary
            externalBoundary = new Position[numPoints];

            if ( byteorder == 0 ) {
                // read points of the external boundary from the byte[]
                for ( int i = 0; i < numPoints; i++ ) {
                    x = ByteUtils.readBEDouble( wkb, offset );
                    offset += 8;
                    y = ByteUtils.readBEDouble( wkb, offset );
                    offset += 8;
                    externalBoundary[i] = new PositionImpl( x, y );
                }
            } else {
                // read points of the external boundary from the byte[]
                for ( int i = 0; i < numPoints; i++ ) {
                    x = ByteUtils.readLEDouble( wkb, offset );
                    offset += 8;
                    y = ByteUtils.readLEDouble( wkb, offset );
                    offset += 8;
                    externalBoundary[i] = new PositionImpl( x, y );
                }
            }

            // only if numRings is larger then one there internal rings
            if ( numRings > 1 ) {
                internalBoundaries = new Position[numRings - 1][];
            }

            if ( byteorder == 0 ) {
                for ( int j = 1; j < numRings; j++ ) {
                    // read number of points of the j-th internal ring
                    numPoints = ByteUtils.readBEInt( wkb, offset );
                    offset += 4;

                    // allocate memory for the j-th internal boundary
                    internalBoundaries[j - 1] = new Position[numPoints];

                    // read points of the external boundary from the byte[]
                    for ( int i = 0; i < numPoints; i++ ) {
                        x = ByteUtils.readBEDouble( wkb, offset );
                        offset += 8;
                        y = ByteUtils.readBEDouble( wkb, offset );
                        offset += 8;
                        internalBoundaries[j - 1][i] = new PositionImpl( x, y );
                    }
                }
            } else {
                for ( int j = 1; j < numRings; j++ ) {
                    // read number of points of the j-th internal ring
                    numPoints = ByteUtils.readLEInt( wkb, offset );
                    offset += 4;

                    // allocate memory for the j-th internal boundary
                    internalBoundaries[j - 1] = new Position[numPoints];

                    // read points of the external boundary from the byte[]
                    for ( int i = 0; i < numPoints; i++ ) {
                        x = ByteUtils.readLEDouble( wkb, offset );
                        offset += 8;
                        y = ByteUtils.readLEDouble( wkb, offset );
                        offset += 8;
                        internalBoundaries[j - 1][i] = new PositionImpl( x, y );
                    }
                }
            }

            SurfacePatch patch = createSurfacePatch( externalBoundary, internalBoundaries, si, crs );

            list.add( createSurface( patch ) );
        }

        MultiSurface multisurface = new MultiSurfaceImpl( crs );

        for ( int i = 0; i < list.size(); i++ ) {
            multisurface.addSurface( list.get( i ) );
        }

        return multisurface;
    }

}
