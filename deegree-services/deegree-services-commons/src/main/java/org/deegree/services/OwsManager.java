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
package org.deegree.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceManager;
import org.deegree.workspace.standard.DefaultResourceManagerMetadata;

/**
 * {@link ResourceManager} for {@link OWS} (and web service configuration).
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class OwsManager extends DefaultResourceManager<OWS> {

	private Workspace workspace;

	public OwsManager() {
		super(new DefaultResourceManagerMetadata<OWS>(OWSProvider.class, "web services", "services"));
	}

	@Override
	public void startup(Workspace workspace) {
		this.workspace = workspace;
		super.startup(workspace);
	}

	@Override
	protected void read(List<ResourceLocation<OWS>> list) {
		ListIterator<ResourceLocation<OWS>> iter = list.listIterator();
		while (iter.hasNext()) {
			ResourceLocation<OWS> loc = iter.next();
			if (loc.getIdentifier().getId().endsWith("_metadata")) {
				iter.remove();
			}
			if (loc.getIdentifier().getId().equals("metadata") | loc.getIdentifier().getId().equals("main")) {
				iter.remove();
			}
		}
		super.read(list);
	}

	/**
	 * Returns the {@link OWS} instance that is responsible for handling requests to a
	 * certain service type, e.g. WMS, WFS.
	 * @param serviceType service type code, e.g. "WMS" or "WFS"
	 * @return responsible <code>OWS</code> or null, if no responsible service was found
	 */
	public List<OWS> getByServiceType(String serviceType) {
		List<ResourceIdentifier<OWS>> list = workspace.getResourcesOfType(OWSProvider.class);
		List<OWS> services = new ArrayList<OWS>();
		for (ResourceIdentifier<OWS> id : list) {
			OWS ows = workspace.getResource(OWSProvider.class, id.getId());
			if (ows == null) {
				continue;
			}
			for (String name : ((OWSProvider) ows.getMetadata().getProvider()).getImplementationMetadata()
				.getImplementedServiceName()) {
				if (name.equalsIgnoreCase(serviceType)) {
					services.add(ows);
				}
			}
		}
		return services;
	}

	/**
	 * Returns the {@link OWS} instance that is responsible for handling requests with a
	 * certain name, e.g. GetMap, GetFeature.
	 * @param requestName request name, e.g. "GetMap" or "GetFeature"
	 * @return responsible <code>OWS</code> or null, if no responsible service was found
	 */
	public List<OWS> getByRequestName(String requestName) {
		List<ResourceIdentifier<OWS>> list = workspace.getResourcesOfType(OWSProvider.class);
		List<OWS> services = new ArrayList<OWS>();
		for (ResourceIdentifier<OWS> id : list) {
			OWS ows = workspace.getResource(OWSProvider.class, id.getId());
			if (ows == null) {
				continue;
			}
			for (String name : ((OWSProvider) ows.getMetadata().getProvider()).getImplementationMetadata()
				.getHandledRequests()) {
				if (name.equalsIgnoreCase("GetCapabilities")) {
					continue;
				}
				if (name.equalsIgnoreCase(requestName)) {
					services.add(ows);
				}
			}
		}
		return services;
	}

	/**
	 * Determines the {@link OWS} instance that is responsible for handling XML requests
	 * in the given namespace.
	 * @param ns XML namespace
	 * @return responsible <code>OWS</code> or null, if no responsible service was found
	 */
	public List<OWS> getByRequestNS(String ns) {
		List<ResourceIdentifier<OWS>> list = workspace.getResourcesOfType(OWSProvider.class);
		List<OWS> services = new ArrayList<OWS>();
		for (ResourceIdentifier<OWS> id : list) {
			OWS ows = workspace.getResource(OWSProvider.class, id.getId());
			if (ows == null) {
				continue;
			}
			for (String name : ((OWSProvider) ows.getMetadata().getProvider()).getImplementationMetadata()
				.getHandledNamespaces()) {
				if (name.equalsIgnoreCase(ns)) {
					services.add(ows);
				}
			}
		}
		return services;
	}

	/**
	 * Return all active {@link OWS}.
	 * @return the instance of the requested service used by OGCFrontController, or null
	 * if the service is not registered.
	 */
	public Map<String, List<OWS>> getAll() {
		List<ResourceIdentifier<OWS>> list = workspace.getResourcesOfType(OWSProvider.class);
		Map<String, List<OWS>> services = new HashMap<String, List<OWS>>();
		for (ResourceIdentifier<OWS> id : list) {
			OWS ows = workspace.getResource(OWSProvider.class, id.getId());
			if (ows == null) {
				continue;
			}

			String[] names = ((OWSProvider) ows.getMetadata().getProvider()).getImplementationMetadata()
				.getImplementedServiceName();
			for (String name : names) {
				List<OWS> owss = services.get(name);
				if (owss == null) {
					owss = new ArrayList<OWS>();
					services.put(name, owss);
				}
				owss.add(ows);
			}
		}
		return services;
	}

	/**
	 * Returns the service controller instance based on the class of the service
	 * controller.
	 * @param c class of the requested service controller, e.g.
	 * <code>WPSController.getClass()</code>
	 * @return the instance of the requested service used by OGCFrontController, or null
	 * if no such service controller is active
	 */
	public List<OWS> getByOWSClass(Class<?> c) {
		List<ResourceIdentifier<OWS>> list = workspace.getResourcesOfType(OWSProvider.class);
		List<OWS> services = new ArrayList<OWS>();
		for (ResourceIdentifier<OWS> id : list) {
			OWS ows = workspace.getResource(OWSProvider.class, id.getId());
			if (ows != null) {
				if (c == ows.getClass()) {
					services.add(ows);
				}
			}
		}
		return services;
	}

	public OWS getSingleConfiguredService() {
		List<ResourceIdentifier<OWS>> owss = workspace.getResourcesOfType(OWSProvider.class);
		if (owss.size() == 1) {
			return workspace.getResource(OWSProvider.class, owss.get(0).getId());
		}
		return null;
	}

	public boolean isSingleServiceConfigured() {
		return getSingleConfiguredService() != null;
	}

}
