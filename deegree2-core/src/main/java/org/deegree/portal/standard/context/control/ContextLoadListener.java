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

package org.deegree.portal.standard.context.control;

import java.io.IOException;
import java.util.List;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCUtils;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.portal.PortalException;

/**
 * This listener generates a list of availavle ViewContexts. The only parameter passed is the user name. Currently only
 * .xml files are being accepted as contexts and it's also also assumed that those are available under
 * WEB-INF/xml/users/some_user user directories
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ContextLoadListener extends AbstractContextListener {

    private static final ILogger LOG = LoggerFactory.getLogger( ContextLoadListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpc = (RPCWebEvent) event;
        try {
            validate( rpc );
        } catch ( PortalException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CNTXT_INVALID_RPC", "ContextLoad", e.getMessage() ) );
            return;
        }

        String userName = extractUserName( rpc );
        List<String> contextList = getContextList( userName );
        String userStartContext;
        try {
            userStartContext = getUsersStartContext( userName );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_SEC_ERROR_STARTCONTEXT" ) );
            return;
        }

        getRequest().setAttribute( "CONTEXT_LIST", contextList );
        getRequest().setAttribute( "USER", userName );
        getRequest().setAttribute( "STARTCONTEXT", userStartContext );
    }

    /**
     * reads the session ID from the passed RPC and gets the assigned user name from a authentification service
     * 
     * @param event
     * @return the user name
     */
    private String extractUserName( RPCWebEvent event ) {
        RPCMethodCall mc = event.getRPCMethodCall();
        RPCParameter[] pars = mc.getParameters();
        RPCStruct struct = (RPCStruct) pars[0].getValue();

        // get map context value
        String name = "default";
        try {
            name = getUserName( RPCUtils.getRpcPropertyAsString( struct, "sessionID" ) );
            if ( name == null ) {
                name = "default";
            }
        } catch ( Exception e ) {
        }
        return name;
    }

    /**
     * validates if the passed RPC call containes the required variables
     * 
     * @param rpc
     * @throws PortalException
     */
    private void validate( RPCWebEvent rpc )
                            throws PortalException {
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter param = mc.getParameters()[0];
        RPCStruct struct = (RPCStruct) param.getValue();
        RPCMember sessionID = struct.getMember( "sessionID" );
        if ( sessionID == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_PARAM", "sessionID", "ContextLoad" ) );
        }
    }

}
