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

import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.transaction.IDGenMode.GENERATE_NEW;
import static org.deegree.protocol.wfs.transaction.IDGenMode.USE_EXISTING;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTransaction;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
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
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs inserts in courtesy of the {@link SQLFeatureStoreTransaction}.
 * <p>
 * The current strategy may appear complicated, but aims for the following
 * <ul>
 * <li>Cope with complex structures / mappings</li>
 * <li>Streaming (still work to be done)</li>
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

    private final Connection conn;

    private final GMLVersion gmlVersion;

    // keys: original feature ids (before reassignment), values: reassignment info
    private final Map<String, IdAssignment> origIdToAssignment = new HashMap<String, IdAssignment>();

    private final Map<InsertRow, List<DelayedInsertRow>> rowToDelayedRows = new HashMap<InsertRow, List<DelayedInsertRow>>();

    public InsertRowManager( SQLFeatureStore fs, Connection conn ) {
        this.fs = fs;
        this.conn = conn;
        this.gmlVersion = fs.getSchema().getXSModel() == null ? GML_32 : fs.getSchema().getXSModel().getVersion();
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
    public IdAssignment insertFeature( Feature feature, FeatureTypeMapping ftMapping, IDGenMode mode )
                            throws SQLException, FeatureStoreException, FilterEvaluationException {

        try {
            IdAssignment fid = getAssignment( feature.getId() );
            DelayedInsertRow row = fid.getInsertRow();
            FIDMapping fidMapping = ftMapping.getFidMapping();

            if ( mode == USE_EXISTING ) {
                row.setTable( ftMapping.getFtTable() );
                row.addPreparedArgument( fidMapping.getColumn(), fid.getOldId() );
            } else if ( mode == GENERATE_NEW ) {
                IDGenerator gen = fidMapping.getIdGenerator();
                if ( gen instanceof AutoIDGenerator ) {
                    row.setTable( ftMapping.getFtTable() );
                    row.setAutoGenColumn( fidMapping.getColumn() );
                } else if ( gen instanceof UUIDGenerator ) {
                    row.setTable( ftMapping.getFtTable() );
                    String uuid = generateNewId();
                    row.addPreparedArgument( fidMapping.getColumn(), uuid );
                } else {
                    throw new FeatureStoreException(
                                                     "Cannot generate new feature id for feature of type '"
                                                                             + feature.getType().getName()
                                                                             + "': currently, only UUIDGenerator and AutoIDGenerator is supported." );
                }
            } else {
                throw new UnsupportedOperationException();
            }

            for ( Mapping particleMapping : ftMapping.getMappings() ) {
                insertParticles( feature, particleMapping, row );
            }

            LOG.debug( "Built feature row {}", row );
            insertRow( row );
            fid.assign( fidMapping );
            return fid;
        } catch ( Throwable t ) {
            t.printStackTrace();
        }
        return null;
    }

    private void insertParticles( final TypedObjectNode particle, final Mapping mapping, final DelayedInsertRow row )
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
            DelayedInsertRow current = row;
            if ( jc != null && !( mapping instanceof FeatureMapping ) ) {
                TableJoin join = jc.get( 0 );
                QTableName table = join.getToTable();
                // TODO make this configurable
                String autoGenColumn = "id";
                current = addChildRow( current, table, autoGenColumn, join );
            }
            if ( mapping instanceof PrimitiveMapping ) {
                MappingExpression me = ( (PrimitiveMapping) mapping ).getMapping();
                if ( !( me instanceof DBField ) ) {
                    LOG.debug( "Skipping primitive mapping. Not mapped to database column." );
                } else {
                    ParticleConverter<PrimitiveValue> converter = (ParticleConverter<PrimitiveValue>) fs.getConverter( mapping );
                    PrimitiveValue primitiveValue = getPrimitiveValue( value );
                    String column = ( (DBField) me ).getColumn();
                    current.addPreparedArgument( column, primitiveValue, converter );
                }
            } else if ( mapping instanceof GeometryMapping ) {
                MappingExpression me = ( (GeometryMapping) mapping ).getMapping();
                if ( !( me instanceof DBField ) ) {
                    LOG.debug( "Skipping geometry mapping. Not mapped to database column." );
                } else {
                    Geometry geom = (Geometry) getPropValue( value );
                    ParticleConverter<Geometry> converter = (ParticleConverter<Geometry>) fs.getConverter( mapping );
                    String column = ( (DBField) me ).getColumn();
                    current.addPreparedArgument( column, geom, converter );
                }
            } else if ( mapping instanceof FeatureMapping ) {
                String subFid = null;
                String href = null;
                Feature feature = (Feature) getPropValue( value );
                if ( feature instanceof FeatureReference ) {
                    if ( ( (FeatureReference) feature ).isLocal() ) {
                        subFid = feature.getId();
                    } else {
                        href = ( (FeatureReference) feature ).getURI();
                    }
                } else if ( feature != null ) {
                    subFid = feature.getId();
                }

                if ( subFid != null ) {
                    if ( jc.isEmpty() ) {
                        LOG.debug( "Skipping feature mapping (fk). Not mapped to database column." );
                    } else {
                        DelayedInsertRow parentRow = getAssignment( subFid ).getInsertRow();
                        TableJoin join = jc.get( 0 );
                        for ( int i = 0; i < join.getFromColumns().size(); i++ ) {
                            // invert join (the logical parent is the sub feature row)
                            String fromColumn = join.getToColumns().get( i );
                            String toColumn = join.getFromColumns().get( i );
                            Object key = parentRow.get( fromColumn );
                            if ( key == null ) {
                                TableJoin inverseJoin = new TableJoin( join.getToTable(), join.getFromTable(),
                                                                       join.getToColumns(), join.getFromColumns(),
                                                                       Collections.EMPTY_LIST, false );
                                InsertRowReference ref = new InsertRowReference( inverseJoin, parentRow );
                                current.addParent( ref );

                                List<DelayedInsertRow> deps = rowToDelayedRows.get( parentRow );
                                if ( deps == null ) {
                                    deps = new ArrayList<DelayedInsertRow>();
                                    rowToDelayedRows.put( parentRow, deps );
                                }
                                deps.add( current );
                                break;
                            } else {
                                current.addPreparedArgument( toColumn, key );
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
                    insertParticles( value, child, current );
                }
            } else {
                LOG.warn( "Unhandled mapping type '" + mapping.getClass() + "'." );
            }

            if ( jc != null ) {
                // add index column value
                for ( String col : jc.get( 0 ).getOrderColumns() ) {
                    if ( current.get( col ) == null ) {
                        // TODO do this properly
                        current.addLiteralValue( col, "" + childIdx++ );
                    }
                }
            }
        }
    }

    private IdAssignment getAssignment( String subFid ) {
        IdAssignment assignment = origIdToAssignment.get( subFid );
        if ( assignment == null ) {
            assignment = new IdAssignment( subFid, new DelayedInsertRow( null, null ) );
            origIdToAssignment.put( subFid, assignment );
        }
        return assignment;
    }

    private DelayedInsertRow addChildRow( DelayedInsertRow parent, QTableName table, String autoGenColumn,
                                          TableJoin join ) {

        DelayedInsertRow newRow = new DelayedInsertRow( table, autoGenColumn );
        InsertRowReference ref = new InsertRowReference( join, parent );
        newRow.addParent( ref );

        List<DelayedInsertRow> deps = rowToDelayedRows.get( parent );
        if ( deps == null ) {
            deps = new ArrayList<DelayedInsertRow>();
            rowToDelayedRows.put( parent, deps );
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

    private void insertRow( DelayedInsertRow row )
                            throws SQLException {
        if ( row.canInsert() ) {
            LOG.debug( "Inserting row " + row );
            row.performInsert( conn );
            List<DelayedInsertRow> delayedRows = rowToDelayedRows.get( row );
            if ( delayedRows != null ) {
                for ( DelayedInsertRow childRow : delayedRows ) {
                    LOG.debug( "Child row: " + childRow );
                    childRow.removeParent( row );
                    insertRow( childRow );
                }
            }
        } else {
            LOG.debug( "Delaying insert of row " + row + "." );
        }
    }

    private String generateNewId() {
        return UUID.randomUUID().toString();
    }
}
