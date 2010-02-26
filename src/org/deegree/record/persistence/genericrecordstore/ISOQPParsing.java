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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * The parsing for the ISO and DC application profile. Here the input XML document is parsed into its parts. So this is
 * the entry point to generate a record that fits with the backend. The queryable and returnable properties are
 * disentangled. This is needed to put them into the queryable property tables in the backend and makes them queryable.
 * In this context they are feasible to build the Dublin Core record which has nearly planar elements with no nested
 * areas.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public final class ISOQPParsing extends XMLAdapter {

    private static final Logger LOG = getLogger( ISOQPParsing.class );

    private NamespaceContext nsContextISOParsing = new NamespaceContext( XMLAdapter.nsContext );

    private OMFactory factory = OMAbstractFactory.getOMFactory();

    private OMNamespace namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "" );

    private OMNamespace namespaceGCO = factory.createOMNamespace( "http://www.isotc211.org/2005/gco", "gco" );

    private QueryableProperties qp;

    private ReturnableProperties rp;

    private GenerateRecord gr;

    private OMElement et;

    private List<OMElement> etList;

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
     * @param element
     *            the XML element that has to be parsed to be able to generate needed database properties
     * @param isInspire
     *            if the INSPIRE directive is set
     * @param connection
     * @return {@link ParsedProfileElement}
     * 
     * 
     * @throws IOException
     */
    public ParsedProfileElement parseAPISO( OMElement element, boolean isInspire, Connection connection )
                            throws IOException {

        setRootElement( element );
        if ( element.getDefaultNamespace() != null ) {
            nsContextISOParsing.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                              rootElement.getDefaultNamespace().getNamespaceURI() );
        }
        nsContextISOParsing.addNamespace( CSWConstants.CSW_PREFIX, CSWConstants.CSW_202_NS );
        nsContextISOParsing.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContextISOParsing.addNamespace( "ows", "http://www.opengis.net/ows" );

        gr = new GenerateRecord();
        qp = new QueryableProperties();

        rp = new ReturnableProperties();

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

            gr.setIdentifier( getElement( rootElement, new XPath( "./gmd:fileIdentifier", nsContextISOParsing ) ) );

        }

        /*---------------------------------------------------------------
         * 
         * 
         * (default) Language
         * 
         * 
         *---------------------------------------------------------------*/
        String l = getNodeAsString(
                                    rootElement,
                                    new XPath(
                                               "./gmd:language/gco:CharacterString | ./gmd:language/gmd:LanguageCode/@codeListValue",
                                               nsContextISOParsing ), null );

        // Locale locale = new Locale(
        // getNodeAsString(
        // rootElement,
        // new XPath(
        // "./gmd:language/gco:CharacterString | ./gmd:language/gmd:LanguageCode/@codeListValue",
        // nsContextISOParsing ), null ) );

        qp.setLanguage( l );
        // LOG.info( getElement( rootElement, new XPath( "./gmd:language", nsContextISOParsing ) ).toString() );
        et = getElement( rootElement, new XPath( "./gmd:language", nsContextISOParsing ) );
        if ( et != null ) {

            gr.setLanguage( getElement( rootElement, new XPath( "./gmd:language", nsContextISOParsing ) ) );

        }

        /*---------------------------------------------------------------
         * 
         * 
         * CharacterSet
         * 
         * 
         *---------------------------------------------------------------*/
        et = getElement( rootElement, new XPath( "./gmd:characterSet", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setCharacterSet( getElement( rootElement, new XPath( "./gmd:characterSet", nsContextISOParsing ) ) );

        }

        /*---------------------------------------------------------------
         * 
         * 
         * ParentIdentifier
         * 
         * 
         *---------------------------------------------------------------*/
        qp.setParentIdentifier( getNodeAsString( rootElement, new XPath( "./gmd:parentIdentifier/gco:CharacterString",
                                                                         nsContextISOParsing ), null ) );

        et = getElement( rootElement, new XPath( "./gmd:parentIdentifier", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setParentIdentifier( getElement( rootElement, new XPath( "./gmd:parentIdentifier", nsContextISOParsing ) ) );

        }

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

        etList = getElements( rootElement, new XPath( "./gmd:hierarchyLevel", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setHierarchyLevel( getElements( rootElement, new XPath( "./gmd:hierarchyLevel", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * HierarchieLevelName
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:hierarchyLevelName", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setHierarchyLevelName( getElements( rootElement, new XPath( "./gmd:hierarchyLevelName",
                                                                           nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * Contact
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:contact", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setContact( getElements( rootElement, new XPath( "./gmd:contact", nsContextISOParsing ) ) );

        }
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

        et = getElement( rootElement, new XPath( "./gmd:dateStamp", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setDateStamp( getElement( rootElement, new XPath( "./gmd:dateStamp", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * MetadataStandardName
         * 
         * 
         *---------------------------------------------------------------*/

        et = getElement( rootElement, new XPath( "./gmd:metadataStandardName", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setMetadataStandardName( getElement( rootElement, new XPath( "./gmd:metadataStandardName",
                                                                            nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * MetadataStandardVersion
         * 
         * 
         *---------------------------------------------------------------*/

        et = getElement( rootElement, new XPath( "./gmd:metadataStandardVersion", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setMetadataStandardVersion( getElement( rootElement, new XPath( "./gmd:metadataStandardVersion",
                                                                               nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * DataSetURI
         * 
         * 
         *---------------------------------------------------------------*/

        et = getElement( rootElement, new XPath( "./gmd:dataSetURI", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setDataSetURI( getElement( rootElement, new XPath( "./gmd:dataSetURI", nsContextISOParsing ) ) );

        }

        /*---------------------------------------------------------------
         * 
         * 
         * Locale (for multilinguarity)
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:locale", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setLocale( getElements( rootElement, new XPath( "./gmd:locale", nsContextISOParsing ) ) );

        }

        /*---------------------------------------------------------------
         * 
         * 
         * SpatialRepresentationInfo
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:spatialRepresentationInfo", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setSpatialRepresentationInfo( getElements( rootElement, new XPath( "./gmd:spatialRepresentationInfo",
                                                                                  nsContextISOParsing ) ) );

        }
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

        etList = getElements( rootElement, new XPath( "./gmd:referenceSystemInfo", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setReferenceSystemInfo( getElements( rootElement, new XPath( "./gmd:referenceSystemInfo",
                                                                            nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * MetadataExtensionInfo
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:metadataExtensionInfo", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setMetadataExtensionInfo( getElements( rootElement, new XPath( "./gmd:metadataExtensionInfo",
                                                                              nsContextISOParsing ) ) );

        }
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

        etList = getElements( rootElement, new XPath( "./gmd:contentInfo", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setContentInfo( getElements( rootElement, new XPath( "./gmd:contentInfo", nsContextISOParsing ) ) );

        }
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

        et = getElement( rootElement, new XPath( "./gmd:distributionInfo", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setDistributionInfo( getElement( rootElement, new XPath( "./gmd:distributionInfo", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * DataQualityInfo
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:contentInfo", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setDataQualityInfo( getElements( rootElement, new XPath( "./gmd:dataQualityInfo", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * PortrayalCatalogueInfo
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:portrayalCatalogueInfo", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setPortrayalCatalogueInfo( getElements( rootElement, new XPath( "./gmd:portrayalCatalogueInfo",
                                                                               nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * MetadataConstraints
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:metadataConstraints", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setMetadataConstraints( getElements( rootElement, new XPath( "./gmd:metadataConstraints",
                                                                            nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * ApplicationSchemaInfo
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:applicationSchemaInfo", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setApplicationSchemaInfo( getElements( rootElement, new XPath( "./gmd:applicationSchemaInfo",
                                                                              nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * MetadataMaintenance
         * 
         * 
         *---------------------------------------------------------------*/

        et = getElement( rootElement, new XPath( "./gmd:metadataMaintenance", nsContextISOParsing ) );

        if ( et != null ) {
            gr.setMetadataMaintenance( getElement( rootElement, new XPath( "./gmd:metadataMaintenance",
                                                                           nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * Series
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:series", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setSeries( getElements( rootElement, new XPath( "./gmd:series", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * Describes
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:describes", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setDescribes( getElements( rootElement, new XPath( "./gmd:describes", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * PropertyType
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:propertyType", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setPropertyType( getElements( rootElement, new XPath( "./gmd:propertyType", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * FeatureType
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:featureType", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setFeatureType( getElements( rootElement, new XPath( "./gmd:featureType", nsContextISOParsing ) ) );

        }
        /*---------------------------------------------------------------
         * 
         * 
         * FeatureAttribute
         * 
         * 
         *---------------------------------------------------------------*/

        etList = getElements( rootElement, new XPath( "./gmd:featureAttribute", nsContextISOParsing ) );

        if ( etList != null ) {
            gr.setFeatureAttribute( getElements( rootElement, new XPath( "./gmd:featureAttribute", nsContextISOParsing ) ) );

        }
        /*
         * sets the properties that are needed for building DC records
         */
        gr.setQueryableProperties( qp );
        gr.setReturnableProperties( rp );

        return new ParsedProfileElement( qp, rp, gr );

    }

    /**
     * This method parses the OMElement regarding to the Dublin Core profile.
     * 
     * @param element
     * @return {@link ParsedProfileElement}
     * 
     * @throws IOException
     */
    public ParsedProfileElement parseAPDC( OMElement element )
                            throws IOException {

        setRootElement( element );
        if ( element.getDefaultNamespace() != null ) {
            nsContextISOParsing.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                              rootElement.getDefaultNamespace().getNamespaceURI() );
        }

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

        return new ParsedProfileElement( qp, rp, gr );

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
