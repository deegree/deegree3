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
package org.deegree.metadata.persistence.iso.parsing;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.types.BoundingBox;
import org.deegree.metadata.persistence.types.Keyword;
import org.deegree.metadata.persistence.types.OperatesOnData;
import org.slf4j.Logger;

/**
 * Parses the identification info element of an in ISO profile declared record. This is an outsourced method because of
 * the complexity.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ParseIdentificationInfo extends XMLAdapter {

    private static final Logger LOG = getLogger( ParseIdentificationInfo.class );

    private NamespaceContext nsContextParseII;

    /**
     * 
     * @param factory
     * @param connection
     * @param nsContext
     */
    protected ParseIdentificationInfo( OMFactory factory, NamespaceContext nsContext ) {
        this.nsContextParseII = nsContext;

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
    protected void parseIdentificationInfo( List<OMElement> identificationInfo, QueryableProperties qp,
                                            ReturnableProperties rp )
                            throws MetadataStoreException {

        List<OMElement> identificationInfo_Update = new ArrayList<OMElement>();

        for ( OMElement root_identInfo : identificationInfo ) {

            // OMElement root_identInfo_Update = factory.createOMElement( "identificationInfo", namespaceGMD );

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
            LOG.debug( "Parsing of citation element..." );
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
                                                                        nsContextParseII ), null );
                Date date = null;
                try {
                    if ( revisionDateString != null ) {
                        date = new Date( revisionDateString );
                    } else {
                        date = null;
                    }
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setRevisionDate( date );

                String creationDateString = getNodeAsString(
                                                             dateElem,
                                                             new XPath(
                                                                        "./gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date",
                                                                        nsContextParseII ), null );

                try {
                    if ( creationDateString != null ) {
                        date = new Date( creationDateString );
                    } else {
                        date = null;
                    }
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setCreationDate( date );

                String publicationDateString = getNodeAsString(
                                                                dateElem,
                                                                new XPath(
                                                                           "./gmd:CI_Date[./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date",
                                                                           nsContextParseII ), null );

                try {
                    if ( publicationDateString != null ) {
                        date = new Date( publicationDateString );
                    } else {
                        date = null;
                    }
                } catch ( ParseException e ) {

                    e.printStackTrace();
                }

                qp.setPublicationDate( date );
            }

            /*---------------------------------------------------------------
             * 
             * RS_/MD_Identifier check
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "Checking resourceIdentifier..." );
            List<String> resourceIdentifierList = new ArrayList<String>();
            for ( OMElement resourceElement : identifier ) {
                // maybe additional this?? : | ./gmd:RS_Identifier/gmd:code/gco:CharacterString
                String resourceIdentifier = getNodeAsString(
                                                             resourceElement,
                                                             new XPath(
                                                                        "./gmd:MD_Identifier/gmd:code/gco:CharacterString | ./gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                                                        nsContextParseII ), null );
                LOG.debug( "resourceIdentifier: '" + resourceIdentifier + "' " );

                resourceIdentifierList.add( resourceIdentifier );

            }
            LOG.debug( "Creating a resourceIdentifierList... is not implemented yet!!" );

            qp.setResourceIdentifier( resourceIdentifierList );

            List<OMElement> citedResponsibleParty = getElements( ci_citation, new XPath( "./gmd:citedResponsibleParty",
                                                                                         nsContextParseII ) );
            List<OMElement> presentationForm = getElements( ci_citation, new XPath( "./gmd:presentationForm",
                                                                                    nsContextParseII ) );
            OMElement series = getElement( ci_citation, new XPath( "./gmd:series", nsContextParseII ) );
            OMElement otherCitationDetails = getElement( ci_citation, new XPath( "./gmd:otherCitationDetails",
                                                                                 nsContextParseII ) );
            OMElement collectiveTitle = getElement( ci_citation, new XPath( "./gmd:collectiveTitle", nsContextParseII ) );
            OMElement ISBN = getElement( ci_citation, new XPath( "./gmd:ISBN", nsContextParseII ) );
            OMElement ISSN = getElement( ci_citation, new XPath( "./gmd:ISSN", nsContextParseII ) );

            parseAbstract( qp, sv_service_OR_md_dataIdentification );

            /*---------------------------------------------------------------
             * 
             * Purpose
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "Purpose element..." );
            OMElement purpose = getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:purpose",
                                                                                            nsContextParseII ) );

            /*---------------------------------------------------------------
             *  
             * Credit
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "Credit element..." );
            List<OMElement> credit = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:credit",
                                                                                                  nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * Status
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "Status element..." );
            List<OMElement> status = getElements( sv_service_OR_md_dataIdentification, new XPath( "./gmd:status",
                                                                                                  nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * PointOfContact
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "PointOfContact element..." );
            List<OMElement> pointOfContact = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:pointOfContact", nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * ResourceMaintenance
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "ResourceMaintenance element..." );
            List<OMElement> resourceMaintenance = getElements(
                                                               sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:resourceMaintenance", nsContextParseII ) );

            parseGraphicOverview( rp, sv_service_OR_md_dataIdentification );

            /*---------------------------------------------------------------
             * 
             * ResourceFormat
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "ResourceFormat element..." );
            List<OMElement> resourceFormat = getElements( sv_service_OR_md_dataIdentification,
                                                          new XPath( "./gmd:resourceFormat", nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * DescriptiveKeywords
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "DescripticeKeywords element..." );
            List<OMElement> descriptiveKeywords = getElements(
                                                               sv_service_OR_md_dataIdentification,
                                                               new XPath( "./gmd:descriptiveKeywords", nsContextParseII ) );

            /*---------------------------------------------------------------
             * 
             * ResourceSpecificUsage
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "ResourceSpecificUsage element..." );
            List<OMElement> resourceSpecificUsage = getElements( sv_service_OR_md_dataIdentification,
                                                                 new XPath( "./gmd:resourceSpecificUsage",
                                                                            nsContextParseII ) );

            parseResourceConstraints( qp, rp, sv_service_OR_md_dataIdentification );
            /*---------------------------------------------------------------
             * 
             * AggregationInfo
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "AggregationInfo element..." );
            List<OMElement> aggregationInfo = getElements( sv_service_OR_md_dataIdentification,
                                                           new XPath( "./gmd:aggregationInfo", nsContextParseII ) );

            List<OMElement> extent_md_dataIdent = new ArrayList<OMElement>();
            List<String> topicCategory = new ArrayList<String>();
            List<OMElement> spatialRepresentationType = null;
            List<String> languageList = new ArrayList<String>();
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
                LOG.debug( "MD_DataIdentification/SpatialRepresentation element..." );
                spatialRepresentationType = getElements(
                                                         md_dataIdentification,
                                                         new XPath( "./gmd:spatialRepresentationType", nsContextParseII ) );
                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * SpatialResolution
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "MD_DataIdentification/SpatialResolution element..." );

                spatialResolution = getElements( md_dataIdentification, new XPath( "./gmd:spatialResolution",
                                                                                   nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * Language
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "MD_DataIdentification/Language element..." );

                language_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:language",
                                                                                       nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * CharacterSet
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "MD_DataIdentification/CharacterSet element..." );

                characterSet_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:characterSet",
                                                                                           nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * TopicCategory
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "MD_DataIdentification/TopicCategory element..." );

                // topicCategory = getElements( md_dataIdentification, new XPath( "./gmd:topicCategory",
                // nsContextParseII ) );

                if ( md_dataIdentification != null ) {
                    topicCategories = getNodesAsStrings( md_dataIdentification,
                                                         new XPath( "./gmd:topicCategory/gmd:MD_TopicCategoryCode",
                                                                    nsContextParseII ) );
                    for ( String t : topicCategories ) {
                        topicCategory.add( t );
                    }
                }

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * EnvironmentDescription
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "MD_DataIdentification/EnvironmentDescription element..." );

                environmentDescription = getElement( md_dataIdentification, new XPath( "./gmd:environmentDescription",
                                                                                       nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * Extent
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "MD_DataIdentification/Extent element..." );

                extent_md_dataIdent = getElements( md_dataIdentification, new XPath( "./gmd:extent", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * MD_DataIdentification
                 * SupplementalInformation
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "MD_DataIdentification/SupplementalInformation element..." );

                supplementalInformation = getElement( md_dataIdentification,
                                                      new XPath( "./gmd:supplementalInformation", nsContextParseII ) );

                for ( OMElement langElem : language_md_dataIdent ) {
                    String resourceLanguage = getNodeAsString( langElem,
                                                               new XPath( "./gmd:language/gco:CharacterString",
                                                                          nsContextParseII ), null );
                    languageList.add( resourceLanguage );
                }

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

            qp.setTopicCategory( topicCategory );
            qp.setResourceLanguage( languageList );

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
                LOG.debug( "SV_ServiceIdentification/ServiceType element..." );

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
                LOG.debug( "SV_ServiceIdentification/ServiceTypeVersion element..." );
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
                LOG.debug( "SV_ServiceIdentification/AccessProperties element..." );
                accessProperties = getElement( sv_serviceIdentification, new XPath( "./srv:accessProperties",
                                                                                    nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Restrictions
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "SV_ServiceIdentification/Restrictions element..." );
                restrictions = getElement( sv_serviceIdentification, new XPath( "./srv:restrictions", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Keywords
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "SV_ServiceIdentification/Keywords element..." );
                keywords_service = getElements( sv_serviceIdentification,
                                                new XPath( "./srv:keywords", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * Extent
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "SV_ServiceIdentification/Extent element..." );
                extent_service = getElements( sv_serviceIdentification, new XPath( "./srv:extent", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * CoupledResource
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "SV_ServiceIdentification/CoupledResource element..." );
                coupledResource = getElements( sv_serviceIdentification, new XPath( "./srv:coupledResource",
                                                                                    nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * CouplingType
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "SV_ServiceIdentification/CouplingType element..." );
                couplingType = getElement( sv_serviceIdentification, new XPath( "./srv:couplingType", nsContextParseII ) );

                /*---------------------------------------------------------------
                 * SV_ServiceIdentification
                 * ContainsOperations
                 * 
                 *---------------------------------------------------------------*/
                LOG.debug( "SV_ServiceIdentification/ContainsOperations element..." );
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
                LOG.debug( "SV_ServiceIdentification/OperatesOn element..." );
                operatesOn = getElements( sv_serviceIdentification, new XPath( "./srv:operatesOn", nsContextParseII ) );
            }
            /*---------------------------------------------------------------
             * SV_ServiceIdentification or MD_DataIdentification
             * Setting the EXTENT for one of the metadatatypes (service or data)
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "setting element Extent either from MD_DataIdentification or SV_ServiceIdentification..." );
            List<OMElement> extent = (List<OMElement>) ( extent_md_dataIdent.size() != 0 ? extent_md_dataIdent
                                                                                        : extent_service );

            Date tempBeg = null;
            Date tempEnd = null;

            String geographicDescriptionCode_service = null;
            String[] geographicDescriptionCode_serviceOtherLang = null;
            List<BoundingBox> bboxList = new LinkedList<BoundingBox>();

            for ( OMElement extentElem : extent ) {

                String temporalExtentBegin = getNodeAsString(
                                                              extentElem,
                                                              new XPath(
                                                                         "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:beginPosition",
                                                                         nsContextParseII ), null );

                String temporalExtentEnd = getNodeAsString(
                                                            extentElem,
                                                            new XPath(
                                                                       "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:endPosition",
                                                                       nsContextParseII ), null );

                try {
                    if ( temporalExtentBegin != null && temporalExtentEnd != null ) {
                        tempBeg = new Date( temporalExtentBegin );
                        tempEnd = new Date( temporalExtentEnd );
                    }
                } catch ( ParseException e ) {
                    LOG.debug( "Parsing of Date failed. " );
                    throw new MetadataStoreException( "Parsing of Date failed. " + e.getMessage() );
                }

                List<OMElement> geographicElement = getElements( extentElem,
                                                                 new XPath( "./gmd:EX_Extent/gmd:geographicElement",
                                                                            nsContextParseII ) );

                for ( OMElement geographicElem : geographicElement ) {

                    double boundingBoxWestLongitude = 0.0;
                    double boundingBoxEastLongitude = 0.0;
                    double boundingBoxSouthLatitude = 0.0;
                    double boundingBoxNorthLatitude = 0.0;

                    OMElement bbox = getElement( geographicElem, new XPath( "./gmd:EX_GeographicBoundingBox",
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

                    if ( geographicDescriptionCode_service == null ) {
                        OMElement geographicDescriptionCode_serviceElem = getElement(
                                                                                      geographicElem,
                                                                                      new XPath(
                                                                                                 "./gmd:EX_GeopraphicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code",
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

                    if ( bbox != null ) {
                        bboxList.add( new BoundingBox( boundingBoxWestLongitude, boundingBoxSouthLatitude,
                                                       boundingBoxEastLongitude, boundingBoxNorthLatitude ) );
                    }
                }

            }

            qp.setTemporalExtentBegin( tempBeg );
            qp.setTemporalExtentEnd( tempEnd );
            qp.setBoundingBox( bboxList );

            List<String> geographicDescCode_serviceList = new ArrayList<String>();
            geographicDescCode_serviceList.addAll( Arrays.asList( geographicDescriptionCode_service ) );
            if ( geographicDescriptionCode_serviceOtherLang != null ) {
                geographicDescCode_serviceList.addAll( Arrays.asList( geographicDescriptionCode_serviceOtherLang ) );
            }
            qp.setGeographicDescriptionCode_service( geographicDescCode_serviceList );

            /*---------------------------------------------------------------
             * SV_ServiceIdentification and IdentificationInfo
             * Setting all the KEYWORDS found in the metadata 
             * 
             *---------------------------------------------------------------*/
            LOG.debug( "setting all the keywords found in metadata..." );

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
            LOG.debug( "setting the couplingType..." );

            List<String> operatesOnList = new ArrayList<String>();
            List<OperatesOnData> operatesOnDataList = new ArrayList<OperatesOnData>();
            for ( OMElement operatesOnElem : operatesOn ) {
                String operatesOnStringUuIdAttribute = operatesOnElem.getAttributeValue( new QName( "uuidref" ) );
                String operatesOnString = "";
                if ( operatesOnStringUuIdAttribute != null && !operatesOnStringUuIdAttribute.equals( "" ) ) {
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
            LOG.debug( "checking consistency in coupling..." );

            if ( sv_serviceIdentification != null ) {

                if ( couplingTypeString.equals( "loose" ) ) {
                    // TODO
                    LOG.debug( "coupling: loose..." );

                } else if ( couplingTypeString.equals( "tight" ) ) {
                    LOG.debug( "coupling: tight..." );

                    // ci.determineTightlyCoupled( operatesOnList, Arrays.asList( operatesOnIdentifierList ) );

                    // } else {
                    // // mixed coupled if there are loose and tight coupled resources.
                    //
                }
            }

        }

    }

    /**
     * ResourceConstraints and hasSecurityConstraints
     * 
     * @param parent
     * @param qp
     * @param rp
     */
    private void parseResourceConstraints( QueryableProperties qp, ReturnableProperties rp, OMElement parent ) {

        LOG.debug( "ResourceConstraints element..." );
        List<OMElement> resourceConstraints = getElements( parent, new XPath( "./gmd:resourceConstraints",
                                                                              nsContextParseII ) );

        String[] useLim = getNodesAsStrings(
                                             parent,
                                             new XPath(
                                                        "./gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString",
                                                        nsContextParseII ) );

        qp.setLimitation( Arrays.asList( useLim ) );

        String[] accessConst = getNodesAsStrings(
                                                  parent,
                                                  new XPath(
                                                             "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue",
                                                             nsContextParseII ) );

        qp.setAccessConstraints( Arrays.asList( accessConst ) );

        String[] useConst = getNodesAsStrings(
                                               parent,
                                               new XPath(
                                                          "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints/gco:CharacterString",
                                                          nsContextParseII ) );

        String[] otherConst = getNodesAsStrings(
                                                 parent,
                                                 new XPath(
                                                            "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString",
                                                            nsContextParseII ) );

        qp.setOtherConstraints( Arrays.asList( otherConst ) );

        qp.setClassification( Arrays.asList( getNodesAsStrings(
                                                                parent,
                                                                new XPath(
                                                                           "./gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue",
                                                                           nsContextParseII ) ) ) );

        LOG.debug( "hasSecurityConstraints..." );
        OMElement hasSecurityConstraintsElement = null;
        List<String> rightsElements = new ArrayList<String>();
        for ( OMElement resourceConstraintsElem : resourceConstraints ) {
            String rights = getNodeAsString(
                                             resourceConstraintsElem,
                                             new XPath(
                                                        "./gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue",
                                                        nsContextParseII ), null );
            if ( rights != null ) {
                rightsElements.add( rights );
            }
            hasSecurityConstraintsElement = getElement( resourceConstraintsElem,
                                                        new XPath( "./gmd:MD_SecurityConstraints", nsContextParseII ) );

        }
        if ( rightsElements != null ) {
            LOG.debug( "hasRights..." );
            rp.setRights( rightsElements );
        }

        boolean hasSecurityConstraint = false;
        if ( hasSecurityConstraintsElement != null ) {
            hasSecurityConstraint = true;
        }
        qp.setHasSecurityConstraints( hasSecurityConstraint );
    }

    /**
     * GraphicOverview
     * 
     * @param rp
     */
    private void parseGraphicOverview( ReturnableProperties rp, OMElement parent ) {

        LOG.debug( "GraphicOverview element..." );
        List<OMElement> graphicOverview = getElements( parent, new XPath( "./gmd:graphicOverview", nsContextParseII ) );

        String graphicOverviewString = getNodeAsString(
                                                        parent,
                                                        new XPath(
                                                                   "./gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString",
                                                                   nsContextParseII ), null );
        rp.setGraphicOverview( graphicOverviewString );

    }

    private void parseAbstract( QueryableProperties qp, OMElement parent ) {
        /*---------------------------------------------------------------
         * 
         * Abstract
         * 
         *---------------------------------------------------------------*/
        LOG.debug( "Abstract element..." );
        OMElement _abstract = getElement( parent, new XPath( "./gmd:abstract", nsContextParseII ) );

        String[] _abstractOtherLang = getNodesAsStrings(
                                                         _abstract,
                                                         new XPath(
                                                                    "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
                                                                    nsContextParseII ) );

        String[] _abstractStrings = getNodesAsStrings( _abstract, new XPath( "./gco:CharacterString", nsContextParseII ) );
        List<String> _abstractList = new ArrayList<String>();
        _abstractList.addAll( Arrays.asList( _abstractStrings ) );
        if ( _abstractOtherLang != null ) {
            _abstractList.addAll( Arrays.asList( _abstractOtherLang ) );
        }
        qp.set_abstract( _abstractList );

    }

}
