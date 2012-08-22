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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.deegree.framework.util.StringPair;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.Privilege;
import org.deegree.security.drm.model.Right;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.SecurableObject;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.Service;
import org.deegree.security.drm.model.User;

/**
 * This is an interface for datastores that are able to stores the following object types and their relations:
 * <ul>
 * <li><code>User</code>
 * <li><code>Group</code>
 * <li><code>Role</code>
 * <li><code>SecurableObject</code>
 * <li><code>Right / RightType</code>
 * <li><code>Privilege</code>
 * </ul>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */
public interface SecurityRegistry {

    /**
     * Initializes the <code>Registry</code> -instance according to the contents of the submitted
     * <code>Properties</code>.
     * <p>
     * The supported keys and values depend on the concrete implementation.
     * 
     * @param properties
     * @throws GeneralSecurityException
     */
    void initialize( Properties properties )
                            throws GeneralSecurityException;

    /**
     * Signals the <code>Registry</code> that a new transaction starts.
     * <p>
     * Only one transaction can be active at a time.
     * 
     * @param transaction
     * 
     * @throws GeneralSecurityException
     */
    void beginTransaction( SecurityTransaction transaction )
                            throws GeneralSecurityException;

    /**
     * Signals the <code>Registry</code> that the current transaction ends. Changes made during the transaction are now
     * made persistent.
     * 
     * @param transaction
     * 
     * @throws GeneralSecurityException
     */
    void commitTransaction( SecurityTransaction transaction )
                            throws GeneralSecurityException;

    /**
     * Signals the <code>Registry</code> that the transaction shall be aborted. Changes made by the transaction are
     * undone.
     * 
     * @param transaction
     * 
     * @throws GeneralSecurityException
     */
    void abortTransaction( SecurityTransaction transaction )
                            throws GeneralSecurityException;

    /**
     * Deletes all data from the <code>Registry</code> and sets the default objects (SEC_ADMIN user, role and group) and
     * standard rights and privileges.
     * 
     * @param transaction
     * @throws GeneralSecurityException
     */
    void clean( SecurityTransaction transaction )
                            throws GeneralSecurityException;

    /**
     * Adds a new User-account to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @param password
     * @param lastName
     * @param firstName
     * @param mailAddress
     * @return the new user
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the group already existed
     */
    User registerUser( SecurityTransaction transaction, String name, String password, String lastName,
                       String firstName, String mailAddress )
                            throws GeneralSecurityException;

    /**
     * Removes an existing <code>User<code> from the <code>Registry</code>.
     * 
     * @param transaction
     * @param user
     * 
     * @throws GeneralSecurityException
     */
    void deregisterUser( SecurityTransaction transaction, User user )
                            throws GeneralSecurityException;

    /**
     * Updates the metadata (name, email, etc.) of a <code>User</code> in the <code>Registry</code>.
     * 
     * @param transaction
     * @param user
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if a user with the new name already existed
     */
    void updateUser( SecurityTransaction transaction, User user )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>User</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @return the user
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the user is not known to the <code>Registry</code>
     */
    User getUserByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>User</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @return the user
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the user is not known to the <code>Registry</code>
     */
    User getUserById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>User</code> s from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @return the users
     * 
     * @throws GeneralSecurityException
     */
    User[] getAllUsers( SecurityAccess securityAccess )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Users</code> s from the <code>Registry</code> that are associated DIRECTLY (SecurityAccess
     * securityAccess, i.e. not via group memberships) with a given <code>Role</code>.
     * 
     * @param securityAccess
     * @param role
     * @return the users
     * 
     * @throws GeneralSecurityException
     */
    User[] getUsersWithRole( SecurityAccess securityAccess, Role role )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>User</code> s from the <code>Registry</code> belong to the given <code>Group</code>.
     * 
     * @param securityAccess
     * @param group
     * @return the users
     * 
     * @throws GeneralSecurityException
     */
    User[] getUsersInGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>User</code> s that are members of a given <code>Group</code>.
     * 
     * @param transaction
     * @param group
     * @param users
     * 
     * @throws GeneralSecurityException
     */
    void setUsersInGroup( SecurityTransaction transaction, Group group, User[] users )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>User</code> s that a given <code>Role</code> is associated to.
     * 
     * @param transaction
     * @param role
     * @param users
     * 
     * @throws GeneralSecurityException
     */
    void setUsersWithRole( SecurityTransaction transaction, Role role, User[] users )
                            throws GeneralSecurityException;

    /**
     * Adds a new Group-account to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @param title
     * @return the group
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the group already existed
     */
    Group registerGroup( SecurityTransaction transaction, String name, String title )
                            throws GeneralSecurityException;

