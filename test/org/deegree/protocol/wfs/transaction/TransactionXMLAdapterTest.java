//$HeadURL$
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

package org.deegree.protocol.wfs.transaction;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.deegree.protocol.wfs.WFSConstants;
import org.junit.Test;

/**
 * Test cases for the {@link TransactionXMLAdapter}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TransactionXMLAdapterTest extends TestCase {

    private final String DELETE_110 = "examples_xml/v110/delete.xml";

    @Test
    public void testDelete110()
                            throws Exception {

        Transaction ta = parse( DELETE_110 );
        assertEquals( WFSConstants.VERSION_110, ta.getVersion() );
        assertEquals( "TA_1", ta.getHandle() );
        assertEquals( null, ta.getReleaseAction() );

        for ( TransactionOperation operation : ta.getOperations()) {
            System.out.println (operation);
        }
    }

    private Transaction parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL exampleURL = this.getClass().getResource( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( exampleURL.toString(),
                                                                                         exampleURL.openStream() );
        xmlStream.nextTag();
        return TransactionXMLAdapter.parse( xmlStream );
    }
}
