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

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Adapter between deegree <code>Geometry</code>s and JTS <code>Geometry<code> objects.
 * <p>
 * Please note that the generated deegree-objects use null as
 * coordinate system!
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$ $Date$
 */
public class JTSAdapter {

    private static final ILogger LOG = LoggerFactory.getLogger( JTSAdapter.class );

    // precision model that is used for all JTS geometries
    private static PrecisionModel pm = new PrecisionModel();

    // factory for creating JTS geometries
    private static com.vividsolutions.jts.geom.GeometryFactory jtsFactory = new com.vividsolutions.jts.geom.GeometryFactory(
                                                                                                                             pm,
                                                                                                                             0 );

    /**
     * Converts a deegree <code>Geometry</code> to a corresponding JTS <code>Geometry</code> object.
     * <p>
     * Currently, the following conversions are supported:
     * <ul>
     * <li>Curve -> LineString</li>
     * <li>Point -> Point</li>
     * <li>Surface -> Polygon</li>
     * <li>MultiCurve -> MultiLineString</li>
     * <li>MultiPoint -> MultiPoint</li>
     * <li>MultiSurface -> MultiPolygon</li>
     * <li>MultiPrimitive -> GeometryCollection</li>
     * <li>MultiGeometry -> GeometryCollection</li>
     * </ul>
     * <p>
     * 
     * @param gmObject
     *            the object to be converted
     * @return the corresponding JTS-<code>Geometry</code> object
     * @throws GeometryException
     *             if type unsupported or conversion failed
     */
    public static synchronized com.vividsolutions.jts.geom.Geometry export( Geometry gmObject )
                            throws GeometryException {

        com.vividsolutions.jts.geom.Geometry geometry = null;
        if ( gmObject instanceof Curve ) {
            geometry = export( (Curve) gmObject );
        } else if ( gmObject instanceof Point ) {
            geometry = export( (Point) gmObject );
        } else if ( gmObject instanceof Surface ) {
            geometry = export( (Surface) gmObject );
        } else if ( gmObject instanceof MultiCurve ) {
            geometry = export( (MultiCurve) gmObject );
        } else if ( gmObject instanceof MultiPoint ) {
            geometry = export( (MultiPoint) gmObject );
        } else if ( gmObject instanceof MultiSurface ) {
            geometry = export( (MultiSurface) gmObject );
        } else if ( gmObject instanceof MultiPrimitive ) {
            geometry = export( (MultiPrimitive) gmObject );
        } else if ( gmObject instanceof MultiGeometry ) {
            geometry = export( (MultiGeometry) gmObject );
        } else {
            throw new GeometryException( "JTSAdapter.export does not support type '" + gmObject.getClass().getName()
                                         + "'!" );
        }
        return geometry;
    }

