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
package org.deegree.client.core.utils;

import java.io.IOException;

import javax.faces.application.Resource;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public final class RendererUtils {

	public static void writeClickImage(FacesContext context, ResponseWriter writer, String className, String library,
			String resourceName, String js) throws IOException {
		writeClickImage(context, writer, className, library, resourceName, js, false);
	}

	public static void writeClickImage(FacesContext context, ResponseWriter writer, String className, String library,
			String resourceName, String js, boolean disabled) throws IOException {
		writeClickImage(context, writer, className, library, resourceName, js, null, disabled);
	}

	public static void writeClickImage(FacesContext context, ResponseWriter writer, String className, String library,
			String resourceName, String js, String title, boolean disabled) throws IOException {
		writer.startElement("span", null);
		writer.writeAttribute("class", className, null);
		writer.startElement("input", null);
		if (disabled)
			writer.writeAttribute("disabled", "disabled", null);
		if (title != null)
			writer.writeAttribute("title", title, null);
		if (js != null)
			writer.writeAttribute("onclick", js, null);
		writer.writeAttribute("type", "image", null);
		Resource img = context.getApplication().getResourceHandler().createResource(resourceName, library);
		if (img != null) {
			writer.writeAttribute("src", img.getRequestPath(), null);
		}
		writer.endElement("input");
		writer.endElement("span");
	}

}
