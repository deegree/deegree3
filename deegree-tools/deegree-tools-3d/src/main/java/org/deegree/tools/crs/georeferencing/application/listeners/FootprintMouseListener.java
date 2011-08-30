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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.communication.panel2D.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;

/**
 * 
 * Controls the MouseListener
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FootprintMouseListener extends MouseAdapter {

    private ApplicationState state;

    public FootprintMouseListener( ApplicationState state ) {
        this.state = state;
    }

    @Override
    public void mouseEntered( MouseEvent m ) {
        state.mouseFootprint.setMouseInside( true );
    }

    @Override
    public void mouseExited( MouseEvent m ) {
        Object source = m.getSource();
        if ( source instanceof JPanel ) {
            if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                state.mouseFootprint.setMouseInside( false );

            }
        }
    }

    @Override
    public void mousePressed( MouseEvent m ) {
        state.mouseFootprint.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
        state.isControlDown = m.isControlDown();
    }

    @Override
    public void mouseReleased( MouseEvent m ) {
        boolean isFirstNumber = false;
        if ( state.isControlDown || state.zoomIn || state.zoomOut ) {
            Point2d pointPressed = new Point2d( state.mouseFootprint.getPointMousePressed().x,
                                                state.mouseFootprint.getPointMousePressed().y );
            Point2d pointReleased = new Point2d( m.getX(), m.getY() );
            Point2d minPoint;
            Point2d maxPoint;
            if ( pointPressed.x < pointReleased.x ) {
                minPoint = pointPressed;
                maxPoint = pointReleased;
            } else {
                minPoint = pointReleased;
                maxPoint = pointPressed;
            }

            if ( state.zoomIn ) {
                if ( minPoint.x == maxPoint.x && minPoint.y == maxPoint.y ) {
                    state.sceneValues.computeZoomedEnvelope( true,
                                                             state.conModel.getDialogModel().getResizeValue().second,
                                                             new FootprintPoint( minPoint.x, minPoint.y ) );
                } else {
                    Rectangle r = new Rectangle( new Double( minPoint.x ).intValue(),
                                                 new Double( minPoint.y ).intValue(),
                                                 Math.abs( new Double( maxPoint.x - minPoint.x ).intValue() ),
                                                 Math.abs( new Double( maxPoint.y - minPoint.y ).intValue() ) );

                    state.sceneValues.createZoomedEnvWithMinPoint( PointType.FootprintPoint, r );
                }
            } else if ( state.zoomOut ) {
                state.sceneValues.computeZoomedEnvelope( false,
                                                         state.conModel.getDialogModel().getResizeValue().second,
                                                         new FootprintPoint( maxPoint.x, maxPoint.y ) );
            }
            state.conModel.getFootPanel().setZoomRect( null );
            state.conModel.getFootPanel().updatePoints( state.sceneValues );
            state.conModel.getFootPanel().repaint();

        } else {
            if ( state.referencing && !state.referencingLeft ) {

                if ( state.start == false ) {
                    state.start = true;
                    state.conModel.getFootPanel().setFocus( true );
                    state.conModel.getPanel().setFocus( false );
                }
                if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                     && state.conModel.getPanel().getLastAbstractPoint() != null
                     && state.conModel.getFootPanel().getFocus() == true ) {
                    state.setValues();
                }
                if ( state.conModel.getFootPanel().getLastAbstractPoint() == null
                     && state.conModel.getPanel().getLastAbstractPoint() == null
                     && state.conModel.getFootPanel().getFocus() == true ) {
                    state.tablePanel.addRow();
                    isFirstNumber = true;
                }
                double x = m.getX();
                double y = m.getY();
                Pair<AbstractGRPoint, FootprintPoint> point = null;
                if ( state.conModel.getDialogModel().getSnappingOnOff().first ) {
                    point = state.conModel.getFootPanel().getClosestPoint( new FootprintPoint( x, y ) );
                } else {
                    point = new Pair<AbstractGRPoint, FootprintPoint>(
                                                                       new FootprintPoint( x, y ),
                                                                       (FootprintPoint) state.sceneValues.getWorldPoint( new FootprintPoint(
                                                                                                                                             x,
                                                                                                                                             y ) ) );
                }
                state.rc = state.tablePanel.setCoords( point.second );
                state.conModel.getFootPanel().setLastAbstractPoint( point.first, point.second, state.rc );
                state.referencingLeft = true;
                if ( isFirstNumber == false ) {
                    state.updateResidualsWithLastAbstractPoint();
                }

            } else if ( state.pan ) {
                state.mouseFootprint.setMouseChanging( new FootprintPoint(
                                                                           ( state.mouseFootprint.getPointMousePressed().x - m.getX() ),
                                                                           ( state.mouseFootprint.getPointMousePressed().y - m.getY() ) ) );

                state.sceneValues.moveEnvelope( state.mouseFootprint.getMouseChanging() );
                state.conModel.getFootPanel().updatePoints( state.sceneValues );
            }
            state.conModel.getFootPanel().repaint();
        }
    }

}
