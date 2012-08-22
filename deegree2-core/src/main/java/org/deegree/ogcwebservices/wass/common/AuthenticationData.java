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

/**
 * Encapsulated data: authn:AuthenticationData element
 *
 * Namespace: http://www.gdi-nrw.org/authentication
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class AuthenticationData {

    private URN authenticationMethod = null;

    private String credentials = null;

    /**
     * @param authenticationMethod
     * @param credentials
     */
    public AuthenticationData( URN authenticationMethod, String credentials ) {
        this.authenticationMethod = authenticationMethod;
        this.credentials = credentials;
    }

    /**
     * @return the Method of authentication
     * @see org.deegree.ogcwebservices.wass.common.URN
     */
    public URN getAuthenticationMethod() {
        return authenticationMethod;
    }

    /**
     * @return the Credentials
     */
    public String getCredentials() {
        return credentials;
    }

    /**
     * @return true, if authenticationMethod is by password
     */
    public boolean usesPasswordAuthentication() {
        return authenticationMethod.isWellformedGDINRW()
               && authenticationMethod.getAuthenticationMethod().equals( "password" );
    }

    /**
     * @return true, if authenticationMethod is by session
     */
    public boolean usesSessionAuthentication() {
        return authenticationMethod.isWellformedGDINRW()
               && authenticationMethod.getAuthenticationMethod().equals( "session" );
    }

    /**
     * @return true, if authenticationMethod is by anonymous
     */
    public boolean usesAnonymousAuthentication() {
        return authenticationMethod.isWellformedGDINRW()
               && authenticationMethod.getAuthenticationMethod().equals( "anonymous" );
    }

    /**
     * @return the username of the credentials or null, if authenticationMethod is not password
     */
    public String getUsername() {
        if ( !usesPasswordAuthentication() ) {
            return null;
        }
        if ( credentials.indexOf( ',' ) > 0 ) {
            return credentials.substring( 0, credentials.indexOf( ',' ) );
        }
        return credentials;
    }

    /**
     * @return the password of the credentials or null, if authenticationMethod is not password
     */
    public String getPassword() {
        if ( !usesPasswordAuthentication() || credentials.indexOf( ',' ) < 0 ) {
            return null;
        }
        return credentials.substring( credentials.indexOf( ',' ) + 1 );

    }

}
