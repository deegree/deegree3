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

package org.deegree.feature.gml;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.gml.GMLVersion;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.gml.schema.ApplicationSchemaXSDDecoder;
import org.deegree.feature.types.ApplicationSchema;
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
public class GMLFeatureEncoderTest {

    private final String DIR = "testdata/features/";

    private final String SOURCE_FILE = "Philosopher_FeatureCollection.xml";

    private final String SCHEMA_LOCATION_ATTRIBUTE = "testdata/schema/Philosopher.xsd";

    private final String SCHEMA_LOCATION = "http://www.deegree.org/app testdata/schema/Philosopher.xsd";

    @Test
    public void testValidateExportedFeatures()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
                            XMLParsingException, UnknownCRSException, TransformationException {
        String schemaURL = this.getClass().getResource( SCHEMA_LOCATION_ATTRIBUTE ).toString();
        ApplicationSchemaXSDDecoder xsdAdapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = GMLFeatureEncoderTest.class.getResource( DIR + SOURCE_FILE );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder gmlAdapter = new GMLFeatureDecoder( schema, idContext );
        Feature feature = gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        idContext.resolveXLinks( schema );

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
        GML311FeatureEncoder exporter = new GML311FeatureEncoder( writer, null );
        exporter.export( feature );
        writer.flush();
        writer.close();
//        XMLAssert.assertValidity( memoryWriter.getReader() );
    }
}
