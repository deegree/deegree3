//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.tools.crs.georeferencing.application.listeners;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.communication.panel2D.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.panel2D.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;

/**
 * 
 * Registeres the mouseMotion in the component.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DMouseMotionListener implements MouseMotionListener {

    private ApplicationState state;

    public Scene2DMouseMotionListener( ApplicationState state ) {
        this.state = state;
    }

    @Override
    public void mouseDragged( MouseEvent m ) {

        Object source = m.getSource();

        if ( source instanceof JPanel ) {
            // Scene2DPanel
            if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                if ( state.zoomIn ) {
                    state.mapController.setZoomRectEnd( m.getX(), m.getY() );
                    state.conModel.getPanel().repaint();
                }
                if ( state.pan ) {
                    state.mapController.updatePanning( m.getX(), m.getY() );
                    state.conModel.getPanel().repaint();
                }
            }
            // footprintPanel
            if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                if ( m.isControlDown() || state.zoomIn ) {
                    int x = new Double( state.mouseFootprint.getPointMousePressed().x ).intValue();
                    int y = new Double( state.mouseFootprint.getPointMousePressed().y ).intValue();
                    int width = new Double( m.getX() - state.mouseFootprint.getPointMousePressed().x ).intValue();
                    int height = new Double( m.getY() - state.mouseFootprint.getPointMousePressed().y ).intValue();
                    Rectangle rec = new Rectangle( x, y, width, height );
                    state.conModel.getFootPanel().setZoomRect( rec );
                    state.conModel.getFootPanel().repaint();
                }
            }
        }

    }

    @Override
    public void mouseMoved( MouseEvent m ) {

        Object source = m.getSource();

        if ( source instanceof JPanel ) {
            // Scene2DPanel
            if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                // System.out.println( m.getPoint() );
                if ( state.mouseGeoRef != null ) {
                    state.mouseGeoRef.setMouseMoved( new GeoReferencedPoint( m.getX(), m.getY() ) );
                }
            }
            // footprintPanel
            if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                // System.out.println( m.getPoint() );
                if ( state.mouseFootprint != null ) {
                    state.mouseFootprint.setMouseMoved( new FootprintPoint( m.getX(), m.getY() ) );
                }
            }
        }
    }

}
