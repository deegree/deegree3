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
package org.deegree.console.workspace;

import java.io.File;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.console.JsfUtils;

/**
 * JSF backing bean for creating a new workspace folder.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 * @since 3.4
 */
@ManagedBean
@RequestScoped
public class CreateWorkspaceBean {

	private String workspaceName;

	public void createWorkspaceFolder() {
		String workspaceRoot = DeegreeWorkspace.getWorkspaceRoot();
		File targetWorkspace = new File(workspaceRoot, workspaceName);
		boolean success = targetWorkspace.mkdir();
		if (!success) {
			JsfUtils.indicateException("Creation of workspace", "Workspace identifier already exists.");
		}
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

}
