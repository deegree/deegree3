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
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryException;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

/**
 *
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
abstract class JTSWrapperGeometry implements Geometry {

    /** id of the geometry object */
    protected String id;

    /**
     * coordinate dimensions of the geometry
     */
    protected int coordinateDimension;

    private CoordinateSystem crs;

    /**
     * the wrapped geometry
     */
    protected com.vividsolutions.jts.geom.Geometry geometry;

    // precision model that is used for all JTS-Geometries
    private PrecisionModel pm;

    /**
     * the JTS geometry factory
     */
    protected GeometryFactory jtsFactory;

    private double precision;

    private Envelope envelope;

    /**
     * the deegree geometry factory for JTS wrapped geometries
     */
    protected static org.deegree.model.geometry.GeometryFactory geomFactory = null;
    static {
        if ( geomFactory == null ) {
            geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory( "JTS" );
        }
    }

    /**
     * 
     * @param precision
     * @param crs
     * @param coordinateDimension
     */
    JTSWrapperGeometry( String id, double precision, CoordinateSystem crs, int coordinateDimension ) {
        this.crs = crs;
        this.precision = precision;
        this.coordinateDimension = coordinateDimension;
        pm = new PrecisionModel( 1d / precision );
        jtsFactory = new GeometryFactory( pm, 0 );
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * this method throws an {@link IllegalArgumentException} if the passed {@link Geometry} is not an instance of a
     * geometry type that can be exported into a JTSGeometry. Allowed types are:
     * <ul>
     * <li>org.deegree.model.geometry.primitive.Point</li>
     * <li>org.deegree.model.geometry.multi.MultiPoint</li>
     * <li>org.deegree.model.geometry.primitive.Curve</li>
     * <li>org.deegree.model.geometry.multi.MultiCurve</li>
     * <li>org.deegree.model.geometry.primitive.Surface</li>
     * <li>org.deegree.model.geometry.multi.MultiSurface</li>
     * <li>org.deegree.model.geometry.multi.MultiGeometry</li>
     * <li>org.deegree.model.geometry.primitive.Envelope</li>
     * </ul>
     * 
     * If conversion fails a {@link GeometryException} will be thrown
     * 
     * @param geometry
     * @return corresponding JTS geometry
     */
    @SuppressWarnings("unchecked")
    protected com.vividsolutions.jts.geom.Geometry export( Geometry geometry ) {

        com.vividsolutions.jts.geom.Geometry geom = null;
        if ( !( geometry instanceof com.vividsolutions.jts.geom.Geometry ) ) {
            if ( geometry instanceof Point ) {
                geom = export( (Point) geometry );
            } else if ( geometry instanceof MultiPoint ) {
                geom = export( (MultiPoint) geometry );
            } else if ( geometry instanceof Curve ) {
                geom = export( (Curve) geometry );
            } else if ( geometry instanceof MultiCurve ) {
                geom = export( (MultiCurve) geometry );
            } else if ( geometry instanceof Surface ) {
                geom = export( (Surface) geometry );
            } else if ( geometry instanceof MultiSurface ) {
                geom = export( (MultiSurface) geometry );
            } else if ( geometry instanceof MultiGeometry ) {
                geom = export( (MultiGeometry<Geometry>) geometry );
            } else if ( geometry instanceof Envelope ) {
                geom = export( (Envelope) geometry );
            } else {
                throw new IllegalArgumentException( "JTSAdapter.export does not support type '"
                                                    + geometry.getClass().getName() + "'!" );
            }
        } else {
            geom = ( (JTSWrapperGeometry) geometry ).getJTSGeometry();
        }
        return geom;
    }

    /**
     * converts a deegree Point into a JTS Coordinate
     * 
     * @param gmPoint
     * @return JTS Coordinate
     */
    Coordinate toCoordinate( Point gmPoint ) {
        Coordinate coord = null;
        if ( gmPoint.getCoordinateDimension() == 2 ) {
            coord = new Coordinate( gmPoint.getX(), gmPoint.getY() );
        } else {
            coord = new Coordinate( gmPoint.getX(), gmPoint.getY(), gmPoint.getZ() );
        }
        jtsFactory.getPrecisionModel().makePrecise( coord );
        return coord;
    }

    /**
     * converts a list of deegree Points into a JTS Coordinate array
     * 
     * @param points
     * @return JTS Coordinate array
     */
    Coordinate[] toCoordinates( List<Point> points ) {
        Coordinate[] coords = new Coordinate[points.size()];

        for ( int i = 0; i < coords.length; i++ ) {
            Point point = points.get( i );
            coords[i] = toCoordinate( point );
        }
        return coords;
    }

    /**
     * Converts a deegree Envelope to a JTS Polygon.
     * 
     * @param envelope
     * @return JTS Polygon representing the passed envelope
     */
    private Polygon export( Envelope envelope ) {
        Coordinate min = toCoordinate( envelope.getMin() );
        Coordinate max = toCoordinate( envelope.getMax() );
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate( min.x, min.y );
        coords[1] = new Coordinate( min.x, max.y );
        coords[2] = new Coordinate( max.x, max.y );
        coords[3] = new Coordinate( max.x, min.y );
        coords[4] = new Coordinate( min.x, min.y );
        CoordinateSequenceFactory fac = CoordinateArraySequenceFactory.instance();
        CoordinateSequence cs = fac.create( coords );
        LinearRing lr = new LinearRing( cs, jtsFactory );
        return jtsFactory.createPolygon( lr, new LinearRing[0] );
    }

    /**
     * Converts a deegree Point to a JTS Point.
     * 
     * 
     * @param gmPoint
     *            point to be converted
     * @return the corresponding Point object
     */
    private com.vividsolutions.jts.geom.Point export( Point gmPoint ) {
        return jtsFactory.createPoint( toCoordinate( gmPoint ) );
    }

    /**
     * Converts a deegree MultiPoint to a JTS MultiPoint.
     * 
     * @param gmMultiPoint
     *            multipoint to be converted
     * @return the corresponding MultiPoint object
     */
    private com.vividsolutions.jts.geom.MultiPoint export( MultiPoint gmMultiPoint ) {
        com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[gmMultiPoint.size()];
        int i = 0;
        for ( Point point : gmMultiPoint) {
            points[i++] = export( point );
        }
        return jtsFactory.createMultiPoint( points );
    }

    /**
     * Converts a deegree Curve to a JTS LineString.
     * 
     * 
     * @param curve
     *            Curve to be converted
     * @return the corresponding LineString object
     */
    private LineString export( Curve curve ) {
        
        LineString geom = null;
        
        switch ( curve.getCurveType() ) {
        case LineString: {
            List<Point> points = ( (org.deegree.model.geometry.primitive.LineString) curve ).getControlPoints();
            geom = jtsFactory.createLineString( toCoordinates( points ) );
        }
        case Curve:
        case CompositeCurve:
        case OrientableCurve: {
            throw new UnsupportedOperationException();
        }
        }
        return geom;
    }

    /**
     * Converts a deegree MultiCurve to a JTS MultiLineString.
     * 
     * 
     * @param multi
     *            MultiCurve to be converted
     * @return the corresponding MultiLineString object
     */
    private MultiLineString export( MultiCurve multi ) {
        LineString[] lineStrings = new LineString[multi.size()];
        int i = 0;
        for ( Curve curve : multi ) {
            lineStrings[i++] = export( curve );
        }
        return jtsFactory.createMultiLineString( lineStrings );
    }

    /**
     * Converts a deegree Surface to a JTS Polygon.
     * 
     * Currently, the Surface _must_ contain exactly one patch!
     * 
     * @param surface
     *            a Surface
     * @return the corresponding Polygon object
     */
    private Polygon export( Surface surface ) {

        List<Curve> boundary = surface.getBoundary();
        CoordinateSequenceFactory fac = CoordinateArraySequenceFactory.instance();
        List<Point> outer = boundary.get( 0 ).getAsLineString().getControlPoints();
        Coordinate[] coords = toCoordinates( outer );

        LinearRing shell = new LinearRing( fac.create( coords ), jtsFactory );

        LinearRing[] holes = new LinearRing[0];
        if ( boundary.size() > 1 ) {
            holes = new LinearRing[boundary.size() - 1];
            for ( int i = 1; i < boundary.size(); i++ ) {
                coords = toCoordinates( boundary.get( i ).getAsLineString().getControlPoints() );
                holes[i - 1] = new LinearRing( fac.create( coords ), jtsFactory );
            }
        }
        return jtsFactory.createPolygon( shell, holes );
    }

    /**
     * Converts a deegree MultiSurface to a JTS MultiPolygon.
     * 
     * Currently, the contained Surface _must_ have exactly one patch!
     * 
     * @param msurface
     *            a MultiSurface
     * @return the corresponding MultiPolygon object
     */
    private MultiPolygon export( MultiSurface msurface ) {

        Polygon[] polygons = new Polygon[msurface.size()];
        int i = 0;
        for ( Surface surface : msurface ) {
            polygons[i++] = export( surface );
        }
        return jtsFactory.createMultiPolygon( polygons );
    }

    /**
     * Converts a deegree MultiGeometry to a JTS GeometryCollection.
     * 
     * 
     * @param multi
     *            a MultiPrimtive
     * @return the corresponding GeometryCollection object
     */
    private com.vividsolutions.jts.geom.GeometryCollection export( MultiGeometry<Geometry> multi ) {

        com.vividsolutions.jts.geom.Geometry[] geoms = new com.vividsolutions.jts.geom.Geometry[multi.size()];
        int i = 0;
        for ( Geometry geometry : multi ) {
            if ( geometry instanceof JTSWrapperGeometry ) {
                geoms[i++] = ( (JTSWrapperGeometry) geometry ).getJTSGeometry();
            } else {
                geoms[i++] = export( geometry );
            }
        }

        return jtsFactory.createGeometryCollection( geoms );
    }

    /**
     * Converts a JTS-Geometry object to a corresponding Geometry. The method throws an {@link IllegalArgumentException}
     * if passed geometry type is unsupported. If conversion fails a {@link GeometryException} will be thrown
     * 
     * Currently, the following conversions are supported:
     * <ul>
     * <li>Point -> Point
     * <li>MultiPoint -> MultiPoint
     * <li>LineString -> Curve
     * <li>MultiLineString -> MultiCurve
     * <li>Polygon -> Surface
     * <li>MultiPolygon -> MultiSurface
     * <li>GeometryCollection -> MultiPrimitive
     * </ul>
     * 
     * 
     * @param geometry
     *            the JTS-Geometry to be converted
     * @return the corresponding Geometry
     */
    public Geometry wrap( com.vividsolutions.jts.geom.Geometry geometry ) {

        Geometry gmObject = null;
        if ( geometry instanceof com.vividsolutions.jts.geom.Point ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.Point) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.MultiPoint ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.MultiPoint) geometry );
        } else if ( geometry instanceof LineString ) {
            gmObject = wrap( (LineString) geometry );
        } else if ( geometry instanceof MultiLineString ) {
            gmObject = wrap( (MultiLineString) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.Polygon ) {
            gmObject = wrap( (Polygon) geometry );
        } else if ( geometry instanceof MultiPolygon ) {
            gmObject = wrap( (MultiPolygon) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.GeometryCollection ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.GeometryCollection) geometry );
        } else {
            throw new IllegalArgumentException( "JTSAdapter.wrap does not support type '"
                                                + geometry.getClass().getName() + "'!" );
        }
        return gmObject;
    }

    /**
     * transforms a JTS coordinate into a deegree {@link Point}
     * 
     * @param coord
     * @return deegree {@link Point}
     */
    protected Point toPoint( Coordinate coord ) {
        return Double.isNaN( coord.z ) ? new JTSWrapperPoint( null, precision, crs, new double[] { coord.x, coord.y } )
                                      : new JTSWrapperPoint( null, precision, crs, new double[] { coord.x, coord.y,
                                                                                                 coord.z } );
    }

    /**
     * transforms a list of JTS coordinates into a list of deegree {@link Point}s
     * 
     * @param coords
     * @return list of deegree {@link Point}s
     */
    protected List<Point> toPoints( Coordinate[] coords ) {
        List<Point> points = new ArrayList<Point>( coords.length );
        for ( int i = 0; i < coords.length; i++ ) {
            points.add( toPoint( coords[i] ) );
        }
        return points;
    }

    private Envelope wrap( com.vividsolutions.jts.geom.Envelope envelope ) {
        Point min = new JTSWrapperPoint( null, precision, crs, new double[] { envelope.getMinX(), envelope.getMinY() } );
        Point max = new JTSWrapperPoint( null, precision, crs, new double[] { envelope.getMaxX(), envelope.getMaxY() } );
        return new JTSWrapperEnvelope( precision, crs, 2, min, max );
    }

    /**
     * Converts a Point to a Points.
     * 
     * 
     * @param point
     *            a Point object
     * @return the corresponding Point
     */
    private Point wrap( com.vividsolutions.jts.geom.Point point ) {
        return toPoint( point.getCoordinate() );
    }

    /**
     * Converts a MultiPoint to a MultiPoint.
     * 
     * @param multi
     *            a MultiPoint object
     * @return the corresponding MultiPoint
     */
    private MultiPoint wrap( com.vividsolutions.jts.geom.MultiPoint multi ) {
        List<Point> gmPoints = new ArrayList<Point>( multi.getNumGeometries() );
        for ( int i = 0; i < multi.getNumGeometries(); i++ ) {
            gmPoints.add( wrap( (com.vividsolutions.jts.geom.Point) multi.getGeometryN( i ) ) );
        }
        return geomFactory.createMultiPoint( null, gmPoints );
    }

    /**
     * Converts a LineString to a Curve.
     * 
     * 
     * @param line
     *            a LineString object
     * @return the corresponding Curve
     */
    private Curve wrap( LineString line ) {
        Coordinate[] coords = line.getCoordinates();
        List<Point> points = toPoints( coords );

        Point[][] pts = new Point[1][];
        pts[0] = points.toArray( new Point[points.size()] );

        return geomFactory.createCurve( null, pts, crs );
    }

    /**
     * Converts a MultiLineString to a MultiCurve.
     * 
     * @param multi
     *            a MultiLineString object
     * @return the corresponding MultiCurve
     */
    private MultiCurve wrap( MultiLineString multi ) {
        List<Curve> curves = new ArrayList<Curve>( multi.getNumGeometries() );
        for ( int i = 0; i < multi.getNumGeometries(); i++ ) {
            curves.add( wrap( (com.vividsolutions.jts.geom.LineString) multi.getGeometryN( i ) ) );
        }
        return geomFactory.createMultiCurve( null, curves );
    }

    /**
     * 
     * Converts a Polygon to a Surface.
     * 
     * @param polygon
     *            a Polygon
     * @return the corresponding Surface object
     */
    private Surface wrap( Polygon polygon ) {
        return null;
//        List<Curve> boundary = new ArrayList<Curve>( polygon.getNumInteriorRing() + 1 );
//        Point[][] ring = new Point[1][];
//        List<Point> list = toPoints( polygon.getExteriorRing().getCoordinates() );
//        ring[0] = list.toArray( new Point[list.size()] );
//        boundary.add( geomFactory.createCurve( null, ring, crs ) );
//        for ( int i = 0; i < polygon.getNumInteriorRing(); i++ ) {
//            list = toPoints( polygon.getInteriorRingN( i ).getCoordinates() );
//            ring[0] = list.toArray( new Point[list.size()] );
//            boundary.add( geomFactory.createCurve( null, ring, crs ) );
//        }
//
//        return geomFactory.createSurface( null, boundary, SurfacePatch.Interpolation.none, crs );
    }

    /**
     * Converts a MultiPolygon to a MultiSurface.
     * 
     * @param multiPolygon
     *            a MultiPolygon
     * @return the corresponding MultiSurface object
     */
    private MultiSurface wrap( com.vividsolutions.jts.geom.MultiPolygon multiPolygon ) {

        List<Surface> surfaces = new ArrayList<Surface>( multiPolygon.getNumGeometries() );
        for ( int i = 0; i < multiPolygon.getNumGeometries(); i++ ) {
            surfaces.add( wrap( (com.vividsolutions.jts.geom.Polygon) multiPolygon.getGeometryN( i ) ) );
        }
        return geomFactory.createMultiSurface( null, surfaces );
    }

    /**
     * Converts a GeometryCollection to a MultiPrimitve.
     * 
     * @param collection
     *            a GeometryCollection
     * @return the corresponding MultiPrimitive object
     */
    private MultiGeometry<Geometry> wrap( GeometryCollection collection ) {

        List<Geometry> geoms = new ArrayList<Geometry>( collection.getNumGeometries() );
        for ( int i = 0; i < collection.getNumGeometries(); i++ ) {
            geoms.add( wrap( collection.getGeometryN( i ) ) );
        }
        return geomFactory.createMultiGeometry( null, geoms );
    }

    public boolean contains( Geometry geometry ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return this.geometry.contains( ( (JTSWrapperGeometry) geometry ).getJTSGeometry() );
        }
        return this.geometry.contains( export( geometry ) );
    }

    public Geometry difference( Geometry geometry ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return wrap( this.geometry.difference( ( (JTSWrapperGeometry) geometry ).getJTSGeometry() ) );
        }
        return wrap( this.geometry.difference( export( geometry ) ) );
    }

    public double distance( Geometry geometry ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return this.geometry.distance( ( (JTSWrapperGeometry) geometry ).getJTSGeometry() );
        }
        return this.geometry.distance( export( geometry ) );
    }

    public double getPrecision() {
        return precision;
    }

    public Geometry getBuffer( double distance ) {
        return wrap( this.geometry.buffer( distance ) );
    }

    public Geometry getConvexHull() {
        return wrap( this.geometry.convexHull() );
    }

    public int getCoordinateDimension() {
        return coordinateDimension;
    }

    public CoordinateSystem getCoordinateSystem() {
        return crs;
    }

    public Envelope getEnvelope() {
        if ( envelope == null ) {
            envelope = wrap( this.geometry.getEnvelopeInternal() );
        }
        return envelope;
    }

    public Geometry intersection( Geometry geometry ) {
        com.vividsolutions.jts.geom.Geometry geom = null;
        if ( geometry instanceof JTSWrapperGeometry ) {
            geom = this.geometry.intersection( ( (JTSWrapperGeometry) geometry ).getJTSGeometry() );
        } else {
            geom = this.geometry.intersection( export( geometry ) );
        }
        return wrap( geom );
    }

    public boolean intersects( Geometry geometry ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return this.geometry.intersects( ( (JTSWrapperGeometry) geometry ).getJTSGeometry() );
        }
        return this.geometry.intersects( export( geometry ) );
    }

    public boolean equals( Geometry geometry ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return this.geometry.equals( ( (JTSWrapperGeometry) geometry ).getJTSGeometry() );
        }
        return this.geometry.equals( export( geometry ) );
    }

    /**
     * tests whether the value of a geometry is topological located within this geometry. This method is the opposite of
     * {@link #contains(Geometry)} method
     * 
     * @param geometry
     * @return true if passed geometry is topological located within this geometry
     */
    public boolean isWithin( Geometry geometry ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return ( (JTSWrapperGeometry) geometry ).getJTSGeometry().contains( this.geometry );
        }
        return export( geometry ).contains( this.geometry );
    }

    /**
     * tests whether the value of a geometric is within a specified distance of this geometry.
     * 
     * @param geometry
     * @param distance
     * @return true if this geometry lies within another
     */
    public boolean isWithinDistance( Geometry geometry, double distance ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return this.geometry.isWithinDistance( ( (JTSWrapperGeometry) geometry ).getJTSGeometry(), distance );
        }
        return this.geometry.isWithinDistance( export( geometry ), distance );
    }

    /**
     * tests whether the value of a geometric is beyond a specified distance of this geometry.
     * 
     * @param geometry
     * @param distance
     * @return true if passed geometry is beyond a specified distance of this geometry.
     */
    public boolean isBeyond( Geometry geometry, double distance ) {
        if ( geometry instanceof JTSWrapperGeometry ) {
            return !this.geometry.isWithinDistance( ( (JTSWrapperGeometry) geometry ).getJTSGeometry(), distance );
        }
        return !this.geometry.isWithinDistance( export( geometry ), distance );
    }

    public Geometry union( Geometry geometry ) {
        com.vividsolutions.jts.geom.Geometry geom = null;
        if ( geometry instanceof JTSWrapperGeometry ) {
            geom = this.geometry.union( ( (JTSWrapperGeometry) geometry ).getJTSGeometry() );
        } else {
            geom = this.geometry.union( export( geometry ) );
        }
        return wrap( geom );
    }

    /**
     * returns the wrapping JTSGeometry
     * 
     * @return wrapping JTSGeometry
     */
    com.vividsolutions.jts.geom.Geometry getJTSGeometry() {
        return geometry;
    }

    @Override
    public String toString() {
        // the name of the wrapper + wkt of the geometry
        return this.getClass().getSimpleName() + ": " + geometry.toString();
    }
}
