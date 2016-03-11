//$HeadURL$
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
package org.deegree.style.se.unevaluated;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.expression.ValueReference;
import org.deegree.style.se.parser.SymbologyParser;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StyleTest {

    private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newInstance();

    @Test
    public void testRetrieveValueReferences_SE_TextSymbolizer()
                            throws Exception {
        InputStream style = this.getClass().getResourceAsStream( "SE_TextSymbolizerWithLabel.xml" );
        XMLStreamReader in = XML_FACTORY.createXMLStreamReader( style );
        in.next();

        Style parsedStyle = SymbologyParser.INSTANCE.parse( in );
        List<ValueReference> valueReferences = parsedStyle.retrieveValueReferences();

        assertThat( valueReferences.size(), is( 2 ) );
        assertThat( valueReferences.get( 0 ).getAsQName(), is( new QName( "AREA" ) ) );
        assertThat( valueReferences.get( 1 ).getAsQName(), is( new QName( "SOME_PROP" ) ) );
    }

    @Test
    public void testRetrieveValueReferences_SE_LineSymbolizer()
                            throws Exception {
        InputStream style = this.getClass().getResourceAsStream( "SE_LineSymbolizerWithStrokeAsPropertyName.xml" );
        XMLStreamReader in = XML_FACTORY.createXMLStreamReader( style );
        in.next();

        Style parsedStyle = SymbologyParser.INSTANCE.parse( in );
        List<ValueReference> valueReferences = parsedStyle.retrieveValueReferences();

        assertThat( valueReferences.size(), is( 1 ) );
        assertThat( valueReferences.get( 0 ).getAsQName(),
                    is( new QName( "http://www.deegree.org/app", "strokeProp" ) ) );
    }

    @Test
    public void testRetrieveValueReferences_SLD_PolygonSymbolizer()
                            throws Exception {
        InputStream style = this.getClass().getResourceAsStream( "SLD_PolygonSymbolizerWithFillAndCssParams.xml" );
        XMLStreamReader in = XML_FACTORY.createXMLStreamReader( style );
        in.next();

        Style parsedStyle = SymbologyParser.INSTANCE.parse( in );
        List<ValueReference> valueReferences = parsedStyle.retrieveValueReferences();

        assertThat( valueReferences.size(), is( 5 ) );
        assertThat( valueReferences.get( 0 ).getAsQName(), is( new QName( "fillG" ) ) );
        assertThat( valueReferences.get( 1 ).getAsQName(), is( new QName( "strokeG" ) ) );
        assertThat( valueReferences.get( 2 ).getAsQName(), is( new QName( "strokeB" ) ) );
        assertThat( valueReferences.get( 3 ).getAsQName(), is( new QName( "width" ) ) );
        assertThat( valueReferences.get( 4 ).getAsQName(), is( new QName( "linecap" ) ) );
    }

    @Test
    public void testRetrieveValueReferences_SLD_PointSymbolizer()
                            throws Exception {
        InputStream style = this.getClass().getResourceAsStream( "SLD_PointSymbolizerWithCustomExpression.xml" );
        XMLStreamReader in = XML_FACTORY.createXMLStreamReader( style );
        in.next();

        Style parsedStyle = SymbologyParser.INSTANCE.parse( in );
        List<ValueReference> valueReferences = parsedStyle.retrieveValueReferences();

        System.out.println( valueReferences );
        assertThat( valueReferences.size(), is( 4 ) );
    }

}