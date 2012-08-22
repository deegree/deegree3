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

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.i18n.Messages;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.User;

/**
 * This <tt>Listener</tt> reacts on 'EditSubadminRole'-events, extracts the submitted role-id and
 * passes the role + known datasets on to the JSP.
 * <p>
 * Access constraints:
 * <ul>
 * <li>only users that have the 'Administrator'-role are allowed</li>
 * </ul>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class EditSubadminRoleListener extends AbstractListener {

    @Override
    public void actionPerformed( FormEvent event ) {

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            if ( params.length != 1 ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_PARAM_NUM" ) );
            }
            if ( params[0].getType() != String.class ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_STRING" ) );
            }
            int roleId = -1;
            try {
                roleId = Integer.parseInt( (String) params[0].getValue() );
            } catch ( NumberFormatException e ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_ROLE_VALUE" ) );
            }

            // get user
            SecurityAccessManager manager = SecurityAccessManager.getInstance();
            User user = manager.getUserByName( toModel().get( ClientHelper.KEY_USERNAME ) );
            SecurityAccess access = manager.acquireAccess( user );

            // perform access check
            ClientHelper.checkForAdminRole( access );
            Role role = access.getRoleById( roleId );

            // supply necessary data for the JSP
            getRequest().setAttribute( "TOKEN", access );
            getRequest().setAttribute( "ROLE", role );
            getRequest().setAttribute( "DATASETS", access.getAllSecuredObjects( "dataset" ) );
        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_FAIL_ACCESS_ROLEEDITOR", e.getMessage() ) );
            setNextPage( "admin/admin_error.jsp" );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_ERROR_ROLE_EDITOR", e.getMessage() ) );
            setNextPage( "admin/admin_error.jsp" );
        }

    }
}
