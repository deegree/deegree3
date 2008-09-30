// $HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/

package org.deegree.commons.xml;

import static javax.xml.stream.XMLStreamConstants.ATTRIBUTE;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.i18n.Messages;
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
 * Technically, the XML handling is based on <a href="http://ws.apache.org/commons/axiom/">AXIOM (AXis Object Model)</a>
 * on top of <a href="http://jcp.org/en/jsr/detail?id=173">StAX (Streaming API for XML)</a>.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( XMLAdapter.class );

    protected static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    protected static final String XLN_NS = "http://www.w3.org/1999/xlink";

    protected static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    private QName SCHEMA_ATTRIBUTE_NAME = new QName( XSI_NS, "schemaLocation" );

    /**
     * Use this URL as SystemID only if the document content cannot be pinpointed to a URL - in this case it may not use
     * any relative references!
     */
    public static final String DEFAULT_URL = "http://www.deegree.org/unknownLocation";

    /** Root element of the XML contents. */
    protected OMElement rootElement;

    // physical source of the element (used for resolving relative URLs in the document)
    private URL systemId;

    /**
     * Creates a new <code>XMLAdapter</code> which is not bound to an XML element.
     */
    public XMLAdapter() {
        // nothing to do
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
        try {
            systemId = new URL( DEFAULT_URL );
        } catch ( MalformedURLException e ) {
            // but it's not malformed
        }
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
                throw new XMLProcessingException( e.getMessage(), e );
            }
        }
    }

    /**
     * Creates a new instance that loads its content from the given <code>Reader</code>.
     * 
     * @param reader
     *            source of the xml content
     * @param systemId
     *            this string should represent a URL that is related to the passed reader. If this URL is not available
     *            or unknown, the string should contain the value of XMLAdapter.DEFAULT_URL
     * 
     * @throws XMLProcessingException
     */
    public XMLAdapter( Reader reader, URL systemId ) throws XMLProcessingException {
        load( reader, systemId );
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
    public XMLAdapter( OMDocument doc, URL systemId ) {
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
    public XMLAdapter( OMElement rootElement, URL systemId ) {
        setRootElement( rootElement );
        setSystemId( systemId );
    }

    /**
     * Returns the systemId (the physical location of the wrapped XML content).
     * 
     * @return the systemId
     */
    public URL getSystemId() {
        return systemId;
    }

    /**
     * Sets the systemId (the physical location of the wrapped XML content).
     * 
     * @param systemId
     *            systemId (physical location) to set
     */
    public void setSystemId( URL systemId ) {
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
            load( url.openStream(), url );
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
    public void load( InputStream istream, URL systemId )
                            throws XMLProcessingException {

        PushbackInputStream pbis = new PushbackInputStream( istream, 1024 );
        String encoding = determineEncoding( pbis );

        InputStreamReader isr;
        try {
            isr = new InputStreamReader( pbis, encoding );
        } catch ( UnsupportedEncodingException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        }
        load( isr, systemId );
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
            String s = new String( b ).toLowerCase();

            // TODO think about this
            String encoding = "UTF-8";
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
            return encoding;
        } catch ( IOException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        }
    }

    /**
     * Initializes this <code>XMLAdapter</code> with the content from the given <code>Reader</code>. Sets the SystemId,
     * too.
     * 
     * @param reader
     *            source of the XML content
     * @param systemId
     *            can not be null. This string should represent a URL that is related to the passed reader. If this URL
     *            is not available or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     * 
     * @throws XMLProcessingException
     */
    public void load( Reader reader, URL systemId )
                            throws XMLProcessingException {
        try {
            PushbackReader pbr = new PushbackReader( reader, 1024 );
            int c = pbr.read();
            if ( c != 65279 && c != 65534 ) {
                // no BOM (byte order mark)! push char back into reader
                pbr.unread( c );
            }

            if ( systemId == null ) {
                throw new NullPointerException( "'systemId' must not be null!" );
            }
            setSystemId( systemId );

            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader( pbr );
            StAXOMBuilder builder = new StAXOMBuilder( parser );
            rootElement = builder.getDocumentElement();
        } catch ( IOException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        } catch ( XMLStreamException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        } catch ( OMException e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        } catch ( FactoryConfigurationError e ) {
            throw new XMLProcessingException( e.getMessage(), e );
        }
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

        URL resolvedURL = new URL( systemId, url );
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

    public OMElement getRequiredElement( OMElement context, XPath xpath )
                            throws XMLParsingException {
        OMElement element = getElement( context, xpath );
        if ( element == null ) {
            String msg = Messages.getMessage( "XML_REQUIRED_ELEMENT_MISSING", xpath, context );
            throw new XMLParsingException( this, context, msg );
        }
        return element;
    }

    public List<OMElement> getRequiredElements( OMElement context, XPath xpath )
                            throws XMLParsingException {
        List<OMElement> elements = getElements( context, xpath );
        if ( elements.size() == 0 ) {
            String msg = Messages.getMessage( "XML_REQUIRED_ELEMENT_MISSING", xpath, context );
            throw new XMLParsingException( this, context, msg );
        }
        return elements;
    }

    public Object getRequiredNode( OMElement context, XPath xpath )
                            throws XMLParsingException {
        Object node = getNode( context, xpath );
        if ( node == null ) {
            String msg = Messages.getMessage( "XML_REQUIRED_NODE_MISSING", xpath, context );
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

    public String getRequiredNodeAsString( OMElement context, XPath xpath )
                            throws XMLParsingException {

        String value = getNodeAsString( context, xpath, null );
        if ( value == null ) {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_NODE_MISSING", xpath, context );
            throw new XMLParsingException( this, context, msg );
        }
        return value;
    }

    public QName getRequiredNodeAsQName( OMElement context, XPath xpath )
                            throws XMLParsingException {

        QName value = getNodeAsQName( context, xpath, null );
        if ( value == null ) {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_NODE_MISSING", xpath, context );
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
        Map<String, String> nsMap = xpath.getNamespaceContext().getNamespaceMap();
        for ( String prefix : nsMap.keySet() ) {
            compiledXPath.addNamespace( prefix, nsMap.get( prefix ) );
        }
        return compiledXPath;
    }

    /**
     * Write an element with simple text content into the XMLStream.
     * <p>
     * Convinience method to write simple elements like:
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
        writer.writeCharacters( value );
        writer.writeEndElement();
    }

    /**
     * Write an element with a single attribute into the XMLStream.
     * 
     * <p>
     * Convinience method to write simple elements like:
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
     * Convinience method to write simple elements like:
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
                writer.writeStartElement( inStream.getPrefix(), inStream.getLocalName(), inStream.getNamespaceURI() );
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
     * Creates printable (debug) information about the event that the cursor of the given <code>XMLStreamReader</code>
     * currently points at.
     * 
     * @param xmlStream
     *            points at the event to be analysed, the current position of <code>xmlStream</code> is unchanged
     * @return printable information
     */
    public final static String getCurrentEventInfo( XMLStreamReader xmlStream ) {
        String s = getEventTypeString( xmlStream.getEventType() );
        if ( xmlStream.getEventType() == START_ELEMENT || xmlStream.getEventType() == END_ELEMENT ) {
            s += ": " + xmlStream.getName();
        }
        Location location = xmlStream.getLocation();
        s += " at line " + location.getLineNumber() + ", column " + location.getColumnNumber() + " (character offset "
             + xmlStream.getLocation().getCharacterOffset() + ")";
        return s;
    }

    private final static String getEventTypeString( int eventType ) {
        switch ( eventType ) {
        case START_ELEMENT:
            return "START_ELEMENT";
        case END_ELEMENT:
            return "END_ELEMENT";
        case PROCESSING_INSTRUCTION:
            return "PROCESSING_INSTRUCTION";
        case CHARACTERS:
            return "CHARACTERS";
        case COMMENT:
            return "COMMENT";
        case START_DOCUMENT:
            return "START_DOCUMENT";
        case END_DOCUMENT:
            return "END_DOCUMENT";
        case ENTITY_REFERENCE:
            return "ENTITY_REFERENCE";
        case ATTRIBUTE:
            return "ATTRIBUTE";
        case DTD:
            return "DTD";
        case CDATA:
            return "CDATA";
        case SPACE:
            return "SPACE";
        }
        return "UNKNOWN_EVENT_TYPE , " + eventType;
    }

    /**
     * Skips all events that belong to the current element (including descendant elements), so that the
     * <code>XMLStreamReader</code> cursor points at the corresponding <code>END_ELEMENT</code> event.
     * 
     * @param xmlStream
     *            cursor must point at a <code>START_ELEMENT</code> event and points at the corresponding
     *            <code>END_ELEMENT</code> event afterwards
     * @throws XMLStreamException
     */
    public static void skipElement( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        int openElements = 1;
        while ( openElements > 0 ) {
            int event = xmlStream.next();
            if ( event == END_ELEMENT ) {
                openElements--;
            } else if ( event == START_ELEMENT ) {
                openElements++;
            }
        }
    }    
}
