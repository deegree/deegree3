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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.vecmath.Point2d;

import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.geometry.Envelope;
import org.deegree.tools.crs.georeferencing.communication.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.NavigationBarPanel;
import org.deegree.tools.crs.georeferencing.communication.PointTablePanel;
import org.deegree.tools.crs.georeferencing.communication.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.AbstractPoint;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.MouseModel;
import org.deegree.tools.crs.georeferencing.model.Scene2D;

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

    private BuildingFootprintPanel footPanel;

    private NavigationBarPanel navPanel;

    private PointTablePanel tablePanel;

    private MouseModel mouse;

    private RasterIOOptions options;

    private Point2d changePoint;

    private RasterRect rect;

    private RasterRect rectangle;

    private boolean isHorizontalRef;

    private int start;

    private int pointCounterFootprint;

    private Vector<AbstractPoint> tableValueGeoRef;

    private Vector<AbstractPoint> tableValueFootPrint;

    private FootprintPoint lastFootprintPoint;

    private GeoReferencedPoint lastGeoReferencedPoint;

    // /**
    // * idicates the initialization of the component. If one click first on the footprintPanel, then there is the
    // * initalization. Here one get the information to start a new point running.
    // */
    // private boolean isInitialized;

    private Footprint footPrint;

    private static final String RASTERIO_LAYER = "RASTERIO_LAYER";

    private static final String RASTER_FORMATLIST = "RASTER_FORMATLIST";

    private static final String RASTER_URL = "RASTER_URL";

    private static final String RIO_WMS_SYS_ID = "RASTERIO_WMS_SYS_ID";

    private static final String RIO_WMS_MAX_SCALE = "RASTERIO_WMS_MAX_SCALE";

    private static final String RIO_WMS_DEFAULT_FORMAT = "RASTERIO_WMS_DEFAULT_FORMAT";

    private static final String RIO_WMS_MAX_WIDTH = "RASTERIO_WMS_MAX_WIDTH";

    private static final String RIO_WMS_MAX_HEIGHT = "RASTERIO_WMS_MAX_HEIGHT";

    private static final String RIO_WMS_LAYERS = "RASTERIO_WMS_REQUESTED_LAYERS";

    private static final String RIO_WMS_ENABLE_TRANSPARENT = "RASTERIO_WMS_ENABLE_TRANSPARENCY";

    private static final String RIO_WMS_TIMEOUT = "RIO_WMS_TIMEOUT";

    /**
     * Specifies the size of the full drawn side.
     */
    private static final String RESOLUTION = "RESOLUTION";

    private Envelope bbox;

    public Controller( GRViewerGUI view, Scene2D model ) {
        this.view = view;
        this.model = model;
        panel = view.getScenePanel2D();
        footPanel = view.getFootprintPanel();
        navPanel = view.getNavigationPanel();
        this.footPrint = new Footprint();
        tablePanel = view.getPointTablePanel();
        start = 0;
        tableValueGeoRef = new Vector();
        tableValueFootPrint = new Vector();
        // this.geomFactory = new GeometryFactory();

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
                        System.out.println( "hier sollte ein boolean rein! " + isHorizontalRef );
                    } else {
                        isHorizontalRef = false;
                        System.out.println( "hier sollte ein boolean rein! " + isHorizontalRef );
                    }
                }

            }
            if ( source instanceof JButton ) {
                if ( ( (JButton) source ).getText().startsWith( PointTablePanel.BUTTON_DELETE_SELECTED ) ) {
                    System.out.println( "you clicked on delete selected" );

                }
                if ( ( (JButton) source ).getText().startsWith( PointTablePanel.BUTTON_DELETE_ALL ) ) {
                    System.out.println( "you clicked on delete all" );
                }
            }
            if ( source instanceof JMenuItem ) {
                if ( ( (JMenuItem) source ).getText().startsWith( GRViewerGUI.MENUITEM_GETMAP ) ) {
                    mouse = new MouseModel();
                    model.reset();
                    options = new RasterIOOptions();

                    options.add( RasterIOOptions.CRS, "EPSG:4326" );
                    // options.add( RasterIOOptions.CRS, "EPSG:32618" );
                    // options.add( RIO_WMS_LAYERS, "populationgrid" );
                    options.add( RESOLUTION, "1.0" );
                    options.add( RIO_WMS_LAYERS, "root" );
                    // options.add( RASTER_FORMATLIST, "image/jpeg" );
                    options.add( RASTER_URL, view.openUrl() );
                    options.add( RasterIOOptions.OPT_FORMAT, "WMS_111" );
                    options.add( RIO_WMS_SYS_ID, view.openUrl() );
                    options.add( RIO_WMS_MAX_SCALE, "0.1" );
                    options.add( RIO_WMS_DEFAULT_FORMAT, "image/jpeg" );
                    // specify the quality
                    options.add( RIO_WMS_MAX_WIDTH, Integer.toString( 200 ) );
                    options.add( RIO_WMS_MAX_HEIGHT, Integer.toString( 200 ) );
                    options.add( RIO_WMS_ENABLE_TRANSPARENT, "true" );
                    // options.add( RIO_WMS_TIMEOUT, "1000" );
                    model.setResolution( 1 );
                    model.init( options, panel.getBounds() );
                    panel.setImageToDraw( model.generateImage( null ) );

                    panel.repaint();
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
            TableModel source = (TableModel) e.getSource();
            int first = e.getFirstRow(), last = e.getLastRow();
            // System.out.println( "MyTableChanged :) " + first + " " + last + " " + e.getType() );

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
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {

                    if ( isHorizontalRef == true ) {
                        if ( start == 0 ) {
                            start = 1;
                            footPanel.setFocus( false );
                            panel.setFocus( true );
                        }
                        if ( panel.getFocus() == true ) {
                            setValues();
                        }
                        int x = m.getX();
                        int y = m.getY();
                        GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                        lastGeoReferencedPoint = geoReferencedPoint;
                        panel.addPoint( tableValueGeoRef );
                        tablePanel.setCoords( geoReferencedPoint );
                        panel.repaint();
                    } else {

                        mouse.setMouseChanging( new Point2d( ( mouse.getPointMousePressed().getX() - m.getX() ),
                                                             ( mouse.getPointMousePressed().getY() - m.getY() ) ) );
                        System.out.println( "MouseChanging: " + mouse.getMouseChanging() );

                        Prediction pred = new Prediction( mouse.getMouseChanging() );
                        pred.start();

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
                        // 
                        // if the user went into any critical region
                        if ( mouse.getCumulatedMouseChanging().getX() >= panel.getImageMargin().getX()
                             || mouse.getCumulatedMouseChanging().getX() <= -panel.getImageMargin().getX()
                             || mouse.getCumulatedMouseChanging().getY() >= panel.getImageMargin().getY()
                             || mouse.getCumulatedMouseChanging().getY() <= -panel.getImageMargin().getY() ) {

                            Point2d updateDrawImageAtPosition = new Point2d( mouse.getCumulatedMouseChanging().getX(),
                                                                             mouse.getCumulatedMouseChanging().getY() );
                            System.out.println( "updatePos: " + updateDrawImageAtPosition );

                            panel.setImageToDraw( model.getGeneratedImage() );
                            mouse.reset();
                            panel.reset();

                        }
                        panel.repaint();
                    }
                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    if ( isHorizontalRef == true ) {

                        if ( start == 0 ) {
                            start = 1;
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
                        footPanel.addPoint( tableValueFootPrint );
                        footPanel.repaint();
                    } else {
                        System.err.println( "not implemented yet." );
                    }

                }
            }
        }

        private void setValues() {
            tableValueFootPrint.add( lastFootprintPoint );
            tableValueGeoRef.add( lastGeoReferencedPoint );
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
            model.generateImage( changing );

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
            if ( m.getWheelRotation() < 0 ) {
                panel.setResolutionOfImage( panel.getResolutionOfImage() - .1 );
            } else {
                panel.setResolutionOfImage( panel.getResolutionOfImage() + .1 );
            }
            model.reset();
            System.out.println( panel.getResolutionOfImage() );

            model.setResolution( panel.getResolutionOfImage() );
            model.init( options, panel.getBounds() );
            panel.setImageToDraw( model.generateImage( null ) );
            panel.repaint();

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
                model.init( options, panel.getBounds() );
                panel.setImageToDraw( model.generateImage( null ) );
                panel.repaint();

            }
            panel.setBeginDrawImageAtPosition( null );
        }

        @Override
        public void componentShown( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

}
