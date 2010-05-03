package org.deegree.tools.crs.georeferencing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
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

public class GUItest extends JFrame {

    private final static String WINDOW_TITLE = "Deegree3 Georeferencing Client ";

    private final Dimension subcomponentDim;

    private final Dimension frameDim;

    private Scene2DPaneltest scenePanel2D;

    private final JPanel panelRightAbove;

    private XYCoordinates xyCoordinates;

    private JMenuItem openurlMenuItem;

    private String ows7url;

    private BufferedImage scene2DBufferedImage;

    private Rectangle scenePanel2DBounds;

    public GUItest() {
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
        // setupOpenGL( gbl, true );
        this.pack();
    }

    private void setupMenubar() {

        JMenuBar menuBar;
        JMenu menuFile;

        menuBar = new JMenuBar();
        menuFile = new JMenu( "File" );
        menuBar.add( menuFile );
        openurlMenuItem = new JMenuItem( "ows7-Test" );

        ows7url = "http://ows7.lat-lon.de/haiti-wms/services?request=GetCapabilities&service=WMS&version=1.1.1";

        menuFile.add( openurlMenuItem );
        this.getRootPane().setJMenuBar( menuBar );
    }

    private void setup2DScene( GridBagLayout gbl ) {
        scenePanel2D = new Scene2DPaneltest();
        scenePanel2D.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        scenePanel2D.setPreferredSize( subcomponentDim );

        scenePanel2DBounds = scenePanel2D.getBounds();

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

    public BufferedImage getScene2DBufferedImage() {
        return scene2DBufferedImage;
    }

    public void setScene2DBufferedImage( BufferedImage scene2dBufferedImage ) {
        scene2DBufferedImage = scene2dBufferedImage;
        scenePanel2D.setBounds( getScenePanel2D().getBounds() );
        scenePanel2D.setImageToDraw( scene2DBufferedImage );

    }

    public void resetScene2D() {
        scenePanel2D.paint( new BufferedImage( 0, 0, BufferedImage.TYPE_3BYTE_BGR ).createGraphics() );
    }

    public void addScene2DurlListener( ActionListener e ) {
        openurlMenuItem.addActionListener( e );

    }

    public String openUrl() {
        return ows7url;
    }

    public Scene2DPaneltest getScenePanel2D() {
        return scenePanel2D;
    }

}
