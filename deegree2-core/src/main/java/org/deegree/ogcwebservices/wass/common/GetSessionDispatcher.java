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

import static org.deegree.i18n.Messages.getMessage;

import java.util.ArrayList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.session.SessionStatusException;

/**
 * This class handles/dispatches all GetSession requests.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class GetSessionDispatcher implements GetSessionHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( GetSessionDispatcher.class );

    private ArrayList<GetSessionHandler> handlers;

    /**
     * Constructs a new handler ready to process your requests.
     *
     * @param getSessionHandlers
     */
    public GetSessionDispatcher( ArrayList<GetSessionHandler> getSessionHandlers ) {
        this.handlers = getSessionHandlers;
    }

    /**
     * Returns a new session ID.
     *
     * @param request
     *            the request on which to base the authentication
     * @return the new session ID or null, if no authentication took place
     * @throws SessionStatusException
     * @throws GeneralSecurityException
     */
    public String handleRequest( GetSession request )
                            throws SessionStatusException, GeneralSecurityException {
        String res = null;

        for ( GetSessionHandler handler : handlers ) {
            res = handler.handleRequest( request );
            if ( res != null ) // The handler handled the request
                return res;
        }

        // did not find a handler, just return null and log a warning
        StringBuffer msg = new StringBuffer( getMessage( "WASS_ERROR_NO_AUTHMETHOD_HANDLER" ) );
        msg.append( request.getAuthenticationData().getAuthenticationMethod() );
        LOG.logWarning( msg.toString() );

        return res;
    }

}
