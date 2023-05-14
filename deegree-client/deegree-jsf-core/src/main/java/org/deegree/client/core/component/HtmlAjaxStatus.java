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

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

import org.deegree.client.core.utils.MessageUtils;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@ResourceDependencies({ @ResourceDependency(library = "deegree", name = "css/ajaxStatus.css"),
		@ResourceDependency(name = "javascript/ajaxStatus.js", library = "deegree", target = "head"),
		@ResourceDependency(name = "jsf.js", library = "javax.faces", target = "head") })
@FacesComponent(value = "HtmlAjaxStatus")
public class HtmlAjaxStatus extends UIComponentBase {

	public static final String COMPONENT_TYPE = "HtmlAjaxStatus";

	public static final String FAMILY_TYPE = "org.deegre.Status";

	private static enum PropertyKeys {

		styleClass, forComponentId, modal, text, style

	}

	public HtmlAjaxStatus() {
		setRendererType("org.deegree.AjaxStatus");
	}

	@Override
	public String getFamily() {
		return FAMILY_TYPE;
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(PropertyKeys.styleClass, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(PropertyKeys.styleClass, styleClass);
	}

	public String getFor() {
		return (String) getStateHelper().eval(PropertyKeys.forComponentId, null);
	}

	public void setFor(String forComponentId) {
		getStateHelper().put(PropertyKeys.forComponentId, forComponentId);
	}

	public boolean getModal() {
		return (Boolean) getStateHelper().eval(PropertyKeys.modal, false);
	}

	public void setModal(boolean modal) {
		getStateHelper().put(PropertyKeys.modal, modal);
	}

	public String getText() {
		String text = (String) getStateHelper().eval(PropertyKeys.text, null);
		if (text == null) {
			text = MessageUtils.getResourceText(null, "org.deegree.client.core.component.HtmlAjaxStatus.TEXT");
		}
		return text;
	}

	public void setText(String text) {
		getStateHelper().put(PropertyKeys.text, text);
	}

	public String getStyle() {
		return (String) getStateHelper().eval(PropertyKeys.style, "");
	}

	public void setStyle(String style) {
		getStateHelper().put(PropertyKeys.style, style);
	}

}