    /**
     * Converts a JTS <code>Geometry</code> object to a corresponding deegree <code>Geometry</code>.
     * <p>
     * Currently, the following conversions are supported:
     * <ul>
     * <li>LineString -> Curve
     * <li>Point -> Point
     * <li>Polygon -> Surface
     * <li>MultiLineString -> MultiCurve
     * <li>MultiPoint -> MultiPoint
     * <li>MultiPolygon -> MultiSurface
     * <li>GeometryCollection -> MultiGeometry
     * </ul>
     * <p>
     * 
     * @param geometry
     *            the JTS-<code>Geometry</code> to be converted
     * @return the corresponding <code>Geometry</code>
     * @throws GeometryException
     *             if type unsupported or conversion failed
     */
    public static Geometry wrap( com.vividsolutions.jts.geom.Geometry geometry )
                            throws GeometryException {

        Geometry gmObject = null;
        if ( geometry instanceof com.vividsolutions.jts.geom.LineString ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.LineString) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.Point ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.Point) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.Polygon ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.Polygon) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.MultiLineString ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.MultiLineString) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.MultiPoint ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.MultiPoint) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.MultiPolygon ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.MultiPolygon) geometry );
        } else if ( geometry instanceof com.vividsolutions.jts.geom.GeometryCollection ) {
            gmObject = wrap( (com.vividsolutions.jts.geom.GeometryCollection) geometry );
        } else {
            throw new GeometryException( "JTSAdapter.wrap does not support type '" + geometry.getClass().getName()
                                         + "'!" );
        }
        return gmObject;
    }

    /**
     * Converts a deegree <code>Point</code> to a JTS <code>Point</code>.
     * 
     * @param gmPoint
     *            point to be converted
     * @return the corresponding <code>Point</code> object
     */
    private static synchronized com.vividsolutions.jts.geom.Point export( Point gmPoint ) {

        com.vividsolutions.jts.geom.Coordinate coord = new com.vividsolutions.jts.geom.Coordinate( gmPoint.getX(),
                                                                                                   gmPoint.getY() );

        return jtsFactory.createPoint( coord );
    }

    /**
     * Converts a deegree <code>MultiPoint</code> to a JTS <code>MultiPoint</code>.
     * 
     * @param gmMultiPoint
     *            multipoint to be converted
     * @return the corresponding <code>MultiPoint</code> object
     */
    private static synchronized com.vividsolutions.jts.geom.MultiPoint export( MultiPoint gmMultiPoint ) {
        Point[] gmPoints = gmMultiPoint.getAllPoints();
        com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[gmPoints.length];
        for ( int i = 0; i < points.length; i++ ) {
            points[i] = export( gmPoints[i] );
        }
        return jtsFactory.createMultiPoint( points );
    }

    /**
     * Converts a deegree <code>Curve</code> to a JTS <code>LineString</code>.
     * 
     * @param curve
     *            <code>Curve</code> to be converted
     * @return the corresponding <code>LineString</code> object
     * @throws GeometryException
     */
    private static synchronized com.vividsolutions.jts.geom.LineString export( Curve curve )
                            throws GeometryException {

        LineString lineString = curve.getAsLineString();
        com.vividsolutions.jts.geom.Coordinate[] coords = new com.vividsolutions.jts.geom.Coordinate[lineString.getNumberOfPoints()];
        for ( int i = 0; i < coords.length; i++ ) {
            Position position = lineString.getPositionAt( i );
            coords[i] = new com.vividsolutions.jts.geom.Coordinate( position.getX(), position.getY() );
        }
        return jtsFactory.createLineString( coords );
    }

    /**
     * Converts a deegree <code>MultiCurve</code> to a JTS <code>MultiLineString</code>.
     * 
     * @param multi
     *            <code>MultiCurve</code> to be converted
     * @return the corresponding <code>MultiLineString</code> object
     * @throws GeometryException
     */
    private static synchronized com.vividsolutions.jts.geom.MultiLineString export( MultiCurve multi )
                            throws GeometryException {

        Curve[] curves = multi.getAllCurves();
        com.vividsolutions.jts.geom.LineString[] lineStrings = new com.vividsolutions.jts.geom.LineString[curves.length];
        for ( int i = 0; i < curves.length; i++ ) {
            lineStrings[i] = export( curves[i] );
        }
        return jtsFactory.createMultiLineString( lineStrings );
    }

    /**
     * Converts an array of deegree <code>Position</code>s to a JTS <code>LinearRing</code>.
     * 
     * @param positions
     *            an array of <code>Position</code>s
     * @return the corresponding <code>LinearRing</code> object
     */
    public static synchronized com.vividsolutions.jts.geom.LinearRing export( Position[] positions ) {
        com.vividsolutions.jts.geom.Coordinate[] coords = new com.vividsolutions.jts.geom.Coordinate[positions.length];
        for ( int i = 0; i < positions.length; i++ ) {
            coords[i] = new com.vividsolutions.jts.geom.Coordinate( positions[i].getX(), positions[i].getY() );
        }
        return jtsFactory.createLinearRing( coords );
    }

    /**
     * Converts a deegree <code>SurfacePatch</code> into a JTS <code>Polygon</code>.
     * 
     * @param patch
     *            {@link SurfacePatch}
     * @return corresponding JTS <code>Polygon</code> object
     */
    public static synchronized com.vividsolutions.jts.geom.Polygon export( SurfacePatch patch ) {

        // convert exterior ring
        LinearRing jtsShell = export( patch.getExteriorRing() );

        // convert interior rings
        LinearRing[] jtsHoles = null;
        Ring[] interiorRings = patch.getInterior();
        if ( interiorRings != null ) {
            jtsHoles = new LinearRing[interiorRings.length];
            for ( int i = 0; i < interiorRings.length; i++ ) {
                jtsHoles[i] = export( interiorRings[i].getPositions() );
            }
        }
        return jtsFactory.createPolygon( jtsShell, jtsHoles );
    }

    /**
     * Converts a deegree <code>Surface</code> to a JTS <code>Polygon</code>.
     * <p>
     * Currently, the <code>Surface</code> _must_ contain exactly one patch!
     * 
     * @param surface
     *            a <code>Surface</code>
     * @return the corresponding <code>Polygon</code> object
     */
    private static synchronized com.vividsolutions.jts.geom.Polygon export( Surface surface ) {
        SurfacePatch patch = null;
        try {
            patch = surface.getSurfacePatchAt( 0 );
            Position[] exteriorRing = patch.getExteriorRing();
            Position[][] interiorRings = patch.getInteriorRings();

            com.vividsolutions.jts.geom.LinearRing shell = export( exteriorRing );
            com.vividsolutions.jts.geom.LinearRing[] holes = new com.vividsolutions.jts.geom.LinearRing[0];
            if ( interiorRings != null ) {
                holes = new com.vividsolutions.jts.geom.LinearRing[interiorRings.length];
                for ( int i = 0; i < holes.length; i++ ) {
                    holes[i] = export( interiorRings[i] );
                }
            }
            return jtsFactory.createPolygon( shell, holes );
        } catch ( GeometryException e ) {
            LOG.logError( "", e );
        }
        return null;
    }

    /**
     * Converts a JTS <code>MultiSurface</code> to a deegree <code>MultiPolygon</code>.
     * <p>
     * Currently, the contained <code>Surface</code> _must_ have exactly one patch!
     * 
     * @param msurface
     *            a <code>MultiSurface</code>
     * @return the corresponding <code>MultiPolygon</code> object
     */
    private static synchronized com.vividsolutions.jts.geom.MultiPolygon export( MultiSurface msurface ) {

        Surface[] surfaces = msurface.getAllSurfaces();
        com.vividsolutions.jts.geom.Polygon[] polygons = new com.vividsolutions.jts.geom.Polygon[surfaces.length];

        for ( int i = 0; i < surfaces.length; i++ ) {
            polygons[i] = export( surfaces[i] );
        }
        return jtsFactory.createMultiPolygon( polygons );
    }

    /**
     * Converts a JTS <code>MultiPrimitive</code> to a deegree <code>GeometryCollection</code>.
     * 
     * @param multi
     *            a <code>MultiPrimitive</code>
     * @return the corresponding <code>GeometryCollection</code> object
     * @throws GeometryException
     */
    private static synchronized com.vividsolutions.jts.geom.GeometryCollection export( MultiPrimitive multi )
                            throws GeometryException {

        Geometry[] primitives = multi.getAllPrimitives();
        com.vividsolutions.jts.geom.Geometry[] geometries = new com.vividsolutions.jts.geom.Geometry[primitives.length];

        for ( int i = 0; i < primitives.length; i++ ) {
            geometries[i] = export( primitives[i] );
        }
        return jtsFactory.createGeometryCollection( geometries );
    }

    /**
     * Converts a JTS <code>MultiGeometry</code> into a deegree <code>GeometryCollection</code>.
     * 
     * @param multi
     *            a <code>MultiGeometry</code>
     * @return corresponding <code>GeometryCollection</code> object
     * @throws GeometryException
     */
    private static synchronized com.vividsolutions.jts.geom.GeometryCollection export( MultiGeometry multi )
                            throws GeometryException {

        Geometry[] memberGeometries = multi.getAll();
        com.vividsolutions.jts.geom.Geometry[] jtsMemberGeometries = new com.vividsolutions.jts.geom.Geometry[memberGeometries.length];
        for ( int i = 0; i < memberGeometries.length; i++ ) {
            jtsMemberGeometries[i] = export( memberGeometries[i] );
        }
        return jtsFactory.createGeometryCollection( jtsMemberGeometries );
    }

    /**
     * Converts a JTS <code>Point</code> to a deegree <code>Point</code>.
     * 
     * @param point
     *            a <code>Point</code> object
     * @return the corresponding <code>Point</code>
     */
    private static Point wrap( com.vividsolutions.jts.geom.Point point ) {
        com.vividsolutions.jts.geom.Coordinate coord = point.getCoordinate();
        return Double.isNaN( coord.z ) ? new PointImpl( coord.x, coord.y, null ) : new PointImpl( coord.x, coord.y,
                                                                                                  coord.z, null );
    }

    /**
     * Converts a JTS <code>MultiPoint</code> to a deegree <code>MultiPoint</code>.
     * 
     * @param multi
     *            a <code>MultiPoint</code> object
     * @return the corresponding <code>MultiPoint</code>
     */
    private static MultiPoint wrap( com.vividsolutions.jts.geom.MultiPoint multi ) {
        Point[] gmPoints = new Point[multi.getNumGeometries()];
        for ( int i = 0; i < gmPoints.length; i++ ) {
            gmPoints[i] = wrap( (com.vividsolutions.jts.geom.Point) multi.getGeometryN( i ) );
        }
        return new MultiPointImpl( gmPoints, null );
    }

    /**
     * Converts a <code>LineString</code> to a <code>Curve</code>.
     * 
     * @param line
     *            a <code>LineString</code> object
     * @return the corresponding <code>Curve</code>
     * @throws GeometryException
     */
    private static Curve wrap( com.vividsolutions.jts.geom.LineString line )
                            throws GeometryException {
        com.vividsolutions.jts.geom.Coordinate[] coords = line.getCoordinates();
        Position[] positions = new Position[coords.length];
        for ( int i = 0; i < coords.length; i++ ) {
            positions[i] = new PositionImpl( coords[i].x, coords[i].y );
        }
        return GeometryFactory.createCurve( positions, null );
    }

    /**
     * Converts a <code>MultiLineString</code> to a <code>MultiCurve</code>.
     * 
     * @param multi
     *            a <code>MultiLineString</code> object
     * @return the corresponding <code>MultiCurve</code>
     * @throws GeometryException
     */
    private static MultiCurve wrap( com.vividsolutions.jts.geom.MultiLineString multi )
                            throws GeometryException {
        Curve[] curves = new Curve[multi.getNumGeometries()];
        for ( int i = 0; i < curves.length; i++ ) {
            curves[i] = wrap( (com.vividsolutions.jts.geom.LineString) multi.getGeometryN( i ) );
        }
        return GeometryFactory.createMultiCurve( curves );
    }

    /**
     * Converts a <code>Polygon</code> to a <code>Surface</code>.
     * 
     * @param polygon
     *            a <code>Polygon</code>
     * @return the corresponding <code>Surface</code> object
     * @throws GeometryException
     */
    private static Surface wrap( com.vividsolutions.jts.geom.Polygon polygon )
                            throws GeometryException {

        Position[] exteriorRing = createGMPositions( polygon.getExteriorRing() );
        Position[][] interiorRings = new Position[polygon.getNumInteriorRing()][];

        for ( int i = 0; i < interiorRings.length; i++ ) {
            interiorRings[i] = createGMPositions( polygon.getInteriorRingN( i ) );
        }
        SurfacePatch patch = new PolygonImpl( new SurfaceInterpolationImpl(), exteriorRing, interiorRings, null );

        return new SurfaceImpl( patch );
    }

    /**
     * Converts a <code>MultiPolygon</code> to a <code>MultiSurface</code>.
     * 
     * @param multiPolygon
     *            a <code>MultiPolygon</code>
     * @return the corresponding <code>MultiSurface</code> object
     * @throws GeometryException
     */
    private static MultiSurface wrap( com.vividsolutions.jts.geom.MultiPolygon multiPolygon )
                            throws GeometryException {

        Surface[] surfaces = new Surface[multiPolygon.getNumGeometries()];
        for ( int i = 0; i < surfaces.length; i++ ) {
            surfaces[i] = wrap( (com.vividsolutions.jts.geom.Polygon) multiPolygon.getGeometryN( i ) );
        }
        return new MultiSurfaceImpl( surfaces );
    }

    /**
     * Converts a <code>GeometryCollection</code> to a <code>MultiGeometry</code>.
     * 
     * @param collection
     *            a <code>GeometryCollection</code>
     * @return the corresponding <code>MultiGeometry</code> object
     * @throws GeometryException
     */
    private static MultiGeometry wrap( com.vividsolutions.jts.geom.GeometryCollection collection )
                            throws GeometryException {

        MultiGeometry multi = new MultiGeometryImpl( null );
        for ( int i = 0; i < collection.getNumGeometries(); i++ ) {
            multi.add( wrap( collection.getGeometryN( i ) ) );
        }
        return multi;
    }

    /**
     * Converts a <code>LineString</code> to an array of <code>Position</code>s.
     * 
     * @param line
     *            a <code>LineString</code> object
     * @return the corresponding array of <code>Position</code>s
     */
    private static Position[] createGMPositions( com.vividsolutions.jts.geom.LineString line ) {
        com.vividsolutions.jts.geom.Coordinate[] coords = line.getCoordinates();
        Position[] positions = new Position[coords.length];
        for ( int i = 0; i < coords.length; i++ ) {
            positions[i] = new PositionImpl( coords[i].x, coords[i].y );
        }
        return positions;
    }
}
