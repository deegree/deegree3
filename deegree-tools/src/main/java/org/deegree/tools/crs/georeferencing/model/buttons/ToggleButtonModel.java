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
package org.deegree.tools.crs.georeferencing.model.buttons;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import org.deegree.commons.utils.Pair;

/**
 * Model for toggleButtons.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ToggleButtonModel {

    private final Map<AbstractButton, Integer> buttons;

    private final Map<Integer, AbstractButton> buttonsInverse;

    private final Pair<AbstractButton, Integer>[] abstractButtons;

    // private final CustomToggleButton ctb;

    private int pointSize;

    /**
     * 
     * @param buttons
     * @param radioButtonsValue
     * @param customTB
     *            , can be <Code>null</Code>.
     * @param e
     */
    public ToggleButtonModel( Pair<AbstractButton, Integer>[] abstractButtons, ActionListener e ) {
        this.abstractButtons = abstractButtons;
        // this.ctb = customTB;

        buttonsInverse = new HashMap<Integer, AbstractButton>();
        for ( Pair<AbstractButton, Integer> b : abstractButtons ) {

            buttonsInverse.put( b.second, b.first );

        }
        // buttonsInverse.put( 0, ctb.getCustom() );
        buttons = new HashMap<AbstractButton, Integer>();
        for ( Pair<AbstractButton, Integer> b : abstractButtons ) {

            buttons.put( b.first, b.second );
        }
        // buttons.put( ctb.getCustom(), 0 );
        ButtonGroup group = new ButtonGroup();
        for ( Pair<AbstractButton, Integer> b : abstractButtons ) {
            group.add( b.first );
            b.first.addActionListener( e );
        }
        // group.add( customTB.getCustom() );
        // customTB.getCustom().addActionListener( e );
    }

    public AbstractButton[] getAllButtons() {
        int size = abstractButtons.length;
        // if ( ctb != null ) {
        // size += 1;
        // }

        AbstractButton[] a = new AbstractButton[size];
        int i = 0;
        for ( Pair<AbstractButton, Integer> b : abstractButtons ) {
            a[i++] = b.first;
        }
        // a[i] = ctb.getCustom();
        return a;
    }

    // public CustomToggleButton getCtb() {
    // return ctb;
    // }

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
        // boolean isNotCustom = false;

        if ( buttons.containsValue( pointSize ) ) {
            buttonsInverse.get( pointSize ).setSelected( true );
            // isNotCustom = true;
        }

        // if ( !isNotCustom ) {
        // ctb.getCustom().setSelected( true );
        // ctb.getCustomTextField().setText( Integer.toString( pointSize ) );
        // }
    }

    public Map<AbstractButton, Integer> getButtons() {
        return buttons;
    }

    public Map<Integer, AbstractButton> getButtonsInverse() {
        return buttonsInverse;
    }

}
