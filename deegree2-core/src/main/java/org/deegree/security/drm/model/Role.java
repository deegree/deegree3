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
import java.util.Set;
import java.util.Stack;

import org.deegree.model.feature.Feature;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityRegistry;


/**
 * Implementation of role-objects. <code>Role</code> s define the
 * <code>Privilege</code> of <code>User</code> s and <code>Groups</code>
 * and their <code>Rights</code> on <code>SecurableObjects</code>.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */
public class Role extends SecurableObject {

    public final static int ID_SEC_ADMIN = 3;

    /**
     * Creates a new <code>Role</code> -instance.
     *
     * @param id
     * @param name
     * @param registry
     */
    public Role(int id, String name, SecurityRegistry registry) {
        this.id = id;
        this.name = name;
        this.title = name;
        this.registry = registry;
    }

    /**
     * Returns the <code>Group</code> s that are associated with this role
     * DIRECTLY, i.e. not via membership in other roles.
     *
     * @param securityAccess
     * @throws GeneralSecurityException
     */
    public Group[] getGroups(SecurityAccess securityAccess)
            throws GeneralSecurityException {
        return registry.getGroupsWithRole(securityAccess, this);
    }

    /**
     * Returns the <code>User</code> s that are associated with this role
     * DIRECTLY, i.e. not via group membership.
     *
     * @param securityAccess
     * @throws GeneralSecurityException
     */
    public User[] getUsers(SecurityAccess securityAccess)
            throws GeneralSecurityException {
        return registry.getUsersWithRole(securityAccess, this);
    }

    /**
     * Returns the <code>User</code> s that are associated with this role
     * either directly or via group membership.
     *
     * @param securityAccess
     * @throws GeneralSecurityException
     */
    public User[] getAllUsers(SecurityAccess securityAccess)
            throws GeneralSecurityException {
        Set<User> allUsers = new HashSet<User>();

        // directly associated users
        User[] directUsers = registry.getUsersWithRole(securityAccess, this);
        for (int i = 0; i < directUsers.length; i++) {
            allUsers.add(directUsers[i]);
        }

        // traverse group hierarchy and add users
        Group[] groups = registry.getGroupsWithRole(securityAccess, this);
        Stack<Group> groupsStack = new Stack<Group>();
        for (int i = 0; i < groups.length; i++) {
            groupsStack.push(groups[i]);
        }
        while (!groupsStack.isEmpty()) {
            Group group = groupsStack.pop();
            Group[] children = group.getGroups(securityAccess);
            for (int i = 0; i < children.length; i++) {
                groupsStack.push(children[i]);
            }
            User[] users = group.getUsers(securityAccess);
            for (int i = 0; i < users.length; i++) {
                allUsers.add(users[i]);
            }
        }

        return allUsers.toArray(new User[allUsers.size()]);
    }

    /**
     * Returns the <code>Privilege</code> s that this role has.
     *
     * @param securityAccess
     */
    public Privilege[] getPrivileges(SecurityAccess securityAccess)
            throws GeneralSecurityException {
        return registry.getPrivilegesForRole(securityAccess, this);
    }

    /**
     * Returns the rights that this role defines concerning the given
     * <code>SecurableObject</code>.
     *
     * @param securityAccess
     */
    public RightSet getRights(SecurityAccess securityAccess,
            SecurableObject object) throws GeneralSecurityException {
        return new RightSet(registry.getRights(securityAccess, object, this));
    }

    /**
     * Returns whether the <code>Role</code> has a certain <code>Right</code>
     * on a <code>SecurableObject</code> (directly or via group
     * memberships).
     */
    public boolean hasRight(SecurityAccess securityAccess, RightType type,
            Feature accessParams, SecurableObject object)
            throws GeneralSecurityException {
        return getRights(securityAccess, object).applies(object, type,
                accessParams);
    }

    /**
     * Returns whether the <code>Role</code> has a certain <code>Right</code>
     * on a <code>SecurableObject</code>.
     */
    public boolean hasRight(SecurityAccess securityAccess, RightType type,
            SecurableObject object) throws GeneralSecurityException {
        return getRights(securityAccess, object).applies(object, type);
    }

    /**
     * Returns whether the <code>Role</code> has a certain right on a
     * <code>SecurableObject</code>.
     */
    public boolean hasRight(SecurityAccess securityAccess, String s,
            SecurableObject object) throws GeneralSecurityException {
        RightType right = registry.getRightTypeByName(securityAccess, s);
        return hasRight(securityAccess, right, object);
    }

    /**
     * Returns whether the <code>Role</code> has a certain
     * <code>Privilege</code>.
     *
     * @param privilege
     */
    public boolean hasPrivilege(SecurityAccess securityAccess,
            Privilege privilege) throws GeneralSecurityException {
        Privilege[] privileges = getPrivileges(securityAccess);
        for (int i = 0; i < privileges.length; i++) {
            if (privileges[i].equals(privilege)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the <code>Role</code> has a certain privilege.
     *
     * @param s
     */
    public boolean hasPrivilege(SecurityAccess securityAccess, String s)
            throws GeneralSecurityException {
        Privilege privilege = registry.getPrivilegeByName(securityAccess, s);
        return hasPrivilege(securityAccess, privilege);
    }

    /**
     * Returns a <code>String</code> representation of this object.
     *
     * @param securityAccess
     */
    public String toString(SecurityAccess securityAccess) {
        StringBuffer sb = new StringBuffer("Name: ").append(name);

        try {
            sb.append(", Users: [");
            User[] users = getUsers(securityAccess);
            for (int i = 0; i < users.length; i++) {
                sb.append(users[i].getName());
                if (i != users.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");

            sb.append(", Groups: [");
            Group[] groups = getGroups(securityAccess);
            for (int i = 0; i < groups.length; i++) {
                sb.append(groups[i].getName());
                if (i != groups.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
