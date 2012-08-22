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
package org.deegree.portal.portlet.enterprise;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.jetspeed.om.security.BaseJetspeedUser;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.portal.PortalException;
import org.deegree.portal.common.control.AbstractSimplePrintListener;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;
import org.deegree.security.drm.model.User;

/**
 * performs a print request/event by creating a PDF document from the current map
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SimplePrintListener extends AbstractSimplePrintListener {

    /**
     * returns the user who is requesting the print map view or <code>null</code> if no user
     * information is available
     *
     * @return the user who is requesting the print map view or <code>null</code> if no user
     *         information is available
     */
    @Override
    protected User getUser() {
        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
        BaseJetspeedUser user = (BaseJetspeedUser) session.getAttribute( "turbine.user" );
        String userName = null;
        String password = null;
        if ( user != null ) {
            userName = user.getUserName();
            password = user.getPassword();
            return new User( 1, userName, password, null, null, null, null );
        }
        return null;
    }

    /**
     * reads the view context to print from the users session
     *
     * @param rpc
     * @return ViewContext
     */
    @Override
    protected ViewContext getViewContext( RPCWebEvent rpc ) {
        RPCStruct struct = (RPCStruct) rpc.getRPCMethodCall().getParameters()[1].getValue();
        String mmid = (String) struct.getMember( "MAPMODELID" ).getValue();
        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
        return IGeoPortalPortletPerform.getCurrentViewContext( session, mmid );
    }

    /**
     * validates the incoming request/RPC if conatins all required elements
     *
     * @param rpc
     * @throws PortalException
     */
    @Override
    protected void validate( RPCWebEvent rpc )
                            throws PortalException {
        super.validate( rpc );
        RPCStruct struct = (RPCStruct) rpc.getRPCMethodCall().getParameters()[1].getValue();
        if ( struct.getMember( "MAPMODELID" ) == null ) {
            throw new PortalException( "struct member: 'MAPMODELID' must be set" );
        }
    }

}
