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

import java.io.Serializable;
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
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectItem;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectManyListbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.PreRenderComponentEvent;

import org.deegree.client.mdeditor.CodeListManager;
import org.deegree.client.mdeditor.FormElementManager;
import org.deegree.client.mdeditor.gui.listener.FormFieldValueChangedListener;
import org.deegree.client.mdeditor.gui.listener.FormGroupSubmitListener;
import org.deegree.client.mdeditor.gui.listener.HelpClickedListener;
import org.deegree.client.mdeditor.gui.listener.ListPreRenderedListener;
import org.deegree.client.mdeditor.model.CodeList;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.SELECT_TYPE;
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
public class FormCreatorBean implements Serializable {

    private static final long serialVersionUID = -1348293091143699536L;

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
                FormGroup fg = FormElementManager.getFormGroup( grpId );
                if ( fg != null ) {
                    HtmlPanelGrid grid = new HtmlPanelGrid();
                    grid.setId( Utils.getUniqueId() );
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
        grid.setId( Utils.getUniqueId() );
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
        if ( fg.isReferenced() ) {
            HtmlCommandButton button = new HtmlCommandButton();
            button.setId( fg.getId() );
            button.setValue( "Speichern" );
            AjaxBehavior ajaxBt = new AjaxBehavior();
            List<String> renderBt = new ArrayList<String>();
            renderBt.add( "@none" );
            ajaxBt.setRender( renderBt );
            ajaxBt.addAjaxBehaviorListener( new FormGroupSubmitListener() );
            button.addClientBehavior( button.getDefaultEventName(), ajaxBt );
            grid.getChildren().add( new HtmlPanelGroup() );
            grid.getChildren().add( button );
            grid.getChildren().add( new HtmlPanelGroup() );

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
        newOutput.setId( Utils.getUniqueId() );

        parentGrid.getChildren().add( newOutput );

        // inputText
        if ( fe instanceof InputFormField ) {
            HtmlInputText newInput = new HtmlInputText();
            newInput.setId( fe.getCompleteId() );

            setValue( fe, newInput, ef, elContext );
            setValueChangedAjaxBehavior( newInput );
            setVisibility( fe, newInput, ef, elContext );

            parentGrid.getChildren().add( newInput );
        } else if ( fe instanceof SelectFormField ) {
            SelectFormField se = (SelectFormField) fe;
            if ( SELECT_TYPE.MANY.equals( se.getSelectType() ) ) {
                HtmlSelectManyListbox selectManyMenu = new HtmlSelectManyListbox();
                selectManyMenu.setId( fe.getCompleteId() );

                setValue( fe, selectManyMenu, ef, elContext );
                setValueChangedAjaxBehavior( selectManyMenu );
                setVisibility( se, selectManyMenu, ef, elContext );

                if ( se.getReferenceToCodeList() != null ) {
                    addCodeListItems( selectManyMenu, se.getReferenceToCodeList() );
                } else if ( se.getReferenceToGroup() != null ) {
                    selectManyMenu.getAttributes().put( "grpReference", se.getReferenceToGroup() );
                    selectManyMenu.subscribeToEvent( PreRenderComponentEvent.class, new ListPreRenderedListener() );
                }

                parentGrid.getChildren().add( selectManyMenu );
            } else {
                HtmlSelectOneMenu selectOneMenu = new HtmlSelectOneMenu();
                selectOneMenu.setId( se.getCompleteId() );

                setValue( fe, selectOneMenu, ef, elContext );
                setValueChangedAjaxBehavior( selectOneMenu );
                setVisibility( se, selectOneMenu, ef, elContext );

                if ( se.getReferenceToCodeList() != null ) {
                    addCodeListItems( selectOneMenu, se.getReferenceToCodeList() );
                } else if ( se.getReferenceToGroup() != null ) {
                    selectOneMenu.getAttributes().put( "grpReference", se.getReferenceToGroup() );
                    selectOneMenu.subscribeToEvent( PreRenderComponentEvent.class, new ListPreRenderedListener() );
                }

                setVisibility( se, selectOneMenu, ef, elContext );

                parentGrid.getChildren().add( selectOneMenu );
            }
        }
        // help
        HtmlCommandLink helpLink = new HtmlCommandLink();
        helpLink.setValue( "o" );
        helpLink.setId( Utils.getUniqueId() );
        UIParameter param = new UIParameter();
        param.setId( Utils.getUniqueId() );
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

    private void addCodeListItems( UIInput select, String codeListRef ) {
        CodeList codeList = CodeListManager.getCodeList( codeListRef );
        if ( codeList != null ) {
            for ( String value : codeList.getCodes().keySet() ) {
                UISelectItem si = new UISelectItem();
                si.setId( Utils.getUniqueId() );
                si.setItemValue( value );
                si.setItemLabel( codeList.getCodes().get( value ) );
                select.getChildren().add( si );
            }
        }
    }

    private void setValueChangedAjaxBehavior( UIInput component ) {
        AjaxBehavior ajaxInput = new AjaxBehavior();
        List<String> executes = new ArrayList<String>();
        executes.add( "@this" );
        ajaxInput.setExecute( executes );
        List<String> render = new ArrayList<String>();
        render.add( "@none" );
        ajaxInput.setRender( render );
        ajaxInput.addAjaxBehaviorListener( new FormFieldValueChangedListener() );
        component.addClientBehavior( component.getDefaultEventName(), ajaxInput );
    }

    private void setVisibility( FormField fe, UIComponent component, ExpressionFactory ef, ELContext elContext ) {
        String el = "#{formFieldBean.elements['" + fe.getCompleteId() + "'].visibility}";
        ValueExpression ve = ef.createValueExpression( elContext, el, Boolean.class );
        component.setValueExpression( "rendered", ve );
    }

    private void setValue( FormField fe, UIComponent component, ExpressionFactory ef, ELContext elContext ) {
        String el = "#{formFieldBean.elements['" + fe.getCompleteId() + "'].value}";
        ValueExpression ve = ef.createValueExpression( elContext, el, Object.class );
        component.setValueExpression( "value", ve );
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
