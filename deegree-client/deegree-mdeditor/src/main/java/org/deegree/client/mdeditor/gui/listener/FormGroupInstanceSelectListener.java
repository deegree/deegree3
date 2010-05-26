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

import java.io.FileNotFoundException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.client.mdeditor.controller.FormGroupInstanceReader;
import org.deegree.client.mdeditor.gui.FormFieldBean;
import org.deegree.client.mdeditor.gui.GuiUtils;
import org.deegree.client.mdeditor.model.FormGroupInstance;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormGroupInstanceSelectListener implements AjaxBehaviorListener {

    private static final Logger LOG = getLogger( FormGroupInstanceSelectListener.class );

    @Override
    public void processAjaxBehavior( AjaxBehaviorEvent event )
                            throws AbortProcessingException {
        String fileName = null;
        String groupId = (String) event.getComponent().getAttributes().get( GuiUtils.GROUPID_ATT_KEY );

        List<UIComponent> children = event.getComponent().getChildren();
        for ( UIComponent child : children ) {
            if ( child instanceof UIParameter
                 && GuiUtils.INSTANCE_FILE_NAME_PARAM.equals( ( (UIParameter) child ).getName() ) ) {
                fileName = String.valueOf( ( (UIParameter) child ).getValue() );
            }
        }

        LOG.debug( "Select " + fileName + " from group " + groupId );
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getELContext();
        FormFieldBean formFieldBean = (FormFieldBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "formFieldBean" );
        try {
            FormGroupInstance formGroupInstance = FormGroupInstanceReader.getFormGroupInstance( groupId, fileName );
            formFieldBean.setValues( groupId, formGroupInstance );
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
