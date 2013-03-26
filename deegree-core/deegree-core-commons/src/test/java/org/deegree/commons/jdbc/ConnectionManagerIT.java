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

package org.deegree.commons.jdbc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConnectionManagerIT {

    private static final String JDBCCONFIGNAME = "jdbc_connection.xml";

    @Ignore("Ignored until the database connection is set correctly. Must be a valid connection, otherweise the test fails!")
    @Test
    public void testGetConnectionInitialzedFromResource()
                            throws URISyntaxException, IOException, SQLException, ResourceInitException {
        // build
        File workspaceDir = setupWorkspace();
        DeegreeWorkspace workspace = DeegreeWorkspace.getInstance( "UnitTest", workspaceDir );
        // operate
        workspace.initAll();
        // compare
        Connection connection = ConnectionManager.getConnection( "jdbc_connection" );
        assertNotNull( connection );
    }

    private File setupWorkspace()
                            throws IOException, FileNotFoundException {
        File workspaceDir = File.createTempFile( "UnitTest", "d3workspace" );
        workspaceDir.delete();
        workspaceDir.mkdir();
        InputStream jdbcConfig = ConnectionManagerIT.class.getResourceAsStream( JDBCCONFIGNAME );
        workspaceDir.mkdir();
        File jdbcDir = new File( workspaceDir, "jdbc" );
        jdbcDir.mkdir();
        IOUtils.copy( jdbcConfig, new FileOutputStream( new File( jdbcDir, JDBCCONFIGNAME ) ) );
        return workspaceDir;
    }

}