//$$Header: $$
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

package org.deegree.enterprise.servlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.portal.owswatch.Constants;
import org.deegree.portal.owswatch.JSPagesReference;
import org.deegree.portal.owswatch.Messages;
import org.deegree.portal.owswatch.ServiceConfiguration;
import org.deegree.portal.owswatch.ServiceWatcher;
import org.deegree.portal.owswatch.ServiceWatcherFactory;

/**
 * Used to authenticate the user in order to view the Protocol file
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ProtocolServlet extends HttpServlet implements Serializable {

    private static final ILogger LOG = LoggerFactory.getLogger( ProtocolServlet.class );

    private final String SESSIONID_KEY = Constants.SESSIONID_KEY;

    private ServiceWatcher watcher = null;

    private String webinfPath = null;

    private String confFilePath = null;

    private ServiceWatcherFactory factory = null;

    /**
     *
     */
    private static final long serialVersionUID = -6509717095713986594L;

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init()
                            throws ServletException {
        confFilePath = this.getServletContext().getRealPath( this.getInitParameter( "owsWatchConfiguration" ) );
        webinfPath = this.getServletContext().getRealPath( "WEB-INF/conf/owswatch" );
        if ( !webinfPath.endsWith( "/" ) ) {
            webinfPath = webinfPath.concat( "/" );
        }
        try {
            factory = ServiceWatcherFactory.getInstance( confFilePath, webinfPath );
            watcher = factory.getServiceWatcherInstance();
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            return;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        PerformAction( request, response );
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        PerformAction( request, response );
    }

    protected void PerformAction( HttpServletRequest request, HttpServletResponse response ) {

        String action = request.getParameter( "action" );
        if ( action == null ) {
            gotoErrorPage( request, response, "The action value is null", null, null );
            return;
        }
        if ( action.equals( "loginProtocol" ) ) {
            handleLoginProtocol( request, response );
        } else if ( action.equals( "serviceProtocol" ) ) {
            handleServiceProtocol( request, response );
        } else {
            gotoErrorPage( request, response, StringTools.concat( 100, "action: ", action,
                                                                  " is unknown to this servlet" ), null, null );
        }
    }

    /**
     * Handle login for Protocol requests
     *
     * @param request
     * @param response
     */
    private boolean handleLoginProtocol( HttpServletRequest request, HttpServletResponse response ) {

        String user = request.getParameter( "username" );
        String pwd = request.getParameter( "password" );
        try {
            if ( factory.getConf().isAuthenticatedUser( user, pwd ) ) {
                HttpSession session = request.getSession( true );
                // isLoggedin
                String sessionId = UUID.randomUUID().toString();
                session.setAttribute( SESSIONID_KEY, sessionId );
                String serviceId = (String) session.getAttribute( "serviceId" );
                String nextpage = StringTools.concat( 200, "wprotocol?action=serviceProtocol&serviceId=", serviceId,
                                                      "&", SESSIONID_KEY, "=", sessionId );
                response.sendRedirect( nextpage );
            } else {
                gotoErrorPage( request, response, Messages.getMessage( "INCORRECT_LOGIN" ),
                               Messages.getMessage( "MESSAGE_GOTO_MAIN" ),
                               JSPagesReference.getString( "OWSWatch.login" ) );
            }
        } catch ( Exception e ) {
            String errorMsg = StringTools.concat( 100, Messages.getMessage( "ERROR_LOGIN" ), "</br>",
                                                  e.getLocalizedMessage() );
            gotoErrorPage( request, response, errorMsg, Messages.getMessage( "MESSAGE_GOTO_MAIN" ),
                           JSPagesReference.getString( "OWSWatch.login" ) );
            return false;
        }
        return true;
    }

    /**
     * forwards the Response to the error page
     *
     * @param request
     * @param response
     * @param error
     * @param urlText
     * @param url
     */
    private void gotoErrorPage( HttpServletRequest request, HttpServletResponse response, String error, String urlText,
                                String url ) {

        LOG.logError( error );
        HttpSession session = request.getSession( true );

        session.setAttribute( "message", StringTools.replace( error, "\n", "<br/>", true ) );
        if ( error == null ) {
            error = "An unknown error has occured";
        }
        if ( urlText == null ) {
            urlText = "";
        }
        session.setAttribute( "URLText", urlText );
        if ( url == null ) {
            url = "";
        }
        session.setAttribute( "URLAdd", url );
        try {
            String nextpage = JSPagesReference.getString( "OWSWatch.error" );
            response.sendRedirect( nextpage );
        } catch ( Exception e ) {
            LOG.logError( "The page could not be redirected to the error page" );
        }
    }

    /**
     * sends the protocol of a serviceMonitor identified by its protIndex (got from request.getParameter()) object has
     * as html file to the browser
     *
     */
    private boolean handleServiceProtocol( HttpServletRequest request, HttpServletResponse response ) {

        if ( watcher == null || !isLoggedIn( request ) ) {
            String serviceId = request.getParameter( "serviceId" );
            if ( serviceId == null ) {
                return handleLogout( request, response );
            }
            String sessionId = (String) request.getSession().getAttribute( SESSIONID_KEY );
            request.getSession().setAttribute( "serviceId", serviceId );
            if ( sessionId == null ) {
                String next = JSPagesReference.getString( "OWSWatch.protocolLogin" );
                // If the user is not logged in, this is to check that the user didn't just logged
                // in for another protocol, so that the user does not have to login everytime he
                // clicks a protocol link
                try {
                    response.sendRedirect( next );
                    return true;
                } catch ( IOException e ) {
                    gotoErrorPage( request, response, Messages.getMessage( "ERROR_PAGE_NOT_FOUND", next ), null, null );
                }
            }
        }
        int serviceId = Integer.parseInt( request.getParameter( "serviceId" ) );

        ServiceConfiguration serviceConfiguration = watcher.getService( serviceId );
        if ( serviceConfiguration == null ) {
            gotoErrorPage( request, response, Messages.getMessage( "ERROR_NULL_OBJ", "ServiceConfiguration" ),
                           Messages.getMessage( "MESSAGE_GOTO_MAIN" ),
                           JSPagesReference.getString( "OWSWatch.owswatchMonitorList" ) );
            return false;
        }

        String xmlURI = watcher.getServiceLogs().get( serviceConfiguration ).getProtocolURI();
        File xmlFile = new File( xmlURI );
        String xslURI = getProtocolURL().concat( JSPagesReference.getString( "OWSWatch.protocolXSLScript" ) );
        File xslFile = new File( xslURI );
        XSLTDocument sheet = new XSLTDocument();

        XMLFragment input = new XMLFragment();

        XMLFragment result = null;

        try {
            input.load( xmlFile.toURL() );
            sheet.load( xslFile.toURL() );
            result = sheet.transform( input );
        } catch ( Exception e ) {
            gotoErrorPage( request, response, Messages.getMessage( "ERROR_LOADING_XML_FILE", "handleServiceProtocol()",
                                                                   xmlFile.getAbsolutePath() ),
                           Messages.getMessage( "MESSAGE_GOTO_MAIN" ),
                           JSPagesReference.getString( "OWSWatch.owswatchMonitorList" ) );
            return false;
        }
        String s = result.getAsString();

        request.setAttribute( "TABLE", s );
        String idx = String.valueOf( serviceId );
        request.setAttribute( "newWinProtocol", idx );
        String next = null;
        try {
            next = JSPagesReference.getString( "OWSWatch.protocolJSP" );
            getServletConfig().getServletContext().getRequestDispatcher( next ).forward( request, response );
        } catch ( Exception e ) {
            gotoErrorPage( request, response, Messages.getMessage( "ERROR_PAGE_NOT_FOUND", next ),
                           Messages.getMessage( "MESSAGE_GOTO_MAIN" ),
                           JSPagesReference.getString( "OWSWatch.owswatchMonitorList" ) );
            return false;
        }
        return true;
    }

    /**
     * Logs the user out
     *
     * @return true if logout successfully, false otherwise
     */
    private boolean handleLogout( HttpServletRequest request, HttpServletResponse response ) {

        HttpSession session = request.getSession( true );
        session.setAttribute( "isLoggedin", false );
        session.removeAttribute( SESSIONID_KEY );

        String nextPage = JSPagesReference.getString( "OWSWatch.login" );
        try {
            response.sendRedirect( nextPage );
        } catch ( Exception e ) {
            gotoErrorPage( request, response, Messages.getMessage( "ERROR_PAGE_NOT_FOUND", nextPage ), null, null );
            return false;
        }

        return true;
    }

    /**
     * Verifies that this user is loggedin through comparing the session ID from the request paarameter with that saved
     * in the session
     *
     * @param request
     * @return true if the user is loggedin, false otherwise
     */
    protected boolean isLoggedIn( HttpServletRequest request ) {
        HttpSession session = request.getSession( true );
        String requestSession = request.getParameter( SESSIONID_KEY );
        String sessionId = (String) session.getAttribute( SESSIONID_KEY );
        if ( requestSession == null || sessionId == null || !requestSession.equals( sessionId ) ) {
            return false;
        }
        return true;
    }

    /**
     * @return the Location of the protocol of this Service
     */
    public String getProtocolURL() {
        String protDirePath = factory.getProtDirPath();
        return protDirePath.endsWith( "/" ) ? protDirePath : protDirePath.concat( "/" );
    }
}
