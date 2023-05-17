/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.services.wms.controller.capabilities.serialize;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.deegree.commons.xml.XsltUtils;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Transforms the incoming capabilities XML via XSLT and writes the output in the response
 * stream.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class XsltCapabilitiesSerializer implements CapabilitiesSerializer {

	private static final Logger LOG = getLogger(XsltCapabilitiesSerializer.class);

	private final URL xslt;

	private Workspace workspace;

	/**
	 * @param xslt path to the xslt file, never <code>null</code>
	 * @param workspace never <code>null</code>
	 */
	public XsltCapabilitiesSerializer(URL xslt, Workspace workspace) {
		this.xslt = xslt;
		this.workspace = workspace;
	}

	@Override
	public void serialize(InputStream capabilitiesXmlStream, OutputStream responseStream) throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(workspace.getModuleClassLoader());
		try {
			XsltUtils.transform(capabilitiesXmlStream, this.xslt, responseStream);
		}
		catch (Exception e) {
			LOG.warn("Unable to transform Capabilities: {}.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

}