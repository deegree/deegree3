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
package org.deegree.metadata;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRS;
import org.deegree.filter.Filter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso.parsing.ISOQPParsing;
import org.deegree.metadata.persistence.iso.parsing.ParsedProfileElement;
import org.deegree.metadata.persistence.types.BoundingBox;
import org.deegree.metadata.persistence.types.Format;
import org.deegree.metadata.persistence.types.Keyword;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an ISO 19115 {@link MetadataRecord}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISORecord implements MetadataRecord {

    private static Logger LOG = LoggerFactory.getLogger( ISORecord.class );

    private OMElement root;

    private XMLAdapter adapter;

    // private static NamespaceContext nsContextISOParsing = CommonNamespaces.getNamespaceContext();

    // private List<OMElement> identificationInfo;

    // private OMElement sv_service_OR_md_dataIdentification;

    private GeometryFactory geom;

    // private List<OMElement> resourceConstraints;

    private ParsedProfileElement pElem;

    // public ISORecord( URL url ) {
    // this.root = new XMLAdapter( xmlStream ).getRootElement();
    // }

    public ISORecord( XMLStreamReader xmlStream ) throws MetadataStoreException {
        // this.adapter = new XMLAdapter( xmlStream );
        // adapter.setRootElement( root );
        // identificationInfo = adapter.getElements( root, new XPath( "./gmd:identificationInfo", nsContextISOParsing )
        // );
        // sv_service_OR_md_dataIdentification = adapter.getElement(
        // identificationInfo.get( 0 ),
        // new XPath(
        // "./srv:SV_ServiceIdentification | ./gmd:MD_DataIdentification",
        // nsContextISOParsing ) );

        // md_dataIdentification = adapter.getElement( identificationInfo.get( 0 ), new XPath(
        // "./gmd:MD_DataIdentification",
        // nsContextISOParsing ) );
        this.geom = new GeometryFactory();
        this.root = new XMLAdapter( xmlStream ).getRootElement();

        this.pElem = new ISOQPParsing().parseAPISO( root, false );
        LOG.info( pElem.toString() );

        // parseResourceConstraints();
    }

    public ISORecord( OMElement root ) throws MetadataStoreException {
        this( root.getXMLStreamReader() );

    }

    @Override
    public boolean eval( Filter filter ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String[] getAbstract() {

        // OMElement _abstract = adapter.getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:abstract",
        // nsContextISOParsing ) );
        //
        // String[] _abstractOtherLang = adapter.getNodesAsStrings(
        // _abstract,
        // new XPath(
        // "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
        // nsContextISOParsing ) );
        //
        // String[] _abstractStrings = adapter.getNodesAsStrings( _abstract, new XPath( "./gco:CharacterString",
        // nsContextISOParsing ) );

        return (String[]) pElem.getQueryableProperties().get_abstract().toArray();
    }

    @Override
    public Envelope[] getBoundingBox() {
        // List<OMElement> extent_md_dataIdent = adapter.getElements( sv_service_OR_md_dataIdentification,
        // new XPath( "./gmd:extent", nsContextISOParsing ) );
        // List<OMElement> extent_service = adapter.getElements( sv_service_OR_md_dataIdentification,
        // new XPath( "./srv:extent", nsContextISOParsing ) );
        //
        // List<OMElement> extent = (List<OMElement>) ( extent_md_dataIdent.size() != 0 ? extent_md_dataIdent
        // : extent_service );
        //
        // double boundingBoxWestLongitude = 0.0;
        // double boundingBoxEastLongitude = 0.0;
        // double boundingBoxSouthLatitude = 0.0;
        // double boundingBoxNorthLatitude = 0.0;
        //
        // CRS crs = null;
        // Date tempBeg = null;
        // Date tempEnd = null;
        // List<CRS> crsList = new ArrayList<CRS>();
        //
        // String geographicDescriptionCode_service = null;
        // String[] geographicDescriptionCode_serviceOtherLang = null;
        Envelope[] env = new Envelope[1];
        // int counter = 0;
        // for ( OMElement extentElem : extent ) {
        //
        // String temporalExtentBegin = adapter.getNodeAsString(
        // extentElem,
        // new XPath(
        // "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:beginPosition",
        // nsContextISOParsing ), null );
        //
        // String temporalExtentEnd = adapter.getNodeAsString(
        // extentElem,
        // new XPath(
        // "./gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod/gmd:endPosition",
        // nsContextISOParsing ), null );
        //
        // try {
        // if ( temporalExtentBegin != null && temporalExtentEnd != null ) {
        // tempBeg = new Date( temporalExtentBegin );
        // tempEnd = new Date( temporalExtentEnd );
        // }
        // } catch ( ParseException e ) {
        //
        // e.printStackTrace();
        // }
        //
        // OMElement bbox = adapter.getElement(
        // extentElem,
        // new XPath(
        // "./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox",
        // nsContextISOParsing ) );
        // if ( boundingBoxWestLongitude == 0.0 ) {
        // boundingBoxWestLongitude = adapter.getNodeAsDouble( bbox,
        // new XPath( "./gmd:westBoundLongitude/gco:Decimal",
        // nsContextISOParsing ), 0.0 );
        //
        // }
        // if ( boundingBoxEastLongitude == 0.0 ) {
        // boundingBoxEastLongitude = adapter.getNodeAsDouble( bbox,
        // new XPath( "./gmd:eastBoundLongitude/gco:Decimal",
        // nsContextISOParsing ), 0.0 );
        //
        // }
        // if ( boundingBoxSouthLatitude == 0.0 ) {
        // boundingBoxSouthLatitude = adapter.getNodeAsDouble( bbox,
        // new XPath( "./gmd:southBoundLatitude/gco:Decimal",
        // nsContextISOParsing ), 0.0 );
        //
        // }
        // if ( boundingBoxNorthLatitude == 0.0 ) {
        // boundingBoxNorthLatitude = adapter.getNodeAsDouble( bbox,
        // new XPath( "./gmd:northBoundLatitude/gco:Decimal",
        // nsContextISOParsing ), 0.0 );
        //
        // }
        //
        // if ( bbox != null ) {
        // crs = new CRS( "EPSG:4326" );
        // crsList.add( crs );
        // // TODO
        // }
        //
        // if ( geographicDescriptionCode_service == null ) {
        // OMElement geographicDescriptionCode_serviceElem = adapter.getElement(
        // extentElem,
        // new XPath(
        // "./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeopraphicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code",
        // nsContextISOParsing ) );
        // geographicDescriptionCode_service = adapter.getNodeAsString( geographicDescriptionCode_serviceElem,
        // new XPath( "./gco:CharacterString",
        // nsContextISOParsing ), null );
        // geographicDescriptionCode_serviceOtherLang = adapter.getNodesAsStrings(
        // geographicDescriptionCode_serviceElem,
        // new XPath(
        // "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
        // nsContextISOParsing ) );
        // }
        BoundingBox box = pElem.getQueryableProperties().getBoundingBox();
        env[0] = geom.createEnvelope( box.getWestBoundLongitude(), box.getSouthBoundLatitude(),
                                      box.getEastBoundLongitude(), box.getNorthBoundLatitude(), new CRS( "EPSG:4326" ) );
        // }

        return env;
    }

    @Override
    public String[] getFormat() {
        // List<OMElement> formats = adapter.getElements(
        // root,
        // new XPath(
        // "./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format",
        // nsContextISOParsing ) );

        // String onlineResource = getNodeAsString(
        // rootElement,
        // new XPath(
        // "./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
        // nsContextISOParsing ), null );

        // List<Format> listOfFormats = new ArrayList<Format>();

        // String[] formate = adapter.getNodesAsStrings(
        // root,
        // new XPath(
        // "/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format/gmd:name/gco:CharacterString",
        // nsContextISOParsing ) );
        // for ( OMElement md_format : formats ) {
        //
        // String formatName = adapter.getNodeAsString( md_format, new XPath( "./gmd:name/gco:CharacterString",
        // nsContextISOParsing ), null );
        //
        // String formatVersion = adapter.getNodeAsString( md_format, new XPath( "./gmd:version/gco:CharacterString",
        // nsContextISOParsing ), null );
        //
        // Format formatClass = new Format( formatName, formatVersion );
        // listOfFormats.add( formatClass );
        //
        // }
        List<Format> formats = pElem.getQueryableProperties().getFormat();
        String[] format = new String[formats.size()];
        int counter = 0;
        for ( Format f : formats ) {
            format[counter++] = f.getName();
        }
        return format;
    }

    @Override
    public String[] getIdentifier() {
        // String[] fileIdentifier = adapter.getNodesAsStrings( root,
        // new XPath( "./gmd:fileIdentifier/gco:CharacterString",
        // nsContextISOParsing ) );

        return pElem.getQueryableProperties().getIdentifier();
    }

    @Override
    public Date[] getModified()
                            throws MetadataStoreException {
        Date[] d = this.pElem.getQueryableProperties().getModified();

        return d;
    }

    @Override
    public String[] getRelation() {
        // List<OMElement> aggregationInfo = adapter.getElements( sv_service_OR_md_dataIdentification,
        // new XPath( "./gmd:aggregationInfo", nsContextISOParsing ) );

        // String[] relation = adapter.getNodesAsStrings( sv_service_OR_md_dataIdentification,
        // new XPath( "./gmd:aggregationInfo/gco:CharacterString",
        // nsContextISOParsing ) );

        // List<String> relationList = new ArrayList<String>();
        // for ( OMElement aggregatInfoElem : aggregationInfo ) {
        //
        // String relation = adapter.getNodeAsString( aggregatInfoElem, new XPath( "./gco:CharacterString",
        // nsContextISOParsing ), null );
        // relationList.add( relation );
        //
        // }

        return (String[]) pElem.getReturnableProperties().getRelation().toArray();
    }

    @Override
    public Object[] getSpatial() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getTitle() {
        // OMElement citation = adapter.getElement( sv_service_OR_md_dataIdentification, new XPath( "./gmd:citation",
        // nsContextISOParsing ) );
        // OMElement ci_citation = adapter.getElement( citation, new XPath( "./gmd:CI_Citation", nsContextISOParsing )
        // );
        // OMElement title = adapter.getElement( ci_citation, new XPath( "./gmd:title", nsContextISOParsing ) );
        //
        // String[] titleList = adapter.getNodesAsStrings(
        // title,
        // new XPath(
        // "./gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
        // nsContextISOParsing ) );

        return (String[]) pElem.getQueryableProperties().getTitle().toArray();
    }

    @Override
    public String getType() {
        // /**
        // * if provided data is a dataset: type = dataset (default)
        // * <p>
        // * if provided data is a datasetCollection: type = series
        // * <p>
        // * if provided data is an application: type = application
        // * <p>
        // * if provided data is a service: type = service
        // */
        // String type = adapter.getNodeAsString( root, new XPath(
        // "./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
        // nsContextISOParsing ), "dataset" );
        return pElem.getQueryableProperties().getType();
    }

    @Override
    public String[] getSubject() {
        // List<OMElement> descriptiveKeywords = adapter.getElements(
        // sv_service_OR_md_dataIdentification,
        // new XPath(
        // "./gmd:descriptiveKeywords || ./srv:keywords",
        // nsContextISOParsing ) );
        //
        // List<Keyword> listOfKeywords = new ArrayList<Keyword>();
        // List<String> keywordList = new ArrayList<String>();
        // for ( OMElement md_keywords : descriptiveKeywords ) {
        //
        // String keywordType = adapter.getNodeAsString(
        // md_keywords,
        // new XPath(
        // "./gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue",
        // nsContextISOParsing ), null );
        //
        // String[] keywords = adapter.getNodesAsStrings(
        // md_keywords,
        // new XPath(
        // "./gmd:MD_Keywords/gmd:keyword/gco:CharacterString",
        // nsContextISOParsing ) );
        //
        // String[] keywordsOtherLang = adapter.getNodesAsStrings(
        // md_keywords,
        // new XPath(
        // "./gmd:MD_Keywords/gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString",
        // nsContextISOParsing ) );
        //
        // String thesaurus = adapter.getNodeAsString(
        // md_keywords,
        // new XPath(
        // "./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString",
        // nsContextISOParsing ), null );
        //
        // keywordList.addAll( Arrays.asList( keywords ) );
        // if ( keywordsOtherLang != null ) {
        // keywordList.addAll( Arrays.asList( keywordsOtherLang ) );
        // }
        // listOfKeywords.add( new Keyword( keywordType, keywordList, thesaurus ) );
        //
        // }
        List<Keyword> keywords = pElem.getQueryableProperties().getKeywords();
        String[] keywordNames = new String[keywords.size()];
        int counter = 0;
        for ( Keyword k : keywords ) {
            for ( String kName : k.getKeywords() ) {
                keywordNames[counter++] = kName;
            }
        }

        return keywordNames;
    }

    @Override
    public XMLStreamReader getAsXMLStream()
                            throws XMLStreamException {
        XMLStreamReader xmlStream = root.getXMLStreamReader();
        StAXParsingHelper.skipStartDocument( xmlStream );
        return xmlStream;
    }

    @Override
    public void serialize( XMLStreamWriter writer, ReturnableElement returnType )
                            throws XMLStreamException {

        XMLStreamReader xmlStream = root.getXMLStreamReader();
        StAXParsingHelper.skipStartDocument( xmlStream );
        XMLAdapter.writeElement( writer, xmlStream );
    }

    @Override
    public DCRecord toDublinCore() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isHasSecurityConstraints() {
        // LOG.debug( "hasSecurityConstraints..." );
        // OMElement hasSecurityConstraintsElement = null;
        // String[] rightsElements = null;
        // for ( OMElement resourceConstraintsElem : resourceConstraints ) {
        // rightsElements = adapter.getNodesAsStrings(
        // resourceConstraintsElem,
        // new XPath(
        // "./gmd:MD_LegalConstraints/gmd:accessConstraints/@codeListValue",
        // nsContextISOParsing ) );
        //
        // hasSecurityConstraintsElement = adapter.getElement( resourceConstraintsElem,
        // new XPath( "./gmd:MD_SecurityConstraints",
        // nsContextISOParsing ) );
        //
        // }
        // // if ( rightsElements != null ) {
        // // LOG.debug( "hasRights..." );
        // // rp.setRights( Arrays.asList( rightsElements ) );
        // // }
        //
        // boolean hasSecurityConstraint = false;
        // if ( hasSecurityConstraintsElement != null ) {
        // hasSecurityConstraint = true;
        // }

        return pElem.getQueryableProperties().isHasSecurityConstraints();
    }

    // private void parseResourceConstraints() {
    // /*---------------------------------------------------------------
    // *
    // * ResourceConstraints
    // *
    // *---------------------------------------------------------------*/
    // LOG.debug( "ResourceConstraints element..." );
    // List<OMElement> resourceConstraints = adapter.getElements( sv_service_OR_md_dataIdentification,
    // new XPath( "./gmd:resourceConstraints",
    // nsContextISOParsing ) );
    //
    // String[] useLim = adapter.getNodesAsStrings(
    // sv_service_OR_md_dataIdentification,
    // new XPath(
    // "./gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString",
    // nsContextISOParsing ) );
    //
    // String[] accessConst = adapter.getNodesAsStrings(
    // sv_service_OR_md_dataIdentification,
    // new XPath(
    // "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue",
    // nsContextISOParsing ) );
    //
    // String[] useConst = adapter.getNodesAsStrings(
    // sv_service_OR_md_dataIdentification,
    // new XPath(
    // "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints/gco:CharacterString",
    // nsContextISOParsing ) );
    //
    // String[] otherConst = adapter.getNodesAsStrings(
    // sv_service_OR_md_dataIdentification,
    // new XPath(
    // "./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString",
    // nsContextISOParsing ) );
    //
    // String[] securityConstraints = adapter.getNodesAsStrings(
    // sv_service_OR_md_dataIdentification,
    // new XPath(
    // "./gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue",
    // nsContextISOParsing ) );
    //
    // }

    public String getLanguage() {
        // return adapter.getNodeAsString(
        // root,
        // new XPath(
        // "./gmd:language/gco:CharacterString | ./gmd:language/gmd:LanguageCode/@codeListValue",
        // nsContextISOParsing ), null );
        return pElem.getQueryableProperties().getLanguage();
    }

    public String getParentIdentifier() {
        // return adapter.getNodeAsString( root, new XPath( "./gmd:parentIdentifier/gco:CharacterString",
        // nsContextISOParsing ), null );
        return pElem.getQueryableProperties().getParentIdentifier();

    }

    @Override
    public ParsedProfileElement getParsedElement() {
        return pElem;
    }

}