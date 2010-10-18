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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRS;
import org.deegree.cs.CRSCodeType;
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

    private ParsedProfileElement pElem;

    private static String[] summaryLocalParts = new String[14];

    private static String[] briefLocalParts = new String[9];

    private static String[] briefSummaryLocalParts = new String[23];

    private static final NamespaceContext ns = new NamespaceContext();

    private static final List<XPath> m = new ArrayList<XPath>();

    static {
        ns.addNamespace( "gmd", "http://www.isotc211.org/2005/gmd" );

        m.add( new XPath( "/gmd:MD_Metadata/gmd:fileIdentifier", ns ) );
        m.add( new XPath( "/gmd:MD_Metadata/gmd:metadataStandardVersion", ns ) );
        m.add( new XPath( "/gmd:MD_Metadata/gmd:series", ns ) );

        summaryLocalParts[0] = "/gmd:MD_Metadata/gmd:dataSetURI";
        summaryLocalParts[1] = "/gmd:MD_Metadata/gmd:locale";
        summaryLocalParts[2] = "/gmd:MD_Metadata/gmd:spatialRepresentationInfo";
        summaryLocalParts[3] = "/gmd:MD_Metadata/gmd:metadataExtensionInfo";
        summaryLocalParts[4] = "/gmd:MD_Metadata/gmd:contentInfo";
        summaryLocalParts[5] = "/gmd:MD_Metadata/gmd:portrayalCatalogueInfo";
        summaryLocalParts[6] = "/gmd:MD_Metadata/gmd:metadataConstraints";
        summaryLocalParts[7] = "/gmd:MD_Metadata/gmd:applicationSchemaInfo";
        summaryLocalParts[8] = "/gmd:MD_Metadata/gmd:metadataMaintenance";
        summaryLocalParts[9] = "/gmd:MD_Metadata/gmd:series";
        summaryLocalParts[10] = "/gmd:MD_Metadata/gmd:describes";
        summaryLocalParts[11] = "/gmd:MD_Metadata/gmd:propertyType";
        summaryLocalParts[12] = "/gmd:MD_Metadata/gmd:featureType";
        summaryLocalParts[13] = "/gmd:MD_Metadata/gmd:featureAttribute";

        briefLocalParts[0] = "/gmd:MD_Metadata/gmd:language";
        briefLocalParts[1] = "/gmd:MD_Metadata/gmd:characterSet";
        briefLocalParts[2] = "/gmd:MD_Metadata/gmd:parentIdentifier";
        briefLocalParts[3] = "/gmd:MD_Metadata/gmd:hierarchieLevelName";
        briefLocalParts[4] = "/gmd:MD_Metadata/gmd:metadataStandardName";
        briefLocalParts[5] = "/gmd:MD_Metadata/gmd:metadataStandardVersion";
        briefLocalParts[6] = "/gmd:MD_Metadata/gmd:referenceSystemInfo";
        briefLocalParts[7] = "/gmd:MD_Metadata/gmd:distributionInfo";
        briefLocalParts[8] = "/gmd:MD_Metadata/gmd:dataQualityInfo";
        // metadatacharacterSet

        briefSummaryLocalParts[0] = "/gmd:MD_Metadata/gmd:dataSetURI";
        briefSummaryLocalParts[1] = "/gmd:MD_Metadata/gmd:locale";
        briefSummaryLocalParts[2] = "/gmd:MD_Metadata/gmd:spatialRepresentationInfo";
        briefSummaryLocalParts[3] = "/gmd:MD_Metadata/gmd:metadataExtensionInfo";
        briefSummaryLocalParts[4] = "/gmd:MD_Metadata/gmd:contentInfo";
        briefSummaryLocalParts[5] = "/gmd:MD_Metadata/gmd:portrayalCatalogueInfo";
        briefSummaryLocalParts[6] = "/gmd:MD_Metadata/gmd:metadataConstraints";
        briefSummaryLocalParts[7] = "/gmd:MD_Metadata/gmd:applicationSchemaInfo";
        briefSummaryLocalParts[8] = "/gmd:MD_Metadata/gmd:metadataMaintenance";
        briefSummaryLocalParts[9] = "/gmd:MD_Metadata/gmd:series";
        briefSummaryLocalParts[10] = "/gmd:MD_Metadata/gmd:describes";
        briefSummaryLocalParts[11] = "/gmd:MD_Metadata/gmd:propertyType";
        briefSummaryLocalParts[12] = "/gmd:MD_Metadata/gmd:featureType";
        briefSummaryLocalParts[13] = "/gmd:MD_Metadata/gmd:featureAttribute";
        briefSummaryLocalParts[14] = "/gmd:MD_Metadata/gmd:language";
        briefSummaryLocalParts[15] = "/gmd:MD_Metadata/gmd:characterSet";
        briefSummaryLocalParts[16] = "/gmd:MD_Metadata/gmd:parentIdentifier";
        briefSummaryLocalParts[17] = "/gmd:MD_Metadata/gmd:hierarchieLevelName";
        briefSummaryLocalParts[18] = "/gmd:MD_Metadata/gmd:metadataStandardName";
        briefSummaryLocalParts[19] = "/gmd:MD_Metadata/gmd:metadataStandardVersion";
        briefSummaryLocalParts[20] = "/gmd:MD_Metadata/gmd:referenceSystemInfo";
        briefSummaryLocalParts[21] = "/gmd:MD_Metadata/gmd:distributionInfo";
        briefSummaryLocalParts[22] = "/gmd:MD_Metadata/gmd:dataQualityInfo";
    }

    private static List<XPath> summaryFilterElementsXPath;

    private static List<XPath> briefFilterElementsXPath;

    public ISORecord( XMLStreamReader xmlStream ) throws MetadataStoreException {

        this.root = new XMLAdapter( xmlStream ).getRootElement();
        this.pElem = new ISOQPParsing().parseAPISO( root, false );

        summaryFilterElementsXPath = removeElementsXPath( summaryLocalParts );

        briefFilterElementsXPath = removeElementsXPath( briefSummaryLocalParts );

    }

    public ISORecord( OMElement root ) throws MetadataStoreException {

        this( root.getXMLStreamReader() );
    }

    @Override
    public QName getName() {
        return root.getQName();
    }

    @Override
    public boolean eval( Filter filter ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String[] getAbstract() {

        List<String> l = pElem.getQueryableProperties().get_abstract();
        String[] s = new String[l.size()];
        int counter = 0;
        for ( String st : l ) {
            s[counter++] = st;
        }

        return s;
    }

    @Override
    public Envelope[] getBoundingBox() {

        List<BoundingBox> bboxList = pElem.getQueryableProperties().getBoundingBox();
        if ( pElem.getQueryableProperties().getCrs().isEmpty() ) {
            List<CRSCodeType> newCRSList = new LinkedList<CRSCodeType>();
            for ( BoundingBox b : bboxList ) {
                newCRSList.add( new CRSCodeType( "4326", "EPSG" ) );
            }

            pElem.getQueryableProperties().setCrs( newCRSList );
        }

        Envelope[] env = new Envelope[bboxList.size()];
        int counter = 0;
        for ( BoundingBox box : bboxList ) {
            CRSCodeType bboxCRS = pElem.getQueryableProperties().getCrs().get( counter );
            CRS crs = new CRS( bboxCRS.toString() );
            env[counter++] = new GeometryFactory().createEnvelope( box.getWestBoundLongitude(),
                                                                   box.getSouthBoundLatitude(),
                                                                   box.getEastBoundLongitude(),
                                                                   box.getNorthBoundLatitude(), crs );
        }
        return env;
    }

    @Override
    public String[] getFormat() {
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

        return pElem.getQueryableProperties().getIdentifier();
    }

    @Override
    public Date[] getModified() {
        Date[] d = this.pElem.getQueryableProperties().getModified();

        return d;
    }

    @Override
    public String[] getRelation() {
        List<String> l = pElem.getReturnableProperties().getRelation();
        String[] s = new String[l.size()];
        int counter = 0;
        for ( String st : l ) {
            s[counter++] = st;
        }

        return s;
    }

    @Override
    public Object[] getSpatial() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getTitle() {
        List<String> l = pElem.getQueryableProperties().getTitle();
        String[] s = new String[l.size()];
        int counter = 0;
        for ( String st : l ) {
            s[counter++] = st;
        }
        return s;
    }

    @Override
    public String getType() {

        return pElem.getQueryableProperties().getType();
    }

    @Override
    public String[] getSubject() {

        List<Keyword> keywords = pElem.getQueryableProperties().getKeywords();
        int keywordSizeCount = 0;
        for ( Keyword k : keywords ) {

            keywordSizeCount += k.getKeywords().size();

        }
        List<String> topicCategories = pElem.getQueryableProperties().getTopicCategory();

        String[] subjects = new String[keywordSizeCount + topicCategories.size()];
        int counter = 0;
        for ( Keyword k : keywords ) {
            for ( String kName : k.getKeywords() ) {
                subjects[counter++] = kName;
            }
        }
        for ( String topics : topicCategories ) {
            subjects[counter++] = topics;
        }

        return subjects;
    }

    /**
     * 
     * @return the ISORecord as xmlStreamReader with skipped startDocument-preamble
     * @throws XMLStreamException
     */
    public XMLStreamReader getAsXMLStream()
                            throws XMLStreamException {
        XMLStreamReader xmlStream = root.getXMLStreamReader();
        StAXParsingHelper.skipStartDocument( xmlStream );
        return xmlStream;
    }

    public byte[] getAsByteArray()
                            throws XMLStreamException, FactoryConfigurationError {

        return root.toString().getBytes();

    }

    @Override
    public void serialize( XMLStreamWriter writer, ReturnableElement returnType )
                            throws XMLStreamException {
        XMLStreamReader xmlStream = root.getXMLStreamReader();

        switch ( returnType ) {
        case brief:
            StAXParsingHelper.skipStartDocument( xmlStream );
            toISOBrief( writer, xmlStream );
            break;
        case summary:
            StAXParsingHelper.skipStartDocument( xmlStream );
            toISOSummary( writer, xmlStream );
            break;
        case full:
            StAXParsingHelper.skipStartDocument( xmlStream );
            XMLAdapter.writeElement( writer, xmlStream );
            break;
        default:
            StAXParsingHelper.skipStartDocument( xmlStream );
            toISOSummary( writer, xmlStream );
            break;
        }

    }

    @Override
    public void serialize( XMLStreamWriter writer, String[] elementNames )
                            throws XMLStreamException {
        List<XPath> xpathEN = new ArrayList<XPath>();
        for ( String s : elementNames ) {
            xpathEN.add( new XPath( s, CommonNamespaces.getNamespaceContext() ) );
        }

        // TODO change m to xpathEN
        OMElement elem = new XPathElementFilter( root, m );
        elem.serialize( writer );
    }

    @Override
    public DCRecord toDublinCore() {
        return new DCRecord( this );

    }

    public boolean isHasSecurityConstraints() {

        return pElem.getQueryableProperties().isHasSecurityConstraints();
    }

    @Override
    public String getContributor() {

        return pElem.getReturnableProperties().getContributor();
    }

    @Override
    public String getPublisher() {

        return pElem.getReturnableProperties().getPublisher();
    }

    @Override
    public String[] getRights() {
        List<String> l = pElem.getReturnableProperties().getRights();
        String[] s = new String[l.size()];
        int counter = 0;
        for ( String st : l ) {
            s[counter++] = st;
        }
        return s;

    }

    @Override
    public String getSource() {
        return pElem.getReturnableProperties().getSource();
    }

    @Override
    public String getCreator() {

        return pElem.getReturnableProperties().getCreator();
    }

    public String getLanguage() {
        return pElem.getQueryableProperties().getLanguage();
    }

    public String getParentIdentifier() {
        return pElem.getQueryableProperties().getParentIdentifier();

    }

    public ParsedProfileElement getParsedElement() {
        return pElem;
    }

    private void toISOSummary( XMLStreamWriter writer, XMLStreamReader xmlStream )
                            throws XMLStreamException {

        // XMLStreamReader filter = new NamedElementFilter( xmlStream, summaryFilterElements );
        OMElement filter = new XPathElementFilter( root, summaryFilterElementsXPath );
        filter.detach();
        generateOutput( writer, filter.getXMLStreamReader() );
    }

    private void toISOBrief( XMLStreamWriter writer, XMLStreamReader xmlStream )
                            throws XMLStreamException {
        // XMLStreamReader filter = new NamedElementFilter( xmlStream, briefSummaryFilterElements );
        OMElement filter = new XPathElementFilter( root, briefFilterElementsXPath );
        filter.detach();
        generateOutput( writer, filter.getXMLStreamReader() );

    }

    private Set<QName> removeElementsISONamespace( String[] localParts ) {
        Set<QName> removeElements = new HashSet<QName>();
        for ( String l : localParts ) {
            removeElements.add( new QName( "http://www.isotc211.org/2005/gmd", l, "gmd" ) );
        }

        return removeElements;

    }

    private List<XPath> removeElementsXPath( String[] xpathExpr ) {
        List<XPath> removeElements = new ArrayList<XPath>();
        for ( String l : xpathExpr ) {

            removeElements.add( new XPath( l, ns ) );
        }

        return removeElements;

    }

    private void generateOutput( XMLStreamWriter writer, XMLStreamReader filter )
                            throws XMLStreamException {

        while ( filter.hasNext() ) {

            if ( filter.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                XMLAdapter.writeElement( writer, filter );
            } else {
                filter.next();
            }
        }
        filter.close();

    }
}