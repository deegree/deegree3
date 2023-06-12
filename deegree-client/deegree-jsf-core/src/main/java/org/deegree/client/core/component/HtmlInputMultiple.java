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
package org.deegree.client.core.component;

import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.component.html.HtmlInputText;

import com.sun.faces.util.Util;

/**
 * Input component which allows multiple insert.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@ResourceDependencies({ @ResourceDependency(library = "deegree", name = "css/inputMultiple.css", target = "head"),
		@ResourceDependency(name = "javascript/multipleItems.js", library = "deegree"),
		@ResourceDependency(name = "jsf.js", target = "head", library = "javax.faces") })
@FacesComponent(value = "HtmlInputMultiple")
public class HtmlInputMultiple extends UIInput implements ClientBehaviorHolder {

	private static enum PropertyKeys {

		initialCollapsed, styleClass, style

	}

	private Class<UIInput> inputComponentClass;

	private boolean collapsible = false;

	private List<Integer> collapsed = new ArrayList<Integer>();

	private boolean initialStateChanged = false;

	public HtmlInputMultiple() {
		setRendererType("org.deegree.InputMultiple");
	}

	@SuppressWarnings("unchecked")
	public void setInputClassName(String inputClassName) {
		try {
			inputComponentClass = Util.loadClass(inputClassName, null);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void setInputComponentClass(Class<UIInput> inputComponentClass) {
		this.inputComponentClass = inputComponentClass;
	}

	public UIInput getInputInstance() {
		if (inputComponentClass != null) {
			try {
				return (UIInput) inputComponentClass.newInstance();
			}
			catch (Exception e) {
				try {
					return HtmlInputText.class.newInstance();
				}
				catch (Exception e1) {
					// TODO
					e.printStackTrace();
				}
			}
		}
		else {
			try {
				return HtmlInputText.class.newInstance();
			}
			catch (Exception e) {
				// TODO
				e.printStackTrace();
			}
		}
		return null;
	}

	public void setStyle(String style) {
		getStateHelper().put(PropertyKeys.style, style);
	}

	public String getStyle() {
		return (String) getStateHelper().eval(PropertyKeys.style, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(PropertyKeys.styleClass, styleClass);
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(PropertyKeys.styleClass, null);
	}

	public boolean isCollapsible() {
		return collapsible;
	}

	public void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
	}

	public boolean isCollapsed(int index) {
		return this.collapsed.contains(index) || isInitialCollapsed();
	}

	public void setCollapsed(int index, boolean collapsed) {
		initialStateChanged = true;
		if (collapsed) {
			this.collapsed.add(index);
		}
		else {
			this.collapsed.remove(index);
		}
	}

	public boolean isInitialCollapsed() {
		return (Boolean) getStateHelper().eval(PropertyKeys.initialCollapsed, false);
	}

	public void setInitialCollapsed(boolean initialCollapsed) {
		getStateHelper().put(PropertyKeys.initialCollapsed, initialCollapsed);
	}

	public boolean isInitialStateChanged() {
		return initialStateChanged;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getValue() {
		Object value = super.getValue();
		if (!(value instanceof List<?>)) {
			throw new FacesException("value of HtmlInputMultiple must be a list");
		}
		return (List<Object>) value;
	}

}
