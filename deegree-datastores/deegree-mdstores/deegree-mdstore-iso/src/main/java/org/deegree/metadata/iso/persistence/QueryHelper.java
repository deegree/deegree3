//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/QueryHelper.java $
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
package org.deegree.metadata.iso.persistence;

import static org.deegree.commons.jdbc.ConnectionManager.Type.MSSQL;
import static org.deegree.commons.jdbc.ConnectionManager.Type.PostgreSQL;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.StringUtils;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.expression.SQLArgument;
import org.deegree.filter.sql.mssql.MSSQLWhereBuilder;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.postgis.PostGISWhereBuilder;
import org.slf4j.Logger;

/**
 * Executes statements that does the interaction with the underlying database. This is a PostGRES implementation.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31272 $, $Date: 2011-07-13 23:10:35 +0200 (Mi, 13. Jul 2011) $
 */
class QueryHelper extends SqlHelper {

    private static final Logger LOG = getLogger( QueryHelper.class );

    /** Used to limit the fetch size for SELECT statements that potentially return a lot of rows. */
    public static final int DEFAULT_FETCH_SIZE = 100;

    QueryHelper( Type connectionType ) {
        super( connectionType );
    }

    ISOMetadataResultSet execute( MetadataQuery query, Connection conn )
                            throws MetadataStoreException {
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            AbstractWhereBuilder builder = getWhereBuilder( query, conn );

            StringBuilder idSelect = getPreparedStatementDatasetIDs( builder );

            if ( query != null && query.getStartPosition() != 1 && connectionType == MSSQL ) {
                String oldHeader = idSelect.toString();
                idSelect = idSelect.append( " from (" ).append( oldHeader );
                idSelect.append( ", ROW_NUMBER() OVER (ORDER BY X1.ID) as rownum" );
            }

            getPSBody( builder, idSelect );
            if ( builder.getOrderBy() != null ) {
                idSelect.append( " ORDER BY " );
                idSelect.append( builder.getOrderBy().getSQL() );
            }
            if ( query != null && query.getStartPosition() != 1 && connectionType == PostgreSQL ) {
                idSelect.append( " OFFSET " ).append( Integer.toString( query.getStartPosition() - 1 ) );
            }
            if ( query != null && query.getStartPosition() != 1 && connectionType == MSSQL ) {
                idSelect.append( ") as X1 where X1.rownum > " );
                idSelect.append( query.getStartPosition() - 1 );
            }
            // take a look in the wiki before changing this!
            if ( connectionType == PostgreSQL && query != null && query.getMaxRecords() > -1 ) {
                idSelect.append( " LIMIT " ).append( query.getMaxRecords() );
            }

            StringBuilder outerSelect = new StringBuilder( "SELECT " );
            outerSelect.append( recordColumn );
            outerSelect.append( " FROM " );
            outerSelect.append( ISOPropertyNameMapper.DatabaseTables.idxtb_main );
            outerSelect.append( " A INNER JOIN (" );
            outerSelect.append( idSelect );
            outerSelect.append( ") B ON A.id=B.id" );

            // append sort criteria in the outer again, because IN statement looses ordering from inner ORDER BY
            if ( builder.getOrderBy() != null ) {
                outerSelect.append( " ORDER BY " );

                // check that all sort columns belong to root table
                String sortCols = builder.getOrderBy().getSQL().toString();
                String rootTableQualifier = builder.getAliasManager().getRootTableAlias() + ".";
                int columnCount = StringUtils.count( sortCols, "," ) + 1;
                int rootAliasCount = StringUtils.count( sortCols, rootTableQualifier );

                if ( rootAliasCount < columnCount ) {
                    String msg = "Sorting based on properties not stored in the root table is currently not supported.";
                    throw new MetadataStoreException( msg );
                }

                String colRegEx = builder.getAliasManager().getRootTableAlias() + ".\\S+";
                for ( int i = 1; i <= columnCount; i++ ) {
                    sortCols = sortCols.replaceFirst( colRegEx, "crit" + i );
                }
                outerSelect.append( sortCols );
            }

            preparedStatement = conn.prepareStatement( outerSelect.toString() );

            int i = 1;
            if ( builder.getWhere() != null ) {
                for ( SQLArgument o : builder.getWhere().getArguments() ) {
                    o.setArgument( preparedStatement, i++ );
                }
            }

            if ( builder.getOrderBy() != null ) {
                for ( SQLArgument o : builder.getOrderBy().getArguments() ) {
                    o.setArgument( preparedStatement, i++ );
                }
            }
            LOG.debug( preparedStatement.toString() );

            preparedStatement.setFetchSize( DEFAULT_FETCH_SIZE );
            rs = preparedStatement.executeQuery();
            return new ISOMetadataResultSet( rs, conn, preparedStatement );
        } catch ( SQLException e ) {
            JDBCUtils.close( rs, preparedStatement, null, LOG );
            String msg = Messages.getMessage( "ERROR_SQL", preparedStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } catch ( Throwable t ) {
            JDBCUtils.close( rs, preparedStatement, null, LOG );
            String msg = Messages.getMessage( "ERROR_REQUEST_TYPE", ResultType.results.name(), t.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
    }

    int executeCounting( MetadataQuery query, Connection conn )
                            throws MetadataStoreException, FilterEvaluationException, UnmappableException {
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            AbstractWhereBuilder builder = getWhereBuilder( query, conn );
            LOG.debug( "new Counting" );
            StringBuilder getDatasetIDs = new StringBuilder();
            getDatasetIDs.append( "SELECT " );
            getDatasetIDs.append( "COUNT( DISTINCT(" );
            getDatasetIDs.append( builder.getAliasManager().getRootTableAlias() );
            getDatasetIDs.append( "." );
            getDatasetIDs.append( idColumn );
            getDatasetIDs.append( "))" );
            getPSBody( builder, getDatasetIDs );
            preparedStatement = conn.prepareStatement( getDatasetIDs.toString() );
            int i = 1;
            if ( builder.getWhere() != null ) {
                for ( SQLArgument o : builder.getWhere().getArguments() ) {
                    o.setArgument( preparedStatement, i++ );
                }
            }
            LOG.debug( preparedStatement.toString() );
            rs = preparedStatement.executeQuery();
            rs.next();
            LOG.debug( "rs for rowCount: " + rs.getInt( 1 ) );
            return rs.getInt( 1 );
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "ERROR_SQL", preparedStatement.toString(), e.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        } finally {
            JDBCUtils.close( rs, preparedStatement, conn, LOG );
        }
    }

    ISOMetadataResultSet executeGetRecordById( List<String> idList, Connection conn )
                            throws MetadataStoreException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            int size = idList.size();

            StringBuilder select = new StringBuilder();
            select.append( "SELECT " ).append( recordColumn );
            select.append( " FROM " ).append( mainTable );
            select.append( " WHERE " );
            for ( int iter = 0; iter < size; iter++ ) {
                select.append( fileIdColumn ).append( " = ? " );
                if ( iter < size - 1 ) {
                    select.append( " OR " );
                }
            }

            stmt = conn.prepareStatement( select.toString() );
            stmt.setFetchSize( DEFAULT_FETCH_SIZE );
            LOG.debug( "select RecordById statement: " + stmt );

            int i = 1;
            for ( String identifier : idList ) {
                stmt.setString( i, identifier );
                LOG.debug( "identifier: " + identifier );
                LOG.debug( "" + stmt );
                i++;
            }
            rs = stmt.executeQuery();
        } catch ( Throwable t ) {
            JDBCUtils.close( rs, stmt, conn, LOG );
            String msg = Messages.getMessage( "ERROR_REQUEST_TYPE", ResultType.results.name(), t.getMessage() );
            LOG.debug( msg );
            throw new MetadataStoreException( msg );
        }
        return new ISOMetadataResultSet( rs, conn, stmt );
    }

    private AbstractWhereBuilder getWhereBuilder( MetadataQuery query, Connection conn )
                            throws FilterEvaluationException, UnmappableException {
        if ( connectionType == PostgreSQL ) {
            // TODO only do this once, it's expensive!
            boolean useLegacyPredicates = JDBCUtils.useLegayPostGISPredicates( conn, LOG );
            ISOPropertyNameMapper mapping = new ISOPropertyNameMapper( connectionType, useLegacyPredicates );
            return new PostGISWhereBuilder( mapping, (OperatorFilter) query.getFilter(), query.getSorting(), false,
                                            useLegacyPredicates );
        }
        if ( connectionType == Type.MSSQL ) {
            ISOPropertyNameMapper mapping = new ISOPropertyNameMapper( connectionType, false );
            return new MSSQLWhereBuilder( mapping, (OperatorFilter) query.getFilter(), query.getSorting(), false );
        }
        return null;
    }
}