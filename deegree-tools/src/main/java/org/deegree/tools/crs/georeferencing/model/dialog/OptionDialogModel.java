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

import org.deegree.commons.utils.Pair;

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

    /**
     * oldValue, actualValue
     */
    private Pair<Boolean, Boolean> snappingOnOff;

    /**
     * oldValue, actualValue
     */
    private Pair<Integer, Integer> selectionPointSize;

    /**
     * oldValue, actualValue
     */
    private Pair<String, String> textFieldKeyString;

    /**
     * Creates a new instance of <Code>OptionDialogPanel</Code>.
     */
    public OptionDialogModel() {
        this.snappingOnOff = new Pair<Boolean, Boolean>( false, false );
        this.selectionPointSize = new Pair<Integer, Integer>( 5, 5 );
        this.textFieldKeyString = new Pair<String, String>( "", "" );

    }

    /**
     * Creates the nodes that for the tree-representation.
     * 
     * @param root
     *            the rootNode, not <Code>null</Code>.
     */
    public void createNodes( DefaultMutableTreeNode root ) {
        DefaultMutableTreeNode general = null;
        DefaultMutableTreeNode view = null;

        general = new DefaultMutableTreeNode( GENERAL );
        root.add( general );
        view = new DefaultMutableTreeNode( VIEW );
        root.add( view );

    }

    /**
     * 
     * @return the snapping.
     */
    public Pair<Boolean, Boolean> getSnappingOnOff() {
        return snappingOnOff;
    }

    /**
     * Sets the second parameter of the Pair.
     * 
     * @param setSnapping
     *            , not <Code>null</Code>.
     */
    public void setSnappingOnOff( boolean setSnapping ) {
        if ( snappingOnOff != null ) {
            this.snappingOnOff.second = setSnapping;
        }
    }

    /**
     * 
     * @return the selectionPointSize.
     */
    public Pair<Integer, Integer> getSelectionPointSize() {
        return selectionPointSize;
    }

    /**
     * Sets the second parameter of the Pair.
     * 
     * @param selectionPointSize
     *            , not <Code>null</Code>.
     */
    public void setSelectionPointSize( int selectionPointSize ) {
        if ( this.selectionPointSize != null ) {

            this.selectionPointSize.second = selectionPointSize;
        }
    }

    /**
     * 
     * @return the text from the textfield.
     */
    public Pair<String, String> getTextFieldKeyString() {
        return textFieldKeyString;
    }

    /**
     * Sets the second parameter of the Pair.
     * 
     * @param textFieldKeyString
     *            , not <Code>null</Code>.
     */
    public void setTextFieldKeyString( String textFieldKeyString ) {
        if ( this.textFieldKeyString != null ) {
            this.textFieldKeyString.second = textFieldKeyString;
        }
    }

    /**
     * Handles the transfer of the newValue to the oldValue. <br>
     * When there is a commit like ok.
     */
    public void transferNewToOld() {
        if ( snappingOnOff != null ) {
            snappingOnOff.first = snappingOnOff.second;
        }
        if ( selectionPointSize != null ) {
            selectionPointSize.first = selectionPointSize.second;
        }
        if ( textFieldKeyString != null ) {
            textFieldKeyString.first = textFieldKeyString.second;
        }
    }

    /**
     * Handles the transfer of the oldValue to the newValue.<br>
     * If there is a reject like cancel.
     */
    public void transferOldToNew() {
        if ( snappingOnOff != null ) {
            snappingOnOff.second = snappingOnOff.first;
        }
        if ( selectionPointSize != null ) {
            selectionPointSize.second = selectionPointSize.first;
        }
        if ( textFieldKeyString != null ) {
            textFieldKeyString.second = textFieldKeyString.first;
        }
    }

}