    /**
     * Removes an existing <code>Group</code> from the <code>Registry</code> (including its relations).
     * 
     * @param transaction
     * @param group
     * 
     * @throws GeneralSecurityException
     */
    void deregisterGroup( SecurityTransaction transaction, Group group )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>Group</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @return the group
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the group is not known to the <code>Registry</code>
     */
    Group getGroupByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>Group</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @return the group
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the group is not known to the <code>Registry</code>
     */
    Group getGroupById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @return the groups
     * 
     * @throws GeneralSecurityException
     */
    Group[] getAllGroups( SecurityAccess securityAccess )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> that the given <code>User</code> belongs to.
     * 
     * @param securityAccess
     * @param user
     * @return the groups
     * 
     * @throws GeneralSecurityException
     */
    Group[] getGroupsForUser( SecurityAccess securityAccess, User user )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> that the given <code>Group</code> belongs to.
     * 
     * @param securityAccess
     * @param group
     * @return the groups
     * 
     * @throws GeneralSecurityException
     */
    Group[] getGroupsForGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> that the given <code>Group</code>s belongs to.
     * 
     * @param securityAccess
     * @param groups
     * @return the groups
     * 
     * @throws GeneralSecurityException
     */
    Group[] getGroupsForGroups( SecurityAccess securityAccess, Group[] groups )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> belong to the given <code>Group</code>.
     * 
     * @param securityAccess
     * @param group
     * @return the groups
     * 
     * @throws GeneralSecurityException
     */
    Group[] getGroupsInGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Group</code> s from the <code>Registry</code> that are associated with a given
     * <code>Role</code>.
     * 
     * @param securityAccess
     * @param role
     * @return the groups
     * 
     * @throws GeneralSecurityException
     */
    Group[] getGroupsWithRole( SecurityAccess securityAccess, Role role )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>Group</code> s that a given <code>User</code> is a DIRECT member of.
     * 
     * @param transaction
     * @param user
     * @param groups
     * 
     * @throws GeneralSecurityException
     */
    void setGroupsForUser( SecurityTransaction transaction, User user, Group[] groups )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>Groups</code> s that are members of a given <code>Group</code>.
     * 
     * @param transaction
     * @param group
     * @param groups
     * 
     * @throws GeneralSecurityException
     */
    void setGroupsInGroup( SecurityTransaction transaction, Group group, Group[] groups )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>Group</code> s that a given <code>Role</code> is associated to.
     * 
     * @param transaction
     * @param role
     * @param groups
     * 
     * @throws GeneralSecurityException
     */
    void setGroupsWithRole( SecurityTransaction transaction, Role role, Group[] groups )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>Groups</code> s that a given <code>Group</code> is member of DIRECTLY (i.e. not via group
     * membership).
     * 
     * @param transaction
     * @param group
     * @param groups
     * 
     * @throws GeneralSecurityException
     */
    void setGroupsForGroup( SecurityTransaction transaction, Group group, Group[] groups )
                            throws GeneralSecurityException;

    /**
     * Adds a new role to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @return the role
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the role already existed
     */
    Role registerRole( SecurityTransaction transaction, String name )
                            throws GeneralSecurityException;

    /**
     * Removes an existing <code>Role</code> from the <code>Registry</code> (including its relations).
     * 
     * @param transaction
     * @param role
     * 
     * @throws GeneralSecurityException
     */
    void deregisterRole( SecurityTransaction transaction, Role role )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>Role</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @return the role
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the role is not known to the <code>Registry</code>
     */
    Role getRoleByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code> that have a certain namespace.
     * 
     * @param securityAccess
     * @param ns
     * @return the roles
     * 
     * @throws GeneralSecurityException
     */
    Role[] getRolesByNS( SecurityAccess securityAccess, String ns )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>Role</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @return the role
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the role is not known to the <code>Registry</code>
     */
    Role getRoleById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code>, except those that are only used internally
     * (these end with a $ symbol);
     * 
     * @param securityAccess
     * @return the roles
     * 
     * @throws GeneralSecurityException
     */
    Role[] getAllRoles( SecurityAccess securityAccess )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code> that are associated with a given
     * <code>User</code> DIRECTLY (i.e. not via group memberships).
     * 
     * @param securityAccess
     * @param user
     * @return the roles
     * 
     * @throws GeneralSecurityException
     */
    Role[] getRolesForUser( SecurityAccess securityAccess, User user )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code> that are associated with a given
     * <code>Group</code> DIRECTLY (i.e. not via group memberships).
     * 
     * @param securityAccess
     * @param group
     * @return the roles
     * 
     * @throws GeneralSecurityException
     */
    Role[] getRolesForGroup( SecurityAccess securityAccess, Group group )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Role</code> s from the <code>Registry</code> that are associated with a given
     * <code>Group</code>s DIRECTLY (i.e. not via group memberships).
     * 
     * @param securityAccess
     * @param groups
     * @return the roles
     * 
     * @throws GeneralSecurityException
     */
    Role[] getRolesForGroups( SecurityAccess securityAccess, Group[] groups )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>Role</code> s that a given <code>User</code> is directly associated to.
     * 
     * @param transaction
     * @param user
     * @param roles
     * 
     * @throws GeneralSecurityException
     */
    void setRolesForUser( SecurityTransaction transaction, User user, Role[] roles )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>Role</code> s that a given <code>Group</code> is associated to.
     * 
     * @param transaction
     * @param group
     * @param roles
     * 
     * @throws GeneralSecurityException
     */
    void setRolesForGroup( SecurityTransaction transaction, Group group, Role[] roles )
                            throws GeneralSecurityException;

