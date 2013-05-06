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
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceMetadata;

/**
 * Responsible for building a graph network based on a list of resource metadata objects. The metadata must obviously be
 * prepared, so they knows their dependencies.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
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
        List<ResourceNode<? extends Resource>> nodes = new ArrayList<ResourceNode<? extends Resource>>();
        for ( ResourceMetadata<? extends Resource> md : metadata ) {
            ResourceNode<? extends Resource> node = new ResourceNode( this, md );
            nodes.add( node );
            nodeMap.put( md.getIdentifier(), node );
        }
        for ( ResourceNode<? extends Resource> node : nodes ) {
            ResourceMetadata<? extends Resource> md = node.getMetadata();
            for ( ResourceIdentifier<? extends Resource> id : md.getDependencies() ) {
                ResourceNode<? extends Resource> depNode = nodeMap.get( id );
                if ( depNode != null ) {
                    node.addDependency( depNode );
                    depNode.addDependent( node );
                }
            }
        }
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
        }
    }

    public List<ResourceMetadata<? extends Resource>> toSortedList() {
        HashSet<ResourceNode<? extends Resource>> nodes = new HashSet<ResourceNode<?>>( nodeMap.values() );

        List<ResourceMetadata<? extends Resource>> roots = new ArrayList<ResourceMetadata<? extends Resource>>();
        for ( ResourceNode<? extends Resource> node : nodeMap.values() ) {
            if ( node.getDependencies().isEmpty() ) {
                roots.add( node.getMetadata() );
                nodes.remove( node );
            }
        }

        outer: while ( !nodes.isEmpty() ) {
            inner: for ( ResourceNode<? extends Resource> node : nodes ) {
                for ( ResourceNode<? extends Resource> dep : node.getDependencies() ) {
                    if ( !roots.contains( dep.getMetadata() ) ) {
                        continue inner;
                    }
                }
                roots.add( node.getMetadata() );
                nodes.remove( node );
                continue outer;
            }
        }

        return roots;
    }

}
