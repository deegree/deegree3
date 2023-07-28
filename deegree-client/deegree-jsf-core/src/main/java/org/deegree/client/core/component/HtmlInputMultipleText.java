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

import java.util.List;

import javax.faces.FacesException;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;

/**
 * Input component which allows multiple insert.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@ResourceDependencies({ @ResourceDependency(library = "deegree", name = "css/multipleText.css", target = "head"),
		@ResourceDependency(name = "javascript/multipleText.js", library = "deegree"),
		@ResourceDependency(name = "jsf.js", target = "head", library = "javax.faces") })
@FacesComponent(value = "HtmlInputMultipleText")
public class HtmlInputMultipleText extends UIInput {

	private static enum PropertyKeys {

		styleClass, style, disabled

	}

	public HtmlInputMultipleText() {
		setRendererType("org.deegree.InputMultipleText");
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

	public void setDisabled(boolean disabled) {
		getStateHelper().put(PropertyKeys.disabled, disabled);
	}

	public boolean getDisabled() {
		return (Boolean) getStateHelper().eval(PropertyKeys.disabled, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getValue() {
		if (super.getValue() != null && !(super.getValue() instanceof List<?>)) {
			throw new FacesException("value of HtmlInputMultiple must be a list");
		}
		return (List<String>) super.getValue();
	}

}
