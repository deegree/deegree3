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

import java.util.List;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.AjaxBehavior;

import org.deegree.client.mdeditor.gui.listener.FormFieldValueChangedListener;

/**
 * Contains convenience methodes used to create a form field.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class FormFieldContainer {

    /**
     * Appends a the listener {@link FormFieldValueChangedListener#} as ajax beahviour to the component. The eventName
     * of the behavior is set to the passed eventName or to thedefault event name of the component if null. Rendered
     * attribute is set to ""@none" and the event will execute "@this" element.
     * 
     * @param component
     *            the component to add the ajax beahviour
     * @param eventName
     *            the name of the event to add; if null the default event name of the component will be added
     */
    protected void setFormFieldChangedAjaxBehavior( UIInput component, String eventName ) {
        AjaxBehavior ajaxInput = new AjaxBehavior();
        List<String> executes = new ArrayList<String>();
        executes.add( "@this" );
        ajaxInput.setExecute( executes );
        List<String> render = new ArrayList<String>();
        render.add( "@none" );
        ajaxInput.setRender( render );
        ajaxInput.addAjaxBehaviorListener( new FormFieldValueChangedListener() );
        if ( eventName == null ) {
            eventName = component.getDefaultEventName();
        }
        component.addClientBehavior( eventName, ajaxInput );
    }

    /**
     * @param path
     *            the path identifiying the form field
     * @param component
     *            the component to add the rendered attribute
     * @param ef
     *            the ExpressionFactory
     * @param elContext
     *            the ELContext
     */
    protected void setVisibility( String path, UIComponent component, ExpressionFactory ef, ELContext elContext ) {
        String el = "#{formFieldBean.formFields['" + path + "'].visibility}";
        ValueExpression ve = ef.createValueExpression( elContext, el, Boolean.class );
        component.setValueExpression( "rendered", ve );
    }

    /**
     * @param path
     *            the path identifiying the form field
     * @param component
     *            the component to add the value attribute
     * @param ef
     *            the ExpressionFactory
     * @param elContext
     *            the ELContext
     */
    protected void setValue( String path, UIComponent component, ExpressionFactory ef, ELContext elContext ) {
        String el = "#{formFieldBean.formFields['" + path + "'].value}";
        ValueExpression ve = ef.createValueExpression( elContext, el, Object.class );
        component.setValueExpression( "value", ve );
    }

    /**
     * @param path
     *            the path identifiying the form field
     * @param component
     *            the component to add the title attribute
     * @param ef
     *            the ExpressionFactory
     * @param elContext
     *            the ELContext
     */
    protected void setTitle( String path, UIComponent component, ExpressionFactory ef, ELContext elContext ) {
        String el = "#{formFieldBean.formFields['" + path + "'].title}";
        ValueExpression ve = ef.createValueExpression( elContext, el, String.class );
        component.setValueExpression( "title", ve );
    }

    /**
     * @param path
     *            the path identifiying the form field
     * @param input
     *            the input component to add the styleClass attribute
     * @param ef
     *            the ExpressionFactory
     * @param elContext
     *            the ELContext
     */
    protected void setStyleClass( String path, UIInput input, ExpressionFactory ef, ELContext elContext ) {
        String el = "#{formFieldBean.formFields['" + path + "'].valid ? '' : 'invalidFF'} mdFormInput";
        ValueExpression ve = ef.createValueExpression( elContext, el, String.class );
        input.setValueExpression( "styleClass", ve );
    }

}
