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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import org.deegree.client.core.component.HtmlExternalLink;

/**
 * <code>ExternalLinkRenderer</code> renders a link ignoring the JSF navigation
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */

@FacesRenderer(componentFamily = "javax.faces.Command", rendererType = "org.deegree.ExternalLink")
public class ExternalLinkRenderer extends Renderer {

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter responseWriter = context.getResponseWriter();
		if (component instanceof HtmlExternalLink) {
			HtmlExternalLink command = (HtmlExternalLink) component;
			String clientId = command.getClientId();

			responseWriter.startElement("a", null);
			responseWriter.writeAttribute("id", clientId, "id");
			responseWriter.writeAttribute("name", clientId, "clientId");
			String href = command.getHref();
			responseWriter.writeAttribute("href", href, null);

			String styleClass = command.getStyleClass();
			if (styleClass != null) {
				responseWriter.writeAttribute("class", styleClass, "styleClass");
			}

			String style = command.getStyle();
			if (style != null) {
				responseWriter.writeAttribute("style", style, "style");
			}
			String target = command.getTarget();
			if (target != null) {
				responseWriter.writeAttribute("target", target, "target");
			}

			String onclick = command.getOnclick();
			if (onclick != null) {
				responseWriter.writeAttribute("onClick", onclick, "onclick");
			}

			String title = command.getTitle();
			responseWriter.writeText(title != null ? title : (href != null ? href : ""), null);

			responseWriter.endElement("a");
			responseWriter.flush();
		}
	}

}
