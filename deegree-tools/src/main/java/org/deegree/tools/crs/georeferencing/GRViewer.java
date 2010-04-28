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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;

/**
 * The <Code>GRViewer</Code> class provides the client to view georeferencing tools/windows.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GRViewer extends JFrame implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 3939734755481662661L;

    private final static String WINDOW_TITLE = "Deegree3 Georeferencing Client ";

    private static GeometryFactory fac = new GeometryFactory();

    private final JPanel panelRightAbove;

    private Scene2DPanel scenePanel2D;

    private XYCoordinates xyCoordinates;

    private final Dimension subcomponentDim;

    private final Dimension frameDim;

    private JMenuItem openurl;

    private String ows7url;

    private final int margin = 200;

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

        setLayout( gbl );
        setMinimumSize( new Dimension( 600, 600 ) );

        setPreferredSize( new Dimension( 600, 600 ) );
        subcomponentDim = new Dimension( 1, 1 );

        // panel right above
        panelRightAbove = new JPanel( new BorderLayout() );
        panelRightAbove.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        xyCoordinates = new XYCoordinates();
        panelRightAbove.setBackground( Color.white );
        panelRightAbove.add( xyCoordinates );
        panelRightAbove.setPreferredSize( subcomponentDim );

        GridBagLayoutHelper.addComponent( this.getContentPane(), gbl, panelRightAbove, 1, 0, 1, 1,
                                          xyCoordinates.getInsets(), GridBagConstraints.LINE_END, 1, 1 );

        setupMenubar();
        setup2DScene( gbl );
        setupOpenGL( gbl, true );

        setVisible( true );
        pack();
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
        // panel2DScene = new JPanel( new BorderLayout() );
        // panel2DScene.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );

        scenePanel2D = new Scene2DPanel();
        scenePanel2D.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        scenePanel2D.addMouseListener( scenePanel2D );
        scenePanel2D.addMouseWheelListener( scenePanel2D );
        scenePanel2D.addMouseMotionListener( scenePanel2D );
        scenePanel2D.setPreferredSize( subcomponentDim );

        GridBagLayoutHelper.addComponent( this.getContentPane(), gbl, scenePanel2D, 0, 0, 1, 2, 1.0, 1.0 );

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
        canvas.setPreferredSize( subcomponentDim );

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

                scenePanel2D.initImage( ows7url, scenePanel2D.getBounds(), margin );

                this.repaint();
            }

        }

    }

    @Override
    public void paint( Graphics g ) {
        if ( this.getWidth() != frameDim.getWidth() || this.getHeight() != frameDim.getHeight() ) {
            if ( ows7url != null ) {
                this.scenePanel2D.initImage( ows7url, scenePanel2D.getBounds(), margin );
            }

        }
        super.paint( g );

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
