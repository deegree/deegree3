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
package org.deegree.portal.standard.admin.control;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.portal.Constants;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.context.Frontend;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.portal.context.XMLFactory;

/**
 * Creates a new WMC for one or more users bases on the current WMC. The new WMCs will contain the modules defined by
 * the admin sending the request.
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CreateWMCListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( CreateWMCListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        LOG.logDebug( "parameters", event.getParameter() );
        Map<String, Object> parameter = event.getParameter();

        // name of the new context
        String wmc = parameter.get( "wmc" ).toString();
        if ( !wmc.toLowerCase().endsWith( ".xml" ) ) {
            wmc += ".xml";
        }

        // list of user who will be considered
        List<Object> users = (List<Object>) parameter.get( "users" );

        // Modules to be used within the new context
        Map<String, Object> modules = (Map<String, Object>) parameter.get( "modules" );
        List<Map<String, Object>> children = (List<Map<String, Object>>) modules.get( "children" );
        modules = (Map<String, Object>) children.get( 0 );
        children = (List<Map<String, Object>>) modules.get( "children" );

        HttpSession session = event.getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        ViewContext newVC = null;
        try {
            XMLFragment xml = XMLFactory.export( vc );
            newVC = WebMapContextFactory.createViewContext( xml, null, null );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            responseHandler.writeAndClose( true, new ExceptionBean( getClass().getName(), e.getMessage() ) );
            return;
        }
        // clear context by removing all modules
        Frontend frontend = newVC.getGeneral().getExtension().getFrontend();
        frontend.getCenter().removeAll();
        frontend.getEast().removeAll();
        frontend.getNorth().removeAll();
        frontend.getSouth().removeAll();
        frontend.getWest().removeAll();

        // add selected modules
        for ( Map<String, Object> map : children ) {
            String area = (String) map.get( "id" );
            List<Map<String, Object>> mds = (List<Map<String, Object>>) map.get( "children" );
            for ( Map<String, Object> moduleMap : mds ) {
                Module[] ctxModules = vc.getGeneral().getExtension().getFrontend().getModulesByName(
                                                                                                     (String) moduleMap.get( "id" ) );
                if ( "center".equalsIgnoreCase( area ) ) {
                    frontend.getCenter().addModul( ctxModules[0] );
                } else if ( "east".equalsIgnoreCase( area ) ) {
                    frontend.getEast().addModul( ctxModules[0] );
                } else if ( "west".equalsIgnoreCase( area ) ) {
                    frontend.getWest().addModul( ctxModules[0] );
                } else if ( "south".equalsIgnoreCase( area ) ) {
                    frontend.getSouth().addModul( ctxModules[0] );
                } else if ( "north".equalsIgnoreCase( area ) ) {
                    frontend.getNorth().addModul( ctxModules[0] );
                }
            }
        }

        // because it is possible that the user and rights DB contains users that has not been
        // registered to iGeoPortal yet for each of these 'new' users a directory must be created
        // underneath conf/igeoportal/users directory
        createUserDirectoriesIfRequired( users );

        try {
            XMLFragment xml = XMLFactory.export( newVC );
            String s = getHomePath() + "WEB-INF/conf/igeoportal/users";
            for ( Object object : users ) {
                String user = (String) object;
                FileUtils.writeToFile( s + '/' + user + '/' + wmc, xml.getAsPrettyString() );
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            responseHandler.writeAndClose( true, new ExceptionBean( getClass().getName(), e.getMessage() ) );
            return;
        }
        responseHandler.writeAndClose( "Success" );
    }

    /**
     * @param users
     */
    private void createUserDirectoriesIfRequired( List<Object> users ) {
        String s = getHomePath() + "WEB-INF/conf/igeoportal/users";
        File dir = new File( s );
        File[] userDirs = dir.listFiles();
        for ( Object object : users ) {
            String user = (String) object;
            boolean exists = false;
            for ( File file : userDirs ) {
                if ( file.isDirectory() && file.getName().equalsIgnoreCase( user ) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                File tmp = new File( s + '/' + user );
                if ( !tmp.mkdir() ) {
                    LOG.logError( "could not create directory for new user: " + user );
                } else {
                    LOG.logInfo( "directory for user: " + user + " created." );
                }
            }
        }

    }
}
