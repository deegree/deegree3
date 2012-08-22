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

package org.deegree.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.WrongCredentialsException;
import org.deegree.security.drm.model.User;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class UserPrincipalAuthentication extends AbstractAuthentication {

    private static final ILogger LOG = LoggerFactory.getLogger( UserPrincipalAuthentication.class );

    protected static final String AUTH_PARAM_USERPRINCIPAL = "USERPRINCIPAL";

    /**
     * 
     * @param authenticationName
     * @param initParams
     */
    public UserPrincipalAuthentication( String authenticationName, Map<String, String> initParams ) {
        super( authenticationName, initParams );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.security.AbstractAuthentication#authenticate(java.util.Map,
     * javax.servlet.http.HttpServletRequest)
     */
    public User authenticate( Map<String, String> params, HttpServletRequest request )
                            throws WrongCredentialsException {
        String user = params.get( AUTH_PARAM_USERPRINCIPAL );

        LOG.logDebug( "USER: ", user );
        User usr = null;
        if ( user != null ) {
            try {
                SecurityAccessManager sam = SecurityAccessManager.getInstance();
                usr = sam.getUserByName( user );
                try {
                    usr.authenticate( null );
                } catch ( Exception e ) {
                    LOG.logWarning( "no authentication of user: " + user + " without password" );
                }
                if ( !usr.isAuthenticated() ) {
                    LOG.logDebug( "authenticate user: " + user + " with password: " + user );
                    usr.authenticate( user );
                }
            } catch ( Exception e ) {
                LOG.logWarning( "no authentication of user: " + user + " with password: " + user );
                LOG.logError( e.getMessage() );
                throw new WrongCredentialsException( Messages.getMessage( "OWSPROXY_USER_AUTH_ERROR", user ) );
            }
        }

        return usr;
    }

}
