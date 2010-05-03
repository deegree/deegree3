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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectManyMenu;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;

import org.deegree.client.mdeditor.config.FormConfigurationParser;
import org.deegree.client.mdeditor.gui.listener.FormFieldValueChangedListener;
import org.deegree.client.mdeditor.gui.listener.HelpClickedListener;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.SelectFormField;
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
public class FormCreatorBean {

    private static final Logger LOG = getLogger( FormCreatorBean.class );

    private String grpId;

    private UIForm form;

    private Map<String, HtmlPanelGrid> forms = new HashMap<String, HtmlPanelGrid>();

    public void load( ComponentSystemEvent event )
                            throws AbortProcessingException {

        LOG.debug( "Load form for goup with id  " + grpId );

        if ( form != null ) {
            form.getChildren().clear();
            if ( forms.containsKey( grpId ) ) {
                form.getChildren().add( forms.get( grpId ) );
            } else if ( grpId != null ) {
                FormGroup fg = FormConfigurationParser.getFormGroup( grpId );
                if ( fg != null ) {
                    HtmlPanelGrid grid = new HtmlPanelGrid();
                    addFormGroup( grid, fg );
                    forms.put( grpId, grid );
                    form.getChildren().add( grid );
                }
            }
        }

    }

    private void addFormGroup( HtmlPanelGrid parentGrid, FormGroup fg ) {

        LOG.debug( "Add FormGroup " + fg.getId() );

        HtmlPanelGrid grid = new HtmlPanelGrid();
        grid.setColumns( 3 );
        grid.setHeaderClass( "mdFormHeader" );

        // label
        UIOutput title = new UIOutput();
        title.setValue( fg.getTitle() );
        grid.getFacets().put( "header", title );

        // createInputPanelGroup();
        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormGroup ) {
                grid.getChildren().add( new HtmlPanelGroup() );
                addFormGroup( grid, (FormGroup) fe );
                grid.getChildren().add( new HtmlPanelGroup() );
            } else {
                addFormField( grid, (FormField) fe );
            }
        }
        parentGrid.getChildren().add( grid );
    }

    private void addFormField( HtmlPanelGrid parentGrid, FormField fe ) {

        LOG.debug( "Add FormField " + fe.getCompleteId() );

        Application app = FacesContext.getCurrentInstance().getApplication();
        ExpressionFactory ef = app.getExpressionFactory();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();

        // label
        UIOutput newOutput = new UIOutput();
        newOutput.setValue( fe.getLabel() );
        setVisibility( fe, newOutput, ef, elContext );

        parentGrid.getChildren().add( newOutput );

        // inputText
        if ( fe instanceof InputFormField ) {
            HtmlInputText newInput = new HtmlInputText();
            newInput.setId( fe.getCompleteId() );

            AjaxBehavior ajaxInput = new AjaxBehavior();
            List<String> executes = new ArrayList<String>();
            executes.add( "@this" );
            // executes.add( hiddenId );
            ajaxInput.setExecute( executes );
            List<String> render = new ArrayList<String>();
            render.add( "@none" );
            ajaxInput.setRender( render );
            ajaxInput.addAjaxBehaviorListener( new FormFieldValueChangedListener() );
            newInput.addClientBehavior( newInput.getDefaultEventName(), ajaxInput );

            setVisibility( fe, newInput, ef, elContext );

            parentGrid.getChildren().add( newInput );
        } else if ( fe instanceof SelectFormField ) {
            if ( "many".equals( ( (SelectFormField) fe ).getSelectType() ) ) {
                HtmlSelectManyMenu selectManyMenu = new HtmlSelectManyMenu();
                selectManyMenu.setId( fe.getCompleteId() + "mdValue" );

                setVisibility( fe, selectManyMenu, ef, elContext );

                parentGrid.getChildren().add( selectManyMenu );
            } else {
                HtmlSelectOneMenu selectOneMenu = new HtmlSelectOneMenu();
                selectOneMenu.setId( fe.getCompleteId() + "mdValue" );

                setVisibility( fe, selectOneMenu, ef, elContext );

                parentGrid.getChildren().add( selectOneMenu );
            }
        }
        // help
        HtmlCommandLink helpLink = new HtmlCommandLink();
        helpLink.setValue( "o" );
        UIParameter param = new UIParameter();
        param.setName( "mdHelp" );
        param.setValue( fe.getHelp() );
        helpLink.getChildren().add( param );
        helpLink.getChildren().add( helpLink );

        AjaxBehavior ajaxHelp = new AjaxBehavior();
        List<String> renderHelp = new ArrayList<String>();
        renderHelp.add( ":helpOutput" );
        ajaxHelp.setRender( renderHelp );
        ajaxHelp.addAjaxBehaviorListener( new HelpClickedListener() );
        helpLink.addClientBehavior( helpLink.getDefaultEventName(), ajaxHelp );

        setVisibility( fe, helpLink, ef, elContext );

        parentGrid.getChildren().add( helpLink );
    }

    private void setVisibility( FormField fe, UIComponent component, ExpressionFactory ef, ELContext elContext ) {
        String el = "#{formFieldBean.elements['" + fe.getCompleteId() + "'].visibility}";
        ValueExpression ve = ef.createValueExpression( elContext, el, Boolean.class );
        component.setValueExpression( "rendered", ve );
    }

    public void setForm( UIForm form ) {
        this.form = form;
    }

    public UIForm getForm() {
        return form;
    }

    public void setGrpId( String grpId ) {
        this.grpId = grpId;
    }

    public String getGrpId() {
        return grpId;
    }

}
