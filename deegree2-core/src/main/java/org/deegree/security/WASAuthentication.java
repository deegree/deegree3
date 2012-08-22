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
package org.deegree.security;

import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.WrongCredentialsException;
import org.deegree.security.drm.model.User;
import org.w3c.dom.Document;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WASAuthentication extends AbstractAuthentication {

    private static final ILogger LOG = LoggerFactory.getLogger( WASAuthentication.class );

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    protected static final String AUTH_PARAM_SESSIONID = "SESSIONID";

    protected static final String INIT_PARAM_WAS = "WAS";

    protected static final String INIT_PARAM_BASEREQUEST = "WAS";

    /**
     *
     * @param authenticationName
     * @param initParams
     */
    public WASAuthentication( String authenticationName, Map<String, String> initParams ) {
        super( authenticationName, initParams );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.security.AbstractAuthentication#authenticate(java.util.Map,
     * javax.servlet.http.HttpServletRequest)
     */
    public User authenticate( Map<String, String> params, HttpServletRequest request )
                            throws WrongCredentialsException {
        String sessionID = params.get( AUTH_PARAM_SESSIONID );
        User usr = null;
        if ( sessionID != null ) {
            String[] user = new String[3];
            String urlStr = initParams.get( INIT_PARAM_WAS );
            urlStr = urlStr.replaceFirst( "\\[SESSIONID\\]", sessionID );
            LOG.logDebug( "request WAS for user information: " + urlStr );
            Document doc = null;
            try {
                URL url = new URL( urlStr );
                XMLFragment xml = new XMLFragment( url );
                doc = xml.getRootElement().getOwnerDocument();
                user[0] = XMLTools.getNodeAsString( doc, "/User/UserName", nsContext, null );
                user[1] = XMLTools.getNodeAsString( doc, "/User/Password", nsContext, null );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new WrongCredentialsException( Messages.getMessage( "OWSProxyServletFilter.WASACCESS" ) );
            }

            if ( user[0] != null ) {
                try {
                    SecurityAccessManager sam = SecurityAccessManager.getInstance();
                    usr = sam.getUserByName( user[0] );
                    usr.authenticate( user[1] );
                } catch ( Exception e ) {
                    throw new WrongCredentialsException( Messages.getMessage( "OWSPROXY_USER_AUTH_ERROR", user[0] ) );
                }
            } else {
                String msg = "undefined error";
                try {
                    msg = XMLTools.getNodeAsString( doc, "//ServiceException", nsContext, "general error" );
                } catch ( Exception e ) {
                    // should never happen
                }
                throw new WrongCredentialsException( msg );
            }
        }

        return usr;
    }



}
