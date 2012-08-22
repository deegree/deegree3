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
import org.deegree.security.session.MemoryBasedSessionManager;
import org.deegree.security.session.Session;
import org.deegree.security.session.SessionStatusException;

/**
 * A <code>GetSessionAnonymousHandler</code> class <br/> creates a session for a client and saves
 * it in the local database.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class GetSessionAnonymousHandler implements GetSessionHandler {

    private final MemoryBasedSessionManager sessionManager;

    private final int sessionLifetime;

    /**
     * Creates a sessionHandler which can handle a request for a session without being given any
     * username or password.
     *
     * @param sessionLifetime
     *            the time for a session to be valid
     */
    public GetSessionAnonymousHandler( int sessionLifetime ) {

        this.sessionLifetime = sessionLifetime;
        sessionManager = MemoryBasedSessionManager.getInstance();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wass.common.GetSessionHandler#handleRequest(org.deegree.ogcwebservices.wass.common.GetSession)
     */
    public String handleRequest( GetSession request )
                            throws SessionStatusException, GeneralSecurityException {

        String result = null;
        if ( request.getAuthenticationData().usesSessionAuthentication() ) {
            Session session = new Session( sessionLifetime );
            sessionManager.addSession( session );
            result = session.getSessionID().getId();
        }

        return result;
    }

}
