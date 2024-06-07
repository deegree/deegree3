package org.deegree.console.security;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.deegree.console.security.SaltedPassword.SHA256_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:friebe@lat-lon.de">Torsten Friebe</a>
 * @since 3.6
 */
public class SaltedPasswordTest {

	@Test
	public void testCreatingFromPlainPassword() throws UnsupportedEncodingException {
		SaltedPassword plainpassword = new SaltedPassword("foo");
		String saltedPassword = new String(plainpassword.getSaltedAndHashedPassword(), StandardCharsets.UTF_8);
		assertThat(plainpassword.getSalt(), startsWith(SHA256_PREFIX));
		assertThat(saltedPassword, is(notNullValue()));
	}

	@Test
	public void testCreatingFromSaltedPassword() {
		String password = "nxIKX54gpaik7RiymYmMEhDou8.9DjFTzFkJxHKQ3D/";
		String salt = "$5$12345678";
		SaltedPassword saltedpassword = new SaltedPassword(password.getBytes(StandardCharsets.UTF_8), salt);
		assertThat(new String(saltedpassword.getSaltedAndHashedPassword(), StandardCharsets.UTF_8), is(password));
		assertThat(saltedpassword.getSalt(), is(salt));
	}

	@Test
	public void testCreateNewPasswordWithSaltFromOtherPassword() throws UnsupportedEncodingException {
		String plainpassword = "foo";
		SaltedPassword storedPassword = new SaltedPassword(plainpassword);
		String saltFromStoredPassword = storedPassword.getSalt();
		SaltedPassword givenPassword = new SaltedPassword(plainpassword, saltFromStoredPassword);
		assertTrue(storedPassword.equals(givenPassword));
	}

	@Test
	public void testSaltedPasswordAsParts() throws UnsupportedEncodingException {
		SaltedPassword password = new SaltedPassword("foo");
		assertThat(password.toString(), startsWith("$"));
		String[] parts = password.toString().split("\\$");
		// first is empty as the string starts with $
		assertThat(parts[0], is(""));
		// the ID for SHA-256
		assertThat(parts[1], is("5"));
		// the salt
		assertThat(parts[2], hasLength(8));
		// the hashed password
		assertThat(parts[3], hasLength(43));
	}

}