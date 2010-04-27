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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;

/**
 * The <Code>GRViewer</Code> class provides the client to view georeferencing tools/windows.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GRViewer extends JFrame implements ActionListener, MouseListener, MouseWheelListener, MouseMotionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 3939734755481662661L;

    private final static String WINDOW_TITLE = "Deegree3 Georeferencing Client ";

    private static GeometryFactory fac = new GeometryFactory();

    private JPanel mP;

    private final JPanel mP2;

    private Point pre2DScene;

    // private Point preAffected;
    //
    // private Point affectedPostition;

    private Generate2DSceneThread gen;

    private final XYCoordinates aff;

    private final Dimension dim;

    private final Dimension frameDim;

    private JMenuItem openurl;

    private String ows7url;

    private final int margin = 200;

    private double rememberMouseX, mouseChangingX;

    private double rememberMouseY, mouseChangingY;

    /**
     * Creates a new instance of <Code>GRViewer</Code>.
     */
    GRViewer() {
        super( WINDOW_TITLE );

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbl.setConstraints( this, gbc );
        frameDim = new Dimension( 600, 600 );
        rememberMouseX = 0.0;
        rememberMouseY = 0.0;

        setLayout( gbl );
        setMinimumSize( new Dimension( 600, 600 ) );

        setPreferredSize( new Dimension( 600, 600 ) );
        // this should be a smaller value than the size of the frame...why?
        // but if this value is not set, the canvas gets a very high value while resizing the window
        dim = new Dimension( 1, 1 );

        // other panel
        mP2 = new JPanel( new BorderLayout() );
        mP2.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );

        mP2.setBackground( Color.white );
        aff = new XYCoordinates();
        aff.setOpaque( true );
        mP2.add( aff );
        mP2.setPreferredSize( dim );

        GridBagLayoutHelper.addComponent( this.getContentPane(), gbl, mP2, 1, 0, 1, 1, aff.getInsets(),
                                          GridBagConstraints.LINE_END, 1, 1 );

        setupMenubar();
        setup2DScene( gbl );
        // setupOpenGL( gbl, true );

        getContentPane().repaint();

        setVisible( true );

    }

    private void setupMenubar() {

        JMenuBar menuBar;
        JMenu file;

        menuBar = new JMenuBar();
        file = new JMenu( "File" );
        menuBar.add( file );
        openurl = new JMenuItem( "ows7-Test" );
        openurl.addActionListener( this );
        file.add( openurl );
        this.getRootPane().setJMenuBar( menuBar );
    }

    private void setup2DScene( GridBagLayout gbl ) {
        mP = new JPanel( new BorderLayout() );
        mP.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );

        gen = new Generate2DSceneThread( null );
        mP.add( gen );
        mP.addMouseListener( this );
        mP.addMouseWheelListener( this );
        mP.addMouseMotionListener( this );
        mP.setPreferredSize( dim );

        GridBagLayoutHelper.addComponent( this.getContentPane(), gbl, mP, 0, 0, 1, 2, 1.0, 1.0 );

    }

    private void setupOpenGL( GridBagLayout gbl, boolean testSphere ) {
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered( true );
        caps.setHardwareAccelerated( true );
        caps.setAlphaBits( 8 );
        caps.setAccumAlphaBits( 8 );
        OpenGLEventHandler openGLEventListener = new OpenGLEventHandler( testSphere );

        GLCanvas canvas = new GLCanvas( caps );
        canvas.addGLEventListener( openGLEventListener );
        canvas.addMouseListener( openGLEventListener.getTrackBall() );
        canvas.addMouseWheelListener( openGLEventListener.getTrackBall() );
        canvas.addMouseMotionListener( openGLEventListener.getTrackBall() );
        canvas.setPreferredSize( dim );

        GridBagLayoutHelper.addComponent( this.getContentPane(), gbl, canvas, 1, 1, 1, 1, new Insets( 10, 10, 0, 0 ),
                                          GridBagConstraints.LINE_END, 1, 1 );

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();
        if ( source instanceof JMenuItem ) {
            JMenuItem menuItem = (JMenuItem) source;
            if ( menuItem == openurl ) {
                ows7url = "http://ows7.lat-lon.de/haiti-wms/services?request=GetCapabilities&service=WMS&version=1.1.1";

                gen.initImage( ows7url, mP.getBounds(), margin );
                this.getContentPane().repaint();
            }

        }

    }

    @Override
    public void mouseClicked( MouseEvent arg0 ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered( MouseEvent m ) {

    }

    @Override
    public void mouseExited( MouseEvent arg0 ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed( MouseEvent m ) {
        pre2DScene = new GeometryFactory().createPoint( "MousePressedPoint", m.getX(), m.getY(), null );
        // preAffected = new GeometryFactory().createPoint( "MousePressedForAffected", m.getX() + xAffectedPostition,
        // m.getY() + yAffectedPosition, null );

    }

    @Override
    public void mouseReleased( MouseEvent m ) {

        gen.setDrawingStartPos( new GeometryFactory().createPoint(
                                                                   "NewDrawingStartPosition",
                                                                   gen.getDrawingStartPos().get0()
                                                                                           - ( (int) pre2DScene.get0() - m.getX() ),
                                                                   gen.getDrawingStartPos().get1()
                                                                                           - ( (int) pre2DScene.get1() - m.getY() ),
                                                                   null ) );
        gen.setAbsoluteImageMaxPos( new GeometryFactory().createPoint(
                                                                       "NewAbsoluteImagePos",
                                                                       gen.getAbsoluteImageMaxPos().get0()
                                                                                               - ( (int) pre2DScene.get0() - m.getX() ),
                                                                       gen.getAbsoluteImageMaxPos().get1()
                                                                                               - ( (int) pre2DScene.get1() - m.getY() ),
                                                                       null ) );

        rememberMouseX += ( pre2DScene.get0() - m.getX() );
        rememberMouseY += ( pre2DScene.get1() - m.getY() );

        mouseChangingX = ( pre2DScene.get0() - m.getX() );
        mouseChangingY = ( pre2DScene.get1() - m.getY() );

        // Generate2DSceneThread t = new Generate2DSceneThread(
        // gen.reTransformSomePointsToEnvelope(
        // new GeometryFactory().createPoint(
        // "MouseShiftingPoint",
        // mouseChangingX,
        // mouseChangingY,
        // null ),
        // gen.getOnePixel() ),
        // ows7url, mP.getBounds() );

        gen.reTransformSomePointsToEnvelope( new GeometryFactory().createPoint( "MouseShiftingPoint", mouseChangingX,
                                                                                mouseChangingY, null ),
                                             gen.getOnePixel() );
        gen.run();
        System.out.println( "cachedEnv: " + gen.getCachedEnvelope() );

        System.out.println( "GRViewer---: My mouse moving: " + rememberMouseX + " - " + rememberMouseY );
        if ( gen.getAbsoluteImageMaxPos().get0() <= mP.getWidth()
             || ( gen.getDrawingStartPos().get0() - margin / 2 ) >= mP.getX()
             || gen.getAbsoluteImageMaxPos().get1() <= mP.getHeight()
             || ( gen.getDrawingStartPos().get1() - margin / 2 ) >= mP.getY() ) {
            double posChangeX = 0;
            double posChangeY = 0;

            if ( gen.getAbsoluteImageMaxPos().get0() <= mP.getWidth() ) {
                posChangeX = gen.getAbsolutePosition().get0() - gen.getAbsoluteImageMaxPos().get0();
                posChangeY = -rememberMouseY;
                System.out.println( "go EAST: " + posChangeX + "|" + posChangeY );
            }
            if ( ( gen.getDrawingStartPos().get0() - margin / 2 ) >= mP.getX() ) {
                posChangeX = -gen.getDrawingStartPos().get0();
                posChangeY = -rememberMouseY;
                System.out.println( "go WEST: " + posChangeX + "|" + posChangeY );

            }
            if ( gen.getAbsoluteImageMaxPos().get1() <= mP.getHeight() ) {
                posChangeY = -gen.getAbsolutePosition().get1() + gen.getAbsoluteImageMaxPos().get1();
                posChangeX = rememberMouseX;
                System.out.println( "go SOUTH: " + posChangeX + "|" + posChangeY );
            }
            if ( ( gen.getDrawingStartPos().get1() - margin / 2 ) >= mP.getY() ) {
                posChangeY = gen.getDrawingStartPos().get1();
                posChangeX = rememberMouseX;
                System.out.println( "go NORTH: " + posChangeX + "|" + posChangeY );
            }
            rememberMouseY = 0.0;
            rememberMouseX = 0.0;
            Rectangle bounds = new Rectangle( (int) posChangeX, (int) posChangeY );
            System.out.println( "GRViewer---: PoschangeX: " + posChangeX + ", PostchangeY: " + posChangeY
                                + ", OnePixel: " + gen.getOnePixel() );
            getImageForViewer( gen.reTransformToEnvelope( bounds, gen.getOnePixel() ), true );
        }
        gen.repaint();

        // aff.setXValue( aff.getXValue() - ( (int) preAffected.get0() - ( m.getX() + xAffectedPostition ) ) );
        // aff.setYValue( aff.getYValue() - ( (int) preAffected.get1() - ( m.getY() + yAffectedPosition ) ) );
        // aff.repaint();
        BufferedImage cachedImage = gen.getCachedImage();
        System.out.println( "mein affe: " + cachedImage );
        aff.setImage( cachedImage );
        aff.repaint();

    }

    @Override
    public void mouseDragged( MouseEvent m ) {

    }

    @Override
    public void mouseMoved( MouseEvent m ) {

    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent m ) {

        if ( m.getWheelRotation() < 0 ) {

            gen.scaleImage( ( (double) 150 / (double) 100 ) );
            this.repaint();
        } else {

            gen.scaleImage( ( (double) 100 / (double) 150 ) );
            this.repaint();
        }
    }

    @Override
    public void paint( Graphics g ) {

        if ( this.getWidth() != frameDim.getWidth() || this.getHeight() != frameDim.getHeight() ) {
            if ( ows7url != null ) {
                this.gen.initImage( ows7url, mP.getBounds(), margin );
            }

        }
        this.getContentPane().repaint();
    }

    private Point getCenterPoint( Component comp ) {
        double x = (double) comp.getWidth() / 2;
        double y = (double) comp.getHeight() / 2;
        return new GeometryFactory().createPoint( "CenterPoint", x, y, null );
    }

    private void getImageForViewer( Envelope env, boolean isMarginOver ) {
        frameDim.setSize( new Dimension( this.getWidth(), this.getHeight() ) );
        if ( ows7url != null ) {

            gen.paintScreen( env, isMarginOver );

        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main( String[] args )
                            throws IOException {
        new GRViewer();

    }

}
