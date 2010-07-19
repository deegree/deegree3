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
package org.deegree.client.mdeditor.gui.elements;

import static org.deegree.client.mdeditor.gui.GuiUtils.*;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;

import org.deegree.client.mdeditor.gui.ReferencedElementBean;
import org.deegree.client.mdeditor.gui.creation.FormCreator;
import org.deegree.client.mdeditor.model.ReferencedElement;

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
public class GenerateIdBean implements ReferencedElementBean, Serializable {

    private static final long serialVersionUID = 7045997278465081712L;

    private UIComponent component;

    @Override
    public UIComponent getComponent( ReferencedElement element ) {
        if ( component == null ) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Application app = fc.getApplication();
            ExpressionFactory ef = app.getExpressionFactory();
            ELContext elContext = FacesContext.getCurrentInstance().getELContext();
            component = new HtmlPanelGroup();
            component.setId( getUniqueId() );
            HtmlInputText text = new HtmlInputText();
            String textId = getUniqueId();
            text.setId( textId );
            text.setDisabled( true );

            FormCreator.setValue( element.getPath().toString(), text, ef, elContext );
            FormCreator.setVisibility( element.getPath().toString(), text, ef, elContext );
            FormCreator.setStyleClass( element.getPath().toString(), text, ef, elContext );
            FormCreator.setTitle( element.getPath().toString(), text, ef, elContext );

            HtmlCommandButton button = new HtmlCommandButton();
            button.setId( getUniqueId() );
            button.setValue( getResourceText( fc, "mdLabels", "generateIdBean_btLabel" ) );
            button.getAttributes().put( FIELDPATH_ATT_KEY, element.getPath() );
            FormCreator.setVisibility( element.getPath().toString(), button, ef, elContext );

            AjaxBehavior ajaxBt = new AjaxBehavior();
            List<String> render = new ArrayList<String>();
            render.add( "emptyForm:" + textId );
            ajaxBt.setRender( render );
            ajaxBt.addAjaxBehaviorListener( new GenerateIdListener() );
            button.addClientBehavior( button.getDefaultEventName(), ajaxBt );

            component.getChildren().add( text );
            component.getChildren().add( button );
        }
        return component;
    }

}
