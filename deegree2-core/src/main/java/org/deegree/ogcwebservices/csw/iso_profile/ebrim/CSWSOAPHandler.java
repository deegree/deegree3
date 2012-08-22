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
package org.deegree.ogcwebservices.csw.iso_profile.ebrim;

import java.net.MalformedURLException;
import java.security.InvalidParameterException;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.CSWExceptionCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The <code>CSWSOAPFilter</code> class parses an incoming soap - (post)request which (if the root element is bound to
 * the soap name space "http://www.w3.org/2003/05/soap-envelope") will copy the csw:Operation inside the envelope. Sets
 * the user name and passwords inside the root element of the csw:reqquest and sends it to the chain.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class CSWSOAPHandler {
    private static ILogger LOG = LoggerFactory.getLogger( CSWSOAPHandler.class );

    private XMLFragment incomingRequest;

    private boolean isSOAPRequest;

    private String userName = "anonymous";

    private String password = "";

    /**
     * Create an empty CSWSoapHandler with username set to anonymous and password to "". No xml-data is set.
     */
    public CSWSOAPHandler() {
        incomingRequest = null;
        isSOAPRequest = false;
    }

    /**
     * Simple constructor buffering the request.
     *
     * @param incomingRequest
     */
    public CSWSOAPHandler( XMLFragment incomingRequest ) {
        if ( incomingRequest == null ) {
            throw new InvalidParameterException( "The incomingRequest parameter may not be null" );
        }
        this.incomingRequest = incomingRequest;
        String ns = incomingRequest.getRootElement().getNamespaceURI();
        LOG.logDebug( " The namespace of the rootelement = " + ns );
        isSOAPRequest = CommonNamespaces.W3SOAP_ENVELOPE.toASCIIString().equals( ns );
    }

    /**
     * @return true if the namespace of the root element is 'http://www.w3.org/2003/05/soap-envelope' false otherwise.
     */
    public boolean isSOAPRequest() {
        return isSOAPRequest;
    }

    /**
     * Finds a user and a password from a given soap request, and inserts them as attributes into the rootelement of the
     * csw:request inside the body of the soap. This request is returned.
     *
     * @return the XMLTree inside the soap:body or <code>null</code> if the element has no localname of Envelope, or
     *         the data (aka as doc) was not set.
     * @throws OGCWebServiceException
     *             if some required elements are not set.
     */
    public XMLFragment createCSWRequestFromSOAP()
                            throws OGCWebServiceException {
        if ( !isSOAPRequest ) {
            LOG.logDebug( " not a soaprequest, so returning the unparsed xmlfragment." );
            return this.incomingRequest;
        }
        if ( incomingRequest == null ) {
            LOG.logDebug( " no data was set, so returning null." );
            return null;
        }

        Element rootElement = this.incomingRequest.getRootElement();

        String localName = rootElement.getLocalName();
        if ( localName == null || !"Envelope".equals( localName ) ) {
            LOG.logDebug( " The localname of the root element is not: 'Envelope', so not parsing the SOAP request. " );
            throw new OGCWebServiceException(
                                              "A SOAP-Request must contain the root element with a localname of 'Envelope', you supplied: "
                                                                      + localName, CSWExceptionCode.WRS_INVALIDREQUEST );
        }
        Element header = null;
        Element cswRequest = null;
        try {
            header = XMLTools.getRequiredElement( rootElement, CommonNamespaces.W3SOAP_ENVELOPE_PREFIX + ":Header",
                                                  CommonNamespaces.getNamespaceContext() );
            Element body = XMLTools.getRequiredElement( rootElement, CommonNamespaces.W3SOAP_ENVELOPE_PREFIX + ":Body",
                                                        CommonNamespaces.getNamespaceContext() );
            List<Element> cswRequests = XMLTools.getElements( body, "*", CommonNamespaces.getNamespaceContext() );
            if ( cswRequests.size() != 1 ) {
                StringBuffer sb = new StringBuffer();
                for ( int i = 0; i < cswRequests.size(); ++i ) {
                    sb.append( cswRequests.get( i ).getLocalName() + ( ( ( i + 1 ) < cswRequests.size() ) ? ", " : "" ) );
                }
                LOG.logDebug( " Not enough csw requests found in the body of the soap envelope, found: "
                              + sb.toString() );
                throw new OGCWebServiceException(
                                                  "You have not supplied exactly one csw requests in the body of the soap envelope (you supplied:  "
                                                                          + sb.toString()
                                                                          + "), only one request per soap envelope is allowed.",
                                                  CSWExceptionCode.WRS_INVALIDREQUEST );
            }
            cswRequest = cswRequests.get( 0 );
            LOG.logDebug( " found csw request: " + cswRequest.getLocalName() );

        } catch ( XMLParsingException e ) {
            LOG.logDebug( " No soap:header or soap:body found: " + e.getMessage() );
            throw new OGCWebServiceException(
                                              "While parsing your soap request, a required element (soap:Header or soap:Body) wasn't found, the exact error message is: "
                                                                      + e.getMessage(),
                                              CSWExceptionCode.WRS_INVALIDREQUEST );

        }

        try {
            userName = XMLTools.getNodeAsString( header, CommonNamespaces.DGSEC_PREFIX + ":user",
                                                 CommonNamespaces.getNamespaceContext(), "default" );
            password = XMLTools.getNodeAsString( header, CommonNamespaces.DGSEC_PREFIX + ":password",
                                                 CommonNamespaces.getNamespaceContext(), "default" );
        } catch ( XMLParsingException e ) {
            LOG.logDebug( " No deegreesec:user/deegreesec:password found: " + e.getMessage() );
            throw new OGCWebServiceException(
                                              "While parsing your soap request, a required element (deegreesec:user/deegreesec:password) wasn't found, the exact error message is: "
                                                                      + e.getMessage(),
                                              CSWExceptionCode.WRS_INVALIDREQUEST );
        }

        LOG.logDebug( " found user: " + userName );
        LOG.logDebug( " found password: " + password );

        String ns = cswRequest.getNamespaceURI();
        if ( !CommonNamespaces.CSWNS.toASCIIString().equals( ns ) ) {
            LOG.logDebug( " The namespace of the enveloped request-root element is not: " + CommonNamespaces.CSWNS
                          + " so not using the SOAP servlet filter. " );
            throw new OGCWebServiceException( "The namespace of the enveloped request-root element is not: "
                                              + CommonNamespaces.CSWNS, CSWExceptionCode.WRS_INVALIDREQUEST );
        }

        Document newRequestDoc = XMLTools.create();
        Node a = newRequestDoc.importNode( cswRequest, true );
        if ( a.getNodeType() != Node.ELEMENT_NODE ) {
            throw new OGCWebServiceException( "The copied request is not of type w3c.ELEMENT_NODE, this cannot be.",
                                              CSWExceptionCode.WRS_INVALIDREQUEST );
        }
        Element insertedRequest = (Element) newRequestDoc.appendChild( a );
        insertedRequest.setAttribute( "user", userName );
        insertedRequest.setAttribute( "password", password );

        XMLFragment frag = null;
        try {
            frag = new XMLFragment( newRequestDoc, XMLFragment.DEFAULT_URL );
            XMLTools.appendNSBinding( frag.getRootElement(), "rim", CommonNamespaces.OASIS_EBRIMNS );
            if ( LOG.isDebug() ) {
                LOG.logDebug( " the resulting request:\n " + frag.getAsPrettyString() );
            }
        } catch ( MalformedURLException e ) {
            // never happens because using default url
        }
        return frag;
    }

    /**
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param doc
     *            sets the incomingRequest to doc.
     */
    public void setIncomingRequest( XMLFragment doc ) {
        if ( doc == null ) {
            throw new InvalidParameterException( "The incomingRequest parameter may not be null" );
        }
        incomingRequest = doc;
        String ns = incomingRequest.getRootElement().getNamespaceURI();
        LOG.logDebug( " The namespace of the rootelement = " + ns );
        isSOAPRequest = CommonNamespaces.W3SOAP_ENVELOPE.toASCIIString().equals( ns );

    }

}
