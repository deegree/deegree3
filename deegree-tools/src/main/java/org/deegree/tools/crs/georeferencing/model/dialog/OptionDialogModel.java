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
package org.deegree.tools.crs.georeferencing.model.dialog;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Handles the option dialogs to show in the GUI.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OptionDialogModel {

    public final static String GENERAL = "General";

    public final static String VIEW = "View";

    private boolean snappingOnOff;

    private int selectionPointSize;

    public void createNodes( DefaultMutableTreeNode root ) {
        DefaultMutableTreeNode general = null;
        DefaultMutableTreeNode view = null;
        // DefaultMutableTreeNode snapping = null;
        // DefaultMutableTreeNode zoom = null;

        general = new DefaultMutableTreeNode( GENERAL );
        root.add( general );
        view = new DefaultMutableTreeNode( VIEW );
        root.add( view );

        // snapping = new DefaultMutableTreeNode( "Snapping" );
        //
        // zoom = new DefaultMutableTreeNode( "Zoom" );

    }

    public boolean isSnappingOnOff() {
        return snappingOnOff;
    }

    public void setSnappingOnOff( boolean snappingOnOff ) {
        this.snappingOnOff = snappingOnOff;
    }

    public int getSelectionPointSize() {
        return selectionPointSize;
    }

    public void setSelectionPointSize( int selectionPointSize ) {
        this.selectionPointSize = selectionPointSize;
    }

}
