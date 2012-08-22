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
package org.deegree.io.quadtree;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;
import org.deegree.io.quadtree.DBQuadtree.SupportedVersions;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;

/**
 * Represents a node of a {@link DBQuadtree}. Nodes contain items which have a spatial extent
 * corresponding to the node's position in the quadtree.
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
class DBNode<T> implements Node<T> {

    private static ILogger LOG = LoggerFactory.getLogger( DBNode.class );

    private String id = null;

    private int level;

    private String[] fk_subnode = new String[4];

    private Envelope envelope = null;

    private JDBCConnection jdbc = null;

    private DBQuadtree<T> qt = null;

    private String indexTable = null;

    private SupportedVersions version;

    private String indexItemTable;

    /**
     * A constructor which reads the envelope from the database. Using an old Quadtree layout.
     * 
     * @param id
     * @param qt
     * @param indexTable
     * @param jdbc
     * @param level
     * @throws IndexException
     *             if the node with given id could not be read from the db.
     */
    // public DBNode( String id, DBQuadtree<T> qt, String indexTable, JDBCConnection jdbc, int level
    // )
    // throws IndexException {
    // this( id, null, qt, indexTable, jdbc, level, SupportedVersions.ONE );
    // }
    /**
     * 
     * @param id
     * @param env
     * @param qt
     * @param indexTable
     * @param jdbc
     * @param level
     * @param version
     *            of the quadtree layout to use
     * @throws IndexException
     *             if the node with given id could not be read from the db.
     */
    public DBNode( String id, Envelope env, DBQuadtree<T> qt, String indexTable, JDBCConnection jdbc, int level,
                   SupportedVersions version ) throws IndexException {
        this.id = id;
        this.envelope = env;
        if ( jdbc == null ) {
            throw new InvalidParameterException( "The JDBCConnection reference parameter 'jdbc' may not be null." );
        }
        this.jdbc = jdbc;

        if ( qt == null ) {
            throw new InvalidParameterException( "The quadtree reference parameter 'qt' may not be null." );
        }
        this.qt = qt;

        if ( level < 1 ) {
            level = 1;
        }
        this.level = level;
        if ( indexTable == null || "".equals( indexTable.trim() ) ) {
            throw new InvalidParameterException(
                                                 "The Table reference String 'indexTable' may neither be null nor an empty string." );
        }
        this.indexTable = indexTable.trim();
        this.indexItemTable = this.indexTable + "_ITEM ";
        this.version = version;
        if ( !loadNodeFromDB() ) {
            addNodeToDB();
        }
        for ( int i = 0; i < fk_subnode.length; ++i ) {
            if ( fk_subnode[i] == null || "null".equalsIgnoreCase( fk_subnode[i].trim() ) ) {
                fk_subnode[i] = "";
            }
        }
        qt.addToCache( this );
    }

    /**
     * A constructor which reads the envelope from the database.
     * 
     * @param id
     * @param qt
     * @param indexTable
     * @param jdbc
     * @param level
     * @param version
     *            of the quadtree layout to use.
     * @throws IndexException
     *             if the node with given id could not be read from the db.
     */
    public DBNode( String id, DBQuadtree<T> qt, String indexTable, JDBCConnection jdbc, int level,
                   SupportedVersions version ) throws IndexException {
        this( id, null, qt, indexTable, jdbc, level, version );
    }

    public boolean insert( T itemKey, Envelope itemEnv )
                            throws IndexException {
        if ( version == SupportedVersions.ONE ) {
            return insertWithLayoutOne( itemKey, itemEnv );
        } else if ( version == SupportedVersions.TWO ) {
            return insertWithLayoutTwo( itemKey, itemEnv );
        }
        return false;
    }

    public void deleteRange( Envelope envelope ) {
        if ( level == qt.getDepth() ) {
            // TODO delete a range from the bottomlevel
        } else {
            // TODO delete a range smaller then the depth
        }
    }

    /**
     * @throws UnsupportedOperationException
     *             if the version of this quadtree(node) is not 2.0.0 or higher.
     */
    public boolean delete( T itemKey, Envelope itemEnv )
                            throws IndexException {
        Connection dbConnection = null;
        DBConnectionPool pool = null;
        try {
            if ( version != SupportedVersions.TWO ) {
                String msg = "Deleting of items is only supported for the quadtree structure with version '2.0.0' or higher";
                LOG.logError( msg );
                throw new UnsupportedOperationException( msg );
            }
            pool = DBConnectionPool.getInstance();
            dbConnection = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
            boolean result = deleteWithLayoutTwo( itemKey, itemEnv, null, dbConnection );

            return result;
        } catch ( DBPoolException e ) {
            throw new IndexException( e );
        } finally {
            try {
                if ( pool != null && dbConnection != null ) {
                    pool.releaseConnection( dbConnection, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(),
                                            jdbc.getPassword() );
                }
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not release the db Connection after deletion in the quadtree because: "
                              + e.getMessage() );
            }
        }

    }

    public boolean update( T itemKey, Envelope newBBox ) {
        // nottin yet
        return true;
    }

    public List<T> query( Envelope searchEnv, List<T> visitor, int currentLevel )
                            throws IndexException {
        if ( version == SupportedVersions.ONE ) {
            LOG.logDebug( "Performing query with layout 1" );
            queryWithLayoutOne( searchEnv, visitor, currentLevel );
        } else if ( version == SupportedVersions.TWO ) {
            LOG.logDebug( "Performing query with layout 2" );
            queryWithLayoutTwo( searchEnv, visitor );
        }
        return visitor;
    }

    public String getId() {
        return id;
    }

    /**
     * @return the envelope (bbox) of this dbNode.
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * creates a new node with current ID and envelope
     * 
     * @throws IndexException
     */
    private void addNodeToDB()
                            throws IndexException {
        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "INSERT INTO " ).append( indexTable );
            sb.append( " ( ID, MINX, MINY, MAXX , MAXY ) " );
            sb.append( "VALUES ( ?, ?, ?, ?, ? ) " );
            PreparedStatement stmt = con.prepareStatement( sb.toString() );
            stmt.setString( 1, id );
            stmt.setFloat( 2, (float) envelope.getMin().getX() );
            stmt.setFloat( 3, (float) envelope.getMin().getY() );
            stmt.setFloat( 4, (float) envelope.getMax().getX() );
            stmt.setFloat( 5, (float) envelope.getMax().getY() );
            stmt.execute();
            stmt.close();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new IndexException( "could not create node definition at database", e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * assignes an item to a node by creating a new row in the JT_QTNODE_ITEM table
     * 
     */
    private void assignItem( T itemKey )
                            throws IndexException {
        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "INSERT INTO " ).append( indexItemTable );
            sb.append( "( FK_QTNODE, FK_ITEM ) " ).append( "VALUES ( ?, ? ) " );
            PreparedStatement stmt = con.prepareStatement( sb.toString() );
            stmt.setString( 1, id );
            if ( itemKey instanceof Integer ) {
                stmt.setInt( 2, ( (Integer) itemKey ).intValue() );
            } else {
                stmt.setString( 2, itemKey.toString() );
            }
            stmt.execute();
            stmt.close();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new IndexException( "could not create node definition at database", e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * This method inserts the given item into the quadtree. The difference between layout ONE and TWO is, that in
     * version TWO all nodes which lie inside the item's bbox possess a reference to the item, allthough the idx_table
     * will be larger, the retrieval of multiple items inside a requested bbox will be a lot faster.
     * 
     * @param itemKey
     *            to be inserted
     * @param itemEnv
     *            bbox of the item to be inserted.
     * @return true if the item was successfully inserted into the quadtree false otherwise.
     * @throws IndexException
     *             if the item could not be inserted because of an <code>db</code> or <code>sql</code> error.
     */
    private boolean insertWithLayoutTwo( T itemKey, Envelope itemEnv )
                            throws IndexException {
        if ( level != qt.getDepth() ) {
            if ( !envelope.intersects( itemEnv ) ) {
                String msg = "The item's envelope: " + itemEnv
                             + " does not intersect with the quadtrees' boundinbox envelope: " + envelope;
                LOG.logError( msg );
                throw new IndexException( msg );
            }
            // split the envelope of this node into four equal sized quarters
            Envelope[] envs = split();
            boolean needsUpdate = false;
            boolean nodeInserted = false;
            for ( int i = 0; i < envs.length; i++ ) {
                if ( envs[i].intersects( itemEnv ) ) {
                    // check which subnodes are intersected by the
                    // items envelope; only these nodes are considered for futher processing
                    if ( "".equals( fk_subnode[i].trim() ) ) {
                        needsUpdate = true;
                        fk_subnode[i] = id + '_' + i;
                    }
                    DBNode<T> node = qt.getFromCache( fk_subnode[i] );
                    if ( node == null ) {
                        node = new DBNode<T>( fk_subnode[i], envs[i], qt, indexTable, jdbc, level + 1, version );
                    }
                    nodeInserted = node.insertWithLayoutTwo( itemKey, itemEnv );
                }
            }
            // if any child node inserted the item, the parent must know about it too, this enhances
            // the speed of area
            // querying.
            if ( nodeInserted ) {
                assignItem( itemKey );
            }
            if ( needsUpdate ) {
                updateNodeInDB();
            }
            return nodeInserted;
        }
        assignItem( itemKey );
        return true;
    }

    /**
     * @param itemEnv
     *            the items envelope
     * @return true if this nodes envelope lies completely inside the items envelope.
     */
    private boolean liesWithIn( Envelope itemEnv ) {
        Position minThis = envelope.getMin();
        Position maxThis = envelope.getMax();
        Position minThat = itemEnv.getMin();
        Position maxThat = itemEnv.getMax();
        return ( minThis.getX() >= minThat.getX() && maxThis.getX() <= maxThat.getX()
                 && minThis.getY() > minThat.getY() && maxThis.getY() < maxThat.getY() );

    }

    private boolean insertWithLayoutOne( T itemKey, Envelope itemEnv )
                            throws IndexException {
        if ( level != qt.getDepth() ) {
            if ( !envelope.intersects( itemEnv ) ) {
                String msg = "The item's envelope: " + itemEnv
                             + " does not intersect with the quadtrees' boundinbox envelope: " + envelope;
                LOG.logError( msg );
                throw new IndexException( msg );
            }
            // split the envelope of this node into four equal sized quarters
            Envelope[] envs = split();
            boolean needsUpdate = false;
            int numberOfInsertedSons = 0;
            for ( int i = 0; i < envs.length; i++ ) {
                if ( envs[i].intersects( itemEnv ) ) {
                    numberOfInsertedSons++;
                    // check which subnodes are intersected by the
                    // items envelope; only these nodes are considered for futher processing
                    if ( "".equals( fk_subnode[i].trim() ) ) {
                        needsUpdate = true;
                        fk_subnode[i] = id + '_' + i;
                    }
                    DBNode<T> node = qt.getFromCache( fk_subnode[i] );
                    if ( node == null ) {
                        node = new DBNode<T>( fk_subnode[i], envs[i], qt, indexTable, jdbc, level + 1, version );
                    }
                    node.insertWithLayoutOne( itemKey, itemEnv );
                }
            }
            if ( numberOfInsertedSons == 4 ) {
                assignItem( itemKey );
            }
            if ( needsUpdate ) {
                updateNodeInDB();
            }
        } else {
            assignItem( itemKey );
        }
        return true;
    }

    /**
     * The enhancement of the second layout is, that every node knows about the items inserted in it's sons. This
     * information can be used if the bbox of this node lies totally within the requested area, all items of the sons of
     * this node are added automatically.
     * 
     * @param searchEnv
     *            the area to get all items for.
     * @param visitor
     *            the list inwhich the items keys will be added.
     * @throws IndexException
     *             if a connection to the db failed.
     */
    private void queryWithLayoutTwo( Envelope searchEnv, List<T> visitor )
                            throws IndexException {
        if ( liesWithIn( searchEnv ) || level == qt.getDepth() ) {
            getAssignedItems( visitor );
        } else {
            Envelope[] envs = split();
            for ( int i = 0; i < envs.length; i++ ) {
                if ( !"".equals( fk_subnode[i] ) && envs[i].intersects( searchEnv ) ) {
                    // check which subnodes are intersected by the
                    // items envelope; just this nodes
                    // are considered for futher processing
                    DBNode<T> node = new DBNode<T>( fk_subnode[i], envs[i], qt, indexTable, jdbc, level + 1, version );
                    node.queryWithLayoutTwo( searchEnv, visitor );
                }
            }
        }

    }

    private void queryWithLayoutOne( Envelope searchEnv, List<T> visitor, int level )
                            throws IndexException {
        /*
         * if ( level == qt.getDepth() || (searchEnv.getWidth() > envelope.getWidth() || searchEnv.getHeight() >
         * envelope.getHeight()) ) { addAssignedItems( visitor ); } else {
         */
        getAssignedItems( visitor );
        if ( level != qt.getDepth() ) {
            Envelope[] envs = split();
            for ( int i = 0; i < envs.length; i++ ) {
                if ( !"".equals( fk_subnode[i] ) && envs[i].intersects( searchEnv ) ) {
                    // check which subnodes are intersected by the
                    // items envelope; just this nodes
                    // are considered for futher processing
                    DBNode<T> node = new DBNode<T>( fk_subnode[i], envs[i], qt, indexTable, jdbc, level + 1, version );
                    node.queryWithLayoutOne( searchEnv, visitor, level + 1 );
                }
            }
        }
    }

    /**
     * The delete method which can handle the layout with version two.
     * 
     * @param itemKey
     *            the key to be deleted.
     * @param itemEnv
     *            the bbox of the item.
     * @param parent
     *            the parent node of this qt-node or <code>null</code> if this is the root node.
     * @throws IndexException
     *             if somehow the connection to the db is lost or an error occurred while executing the deletion.
     */
    private boolean deleteWithLayoutTwo( T itemKey, Envelope itemEnv, DBNode<T> parent, Connection dbConnection )
                            throws IndexException {
        List<T> items = new ArrayList<T>();
        // first get all items assigned to this node, and remove the fitting item from the idx-item
        // join table.
        getAssignedItems( items );
        if ( items.contains( itemKey ) ) {
            if ( deleteItemFromDB( itemKey, dbConnection ) ) {
                items.remove( itemKey );
            }
        }
        if ( level != qt.getDepth() ) {
            Envelope[] envs = split();
            for ( int i = 0; i < fk_subnode.length; ++i ) {
                if ( !"".equals( fk_subnode[i] ) && envs[i].intersects( itemEnv ) ) {
                    DBNode<T> node = new DBNode<T>( fk_subnode[i], envs[i], qt, indexTable, jdbc, level + 1, version );
                    node.deleteWithLayoutTwo( itemKey, itemEnv, this, dbConnection );
                }
            }
        }
        // If this is not the root of the quadtree, delete this node from the parent if it is a leaf
        // and has no items.
        if ( parent != null ) {
            if ( items.isEmpty() && !hasSons() ) {
                if ( deletNodeFromDB( dbConnection ) ) {
                    if ( parent.deleteSon( id, dbConnection ) ) {
                        qt.addToCache( parent );
                        qt.removeFromCache( this );
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param dbConnection
     *            a live connection to the database
     * @return true if the node has been successfully removed from the database.
     */
    private boolean deletNodeFromDB( Connection dbConnection ) {
        if ( hasSons() ) {
            LOG.logError( "Trying to delete a node (with id: '" + id + "') which still has sons, this may not happen!" );
            return false;
        }
        StringBuilder deleteStatement = new StringBuilder( 400 );
        deleteStatement.append( "DELETE FROM " ).append( indexTable );
        deleteStatement.append( " WHERE ID = '" ).append( id ).append( "'" );
        LOG.logDebug( "Trying to delete dbnode with id='" + id + "'. The sql statement is as follows:\n"
                      + deleteStatement.toString() );
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = dbConnection.createStatement();
            rs = stmt.executeQuery( deleteStatement.toString() );
        } catch ( SQLException e ) {
            LOG.logDebug( "Could not delete the dbNode with id='" + id + "' because: " + e.getMessage() );
            return false;
        } finally {
            try {
                if ( rs != null ) {
                    rs.close();
                }
                if ( stmt != null ) {
                    stmt.close();
                }
            } catch ( SQLException e ) {
                LOG.logDebug( "An error occurred while trying to close statement and/or resultset because: "
                              + e.getMessage() );
            }

        }
        return true;
    }

    /**
     * @return true if this node has a son (e.g. one of the fk_subnodes is not empty ).
     */
    private boolean hasSons() {
        return !( "".equals( fk_subnode[0] ) && "".equals( fk_subnode[1] ) && "".equals( fk_subnode[2] ) && "".equals( fk_subnode[3] ) );

    }

    /**
     * Deletes the specifies item from the quadtree, e.g. removing it from the idx_table_item join table, where the
     * FK_QTNODE equals this.id and the FK_ITEM = itemKey.
     * 
     * @param itemKey
     *            to be deleted
     * @param dbConnection
     *            used to execute the query.
     * @return true if the item was deleted from the database, false otherwhise.
     */
    private boolean deleteItemFromDB( T itemKey, Connection dbConnection ) {
        if ( itemKey == null ) {
            LOG.logDebug( "Trying to delete an itemkey which is null, this may not be (current node id='" + id + "')" );
            return false;
        }
        StringBuilder delItemStatement = new StringBuilder( 400 );
        delItemStatement.append( "DELETE FROM " ).append( indexItemTable );
        delItemStatement.append( " WHERE FK_QTNODE = '" ).append( id ).append( "'" );
        delItemStatement.append( " AND FK_ITEM = " );
        if ( itemKey instanceof String ) {
            delItemStatement.append( "'" );
        }
        delItemStatement.append( itemKey );
        if ( itemKey instanceof String ) {
            delItemStatement.append( "'" );
        }
        LOG.logDebug( "Trying to delete item with key='" + itemKey + "' from node with id='" + id
                      + "'. The sql statement is as follows:\n" + delItemStatement.toString() );
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = dbConnection.createStatement();
            rs = stmt.executeQuery( delItemStatement.toString() );
        } catch ( SQLException e ) {
            LOG.logDebug( "Could not delete the item with key='" + itemKey + "' from node with id='" + id
                          + "' because: " + e.getMessage() );
            return false;
        } finally {
            try {
                if ( rs != null ) {
                    rs.close();
                }
                if ( stmt != null ) {
                    stmt.close();
                }
            } catch ( SQLException e ) {
                LOG.logDebug( "An error occurred while trying to close statement and/or resultset because: "
                              + e.getMessage() );
            }

        }

        return true;

    }

    /**
     * Deletes a son from the database, e.g. perform an update to null on the idx_table. And if successfull also sets
     * the fk_subnode to an empty string.
     * 
     * @param sonsID
     *            which must be set to null.
     * @param dbConnection
     *            which will be used to perform the query.
     * @return true if the son was deleted.
     */
    private boolean deleteSon( String sonsID, Connection dbConnection ) {
        if ( sonsID == null || "".equals( sonsID.trim() ) ) {
            LOG.logDebug( "Trying to delete a son with an id which is null, this may not be (parent node id='" + id
                          + "')" );
            return false;
        }
        for ( int i = 0; i < fk_subnode.length; ++i ) {
            if ( fk_subnode[i].trim().equals( sonsID.trim() ) ) {
                StringBuilder delSonStatement = new StringBuilder( 400 );
                delSonStatement.append( "UPDATE " ).append( indexTable );
                delSonStatement.append( " SET FK_SUBNODE" ).append( ( i + 1 ) ).append( " = 'null'" );
                delSonStatement.append( " WHERE ID = '" ).append( id ).append( "'" );

                LOG.logDebug( "Trying to delete son with id='" + sonsID + "' from (parent) node with id='" + this.id
                              + "'. The sql statement is as follows:\n" + delSonStatement.toString() );
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = dbConnection.createStatement();
                    rs = stmt.executeQuery( delSonStatement.toString() );
                } catch ( SQLException e ) {
                    LOG.logDebug( "Could not delete son with id='" + sonsID + "' from (parent) node with id='"
                                  + this.id + "' because: " + e.getMessage() );
                    return false;
                } finally {
                    try {
                        if ( rs != null ) {
                            rs.close();
                        }
                        if ( stmt != null ) {
                            stmt.close();
                        }
                    } catch ( SQLException e ) {
                        LOG.logDebug( "An error occurred while trying to close statement and/or resultset because: "
                                      + e.getMessage() );
                    }

                }
                fk_subnode[i] = "";
                return true;
            }

        }

        LOG.logDebug( "It seems this (parent) node with id='" + id + "' has no son with id='" + sonsID
                      + "', all sons of this node  are:\n " + "- fk_subnode[0]='" + fk_subnode[0] + "'\n "
                      + "- fk_subnode[1]='" + fk_subnode[1] + "'\n " + "- fk_subnode[2]='" + fk_subnode[2] + "'\n "
                      + "- fk_subnode[3]='" + fk_subnode[3] + "'" );
        return false;
    }

    /**
     * load all Node parameters from the database.
     * 
     * @return true if a node with current ID is already available from the database and the parameters have been read
     *         successfully.
     * 
     */
    private boolean loadNodeFromDB()
                            throws IndexException {
        Connection con = null;
        DBConnectionPool pool = null;
        boolean available = true;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "Select * from " ).append( indexTable );
            sb.append( " where ID = '" ).append( id ).append( "'" );

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( sb.toString() );
            if ( rs.next() ) {
                double minx = rs.getFloat( "MINX" );
                double miny = rs.getFloat( "MINY" );
                double maxx = rs.getFloat( "MAXX" );
                double maxy = rs.getFloat( "MAXY" );
                envelope = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, null );
                fk_subnode[0] = rs.getString( "FK_SUBNODE1" );
                fk_subnode[1] = rs.getString( "FK_SUBNODE2" );
                fk_subnode[2] = rs.getString( "FK_SUBNODE3" );
                fk_subnode[3] = rs.getString( "FK_SUBNODE4" );
            } else {
                available = false;
            }
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new IndexException( "could not load node definition from database", e );
        } finally {
            try {
                if ( con != null && pool != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }
        return available;
    }

    /**
     * updates the database representation of the current node
     * 
     * @throws IndexException
     */
    private void updateNodeInDB()
                            throws IndexException {
        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 250 );
            sb.append( "UPDATE " ).append( indexTable ).append( " set " );
            boolean submit = false;
            for ( int i = 0; i < fk_subnode.length; i++ ) {
                if ( !"".equals( fk_subnode[i] ) ) {
                    sb.append( " FK_SUBNODE" ).append( i + 1 ).append( "='" );
                    sb.append( fk_subnode[i] ).append( "' ," );
                    submit = true;
                }
            }
            if ( submit ) {
                // just execute update if at least one sub node != null
                sb = new StringBuilder( sb.substring( 0, sb.length() - 1 ) );
                sb.append( " where ID = '" ).append( id ).append( "'" );
                Statement stmt = con.createStatement();
                stmt.execute( sb.toString() );
                stmt.close();
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new IndexException( "could not update node definition at database " + "for node: " + id, e );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }
        qt.addToCache( this );
    }

    /**
     * Get the item (IDs) assigned to this node and stores them in the given list
     * 
     * @param visitor
     * @throws IndexException
     */
    private void getAssignedItems( List<T> visitor )
                            throws IndexException {

        Connection con = null;
        DBConnectionPool pool = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 100 );
            sb.append( "SELECT DISTINCT FK_ITEM from " ).append( indexTable ).append( "_ITEM" );
            sb.append( " where " ).append( "FK_QTNODE = '" ).append( id ).append( "'" );
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( sb.toString() );

            while ( rs.next() ) {
                Object result = rs.getObject( 1 );
                if ( result != null ) {
                    T s = (T) result;
                    if ( !visitor.contains( s ) ) {
                        visitor.add( s );
                    }
                } else {
                    LOG.logDebug( "Found a node (id='" + id + "') with a null value." );
                }
            }
            stmt.close();
        } catch ( DBPoolException e ) {
            throw new IndexException( "Database QuadTree could not acquire sons of a node because: " + e.getMessage() );
        } catch ( SQLException e ) {
            throw new IndexException( "Database QuadTree could not acquire sons of a node because: " + e.getMessage() );
        } finally {
            try {
                if ( pool != null && con != null ) {
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                }
            } catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }
    }

    private Envelope[] split() {
        Envelope[] envs = new Envelope[4];
        double nW = envelope.getWidth() / 2d;
        double nH = envelope.getHeight() / 2d;

        envs[0] = GeometryFactory.createEnvelope( envelope.getMin().getX(), envelope.getMin().getY(),
                                                  envelope.getMin().getX() + nW, envelope.getMin().getY() + nH, null );
        envs[1] = GeometryFactory.createEnvelope( envelope.getMin().getX() + nW, envelope.getMin().getY(),
                                                  envelope.getMin().getX() + ( 2 * nW ), envelope.getMin().getY() + nH,
                                                  null );
        envs[2] = GeometryFactory.createEnvelope( envelope.getMin().getX() + nW, envelope.getMin().getY() + nH,
                                                  envelope.getMin().getX() + ( 2 * nW ), envelope.getMin().getY()
                                                                                         + ( 2 * nH ), null );
        envs[3] = GeometryFactory.createEnvelope( envelope.getMin().getX(), envelope.getMin().getY() + nH,
                                                  envelope.getMin().getX() + nW, envelope.getMin().getY() + ( 2 * nH ),
                                                  null );

        return envs;
    }

    // /**
    // * @param itemKey
    // * the key of the item to get the bbox for.
    // * @return the envelope of item with itemKey, currently stored in the quadtree.
    // */
    // public Envelope getBoundingBoxForItem( T itemKey ) {
    // return null;
    // }

}
