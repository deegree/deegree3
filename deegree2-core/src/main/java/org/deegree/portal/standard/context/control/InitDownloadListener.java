//$HeadURL$$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn
 and
 - lat/lon GmbH

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.portal.Constants;
import org.deegree.portal.context.DataService;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.LayerExtension;
import org.deegree.portal.context.ViewContext;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.User;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InitDownloadListener extends AbstractContextListener {

    private static final ILogger LOG = LoggerFactory.getLogger( InitDownloadListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.enterprise.control.AbstractListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        Map<String, String> map = toModel();

        try {
            String userName = null;
            String userPw = null;
            String email = null;

            if ( "null".equals( map.get( "SESSIONID" ) ) ) {
                // yes, this if condition is correct.
                // map entry is never NULL, but it may be "null".
                userName = ( (HttpServletRequest) getRequest() ).getUserPrincipal().getName(); // userName = "default"
            } else {
                userName = getUserName( (String) map.get( "SESSIONID" ) ); // userName = "MyName" or "default"
                Properties prop = new Properties();
                prop.load( getClass().getClassLoader().getResourceAsStream( "org/deegree/enterprise/servlet/ServletRequestWrapper.properties" ) );
                if ( !prop.get( "defaultuser" ).equals( userName ) ) {
                    userPw = getUserPassword( (String) map.get( "SESSIONID" ) );
                    email = "known";
                }
            }
            if( LOG.getLevel() == ILogger.LOG_DEBUG ){
                LOG.logDebug( "user name: ", userName );
            }
            Layer[] layers = getLayers( userName, userPw );
            
            // default value. MUST NOT BE CHANGED for backward compatibilities !!! (shp was the first supported format)
            String formats = "SHP";
            if ( getInitParameter( "DOWNLOAD_FORMAT" ) != null ) {
                formats = getInitParameter( "DOWNLOAD_FORMAT" );
            }
            List<String> formatArray = StringTools.toList( formats, ",", true );

            getRequest().setAttribute( "LAYERS", layers );
            getRequest().setAttribute( "EMAIL", email );
            getRequest().setAttribute( "FORMATS", formatArray );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( e.getMessage() );
            return;
        }
    }

    /**
     * returns true if the passed user is allowed to download the passed featureType.
     * 
     * If there is no security component installed, a user is authorized for all featuretypes. Otherwise, user
     * authorisation is checked against the user-rights database.
     * 
     * @param user
     * @param featureType
     * @return true if the user is authorized, false otherwise
     */
    private boolean isAuthorized( String user, String password, String featureType ) {

        boolean isSAM = false;
        
        if( LOG.getLevel() == ILogger.LOG_DEBUG ){
            LOG.logDebug( "SecurityAccesManagr.isInitialized: ", SecurityAccessManager.isInitialized() );
        }
        
        if ( !SecurityAccessManager.isInitialized() ) {
            isSAM = initSAM();
        } else {
            isSAM = true;
        }

        if ( !isSAM ) {
            // no security component -> user has all rights -> user is authorized
            if( LOG.getLevel() == ILogger.LOG_DEBUG ){
                LOG.logDebug( "no security component available -> user has all rights -> user is authorized" );
            }
            return true;
        } else {
            // security component is installed. checking for user rights
            try {
                SecurityAccessManager sam = SecurityAccessManager.getInstance();
                User usr = sam.getUserByName( user );
                usr.authenticate( password );
                SecurityAccess access = sam.acquireAccess( usr );
                SecuredObject secObj = access.getSecuredObjectByName( featureType, "Featuretype" );
                if ( !usr.hasRight( access, RightType.GETFEATURE, secObj ) ) {
                    LOG.logError( "You are trying to access a feature/resource on a securedObject, which you do not have authentication to: "
                                  + featureType );
                    return false;
                } else {
                    return true;
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                return false;
            }
        }
    }

    /**
     * @return true, if initialization of SecurityAccessManager was successful. return false otherwise.
     */
    protected boolean initSAM() {
        if( LOG.getLevel() == ILogger.LOG_DEBUG ){
            LOG.logDebug( "try to init security access manager" );
        }
        String driver = getInitParameter( "driver" );
        String url = getInitParameter( "url" );
        String user = getInitParameter( "user" );
        String password = getInitParameter( "password" );

        Properties properties = new Properties();
        if ( driver != null && url != null && user != null && password != null ) {
            properties.setProperty( "driver", driver );
            properties.setProperty( "url", url );
            properties.setProperty( "user", user );
            properties.setProperty( "password", password );
        }
        try {
            SecurityAccessManager.initialize( "org.deegree.security.drm.SQLRegistry", properties, 60 * 1000 );
        } catch ( Exception e ) {
            LOG.logError( "security access manager could not be initialized." );
            return false;
        }
        return true;
    }

    /**
     * returns a list of layers that are downloadable (assigned to a WFS).
     * 
     * If a security component (WAS) is installed, it is checked for which layers the passed user has access rights. If
     * NO security ocmponent is available, all downloadable layers will be returned.
     * 
     * @param user
     *            username
     * @param pw
     *            password
     * @return all downloadable layers allowed for the given user
     */
    private Layer[] getLayers( String user, String pw ) {
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        List<Layer> list = new ArrayList<Layer>();
        Layer[] layers = vc.getLayerList().getLayers();
        LOG.logDebug( "Calling getLayers()" );
        for ( int i = 0; i < layers.length; i++ ) {
            // gets the dataservice (WFS) that is responsible for delivering a layers data
            DataService ds = ( (LayerExtension) layers[i].getExtension() ).getDataService();
            if ( ds != null ) {
                if ( isAuthorized( user, pw, ds.getFeatureType() ) ) {
                    list.add( layers[i] );
                    LOG.logDebug( "Adding layer '", layers[i].getName(), "' to list" );
                } else {
                    LOG.logDebug( "You are not authorized to layer '", layers[i].getName(), "'" );
                }
            } else {
                LOG.logDebug( "Dataservice for layer '", layers[i].getName(), "' is null" );
            }
        }
        layers = new Layer[list.size()];
        return (Layer[]) list.toArray( layers );
    }

}
