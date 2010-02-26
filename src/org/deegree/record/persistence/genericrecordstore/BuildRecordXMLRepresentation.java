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
import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.slf4j.Logger;

/**
 * Here is the handling of generating and additional inserting the ISO and DC application profile record as well as the
 * update of these records.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class BuildRecordXMLRepresentation {

    private static final Logger LOG = getLogger( BuildRecordXMLRepresentation.class );

    /**
     * Tablename in backend
     */
    private final static String RECORDBRIEF = "recordbrief";

    /**
     * Tablename in backend
     */
    private final static String RECORDSUMMARY = "recordsummary";

    /**
     * Tablename in backend
     */
    private final static String RECORDFULL = "recordfull";

    /**
     * XML element name in the representation of the response
     */
    private final static String BRIEFRECORD = "BriefRecord";

    /**
     * XML element name in the representation of the response
     */
    private final static String SUMMARYRECORD = "SummaryRecord";

    /**
     * XML element name in the representation of the response
     */
    private final static String RECORD = "Record";

    /**
     * 
     */
    private static Map<String, String> tableRecordType = new HashMap<String, String>();

    static {

        tableRecordType.put( RECORDBRIEF, BRIEFRECORD );
        tableRecordType.put( RECORDSUMMARY, SUMMARYRECORD );
        tableRecordType.put( RECORDFULL, RECORD );
    }

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
    int updateRecord( int fk_datasets, ParsedProfileElement parsedElement, Statement stm )
                            throws IOException {

        StringWriter isoOMElement = new StringWriter( 2000 );
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

        int counter = 0;
        for ( String databaseTable : tableRecordType.keySet() ) {

            StringWriter sqlStatement = new StringWriter( 500 );
            StringBuffer buf = new StringBuffer();

            try {
                // DC-update
                OMElement omElement = factory.createOMElement( tableRecordType.get( databaseTable ), namespaceCSW );

                if ( omElement.getLocalName().equals( BRIEFRECORD ) ) {
                    parsedElement.getGenerateRecord().buildElementAsDcBriefElement( omElement, factory );
                    isoOMElement.write( parsedElement.getGenerateRecord().getIsoBriefElement().toString() );
                    counter++;
                } else if ( omElement.getLocalName().equals( SUMMARYRECORD ) ) {
                    parsedElement.getGenerateRecord().buildElementAsDcSummaryElement( omElement, factory );
                    isoOMElement.write( parsedElement.getGenerateRecord().getIsoSummaryElement().toString() );
                } else {
                    parsedElement.getGenerateRecord().buildElementAsDcFullElement( omElement, factory );
                    isoOMElement.write( parsedElement.getGenerateRecord().getIsoFullElement().toString() );
                }

                setBoundingBoxElement( omElement, parsedElement.getQueryableProperties() );

                sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + omElement.toString()
                                    + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 1 );

                buf = sqlStatement.getBuffer();
                stm.executeUpdate( sqlStatement.toString() );
                buf.setLength( 0 );

                // ISO-update
                sqlStatement.write( "UPDATE " + databaseTable + " SET data = '" + isoOMElement
                                    + "' WHERE fk_datasets = " + fk_datasets + " AND format = " + 2 );

                buf = sqlStatement.getBuffer();
                stm.executeUpdate( sqlStatement.toString() );
                buf.setLength( 0 );

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
     */
    int generateISO( Connection connection, Statement stm, int operatesOnId, ParsedProfileElement parsedElement )
                            throws IOException {

        int idDatabaseTable;
        for ( String databaseTable : tableRecordType.keySet() ) {
            StringWriter sqlStatement = new StringWriter( 500 );
            OMElement isoElement;
            if ( databaseTable.equals( RECORDBRIEF ) ) {
                isoElement = parsedElement.getGenerateRecord().getIsoBriefElement();
            } else if ( databaseTable.equals( RECORDSUMMARY ) ) {
                isoElement = parsedElement.getGenerateRecord().getIsoSummaryElement();
            } else {
                isoElement = parsedElement.getGenerateRecord().getIsoFullElement();
            }

            try {

                idDatabaseTable = getLastDatasetId( connection, databaseTable );
                idDatabaseTable++;
                LOG.info( "ISODatabaseTable: " + idDatabaseTable );

                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                                     + idDatabaseTable + "," + operatesOnId + ", 2, '" + isoElement.toString() + "');" );
                stm.executeUpdate( sqlStatement.toString() );

            } catch ( SQLException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            }

        }
        /*
         * additional it generates the Dublin Core representation
         */

        return generateDC( connection, stm, operatesOnId, parsedElement );

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
     */
    int generateDC( Connection connection, Statement stm, int operatesOnId, ParsedProfileElement parsedElement ) {

        int recordsAffectedID = 0;
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

        int idDatabaseTable;
        for ( String databaseTable : tableRecordType.keySet() ) {
            StringWriter sqlStatement = new StringWriter( 500 );

            try {

                idDatabaseTable = getLastDatasetId( connection, databaseTable );
                idDatabaseTable++;

                OMElement omElement = factory.createOMElement( tableRecordType.get( databaseTable ), namespaceCSW );

                if ( omElement.getLocalName().equals( BRIEFRECORD ) ) {
                    parsedElement.getGenerateRecord().buildElementAsDcBriefElement( omElement, factory );
                    recordsAffectedID = idDatabaseTable;
                } else if ( omElement.getLocalName().equals( SUMMARYRECORD ) ) {
                    parsedElement.getGenerateRecord().buildElementAsDcSummaryElement( omElement, factory );
                } else {
                    parsedElement.getGenerateRecord().buildElementAsDcFullElement( omElement, factory );
                }

                setBoundingBoxElement( omElement, parsedElement.getQueryableProperties() );

                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                                     + idDatabaseTable + "," + operatesOnId + ", 1, '" + omElement.toString() + "');" );

                stm.executeUpdate( sqlStatement.toString() );

            } catch ( SQLException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            } catch ( ParseException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            }
        }
        return recordsAffectedID;

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
                               + qp.getBoundingBox().getSouthBoundLatitude() );
        omLowerCorner.setText( qp.getBoundingBox().getWestBoundLongitude() + " "
                               + qp.getBoundingBox().getNorthBoundLatitude() );
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
        String selectIDRows = "SELECT id from " + databaseTable + " ORDER BY id DESC LIMIT 1";
        ResultSet rsBrief = conn.createStatement().executeQuery( selectIDRows );

        while ( rsBrief.next() ) {

            result = rsBrief.getInt( 1 );

        }
        rsBrief.close();
        return result;

    }

}
