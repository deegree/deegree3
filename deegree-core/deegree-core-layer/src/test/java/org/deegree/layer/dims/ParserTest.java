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

package org.deegree.layer.dims;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

/**
 * <code>ParserTest</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class ParserTest extends TestCase {

	/**
	 * @throws Exception
	 */
	@Test
	public void testParser() throws Exception {
		DimensionsParser p = getParser("123.445");
		assertEquals("123.445", p.values.get(0));

		p = getParser("123.445/543/2");
		assertEquals("123.445/543/2", p.values.get(0).toString());

		p = getParser("123.445/543");
		assertEquals("123.445/543/0", p.values.get(0).toString());

		p = getParser("a,b,c");
		// do the quick'n'dirty list 'equals'
		assertEquals("[a, b, c]", p.values.toString());

		p = getParser("    a , b , c   ");
		assertEquals("[a, b, c]", p.values.toString());

		p = getParser("a/b/c,b/c/a,c/b/a");
		assertEquals("[a/b/c, b/c/a, c/b/a]", p.values.toString());

		p = getParser("1,2,");
		assertEquals("Expected another value after [1, 2].", p.error);

		p = getParser("1/3,2/54/gf,");
		assertEquals("Expected another value after [1/3/0, 2/54/gf].", p.error);

		p = getParser("1/3, single");
		assertEquals("[1/3/0, single]", p.values.toString());

		p = getParser("1/3, single/");
		assertEquals("Missing max value for interval.", p.error);

		p = getParser("single, pseudointerval/");
		assertEquals("Missing max value for interval.", p.error);

		p = getParser("one/two, three/four/, ");
		assertEquals("Expected another value after [one/two/0, three/four/0].", p.error);

		p = getParser("2000-01-01T00:00:00Z/2000-01-01T00:01:00Z/PT5S");
		assertEquals("2000-01-01T00:00:00Z/2000-01-01T00:01:00Z/PT5S", p.values.get(0).toString());
	}

	private static DimensionsParser getParser(String input) throws RecognitionException {
		DimensionsLexer lexer = new DimensionsLexer(new ANTLRStringStream(input));
		DimensionsParser parser = new DimensionsParser(new CommonTokenStream(lexer));
		try {
			parser.dimensionvalues();
		}
		catch (RecognitionException e) {
			// ignore exception, error message in the parser
		}
		return parser;
	}

}
