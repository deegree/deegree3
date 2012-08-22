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

package org.deegree.tools.legend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * This GUI is for creating Legend Element, which are small thumbnails with textlabel and title
 * showing the defined styles from a SLD.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:26.03.2007$
 */
public class LecGUI extends JPanel {

    private static final long serialVersionUID = -8887936196797987954L;

    private static final ILogger LOG = LoggerFactory.getLogger( LecGUI.class );

    private static String TITLE = "LEC LegendElementCreator GUI - v1.0.1";

    private String lastDir = ".";

    private static String DEFAULTFORMAT = "png";

    private static String DEFAULTCOLOR = "white";

    private static int DEFAULTWIDTH = 40;

    private static int DEFAULTHEIGHT = 40;

    private static String[] POSSIBLE_OUTPUT_FORMAT = { "bmp", "gif", "jpg", "png", "tif" };

    private static String[] POSSIBLE_OUTPUT_COLORS = { "transp.", "black", "blue", "cyan", "dark_gray", "gray",
                                                      "green", "light_gray", "magenta", "orange", "pink", "red",
                                                      "white", "yellow" };

    // GUI Variables declaration //
    private JPanel filePanel;

    private JTextField sourcesld_tf;

    private JTextField targetdir_tf;

    protected static final String BTOPENSOURCE = "opensourcefile";

    protected static final String BTOPENTARGET = "opentargetdir";

    private JPanel optionsPanel;

    private JComboBox formatCBox;

    private JComboBox colorCBox;

    private JSpinner widthspinner;

    private JSpinner heightspinner;

    private JTextField titletextfield;

    private JPanel buttonPanel;

    private JButton startbutton;

    private JButton infobutton;

    private JButton exitbutton;

    protected static final String BTSTART = "START";

    protected static final String BTINFO = "INFO";

    protected static final String BTEXIT = "EXIT";

    private JPanel debugPanel;

    private JTextArea debugTextArea;

    private static final String FILEMENU = "File";

    protected static final String OPENSOURCEMENUITEM = "Open Source SLD";

    protected static final String OPENTARGETMENUITEM = "Open Target Directory";

    protected static final String STARTMENUITEM = "Start";

    protected static final String EXITMENUITEM = "Exit";

    private static final String HELPMENU = "Help";

    protected static final String INFOMENUITEM = "Info";

    // Action Handler
    LecGUIButtonHandler bel = null;

    LecGUIMenuHandler mel = null;

    /**
     * Creates new form LecGUI
     *
     */
    private LecGUI() {

        bel = new LecGUIButtonHandler( this );
        mel = new LecGUIMenuHandler( this );
        initComponents();
    }

    /**
     * initializes the GUI. Calls several methods, which inits the several gui-elements.
     */
    private void initComponents() {
        setLayout( new BorderLayout() );

        JPanel northPanel = new JPanel( new BorderLayout() );

        JPanel menuPanel = new JPanel( new BorderLayout() );
        JMenuBar menubar = initMenuBar();
        menuPanel.add( menubar, BorderLayout.BEFORE_FIRST_LINE );
        northPanel.add( menuPanel, BorderLayout.NORTH );

        JPanel mainPanel = new JPanel( new BorderLayout() );

        filePanel = initFilePanel();
        filePanel.setBorder( new javax.swing.border.EmptyBorder( new Insets( 10, 10, 10, 10 ) ) );
        mainPanel.add( filePanel, BorderLayout.NORTH );

        optionsPanel = initOptionsPanel();
        optionsPanel.setBorder( new javax.swing.border.EmptyBorder( new Insets( 10, 10, 10, 10 ) ) );
        mainPanel.add( optionsPanel, BorderLayout.WEST );

        buttonPanel = initButtonPanel();
        buttonPanel.setBorder( new javax.swing.border.EmptyBorder( new Insets( 10, 10, 10, 10 ) ) );
        mainPanel.add( buttonPanel, BorderLayout.EAST );

        northPanel.add( mainPanel, BorderLayout.CENTER );
        add( northPanel, BorderLayout.NORTH );

        debugPanel = initDebugPanel();
        debugPanel.setBorder( new TitledBorder( null, "Debug", TitledBorder.DEFAULT_JUSTIFICATION,
                                                TitledBorder.DEFAULT_POSITION, new java.awt.Font( "Dialog", 0, 10 ) ) );
        add( debugPanel, BorderLayout.CENTER );
    }

