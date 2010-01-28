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
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
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

    private OMFactory factory = OMAbstractFactory.getOMFactory();

    private OMNamespace namespaceCSW = factory.createOMNamespace( "http://www.opengis.net/cat/csw/2.0.2", "csw" );

    private OMNamespace namespaceDC = factory.createOMNamespace( "http://purl.org/dc/elements/1.1/", "dc" );

    private OMNamespace namespaceDCT = factory.createOMNamespace( "http://purl.org/dc/terms/", "dct" );

    private OMNamespace namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "" );

    private OMNamespace namespaceOWS = factory.createOMNamespace( "http://www.opengis.net/ows", "ows" );

    private int id;

    private Connection connection;

    private OMElement elementFull;

    private OMElement identifier = null;

    private OMElement hierarchyLevel = null;

    private OMElement hierarchyLevelName = null;

    private OMElement language = null;

    private OMElement dataQualityInfo = null;

    private OMElement characterSet = null;

    private OMElement metadataStandardName = null;

    private OMElement metadataStandardVersion = null;

    private OMElement parentIdentifier = null;

    private OMElement identificationInfo = null;

    private List<OMElement> referenceSystemInfo = new ArrayList<OMElement>();

    private OMElement distributionInfo = null;

    private OMElement dateStamp = null;

    private Statement stm;

    private List<Integer> recordInsertIDs;

    private List<CRS> crsList = new ArrayList<CRS>();

    /**
     * 
     * 
     * @param element
     * @param connection
     */
    public ISOQPParsing( OMElement element, Connection connection ) {

        this.elementFull = element.cloneOMElement();
        this.connection = connection;

        setRootElement( element );
        nsContext.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                rootElement.getDefaultNamespace().getNamespaceURI() );
        nsContext.addNamespace( CSW_PREFIX, CSWConstants.CSW_202_NS );
        nsContext.addNamespace( "srv", "http://www.isotc211.org/2005/srv" );
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows" );

        // try {
        //
        // parseAPISO( element );
        //
        // } catch ( IOException e ) {
        //
        // e.printStackTrace();
        // }
    }

    private List<String> validate( OMElement elem ) {
        StringWriter s = new StringWriter();
        try {
            elem.serialize( s );
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StringReader reader = new StringReader( s.toString() );
        return SchemaValidator.validate( reader, "http://schemas.opengis.net/iso/19139/20070417/gmd/metadataEntity.xsd" );
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
     */
    public void parseAPISO( boolean isInspire )
                            throws IOException {
        // qp.setAnyText( element.toString() );

        // /*/self::node() => /*/self::* => /* -> Transaction
        // */MD_Metadata -> null
        // //MD_Metadata => ./gmd:MD_Metadata -> null
        // */self::* => * => ./* => ./child::*-> fileIdentifier, identificationInfo
        // ./. => ./self::node() -> MD_Metadata
        // //. -> jedes element...alles also
        // /*/*/gmd:MD_Metadata -> MD_Metadata
        List<OMElement> recordElements = getElements( rootElement, new XPath( "*", nsContext ) );
        for ( OMElement elem : recordElements ) {

            if ( elem.getLocalName().equals( "fileIdentifier" ) ) {
                qp.setIdentifier( getNodeAsString( elem, new XPath( "./gco:CharacterString", nsContext ), null ) );

                identifier = elem;
                OMNamespace namespace = identifier.getNamespace();
                identifier.setNamespace( namespace );

                continue;

            }

            /**
             * if provided data is a dataset: type = dataset (default)
             * <p>
             * if provided data is a datasetCollection: type = series
             * <p>
             * if provided data is an application: type = application
             * <p>
             * if provided data is a service: type = service
             */
            if ( elem.getLocalName().equals( "hierarchyLevel" ) ) {

                String type = getNodeAsString( elem, new XPath( "./gmd:MD_ScopeCode/@codeListValue", nsContext ),
                                               "dataset" );

                qp.setType( type );

                hierarchyLevel = elem;
                OMNamespace namespace = hierarchyLevel.getNamespace();
                hierarchyLevel.setNamespace( namespace );

                continue;
            }

            if ( elem.getLocalName().equals( "hierarchyLevelName" ) ) {

                hierarchyLevelName = elem;
                OMNamespace namespace = hierarchyLevelName.getNamespace();
                hierarchyLevelName.setNamespace( namespace );

                continue;
            }

            if ( elem.getLocalName().equals( "dateStamp" ) ) {

                String dateString = getNodeAsString( elem, new XPath( "./gco:Date", nsContext ), "0000-00-00" );
                Date date = null;
                try {
                    date = new Date( dateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setModified( date );
                // String[] dateStrings = getNodesAsStrings( elem, new XPath( "./gco:Date", nsContext ) );
                // Date[] dates = new Date[dateStrings.length];
                // Date date = null;
                // for ( int i = 0; i < dateStrings.length; i++ ) {
                // try {
                // date = new Date( dateStrings[i] );
                // } catch ( ParseException e ) {
                //
                // e.printStackTrace();
                // }
                // dates[i] = date;
                //
                // }
                //
                // qp.setModified( Arrays.asList( dates ) );

                dateStamp = elem;
                OMNamespace namespace = dateStamp.getNamespace();
                dateStamp.setNamespace( namespace );

                continue;
            }

            if ( elem.getLocalName().equals( "referenceSystemInfo" ) ) {
                List<OMElement> crsElements = getElements(
                                                           elem,
                                                           new XPath(
                                                                      "./gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier",
                                                                      nsContext ) );

                for ( OMElement crsElement : crsElements ) {
                    String crsIdentification = getNodeAsString(
                                                                crsElement,
                                                                new XPath( "./gmd:code/gco:CharacterString", nsContext ),
                                                                "" );

                    String crsAuthority = getNodeAsString(
                                                           crsElement,
                                                           new XPath( "./gmd:codeSpace/gco:CharacterString", nsContext ),
                                                           "" );

                    String crsVersion = getNodeAsString( crsElement, new XPath( "./gmd:version/gco:CharacterString",
                                                                                nsContext ), "" );
                    // CRS crs = new CRS( crsAuthority, crsIdentification, crsVersion );

                    CRS crs = new CRS( crsIdentification );

                    crsList.add( crs );
                }

                referenceSystemInfo.add( elem );

                continue;

            }

            if ( elem.getLocalName().equals( "language" ) ) {

                rp.setLanguage( getNodeAsString( elem, new XPath( "./gco:CharacterString", nsContext ), null ) );
                language = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "dataQualityInfo" ) ) {

                dataQualityInfo = elem;

                continue;
            }

            if ( elem.getLocalName().equals( "characterSet" ) ) {

                characterSet = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "metadataStandardName" ) ) {

                metadataStandardName = elem;
                continue;
            }

            if ( elem.getLocalName().equals( "metadataStandardVersion" ) ) {

                metadataStandardVersion = elem;
                continue;
            }
            if ( elem.getLocalName().equals( "parentIdentifier" ) ) {
                qp.setParentIdentifier( getNodeAsString( elem, new XPath( "./gco:CharacterString", nsContext ), null ) );

                parentIdentifier = elem;
                OMNamespace namespace = parentIdentifier.getNamespace();
                parentIdentifier.setNamespace( namespace );

                continue;

            }

            if ( elem.getLocalName().equals( "identificationInfo" ) ) {

                OMElement md_identification = getElement( elem, new XPath( "./gmd:MD_Identification", nsContext ) );

                OMElement sv_service_OR_md_dataIdentification = getElement(
                                                                            elem,
                                                                            new XPath(
                                                                                       "./srv:SV_ServiceIdentification | gmd:MD_DataIdentification",
                                                                                       nsContext ) );

                OMElement ci_responsibleParty = getElement( sv_service_OR_md_dataIdentification,
                                                            new XPath( "./gmd:pointOfContact/gmd:CI_ResponsibleParty",
                                                                       nsContext ) );

                OMElement spatialResolution = getElement( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:spatialResolution", nsContext ) );

                String temporalExtentBegin = getNodeAsString(
                                                              sv_service_OR_md_dataIdentification,
                                                              new XPath(
                                                                         "./gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:beginPosition",
                                                                         nsContext ), "0000-00-00" );
                Date dateTempBeg = null;
                try {
                    dateTempBeg = new Date( temporalExtentBegin );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setTemporalExtentBegin( dateTempBeg );

                String temporalExtentEnd = getNodeAsString(
                                                            sv_service_OR_md_dataIdentification,
                                                            new XPath(
                                                                       "./gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:endPosition",
                                                                       nsContext ), "0000-00-00" );
                Date dateTempEnd = null;
                try {
                    dateTempEnd = new Date( temporalExtentEnd );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setTemporalExtentEnd( dateTempEnd );

                int denominator = getNodeAsInt(
                                                spatialResolution,
                                                new XPath(
                                                           "./gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer",
                                                           nsContext ), -1 );
                qp.setDenominator( denominator );

                // TODO put here the constraint that there can a denominator be available iff distanceValue and
                // distanceUOM are not set and vice versa!!
                float distanceValue = getNodeAsFloat( spatialResolution,
                                                      new XPath( "./gmd:MD_Resolution/gmd:distance/gco:Distance",
                                                                 nsContext ), -1 );
                qp.setDistanceValue( distanceValue );

                String distanceUOM = getNodeAsString( spatialResolution,
                                                      new XPath( "./gmd:MD_Resolution/gmd:distance/gco:Distance/@uom",
                                                                 nsContext ), null );
                qp.setDistanceUOM( distanceUOM );

                String serviceType = getNodeAsString( sv_service_OR_md_dataIdentification,
                                                      new XPath( "./srv:serviceType", nsContext ), null );
                qp.setServiceType( serviceType );

                String serviceTypeVersion = getNodeAsString( sv_service_OR_md_dataIdentification,
                                                             new XPath( "./srv:serviceTypeVersion", nsContext ), null );
                qp.setServiceTypeVersion( serviceTypeVersion );

                String operation = getNodeAsString(
                                                    sv_service_OR_md_dataIdentification,
                                                    new XPath(
                                                               "./srv:containsOperations/srv:SV_OperationMetadata/srv:operationName",
                                                               nsContext ), null );

                String operation_dcp = getNodeAsString(
                                                        sv_service_OR_md_dataIdentification,
                                                        new XPath(
                                                                   "./srv:containsOperations/srv:SV_OperationMetadata/srv:DCP",
                                                                   nsContext ), null );

                String operation_linkage = getNodeAsString(
                                                            sv_service_OR_md_dataIdentification,
                                                            new XPath(
                                                                       "./srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/srv:CI_OnlineResource/srv:linkage/srv:URL",
                                                                       nsContext ), null );

                qp.setOperation( operation );

                String geographicDescriptionCode_service = getNodeAsString(
                                                                            sv_service_OR_md_dataIdentification,
                                                                            new XPath(
                                                                                       "./srv:extent/srv:EX_Extent/srv:geographicElement/srv:EX_GeopraphicDescription/srv:geographicIdentifier/srv:MD_Identifier/srv:code",
                                                                                       nsContext ), null );
                qp.setGeographicDescriptionCode_service( geographicDescriptionCode_service );

                String[] resourceIdentifierList = getNodesAsStrings(
                                                                     sv_service_OR_md_dataIdentification,
                                                                     new XPath(
                                                                                "./gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString",
                                                                                nsContext ) );

                for ( int i = 0; i < resourceIdentifierList.length; i++ ) {
                    String at = sv_service_OR_md_dataIdentification.getAttributeValue( new QName( "id" ) );
                    System.out.println( at );
                }

                String operatesOn = getNodeAsString(
                                                     sv_service_OR_md_dataIdentification,
                                                     new XPath(
                                                                "./srv:operatesOn/srv:MD_DataIdentification/srv:citation/srv:CI_Citation/srv:identifier",
                                                                nsContext ), null );
                qp.setOperatesOn( operatesOn );

                String operatesOnIdentifier = getNodeAsString(
                                                               sv_service_OR_md_dataIdentification,
                                                               new XPath(
                                                                          "./srv:coupledResource/srv:SV_CoupledResource/srv:identifier",
                                                                          nsContext ), null );
                qp.setOperatesOnIdentifier( operatesOnIdentifier );

                String operatesOnName = getNodeAsString(
                                                         sv_service_OR_md_dataIdentification,
                                                         new XPath(
                                                                    "./srv:coupledResource/srv:SV_CoupledResource/srv:operationName",
                                                                    nsContext ), null );
                qp.setOperatesOnName( operatesOnName );

                String couplingType = getNodeAsString(
                                                       sv_service_OR_md_dataIdentification,
                                                       new XPath(
                                                                  "./srv:couplingType/srv:SV_CouplingType/srv:code/@codeListValue",
                                                                  nsContext ), null );
                qp.setCouplingType( couplingType );

                String resourceLanguage = getNodeAsString(
                                                           sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:language/gco:CharacterString", nsContext ),
                                                           null );
                qp.setResourceLanguage( resourceLanguage );

                String creator = getNodeAsString(
                                                  ci_responsibleParty,
                                                  new XPath(
                                                             "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gco:CharacterString",
                                                             nsContext ), null );

                rp.setCreator( creator );

                String publisher = getNodeAsString(
                                                    ci_responsibleParty,
                                                    new XPath(
                                                               "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']/gco:CharacterString",
                                                               nsContext ), null );

                rp.setPublisher( publisher );

                String contributor = getNodeAsString(
                                                      ci_responsibleParty,
                                                      new XPath(
                                                                 "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='author']/gco:CharacterString",
                                                                 nsContext ), null );
                rp.setContributor( contributor );

                String[] rightsElements = getNodesAsStrings(
                                                             sv_service_OR_md_dataIdentification,
                                                             new XPath(
                                                                        "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/@codeListValue",
                                                                        nsContext ) );
                rp.setRights( Arrays.asList( rightsElements ) );

                // OMElement sv_serviceIdentification = getElement( elem, new XPath( "./srv:SV_ServiceIdentification",
                // nsContext ) );

                // String couplingType = getNodeAsString( sv_serviceIdentification, new XPath(
                // "./srv:couplingType/srv:SV_CouplingType/@codeListValue", nsContext ), null );

                OMElement _abstract = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:abstract",
                                                                                                  nsContext ) );

                OMElement bbox = getRequiredElement(
                                                     sv_service_OR_md_dataIdentification,
                                                     new XPath(
                                                                "./gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox",
                                                                nsContext ) );

                List<OMElement> descriptiveKeywords = getElements( sv_service_OR_md_dataIdentification,
                                                                   new XPath( "./gmd:descriptiveKeywords", nsContext ) );

                String[] topicCategories = getNodesAsStrings(
                                                              sv_service_OR_md_dataIdentification,
                                                              new XPath(
                                                                         "./gmd:topicCategory/gmd:MD_TopicCategoryCode",
                                                                         nsContext ) );

                String graphicOverview = getNodeAsString( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:graphicOverview/gmd:MD_BrowseGraphic",
                                                                     nsContext ), null );

                String[] titleElements = getNodesAsStrings(
                                                            sv_service_OR_md_dataIdentification,
                                                            new XPath(
                                                                       "./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                                       nsContext ) );

                String[] alternateTitleElements = getNodesAsStrings(
                                                                     sv_service_OR_md_dataIdentification,
                                                                     new XPath(
                                                                                "./gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString",
                                                                                nsContext ) );

                double boundingBoxWestLongitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:westBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxEastLongitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:eastBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxSouthLatitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:southBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                double boundingBoxNorthLatitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:northBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );

                qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                                    boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );

                // TODO
                CRS crs = new CRS( "EPSG:4326" );
                crsList.add( crs );

                qp.setTitle( Arrays.asList( titleElements ) );

                qp.setAlternateTitle( Arrays.asList( alternateTitleElements ) );

                rp.setGraphicOverview( graphicOverview );

                Keyword keywordClass;

                List<Keyword> listOfKeywords = new ArrayList<Keyword>();
                for ( OMElement md_keywords : descriptiveKeywords ) {
                    keywordClass = new Keyword();

                    String keywordType = getNodeAsString(
                                                          md_keywords,
                                                          new XPath(
                                                                     "./gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue",
                                                                     nsContext ), null );

                    String[] keywords = getNodesAsStrings(
                                                           md_keywords,
                                                           new XPath(
                                                                      "./gmd:MD_Keywords/gmd:keyword/gco:CharacterString",
                                                                      nsContext ) );

                    String thesaurus = getNodeAsString(
                                                        md_keywords,
                                                        new XPath(
                                                                   "./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                                   nsContext ), null );

                    keywordClass.setKeywordType( keywordType );

                    keywordClass.setKeywords( Arrays.asList( keywords ) );

                    keywordClass.setThesaurus( thesaurus );
                    listOfKeywords.add( keywordClass );

                }

                qp.setKeywords( listOfKeywords );
                qp.setTopicCategory( Arrays.asList( topicCategories ) );

                String revisionDateString = getNodeAsString(
                                                             sv_service_OR_md_dataIdentification,
                                                             new XPath(
                                                                        "./gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date",
                                                                        nsContext ), "0000-00-00" );
                Date date = null;
                try {
                    date = new Date( revisionDateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setRevisionDate( date );

                String creationDateString = getNodeAsString(
                                                             sv_service_OR_md_dataIdentification,
                                                             new XPath(
                                                                        "././gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date",
                                                                        nsContext ), "0000-00-00" );

                try {
                    date = new Date( creationDateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setCreationDate( date );

                String publicationDateString = getNodeAsString(
                                                                sv_service_OR_md_dataIdentification,
                                                                new XPath(
                                                                           "././gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date",
                                                                           nsContext ), "0000-00-00" );

                try {
                    date = new Date( publicationDateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setPublicationDate( date );

                String relation = getNodeAsString( md_identification,
                                                   new XPath( "./gmd:aggreationInfo/gco:CharacterString", nsContext ),
                                                   null );
                rp.setRelation( relation );

                String[] rightsStrings = getNodesAsStrings(
                                                            md_identification,
                                                            new XPath(
                                                                       "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/@codeListValue",
                                                                       nsContext ) );

                rp.setRights( Arrays.asList( rightsStrings ) );

                String organisationName = getNodeAsString(
                                                           md_identification,
                                                           new XPath(
                                                                      "./gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString",
                                                                      nsContext ), null );

                qp.setOrganisationName( organisationName );

                boolean hasSecurityConstraint = getNodeAsBoolean(
                                                                  md_identification,
                                                                  new XPath(
                                                                             "./gmd:MD_Identification/gmd:resourceConstraints/gmd:MD_SecurityConstraints",
                                                                             nsContext ), false );
                qp.setHasSecurityConstraints( hasSecurityConstraint );

                String[] _abstractStrings = getNodesAsStrings( _abstract,
                                                               new XPath( "./gco:CharacterString", nsContext ) );

                qp.set_abstract( Arrays.asList( _abstractStrings ) );

                identificationInfo = elem;
                OMNamespace namespace = identificationInfo.getNamespace();
                identificationInfo.setNamespace( namespace );
                continue;

            }

            if ( elem.getLocalName().equals( "distributionInfo" ) ) {
                List<OMElement> formats = getElements(
                                                       elem,
                                                       new XPath(
                                                                  "./gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format",
                                                                  nsContext ) );

                String onlineResource = getNodeAsString(
                                                         elem,
                                                         new XPath(
                                                                    "./gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
                                                                    nsContext ), null );

                Format formatClass = null;
                List<Format> listOfFormats = new ArrayList<Format>();
                for ( OMElement md_format : formats ) {
                    formatClass = new Format();
                    String formatName = getNodeAsString( md_format, new XPath( "./gmd:name/gco:CharacterString",
                                                                               nsContext ), null );

                    String formatVersion = getNodeAsString( md_format, new XPath( "./gmd:version/gco:CharacterString",
                                                                                  nsContext ), null );

                    formatClass.setName( formatName );
                    formatClass.setVersion( formatVersion );

                    listOfFormats.add( formatClass );

                }

                qp.setFormat( listOfFormats );
                distributionInfo = elem;
                OMNamespace namespace = distributionInfo.getNamespace();
                distributionInfo.setNamespace( namespace );
                continue;
            }

        }
        qp.setCrs( crsList );

    }

    /**
     * This method parses the OMElement in Dublin Core.
     * 
     * @throws IOException
     */
    public void parseAPDC()
                            throws IOException {

        // /*/self::node() => /*/self::* => /* -> Transaction
        // */MD_Metadata -> null
        // //MD_Metadata => ./gmd:MD_Metadata -> null
        // */self::* => * => ./* => ./child::*-> fileIdentifier, identificationInfo
        // ./. => ./self::node() -> MD_Metadata
        // //. -> jedes element...alles also
        // /*/*/gmd:MD_Metadata -> MD_Metadata
        List<OMElement> recordElements = getElements( rootElement, new XPath( "*", nsContext ) );
        List<Keyword> keywordList = new ArrayList<Keyword>();
        List<String> title = new ArrayList<String>();
        List<String> _abstract = new ArrayList<String>();
        List<Format> formatList = new ArrayList<Format>();
        StringWriter anyText = new StringWriter();

        for ( OMElement elem : recordElements ) {
            anyText.append( elem.toString() );
            if ( elem.getLocalName().equals( "identifier" ) ) {
                qp.setIdentifier( getNodeAsString( elem, new XPath( ".", nsContext ), null ) );

                continue;

            }
            if ( elem.getLocalName().equals( "creator" ) ) {
                rp.setCreator( getNodeAsString( elem, new XPath( ".", nsContext ), null ) );
                continue;

            }
            Keyword keyword = new Keyword();

            if ( elem.getLocalName().equals( "subject" ) ) {
                List<String> keywordStringList = new ArrayList<String>();
                keywordStringList.add( getNodeAsString( elem, new XPath( ".", nsContext ), null ) );
                keyword.setKeywords( keywordStringList );
                keywordList.add( keyword );
                continue;

            }
            if ( elem.getLocalName().equals( "title" ) ) {
                String titleString = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                title.add( titleString );
                continue;

            }
            if ( elem.getLocalName().equals( "abstract" ) ) {
                String _abstractString = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                _abstract.add( _abstractString );
                continue;

            }
            Format format = new Format();
            if ( elem.getLocalName().equals( "format" ) ) {
                String formatString = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                format.setName( formatString );
                continue;

            }
            if ( elem.getLocalName().equals( "modified" ) ) {
                String modifiedString = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                Date modified = null;
                try {
                    modified = new Date( modifiedString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }
                qp.setModified( modified );
                continue;

            }
            if ( elem.getLocalName().equals( "type" ) ) {
                String type = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                qp.setType( type );
                continue;

            }
            if ( elem.getLocalName().equals( "BoundingBox" ) || elem.getLocalName().equals( "WGS84BoundingBox" ) ) {
                String bbox_lowerCorner = getNodeAsString( elem, new XPath( "./ows:LowerCorner", nsContext ), null );
                String bbox_upperCorner = getNodeAsString( elem, new XPath( "./ows:UpperCorner", nsContext ), null );

                String[] lowerCornerSplitting = bbox_lowerCorner.split( " " );
                String[] upperCornerSplitting = bbox_upperCorner.split( " " );

                double boundingBoxWestLongitude = Double.parseDouble( lowerCornerSplitting[0] );

                double boundingBoxEastLongitude = Double.parseDouble( lowerCornerSplitting[1] );

                double boundingBoxSouthLatitude = Double.parseDouble( upperCornerSplitting[0] );

                double boundingBoxNorthLatitude = Double.parseDouble( upperCornerSplitting[1] );

                qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                                    boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );

                continue;

            }

            if ( elem.getLocalName().equals( "publisher" ) ) {
                String publisher = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                rp.setPublisher( publisher );
                continue;

            }
            if ( elem.getLocalName().equals( "contributor" ) ) {
                String contributor = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                rp.setContributor( contributor );
                continue;

            }
            if ( elem.getLocalName().equals( "source" ) ) {
                String source = getNodeAsString( elem, new XPath( ".", nsContext ), null );
                rp.setSource( source );
                continue;

            }

        }
        qp.set_abstract( _abstract );
        qp.setTitle( title );
        qp.setKeywords( keywordList );
        qp.setAnyText( anyText.toString() );
        qp.setFormat( formatList );
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

            generateMainDatabaseDataset();
            if ( isDC == true ) {
                generateDC();
            } else {
                generateISO();
            }
            executeQueryableProperties( isUpdate );

            stm.close();
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
        boolean isUpdate = true;

        StringWriter sqlStatementUpdate = new StringWriter( 500 );
        StringBuffer buf = new StringBuffer();
        int requestedId = 0;
        String modifiedAttribute = "null";
        try {
            stm = connection.createStatement();
            sqlStatementUpdate.append( "SELECT " + databaseTable + ".id from " + databaseTable + " where "
                                       + databaseTable + ".identifier = '" + qp.getIdentifier() + "'" );
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
                // identifier
                if ( qp.getIdentifier() != null ) {

                    sqlStatementUpdate.write( "UPDATE " + databaseTable + " SET identifier = '" + qp.getIdentifier()
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
        // if ( qp.getTemporalExtentBegin() != null && qp.getTemporalExtentEnd() != null ) {
        // generateISOQP_TemporalExtentStatement( isUpdate );
        // }
        if ( qp.getOperatesOn() != null && qp.getOperatesOnIdentifier() != null && qp.getOperatesOnName() != null ) {
            generateISOQP_OperatesOnStatement( isUpdate );
        }
        if ( qp.getCouplingType() != null ) {
            generateISOQP_CouplingTypeStatement( isUpdate );
        }
        // TODO spatial
        if ( qp.getBoundingBox() != null ) {
            generateISOQP_BoundingBoxStatement( isUpdate );
        }
        if ( qp.getCrs() != null || qp.getCrs().size() != 0 ) {
            generateISOQP_CRSStatement( isUpdate );
        }

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
                            + " (id, version, status, anyText, identifier, modified, hassecurityconstraints, language, parentidentifier, source, association) VALUES ("
                            + this.id + ",null,null,'" + qp.getAnyText() + "','" + qp.getIdentifier() + "',"
                            + modifiedAttribute + "," + qp.isHasSecurityConstraints() + ",'" + rp.getLanguage() + "','"
                            + qp.getParentIdentifier() + "','', null);";
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
                    setDCBriefElements( omElement );
                    isoOMElement = setISOBriefElements().toString();
                } else if ( omElement.getLocalName().equals( SUMMARYRECORD ) ) {
                    setDCSummaryElements( omElement );
                    isoOMElement = setISOSummaryElements().toString();
                } else {
                    setDCFullElements( omElement );
                    isoOMElement = elementFull.toString();
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

        String sqlStatement = "";
        String isoElements = "";
        int fk_datasets = this.id;
        int idDatabaseTable = 0;
        for ( String databaseTable : tableRecordType.keySet() ) {

            if ( databaseTable.equals( RECORDBRIEF ) ) {
                isoElements = setISOBriefElements().toString();
            } else if ( databaseTable.equals( RECORDSUMMARY ) ) {
                isoElements = setISOSummaryElements().toString();
            } else {
                isoElements = elementFull.toString();
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
                    setDCBriefElements( omElement );
                } else if ( omElement.getLocalName().equals( SUMMARYRECORD ) ) {
                    setDCSummaryElements( omElement );
                } else {
                    setDCFullElements( omElement );
                }

                setBoundingBoxElement( omElement );

                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, format, data) VALUES ("
                               + idDatabaseTable + "," + fk_datasets + ", 1, '" + omElement.toString() + "');";

                recordInsertIDs.add( idDatabaseTable );

                stm.executeUpdate( sqlStatement );

            } catch ( SQLException e ) {

                e.printStackTrace();
            }
        }

    }

    /**
     * Generates the organisationname for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OrganisationNameStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_organisationname";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, organisationname) VALUES (" + id
                               + "," + mainDatabaseTableID + ",'" + qp.getResourceIdentifier() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET organisationname = '" + qp.getResourceIdentifier()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the temporalExtent for this dataset. TODO be aware about the datehandling
     * 
     * @param isUpdate
     */
    private void generateISOQP_TemporalExtentStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_temporalextent";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable
                               + " (id, fk_datasets, tempextent_begin, tempextent_end) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + qp.getTemporalExtentBegin() + "','"
                               + qp.getTemporalExtentEnd() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET tempextent_begin = '" + qp.getTemporalExtentBegin()
                               + "', tempextent_end = '" + qp.getTemporalExtentEnd() + "' WHERE fk_datasets = "
                               + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the spatialResolution for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_SpatialResolutionStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_spatialresolution";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable
                               + " (id, fk_datasets, denominator, distancevalue, distanceuom) VALUES (" + id + ","
                               + mainDatabaseTableID + "," + qp.getDenominator() + "," + qp.getDistanceValue() + ",'"
                               + qp.getDistanceUOM() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET denominator = " + qp.getDenominator()
                               + ", distancevalue = " + qp.getDistanceValue() + ", distanceuom = '"
                               + qp.getDistanceUOM() + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the couplingType for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_CouplingTypeStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_couplingtype";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, couplingtype) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + qp.getCouplingType() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET couplingtype = '" + qp.getCouplingType()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the operatesOn for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperatesOnStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_operateson";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable
                               + " (id, fk_datasets, operateson, operatesonidentifier, operatesonname) VALUES (" + id
                               + "," + mainDatabaseTableID + ",'" + qp.getOperatesOn() + "','"
                               + qp.getOperatesOnIdentifier() + "','" + qp.getOperatesOnName() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET operateson = '" + qp.getOperatesOn()
                               + "', operatesonidentifier = '" + qp.getOperatesOnIdentifier() + "', operatesonname = '"
                               + qp.getOperatesOnName() + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the operation for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_OperationStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_operation";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, operation) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + qp.getOperation() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET organisationname = '" + qp.getOperation()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the geographicDescriptionCode for the type "service" for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_GeographicDescriptionCode_ServiceStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_geographicdescriptioncode";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable
                               + " (id, fk_datasets, geographicdescriptioncode) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + qp.getGeographicDescriptionCode_service() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET organisationname = '"
                               + qp.getGeographicDescriptionCode_service() + "' WHERE fk_datasets = "
                               + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the serviceTypeVersion for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeVersionStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_servicetypeversion";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, servicetypeversion) VALUES (" + id
                               + "," + mainDatabaseTableID + ",'" + qp.getServiceTypeVersion() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET organisationname = '" + qp.getServiceTypeVersion()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the serviceType for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ServiceTypeStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_servicetype";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, servicetype) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + qp.getServiceType() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET organisationname = '" + qp.getServiceType()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the resourceLanguage for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceLanguageStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_resourcelanguage";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, resourcelanguage) VALUES (" + id
                               + "," + mainDatabaseTableID + ",'" + qp.getResourceLanguage() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET resourcelanguage = '" + qp.getResourceLanguage()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the revisiondate for this dataset.
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

                if ( isUpdate == false ) {
                    id = getLastDatasetId( connection, databaseTable );
                    id++;
                    sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, revisiondate) VALUES (" + id
                                   + "," + mainDatabaseTableID + "," + revisionDateAttribute + ");";
                } else {
                    sqlStatement = "UPDATE " + databaseTable + " SET revisiondate = " + revisionDateAttribute
                                   + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                }
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
     * Generates the creationdate for this dataset.
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

                if ( isUpdate == false ) {
                    id = getLastDatasetId( connection, databaseTable );
                    id++;
                    sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, creationdate) VALUES (" + id
                                   + "," + mainDatabaseTableID + "," + creationDateAttribute + ");";
                } else {
                    sqlStatement = "UPDATE " + databaseTable + " SET creationdate = " + creationDateAttribute
                                   + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                }

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
     * Generates the publicationdate for this dataset.
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

                if ( isUpdate == false ) {
                    id = getLastDatasetId( connection, databaseTable );
                    id++;
                    sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, publicationdate) VALUES (" + id
                                   + "," + mainDatabaseTableID + "," + publicationDateAttribute + ");";
                } else {
                    sqlStatement = "UPDATE " + databaseTable + " SET publicationdate = " + publicationDateAttribute
                                   + " WHERE fk_datasets = " + mainDatabaseTableID + ";";
                }

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
     * Generates the resourceidentifier for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_ResourceIdentifierStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_resourceidentifier";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, resourceidentifier) VALUES (" + id
                               + "," + mainDatabaseTableID + ",'" + qp.getResourceIdentifier() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET resourceidentifier = '" + qp.getResourceIdentifier()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the alternate title for this dataset.
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
     * Generates the title for this dataset.
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
     * Generates the type for this dataset.
     * 
     * @param isUpdate
     */
    private void generateISOQP_TypeStatement( boolean isUpdate ) {
        final String databaseTable = "isoqp_type";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, type) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + qp.getType() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET type = '" + qp.getType() + "' WHERE fk_datasets = "
                               + mainDatabaseTableID + ";";
            }
            System.out.println( sqlStatement );
            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Generates the keywords for this dataset.
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
     * Generates the topicCategory for this dataset.
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
     * Generates the format for this dataset.
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
     * Generates the abstract for this dataset.
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
     * Creation of the "brief"-representation in DC of a record.
     * 
     * @param factory
     * @param omElement
     */
    private void setDCBriefElements( OMElement omElement ) {

        OMElement omIdentifier = factory.createOMElement( "identifier", namespaceDC );
        OMElement omType = factory.createOMElement( "type", namespaceDC );

        omIdentifier.setText( qp.getIdentifier() );

        omElement.addChild( omIdentifier );

        for ( String title : qp.getTitle() ) {
            OMElement omTitle = factory.createOMElement( "title", namespaceDC );
            omTitle.setText( title );
            omElement.addChild( omTitle );
        }
        if ( qp.getType() != null ) {
            omType.setText( qp.getType() );
        } else {
            omType.setText( "" );
        }
        omElement.addChild( omType );
    }

    /**
     * Creation of the "summary"-representation in DC of a record.
     * 
     * @param omElement
     */
    private void setDCSummaryElements( OMElement omElement ) {
        setDCBriefElements( omElement );

        OMElement omSubject;
        // dc:subject
        for ( Keyword subjects : qp.getKeywords() ) {
            for ( String subject : subjects.getKeywords() ) {
                omSubject = factory.createOMElement( "subject", namespaceDC );
                omSubject.setText( subject );
                omElement.addChild( omSubject );
            }
        }
        if ( qp.getTopicCategory() != null ) {
            for ( String subject : qp.getTopicCategory() ) {
                omSubject = factory.createOMElement( "subject", namespaceDC );
                omSubject.setText( subject );
                omElement.addChild( omSubject );
            }
        }
        // dc:format
        if ( qp.getFormat() != null || qp.getFormat().size() != 0 ) {
            for ( Format format : qp.getFormat() ) {
                OMElement omFormat = factory.createOMElement( "format", namespaceDC );
                omFormat.setText( format.getName() );
                omElement.addChild( omFormat );
            }
        } else {
            OMElement omFormat = factory.createOMElement( "format", namespaceDC );
            omElement.addChild( omFormat );
        }

        // dc:relation
        // TODO

        // dct:modified
        // for ( Date date : qp.getModified() ) {
        // OMElement omModified = factory.createOMElement( "modified", namespaceDCT );
        // omModified.setText( date.toString() );
        // omElement.addChild( omModified );
        // }
        if ( qp.getModified() != null ) {
            OMElement omModified = factory.createOMElement( "modified", namespaceDCT );
            omModified.setText( qp.getModified().toString() );
            omElement.addChild( omModified );
        } else {
            OMElement omModified = factory.createOMElement( "modified", namespaceDCT );
            omElement.addChild( omModified );
        }
        // dct:abstract
        for ( String _abstract : qp.get_abstract() ) {
            OMElement omAbstract = factory.createOMElement( "abstract", namespaceDCT );
            omAbstract.setText( _abstract.toString() );
            omElement.addChild( omAbstract );
        }

        // dct:spatial
        // TODO

    }

    /**
     * Creation of the "full"-representation in DC of a record.
     * 
     * @param omElement
     */
    private void setDCFullElements( OMElement omElement ) {

        setDCSummaryElements( omElement );

        if ( rp.getCreator() != null ) {
            OMElement omCreator = factory.createOMElement( "creator", namespaceDC );
            omCreator.setText( rp.getCreator() );
            omElement.addChild( omCreator );
        }

        if ( rp.getPublisher() != null ) {
            OMElement omPublisher = factory.createOMElement( "publisher", namespaceDC );
            omPublisher.setText( rp.getPublisher() );
            omElement.addChild( omPublisher );
        }
        if ( rp.getContributor() != null ) {
            OMElement omContributor = factory.createOMElement( "contributor", namespaceDC );
            omContributor.setText( rp.getContributor() );
            omElement.addChild( omContributor );
        }
        if ( rp.getSource() != null ) {
            OMElement omSource = factory.createOMElement( "source", namespaceDC );
            omSource.setText( rp.getSource() );
            omElement.addChild( omSource );
        }
        if ( rp.getLanguage() != null ) {
            OMElement omLanguage = factory.createOMElement( "language", namespaceDC );
            omLanguage.setText( rp.getLanguage() );
            omElement.addChild( omLanguage );
        }

        // dc:rights
        if ( rp.getRights() != null ) {
            for ( String rights : rp.getRights() ) {
                OMElement omRights = factory.createOMElement( "rights", namespaceDC );
                omRights.setText( rights );
                omElement.addChild( omRights );
            }
        }

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
        OMAttribute omCrs = factory.createOMAttribute( "crs", namespaceOWS, "EPSG:4326" );

        omUpperCorner.setText( qp.getBoundingBox().getEastBoundLongitude() + " "
                               + qp.getBoundingBox().getSouthBoundLatitude() );
        omLowerCorner.setText( qp.getBoundingBox().getWestBoundLongitude() + " "
                               + qp.getBoundingBox().getNorthBoundLatitude() );
        omBoundingBox.addChild( omLowerCorner );
        omBoundingBox.addChild( omUpperCorner );
        omBoundingBox.addAttribute( omCrs );

        omElement.addChild( omBoundingBox );

    }

    /**
     * Adds the elements for the brief representation in ISO.
     * 
     * @return OMElement
     */
    private OMElement setISOBriefElements() {

        OMElement omElement;

        omElement = factory.createOMElement( "MD_Metadata", namespaceGMD );
        // identifier
        omElement.addChild( identifier );
        // type
        if ( hierarchyLevel != null ) {
            omElement.addChild( hierarchyLevel );
        }
        // BoundingBox, GraphicOverview, ServiceType, ServiceTypeVersion
        if ( identificationInfo != null ) {
            omElement.addChild( identificationInfo );
        }
        return omElement;

    }

    /**
     * Adds the elements for the summary representation in ISO.
     * 
     * @return OMElement
     */
    private OMElement setISOSummaryElements() {

        OMElement omElement;

        omElement = factory.createOMElement( "MD_Metadata", namespaceGMD );
        // identifier
        omElement.addChild( identifier );
        // Language
        if ( language != null ) {
            omElement.addChild( language );
        }
        // MetadataCharacterSet
        if ( characterSet != null ) {
            omElement.addChild( characterSet );
        }
        // ParentIdentifier
        if ( parentIdentifier != null ) {
            omElement.addChild( parentIdentifier );
        }
        // type
        if ( hierarchyLevel != null ) {
            omElement.addChild( hierarchyLevel );
        }
        // HierarchieLevelName
        if ( hierarchyLevelName != null ) {
            omElement.addChild( hierarchyLevelName );
        }
        // Modified
        if ( dateStamp != null ) {
            omElement.addChild( dateStamp );
        }
        // MetadataStandardName
        if ( metadataStandardName != null ) {
            omElement.addChild( metadataStandardName );
        }
        // MetadataStandardVersion
        if ( metadataStandardVersion != null ) {
            omElement.addChild( metadataStandardVersion );
        }
        // ReferenceInfoSystem
        if ( referenceSystemInfo != null || referenceSystemInfo.size() != 0 ) {
            for ( OMElement refSysInfoElem : referenceSystemInfo ) {
                omElement.addChild( refSysInfoElem );
            }
        }
        // BoundingBox, GraphicOverview, ServiceType, ServiceTypeVersion, Abstract, Creator, Contributor, CouplingType,
        // Publisher, ResourceIdentifier, ResourceLanguage, RevisionDate,
        // Rights, ServiceOperation, SpatialResolution, SpatialRepresentationType, TopicCategory
        if ( identificationInfo != null ) {
            omElement.addChild( identificationInfo );
        }
        // Format, FormatVersion, OnlineResource
        if ( distributionInfo != null ) {
            omElement.addChild( distributionInfo );
        }
        // Lineage
        if ( dataQualityInfo != null ) {
            omElement.addChild( dataQualityInfo );
        }

        return omElement;

    }

}
