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
package org.deegree.tools.crs.georeferencing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.vecmath.Point2d;

import org.deegree.geometry.Envelope;

/**
 * The JPanel that should display a BufferedImage.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DPanel extends JPanel implements MouseListener, MouseWheelListener, MouseMotionListener {

    private Scene2DImplWMS generate2DScene;

    private BufferedImage workingImage;

    private Point2d imageDimension;

    private Point2d mouseChanging;

    private int margin;

    private Point2d pointMousePressed;

    private Point2d beginDrawImageAtPosition;

    @Override
    protected void paintComponent( Graphics g ) {

        super.paintComponent( g );

        if ( workingImage != null ) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage( workingImage, (int) beginDrawImageAtPosition.getX(), (int) beginDrawImageAtPosition.getY(),
                          (int) imageDimension.getX(), (int) imageDimension.getY(), this );
            System.out.println( "startPos: " + beginDrawImageAtPosition );

        }

    }

    /**
     * Initializes the imagerequest and puts all the needed and never changed fields for the image.
     * 
     * @param wmsFilename
     * @param panelBounds
     * @param margin
     */
    public void initImage( String wmsFilename, Rectangle panelBounds, int margin ) {
        this.margin = margin;
        initializeNewRequest();
        generate2DScene = new Scene2DImplWMS();
        workingImage = generate2DScene.initImage( wmsFilename, panelBounds, margin );
        if ( workingImage != null ) {
            imageDimension = new Point2d( ( panelBounds.getWidth() + margin ), ( panelBounds.getHeight() + margin ) );
        }
    }

    /**
     * Initializes the fields that are needed in startposition every new imagerequest.
     */
    private void initializeNewRequest() {
        double coordinate = 0 - ( margin / 2 );

        this.mouseChanging = new Point2d( 0.0, 0.0 );
        this.beginDrawImageAtPosition = new Point2d( coordinate, coordinate );
    }

    /**
     * Gets the image from the GetMap()-request.
     * 
     * @param env
     * @param isMarginOver
     */
    public void getImage( Envelope env, boolean isMarginOver ) {

        if ( generate2DScene != null ) {
            workingImage = generate2DScene.getImage( env, isMarginOver );
        }
    }

    @Override
    public void mousePressed( MouseEvent m ) {
        pointMousePressed = new Point2d( m.getX(), m.getY() );

    }

    @Override
    public void mouseReleased( MouseEvent m ) {

        Point2d mousePressed = new Point2d( pointMousePressed.getX(), pointMousePressed.getY() );

        mouseChanging = new Point2d( ( mousePressed.getX() - m.getX() ), ( mousePressed.getY() - m.getY() ) );
        System.out.println( "MouseChanging: " + mouseChanging );

        System.out.println( "drawPosition: " + ( beginDrawImageAtPosition.getX() - mouseChanging.getX() ) + " "
                            + ( -margin ) );
        double releasedPositionX = beginDrawImageAtPosition.getX() - mouseChanging.getX();
        double releasedPositionY = beginDrawImageAtPosition.getY() - mouseChanging.getY();

        // if the user went into any critical region
        if ( (int) releasedPositionX <= -margin || (int) releasedPositionX >= 0 || (int) releasedPositionY <= -margin
             || (int) releasedPositionY >= 0 ) {
            Point2d updateDrawImageAtPosition = new Point2d( releasedPositionX + ( margin / 2 ), releasedPositionY
                                                                                                 + ( margin / 2 ) );
            if ( (int) releasedPositionX <= -margin ) {
                updateDrawImageAtPosition.setX( ( beginDrawImageAtPosition.getX() - mouseChanging.getX() )
                                                + ( margin / 2 ) );
                System.out.println( "went EAST" );

            }
            if ( (int) releasedPositionX >= 0 ) {
                updateDrawImageAtPosition.setX( ( beginDrawImageAtPosition.getX() - mouseChanging.getX() )
                                                + ( margin / 2 ) );
                System.out.println( "went WEST" );

            }
            if ( (int) releasedPositionY <= -margin ) {
                updateDrawImageAtPosition.setY( ( beginDrawImageAtPosition.getY() - mouseChanging.getY() )
                                                + ( margin / 2 ) );
                System.out.println( "went SOUTH" );

            }
            if ( (int) releasedPositionY >= 0 ) {
                updateDrawImageAtPosition.setY( ( beginDrawImageAtPosition.getY() - mouseChanging.getY() )
                                                + ( margin / 2 ) );
                System.out.println( "went NORTH" );

            }

            workingImage = generate2DScene.getImage(
                                                     generate2DScene.reTransformToEnvelope(
                                                                                            updateDrawImageAtPosition,
                                                                                            generate2DScene.getOnePixel() ),
                                                     true );

            System.out.println( "my new Point2D: " + beginDrawImageAtPosition );
            this.repaint();
            initializeNewRequest();
            System.out.println( "my new Point2D after: " + updateDrawImageAtPosition );

        } else {
            beginDrawImageAtPosition.set( beginDrawImageAtPosition.getX() - mouseChanging.getX(),
                                          beginDrawImageAtPosition.getY() - mouseChanging.getY() );
            this.repaint();
        }

    }

    @Override
    public void mouseDragged( MouseEvent m ) {

    }

    @Override
    public void mouseMoved( MouseEvent m ) {

    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent m ) {

        // if ( m.getWheelRotation() < 0 ) {
        //
        // generate2DScenePanel.scaleImage( ( (double) 150 / (double) 100 ) );
        // this.repaint();
        // } else {
        //
        // generate2DScenePanel.scaleImage( ( (double) 100 / (double) 150 ) );
        // this.repaint();
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

}
