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
package org.deegree.console;

import static org.deegree.console.JsfUtils.getWorkspace;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.deegree.console.metadata.ConfigExample;
import org.deegree.console.metadata.ResourceManagerMetadata;
import org.deegree.console.metadata.ResourceProviderMetadata;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * JSF backing bean for views of type "Create new XYZ resource".
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
@ManagedBean
@ViewScoped
public abstract class AbstractCreateResourceBean {

	private String id;

	private String type;

	private String configTemplate;

	private List<String> configTemplates;

	private transient ResourceManagerMetadata metadata;

	private File resourceDir;

	protected AbstractCreateResourceBean() {
		// required by JSF
	}

	protected AbstractCreateResourceBean(Class<? extends ResourceManager<?>> resourceMgrClass) {
		ResourceManager<?> mgr = getWorkspace().getResourceManager(resourceMgrClass);
		metadata = ResourceManagerMetadata.getMetadata(mgr, getWorkspace());
		File wsDir = ((DefaultWorkspace) getWorkspace()).getLocation();
		resourceDir = new File(wsDir, mgr.getMetadata().getWorkspacePath());
		if (!getTypes().isEmpty()) {
			type = getTypes().get(0);
			changeType(null);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void changeType(AjaxBehaviorEvent event) {
		ResourceProvider<?> provider = metadata.getProvider(type);
		ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata(provider);
		configTemplates = new ArrayList<String>(md.getExamples().keySet());
		if (configTemplates.isEmpty()) {
			configTemplate = null;
		}
		else {
			configTemplate = configTemplates.get(0);
		}
	}

	public List<String> getTypes() {
		return metadata.getProviderNames();
	}

	public String getConfigTemplate() {
		return configTemplate;
	}

	public void setConfigTemplate(String configTemplate) {
		this.configTemplate = configTemplate;
	}

	public List<String> getConfigTemplates() {
		return configTemplates;
	}

	public String create() {
		ResourceProvider<?> provider = metadata.getProvider(type);
		if (provider == null) {
			provider = metadata.getProviders().get(0);
		}
		ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata(provider);
		final Map<String, ConfigExample> examples = md.getExamples();
		if (examples == null || !examples.containsKey(configTemplate)) {
			JsfUtils.indicateException("Creating resource", "Internal error: Example template missing!?");
			return getOutcome();
		}
		URL templateURL = md.getExamples().get(configTemplate).getContentLocation();
		try {
			URL schemaURL = null;
			if (provider != null && provider instanceof AbstractResourceProvider<?>) {
				schemaURL = ((AbstractResourceProvider<?>) provider).getSchema();
			}
			File resourceFile = new File(resourceDir, id + ".xml");
			if (resourceFile.exists()) {
				JsfUtils.indicateException("Creating resource", "Resource with this identifier already exists!");
				return getOutcome();
			}
			StringBuilder sb = new StringBuilder("/console/generic/xmleditor?faces-redirect=true");
			sb.append("&id=").append(id);
			sb.append("&schemaUrl=");
			if (schemaURL != null) {
				sb.append(schemaURL.toString());
			}
			sb.append("&resourceProviderClass=")
				.append(metadata.getManager().getMetadata().getProviderClass().getCanonicalName());
			sb.append("&nextView=").append(getOutcome());
			sb.append("&emptyTemplate=").append(templateURL);

			return sb.toString();
		}
		catch (Exception t) {
			JsfUtils.indicateException("Creating resource", t);
		}
		return getOutcome();
	}

	protected abstract String getOutcome();

}
