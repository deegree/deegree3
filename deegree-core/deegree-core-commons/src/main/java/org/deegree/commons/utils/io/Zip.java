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
package org.deegree.commons.utils.io;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Zip {

	private static final Logger LOG = getLogger(Zip.class);

	public static void unzip(final InputStream in, File dir, boolean overwrite)
			throws FileNotFoundException, IOException {
		ZipInputStream zin = new ZipInputStream(in);
		ZipEntry entry;

		if (!dir.exists()) {
			dir.mkdir();
		}

		boolean rootRead = false;

		while ((entry = zin.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				File f = new File(dir, entry.getName());
				// avoid directory-in-directory
				if (!rootRead) {
					if (f.getName().equals(dir.getName())) {
						dir = dir.getParentFile();
					}
				}
				rootRead = true;
				f.mkdir();
				continue;
			}

			File f = new File(dir, entry.getName());

			if (f.exists() && !overwrite) {
				LOG.debug("Not overwriting {}.", f);
				continue;
			}

			byte[] bs = new byte[16384];
			File parent = f.getAbsoluteFile().getParentFile();
			parent.mkdirs();
			FileOutputStream out = new FileOutputStream(f);
			int read;
			while ((read = zin.read(bs)) != -1) {
				out.write(bs, 0, read);
			}
			out.close();
		}

		in.close();
	}

	/**
	 * @param in
	 * @param dir
	 * @throws IOException
	 */
	public static void unzip(final InputStream in, File dir) throws IOException {
		unzip(in, dir, true);
	}

	/**
	 * .svn files/directories will be ignored.
	 * @param f
	 * @param out
	 * @param parent may be null, all written paths in the zip will be relative to this
	 * one (default is f.toURI)
	 * @throws IOException
	 */
	public static void zip(File f, ZipOutputStream out, URI parent) throws IOException {
		if (f.getName().equalsIgnoreCase(".svn")) {
			return;
		}

		if (parent == null) {
			parent = f.toURI();
		}

		String name = parent.relativize(f.getAbsoluteFile().toURI()).toString();
		if (f.isDirectory()) {
			if (!name.isEmpty()) {
				ZipEntry e = new ZipEntry(name);
				out.putNextEntry(e);
			}
			File[] fs = f.listFiles();
			if (fs != null) {
				for (File f2 : fs) {
					zip(f2, out, parent);
				}
			}
		}
		else {
			ZipEntry e = new ZipEntry(name);
			out.putNextEntry(e);
			InputStream is = null;
			try {
				is = new FileInputStream(f);
				copy(is, out);
			}
			finally {
				closeQuietly(is);
			}
		}
	}

}
