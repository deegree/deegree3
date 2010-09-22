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
package org.deegree.metadata.persistence.iso.generating;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso.PostGISMappingsISODC;
import org.deegree.metadata.persistence.iso.parsing.ParsedProfileElement;
import org.deegree.metadata.persistence.iso.parsing.QueryableProperties;
import org.slf4j.Logger;

/**
 * Here is the handling of generating and inserting the ISO and DC application profile metadata as well as the update of
 * these metadata.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BuildMetadataXMLRepresentation {

    private static final Logger LOG = getLogger( BuildMetadataXMLRepresentation.class );

    private static final String dataColumn = PostGISMappingsISODC.CommonColumnNames.data.name();

    private static final String idColumn = PostGISMappingsISODC.CommonColumnNames.id.name();

    private static final String fk_datasetsColumn = PostGISMappingsISODC.CommonColumnNames.fk_datasets.name();

    private static final String formatColumn = PostGISMappingsISODC.CommonColumnNames.format.name();

    private static final StringBuilder sqlStatementUpdate = new StringBuilder().append( " SET " ).append( dataColumn ).append(
                                                                                                                               " = ? WHERE " ).append(
                                                                                                                                                       fk_datasetsColumn ).append(
                                                                                                                                                                                   " = ? AND " ).append(
                                                                                                                                                                                                         formatColumn ).append(
                                                                                                                                                                                                                                " = ?;" );;

    private static final StringBuilder sqlStatementInsert = new StringBuilder().append( " ( " ).append( idColumn ).append(
                                                                                                                           ", " ).append(
                                                                                                                                          fk_datasetsColumn ).append(
                                                                                                                                                                      ", " ).append(
                                                                                                                                                                                     formatColumn ).append(
                                                                                                                                                                                                            ", " ).append(
                                                                                                                                                                                                                           dataColumn ).append(
                                                                                                                                                                                                                                                " ) VALUES (?, ?, ?, ?);" );

    private int insertDatadaseTableLength;

    private int updateDatadaseTableLength;

    /**
     * Updating the XML representation of a record in DC and ISO.
     * 
     * @param fk_datasets
     *            which record dataset should be updated
     * @param parsedElement
     * @param stm
     * @return an integer that indicates if there is a record updated
     * @throws IOException
     */
    public int updateRecord( int fk_datasets, ParsedProfileElement parsedElement, Connection connection )
                            throws IOException {

        StringWriter isoOMElement = new StringWriter( 2000 );
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespaceCSW = factory.createOMNamespace( CSW_202_NS, CSW_PREFIX );

        int counter = 0;
        for ( String databaseTable : PostGISMappingsISODC.getTableRecordType().keySet() ) {

            // StringBuilder sqlStatement = new StringBuilder( 500 );
            PreparedStatement stm = null;

            try {

                String updateDatabaseTable = "UPDATE " + databaseTable;
                updateDatadaseTableLength = updateDatabaseTable.length();
                sqlStatementUpdate.insert( 0, "UPDATE " ).insert( 7, databaseTable );
                // DC-update
                OMElement omElement = factory.createOMElement(
                                                               PostGISMappingsISODC.getTableRecordType().get(
                                                                                                              databaseTable ),
                                                               namespaceCSW );

                if ( omElement.getLocalName().equals( PostGISMappingsISODC.BRIEFRECORD ) ) {
                    parsedElement.getGenerateRecord().buildElementAsDcBriefElement( omElement, factory );
                    isoOMElement.write( parsedElement.getGenerateRecord().getIsoBriefElement().toString() );
                    counter++;
                } else if ( omElement.getLocalName().equals( PostGISMappingsISODC.SUMMARYRECORD ) ) {
                    parsedElement.getGenerateRecord().buildElementAsDcSummaryElement( omElement, factory );
                    isoOMElement.write( parsedElement.getGenerateRecord().getIsoSummaryElement().toString() );
                } else {
                    parsedElement.getGenerateRecord().buildElementAsDcFullElement( omElement, factory );
                    isoOMElement.write( parsedElement.getGenerateRecord().getIsoFullElement().toString() );
                }

                setBoundingBoxElement( omElement, parsedElement.getQueryableProperties() );

                executeUpdate( stm, connection, fk_datasets, omElement );

                // ISO-update
                stm = connection.prepareStatement( sqlStatementUpdate.toString() );
                stm.setBytes( 1, isoOMElement.toString().getBytes() );
                stm.setObject( 2, fk_datasets );
                stm.setObject( 3, 2 );
                LOG.debug( "" + stm );
                stm.executeUpdate();
                stm.close();

                sqlStatementUpdate.delete( 0, updateDatadaseTableLength );

            } catch ( SQLException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            } catch ( ParseException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            }
        }

        return counter;

    }

    /**
     * Generates the ISO representation in brief, summary and full for this dataset.
     * 
     * @param connection
     * @param stm
     * @param operatesOnId
     * @param parsedElement
     * @return an integer that is the primarykey from the inserted record
     * 
     * @throws IOException
     * @throws MetadataStoreException
     */
    public String generateISO( Connection connection, int operatesOnId, ParsedProfileElement parsedElement )
                            throws IOException, MetadataStoreException {

        int idDatabaseTable;
        for ( String databaseTableISO : PostGISMappingsISODC.getTableRecordType().keySet() ) {

            // ------------------------
            // there is a restriction regarding to the databasetables.
            // in some cases there is no preparedStatement allowed for databasetables
            // so: put the insert-preample before the final statement
            // and delete it later...
            String insertDatabaseTable = "INSERT INTO " + databaseTableISO;
            insertDatadaseTableLength = insertDatabaseTable.length();
            sqlStatementInsert.insert( 0, "INSERT INTO " ).insert( 12, databaseTableISO );
            // ------------------------
            PreparedStatement stm = null;
            OMElement isoElement;
            if ( databaseTableISO.equals( PostGISMappingsISODC.RECORDBRIEF ) ) {
                isoElement = parsedElement.getGenerateRecord().getIsoBriefElement();
            } else if ( databaseTableISO.equals( PostGISMappingsISODC.RECORDSUMMARY ) ) {
                isoElement = parsedElement.getGenerateRecord().getIsoSummaryElement();
            } else {
                isoElement = parsedElement.getGenerateRecord().getIsoFullElement();
            }

            try {

                idDatabaseTable = getLastDatasetId( connection, databaseTableISO );
                idDatabaseTable++;

                executeInsert( stm, connection, idDatabaseTable, operatesOnId, isoElement );
            } catch ( SQLException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            }

        }
        /*
         * additional it generates the Dublin Core representation
         */

        return generateDC( connection, operatesOnId, parsedElement );

    }

    private void executeInsert( PreparedStatement stm, Connection connection, int idDatabaseTable, int operatesOnId,
                                OMElement element )
                            throws SQLException {
        stm = connection.prepareStatement( sqlStatementInsert.toString() );
        stm.setObject( 1, idDatabaseTable );
        stm.setObject( 2, operatesOnId );
        stm.setObject( 3, 2 );
        stm.setBytes( 4, element.toString().getBytes() );
        LOG.debug( "" + stm );
        stm.executeUpdate();
        stm.close();

        // here is the deletion of the insert-preample
        sqlStatementInsert.delete( 0, insertDatadaseTableLength );

    }

    private void executeUpdate( PreparedStatement stm, Connection connection, int fk_datasets, OMElement omElement )
                            throws SQLException {
        stm = connection.prepareStatement( sqlStatementUpdate.toString() );
        stm.setBytes( 1, omElement.toString().getBytes() );
        stm.setObject( 2, fk_datasets );
        stm.setObject( 3, 1 );
        LOG.debug( "" + stm );
        stm.executeUpdate();
    }

    /**
     * Generates the Dublin Core representation in brief, summary and full for this dataset.
     * 
     * @param connection
     * @param stm
     * @param operatesOnId
     * @param parsedElement
     * 
     * @return an integer that is the primarykey from the inserted record
     * @throws MetadataStoreException
     */
    public String generateDC( Connection connection, int operatesOnId, ParsedProfileElement parsedElement )
                            throws MetadataStoreException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespaceCSW = factory.createOMNamespace( CSW_202_NS, CSW_PREFIX );

        int idDatabaseTable;
        String identifier = null;
        for ( String databaseTableDC : PostGISMappingsISODC.getTableRecordType().keySet() ) {
            PreparedStatement stm = null;

            // ------------------------
            // there is a restriction regarding to the databasetables.
            // in some cases there is no preparedStatement allowed for databasetables
            // so: put the insert-preample before the final statement
            // and delete it later...
            String insertDatabaseTable = "INSERT INTO " + databaseTableDC;
            insertDatadaseTableLength = insertDatabaseTable.length();
            sqlStatementInsert.insert( 0, "INSERT INTO " ).insert( 12, databaseTableDC );
            // ------------------------
            try {

                idDatabaseTable = getLastDatasetId( connection, databaseTableDC );
                idDatabaseTable++;

                OMElement omElement = factory.createOMElement(
                                                               PostGISMappingsISODC.getTableRecordType().get(
                                                                                                              databaseTableDC ),
                                                               namespaceCSW );

                if ( omElement.getLocalName().equals( PostGISMappingsISODC.BRIEFRECORD ) ) {
                    identifier = parsedElement.getGenerateRecord().buildElementAsDcBriefElement( omElement, factory );
                } else if ( omElement.getLocalName().equals( PostGISMappingsISODC.SUMMARYRECORD ) ) {
                    identifier = parsedElement.getGenerateRecord().buildElementAsDcSummaryElement( omElement, factory );
                } else {
                    identifier = parsedElement.getGenerateRecord().buildElementAsDcFullElement( omElement, factory );
                }

                if ( parsedElement.getQueryableProperties().getBoundingBox() != null ) {
                    setBoundingBoxElement( omElement, parsedElement.getQueryableProperties() );
                }

                stm = connection.prepareStatement( sqlStatementInsert.toString() );

                executeInsert( stm, connection, idDatabaseTable, operatesOnId, omElement );

            } catch ( SQLException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
                throw new MetadataStoreException( e.getMessage() );
            } catch ( ParseException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
                throw new MetadataStoreException( e.getMessage() );
            }

        }

        return identifier;

    }

    /**
     * Creation of the boundingBox element. Specifies which points has to be at which corner. The CRS is set to
     * EPSG:4326 because EX_GeographicBoundingBox is in this code implicitly.
     * 
     * 
     * @param omElement
     * @param qp
     */
    private void setBoundingBoxElement( OMElement omElement, QueryableProperties qp ) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespaceOWS = factory.createOMNamespace( "http://www.opengis.net/ows", "ows" );

        OMElement omBoundingBox = factory.createOMElement( "BoundingBox", namespaceOWS );
        OMElement omLowerCorner = factory.createOMElement( "LowerCorner", namespaceOWS );
        OMElement omUpperCorner = factory.createOMElement( "UpperCorner", namespaceOWS );
        // OMAttribute omCrs = factory.createOMAttribute( "crs", namespaceOWS, "EPSG:4326" );

        omUpperCorner.setText( qp.getBoundingBox().getEastBoundLongitude() + " "
                               + qp.getBoundingBox().getNorthBoundLatitude() );
        omLowerCorner.setText( qp.getBoundingBox().getWestBoundLongitude() + " "
                               + qp.getBoundingBox().getSouthBoundLatitude() );
        omBoundingBox.addChild( omLowerCorner );
        omBoundingBox.addChild( omUpperCorner );
        // omBoundingBox.addAttribute( omCrs );

        omElement.addChild( omBoundingBox );

    }

    /**
     * Provides the last known id in the databaseTable. So it is possible to insert new datasets into this table come
     * from this id.
     * 
     * @param conn
     * @param databaseTable
     *            the databaseTable that is requested.
     * @return the last Primary Key ID of the databaseTable.
     * @throws SQLException
     */
    private int getLastDatasetId( Connection conn, String databaseTable )
                            throws SQLException {
        int result = 0;
        String selectIDRows = "SELECT " + idColumn + " from " + databaseTable + " ORDER BY " + idColumn
                              + " DESC LIMIT 1";
        PreparedStatement prepState = conn.prepareStatement( selectIDRows );

        ResultSet rsBrief = prepState.executeQuery();

        while ( rsBrief.next() ) {

            result = rsBrief.getInt( 1 );

        }
        rsBrief.close();
        prepState.close();
        return result;

    }

}
