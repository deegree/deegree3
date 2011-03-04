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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractGRDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ButtonPanel buttons;

    private Dimension dim;

    private JPanel panel;

    private Component parent;

    private JScrollPane pane;

    public AbstractGRDialog( Component parent, Dimension dim ) {
        this.parent = parent;
        this.dim = dim;
        this.setLayout( new BorderLayout() );
        this.setModal( true );
        this.setResizable( false );
        pane = new JScrollPane();
        buttons = new ButtonPanel();
        panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

        this.setPreferredSize( this.dim );
        this.setBounds( new Double( parent.getBounds().getCenterX() - ( this.dim.getWidth() / 2 ) ).intValue(),
                        new Double( parent.getBounds().getCenterY() - ( this.dim.getHeight() / 2 ) ).intValue(),
                        new Double( this.dim.getWidth() ).intValue(), new Double( this.dim.getHeight() ).intValue() );

        this.getContentPane().add( pane, BorderLayout.CENTER );
        pane.setViewportView( panel );
        // this.getContentPane().add( panel, BorderLayout.CENTER );
        this.getContentPane().add( buttons, BorderLayout.SOUTH );

    }

    public ButtonPanel getButtons() {
        return buttons;
    }

    /**
     * Adds the actionListener to the visible components to interact with the user.
     * 
     * @param e
     */
    public void addListeners( ActionListener e ) {

        buttons.addListeners( e );

    }

    public void setDimension( Dimension dim ) {
        this.dim = dim;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setToCenter() {
        this.setBounds( new Double( parent.getBounds().getCenterX() - ( dim.getWidth() / 2 ) ).intValue(),
                        new Double( parent.getBounds().getCenterY() - ( dim.getHeight() / 2 ) ).intValue(),
                        new Double( dim.getWidth() ).intValue(), new Double( dim.getHeight() ).intValue() );

    }

    // public void setResisable(boolean isResizable){
    // this.setResisable( isResizable );
    // }

}
