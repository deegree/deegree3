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

import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Utils {

	/**
	 * @param path
	 * @return never a null workspace, maybe a null path if none was specified
	 */
	public static Pair<DeegreeWorkspace, String> getWorkspaceAndPath(String path) {
		if (path == null || path.isEmpty() || path.equals("/")) {
			return new Pair<DeegreeWorkspace, String>(getServiceWorkspace(), null);
		}

		if (path.indexOf("..") != -1) {
			throw new SecurityException("Do not use .. in paths.");
		}

		path = path.substring(1);
		if (path.indexOf("/") != -1) {
			String wsName = path.substring(0, path.indexOf("/"));
			if (DeegreeWorkspace.isWorkspace(wsName)) {
				DeegreeWorkspace ws = DeegreeWorkspace.getInstance(wsName);
				return new Pair<DeegreeWorkspace, String>(ws, path.substring(path.indexOf("/") + 1));
			}
			return new Pair<DeegreeWorkspace, String>(getServiceWorkspace(), path);
		}
		if (DeegreeWorkspace.isWorkspace(path)) {
			return new Pair<DeegreeWorkspace, String>(DeegreeWorkspace.getInstance(path), null);
		}

		return new Pair<DeegreeWorkspace, String>(getServiceWorkspace(), path.isEmpty() ? null : path);
	}

}
