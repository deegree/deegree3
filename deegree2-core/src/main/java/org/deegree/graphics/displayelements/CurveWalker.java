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
package org.deegree.graphics.displayelements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Walks along a given curve and generates positions on the line string in regular intervals (i.e. a
 * series of points on the line string with steps of the same distance).
 *
 * @author <a href="mailto:mschneider@lat-lon.de>Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class CurveWalker {

    private double minX;

    private double minY;

    private double maxX;

    private double maxY;

    /**
     * Creates a new instance of <code>CurveWalker</code>. The region of interest (where
     * positions are actually needed, because they are visible) is specified by the given
     * {@link Rectangle}.
     *
     * @param roi
     *            region of interest (visibile area)
     */
    CurveWalker( Rectangle roi ) {
        minX = roi.getMinX();
        minY = roi.getMinY();
        maxX = roi.getMaxX();
        maxY = roi.getMaxY();
    }

    /**
     * Determines positions on the given curve where a caption could be drawn. For each of these
     * positions, three candidates are produced; one on the line, one above of it and one below.
     *
     * @param pos
     * @param width
     *            distance between single positions
     * @param polygon true if the positions denote a polygon
     * @return ArrayList containing positions
     */
    ArrayList<double[]> createPositions( int[][] pos, double width, boolean polygon ) {

        // walk along the linestring and "collect" possible placement positions
        int w = (int) width;
        double lastX = pos[0][0];
        double lastY = pos[1][0];
        double count = pos[2][0];
        double boxStartX = lastX;
        double boxStartY = lastY;

        ArrayList<double[]> labels = new ArrayList<double[]>( 300 );
        List<double[]> eCandidates = new ArrayList<double[]>( 300 );
        int i = 0;

        while ( ( i <= count && polygon ) || i < count ) {
            double x = i == count ? pos[0][0] : pos[0][i];
            double y = i == count ? pos[1][0] : pos[1][i];

            // segment found where endpoint of box should be located?
            if ( getDistance( boxStartX, boxStartY, x, y ) >= w ) {

                double[] p0 = new double[] { boxStartX, boxStartY };
                double[] p1 = new double[] { lastX, lastY };
                double[] p2 = new double[] { x, y };

                double[] p = findPointWithDistance( p0, p1, p2, w );
                x = p[0];
                y = p[1];

                lastX = x;
                lastY = y;
                double boxEndX = x;
                double boxEndY = y;

                double rotation = getSlope( boxStartX, boxStartY, boxEndX, boxEndY );

                /*
                 * double[] deviation = calcDeviation(new double[] { boxStartX, boxStartY }, new
                 * double[] { boxEndX, boxEndY }, eCandidates);
                 */

                // only add position if it is (at least partly) visible
                if ( isInROI( boxStartX, boxStartY ) || isInROI( boxStartX, boxEndY ) || isInROI( boxEndX, boxEndY )
                     || isInROI( boxEndX, boxStartY ) ) {
                    labels.add( new double[] { boxStartX, boxStartY, rotation } );
                }

                boxStartX = lastX;
                boxStartY = lastY;
                eCandidates.clear();
            } else {
                eCandidates.add( new double[] { x, y } );
                lastX = x;
                lastY = y;
                i++;
            }
        }
        return labels;
    }

    /**
     * Returns whether the given point is inside the region of interest.
     *
     * @param x
     *            x coordinate of the point
     * @param y
     *            y coordinate of the point
     * @return true, if the point is inside the region of interest, false otherwise
     */
    private boolean isInROI( double x, double y ) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /**
     * Finds a point on the line between p1 and p2 that has a certain distance from point p0
     * (provided that there is such a point).
     *
     * @param p0
     *            point that is used as reference point for the distance
     * @param p1
     *            starting point of the line
     * @param p2
     *            end point of the line
     * @param d
     *            distance
     * @return a point on the line between p1 and p2 with distance d from p0
     */
    private static double[] findPointWithDistance( double[] p0, double[] p1, double[] p2, double d ) {

        double x, y;
        double x0 = p0[0];
        double y0 = p0[1];
        double x1 = p1[0];
        double y1 = p1[1];
        double x2 = p2[0];
        double y2 = p2[1];

        if ( x1 != x2 ) {
            // line segment does not run vertical
            double u = ( y2 - y1 ) / ( x2 - x1 );
            double p = -2 * ( x0 + u * u * x1 - u * ( y1 - y0 ) ) / ( u * u + 1 );
            double q = ( ( y1 - y0 ) * ( y1 - y0 ) + u * u * x1 * x1 + x0 * x0 - 2 * u * x1 * ( y1 - y0 ) - d * d )
                       / ( u * u + 1 );
            double minX = p1[0];
            double maxX = p2[0];
            double minY = p1[1];
            double maxY = p2[1];
            if ( minX > maxX ) {
                minX = p2[0];
                maxX = p1[0];
            }
            if ( minY > maxY ) {
                minY = p2[1];
                maxY = p1[1];
            }

            if ( x1 < x2 ) {
                // to the right
                x = -p / 2 + Math.sqrt( ( p / 2 ) * ( p / 2 ) - q );
            } else {
                // to the left
                x = -p / 2 - Math.sqrt( ( p / 2 ) * ( p / 2 ) - q );
            }

            // if ((int) (x + 0.5) <= minX || (int) (x + 0.5) >= maxX) {
            // x = -p / 2 + Math.sqrt ((p / 2) * (p / 2) - q);
            // }
            y = ( x - x1 ) * u + y1;
        } else {
            // vertical line segment
            x = x1;
            double minY = p1[1];
            double maxY = p2[1];

            if ( minY > maxY ) {
                minY = p2[1];
                maxY = p1[1];
            }

            double p = -2 * y0;
            double q = y0 * y0 + ( x1 - x0 ) * ( x1 - x0 ) - d * d;

            if ( y1 > y2 ) {
                // down
                y = -p / 2 - Math.sqrt( ( p / 2 ) * ( p / 2 ) - q );
            } else {
                // up
                y = -p / 2 + Math.sqrt( ( p / 2 ) * ( p / 2 ) - q );
            }

            // y = -p / 2 - Math.sqrt ((p / 2) * (p / 2) - q);
            // if ((int) (y + 0.5) <= minY || (int) (y + 0.5) >= maxY) {
            // y = -p / 2 + Math.sqrt ((p / 2) * (p / 2) - q);
            // }
        }
        return new double[] { x, y };
    }

    /**
     * Returns the slope of the line that is constructed by two given points.
     *
     * @param x1
     *            x coordinate of point 1
     * @param y1
     *            y coordinate of point 1
     * @param x2
     *            x coordinate of point 2
     * @param y2
     *            y coordinate of point 2
     * @return the slope of the line (in degrees)
     */
    private double getSlope( double x1, double y1, double x2, double y2 ) {
        double dx = x2 - x1;
        double dy = -( y2 - y1 );
        double rotation = 0.0;

        if ( dx <= 0 ) {
            // dx = 0. division not possible.
            if ( dx == 0 ) {
                dx = 1;
            }
            if ( dy <= 0 ) {
                // left down
                rotation = -Math.atan( dy / dx );
            } else {
                // left up
                rotation = -Math.atan( dy / dx );
            }
        } else {
            if ( dy <= 0 ) {
                // right down
                rotation = -Math.PI - Math.atan( dy / dx );
            } else {
                // right up
                rotation = -Math.PI - Math.atan( dy / dx );
            }
        }
        return Math.toDegrees( rotation );
    }

    /**
     * Returns the Euclidean distance between two given points.
     *
     * @param x1
     *            x coordinate of point 1
     * @param y1
     *            y coordinate of point 1
     * @param x2
     *            x coordinate of point 2
     * @param y2
     *            y coordinate of point 2
     * @return the Euclidean distance between (x1,y1) and (x2,y2)
     */
    private double getDistance( double x1, double y1, double x2, double y2 ) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt( dx * dx + dy * dy );
    }

    // /**
    // * Returns the Euclidean distance between two given points.
    // *
    // * @param p1
    // * point 1
    // * @param p2
    // * point 2
    // * @return the Euclidean distance between p1 and p2
    // */
    // private double getDistance( double[] p1, double[] p2 ) {
    // double dx = p1[0] - p2[0];
    // double dy = p1[1] - p2[1];
    // return Math.sqrt( dx * dx + dy * dy );
    // }

    // /**
    // * Calculates the maximum deviation that points on a linestring have to the ideal line between
    // * the starting point and the end point.
    // * <p>
    // * The ideal line is thought to be running from left to right, the left deviation value
    // * generally is above the line, the right value is below.
    // *
    // * @param start
    // * starting point of the linestring
    // * @param end
    // * end point of the linestring
    // * @param points
    // * points in between
    // * @return the maximum deviation
    // */
    // private double[] calcDeviation( double[] start, double[] end, List points ) {
    //
    // // extreme deviation to the left
    // double d1 = 0.0;
    // // extreme deviation to the right
    // double d2 = 0.0;
    // Iterator it = points.iterator();
    //
    // // eventually swap start and end point
    // if ( start[0] > end[0] ) {
    // double[] tmp = start;
    // start = end;
    // end = tmp;
    // }
    //
    // if ( start[0] != end[0] ) {
    // // label orientation is not completly vertical
    // if ( start[1] != end[1] ) {
    // // label orientation is not completly horizontal
    // while ( it.hasNext() ) {
    // double[] point = (double[]) it.next();
    // double u = ( end[1] - start[1] ) / ( end[0] - start[0] );
    // double x = ( u * u * start[0] - u * ( start[1] - point[1] ) + point[0] )
    // / ( 1.0 + u * u );
    // double y = ( x - start[0] ) * u + start[1];
    // double d = getDistance( point, new double[] { x, y } );
    // if ( y >= point[1] ) {
    // // candidate for left extreme value
    // if ( d > d1 ) {
    // d1 = d;
    // }
    // } else if ( d > d2 ) {
    // // candidate for right extreme value
    // d2 = d;
    // }
    // }
    // } else {
    // // label orientation is completly horizontal
    // while ( it.hasNext() ) {
    // double[] point = (double[]) it.next();
    // double d = point[1] - start[1];
    // if ( d < 0 ) {
    // // candidate for left extreme value
    // if ( -d > d1 ) {
    // d1 = -d;
    // }
    // } else if ( d > d2 ) {
    // // candidate for left extreme value
    // d2 = d;
    // }
    // }
    // }
    // } else {
    // // label orientation is completely vertical
    // while ( it.hasNext() ) {
    // double[] point = (double[]) it.next();
    // double d = point[0] - start[0];
    // if ( d < 0 ) {
    // // candidate for left extreme value
    // if ( -d > d1 ) {
    // d1 = -d;
    // }
    // } else if ( d > d2 ) {
    // // candidate for right extreme value
    // d2 = d;
    // }
    // }
    // }
    // return new double[] { d1, d2 };
    // }
}
