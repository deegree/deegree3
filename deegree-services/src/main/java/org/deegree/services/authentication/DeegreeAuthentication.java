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
import java.security.AccessControlException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.soap.SOAPEnvelope;
import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * deegree Authentication.
 * <p>
 * This is a deegree specific authentication that uses the possibilities of KVP to append the username and password as
 * well as the sessionID in the URL.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class DeegreeAuthentication implements CredentialsProvider {

    private static Logger LOG = LoggerFactory.getLogger( DeegreeAuthentication.class );

    @Override
    public Credentials doKVP( Map<String, String> normalizedKVPParams, HttpServletRequest req,
                              HttpServletResponse response )
                            throws SecurityException, AccessControlException {

        // extract (deegree specific) security information and bind to current thread
        String user = normalizedKVPParams.get( "USER" );
        String password = normalizedKVPParams.get( "PASSWORD" );
        String tokenId = normalizedKVPParams.get( "SESSIONID" );

        return new Credentials( user, password, tokenId );
    }

    @Override
    public Credentials doXML( XMLStreamReader xmlStream, HttpServletRequest req, HttpServletResponse response )
                            throws SecurityException {

        // extract (deegree specific) security information and bind to current thread
        String user = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "user" );
        String password = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "password" );
        String tokenId = xmlStream.getAttributeValue( XMLConstants.NULL_NS_URI, "sessionId" );

        return new Credentials( user, password, tokenId );
    }

    @Override
    public Credentials doSOAP( SOAPEnvelope soapDoc, HttpServletRequest req ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handleException( HttpServletResponse response, SecurityException e )
                            throws IOException {
        if ( e instanceof InvalidCredentialsException ) {
            doInvalidCredentialsExceptionException( response, (InvalidCredentialsException) e );
        } else {
            doAuthenticationException( response, e );
        }

    }

    /**
     * Handles the authentication to put credentials into the URL.
     * 
     * @param response
     * @param e
     * @throws IOException
     */
    private void doAuthenticationException( HttpServletResponse response, SecurityException e )
                            throws IOException {

        LOG.debug( "SecurityException: " );
        response.reset();

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
