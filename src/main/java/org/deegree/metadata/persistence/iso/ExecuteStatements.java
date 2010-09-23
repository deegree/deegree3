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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.Join;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso.generating.BuildMetadataXMLRepresentation;
import org.deegree.metadata.persistence.iso.generating.GenerateQueryableProperties;
import org.deegree.metadata.persistence.iso.parsing.ParsedProfileElement;
import org.slf4j.Logger;

/**
 * The execution of the actions affected by the transaction operation against the database.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecuteStatements {

    private static final Logger LOG = getLogger( ExecuteStatements.class );

    private GenerateQueryableProperties generateQP;

    private BuildMetadataXMLRepresentation buildRecXML;

    private static final String databaseTable = PostGISMappingsISODC.DatabaseTables.datasets.name();

    private static final String qp_identifier = PostGISMappingsISODC.DatabaseTables.qp_identifier.name();

    private static final String id = PostGISMappingsISODC.CommonColumnNames.id.name();

    private static final String fk_datasets = PostGISMappingsISODC.CommonColumnNames.fk_datasets.name();

    private static final String identifier = PostGISMappingsISODC.CommonColumnNames.identifier.name();

    /**
     * This method executes the statement for INSERT datasets
     * 
     * @param isDC
     *            true, if a Dublin Core record should be inserted <br>
     *            <div style="text-indent:38px;">false, if an ISO record should be inserted</div>
     * @param connection
     * @param parsedElement
     *            {@link ParsedProfileElement}
     * @throws IOException
     * @throws MetadataStoreException
     */
    public String executeInsertStatement( boolean isDC, Connection connection, ParsedProfileElement parsedElement )
                            throws IOException, MetadataStoreException {
        generateQP = new GenerateQueryableProperties();
        buildRecXML = new BuildMetadataXMLRepresentation();

        boolean isUpdate = false;
        String identifier = null;
        int operatesOnId = generateQP.generateMainDatabaseDataset( connection, parsedElement );

        if ( isDC == true ) {
            identifier = buildRecXML.generateDC( connection, operatesOnId, parsedElement );
        } else {
            identifier = buildRecXML.generateISO( connection, operatesOnId, parsedElement );

        }
        generateQP.executeQueryableProperties( isUpdate, connection, operatesOnId, parsedElement );
        return identifier;

    }

    /**
     * 
     * @param connection
     * @param builder
     * @param formatNumber
     * @return the number of deleted metadata.
     * @throws MetadataStoreException
     */
    public int executeDeleteStatement( Connection connection, PostGISWhereBuilder builder, int formatNumber )
                            throws MetadataStoreException {

        StringBuilder getDatasetIDs = new StringBuilder( 300 );
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<Integer> deletableDatasets;
        try {

            LOG.debug( "wherebuilder: " + builder );

            String rootTableAlias = builder.getAliasManager().getRootTableAlias();
            String blobTableAlias = builder.getAliasManager().generateNew();

            getDatasetIDs.append( "SELECT " );

            getDatasetIDs.append( rootTableAlias );
            getDatasetIDs.append( '.' );
            getDatasetIDs.append( id );
            // getDatasetIDs.append( ',' );
            // getDatasetIDs.append( blobTableAlias );
            // getDatasetIDs.append( '.' );
            // getDatasetIDs.append( PostGISMappingsISODC.CommonColumnNames.data.name() );

            getDatasetIDs.append( " FROM " );
            getDatasetIDs.append( PostGISMappingsISODC.DatabaseTables.datasets.name() );
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

            getDatasetIDs.append( " LEFT OUTER JOIN " );
            // TODO remove hard coded
            getDatasetIDs.append( "recordbrief" );
            getDatasetIDs.append( " AS " );
            getDatasetIDs.append( blobTableAlias );
            getDatasetIDs.append( " ON " );
            getDatasetIDs.append( rootTableAlias );
            getDatasetIDs.append( "." );
            getDatasetIDs.append( id );
            getDatasetIDs.append( "=" );
            getDatasetIDs.append( blobTableAlias );
            getDatasetIDs.append( "." );
            getDatasetIDs.append( fk_datasets );

            getDatasetIDs.append( " WHERE " );
            getDatasetIDs.append( blobTableAlias );
            getDatasetIDs.append( '.' );
            getDatasetIDs.append( PostGISMappingsISODC.CommonColumnNames.format.name() );
            getDatasetIDs.append( "=?" );

            if ( builder.getWhere() != null ) {
                getDatasetIDs.append( " AND " );
                getDatasetIDs.append( builder.getWhere().getSQL() );
            }

            preparedStatement = connection.prepareStatement( getDatasetIDs.toString() );

            int i = 1;
            preparedStatement.setInt( i++, formatNumber );

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

            LOG.debug( preparedStatement.toString() );

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
                    preparedStatement.executeUpdate();

                }
            }

        } catch ( SQLException e ) {
            JDBCUtils.close( rs, preparedStatement, connection, LOG );

            LOG.debug( "Error while generating the SELECT statement: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while generating the SELECT statement: {}", e );
        } finally {
            JDBCUtils.close( rs, preparedStatement, null, LOG );

        }

        return deletableDatasets.size();

    }

    /**
     * This method executes the statement for updating the queryable- and returnable properties of one specific record.
     * 
     * @param connection
     * @param updatedIds
     * @param parsedElement
     *            {@link ParsedProfileElement}
     */
    public void executeUpdateStatement( Connection connection, List<Integer> updatedIds,
                                        ParsedProfileElement parsedElement ) {

        boolean isUpdate = true;
        generateQP = new GenerateQueryableProperties();
        buildRecXML = new BuildMetadataXMLRepresentation();

        PreparedStatement stm = null;
        Statement stmt = null;
        ResultSet rs = null;

        StringBuilder sqlStatementUpdate = new StringBuilder( 500 );

        int requestedId = 0;
        String modifiedAttribute = "null";

        try {
            stmt = connection.createStatement();
            for ( String identifierString : parsedElement.getQueryableProperties().getIdentifier() ) {

                sqlStatementUpdate.append( "SELECT " ).append( databaseTable ).append( '.' );
                sqlStatementUpdate.append( id ).append( " FROM " );
                sqlStatementUpdate.append( databaseTable ).append( ',' ).append( qp_identifier ).append( " WHERE " );
                sqlStatementUpdate.append( databaseTable ).append( '.' ).append( id );
                sqlStatementUpdate.append( '=' ).append( qp_identifier ).append( '.' ).append( fk_datasets );
                sqlStatementUpdate.append( " AND " ).append( qp_identifier ).append( '.' ).append( identifier ).append(
                                                                                                                        " = ?" );
                LOG.debug( sqlStatementUpdate.toString() );

                stm = connection.prepareStatement( sqlStatementUpdate.toString() );
                stm.setObject( 1, identifierString );
                rs = stm.executeQuery();
                sqlStatementUpdate.setLength( 0 );

                while ( rs.next() ) {
                    requestedId = rs.getInt( 1 );
                    LOG.debug( "resultSet: " + rs.getInt( 1 ) );
                }

                if ( requestedId != 0 ) {

                    if ( !parsedElement.getQueryableProperties().getModified().equals( new Date( "0000-00-00" ) ) ) {
                        modifiedAttribute = "'" + parsedElement.getQueryableProperties().getModified() + "'";
                    }

                    // TODO version

                    // TODO status

                    // anyText
                    if ( parsedElement.getQueryableProperties().getAnyText() != null ) {

                        sqlStatementUpdate.append( "UPDATE " ).append( databaseTable ).append( " SET anyText = '" );
                        sqlStatementUpdate.append( parsedElement.getQueryableProperties().getAnyText() ).append(
                                                                                                                 "' WHERE " );
                        sqlStatementUpdate.append( id ).append( '=' );
                        sqlStatementUpdate.append( requestedId );
                        stmt.executeUpdate( sqlStatementUpdate.toString() );
                        sqlStatementUpdate.setLength( 0 );

                    }

                    // modified
                    if ( !parsedElement.getQueryableProperties().getModified().equals( new Date( "0000-00-00" ) ) ) {
                        sqlStatementUpdate.append( "UPDATE " ).append( databaseTable ).append( " SET modified = " ).append(
                                                                                                                            modifiedAttribute );
                        sqlStatementUpdate.append( " WHERE " ).append( id );
                        sqlStatementUpdate.append( '=' ).append( requestedId );
                        stmt.executeUpdate( sqlStatementUpdate.toString() );
                        sqlStatementUpdate.setLength( 0 );
                    }
                    // hassecurityconstraints
                    if ( parsedElement.getQueryableProperties().isHasSecurityConstraints() == true ) {
                        sqlStatementUpdate.append( "UPDATE " ).append( databaseTable ).append(
                                                                                               " SET hassecurityconstraints = '" );
                        sqlStatementUpdate.append( parsedElement.getQueryableProperties().isHasSecurityConstraints() );
                        sqlStatementUpdate.append( "' WHERE " ).append( id );
                        sqlStatementUpdate.append( '=' ).append( requestedId );

                        stmt.executeUpdate( sqlStatementUpdate.toString() );
                        sqlStatementUpdate.setLength( 0 );
                    }

                    // language
                    if ( parsedElement.getQueryableProperties().getLanguage() != null ) {
                        sqlStatementUpdate.append( "UPDATE " ).append( databaseTable ).append( " SET language = '" );
                        sqlStatementUpdate.append( parsedElement.getQueryableProperties().getLanguage() ).append(
                                                                                                                  "' WHERE " );
                        sqlStatementUpdate.append( id ).append( '=' );
                        sqlStatementUpdate.append( requestedId );

                        stmt.executeUpdate( sqlStatementUpdate.toString() );
                        sqlStatementUpdate.setLength( 0 );
                    }
                    // parentidentifier
                    if ( parsedElement.getQueryableProperties().getParentIdentifier() != null ) {
                        sqlStatementUpdate.append( "UPDATE " ).append( databaseTable ).append(
                                                                                               " SET parentidentifier = '" );
                        sqlStatementUpdate.append( parsedElement.getQueryableProperties().getParentIdentifier() );
                        sqlStatementUpdate.append( "' WHERE " ).append( id );
                        sqlStatementUpdate.append( '=' ).append( requestedId );

                        stmt.executeUpdate( sqlStatementUpdate.toString() );
                        sqlStatementUpdate.setLength( 0 );
                    }
                    // TODO source

                    // TODO association

                    // recordBrief, recordSummary, recordFull update
                    updatedIds.add( buildRecXML.updateRecord( requestedId, parsedElement, connection ) );

                    generateQP.executeQueryableProperties( isUpdate, connection, requestedId, parsedElement );

                } else {
                    // TODO think about what response should be written if there is no such dataset in the backend??
                    String msg = "No dataset found for the identifier --> "
                                 + parsedElement.getQueryableProperties().getIdentifier() + " <--. ";
                    throw new SQLException( msg );
                }
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
            if ( stm != null ) {
                stm.close();
            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( IOException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } finally {
            JDBCUtils.close( rs, stm, connection, LOG );
            JDBCUtils.close( null, stmt, null, LOG );
        }
    }

}
