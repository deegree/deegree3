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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.SelectItemsIterator;
import org.deegree.commons.utils.StringPair;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.MenuRenderer;

/**
 * <code>LiteralInputRenderer</code> renderes a HtmlLiteralInput form element.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesRenderer(componentFamily = "javax.faces.SelectOne", rendererType = "org.deegree.LiteralInput")
public class LiteralInputRenderer extends MenuRenderer {

	private static final String UOM_SUFFIX = "UOM";

	private static final String VALUE_SUFFIX = "VALUE";

	@Override
	public void decode(FacesContext context, UIComponent component) {
		String clientId = component.getClientId();
		HtmlLiteralInput literalInput = (HtmlLiteralInput) component;
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
		String value = null;
		String uom = null;
		for (String key : requestMap.keySet()) {
			if ((clientId + ":" + VALUE_SUFFIX).equals(key)) {
				value = requestMap.get(key);
			}
			else if ((clientId + ":" + UOM_SUFFIX).equals(key)) {
				uom = requestMap.get(key);
			}
		}
		literalInput.setSubmittedValue(new StringPair(value, uom));
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		HtmlLiteralInput inputComponent = (HtmlLiteralInput) component;
		ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId();

		writer.startElement("span", component);
		writer.writeAttribute("id", clientId, "id");
		writer.writeAttribute("name", clientId, "id");
		if (inputComponent.getStyleClass() != null) {
			writer.writeAttribute("class", inputComponent.getStyleClass(), "styleClass");
		}

		String value = getCurrentValue(context, inputComponent);
		String uom = (String) getCurrentSelectedValues(inputComponent);

		writer.startElement("input", null);
		writer.writeAttribute("id", clientId + ":" + VALUE_SUFFIX, "id");
		writer.writeAttribute("name", clientId + ":" + VALUE_SUFFIX, "id");
		writer.writeAttribute("type", "text", "text");
		writer.writeAttribute("value", value, "value");
		writer.writeAttribute("style", "margin-right:10px", "style");
		writer.endElement("input");

		if (uom != null && inputComponent.getChildCount() == 1) {
			writer.writeText(uom, "uom");
		}
		else if (inputComponent.getChildCount() > 0) {
			writer.startElement("select", null);
			writer.writeAttribute("id", clientId + ":" + UOM_SUFFIX, "id");
			writer.writeAttribute("name", clientId + ":" + UOM_SUFFIX, "id");

			SelectItemsIterator<SelectItem> items = RenderKitUtils.getSelectItems(context, inputComponent);
			renderOptions(context, inputComponent, items);
			writer.endElement("select");
		}

	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("span");
	}

	@Override
	protected Object getCurrentSelectedValues(UIComponent component) {
		Object result = null;
		if (component instanceof HtmlLiteralInput) {
			Object selectedUom = ((HtmlLiteralInput) component).getValue();
			if (selectedUom != null && selectedUom instanceof StringPair) {
				result = ((StringPair) selectedUom).second;
			}
			else {
				result = ((HtmlLiteralInput) component).getDefaultUom();
			}
		}
		return result;
	}

	@Override
	protected Object[] getSubmittedSelectedValues(UIComponent component) {
		if (component instanceof HtmlLiteralInput) {
			Object selectedUom = ((HtmlLiteralInput) component).getValue();
			if (selectedUom != null && selectedUom instanceof StringPair)
				return new Object[] { ((StringPair) selectedUom).second };
		}
		return null;
	}

	@Override
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
			throws ConverterException {
		if (submittedValue != null && submittedValue instanceof StringPair) {
			return submittedValue;
		}
		return super.getConvertedValue(context, component, submittedValue);
	}

	@Override
	protected String getCurrentValue(FacesContext context, UIComponent component) {
		if (component instanceof HtmlLiteralInput) {
			Object submittedValue = ((UIInput) component).getSubmittedValue();
			if (submittedValue != null && submittedValue instanceof StringPair) {
				return ((StringPair) submittedValue).first;
			}
		}
		String currentValue = null;
		Object currentObj = getValue(component);
		if (currentObj != null && currentObj instanceof StringPair) {
			return ((StringPair) currentObj).first;
		}
		return currentValue;
	}

}
