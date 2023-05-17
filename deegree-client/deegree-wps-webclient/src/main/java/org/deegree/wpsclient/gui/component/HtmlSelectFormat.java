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
package org.deegree.wpsclient.gui.component;

import javax.faces.component.FacesComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.deegree.protocol.wps.client.param.ComplexFormat;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesComponent(value = "HtmlSelectFormat")
public class HtmlSelectFormat extends UISelectOne {

	/**
	 * <p>
	 * The standard component type for this component.
	 * </p>
	 */
	public static final String COMPONENT_TYPE = "HtmlSelectFormat";

	private static enum AdditionalPropertyKeys {

		styleClass, defaultFormat, supportedFormats, onchange

	}

	public HtmlSelectFormat() {
		setRendererType("org.deegree.SelectFormat");
	}

	public String getOnchange() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.onchange, null);
	}

	public void setOnchange(String onchange) {
		getStateHelper().put(AdditionalPropertyKeys.onchange, onchange);
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.styleClass, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(AdditionalPropertyKeys.styleClass, styleClass);
	}

	public ComplexFormat getDefaultFormat() {
		return (ComplexFormat) getStateHelper().eval(AdditionalPropertyKeys.defaultFormat, null);
	}

	public void setDefaultFormat(ComplexFormat defaultFormat) {
		getStateHelper().put(AdditionalPropertyKeys.defaultFormat, defaultFormat);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * javax.faces.component.UISelectOne#validateValue(javax.faces.context.FacesContext,
	 * java.lang.Object)
	 */
	@Override
	protected void validateValue(FacesContext context, Object value) {
		super.validateValue(context, value);
	}

}