    /**
     * Adds a new <code>SecuredObject</code> to the <code>Registry</code>.
     * 
     * @param transaction
     * @param type
     * @param name
     * @param title
     * @return the new object
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the object already existed
     */
    SecuredObject registerSecuredObject( SecurityTransaction transaction, String type, String name, String title )
                            throws GeneralSecurityException;

    /**
     * Removes an existing <code>SecuredObject</code> from the <code>Registry</code> (including its associated rights).
     * 
     * @param transaction
     * @param object
     * 
     * @throws GeneralSecurityException
     */
    void deregisterSecuredObject( SecurityTransaction transaction, SecuredObject object )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>SecuredObject</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @param type
     * @return the object
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the <code>SecuredObject</code> is not known to the
     *             <code>Registry</code>
     */
    SecuredObject getSecuredObjectByName( SecurityAccess securityAccess, String name, String type )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>SecuredObject</code> s from the <code>Registry</code> that have a certain namespace.
     * 
     * @param securityAccess
     * @param ns
     * @param type
     * @return the objects
     * 
     * @throws GeneralSecurityException
     */
    SecuredObject[] getSecuredObjectsByNS( SecurityAccess securityAccess, String ns, String type )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>SecuredObject</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param id
     * @return the object
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the <code>SecuredObject</code> is not known to the
     *             <code>Registry</code>
     */
    SecuredObject getSecuredObjectById( SecurityAccess securityAccess, int id )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>SecuredObject</code> s from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param type
     * @return the objects
     * 
     * @throws GeneralSecurityException
     */
    SecuredObject[] getAllSecuredObjects( SecurityAccess securityAccess, String type )
                            throws GeneralSecurityException;

    /**
     * Adds a new <code>Privilege</code> to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @return the privilege
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the <code>Privilege</code> already existed
     */
    Privilege registerPrivilege( SecurityTransaction transaction, String name )
                            throws GeneralSecurityException;

    /**
     * Removes an existing</code> Privilege</code> from the <code>Registry </code> (including its relations).
     * 
     * @param transaction
     * @param privilege
     * 
     * @throws GeneralSecurityException
     */
    void deregisterPrivilege( SecurityTransaction transaction, Privilege privilege )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>Privilege</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @return the privilege
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the privilege is not known to the <code>Registry</code>
     */
    Privilege getPrivilegeByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException;

    /**
     * Retrieves all <code>Privileges</code> s from the <code>Registry</code> that are associated with a given
     * <code>Role</code>.
     * 
     * @param securityAccess
     * @param role
     * @return the privileges
     * 
     * @throws GeneralSecurityException
     */
    Privilege[] getPrivilegesForRole( SecurityAccess securityAccess, Role role )
                            throws GeneralSecurityException;

    /**
     * Sets all <code>Privilege</code> s that are associated with a given <code>Role</code>.
     * 
     * @param transaction
     * @param role
     * @param privileges
     * 
     * @throws GeneralSecurityException
     */
    void setPrivilegesForRole( SecurityTransaction transaction, Role role, Privilege[] privileges )
                            throws GeneralSecurityException;

    /**
     * Adds a new <code>RightType</code> to the <code>Registry</code>.
     * 
     * @param transaction
     * @param name
     * @return the right type
     * 
     * @throws GeneralSecurityException
     *             this is a <code>DuplicateException</code> if the <code>RightType</code> already existed
     */
    RightType registerRightType( SecurityTransaction transaction, String name )
                            throws GeneralSecurityException;

    /**
     * Removes an existing <code>RightType</code> from the <code>Registry</code> (including its relations).
     * 
     * @param transaction
     * @param type
     * 
     * @throws GeneralSecurityException
     */
    void deregisterRightType( SecurityTransaction transaction, RightType type )
                            throws GeneralSecurityException;

