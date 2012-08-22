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
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class LayerList {

    private List<Layer> list = new ArrayList<Layer>( 50 );

    private List<Layer> treeList = new ArrayList<Layer>( 50 );

    /**
     * Creates a new LayerList object.
     *
     * @param layers
     */
    public LayerList( Layer[] layers ) {
        setLayers( layers );
    }

    
    /**
     * returns a layer identifies by its name and service address
     *
     * @param name
     *            name ofthe layer
     * @param serverAddress
     *            address of the server which servers the layer
     *
     * @return named layer
     */
    public Layer getLayer( String name, String serverAddress ) {

        if ( serverAddress != null ) {
            for ( Layer layer : list ) {
                String s = layer.getServer().getOnlineResource().toExternalForm();
                if ( layer.getName().equals( name ) && s.equals( serverAddress ) ) {
                    return layer;
                }
            }
        } else {
            for ( Layer layer : list ) {
                if ( layer.getName().equals( name ) ) {
                    return layer;
                }
            }
        }
        return null;
    }

    /**
     * returns all layers of the web map context
     *
     * @return array of layers
     */
    public Layer[] getLayers() {
        Layer[] cl = new Layer[list.size()];
        return list.toArray( cl );
    }

    /**
     * sets all layers of the web map context
     *
     * @param layers
     */
    public void setLayers( Layer[] layers ) {
        this.list.clear();

        if ( layers != null ) {
            for ( int i = 0; i < layers.length; i++ ) {
                list.add( layers[i] );
            }
        }
    }

    /**
     * @param id
     *
     * @return the layers of a node of the tree of the web map context
     */
    public Layer[] getLayersByNodeId( int id ) {
        List<Layer> nodeLayerList = new ArrayList<Layer>( list.size() );
        int parentNodeId;
        for ( int k = 0; k < list.size(); k++ ) {
            parentNodeId = ( list.get( k ) ).getExtension().getParentNodeId();
            if ( parentNodeId == id ) {
                nodeLayerList.add( list.get( k ) );
            }
        }
        Layer[] nodeLayers = new Layer[nodeLayerList.size()];
        return nodeLayerList.toArray( nodeLayers );
    }

    /**
     * TODO: review this changed; it has been introduced as of TreeLayerView Portlet
     *
     * @param root
     */
    public void orderLayerListByLayerTree( Node root ) {
        treeList.clear();
        Node[] rootNode = new Node[1];
        rootNode[0] = root;
        treeList = getLayerListByNode( rootNode );
        setLayers( treeList.toArray( new Layer[treeList.size()] ) );
    }

    /**
     *
     * @param nodes
     * @return list of layers
     */
    private List<Layer> getLayerListByNode( Node[] nodes ) {
        for ( int i = 0; i < nodes.length; i++ ) {
            getLayerListByNode( nodes[i].getNodes() );
            Layer[] nl = getLayersByNodeId( nodes[i].getId() );
            for ( int j = 0; j < nl.length; j++ ) {
                treeList.add( nl[j] );
            }

        }
        return treeList;
    }

    /**
     * adds one layer to the the web map context. If a layer with the same name as the passed layer already exits it
     * will be overwritten
     *
     * @param layer
     */
    public void addLayer( Layer layer ) {
        list.add( layer );
    }

    /**
     * adds one layer to the top of the web map context. If a layer with the same name as the passed layer already exits
     * it will be overwritten
     *
     * @param layer
     */
    public void addLayerToTop( Layer layer ) {
        list.add( 0, layer );
    }

    

    /**
     * removes a layer identified by its name from the web map context
     *
     * @param name
     *            name of the layer to be removed
     * @param serverAddress
     *
     * @return removed layer
     */
    public Layer removeLayer( String name, String serverAddress ) {
        Layer layer = getLayer( name, serverAddress );
        list.remove( layer );
        return layer;
    }

    /**
     * moves a layer within the layer list up or down
     *
     * @param layer
     *            layer to be moved
     * @param up
     *            if true the layer will be moved up otherwise it will be moved down
     */
    public void move( Layer layer, boolean up ) {
        int i = 0;
        Layer target = null;
        while ( i < list.size() && target == null ) {
            Layer tmp = list.get( i );
            if ( tmp.getName().equals( layer.getName() )
                 && tmp.getServer().getOnlineResource().equals( layer.getServer().getOnlineResource() ) ) {
                target = tmp;
            }
            i++;
        }
        i--;
        if ( i > 0 && up ) {
            Layer o = list.get( i );
            list.set( i, list.get( i - 1 ) );
            list.set( i - 1, o );
        } else if ( i < list.size() - 1 && !up ) {
            Layer o = list.get( i );
            list.set( i, list.get( i + 1 ) );
            list.set( i + 1, o );
        }
    }

    /**
     * moves a layer within the layer list before the beforeLayer
     *
     * @param layer
     *            layer to be moved
     * @param beforeLayer
     *            put the layer before this beforeLayer. If beforeLayer is <code>null</code> move to bottom.
     *
     */
    public void move( Layer layer, Layer beforeLayer ) {
        if ( beforeLayer != null ) { // move layer before beforeLayer
            int i = 0;
            ArrayList<Layer> newList = new ArrayList<Layer>( list.size() );

            while ( i < list.size() ) {
                Layer tmp = list.get( i );
                if ( tmp.getName().equals( beforeLayer.getName() )
                     && tmp.getServer().getOnlineResource().equals( beforeLayer.getServer().getOnlineResource() ) ) {
                    newList.add( layer );
                    newList.add( beforeLayer );
                } else if ( tmp.getName().equals( layer.getName() )
                            && tmp.getServer().getOnlineResource().equals( layer.getServer().getOnlineResource() ) ) {
                    // do nothing
                } else {
                    newList.add( tmp );
                }
                i++;
            }
            list = newList;
        } else { // move to end...
            // removeLayer( layer.getName(), layer.getServer().getOnlineResource().toString() );
            // addLayer( layer );
            // ...but not at the end of the list! some utils need the list ordered
            // by the parent node IDs.
            int parentNodeID = layer.getExtension().getParentNodeId();
            removeLayer( layer.getName(), layer.getServer().getOnlineResource().toString() );
            boolean inParentNode = false;
            boolean inserted = false;
            List<Layer> newList = new LinkedList<Layer>();
            int i = 0;
            while ( i < list.size() ) {
                Layer tmp = list.get( i );
                if ( tmp.getExtension().getParentNodeId() == parentNodeID ) {
                    inParentNode = true;
                } else if ( inParentNode ) { // end of parentNode, point to insert layer
                    newList.add( layer );
                    inParentNode = false;
                    inserted = true;
                }
                newList.add( tmp );
                i++;
            }

            if ( !inserted ) {
                newList.add( layer );
            }
            list = newList;
        }
    }

    /**
     * move all layers with parent <code>nodeID</code> befor the layers with parent <code>beforeNodeID</code>.
     *
     * @param nodeID
     * @param beforeNodeID
     */
    public void moveNodes( int nodeID, int beforeNodeID ) {
        List<Layer> result = new LinkedList<Layer>();
        List<Layer> tmpNodeLayers = new LinkedList<Layer>();
        boolean nodeLayersInserted = false;

        // collect all affected nodes
        for ( Layer layer : list ) {
            if ( layer.getExtension().getParentNodeId() == nodeID ) {
                tmpNodeLayers.add( layer );
            }
        }

        for ( Layer layer : list ) {
            if ( layer.getExtension().getParentNodeId() == nodeID ) {
                // don't insert
            } else {
                // insert before beforeNodeID
                if ( !nodeLayersInserted && layer.getExtension().getParentNodeId() == beforeNodeID ) {
                    result.addAll( tmpNodeLayers );
                    nodeLayersInserted = true;
                }
                // add all other nodes
                result.add( layer );
            }
        }
        // if not inserted put at the end (eg. beforeNodeID not found)
        if ( !nodeLayersInserted ) {
            result.addAll( tmpNodeLayers );
        }
        list = result;
    }

    /**
     * removes all layers from the web map context
     */
    public void clear() {
        list.clear();
    }

    /**
     * remove all layer with <code>nodeID</code> as parent node
     *
     * @param nodeID
     */
    public void removeLayers( int nodeID ) {
        List<Layer> result = new LinkedList<Layer>();
        for ( Layer layer : list ) {
            if ( layer.getExtension().getParentNodeId() != nodeID ) {
                result.add( layer );
            }
        }
        list = result;
    }

}
