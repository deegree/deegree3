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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
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
public class IPAddressAuthentication extends AbstractAuthentication {

    private static final ILogger LOG = LoggerFactory.getLogger( IPAddressAuthentication.class );

    protected static final String AUTH_PARAM_IPADDRESS = "IPADDRESS";

    protected static final String INIT_PARAM_PATTERN = "pattern";

    /**
     * 
     * @param authenticationName
     * @param initParams
     */
    public IPAddressAuthentication( String authenticationName, Map<String, String> initParams ) {
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
        String tmp = initParams.get( INIT_PARAM_PATTERN );
        List<String> patterns = StringTools.toList( tmp, ",;", true );

        String ipAddress = params.get( AUTH_PARAM_IPADDRESS );
        if ( ipAddress != null ) {
            for ( String p : patterns ) {
                if ( ipAddress.matches( p ) ) {
                    User usr = null;
                    try {
                        SecurityAccessManager sam = SecurityAccessManager.getInstance();
                        // use matching pattern as username and password
                        usr = sam.getUserByName( p );
                        usr.authenticate( null );
                    } catch ( Exception e ) {
                        LOG.logError( e.getMessage() );
                        String msg = Messages.getMessage( "OWSPROXY_USER_AUTH_ERROR", ipAddress );
                        throw new WrongCredentialsException( msg );
                    }
                    return usr;
                }
            }
            throw new WrongCredentialsException( Messages.getMessage( "OWSPROXY_USER_AUTH_ERROR", ipAddress ) );
        }
        return null;
    }

   
}
