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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.User;

/**
 * Helper class that performs access checks.
 * <p>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SecurityHelper {

    /**
     * Returns the administrator (the 'Administrator'- or a 'SUBADMIN:'-role) for the given role.
     * <p>
     *
     * @param access
     * @param role
     * @return the administrator (the 'Administrator'- or a 'SUBADMIN:'-role) for the given role.
     * @throws GeneralSecurityException
     *
     */
    public static Role findAdminForRole( SecurityAccess access, Role role )
                            throws GeneralSecurityException {

        Role[] allRoles = access.getAllRoles();
        Role admin = access.getRoleById( Role.ID_SEC_ADMIN );
        for ( int i = 0; i < allRoles.length; i++ ) {
            if ( allRoles[i].getName().startsWith( "SUBADMIN:" ) ) {
                // if a subadmin-role has the update right, it is
                // considered to be the administrative for the role
                if ( allRoles[i].hasRight( access, RightType.UPDATE, role ) ) {
                    admin = allRoles[i];
                }
            }
        }
        return admin;
    }

    /**
     * Returns the associated 'Administrator'- or 'SUBADMIN:'-role of the token holder.
     * <p>
     *
     * @param access
     * @return the associated 'Administrator'- or 'SUBADMIN:'-role of the token holder.
     *         <p>
     * @throws ManagementException
     * @throws GeneralSecurityException
     */
    public static Role checkForAdminOrSubadminRole( SecurityAccess access )
                            throws ManagementException, GeneralSecurityException {
        Role adminOrSubadminRole = null;
        Role[] roles = access.getUser().getRoles( access );
        for ( int i = 0; i < roles.length; i++ ) {
            if ( roles[i].getID() == Role.ID_SEC_ADMIN || roles[i].getName().startsWith( "SUBADMIN:" ) ) {
                if ( adminOrSubadminRole == null ) {
                    adminOrSubadminRole = roles[i];
                } else {
                    throw new ManagementException( "Unzulässige Rollenvergabe: Benutzer '"
                                                   + access.getUser().getTitle() + "' hat sowohl die Rolle '"
                                                   + adminOrSubadminRole.getTitle() + "' als auch die Rolle '"
                                                   + roles[i].getTitle() + "'." );
                }
            }
        }
        if ( adminOrSubadminRole == null ) {
            throw new UnauthorizedException( "Sie haben nicht die für den Zugriff " + "benötigte 'Administrator'- "
                                             + "bzw. Subadministrator-Rolle." );
        }
        return adminOrSubadminRole;
    }

    /**
     * Tests if the given token is associated with the 'Administrator'-role.
     * <p>
     *
     * @param access
     * @throws GeneralSecurityException
     *             this is an UnauthorizedException if the user does not have the
     *             'Administrator'-role
     */
    public static void checkForAdminRole( SecurityAccess access )
                            throws GeneralSecurityException {
        Role[] roles = access.getUser().getRoles( access );
        for ( int i = 0; i < roles.length; i++ ) {
            if ( roles[i].getID() == Role.ID_SEC_ADMIN ) {
                return;
            }
        }
        throw new UnauthorizedException( "Sie haben nicht die für den Zugriff " + "benötigte 'Administrator'-Rolle." );
    }

    /**
     * Tests if the 'SUBADMIN:' and 'Administrator'-roles are all disjoint (so that there are no
     * users that have more than 1 role).
     * <p>
     *
     * @param access
     * @throws ManagementException
     *             if there is a user with more than one role
     * @throws GeneralSecurityException
     */
    @SuppressWarnings("unchecked")
    public static void checkSubadminRoleValidity( SecurityAccess access )
                            throws ManagementException, GeneralSecurityException {

        Role[] subadminRoles = access.getRolesByNS( "SUBADMIN" );
        Set<User>[] rolesAndUsers = new Set[subadminRoles.length + 1];
        String[] roleNames = new String[subadminRoles.length + 1];

        // admin role
        User[] users = access.getRoleById( Role.ID_SEC_ADMIN ).getAllUsers( access );
        rolesAndUsers[0] = new HashSet<User>();
        roleNames[0] = "Administrator";
        for ( int i = 0; i < users.length; i++ ) {
            rolesAndUsers[0].add( users[i] );
        }

        // subadmin roles
        for ( int i = 1; i < rolesAndUsers.length; i++ ) {
            users = subadminRoles[i - 1].getAllUsers( access );
            rolesAndUsers[i] = new HashSet<User>();
            roleNames[i] = subadminRoles[i - 1].getTitle();
            for ( int j = 0; j < users.length; j++ ) {
                rolesAndUsers[i].add( users[j] );
            }
        }

        // now check if all usersets are disjoint
        for ( int i = 0; i < rolesAndUsers.length - 1; i++ ) {
            Set userSet1 = rolesAndUsers[i];
            for ( int j = i + 1; j < rolesAndUsers.length; j++ ) {
                Set userSet2 = rolesAndUsers[j];
                Iterator it = userSet2.iterator();
                while ( it.hasNext() ) {
                    User user = (User) it.next();
                    if ( userSet1.contains( user ) ) {
                        throw new ManagementException( "Ungültige Subadmin-Rollenvergabe. Benutzer '" + user.getTitle()
                                                       + "' würde sowohl die Rolle '" + roleNames[i]
                                                       + "' als auch die Rolle '" + roleNames[j] + "' erhalten." );
                    }
                }
            }
        }
    }
}
