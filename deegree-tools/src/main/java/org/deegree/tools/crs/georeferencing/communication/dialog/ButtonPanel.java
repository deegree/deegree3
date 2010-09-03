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

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Panel for outsourcing the Buttons that should commit or reject the actions made in the <Code>OptionPanel</Code>.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ButtonPanel extends JPanel {

    public static final String BUTTON_TEXT_OK = "OK";

    public static final String BUTTON_NAME_OK = "ButtonPanel_OK";

    public static final String BUTTON_TEXT_CANCEL = "Cancel";

    public static final String BUTTON_NAME_CANCEL = "ButtonPanel_Cancel";

    public static final String BUTTON_PANEL_NAME = "buttonPanel";

    private JButton buttonOK;

    private JButton buttonCancel;

    /**
     * Creates a new instance of <Code>ButtonPanel</Code>.
     */
    public ButtonPanel() {
        this.setLayout( new FlowLayout() );
        this.setName( BUTTON_PANEL_NAME );
        buttonOK = new JButton( BUTTON_TEXT_OK );
        buttonOK.setName( BUTTON_NAME_OK );
        buttonCancel = new JButton( BUTTON_TEXT_CANCEL );
        buttonCancel.setName( BUTTON_NAME_CANCEL );

        this.add( buttonCancel );
        this.add( buttonOK );

        this.setVisible( true );
    }

    /**
     * Adds the ActionListener to the buttons.
     * 
     * @param e
     */
    public void addListeners( ActionListener e ) {
        buttonOK.addActionListener( e );
        buttonCancel.addActionListener( e );
    }

}
