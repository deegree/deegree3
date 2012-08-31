//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.protocol.wfs.transaction.action.Delete;
import org.deegree.protocol.wfs.transaction.action.Insert;
import org.junit.Test;

/**
 * Test cases for {@link TransactionXmlReader200}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TransactionXmlReader200Test extends TestCase {

    private final String DELETE_ACTION1_200 = "v200/DeleteAction1.xml";

    private final String DELETE_ACTION2_200 = "v200/DeleteAction2.xml";

    private final String DELETE_ACTION3_200 = "v200/DeleteAction3.xml";

    private final String INSERT_ACTION1_200 = "v200/InsertAction1.xml";

    private final TransactionXmlReader200 reader = new TransactionXmlReader200();

    @Test
    public void testReadDeleteActionWfs200SpecExample1()
                            throws Exception {
        XMLStreamReader xmlStream = getXMLStreamReader( DELETE_ACTION1_200 );
        Delete delete = reader.readDelete( xmlStream );
        xmlStream.require( XMLStreamReader.END_ELEMENT, WFS_200_NS, "Delete" );
        assertNull( delete.getHandle() );
        assertEquals( new QName( "InWaterA_1M" ), delete.getTypeName() );
        assertNotNull( delete.getFilter() );
    }

    @Test
    public void testReadDeleteActionWfs200SpecExample2()
                            throws Exception {
        XMLStreamReader xmlStream = getXMLStreamReader( DELETE_ACTION2_200 );
        Delete delete = reader.readDelete( xmlStream );
        xmlStream.require( XMLStreamReader.END_ELEMENT, WFS_200_NS, "Delete" );
        assertNull( delete.getHandle() );
        QName expectedTypeName = new QName( "http://www.someserver.com/myns", "InWaterA_1M" );
        assertEquals( expectedTypeName, delete.getTypeName() );
        assertNotNull( delete.getFilter() );
    }

    @Test
    public void testReadDeleteActionWfs200SpecExample3()
                            throws Exception {
        XMLStreamReader xmlStream = getXMLStreamReader( DELETE_ACTION3_200 );
        Delete delete = reader.readDelete( xmlStream );
        xmlStream.require( XMLStreamReader.END_ELEMENT, WFS_200_NS, "Delete" );
        assertNull( delete.getHandle() );
        QName expectedTypeName = new QName( "InWaterA_1M" );
        assertEquals( expectedTypeName, delete.getTypeName() );
        assertNotNull( delete.getFilter() );
    }

    @Test
    public void testReadInsertActionWfs200SpecExample1()
                            throws Exception {
        XMLStreamReader xmlStream = getXMLStreamReader( INSERT_ACTION1_200 );
        Insert insert = reader.readInsert( xmlStream );
        assertNull( insert.getHandle() );
        assertNull( insert.getInputFormat() );
        assertNull( insert.getSrsName() );
        xmlStream.require( XMLStreamReader.START_ELEMENT, null, "InWaterA_1M" );
        assertNull( insert.getSrsName() );
        assertEquals( xmlStream, insert.getFeatures() );
    }

    private XMLStreamReader getXMLStreamReader( String resourceName )
                            throws XMLStreamException, IOException {
        URL exampleURL = this.getClass().getResource( resourceName );
        XMLInputFactory inputFac = XMLInputFactory.newInstance();
        XMLStreamReader xmlStream = inputFac.createXMLStreamReader( exampleURL.toString(), exampleURL.openStream() );
        XMLStreamUtils.skipStartDocument( xmlStream );
        return xmlStream;
    }
}
