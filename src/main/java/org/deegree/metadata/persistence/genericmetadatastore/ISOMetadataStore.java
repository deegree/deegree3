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
package org.deegree.metadata.persistence.genericmetadatastore;

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
import java.io.InputStream;
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.Join;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.expression.SQLLiteral;
import org.deegree.filter.sql.postgis.PostGISWhereBuilder;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.RecordStoreOptions;
import org.deegree.metadata.persistence.genericmetadatastore.parsing.CoupledDataInspector;
import org.deegree.metadata.persistence.genericmetadatastore.parsing.FileIdentifierInspector;
import org.deegree.metadata.persistence.genericmetadatastore.parsing.ISOQPParsing;
import org.deegree.metadata.persistence.genericmetadatastore.parsing.InspireCompliance;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig;
import org.deegree.metadata.publication.DeleteTransaction;
import org.deegree.metadata.publication.InsertTransaction;
import org.deegree.metadata.publication.MetadataProperty;
import org.deegree.metadata.publication.TransactionOperation;
import org.deegree.metadata.publication.UpdateTransaction;
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

    private ExecuteStatements executeStatements;

    // if true, use old-style for spatial predicates (intersects instead of ST_Intersecs)
    private boolean useLegacyPredicates;

    private FileIdentifierInspector fi;

    private InspireCompliance ic;

    private CoupledDataInspector ci;

    /**
     * shows the encoding of the database that is used
     */
    private String encoding;

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
        fi = FileIdentifierInspector.newInstance( config.getIdentifierInspector(), connectionId );
        ic = InspireCompliance.newInstance( config.getRequireInspireCompliance(), connectionId );
        ci = CoupledDataInspector.newInstance( config.getCoupledResourceInspector(), connectionId );

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
                throw new IllegalArgumentException( errorMessage );
            }

            urlConn.setDoInput( true );
            bais = new BufferedInputStream( urlConn.getInputStream() );

            Charset charset = encoding == null ? Charset.defaultCharset() : Charset.forName( encoding );
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
                    writeResultSet( rsOut, writer, 1 );
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

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#transaction(javax.xml.stream.XMLStreamWriter,
     * org.deegree.commons.configuration.JDBCConnections, java.util.List)
     */

    @Override
    public List<Integer> transaction( XMLStreamWriter writer, TransactionOperation operations )
                            throws XMLStreamException, MetadataStoreException {

        List<Integer> affectedIds = new ArrayList<Integer>();
        PostGISMappingsISODC mapping = new PostGISMappingsISODC();
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionManager.getConnection( connectionId );

            switch ( operations.getType() ) {
            case INSERT:
                InsertTransaction ins = (InsertTransaction) operations;

                for ( OMElement element : ins.getElement() ) {
                    QName localName = element.getQName();

                    try {

                        executeStatements = new ExecuteStatements();

                        if ( localName.getLocalPart().equals( "Record" ) ) {

                            executeStatements.executeInsertStatement( true, conn, affectedIds,
                                                                      new ISOQPParsing().parseAPDC( element ) );

                        } else {

                            executeStatements.executeInsertStatement( false, conn, affectedIds,
                                                                      new ISOQPParsing().parseAPISO( fi, ic, ci,
                                                                                                     element ) );

                        }

                    } catch ( IOException e ) {

                        LOG.debug( "error: " + e.getMessage(), e );
                    }

                }
                break;

            /*
             * There is a known BUG here. If you update one complete record, there is no problem. If you update just
             * some properties, multiple properties like "keywords" are not correctly updated. Have a look at {@link
             * #recursiveElementKnotUpdate}
             */
            case UPDATE:

                UpdateTransaction upd = (UpdateTransaction) operations;
                /*
                 * if there should a complete record be updated or some properties
                 */
                if ( upd.getElement() != null ) {
                    QName localName = upd.getElement().getQName();

                    executeStatements = new ExecuteStatements();

                    if ( localName.getLocalPart().equals( "Record" ) ) {

                        executeStatements.executeUpdateStatement( conn, affectedIds,
                                                                  new ISOQPParsing().parseAPDC( upd.getElement() ) );

                    } else {
                        executeStatements.executeUpdateStatement( conn, affectedIds,
                                                                  new ISOQPParsing().parseAPISO( fi, ic, ci,
                                                                                                 upd.getElement() ) );

                    }

                } else {

                    RecordStoreOptions gdds = new RecordStoreOptions( upd.getConstraint(), ResultType.results,
                                                                      ReturnableElement.full );

                    int formatNumber = 0;
                    Set<QName> qNameSet = new HashSet<QName>();

                    // TODO sortProperty

                    PostGISWhereBuilder builder = new PostGISWhereBuilder( mapping,
                                                                           (OperatorFilter) upd.getConstraint(), null,
                                                                           useLegacyPredicates );

                    for ( QName propName : mapping.getPropToTableAndCol().keySet() ) {
                        String nsURI = propName.getNamespaceURI();
                        String prefix = propName.getPrefix();
                        QName analysedQName = new QName( nsURI, "", prefix );
                        qNameSet.add( analysedQName );
                    }

                    for ( QName qName : typeNames.keySet() ) {
                        if ( qName.equals( qNameSet.iterator().next() ) ) {
                            formatNumber = typeNames.get( qName );
                        }
                    }

                    PreparedStatement str = getRequestedIDStatement(
                                                                     formatTypeInISORecordStore.get( ReturnableElement.full ),
                                                                     gdds, formatNumber, builder );

                    ResultSet rsUpdatableDatasets = str.executeQuery();
                    List<Integer> updatableDatasets = new ArrayList<Integer>();
                    while ( rsUpdatableDatasets.next() ) {
                        updatableDatasets.add( rsUpdatableDatasets.getInt( 1 ) );

                    }
                    str.close();
                    rsUpdatableDatasets.close();

                    if ( updatableDatasets.size() != 0 ) {
                        PreparedStatement stmt = null;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append( "SELECT " ).append(
                                                                  formatTypeInISORecordStore.get( ReturnableElement.full ) );
                        stringBuilder.append( '.' ).append( data );
                        stringBuilder.append( " FROM " ).append(
                                                                 formatTypeInISORecordStore.get( ReturnableElement.full ) );
                        stringBuilder.append( " WHERE " ).append(
                                                                  formatTypeInISORecordStore.get( ReturnableElement.full ) );
                        stringBuilder.append( '.' ).append( format );
                        stringBuilder.append( " = 2 AND " ).append(
                                                                    formatTypeInISORecordStore.get( ReturnableElement.full ) );
                        stringBuilder.append( '.' ).append( fk_datasets ).append( " = ?;" );
                        for ( int i : updatableDatasets ) {

                            stmt = conn.prepareStatement( stringBuilder.toString() );
                            stmt.setObject( 1, i );
                            ResultSet rsGetStoredFullRecordXML = stmt.executeQuery();

                            while ( rsGetStoredFullRecordXML.next() ) {
                                for ( MetadataProperty recProp : upd.getRecordProperty() ) {

                                    PropertyNameMapping propMapping = mapping.getMapping( recProp.getPropertyName(),
                                                                                          null );

                                    Object obje = mapping.getPostGISValue( (Literal<?>) recProp.getReplacementValue(),
                                                                           recProp.getPropertyName() );

                                    // creating an OMElement read from backend byteData
                                    InputStream in = rsGetStoredFullRecordXML.getBinaryStream( 1 );
                                    XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( in );

                                    OMElement elementBuiltFromDB = new StAXOMBuilder( reader ).getDocument().getOMDocumentElement();

                                    OMElement omElement = recursiveElementKnotUpdate(
                                                                                      elementBuiltFromDB,
                                                                                      elementBuiltFromDB.getChildElements(),
                                                                                      propMapping.getTargetField().getColumn(),
                                                                                      obje.toString() );

                                    QName localName = omElement.getQName();

                                    ExecuteStatements executeStatements = new ExecuteStatements();

                                    if ( localName.equals( new QName( CSW_202_NS, "Record", CSW_PREFIX ) )
                                         || localName.equals( new QName( CSW_202_NS, "Record", "" ) ) ) {

                                        executeStatements.executeUpdateStatement(
                                                                                  conn,
                                                                                  affectedIds,
                                                                                  new ISOQPParsing().parseAPDC( omElement ) );

                                    } else {

                                        executeStatements.executeUpdateStatement(
                                                                                  conn,
                                                                                  affectedIds,
                                                                                  new ISOQPParsing().parseAPISO( fi,
                                                                                                                 ic,
                                                                                                                 ci,
                                                                                                                 omElement ) );

                                    }

                                }
                            }
                            stmt.close();
                            rsGetStoredFullRecordXML.close();

                        }
                    }

                }

                break;

            case DELETE:
                DeleteTransaction delete = (DeleteTransaction) operations;

                PostGISWhereBuilder builder = null;
                int formatNumber = 0;
                int[] formatNumbers = null;
                PreparedStatement stmt = null;

                // if there is a typeName denoted, the record with this profile should be deleted.
                // if there is no typeName attribute denoted, every record matched should be deleted.
                if ( delete.getTypeName() != null ) {

                    for ( QName qName : typeNames.keySet() ) {
                        if ( qName.equals( delete.getTypeName() ) ) {
                            formatNumber = typeNames.get( qName );
                        }
                    }
                } else {
                    // TODO remove hack,
                    // but: a csw record is available in every case, if not there is no iso, as well
                    formatNumbers = new int[1];
                    formatNumbers[0] = 1;
                }
                if ( formatNumber == 0 ) {
                    for ( int formatNum : formatNumbers ) {
                        // TODO sortProperty
                        try {
                            builder = new PostGISWhereBuilder( mapping, (OperatorFilter) delete.getConstraint(), null,
                                                               useLegacyPredicates );

                        } catch ( FilterEvaluationException e ) {

                            e.printStackTrace();
                        }

                        // test if there is a record to delete

                        ps = generateSELECTStatement(
                                                      formatTypeInISORecordStore.get( CSWConstants.ReturnableElement.brief ),
                                                      null, formatNum, false, builder, conn );

                        rs = ps.executeQuery();
                        List<Integer> deletableDatasets = new ArrayList<Integer>();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append( "DELETE FROM " );
                        stringBuilder.append( datasets );
                        stringBuilder.append( " WHERE " ).append( id );
                        stringBuilder.append( " = ?" );

                        if ( rs != null ) {
                            while ( rs.next() ) {
                                deletableDatasets.add( rs.getInt( 1 ) );

                            }
                            rs.close();

                            for ( int i : deletableDatasets ) {

                                stmt = conn.prepareStatement( stringBuilder.toString() );
                                stmt.setObject( 1, i );
                                stmt.executeUpdate();

                            }
                        }
                        affectedIds.addAll( deletableDatasets );

                    }

                }

                if ( stmt != null ) {
                    stmt.close();
                }

                break;
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while opening the JDBC connection: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while opening the JDBC connection: {}", e );
        } catch ( FilterEvaluationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            close( conn );
        }

        return affectedIds;
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
                LOG.debug( "rec: " + u.toString() );
                u.getFirstElement().setText( newContent );
                LOG.debug( "rec2: " + u.toString() );
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
                               ReturnableElement elementSetName )
                            throws MetadataStoreException, XMLStreamException {

        int profileFormatNumberOutputSchema = 0;
        String elementSetNameString = null;
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {

            conn = ConnectionManager.getConnection( connectionId );
            for ( QName qName : typeNames.keySet() ) {
                if ( qName.getNamespaceURI().equals( outputSchema.toString() ) ) {
                    profileFormatNumberOutputSchema = typeNames.get( qName );
                }
            }

            for ( String identifier : idList ) {
                if ( fi.proveIdExistence( identifier ) ) {
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

                    rs = stmt.executeQuery();
                    writeResultSet( rs, writer, 1 );
                    stmt.close();
                }
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while performing the getRecordById request: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while performing the getRecordById request: {}", e );
        } finally {
            JDBCUtils.close( rs, stmt, conn, LOG );
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.record.persistence.RecordStore#getRecordsForTransactionInsertStatement(javax.xml.stream.XMLStreamWriter
     * )
     */
    @Override
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
                writeResultSet( rsInsertedDatasets, writer, 1 );
                stmt.close();
                rsInsertedDatasets.close();
            }

        } catch ( SQLException e ) {
            LOG.debug( "Error while generating metadata output for the transaction: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while generating metadata output for the transaction: {}", e );
        } catch ( XMLStreamException e ) {
            LOG.debug( "Error while writing the result to the OutputStream: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while writing the result to the OutputStream: {}", e );
        } finally {
            close( conn );
            close( stmt );
            close( rsInsertedDatasets );
        }

    }

    /**
     * This method writes the resultSet from the database with the writer to an XML-output.
     * 
     * @param resultSet
     *            that should search the backend, you have to close it manually.
     * @param writer
     *            that writes the data to the output
     * @param columnIndex
     *            the column that should be requested, not <Code>null</Code>.
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void writeResultSet( ResultSet resultSet, XMLStreamWriter writer, int columnIndex )
                            throws SQLException, XMLStreamException {
        InputStreamReader isr = null;
        Charset charset = encoding == null ? Charset.defaultCharset() : Charset.forName( encoding );
        while ( resultSet.next() ) {

            BufferedInputStream bais = new BufferedInputStream( resultSet.getBinaryStream( columnIndex ) );

            try {
                isr = new InputStreamReader( bais, charset );
            } catch ( Exception e ) {

                LOG.debug( "error while writing the result: {}", e.getMessage() );
            }

            readXMLFragment( isr, writer );

        }

    }

    /**
     * Reads a valid XML fragment
     * 
     * @param isr
     * @param xmlWriter
     * @throws XMLStreamException
     */
    private void readXMLFragment( InputStreamReader isr, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {

        // XMLStreamReader xmlReaderOut;

        XMLStreamReader xmlReader = null;
        try {
            // FileOutputStream fout = new FileOutputStream( "/home/thomas/Desktop/test.xml" );
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
        } finally {
            xmlReader.close();
        }
        // catch ( FileNotFoundException e ) {
        //
        // LOG.debug( "error: " + e.getMessage(), e );
        // } catch ( IOException e ) {
        //
        // LOG.debug( "error: " + e.getMessage(), e );
        // }

    }

}
