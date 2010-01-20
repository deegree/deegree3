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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.PooledConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.record.persistence.GenericDatabaseDS;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.RecordStoreException;
import org.deegree.record.persistence.sqltransform.postgres.TransformatorPostGres;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.TransactionOperation;
import org.deegree.record.publication.UpdateTransaction;

/**
 * {@link RecordStore} implementation of Dublin Core and ISO Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ISORecordStore implements RecordStore {

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
        typeNames.put( new QName( "http://purl.org/dc/elements/1.1/", "", "dc" ), 1 );
        typeNames.put( new QName( "http://www.isotc211.org/2005/gmd", "MD_Metadata", "gmd" ), 2 );
        typeNames.put( new QName( "http://www.opengis.net/cat/csw/apiso/1.0", "", "apiso" ), 2 );
        
    }

    public ISORecordStore( String connectionId ) {
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
                url = ISORecordStore.class.getResource( "dc.xsd" );

            } else {
                // in = new FileInputStream( "../gmd/gmd.xsd" );
                url = ISORecordStore.class.getResource( "gmd_metadata.xsd" );
            }
        }

        XMLAdapter ada = new XMLAdapter( url );

        System.out.println( ada.toString() );
        // readXMLFragment( ada.toString(), writer );
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
    public void getRecords( XMLStreamWriter writer, QName typeName, URI outputSchema, JDBCConnections con,
                            GenericDatabaseDS constraint )
                            throws SQLException, XMLStreamException, IOException {

        if ( constraint.getTable() != null ) {
            tableSet = constraint.getTable();
        } else {
            tableSet = new HashSet<String>();
        }
        correctTable( tableSet );
        int profileFormatNumberOutputSchema = 0;

        if ( typeName.getNamespaceURI().equals( outputSchema ) ) {

        } else {
            for ( QName qName : typeNames.keySet() ) {
                if ( qName.getNamespaceURI().equals( outputSchema.toString() ) ) {
                    profileFormatNumberOutputSchema = typeNames.get( qName );
                }
            }
        }

        switch ( constraint.getResultType() ) {
        case results:

            doResultsOnGetRecord( writer, typeName, profileFormatNumberOutputSchema, constraint, con );
            break;
        case hits:

            doHitsOnGetRecord( writer, typeName, profileFormatNumberOutputSchema, constraint, con,
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
     * @param profileFormatNumberOutputSchema
     * @param constraint
     * @param con
     * @param formatType
     * @param resultType
     * @throws SQLException
     * @throws XMLStreamException
     * @throws IOException 
     */
    private void doHitsOnGetRecord( XMLStreamWriter writer, QName typeName, int profileFormatNumberOutputSchema,
                                    GenericDatabaseDS constraint, JDBCConnections con, String formatType,
                                    String resultType )
                            throws SQLException, XMLStreamException, IOException {

        int countRows = 0;
        int nextRecord = 0;
        int returnedRecords = 0;


        Writer selectCountRows = generateSELECTStatement( formatType, constraint, profileFormatNumberOutputSchema, true );

        // ConnectionManager.addConnections( con );
        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );
            ResultSet rs = conn.createStatement().executeQuery( selectCountRows.toString() );

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
     * @param profileFormatNumberOutputSchema
     * @param constraint
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     * @throws IOException 
     */
    private void doResultsOnGetRecord( XMLStreamWriter writer, QName typeName, int profileFormatNumberOutputSchema,
                                       GenericDatabaseDS constraint, JDBCConnections con )
                            throws SQLException, XMLStreamException, IOException {

        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );

            ResultSet rs = null;
            switch ( constraint.getSetOfReturnableElements() ) {

            case brief:

                Writer selectBrief = generateSELECTStatement( formatTypeInGenericRecordStore.get( "brief" ),
                                                              constraint, profileFormatNumberOutputSchema, false );
                rs = conn.createStatement().executeQuery( selectBrief.toString() );

                doHitsOnGetRecord( writer, typeName, profileFormatNumberOutputSchema, constraint, con,
                                   formatTypeInGenericRecordStore.get( "brief" ), "results" );

                break;
            case summary:

                Writer selectSummary = generateSELECTStatement( formatTypeInGenericRecordStore.get( "summary" ),
                                                                constraint, profileFormatNumberOutputSchema, false );
                rs = conn.createStatement().executeQuery( selectSummary.toString() );

                doHitsOnGetRecord( writer, typeName, profileFormatNumberOutputSchema, constraint, con,
                                   formatTypeInGenericRecordStore.get( "summary" ), "results" );

                break;
            case full:

                Writer selectFull = generateSELECTStatement( formatTypeInGenericRecordStore.get( "full" ), constraint,
                                                             profileFormatNumberOutputSchema, false );
                rs = conn.createStatement().executeQuery( selectFull.toString() );

                doHitsOnGetRecord( writer, typeName, profileFormatNumberOutputSchema, constraint, con,
                                   formatTypeInGenericRecordStore.get( "full" ), "results" );

                break;
            }

            if ( rs != null ) {
                while ( rs.next() ) {

                    BufferedInputStream bais = new BufferedInputStream( rs.getBinaryStream( 1 ) );
                    //ByteArrayInputStream bais2 = new ByteArrayInputStream( rs.getBytes( 1 ) );

                    //TODO remove hardcoding 
                    Charset charset = Charset.forName( "UTF-8" );
                    InputStreamReader isr = null;
                    try {
                        isr = new InputStreamReader( bais, charset );
                    } catch ( Exception e ) {
                        
                        e.printStackTrace();
                    }
                    
                    
                    
                    readXMLFragment( isr, writer );

                }
                rs.close();
            }

            conn.close();

        }

    }

    /**
     * Corrects the table set from the mainDatabaseTable. Because it could happen that the mainDatabaseTable is called
     * in the Filterexpression explicitly.
     * 
     * @param tableSet
     */
    private void correctTable( Set<String> tableSet ) {
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
     * @throws IOException 
     */
    private Writer generateSELECTStatement( String formatType, GenericDatabaseDS constraint,
                                            int profileFormatNumberOutputSchema, boolean setCount ) throws IOException {
        Writer s = new StringWriter();
        Writer constraintExpression = new StringWriter();
        String COUNT_PRE;
        String COUNT_SUF;
        if ( !constraint.getExpressionWriter().equals( null) ) {
            constraintExpression.append( "AND (" + constraint.getExpressionWriter().toString() + ") ");
        } else {
            constraintExpression.append( "");
        }

        if ( setCount == true ) {
            COUNT_PRE = "COUNT(";
            COUNT_SUF = ")";
        } else {
            COUNT_PRE = "";
            COUNT_SUF = "";
        }

        s.append( "SELECT " + COUNT_PRE + formatType + ".data" + COUNT_SUF + " FROM " + formatType + " ");

        s.append( "WHERE " + formatType + ".format = " + profileFormatNumberOutputSchema + " ");

        s.append( "AND " + formatType + ".data IN(");

        s.append( "SELECT " + formatType + ".data FROM " + mainDatabaseTable + ", " + formatType);

        if ( tableSet.size() == 0 ) {
            s.append( ' ');
        } else {
            s.append( ", " + concatTableFROM( tableSet ));
        }

        s.append( "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " + formatType + "."
             + commonForeignkey + " >= " + constraint.getStartPosition());

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( " AND " + concatTableWHERE( tableSet ));
        }

        s.append( constraintExpression + ")" + " LIMIT " + constraint.getMaxRecords() );

        System.out.println( s );
        return s;
    }

    /**
     * Relates the tables to the main table "datasets".
     * 
     * @param table
     * @return
     * @throws IOException 
     */
    private Writer concatTableWHERE( Set<String> table ) throws IOException {
        Writer string = new StringWriter();
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string.append( s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND ");
            } else {
                string.append( s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id ");
            }
        }
        return string;
    }

    /**
     * @param table
     * @return
     * @throws IOException 
     */
    private Writer concatTableFROM( Set<String> table ) throws IOException {
        Writer string = new StringWriter();
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string.append( s + ", ");
            } else {
                string.append( s + " ");
            }
        }
        return string;
    }

    /**
     * Reads a valid XML fragment
     * TODO change fileOutput back into streamWriter
     * @param
     * @param xmlWriter
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    private void readXMLFragment( InputStreamReader isr, XMLStreamWriter xmlWriter ) {
        
       
        
        
        //XMLStreamReader xmlReaderOut;
        
        XMLStreamReader xmlReader;
        try {
            //FileOutputStream fout = new FileOutputStream("/home/thomas/Desktop/test.xml");
            //XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter( fout );
            
            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( isr );
            

            // skip START_DOCUMENT
            xmlReader.nextTag();
            
            
            //XMLAdapter.writeElement( out, xmlReader );

            XMLAdapter.writeElement( xmlWriter, xmlReader );
            //fout.close();
            xmlReader.close();

        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            e.printStackTrace();
        }
//        catch ( FileNotFoundException e ) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch ( IOException e ) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

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

            break;

        case UPDATE:
            
            UpdateTransaction upd = (UpdateTransaction) operations;
            if(upd.getElement() != null){
                ISOQPParsing elementParsing = new ISOQPParsing( upd.getElement(), conn );
                elementParsing.executeUpdateStatement();
            }else{
                Writer stream = new StringWriter();
                XMLStreamWriter xmlUpdateWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
                try {
                    TransformatorPostGres filterExpression = new TransformatorPostGres( upd.getConstraint() );
                    GenericDatabaseDS gdds = new GenericDatabaseDS( stream, ResultType.results,
                                                                    SetOfReturnableElements.full, 100, 1,
                                                                    filterExpression.getTable(), filterExpression.getColumn() );
                    
                    getRecords(xmlUpdateWriter, upd.getTypeName(), URI.create( upd.getTypeName().getNamespaceURI()), connection, gdds);
                    
                    InputStream in = getClass().getResourceAsStream( xmlUpdateWriter.toString() );
                    XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( in );
                    StAXOMBuilder builder = new StAXOMBuilder( reader );
                    OMDocument doc = builder.getDocument();
                    OMElement omElement = doc.getOMDocumentElement();
                
                
                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            
            

            break;

        case DELETE:
            
            break;
        }
        conn.close();

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
     * Gets the records in dublin core representation for the insert transaction operation. If there is an INSERT
     * statement in the transaction operation there must be a brief representation of this inserted record presented in
     * the response.
     * 
     * @param writer
     * @param conn
     * @param insertedIds
     *            the briefrecord datasets that have been inserted into the backend
     * @throws SQLException
     * @throws IOException 
     */
    private void getRecordsForTransactionInsertStatement( XMLStreamWriter writer, Connection conn,
                                                          List<Integer> insertedIds )
                            throws SQLException, IOException {

        for ( int i : insertedIds ) {
            Writer s = new StringWriter();
            s.append( " SELECT " + "recordbrief" + ".data " + "FROM " + mainDatabaseTable + ", " + "recordbrief" + " ");

            s.append(  " WHERE " + "recordbrief" + "." + commonForeignkey + " = " + mainDatabaseTable + ".id ");

            s.append( " AND " + "recordbrief" + "." + "id" + " = " + i);
            System.out.println( s );

            ResultSet rsInsertedDatasets = conn.createStatement().executeQuery( s.toString() );

            while ( rsInsertedDatasets.next() ) {
                ByteArrayInputStream bais = new ByteArrayInputStream( rsInsertedDatasets.getBytes( 1 ) );

                Charset charset = Charset.forName( "UTF-8" );
                InputStreamReader isr = null;
                
                    isr = new InputStreamReader( bais, charset );
                

                readXMLFragment( isr, writer );

            }
            rsInsertedDatasets.close();
        }

    }

}
