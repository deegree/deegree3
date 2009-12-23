//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.gml.feature.schema;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.JAXBAdapter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDEncoder;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ApplicationSchemaXSDEncoderTest {

//    @Test
    public void testExportAsGML31()
                            throws XMLStreamException, IOException, JAXBException {

        URL url = ApplicationSchemaXSDEncoderTest.class.getResource( "example.xml" );
        JAXBAdapter adapter = new JAXBAdapter( url );
        ApplicationSchema schema = adapter.getApplicationSchema();
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        // outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );

        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        new ApplicationSchemaXSDEncoder( GMLVersion.GML_31, null ).export( memoryWriter.getXMLStreamWriter(), schema );
        memoryWriter.getXMLStreamWriter().close();
    }

    @Test
    public void testReexportCiteSF1()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/cite-gmlsf1.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();

        XMLStreamWriter writer = new FormattingXMLStreamWriter(
                                                                XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                      new FileWriter(
                                                                                                                                      "/tmp/out.xml" ) ) );
        writer.setPrefix( "xlink", CommonNamespaces.XLNNS );
        writer.setPrefix( "sf", "http://cite.opengeospatial.org/gmlsf" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        new ApplicationSchemaXSDEncoder( GMLVersion.GML_31, null ).export( writer, schema );
        writer.close();
    }
}
