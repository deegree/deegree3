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

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.deegree.commons.config.DeegreeWorkspace.unregisterWorkspace;
import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Delete {

	public static void delete(String path, HttpServletResponse resp) throws IOException {

		Pair<DeegreeWorkspace, String> p = getWorkspaceAndPath(path);

		resp.setContentType("text/plain");

		if (p.second == null) {
			File dir = p.first.getLocation();
			if (!deleteQuietly(dir)) {
				IOUtils.write("Workspace deletion unsuccessful.\n", resp.getOutputStream());
			}
			else {
				IOUtils.write("Workspace deleted.\n", resp.getOutputStream());
			}
			unregisterWorkspace(p.first.getName());
			return;
		}
		File fileOrDir = new File(p.first.getLocation(), p.second);
		if (!fileOrDir.exists()) {
			resp.setStatus(404);
		}
		if (!deleteQuietly(fileOrDir)) {
			IOUtils.write("Deletion unsuccessful.\n", resp.getOutputStream());
		}
		else {
			IOUtils.write(fileOrDir.getName() + " deleted.\n", resp.getOutputStream());
		}
	}

}
