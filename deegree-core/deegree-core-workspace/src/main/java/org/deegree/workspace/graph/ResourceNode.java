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
package org.deegree.workspace.graph;

import java.util.ArrayList;
import java.util.List;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceMetadata;

/**
 * A node in the dependency graph. Coupled with a resource metadata object.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class ResourceNode<T extends Resource> {

	private ResourceGraph graph;

	private ResourceMetadata<T> metadata;

	private List<ResourceNode<? extends Resource>> dependencies = new ArrayList<ResourceNode<? extends Resource>>();

	private List<ResourceNode<? extends Resource>> softDependencies = new ArrayList<ResourceNode<? extends Resource>>();

	private List<ResourceNode<? extends Resource>> dependents = new ArrayList<ResourceNode<? extends Resource>>();

	/**
	 * @param graph never <code>null</code>
	 * @param metadata never <code>null</code>
	 */
	public ResourceNode(ResourceGraph graph, ResourceMetadata<T> metadata) {
		this.graph = graph;
		this.metadata = metadata;
	}

	/**
	 * @param node never <code>null</code>
	 */
	public void addDependent(ResourceNode<? extends Resource> node) {
		if (!dependents.contains(node)) {
			dependents.add(node);
		}
	}

	/**
	 * @param node never <code>null</code>
	 */
	public void addDependency(ResourceNode<? extends Resource> node) {
		if (!dependencies.contains(node)) {
			dependencies.add(node);
		}
	}

	public void addSoftDependency(ResourceNode<? extends Resource> node) {
		if (!softDependencies.contains(node)) {
			softDependencies.add(node);
		}
	}

	/**
	 * @return the metadata, never <code>null</code>
	 */
	public ResourceMetadata<T> getMetadata() {
		return metadata;
	}

	/**
	 * @return true, if all dependencies are available
	 */
	public boolean areDependenciesAvailable() {
		for (ResourceIdentifier<? extends Resource> id : metadata.getDependencies()) {
			if (graph.getNode(id) == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the list of dependency nodes, never <code>null</code>
	 */
	public List<ResourceNode<? extends Resource>> getDependencies() {
		return dependencies;
	}

	/**
	 * @return the list of soft dependency nodes, never <code>null</code>
	 */
	public List<ResourceNode<? extends Resource>> getSoftDependencies() {
		return softDependencies;
	}

	/**
	 * @return the list of dependent nodes, never <code>null</code>
	 */
	public List<ResourceNode<? extends Resource>> getDependents() {
		return dependents;
	}

	/**
	 * A {!} signals that dependencies are unavailable at a specific node.
	 * @return a string representation of all nodes related to this one
	 */
	public String print() {
		return printDependencies(this) + metadata.getIdentifier() + printDependents(this);
	}

	private String printDependents(ResourceNode<? extends Resource> node) {
		StringBuilder sb = new StringBuilder();

		sb.append(" -> { ");
		for (ResourceNode<? extends Resource> n : node.getDependents()) {
			sb.append(n.getMetadata().getIdentifier() + printDependents(n));
			sb.append(" ");
		}
		sb.append("}");

		return sb.toString();
	}

	private String printDependencies(ResourceNode<? extends Resource> node) {
		if (!node.areDependenciesAvailable()) {
			return "{!} -> ";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		for (ResourceNode<? extends Resource> n : node.getDependencies()) {
			sb.append(printDependencies(n) + n.getMetadata().getIdentifier());
			sb.append(" ");
		}
		sb.append("} -> ");
		return sb.toString();
	}

	@Override
	public String toString() {
		return metadata.getIdentifier().toString();
	}

}
