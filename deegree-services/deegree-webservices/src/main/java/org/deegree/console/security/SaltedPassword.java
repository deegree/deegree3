/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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

import org.apache.commons.codec.digest.Sha2Crypt;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Encapsulates a salt value and the hash of a password that has been salted with the same
 * value using Apache Commons Codec SHA-256 implementation.
 *
 * As of deegree version 3.6 the encoding format has been changed to the format of the
 * extended password format as used in UNIX crypt:
 *
 * <pre>
 *    $ID$SALT$PWD
 * </pre>
 *
 * The ID for the SHA-256 and SHA-512 methods are as follows:
 *
 * <pre>
 *      ID       |    Method
 *   -------------------------------
 *      5        |  SHA-256
 *      6        |  SHA-512
 * </pre>
 *
 * Currently only the SHA-256 method is used.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author <a href="mailto:friebe@lat-lon.de">Torsten Friebe</a>
 * @version 3.6
 * @since 3.3
 * @see <a href=
 * "https://commons.apache.org/proper/commons-codec/apidocs/org/apache/commons/codec/digest/Sha2Crypt.html">Apache
 * Commons Codec Sha2Crypt</a>
 * @see <a href="https://www.akkadia.org/drepper/SHA-crypt.txt">Unix crypt using SHA-256
 * and SHA-512</a>
 */
public final class SaltedPassword {

	private static final String CHARSET = StandardCharsets.UTF_8.toString();

	static final String SHA256_PREFIX = "$5$";

	private final byte[] saltedAndHashedPassword;

	private final String salt;

	public SaltedPassword(byte[] saltedAndHashedPassword, String salt) {
		this.saltedAndHashedPassword = saltedAndHashedPassword;
		this.salt = salt;
	}

	public SaltedPassword(String plainPassword, String salt) throws UnsupportedEncodingException {
		byte[] plainPasswordBinary = plainPassword.getBytes(CHARSET);
		String saltedPassword = Sha2Crypt.sha256Crypt(plainPasswordBinary, salt);
		int delimiterPos = nthIndexOf(saltedPassword, "$", 3);
		this.saltedAndHashedPassword = saltedPassword.substring(delimiterPos + 1, saltedPassword.length())
			.getBytes(StandardCharsets.UTF_8);
		this.salt = salt;
	}

	public SaltedPassword(String plainPassword) throws UnsupportedEncodingException {
		byte[] plainPasswordBinary = plainPassword.getBytes(CHARSET);
		String saltedPassword = Sha2Crypt.sha256Crypt(plainPasswordBinary);
		int delimiterPos = nthIndexOf(saltedPassword, "$", 3);
		this.salt = saltedPassword.substring(0, delimiterPos);
		this.saltedAndHashedPassword = saltedPassword.substring(delimiterPos + 1, saltedPassword.length())
			.getBytes(StandardCharsets.UTF_8);
	}

	public byte[] getSaltedAndHashedPassword() {
		return saltedAndHashedPassword;
	}

	public String getSalt() {
		return salt;
	}

	@Override
	public String toString() {
		return this.getSalt() + "$" + new String(this.getSaltedAndHashedPassword(), StandardCharsets.UTF_8);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof SaltedPassword)) {
			return false;
		}
		SaltedPassword that = (SaltedPassword) o;
		return equalsBytewise(this.saltedAndHashedPassword, that.saltedAndHashedPassword);
	}

	private boolean equalsBytewise(byte[] bytes1, byte[] bytes2) {
		if (bytes1.length != bytes2.length) {
			return false;
		}
		for (int i = 0; i < bytes1.length; i++) {
			if (bytes1[i] != bytes2[i]) {
				return false;
			}
		}
		return true;
	}

	private int nthIndexOf(String input, String substring, int nth) {
		if (nth == 1) {
			return input.indexOf(substring);
		}
		else {
			return input.indexOf(substring, nthIndexOf(input, substring, nth - 1) + substring.length());
		}
	}

}
