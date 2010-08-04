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
package org.deegree.services.authentication;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.deegree.services.authentication.soapheader.SoapHeader;
import org.deegree.services.authentication.soapheader.SoapHeaderXMLAdapter;
import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOAP Authentication.
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SOAPAuthentication implements CredentialsProvider {

    private static Logger LOG = LoggerFactory.getLogger( SOAPAuthentication.class );

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.services.controller.CredentialProvider#doKVP(java.util.Map,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Credentials doKVP( Map<String, String> normalizedKVPParams, HttpServletRequest req,
                              HttpServletResponse response )
                            throws SecurityException {

        throw new UnsupportedOperationException( "KVP Security is not implementable in SOAP!" );

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.services.controller.CredentialProvider#doXML(javax.xml.stream.XMLStreamReader,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Credentials doXML( XMLStreamReader reader, HttpServletRequest req, HttpServletResponse response )
                            throws SecurityException {

        throw new UnsupportedOperationException( "XML Security is not implementable in SOAP!" );

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.services.controller.CredentialProvider#doSOAP(javax.xml.stream.XMLStreamReader,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Credentials doSOAP( SOAPEnvelope soapDoc, HttpServletRequest req )
                            throws SecurityException {

        OMElement requestHeader = soapDoc.getHeader();
        SoapHeaderXMLAdapter soapXMLHeader = new SoapHeaderXMLAdapter();
        soapXMLHeader.setRootElement( requestHeader );
        SoapHeader soapHeader = soapXMLHeader.parseHeader();

        LOG.info( soapHeader.getUsername() + " " + soapHeader.getPassword() );
        return new Credentials( soapHeader.getUsername(), soapHeader.getPassword() );

    }

    public void handleException( HttpServletResponse response, SecurityException e )
                            throws IOException {

        if ( e instanceof InvalidCredentialsException ) {
            doInvalidCredentialsExceptionException( response, (InvalidCredentialsException) e );
        } else if ( e instanceof SecurityException ) {
            doAuthenticationException( response, e );
        }

    }

    /**
     * Handles the authentication.
     * 
     * @param response
     * @param e
     * @throws IOException
     */
    private void doAuthenticationException( HttpServletResponse response, SecurityException e )
                            throws IOException {

        LOG.debug( "WSSE_SecurityException: " );
        response.reset();
        response.resetBuffer();
        response.setHeader( "WWW-Authenticate", "WSSE realm=\" Backroom\", profile=\"UsernameToken\" " );
        response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
        response.flushBuffer();

    }

    /**
     * Handles the authentication.
     * 
     * @param response
     * @param e
     * @throws IOException
     */
    private void doInvalidCredentialsExceptionException( HttpServletResponse response, InvalidCredentialsException e )
                            throws IOException {

        LOG.debug( "exception should respond Forbidden: " );

        response.sendError( 403 );

    }

}
