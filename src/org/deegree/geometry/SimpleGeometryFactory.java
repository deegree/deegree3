//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/geometry/GeometryFactory.java $
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

package org.deegree.geometry;

import java.util.ArrayList;
import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiLineString;
import org.deegree.geometry.standard.multi.DefaultMultiPoint;
import org.deegree.geometry.standard.multi.DefaultMultiPolygon;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;

/**
 * Supplies utility methods for building simple {@link Geometry} objects.
 * 
 * @see GeometryFactory
 * @see GeometryInspector
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version. $Revision: 18333 $, $Date: 2009-07-07 10:32:18 +0200 (Di, 07 Jul 2009) $
 */
public class SimpleGeometryFactory {

    protected List<GeometryInspector> inspectors = new ArrayList<GeometryInspector>();

    protected PrecisionModel pm;

    public SimpleGeometryFactory() {
        this.pm = PrecisionModel.DEFAULT_PRECISION_MODEL;
    }

    public SimpleGeometryFactory( PrecisionModel pm ) {
        this.pm = pm;
    }

    /**
     * Adds the given {@link GeometryInspector} which will be invoked for every {@link Geometry} / {@link CurveSegment}
     * or {@link SurfacePatch} instance created by this factory.
     * 
     * @param inspector
     *            inspector to be added, must not be <code>null</code>
     */
    public void addInspector( GeometryInspector inspector ) {
        inspectors.add( inspector );
    }

    protected Geometry inspect( Geometry geom ) {
        Geometry inspected = geom;
        for ( GeometryInspector inspector : inspectors ) {
            inspected = inspector.inspect( inspected );
        }
        return inspected;
    }
    
    protected Points inspect( Points points ) {
        Points inspected = points;
        for ( GeometryInspector inspector : inspectors ) {
            inspected = inspector.inspect( inspected );
        }
        return inspected;
    }    

    /**
     * Creates a {@link Point} in 2D space.
     * 
     * @param id
     *            identifier, may be null
     * @param x
     *            value for first coordinate
     * @param y
     *            value for second coordinate
     * @param crs
     *            coordinate reference system, may be null
     * @return created {@link Point}
     */
    public Point createPoint( String id, double x, double y, CRS crs ) {
        return (Point) inspect( new DefaultPoint( id, crs, pm, new double[] { x, y } ) );
    }

    /**
     * Creates a {@link Point} in 3D space.
     * 
     * @param id
     *            identifier, may be null
     * @param x
     *            value for first coordinate
     * @param y
     *            value for second coordinate
     * @param z
     *            value for third coordinate
     * @param crs
     *            coordinate reference system, may be null
     * @return created {@link Point}
     */
    public Point createPoint( String id, double x, double y, double z, CRS crs ) {
        return (Point) inspect( new DefaultPoint( id, crs, pm, new double[] { x, y, z } ) );
    }

    /**
     * Creates a {@link Point} with an arbitrary number of coordinates.
     * 
     * @param id
     *            identifier, may be null
     * @param coordinates
     *            coordinate values
     * @param crs
     *            coordinate reference system, may be null
     * @return created {@link Point}
     */
    public Point createPoint( String id, double[] coordinates, CRS crs ) {
        return (Point) inspect( new DefaultPoint( id, crs, pm, coordinates ) );
    }

    /**
     * Creates a {@link Points} object from the given list of {@link Point} instances.
     * 
     * @param points
     *            list of points, must not be <code>null</code>
     * @return created {@link Points}
     */
    public Points createPoints (List<Point> points) {
        return inspect (new PointsList( points ));
    }

    /**
     * Creates a {@link Polygon} surface.
     * 
     * @param id
     *            identifier of the new geometry instance
     * @param crs
     *            coordinate reference system, may be null
     * @param exteriorRing
     *            ring that defines the outer boundary, this may be null (see section 9.2.2.5 of GML spec)
     * @param interiorRings
     *            list of rings that define the inner boundaries, may be empty or null
     * @return created {@link Polygon}
     */
    public Polygon createPolygon( String id, CRS crs, Ring exteriorRing, List<Ring> interiorRings ) {
        return (Polygon) inspect( new DefaultPolygon( id, crs, pm, exteriorRing, interiorRings ) );
    }

