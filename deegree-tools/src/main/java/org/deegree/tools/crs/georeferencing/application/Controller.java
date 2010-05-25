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
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.vecmath.Point2d;

import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tools.crs.georeferencing.communication.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.Scene2DPanel;
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

    private MouseModel mouse;

    private URL scene2DUrl;

    private RasterIOOptions options;

    private BufferedImage predictedImage;

    private GeometryFactory geomFactory;

    private boolean wentIntoCriticalRegion;

    private Point2d changePoint;

    private RasterRect rect;

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

    // private Rectangle predictedBounds;

    private Envelope bbox;

    public Controller( GRViewerGUI view, Scene2D model ) {
        this.view = view;
        this.model = model;
        panel = view.getScenePanel2D();
        footPanel = view.getFootprintPanel();
        this.geomFactory = new GeometryFactory();

        view.addScene2DurlListener( new Scene2DurlListener() );
        view.addHoleWindowListener( new HoleWindowListener() );

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
    class Scene2DurlListener implements ActionListener {

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed( ActionEvent e ) {

            mouse = new MouseModel();
            model.reset();

            rect = new RasterRect( panel.getBounds() );
            options = new RasterIOOptions();

            // options.add( RasterIOOptions.CRS, "EPSG:4326" );
            options.add( RasterIOOptions.CRS, "EPSG:32618" );
            options.add( RIO_WMS_LAYERS, "dem" );
            // options.add( RIO_WMS_LAYERS, "root" );
            // options.add( RASTER_FORMATLIST, "image/jpeg" );
            options.add( RASTER_URL, view.openUrl() );
            options.add( RasterIOOptions.OPT_FORMAT, "WMS_111" );
            options.add( RIO_WMS_SYS_ID, view.openUrl() );
            options.add( RIO_WMS_MAX_SCALE, "0.1" );
            options.add( RIO_WMS_DEFAULT_FORMAT, "image/jpeg" );
            options.add( RIO_WMS_MAX_WIDTH, Integer.toString( rect.width ) );
            options.add( RIO_WMS_MAX_HEIGHT, Integer.toString( rect.height ) );
            options.add( RIO_WMS_ENABLE_TRANSPARENT, "true" );
            // options.add( RIO_WMS_TIMEOUT, "1000" );
            panel.setImageToDraw( model.generateImage( options ) );

            panel.repaint();
            panel.addScene2DMouseListener( new Scene2DMouseListener() );
            // panel.addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
            panel.addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );

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
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseEntered( MouseEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseExited( MouseEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mousePressed( MouseEvent m ) {
            mouse.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );

        }

        @Override
        public void mouseReleased( MouseEvent m ) {

            mouse.setMouseChanging( new Point2d( ( mouse.getPointMousePressed().getX() - m.getX() ),
                                                 ( mouse.getPointMousePressed().getY() - m.getY() ) ) );
            System.out.println( "MouseChanging: " + mouse.getMouseChanging() );

            Prediction pred = new Prediction( mouse.getMouseChanging() );
            pred.start();

            mouse.setCumulatedMouseChanging( new Point2d( mouse.getCumulatedMouseChanging().getX()
                                                          + mouse.getMouseChanging().getX(),
                                                          mouse.getCumulatedMouseChanging().getY()
                                                                                  + mouse.getMouseChanging().getY() ) );

            System.out.println( "buffaußerhalb: " + predictedImage );

            // if the user went into any critical region
            if ( mouse.getCumulatedMouseChanging().getX() >= panel.getImageMargin().getX()
                 || mouse.getCumulatedMouseChanging().getX() <= -panel.getImageMargin().getX()
                 || mouse.getCumulatedMouseChanging().getY() >= panel.getImageMargin().getY()
                 || mouse.getCumulatedMouseChanging().getY() <= -panel.getImageMargin().getY() ) {

                Point2d updateDrawImageAtPosition = new Point2d( mouse.getCumulatedMouseChanging().getX(),
                                                                 mouse.getCumulatedMouseChanging().getY() );

                System.out.println( "my new Point2D: " + updateDrawImageAtPosition );
                model.changeImageBoundingbox( updateDrawImageAtPosition );
                // panel.setImageToDraw( model.generateImage( panel.getBounds() ) );
                System.out.println( "changepoint: " + changePoint );

                mouse.reset();
                panel.repaint();

            } else {
                wentIntoCriticalRegion = false;
                panel.setBeginDrawImageAtPosition( new Point2d(
                                                                panel.getBeginDrawImageAtPosition().getX()
                                                                                        - mouse.getMouseChanging().getX(),
                                                                panel.getBeginDrawImageAtPosition().getY()
                                                                                        - mouse.getMouseChanging().getY() ) );
                panel.repaint();

            }

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

            // predictedBounds = new Rectangle( (int) ( panel.getBounds().getWidth() * ( 1 + panel.getMargin() ) ),
            // (int) ( panel.getBounds().getHeight() * ( 1 + panel.getMargin() ) ) );

            changePoint = new Point2d( changing.getX(), changing.getY() );
            System.out.println( "Threadchange: " + changePoint );

            model.changePredictionBoundingbox( changePoint );

            bbox = model.getPredictionBoundingbox();
            predictedImage = model.generatePredictedImage( panel.getBounds(), bbox );
            footPanel.setImage( predictedImage );
            footPanel.repaint();

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
                panel.setResolutionOfImage( panel.getResolutionOfImage() * 0.7 );
            } else {
                panel.setResolutionOfImage( panel.getResolutionOfImage() * 1.3 );
            }
            model.reset();
            // setSightWindowAttributes( model.getHoleRequestBoundingbox() );
            // panel.init( model.generateImage( panel.getBounds() ) );
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
            if ( model.getImageBoundingbox() != null ) {
                model.reset();
                // setSightWindowAttributes( model.getHoleRequestBoundingbox() );
                // panel.init( model.generateImage( panel.getBounds() ) );
                panel.repaint();

            }

        }

        @Override
        public void componentShown( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

}
