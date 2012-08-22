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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.sql.AbstractRequestHandler;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.transaction.SQLTransaction;
import org.deegree.io.datastore.sql.transaction.UpdateHandler;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;

/**
 * Handler for {@link Delete} operations (which usually occur as parts of {@link Transaction} requests).
 * <p>
 * When a {@link Delete} operation is performed, the following actions are taken:
 * <ul>
 * <li>the {@link FeatureId}s of all (root) feature instances that match the associated {@link Filter} are determined</li>
 * <li>the {@link FeatureGraph} is built in order to determine which features may be deleted without removing
 * subfeatures of independent features</li>
 * <li>the {@link TableGraph} is built that contains explicit information on all table rows that have to be deleted (and
 * their dependencies)</li>
 * <li>the {@link TableNode}s of the {@link TableGraph} are sorted in topological order, i.e. they may be deleted in
 * that order without violating any foreign key constraints</li>
 * </ul>
 *
 * @see FeatureGraph
 * @see TableGraph
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DeleteHandler extends AbstractRequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( DeleteHandler.class );

    private String lockId;

    /**
     * Creates a new <code>DeleteHandler</code> from the given parameters.
     *
     * @param dsTa
     * @param aliasGenerator
     * @param conn
     * @param lockId
     *            optional id of associated lock (may be null)
     */
    public DeleteHandler( SQLTransaction dsTa, TableAliasGenerator aliasGenerator, Connection conn, String lockId ) {
        super( dsTa.getDatastore(), aliasGenerator, conn );
        this.lockId = lockId;
    }

    /**
     * Deletes the features from the {@link Datastore} that have a certain type and are matched by the given filter.
     *
     * @param ft
     *            non-abstract feature type of the features to be deleted
     * @param filter
     *            constraints the feature instances to be deleted
     * @return number of deleted feature instances
     * @throws DatastoreException
     */
    public int performDelete( MappedFeatureType ft, Filter filter )
                            throws DatastoreException {

        assert !ft.isAbstract();

        if ( !ft.isDeletable() ) {
            String msg = Messages.getMessage( "DATASTORE_FT_NOT_DELETABLE", ft.getName() );
            throw new DatastoreException( msg );
        }

        List<FeatureId> fids = determineAffectedAndModifiableFIDs( ft, filter, this.lockId );

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "Affected fids:" );
            for ( FeatureId fid : fids ) {
                LOG.logDebug( "" + fid );
            }
        }

        FeatureGraph featureGraph = new FeatureGraph( fids, this );
        TableGraph tableGraph = new TableGraph( featureGraph, this );

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "FeatureGraph: " + featureGraph );
            LOG.logDebug( "TableGraph: " + tableGraph );
        }

        List<TableNode> sortedNodes = tableGraph.getDeletionOrder();
        for ( TableNode node : sortedNodes ) {
            boolean delete = true;
            if ( node.isDeleteVetoPossible() ) {
                List<TableNode> referencingRows = getReferencingRows( node );
                if ( referencingRows.size() > 0 ) {
                    delete = false;
                    LOG.logDebug( "Skipping delete of " + node + ": " + referencingRows.size() + " reference(s) exist." );
                    for ( TableNode referencingNode : referencingRows ) {
                        LOG.logDebug( "Referenced by: " + referencingNode );
                    }
                }
            }
            if ( delete ) {
                performDelete( node );
            }
        }

        int deletedFeatures = tableGraph.getDeletableRootFeatureCount();

        if ( deletedFeatures != fids.size() ) {
            String msg = Messages.getMessage( "DATASTORE_COULD_NOT_DELETE_ALL" );
            LOG.logInfo( msg );
        }

        // return count of actually deleted (root) features
        return deletedFeatures;
    }

    /**
     * Deletes the table entry from the SQL database that is represented by the given {@link TableNode}.
     *
     * @param node
     * @throws DatastoreException
     */
    private void performDelete( TableNode node )
                            throws DatastoreException {

        StatementBuffer query = new StatementBuffer();
        query.append( "DELETE FROM " );
        query.append( node.getTable() );
        query.append( " WHERE " );
        boolean first = true;
        for ( KeyColumn column : node.getKeyColumns() ) {
            if ( first ) {
                first = false;
            } else {
                query.append( " AND " );
            }
            query.append( column.getName() );
            query.append( "=?" );
            query.addArgument( column.getValue(), column.getTypeCode() );
        }

        PreparedStatement stmt = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Deleting row: " + query );
            stmt.execute();
        } catch ( SQLException e ) {
            String msg = "Error performing delete '" + query + "': " + e.getMessage();
            LOG.logInfo( msg, e );
            throw new DatastoreException( msg );
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    String msg = "Error closing statement: " + e.getMessage();
                    LOG.logError( msg, e );
                }
            }
        }
    }

    /**
     * Determines the {@link TableNode} that represent the simple/geometry properties in the property table attached by
     * the given {@link TableRelation}.
     *
     * @param fid
     *            id of the feature that owns the properties
     * @param relation
     *            describes how the property table is joined to the feature table
     * @return the simple/geometry properties in the the related property table
     * @throws DatastoreException
     */
    List<TableNode> determinePropNodes( FeatureId fid, TableRelation relation )
                            throws DatastoreException {

        List<TableNode> propEntries = new ArrayList<TableNode>();

        this.aliasGenerator.reset();
        String fromAlias = this.aliasGenerator.generateUniqueAlias();
        String toAlias = this.aliasGenerator.generateUniqueAlias();
        MappingField[] fromFields = relation.getFromFields();
        MappingField[] toFields = relation.getToFields();

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT DISTINCT " );
        for ( int i = 0; i < toFields.length; i++ ) {
            query.append( toAlias );
            query.append( "." );
            query.append( toFields[i].getField() );
            if ( i != toFields.length - 1 ) {
                query.append( ',' );
            }
        }
        query.append( " FROM " );
        query.append( fid.getFeatureType().getTable() );
        query.append( " " );
        query.append( fromAlias );
        query.append( " INNER JOIN " );
        query.append( relation.getToTable() );
        query.append( " " );
        query.append( toAlias );
        query.append( " ON " );
        for ( int j = 0; j < fromFields.length; j++ ) {
            query.append( fromAlias );
            query.append( '.' );
            query.append( fromFields[j].getField() );
            query.append( '=' );
            query.append( toAlias );
            query.append( '.' );
            query.append( toFields[j].getField() );
        }
        query.append( " WHERE " );
        appendFeatureIdConstraint( query, fid, fromAlias );

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Performing: " + query );
            rs = stmt.executeQuery();
            while ( rs.next() ) {
                Collection<KeyColumn> keyColumns = new ArrayList<KeyColumn>();
                for ( int i = 0; i < toFields.length; i++ ) {
                    KeyColumn column = new KeyColumn( toFields[i].getField(), toFields[i].getType(),
                                                      rs.getObject( i + 1 ) );
                    keyColumns.add( column );
                }
                TableNode propEntry = new TableNode( relation.getToTable(), keyColumns );
                propEntries.add( propEntry );
            }
        } catch ( SQLException e ) {
            LOG.logInfo( e.getMessage(), e );
            throw new DatastoreException( "Error in addPropertyNodes(): " + e.getMessage() );
        } finally {
            try {
                if ( rs != null ) {
                    try {
                        rs.close();
                    } catch ( SQLException e ) {
                        LOG.logError( "Error closing result set: '" + e.getMessage() + "'.", e );
                    }
                }
            } finally {
                if ( stmt != null ) {
                    try {
                        stmt.close();
                    } catch ( SQLException e ) {
                        LOG.logError( "Error closing statement: '" + e.getMessage() + "'.", e );
                    }
                }
            }
        }
        return propEntries;
    }

    /**
     * Determines the row in the join table that connects a certain feature with a subfeature.
     *
     * @param fid
     *            id of the (super-) feature
     * @param subFid
     *            id of the subfeature
     * @param relation1
     *            describes how the join table is attached
     * @param relation2
     *            describes how the subfeature table is joined
     * @return join table row (as a {@link TableNode})
     * @throws DatastoreException
     */
    TableNode determineJTNode( FeatureId fid, FeatureId subFid, TableRelation relation1, TableRelation relation2 )
                            throws DatastoreException {

        LOG.logDebug( "Determining join table entry for feature " + fid + " and subfeature " + subFid );
        TableNode jtEntry = null;

        this.aliasGenerator.reset();

        String featureTableAlias = this.aliasGenerator.generateUniqueAlias();
        String joinTableAlias = this.aliasGenerator.generateUniqueAlias();
        String subFeatureTableAlias = this.aliasGenerator.generateUniqueAlias();

        MappingField[] fromFields = relation1.getFromFields();
        MappingField[] fromFields2 = relation2.getFromFields();
        MappingField[] toFields = relation1.getToFields();
        MappingField[] toFields2 = relation2.getToFields();

        // need to select 'from' fields of second relation element as well
        MappingField[] selectFields = new MappingField[toFields.length + fromFields2.length];
        for ( int i = 0; i < toFields.length; i++ ) {
            selectFields[i] = toFields[i];
        }
        for ( int i = 0; i < fromFields2.length; i++ ) {
            selectFields[i + toFields.length] = fromFields2[i];
        }

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT DISTINCT " );
        for ( int i = 0; i < selectFields.length; i++ ) {
            query.append( joinTableAlias );
            query.append( "." );
            query.append( selectFields[i].getField() );
            if ( i != selectFields.length - 1 ) {
                query.append( ',' );
            }
        }
        query.append( " FROM " );
        query.append( fid.getFeatureType().getTable() );
        query.append( " " );
        query.append( featureTableAlias );
        query.append( " INNER JOIN " );
        query.append( relation1.getToTable() );
        query.append( " " );
        query.append( joinTableAlias );
        query.append( " ON " );
        for ( int j = 0; j < fromFields.length; j++ ) {
            query.append( featureTableAlias );
            query.append( '.' );
            query.append( fromFields[j].getField() );
            query.append( '=' );
            query.append( joinTableAlias );
            query.append( '.' );
            query.append( toFields[j].getField() );
        }
        query.append( " INNER JOIN " );
        query.append( subFid.getFeatureType().getTable() );
        query.append( " " );
        query.append( subFeatureTableAlias );
        query.append( " ON " );
        for ( int j = 0; j < fromFields2.length; j++ ) {
            query.append( joinTableAlias );
            query.append( '.' );
            query.append( fromFields2[j].getField() );
            query.append( '=' );
            query.append( subFeatureTableAlias );
            query.append( '.' );
            query.append( toFields2[j].getField() );
        }

        query.append( " WHERE " );
        appendFeatureIdConstraint( query, fid, featureTableAlias );
        query.append( " AND " );
        appendFeatureIdConstraint( query, subFid, subFeatureTableAlias );

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Determining join table row: " + query );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                Collection<KeyColumn> keyColumns = new ArrayList<KeyColumn>( selectFields.length );
                for ( int i = 0; i < selectFields.length; i++ ) {
                    KeyColumn column = new KeyColumn( selectFields[i].getField(), selectFields[i].getType(),
                                                      rs.getObject( i + 1 ) );
                    keyColumns.add( column );
                }

                if ( subFid.getFeatureType().hasSeveralImplementations() ) {
                    String localSubFtName = subFid.getFeatureType().getName().getLocalName();
                    KeyColumn column = new KeyColumn( FT_COLUMN, Types.VARCHAR, localSubFtName );
                    keyColumns.add( column );
                }
                jtEntry = new TableNode( relation1.getToTable(), keyColumns );
            } else {
                String msg = "This is impossible: No join table row between feature and subfeature!?";
                throw new DatastoreException( msg );
            }
        } catch ( SQLException e ) {
            LOG.logInfo( e.getMessage(), e );
            throw new DatastoreException( "Error in determineJTNode(): " + e.getMessage() );
        } finally {
            try {
                if ( rs != null ) {
                    try {
                        rs.close();
                    } catch ( SQLException e ) {
                        LOG.logError( "Error closing result set: '" + e.getMessage() + "'.", e );
                    }
                }
            } finally {
                if ( stmt != null ) {
                    try {
                        stmt.close();
                    } catch ( SQLException e ) {
                        LOG.logError( "Error closing statement: '" + e.getMessage() + "'.", e );
                    }
                }
            }
        }
        return jtEntry;
    }

    /**
     * Delete orphaned rows in the specified property table (target table of the given {@link TableRelation}).
     * <p>
     * Only used by the {@link UpdateHandler}.
     *
     * @param relation
     * @param keyValues
     * @throws DatastoreException
     */
    public void deleteOrphanedPropertyRows( TableRelation relation, Object[] keyValues )
                            throws DatastoreException {
        Collection<KeyColumn> keyColumns = new ArrayList<KeyColumn>( keyValues.length );
        for ( int i = 0; i < keyValues.length; i++ ) {
            KeyColumn keyColumn = new KeyColumn( relation.getToFields()[i].getField(),
                                                 relation.getToFields()[i].getType(), keyValues[i] );
            keyColumns.add( keyColumn );
        }
        TableNode node = new TableNode( relation.getToTable(), keyColumns );
        if ( getReferencingRows( node ).size() == 0 ) {
            performDelete( node );
        }
    }

    /**
     * Returns all table rows that reference the given table row ({@link TableNode}).
     *
     * @param node
     * @return all table rows that reference the given table row
     * @throws DatastoreException
     */
    private List<TableNode> getReferencingRows( TableNode node )
                            throws DatastoreException {

        List<TableNode> rows = new ArrayList<TableNode>();
        for ( TableReference tableReference : getReferencingTables( node.getTable() ) ) {
            rows.addAll( getReferencingRows( node, tableReference ) );
        }
        return rows;
    }

    /**
     * Returns all stored rows (as {@link TableNode}s) that reference the given row ({@link TableNode}) via the also
     * given reference relation.
     *
     * @param node
     * @param ref
     * @return all stored rows that reference the given row
     * @throws DatastoreException
     */
    private List<TableNode> getReferencingRows( TableNode node, TableReference ref )
                            throws DatastoreException {

        List<TableNode> referencingRows = new ArrayList<TableNode>();
        this.aliasGenerator.reset();
        String fromAlias = this.aliasGenerator.generateUniqueAlias();
        String toAlias = this.aliasGenerator.generateUniqueAlias();
        MappingField[] fromFields = ref.getFkColumns();
        MappingField[] toFields = ref.getKeyColumns();

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT DISTINCT " );
        for ( int i = 0; i < fromFields.length; i++ ) {
            query.append( fromAlias );
            query.append( "." );
            query.append( fromFields[i].getField() );
            if ( i != fromFields.length - 1 ) {
                query.append( ',' );
            }
        }
        query.append( " FROM " );
        query.append( ref.getFromTable() );
        query.append( " " );
        query.append( fromAlias );
        query.append( " INNER JOIN " );
        query.append( ref.getToTable() );
        query.append( " " );
        query.append( toAlias );
        query.append( " ON " );
        for ( int j = 0; j < fromFields.length; j++ ) {
            query.append( fromAlias );
            query.append( '.' );
            query.append( fromFields[j].getField() );
            query.append( '=' );
            query.append( toAlias );
            query.append( '.' );
            query.append( toFields[j].getField() );
        }
        query.append( " WHERE " );
        int i = node.getKeyColumns().size();
        for ( KeyColumn column : node.getKeyColumns() ) {
            query.append( toAlias );
            query.append( '.' );
            query.append( column.getName() );
            query.append( "=?" );
            query.addArgument( column.getValue(), column.getTypeCode() );
            if ( --i != 0 ) {
                query.append( " AND " );
            }
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Performing: " + query );
            rs = stmt.executeQuery();
            while ( rs.next() ) {
                Collection<KeyColumn> keyColumns = new ArrayList<KeyColumn>( fromFields.length );
                for ( i = 0; i < fromFields.length; i++ ) {
                    KeyColumn column = new KeyColumn( fromFields[i].getField(), fromFields[i].getType(),
                                                      rs.getObject( i + 1 ) );
                    keyColumns.add( column );
                }
                TableNode referencingRow = new TableNode( ref.getFromTable(), keyColumns );
                referencingRows.add( referencingRow );
            }
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error in getReferencingRows(): " + e.getMessage() );
        } finally {
            try {
                if ( rs != null ) {
                    try {
                        rs.close();
                    } catch ( SQLException e ) {
                        LOG.logError( "Error closing result set: '" + e.getMessage() + "'.", e );
                    }
                }
            } finally {
                if ( stmt != null ) {
                    try {
                        stmt.close();
                    } catch ( SQLException e ) {
                        LOG.logError( "Error closing statement: '" + e.getMessage() + "'.", e );
                    }
                }
            }
        }
        return referencingRows;
    }

    /**
     * Returns all tables that reference the given table.
     *
     * TODO cache search
     *
     * @param table
     * @return all tables that reference the given table
     */
    private List<TableReference> getReferencingTables( String table ) {

        List<TableReference> tables = new ArrayList<TableReference>();
        MappedGMLSchema[] schemas = this.datastore.getSchemas();
        for ( int i = 0; i < schemas.length; i++ ) {
            MappedGMLSchema schema = schemas[i];
            FeatureType[] fts = schema.getFeatureTypes();
            for ( int j = 0; j < fts.length; j++ ) {
                MappedFeatureType ft = (MappedFeatureType) fts[j];
                if ( !ft.isAbstract() ) {
                    PropertyType[] props = ft.getProperties();
                    for ( int k = 0; k < props.length; k++ ) {
                        tables.addAll( getReferencingTables( (MappedPropertyType) props[k], table ) );
                    }
                }
            }
        }
        return tables;
    }

    /**
     * Returns all tables that reference the given table and that are defined in the mapping of the given property type.
     *
     * @param property
     * @param table
     * @return all tables that reference the given table
     */
    private List<TableReference> getReferencingTables( MappedPropertyType property, String table ) {

        List<TableReference> tables = new ArrayList<TableReference>();
        if ( property instanceof MappedFeaturePropertyType
             && ( (MappedFeaturePropertyType) property ).getFeatureTypeReference().getFeatureType().isAbstract() ) {
            TableRelation[] relations = property.getTableRelations();
            for ( int j = 0; j < relations.length - 1; j++ ) {
                TableReference ref = new TableReference( relations[j] );
                if ( ref.getToTable().equals( table ) ) {
                    tables.add( ref );
                }
            }
            MappedFeaturePropertyType pt = (MappedFeaturePropertyType) property;
            MappedFeatureType abstractFt = pt.getFeatureTypeReference().getFeatureType();
            MappedFeatureType[] substitutions = abstractFt.getConcreteSubstitutions();
            for ( MappedFeatureType concreteType : substitutions ) {
                TableRelation finalStep = relations[relations.length - 1];
                TableReference ref = new TableReference( getTableRelation( finalStep, concreteType.getTable() ) );
                if ( ref.getToTable().equals( table ) ) {
                    tables.add( ref );
                }
            }

        } else {
            TableRelation[] relations = property.getTableRelations();
            for ( int j = 0; j < relations.length; j++ ) {
                TableReference ref = new TableReference( relations[j] );
                if ( ref.getToTable().equals( table ) ) {
                    tables.add( ref );
                }
            }
        }
        return tables;
    }

    private TableRelation getTableRelation( TableRelation toAbstractSubFt, String table ) {
        MappingField[] toConcreteFields = new MappingField[toAbstractSubFt.getToFields().length];
        for ( int i = 0; i < toConcreteFields.length; i++ ) {
            MappingField toAbstractField = toAbstractSubFt.getToFields()[i];
            toConcreteFields[i] = new MappingField( table, toAbstractField.getField(), toAbstractField.getType() );
        }
        TableRelation toConcreteSubFt = new TableRelation( toAbstractSubFt.getFromFields(), toConcreteFields,
                                                           toAbstractSubFt.getFKInfo(),
                                                           toAbstractSubFt.getIdGenerator() );
        return toConcreteSubFt;
    }
}
