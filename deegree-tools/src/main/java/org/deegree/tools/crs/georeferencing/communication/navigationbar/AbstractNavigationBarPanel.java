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

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.deegree.tools.crs.georeferencing.communication.GUIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all <Code>NavigationBarPanel</Code>s.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractNavigationBarPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractNavigationBarPanel.class );

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final String PAN = "/org/deegree/tools/crs/georeferencing/communication/icons/pan.png";

    private static final String ZOOM_IN = "/org/deegree/tools/crs/georeferencing/communication/icons/zoomin.png";

    private static final String ZOOM_OUT = "/org/deegree/tools/crs/georeferencing/communication/icons/zoomout.png";

    protected JToggleButton buttonPan;

    protected JToggleButton buttonZoomIn;

    protected JToggleButton buttonZoomOut;

    public AbstractNavigationBarPanel() {
        this.setLayout( new FlowLayout( 10 ) );
        try {
            InputStream inPan = this.getClass().getResourceAsStream( PAN );

            InputStream inZoomIn = this.getClass().getResourceAsStream( ZOOM_IN );

            InputStream inZoomOut = this.getClass().getResourceAsStream( ZOOM_OUT );

            ImageIcon iconPan = new ImageIcon( ImageIO.read( inPan ) );
            ImageIcon iconZoomIn = new ImageIcon( ImageIO.read( inZoomIn ) );
            ImageIcon iconZoomOut = new ImageIcon( ImageIO.read( inZoomOut ) );

            buttonPan = new JToggleButton( iconPan );
            buttonPan.setName( GUIConstants.JBUTTON_PAN );
            buttonZoomIn = new JToggleButton( iconZoomIn );
            buttonZoomIn.setName( GUIConstants.JBUTTON_ZOOM_IN );
            buttonZoomOut = new JToggleButton( iconZoomOut );
            buttonZoomOut.setName( GUIConstants.JBUTTON_ZOOM_OUT );

            buttonPan.setPreferredSize( DIM_NAVIGATION_BUTTONS );
            buttonZoomIn.setPreferredSize( DIM_NAVIGATION_BUTTONS );
            buttonZoomOut.setPreferredSize( DIM_NAVIGATION_BUTTONS );

            this.add( buttonZoomIn );
            this.add( buttonZoomOut );
            this.add( buttonPan );

            inPan.close();
            inZoomIn.close();
            inZoomOut.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.repaint();
    }

    /**
     * Adds the ActionListener to the AbstractButtons that should be affected.
     * 
     * @param c
     */
    public void addAbstractCoordListener( ActionListener c ) {
        buttonPan.addActionListener( c );
        buttonZoomIn.addActionListener( c );
        buttonZoomOut.addActionListener( c );

    }

    public JToggleButton getButtonPan() {
        return buttonPan;
    }

    public JToggleButton getButtonZoomIn() {
        return buttonZoomIn;
    }

    public JToggleButton getButtonZoomOut() {
        return buttonZoomOut;
    }

}
