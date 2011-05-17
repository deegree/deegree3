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
package org.deegree.metadata.iso;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.iso.persistence.parsing.ISOQPParsing;
import org.deegree.metadata.iso.persistence.parsing.ParsedProfileElement;
import org.deegree.metadata.iso.types.BoundingBox;
import org.deegree.metadata.iso.types.Format;
import org.deegree.metadata.iso.types.Keyword;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an ISO 19115 {@link MetadataRecord}.
 * <p>
 * An ISO 19115 record can be either a data or a service metadata record. The root element name for both types of
 * records is {http://www.isotc211.org/2005/gmd}MD_Metadata.
 * <ul>
 * <li>Data Metadata: /gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset' (or missing) or 'series' or
 * 'application'</li>
 * <li>Service Metadata: /gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service'</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISORecord implements MetadataRecord {

    private static Logger LOG = LoggerFactory.getLogger( ISORecord.class );

    /** Namespace for ISORecord elements */
    public static String ISO_RECORD_NS = CommonNamespaces.ISOAP10GMDNS;

    /** Schema URL for ISO Data and Service Metadata records **/
    public static final String SCHEMA_URL_GMD = "http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd";

    /** Additional schema URL for Service Metadata records **/
    public static final String SCHEMA_URL_SRV = "http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd";

    private OMElement root;

    private ParsedProfileElement pElem;

    private static final String STOPWORD = " ";

    private static final Set<String> summaryElements = new HashSet<String>();

    private static final Set<String> briefElements = new HashSet<String>();

    private static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

    private static final XPath[] xpathAll = new XPath[1];

    static {
        xpathAll[0] = new XPath( "//child::text()", null );

        summaryElements.add( "applicationSchemaInfo" );
        summaryElements.add( "contentInfo" );
        summaryElements.add( "dataSetURI" );
        summaryElements.add( "describes" );
        summaryElements.add( "featureAttribute" );
        summaryElements.add( "featureType" );
        summaryElements.add( "locale" );
        summaryElements.add( "metadataConstraints" );
        summaryElements.add( "metadataExtensionInfo" );
        summaryElements.add( "metadataMaintenance" );
        summaryElements.add( "portrayalCatalogueInfo" );
        summaryElements.add( "propertyType" );
        summaryElements.add( "series" );
        summaryElements.add( "spatialRepresentationInfo" );

        briefElements.add( "characterSet" );
        briefElements.add( "dataQualityInfo" );
        briefElements.add( "distributionInfo" );
        briefElements.add( "hierarchyLevelName" );
        briefElements.add( "language" );
        briefElements.add( "metadataStandardName" );
        briefElements.add( "metadataStandardVersion" );
        briefElements.add( "parentIdentifier" );
        briefElements.add( "referenceSystemInfo" );
    }

    public ISORecord( XMLStreamReader xmlStream ) {
        this.root = new XMLAdapter( xmlStream ).getRootElement();
        this.pElem = new ISOQPParsing().parseAPISO( root );
        root.declareDefaultNamespace( "http://www.isotc211.org/2005/gmd" );
    }

    public ISORecord( OMElement root ) {
        this( root.getXMLStreamReader() );
    }

    @Override
    public QName getName() {
        return root.getQName();
    }

    @Override
    public boolean eval( Filter filter ) {
        throw new UnsupportedOperationException( "In-memory filter evaluation not implemented yet." );
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

    public String getAnyText( AnyText anyText ) {
        String anyTextString = null;
        if ( anyText == null || anyText.getCore() != null ) {
            StringBuilder sb = new StringBuilder();

            for ( String s : getAbstract() ) {
                sb.append( s ).append( STOPWORD );
            }
            for ( String f : getFormat() ) {
                sb.append( f ).append( STOPWORD );
            }
            if ( getIdentifier() != null ) {
                sb.append( getIdentifier() ).append( STOPWORD );
            }
            if ( getLanguage() != null ) {
                sb.append( getLanguage() ).append( STOPWORD );
            }
            if ( getModified() != null ) {
                sb.append( getModified().getDate() ).append( STOPWORD );
            }
            for ( String f : getRelation() ) {
                sb.append( f ).append( STOPWORD );
            }
            for ( String f : getTitle() ) {
                sb.append( f ).append( STOPWORD );
            }
            if ( getType() != null ) {
                sb.append( getType() ).append( STOPWORD );
            }
            for ( String f : getSubject() ) {
                sb.append( f ).append( STOPWORD );
            }
            sb.append( isHasSecurityConstraints() ).append( STOPWORD );
            for ( String f : getRights() ) {
                sb.append( f ).append( STOPWORD );
            }
            if ( getContributor() != null ) {
                sb.append( getContributor() ).append( STOPWORD );
            }
            if ( getPublisher() != null ) {
                sb.append( getPublisher() ).append( STOPWORD );
            }
            if ( getSource() != null ) {
                sb.append( getSource() ).append( STOPWORD );
            }
            if ( getCreator() != null ) {
                sb.append( getCreator() ).append( STOPWORD );
            }
            if ( getParentIdentifier() != null ) {
                sb.append( getParentIdentifier() ).append( STOPWORD );
            }
            anyTextString = sb.toString();
        } else if ( anyText.getAll() != null ) {
            StringBuilder sb = new StringBuilder();
            try {
                XMLStreamReader xmlStream = getAsXMLStream();
                while ( xmlStream.hasNext() ) {
                    xmlStream.next();
                    if ( xmlStream.getEventType() == XMLStreamConstants.CHARACTERS && !xmlStream.isWhiteSpace() ) {
                        sb.append( xmlStream.getText() ).append( STOPWORD );
                    }
                }
            } catch ( XMLStreamException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            anyTextString = sb.toString();

        } else if ( anyText.getCustom() != null ) {
            List<String> xpathList = anyText.getCustom().getXPath();
            if ( xpathList != null && !xpathList.isEmpty() ) {
                XPath[] path = new XPath[xpathList.size()];
                int counter = 0;
                for ( String x : xpathList ) {
                    path[counter++] = new XPath( x, ns );
                }
                anyTextString = generateAnyText( path ).toString();
            }
        } else {
            anyTextString = "";
        }
        return anyTextString;

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
            ICRS crs = CRSManager.getCRSRef( bboxCRS.toString() );
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
    public String getIdentifier() {
        return pElem.getQueryableProperties().getIdentifier();
    }

    @Override
    public Date getModified() {
        return pElem.getQueryableProperties().getModified();
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
        root.declareDefaultNamespace( "http://www.isotc211.org/2005/gmd" );
        XMLStreamReader xmlStream = root.getXMLStreamReader();
        StAXParsingHelper.skipStartDocument( xmlStream );
        return xmlStream;
    }

    @Override
    public OMElement getAsOMElement() {
        return root;
    }

    public byte[] getAsByteArray()
                            throws FactoryConfigurationError {
        // XMLStreamReader reader = new WhitespaceElementFilter( getAsXMLStream() );
        // XMLStreamReader reader = getAsXMLStream();
        // XMLStreamWriter writer = null;
        // try {
        // writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
        // new FileOutputStream(
        // "/home/thomas/Desktop/ztest.xml" ) );
        // } catch ( FileNotFoundException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // generateOutput( writer, reader );
        root.declareDefaultNamespace( "http://www.isotc211.org/2005/gmd" );
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream( 20000 );
            root.serialize( out );
            out.close();
            return out.toByteArray();
        } catch ( XMLStreamException e ) {
            return root.toString().getBytes();
        } catch ( IOException e ) {
            return root.toString().getBytes();
        }

    }

    @Override
    public void serialize( XMLStreamWriter writer, ReturnableElement returnType )
                            throws XMLStreamException {
        switch ( returnType ) {
        case brief:
            serialize( writer, briefElements );
            break;
        case summary:
            serialize( writer, summaryElements );
            break;
        case full:
            XMLStreamReader inStream = root.getXMLStreamReader();
            StAXParsingHelper.skipStartDocument( inStream );
            XMLAdapter.writeElement( writer, inStream );
            break;
        default:
            throw new IllegalArgumentException( "Unexpected return type '" + returnType + "'." );
        }
    }

    @Override
    public void serialize( XMLStreamWriter writer, String[] elementNames )
                            throws XMLStreamException {
        Set<String> elSet = new HashSet<String>();
        for ( String s : elementNames ) {
            elSet.add( s );
        }
        serialize( writer, elSet );
    }

    private void serialize( XMLStreamWriter writer, Set<String> includeElements )
                            throws XMLStreamException {
        XMLStreamReader inStream = root.getXMLStreamReader();
        StAXParsingHelper.skipStartDocument( inStream );
        if ( inStream.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            throw new XMLStreamException( "Input stream does not point to a START_ELEMENT event." );
        }

        if ( inStream.getNamespaceURI() == NULL_NS_URI
             && ( inStream.getPrefix() == DEFAULT_NS_PREFIX || inStream.getPrefix() == null ) ) {
            writer.writeStartElement( inStream.getLocalName() );
        } else {
            if ( inStream.getPrefix() != null
                 && writer.getNamespaceContext().getPrefix( inStream.getPrefix() ) == XMLConstants.NULL_NS_URI ) {
                // TODO handle special cases for prefix binding, see
                // http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
                writer.setPrefix( inStream.getPrefix(), inStream.getNamespaceURI() );
            }
            writer.writeStartElement( inStream.getPrefix(), inStream.getLocalName(), inStream.getNamespaceURI() );
        }

        // copy namespace bindings
        for ( int i = 0; i < inStream.getNamespaceCount(); i++ ) {
            String nsPrefix = inStream.getNamespacePrefix( i );
            String nsURI = inStream.getNamespaceURI( i );
            writer.writeNamespace( nsPrefix, nsURI );
        }

        // copy all attributes
        for ( int i = 0; i < inStream.getAttributeCount(); i++ ) {
            String localName = inStream.getAttributeLocalName( i );
            String nsPrefix = inStream.getAttributePrefix( i );
            String value = inStream.getAttributeValue( i );
            String nsURI = inStream.getAttributeNamespace( i );
            if ( nsURI == null ) {
                writer.writeAttribute( localName, value );
            } else {
                writer.writeAttribute( nsPrefix, nsURI, localName, value );
            }
        }

        while ( inStream.next() != END_ELEMENT ) {
            if ( inStream.isStartElement() ) {
                QName elName = inStream.getName();
                // TODO should namespace be considered?
                if ( includeElements.contains( elName.getLocalPart() ) ) {
                    XMLAdapter.writeElement( writer, inStream );
                } else {
                    StAXParsingHelper.skipElement( inStream );
                }
            }
        }

        writer.writeEndElement();
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

    public String getStringFromXPath( XPath xpath ) {
        return new XMLAdapter().getNodeAsString( root, xpath, null );
    }

    public OMElement getNodeFromXPath( XPath xpath ) {
        return new XMLAdapter().getElement( root, xpath );
    }

    private StringBuilder generateAnyText( XPath[] xpath ) {
        StringBuilder sb = new StringBuilder();
        List<String> textNodes = new ArrayList<String>();

        for ( XPath x : xpath ) {

            String[] tmp = new XMLAdapter().getNodesAsStrings( root, x );
            for ( String s : tmp ) {
                textNodes.add( s );
            }
        }
        for ( String s : textNodes ) {
            sb.append( s ).append( STOPWORD );
        }

        return sb;
    }

    public void update( PropertyName propName, String s ) {
        AXIOMXPath path;
        Object node;
        try {
            path = getAsXPath( propName );
            node = path.selectSingleNode( root );
        } catch ( JaxenException e ) {
            String msg = "Could not propName as xPath and locate in in the record: " + propName;
            LOG.debug( msg, e );
            throw new InvalidParameterException( msg );
        }
        if ( node == null ) {
            String msg = "Could not find node with xPath: " + path;
            LOG.debug( msg );
            throw new InvalidParameterException( msg );
        } else if ( ( !( node instanceof OMElement ) ) ) {
            String msg = "Xpath + " + path + " does not adress a Node!";
            LOG.debug( msg );
            throw new InvalidParameterException( msg );
        }
        OMElement el = (OMElement) node;
        el.setText( s );
    }

    public void update( PropertyName propName, OMElement newEl ) {
        AXIOMXPath path;
        Object rootNode;
        try {
            path = getAsXPath( propName );
            rootNode = path.selectSingleNode( root );
        } catch ( JaxenException e ) {
            String msg = "Could not propName as xPath and locate in in the record: " + propName;
            LOG.debug( msg, e );
            throw new InvalidParameterException( msg );
        }
        if ( rootNode == null ) {
            String msg = "Could not find node with xPath: " + path;
            LOG.debug( msg );
            throw new InvalidParameterException( msg );
        } else if ( ( !( rootNode instanceof OMElement ) ) ) {
            String msg = "Xpath + " + path + " does not adress a Node!";
            LOG.debug( msg );
            throw new InvalidParameterException( msg );
        }

        OMElement rootEl = (OMElement) rootNode;
        OMNode prevSib = null;

        // replace them
        Iterator<?> childs = rootEl.getChildrenWithName( newEl.getQName() );
        while ( childs.hasNext() ) {
            Object next = childs.next();
            if ( next instanceof OMElement ) {
                prevSib = ( (OMElement) next ).getPreviousOMSibling();
                ( (OMElement) next ).detach();
            }
        }
        prevSib.insertSiblingAfter( newEl );
    }

    public void removeNode( PropertyName propName ) {
        AXIOMXPath path;
        Object rootNode;
        try {
            path = getAsXPath( propName );
            rootNode = path.selectSingleNode( root );
        } catch ( JaxenException e ) {
            String msg = "Could not propName as xPath and locate in in the record: " + propName;
            LOG.debug( msg, e );
            throw new InvalidParameterException( msg );
        }
        if ( rootNode == null ) {
            String msg = "Could not find node with xPath: " + path;
            LOG.debug( msg );
            throw new InvalidParameterException( msg );
        } else if ( ( !( rootNode instanceof OMElement ) ) ) {
            String msg = "Xpath + " + path + " does not adress a Node!";
            LOG.debug( msg );
            throw new InvalidParameterException( msg );
        }
        OMElement rootEl = (OMElement) rootNode;
        rootEl.detach();
    }

    private AXIOMXPath getAsXPath( PropertyName propName )
                            throws JaxenException {
        AXIOMXPath path;
        XPath xPathFromCQP = ISOCQPMapping.getXPathFromCQP( propName.getAsQName(), getType() );
        if ( xPathFromCQP != null )
            path = new AXIOMXPath( xPathFromCQP.getXPath() );
        else
            path = new AXIOMXPath( propName.getAsText() );
        path.setNamespaceContext( ns );
        return path;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }
}