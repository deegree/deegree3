//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.portal.PortalException;

/**
 * Listener class that enables deleting a named conetxt
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ContextDeleteListener extends AbstractContextListener {

    private static ILogger LOG = LoggerFactory.getLogger( ContextDeleteListener.class );

    /**
     * WEB-INF/conf/igeoportal/
     */
    private static String confDir = "WEB-INF/conf/igeoportal/";

    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpc = (RPCWebEvent) event;
        try {
            validate( rpc );
        } catch ( PortalException e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CNTXT_NO_VALID_RPC", e.getMessage() ) );
            return;
        }

        String userName = null;
        try {
            userName = extractUserName( rpc );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CNTXT_INVALID_USER_IDENTIFICATION", e.getMessage() ) );
            return;
        }

        String contextName = readContextName( rpc );
        try {
            deleteContext( contextName, userName );
        } catch ( Exception e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CNTXT_DELETE_FAILED", e.getMessage() ) );
            e.printStackTrace();
            return;
        }

        int pos = contextName.lastIndexOf( "/" );
        contextName = contextName.substring( pos + 1, contextName.length() );
        List<String> contextList = null;
        try {
            contextList = getContextList( userName );
        } catch ( Exception e ) {
            gotoErrorPage( "List of available context documents\n" + e.getMessage() );
            return;
        }

        getRequest().setAttribute( "CONTEXT", contextName );
        getRequest().setAttribute( "CONTEXT_LIST", contextList );
        getRequest().setAttribute( "USER", userName );
    }

    /**
     * validates if the passed RPC call containes the required variables
     * 
     * @param rpc
     * @throws ClientException
     */
    private void validate( RPCWebEvent rpc )
                            throws PortalException {
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter param = mc.getParameters()[0];
        RPCStruct struct = (RPCStruct) param.getValue();

        RPCMember selectedContext = struct.getMember( "mapContext" );
        if ( selectedContext == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_PARAM", "mapContext",
                                                            "ContextDelete" ) );
        }
        RPCMember sessionID = struct.getMember( "sessionID" );
        if ( sessionID == null ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_MISSING_PARAM", "sessionID",
                                                            "ContextDelete" ) );
        }
        LOG.logDebug( "*** RPC VALIDATION ***\n mapContext: ", selectedContext.getValue(), "\n sessionID: ",
                      sessionID.getValue() );
    }

    /**
     * reads the session ID from the passed RPC and gets the assigned user name from a authentification service.
     * 
     * @param event
     * @return
     * @throws Exception
     */
    private String extractUserName( RPCWebEvent event )
                            throws Exception {
        RPCMethodCall mc = event.getRPCMethodCall();
        RPCParameter[] pars = mc.getParameters();
        RPCStruct struct = (RPCStruct) pars[0].getValue();

        // get map context value
        String name = "default";
        try {
            name = getUserName( (String) struct.getMember( "sessionID" ).getValue() );
            if ( name == null ) {
                name = "default";
            }
        } catch ( Exception e ) {
        }
        return name;
    }

    private String readContextName( RPCWebEvent rpc ) {
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter param = mc.getParameters()[0];
        RPCStruct struct = (RPCStruct) param.getValue();
        return (String) struct.getMember( "mapContext" ).getValue();
    }

    /**
     * 
     * @param contextName
     * @param userName
     * @throws ClientException
     * @throws IOException
     */
    private void deleteContext( String contextName, String userName )
                            throws PortalException, IOException {

        String fName = getHomePath() + confDir + contextName;
        // ensure that no configuration files are deleted
        if ( !fName.endsWith( ".properties" ) && !fName.endsWith( ".properies~" ) ) {

            String startCnxt = getUsersStartContext( userName );
            if ( new File( startCnxt ).getName().equals( new File( fName ).getName() ) ) {
                throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_DELETE_STARTCONTEXT_NOT_ALLOWED",
                                                                contextName ) );
            }

            File file = new File( fName );
            if ( !file.exists() ) {
                throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_DOES_NOT_EXIST", contextName ) );
            }
            if ( !file.delete() ) {
                throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_NOT_DELETED", contextName ) );
            }
        }
    }

}
