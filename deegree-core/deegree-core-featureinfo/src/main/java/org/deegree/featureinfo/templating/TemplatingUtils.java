/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.featureinfo.templating;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.featureinfo.templating.lang.PropertyTemplateCall;
import org.slf4j.Logger;

/**
 * Utility method to run a template against a feature collection.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class TemplatingUtils {

	private static final Logger LOG = getLogger(TemplatingUtils.class);

	public static void runTemplate(OutputStream response, String fiFile, FeatureCollection col, boolean geometries)
			throws IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response, "UTF-8"));

		try {
			InputStream in;
			if (fiFile == null) {
				in = FeatureInfoManager.class.getResourceAsStream("html.gfi");
			}
			else {
				in = new FileInputStream(fiFile);
			}

			CharStream input = new ANTLRInputStream(in);
			Templating2Lexer lexer = new Templating2Lexer(input);
			CommonTokenStream cts = new CommonTokenStream(lexer);
			cts.fill();
			Templating2Parser parser = new Templating2Parser(cts);
			HashMap<String, Object> defs = (HashMap) parser.definitions();

			StringBuilder sb = new StringBuilder();
			new PropertyTemplateCall("start", singletonList("*"), false).eval(sb, defs, col, geometries);
			out.println(sb.toString());
		}
		catch (Throwable e) {
			if (fiFile == null) {
				LOG.error("Could not load internal template for GFI response.");
			}
			else {
				LOG.error("Could not load template '{}' for GFI response.", fiFile);
			}
			LOG.trace("Stack trace:", e);
		}
		finally {
			out.close();
		}
	}

}
