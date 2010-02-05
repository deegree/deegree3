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
import java.sql.Connection;
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
import org.deegree.commons.types.datetime.Date;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.crs.CRS;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ParseIdentificationInfo extends XMLAdapter{
    
    private OMFactory factory;
    
    private Connection connection;
    
    private OMNamespace namespaceGMD = factory.createOMNamespace( "http://www.isotc211.org/2005/gmd", "" );

    private OMNamespace namespaceGCO = factory.createOMNamespace( "http://www.isotc211.org/2005/gco", "gco" );

    
    
    ParseIdentificationInfo(OMFactory factory, Connection connection){
        this.factory = factory;
        this.connection = connection;
    }
    
    
    
    protected void parseIdentificationInfo(List<OMElement> identificationInfo, GenerateRecord gr, QueryableProperties qp, ReturnableProperties rp, boolean isInspire, List<CRS> crsList ) throws IOException{
        
        
        
        
        List<OMElement> identificationInfo_Update = new ArrayList<OMElement>();

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

            /*---------------------------------------------------------------
             * 
             * Citation
             * 
             *---------------------------------------------------------------*/
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
                    // maybe additional this?? : | ./gmd:RS_Identifier/gmd:code/gco:CharacterString
                    String resourceIdentifier = getNodeAsString(
                                                                 resourceElement,
                                                                 new XPath(
                                                                            "./gmd:MD_Identifier/gmd:code/gco:CharacterString",
                                                                            nsContext ), null );
                    resourceIdentifierList.add( resourceIdentifier );
                }
                qp.setResourceIdentifier( resourceIdentifierList );

            } else {
                for ( OMElement resourceElement : identifier ) {
                    // maybe additional this?? : | ./gmd:RS_Identifier/gmd:code/gco:CharacterString
                    String resourceIdentifier = getNodeAsString(
                                                                 resourceElement,
                                                                 new XPath(
                                                                            "./gmd:MD_Identifier/gmd:code/gco:CharacterString",
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
                    //String uuid_gen = generateUUID();
                    //resourceIdentifierList.add( uuid_gen );

                    OMElement omIdentifier = factory.createOMElement( "identifier", namespaceGMD );
                    OMElement omMD_Identifier = factory.createOMElement( "MD_Identifier", namespaceGMD );
                    OMElement omCode = factory.createOMElement( "code", namespaceGMD );
                    OMElement omCharacterString = factory.createOMElement( "CharacterString", namespaceGCO );

                    //omCharacterString.setText( uuid_gen );
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

            /*---------------------------------------------------------------
             * 
             * Abstract
             * 
             *---------------------------------------------------------------*/
            OMElement _abstract = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:abstract",
                                                                                              nsContext ) );

            String[] _abstractStrings = getNodesAsStrings( _abstract, new XPath( "./gco:CharacterString", nsContext ) );

            qp.set_abstract( Arrays.asList( _abstractStrings ) );

            /*---------------------------------------------------------------
             * 
             * Purpose
             * 
             *---------------------------------------------------------------*/
            OMElement purpose = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:purpose", nsContext ) );

            /*---------------------------------------------------------------
             *  
             * Credit
             * 
             *---------------------------------------------------------------*/
            List<OMElement> credit = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:credit",
                                                                                                  nsContext ) );

            /*---------------------------------------------------------------
             * 
             * Status
             * 
             *---------------------------------------------------------------*/
            List<OMElement> status = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:status",
                                                                                                  nsContext ) );

            /*---------------------------------------------------------------
             * 
             * PointOfContact
             * 
             *---------------------------------------------------------------*/
            List<OMElement> pointOfContact = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:pointOfContact", nsContext ) );

            /*---------------------------------------------------------------
             * 
             * ResourceMaintenance
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceMaintenance = getElements( sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:resourceMaintenance", nsContext ) );

            /*---------------------------------------------------------------
             * 
             * GraphicOverview
             * 
             *---------------------------------------------------------------*/
            List<OMElement> graphicOverview = getElements( sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:graphicOverview", nsContext ) );

            String graphicOverviewString = getNodeAsString(
                                                            sv_service_OR_md_dataIdentification,
                                                            new XPath(
                                                                       "./gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString",
                                                                       nsContext ), null );
            rp.setGraphicOverview( graphicOverviewString );

            /*---------------------------------------------------------------
             * 
             * ResourceFormat
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceFormat = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:resourceFormat", nsContext ) );

            /*---------------------------------------------------------------
             * 
             * DescriptiveKeywords
             * 
             *---------------------------------------------------------------*/
            List<OMElement> descriptiveKeywords = getElements( sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:descriptiveKeywords", nsContext ) );

            
            Keyword keywordClass;

            List<Keyword> listOfKeywords = new ArrayList<Keyword>();

            /*---------------------------------------------------------------
             * 
             * ResourceSpecificUsage
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceSpecificUsage = getElements( sv_service_OR_md_dataIdentification,
                                                                 new XPath( "./gmd:resourceSpecificUsage", nsContext ) );

            /*---------------------------------------------------------------
             * 
             * ResourceConstraints
             * 
             *---------------------------------------------------------------*/
            List<OMElement> resourceConstraints = getElements( sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:resourceConstraints", nsContext ) );

            /*---------------------------------------------------------------
             * 
             * AggregationInfo
             * 
             *---------------------------------------------------------------*/
            List<OMElement> aggregationInfo = getElements( sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:aggregationInfo", nsContext ) );

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
                spatialRepresentationType = getElements( md_dataIdentification,
                                                         new XPath( "./gmd:spatialRepresentationType", nsContext ) );
                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * SpatialResolution
                 * 
                 *---------------------------------------------------------------*/
                spatialResolution = getElements( md_dataIdentification,
                                                 new XPath( "./gmd:spatialResolution", nsContext ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * Language
                 * 
                 *---------------------------------------------------------------*/
                language_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:language", nsContext ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * CharacterSet
                 * 
                 *---------------------------------------------------------------*/
                characterSet_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:characterSet",
                                                                                           nsContext ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * TopicCategory
                 * 
                 *---------------------------------------------------------------*/
                topicCategory = getElements( md_dataIdentification, new XPath( "./gmd:topicCategory", nsContext ) );

                
                if ( md_dataIdentification != null ) {
                    topicCategories = getNodesAsStrings( md_dataIdentification,
                                                         new XPath( "./gmd:topicCategory/gmd:MD_TopicCategoryCode",
                                                                    nsContext ) );
                }
                qp.setTopicCategory( Arrays.asList( topicCategories ) );
                
                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * EnvironmentDescription
                 * 
                 *---------------------------------------------------------------*/
                environmentDescription = getElement( md_dataIdentification, new XPath( "./gmd:environmentDescription",
                                                                                       nsContext ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * Extent
                 * 
                 *---------------------------------------------------------------*/
                extent_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:extent", nsContext ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * SupplementalInformation
                 * 
                 *---------------------------------------------------------------*/
                supplementalInformation = getElement( md_dataIdentification,
                                                      new XPath( "./gmd:supplementalInformation", nsContext ) );

                List<String> languageList = new ArrayList<String>();
                for ( OMElement langElem : language_md_dataIdent ) {
                    String resourceLanguage = getNodeAsString( langElem,
                                                               new XPath( "./gmd:language/gco:CharacterString",
                                                                          nsContext ), null );
                    languageList.add( resourceLanguage );
                }

                qp.setResourceLanguage( languageList );

                for ( OMElement spatialResolutionElem : spatialResolution ) {
                    int denominator = getNodeAsInt(
                                                    spatialResolutionElem,
                                                    new XPath(
                                                               "./gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer",
                                                               nsContext ), -1 );
                    qp.setDenominator( denominator );

                    // TODO put here the constraint that there can a denominator be available iff distanceValue and
                    // distanceUOM are not set and vice versa!!
                    float distanceValue = getNodeAsFloat( spatialResolutionElem,
                                                          new XPath( "./gmd:distance/gco:Distance", nsContext ), -1 );
                    qp.setDistanceValue( distanceValue );

                    String distanceUOM = getNodeAsString( spatialResolutionElem,
                                                          new XPath( "./gmd:distance/gco:Distance/@uom", nsContext ),
                                                          null );
                    qp.setDistanceUOM( distanceUOM );

                }

            }

            List<String> relationList = new ArrayList<String>();
            for ( OMElement aggregatInfoElem : aggregationInfo ) {

                String relation = getNodeAsString( aggregatInfoElem, new XPath( "./gco:CharacterString", nsContext ),
                                                   null );
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
                                                      new XPath( "./srv:serviceType/gco:LocalName", nsContext ), null );
                qp.setServiceType( serviceType );

                serviceTypeElem = getElement( sv_serviceIdentification, new XPath( "./srv:serviceType", nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * ServiceTypeVersion
                 * 
                 *---------------------------------------------------------------*/
                String[] serviceTypeVersion = getNodesAsStrings(
                                                                 sv_serviceIdentification,
                                                                 new XPath(
                                                                            "./srv:serviceTypeVersion/gco:CharacterString",
                                                                            nsContext ) );
                qp.setServiceTypeVersion( Arrays.asList( serviceTypeVersion ) );

                serviceTypeVersionElem = getElements( sv_serviceIdentification, new XPath( "./srv:serviceTypeVersion",
                                                                                           nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * AccessProperties
                 * 
                 *---------------------------------------------------------------*/
                accessProperties = getElement( sv_serviceIdentification,
                                               new XPath( "./srv:accessProperties", nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Restrictions
                 * 
                 *---------------------------------------------------------------*/
                restrictions = getElement( sv_serviceIdentification, new XPath( "./srv:restrictions", nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Keywords
                 * 
                 *---------------------------------------------------------------*/
                keywords_service = getElements( sv_serviceIdentification, new XPath( "./srv:keywords", nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Extent
                 * 
                 *---------------------------------------------------------------*/
                extent_service = getElements( sv_serviceIdentification, new XPath( "./srv:extent", nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * CoupledResource
                 * 
                 *---------------------------------------------------------------*/
                coupledResource = getElements( sv_serviceIdentification, new XPath( "./srv:coupledResource", nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * CouplingType
                 * 
                 *---------------------------------------------------------------*/
                couplingType = getElement( sv_serviceIdentification, new XPath( "./srv:couplingType", nsContext ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * ContainsOperations
                 * 
                 *---------------------------------------------------------------*/
                containsOperations = getElements( sv_serviceIdentification, new XPath( "./srv:containsOperations",
                                                                                       nsContext ) );
                String[] operation = getNodesAsStrings(
                                                        sv_serviceIdentification,
                                                        new XPath(
                                                                   "./srv:containsOperations/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString",
                                                                   nsContext ) );
                for ( OMElement containsOpElem : containsOperations ) {

                    String operation_dcp = getNodeAsString(
                                                            containsOpElem,
                                                            new XPath(
                                                                       "./srv:SV_OperationMetadata/srv:DCP/srv:DCPList",
                                                                       nsContext ), null );

                    String operation_linkage = getNodeAsString(
                                                                containsOpElem,
                                                                new XPath(
                                                                           "./srv:SV_OperationMetadata/srv:connectPoint/srv:CI_OnlineResource/srv:linkage/srv:URL",
                                                                           nsContext ), null );

                }
                qp.setOperation( Arrays.asList( operation ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * OperatesOn
                 * 
                 *---------------------------------------------------------------*/
                operatesOn = getElements( sv_serviceIdentification, new XPath( "./srv:operatesOn", nsContext ) );
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

            for ( OMElement extentElem : extent ) {

                if ( temporalExtentBegin.equals( "0000-00-00" ) ) {
                    temporalExtentBegin = getNodeAsString(
                                                           extentElem,
                                                           new XPath(
                                                                      "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:beginPosition",
                                                                      nsContext ), "0000-00-00" );
                }

                if ( temporalExtentEnd.equals( "0000-00-00" ) ) {
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
                if ( boundingBoxWestLongitude == 0.0 ) {
                    boundingBoxWestLongitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:westBoundLongitude/gco:Decimal",
                                                                           nsContext ), 0.0 );
                }
                if ( boundingBoxEastLongitude == 0.0 ) {
                    boundingBoxEastLongitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:eastBoundLongitude/gco:Decimal",
                                                                           nsContext ), 0.0 );
                }
                if ( boundingBoxSouthLatitude == 0.0 ) {
                    boundingBoxSouthLatitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:southBoundLatitude/gco:Decimal",
                                                                           nsContext ), 0.0 );
                }
                if ( boundingBoxNorthLatitude == 0.0 ) {
                    boundingBoxNorthLatitude = getNodeAsDouble( bbox,
                                                                new XPath( "./gmd:northBoundLatitude/gco:Decimal",
                                                                           nsContext ), 0.0 );
                }

                if ( bbox != null ) {
                    crs = new CRS( "EPSG:4326" );
                    crsList.add( crs );

                }

                if ( geographicDescriptionCode_service == null ) {
                    geographicDescriptionCode_service = getNodeAsString(
                                                                         extentElem,
                                                                         new XPath(
                                                                                    "./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeopraphicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code",
                                                                                    nsContext ), null );
                }

            }

            qp.setTemporalExtentBegin( dateTempBeg );
            qp.setTemporalExtentEnd( dateTempEnd );
            qp.setBoundingBox( new BoundingBox( boundingBoxWestLongitude, boundingBoxEastLongitude,
                                                boundingBoxSouthLatitude, boundingBoxNorthLatitude ) );
            qp.setCrs( crsList );
            qp.setGeographicDescriptionCode_service( geographicDescriptionCode_service );

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
            

            /*---------------------------------------------------------------
             * SV_ServiceIdentification
             * Setting the COUPLINGTYPE
             * 
             *---------------------------------------------------------------*/
            List<String> operatesOnList = new ArrayList<String>();
            for ( OMElement operatesOnElem : operatesOn ) {
                // ./gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier(/gmd:RS_Identifier |
                // /gmd:MD_Identifier)/gmd:code/gco:CharacterString
                String operatesOnString = getNodeAsString(
                                                           operatesOnElem,
                                                           new XPath(
                                                                      "./gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString",
                                                                      nsContext ), null );
                operatesOnList.add( operatesOnString );

            }
            qp.setOperatesOn( operatesOnList );

            String[] operatesOnIdentifierList = getNodesAsStrings(
                                                                   sv_serviceIdentification,
                                                                   new XPath(
                                                                              "./srv:coupledResource/srv:SV_CoupledResource/srv:identifier/gco:CharacterString",
                                                                              nsContext ) );

            String[] operatesOnNameList = getNodesAsStrings(
                                                             sv_serviceIdentification,
                                                             new XPath(
                                                                        "./srv:coupledResource/srv:SV_CoupledResource/srv:operationName/gco:CharacterString",
                                                                        nsContext ) );
            if ( operatesOnIdentifierList != null ) {
                qp.setOperatesOnIdentifier( Arrays.asList( operatesOnIdentifierList ) );
            }
            if ( operatesOnNameList != null ) {
                qp.setOperatesOnName( Arrays.asList( operatesOnNameList ) );
            }
            String couplingTypeString = getNodeAsString(
                                                         sv_serviceIdentification,
                                                         new XPath(
                                                                    "./srv:couplingType/srv:SV_CouplingType/@codeListValue",
                                                                    nsContext ), null );
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
                    //TODO please more effiziency and intelligence
                    for ( String operatesOnString : operatesOnList ) {

                        for ( String operatesOnIdentifierString : operatesOnIdentifierList ) {

                            if ( operatesOnString.equals( operatesOnIdentifierString ) ) {
                                isTightlyCoupledOK = true;
                                break;
                            } else {
                                isTightlyCoupledOK = false;
                            }

                        }
                        // OperatesOnList [a,b,c] - OperatesOnIdList [b,c,d] -> a not in OperatesOnIdList ->
                        // inconsistency
                        if ( isTightlyCoupledOK == false ) {

                            String msg = "Missmatch between OperatesOn '" + operatesOnString
                                         + "' and its tightly coupled resource OperatesOnIdentifier. ";
                            throw new IOException( msg );
                            
                            //there is no possibility to set the operationName -> not able to set the coupledResource
                            
                            
                            
                        }

                    }
                    // OperatesOnList [] - OperatesOnIdList [a,b,c] -> inconsistency
                    if ( isTightlyCoupledOK == false && operatesOnIdentifierList.length != 0 ) {

                        String msg = "Missmatch between OperatesOn and its tightly coupled resource OperatesOnIdentifier. ";
                        throw new IOException( msg );
                    }
                }else{
                    //mixed coupled if there are loose and tight coupled resources. 
                    
                }
            }

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

        String s = "SELECT resourceidentifier FROM isoqp_resourceidentifier WHERE resourceidentifier = '"
                   + resourceIdentifier + "';";
        ResultSet rs;

        try {
            rs = connection.createStatement().executeQuery( s );
            while ( rs.next() ) {
                gotOneDataset = true;
            }
        } catch ( SQLException e ) {

            e.printStackTrace();
        }

        return gotOneDataset;
    }
    

}
