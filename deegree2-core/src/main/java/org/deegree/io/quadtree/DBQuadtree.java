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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 * @param <T>
 *            the datatype to be used as id
 *
 * @since 2.0
 */
public class DBQuadtree<T> implements Quadtree<T> {

    private static ILogger LOG = LoggerFactory.getLogger( DBQuadtree.class );

    private String fk_root;

    private int depth;

    private int id = 0;

    private String indexName = null;

    private JDBCConnection jdbc = null;

    private Map<String, DBNode<T>> nodeCache = new HashMap<String, DBNode<T>>( 10000 );

    private double accuracyX;

    private double accuracyY;

    private DBQuadtree.SupportedVersions version;

    private Connection con = null;

    private DBConnectionPool pool = null;

    /**
     * The <code>SupportedVersions</code> supported by this quatree
     *
     */
    public static enum SupportedVersions {
        /**
         * the old version or 1.0.0
         */
        ONE,
        /**
         * The new version or "2.0.0"
         */
        TWO
    }

    /**
     * Creates a Database with given version.
     *
     * @param id
     *            of the table which contains the features.
     * @param indexName
     *            this name will be used to create the table that stores the nodes of a specific
     *            quadtree
     * @param jdbc
     *            description of database connection
     * @param version
     *            of the quadtree, which is usefull for the determination of the layout, if null
     *            then the version is assumed to be unknown and the old layout is used.
     * @throws IndexException
     */
    public DBQuadtree( int id, String indexName, JDBCConnection jdbc, String version ) throws IndexException {
        this( id, indexName, jdbc, 0.0001, 0.0001, version );
    }

    /**
     * initializes a quadtree already existing in a database. New items will have a slightly larger
     * bbox with 0.0001 added to each boundary, this is usefull for point geometries.
     *
     * @param id
     *            of the table which contains the features.
     * @param indexName
     *            this name will be used to create the table that stores the nodes of a specific
     *            quadtree
     * @param jdbc
     *            description of database connection
     * @throws IndexException
     *             if the quadtree node with 'id' could not be read from the database.
     */
    public DBQuadtree( int id, String indexName, JDBCConnection jdbc ) throws IndexException {
        this( id, indexName, jdbc, 0.0001, 0.0001, "1.0.0" );
    }

    /**
     * initializes a quadtree already existing in a database
     *
     * @param id
     *            of the table which contains the features.
     * @param indexName
     *            this name will be used to create the table that stores the nodes of a specific
     *            quadtree
     * @param jdbc
     *            description of database connection
     * @param accuracyX
     * @param accuracyY
     * @param version
     *            of the quadtree, which is usefull for the determination of the layout, if null
     *            then the version is assumed to be "1.0.0" (the first version) and the old layout
     *            is used.
     * @throws IndexException
     *             if the quadtree node with 'id' could not be read from the database.
     */
    public DBQuadtree( int id, String indexName, JDBCConnection jdbc, double accuracyX, double accuracyY, String version )
                            throws IndexException {
        this.id = id;
        this.jdbc = jdbc;
        this.indexName = indexName;
        this.accuracyX = accuracyX;
        this.accuracyY = accuracyY;
        pool = DBConnectionPool.getInstance();
        try {
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        } catch ( DBPoolException e ) {
            String msg = "Could not acquire a database connection for the quadtree, no possibility to go on!";
            LOG.logError( msg + " Cause: " + e.getMessage() );
            throw new IndexException( msg, e );
        }

        if ( version == null || !"2.0.0".equals( version.trim() ) ) {
            this.version = SupportedVersions.ONE;
        } else {
            this.version = SupportedVersions.TWO;
        }
        readRootNodeId();
    }

    /**
     *
     * @param id
     * @return node
     */
    DBNode<T> getFromCache( String id ) {
        return nodeCache.get( id );
    }

    /**
     *
     * @param node
     */
    void addToCache( DBNode<T> node ) {
        nodeCache.put( node.getId(), node );
    }

