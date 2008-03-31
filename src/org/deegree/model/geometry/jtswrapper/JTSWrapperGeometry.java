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

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryException;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.Curve.ORIENTATION;

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
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
abstract class JTSWrapperGeometry implements Geometry {

    protected int coordinateDimension;

    private CoordinateSystem crs;

    protected com.vividsolutions.jts.geom.Geometry geometry;

    // precision model that is used for all JTS-Geometries
    private PrecisionModel pm;

    protected GeometryFactory jtsFactory;

    private double precision;

    private Envelope envelope;

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
    JTSWrapperGeometry( double precision, CoordinateSystem crs, int coordinateDimension ) {
        this.crs = crs;
        this.precision = precision;
        this.coordinateDimension = coordinateDimension;
        pm = new PrecisionModel( 1d / precision );
        jtsFactory = new GeometryFactory( pm, 0 );
    }

    /**
     * 
     * @param gmObject
     * @return
     * @throws GeometryException
     */
    protected com.vividsolutions.jts.geom.Geometry export( Geometry gmObject )
                            throws GeometryException {

        com.vividsolutions.jts.geom.Geometry geom = null;
        if ( !( gmObject instanceof com.vividsolutions.jts.geom.Geometry ) ) {
            if ( gmObject instanceof Point ) {
                geom = export( (Point) gmObject );
            } else if ( gmObject instanceof MultiPoint ) {
                geom = export( (MultiPoint<Point>) gmObject );
            } else if ( gmObject instanceof Curve ) {
                geom = export( (Curve) gmObject );
            } else if ( gmObject instanceof MultiCurve ) {
                geom = export( (MultiCurve<Curve>) gmObject );
            } else if ( gmObject instanceof Surface ) {
                geom = export( (Surface) gmObject );
            } else if ( gmObject instanceof MultiSurface ) {
                geom = export( (MultiSurface<Surface>) gmObject );
            } else if ( gmObject instanceof MultiGeometry ) {
                geom = export( (MultiGeometry<Geometry>) gmObject );
            } else if ( gmObject instanceof Envelope ) {
                geom = export( (Envelope) gmObject );
            } else {
                throw new GeometryException( "JTSAdapter.export does not support type '"
                                             + gmObject.getClass().getName() + "'!" );
            }
        } else {
            geom = ( (JTSWrapperGeometry) gmObject ).getJTSGeometry();
        }
        return geom;
    }

    /**
     * converts a deegree Point into a JTS Coordinate
     * 
     * @param gmPoint
     * @return JTS Coordinate
     */
    protected Coordinate toCoordinate( Point gmPoint ) {
        Coordinate coord = null;
        if ( gmPoint.getCoordinateDimension() == 2 ) {
            coord = new Coordinate( gmPoint.getX(), gmPoint.getY() );
        } else {
            coord = new Coordinate( gmPoint.getX(), gmPoint.getY(), gmPoint.getZ() );
        }
        return coord;
    }

    /**
     * converts a list of deegree Points into a JTS Coordinate array
     * 
     * @param points
     * @return JTS Coordinate array
     */
    protected Coordinate[] toCoordinates( List<Point> points ) {
        Coordinate[] coords = new Coordinate[points.size()];

        for ( int i = 0; i < coords.length; i++ ) {
            Point point = points.get( i );
            coords[i] = toCoordinate( point );
        }
        return coords;
    }
    
    /**
     * Converts a deegree <code>Envelope</code> to a JTS <code>Polygon</code>.
     * 
     * @param envelope
     * @return JTS Polygon representing the passed envelope
     */
    private Polygon export( Envelope envelope ) {
        Point min = envelope.getMin();
        Point max = envelope.getMax();
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate( min.getX(), min.getY() );
        coords[1] = new Coordinate( min.getX(), max.getY() );
        coords[2] = new Coordinate( max.getX(), max.getY() );
        coords[3] = new Coordinate( max.getX(), min.getY() );
        coords[4] = new Coordinate( min.getX(), min.getY() );
        CoordinateSequenceFactory fac = CoordinateArraySequenceFactory.instance();
        CoordinateSequence cs = fac.create( coords );
        LinearRing lr = new LinearRing( cs, jtsFactory );
        return jtsFactory.createPolygon( lr, new LinearRing[0] );
    }