    /**
     * Retrieves a <code>RightType</code> from the <code>Registry</code>.
     * 
     * @param securityAccess
     * @param name
     * @return the right type
     * 
     * @throws GeneralSecurityException
     *             this is an <code>UnknownException</code> if the <code>RightType</code> is not known to the
     *             <code>Registry</code>
     */
    RightType getRightTypeByName( SecurityAccess securityAccess, String name )
                            throws GeneralSecurityException;

    /**
     * Retrieves the <code>Rights</code> from the <code>Registry</code> that are associated with a given
     * <code>Role</code> and a <code>SecurableObject</code>.
     * 
     * @param securityAccess
     * @param object
     * @param role
     * @return the rights
     * 
     * @throws GeneralSecurityException
     */
    Right[] getRights( SecurityAccess securityAccess, SecurableObject object, Role role )
                            throws GeneralSecurityException;

    /**
     * Retrieves the <code>Rights</code> from the <code>Registry</code> that are associated with a given
     * <code>Role</code> and a <code>SecurableObject</code>.
     * 
     * @param securityAccess
     * @param object
     * @param roles
     * @param type
     * @return the rights
     * 
     * @throws GeneralSecurityException
     */
    Right[] getRights( SecurityAccess securityAccess, SecurableObject object, Role[] roles, RightType type )
                            throws GeneralSecurityException;

    /**
     * Sets the <code>Rights</code> to be associated with a given <code>Role</code> and <code>SecurableObject</code>.
     * 
     * @param transaction
     * @param object
     * @param role
     * @param rights
     * 
     * @throws GeneralSecurityException
     */
    void setRights( SecurityTransaction transaction, SecurableObject object, Role role, Right[] rights )
                            throws GeneralSecurityException;

    /**
     * Sets the services an user has access to.
     * 
     * @param transaction
     * @param services
     *            the ids of the accessible services
     * @param role
     * @throws GeneralSecurityException
     */
    void setServicesRights( SecurityTransaction transaction, Collection<Integer> services, Role role )
                            throws GeneralSecurityException;

    /**
     * Sets one <code>Right</code> to be associated with a given <code>Role</code> and all given
     * <code>SecurableObjects</code>.
     * 
     * @param transaction
     * @param objects
     * @param role
     * @param right
     * 
     * @throws GeneralSecurityException
     */
    void setRights( SecurityTransaction transaction, SecurableObject[] objects, Role role, Right right )
                            throws GeneralSecurityException;

    /**
     * @param access
     * @param address
     * @return the service
     * @throws GeneralSecurityException
     */
    Service getServiceByAddress( SecurityAccess access, String address )
                            throws GeneralSecurityException;

    /**
     * @param transaction
     * @param address
     * @param title
     * @param objects
     * @param type
     * @return the new service
     * @throws GeneralSecurityException
     */
    Service registerService( SecurityTransaction transaction, String address, String title, List<StringPair> objects,
                             String type )
                            throws GeneralSecurityException;

    /**
     * @param transaction
     * @param service
     * @throws GeneralSecurityException
     */
    void deregisterService( SecurityTransaction transaction, Service service )
                            throws GeneralSecurityException;

    /**
     * @param access
     * @return all services
     * @throws GeneralSecurityException
     */
    LinkedList<Service> getAllServices( SecurityAccess access )
                            throws GeneralSecurityException;

    /**
     * @param securityTransaction
     * @param oldService
     * @param newService
     * @throws GeneralSecurityException
     */
    void updateService( SecurityTransaction securityTransaction, Service oldService, Service newService )
                            throws GeneralSecurityException;

    /**
     * @param securityTransaction
     * @param service
     * @param oldName
     * @param newName
     * @throws GeneralSecurityException
     */
    void renameObject( SecurityTransaction securityTransaction, Service service, String oldName, String newName )
                            throws GeneralSecurityException;

    /**
     * @param securityTransaction
     * @param service
     * @param newTitle
     * @param newAddress
     * @throws GeneralSecurityException
     */
    void editService( SecurityTransaction securityTransaction, Service service, String newTitle, String newAddress )
                            throws GeneralSecurityException;

    /**
     * @param access
     * @param role
     * @return the appropriate services
     * @throws GeneralSecurityException
     */
    LinkedList<Service> getRolesServices( SecurityAccess access, Role role )
                            throws GeneralSecurityException;

    /**
     * @param transaction
     * @param role
     * @param right
     */
    void setServiceRight( SecurityTransaction transaction, Service service, Role role, RightType right )
                            throws GeneralSecurityException;

    Service getServiceById( SecurityAccess access, int id )
                            throws GeneralSecurityException;

    boolean hasServiceRight( SecurityAccess access, Service service, Role role, RightType right )
                            throws GeneralSecurityException;

    String getConstraints( SecurityAccess access, Role role, Service service )
                            throws GeneralSecurityException;

    void setConstraints( SecurityTransaction transaction, Service service, Role role, String constraints )
                            throws GeneralSecurityException;
}
