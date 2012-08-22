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

import org.deegree.i18n.Messages;
import org.deegree.security.session.MemoryBasedSessionManager;
import org.deegree.security.session.SessionStatusException;

/**
 * This class handles CloseSession requests as specified by the GDI NRW Access Control spec V1.0.
 * Note that according to the spec, the response should be empty.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class CloseSessionHandler {

    private MemoryBasedSessionManager sessionManager = null;

    /**
     * Creates new instance that can handle CloseSession requests.
     */
    public CloseSessionHandler() {
        sessionManager = MemoryBasedSessionManager.getInstance();
    }

    /**
     * Closes the session encapsulated in the request.
     *
     * @param request
     *            the request
     * @throws SessionStatusException
     */
    public void handleRequest( CloseSession request )
                            throws SessionStatusException {
        String session = request.getSessionID();
        if ( sessionManager.getSessionByID( session ) == null )
            throw new SessionStatusException( Messages.getMessage( "WASS_ERROR_INVALID_SESSION", request.getServiceName() ) );
        sessionManager.removeSessionByID( session );
    }

}
