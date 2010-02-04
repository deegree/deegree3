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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.PooledConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.record.persistence.GenericDatabaseDS;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.RecordStoreException;
import org.deegree.record.persistence.sqltransform.postgres.ExpressionFilterHandling;
import org.deegree.record.persistence.sqltransform.postgres.ExpressionFilterObject;
import org.deegree.record.persistence.sqltransform.postgres.TransformatorPostGres;
import org.deegree.record.publication.DeleteTransaction;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.RecordProperty;
import org.deegree.record.publication.TransactionOperation;
import org.deegree.record.publication.TransactionOptions;
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

    private List<Integer> insertedIds = new ArrayList<Integer>();

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

        typeNames.put( new QName( "", "", "" ), 1 );
        typeNames.put( new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "" ), 1 );
        typeNames.put( new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "csw" ), 1 );
        typeNames.put( new QName( "http://purl.org/dc/elements/1.1/", "", "dc" ), 1 );
        typeNames.put( new QName( "http://www.isotc211.org/2005/gmd", "MD_Metadata", "" ), 2 );
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
        try {
            URL dc = null;
            URL url_identification = null;
            URL url_service_metadata = null;
            BufferedInputStream bais;
            URLConnection urlConn = null;

            if ( typeName.equals( new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "csw" ) ) ) {

                dc = new URL( "http://schemas.opengis.net/csw/2.0.2/record.xsd" );

                urlConn = dc.openConnection();

            } else if ( typeName.equals( new QName( "http://www.isotc211.org/2005/gmd", "MD_Metadata", "gmd" ) ) ) {

                url_identification = new URL( "http://www.isotc211.org/2005/gmd/identification.xsd" );

                urlConn = url_identification.openConnection();

                writer.writeAttribute( "parentSchema", "http://www.isotc211.org/2005/gmd/gmd.xsd" );

            }

            urlConn.setDoInput( true );
            bais = new BufferedInputStream( urlConn.getInputStream() );

            // TODO remove hardcoding
            Charset charset = Charset.forName( "UTF-8" );
            InputStreamReader isr = null;

            isr = new InputStreamReader( bais, charset );

            readXMLFragment( isr, writer );
        } catch ( MalformedURLException e ) {

            e.printStackTrace();
        } catch ( IOException e ) {

            e.printStackTrace();
        } catch ( Exception e ) {

            e.printStackTrace();
        }

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

        int profileFormatNumberOutputSchema = 0;
        int typeNameFormatNumber = 0;

        if ( typeNames.containsKey( typeName ) ) {
            typeNameFormatNumber = typeNames.get( typeName );
        }

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

            doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, constraint, con,
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
    private void doHitsOnGetRecord( XMLStreamWriter writer, int typeNameFormatNumber,
                                    int profileFormatNumberOutputSchema, GenericDatabaseDS constraint,
                                    JDBCConnections con, String formatType, String resultType )
                            throws SQLException, XMLStreamException, IOException {

        int countRows = 0;
        int nextRecord = 0;
        int returnedRecords = 0;

        Writer selectCountRows = generateSELECTStatement( formatType, constraint, typeNameFormatNumber,
                                                          profileFormatNumberOutputSchema, true );

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
        int typeNameFormatNumber = 0;
        if ( typeNames.containsKey( typeName ) ) {
            typeNameFormatNumber = typeNames.get( typeName );
        }

        for ( PooledConnection pool : con.getPooledConnection() ) {

            Connection conn = ConnectionManager.getConnection( connectionId );

            ResultSet rs = null;
            switch ( constraint.getSetOfReturnableElements() ) {

            case brief:

                Writer selectBrief = generateSELECTStatement( formatTypeInGenericRecordStore.get( "brief" ),
                                                              constraint, typeNameFormatNumber,
                                                              profileFormatNumberOutputSchema, false );
                rs = conn.createStatement().executeQuery( selectBrief.toString() );

                doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, constraint, con,
                                   formatTypeInGenericRecordStore.get( "brief" ), "results" );

                break;
            case summary:

                Writer selectSummary = generateSELECTStatement( formatTypeInGenericRecordStore.get( "summary" ),
                                                                constraint, typeNameFormatNumber,
                                                                profileFormatNumberOutputSchema, false );
                rs = conn.createStatement().executeQuery( selectSummary.toString() );

                doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, constraint, con,
                                   formatTypeInGenericRecordStore.get( "summary" ), "results" );

                break;
            case full:

                Writer selectFull = generateSELECTStatement( formatTypeInGenericRecordStore.get( "full" ), constraint,
                                                             typeNameFormatNumber, profileFormatNumberOutputSchema,
                                                             false );
                rs = conn.createStatement().executeQuery( selectFull.toString() );

                doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, constraint, con,
                                   formatTypeInGenericRecordStore.get( "full" ), "results" );

                break;
            }

            if ( rs != null ) {
                writeResultSet( rs, writer );
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
    private Writer generateSELECTStatement( String formatType, GenericDatabaseDS constraint, int typeNameFormatNumber,
                                            int profileFormatNumberOutputSchema, boolean setCount )
                            throws IOException {
        if ( constraint.getTable() != null ) {
            tableSet = constraint.getTable();
        } else {
            tableSet = new HashSet<String>();
        }
        correctTable( tableSet );

        Writer s = new StringWriter();
        Writer constraintExpression = new StringWriter();
        String COUNT_PRE;
        String COUNT_SUF;
        StringWriter stringWriter = (StringWriter) constraint.getExpressionWriter();

        if ( stringWriter.getBuffer().length() != 0 ) {
            constraintExpression.append( "AND (" + constraint.getExpressionWriter().toString() + ") " );
        } else {
            constraintExpression.append( "" );
        }

        if ( setCount == true ) {
            COUNT_PRE = "COUNT(";
            COUNT_SUF = ")";
        } else {
            COUNT_PRE = "";
            COUNT_SUF = "";
        }

        s.append( "SELECT " + COUNT_PRE + formatType + ".data" + COUNT_SUF + " FROM " + formatType + " " );

        s.append( "WHERE " + formatType + ".format = " + profileFormatNumberOutputSchema + " " );

        s.append( "AND " + formatType + ".data IN(" );

        s.append( "SELECT " + formatType + ".data FROM " + mainDatabaseTable + ", " + formatType );

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( ", " + concatTableFROM( tableSet ) );
        }

        s.append( "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " + formatType
                  + ".format = " + typeNameFormatNumber + " AND " + formatType + "." + commonForeignkey + " >= "
                  + constraint.getStartPosition() );

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( " AND " + concatTableWHERE( tableSet ) );
        }
        s.append( constraintExpression + ")" );
        if ( constraint.getMaxRecords() < 0 ) {

        } else {
            s.append( " LIMIT " + constraint.getMaxRecords() );
        }

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
    private Writer concatTableWHERE( Set<String> table )
                            throws IOException {
        Writer string = new StringWriter();
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string.append( s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " );
            } else {
                string.append( s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id " );
            }
        }
        return string;
    }

    /**
     * @param table
     * @return
     * @throws IOException
     */
    private Writer concatTableFROM( Set<String> table )
                            throws IOException {
        Writer string = new StringWriter();
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string.append( s + ", " );
            } else {
                string.append( s + " " );
            }
        }
        return string;
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
    public int transaction( XMLStreamWriter writer, TransactionOperation operations, TransactionOptions options )
                            throws SQLException, XMLStreamException {

        int successfullTransaction = 0;
        Connection conn = ConnectionManager.getConnection( connectionId );

        switch ( operations.getType() ) {
        case INSERT:
            InsertTransaction ins = (InsertTransaction) operations;

            for ( OMElement element : ins.getElement() ) {
                QName localName = element.getQName();
                boolean isDC = true;
                try {
                    ISOQPParsing elementParsing = new ISOQPParsing( element, conn );
                    if ( localName.equals( new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "csw" ) )
                         || localName.equals( new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "" ) ) ) {
                        elementParsing.parseAPDC();

                    } else {
                        elementParsing.parseAPISO( options.isInspire() );
                        isDC = false;
                    }
                    elementParsing.executeInsertStatement( isDC );
                    insertedIds = elementParsing.getRecordInsertIDs();
                    successfullTransaction++;
                } catch ( IOException e ) {
                    successfullTransaction--;
                    e.printStackTrace();
                }

            }

            break;

        case UPDATE:

            UpdateTransaction upd = (UpdateTransaction) operations;
            /**
             * if there should a complete record be updated or some properties
             */
            if ( upd.getElement() != null ) {
                ISOQPParsing elementParsing = new ISOQPParsing( upd.getElement(), conn );
                elementParsing.executeUpdateStatement();
                successfullTransaction++;
            } else {
                try {
                    TransformatorPostGres filterExpression = new TransformatorPostGres( upd.getConstraint() );
                    GenericDatabaseDS gdds = new GenericDatabaseDS( filterExpression.getStringWriter(),
                                                                    ResultType.results, SetOfReturnableElements.full,
                                                                    -1, 1, filterExpression.getTable(),
                                                                    filterExpression.getColumn() );

                    int formatNumber = 0;
                    String nsURI = filterExpression.getPropName().getNamespaceURI();
                    String prefix = filterExpression.getPropName().getPrefix();
                    QName analysedQName = new QName( nsURI, "", prefix );
                    for ( QName qName : typeNames.keySet() ) {
                        if ( qName.equals( analysedQName ) ) {
                            formatNumber = typeNames.get( qName );
                        }
                    }

                    Writer str = getRequestedIDStatement( formatTypeInGenericRecordStore.get( "full" ), gdds,
                                                          formatNumber );

                    ResultSet rsUpdatableDatasets = conn.createStatement().executeQuery( str.toString() );
                    List<Integer> deletableDatasets = new ArrayList<Integer>();
                    while ( rsUpdatableDatasets.next() ) {
                        deletableDatasets.add( rsUpdatableDatasets.getInt( 1 ) );

                    }
                    rsUpdatableDatasets.close();

                    for ( int i : deletableDatasets ) {
                        String stri = "SELECT " + formatTypeInGenericRecordStore.get( "full" ) + ".data FROM "
                                      + formatTypeInGenericRecordStore.get( "full" ) + " WHERE "
                                      + formatTypeInGenericRecordStore.get( "full" ) + ".format = 2 AND "
                                      + formatTypeInGenericRecordStore.get( "full" ) + "." + commonForeignkey + " = "
                                      + i;
                        ResultSet rsGetStoredFullRecordXML = conn.createStatement().executeQuery( stri.toString() );

                        while ( rsGetStoredFullRecordXML.next() ) {
                            for ( RecordProperty recProp : upd.getRecordProperty() ) {
                                ExpressionFilterHandling filterHandle = new ExpressionFilterHandling();
                                ExpressionFilterObject recordPropertyName;
                                ExpressionFilterObject recordPropertyValue;
                                recordPropertyName = filterHandle.expressionFilterHandling(
                                                                                            org.deegree.filter.Expression.Type.PROPERTY_NAME,
                                                                                            recProp.getPropertyName() );
                                recordPropertyValue = filterHandle.expressionFilterHandling(
                                                                                             org.deegree.filter.Expression.Type.LITERAL,
                                                                                             recProp.getReplacementValue() );
                                if ( recordPropertyName.isMatching() == true ) {

                                    // not important. There is just one name possible
                                    for ( String column : recordPropertyName.getColumn() ) {

                                        // creating an OMElement readed from the backend byteData
                                        InputStream in = rsGetStoredFullRecordXML.getBinaryStream( 1 );
                                        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                                                      in );
                                        StAXOMBuilder builder = new StAXOMBuilder( reader );
                                        OMDocument doc = builder.getDocument();
                                        OMElement elementBuiltFromDB = doc.getOMDocumentElement();

                                        OMElement omElement = recursiveElementKnotUpdate(
                                                                                          elementBuiltFromDB,
                                                                                          elementBuiltFromDB.getChildElements(),
                                                                                          column,
                                                                                          recordPropertyValue.getExpression() );

                                        ISOQPParsing elementParsing = new ISOQPParsing( omElement, conn );
                                        elementParsing.executeUpdateStatement();

                                    }

                                } else {

                                    String msg = "No matching found between backend and " + recProp.getPropertyName();
                                    throw new IllegalArgumentException( msg );
                                }
                            }
                        }
                        rsGetStoredFullRecordXML.close();

                    }
                    successfullTransaction++;
                } catch ( IOException e ) {
                    successfullTransaction--;
                    e.printStackTrace();
                }
            }

            break;

        case DELETE:

            DeleteTransaction delete = (DeleteTransaction) operations;
            TransformatorPostGres filterExpression = new TransformatorPostGres( delete.getConstraint() );
            int formatNumber = 0;
            String nsURI = filterExpression.getPropName().getNamespaceURI();
            String prefix = filterExpression.getPropName().getPrefix();
            QName analysedQName = new QName( nsURI, "", prefix );
            for ( QName qName : typeNames.keySet() ) {
                if ( qName.equals( analysedQName ) ) {
                    formatNumber = typeNames.get( qName );
                }
            }

            GenericDatabaseDS gdds = new GenericDatabaseDS( filterExpression.getStringWriter(), ResultType.results,
                                                            SetOfReturnableElements.full, -1, 1,
                                                            filterExpression.getTable(), filterExpression.getColumn() );
            Writer str = new StringWriter();

            try {
                str = getRequestedIDStatement( formatTypeInGenericRecordStore.get( "full" ), gdds, formatNumber );
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ResultSet rsDeletableDatasets = conn.createStatement().executeQuery( str.toString() );
            List<Integer> deletableDatasets = new ArrayList<Integer>();
            while ( rsDeletableDatasets.next() ) {
                deletableDatasets.add( rsDeletableDatasets.getInt( 1 ) );

            }
            rsDeletableDatasets.close();

            for ( int i : deletableDatasets ) {
                String deleteDataset = "DELETE FROM " + mainDatabaseTable + " WHERE id = " + i;
                int deleteRS = conn.createStatement().executeUpdate( deleteDataset );
                successfullTransaction++;
            }

            break;
        }
        conn.close();

        return successfullTransaction;
    }

    /**
     * This method replaces the text content of an elementknot.
     * <p>
     * TODO this is suitable for updates which affect an elementknot that has just one child. <br>
     * BUG - if there a more childs like in the "keyword"-elementknot.
     * 
     * @param element
     *            where to start in the OMTree
     * @param childElements
     *            as an Iterator above all the childElements of the element
     * @param searchForLocalName
     *            is the name that is searched for. This is the elementknot thats content should be updated.
     * @param newContent
     *            is the new content that should be updated
     * @return OMElement
     */
    private OMElement recursiveElementKnotUpdate( OMElement element, Iterator childElements, String searchForLocalName,
                                                  String newContent ) {

        Iterator it = element.getChildrenWithLocalName( searchForLocalName );

        if ( it.hasNext() ) {
            OMElement u = null;
            while ( it.hasNext() ) {
                u = (OMElement) it.next();
                System.out.println( u.toString() );
                u.getFirstElement().setText( newContent.substring( 1, newContent.length() - 1 ) );
                System.out.println( u.toString() );
            }
            return element;

        } else {

            while ( childElements.hasNext() ) {
                OMElement elem = (OMElement) childElements.next();

                recursiveElementKnotUpdate( elem, elem.getChildElements(), searchForLocalName, newContent );

            }

        }

        return element;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getRecordsById(javax.xml.stream.XMLStreamWriter,
     * org.deegree.commons.configuration.JDBCConnections, java.util.List)
     */
    @Override
    public void getRecordsById( XMLStreamWriter writer, List<String> idList, URI outputSchema,
                                SetOfReturnableElements elementSetName )
                            throws SQLException {

        Connection conn = ConnectionManager.getConnection( connectionId );
        int profileFormatNumberOutputSchema = 0;

        for ( QName qName : typeNames.keySet() ) {
            if ( qName.getNamespaceURI().equals( outputSchema.toString() ) ) {
                profileFormatNumberOutputSchema = typeNames.get( qName );
            }
        }

        ResultSet rs = null;
        for ( String identifier : idList ) {
            switch ( elementSetName ) {

            case brief:

                String selectBrief = "SELECT rb.data FROM recordbrief AS rb, datasets AS ds, qp_identifier AS i WHERE rb.fk_datasets = ds.id AND i.fk_datasets = ds.id AND i.identifier = '"
                                     + identifier + "' AND rb.format = " + profileFormatNumberOutputSchema + ";";
                rs = conn.createStatement().executeQuery( selectBrief );
                break;
            case summary:

                String selectSummary = "SELECT rs.data FROM recordsummary AS rs, datasets AS ds, qp_identifier AS i WHERE rs.fk_datasets = ds.id AND i.fk_datasets = ds.id AND i.identifier = '"
                                       + identifier + "' AND rs.format = " + profileFormatNumberOutputSchema + ";";
                rs = conn.createStatement().executeQuery( selectSummary );
                break;
            case full:

                String selectFull = "SELECT rf.data FROM recordfull AS rf, datasets AS ds, qp_identifier AS i WHERE rf.fk_datasets = ds.id AND i.fk_datasets = ds.id AND i.identifier = '"
                                    + identifier + "' AND rf.format = " + profileFormatNumberOutputSchema + ";";
                rs = conn.createStatement().executeQuery( selectFull );
                break;
            }

            writeResultSet( rs, writer );

        }

        // try {
        // doResultsOnGetRecord(writer, null, profileFormatNumberOutputSchema, null, connection);
        // } catch ( SQLException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch ( XMLStreamException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

    /**
     * Prepares the statement to get all the central recordIDs for a statement.
     * 
     * @param formatType
     * @param constraint
     * @param formatNumber
     * @return
     * @throws IOException
     */
    private Writer getRequestedIDStatement( String formatType, GenericDatabaseDS constraint, int formatNumber )
                            throws IOException {
        if ( constraint.getTable() != null ) {
            tableSet = constraint.getTable();
        } else {
            tableSet = new HashSet<String>();
        }
        correctTable( tableSet );
        StringWriter s = new StringWriter();
        Writer constraintExpression = new StringWriter();

        StringWriter stringWriter = (StringWriter) constraint.getExpressionWriter();

        if ( stringWriter.getBuffer().length() != 0 ) {
            constraintExpression.append( "AND (" + constraint.getExpressionWriter().toString() + ") " );
        } else {
            constraintExpression.append( "" );
        }

        s.append( "SELECT " + formatType + "." + commonForeignkey + " FROM " + mainDatabaseTable + ", " + formatType );

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( ", " + concatTableFROM( tableSet ) );
        }

        s.append( "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND " + formatType
                  + "." + commonForeignkey + " >= " + constraint.getStartPosition() + " AND " + formatType
                  + ".format = " + formatNumber );

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( " AND " + concatTableWHERE( tableSet ) );
        }
        s.append( constraintExpression + "" );

        return s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.record.persistence.RecordStore#getRecordsForTransactionInsertStatement(javax.xml.stream.XMLStreamWriter
     * )
     */
    @Override
    public void getRecordsForTransactionInsertStatement( XMLStreamWriter writer )
                            throws SQLException, IOException {
        Connection conn = ConnectionManager.getConnection( connectionId );
        for ( int i : insertedIds ) {
            Writer s = new StringWriter();
            s.append( " SELECT " + "recordbrief" + ".data " + "FROM " + mainDatabaseTable + ", " + "recordbrief" + " " );

            s.append( " WHERE " + "recordbrief" + "." + commonForeignkey + " = " + mainDatabaseTable + ".id " );

            s.append( " AND " + "recordbrief" + "." + "id" + " = " + i );
            System.out.println( s );

            ResultSet rsInsertedDatasets = conn.createStatement().executeQuery( s.toString() );

        }

    }

    /**
     * This method writes the resultSet from the database to the writer.
     * 
     * @param resultSet
     *            that should search the backend
     * @param writer
     *            that writes the data to the output
     * @throws SQLException
     */
    private void writeResultSet( ResultSet resultSet, XMLStreamWriter writer )
                            throws SQLException {

        while ( resultSet.next() ) {
            BufferedInputStream bais = new BufferedInputStream( resultSet.getBinaryStream( 1 ) );
            // ByteArrayInputStream bais2 = new ByteArrayInputStream( rs.getBytes( 1 ) );

            // TODO remove hardcoding
            Charset charset = Charset.forName( "UTF-8" );
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader( bais, charset );
            } catch ( Exception e ) {

                e.printStackTrace();
            }

            readXMLFragment( isr, writer );

        }
        resultSet.close();

    }

    /**
     * Reads a valid XML fragment TODO change fileOutput back into streamWriter
     * 
     * @param
     * @param xmlWriter
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    private void readXMLFragment( InputStreamReader isr, XMLStreamWriter xmlWriter ) {

        // XMLStreamReader xmlReaderOut;

        XMLStreamReader xmlReader;
        try {
            // FileOutputStream fout = new FileOutputStream("/home/thomas/Desktop/test.xml");
            // XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter( fout );

            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( isr );

            // skip START_DOCUMENT
            xmlReader.nextTag();

            // XMLAdapter.writeElement( out, xmlReader );

            XMLAdapter.writeElement( xmlWriter, xmlReader );
            // fout.close();
            xmlReader.close();

        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            e.printStackTrace();
        }
        // catch ( FileNotFoundException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

}
