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
package org.deegree.layer.persistence.feature;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Style;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class QueryBuilderTest {

    @Test
    public void testParseValueReferencesFromFilter_PropertyIsLessThan()
                            throws Exception {
        OperatorFilter filter = parseFilter( "PropertyIsLessThan.xml" );

        List<ValueReference> valueReferences = QueryBuilder.parseValueReferencesFromFilter( filter );

        assertThat( valueReferences.size(), is( 1 ) );
        assertThat( valueReferences.get( 0 ).getAsQName(), is( new QName( "http://www.deegree.org/app", "id" ) ) );
    }

    @Test
    public void testParseValueReferencesFromFilter_And()
                            throws Exception {
        OperatorFilter filter = parseFilter( "And.xml" );

        List<ValueReference> valueReferences = QueryBuilder.parseValueReferencesFromFilter( filter );

        assertThat( valueReferences.size(), is( 2 ) );
        assertThat( valueReferences.get( 0 ).getAsQName(),
                    is( new QName( "http://www.deegree.org/app", "dateOfBirth" ) ) );
        assertThat( valueReferences.get( 1 ).getAsQName(),
                    is( new QName( "http://www.deegree.org/app", "placeOfBirth" ) ) );
    }

    @Test
    public void testParseValueReferencesFromStyle()
                            throws Exception {
        Style filter = parseStyle( "Style.xml" );

        List<ValueReference> valueReferences = QueryBuilder.parseValueReferencesFromStyle( filter );

        assertThat( valueReferences.size(), is( 2 ) );
        assertThat( valueReferences.get( 0 ).getAsQName(),
                    is( new QName( "http://www.deegree.org/app", "category1" ) ) );
        assertThat( valueReferences.get( 1 ).getAsQName(),
                    is( new QName( "http://www.deegree.org/app", "category2" ) ) );
    }

    private OperatorFilter parseFilter( String resourceName )
                            throws Exception {
        InputStream resource = this.getClass().getResourceAsStream( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( resource );
        xmlStream.nextTag();
        Filter parse = Filter110XMLDecoder.parse( xmlStream );
        return (OperatorFilter) parse;
    }

    private Style parseStyle( String resourceName )
                            throws Exception {
        InputStream resource = this.getClass().getResourceAsStream( resourceName );
        try {
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( resource );
            xmlStream.nextTag();
            SymbologyParser symbologyParser = new SymbologyParser();
            return symbologyParser.parse( xmlStream );
        } finally {
            resource.close();
        }
    }

}