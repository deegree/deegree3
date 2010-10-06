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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
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

    private ParsedProfileElement pElem;

    private static String[] summaryLocalParts = new String[14];

    private static String[] briefLocalParts = new String[9];

    private static String[] briefSummaryLocalParts = new String[23];

    static {

        summaryLocalParts[0] = "datasetURI";
        summaryLocalParts[1] = "locale";
        summaryLocalParts[2] = "spatialRepresentationInfo";
        summaryLocalParts[3] = "metadataExtensionInfo";
        summaryLocalParts[4] = "contentInfo";
        summaryLocalParts[5] = "portrayalCatalogueInfo";
        summaryLocalParts[6] = "metadataConstraints";
        summaryLocalParts[7] = "applicationSchemaInfo";
        summaryLocalParts[8] = "metadataMaintenance";
        summaryLocalParts[9] = "series";
        summaryLocalParts[10] = "describes";
        summaryLocalParts[11] = "propertyType";
        summaryLocalParts[12] = "featureType";
        summaryLocalParts[13] = "featureAttribute";

        briefLocalParts[0] = "language";
        briefLocalParts[1] = "metadataCharacterSet";
        briefLocalParts[2] = "parentIdentifier";
        briefLocalParts[3] = "hierarchieLevelName";
        briefLocalParts[4] = "metadataStandardName";
        briefLocalParts[5] = "metadataStandardVersion";
        briefLocalParts[6] = "referenceSystemInfo";
        briefLocalParts[7] = "distributionInfo";
        briefLocalParts[8] = "dataQualityInfo";

        briefSummaryLocalParts[0] = "datasetURI";
        briefSummaryLocalParts[1] = "locale";
        briefSummaryLocalParts[2] = "spatialRepresentationInfo";
        briefSummaryLocalParts[3] = "metadataExtensionInfo";
        briefSummaryLocalParts[4] = "contentInfo";
        briefSummaryLocalParts[5] = "portrayalCatalogueInfo";
        briefSummaryLocalParts[6] = "metadataConstraints";
        briefSummaryLocalParts[7] = "applicationSchemaInfo";
        briefSummaryLocalParts[8] = "metadataMaintenance";
        briefSummaryLocalParts[9] = "series";
        briefSummaryLocalParts[10] = "describes";
        briefSummaryLocalParts[11] = "propertyType";
        briefSummaryLocalParts[12] = "featureType";
        briefSummaryLocalParts[13] = "featureAttribute";
        briefSummaryLocalParts[14] = "language";
        briefSummaryLocalParts[15] = "metadataCharacterSet";
        briefSummaryLocalParts[16] = "parentIdentifier";
        briefSummaryLocalParts[17] = "hierarchieLevelName";
        briefSummaryLocalParts[18] = "metadataStandardName";
        briefSummaryLocalParts[19] = "metadataStandardVersion";
        briefSummaryLocalParts[20] = "referenceSystemInfo";
        briefSummaryLocalParts[21] = "distributionInfo";
        briefSummaryLocalParts[22] = "dataQualityInfo";
    }

    private static Set<QName> summaryFilterElements;

    // public ISORecord( URL url ) {
    // this.root = new XMLAdapter( xmlStream ).getRootElement();
    // }

    public ISORecord( XMLStreamReader xmlStream ) throws MetadataStoreException {

        this.root = new XMLAdapter( xmlStream ).getRootElement();

        this.pElem = new ISOQPParsing().parseAPISO( root, false );
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

        return (String[]) pElem.getQueryableProperties().get_abstract().toArray();
    }

    @Override
    public Envelope[] getBoundingBox() {

        Envelope[] env = new Envelope[1];

        BoundingBox box = pElem.getQueryableProperties().getBoundingBox();
        env[0] = new GeometryFactory().createEnvelope( box.getWestBoundLongitude(), box.getSouthBoundLatitude(),
                                                       box.getEastBoundLongitude(), box.getNorthBoundLatitude(),
                                                       new CRS( "EPSG:4326" ) );

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
    public Date[] getModified()
                            throws MetadataStoreException {
        Date[] d = this.pElem.getQueryableProperties().getModified();

        return d;
    }

    @Override
    public String[] getRelation() {

        return (String[]) pElem.getReturnableProperties().getRelation().toArray();
    }

    @Override
    public Object[] getSpatial() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getTitle() {

        return (String[]) pElem.getQueryableProperties().getTitle().toArray();
    }

    @Override
    public String getType() {

        return pElem.getQueryableProperties().getType();
    }

    @Override
    public String[] getSubject() {

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

        switch ( returnType ) {
        case brief:
            StAXParsingHelper.skipStartDocument( xmlStream );
            toBrief( writer, xmlStream );
            break;
        case summary:
            StAXParsingHelper.skipStartDocument( xmlStream );
            toSummary( writer, xmlStream );
            break;
        case full:
            StAXParsingHelper.skipStartDocument( xmlStream );

            XMLAdapter.writeElement( writer, xmlStream );
            break;
        default:
            StAXParsingHelper.skipStartDocument( xmlStream );
            toSummary( writer, xmlStream );
            break;
        }

    }

    @Override
    public DCRecord toDublinCore() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isHasSecurityConstraints() {

        return pElem.getQueryableProperties().isHasSecurityConstraints();
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

    private void toSummary( XMLStreamWriter writer, XMLStreamReader xmlStream )
                            throws XMLStreamException {

        XMLStreamReader filter = new NamedElementFilter( xmlStream, removeElementsISONamespace( summaryLocalParts ) );

        generateOutput( writer, filter );

    }

    private void toBrief( XMLStreamWriter writer, XMLStreamReader xmlStream )
                            throws XMLStreamException {
        XMLStreamReader filter = new NamedElementFilter( xmlStream, removeElementsISONamespace( briefSummaryLocalParts ) );
        generateOutput( writer, filter );
    }

    private Set<QName> removeElementsISONamespace( String[] localParts ) {
        Set<QName> removeElements = new HashSet<QName>();
        for ( String l : localParts ) {
            removeElements.add( new QName( "http://www.isotc211.org/2005/gmd", l, "gmd" ) );
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