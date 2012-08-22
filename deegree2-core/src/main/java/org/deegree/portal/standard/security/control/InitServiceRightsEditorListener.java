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

import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.ServletRequest;

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
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.Service;
import org.deegree.security.drm.model.User;

/**
 * This <code>Listener</code> reacts on RPC-EditRole events, extracts the submitted role-id and passes the role + known
 * securable objects on the JSP.
 * <p>
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 * 
 * @author <a href="mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InitServiceRightsEditorListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( InitServiceRightsEditorListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {
        ServletRequest request = getRequest();
        try {
            // perform access check
            SecurityAccess access = SecurityHelper.acquireAccess( this );
            SecurityHelper.checkForAdminRole( access );
            User user = access.getUser();

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

            // get Role to be edited
            Role role = access.getRoleById( roleId );

            // check if user has the right to update the role
            if ( !user.hasRight( access, RightType.UPDATE, role ) ) {
                throw new GeneralSecurityException( Messages.getMessage( "IGEO_STD_SEC_MISSING_RIGHT", role.getName() ) );
            }

            LinkedList<Service> services = access.getRolesServices( role );
            LinkedList<Service> otherServices = access.getAllServices();
            ListIterator<Service> iter = otherServices.listIterator();
            while ( iter.hasNext() ) {
                Service s = iter.next();
                if ( services.contains( s ) ) {
                    iter.remove();
                }
            }
            request.setAttribute( "SELECTED_SERVICES", services );
            request.setAttribute( "AVAILABLE_SERVICES", otherServices );
            request.setAttribute( "ROLE", role );
        } catch ( RPCException e ) {
            LOG.logError( e.getMessage(), e );
            request.setAttribute( "SOURCE", this.getClass().getName() );
            request.setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_RIGHTSEDITOR_REQUEST",
                                                                  e.getMessage() ) );
            setNextPage( "error.jsp" );
        } catch ( GeneralSecurityException e ) {
            LOG.logError( e.getMessage(), e );
            request.setAttribute( "SOURCE", this.getClass().getName() );
            request.setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_RIGHTSEDITOR", e.getMessage() ) );
            setNextPage( "error.jsp" );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }

    }

}
