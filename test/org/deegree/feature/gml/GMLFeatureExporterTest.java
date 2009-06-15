//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.gml.schema.ApplicationSchemaXSDAdapter;
import org.deegree.feature.gml.schema.GMLVersion;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.junit.XMLAssert;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * Exports the features in the Philosophers example and validates them against the corresponding schema.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class GMLFeatureExporterTest {

    final String DIR = "testdata/features/";
    
    final String SOURCE_FILE = "Philosopher_FeatureCollection.xml";
    
    final String SCHEMA_LOCATION_ATTRIBUTE = "schema/Philosopher_typesafe.xsd";
    
    final String SCHEMA_LOCATION = "http://www.deegree.org/app schema/Philosopher_typesafe.xsd"; 

    @Test
    public void testValidateExportedFeatures()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException,
                            XMLParsingException, UnknownCRSException {
        String schemaURL = this.getClass().getResource( SCHEMA_LOCATION_ATTRIBUTE ).toString();
        ApplicationSchemaXSDAdapter xsdAdapter = new ApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.GML_31 );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();

        URL docURL = GMLFeatureExporterTest.class.getResource( DIR + SOURCE_FILE );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( schema, idContext );
        Feature feature = gmlAdapter.parseFeature(new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        idContext.resolveXLinks( schema );
        
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        OutputStream out = new FileOutputStream( "/tmp/exported_" + SOURCE_FILE );
        XMLStreamWriterWrapper writer = 
            new XMLStreamWriterWrapper( outputFactory.createXMLStreamWriter( out ), SCHEMA_LOCATION_ATTRIBUTE );
        writer.setDefaultNamespace( "http://www.opengis.net/gml" );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );        
        GMLFeatureExporter exporter = new GMLFeatureExporter( writer );
        exporter.export( feature );    
        writer.flush();
        writer.close();
        out.close();
        
        XMLAssert.assertValidDocument( schemaURL, new InputSource( new FileReader( "/tmp/exported_" + SOURCE_FILE ) ) );
    }
}
