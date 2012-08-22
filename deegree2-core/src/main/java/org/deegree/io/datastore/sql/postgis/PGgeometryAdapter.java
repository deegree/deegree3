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

package org.deegree.io.datastore.sql.postgis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiGeometry;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;
import org.deegree.model.spatialschema.WKTAdapter;
import org.postgis.GeometryCollection;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.MultiPolygon;
import org.postgis.PGbox3d;
import org.postgis.PGboxbase;
import org.postgis.PGgeometry;
import org.postgis.Polygon;

/**
 * Adapter between deegree <code>Geometry</code> objects and PostGIS <code>Geometry</code> objects.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </A>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PGgeometryAdapter {

    private PGgeometryAdapter() {
        // avoid instantiation
    }

    /**
     * Converts a deegree <code>Geometry</code> instance to a corresponding PostGIS {@link PGgeometry} object.
     *
     * @param geometry
     *            deegree <code>Geometry</code> to be converted
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     * @return corresponding PostGIS <code>Geometry</code>
     * @throws GeometryException
     */
    public static PGgeometry export( Geometry geometry, int srid )
                            throws GeometryException {
        PGgeometry pgGeometry = null;
        if ( geometry instanceof Point ) {
            pgGeometry = exportPoint( (Point) geometry, srid );
        } else if ( geometry instanceof MultiPoint ) {
            pgGeometry = exportMultiPoint( (MultiPoint) geometry, srid );
        } else if ( geometry instanceof Curve ) {
            pgGeometry = exportCurve( (Curve) geometry, srid );
        } else if ( geometry instanceof MultiCurve ) {
            pgGeometry = exportMultiCurve( (MultiCurve) geometry, srid );
        } else if ( geometry instanceof Surface ) {
            pgGeometry = exportSurface( (Surface) geometry, srid );
        } else if ( geometry instanceof MultiSurface ) {
            pgGeometry = exportMultiSurface( (MultiSurface) geometry, srid );
        } else if ( geometry instanceof MultiGeometry ) {
            pgGeometry = exportMultiGeometry( (MultiGeometry) geometry, srid );
        } else {
            throw new GeometryException( "Cannot export geometry of type '" + geometry.getClass()
                                         + "' to PostGIS geometry: Unsupported type." );
        }
        return pgGeometry;
    }

    /**
     * Converts a deegree <code>Envelope</code> instance to a corresponding PostGIS <code>PGboxbase</code> object.
     *
     * @param envelope
     *            deegree <code>Envelope</code> to be converted
     * @return corresponding PostGIS <code>PGboxbase</code>
     * @throws GeometryException
     */
    public static PGboxbase export( Envelope envelope )
                            throws GeometryException {
        StringBuffer sb = WKTAdapter.export( envelope );
        PGbox3d box = null;
        try {
            box = new PGbox3d( sb.toString() );
        } catch ( Exception e ) {
            throw new GeometryException( e.toString() );
        }

        return box;
    }

    /**
     * Converts a PostGIS <code>PGGeometry</code> instance to a corresponding deegree <code>Geometry</code> object.
     *
     * @param pgGeometry
     *            PostGIS <code>PGgeometry</code> to be converted
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return corresponding deegree <code>Geometry</code>
     * @throws GeometryException
     */
    public static Geometry wrap( PGgeometry pgGeometry, CoordinateSystem crs )
                            throws GeometryException {
        return wrap( pgGeometry.getGeometry(), crs );
    }

    /**
     * Converts a PostGIS <code>Geometry</code> instance to a corresponding deegree <code>Geometry</code> object.
     *
     * @param geometry
     *            PostGIS <code>PGgeometry</code> to be converted
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return corresponding deegree <code>Geometry</code>
     * @throws GeometryException
     */
    public static Geometry wrap( org.postgis.Geometry geometry, CoordinateSystem crs )
                            throws GeometryException {
        Geometry geo = null;

        switch ( geometry.type ) {
        case org.postgis.Geometry.POINT:
            geo = wrapPoint( (org.postgis.Point) geometry, crs );
            break;
        case org.postgis.Geometry.LINESTRING:
            geo = wrapCurve( (LineString) geometry, crs );
            break;
        case org.postgis.Geometry.POLYGON:
            geo = wrapSurface( (Polygon) geometry, crs );
            break;
        case org.postgis.Geometry.MULTIPOINT:
            geo = wrapMultiPoint( (org.postgis.MultiPoint) geometry, crs );
            break;
        case org.postgis.Geometry.MULTILINESTRING:
            geo = wrapMultiCurve( (MultiLineString) geometry, crs );
            break;
        case org.postgis.Geometry.MULTIPOLYGON:
            geo = wrapMultiSurface( (MultiPolygon) geometry, crs );
            break;
        case org.postgis.Geometry.GEOMETRYCOLLECTION:
            geo = wrapMultiGeometry( (GeometryCollection) geometry, crs );
            break;
        default: {
            throw new GeometryException( "Cannot export PostGIS geometry of type '" + geometry.getType()
                                         + "' to deegree geometry: Unsupported type." );
        }
        }
        return geo;
    }

    /**
     * Creates a PostGIS <code>MultiPoint</code> from a deegree <code>Point</code>.
     *
     * @param point
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     * @throws GeometryException
     */
    private static PGgeometry exportPoint( Point point, int srid )
                            throws GeometryException {

        StringBuffer sb = WKTAdapter.export( point );
        org.postgis.Point pgPoint = null;

        try {
            pgPoint = new org.postgis.Point( sb.toString() );
        } catch ( SQLException e ) {
            throw new GeometryException( e.toString() );
        }

        pgPoint.setSrid( srid );
        return new PGgeometry( pgPoint );
    }

    /**
     * Creates a PostGIS <code>MultiPoint</code> from a deegree <code>MultiPoint</code>.
     *
     * @param multiPoint
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     * @throws GeometryException
     */
    private static PGgeometry exportMultiPoint( MultiPoint multiPoint, int srid )
                            throws GeometryException {

        StringBuffer sb = WKTAdapter.export( multiPoint );
        org.postgis.MultiPoint pgMPoint = null;

        try {
            pgMPoint = new org.postgis.MultiPoint( sb.toString() );
        } catch ( Exception e ) {
            throw new GeometryException( e.toString() );
        }

        pgMPoint.setSrid( srid );
        return new PGgeometry( pgMPoint );
    }

    /**
     * Creates a PostGIS <code>LineString</code> from a deegree <code>Curve</code>.
     *
     * @param curve
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     */
    private static PGgeometry exportCurve( Curve curve, int srid )
                            throws GeometryException {
        StringBuffer sb = WKTAdapter.export( curve );
        org.postgis.LineString pgLineString = null;

        try {
            pgLineString = new org.postgis.LineString( sb.toString() );
        } catch ( Exception e ) {
            throw new GeometryException( e.toString() );
        }

        pgLineString.setSrid( srid );
        return new PGgeometry( pgLineString );
    }

    /**
     * Creates a PostGIS <code>org.postgis.MultiCurve</code> from a deegree <code>MultiCurve</code>.
     *
     * @param multiCurve
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     * @throws GeometryException
     */
    private static PGgeometry exportMultiCurve( MultiCurve multiCurve, int srid )
                            throws GeometryException {
        StringBuffer sb = WKTAdapter.export( multiCurve );
        org.postgis.MultiLineString pgMLineString = null;

        try {
            pgMLineString = new org.postgis.MultiLineString( sb.toString() );
        } catch ( Exception e ) {
            throw new GeometryException( e.toString() );
        }

        pgMLineString.setSrid( srid );
        return new PGgeometry( pgMLineString );
    }

    /**
     * Creates a PostGIS <code>Polygon</code> from a deegree <code>Surface</code>.
     *
     * @param surface
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     * @throws GeometryException
     */
    private static PGgeometry exportSurface( Surface surface, int srid )
                            throws GeometryException {
        StringBuffer sb = WKTAdapter.export( surface );
        org.postgis.Polygon pgPoly = null;

        try {
            pgPoly = new org.postgis.Polygon( sb.toString() );
        } catch ( Exception e ) {
            throw new GeometryException( e.toString() );
        }

        pgPoly.setSrid( srid );
        return new PGgeometry( pgPoly );
    }

    /**
     * Creates a PostGIS <code>MultiSurface</code> from a deegree <code>MultiSurface</code>.
     *
     * @param multiSurface
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     * @throws GeometryException
     */
    private static PGgeometry exportMultiSurface( MultiSurface multiSurface, int srid )
                            throws GeometryException {
        StringBuffer sb = WKTAdapter.export( multiSurface );
        org.postgis.MultiPolygon pgMPoly = null;

        try {
            pgMPoly = new org.postgis.MultiPolygon( sb.toString() );
        } catch ( Exception e ) {
            throw new GeometryException( e.toString() );
        }

        pgMPoly.setSrid( srid );
        return new PGgeometry( pgMPoly );
    }

    /**
     * Creates a PostGIS <code>GeometryCollection</code> from a deegree <code>MultiGeometry</code>.
     *
     * @param multiGeometry
     * @param srid
     *            PostGIS SRS id that is used to store the geometry
     * @throws GeometryException
     */
    private static PGgeometry exportMultiGeometry( MultiGeometry multiGeometry, int srid )
                            throws GeometryException {

        StringBuffer sb = WKTAdapter.export( multiGeometry );
        GeometryCollection pgGeometryCollection = null;

        try {
            pgGeometryCollection = new GeometryCollection( sb.toString() );
        } catch ( Exception e ) {
            throw new GeometryException( e.toString() );
        }

        pgGeometryCollection.setSrid( srid );
        return new PGgeometry( pgGeometryCollection );
    }

    /**
     * Creates a deegree <code>Point</code> from a PostGIS <code>Point</code>.
     *
     * @param pgPoint
     *            PostGIS <code>Point</code>
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree <code>Point</code>
     */
    private static Point wrapPoint( org.postgis.Point pgPoint, CoordinateSystem crs ) {
        // if geometry is 2-dimensional
        Position p = null;
        if ( pgPoint.getDimension() == 2 ) {
            // convert PostGIS Point to a Point using the GeometryFactory
            p = GeometryFactory.createPosition( new double[] { pgPoint.getX(), pgPoint.getY() } );
            // if geometry is 3-dimensional
        } else if ( pgPoint.getDimension() == 3 ) {
            // convert PostGIS Point to a Point using the GeometryFactory
            p = GeometryFactory.createPosition( new double[] { pgPoint.getX(), pgPoint.getY(), pgPoint.getZ() } );
        }
        return GeometryFactory.createPoint( p, crs );
    }

    /**
     * Creates a deegree <code>MultiPoint</code> from a PostGIS <code>MultiPoint</code>.
     *
     * @param pgMultiPoint
     *            PostGIS <code>MultiPoint</code>
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree <code>MultiPoint</code>
     */
    private static MultiPoint wrapMultiPoint( org.postgis.MultiPoint pgMultiPoint, CoordinateSystem crs ) {
        // create a temporary Point Array to store the Points the
        // MultiPoint will consist of
        Point[] mpoints = new Point[pgMultiPoint.numPoints()];
        // for all Points
        for ( int i = 0; i < pgMultiPoint.numPoints(); i++ ) {
            // convert PostGIS Point to a Point using the GeometryFactory
            mpoints[i] = wrapPoint( pgMultiPoint.getPoint( i ), crs );
        }
        // create a Multipoint from the Array points
        return GeometryFactory.createMultiPoint( mpoints );
    }

    /**
     * Creates a deegree <code>Curve</code> from a PostGIS <code>LineString</code>.
     *
     * @param pgLineString
     *            PostGIS <code>LineString</code>
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree <code>Curve</code>
     * @throws GeometryException
     */
    private static Curve wrapCurve( LineString pgLineString, CoordinateSystem crs )
                            throws GeometryException {
        // create a Position Array. Used to store the Points the
        // Curve will consist of
        Position[] points = new Position[pgLineString.numPoints()];

        // if geometry is 2-dimensional
        if ( pgLineString.getDimension() == 2 ) {
            // for all Points
            for ( int i = 0; i < pgLineString.numPoints(); i++ ) {
                // create a Position from the PostGIS Point using the
                // GeometryFactory
                double[] d = new double[] { pgLineString.getPoint( i ).getX(), pgLineString.getPoint( i ).getY() };
                points[i] = GeometryFactory.createPosition( d );
            }
            // if geometry is 3-dimensional
        } else if ( pgLineString.getDimension() == 3 ) {
            // for all Points
            for ( int i = 0; i < pgLineString.numPoints(); i++ ) {
                // create a Position from the PostGIS Point using the
                // GeometryFactory
                double[] d = new double[] { pgLineString.getPoint( i ).getX(), pgLineString.getPoint( i ).getY(),
                                           pgLineString.getPoint( i ).getZ() };
                points[i] = GeometryFactory.createPosition( d );
            }
        }
        return GeometryFactory.createCurve( points, crs );
    }

    /**
     * Creates a deegree <code>MultiCurve</code> from a PostGIS <code>MultiLineString</code>.
     *
     * @param pgMultiLineString
     *            PostGIS <code>MultiLineString</code>
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree <code>MultiCurve</code>
     * @throws GeometryException
     */
    private static MultiCurve wrapMultiCurve( MultiLineString pgMultiLineString, CoordinateSystem crs )
                            throws GeometryException {
        // create a Curve Array. Used to store the CurveSegments the
        // Curve will consist of
        Curve[] curves = new Curve[pgMultiLineString.numLines()];
        // for all Lines
        for ( int i = 0; i < pgMultiLineString.numLines(); i++ ) {
            // create a Curve form the positions Array using the
            // GeometryFactory
            curves[i] = wrapCurve( pgMultiLineString.getLine( i ), crs );
        }
        // create a Curve form all the CurveSegments stored in the
        // csegments Array using the GeometryFactory
        return GeometryFactory.createMultiCurve( curves );
    }

    /**
     * Creates a deegree <code>Surface</code> from a PostGIS <code>Polygon</code>.
     *
     * @param pgPolygon
     *            PostGIS <code>Polygon</code>
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree <code>Surface</code>
     * @throws GeometryException
     */
    private static Surface wrapSurface( Polygon pgPolygon, CoordinateSystem crs )
                            throws GeometryException {

        // create a Position Array. Used to store the Positions the
        // exterior Ring of the Surface will consist of
        Position[] eRing = new Position[pgPolygon.getRing( 0 ).numPoints()];
        // declares a Position[][] Array. Used to store the Positions
        // of the interior Rings the Surface will consist of. The exterior
        // Ring is stored seperately
        Position[][] iRings = null;

        // if geometry is 2-dimensional
        if ( pgPolygon.getDimension() == 2 ) {
            // for all the Points of the fist LinearRing (which is the exterior)
            org.postgis.LinearRing ring = pgPolygon.getRing( 0 );
            for ( int j = 0; j < eRing.length; j++ ) {
                // store all the Points of the exterior Ring in the Array
                // eRing. Convert them using GeometryFactory
                double[] d = new double[] { ring.getPoint( j ).getX(), ring.getPoint( j ).getY() };
                eRing[j] = GeometryFactory.createPosition( d );
            }

            if ( pgPolygon.numRings() > 1 ) {
                iRings = new Position[pgPolygon.numRings() - 1][];
                // for all LinearRings except the first one (which is the exterior one)
                for ( int i = 1; i < pgPolygon.numRings(); i++ ) {
                    iRings[i - 1] = new Position[pgPolygon.getRing( i ).numPoints()];
                    // for all the Points in the ith LinearRing
                    ring = pgPolygon.getRing( i );
                    for ( int j = 0; j < ring.numPoints(); j++ ) {
                        // store all the Points of the ith interior Ring in
                        // the iRings Array
                        double[] d = new double[] { ring.getPoint( j ).getX(), ring.getPoint( j ).getY() };
                        iRings[i - 1][j] = GeometryFactory.createPosition( d );
                    }
                }
            }
            // if geometry is 3-dimensional
        } else if ( pgPolygon.getDimension() == 3 ) {
            // for all the Points of the fist LinearRing (which is the exterior)
            org.postgis.LinearRing ring = pgPolygon.getRing( 0 );
            for ( int j = 0; j < ring.numPoints(); j++ ) {
                // store all the Points of the exterior Ring in the Array
                // eRing. Convert them using GeometryFactory
                double[] d = new double[] { ring.getPoint( j ).getX(), ring.getPoint( j ).getY(),
                                           ring.getPoint( j ).getZ() };
                eRing[j] = GeometryFactory.createPosition( d );
            }

            if ( pgPolygon.numRings() > 1 ) {
                iRings = new Position[pgPolygon.numRings() - 1][];
                // for all LinearRings except the first one (which is the exterior one)
                for ( int i = 1; i < pgPolygon.numRings(); i++ ) {
                    iRings[i - 1] = new Position[pgPolygon.getRing( i ).numPoints()];
                    // for all the Points in the ith LinearRing
                    ring = pgPolygon.getRing( i );
                    for ( int j = 0; j < ring.numPoints(); j++ ) {
                        // store all the Points of the ith interior Ring in the iRings Array
                        double[] d = new double[] { ring.getPoint( j ).getX(), ring.getPoint( j ).getY(),
                                                   ring.getPoint( j ).getZ() };
                        iRings[i - 1][j] = GeometryFactory.createPosition( d );
                    }
                }
            }
        }

        return GeometryFactory.createSurface( eRing, iRings, new SurfaceInterpolationImpl(), crs );
    }

    /**
     * Creates a deegree <code>MultiSurface</code> from a PostGIS <code>MultiPolygon</code>.
     *
     * @param pgMultiPolygon
     *            PostGIS <code>MultiPolygon</code>
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree <code>MultiSurface</code>
     * @throws GeometryException
     */
    private static MultiSurface wrapMultiSurface( MultiPolygon pgMultiPolygon, CoordinateSystem crs )
                            throws GeometryException {
        // create a Surfaces Array. Used to store the Surfaces the
        // MultiSurface will consist of
        Surface[] surfaces = new Surface[pgMultiPolygon.numPolygons()];
        // for all Polygons the MultiPolygon consists of
        for ( int i = 0; i < pgMultiPolygon.numPolygons(); i++ ) {
            surfaces[i] = wrapSurface( pgMultiPolygon.getPolygon( i ), crs );
        }

        return GeometryFactory.createMultiSurface( surfaces );
    }

    /**
     * Creates a deegree <code>MultiGeometry</code> from a PostGIS <code>GeometryCollection</code>.
     *
     * @param pgGeometryCollection
     *            PostGIS <code>GeometryCollection</code>
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree <code>MultiGeometry</code>
     * @throws GeometryException
     */
    private static MultiGeometry wrapMultiGeometry( GeometryCollection pgGeometryCollection, CoordinateSystem crs )
                            throws GeometryException {

        List<Geometry> members = new ArrayList<Geometry>();
        Iterator<?> memberIter = pgGeometryCollection.iterator();
        while ( memberIter.hasNext() ) {
            org.postgis.Geometry memberGeometry = (org.postgis.Geometry) memberIter.next();
            members.add( wrap( memberGeometry, crs ) );
        }
        return GeometryFactory.createMultiGeometry( members.toArray( new Geometry[members.size()] ), crs );
    }
}
