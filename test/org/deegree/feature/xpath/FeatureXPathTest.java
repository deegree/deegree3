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

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureReaderTest;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.junit.Assert;
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

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath1()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "*", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertNotNull( selectedNodes );
        Assert.assertEquals( 7, selectedNodes.size() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath2()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 7, selectedNodes.size() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath3()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 7, selectedNodes.size() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath4()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        FeatureNode featureNode = (FeatureNode) selectedNodes.get( 0 );
        Feature feature = featureNode.getFeature();
        Assert.assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath5()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher/app:name", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        PropertyNode propNode = (PropertyNode) selectedNodes.get( 0 );
        Property prop = propNode.getProperty();
        Assert.assertEquals( "Karl Marx", prop.getValue() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath6()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher/app:name/text()", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        TextNode textNode = (TextNode) selectedNodes.get( 0 );
        Assert.assertEquals( "Karl Marx", textNode.getValue() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath7()
                            throws JaxenException {
        XPath xpath = new FeatureXPath(
                                        "gml:featureMember/app:Philosopher[app:name='Albert Camus' and app:placeOfBirth/*/app:name='Mondovi']/app:placeOfBirth/app:Place/app:name",
                                        GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        PropertyNode propNode = (PropertyNode) selectedNodes.get( 0 );
        Property prop = propNode.getProperty();
        Assert.assertEquals( "Mondovi", prop.getValue() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath8()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher/app:placeOfBirth/app:Place",
                                        GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        FeatureNode featureNode = (FeatureNode) selectedNodes.get( 0 );
        Feature feature = featureNode.getFeature();
        Assert.assertEquals( "PLACE_2", feature.getId() );

        xpath = new FeatureXPath( "../..", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        selectedNodes = xpath.selectNodes( featureNode );
        Assert.assertEquals( 1, selectedNodes.size() );
        featureNode = (FeatureNode) selectedNodes.get( 0 );
        feature = featureNode.getFeature();
        Assert.assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath9()
                            throws JaxenException {

        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher[app:id < 3]/app:name", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );

        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Set<String> names = new HashSet<String>();
        for ( Node node : selectedNodes ) {
            names.add( (String) ( (PropertyNode) node ).getProperty().getValue() );
        }
        Assert.assertEquals( 2, names.size() );
        Assert.assertTrue( names.contains( "Friedrich Engels" ) );
        Assert.assertTrue( names.contains( "Karl Marx" ) );
    }

    // @SuppressWarnings("unchecked")
    // @Test
    // public void testXPath10()
    // throws JaxenException {
    // XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher/app:friend/app:Philosopher//app:name" );
    // xpath.setNamespaceContext( nsContext );
    // List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
    // for ( Node node : selectedNodes ) {
    // System.out.println( ( (PropertyNode) node ).getProperty().getValue() );
    // }
    // }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath11()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher[@gml:id='PHILOSOPHER_1']", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        FeatureNode featureNode = (FeatureNode) selectedNodes.get( 0 );
        Feature feature = featureNode.getFeature();
        Assert.assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath12()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher[gml:name='JEAN_PAUL']", GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        FeatureNode featureNode = (FeatureNode) selectedNodes.get( 0 );
        Feature feature = featureNode.getFeature();
        Assert.assertEquals( "PHILOSOPHER_6", feature.getId() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath13()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "/gml:FeatureCollection/gml:featureMember", fc, GMLVersion.GML_31 );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 7, selectedNodes.size() );
    }
}