    /**
     *
     * @param node
     */
    DBNode<T> removeFromCache( DBNode<T> node ) {
        return nodeCache.remove( node.getId() );
    }

    /**
     * inserts a new item into a quadtree
     *
     * @param item
     * @param envelope
     */
    public void insert( T item, Envelope envelope )
                            throws IndexException {
        DBNode<T> node = new DBNode<T>( fk_root, this, indexName, jdbc, 1, version );
        node.insert( item, envelope );
    }

    /**
     * @param item
     * @param point
     */
    public void insert( T item, Point point )
                            throws IndexException {
        DBNode<T> node = new DBNode<T>( fk_root, this, indexName, jdbc, 1, version );
        Envelope envelope = GeometryFactory.createEnvelope( point.getX() - accuracyX, point.getY() - accuracyY,
                                                            point.getX() + accuracyX, point.getY() + accuracyY, null );
        node.insert( item, envelope );
    }

    /**
     *
     * @param envelope
     * @return list a items intersecting with the passed envelope
     */
    public List<T> query( Envelope envelope )
                            throws IndexException {
        LOG.logDebug( "Performing query for envelope: " + envelope );
        // Thread.dumpStack();
        List<T> visitor = new ArrayList<T>( 1000 );
        DBNode<T> node = new DBNode<T>( fk_root, null, this, indexName, jdbc, 1, version );
        envelope = envelope.createIntersection( node.getEnvelope() );
        if ( envelope == null ) {
            LOG.logDebug( "Found no intersection with the root element of the quadtree, returning an emtpy feature collection." );
            return new ArrayList<T>();
        }
        return node.query( envelope, visitor, 1 );
    }

    /**
     * deletes an item from a quadtree
     *
     * @param item
     */
    public void deleteItem( T item )
                            throws IndexException {
        DBNode<T> root = new DBNode<T>( fk_root, this, indexName, jdbc, 1, version );
        root.delete( item, root.getEnvelope() );
    }

    /**
     * updates the envelope of an item
     *
     * @param item
     * @param newBBox
     */
    public void update( T item, Envelope newBBox )
                            throws IndexException {
        Node<T> root = new DBNode<T>( fk_root, this, indexName, jdbc, 1, version );
        root.update( item, newBBox );
    }

    public void deleteRange( Envelope envelope ) {
        throw new UnsupportedOperationException();
        // TODO
        // and even more magic
    }

    /**
     * @return depth of a quadtree
     */
    public int getDepth() {
        return depth;
    }

    /**
     * @return true if the db connection could be released to the pool, false otherwise.
     */
    public boolean releaseConnection() {
        try {
            pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        } catch ( DBPoolException e ) {
            LOG.logError( "Could not release database connection because: " + e.getMessage() );
            return false;
        }
        return true;
    }

    /**
     * reads the root node from the database
     *
     * @throws IndexException
     *             if the database could not be loaded or no quadtree rootnode was found.
     */
    private void readRootNodeId()
                            throws IndexException {

        try {
            StringBuffer sb = new StringBuffer( 200 );
            sb.append( "Select FK_ROOT, DEPTH from TAB_QUADTREE where ID = " );
            sb.append( id );

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( sb.toString() );
            if ( rs.next() ) {
                fk_root = rs.getString( "FK_ROOT" );
                depth = rs.getInt( "DEPTH" );
            } else {
                throw new IndexException( "Could not read FK_ROOT and DEPTH for Quadtree with ID" + id );
            }
            rs.close();
            stmt.close();
        } catch ( SQLException e ) {
            throw new IndexException( "Could not load quadtree definition from database because: " + e.getMessage() );
        }

    }

    /**
     * @return envelope of a quadtree's root node
     */
    public Envelope getRootBoundingBox()
                            throws IndexException {
        DBNode<T> root = new DBNode<T>( fk_root, this, indexName, jdbc, 1, version );
        return root.getEnvelope();
    }

}
