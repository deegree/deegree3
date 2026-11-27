/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2026 by:
 - IDgis B.V. -

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

 IDgis B.V.
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 https://www.idgis.nl

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.launcher;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		String host = System.getProperty("DEEGREE_LAUNCHER_HOST", "0.0.0.0");
		int port = Integer.parseInt(System.getProperty("DEEGREE_LAUNCHER_PORT", "8080"));
		String contextPath = System.getProperty("DEEGREE_LAUNCHER_CONTEXT_PATH", "/");

		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setHost(host);
		connector.setPort(port);
		server.addConnector(connector);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(contextPath);

		Path docBase = Files.createTempDirectory("deegree-docbase");
		webapp.setBaseResource(ResourceFactory.of(webapp).newResource(docBase));

		webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*");

		webapp.setConfigurationClasses(new String[] { "org.eclipse.jetty.ee10.webapp.WebInfConfiguration",
				"org.eclipse.jetty.ee10.webapp.WebXmlConfiguration",
				"org.eclipse.jetty.ee10.webapp.MetaInfConfiguration",
				"org.eclipse.jetty.ee10.webapp.FragmentConfiguration",
				"org.eclipse.jetty.ee10.annotations.AnnotationConfiguration",
				"org.eclipse.jetty.ee10.webapp.JettyWebXmlConfiguration" });

		webapp.setParentLoaderPriority(true);

		webapp.setThrowUnavailableOnStartupException(true);

		webapp.getHiddenClassMatcher().add("-org.eclipse.jetty.");

		server.setHandler(webapp);
		server.start();

		LOG.info("Successfully launched deegree, listening on http://{}:{}", host, port);

		server.join();
	}

}
