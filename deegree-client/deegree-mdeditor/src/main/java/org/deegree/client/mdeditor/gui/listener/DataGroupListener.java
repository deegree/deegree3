//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.mdeditor.gui.listener;

import static org.slf4j.LoggerFactory.getLogger;

import static org.deegree.client.mdeditor.gui.GuiUtils.ACTION_ATT_VALUES;
import static org.deegree.client.mdeditor.gui.GuiUtils.GROUPID_ATT_KEY;
import static org.deegree.client.mdeditor.gui.GuiUtils.ACTION_ATT_KEY;
import static org.deegree.client.mdeditor.gui.GuiUtils.DG_ID_PARAM;
import static org.deegree.client.mdeditor.gui.GuiUtils.IS_REFERENCED_PARAM;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.gui.EditorBean;
import org.deegree.client.mdeditor.gui.DataGroupBean;
import org.deegree.client.mdeditor.gui.GuiUtils.ACTION_ATT_VALUES;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.model.DataGroup;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataGroupListener implements AjaxBehaviorListener {

    private static final Logger LOG = getLogger( DataGroupListener.class );

    @Override
    public void processAjaxBehavior( AjaxBehaviorEvent event )
                            throws AbortProcessingException {
        HtmlCommandButton comp = (HtmlCommandButton) event.getComponent();
        String grpId = (String) comp.getAttributes().get( GROUPID_ATT_KEY );
        ACTION_ATT_VALUES action = (ACTION_ATT_VALUES) comp.getAttributes().get( ACTION_ATT_KEY );

        String id = null;
        boolean isReferencedGrp = true;
        for ( UIComponent child : comp.getChildren() ) {
            if ( child instanceof UIParameter ) {
                UIParameter param = (UIParameter) child;
                if ( DG_ID_PARAM.equals( param.getName() ) ) {
                    id = (String) param.getValue();
                } else if ( IS_REFERENCED_PARAM.equals( param.getName() ) ) {
                    isReferencedGrp = (Boolean) param.getValue();
                }
            }
        }

        LOG.debug( "FormGroup with id " + grpId + " action: " + action + " id is " + id );
        FacesContext fc = FacesContext.getCurrentInstance();
        EditorBean editorBean = (EditorBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "editorBean" );
        DataGroupBean dataGroupBean = (DataGroupBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "dataGroupBean" );

        if ( grpId == null ) {
            // TODO: throw exception!
            return;
        }

        if ( isReferencedGrp ) {
            try {
                handleReferencedGrp( grpId, action, id, fc, editorBean, dataGroupBean );
            } catch ( ConfigurationException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            handleInlineGrp( grpId, action, id, fc, editorBean, dataGroupBean );
        }
        dataGroupBean.reloadFormGroup( grpId, isReferencedGrp );

    }

    private void handleInlineGrp( String grpId, ACTION_ATT_VALUES action, String id, FacesContext fc,
                                  EditorBean editorBean, DataGroupBean dataGroupBean ) {

        switch ( action ) {
        case DELETE:
            editorBean.removeDataGroup( grpId, id );
            dataGroupBean.addSelectedDataGroup( grpId, null );
            break;
        case EDIT:
            editorBean.saveDataGroup( grpId, id );
            break;
        case RESET:
            editorBean.resetToDataGroup( grpId, id );
            break;
        case NEW:
        case SAVE:
            String dgId = editorBean.saveDataGroup( grpId, null );
            dataGroupBean.addSelectedDataGroup( grpId, dgId );
            break;
        }
    }

    /**
     * @param grpId
     * @param action
     * @param id
     * @param fc
     * @param editorBean
     * @param dataGroupBean
     * @throws ConfigurationException
     */
    private void handleReferencedGrp( String grpId, ACTION_ATT_VALUES action, String id, FacesContext fc,
                                      EditorBean editorBean, DataGroupBean dataGroupBean )
                            throws ConfigurationException {
        DataHandler handler = DataHandler.getInstance();
        switch ( action ) {
        case DELETE:
            handler.deleteDataGroup( grpId, id );
            dataGroupBean.addSelectedDataGroup( grpId, null );
            break;
        case EDIT:
            try {
                handler.writeDataGroup( id, editorBean.getFormGroup( grpId ) );
            } catch ( DataIOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case RESET:
            DataGroup dgReset = handler.getDataGroup( grpId, id );
            if ( dgReset != null ) {
                editorBean.setValues( grpId, dgReset );
            }
            break;
        case NEW:
        case SAVE:
            try {
                String newFileId = handler.writeDataGroup( null, editorBean.getFormGroup( grpId ) );
                dataGroupBean.addSelectedDataGroup( grpId, newFileId );
            } catch ( DataIOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        }
    }
}
