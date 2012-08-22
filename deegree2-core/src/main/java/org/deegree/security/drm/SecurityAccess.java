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
package org.deegree.security.drm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.Privilege;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.SecurableObject;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.Service;
import org.deegree.security.drm.model.User;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class SecurityAccess {

    protected User user;

    protected SecurityRegistry registry;

    SecurityAccess( User user, SecurityRegistry registry ) {
        this.user = user;
        this.registry = registry;
    }

    /**
     * @return probably the admin user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param name
     * @return the user
     * @throws GeneralSecurityException
     */
    public User getUserByName( String name )
                            throws GeneralSecurityException {
        return registry.getUserByName( this, name );
    }

    /**
     * @param id
     * @return the user
     * @throws GeneralSecurityException
     */
    public User getUserById( int id )
                            throws GeneralSecurityException {
        return registry.getUserById( this, id );
    }

    /**
     * @param name
     * @return the group
     * @throws GeneralSecurityException
     */
    public Group getGroupByName( String name )
                            throws GeneralSecurityException {
        return registry.getGroupByName( this, name );
    }

    /**
     * @param id
     * @return the group
     * @throws GeneralSecurityException
     */
    public Group getGroupById( int id )
                            throws GeneralSecurityException {
        return registry.getGroupById( this, id );
    }

    /**
     * @param name
     * @return the role
     * @throws GeneralSecurityException
     */
    public Role getRoleByName( String name )
                            throws GeneralSecurityException {
        return registry.getRoleByName( this, name );
    }

    /**
     * @param ns
     * @return the roles
     * @throws GeneralSecurityException
     */
    public Role[] getRolesByNS( String ns )
                            throws GeneralSecurityException {
        return registry.getRolesByNS( this, ns );
    }

    /**
     * @param id
     * @return the role
     * @throws GeneralSecurityException
     */
    public Role getRoleById( int id )
                            throws GeneralSecurityException {
        return registry.getRoleById( this, id );
    }

    /**
     * @param name
     * @return the right
     * @throws GeneralSecurityException
     */
    public RightType getRightByName( String name )
                            throws GeneralSecurityException {
        return registry.getRightTypeByName( this, name );
    }

    /**
     * @param name
     * @return the privilege
     * @throws GeneralSecurityException
     */
    public Privilege getPrivilegeByName( String name )
                            throws GeneralSecurityException {
        return registry.getPrivilegeByName( this, name );
    }

    /**
     * @param id
     * @return the object
     * @throws GeneralSecurityException
     */
    public SecuredObject getSecuredObjectById( int id )
                            throws GeneralSecurityException {
        return registry.getSecuredObjectById( this, id );
    }

    /**
     * @param name
     * @param type
     * @return the object
     * @throws GeneralSecurityException
     */
    public SecuredObject getSecuredObjectByName( String name, String type )
                            throws GeneralSecurityException {
        return registry.getSecuredObjectByName( this, name, type );
    }

    /**
     * @param ns
     * @param type
     * @return the objects
     * @throws GeneralSecurityException
     */
    public SecuredObject[] getSecuredObjectsByNS( String ns, String type )
                            throws GeneralSecurityException {
        return registry.getSecuredObjectsByNS( this, ns, type );
    }

    /**
     * @return the users
     * @throws GeneralSecurityException
     */
    public User[] getAllUsers()
                            throws GeneralSecurityException {
        return registry.getAllUsers( this );
    }

    /**
     * @return all groups
     * @throws GeneralSecurityException
     */
    public Group[] getAllGroups()
                            throws GeneralSecurityException {
        return registry.getAllGroups( this );
    }

    /**
     * @param type
     * @return all secured objects
     * @throws GeneralSecurityException
     */
    public SecuredObject[] getAllSecuredObjects( String type )
                            throws GeneralSecurityException {
        return registry.getAllSecuredObjects( this, type );
    }

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code>, except those that are only used internally
     * (these have a namespace starting with the $ symbol);
     * 
     * @return all roles
     * 
     * @throws GeneralSecurityException
     */
    public Role[] getAllRoles()
                            throws GeneralSecurityException {
        return registry.getAllRoles( this );
    }

    /**
     * Returns all <code>Role</code> s that the given <code>User</code> is associated with (directly and via group
     * memberships).
     * 
     * @param user
     * @return all his roles
     * @throws GeneralSecurityException
     */
    public Role[] getAllRolesForUser( User user )
                            throws GeneralSecurityException {
        // long l = System.currentTimeMillis();
        // counts number of accesses against SecurityRegistry
        int count = 0;
        HashSet<Group> allGroups = new HashSet<Group>();
        Group[] groups = registry.getGroupsForUser( this, user );
        count++;
        for ( Group group : groups ) {
            allGroups.add( group );
        }

        groups = registry.getGroupsForGroups( this, allGroups.toArray( new Group[allGroups.size()] ) );
        count++;
        List<Group> tmp = new ArrayList<Group>( 200 );
        do {
            tmp.clear();
            for ( Group group : groups ) {
                if ( !allGroups.contains( group ) ) {
                    allGroups.add( group );
                    tmp.add( group );
                }
            }
            if ( tmp.size() > 0 ) {
                groups = registry.getGroupsForGroups( this, tmp.toArray( new Group[tmp.size()] ) );
                count++;
            }
        } while ( tmp.size() > 0 );

        // Stack<Group> groupStack = new Stack<Group>();
        // for ( int i = 0; i < groups.length; i++ ) {
        // groupStack.push( groups[i] );
        // }
        //
        // // collect all groups that user is member of
        // while ( !groupStack.isEmpty() ) {
        // Group currentGroup = groupStack.pop();
        //
        // allGroups.add( currentGroup );
        // groups = registry.getGroupsForGroup( this, currentGroup );
        // count++;
        // for ( int i = 0; i < groups.length; i++ ) {
        // if ( !allGroups.contains( groups[i] ) ) {
        // allGroups.add( groups[i] );
        // groupStack.push( groups[i] );
        // }
        // }
        // }

        HashSet<Role> allRoles = new HashSet<Role>();

        // add all directly associated roles
        Role[] roles = registry.getRolesForUser( this, user );
        count++;
        for ( int i = 0; i < roles.length; i++ ) {
            allRoles.add( roles[i] );
        }

        // add all roles that are associated via group membership
        // Iterator<Group> it = allGroups.iterator();
        // while ( it.hasNext() ) {
        // Group group = it.next();
        // roles = registry.getRolesForGroup( this, group );
        // count++;
        // for ( int i = 0; i < roles.length; i++ ) {
        // allRoles.add( roles[i] );
        // }
        // }
        roles = registry.getRolesForGroups( this, allGroups.toArray( new Group[allGroups.size()] ) );
        for ( Role role : roles ) {
            allRoles.add( role );
        }
        // System.out.println( "=========  getAllRolesForUser ===============" );
        // System.out.println( count );
        // System.out.println( ( System.currentTimeMillis() - l ) );
        // System.out.println( "=============================================" );
        return allRoles.toArray( new Role[allRoles.size()] );
    }

    /**
     * Returns all <code>Role</code> s that the given <code>Group</code> is associated with (directly and via group
     * memberships).
     * 
     * @param group
     * @return all their roles
     * @throws GeneralSecurityException
     */
    public Role[] getAllRolesForGroup( Group group )
                            throws GeneralSecurityException {

        HashSet<Group> allGroups = new HashSet<Group>();
        Stack<Group> groupStack = new Stack<Group>();
        groupStack.push( group );

        while ( !groupStack.isEmpty() ) {
            Group currentGroup = groupStack.pop();
            Group[] groups = registry.getGroupsForGroup( this, currentGroup );
            for ( int i = 0; i < groups.length; i++ ) {
                if ( !allGroups.contains( groups[i] ) ) {
                    allGroups.add( groups[i] );
                    groupStack.push( groups[i] );
                }
            }
        }

        HashSet<Role> allRoles = new HashSet<Role>();
        Iterator<Group> it = allGroups.iterator();
        while ( it.hasNext() ) {
            Role[] roles = registry.getRolesForGroup( this, it.next() );
            for ( int i = 0; i < roles.length; i++ ) {
                allRoles.add( roles[i] );
            }
        }
        return allRoles.toArray( new Role[allRoles.size()] );
    }

    /**
     * Tries to find a cyle in the groups relations of the <code>Registry</code>.
     * 
     * @return indicates the cycle's nodes (groups)
     * @throws GeneralSecurityException
     */
    public Group[] findGroupCycle()
                            throws GeneralSecurityException {
        Group[] allGroups = getAllGroups();
        for ( int i = 0; i < allGroups.length; i++ ) {
            Stack<Group> path = new Stack<Group>();
            if ( findGroupCycle( allGroups[i], path ) ) {
                return path.toArray( new Group[path.size()] );
            }
        }
        return null;
    }

    /**
     * Recursion part for the <code>findGroupCycle</code> -algorithm.
     * <p>
     * Modified depth first search.
     * 
     * @param group
     * @param path
     * @return true if cycle
     * @throws GeneralSecurityException
     */
    private boolean findGroupCycle( Group group, Stack<Group> path )
                            throws GeneralSecurityException {
        if ( path.contains( group ) ) {
            path.push( group );
            return true;
        }
        path.push( group );
        Group[] members = registry.getGroupsForGroup( this, group );
        for ( int i = 0; i < members.length; i++ ) {
            if ( findGroupCycle( members[i], path ) ) {
                return true;
            }
        }
        path.pop();
        return false;
    }

    /**
     * Checks if the associated <code>User</code> has a certain <code>Privilege</code>.
     * 
     * @param privilege
     * @throws GeneralSecurityException
     *             if holder does not have the privilege
     */
    protected void checkForPrivilege( Privilege privilege )
                            throws GeneralSecurityException {
        if ( !user.hasPrivilege( this, privilege ) ) {
            throw new GeneralSecurityException( "The requested operation requires the privilege '"
                                                + privilege.getName() + "'." );
        }
    }

    /**
     * Checks if the associated <code>User</code> has a certain <code>Right</code> on the given
     * <code>SecurableObject</code>.
     * 
     * @param right
     * @param object
     * @throws GeneralSecurityException
     *             this is a UnauthorizedException if the holder does not have the right
     */
    protected void checkForRight( RightType right, SecurableObject object )
                            throws UnauthorizedException, GeneralSecurityException {
        if ( !user.hasRight( this, right, object ) ) {
            throw new UnauthorizedException( "The requested operation requires the right '" + right.getName()
                                             + "' on the object '" + object.getName() + "'." );
        }
    }

    /**
     * @param address
     * @return the service
     * @throws GeneralSecurityException
     */
    public Service getServiceByAddress( String address )
                            throws GeneralSecurityException {
        return registry.getServiceByAddress( this, address );
    }

    public Service getServiceById( int id )
                            throws GeneralSecurityException {
        return registry.getServiceById( this, id );
    }

    /**
     * @return all services
     * @throws GeneralSecurityException
     */
    public LinkedList<Service> getAllServices()
                            throws GeneralSecurityException {
        return registry.getAllServices( this );
    }

    /**
     * @param role
     * @return the appropriate services
     * @throws GeneralSecurityException
     */
    public LinkedList<Service> getRolesServices( Role role )
                            throws GeneralSecurityException {
        return registry.getRolesServices( this, role );
    }

    public boolean hasServiceRight( Service service, Role role, RightType right )
                            throws GeneralSecurityException {
        return registry.hasServiceRight( this, service, role, right );
    }

    public String getConstraints( Role role, Service service )
                            throws GeneralSecurityException {
        return registry.getConstraints( this, role, service );
    }
}