    /**
     * Creates a {@link LineString} geometry.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system
     * @param points
     *            control points
     * @return created {@link LineString}
     */
    public LineString createLineString( String id, CRS crs, Points points ) {
        return (LineString) inspect( new DefaultLineString( id, crs, pm, points ) );
    }

    /**
     * Creates an {@link Envelope}.
     * 
     * @param min
     *            minimum corner coordinates
     * @param max
     *            maximum corner coordinates
     * @param crs
     *            coordinate reference system, may be null
     * @return created {@link Envelope}
     */
    public Envelope createEnvelope( double[] min, double[] max, CRS crs ) {
        return (Envelope) inspect( new DefaultEnvelope( null, crs, pm, new DefaultPoint( null, crs, pm, min ),
                                                        new DefaultPoint( null, crs, pm, max ) ) );
    }

    /**
     * Creates an {@link Envelope} in 2D space.
     * 
     * @param minx
     *            minimum x corner coordinate
     * @param miny
     *            minimum y corner coordinate
     * @param maxx
     *            maximum x corner coordinate
     * @param maxy
     *            maximum y corner coordinate
     * @param crs
     *            coordinate reference system, may be null
     * @return created {@link Envelope}
     */
    public Envelope createEnvelope( double minx, double miny, double maxx, double maxy, CRS crs ) {
        return createEnvelope( new double[] { minx, miny }, new double[] { maxx, maxy }, crs );
    }

    /**
     * Create an {@link Envelope} from a list of Doubles.
     * 
     * @param lowerCorner
     * @param upperCorner
     * @param crs
     *            coordinate reference system, may be null
     * @return the envelope
     */
    public Envelope createEnvelope( List<Double> lowerCorner, List<Double> upperCorner, CRS crs ) {
        if ( lowerCorner.size() != upperCorner.size() ) {
            throw new IllegalArgumentException( "LowerCorner must be of same dimension as upperCorner." );
        }
        double[] lc = new double[lowerCorner.size()];
        double[] uc = new double[upperCorner.size()];
        for ( int i = 0; i < lc.length; ++i ) {
            lc[i] = lowerCorner.get( i );
            uc[i] = upperCorner.get( i );
        }
        return createEnvelope( lc, uc, crs );
    }

    /**
     * Creates an untyped multi geometry from a list of {@link Geometry}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     * @return created {@link MultiGeometry}
     */
    @SuppressWarnings("unchecked")
    public MultiGeometry<Geometry> createMultiGeometry( String id, CRS crs, List<Geometry> members ) {
        return (MultiGeometry<Geometry>) inspect( new DefaultMultiGeometry<Geometry>( id, crs, pm, members ) );
    }

    /**
     * Creates a {@link MultiPoint} from a list of passed {@link Point}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            points that constitute the collection
     * @return created {@link MultiPoint}
     */
    public MultiPoint createMultiPoint( String id, CRS crs, List<Point> members ) {
        return (MultiPoint) inspect( new DefaultMultiPoint( id, crs, pm, members ) );
    }

    /**
     * Creates a {@link MultiCurve} from a list of passed {@link LineString}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            curves that constitute the collection
     * @return created {@link MultiLineString}
     */
    public MultiLineString createMultiLineString( String id, CRS crs, List<LineString> members ) {
        return (MultiLineString) inspect( new DefaultMultiLineString( id, crs, pm, members ) );
    }

    /**
     * Creates a {@link MultiPolygon} from a list of passed {@link Polygon}s.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param members
     *            polygons that constitute the collection
     * @return created {@link MultiPolygon}
     */
    public MultiPolygon createMultiPolygon( String id, CRS crs, List<Polygon> members ) {
        return (MultiPolygon) inspect( new DefaultMultiPolygon( id, crs, pm, members ) );
    }
}
