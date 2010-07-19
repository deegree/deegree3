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

import static org.deegree.client.mdeditor.gui.GuiUtils.DG_ID_PARAM;
import static org.deegree.client.mdeditor.gui.GuiUtils.IS_REFERENCED_PARAM;
import static org.slf4j.LoggerFactory.getLogger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.gui.EditorBean;
import org.deegree.client.mdeditor.gui.DataGroupBean;
import org.deegree.client.mdeditor.gui.GuiUtils;
import org.deegree.client.mdeditor.io.DataHandler;
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
public class DataGroupSelectListener implements AjaxBehaviorListener {

    private static final Logger LOG = getLogger( DataGroupSelectListener.class );

    @Override
    public void processAjaxBehavior( AjaxBehaviorEvent event )
                            throws AbortProcessingException {
        String grpId = (String) event.getComponent().getAttributes().get( GuiUtils.GROUPID_ATT_KEY );
        String id = null;
        boolean isReferencedGrp = true;
        for ( UIComponent child : event.getComponent().getChildren() ) {
            if ( child instanceof UIParameter ) {
                UIParameter param = (UIParameter) child;
                if ( DG_ID_PARAM.equals( param.getName() ) ) {
                    id = (String) param.getValue();
                } else if ( IS_REFERENCED_PARAM.equals( param.getName() ) ) {
                    isReferencedGrp = (Boolean) param.getValue();
                }
            }
        }

        LOG.debug( "Select " + id + " from group " + grpId );
        FacesContext fc = FacesContext.getCurrentInstance();
        EditorBean editorBean = (EditorBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "editorBean" );
        DataGroup dataGroup = null;
        if ( isReferencedGrp ) {
            try {
                dataGroup = DataHandler.getInstance().getDataGroup( grpId, id );
            } catch ( ConfigurationException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            dataGroup = editorBean.getDataGroup( grpId, id );
        }
        if ( dataGroup != null ) {
            editorBean.setValues( grpId, dataGroup );

            DataGroupBean dataGroupBean = (DataGroupBean) fc.getApplication().getELResolver().getValue(
                                                                                                        fc.getELContext(),
                                                                                                        null,
                                                                                                        "dataGroupBean" );
            dataGroupBean.addSelectedDataGroup( grpId, id );
        } else {
            // TODO message
        }
    }
}
