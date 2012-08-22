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
package org.deegree.ogcwebservices.wass.common;

import static org.deegree.framework.log.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.session.MemoryBasedSessionManager;
import org.deegree.security.session.Session;
import org.deegree.security.session.SessionID;
import org.deegree.security.session.SessionStatusException;
import org.xml.sax.SAXException;

/**
 * GetSession handler that handles the password method using a remote WAS.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class GetSessionWASHandler implements GetSessionHandler {

    private static final ILogger LOG = getLogger( GetSessionWASHandler.class );

    private String url;

    private MemoryBasedSessionManager sessionManager;

    private int sessionLifetime;

    /**
     * Creates new instance using a wass SecurityAccessManager instance to create and instantiate the deegree
     * SecurityAccessManager.
     *
     * @param address
     * @param sessionLifetime
     *
     * @throws GeneralSecurityException
     */
    public GetSessionWASHandler( OnlineResource address, int sessionLifetime ) throws GeneralSecurityException {
        url = address.getLinkage().getHref().toExternalForm();
        sessionManager = MemoryBasedSessionManager.getInstance();
        this.sessionLifetime = sessionLifetime;
    }

    /**
     * Handles only requests with password authentication method.
     *
     * @return a string with a session ID or null, if the method of the request is not password
     * @see org.deegree.ogcwebservices.wass.common.GetSessionHandler#handleRequest(org.deegree.ogcwebservices.wass.common.GetSession)
     */
    public String handleRequest( GetSession request )
                            throws SessionStatusException, GeneralSecurityException {

        AuthenticationData authData = request.getAuthenticationData();

        if ( authData.usesPasswordAuthentication() ) {

            String url = this.url;
            url += ( url.endsWith( "?" ) || url.endsWith( "&" ) ) ? "" : "?";
            url += "request=GetSession&service=WAS&version=1.0.0&authmethod=urn:x-gdi-nrw:authnMethod:1.0:password&credentials=";
            url += authData.getUsername() + "," + authData.getPassword();

            try {
                URLConnection conn = new URL( url ).openConnection();
                String contentType = conn.getContentType();
                if ( contentType != null && contentType.startsWith( "application/vnd.ogc.se_xml" ) ) {
                    LOG.logError( "Could not connect to remote WAS service. Service exception was:" );
                    try {
                        LOG.logError( new XMLFragment( new InputStreamReader( conn.getInputStream() ), url ).getAsPrettyString() );
                    } catch ( SAXException e ) {
                        LOG.logError( "(the service exception could not be parsed)" );
                    }

                    return null;
                }

                BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
                String sessionId = in.readLine().trim();
                if ( sessionId.length() == 0 ) {
                    LOG.logError( "The configured WAS address probably points to a non-existing Tomcat context. The given location returns empty content." );
                    return null;
                }

                sessionManager.addSession( new Session( new SessionID( sessionId, sessionLifetime ) ) );

                return sessionId;

            } catch ( IOException e ) {
                LOG.logError( "Could not connect to remote WAS service. IO exception that occured: ", e );
            }
        }

        return null;
    }

}
