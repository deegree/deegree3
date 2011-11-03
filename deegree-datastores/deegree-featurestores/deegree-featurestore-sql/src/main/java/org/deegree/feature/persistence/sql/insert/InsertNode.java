//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.sql.insert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.jdbc.TransactionRow;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.sql.ParticleConversion;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.id.SequenceIDGenerator;
import org.deegree.feature.persistence.sql.id.UUIDGenerator;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.protocol.wfs.transaction.IDGenMode;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TransactionRow} that can not be inserted until the values for the foreign keys are known.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class InsertNode extends TransactionRow {

    private static Logger LOG = LoggerFactory.getLogger( InsertNode.class );

    private final SQLDialect dialect;

    private final Connection conn;

    // parent rows
    private Map<InsertNode, InsertRowReference> parentToRef = new HashMap<InsertNode, InsertRowReference>();

    // key columns that will only be generated on actual insert (AutoIDGenerator)
    private List<String> autoGenColumns = new ArrayList<String>();

    private InsertFID fid;

    InsertNode( TableName table, SQLDialect dialect, Connection conn,
                Map<SQLIdentifier, IDGenerator> keyColumnToGenerator ) throws FeatureStoreException {

        super( table );
        this.dialect = dialect;
        this.conn = conn;
        generateImmediateKeys( keyColumnToGenerator );
    }

    void assignFeatureType( SQLFeatureStore fs, Feature feature, FeatureTypeMapping ftMapping, InsertFID fid,
                            IDGenMode idGenMode )
                            throws FeatureStoreException {

        this.table = ftMapping.getFtTable();
        this.fid = fid;
        fid.setFIDMapping( ftMapping.getFidMapping() );

        switch ( idGenMode ) {
        case GENERATE_NEW: {
            Map<SQLIdentifier, IDGenerator> keyColumnToGenerator = new HashMap<SQLIdentifier, IDGenerator>();
            SQLIdentifier fidColumn = new SQLIdentifier( ftMapping.getFidMapping().getColumn() );
            keyColumnToGenerator.put( fidColumn, ftMapping.getFidMapping().getIdGenerator() );

            // hack to cope with perverted deegree 2 schemas (e.g. IMRO 2008)
            // TODO get rid of this
            for ( Mapping mapping : ftMapping.getMappings() ) {
                List<TableJoin> joinedTables = mapping.getJoinedTable();
                if ( joinedTables != null && !joinedTables.isEmpty() ) {
                    TableJoin join = joinedTables.get( 0 );
                    if ( !join.getFromColumns().get( 0 ).equals( fidColumn ) ) {
                        keyColumnToGenerator.put( join.getFromColumns().get( 0 ),
                                                  ftMapping.getFidMapping().getIdGenerator() );
                        // mega hack
                        join.getKeyColumnToGenerator().remove( new SQLIdentifier( "id" ) );
                    }
                }
            }

            generateImmediateKeys( keyColumnToGenerator );

            if ( get( fidColumn ) != null ) {
                fid.assign( this );
            }
            break;
        }
        case USE_EXISTING: {
            preInsertUseExisting( fs, feature, fid, ftMapping.getFidMapping() );
            fid.assign( this );
            break;
        }
        case REPLACE_DUPLICATE: {
            break;
        }
        }
    }

    private void preInsertUseExisting( SQLFeatureStore fs, Feature feature, InsertFID fid, FIDMapping fidMapping )
                            throws FeatureStoreException {

        if ( fid.getOriginalId() == null || fid.getOriginalId().isEmpty() ) {
            String msg = "Cannot insert features without id and id generation mode 'UseExisting'.";
            throw new FeatureStoreException( msg );
        }
        String[] idKernels = null;
        try {
            IdAnalysis analysis = fs.getSchema().analyzeId( fid.getOriginalId() );
            idKernels = analysis.getIdKernels();
            if ( analysis.getFeatureType() != feature.getType() ) {
                String msg = "Cannot insert feature with id '" + fid.getOriginalId()
                             + "' and id generation mode 'UseExisting'. "
                             + "Id does not match configured feature id pattern for feature type '"
                             + feature.getType().getName() + "'.";
                throw new FeatureStoreException( msg );
            }
        } catch ( IllegalArgumentException e ) {
            String msg = "Cannot insert feature with id '" + fid.getOriginalId()
                         + "' and id generation mode 'UseExisting'. "
                         + "Id does not match configured feature id pattern.";
            throw new FeatureStoreException( msg );
        }
        for ( int i = 0; i < fidMapping.getColumns().size(); i++ ) {
            Pair<SQLIdentifier, BaseType> idColumn = fidMapping.getColumns().get( i );
            // TODO mapping to non-string columns
            Object value = idKernels[i];
            addPreparedArgument( idColumn.getFirst(), value );
        }
    }

    private void generateImmediateKeys( Map<SQLIdentifier, IDGenerator> keyColumnToGenerator )
                            throws FeatureStoreException {
        if ( keyColumnToGenerator != null ) {
            for ( SQLIdentifier autoKeyColumn : keyColumnToGenerator.keySet() ) {
                IDGenerator idGenerator = keyColumnToGenerator.get( autoKeyColumn );
                if ( idGenerator instanceof SequenceIDGenerator ) {
                    int seqVal = getSequenceNextVal( ( (SequenceIDGenerator) idGenerator ).getSequence() );
                    LOG.debug( "Got key value for column '" + autoKeyColumn.getName() + "' from sequence: " + seqVal );
                    addPreparedArgument( autoKeyColumn, seqVal );
                } else if ( idGenerator instanceof UUIDGenerator ) {
                    String uuid = UUID.randomUUID().toString();
                    LOG.debug( "Got key value for column '" + autoKeyColumn.getName() + "' from UUID: " + uuid );
                    addPreparedArgument( autoKeyColumn, uuid );
                } else if ( idGenerator instanceof AutoIDGenerator ) {
                    LOG.debug( "Adding key for column '" + autoKeyColumn.getName()
                               + "' to autogenerated keys: generated on insert by DB." );
                    autoGenColumns.add( autoKeyColumn.getName() );
                } else {
                    LOG.warn( "Unhandled ID generator: " + idGenerator.getClass().getName() );
                }
            }
        }
    }

    private int getSequenceNextVal( String sequenceName )
                            throws FeatureStoreException {
        String sql = dialect.getSelectSequenceNextVal( sequenceName );
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            LOG.debug( "Determing feature ID from db sequence: " + sql );
            rs = stmt.executeQuery( sql );
            if ( rs.next() ) {
                return rs.getInt( 1 );
            } else {
                String msg = "Error determining ID from db sequence. No value returned for: " + sql;
                throw new FeatureStoreException( msg );
            }
        } catch ( SQLException e ) {
            String msg = "Error determining ID from db sequence. No value returned for: " + sql;
            throw new FeatureStoreException( msg, e );
        } finally {
            JDBCUtils.close( rs, stmt, null, LOG );
        }
    }

    void addParent( InsertRowReference ref ) {
        parentToRef.put( ref.getRef(), ref );
    }

    public void removeParent( InsertNode parent, InsertFID fid ) {

        InsertRowReference row = parentToRef.get( parent );

        // propagate keys
        for ( int i = 0; i < row.getJoin().getFromColumns().size(); i++ ) {
            SQLIdentifier fromColumn = row.getJoin().getFromColumns().get( i );
            SQLIdentifier toColumn = row.getJoin().getToColumns().get( i );
            Object key = parent.get( fromColumn );
            if ( key == null ) {
                throw new IllegalArgumentException(
                                                    "Unable to resolve foreign key relation. Encountered NULL value for key column '"
                                                                            + fromColumn + "'." );
            }
            addPreparedArgument( toColumn, key );
        }

        if ( row.isHrefed( this ) && fid != null ) {
            addPreparedArgument( "href", "#" + fid.getNewId() );
        }

        parentToRef.remove( parent );
    }

    /**
     * Returns whether this {@link InsertNode} has uninserted parents, i.e. rows that provide keys which a necessary for
     * this row.
     * 
     * @return <code>true</code>, if this node has uninserted parents, <code>false</code> otherwise
     */
    boolean hasParents() {
        return !parentToRef.isEmpty();
    }

    /**
     * Performs the insertion and deals with autogenerated columns.
     * 
     * @param conn
     *            JDBC connection to use for the insertion, must not be <code>null</code>
     * @param propagateAutoGenColumns
     *            <code>true</code>, if auto generated key columns should be processed (and propagated),
     *            <code>false</code> otherwise
     * @throws SQLException
     * @throws FeatureStoreException
     */
    public void performInsert( Connection conn, boolean propagateAutoGenColumns, SQLDialect dialect )
                            throws SQLException, FeatureStoreException {

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Inserting: " + this );
        }

        String sql = getSql();
        PreparedStatement stmt = null;

        if ( autoGenColumns.isEmpty() || !propagateAutoGenColumns ) {
            stmt = conn.prepareStatement( sql );
        } else {
            stmt = conn.prepareStatement( sql, autoGenColumns.toArray( new String[autoGenColumns.size()] ) );
        }
        int columnId = 1;
        for ( Entry<SQLIdentifier, Object> entry : columnToObject.entrySet() ) {
            if ( entry.getValue() != null ) {
                LOG.debug( "- Argument " + entry.getKey() + " = " + entry.getValue() + " ("
                           + entry.getValue().getClass() + ")" );
                if ( entry.getValue() instanceof ParticleConversion<?> ) {
                    ParticleConversion<?> conversion = (ParticleConversion<?>) entry.getValue();
                    conversion.setParticle( stmt, columnId++ );
                } else {
                    stmt.setObject( columnId++, entry.getValue() );
                }
            } else {
                LOG.debug( "- Argument " + entry.getKey() + " = NULL" );
                stmt.setObject( columnId++, null );
            }
        }
        stmt.execute();

        if ( propagateAutoGenColumns && !autoGenColumns.isEmpty() ) {
            ResultSet rs = null;
            try {
                rs = stmt.getGeneratedKeys();
                if ( rs.next() ) {
                    for ( int i = 1; i <= autoGenColumns.size(); i++ ) {
                        String autogenColumn = autoGenColumns.get( i - 1 );
                        Object key = rs.getObject( i );
                        columnToObject.put( new SQLIdentifier( autogenColumn ), key );
                        LOG.debug( "Retrieved auto generated key: " + autogenColumn + "=" + key );
                    }
                }
            } finally {
                if ( rs != null ) {
                    rs.close();
                }
            }
            if ( fid != null ) {
                fid.assign( this );
            }
        }
        stmt.close();
    }

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder( "INSERT INTO " + table + "(" );
        boolean first = true;
        for ( SQLIdentifier column : columnToLiteral.keySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( column );
        }
        sql.append( ") VALUES(" );
        first = true;
        for ( Entry<SQLIdentifier, String> entry : columnToLiteral.entrySet() ) {
            if ( !first ) {
                sql.append( ',' );
            } else {
                first = false;
            }
            sql.append( entry.getValue() );
        }
        sql.append( ")" );
        return sql.toString();
    }

    @Override
    public String toString() {
        return getSql();
    }
}
