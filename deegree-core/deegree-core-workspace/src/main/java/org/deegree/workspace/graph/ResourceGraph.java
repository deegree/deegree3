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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceMetadata;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * Responsible for building a graph network based on a list of resource metadata objects. The metadata must obviously be
 * prepared, so they knows their dependencies.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @param <E>
 * @param <V>
 * 
 * @since 3.4
 */
public class ResourceGraph {

    private final DefaultDirectedGraph<ResourceIdentifier<?>, DefaultEdge> graph;

    /**
     * Creates a new graph without any content.
     */
    public ResourceGraph() {
        this( new DefaultDirectedGraph<ResourceIdentifier<?>, DefaultEdge>( DefaultEdge.class ) );
    }

    private ResourceGraph( DefaultDirectedGraph<ResourceIdentifier<?>, DefaultEdge> graph ) {
        this.graph = graph;
    }

    /**
     * @param metadata
     *            may not be <code>null</code>
     * @return the new node, never <code>null</code>
     */
    public synchronized void insertNode( ResourceMetadata<?> metadata ) {
        ResourceIdentifier<?> identifier = metadata.getIdentifier();
        if ( !graph.containsVertex( identifier ) ) {
            graph.addVertex( identifier );
        }
        insertNode( identifier, metadata.getDependencies() );
        insertNode( identifier, metadata.getSoftDependencies() );
    }

    /**
     * The provided iterator iterates through the graph in initialization order, with resources that need to be
     * initialized first at the beginning.
     * 
     * @return an Iterator iterating through the graph in initialization order, never <code>null</code>
     */
    public Iterator<ResourceIdentifier<?>> traverseGraphFromBottomToTop() {
        return new TopologicalOrderIterator<ResourceIdentifier<?>, DefaultEdge>( graph );
    }

    /**
     * The provided iterator iterates through the graph in reversed initialization order, with resources that need to be
     * initialized last at the beginning.
     * 
     * @return
     */
    public Iterator<ResourceIdentifier<?>> traverseGraphFromTopToBottom() {
        return new ClosestFirstIterator<ResourceIdentifier<?>, DefaultEdge>( graph );
    }

    /**
     * @param nodeInGraph
     *            the node which identifies the graph to return, never <code>null</code>
     * @return the graph the nodeInGraph node is part of, may be <code>null</code> if the node is not part of this graph
     */
    public ResourceGraph getSubgraph( ResourceIdentifier<?> nodeInGraph ) {
        if ( !graph.containsVertex( nodeInGraph ) )
            return null;
        DefaultDirectedGraph<ResourceIdentifier<?>, DefaultEdge> subgraph = new DefaultDirectedGraph<ResourceIdentifier<?>, DefaultEdge>(
                                                                                                                                          DefaultEdge.class );
        subgraph.addVertex( nodeInGraph );
        appendRelatedVerticesAndEdges( nodeInGraph, subgraph );
        // graph.getEdgeSource( e );
        return new ResourceGraph( subgraph );
    }

    /**
     * @param nodeInGraph
     *            , never <code>null</code>
     * @return the dependents of of the passed node, <code>null</code> if the node is not part of this graph
     */
    public List<ResourceIdentifier<?>> getDependents( ResourceIdentifier<?> nodeInGraph ) {
        if ( !graph.containsVertex( nodeInGraph ) )
            return null;
        List<ResourceIdentifier<?>> dependents = new ArrayList<ResourceIdentifier<?>>();
        Set<DefaultEdge> outgoingEdgesOf = graph.outgoingEdgesOf( nodeInGraph );
        for ( DefaultEdge outgoingEdge : outgoingEdgesOf ) {
            dependents.add( graph.getEdgeTarget( outgoingEdge ) );
        }
        return dependents;
    }

    /**
     * @param nodeInGraph
     *            , never <code>null</code>
     * @return the dependencies of of the passed node, <code>null</code> if the node is not part of this graph
     */
    public List<ResourceIdentifier<?>> getDependencies( ResourceIdentifier<?> nodeInGraph ) {
        if ( !graph.containsVertex( nodeInGraph ) )
            return null;
        List<ResourceIdentifier<?>> dependencies = new ArrayList<ResourceIdentifier<?>>();
        Set<DefaultEdge> incomingEdgesOf = graph.incomingEdgesOf( nodeInGraph );
        for ( DefaultEdge incomingEdge : incomingEdgesOf ) {
            dependencies.add( graph.getEdgeSource( incomingEdge ) );
        }
        return dependencies;
    }

    private void insertNode( ResourceIdentifier<?> identifier, Set<ResourceIdentifier<? extends Resource>> dependencies ) {
        for ( ResourceIdentifier<? extends Resource> dep : dependencies ) {
            if ( !graph.containsVertex( dep ) ) {
                graph.addVertex( dep );
                graph.addEdge( dep, identifier );
            } else {
                if ( !graph.containsEdge( dep, identifier ) )
                    graph.addEdge( dep, identifier );
            }
        }
    }

    private void appendRelatedVerticesAndEdges( ResourceIdentifier<?> vertex,
                                                DefaultDirectedGraph<ResourceIdentifier<?>, DefaultEdge> subgraph ) {

        Set<DefaultEdge> outgoingEdgesOf = graph.outgoingEdgesOf( vertex );
        for ( DefaultEdge outgoingEdge : outgoingEdgesOf ) {
            ResourceIdentifier<?> targetVertex = graph.getEdgeTarget( outgoingEdge );
            if ( !subgraph.containsVertex( targetVertex ) ) {
                subgraph.addVertex( targetVertex );
                if ( !subgraph.containsEdge( vertex, targetVertex ) )
                    subgraph.addEdge( vertex, targetVertex );
                appendRelatedVerticesAndEdges( targetVertex, subgraph );
            }
        }
        Set<DefaultEdge> incomingEdgesOf = graph.incomingEdgesOf( vertex );
        for ( DefaultEdge incomingEdge : incomingEdgesOf ) {
            ResourceIdentifier<?> sourceVertex = graph.getEdgeSource( incomingEdge );
            if ( !subgraph.containsVertex( sourceVertex ) ) {
                subgraph.addVertex( sourceVertex );
                if ( !subgraph.containsEdge( sourceVertex, vertex ) )
                    subgraph.addEdge( sourceVertex, vertex );
                appendRelatedVerticesAndEdges( sourceVertex, subgraph );
            }
        }
    }

}