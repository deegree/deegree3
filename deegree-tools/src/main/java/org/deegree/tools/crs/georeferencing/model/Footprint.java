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
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;

/**
 * 
 * Model of the footprint of a 3D building. Basis for georeferencing.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Footprint {

    private Polygon polygon;

    private Point2d[] points;

    private Map<AbstractGRPoint, AbstractGRPoint> mappedPoints;

    /**
     * Creates a new <Code>Footprint</Code> instance.
     */
    public Footprint() {
        mappedPoints = new HashMap<AbstractGRPoint, AbstractGRPoint>();
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

    public Map<AbstractGRPoint, AbstractGRPoint> getMappedPoints() {
        return mappedPoints;
    }

    public void setMappedPoints( Map<AbstractGRPoint, AbstractGRPoint> mappedPoints ) {
        this.mappedPoints = mappedPoints;
    }

    /**
     * Adds the <Code>AbstractPoint</Code>s to a map
     * 
     * @param mappedPointKey
     * @param mappedPointValue
     */
    public void addToMappedPoints( AbstractGRPoint mappedPointKey, AbstractGRPoint mappedPointValue ) {
        if ( mappedPointKey != null && mappedPointValue != null ) {
            this.mappedPoints.put( mappedPointKey, mappedPointValue );
        }

    }

    public void removeFromMappedPoints( AbstractGRPoint mappedPointKey ) {
        if ( mappedPointKey != null ) {
            this.mappedPoints.remove( mappedPointKey );
            for ( AbstractGRPoint g : mappedPoints.keySet() ) {
                System.out.println( "key: " + g + " value: " + mappedPoints.get( g ) );

            }
        }
    }

    public void removeAllFromMappedPoints() {
        mappedPoints = new HashMap<AbstractGRPoint, AbstractGRPoint>();

    }

}
