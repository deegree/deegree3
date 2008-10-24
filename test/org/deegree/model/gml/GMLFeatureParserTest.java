//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
package org.deegree.model.gml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.deegree.commons.xml.FormattingXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.types.ApplicationSchema;
import org.deegree.model.feature.types.FeaturePropertyType;
import org.deegree.model.feature.types.FeatureType;
import org.deegree.model.feature.types.GenericFeatureCollectionType;
import org.deegree.model.feature.types.GenericFeatureType;
import org.deegree.model.feature.types.GeometryPropertyType;
import org.deegree.model.feature.types.PropertyType;
import org.deegree.model.feature.types.SimplePropertyType;
import org.deegree.model.gml.schema.GMLApplicationSchemaXSDAdapter;
import org.deegree.model.gml.schema.GMLVersion;
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

    @Test
    public void testGenericFeatureParsing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        // manually set up a simple "app:Country" feature type
        List<PropertyType> propDecls = new ArrayList<PropertyType>();
        propDecls.add( new SimplePropertyType( new QName( "http://www.deegree.org/app", "name" ), 1, 1,
                                               new QName( "http://www.w3.org/2001/XMLSchema", "string" ) ) );
        propDecls.add( new GeometryPropertyType( new QName( "http://www.deegree.org/app", "boundary" ), 1, 1,
                                                 new QName( "http://www.opengis.net", "MultiSurfacePropertyType" ) ) );

        FeatureType ft = new GenericFeatureType( new QName( "http://www.deegree.org/app", "Country" ), propDecls, false );
        FeatureType[] fts = new FeatureType[] { ft };
        ApplicationSchema schema = new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>() );

        GMLFeatureParser adapter = new GMLFeatureParser( schema );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "SimpleFeatureExample1.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = adapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null,
                                                idContext );
        xmlReader.close();

        Assert.assertEquals( new QName( "http://www.deegree.org/app", "Country" ), feature.getName() );
        Assert.assertEquals( "COUNTRY_1", feature.getId() );
        Assert.assertEquals( 2, feature.getProperties().length );
        Assert.assertEquals( "France", feature.getProperties()[0].getValue() );

        XMLStreamWriter xmlWriter = new FormattingXMLStreamWriter(
                                                                   XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                         System.out ) );
    }

    @Test
    public void testGenericFeatureParsingNoNS()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        // manually set up a simple "app:Country" feature type
        List<PropertyType> propDecls = new ArrayList<PropertyType>();
        propDecls.add( new SimplePropertyType( new QName( "name" ), 1, 1,
                                               new QName( "http://www.w3.org/2001/XMLSchema", "string" ) ) );
        propDecls.add( new GeometryPropertyType( new QName( "boundary" ), 1, 1, new QName( "http://www.opengis.net",
                                                                                           "MultiSurfacePropertyType" ) ) );

        FeatureType ft = new GenericFeatureType( new QName( "Country" ), propDecls, false );
        FeatureType[] fts = new FeatureType[] { ft };
        ApplicationSchema schema = new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>() );
        GMLFeatureParser adapter = new GMLFeatureParser( schema );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "SimpleFeatureExampleNoNS1.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = adapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null,
                                                idContext );
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

    @Test
    public void testGenericFeatureCollectionParsing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        FeatureType[] fts = new FeatureType[2];

        // manually set up a simple "app:Country" feature type
        List<PropertyType> propDecls = new ArrayList<PropertyType>();
        propDecls.add( new SimplePropertyType( new QName( "http://www.deegree.org/app", "name" ), 1, 1,
                                               new QName( "http://www.w3.org/2001/XMLSchema", "string" ) ) );
        propDecls.add( new GeometryPropertyType( new QName( "http://www.deegree.org/app", "boundary" ), 1, 1,
                                                 new QName( "http://www.opengis.net", "MultiSurfacePropertyType" ) ) );
        fts[0] = new GenericFeatureType( new QName( "http://www.deegree.org/app", "Country" ), propDecls, false );

        // manually set up "gml:FeatureCollection" feature (collection) type
        propDecls = new ArrayList<PropertyType>();
        propDecls.add( new FeaturePropertyType( new QName( "http://www.opengis.net/gml", "featureMember" ), 1, -1,
                                                new QName( "http://www.opengis.net/gml", "_Feature" ) ) );
        fts[1] = new GenericFeatureCollectionType( new QName( "http://www.opengis.net/gml", "FeatureCollection" ),
                                                   propDecls, false );
        ApplicationSchema schema = new ApplicationSchema( fts, new HashMap<FeatureType, FeatureType>() );
        GMLFeatureParser adapter = new GMLFeatureParser( schema );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "SimpleFeatureCollectionExample1.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = adapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null,
                                                idContext );
        xmlReader.close();

        // Assert.assertEquals( new QName( "http://www.deegree.org/app", "Country" ), feature.getName() );
        // Assert.assertEquals( "COUNTRY_1", feature.getId() );
        // Assert.assertEquals( 2, feature.getProperties().length );
        // Assert.assertEquals( "France", feature.getProperties()[0].getValue() );
    }

    @Test
    public void testParsingIMRO2008FeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        String schemaURL = "file:///home/schneider/workspace/vrom_roonline2/resources/schema/imro2008/local-IMRO2008.xsd";
        GMLApplicationSchemaXSDAdapter xsdAdapter = new GMLApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.VERSION_31 );
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( xsdAdapter.extractFeatureTypeSchema() );

        URL docURL = new URL(
                              "file:///home/schneider/workspace/vrom_roonline2/resources/data/IMRO2008-testplans/NL.IMRO.0964.000matrixplan1-0003.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null,
                                                   idContext );
        idContext.resolveXLinks();
        xmlReader.close();
    }

    @Test
    public void testParsingIMRO2006FeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        String schemaURL = "file:///home/schneider/workspace/vrom_roonline2/resources/schema/imro2006/IMRO2006.xsd";
        GMLApplicationSchemaXSDAdapter xsdAdapter = new GMLApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.VERSION_31 );
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( xsdAdapter.extractFeatureTypeSchema() );

        URL docURL = new URL(
                              "file:///home/schneider/workspace/vrom-roonline/resources/data/testdata_herman_oktober2007/NL.IMRO.00280000-hoekschewaard.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null,
                                                   idContext );
        idContext.resolveXLinks();
        xmlReader.close();
    }

    @Test
    public void testParsingXPlanGMLFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        String schemaURL = "file:///home/schneider/workspace/lkee_xplanung/resources/schema/XPlanung-Operationen.xsd";
        GMLApplicationSchemaXSDAdapter xsdAdapter = new GMLApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.VERSION_31 );
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( xsdAdapter.extractFeatureTypeSchema() );

        URL docURL = new URL( "file:///home/schneider/workspace/lkee_xplanung/resources/data/BP2070.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.nextTag();
        GMLIdContext idContext = new GMLIdContext();
        Feature feature = gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null,
                                                   idContext );
        idContext.resolveXLinks();
        xmlReader.close();
    }

    @Test
    public void testParsingPhilosopherFeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {
        String schemaURL = this.getClass().getResource( "schema/Philosopher_typesafe.xsd" ).toString();
        GMLApplicationSchemaXSDAdapter xsdAdapter = new GMLApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.VERSION_31 );
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( xsdAdapter.extractFeatureTypeSchema() );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        FeatureCollection fc = (FeatureCollection) gmlAdapter.parseFeature(
                                                                            new XMLStreamReaderWrapper(
                                                                                                        xmlReader,
                                                                                                        docURL.toString() ),
                                                                            null, idContext );
        idContext.resolveXLinks();
    }
}
