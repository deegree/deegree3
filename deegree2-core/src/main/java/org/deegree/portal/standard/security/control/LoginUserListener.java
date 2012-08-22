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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.UnknownException;
import org.deegree.security.drm.WrongCredentialsException;
import org.deegree.security.drm.model.User;

/**
 * This <code>Listener</code> reacts on RPC-LoginUser events, extracts the submitted username +
 * password and tries to authenticate the user against the rights management subsystem.
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LoginUserListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( LoginUserListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            if ( params.length != 2 ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_PARAMS_NUM", "2" ) );
            }
            if ( params[0].getType() != String.class || params[1].getType() != String.class ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_STRING" ) );
            }
            String userName = (String) params[0].getValue();
            String password = (String) params[1].getValue();

            // login user to SecurityAccessManager
            SecurityAccessManager manager = SecurityAccessManager.getInstance();
            User user = manager.getUserByName( userName );
            user.authenticate( password );

            // set USERNAME and PASSWORD in HttpSession
            HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
            session.setAttribute( ClientHelper.KEY_USERNAME, userName );
            session.setAttribute( ClientHelper.KEY_PASSWORD, password );
        } catch ( UnknownException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_WRONG_LOGIN" ) );
            setNextPage( "index.jsp" );
            LOG.logError( e.getMessage(), e );
        } catch ( WrongCredentialsException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_WRONG_LOGIN" ) );
            setNextPage( "index.jsp" );
            LOG.logError( e.getMessage(), e );
        } catch ( Exception e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_FAIL_LOGIN", e.getMessage() ) );
            setNextPage( "index.jsp" );
            LOG.logError( e.getMessage(), e );
        }
    }
}
