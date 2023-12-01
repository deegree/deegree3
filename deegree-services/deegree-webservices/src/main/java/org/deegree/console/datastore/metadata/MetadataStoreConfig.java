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
package org.deegree.console.datastore.metadata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.deegree.client.core.utils.MessageUtils;
import org.deegree.client.core.utils.SQLExecution;
import org.deegree.console.Config;
import org.deegree.console.workspace.WorkspaceBean;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

public class MetadataStoreConfig extends Config {

	public MetadataStoreConfig(ResourceMetadata<?> state, ResourceManager<?> resourceManager) {
		super(state, resourceManager, "/console/datastore/metadata/index", true);
	}

	private Workspace getWorkspace() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		return ((WorkspaceBean) ctx.getApplicationMap().get("workspace")).getActiveWorkspace().getNewWorkspace();
	}

	public void updateId(ActionEvent evt) {
		id = ((HtmlCommandButton) evt.getComponent()).getAlt();
	}

	public String openImporter() throws Exception {
		MetadataStore<?> ms = getWorkspace().getResource(MetadataStoreProvider.class, getId());
		if (ms == null) {
			throw new Exception("No metadata store with id '" + getId() + "' known / active.");
		}
		MetadataImporter msImporter = new MetadataImporter(ms);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("msConfig", this);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("msImporter", msImporter);
		return "/console/metadatastore/importer?faces-redirect=true";
	}

	public String createTables() throws MetadataStoreException {
		MetadataStore<?> ms = getWorkspace().getResource(MetadataStoreProvider.class, getId());
		MetadataStoreProvider provider = (MetadataStoreProvider) ms.getMetadata().getProvider();
		String[] sql;
		try {
			String connId = ms.getConnId();
			Workspace ws = getWorkspace();
			ConnectionProvider prov = ws.getResource(ConnectionProviderProvider.class, connId);

			sql = provider.getCreateStatements(prov.getDialect());

			SQLExecution execution = new SQLExecution(connId, sql, "/console/datastore/metadata/index", ws);

			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("execution", execution);
		}
		catch (UnsupportedEncodingException e) {
			FacesMessage msg = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"METADATASTORE_FAILED_CREATE_SQL_STATEMENTS", getId(), e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		catch (IOException e) {
			FacesMessage msg = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"METADATASTORE_FAILED_CREATE_SQL_STATEMENTS", getId(), e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		return "/console/generic/sql.jsf?faces-redirect=true";
	}

}
