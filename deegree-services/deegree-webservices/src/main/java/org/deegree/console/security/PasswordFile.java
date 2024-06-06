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
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * An instance of the <code>PasswordFile</code> class encapsulates a text file storing a
 * <code>SaltedPassword</code>.
 *
 * As of deegree version 3.6 the encoding format of the salted password has been changed
 * to <code>$ID$SALT$PWD</code>. In previous versions of deegree the format has been
 * <code>SALT:PWD</code>.
 *
 * <b>Attention:</b> There is no automatic password migration available. Files created
 * with older versions of deegree need to be recreated with deegree 3.6.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author <a href="mailto:friebe@lat-lon.de">Torsten Friebe</a>
 * @since 3.0
 * @see SaltedPassword
 */
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
				saltedPw = parseSaltedPassword(lines.get(0));
			}
			finally {
				in.close();
			}
		}
		return saltedPw;
	}

	private SaltedPassword parseSaltedPassword(String encoded) throws IOException {
		int offset = encoded.indexOf('$');
		if (offset == -1) {
			throw new IOException("Password file has incorrect format.");
		}
		String[] parts = encoded.split("\\$");

		return new SaltedPassword(parts[3].getBytes(StandardCharsets.UTF_8), parts[2]);
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

		writePasswordToWriter(new PrintWriter(file, StandardCharsets.UTF_8), pw);
	}

	protected void writePasswordToWriter(PrintWriter writer, SaltedPassword pw) {
		writer.print(pw.toString());
		writer.flush();
		writer.close();
	}

	public boolean exists() {
		return file.exists();
	}

}
