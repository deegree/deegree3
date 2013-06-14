//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.db;

import static org.deegree.commons.xml.XMLAdapter.writeElement;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.concurrent.Executor;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.IncorporealResourceLocation;

/**
 * Utilities for handling connection providers.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConnectionProviderUtils {

    /**
     * Use this to obtain a synthetic connection provider resource for use in a workspace without an actual config file.
     * 
     * @param id
     *            the id you wish the connection provider to have, never <code>null</code>
     * @param url
     *            never <code>null</code>
     * @param user
     *            never <code>null</code>
     * @param pass
     *            never <code>null</code>
     * @return never <code>null</code>
     */
    public static ResourceLocation<ConnectionProvider> getSyntheticProvider( String id, String url, String user,
                                                                             String pass ) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
            writer.writeStartDocument();
            String ns = "http://www.deegree.org/jdbc";
            writer.setDefaultNamespace( ns );
            writer.writeStartElement( ns, "JDBCConnection" );
            writer.writeDefaultNamespace( ns );
            writer.writeAttribute( "configVersion", "3.0.0" );
            writeElement( writer, ns, "Url", url );
            writeElement( writer, ns, "User", user );
            writeElement( writer, ns, "Password", pass );
            writer.writeEndElement();
            writer.close();
            bos.close();
            ResourceIdentifier<ConnectionProvider> rid;
            rid = new DefaultResourceIdentifier<ConnectionProvider>( ConnectionProviderProvider.class, id );
            return new IncorporealResourceLocation<ConnectionProvider>( bos.toByteArray(), rid );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Unable to create synthetic connection provider: "
                                             + e.getLocalizedMessage(), e );
        }
    }

    /**
     * Executes the SQL query in the given {@link PreparedStatement} object and returns the ResultSet object generated
     * by the query.
     * 
     * @param stmt
     *            statement to be executed, must not be <code>null</code>
     * @param connManager
     *            {@link ConnectionProvider} that has been used to retrieve the connection (needed for invalidating
     *            cancelled queries), must not be <code>null</code>
     * @param connId
     *            id of the connection pool that the connection belongs to (needed for invalidating cancelled queries),
     *            must not be <code>null</code>
     * @param executionTimeout
     *            timeout in milliseconds, if 0 or negative, timeout is disabled
     * @return a <code>ResultSet</code> object that contains the data produced by the query, never <code>null</code>
     * @throws SQLException
     *             if an execution timeout or a database access error occurs; this method is called on a closed
     *             <code>PreparedStatement</code> or the SQL statement does not return a <code>ResultSet</code> object
     */
    public static ResultSet executeQuery( final PreparedStatement stmt, ConnectionProvider connProvider,
                                          long executionTimeout )
                            throws SQLException {
        if ( executionTimeout <= 0 ) {
            return stmt.executeQuery();
        }
        try {
            return Executor.getInstance().performSynchronously( new Callable<ResultSet>() {
                @Override
                public ResultSet call()
                                        throws Exception {
                    return stmt.executeQuery();
                }
            }, executionTimeout );
        } catch ( CancellationException e ) {
            stmt.cancel();
            Connection conn = stmt.getConnection();
            String msg = "Database query has been cancelled, because query execution timeout (" + executionTimeout
                         + "[ms]) has been exceeded.";
            try {
                // This is necessary, because cancelled connections appear not to be reusable, so errors occur if a
                // cancelled connection is returned to the pool and re-used (at least on PostgreSQL)
                connProvider.invalidate( conn );
            } catch ( Throwable t ) {
                msg += "Invalidation of connection failed: " + t.getMessage();
            }
            throw new SQLException( msg );
        } catch ( InterruptedException e ) {
            String msg = "Interruption during database query: " + e.getMessage();
            throw new SQLException( msg, e );
        } catch ( SQLException e ) {
            throw e;
        } catch ( Throwable e ) {
            String msg = "Error executing query: " + e.getMessage();
            throw new SQLException( msg, e );
        }
    }

}
