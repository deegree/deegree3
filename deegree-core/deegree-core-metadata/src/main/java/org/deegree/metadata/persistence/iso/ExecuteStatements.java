//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.persistence.iso;

import static org.deegree.commons.jdbc.ConnectionManager.Type.MSSQL;
import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.Join;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;

/**
 * Executes statements that does the interaction with the underlying database. This is a PostGRES implementation.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecuteStatements {

    private static final Logger LOG = getLogger( ExecuteStatements.class );

    private String mainTable;

    private String id = PostGISMappingsISODC.CommonColumnNames.id.name();

    private String rf = PostGISMappingsISODC.CommonColumnNames.recordfull.name();

    private final Type dbType;

    public ExecuteStatements( Type dbType ) {
        this.dbType = dbType;
        if ( dbType == PostgreSQL ) {
            mainTable = PostGISMappingsISODC.DatabaseTables.idxtb_main.name();
            id = PostGISMappingsISODC.CommonColumnNames.id.name();
            rf = PostGISMappingsISODC.CommonColumnNames.recordfull.name();
        }
        if ( dbType == MSSQL ) {
            mainTable = MSSQLMappingsISODC.DatabaseTables.idxtb_main.name();
            id = MSSQLMappingsISODC.CommonColumnNames.id.name();
            rf = MSSQLMappingsISODC.CommonColumnNames.recordfull.name();
        }
    }

    public int executeDeleteStatement( Connection connection, AbstractWhereBuilder builder )
                            throws MetadataStoreException {
        LOG.debug( Messages.getMessage( "INFO_EXEC", "delete-statement" ) );
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<Integer> deletableDatasets;
        int deleted = 0;
        try {
            StringBuilder header = getPreparedStatementDatasetIDs( builder );
            getPSBody( builder, header );
            preparedStatement = connection.prepareStatement( header.toString() );
            int i = 1;
            if ( builder.getWhere() != null ) {
                for ( SQLLiteral o : builder.getWhere().getLiterals() ) {
                    preparedStatement.setObject( i++, o.getValue() );
                }
            }
            if ( builder.getOrderBy() != null ) {
                for ( SQLLiteral o : builder.getOrderBy().getLiterals() ) {
                    preparedStatement.setObject( i++, o.getValue() );
                }
            }
            LOG.debug( Messages.getMessage( "INFO_TA_DELETE_FIND", preparedStatement.toString() ) );

            rs = preparedStatement.executeQuery();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append( "DELETE FROM " );
            stringBuilder.append( mainTable );
            stringBuilder.append( " WHERE " ).append( id );
            stringBuilder.append( " = ?" );

            deletableDatasets = new ArrayList<Integer>();
            if ( rs != null ) {
                while ( rs.next() ) {
                    deletableDatasets.add( rs.getInt( 1 ) );
                }
                rs.close();
                for ( int d : deletableDatasets ) {
                    preparedStatement = connection.prepareStatement( stringBuilder.toString() );
                    preparedStatement.setInt( 1, d );

                    LOG.debug( Messages.getMessage( "INFO_TA_DELETE_DEL", preparedStatement.toString() ) );
                    deleted = deleted + preparedStatement.executeUpdate();
                }
            }
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", preparedStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( rs, preparedStatement, null, LOG );
        }
        return deleted;
    }

    private StringBuilder getPreparedStatementDatasetIDs( AbstractWhereBuilder builder ) {

        StringBuilder getDatasetIDs = new StringBuilder( 300 );
        String orderByclause = null;
        if ( builder.getOrderBy() != null ) {
            int length = builder.getOrderBy().getSQL().length();
            orderByclause = builder.getOrderBy().getSQL().toString().substring( 0, length - 4 );
        }
        String rootTableAlias = builder.getAliasManager().getRootTableAlias();
        getDatasetIDs.append( "SELECT DISTINCT " );
        getDatasetIDs.append( rootTableAlias );
        getDatasetIDs.append( '.' );
        getDatasetIDs.append( id );
        if ( orderByclause != null ) {
            getDatasetIDs.append( ',' );
            getDatasetIDs.append( orderByclause );
        }
        return getDatasetIDs;
    }

    private void getPSBody( AbstractWhereBuilder builder, StringBuilder getDatasetIDs ) {

        String rootTableAlias = builder.getAliasManager().getRootTableAlias();
        getDatasetIDs.append( " FROM " );
        getDatasetIDs.append( mainTable );
        getDatasetIDs.append( " " );
        getDatasetIDs.append( rootTableAlias );

        for ( PropertyNameMapping mappedPropName : builder.getMappedPropertyNames() ) {
            String currentAlias = rootTableAlias;
            for ( Join join : mappedPropName.getJoins() ) {
                DBField from = join.getFrom();
                DBField to = join.getTo();
                getDatasetIDs.append( " LEFT OUTER JOIN " );
                getDatasetIDs.append( to.getTable() );
                getDatasetIDs.append( " AS " );
                getDatasetIDs.append( to.getAlias() );
                getDatasetIDs.append( " ON " );
                getDatasetIDs.append( currentAlias );
                getDatasetIDs.append( "." );
                getDatasetIDs.append( from.getColumn() );
                getDatasetIDs.append( "=" );
                currentAlias = to.getAlias();
                getDatasetIDs.append( currentAlias );
                getDatasetIDs.append( "." );
                getDatasetIDs.append( to.getColumn() );
            }
        }

        if ( builder.getWhere() != null ) {
            getDatasetIDs.append( " WHERE " );
            getDatasetIDs.append( builder.getWhere().getSQL() );
        }

    }

    public PreparedStatement executeGetRecords( MetadataQuery query, AbstractWhereBuilder builder, Connection conn )
                            throws MetadataStoreException {
        PreparedStatement preparedStatement = null;
        java.util.Date date = null;
        try {

            LOG.debug( Messages.getMessage( "INFO_EXEC", "getRecords-statement" ) );

            StringBuilder sql = getPreparedStatementDatasetIDs( builder );

            if ( query != null && query.getStartPosition() != 1 && dbType == MSSQL ) {
                String oldHeader = sql.toString();
                sql = sql.append( " from (" ).append( oldHeader );
                sql.append( ", ROW_NUMBER() OVER (ORDER BY X1.ID) as rownum" );
            }

            getPSBody( builder, sql );
            if ( builder.getOrderBy() != null ) {
                sql.append( " ORDER BY " );
                sql.append( builder.getOrderBy().getSQL() );
            }
            if ( query != null && query.getStartPosition() != 1 && dbType == PostgreSQL ) {
                sql.append( " OFFSET " ).append( Integer.toString( query.getStartPosition() - 1 ) );
            }
            if ( query != null && query.getStartPosition() != 1 && dbType == MSSQL ) {
                sql.append( ") as X1 where X1.rownum > " );
                sql.append( query.getStartPosition() - 1 );
            }
            // take a look in the wiki before changing this! 
            if ( dbType == PostgreSQL && query != null ) {
                sql.append( " LIMIT " ).append( query.getMaxRecords() );
            }
            
            StringBuilder innerSelect = new StringBuilder( "SELECT in1.id FROM (" );
            innerSelect.append( sql );
            innerSelect.append( " ) as in1" );
            
            StringBuilder outerSelect = new StringBuilder( "SELECT " );
            outerSelect.append( rf );
            outerSelect.append( " FROM " );
            if ( dbType == PostgreSQL ) {
                outerSelect.append( PostGISMappingsISODC.DatabaseTables.idxtb_main );
            } else if ( dbType == MSSQL ) {
                outerSelect.append( MSSQLMappingsISODC.DatabaseTables.idxtb_main );
            } else {
                throw new IllegalArgumentException();
            }
            outerSelect.append( " WHERE " );
            outerSelect.append( id );
            outerSelect.append( " IN (" );
            outerSelect.append( innerSelect );
            outerSelect.append( ")" );

            preparedStatement = conn.prepareStatement( outerSelect.toString() );

            int i = 1;
            if ( builder.getWhere() != null ) {
                for ( SQLLiteral o : builder.getWhere().getLiterals() ) {
                    if ( o.getSQLType() == Types.TIMESTAMP ) {
                        date = DateUtils.parseISO8601Date( o.getValue().toString() );
                        Timestamp d = new Timestamp( date.getTime() );
                        preparedStatement.setTimestamp( i++, d );
                    } else if ( o.getSQLType() == Types.BOOLEAN ) {
                        String bool = o.getValue().toString();
                        boolean b = false;
                        if ( bool.equals( "true" ) ) {
                            b = true;
                        }
                        preparedStatement.setBoolean( i++, b );
                    } else {
                        preparedStatement.setObject( i++, o.getValue() );
                    }
                }
            }

            if ( builder.getOrderBy() != null ) {
                for ( SQLLiteral o : builder.getOrderBy().getLiterals() ) {
                    preparedStatement.setObject( i++, o.getValue() );
                }
            }
System.out.println(preparedStatement.toString());
            LOG.debug( preparedStatement.toString() );
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", preparedStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } catch ( ParseException e ) {
            String msg = Messages.getMessage( "ERROR_PARSING", date, e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
        return preparedStatement;

    }

    public PreparedStatement executeCounting( AbstractWhereBuilder builder, Connection conn )
                            throws MetadataStoreException {
        PreparedStatement preparedStatement = null;
        java.util.Date date = null;
        try {

            LOG.debug( "new Counting" );
            StringBuilder getDatasetIDs = new StringBuilder();
            getDatasetIDs.append( "SELECT " );
            getDatasetIDs.append( "COUNT( DISTINCT(" );
            getDatasetIDs.append( builder.getAliasManager().getRootTableAlias() );
            getDatasetIDs.append( "." );
            getDatasetIDs.append( id );
            getDatasetIDs.append( "))" );
            getPSBody( builder, getDatasetIDs );
            preparedStatement = conn.prepareStatement( getDatasetIDs.toString() );
            int i = 1;
            if ( builder.getWhere() != null ) {
                for ( SQLLiteral o : builder.getWhere().getLiterals() ) {
                    if ( o.getSQLType() == Types.TIMESTAMP ) {
                        date = DateUtils.parseISO8601Date( o.getValue().toString() );
                        Timestamp d = new Timestamp( date.getTime() );
                        preparedStatement.setTimestamp( i++, d );
                    } else if ( o.getSQLType() == Types.BOOLEAN ) {
                        String bool = o.getValue().toString();
                        boolean b = false;
                        if ( bool.equals( "true" ) ) {
                            b = true;
                        }
                        preparedStatement.setBoolean( i++, b );
                    } else {
                        preparedStatement.setObject( i++, o.getValue() );
                    }
                }
            }
            LOG.debug( preparedStatement.toString() );
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", preparedStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } catch ( ParseException e ) {
            String msg = Messages.getMessage( "ERROR_PARSING", date, e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
        return preparedStatement;
    }

}
