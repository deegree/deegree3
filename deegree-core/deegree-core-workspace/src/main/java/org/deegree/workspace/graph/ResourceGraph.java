//$HeadURL$
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceMetadata;

/**
 * Responsible for building a graph network based on a list of resource metadata objects. The metadata must obviously be
 * prepared, so they knows their dependencies.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class ResourceGraph {

    private Map<ResourceIdentifier<? extends Resource>, ResourceNode<? extends Resource>> nodeMap;

    public ResourceGraph() {
        nodeMap = new HashMap<ResourceIdentifier<? extends Resource>, ResourceNode<? extends Resource>>();
    }

    /**
     * @param metadata
     *            a list of prepared metadata objects, never <code>null</code>
     */
    public ResourceGraph( List<ResourceMetadata<? extends Resource>> metadata ) {
        this();
        for ( ResourceMetadata<? extends Resource> md : metadata ) {
            ResourceNode<? extends Resource> node = new ResourceNode( this, md );
            nodeMap.put( md.getIdentifier(), node );
        }
        updateDependencies();
    }

    /**
     * @param id
     *            may not be <code>null</code>
     * @return a single node of the dependency network, <code>null</code> if no such node exists
     */
    public <T extends Resource> ResourceNode<T> getNode( ResourceIdentifier<T> id ) {
        return (ResourceNode) nodeMap.get( id );
    }

    /**
     * @param metadata
     *            may not be <code>null</code>
     * @return the new node, never <code>null</code>
     */
    public synchronized <T extends Resource> ResourceNode<T> insertNode( ResourceMetadata<T> metadata ) {
        ResourceNode<T> node = new ResourceNode<T>( this, metadata );
        nodeMap.put( metadata.getIdentifier(), node );

        updateDependencies();

        return node;
    }

    public <T extends Resource> void removeNode (ResourceIdentifier<T> id) {
        nodeMap.remove( id );
        updateDependencies();
    }

    private void updateDependencies() {
        // better algorithm possible?
        for ( ResourceNode<? extends Resource> node : nodeMap.values() ) {
            node.getDependencies().clear();
            node.getDependents().clear();
        }
        for ( ResourceNode<? extends Resource> node : nodeMap.values() ) {
            ResourceMetadata<? extends Resource> md = node.getMetadata();
            for ( ResourceIdentifier<? extends Resource> id : md.getDependencies() ) {
                ResourceNode<? extends Resource> depNode = nodeMap.get( id );
                if ( depNode != null ) {
                    node.addDependency( depNode );
                    depNode.addDependent( node );
                }
            }
            for ( ResourceIdentifier<? extends Resource> id : md.getSoftDependencies() ) {
                ResourceNode<? extends Resource> depNode = nodeMap.get( id );
                if ( depNode != null ) {
                    node.addSoftDependency( depNode );
                    depNode.addDependent( node );
                }
            }
        }
    }

    /**
     * The list is sorted by initialization order, with resources that need to be initialized first at the beginning of
     * the list.
     * 
     * @return a sorted list of resource metadata objects, never <code>null</code>
     */
    public List<ResourceMetadata<? extends Resource>> toSortedList() {
        // sketch: first add resources without dependencies, then add resources whose dependencies are met until done
        HashSet<ResourceNode<? extends Resource>> nodes = new HashSet<ResourceNode<?>>( nodeMap.values() );

        List<ResourceMetadata<? extends Resource>> roots = getRoots( nodes );

        boolean changed = true;

        outer: while ( !nodes.isEmpty() ) {
            if ( changed ) {
                changed = false;
            } else {
                throw new ResourceException( "There are inconsistent dependency chains." );
            }
            inner: for ( ResourceNode<? extends Resource> node : nodes ) {
                for ( ResourceNode<? extends Resource> dep : node.getDependencies() ) {
                    if ( !roots.contains( dep.getMetadata() ) ) {
                        continue inner;
                    }
                }
                for ( ResourceNode<? extends Resource> dep : node.getSoftDependencies() ) {
                    if ( !roots.contains( dep.getMetadata() ) ) {
                        continue inner;
                    }
                }
                roots.add( node.getMetadata() );
                nodes.remove( node );
                changed = true;
                // could be optimized by continuing to inner, needs a little rewrite
                continue outer;
            }
        }

        return roots;
    }

    private List<ResourceMetadata<? extends Resource>> getRoots( HashSet<ResourceNode<? extends Resource>> nodes ) {
        List<ResourceMetadata<? extends Resource>> roots = new ArrayList<ResourceMetadata<? extends Resource>>();
        for ( ResourceNode<? extends Resource> node : nodeMap.values() ) {
            if ( node.getDependencies().isEmpty() && node.getSoftDependencies().isEmpty() ) {
                roots.add( node.getMetadata() );
                nodes.remove( node );
            }
        }
        return roots;
    }

}
