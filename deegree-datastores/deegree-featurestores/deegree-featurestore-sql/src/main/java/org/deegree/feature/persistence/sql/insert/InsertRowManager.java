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

import static java.util.Collections.EMPTY_LIST;
import static org.deegree.gml.GMLVersion.GML_32;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTransaction;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.id.SequenceIDGenerator;
import org.deegree.feature.persistence.sql.id.UUIDGenerator;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.property.Property;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.FeatureReference;
import org.deegree.protocol.wfs.transaction.IDGenMode;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs inserts in courtesy of the {@link SQLFeatureStoreTransaction}.
 * <p>
 * The strategy aims for:
 * <ul>
 * <li>Streaming/low memory footprint</li>
 * <li>Feature references must not be resolved</li>
 * <li>Usability for complex structures / mappings</li>
 * <li>Auto-generated columns (feature ids)</li>
 * <li>Forward xlink references</li>
 * <li>Backward xlink references</li>
 * </ul>
 * 
 * TODO strategy for cyclic foreign keys (at least detection)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InsertRowManager {

    private static Logger LOG = LoggerFactory.getLogger( InsertRowManager.class );

    private final SQLFeatureStore fs;

    private final SQLDialect dialect;

    private final Connection conn;

    private final GMLVersion gmlVersion;

    private final Map<String, InsertFID> origIdToInsertFID = new HashMap<String, InsertFID>();

    private final Map<InsertFID, ChildInsertRow> fidToFeatureRow = new HashMap<InsertFID, ChildInsertRow>();

    private final Map<ChildInsertRow, InsertFID> delayedFeatureRowToFID = new HashMap<ChildInsertRow, InsertFID>();

    private final Map<InsertRow, List<ChildInsertRow>> rowToChildRows = new HashMap<InsertRow, List<ChildInsertRow>>();

    public InsertRowManager( SQLFeatureStore fs, Connection conn ) {
        this.fs = fs;
        this.dialect = fs.getDialect();
        this.conn = conn;
        this.gmlVersion = fs.getSchema().getGMLSchema() == null ? GML_32 : fs.getSchema().getGMLSchema().getVersion();
    }

    /**
     * Inserts the specified feature (relational mode).
     * 
     * @param f
     *            feature instance to be inserted, must not be <code>null</code>
     * @param ftMapping
     *            mapping of the corresponding feature type, must not be <code>null</code>
     * @param idGenMode
     *            feature id generation mode, must not be <code>null</code>
     * @return id of the stored feature, never <code>null</code>
     */
    public InsertFID insertFeature( Feature feature, FeatureTypeMapping ftMapping, IDGenMode mode )
                            throws SQLException, FeatureStoreException, FilterEvaluationException {

        InsertFID fid = getInsertFID( feature );
        ChildInsertRow featureRow = getFeatureRow( fid );
        featureRow.setTable( ftMapping.getFtTable() );
        FIDMapping fidMapping = ftMapping.getFidMapping();
        fid.setFIDMapping( fidMapping );

        // pre-INSERT fid assignment
        switch ( mode ) {
        case GENERATE_NEW: {
            preInsertGenerateNew( feature, fid, featureRow, fidMapping );
            break;
        }
        case REPLACE_DUPLICATE: {
            throw new UnsupportedOperationException( "ReplaceDuplicate is not implemented yet." );
        }
        case USE_EXISTING: {
            preInsertUseExisting( feature, fid, featureRow, fidMapping );
            break;
        }
        }

        for ( Mapping particleMapping : ftMapping.getMappings() ) {
            insertParticles( feature, particleMapping, featureRow );
        }

        LOG.debug( "Built feature row {}", featureRow );
        insertRow( featureRow );
        return fid;
    }

    private void preInsertGenerateNew( Feature feature, InsertFID fid, ChildInsertRow featureRow, FIDMapping fidMapping )
                            throws FeatureStoreException {

        IDGenerator gen = fidMapping.getIdGenerator();
        if ( gen instanceof AutoIDGenerator ) {
            featureRow.setAutoGenColumn( new SQLIdentifier( fidMapping.getColumn() ) );
        } else if ( gen instanceof UUIDGenerator ) {
            String uuid = UUID.randomUUID().toString();
            featureRow.addPreparedArgument( fidMapping.getColumn(), uuid );
            fid.assign( featureRow );
        } else {
            String sql = dialect.getSelectSequenceNextVal( ( (SequenceIDGenerator) gen ).getSequence() );
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.createStatement();
                LOG.debug( "Determing feature ID from db sequence: " + sql );
                rs = stmt.executeQuery( sql );
                if ( rs.next() ) {
                    int idKernel = rs.getInt( 1 );
                    featureRow.addPreparedArgument( fidMapping.getColumn(), idKernel );
                    fid.assign( featureRow );
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
    }

    private void preInsertUseExisting( Feature feature, InsertFID fid, ChildInsertRow featureRow, FIDMapping fidMapping )
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
            featureRow.addPreparedArgument( idColumn.getFirst(), value );
        }
        fid.assign( featureRow );
    }

    private InsertFID getInsertFID( Feature f ) {
        String fid = f.getId();
        if ( fid == null ) {
            fid = "" + f.hashCode();
        }
        return getInsertFID( fid );
    }

    private InsertFID getInsertFID( String fid ) {
        InsertFID insertFid = origIdToInsertFID.get( fid );
        if ( insertFid == null ) {
            insertFid = new InsertFID( fid );
            if ( fid != null ) {
                origIdToInsertFID.put( fid, insertFid );
            }
        }
        return insertFid;
    }

    private ChildInsertRow getFeatureRow( InsertFID fid ) {
        ChildInsertRow featureRow = fidToFeatureRow.get( fid );
        if ( featureRow == null ) {
            featureRow = new ChildInsertRow( null, null );
            fidToFeatureRow.put( fid, featureRow );
            delayedFeatureRowToFID.put( featureRow, fid );
        }
        return featureRow;
    }

    private void insertParticles( final TypedObjectNode particle, final Mapping mapping, final ChildInsertRow row )
                            throws FilterEvaluationException, FeatureStoreException {

        List<TableJoin> jc = mapping.getJoinedTable();
        if ( jc != null ) {
            if ( jc.size() != 1 ) {
                throw new FeatureStoreException( "Handling of joins with " + jc.size() + " steps is not implemented." );
            }
        }

        FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( gmlVersion );
        TypedObjectNode[] values = evaluator.eval( particle, mapping.getPath() );
        int childIdx = 1;
        for ( TypedObjectNode value : values ) {
            ChildInsertRow currentRow = row;
            if ( jc != null && !( mapping instanceof FeatureMapping ) ) {
                TableJoin join = jc.get( 0 );
                TableName table = join.getToTable();
                // TODO make this configurable
                SQLIdentifier autoGenColumn = new SQLIdentifier( "id" );
                currentRow = addChildRow( currentRow, table, autoGenColumn, join );
            }
            if ( mapping instanceof PrimitiveMapping ) {
                MappingExpression me = ( (PrimitiveMapping) mapping ).getMapping();
                if ( !( me instanceof DBField ) ) {
                    LOG.debug( "Skipping primitive mapping. Not mapped to database column." );
                } else {
                    ParticleConverter<PrimitiveValue> converter = (ParticleConverter<PrimitiveValue>) fs.getConverter( mapping );
                    PrimitiveValue primitiveValue = getPrimitiveValue( value );
                    String column = ( (DBField) me ).getColumn();
                    currentRow.addPreparedArgument( column, primitiveValue, converter );
                }
            } else if ( mapping instanceof GeometryMapping ) {
                MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                if ( !( me instanceof DBField ) ) {
                    LOG.debug( "Skipping geometry mapping. Not mapped to database column." );
                } else {
                    Geometry geom = (Geometry) getPropValue( value );
                    @SuppressWarnings("unchecked")
                    ParticleConverter<Geometry> converter = (ParticleConverter<Geometry>) fs.getConverter( mapping );
                    String column = ( (DBField) me ).getColumn();
                    currentRow.addPreparedArgument( column, geom, converter );
                }
            } else if ( mapping instanceof FeatureMapping ) {
                InsertFID subFid = null;
                String href = null;
                Feature feature = (Feature) getPropValue( value );
                if ( feature instanceof FeatureReference ) {
                    if ( ( (FeatureReference) feature ).isLocal() ) {
                        subFid = getInsertFID( feature.getId() );
                    } else {
                        href = ( (FeatureReference) feature ).getURI();
                    }
                } else if ( feature != null ) {
                    subFid = getInsertFID( feature );
                }

                if ( subFid != null ) {
                    if ( subFid.getNewId() != null ) {
                        href = "#" + subFid.getNewId();
                    }
                    if ( jc.isEmpty() ) {
                        LOG.debug( "Skipping feature mapping (fk). Not mapped to database column." );
                    } else {
                        ChildInsertRow parentRow = getFeatureRow( subFid );
                        TableJoin join = jc.get( 0 );
                        for ( int i = 0; i < join.getFromColumns().size(); i++ ) {
                            // invert join (the logical parent is the sub feature row)
                            SQLIdentifier fromColumn = join.getToColumns().get( i );
                            SQLIdentifier toColumn = join.getFromColumns().get( i );                           
                            Object key = parentRow.get( fromColumn );
                            if ( key == null ) {
                                TableJoin inverseJoin = new TableJoin( false, join.getToTable(), join.getFromTable(),
                                                                       join.getToColumns(), join.getFromColumns(),
                                                                       EMPTY_LIST );
                                InsertRowReference ref = new InsertRowReference( inverseJoin, parentRow );
                                currentRow.addParent( ref );
                                ref.addHrefingRow( currentRow );

                                List<ChildInsertRow> deps = rowToChildRows.get( parentRow );
                                if ( deps == null ) {
                                    deps = new ArrayList<ChildInsertRow>();
                                    rowToChildRows.put( parentRow, deps );
                                }
                                deps.add( currentRow );
                                break;
                            } else {
                                currentRow.addPreparedArgument( toColumn, key );
                            }
                        }
                    }
                }
                if ( href != null ) {
                    MappingExpression me = ( (FeatureMapping) mapping ).getHrefMapping();
                    if ( !( me instanceof DBField ) ) {
                        LOG.debug( "Skipping feature mapping (href). Not mapped to database column." );
                    } else {
                        String column = ( (DBField) me ).getColumn();
                        Object sqlValue = SQLValueMangler.internalToSQL( href );
                        row.addPreparedArgument( column, sqlValue );
                    }
                }
            } else if ( mapping instanceof CompoundMapping ) {
                for ( Mapping child : ( (CompoundMapping) mapping ).getParticles() ) {
                    insertParticles( value, child, currentRow );
                }
            } else {
                LOG.warn( "Unhandled mapping type '" + mapping.getClass() + "'." );
            }

            if ( jc != null ) {
                // add index column value
                for ( SQLIdentifier col : jc.get( 0 ).getOrderColumns() ) {
                    if ( currentRow.get( col ) == null ) {
                        // TODO do this properly
                        currentRow.addLiteralValue( col, "" + childIdx++ );
                    }
                }
            }
        }
    }

    private ChildInsertRow addChildRow( ChildInsertRow parent, TableName table, SQLIdentifier autoGenColumn,
                                        TableJoin join ) {

        ChildInsertRow newRow = new ChildInsertRow( table, autoGenColumn );
        InsertRowReference ref = new InsertRowReference( join, parent );
        newRow.addParent( ref );

        List<ChildInsertRow> deps = rowToChildRows.get( parent );
        if ( deps == null ) {
            deps = new ArrayList<ChildInsertRow>();
            rowToChildRows.put( parent, deps );
        }
        deps.add( newRow );
        return newRow;
    }

    private PrimitiveValue getPrimitiveValue( TypedObjectNode value ) {
        if ( value instanceof Property ) {
            value = ( (Property) value ).getValue();
        }
        if ( value instanceof GenericXMLElement ) {
            value = ( (GenericXMLElement) value ).getValue();
        }
        return (PrimitiveValue) value;
    }

    private TypedObjectNode getPropValue( TypedObjectNode prop ) {
        if ( prop instanceof Property ) {
            return ( (Property) prop ).getValue();
        }
        return prop;
    }

    private void insertRow( ChildInsertRow row )
                            throws SQLException, FeatureStoreException {
        if ( row.hasParents() ) {
            LOG.debug( "Inserting row " + row );
            row.performInsert( conn );

            InsertFID fid = delayedFeatureRowToFID.get( row );
            if ( fid != null ) {
                delayedFeatureRowToFID.remove( row );
                if ( fid.getNewId() == null ) {
                    fid.assign( row );
                }
            }

            List<ChildInsertRow> delayedRows = rowToChildRows.get( row );
            if ( delayedRows != null ) {
                for ( ChildInsertRow childRow : delayedRows ) {
                    LOG.debug( "Child row: " + childRow );
                    childRow.removeParent( row, fid );
                    insertRow( childRow );
                }
            }
        } else {
            LOG.debug( "Delaying insert of row " + row + "." );
        }
    }
}
