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
package org.deegree.tools.crs.georeferencing.communication.checkboxlist;

import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.MENUITEM_TRANS_AFFINE;
import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.MENUITEM_TRANS_HELMERT;
import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.MENUITEM_TRANS_POLYNOM_FIRST;
import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.MENUITEM_TRANS_POLYNOM_FOURTH;
import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.MENUITEM_TRANS_POLYNOM_SECOND;
import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.MENUITEM_TRANS_POLYNOM_THIRD;

import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.deegree.tools.crs.georeferencing.model.CheckBoxListModel;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CheckboxListTransformation {

    private final String[] sArray = new String[] { MENUITEM_TRANS_POLYNOM_FIRST, MENUITEM_TRANS_POLYNOM_SECOND,
                                                  MENUITEM_TRANS_POLYNOM_THIRD, MENUITEM_TRANS_POLYNOM_FOURTH,
                                                  MENUITEM_TRANS_HELMERT, MENUITEM_TRANS_AFFINE };

    private final CheckBoxListModel model;

    public CheckboxListTransformation( CheckBoxListModel model ) {
        this.model = model;
        model.addCheckboxs( sArray );

    }

    /**
     * Adds the ActionListener to all the checkboxes.
     * 
     * @param l
     */
    public void addCheckboxListener( ActionListener l ) {
        for ( JCheckBox checkbox : model.getList() ) {
            checkbox.addActionListener( l );
        }
    }

    public CheckBoxListModel getModel() {
        return model;
    }

}
