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

package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionAction;
import org.deegree.protocol.wfs.transaction.TransactionActionType;
import org.deegree.protocol.wfs.transaction.action.Delete;
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

    private final String DELETE1_200 = "v200/TransactionWithDeleteAction1.xml";

    @Test
    public void testDelete1_200()
                            throws Exception {

        Transaction ta = parse( DELETE1_200 );
        assertEquals( VERSION_200, ta.getVersion() );
        assertNull( ta.getHandle() );
        assertNull( ta.getReleaseAction() );

        Iterator<TransactionAction> iter = ta.getActions().iterator();
        TransactionAction operation = iter.next();
        assertEquals( TransactionActionType.DELETE, operation.getType() );
        Delete delete = (Delete) operation;
        assertNull( delete.getHandle() );
        assertEquals( new QName( "InWaterA_1M" ), delete.getTypeName() );
        assertNotNull( delete.getFilter() );

        assertFalse( iter.hasNext() );
    }

    private Transaction parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL exampleURL = this.getClass().getResource( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( exampleURL.toString(),
                                                                                         exampleURL.openStream() );
        xmlStream.nextTag();
        return new TransactionXmlReader200().read( xmlStream );
    }
}
