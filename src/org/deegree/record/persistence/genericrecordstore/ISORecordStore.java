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

import java.io.BufferedInputStream;
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
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.RecordStoreException;
import org.deegree.record.persistence.RecordStoreOptions;
import org.deegree.record.persistence.sqltransform.postgres.ExpressionFilterHandling;
import org.deegree.record.persistence.sqltransform.postgres.ExpressionFilterObject;
import org.deegree.record.persistence.sqltransform.postgres.TransformatorPostGres;
import org.deegree.record.publication.DeleteTransaction;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.RecordProperty;
import org.deegree.record.publication.TransactionOperation;
import org.deegree.record.publication.TransactionOptions;
import org.deegree.record.publication.UpdateTransaction;
import org.slf4j.Logger;

/**
 * {@link RecordStore} implementation of Dublin Core and ISO Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ISORecordStore implements RecordStore {

    private static final Logger LOG = getLogger( ISORecordStore.class );

    /**
     * registers the typeNames that are applicable to this recordStore and maps a typeName to a format, if it is DC or
     * ISO
     */
    private static Map<QName, Integer> typeNames = new HashMap<QName, Integer>();

    private String connectionId;

    private ISO_DC_Mappings mappings = new ISO_DC_Mappings();

    private Set<String> tableSet;

    /**
     * maps the specific returnable element format to a concrete table in the backend
     */
    private static final Map<SetOfReturnableElements, String> formatTypeInISORecordStore = new HashMap<SetOfReturnableElements, String>();

    static {

        formatTypeInISORecordStore.put( SetOfReturnableElements.brief, "recordbrief" );
        formatTypeInISORecordStore.put( SetOfReturnableElements.summary, "recordsummary" );
        formatTypeInISORecordStore.put( SetOfReturnableElements.full, "recordfull" );

        typeNames.put( new QName( "", "", "" ), 1 );
        typeNames.put( new QName( CSWConstants.CSW_202_NS, "Record", "" ), 1 );
        typeNames.put( new QName( CSWConstants.CSW_202_NS, "Record", CSWConstants.CSW_PREFIX ), 1 );
        typeNames.put( new QName( "http://purl.org/dc/elements/1.1/", "", "dc" ), 1 );
        typeNames.put( new QName( CSWConstants.GMD_NS, "MD_Metadata", "" ), 2 );
        typeNames.put( new QName( CSWConstants.GMD_NS, "MD_Metadata", CSWConstants.GMD_PREFIX ), 2 );
        typeNames.put( new QName( "http://www.opengis.net/cat/csw/apiso/1.0", "", "apiso" ), 2 );

    }

    /**
     * Creates a new {@link ISORecordStore} instance with a registered connectionId.
     * 
     * @param connectionId
     */
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

            BufferedInputStream bais;
            URLConnection urlConn = null;

            if ( "".equals( typeName ) ) {

                urlConn = new URL( CSWConstants.CSW_202_RECORD ).openConnection();

                urlConn = new URL( "http://www.isotc211.org/2005/gmd/identification.xsd" ).openConnection();

                writer.writeAttribute( "parentSchema", "http://www.isotc211.org/2005/gmd/gmd.xsd" );

            } else if ( typeName.equals( new QName( CSWConstants.CSW_202_NS, "Record", CSWConstants.CSW_PREFIX ) ) ) {

                urlConn = new URL( CSWConstants.CSW_202_RECORD ).openConnection();

            } else if ( typeName.equals( new QName( CSWConstants.GMD_NS, "MD_Metadata", CSWConstants.GMD_PREFIX ) ) ) {

                urlConn = new URL( "http://www.isotc211.org/2005/gmd/identification.xsd" ).openConnection();

                writer.writeAttribute( "parentSchema", "http://www.isotc211.org/2005/gmd/gmd.xsd" );

            } else {
                String errorMessage = "The typeName " + typeName + "is not supported by this profile. ";
                LOG.debug( errorMessage );
                throw new IllegalArgumentException( errorMessage );
            }

            urlConn.setDoInput( true );
            bais = new BufferedInputStream( urlConn.getInputStream() );

            // TODO remove hardcoding
            Charset charset = Charset.forName( "UTF-8" );
            InputStreamReader isr = new InputStreamReader( bais, charset );

            readXMLFragment( isr, writer );
        } catch ( MalformedURLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( IOException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( Exception e ) {

            LOG.debug( "error: " + e.getMessage(), e );
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
    public void getRecords( XMLStreamWriter writer, QName typeName, URI outputSchema, RecordStoreOptions constraint )
                            throws SQLException, XMLStreamException, IOException {

        int profileFormatNumberOutputSchema = 0;
        int typeNameFormatNumber = 0;

        if ( typeNames.containsKey( typeName ) ) {
            typeNameFormatNumber = typeNames.get( typeName );
        }

        if ( !typeName.getNamespaceURI().equals( outputSchema ) ) {
            for ( QName qName : typeNames.keySet() ) {
                if ( qName.getNamespaceURI().equals( outputSchema.toString() ) ) {
                    profileFormatNumberOutputSchema = typeNames.get( qName );
                }
            }
        }

        switch ( constraint.getResultType() ) {
        case results:

            doResultsOnGetRecord( writer, typeName, profileFormatNumberOutputSchema, constraint );
            break;
        case hits:

            doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, constraint,
                               formatTypeInISORecordStore.get( constraint.getSetOfReturnableElements() ),
                               ResultType.hits );
            break;

        case validate:

        }

    }

    /**
     * The mandatory "resultType" attribute in the GetRecords operation is set to "hits".
     * 
     * @param writer
     *            - the XMLStreamWriter
     * @param typeName
     *            - the requested typeName
     * @param profileFormatNumberOutputSchema
     *            - the format number of the outputSchema
     * @param propertyAttributes
     *            - the properties that are identified by the request
     * @param con
     *            - the JDBCConnection
     * @throws SQLException
     * @throws XMLStreamException
     * @throws IOException
     */
    private void doHitsOnGetRecord( XMLStreamWriter writer, int typeNameFormatNumber,
                                    int profileFormatNumberOutputSchema, RecordStoreOptions constraint,
                                    String formatType, ResultType resultType )
                            throws SQLException, XMLStreamException, IOException {

        int countRows = 0;
        int nextRecord = 0;
        int returnedRecords = 0;

        Writer selectCountRows = generateSELECTStatement( formatType, constraint, typeNameFormatNumber,
                                                          profileFormatNumberOutputSchema, true );

        // ConnectionManager.addConnections( con );

        Connection conn = ConnectionManager.getConnection( connectionId );
        ResultSet rs = conn.createStatement().executeQuery( selectCountRows.toString() );

        while ( rs.next() ) {
            countRows = rs.getInt( 1 );
            LOG.debug( "rs: " + rs.getInt( 1 ) );
        }

        if ( resultType.equals( ResultType.hits ) ) {
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

    /**
     * The mandatory "resultType" attribute in the GetRecords operation is set to "results".
     * 
     * @param writer
     *            - the XMLStreamWriter
     * @param typeName
     *            - the requested typeName
     * @param profileFormatNumberOutputSchema
     *            - the format number of the outputSchema
     * @param propertyAttributes
     *            - the properties that are identified by the request
     * @param con
     *            - the JDBCConnection
     * @throws SQLException
     * @throws XMLStreamException
     * @throws IOException
     */
    private void doResultsOnGetRecord( XMLStreamWriter writer, QName typeName, int profileFormatNumberOutputSchema,
                                       RecordStoreOptions propertyAttributes )
                            throws SQLException, XMLStreamException, IOException {
        int typeNameFormatNumber = 0;
        if ( typeNames.containsKey( typeName ) ) {
            typeNameFormatNumber = typeNames.get( typeName );
        }

        Connection conn = ConnectionManager.getConnection( connectionId );

        ResultSet rs = null;
        switch ( propertyAttributes.getSetOfReturnableElements() ) {

        case brief:

            Writer selectBrief = generateSELECTStatement(
                                                          formatTypeInISORecordStore.get( CSWConstants.SetOfReturnableElements.brief ),
                                                          propertyAttributes, typeNameFormatNumber,
                                                          profileFormatNumberOutputSchema, false );
            rs = conn.createStatement().executeQuery( selectBrief.toString() );

            doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, propertyAttributes,
                               formatTypeInISORecordStore.get( CSWConstants.SetOfReturnableElements.brief ),
                               ResultType.results );

            break;
        case summary:

            Writer selectSummary = generateSELECTStatement(
                                                            formatTypeInISORecordStore.get( CSWConstants.SetOfReturnableElements.summary ),
                                                            propertyAttributes, typeNameFormatNumber,
                                                            profileFormatNumberOutputSchema, false );
            rs = conn.createStatement().executeQuery( selectSummary.toString() );

            doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, propertyAttributes,
                               formatTypeInISORecordStore.get( CSWConstants.SetOfReturnableElements.summary ),
                               ResultType.results );

            break;
        case full:

            Writer selectFull = generateSELECTStatement(
                                                         formatTypeInISORecordStore.get( CSWConstants.SetOfReturnableElements.full ),
                                                         propertyAttributes, typeNameFormatNumber,
                                                         profileFormatNumberOutputSchema, false );
            rs = conn.createStatement().executeQuery( selectFull.toString() );

            doHitsOnGetRecord( writer, typeNameFormatNumber, profileFormatNumberOutputSchema, propertyAttributes,
                               formatTypeInISORecordStore.get( CSWConstants.SetOfReturnableElements.full ),
                               ResultType.results );

            break;
        }

        if ( rs != null ) {
            writeResultSet( rs, writer );
        }

        conn.close();

    }

    /**
     * Corrects the table set from the mainDatabaseTable. Because it could happen that the mainDatabaseTable is called
     * in the Filterexpression explicitly.
     * 
     * @param tableSetToBeCorrected
     */
    private void correctTable( Set<String> tableSetToBeCorrected ) {
        for ( String s : tableSetToBeCorrected ) {
            if ( mappings.mainDatabaseTable.equals( s ) ) {
                tableSetToBeCorrected.remove( s );
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
     *            - brief, summary or full
     * @param propertyAttributes
     *            - properties that were requested
     * @param typeNameFormatNumber
     *            - the format number that is identified by the requested typeName
     * @param profileFormatNumberOutputSchema
     *            - the format number that is identified by the requested output schema
     * @param setCount
     *            - if the COUNT method should be in the statement
     * @return a Writer
     * @throws IOException
     */
    private Writer generateSELECTStatement( String formatType, RecordStoreOptions propertyAttributes,
                                            int typeNameFormatNumber, int profileFormatNumberOutputSchema,
                                            boolean setCount )
                            throws IOException {
        if ( propertyAttributes.getTables() != null ) {
            tableSet = propertyAttributes.getTables();
        } else {
            tableSet = new HashSet<String>();
        }
        correctTable( tableSet );

        Writer s = new StringWriter();
        Writer constraintExpression = new StringWriter();
        String COUNT_PRE;
        String COUNT_SUF;
        StringWriter stringWriter = (StringWriter) propertyAttributes.getExpressionWriter();

        if ( stringWriter.getBuffer().length() != 0 ) {
            constraintExpression.append( "AND (" + propertyAttributes.getExpressionWriter().toString() + ") " );
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

        s.append( "SELECT " + formatType + ".data FROM " + mappings.mainDatabaseTable + ", " + formatType );

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( ", " + concatTableFROM( tableSet ) );
        }

        s.append( "WHERE " + formatType + "." + mappings.commonForeignkey + " = " + mappings.mainDatabaseTable
                  + ".id AND " + formatType + ".format = " + typeNameFormatNumber + " AND " + formatType + "."
                  + mappings.commonForeignkey + " >= " + propertyAttributes.getStartPosition() );

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( " AND " + concatTableWHERE( tableSet ) );
        }
        s.append( constraintExpression + ")" );
        if ( propertyAttributes.getMaxRecords() != 0 ) {
            s.append( " LIMIT " + propertyAttributes.getMaxRecords() );
        }

        LOG.debug( "rs: " + s );
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
                string.append( s + "." + mappings.commonForeignkey + " = " + mappings.mainDatabaseTable + ".id AND " );
            } else {
                string.append( s + "." + mappings.commonForeignkey + " = " + mappings.mainDatabaseTable + ".id " );
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

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#transaction(javax.xml.stream.XMLStreamWriter,
     * org.deegree.commons.configuration.JDBCConnections, java.util.List)
     */
    @Override
    public List<Integer> transaction( XMLStreamWriter writer, TransactionOperation operations,
                                      TransactionOptions options )
                            throws SQLException, XMLStreamException {

        List<Integer> insertedIds = new ArrayList<Integer>();

        Connection conn = ConnectionManager.getConnection( connectionId );

        switch ( operations.getType() ) {
        case INSERT:
            InsertTransaction ins = (InsertTransaction) operations;

            for ( OMElement element : ins.getElement() ) {
                QName localName = element.getQName();
                boolean isDC = true;
                try {
                    ISOQPParsing elementParsing = new ISOQPParsing( element );

                    if ( localName.equals( new QName( CSWConstants.CSW_202_NS, "Record", CSWConstants.CSW_PREFIX ) )
                         || localName.equals( new QName( CSWConstants.CSW_202_NS, "Record", "" ) ) ) {

                        elementParsing.parseAPDC();

                    } else {
                        elementParsing.parseAPISO( options.isInspire(), conn );
                        isDC = false;
                    }
                    elementParsing.executeInsertStatement( isDC, conn );
                    insertedIds.addAll( elementParsing.getRecordsAffectedIDs() );

                } catch ( IOException e ) {

                    LOG.debug( "error: " + e.getMessage(), e );
                }

            }
            break;

        case UPDATE:

            UpdateTransaction upd = (UpdateTransaction) operations;
            /**
             * if there should a complete record be updated or some properties
             */
            if ( upd.getElement() != null ) {
                try {
                    QName localName = upd.getElement().getQName();

                    ISOQPParsing elementParsing = new ISOQPParsing( upd.getElement() );

                    if ( localName.equals( new QName( CSWConstants.CSW_202_NS, "Record", CSWConstants.CSW_PREFIX ) )
                         || localName.equals( new QName( CSWConstants.CSW_202_NS, "Record", "" ) ) ) {

                        elementParsing.parseAPDC();

                    } else {
                        elementParsing.parseAPISO( options.isInspire(), conn );

                    }
                    elementParsing.executeUpdateStatement( conn );

                } catch ( IOException e ) {

                    LOG.debug( "error: " + e.getMessage(), e );
                }
            } else {
                try {
                    TransformatorPostGres filterExpression = new TransformatorPostGres( upd.getConstraint() );
                    RecordStoreOptions gdds = new RecordStoreOptions( filterExpression.getStringWriter(),
                                                                      ResultType.results, SetOfReturnableElements.full,
                                                                      filterExpression.getTable(),
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

                    Writer str = getRequestedIDStatement(
                                                          formatTypeInISORecordStore.get( SetOfReturnableElements.full ),
                                                          gdds, formatNumber );

                    ResultSet rsUpdatableDatasets = conn.createStatement().executeQuery( str.toString() );
                    List<Integer> deletableDatasets = new ArrayList<Integer>();
                    while ( rsUpdatableDatasets.next() ) {
                        deletableDatasets.add( rsUpdatableDatasets.getInt( 1 ) );

                    }
                    rsUpdatableDatasets.close();
                    if ( deletableDatasets.size() == 0 ) {
                        // String msg = "No matching found between backend and " + recProp.getPropertyName();
                        // throw new IllegalArgumentException( msg );
                    } else {
                        for ( int i : deletableDatasets ) {
                            String stri = "SELECT " + formatTypeInISORecordStore.get( SetOfReturnableElements.full )
                                          + ".data FROM "
                                          + formatTypeInISORecordStore.get( SetOfReturnableElements.full ) + " WHERE "
                                          + formatTypeInISORecordStore.get( SetOfReturnableElements.full )
                                          + ".format = 2 AND "
                                          + formatTypeInISORecordStore.get( SetOfReturnableElements.full ) + "."
                                          + mappings.commonForeignkey + " = " + i;
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

                                            try {
                                                QName localName = omElement.getQName();

                                                ISOQPParsing elementParsing = new ISOQPParsing( omElement );

                                                if ( localName.equals( new QName( CSWConstants.CSW_202_NS, "Record",
                                                                                  CSWConstants.CSW_PREFIX ) )
                                                     || localName.equals( new QName( CSWConstants.CSW_202_NS, "Record",
                                                                                     "" ) ) ) {

                                                    elementParsing.parseAPDC();

                                                } else {
                                                    elementParsing.parseAPISO( options.isInspire(), conn );

                                                }
                                                elementParsing.executeUpdateStatement( conn );

                                            } catch ( IOException e ) {

                                                LOG.debug( "error: " + e.getMessage(), e );
                                            }

                                        }

                                    } else {

                                        String msg = "No matching found between backend and "
                                                     + recProp.getPropertyName();
                                        throw new IllegalArgumentException( msg );
                                    }
                                }
                            }
                            rsGetStoredFullRecordXML.close();

                        }
                    }

                } catch ( IOException e ) {

                    LOG.debug( "error: " + e.getMessage(), e );
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

            RecordStoreOptions gdds = new RecordStoreOptions( filterExpression.getStringWriter(), ResultType.results,
                                                              SetOfReturnableElements.full,
                                                              filterExpression.getTable(), filterExpression.getColumn() );
            Writer str = new StringWriter();

            try {
                str = getRequestedIDStatement( formatTypeInISORecordStore.get( SetOfReturnableElements.full ), gdds,
                                               formatNumber );
            } catch ( IOException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            }

            ResultSet rsDeletableDatasets = conn.createStatement().executeQuery( str.toString() );
            List<Integer> deletableDatasets = new ArrayList<Integer>();
            while ( rsDeletableDatasets.next() ) {
                deletableDatasets.add( rsDeletableDatasets.getInt( 1 ) );

            }
            rsDeletableDatasets.close();

            for ( int i : deletableDatasets ) {
                conn.createStatement().executeUpdate( "DELETE FROM " + mappings.mainDatabaseTable + " WHERE id = " + i );
            }

            break;
        }
        conn.close();

        return insertedIds;
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
                LOG.debug( u.toString() );
                u.getFirstElement().setText( newContent.substring( 1, newContent.length() - 1 ) );
                LOG.debug( u.toString() );
            }
            return element;

        }
        while ( childElements.hasNext() ) {
            OMElement elem = (OMElement) childElements.next();

            recursiveElementKnotUpdate( elem, elem.getChildElements(), searchForLocalName, newContent );

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
    public void getRecordById( XMLStreamWriter writer, List<String> idList, URI outputSchema,
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

                String selectBrief = "SELECT rb.data FROM "
                                     + formatTypeInISORecordStore.get( SetOfReturnableElements.brief ) + " AS rb, "
                                     + mappings.mainDatabaseTable + " AS ds, qp_identifier AS i WHERE rb."
                                     + mappings.commonForeignkey + " = ds.id AND i." + mappings.commonForeignkey
                                     + " = ds.id AND i.identifier = '" + identifier + "' AND rb.format = "
                                     + profileFormatNumberOutputSchema + ";";
                rs = conn.createStatement().executeQuery( selectBrief );
                break;
            case summary:

                String selectSummary = "SELECT rs.data FROM "
                                       + formatTypeInISORecordStore.get( SetOfReturnableElements.summary ) + " AS rs, "
                                       + mappings.mainDatabaseTable + " AS ds, qp_identifier AS i WHERE rs."
                                       + mappings.commonForeignkey + " = ds.id AND i." + mappings.commonForeignkey
                                       + " = ds.id AND i.identifier = '" + identifier + "' AND rs.format = "
                                       + profileFormatNumberOutputSchema + ";";
                LOG.debug( selectSummary );
                rs = conn.createStatement().executeQuery( selectSummary );
                break;
            case full:

                String selectFull = "SELECT rf.data FROM "
                                    + formatTypeInISORecordStore.get( SetOfReturnableElements.full ) + " AS rf, "
                                    + mappings.mainDatabaseTable + " AS ds, qp_identifier AS i WHERE rf."
                                    + mappings.commonForeignkey + " = ds.id AND i." + mappings.commonForeignkey
                                    + " = ds.id AND i.identifier = '" + identifier + "' AND rf.format = "
                                    + profileFormatNumberOutputSchema + ";";
                rs = conn.createStatement().executeQuery( selectFull );
                break;
            }

            writeResultSet( rs, writer );

        }

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
    private Writer getRequestedIDStatement( String formatType, RecordStoreOptions constraint, int formatNumber )
                            throws IOException {
        if ( constraint.getTables() != null ) {
            tableSet = constraint.getTables();
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

        s.append( "SELECT " + formatType + "." + mappings.commonForeignkey + " FROM " + mappings.mainDatabaseTable
                  + ", " + formatType );

        if ( tableSet.size() == 0 ) {
            s.append( ' ' );
        } else {
            s.append( ", " + concatTableFROM( tableSet ) );
        }

        s.append( "WHERE " + formatType + "." + mappings.commonForeignkey + " = " + mappings.mainDatabaseTable
                  + ".id AND " + formatType + "." + mappings.commonForeignkey + " >= " + constraint.getStartPosition()
                  + " AND " + formatType + ".format = " + formatNumber );

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
    public void getRecordsForTransactionInsertStatement( XMLStreamWriter writer, List<Integer> transactionIds )
                            throws SQLException, IOException {
        Connection conn = ConnectionManager.getConnection( connectionId );
        for ( int i : transactionIds ) {
            Writer s = new StringWriter();
            s.append( " SELECT " + formatTypeInISORecordStore.get( SetOfReturnableElements.brief ) + ".data " + "FROM "
                      + mappings.mainDatabaseTable + ", "
                      + formatTypeInISORecordStore.get( SetOfReturnableElements.brief ) + " " );

            s.append( " WHERE " + formatTypeInISORecordStore.get( SetOfReturnableElements.brief ) + "."
                      + mappings.commonForeignkey + " = " + mappings.mainDatabaseTable + ".id " );

            s.append( " AND " + formatTypeInISORecordStore.get( SetOfReturnableElements.brief ) + "." + "id" + " = "
                      + i );
            LOG.debug( "rs: " + s );

            ResultSet rsInsertedDatasets = conn.createStatement().executeQuery( s.toString() );

            writeResultSet( rsInsertedDatasets, writer );

        }

    }

    /**
     * This method writes the resultSet from the database with the writer to an XML-output.
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

                LOG.debug( "error: " + e.getMessage(), e );
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
            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( FactoryConfigurationError e ) {
            LOG.debug( "error: " + e.getMessage(), e );
        }
        // catch ( FileNotFoundException e ) {
        // // TODO Auto-generated catch block
        // LOG.debug( "error: " + e.getMessage(), e );
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // LOG.debug( "error: " + e.getMessage(), e );
        // }

    }

}
