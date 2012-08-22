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
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.User;

/**
 * This <code>Listener</code> reacts on RPC-StoreGroups events, extracts the contained user/group
 * relations and updates the <code>SecurityManager</code> accordingly.
 *
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class StoreGroupsListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( StoreGroupsListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        SecurityAccessManager manager = null;
        SecurityTransaction transaction = null;

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            // values are Integers (groupIds) or Strings (groupNames)
            Object[] groups = new Object[params.length];
            // values of the ArrayLists are Integers (groupIds)
            ArrayList[] userMembersIds = new ArrayList[params.length];
            // values of the ArrayLists are Integers (userIds)
            ArrayList[] groupMembersIds = new ArrayList[params.length];

            for ( int i = 0; i < params.length; i++ ) {
                ArrayList<Integer> userMemberList = new ArrayList<Integer>( 200 );
                ArrayList<Integer> groupMemberList = new ArrayList<Integer>( 200 );
                userMembersIds[i] = userMemberList;
                groupMembersIds[i] = groupMemberList;
                if ( !( params[0].getValue() instanceof RPCStruct ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_STRUCT" ) );
                }
                RPCStruct struct = (RPCStruct) params[i].getValue();

                // extract group-id / group-name
                extractGroupInfo( groups, i, struct );

                // extract user members
                extractUsers( userMemberList, struct );

                // extract group members
                extractGroups( groupMemberList, struct );

            }

            // get Transaction and perform access check
            manager = SecurityAccessManager.getInstance();
            transaction = SecurityHelper.acquireTransaction( this );
            SecurityHelper.checkForAdminRole( transaction );

            // remove deleted groups
            removeDeletedGroups( transaction, groups );

            // save all submitted groups (and their members)
            saveSubmittedGroups( transaction, groups, userMembersIds, groupMembersIds );
            Group[] cycle = transaction.findGroupCycle();
            manager.commitTransaction( transaction );
            transaction = null;

            StringBuffer sb = new StringBuffer( 200 );
            sb.append( Messages.getMessage( "IGEO_STD_SEC_SUCCESS_STOREGROUPS" ) );

            if ( cycle != null ) {
                sb.append( "<br><p><h4>" );
                sb.append( Messages.getMessage( "IGEO_STD_SEC_SUCCESS_STOREGROUPS_ADDTXT" ) );
                sb.append( "<br><code>" );
                for ( int i = 0; i < cycle.length; i++ ) {
                    sb.append( cycle[i].getName() );
                    if ( i != cycle.length - 1 ) {
                        sb.append( " -> " );
                    }
                }
                sb.append( "</code></h4></p>" );
            }

            getRequest().setAttribute( "MESSAGE", sb.toString() );
        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE_REQ",
                                                            e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logDebug( e.getMessage(), e );

        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE",
                                                            e.getMessage() ) );
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

    private void saveSubmittedGroups( SecurityTransaction transaction, Object[] groups,
                                     ArrayList[] userMembersIds, ArrayList[] groupMembersIds )
                            throws GeneralSecurityException, UnauthorizedException {
        for ( int i = 0; i < groups.length; i++ ) {
            Group group;
            if ( groups[i] instanceof Integer ) {
                group = transaction.getGroupById( ( (Integer) groups[i] ).intValue() );
            } else {
                group = transaction.registerGroup( (String) groups[i], (String) groups[i] );
            }

            // set user members
            User[] userMembers = new User[userMembersIds[i].size()];
            for ( int j = 0; j < userMembersIds[i].size(); j++ ) {
                int userId = ( (Integer) userMembersIds[i].get( j ) ).intValue();
                userMembers[j] = transaction.getUserById( userId );
            }
            transaction.setUsersInGroup( group, userMembers );

            // set group members
            Group[] groupMembers = new Group[groupMembersIds[i].size()];
            for ( int j = 0; j < groupMembersIds[i].size(); j++ ) {
                int groupId = ( (Integer) groupMembersIds[i].get( j ) ).intValue();
                groupMembers[j] = transaction.getGroupById( groupId );
            }
            transaction.setGroupsInGroup( group, groupMembers );
        }
    }

    private void removeDeletedGroups( SecurityTransaction transaction, Object[] groups )
                            throws GeneralSecurityException, UnauthorizedException {
        Group[] oldGroups = transaction.getAllGroups();
        for ( int i = 0; i < oldGroups.length; i++ ) {
            if ( oldGroups[i].getID() != Group.ID_SEC_ADMIN ) {
                boolean deleted = true;
                for ( int j = 0; j < groups.length; j++ ) {
                    if ( groups[j] instanceof Integer ) {
                        if ( ( (Integer) groups[j] ).intValue() == oldGroups[i].getID() ) {
                            deleted = false;
                        }
                    }
                }
                if ( deleted ) {
                    transaction.deregisterGroup( oldGroups[i] );
                }
            }
        }
    }

    private void extractGroups( ArrayList<Integer> groupMemberList, RPCStruct struct )
                            throws RPCException {
        RPCMember groupMembers = struct.getMember( "groupMembers" );
        if ( !( groupMembers.getValue() instanceof RPCParameter[] ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_ARRAY",
                                                         "groupMembers" ) );
        }
        RPCParameter[] memberArray = (RPCParameter[]) groupMembers.getValue();
        for ( int j = 0; j < memberArray.length; j++ ) {
            if ( !( memberArray[j].getValue() instanceof String ) ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_ARRAY_VALUES",
                                                             "groupMembers", "String" ) );
            }
            try {
                groupMemberList.add( new Integer( (String) memberArray[j].getValue() ) );
            } catch ( NumberFormatException e ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_INVALID_ARRAY_VALUES",
                                                             "groupMembers", "Integer" ) );
            }
        }
    }

    private void extractUsers( ArrayList<Integer> userMemberList, RPCStruct struct )
                            throws RPCException {
        RPCMember userMembers = struct.getMember( "userMembers" );
        if ( !( userMembers.getValue() instanceof RPCParameter[] ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_ARRAY",
                                                         "userMembers" ) );
        }
        RPCParameter[] memberArray = (RPCParameter[]) userMembers.getValue();
        for ( int j = 0; j < memberArray.length; j++ ) {
            if ( !( memberArray[j].getValue() instanceof String ) ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_ARRAY_VALUES",
                                                             "userMembers", "String" ) );
            }
            try {
                userMemberList.add( new Integer( (String) memberArray[j].getValue() ) );
            } catch ( NumberFormatException e ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_INVALID_ARRAY_VALUES",
                                                             "userMembers", "Integer" ) );
            }
        }
    }

    private void extractGroupInfo( Object[] groups, int index, RPCStruct struct )
                            throws RPCException {

        RPCMember groupId = struct.getMember( "groupId" );
        RPCMember groupName = struct.getMember( "groupName" );
        if ( ( groupId == null && groupName == null ) || ( groupId != null && groupName != null ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_GROUPS" ) );
        }
        if ( groupId != null ) {
            if ( !( groupId.getValue() instanceof String ) ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_GROUP_PARAM",
                                                             "groupId", "String" ) );
            }
            try {
                groups[index] = ( new Integer( (String) groupId.getValue() ) );
            } catch ( NumberFormatException e ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_GROUP_PARAM",
                                                             "groupId", "Integer" ) );
            }
        } else {
            if ( !( groupName.getValue() instanceof String ) ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_GROUP_PARAM",
                                                             "groupName", "String" ) );
            }

            groups[index] = groupName.getValue();
        }
    }
}
