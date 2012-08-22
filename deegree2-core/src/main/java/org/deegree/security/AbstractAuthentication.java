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
public abstract class AbstractAuthentication {

    private String authenticationName;

    protected Map<String, String> initParams;

    /**
     * 
     * @param authenticationName
     * @param initParams
     */
    public AbstractAuthentication( String authenticationName, Map<String, String> initParams ) {
        this.authenticationName = authenticationName;
        this.initParams = initParams;
    }

    /**
     * returns the name of the authentication method implemented by a class
     * 
     * @return name of the authentication method implemented by a class
     */
    public String getAuthenticationName() {
        return authenticationName;
    }

    /**
     * authenticates a user and returns an instance of {@link User} if authentication has been successfull. Otherwise a
     * WrongCredentialsException will be thrown.
     * 
     * @return an instance of User
     * @param params
     *            authentication parameters
     * @param request
     * @throws WrongCredentialsException
     *             if authentication was not successfull
     */
    public abstract User authenticate( Map<String, String> params, HttpServletRequest request )
                            throws WrongCredentialsException;
}
