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
package org.deegree.tools.crs.georeferencing.application.handler;

import javax.swing.JCheckBox;

import org.deegree.tools.crs.georeferencing.application.transformation.TransformationMethod.TransformationType;
import org.deegree.tools.crs.georeferencing.communication.GUIConstants;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GeneralPanel;
import org.deegree.tools.crs.georeferencing.model.ControllerModel;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JCheckboxHandler {

    public JCheckboxHandler( JCheckBox source, ControllerModel cm ) {

        if ( ( source ).getText().startsWith( GeneralPanel.SNAPPING_TEXT ) ) {

            boolean isSnappingOn = false;
            if ( cm.getDialogModel().getSnappingOnOff().second == false ) {
                isSnappingOn = true;

            } else {
                isSnappingOn = false;
            }
            cm.getDialogModel().setSnappingOnOff( isSnappingOn );
        }
        if ( ( source ).getText().startsWith( GUIConstants.MENUITEM_TRANS_POLYNOM_FIRST ) ) {

            cm.setTransformationType( TransformationType.Polynomial );
            cm.setOrder( 1 );

            cm.getView().activateTransformationCheckbox( source );
        }
        if ( ( source ).getText().startsWith( GUIConstants.MENUITEM_TRANS_POLYNOM_SECOND ) ) {

            cm.setTransformationType( TransformationType.Polynomial );
            cm.setOrder( 2 );

            cm.getView().activateTransformationCheckbox( source );
        }
        if ( ( source ).getText().startsWith( GUIConstants.MENUITEM_TRANS_POLYNOM_THIRD ) ) {

            cm.setTransformationType( TransformationType.Polynomial );
            cm.setOrder( 3 );

            cm.getView().activateTransformationCheckbox( source );
        }
        if ( ( source ).getText().startsWith( GUIConstants.MENUITEM_TRANS_POLYNOM_FOURTH ) ) {

            cm.setTransformationType( TransformationType.Polynomial );
            cm.setOrder( 4 );

            cm.getView().activateTransformationCheckbox( source );
        }
        if ( ( source ).getText().startsWith( GUIConstants.MENUITEM_TRANS_HELMERT ) ) {

            cm.setTransformationType( TransformationType.Helmert_4 );
            cm.setOrder( 1 );
            cm.getView().activateTransformationCheckbox( source );
        }
        if ( ( source ).getText().startsWith( GUIConstants.MENUITEM_TRANS_AFFINE ) ) {

            cm.setTransformationType( TransformationType.Affine );
            cm.setOrder( 1 );
            cm.getView().activateTransformationCheckbox( source );
        }

    }

}
