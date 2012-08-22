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
package org.deegree.enterprise.servlet;

import static org.deegree.ogcbase.CommonNamespaces.W3SOAP_ENVELOPE;
import static org.deegree.ogcbase.CommonNamespaces.W3SOAP_ENVELOPE_PREFIX;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 */
public class SOAPFacadeServletFilter implements Filter {

    private static ILogger LOG = LoggerFactory.getLogger( SOAPFacadeServletFilter.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * @param filterConfig
     */
    public void init( FilterConfig filterConfig )
                            throws ServletException {
        // nothing to do
    }

    /**
     *
     */
    public void destroy() {
        // nothing to do
    }

    /**
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {
        if ( ( (HttpServletRequest) request ).getMethod().equalsIgnoreCase( "GET" ) ) {
            LOG.logDebug( "simply forward because request method is HTTP GET" );
            chain.doFilter( request, response );
        } else {
            ServletRequestWrapper reqWrapper = new ServletRequestWrapper( (HttpServletRequest) request );

            XMLFragment xml = new XMLFragment();
            try {
                xml.load( reqWrapper.getInputStream(), XMLFragment.DEFAULT_URL );
            } catch ( XMLException e ) {
                LOG.logError( "parsing request as XML", e );
                throw new ServletException( StringTools.stackTraceToString( e ) );
            } catch ( SAXException e ) {
                LOG.logError( "parsing request as XML", e );
                throw new ServletException( StringTools.stackTraceToString( e ) );
            }
            String s = xml.getRootElement().getNamespaceURI();

            // checking if the root elements node name equals the root name of
            // a SOAP message document. If so the SOAP body must be accessed
            // to be forwarded to the the filter/servlet
            if ( s.equals( W3SOAP_ENVELOPE.toASCIIString() ) ) {
                LOG.logDebug( "handle SOAP request" );
                try {
                    handleSOAPRequest( reqWrapper, (HttpServletResponse) response, chain, xml );
                } catch ( Exception e ) {
                    LOG.logError( "handling SOAP request", e );
                    throw new ServletException( StringTools.stackTraceToString( e ) );
                }
            } else {
                LOG.logDebug( "just forward request to next filter or servlet" );
                chain.doFilter( reqWrapper, response );
            }
        }

    }

    /**
     * handles a SOAP request. It is assumed that SOAP messaging has been used and the request to be performed against a
     * OWS is wrapped within the SOAPBody.
     *
     * @param request
     * @param response
     * @param chain
     * @param xmlReq
     * @throws IOException
     * @throws ServletException
     * @throws SAXException
     * @throws XMLParsingException
     */
    private void handleSOAPRequest( HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                    XMLFragment xmlReq )
                            throws IOException, ServletException, SAXException, XMLParsingException {

        XMLFragment sm = null;
        if ( hasMandatoryHeader( xmlReq ) ) {
            sm = handleMustUnderstandFault();
        } else {
            String s = W3SOAP_ENVELOPE_PREFIX + ":Body";
            Element elem = XMLTools.getRequiredElement( xmlReq.getRootElement(), s, nsContext );

            // use first child element
            elem = XMLTools.getElement( elem, "child::*[1]", nsContext );

            // extract SOAPBody and wrap it into a ServletWrapper
            XMLFragment xml = new XMLFragment( elem );

            if ( LOG.isDebug() ) {
                LOG.logDebug( "Extracted request", xml.getAsPrettyString() );
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream( 50000 );
            xml.write( bos );

            ServletRequestWrapper forward = new ServletRequestWrapper( request );
            forward.setInputStreamAsByteArray( bos.toByteArray() );
            bos.close();
            ServletResponseWrapper resWrapper = new ServletResponseWrapper( response );
            chain.doFilter( forward, resWrapper );

            OutputStream os = resWrapper.getOutputStream();
            byte[] b = ( (ServletResponseWrapper.ProxyServletOutputStream) os ).toByteArray();
            os.close();

            sm = createResponseMessage( b );
        }

        response.setContentType( "application/soap+xml" );

        // write into stream to calling client
        OutputStream os = response.getOutputStream();
        sm.write( os );
        os.close();

    }

    private XMLFragment handleMustUnderstandFault()
                            throws SAXException, IOException {
        String s = StringTools.concat( 300, "<?xml version='1.0' encoding='UTF-8'?>",
                                       "<soapenv:Envelope xmlns:soapenv='" + W3SOAP_ENVELOPE + "'><soapenv:Body>",
                                       "<soapenv:Fault><soapenv:Code><soapenv:Value>soapenv:MustUnderstand",
                                       "</soapenv:Value></soapenv:Code><soapenv:Reason><soapenv:Text ",
                                       "xml:lang='en'>One or more mandatory SOAP header blocks not ",
                                       "understood</soapenv:Text></soapenv:Reason></soapenv:Fault>",
                                       "</soapenv:Body></soapenv:Envelope>" );
        StringReader sr = new StringReader( s );
        return new XMLFragment( sr, XMLFragment.DEFAULT_URL );
    }

    /**
     * returns true if the passed SOAP message contains a header that must be understood by a handling node
     *
     * @param xmlReq
     * @return true if the passed SOAP message contains a header that must be understood by a handling node
     * @throws XMLParsingException
     */
    private boolean hasMandatoryHeader( XMLFragment xmlReq )
                            throws XMLParsingException {
        List<Element> list = XMLTools.getElements( xmlReq.getRootElement(), "soap:Header", nsContext );
        for ( Iterator<Element> iter = list.iterator(); iter.hasNext(); ) {
            Element element = iter.next();
            NodeList nl = element.getChildNodes();
            for ( int i = 0; i < nl.getLength(); i++ ) {
                if ( nl.item( i ) instanceof Element ) {
                    Element el = (Element) nl.item( i );
                    if ( XMLTools.getNode( el, "@soap:mustUnderstand", nsContext ) != null ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * private Map collectNamespaces( Element node, Map xmlns ) {
     *
     * NamedNodeMap nnm = node.getAttributes(); for (int i = 0; i < nnm.getLength(); i++) { Node nd = nnm.item( i ); if
     * ( node.getNodeName().startsWith( "xmlns" ) ) { xmlns.put( node.getNodeName(), nd.getNodeValue() ); } } NodeList
     * nl = node.getChildNodes(); for (int i = 0; i < nl.getLength(); i++) { if ( nl.item( i ) instanceof Element ) {
     * xmlns = collectNamespaces( (Element) nl.item( i ), xmlns ); } }
     *
     * return xmlns; }
     */

    /**
     * creates a SOAP message where the response of a OWS call is wrapped whithin the SOAPBody
     *
     * @param b
     *            response to embed into the body of the SOAP response message
     * @return SOAP response message
     */
    private XMLFragment createResponseMessage( byte[] b )
                            throws IOException, SAXException {

        XMLFragment xml = new XMLFragment();
        xml.load( new ByteArrayInputStream( b ), XMLFragment.DEFAULT_URL );

        String s = StringTools.concat( 200, "<?xml version='1.0' encoding='UTF-8'?>",
                                       "<soapenv:Envelope xmlns:soapenv='", W3SOAP_ENVELOPE,
                                       "'><soapenv:Body></soapenv:Body></soapenv:Envelope>" );
        StringReader sr = new StringReader( s );
        XMLFragment message = new XMLFragment( sr, XMLFragment.DEFAULT_URL );

        if ( xml.getRootElement().getLocalName().equals( "ExceptionReport" ) ) {
            Element e = (Element) message.getRootElement().getFirstChild();
            e = XMLTools.appendElement( e, W3SOAP_ENVELOPE, "soapenv:Fault" );
            e = XMLTools.appendElement( e, W3SOAP_ENVELOPE, "soapenv:Detail" );
            XMLTools.insertNodeInto( xml.getRootElement(), e );
        } else {
            XMLTools.insertNodeInto( xml.getRootElement(), message.getRootElement().getFirstChild() );
        }

        /*
         * can not be used for CSW because CSW DE-profile requires SOAP 1.2 MessageFactory factory =
         * MessageFactory.newInstance(); SOAPMessage message = factory.createMessage();
         * message.getSOAPBody().addDocument( doc );
         */

        return message;
    }

}
