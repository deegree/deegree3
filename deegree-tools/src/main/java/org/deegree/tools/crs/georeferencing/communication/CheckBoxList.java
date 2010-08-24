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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A list of checkboxes where one can only select one item. It's like a group of radiobuttons but with checkboxes
 * instead.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CheckBoxList extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private List<JCheckBox> list;

    protected static Border noFocusBorder = new EmptyBorder( 1, 1, 1, 1 );

    /**
     * Creates a new instance of <Code>CheckBoxList</Code>.
     * 
     * @param checkboxNames
     *            the names that each checkbox should have. The length of the checkboxNames is the size of the list, not
     *            <Code>null</Code>.
     */
    public CheckBoxList( String[] checkboxNames ) {

        this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        int length = checkboxNames.length;

        list = new ArrayList<JCheckBox>();
        for ( int i = 0; i < length; i++ ) {
            JCheckBox checkbox = new JCheckBox( checkboxNames[i] );
            checkbox.setName( checkboxNames[i] );
            this.add( checkbox, Component.LEFT_ALIGNMENT );
            list.add( checkbox );
        }

        // this.setBorder( BorderFactory.createLineBorder( Color.black ) );
        this.setPreferredSize( GUIConstants.DIM_CHECKBOXLIST );
        this.setVisible( true );

    }

    /**
     * Adds the ActionListener to all the checkboxes.
     * 
     * @param l
     */
    public void addCheckboxListener( ActionListener l ) {
        for ( JCheckBox checkbox : list ) {
            checkbox.addActionListener( l );
        }
    }

    /**
     * Deselects all checkboxes except of the one, that should be selected.
     * 
     * @param selectedCheckbox
     *            the selected checkbox.
     */
    public void selectThisCheckbox( JCheckBox selectedCheckbox ) {
        for ( JCheckBox checkbox : list ) {
            if ( checkbox == selectedCheckbox ) {
                checkbox.setSelected( true );
            } else {
                checkbox.setSelected( false );
            }
        }

    }

    public List<JCheckBox> getList() {
        return list;
    }

}
