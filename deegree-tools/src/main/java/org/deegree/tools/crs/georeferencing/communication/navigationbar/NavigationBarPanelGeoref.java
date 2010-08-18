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
package org.deegree.tools.crs.georeferencing.communication.navigationbar;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * 
 * The NavigationBar above all the components.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class NavigationBarPanelGeoref extends AbstractNavigationBarPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public final static String HORIZONTAL_REFERENCING = "Horizontal Referencing";

    public final static String NAVIGATIONBAR_PANEL_NAME_GEOREF = "NavigationBarPanelGeoref";

    // private JCheckBox checkBox1 = new JCheckBox( HORIZONTAL_REFERENCING );

    private static final URL ZOOM_BY_COORD = NavigationBarPanelGeoref.class.getResource( "../../icons/zoombycoord.png" );

    private JButton buttonZoomCoord;

    /**
     * Creates a new instance of <Code>NavigationBarPanel</Code>.
     */
    public NavigationBarPanelGeoref() {
        super();
        this.setName( NAVIGATIONBAR_PANEL_NAME_GEOREF );

        ImageIcon iconZoomCoord = new ImageIcon( ZOOM_BY_COORD );

        buttonZoomCoord = new JButton( iconZoomCoord );
        buttonZoomCoord.setPreferredSize( DIM );

        this.add( buttonZoomCoord );
        // this.add( checkBox1 );

    }

    // /**
    // * Adds the ActionListener to the AbstractButtonsthat should be affected.
    // *
    // * @param c
    // */
    // public void addHorizontalRefListener( ActionListener c ) {
    // checkBox1.addActionListener( c );
    //
    // }

}
