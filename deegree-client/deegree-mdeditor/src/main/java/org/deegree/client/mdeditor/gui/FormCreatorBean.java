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
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectItem;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.Behavior;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import com.sun.faces.facelets.el.TagValueExpression;

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

    private String grpId;

    private UIForm form;

    private Map<String, HtmlPanelGrid> forms = new HashMap<String, HtmlPanelGrid>();

    public void load( ComponentSystemEvent event )
                            throws AbortProcessingException {
        Application app = FacesContext.getCurrentInstance().getApplication();
        ExpressionFactory ef = app.getExpressionFactory();

        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        System.out.println( "load " + grpId );
        System.out.println( "form " + form );

        if ( form != null ) {
            form.getChildren().clear();
            if ( forms.containsKey( grpId ) ) {
                form.getChildren().add( forms.get( grpId ) );
            } else {
                HtmlPanelGrid grid = new HtmlPanelGrid();

                UIOutput newOutput = new UIOutput();
                newOutput.setValue( "Problem:" );

                HtmlInputText newInput = new HtmlInputText();
                newInput.setId( "text5mdValue" );
                // ValueExpression
                newInput.setValueExpression(
                                             "value",
                                             ef.createValueExpression(
                                                                       elContext,
                                                                       "#{formElementBean.elements['FormGroup3_text5'].value}",
                                                                       Object.class ) );

                AjaxBehavior ajax = new AjaxBehavior();
                List<String> executes = new ArrayList<String>();
                executes.add( "@this" );
                executes.add( "text5mdValue" );
                ajax.setExecute( executes );
                List<String> render = new ArrayList<String>();
                render.add( "@none" );
                ajax.setRender( render );
                System.out.println( "w " + newInput.getDefaultEventName() );

                ajax.addAjaxBehaviorListener( new AjaxBehaviorListener() {

                    @Override
                    public void processAjaxBehavior( AjaxBehaviorEvent event )
                                            throws AbortProcessingException {

                        System.out.println( "tu was" );

                    }
                } );

                newInput.addClientBehavior( "valueChange", ajax );

                grid.getChildren().add( newOutput );
                grid.getChildren().add( newInput );

                HtmlSelectOneMenu select = new HtmlSelectOneMenu();
                UISelectItem s1 = new UISelectItem();
                s1.setItemLabel( "test1" );
                select.getChildren().add( s1 );
                UISelectItem s2 = new UISelectItem();
                s2.setItemLabel( "test2" );
                select.getChildren().add( s2 );

                AjaxBehavior ajax2 = new AjaxBehavior();

                List<String> render2 = new ArrayList<String>();
                render2.add( ":helpOutput" );
                ajax.setRender( render2 );
//                ajax2.addAjaxBehaviorListener( new AjaxBehaviorListener() {
//
//                    @Override
//                    public void processAjaxBehavior( AjaxBehaviorEvent event )
//                                            throws AbortProcessingException {
//
//                        System.out.println( "tu was" );
//
//                    }
//                } );
                select.addClientBehavior( "valueChange", ajax2 );
System.out.println("w 2 "+ select.getClientBehaviors() + " " + select.toString());
                grid.getChildren().add( select );

                forms.put( grpId, grid );
                form.getChildren().add( grid );
            }
        }
    }

    // <h:inputHidden id="mdFieldId" value="FormGroup3_text5" />
    // <h:outputLabel for="text5mdValue" value="Problem:" rendered="#{formGroup3Bean.text5Visibility}" />
    // <h:inputText id="text5mdValue" rendered="#{formGroup3Bean.text5Visibility}"
    // required="#{formGroup3Bean.text5Required}" value="#{formElementBean.elements['FormGroup3_text5'].value}">
    // <f:ajax execute="@this mdFieldId" render="@none" listener="#{formElementBean.saveValue}"/>
    // </h:inputText>

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

    // MethodExpressionActionListener l = new MethodExpressionActionListener(
    // ef.createMethodExpression(
    // elContext,
    // "#{formElementBean.saveValue}",
    // null,
    // new Class[0] ) );

    // newInput.addValueChangeListener( new ValueChangeListener() {
    // @Override
    // public void processValueChange( ValueChangeEvent event )
    // throws AbortProcessingException {
    //
    // System.out.println( "vcl" );
    //
    // FacesContext fc = FacesContext.getCurrentInstance();
    // FormElementBean feBean = (FormElementBean) fc.getApplication().getELResolver().getValue(
    // fc.getELContext(),
    // null,
    // "formElementBean" );
    // feBean.saveValue( event.getNewValue() );
    //
    // }
    // } );

}
