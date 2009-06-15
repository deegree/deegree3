//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.feature.gml.schema;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.FormattingXMLStreamWriter;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.JAXBAdapter;
import org.junit.Before;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class ApplicationSchemaXSDExporterTest {

    private ApplicationSchema schema;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        URL url = new URL( "file:/home/schneider/workspace/d3_commons/resources/schema/feature/example.xml" );
        JAXBAdapter adapter = new JAXBAdapter( url );
        schema = adapter.getApplicationSchema();     
    }

    @Test
    public void testExportAsGML31() throws XMLStreamException, IOException {        
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
//        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );        
        FormattingXMLStreamWriter writer = new FormattingXMLStreamWriter (outputFactory.createXMLStreamWriter( new FileWriter ("/home/schneider/philosopher_gml200.xsd") ));
        new ApplicationSchemaXSDExporter(GMLVersion.GML_2, null).export( writer, schema );
        writer.close();
    }
}
