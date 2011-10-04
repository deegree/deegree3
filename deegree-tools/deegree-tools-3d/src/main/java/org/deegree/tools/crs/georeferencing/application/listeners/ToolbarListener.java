//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.tools.crs.georeferencing.application.listeners;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.deegree.tools.crs.georeferencing.i18n.Messages.get;
import static org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType.GeoreferencedPoint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.communication.dialog.coordinatejump.CoordinateJumperTextfieldDialog;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ToolbarListener implements ActionListener {

    private ApplicationState state;

    private final JToggleButton pan, zoomIn, zoomOut, reference;

    private final JButton zoomToCoordinate, zoomToMaxExtent;

    public ToolbarListener( ApplicationState state, JToggleButton pan, JToggleButton zoomIn, JToggleButton zoomOut,
                            JToggleButton reference, JButton zoomToCoordinate, JButton zoomToMaxExtent ) {
        this.state = state;
        this.pan = pan;
        this.zoomIn = zoomIn;
        this.zoomOut = zoomOut;
        this.reference = reference;
        this.zoomToCoordinate = zoomToCoordinate;
        this.zoomToMaxExtent = zoomToMaxExtent;
        pan.addActionListener( this );
        zoomIn.addActionListener( this );
        zoomOut.addActionListener( this );
        reference.addActionListener( this );
        zoomToCoordinate.addActionListener( this );
        zoomToMaxExtent.addActionListener( this );
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object src = e.getSource();
        if ( src == zoomToCoordinate ) {
            fireTextfieldJumperDialog();
            return;
        }
        if ( src == zoomToMaxExtent ) {
            state.mapController.zoomToMaxExtent();
            if ( state.chosenFile != null ) {
                state.initFootprintScene( state.chosenFile );

                state.conModel.getFootPanel().updatePoints( state.sceneValues );
                state.conModel.getFootPanel().repaint();
            }
            state.conModel.getPanel().updatePoints( state.sceneValues );
            state.conModel.getPanel().repaint();
        }
        state.pan = pan.isSelected();
        state.zoomIn = zoomIn.isSelected();
        state.zoomOut = zoomOut.isSelected();
        state.referencing = reference.isSelected();
    }

    private void fireTextfieldJumperDialog() {
        CoordinateJumperTextfieldDialog dlg = new CoordinateJumperTextfieldDialog( zoomToCoordinate.getParent() );

        dlg.setVisible( true );

        if ( dlg.wasOk() ) {
            double[] coords = dlg.getCoords();
            if ( coords == null ) {
                showMessageDialog( zoomToCoordinate.getParent(), get( "NUMBERS_ONLY" ), get( "ERROR" ), ERROR_MESSAGE );
            } else {
                if ( state.sceneValues.getEnvelopeGeoref() != null ) {
                    if ( coords.length == 2 ) {
                        state.sceneValues.setCentroidWorldEnvelopePosition( coords[0], coords[1], GeoreferencedPoint );
                    } else {
                        state.sceneValues.setCentroidWorldEnvelopePosition( coords[0], coords[1], coords[2], coords[3],
                                                                            GeoreferencedPoint );
                    }
                    state.conModel.getPanel().updatePoints( state.sceneValues );
                    state.conModel.getPanel().repaint();
                }
            }
        }
    }

}
