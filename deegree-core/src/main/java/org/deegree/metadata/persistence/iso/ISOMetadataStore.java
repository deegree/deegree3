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

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.deegree.protocol.csw.CSWConstants.APISO_NS;
import static org.deegree.protocol.csw.CSWConstants.APISO_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_RECORD;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DC_LOCAL_PART;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_LOCAL_PART;
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.Join;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.RecordStoreOptions;
import org.deegree.metadata.persistence.iso.parsing.ParsingUtils;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.slf4j.Logger;

/**
 * {@link MetadataStore} implementation of Dublin Core and ISO Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataStore implements MetadataStore {

    private static final Logger LOG = getLogger( ISOMetadataStore.class );

    /**
     * registers the typeNames that are applicable to this recordStore and maps a typeName to a format, if it is DC or
     * ISO
     */
    private static Map<QName, Integer> typeNames = new HashMap<QName, Integer>();

    private final String connectionId;

    // if true, use old-style for spatial predicates (intersects instead of ST_Intersecs)
    private boolean useLegacyPredicates;

    // private FileIdentifierInspector fi;
    //
    // private InspireCompliance ic;
    //
    // private CoupledDataInspector ci;

    /**
     * shows the encoding of the database that is used
     */
    private String encoding;

    private ISOMetadataStoreConfig config;

    private static final String datasets;

    private static final String data;

    private static final String fk_datasets;

    private static final String format;

    private static final String id;

    private static final String qp_identifier;

    private static final String backendIdentifier;

    /**
     * maps the specific returnable element format to a concrete table in the backend<br>
     * brief, summary, full
     */
    private static final Map<ReturnableElement, String> formatTypeInISORecordStore = new HashMap<ReturnableElement, String>();

    static {
        datasets = PostGISMappingsISODC.DatabaseTables.datasets.name();
        qp_identifier = PostGISMappingsISODC.DatabaseTables.qp_identifier.name();

        data = PostGISMappingsISODC.CommonColumnNames.data.name();
        fk_datasets = PostGISMappingsISODC.CommonColumnNames.fk_datasets.name();
        format = PostGISMappingsISODC.CommonColumnNames.format.name();
        id = PostGISMappingsISODC.CommonColumnNames.id.name();
        backendIdentifier = PostGISMappingsISODC.CommonColumnNames.identifier.name();

        formatTypeInISORecordStore.put( ReturnableElement.brief, "recordbrief" );
        formatTypeInISORecordStore.put( ReturnableElement.summary, "recordsummary" );
        formatTypeInISORecordStore.put( ReturnableElement.full, "recordfull" );

        typeNames.put( new QName( "", "", "" ), 1 );
        typeNames.put( new QName( CSW_202_NS, DC_LOCAL_PART, "" ), 1 );
        typeNames.put( new QName( CSW_202_NS, DC_LOCAL_PART, CSW_PREFIX ), 1 );
        typeNames.put( new QName( DC_NS, "", "dc" ), 1 );
        typeNames.put( new QName( GMD_NS, GMD_LOCAL_PART, "" ), 2 );
        typeNames.put( new QName( GMD_NS, GMD_LOCAL_PART, GMD_PREFIX ), 2 );
        typeNames.put( new QName( APISO_NS, "", APISO_PREFIX ), 2 );

    }

    /**
     * Creates a new {@link ISOMetadataStore} instance from the given JAXB configuration object.
     * 
     * @param config
     */
    public ISOMetadataStore( ISOMetadataStoreConfig config ) {
        this.connectionId = config.getConnId();
        this.config = config;

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

            /*
             * if typeName is csw:Record
             */
            if ( typeName.equals( new QName( CSW_202_NS, DC_LOCAL_PART, CSW_PREFIX ) ) ) {

                urlConn = new URL( CSW_202_RECORD ).openConnection();

            }
            /*
             * if typeName is gmd:MD_Metadata
             */
            else if ( typeName.equals( new QName( GMD_NS, GMD_LOCAL_PART, GMD_PREFIX ) ) ) {

                urlConn = new URL( "http://www.isotc211.org/2005/gmd/identification.xsd" ).openConnection();

                writer.writeAttribute( "parentSchema", "http://www.isotc211.org/2005/gmd/gmd.xsd" );

            }
            /*
             * if the typeName is no registered in this recordprofile
             */
            else {
                String errorMessage = "The typeName " + typeName + "is not supported by this profile. ";
                LOG.debug( errorMessage );
                throw new InvalidParameterValueException( errorMessage );
            }

            urlConn.setDoInput( true );
            bais = new BufferedInputStream( urlConn.getInputStream() );

            Charset charset = encoding == null ? Charset.defaultCharset() : Charset.forName( encoding );
            InputStreamReader isr = new InputStreamReader( bais, charset );

            // readXMLFragment( isr, writer );

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
                            throws MetadataStoreException {

        LOG.debug( "init" );
        // lockManager = new DefaultLockManager( this, "LOCK_DB" );

        Connection conn = null;
        try {
            Class.forName( "org.postgresql.Driver" );
            conn = ConnectionManager.getConnection( connectionId );

            encoding = determinePostGRESEncoding( conn );

            String version = determinePostGISVersion( conn );
            if ( version.startsWith( "0." ) || version.startsWith( "1.0" ) || version.startsWith( "1.1" )
                 || version.startsWith( "1.2" ) ) {
                LOG.debug( "PostGIS version is " + version + " -- using legacy (pre-SQL-MM) predicates." );
                useLegacyPredicates = true;
            } else {
                LOG.debug( "PostGIS version is " + version + " -- using modern (SQL-MM) predicates." );
            }
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new MetadataStoreException( e.getMessage(), e );
        } catch ( ClassNotFoundException e ) {
            LOG.debug( e.getMessage(), e );
            throw new MetadataStoreException( e.getMessage(), e );
        } finally {
            close( conn );
        }

    }

    /**
     * @param conn
     * @return the encoding of the PostGRES database.
     */
    private String determinePostGRESEncoding( Connection conn ) {
        String encodingPostGRES = "UTF-8";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SHOW server_encoding" );
            rs.next();
            encodingPostGRES = rs.getString( 1 );
            LOG.debug( "PostGRES encoding: {}", encodingPostGRES );
            stmt.close();
            rs.close();
        } catch ( Exception e ) {
            LOG.warn( "Could not determine PostGRES encoding: {} -- defaulting to UTF-8", e.getMessage() );
        } finally {
            close( rs );
            close( stmt );
        }

        return encodingPostGRES;
    }

    private String determinePostGISVersion( Connection conn ) {
        String version = "1.0";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "SELECT postgis_version()" );
            rs.next();
            String postGISVersion = rs.getString( 1 );
            version = postGISVersion.split( " " )[0];
            LOG.debug( "PostGIS version: {}", version );

        } catch ( Exception e ) {
            LOG.warn( "Could not determine PostGIS version: {} -- defaulting to 1.0.0", e.getMessage() );
        } finally {
            close( rs );
            close( stmt );
        }
        return version;
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
    public void getRecords( XMLStreamWriter writer, QName typeName, URI outputSchema,
                            RecordStoreOptions recordStoreOptions )
                            throws MetadataStoreException, XMLStreamException {

        PostGISMappingsISODC mapping = new PostGISMappingsISODC();
        PostGISWhereBuilder builder = null;
        Connection conn = null;

        try {
            conn = ConnectionManager.getConnection( connectionId );
            builder = new PostGISWhereBuilder( mapping, (OperatorFilter) recordStoreOptions.getFilter(),
                                               recordStoreOptions.getSorting(), useLegacyPredicates );

            int profileFormatNumberOutputSchema = 0;
            int typeNameFormatNumber = 0;

            if ( typeNames.containsKey( typeName ) ) {
                typeNameFormatNumber = typeNames.get( typeName );
            }

            for ( QName qName : typeNames.keySet() ) {
                if ( qName.getNamespaceURI().equals( outputSchema.toString() ) ) {
                    profileFormatNumberOutputSchema = typeNames.get( qName );
                }
            }

            switch ( recordStoreOptions.getResultType() ) {
            case results:

                doResultsOnGetRecord( writer, typeName, profileFormatNumberOutputSchema, recordStoreOptions, builder,
                                      conn );
                break;
            case hits:

                doHitsOnGetRecord( writer, typeNameFormatNumber, recordStoreOptions,
                                   formatTypeInISORecordStore.get( recordStoreOptions.getSetOfReturnableElements() ),
                                   ResultType.hits, builder, conn );
                break;

            case validate:
                // TODO
            }
        } catch ( FilterEvaluationException e ) {
            LOG.error( e.getLocalizedMessage() );
            throw new MetadataStoreException( e.getMessage(), e );
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new MetadataStoreException( e.getMessage(), e );
        } finally {
            close( conn );
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
                                    RecordStoreOptions recordStoreOptions, String formatType, ResultType resultType,
                                    PostGISWhereBuilder builder, Connection conn )
                            throws MetadataStoreException, XMLStreamException {

        ResultSet rs = null;
        PreparedStatement ps = null;
        try {

            int countRows = 0;
            int nextRecord = 0;
            int returnedRecords = 0;

            ps = generateSELECTStatement( formatType, recordStoreOptions, typeNameFormatNumber, true, builder, conn );

            rs = ps.executeQuery();
            rs.next();
            countRows = rs.getInt( 1 );
            LOG.debug( "rs for rowCount: " + rs.getInt( 1 ) );

            if ( resultType.equals( ResultType.hits ) ) {
                writer.writeAttribute( "elementSet", recordStoreOptions.getSetOfReturnableElements().name() );

                // writer.writeAttribute( "recordSchema", "");

                writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

                writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( 0 ) );

                writer.writeAttribute( "nextRecord", Integer.toString( 1 ) );

                writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );
            } else {

                if ( countRows > recordStoreOptions.getMaxRecords() ) {
                    nextRecord = recordStoreOptions.getMaxRecords() + 1;
                    returnedRecords = recordStoreOptions.getMaxRecords();
                } else {
                    nextRecord = 0;
                    returnedRecords = countRows - recordStoreOptions.getStartPosition() + 1;
                }

                writer.writeAttribute( "elementSet", recordStoreOptions.getSetOfReturnableElements().name() );

                // writer.writeAttribute( "recordSchema", "");

                writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

                writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( returnedRecords ) );

                writer.writeAttribute( "nextRecord", Integer.toString( nextRecord ) );

                writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );
            }
        } catch ( Exception e ) {
            LOG.debug( "Error while perfoming hits on the metadata: {}", e.getMessage() );
            throw new MetadataStoreException( e.getMessage(), e );
        } finally {
            close( rs );
            close( ps );
        }

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
     * @param recordStoreOptions
     *            - the properties that are identified by the request
     * @param con
     *            - the JDBCConnection
     * @throws SQLException
     * @throws XMLStreamException
     * @throws IOException
     */
    private void doResultsOnGetRecord( XMLStreamWriter writer, QName typeName, int profileFormatNumberOutputSchema,
                                       RecordStoreOptions recordStoreOptions, PostGISWhereBuilder builder,
                                       Connection conn )
                            throws MetadataStoreException, XMLStreamException {
        int typeNameFormatNumber = 0;
        if ( typeNames.containsKey( typeName ) ) {
            typeNameFormatNumber = typeNames.get( typeName );
        }

        ResultSet rs = null;
        ResultSet rsOut = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement stmtOut = null;
        String formatType = null;
        try {
            switch ( recordStoreOptions.getSetOfReturnableElements() ) {

            case brief:
                formatType = formatTypeInISORecordStore.get( CSWConstants.ReturnableElement.brief );
                preparedStatement = generateSELECTStatement( formatType, recordStoreOptions, typeNameFormatNumber,
                                                             false, builder, conn );

                doHitsOnGetRecord( writer, typeNameFormatNumber, recordStoreOptions, formatType, ResultType.results,
                                   builder, conn );
                break;
            case summary:
                formatType = formatTypeInISORecordStore.get( CSWConstants.ReturnableElement.summary );
                preparedStatement = generateSELECTStatement( formatType, recordStoreOptions, typeNameFormatNumber,
                                                             false, builder, conn );

                doHitsOnGetRecord( writer, typeNameFormatNumber, recordStoreOptions, formatType, ResultType.results,
                                   builder, conn );
                break;
            case full:
                formatType = formatTypeInISORecordStore.get( CSWConstants.ReturnableElement.full );
                preparedStatement = generateSELECTStatement( formatType, recordStoreOptions, typeNameFormatNumber,
                                                             false, builder, conn );

                doHitsOnGetRecord( writer, typeNameFormatNumber, recordStoreOptions, formatType, ResultType.results,
                                   builder, conn );
                break;
            }

            rs = preparedStatement.executeQuery();

            if ( rs != null && recordStoreOptions.getMaxRecords() != 0 ) {
                // generate the output based on the outputSchema
                while ( rs.next() ) {
                    int returnedID = rs.getInt( 1 );

                    StringBuilder outS = new StringBuilder();
                    outS.append( "SELECT " ).append( formatType ).append( '.' ).append( data );
                    outS.append( " FROM " ).append( formatType );
                    outS.append( " WHERE " ).append( formatType ).append( '.' ).append( fk_datasets );
                    outS.append( " = " ).append( returnedID ).append( " AND " ).append( format );
                    outS.append( " = " ).append( profileFormatNumberOutputSchema );
                    stmtOut = conn.prepareStatement( outS.toString() );
                    rsOut = stmtOut.executeQuery();
                    // writeResultSet( rsOut, writer, 1 );
                    stmtOut.close();
                    rsOut.close();
                }

                rs.close();
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while perfoming hits on the metadata: {}", e.getMessage() );
            throw new MetadataStoreException( e.getMessage(), e );
        } finally {
            close( rs );
            close( rsOut );
            close( preparedStatement );
            close( stmtOut );
        }

    }

    /**
     * Selectstatement for the constrainted tables.
     * 
     * @param formatType
     *            - brief, summary or full
     * @param recordStoreOptions
     *            - properties that were requested
     * @param typeNameFormatNumber
     *            - the format number that is identified by the requested typeName
     * @param profileFormatNumberOutputSchema
     *            - the format number that is identified by the requested output schema
     * @param setCount
     *            - if the COUNT method should be in the statement
     * @param builder
     *            - the SQLWhereBuilder
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private PreparedStatement generateSELECTStatement( String formatType, RecordStoreOptions recordStoreOptions,
                                                       int typeNameFormatNumber, boolean setCount,
                                                       PostGISWhereBuilder builder, Connection conn )
                            throws MetadataStoreException {

        StringBuilder getDatasetIDs = new StringBuilder( 300 );
        PreparedStatement preparedStatement = null;
        try {

            LOG.debug( "wherebuilder: " + builder );

            String rootTableAlias = builder.getAliasManager().getRootTableAlias();
            String blobTableAlias = builder.getAliasManager().generateNew();

            getDatasetIDs.append( "SELECT " );
            if ( setCount ) {
                getDatasetIDs.append( "COUNT(*)" );
            } else {
                getDatasetIDs.append( rootTableAlias );
                getDatasetIDs.append( '.' );
                getDatasetIDs.append( id );
                getDatasetIDs.append( ',' );
                getDatasetIDs.append( blobTableAlias );
                getDatasetIDs.append( '.' );
                getDatasetIDs.append( data );
            }
            getDatasetIDs.append( " FROM " );
            getDatasetIDs.append( datasets );
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
            getDatasetIDs.append( formatType );
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
            getDatasetIDs.append( format );
            getDatasetIDs.append( "=?" );

            if ( builder.getWhere() != null ) {
                getDatasetIDs.append( " AND " );
                getDatasetIDs.append( builder.getWhere().getSQL() );
            }

            if ( builder.getOrderBy() != null && !setCount ) {
                getDatasetIDs.append( " ORDER BY " );
                getDatasetIDs.append( builder.getOrderBy().getSQL() );
            }

            if ( !setCount && recordStoreOptions != null ) {
                getDatasetIDs.append( " OFFSET " ).append( Integer.toString( recordStoreOptions.getStartPosition() - 1 ) );
                getDatasetIDs.append( " LIMIT " ).append( recordStoreOptions.getMaxRecords() );
            }

            preparedStatement = conn.prepareStatement( getDatasetIDs.toString() );

            int i = 1;
            preparedStatement.setInt( i++, typeNameFormatNumber );

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
        } catch ( SQLException e ) {
            LOG.debug( "Error while generating the SELECT statement: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while generating the SELECT statement: {}", e );
        }
        return preparedStatement;
    }

    // public List<Integer> transaction( TransactionOperation operations )
    // throws XMLStreamException, MetadataStoreException {
    //
    // List<Integer> affectedIds = new ArrayList<Integer>();
    // PostGISMappingsISODC mapping = new PostGISMappingsISODC();
    // Connection conn = null;
    // ResultSet rs = null;
    // PreparedStatement ps = null;
    // try {
    // conn = ConnectionManager.getConnection( connectionId );
    //
    // switch ( operations.getType() ) {
    // case INSERT:
    //
    // break;
    //
    // /*
    // * There is a known BUG here. If you update one complete record, there is no problem. If you update just
    // * some properties, multiple properties like "keywords" are not correctly updated. Have a look at {@link
    // * #recursiveElementKnotUpdate}
    // */
    // case UPDATE:
    //
    //                
    //
    // break;
    //
    // case DELETE:
    //
    // break;
    // }
    // } catch ( SQLException e ) {
    // LOG.debug( "Error while opening the JDBC connection: {}", e.getMessage() );
    // throw new MetadataStoreException( "Error while opening the JDBC connection: {}", e );
    // } catch ( FilterEvaluationException e ) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } finally {
    // close( conn );
    // }
    //
    // return affectedIds;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getRecordsById(javax.xml.stream.XMLStreamWriter,
     * org.deegree.commons.configuration.JDBCConnections, java.util.List)
     */
    @Override
    public List<MetadataRecord> getRecordById( List<String> idList, URI outputSchema, ReturnableElement elementSetName )
                            throws MetadataStoreException {

        int profileFormatNumberOutputSchema = 0;
        String elementSetNameString = null;
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        // MetadataResultSet result = null;
        XMLStreamReader xmlReader = null;

        List<MetadataRecord> result = new ArrayList<MetadataRecord>();
        try {

            conn = ConnectionManager.getConnection( connectionId );
            for ( QName qName : typeNames.keySet() ) {
                if ( qName.getNamespaceURI().equals( outputSchema.toString() ) ) {
                    profileFormatNumberOutputSchema = typeNames.get( qName );
                }
            }

            for ( String identifier : idList ) {
                if ( ParsingUtils.newInstance( conn ).proveIdExistence( identifier ) ) {
                    String msg = "No Metadata found with ID: '" + identifier + "'";
                    LOG.info( msg );
                    throw new MetadataStoreException( msg );
                }

                switch ( elementSetName ) {

                case brief:

                    elementSetNameString = formatTypeInISORecordStore.get( ReturnableElement.brief );
                    break;
                case summary:

                    elementSetNameString = formatTypeInISORecordStore.get( ReturnableElement.summary );
                    break;
                case full:

                    elementSetNameString = formatTypeInISORecordStore.get( ReturnableElement.full );
                    break;
                default:

                    elementSetNameString = formatTypeInISORecordStore.get( ReturnableElement.brief );

                    break;
                }

                StringBuilder select = new StringBuilder().append( "SELECT recordAlias." );
                select.append( data ).append( " FROM " );
                select.append( elementSetNameString ).append( " AS recordAlias, " );
                select.append( datasets ).append( " AS ds, " );
                select.append( qp_identifier );
                select.append( " AS i WHERE recordAlias." );
                select.append( fk_datasets ).append( " = ds." );
                select.append( id ).append( " AND i." );
                select.append( fk_datasets ).append( " = ds." );
                select.append( id ).append( " AND i." );
                select.append( backendIdentifier ).append( " = ? AND recordAlias." );
                select.append( format ).append( " = ?;" );

                stmt = conn.prepareStatement( select.toString() );
                LOG.debug( "select RecordById statement: " + stmt );

                if ( stmt != null ) {

                    stmt.setObject( 1, identifier );
                    stmt.setInt( 2, profileFormatNumberOutputSchema );
                    LOG.debug( "identifier: " + identifier );
                    LOG.debug( "outputFormat: " + profileFormatNumberOutputSchema );
                    LOG.debug( "" + stmt );
                    rs = stmt.executeQuery();
                    InputStreamReader isr = null;
                    Charset charset = encoding == null ? Charset.defaultCharset() : Charset.forName( encoding );

                    while ( rs.next() ) {
                        BufferedInputStream bais = new BufferedInputStream( rs.getBinaryStream( 1 ) );

                        try {
                            isr = new InputStreamReader( bais, charset );
                        } catch ( Exception e ) {

                            LOG.debug( "error while writing the result: {}", e.getMessage() );
                        }

                        xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( isr );
                        result.add( new ISORecord( xmlReader ) );

                    }
                    stmt.close();
                }
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while performing the getRecordById request: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while performing the getRecordById request: {}", e );
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            JDBCUtils.close( rs, stmt, conn, LOG );

        }
        return result;
    }

    /**
     * Prepares the statement to get all the central recordIDs for a statement.
     * 
     * @param formatType
     * @param constraint
     * @param formatNumber
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private PreparedStatement getRequestedIDStatement( String formatType, RecordStoreOptions constraint,
                                                       int formatNumber, PostGISWhereBuilder builder )
                            throws MetadataStoreException {

        StringBuilder s = new StringBuilder();
        PreparedStatement stmt = null;
        Connection conn = null;
        StringBuilder constraintExpression = new StringBuilder();
        try {
            conn = ConnectionManager.getConnection( connectionId );

            StringBuilder stringBuilder = builder.getWhere().getSQL();

            if ( stringBuilder.length() != 0 ) {
                constraintExpression.append( " AND (" ).append( builder.getWhere() ).append( ')' );
            } else {
                constraintExpression.append( ' ' );
            }

            s.append( "SELECT " ).append( formatType ).append( '.' );
            s.append( fk_datasets ).append( " FROM " );
            s.append( datasets ).append( ',' ).append( formatType );

            for ( PropertyNameMapping propName : builder.getMappedPropertyNames() ) {
                if ( propName.getTargetField().getTable() == null ) {
                    s.append( ' ' );
                } else {
                    s.append( ',' ).append( propName.getTargetField().getTable() ).append( ' ' );
                }
            }

            s.append( " WHERE " ).append( formatType ).append( '.' );
            s.append( fk_datasets ).append( '=' );
            s.append( datasets ).append( '.' );
            s.append( id ).append( " AND " );
            s.append( formatType ).append( '.' ).append( fk_datasets );
            s.append( " >= " ).append( constraint.getStartPosition() );
            s.append( " AND " ).append( formatType ).append( '.' );
            s.append( format ).append( '=' ).append( formatNumber );

            for ( PropertyNameMapping propName : builder.getMappedPropertyNames() ) {
                if ( propName.getTargetField().getTable() == null ) {
                    s.append( ' ' );
                } else {
                    s.append( " AND " ).append( propName.getTargetField().getTable() ).append( '.' );
                    s.append( fk_datasets ).append( '=' );
                    s.append( datasets ).append( '.' );
                    s.append( id );
                }
            }

            s.append( constraintExpression );

            stmt = conn.prepareStatement( s.toString() );

            if ( builder.getWhere() != null ) {
                for ( SQLLiteral literal : builder.getWhere().getLiterals() ) {
                    LOG.info( "Setting argument: " + literal );
                    stmt.setObject( 1, literal.getValue() );
                }
            }

            LOG.debug( "PreparedStatement:" + stmt );
        } catch ( SQLException e ) {
            LOG.debug( "Error while preparing the statement for the metadataIDs: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while preparing the statement for the metadataIDs: {}", e );
        } finally {
            JDBCUtils.close( null, stmt, conn, LOG );
        }
        return stmt;
    }

    public void getRecordsForTransactionInsertStatement( XMLStreamWriter writer, List<Integer> transactionIds )
                            throws MetadataStoreException {
        ResultSet rsInsertedDatasets = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionManager.getConnection( connectionId );

            StringBuilder s = new StringBuilder().append( " SELECT " );
            s.append( formatTypeInISORecordStore.get( ReturnableElement.brief ) );
            s.append( '.' );
            s.append( data );
            s.append( " FROM " ).append( datasets );
            s.append( ',' ).append( formatTypeInISORecordStore.get( ReturnableElement.brief ) );
            s.append( " WHERE " ).append( formatTypeInISORecordStore.get( ReturnableElement.brief ) );
            s.append( '.' ).append( fk_datasets );
            s.append( '=' ).append( datasets );
            s.append( '.' ).append( id );
            s.append( " AND " ).append( formatTypeInISORecordStore.get( ReturnableElement.brief ) );
            s.append( '.' ).append( id ).append( " = ?" );

            for ( int i : transactionIds ) {
                stmt = conn.prepareStatement( s.toString() );
                stmt.setObject( 1, i );
                rsInsertedDatasets = stmt.executeQuery();
                // writeResultSet( rsInsertedDatasets, writer, 1 );
                stmt.close();
                rsInsertedDatasets.close();
            }

        } catch ( SQLException e ) {
            LOG.debug( "Error while generating metadata output for the transaction: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while generating metadata output for the transaction: {}", e );
        }
        // catch ( XMLStreamException e ) {
        // LOG.debug( "Error while writing the result to the OutputStream: {}", e.getMessage() );
        // throw new MetadataStoreException( "Error while writing the result to the OutputStream: {}", e );
        // }
        finally {
            close( conn );
            close( stmt );
            close( rsInsertedDatasets );
        }

    }

    @Override
    public MetadataStoreTransaction acquireTransaction()
                            throws MetadataStoreException {
        ISOMetadataStoreTransaction ta = null;
        try {
            ta = new ISOMetadataStoreTransaction( ConnectionManager.getConnection( connectionId ), config, typeNames,
                                                  useLegacyPredicates );
        } catch ( SQLException e ) {
            throw new MetadataStoreException( e.getMessage() );
        }
        return ta;
    }
}