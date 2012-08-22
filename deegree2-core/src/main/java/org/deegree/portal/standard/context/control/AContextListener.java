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

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.log.ILogger;
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
public abstract class AContextListener extends AbstractListener{
    
    private static ILogger LOG = LoggerFactory.getLogger( AContextListener.class );
    
    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * gets the user name assigned to the passed session ID from an authentication service. If no user is assigned to
     * the session ID <tt>null</tt> will be returned. If the session is closed or expired an exception will be thrown
     * 
     * @param sessionId
     * @return name of the user assigned to the passed session ID
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     */
    protected String getUserName( String sessionId )
                            throws XMLParsingException, IOException, SAXException {

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        if ( vc == null ) {
            return null;
        }
        GeneralExtension ge = vc.getGeneral().getExtension();
        String userName = null;
        if ( sessionId != null && ge.getAuthentificationSettings() != null ) {
            LOG.logDebug( "try getting user from WAS/sessionID" );
            BaseURL baseUrl = ge.getAuthentificationSettings().getAuthentificationURL();
            String url = OWSUtils.validateHTTPGetBaseURL( baseUrl.getOnlineResource().toExternalForm() );
            StringBuffer sb = new StringBuffer( url );
            sb.append( "request=DescribeUser&SESSIONID=" ).append( sessionId );

            XMLFragment xml = new XMLFragment();
            xml.load( new URL( sb.toString() ) );

            userName = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "/User/UserName", nsContext );
        } else {
            LOG.logDebug( "try getting user from getUserPrincipal()" );
            if ( ( (HttpServletRequest) getRequest() ).getUserPrincipal() != null ) {
                userName = ( (HttpServletRequest) getRequest() ).getUserPrincipal().getName();
                if ( userName.indexOf( "\\" ) > 1 ) {
                    String[] us = StringTools.toArray( userName, "\\", false );
                    userName = us[us.length - 1];
                }
            }
        }
        LOG.logDebug( "userName: " + userName );
        return userName;
    }

}
