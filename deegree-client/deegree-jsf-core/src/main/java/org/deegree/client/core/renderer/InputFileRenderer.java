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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.deegree.client.core.component.HtmlInputFile;
import org.deegree.client.core.model.UploadedFile;
import org.deegree.commons.utils.TempFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>InputFileRenderer</code> rendering an input form element of type file.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@ResourceDependencies({ @ResourceDependency(library = "deegree", name = "css/inputFile.css") })
@FacesRenderer(componentFamily = "javax.faces.Input", rendererType = "org.deegree.InputFile")
public class InputFileRenderer extends Renderer {

	private static Logger LOG = LoggerFactory.getLogger(HtmlInputFile.class);

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		HtmlInputFile input = (HtmlInputFile) component;

		ResponseWriter writer = context.getResponseWriter();
		String clientId = component.getClientId();

		writer.startElement("input", null);
		writer.writeAttribute("id", clientId, "id");
		writer.writeAttribute("name", clientId, "clientId");
		writer.writeAttribute("type", "file", "file");

		String styleClass = input.getStyleClass();
		if (styleClass != null) {
			writer.writeAttribute("class", styleClass, "styleClass");
		}
		String style = input.getStyle();
		if (style != null) {
			writer.writeAttribute("style", style, "style");
		}

		writer.endElement("input");
		writer.flush();
	}

	@Override
	public void decode(FacesContext context, UIComponent component) {
		ExternalContext external = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) external.getRequest();
		String clientId = component.getClientId(context);

		FileItem item = (FileItem) request.getAttribute(clientId);

		HtmlInputFile fileComponent = (HtmlInputFile) component;
		UploadedFile uploadedFile = new UploadedFile();
		if (item != null) {
			try {
				String target = fileComponent.getTarget();
				URL url = getUrl(request, target, item.getName());
				ServletContext sc = (ServletContext) external.getContext();
				File file = getTargetFile(sc, target, item.getName());
				item.write(file);
				uploadedFile.setFileItem(item);
				uploadedFile.setUrl(url);
				uploadedFile.setAbsolutePath(file.getAbsolutePath());
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
		fileComponent.setSubmittedValue(uploadedFile);
	}

	private File getTargetFile(ServletContext sc, String target, String fileName) throws IOException {
		File targetFile = null;
		if (target != null) {
			if (!target.endsWith("/")) {
				target += "/";
			}
			targetFile = new File(sc.getRealPath(target + fileName));
		}
		else {
			// use deegree's temp directory
			File tempDir = TempFileManager.getBaseDir();
			targetFile = File.createTempFile("upload", "", tempDir);
		}

		LOG.info("Uploading file '" + fileName + "' to: '" + targetFile + "'");
		return targetFile;
	}

	private URL getUrl(HttpServletRequest request, String target, String fileName) throws MalformedURLException {
		if (target == null) {
			target = "";
		}
		if (!target.startsWith("/")) {
			target = "/" + target;
		}
		if (!target.endsWith("/")) {
			target += "/";
		}
		return new URL(request.getScheme(), request.getServerName(), request.getServerPort(),
				request.getContextPath() + target + fileName);
	}

	@Override
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
			throws ConverterException {
		return submittedValue;
	}

}
