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
package org.deegree.console.datastore.feature;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.client.core.utils.SQLExecution;
import org.deegree.console.Config;
import org.deegree.console.workspace.WorkspaceBean;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

public class FeatureStoreConfig extends Config {

	public FeatureStoreConfig(ResourceMetadata metadata, ResourceManager resourceManager) {
		super(metadata, resourceManager, "/console/datastore/feature/index", true);
	}

	private Workspace getWorkspace() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		return ((WorkspaceBean) ctx.getApplicationMap().get("workspace")).getActiveWorkspace().getNewWorkspace();
	}

	public boolean getSql() {
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		return fs != null && fs instanceof SQLFeatureStore;
	}

	public String createTables() {

		if (!getSql()) {
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR,
					"Current feature store is not capable of creating tables.", null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
			return "/console/featurestore/buttons";
		}
		SQLFeatureStore fs = (SQLFeatureStore) getWorkspace().getResource(FeatureStoreProvider.class, getId());
		String connId = fs.getConnId();
		String[] sql = DDLCreator.newInstance(fs.getSchema(), fs.getDialect()).getDDL();
		SQLExecution execution = new SQLExecution(connId, sql, "/console/featurestore/buttons", getWorkspace());

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("execution", execution);
		return "/console/generic/sql.jsf?faces-redirect=true";
	}

	public String showInfo() {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("editConfig", this);
		return "/console/datastore/feature/info";
	}

	public String openLoader() throws Exception {
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		if (fs == null) {
			throw new Exception("No feature store with id '" + getId() + "' known / active.");
		}
		FeatureStoreLoader fsLoader = new FeatureStoreLoader(fs);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("fsConfig", this);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("fsLoader", fsLoader);
		return "/console/datastore/feature/loader";
	}

	public List<NamespaceBinding> getNamespaces() {
		Set<NamespaceBinding> namespaces = new TreeSet<NamespaceBinding>();
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		if (fs == null) {
			return Collections.emptyList();
		}
		AppSchema schema = fs.getSchema();
		for (FeatureType ft : schema.getFeatureTypes()) {
			String prefix = ft.getName().getPrefix();
			String ns = ft.getName().getNamespaceURI();
			namespaces.add(new NamespaceBinding(prefix, ns));
		}
		return new ArrayList<NamespaceBinding>(namespaces);
	}

	public String getNumFtsTotal() {
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		AppSchema schema = fs.getSchema();
		int numFtsTotal = schema.getFeatureTypes(null, false, true).size();
		return "" + numFtsTotal;
	}

	public String getNumFtsAbstract() {
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		AppSchema schema = fs.getSchema();
		int numFtsTotal = schema.getFeatureTypes(null, false, true).size();
		int numFtsConcrete = schema.getFeatureTypes(null, false, false).size();
		return "" + (numFtsTotal - numFtsConcrete);
	}

	public String getNumFtsConcrete() {
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		AppSchema schema = fs.getSchema();
		int numFtsConcrete = schema.getFeatureTypes(null, false, false).size();
		return "" + numFtsConcrete;
	}

	public String getFtInfo() throws IOException {
		StringBuffer sb = new StringBuffer();
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		AppSchema schema = fs.getSchema();
		FeatureType[] fts = schema.getRootFeatureTypes();

		// sort the types by name
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});

		for (FeatureType ft : fts) {
			if (!(ft instanceof FeatureCollectionType)) {
				appendFtInfo(ft, fs, sb, "");
				sb.append("<br/>");
			}
		}
		return sb.toString();
	}

	public String getFcInfo() throws IOException {
		StringBuffer sb = new StringBuffer();
		FeatureStore fs = getWorkspace().getResource(FeatureStoreProvider.class, getId());
		AppSchema schema = fs.getSchema();
		FeatureType[] fts = schema.getRootFeatureTypes();

		// sort the types by name
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});

		for (FeatureType ft : fts) {
			appendFcInfo(ft, sb, "");
			sb.append("<br/>");
		}
		return sb.toString();
	}

	private void appendFtInfo(FeatureType ft, FeatureStore store, StringBuffer sb, String indent) throws IOException {
		if (ft instanceof FeatureCollectionType) {
			return;
		}
		if (ft.isAbstract()) {
			sb.append(indent + "- <i>" + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart()
					+ " (abstract)</i><br/>");
		}
		else {
			if (store.isMapped(ft.getName())) {
				Query query = new Query(ft.getName(), null, 0, -1, -1);
				int numInstances = -1;
				try {
					numInstances = store.queryHits(query);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				sb.append(indent + "- " + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart() + " ("
						+ numInstances + " instances)<br/>");
			}
			else {
				sb.append(indent + "- " + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart()
						+ " (not mapped)<br/>");
			}
		}
		FeatureType[] fts = ft.getSchema().getDirectSubtypes(ft);
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});
		for (FeatureType childType : fts) {
			appendFtInfo(childType, store, sb, indent + "&nbsp;&nbsp;");
		}
	}

	private void appendFcInfo(FeatureType ft, StringBuffer sb, String indent) throws IOException {
		if (ft instanceof FeatureCollectionType) {
			if (ft.isAbstract()) {
				sb.append(indent + "- <i>" + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart()
						+ " (abstract)</i><br/>");
			}
			else {
				sb.append(indent + "- " + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart() + "<br/>");
			}
		}
		FeatureType[] fts = ft.getSchema().getDirectSubtypes(ft);
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});
		for (FeatureType childType : fts) {
			appendFcInfo(childType, sb, indent + "&nbsp;&nbsp;");
		}
	}

}
