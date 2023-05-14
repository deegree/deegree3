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
import javax.faces.component.UIPanel;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesComponent(value = "HtmlFieldset")
public class HtmlFieldset extends UIPanel {

	private static enum AdditionalPropertyKeys {

		styleClass, style, legend

	}

	public HtmlFieldset() {
		setRendererType("org.deegree.Fieldset");
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.styleClass, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(AdditionalPropertyKeys.styleClass, styleClass);
	}

	public String getStyle() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.style, null);
	}

	public void setStyle(String style) {
		getStateHelper().put(AdditionalPropertyKeys.style, style);
	}

	public String getLegend() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.legend, null);
	}

	public void setLegend(String legend) {
		getStateHelper().put(AdditionalPropertyKeys.legend, legend);
	}

}
