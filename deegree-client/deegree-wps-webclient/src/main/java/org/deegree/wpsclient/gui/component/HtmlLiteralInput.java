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

import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.deegree.client.core.utils.MessageUtils;
import org.deegree.commons.utils.StringPair;

/**
 * <code>HtmlLiteralInput</code> capsulates a LiteralInput GUI field
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesComponent(value = "HtmlLiteralInput")
public class HtmlLiteralInput extends UISelectOne {

	/**
	 * <p>
	 * The standard component type for this component.
	 * </p>
	 */
	public static final String COMPONENT_TYPE = "HtmlLiteralInput";

	private static enum AdditionalPropertyKeys {

		styleClass, defaultUom, dataType, allowedValues

	}

	public HtmlLiteralInput() {
		setRendererType("org.deegree.LiteralInput");
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.styleClass, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(AdditionalPropertyKeys.styleClass, styleClass);
	}

	public String getDefaultUom() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.defaultUom, null);
	}

	public void setDefaultUom(String defaultUom) {
		getStateHelper().put(AdditionalPropertyKeys.defaultUom, defaultUom);
	}

	public String getDataType() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.dataType, null);
	}

	public void setDataType(String dataType) {
		getStateHelper().put(AdditionalPropertyKeys.dataType, dataType);
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllowedValues() {
		Object allowedValues = getStateHelper().eval(AdditionalPropertyKeys.styleClass, null);
		if (allowedValues != null && allowedValues instanceof List<?>)
			return (List<String>) allowedValues;
		return Collections.EMPTY_LIST;
	}

	public void setAllowedValues(List<String> allowedValues) {
		getStateHelper().put(AdditionalPropertyKeys.allowedValues, allowedValues);
	}

	@Override
	protected void validateValue(FacesContext context, Object newValue) {
		if (!isValid()) {
			return;
		}
		if (newValue instanceof StringPair) {
			String value = ((StringPair) newValue).getFirst();
			if (isRequired() && (value == null || value.trim().length() == 0)) {
				String requiredMessageStr = getRequiredMessage();
				FacesMessage message;
				if (null != requiredMessageStr) {
					message = new FacesMessage(FacesMessage.SEVERITY_ERROR, requiredMessageStr, requiredMessageStr);
				}
				else {
					message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR.REQUIRED_LITERAL_INPUT");
				}
				context.addMessage(getClientId(context), message);
				setValid(false);
			}
			else if (value != null && value.length() > 0) {
				// TODO
				List<String> allowedValues = getAllowedValues();
				String dataType = getDataType();
				if ("integer".equalsIgnoreCase(dataType)) {
					try {
						Integer.parseInt(value);
					}
					catch (NumberFormatException e) {
						FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
								"ERROR.INVALID_LITERAL_INPUT_INTEGER", value);
						context.addMessage(getClientId(context), message);
						setValid(false);
						return;
					}
				}
				else if ("double".equalsIgnoreCase(dataType)) {
					try {
						Double.parseDouble(value);
					}
					catch (NumberFormatException e) {
						FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
								"ERROR.INVALID_LITERAL_INPUT_DOUBLE", value);
						context.addMessage(getClientId(context), message);
						setValid(false);
						return;
					}
				}
			}
		}
	}

}
