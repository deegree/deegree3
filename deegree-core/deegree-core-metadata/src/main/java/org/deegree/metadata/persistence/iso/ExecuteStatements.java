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
import org.deegree.metadata.persistence.GenericDatabaseExecution;
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
public class ExecuteStatements implements GenericDatabaseExecution {

    private static final Logger LOG = getLogger( ExecuteStatements.class );

    private String databaseTable = PostGISMappingsISODC.DatabaseTables.datasets.name();

    private String id = PostGISMappingsISODC.CommonColumnNames.id.name();

    private String rf = PostGISMappingsISODC.CommonColumnNames.recordfull.name();

    private final Type dbType;

    public ExecuteStatements( Type dbType ) {
        this.dbType = dbType;
        if ( dbType == PostgreSQL ) {
            databaseTable = PostGISMappingsISODC.DatabaseTables.datasets.name();
            id = PostGISMappingsISODC.CommonColumnNames.id.name();
            rf = PostGISMappingsISODC.CommonColumnNames.recordfull.name();
        }
        if ( dbType == MSSQL ) {
            databaseTable = MSSQLMappingsISODC.DatabaseTables.datasets.name();
            id = MSSQLMappingsISODC.CommonColumnNames.id.name();
            rf = MSSQLMappingsISODC.CommonColumnNames.recordfull.name();
        }
    }

    @Override
    public int executeDeleteStatement( Connection connection, AbstractWhereBuilder builder )
                            throws MetadataStoreException {

        LOG.debug( Messages.getMessage( "INFO_EXEC", "delete-statement" ) );
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<Integer> deletableDatasets;
        try {

            StringBuilder header = getPreparedStatementDatasetIDs( null, true, builder );
            getPSBody( null, connection, builder, header );
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
            stringBuilder.append( PostGISMappingsISODC.DatabaseTables.datasets.name() );
            stringBuilder.append( " WHERE " ).append( PostGISMappingsISODC.CommonColumnNames.id.name() );
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
                    preparedStatement.executeUpdate();

                }
            }

        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", preparedStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( rs, preparedStatement, null, LOG );

        }

        return deletableDatasets.size();

    }

    private StringBuilder getPreparedStatementDatasetIDs( MetadataQuery query, boolean setDelete,
                                                          AbstractWhereBuilder builder ) {

        StringBuilder getDatasetIDs = new StringBuilder( 300 );
        String orderByclause = null;
        if ( builder.getOrderBy() != null ) {
            int length = builder.getOrderBy().getSQL().length();
            orderByclause = builder.getOrderBy().getSQL().toString().substring( 0, length - 4 );
        }
        String rootTableAlias = builder.getAliasManager().getRootTableAlias();
        getDatasetIDs.append( "SELECT " );

        if ( setDelete ) {
            getDatasetIDs.append( " DISTINCT " );
            getDatasetIDs.append( rootTableAlias );
            getDatasetIDs.append( '.' );
            getDatasetIDs.append( id );
            if ( orderByclause != null ) {
                getDatasetIDs.append( ',' );
                getDatasetIDs.append( orderByclause );
            }
        } else {
            getDatasetIDs.append( " DISTINCT " );
            getDatasetIDs.append( rootTableAlias );
            getDatasetIDs.append( '.' );
            getDatasetIDs.append( rf );
            if ( orderByclause != null ) {
                getDatasetIDs.append( ',' );
                getDatasetIDs.append( orderByclause );
            }
        }

        return getDatasetIDs;

    }

    private void getPSBody( MetadataQuery query, Connection connection, AbstractWhereBuilder builder,
                            StringBuilder getDatasetIDs ) {

        String rootTableAlias = builder.getAliasManager().getRootTableAlias();
        getDatasetIDs.append( " FROM " );
        getDatasetIDs.append( databaseTable );
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

    @Override
    public PreparedStatement executeGetRecords( MetadataQuery query, AbstractWhereBuilder builder, Connection conn )
                            throws MetadataStoreException {
        PreparedStatement preparedStatement = null;
        java.util.Date date = null;
        try {

            LOG.debug( Messages.getMessage( "INFO_EXEC", "getRecords-statement" ) );

            StringBuilder header = getPreparedStatementDatasetIDs( query, false, builder );

            if ( query != null && query.getStartPosition() != 1 && dbType == MSSQL ) {
                String oldHeader = header.toString();
                header = header.append( " from (" ).append( oldHeader );
                header.append( ", ROW_NUMBER() OVER (ORDER BY ID) as rownum" );
            }

            getPSBody( query, conn, builder, header );
            if ( builder.getOrderBy() != null ) {
                header.append( " ORDER BY " );
                header.append( builder.getOrderBy().getSQL() );
            }
            if ( query != null && query.getStartPosition() != 1 && dbType == PostgreSQL ) {
                header.append( " OFFSET " ).append( Integer.toString( query.getStartPosition() - 1 ) );
            }
            if ( query != null && query.getStartPosition() != 1 && dbType == MSSQL ) {
                header.append( ") as X1 where X1.rownum > " );
                header.append( query.getStartPosition() - 1 );
            }
            System.out.println( header );
            preparedStatement = conn.prepareStatement( header.toString() );

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

    public PreparedStatement executeCounting( MetadataQuery query, AbstractWhereBuilder builder, Connection conn )
                            throws MetadataStoreException {
        PreparedStatement preparedStatement = null;
        java.util.Date date = null;
        try {

            LOG.info( "new Counting" );
            StringBuilder getDatasetIDs = new StringBuilder();
            getDatasetIDs.append( "SELECT " );
            getDatasetIDs.append( "COUNT( DISTINCT(" );
            getDatasetIDs.append( rf );
            getDatasetIDs.append( "))" );
            getPSBody( query, conn, builder, getDatasetIDs );
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
