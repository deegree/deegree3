/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.test.cite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * <code>CiteWMS130TestSuite</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
@RunWith(Parameterized.class)
public class CiteWMS130TestSuite {

	private static String CITE_SCRIPT_PROP = "cite.script";

	private String testLabel;

	private String resultSnippet;

	public CiteWMS130TestSuite(String testLabel, String resultSnippet) {
		this.testLabel = testLabel;
		this.resultSnippet = resultSnippet;
	}

	@Parameters
	public static Collection getResultSnippets() throws Exception {

		CiteWrapper wrapper = new CiteWrapper(System.getProperty(CITE_SCRIPT_PROP));
		wrapper.execute();
		String out = wrapper.getOutput();
		String err = wrapper.getError();

		System.out.println(out);
		if (!err.isEmpty()) {
			System.out.println("Standard error messages: " + err);
		}

		return getResultSnippets(out);
	}

	private static Collection getResultSnippets(String out) throws IOException {

		List resultSnippets = new ArrayList();

		BufferedReader reader = new BufferedReader(new StringReader(out));
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		int currentLine = 0;
		while (currentLine < lines.size()) {
			String trimmed = lines.get(currentLine++).trim();
			if (trimmed.startsWith("Testing") && !trimmed.startsWith("Testing suite")) {
				String s = trimmed.substring(8);
				String caseId = s.substring(0, s.indexOf(' '));
				String result = findCorrespondingResult(lines, currentLine, caseId);
				resultSnippets.add(new Object[] { caseId, result });
			}
		}
		return resultSnippets;
	}

	private static String findCorrespondingResult(List<String> lines, int currentLine, String caseId) {
		while (currentLine < lines.size()) {
			String trimmed = lines.get(currentLine++).trim();
			if (trimmed.startsWith("Test " + caseId)) {
				return trimmed;
			}
		}
		throw new RuntimeException("Error parsing CITE result log.");
	}

	@Test
	public void singleTest() {
		if (resultSnippet.contains("Failed")) {
			throw new RuntimeException("Test '" + testLabel + "' failed.");
		}
	}

}
