//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.generic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@SessionScoped
public class LogBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger( LogBean.class );

    // location of password file (relative to workspace root)
    private static final String PASSWORD_FILE = "manager/password.txt";

    private static final long serialVersionUID = -4865071415988778817L;

    private String password;

    private boolean loggedIn = false;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public Object logIn() {
        String correctPw = null;
        File pwFile = null;
        try {
            File workspace = OGCFrontController.getServiceWorkspace().getLocation();
            pwFile = new File( workspace, PASSWORD_FILE );
            BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( pwFile ) ) );
            try {
                String line;
                while ( ( line = in.readLine() ) != null ) {
                    if ( !line.startsWith( "#" ) ) {
                        correctPw = line.trim();
                        break;
                    }
                }
            } finally {
                in.close();
            }
        } catch ( IOException e ) {
            LOG.warn( "Error reading password from file '{}': {}", pwFile, e.getMessage() );
        }
        if ( password != null && password.equals( correctPw ) ) {
            loggedIn = true;
            return "success";
        } else {
            return "failed";
        }
    }

    public Object logOut() {
        loggedIn = false;
        return "/console";
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}