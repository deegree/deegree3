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
package org.deegree.feature.xpath;

import static java.lang.Boolean.TRUE;
import static org.deegree.commons.tom.primitive.PrimitiveType.BOOLEAN;
import static org.deegree.commons.tom.primitive.PrimitiveType.DOUBLE;
import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.property.Property;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.PropertyName;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureReaderTest;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the correct evaluation of {@link FeatureXPath} expressions.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class FeatureXPathTest {

    private static final String BASE_DIR = "../../gml/feature/testdata/features/";

    private FeatureCollection fc;

    private SimpleNamespaceContext nsContext;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {

        String schemaURL = this.getClass().getResource( "../../gml/feature/testdata/schema/Philosopher.xsd" ).toString();
        ApplicationSchemaXSDDecoder xsdAdapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        nsContext = new SimpleNamespaceContext();
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        nsContext.addNamespace( "app", "http://www.deegree.org/app" );
    }

    @Test
    public void testXPath1()
                            throws FilterEvaluationException {
        String xpath = "*";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 7, result.length );
        for ( TypedObjectNode object : result ) {
            assertTrue( object instanceof Property );
        }
    }

    @Test
    public void testXPath2()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 7, result.length );
        for ( TypedObjectNode object : result ) {
            assertTrue( object instanceof Property );
        }
    }

    @Test
    public void testXPath3()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember/app:Philosopher";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 7, result.length );
        for ( TypedObjectNode object : result ) {
            assertTrue( object instanceof Feature );
        }
    }

    @Test
    public void testXPath4()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember[1]/app:Philosopher";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertEquals( 1, result.length );
        Feature feature = (Feature) result[0];
        assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @Test
    public void testXPath5()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember[1]/app:Philosopher/app:name";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertEquals( 1, result.length );
        SimpleProperty prop = (SimpleProperty) result[0];
        PrimitiveValue name = prop.getValue();
        assertEquals( STRING, name.getType() );
        assertEquals( "Karl Marx", name.getAsText() );
        assertTrue( name.getValue() instanceof String );
        // assertEquals( STRING, name.getXSType().getName() );
    }

    @Test
    public void testXPath6()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember[1]/app:Philosopher/app:name/text()";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertEquals( 1, result.length );
        PrimitiveValue name = (PrimitiveValue) result[0];
        assertEquals( STRING, name.getType() );
        assertEquals( "Karl Marx", name.getAsText() );
        assertTrue( name.getValue() instanceof String );
        // assertEquals( STRING, name.getXSType().getName() );
    }

    @Test
    public void testXPath7()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember/app:Philosopher[app:name='Albert Camus' and app:placeOfBirth/*/app:name='Mondovi']/app:placeOfBirth/app:Place/app:name";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertEquals( 1, result.length );
        SimpleProperty prop = (SimpleProperty) result[0];
        PrimitiveValue name = prop.getValue();
        assertEquals( STRING, name.getType() );
        assertEquals( "Mondovi", name.getAsText() );
        assertTrue( name.getValue() instanceof String );
    }

    @Test
    public void testXPath8()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember[1]/app:Philosopher/app:placeOfBirth/app:Place/../..";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertEquals( 1, result.length );
        Feature feature = (Feature) result[0];
        assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @Test
    public void testXPath9()
                            throws FilterEvaluationException {

        String xpath = "gml:featureMember/app:Philosopher[app:id < 3]/app:name";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        Set<String> names = new HashSet<String>();
        for ( TypedObjectNode node : result ) {
            names.add( ( (SimpleProperty) node ).getValue().toString() );
        }
        Assert.assertEquals( 2, names.size() );
        Assert.assertTrue( names.contains( "Friedrich Engels" ) );
        Assert.assertTrue( names.contains( "Karl Marx" ) );
    }

    // @Test
    // public void testXPath10()
    // throws FilterEvaluationException {
    // String xpath = "gml:featureMember/app:Philosopher/app:friend/app:Philosopher//app:name";
    // TypedObjectNode[] result = fc.evalXPath( new PropertyName( xpath, nsContext ), GML_31 );
    // for ( TypedObjectNode node : result ) {
    // System.out.println (node);
    // }
    // }

    @Test
    public void testXPath11()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember/app:Philosopher[@gml:id='PHILOSOPHER_1']";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertEquals( 1, result.length );
        Feature feature = (Feature) result[0];
        assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @Test
    public void testXPath12()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember/app:Philosopher[gml:name='JEAN_PAUL']";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertEquals( 1, result.length );
        Feature feature = (Feature) result[0];
        assertEquals( "PHILOSOPHER_6", feature.getId() );
    }

    @Test
    public void testXPath13()
                            throws FilterEvaluationException {
        String xpath = "/gml:FeatureCollection/gml:featureMember";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 7, result.length );
        for ( TypedObjectNode object : result ) {
            assertTrue( object instanceof Property );
        }
    }

    @Test
    public void testXPath14()
                            throws FilterEvaluationException {
        String xpath = "true()";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 1, result.length );
        PrimitiveValue value = (PrimitiveValue) result[0];
        assertEquals( BOOLEAN, value.getType() );
        assertEquals( TRUE, value.getValue() );
    }

    @Test
    public void testXPath15()
                            throws FilterEvaluationException {
        String xpath = "count(gml:featureMember/app:Philosopher)";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 1, result.length );
        PrimitiveValue value = (PrimitiveValue) result[0];
        assertEquals( DOUBLE, value.getType() );
        assertEquals( new Double( 7.0 ), value.getValue() );
    }

    @Test
    public void testXPath16()
                            throws FilterEvaluationException {
        String xpath = "string(gml:featureMember/app:Philosopher/app:name)";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 1, result.length );
        PrimitiveValue value = (PrimitiveValue) result[0];
        assertEquals( STRING, value.getType() );
        assertEquals( "Albert Camus", value.getValue() );
    }

    @Test
    public void testXPath17()
                            throws FilterEvaluationException {
        String xpath = "/";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 1, result.length );
        FeatureCollection fc2 = (FeatureCollection) result[0];
        assertTrue( fc == fc2 );
    }

    @Test
    public void testXPath18()
                            throws FilterEvaluationException {
        String xpath = "gml:featureMember/app:Philosopher/@gml:id";
        TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new PropertyName( xpath, nsContext ) );
        assertNotNull( result );
        assertEquals( 7, result.length );
        for ( TypedObjectNode typedObjectNode : result ) {
            PrimitiveValue value = (PrimitiveValue) typedObjectNode;
            assertEquals( STRING, value.getType() );
            assertTrue( value.getValue().toString().startsWith( "PHILOSOPHER_" ) );
        }
    }
}