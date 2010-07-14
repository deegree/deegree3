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
package org.deegree.tools.crs.georeferencing.communication.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Custom class to provide the functionality to show option dialogs.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OptionDialog extends JDialog {

    private final static Dimension DIALOG_DIMENSION = new Dimension( 300, 300 );

    /*
     * PANEL_NAVIGATION_WIDTH
     */
    private final static int PNW = 100;

    private NavigationPanel navigationPanel;

    private SettingsPanel settingsPanel;

    /**
     * Creates a new instance of {@code Dialog} with the modal attribute <i>true</i>.
     * 
     * @param frame
     *            the parentFrame, not <Code>null</Code>.
     * @param root
     */
    public OptionDialog( Frame frame, DefaultMutableTreeNode root ) {
        super( frame, "Options", true );
        this.setBounds( new Rectangle( DIALOG_DIMENSION ) );
        setLayout( new BorderLayout() );

        navigationPanel = new NavigationPanel( new FlowLayout(), root );
        navigationPanel.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        navigationPanel.setPreferredSize( new Dimension( PNW, this.getBounds().height ) );

        settingsPanel = new SettingsPanel();
        settingsPanel.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        settingsPanel.setPreferredSize( new Dimension( this.getBounds().width - PNW, this.getBounds().height ) );
        settingsPanel.setBounds( new Rectangle( new Dimension( this.getBounds().width - PNW, this.getBounds().height ) ) );

        this.add( navigationPanel, BorderLayout.WEST );
        this.add( settingsPanel, BorderLayout.CENTER );

        this.pack();
    }

    public NavigationPanel getNavigationPanel() {
        return navigationPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    public void setNavigationPanel( NavigationPanel navigationPanel ) {
        this.navigationPanel = navigationPanel;
        this.setVisible( true );
    }

    public void setSettingsPanel( SettingsPanel settingsPanel ) {
        this.settingsPanel = settingsPanel;

        reset();
    }

    public void reset() {
        this.repaint();
        this.setVisible( true );
    }

}
