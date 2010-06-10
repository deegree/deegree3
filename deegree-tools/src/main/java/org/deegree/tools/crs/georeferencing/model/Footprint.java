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
package org.deegree.tools.crs.georeferencing.model;

import java.awt.Polygon;
import java.util.Vector;

import javax.vecmath.Point2d;

import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;

/**
 * 
 * Model of the footprint of a 3D building.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Footprint {

    private Polygon polygon;

    private Point2d[] points;

    private Vector<AbstractGRPoint> tableValueGeoRef;

    private Vector<AbstractGRPoint> tableValueFootPrint;

    /**
     * Creates a new <Code>Footprint</Code> instance.
     */
    public Footprint() {
        tableValueGeoRef = new Vector();
        tableValueFootPrint = new Vector();
    }

    /**
     * Creates a default instance of a <Code>Polygon</Code> from native Java AWT package.
     * 
     * <dl>
     * <dt>first point</dt>
     * <dd>50,50</dd>
     * <dt>second point</dt>
     * <dd>50,250</dd>
     * <dt>third point</dt>
     * <dd>200,200</dd>
     * <dt>fourth point</dt>
     * <dd>200,80</dd>
     * </dl>
     */
    public void setDefaultPolygon() {
        points = new Point2d[4];
        points[0] = new Point2d( 50, 50 );
        points[1] = new Point2d( 50, 250 );
        points[2] = new Point2d( 200, 200 );
        points[3] = new Point2d( 200, 80 );

        this.polygon = new Polygon( new int[] { (int) points[0].x, (int) points[1].x, (int) points[2].x,
                                               (int) points[3].x }, new int[] { (int) points[0].y, (int) points[1].y,
                                                                               (int) points[2].y, (int) points[3].y },
                                    points.length );

    }

    /**
     * This should be a coverage later.
     * 
     * @return an AWT <Code>Polygon</Code>
     */
    public Polygon getPolygon() {
        return polygon;
    }

    /**
     * Determines the closest point of a raster (a polygon at the moment) to a specified point.
     * 
     * @param point2d
     *            the specified point
     * @return an <Code>AbstractPoint</Code> of the raster that is the closest point to point2d
     */
    public AbstractGRPoint getClosestPoint( AbstractGRPoint point2d ) {
        AbstractGRPoint closestPoint = null;
        if ( points != null || points.length != 0 ) {
            double distance = 0.0;

            for ( Point2d point : points ) {
                if ( distance == 0.0 ) {
                    distance = point.distance( point2d );
                    if ( point2d instanceof FootprintPoint ) {
                        closestPoint = new FootprintPoint( point.x, point.y );
                    } else if ( point2d instanceof GeoReferencedPoint ) {
                        closestPoint = new GeoReferencedPoint( point.x, point.y );
                    }

                } else {
                    double distanceTemp = point.distance( point2d );
                    if ( distanceTemp < distance ) {
                        distance = distanceTemp;
                        if ( point2d instanceof FootprintPoint ) {
                            closestPoint = new FootprintPoint( point.x, point.y );
                        } else if ( point2d instanceof GeoReferencedPoint ) {
                            closestPoint = new GeoReferencedPoint( point.x, point.y );
                        }
                    }
                }
            }
        }

        return closestPoint;
    }

    /**
     * Adds an <Code>AbstractPoint</Code> to the <Code>GeoReference</Code> value of the <Code>TableModel</Code>.
     * 
     * @param lastGeoReferencedPoint
     */
    public void addToTableValueGeoRef( AbstractGRPoint lastGeoReferencedPoint ) {
        if ( lastGeoReferencedPoint != null ) {
            this.tableValueGeoRef.add( lastGeoReferencedPoint );
        }

    }

    /**
     * 
     * @return a Vector of <Code>GeoReferencedPoint</Code>s
     */
    public Vector<AbstractGRPoint> getTableValueGeoRef() {
        return tableValueGeoRef;
    }

    /**
     * Adds an <Code>AbstractPoint</Code> to the <Code>Footprint</Code> value of the <Code>TableModel</Code>.
     * 
     * @param lastFootprintPoint
     */
    public void addToTableValueFootPrint( AbstractGRPoint lastFootprintPoint ) {
        if ( lastFootprintPoint != null ) {
            this.tableValueFootPrint.add( lastFootprintPoint );
        }

    }

    /**
     * 
     * @return a Vector of <Code>FootprintPoint</Code>s
     */
    public Vector<AbstractGRPoint> getTableValueFootPrint() {
        return tableValueFootPrint;
    }

}