    /**
     *
     * @return JPanel
     */
    private JPanel initDebugPanel() {
        JPanel panel = new JPanel( new BorderLayout() );

        JScrollPane jScrollPane1 = new JScrollPane();
        jScrollPane1.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        jScrollPane1.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
        this.debugTextArea = new JTextArea();
        this.debugTextArea.setFont( new java.awt.Font( "Monospaced", 0, 11 ) );
        this.debugTextArea.setEditable( false );
        this.debugTextArea.setText( "You can mark this debug-output (per mouse or STRG + A)\n"
                                    + "and copy it (STRG + C) to the clipboard.\n" );

        panel.setLayout( new java.awt.BorderLayout() );

        jScrollPane1.setViewportView( debugTextArea );

        panel.add( jScrollPane1, java.awt.BorderLayout.CENTER );
        panel.setPreferredSize( new Dimension( 150, 150 ) );

        return panel;
    }

    /**
     * inits the filepanel, the panel at the top to choose the files.
     *
     * @return the JPanel containing the gui-elements to open the file and dir.
     */
    private JPanel initFilePanel() {
        JPanel filePanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        filePanel.setLayout( gridbag );
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets( 2, 2, 2, 2 );

        JLabel label1 = new JLabel( "Source SLD-File:" );
        label1.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints( label1, c );
        filePanel.add( label1 );

        sourcesld_tf = new JTextField();
        sourcesld_tf.setColumns( 25 );
        c.weightx = 0.5;
        c.gridx = 1;
        gridbag.setConstraints( sourcesld_tf, c );
        filePanel.add( sourcesld_tf );

        JButton open1 = new JButton( "open" );
        open1.setFont( new Font( "Dialog", 0, 12 ) );
        open1.setActionCommand( BTOPENSOURCE );
        open1.addActionListener( bel );
        c.weightx = 0;
        c.gridx = 3;
        gridbag.setConstraints( open1, c );
        filePanel.add( open1 );

        // 2

        JLabel label2 = new JLabel( "Target Directory:" );
        label2.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints( label2, c );
        filePanel.add( label2 );

        targetdir_tf = new JTextField();
        targetdir_tf.setColumns( 25 );
        c.weightx = 0.5;
        c.gridx = 1;
        gridbag.setConstraints( targetdir_tf, c );
        filePanel.add( targetdir_tf );

        JButton open2 = new JButton( "open" );
        open2.setFont( new Font( "Dialog", 0, 12 ) );
        open2.setActionCommand( BTOPENTARGET );
        open2.addActionListener( bel );
        c.weightx = 0;
        c.gridx = 3;
        gridbag.setConstraints( open2, c );
        filePanel.add( open2 );

        return filePanel;
    }

    /**
     * inits the option-panel.
     *
     * @return the option-panel containing the gui-elements to choose the options
     */
    private JPanel initOptionsPanel() {
        JPanel bp = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        bp.setLayout( gridbag );
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets( 2, 2, 2, 2 );

        // FORMAT

        JLabel formatlabel = new JLabel( "output format: " );
        formatlabel.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints( formatlabel, c );
        bp.add( formatlabel );

        formatCBox = new JComboBox( POSSIBLE_OUTPUT_FORMAT );
        formatCBox.setSelectedItem( DEFAULTFORMAT );
        formatCBox.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 0;
        gridbag.setConstraints( formatCBox, c );
        bp.add( formatCBox );

        // BGCOLOR

        JLabel colorlabel = new JLabel( "background color: " );
        colorlabel.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints( colorlabel, c );
        bp.add( colorlabel );

        colorCBox = new JComboBox( POSSIBLE_OUTPUT_COLORS );
        colorCBox.setSelectedItem( DEFAULTCOLOR );
        colorCBox.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 1;
        gridbag.setConstraints( colorCBox, c );
        bp.add( colorCBox );

        // WIDTH

        JLabel widthlabel = new JLabel( "width:" );
        widthlabel.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints( widthlabel, c );
        bp.add( widthlabel );

        widthspinner = new JSpinner();
        widthspinner.setValue( new Integer( DEFAULTWIDTH ) );
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 2;
        gridbag.setConstraints( widthspinner, c );
        bp.add( widthspinner );

        // HEIGHT

        JLabel heightlabel = new JLabel( "height:" );
        heightlabel.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints( heightlabel, c );
        bp.add( heightlabel );

        heightspinner = new JSpinner();
        heightspinner.setValue( new Integer( DEFAULTHEIGHT ) );
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 3;
        gridbag.setConstraints( heightspinner, c );
        bp.add( heightspinner );

        // TITLE

        JLabel titlelabel = new JLabel( "title:" );
        titlelabel.setFont( new Font( "Dialog", 0, 12 ) );
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 4;
        gridbag.setConstraints( titlelabel, c );
        bp.add( titlelabel );

        titletextfield = new JTextField();
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 4;
        gridbag.setConstraints( titletextfield, c );
        bp.add( titletextfield );

        return bp;
    }

