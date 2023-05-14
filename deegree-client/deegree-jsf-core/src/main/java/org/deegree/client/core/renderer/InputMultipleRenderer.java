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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import org.deegree.client.core.component.HtmlInputMultiple;
import org.deegree.client.core.utils.JavaScriptUtils;
import org.deegree.client.core.utils.MessageUtils;
import org.deegree.client.core.utils.RendererUtils;

/**
 * Render a {@link HtmlInputMultiple}
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesRenderer(componentFamily = "javax.faces.Input", rendererType = "org.deegree.InputMultiple")
public class InputMultipleRenderer extends Renderer {

	private static final String ADD_EVENT = "AddItem";

	private static final String DELETE_EVENT = "DeleteItem";

	private static final String INDEX_PARAM = "index";

	@Override
	public void decode(FacesContext context, UIComponent component) {
		ExternalContext external = context.getExternalContext();
		Map<String, String> params = external.getRequestParameterMap();
		String behaviorEvent = params.get("javax.faces.behavior.event");

		HtmlInputMultiple multiple = (HtmlInputMultiple) component;

		if (ADD_EVENT.equals(behaviorEvent)) {
			List<Object> value = (List<Object>) multiple.getValue();
			value.add(null);
			multiple.setSubmittedValue(value);
		}
		else if (DELETE_EVENT.equals(behaviorEvent)) {
			if (params.containsKey(INDEX_PARAM)) {
				int index = Integer.parseInt(params.get(INDEX_PARAM));
				List<Object> value = (List<Object>) multiple.getValue();
				value.remove(index);
				multiple.setSubmittedValue(value);
			}
		}
		else {
			System.out.println("save values!");
		}
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId();
		HtmlInputMultiple multiple = (HtmlInputMultiple) component;

		writer.startElement("div", component);
		writer.writeAttribute("name", clientId, "clientId");
		writer.writeAttribute("id", clientId, "clientId");

		writer.startElement("input", component);
		writer.writeAttribute("name", clientId, "clientId");
		writer.writeAttribute("type", "hidden", null);
		writer.endElement("input");

		writer.startElement("div", component);
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

		renderChilds(context, writer, multiple);
	}

	private void renderChilds(FacesContext context, ResponseWriter writer, HtmlInputMultiple multiple)
			throws IOException {
		List<Object> list = multiple.getValue();
		for (Object v : list) {
			int index = list.indexOf(v);
			boolean collapsed = multiple.isCollapsed(index);
			writer.startElement("div", null);

			// min /max /close
			writer.startElement("div", null);
			writer.writeAttribute("class", "menu", null);
			if (collapsed) {
				String js = "javascript:toggle(this, " + index + "); return false;";
				RendererUtils.writeClickImage(context, writer, "max", "deegree", "images/page_max.gif", js);
			}
			else {
				String js = "javascript:toggle(this, " + index + "); return false;";
				RendererUtils.writeClickImage(context, writer, "min", "deegree", "images/page_min.gif", js);
			}
			RendererUtils.writeClickImage(context, writer, "close", "deegree", "images/page_close.gif",
					getDeleteBehaviour(writer, multiple.getClientId(), index));
			writer.endElement("div");

			writer.startElement("div", null);
			writer.writeAttribute("style", "display:" + (collapsed ? "none;" : "inline;"), null);
			UIInput input = multiple.getInputInstance();
			input.setId(createChildId(multiple.getId(), index));
			input.setValue(v);
			input.encodeAll(context);
			writer.endElement("div");

			writer.startElement("br", null);
			writer.endElement("br");
			writer.endElement("div");
		}
	}

	private String createChildId(String id, int index) {
		return id + "_" + index;
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("div");

		writer.startElement("div", null);
		writer.writeAttribute("class", "add", null);
		RendererUtils.writeClickImage(context, writer, "add", "deegree", "images/add.png",
				getAddBehaviour(context, writer, component.getClientId()));
		writer.endElement("div");
		writer.endElement("div");
	}

	private String getDeleteBehaviour(ResponseWriter writer, String clientId, int index) throws IOException {
		Map<String, String> options = new HashMap<String, String>();
		options.put("javax.faces.behavior.event", DELETE_EVENT);
		options.put("execute", clientId);
		options.put("render", clientId);
		options.put(INDEX_PARAM, "" + index);

		String msg = MessageUtils.getResourceText(null,
				"org.deegree.client.core.renderer.InputMultipleRenderer.CONFIRM_MSG");
		return "javascript:confirmDelete('" + msg + "');" + JavaScriptUtils.getAjaxRequest(options, clientId)
				+ " return false;";
	}

	private String getAddBehaviour(FacesContext context, ResponseWriter writer, String clientId) throws IOException {
		Map<String, String> options = new HashMap<String, String>();
		options.put("javax.faces.behavior.event", ADD_EVENT);
		options.put("execute", clientId);
		options.put("render", clientId);
		return JavaScriptUtils.getAjaxRequest(options, clientId) + " return false;";
	}

}
