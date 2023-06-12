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

import static java.lang.Character.digit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class PasswordFile implements Serializable {

	private static final long serialVersionUID = -8331316987059763053L;

	private final File file;

	public PasswordFile(File file) {
		this.file = file;
	}

	/**
	 * Returns the {@link SaltedPassword} currently stored in the file.
	 * @return salted password, never <code>null</code>
	 */
	public SaltedPassword getCurrentContent() throws IOException {

		SaltedPassword saltedPw = null;
		if (file.exists()) {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			try {
				List<String> lines = IOUtils.readLines(in);
				if (lines.size() != 1) {
					throw new IOException("Password file has incorrect format.");
				}
				saltedPw = decodeHexEncodedSaltAndPassword(lines.get(0));
			}
			finally {
				in.close();
			}
		}
		return saltedPw;
	}

	private SaltedPassword decodeHexEncodedSaltAndPassword(String encoded) throws IOException {

		int offset = encoded.indexOf(':');
		if (offset == -1) {
			throw new IOException("Password file has incorrect format.");
		}
		String hexEncodedSalt = encoded.substring(0, offset);
		String hexEncodedSaltedAndHashedPassword = encoded.substring(offset + 1, encoded.length());
		byte[] salt = decodeHexString(hexEncodedSalt);
		byte[] saltedAndHashedPassword = decodeHexString(hexEncodedSaltedAndHashedPassword);
		return new SaltedPassword(saltedAndHashedPassword, salt);
	}

	private byte[] decodeHexString(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((digit(s.charAt(i), 16) << 4) + digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Updates the file with the given {@link SaltedPassword}.
	 * @param pw salted password, must not be <code>null</code>
	 * @throws IOException if the password could not be stored
	 */
	public void update(SaltedPassword pw) throws IOException {

		if (file.exists()) {
			if (!file.delete()) {
				throw new IOException("Could not delete existing password file '" + file + "'.");
			}
		}

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		PrintWriter writer = new PrintWriter(file, "UTF-8");
		writer.print(encodeHexString(pw.getSalt()));
		writer.print(':');
		writer.println(encodeHexString(pw.getSaltedAndHashedPassword()));
		writer.close();
	}

	private String encodeHexString(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * bytes.length);
		for (final byte b : bytes) {
			final int hiVal = (b & 0xF0) >> 4;
			final int loVal = b & 0x0F;
			hex.append((char) ('0' + (hiVal + (hiVal / 10 * 7))));
			hex.append((char) ('0' + (loVal + (loVal / 10 * 7))));
		}
		return hex.toString();
	}

	public boolean exists() {
		return file.exists();
	}

}
