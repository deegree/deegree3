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
package org.deegree.record.persistence.genericrecordstore;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.record.persistence.genericrecordstore.generating.BuildRecordXMLRepresentation;
import org.deegree.record.persistence.genericrecordstore.generating.GenerateQueryableProperties;
import org.deegree.record.persistence.genericrecordstore.parsing.ParsedProfileElement;
import org.slf4j.Logger;

/**
 * The execution of the actions affected by the transaction operation against the database.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExecuteStatements {

    private static final Logger LOG = getLogger( ExecuteStatements.class );

    private GenerateQueryableProperties generateQP;

    private BuildRecordXMLRepresentation buildRecXML;

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
     */
    public void executeInsertStatement( boolean isDC, Connection connection, List<Integer> insertedIds,
                                        ParsedProfileElement parsedElement )
                            throws IOException {
        generateQP = new GenerateQueryableProperties();
        buildRecXML = new BuildRecordXMLRepresentation();

        try {
            PreparedStatement stm = null;
            boolean isUpdate = false;

            /*
             * Question if there already exists the identifier.
             */
            for ( String identifier : parsedElement.getQueryableProperties().getIdentifier() ) {
                String s = "SELECT i.identifier FROM " + PostGISMappingsISODC.DatabaseTables.qp_identifier.name()
                           + " AS i WHERE i.identifier = ?;";
                stm = connection.prepareStatement( s );
                stm.setObject( 1, identifier );
                ResultSet r = stm.executeQuery();
                LOG.debug( s );

                if ( r.next() ) {
                    stm.close();
                    throw new IOException( "Record with identifier '"
                                           + parsedElement.getQueryableProperties().getIdentifier()
                                           + "' already exists!" );
                }
            }
            int operatesOnId = generateQP.generateMainDatabaseDataset( connection, parsedElement );

            if ( isDC == true ) {
                insertedIds.add( buildRecXML.generateDC( connection, operatesOnId, parsedElement ) );
            } else {
                insertedIds.add( buildRecXML.generateISO( connection, operatesOnId, parsedElement ) );

            }
            generateQP.executeQueryableProperties( isUpdate, connection, operatesOnId, parsedElement );
            if ( stm != null ) {
                stm.close();
            }
        } catch ( SQLException e ) {
            LOG.debug( "error: " + e.getMessage(), e );
        }

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

        final String databaseTable = PostGISMappingsISODC.DatabaseTables.datasets.name();
        final String qp_identifier = PostGISMappingsISODC.DatabaseTables.qp_identifier.name();
        boolean isUpdate = true;
        generateQP = new GenerateQueryableProperties();
        buildRecXML = new BuildRecordXMLRepresentation();

        StringWriter sqlStatementUpdate = new StringWriter( 500 );

        int requestedId = 0;
        String modifiedAttribute = "null";

        try {
            PreparedStatement stm = null;
            for ( String identifierString : parsedElement.getQueryableProperties().getIdentifier() ) {

                sqlStatementUpdate.append( "SELECT " + databaseTable + "."
                                           + PostGISMappingsISODC.CommonColumnNames.id.name() + " FROM "
                                           + databaseTable + "," + qp_identifier + " WHERE " + databaseTable + "."
                                           + PostGISMappingsISODC.CommonColumnNames.id.name() + " = " + qp_identifier
                                           + "." + PostGISMappingsISODC.CommonColumnNames.fk_datasets.name() + " AND "
                                           + qp_identifier + ".identifier = ?" );
                LOG.debug( sqlStatementUpdate.toString() );
                StringBuffer buf = sqlStatementUpdate.getBuffer();
                stm = connection.prepareStatement( sqlStatementUpdate.toString() );
                stm.setObject( 1, identifierString );
                ResultSet rs = stm.executeQuery();

                while ( rs.next() ) {
                    requestedId = rs.getInt( 1 );
                    LOG.debug( "resultSet: " + rs.getInt( 1 ) );
                }
                buf.setLength( 0 );
                rs.close();

                if ( requestedId != 0 ) {

                    if ( !parsedElement.getQueryableProperties().getModified().equals( new Date( "0000-00-00" ) ) ) {
                        modifiedAttribute = "'" + parsedElement.getQueryableProperties().getModified() + "'";
                    }

                    // TODO version

                    // TODO status

                    // anyText
                    if ( parsedElement.getQueryableProperties().getAnyText() != null ) {

                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET anyText = '"
                                                  + parsedElement.getQueryableProperties().getAnyText() + "' WHERE "
                                                  + PostGISMappingsISODC.CommonColumnNames.id.name() + " = "
                                                  + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate, stm );

                    }

                    // modified
                    if ( !parsedElement.getQueryableProperties().getModified().equals( new Date( "0000-00-00" ) ) ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET modified = " + modifiedAttribute
                                                  + " WHERE " + PostGISMappingsISODC.CommonColumnNames.id.name()
                                                  + " = " + requestedId );
                        executeSQLStatementUpdate( sqlStatementUpdate, stm );
                    }
                    // hassecurityconstraints
                    if ( parsedElement.getQueryableProperties().isHasSecurityConstraints() == true ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET hassecurityconstraints = '"
                                                  + parsedElement.getQueryableProperties().isHasSecurityConstraints()
                                                  + "' WHERE " + PostGISMappingsISODC.CommonColumnNames.id.name()
                                                  + " = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate, stm );
                    }

                    // language
                    if ( parsedElement.getQueryableProperties().getLanguage() != null ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET language = '"
                                                  + parsedElement.getQueryableProperties().getLanguage() + "' WHERE "
                                                  + PostGISMappingsISODC.CommonColumnNames.id.name() + " = "
                                                  + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate, stm );
                    }
                    // parentidentifier
                    if ( parsedElement.getQueryableProperties().getParentIdentifier() != null ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET parentidentifier = '"
                                                  + parsedElement.getQueryableProperties().getParentIdentifier()
                                                  + "' WHERE " + PostGISMappingsISODC.CommonColumnNames.id.name()
                                                  + " = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate, stm );
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
        }
    }

    /**
     * Executes the SQL statement and cleans the size.<br>
     * It can be seen as a helper method to keep the code easy.
     * 
     * @param sqlStatementUpdate
     *            the statement that is responsible for updating the backend
     * @throws SQLException
     */
    private void executeSQLStatementUpdate( StringWriter sqlStatementUpdate, PreparedStatement stm )
                            throws SQLException {
        StringBuffer buf = sqlStatementUpdate.getBuffer();
        LOG.debug( sqlStatementUpdate.toString() );
        stm.executeUpdate( sqlStatementUpdate.toString() );
        buf.setLength( 0 );

    }

}
