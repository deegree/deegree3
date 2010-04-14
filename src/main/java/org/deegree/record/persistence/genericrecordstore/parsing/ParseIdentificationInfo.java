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
package org.deegree.record.persistence.genericrecordstore.parsing;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.CRS;
import org.deegree.record.persistence.genericrecordstore.generating.GenerateRecord;
import org.deegree.record.persistence.neededdatastructures.BoundingBox;
import org.deegree.record.persistence.neededdatastructures.Keyword;
import org.deegree.record.persistence.neededdatastructures.OperatesOnData;
import org.slf4j.Logger;

/**
 * Parses the identification info element of an in ISO profile declared record. This is an outsourced method because of
 * the complexity.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ParseIdentificationInfo extends XMLAdapter {

    private static final Logger LOG = getLogger( ParseIdentificationInfo.class );

    private OMFactory factory;

    private OMNamespace namespaceGMD;

    private OMNamespace namespaceGCO;

    private Connection connection;

    private NamespaceContext nsContextParseII;

    /**
     * 
     * @param factory
     * @param connection
     * @param nsContext
     */
    protected ParseIdentificationInfo( OMFactory factory, Connection connection, NamespaceContext nsContext ) {
        this.factory = factory;
        this.connection = connection;
        this.nsContextParseII = nsContext;

        namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "gmd" );
        namespaceGCO = factory.createOMNamespace( "http://www.isotc211.org/2005/gco", "gco" );

    }

    /**
     * 
     * 
     * @param identificationInfo
     * @param gr
     * @param qp
     * @param rp
     * @param isInspire
     * @param crsList
     * @throws IOException
     */
    protected void parseIdentificationInfo( List<OMElement> identificationInfo, GenerateRecord gr,
                                            QueryableProperties qp, ReturnableProperties rp, boolean isInspire,
                                            List<CRS> crsList )
                            throws IOException {

        List<OMElement> identificationInfo_Update = new ArrayList<OMElement>();

        for ( OMElement root_identInfo : identificationInfo ) {

            OMElement root_identInfo_Update = factory.createOMElement( "identifier", namespaceGMD );

            OMElement md_dataIdentification = getElement( root_identInfo, new XPath( "./gmd:MD_DataIdentification",
                                                                                     nsContextParseII ) );

            OMElement sv_serviceIdentification = getElement( root_identInfo,
                                                             new XPath( "./srv:SV_ServiceIdentification",
                                                                        nsContextParseII ) );

            OMElement sv_service_OR_md_dataIdentification = getElement(
                                                                        root_identInfo,
                                                                        new XPath(
                                                                                   "./srv:SV_ServiceIdentification | ./gmd:MD_DataIdentification",
                                                                                   nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * Citation
             * 
             *---------------------------------------------------------------*/
            OMElement citation = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:citation",
                                                                                             nsContextParseII ) );

            OMElement ci_citation = getElement( citation, new XPath( "./gmd:CI_Citation", nsContextParseII ) );

            OMElement title = getElement( ci_citation, new XPath( "./gmd:title", nsContextParseII ) );

            String[] titleList = getNodesAsStrings(
                                                    title,
                                                    new XPath(
                                                               "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
                                                               nsContextParseII ) );

            List<OMElement> alternateTitle = getElements( ci_citation, new XPath( "./gmd:alternateTitle",
                                                                                  nsContextParseII ) );

            String[] alternateTitleOtherLang = null;
            for ( OMElement alternateTitleElement : alternateTitle ) {
                alternateTitleOtherLang = getNodesAsStrings(
                                                             alternateTitleElement,
                                                             new XPath(
                                                                        "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
                                                                        nsContextParseII ) );

            }
            List<OMElement> citation_date = getElements( ci_citation, new XPath( "./gmd:date", nsContextParseII ) );

            OMElement edition = getElement( ci_citation, new XPath( "./gmd:edition", nsContextParseII ) );
            OMElement editionDate = getElement( ci_citation, new XPath( "./gmd:editionDate", nsContextParseII ) );

            List<OMElement> identifier = getElements( ci_citation, new XPath( "./gmd:identifier", nsContextParseII ) );

            List<String> resourceIdentifierList = new ArrayList<String>();

            String[] titleElements = getNodesAsStrings( title, new XPath( "./gco:CharacterString", nsContextParseII ) );

            String[] alternateTitleElements = getNodesAsStrings(
                                                                 sv_service_OR_md_dataIdentification,
                                                                 new XPath(
                                                                            "./gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString",
                                                                            nsContextParseII ) );
            List<String> titleStringList = new ArrayList<String>();
            titleStringList.addAll( Arrays.asList( titleElements ) );
            if ( titleList != null ) {
                titleStringList.addAll( Arrays.asList( titleList ) );
            }
            qp.setTitle( titleStringList );

            List<String> alternateTitleList = new ArrayList<String>();
            alternateTitleList.addAll( Arrays.asList( alternateTitleElements ) );
            if ( alternateTitleOtherLang != null ) {
                alternateTitleList.addAll( Arrays.asList( alternateTitleOtherLang ) );
            }
            qp.setAlternateTitle( alternateTitleList );

            for ( OMElement dateElem : citation_date ) {

                String revisionDateString = getNodeAsString(
                                                             dateElem,
                                                             new XPath(
                                                                        "./gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date",
                                                                        nsContextParseII ), "0000-00-00" );
                Date date = null;
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
                                                                        nsContextParseII ), "0000-00-00" );

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
                                                                           nsContextParseII ), "0000-00-00" );

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
                    // maybe additional this?? : | ./gmd:RS_Identifier/gmd:code/gco:CharacterString
                    String resourceIdentifier = getNodeAsString(
                                                                 resourceElement,
                                                                 new XPath(
                                                                            "./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                                                            nsContextParseII ), null );
                    resourceIdentifierList.add( resourceIdentifier );
                }
                qp.setResourceIdentifier( resourceIdentifierList );

            } else {
                for ( OMElement resourceElement : identifier ) {
                    // maybe additional this?? : | ./gmd:RS_Identifier/gmd:code/gco:CharacterString
                    String resourceIdentifier = getNodeAsString(
                                                                 resourceElement,
                                                                 new XPath(
                                                                            "./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                                                            nsContextParseII ), null );
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
                    // String uuid_gen = generateUUID();
                    // resourceIdentifierList.add( uuid_gen );

                    OMElement omIdentifier = factory.createOMElement( "identifier", namespaceGMD );
                    OMElement omMD_Identifier = factory.createOMElement( "MD_Identifier", namespaceGMD );
                    OMElement omCode = factory.createOMElement( "code", namespaceGMD );
                    OMElement omCharacterString = factory.createOMElement( "CharacterString", namespaceGCO );

                    // omCharacterString.setText( uuid_gen );
                    omCode.addChild( omCharacterString );
                    omMD_Identifier.addChild( omCode );
                    omIdentifier.addChild( omMD_Identifier );
                    identifier.add( omIdentifier );

                }
                String dataIdentificationId = md_dataIdentification.getAttributeValue( new QName( "id" ) );
                String dataIdentificationUuId = md_dataIdentification.getAttributeValue( new QName( "uuid" ) );
                if ( firstResourceId.equals( dataIdentificationId ) ) {
                } else {
                    md_dataIdentification.getAttribute( new QName( "id" ) ).setAttributeValue( firstResourceId );

                }
                if ( firstResourceId.equals( dataIdentificationUuId ) ) {
                } else {
                    md_dataIdentification.getAttribute( new QName( "uuid" ) ).setAttributeValue( firstResourceId );

                }

                qp.setResourceIdentifier( resourceIdentifierList );

                List<OMElement> citedResponsibleParty = getElements( ci_citation,
                                                                     new XPath( "./gmd:citedResponsibleParty",
                                                                                nsContextParseII ) );
                List<OMElement> presentationForm = getElements( ci_citation, new XPath( "./gmd:presentationForm",
                                                                                        nsContextParseII ) );
                OMElement series = getElement( ci_citation, new XPath( "./gmd:series", nsContextParseII ) );
                OMElement otherCitationDetails = getElement( ci_citation, new XPath( "./gmd:otherCitationDetails",
                                                                                     nsContextParseII ) );
                OMElement collectiveTitle = getElement( ci_citation, new XPath( "./gmd:collectiveTitle",
                                                                                nsContextParseII ) );
                OMElement ISBN = getElement( ci_citation, new XPath( "./gmd:ISBN", nsContextParseII ) );
                OMElement ISSN = getElement( ci_citation, new XPath( "./gmd:ISSN", nsContextParseII ) );

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

            /*---------------------------------------------------------------
             * 
             * Abstract
             * 
             *---------------------------------------------------------------*/
            OMElement _abstract = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:abstract",
                                                                                              nsContextParseII ) );

            String[] _abstractOtherLang = getNodesAsStrings(
                                                             _abstract,
                                                             new XPath(
                                                                        "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
                                                                        nsContextParseII ) );

            String[] _abstractStrings = getNodesAsStrings( _abstract, new XPath( "./gco:CharacterString",
                                                                                 nsContextParseII ) );
            List<String> _abstractList = new ArrayList<String>();
            _abstractList.addAll( Arrays.asList( _abstractStrings ) );
            if ( _abstractOtherLang != null ) {
                _abstractList.addAll( Arrays.asList( _abstractOtherLang ) );
            }
            qp.set_abstract( _abstractList );

            /*---------------------------------------------------------------
             * 
             * Purpose
             * 
             *---------------------------------------------------------------*/
            OMElement purpose = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:purpose",
                                                                                            nsContextParseII ) );

            /*---------------------------------------------------------------
             *  
             * Credit
             * 
             *---------------------------------------------------------------*/
            List<OMElement> credit = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:credit",
                                                                                                  nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * Status
             * 
             *---------------------------------------------------------------*/
            List<OMElement> status = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:status",
                                                                                                  nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * PointOfContact
             * 
             *---------------------------------------------------------------*/
            List<OMElement> pointOfContact = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:pointOfContact", nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * ResourceMaintenance
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceMaintenance = getElements(
                                                               sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:resourceMaintenance", nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * GraphicOverview
             * 
             *---------------------------------------------------------------*/
            List<OMElement> graphicOverview = getElements( sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:graphicOverview", nsContextParseII ) );

            String graphicOverviewString = getNodeAsString(
                                                            sv_service_OR_md_dataIdentification,
                                                            new XPath(
                                                                       "./gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString",
                                                                       nsContextParseII ), null );
            rp.setGraphicOverview( graphicOverviewString );

            /*---------------------------------------------------------------
             * 
             * ResourceFormat
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceFormat = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:resourceFormat", nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * DescriptiveKeywords
             * 
             *---------------------------------------------------------------*/
            List<OMElement> descriptiveKeywords = getElements(
                                                               sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:descriptiveKeywords", nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * ResourceSpecificUsage
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceSpecificUsage = getElements( sv_service_OR_md_dataIdentification,
                                                                 new XPath( "./gmd:resourceSpecificUsage",
                                                                            nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * ResourceConstraints
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceConstraints = getElements(
                                                               sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:resourceConstraints", nsContextParseII ) );

            qp.setLimitation( Arrays.asList( getNodesAsStrings(
                                                                sv_service_OR_md_dataIdentification,
                                                                new XPath(
                                                                           "./gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString",
                                                                           nsContextParseII ) ) ) );

            qp.setAccessConstraints( Arrays.asList( getNodesAsStrings(
                                                                       sv_service_OR_md_dataIdentification,
                                                                       new XPath(
                                                                                  "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue",
                                                                                  nsContextParseII ) ) ) );

            qp.setOtherConstraints( Arrays.asList( getNodesAsStrings(
                                                                      sv_service_OR_md_dataIdentification,
                                                                      new XPath(
                                                                                 "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString",
                                                                                 nsContextParseII ) ) ) );

            qp.setClassification( Arrays.asList( getNodesAsStrings(
                                                                    sv_service_OR_md_dataIdentification,
                                                                    new XPath(
                                                                               "./gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue",
                                                                               nsContextParseII ) ) ) );

            /*---------------------------------------------------------------
             * 
             * AggregationInfo
             * 
             *---------------------------------------------------------------*/
            List<OMElement> aggregationInfo = getElements( sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:aggregationInfo", nsContextParseII ) );

            List<OMElement> extent_md_dataIdent = new ArrayList<OMElement>();
            List<OMElement> topicCategory = new ArrayList<OMElement>();
            List<OMElement> spatialRepresentationType = null;
            List<OMElement> spatialResolution = null;
            List<OMElement> language_md_dataIdent = null;
            List<OMElement> characterSet_md_dataIdent = null;
            OMElement environmentDescription = null;
            OMElement supplementalInformation = null;
            String[] topicCategories = null;
            if ( md_dataIdentification != null ) {
                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * SpatialRepresentationType
                 * 
                 *---------------------------------------------------------------*/
                spatialRepresentationType = getElements(
                                                         md_dataIdentification,
                                                         new XPath( "./gmd:spatialRepresentationType", nsContextParseII ) );
                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * SpatialResolution
                 * 
                 *---------------------------------------------------------------*/
                spatialResolution = getElements( md_dataIdentification, new XPath( "./gmd:spatialResolution",
                                                                                   nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * Language
                 * 
                 *---------------------------------------------------------------*/
                language_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:language",
                                                                                       nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * CharacterSet
                 * 
                 *---------------------------------------------------------------*/
                characterSet_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:characterSet",
                                                                                           nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * TopicCategory
                 * 
                 *---------------------------------------------------------------*/
                topicCategory = getElements( md_dataIdentification, new XPath( "./gmd:topicCategory", nsContextParseII ) );

                if ( md_dataIdentification != null ) {
                    topicCategories = getNodesAsStrings( md_dataIdentification,
                                                         new XPath( "./gmd:topicCategory/gmd:MD_TopicCategoryCode",
                                                                    nsContextParseII ) );
                }
                qp.setTopicCategory( Arrays.asList( topicCategories ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * EnvironmentDescription
                 * 
                 *---------------------------------------------------------------*/
                environmentDescription = getElement( md_dataIdentification, new XPath( "./gmd:environmentDescription",
                                                                                       nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * Extent
                 * 
                 *---------------------------------------------------------------*/
                extent_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:extent", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * SupplementalInformation
                 * 
                 *---------------------------------------------------------------*/
                supplementalInformation = getElement( md_dataIdentification,
                                                      new XPath( "./gmd:supplementalInformation", nsContextParseII ) );

                List<String> languageList = new ArrayList<String>();
                for ( OMElement langElem : language_md_dataIdent ) {
                    String resourceLanguage = getNodeAsString( langElem,
                                                               new XPath( "./gmd:language/gco:CharacterString",
                                                                          nsContextParseII ), null );
                    languageList.add( resourceLanguage );
                }

                qp.setResourceLanguage( languageList );

                for ( OMElement spatialResolutionElem : spatialResolution ) {
                    int denominator = getNodeAsInt(
                                                    spatialResolutionElem,
                                                    new XPath(
                                                               "./gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer",
                                                               nsContextParseII ), -1 );
                    qp.setDenominator( denominator );

                    // TODO put here the constraint that there can a denominator be available iff distanceValue and
                    // distanceUOM are not set and vice versa!!
                    float distanceValue = getNodeAsFloat( spatialResolutionElem,
                                                          new XPath( "./gmd:distance/gco:Distance", nsContextParseII ),
                                                          -1 );
                    qp.setDistanceValue( distanceValue );

                    String distanceUOM = getNodeAsString( spatialResolutionElem,
                                                          new XPath( "./gmd:distance/gco:Distance/@uom",
                                                                     nsContextParseII ), null );
                    qp.setDistanceUOM( distanceUOM );

                }

            }

            List<String> relationList = new ArrayList<String>();
            for ( OMElement aggregatInfoElem : aggregationInfo ) {

                String relation = getNodeAsString( aggregatInfoElem, new XPath( "./gco:CharacterString",
                                                                                nsContextParseII ), null );
                relationList.add( relation );

            }
            rp.setRelation( relationList );

            // for ( OMElement resourceSpecificUsageElem : resourceSpecificUsage ) {
            //
            // OMElement usage = getElement( resourceSpecificUsageElem, new XPath( "./gmd:MD_Usage", nsContext ) );
            //
            // }

            for ( OMElement pointOfContactElem : pointOfContact ) {
                OMElement ci_responsibleParty = getElement( pointOfContactElem, new XPath( "./gmd:CI_ResponsibleParty",
                                                                                           nsContextParseII ) );

                String creator = getNodeAsString(
                                                  ci_responsibleParty,
                                                  new XPath(
                                                             "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gco:CharacterString",
                                                             nsContextParseII ), null );

                rp.setCreator( creator );

                String publisher = getNodeAsString(
                                                    ci_responsibleParty,
                                                    new XPath(
                                                               "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']/gco:CharacterString",
                                                               nsContextParseII ), null );

                rp.setPublisher( publisher );

                String contributor = getNodeAsString(
                                                      ci_responsibleParty,
                                                      new XPath(
                                                                 "./gmd:organisationName[../gmd:role/gmd:CI_RoleCode/@codeListValue='author']/gco:CharacterString",
                                                                 nsContextParseII ), null );
                rp.setContributor( contributor );

                String organisationName = getNodeAsString( ci_responsibleParty,
                                                           new XPath( "./gmd:organisationName/gco:CharacterString",
                                                                      nsContextParseII ), null );

                qp.setOrganisationName( organisationName );
            }

            OMElement serviceTypeElem = null;
            List<OMElement> serviceTypeVersionElem = null;
            OMElement accessProperties = null;
            OMElement restrictions = null;
            List<OMElement> keywords_service = null;
            List<OMElement> extent_service = null;
            List<OMElement> coupledResource = null;
            OMElement couplingType = null;
            List<OMElement> containsOperations = null;
            List<OMElement> operatesOn = new ArrayList<OMElement>();
            if ( sv_serviceIdentification != null ) {
                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * ServiceType
                 * 
                 *---------------------------------------------------------------*/
                String serviceType = getNodeAsString( sv_serviceIdentification,
                                                      new XPath( "./srv:serviceType/gco:LocalName", nsContextParseII ),
                                                      null );
                qp.setServiceType( serviceType );

                serviceTypeElem = getElement( sv_serviceIdentification, new XPath( "./srv:serviceType",
                                                                                   nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * ServiceTypeVersion
                 * 
                 *---------------------------------------------------------------*/
                String[] serviceTypeVersion = getNodesAsStrings(
                                                                 sv_serviceIdentification,
                                                                 new XPath(
                                                                            "./srv:serviceTypeVersion/gco:CharacterString",
                                                                            nsContextParseII ) );
                qp.setServiceTypeVersion( Arrays.asList( serviceTypeVersion ) );

                serviceTypeVersionElem = getElements( sv_serviceIdentification, new XPath( "./srv:serviceTypeVersion",
                                                                                           nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * AccessProperties
                 * 
                 *---------------------------------------------------------------*/
                accessProperties = getElement( sv_serviceIdentification, new XPath( "./srv:accessProperties",
                                                                                    nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Restrictions
                 * 
                 *---------------------------------------------------------------*/
                restrictions = getElement( sv_serviceIdentification, new XPath( "./srv:restrictions", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Keywords
                 * 
                 *---------------------------------------------------------------*/
                keywords_service = getElements( sv_serviceIdentification,
                                                new XPath( "./srv:keywords", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Extent
                 * 
                 *---------------------------------------------------------------*/
                extent_service = getElements( sv_serviceIdentification, new XPath( "./srv:extent", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * CoupledResource
                 * 
                 *---------------------------------------------------------------*/
                coupledResource = getElements( sv_serviceIdentification, new XPath( "./srv:coupledResource",
                                                                                    nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * CouplingType
                 * 
                 *---------------------------------------------------------------*/
                couplingType = getElement( sv_serviceIdentification, new XPath( "./srv:couplingType", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * ContainsOperations
                 * 
                 *---------------------------------------------------------------*/
                containsOperations = getElements( sv_serviceIdentification, new XPath( "./srv:containsOperations",
                                                                                       nsContextParseII ) );
                String[] operation = getNodesAsStrings(
                                                        sv_serviceIdentification,
                                                        new XPath(
                                                                   "./srv:containsOperations/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString",
                                                                   nsContextParseII ) );
                for ( OMElement containsOpElem : containsOperations ) {

                    String operation_dcp = getNodeAsString(
                                                            containsOpElem,
                                                            new XPath(
                                                                       "./srv:SV_OperationMetadata/srv:DCP/srv:DCPList",
                                                                       nsContextParseII ), null );

                    String operation_linkage = getNodeAsString(
                                                                containsOpElem,
                                                                new XPath(
                                                                           "./srv:SV_OperationMetadata/srv:connectPoint/srv:CI_OnlineResource/srv:linkage/srv:URL",
                                                                           nsContextParseII ), null );

                }
                qp.setOperation( Arrays.asList( operation ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * OperatesOn
                 * 
                 *---------------------------------------------------------------*/
                operatesOn = getElements( sv_serviceIdentification, new XPath( "./srv:operatesOn", nsContextParseII ) );
            }
            /*---------------------------------------------------------------
             * SV_ServiceIdentification or MD_DataIdentification
             * Setting the EXTENT for one of the metadatatypes (service or data)
             * 
             *---------------------------------------------------------------*/
            List<OMElement> extent = (List<OMElement>) ( extent_md_dataIdent.size() != 0 ? extent_md_dataIdent
                                                                                        : extent_service );
            String temporalExtentBegin = "0000-00-00";
            Date dateTempBeg = null;
            try {
                dateTempBeg = new Date( temporalExtentBegin );
            } catch ( ParseException e ) {

                e.printStackTrace();
            }

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

            CRS crs = null;

            String geographicDescriptionCode_service = null;
            String[] geographicDescriptionCode_serviceOtherLang = null;

            for ( OMElement extentElem : extent ) {

                if ( temporalExtentBegin.equals( "0000-00-00" ) ) {
                    temporalExtentBegin = getNodeAsString(
                                                           extentElem,
                                                           new XPath(
                                                                      "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:beginPosition",
                                                                      nsContextParseII ), "0000-00-00" );
                }

                if ( temporalExtentEnd.equals( "0000-00-00" ) ) {
                    temporalExtentEnd = getNodeAsString(
                                                         extentElem,
                                                         new XPath(
                                                                    "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:endPosition",
                                                                    nsContextParseII ), "0000-00-00" );
                }

                OMElement bbox = getElement(
                                             extentElem,
                                             new XPath(
                                                        "./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox",
                                                        nsContextParseII ) );
                if ( boundingBoxWestLongitude == 0.0 ) {
                    boundingBoxWestLongitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:westBoundLongitude/gco:Decimal",
                                                                           nsContextParseII ), 0.0 );

                }
                if ( boundingBoxEastLongitude == 0.0 ) {
                    boundingBoxEastLongitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:eastBoundLongitude/gco:Decimal",
                                                                           nsContextParseII ), 0.0 );

                }
                if ( boundingBoxSouthLatitude == 0.0 ) {
                    boundingBoxSouthLatitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:southBoundLatitude/gco:Decimal",
                                                                           nsContextParseII ), 0.0 );

                }
                if ( boundingBoxNorthLatitude == 0.0 ) {
                    boundingBoxNorthLatitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:northBoundLatitude/gco:Decimal",
                                                                           nsContextParseII ), 0.0 );

                }

                if ( bbox != null ) {
                    crs = new CRS( "EPSG:4326" );
                    crsList.add( crs );

                }

                if ( geographicDescriptionCode_service == null ) {
                    OMElement geographicDescriptionCode_serviceElem = getElement(
                                                                                  extentElem,
                                                                                  new XPath(
                                                                                             "./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeopraphicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code",
                                                                                             nsContextParseII ) );
                    geographicDescriptionCode_service = getNodeAsString( geographicDescriptionCode_serviceElem,
                                                                         new XPath( "./gco:CharacterString",
                                                                                    nsContextParseII ), null );
                    geographicDescriptionCode_serviceOtherLang = getNodesAsStrings(
                                                                                    geographicDescriptionCode_serviceElem,
                                                                                    new XPath(
                                                                                               "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
                                                                                               nsContextParseII ) );
                }

            }

            qp.setTemporalExtentBegin( dateTempBeg );
            qp.setTemporalExtentEnd( dateTempEnd );
            qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxSouthLatitude,
                                                boundingBoxEastLongitude, boundingBoxNorthLatitude ) );
            qp.setCrs( crsList );

            List<String> geographicDescCode_serviceList = new ArrayList<String>();
            geographicDescCode_serviceList.addAll( Arrays.asList( geographicDescriptionCode_service ) );
            if ( geographicDescriptionCode_serviceOtherLang != null ) {
                geographicDescCode_serviceList.addAll( Arrays.asList( geographicDescriptionCode_serviceOtherLang ) );
            }
            qp.setGeographicDescriptionCode_service( geographicDescCode_serviceList );

            /*---------------------------------------------------------------
             * SV_ServiceIdentification and IdentificationInfo
             * Setting all the KEYWORDS found in the record 
             * 
             *---------------------------------------------------------------*/
            List<OMElement> commonKeywords = new ArrayList<OMElement>();

            commonKeywords.addAll( descriptiveKeywords );
            if ( sv_serviceIdentification != null ) {
                commonKeywords.addAll( keywords_service );
            }

            List<Keyword> listOfKeywords = new ArrayList<Keyword>();
            for ( OMElement md_keywords : commonKeywords ) {

                String keywordType = getNodeAsString(
                                                      md_keywords,
                                                      new XPath(
                                                                 "./gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue",
                                                                 nsContextParseII ), null );

                String[] keywords = getNodesAsStrings( md_keywords,
                                                       new XPath( "./gmd:MD_Keywords/gmd:keyword/gco:CharacterString",
                                                                  nsContextParseII ) );

                String[] keywordsOtherLang = getNodesAsStrings(
                                                                md_keywords,
                                                                new XPath(
                                                                           "./gmd:MD_Keywords/gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
                                                                           nsContextParseII ) );

                String thesaurus = getNodeAsString(
                                                    md_keywords,
                                                    new XPath(
                                                               "./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString",
                                                               nsContextParseII ), null );

                List<String> keywordList = new ArrayList<String>();
                keywordList.addAll( Arrays.asList( keywords ) );
                if ( keywordsOtherLang != null ) {
                    keywordList.addAll( Arrays.asList( keywordsOtherLang ) );
                }
                listOfKeywords.add( new Keyword( keywordType, keywordList, thesaurus ) );

            }

            qp.setKeywords( listOfKeywords );

            /*---------------------------------------------------------------
             * SV_ServiceIdentification
             * Setting the COUPLINGTYPE
             * 
             *---------------------------------------------------------------*/
            List<String> operatesOnList = new ArrayList<String>();
            List<OperatesOnData> operatesOnDataList = new ArrayList<OperatesOnData>();
            for ( OMElement operatesOnElem : operatesOn ) {
                // ./gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier(/gmd:RS_Identifier |
                // /gmd:MD_Identifier)/gmd:code/gco:CharacterString
                String operatesOnStringUuIdAttribute = operatesOnElem.getAttributeValue( new QName( "uuidref" ) );
                String operatesOnString = "";
                if ( !operatesOnStringUuIdAttribute.equals( "" ) ) {
                    operatesOnList.add( operatesOnStringUuIdAttribute );
                } else {
                    operatesOnString = getNodeAsString(
                                                        operatesOnElem,
                                                        new XPath(
                                                                   "./gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString",
                                                                   nsContextParseII ), null );
                    operatesOnList.add( operatesOnString );
                }

            }

            List<OMElement> operatesOnCoupledResources = getElements(
                                                                      sv_serviceIdentification,
                                                                      new XPath(
                                                                                 "./srv:coupledResource/srv:SV_CoupledResource",
                                                                                 nsContextParseII ) );
            String[] operatesOnIdentifierList = getNodesAsStrings(
                                                                   sv_serviceIdentification,
                                                                   new XPath(
                                                                              "./srv:coupledResource/srv:SV_CoupledResource/srv:identifier/gco:CharacterString",
                                                                              nsContextParseII ) );

            for ( OMElement operatesOnCoupledResource : operatesOnCoupledResources ) {
                String operatesOnIdentifierString = getNodeAsString( operatesOnCoupledResource,
                                                                     new XPath( "./srv:identifier/gco:CharacterString",
                                                                                nsContextParseII ), "" );

                String operatesOnNameString = getNodeAsString( operatesOnCoupledResource,
                                                               new XPath( "./srv:operationName/gco:CharacterString",
                                                                          nsContextParseII ), "" );

                OperatesOnData ood = null;

                for ( String operatesOnId : operatesOnList ) {

                    if ( operatesOnId.equals( operatesOnIdentifierString ) ) {

                        ood = new OperatesOnData( operatesOnId, operatesOnIdentifierString, operatesOnNameString );
                        break;
                    }

                }

                operatesOnDataList.add( ood );

            }
            qp.setOperatesOnData( operatesOnDataList );

            String couplingTypeString = getNodeAsString(
                                                         sv_serviceIdentification,
                                                         new XPath(
                                                                    "./srv:couplingType/srv:SV_CouplingType/@codeListValue",
                                                                    nsContextParseII ), null );
            if ( couplingTypeString != null ) {
                qp.setCouplingType( couplingTypeString );
            }
            /*---------------------------------------------------------------
             * SV_ServiceIdentification
             * Check for consistency in the coupling.
             * 
             *---------------------------------------------------------------*/
            if ( sv_serviceIdentification != null ) {

                if ( couplingTypeString.equals( "loose" ) ) {
                    // TODO

                } else if ( couplingTypeString.equals( "tight" ) ) {
                    for ( String operatesOnString : operatesOnList ) {
                        if ( !getCoupledDataMetadatasets( operatesOnString ) ) {
                            String msg = "No resourceIdentifier " + operatesOnString
                                         + " found in the data metadata. So there is no coupling possible.";
                            throw new IOException( msg );
                        }
                    }

                    boolean isTightlyCoupledOK = false;
                    // TODO please more efficiency and intelligence
                    for ( String operatesOnString : operatesOnList ) {

                        for ( String operatesOnIdentifierString : operatesOnIdentifierList ) {

                            if ( operatesOnString.equals( operatesOnIdentifierString ) ) {
                                isTightlyCoupledOK = true;
                                break;
                            }
                            isTightlyCoupledOK = false;

                        }
                        // OperatesOnList [a,b,c] - OperatesOnIdList [b,c,d] -> a not in OperatesOnIdList ->
                        // inconsistency
                        if ( isTightlyCoupledOK == false ) {

                            String msg = "Missmatch between OperatesOn '" + operatesOnString
                                         + "' and its tightly coupled resource OperatesOnIdentifier. ";
                            throw new IOException( msg );

                            // there is no possibility to set the operationName -> not able to set the coupledResource

                        }

                    }
                    // OperatesOnList [] - OperatesOnIdList [a,b,c] -> inconsistency
                    if ( isTightlyCoupledOK == false && operatesOnIdentifierList.length != 0 ) {

                        String msg = "Missmatch between OperatesOn and its tightly coupled resource OperatesOnIdentifier. ";
                        throw new IOException( msg );
                    }
                } else {
                    // mixed coupled if there are loose and tight coupled resources.

                }
            }

            OMElement hasSecurityConstraintsElement = null;
            String[] rightsElements = null;
            for ( OMElement resourceConstraintsElem : resourceConstraints ) {
                rightsElements = getNodesAsStrings(
                                                    resourceConstraintsElem,
                                                    new XPath(
                                                               "./gmd:MD_LegalConstraints/gmd:accessConstraints/@codeListValue",
                                                               nsContextParseII ) );

                hasSecurityConstraintsElement = getElement(
                                                            resourceConstraintsElem,
                                                            new XPath( "./gmd:MD_SecurityConstraints", nsContextParseII ) );

            }
            if ( rightsElements != null ) {
                rp.setRights( Arrays.asList( rightsElements ) );
            }

            boolean hasSecurityConstraint = false;
            if ( hasSecurityConstraintsElement != null ) {
                hasSecurityConstraint = true;
            }
            qp.setHasSecurityConstraints( hasSecurityConstraint );

            if ( isInspire == true ) {
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
            } else {
                identificationInfo_Update.addAll( identificationInfo );
            }
        }

        gr.setIdentificationInfo( identificationInfo_Update );
    }

    /**
     * If there is a data metadata record available for the service metadata record.
     * 
     * @param resourceIdentifierList
     * @return
     */
    private boolean getCoupledDataMetadatasets( String resourceIdentifier ) {
        boolean gotOneDataset = false;
        ResultSet rs = null;
        PreparedStatement stm = null;

        String s = "SELECT resourceidentifier FROM isoqp_resourceidentifier WHERE resourceidentifier = ?;";

        try {
            stm = connection.prepareStatement( s );
            stm.setObject( 1, resourceIdentifier );
            rs = stm.executeQuery();
            while ( rs.next() ) {
                gotOneDataset = true;
            }
            stm.close();
            rs.close();
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

        return gotOneDataset;
    }

}
