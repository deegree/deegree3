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
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.types.datetime.Date;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.deegree.crs.CRS;
import org.deegree.protocol.csw.CSWConstants;
import org.slf4j.Logger;

/**
 * The parsing for the ISO application profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public final class ISOQPParsing extends XMLAdapter {

    private static final Logger LOG = getLogger( ISOQPParsing.class );

    private NamespaceContext nsContextISOParsing = new NamespaceContext( XMLAdapter.nsContext );

    private final static String CSW_PREFIX = "csw";

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

    private OMFactory factory = OMAbstractFactory.getOMFactory();

    private OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

    private OMNamespace namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "" );

    private OMNamespace namespaceOWS = factory.createOMNamespace( "http://www.opengis.net/ows", "ows" );

    private OMNamespace namespaceGCO = factory.createOMNamespace( "http://www.isotc211.org/2005/gco", "gco" );

    /**
     * 
     */
    private static Map<String, String> tableRecordType = new HashMap<String, String>();

    static {

        tableRecordType.put( RECORDBRIEF, BRIEFRECORD );
        tableRecordType.put( RECORDSUMMARY, SUMMARYRECORD );
        tableRecordType.put( RECORDFULL, RECORD );
    }

    private QueryableProperties qp = new QueryableProperties();

    private ReturnableProperties rp = new ReturnableProperties();

    private int id;

    private Statement stm;

    private List<Integer> recordsAffectedIDs;

    private GenerateRecord gr = new GenerateRecord();

    /**
     * Creates a new {@link ISOQPParsing} instance.
     * 
     * @param element
     */
    public ISOQPParsing( OMElement element ) {

        setRootElement( element );
        if ( element.getDefaultNamespace() != null ) {
            nsContextISOParsing.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                              rootElement.getDefaultNamespace().getNamespaceURI() );
        }
        nsContextISOParsing.addNamespace( CSW_PREFIX, CSWConstants.CSW_202_NS );
        nsContextISOParsing.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContextISOParsing.addNamespace( "ows", "http://www.opengis.net/ows" );
    }

    /**
     * Before any transaction operation is possible there should be an evaluation of the record. The response of the
     * full ISO record has to be valid. With this method this is guaranteed.
     * 
     * @param elem
     *            that has to be evaluated before there is any transaction operation possible.
     * @return a list of error-strings
     */
    private List<String> validate( OMElement elem ) {
        StringWriter s = new StringWriter();
        try {
            elem.serialize( s );
        } catch ( XMLStreamException e ) {
            LOG.debug( "error: " + e.getMessage(), e );
        }
        StringReader reader = new StringReader( s.toString() );
        if ( elem.getLocalName().equals( "MD_Metadata" ) ) {
            return SchemaValidator.validate( reader, "http://www.isotc211.org/2005/gmd/metadataEntity.xsd" );

        }
        return SchemaValidator.validate( reader, "http://schemas.opengis.net/csw/2.0.2/record.xsd" );
    }

    /**
     * Parses the recordelement that should be inserted into the backend. Every elementknot is put into an OMElement and
     * its atomic representation:
     * <p>
     * e.g. the "fileIdentifier" is put into an OMElement identifier and its identification-String is put into the
     * {@link QueryableProperties}.
     * 
     * @param isInspire
     * 
     * 
     * @throws IOException
     */
    public void parseAPISO( boolean isInspire, Connection connection )
                            throws IOException {

        // for ( String error : validate( rootElement ) ) {
        // throw new IOException( "VALIDATION-ERROR: " + error );
        // }

        /*---------------------------------------------------------------
         * 
         * 
         * FileIdentifier
         * 
         * 
         *---------------------------------------------------------------*/
        String fileIdentifierString = getNodeAsString( rootElement,
                                                       new XPath( "./gmd:fileIdentifier/gco:CharacterString",
                                                                  nsContextISOParsing ), null );
        List<String> idList = new ArrayList<String>();
        if ( fileIdentifierString == null ) {
            idList.add( generateUUID( connection ) );
            qp.setIdentifier( idList );

            OMElement omFileIdentifier = factory.createOMElement( "fileIdentifier", namespaceGMD );
            OMElement omFileCharacterString = factory.createOMElement( "CharacterString", namespaceGCO );
            omFileIdentifier.addChild( omFileCharacterString );
            omFileCharacterString.setText( qp.getIdentifier().get( 0 ) );
            gr.setIdentifier( omFileIdentifier );

        } else {
            idList.add( fileIdentifierString );
            qp.setIdentifier( idList );

            // TODO hack

            idList.add( fileIdentifierString );

            gr.setIdentifier( getElement( rootElement, new XPath( "./gmd:fileIdentifier", nsContextISOParsing ) ) );

        }

        /*---------------------------------------------------------------
         * 
         * 
         * (default) Language
         * 
         * 
         *---------------------------------------------------------------*/
        Locale locale = new Locale(
                                    getNodeAsString(
                                                     rootElement,
                                                     new XPath(
                                                                "./gmd:language/gco:CharacterString | ./gmd:language/gmd:LanguageCode/@codeListValue",
                                                                nsContextISOParsing ), null ) );

        qp.setLanguage( locale.getLanguage() );

        gr.setLanguage( getElement( rootElement, new XPath( "./gmd:language", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * CharacterSet
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setCharacterSet( getElement( rootElement, new XPath( "./gmd:characterSet", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * ParentIdentifier
         * 
         * 
         *---------------------------------------------------------------*/
        qp.setParentIdentifier( getNodeAsString( rootElement, new XPath( "./gmd:parentIdentifier/gco:CharacterString",
                                                                         nsContextISOParsing ), null ) );

        gr.setParentIdentifier( getElement( rootElement, new XPath( "./gmd:parentIdentifier", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Type
         * HierarchieLevel
         * 
         *---------------------------------------------------------------*/
        /**
         * if provided data is a dataset: type = dataset (default)
         * <p>
         * if provided data is a datasetCollection: type = series
         * <p>
         * if provided data is an application: type = application
         * <p>
         * if provided data is a service: type = service
         */
        qp.setType( getNodeAsString( rootElement, new XPath( "./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
                                                             nsContextISOParsing ), "dataset" ) );

        gr.setHierarchyLevel( getElements( rootElement, new XPath( "./gmd:hierarchyLevel", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * HierarchieLevelName
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setHierarchyLevelName( getElements( rootElement, new XPath( "./gmd:hierarchyLevelName", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Contact
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setContact( getElements( rootElement, new XPath( "./gmd:contact", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * DateStamp
         * Modified
         * 
         * 
         *---------------------------------------------------------------*/
        String dateString = getNodeAsString( rootElement, new XPath( "./gmd:dateStamp/gco:Date", nsContextISOParsing ),
                                             "0000-00-00" );
        Date date = null;
        try {
            date = new Date( dateString );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

        qp.setModified( date );
        gr.setDateStamp( getElement( rootElement, new XPath( "./gmd:dateStamp", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataStandardName
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataStandardName( getElement( rootElement, new XPath( "./gmd:metadataStandardName",
                                                                        nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataStandardVersion
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataStandardVersion( getElement( rootElement, new XPath( "./gmd:metadataStandardVersion",
                                                                           nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * DataSetURI
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setDataSetURI( getElement( rootElement, new XPath( "./gmd:dataSetURI", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Locale (for multilinguarity)
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setLocale( getElements( rootElement, new XPath( "./gmd:locale", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * SpatialRepresentationInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setSpatialRepresentationInfo( getElements( rootElement, new XPath( "./gmd:spatialRepresentationInfo",
                                                                              nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * ReferenceSystemInfo
         * 
         * 
         *---------------------------------------------------------------*/
        List<OMElement> crsElements = getElements(
                                                   rootElement,
                                                   new XPath(
                                                              "./gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier",
                                                              nsContextISOParsing ) );

        List<CRS> crsList = new ArrayList<CRS>();
        for ( OMElement crsElement : crsElements ) {
            String crsIdentification = getNodeAsString( crsElement, new XPath( "./gmd:code/gco:CharacterString",
                                                                               nsContextISOParsing ), "" );

            // String crsAuthority = getNodeAsString( crsElement, new XPath( "./gmd:codeSpace/gco:CharacterString",
            // nsContextISOParsing ), "" );
            //
            // String crsVersion = getNodeAsString( crsElement,
            // new XPath( "./gmd:version/gco:CharacterString", nsContextISOParsing ), "" );

            CRS crs = new CRS( crsIdentification );

            crsList.add( crs );
        }

        gr.setReferenceSystemInfo( getElements( rootElement, new XPath( "./gmd:referenceSystemInfo",
                                                                        nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataExtensionInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataExtensionInfo( getElements( rootElement, new XPath( "./gmd:metadataExtensionInfo",
                                                                          nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * IdentificationInfo
         * 
         * 
         *---------------------------------------------------------------*/
        List<OMElement> identificationInfo = getElements( rootElement, new XPath( "./gmd:identificationInfo",
                                                                                  nsContextISOParsing ) );

        ParseIdentificationInfo pI = new ParseIdentificationInfo( factory, connection, nsContextISOParsing );
        pI.parseIdentificationInfo( identificationInfo, gr, qp, rp, isInspire, crsList );

        /*---------------------------------------------------------------
         * 
         * 
         * ContentInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setContentInfo( getElements( rootElement, new XPath( "./gmd:contentInfo", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * DistributionInfo
         * 
         * 
         *---------------------------------------------------------------*/
        List<OMElement> formats = getElements(
                                               rootElement,
                                               new XPath(
                                                          "./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format",
                                                          nsContextISOParsing ) );

        // String onlineResource = getNodeAsString(
        // rootElement,
        // new XPath(
        // "./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
        // nsContextISOParsing ), null );

        List<Format> listOfFormats = new ArrayList<Format>();
        for ( OMElement md_format : formats ) {

            String formatName = getNodeAsString( md_format, new XPath( "./gmd:name/gco:CharacterString",
                                                                       nsContextISOParsing ), null );

            String formatVersion = getNodeAsString( md_format, new XPath( "./gmd:version/gco:CharacterString",
                                                                          nsContextISOParsing ), null );

            Format formatClass = new Format( formatName, formatVersion );
            listOfFormats.add( formatClass );

        }

        qp.setFormat( listOfFormats );
        gr.setDistributionInfo( getElement( rootElement, new XPath( "./gmd:distributionInfo", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * DataQualityInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setDataQualityInfo( getElements( rootElement, new XPath( "./gmd:dataQualityInfo", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * PortrayalCatalogueInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setPortrayalCatalogueInfo( getElements( rootElement, new XPath( "./gmd:portrayalCatalogueInfo",
                                                                           nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataConstraints
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataConstraints( getElements( rootElement, new XPath( "./gmd:metadataConstraints",
                                                                        nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * ApplicationSchemaInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setApplicationSchemaInfo( getElements( rootElement, new XPath( "./gmd:applicationSchemaInfo",
                                                                          nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataMaintenance
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataMaintenance( getElement( rootElement,
                                               new XPath( "./gmd:metadataMaintenance", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Series
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setSeries( getElements( rootElement, new XPath( "./gmd:series", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Describes
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setDescribes( getElements( rootElement, new XPath( "./gmd:describes", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * PropertyType
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setPropertyType( getElements( rootElement, new XPath( "./gmd:propertyType", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * FeatureType
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setFeatureType( getElements( rootElement, new XPath( "./gmd:featureType", nsContextISOParsing ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * FeatureAttribute
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setFeatureAttribute( getElements( rootElement, new XPath( "./gmd:featureAttribute", nsContextISOParsing ) ) );

        /*
         * sets the properties that are needed for building DC records
         */
        gr.setQueryableProperties( qp );
        gr.setReturnableProperties( rp );

    }

    /**
     * This method parses the OMElement regarding to the Dublin Core profile.
     * 
     * @throws IOException
     */
    public void parseAPDC()
                            throws IOException {

        for ( String error : validate( rootElement ) ) {
            LOG.debug( "VALIDATION-ERROR: " + error );
        }

        List<Keyword> keywordList = new ArrayList<Keyword>();

        List<Format> formatList = new ArrayList<Format>();
        // TODO anyText
        // StringWriter anyText = new StringWriter();

        qp.setIdentifier( Arrays.asList( getNodesAsStrings( rootElement, new XPath( "./dc:identifier",
                                                                                    nsContextISOParsing ) ) ) );

        rp.setCreator( getNodeAsString( rootElement, new XPath( "./dc:creator", nsContextISOParsing ), null ) );

        Keyword keyword = new Keyword( null, Arrays.asList( getNodesAsStrings( rootElement,
                                                                               new XPath( "./dc:subject",
                                                                                          nsContextISOParsing ) ) ),
                                       null );
        keywordList.add( keyword );
        qp.setKeywords( keywordList );

        qp.setTitle( Arrays.asList( getNodesAsStrings( rootElement, new XPath( "./dc:title", nsContextISOParsing ) ) ) );

        qp.set_abstract( Arrays.asList( getNodesAsStrings( rootElement, new XPath( "./dct:abstract",
                                                                                   nsContextISOParsing ) ) ) );

        String[] formatStrings = getNodesAsStrings( rootElement, new XPath( "./dc:format", nsContextISOParsing ) );

        for ( String s : formatStrings ) {

            Format format = new Format( s, null );
            formatList.add( format );
        }

        qp.setFormat( formatList );

        Date modified = null;
        try {
            modified = new Date(
                                 getNodeAsString( rootElement, new XPath( "./dct:modified", nsContextISOParsing ), null ) );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }
        qp.setModified( modified );

        qp.setType( getNodeAsString( rootElement, new XPath( "./dc:type", nsContextISOParsing ), null ) );

        String bbox_lowerCorner = getNodeAsString(
                                                   rootElement,
                                                   new XPath(
                                                              "./ows:BoundingBox/ows:LowerCorner | ./ows:WGS84BoundingBox/ows:LowerCorner",
                                                              nsContextISOParsing ), null );
        String bbox_upperCorner = getNodeAsString(
                                                   rootElement,
                                                   new XPath(
                                                              "./ows:BoundingBox/ows:UpperCorner | ./ows:WGS84BoundingBox/ows:UpperCorner",
                                                              nsContextISOParsing ), null );

        String[] lowerCornerSplitting = bbox_lowerCorner.split( " " );
        String[] upperCornerSplitting = bbox_upperCorner.split( " " );

        double boundingBoxWestLongitude = Double.parseDouble( lowerCornerSplitting[0] );

        double boundingBoxEastLongitude = Double.parseDouble( lowerCornerSplitting[1] );

        double boundingBoxSouthLatitude = Double.parseDouble( upperCornerSplitting[0] );

        double boundingBoxNorthLatitude = Double.parseDouble( upperCornerSplitting[1] );

        qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                            boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );

        rp.setPublisher( getNodeAsString( rootElement, new XPath( "./dc:publisher", nsContextISOParsing ), null ) );

        rp.setContributor( getNodeAsString( rootElement, new XPath( "./dc:contributor", nsContextISOParsing ), null ) );

        rp.setSource( getNodeAsString( rootElement, new XPath( "./dc:source", nsContextISOParsing ), null ) );

        gr.setQueryableProperties( qp );
        gr.setReturnableProperties( rp );

    }

    /**
     * This method executes the statement for INSERT datasets
     * 
     * @param isDC
     *            true, if a Dublin Core record should be inserted <br>
     *            <div style="text-indent:38px;">false, if an ISO record should be inserted</div>
     * @throws IOException
     */
    public void executeInsertStatement( boolean isDC, Connection connection )
                            throws IOException {
        try {
            stm = connection.createStatement();
            boolean isUpdate = false;
            /*
             * Question if there already exists the identifier.
             */
            String s = "SELECT i.identifier FROM qp_identifier AS i WHERE i.identifier = '" + qp.getIdentifier() + "';";
            ResultSet r = stm.executeQuery( s );
            LOG.debug( s );
            if ( r.next() ) {
                stm.close();
                throw new IOException( "Record with identifier '" + qp.getIdentifier() + "' already exists!" );
            }
            generateMainDatabaseDataset( connection );
            if ( isDC == true ) {
                generateDC( connection );
            } else {
                generateISO( connection );
            }
            executeQueryableProperties( isUpdate, connection );
            stm.close();

        } catch ( SQLException e ) {
            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * This method executes the statement for updating the queryable- and returnable properties of one specific record.
     */
    public void executeUpdateStatement( Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.datasets.name();
        final String qp_identifier = ISO_DC_Mappings.databaseTables.qp_identifier.name();
        boolean isUpdate = true;

        StringWriter sqlStatementUpdate = new StringWriter( 500 );

        int requestedId = 0;
        String modifiedAttribute = "null";
        try {
            stm = connection.createStatement();
            for ( String identifierString : qp.getIdentifier() ) {

                sqlStatementUpdate.append( "SELECT " + databaseTable + ".id from " + databaseTable + ","
                                           + qp_identifier + " where " + databaseTable + ".id = " + qp_identifier
                                           + ".fk_datasets AND " + qp_identifier + ".identifier = '" + identifierString
                                           + "'" );
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
                    this.id = requestedId;

                    if ( qp.getModified() != null || !qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
                        modifiedAttribute = "'" + qp.getModified() + "'";
                    }

                    // TODO version

                    // TODO status

                    // anyText
                    if ( qp.getAnyText() != null ) {

                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET anyText = '" + qp.getAnyText()
                                                  + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );

                    }

                    // modified
                    if ( qp.getModified() != null || !qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET modified = " + modifiedAttribute
                                                  + " WHERE id = " + requestedId );
                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }
                    // hassecurityconstraints
                    if ( qp.isHasSecurityConstraints() == true ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET hassecurityconstraints = '"
                                                  + qp.isHasSecurityConstraints() + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }

                    // language
                    if ( qp.getLanguage() != null ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET language = '" + qp.getLanguage()
                                                  + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }
                    // parentidentifier
                    if ( qp.getParentIdentifier() != null ) {
                        sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET parentidentifier = '"
                                                  + qp.getParentIdentifier() + "' WHERE id = " + requestedId );

                        executeSQLStatementUpdate( sqlStatementUpdate );
                    }
                    // TODO source

                    // TODO association

                    // recordBrief, recordSummary, recordFull update
                    updateRecord( requestedId );

                    executeQueryableProperties( isUpdate, connection );

                } else {
                    // TODO think about what response should be written if there is no such dataset in the backend??
                    String msg = "No dataset found for the identifier --> " + qp.getIdentifier() + " <--. ";
                    throw new SQLException( msg );
                }
            }
            stm.close();

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( IOException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            LOG.debug( "error: " + e.getMessage(), e );
        }
    }

    /**
     * Executes the SQL statement and cleans the size.
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

    /**
     * Method that encapsulates the generating for all the queryable properties.
     * 
     * @param isUpdate
     */
    private void executeQueryableProperties( boolean isUpdate, Connection connection ) {

        if ( qp.getIdentifier() != null ) {
            generateQP_IdentifierStatement( isUpdate, connection );
        }
        if ( qp.getTitle() != null ) {
            generateISOQP_TitleStatement( isUpdate, connection );
        }
        if ( qp.getType() != null ) {
            generateISOQP_TypeStatement( isUpdate, connection );
        }
        if ( qp.getKeywords() != null ) {
            generateISOQP_KeywordStatement( isUpdate, connection );
        }
        if ( qp.getTopicCategory() != null ) {
            generateISOQP_TopicCategoryStatement( isUpdate, connection );
        }
        if ( qp.getFormat() != null ) {
            generateISOQP_FormatStatement( isUpdate, connection );
        }
        // TODO relation
        if ( qp.get_abstract() != null ) {
            generateISOQP_AbstractStatement( isUpdate, connection );
        }
        if ( qp.getAlternateTitle() != null ) {
            generateISOQP_AlternateTitleStatement( isUpdate, connection );
        }
        if ( qp.getCreationDate() != null ) {
            generateISOQP_CreationDateStatement( isUpdate, connection );
        }
        if ( qp.getPublicationDate() != null ) {
            generateISOQP_PublicationDateStatement( isUpdate, connection );
        }
        if ( qp.getRevisionDate() != null ) {
            generateISOQP_RevisionDateStatement( isUpdate, connection );
        }
        if ( qp.getResourceIdentifier() != null ) {
            generateISOQP_ResourceIdentifierStatement( isUpdate, connection );
        }
        if ( qp.getServiceType() != null ) {
            generateISOQP_ServiceTypeStatement( isUpdate, connection );
        }
        if ( qp.getServiceTypeVersion() != null ) {
            generateISOQP_ServiceTypeVersionStatement( isUpdate, connection );
        }
        if ( qp.getGeographicDescriptionCode_service() != null ) {
            generateISOQP_GeographicDescriptionCode_ServiceStatement( isUpdate, connection );
        }
        if ( qp.getOperation() != null ) {
            generateISOQP_OperationStatement( isUpdate, connection );
        }
        if ( qp.getDenominator() != 0 || ( qp.getDistanceValue() != 0 && qp.getDistanceUOM() != null ) ) {
            generateISOQP_SpatialResolutionStatement( isUpdate, connection );
        }
        if ( qp.getOrganisationName() != null ) {
            generateISOQP_OrganisationNameStatement( isUpdate, connection );
        }
        if ( qp.getResourceLanguage() != null ) {
            generateISOQP_ResourceLanguageStatement( isUpdate, connection );
        }
        try {
            if ( ( qp.getTemporalExtentBegin().equals( new Date( "0000-00-00" ) ) && qp.getTemporalExtentEnd().equals(
                                                                                                                       new Date(
                                                                                                                                 "0000-00-00" ) ) )
                 || ( qp.getTemporalExtentBegin() != null && qp.getTemporalExtentEnd() != null ) ) {
                generateISOQP_TemporalExtentStatement( isUpdate, connection );
            }
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }
        if ( qp.getOperatesOnData() != null || qp.getOperatesOnData().size() != 0 ) {
            generateISOQP_OperatesOnStatement( isUpdate, connection );
        }
        if ( qp.getCouplingType() != null ) {
            generateISOQP_CouplingTypeStatement( isUpdate, connection );
        }
        // TODO spatial
        if ( qp.getBoundingBox() != null ) {
            generateISOQP_BoundingBoxStatement( isUpdate, connection );
        }
        // if ( qp.getCrs() != null || qp.getCrs().size() != 0 ) {
        // generateISOQP_CRSStatement( isUpdate );
        // }

    }

    /**
     * BE AWARE: the "modified" attribute is get from the first position in the list. The backend has the possibility to
     * add one such attribute. In the xsd-file there are more possible...
     * 
     */
    private void generateMainDatabaseDataset( Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.datasets.name();
        StringWriter sqlStatement = new StringWriter( 1000 );
        String modifiedAttribute = "null";
        boolean isCaseSensitive = true;
        try {

            this.id = getLastDatasetId( connection, databaseTable );
            this.id++;

            if ( qp.getModified() != null || !qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
                modifiedAttribute = "'" + qp.getModified() + "'";
            }

            sqlStatement.append( "INSERT INTO "
                                 + databaseTable
                                 + " (id, version, status, anyText, modified, hassecurityconstraints, language, parentidentifier, source, association) VALUES ("
                                 + this.id + ",null,null,'" + generateISOQP_AnyTextStatement( isCaseSensitive ) + "',"
                                 + modifiedAttribute + "," + qp.isHasSecurityConstraints() + ",'" + qp.getLanguage()
                                 + "','" + qp.getParentIdentifier() + "',null, null);" );
            LOG.debug( sqlStatement.toString() );
            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Updating the XML representation of a record in DC and ISO.
     * 
     * @param fk_datasets
     *            which record dataset should be updated
     * @throws IOException
     */
    private void updateRecord( int fk_datasets )
                            throws IOException {

        StringWriter isoOMElement = new StringWriter( 2000 );

        for ( String databaseTable : tableRecordType.keySet() ) {

            StringWriter sqlStatement = new StringWriter( 500 );
            StringBuffer buf = new StringBuffer();
            OMElement omElement = null;

            try {
                // DC-update
                omElement = factory.createOMElement( tableRecordType.get( databaseTable ), namespaceCSW );

                if ( omElement.getLocalName().equals( BRIEFRECORD ) ) {
                    gr.buildElementAsDcBriefElement( omElement );
                    isoOMElement.write( gr.getIsoBriefElement().toString() );
                } else if ( omElement.getLocalName().equals( SUMMARYRECORD ) ) {
                    gr.buildElementAsDcSummaryElement( omElement );
                    isoOMElement.write( gr.getIsoSummaryElement().toString() );
                } else {
                    gr.buildElementAsDcFullElement( omElement );
                    isoOMElement.write( gr.getIsoFullElement().toString() );
                }

                setBoundingBoxElement( omElement );

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

    }

    /**
     * Generates the ISO representation in brief, summary and full for this dataset.
     * 
     * @throws IOException
     */
    private void generateISO( Connection connection )
                            throws IOException {

        int fk_datasets = this.id;
        int idDatabaseTable = 0;
        for ( String databaseTable : tableRecordType.keySet() ) {
            StringWriter sqlStatement = new StringWriter( 500 );
            StringWriter isoElements = new StringWriter( 2000 );
            if ( databaseTable.equals( RECORDBRIEF ) ) {
                isoElements.write( gr.getIsoBriefElement().toString() );
            } else if ( databaseTable.equals( RECORDSUMMARY ) ) {
                isoElements.write( gr.getIsoSummaryElement().toString() );
            } else {
                isoElements.write( gr.getIsoFullElement().toString() );
            }

            try {

                idDatabaseTable = getLastDatasetId( connection, databaseTable );
                idDatabaseTable++;

                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                                     + idDatabaseTable + "," + fk_datasets + ", 2, '" + isoElements + "');" );

                stm.executeUpdate( sqlStatement.toString() );

            } catch ( SQLException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            }

        }
        /*
         * additional it generates the Dublin Core representation
         */
        generateDC( connection );

    }

    /**
     * Generates the Dublin Core representation in brief, summary and full for this dataset.
     * 
     * @param databaseTable
     *            which should be generated
     */
    private void generateDC( Connection connection ) {
        OMElement omElement = null;
        StringWriter sqlStatement = new StringWriter( 500 );

        int fk_datasets = this.id;

        int idDatabaseTable = 0;
        for ( String databaseTable : tableRecordType.keySet() ) {

            try {
                recordsAffectedIDs = new ArrayList<Integer>();

                idDatabaseTable = getLastDatasetId( connection, databaseTable );
                idDatabaseTable++;

                omElement = factory.createOMElement( tableRecordType.get( databaseTable ), namespaceCSW );

                if ( omElement.getLocalName().equals( BRIEFRECORD ) ) {
                    gr.buildElementAsDcBriefElement( omElement );
                } else if ( omElement.getLocalName().equals( SUMMARYRECORD ) ) {
                    gr.buildElementAsDcSummaryElement( omElement );
                } else {
                    gr.buildElementAsDcFullElement( omElement );
                }

                setBoundingBoxElement( omElement );

                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                                     + idDatabaseTable + "," + fk_datasets + ", 1, '" + omElement.toString() + "');" );

                recordsAffectedIDs.add( idDatabaseTable );

                stm.executeUpdate( sqlStatement.toString() );

            } catch ( SQLException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            } catch ( ParseException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
            }
        }

    }

    /**
     * Puts the identifier for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateQP_IdentifierStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.qp_identifier.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, identifier) VALUES (" + localId
                                 + "," + this.id + ",'" + qp.getIdentifier() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the anyText for this dataset into the database.
     * 
     * @param isUpdate
     */
    private String generateISOQP_AnyTextStatement( boolean isCaseSensitive ) {

        StringWriter anyText = new StringWriter();
        String stopWord = " # ";

        // Keywords
        for ( Keyword keyword : qp.getKeywords() ) {
            if ( keyword.getKeywordType() != null ) {
                anyText.append( keyword.getKeywordType() + stopWord );
            }
            if ( keyword.getThesaurus() != null ) {
                anyText.append( keyword.getThesaurus() + stopWord );
            }
            if ( keyword.getKeywords() != null ) {
                for ( String keywordString : keyword.getKeywords() ) {
                    anyText.append( keywordString + stopWord );
                }
            }
        }

        // title
        if ( qp.getTitle() != null ) {
            for ( String title : qp.getTitle() ) {
                anyText.append( title + stopWord );
            }
        }

        // abstract
        if ( qp.get_abstract() != null ) {
            for ( String _abstract : qp.get_abstract() ) {
                anyText.append( _abstract + stopWord );
            }
        }
        // format
        if ( qp.getFormat() != null ) {
            for ( Format format : qp.getFormat() ) {
                anyText.append( format.getName() + stopWord );
                // anyText.append( format.getVersion() + stopWord );
            }
        }

        // type
        anyText.append( qp.getType() + stopWord );

        // crs
        if ( qp.getCrs() != null ) {
            for ( CRS crs : qp.getCrs() ) {
                anyText.append( crs.getName() + stopWord );

            }
        }

        // creator
        anyText.append( rp.getCreator() + stopWord );

        // contributor
        anyText.append( rp.getContributor() + stopWord );

        // publisher
        anyText.append( rp.getPublisher() + stopWord );

        // language
        anyText.append( qp.getLanguage() + stopWord );

        // relation
        if ( rp.getRelation() != null ) {
            for ( String relation : rp.getRelation() ) {
                anyText.append( relation + stopWord );
            }
        }

        // rights
        if ( rp.getRights() != null ) {
            for ( String rights : rp.getRights() ) {
                anyText.append( rights + stopWord );
            }
        }

        // alternateTitle
        if ( qp.getAlternateTitle() != null ) {
            for ( String alternateTitle : qp.getAlternateTitle() ) {
                anyText.append( alternateTitle + stopWord );
            }
        }
        // organisationName
        anyText.append( qp.getOrganisationName() + stopWord );

        // topicCategory
        if ( qp.getTopicCategory() != null ) {
            for ( String topicCategory : qp.getTopicCategory() ) {
                anyText.append( topicCategory + stopWord );
            }
        }
        // resourceLanguage
        if ( qp.getResourceLanguage() != null ) {
            for ( String resourceLanguage : qp.getResourceLanguage() ) {
                anyText.append( resourceLanguage + stopWord );
            }
        }
        // geographicDescriptionCode
        anyText.append( qp.getGeographicDescriptionCode_service() + stopWord );

        // spatialResolution
        anyText.append( qp.getDistanceUOM() + stopWord );

        // serviceType
        anyText.append( qp.getServiceType() + stopWord );

        // operation
        if ( qp.getOperation() != null ) {
            for ( String operation : qp.getOperation() ) {
                anyText.append( operation + stopWord );
            }
        }

        // operatesOnData
        if ( qp.getOperatesOnData() != null ) {
            for ( OperatesOnData data : qp.getOperatesOnData() ) {
                anyText.append( data.getOperatesOn() + stopWord );
                anyText.append( data.getOperatesOnIdentifier() + stopWord );
                anyText.append( data.getOperatesOnName() + stopWord );

            }
        }

        // couplingType
        anyText.append( qp.getCouplingType() + stopWord );

        if ( isCaseSensitive == true ) {
            return anyText.toString();
        }
        return anyText.toString().toLowerCase();

    }

    /**
     * Puts the organisationname for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OrganisationNameStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_organisationname.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, organisationname) VALUES ("
                                 + localId + "," + this.id + ",'" + qp.getOrganisationName() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the temporalextent for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TemporalExtentStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_temporalextent.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }

            if ( ( qp.getTemporalExtentBegin() != null || !qp.getTemporalExtentBegin().equals( new Date( "0000-00-00" ) ) )
                 && ( qp.getTemporalExtentEnd() != null || !qp.getTemporalExtentEnd().equals( new Date( "0000-00-00" ) ) ) ) {
                localId = getLastDatasetId( connection, databaseTable );
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable
                                     + " (id, fk_datasets, tempextent_begin, tempextent_end) VALUES (" + localId + ","
                                     + this.id + ",'" + qp.getTemporalExtentBegin() + "','" + qp.getTemporalExtentEnd()
                                     + "');" );
                LOG.debug( sqlStatement.toString() );
                stm.executeUpdate( sqlStatement.toString() );
            }
        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the spatialresolution for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_SpatialResolutionStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_spatialresolution.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable
                                 + " (id, fk_datasets, denominator, distancevalue, distanceuom) VALUES (" + localId
                                 + "," + this.id + "," + qp.getDenominator() + "," + qp.getDistanceValue() + ",'"
                                 + qp.getDistanceUOM() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the couplingtype for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_CouplingTypeStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_couplingtype.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, couplingtype) VALUES (" + localId
                                 + "," + this.id + ",'" + qp.getCouplingType() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the operatesondata for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperatesOnStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_operatesondata.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            for ( OperatesOnData operatesOnData : qp.getOperatesOnData() ) {
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable
                                     + " (id, fk_datasets, operateson, operatesonidentifier, operatesonname) VALUES ("
                                     + localId + "," + this.id + ",'" + operatesOnData.getOperatesOn() + "','"
                                     + operatesOnData.getOperatesOnIdentifier() + "','"
                                     + operatesOnData.getOperatesOnName() + "');" );

                stm.executeUpdate( sqlStatement.toString() );
            }
        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the operation for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperationStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_operation.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }

            localId = getLastDatasetId( connection, databaseTable );
            for ( String operation : qp.getOperation() ) {

                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, operation) VALUES ("
                                     + localId + "," + this.id + ",'" + operation + "');" );

                stm.executeUpdate( sqlStatement.toString() );

            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the geographicDescriptionCode for the type "service" for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_GeographicDescriptionCode_ServiceStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_geographicdescriptioncode.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable
                                 + " (id, fk_datasets, geographicdescriptioncode) VALUES (" + localId + "," + this.id
                                 + ",'" + qp.getGeographicDescriptionCode_service() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the servicetypeversion for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeVersionStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_servicetypeversion.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, servicetypeversion) VALUES ("
                                 + localId + "," + this.id + ",'" + qp.getServiceTypeVersion() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the servicetype for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_servicetype.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, servicetype) VALUES (" + localId
                                 + "," + this.id + ",'" + qp.getServiceType() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the resourcelanguage for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceLanguageStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_resourcelanguage.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, resourcelanguage) VALUES ("
                                 + localId + "," + this.id + ",'" + qp.getResourceLanguage() + "');" );

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the revisiondate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_RevisionDateStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_revisiondate.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        String revisionDateAttribute = "null";
        int localId = 0;
        try {

            if ( qp.getRevisionDate() != null || !qp.getRevisionDate().equals( new Date( "0000-00-00" ) ) ) {
                revisionDateAttribute = "'" + qp.getRevisionDate() + "'";

                if ( isUpdate == true ) {
                    sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                    stm.executeUpdate( sqlStatement.toString() );
                }
                localId = getLastDatasetId( connection, databaseTable );
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, revisiondate) VALUES ("
                                     + localId + "," + this.id + "," + revisionDateAttribute + ");" );

                LOG.debug( sqlStatement.toString() );
                stm.executeUpdate( sqlStatement.toString() );
            }
        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the creationdate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_CreationDateStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_creationdate.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        String creationDateAttribute = "null";
        int localId = 0;
        try {

            if ( qp.getCreationDate() != null || !qp.getCreationDate().equals( new Date( "0000-00-00" ) ) ) {
                creationDateAttribute = "'" + qp.getCreationDate() + "'";

                if ( isUpdate == true ) {
                    sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                    stm.executeUpdate( sqlStatement.toString() );
                }
                localId = getLastDatasetId( connection, databaseTable );
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, creationdate) VALUES ("
                                     + localId + "," + this.id + "," + creationDateAttribute + ");" );

                stm.executeUpdate( sqlStatement.toString() );
            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the publicationdate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_PublicationDateStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_publicationdate.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        String publicationDateAttribute = "null";
        int localId = 0;
        try {

            if ( qp.getPublicationDate() == null || qp.getPublicationDate().equals( new Date( "0000-00-00" ) ) ) {
                publicationDateAttribute = "'" + qp.getPublicationDate() + "'";

                if ( isUpdate == true ) {
                    sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                    stm.executeUpdate( sqlStatement.toString() );
                }
                localId = getLastDatasetId( connection, databaseTable );
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, publicationdate) VALUES ("
                                     + localId + "," + this.id + "," + publicationDateAttribute + ");" );

                stm.executeUpdate( sqlStatement.toString() );
            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        } catch ( ParseException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the resourceIdentifier for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceIdentifierStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_resourceIdentifier.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }

            localId = getLastDatasetId( connection, databaseTable );
            for ( String resourceId : qp.getResourceIdentifier() ) {

                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, resourceidentifier) VALUES ("
                                     + localId + "," + this.id + ",'" + resourceId + "');" );

                stm.executeUpdate( sqlStatement.toString() );

            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the alternatetitle for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_AlternateTitleStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_alternatetitle.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            for ( String alternateTitle : qp.getAlternateTitle() ) {
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, alternatetitle) VALUES ("
                                     + localId + "," + this.id + ",'" + alternateTitle + "');" );
            }

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the title for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TitleStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_title.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            for ( String title : qp.getTitle() ) {
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, title) VALUES (" + localId
                                     + "," + this.id + ",'" + title + "');" );
            }

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the type for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TypeStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_type.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            localId++;
            sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, type) VALUES (" + localId + ","
                                 + this.id + ",'" + qp.getType() + "');" );

            LOG.debug( sqlStatement.toString() );
            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the keyword for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_KeywordStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_keyword.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }

            localId = getLastDatasetId( connection, databaseTable );
            for ( Keyword keyword : qp.getKeywords() ) {
                for ( String keywordString : keyword.getKeywords() ) {

                    localId++;
                    sqlStatement.append( "INSERT INTO " + databaseTable
                                         + " (id, fk_datasets, keywordtype, keyword, thesaurus) VALUES (" + localId
                                         + "," + this.id + ",'" + keyword.getKeywordType() + "','" + keywordString
                                         + "','" + keyword.getThesaurus() + "');" );

                    stm.executeUpdate( sqlStatement.toString() );
                }
            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the topiccategory for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TopicCategoryStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_topiccategory.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }

            localId = getLastDatasetId( connection, databaseTable );
            for ( String topicCategory : qp.getTopicCategory() ) {

                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, topiccategory) VALUES ("
                                     + localId + "," + this.id + ",'" + topicCategory + "');" );

                stm.executeUpdate( sqlStatement.toString() );

            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the format for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_FormatStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_format.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;

        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );

            for ( Format format : qp.getFormat() ) {
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, format) VALUES (" + localId
                                     + "," + this.id + ",'" + format.getName() + "');" );
            }

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the abstract for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_AbstractStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_abstract.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement.append( "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + this.id + ";" );
                stm.executeUpdate( sqlStatement.toString() );
            }
            localId = getLastDatasetId( connection, databaseTable );
            for ( String _abstract : qp.get_abstract() ) {
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, abstract) VALUES (" + localId
                                     + "," + this.id + ",'" + _abstract + "');" );
            }

            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the boundingbox for this dataset into the database.
     * 
     * @param isUpdate
     */
    // TODO one record got one or more bboxes?
    private void generateISOQP_BoundingBoxStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_boundingbox.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == false ) {
                localId = getLastDatasetId( connection, databaseTable );
                localId++;
                sqlStatement.append( "INSERT INTO " + databaseTable + " (id, fk_datasets, bbox) VALUES (" + localId
                                     + "," + this.id + ",SetSRID('BOX3D(" + qp.getBoundingBox().getEastBoundLongitude()
                                     + " " + qp.getBoundingBox().getNorthBoundLatitude() + ","
                                     + qp.getBoundingBox().getWestBoundLongitude() + " "
                                     + qp.getBoundingBox().getSouthBoundLatitude() + ")'::box3d,4326));" );
            } else {
                sqlStatement.append( "UPDATE " + databaseTable + " SET bbox = " + "SetSRID('BOX3D("
                                     + qp.getBoundingBox().getEastBoundLongitude() + " "
                                     + qp.getBoundingBox().getNorthBoundLatitude() + ","
                                     + qp.getBoundingBox().getWestBoundLongitude() + " "
                                     + qp.getBoundingBox().getSouthBoundLatitude()
                                     + ")'::box3d,4326) WHERE fk_datasets = " + this.id + ";" );
            }
            stm.executeUpdate( sqlStatement.toString() );

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

    }

    /**
     * Puts the crs for this dataset into the database.<br>
     * Creation of the CRS element. <br>
     * TODO its not clear where to get all the elements...
     * 
     * @param isUpdate
     */
    private void generateISOQP_CRSStatement( boolean isUpdate, Connection connection ) {
        final String databaseTable = ISO_DC_Mappings.databaseTables.isoqp_crs.name();
        StringWriter sqlStatement = new StringWriter( 500 );

        int localId = 0;
        try {

            if ( isUpdate == false ) {
                localId = getLastDatasetId( connection, databaseTable );
                for ( CRS crs : qp.getCrs() ) {
                    localId++;
                    sqlStatement.append( "INSERT INTO " + databaseTable
                                         + " (id, fk_datasets, authority, id_crs, version) VALUES (" + localId + ","
                                         + this.id + "," + crs.getName() + "," + CRS.EPSG_4326 + "," + crs.getName()
                                         + ");" );
                    stm.executeUpdate( sqlStatement.toString() );
                }
            }

        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

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

    /**
     * @return the maindatabasetable IDs of the records that are affected by the transaction
     */
    public List<Integer> getRecordsAffectedIDs() {
        return recordsAffectedIDs;
    }

    /**
     * Creation of the boundingBox element. Specifies which points has to be at which corner. The CRS is set to
     * EPSG:4326 because EX_GeographicBoundingBox is in this code implicitly.
     * 
     * @param omElement
     */
    private void setBoundingBoxElement( OMElement omElement ) {

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
     * Method to generate via the Java UUID-API a UUID if there is no identifier available.<br>
     * If the generated ID begins with a number then this is replaced with a random letter from the ASCII table. This
     * has to be done because the id attribute in the xml does not support any number at the beginning of an uuid. The
     * uppercase letters are in range from 65 to 90 whereas the lowercase letters are from 97 to 122. After the
     * generation there is a check if (in spite of the nearly impossibility) this uuid exists in the database already.
     * 
     * 
     * @return a uuid that is unique in the backend.
     */
    private String generateUUID( Connection connection ) {

        String uuid = UUID.randomUUID().toString();
        char firstChar = uuid.charAt( 0 );
        Pattern p = Pattern.compile( "[0-9]" );
        Matcher m = p.matcher( "" + firstChar );
        if ( m.matches() ) {
            int i;
            double ma = Math.random();
            if ( ma < 0.5 ) {
                i = 65;

            } else {
                i = 97;
            }

            firstChar = (char) ( (int) ( i + ma * 26 ) );
            uuid = uuid.replaceFirst( "[0-9]", String.valueOf( firstChar ) );
        }
        boolean uuidIsEqual = false;
        ResultSet rs;
        String compareIdentifier = "SELECT identifier FROM qp_identifier WHERE identifier = '" + uuid + "'";
        try {
            rs = connection.createStatement().executeQuery( compareIdentifier );
            while ( rs.next() ) {
                uuidIsEqual = true;
            }
        } catch ( SQLException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

        if ( uuidIsEqual == true ) {
            return generateUUID( connection );
        }
        return uuid;

    }

}
