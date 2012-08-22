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

package org.deegree.io.datastore.sql.oracle;

import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;

/**
 * Adapter between deegree <code>Geometry</code> objects and Oracle <code>JGeometry</code> objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </A>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JGeometryAdapter {

    private JGeometryAdapter() {
        // avoid instantiation
    }

    /**
     * Converts a deegree <code>Geometry</code> instance to a corresponding Oracle <code>JGeometry</code> object.
     * 
     * @param geometry
     *            deegree <code>Geometry</code> to be converted
     * @param srid
     *            coordinate system for created Oracle <code>JGeometry</code> objects
     * @return corresponding Oracle <code>JGeometry</code>
     * @throws GeometryException
     */
    public static JGeometry export( Geometry geometry, int srid )
                            throws GeometryException {
        JGeometry jGeometry = null;

        if ( geometry instanceof Point ) {
            jGeometry = exportPoint( (Point) geometry, srid );
        } else if ( geometry instanceof MultiPoint ) {
            jGeometry = exportMultiPoint( (MultiPoint) geometry, srid );
        } else if ( geometry instanceof Curve ) {
            jGeometry = exportCurve( (Curve) geometry, srid );
        } else if ( geometry instanceof MultiCurve ) {
            jGeometry = exportMultiCurve( (MultiCurve) geometry, srid );
        } else if ( geometry instanceof Surface ) {
            jGeometry = exportSurface( (Surface) geometry, srid );
        } else if ( geometry instanceof MultiSurface ) {
            jGeometry = exportMultiSurface( (MultiSurface) geometry, srid );
        } else {
            throw new GeometryException( "Cannot export geometry of type '" + geometry.getClass()
                                         + "' to Oracle JGeometry: Unsupported type." );
        }
        return jGeometry;
    }

    /**
     * Converts an Oracle <code>JGeometry</code> instance to a corresponding deegree <code>Geometry</code> object.
     * 
     * @param jGeometry
     *            Oracle <code>JGeometry</code> to be converted
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return corresponding deegree <code>Geometry</code>
     * @throws GeometryException
     */
    public static Geometry wrap( JGeometry jGeometry, CoordinateSystem crs )
                            throws GeometryException {
        Geometry geo = null;

        switch ( jGeometry.getType() ) {
        case JGeometry.GTYPE_POINT:
            geo = wrapPoint( jGeometry, crs );
            break;
        case JGeometry.GTYPE_CURVE:
            geo = wrapCurve( jGeometry, crs );
            break;
        case JGeometry.GTYPE_POLYGON:
            geo = wrapSurface( jGeometry, crs );
            break;
        case JGeometry.GTYPE_MULTIPOINT:
            geo = wrapMultiPoint( jGeometry, crs );
            break;
        case JGeometry.GTYPE_MULTICURVE:
            geo = wrapMultiCurve( jGeometry, crs );
            break;
        case JGeometry.GTYPE_MULTIPOLYGON:
            geo = wrapMultiSurface( jGeometry, crs );
            break;
        case JGeometry.GTYPE_COLLECTION:
        default: {
            throw new GeometryException( "Cannot export Oracle JGeometry of type '" + jGeometry.getType()
                                         + "' to deegree geometry: Unsupported type." );
        }
        }
        return geo;
    }

    /**
     * Creates an Oracle <code>JGeometry</code> with type <code>GTYPE_POINT</code> from a <code>Point</code>.
     * 
     * @param point
     * @param srid
     *            coordinate system for created Oracle <code>JGeometry</code> objects
     */
    private static JGeometry exportPoint( Point point, int srid ) {
        int dimension = point.getCoordinateDimension();
        double[] coords = point.getAsArray();
        if ( dimension == 2 ) {
            coords = new double[] { coords[0], coords[1] };
        } else {
            coords = new double[] { coords[0], coords[1], coords[2] };
        }
        return JGeometry.createPoint( coords, point.getCoordinateDimension(), srid );
    }

    /**
     * Creates an Oracle <code>JGeometry</code> with type <code>GTYPE_MULTIPOINT</code> from a <code>MultiPoint</code>.
     * 
     * @param multiPoint
     * @param srid
     *            coordinate system for created Oracle <code>JGeometry</code> objects
     */
    private static JGeometry exportMultiPoint( MultiPoint multiPoint, int srid ) {
        Point[] points = multiPoint.getAllPoints();
        int dimension = multiPoint.getCoordinateDimension();
        Object[] coords = new Object[points.length];
        for ( int i = 0; i < coords.length; i++ ) {
            double[] d = points[i].getAsArray();
            if ( dimension == 2 ) {
                coords[i] = new double[] { d[0], d[1] };
            } else {
                coords[i] = new double[] { d[0], d[1], d[2] };
            }
        }
        return JGeometry.createMultiPoint( coords, multiPoint.getCoordinateDimension(), srid );
    }

    /**
     * Creates an Oracle <code>JGeometry</code> with type <code>GTYPE_CURVE</code> from a <code>Curve</code>.
     * 
     * @param curve
     * @param srid
     *            coordinate system for created Oracle <code>JGeometry</code> objects
     */
    private static JGeometry exportCurve( Curve curve, int srid )
                            throws GeometryException {
        int dimension = curve.getCoordinateDimension();
        Position[] positions = curve.getAsLineString().getPositions();
        double[] ordinates = new double[positions.length * dimension];
        int ordinateIndex = 0;
        for ( int i = 0; i < positions.length; i++ ) {
            double[] position = positions[i].getAsArray();
            for ( int j = 0; j < dimension; j++ ) {
                ordinates[ordinateIndex++] = position[j];
            }
        }
        return JGeometry.createLinearLineString( ordinates, dimension, srid );
    }

    /**
     * Creates an Oracle <code>JGeometry</code> with type <code>GTYPE_MULTICURVE</code> from a <code>MultiCurve</code>.
     * 
     * @param multiCurve
     * @param srid
     *            coordinate system for created Oracle <code>JGeometry</code> objects
     * @throws GeometryException
     */
    private static JGeometry exportMultiCurve( MultiCurve multiCurve, int srid )
                            throws GeometryException {
        int dimension = multiCurve.getCoordinateDimension();
        Curve[] curves = multiCurve.getAllCurves();
        Object[] coords = new Object[curves.length];
        for ( int i = 0; i < curves.length; i++ ) {
            Position[] positions = curves[i].getAsLineString().getPositions();
            double[] ordinates = new double[positions.length * dimension];
            int ordinateIndex = 0;
            for ( int j = 0; j < positions.length; j++ ) {
                double[] position = positions[j].getAsArray();
                for ( int k = 0; k < dimension; k++ ) {
                    ordinates[ordinateIndex++] = position[k];
                }
            }
            coords[i] = ordinates;
        }
        return JGeometry.createLinearMultiLineString( coords, dimension, srid );
    }

    /**
     * Creates an Oracle <code>JGeometry</code> with type <code>GTYPE_POLYGON</code> from a <code>Surface</code>.
     * 
     * @param surface
     * @param srid
     *            coordinate system for created Oracle <code>JGeometry</code> objects
     */
    private static JGeometry exportSurface( Surface surface, int srid ) {
        int dimension = surface.getCoordinateDimension();
        Ring exteriorRing = surface.getSurfaceBoundary().getExteriorRing();
        Ring[] interiorRings = surface.getSurfaceBoundary().getInteriorRings();
        Object[] coords = null;
        if ( interiorRings != null ) {
            coords = new Object[1 + interiorRings.length];
        } else {
            coords = new Object[1];
        }

        // counter for rings
        int ringIndex = 0;
        Position[] positions = exteriorRing.getPositions();
        double[] ringOrdinates = new double[positions.length * dimension];
        int ordinateIndex = 0;

        // process exterior ring
        for ( int i = 0; i < positions.length; i++ ) {
            double[] ordinates = positions[i].getAsArray();
            for ( int j = 0; j < dimension; j++ ) {
                ringOrdinates[ordinateIndex++] = ordinates[j];
            }
        }
        coords[ringIndex++] = ringOrdinates;

        // process interior rings
        if ( interiorRings != null ) {
            for ( int interiorRingIndex = 0; interiorRingIndex < interiorRings.length; interiorRingIndex++ ) {
                positions = interiorRings[interiorRingIndex].getPositions();
                ringOrdinates = new double[positions.length * dimension];
                ordinateIndex = 0;
                for ( int i = 0; i < positions.length; i++ ) {
                    double[] ordinates = positions[i].getAsArray();
                    for ( int j = 0; j < dimension; j++ ) {
                        ringOrdinates[ordinateIndex++] = ordinates[j];
                    }
                }
                coords[ringIndex++] = ringOrdinates;
            }
        }
        return JGeometry.createLinearPolygon( coords, dimension, srid );
    }

    /**
     * Creates an Oracle <code>JGeometry</code> with type <code>GTYPE_MULTIPOLYGON</code> from a
     * <code>MultiSurface</code>.
     * 
     * @param multiSurface
     * @param srid
     *            coordinate system for created Oracle <code>JGeometry</code> objects
     */
    private static JGeometry exportMultiSurface( MultiSurface multiSurface, int srid ) {

        List<Integer> elemInfoList = new ArrayList<Integer>( 50 );
        List<Double> ordinateList = new ArrayList<Double>( 5000 );
        Surface[] surfaces = multiSurface.getAllSurfaces();
        int ordinateIdx = 1;
        int dimension = multiSurface.getCoordinateDimension();
        // for each surface
        for ( int surfaceIdx = 0; surfaceIdx < surfaces.length; surfaceIdx++ ) {
            Surface surface = surfaces[surfaceIdx];
            // process exterior ring
            Ring exteriorRing = surface.getSurfaceBoundary().getExteriorRing();
            Position[] positions = exteriorRing.getPositions();
            elemInfoList.add( new Integer( ordinateIdx ) );
            elemInfoList.add( new Integer( 1003 ) );
            elemInfoList.add( new Integer( 1 ) );
            for ( int i = 0; i < positions.length; i++ ) {
                double[] ordinates = positions[i].getAsArray();
                for ( int j = 0; j < dimension; j++ ) {
                    ordinateList.add( new Double( ordinates[j] ) );
                    ordinateIdx++;
                }
            }
            // process interior rings
            Ring[] interiorRings = surface.getSurfaceBoundary().getInteriorRings();
            if ( interiorRings != null ) {
                for ( int interiorRingIdx = 0; interiorRingIdx < interiorRings.length; interiorRingIdx++ ) {
                    positions = interiorRings[interiorRingIdx].getPositions();
                    elemInfoList.add( new Integer( ordinateIdx ) );
                    elemInfoList.add( new Integer( 2003 ) );
                    elemInfoList.add( new Integer( 1 ) );
                    for ( int i = 0; i < positions.length; i++ ) {
                        double[] ordinates = positions[i].getAsArray();
                        for ( int j = 0; j < dimension; j++ ) {
                            ordinateList.add( new Double( ordinates[j] ) );
                            ordinateIdx++;
                        }
                    }
                }
            }
        }
        int[] elemInfo = new int[elemInfoList.size()];
        for ( int i = 0; i < elemInfo.length; i++ ) {
            elemInfo[i] = elemInfoList.get( i ).intValue();
        }
        double[] ordinates = new double[ordinateList.size()];
        for ( int i = 0; i < ordinates.length; i++ ) {
            ordinates[i] = ordinateList.get( i ).doubleValue();
        }
        return new JGeometry( JGeometry.GTYPE_MULTIPOLYGON, srid, elemInfo, ordinates );
    }

    /**
     * Creates a <code>Point</code> from an Oracle <code>JGeometry</code> with type <code>GTYPE_POINT</code>.
     * 
     * @param geometry
     *            Oracle SDO geometry (must be of type <code>GTYPE_POINT</code>)
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree geometry
     * 
     */
    private static Point wrapPoint( JGeometry geometry, CoordinateSystem crs ) {

        int[] elemInfo = geometry.getElemInfo();
        double ord[];
        if ( elemInfo != null && elemInfo.length == 6 && elemInfo[5] == 0 ) {
            // SDO geometry is an oriented point
            int dimension = geometry.getDimensions();
            double[] fullOrd = geometry.getOrdinatesArray();
            ord = new double[dimension];
            for ( int i = 0; i < dimension; i++ ) {
                ord[i] = fullOrd[i];
            }
        } else {
            ord = geometry.getPoint();
        }

        Position pos = GeometryFactory.createPosition( ord );
        return GeometryFactory.createPoint( pos, crs );
    }

    /**
     * Creates a <code>MultiPoint</code> from an Oracle <code>JGeometry</code> with type <code>GTYPE_MULTIPOINT</code>.
     * 
     * @param geometry
     *            Oracle SDO geometry (must be of type <code>GTYPE_MULTIPOINT</code>)
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree geometry
     */
    private static MultiPoint wrapMultiPoint( JGeometry geometry, CoordinateSystem crs ) {

        int[] elemInfo = geometry.getElemInfo();

        int dimension = geometry.getDimensions();
        double[] ordinates = geometry.getOrdinatesArray();
        if ( elemInfo != null ) {
            ArrayList<Point> points = new ArrayList<Point>();

            for ( int i = 0; i < elemInfo.length; i += 3 ) {
                int offset = elemInfo[i] - 1;
                double[] pointOrdinates = new double[dimension];
                // elemInfo[i + 2] == 0 for oriented points
                for ( int j = 0; j < elemInfo[i + 2]; j++ ) {
                    for ( int k = 0; k < dimension; k++ ) {
                        pointOrdinates[k] = ordinates[offset++];
                    }
                    Position position = GeometryFactory.createPosition( pointOrdinates );
                    points.add( GeometryFactory.createPoint( position, crs ) );
                }
            }
            return GeometryFactory.createMultiPoint( points.toArray( new Point[points.size()] ) );
        }
        Point[] points = new Point[geometry.getNumPoints()];

        for ( int i = 0; i < points.length; i++ ) {
            double[] pointOrdinates = new double[dimension];
            for ( int j = 0; j < dimension; j++ ) {
                pointOrdinates[j] = ordinates[i * dimension + j];
            }
            Position position = GeometryFactory.createPosition( pointOrdinates );
            points[i] = GeometryFactory.createPoint( position, crs );
        }
        return GeometryFactory.createMultiPoint( points );
    }

    /**
     * Creates a <code>Curve</code> from an Oracle <code>JGeometry</code> with type <code>GTYPE_CURVE</code>.
     * 
     * @param geometry
     *            Oracle SDO geometry (must be of type <code>GTYPE_CURVE</code>)
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree geometry
     * @throws GeometryException
     */
    private static Curve wrapCurve( JGeometry geometry, CoordinateSystem crs )
                            throws GeometryException {
        return GeometryFactory.createCurve( geometry.getOrdinatesArray(), geometry.getDimensions(), crs );
    }

    /**
     * Creates a <code>MultiCurve</code> from an Oracle <code>JGeometry</code> with type <code>GTYPE_MULTICURVE</code>.
     * 
     * @param geometry
     *            Oracle SDO geometry (must be of type <code>GTYPE_MULTICURVE</code>)
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree geometry
     * @throws GeometryException
     */
    private static MultiCurve wrapMultiCurve( JGeometry geometry, CoordinateSystem crs )
                            throws GeometryException {
        Object[] ooe = geometry.getOrdinatesOfElements();
        int dim = geometry.getDimensions();
        Curve[] curves = new Curve[ooe.length];
        for ( int i = 0; i < ooe.length; i++ ) {
            curves[i] = GeometryFactory.createCurve( (double[]) ooe[i], dim, crs );
        }
        return GeometryFactory.createMultiCurve( curves );
    }

    /**
     * Creates a <code>Surface</code> from an Oracle <code>JGeometry</code> with type <code>GTYPE_POLYGON</code>.
     * 
     * @param geometry
     *            Oracle SDO geometry (must be of type <code>GTYPE_POLYGON</code>)
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree geometry
     * @throws GeometryException
     */
    private static Surface wrapSurface( JGeometry geometry, CoordinateSystem crs )
                            throws GeometryException {
        Object[] ooe = geometry.getOrdinatesOfElements();
        int dim = geometry.getDimensions();
        double[] exteriorRingOrdinates = (double[]) ooe[0];
        double[][] interiorRingsOrdinates = null;
        if ( exteriorRingOrdinates.length == 4 ) {
            // it's a box
            double[] tmp = new double[10];
            tmp[0] = exteriorRingOrdinates[0];
            tmp[1] = exteriorRingOrdinates[1];
            tmp[2] = exteriorRingOrdinates[0];
            tmp[3] = exteriorRingOrdinates[3];
            tmp[4] = exteriorRingOrdinates[2];
            tmp[5] = exteriorRingOrdinates[3];
            tmp[6] = exteriorRingOrdinates[2];
            tmp[7] = exteriorRingOrdinates[1];
            tmp[8] = exteriorRingOrdinates[0];
            tmp[9] = exteriorRingOrdinates[1];
            exteriorRingOrdinates = tmp;
        } else {
            if ( ooe.length > 1 ) {
                interiorRingsOrdinates = new double[ooe.length - 1][];
                for ( int i = 0; i < ooe.length - 1; i++ ) {
                    interiorRingsOrdinates[i] = (double[]) ooe[i + 1];
                }
            }
        }
        return GeometryFactory.createSurface( exteriorRingOrdinates, interiorRingsOrdinates, dim, crs );
    }

    /**
     * Creates a <code>MultiSurface</code> from an Oracle <code>JGeometry</code> with type
     * <code>GTYPE_MULTIPOLYGON</code>.
     * 
     * @param geometry
     *            Oracle SDO geometry (must be of type <code>GTYPE_MULTIPOLYGON</code>)
     * @param crs
     *            coordinate system of the created deegree <code>Geometry</code> object
     * @return deegree geometry
     * @throws GeometryException
     */
    private static MultiSurface wrapMultiSurface( JGeometry geometry, CoordinateSystem crs )
                            throws GeometryException {
        Object[] ooe = geometry.getOrdinatesOfElements();
        int dim = geometry.getDimensions();
        List<Surface> list = new ArrayList<Surface>( 100 );

        int i = 0;
        while ( i < ooe.length ) {
            double[] ext = (double[]) ooe[i++];
            Surface surf = GeometryFactory.createSurface( ext, null, dim, crs );
            boolean within = false;
            List<double[]> temp = new ArrayList<double[]>( 100 );
            if ( i < ooe.length - 1 ) {
                do {
                    double[] ord = (double[]) ooe[i++];
                    double[] pnt = new double[dim];
                    for ( int j = 0; j < pnt.length; j++ ) {
                        pnt[j] = ord[j];
                    }
                    Position pos = GeometryFactory.createPosition( pnt );
                    within = surf.contains( pos );
                    if ( within ) {
                        temp.add( ord );
                    }
                } while ( within && i < ooe.length );
                if ( !within ) {
                    i--;
                }
            }
            double[][] in = new double[temp.size()][];
            in = temp.toArray( in );
            list.add( GeometryFactory.createSurface( ext, in, dim, crs ) );
        }

        Surface[] polys = new Surface[list.size()];
        polys = list.toArray( polys );
        return GeometryFactory.createMultiSurface( polys );
    }
}
