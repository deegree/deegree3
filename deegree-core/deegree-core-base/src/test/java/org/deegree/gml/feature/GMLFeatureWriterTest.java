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

package org.deegree.gml.feature;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.projection.TimeSliceProjection;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.matchers.HasXPathMatcher;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.filter.MatchAction.ALL;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.xmlunit.diff.ComparisonResult.DIFFERENT;
import static org.xmlunit.diff.ComparisonResult.EQUAL;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

/**
 * Exports the features in the Philosophers example and validates them against the
 * corresponding schema.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GMLFeatureWriterTest {

	private final String SOURCE_FILE_31 = "../misc/feature/Philosopher_FeatureCollection.xml";

	private final String SOURCE_FILE_32 = "../misc/feature/Philosopher_FeatureCollection_Gml32.xml";

	private final String SCHEMA_LOCATION_ATTRIBUTE_31 = "../misc/schema/Philosopher.xsd";

	private final String SCHEMA_LOCATION_31 = "http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/feature.xsd http://www.deegree.org/app testdata/schema/Philosopher.xsd";

	@Test
	public void testWriteGML2() throws Exception {
		String schemaURL = this.getClass().getResource(SCHEMA_LOCATION_ATTRIBUTE_31).toString();
		GMLAppSchemaReader xsdAdapter = new GMLAppSchemaReader(GML_31, null, schemaURL);
		AppSchema schema = xsdAdapter.extractAppSchema();

		URL docURL = GMLFeatureWriterTest.class.getResource(SOURCE_FILE_31);
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		gmlReader.setApplicationSchema(schema);
		Feature feature = gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_31);
		writer.setDefaultNamespace("http://www.opengis.net/gml");
		writer.setPrefix("app", "http://www.deegree.org/app");
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("ogc", "http://www.opengis.net/ogc");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		GMLStreamWriter exporter = createGMLStreamWriter(GML_2, new IndentingXMLStreamWriter(writer));
		exporter.write(feature);
		writer.flush();
		writer.close();

		String actual = memoryWriter.toString();
		assertThat(actual,
				isSimilarTo(expectedXml("expectedExport-gml2.xml")).ignoreWhitespace()
					.ignoreElementContentWhitespace()
					.withDifferenceEvaluator(inoreLineBreaksInElementContent()));
	}

	@Test
	public void testWriteGML31() throws Exception {
		String schemaURL = this.getClass().getResource(SCHEMA_LOCATION_ATTRIBUTE_31).toString();
		GMLAppSchemaReader xsdAdapter = new GMLAppSchemaReader(GML_31, null, schemaURL);
		AppSchema schema = xsdAdapter.extractAppSchema();

		URL docURL = GMLFeatureWriterTest.class.getResource(SOURCE_FILE_31);
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		gmlReader.setApplicationSchema(schema);
		Feature feature = gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter(memoryWriter.getXMLStreamWriter(),
				SCHEMA_LOCATION_31);
		writer.setDefaultNamespace("http://www.opengis.net/gml");
		writer.setPrefix("app", "http://www.deegree.org/app");
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("ogc", "http://www.opengis.net/ogc");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		GMLStreamWriter exporter = createGMLStreamWriter(GML_31, writer);
		exporter.write(feature);
		writer.flush();
		writer.close();

		String actual = memoryWriter.toString();
		assertThat(actual,
				isSimilarTo(expectedXml("expectedExport-gml31.xml")).ignoreWhitespace()
					.ignoreElementContentWhitespace()
					.withDifferenceEvaluator(inoreLineBreaksInElementContent()));
	}

	@Test
	public void testWriteGML32() throws Exception {
		URL docURL = GMLFeatureWriterTest.class.getResource(SOURCE_FILE_32);
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		Feature feature = gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter writer = memoryWriter.getXMLStreamWriter();

		writer.setDefaultNamespace("http://www.opengis.net/gml");
		writer.setPrefix("app", "http://www.deegree.org/app");
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("ogc", "http://www.opengis.net/ogc");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		GMLStreamWriter exporter = createGMLStreamWriter(GML_32, writer);
		exporter.write(feature);
		writer.flush();
		writer.close();

		String actual = memoryWriter.toString();
		assertThat(actual,
				isSimilarTo(expectedXml("expectedExport-gml32.xml")).ignoreWhitespace()
					.ignoreElementContentWhitespace()
					.withDifferenceEvaluator(inoreLineBreaksInElementContent()));
	}

	// @Test
	// public void testFI()
	// throws XMLStreamException, ClassCastException, ClassNotFoundException,
	// InstantiationException, IllegalAccessException, XMLParsingException,
	// UnknownCRSException,
	// FactoryConfigurationError, IOException, TransformationException {
	//
	// String schemaURL = this.getClass().getResource( SCHEMA_LOCATION_ATTRIBUTE
	// ).toString();
	// ApplicationSchemaXSDDecoder xsdAdapter = new ApplicationSchemaXSDDecoder( GML_31,
	// null, schemaURL );
	// ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();
	//
	// URL docURL = GMLFeatureWriterTest.class.getResource( DIR + SOURCE_FILE );
	// GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL
	// );
	// gmlReader.setApplicationSchema( schema );
	// Feature feature = gmlReader.readFeature();
	// gmlReader.getIdContext().resolveLocalRefs();
	//
	// OutputStream fiDocument = new FileOutputStream( "/tmp/out.fi" );
	//
	// // Create the StAX document serializer
	// StAXDocumentSerializer staxDocumentSerializer = new StAXDocumentSerializer();
	// staxDocumentSerializer.setOutputStream( fiDocument );
	//
	// SerializerVocabulary initialVocabulary = new SerializerVocabulary();
	// initialVocabulary.setExternalVocabulary( "urn:external-vocabulary",
	// BinaryVocabulary.serializerVoc, false );
	// staxDocumentSerializer.setVocabulary( initialVocabulary );
	//
	// // Obtain XMLStreamWriter interface
	// XMLStreamWriter writer = staxDocumentSerializer;
	// writer.writeStartDocument();
	//
	// // writer.setDefaultNamespace( "http://www.opengis.net/gml" );
	// writer.setPrefix( "app1", "http://www.deegree.org/app" );
	// writer.setPrefix( "gml", "http://www.opengis.net/gml" );
	// writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
	// writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
	// writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
	// writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
	//
	// GMLFeatureWriter exporter = new GMLFeatureWriter( GML_31, writer, null, null, null,
	// null, 0, -1, null, false);
	// exporter.export( feature );
	//
	// writer.writeEndDocument();
	//
	// writer.close();
	// }
	@Test
	public void testReexportDynamicallyParsedFeatureCollection() throws Exception {
		URL url = GMLFeatureWriterTest.class.getResource("../misc/feature/test.gml");
		GMLStreamReader reader = GMLInputFactory.createGMLStreamReader(GML_2, url);
		FeatureCollection fc = reader.readFeatureCollection();
		XMLOutputFactory outfac = XMLOutputFactory.newInstance();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter writer = outfac.createXMLStreamWriter(os);
		GMLStreamWriter gmlwriter = GMLOutputFactory.createGMLStreamWriter(GMLVersion.GML_32, writer);
		gmlwriter.setNamespaceBindings(reader.getAppSchema().getNamespaceBindings());
		gmlwriter.write(fc);
		gmlwriter.close();

		String actual = os.toString();
		assertThat(actual, isSimilarTo(expectedXml("expectedExport-reexport.xml")).ignoreWhitespace());
	}

	@Test
	public void testExportWithoutBoundedBy() throws Exception {
		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf0.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		for (Feature f : fc) {
			f.setEnvelope(null);
		}

		XMLOutputFactory outfac = XMLOutputFactory.newInstance();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter writer = outfac.createXMLStreamWriter(os);
		GMLStreamWriter gmlwriter = createGMLStreamWriter(GML_31, writer);
		gmlwriter.setNamespaceBindings(gmlReader.getAppSchema().getNamespaceBindings());
		gmlwriter.write(fc);
		gmlwriter.close();

		String actual = os.toString();
		assertThat(actual,
				hasXPath("count(gml31:featureMember/*/gml31:boundedBy)", is("0")).withNamespaceContext(nsContext()));
		assertThat(actual,
				isSimilarTo(expectedXml("expectedExport-withoutBoundedBy.xml")).ignoreWhitespace()
					.ignoreElementContentWhitespace()
					.withDifferenceEvaluator(inoreLineBreaksInElementContent()));
	}

	@Test
	public void testExportWithBoundedBy() throws Exception {
		URL docURL = GMLFeatureReaderTest.class.getResource("../cite/feature/dataset-sf0.xml");
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, docURL);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		for (Feature f : fc) {
			f.setEnvelope(null);
		}

		XMLOutputFactory outfac = XMLOutputFactory.newInstance();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter writer = outfac.createXMLStreamWriter(os);
		GMLStreamWriter gmlwriter = createGMLStreamWriter(GML_31, writer);
		gmlwriter.setNamespaceBindings(gmlReader.getAppSchema().getNamespaceBindings());
		gmlwriter.setGenerateBoundedByForFeatures(true);
		gmlwriter.write(fc);
		gmlwriter.close();

		String actual = os.toString();
		assertThat(actual,
				hasXPath("count(//gml31:featureMember/*/gml31:boundedBy)", is("15")).withNamespaceContext(nsContext()));
		assertThat(actual,
				isSimilarTo(expectedXml("expectedExport-withBoundedBy.xml")).ignoreWhitespace()
					.ignoreElementContentWhitespace()
					.withDifferenceEvaluator(inoreLineBreaksInElementContent()));
	}

	@Test
	public void testAIXM51RouteSegmentWithUrnXlink() throws Exception {
		URL docURL = GMLFeatureReaderTest.class.getResource("../aixm/feature/AIXM51_RouteSegment.gml");
		GMLStreamReader gmlReader = createGMLStreamReader(GML_32, docURL);
		Feature f = gmlReader.readFeature();

		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter writer = new IndentingXMLStreamWriter(memoryWriter.getXMLStreamWriter());
		GMLStreamWriter gmlwriter = createGMLStreamWriter(GML_32, writer);
		gmlwriter.setNamespaceBindings(gmlReader.getAppSchema().getNamespaceBindings());
		gmlwriter.write(f);
		gmlwriter.close();

		String actual = memoryWriter.toString();
		assertThat(actual,
				isSimilarTo(expectedXml("expectedExport-testAIXM51RouteSegmentWithUrnXlink.xml")).ignoreWhitespace());
	}

	@Test
	public void testAIXM51RouteSegmentTimeSliceProjection1() throws Exception {
		URL docURL = GMLFeatureReaderTest.class.getResource("../aixm/feature/AIXM51_RouteSegment.gml");
		GMLStreamReader gmlReader = createGMLStreamReader(GML_32, docURL);
		Feature f = gmlReader.readFeature();

		NamespaceBindings nsBindings = new NamespaceBindings();
		nsBindings.addNamespace("gml", GML3_2_NS);
		ValueReference validTimeRef = new ValueReference("gml:validTime/gml:TimePeriod/gml:beginPosition", nsBindings);
		Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>("2010-01-01T00:00:00.000");
		PropertyIsEqualTo comp = new PropertyIsEqualTo(validTimeRef, literal, false, ALL);
		Filter timeSliceFilter = new OperatorFilter(comp);

		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter writer = new IndentingXMLStreamWriter(memoryWriter.getXMLStreamWriter());
		GMLStreamWriter gmlwriter = createGMLStreamWriter(GML_32, writer);
		gmlwriter.setNamespaceBindings(gmlReader.getAppSchema().getNamespaceBindings());
		Map<QName, List<ProjectionClause>> projections = new HashMap<>();
		projections.put(f.getName(), Collections.singletonList(new TimeSliceProjection(timeSliceFilter)));
		gmlwriter.setProjections(projections);
		gmlwriter.write(f);
		gmlwriter.close();

		String actual = memoryWriter.toString();
		assertThat(actual, HasXPathMatcher.hasXPath("//aixm:RouteSegmentTimeSlice[@gml32:id = 'rsts207']")
			.withNamespaceContext(nsContext()));
		assertThat(actual, not(HasXPathMatcher.hasXPath("//aixm:RouteSegmentTimeSlice[@gml32:id = 'rsts206']")
			.withNamespaceContext(nsContext())));
		assertThat(actual, isSimilarTo(expectedXml("expectedExport-AIXM51RouteSegmentTimeSliceProjection1.xml"))
			.ignoreWhitespace());
	}

	@Test
	public void testAIXM51RouteSegmentTimeSliceProjection2() throws Exception {
		URL docURL = GMLFeatureReaderTest.class.getResource("../aixm/feature/AIXM51_RouteSegment.gml");
		GMLStreamReader gmlReader = createGMLStreamReader(GML_32, docURL);
		Feature f = gmlReader.readFeature();

		NamespaceBindings nsBindings = new NamespaceBindings();
		nsBindings.addNamespace("gml", GML3_2_NS);
		ValueReference validTimeRef = new ValueReference("gml:validTime/gml:TimePeriod/gml:beginPosition", nsBindings);
		Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>("2009-01-01T00:00:00.000");
		PropertyIsEqualTo comp = new PropertyIsEqualTo(validTimeRef, literal, false, ALL);
		Filter timeSliceFilter = new OperatorFilter(comp);

		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter writer = new IndentingXMLStreamWriter(memoryWriter.getXMLStreamWriter());
		GMLStreamWriter gmlwriter = createGMLStreamWriter(GML_32, writer);
		gmlwriter.setNamespaceBindings(gmlReader.getAppSchema().getNamespaceBindings());
		Map<QName, List<ProjectionClause>> projections = new HashMap<>();
		projections.put(f.getName(), Collections.singletonList(new TimeSliceProjection(timeSliceFilter)));
		gmlwriter.setProjections(projections);
		gmlwriter.write(f);
		gmlwriter.close();

		String actual = memoryWriter.toString();
		assertThat(actual, HasXPathMatcher.hasXPath("//aixm:RouteSegmentTimeSlice[@gml32:id = 'rsts206']")
			.withNamespaceContext(nsContext()));
		assertThat(actual, not(HasXPathMatcher.hasXPath("//aixm:RouteSegmentTimeSlice[@gml32:id = 'rsts207']")
			.withNamespaceContext(nsContext())));
		assertThat(actual, isSimilarTo(expectedXml("expectedExport-AIXM51RouteSegmentTimeSliceProjection2.xml"))
			.ignoreWhitespace());
	}

	@Test
	public void testDecimalPropertyEncodedFaithfully() throws Exception {
		final String formattedInputValue = "0.00000009";
		final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		final XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		final XMLStreamWriter writer = memoryWriter.getXMLStreamWriter();
		final GMLStreamWriter exporter = createGMLStreamWriter(GML_2, writer);
		final GMLFeatureWriter featureWriter = exporter.getFeatureWriter();
		final PropertyType decimalPt = new SimplePropertyType(new QName("property"), 1, 1, DECIMAL, null, null);
		final PrimitiveType pt = new PrimitiveType(DECIMAL);
		final TypedObjectNode value = new PrimitiveValue(formattedInputValue, pt);
		final Property prop = new GenericProperty(decimalPt, value);
		featureWriter.export(prop);
		writer.flush();
		writer.close();

		String actual = memoryWriter.toString();
		String expectedXml = "<property>0.00000009</property>";
		assertThat(actual, isSimilarTo(expectedXml).ignoreWhitespace());
	}

	@Test
	public void testProjections_QName() throws Exception {
		URL docURL = GMLFeatureWriterTest.class.getResource(SOURCE_FILE_32);
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		Feature feature = gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter writer = memoryWriter.getXMLStreamWriter();

		writer.setDefaultNamespace("http://www.opengis.net/gml");
		writer.setPrefix("app", "http://www.deegree.org/app");
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("ogc", "http://www.opengis.net/ogc");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		GMLStreamWriter exporter = createGMLStreamWriter(GML_32, writer);

		Map<QName, List<ProjectionClause>> projections = new HashMap<>();
		QName ftName = new QName("http://www.deegree.org/app", "Philosopher", "app");
		List<ProjectionClause> ftProjections = new ArrayList<>();
		ftProjections.add(createPropertyName("id"));
		ftProjections.add(createPropertyName("name"));
		ftProjections.add(createPropertyName("isAuthorOf"));
		projections.put(ftName, ftProjections);
		exporter.setProjections(projections);

		exporter.write(feature);
		writer.flush();
		writer.close();

		String actual = memoryWriter.toString();
		assertThat(actual, isSimilarTo(expectedXml("expectedExport-projectionQName.xml")).ignoreWhitespace());
	}

	@Test
	public void testProjections_XPath() throws Exception {
		URL docURL = GMLFeatureWriterTest.class.getResource(SOURCE_FILE_32);
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		Feature feature = gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter writer = memoryWriter.getXMLStreamWriter();

		writer.setDefaultNamespace("http://www.opengis.net/gml");
		writer.setPrefix("app", "http://www.deegree.org/app");
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("ogc", "http://www.opengis.net/ogc");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		GMLStreamWriter exporter = createGMLStreamWriter(GML_32, writer);

		Map<QName, List<ProjectionClause>> projections = new HashMap<>();
		QName ftName = new QName("http://www.deegree.org/app", "Philosopher", "app");
		List<ProjectionClause> ftProjections = new ArrayList<>();
		ftProjections.add(createPropertyName("id"));
		ftProjections.add(createPropertyNameWithXPath("app:placeOfBirth/app:Place/app:name"));
		ftProjections.add(createPropertyNameWithXPath("app:placeOfBirth/app:Place/app:country/app:Country/app:name"));
		projections.put(ftName, ftProjections);
		exporter.setProjections(projections);

		exporter.write(feature);
		writer.flush();
		writer.close();

		String actual = memoryWriter.toString();
		assertThat(actual, isSimilarTo(expectedXml("expectedExport-projectionXPath.xml")).ignoreWhitespace());
	}

	@Test
	public void testProjections_XPath_SelectPlace() throws Exception {
		URL docURL = GMLFeatureWriterTest.class.getResource(SOURCE_FILE_32);
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_32, docURL);
		Feature feature = gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", new Boolean(true));
		XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
		XMLStreamWriter writer = memoryWriter.getXMLStreamWriter();

		writer.setDefaultNamespace("http://www.opengis.net/gml");
		writer.setPrefix("app", "http://www.deegree.org/app");
		writer.setPrefix("gml", "http://www.opengis.net/gml");
		writer.setPrefix("ogc", "http://www.opengis.net/ogc");
		writer.setPrefix("wfs", "http://www.opengis.net/wfs");
		writer.setPrefix("xlink", "http://www.w3.org/1999/xlink");
		writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		GMLStreamWriter exporter = createGMLStreamWriter(GML_32, writer);

		Map<QName, List<ProjectionClause>> projections = new HashMap<>();
		QName ftName = new QName("http://www.deegree.org/app", "Philosopher", "app");
		List<ProjectionClause> ftProjections = new ArrayList<>();
		ftProjections.add(createPropertyName("id"));
		ftProjections.add(createPropertyNameWithXPath("app:placeOfBirth/app:Place"));
		projections.put(ftName, ftProjections);
		exporter.setProjections(projections);

		exporter.write(feature);
		writer.flush();
		writer.close();

		String xml = memoryWriter.toString();
		assertThat(xml,
				isSimilarTo(expectedXml("expectedExport-projectionXPath_Place.xml")).ignoreWhitespace()
					.ignoreElementContentWhitespace()
					.withDifferenceEvaluator(inoreLineBreaksInElementContent()));
	}

	private PropertyName createPropertyName(String propertyName) {
		QName propName = new QName("http://www.deegree.org/app", propertyName, "app");
		ValueReference valueRef = new ValueReference(propName);
		ResolveParams resolveParams = new ResolveParams(ResolveMode.ALL, "*", BigInteger.valueOf(1000));
		return new PropertyName(valueRef, resolveParams, null);
	}

	private PropertyName createPropertyNameWithXPath(String xPath) {
		org.jaxen.SimpleNamespaceContext namespaceContext = new org.jaxen.SimpleNamespaceContext();
		namespaceContext.addNamespace("app", "http://www.deegree.org/app");
		ValueReference valueRef = new ValueReference(xPath, namespaceContext);
		ResolveParams resolveParams = new ResolveParams(ResolveMode.ALL, "*", BigInteger.valueOf(1000));
		return new PropertyName(valueRef, resolveParams, null);
	}

	private Map<String, String> nsContext() {
		Map<String, String> nsContext = new HashMap<>();
		nsContext.put("gml31", GML_31.getNamespace());
		nsContext.put("gml32", GML_32.getNamespace());
		nsContext.put("aixm", "http://www.aixm.aero/schema/5.1");
		return nsContext;
	}

	private String expectedXml(String resource) throws IOException {
		return IOUtils.toString(getClass().getResourceAsStream(resource), UTF_8.name());
	}

	private DifferenceEvaluator inoreLineBreaksInElementContent() {
		return (comparison, comparisonResult) -> {
			if (comparisonResult.equals(DIFFERENT)) {
				Object testValue = comparison.getTestDetails().getValue();
				Object controlValue = comparison.getControlDetails().getValue();
				if (testValue instanceof String && controlValue instanceof String && testValue != null
						&& testValue != null) {
					String control = ((String) controlValue).replaceAll("\n", "").replace(" ", "");
					String test = ((String) testValue).replaceAll("\n", "").replace(" ", "");
					return control.equals(test) ? EQUAL : DIFFERENT;
				}
				return comparisonResult;
			}
			return comparisonResult;
		};
	}

}