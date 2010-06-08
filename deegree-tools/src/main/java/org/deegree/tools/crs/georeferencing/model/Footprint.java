package org.deegree.tools.crs.georeferencing.model;

import java.awt.Polygon;

import javax.vecmath.Point2d;

public class Footprint {

    private Polygon polygon;

    private Point2d[] points;

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

    public Polygon getPolygon() {
        return polygon;
    }

    public Point2d[] getPoints() {
        return points;
    }

    public Point2d getClosestPoint( Point2d point2d ) {
        Point2d closestPoint = null;
        if ( points != null || points.length != 0 ) {
            double distance = 0.0;

            for ( Point2d point : points ) {
                if ( distance == 0.0 ) {
                    distance = point.distance( point2d );
                    closestPoint = new Point2d( point.x, point.y );
                } else {
                    double distanceTemp = point.distance( point2d );
                    if ( distanceTemp < distance ) {
                        distance = distanceTemp;
                        closestPoint = new Point2d( point.x, point.y );
                    }
                }
            }
        }

        return closestPoint;
    }

}
