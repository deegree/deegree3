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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Abstract base class for all <Code>NavigationBarPanel</Code>s.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractNavigationBarPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static final URL PAN = AbstractNavigationBarPanel.class.getResource( "../../icons/pan.png" );

    protected static final URL ZOOM_IN = AbstractNavigationBarPanel.class.getResource( "../../icons/zoomin.png" );

    protected static final URL ZOOM_OUT = AbstractNavigationBarPanel.class.getResource( "../../icons/zoomout.png" );

    protected static final Dimension DIM = new Dimension( 20, 20 );

    protected JButton buttonPan;

    protected JButton buttonZoomIn;

    protected JButton buttonZoomOut;

    public AbstractNavigationBarPanel() {
        this.setLayout( new FlowLayout( 10 ) );

        ImageIcon iconPan = new ImageIcon( PAN );
        ImageIcon iconZoomIn = new ImageIcon( ZOOM_IN );
        ImageIcon iconZoomOut = new ImageIcon( ZOOM_OUT );

        buttonPan = new JButton( iconPan );
        buttonZoomIn = new JButton( iconZoomIn );
        buttonZoomOut = new JButton( iconZoomOut );

        buttonPan.setPreferredSize( DIM );
        buttonZoomIn.setPreferredSize( DIM );
        buttonZoomOut.setPreferredSize( DIM );

        this.add( buttonZoomIn );
        this.add( buttonZoomOut );
        this.add( buttonPan );

        this.repaint();
    }

}