    /**
     * Converts a deegree <code>Point</code> to a JTS <code>Point</code>.
     * 
     * 
     * @param gmPoint
     *            point to be converted
     * @return the corresponding <code>Point</code> object
     */
    private com.vividsolutions.jts.geom.Point export( Point gmPoint ) {
        return jtsFactory.createPoint( toCoordinate( gmPoint ) );
    }
    

    /**
     * Converts a deegree <code>MultiPoint</code> to a JTS <code>MultiPoint</code>.
     * 
     * 
     * @param gmMultiPoint
     *            multipoint to be converted
     * @return the corresponding <code>MultiPoint</code> object
     */
    private com.vividsolutions.jts.geom.MultiPoint export( MultiPoint<Point> gmMultiPoint ) {
        List<Point> gmPoints = gmMultiPoint.getGeometries();

        com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[gmPoints.size()];
        int i = 0;
        for ( Point point : gmPoints ) {
            points[i++] = export( point );
        }

        return jtsFactory.createMultiPoint( points );
    }

    /**
     * Converts a deegree <code>Curve</code> to a JTS <code>LineString</code>.
     * 
     * 
     * @param curve
     *            <code>Curve</code> to be converted
     * @return the corresponding <code>LineString</code> object
     * @throws GeometryException
     */
    private LineString export( Curve curve )
                            throws GeometryException {

        List<Point> points = curve.getPoints();
        return jtsFactory.createLineString( toCoordinates( points ) );
    }

    /**
     * Converts a deegree <code>MultiCurve</code> to a JTS <code>MultiLineString</code>.
     * 
     * 
     * @param multi
     *            <code>MultiCurve</code> to be converted
     * @return the corresponding <code>MultiLineString</code> object
     * @throws GeometryException
     */
    private MultiLineString export( MultiCurve<Curve> multi )
                            throws GeometryException {

        List<Curve> curves = multi.getGeometries();

        LineString[] lineStrings = new LineString[curves.size()];
        int i = 0;
        for ( Curve curve : curves ) {
            lineStrings[i++] = export( curve );
        }
        return jtsFactory.createMultiLineString( lineStrings );
    }

    /**
     * Converts a deegree <code>Surface</code> to a JTS <code>Polygon</code>.
     * 
     * Currently, the <code>Surface</code> _must_ contain exactly one patch!
     * 
     * 
     * @param surface
     *            a <code>Surface</code>
     * @return the corresponding <code>Polygon</code> object
     */
    private Polygon export( Surface surface ) {

        List<Curve> boundary = surface.getBoundary();
        CoordinateSequenceFactory fac = CoordinateArraySequenceFactory.instance();
        List<Point> outer = boundary.get( 0 ).getPoints();
        Coordinate[] coords = toCoordinates( outer );

        LinearRing shell = new LinearRing( fac.create( coords ), jtsFactory );

        LinearRing[] holes = new LinearRing[0];
        if ( boundary.size() > 1 ) {
            holes = new LinearRing[boundary.size() - 1];
            for ( int i = 1; i < boundary.size(); i++ ) {
                coords = toCoordinates( boundary.get( i ).getPoints() );
                holes[i - 1] = new LinearRing( fac.create( coords ), jtsFactory );
            }
        }
        return jtsFactory.createPolygon( shell, holes );
    }

    /**
     * Converts a deegree <code>MultiSurface</code> to a JTS <code>MultiPolygon</code>.
     * 
     * Currently, the contained <code>Surface</code> _must_ have exactly one patch!
     * 
     * 
     * @param msurface
     *            a <code>MultiSurface</code>
     * @return the corresponding <code>MultiPolygon</code> object
     */
    private MultiPolygon export( MultiSurface<Surface> msurface ) {

        List<Surface> surfaces = msurface.getGeometries();
        Polygon[] polygons = new Polygon[surfaces.size()];

        int i = 0;
        for ( Surface surface : surfaces ) {
            polygons[i++] = export( surface );
        }

        return jtsFactory.createMultiPolygon( polygons );
    }

