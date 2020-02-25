//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.filter.MatchAction.ALL;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
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
import org.deegree.filter.projection.TimeSliceProjection;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Test;

/**
 * Exports the features in the Philosophers example and validates them against the corresponding schema.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 *
 * @version $Revision: $, $Date: $
 */
public class GMLFeatureWriterTest {

    private final String SOURCE_FILE = "../misc/feature/Philosopher_FeatureCollection.xml";

    private final String SCHEMA_LOCATION_ATTRIBUTE = "../misc/schema/Philosopher.xsd";

    private final String SCHEMA_LOCATION = "http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/feature.xsd http://www.deegree.org/app testdata/schema/Philosopher.xsd";

    @Test
    public void testWriteGML2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, TransformationException {
        String schemaURL = this.getClass().getResource( SCHEMA_LOCATION_ATTRIBUTE ).toString();
        GMLAppSchemaReader xsdAdapter = new GMLAppSchemaReader( GML_31, null, schemaURL );
        AppSchema schema = xsdAdapter.extractAppSchema();

        URL docURL = GMLFeatureWriterTest.class.getResource( SOURCE_FILE );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        Feature feature = gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter( memoryWriter.getXMLStreamWriter(),
                                                                                  SCHEMA_LOCATION );
        writer.setDefaultNamespace( "http://www.opengis.net/gml" );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GMLStreamWriter exporter = createGMLStreamWriter( GML_2, new IndentingXMLStreamWriter( writer ) );
        exporter.write( feature );
        writer.flush();
        writer.close();
        // XMLAssert.assertValidity( memoryWriter.getReader() );
    }

    @Test
    public void testWriteGML31()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, TransformationException {
        String schemaURL = this.getClass().getResource( SCHEMA_LOCATION_ATTRIBUTE ).toString();
        GMLAppSchemaReader xsdAdapter = new GMLAppSchemaReader( GML_31, null, schemaURL );
        AppSchema schema = xsdAdapter.extractAppSchema();

        URL docURL = GMLFeatureWriterTest.class.getResource( SOURCE_FILE );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        Feature feature = gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        SchemaLocationXMLStreamWriter writer = new SchemaLocationXMLStreamWriter( memoryWriter.getXMLStreamWriter(),
                                                                                  SCHEMA_LOCATION );
        writer.setDefaultNamespace( "http://www.opengis.net/gml" );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GMLStreamWriter exporter = createGMLStreamWriter( GML_31, writer );
        exporter.write( feature );
        writer.flush();
        writer.close();
        // XMLAssert.assertValidity( memoryWriter.getReader() );
        // System.out.println (memoryWriter.toString());
    }

    // @Test
    // public void testFI()
    // throws XMLStreamException, ClassCastException, ClassNotFoundException,
    // InstantiationException, IllegalAccessException, XMLParsingException, UnknownCRSException,
    // FactoryConfigurationError, IOException, TransformationException {
    //
    // String schemaURL = this.getClass().getResource( SCHEMA_LOCATION_ATTRIBUTE ).toString();
    // ApplicationSchemaXSDDecoder xsdAdapter = new ApplicationSchemaXSDDecoder( GML_31, null, schemaURL );
    // ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();
    //
    // URL docURL = GMLFeatureWriterTest.class.getResource( DIR + SOURCE_FILE );
    // GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL );
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
    // initialVocabulary.setExternalVocabulary( "urn:external-vocabulary", BinaryVocabulary.serializerVoc, false );
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
    // GMLFeatureWriter exporter = new GMLFeatureWriter( GML_31, writer, null, null, null, null, 0, -1, null, false);
    // exporter.export( feature );
    //
    // writer.writeEndDocument();
    //
    // writer.close();
    // }

