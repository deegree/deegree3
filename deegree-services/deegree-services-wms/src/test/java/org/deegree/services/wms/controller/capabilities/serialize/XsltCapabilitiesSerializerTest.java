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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.deegree.workspace.Workspace;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class XsltCapabilitiesSerializerTest {

	@Test
	public void testSerialize() throws Exception {
		URL xslt = XsltCapabilitiesSerializerTest.class.getResource("capabilities2html.xsl");
		Workspace workspace = mockWorkspace();
		XsltCapabilitiesSerializer xsltCapabilitiesSerializer = new XsltCapabilitiesSerializer(xslt, workspace);

		ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
		xsltCapabilitiesSerializer.serialize(capabilitiesXmlStream(), responseStream);

		String html = responseStream.toString();

		assertThat(html, containsString("WMS"));
		assertThat(html, containsString("deegree WMS capabilities"));
	}

	private InputStream capabilitiesXmlStream() {
		return XsltCapabilitiesSerializerTest.class.getResourceAsStream("wmsCapabilities_130.xml");
	}

	private Workspace mockWorkspace() {
		return mock(Workspace.class);
	}

}
