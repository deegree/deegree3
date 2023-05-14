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

import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import org.deegree.client.core.component.HtmlAjaxStatus;

import com.sun.faces.renderkit.html_basic.HtmlBasicRenderer;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesRenderer(componentFamily = "org.deegre.Status", rendererType = "org.deegree.AjaxStatus")
public class AjaxStatusRenderer extends HtmlBasicRenderer {

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId();
		HtmlAjaxStatus ajaxStatus = (HtmlAjaxStatus) component;
		boolean isModal = ajaxStatus.getModal();
		UIComponent forComponent = getForComponent(context, ajaxStatus.getFor(), component);

		String forId = forComponent != null ? "'" + forComponent.getClientId() + "'" : null;
		String jsToRegister = "registerAjaxStatus('" + clientId + "', " + isModal + ", " + forId + ");";

		// seems
		Resource js = context.getApplication()
			.getResourceHandler()
			.createResource("ajaxStatus.js", "deegree/javascript");

		writer.startElement("script", component);
		writer.writeAttribute("type", "text/javascript", "type");
		writer.writeAttribute("src", js.getRequestPath(), null);
		writer.writeText(jsToRegister, null);
		writer.endElement("script");

		writer.startElement("span", component);
		writer.writeAttribute("id", clientId, "id");
		writer.writeAttribute("name", clientId, "id");

		if (ajaxStatus.getStyleClass() != null)
			writer.writeAttribute("class", ajaxStatus.getStyleClass(), "id");
		writer.writeAttribute("style", "display: none;" + ajaxStatus.getStyle(), "id");

		String text = ajaxStatus.getText();
		if (isModal) {
			writer.startElement("div", null);
			writer.writeAttribute("id", "PLEASEWAIT", null);
			writer.startElement("div", null);
			writer.writeAttribute("class", "curved", null);

			writer.startElement("b", null);
			writer.writeAttribute("class", "b1", null);
			writer.endElement("b");
			writer.startElement("b", null);
			writer.writeAttribute("class", "b2", null);
			writer.endElement("b");
			writer.startElement("b", null);
			writer.writeAttribute("class", "b3", null);
			writer.endElement("b");
			writer.startElement("b", null);
			writer.writeAttribute("class", "b4", null);
			writer.endElement("b");

			writer.startElement("div", null);
			writer.writeAttribute("class", "boxcontent", null);
			writer.startElement("img", null);

			Resource resource = context.getApplication()
				.getResourceHandler()
				.createResource("ajaxStatusLoader.gif", "deegree/images");
			writer.writeAttribute("src", resource.getRequestPath(), null);
			writer.endElement("img");
			writer.startElement("p", null);
			writer.writeText(text, null);
			writer.endElement("p");
			writer.endElement("div");
			writer.startElement("b", null);
			writer.writeAttribute("class", "b4", null);
			writer.endElement("b");
			writer.startElement("b", null);
			writer.writeAttribute("class", "b3", null);
			writer.endElement("b");
			writer.startElement("b", null);
			writer.writeAttribute("class", "b2", null);
			writer.endElement("b");
			writer.startElement("b", null);
			writer.writeAttribute("class", "b1", null);
			writer.endElement("b");

			writer.endElement("div");
			writer.endElement("div");

			writer.startElement("div", null);
			writer.writeAttribute("id", "PLEASEWAIT_BG", null);
			writer.endElement("div");
		}
		else {
			writer.writeText(text, null);
		}
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("span");
	}

}
