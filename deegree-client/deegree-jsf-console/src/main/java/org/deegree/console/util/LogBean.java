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
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    // location of password file (relative to workspace)
    private static final String PASSWORD_FILE = "../console-pw.txt";

    private static final long serialVersionUID = -4865071415988778817L;

    private boolean loggedIn = false;

    private String currentPassword;

    private String newPassword;

    private String newPassword2;

    public boolean isLoggedIn() {
        return !getPasswordFile().exists() || loggedIn;
    }

    public boolean isPasswordSet() {
        return getPasswordFile().exists();
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
                            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String storedPasswordHexHash = getStoredPasswordHexHash();
        if ( storedPasswordHexHash == null ) {
            loggedIn = true;
            FacesMessage fm = new FacesMessage( SEVERITY_WARN, "Please set a password.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return CONSOLE;
        }

        String currentPasswordHexHash = getHexPasswordHexHash( currentPassword, "SALT" );

        if ( storedPasswordHexHash.equals( currentPasswordHexHash ) ) {
            loggedIn = true;
            return FacesContext.getCurrentInstance().getViewRoot().getViewId();
        }
        return LOGIN_FAILED;
    }

    public String logOut() {
        loggedIn = false;
        return CONSOLE;
    }

    public String changePassword()
                            throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
        } catch ( Throwable e ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Error updating password: " + e.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return CHANGE_PASSWORD;
        }

        FacesMessage fm = new FacesMessage( FacesMessage.SEVERITY_INFO, "Password changed successfully.", null );
        FacesContext.getCurrentInstance().addMessage( null, fm );
        loggedIn = true;
        return CHANGE_PASSWORD;
    }

    private String getStoredPasswordHexHash() {
        String storedPw = null;
        File pwFile = null;
        try {
            File workspace = OGCFrontController.getServiceWorkspace().getLocation();
            pwFile = new File( workspace, PASSWORD_FILE );

            if ( pwFile.exists() ) {
                BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( pwFile ) ) );
                try {
                    String line;
                    while ( ( line = in.readLine() ) != null ) {
                        storedPw = line.trim();
                    }
                } finally {
                    in.close();
                }
            }
        } catch ( IOException e ) {
            LOG.warn( "Error reading password from file '{}': {}", pwFile, e.getMessage() );
        }
        return storedPw;
    }

    private void updatedStoredPassword( String newPassword )
                            throws IOException, NoSuchAlgorithmException {

        File pwFile = getPasswordFile();

        if ( pwFile.exists() ) {
            if ( !pwFile.delete() ) {
                throw new IOException( "Could not delete password file '" + pwFile + "'." );
            }
        }

        if ( !pwFile.getParentFile().exists() ) {
            pwFile.getParentFile().mkdirs();
        }
        PrintWriter writer = new PrintWriter( pwFile );
        String newPasswordHexHash = getHexPasswordHexHash( newPassword, "SALT" );
        writer.print( newPasswordHexHash );
        writer.close();
    }

    private File getPasswordFile() {
        File workspace = OGCFrontController.getServiceWorkspace().getLocation();
        return new File( workspace, PASSWORD_FILE );
    }

    private boolean newPasswordsMatch() {
        if ( newPassword == null ) {
            return newPassword2 == null;
        }
        return newPassword.equals( newPassword2 );
    }

    private boolean checkCurrentPassword()
                            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String storedPassword = getStoredPasswordHexHash();
        if ( storedPassword == null ) {
            return true;
        }

        String currentPasswordHexHash = getHexPasswordHexHash( currentPassword, "SALT" );
        if ( storedPassword.equals( currentPasswordHexHash ) ) {
            return true;
        }
        return false;
    }

    private String getHexPasswordHexHash( String pw, String salt )
                            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] hash = getHash( pw, salt );
        return getHex( hash );
    }

    private String getHex( final byte[] bytes ) {
        if ( bytes == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * bytes.length );
        for ( final byte b : bytes ) {
            final int hiVal = ( b & 0xF0 ) >> 4;
            final int loVal = b & 0x0F;
            hex.append( (char) ( '0' + ( hiVal + ( hiVal / 10 * 7 ) ) ) );
            hex.append( (char) ( '0' + ( loVal + ( loVal / 10 * 7 ) ) ) );
        }
        return hex.toString();
    }

    private byte[] getHash( String pw, String salt )
                            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String saltedPw = pw + salt;
        MessageDigest md = MessageDigest.getInstance( "SHA-256" );
        md.update( saltedPw.getBytes( "UTF-8" ) );
        byte[] mdbytes = md.digest();
        return mdbytes;
    }
}
