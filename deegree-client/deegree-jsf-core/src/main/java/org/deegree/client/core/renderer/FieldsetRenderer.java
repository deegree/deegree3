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

import org.deegree.client.core.component.HtmlFieldset;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */

@FacesRenderer(componentFamily = "javax.faces.Panel", rendererType = "org.deegree.Fieldset")
public class FieldsetRenderer extends Renderer {

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		if (component instanceof HtmlFieldset) {
			HtmlFieldset fieldset = (HtmlFieldset) component;
			ResponseWriter writer = FacesContext.getCurrentInstance().getResponseWriter();
			writer.startElement("fieldset", component);
			writer.writeAttribute("id", fieldset.getClientId(), "id");
			String styleClass = fieldset.getStyleClass();
			if (styleClass != null) {
				writer.writeAttribute("class", styleClass, "styleClass");
			}
			String style = fieldset.getStyle();
			if (style != null) {
				writer.writeAttribute("style", style, "style");
			}
			String legend = fieldset.getLegend();
			if (legend != null) {
				writer.startElement("legend", null);
				writer.write(legend);
				writer.endElement("legend");
			}
		}
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		if (component instanceof HtmlFieldset) {
			ResponseWriter writer = FacesContext.getCurrentInstance().getResponseWriter();
			writer.endElement("fieldset");
		}
	}

}
