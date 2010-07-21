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
import java.awt.event.ActionListener;

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

    public static final String SNAPPING = "snapping";

    public static final String SNAPPING_TEXT = "snapping ON/OFF";

    public static final String ZOOM = "zoom";

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
        snapping.setBorder( BorderFactory.createTitledBorder( SNAPPING ) );
        snapping.setBounds( new Rectangle( parent.getBounds().width, SH ) );
        snappingOnOff = new JCheckBox( SNAPPING_TEXT );
        snapping.add( snappingOnOff );
        snappingOnOff.setToolTipText( getSnappingTooltipText() );
        snappingOnOff.setName( SNAPPING );

        zoom = new JPanel();
        zoom.setBorder( BorderFactory.createTitledBorder( "zoom" ) );
        zoom.setBounds( new Rectangle( parent.getBounds().width, ZH ) );
        zoomValue = new JCheckBox( "zoomValue" );
        zoom.add( zoomValue );
        zoom.setName( ZOOM );

        this.add( snapping, this );
        this.add( zoom, this );

    }

    public void addCheckboxListener( ActionListener c ) {

        snappingOnOff.addActionListener( c );

    }

    /**
     * 
     * @return the tooltip of the snapping.
     */
    private String getSnappingTooltipText() {
        StringBuilder sb = new StringBuilder();
        sb.append( "Snapping is activated if the checkmark is set." );
        return sb.toString();
    }

    public boolean getSnappingOnOff() {
        return snappingOnOff.isSelected();
    }

    public void setSnappingOnOff( boolean setSnapping ) {
        this.snappingOnOff.setSelected( setSnapping );
    }

    @Override
    public PanelType getType() {

        return PanelType.GeneralPanel;
    }

}
