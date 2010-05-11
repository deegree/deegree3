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
package org.deegree.tools.crs.georeferencing.communication;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;

/**
 * The <Code>GRViewerGUI</Code> class provides the client to view georeferencing tools/windows.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GRViewerGUI extends JFrame {

    private final static String WINDOW_TITLE = " deegree3 Georeferencing Client ";

    private final static Dimension SUBCOMPONENT_DIMENSION = new Dimension( 1, 1 );

    private final static Dimension FRAME_DIMENSION = new Dimension( 600, 600 );;

    private Scene2DPanel scenePanel2D;

    private final JPanel panelRightAbove;

    private BuildingFootprintPanel footprintPanel;

    private JMenuItem openurlMenuItem;

    private String ows7url;

    public GRViewerGUI() {
        super( WINDOW_TITLE );

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbl.setConstraints( this, gbc );

        setLayout( gbl );
        setMinimumSize( new Dimension( 600, 600 ) );

        setPreferredSize( new Dimension( 600, 600 ) );

        // panel right above
        panelRightAbove = new JPanel( new BorderLayout() );
        panelRightAbove.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        footprintPanel = new BuildingFootprintPanel();
        panelRightAbove.setBackground( Color.white );
        panelRightAbove.add( footprintPanel );
        panelRightAbove.setPreferredSize( SUBCOMPONENT_DIMENSION );

        GridBagLayoutHelper.addComponent( this.getContentPane(), gbl, panelRightAbove, 1, 0, 1, 1,
                                          footprintPanel.getInsets(), GridBagConstraints.LINE_END, 1, 1 );

        setupMenubar();
        setup2DScene( gbl );
        setupOpenGL( gbl, true );
        this.pack();
    }

    private void setupMenubar() {

        JMenuBar menuBar;
        JMenu menuFile;

        menuBar = new JMenuBar();
        menuFile = new JMenu( "File" );
        menuBar.add( menuFile );
        openurlMenuItem = new JMenuItem( "ows7-Test" );

        // ows7url = "http://ows7.lat-lon.de/haiti-wms/services?request=GetCapabilities&service=WMS&version=1.1.1";
        ows7url = "http://localhost:8080/deegree-wms-cite111/services?REQUEST=GetCapabilities&VERSION=1.1.1&SERVICE=WMS";

        menuFile.add( openurlMenuItem );
        this.getRootPane().setJMenuBar( menuBar );
    }

    private void setup2DScene( GridBagLayout gbl ) {
        scenePanel2D = new Scene2DPanel();
        scenePanel2D.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        scenePanel2D.setPreferredSize( SUBCOMPONENT_DIMENSION );

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
        canvas.setPreferredSize( SUBCOMPONENT_DIMENSION );

        GridBagLayoutHelper.addComponent( this.getContentPane(), gbl, canvas, 1, 1, 1, 1, new Insets( 10, 10, 0, 0 ),
                                          GridBagConstraints.LINE_END, 1, 1 );
    }

    /**
     * not used at the moment
     */
    public void resetScene2D() {
        scenePanel2D.paint( new BufferedImage( 0, 0, BufferedImage.TYPE_3BYTE_BGR ).createGraphics() );
    }

    /**
     * Adds the actionListener to the menuItem that specify the URL to the resource.
     * 
     * @param e
     */
    public void addScene2DurlListener( ActionListener e ) {
        openurlMenuItem.addActionListener( e );

    }

    /**
     * Populates the URL to the resource.
     * 
     * @return
     */
    public String openUrl() {
        return ows7url;
    }

    /**
     * The {@link Scene2DPanel} is a child of this Container
     * 
     * @return
     */
    public Scene2DPanel getScenePanel2D() {
        return scenePanel2D;
    }

    public void addHoleWindowListener( ComponentListener c ) {
        this.addComponentListener( c );

    }

}
