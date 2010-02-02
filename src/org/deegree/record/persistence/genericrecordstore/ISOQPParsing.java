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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.ValidationException;
import javax.xml.namespace.QName;
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

    private OMElement elementFull;

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

        this.elementFull = element.cloneOMElement();
        this.connection = connection;

        setRootElement( element );
        nsContext.addNamespace( rootElement.getDefaultNamespace().getPrefix(),
                                rootElement.getDefaultNamespace().getNamespaceURI() );
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

        for ( String error : validate( rootElement ) ) {
            throw new IOException( "VALIDATION-ERROR: " + error );
        }

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
           
            
            //TODO hack
            List<String> idList = new ArrayList<String>();
            idList.add( fileIdentifierString );
            qp.setIdentifierDC( idList );
            
            
            gr.setIdentifier( getElement( rootElement, new XPath( "./gmd:fileIdentifier", nsContext ) ) );

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
        qp.setType( getNodeAsString( rootElement, new XPath( "./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
                                                             nsContext ), "dataset" ) );
        gr.setHierarchyLevel( getElements( rootElement, new XPath( "./gmd:hierarchyLevel", nsContext ) ) );

        gr.setHierarchyLevelName( getElements( rootElement, new XPath( "./gmd:hierarchyLevelName", nsContext ) ) );

        gr.setContact( getElements( rootElement, new XPath( "./gmd:contact", nsContext ) ) );

        String dateString = getNodeAsString( rootElement, new XPath( "./gmd:dateStamp/gco:DateTime", nsContext ),
                                             "0000-00-00" );
        Date date = null;
        try {
            date = new Date( dateString );
        } catch ( ParseException e ) {

            e.printStackTrace();
        }

        qp.setModified( date );
        gr.setDateStamp( getElement( rootElement, new XPath( "./gmd:dateStamp", nsContext ) ) );

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

        rp.setLanguage( getNodeAsString( rootElement, new XPath( "./gmd:language/gco:CharacterString", nsContext ),
                                         null ) );

        gr.setLanguage( getElement( rootElement, new XPath( "./gmd:language", nsContext ) ) );

        gr.setDataQualityInfo( getElements( rootElement, new XPath( "./gmd:dataQualityInfo", nsContext ) ) );

        gr.setCharacterSet( getElement( rootElement, new XPath( "./gmd:characterSet", nsContext ) ) );

        gr.setMetadataStandardName( getElement( rootElement, new XPath( "./gmd:metadataStandardName", nsContext ) ) );

        gr.setMetadataStandardVersion( getElement( rootElement, new XPath( "./gmd:metadataStandardVersion", nsContext ) ) );

        qp.setParentIdentifier( getNodeAsString( rootElement, new XPath( "./gmd:parentIdentifier/gco:CharacterString",
                                                                         nsContext ), null ) );

        gr.setParentIdentifier( getElement( rootElement, new XPath( "./gmd:parentIdentifier", nsContext ) ) );

        gr.setDataSetURI( getElement( rootElement, new XPath( "./gmd:dataSetURI", nsContext ) ) );

        gr.setLocale( getElements( rootElement, new XPath( "./gmd:locale", nsContext ) ) );

        gr.setSpatialRepresentationInfo( getElements( rootElement, new XPath( "./gmd:spatialRepresentationInfo",
                                                                              nsContext ) ) );

        gr.setMetadataExtensionInfo( getElements( rootElement, new XPath( "./gmd:metadataExtensionInfo", nsContext ) ) );

        gr.setContentInfo( getElements( rootElement, new XPath( "./gmd:contentInfo", nsContext ) ) );

        gr.setPortrayalCatalogueInfo( getElements( rootElement, new XPath( "./gmd:portrayalCatalogueInfo", nsContext ) ) );

        gr.setMetadataConstraints( getElements( rootElement, new XPath( "./gmd:metadataConstraints", nsContext ) ) );

        gr.setApplicationSchemaInfo( getElements( rootElement, new XPath( "./gmd:applicationSchemaInfo", nsContext ) ) );

        gr.setMetadataMaintenance( getElement( rootElement, new XPath( "./gmd:metadataMaintenance", nsContext ) ) );

        gr.setSeries( getElements( rootElement, new XPath( "./gmd:series", nsContext ) ) );

        gr.setDescribes( getElements( rootElement, new XPath( "./gmd:describes", nsContext ) ) );

        gr.setPropertyType( getElements( rootElement, new XPath( "./gmd:propertyType", nsContext ) ) );

        gr.setFeatureType( getElements( rootElement, new XPath( "./gmd:featureType", nsContext ) ) );

        gr.setFeatureAttribute( getElements( rootElement, new XPath( "./gmd:featureAttribute", nsContext ) ) );

        List<OMElement> identificationInfo = getElements( rootElement,
                                                          new XPath( "./gmd:identificationInfo", nsContext ) );

        List<OMElement> identificationInfo_Update = new ArrayList<OMElement>();

        /*
         * 
         * IdentificationInfo
         */
        for ( OMElement root_identInfo : identificationInfo ) {

            OMElement root_identInfo_Update = factory.createOMElement( "identifier", namespaceGMD );

            OMElement md_dataIdentification = getElement( root_identInfo, new XPath( "./gmd:MD_DataIdentification",
                                                                                     nsContext ) );

            OMElement sv_serviceIdentification = getElement( root_identInfo,
                                                             new XPath( "./srv:SV_ServiceIdentification", nsContext ) );

            OMElement sv_service_OR_md_dataIdentification = getElement(
                                                                        root_identInfo,
                                                                        new XPath(
                                                                                   "./srv:SV_ServiceIdentification | ./gmd:MD_DataIdentification",
                                                                                   nsContext ) );

            OMElement citation = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:citation",
                                                                                             nsContext ) );

            OMElement ci_citation = getElement( citation, new XPath( "./gmd:CI_Citation", nsContext ) );

            OMElement title = getElement( ci_citation, new XPath( "./gmd:title", nsContext ) );

            List<OMElement> alternateTitle = getElements( ci_citation, new XPath( "./gmd:alternateTitle", nsContext ) );

            List<OMElement> citation_date = getElements( ci_citation, new XPath( "./gmd:date", nsContext ) );

            OMElement edition = getElement( ci_citation, new XPath( "./gmd:edition", nsContext ) );
            OMElement editionDate = getElement( ci_citation, new XPath( "./gmd:editionDate", nsContext ) );

            List<OMElement> identifier = getElements( ci_citation, new XPath( "./gmd:identifier", nsContext ) );

            List<String> resourceIdentifierList = new ArrayList<String>();

            String[] titleElements = getNodesAsStrings( title, new XPath( "./gco:CharacterString", nsContext ) );

            String[] alternateTitleElements = getNodesAsStrings(
                                                                 sv_service_OR_md_dataIdentification,
                                                                 new XPath(
                                                                            "./gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString",
                                                                            nsContext ) );

            qp.setTitle( Arrays.asList( titleElements ) );

            qp.setAlternateTitle( Arrays.asList( alternateTitleElements ) );

            for ( OMElement dateElem : citation_date ) {

                String revisionDateString = getNodeAsString(
                                                             dateElem,
                                                             new XPath(
                                                                        "./gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date",
                                                                        nsContext ), "0000-00-00" );
                date = null;
                try {
                    date = new Date( revisionDateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setRevisionDate( date );

                String creationDateString = getNodeAsString(
                                                             dateElem,
                                                             new XPath(
                                                                        "./gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date",
                                                                        nsContext ), "0000-00-00" );

                try {
                    date = new Date( creationDateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setCreationDate( date );

                String publicationDateString = getNodeAsString(
                                                                dateElem,
                                                                new XPath(
                                                                           "./gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date",
                                                                           nsContext ), "0000-00-00" );

                try {
                    date = new Date( publicationDateString );
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setPublicationDate( date );
            }

            OMElement omCitation;
            if ( isInspire == false ) {

                omCitation = citation;

                for ( OMElement resourceElement : identifier ) {
                    String resourceIdentifier = getNodeAsString(
                                                                 resourceElement,
                                                                 new XPath(
                                                                            "./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                                                            nsContext ), null );
                    resourceIdentifierList.add( resourceIdentifier );
                }
                qp.setResourceIdentifier( resourceIdentifierList );

            } else {
                for ( OMElement resourceElement : identifier ) {
                    String resourceIdentifier = getNodeAsString(
                                                                 resourceElement,
                                                                 new XPath(
                                                                            "./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                                                            nsContext ), null );
                    resourceIdentifierList.add( resourceIdentifier );
                }
                String firstResourceId = "";
                // gets the first identifier in the list
                // if size == 0 then generate a new UUID
                if ( resourceIdentifierList.size() != 0 ) {
                    for ( int i = 0; i < 1; i++ ) {
                        firstResourceId = resourceIdentifierList.get( i );
                    }
                } else {
                    String uuid_gen = generateUUID();
                    resourceIdentifierList.add( uuid_gen );

                    OMElement omIdentifier = factory.createOMElement( "identifier", namespaceGMD );
                    OMElement omMD_Identifier = factory.createOMElement( "MD_Identifier", namespaceGMD );
                    OMElement omCode = factory.createOMElement( "code", namespaceGMD );
                    OMElement omCharacterString = factory.createOMElement( "CharacterString", namespaceGCO );

                    omCharacterString.setText( uuid_gen );
                    omCode.addChild( omCharacterString );
                    omMD_Identifier.addChild( omCode );
                    omIdentifier.addChild( omMD_Identifier );
                    identifier.add( omIdentifier );

                }
                String dataIdentificationId = md_dataIdentification.getAttributeValue( new QName( "id" ) );
                if ( firstResourceId.equals( dataIdentificationId ) ) {
                } else {
                    md_dataIdentification.getAttribute( new QName( "id" ) ).setAttributeValue( firstResourceId );

                }

                qp.setResourceIdentifier( resourceIdentifierList );

                List<OMElement> citedResponsibleParty = getElements( ci_citation,
                                                                     new XPath( "./gmd:citedResponsibleParty",
                                                                                nsContext ) );
                List<OMElement> presentationForm = getElements( ci_citation, new XPath( "./gmd:presentationForm",
                                                                                        nsContext ) );
                OMElement series = getElement( ci_citation, new XPath( "./gmd:series", nsContext ) );
                OMElement otherCitationDetails = getElement( ci_citation, new XPath( "./gmd:otherCitationDetails",
                                                                                     nsContext ) );
                OMElement collectiveTitle = getElement( ci_citation, new XPath( "./gmd:collectiveTitle", nsContext ) );
                OMElement ISBN = getElement( ci_citation, new XPath( "./gmd:ISBN", nsContext ) );
                OMElement ISSN = getElement( ci_citation, new XPath( "./gmd:ISSN", nsContext ) );

                omCitation = factory.createOMElement( "citation", namespaceGMD );
                OMElement omCI_Citation = factory.createOMElement( "CI_Citation", namespaceGCO );

                omCI_Citation.addChild( title );
                for ( OMElement elem : alternateTitle ) {
                    omCI_Citation.addChild( elem );
                }
                for ( OMElement elem : citation_date ) {
                    omCI_Citation.addChild( elem );
                }
                if ( edition != null ) {
                    omCI_Citation.addChild( edition );
                }
                if ( editionDate != null ) {
                    omCI_Citation.addChild( editionDate );
                }
                for ( OMElement elem : identifier ) {
                    omCI_Citation.addChild( elem );
                }
                for ( OMElement elem : citedResponsibleParty ) {
                    omCI_Citation.addChild( elem );
                }
                for ( OMElement elem : presentationForm ) {
                    omCI_Citation.addChild( elem );
                }
                if ( series != null ) {
                    omCI_Citation.addChild( series );
                }
                if ( otherCitationDetails != null ) {
                    omCI_Citation.addChild( otherCitationDetails );
                }
                if ( collectiveTitle != null ) {
                    omCI_Citation.addChild( collectiveTitle );
                }
                if ( ISBN != null ) {
                    omCI_Citation.addChild( ISBN );
                }
                if ( ISSN != null ) {
                    omCI_Citation.addChild( ISSN );
                }

                omCitation.addChild( omCI_Citation );
            }

            OMElement _abstract = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:abstract",
                                                                                              nsContext ) );

            String[] _abstractStrings = getNodesAsStrings( _abstract, new XPath( "./gco:CharacterString", nsContext ) );

            qp.set_abstract( Arrays.asList( _abstractStrings ) );

            OMElement purpose = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:purpose", nsContext ) );

            List<OMElement> credit = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:credit",
                                                                                                  nsContext ) );

            List<OMElement> status = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:status",
                                                                                                  nsContext ) );

            List<OMElement> pointOfContact = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:pointOfContact", nsContext ) );

            List<OMElement> resourceMaintenance = getElements( sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:resourceMaintenance", nsContext ) );

            List<OMElement> graphicOverview = getElements( sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:graphicOverview", nsContext ) );

            String graphicOverviewString = getNodeAsString(
                                                            sv_service_OR_md_dataIdentification,
                                                            new XPath(
                                                                       "./gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString",
                                                                       nsContext ), null );
            rp.setGraphicOverview( graphicOverviewString );

            List<OMElement> resourceFormat = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:resourceFormat", nsContext ) );

            List<OMElement> descriptiveKeywords = getElements( sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:descriptiveKeywords", nsContext ) );

            String[] topicCategories = getNodesAsStrings( md_dataIdentification,
                                                          new XPath( "./gmd:topicCategory/gmd:MD_TopicCategoryCode",
                                                                     nsContext ) );

            Keyword keywordClass;

            List<Keyword> listOfKeywords = new ArrayList<Keyword>();

            List<OMElement> resourceSpecificUsage = getElements( sv_service_OR_md_dataIdentification,
                                                                 new XPath( "./gmd:resourceSpecificUsage", nsContext ) );

            List<OMElement> resourceConstraints = getElements( sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:resourceConstraints", nsContext ) );

            List<OMElement> aggregationInfo = getElements( sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:aggregationInfo", nsContext ) );

            List<OMElement> spatialRepresentationType = getElements( md_dataIdentification,
                                                                     new XPath( "./gmd:spatialRepresentationType",
                                                                                nsContext ) );

            List<OMElement> spatialResolution = getElements( md_dataIdentification, new XPath( "./gmd:MD_Resolution",
                                                                                               nsContext ) );

            List<OMElement> language_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:language",
                                                                                                   nsContext ) );

            List<OMElement> characterSet_md_dataIdent = getElements( md_dataIdentification,
                                                                     new XPath( "./gmd:spatialResolution", nsContext ) );

            List<OMElement> topicCategory = getElements( md_dataIdentification, new XPath( "./gmd:topicCategory",
                                                                                           nsContext ) );

            OMElement environmentDescription = getElement( md_dataIdentification,
                                                           new XPath( "./gmd:environmentDescription", nsContext ) );

            List<OMElement> extent_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:extent",
                                                                                                 nsContext ) );

            OMElement supplementalInformation = getElement( md_dataIdentification,
                                                            new XPath( "./gmd:supplementalInformation", nsContext ) );
            List<String> relationList = new ArrayList<String>();
            for ( OMElement aggregatInfoElem : aggregationInfo ) {

                String relation = getNodeAsString( aggregatInfoElem, new XPath( "./gco:CharacterString", nsContext ),
                                                   null );
                relationList.add( relation );

            }
            rp.setRelation( relationList );

            List<String> languageList = new ArrayList<String>();
            for ( OMElement langElem : language_md_dataIdent ) {
                String resourceLanguage = getNodeAsString( langElem, new XPath( "./gmd:language/gco:CharacterString",
                                                                                nsContext ), null );
                languageList.add( resourceLanguage );
            }

            qp.setResourceLanguage( languageList );

            // for ( OMElement resourceSpecificUsageElem : resourceSpecificUsage ) {
            //
            // OMElement usage = getElement( resourceSpecificUsageElem, new XPath( "./gmd:MD_Usage", nsContext ) );
            //
            // }

            for ( OMElement pointOfContactElem : pointOfContact ) {
                OMElement ci_responsibleParty = getElement( pointOfContactElem, new XPath( "./gmd:CI_ResponsibleParty",
                                                                                           nsContext ) );

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

                String organisationName = getNodeAsString( ci_responsibleParty,
                                                           new XPath( "./gmd:organisationName/gco:CharacterString",
                                                                      nsContext ), null );

                qp.setOrganisationName( organisationName );
            }

            for ( OMElement spatialResolutionElem : spatialResolution ) {
                int denominator = getNodeAsInt(
                                                spatialResolutionElem,
                                                new XPath(
                                                           "./gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer",
                                                           nsContext ), -1 );
                qp.setDenominator( denominator );

                // TODO put here the constraint that there can a denominator be available iff distanceValue and
                // distanceUOM are not set and vice versa!!
                float distanceValue = getNodeAsFloat( spatialResolutionElem, new XPath( "./gmd:distance/gco:Distance",
                                                                                        nsContext ), -1 );
                qp.setDistanceValue( distanceValue );

                String distanceUOM = getNodeAsString( spatialResolutionElem,
                                                      new XPath( "./gmd:distance/gco:Distance/@uom", nsContext ), null );
                qp.setDistanceUOM( distanceUOM );

            }

            String serviceType = getNodeAsString( sv_serviceIdentification,
                                                  new XPath( "./srv:serviceType/gco:LocalName", nsContext ), null );
            qp.setServiceType( serviceType );

            OMElement serviceTypeElem = getElement( sv_serviceIdentification,
                                                    new XPath( "./srv:serviceType", nsContext ) );

            String serviceTypeVersion = getNodeAsString( sv_serviceIdentification,
                                                         new XPath( "./srv:serviceTypeVersion/gco:CharacterString",
                                                                    nsContext ), null );
            qp.setServiceTypeVersion( serviceTypeVersion );

            List<OMElement> serviceTypeVersionElem = getElements( sv_serviceIdentification,
                                                                  new XPath( "./srv:serviceTypeVersion", nsContext ) );

            OMElement accessProperties = getElement( sv_serviceIdentification, new XPath( "./srv:accessProperties",
                                                                                          nsContext ) );

            OMElement restrictions = getElement( sv_serviceIdentification, new XPath( "./srv:restrictions", nsContext ) );

            List<OMElement> keywords_service = getElements( sv_serviceIdentification, new XPath( "./srv:keywords",
                                                                                                 nsContext ) );

            List<OMElement> extent_service = getElements( sv_serviceIdentification, new XPath( "./srv:extent",
                                                                                               nsContext ) );

            List<OMElement> coupledResource = getElements( sv_serviceIdentification,
                                                           new XPath( "./srv:coupledResource", nsContext ) );

            OMElement couplingType = getElement( sv_serviceIdentification, new XPath( "./srv:couplingType", nsContext ) );

            List<OMElement> containsOperations = getElements( sv_serviceIdentification,
                                                              new XPath( "./srv:containsOperations", nsContext ) );

            List<OMElement> operatesOn = getElements( sv_serviceIdentification, new XPath( "./srv:operatesOn",
                                                                                           nsContext ) );

            List<OMElement> extent = (List<OMElement>) ( extent_md_dataIdent.size() != 0 ? extent_md_dataIdent
                                                                                        : extent_service );
            String temporalExtentBegin = "0000-00-00";
            Date dateTempBeg = null;
            try {
                dateTempBeg = new Date( temporalExtentBegin );
            } catch ( ParseException e ) {

                e.printStackTrace();
            }

            qp.setTemporalExtentBegin( dateTempBeg );
            
            String temporalExtentEnd = "0000-00-00";
            
            Date dateTempEnd = null;
            try {
                dateTempEnd = new Date( temporalExtentEnd );
            } catch ( ParseException e ) {

                e.printStackTrace();
            }

            
            
            double boundingBoxWestLongitude = 0.0;
            double boundingBoxEastLongitude = 0.0;
            double boundingBoxSouthLatitude = 0.0;
            double boundingBoxNorthLatitude = 0.0;
            

            CRS crs = new CRS( "EPSG:4326" );
            crsList.add( crs );
            qp.setCrs( crsList );
            
            for ( OMElement extentElem : extent ) {

                if(temporalExtentBegin.equals( "0000-00-00" ) ){
                temporalExtentBegin = getNodeAsString(
                                                              extentElem,
                                                              new XPath(
                                                                         "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:beginPosition",
                                                                         nsContext ), "0000-00-00" );
                }
                
                if(temporalExtentEnd.equals( "0000-00-00" ) ){
                temporalExtentEnd = getNodeAsString(
                                                            extentElem,
                                                            new XPath(
                                                                       "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:endPosition",
                                                                       nsContext ), "0000-00-00" );
                }

                OMElement bbox = getElement(
                                             extentElem,
                                             new XPath(
                                                        "./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox",
                                                        nsContext ) );
                if(boundingBoxWestLongitude == 0.0){
                boundingBoxWestLongitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:westBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );
                }
                if(boundingBoxEastLongitude == 0.0){
                boundingBoxEastLongitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:eastBoundLongitude/gco:Decimal",
                                                                              nsContext ), 0.0 );
                }
                if(boundingBoxSouthLatitude == 0.0){
                boundingBoxSouthLatitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:southBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );
                }
                if(boundingBoxNorthLatitude == 0.0){
                boundingBoxNorthLatitude = getNodeAsDouble( bbox,
                                                                   new XPath( "./gmd:northBoundLatitude/gco:Decimal",
                                                                              nsContext ), 0.0 );
                }
                

            }
            
            qp.setTemporalExtentEnd( dateTempEnd );
            qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                                boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );

            List<OMElement> commonKeywords = new ArrayList<OMElement>();
            commonKeywords.addAll( descriptiveKeywords );
            commonKeywords.addAll( keywords_service );

            for ( OMElement md_keywords : commonKeywords ) {
                keywordClass = new Keyword();

                String keywordType = getNodeAsString(
                                                      md_keywords,
                                                      new XPath(
                                                                 "./gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue",
                                                                 nsContext ), null );

                String[] keywords = getNodesAsStrings( md_keywords,
                                                       new XPath( "./gmd:MD_Keywords/gmd:keyword/gco:CharacterString",
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

            for ( OMElement operatesOnElem : operatesOn ) {

                String operatesOnString = getNodeAsString(
                                                           operatesOnElem,
                                                           new XPath(
                                                                      "./srv:MD_DataIdentification/srv:citation/srv:CI_Citation/srv:identifier/gco:CharacterString",
                                                                      nsContext ), null );
                qp.setOperatesOn( operatesOnString );

                String operatesOnIdentifier = getNodeAsString(
                                                               operatesOnElem,
                                                               new XPath(
                                                                          "./srv:coupledResource/srv:SV_CoupledResource/srv:identifier/gco:CharacterString",
                                                                          nsContext ), null );
                qp.setOperatesOnIdentifier( operatesOnIdentifier );

                String operatesOnName = getNodeAsString(
                                                         operatesOnElem,
                                                         new XPath(
                                                                    "./srv:coupledResource/srv:SV_CoupledResource/srv:operationName/gco:CharacterString",
                                                                    nsContext ), null );
                qp.setOperatesOnName( operatesOnName );

            }

            for ( OMElement extent_serviceElem : extent_service ) {

                String geographicDescriptionCode_service = getNodeAsString(
                                                                            extent_serviceElem,
                                                                            new XPath(
                                                                                       "./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeopraphicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code",
                                                                                       nsContext ), null );
                qp.setGeographicDescriptionCode_service( geographicDescriptionCode_service );

            }

            for ( OMElement containsOpElem : containsOperations ) {

                String operation = getNodeAsString(
                                                    containsOpElem,
                                                    new XPath(
                                                               "./srv:SV_OperationMetadata/srv:operationName/gco:CharacterString",
                                                               nsContext ), null );

                String operation_dcp = getNodeAsString( containsOpElem,
                                                        new XPath( "./srv:SV_OperationMetadata/srv:DCP/srv:DCPList",
                                                                   nsContext ), null );

                String operation_linkage = getNodeAsString(
                                                            containsOpElem,
                                                            new XPath(
                                                                       "./srv:SV_OperationMetadata/srv:connectPoint/srv:CI_OnlineResource/srv:linkage/srv:URL",
                                                                       nsContext ), null );

                qp.setOperation( operation );

            }

            String couplingTypeString = getNodeAsString(
                                                         sv_service_OR_md_dataIdentification,
                                                         new XPath(
                                                                    "./srv:couplingType/srv:SV_CouplingType/srv:code/@codeListValue",
                                                                    nsContext ), null );
            qp.setCouplingType( couplingTypeString );

            OMElement hasSecurityConstraintsElement = null;
            String[] rightsElements = null;
            for ( OMElement resourceConstraintsElem : resourceConstraints ) {
                rightsElements = getNodesAsStrings(
                                                    resourceConstraintsElem,
                                                    new XPath(
                                                               "./gmd:MD_LegalConstraints/gmd:accessConstraints/@codeListValue",
                                                               nsContext ) );

                hasSecurityConstraintsElement = getElement( resourceConstraintsElem,
                                                            new XPath( "./gmd:MD_SecurityConstraints", nsContext ) );

            }
            if ( rightsElements != null ) {
                rp.setRights( Arrays.asList( rightsElements ) );
            }

            boolean hasSecurityConstraint = false;
            if ( hasSecurityConstraintsElement != null ) {
                hasSecurityConstraint = true;
            }
            qp.setHasSecurityConstraints( hasSecurityConstraint );
            if(isInspire == true){
            if ( omCitation != null ) {
                root_identInfo_Update.addChild( omCitation );
            }
            if ( _abstract != null ) {
                root_identInfo_Update.addChild( _abstract );
            }
            if ( purpose != null ) {
                root_identInfo_Update.addChild( purpose );
            }
            for ( OMElement elem : credit ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : status ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : pointOfContact ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : resourceMaintenance ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : graphicOverview ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : resourceFormat ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : descriptiveKeywords ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : resourceSpecificUsage ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : resourceConstraints ) {
                root_identInfo_Update.addChild( elem );
            }
            for ( OMElement elem : aggregationInfo ) {
                root_identInfo_Update.addChild( elem );
            }

            // if MD_DataIdentification or SV_ServiceIdentification
            if ( md_dataIdentification != null ) {
                for ( OMElement elem : spatialRepresentationType ) {
                    root_identInfo_Update.addChild( elem );
                }
                for ( OMElement elem : spatialResolution ) {
                    root_identInfo_Update.addChild( elem );
                }
                for ( OMElement elem : language_md_dataIdent ) {
                    root_identInfo_Update.addChild( elem );
                }
                for ( OMElement elem : characterSet_md_dataIdent ) {
                    root_identInfo_Update.addChild( elem );
                }
                for ( OMElement elem : topicCategory ) {
                    root_identInfo_Update.addChild( elem );
                }
                if ( environmentDescription != null ) {
                    root_identInfo_Update.addChild( environmentDescription );
                }
                for ( OMElement elem : extent ) {
                    root_identInfo_Update.addChild( elem );
                }
                if ( supplementalInformation != null ) {
                    root_identInfo_Update.addChild( supplementalInformation );
                }
            } else {
                root_identInfo_Update.addChild( serviceTypeElem );
                for ( OMElement elem : serviceTypeVersionElem ) {
                    root_identInfo_Update.addChild( elem );
                }
                root_identInfo_Update.addChild( accessProperties );
                root_identInfo_Update.addChild( restrictions );
                for ( OMElement elem : keywords_service ) {
                    root_identInfo_Update.addChild( elem );
                }
                for ( OMElement elem : extent ) {
                    root_identInfo_Update.addChild( elem );
                }
                for ( OMElement elem : coupledResource ) {
                    root_identInfo_Update.addChild( elem );
                }
                root_identInfo_Update.addChild( couplingType );
                for ( OMElement elem : containsOperations ) {
                    root_identInfo_Update.addChild( elem );
                }
                for ( OMElement elem : operatesOn ) {
                    root_identInfo_Update.addChild( elem );
                }

            }
            identificationInfo_Update.add( root_identInfo_Update );
            }
        }
        if ( isInspire == true ) {
            gr.setIdentificationInfo( identificationInfo_Update );
        } else {
            gr.setIdentificationInfo( identificationInfo );
        }
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

        anyText.append( rootElement.toString() );

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
            }
        }

    }

    /**
     * Generates the identifier for this dataset.
     * 
     * @param isUpdate
     */
    private void generateQP_IdentifierStatement( boolean isUpdate ) {
        final String databaseTable = "qp_identifier";
        String sqlStatement = "";
        int mainDatabaseTableID = this.id;
        int id = 0;
        try {

            if ( isUpdate == false ) {
                id = getLastDatasetId( connection, databaseTable );
                id++;
                sqlStatement = "INSERT INTO " + databaseTable + " (id, fk_datasets, identifier) VALUES (" + id + ","
                               + mainDatabaseTableID + ",'" + qp.getIdentifier() + "');";
            } else {
                sqlStatement = "UPDATE " + databaseTable + " SET identifier = '" + qp.getIdentifier()
                               + "' WHERE fk_datasets = " + mainDatabaseTableID + ";";
            }

            stm.executeUpdate( sqlStatement );

        } catch ( SQLException e ) {

            e.printStackTrace();
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
     * Generates the resourceIdentifier for this dataset.
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
