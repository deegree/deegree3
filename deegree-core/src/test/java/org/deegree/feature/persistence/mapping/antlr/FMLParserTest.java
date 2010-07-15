//$HeadURL$
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
package org.deegree.feature.persistence.mapping.antlr;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

/**
 * Tests the parsing of FML (FeatureMappingLanguage) expressions.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FMLParserTest {

    @Test
    public void testParsingUnqualifiedColumn()
                            throws RecognitionException {
        ANTLRStringStream in = new ANTLRStringStream( "NAME" );
        FMLLexer lexer = new FMLLexer( in );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        FMLParser parser = new FMLParser( tokens );
        Object result = parser.eval().value;
        // System.out.println (result);
    }

//    @Test(expected = ExpectException.class)
//    public void testParsingBrokenExpression()
//                            throws RecognitionException {
//        ANTLRStringStream in = new ANTLRStringStream( "NAME NAME" );
//        FMLLexer lexer = new FMLLexer( in );
//        CommonTokenStream tokens = new CommonTokenStream( lexer );
//        FMLParser parser = new FMLParser( tokens );
//        parser.eval();
//    }

}
