//$HeadURL$
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

package org.deegree.portal.context;

import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.deegree.framework.util.Parameter;
import org.deegree.framework.util.ParameterList;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.CitedResponsibleParty;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ImageURL;
import org.deegree.ogcwebservices.OWSUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * This is a factory class to export a <code>ViewContext</code> and a <code>ViewContextCollection</code> as an xml
 * <code>org.w3c.dom.Document</code>.
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLFactory {

    // Import and define constants
    private static URI OGC_CONTEXT_NS = CommonNamespaces.CNTXTNS;

    private static URI D_CONTEXT_NS = CommonNamespaces.DGCNTXTNS;

    private static URI SLD_NS = CommonNamespaces.SLDNS;

    private static URI XSI_NS = CommonNamespaces.buildNSURI( "http://www.w3.org/2001/XMLSchema-instance" );

    private static URI XLINK_NS = CommonNamespaces.buildNSURI( "http://www.w3.org/1999/xlink" );

    // Common objects
    protected static javax.xml.parsers.DocumentBuilderFactory factory = null;

    protected static javax.xml.parsers.DocumentBuilder builder = null;

    protected static Document document = null;

    private XMLFactory() {
        // Forbid instantiation
    }

    /**
     * Convenience method for creating a common document builder. Implementation copied from XmlDocument (by tf and ap).
     * 
     * @throws ParserConfigurationException
     */
    protected static void initDocBuilder()
                            throws ParserConfigurationException {

        if ( builder == null ) {
            if ( factory == null ) {
                factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                factory.setIgnoringElementContentWhitespace( true );
                factory.setNamespaceAware( false );
                factory.setExpandEntityReferences( false );
            }
            builder = factory.newDocumentBuilder();
        }

    }

    /**
     * Creates a new <code>org.w3c.dom.Document</code> using the internal document builder.
     * 
     * @return new <code>Document</code> instance
     * @throws ParserConfigurationException
     */
    protected static Document createDocument()
                            throws ParserConfigurationException {

        initDocBuilder();

        return builder.newDocument();
    }

    /**
     * Creates a new <code>org.w3c.dom.Element</code>.
     * 
     * @param namespace
     *            the element namespace
     * @param elemName
     *            the element name
     * @return new <code>Element</code> instance
     */
    private static Element createElement( URI namespace, String elemName ) {

        return document.createElementNS( namespace == null ? null : namespace.toString(), elemName );
    }

    /**
     * Creates a new <code>org.w3c.dom.Attr</code>.
     * 
     * @param attName
     *            the attribute name
     * @param value
     *            the attribute value
     * @return new <code>Attr</code> instance
     */
    private static Attr createAttribute( String attName, String value ) {

        Attr attr = document.createAttribute( attName );
        attr.setValue( value );

        return attr;
    }

    /**
     * Creates a new <code>org.w3c.dom.Text</code>. This is the textual content of an element.
     * 
     * @param text
     *            the attribute name (if <code>null</code>, then context stays empty)
     * @return new <code>Text</code> instance
     */
    private static Text createTextNode( String text ) {

        String t = "";
        if ( text != null ) {
            t = text;
        }

        return document.createTextNode( t );
    }

    /**
     * Creates a new <code>org.w3c.dom.Document</code> describing a <code>ViewContext</code>.
     * 
     * @param viewContext
     *            the <code>ViewContext</code> to be exported
     * @return the xml dom-representation of the given viewContext.
     * @throws ParserConfigurationException
     *             if an XML parser couldn't be found
     */
    public static XMLFragment export( ViewContext viewContext )
                            throws ParserConfigurationException {

        document = createDocument();
        // start appending nodes...
        try {
            appendViewContext( document, viewContext );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ParserConfigurationException( e.getMessage() );
        }

        XMLFragment xml = null;
        try {
            xml = new XMLFragment( document, XMLFragment.DEFAULT_URL );
        } catch ( MalformedURLException neverHappens ) {
            neverHappens.printStackTrace();
        }

        return xml;
    }

    /**
     * Creates a new <code>org.w3c.dom.Document</code> describing a <code>ViewContextCollection</code>.
     * 
     * @param viewContCollec
     *            the <code>ViewContextCollection</code> to be exported
     * @return the xml dom-representation of the given viewContextCollection.
     * @throws ParserConfigurationException
     *             if an XML parser couldn't be found
     * 
     */
    public static Document export( ViewContextCollection viewContCollec )
                            throws ParserConfigurationException {

        document = createDocument();
        // start appending nodes...
        appendViewContextCollection( document, viewContCollec );

        return document;
    }

    /**
     * Appends the XML representation of a <code>ViewContext</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param viewContxt
     *            the <code>ViewContext</code> to be appended as new element
     * @throws URISyntaxException
     * @throws DOMException
     * 
     * 
     */
    protected static void appendViewContext( Node toNode, ViewContext viewContxt )
                            throws DOMException, URISyntaxException {

        if ( viewContxt != null ) {
            Element e = createElement( OGC_CONTEXT_NS, "ViewContext" );
            Element rootNode = (Element) toNode.appendChild( e );
            XMLTools.appendNSBinding( rootNode, "sld", SLD_NS );
            XMLTools.appendNSBinding( rootNode, "xlink", XLINK_NS );
            XMLTools.appendNSBinding( rootNode, "deegree", D_CONTEXT_NS );
            XMLTools.appendNSBinding( rootNode, "xsi", XSI_NS );
            // XMLTools.appendNSBinding( rootNode, "", OGC_CONTEXT_NS );
            XMLTools.appendNSDefaultBinding( rootNode, OGC_CONTEXT_NS );

            // e.setAttributeNode( createAttribute( "xmlns", OGC_CONTEXT_NS.toString() ) );
            // e.setAttributeNode( createAttribute( "xmlns:sld", ) );
            // e.setAttributeNode( createAttribute( "xmlns:xlink", .toString() ) );
            // e.setAttributeNode( createAttribute( "xmlns:deegree", D_CONTEXT_NS.toString() ) );
            // e.setAttributeNode( createAttribute( "xmlns:", XSI_NS.toString() ) );
            rootNode.setAttribute( "version", "1.0.0" );
            rootNode.setAttribute( "id", "viewContext_id" );

            appendGeneral( rootNode, viewContxt.getGeneral() );
            appendLayerList( rootNode, viewContxt.getLayerList() );

            // toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a <code>General</code> to a <code>Node</code> using the <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param gen
     *            the <code>General</code> to be appended as new element
     * 
     *            contains illegal characters
     * @throws URISyntaxException
     * @throws DOMException
     */
    protected static void appendGeneral( Node toNode, General gen )
                            throws DOMException, URISyntaxException {

        if ( gen != null ) {
            Element e = createElement( OGC_CONTEXT_NS, "General" );
            appendWindow( e, gen.getWindow() );
            appendBoundingBox( e, gen.getBoundingBox() );
            appendTitle( e, gen.getTitle() );
            appendAbstract( e, gen.getAbstract() );
            appendKeywords( e, gen.getKeywords() );
            appendDescriptionURL( e, gen.getDescriptionURL() );
            appendLogoURL( e, gen.getLogoURL() );
            appendContactInformation( e, gen.getContactInformation() );
            // append deegree-specific extension
            appendGeneralExtension( e, gen.getExtension() );

            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a <code>Rectangle</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * <p/>
     * Note that the XML representation of a <code>Rectangle</code> is given by a <code>&lt;Window&gt;</code> element.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param r
     *            the <code>Rectangle</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendWindow( Node toNode, Rectangle r ) {

        if ( r != null ) {
            Element window = createElement( OGC_CONTEXT_NS, "Window" );

            window.setAttribute( "width", String.valueOf( r.width ) );
            window.setAttribute( "height", String.valueOf( r.height ) );

            toNode.appendChild( window );
        }

    }

    /**
     * Appends the XML representation of a <code>GM_Point[]</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * <p/>
     * Note that the XML representation of a <code>GM_Point[]</code> is given by a <code>&lt;BoundingBox&gt;</code>
     * element.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param points
     *            the <code>GM_Point[]</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendBoundingBox( Node toNode, Point[] points ) {

        if ( points != null && points.length == 2 ) {
            Element bbox = createElement( OGC_CONTEXT_NS, "BoundingBox" );
            String srs = "UNKNOWN_SRS";
            try {
                srs = points[0].getCoordinateSystem().getIdentifier();
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            bbox.setAttributeNode( createAttribute( "SRS", srs ) );

            bbox.setAttribute( "minx", String.valueOf( points[0].getX() ) );
            bbox.setAttribute( "miny", String.valueOf( points[0].getY() ) );
            bbox.setAttribute( "maxx", String.valueOf( points[1].getX() ) );
            bbox.setAttribute( "maxy", String.valueOf( points[1].getY() ) );

            toNode.appendChild( bbox );

        }

    }

    /**
     * Appends the XML representation of a <code>Title</code> to a <code>Node</code> using the <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param title
     *            the <code>String</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendTitle( Node toNode, String title ) {

        String t = "";
        if ( title != null ) {
            t = title;
        }
        Element te = createElement( OGC_CONTEXT_NS, "Title" );
        te.appendChild( createTextNode( t ) );
        toNode.appendChild( te );

    }

    /**
     * Appends the XML representation of an <code>Abstract</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param abstr
     *            the <code>String</code> to be appended as new element
     * 
     */
    protected static void appendAbstract( Node toNode, String abstr ) {

        if ( abstr != null ) {
            Element te = createElement( OGC_CONTEXT_NS, "Abstract" );
            te.appendChild( createTextNode( abstr ) );
            toNode.appendChild( te );
        }

    }

    /**
     * Appends the XML representation of an <code>ImageURL</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param logoURL
     *            the <code>ImageURL</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendLogoURL( Node toNode, ImageURL logoURL ) {

        if ( logoURL != null && logoURL.getOnlineResource() != null ) {
            Element e = createElement( OGC_CONTEXT_NS, "LogoURL" );
            appendOnlineResource( e, logoURL.getOnlineResource() );
            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a keyword list as a <code>String[]</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * <p/>
     * Note that the keywords are appended to a <code>&lt;KeywordList&gt;</code> element.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param keywords
     *            the <code>ImageURL</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendKeywords( Node toNode, String[] keywords ) {

        if ( keywords != null ) {
            Element kWordList = createElement( OGC_CONTEXT_NS, "KeywordList" );
            for ( int i = 0; i < keywords.length; i++ ) {
                Element kw = createElement( OGC_CONTEXT_NS, "Keyword" );
                kw.appendChild( createTextNode( keywords[i] ) );
                kWordList.appendChild( kw );
            }
            toNode.appendChild( kWordList );
        }

    }

    /**
     * Appends the XML representation of a <code>BaseURL</code>, the <code>DescriptionURL</code>, to a <code>Node</code>
     * using the <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param bURL
     *            the <code>BaseURL</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendDescriptionURL( Node toNode, BaseURL bURL ) {

        if ( bURL != null ) {
            Element du = createElement( OGC_CONTEXT_NS, "DescriptionURL" );
            String f = bURL.getFormat();
            if ( f != null ) {
                du.setAttribute( "format", f );
            }

            URL onlineRes = bURL.getOnlineResource();
            appendOnlineResource( du, onlineRes );

            toNode.appendChild( du );
        }

    }

    /**
     * Appends the XML representation of a <code>URL</code> to a <code>Node</code> as a
     * <code>&lt;OnlineResource&gt;</code> using the <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param onlineRes
     *            the <code>URL</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendOnlineResource( Node toNode, URL onlineRes ) {

        if ( onlineRes != null ) {
            Element or = createElement( OGC_CONTEXT_NS, "OnlineResource" );
            or.setAttributeNS( XLINK_NS.toASCIIString(), "xlink:type", "simple" );

            String href = onlineRes.toExternalForm();
            if ( href != null ) {
                // according to OGC WMS 1.3 Testsuite a URL to a service operation
                // via HTTPGet must end with '?' or '&'
                if ( href.indexOf( '.' ) < 0 ) {
                    href = OWSUtils.validateHTTPGetBaseURL( href );
                }
                or.setAttributeNS( XLINK_NS.toASCIIString(), "xlink:href", href );
            }
            toNode.appendChild( or );
        }

    }

    /**
     * Appends the XML representation of a <code>ContactInformation</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param respParty
     */
    protected static void appendContactInformation( Node toNode, CitedResponsibleParty respParty ) {

        if ( respParty != null ) {
            Element ci = createElement( OGC_CONTEXT_NS, "ContactInformation" );

            appendContactPersonPrimary( ci, respParty );

            Element pos = createElement( OGC_CONTEXT_NS, "ContactPosition" );
            pos.appendChild( createTextNode( respParty.getPositionName()[0] ) );
            ci.appendChild( pos );
            ContactInfo[] conInf = respParty.getContactInfo();

            if ( conInf != null && conInf.length > 0 ) {
                appendContactAddress( ci, conInf[0] );
                if ( conInf[0].getPhone().getVoice() != null && conInf[0].getPhone().getVoice().length > 0 ) {
                    Element e = createElement( OGC_CONTEXT_NS, "ContactVoiceTelephone" );
                    e.appendChild( createTextNode( conInf[0].getPhone().getVoice()[0] ) );
                    ci.appendChild( e );
                }
                if ( conInf[0].getAddress().getElectronicMailAddress() != null
                     && conInf[0].getAddress().getElectronicMailAddress().length > 0 ) {
                    Element e = createElement( OGC_CONTEXT_NS, "ContactElectronicMailAddress" );
                    e.appendChild( createTextNode( conInf[0].getAddress().getElectronicMailAddress()[0] ) );
                    ci.appendChild( e );
                }
            }

            toNode.appendChild( ci );
        }

    }

    /**
     * Appends the XML representation of a <code>ContactPersonPrimary</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param respParty
     */
    protected static void appendContactPersonPrimary( Node toNode, CitedResponsibleParty respParty ) {

        if ( respParty.getIndividualName() != null && respParty.getIndividualName().length > 0 ) {
            Element cpp = createElement( OGC_CONTEXT_NS, "ContactPersonPrimary" );

            Element p = createElement( OGC_CONTEXT_NS, "ContactPerson" );

            p.appendChild( createTextNode( respParty.getIndividualName()[0] ) );
            cpp.appendChild( p );

            Element org = createElement( OGC_CONTEXT_NS, "ContactOrganization" );
            org.appendChild( createTextNode( respParty.getOrganisationName()[0] ) );
            cpp.appendChild( org );

            toNode.appendChild( cpp );
        }

    }

    /**
     * Appends the XML representation of a <code>ContactAddress</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param ci
     *            the <code>ContactAddress</code> to be appended as new element
     * 
     */
    protected static void appendContactAddress( Node toNode, ContactInfo ci ) {

        if ( ci != null ) {
            Element ca = createElement( OGC_CONTEXT_NS, "ContactAddress" );

            Element e = createElement( OGC_CONTEXT_NS, "AddressType" );
            e.appendChild( createTextNode( "postal" ) );
            ca.appendChild( e );

            e = createElement( OGC_CONTEXT_NS, "Address" );
            e.appendChild( createTextNode( ci.getAddress().getDeliveryPoint()[0] ) );
            ca.appendChild( e );

            e = createElement( OGC_CONTEXT_NS, "City" );
            e.appendChild( createTextNode( ci.getAddress().getCity() ) );
            ca.appendChild( e );

            e = createElement( OGC_CONTEXT_NS, "StateOrProvince" );
            e.appendChild( createTextNode( ci.getAddress().getAdministrativeArea() ) );
            ca.appendChild( e );

            e = createElement( OGC_CONTEXT_NS, "PostCode" );
            e.appendChild( createTextNode( ci.getAddress().getPostalCode() ) );
            ca.appendChild( e );

            e = createElement( OGC_CONTEXT_NS, "Country" );
            e.appendChild( createTextNode( ci.getAddress().getCountry() ) );
            ca.appendChild( e );

            toNode.appendChild( ca );
        }

    }

    /**
     * Appends the XML representation of a <code>LayerList</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param lList
     *            the <code>LayerList</code> to be appended as new element
     * 
     */
    protected static void appendLayerList( Node toNode, LayerList lList ) {

        if ( lList != null ) {
            Element list = createElement( OGC_CONTEXT_NS, "LayerList" );

            Layer[] ls = lList.getLayers();
            if ( ls != null ) {
                for ( int i = 0; i < ls.length; i++ ) {
                    appendLayer( list, ls[i] );
                }
            }
            toNode.appendChild( list );
        }

    }

    /**
     * Appends the XML representation of a <code>Layer</code> to a <code>Node</code> using the <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param layer
     *            the <code>Layer</code> to be appended as new element
     * 
     */
    protected static void appendLayer( Node toNode, Layer layer ) {

        if ( layer != null ) {
            Element le = createElement( OGC_CONTEXT_NS, "Layer" );

            le.setAttribute( "queryable", stringValue01( layer.isQueryable() ) );
            le.setAttribute( "hidden", stringValue01( layer.isHidden() ) );

            appendServer( le, layer.getServer() );

            Element n = createElement( OGC_CONTEXT_NS, "Name" );
            n.appendChild( createTextNode( layer.getName() ) );
            le.appendChild( n );

            if ( layer.getAbstract() != null ) {
                n = createElement( OGC_CONTEXT_NS, "Abstract" );
                n.appendChild( createTextNode( layer.getAbstract() ) );
                le.appendChild( n );
            }

            n = createElement( OGC_CONTEXT_NS, "Title" );
            n.appendChild( createTextNode( layer.getTitle() ) );
            le.appendChild( n );

            if ( layer.getMetadataURL() != null ) {
                n = createElement( OGC_CONTEXT_NS, "MetadataURL" );
                le.appendChild( n );
                appendOnlineResource( n, layer.getMetadataURL().getOnlineResource() );
            }

            appendSrs( le, layer.getSrs() );
            appendFormatList( le, layer.getFormatList() );
            appendStyleList( le, layer.getStyleList() );

            appendLayerExtension( le, layer.getExtension() );

            toNode.appendChild( le );
        }

    }

    /**
     * Appends the XML representation of a <code>Server</code> to a <code>Node</code> using the <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param server
     *            the <code>Server</code> to be appended as new element
     * 
     */
    protected static void appendServer( Node toNode, Server server ) {

        if ( server != null ) {
            Element serv = createElement( OGC_CONTEXT_NS, "Server" );

            if ( server.getService() != null ) {
                serv.setAttribute( "service", server.getService() );
            }
            if ( server.getService() != null ) {
                serv.setAttribute( "version", server.getVersion() );
            }
            if ( server.getService() != null ) {
                serv.setAttribute( "title", server.getTitle() );
            }

            appendOnlineResource( serv, server.getOnlineResource() );

            toNode.appendChild( serv );
        }

    }

    /**
     * Appends the XML representation of a list of SRSs as a <code>String[]</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param srsList
     *            the <code>String[]</code> to be appended as new element
     * 
     */
    protected static void appendSrs( Node toNode, String[] srsList ) {

        if ( srsList != null ) {
            StringBuffer sBuf = new StringBuffer( 100 );
            for ( int i = 0; i < srsList.length; i++ ) {
                sBuf.append( srsList[i] );
                if ( i < srsList.length - 1 )
                    sBuf.append( ";" );

            }
            Element e = createElement( OGC_CONTEXT_NS, "SRS" );
            e.appendChild( createTextNode( sBuf.toString() ) );
            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>FormatList</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param formatList
     *            the <code>FormatList</code> to be appended as new element
     * 
     *            contains illegal characters
     */
    protected static void appendFormatList( Node toNode, FormatList formatList ) {

        if ( formatList != null ) {

            Format[] formats = formatList.getFormats();
            if ( formats != null ) {
                Element e = createElement( OGC_CONTEXT_NS, "FormatList" );

                for ( int i = 0; i < formats.length; i++ ) {
                    if ( formats[i] != null ) {
                        Element f = createElement( OGC_CONTEXT_NS, "Format" );
                        f.setAttribute( "current", stringValue01( formats[i].isCurrent() ) );
                        if ( formats[i].getName() != null )
                            f.appendChild( createTextNode( formats[i].getName() ) );
                        e.appendChild( f );
                    }
                }
                toNode.appendChild( e );
            }
        }

    }

    /**
     * Appends the XML representation of a list of a <code>StyleList</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param styleList
     *            the <code>StyleList</code> to be appended as new element
     * 
     */
    protected static void appendStyleList( Node toNode, StyleList styleList ) {

        if ( styleList != null ) {

            Style[] styles = styleList.getStyles();
            if ( styles != null ) {
                Element e = createElement( OGC_CONTEXT_NS, "StyleList" );

                for ( int i = 0; i < styles.length; i++ ) {
                    if ( styles[i] != null ) {
                        Element s = createElement( OGC_CONTEXT_NS, "Style" );
                        s.setAttribute( "current", stringValue01( styles[i].isCurrent() ) );

                        if ( styles[i].getName() != null ) {
                            Element ne = createElement( OGC_CONTEXT_NS, "Name" );
                            ne.appendChild( createTextNode( styles[i].getName() ) );
                            s.appendChild( ne );
                        }
                        if ( styles[i].getTitle() != null ) {
                            Element ne = createElement( OGC_CONTEXT_NS, "Title" );
                            ne.appendChild( createTextNode( styles[i].getTitle() ) );
                            s.appendChild( ne );
                        }
                        if ( styles[i].getAbstract() != null ) {
                            Element ne = createElement( OGC_CONTEXT_NS, "Abstract" );
                            ne.appendChild( createTextNode( styles[i].getAbstract() ) );
                            s.appendChild( ne );
                        }
                        if ( styles[i].getLegendURL() != null && styles[i].getLegendURL().getOnlineResource() != null ) {
                            Element ne = createElement( OGC_CONTEXT_NS, "LegendURL" );
                            ne.setAttribute( "width", String.valueOf( styles[i].getLegendURL().getWidth() ) );
                            ne.setAttribute( "height", String.valueOf( styles[i].getLegendURL().getHeight() ) );
                            ne.setAttribute( "width", String.valueOf( styles[i].getLegendURL().getWidth() ) );
                            appendOnlineResource( ne, styles[i].getLegendURL().getOnlineResource() );
                            s.appendChild( ne );
                        }
                        e.appendChild( s );

                    }
                }
                toNode.appendChild( e );
            }
        }

    }

    /**
     * Appends the XML representation of a list of a <code>ViewContextCollection</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param vcc
     *            the <code>ViewContextCollection</code> to be appended as new element
     * 
     */
    protected static void appendViewContextCollection( Node toNode, ViewContextCollection vcc ) {

        if ( vcc != null ) {
            Element e = createElement( OGC_CONTEXT_NS, "ViewContextCollection" );
            e.setAttributeNode( createAttribute( "xmlns", OGC_CONTEXT_NS.toString() ) );
            e.setAttributeNode( createAttribute( "xmlns:sld", SLD_NS.toString() ) );
            e.setAttributeNode( createAttribute( "xmlns:xlink", XLINK_NS.toString() ) );
            e.setAttributeNode( createAttribute( "xmlns:deegree", D_CONTEXT_NS.toString() ) );
            e.setAttributeNode( createAttribute( "xmlns:xsi", XSI_NS.toString() ) );
            e.setAttributeNode( createAttribute( "version", "1.0.0" ) );

            ViewContextReference[] vcrs = vcc.getViewContextReferences();
            if ( vcrs != null && vcrs.length > 0 ) {
                for ( int i = 0; i < vcrs.length; i++ ) {
                    if ( vcrs[i] != null ) {
                        appendContextReference( e, vcrs[i] );
                    }
                }
            }
            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>ViewContextReference</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * <p/>
     * // TODO implement ID in VCR
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param vcr
     *            the <code>ViewContextReference</code> to be appended as new element
     */
    protected static void appendContextReference( Node toNode, ViewContextReference vcr ) {

        if ( vcr != null ) {
            Element e = createElement( OGC_CONTEXT_NS, "ViewContextReference" );

            e.setAttributeNode( createAttribute( "version", "1.0.0" ) );

            String id = vcr.getTitle().replace( ' ', '_' ).toLowerCase();
            e.setAttributeNode( createAttribute( "id", id ) );

            Element t = createElement( OGC_CONTEXT_NS, "Title" );
            t.appendChild( createTextNode( vcr.getTitle() ) );
            e.appendChild( t );

            if ( vcr.getContextURL() != null ) {
                Element c = createElement( OGC_CONTEXT_NS, "ViewContextURL" );
                appendOnlineResource( c, vcr.getContextURL() );
                e.appendChild( c );
            }
            toNode.appendChild( e );
        }

    }

    /**
     * Creates a String representation ("0" or "1") of a boolean value.
     * 
     * @param value
     *            the input value
     * @return "0" or "1" if value is true or false, respectively
     */
    private static final String stringValue01( boolean value ) {
        return value ? "1" : "0";
    }

    // ***********************************************************************
    // BEGIN Deegree specific methods
    // ***********************************************************************

    /**
     * Appends the XML representation of a list of a <code>GeneralExtension</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param genExt
     *            the <code>GeneralExtension</code> to be appended as new element
     * @throws URISyntaxException
     * @throws DOMException
     * 
     */
    protected static void appendGeneralExtension( Node toNode, GeneralExtension genExt )
                            throws DOMException, URISyntaxException {

        if ( genExt != null ) {
            Element e = createElement( OGC_CONTEXT_NS, "Extension" );
            Element a = createElement( D_CONTEXT_NS, "deegree:Mode" );
            a.appendChild( createTextNode( genExt.getMode() ) );
            e.appendChild( a );

            appendAuthentificationSettings( e, genExt.getAuthentificationSettings() );
            appendIOSettings( e, genExt.getIOSettings() );
            appendFrontend( e, genExt.getFrontend() );
            appendMapParameter( e, genExt.getMapParameter() );

            appendLayerTree( e, genExt.getLayerTreeRoot() );

            appendMapModel( e, genExt.getMapModel() );

            a = createElement( D_CONTEXT_NS, "deegree:XSLT" );
            // TODO
            // we should evaluate if it would be a better solution just to store local file name ...
            a.appendChild( createTextNode( genExt.getXslt().toExternalForm() ) );
            e.appendChild( a );

            toNode.appendChild( e );
        }

    }

    /**
     * @param e
     * @param mapModel
     */
    private static void appendMapModel( Element e, MapModel mapModel ) {
        if ( mapModel != null ) {
            Element mm = createElement( D_CONTEXT_NS, "deegree:MapModel" );
            e.appendChild( mm );
            List<LayerGroup> layerGroups = mapModel.getLayerGroups();
            for ( LayerGroup layerGroup : layerGroups ) {
                appendLayerGroup( mm, layerGroup );
            }
        }
    }

    /**
     * @param mm
     * @param layerGroup
     */
    private static void appendLayerGroup( Element mm, LayerGroup layerGroup ) {
        Element lg = createElement( D_CONTEXT_NS, "deegree:LayerGroup" );
        mm.appendChild( lg );
        lg.setAttribute( "title", layerGroup.getTitle() );
        lg.setAttribute( "identifier", layerGroup.getTitle() );
        lg.setAttribute( "hidden", Boolean.toString( layerGroup.isHidden() ) );
        lg.setAttribute( "expanded", Boolean.toString( layerGroup.isExpanded() ) );
        List<MapModelEntry> mmes = layerGroup.getMapModelEntries();
        for ( MapModelEntry mapModelEntry : mmes ) {
            if ( mapModelEntry instanceof LayerGroup ) {
                appendLayerGroup( lg, (LayerGroup) mapModelEntry );
            } else {
                appendLayer( lg, (MMLayer) mapModelEntry );
            }
        }
    }

    /**
     * @param lg
     * @param layer
     */
    private static void appendLayer( Element lg, MMLayer layer ) {
        Element lay = createElement( D_CONTEXT_NS, "deegree:Layer" );
        lg.appendChild( lay );
        lay.setAttribute( "layerId", layer.getIdentifier() );
    }

    /**
     * Appends the XML representation of a <code>LayerTree</code>.
     * 
     * @param e
     *            the <code>Element</code> to append the new element to
     * @param layerTreeRoot
     *            the root <code>Node</code> of the LayerTree
     * 
     */
    protected static void appendLayerTree( Element e, org.deegree.portal.context.Node layerTreeRoot ) {
        Element layerTree = createElement( D_CONTEXT_NS, "deegree:LayerTree" );
        layerTree.appendChild( getLayerTreeNode( layerTreeRoot ) );
        e.appendChild( layerTree );
    }

    // create LayerTree elements
    private static Element getLayerTreeNode( org.deegree.portal.context.Node node ) {
        Element n = createElement( D_CONTEXT_NS, "deegree:Node" );
        n.setAttributeNode( createAttribute( "id", String.valueOf( node.getId() ) ) );
        n.setAttributeNode( createAttribute( "title", node.getTitle() ) );

        org.deegree.portal.context.Node[] nodes = node.getNodes();
        for ( org.deegree.portal.context.Node childNode : nodes ) {
            // ( tree -> recursion )
            n.appendChild( getLayerTreeNode( childNode ) );
        }
        return n;
    }

    /**
     * @param toNode
     * @param settings
     */
    protected static void appendAuthentificationSettings( Node toNode, AuthentificationSettings settings ) {

        if ( settings != null ) {
            Element e = createElement( D_CONTEXT_NS, "deegree:AuthentificationSettings" );
            Element ee = createElement( D_CONTEXT_NS, "deegree:AuthentificationService" );
            appendOnlineResource( ee, settings.getAuthentificationURL().getOnlineResource() );
            e.appendChild( ee );
            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>IOSettings</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param ioSetts
     *            the <code>IOSettings</code> to be appended as new element
     */
    protected static void appendIOSettings( Node toNode, IOSettings ioSetts ) {

        if ( ioSetts != null ) {
            Element e = createElement( D_CONTEXT_NS, "deegree:IOSettings" );

            // TODO: ioSetts.getTempDirectory() , inexistent till now
            /*
             * if(ioSetts.getRootDirectory() != null ){ Element rd = createElement(namespace,"deegree:TempDirectory");
             * rd.appendChild( createTextNode( ioSetts.getRootDirectory() + "temp")); e.appendChild(rd); }
             */

            appendDirectoryAccess( e, ioSetts.getTempDirectory(), "deegree:TempDirectory" );
            // appendDirectoryAccess( e, ioSetts.getDownloadDirectory(), "deegree:TempDirectory" );
            appendDirectoryAccess( e, ioSetts.getDownloadDirectory(), "deegree:DownloadDirectory" );
            appendDirectoryAccess( e, ioSetts.getSLDDirectory(), "deegree:SLDDirectory" );
            appendDirectoryAccess( e, ioSetts.getPrintDirectory(), "deegree:PrintDirectory" );

            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>DirectoryAccess</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param dirAcc
     *            the <code>DirectoryAccess</code> to be appended as new element
     * @param dirName
     * 
     */
    protected static void appendDirectoryAccess( Node toNode, DirectoryAccess dirAcc, String dirName ) {

        if ( dirAcc != null ) {
            Element d = createElement( D_CONTEXT_NS, dirName );
            if ( dirAcc.getDirectoryName() != null ) {
                Element a = createElement( D_CONTEXT_NS, "deegree:Name" );
                a.appendChild( createTextNode( dirAcc.getDirectoryName() ) );
                d.appendChild( a );

            }
            if ( dirAcc.getOnlineResource() != null ) {
                Element a = createElement( D_CONTEXT_NS, "deegree:Access" );
                appendOnlineResource( a, dirAcc.getOnlineResource() );
                d.appendChild( a );
            }
            toNode.appendChild( d );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>Frontend</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param fEnd
     *            the <code>Frontend</code> to be appended as new element
     * 
     */
    protected static void appendFrontend( Node toNode, Frontend fEnd ) {

        if ( fEnd != null ) {
            Element e = createElement( D_CONTEXT_NS, "deegree:Frontend" );

            e.setAttribute( "scope", "JSP" );
            if ( fEnd.getController() != null ) {
                Element c = createElement( D_CONTEXT_NS, "deegree:Controller" );
                c.appendChild( createTextNode( fEnd.getController() ) );
                e.appendChild( c );
            }
            if ( ( (JSPFrontend) fEnd ).getStyle() != null ) {
                Element c = createElement( D_CONTEXT_NS, "deegree:Style" );
                c.appendChild( createTextNode( ( (JSPFrontend) fEnd ).getStyle() ) );
                e.appendChild( c );
            }
            if ( ( (JSPFrontend) fEnd ).getHeader() != null ) {
                Element c = createElement( D_CONTEXT_NS, "deegree:Header" );
                c.appendChild( createTextNode( ( (JSPFrontend) fEnd ).getHeader() ) );
                e.appendChild( c );
            }
            if ( ( (JSPFrontend) fEnd ).getFooter() != null ) {
                Element c = createElement( D_CONTEXT_NS, "deegree:Footer" );
                c.appendChild( createTextNode( ( (JSPFrontend) fEnd ).getFooter() ) );
                e.appendChild( c );
            }

            appendCommonJS( e, ( (JSPFrontend) fEnd ).getCommonJS() );

            appendButtons( e, ( (JSPFrontend) fEnd ).getButtons() );

            appendGUIArea( e, fEnd.getNorth(), "deegree:North" );
            appendGUIArea( e, fEnd.getWest(), "deegree:West" );
            appendGUIArea( e, fEnd.getCenter(), "deegree:Center" );
            appendGUIArea( e, fEnd.getEast(), "deegree:East" );
            appendGUIArea( e, fEnd.getSouth(), "deegree:South" );

            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>String[]</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param commonJS
     *            the <code>String[]</code> to be appended as new element
     * 
     */
    protected static void appendCommonJS( Node toNode, String[] commonJS ) {

        if ( commonJS != null ) {
            Element c = createElement( D_CONTEXT_NS, "deegree:CommonJS" );

            for ( int i = 0; i < commonJS.length; i++ ) {
                if ( commonJS[i] != null ) {
                    Element n = createElement( D_CONTEXT_NS, "deegree:Name" );
                    n.appendChild( createTextNode( commonJS[i] ) );
                    c.appendChild( n );
                }
            }
            toNode.appendChild( c );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>String</code> to a <code>Node</code> using the
     * <code>namespace</code>. // TODO
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param buttons
     *            the <code>String</code> to be appended as new element
     * 
     */
    protected static void appendButtons( Node toNode, String buttons ) {

        if ( buttons != null ) {
            Element b = createElement( D_CONTEXT_NS, "deegree:Buttons" );
            b.appendChild( createTextNode( buttons ) );

            toNode.appendChild( b );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>GUIArea</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param guiArea
     *            the <code>GUIArea</code> to be appended as new element
     * @param name
     * 
     */
    protected static void appendGUIArea( Node toNode, GUIArea guiArea, String name ) {

        if ( guiArea != null ) {
            Element e = createElement( D_CONTEXT_NS, name );
            e.setAttribute( "hidden", String.valueOf( guiArea.isHidden() ) );
            if ( guiArea.getWidth() > 0 ) {
                e.setAttribute( "width", String.valueOf( guiArea.getWidth() ) );
            }
            if ( guiArea.getHeight() > 0 ) {
                e.setAttribute( "height", String.valueOf( guiArea.getHeight() ) );
            }
            if ( guiArea.getTop() > -1 ) {
                e.setAttribute( "top", String.valueOf( guiArea.getTop() ) );
            }
            if ( guiArea.getRight() > 0 ) {
                e.setAttribute( "right", String.valueOf( guiArea.getRight() ) );
            }
            if ( guiArea.getLeft() > -1 ) {
                e.setAttribute( "left", String.valueOf( guiArea.getLeft() ) );
            }
            if ( guiArea.getBottom() > 0 ) {
                e.setAttribute( "bottom", String.valueOf( guiArea.getBottom() ) );
            }
            e.setAttribute( "closable", String.valueOf( guiArea.isClosable() ).toLowerCase() );
            e.setAttribute( "header", String.valueOf( guiArea.hasHeader() ).toLowerCase() );
            e.setAttribute( "overlay", String.valueOf( guiArea.isOverlay() ).toLowerCase() );

            Module[] mods = guiArea.getModules();
            if ( mods != null ) {
                for ( int i = 0; i < mods.length; i++ ) {
                    if ( mods[i] != null ) {
                        appendModule( e, mods[i] );
                    }
                }
            }

            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>GUIArea</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param mod
     * 
     */
    protected static void appendModule( Node toNode, Module mod ) {

        if ( mod != null ) {
            Element m = createElement( D_CONTEXT_NS, "deegree:Module" );
            m.setAttribute( "hidden", String.valueOf( mod.isHidden() ) );

            m.setAttribute( "type", mod.getType() );
            m.setAttribute( "scrolling", String.valueOf( mod.getScrolling() ) );
            if ( !mod.getWidth().startsWith( "-" ) ) {
                m.setAttribute( "width", String.valueOf( mod.getWidth() ) );
            }
            if ( !mod.getHeight().startsWith( "-" ) ) {
                m.setAttribute( "height", String.valueOf( mod.getHeight() ) );
            }
            if ( mod.getTop() > -1 ) {
                m.setAttribute( "top", String.valueOf( mod.getTop() ) );
            }
            if ( mod.getRight() > 0 ) {
                m.setAttribute( "right", String.valueOf( mod.getRight() ) );
            }
            if ( mod.getLeft() > -1 ) {
                m.setAttribute( "left", String.valueOf( mod.getLeft() ) );
            }
            if ( mod.getBottom() > 0 ) {
                m.setAttribute( "bottom", String.valueOf( mod.getBottom() ) );
            }
            m.setAttribute( "closeable", String.valueOf( mod.isClosable() ).toLowerCase() );
            m.setAttribute( "header", String.valueOf( mod.hasHeader() ).toLowerCase() );
            m.setAttribute( "overlay", String.valueOf( mod.isOverlay() ).toLowerCase() );
            m.setAttribute( "collapsed", String.valueOf( mod.isCollapsed() ).toLowerCase() );

            Element n = createElement( D_CONTEXT_NS, "deegree:Name" );
            n.appendChild( createTextNode( mod.getName() ) );
            m.appendChild( n );

            n = createElement( D_CONTEXT_NS, "deegree:Title" );
            n.appendChild( createTextNode( mod.getTitle() ) );
            m.appendChild( n );

            n = createElement( D_CONTEXT_NS, "deegree:Content" );
            n.appendChild( createTextNode( mod.getContent() ) );
            m.appendChild( n );

            appendModuleJSList( m, mod.getModuleJSList() );
            appendModuleConfiguration( m, mod.getModuleConfiguration() );
            appendParameterList( m, mod.getParameter() );

            toNode.appendChild( m );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>ModuleConfiguration</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param modConf
     *            the <code>ModuleConfiguration</code> to be appended as new element
     * 
     */
    protected static void appendModuleConfiguration( Node toNode, ModuleConfiguration modConf ) {

        if ( modConf != null && modConf.getOnlineResource() != null ) {
            Element e = createElement( D_CONTEXT_NS, "deegree:ModuleConfiguration" );
            appendOnlineResource( e, modConf.getOnlineResource() );
            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>ParameterList</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param parList
     *            the <code>ParameterList</code> to be appended as new element
     * 
     */
    protected static void appendParameterList( Node toNode, ParameterList parList ) {

        if ( parList != null && parList.getParameters().length > 0 ) {

            Element e = createElement( D_CONTEXT_NS, "deegree:ParameterList" );

            Parameter[] pars = parList.getParameters();
            for ( int i = 0; i < pars.length; i++ ) {
                if ( pars[i] != null ) {
                    Element p = createElement( D_CONTEXT_NS, "deegree:Parameter" );

                    Element n = createElement( D_CONTEXT_NS, "deegree:Name" );
                    String name = pars[i].getName();
                    // name = name.substring(0,name.indexOf(':'));
                    n.appendChild( createTextNode( name ) );
                    p.appendChild( n );

                    n = createElement( D_CONTEXT_NS, "deegree:Value" );
                    n.appendChild( createTextNode( pars[i].getValue().toString() ) );
                    p.appendChild( n );

                    e.appendChild( p );
                }
            }
            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>MapParameter</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param mapPar
     *            the <code>MapParameter</code> to be appended as new element
     * 
     */
    protected static void appendMapParameter( Node toNode, MapParameter mapPar ) {

        if ( mapPar != null ) {
            Element e = createElement( D_CONTEXT_NS, "deegree:MapParameter" );

            Element f = createElement( D_CONTEXT_NS, "deegree:OfferedInfoFormats" );
            appendFormats( f, mapPar.getOfferedInfoFormats() );
            e.appendChild( f );

            appendMapOperationFactors( e, mapPar.getOfferedZoomFactors(), "deegree:OfferedZoomFactor" );
            appendMapOperationFactors( e, mapPar.getOfferedPanFactors(), "deegree:OfferedPanFactor" );

            Element minScale = createElement( D_CONTEXT_NS, "deegree:MinScale" );
            minScale.appendChild( createTextNode( String.valueOf( mapPar.getMinScale() ) ) );
            e.appendChild( minScale );

            Element maxScale = createElement( D_CONTEXT_NS, "deegree:MaxScale" );
            maxScale.appendChild( createTextNode( String.valueOf( mapPar.getMaxScale() ) ) );
            e.appendChild( maxScale );

            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>Format[]</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param formats
     *            the <code>Format[]</code> to be appended as new element
     * 
     */
    protected static void appendFormats( Node toNode, Format[] formats ) {

        if ( formats != null ) {
            for ( int i = 0; i < formats.length; i++ ) {
                if ( formats[i] != null ) {
                    Element f = createElement( D_CONTEXT_NS, "deegree:Format" );

                    // TODO is current or selected?
                    if ( formats[i].isCurrent() ) {
                        f.setAttribute( "selected", String.valueOf( formats[i].isCurrent() ) );
                    }

                    f.appendChild( createTextNode( formats[i].getName() ) );
                    toNode.appendChild( f );
                }
            }
        }

    }

    /**
     * Appends the XML representation of a list of a <code>MapOperationFactor</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param mapOpFac
     *            the <code>MapOperationFactor</code> to be appended as new element
     * @param opName
     * 
     */
    protected static void appendMapOperationFactors( Node toNode, MapOperationFactor[] mapOpFac, String opName ) {

        if ( mapOpFac != null ) {
            for ( int i = 0; i < mapOpFac.length; i++ ) {
                if ( mapOpFac[i] != null ) {

                    Element mof = createElement( D_CONTEXT_NS, opName );
                    Element f = createElement( D_CONTEXT_NS, "deegree:Factor" );
                    f.appendChild( createTextNode( String.valueOf( mapOpFac[i].getFactor() ) ) );

                    if ( mapOpFac[i].isSelected() ) {
                        f.setAttribute( "selected", String.valueOf( mapOpFac[i].isSelected() ) );
                    }

                    // TODO isFree ???

                    mof.appendChild( f );
                    toNode.appendChild( mof );
                }
            }
        }

    }

    /**
     * Appends the XML representation of a list of a <code>LayerExtension</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param layExt
     *            the <code>LayerExtension</code> to be appended as new element
     * 
     */
    protected static void appendLayerExtension( Node toNode, LayerExtension layExt ) {

        if ( layExt != null ) {
            Element e = createElement( OGC_CONTEXT_NS, "Extension" );

            appendDataService( e, layExt.getDataService() );

            Element g = createElement( D_CONTEXT_NS, "deegree:MasterLayer" );
            g.appendChild( createTextNode( String.valueOf( layExt.isMasterLayer() ) ) );
            e.appendChild( g );

            g = createElement( D_CONTEXT_NS, "deegree:ScaleHint" );
            g.setAttribute( "min", "" + layExt.getMinScaleHint() );
            g.setAttribute( "max", "" + layExt.getMaxScaleHint() );
            e.appendChild( g );

            g = createElement( D_CONTEXT_NS, "deegree:parentNodeId" );
            g.appendChild( createTextNode( String.valueOf( layExt.getParentNodeId() ) ) );
            e.appendChild( g );

            g = createElement( D_CONTEXT_NS, "deegree:SelectedForQuery" );
            g.appendChild( createTextNode( String.valueOf( layExt.isSelectedForQuery() ) ) );
            e.appendChild( g );

            if ( layExt.getIdentifier() != null ) {
                g = createElement( D_CONTEXT_NS, "deegree:identifier" );
                g.appendChild( createTextNode( String.valueOf( layExt.getIdentifier() ) ) );
                e.appendChild( g );
            }

            g = createElement( D_CONTEXT_NS, "deegree:tiled" );
            g.appendChild( createTextNode( String.valueOf( layExt.isTiled() ) ) );
            e.appendChild( g );
            
            g = createElement( D_CONTEXT_NS, "deegree:valid" );
            g.appendChild( createTextNode( String.valueOf( layExt.isValid() ) ) );
            e.appendChild( g );

            g = createElement( D_CONTEXT_NS, "deegree:UseAuthentication" );
            // System.out.println(layExt.getAuthentication());
            if ( layExt.getAuthentication() == LayerExtension.SESSIONID ) {
                g.appendChild( createTextNode( "sessionID" ) );
            } else if ( layExt.getAuthentication() == LayerExtension.USERPASSWORD ) {
                g.appendChild( createTextNode( "user/password" ) );
            } else if ( layExt.getAuthentication() == LayerExtension.NONE ) {
                g.appendChild( createTextNode( "none" ) );
            }
            e.appendChild( g );

            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>DataService</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param dataServ
     *            the <code>DataService</code> to be appended as new element
     * 
     */
    protected static void appendDataService( Node toNode, DataService dataServ ) {

        if ( dataServ != null ) {
            Element e = createElement( D_CONTEXT_NS, "deegree:DataService" );

            if ( dataServ.getServer() != null ) {
                appendServer( e, dataServ.getServer() );
            }
            String geoType = dataServ.getGeometryType();
            if ( geoType != null ) {
                Element g = createElement( D_CONTEXT_NS, "deegree:GeometryType" );
                g.appendChild( createTextNode( dataServ.getGeometryType() ) );
                e.appendChild( g );
            }
            String featType = dataServ.getFeatureType();
            if ( featType != null ) {
                Element g = createElement( D_CONTEXT_NS, "deegree:FeatureType" );
                g.appendChild( createTextNode( featType ) );
                e.appendChild( g );
            }

            toNode.appendChild( e );
        }

    }

    /**
     * Appends the XML representation of a list of a <code>ParameterList</code> to a <code>Node</code> using the
     * <code>namespace</code>.
     * 
     * @param toNode
     *            the <code>Node</code> to append the new element to
     * @param modJSList
     *            the <code>modJSList</code> to be appended as new element
     * 
     */
    protected static void appendModuleJSList( Node toNode, String[] modJSList ) {

        if ( modJSList != null && modJSList.length > 0 ) {

            for ( int i = 0; i < modJSList.length; i++ ) {
                if ( modJSList[i] != null ) {
                    Element p = createElement( D_CONTEXT_NS, "deegree:ModuleJS" );
                    p.appendChild( createTextNode( modJSList[i] ) );

                    toNode.appendChild( p );
                }
            }
        }

    }

}
