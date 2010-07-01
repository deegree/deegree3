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
package org.deegree.tools.crs.georeferencing.communication;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;

/**
 * 
 * Panel for drawing the footprints of an imported geometry.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BuildingFootprintPanel extends AbstractPanel2D {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public final static String BUILDINGFOOTPRINT_PANEL_NAME = "BuildingFootprintPanel";

    private List<Polygon> polygonList;

    private List<Polygon> worldPolygonList;

    private FootprintPoint[] pixelCoordinates;

    private int offset;

    private List<Polygon> pixelCoordinatePolygonList;

    private Point2d cumTranslationPoint;

    private final Insets insets = new Insets( 0, 10, 0, 0 );

    private boolean isTranslated;

    private float resolution;

    private float resizing;

    private float initialResolution;

    private Map<FootprintPoint, FootprintPoint> pointsPixelToWorld;

    /**
     * 
     */
    public BuildingFootprintPanel( float initialResolution ) {
        this.setName( BUILDINGFOOTPRINT_PANEL_NAME );
        pointsPixelToWorld = new HashMap<FootprintPoint, FootprintPoint>();
        this.initialResolution = initialResolution;
        this.selectedPoints = new ArrayList<Point4Values>();
    }

    @Override
    protected void paintComponent( Graphics g ) {

        super.paintComponent( g );
        Graphics2D g2 = (Graphics2D) g;

        if ( cumTranslationPoint == null ) {
            cumTranslationPoint = new Point2d( 0.0, 0.0 );
        }

        g2.translate( -cumTranslationPoint.x, -cumTranslationPoint.y );

        if ( polygonList != null ) {
            for ( Polygon polygon : polygonList ) {
                g2.drawPolygon( polygon );
            }
        }
        if ( points != null ) {
            for ( Pair<Point4Values, Point4Values> point : points ) {
                g2.fillOval( (int) point.first.getNewValue().getX() - 5, (int) point.first.getNewValue().getY() - 5,
                             10, 10 );
            }
        }
        if ( lastAbstractPoint != null ) {
            if ( isTranslated == false ) {

                Point2d p = new Point2d( lastAbstractPoint.getNewValue().getX() - 5,
                                         lastAbstractPoint.getNewValue().getY() - 5 );

                g2.fillOval( (int) p.x, (int) p.y, 10, 10 );

            }
        }

        g2.translate( cumTranslationPoint.x, cumTranslationPoint.y );

        System.out.println( "TranslationPoint: " + cumTranslationPoint );

        if ( lastAbstractPoint != null ) {
            if ( isTranslated == true ) {
                g2.fillOval( (int) ( lastAbstractPoint.getNewValue().getX() - 5 ),
                             (int) ( lastAbstractPoint.getNewValue().getY() - 5 ), 10, 10 );

            }
        }
    }

    @Override
    public Insets getInsets() {
        return insets;
    }

    public List<Polygon> getPolygonList() {
        return polygonList;
    }

    public void setPolygonList( List<Polygon> polygonList ) {
        this.worldPolygonList = polygonList;
        if ( this.resolution == 0.0f ) {
            this.resolution = 4.0f;
        }
        System.out.println( "[Footprint] Resize: " + resolution );
        pixelCoordinatePolygonList = new ArrayList<Polygon>();
        List<Rectangle> rect = new ArrayList<Rectangle>();

        int sizeOfPoints = 0;
        for ( Polygon p : polygonList ) {
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
        for ( Polygon po : polygonList ) {
            int[] x2 = new int[po.npoints];
            int[] y2 = new int[po.npoints];
            for ( int i = 0; i < po.npoints; i++ ) {
                x2[i] = (int) ( ( po.xpoints[i] - x ) * resolution );
                y2[i] = (int) ( ( po.ypoints[i] - y ) * resolution );
                pixelCoordinates[counter++] = new FootprintPoint( ( po.xpoints[i] - x ) * resolution,
                                                                  ( po.ypoints[i] - y ) * resolution );
                pointsPixelToWorld.put( new FootprintPoint( x2[i], y2[i] ), new FootprintPoint( po.xpoints[i],
                                                                                                po.ypoints[i] ) );
                // System.out.println( "[Footprint] Polygon: " + x2[i] );
            }
            Polygon p = new Polygon( x2, y2, po.npoints );
            pixelCoordinatePolygonList.add( p );

        }

        this.polygonList = pixelCoordinatePolygonList;
    }

    public Point2d getCumTranslationPoint() {
        return cumTranslationPoint;
    }

    public void setCumTranslationPoint( Point2d translationPoint ) {
        this.cumTranslationPoint = translationPoint;
    }

    public void setTranslated( boolean isTranslated ) {
        this.isTranslated = isTranslated;
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
                // System.out.println( "[BuildingFootprint] Mapping: " + point + " - " + pointsPixelToWorld.get( point )
                // );
                if ( distance == 0.0 ) {
                    distance = point.distance( point2d );
                    if ( point2d instanceof FootprintPoint ) {
                        closestPoint.first = new FootprintPoint( point.x, point.y );
                        closestPoint.second = new FootprintPoint( pointsPixelToWorld.get( point ).getX(),
                                                                  pointsPixelToWorld.get( point ).getY() );
                    }

                } else {
                    double distanceTemp = point.distance( point2d );
                    if ( distanceTemp < distance ) {
                        distance = distanceTemp;
                        if ( point2d instanceof FootprintPoint ) {
                            closestPoint.first = new FootprintPoint( point.x, point.y );
                            closestPoint.second = new FootprintPoint( pointsPixelToWorld.get( point ).getX(),
                                                                      pointsPixelToWorld.get( point ).getY() );
                        }
                    }
                }
            }
        }

        return closestPoint;
    }

    public FootprintPoint[] getPixelCoordinates() {
        return pixelCoordinates;
    }

    public void updatePoints( Point2d changePoint ) {
        for ( FootprintPoint p : pixelCoordinates ) {
            p.setX( p.getX() - changePoint.x );
            p.setY( p.getY() - changePoint.y );
        }

    }

    @Override
    public void updatePoints( float newSize ) {
        this.resizing = newSize - this.resolution;
        BigDecimal b = new BigDecimal( newSize );
        b = b.round( new MathContext( 2 ) );
        this.resolution = b.floatValue();

        setPolygonList( worldPolygonList );

        updateSelectedPoints();

    }

    public float getResolution() {
        return resolution;
    }

    private void updateSelectedPoints() {
        FootprintPoint point = null;
        List<Point4Values> selectedPointsTemp = new ArrayList<Point4Values>();
        for ( Point4Values p : selectedPoints ) {
            point = new FootprintPoint( ( p.getInitialValue().getX() / initialResolution ) * resolution,
                                        ( p.getInitialValue().getY() / initialResolution ) * resolution );
            selectedPointsTemp.add( new Point4Values( p.getNewValue(), p.getInitialValue(), point, p.getWorldCoords() ) );
        }
        selectedPoints = selectedPointsTemp;
        if ( lastAbstractPoint != null ) {
            double x = lastAbstractPoint.getInitialValue().getX() / initialResolution;
            double y = lastAbstractPoint.getInitialValue().getY() / initialResolution;
            double x1 = roundDouble( x * resizing );
            double y1 = roundDouble( y * resizing );
            FootprintPoint pi = (FootprintPoint) getClosestPoint( new FootprintPoint(
                                                                                      lastAbstractPoint.getNewValue().getX()
                                                                                                              + x1,
                                                                                      lastAbstractPoint.getNewValue().getY()
                                                                                                              + y1 ) ).first;

            lastAbstractPoint.setNewValue( new FootprintPoint( pi.getX(), pi.getY() ) );
        }
    }

}
