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
package org.deegree.feature.gml;

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

import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.gml.GMLVersion;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.gml.schema.ApplicationSchemaXSDDecoder;
import org.deegree.feature.types.ApplicationSchema;
import org.junit.Test;

/**
 * Tests that check the extraction of {@link Feature}s from GML documents.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureDecoderTest {

    private static final String BASE_DIR = "testdata/features/";

    @Test
    public void testParsingPhilosopherFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException {

        URL docURL = GMLFeatureDecoderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );               
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );
        
        System.out.println (fc.size ());
    }

    @Test(expected = XMLParsingException.class)
    public void testParsingPhilosopherFeatureCollectionNoSchema()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException {

        URL docURL = GMLFeatureDecoderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection_no_schema.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
    }

    @Test
    public void testParsingCiteSF0()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, TransformationException {

        URL docURL = GMLFeatureDecoderTest.class.getResource( BASE_DIR + "dataset-sf0.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

//        XMLStreamWriter writer = new FormattingXMLStreamWriter(
//                                                                XMLOutputFactory.newInstance().createXMLStreamWriter(
//                                                                                                                      new FileWriter(
//                                                                                                                                      "/tmp/out.xml" ) ) );
//        writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
//        writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
//        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
//        GML311FeatureEncoder encoder = new GML311FeatureEncoder( writer, null );
//        encoder.export( fc );
//        writer.close();

        for ( Feature feature : fc ) {
            if ( "f094".equals( feature.getId() ) ) {
                Property<?> decimalProp = feature.getProperty( new QName( "http://cite.opengeospatial.org/gmlsf",
                                                                               "decimalProperty" ) );
                System.out.println (decimalProp);
            }
        }
    }

    @Test
    public void testParsingCiteSF1()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, TransformationException {

        URL docURL = GMLFeatureDecoderTest.class.getResource( BASE_DIR + "dataset-sf1.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

        XMLStreamWriter writer = new FormattingXMLStreamWriter(
                                                                XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                      new FileWriter(
                                                                                                                                      "/tmp/out.xml" ) ) );
        writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
        writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        GML311FeatureEncoder encoder = new GML311FeatureEncoder( writer, null );
        encoder.export( fc );
        writer.close();
    }
    
    @Test
    public void testParsingCiteSF2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, TransformationException {

        URL docURL = GMLFeatureDecoderTest.class.getResource( BASE_DIR + "dataset-sf2.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

//        XMLStreamWriter writer = new FormattingXMLStreamWriter(
//                                                                XMLOutputFactory.newInstance().createXMLStreamWriter(
//                                                                                                                      new FileWriter(
//                                                                                                                                      "/tmp/out.xml" ) ) );
//        writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
//        writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
//        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
//        GML311FeatureEncoder encoder = new GML311FeatureEncoder( writer, null );
//        encoder.export( fc );
//        writer.close();
    }    

    // @Test
    public void testParsingCityGML()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException, TransformationException {

        String schemaURL = "http://schemas.opengis.net/citygml/profiles/base/1.0/CityGML.xsd";
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();

        URL docURL = new URL( "file:/home/schneider/Desktop/waldbruecke_v1.0.0.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( schema, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

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
                            XMLParsingException, UnknownCRSException, JAXBException {

        // BP2070
        URL docURL = new URL(
                              "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/BP2070.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( null, idContext );
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

        // BP2135
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/BP2135.gml" );
        xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(), docURL.openStream() );
        xmlReader.nextTag();
        idContext = new GMLIdContext();
        gmlAdapter = new GMLFeatureDecoder( null, idContext );
        wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

        // PlanA
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/FPlan_2.0.gml" );
        xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(), docURL.openStream() );
        xmlReader.nextTag();
        idContext = new GMLIdContext();
        gmlAdapter = new GMLFeatureDecoder( null, idContext );
        wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

        // LA22
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/LA 22.gml" );
        xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(), docURL.openStream() );
        xmlReader.nextTag();
        idContext = new GMLIdContext();
        gmlAdapter = new GMLFeatureDecoder( null, idContext );
        wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );

        // LA67
        docURL = new URL( "file:/home/schneider/workspace/lkee_xplanung2/resources/testdata/XPlanGML_2_0/LA67_2_0.gml" );
        xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(), docURL.openStream() );
        xmlReader.nextTag();
        idContext = new GMLIdContext();
        gmlAdapter = new GMLFeatureDecoder( null, idContext );
        wrapper = new XMLStreamReaderWrapper( xmlReader, docURL.toString() );
        fc = (FeatureCollection) gmlAdapter.parseFeature( wrapper, null );
        idContext.resolveXLinks( gmlAdapter.getApplicationSchema() );
    }
}
