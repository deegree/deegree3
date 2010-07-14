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

import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * View panel. TODO custom
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ViewPanel extends GenericSettingsPanel {

    public static final String DEFAULT = "5pt (default)";

    public static final String SEVEN = "7pt";

    public static final String TEN = "10pt";

    public static final String CUSTOM = "custom";

    private JRadioButton radioDefault;

    private JRadioButton radio7PT;

    private JRadioButton radio10PT;

    private JTextField custom;

    public ViewPanel() {
        this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        radioDefault = new JRadioButton( DEFAULT );
        radio7PT = new JRadioButton( SEVEN );
        radio10PT = new JRadioButton( TEN );
        custom = new JTextField();

        // radioDefault.setActionCommand( DEFAULT );
        // radio7PT.setActionCommand( SEVEN );
        // radio10PT.setActionCommand( TEN );

        ButtonGroup group = new ButtonGroup();
        group.add( radioDefault );
        group.add( radio7PT );
        group.add( radio10PT );
        this.add( radioDefault, this );
        this.add( radio7PT, this );
        this.add( radio10PT, this );

    }

    public void addRadioButtonListener( ActionListener e ) {
        radioDefault.addActionListener( e );
        radio7PT.addActionListener( e );
        radio10PT.addActionListener( e );
    }

    @Override
    public PanelType getType() {

        return PanelType.ViewPanel;
    }

}
