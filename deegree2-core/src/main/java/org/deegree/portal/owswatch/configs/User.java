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

package org.deegree.portal.owswatch.configs;

import java.io.Serializable;
import java.util.List;

/**
 * A Class to hold the information of an owsWatch user
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class User implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8831467227767271591L;

    private String username = null;

    private String password = null;

    private String firstName = null;

    private String lastName = null;

    private String email = null;

    private List<String> roles = null;

    /**
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @param email
     *            The email will be used to send error emails in case a service failed
     * @param roles
     *            indicates whether a user is allowed to add/edit services, or just watch them
     */
    public User( String username, String password, String firstName, String lastName, String email, List<String> roles ) {

        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
    }

    /**
     * @return String
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return String
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return String
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return String
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return List<String>
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * @return String
     */
    public String getUsername() {
        return username;
    }
}
