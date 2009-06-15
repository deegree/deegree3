//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.feature.gml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.deegree.commons.xml.FormattingXMLStreamWriter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.gml.schema.ApplicationSchemaXSDAdapter;
import org.deegree.feature.gml.schema.GMLVersion;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.JAXBAdapter;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.SimplePropertyType.PrimitiveType;
import org.deegree.geometry.Geometry;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLFeatureParserTest {

    private static final String BASE_DIR = "testdata/features/";

    // @Test
    public void testGenericFeatureParsing()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {

        // manually set up a simple "app:Country" feature type
        List<PropertyType> propDecls = new ArrayList<PropertyType>();
        propDecls.add( new SimplePropertyType( new QName( "http://www.deegree.org/app", "name" ), 1, 1,
                                               PrimitiveType.STRING ) );
        propDecls.add( new GeometryPropertyType( new QName( "http://www.deegree.org/app", "boundary" ), 1, 1,
                                                 GeometryType.MULTI_SURFACE, CoordinateDimension.DIM_2 ) );

        FeatureType ft = new GenericFeatureType( new QName( "http://www.deegree.org/app", "Country" ), propDecls, false );
        FeatureType[] fts = new FeatureType[] { ft };
        ApplicationSchema schema = new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>(), null );

        GMLFeatureParser adapter = new GMLFeatureParser( schema );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "SimpleFeatureExample1.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        Feature feature = adapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        xmlReader.close();

        Assert.assertEquals( new QName( "http://www.deegree.org/app", "Country" ), feature.getName() );
        Assert.assertEquals( "COUNTRY_1", feature.getId() );
        Assert.assertEquals( 2, feature.getProperties().length );
        Assert.assertEquals( "France", feature.getProperties()[0].getValue() );

        XMLStreamWriter xmlWriter = new FormattingXMLStreamWriter(
                                                                   XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                         System.out ) );
    }

    // @Test
    public void testGenericFeatureParsingNoNS()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {

        // manually set up a simple "app:Country" feature type
        List<PropertyType> propDecls = new ArrayList<PropertyType>();
        propDecls.add( new SimplePropertyType( new QName( "name" ), 1, 1,
                                               PrimitiveType.STRING ) );
        propDecls.add( new GeometryPropertyType( new QName( "http://www.deegree.org/app", "boundary" ), 1, 1,
                                                 GeometryType.MULTI_SURFACE, CoordinateDimension.DIM_2 ) );

        FeatureType ft = new GenericFeatureType( new QName( "Country" ), propDecls, false );
        FeatureType[] fts = new FeatureType[] { ft };
        ApplicationSchema schema = new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>(), null );
        GMLFeatureParser adapter = new GMLFeatureParser( schema );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "SimpleFeatureExampleNoNS1.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = adapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        xmlReader.close();

        Assert.assertEquals( new QName( "Country" ), feature.getName() );
        Assert.assertEquals( "COUNTRY_1", feature.getId() );
        Assert.assertEquals( 2, feature.getProperties().length );
        Assert.assertEquals( "France", feature.getProperties()[0].getValue() );
    }

    // public void testFeatureExport () throws XMLStreamException, FactoryConfigurationError, IOException {
    //
    // XMLStreamWriter xmlWriter = new
    // IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
    // xmlWriter.setPrefix("app", "http://www.deegree.org/app");
    // adapter.export(xmlWriter, feature);
    // xmlWriter.flush();
    // }

    // @Test
    public void testGenericFeatureCollectionParsing()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {

        FeatureType[] fts = new FeatureType[2];

        // manually set up a simple "app:Country" feature type
        List<PropertyType> propDecls = new ArrayList<PropertyType>();
        propDecls.add( new SimplePropertyType( new QName( "http://www.deegree.org/app", "name" ), 1, 1,
                                               PrimitiveType.STRING ) );
        propDecls.add( new GeometryPropertyType( new QName( "http://www.deegree.org/app", "boundary" ), 1, 1,
                                                 GeometryType.MULTI_SURFACE, CoordinateDimension.DIM_2 ) );
        fts[0] = new GenericFeatureType( new QName( "http://www.deegree.org/app", "Country" ), propDecls, false );

        // manually set up "gml:FeatureCollection" feature (collection) type
        propDecls = new ArrayList<PropertyType>();
        propDecls.add( new FeaturePropertyType( new QName( "http://www.opengis.net/gml", "featureMember" ), 1, -1,
                                                new QName( "http://www.opengis.net/gml", "_Feature" ) ) );
        fts[1] = new GenericFeatureCollectionType( new QName( "http://www.opengis.net/gml", "FeatureCollection" ),
                                                   propDecls, false );
        ApplicationSchema schema = new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>(), null );
        GMLFeatureParser adapter = new GMLFeatureParser( schema );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "SimpleFeatureCollectionExample1.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = adapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        xmlReader.close();

        // Assert.assertEquals( new QName( "http://www.deegree.org/app", "Country" ), feature.getName() );
        // Assert.assertEquals( "COUNTRY_1", feature.getId() );
        // Assert.assertEquals( 2, feature.getProperties().length );
        // Assert.assertEquals( "France", feature.getProperties()[0].getValue() );
    }

    // @Test
    public void testParsingIMRO2008FeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException {

        String schemaURL = "file:///home/schneider/workspace/prvlimburg_nlrpp/resources/schemas/imro2008/IMRO2008-with-xlinks.xsd";
        ApplicationSchemaXSDAdapter xsdAdapter = new ApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.GML_31 );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = new URL(
                              "file:///home/schneider/workspace/prvlimburg_nlrpp/resources/testplans/NL.IMRO.0964.000matrixplan2-0003.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( schema, idContext );
        Feature feature = gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        System.out.println( "A" );
        idContext.resolveXLinks( schema );
        System.out.println( "B" );
        xmlReader.close();
    }

    // @Test
    public void testParsingIMRO2006FeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException {

        String schemaURL = "file:///home/schneider/workspace/prvlimburg_nlrpp/resources/schemas/imro2006/IMRO2006-adapted.xsd";
        ApplicationSchemaXSDAdapter xsdAdapter = new ApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.GML_31 );

        URL docURL = new URL(
                              "file:///home/schneider/workspace/prvlimburg_nlrpp/resources/testplans/NL.IMRO.02020000705-.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( xsdAdapter.extractFeatureTypeSchema(), idContext );
        Feature feature = gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );

        System.out.println( idContext );

        // idContext.resolveXLinks();

        xmlReader.close();
    }

    // @Test
    public void testParsingXPlanGMLFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException {

        String schemaURL = "file:///home/schneider/workspace/lkee_xplanung/resources/schema/XPlanung-Operationen.xsd";
        ApplicationSchemaXSDAdapter xsdAdapter = new ApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.GML_31 );
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( xsdAdapter.extractFeatureTypeSchema() );

        URL docURL = new URL( "file:///home/schneider/workspace/lkee_xplanung/resources/data/BP2070.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        Feature feature = gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        xmlReader.close();
    }

    @Test
    public void testParsingPhilosopherFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, JAXBException {

        URL url = new URL( "file:/home/schneider/workspace/d3_commons/resources/schema/feature/example.xml" );
        JAXBAdapter adapter = new JAXBAdapter( url );
        ApplicationSchema schema = adapter.getApplicationSchema();        
        
//        String schemaURL = this.getClass().getResource( "schema/Philosopher_typesafe.xsd" ).toString();
//        GMLApplicationSchemaXSDAdapter xsdAdapter = new GMLApplicationSchemaXSDAdapter( schemaURL,
//                                                                                        GMLVersion.VERSION_31 );
//        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( schema, idContext );
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature(
                                                                            new XMLStreamReaderWrapper(
                                                                                                        xmlReader,
                                                                                                        docURL.toString() ),
                                                                            null );
        idContext.resolveXLinks( schema );

        for ( Feature member : fc ) {
            System.out.println( member );
        }
    }

    @Test
    public void testParsingCityGML()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException {
        String schemaURL = "file:///home/schneider/workspace/schemas/citygml/profiles/base/1.0/CityGML.xsd";
        ApplicationSchemaXSDAdapter xsdAdapter = new ApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.GML_31 );

        URL docURL = new URL( "file:///home/schneider/Desktop/Stadt-Ettenheim-LoD3_edited_v1.0.0.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( schema, idContext );
        long begin = System.currentTimeMillis();
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature(
                                                                            new XMLStreamReaderWrapper(
                                                                                                        xmlReader,
                                                                                                        docURL.toString() ),
                                                                            new CRS( "EPSG:31466" ) );
        idContext.resolveXLinks(schema);

        long elapsed = System.currentTimeMillis() - begin;
        System.out.println( "Parsing: " + elapsed + "[ms]" );

        System.out.println( fc.size() );
        Feature first = fc.iterator().next();
        for ( Property prop : first.getProperties() ) {
            System.out.println( prop.getName() + "=" + prop.getValue() );
            if ( prop.getValue() instanceof Geometry ) {
                System.out.println( ( (Geometry) prop.getValue() ).getCoordinateSystem() );
            }
        }
    }
}
