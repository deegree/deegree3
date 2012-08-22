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
package org.deegree.portal.standard.security.control;

import java.util.ArrayList;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.Right;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;

/**
 * This <code>Listener</code> reacts on RPC-StoreRoles events, extracts the submitted role/group
 * relations and updates the <code>SecurityAccessManager</code> accordingly.
 *
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-rol are allowed</li>
 * </ul>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class StoreRolesListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( StoreRolesListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        // contains the data from the RPC, values of the ArrayLists
        // are Integers (one roleId followed by several groupIds; the
        // first value is a String in case of a new role)
        ArrayList[] roles = null;

        SecurityAccessManager manager = null;
        SecurityTransaction transaction = null;

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            roles = new ArrayList[params.length];
            for ( int i = 0; i < params.length; i++ ) {
                ArrayList<Object> list = new ArrayList<Object>();
                roles[i] = list;
                if ( !( params[0].getValue() instanceof RPCStruct ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_STRUCT" ) );
                }
                RPCStruct struct = (RPCStruct) params[i].getValue();

                // extract role-id / role-name
                RPCMember roleId = struct.getMember( "roleId" );
                RPCMember roleName = struct.getMember( "roleName" );
                if ( ( roleId == null && roleName == null ) || ( roleId != null && roleName != null ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_ROLES", "roleId", "roleName" ) );
                }
                if ( roleId != null ) {
                    if ( !( roleId.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "roleId", "string" ) );
                    }
                    try {
                        list.add( new Integer( (String) roleId.getValue() ) );
                    } catch ( NumberFormatException e ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "roleId", "integer" ) );
                    }
                } else {
                    if ( !( roleName.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "roleName", "string" ) );
                    }
                    list.add( roleName.getValue() );
                }

                // extract groups
                RPCMember groups = struct.getMember( "groups" );
                if ( !( groups.getValue() instanceof RPCParameter[] ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_ARRAY", "groups" ) );
                }
                RPCParameter[] groupArray = (RPCParameter[]) groups.getValue();
                for ( int j = 0; j < groupArray.length; j++ ) {
                    if ( !( groupArray[j].getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_ARRAY_VALUES", "groups",
                                                                     "String" ) );
                    }
                    try {
                        list.add( new Integer( (String) groupArray[j].getValue() ) );
                    } catch ( NumberFormatException e ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_INVALID_ARRAY_VALUES", "groups",
                                                                     "Integer" ) );
                    }
                }
            }

            // get Transaction
            manager = SecurityAccessManager.getInstance();
            transaction = SecurityHelper.acquireTransaction( this );
            SecurityHelper.checkForAdminRole( transaction );

            // perform access check (and get admin/subadmin role)
            Role subadminRole = SecurityHelper.checkForAdminOrSubadminRole( transaction );

            // remove deleted roles
            Role[] oldRoles = transaction.getAllRoles();
            for ( int i = 0; i < oldRoles.length; i++ ) {
                if ( !oldRoles[i].getName().startsWith( "SUBADMIN:" ) ) {
                    boolean deleted = true;
                    for ( int j = 0; j < roles.length; j++ ) {
                        ArrayList list = roles[j];
                        if ( list.get( 0 ) instanceof Integer ) {
                            if ( ( (Integer) list.get( 0 ) ).intValue() == oldRoles[i].getID() ) {
                                deleted = false;
                            }
                        }
                    }
                    if ( deleted ) {
                        // deregister Role
                        transaction.deregisterRole( oldRoles[i] );
                    }
                }
            }

            // store all submitted roles (and their groups)
            for ( int i = 0; i < roles.length; i++ ) {
                Role role = null;

                ArrayList list = roles[i];
                if ( list.get( 0 ) instanceof Integer ) {
                    role = transaction.getRoleById( ( (Integer) list.get( 0 ) ).intValue() );

                    // only modify role if editor has the right to grant the
                    // role
                    if ( !transaction.getUser().hasRight( transaction, "grant", role ) ) {
                        continue;
                    }
                } else {
                    // only add role if editor has the privilege to do so
                    if ( transaction.getUser().hasPrivilege( transaction, "addrole" ) ) {
                        role = transaction.registerRole( (String) list.get( 0 ) );
                        if ( subadminRole.getID() != Role.ID_SEC_ADMIN ) {
                            transaction.setRights( role, subadminRole,
                                                   new Right[] { new Right( role, RightType.DELETE ),
                                                                new Right( role, RightType.UPDATE ),
                                                                new Right( role, RightType.GRANT ) } );
                        }
                    }
                }
                // set groups to be associated with the role
                Group[] groups = new Group[list.size() - 1];
                for ( int j = 1; j < list.size(); j++ ) {
                    int groupId = ( (Integer) list.get( j ) ).intValue();
                    groups[j - 1] = transaction.getGroupById( groupId );
                }
                transaction.setGroupsWithRole( role, groups );
            }
            manager.commitTransaction( transaction );
            transaction = null;

            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_SUCCESS_INITROLEEDITOR" ) );

        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE_REQ", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logDebug( e.getMessage(), e );

        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logDebug( e.getMessage(), e );
        } finally {
            if ( manager != null && transaction != null ) {
                try {
                    manager.abortTransaction( transaction );
                } catch ( GeneralSecurityException ex ) {
                    LOG.logDebug( ex.getMessage(), ex );
                }
            }
        }
    }
}
