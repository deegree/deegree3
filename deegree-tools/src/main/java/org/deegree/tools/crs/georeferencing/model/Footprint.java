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
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point3Values;

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

    private FootprintPoint[] pixelCoordinates;

    private List<Polygon> pixelCoordinatePolygonList;

    private List<Polygon> worldCoordinatePolygonList;

    private int offset;

    private float size;

    private float resizing;

    private List<float[]> footprintPointsList;

    /**
     * Map<initialValue,newValue>
     */
    private List<Point3Values> selectedPoints;

    private Point3Values lastFootprintPoint;

    private float initialResolution;

    /**
     * Creates a new <Code>Footprint</Code> instance.
     */
    public Footprint( float initialResolution ) {
        this.initialResolution = initialResolution;
        selectedPoints = new ArrayList<Point3Values>();
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
        Point2d[] pointsWorldCoordinate = new Point2d[4];
        pointsWorldCoordinate[0] = new Point2d( 50, 50 );
        pointsWorldCoordinate[1] = new Point2d( 50, 250 );
        pointsWorldCoordinate[2] = new Point2d( 200, 200 );
        pointsWorldCoordinate[3] = new Point2d( 200, 80 );

        this.polygon = new Polygon( new int[] { (int) pointsWorldCoordinate[0].x, (int) pointsWorldCoordinate[1].x,
                                               (int) pointsWorldCoordinate[2].x, (int) pointsWorldCoordinate[3].x },
                                    new int[] { (int) pointsWorldCoordinate[0].y, (int) pointsWorldCoordinate[1].y,
                                               (int) pointsWorldCoordinate[2].y, (int) pointsWorldCoordinate[3].y },
                                    pointsWorldCoordinate.length );

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
     * Determines the closest point of the point2d in worldCoordinate.
     * 
     * @param point2d
     *            the specified point
     * @return an <Code>AbstractPoint</Code> that is the closest point to point2d
     */
    public FootprintPoint getClosestPoint( AbstractGRPoint point2d ) {
        FootprintPoint closestPoint = null;
        double distance = 0.0;

        for ( FootprintPoint point : pixelCoordinates ) {
            // System.out.println( "[Footprint] PixelPoint " + point );
            if ( distance == 0.0 ) {
                distance = point.distance( point2d );
                if ( point2d instanceof FootprintPoint ) {
                    closestPoint = new FootprintPoint( point.getX(), point.getY() );
                }
                // else if ( point2d instanceof GeoReferencedPoint ) {
                // closestPoint = new GeoReferencedPoint( point.x, point.y );
                // }

            } else {
                double distanceTemp = point.distance( point2d );
                if ( distanceTemp < distance ) {
                    distance = distanceTemp;
                    if ( point2d instanceof FootprintPoint ) {
                        closestPoint = new FootprintPoint( point.getX(), point.getY() );
                    }
                    // else if ( point2d instanceof GeoReferencedPoint ) {
                    // closestPoint = new GeoReferencedPoint( point.x, point.y );
                    // }
                }
            }
        }

        return closestPoint;
    }

    /**
     * Generates the polygons in world- and pixel-coordinates.
     * 
     * @param footprintPointsList
     *            the points from the <Code>WorldRenderableObject</Code>
     */
    public void generateFootprints( List<float[]> footprintPointsList ) {
        this.footprintPointsList = footprintPointsList;
        worldCoordinatePolygonList = new ArrayList<Polygon>();
        for ( float[] f : footprintPointsList ) {
            int size = f.length / 3;
            int[] x = new int[size];
            int[] y = new int[size];
            int count = 0;

            // get all points in 2D, so z-axis is omitted
            for ( int i = 0; i < f.length; i += 3 ) {
                x[count] = (int) f[i];
                y[count] = (int) f[i + 1];
                count++;
            }
            Polygon p = new Polygon( x, y, size );
            worldCoordinatePolygonList.add( p );
        }
        generateFootprintsPixelCoordinate( worldCoordinatePolygonList );

    }

    private void generateFootprintsPixelCoordinate( List<Polygon> polyList ) {
        if ( this.size == 0.0f ) {
            this.size = 4.0f;
        }
        System.out.println( "[Footprint] Resize: " + size );
        pixelCoordinatePolygonList = new ArrayList<Polygon>();
        List<Rectangle> rect = new ArrayList<Rectangle>();

        int sizeOfPoints = 0;
        for ( Polygon p : polyList ) {
            sizeOfPoints += p.npoints;
            rect.add( p.getBounds() );
        }

        if ( pixelCoordinates == null ) {
            pixelCoordinates = new FootprintPoint[sizeOfPoints];
        }
        Rectangle temp = null;
        // get minimum X
        for ( Rectangle rec : rect ) {
            if ( temp == null ) {
                temp = rec;
            } else {
                if ( rec.x < temp.x ) {
                    temp = rec;
                }
            }
        }
        int x = temp.x - offset;

        Rectangle tempY = null;
        // get minimum Y
        for ( Rectangle rec : rect ) {
            if ( tempY == null ) {
                tempY = rec;
            } else {
                if ( rec.y < temp.y ) {
                    tempY = rec;
                }
            }
        }
        int y = temp.y - offset;
        int counter = 0;
        for ( Polygon po : polyList ) {
            int[] x2 = new int[po.npoints];
            int[] y2 = new int[po.npoints];
            for ( int i = 0; i < po.npoints; i++ ) {
                x2[i] = (int) ( ( po.xpoints[i] - x ) * size );
                y2[i] = (int) ( ( po.ypoints[i] - y ) * size );
                pixelCoordinates[counter++] = new FootprintPoint( ( po.xpoints[i] - x ) * size, ( po.ypoints[i] - y )
                                                                                                * size );
                // System.out.println( "[Footprint] Polygon: " + x2[i] );
            }
            Polygon p = new Polygon( x2, y2, po.npoints );
            pixelCoordinatePolygonList.add( p );

        }

    }

    /**
     * 
     * @return the list of polygons in pixel-coordinates
     */
    public List<Polygon> getPixelCoordinatePolygonList() {
        return pixelCoordinatePolygonList;
    }

    /**
     * 
     * @return the list of polygons in world-coordinates
     */
    public List<Polygon> getWorldCoordinatePolygonList() {
        return worldCoordinatePolygonList;
    }

    public int getOffset() {
        return offset;
    }

    /**
     * This value specifies how many pixels the lowest position should be.
     * 
     * @param offset
     */
    public void setOffset( int offset ) {
        this.offset = offset;
    }

    /**
     * better to use resolution instead of size?
     * 
     * @return
     */
    public float getSize() {
        return size;
    }

    public void setSize( float size ) {
        this.size = size;
    }

    public void updatePoints( Point2d changePoint ) {
        for ( FootprintPoint p : pixelCoordinates ) {
            p.setX( p.getX() - changePoint.x );
            p.setY( p.getY() - changePoint.y );
        }

    }

    public void updatePoints( float newSize ) {
        this.resizing = newSize - this.size;
        BigDecimal b = new BigDecimal( newSize );
        b = b.round( new MathContext( 2 ) );
        this.size = b.floatValue();

        generateFootprints( footprintPointsList );

        updateSelectedPoints();

    }

    private float roundFloat( float value ) {
        BigDecimal b = new BigDecimal( value );
        b = b.round( new MathContext( 2 ) );
        return b.floatValue();
    }

    private float roundDouble( double value ) {
        BigDecimal b = new BigDecimal( value );
        b = b.round( new MathContext( 2 ) );
        return b.floatValue();
    }

    public FootprintPoint[] getPixelCoordinates() {
        return pixelCoordinates;
    }

    public Point3Values getLastFootprintPoint() {
        return lastFootprintPoint;
    }

    public void setLastFootprintPoint( FootprintPoint lastFootprintPoint ) {
        this.lastFootprintPoint = new Point3Values( lastFootprintPoint );
    }

    public void addToSelectedPoints( Point3Values point ) {

        selectedPoints.add( point );

    }

    public List<Point3Values> getSelectedPoints() {
        return selectedPoints;
    }

    private void updateSelectedPoints() {
        FootprintPoint point = null;
        List<Point3Values> selectedPointsTemp = new ArrayList<Point3Values>();
        for ( Point3Values p : selectedPoints ) {
            point = new FootprintPoint( ( p.getInitialValue().getX() / initialResolution ) * size,
                                        ( p.getInitialValue().getY() / initialResolution ) * size );
            selectedPointsTemp.add( new Point3Values( p.getNewValue(), p.getInitialValue(), point ) );
        }
        selectedPoints = selectedPointsTemp;
        double x = lastFootprintPoint.getInitialValue().getX() / initialResolution;
        double y = lastFootprintPoint.getInitialValue().getY() / initialResolution;
        double x1 = roundDouble( x * resizing );
        double y1 = roundDouble( y * resizing );
        FootprintPoint pi = getClosestPoint( new FootprintPoint( lastFootprintPoint.getNewValue().getX() + x1,
                                                                 lastFootprintPoint.getNewValue().getY() + y1 ) );

        lastFootprintPoint.setNewValue( new FootprintPoint( pi.getX(), pi.getY() ) );

    }

}
