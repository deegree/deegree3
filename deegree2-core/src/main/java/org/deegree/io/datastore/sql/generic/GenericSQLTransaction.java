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

package org.deegree.io.datastore.sql.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.sql.AbstractSQLDatastore;
import org.deegree.io.datastore.sql.SQLDatastoreConfiguration;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.transaction.SQLTransaction;
import org.deegree.io.quadtree.DBQuadtree;
import org.deegree.io.quadtree.IndexException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;

/**
 * Special transaction implementation for the {@link GenericSQLDatastore}.
 * <p>
 * Please note that the quadtree management isn't finished yet.
 * </p><p>
 * What should work:
 * <ul>
 * <li>inserting of new features</li>
 * <li>deleting of features</li>
 * <li>updating features (unless the geometry property is changed)</li>
 * </ul>
 * </p><p>
 * What definitely won't work:
 * <ul>
 * <li>updating geometry properties will most probably break the index</li>
 * </ul>
 * </p>
 *
 * @see org.deegree.io.quadtree
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GenericSQLTransaction extends SQLTransaction {

    private static final ILogger LOG = LoggerFactory.getLogger( GenericSQLTransaction.class );

    private String indexName;

    /**
     * saves versioninfos about the different quadtrees.
     */
    public static HashMap<String, String> quadTreeVersionInfo = new HashMap<String, String>();

    /**
     *
     * @param ds
     * @param aliasGenerator
     * @param conn
     * @throws DatastoreException
     */
    GenericSQLTransaction( AbstractSQLDatastore ds, TableAliasGenerator aliasGenerator, Connection conn )
                            throws DatastoreException {
        super( ds, aliasGenerator, conn );
    }

    @Override
    public int performDelete( MappedFeatureType mappedFeatureType, Filter filter, String lockId )
                                                                                                 throws DatastoreException {
        assert !mappedFeatureType.isAbstract();

        if ( !mappedFeatureType.isDeletable() ) {
            String msg = Messages.getMessage( "DATASTORE_FT_NOT_DELETABLE", mappedFeatureType.getName() );
            throw new DatastoreException( msg );
        }
        // QualifiedName qName = mappedFeatureType.getName();
        // if ( qName == null ) {
        // LOG.logDebug( "The mappedfeauterType's qname is null, this cannot be!" );
        // }
        // Query q = Query.create( mappedFeatureType.getName(), filter );
        // if ( q == null ) {
        // LOG.logDebug( "The query created from the qname and filter is null, this cannot be!" );
        // }
        // FeatureCollection fc = null;
        // try {
        // fc = getDatastore().performQuery( q, new MappedFeatureType[] { mappedFeatureType } );
        // } catch ( UnknownCRSException e ) {
        // throw new DatastoreException( e );
        // }

        List<FeatureId> featureList = determineAffectedAndModifiableFIDs( mappedFeatureType, filter, lockId );

        SQLDatastoreConfiguration config = (SQLDatastoreConfiguration) getDatastore().getConfiguration();
        JDBCConnection jdbc = config.getJDBCConnection();
        String table = mappedFeatureType.getTable();
        String version = getQTVersion( table, jdbc );
        LOG.logDebug( "Found quadtree version: " + version );
        try {
            for ( FeatureId fID : featureList ) {
                int fk_index = loadIndexMetadata( jdbc, table );
                Object rawId = FeatureId.removeFIDPrefix( fID.getAsString(), mappedFeatureType.getGMLId() );
                if ( rawId instanceof Integer ) {
                    DBQuadtree<Integer> qt = null;
                    try {
                        qt = new DBQuadtree<Integer>( fk_index, indexName, jdbc, version );
                        qt.deleteItem( (Integer) rawId );
                    } finally {
                        if ( qt != null ) {
                            qt.releaseConnection();
                        }
                    }
                } else if ( rawId instanceof String ) {
                    DBQuadtree<String> qt = null;
                    try {
                        qt = new DBQuadtree<String>( fk_index, indexName, jdbc, version );
                        qt.deleteItem( (String) rawId );
                    } finally {
                        if ( qt != null ) {
                            qt.releaseConnection();
                        }
                    }
                }
            }
        } catch ( IndexException e ) {
            LOG.logError( e.getMessage(), e );
            throw new DatastoreException( e.getMessage(), e );
        }

        super.performDelete( mappedFeatureType, filter, lockId );

        return featureList.size();
    }

    @Override
    public List<FeatureId> performInsert( List<Feature> features )
                                                                  throws DatastoreException {
        List<FeatureId> fids = super.performInsert( features );

        // update index
        try {
            SQLDatastoreConfiguration config = (SQLDatastoreConfiguration) getDatastore().getConfiguration();
            JDBCConnection jdbc = config.getJDBCConnection();
            for ( int i = 0; i < features.size(); i++ ) {
                Envelope env = features.get( i ).getBoundedBy();
                if ( env != null ) {
                    MappedFeatureType mft = datastore.getFeatureType( features.get( i ).getFeatureType().getName() );

                    String table = mft.getTable();
                    String version = getQTVersion( table, jdbc );
                    int fk_index = loadIndexMetadata( jdbc, table );
                    Object rawId = FeatureId.removeFIDPrefix( fids.get( i ).getAsString(), mft.getGMLId() );
                    if ( rawId instanceof String ) {
                        DBQuadtree<String> qt = null;
                        try {
                            qt = new DBQuadtree<String>( fk_index, indexName, jdbc, version );
                            qt.insert( (String) rawId, env );
                        } finally {
                            if ( qt != null ) {
                                qt.releaseConnection();
                            }
                        }
                    } else if ( rawId instanceof Integer ) {
                        DBQuadtree<Integer> qt = null;
                        try {
                            qt = new DBQuadtree<Integer>( fk_index, indexName, jdbc, version );
                            qt.insert( (Integer) rawId, env );
                        } finally {
                            if ( qt != null ) {
                                qt.releaseConnection();
                            }
                        }

                    }
                }
            }
        } catch ( IndexException e ) {
            LOG.logError( e.getMessage(), e );
            throw new DatastoreException( e.getMessage(), e );
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
            throw new DatastoreException( e.getMessage(), e );
        }
        return fids;
    }

    /**
     * @param table
     *            to open a quadtree for.
     * @return the version of the quadtree used.
     */
    private String getQTVersion( String table, JDBCConnection jdbc ) {
        String version = "1.0.0";
        if ( quadTreeVersionInfo.containsKey( table ) && quadTreeVersionInfo.get( table ) != null ) {
            LOG.logDebug( "Retrieved the quatdree version info for table: " + table + " from cache." );
            version = quadTreeVersionInfo.get( table );
        } else {
            Connection con = null;
            DBConnectionPool pool = null;
            Statement stmt = null;
            ResultSet rs = null;
            pool = DBConnectionPool.getInstance();
            StringBuilder sb = new StringBuilder( 400 );
            sb.append( "SELECT fk_indextree FROM tab_deegree_idx WHERE " );
            sb.append( "column_name = 'geometry' AND " );
            sb.append( "table_name = '" ).append( table.toLowerCase() ).append( "'" );

            LOG.logDebug( "Get Index Metadata sql statement:\n", sb );
            try {
                con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                stmt = con.createStatement();
                rs = stmt.executeQuery( sb.toString() );
                String tableID = null;
                if ( rs.next() ) {
                    tableID = rs.getString( 1 );
                }
                if ( tableID != null ) {
                    sb = new StringBuilder( 400 );
                    sb.append( "SELECT * FROM tab_quadtree WHERE " );
                    sb.append( "fk_root = '" ).append( tableID.trim() ).append( "'" );
                    if ( rs != null ) {
                        rs.close();
                    }
                    if ( stmt != null ) {
                        stmt.close();
                    }
                    stmt = con.createStatement();
                    rs = stmt.executeQuery( sb.toString() );
                    if ( rs.next() ) {
                        boolean hasVersion = false;
                        ResultSetMetaData md = rs.getMetaData();
                        int numberOfColumns = md.getColumnCount();
                        LOG.logDebug ("Column count: " + numberOfColumns);
                        for ( int i = 1; i <= numberOfColumns && !hasVersion; i++ ) {
                            String tmp = md.getColumnName( i );
                            LOG.logDebug( "Found columnname: " + tmp );
                            if ( tmp != null ) {
                                if ( "version".equalsIgnoreCase( tmp.trim() ) ) {
                                    hasVersion = true;
                                    version = rs.getString( i );
                                    LOG.logDebug( "Found a version column, setting version to: " + rs.getString( i ) );
                                }
                            }
                        }
                        if ( !hasVersion ) {
                            try {
                                LOG.logInfo( "Found no Version Column in the TAB_QUADTREE table, assuming version 1.0.0, and adding the version column." );
                                if ( rs != null ) {
                                    rs.close();
                                }
                                if ( stmt != null ) {
                                    stmt.close();
                                }
                                stmt = con.createStatement();
                                rs = stmt.executeQuery( "ALTER TABLE TAB_QUADTREE ADD version VARCHAR(15)" );
                                rs.close();
                                stmt.close();
                            } catch ( SQLException e ) {
                                if ( rs != null ) {
                                    rs.close();
                                }
                                if ( stmt != null ) {
                                    stmt.close();
                                }
                                LOG.logError( "An error occurred while trying to insert a new 'version' column in the database: " + e.getMessage(),
                                              e );
                            }
                        }
                    }
                } else {
                    LOG.logError( "Could not find the foreign key (fk_root) of the table: '" + table
                                  + "'. Is your database set up correct?" );
                }
            } catch ( SQLException e ) {
                LOG.logError( "An error occurred while determening version of quadtree, therefore setting version to '1.0.0'. Errormessage: " + e.getMessage(),
                              e );
            } catch ( DBPoolException e ) {
                LOG.logError( "An error occurred while acquiring connection to the database to determine version of quadtree, therefore setting version to '1.0.0'. Errormessage: " + e.getMessage(),
                              e );
            } finally {
                quadTreeVersionInfo.put( table, version );
                try {
                    if ( rs != null ) {
                        rs.close();
                    }
                    if ( stmt != null ) {
                        stmt.close();
                    }
                    pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
                } catch ( SQLException e ) {
                    LOG.logError( "Could not close ResultSet or Statement because: " + e.getMessage() );
                } catch ( DBPoolException e ) {
                    LOG.logError( "Could not release connection because: " + e.getMessage() );
                }
            }
        }

        // } catch ( SQLException e ) {
        // String msg = e.getMessage();
        // if( msg != null && msg.contains( " )
        // }

        return version;
    }

    @Override
    public int performUpdate( MappedFeatureType mappedFeatureType, Feature replacementFeature, Filter filter,
                              String lockId )
                                             throws DatastoreException {
        int cnt = super.performUpdate( mappedFeatureType, replacementFeature, filter, lockId );

        // update index

        return cnt;
    }

    @Override
    public int performUpdate( MappedFeatureType mappedFeatureType, Map<PropertyPath, FeatureProperty> replacementProps,
                              Filter filter, String lockId )
                                                            throws DatastoreException {

        int cnt = super.performUpdate( mappedFeatureType, replacementProps, filter, lockId );

        // update index

        return cnt;
    }

    /**
     * loads the metadata of an Index from the TAB_DEEGREE_IDX table
     *
     * @param jdbc
     *            database connection information
     * @param table
     *            name of the table containing a featuretypes data
     *
     * @return FK to the index
     * @throws IndexException
     */
    private int loadIndexMetadata( JDBCConnection jdbc, String table )
                                                                      throws IndexException {
        int fk_indexTree = -1;
        Connection con = null;
        DBConnectionPool pool = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            pool = DBConnectionPool.getInstance();
            con = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );

            StringBuilder sb = new StringBuilder( 400 );
            sb.append( "Select INDEX_NAME, FK_INDEXTREE from TAB_DEEGREE_IDX where " );
            sb.append( "column_name = 'geometry' AND " );
            sb.append( "table_name = '" ).append( table.toLowerCase() ).append( "'" );

            LOG.logDebug( "Get Index Metadata sql statement:\n", sb );

            stmt = con.createStatement();
            rs = stmt.executeQuery( sb.toString() );

            if ( rs.next() ) {
                indexName = rs.getString( "INDEX_NAME" );
                fk_indexTree = rs.getInt( "FK_INDEXTREE" );
            } else {
                throw new IndexException( "could not read index metadata" );
            }
        } catch ( DBPoolException e ) {
            throw new IndexException( "could not load quadtree definition from database", e );
        } catch ( SQLException e ) {
            throw new IndexException( "could not load quadtree definition from database", e );
        } finally {
            try {
                if ( rs != null ) {
                    rs.close();
                }
                if ( stmt != null ) {
                    stmt.close();
                }
                pool.releaseConnection( con, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
            } catch ( SQLException e ) {
                LOG.logError( "Could not close Set or Statement because: " + e.getMessage() );
            } catch ( DBPoolException e ) {
                LOG.logError( "Could not reslease connection because: " + e.getMessage() );
            }
        }
        return fk_indexTree;
    }
}
