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
package org.deegree.console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.deegree.console.datastore.feature.FeatureStoreConfig;
import org.deegree.console.metadata.ResourceManagerMetadata;
import org.deegree.console.metadata.ResourceProviderMetadata;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:name@company.com">Your Name</a>
 */
public abstract class AbstractResourceManagerBean<T extends ResourceManager<?>> implements Serializable {

	private static final long serialVersionUID = -7795125766411006135L;

	private String newConfigType;

	private List<String> newConfigTypeTemplates;

	private String newConfigTypeTemplate;

	private String newConfigId;

	private transient ResourceManagerMetadata metadata;

	protected transient ResourceManager<?> resourceManager;

	private transient Workspace workspace;

	protected AbstractResourceManagerBean() {
		// default constructor required
	}

	protected AbstractResourceManagerBean(Class<T> mgrClass) {
		workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
		resourceManager = workspace.getResourceManager(mgrClass);
		metadata = ResourceManagerMetadata.getMetadata(resourceManager, workspace);
	}

	public ResourceManagerMetadata getMetadata() {
		return metadata;
	}

	public List<Config> getConfigs() {
		List<Config> configs = new ArrayList<Config>();
		for (ResourceMetadata<?> md : resourceManager.getResourceMetadata()) {
			configs.add(new Config(md, resourceManager, getStartView(), true));
		}
		Collections.sort(configs);
		return configs;
	}

	public List<String> getNewConfigTypeTemplates() {
		return newConfigTypeTemplates;
	}

	public void setNewConfigTypeTemplates(List<String> newConfigTypeTemplates) {
		this.newConfigTypeTemplates = newConfigTypeTemplates;
	}

	public String getNewConfigTypeTemplate() {
		return newConfigTypeTemplate;
	}

	public void setNewConfigTypeTemplate(String newConfigTypeTemplate) {
		this.newConfigTypeTemplate = newConfigTypeTemplate;
	}

	public String getNewConfigId() {
		return newConfigId;
	}

	public void setNewConfigId(String newConfigId) {
		this.newConfigId = newConfigId;
	}

	public String getNewConfigType() {
		return newConfigType;
	}

	public void setNewConfigType(String newConfigType) {
		this.newConfigType = newConfigType;
		ResourceProvider<?> provider = metadata.getProvider(newConfigType);
		if (provider == null) {
			provider = metadata.getProviders().get(0);
		}
		ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata(provider);
		newConfigTypeTemplates = new LinkedList<String>(md.getExamples().keySet());
	}

	public String getStartView() {
		return null;
	}

	public boolean getHasErrors() {
		for (ResourceMetadata<?> md : resourceManager.getResourceMetadata()) {
			FeatureStoreConfig config = new FeatureStoreConfig(md, resourceManager);
			String state = config.getState();
			if ("Error".equals(state)) {
				return true;
			}
		}
		return false;
	}

}
