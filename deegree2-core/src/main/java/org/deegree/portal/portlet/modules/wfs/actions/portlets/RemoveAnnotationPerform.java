//$Header: $
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

package org.deegree.portal.portlet.modules.wfs.actions.portlets;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.portal.Portlet;
import org.apache.turbine.util.RunData;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.portal.PortalException;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;

/**
 * Removes a number of Features (listed by their ID).
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class RemoveAnnotationPerform extends IGeoPortalPortletPerform {

    private static final ILogger LOG = LoggerFactory.getLogger( RemoveAnnotationPerform.class );

    /**
     *
     * Creates an instance of a RemoveAnnotationPerform.
     *
     * @param request
     * @param portlet
     * @param servletContext
     */
    public RemoveAnnotationPerform( HttpServletRequest request, Portlet portlet, ServletContext servletContext ) {
        super( request, portlet, servletContext );
    }

    /**
     * Builds up the portlet.
     *
     * @param data
     * @throws PortalException
     */
    public void buildNormalContext( RunData data )
                            throws PortalException {
        super.buildNormalContext();

        if ( getInitParam( "driver" ) != null ) {

            LOG.logDebug( "Build dataset table for annotations objects." );

            Connection con = createConnection();
            String sql = getInitParam( "SQLDisplayStatement" );
            LOG.logDebug( "Clean DB Object SQL: " + sql );
            sql = createSelectionStatement( data, sql );

            List<List<Object>> result;

            try {
                result = createTable( con, sql );
                LOG.logDebug( "Built dataset table for annotations objects: " + result );

                con.close();
            } catch ( SQLException e ) {
                e.printStackTrace();
                throw new PortalException( e.getMessage() );
            }
            request.setAttribute( "DB_OBJECTS", result );
        }

        request.setAttribute( "TITLE", getInitParam( "title" ) );

    }

    /**
     * Creates the SQL select statement. The sql script is taken from the initial parameter
     * 'SQLDisplayStatement'. Username is automotically inserted, if there is a '$USERNAME'
     * placeholder.
     *
     * @param data
     *            the RunData
     * @param sql
     *            the original sql script
     * @return the changed sql script
     */
    private String createSelectionStatement( RunData data, String sql ) {
        String username = data.getUser().getUserName();
        sql = sql.replaceAll( "'", "\"" );
        sql = sql.replace( "$USERNAME", "'" + username + "'" );
        LOG.logDebug( "Populated DB Object SQL : " + sql );
        return sql;
    }

    /**
     * Creates a pseudo-table of results. The results are held in a list of lists. The first list
     * has the strings describing the column names (or labels, if the use of 'as' - like in selece
     * id as 'Identifier' has been made.
     *
     * @param con
     *            the connection
     * @param sql
     *            the sql statement
     * @return a List containing Lists of results
     * @throws SQLException
     */
    private List<List<Object>> createTable( Connection con, String sql )
                            throws SQLException {

        Statement stm = con.createStatement();
        stm.execute( sql );
        ResultSet rSet = stm.getResultSet();

        List<List<Object>> rows = new ArrayList<List<Object>>();

        int colCount = rSet.getMetaData().getColumnCount();
        LOG.logDebug( "Creating dataset table for annotations objects: " + colCount );

        while ( rSet.next() ) {
            List<Object> cols = new ArrayList<Object>();

            for ( int i = 0; i < colCount; i++ ) {
                cols.add( rSet.getObject( i + 1 ) );
            }
            rows.add( cols );
        }

        if ( rows.size() > 0 ) {
            // if anything found, put headers in the list
            List<Object> cols = new ArrayList<Object>( colCount );
            for ( int i = 0; i < colCount; i++ ) {
                cols.add( rSet.getMetaData().getColumnLabel( i + 1 ) );
            }
            rows.add( 0, cols );
        }
        stm.close();
        return rows;
    }

    /**
     * Executes a delete statement.
     *
     * @param data
     *            the RunData
     * @throws PortalException
     *             if something evil happened
     */
    public void doDeletetransaction( RunData data )
                            throws PortalException {

        if ( getInitParam( "driver" ) != null ) {
            // let us hope and say, there's a driver
            Connection con = createConnection();
            try {
                String s = parameter.get( "OBJECTID" );
                String[] featureIds = StringTools.toArray( s, ",;", true );
                if ( featureIds == null ) {
                    LOG.logDebug( "No 'objectId' parameter in the request. Skipping..." );
                    return;
                }
                for ( String id : featureIds ) {
                    String sql = createDeleteSQL( data, id );
                    System.out.println( sql );
                    deleteObject( con, sql );
                }

            } catch ( SQLException e ) {
                e.printStackTrace();
                throw new PortalException( e.getMessage() );
            } finally {
                try {
                    con.close();
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates a delete statement based on the initial parameter 'SQLDeleteStatement' on the RunData
     * containing the user name and on the id of the data object to be deleted.
     *
     * @param data
     *            the RunData object
     * @param id
     *            the id of the dataset
     * @return a sql with proper user name, and data set id
     */
    private String createDeleteSQL( RunData data, String id ) {

        String sql = getInitParam( "SQLDeleteStatement" );
        if ( data.getUser() == null ) {
            throw new IllegalArgumentException( "RunData object has null user." );
        }

        String username = data.getUser().getUserName();
        LOG.logDebug( "Preparing deletion of object id '" + id + "' for user: '" + username + "'" );

        sql = sql.replace( "$USERNAME", "'" + username + "'" );
        sql = sql.replace( "$OBJECT_ID", "'" + id + "'" );

        LOG.logDebug( "Create SQL for deleting: " + sql );

        return sql;
    }

    /**
     *
     * @param con
     * @param sql
     * @throws SQLException
     */
    private void deleteObject( Connection con, String sql )
                            throws SQLException {
        Statement stmt = con.createStatement();
        stmt.execute( sql );
        con.commit();
    }

    /**
     *
     * @return the Connection
     * @throws PortalException
     */
    private Connection createConnection()
                            throws PortalException {

        Connection con = null;

        String url = getInitParam( "url" );
        String driver = getInitParam( "driver" );
        String user = getInitParam( "user" );
        String password = getInitParam( "password" );

        con = initConnection( url, driver, user, password );

        return con;
    }

    /**
     *
     * @param url
     * @param driver
     * @param user
     * @param password
     * @return the Connection
     * @throws PortalException
     */
    private Connection initConnection( String url, String driver, String user, String password )
                            throws PortalException {
        LOG.logDebug( "connecting database for insert ... " );

        Connection con = null;

        try {
            Driver drv = (Driver) Class.forName( driver ).newInstance();
            DriverManager.registerDriver( drv );
            LOG.logDebug( "initializing connection with " + drv + " " + url + " " + user + " " + password );

            con = DriverManager.getConnection( url, user, password );
        } catch ( SQLException e ) {
            LOG.logError( "could not establish database connection: " + url, e );
            throw new PortalException( "could not establish database connection: " + url, e );
        } catch ( Exception e ) {
            LOG.logError( "could not initialize driver class: " + driver, e );
            throw new PortalException( "could not initialize driver class: " + driver, e );
        }
        return con;
    }
}
