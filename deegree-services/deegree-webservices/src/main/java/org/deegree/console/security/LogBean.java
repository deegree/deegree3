/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
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

/**
 * JSF backing bean for logging in, logging out, checking login status and password
 * change.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.3
 */
@ManagedBean
@SessionScoped
public class LogBean implements Serializable {

	private static final long serialVersionUID = -4865071415988778817L;

	private static final String PASSWORD_FILE = "console.pw";

	public static final String CONSOLE = "/index";

	public static final String CHANGE_PASSWORD = "/console/security/password";

	private boolean loggedIn = false;

	private String currentPassword;

	private String newPassword;

	private String newPassword2;

	private final PasswordFile passwordFile = new PasswordFile(getPasswordFile());

	private File getPasswordFile() {
		File workspace = new File(DeegreeWorkspace.getWorkspaceRoot());
		return new File(workspace, PASSWORD_FILE);
	}

	public boolean isLoggedIn() {
		return !passwordFile.exists() || loggedIn;
	}

	public boolean isPasswordSet() {
		return passwordFile.exists();
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword2(String newPassword2) {
		this.newPassword2 = newPassword2;
	}

	public String getNewPassword2() {
		return newPassword2;
	}

	public String logIn() throws NoSuchAlgorithmException, IOException {

		SaltedPassword storedPassword = passwordFile.getCurrentContent();
		if (storedPassword == null) {
			loggedIn = true;
			FacesMessage fm = new FacesMessage(SEVERITY_WARN, "Please set a password.", null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
			return CONSOLE;
		}

		SaltedPassword givenPassword = new SaltedPassword(currentPassword, storedPassword.getSalt());
		loggedIn = storedPassword.equals(givenPassword);
		return FacesContext.getCurrentInstance().getViewRoot().getViewId();
	}

	public String logOut() {
		loggedIn = false;
		return CONSOLE;
	}

	public String changePassword() throws NoSuchAlgorithmException, IOException {

		try {
			if (!checkCurrentPassword()) {
				FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "Current password is incorrect.", null);
				FacesContext.getCurrentInstance().addMessage(null, fm);
				return CHANGE_PASSWORD;
			}
			if (!newPasswordsMatch()) {
				FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "New passwords don't match.", null);
				FacesContext.getCurrentInstance().addMessage(null, fm);
				return CHANGE_PASSWORD;
			}

			SaltedPassword newSaltedPassword = new SaltedPassword(newPassword);
			passwordFile.update(newSaltedPassword);
		}
		catch (Throwable e) {
			e.printStackTrace();
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "Error updating password: " + e.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
			return CHANGE_PASSWORD;
		}

		FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "Password changed successfully.", null);
		FacesContext.getCurrentInstance().addMessage(null, fm);
		loggedIn = true;
		return CHANGE_PASSWORD;
	}

	private boolean checkCurrentPassword() throws IOException, NoSuchAlgorithmException {

		SaltedPassword storedPassword = passwordFile.getCurrentContent();
		if (storedPassword == null) {
			return true;
		}
		SaltedPassword specifiedPassword = new SaltedPassword(currentPassword, storedPassword.getSalt());
		return storedPassword.equals(specifiedPassword);
	}

	private boolean newPasswordsMatch() {
		if (newPassword == null) {
			return newPassword2 == null;
		}
		return newPassword.equals(newPassword2);
	}

}
