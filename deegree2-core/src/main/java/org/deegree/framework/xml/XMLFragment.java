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

package org.deegree.framework.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.model.feature.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An instance of <code>XMLFragment</code> encapsulates an underlying {@link Element} which acts as the root element of
 * the document (which may be a fragment or a whole document).
 * <p>
 * Basically, <code>XMLFragment</code> provides easy loading and proper saving (automatically generated CDATA-elements
 * for text nodes that need to be escaped) and acts as base class for all XML parsers in deegree.
 * 
 * TODO: automatically generated CDATA-elements are not implemented yet
 * 
 * <p>
 * Additionally, <code>XMLFragment</code> tries to make the handling of relative paths inside the document's content as
 * painless as possible. This means that after initialization of the <code>XMLFragment</code> with the correct SystemID
 * (i.e. the URL of the document):
 * <ul>
 * <li>external parsed entities (in the DOCTYPE part) can use relative URLs; e.g. &lt;!ENTITY local SYSTEM
 * "conf/wfs/wfs.cfg"&gt;</li>
 * <li>application specific documents which extend <code>XMLFragment</code> can resolve relative URLs during parsing by
 * calling the <code>resolve()</code> method</li>
 * </ul>
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @see org.deegree.framework.xml.XMLTools
 */

public class XMLFragment implements Serializable {

    private static final long serialVersionUID = 8984447437613709386L;

    /**
     * The namespace map containing the prefixes mapped to the namespaces.
     */
    protected static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * The xlink namespace
     */
    protected static final URI XLNNS = CommonNamespaces.XLNNS;

    private static final ILogger LOG = LoggerFactory.getLogger( XMLFragment.class );

    /**
     * Use this URL as SystemID only if an <code>XMLFragment</code> cannot be pinpointed to a URL - in this case it may
     * not use any relative references!
     */
    public static final String DEFAULT_URL = "http://www.deegree.org";

    private URL systemId;

    private Element rootElement;

    static {
        LOG.logDebug( "DOM implementation in use (DocumentBuilderFactory): "
                      + DocumentBuilderFactory.newInstance().getClass().getName() );
        try {
            LOG.logDebug( "DOM implementation in use (DocumentBuilder): "
                          + DocumentBuilderFactory.newInstance().newDocumentBuilder().getClass().getName() );
        } catch ( Exception e ) {
            BootLogger.logError( "Error creating test DocumentBuilder instance.", e );
        }
    }

    /**
     * Creates a new <code>XMLFragment</code> which is not initialized.
     */
    public XMLFragment() {
        // nothing to do
    }

    /**
     * Creates a new <code>XMLFragment</code> which is loaded from the given <code>URL</code>.
     * 
     * @param url
     * @throws IOException
     * @throws SAXException
     */
    public XMLFragment( URL url ) throws IOException, SAXException {
        load( url );
    }

    /**
     * Creates a new <code>XMLFragment</code> which is loaded from the given <code>File</code>.
     * 
     * @param file
     *            the file to load from
     * @throws SAXException
     *             if the document could not be parsed
     * @throws IOException
     *             if the document could not be read
     * @throws MalformedURLException
     *             if the file cannot be transposed to a valid url
     */
    public XMLFragment( File file ) throws MalformedURLException, IOException, SAXException {
        if ( file != null ) {
            load( file.toURI().toURL() );
        }
    }

    /**
     * Creates a new <code>XMLFragment</code> which is loaded from the given <code>Reader</code>.
     * 
     * @param reader
     * @param systemId
     *            this string should represent a URL that is related to the passed reader. If this URL is not available
     *            or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     * @throws SAXException
     * @throws IOException
     */
    public XMLFragment( Reader reader, String systemId ) throws SAXException, IOException {
        load( reader, systemId );
    }

    /**
     * Creates a new <code>XMLFragment</code> instance based on the submitted <code>Document</code>.
     * 
     * @param doc
     * @param systemId
     *            this string should represent a URL that is the source of the passed doc. If this URL is not available
     *            or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     * @throws MalformedURLException
     *             if systemId is no valid and absolute <code>URL</code>
     */
    public XMLFragment( Document doc, String systemId ) throws MalformedURLException {
        setRootElement( doc.getDocumentElement() );
        setSystemId( systemId );
    }

