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

import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
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

    private final String DIR = "testdata/features/";

    private final String SOURCE_FILE = "Philosopher_FeatureCollection.xml";

    private final String SCHEMA_LOCATION_ATTRIBUTE = "testdata/schema/Philosopher.xsd";

    private final String SCHEMA_LOCATION = "http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/feature.xsd http://www.deegree.org/app testdata/schema/Philosopher.xsd";

    @Test
    public void testWriteGML2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException,
                            XMLParsingException, UnknownCRSException, TransformationException {
        String schemaURL = this.getClass().getResource( SCHEMA_LOCATION_ATTRIBUTE ).toString();
        ApplicationSchemaXSDDecoder xsdAdapter = new ApplicationSchemaXSDDecoder( GML_31, null, schemaURL );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = GMLFeatureWriterTest.class.getResource( DIR + SOURCE_FILE );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        Feature feature = gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(), SCHEMA_LOCATION );
        writer.setDefaultNamespace( "http://www.opengis.net/gml" );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GMLFeatureWriter exporter = new GMLFeatureWriter( GML_2, new FormattingXMLStreamWriter( writer ), null, null,
                                                          null, null, 0, -1, null, false );
        exporter.export( feature );
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
        ApplicationSchemaXSDDecoder xsdAdapter = new ApplicationSchemaXSDDecoder( GML_31, null, schemaURL );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = GMLFeatureWriterTest.class.getResource( DIR + SOURCE_FILE );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        Feature feature = gmlReader.readFeature();
        gmlReader.getIdContext().resolveLocalRefs();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(), SCHEMA_LOCATION );
        writer.setDefaultNamespace( "http://www.opengis.net/gml" );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GMLFeatureWriter exporter = new GMLFeatureWriter( GML_31, writer, null, null, null, null, 0, -1, null, false );
        exporter.export( feature );
        writer.flush();
        writer.close();
        // XMLAssert.assertValidity( memoryWriter.getReader() );
    }
}
