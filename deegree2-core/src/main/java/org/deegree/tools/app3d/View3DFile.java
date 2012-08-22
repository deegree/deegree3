//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.tools.app3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfigTemplate;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.prefs.Preferences;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.j3d.renderer.java3d.geom.Sphere;
import org.w3c.dom.Node;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * The <code>View3DFile</code> class can display shape and gml/citygml file in 3D.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class View3DFile extends JFrame implements ActionListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 7698388852544865855L;

    private static ILogger LOG = LoggerFactory.getLogger( View3DFile.class );

    private SimpleUniverse simpleUniverse;

    private Canvas3D canvas;

    private MouseRotate trackBall;

    private BranchGroup scene;

    private TransformGroup rotationGroup;

    private Light firstLight, secondLight, thirdLight;

    private Point3d centroid;

    private Preferences prefs;

    private final static String OPEN_KEY = "lastOpenLocation";

    private final static String LAST_EXTENSION = "lastFileExtension";

    private final static String WIN_TITLE = "Deegree 3D Object viewer: ";

    private Background backGround;

    private double zoomX = 1;

    private double zoomY = 1;

    private double zoomZ = 1;

    private final Vector3d upVector = new Vector3d( 0, 1, 0 );

    private List<CustomFileFilter> supportedOpenFilter = new ArrayList<CustomFileFilter>();

    /**
     * A panel showing some key stroke helps
     */
    JPanel helpLister;

    /**
     * Creates a frame with the menus and the canvas3d set and tries to load the file from given location.
     *
     * @param fileName
     *            to be loaded.
     */
    public View3DFile( String fileName ) {
        this( false );
        readFile( fileName );
    }

    /**
     * Creates a new frame with the menus and the canvas3d set.
     *
     * @param testSphere
     *            true if a sphere should be displayed.
     */
    public View3DFile( boolean testSphere ) {
        super( WIN_TITLE );
        prefs = Preferences.userNodeForPackage( View3DFile.class );
        setupGUI();

        // openFileChooser();

        setupJava3D( testSphere );
        ArrayList<String> extensions = new ArrayList<String>();

        extensions.add( "gml" );
        extensions.add( "xml" );
        supportedOpenFilter.add( new CustomFileFilter( extensions, "(*.gml, *.xml) GML or CityGML-Files" ) );

        extensions.clear();
        extensions.add( "shp" );
        supportedOpenFilter.add( new CustomFileFilter( extensions, "(*.shp) Esri ShapeFiles" ) );

        extensions.clear();
        extensions.add( "vrml" );
        extensions.add( "wrl" );
        supportedOpenFilter.add( new CustomFileFilter( extensions,
                                                       "(*.vrml, *.wrl) VRML97 - Virtual Reality Modelling Language" ) );

        pack();
    }

    /**
     * GUI stuff
     */
    private void setupGUI() {
        // add listener for closing the frame/application
        addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent evt ) {
                System.exit( 0 );
            }
        } );
        setLayout( new BorderLayout() );
        setMinimumSize( new Dimension( 600, 600 ) );
        setPreferredSize( new Dimension( 600, 600 ) );
        setVisible( true );
        // Adding the button panel
        JPanel totalPanel = new JPanel( new BorderLayout() );
        // JPanel buttonPanel = new JPanel( new FlowLayout() );
        // createButtons( buttonPanel );
        totalPanel.add( createButtons(), BorderLayout.NORTH );
        addKeyListener( this );
        helpLister = new JPanel( new GridBagLayout() );
        Border border = BorderFactory.createTitledBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ),
                                                          "Instant help" );
        helpLister.setBorder( border );
        GridBagConstraints gb = new GridBagConstraints();
        gb.ipadx = 10;
        gb.gridx = 0;
        gb.gridy = 0;
        JLabel tmp = new JLabel( "x: move postive X-axis" );
        helpLister.add( tmp, gb );

        gb.gridx++;
        tmp = new JLabel( "X: move negative X-axis" );
        helpLister.add( tmp, gb );

        gb.gridx = 0;
        gb.gridy++;
        tmp = new JLabel( "y: move positve Y-axis" );
        helpLister.add( tmp, gb );

        gb.gridx++;
        tmp = new JLabel( "Y: move negative Y-axis" );
        helpLister.add( tmp, gb );

        gb.gridy++;
        gb.gridx = 0;
        tmp = new JLabel( "z: move positve Z-axis" );
        helpLister.add( tmp, gb );
        gb.gridx++;

        tmp = new JLabel( "Z: move negative Z-axis" );
        helpLister.add( tmp, gb );
        helpLister.setVisible( false );

        totalPanel.add( helpLister, BorderLayout.SOUTH );
        getContentPane().add( totalPanel, BorderLayout.SOUTH );

    }

    private JFileChooser createFileChooser( List<CustomFileFilter> fileFilter ) {
        // Setting up the fileChooser.

        String lastLoc = prefs.get( OPEN_KEY, System.getProperty( "user.home" ) );

        File lastFile = new File( lastLoc );
        if ( !lastFile.exists() ) {
            lastFile = new File( System.getProperty( "user.home" ) );
        }
        JFileChooser fileChooser = new JFileChooser( lastFile );
        fileChooser.setMultiSelectionEnabled( false );
        if ( fileFilter != null && fileFilter.size() > 0 ) {
            // the *.* file fileter is off
            fileChooser.setAcceptAllFileFilterUsed( false );
            String lastExtension = prefs.get( LAST_EXTENSION, "*" );
            FileFilter selected = fileFilter.get( 0 );
            for ( CustomFileFilter filter : fileFilter ) {
                fileChooser.setFileFilter( filter );
                if ( filter.accepts( lastExtension ) ) {
                    selected = filter;
                }
            }

            fileChooser.setFileFilter( selected );
        }

        return fileChooser;
    }

    private void setupJava3D( boolean testSphere ) {
        // setting up Java3D
        GraphicsConfigTemplate3D configTemplate = new GraphicsConfigTemplate3D();
        configTemplate.setSceneAntialiasing( GraphicsConfigTemplate.PREFERRED );
        configTemplate.setDoubleBuffer( GraphicsConfigTemplate.REQUIRED );
        canvas = new Canvas3D( SimpleUniverse.getPreferredConfiguration() );
        canvas.setDoubleBufferEnable( true );
        simpleUniverse = new SimpleUniverse( canvas );
        if ( canvas != null ) {
            getContentPane().add( canvas, BorderLayout.CENTER );
        }

        View view = simpleUniverse.getViewer().getView();

        // view parameters
        view.setSceneAntialiasingEnable( true );
        view.setUserHeadToVworldEnable( true );

        centroid = new Point3d( 0.3, 0.3, 0.3 );
        scene = new BranchGroup();
        firstLight = createDirectionalLight( new Vector3f( 0, 0, 1 ) );
        secondLight = createDirectionalLight( new Vector3f( 0, -1, -1 ) );
        thirdLight = createDirectionalLight( new Vector3f( -1, 0, 0 ) );

        scene.addChild( firstLight );
        scene.addChild( secondLight );
        scene.addChild( thirdLight );

        // is handled by the mouse rotater, all objects will be added to it.
        rotationGroup = new TransformGroup();
        rotationGroup.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
        rotationGroup.setCapability( TransformGroup.ALLOW_TRANSFORM_READ );
        rotationGroup.setCapability( BranchGroup.ALLOW_DETACH );
        rotationGroup.setCapability( Group.ALLOW_CHILDREN_EXTEND );
        rotationGroup.setCapability( Group.ALLOW_CHILDREN_READ );
        rotationGroup.setCapability( Group.ALLOW_CHILDREN_WRITE );
        scene.addChild( rotationGroup );

        backGround = new Background( new Color3f( Color.LIGHT_GRAY ) );
        backGround.setCapability( javax.media.j3d.Node.ALLOW_BOUNDS_WRITE );
        backGround.setCapability( javax.media.j3d.Node.ALLOW_BOUNDS_READ );
        backGround.setCapability( Background.ALLOW_APPLICATION_BOUNDS_READ );
        backGround.setCapability( Background.ALLOW_APPLICATION_BOUNDS_WRITE );

        scene.addChild( backGround );

        trackBall = new MouseRotate( rotationGroup );
        scene.addChild( trackBall );

        simpleUniverse.addBranchGraph( scene );

        // adding the key listeners
        canvas.addKeyListener( this );

        if ( testSphere ) {
            BranchGroup sphere = createStartScene( null );
            addBranchGroupToScene( sphere );
        }
    }

    /**
     * Create a directional light, with color.WHITE.
     */
    private DirectionalLight createDirectionalLight( Vector3f lightDir ) {
        // create the color for the light
        Color3f color = new Color3f( Color.WHITE );
        // create the directional light with the color and direction
        DirectionalLight light = new DirectionalLight( color, lightDir );
        light.setCapability( Light.ALLOW_INFLUENCING_BOUNDS_READ );
        light.setCapability( Light.ALLOW_INFLUENCING_BOUNDS_WRITE );
        return light;
    }

    /**
     * Add the given branch group to the scene and set the appropriate distance etc. After adding the branch group to
     * the rotation group which is controlled by the mouse rotator.
     *
     * @param b
     */
    private void addBranchGroupToScene( BranchGroup b ) {
        rotationGroup.removeAllChildren();
        // translationGroup.removeAllChildren();
        // System.out.println( b.getBounds() );
        Bounds bounds = b.getBounds();
        BranchGroup wrapper = new BranchGroup();
        wrapper.setCapability( BranchGroup.ALLOW_DETACH );
        wrapper.setCapability( Group.ALLOW_CHILDREN_READ );
        wrapper.setCapability( Texture.ALLOW_ENABLE_READ );
        wrapper.setCapability( Texture.ALLOW_IMAGE_READ );
        if ( bounds != null ) {
            LOG.logDebug( "Old centroid: " + centroid );
            BoundingSphere bs = new BoundingSphere( bounds );
            bs.getCenter( centroid );
            LOG.logDebug( "New centroid: " + centroid );

            TransformGroup viewToWorld = simpleUniverse.getViewingPlatform().getViewPlatformTransform();
            Transform3D trans = new Transform3D();

            BoundingBox bbox = new BoundingBox( bounds );
            Point3d lower = new Point3d();
            bbox.getLower( lower );
            Point3d upper = new Point3d();
            bbox.getUpper( upper );
            double lengthX = Math.abs( upper.x - lower.x );
            double lengthY = Math.abs( upper.y - lower.y );
            double lengthZ = Math.abs( upper.z - lower.z );

            zoomX = lengthX * 0.1;
            zoomY = lengthY * 0.1;
            zoomZ = lengthZ * 0.1;

            double radius = ( lengthX > lengthY ) ? ( ( lengthX > lengthZ ) ? lengthX
                                                                           : ( ( lengthY > lengthZ ) ? lengthY
                                                                                                    : lengthZ ) )
                                                 : ( ( lengthY > lengthZ ) ? lengthY : lengthZ );
            Point3d eye = new Point3d();
            eye.z += ( ( lower.z - upper.z < 0 ) ? -1 : 1 ) * ( radius * 1.5 );
            if ( Math.abs( eye.z ) < 0.00001 && Math.abs( centroid.z ) < 0.00001 ) {
                eye.z = 1;
            }
            trans.lookAt( eye, centroid, upVector );

            LOG.logDebug( "Trans Matrix after lookat:\n" + trans );
            LOG.logDebug( "BBox lower: " + lower );
            LOG.logDebug( "BBox upper: " + upper );
            LOG.logDebug( "Center: " + centroid );
            LOG.logDebug( "eye: " + eye );
            LOG.logDebug( "up: " + upVector );
            LOG.logDebug( "radius: " + bs.getRadius() );
            LOG.logDebug( "used radius (of largest axis): " + radius );
            LOG.logDebug( "zoomX: " + zoomX );
            LOG.logDebug( "zoomY: " + zoomY );
            LOG.logDebug( "zoomZ: " + zoomZ );
            viewToWorld.setTransform( trans );
            View view = simpleUniverse.getViewer().getView();

            // view parameters
            view.setBackClipDistance( radius * 100 );
            // the near clippingplane is one hundereth of the far.
            view.setFrontClipDistance( radius * 0.001 );

            BoundingSphere influence = new BoundingSphere( eye, radius * 4 );

            firstLight.setInfluencingBounds( influence );
            secondLight.setInfluencingBounds( influence );
            thirdLight.setInfluencingBounds( influence );
            trackBall.setSchedulingBounds( influence );
            backGround.setApplicationBounds( influence );

            // add the negativ translation for the mouse tracker to work.
            TransformGroup resultTrans = new TransformGroup();
            Transform3D negativTranslation = new Transform3D();
            negativTranslation.setTranslation( new Vector3d( -centroid.x, -centroid.y, -centroid.z ) );
            resultTrans.setTransform( negativTranslation );
            resultTrans.addChild( b );
            wrapper.addChild( resultTrans );
        } else {
            wrapper.addChild( b );
        }
        rotationGroup.addChild( wrapper );
    }

    /**
     * @return a shere and a cube
     */
    private BranchGroup createStartScene( Point3d translation ) {
        Appearance app = new Appearance();
        RenderingAttributes ra = new RenderingAttributes();
        ra.setDepthBufferEnable( true );
        ra.setDepthBufferWriteEnable( true );
        app.setRenderingAttributes( ra );

        ColoringAttributes ca = new ColoringAttributes();
        ca.setShadeModel( ColoringAttributes.SHADE_GOURAUD );
        ca.setCapability( ColoringAttributes.NICEST );
        app.setColoringAttributes( ca );

        Material material = new Material();
        material.setAmbientColor( new Color3f( Color.WHITE ) );
        material.setDiffuseColor( new Color3f( Color.RED ) );
        material.setSpecularColor( new Color3f( Color.ORANGE ) );
        app.setMaterial( material );
        TransformGroup tg = new TransformGroup();
        Transform3D trans = new Transform3D();
        if ( translation != null ) {
            trans.setTranslation( new Vector3d( translation ) );
            tg.setTransform( trans );
        }
        tg.setCapability( BranchGroup.ALLOW_DETACH );
        tg.setCapability( Group.ALLOW_CHILDREN_READ );
        tg.addChild( new ColorCube( 0.2f ) );
        BranchGroup b = new BranchGroup();
        b.setCapability( BranchGroup.ALLOW_DETACH );
        b.addChild( tg );

        tg.addChild( new Sphere( 0.3f, app ) );

        return b;
    }

    private JPanel createButtons() {
        JPanel buttonPanel = new JPanel( new GridBagLayout() );
        GridBagConstraints gb = new GridBagConstraints();
        gb.gridx = 0;
        gb.gridy = 0;

        JRadioButton help = new JRadioButton( "Activate help" );
        help.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( helpLister.isVisible() ) {
                    helpLister.setVisible( false );
                    ( (JRadioButton) e.getSource() ).setText( "Activate help" );
                } else {
                    helpLister.setVisible( true );
                    ( (JRadioButton) e.getSource() ).setText( "De-Activate help" );

                }
            }
        } );
        buttonPanel.add( help, gb );

        gb.insets = new Insets( 10, 10, 10, 10 );
        gb.gridx++;
        JButton button = new JButton( "Open File" );
        button.setMnemonic( KeyEvent.VK_O );
        button.addActionListener( this );
        buttonPanel.add( button, gb );

        gb.gridx++;
        button = new JButton( "Export File" );
        button.setMnemonic( KeyEvent.VK_O );
        button.addActionListener( this );
        buttonPanel.add( button, gb );

        return buttonPanel;
    }

    private void readFile( String fileName ) {

        if ( fileName == null || "".equals( fileName.trim() ) ) {
            throw new InvalidParameterException( "the file name may not be null or empty" );
        }
        fileName = fileName.trim();

        final Open3DFile openFile = new Open3DFile( fileName, this );

        final JDialog dialog = new JDialog( this, "Loading", true );

        dialog.getContentPane().setLayout( new BorderLayout() );
        dialog.getContentPane().add(
                                     new JLabel( "<HTML>Loading file:<br>" + fileName + "<br>Please wait!</HTML>",
                                                 SwingConstants.CENTER ), BorderLayout.NORTH );
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted( true );
        progressBar.setIndeterminate( false );
        dialog.getContentPane().add( progressBar, BorderLayout.CENTER );

        dialog.pack();
        dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        dialog.setResizable( false );
        dialog.setLocationRelativeTo( this );

        final Thread openThread = new Thread() {
            /**
             * Opens the file in a separate thread.
             */
            @Override
            public void run() {
                openFile.openFile( progressBar );
                if ( dialog.isDisplayable() ) {
                    dialog.setVisible( false );
                    dialog.dispose();
                }
            }
        };
        openThread.start();

        dialog.setVisible( true );
        BranchGroup result = openFile.getOpenedFile();
        //
        if ( result != null ) {
            addBranchGroupToScene( result );
            File f = new File( fileName );
            setTitle( WIN_TITLE + f.getName() );
        } else {
            showExceptionDialog( "The file: " + fileName
                                 + " could not be read,\nSee error log for detailed information." );
        }

    }

    /**
     * Shows an export dialog to the user.
     */
    @SuppressWarnings("unchecked")
    private void doExport() {

        Export3DFile exportEvaluater = new Export3DFile( this );
        // find the scene graph to export
        Enumeration<Node> en = rotationGroup.getAllChildren();
        BranchGroup toExport = null;
        if ( en.hasMoreElements() ) {
            toExport = (BranchGroup) en.nextElement();
        }
        if ( toExport == null ) {
            showExceptionDialog( "Could not get the scene to export." );
            return;
        }
        StringBuilder sb = exportEvaluater.exportBranchgroup( toExport );
        if ( sb.length() == 0 ) {
            showExceptionDialog( "Exporting failed, see error log for details." );
            return;
        }
        addBranchGroupToScene( toExport );
        JFileChooser chooser = createFileChooser( null );
        int result = chooser.showSaveDialog( this );
        if ( JFileChooser.APPROVE_OPTION == result ) {
            File f = chooser.getSelectedFile();
            FileFilter ff = chooser.getFileFilter();
            if ( ff instanceof CustomFileFilter ) {
                prefs.put( LAST_EXTENSION, ( (CustomFileFilter) ff ).getExtension( f ) );
                prefs.put( OPEN_KEY, f.getParent() );
            }
            try {
                FileWriter output = new FileWriter( f );
                output.write( sb.toString() );
                output.flush();
                output.close();
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                showExceptionDialog( "Exporting failed, see error log for details." );
            }

        }
    }

    /**
     * @param errorMessage
     *            to display
     */
    public void showExceptionDialog( String errorMessage ) {
        JOptionPane.showMessageDialog( this, errorMessage );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();
        if ( source instanceof JButton ) {
            JButton clicked = (JButton) source;
            if ( clicked.getText().startsWith( "Export" ) ) {
                doExport();
            } else {
                JFileChooser fileChooser = createFileChooser( supportedOpenFilter );
                int result = fileChooser.showOpenDialog( this );
                if ( JFileChooser.APPROVE_OPTION == result ) {
                    File f = fileChooser.getSelectedFile();
                    if ( f != null ) {
                        String path = f.getAbsolutePath();
                        prefs.put( LAST_EXTENSION, ( (CustomFileFilter) fileChooser.getFileFilter() ).getExtension( f ) );
                        prefs.put( OPEN_KEY, f.getParent() );
                        readFile( path );
                    }

                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed( KeyEvent arg0 ) {
        // nottin
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased( KeyEvent arg0 ) {
        // nottin
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped( KeyEvent e ) {
        double x = 0;
        double y = 0;
        double z = 0;
        if ( e.getKeyChar() == 'q' ) {
            System.exit( 0 );
        } else if ( e.getKeyChar() == 'x' ) {
            x = zoomX;
        } else if ( e.getKeyChar() == 'X' ) {
            x = -zoomX;
        } else if ( e.getKeyChar() == 'y' ) {
            y = zoomY;
        } else if ( e.getKeyChar() == 'Y' ) {
            y = -zoomY;
        } else if ( e.getKeyChar() == 'z' ) {
            z = zoomZ;
        } else if ( e.getKeyChar() == 'Z' ) {
            z = -zoomZ;
        }

        TransformGroup viewToWorld = simpleUniverse.getViewingPlatform().getViewPlatformTransform();
        Transform3D trans = new Transform3D();
        viewToWorld.getTransform( trans );
        trans.invert();
        Vector3d translation = new Vector3d();
        trans.get( translation );

        x += translation.x;
        y += translation.y;
        z += translation.z;

        Point3d eye = new Point3d( x, y, z );
        trans.lookAt( eye, centroid, upVector );
        LOG.logDebug( "Trans after:\n" + trans + "\ncentroid: " + centroid );
        LOG.logDebug( "Center: " + centroid );
        LOG.logDebug( "eye: " + eye );

        Vector3d dist = new Vector3d( centroid );
        dist.sub( eye );
        // trackBall.setSchedulingBounds( new BoundingSphere( centroid, dist.length() ) );
        viewToWorld.setTransform( trans );

    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        View3DFile viewer = new View3DFile( true );
        viewer.toFront();

    }

    /**
     *
     * The <code>CustomFileFilter</code> class adds functionality to the filefilter mechanism of the JFileChooser.
     *
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     *
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     *
     */
    private class CustomFileFilter extends FileFilter {

        private List<String> acceptedExtensions;

        private String desc;

        /**
         * @param acceptedExtensions
         *            list of extensions this filter accepts.
         * @param description
         *            to show
         */
        CustomFileFilter( List<String> acceptedExtensions, String description ) {
            this.acceptedExtensions = new ArrayList<String>( acceptedExtensions.size() );
            StringBuilder sb = new StringBuilder();
            if ( acceptedExtensions.size() > 0 ) {

                sb.append( "(" );
                int i = 0;
                for ( String ext : acceptedExtensions ) {
                    if ( ext.startsWith( "." ) ) {
                        ext = ext.substring( 1 );
                    } else if ( ext.startsWith( "*." ) ) {
                        ext = ext.substring( 2 );
                    } else if ( ext.startsWith( "*" ) ) {
                        ext = ext.substring( 1 );
                    }

                    this.acceptedExtensions.add( ext.trim().toUpperCase() );
                    sb.append( "*." );
                    sb.append( ext );
                    if ( ++i < acceptedExtensions.size() ) {
                        sb.append( ", " );
                    }
                }
                sb.append( ")" );
            }
            sb.append( description );
            desc = sb.toString();
        }

        /**
         * @param extension
         * @return true if the extension is accepted
         */
        public boolean accepts( String extension ) {
            return extension != null && acceptedExtensions.contains( extension.toUpperCase() );
        }

        @Override
        public boolean accept( File pathname ) {
            if ( pathname.isDirectory() ) {
                return true;
            }

            String extension = getExtension( pathname );
            if ( extension != null ) {
                if ( acceptedExtensions.contains( extension.trim().toUpperCase() ) ) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @param f
         * @return the file extension (e.g. gml/shp/xml etc.)
         */
        String getExtension( File f ) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf( '.' );

            if ( i > 0 && i < s.length() - 1 ) {
                ext = s.substring( i + 1 ).toLowerCase();
            }
            return ext;
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }

}
