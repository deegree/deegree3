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
import java.net.MalformedURLException;
import java.net.URL;

import javax.vecmath.Point2d;

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

    private MouseModel mouse;

    private URL scene2DUrl;

    public Controller( GRViewerGUI view, Scene2D model ) {
        this.view = view;
        this.model = model;
        panel = view.getScenePanel2D();

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

            initImagePaint();

        }
    }

    /**
     * 
     */
    private void initImagePaint() {
        // get the image and resizes it with the margin
        try {
            scene2DUrl = new URL( view.openUrl() );
            model.setImageUrl( scene2DUrl );
            panel.setImageToDraw( model.generateImage( panel.getBounds() ) );
            panel.init();
            panel.addScene2DMouseListener( new Scene2DMouseListener() );
            mouse = new MouseModel();
            panel.repaint();

        } catch ( MalformedURLException e1 ) {
            e1.printStackTrace();
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

            mouse.setCumulatedMouseChanging( new Point2d( mouse.getCumulatedMouseChanging().getX()
                                                          + mouse.getMouseChanging().getX(),
                                                          mouse.getCumulatedMouseChanging().getY()
                                                                                  + mouse.getMouseChanging().getY() ) );

            // if the user went into any critical region
            if ( mouse.getCumulatedMouseChanging().getX() >= panel.getImageMargin().getX()
                 || mouse.getCumulatedMouseChanging().getX() <= -panel.getImageMargin().getX()
                 || mouse.getCumulatedMouseChanging().getY() >= panel.getImageMargin().getY()
                 || mouse.getCumulatedMouseChanging().getY() <= -panel.getImageMargin().getY() ) {

                Point2d updateDrawImageAtPosition = new Point2d( mouse.getCumulatedMouseChanging().getX(),
                                                                 mouse.getCumulatedMouseChanging().getY() );

                System.out.println( "my new Point2D: " + updateDrawImageAtPosition );
                model.setImageBoundingbox( updateDrawImageAtPosition );
                panel.setImageToDraw( model.generateImage( panel.getBounds() ) );
                mouse.reset();
                panel.repaint();

            } else {
                panel.setBeginDrawImageAtPosition( new Point2d(
                                                                panel.getBeginDrawImageAtPosition().getX()
                                                                                        - mouse.getMouseChanging().getX(),
                                                                panel.getBeginDrawImageAtPosition().getY()
                                                                                        - mouse.getMouseChanging().getY() ) );
                panel.repaint();
                System.out.println( panel.getBeginDrawImageAtPosition() );
            }

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
                initImagePaint();
            }

        }

        @Override
        public void componentShown( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

}
