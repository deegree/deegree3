//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-client/deegree-jsf-console/src/main/java/org/deegree/client/generic/LogBean.java $
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
package org.deegree.console.util;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.deegree.console.Navigation.CHANGE_PASSWORD;
import static org.deegree.console.Navigation.CONSOLE;
import static org.deegree.console.Navigation.LOGIN_FAILED;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 29926 $, $Date: 2011-03-08 11:47:59 +0100 (Di, 08. Mär 2011) $
 */
@ManagedBean
@SessionScoped
public class LogBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger( LogBean.class );

    // location of password file (relative to workspace root)
    private static final String PASSWORD_FILE = "../console-pw.txt";

    private static final long serialVersionUID = -4865071415988778817L;

    private boolean loggedIn = false;

    private String currentPassword;

    private String newPassword;

    private String newPassword2;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String logIn() {
        String storedPassword = getStoredPassword();
        String view = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if ( currentPassword != null && currentPassword.equals( storedPassword ) ) {
            loggedIn = true;
            if ( view.indexOf( "Failed" ) == -1 ) {
                return view;
            }
            return CONSOLE;
        }
        return LOGIN_FAILED;
    }

    private String getStoredPassword() {
        String storedPw = null;
        File pwFile = null;
        try {
            File workspace = OGCFrontController.getServiceWorkspace().getLocation();
            pwFile = new File( workspace, PASSWORD_FILE );

            if ( !pwFile.exists() ) {
                LOG.info( "Password file '{}' does not exist. Creating default with default content.", pwFile );
                if ( !pwFile.getParentFile().exists() ) {
                    pwFile.getParentFile().mkdirs();
                }
                PrintWriter writer = new PrintWriter( pwFile );
                writer.print( "deegree" );
                writer.close();
            }

            BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( pwFile ) ) );
            try {
                String line;
                while ( ( line = in.readLine() ) != null ) {
                    if ( !line.startsWith( "#" ) ) {
                        storedPw = line.trim();
                        break;
                    }
                }
            } finally {
                in.close();
            }
        } catch ( IOException e ) {
            LOG.warn( "Error reading password from file '{}': {}", pwFile, e.getMessage() );
        }
        return storedPw;
    }

    public String logOut() {
        loggedIn = false;
        return CONSOLE;
    }

    public String changePassword() {
        if ( !checkCurrentPassword() ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Current password is incorrect.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return CHANGE_PASSWORD;
        }
        if ( !newPasswordsMatch() ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "New passwords don't match.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return CHANGE_PASSWORD;
        }

        try {
            updatedStoredPassword( newPassword );
        } catch ( IOException e ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Error updating password: " + e.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return CHANGE_PASSWORD;
        }

        FacesMessage fm = new FacesMessage( FacesMessage.SEVERITY_INFO, "Password changed successfully.", null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
        return CHANGE_PASSWORD;
    }

    private void updatedStoredPassword( String newPassword )
                            throws IOException {

        File pwFile = null;
        File workspace = OGCFrontController.getServiceWorkspace().getLocation();
        pwFile = new File( workspace, PASSWORD_FILE );

        if ( pwFile.exists() ) {
            if ( !pwFile.delete() ) {
                throw new IOException( "Could not delete password file '" + pwFile + "'." );
            }
        }

        if ( !pwFile.getParentFile().exists() ) {
            pwFile.getParentFile().mkdirs();
        }
        PrintWriter writer = new PrintWriter( pwFile );
        writer.print( newPassword );
        writer.close();
    }

    private boolean newPasswordsMatch() {
        if ( newPassword == null ) {
            return newPassword2 == null;
        }
        return newPassword.equals( newPassword2 );
    }

    private boolean checkCurrentPassword() {
        String storedPassword = getStoredPassword();
        if ( currentPassword != null && currentPassword.equals( storedPassword ) ) {
            return true;
        }
        return false;
    }

    public void setCurrentPassword( String currentPassword ) {
        this.currentPassword = currentPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setNewPassword( String newPassword ) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword2( String newPassword2 ) {
        this.newPassword2 = newPassword2;
    }

    public String getNewPassword2() {
        return newPassword2;
    }
}