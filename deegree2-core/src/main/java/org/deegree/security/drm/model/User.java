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
package org.deegree.security.drm.model;

import java.util.HashSet;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.Feature;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityRegistry;
import org.deegree.security.drm.WrongCredentialsException;

/**
 * Implementation of user-objects. <code>User</code> s can be members of <code>Groups</code> and
 * can be associated with <code>Role</code>s.
 * <p>
 * A user is always in one of two states:
 *
 * <ul>
 * <li>
 * Not authenticated: <code>SecurityManager</code> will not issue <code>SecurityAccess</code>
 * instances for this user
 * </li>
 * <li>
 * Authenticated: achieved by calling <code>authenticate()</code> and submitting the correct
 * password, afterwards <code>SecurityAccess</code> instances for the user can be issued
 * </li>
 * </ul>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mays$
 *
 * @version $Revision$, $Date: 21.08.2007 16:51:15$
 */
public class User extends SecurableObject {

    private ILogger LOG = LoggerFactory.getLogger( User.class );

    /**
     *
     */
    public final static int ID_SEC_ADMIN = 1;

    private String password;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private boolean isAuthenticated = false;

    /**
     * Creates a new <code>User</code> -instance.
     *
     * @param id
     * @param name
     * @param password
     *            null means that password checking is disabled
     * @param firstName
     * @param lastName
     * @param emailAddress
     * @param registry
     */
    public User( int id, String name, String password, String firstName, String lastName, String emailAddress,
                 SecurityRegistry registry ) {
        this.id = id;
        this.name = name;
        this.password = password;
        if ( password == null ) {
            isAuthenticated = true;
        }
        if ( lastName == null || firstName == null ) {
            this.title = name;
        } else if ( ( lastName == null || lastName.equals( "" ) ) && ( firstName == null || firstName.equals( "" ) ) ) {
            this.title = name;
        } else if ( ( !lastName.equals( "" ) ) && ( !firstName.equals( "" ) ) ) {
            this.title = lastName + ", " + firstName;
        } else if ( lastName.equals( "" ) ) {
            this.title = firstName;
        } else {
            this.title = lastName;
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.registry = registry;
    }

    /**
     * @return the first name
     *
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the last name
     *
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the mail address
     *
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @return the password
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the groups that this user belongs to.
     *
     * @param securityAccess
     * @return the user's groups
     * @throws GeneralSecurityException
     */
    public Group[] getGroups( SecurityAccess securityAccess )
                            throws GeneralSecurityException {
        return registry.getGroupsForUser( securityAccess, this );
    }

    /**
     * Returns the roles that this is user is associated with (directly and via group memberships).
     * <p>
     *
     * @param securityAccess
     * @return the user's roles
     * @throws GeneralSecurityException
     */
    public Role[] getRoles( SecurityAccess securityAccess )
                            throws GeneralSecurityException {
        return securityAccess.getAllRolesForUser( this );
    }

    /**
     * Returns the <code>Privileges</code> that the <code>User</code> has (directly and via
     * group memberships).
     *
     * @param securityAccess
     * @return the user's privileges
     * @throws GeneralSecurityException
     */
    public Privilege[] getPrivileges( SecurityAccess securityAccess )
                            throws GeneralSecurityException {

        Role[] roles = securityAccess.getAllRolesForUser( this );
        HashSet<Privilege> privilegeSet = new HashSet<Privilege>();
        // gather privileges for all associated roles
        for ( int i = 0; i < roles.length; i++ ) {
            Privilege[] rolePrivileges = registry.getPrivilegesForRole( securityAccess, roles[i] );
            for ( int j = 0; j < rolePrivileges.length; j++ ) {
                privilegeSet.add( rolePrivileges[j] );
            }
        }
        return privilegeSet.toArray( new Privilege[privilegeSet.size()] );
    }

    /**
     * Returns whether the <code>User</code> has a certain <code>Privilege</code> (either
     * directly or via group memberships).
     *
     * @param securityAccess
     * @param privilege
     * @return true if the user has the specified privilege
     * @throws GeneralSecurityException
     */
    public boolean hasPrivilege( SecurityAccess securityAccess, Privilege privilege )
                            throws GeneralSecurityException {
        Privilege[] privileges = getPrivileges( securityAccess );
        for ( int i = 0; i < privileges.length; i++ ) {
            if ( privileges[i].equals( privilege ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the <code>User</code> has a certain privilege (either directly or via group
     * memberships).
     *
     * @param securityAccess
     * @param s
     * @return true if the user has the specified privilege
     * @throws GeneralSecurityException
     */
    public boolean hasPrivilege( SecurityAccess securityAccess, String s )
                            throws GeneralSecurityException {
        Privilege privilege = registry.getPrivilegeByName( securityAccess, s );
        return hasPrivilege( securityAccess, privilege );
    }

    /**
     * Returns the rights that this <code>User</code> has on the given
     * <code>SecurableObject</code> (directly and via group memberships).
     *
     * @param securityAccess
     * @param object
     * @return the user's right for the specified object
     * @throws GeneralSecurityException
     */
    public RightSet getRights( SecurityAccess securityAccess, SecurableObject object, RightType type )
                            throws GeneralSecurityException {
        Role[] roles = securityAccess.getAllRolesForUser( this );
        return new RightSet( registry.getRights( securityAccess, object, roles, type ) );
    }
    
    /**
     * Returns the rights that this <code>User</code> has on the given
     * <code>SecurableObject</code> (directly and via group memberships).
     *
     * @param securityAccess
     * @param object
     * @return the user's right for the specified object
     * @throws GeneralSecurityException
     */
    public RightSet getRights( SecurityAccess securityAccess, SecurableObject object )
                            throws GeneralSecurityException {
        Role[] roles = securityAccess.getAllRolesForUser( this );
        RightSet rights = new RightSet();

        for ( int i = 0; i < roles.length; i++ ) {
            rights = rights.merge( new RightSet( registry.getRights( securityAccess, object, roles[i] ) ) );
        }
        return rights;
    }

    /**
     * Returns whether the <code>User</code> has a certain <code>Right</code> on this
     * <code>SecurableObject</code> (directly or via group memberships).
     *
     * @param securityAccess
     * @param type
     * @param accessParams
     * @param object
     * @return true if the user has the right for the specified object
     * @throws GeneralSecurityException
     */
    public boolean hasRight( SecurityAccess securityAccess, RightType type, Feature accessParams, SecurableObject object )
                            throws GeneralSecurityException {
        LOG.logDebug( "has Right", type );
        LOG.logDebug( "has Right", object );
        //return getRights( securityAccess, object ).applies( object, type, accessParams );
        return getRights( securityAccess, object, type ).applies( object, type, accessParams );
    }

    /**
     * Returns whether the <code>User</code> has a certain <code>Right</code> on this
     * <code>SecurableObject</code> (directly or via group memberships).
     *
     * @param securityAccess
     * @param type
     * @param object
     * @return true if the user has the right for the specified object
     * @throws GeneralSecurityException
     */
    public boolean hasRight( SecurityAccess securityAccess, RightType type, SecurableObject object )
                            throws GeneralSecurityException {
        // TODO
        // must be tested
        return getRights( securityAccess, object ).applies( object, type );
    }

    /**
     * Returns whether the <code>User</code> has a certain right on this
     * <code>SecurableObject</code> (directly or via group memberships).
     *
     * @param securityAccess
     * @param s
     * @param object
     * @return true if the user has the right for the specified object
     * @throws GeneralSecurityException
     */
    public boolean hasRight( SecurityAccess securityAccess, String s, SecurableObject object )
                            throws GeneralSecurityException {
        RightType right = registry.getRightTypeByName( securityAccess, s );
        return hasRight( securityAccess, right, object );
    }

    /**
     * Returns whether the <code>User</code> has already been authenticated by a call to
     * <code>authenticate()</code> with the correct password (or if the <code>user</code>'s
     * password is null).
     *
     * @return true, if the user is authenticated
     */
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Returns a <code>String</code> representation of this object.
     *
     * @param securityAccess
     * @return the object as string
     */
    public String toString( SecurityAccess securityAccess ) {
        StringBuffer sb = new StringBuffer( "Name: " ).append( name ).append( ", Title: " ).append( title );

        try {
            sb.append( ", Groups: [" );
            Group[] groups = getGroups( securityAccess );
            for ( int i = 0; i < groups.length; i++ ) {
                sb.append( groups[i].getName() );
                if ( i != groups.length - 1 ) {
                    sb.append( ", " );
                }
            }
            sb.append( "]" );

            sb.append( ", Roles: [" );
            Role[] roles = getRoles( securityAccess );
            for ( int i = 0; i < roles.length; i++ ) {
                sb.append( roles[i].getName() );
                if ( i != roles.length - 1 ) {
                    sb.append( ", " );
                }
            }
            sb.append( "]" );

            sb.append( ", Privileges: [" );
            Privilege[] privileges = getPrivileges( securityAccess );
            for ( int i = 0; i < privileges.length; i++ ) {
                sb.append( privileges[i].getName() );
                if ( i != privileges.length - 1 ) {
                    sb.append( ", " );
                }
            }
            sb.append( "]" );

        } catch ( GeneralSecurityException e ) {
            LOG.logError( e.getMessage(), e );
        }
        return sb.toString();
    }

    /**
     * Checks if the submitted password is equal to the one of this user instance and sets the state
     * to "authenticated" in case it is correct.
     *
     * @param password
     * @throws WrongCredentialsException
     */
    public void authenticate( String password )
                            throws WrongCredentialsException {
        if ( this.password == null || "".equals( this.password ) ) {
            isAuthenticated = true;
            return;
        }
        if ( !this.password.equals( password ) ) {
            isAuthenticated = false;
            throw new WrongCredentialsException( "The submitted password is incorrect." );
        }
        isAuthenticated = true;
    }
}