    /**
     * inits the button-panel
     *
     * @return the button-panel containing the start, info and exit buttons.
     */
    private JPanel initButtonPanel() {
        JPanel bp = new JPanel();

        startbutton = new JButton( "Start" );
        infobutton = new JButton( "Info" );
        exitbutton = new JButton( "Exit" );

        startbutton.setFont( new Font( "Dialog", 0, 12 ) );
        infobutton.setFont( new Font( "Dialog", 0, 12 ) );
        exitbutton.setFont( new Font( "Dialog", 0, 12 ) );

        startbutton.setActionCommand( BTSTART );
        startbutton.addActionListener( bel );
        infobutton.setActionCommand( BTINFO );
        infobutton.addActionListener( bel );
        exitbutton.setActionCommand( BTEXIT );
        exitbutton.addActionListener( bel );

        bp.add( startbutton );
        bp.add( infobutton );
        bp.add( exitbutton );

        return bp;
    }

    /**
     * creates the menubar. called from the main-method, not the constructor.
     *
     * @return the menubar.
     */
    private JMenuBar initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // file menu
        JMenu menu = new JMenu( FILEMENU );
        appendMenuItem( OPENSOURCEMENUITEM, menu );
        appendMenuItem( OPENTARGETMENUITEM, menu );
        menu.addSeparator();
        appendMenuItem( STARTMENUITEM, menu );
        menu.addSeparator();
        appendMenuItem( EXITMENUITEM, menu );
        menuBar.add( menu );

        // info / help menu
        menu = new JMenu( HELPMENU );
        appendMenuItem( INFOMENUITEM, menu );
        menuBar.add( menu );

