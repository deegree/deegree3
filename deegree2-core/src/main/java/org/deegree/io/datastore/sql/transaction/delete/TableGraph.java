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
package org.deegree.io.datastore.sql.transaction.delete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.TableRelation.FK_INFO;
import org.deegree.model.feature.schema.PropertyType;

/**
 * Represents a delete operation on the relational (table) level. Contains explicit information on all table rows to be
 * removed from the database and their foreign key references.
 *
 * @see FeatureGraph
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class TableGraph {

    private static final ILogger LOG = LoggerFactory.getLogger( TableGraph.class );

    private DeleteHandler handler;

    private List<TableNode> allNodes = new ArrayList<TableNode>();

    private Map<FeatureId, TableNode> fidToNode = new HashMap<FeatureId, TableNode>();

    private int rootFeatureCount = 0;

    /**
     * Creates a new <code>DeleteGraph</code> instance from the given {@link FeatureGraph}.
     *
     * @param featureGraph
     * @param handler
     * @throws DatastoreException
     */
    TableGraph( FeatureGraph featureGraph, DeleteHandler handler ) throws DatastoreException {

        this.handler = handler;

        // add nodes for all deletable features
        for ( FeatureNode rootFeature : featureGraph.getRootNodes() ) {
            if ( rootFeature.isDeletable() ) {
                add( rootFeature );
                rootFeatureCount++;
            }
        }

        // add connectivity information for all feature nodes
        for ( FeatureId fid : this.fidToNode.keySet() ) {
            addConnectivityInfo( fid, featureGraph );
        }
    }

    /**
     * Returns the number of deletable root features.
     *
     * @return number of deletable root features.
     */
    int getDeletableRootFeatureCount() {
        return this.rootFeatureCount;
    }

    /**
     * Adds a {@FeatureNode} and all it's descendant deletable subfeatures to the graph.
     *
     * @param featureNode
     */
    private boolean add( FeatureNode featureNode ) {
        if ( featureNode.isDeletable() ) {
            FeatureId fid = featureNode.getFid();
            if ( fidToNode.get( fid ) == null ) {
                TableNode deleteNode = new TableNode( fid );
                allNodes.add( deleteNode );
                fidToNode.put( fid, deleteNode );
                for ( FeatureNode subFeature : featureNode.getSubFeatures() ) {
                    add( subFeature );
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Adds the connection information (sucessors / precedessors) for the given feature to the graph.
     *
     * @param fid
     * @param fGraph
     * @throws DatastoreException
     */
    private void addConnectivityInfo( FeatureId fid, FeatureGraph fGraph )
                            throws DatastoreException {

        MappedFeatureType ft = fid.getFeatureType();
        PropertyType[] props = ft.getProperties();
        TableNode sourceNode = this.fidToNode.get( fid );
        for ( PropertyType pt : props ) {
            if ( pt instanceof MappedSimplePropertyType || pt instanceof MappedGeometryPropertyType ) {
                TableRelation[] relations = ( (MappedPropertyType) pt ).getTableRelations();
                if ( relations.length == 1 ) {
                    LOG.logDebug( "Adding simple/ geometry property '" + pt.getName() + "'..." );
                    List<TableNode> propNodes = this.handler.determinePropNodes( fid, relations[0] );
                    for ( TableNode node : propNodes ) {
                        add( sourceNode, node, relations[0] );
                    }
                } else if ( relations.length > 1 ) {
                    String msg = Messages.getMessage( "DATASTORE_SQL_DELETE_UNSUPPORTED_JOIN" );
                    throw new DatastoreException( msg );
                }
                // if (relations.length == 0) property is deleted automatically
            } else if ( pt instanceof MappedFeaturePropertyType ) {
                FeatureNode fNode = fGraph.getNode( fid );
                List<FeatureId> subFeatureIds = fNode.getSubFeatureIds( (MappedFeaturePropertyType) pt );
                for ( FeatureId subFid : subFeatureIds ) {
                    connect( fid, sourceNode, (MappedFeaturePropertyType) pt, fGraph.getNode( subFid ) );
                }
            }
        }
    }

    private void add( TableNode featureNode, TableNode propNode, TableRelation relation ) {
        if ( findNode( propNode ) != null ) {
            propNode = findNode( propNode );
        } else {
            allNodes.add( propNode );
        }
        if ( relation.getFKInfo() == FK_INFO.fkIsFromField ) {
            featureNode.connect( propNode );
            propNode.setDeleteVetoPossible();
        } else {
            propNode.connect( featureNode );
        }
    }

    private void connect( FeatureId fid, TableNode sourceNode, MappedFeaturePropertyType pt, FeatureNode subFeature )
                            throws DatastoreException {

        FeatureId subFid = subFeature.getFid();
        TableRelation[] relations = pt.getTableRelations();

        if ( subFeature.isDeletable() ) {
            TableNode subFeatureNode = this.fidToNode.get( subFid );
            switch ( relations.length ) {
            case 1: {
                TableRelation relation = relations[0];
                if ( relation.getFKInfo() == FK_INFO.fkIsFromField ) {
                    sourceNode.connect( subFeatureNode );
                } else {
                    subFeatureNode.connect( sourceNode );
                }
                break;
            }
            case 2: {
                TableNode jtEntry = handler.determineJTNode( fid, subFid, relations[0], relations[1] );
                this.allNodes.add( jtEntry );
                if ( relations[0].getFKInfo() == FK_INFO.fkIsFromField ) {
                    sourceNode.connect( jtEntry );
                } else {
                    jtEntry.connect( sourceNode );
                }
                if ( relations[1].getFKInfo() == FK_INFO.fkIsFromField ) {
                    jtEntry.connect( subFeatureNode );
                } else {
                    subFeatureNode.connect( jtEntry );
                }
                break;
            }
            default: {
                assert false;
            }
            }
        } else {
            if ( relations.length == 2 ) {
                TableNode jtEntry = handler.determineJTNode( fid, subFid, relations[0], relations[1] );
                this.allNodes.add( jtEntry );
                if ( relations[0].getFKInfo() == FK_INFO.fkIsFromField ) {
                    sourceNode.connect( jtEntry );
                } else {
                    jtEntry.connect( sourceNode );
                }
            }
        }
    }

    /**
     * Returns the {@link TableNode} that represents the specified feature instance.
     *
     * @param fid
     *            FeatureId of the feature instance
     * @return TableNode for the feature instance if it exists in the graph, null otherwise
     */
    TableNode findNode( FeatureId fid ) {
        return this.fidToNode.get( fid );
    }

    /**
     * Returns the {@link TableNode} that represents the specified feature instance (given as a {@link TableNode}).
     *
     * @param newNode
     *            feature to look up
     * @return TableNode for the feature instance if it exists in the graph, null otherwise
     */
    TableNode findNode( TableNode newNode ) {
        for ( TableNode node : allNodes ) {
            if ( node.equals( newNode ) ) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns the nodes of the graph in topological order, i.e. they may be deleted in that order without violating any
     * foreign key constraints.
     * <p>
     * NOTE: If the foreign key constraints constitute a cycle, no topological order is possible and the initial order
     * (arbitrary) is returned.
     *
     * @return the nodes of the graph in topological order if no cycle is present, else in arbitrary order
     */
    List<TableNode> getDeletionOrder() {
        List<TableNode> orderedList = new ArrayList<TableNode>( allNodes.size() );

        // key: node (table entry), value: number of open fk constraints
        Map<TableNode, Integer> preMap = new HashMap<TableNode, Integer>();
        // contains only nodes that have no open fk constraints
        List<TableNode> noPreNodes = new ArrayList<TableNode>();
        for ( TableNode node : allNodes ) {
            int preCount = node.getPreNodes().size();
            preMap.put( node, preCount );
            if ( preCount == 0 ) {
                noPreNodes.add( node );
            }
        }

        while ( !noPreNodes.isEmpty() ) {
            TableNode currentNode = noPreNodes.remove( noPreNodes.size() - 1 );
            orderedList.add( currentNode );
            for ( TableNode post : currentNode.getPostNodes() ) {
                int preCount = preMap.get( post ) - 1;
                preMap.put( post, preCount );
                if ( preCount == 0 ) {
                    noPreNodes.add( post );
                }
            }
        }

        if ( orderedList.size() != allNodes.size() ) {
            String msg = "Cannot determine topological order: cycle in fk constraints. Deleting rows in arbitrary order.";
            LOG.logWarning( msg );
            orderedList.clear();
            orderedList.addAll( allNodes );
        }
        return orderedList;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Set<TableNode> printedNodes = new HashSet<TableNode>();

        for ( TableNode node : allNodes ) {
            sb.append( node.toString( "", printedNodes ) );
            sb.append( '\n' );
        }
        return sb.toString();
    }
}
