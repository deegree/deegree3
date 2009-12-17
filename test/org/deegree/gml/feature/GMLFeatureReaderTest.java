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
package org.deegree.gml.feature;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.ReferenceResolvingException;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.junit.Test;

/**
 * Tests that check the correct reading of {@link Feature} objects from GML documents.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureReaderTest {

    private static final String BASE_DIR = "testdata/features/";

    @Test
    public void testParsingPhilosopherFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException, ReferenceResolvingException {

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLFeatureReader decoder = new GMLFeatureReader( GMLVersion.GML_31, null, null );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) decoder.parseFeature( wrapper, null );
        decoder.getDocumentIdContext().resolveLocalRefs();
        Assert.assertEquals( 7, fc.size() );
    }

    @Test(expected = XMLParsingException.class)
    public void testParsingPhilosopherFeatureCollectionNoSchema()
                            throws XMLStreamException, FactoryConfigurationError, IOException,
                            ReferenceResolvingException, XMLParsingException, UnknownCRSException {

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection_no_schema.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLFeatureReader gmlAdapter = new GMLFeatureReader( GMLVersion.GML_31, null, null);
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        gmlAdapter.getDocumentIdContext().resolveLocalRefs();
        Assert.assertEquals( 7, fc.size() );
    }

    @Test
    public void testParsingCiteSF0()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, TransformationException,
                            ReferenceResolvingException {

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "dataset-sf0.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        // XMLStreamWriter writer = new FormattingXMLStreamWriter(
        // XMLOutputFactory.newInstance().createXMLStreamWriter(
        // new FileWriter(
        // "/tmp/out.xml" ) ) );
        // writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
        // writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
        // writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        // GML_311FeatureEncoder encoder = new GML_311FeatureEncoder( writer, null );
        // encoder.export( fc );
        // writer.close();

        for ( Feature feature : fc ) {
            if ( "f094".equals( feature.getId() ) ) {
                Property<?> decimalProp = feature.getProperty( new QName( "http://cite.opengeospatial.org/gmlsf",
                                                                          "decimalProperty" ) );
                System.out.println( decimalProp );
            }
        }
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Test
    public void testParsingCiteSF1()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException, TransformationException {

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "dataset-sf1.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();

        for ( Feature feature : fc ) {
            System.out.println( feature.getId() );
            for ( Property<?> prop : feature.getProperties( GMLVersion.GML_31 ) ) {
                System.out.println( prop.getName() );
            }
            break;
        }

        gmlReader.getIdContext().resolveLocalRefs();
        XMLStreamWriter writer = new FormattingXMLStreamWriter(
                                                                XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                      new FileWriter(
                                                                                                                                      "/tmp/out.xml" ) ) );
        writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
        writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        GMLFeatureWriter encoder = new GMLFeatureWriter( writer, null );
        encoder.export( fc );
        writer.close();
    }

    @Test
    public void testParsingCiteSF2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, TransformationException,
                            ReferenceResolvingException {

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "dataset-sf2.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        // XMLStreamWriter writer = new FormattingXMLStreamWriter(
        // XMLOutputFactory.newInstance().createXMLStreamWriter(
        // new FileWriter(
        // "/tmp/out.xml" ) ) );
        // writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
        // writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
        // writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        // GML_311FeatureEncoder encoder = new GML_311FeatureEncoder( writer, null );
        // encoder.export( fc );
        // writer.close();
    }

    @Test
    public void testParsingCite100()
                            throws FactoryConfigurationError, Exception {

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "dataset.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_2, docURL );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

//        XMLStreamWriter writer = new FormattingXMLStreamWriter(
//                                                                XMLOutputFactory.newInstance().createXMLStreamWriter(
//                                                                                                                      new FileWriter(
//                                                                                                                                      "/tmp/out.xml" ) ) );
//        writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
//        writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
//        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
//        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
//        writer.setPrefix( "cgf", "http://www.opengis.net/cite/geometry" );
//        writer.setPrefix( "ccf", "http://www.opengis.net/cite/complex" );
//        writer.setPrefix( "uri", "http://www.opengis.net/cite/data" );
//        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
//        GML2FeatureEncoder encoder = new GML2FeatureEncoder( writer, null );
//        encoder.export( fc );
//        writer.close();
    }

    // @Test
    public void testParsingCityGML()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, TransformationException,
                            ReferenceResolvingException {

        String schemaURL = "http://schemas.opengis.net/citygml/profiles/base/1.0/CityGML.xsd";
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();

        URL docURL = new URL( "file:/home/schneider/Desktop/waldbruecke_v1.0.0.gml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        // work with the fc
        for ( Feature feature : fc ) {
            System.out.println( "member fid: " + feature.getId() );
        }
        System.out.println( "member features: " + fc.size() );
    }

    // @Test
    public void testParsingXPlan20()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, ReferenceResolvingException {

        // BP2070
        URL docURL = new URL(
                              "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/BP2070.gml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        // BP2135
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/BP2135.gml" );
        gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        // PlanA
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/FPlan_2.0.gml" );
        gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        // LA22
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/LA 22.gml" );
        gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        // LA67
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/LA67_2_0.gml" );
        gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        fc = (FeatureCollection) gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();
    }
}
