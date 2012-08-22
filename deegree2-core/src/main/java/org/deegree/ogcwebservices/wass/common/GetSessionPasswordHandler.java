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

import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.User;
import org.deegree.security.session.MemoryBasedSessionManager;
import org.deegree.security.session.Session;
import org.deegree.security.session.SessionStatusException;

/**
 * GetSession handler that handles the password method.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class GetSessionPasswordHandler implements GetSessionHandler {

    private final SecurityAccessManager manager;

    private final MemoryBasedSessionManager sessionManager;

    private int sessionLifetime = 0;

    /**
     * Creates new instance using a wass SecurityAccessManager instance to create and instantiate the deegree
     * SecurityAccessManager.
     *
     * @param securityManager
     * @param sessionLifetime
     * @throws GeneralSecurityException
     */
    public GetSessionPasswordHandler( WASSSecurityManager securityManager, int sessionLifetime )
                            throws GeneralSecurityException {
        manager = securityManager.getSecurityAccessManager();
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
        String res = null;
        // password authentication used?
        if ( authData.usesPasswordAuthentication() ) {

            // use manager to authenticate the user with the password
            String user = authData.getUsername();
            String pass = authData.getPassword();
            User usr = manager.getUserByName( user );

            usr.authenticate( pass );

            // create session
            Session session = MemoryBasedSessionManager.createSession( authData.getUsername(), sessionLifetime );
            sessionManager.addSession( session );
            res = session.getSessionID().getId();
        }

        return res;
    }

}
