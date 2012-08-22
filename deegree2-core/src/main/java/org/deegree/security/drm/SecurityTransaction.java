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

import static org.deegree.security.drm.SecurityAccessManager.getInstance;
import static org.deegree.security.drm.model.Privilege.ADDOBJECT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.deegree.framework.util.StringPair;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.Privilege;
import org.deegree.security.drm.model.Right;
import org.deegree.security.drm.model.RightSet;
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
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SecurityTransaction extends SecurityAccess {

    private Role adminRole;

    private long timestamp;

    /**
     * @param user
     * @param registry
     * @param adminRole
     */
    SecurityTransaction( User user, SecurityRegistry registry, Role adminRole ) {
        super( user, registry );
        this.adminRole = adminRole;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returns the conjunction of an array of roles plus a single role.
     * 
     * @param roles
     * @param role
     * @return the conjunction of an array of roles plus a single role.
     */
    public Role[] addRoles( Role[] roles, Role role ) {
        HashSet<Role> roleSet = new HashSet<Role>( roles.length + 1 );
        roleSet.add( role );
        for ( int i = 0; i < roles.length; i++ ) {
            roleSet.add( roles[i] );
        }
        return roleSet.toArray( new Role[roleSet.size()] );
    }

    /**
     * Deletes all data from the underlying <code>Registry</code> and sets the default objects (SEC_ADMIN user, role and
     * group) and standard rights and privileges.
     * 
     * @throws GeneralSecurityException
     */
    public void clean()
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        registry.clean( this );
    }

    /**
     * Removes a <code>Group</code> from the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>owner role ($G:GROUPNAME) is removed
     * <li>group is removed
     * </ul>
     * 
     * NOTE: Only performed if the acting user has the 'delete'-right on the group object.
     * 
     * @param group
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void deregisterGroup( Group group )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.DELETE, group );
        try {
            Role ownerRole = registry.getRoleByName( this, "$G:" + group.getName() );
            registry.deregisterRole( this, ownerRole );
        } catch ( UnknownException e ) {
        }
        registry.deregisterGroup( this, group );
    }

    /**
     * Removes a <code>Role</code> from the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>owner role ($R:ROLENAME) is removed
     * <li>role is removed
     * </ul>
     * 
     * NOTE: Only performed if acting user has the 'delete'-right on the role object.
     * 
     * @param role
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void deregisterRole( Role role )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.DELETE, role );
        try {
            Role ownerRole = registry.getRoleByName( this, "$R:" + role.getName() );
            registry.deregisterRole( this, ownerRole );
        } catch ( UnknownException e ) {
        }
        registry.deregisterRole( this, role );
    }

    /**
     * Removes a <code>SecuredObject</code> from the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>owner role ($O:OBJECTNAME) is removed
     * <li>object is removed
     * </ul>
     * 
     * NOTE: Only performed if acting user has the 'delete'-right on the secured object.
     * 
     * @param object
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void deregisterSecuredObject( SecuredObject object )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.DELETE, object );
        try {
            Role ownerRole = registry.getRoleByName( this, "$O:" + object.getName() );
            registry.deregisterRole( this, ownerRole );
        } catch ( UnknownException e ) {
        }
        registry.deregisterSecuredObject( this, object );
    }

    /**
     * Removes a <code>User</code> from the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>owner role ($U:USERNAME) is removed
     * <li>user is removed
     * </ul>
     * 
     * NOTE: Only performed if acting user has the 'delete'-right on the user object.
     * 
     * @param user
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void deregisterUser( User user )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.DELETE, user );
        try {
            Role ownerRole = registry.getRoleByName( this, "$U:" + user.getName() );
            registry.deregisterRole( this, ownerRole );
        } catch ( UnknownException e ) {
            e.printStackTrace();
        }
        registry.deregisterUser( this, user );
    }

    /**
     * 
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Registers a new <code>Group</code> to the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>a group is created in the registry
     * <li>a corresponding owner role is created: $G:GROUPNAME
     * <li>rights for the owner role are set up; creator has delete, update and grant rights on the group, administrator
     * role gets these right, too
     * </ul>
     * 
     * NOTE: Only performed if acting user has the 'addgroup'-privilege.
     * 
     * @param name
     * @param title
     * @return the new Group
     * @throws GeneralSecurityException
     */
    public Group registerGroup( String name, String title )
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        checkForPrivilege( Privilege.ADDGROUP );
        if ( name.startsWith( "$" ) ) {
            throw new GeneralSecurityException( "Groupname '" + name + "' is invalid. The '$'-character is for "
                                                + "internal use only." );
        }
        Group group = registry.registerGroup( this, name, title );
        // only add owner role if lock holder is not the administrator
        if ( this.user.getID() != User.ID_SEC_ADMIN ) {
            Role ownerRole = registry.registerRole( this, "$G:" + name );
            registry.setRolesForUser( this, user, addRoles( registry.getRolesForUser( this, user ), ownerRole ) );
            registry.setRights( this, group, ownerRole, new Right[] { new Right( group, RightType.DELETE ),
                                                                     new Right( group, RightType.UPDATE ),
                                                                     new Right( group, RightType.GRANT ) } );
        }
        registry.setRights( this, group, adminRole, new Right[] { new Right( group, RightType.DELETE ),
                                                                 new Right( group, RightType.UPDATE ),
                                                                 new Right( group, RightType.GRANT ) } );
        return group;
    }

    /**
     * Registers a new <code>Role</code> to the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>a role is created in the registry
     * <li>a corresponding owner role is created: $R:ROLENAME
     * <li>rights for the owner role are set up; creator has delete, update and grant rights on the role, administrator
     * role gets these right, too
     * </ul>
     * 
     * NOTE: Only performed if acting user has the 'addrole'-privilege.
     * 
     * @param name
     * @return the new Role
     * @throws GeneralSecurityException
     */
    public Role registerRole( String name )
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        checkForPrivilege( Privilege.ADDROLE );
        if ( name.startsWith( "$" ) ) {
            throw new GeneralSecurityException( "Rolename '" + name + "' is invalid. The '$'-character is for "
                                                + "internal use only." );
        }

        Role role = registry.registerRole( this, name );
        if ( this.user.getID() != User.ID_SEC_ADMIN ) {
            Role ownerRole = registry.registerRole( this, "$R:" + name );
            registry.setRolesForUser( this, user, addRoles( registry.getRolesForUser( this, user ), ownerRole ) );
            registry.setRights( this, role, ownerRole, new Right[] { new Right( role, RightType.DELETE ),
                                                                    new Right( role, RightType.UPDATE ),
                                                                    new Right( role, RightType.GRANT ) } );
        }
        registry.setRights( this, role, adminRole, new Right[] { new Right( role, RightType.DELETE ),
                                                                new Right( role, RightType.UPDATE ),
                                                                new Right( role, RightType.GRANT ) } );
        return role;
    }

    /**
     * Registers a new <code>SecuredObject</code> to the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>a secured object is created in the registry
     * <li>a corresponding owner role is created: $O:OBJECTNAME
     * <li>rights for the owner role are set up; creator has delete, update and grant rights on the object,
     * administrator role gets these right, too
     * </ul>
     * 
     * @param type
     * @param name
     * @param title
     * @return the new SecuredObject
     * @throws GeneralSecurityException
     */
    public SecuredObject registerSecuredObject( String type, String name, String title )
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        checkForPrivilege( Privilege.ADDOBJECT );
        if ( name.startsWith( "$" ) ) {
            throw new GeneralSecurityException( "Objectname '" + name + "' is invalid. The '$'-character is for "
                                                + "internal use only." );
        }
        SecuredObject object = registry.registerSecuredObject( this, type, name, title );
        if ( this.user.getID() != User.ID_SEC_ADMIN ) {
            Role ownerRole = registry.registerRole( this, "$O:" + name );
            registry.setRolesForUser( this, user, addRoles( registry.getRolesForUser( this, user ), ownerRole ) );
            registry.setRights( this, object, ownerRole, new Right[] { new Right( object, RightType.DELETE ),
                                                                      new Right( object, RightType.UPDATE ),
                                                                      new Right( object, RightType.GRANT ) } );
        }
        registry.setRights( this, object, adminRole, new Right[] { new Right( object, RightType.DELETE ),
                                                                  new Right( object, RightType.UPDATE ),
                                                                  new Right( object, RightType.GRANT ) } );
        return object;
    }

    /**
     * Registers a new <code>User</code> to the <code>Registry</code>.
     * 
     * This means:
     * <ul>
     * <li>a user is created in the registry
     * <li>a corresponding owner role is created: $U:USERNAME
     * <li>rights for the owner role are set up; creator has delete, update and grant rights on the user, administrator
     * role gets these right, too
     * </ul>
     * 
     * NOTE: Only performed if acting user has the 'adduser'-privilege.
     * 
     * @param name
     * @param password
     *            null means that password checking is disabled
     * @param lastName
     * @param firstName
     * @param mailAddress
     * @return the new User
     * @throws GeneralSecurityException
     */
    public User registerUser( String name, String password, String lastName, String firstName, String mailAddress )
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        checkForPrivilege( Privilege.ADDUSER );
        if ( name.startsWith( "$" ) ) {
            throw new GeneralSecurityException( "Username '" + name + "' is invalid. The '$'-character is for "
                                                + "internal use only." );
        }
        User user = registry.registerUser( this, name, password, lastName, firstName, mailAddress );

        // only add owner role if lock holder is not the administrator
        if ( this.user.getID() != User.ID_SEC_ADMIN ) {
            Role ownerRole = registry.registerRole( this, "$U:" + name );
            registry.setRolesForUser( this, user, addRoles( registry.getRolesForUser( this, user ), ownerRole ) );
            registry.setRights( this, user, ownerRole, new Right[] { new Right( user, RightType.DELETE ),
                                                                    new Right( user, RightType.UPDATE ),
                                                                    new Right( user, RightType.GRANT ) } );
        }
        registry.setRights( this, user, adminRole, new Right[] { new Right( user, RightType.DELETE ),
                                                                new Right( user, RightType.UPDATE ),
                                                                new Right( user, RightType.GRANT ) } );
        return user;
    }

    /**
     * Updates the data of an existing <code>User</code> in the <code>Registry</code>.
     * 
     * NOTE: Only performed if acting user has the 'update'-right on the user.
     * 
     * @param user
     * @throws GeneralSecurityException
     */
    public void updateUser( User user )
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.UPDATE, user );
        registry.updateUser( this, user );
    }

    /**
     * Sets the <code>Group</code> s that a given <code>Group</code> is a DIRECT member of.
     * 
     * NOTE: Only performed if the acting user has the 'grant'-right for all the groups that are requested to be added /
     * removed.
     * 
     * @param group
     * @param newGroups
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void setGroupsForGroup( Group group, Group[] newGroups )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        Group[] oldGroups = group.getGroups( this );

        // build set for old groups
        HashSet<Group> oldGroupSet = new HashSet<Group>( oldGroups.length );
        for ( int i = 0; i < oldGroups.length; i++ ) {
            oldGroupSet.add( oldGroups[i] );
        }
        // build set for new groups
        HashSet<Group> newGroupSet = new HashSet<Group>( oldGroups.length );
        for ( int i = 0; i < newGroups.length; i++ ) {
            newGroupSet.add( newGroups[i] );
        }

        // check grant right for all groups requested to be removed
        Iterator<Group> it = oldGroupSet.iterator();
        while ( it.hasNext() ) {
            Group currGroup = it.next();
            if ( !newGroupSet.contains( currGroup ) ) {
                checkForRight( RightType.GRANT, group );
            }
        }

        // check grant right for all groups requested to be added
        it = newGroupSet.iterator();
        while ( it.hasNext() ) {
            Group currGroup = it.next();
            if ( !oldGroupSet.contains( currGroup ) ) {
                checkForRight( RightType.GRANT, group );
            }
        }
        registry.setGroupsForGroup( this, group, newGroups );
    }

    /**
     * Sets the <code>Groups</code> that a given <code>User</code> is a DIRECT member of.
     * 
     * NOTE: Only performed if the acting user has the 'grant'-right for all the groups that are requested to be added /
     * removed.
     * 
     * @param user
     * @param newGroups
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void setGroupsForUser( User user, Group[] newGroups )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        Group[] oldGroups = user.getGroups( this );

        // build set for old groups
        HashSet<Group> oldGroupSet = new HashSet<Group>( oldGroups.length );
        for ( int i = 0; i < oldGroups.length; i++ ) {
            oldGroupSet.add( oldGroups[i] );
        }
        // build set for new groups
        HashSet<Group> newGroupSet = new HashSet<Group>( oldGroups.length );
        for ( int i = 0; i < newGroups.length; i++ ) {
            newGroupSet.add( newGroups[i] );
        }

        // check grant right for all groups requested to be removed
        Iterator<Group> it = oldGroupSet.iterator();
        while ( it.hasNext() ) {
            Group group = it.next();
            if ( !newGroupSet.contains( group ) ) {
                checkForRight( RightType.GRANT, group );
            }
        }

        // check grant right for all groups requested to be added
        it = newGroupSet.iterator();
        while ( it.hasNext() ) {
            Group group = it.next();
            if ( !oldGroupSet.contains( group ) ) {
                checkForRight( RightType.GRANT, group );
            }
        }
        registry.setGroupsForUser( this, user, newGroups );
    }

    /**
     * Sets the members (groups) for a group.
     * 
     * NOTE: Only performed if the acting user has the 'grant'-right on the group.
     * 
     * @param group
     * @param groups
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void setGroupsInGroup( Group group, Group[] groups )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.GRANT, group );
        registry.setGroupsInGroup( this, group, groups );
    }

    /**
     * Sets the groups to be associated with the given role.
     * 
     * NOTE: Only performed if the acting user has the 'grant'-right on the role.
     * 
     * @param role
     * @param groups
     * @throws GeneralSecurityException
     *             if not permitted
     * @throws UnauthorizedException
     */
    public void setGroupsWithRole( Role role, Group[] groups )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.GRANT, role );
        registry.setGroupsWithRole( this, role, groups );
    }

    /**
     * Sets the privileges for a certain role.
     * 
     * NOTE: Only performed if the acting user has all the privileges he is trying to grant.
     * 
     * FIXME: Shouldn't that be "... to grant / withdraw"?
     * 
     * @param role
     * @param privileges
     * @throws GeneralSecurityException
     *             if not permitted
     */
    public void setPrivilegesForRole( Role role, Privilege[] privileges )
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        Privilege[] holderPrivileges = user.getPrivileges( this );
        HashSet<Privilege> holderSet = new HashSet<Privilege>( holderPrivileges.length );
        for ( int i = 0; i < holderPrivileges.length; i++ ) {
            holderSet.add( holderPrivileges[i] );
        }
        for ( int i = 0; i < privileges.length; i++ ) {
            if ( !holderSet.contains( privileges[i] ) ) {
                throw new GeneralSecurityException( "The requested operation requires the privilege '"
                                                    + privileges[i].getName() + "'." );
            }
        }
        registry.setPrivilegesForRole( this, role, privileges );
    }

    /**
     * @param transaction
     * @param services
     * @param role
     * @throws GeneralSecurityException
     */
    public void setServicesRights( Collection<Integer> services, Role role )
                            throws GeneralSecurityException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.UPDATE, role );
        registry.setServicesRights( this, services, role );
    }

    /**
     * Sets the <code>Rights</code> that a certain role has on a given object.
     * 
     * NOTE: Only performed if the acting user has the 'update'-right on the role and the 'grant'-right on the securable
     * object.
     * 
     * @param object
     * @param role
     * @param rights
     * @throws GeneralSecurityException
     *             if not permitted
     * @throws UnauthorizedException
     */
    public void setRights( SecurableObject object, Role role, Right[] rights )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.UPDATE, role );
        checkForRight( RightType.GRANT, object );
        registry.setRights( this, object, role, rights );
    }

    /**
     * Sets one certain right that a certain role has on the given objects.
     * 
     * NOTE: Only performed if the acting user has the 'update'-right on the role and the 'grant'-right on the securable
     * objects.
     * 
     * @param objects
     * @param role
     * @param right
     * @throws GeneralSecurityException
     *             if not permitted
     * @throws UnauthorizedException
     */
    public void setRights( SecurableObject[] objects, Role role, Right right )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.UPDATE, role );
        for ( int i = 0; i < objects.length; i++ ) {
            checkForRight( RightType.GRANT, objects[i] );
        }
        registry.setRights( this, objects, role, right );
    }

    /**
     * Adds the specified <code>Rights</code> on the passed object to the passed role. If they are already present,
     * nothing happens.
     * 
     * @param object
     * @param role
     * @param additionalRights
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void addRights( SecurableObject object, Role role, Right[] additionalRights )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.UPDATE, role );
        checkForRight( RightType.GRANT, object );
        RightSet presentRights = new RightSet( registry.getRights( this, object, role ) );
        RightSet newRights = presentRights.merge( new RightSet( additionalRights ) );
        registry.setRights( this, object, role, newRights.toArray( object ) );
    }

    /**
     * Adds the specified <code>Rights</code> on the passed object to the passed role. If they are already present,
     * nothing happens.
     * 
     * @param object
     * @param role
     * @param types
     * @throws UnauthorizedException
     * @throws GeneralSecurityException
     */
    public void addRights( SecurableObject object, Role role, RightType[] types )
                            throws UnauthorizedException, GeneralSecurityException {
        Right[] additionalRights = new Right[types.length];
        for ( int i = 0; i < additionalRights.length; i++ ) {
            additionalRights[i] = new Right( object, types[i] );
        }
        addRights( object, role, additionalRights );
    }

    /**
     * Removes all rights of the specified types that the role may have on the given <code>SecurableObject</code>.
     * 
     * @param object
     * @param role
     * @param types
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void removeRights( SecurableObject object, Role role, RightType[] types )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.UPDATE, role );
        checkForRight( RightType.GRANT, object );

        Right[] rights = registry.getRights( this, object, role );
        List<Right> newRightList = new ArrayList<Right>( 20 );
        for ( int i = 0; i < rights.length; i++ ) {
            RightType type = rights[i].getType();
            boolean remove = true;
            for ( int j = 0; j < types.length; j++ ) {
                if ( type.equals( types[j] ) ) {
                    remove = true;
                }
            }
            if ( !remove ) {
                newRightList.add( rights[i] );
            }
        }
        Right[] newRights = newRightList.toArray( new Right[newRightList.size()] );
        registry.setRights( this, object, role, newRights );
    }

    /**
     * Sets the members (users) in a group.
     * 
     * NOTE: Only performed if the acting user has the 'grant'-right on the group.
     * 
     * @param group
     * @param users
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    public void setUsersInGroup( Group group, User[] users )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.GRANT, group );
        registry.setUsersInGroup( this, group, users );
    }

    /**
     * Sets the users to be associated with the given role (DIRECTLY, i.e. not via group memberships).
     * 
     * NOTE: Only performed if the user has the 'grant'-right on the role.
     * 
     * @param role
     * @param users
     * @throws GeneralSecurityException
     *             if not permitted
     * @throws UnauthorizedException
     */
    public void setUsersWithRole( Role role, User[] users )
                            throws GeneralSecurityException, UnauthorizedException {
        SecurityAccessManager.getInstance().verify( this );
        checkForRight( RightType.GRANT, role );
        registry.setUsersWithRole( this, role, users );
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        try {
            User[] users = getAllUsers();

            sb.append( "\n\nSecurityAccess @ " + System.currentTimeMillis() );

            sb.append( "\n\n" ).append( users.length ).append( " registered users:\n" );
            for ( int i = 0; i < users.length; i++ ) {
                sb.append( users[i].toString( this ) ).append( "\n" );
            }
            Group[] groups = getAllGroups();
            sb.append( "\n" ).append( groups.length ).append( " registered groups:\n" );
            for ( int i = 0; i < groups.length; i++ ) {
                sb.append( groups[i].toString( this ) ).append( "\n" );
            }
            Role[] roles = getAllRoles();
            sb.append( "\n" ).append( roles.length ).append( " registered roles:\n" );
            for ( int i = 0; i < roles.length; i++ ) {
                sb.append( roles[i].toString( this ) ).append( "\n" );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * @param address
     * @param title
     * @param objects
     * @param type
     * @return the new service
     * @throws GeneralSecurityException
     */
    public Service registerService( String address, String title, List<StringPair> objects, String type )
                            throws GeneralSecurityException {
        getInstance().verify( this );
        checkForPrivilege( ADDOBJECT );
        Service service = registry.registerService( this, address, title, objects, type );
        return service;
    }

    /**
     * @param service
     * @throws GeneralSecurityException
     */
    public void deregisterService( Service service )
                            throws GeneralSecurityException {
        getInstance().verify( this );
        // it's probably not necessary to actually use the rights management for this
        // checkForRight( DELETE, service );
        registry.deregisterService( this, service );
    }

    /**
     * @param oldService
     * @param newService
     * @throws ReadWriteLockInvalidException
     * @throws GeneralSecurityException
     */
    public void updateService( Service oldService, Service newService )
                            throws ReadWriteLockInvalidException, GeneralSecurityException {
        getInstance().verify( this );
        registry.updateService( this, oldService, newService );
    }

    /**
     * @param service
     * @param oldName
     * @param newName
     * @throws ReadWriteLockInvalidException
     * @throws GeneralSecurityException
     */
    public void renameObject( Service service, String oldName, String newName )
                            throws ReadWriteLockInvalidException, GeneralSecurityException {
        getInstance().verify( this );
        registry.renameObject( this, service, oldName, newName );
    }

    /**
     * @param service
     * @param newTitle
     * @param newAddress
     * @throws GeneralSecurityException
     */
    public void editService( Service service, String newTitle, String newAddress )
                            throws GeneralSecurityException {
        getInstance().verify( this );
        registry.editService( this, service, newTitle, newAddress );
    }

    /**
     *
     */
    void renew() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @param transaction
     * @param role
     * @param right
     */
    public void setServiceRight( Service service, Role role, RightType right )
                            throws GeneralSecurityException {
        getInstance().verify( this );
        registry.setServiceRight( this, service, role, right );
    }

    public void setConstraints( Service service, Role role, String constraints )
                            throws GeneralSecurityException {
        getInstance().verify( this );
        registry.setConstraints( this, service, role, constraints );
    }
}
