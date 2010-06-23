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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;

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

    private Map<FootprintPoint, FootprintPoint> pointsPixelToWorld;

    private Map<FootprintPoint, FootprintPoint> selectedPoints;

    private List<Polygon> pixelCoordinatePolygonList;

    private List<Polygon> worldCoordinatePolygonList;

    private int offset;

    private float resize;

    private List<float[]> footprintPointsList;

    /**
     * Creates a new <Code>Footprint</Code> instance.
     */
    public Footprint() {
        pointsPixelToWorld = new HashMap<FootprintPoint, FootprintPoint>();
        selectedPoints = new HashMap<FootprintPoint, FootprintPoint>();
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
    public Pair<AbstractGRPoint, FootprintPoint> getClosestPoint( AbstractGRPoint point2d ) {
        Pair<AbstractGRPoint, FootprintPoint> closestPoint = new Pair<AbstractGRPoint, FootprintPoint>();

        if ( pointsPixelToWorld.size() != 0 ) {
            double distance = 0.0;

            for ( Point2d point : pointsPixelToWorld.keySet() ) {
                System.out.println( "[Footprint] Mapping: " + point + " - " + pointsPixelToWorld.get( point ) );
                if ( distance == 0.0 ) {
                    distance = point.distance( point2d );
                    if ( point2d instanceof FootprintPoint ) {
                        closestPoint.first = new FootprintPoint( point.x, point.y );
                        closestPoint.second = new FootprintPoint( pointsPixelToWorld.get( point ).getX(),
                                                                  pointsPixelToWorld.get( point ).getY() );
                    }
                    // else if ( point2d instanceof GeoReferencedPoint ) {
                    // closestPoint = new GeoReferencedPoint( point.x, point.y );
                    // }

                } else {
                    double distanceTemp = point.distance( point2d );
                    if ( distanceTemp < distance ) {
                        distance = distanceTemp;
                        if ( point2d instanceof FootprintPoint ) {
                            closestPoint.first = new FootprintPoint( point.x, point.y );
                            closestPoint.second = new FootprintPoint( pointsPixelToWorld.get( point ).getX(),
                                                                      pointsPixelToWorld.get( point ).getY() );
                        }
                        // else if ( point2d instanceof GeoReferencedPoint ) {
                        // closestPoint = new GeoReferencedPoint( point.x, point.y );
                        // }
                    }
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
        if ( resize == 0.0f ) {
            resize = 1.0f;
        }
        System.out.println( "[Footprint] Resize: " + resize );
        pixelCoordinatePolygonList = new ArrayList<Polygon>();
        List<Rectangle> rect = new ArrayList<Rectangle>();

        if ( pointsPixelToWorld.size() != 0 ) {
            pointsPixelToWorld = new HashMap<FootprintPoint, FootprintPoint>();
        }

        for ( Polygon p : polyList ) {

            rect.add( p.getBounds() );
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

        for ( Polygon po : polyList ) {
            int[] x2 = new int[po.npoints];
            int[] y2 = new int[po.npoints];
            for ( int i = 0; i < po.npoints; i++ ) {
                x2[i] = (int) ( ( po.xpoints[i] - x ) * resize );
                y2[i] = (int) ( ( po.ypoints[i] - y ) * resize );
                pointsPixelToWorld.put( new FootprintPoint( x2[i], y2[i] ), new FootprintPoint( po.xpoints[i],
                                                                                                po.ypoints[i] ) );
                System.out.println( "[Footprint] Polygon: " + x2[i] );
            }
            Polygon p = new Polygon( x2, y2, po.npoints );
            pixelCoordinatePolygonList.add( p );

        }
        // if(){
        // Map<Point2d, Point2d> pointsPixelToWorldTemp = new HashMap<Point2d, Point2d>();
        // for ( Point2d p : pointsPixelToWorld.keySet() ) {
        // Point2d key = p;
        // Point2d newKey = new Point2d( key.x * changedSize, key.y * changedSize );
        // pointsPixelToWorldTemp.put( newKey, pointsPixelToWorld.get( key ) );
        //
        // }
        // pointsPixelToWorld = new HashMap<Point2d, Point2d>();
        // pointsPixelToWorld.putAll( pointsPixelToWorldTemp );
        // }

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

    public void setOffset( int offset ) {
        this.offset = offset;
    }

    public float getResize() {

        return resize;
    }

    public void setResize( float resize ) {
        this.resize = resize;
    }

    public void updatePoints( Point2d changePoint ) {
        Map<FootprintPoint, FootprintPoint> pointsPixelToWorldTemp = new HashMap<FootprintPoint, FootprintPoint>();
        for ( FootprintPoint p : pointsPixelToWorld.keySet() ) {
            FootprintPoint key = p;
            FootprintPoint newKey = new FootprintPoint( key.getX() - changePoint.x, key.getY() - changePoint.y );
            pointsPixelToWorldTemp.put( newKey, pointsPixelToWorld.get( key ) );

        }
        pointsPixelToWorld = new HashMap<FootprintPoint, FootprintPoint>();
        pointsPixelToWorld.putAll( pointsPixelToWorldTemp );

    }

    public void updatePoints( float changedSize ) {

        generateFootprints( footprintPointsList );

    }

    public Map<FootprintPoint, FootprintPoint> getSelectedPoints() {
        return selectedPoints;
    }

    public void setSelectedPoints( Map<FootprintPoint, FootprintPoint> selectedPoints ) {
        this.selectedPoints = selectedPoints;
    }

    public void addPointToSelectedPointsList( FootprintPoint point ) {
        selectedPoints.put( point, pointsPixelToWorld.get( point ) );

    }

    public void changePointInSelectedPointsList( FootprintPoint point ) {
        FootprintPoint newKey = null;
        FootprintPoint newValue = null;
        for ( FootprintPoint fp : selectedPoints.keySet() ) {
            if ( point == fp ) {
                newKey = point;
                newValue = pointsPixelToWorld.get( point );
                selectedPoints.remove( fp );
                break;
            }
        }
        if ( newKey != null && newValue != null ) {
            selectedPoints.put( newKey, newValue );
        }
    }

    public void removePointFromSelectedPointsList( FootprintPoint point ) {
        for ( FootprintPoint fp : selectedPoints.keySet() ) {
            if ( point == fp ) {
                selectedPoints.remove( fp );
                break;
            }
        }
    }

}
