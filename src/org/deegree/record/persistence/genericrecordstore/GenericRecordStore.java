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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.PooledConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.record.persistence.GenericDatabaseDS;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.RecordStoreException;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.TransactionOperation;

/**
 * {@link RecordStore} implementation of Dublin Core and ISO Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GenericRecordStore implements RecordStore {

    private static Map<QName, Integer> typeNames = new HashMap<QName, Integer>();

    private String connectionId;

    /**
     * datasets
     */
    private final String mainDatabaseTable = "datasets";

    /**
     * fk_datasets
     */
    private final String commonForeignkey = "fk_datasets";

    private Set<String> tableSet;

    private static final Map<String, String> formatTypeInGenericRecordStore = new HashMap<String, String>();

    static {

        formatTypeInGenericRecordStore.put( "brief", "recordbrief" );
        formatTypeInGenericRecordStore.put( "summary", "recordsummary" );
        formatTypeInGenericRecordStore.put( "full", "recordfull" );

        // typeNames[0] = new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "csw" );
        // typeNames[1] = new QName( "http://www.isotc211.org/2005/gmd", "MD_Metadata", "gmd" );

        typeNames.put( new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "csw" ), 1 );
        typeNames.put( new QName( "http://www.isotc211.org/2005/gmd", "MD_Metadata", "gmd" ), 2 );

    }

    public GenericRecordStore( String connectionId ) {
        this.connectionId = connectionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#describeRecord(javax.xml.stream.XMLStreamWriter)
     */
    @Override
    public void describeRecord( XMLStreamWriter writer, QName typeName ) {

        URL url = null;
        for ( QName name : typeNames.keySet() ) {
            if ( typeName.equals( name ) ) {

                // in = new FileInputStream( "../dc/dc.xsd" );
                url = GenericRecordStore.class.getResource( "dc.xsd" );

            } else {
                // in = new FileInputStream( "../gmd/gmd.xsd" );
                url = GenericRecordStore.class.getResource( "gmd_metadata.xsd" );
            }
        }

        XMLAdapter ada = new XMLAdapter( url );

        System.out.println( ada.toString() );
        readXMLFragment( ada.toString(), writer );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#init()
     */
    @Override
    public void init()
                            throws RecordStoreException {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getTypeNames()
     */
    @Override
    public Map<QName, Integer> getTypeNames() {

        return typeNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getRecords(javax.xml.stream.XMLStreamWriter,
     * javax.xml.namespace.QName)
     */
    @Override
    public void getRecords( XMLStreamWriter writer, QName typeName, JDBCConnections con, GenericDatabaseDS constraint )
                            throws SQLException, XMLStreamException {

        if ( constraint.getTable() != null ) {
            tableSet = constraint.getTable();
        } else {
            tableSet = new HashSet<String>();
        }
        correctTable( tableSet );

        switch ( constraint.getResultType() ) {
        case results:

            doResultsOnGetRecord( writer, typeName, constraint, con );
            break;
        case hits:

            doHitsOnGetRecord( writer, typeName, constraint, con,
                               formatTypeInGenericRecordStore.get( constraint.getSetOfReturnableElements().name() ),
                               "hits" );
            break;
        case validate:

            doValidateOnGetRecord( writer, typeName, constraint.getSetOfReturnableElements(), con );
            break;
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatType
     * @param returnableElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doHitsOnGetRecord( XMLStreamWriter writer, QName typeName, GenericDatabaseDS constraint,
                                    JDBCConnections con, String formatType, String resultType )
                            throws SQLException, XMLStreamException {

        int countRows = 0;
        int nextRecord = 0;
        int returnedRecords = 0;
        int profileFormatNumber = 1;

        String selectCountRows = "";

        for ( QName whichTypeName : typeNames.keySet() ) {
            if ( typeName == whichTypeName ) {
                profileFormatNumber = typeNames.get( whichTypeName );
                break;
            }

        }

        selectCountRows = generateCOUNTStatement( formatType, constraint, profileFormatNumber );

        // ConnectionManager.addConnections( con );
        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );
            ResultSet rs = conn.createStatement().executeQuery( selectCountRows );

            while ( rs.next() ) {
                countRows = rs.getInt( 1 );
                System.out.println( rs.getInt( 1 ) );
            }

            if ( resultType.equals( "hits" ) ) {
                writer.writeAttribute( "elementSet", constraint.getSetOfReturnableElements().name() );

                // writer.writeAttribute( "recordSchema", "");

                writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

                writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( 0 ) );

                writer.writeAttribute( "nextRecord", Integer.toString( 1 ) );

                writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );
            } else {

                if ( countRows > constraint.getMaxRecords() ) {
                    nextRecord = constraint.getMaxRecords() + 1;
                    returnedRecords = constraint.getMaxRecords();
                } else {
                    nextRecord = 0;
                    returnedRecords = countRows;
                }

                writer.writeAttribute( "elementSet", constraint.getSetOfReturnableElements().name() );

                // writer.writeAttribute( "recordSchema", "");

                writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

                writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( returnedRecords ) );

                writer.writeAttribute( "nextRecord", Integer.toString( nextRecord ) );

                writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );
            }

            rs.close();
            conn.close();
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatTypeInGenericRecordStore
     * @param returnableElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doResultsOnGetRecord( XMLStreamWriter writer, QName typeName, GenericDatabaseDS constraint,
                                       JDBCConnections con )
                            throws SQLException, XMLStreamException {
        int profileFormatNumber = 1;

        for ( QName whichTypeName : typeNames.keySet() ) {
            if ( typeName.equals( whichTypeName ) ) {
                profileFormatNumber = typeNames.get( whichTypeName );
                break;
            }

        }

        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );

            switch ( constraint.getSetOfReturnableElements() ) {

            case brief:

                String selectBrief = generateSELECTStatement( formatTypeInGenericRecordStore.get( "brief" ),
                                                              constraint, profileFormatNumber );
                ResultSet rsBrief = conn.createStatement().executeQuery( selectBrief );

                doHitsOnGetRecord( writer, typeName, constraint, con, formatTypeInGenericRecordStore.get( "brief" ),
                                   "results" );

                while ( rsBrief.next() ) {

                    String result = rsBrief.getString( 1 );

                    readXMLFragment( result, writer );

                }
                rsBrief.close();

                break;
            case summary:

                String selectSummary = generateSELECTStatement( formatTypeInGenericRecordStore.get( "summary" ),
                                                                constraint, profileFormatNumber );
                ResultSet rsSummary = conn.createStatement().executeQuery( selectSummary );

                doHitsOnGetRecord( writer, typeName, constraint, con, formatTypeInGenericRecordStore.get( "summary" ),
                                   "results" );

                while ( rsSummary.next() ) {
                    String result = rsSummary.getString( 1 );

                    readXMLFragment( result, writer );

                }

                rsSummary.close();
                break;
            case full:

                String selectFull = generateSELECTStatement( formatTypeInGenericRecordStore.get( "full" ), constraint,
                                                             profileFormatNumber );
                ResultSet rsFull = conn.createStatement().executeQuery( selectFull );

                doHitsOnGetRecord( writer, typeName, constraint, con, formatTypeInGenericRecordStore.get( "full" ),
                                   "results" );

                while ( rsFull.next() ) {
                    String result = rsFull.getString( 1 );

                    readXMLFragment( result, writer );

                }
                rsFull.close();

                break;
            }// muss dann noch gecatcht werden

            conn.close();

        }

    }

    /**
     * Corrects the table set from the mainDatabaseTable. Because it could happen that the mainDatabaseTable is called
     * in the Filterexpression explicitly.
     * 
     * @param tableSet
     */
    public void correctTable( Set<String> tableSet ) {
        for ( String s : tableSet ) {
            if ( mainDatabaseTable.equals( s ) ) {
                tableSet.remove( s );
                break;
            }
        }

    }

    /**
     * Selectstatement for the constrainted tables.
     * <p>
     * Realisation with AND
     * 
     * @param formatType
     * @param constraint
     * @return
     */
    private String generateSELECTStatement( String formatType, GenericDatabaseDS constraint, int profileFormatNumber ) {
        String s = "";
        if ( constraint.getExpressionWriter() != null ) {
            s += "SELECT " + formatType + ".data " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " + formatType
                 + "." + commonForeignkey + " >= " + constraint.getStartPosition() + " ";
            s += "AND " + formatType + "." + "format = " + profileFormatNumber + " ";

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += " AND " + concatTableWHERE( tableSet );
            }

            s += "AND (" + constraint.getExpressionWriter().toString() + ") LIMIT " + constraint.getMaxRecords();

        } else {
            s += "SELECT " + formatType + ".data " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " + formatType
                 + "." + commonForeignkey + " >= " + constraint.getStartPosition() + " AND " + formatType + "."
                 + "format = " + profileFormatNumber + " LIMIT " + constraint.getMaxRecords();

        }
        System.out.println( s );
        return s;
    }

    /**
     * 
     * Counts the rows that are in the resultset.
     * 
     * @param formatType
     * @param constraint
     * @return
     */
    private String generateCOUNTStatement( String formatType, GenericDatabaseDS constraint, int profileFormatNumber ) {
        String s = "";
        if ( constraint.getExpressionWriter() != null ) {
            s += "SELECT COUNT(" + formatType + ".data) " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " + formatType
                 + "." + commonForeignkey + " >= " + constraint.getStartPosition() + " ";
            s += "AND " + formatType + "." + "format = " + profileFormatNumber + " ";

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += "AND " + concatTableWHERE( tableSet );
            }

            s += "AND (" + constraint.getExpressionWriter().toString() + ") LIMIT " + constraint.getMaxRecords();

        } else {
            s += "SELECT COUNT(" + formatType + ".data) " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " + formatType
                 + "." + commonForeignkey + " >= " + constraint.getStartPosition() + " LIMIT "
                 + constraint.getMaxRecords();

        }
        System.out.println( s );
        return s;
    }

    /**
     * Relates the tables to the main table "datasets".
     * 
     * @param table
     * @return
     */
    private String concatTableWHERE( Set<String> table ) {
        String string = "";
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string += s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND ";
            } else {
                string += s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id ";
            }
        }
        return string;
    }

    /**
     * @param table
     * @return
     */
    private String concatTableFROM( Set<String> table ) {
        String string = "";
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string += s + ", ";
            } else {
                string += s + " ";
            }
        }
        return string;
    }

    /**
     * Reads a valid XML fragment
     * 
     * @param result
     * @param xmlWriter
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    private void readXMLFragment( String result, XMLStreamWriter xmlWriter ) {

        XMLStreamReader xmlReader;
        try {
            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( new StringReader( result ) );

            // skip START_DOCUMENT
            xmlReader.nextTag();

            XMLAdapter.writeElement( xmlWriter, xmlReader );

            xmlReader.close();

        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param returnatbleElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doValidateOnGetRecord( XMLStreamWriter writer, QName typeName,
                                        SetOfReturnableElements returnatbleElement, JDBCConnections con )
                            throws SQLException, XMLStreamException {
        // TODO Auto-generated method stub

    }

    /**
     * Transformation operation for the parsed filter expression. Example for isoqp_title with INNER JOIN
     * 
     * @param constraint
     * @param constraintLanguage
     * @return
     */
    private String transformFilterExpression( ConstraintLanguage constraintLanguage, String constraint ) {

        String isoqp_title = "";
        String rest = "";
        constraint = constraint.replace( "\"", "" );

        for ( String s : constraint.split( " = " ) ) {
            if ( s.equals( "title" ) ) {
                isoqp_title = "isoqp_title";
            } else {
                rest = s;
            }
        }

        String sqlExpression = "INNER JOIN " + isoqp_title + " ON (ds.id = " + isoqp_title + ".fk_datasets) WHERE "
                               + isoqp_title + ".title = " + rest;

        return sqlExpression;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#transaction(javax.xml.stream.XMLStreamWriter,
     * org.deegree.commons.configuration.JDBCConnections, java.util.List)
     */
    @Override
    public void transaction( XMLStreamWriter writer, JDBCConnections connection, TransactionOperation operations )
                            throws SQLException, XMLStreamException {

        Connection conn = ConnectionManager.getConnection( connectionId );

        switch ( operations.getType() ) {
        case INSERT:
            InsertTransaction ins = (InsertTransaction) operations;

            for ( OMElement element : ins.getElement() ) {

                try {

                    ISOQPParsing elementParsing = new ISOQPParsing( element, conn );
                    elementParsing.executeInsertStatement();
                    getRecordsForTransactionInsertStatement( writer, conn, elementParsing.getRecordInsertIDs() );
                } catch ( IOException e ) {

                    e.printStackTrace();
                }

            }

            conn.close();

            break;

        case UPDATE:

            break;

        case DELETE:

            break;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getRecordsById(javax.xml.stream.XMLStreamWriter,
     * org.deegree.commons.configuration.JDBCConnections, java.util.List)
     */
    @Override
    public void getRecordsById( XMLStreamWriter writer, JDBCConnections connection, List<String> idList ) {

    }

    /**
     * Gets the records in dublin core representation for the insert transaction operation.
     * 
     * @param writer
     * @param conn
     * @param insertedIds
     *            the briefrecord datasets that have been inserted into the backend
     * @throws SQLException
     */
    private void getRecordsForTransactionInsertStatement( XMLStreamWriter writer, Connection conn,
                                                          List<Integer> insertedIds )
                            throws SQLException {
        String s = "";
        for ( int i : insertedIds ) {

            s += "SELECT " + "recordbrief" + ".data " + "FROM " + mainDatabaseTable + ", " + "recordbrief" + " ";

            s += "WHERE " + "recordbrief" + "." + commonForeignkey + " = " + mainDatabaseTable + ".id ";

            s += "AND " + "recordbrief" + "." + "id" + " = " + i;
            System.out.println( s );

            ResultSet rsInsertedDatasets = conn.createStatement().executeQuery( s );

            while ( rsInsertedDatasets.next() ) {
                String result = rsInsertedDatasets.getString( 1 );

                readXMLFragment( result, writer );

            }
            rsInsertedDatasets.close();
        }

    }

}
