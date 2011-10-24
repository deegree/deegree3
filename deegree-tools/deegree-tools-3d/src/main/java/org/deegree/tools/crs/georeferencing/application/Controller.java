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
package org.deegree.tools.crs.georeferencing.application;

import static org.deegree.tools.crs.georeferencing.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Method;
import java.util.EventListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Triple;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tools.crs.georeferencing.application.listeners.ButtonListener;
import org.deegree.tools.crs.georeferencing.application.transformation.AbstractTransformation;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;
import org.deegree.tools.crs.georeferencing.communication.checkboxlist.CheckboxListTransformation;
import org.deegree.tools.crs.georeferencing.communication.panel2D.AbstractPanel2D;
import org.deegree.tools.crs.georeferencing.model.CheckBoxListModel;
import org.deegree.tools.crs.georeferencing.model.ControllerModel;
import org.deegree.tools.crs.georeferencing.model.dialog.OptionDialogModel;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;
import org.slf4j.Logger;

/**
 * The <Code>Controller</Code> is responsible to bind the view with the model.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Controller {

    static final Logger LOG = getLogger( Controller.class );

    ApplicationState state;

    private GeometryFactory geom;

    public Controller( GRViewerGUI view, ApplicationState state ) {
        this.state = state;
        this.geom = new GeometryFactory();
        state.sceneValues = new Scene2DValues( this.geom );

        state.conModel = new ControllerModel( view, view.getFootprintPanel(), view.getScenePanel2D(),
                                              new OptionDialogModel() );

        state.start = false;

        state.glHandler = view.getOpenGLEventListener();
        AbstractPanel2D.selectedPointSize = state.conModel.getDialogModel().getSelectionPointSize().first;
        AbstractPanel2D.zoomValue = state.conModel.getDialogModel().getResizeValue().first;

        view.addListeners( new ButtonListener( state ) );
        view.addHoleWindowListener( new HoleWindowListener( state ) );

        // init the Checkboxlist for Transformation
        state.modelTransformation = new CheckBoxListModel();
        state.checkBoxListTransform = new CheckboxListTransformation( state.modelTransformation );
        view.addToMenuTransformation( state.checkBoxListTransform );
        state.checkBoxListTransform.addCheckboxListener( new ButtonListener( state ) );

        // init the transformation method
        state.tablePanel = new PointTableFrame();
        state.tablePanel.getSaveButton().setEnabled( false );
        state.tablePanel.getLoadButton().setEnabled( false );
        state.tablePanel.addTableModelListener( new TableChangedEventListener() );
        state.tablePanel.addActionButtonListener( new ButtonListener( state ) );
        // transform = null;
        if ( state.conModel.getTransformationType() == null ) {
            for ( JCheckBox box : state.modelTransformation.getList() ) {
                if ( ( box ).getText().startsWith( get( "MENUITEM_TRANS_HELMERT" ) ) ) {
                    state.conModel.setTransformationType( AbstractTransformation.TransformationType.Helmert_4 );
                    view.activateTransformationCheckbox( box );
                    break;
                }
            }

        }

    }

    public ApplicationState getState() {
        return this.state;
    }

    /**
     * Removes all the listeners of one component. http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4380536
     * 
     * @param comp
     */
    static void removeListeners( Component comp ) {
        Method[] methods = comp.getClass().getMethods();
        for ( int i = 0; i < methods.length; i++ ) {
            Method method = methods[i];
            String name = method.getName();
            if ( name.startsWith( "remove" ) && name.endsWith( "Listener" ) ) {

                Class<EventListener>[] params = (Class<EventListener>[]) method.getParameterTypes();
                if ( params.length == 1 ) {
                    EventListener[] listeners = null;
                    try {
                        listeners = comp.getListeners( params[0] );
                    } catch ( Exception e ) {
                        // It is possible that someone could create a listener
                        // that doesn't extend from EventListener. If so,
                        // ignore it
                        System.out.println( "Listener " + params[0] + " does not extend EventListener" );
                        continue;
                    }
                    for ( int j = 0; j < listeners.length; j++ ) {
                        try {
                            method.invoke( comp, new Object[] { listeners[j] } );
                            // System.out.println("removed Listener " + name + "for comp " + comp + "\n");
                        } catch ( Exception e ) {
                            System.out.println( "Cannot invoke removeListener method " + e );
                            // Continue on. The reason for removing all listeners is to
                            // make sure that we don't have a listener holding on to something
                            // which will keep it from being garbage collected. We want to
                            // continue freeing listeners to make sure we can free as much
                            // memory has possible
                        }
                    }
                } else {
                    // The only Listener method that I know of that has more than
                    // one argument is removePropertyChangeListener. If it is
                    // something other than that, flag it and move on.
                    if ( !name.equals( "removePropertyChangeListener" ) )
                        System.out.println( "    Wrong number of Args " + name );
                }
            }
        }
    }

    /**
     * 
     * Controls if there is a change inside the table
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class TableChangedEventListener implements TableModelListener {

        @Override
        public void tableChanged( TableModelEvent e ) {

            int row = e.getFirstRow();
            int column = e.getColumn();
            TableModel model = (TableModel) e.getSource();

            if ( column != -1 && model.getValueAt( row, column ) != null ) {
                Object data = model.getValueAt( row, column );

                if ( row == e.getLastRow() ) {
                    Triple<Point4Values, Point4Values, PointResidual> newLastPair;
                    newLastPair = new Triple<Point4Values, Point4Values, PointResidual>(
                                                                                         Controller.this.state.conModel.getFootPanel().getLastAbstractPoint(),
                                                                                         Controller.this.state.conModel.getPanel().getLastAbstractPoint(),
                                                                                         null );
                    if ( Controller.this.state.conModel.getFootPanel().getLastAbstractPoint() != null
                         && Controller.this.state.conModel.getPanel().getLastAbstractPoint() != null ) {
                        boolean changed = Controller.this.changePointLocation( newLastPair, data, row, column );
                        if ( changed ) {

                            if ( Controller.this.state.conModel.getFootPanel().getLastAbstractPoint() != null
                                 && Controller.this.state.conModel.getPanel().getLastAbstractPoint() != null ) {
                                Controller.this.state.conModel.getPanel().setLastAbstractPoint( newLastPair.second.getNewValue(),
                                                                                                newLastPair.second.getWorldCoords(),
                                                                                                newLastPair.second.getRc() );
                                Controller.this.state.conModel.getFootPanel().setLastAbstractPoint( newLastPair.first.getNewValue(),
                                                                                                    newLastPair.first.getWorldCoords(),
                                                                                                    newLastPair.first.getRc() );
                            }
                            Controller.this.state.conModel.getPanel().repaint();
                            Controller.this.state.conModel.getFootPanel().repaint();

                        }
                    }
                }

            }
        }

    }

    /**
     * If there is a user change in the pointsTable like modifying a single point.
     * 
     * @param p
     *            that is viewed to be changed, not <Code>null</Code>.
     * @param data
     *            the new data, not <Code>null</Code>.
     * @param row
     *            the row to be changed, not <Code>null</Code>.
     * @param column
     *            the column to be changed, not <Code>null</Code>.
     */
    boolean changePointLocation( Triple<Point4Values, Point4Values, PointResidual> p, Object data, int row, int column ) {
        boolean changed = false;
        AbstractGRPoint pixelValue = null;
        AbstractGRPoint worldCoords = null;
        int rowPoints = p.first.getRc().getRow();
        int fcpx = p.first.getRc().getColumnX();
        int fcpy = p.first.getRc().getColumnY();

        int scpx = p.second.getRc().getColumnX();
        int scpy = p.second.getRc().getColumnY();
        if ( row == rowPoints ) {
            if ( column == fcpx ) {
                worldCoords = new FootprintPoint( new Double( data.toString() ).doubleValue(),
                                                  p.first.getWorldCoords().y );
                int[] i = this.state.sceneValues.getPixelCoord( worldCoords );
                pixelValue = new FootprintPoint( i[0], i[1] );

                p.first = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.first.getRc() );
                changed = true;
            } else if ( column == fcpy ) {
                worldCoords = new FootprintPoint( p.first.getWorldCoords().x,
                                                  new Double( data.toString() ).doubleValue() );
                int[] i = this.state.sceneValues.getPixelCoord( worldCoords );
                pixelValue = new FootprintPoint( i[0], i[1] );
                p.first = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.first.getRc() );
                changed = true;
            } else if ( column == scpx ) {
                worldCoords = new GeoReferencedPoint( new Double( data.toString() ).doubleValue(),
                                                      p.second.getWorldCoords().y );
                int[] i = this.state.sceneValues.getPixelCoord( worldCoords );
                pixelValue = new GeoReferencedPoint( i[0], i[1] );

                p.second = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.second.getRc() );
                changed = true;
            } else if ( column == scpy ) {
                worldCoords = new GeoReferencedPoint( p.second.getWorldCoords().x,
                                                      new Double( data.toString() ).doubleValue() );
                int[] i = this.state.sceneValues.getPixelCoord( worldCoords );
                pixelValue = new GeoReferencedPoint( i[0], i[1] );

                p.second = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.second.getRc() );
                changed = true;
            }

        }
        return changed;
    }

    /**
     * 
     * The <Code>Prediction</Code> class should handle the getImage in the background.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Prediction extends Thread {

        Point2d changing;

        public Prediction( Point2d changing ) {
            this.changing = changing;

        }

        @Override
        public void run() {

            Controller.this.state.changePoint = new Point2d( this.changing.x, this.changing.y );

            // model.generatePredictedImage( changing );
            // model.generateImage( changing );

        }

    }

    /**
     * 
     * Controls the ComponentListener
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    static class HoleWindowListener extends ComponentAdapter {

        private ApplicationState state;

        HoleWindowListener( ApplicationState state ) {
            this.state = state;
        }

        @Override
        public void componentResized( ComponentEvent c ) {
            Object source = c.getSource();

            if ( source instanceof JFrame ) {
                if ( this.state.mapController != null ) {
                    if ( this.state.sceneValues != null ) {
                        this.state.mapController.setSize( this.state.conModel.getPanel().getBounds().width,
                                                          this.state.conModel.getPanel().getBounds().height );
                        this.state.sceneValues.setEnvelopeGeoref( state.mapController.getCurrentEnvelope() );
                        state.sceneValues.setGeorefDimension( state.conModel.getPanel().getBounds() );
                        this.state.conModel.getFootPanel().updatePoints( this.state.sceneValues );
                        this.state.conModel.getFootPanel().repaint();
                    }

                }
            }

        }

    }

    public ControllerModel getConModel() {
        return this.state.conModel;
    }

}
