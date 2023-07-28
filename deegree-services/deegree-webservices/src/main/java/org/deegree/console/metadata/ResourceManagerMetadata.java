/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.console.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class ResourceManagerMetadata implements Comparable<ResourceManagerMetadata> {

	private static Logger LOG = LoggerFactory.getLogger(ResourceManagerMetadata.class);

	private String name, category;

	private String startView = "/console/jsf/resources";

	private final ResourceManager<?> mgr;

	private final Map<String, ResourceProvider<?>> nameToProvider = new HashMap<String, ResourceProvider<?>>();

	private List<ResourceProvider<?>> providers = new ArrayList<ResourceProvider<?>>();

	private final List<String> providerNames = new ArrayList<String>();

	private final Workspace workspace;

	private ResourceManagerMetadata(ResourceManager<?> mgr, Workspace workspace) {
		this.workspace = workspace;
		if (mgr.getMetadata() != null) {
			for (ResourceProvider<?> provider : mgr.getProviders()) {
				ResourceProviderMetadata providerMd = ResourceProviderMetadata.getMetadata(provider);
				if ("LockDbProviderProvider".equals(providerMd.getName())) {
					continue;
				}
				providers.add(provider);
				providerNames.add(providerMd.getName());
				nameToProvider.put(providerMd.getName(), provider);
			}
		}
		else {
			providers = Collections.emptyList();
		}

		String className = mgr.getClass().getName();
		String metadataUrl = "/META-INF/console/resourcemanager/" + className;
		URL url = ResourceManagerMetadata.class.getResource(metadataUrl);
		if (url != null) {
			LOG.debug("Loading resource manager metadata from '" + url + "'");
			Properties props = new Properties();
			InputStream is = null;
			try {
				is = url.openStream();
				props.load(is);
				name = props.getProperty("name");
				if (name != null) {
					name = name.trim();
				}
				category = props.getProperty("category");
				if (props.containsKey("start_view")) {
					startView = props.getProperty("start_view").trim();
				}
			}
			catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
			finally {
				IOUtils.closeQuietly(is);
			}
		}
		else {
			throw new RuntimeException("Internal error: File '" + metadataUrl + "' missing on classpath.");
		}
		this.mgr = mgr;
	}

	public static synchronized ResourceManagerMetadata getMetadata(ResourceManager<?> rm, Workspace workspace) {
		ResourceManagerMetadata md = new ResourceManagerMetadata(rm, workspace);
		if (md.name == null) {
			return null;
		}
		return md;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getStartView() {
		return startView;
	}

	public ResourceManager<?> getManager() {
		return mgr;
	}

	public String getManagerClass() {
		return mgr.getClass().getName();
	}

	public ResourceProvider<?> getProvider(String name) {
		return nameToProvider.get(name);
	}

	public List<ResourceProvider<?>> getProviders() {
		return providers;
	}

	public List<String> getProviderNames() {
		return providerNames;
	}

	public boolean getMultipleProviders() {
		return providers.size() > 1;
	}

	public boolean getHasErrors() {
		return workspace.getErrorHandler().hasErrors();
	}

	@Override
	public int compareTo(ResourceManagerMetadata o) {
		return this.name.compareTo(o.name);
	}

}
