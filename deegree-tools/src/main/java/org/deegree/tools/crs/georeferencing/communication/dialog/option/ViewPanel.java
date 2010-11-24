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
package org.deegree.tools.crs.georeferencing.communication.dialog.option;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.deegree.commons.utils.Pair;
import org.deegree.tools.crs.georeferencing.model.buttons.ToggleButtonModel;

/**
 * <Code>ViewPanel</Code>.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ViewPanel extends GenericSettingsPanel {

    private static final String TWO = "2pt";

    private static final String THREE = "3pt";

    private static final String DEFAULT = "5pt (default)";

    private static final String SEVEN = "7pt";

    private static final String TEN = "10pt";

    public static final String CUSTOM_TEXTFIELD = "customTextField";

    private final Pair<AbstractButton, Integer>[] radiobuttons = new Pair[] {
                                                                             new Pair( new JRadioButton( TWO ), 2 ),
                                                                             new Pair( new JRadioButton( THREE ), 3 ),
                                                                             new Pair( new JRadioButton( DEFAULT ), 5 ),
                                                                             new Pair( new JRadioButton( SEVEN ), 7 ),
                                                                             new Pair( new JRadioButton( TEN ), 10 ) };

    // private final CustomToggleButton ctb;

    private final ToggleButtonModel tbm;

    /**
     * Creates a new instance of <Code>ViewPanel</Code>.
     */
    public ViewPanel( ActionListener e ) {
        // this.ctb = new CustomToggleButton( 10, CUSTOM_TEXTFIELD );
        this.tbm = new ToggleButtonModel( radiobuttons, e );

        JPanel defined = new JPanel();
        defined.setLayout( new BoxLayout( defined, BoxLayout.Y_AXIS ) );
        JPanel custom = new JPanel();
        custom.setLayout( new BoxLayout( custom, BoxLayout.X_AXIS ) );
        this.setLayout( new BorderLayout() );

        for ( AbstractButton b : tbm.getAllButtons() ) {
            defined.add( b, Component.LEFT_ALIGNMENT );
        }
        // custom.add( ctb.getCustom(), Component.LEFT_ALIGNMENT );
        // custom.add( ctb.getCustomTextField() );

        this.add( defined, BorderLayout.PAGE_START );
        this.add( custom, BorderLayout.PAGE_END );
    }

    @Override
    public PanelType getType() {

        return PanelType.ViewPanel;
    }

    public ToggleButtonModel getTbm() {
        return tbm;
    }

}
