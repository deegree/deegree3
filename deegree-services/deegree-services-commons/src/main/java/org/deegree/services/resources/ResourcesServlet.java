/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.services.resources;

import static org.h2.util.IOUtils.copyAndClose;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to service-related resources stored in the active workspace, e.g. XML
 * schema files.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ResourcesServlet extends HttpServlet {

	private static final Logger LOG = LoggerFactory.getLogger(ResourcesServlet.class);

	private static final long serialVersionUID = -2072170206703402474L;

	/**
	 * Returns the URL for retrieving the specified workspace file via HTTP.
	 * <p>
	 * NOTE: This method will only return a correct result if the calling thread
	 * originated in the {@link #doGet(HttpServletRequest, HttpServletResponse)} or
	 * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been
	 * spawned as a child thread by such a thread).
	 * </p>
	 * @param workspaceFilePath relative path to the workspace file, must not be
	 * <code>null</code>
	 * @return the URL, never <code>null</code>
	 */
	public static String getUrl(String workspaceFilePath) {
		return OGCFrontController.getResourcesUrl() + "/" + workspaceFilePath;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String resourcePath = request.getPathInfo();
		if (!resourcePath.startsWith("/")) {
			throw new ServletException("Requested resource path does not start with '/'.");
		}
		// .wsdl/ALL is special handling for WSDL of WPS
		if (!resourcePath.toLowerCase().endsWith(".xsd") && !resourcePath.toLowerCase().endsWith(".wsdl")
				&& !resourcePath.endsWith(".wsdl/ALL")) {
			throw new ServletException("Requested resource path does not end with '.xsd', '.wsdl' or '.wsdl/ALL'.");
		}
		resourcePath = resourcePath.substring(1);

		// Special handling for WSDL of WPS
		if (resourcePath.endsWith(".wsdl/ALL"))
			resourcePath = resourcePath.substring(0, resourcePath.length() - 4);

		LOG.debug("Requested resource: " + resourcePath);
		File wsDir = OGCFrontController.getServiceWorkspace().getLocation();
		File resource = new File(wsDir, resourcePath);
		if (!resource.exists()) {
			throw new ServletException("Resource " + resourcePath + " does not exist.");
		}
		if (!resource.isFile()) {
			throw new ServletException("Resource " + resourcePath + " does not denote a file.");
		}
		sendResource(resource, response);
	}

	private void sendResource(File resource, HttpServletResponse response) throws IOException {

		response.setContentLength((int) resource.length());
		String mimeType = determineMimeType(resource);
		response.setContentType(mimeType);

		copyAndClose(new FileInputStream(resource), response.getOutputStream());
	}

	private String determineMimeType(File resource) {
		// TODO
		return "text/xml";
	}

}
