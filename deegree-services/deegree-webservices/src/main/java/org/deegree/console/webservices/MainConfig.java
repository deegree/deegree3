/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.console.webservices;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.deegree.console.Config;
import org.deegree.services.controller.OGCFrontController;

/**
 * Config implementation for the main.xml.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class MainConfig extends Config implements Serializable {

	private static final long serialVersionUID = -8185523546919352171L;

	private static final URL MAIN_SCHEMA_URL = ServicesBean.class
		.getResource("/META-INF/schemas/services/controller/controller.xsd");

	public MainConfig() {
		super(null, null, "/console/webservices/index", false);
	}

	@Override
	public String edit() throws IOException {
		String workspaceName = OGCFrontController.getServiceWorkspace().getName();
		String fileName = workspaceName + "/services/main.xml";

		StringBuilder sb = new StringBuilder("/console/generic/xmleditor?faces-redirect=true");
		sb.append("&id=").append(id);
		sb.append("&schemaUrl=").append(MAIN_SCHEMA_URL.toString());
		sb.append("&fileName=").append(fileName);
		sb.append("&emptyTemplate=").append(getTemplate());
		sb.append("&nextView=").append(getResourceOutcome());
		return sb.toString();
	}

	@Override
	public String getSchemaAsText() {
		try {
			return IOUtils.toString(MAIN_SCHEMA_URL);
		}
		catch (IOException e) {
			// ignore
		}
		return "";
	}

	@Override
	public URL getTemplate() {
		return ServicesBean.class.getResource("/META-INF/schemas/services/controller/example.xml");
	}

	@Override
	public URL getSchemaURL() {
		return MAIN_SCHEMA_URL;
	}

}
