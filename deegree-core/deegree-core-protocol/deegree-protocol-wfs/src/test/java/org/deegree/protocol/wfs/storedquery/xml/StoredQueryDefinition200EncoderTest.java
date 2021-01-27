//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wfs.storedquery.xml;

import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.XmlMatchers.isSimilarTo;
import static org.xmlmatchers.transform.XmlConverters.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;
import org.deegree.protocol.wfs.storedquery.xml.StoredQueryDefinition200Encoder;
import org.deegree.protocol.wfs.storedquery.xml.StoredQueryDefinitionXMLAdapter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StoredQueryDefinition200EncoderTest {

    private static final NamespaceBindings NS_CONTEXT = new NamespaceBindings();

    @BeforeClass
    public static void initNamespaceContext()
                            throws IOException {
        NS_CONTEXT.addNamespace( "wfs", WFS_200_NS );
    }

    @Test
    public void testExport()
                            throws Exception {
        String storedQueryResource = "storedQuery.xml";
        StoredQueryDefinition queryDefinition = parseStoredQueryDefinition( storedQueryResource );

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
        StoredQueryDefinition200Encoder.export( queryDefinition, writer );
        writer.close();

        assertThat( xml( stream.toString() ),
                    hasXPath( "/wfs:StoredQueryDefinition/wfs:QueryExpressionText/wfs:Query/@typeNames",
                              is( "cp:CadastralParcel" ), NS_CONTEXT ) );

        assertThat( xml( stream.toString() ),
                    hasXPath( "/wfs:StoredQueryDefinition/wfs:Parameter/@name", is( "label" ), NS_CONTEXT ) );

        assertThat( xml( stream.toString() ), isSimilarTo( the( storedQueryResource ) ) );
    }

    private StoredQueryDefinition parseStoredQueryDefinition( String resource )
                            throws IOException {
        InputStream storedQueryResource = StoredQueryDefinition200EncoderTest.class.getResourceAsStream( resource );
        StoredQueryDefinitionXMLAdapter storedQueryXMLAdapter = new StoredQueryDefinitionXMLAdapter();
        storedQueryXMLAdapter.load( storedQueryResource );
        StoredQueryDefinition queryDefinition = storedQueryXMLAdapter.parse();
        storedQueryResource.close();
        return queryDefinition;
    }

    private Source the( String resource ) {
        InputStream inputStream = StoredQueryDefinition200EncoderTest.class.getResourceAsStream( resource );
        return new StreamSource( inputStream );
    }

}