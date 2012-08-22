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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;

/**
 * Listener to retrieve the (deegree managed) sessionID of a user.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetSessionIDListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GetSessionIDListener.class );

    private static String userDir = "WEB-INF/conf/igeoportal/users/";

    /**
     * performs a login request. the passed event contains a RPC method call containing user name
     * and password.
     *
     * @param event
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent re = (RPCWebEvent) event;

        if ( !validateRequest( re ) ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_SEC_MISSING_USER" ) );
            return;
        }

        try {
            // write request parameter into session to reconstruct the search form
            HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
            getRequest().setAttribute( "SESSIONID", session.getAttribute( "SESSIONID" ) );
            getRequest().setAttribute( "STARTCONTEXT", getUsersStartContext( re ) );
        } catch ( IOException e ) {
            gotoErrorPage( Messages.getMessage( "IGEO_STD_SEC_ERROR_STARTCONTEXT" ) );
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * validates the passed event to be valid agaist the requirements of the listener (contains user
     * name )
     *
     * @param event
     * @return boolean
     */
    private boolean validateRequest( RPCWebEvent event ) {
        RPCMethodCall mc = event.getRPCMethodCall();
        if ( mc.getParameters().length == 0 ) {
            return false;
        }
        RPCStruct struct = (RPCStruct) mc.getParameters()[0].getValue();
        if ( struct.getMember( "user" ) == null ) {
            return false;
        }

        return true;
    }

    /**
     * returns the name of the users start context. If the user does not own an individual start
     * context the name of the default start context for all users will be returned.
     *
     * @param event
     * @return String
     * @throws IOException
     */
    private String getUsersStartContext( RPCWebEvent event )
                            throws IOException {
        RPCMethodCall mc = event.getRPCMethodCall();
        RPCStruct struct = (RPCStruct) mc.getParameters()[0].getValue();
        String userName = (String) struct.getMember( "user" ).getValue();

        StringBuffer dir = new StringBuffer( "users/" );

        StringBuffer sb = new StringBuffer( 300 );
        sb.append( getHomePath() ).append( userDir ).append( userName );
        sb.append( "/context.properties" );
        File file = new File( sb.toString() );

        if ( !file.exists() ) {
            sb.delete( 0, sb.length() );
            sb.append( getHomePath() ).append( userDir ).append( "context.properties" );
            file = new File( sb.toString() );
        } else {
            dir.append( userName ).append( '/' );
        }

        Properties prop = new Properties();
        InputStream is = file.toURL().openStream();
        prop.load( is );
        is.close();

        return dir.append( prop.getProperty( "STARTCONTEXT" ) ).toString();
    }

}
