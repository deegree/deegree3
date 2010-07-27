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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.geometry.primitive.Ring;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
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

    private List<Ring> worldPolygonList;

    private final Insets insets = new Insets( 0, 10, 0, 0 );

    private Map<FootprintPoint, FootprintPoint> pointsPixelToWorld;

    private ArrayList<Polygon> polygonListTranslated;

    /**
     * 
     */
    public BuildingFootprintPanel() {
        this.setName( BUILDINGFOOTPRINT_PANEL_NAME );

        this.selectedPoints = new ArrayList<Point4Values>();
    }

    @Override
    protected void paintComponent( Graphics g ) {

        super.paintComponent( g );
        Graphics2D g2 = (Graphics2D) g;

        if ( polygonList != null ) {
            for ( Polygon polygon : polygonList ) {
                g2.drawPolygon( polygon );
            }
        }

        if ( zoomRect != null ) {
            int x = new Double( zoomRect.getX() ).intValue();
            int y = new Double( zoomRect.getY() ).intValue();
            int width = new Double( zoomRect.getWidth() ).intValue();
            int height = new Double( zoomRect.getHeight() ).intValue();

            g2.drawRect( x, y, width, height );
        }

        if ( selectedPoints != null ) {
            for ( Point4Values point : selectedPoints ) {
                g2.fillOval( new Double( point.getNewValue().getX() ).intValue() - selectedPointSize,
                             new Double( point.getNewValue().getY() ).intValue() - selectedPointSize,
                             selectedPointSize * 2, selectedPointSize * 2 );
            }
        }
        if ( lastAbstractPoint != null ) {

            Point2d p = new Point2d( lastAbstractPoint.getNewValue().getX() - selectedPointSize,
                                     lastAbstractPoint.getNewValue().getY() - selectedPointSize );

            g2.fillOval( new Double( p.x ).intValue(), new Double( p.y ).intValue(), selectedPointSize * 2,
                         selectedPointSize * 2 );
        }
    }

    @Override
    public Insets getInsets() {
        return insets;
    }

    public List<Polygon> getPolygonList() {
        return polygonList;
    }

    @Override
    public void setPolygonList( List<Ring> polygonRing, Scene2DValues sceneValues ) {

        if ( polygonRing != null ) {
            this.worldPolygonList = polygonRing;
            polygonListTranslated = new ArrayList<Polygon>();
            pointsPixelToWorld = new HashMap<FootprintPoint, FootprintPoint>();
            int sizeOfPoints = 0;
            for ( Ring p : polygonRing ) {
                sizeOfPoints += p.getControlPoints().size();

            }
            for ( Ring ring : polygonRing ) {
                int[] x2 = new int[ring.getControlPoints().size()];
                int[] y2 = new int[ring.getControlPoints().size()];
                for ( int i = 0; i < ring.getControlPoints().size(); i++ ) {
                    double x = ring.getControlPoints().getX( i );
                    double y = ring.getControlPoints().getY( i );
                    int[] p = sceneValues.getPixelCoord( new FootprintPoint( x, y ) );
                    x2[i] = p[0];
                    y2[i] = p[1];

                    pointsPixelToWorld.put( new FootprintPoint( x2[i], y2[i] ),
                                            new FootprintPoint( ring.getControlPoints().getX( i ),
                                                                ring.getControlPoints().getY( i ) ) );

                }
                Polygon p = new Polygon( x2, y2, ring.getControlPoints().size() );
                polygonListTranslated.add( p );

            }

            this.polygonList = polygonListTranslated;
        } else {
            this.polygonList = null;
        }

    }

    /**
     * Determines the closest point of the point2d in worldCoordinate.
     * 
     * @param point2d
     *            the specified point
     * @return a Pair of <Code>AbstractPoint</Code> in pixelCoordinates and <Code>FootprintPoint</Code> in
     *         worldCoordinates that is the closest point to point2d
     */
    public Pair<AbstractGRPoint, FootprintPoint> getClosestPoint( AbstractGRPoint point2d ) {
        Pair<AbstractGRPoint, FootprintPoint> closestPoint = new Pair<AbstractGRPoint, FootprintPoint>();

        if ( pointsPixelToWorld.size() != 0 ) {
            double distance = -1.0;

            for ( Point2d point : pointsPixelToWorld.keySet() ) {
                if ( distance == -1.0 ) {
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

    @Override
    public void updatePoints( Scene2DValues sceneValues ) {

        if ( worldPolygonList != null ) {

            setPolygonList( worldPolygonList, sceneValues );
        }

        updateSelectedPoints( sceneValues );

    }

    private void updateSelectedPoints( Scene2DValues sceneValues ) {
        List<Point4Values> selectedPointsTemp = new ArrayList<Point4Values>();
        for ( Point4Values p : selectedPoints ) {
            int[] pValues = sceneValues.getPixelCoord( p.getWorldCoords() );
            double x = pValues[0];
            double y = pValues[1];
            FootprintPoint pi = new FootprintPoint( x, y );
            selectedPointsTemp.add( new Point4Values( p.getNewValue(), p.getInitialValue(), pi, p.getWorldCoords() ) );
        }
        selectedPoints = selectedPointsTemp;
        if ( lastAbstractPoint != null ) {

            int[] pValues = sceneValues.getPixelCoord( lastAbstractPoint.getWorldCoords() );
            double x = pValues[0];
            double y = pValues[1];

            FootprintPoint pi = new FootprintPoint( x, y );
            lastAbstractPoint.setNewValue( new FootprintPoint( pi.getX(), pi.getY() ) );

            System.out.println( "[BuildingFootprintPanel] " + lastAbstractPoint );
        }
    }

}
