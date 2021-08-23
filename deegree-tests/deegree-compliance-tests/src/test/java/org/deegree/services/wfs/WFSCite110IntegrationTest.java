//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.services.wfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deegree.services.AbstractCiteIntegrationTest;
import org.deegree.services.CiteWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Wraps the execution of the CITE WFS 1.1.0 TestSuite as a JUnit-test.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 22950 $, $Date: 2010-03-09 19:05:17 +0100 (Di, 09. Mär
 *          2010) $
 */
@RunWith(Parameterized.class)
public class WFSCite110IntegrationTest extends AbstractCiteIntegrationTest {

	private static String CITE_SCRIPT_PROP = "cite.script";

	private String testLabel = "WFS110";

	@Parameters
	public static Collection getResultSnippets() throws Exception {
		return getResultSnippets("/citewfs110/src/main.xml", "capabilities-url", "wfs110?request=GetCapabilities&service=WFS");
	}

	public WFSCite110IntegrationTest(String testLabel, String resultSnippet) {
		this.testLabel = testLabel;
		this.resultSnippet = resultSnippet;
	}

	@Test
	public void singleTest() {
		if (resultSnippet.contains("Failed")) {
			throw new RuntimeException("Test '" + testLabel + "' failed.");
		}
	}
}
