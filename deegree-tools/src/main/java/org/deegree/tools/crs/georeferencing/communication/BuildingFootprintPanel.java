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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.vecmath.Point2d;

import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;

/**
 * 
 * Panel for drawing the footprints of an imported geometry.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BuildingFootprintPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public final static String BUILDINGFOOTPRINT_PANEL_NAME = "BuildingFootprintPanel";

    private Map<FootprintPoint, FootprintPoint> points;

    /**
     * Temporal point
     */
    private AbstractGRPoint tempPoint;

    private List<Polygon> polygonList;

    private boolean focus;

    private Point2d cumTranslationPoint;

    private final Insets insets = new Insets( 10, 10, 0, 0 );

    private boolean isTranslated;

    /**
     * 
     */
    public BuildingFootprintPanel() {
        this.setName( BUILDINGFOOTPRINT_PANEL_NAME );
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
                System.out.println( "i'm to draw polygon" );

            }
        }
        if ( points != null ) {
            for ( FootprintPoint point : points.keySet() ) {
                g2.fillOval( (int) point.getX() - 5, (int) point.getY() - 5, 10, 10 );
            }
        }
        // if ( tempPoint != null ) {
        // if ( isTranslated == false ) {
        //
        // Point2d p = new Point2d( tempPoint.x - 5, tempPoint.y - 5 );
        //
        // g2.fillOval( (int) p.x, (int) p.y, 10, 10 );
        //
        // }
        // }
        g2.translate( cumTranslationPoint.x, cumTranslationPoint.y );
        System.out.println( "TranslationPoint: " + cumTranslationPoint );

        // if ( tempPoint != null ) {
        // if ( isTranslated == true ) {
        // g2.fillOval( (int) ( tempPoint.x - 5 ), (int) ( tempPoint.y - 5 ), 10, 10 );
        //
        // }
        // }
    }

    @Override
    public Insets getInsets() {
        return insets;
    }

    public List<Polygon> getPolygonList() {
        return polygonList;
    }

    public void setPolygonList( List<Polygon> polygonList ) {
        this.polygonList = polygonList;
    }

    public void addScene2DMouseListener( MouseListener m ) {

        this.addMouseListener( m );

    }

    public void addScene2DMouseMotionListener( MouseMotionListener m ) {
        this.addMouseMotionListener( m );
    }

    public void addScene2DMouseWheelListener( MouseWheelListener m ) {
        this.addMouseWheelListener( m );
    }

    // public void addPoint( Map<AbstractGRPoint, AbstractGRPoint> points, AbstractGRPoint tempPoint ) {
    // this.points = points;
    // this.tempPoint = tempPoint;
    // }

    public void setPoints( Map<FootprintPoint, FootprintPoint> points ) {
        this.points = points;
    }

    public void setFocus( boolean focus ) {
        this.focus = focus;
    }

    public boolean getFocus() {
        return focus;
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

}
