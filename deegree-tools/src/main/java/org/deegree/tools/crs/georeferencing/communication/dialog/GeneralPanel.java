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

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * General panel.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeneralPanel extends GenericSettingsPanel {

    private static final int SH = 100;

    private static final int ZH = 100;

    private JPanel snapping;

    private JPanel zoom;

    private JCheckBox snappingOnOff;

    private JCheckBox zoomValue;

    public GeneralPanel( Component parent ) {
        this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        this.setBounds( parent.getBounds() );

        snapping = new JPanel();
        snapping.setBorder( BorderFactory.createTitledBorder( "snapping" ) );
        snapping.setBounds( new Rectangle( parent.getBounds().width, SH ) );
        snappingOnOff = new JCheckBox( "snapping ON/OFF" );
        snapping.add( snappingOnOff );

        zoom = new JPanel();
        zoom.setBorder( BorderFactory.createTitledBorder( "zoom" ) );
        zoom.setBounds( new Rectangle( parent.getBounds().width, ZH ) );
        zoomValue = new JCheckBox( "zoomValue" );
        zoom.add( zoomValue );

        this.add( snapping, this );
        this.add( zoom, this );

    }

    public JCheckBox getSnappingOnOff() {
        return snappingOnOff;
    }

    @Override
    public PanelType getType() {

        return PanelType.GeneralPanel;
    }

}
