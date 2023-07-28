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
package org.deegree.services.config.actions;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.deegree.commons.utils.io.Zip.zip;
import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Download {

	/**
	 * @param path
	 * @param resp
	 * @throws IOException
	 */
	public static void download(String path, HttpServletResponse resp) throws IOException {
		Pair<DeegreeWorkspace, String> p = getWorkspaceAndPath(path);

		if (p.second == null) {
			try {
				download(p.first, resp);
			}
			catch (IOException e) {
				resp.setStatus(500);
				resp.setContentType("text/plain");
				IOUtils.write("Error while downloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream());
			}
			return;
		}

		try {
			download(p.first, p.second, resp);
		}
		catch (IOException e) {
			resp.setStatus(500);
			resp.setContentType("text/plain");
			IOUtils.write("Error while downloading: " + e.getLocalizedMessage() + "\n", resp.getOutputStream());
		}

	}

	private static void download(DeegreeWorkspace ws, String file, HttpServletResponse resp) throws IOException {
		File f = new File(ws.getLocation(), file);
		if (!f.exists()) {
			resp.setStatus(404);
			resp.setContentType("text/plain");
			IOUtils.write("No such file in workspace: " + ws.getName() + " -> " + file + "\n", resp.getOutputStream());
			return;
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
			if (f.getName().endsWith(".xml"))
				resp.setContentType("application/xml");
			else
				resp.setContentType("application/octet-stream");
			copy(in, resp.getOutputStream());
		}
		finally {
			closeQuietly(in);
		}
	}

	/**
	 * @param ws
	 * @param resp
	 * @throws IOException
	 */
	private static void download(DeegreeWorkspace ws, HttpServletResponse resp) throws IOException {
		File dir = ws.getLocation();
		if (!dir.exists()) {
			resp.setStatus(404);
			resp.setContentType("text/plain");
			IOUtils.write("No such workspace.\n", resp.getOutputStream());
			return;
		}
		resp.setContentType("application/x-download");
		resp.setHeader("Content-Disposition", "attachment; filename=" + dir.getName() + ".zip");
		resp.setContentType("application/zip");
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(resp.getOutputStream());
			zip(dir, out, null);
		}
		finally {
			closeQuietly(out);
		}
	}

}
