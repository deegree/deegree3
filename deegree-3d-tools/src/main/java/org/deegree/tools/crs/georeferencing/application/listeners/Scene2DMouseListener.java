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
import java.util.List;

import javax.swing.JPanel;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.primitive.Ring;
import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.communication.panel2D.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.panel2D.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;

/**
 * 
 * Controls the MouseListener
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DMouseListener extends MouseAdapter {

    private ApplicationState state;

    public Scene2DMouseListener( ApplicationState state ) {
        this.state = state;
    }

    @Override
    public void mouseEntered( MouseEvent m ) {
        Object source = m.getSource();
        if ( source instanceof JPanel ) {
            // Scene2DPanel
            if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {

                state.mouseGeoRef.setMouseInside( true );

            }
            if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                state.mouseFootprint.setMouseInside( true );

            }
        }
    }

    @Override
    public void mouseExited( MouseEvent m ) {
        Object source = m.getSource();
        if ( source instanceof JPanel ) {
            // Scene2DPanel
            if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                state.mouseGeoRef.setMouseInside( false );

            }
            if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                state.mouseFootprint.setMouseInside( false );

            }
        }
    }

    @Override
    public void mousePressed( MouseEvent m ) {
        Object source = m.getSource();
        if ( source instanceof JPanel ) {
            // Scene2DPanel
            if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                state.mouseGeoRef.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                state.isControlDown = m.isControlDown();
            }
            if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                state.mouseFootprint.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                state.isControlDown = m.isControlDown();
            }
        }

    }

    @Override
    public void mouseReleased( MouseEvent m ) {
        Object source = m.getSource();
        boolean isFirstNumber = false;
        if ( source instanceof JPanel ) {
            // Scene2DPanel
            if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                if ( state.model != null ) {
                    if ( state.isControlDown || state.zoomIn || state.zoomOut ) {
                        Point2d pointPressed = new Point2d( state.mouseGeoRef.getPointMousePressed().x,
                                                            state.mouseGeoRef.getPointMousePressed().y );
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
                                state.sceneValues.computeZoomedEnvelope(
                                                                         true,
                                                                         state.conModel.getDialogModel().getResizeValue().second,
                                                                         new GeoReferencedPoint( minPoint.x, minPoint.y ) );
                            } else {
                                Rectangle r = new Rectangle(
                                                             new Double( minPoint.x ).intValue(),
                                                             new Double( minPoint.y ).intValue(),
                                                             Math.abs( new Double( maxPoint.x - minPoint.x ).intValue() ),
                                                             Math.abs( new Double( maxPoint.y - minPoint.y ).intValue() ) );

                                state.sceneValues.createZoomedEnvWithMinPoint( PointType.GeoreferencedPoint, r );

                            }
                        } else if ( state.zoomOut ) {
                            state.sceneValues.computeZoomedEnvelope(
                                                                     false,
                                                                     state.conModel.getDialogModel().getResizeValue().second,
                                                                     new GeoReferencedPoint( maxPoint.x, maxPoint.y ) );
                        }

                        state.conModel.getPanel().setImageToDraw(
                                                                  state.model.generateSubImageFromRaster( state.sceneValues.getEnvelopeGeoref() ) );
                        state.conModel.getPanel().updatePoints( state.sceneValues );
                        state.conModel.getPanel().setZoomRect( null );
                        state.conModel.getPanel().repaint();
                    }

                    else {
                        if ( state.referencing ) {
                            if ( state.start == false ) {
                                state.start = true;
                                state.conModel.getFootPanel().setFocus( false );
                                state.conModel.getPanel().setFocus( true );
                            }
                            if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                                 && state.conModel.getPanel().getLastAbstractPoint() != null
                                 && state.conModel.getPanel().getFocus() == true ) {
                                state.setValues();
                            }
                            if ( state.conModel.getFootPanel().getLastAbstractPoint() == null
                                 && state.conModel.getPanel().getLastAbstractPoint() == null
                                 && state.conModel.getPanel().getFocus() == true ) {
                                state.tablePanel.addRow();
                                isFirstNumber = true;
                            }

                            double x = m.getX();
                            double y = m.getY();
                            GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                            GeoReferencedPoint g = (GeoReferencedPoint) state.sceneValues.getWorldPoint( geoReferencedPoint );
                            state.rc = state.tablePanel.setCoords( g );
                            state.conModel.getPanel().setLastAbstractPoint( geoReferencedPoint, g, state.rc );
                            if ( isFirstNumber == false ) {
                                state.updateResidualsWithLastAbstractPoint();
                            }

                            updateTransformation();

                        } else if ( state.pan ) {
                            // just pan
                            state.mouseGeoRef.setMouseChanging( new GeoReferencedPoint(
                                                                                        ( state.mouseGeoRef.getPointMousePressed().x - m.getX() ),
                                                                                        ( state.mouseGeoRef.getPointMousePressed().y - m.getY() ) ) );

                            state.sceneValues.moveEnvelope( state.mouseGeoRef.getMouseChanging() );
                            state.conModel.getPanel().setImageToDraw(
                                                                      state.model.generateSubImageFromRaster( state.sceneValues.getEnvelopeGeoref() ) );
                            state.conModel.getPanel().updatePoints( state.sceneValues );
                        }

                        state.conModel.getPanel().repaint();
                    }

                }
            }
            // footprintPanel
            if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

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
                            state.sceneValues.computeZoomedEnvelope(
                                                                     true,
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
                        state.sceneValues.computeZoomedEnvelope(
                                                                 false,
                                                                 state.conModel.getDialogModel().getResizeValue().second,
                                                                 new FootprintPoint( maxPoint.x, maxPoint.y ) );
                    }
                    state.conModel.getFootPanel().setZoomRect( null );
                    state.conModel.getFootPanel().updatePoints( state.sceneValues );
                    state.conModel.getFootPanel().repaint();

                } else {
                    if ( state.referencing ) {

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
    }

    void updateTransformation() {
        // swap the tempPoints into the map now
        if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
             && state.conModel.getPanel().getLastAbstractPoint() != null ) {
            state.setValues();
        }

        try {
            state.conModel.setTransform( state.determineTransformationType( state.conModel.getTransformationType() ) );
        } catch ( UnknownCRSException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if ( state.conModel.getTransform() == null ) {
            return;
        }
        List<Ring> polygonRing = state.conModel.getTransform().computeRingList();

        state.updateResiduals( state.conModel.getTransformationType() );

        state.conModel.getPanel().setPolygonList( polygonRing, state.sceneValues );

        state.conModel.getPanel().repaint();

        state.reset();
    }

}
