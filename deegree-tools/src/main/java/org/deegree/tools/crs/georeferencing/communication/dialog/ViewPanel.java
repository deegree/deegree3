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
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * <Code>ViewPanel</Code>.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ViewPanel extends GenericSettingsPanel {

    public static final String TWO = "2pt";

    public static final String THREE = "3pt";

    public static final String DEFAULT = "5pt (default)";

    public static final String SEVEN = "7pt";

    public static final String TEN = "10pt";

    public static final String CUSTOM = "custom";

    public static final String CUSTOM_TEXTFIELD = "customTextField";

    private JRadioButton radio2PT;

    private JRadioButton radio3PT;

    private JRadioButton radioDefault;

    private JRadioButton radio7PT;

    private JRadioButton radio10PT;

    private JRadioButton radioCustom;

    private JTextField textFieldCustom;

    private int pointSize;

    /**
     * Creates a new instance of <Code>ViewPanel</Code>.
     */
    public ViewPanel() {

        JPanel defined = new JPanel();
        defined.setLayout( new BoxLayout( defined, BoxLayout.Y_AXIS ) );
        JPanel custom = new JPanel();
        custom.setLayout( new BoxLayout( custom, BoxLayout.X_AXIS ) );

        this.setLayout( new BorderLayout() );

        radio2PT = new JRadioButton( TWO );
        radio3PT = new JRadioButton( THREE );
        radioDefault = new JRadioButton( DEFAULT );
        radio7PT = new JRadioButton( SEVEN );
        radio10PT = new JRadioButton( TEN );
        radioCustom = new JRadioButton( CUSTOM );
        textFieldCustom = new JTextField( 10 );
        radioDefault.setName( DEFAULT );
        radio7PT.setName( SEVEN );
        radio10PT.setName( TEN );
        textFieldCustom.setName( CUSTOM_TEXTFIELD );

        ButtonGroup group = new ButtonGroup();
        group.add( radio2PT );
        group.add( radio3PT );
        group.add( radioDefault );
        group.add( radio7PT );
        group.add( radio10PT );
        group.add( radioCustom );

        defined.add( radio2PT, Component.LEFT_ALIGNMENT );
        defined.add( radio3PT, Component.LEFT_ALIGNMENT );
        defined.add( radioDefault, Component.LEFT_ALIGNMENT );
        defined.add( radio7PT, Component.LEFT_ALIGNMENT );
        defined.add( radio10PT, Component.LEFT_ALIGNMENT );
        custom.add( radioCustom, Component.LEFT_ALIGNMENT );
        custom.add( textFieldCustom );
        this.add( defined, BorderLayout.PAGE_START );
        this.add( custom, BorderLayout.PAGE_END );
    }

    /**
     * Adds the ActionListener to the RadioButtons.
     * 
     * @param e
     */
    public void addRadioButtonListener( ActionListener e ) {
        radio2PT.addActionListener( e );
        radio3PT.addActionListener( e );
        radioDefault.addActionListener( e );
        radio7PT.addActionListener( e );
        radio10PT.addActionListener( e );
        radioCustom.addActionListener( e );
    }

    @Override
    public PanelType getType() {

        return PanelType.ViewPanel;
    }

    /**
     * 
     * @return the content of the textfield.
     */
    public JTextField getTextFieldCustom() {
        return textFieldCustom;
    }

    /**
     * 
     * @return the pointSize that is selected.
     */
    public int getPointSize() {
        return pointSize;
    }

    /**
     * Sets the pointSize to this view.
     * 
     * @param pointSize
     */
    public void setPointSize( int pointSize ) {
        this.pointSize = pointSize;
        switch ( pointSize ) {
        case 2:
            radio2PT.setSelected( true );
            break;
        case 3:
            radio3PT.setSelected( true );
            break;
        case 5:
            radioDefault.setSelected( true );
            break;
        case 7:
            radio7PT.setSelected( true );
            break;
        case 10:
            radio10PT.setSelected( true );
            break;
        default:
            radioCustom.setSelected( true );
            textFieldCustom.setText( Integer.toString( pointSize ) );

        }
    }

    public JRadioButton getRadioCustom() {
        return radioCustom;
    }

}
