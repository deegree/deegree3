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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Encapsulates a salt value and the hash of a password that has been salted with the same
 * value.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class SaltedPassword {

	private static final String HASH_ALGORITHM_ID = "SHA-256";

	private static final String CHARSET = "UTF-8";

	private final byte[] saltedAndHashedPassword;

	private final byte[] salt;

	public SaltedPassword(byte[] saltedAndHashedPassword, byte[] salt) {
		this.saltedAndHashedPassword = saltedAndHashedPassword;
		this.salt = salt;
	}

	public SaltedPassword(String plainPassword, byte[] salt)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] plainPasswordBinary = plainPassword.getBytes(CHARSET);
		byte[] saltedAndHashedPassword = getHashedAndSaltedPassword(plainPasswordBinary, salt);
		this.saltedAndHashedPassword = saltedAndHashedPassword;
		this.salt = salt;
	}

	public SaltedPassword(String plainPassword) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		this(plainPassword, getNewSalt());
	}

	private byte[] getHashedAndSaltedPassword(byte[] plainPassword, byte[] salt)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM_ID);
		md.update(plainPassword);
		md.update(salt);
		return md.digest();
	}

	public byte[] getSaltedAndHashedPassword() {
		return saltedAndHashedPassword;
	}

	public byte[] getSalt() {
		return salt;
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

	private static byte[] getNewSalt() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(Long.SIZE / 8);
		byteBuffer.putLong(new Date().getTime());
		return byteBuffer.array();
	}

}
