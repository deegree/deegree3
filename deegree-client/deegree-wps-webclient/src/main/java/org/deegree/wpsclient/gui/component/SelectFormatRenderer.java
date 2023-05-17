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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.model.SelectItem;
import javax.faces.render.FacesRenderer;

import com.sun.faces.renderkit.SelectItemsIterator;
import org.deegree.client.core.utils.JavaScriptUtils;
import org.deegree.protocol.wps.client.param.ComplexFormat;
import org.deegree.wpsclient.gui.converter.ComplexFormatConverter;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.MenuRenderer;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesRenderer(componentFamily = "javax.faces.SelectOne", rendererType = "org.deegree.SelectFormat")
public class SelectFormatRenderer extends MenuRenderer {

	public static final String FORMAT_SUFFIX = ":format";

	private static final String ONCHANGE_EVENT = "updateFormat";

	@Override
	public void decode(FacesContext context, UIComponent component) {
		String clientId = component.getClientId();
		HtmlSelectFormat literalInput = (HtmlSelectFormat) component;
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
		String format = null;
		for (String key : requestMap.keySet()) {
			if (clientId.equals(key)) {
				format = requestMap.get(key);
			}
		}
		literalInput.setSubmittedValue(format);
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		HtmlSelectFormat formatComp = (HtmlSelectFormat) component;
		ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId();
		writer.startElement("span", component);
		writer.writeAttribute("id", clientId, "id");
		// writer.writeAttribute( "name", clientId + ":super", "id" );
		if (formatComp.getStyleClass() != null) {
			writer.writeAttribute("class", formatComp.getStyleClass(), "styleClass");
		}

		ComplexFormat selectedFormat = (ComplexFormat) getCurrentSelectedValues(formatComp);

		writer.startElement("select", null);
		writer.writeAttribute("id", clientId + FORMAT_SUFFIX, "id");
		writer.writeAttribute("name", clientId, "id");
		String js = "";
		if (formatComp.getOnchange() != null) {
			js = formatComp.getOnchange();
		}
		js = js + getOnChangeBehaviour(clientId);
		writer.writeAttribute("onChange", js, "js");

		SelectItemsIterator<SelectItem> items = RenderKitUtils.getSelectItems(context, formatComp);
		renderOptions(context, formatComp, items);
		writer.endElement("select");

		writer.startElement("table", component);

		writer.startElement("tr", null);
		writer.startElement("td", null);
		writer.writeText("Schema:", null);
		writer.endElement("td");
		writer.startElement("td", null);
		writer.writeText(selectedFormat.getSchema() != null ? selectedFormat.getSchema() : "--", null);
		writer.endElement("td");
		writer.endElement("tr");

		writer.startElement("tr", null);
		writer.startElement("td", null);
		writer.writeText("Encoding:", null);
		writer.endElement("td");
		writer.startElement("td", null);
		writer.writeText(selectedFormat.getEncoding() != null ? selectedFormat.getEncoding() : "--", null);
		writer.endElement("td");
		writer.endElement("tr");

		writer.startElement("tr", null);
		writer.startElement("td", null);
		writer.writeText("Mime-Type:", null);
		writer.endElement("td");
		writer.startElement("td", null);
		writer.writeText(selectedFormat.getMimeType() != null ? selectedFormat.getMimeType() : "--", null);
		writer.endElement("td");
		writer.endElement("tr");

		writer.endElement("table");
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("span");
	}

	@Override
	protected Object getCurrentSelectedValues(UIComponent component) {
		Object result = null;
		if (component instanceof HtmlSelectFormat) {
			Object selectedFormat = ((HtmlSelectFormat) component).getValue();
			if (selectedFormat != null && selectedFormat instanceof ComplexFormat) {
				result = ((ComplexFormat) selectedFormat);
			}
			else {
				result = ((HtmlSelectFormat) component).getDefaultFormat();
			}
		}
		return result;
	}

	@Override
	protected String getCurrentValue(FacesContext context, UIComponent component) {
		if (component instanceof HtmlSelectFormat) {
			Object submittedValue = ((UIInput) component).getSubmittedValue();
			if (submittedValue != null && submittedValue instanceof ComplexFormat) {
				return ComplexFormatConverter.getAsString((ComplexFormat) submittedValue);
			}
		}
		String currentValue = null;
		Object currentObj = getValue(component);
		if (currentObj != null && currentObj instanceof ComplexFormat) {
			currentValue = ComplexFormatConverter.getAsString((ComplexFormat) currentObj);
		}
		return currentValue;
	}

	private String getOnChangeBehaviour(String clientId) {
		Map<String, String> options = new HashMap<String, String>();
		options.put("javax.faces.behavior.event", ONCHANGE_EVENT);
		options.put("execute", clientId);
		options.put("render", clientId);
		return JavaScriptUtils.getAjaxRequest(options, clientId);
	}

}