    @Test
    public void testReexportDynamicallyParsedFeatureCollection()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            TransformationException, FactoryConfigurationError, IOException {

        URL url = GMLFeatureWriterTest.class.getResource( "../misc/feature/test.gml" );
        GMLStreamReader reader = GMLInputFactory.createGMLStreamReader( GML_2, url );
        FeatureCollection fc = reader.readFeatureCollection();
        XMLOutputFactory outfac = XMLOutputFactory.newInstance();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter writer = outfac.createXMLStreamWriter( os );
        GMLStreamWriter gmlwriter = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_32, writer );
        gmlwriter.setNamespaceBindings( reader.getAppSchema().getNamespaceBindings() );
        gmlwriter.write( fc );
        gmlwriter.close();
    }

    @Test
    public void testExportWithoutBoundedBy()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            TransformationException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        URL docURL = GMLFeatureReaderTest.class.getResource( "../cite/feature/dataset-sf0.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();
        for ( Feature f : fc ) {
            f.setEnvelope( null );
        }

        XMLOutputFactory outfac = XMLOutputFactory.newInstance();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter writer = outfac.createXMLStreamWriter( os );
        GMLStreamWriter gmlwriter = createGMLStreamWriter( GML_31, writer );
        gmlwriter.setNamespaceBindings( gmlReader.getAppSchema().getNamespaceBindings() );
        gmlwriter.write( fc );
        gmlwriter.close();

        XMLAdapter writtenDoc = new XMLAdapter( new ByteArrayInputStream( os.toByteArray() ), null );
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "gml", GML_31.getNamespace() );
        XPath xpath = new XPath( "gml:featureMember/*/gml:boundedBy", nsContext );
        List<OMElement> boundedBys = writtenDoc.getElements( writtenDoc.getRootElement(), xpath );
        assertEquals( 0, boundedBys.size() );
    }

    @Test
    public void testExportWithBoundedBy()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            TransformationException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        URL docURL = GMLFeatureReaderTest.class.getResource( "../cite/feature/dataset-sf0.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();
        for ( Feature f : fc ) {
            f.setEnvelope( null );
        }

        XMLOutputFactory outfac = XMLOutputFactory.newInstance();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter writer = outfac.createXMLStreamWriter( os );
        GMLStreamWriter gmlwriter = createGMLStreamWriter( GML_31, writer );
        gmlwriter.setNamespaceBindings( gmlReader.getAppSchema().getNamespaceBindings() );
        gmlwriter.setGenerateBoundedByForFeatures( true );
        gmlwriter.write( fc );
        gmlwriter.close();

        XMLAdapter writtenDoc = new XMLAdapter( new ByteArrayInputStream( os.toByteArray() ), null );
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "gml", GML_31.getNamespace() );
        XPath xpath = new XPath( "gml:featureMember/*/gml:boundedBy", nsContext );
        List<OMElement> boundedBys = writtenDoc.getElements( writtenDoc.getRootElement(), xpath );
        assertEquals( 15, boundedBys.size() );
    }

    @Test
    public void testAIXM51RouteSegmentWithUrnXlink()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            XMLParsingException, UnknownCRSException, ReferenceResolvingException,
                            TransformationException {

        URL docURL = GMLFeatureReaderTest.class.getResource( "../aixm/feature/AIXM51_RouteSegment.gml" );
        GMLStreamReader gmlReader = createGMLStreamReader( GML_32, docURL );
        Feature f = gmlReader.readFeature();

        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriter writer = new IndentingXMLStreamWriter( memoryWriter.getXMLStreamWriter() );
        GMLStreamWriter gmlwriter = createGMLStreamWriter( GML_32, writer );
        gmlwriter.setNamespaceBindings( gmlReader.getAppSchema().getNamespaceBindings() );
        gmlwriter.write( f );
        gmlwriter.close();

        URL schemaUrl = GMLFeatureReaderTest.class.getResource( "../aixm/schema/AIXM_Features.xsd" );
        XMLAssert.assertValidity( memoryWriter.getReader(), schemaUrl.toString() );
    }

    @Test
    public void testAIXM51RouteSegmentTimeSliceProjection1()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            XMLParsingException, UnknownCRSException, ReferenceResolvingException,
                            TransformationException {

        URL docURL = GMLFeatureReaderTest.class.getResource( "../aixm/feature/AIXM51_RouteSegment.gml" );
        GMLStreamReader gmlReader = createGMLStreamReader( GML_32, docURL );
        Feature f = gmlReader.readFeature();

        NamespaceBindings nsBindings = new NamespaceBindings();
        nsBindings.addNamespace( "gml", GML3_2_NS );
        ValueReference validTimeRef = new ValueReference( "gml:validTime/gml:TimePeriod/gml:beginPosition", nsBindings );
        Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>( "2010-01-01T00:00:00.000" );
        PropertyIsEqualTo comp = new PropertyIsEqualTo( validTimeRef, literal, false, ALL );
        Filter timeSliceFilter = new OperatorFilter( comp );

        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriter writer = new IndentingXMLStreamWriter( memoryWriter.getXMLStreamWriter() );
        GMLStreamWriter gmlwriter = createGMLStreamWriter( GML_32, writer );
        gmlwriter.setNamespaceBindings( gmlReader.getAppSchema().getNamespaceBindings() );
        Map<QName, List<ProjectionClause>> projections = new HashMap<>();
        projections.put( f.getName(), Collections.singletonList( new TimeSliceProjection( timeSliceFilter ) ) );
        gmlwriter.setProjections( projections );
        gmlwriter.write( f );
        gmlwriter.close();

        URL schemaUrl = GMLFeatureReaderTest.class.getResource( "../aixm/schema/AIXM_Features.xsd" );
        XMLAssert.assertValidity( memoryWriter.getReader(), schemaUrl.toString() );

        assertFalse( memoryWriter.toString().contains( "rsts206" ) );
        assertTrue( memoryWriter.toString().contains( "rsts207" ) );
    }

    @Test
    public void testAIXM51RouteSegmentTimeSliceProjection2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            XMLParsingException, UnknownCRSException, ReferenceResolvingException,
                            TransformationException {

        URL docURL = GMLFeatureReaderTest.class.getResource( "../aixm/feature/AIXM51_RouteSegment.gml" );
        GMLStreamReader gmlReader = createGMLStreamReader( GML_32, docURL );
        Feature f = gmlReader.readFeature();

        NamespaceBindings nsBindings = new NamespaceBindings();
        nsBindings.addNamespace( "gml", GML3_2_NS );
        ValueReference validTimeRef = new ValueReference( "gml:validTime/gml:TimePeriod/gml:beginPosition", nsBindings );
        Literal<PrimitiveValue> literal = new Literal<PrimitiveValue>( "2009-01-01T00:00:00.000" );
        PropertyIsEqualTo comp = new PropertyIsEqualTo( validTimeRef, literal, false, ALL );
        Filter timeSliceFilter = new OperatorFilter( comp );

        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriter writer = new IndentingXMLStreamWriter( memoryWriter.getXMLStreamWriter() );
        GMLStreamWriter gmlwriter = createGMLStreamWriter( GML_32, writer );
        gmlwriter.setNamespaceBindings( gmlReader.getAppSchema().getNamespaceBindings() );
        Map<QName, List<ProjectionClause>> projections = new HashMap<>();
        projections.put( f.getName(), Collections.singletonList( new TimeSliceProjection( timeSliceFilter ) ) );
        gmlwriter.setProjections( projections );
        gmlwriter.write( f );
        gmlwriter.close();

        URL schemaUrl = GMLFeatureReaderTest.class.getResource( "../aixm/schema/AIXM_Features.xsd" );
        XMLAssert.assertValidity( memoryWriter.getReader(), schemaUrl.toString() );

        assertTrue( memoryWriter.toString().contains( "rsts206" ) );
        assertFalse( memoryWriter.toString().contains( "rsts207" ) );
    }
    
    @Test
    public void testDecimalPropertyEncodedFaithfully()
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        final String formattedInputValue = "0.00000009";
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        final XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        final XMLStreamWriter writer = memoryWriter.getXMLStreamWriter();
        final GMLStreamWriter exporter = createGMLStreamWriter( GML_2, writer );
        final GMLFeatureWriter featureWriter = exporter.getFeatureWriter();
        final PropertyType decimalPt = new SimplePropertyType( new QName( "property" ), 1, 1, DECIMAL, null, null );
        final PrimitiveType pt = new PrimitiveType( DECIMAL );
        final TypedObjectNode value = new PrimitiveValue( formattedInputValue, pt );
        final Property prop = new GenericProperty( decimalPt, value );
        featureWriter.export( prop );
        writer.flush();
        writer.close();
        assertEquals( "<property>0.00000009</property>\n", memoryWriter.toString() );
    }

}
