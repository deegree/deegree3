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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.model.User;

/**
 * This <code>Listener</code> reacts on RPC-GetUsers events, extracts the submitted letters and
 * passes the users that begin with one of the letters on to the JSP.
 * <p>
 * The internal "SEC_ADMIN" user is sorted out from the USERS parameter.
 * </p>
 * <p>
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 * </p>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetUsersListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GetUsersListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        try {
            // perform access check
            SecurityAccess access = SecurityHelper.acquireAccess( this );
            SecurityHelper.checkForAdminRole( access );

            String regex = null;

            if ( event instanceof RPCWebEvent ) {
                RPCWebEvent ev = (RPCWebEvent) event;
                RPCMethodCall rpcCall = ev.getRPCMethodCall();
                RPCParameter[] params = rpcCall.getParameters();
                if ( params.length != 1 || !( params[0].getValue() instanceof String ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_PARAM_NUM_REGEX" ) );
                }
                regex = (String) params[0].getValue();
            }

            User[] users = access.getAllUsers();
            ArrayList<User> filteredUsers = new ArrayList<User>( 1000 );
            Pattern pattern = Pattern.compile( regex );

            // include all users which match the submitte regular expression
            for ( int i = 0; i < users.length; i++ ) {
                if ( users[i].getID() != User.ID_SEC_ADMIN ) {
                    String name = users[i].getName();
                    LOG.logDebug( "Does '" + name + "' match '" + regex + "'? " );
                    if ( pattern.matcher( name ).matches() ) {
                        LOG.logDebug( "Yes." );
                        filteredUsers.add( users[i] );
                    } else {
                        LOG.logDebug( "No." );
                    }
                }
            }

            User[] us = filteredUsers.toArray( new User[filteredUsers.size()] );
            getRequest().setAttribute( "USERS", us );
        } catch ( PatternSyntaxException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_ERROR_GET_USERS_REGEX", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage() );
        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_ERROR_GET_USERS_REQUEST", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage() );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_GET_USERS", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage() );
        }

    }
}
