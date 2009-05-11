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

package org.deegree.commons.jdbc;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class ConnectionManagerTest {

    @Test
    public void testConnectionAllocation () throws JAXBException, SQLException {

        URL configURL = ConnectionManagerTest.class.getResource( "jdbc_connections.xml");
        ConnectionManager.init (configURL);
        
        Connection[] conns = new Connection[20];
        for ( int i = 0; i < conns.length; i++ ) {
            conns [i] = ConnectionManager.getConnection( "conn1" );
        }
        
        for ( int i = 0; i < conns.length; i++ ) {
            conns [i].close();
        }        
    }    
}
