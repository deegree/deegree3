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

import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;

/**
 * <code>HtmlExternalLink</code> a link component ignoring the JSF navigation
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesComponent(value = "HtmlExternalLink")
public class HtmlExternalLink extends UICommand {

	public HtmlExternalLink() {
		setRendererType("org.deegree.ExternalLink");
	}

	private static enum AdditionalPropertyKeys {

		href, onclick, style, styleClass, title, target

	}

	public void setHref(String href) {
		getStateHelper().put(AdditionalPropertyKeys.href, href);
	}

	public String getHref() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.href, "href");
	}

	public void setTarget(String target) {
		getStateHelper().put(AdditionalPropertyKeys.target, target);
	}

	public String getTarget() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.target, "target");
	}

	public void setTitle(String title) {
		getStateHelper().put(AdditionalPropertyKeys.title, title);
	}

	public String getTitle() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.title, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(AdditionalPropertyKeys.styleClass, styleClass);
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.styleClass, "styleClass");
	}

	public void setStyle(String style) {
		getStateHelper().put(AdditionalPropertyKeys.style, style);
	}

	public String getStyle() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.style, "style");
	}

	public void setOnclick(String onclick) {
		getStateHelper().put(AdditionalPropertyKeys.onclick, onclick);
	}

	public String getOnclick() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.onclick, null);
	}

}
