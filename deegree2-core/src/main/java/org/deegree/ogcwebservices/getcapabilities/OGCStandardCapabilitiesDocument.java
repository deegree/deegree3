// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/getcapabilities/CapabilitiesDocument.java,v
// 1.7 2004/06/22 13:25:14 ap Exp $
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
package org.deegree.ogcwebservices.getcapabilities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.ExceptionFormat;
import org.deegree.ogcwebservices.MetadataLink;
import org.deegree.ogcwebservices.MetadataType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 *
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public abstract class OGCStandardCapabilitiesDocument extends OGCCapabilitiesDocument {

    private static final long serialVersionUID = 6122454912283502059L;

    protected static final URI OGCNS = CommonNamespaces.OGCNS;

    protected static final URI GMLNS = CommonNamespaces.GMLNS;

    /**
     * returns the value of the version attribute of the capabilities document
     *
     * @return the value of the version attribute of the capabilities document
     */
    @Override
    public String parseVersion() {
        return this.getRootElement().getAttribute( "version" );
    }

    /**
     * returns the service section of the configuration/capabilities. vendorspecific capabilities
     * are not supported yet
     *
     * @param namespaceURI
     *
     * @return the service section of the configuration/capabilities. vendorspecific capabilities
     *         are not supported yet
     * @throws InvalidCapabilitiesException
     */
    public Capability getCapabilitySection( URI namespaceURI )
                            throws InvalidCapabilitiesException {
        try {
            Node root = this.getRootElement();

            Element element = XMLTools.getRequiredChildElement( "Capability", namespaceURI, root );
            Element elem = XMLTools.getRequiredChildElement( "Request", namespaceURI, element );
            OperationsMetadata request = parseOperations( elem, namespaceURI );

            elem = XMLTools.getRequiredChildElement( "Exception", namespaceURI, element );
            ExceptionFormat eFormat = getExceptionFormat( elem, namespaceURI );

            // vendorspecific capabilities are not supported yet
            // elem = XMLTools.getRequiredChildByName(
            // "VendorSpecificCapabilities", WCSNS, element);

            String version = element.getAttribute( "version" );
            if ( version == null || version.equals( "" ) ) {
                version = this.parseVersion();
            }
            String updateSequence = element.getAttribute( "updateSequence" );
            if ( updateSequence == null || updateSequence.equals( "" ) ) {
                updateSequence = this.getRootElement().getAttribute( "updateSequence" );
            }

            return new Capability( version, updateSequence, request, eFormat, null );

        } catch ( XMLParsingException e ) {
            String s = e.getMessage();
            throw new InvalidCapabilitiesException( "Error while parsing the Capability "
                                                    + "Section of the capabilities\n" + s
                                                    + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * creates a <tt>Request</tt> object (instance of WCSCapabilityRequest) from the passed
     * element encapsulating the Request part of the WCS Capabiliy section
     *
     * @param element
     * @return created <tt>Request</tt>
     * @throws XMLParsingException
     */
    protected abstract OperationsMetadata parseOperations( Element element, URI namespaceURI )
                            throws XMLParsingException;

    /**
     * creates a <tt>MetadataLink</tt> object from the passed element.
     *
     * @param element
     * @return created <tt>MetadataLink</tt>
     * @throws XMLParsingException
     */
    @Override
    protected MetadataLink parseMetadataLink( Element element )
                            throws XMLParsingException {
        if ( element == null )
            return null;

        try {
            URL reference = new URL( XMLTools.getAttrValue( element, "xlink:href" ) );
            String title = XMLTools.getAttrValue( element, "xlink:title" );
            URI about = new URI( XMLTools.getAttrValue( element, null, "about", null ) );
            String tmp = XMLTools.getAttrValue( element, null, "metadataType", null );
            MetadataType metadataType = new MetadataType( tmp );
            return new MetadataLink( reference, title, about, metadataType );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "Couldn't parse metadataLink reference\n"
                                           + StringTools.stackTraceToString( e ) );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "Couldn't parse metadataLink about\n" + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * Creates an <tt>ExceptionFormat</tt> instance from the passed element.
     *
     * @param element
     * @return the new instance
     */
    protected ExceptionFormat getExceptionFormat( Element element, URI namespaceURI ) {
        ElementList el = XMLTools.getChildElements( "Format", namespaceURI, element );
        String[] formats = new String[el.getLength()];
        for ( int i = 0; i < formats.length; i++ ) {
            formats[i] = XMLTools.getStringValue( el.item( i ) );
        }
        return new ExceptionFormat( formats );
    }

    /**
     * Creates a <code>DCPType</code> object from the passed <code>DCPType</code> element.
     * <p>
     * NOTE: Currently the <code>OnlineResources</code> included in the <code>DCPType</code> are
     * just stored as simple <code>URLs</code> (not as <code>OnLineResource</code> instances)!
     * <p>
     * NOTE: In an <code>OGCStandardCapabilitiesDocument</code> the <code>XLinks</code> (the
     * <code>URLs</code>) are stored in separate elements (<code>OnlineResource</code>), in
     * an <code>OGCCommonCapabilitiesDocument</code> they are the
     * <code>Get<code>/<code>Post</code> elements themselves.
     *
     * @param element
     * @param namespaceURI
     * @return created <code>DCPType</code>
     * @throws XMLParsingException
     * @see org.deegree.owscommon.OWSCommonCapabilities
     */
    protected DCPType getDCPType( Element element, URI namespaceURI )
                            throws XMLParsingException {
        try {
            Element elem = XMLTools.getRequiredChildElement( "HTTP", namespaceURI, element );
            ElementList el = XMLTools.getChildElements( "Get", namespaceURI, elem );

            URL[] get = new URL[el.getLength()];
            for ( int i = 0; i < get.length; i++ ) {
                Element ell = XMLTools.getRequiredChildElement( "OnlineResource", namespaceURI, el.item( i ) );
                String s = XMLTools.getRequiredAttrValue( "href", XLNNS, ell );
                get[i] = new URL( s );
            }
            el = XMLTools.getChildElements( "Post", namespaceURI, elem );

            URL[] post = new URL[el.getLength()];
            for ( int i = 0; i < post.length; i++ ) {
                Element ell = XMLTools.getRequiredChildElement( "OnlineResource", namespaceURI, el.item( i ) );
                String s = XMLTools.getRequiredAttrValue( "href", XLNNS, ell );
                post[i] = new URL( s );
            }
            Protocol protocol = new HTTP( get, post );
            return new DCPType( protocol );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "Couldn't parse DCPType onlineresoure URL about\n"
                                           + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * Creates an array of <code>DCPType</code> objects from the passed element list.
     *
     * NOTE: Currently the <code>OnlineResources</code> included in the <code>DCPType</code> are
     * just stored as simple <code>URLs</code> (not as <code>OnLineResource</code> instances)!
     *
     * @param el
     * @param namespaceURI
     * @return the dcp types
     * @throws XMLParsingException
     */
    protected DCPType[] getDCPTypes( ElementList el, URI namespaceURI )
                            throws XMLParsingException {
        DCPType[] dcpTypes = new DCPType[el.getLength()];
        for ( int i = 0; i < dcpTypes.length; i++ ) {
            dcpTypes[i] = getDCPType( el.item( i ), namespaceURI );
        }
        return dcpTypes;
    }
}
