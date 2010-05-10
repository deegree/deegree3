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
package org.deegree.client.mdeditor.gui;

import static org.deegree.client.mdeditor.model.LAYOUT_TYPE.MENU;
import static org.deegree.client.mdeditor.model.LAYOUT_TYPE.TAB;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutcomeTargetLink;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpSession;

import org.deegree.client.mdeditor.config.ConfigurationException;
import org.deegree.client.mdeditor.config.FormConfigurationFactory;
import org.deegree.client.mdeditor.gui.components.ListGroup;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.LAYOUT_TYPE;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@SessionScoped
public class MenuCreatorBean implements Serializable {

    private static final long serialVersionUID = -766056912953851369L;

    private static final Logger LOG = getLogger( MenuCreatorBean.class );

    private ListGroup listGroup;

    public void load( ComponentSystemEvent event )
                            throws AbortProcessingException {

        FacesContext fc = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fc.getExternalContext().getSession( false );
        try {
            FormConfiguration manager = FormConfigurationFactory.getOrCreateFormConfiguration( session.getId() );

            LAYOUT_TYPE layoutType = manager.getLayoutType();
            LOG.debug( "create menu for layout type: " + layoutType );

            String menuId = null;
            String listId = null;
            if ( MENU.equals( layoutType ) ) {
                menuId = "verticalMenu";
                listId = "verticalList";
            } else if ( TAB.equals( layoutType ) ) {
                menuId = "horizontalMenu";
                listId = "horizontalList";
            }

            if ( listId != null && listGroup.getChildCount() == 0 ) {
                listGroup.setRendererType( "org.deegree.ListGroupRenderer" );
                listGroup.setId( GuiUtils.getUniqueId() );
                listGroup.getAttributes().put( "listId", listId );
                listGroup.getAttributes().put( "menuId", menuId );
                for ( FormGroup formGroup : manager.getFormGroups() ) {
                    HtmlOutcomeTargetLink link = new HtmlOutcomeTargetLink();
                    link.setId( GuiUtils.getUniqueId() );
                    link.setValue( formGroup.getLabel() );
                    link.setOutcome( "emptyForm" );
                    UIParameter param = new UIParameter();
                    param.setId( GuiUtils.getUniqueId() );
                    param.setName( "grpId" );
                    param.setValue( formGroup.getId() );
                    link.getChildren().add( param );
                    listGroup.getChildren().add( link );
                    System.out.println("l: " + link.getId() + " p " + param.getId());
                } 
            }
        } catch ( ConfigurationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setListGroup( ListGroup menuForm ) {
        this.listGroup = menuForm;
    }

    public ListGroup getListGroup() {
        return listGroup;
    }

    public void forceReloaded() {
        listGroup = null;
    }

}
