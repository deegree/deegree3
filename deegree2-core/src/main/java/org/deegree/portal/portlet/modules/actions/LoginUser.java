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
package org.deegree.portal.portlet.modules.actions;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.deegree.portal.context.ViewContext;
import org.deegree.portal.context.WebMapContextFactory;
import org.deegree.security.drm.model.User;

/**
 * This class can be used within listener classes that will be called if a user logs in into a
 * portal. It reads the context documents from the users context directory that are storing the
 * state of the maps when the users has logged out the last time. <BR>
 * At the moment a concrete listener is available for Jetspeed 1.6
 *
 * @see org.deegree.portal.portlet.modules.actions.IGeoJetspeed16LoginUser
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class LoginUser {

    /**
     * validates if a WMC directory for the passed user is already available and creates it if not.
     * The user's WMC directory will be returned as in instance of <@link File>
     *
     * @param user
     * @param sc
     * @return user's WMC directory
     */
    File ensureDirectory( ServletContext sc, String user ) {

        File dir = new File( sc.getRealPath( "WEB-INF/wmc/" + user ) );
        if ( !dir.exists() ) {
            dir.mkdir();
        }

        return dir;
    }

    /**
     *
     * @param dir
     * @param ses
     * @param user
     */
    void readContextDocuments( File dir, HttpSession ses, User user ) {
        File[] files = dir.listFiles();
        // we have to look for all files stored in the user's WMC
        // directory and read those which can be identified as stored
        // when the has been logged out the last time. These files will
        // read and stored in the users session where the file name
        // ( without extension ) is the attributes key and will
        // be used by the portlets to access the assigned WMC
        if ( files != null ) {
            for ( int i = 0; i < files.length; i++ ) {
                String name = files[i].getName();
                if ( name.endsWith( AbstractPortletPerform.CURRENT_WMC + ".xml" ) ) {

                    int pos = name.lastIndexOf( '.' );
                    name = name.substring( 0, pos );
                    ViewContext vc = null;
                    try {
                        vc = WebMapContextFactory.createViewContext( files[i].toURL(), user, null );
                    } catch ( Exception e ) {
                        e.printStackTrace();
                    }
                    ses.setAttribute( name, vc );

                }
            }
        }
    }

}
