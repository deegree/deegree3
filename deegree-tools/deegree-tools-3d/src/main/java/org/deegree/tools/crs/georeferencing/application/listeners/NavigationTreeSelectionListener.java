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

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GeneralPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel.PanelType;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.NavigationPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.ViewPanel;
import org.deegree.tools.crs.georeferencing.model.dialog.OptionDialogModel;

/**
 * 
 * Provides functionality to handle user interaction within a JTree.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class NavigationTreeSelectionListener implements TreeSelectionListener {

    private ApplicationState state;

    public NavigationTreeSelectionListener( ApplicationState state ) {
        this.state = state;
    }

    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        Object source = e.getSource();
        if ( ( (JTree) source ).getName().equals( NavigationPanel.TREE_NAME ) ) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) state.optionNavPanel.getTree().getLastSelectedPathComponent();

            if ( node == null )
                // Nothing is selected.
                return;

            Object nodeInfo = node.getUserObject();
            if ( node.isLeaf() ) {
                PanelType panelType = null;
                if ( nodeInfo.equals( OptionDialogModel.GENERAL ) ) {
                    panelType = GenericSettingsPanel.PanelType.GeneralPanel;

                } else if ( nodeInfo.equals( OptionDialogModel.VIEW ) ) {
                    panelType = GenericSettingsPanel.PanelType.ViewPanel;
                }

                switch ( panelType ) {
                case GeneralPanel:
                    state.optionSettingPanel = new GeneralPanel( state.optionSettPanel );
                    ( (GeneralPanel) state.optionSettingPanel ).addCheckboxListener( new ButtonListener( state ) );
                    ( (GeneralPanel) state.optionSettingPanel ).setSnappingOnOff( state.conModel.getDialogModel().getSnappingOnOff().second );
                    ( (GeneralPanel) state.optionSettingPanel ).setInitialZoomValue( state.conModel.getDialogModel().getResizeValue().second );
                    break;
                case ViewPanel:
                    state.optionSettingPanel = new ViewPanel( new ButtonListener( state ) );
                    ( (ViewPanel) state.optionSettingPanel ).getTbm().setPointSize(
                                                                                    state.conModel.getDialogModel().getSelectionPointSize().second );
                    // ( (ViewPanel) state.optionSettingPanel ).addRadioButtonListener( new ButtonListener() );

                    break;
                }
                state.optionSettPanel.setCurrentPanel( state.optionSettingPanel );
                state.optionDialog.setSettingsPanel( state.optionSettPanel );

            } else {
                state.optionSettPanel.reset();
                state.optionDialog.reset();
            }

        }

    }
}
