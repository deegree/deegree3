//$HeadURL$
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.featureinfo.templating.lang.Definition;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class Templating2ParserTest {

    @Test
    public void testMapDefinition()
                            throws IOException, RecognitionException {
        Templating2Parser parser = getParser( "map.gfi" );
        Map<String, Definition> defs = parser.definitions();
        Assert.assertEquals( 2, defs.size() );
        Assert.assertEquals( 0, parser.getNumberOfSyntaxErrors() );
    }

    @Test
    public void testMapDefinitionUmlauts()
                            throws IOException, RecognitionException {
        Templating2Parser parser = getParser( "mapumlauts.gfi" );
        Map<String, Definition> defs = parser.definitions();
        Assert.assertEquals( 2, defs.size() );
        Assert.assertEquals( 0, parser.getNumberOfSyntaxErrors() );
    }

    @Test
    public void testError()
                            throws IOException, RecognitionException {
        Templating2Parser parser = getParser( "error.gfi" );
        Map<String, Definition> defs = parser.definitions();
        Assert.assertEquals( 1, defs.size() );
        Assert.assertEquals( 1, parser.getNumberOfSyntaxErrors() );
    }

    @Test
    public void testUtah1()
                            throws IOException, RecognitionException {
        Templating2Parser parser = getParser( "utahdemo.gfi" );
        Map<String, Definition> defs = parser.definitions();
        Assert.assertEquals( 3, defs.size() );
        Assert.assertEquals( 0, parser.getNumberOfSyntaxErrors() );
    }

    @Test
    public void testUtah2()
                            throws IOException, RecognitionException {
        Templating2Parser parser = getParser( "utahdemo2.gfi" );
        Map<String, Definition> defs = parser.definitions();
        Assert.assertEquals( 6, defs.size() );
        Assert.assertEquals( 0, parser.getNumberOfSyntaxErrors() );
    }

    @Test
    public void testStandardTemplate()
                            throws IOException, RecognitionException {
        Templating2Parser parser = getParser( "../html.gfi" );
        Map<String, Definition> defs = parser.definitions();
        Assert.assertEquals( 4, defs.size() );
        Assert.assertEquals( 0, parser.getNumberOfSyntaxErrors() );
    }

    @Test
    public void testForceProperty()
                            throws IOException, RecognitionException {
        Templating2Parser parser = getParser( "forceproperty.gfi" );
        Map<String, Definition> defs = parser.definitions();
        Assert.assertEquals( 6, defs.size() );
        Assert.assertEquals( 0, parser.getNumberOfSyntaxErrors() );
    }

    @Test
    public void testForcePropertyEval()
                            throws URISyntaxException, IOException {
        File file = new File( Templating2ParserTest.class.getResource( "forcepropertyeval.gfi" ).toURI() );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FeatureCollection col = new GenericFeatureCollection();
        Feature f = new GenericFeature(null, null, new ArrayList(), null );
        col.add(f);
        TemplatingUtils.runTemplate( bos, file.toString(), col, false );
System.out.println(new String(bos.toByteArray()));
    }

    private static Templating2Parser getParser( String name )
                            throws IOException {
        CharStream input = new ANTLRInputStream( Templating2ParserTest.class.getResourceAsStream( name ) );
        Templating2Lexer lexer = new Templating2Lexer( input );
        CommonTokenStream cts = new CommonTokenStream( lexer );
        return new Templating2Parser( cts );
    }

}
