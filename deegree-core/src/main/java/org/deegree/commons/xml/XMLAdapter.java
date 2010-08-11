// $HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.commons.xml;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.i18n.Messages;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.stax.XMLStreamReaderDoc;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>XMLAdapter</code> is the common base class of all hand-written (i.e. not automatically generated) XML parsers
 * and exporters in deegree. Classes that extend <code>XMLAdapter</code> provide the binding between a certain type of
 * XML documents and their corresponding Java bean representation.
 * <p>
 * <code>XMLAdapter</code> tries to make the process of writing custom XML parsers as painless as possible. It provides
 * the following functionality:
 * <ul>
 * <li>Lookup of nodes using XPath expressions.</li>
 * <li>Lookup of <i>required</i> nodes. These methods throw an {@link XMLParsingException} if the expression does not
 * have a result.</li>
 * <li>Convenient retrieving of node values as Java primitives (<code>int</code>, <code>boolean</code>, ...) or common
 * Objects (<code>QName</code>, <code>SimpleLink</code>, ...). If the value can not be converted to the expected type,
 * an {@link XMLParsingException} is thrown.
 * <li>Loading the XML content from different sources (<code>URL</code>, <code>Reader</code>, <code>InputStream</code>).
 * </li>
 * <li>Resolving of relative URLs that occur in the document content, i.e. that refer to resources that are located
 * relative to the document.</li>
 * </ul>
 * </p>
 * <p>
 * Technically, the XML handling is based on <a href="http://ws.apache.org/commons/axiom/">AXIOM (AXis Object
 * Model)</a>.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( XMLAdapter.class );

    /**
     * The context
     */
    protected static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * the xlink namespace
     */
    protected static final String XLN_NS = CommonNamespaces.XLNNS;

    private QName SCHEMA_ATTRIBUTE_NAME = new QName( CommonNamespaces.XSINS, "schemaLocation" );

    /**
     * Use this URL as SystemID only if the document content cannot be pinpointed to a URL - in this case it may not use
     * any relative references!
     */
    public static final String DEFAULT_URL = "http://www.deegree.org/unknownLocation";

    /** Root element of the XML contents. */
    protected OMElement rootElement;

    // physical source of the element (used for resolving relative URLs in the document)
    private String systemId;

    /**
     * Creates a new <code>XMLAdapter</code> which is not bound to an XML element.
     */
    public XMLAdapter() {
        // nothing to do
    }

    /**
     * Creates a new <code>XMLAdapter</code> with the given OMElement as root element;
     * 
     * @param rootElement
     *            the root element of the xml adapter
     */
    public XMLAdapter( OMElement rootElement ) {
        this.rootElement = rootElement;
    }

    /**
     * Creates a new instance that loads its content from the given <code>URL</code>.
     * 
     * @param url
     *            source of the xml content
     * @throws XMLProcessingException
     */
    public XMLAdapter( URL url ) throws XMLProcessingException {
        load( url );
    }

    /**
     * Creates a new instance that loads its content from the given <code>File</code>.
     * 
     * @param file
     *            source of the xml content
     * @throws XMLProcessingException
     */
    public XMLAdapter( File file ) throws XMLProcessingException {
        if ( file != null ) {
            try {
                load( file.toURI().toURL() );
            } catch ( MalformedURLException e ) {
                throw new XMLProcessingException( e );
            }
        }
    }

    /**
     * Creates a new instance that loads its content from the given <code>StringReader</code> using the default url.
     * 
     * @param reader
     *            source of the xml content
     * 
     * @throws XMLProcessingException
     */
    public XMLAdapter( StringReader reader ) throws XMLProcessingException {
        load( reader, DEFAULT_URL );
    }

    /**
     * Creates a new instance that loads its content from the given <code>StringReader</code>.
     * 
     * @param reader
     *            source of the xml content
     * @param systemId
     *            this string should represent a URL that is related to the passed reader. If this URL is not available
     *            or unknown, the string should contain the value of XMLAdapter.DEFAULT_URL
     * 
     * @throws XMLProcessingException
     */
    public XMLAdapter( StringReader reader, String systemId ) throws XMLProcessingException {
        load( reader, systemId );
    }

    /**
     * Creates a new instance that loads its content from the given <code>InputStream</code> using the default url.
     * 
     * @param in
     *            source of the xml content
     * 
     * @throws XMLProcessingException
     */
    public XMLAdapter( InputStream in ) throws XMLProcessingException {
        load( in, DEFAULT_URL );
    }

    /**
     * Creates a new instance that loads its content from the given <code>InputStream</code>.
     * 
     * @param in
     *            source of the xml content
     * @param systemId
     *            this string should represent a URL that is related to the passed reader. If this URL is not available
     *            or unknown, the string should contain the value of XMLAdapter.DEFAULT_URL
     * 
     * @throws XMLProcessingException
     */
    public XMLAdapter( InputStream in, String systemId ) throws XMLProcessingException {
        load( in, systemId );
    }

    /**
     * Creates a new instance that wraps the submitted XML document.
     * 
     * @param doc
     *            xml content
     * @param systemId
     *            the URL that is the source of the passed doc. If this URL is not available or unknown, the string
     *            should contain the value of XMLFragment.DEFAULT_URL
     */
    public XMLAdapter( OMDocument doc, String systemId ) {
        this( doc.getOMDocumentElement(), systemId );
    }

    /**
     * Creates a new instance that wraps the given XML element.
     * 
     * @param rootElement
     *            xml content
     * @param systemId
     *            the URL that is the source of the passed doc. If this URL is not available or unknown, the string
     *            should contain the value of XMLFragment.DEFAULT_URL
     */
    public XMLAdapter( OMElement rootElement, String systemId ) {
        setRootElement( rootElement );
        setSystemId( systemId );
    }

    public XMLAdapter( XMLStreamReader xmlStream ) {
        load (xmlStream);
    }

    /**
     * Returns the systemId (the physical location of the wrapped XML content).
     * 
     * @return the systemId
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * Sets the systemId (the physical location of the wrapped XML content).
     * 
     * @param systemId
     *            systemId (physical location) to set
     */
    public void setSystemId( String systemId ) {
        this.systemId = systemId;
    }

    /**
     * Returns whether the wrapped XML element contains schema references.
     * 
     * @return true, if the element contains schema references, false otherwise
     */
    public boolean hasSchemas() {
        return rootElement.getAttribute( SCHEMA_ATTRIBUTE_NAME ) != null;
    }

    /**
     * Determines the namespace <code>URI</code>s and the bound schema <code>URL</code>s from the 'xsi:schemaLocation'
     * attribute of the wrapped XML element.
     * 
     * @return keys are URIs (namespaces), values are URLs (schema locations)
     * @throws XMLProcessingException
     */
    public Map<String, URL> getSchemas()
                            throws XMLProcessingException {

        Map<String, URL> schemaMap = new HashMap<String, URL>();

        OMAttribute schemaLocationAttr = rootElement.getAttribute( SCHEMA_ATTRIBUTE_NAME );
        if ( schemaLocationAttr == null ) {
            return schemaMap;
        }

        String target = schemaLocationAttr.getAttributeValue();
        StringTokenizer tokenizer = new StringTokenizer( target );

        while ( tokenizer.hasMoreTokens() ) {
            URI nsURI = null;
            String token = tokenizer.nextToken();
            try {
                nsURI = new URI( token );
            } catch ( URISyntaxException e ) {
                String msg = "Invalid 'xsi:schemaLocation' attribute: namespace " + token + "' is not a valid URI.";
                LOG.error( msg );
                throw new XMLProcessingException( msg );
            }

            URL schemaURL = null;
            try {
                token = tokenizer.nextToken();
                schemaURL = resolve( token );
            } catch ( NoSuchElementException e ) {
                String msg = "Invalid 'xsi:schemaLocation' attribute: namespace '" + nsURI
                             + "' is missing a schema URL.";
                LOG.error( msg );
                throw new XMLProcessingException( msg );
            } catch ( MalformedURLException ex ) {
                String msg = "Invalid 'xsi:schemaLocation' attribute: '" + token + "' for namespace '" + nsURI
                             + "' could not be parsed as URL.";
                throw new XMLProcessingException( msg );
            }
            schemaMap.put( token, schemaURL );
        }
        return schemaMap;
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>URL</code>. Sets the SystemId,
     * too.
     * 
     * @param url
     *            source of the xml content
     * @throws XMLProcessingException
     */
    public void load( URL url )
                            throws XMLProcessingException {
        if ( url == null ) {
            throw new IllegalArgumentException( "The given url may not be null" );
        }
        try {
            load( url.openStream(), url.toExternalForm() );
        } catch ( IOException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        }
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>InputStream</code>. Sets the
     * SystemId, too.
     * 
     * @param istream
     *            source of the xml content
     * @param systemId
     *            cannot be null. This string should represent a URL that is related to the passed istream. If this URL
     *            is not available or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     * 
     * @throws XMLProcessingException
     */
    public void load( InputStream istream, String systemId )
                            throws XMLProcessingException {
        if ( istream != null ) {

            // TODO evaluate correct encoding handling

            // PushbackInputStream pbis = new PushbackInputStream( istream, 1024 );
            // String encoding = determineEncoding( pbis );
            //
            // InputStreamReader isr;
            // try {
            // isr = new InputStreamReader( pbis, encoding );
            // } catch ( UnsupportedEncodingException e ) {
            // throw new XMLProcessingException( e.getMessage(), e );
            // }
            //
            setSystemId( systemId );

            // TODO the code below is used, because constructing from Reader causes an error if the
            // document contains a DOCTYPE definition

            try {
                StAXOMBuilder builder = new StAXOMBuilder( istream );
                rootElement = builder.getDocumentElement();
            } catch ( XMLStreamException e ) {
                throw new XMLProcessingException( e.getMessage(), e );
            }

            // load( isr, systemId );
        } else {
            throw new NullPointerException( "The stream may not be null." );
        }
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>InputStream</code> and sets the
     * system id to the {@link #DEFAULT_URL}
     * 
     * @param resourceStream
     *            to load the xml from.
     * @throws XMLProcessingException
     */
    public void load( XMLStreamReader xmlStream )
                            throws XMLProcessingException {
        if ( xmlStream.getEventType() != XMLStreamConstants.START_DOCUMENT ) {
            setRootElement( new StAXOMBuilder( new XMLStreamReaderDoc( xmlStream ) ).getDocumentElement() );
        } else {
            setRootElement( new StAXOMBuilder( xmlStream ).getDocumentElement() );
        }
        if ( xmlStream.getLocation() != null && xmlStream.getLocation().getSystemId() != null ) {
            setSystemId( xmlStream.getLocation().getSystemId() );
        }
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>InputStream</code> and sets the
     * system id to the {@link #DEFAULT_URL}
     * 
     * @param resourceStream
     *            to load the xml from.
     * @throws XMLProcessingException
     */
    public void load( InputStream resourceStream )
                            throws XMLProcessingException {
        load( resourceStream, DEFAULT_URL );
    }

    /**
     * Determines the encoding of an XML document from its header. If no header available
     * <code>CharsetUtils.getSystemCharset()</code> will be returned
     * 
     * @param pbis
     * @return encoding of a XML document
     * @throws XMLProcessingException
     */
    private String determineEncoding( PushbackInputStream pbis )
                            throws XMLProcessingException {
        try {
            byte[] b = new byte[80];
            int rd = pbis.read( b );

            // TODO think about this
            String encoding = "UTF-8";
            if ( rd > 0 ) {
                String s = new String( b ).toLowerCase();

                if ( s.indexOf( "?>" ) > -1 ) {
                    int p = s.indexOf( "encoding=" );
                    if ( p > -1 ) {
                        StringBuffer sb = new StringBuffer();
                        int k = p + 1 + "encoding=".length();
                        while ( s.charAt( k ) != '"' && s.charAt( k ) != '\'' ) {
                            sb.append( s.charAt( k++ ) );
                        }
                        encoding = sb.toString();
                    }
                }
                pbis.unread( b, 0, rd );
            }
            return encoding;
        } catch ( IOException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        }
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>StringReader</code>. Sets the
     * SystemId, too.
     * 
     * @param reader
     *            source of the XML content
     * @param systemId
     *            can not be null. This string should represent a URL that is related to the passed reader. If this URL
     *            is not available or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     * 
     * @throws XMLProcessingException
     */
    public void load( StringReader reader, String systemId )
                            throws XMLProcessingException {
        try {
            if ( systemId == null ) {
                throw new NullPointerException( "'systemId' must not be null!" );
            }
            setSystemId( systemId );

            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader( reader );
            StAXOMBuilder builder = new StAXOMBuilder( parser );
            rootElement = builder.getDocumentElement();
        } catch ( XMLStreamException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        } catch ( OMException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        } catch ( FactoryConfigurationError e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        }
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>StringReader</code> and sets the
     * system id to the {@link #DEFAULT_URL}
     * 
     * @param reader
     *            to load the xml from.
     * @throws XMLProcessingException
     */
    public void load( StringReader reader )
                            throws XMLProcessingException {
        load( reader, DEFAULT_URL );
    }

    /**
     * Sets the root element, i.e. the XML element encapsulated by this <code>XMLAdapter</code>.
     * 
     * @param rootElement
     *            the root element
     */
    public void setRootElement( OMElement rootElement ) {
        this.rootElement = rootElement;
    }

    /**
     * Returns the root element, i.e. the XML element encapsulated by this <code>XMLAdapter</code>.
     * 
     * @return the root element
     */
    public OMElement getRootElement() {
        return rootElement;
    }

    /**
     * Resolves the given URL (which may be relative) against the SystemID of this <code>XMLAdapter</code> into an
     * absolute <code>URL</code>.
     * 
     * @param url
     *            <code>URL</code> to be resolved (may be relative or absolute)
     * @return the resolved URL
     * @throws MalformedURLException
     */
    public URL resolve( String url )
                            throws MalformedURLException {

        LOG.debug( "Resolving URL '" + url + "' against SystemID '" + systemId + "'." );

        // check if url is an absolute path
        File file = new File( url );
        if ( file.isAbsolute() ) {
            return file.toURI().toURL();
        }

        // TODO this is not really nice, also think about handling url specs here
        URL resolvedURL = new URL( new URL( systemId ), url.replace( " ", "%20" ) );
        LOG.debug( "-> resolvedURL: '" + resolvedURL + "'" );
        return resolvedURL;
    }

    public Object evaluateXPath( XPath xpath, Object context )
                            throws XMLProcessingException {
        Object result;
        try {
            result = getAXIOMXPath( xpath ).evaluate( context );
        } catch ( JaxenException e ) {
            throw new XMLProcessingException( e.getMessage() );
        }
        return result;
    }

    /**
     * Parses the submitted XML element as a {@link SimpleLink}.
     * <p>
     * Possible escaping of the attributes "xlink:href", "xlink:role" and "xlink:arcrole" is performed automatically.
     * </p>
     * 
     * @param element
     * @return the object representation of the element
     * @throws XMLParsingException
     */
    public SimpleLink parseSimpleLink( OMElement element )
                            throws XMLParsingException {

        URI href = null;
        URI role = null;
        URI arcrole = null;
        String title = null;
        String show = null;
        String actuate = null;

        String uriString = null;
        try {
            uriString = element.getAttributeValue( new QName( CommonNamespaces.XLNNS, "href" ) );
            if ( uriString != null ) {
                href = new URI( null, uriString, null );
            }
            uriString = element.getAttributeValue( new QName( CommonNamespaces.XLNNS, "role" ) );
            if ( uriString != null ) {
                role = new URI( null, uriString, null );
            }
            uriString = element.getAttributeValue( new QName( CommonNamespaces.XLNNS, "arcrole" ) );
            if ( uriString != null ) {
                arcrole = new URI( null, uriString, null );
            }
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( this, element, "'" + uriString + "' is not a valid URI." );
        }

        return new SimpleLink( href, role, arcrole, title, show, actuate );
    }

    /**
     * Parses the given <code>String</code> as an <code>xsd:boolean</code> value.
     * 
     * @param s
     *            the <code>String</code> to be parsed
     * @return corresponding boolean value
     * @throws XMLParsingException
     *             if the given <code>String</code> is not a valid instance of <code>xsd:boolean</code>
     */
    public boolean parseBoolean( String s )
                            throws XMLParsingException {

        boolean value = false;
        if ( "true".equals( s ) || "1".equals( s ) ) {
            value = true;
        } else if ( "false".equals( s ) || "0".equals( s ) ) {
            value = false;
        } else {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_BOOLEAN", s );
            throw new XMLParsingException( this, (OMElement) null, msg );
        }
        return value;
    }

    /**
     * Parses the given <code>String</code> as an <code>xsd:double</code> value.
     * 
     * @param s
     *            the <code>String</code> to be parsed
     * @return corresponding double value
     * @throws XMLParsingException
     *             if the given <code>String</code> is not a valid instance of <code>xsd:double</code>
     */
    public double parseDouble( String s )
                            throws XMLParsingException {

        double value = 0.0;
        try {
            value = Double.parseDouble( s );
        } catch ( NumberFormatException e ) {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_DOUBLE", s );
            throw new XMLParsingException( this, (OMElement) null, msg );
        }
        return value;
    }

    /**
     * Parses the given <code>String</code> as an <code>xsd:float</code> value.
     * 
     * @param s
     *            the <code>String</code> to be parsed
     * @return corresponding float value
     * @throws XMLParsingException
     *             if the given <code>String</code> is not a valid instance of <code>xsd:float</code>
     */
    public float parseFloat( String s )
                            throws XMLParsingException {

        float value = 0.0f;
        try {
            value = Float.parseFloat( s );
        } catch ( NumberFormatException e ) {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_FLOAT", s );
            throw new XMLParsingException( this, (OMElement) null, msg );
        }
        return value;
    }

    /**
     * Parses the given <code>String</code> as an <code>xsd:integer</code> value.
     * 
     * @param s
     *            the <code>String</code> to be parsed
     * @return corresponding integer value
     * @throws XMLParsingException
     *             if the given <code>String</code> is not a valid instance of <code>xsd:integer</code>
     */
    public int parseInt( String s )
                            throws XMLParsingException {

        int value = 0;
        try {
            value = Integer.parseInt( s );
        } catch ( NumberFormatException e ) {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_INT", s );
            throw new XMLParsingException( this, (OMElement) null, msg );
        }
        return value;
    }

    /**
     * Parses the given <code>String</code> as an {@link URL}.
     * 
     * @param s
     *            the <code>String</code> to be parsed
     * @return corresponding URL value
     * @throws XMLParsingException
     *             if the given <code>String</code> is not a valid {@link URL}
     */
    public URL parseURL( String s )
                            throws XMLParsingException {

        URL value = null;
        try {
            value = new URL( s );
        } catch ( MalformedURLException e ) {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_URL", s );
            throw new XMLParsingException( this, (OMElement) null, msg );
        }
        return value;
    }

    /**
     * Parses the given <code>String</code> as an <code>xsd:QName</code> value.
     * 
     * @param s
     *            the <code>String</code> to be parsed
     * @param element
     *            element that provides the namespace context (used to resolve the namespace prefix)
     * @return corresponding QName value
     * @throws XMLParsingException
     *             if the given <code>String</code> is not a valid instance of <code>xsd:QName</code>
     */
    public QName parseQName( String s, OMElement element )
                            throws XMLParsingException {

        QName value = element.resolveQName( s );
        return value;
    }

    public OMElement getElement( OMElement context, XPath xpath )
                            throws XMLParsingException {
        Object result = getNode( context, xpath );
        if ( result == null ) {
            return null;
        }
        if ( !( result instanceof OMElement ) ) {
            String msg = Messages.getMessage( "XML_PARSING_ERROR_NOT_ELEMENT", xpath, context, result.getClass() );
            throw new XMLParsingException( this, context, msg );
        }
        return (OMElement) result;
    }

    @SuppressWarnings("unchecked")
    public List<OMElement> getElements( OMElement context, XPath xpath )
                            throws XMLParsingException {
        return getNodes( context, xpath );
    }

    // TODO Should we consider changing OMElement in OMNode for getNode* methods?
    public Object getNode( OMElement context, XPath xpath )
                            throws XMLParsingException {
        Object node;
        try {
            node = getAXIOMXPath( xpath ).selectSingleNode( context );
        } catch ( JaxenException e ) {
            throw new XMLParsingException( this, context, e.getMessage() );
        }
        return node;
    }

    public boolean getNodeAsBoolean( OMElement context, XPath xpath, boolean defaultValue )
                            throws XMLParsingException {

        boolean value = defaultValue;
        String s = getNodeAsString( context, xpath, null );
        if ( s != null ) {
            value = parseBoolean( s );
        }
        return value;
    }

    public double getNodeAsDouble( OMElement context, XPath xpath, double defaultValue )
                            throws XMLParsingException {

        double value = defaultValue;
        String s = getNodeAsString( context, xpath, null );
        if ( s != null ) {
            value = parseDouble( s );
        }
        return value;
    }

    public float getNodeAsFloat( OMElement context, XPath xpath, float defaultValue )
                            throws XMLParsingException {

        float value = defaultValue;
        String s = getNodeAsString( context, xpath, null );
        if ( s != null ) {
            value = parseFloat( s );
        }
        return value;
    }

    public int getNodeAsInt( OMElement context, XPath xpath, int defaultValue )
                            throws XMLParsingException {

        int value = defaultValue;
        String s = getNodeAsString( context, xpath, null );
        if ( s != null ) {
            value = parseInt( s );
        }
        return value;
    }

    public URL getNodeAsURL( OMElement context, XPath xpath, URL defaultValue )
                            throws XMLParsingException {
        URL value = defaultValue;
        String s = getNodeAsString( context, xpath, null );
        if ( s != null ) {
            value = parseURL( s );
        }
        return value;
    }

    public QName getNodeAsQName( OMElement context, XPath xpath, QName defaultValue )
                            throws XMLParsingException {

        QName value = defaultValue;
        Object node = getNode( context, xpath );
        if ( node != null ) {
            if ( node instanceof OMText ) {
                value = ( (OMText) node ).getTextAsQName();
            } else if ( node instanceof OMElement ) {
                OMElement element = (OMElement) node;
                value = element.resolveQName( element.getText() );
            } else if ( node instanceof OMAttribute ) {
                OMAttribute attribute = (OMAttribute) node;
                value = attribute.getOwner().resolveQName( attribute.getAttributeValue() );
            } else {
                String msg = "Unexpected node type '" + node.getClass() + "'.";
                throw new XMLParsingException( this, context, msg );
            }
        }
        return value;
    }

    public String getNodeAsString( OMElement context, XPath xpath, String defaultValue )
                            throws XMLParsingException {
        String value = defaultValue;
        Object node = getNode( context, xpath );
        if ( node != null ) {
            try {
                if ( node instanceof OMText ) {
                    value = ( (OMText) node ).getText();
                } else if ( node instanceof OMElement ) {
                    value = ( (OMElement) node ).getText();
                } else if ( node instanceof OMAttribute ) {
                    value = ( (OMAttribute) node ).getAttributeValue();
                } else {
                    String msg = "Unexpected node type '" + node.getClass() + "'.";
                    throw new XMLParsingException( this, context, msg );
                }
            } catch ( OMException ex ) {
                String msg = "Internal error while accessing node '" + ex.getMessage() + "'.";
                throw new XMLParsingException( this, context, msg );
            }
        }
        return value;
    }

    public Version getNodeAsVersion( OMElement context, XPath xpath, Version defaultValue )
                            throws XMLParsingException {
        Version value = defaultValue;
        String s = getNodeAsString( context, xpath, null );
        if ( s != null ) {
            value = value.parseVersion( s );
        }
        return value;

    }

    public List getNodes( OMElement context, XPath xpath )
                            throws XMLParsingException {
        List<?> nodes;
        try {
            nodes = getAXIOMXPath( xpath ).selectNodes( context );
        } catch ( JaxenException e ) {
            throw new XMLParsingException( this, context, e.getMessage() );
        }
        return nodes;
    }

    public String[] getNodesAsStrings( OMElement contextNode, XPath xpath ) {
        String[] values = null;
        List<?> nl = getNodes( contextNode, xpath );
        if ( nl != null ) {
            values = new String[nl.size()];
            for ( int i = 0; i < nl.size(); i++ ) {
                Object node = nl.get( i );
                String value = null;
                if ( node != null ) {
                    try {
                        if ( node instanceof OMText ) {
                            value = ( (OMText) node ).getText();
                        } else if ( node instanceof OMElement ) {
                            value = ( (OMElement) node ).getText();
                        } else if ( node instanceof OMAttribute ) {
                            value = ( (OMAttribute) node ).getAttributeValue();
                        } else {
                            String msg = "Unexpected node type '" + node.getClass() + "'.";
                            throw new XMLParsingException( this, contextNode, msg );
                        }
                    } catch ( OMException ex ) {
                        String msg = "Internal error while accessing node '" + ex.getMessage() + "'.";
                        throw new XMLParsingException( this, contextNode, msg );
                    }
                }
                values[i] = value;
            }
        } else {
            values = new String[0];
        }
        return values;
    }

    public QName[] getNodesAsQNames( OMElement contextNode, XPath xpath ) {
        QName[] values = null;
        List<?> nl = getNodes( contextNode, xpath );
        if ( nl != null ) {
            values = new QName[nl.size()];
            for ( int i = 0; i < nl.size(); i++ ) {
                Object node = nl.get( i );
                QName value = null;
                if ( node instanceof OMText ) {
                    value = ( (OMText) node ).getTextAsQName();
                } else if ( node instanceof OMElement ) {
                    OMElement element = (OMElement) node;
                    value = element.resolveQName( element.getText() );
                } else if ( node instanceof OMAttribute ) {
                    OMAttribute attribute = (OMAttribute) node;
                    value = attribute.getOwner().resolveQName( attribute.getAttributeValue() );
                } else {
                    String msg = "Unexpected node type '" + node.getClass() + "'.";
                    throw new XMLParsingException( this, contextNode, msg );
                }
                values[i] = value;
            }
        } else {
            values = new QName[0];
        }
        return values;
    }

    public OMElement getRequiredElement( OMElement context, XPath xpath )
                            throws XMLParsingException {
        OMElement element = getElement( context, xpath );
        if ( element == null ) {
            String msg = Messages.getMessage( "XML_REQUIRED_ELEMENT_MISSING", xpath, context.getQName() );
            throw new XMLParsingException( this, context, msg );
        }
        return element;
    }

    public List<OMElement> getRequiredElements( OMElement context, XPath xpath )
                            throws XMLParsingException {
        List<OMElement> elements = getElements( context, xpath );
        if ( elements.size() == 0 ) {
            String msg = Messages.getMessage( "XML_REQUIRED_ELEMENT_MISSING", xpath, context.getQName() );
            throw new XMLParsingException( this, context, msg );
        }
        return elements;
    }

    public Object getRequiredNode( OMElement context, XPath xpath )
                            throws XMLParsingException {
        Object node = getNode( context, xpath );
        if ( node == null ) {
            String msg = Messages.getMessage( "XML_REQUIRED_NODE_MISSING", xpath, context.getQName() );
            throw new XMLParsingException( this, context, msg );
        }
        return node;
    }

    public boolean getRequiredNodeAsBoolean( OMElement context, XPath xpath )
                            throws XMLParsingException {
        String s = getRequiredNodeAsString( context, xpath );
        boolean value = parseBoolean( s );
        return value;
    }

    public double getRequiredNodeAsDouble( OMElement context, XPath xpath )
                            throws XMLParsingException {
        String s = getRequiredNodeAsString( context, xpath );
        double value = parseDouble( s );
        return value;
    }

    public float getRequiredNodeAsFloat( OMElement context, XPath xpath )
                            throws XMLParsingException {

        String s = getRequiredNodeAsString( context, xpath );
        float value = parseFloat( s );
        return value;
    }

    public int getRequiredNodeAsInteger( OMElement context, XPath xpath )
                            throws XMLParsingException {

        String s = getRequiredNodeAsString( context, xpath );
        int value = parseInt( s );
        return value;
    }

    public URL getRequiredNodeAsURL( OMElement context, XPath xpath )
                            throws XMLParsingException {

        String s = getRequiredNodeAsString( context, xpath );
        URL value = parseURL( s );
        return value;
    }

    public String getRequiredNodeAsString( OMElement context, XPath xpath )
                            throws XMLParsingException {

        String value = getNodeAsString( context, xpath, null );
        if ( value == null ) {
            String msg = Messages.getMessage( "XML_REQUIRED_NODE_MISSING", xpath, context.getQName() );
            throw new XMLParsingException( this, context, msg );
        }
        return value;
    }

    public QName getRequiredNodeAsQName( OMElement context, XPath xpath )
                            throws XMLParsingException {

        QName value = getNodeAsQName( context, xpath, null );
        if ( value == null ) {
            String msg = Messages.getMessage( "XML_REQUIRED_NODE_MISSING", xpath, context.getQName() );
            throw new XMLParsingException( this, context, msg );
        }
        return value;
    }

    public Version getRequiredNodeAsVersion( OMElement context, XPath xpath )
                            throws XMLParsingException {
        Version value = getNodeAsVersion( context, xpath, null );
        if ( value == null ) {
            String msg = Messages.getMessage( "XML_REQUIRED_NODE_MISSING", xpath, context.getQName() );
            throw new XMLParsingException( this, context, msg );
        }
        return value;
    }

    public List getRequiredNodes( OMElement context, XPath xpath )
                            throws XMLParsingException {
        List nodes = getNodes( context, xpath );
        if ( nodes.size() == 0 ) {

        }
        return nodes;
    }

    private AXIOMXPath getAXIOMXPath( XPath xpath )
                            throws JaxenException {
        AXIOMXPath compiledXPath = new AXIOMXPath( xpath.getXPath() );
        compiledXPath.setNamespaceContext( xpath.getNamespaceContext() );
        return compiledXPath;
    }

    /**
     * Constructs a {@link NamespaceContext} from all active namespace bindings available in the scope of the given
     * {@link OMElement}.
     * 
     * @param element
     *            the given element
     * @return the constructed namespace context
     */
    public NamespaceContext getNamespaceContext( OMElement element ) {

        NamespaceContext nsContext = new NamespaceContext();
        augmentNamespaceContext( element, nsContext );
        // System.out.println( nsContext );
        return nsContext;
    }

    @SuppressWarnings("unchecked")
    private void augmentNamespaceContext( OMElement element, NamespaceContext nsContext ) {
        Iterator<OMNamespace> iterator = element.getAllDeclaredNamespaces();
        while ( iterator.hasNext() ) {
            OMNamespace namespace = iterator.next();
            if ( nsContext.getURI( namespace.getPrefix() ) == null ) {
                nsContext.addNamespace( namespace.getPrefix(), namespace.getNamespaceURI() );
            }
        }
        OMContainer parent = element.getParent();
        if ( parent != null && parent instanceof OMElement ) {
            augmentNamespaceContext( (OMElement) parent, nsContext );
        }
    }

    /**
     * Write an element with simple text content into the XMLStream.
     * <p>
     * Convenience method to write simple elements like:
     * 
     * <pre>
     * &lt;ogc:GeometryOperand&gt;gml:Envelope&lt;/ogc:GeometryOperand&gt;
     * &lt;gml:upperCorner&gt;90 180&lt;/gml:upperCorner&gt;
     * </pre>
     * 
     * @param writer
     * @param namespace
     *            the namespace of the element
     * @param elemName
     *            the element name
     * @param value
     *            the text value of the element
     * @throws XMLStreamException
     */
    public static void writeElement( XMLStreamWriter writer, String namespace, String elemName, String value )
                            throws XMLStreamException {
        writer.writeStartElement( namespace, elemName );
        if ( value != null ) {
            writer.writeCharacters( value );
        }
        writer.writeEndElement();
    }

    /**
     * Write an element with simple text content and an attribute into the XMLStream.
     * <p>
     * Convenience method to write simple elements like:
     * 
     * <pre>
     * &lt;ogc:GeometryOperand name=&quot;env&quot;&gt;gml:Envelope&lt;/ogc:GeometryOperand&gt;
     * &lt;gml:upperCorner&gt;90 180&lt;/gml:upperCorner&gt;
     * </pre>
     * 
     * @param writer
     * @param namespace
     *            the namespace of the element
     * @param elemName
     *            the element name
     * @param value
     *            the text value of the element
     * @param attrNS
     *            the namespace of the attribute, <code>null</null> if the local namespace of the element should be used
     * @param attribPRE
     *            to use for the namespace binding
     * @param attrName
     *            the attribute name
     * @param attrValue
     *            the attribute value, if <code>null</code> the attribute will not be written.
     * @throws XMLStreamException
     */
    public static void writeElement( XMLStreamWriter writer, String namespace, String elemName, String value,
                                     String attrNS, String attribPRE, String attrName, String attrValue )
                            throws XMLStreamException {
        writer.writeStartElement( namespace, elemName );
        if ( attrValue != null ) {
            if ( attrNS == null ) {
                writer.writeAttribute( attrName, attrValue );
            } else {
                if ( attribPRE == null ) {
                    writer.writeAttribute( attrNS, attrName, attrValue );
                } else {
                    writer.writeAttribute( attribPRE, attrNS, attrName, attrValue );
                }
            }
        }
        if ( value != null ) {
            writer.writeCharacters( value );
        }
        writer.writeEndElement();
    }

    /**
     * Write an optional attribute at the current position of the writer. If the value is empty or <code>null</code> no
     * attribute will be written.
     * 
     * @param writer
     * @param name
     * @param value
     * @throws XMLStreamException
     */
    public static void writeOptionalAttribute( XMLStreamWriter writer, String name, String value )
                            throws XMLStreamException {
        if ( value != null && !"".equals( value ) ) {
            writer.writeAttribute( name, value );
        }
    }

    /**
     * Write an optional attribute at the current position of the writer. If the value is empty or <code>null</code> no
     * attribute will be written.
     * 
     * @param writer
     * @param namespace
     *            of the attribute
     * @param name
     *            of the attribute
     * @param value
     *            of the attribute might be <code>null</code>
     * @throws XMLStreamException
     */
    public static void writeOptionalNSAttribute( XMLStreamWriter writer, String namespace, String name, String value )
                            throws XMLStreamException {
        if ( value != null && !"".equals( value ) ) {
            writer.writeAttribute( namespace, name, value );
        }
    }

    /**
     * Write an element with a single attribute into the XMLStream.
     * 
     * <p>
     * Convenience method to write simple elements like:
     * 
     * <pre>
     * &lt;ows:Post xlink:href=&quot;http://localhost/&quot; /&gt;
     * &lt;ogc:TemporalOperator name=&quot;TM_Begins&quot; /&gt;
     * </pre>
     * 
     * @param writer
     * @param namespace
     *            the namespace of the element
     * @param elemName
     *            the element name
     * @param attrNS
     *            the namespace of the attribute, <code>null</null> if the local namespace of the element should be used
     * @param attrName
     *            the attribute name
     * @param attrValue
     *            the attribute value
     * @throws XMLStreamException
     */
    public static void writeElement( XMLStreamWriter writer, String namespace, String elemName, String attrNS,
                                     String attrName, String attrValue )
                            throws XMLStreamException {
        writer.writeStartElement( namespace, elemName );
        if ( attrNS == null ) {
            writer.writeAttribute( attrName, attrValue );
        } else {
            writer.writeAttribute( attrNS, attrName, attrValue );
        }
        writer.writeEndElement();
    }

    /**
     * Write an optional element with simple text content into the XMLStream. If the value is <code>null</code>, than
     * the element is omitted.
     * 
     * <p>
     * Convenience method to write simple elements like:
     * 
     * <pre>
     * &lt;ogc:GeometryOperand&gt;gml:Envelope&lt;/ogc:GeometryOperand&gt;
     * &lt;gml:upperCorner&gt;10 -42&lt;/gml:upperCorner&gt;
     * </pre>
     * 
     * @param writer
     * @param namespace
     *            the namespace of the element
     * @param elemName
     *            the element name
     * @param value
     *            the text value of the element
     * @throws XMLStreamException
     */
    public static void writeOptionalElement( XMLStreamWriter writer, String namespace, String elemName, String value )
                            throws XMLStreamException {
        if ( value != null ) {
            writer.writeStartElement( namespace, elemName );
            writer.writeCharacters( value );
            writer.writeEndElement();
        }
    }

    /**
     * Copies an XML element (including all attributes and subnodes) from an {@link XMLStreamReader} into the given
     * {@link XMLStreamWriter}.
     * 
     * @param writer
     *            {@link XMLStreamWriter} that the xml is appended to
     * @param inStream
     *            cursor must point at a <code>START_ELEMENT</code> event and points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @throws XMLStreamException
     */
    public static void writeElement( XMLStreamWriter writer, XMLStreamReader inStream )
                            throws XMLStreamException {

        if ( inStream.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            throw new XMLStreamException( "Input stream does not point to a START_ELEMENT event." );
        }
        int openElements = 0;
        boolean firstRun = true;
        while ( firstRun || openElements > 0 ) {
            firstRun = false;
            int eventType = inStream.getEventType();

            switch ( eventType ) {
            case CDATA: {
                writer.writeCData( inStream.getText() );
                break;
            }
            case CHARACTERS: {
                writer.writeCharacters( inStream.getTextCharacters(), inStream.getTextStart(), inStream.getTextLength() );
                break;
            }
            case END_ELEMENT: {
                writer.writeEndElement();
                openElements--;
                break;
            }
            case START_ELEMENT: {
                if ( inStream.getNamespaceURI() == NULL_NS_URI || inStream.getPrefix() == DEFAULT_NS_PREFIX ) {
                    writer.writeStartElement( inStream.getLocalName() );
                } else {
                    if ( writer.getNamespaceContext().getPrefix( inStream.getPrefix() ) == XMLConstants.NULL_NS_URI ) {
                        // TODO handle special cases for prefix binding, see
                        // http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
                        writer.setPrefix( inStream.getPrefix(), inStream.getNamespaceURI() );
                    }
                    writer.writeStartElement( inStream.getPrefix(), inStream.getLocalName(), inStream.getNamespaceURI() );
                }
                // copy all namespace bindings
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

                openElements++;
                break;
            }
            default: {
                break;
            }
            }
            if ( openElements > 0 ) {
                inStream.next();
            }
        }
    }

    /**
     * Writes an element without namespace, and with an (optional) text
     * 
     * @param writer
     * @param name
     * @param text
     * @throws XMLStreamException
     */
    public static void writeElement( XMLStreamWriter writer, String name, String text )
                            throws XMLStreamException {
        writer.writeStartElement( name );
        if ( text != null ) {
            writer.writeCharacters( text );
        }
        writer.writeEndElement();
    }

    /**
     * Writes an element without namespace, only if text not null
     * 
     * @param writer
     * @param name
     * @param text
     * @throws XMLStreamException
     */
    public static void maybeWriteElement( XMLStreamWriter writer, String name, String text )
                            throws XMLStreamException {
        if ( text != null ) {
            writer.writeStartElement( name );
            writer.writeCharacters( text );
            writer.writeEndElement();
        }
    }

    /**
     * Writes an element with namespace, only if text not null
     * 
     * @param writer
     * @param ns
     * @param name
     * @param text
     * @throws XMLStreamException
     */
    public static void maybeWriteElementNS( XMLStreamWriter writer, String ns, String name, String text )
                            throws XMLStreamException {
        if ( text != null ) {
            writer.writeStartElement( ns, name );
            writer.writeCharacters( text );
            writer.writeEndElement();
        }
    }

    @Override
    public String toString() {
        return rootElement == null ? "(no document)" : rootElement.toString();
    }
}
