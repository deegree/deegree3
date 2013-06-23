//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.iso.persistence.sql;

import static java.sql.DriverManager.getConnection;
import static org.deegree.commons.jdbc.ConnectionManager.Type.Oracle;
import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.metadata.iso.persistence.ISOPropertyNameMapper;
import org.deegree.metadata.iso.persistence.queryable.Queryable;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.oracle.OracleDialect;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Checks the created sql statements.
 * 
 * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DefaultQueryServiceIT {

    private Connection connection;

    private AbstractWhereBuilder builder;

    private DefaultQueryService queryService;

    @Before
    public void initiQueryService()
                            throws Exception {
        Pair<Type, Connection> typeToConnection = initTypeAndConnection();
        if ( typeToConnection != null ) {
            connection = typeToConnection.second;
            MetadataQuery query = parseQuery();
            SQLDialect dialect = initSqlDialect();
            List<Queryable> queryables = new ArrayList<Queryable>();
            builder = initWhereBuilder( dialect, queryables, query, connection );
            queryService = new DefaultQueryService( dialect, queryables );
        }
    }

    @After
    public void closeConnection()
                            throws SQLException {
        if ( connection != null )
            connection.close();
    }

    @Test
    public void testGetPSBodyShouldNotHaveDuplicatedJoins()
                            throws Exception {
        assumeThat( queryService, notNullValue() );

        StringBuilder sql = new StringBuilder();
        queryService.getPSBody( builder, sql );
        String sqlSnippet = sql.toString();

        assertThat( sqlSnippet, hasNoDuplicatedJoins() );
    }

    @Test
    public void testGetPSBodyShouldHaveBoundAliases()
                            throws Exception {
        assumeThat( queryService, notNullValue() );

        StringBuilder sql = new StringBuilder();
        queryService.getPSBody( builder, sql );
        String sqlSnippet = sql.toString();

        assertThat( sqlSnippet, hasOnlyBoundAliases() );
    }

    private org.hamcrest.Matcher<? super String> hasNoDuplicatedJoins() {
        return new NoDuplicatedJoinsMatcher();
    }

    private org.hamcrest.Matcher<? super String> hasOnlyBoundAliases() {
        return new AliasesBoundMatcher();
    }

    private MetadataQuery parseQuery()
                            throws XMLStreamException, FactoryConfigurationError {
        OperatorFilter filter = parseFilter( "complexFilter_operatesOn.xml" );
        return new MetadataQuery( null, null, filter, null, 1, 1000 );
    }

    private AbstractWhereBuilder initWhereBuilder( SQLDialect dialect, List<Queryable> queryables, MetadataQuery query,
                                                   Connection conn )
                            throws FilterEvaluationException, UnmappableException {
        return dialect.getWhereBuilder( new ISOPropertyNameMapper( dialect, queryables ),
                                        (OperatorFilter) query.getFilter(), query.getSorting(), false );
    }

    private OperatorFilter parseFilter( String filterName )
                            throws XMLStreamException, FactoryConfigurationError {
        InputStream stream = DefaultQueryServiceIT.class.getResourceAsStream( filterName );
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader( stream );
        xmlStreamReader.nextTag();
        return (OperatorFilter) Filter110XMLDecoder.parse( xmlStreamReader );
    }

    private Pair<Type, Connection> initTypeAndConnection()
                            throws IOException, ClassNotFoundException {
        Properties props = new Properties();
        props.load( DefaultQueryServiceIT.class.getResourceAsStream( "/testdb.properties" ) );
        String jdbcUrlOpt = props.getProperty( "jdbcUrl" );
        Type type = detectSqlType( jdbcUrlOpt );
        loadDriver( type );
        try {
            String user = props.getProperty( "user" );
            String password = props.getProperty( "password" );
            Connection connection = getConnection( jdbcUrlOpt, user, password );
            return new Pair<Type, Connection>( type, connection );
        } catch ( SQLException e ) {
            return null;
        }
    }

    private Type detectSqlType( String jdbcUrlOpt ) {
        Type type;
        if ( jdbcUrlOpt.startsWith( "jdbc:oracle:" ) ) {
            type = Oracle;
        } else {
            type = PostgreSQL;
        }
        return type;
    }

    private void loadDriver( Type type )
                            throws ClassNotFoundException {
        if ( type == Oracle ) {
            Class.forName( "oracle.jdbc.driver.OracleDriver" );
        } else {
            Class.forName( "org.postgresql.Driver" );
        }
    }

    private SQLDialect initSqlDialect()
                            throws SQLException, IOException, ClassNotFoundException {
        Pair<Type, Connection> conn = initTypeAndConnection();
        if ( conn.first == Oracle ) {
            return initOracleSqlDialect( conn );
        } else {
            return new PostGISDialect( true );
        }
    }

    private SQLDialect initOracleSqlDialect( Pair<Type, Connection> conn )
                            throws SQLException {
        String schema = null;
        Statement stmt = null;
        ResultSet rs = null;

        // default to 10.0
        int major = 10;
        int minor = 0;
        try {
            stmt = conn.second.createStatement();
            // this function / parameters exists since oracle version 8
            rs = stmt.executeQuery( "SELECT sys_context('USERENV', 'CURRENT_SCHEMA') FROM DUAL" );
            if ( rs.next() )
                schema = rs.getString( 1 );

            JDBCUtils.close( rs );
            // tested with oracle 9, 10, 11
            rs = stmt.executeQuery( "SELECT version FROM product_component_version WHERE product LIKE 'Oracle%'" );

            if ( rs.next() ) {
                Pattern p = Pattern.compile( "^(\\d+)\\.(\\d+)\\." );
                Matcher m = p.matcher( rs.getString( 1 ) );
                if ( m.find() ) {
                    major = Integer.valueOf( m.group( 1 ) );
                    minor = Integer.valueOf( m.group( 2 ) );
                }
            }
        } finally {
            JDBCUtils.close( rs, stmt, conn.second, null );
        }
        return new OracleDialect( schema, major, minor );
    }

}