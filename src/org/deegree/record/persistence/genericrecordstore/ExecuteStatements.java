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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.List;

import org.deegree.commons.types.datetime.Date;
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

    private Statement stm;

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
            stm = connection.createStatement();
            boolean isUpdate = false;

            /*
             * Question if there already exists the identifier.
             */
            for ( String identifier : parsedElement.getQueryableProperties().getIdentifier() ) {
                String s = "SELECT i.identifier FROM qp_identifier AS i WHERE i.identifier = '" + identifier + "';";
                ResultSet r = stm.executeQuery( s );
                LOG.debug( s );

                if ( r.next() ) {
                    stm.close();
                    throw new IOException( "Record with identifier '"
                                           + parsedElement.getQueryableProperties().getIdentifier()
                                           + "' already exists!" );
                }
            }
            int operatesOnId = generateQP.generateMainDatabaseDataset( connection, stm, parsedElement );

            if ( isDC == true ) {
                insertedIds.add( buildRecXML.generateDC( connection, stm, operatesOnId, parsedElement ) );
            } else {
                insertedIds.add( buildRecXML.generateISO( connection, stm, operatesOnId, parsedElement ) );

            }
            generateQP.executeQueryableProperties( isUpdate, connection, stm, operatesOnId, parsedElement );
            stm.close();

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

        final String databaseTable = PostGISMappingsISODC.databaseTables.datasets.name();
        final String qp_identifier = PostGISMappingsISODC.databaseTables.qp_identifier.name();
        boolean isUpdate = true;
        generateQP = new GenerateQueryableProperties();
        buildRecXML = new BuildRecordXMLRepresentation();

        StringWriter sqlStatementUpdate = new StringWriter( 500 );

        int requestedId = 0;
        String modifiedAttribute = "null";
        try {
            stm = connection.createStatement();
            for ( String identifierString : parsedElement.getQueryableProperties().getIdentifier() ) {

                sqlStatementUpdate.append( "SELECT " + databaseTable + "."
                                           + PostGISMappingsISODC.commonColumnNames.id.name() + " FROM " + databaseTable
                                           + "," + qp_identifier + " WHERE " + databaseTable + "."
                                           + PostGISMappingsISODC.commonColumnNames.id.name() + " = " + qp_identifier + "."
                                           + PostGISMappingsISODC.commonColumnNames.fk_datasets.name() + " AND "
                                           + qp_identifier + ".identifier = '" + identifierString + "'" );
                LOG.debug( sqlStatementUpdate.toString() );
                StringBuffer buf = sqlStatementUpdate.getBuffer();
                ResultSet rs = connection.createStatement().executeQuery( sqlStatementUpdate.toString() );

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
                                                  + parsedElement.getQueryableProperties().getAnyText()
                                                  + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );

                    }

                    // modified
                    if ( !parsedElement.getQueryableProperties().getModified().equals( new Date( "0000-00-00" ) ) ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET modified = " + modifiedAttribute
                                                  + " WHERE id = " + requestedId );
                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }
                    // hassecurityconstraints
                    if ( parsedElement.getQueryableProperties().isHasSecurityConstraints() == true ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET hassecurityconstraints = '"
                                                  + parsedElement.getQueryableProperties().isHasSecurityConstraints()
                                                  + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }

                    // language
                    if ( parsedElement.getQueryableProperties().getLanguage() != null ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET language = '"
                                                  + parsedElement.getQueryableProperties().getLanguage()
                                                  + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }
                    // parentidentifier
                    if ( parsedElement.getQueryableProperties().getParentIdentifier() != null ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET parentidentifier = '"
                                                  + parsedElement.getQueryableProperties().getParentIdentifier()
                                                  + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }
                    // TODO source

                    // TODO association

                    // recordBrief, recordSummary, recordFull update
                    updatedIds.add( buildRecXML.updateRecord( requestedId, parsedElement, stm ) );

                    generateQP.executeQueryableProperties( isUpdate, connection, stm, requestedId, parsedElement );

                } else {
                    // TODO think about what response should be written if there is no such dataset in the backend??
                    String msg = "No dataset found for the identifier --> "
                                 + parsedElement.getQueryableProperties().getIdentifier() + " <--. ";
                    throw new SQLException( msg );
                }
            }
            stm.close();

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
    private void executeSQLStatementUpdate( StringWriter sqlStatementUpdate )
                            throws SQLException {
        StringBuffer buf = sqlStatementUpdate.getBuffer();
        LOG.debug( sqlStatementUpdate.toString() );
        stm.executeUpdate( sqlStatementUpdate.toString() );
        buf.setLength( 0 );

    }

}