        return menuBar;

    }

    /**
     * help method to init the menu
     *
     * @param name
     *            name of the menu-item
     * @param menu
     *            the menu
     */
    private void appendMenuItem( String name, JMenu menu ) {
        JMenuItem item = menu.add( name );
        item.addActionListener( mel );
    }

    /**
     * returns the content of the open dialog source-textfield
     *
     * @return content of the source-textfield
     */
    private String getSourceTextfieldContent() {
        return this.sourcesld_tf.getText();
    }

    /**
     * @see #getSourceTextfieldContent()
     * @param content
     *            the text in the source-textfield
     */
    private void setSourceTextfieldContent( String content ) {
        this.sourcesld_tf.setText( content );
    }

    /**
     * returns the content of the open dialog destination/target-textfield
     *
     * @return content of the targetdir-textfield
     */
    private String getDestDirTextfieldContent() {
        return this.targetdir_tf.getText();
    }

    /**
     * @see #getDestDirTextfieldContent()
     * @param content
     *            the text in the targetdir-textfield
     */
    private void setDestdirTextfieldContent( String content ) {
        this.targetdir_tf.setText( content );
    }

    /**
     *
     * @return the selected format
     */
    private String getSelectedFormat() {
        return (String) this.formatCBox.getSelectedItem();
    }

    /**
     *
     * @return the selected color
     */
    private String getSelectedColor() {
        return (String) this.colorCBox.getSelectedItem();
    }

    /**
     *
     * @return the selected width
     */
    private String getSelectedWidth() {
        return this.widthspinner.getValue().toString();
    }

    /**
     *
     * @return the selected height
     */
    private String getSelectedHeight() {
        return this.heightspinner.getValue().toString();
    }

    /**
     *
     * @return the text in the title textfield
     */
    private String getSelectedTitle() {
        return this.titletextfield.getText();
    }

    /**
     *
     * @param debuginformation
     */
    protected void addDebugInformation( String debuginformation ) {
        this.debugTextArea.setText( this.debugTextArea.getText() + "\n" + debuginformation );
    }

    /**
     *
     * @param args
     */
    public static void main( String[] args ) {
        // Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated( true );
        JDialog.setDefaultLookAndFeelDecorated( true );

        // Create and set up the window.
        JFrame frame = new JFrame( TITLE );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        // Create and set up the content pane.
        LecGUI lg = new LecGUI();
        lg.setOpaque( true );

        frame.setContentPane( lg );

        // Display the window.
        frame.pack();
        frame.setVisible( true );
    }

    /**
     * the funcionality of the program. parses the input from the gui-elements and passes them to
     * the LegendElementCreator class.
     */
    protected void doStart() {
        if ( getSourceTextfieldContent() == null || getSourceTextfieldContent().length() == 0 ) {
            JOptionPane.showMessageDialog( this, "No source SLD-file specified.", "no source, no start",
                                           JOptionPane.ERROR_MESSAGE );
        } else if ( getDestDirTextfieldContent() == null || getDestDirTextfieldContent().length() == 0 ) {
            JOptionPane.showMessageDialog( this, "No target-directory specified.", "no target, no thumbnail",
                                           JOptionPane.ERROR_MESSAGE );
        } else {
            String sldfile = getSourceTextfieldContent();
            String directory = getDestDirTextfieldContent();
            String format = getSelectedFormat();
            Color color = getColorFromString( getSelectedColor() );
            int width = Integer.parseInt( getSelectedWidth() );
            int height = Integer.parseInt( getSelectedHeight() );
            String title = getSelectedTitle();
            try {
                LegendElementCreator lec = new LegendElementCreator( sldfile, directory, format, color, width, height,
                                                                     title, this );
                if ( lec.getVerboseOutput() != null && lec.getVerboseOutput().length() > 0 ) {
                    addDebugInformation( "Finished!" + "\n" + lec.getVerboseOutput() );
                    JOptionPane.showMessageDialog(
                                                   this,
                                                   "Creation of LegendElements successful.\nSee the Debug output for details.",
                                                   "Finished! Press <SPACE> to suppress this window.",
                                                   JOptionPane.INFORMATION_MESSAGE );
                }
            } catch ( Exception e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
                e.printStackTrace();
                addDebugInformation( e.getMessage() );
            }

        }
    }

    /**
     * reads out the color from the string and returns the corresponding color.
     *
     * @param colorstring
     *            the color as string
     * @return the color
     */
    private Color getColorFromString( String colorstring ) {
        Color color = Color.WHITE;
        // BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA, ORANGE, PINK, RED, WHITE,
        // YELLOW
        LOG.logInfo( "colorstring: " + colorstring );
        if ( colorstring.equalsIgnoreCase( "BLACK" ) )
            color = Color.BLACK;
        else if ( colorstring.equalsIgnoreCase( "BLUE" ) )
            color = Color.BLUE;
        else if ( colorstring.equalsIgnoreCase( "CYAN" ) )
            color = Color.CYAN;
        else if ( colorstring.equalsIgnoreCase( "DARK_GRAY" ) )
            color = Color.DARK_GRAY;
        else if ( colorstring.equalsIgnoreCase( "GRAY" ) )
            color = Color.GRAY;
        else if ( colorstring.equalsIgnoreCase( "GREEN" ) )
            color = Color.GREEN;
        else if ( colorstring.equalsIgnoreCase( "LIGHT_GRAY" ) )
            color = Color.LIGHT_GRAY;
        else if ( colorstring.equalsIgnoreCase( "MAGENTA" ) )
            color = Color.MAGENTA;
        else if ( colorstring.equalsIgnoreCase( "ORANGE" ) )
            color = Color.ORANGE;
        else if ( colorstring.equalsIgnoreCase( "PINK" ) )
            color = Color.PINK;
        else if ( colorstring.equalsIgnoreCase( "RED" ) )
            color = Color.RED;
        else if ( colorstring.equalsIgnoreCase( "WHITE" ) )
            color = Color.WHITE;
        else if ( colorstring.equalsIgnoreCase( "YELLOW" ) )
            color = Color.YELLOW;
        else if ( colorstring.equalsIgnoreCase( "TRANSP." ) )
            color = null;
        else {
            // try {
            color = Color.decode( colorstring );
        }
        return color;
    }

    /**
     * opens the file chooser
     */
    protected void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory( new File( lastDir ) );
        chooser.setFileFilter( new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept( File f ) {
                return f.getName().toLowerCase().endsWith( ".xml" ) || f.isDirectory();
            }


            @Override
            public String getDescription() {
                return "StyledLayerDescriptor (*.xml)";
            }
        } );

        int returnVal = chooser.showOpenDialog( this );

        if ( returnVal == JFileChooser.APPROVE_OPTION ) {
            LOG.logInfo( chooser.getSelectedFile().getPath() );
            setSourceTextfieldContent( chooser.getSelectedFile().getPath() );
        }
    }

    /**
     * opens the target-dir file chooser. only dirs are available for selection
     */
    protected void openDirChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory( new File( lastDir ) );
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

        int returnVal = chooser.showOpenDialog( this );

        if ( returnVal == JFileChooser.APPROVE_OPTION ) {
            setDestdirTextfieldContent( chooser.getSelectedFile().getPath() );
        }
    }

    /**
     * shows the Info in a JOptionPane
     *
     */
    protected void showInfo() {
        String text = "This application is part of deegree.\n" + "http://www.deegree.org\n\n"
                      + "lat/lon GmbH \n" + "e-mail: info@lat-lon.de";
        JOptionPane.showMessageDialog( this, text, "Information", JOptionPane.INFORMATION_MESSAGE );
    }

}

