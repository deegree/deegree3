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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.featureinfo.templating.lang.Definition;
import org.junit.Test;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class Templating2ParserTest {

	@Test
	public void testMapDefinition() throws IOException, RecognitionException {
		Templating2Parser parser = getParser("map.gfi");
		Map<String, Definition> defs = parser.definitions();
		Assert.assertEquals(2, defs.size());
		Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
	}

	@Test
	public void testMapDefinitionUmlauts() throws IOException, RecognitionException {
		Templating2Parser parser = getParser("mapumlauts.gfi");
		Map<String, Definition> defs = parser.definitions();
		Assert.assertEquals(2, defs.size());
		Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
	}

	@Test
	public void testError() throws IOException, RecognitionException {
		Templating2Parser parser = getParser("error.gfi");
		Map<String, Definition> defs = parser.definitions();
		Assert.assertEquals(1, defs.size());
		Assert.assertEquals(1, parser.getNumberOfSyntaxErrors());
	}

	@Test
	public void testUtah1() throws IOException, RecognitionException {
		Templating2Parser parser = getParser("utahdemo.gfi");
		Map<String, Definition> defs = parser.definitions();
		Assert.assertEquals(3, defs.size());
		Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
	}

	@Test
	public void testUtah2() throws IOException, RecognitionException {
		Templating2Parser parser = getParser("utahdemo2.gfi");
		Map<String, Definition> defs = parser.definitions();
		Assert.assertEquals(6, defs.size());
		Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
	}

	@Test
	public void testStandardTemplate() throws IOException, RecognitionException {
		Templating2Parser parser = getParser("../html.gfi");
		Map<String, Definition> defs = parser.definitions();
		Assert.assertEquals(4, defs.size());
		Assert.assertEquals(0, parser.getNumberOfSyntaxErrors());
	}

	private static Templating2Parser getParser(String name) throws IOException {
		CharStream input = new ANTLRInputStream(Templating2ParserTest.class.getResourceAsStream(name));
		Templating2Lexer lexer = new Templating2Lexer(input);
		CommonTokenStream cts = new CommonTokenStream(lexer);
		return new Templating2Parser(cts);
	}

}
