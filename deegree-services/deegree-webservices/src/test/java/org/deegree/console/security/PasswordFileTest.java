package org.deegree.console.security;

import static org.deegree.console.security.LogBean.PASSWORD_FILE;
import static org.deegree.console.security.SaltedPassword.SHA256_PREFIX;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mailto:friebe@lat-lon.de">Torsten Friebe</a>
 * @since 3.6
 */
public class PasswordFileTest {

	/**
	 * This unit tests can validate if the existing console.pw file contains a password in
	 * the new extended password format introduced with deegree version 3.6.
	 * @throws IOException in case of errors
	 */
	@Test
	@Ignore
	public void getCurrentContentFromWorkspaceRoot() throws IOException {
		File workspaceDir = new File(DeegreeWorkspace.getWorkspaceRoot());
		File passwordFileFQN = new File(workspaceDir, PASSWORD_FILE);
		assertTrue(passwordFileFQN.exists());
		assertTrue(passwordFileFQN.isFile());
		PasswordFile passwordFile = new PasswordFile(passwordFileFQN);
		assertTrue(passwordFile.exists());
		SaltedPassword saltedPassword = passwordFile.getCurrentContent();
		assertThat(saltedPassword.toString(), not(isEmptyOrNullString()));
		assertThat(saltedPassword.getSalt(), startsWith(SHA256_PREFIX));
	}

	/**
	 * Attention: Enabling this unit test will overwrite the content of the console.pw
	 * file! This test is intended as an example how to create a valid console.pw file
	 * with the new extended passwort format introduced with deegree version 3.6.
	 * @throws IOException in case of errors
	 * @since 3.6
	 * @see SaltedPassword
	 */
	@Test
	@Ignore
	public void update() throws IOException {
		File workspaceDir = new File(DeegreeWorkspace.getWorkspaceRoot());
		File passwordFileFQN = new File(workspaceDir, PASSWORD_FILE);
		PasswordFile passwordFile = new PasswordFile(passwordFileFQN);
		passwordFile.update(new SaltedPassword("deegree3"));
	}

	@Test
	public void writePasswordToWriter() throws IOException, NoSuchAlgorithmException {
		SaltedPassword mypassword = new SaltedPassword("deegree3");
		PasswordFile passwordFile = new PasswordFile(File.createTempFile("pwd", ".tmp"));
		StringWriter writer = new StringWriter();
		passwordFile.writePasswordToWriter(new PrintWriter(writer), mypassword);
		assertThat(writer.toString(), not(isEmptyOrNullString()));
		assertThat(writer.toString(), is(equalTo(mypassword.toString())));
	}

	@Test
	public void exists() throws IOException {
		PasswordFile passwordFile = new PasswordFile(File.createTempFile("pwd", ".tmp"));
		assertTrue(passwordFile.exists());
	}

}