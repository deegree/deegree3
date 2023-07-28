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
package org.deegree.client.core.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import org.deegree.client.core.component.HtmlInputMultiple;
import org.deegree.client.core.component.HtmlInputMultipleText;
import org.deegree.client.core.utils.RendererUtils;

/**
 * Render a {@link HtmlInputMultiple}
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesRenderer(componentFamily = "javax.faces.Input", rendererType = "org.deegree.InputMultipleText")
public class InputMultipleTextRenderer extends Renderer {

	@Override
	public void decode(FacesContext context, UIComponent component) {
		ExternalContext external = context.getExternalContext();
		Map<String, String> params = external.getRequestParameterMap();

		List<String> values = new ArrayList<String>();

		for (String key : params.keySet()) {
			if (key.startsWith(component.getClientId(context) + ":child_")) {
				values.add(params.get(key));
			}
		}
		HtmlInputMultipleText multiple = (HtmlInputMultipleText) component;
		multiple.setSubmittedValue(values);
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId();
		HtmlInputMultipleText multiple = (HtmlInputMultipleText) component;

		writer.startElement("div", component);
		writer.writeAttribute("name", clientId, "clientId");

		writer.writeAttribute("id", clientId, "clientId");
		String styleClass = multiple.getStyleClass();
		if (styleClass == null) {
			styleClass = "multipleComponent";
		}
		else {
			styleClass += " multipleComponent";
		}
		writer.writeAttribute("class", styleClass, "styleClass");

		String style = multiple.getStyle();
		if (style != null) {
			writer.writeAttribute("style", style, "style");
		}
		boolean disabled = multiple.getDisabled();

		renderChilds(context, writer, multiple, disabled);
		String templateID = getTemplateID(context, multiple);
		renderInput(context, writer, null, templateID, true, disabled);

	}

	private void renderChilds(FacesContext context, ResponseWriter writer, HtmlInputMultipleText multiple,
			boolean disabled) throws IOException {
		List<String> list = multiple.getValue();
		if (list != null && list.size() > 0) {
			for (String v : list) {
				String id = multiple.getClientId(context) + ":child_" + list.indexOf(v);
				renderInput(context, writer, v, id, false, disabled);
			}
		}
		else {
			String id = multiple.getClientId(context) + ":child_" + 0;
			renderInput(context, writer, null, id, false, disabled);
		}

	}

	private void renderInput(FacesContext context, ResponseWriter writer, String value, String id, boolean isHidden,
			boolean disabled) throws IOException {

		writer.startElement("div", null);
		if (isHidden) {
			writer.writeAttribute("style", "display:none;", null);
		}
		writer.writeAttribute("id", id, null);
		writer.startElement("input", null);
		writer.writeAttribute("type", "text", null);
		writer.writeAttribute("name", id, "clientId");
		if (disabled)
			writer.writeAttribute("disabled", "disabled", "disabled");

		// render default text specified
		if (value != null) {
			writer.writeAttribute("value", value, "value");
		}

		writer.endElement("input");

		RendererUtils.writeClickImage(context, writer, "close", "deegree", "images/delete.png", getRemoveJS());
		writer.endElement("div");
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();

		writer.startElement("div", null);
		writer.writeAttribute("class", "add", null);
		RendererUtils.writeClickImage(context, writer, "add", "deegree", "images/add.png",
				getAddJS(getTemplateID(context, component)));
		writer.endElement("div");
		writer.endElement("div");
	}

	private String getAddJS(String templateId) {
		return "javascript:add(this, '" + templateId + "'); return false;";
	}

	private String getRemoveJS() {
		return "javascript:remove(this); return false;";
	}

	private String getTemplateID(FacesContext context, UIComponent component) {
		return component.getClientId(context) + ":template";
	}

}
