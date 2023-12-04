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
package org.deegree.console.generic;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.console.workspace.WorkspaceBean;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.workspace.Workspace;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
@ManagedBean
@RequestScoped
public class Connection implements Serializable {

	private static final long serialVersionUID = 6495856816506360039L;

	private String id;

	private String status = "OK";

	private Workspace getWorkspace() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		return ((WorkspaceBean) ctx.getApplicationMap().get("workspace")).getActiveWorkspace().getNewWorkspace();
	}

	public String getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	Connection(String id) {
		try {
			this.id = id;
			ConnectionProvider prov = getWorkspace().getResource(ConnectionProviderProvider.class, id);
			prov.getConnection().close();
		}
		catch (Exception e) {
			status = "ERROR: " + e.getMessage();
		}
	}

}