/**
 * class for handling button events.
 * <hr>
 *
 * @author <a href="mailto:schaefer@lat-lon.de>Axel Schaefer</a>
 */
class LecGUIButtonHandler implements ActionListener {

    private LecGUI lecgui = null;

    /**
     * constructor
     *
     * @param lecgui
     *            the DeegreeDemoInstallerGUI uses this Button
     */
    protected LecGUIButtonHandler( LecGUI lecgui ) {
        this.lecgui = lecgui;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent event ) {
        String ac = event.getActionCommand();
        if ( ac.equals( LecGUI.BTOPENSOURCE ) ) {
            this.lecgui.openFileChooser();
        } else if ( ac.equals( LecGUI.BTOPENTARGET ) ) {
            this.lecgui.openDirChooser();
        } else if ( ac.equals( LecGUI.BTEXIT ) ) {
            System.exit( 0 );
        } else if ( ac.equals( LecGUI.BTINFO ) ) {
            this.lecgui.showInfo();
        } else if ( ac.equals( LecGUI.BTSTART ) ) {
            this.lecgui.doStart();
        }
    }
}

/**
 * class for handling button events.
 * <hr>
 *
 * @author <a href="mailto:schaefer@lat-lon.de>Axel Schaefer</a>
 */
class LecGUIMenuHandler implements ActionListener {

    private LecGUI lecgui = null;

    /**
     * constructor
     *
     * @param lecgui
     *            the DeegreeDemoInstallerGUI uses this Button
     */
    protected LecGUIMenuHandler( LecGUI lecgui ) {
        this.lecgui = lecgui;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent event ) {
        String ac = event.getActionCommand();
        if ( ac.equals( LecGUI.OPENSOURCEMENUITEM ) ) {
            this.lecgui.openFileChooser();
        } else if ( ac.equals( LecGUI.OPENTARGETMENUITEM ) ) {
            this.lecgui.openDirChooser();
        } else if ( ac.equals( LecGUI.EXITMENUITEM ) ) {
            System.exit( 0 );
        } else if ( ac.equals( LecGUI.INFOMENUITEM ) ) {
            this.lecgui.showInfo();
        } else if ( ac.equals( LecGUI.STARTMENUITEM ) ) {
            this.lecgui.doStart();
        }
    }
}
