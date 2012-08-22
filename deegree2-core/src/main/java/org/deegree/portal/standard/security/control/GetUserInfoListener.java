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
package org.deegree.portal.standard.security.control;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.portal.Constants;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.ViewContext;
import org.xml.sax.SAXException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetUserInfoListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GetUserInfoListener.class );

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        HttpSession session = event.getSession();
        try {
            UserBean user = readUserInformation( session );
            responseHandler.writeAndClose( false, user );
        } catch ( Exception e ) {
            LOG.logError( e );
            responseHandler.writeAndClose( "ERROR: can not read user informations" );
        }
    }

    /**
     * gets the user name assigned to the passed session ID from an authentication service. If no user is assigned to
     * the session ID <tt>null</tt> will be returned. If the session is closed or expired an exception will be thrown
     * 
     * @param session
     * @return name of the user assigned to the passed session ID
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     */
    protected UserBean readUserInformation( HttpSession session )
                            throws XMLParsingException, IOException, SAXException {

        String sessionId = null;
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        if ( vc == null ) {
            return null;
        }
        GeneralExtension ge = vc.getGeneral().getExtension();
        UserBean user = null;

        if ( sessionId != null && ge.getAuthentificationSettings() != null ) {
            LOG.logDebug( "try getting user from WAS/sessionID" );
            BaseURL baseUrl = ge.getAuthentificationSettings().getAuthentificationURL();
            String url = OWSUtils.validateHTTPGetBaseURL( baseUrl.getOnlineResource().toExternalForm() );
            StringBuffer sb = new StringBuffer( url );
            sb.append( "request=DescribeUser&SESSIONID=" ).append( sessionId );

            XMLFragment xml = new XMLFragment();
            xml.load( new URL( sb.toString() ) );

            String userName = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "/User/UserName", nsContext );
            String firstName = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "/User/FirstName", nsContext );
            String lastName = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "/User/LastName", nsContext );
            String mailAddress = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "/User/EMailAddress",
                                                                   nsContext );
            user = new UserBean( userName, firstName, lastName, mailAddress );
        } else {
            LOG.logDebug( "try getting user from getUserPrincipal()" );
            if ( ( (HttpServletRequest) getRequest() ).getUserPrincipal() != null ) {
                String userName = ( (HttpServletRequest) getRequest() ).getUserPrincipal().getName();
                if ( userName.indexOf( "\\" ) > 1 ) {
                    String[] us = StringTools.toArray( userName, "\\", false );
                    userName = us[us.length - 1];
                }
            }
        }
        LOG.logDebug( "userName: " + user.getUserName() );
        return user;
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // inner classes
    // ////////////////////////////////////////////////////////////////////////////////

    public class UserBean {
        private String userName = null;

        private String firstName = null;

        private String lastName = null;

        private String mailAddress = null;

        /**
         * @param userName
         * @param firstName
         * @param lastName
         * @param mailAddress
         */
        public UserBean( String userName, String firstName, String lastName, String mailAddress ) {
            super();
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.mailAddress = mailAddress;
        }

        /**
         * @return the userName
         */
        public String getUserName() {
            return userName;
        }

        /**
         * @param userName
         *            the userName to set
         */
        public void setUserName( String userName ) {
            this.userName = userName;
        }

        /**
         * @return the firstName
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         * @param firstName
         *            the firstName to set
         */
        public void setFirstName( String firstName ) {
            this.firstName = firstName;
        }

        /**
         * @return the lastName
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * @param lastName
         *            the lastName to set
         */
        public void setLastName( String lastName ) {
            this.lastName = lastName;
        }

        /**
         * @return the mailAddress
         */
        public String getMailAddress() {
            return mailAddress;
        }

        /**
         * @param mailAddress
         *            the mailAddress to set
         */
        public void setMailAddress( String mailAddress ) {
            this.mailAddress = mailAddress;
        }

    }

}
