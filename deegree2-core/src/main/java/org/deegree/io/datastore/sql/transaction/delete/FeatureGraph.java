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
import org.deegree.io.datastore.sql.AbstractRequestHandler;
import org.deegree.io.datastore.sql.LockHandler;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;

/**
 * Used to determine the structure of features and subfeatures that have to be deleted by a {@link Delete} operation.
 * <p>
 * When features are selected for deletion, all of their descendant subfeatures will be deleted as well, except for
 * those features that have superfeatures which are not descendants of the features to be deleted.
 *
 * @see DeleteHandler
 * @see FeatureNode
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureGraph {

    private static final ILogger LOG = LoggerFactory.getLogger( FeatureGraph.class );

    private AbstractRequestHandler handler;

    private Set<FeatureId> rootFeatures = new HashSet<FeatureId>();

    private Map<FeatureId, FeatureNode> fidToNode = new HashMap<FeatureId, FeatureNode>();

    /**
     * Creates a new <code>FeatureGraph</code> instance for the given root {@link FeatureId}s and the specified
     * {@link DeleteHandler}.
     *
     * @param rootFids
     *            ids of the feature instances to be deleted
     * @param handler
     *            associated <code>DeleteHandler</code>
     * @throws DatastoreException
     */
    FeatureGraph( List<FeatureId> rootFids, DeleteHandler handler ) throws DatastoreException {
        this.handler = handler;
        for ( FeatureId fid : rootFids ) {
            this.rootFeatures.add( fid );
            addNode( fid );
        }
        markUndeletableFeatures();
    }

    /**
     * Creates a new <code>FeatureGraph</code> instance for the given root {@link FeatureId}s and the specified
     * {@link LockHandler}.
     *
     * @param rootFids
     *            ids of the feature instances to be locked
     * @param handler
     *            associated <code>LockHandler</code>
     * @throws DatastoreException
     */
    public FeatureGraph( Set<FeatureId> rootFids, LockHandler handler ) throws DatastoreException {
        this.handler = handler;
        for ( FeatureId fid : rootFids ) {
            this.rootFeatures.add( fid );
            addNode( fid );
        }
    }

    /**
     * Returns the {@link FeatureId}s of all features contained in the graph.
     *
     * @return the <code>FeatureId</code>s of all features contained in the graph
     */
    public Set<FeatureId> getAllFids() {
        return this.fidToNode.keySet();
    }

    /**
     * Returns the {@link FeatureNode} that represents the feature with the given {@link FeatureId}.
     *
     * @param fid
     *            id of the feature to look up
     * @return the corresponding <code>FeatureNode</code> if it exists in the graph, null otherwise
     */
    FeatureNode getNode( FeatureId fid ) {
        return fidToNode.get( fid );
    }

    /**
     * Returns the {@link FeatureNode}s that represent all root features that have been targeted for deletion.
     *
     * @return <code>FeatureNode</code> representing all root features
     */
    List<FeatureNode> getRootNodes() {
        List<FeatureNode> rootNodes = new ArrayList<FeatureNode>( this.rootFeatures.size() );
        for ( FeatureId fid : rootFeatures ) {
            rootNodes.add( getNode( fid ) );
        }
        return rootNodes;
    }

    /**
     * Adds the specified feature (and it's descendants) (as {@link FeatureNode}s).
     *
     * @param fid
     *            the id of the feature to be added
     * @throws DatastoreException
     */
    private void addNode( FeatureId fid )
                            throws DatastoreException {

        if ( this.fidToNode.get( fid ) == null ) {
            // skip determination of super features if feature is not deletable anyway
            Set<FeatureId> superFeatures = null;
            if ( fid.getFeatureType().isDeletable() ) {
                superFeatures = handler.determineSuperFeatures( fid );
            } else {
                LOG.logDebug( "Skipping super feature lookup for feature: " + fid
                              + " -- feature type is not deletable anyway." );
                superFeatures = new HashSet<FeatureId>();
                superFeatures.add (fid);
            }

            Map<MappedFeaturePropertyType, List<FeatureId>> subFeatures = handler.determineSubFeatures( fid );
            FeatureNode node = new FeatureNode( this, fid, subFeatures, superFeatures );
            this.fidToNode.put( fid, node );

            for ( FeatureId subFid : node.getSubFeatureIds() ) {
                addNode( subFid );
            }
        }
    }

    /**
     * Marks all {@link FeatureNode}s in the graph that cannot be deleted, because they are descendants (subfeatures)
     * of other undeletable {@link FeatureNode}s.
     */
    private void markUndeletableFeatures() {

        LOG.logDebug( "Determining undeletable features." );

        int lastVetoCount = -1;
        int vetoCount = 0;

        while ( vetoCount != lastVetoCount ) {
            LOG.logDebug( "vetoCount: " + vetoCount );
            LOG.logDebug( "lastVetoCount: " + lastVetoCount );
            lastVetoCount = vetoCount;
            vetoCount = 0;
            for ( FeatureNode node : this.fidToNode.values() ) {
                if ( node.isDeletable() ) {
                    boolean deletable = true;
                    if ( !node.getFid().getFeatureType().isDeletable() ) {
                        String msg = Messages.getMessage( "DATASTORE_FEATURE_NOT_DELETABLE", node.getFid().toString() );
                        LOG.logInfo( msg );
                        deletable = false;
                    } else {
                        for ( FeatureId superFid : node.getSuperFeatureIds() ) {
                            FeatureNode superNode = getNode( superFid );
                            if ( superNode == null || !superNode.isDeletable() ) {
                                deletable = false;
                                break;
                            }
                        }
                    }
                    if ( !deletable ) {
                        node.markAsUndeletable();
                        vetoCount++;
                    }
                } else {
                    vetoCount++;
                }
            }
        }
        LOG.logDebug( "Delete vetos: " + lastVetoCount );
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( "FeatureGraph:\n" );
        for ( FeatureId rootFid : this.rootFeatures ) {
            sb.append( this.fidToNode.get( rootFid ).toString( "", new HashSet<FeatureNode>() ) );
        }
        return sb.toString();
    }
}
