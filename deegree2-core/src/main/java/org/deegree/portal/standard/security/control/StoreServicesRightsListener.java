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
import java.util.List;

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
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.User;

/**
 * This <code>Listener</code> reacts on RPC-StoreRights events.
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
public class StoreServicesRightsListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( StoreServicesRightsListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        // the Role for which the rights are to be set
        int roleId = -1;

        List<Integer> selectedServices = null;

        SecurityAccessManager manager = null;
        SecurityTransaction transaction = null;

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            // validates the incoming method call and extracts the roleID
            roleId = validate( params );

            RPCParameter[] selected = (RPCParameter[]) params[1].getValue();
            selectedServices = new ArrayList<Integer>( selected.length );

            for ( int i = 0; i < selected.length; ++i ) {
                try {
                    selectedServices.add( Integer.parseInt( (String) selected[i].getValue() ) );
                } catch ( NumberFormatException e ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_ROLE_PARAM" ) );
                }
            }

            transaction = SecurityHelper.acquireTransaction( this );
            SecurityHelper.checkForAdminRole( transaction );

            manager = SecurityAccessManager.getInstance();
            User user = transaction.getUser();
            Role role = transaction.getRoleById( roleId );

            // perform access check
            if ( !user.hasRight( transaction, "update", role ) ) {
                getRequest().setAttribute( "SOURCE", this.getClass().getName() );
                String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_MISSING_RIGHTS", role.getName() );
                getRequest().setAttribute( "MESSAGE", s );
                setNextPage( "error.jsp" );
                return;
            }

            transaction.setServicesRights( selectedServices, role );

            manager.commitTransaction( transaction );
            transaction = null;
            String s = Messages.getMessage( "IGEO_STD_STORESERVICESRIGHTS_SUCCESS", role.getID() );
            getRequest().setAttribute( "MESSAGE", s );
        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_INVALID_REQ", e.getMessage() );
            getRequest().setAttribute( "MESSAGE", s );
            setNextPage( "error.jsp" );
            LOG.logDebug( e.getMessage(), e );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_ERROR", e.getMessage() );
            getRequest().setAttribute( "MESSAGE", s );
            setNextPage( "error.jsp" );
            LOG.logDebug( e.getMessage(), e );
        } finally {
            if ( manager != null && transaction != null ) {
                try {
                    manager.abortTransaction( transaction );
                } catch ( GeneralSecurityException e ) {
                    LOG.logDebug( e.getMessage(), e );
                }
            }
        }

    }

    private int validate( RPCParameter[] params )
                            throws RPCException {

        if ( params.length != 2 ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_PARAMS_NUM", "2" ) );
        }

        if ( !( params[0].getValue() instanceof String ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_FIRST_PARAM" ) );
        }

        // extract role-id
        int roleId = -1;
        try {
            roleId = Integer.parseInt( (String) params[0].getValue() );
        } catch ( NumberFormatException e ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_ROLE_PARAM" ) );
        }

        // extract Layer rights
        if ( !( params[1].getValue() instanceof RPCParameter[] ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_SECOND_PARAM" ) );
        }
        return roleId;
    }

}
