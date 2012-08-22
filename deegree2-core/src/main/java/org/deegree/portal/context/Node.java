//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.portal.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * encapsulates about a node described/contained by a Web Map Context
 *
 * @version $Revision$
 * @author <a href="mailto:vesll@idgis.nl">Linda Vels</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class Node {

    private int id;

    private String title = null;

    private boolean selectable = false;

    private boolean collapsed = false;

    private Node[] nodes = new Node[0];

    private Node parent = null;

    List<Node[]> tree = new ArrayList<Node[]>( 50 );

    /**
     * Creates a new ContextNode object.
     *
     * @param id
     *            id of the selected node
     * @param parent
     * @param title
     *            title of the selected node
     * @param selectable
     * @param collapsed
     *            defines if the node is collapsed in the legend viewer
     * @throws ContextException
     */
    public Node( int id, Node parent, String title, boolean selectable, boolean collapsed ) throws ContextException {
        setId( id );
        setParent( parent );
        setTitle( title );
        setCollapsed( collapsed );
        setSelectable( selectable );
    }

    /**
     * The childnodes of the selected node in the tree
     *
     * @return all nodes
     */
    public Node[] getNodes() {
        return nodes;
    }

    /**
     * Returns a childnode of the selected node by id
     *
     * @param nodeId
     *            to retrieve
     *
     * @return node by id or <code>null</code> if not found
     */
    public Node getNode( int nodeId ) {
        Node node = null;
        for ( int i = 0; i < nodes.length; i++ ) {
            node = nodes[i].getNode( nodeId );
            if ( node != null ) {
                return node;
            }
            if ( nodes[i].getId() == nodeId ) {
                return nodes[i];
            }
        }
        return node;
    }

    /**
     *
     * @param nodeId
     * @param nodes
     * @return node by id from a list
     */
    public Node getNode( int nodeId, Node[] nodes ) {
        Node node = null;
        for ( int i = 0; i < nodes.length; i++ ) {
            node = nodes[i].getNode( nodeId, nodes[i].getNodes() );
            if ( node != null ) {
                return node;
            }
            if ( nodes[i].getId() == nodeId ) {
                return nodes[i];
            }

        }
        return node;
    }

    /**
     * return the maximum id of all nodes
     *
     * @return maximum id of all nodes
     */
    public int getMaxNodeId() {
        int maxNodeId = id;
        for ( int i = 0; i < nodes.length; i++ ) {
            Node[] brancheNodes = nodes[i].getNodes();
            if ( nodes[i].getId() > maxNodeId ) {
                maxNodeId = nodes[i].getId();

            }
            maxNodeId = getMaxId( brancheNodes, maxNodeId );
        }
        return maxNodeId;
    }

    /**
     *
     * @param nodes
     * @param maxNodeId
     * @return maximum id
     */
    private int getMaxId( Node[] nodes, int maxNodeId ) {
        for ( int i = 0; i < nodes.length; i++ ) {
            Node[] brancheNodes = nodes[i].getNodes();
            if ( nodes[i].getId() > maxNodeId ) {
                maxNodeId = nodes[i].getId();

            }
            maxNodeId = getMaxId( brancheNodes, maxNodeId );
        }
        return maxNodeId;
    }

    /**
     *
     * @param parent
     */
    public void setParent( Node parent ) {
        this.parent = parent;
    }

    /**
     *
     * @return parent node
     */
    public Node getParent() {
        return parent;
    }

    /**
     * The id of the selected node.
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * The title of the selected node.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * The status of the node (collapsed or not (expanded)).
     *
     * @return true if node is collapsed
     */
    public boolean isCollapsed() {
        return collapsed;
    }

    /**
     * The selectable status of the node.
     *
     * @return true if node is selectable
     */
    public boolean isSelectable() {
        return selectable;
    }

    /**
     * @param nodes
     */
    public void setNodes( Node[] nodes ) {
        if ( nodes == null ) {
            nodes = new Node[0];
        }
        this.nodes = nodes;
    }

    /**
     * @param selectable
     */
    public void setSelectable( boolean selectable ) {
        this.selectable = selectable;
    }

    /**
     * @param collapsed
     */
    public void setCollapsed( boolean collapsed ) {
        this.collapsed = collapsed;
    }

    /**
     * @param id
     */
    public void setId( int id ) {
        this.id = id;
    }

    /**
     *
     * @param title
     *
     * @throws ContextException
     */
    public void setTitle( String title )
                            throws ContextException {
        if ( title == null ) {
            throw new ContextException( "title isn't allowed to be null" );
        }
        this.title = title;
    }

    /**
     *
     * @return flat tree as node matrix
     */
    public Node[][] getFlatTree() {
        tree = new ArrayList<Node[]>();
        tree = getBranches( nodes );
        Node[][] flatTree = new Node[tree.size()][];
        return tree.toArray( flatTree );
    }

    /**
     *
     * @param nodes
     * @return tree branches
     */
    private List<Node[]> getBranches( Node[] nodes ) {
        for ( int i = 0; i < nodes.length; i++ ) {
            Node[] branchNodes = nodes[i].getNodes();
            if ( branchNodes.length == 0 ) {
                List<Node> treeRowList = new ArrayList<Node>( 50 );
                Node tmpNode = nodes[i];
                while ( tmpNode != null ) {
                    treeRowList.add( tmpNode );
                    tmpNode = tmpNode.getParent();
                }
                Collections.reverse( treeRowList );
                Node[] treeRow = new Node[treeRowList.size()];
                tree.add( treeRowList.toArray( treeRow ) );
            } else {
                getBranches( branchNodes );
            }
        }
        return tree;

    }

    /**
     * moves a node within the tree up or down
     *
     * @param nodeId
     * @param up
     */
    public void moveNode( int nodeId, Boolean up ) {

        for ( int i = 0; i < nodes.length; i++ ) {
            if ( nodes[i].getId() == nodeId ) {
                Node source = null;
                Node target = null;
                if ( up ) {
                    source = nodes[i];
                    target = nodes[i - 1];
                    nodes[i] = target;
                    nodes[i - 1] = source;
                    return;
                }
                source = nodes[i];
                target = nodes[i + 1];
                nodes[i] = target;
                nodes[i + 1] = source;
                return;

            }
            nodes[i].moveNode( nodeId, up );
        }

    }

    /**
     * adds a new child node to the node
     *
     * @param childNode
     */
    public void appendNode( Node childNode ) {
        Node[] newNodes = new Node[nodes.length + 1];
        System.arraycopy( nodes, 0, newNodes, 0, nodes.length );
        newNodes[nodes.length] = childNode;
        nodes = newNodes;
    }

    /**
     * insert a new child node at given position
     *
     * @param childNode
     *            the new child node
     * @param index
     *            the position of the new child node (0=first node)
     * @throws IndexOutOfBoundsException
     */
    public void insertNode( Node childNode, int index ) {
        if ( index > nodes.length ) {
            throw new IndexOutOfBoundsException();
        }
        Node[] newNodes = new Node[nodes.length + 1];
        System.arraycopy( nodes, 0, newNodes, 0, index );
        newNodes[index] = childNode;
        System.arraycopy( nodes, index, newNodes, index + 1, nodes.length - index );
        nodes = newNodes;
    }

    /**
     * removes a node
     *
     * @param nodeID
     *            the id of the node
     * @return true if node is removed
     */
    public boolean removeNode( int nodeID ) {
        boolean removed = false;

        for ( int i = 0; i < nodes.length; i++ ) {
            if ( nodes[i].id == nodeID ) {
                Node[] tmpNodes = new Node[nodes.length - 1];
                System.arraycopy( nodes, 0, tmpNodes, 0, i );
                System.arraycopy( nodes, i + 1, tmpNodes, i, tmpNodes.length - i );
                nodes = tmpNodes;
                removed = true;
                break;
            }
            // try to find and remove in child node
            if ( nodes[i].removeNode( nodeID ) ) {
                removed = true;
                break;
            }
        }
        return removed;
    }

    /**
     * returns the index of the child node
     *
     * @param nodeID
     *            the node id to look for
     * @return index of the child node, -1 if node is not found
     */
    public int getIndex( int nodeID ) {
        for ( int i = 0; i < nodes.length; i++ ) {
            if ( nodes[i].id == nodeID ) {
                return i;
            }
        }
        return -1;
    }

}
