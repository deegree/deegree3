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
package org.deegree.tools.crs.georeferencing.communication.dialog.coordinatejump;

import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.DIM_COORDINATEJUMPER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.vecmath.Point2d;

import org.deegree.tools.crs.georeferencing.communication.dialog.ButtonPanel;

/**
 * Dialog for jumping to a specific coordinate. <li>It is modal, so there is no other user interaction possible.</li>
 * <li>It is set to the center of the parent component.</li>
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CoordinateJumperSpinnerDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ButtonPanel buttons;

    private SpinnerModel spinnerModelX;

    private SpinnerModel spinnerModelY;

    private SpinnerModel spinnerModelSpan;

    private JSpinner xCenter;

    private JSpinner yCenter;

    // private JSpinner span;

    private Point2d centerPoint;

    // private double spanValue;

    public CoordinateJumperSpinnerDialog( Component parent ) {
        this.setLayout( new BorderLayout() );
        this.setPreferredSize( DIM_COORDINATEJUMPER );
        this.setBounds(
                        new Double( parent.getBounds().getCenterX() - ( DIM_COORDINATEJUMPER.getWidth() / 2 ) ).intValue(),
                        new Double( parent.getBounds().getCenterY() - ( DIM_COORDINATEJUMPER.getHeight() / 2 ) ).intValue(),
                        new Double( DIM_COORDINATEJUMPER.getWidth() ).intValue(),
                        new Double( DIM_COORDINATEJUMPER.getHeight() ).intValue() );
        this.setModal( true );
        this.setResizable( false );
        buttons = new ButtonPanel();

        if ( centerPoint == null ) {
            centerPoint = new Point2d( 0, 0 );
        }
        spinnerModelX = new SpinnerNumberModel( centerPoint.getX(), 0.00, 1.0, 0.01 );
        spinnerModelY = new SpinnerNumberModel( centerPoint.getY(), 0.00, 1.0, 0.01 );
        // spinnerModelSpan = new SpinnerNumberModel( spanValue, 0.00, 1.0, 0.01 );

        xCenter = new JSpinner( spinnerModelX );
        yCenter = new JSpinner( spinnerModelY );
        // span = new JSpinner(spinnerModelSpan);

        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
        panel.add( xCenter, Component.LEFT_ALIGNMENT );
        panel.add( yCenter, Component.LEFT_ALIGNMENT );

        this.add( panel, BorderLayout.CENTER );
        this.add( buttons, BorderLayout.SOUTH );
        this.pack();
    }

    /**
     * Adds the actionListener to the visible components to interact with the user.
     * 
     * @param e
     */
    public void addListeners( ActionListener e ) {
        // coordinateJumper.addActionListener( e );
        buttons.addListeners( e );

    }

    public ButtonPanel getButtons() {
        return buttons;
    }

}
