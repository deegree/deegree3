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
package org.deegree.console.security;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.deegree.commons.config.DeegreeWorkspace;
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

    private static final long serialVersionUID = -4865071415988778817L;

    private static final String PASSWORD_FILE = "console.pw";
    
    public static final String CONSOLE = "/console";

    public static final String CHANGE_PASSWORD = "/console/security/password";

    private final PasswordFile passwordFile;

    private boolean loggedIn = false;

    private String currentPassword;

    private String newPassword;

    private String newPassword2;

    public LogBean() {
        passwordFile = new PasswordFile( getPasswordFile() );
    }

    private File getPasswordFile() {
        File workspace = new File( DeegreeWorkspace.getWorkspaceRoot() );
        return new File( workspace, PASSWORD_FILE );
    }

    public boolean isLoggedIn() {
        return !passwordFile.exists() || loggedIn;
    }

    public boolean isPasswordSet() {
        return passwordFile.exists();
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

    public String logIn()
                            throws NoSuchAlgorithmException, IOException {

        SaltedPassword storedPassword = passwordFile.getCurrentContent();
        if ( storedPassword == null ) {
            loggedIn = true;
            FacesMessage fm = new FacesMessage( SEVERITY_WARN, "Please set a password.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return CONSOLE;
        }

        SaltedPassword givenPassword = new SaltedPassword( currentPassword, storedPassword.getSalt() );
        loggedIn = storedPassword.equals( givenPassword );
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    public String logOut() {
        loggedIn = false;
        return CONSOLE;
    }

    public String changePassword()
                            throws NoSuchAlgorithmException, IOException {

        try {
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

            SaltedPassword newSaltedPassword = new SaltedPassword( newPassword );
            passwordFile.update( newSaltedPassword );
        } catch ( Throwable e ) {
            e.printStackTrace();
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Error updating password: " + e.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return CHANGE_PASSWORD;
        }

        FacesMessage fm = new FacesMessage( FacesMessage.SEVERITY_INFO, "Password changed successfully.", null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
        loggedIn = true;
        return CHANGE_PASSWORD;
    }

    private boolean checkCurrentPassword()
                            throws IOException, NoSuchAlgorithmException {

        SaltedPassword storedPassword = passwordFile.getCurrentContent();
        if ( storedPassword == null ) {
            return true;
        }
        SaltedPassword specifiedPassword = new SaltedPassword( currentPassword, storedPassword.getSalt() );
        return storedPassword.equals( specifiedPassword );
    }

    private boolean newPasswordsMatch() {
        if ( newPassword == null ) {
            return newPassword2 == null;
        }
        return newPassword.equals( newPassword2 );
    }
}
