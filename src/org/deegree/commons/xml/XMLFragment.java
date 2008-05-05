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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.utils.StringTools;
import org.exolab.castor.xml.XMLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An instance of <code>XMLFragment</code> encapsulates an underlying {@link Element} which acts as the root element
 * of the document (which may be a fragment or a whole document).
 * <p>
 * Basically, <code>XMLFragment</code> provides easy loading and proper saving (automatically generated CDATA-elements
 * for text nodes that need to be escaped) and acts as base class for all XML parsers in deegree.
 * 
 * TODO: automatically generated CDATA-elements are not implemented yet
 * 
 * <p>
 * Additionally, <code>XMLFragment</code> tries to make the handling of relative paths inside the document's content
 * as painless as possible. This means that after initialization of the <code>XMLFragment</code> with the correct
 * SystemID (i.e. the URL of the document):
 * <ul>
 * <li>external parsed entities (in the DOCTYPE part) can use relative URLs; e.g. &lt;!ENTITY local SYSTEM
 * "conf/wfs/wfs.cfg"&gt;</li>
 * <li>application specific documents which extend <code>XMLFragment</code> can resolve relative URLs during parsing
 * by calling the <code>resolve()</code> method</li>
 * </ul>
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @see org.deegree.framework.xml.XMLTools
 * @deprecated this class should be updated to the new xml framework.
 */

@Deprecated
public class XMLFragment implements Serializable {

    private static final long serialVersionUID = 8984447437613709386L;

    protected static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    protected static final String XLNNS = CommonNamespaces.XLNNS;

    private static final Log LOG = LogFactory.getLog( XMLFragment.class );

    /**
     * Use this URL as SystemID only if an <code>XMLFragment</code> cannot be pinpointed to a URL - in this case it
     * may not use any relative references!
     */
    public static final String DEFAULT_URL = "http://www.deegree.org";

    private URL systemId;

    private Element rootElement;

    static {
        LOG.debug( "DOM implementation in use (DocumentBuilderFactory): "
                   + DocumentBuilderFactory.newInstance().getClass().getName() );
        try {
            LOG.debug( "DOM implementation in use (DocumentBuilder): "
                       + DocumentBuilderFactory.newInstance().newDocumentBuilder().getClass().getName() );
        } catch ( Exception e ) {
            LOG.error( "Error creating test DocumentBuilder instance.", e );
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
     * Constructs an empty document with the given <code>QualifiedName</code> as root node.
     * 
     * @param elementName
     *            if the name's namespace is set, the prefix should be set as well.
     */
    public XMLFragment( QName elementName ) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if ( elementName.getNamespaceURI() == null ) {
                rootElement = db.newDocument().createElement( elementName.getLocalPart() );
            } else {
                String pre = elementName.getPrefix();
                String ns = elementName.getNamespaceURI();
                if ( pre == null || pre.trim().length() == 0 ) {
                    pre = "dummy";
                    LOG.warn( StringTools.concat( 200, "Incorrect usage of deegree API,",
                                                  " prefix of a root node was not ", "defined:\nNode name was ",
                                                  elementName.getLocalPart(), ", namespace was ", ns ) );
                }
                String name = StringTools.concat( 200, pre, ":", elementName.getLocalPart() );
                rootElement = db.newDocument().createElementNS( ns, name );
                rootElement.setAttribute( "xmlns:" + pre, ns );
            }
        } catch ( ParserConfigurationException e ) {
            LOG.error( "The parser seems to be misconfigured. Broken installation?", e );
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
     * Determines the namespace <code>URI</code>s and the bound schema <code>URL</code>s from the
     * 'xsi:schemaLocation' attribute of the document element.
     * 
     * @return keys are URIs (namespaces), values are URLs (schema locations)
     * @throws XMLProcessingException
     */
    public Map<URI, URL> getAttachedSchemas()
                            throws XMLProcessingException {

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
            schemaMap.put( nsURI, schemaURL );
        }
        return schemaMap;
    }

    /**
     * Initializes the <code>XMLFragment</code> with the content from the given <code>URL</code>. Sets the
     * SystemId, too.
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
        load( url.openStream(), uri );
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
                            throws SAXException, IOException {

        PushbackInputStream pbis = new PushbackInputStream( istream, 1024 );
        String encoding = readEncoding( pbis );

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
        int rd = pbis.read( b );
        String s = new String( b ).toLowerCase();
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
    }

    /**
     * Initializes the <code>XMLFragment</code> with the content from the given <code>Reader</code>. Sets the
     * SystemId, too.
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
        LOG.debug( StringTools.concat( 200, "Resolving URL '", url, "' against SystemID '", systemId,
                                       "' of XMLFragment" ) );
        // check if url is an absolut path
        File file = new File( url );
        if ( file.isAbsolute() ) {
            return file.toURI().toURL();
        }
        // remove leading '/' because otherwise
        // URL resolvedURL = new URL( systemId, url ); will fail
        if ( url.startsWith( "/" ) ) {
            url = url.substring( 1, url.length() );
            LOG.info( "URL has been corrected by removing the leading '/'" );
        }
        URL resolvedURL = new URL( systemId, url );

        LOG.debug( StringTools.concat( 100, "-> resolvedURL: '", resolvedURL, "'" ) );
        return resolvedURL;
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
            transformer.setOutputProperty( "encoding", "UTF-8" );
            transformer.transform( source, new StreamResult( writer ) );
        } catch ( Exception e ) {
            LOG.error( "Error serializing XMLFragment!", e );
        }
        return writer.toString();
    }

    /**
     * Returns a string representation of the XML Document, pretty printed. Note that pretty printing can mess up XML
     * documents in some cases (GML, for instance).
     * 
     * @return the string
     * @throws XMLProcessingException 
     */
    public String getAsPrettyString() throws XMLProcessingException {
        StringWriter writer = new StringWriter( 50000 );
        try {
            Source source = new DOMSource( rootElement );
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform( source, new StreamResult( writer ) );
        } catch ( TransformerConfigurationException e ) {
            LOG.error( e.getMessage(), e );
            throw new XMLProcessingException( e );
        } catch ( Exception e ) {
            LOG.error( e.getMessage(), e );
            throw new XMLProcessingException( e );
        }

//        StringWriter writer = new StringWriter( 50000 );
//        try {
//            write( writer );
//        } catch ( TransformerException e ) {
//            LOG.error( "Error pretty printing XMLFragment!", e );
//        }
        return writer.toString();
    }
        
        /**
         * Writes the <code>XMLFragment</code> instance to the given <code>Writer</code> using the
         * specified <code>OutputKeys</code>.
         * 
         * @param writer
         *            cannot be null
         * @param outputProperties
         *            output properties for the <code>Transformer</code> that is used to serialize the
         *            document
         * 
         * see javax.xml.OutputKeys
         */
        public void write( Writer writer, Properties outputProperties ) {
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
