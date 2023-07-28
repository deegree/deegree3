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
import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DOUBLE;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.AppSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureReaderTest;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;

/**
 * Tests the correct evaluation of {@link GMLObjectXPath} expressions.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class GMLObjectXPathTest {

	private static final String BASE_DIR = "../../gml/misc/feature/";

	private FeatureCollection fc;

	private SimpleNamespaceContext nsContext;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		String schemaURL = this.getClass().getResource("../../gml/misc/schema/Philosopher.xsd").toString();
		URL docURL = GMLFeatureReaderTest.class.getResource(BASE_DIR + "Philosopher_FeatureCollection.xml");
		fc = parseFeatureCollection(GMLVersion.GML_31, schemaURL, docURL);

		nsContext = new SimpleNamespaceContext();
		nsContext.addNamespace("gml32", "http://www.opengis.net/gml/3.2");
		nsContext.addNamespace("gml", "http://www.opengis.net/gml");
		nsContext.addNamespace("app", "http://www.deegree.org/app");
		nsContext.addNamespace("xlink", "http://www.w3.org/1999/xlink");
		new DefaultWorkspace(new File("nix")).initAll();
	}

	@Test
	public void testXPath1() throws FilterEvaluationException {
		String xpath = "*";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(7, result.length);
		for (TypedObjectNode object : result) {
			assertTrue(object instanceof Property);
		}
	}

	@Test
	public void testXPath2() throws FilterEvaluationException {
		String xpath = "gml:featureMember";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(7, result.length);
		for (TypedObjectNode object : result) {
			assertTrue(object instanceof Property);
		}
	}

	@Test
	public void testXPath3() throws FilterEvaluationException {
		String xpath = "gml:featureMember/app:Philosopher";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(7, result.length);
		for (TypedObjectNode object : result) {
			assertTrue(object instanceof Feature);
		}
	}

	@Test
	public void testXPath4() throws FilterEvaluationException {
		String xpath = "gml:featureMember[1]/app:Philosopher";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertEquals(1, result.length);
		Feature feature = (Feature) result[0];
		assertEquals("PHILOSOPHER_1", feature.getId());
	}

	@Test
	public void testXPath5() throws FilterEvaluationException {
		String xpath = "gml:featureMember[1]/app:Philosopher/app:name";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertEquals(1, result.length);
		SimpleProperty prop = (SimpleProperty) result[0];
		PrimitiveValue name = prop.getValue();
		assertEquals(STRING, name.getType().getBaseType());
		assertEquals("Karl Marx", name.getAsText());
		assertTrue(name.getValue() instanceof String);
		// assertEquals( STRING, name.getXSType().getName() );
	}

	@Test
	public void testXPath6() throws FilterEvaluationException {
		String xpath = "gml:featureMember[1]/app:Philosopher/app:name/text()";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertEquals(1, result.length);
		PrimitiveValue name = (PrimitiveValue) result[0];
		assertEquals(STRING, name.getType().getBaseType());
		assertEquals("Karl Marx", name.getAsText());
		assertTrue(name.getValue() instanceof String);
		// assertEquals( STRING, name.getXSType().getName() );
	}

	@Test
	public void testXPath7() throws FilterEvaluationException {
		String xpath = "gml:featureMember/app:Philosopher[app:name='Albert Camus' and app:placeOfBirth/*/app:name='Mondovi']/app:placeOfBirth/app:Place/app:name";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertEquals(1, result.length);
		SimpleProperty prop = (SimpleProperty) result[0];
		PrimitiveValue name = prop.getValue();
		assertEquals(STRING, name.getType().getBaseType());
		assertEquals("Mondovi", name.getAsText());
		assertTrue(name.getValue() instanceof String);
	}

	@Test
	public void testXPath8() throws FilterEvaluationException {
		String xpath = "gml:featureMember[1]/app:Philosopher/app:placeOfBirth/app:Place/../..";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertEquals(1, result.length);
		Feature feature = (Feature) result[0];
		assertEquals("PHILOSOPHER_1", feature.getId());
	}

	@Test
	public void testXPath9() throws FilterEvaluationException {

		String xpath = "gml:featureMember/app:Philosopher[app:id < 3]/app:name";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		Set<String> names = new HashSet<String>();
		for (TypedObjectNode node : result) {
			names.add(((SimpleProperty) node).getValue().toString());
		}
		Assert.assertEquals(2, names.size());
		Assert.assertTrue(names.contains("Friedrich Engels"));
		Assert.assertTrue(names.contains("Karl Marx"));
	}

	// @Test
	// public void testXPath10()
	// throws FilterEvaluationException {
	// String xpath =
	// "gml:featureMember/app:Philosopher/app:friend/app:Philosopher//app:name";
	// TypedObjectNode[] result = fc.evalXPath( new PropertyName( xpath, nsContext ),
	// GML_31 );
	// for ( TypedObjectNode node : result ) {
	// System.out.println (node);
	// }
	// }

	@Test
	public void testXPath11() throws FilterEvaluationException {
		String xpath = "gml:featureMember/app:Philosopher[@gml:id='PHILOSOPHER_1']";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertEquals(1, result.length);
		Feature feature = (Feature) result[0];
		assertEquals("PHILOSOPHER_1", feature.getId());
	}

	@Test
	public void testXPath12() throws FilterEvaluationException {
		String xpath = "gml:featureMember/app:Philosopher[gml:name='JEAN_PAUL']";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertEquals(1, result.length);
		Feature feature = (Feature) result[0];
		assertEquals("PHILOSOPHER_6", feature.getId());
	}

	@Test
	public void testXPath13() throws FilterEvaluationException {
		String xpath = "/gml:FeatureCollection/gml:featureMember";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(7, result.length);
		for (TypedObjectNode object : result) {
			assertTrue(object instanceof Property);
		}
	}

	@Test
	public void testXPath14() throws FilterEvaluationException {
		String xpath = "true()";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(1, result.length);
		PrimitiveValue value = (PrimitiveValue) result[0];
		assertEquals(BOOLEAN, value.getType().getBaseType());
		assertEquals(TRUE, value.getValue());
	}

	@Test
	public void testXPath15() throws FilterEvaluationException {
		String xpath = "count(gml:featureMember/app:Philosopher)";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(1, result.length);
		PrimitiveValue value = (PrimitiveValue) result[0];
		assertEquals(DOUBLE, value.getType().getBaseType());
		assertEquals(new Double(7.0), value.getValue());
	}

	@Test
	public void testXPath16() throws FilterEvaluationException {
		String xpath = "string(gml:featureMember[1]/app:Philosopher[1]/app:name[1])";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(1, result.length);
		PrimitiveValue value = (PrimitiveValue) result[0];
		assertEquals(STRING, value.getType().getBaseType());
		assertEquals("Karl Marx", value.getValue());
	}

	@Test
	public void testXPath17() throws FilterEvaluationException {
		String xpath = "/";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(1, result.length);
		FeatureCollection fc2 = (FeatureCollection) result[0];
		assertTrue(fc == fc2);
	}

	@Test
	public void testXPath18() throws FilterEvaluationException {
		String xpath = "gml:featureMember/app:Philosopher/@gml:id";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(7, result.length);
		for (TypedObjectNode typedObjectNode : result) {
			PrimitiveValue value = (PrimitiveValue) typedObjectNode;
			assertEquals(STRING, value.getType().getBaseType());
			assertTrue(value.getValue().toString().startsWith("PHILOSOPHER_"));
		}
	}

	@Test
	public void testXPath19() throws FilterEvaluationException {
		String xpath = "local-name(gml:featureMember/*)";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals("Philosopher", ((PrimitiveValue) result[0]).getAsText());
	}

	@Test
	public void testXPath20() throws FilterEvaluationException {
		String xpath = "valueOf(gml:featureMember)/@gml:id";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(7, result.length);
		assertEquals("PHILOSOPHER_1", ((PrimitiveValue) result[0]).getAsText());
	}

	@Test
	public void testXPath21() throws FilterEvaluationException {
		String xpath = "valueOf(valueOf(gml:featureMember)/app:placeOfBirth)/@gml:id";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(7, result.length);
		assertEquals("PLACE_2", ((PrimitiveValue) result[0]).getAsText());
	}

	@Test
	public void testXPath22() throws FilterEvaluationException {
		String xpath = "valueOf(valueOf(valueOf(gml:featureMember)/app:placeOfBirth)/app:country)/@gml:id";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(6, result.length);
		assertEquals("COUNTRY_2", ((PrimitiveValue) result[0]).getAsText());
	}

	@Test
	public void testXPath23() throws FilterEvaluationException {
		String xpath = "gml:featureMember/app:Philosopher[@gml:id='PHILOSOPHER_1']/app:placeOfBirth/app:Place/app:country/app:Country/app:geom/gml:MultiPolygon/gml:name";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(3, result.length);
	}

	@Test
	public void testXPath24() throws FilterEvaluationException {
		String xpath = "gml:featureMember/app:Philosopher[@gml:id='PHILOSOPHER_1']/app:placeOfBirth/app:Place/app:country/app:Country/app:geom/gml:MultiPolygon/gml:name[1]/text()";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals("POLYGON_1", ((PrimitiveValue) result[0]).getAsText());
	}

	// @Test
	// public void testXPath23()
	// throws FilterEvaluationException {
	// String xpath =
	// "valueOf(gml:featureMember)/valueOf(app:placeOfBirth)/valueOf(app:country)/@gml:id";
	// TypedObjectNode[] result = new FeatureXPathEvaluator( GML_31 ).eval( fc, new
	// ValueReference( xpath, nsContext )
	// );
	// assertNotNull( result );
	// assertEquals( 6, result.length );
	// assertEquals( "COUNTRY_2", ( (PrimitiveValue) result[0] ).getAsText() );
	// }

	@Test
	public void testXPathXlinkHref() throws Exception {
		String schemaURL = this.getClass().getResource("../../gml/misc/schema/CustomPropertiesGml32.xsd").toString();
		URL docURL = GMLFeatureReaderTest.class.getResource(BASE_DIR + "CustomPropertiesGml32.xml");
		FeatureCollection fc = parseFeatureCollection(GMLVersion.GML_32, schemaURL, docURL);

		String xpath = "gml32:featureMember/app:ComplexFeature/app:type/@xlink:href";
		TypedObjectNode[] result = new TypedObjectNodeXPathEvaluator().eval(fc, new ValueReference(xpath, nsContext));
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals("http://pathtoregistry/abc/code", ((PrimitiveValue) result[0]).getAsText());
	}

	private FeatureCollection parseFeatureCollection(GMLVersion gmlVersion, String schemaURL, URL docURL)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, XMLStreamException,
			IOException, UnknownCRSException {

		GMLAppSchemaReader xsdAdapter = new GMLAppSchemaReader(gmlVersion, null, schemaURL);
		AppSchema schema = xsdAdapter.extractAppSchema();

		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(gmlVersion, docURL);
		gmlReader.setApplicationSchema(schema);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		return fc;
	}

}
