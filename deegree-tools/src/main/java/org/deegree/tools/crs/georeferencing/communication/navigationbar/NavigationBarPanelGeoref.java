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

import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.DIM_NAVIGATION_BUTTONS;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.deegree.tools.crs.georeferencing.communication.GUIConstants;

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

    public final static String NAVIGATIONBAR_PANEL_NAME_GEOREF = "NavigationBarPanelGeoref";

    private final JButton computeTransform = new JButton( GUIConstants.COMPUTE_BUTTON_TEXT );

    private final JButton resetView = new JButton( GUIConstants.RESET_VIEW_BUTTON_TEXT );

    private static final String COORD = "/org/deegree/tools/crs/georeferencing/communication/icons/zoombycoord.png";

    private JToggleButton buttonZoomCoord;

    /**
     * Creates a new instance of <Code>NavigationBarPanel</Code>.
     */
    public NavigationBarPanelGeoref() {
        super();
        this.setName( NAVIGATIONBAR_PANEL_NAME_GEOREF );
        try {
            InputStream inCoord = NavigationBarPanelGeoref.class.getResourceAsStream( COORD );

            ImageIcon iconZoomCoord = new ImageIcon( ImageIO.read( inCoord ) );

            buttonZoomCoord = new JToggleButton( iconZoomCoord );
            buttonZoomCoord.setName( GUIConstants.JBUTTON_ZOOM_COORD );
            buttonZoomCoord.setPreferredSize( DIM_NAVIGATION_BUTTONS );

            computeTransform.setName( GUIConstants.COMPUTE_BUTTON_TEXT );
            resetView.setName( GUIConstants.RESET_VIEW_BUTTON_TEXT );

            this.add( buttonZoomCoord );
            this.add( computeTransform, Component.RIGHT_ALIGNMENT );
            this.add( resetView, Component.RIGHT_ALIGNMENT );
            inCoord.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Adds the ActionListener to the AbstractButtonsthat should be affected.
     * 
     * @param c
     */
    public void addCoordListener( ActionListener c ) {
        buttonZoomCoord.addActionListener( c );
        computeTransform.addActionListener( c );
        resetView.addActionListener( c );

    }

    public JToggleButton getButtonZoomCoord() {
        return buttonZoomCoord;
    }

}