    /**
     * Converts a deegree <code>MultiGeometry</code> to a JTS <code>GeometryCollection</code>.
     * 
     * 
     * @param multi
     *            a <code>MultiPrimtive</code>
     * @return the corresponding <code>GeometryCollection</code> object
     * @throws GeometryException
     */
    private com.vividsolutions.jts.geom.GeometryCollection export( MultiGeometry<Geometry> multi )
                            throws GeometryException {

        List<Geometry> geometries = multi.getGeometries();
        com.vividsolutions.jts.geom.Geometry[] geoms = new com.vividsolutions.jts.geom.Geometry[geometries.size()];
        int i = 0;
        for ( Geometry geometry : geometries ) {
            geoms[i++] = export( geometry );
        }

        return jtsFactory.createGeometryCollection( geoms );
    }

    /**
     * Converts a JTS-<code>Geometry</code> object to a corresponding <code>Geometry</code>.
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
     *            the JTS-<code>Geometry</code> to be converted
     * @return the corresponding <code>Geometry</code>
     * @throws GeometryException
     *             if type unsupported or conversion failed
     */
    public Geometry wrap( com.vividsolutions.jts.geom.Geometry geometry )
                            throws GeometryException {

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
            throw new GeometryException( "JTSAdapter.wrap does not support type '" + geometry.getClass().getName()
                                         + "'!" );
        }
        return gmObject;
    }

    protected Point toPoint( Coordinate coord ) {
        return Double.isNaN( coord.z ) ? new JTSWrapperPoint( precision, crs, new double[] { coord.x, coord.y } )
                                      : new JTSWrapperPoint( precision, crs, new double[] { coord.x, coord.y, coord.z } );
    }

    protected List<Point> toPoints( Coordinate[] coords ) {
        List<Point> points = new ArrayList<Point>( coords.length );
        for ( int i = 0; i < coords.length; i++ ) {
            points.add( toPoint( coords[i] ) );
        }
        return points;
    }

    private Envelope wrap( com.vividsolutions.jts.geom.Envelope envelope ) {
        Point min = new JTSWrapperPoint( precision, crs, new double[] { envelope.getMinX(), envelope.getMinY() } );
        Point max = new JTSWrapperPoint( precision, crs, new double[] { envelope.getMaxX(), envelope.getMaxY() } );        
        return new JTSWrapperEnvelope( precision, crs, 2, min, max );
    }

    /**
     * Converts a <code>Point</code> to a <code>Point</code>s.
     * 
     * 
     * @param point
     *            a <code>Point</code> object
     * @return the corresponding <code>Point</code>
     */
    private Point wrap( com.vividsolutions.jts.geom.Point point ) {
        return toPoint( point.getCoordinate() );
    }

    /**
     * Converts a <code>MultiPoint</code> to a <code>MultiPoint</code>.
     * 
     * 
     * @param multi
     *            a <code>MultiPoint</code> object
     * @return the corresponding <code>MultiPoint</code>
     */
    private MultiPoint wrap( com.vividsolutions.jts.geom.MultiPoint multi ) {
        List<Point> gmPoints = new ArrayList<Point>( multi.getNumGeometries() );
        for ( int i = 0; i < gmPoints.size(); i++ ) {
            gmPoints.add( wrap( (com.vividsolutions.jts.geom.Point) multi.getGeometryN( i ) ) );
        }
        return geomFactory.createMultiPoint( gmPoints );
    }

    /**
     * Converts a <code>LineString</code> to a <code>Curve</code>.
     * 
     * 
     * @param line
     *            a <code>LineString</code> object
     * @return the corresponding <code>Curve</code>
     * @throws GeometryException
     */
    private Curve wrap( LineString line )
                            throws GeometryException {
        Coordinate[] coords = line.getCoordinates();
        List<Point> points = toPoints( coords );

        Point[][] pts = new Point[1][];
        pts[0] = points.toArray( new Point[points.size()] );

        return geomFactory.createCurve( pts, ORIENTATION.unknown, crs );
    }

    /**
     * Converts a <code>MultiLineString</code> to a <code>MultiCurve</code>.
     * 
     * @param multi
     *            a <code>MultiLineString</code> object
     * @return the corresponding <code>MultiCurve</code>
     * @throws GeometryException
     */
    private MultiCurve wrap( MultiLineString multi )
                            throws GeometryException {
        List<Curve> curves = new ArrayList<Curve>( multi.getNumGeometries() );
        for ( int i = 0; i < curves.size(); i++ ) {
            curves.add( wrap( (com.vividsolutions.jts.geom.LineString) multi.getGeometryN( i ) ) );
        }
        return geomFactory.createMultiCurve( curves );
    }

    /**
     * 
     * Converts a <code>Polygon</code> to a <code>Surface</code>.
     * 
     * @param polygon
     *            a <code>Polygon</code>
     * @return the corresponding <code>Surface</code> object
     * @throws GeometryException
     */
    private Surface wrap( Polygon polygon )
                            throws GeometryException {
        Curve[] boundary = new Curve[polygon.getNumInteriorRing() + 1];
        Point[][] ring = new Point[1][];
        List<Point> list = toPoints( polygon.getExteriorRing().getCoordinates() );
        ring[0] = list.toArray( new Point[list.size()] );
        boundary[0] = geomFactory.createCurve( ring, ORIENTATION.unknown, crs );
        for ( int i = 0; i < polygon.getNumInteriorRing(); i++ ) {
            list = toPoints( polygon.getInteriorRingN( i ).getCoordinates() );
            ring[0] = list.toArray( new Point[list.size()] );
            boundary[i + 1] = geomFactory.createCurve( ring, ORIENTATION.unknown, crs );
        }

        return geomFactory.createSurface( boundary, crs, null );
    }

    /**
     * Converts a <code>MultiPolygon</code> to a <code>MultiSurface</code>.
     * 
     * @param multiPolygon
     *            a <code>MultiPolygon</code>
     * @return the corresponding <code>MultiSurface</code> object
     * @throws GeometryException
     */
    private MultiSurface wrap( com.vividsolutions.jts.geom.MultiPolygon multiPolygon )
                            throws GeometryException {

        List<Surface> surfaces = new ArrayList<Surface>( multiPolygon.getNumGeometries() );
        for ( int i = 0; i < surfaces.size(); i++ ) {
            surfaces.add( wrap( (com.vividsolutions.jts.geom.Polygon) multiPolygon.getGeometryN( i ) ) );
        }
        return geomFactory.createMultiSurface( surfaces );
    }

    /**
     * Converts a <code>GeometryCollection</code> to a <code>MultiPrimitve</code>.
     * 
     * @param collection
     *            a <code>GeometryCollection</code>
     * @return the corresponding <code>MultiPrimitive</code> object
     * @throws GeometryException
     */
    private MultiGeometry<Geometry> wrap( GeometryCollection collection )
                            throws GeometryException {

        Geometry[] geoms = new Geometry[collection.getNumGeometries()];
        for ( int i = 0; i < collection.getNumGeometries(); i++ ) {
            geoms[i] = wrap( collection.getGeometryN( i ) );
        }
        return null;
    }

    public boolean contains( Geometry geometry ) {
        return this.geometry.contains( export( geometry ) );
    }

    public Geometry difference( Geometry geometry ) {
        return wrap( this.geometry.difference( export( geometry ) ) );
    }

    public double distance( Geometry geometry ) {
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
        com.vividsolutions.jts.geom.Geometry geom = this.geometry.intersection( export( geometry ) );
        return wrap( geom );
    }

    public boolean intersects( Geometry geometry ) {
        return this.geometry.intersects( export( geometry ) );
    }

    /**
     * tests whether the value of a geometric is topological located within this geometry. This
     * method is the opposite of {@link #contains(Geometry)} method
     * 
     * @param geometry
     * @return
     */
    public boolean isWithin( Geometry geometry ) {
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
        return !this.geometry.isWithinDistance( export( geometry ), distance );
    }

    public Geometry union( Geometry geometry ) {
        com.vividsolutions.jts.geom.Geometry geom = this.geometry.union( export( geometry ) );
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

}
