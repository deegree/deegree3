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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.vecmath.Point2d;

import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.tools.crs.georeferencing.communication.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.NavigationBarPanel;
import org.deegree.tools.crs.georeferencing.communication.PointTablePanel;
import org.deegree.tools.crs.georeferencing.communication.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.MouseModel;
import org.deegree.tools.crs.georeferencing.model.Scene2D;
import org.deegree.tools.crs.georeferencing.model.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;

/**
 * The <Code>Controller</Code> is responsible to bind the view with the model.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Controller {

    private GRViewerGUI view;

    private Scene2D model;

    private Scene2DPanel panel;

    private Scene2DValues sceneValues;

    private BuildingFootprintPanel footPanel;

    private NavigationBarPanel navPanel;

    private PointTablePanel tablePanel;

    private RasterIOOptions options;

    private Footprint footPrint;

    private MouseModel mouse;

    private Point2d changePoint;

    private boolean isHorizontalRef, start;

    private FootprintPoint lastFootprintPoint;

    private GeoReferencedPoint lastGeoReferencedPoint;

    public Controller( GRViewerGUI view, Scene2D model ) {
        this.view = view;
        this.model = model;
        this.panel = view.getScenePanel2D();
        this.footPanel = view.getFootprintPanel();
        this.navPanel = view.getNavigationPanel();
        this.footPrint = new Footprint();
        this.tablePanel = view.getPointTablePanel();
        this.start = false;
        options = new RasterOptions( view ).getOptions();
        sceneValues = new Scene2DValues( options );
        model.init( options, sceneValues );
        view.addScene2DurlListener( new ButtonListener() );
        view.addHoleWindowListener( new HoleWindowListener() );
        navPanel.addHorizontalRefListener( new ButtonListener() );
        footPanel.addScene2DMouseListener( new Scene2DMouseListener() );
        tablePanel.addHorizontalRefListener( new ButtonListener() );
        tablePanel.addTableModelListener( new TableListener() );

        isHorizontalRef = false;

    }

    /**
     * 
     * Controls the ActionListener
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class ButtonListener implements ActionListener {

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source instanceof JCheckBox ) {
                if ( ( (JCheckBox) source ).getText().startsWith( NavigationBarPanel.HORIZONTAL_REFERENCING ) ) {
                    if ( isHorizontalRef == false ) {
                        isHorizontalRef = true;
                    } else {
                        isHorizontalRef = false;
                    }
                }

            }
            if ( source instanceof JButton ) {
                if ( ( (JButton) source ).getText().startsWith( PointTablePanel.BUTTON_DELETE_SELECTED ) ) {
                    System.out.println( "you clicked on delete selected" );
                    int[] tableRows = tablePanel.getTable().getSelectedRows();

                    for ( int tableRow : tableRows ) {
                        AbstractGRPoint keyPoint = new FootprintPoint(
                                                                       (Double) tablePanel.getModel().getValueAt(
                                                                                                                  tableRow,
                                                                                                                  2 ),
                                                                       (Double) tablePanel.getModel().getValueAt(
                                                                                                                  tableRow,
                                                                                                                  3 ) );

                        if ( footPrint.getMappedPoints().containsKey( keyPoint ) ) {
                            footPrint.removeFromMappedPoints( keyPoint );
                        } else {
                            lastFootprintPoint = null;
                            lastGeoReferencedPoint = null;
                        }

                        tablePanel.removeRow( tableRow );
                    }

                    panel.addPoint( footPrint.getMappedPoints(), lastGeoReferencedPoint );
                    footPanel.addPoint( footPrint.getMappedPoints(), lastFootprintPoint );
                    panel.repaint();
                    footPanel.repaint();
                }
                if ( ( (JButton) source ).getText().startsWith( PointTablePanel.BUTTON_DELETE_ALL ) ) {
                    System.out.println( "you clicked on delete all" );
                    tablePanel.removeAllRows();
                    footPrint.removeAllFromMappedPoints();
                    panel.addPoint( null, null );
                    footPanel.addPoint( null, null );
                    lastFootprintPoint = null;
                    lastGeoReferencedPoint = null;
                    panel.repaint();
                    footPanel.repaint();
                }
                if ( ( (JButton) source ).getText().startsWith( NavigationBarPanel.COMPUTE_BUTTON_NAME ) ) {
                    System.out.println( "you clicked on computation" );
                }
            }
            if ( source instanceof JMenuItem ) {
                if ( ( (JMenuItem) source ).getText().startsWith( GRViewerGUI.MENUITEM_GETMAP ) ) {
                    mouse = new MouseModel();
                    init();
                    panel.addScene2DMouseListener( new Scene2DMouseListener() );
                    // panel.addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
                    panel.addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );

                    footPrint.setDefaultPolygon();
                    // rectangle = new RasterRect( 50, 50, 200, 200 );
                    footPanel.setPolygon( footPrint.getPolygon() );
                    // footPanel.setGeometry( rectangle );
                    footPanel.repaint();
                }
            }

        }
    }

    class TableListener implements TableModelListener {

        @Override
        public void tableChanged( TableModelEvent e ) {

        }

    }

    /**
     * 
     * Controls the MouseListener
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseListener implements MouseListener {

        @Override
        public void mouseClicked( MouseEvent arg0 ) {

        }

        @Override
        public void mouseEntered( MouseEvent arg0 ) {

        }

        @Override
        public void mouseExited( MouseEvent arg0 ) {

        }

        @Override
        public void mousePressed( MouseEvent m ) {
            mouse.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );

        }

        @Override
        public void mouseReleased( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {

                    if ( isHorizontalRef == true ) {
                        if ( start == false ) {
                            start = true;
                            footPanel.setFocus( false );
                            panel.setFocus( true );
                        }
                        if ( lastFootprintPoint != null && lastGeoReferencedPoint != null && panel.getFocus() == true ) {
                            setValues();
                        }
                        int x = m.getX();
                        int y = m.getY();
                        GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                        lastGeoReferencedPoint = geoReferencedPoint;
                        panel.addPoint( footPrint.getMappedPoints(), geoReferencedPoint );
                        GeoReferencedPoint point = (GeoReferencedPoint) sceneValues.getWorldPoint( geoReferencedPoint );
                        tablePanel.setCoords( point );

                    } else {

                        mouse.setMouseChanging( new Point2d( ( mouse.getPointMousePressed().getX() - m.getX() ),
                                                             ( mouse.getPointMousePressed().getY() - m.getY() ) ) );
                        System.out.println( "MouseChanging: " + mouse.getMouseChanging() );

                        // Prediction pred = new Prediction( mouse.getMouseChanging() );
                        // pred.start();

                        mouse.setCumulatedMouseChanging( new Point2d(
                                                                      mouse.getCumulatedMouseChanging().getX()
                                                                                              + mouse.getMouseChanging().getX(),
                                                                      mouse.getCumulatedMouseChanging().getY()
                                                                                              + mouse.getMouseChanging().getY() ) );

                        panel.setBeginDrawImageAtPosition( new Point2d(
                                                                        panel.getBeginDrawImageAtPosition().getX()
                                                                                                - mouse.getMouseChanging().getX(),
                                                                        panel.getBeginDrawImageAtPosition().getY()
                                                                                                - mouse.getMouseChanging().getY() ) );
                        sceneValues.setStartRasterEnvelopePosition( mouse.getMouseChanging() );
                        // 
                        // if the user went into any critical region
                        if ( mouse.getCumulatedMouseChanging().getX() >= sceneValues.getImageMargin().getX()
                             || mouse.getCumulatedMouseChanging().getX() <= -sceneValues.getImageMargin().getX()
                             || mouse.getCumulatedMouseChanging().getY() >= sceneValues.getImageMargin().getY()
                             || mouse.getCumulatedMouseChanging().getY() <= -sceneValues.getImageMargin().getY() ) {

                            Point2d updateDrawImageAtPosition = new Point2d( mouse.getCumulatedMouseChanging().getX(),
                                                                             mouse.getCumulatedMouseChanging().getY() );
                            System.out.println( "updatePos: " + updateDrawImageAtPosition );

                            // panel.setImageToDraw( model.getGeneratedImage() );
                            // sceneValues.setStartRasterEnvelopePosition( updateDrawImageAtPosition );
                            sceneValues.setMinPointPixel( null );
                            panel.setImageToDraw( model.generateSubImage( sceneValues.getImageDimension() ) );
                            mouse.reset();
                            panel.setBeginDrawImageAtPosition( sceneValues.getImageStartPosition() );

                        }

                    }
                    panel.repaint();
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    if ( isHorizontalRef == true ) {

                        if ( start == false ) {
                            start = true;
                            footPanel.setFocus( true );
                            panel.setFocus( false );
                        }
                        if ( lastFootprintPoint != null && lastGeoReferencedPoint != null
                             && footPanel.getFocus() == true ) {
                            setValues();
                        }
                        int x = m.getX();
                        int y = m.getY();
                        FootprintPoint footprintPoint = new FootprintPoint( x, y );
                        FootprintPoint point = (FootprintPoint) footPrint.getClosestPoint( footprintPoint );
                        lastFootprintPoint = point;
                        tablePanel.setCoords( point );
                        footPanel.addPoint( footPrint.getMappedPoints(), point );
                        footPanel.repaint();
                    } else {
                        System.err.println( "not yet implemented." );
                    }

                }
            }
        }

        /**
         * Sets values to the JTableModel and adds a new row to it.
         */
        private void setValues() {
            footPrint.addToMappedPoints( lastFootprintPoint, lastGeoReferencedPoint );
            lastFootprintPoint = null;
            lastGeoReferencedPoint = null;
            tablePanel.addRow();

        }
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

        public void run() {

            changePoint = new Point2d( changing.getX(), changing.getY() );
            System.out.println( "Threadchange: " + changePoint );

            // model.generatePredictedImage( changing );
            // model.generateImage( changing );

        }

    }

    /**
     * 
     * TODO add class documentation here
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged( MouseEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseMoved( MouseEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

    /**
     * 
     * TODO add class documentation here
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseWheelListener implements MouseWheelListener {

        @Override
        public void mouseWheelMoved( MouseWheelEvent m ) {
            // if ( m.getWheelRotation() < 0 ) {
            // panel.setResolutionOfImage( panel.getResolutionOfImage() - .1 );
            // } else {
            // panel.setResolutionOfImage( panel.getResolutionOfImage() + .1 );
            // }
            // model.reset();
            // System.out.println( panel.getResolutionOfImage() );
            //
            // model.setResolution( panel.getResolutionOfImage() );
            // model.init( options, panel.getBounds() );
            // panel.setImageToDraw( model.generateSubImage() );
            // panel.repaint();

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
    class HoleWindowListener implements ComponentListener {

        @Override
        public void componentHidden( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void componentMoved( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void componentResized( ComponentEvent c ) {
            if ( model.getGeneratedImage() != null ) {
                init();

            }

        }

        @Override
        public void componentShown( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

    private void init() {
        // model.reset();
        sceneValues.setImageMargin( new Point2d( panel.getBounds().width * 2.1, panel.getBounds().height * 2.1 ) );
        sceneValues.setImageDimension( new Rectangle(
                                                      (int) ( panel.getBounds().width + 2 * sceneValues.getImageMargin().x ),
                                                      (int) ( panel.getBounds().height + 2 * sceneValues.getImageMargin().y ) ) );
        sceneValues.setImageStartPosition( new Point2d( -sceneValues.getImageMargin().x,
                                                        -sceneValues.getImageMargin().y ) );

        sceneValues.setSize( 1 );
        panel.setBeginDrawImageAtPosition( sceneValues.getImageStartPosition() );
        panel.setImageDimension( sceneValues.getImageDimension() );

        panel.setImageToDraw( model.generateSubImage( sceneValues.getImageDimension() ) );

        panel.repaint();
    }

}
