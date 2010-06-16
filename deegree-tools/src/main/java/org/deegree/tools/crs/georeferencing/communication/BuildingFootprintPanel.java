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
import java.util.Map;

import javax.swing.JPanel;

import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;

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

    private int xValue = 0;

    private int yValue = 0;

    private Map<AbstractGRPoint, AbstractGRPoint> points;

    /**
     * Temporal point
     */
    private AbstractGRPoint tempPoint;

    private Polygon polygon;

    private boolean focus;

    private final Insets insets = new Insets( 10, 10, 0, 0 );

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
        if ( polygon != null ) {
            g2.drawPolygon( polygon );

        }

        if ( tempPoint != null ) {
            g2.fillOval( (int) tempPoint.x - 5, (int) tempPoint.y - 5, 10, 10 );
        }

        if ( points != null ) {
            for ( AbstractGRPoint point : points.keySet() ) {
                g2.fillOval( (int) point.x - 5, (int) point.y - 5, 10, 10 );
            }
        }

    }

    public int getXValue() {
        return xValue;
    }

    public int getYValue() {
        return yValue;
    }

    public void setXValue( int x ) {
        xValue = x;
    }

    public void setYValue( int y ) {
        yValue = y;
    }

    @Override
    public Insets getInsets() {
        return insets;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon( Polygon polygon ) {
        try {
            // org.deegree.geometry.primitive.Polygon pol = (org.deegree.geometry.primitive.Polygon) geometry;
            // Ring ring = pol.getExteriorRing();
            // Points points = ring.getControlPoints();

            this.polygon = polygon;// new Polygon( new int[] { 50, 50, 200, 200 }, new int[] { 50, 250, 200, 80 }, 4 );
        } catch ( ClassCastException e ) {
            System.err.println( "No Polygon provided: " + e.getMessage() );
        }
    }

    public void addScene2DMouseListener( MouseListener m ) {

        this.addMouseListener( m );

    }

    public void addPoint( Map<AbstractGRPoint, AbstractGRPoint> points, AbstractGRPoint tempPoint ) {
        this.points = points;
        this.tempPoint = tempPoint;
    }

    public void setGeometry( RasterRect rect ) {
        this.polygon = new Polygon( new int[] { rect.x, rect.x, rect.width, rect.width }, new int[] { rect.y,
                                                                                                     rect.height,
                                                                                                     rect.height,
                                                                                                     rect.y }, 4 );

    }

    public void setFocus( boolean focus ) {
        this.focus = focus;
    }

    public boolean getFocus() {
        return focus;
    }

}
