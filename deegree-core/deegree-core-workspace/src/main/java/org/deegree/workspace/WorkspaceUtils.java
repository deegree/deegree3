/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.deegree.workspace.graph.ResourceGraph;
import org.deegree.workspace.graph.ResourceNode;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultResourceLocation;
import org.deegree.workspace.standard.IncorporealResourceLocation;

/**
 * Utility methods to work with workspaces and its resources.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class WorkspaceUtils {

	/**
	 * Destroys and initializes all resources connected to the resource with the given id.
	 * @param workspace may not be <code>null</code>
	 * @param id may not be <code>null</code>
	 * @return a list of identifiers of the initialized resources, never <code>null</code>
	 */
	public static List<String> reinitializeChain(Workspace workspace, ResourceIdentifier<? extends Resource> id) {
		List<String> initialisedIdentifiers = new ArrayList<>();
		ResourceNode<? extends Resource> node = workspace.getDependencyGraph().getNode(id);
		List<ResourceMetadata<? extends Resource>> list = new ArrayList<ResourceMetadata<? extends Resource>>();
		ResourceMetadata<? extends Resource> meta = workspace.getResourceMetadata(id.getProvider(), id.getId());
		if (meta != null) {
			list.add(meta);
			collectDependencies(list, node);
			collectDependents(list, node);
			ResourceGraph g = new ResourceGraph(list);
			list = g.toSortedList();
			workspace.destroy(list.get(0).getIdentifier());
			for (ResourceMetadata<? extends Resource> md : list) {
				ResourceIdentifier<? extends Resource> identifier = md.getIdentifier();
				workspace.add(md.getLocation());
				workspace.prepare(identifier);
				workspace.init(identifier, null);
				initialisedIdentifiers.add(identifier.getId());
			}
		}
		return initialisedIdentifiers;
	}

	/**
	 * Collects all transitive dependencies of the resource node given.
	 * @param list may not be <code>null</code>
	 * @param node may not be <code>null</code>
	 */
	public static void collectDependencies(List<ResourceMetadata<? extends Resource>> list,
			ResourceNode<? extends Resource> node) {
		if (node == null) {
			return;
		}
		collectDependencies(list, node.getDependencies());
		collectDependencies(list, node.getSoftDependencies());
	}

	/**
	 * Transitively collects all resources which depend on the resource connected to the
	 * node given.
	 * @param list may not be <code>null</code>
	 * @param node may not be <code>null</code>
	 */
	public static void collectDependents(List<ResourceMetadata<? extends Resource>> list,
			ResourceNode<? extends Resource> node) {
		if (node == null) {
			return;
		}
		for (ResourceNode<? extends Resource> n : node.getDependents()) {
			if (n.getMetadata() != null) {
				list.add(n.getMetadata());
				collectDependents(list, n);
			}
		}
	}

	/**
	 * Adds and completely initializes a synthetic resource from string.
	 * @param workspace may not be <code>null</code>
	 * @param providerClass may not be <code>null</code>
	 * @param id may not be <code>null</code>
	 * @param content configuration content, may not be <code>null</code>
	 * @return the initialized resource
	 */
	public static <T extends Resource> T activateSynthetic(Workspace workspace,
			Class<? extends ResourceProvider<T>> providerClass, String id, String content) {
		IncorporealResourceLocation<? extends Resource> loc;
		ResourceIdentifier<T> identifier = new DefaultResourceIdentifier<T>(providerClass, id);
		Charset cs = Charset.forName("UTF-8");
		loc = new IncorporealResourceLocation<T>(content.getBytes(cs), identifier);
		workspace.add(loc);
		workspace.prepare(identifier);
		return workspace.init(identifier, null);
	}

	/**
	 * Adds and completely initializes a synthetic resource from file.
	 * @param workspace may not be <code>null</code>
	 * @param providerClass may not be <code>null</code>
	 * @param id may not be <code>null</code>
	 * @param content configuration content, may not be <code>null</code>
	 * @return the initialized resource
	 */
	public static <T extends Resource> T activateFromFile(Workspace workspace,
			Class<? extends ResourceProvider<T>> providerClass, String id, File content) {
		DefaultResourceLocation<? extends Resource> loc;
		ResourceIdentifier<T> identifier = new DefaultResourceIdentifier<T>(providerClass, id);
		loc = new DefaultResourceLocation<T>(content, identifier);
		workspace.add(loc);
		workspace.prepare(identifier);
		return workspace.init(identifier, null);
	}

	/**
	 * Adds and completely initializes a synthetic resource from an URL.
	 * @param workspace may not be <code>null</code>
	 * @param providerClass may not be <code>null</code>
	 * @param id may not be <code>null</code>
	 * @param content configuration content, may not be <code>null</code>
	 * @return the initialized resource
	 */
	public static <T extends Resource> T activateFromUrl(Workspace workspace,
			Class<? extends ResourceProvider<T>> providerClass, String id, URL content) {
		IncorporealResourceLocation<? extends Resource> loc;
		ResourceIdentifier<T> identifier = new DefaultResourceIdentifier<T>(providerClass, id);
		try {
			loc = new IncorporealResourceLocation<T>(IOUtils.toByteArray(content), identifier);
			workspace.add(loc);
			workspace.prepare(identifier);
			return workspace.init(identifier, null);
		}
		catch (Exception e) {
			throw new ResourceInitException("Unable to load URL " + content + ": " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Searches for resource managers corresponding to a workspace path.
	 * @param workspace may not be <code>null</code>
	 * @param path may not be <code>null</code>, may contain other path elements
	 * @return a list of matching resource managers, may be empty but never
	 * <code>null</code>
	 */
	public static List<ResourceManager<?>> getManagersForWorkspacePath(Workspace workspace, String path) {
		List<ResourceManager<?>> result = new ArrayList<ResourceManager<?>>();
		List<ResourceManager<?>> list = workspace.getResourceManagers();
		for (ResourceManager<?> mgr : list) {
			ResourceManagerMetadata<?> md = mgr.getMetadata();
			if (path.startsWith(md.getWorkspacePath())) {
				result.add(mgr);
			}
		}
		return result;
	}

	/**
	 * Returns a list of possible resource identifiers for a given workspace path.
	 * @param workspace
	 * @param path
	 * @return
	 */
	public static List<ResourceIdentifier<?>> getPossibleIdentifiers(Workspace workspace, String path) {
		List<ResourceManager<?>> list = getManagersForWorkspacePath(workspace, path);
		List<ResourceIdentifier<?>> result = new ArrayList<ResourceIdentifier<?>>();
		for (ResourceManager<?> mgr : list) {
			Class<?> cls = mgr.getMetadata().getProviderClass();
			int one = mgr.getMetadata().getWorkspacePath().endsWith("/") ? 0 : 1;
			String id = path.substring(mgr.getMetadata().getWorkspacePath().length() + one);
			result.add(new DefaultResourceIdentifier(cls, id));
		}
		return result;
	}

	private static void collectDependencies(List<ResourceMetadata<? extends Resource>> list,
			List<ResourceNode<? extends Resource>> dependencies) {
		for (ResourceNode<? extends Resource> n : dependencies) {
			if (n.getMetadata() != null) {
				list.add(n.getMetadata());
				collectDependencies(list, n);
			}
		}
	}

}
