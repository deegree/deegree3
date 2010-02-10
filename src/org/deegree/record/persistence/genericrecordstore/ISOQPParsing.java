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

import javax.xml.bind.ValidationException;
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

/**
 * The parsing for the ISO application profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ISOQPParsing extends XMLAdapter {

    private NamespaceContext nsContext = new NamespaceContext( XMLAdapter.nsContext );

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

    static Map<String, String> tableRecordType = new HashMap<String, String>();

    static {

        tableRecordType.put( RECORDBRIEF, BRIEFRECORD );
        tableRecordType.put( RECORDSUMMARY, SUMMARYRECORD );
        tableRecordType.put( RECORDFULL, RECORD );
    }

    QueryableProperties qp = new QueryableProperties();

    ReturnableProperties rp = new ReturnableProperties();

    private int id;

    private Connection connection;

    private ParseIdentificationInfo pI;

    private OMFactory factory = OMAbstractFactory.getOMFactory();

    private OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

    private OMNamespace namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "" );

    private OMNamespace namespaceOWS = factory.createOMNamespace( "http://www.opengis.net/ows", "ows" );

    private OMNamespace namespaceGCO = factory.createOMNamespace( "http://www.isotc211.org/2005/gco", "gco" );

    private Statement stm;

    private List<Integer> recordInsertIDs;

    private List<CRS> crsList = new ArrayList<CRS>();

    private GenerateRecord gr = new GenerateRecord();

    /**
     * 
     * 
     * @param element
     * @param connection
     */
    public ISOQPParsing( OMElement element, Connection connection ) {

        this.connection = connection;

        setRootElement( element );
        if ( element.getDefaultNamespace() != null ) {
            nsContext.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                    rootElement.getDefaultNamespace().getNamespaceURI() );
        }
        nsContext.addNamespace( CSW_PREFIX, CSWConstants.CSW_202_NS );
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows" );

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
            e.printStackTrace();
        }
        StringReader reader = new StringReader( s.toString() );
        if ( elem.getLocalName().equals( "MD_Metadata" ) ) {
            return SchemaValidator.validate( reader, "http://www.isotc211.org/2005/gmd/metadataEntity.xsd" );

        } else {
            return SchemaValidator.validate( reader, "http://schemas.opengis.net/csw/2.0.2/record.xsd" );

        }
    }

    /**
     * Parses the recordelement that should be inserted into the backend. Every elementknot is put into an OMElement and
     * its atomic representation:
     * <p>
     * e.g. the "fileIdentifier" is put into an OMElement identifier and its identification-String is put into the
     * {@link QueryableProperties}.
     * 
     * 
     * @throws IOException
     * @throws ValidationException
     */
    public void parseAPISO( boolean isInspire )
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
        String fileIdentifierString = getNodeAsString(
                                                       rootElement,
                                                       new XPath( "./gmd:fileIdentifier/gco:CharacterString", nsContext ),
                                                       null );

        if ( fileIdentifierString == null ) {
            qp.setIdentifier( generateUUID() );

            OMElement omFileIdentifier = factory.createOMElement( "fileIdentifier", namespaceGMD );
            OMElement omFileCharacterString = factory.createOMElement( "CharacterString", namespaceGCO );
            omFileIdentifier.addChild( omFileCharacterString );
            omFileCharacterString.setText( qp.getIdentifier() );
            gr.setIdentifier( omFileIdentifier );

        } else {
            qp.setIdentifier( fileIdentifierString );

            // TODO hack
            List<String> idList = new ArrayList<String>();
            idList.add( fileIdentifierString );
            qp.setIdentifierDC( idList );

            gr.setIdentifier( getElement( rootElement, new XPath( "./gmd:fileIdentifier", nsContext ) ) );

        }

        /*---------------------------------------------------------------
         * 
         * 
         * (default) Language
         * 
         * 
         *---------------------------------------------------------------*/
        Locale locale = new Locale(getNodeAsString( rootElement, new XPath( "./gmd:language/gco:CharacterString | ./gmd:language/gmd:LanguageCode/@codeListValue", nsContext ),
                                                    null ));
        rp.setLanguage( locale.getISO3Language() );

        gr.setLanguage( getElement( rootElement, new XPath( "./gmd:language", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * CharacterSet
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setCharacterSet( getElement( rootElement, new XPath( "./gmd:characterSet", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * ParentIdentifier
         * 
         * 
         *---------------------------------------------------------------*/
        qp.setParentIdentifier( getNodeAsString( rootElement, new XPath( "./gmd:parentIdentifier/gco:CharacterString",
                                                                         nsContext ), null ) );

        gr.setParentIdentifier( getElement( rootElement, new XPath( "./gmd:parentIdentifier", nsContext ) ) );

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
                                                             nsContext ), "dataset" ) );

        gr.setHierarchyLevel( getElements( rootElement, new XPath( "./gmd:hierarchyLevel", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * HierarchieLevelName
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setHierarchyLevelName( getElements( rootElement, new XPath( "./gmd:hierarchyLevelName", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Contact
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setContact( getElements( rootElement, new XPath( "./gmd:contact", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * DateStamp
         * Modified
         * 
         * 
         *---------------------------------------------------------------*/
        String dateString = getNodeAsString( rootElement, new XPath( "./gmd:dateStamp/gco:Date", nsContext ),
                                             "0000-00-00" );
        Date date = null;
        try {
            date = new Date( dateString );
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

        qp.setModified( date );
        gr.setDateStamp( getElement( rootElement, new XPath( "./gmd:dateStamp", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataStandardName
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataStandardName( getElement( rootElement, new XPath( "./gmd:metadataStandardName", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataStandardVersion
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataStandardVersion( getElement( rootElement, new XPath( "./gmd:metadataStandardVersion", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * DataSetURI
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setDataSetURI( getElement( rootElement, new XPath( "./gmd:dataSetURI", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Locale (for multilinguarity)
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setLocale( getElements( rootElement, new XPath( "./gmd:locale", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * SpatialRepresentationInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setSpatialRepresentationInfo( getElements( rootElement, new XPath( "./gmd:spatialRepresentationInfo",
                                                                              nsContext ) ) );

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
                                                              nsContext ) );

        for ( OMElement crsElement : crsElements ) {
            String crsIdentification = getNodeAsString( crsElement, new XPath( "./gmd:code/gco:CharacterString",
                                                                               nsContext ), "" );

            String crsAuthority = getNodeAsString( crsElement, new XPath( "./gmd:codeSpace/gco:CharacterString",
                                                                          nsContext ), "" );

            String crsVersion = getNodeAsString( crsElement,
                                                 new XPath( "./gmd:version/gco:CharacterString", nsContext ), "" );
            // CRS crs = new CRS( crsAuthority, crsIdentification, crsVersion );

            CRS crs = new CRS( crsIdentification );

            crsList.add( crs );
        }

        gr.setReferenceSystemInfo( getElements( rootElement, new XPath( "./gmd:referenceSystemInfo", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataExtensionInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataExtensionInfo( getElements( rootElement, new XPath( "./gmd:metadataExtensionInfo", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * IdentificationInfo
         * 
         * 
         *---------------------------------------------------------------*/
        List<OMElement> identificationInfo = getElements( rootElement,
                                                          new XPath( "./gmd:identificationInfo", nsContext ) );

        pI = new ParseIdentificationInfo( factory, connection, nsContext );
        pI.parseIdentificationInfo( identificationInfo, gr, qp, rp, isInspire, crsList );

        /*---------------------------------------------------------------
         * 
         * 
         * ContentInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setContentInfo( getElements( rootElement, new XPath( "./gmd:contentInfo", nsContext ) ) );

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
                                                          nsContext ) );

        String onlineResource = getNodeAsString(
                                                 rootElement,
                                                 new XPath(
                                                            "./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
                                                            nsContext ), null );

        Format formatClass = null;
        List<Format> listOfFormats = new ArrayList<Format>();
        for ( OMElement md_format : formats ) {
            formatClass = new Format();
            String formatName = getNodeAsString( md_format, new XPath( "./gmd:name/gco:CharacterString", nsContext ),
                                                 null );

            String formatVersion = getNodeAsString( md_format, new XPath( "./gmd:version/gco:CharacterString",
                                                                          nsContext ), null );

            formatClass.setName( formatName );
            formatClass.setVersion( formatVersion );

            listOfFormats.add( formatClass );

        }

        qp.setFormat( listOfFormats );
        gr.setDistributionInfo( getElement( rootElement, new XPath( "./gmd:distributionInfo", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * DataQualityInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setDataQualityInfo( getElements( rootElement, new XPath( "./gmd:dataQualityInfo", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * PortrayalCatalogueInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setPortrayalCatalogueInfo( getElements( rootElement, new XPath( "./gmd:portrayalCatalogueInfo", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataConstraints
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataConstraints( getElements( rootElement, new XPath( "./gmd:metadataConstraints", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * ApplicationSchemaInfo
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setApplicationSchemaInfo( getElements( rootElement, new XPath( "./gmd:applicationSchemaInfo", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * MetadataMaintenance
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setMetadataMaintenance( getElement( rootElement, new XPath( "./gmd:metadataMaintenance", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Series
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setSeries( getElements( rootElement, new XPath( "./gmd:series", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * Describes
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setDescribes( getElements( rootElement, new XPath( "./gmd:describes", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * PropertyType
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setPropertyType( getElements( rootElement, new XPath( "./gmd:propertyType", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * FeatureType
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setFeatureType( getElements( rootElement, new XPath( "./gmd:featureType", nsContext ) ) );

        /*---------------------------------------------------------------
         * 
         * 
         * FeatureAttribute
         * 
         * 
         *---------------------------------------------------------------*/
        gr.setFeatureAttribute( getElements( rootElement, new XPath( "./gmd:featureAttribute", nsContext ) ) );

        /*
         * sets the properties that are needed for building DC records
         */
        gr.setQueryableProperties( qp );
        gr.setReturnableProperties( rp );

    }

    /**
     * This method parses the OMElement in Dublin Core.
     * 
     * @throws IOException
     */
    public void parseAPDC()
                            throws IOException {

        for ( String error : validate( rootElement ) ) {
            System.out.println( "VALIDATION-ERROR: " + error );
        }

        List<Keyword> keywordList = new ArrayList<Keyword>();

        List<Format> formatList = new ArrayList<Format>();
        StringWriter anyText = new StringWriter();
        Keyword keyword = new Keyword();
        Format format = new Format();

        

        qp.setIdentifierDC( Arrays.asList( getNodesAsStrings( rootElement, new XPath( "./dc:identifier", nsContext ) ) ) );

        rp.setCreator( getNodeAsString( rootElement, new XPath( "./dc:creator", nsContext ), null ) );

        keyword.setKeywords( Arrays.asList( getNodesAsStrings( rootElement, new XPath( "./dc:subject", nsContext ) ) ) );
        keywordList.add( keyword );
        qp.setKeywords( keywordList );

        qp.setTitle( Arrays.asList( getNodesAsStrings( rootElement, new XPath( "./dc:title", nsContext ) ) ) );

        qp.set_abstract( Arrays.asList( getNodesAsStrings( rootElement, new XPath( "./dct:abstract", nsContext ) ) ) );

        String[] formatStrings = getNodesAsStrings( rootElement, new XPath( "./dc:format", nsContext ) );

        for ( String s : formatStrings ) {
            format.setName( s );
            formatList.add( format );
        }

        qp.setFormat( formatList );

        Date modified = null;
        try {
            modified = new Date( getNodeAsString( rootElement, new XPath( "./dct:modified", nsContext ), null ) );
        } catch ( ParseException e ) {

            e.printStackTrace();
        }
        qp.setModified( modified );

        qp.setType( getNodeAsString( rootElement, new XPath( "./dc:type", nsContext ), null ) );

        String bbox_lowerCorner = getNodeAsString(
                                                   rootElement,
                                                   new XPath(
                                                              "./ows:BoundingBox/ows:LowerCorner | ./ows:WGS84BoundingBox/ows:LowerCorner",
                                                              nsContext ), null );
        String bbox_upperCorner = getNodeAsString(
                                                   rootElement,
                                                   new XPath(
                                                              "./ows:BoundingBox/ows:UpperCorner | ./ows:WGS84BoundingBox/ows:UpperCorner",
                                                              nsContext ), null );

        String[] lowerCornerSplitting = bbox_lowerCorner.split( " " );
        String[] upperCornerSplitting = bbox_upperCorner.split( " " );

        double boundingBoxWestLongitude = Double.parseDouble( lowerCornerSplitting[0] );

        double boundingBoxEastLongitude = Double.parseDouble( lowerCornerSplitting[1] );

        double boundingBoxSouthLatitude = Double.parseDouble( upperCornerSplitting[0] );

        double boundingBoxNorthLatitude = Double.parseDouble( upperCornerSplitting[1] );

        qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                            boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );

        rp.setPublisher( getNodeAsString( rootElement, new XPath( "./dc:publisher", nsContext ), null ) );

        rp.setContributor( getNodeAsString( rootElement, new XPath( "./dc:contributor", nsContext ), null ) );

        rp.setSource( getNodeAsString( rootElement, new XPath( "./dc:source", nsContext ), null ) );

        gr.setQueryableProperties( qp );
        gr.setReturnableProperties( rp );

    }

    /**
     * This method executes the statement for INSERT datasets
     * <p>
     * TODO ExceptionHandling if there are properties that have to be in the insert statement
     * 
     * 
     * @param isDC
     *            true, if a Dublin Core record should be inserted <br>
     *            <div style="text-indent:38px;"/>false, if an ISO record should be inserted</div>
     * @throws IOException
     */
    public void executeInsertStatement( boolean isDC )
                            throws IOException {
        try {
            stm = connection.createStatement();

            boolean isUpdate = false;
            String s = "SELECT i.identifier FROM qp_identifier AS i WHERE i.identifier = '"+ qp.getIdentifier() + "';";
            ResultSet r = stm.executeQuery( s );
            System.out.println(s);
            if ( r.next() ) {
                stm.close();
                throw new IOException("Record with identifier '" + qp.getIdentifier() + "' already exists!");
            } else {
                generateMainDatabaseDataset();
                if ( isDC == true ) {
                    generateDC();
                } else {
                    generateISO();
                }
                executeQueryableProperties( isUpdate );
                stm.close();
            }
            

        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * This method executes the statement for updating the queryable- and returnable properties of one record.
     */
    public void executeUpdateStatement() {
        final String databaseTable = "datasets";
        final String qp_identifier = "qp_identifier";
        boolean isUpdate = true;

        StringWriter sqlStatementUpdate = new StringWriter( 500 );
        StringBuffer buf = new StringBuffer();
        int requestedId = 0;
        String modifiedAttribute = "null";
        try {
            stm = connection.createStatement();
            sqlStatementUpdate.append( "SELECT " + databaseTable + ".id from " + databaseTable + "," + qp_identifier + " where "
                                       + databaseTable + ".id = " + qp_identifier + ".fk_datasets AND " + qp_identifier + ".identifier = '" + qp.getIdentifier() + "'" );
            System.out.println( sqlStatementUpdate.toString() );
            buf = sqlStatementUpdate.getBuffer();
            ResultSet rs = connection.createStatement().executeQuery( sqlStatementUpdate.toString() );

            if ( rs != null ) {
                while ( rs.next() ) {
                    requestedId = rs.getInt( 1 );
                    System.out.println( rs.getInt( 1 ) );
                }
                buf.setLength( 0 );
                rs.close();
            }
            if ( requestedId != 0 ) {
                this.id = requestedId;

                if ( qp.getModified() == null || qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
                } else {
                    modifiedAttribute = "'" + qp.getModified() + "'";
                }

                // TODO version

                // TODO status

                // anyText
                if ( qp.getAnyText() != null ) {

                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET anyText = '" + qp.getAnyText()
                                              + "' WHERE id = " + requestedId );

                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );

                }

                // modified
                if ( qp.getModified() != null || !qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET modified = " + modifiedAttribute
                                              + " WHERE id = " + requestedId );
                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );
                }
                // hassecurityconstraints
                if ( qp.isHasSecurityConstraints() == true ) {
                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET hassecurityconstraints = '"
                                              + qp.isHasSecurityConstraints() + "' WHERE id = " + requestedId );

                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );
                }

                // language
                if ( rp.getLanguage() != null ) {
                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET language = '" + rp.getLanguage()
                                              + "' WHERE id = " + requestedId );

                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );
                }
                // parentidentifier
                if ( qp.getParentIdentifier() != null ) {
                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET parentidentifier = '"
                                              + qp.getParentIdentifier() + "' WHERE id = " + requestedId );

                    buf = sqlStatementUpdate.getBuffer();
                    System.out.println( sqlStatementUpdate.toString() );
                    stm.executeUpdate( sqlStatementUpdate.toString() );
                    buf.setLength( 0 );
                }
                // TODO source

                // TODO association

                // recordBrief, recordSummary, recordFull update
                updateRecord( requestedId );

                executeQueryableProperties( isUpdate );

            } else {
                // TODO think about what response should be written if there is no such dataset in the backend??
                String msg = "No dataset found for the identifier --> " + qp.getIdentifier() + " <--. ";
                throw new SQLException( msg );
            }

            stm.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( IOException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Method that encapsulates the generating for all the queryable properties.
     * 
     * @param isUpdate
     */
    private void executeQueryableProperties( boolean isUpdate ) {

        if ( qp.getIdentifier() != null ) {
            generateQP_IdentifierStatement( isUpdate );
        }
        if ( qp.getTitle() != null ) {
            generateISOQP_TitleStatement( isUpdate );
        }
        if ( qp.getType() != null ) {
            generateISOQP_TypeStatement( isUpdate );
        }
        if ( qp.getKeywords() != null ) {
            generateISOQP_KeywordStatement( isUpdate );
        }
        if ( qp.getTopicCategory() != null ) {
            generateISOQP_TopicCategoryStatement( isUpdate );
        }
        if ( qp.getFormat() != null ) {
            generateISOQP_FormatStatement( isUpdate );
        }
        // TODO relation
        if ( qp.get_abstract() != null ) {
            generateISOQP_AbstractStatement( isUpdate );
        }
        if ( qp.getAlternateTitle() != null ) {
            generateISOQP_AlternateTitleStatement( isUpdate );
        }
        if ( qp.getCreationDate() != null ) {
            generateISOQP_CreationDateStatement( isUpdate );
        }
        if ( qp.getPublicationDate() != null ) {
            generateISOQP_PublicationDateStatement( isUpdate );
        }
        if ( qp.getRevisionDate() != null ) {
            generateISOQP_RevisionDateStatement( isUpdate );
        }
        if ( qp.getResourceIdentifier() != null ) {
            generateISOQP_ResourceIdentifierStatement( isUpdate );
        }
        if ( qp.getServiceType() != null ) {
            generateISOQP_ServiceTypeStatement( isUpdate );
        }
        if ( qp.getServiceTypeVersion() != null ) {
            generateISOQP_ServiceTypeVersionStatement( isUpdate );
        }
        if ( qp.getGeographicDescriptionCode_service() != null ) {
            generateISOQP_GeographicDescriptionCode_ServiceStatement( isUpdate );
        }
        if ( qp.getOperation() != null ) {
            generateISOQP_OperationStatement( isUpdate );
        }
        if ( qp.getDenominator() != 0 || ( qp.getDistanceValue() != 0 && qp.getDistanceUOM() != null ) ) {
            generateISOQP_SpatialResolutionStatement( isUpdate );
        }
        if ( qp.getOrganisationName() != null ) {
            generateISOQP_OrganisationNameStatement( isUpdate );
        }
        if ( qp.getResourceLanguage() != null ) {
            generateISOQP_ResourceLanguageStatement( isUpdate );
        }
        try {
            if ( ( qp.getTemporalExtentBegin().equals( new Date( "0000-00-00" ) ) && qp.getTemporalExtentEnd().equals(
                                                                                                                       new Date(
                                                                                                                                 "0000-00-00" ) ) )
                 || ( qp.getTemporalExtentBegin() != null && qp.getTemporalExtentEnd() != null ) ) {
                generateISOQP_TemporalExtentStatement( isUpdate );
            }
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if ( qp.getOperatesOnData() != null || qp.getOperatesOnData().size() != 0 ) {
            generateISOQP_OperatesOnStatement( isUpdate );
        }
        if ( qp.getCouplingType() != null ) {
            generateISOQP_CouplingTypeStatement( isUpdate );
        }
        // TODO spatial
        if ( qp.getBoundingBox() != null ) {
            generateISOQP_BoundingBoxStatement( isUpdate );
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
    private void generateMainDatabaseDataset() {
        final String databaseTable = "datasets";
        String sqlStatement = "";
        String modifiedAttribute = "null";
        try {

            this.id = getLastDatasetId( connection, databaseTable );
            this.id++;

            if ( qp.getModified() == null || qp.getModified().equals( new Date( "0000-00-00" ) ) ) {
            } else {
                modifiedAttribute = "'" + qp.getModified() + "'";
            }

            sqlStatement += "INSERT INTO "
                            + databaseTable
                            + " (id, version, status, anyText, modified, hassecurityconstraints, language, parentidentifier, source, association) VALUES ("
                            + this.id + ",null,null,'" + qp.getAnyText() + "'," + modifiedAttribute + ","
                            + qp.isHasSecurityConstraints() + ",'" + rp.getLanguage() + "','"
                            + qp.getParentIdentifier() + "',null, null);";
            System.out.println( sqlStatement );
            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

        String isoOMElement = "";

        for ( String databaseTable : tableRecordType.keySet() ) {

            StringWriter sqlStatement = new StringWriter( 500 );
            StringBuffer buf = new StringBuffer();
            OMElement omElement = null;

            try {
                // DC-update
                omElement = factory.createOMElement( tableRecordType.get( databaseTable ), namespaceCSW );

                if ( omElement.getLocalName().equals( BRIEFRECORD ) ) {
                    gr.buildElementAsDcBriefElement( omElement );
                    isoOMElement = gr.getIsoBriefElement().toString();
                } else if ( omElement.getLocalName().equals( SUMMARYRECORD ) ) {
                    gr.buildElementAsDcSummaryElement( omElement );
                    isoOMElement = gr.getIsoSummaryElement().toString();
                } else {
                    gr.buildElementAsDcFullElement( omElement );
                    isoOMElement = gr.getIsoFullElement().toString();
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

                e.printStackTrace();
            } catch ( ParseException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * Generates the ISO representation in brief, summary and full for this dataset.
     * 
     * @throws IOException
     */
    private void generateISO()
                            throws IOException {

        int fk_datasets = this.id;
        int idDatabaseTable = 0;
        for ( String databaseTable : tableRecordType.keySet() ) {
            String sqlStatement = "";
            String isoElements = "";
            if ( databaseTable.equals( RECORDBRIEF ) ) {
                isoElements = gr.getIsoBriefElement().toString();
            } else if ( databaseTable.equals( RECORDSUMMARY ) ) {
                isoElements = gr.getIsoSummaryElement().toString();
            } else {
                isoElements = gr.getIsoFullElement().toString();
            }

            try {

                idDatabaseTable = getLastDatasetId( connection, databaseTable );
                idDatabaseTable++;

                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                               + idDatabaseTable + "," + fk_datasets + ", 2, '" + isoElements + "');";

                stm.executeUpdate( sqlStatement );

            } catch ( SQLException e ) {

                e.printStackTrace();
            }

        }
        generateDC();

    }

    /**
     * Generates the Dublin Core representation in brief, summary and full for this dataset.
     * 
     * @param databaseTable
     *            which should be generated
     */
    private void generateDC() {
        OMElement omElement = null;
        String sqlStatement = "";

        int fk_datasets = this.id;

        int idDatabaseTable = 0;
        for ( String databaseTable : tableRecordType.keySet() ) {

            try {
                recordInsertIDs = new ArrayList<Integer>();

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

                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                               + idDatabaseTable + "," + fk_datasets + ", 1, '" + omElement.toString() + "');";

                recordInsertIDs.add( idDatabaseTable );

                stm.executeUpdate( sqlStatement );

            } catch ( SQLException e ) {

                e.printStackTrace();
            } catch ( ParseException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * Puts the identifier for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateQP_IdentifierStatement( boolean isUpdate ) {
        final String databaseTable = "qp_identifier";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, identifier) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + qp.getIdentifier() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }
    
    /**
     * Puts the anyText for this dataset into the database.
     * 
     * @param isUpdate
     */
    private String generateISOQP_AnyTextStatement(boolean isCaseSensitive) {
        
        StringWriter anyText = new StringWriter();
        String stopWord = " # ";
        if(isCaseSensitive == true){
            //Keywords
            for(Keyword keyword : qp.getKeywords()){
                anyText.append( keyword.getKeywordType() + stopWord );
                anyText.append( keyword.getThesaurus() + stopWord );
                for(String keywordString : keyword.getKeywords()){
                    anyText.append( keywordString + stopWord );
                }
            }
            
            //title
            for(String title : qp.getTitle()){
                anyText.append( title + stopWord );
            }
            
            //abstract
            for(String _abstract : qp.get_abstract()){
                anyText.append( _abstract + stopWord );
            }
            
            //format
            for(Format format : qp.getFormat()){
                anyText.append( format.getName() + stopWord );
                //anyText.append( format.getVersion() + stopWord );
            }
            
            //type
            anyText.append( qp.getType() + stopWord );
            
            //crs
            for(CRS crs : qp.getCrs()){
                anyText.append( crs.getName() + stopWord );
                
            }
            
            //creator
            anyText.append( rp.getCreator() + stopWord );
            
            //contributor
            anyText.append( rp.getContributor() + stopWord );
            
            //publisher
            anyText.append( rp.getPublisher() + stopWord );
            
            //language
            anyText.append( rp.getLanguage() + stopWord );
            
            //relation
            for(String relation : rp.getRelation()){
                anyText.append( relation + stopWord );
            }
            
            //rights
            for(String rights : rp.getRights()){
                anyText.append( rights + stopWord );
            }
            
            //alternateTitle
            for(String alternateTitle : qp.getAlternateTitle()){
                anyText.append( alternateTitle + stopWord );
            }
            
            //organisationName
            anyText.append( qp.getOrganisationName() + stopWord );
            
            //topicCategory
            for( String topicCategory : qp.getTopicCategory()){
                anyText.append( topicCategory + stopWord );
            }
            
            //resourceLanguage
            for(String resourceLanguage : qp.getResourceLanguage()){
                anyText.append( resourceLanguage + stopWord );
            }
            
            //geographicDescriptionCode
            anyText.append( qp.getGeographicDescriptionCode_service() + stopWord );
            
            //spatialResolution
            anyText.append( qp.getDistanceUOM() + stopWord );
            
            //serviceType
            anyText.append( qp.getServiceType() + stopWord );
            
            //operation
            for(String operation : qp.getOperation()){
                anyText.append( operation + stopWord );
            }
            
            //operatesOnData
            for(OperatesOnData data : qp.getOperatesOnData()){
                anyText.append( data.getOperatesOn() + stopWord );
                anyText.append( data.getOperatesOnIdentifier() + stopWord );
                anyText.append( data.getOperatesOnName() + stopWord );
                
            }
            
            //couplingType
            anyText.append( qp.getCouplingType() + stopWord );
            
            
            
        }
        
        
        
        return anyText.toString();

    }

    /**
     * Puts the organisationname for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OrganisationNameStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_organisationname";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, organisationname) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + qp.getOrganisationName() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the temporalextent for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TemporalExtentStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_temporalextent";
        String sqlStatement = "";
        String tempBeginAttribute = "";
        String tempEndAttribute = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }

            if ( qp.getTemporalExtentBegin() == null || qp.getTemporalExtentBegin().equals( new Date( "0000-00-00" ) ) ) {
            } else {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable
                               + " (id, fk_datasets, tempextent_begin, tempextent_end) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + tempBeginAttribute + "','" + tempEndAttribute + "');";
                System.out.println( sqlStatement );
                stm.executeUpdate( sqlStatement );
            }

        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Puts the spatialresolution for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_SpatialResolutionStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_spatialresolution";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable
                           + " (id, fk_datasets, denominator, distancevalue, distanceuom) VALUES (" + id + ","
                           + mainDatabaseTableID + "," + qp.getDenominator() + "," + qp.getDistanceValue() + ",'"
                           + qp.getDistanceUOM() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the couplingtype for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_CouplingTypeStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_couplingtype";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, couplingtype) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + qp.getCouplingType() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the operatesondata for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperatesOnStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_operatesondata";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            for ( OperatesOnData operatesOnData : qp.getOperatesOnData() ) {
            id++;
            sqlStatement = "INSERT INTO " + databaseTable
                           + " (id, fk_datasets, operateson, operatesonidentifier, operatesonname) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + operatesOnData.getOperatesOn() + "','" + operatesOnData.getOperatesOnIdentifier()
                           + "','" + operatesOnData.getOperatesOnName() + "');";

            stm.executeUpdate( sqlStatement );
            }
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the operation for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperationStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_operation";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }

            id = getLastDatasetId( connection, databaseTable );
            for ( String operation : qp.getOperation() ) {

                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, operation) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + operation + "');";

                stm.executeUpdate( sqlStatement );

            }

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the geographicDescriptionCode for the type "service" for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_GeographicDescriptionCode_ServiceStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_geographicdescriptioncode";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, geographicdescriptioncode) VALUES ("
                           + id + "," + mainDatabaseTableID + ",'" + qp.getGeographicDescriptionCode_service() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the servicetypeversion for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeVersionStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_servicetypeversion";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, servicetypeversion) VALUES (" + id
                           + "," + mainDatabaseTableID + ",'" + qp.getServiceTypeVersion() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the servicetype for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_servicetype";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, servicetype) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + qp.getServiceType() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the resourcelanguage for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceLanguageStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_resourcelanguage";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, resourcelanguage) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + qp.getResourceLanguage() + "');";

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the revisiondate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_RevisionDateStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_revisiondate";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        String revisionDateAttribute = "null";
        int id = 0;
        try {

            if ( qp.getRevisionDate() == null || qp.getRevisionDate().equals( new Date( "0000-00-00" ) ) ) {
            } else {
                revisionDateAttribute = "'" + qp.getRevisionDate() + "'";

                if ( isUpdate == true ) {
                    sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                    stm.executeUpdate( sqlStatement );
                }
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, revisiondate) VALUES (" + id + ","
                               + mainDatabaseTableID + "," + revisionDateAttribute + ");";

                System.out.println( sqlStatement );
                stm.executeUpdate( sqlStatement );
            }
        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the creationdate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_CreationDateStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_creationdate";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        String creationDateAttribute = "null";
        int id = 0;
        try {

            if ( qp.getCreationDate() == null || qp.getCreationDate().equals( new Date( "0000-00-00" ) ) ) {
            } else {
                creationDateAttribute = "'" + qp.getCreationDate() + "'";

                if ( isUpdate == true ) {
                    sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                    stm.executeUpdate( sqlStatement );
                }
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, creationdate) VALUES (" + id + ","
                               + mainDatabaseTableID + "," + creationDateAttribute + ");";

                stm.executeUpdate( sqlStatement );
            }
        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Puts the publicationdate for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_PublicationDateStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_publicationdate";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        String publicationDateAttribute = "null";
        int id = 0;
        try {

            if ( qp.getPublicationDate() == null || qp.getPublicationDate().equals( new Date( "0000-00-00" ) ) ) {
            } else {
                publicationDateAttribute = "'" + qp.getPublicationDate() + "'";

                if ( isUpdate == true ) {
                    sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                    stm.executeUpdate( sqlStatement );
                }
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, publicationdate) VALUES (" + id
                               + "," + mainDatabaseTableID + "," + publicationDateAttribute + ");";

                stm.executeUpdate( sqlStatement );
            }
        } catch ( SQLException e ) {

            e.printStackTrace();
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Puts the resourceIdentifier for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceIdentifierStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_resourceIdentifier";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }

            id = getLastDatasetId( connection, databaseTable );
            for ( String resourceId : qp.getResourceIdentifier() ) {

                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, resourceidentifier) VALUES (" + id
                               + "," + mainDatabaseTableID + ",'" + resourceId + "');";

                stm.executeUpdate( sqlStatement );

            }

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the alternatetitle for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_AlternateTitleStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_alternatetitle";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            for ( String alternateTitle : qp.getAlternateTitle() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, alternatetitle) VALUES (" + id
                               + "," + mainDatabaseTableID + ",'" + alternateTitle + "');";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the title for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TitleStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_title";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            for ( String title : qp.getTitle() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, title) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + title + "');";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the type for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TypeStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_type";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            id++;
            sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, type) VALUES (" + id + ","
                           + mainDatabaseTableID + ",'" + qp.getType() + "');";

            System.out.println( sqlStatement );
            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the keyword for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_KeywordStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_keyword";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }

            id = getLastDatasetId( connection, databaseTable );
            for ( Keyword keyword : qp.getKeywords() ) {
                for ( String keywordString : keyword.getKeywords() ) {

                    id++;
                    sqlStatement = "INSERT INTO " + databaseTable
                                   + " (id, fk_datasets, keywordtype, keyword, thesaurus) VALUES (" + id + ","
                                   + mainDatabaseTableID + ",'" + keyword.getKeywordType() + "','" + keywordString
                                   + "','" + keyword.getThesaurus() + "');";

                    stm.executeUpdate( sqlStatement );
                }
            }

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the topiccategory for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TopicCategoryStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_topiccategory";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }

            id = getLastDatasetId( connection, databaseTable );
            for ( String topicCategory : qp.getTopicCategory() ) {

                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, topiccategory) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + topicCategory + "');";

                stm.executeUpdate( sqlStatement );

            }

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the format for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_FormatStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_format";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );

            for ( Format format : qp.getFormat() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + format.getName() + "');";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the abstract for this dataset into the database.
     * 
     * @param isUpdate
     */
    private void generateISOQP_AbstractStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_abstract";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == true ) {
                sqlStatement = "DELETE FROM " + databaseTable + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                stm.executeUpdate( sqlStatement );
            }
            id = getLastDatasetId( connection, databaseTable );
            for ( String _abstract : qp.get_abstract() ) {
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, abstract) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + _abstract + "');";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the boundingbox for this dataset into the database.
     * 
     * @param isUpdate
     */
    // TODO one record got one or more bboxes?
    private void generateISOQP_BoundingBoxStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_boundingbox";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, bbox) VALUES (" + id + ","
                               + mainDatabaseTableID + ",SetSRID('BOX3D(" + qp.getBoundingBox().getEastBoundLongitude()
                               + " " + qp.getBoundingBox().getNorthBoundLatitude() + ","
                               + qp.getBoundingBox().getWestBoundLongitude() + " "
                               + qp.getBoundingBox().getSouthBoundLatitude() + ")'::box3d,4326));";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET bbox = " + "SetSRID('BOX3D("
                               + qp.getBoundingBox().getEastBoundLongitude() + " "
                               + qp.getBoundingBox().getNorthBoundLatitude() + ","
                               + qp.getBoundingBox().getWestBoundLongitude() + " "
                               + qp.getBoundingBox().getSouthBoundLatitude() + ")'::box3d,4326) WHERE fk_datasets = "
                               + mainDatabaseTableID + ";";
            }
            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Puts the crs for this dataset into the database.<br>
     * Creation of the CRS element. <br>
     * TODO its not clear where to get all the elements...
     * 
     * @param isUpdate
     */
    private void generateISOQP_CRSStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_crs";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                for ( CRS crs : qp.getCrs() ) {
                    id++;
                    sqlStatement = "INSERT INTO " + databaseTable
                                   + " (id, fk_datasets, authority, id_crs, version) VALUES (" + id + ","
                                   + mainDatabaseTableID + "," + crs.getName() + "," + crs.EPSG_4326 + ","
                                   + crs.getName() + ");";
                    stm.executeUpdate( sqlStatement );
                }
            } else {
                sqlStatement = "";
            }

        } catch ( SQLException e ) {

            e.printStackTrace();
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
     * @return the recordInsertIDs
     */
    public List<Integer> getRecordInsertIDs() {
        return recordInsertIDs;
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
    private String generateUUID() {

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

            e.printStackTrace();
        }

        if ( uuidIsEqual == true ) {
            return generateUUID();
        } else {
            return uuid;
        }

    }

}