    /**
     * Creates a new <code>XMLFragment</code> instance based on the submitted <code>Element</code>.
     * 
     * @param element
     */
    public XMLFragment( Element element ) {
        setRootElement( element );
    }

    /**
     * Constructs an empty document with the given <code>QualifiedName</code> as root node.
     * 
     * @param elementName
     *            if the name's namespace is set, the prefix should be set as well.
     */
    public XMLFragment( QualifiedName elementName ) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if ( elementName.getNamespace() == null ) {
                rootElement = db.newDocument().createElement( elementName.getLocalName() );
            } else {
                String pre = elementName.getPrefix();
                String ns = elementName.getNamespace().toString();
                if ( pre == null || pre.trim().length() == 0 ) {
                    pre = "dummy";
                    LOG.logWarning( StringTools.concat( 200, "Incorrect usage of deegree API,",
                                                        " prefix of a root node was not ", "defined:\nNode name was ",
                                                        elementName.getLocalName(), ", namespace was ", ns ) );
                }
                String name = StringTools.concat( 200, pre, ":", elementName.getLocalName() );
                rootElement = db.newDocument().createElementNS( ns, name );
                rootElement.getOwnerDocument().appendChild( rootElement );
            }
        } catch ( ParserConfigurationException e ) {
            LOG.logError( "The parser seems to be misconfigured. Broken installation?", e );
        }
    }

    /**
     * Returns the systemId (the URL of the <code>XMLFragment</code>).
     * 
     * @return the systemId
     */
    public URL getSystemId() {
        return systemId;
    }

    /**
     * @param systemId
     *            The systemId (physical location) to set (may be null).
     * @throws MalformedURLException
     */
    public void setSystemId( String systemId )
                            throws MalformedURLException {
        if ( systemId != null ) {
            this.systemId = new URL( systemId );
        }
    }

    /**
     * @param systemId
     *            The systemId (physical location) to set.
     */
    public void setSystemId( URL systemId ) {
        this.systemId = systemId;
    }

    /**
     * Returns whether the document has a schema reference.
     * 
     * @return true, if the document has a schema reference, false otherwise
     */
    public boolean hasSchema() {
        if ( this.rootElement.getAttribute( "xsi:schemaLocation" ) != null ) {
            return true;
        }
        return false;
    }

    /**
     * Determines the namespace <code>URI</code>s and the bound schema <code>URL</code>s from the 'xsi:schemaLocation'
     * attribute of the document element.
     * 
     * @return keys are URIs (namespaces), values are URLs (schema locations)
     * @throws XMLParsingException
     */
    public Map<URI, URL> getAttachedSchemas()
                            throws XMLParsingException {

        Map<URI, URL> schemaMap = new HashMap<URI, URL>();

        NamedNodeMap attrMap = rootElement.getAttributes();
        Node schemaLocationAttr = attrMap.getNamedItem( "xsi:schemaLocation" );
        if ( schemaLocationAttr == null ) {
            return schemaMap;
        }

        String target = schemaLocationAttr.getNodeValue();
        StringTokenizer tokenizer = new StringTokenizer( target );

        while ( tokenizer.hasMoreTokens() ) {
            URI nsURI = null;
            String token = tokenizer.nextToken();
            try {
                nsURI = new URI( token );
            } catch ( URISyntaxException e ) {
                String msg = "Invalid 'xsi:schemaLocation' attribute: namespace " + token + "' is not a valid URI.";
                LOG.logError( msg );
                throw new XMLParsingException( msg );
            }

            URL schemaURL = null;
            try {
                token = tokenizer.nextToken();
                schemaURL = resolve( token );
            } catch ( NoSuchElementException e ) {
                String msg = "Invalid 'xsi:schemaLocation' attribute: namespace '" + nsURI
                             + "' is missing a schema URL.";
                LOG.logError( msg );
                throw new XMLParsingException( msg );
            } catch ( MalformedURLException ex ) {
                String msg = "Invalid 'xsi:schemaLocation' attribute: '" + token + "' for namespace '" + nsURI
                             + "' could not be parsed as URL.";
                throw new XMLParsingException( msg );
            }
            schemaMap.put( nsURI, schemaURL );
        }
        return schemaMap;
    }

    /**
     * Initializes the <code>XMLFragment</code> with the content from the given <code>URL</code>. Sets the SystemId,
     * too.
     * 
     * @param url
     * @throws IOException
     * @throws SAXException
     */
    public void load( URL url )
                            throws IOException, SAXException {
        if ( url == null ) {
            throw new IllegalArgumentException( "The given url may not be null" );
        }

        String uri = url.toExternalForm();
        if ( !uri.startsWith( "http://" ) ) {
            load( url.openStream(), uri );
            return;
        }
        // else try to use a proxy
        HttpClient client = new HttpClient();
        WebUtils.enableProxyUsage( client, url );
        GetMethod get = new GetMethod( url.toExternalForm() );
        client.executeMethod( get );
        load( get.getResponseBodyAsStream(), uri );
    }

    /**
     * Initializes the <code>XMLFragment</code> with the content from the given <code>InputStream</code>. Sets the
     * SystemId, too.
     * 
     * @param istream
     * @param systemId
     *            cannot be null. This string should represent a URL that is related to the passed istream. If this URL
     *            is not available or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     * @throws SAXException
     * @throws IOException
     * @throws XMLException
     * @throws NullPointerException
     */
    public void load( InputStream istream, String systemId )
                            throws SAXException, IOException, XMLException {

        PushbackInputStream pbis = new PushbackInputStream( istream, 1024 );
        String encoding = readEncoding( pbis );

        if ( LOG.isDebug() ) {
            LOG.logDebug( "Reading XMLFragment " + systemId + " with encoding ", encoding );
        }

        InputStreamReader isr = new InputStreamReader( pbis, encoding );
        load( isr, systemId );
    }

    /**
     * reads the encoding of a XML document from its header. If no header available
     * <code>CharsetUtils.getSystemCharset()</code> will be returned
     * 
     * @param pbis
     * @return encoding of a XML document
     * @throws IOException
     */
    private String readEncoding( PushbackInputStream pbis )
                            throws IOException {
        byte[] b = new byte[80];
        String s = "";
        int rd = 0;

        LinkedList<byte[]> bs = new LinkedList<byte[]>();
        LinkedList<Integer> rds = new LinkedList<Integer>();
        while ( rd < 80 ) {
            rds.addFirst( pbis.read( b ) );
            if ( rds.peek() == -1 ) {
                rds.poll();
                break;
            }
            rd += rds.peek();
            s += new String( b, 0, rds.peek() ).toLowerCase();
            bs.addFirst( b );
            b = new byte[80];
        }

        String encoding = CharsetUtils.getSystemCharset();
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
        while ( !bs.isEmpty() ) {
            pbis.unread( bs.poll(), 0, rds.poll() );
        }

        return encoding;
    }

    /**
     * Initializes the <code>XMLFragment</code> with the content from the given <code>Reader</code>. Sets the SystemId,
     * too.
     * 
     * @param reader
     * @param systemId
     *            can not be null. This string should represent a URL that is related to the passed reader. If this URL
     *            is not available or unknown, the string should contain the value of XMLFragment.DEFAULT_URL
     * @throws SAXException
     * @throws IOException
     * @throws NullPointerException
     */
    public void load( Reader reader, String systemId )
                            throws SAXException, IOException {

        PushbackReader pbr = new PushbackReader( reader, 1024 );
        int c = pbr.read();
        if ( c != 65279 && c != 65534 ) {
            // no BOM! push char back into reader
            pbr.unread( c );
        }

        InputSource source = new InputSource( pbr );
        if ( systemId == null ) {
            throw new NullPointerException( "'systemId' must not be null!" );
        }
        setSystemId( systemId );
        DocumentBuilder builder = XMLTools.getDocumentBuilder();
        Document doc = builder.parse( source );
        setRootElement( doc.getDocumentElement() );
    }

    /**
     * @param rootElement
     */
    public void setRootElement( Element rootElement ) {
        this.rootElement = rootElement;
    }

    /**
     * @return the element
     */
    public Element getRootElement() {
        return rootElement;
    }

    /**
     * Resolves the given URL (which may be relative) against the SystemID of the <code>XMLFragment</code> into a
     * <code>URL</code> (which is always absolute).
     * 
     * @param url
     * @return the resolved URL object
     * @throws MalformedURLException
     */
    public URL resolve( String url )
                            throws MalformedURLException {
        LOG.logDebug( StringTools.concat( 200, "Resolving URL '", url, "' against SystemID '", systemId,
                                          "' of XMLFragment" ) );

        URL resolvedURL = FileUtils.resolt( systemId, url );

        LOG.logDebug( StringTools.concat( 100, "-> resolvedURL: '", resolvedURL, "'" ) );
        return resolvedURL;
    }

    /**
     * Writes the <code>XMLFragment</code> instance to the given <code>Writer</code> using the default system encoding
     * and adding CDATA-sections in for text-nodes where needed.
     * 
     * TODO: Add code for CDATA safety.
     * 
     * @param writer
     */
    public void write( Writer writer ) {
        Properties properties = new Properties();
        properties.setProperty( OutputKeys.ENCODING, CharsetUtils.getSystemCharset() );
        write( writer, properties );
    }

    /**
     * Writes the <code>XMLFragment</code> instance to the given <code>Writer</code> using the specified
     * <code>OutputKeys</code>.
     * 
     * @param writer
     *            cannot be null
     * @param outputProperties
     *            output properties for the <code>Transformer</code> that is used to serialize the document
     * 
     *            see javax.xml.OutputKeys
     */
    public void write( Writer writer, Properties outputProperties ) {
        try {
            Source source = new DOMSource( rootElement );
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if ( outputProperties != null ) {
                transformer.setOutputProperties( outputProperties );
            }
            transformer.transform( source, new StreamResult( writer ) );
        } catch ( TransformerConfigurationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLException( e );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLException( e );
        }
    }

    /**
     * Writes the <code>XMLFragment</code> instance to the given <code>OutputStream</code> using the default system
     * encoding and adding CDATA-sections in for text-nodes where needed.
     * 
     * TODO: Add code for CDATA safety.
     * 
     * @param os
     */
    public void write( OutputStream os ) {
        Properties properties = new Properties();
        properties.setProperty( OutputKeys.ENCODING, CharsetUtils.getSystemCharset() );
        write( os, properties );
    }

    /**
     * Writes the <code>XMLFragment</code> instance to the given <code>OutputStream</code> using the specified
     * <code>OutputKeys</code> which allow complete control of the generated output.
     * 
     * @param os
     *            cannot be null
     * @param outputProperties
     *            output properties for the <code>Transformer</code> used to serialize the document
     * 
     * @see javax.xml.transform.OutputKeys
     */
    public void write( OutputStream os, Properties outputProperties ) {
        try {
            Source source = new DOMSource( rootElement );
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if ( outputProperties != null ) {
                transformer.setOutputProperties( outputProperties );
            }
            transformer.transform( source, new StreamResult( os ) );
        } catch ( TransformerConfigurationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLException( e );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLException( e );
        }
    }

    /**
     * Writes the <code>XMLFragment</code> instance to the given <code>OutputStream</code> using indentation so it may
     * be read easily.
     * 
     * @param os
     * @throws TransformerException
     */
    public void prettyPrint( OutputStream os )
                            throws TransformerException {
        InputStream xsl = XMLFragment.class.getResourceAsStream( "PrettyPrinter.xsl" );
        Transformer transformer = TransformerFactory.newInstance().newTransformer( new StreamSource( xsl ) );
        transformer.transform( new DOMSource( rootElement ), new StreamResult( os ) );
    }

    /**
     * Writes the <code>XMLFragment</code> instance to the given <code>Writer</code> using indentation so it may be read
     * easily.
     * 
     * @param writer
     * @throws TransformerException
     */
    public void prettyPrint( Writer writer )
                            throws TransformerException {
        InputStream xsl = XMLFragment.class.getResourceAsStream( "PrettyPrinter.xsl" );
        Transformer transformer = TransformerFactory.newInstance().newTransformer( new StreamSource( xsl ) );
        transformer.transform( new DOMSource( rootElement ), new StreamResult( writer ) );
    }

    /**
     * Parses the submitted <code>Element</code> as a <code>SimpleLink</code>.
     * <p>
     * Possible escaping of the attributes "xlink:href", "xlink:role" and "xlink:arcrole" is performed automatically.
     * </p>
     * 
     * @param element
     * @return the object representation of the element
     * @throws XMLParsingException
     */
    protected SimpleLink parseSimpleLink( Element element )
                            throws XMLParsingException {

        URI href = null;
        URI role = null;
        URI arcrole = null;
        String title = null;
        String show = null;
        String actuate = null;

        String uriString = null;
        try {
            uriString = XMLTools.getNodeAsString( element, "@xlink:href", nsContext, null );
            if ( uriString != null ) {
                href = new URI( null, uriString, null );
            }
            uriString = XMLTools.getNodeAsString( element, "@xlink:role", nsContext, null );
            if ( uriString != null ) {
                role = new URI( null, uriString, null );
            }
            uriString = XMLTools.getNodeAsString( element, "@xlink:arcrole", nsContext, null );
            if ( uriString != null ) {
                arcrole = new URI( null, uriString, null );
            }
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "'" + uriString + "' is not a valid URI." );
        }

        return new SimpleLink( href, role, arcrole, title, show, actuate );
    }

    /**
     * Parses the value of the submitted <code>Node</code> as a <code>QualifiedName</code>.
     * <p>
     * To parse the text contents of an <code>Element</code> node, the actual text node must be given, not the
     * <code>Element</code> node itself.
     * </p>
     * 
     * @param node
     * @return object representation of the element
     * @throws XMLParsingException
     */
    public static QualifiedName parseQualifiedName( Node node )
                            throws XMLParsingException {

        String name = node.getNodeValue().trim();
        QualifiedName qName = null;
        if ( name.indexOf( ':' ) > -1 ) {
            String[] tmp = StringTools.toArray( name, ":", false );
            try {
                qName = new QualifiedName( tmp[0], tmp[1], XMLTools.getNamespaceForPrefix( tmp[0], node ) );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( e.getMessage(), e );
            }
        } else {
            qName = new QualifiedName( name );
        }
        return qName;
    }

    /**
     * Returns the qualified name of the given element.
     * 
     * @param element
     * @return the qualified name of the given element.
     * @throws XMLParsingException
     */
    protected QualifiedName getQualifiedName( Element element )
                            throws XMLParsingException {

        // TODO check if we can use element.getNamespaceURI() instead
        URI nsURI = null;
        String prefix = element.getPrefix();
        try {
            nsURI = XMLTools.getNamespaceForPrefix( prefix, element );
        } catch ( URISyntaxException e ) {
            String msg = Messages.format( "ERROR_NSURI_NO_URI", element.getPrefix() );
            LOG.logError( msg, e );
            throw new XMLParsingException( msg, e );
        }
        QualifiedName ftName = new QualifiedName( prefix, element.getLocalName(), nsURI );

        return ftName;
    }

    /**
     * Returns a string representation of the XML Document
     * 
     * @return the string
     */
    public String getAsString() {
        StringWriter writer = new StringWriter( 50000 );
        Source source = new DOMSource( rootElement );
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty( "encoding", CharsetUtils.getSystemCharset() );
            transformer.transform( source, new StreamResult( writer ) );
        } catch ( Exception e ) {
            LOG.logError( "Error serializing XMLFragment!", e );
        }
        return writer.toString();
    }

    /**
     * Returns a string representation of the XML Document, pretty printed. Note that pretty printing can mess up XML
     * documents in some cases (GML, for instance).
     * 
     * @return the string
     */
    public String getAsPrettyString() {
        StringWriter writer = new StringWriter( 50000 );
        try {
            prettyPrint( writer );
        } catch ( TransformerException e ) {
            LOG.logError( "Error pretty printing XMLFragment!", e );
        }
        return writer.toString();
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return getAsString();
    }
}
