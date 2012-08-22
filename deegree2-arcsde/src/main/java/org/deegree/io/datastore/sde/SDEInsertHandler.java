//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.datastore.sde;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.TransactionException;
import org.deegree.io.datastore.idgenerator.FeatureIdAssigner;
import org.deegree.io.datastore.idgenerator.IdGenerationException;
import org.deegree.io.datastore.idgenerator.ParentIDGenerator;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.transaction.insert.FeatureRow;
import org.deegree.io.datastore.sql.transaction.insert.InsertField;
import org.deegree.io.datastore.sql.transaction.insert.InsertRow;
import org.deegree.io.sdeapi.SDEAdapter;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeaturePropertyType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.Geometry;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeState;

/**
 * Handler for <code>Insert</code> operations (usually contained in <code>Transaction</code> requests).
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class SDEInsertHandler extends AbstractSDERequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( SDEInsertHandler.class );

    // features that are currently being processed
    private Map<FeatureId, FeatureRow> featuresInInsertion = new HashMap<FeatureId, FeatureRow>();

    // contains only property rows and join table rows (but no feature rows)
    private List<InsertRow> insertRows = new ArrayList<InsertRow>();

    private SDETransaction dsTa;

    /**
     * Creates a new <code>InsertHandler</code> from the given parameters.
     * 
     * @param dsTa
     */
    public SDEInsertHandler( SDETransaction dsTa ) {
        super( dsTa.getDatastore(), dsTa.getAliasGenerator(), dsTa.getConnection() );
        this.dsTa = dsTa;
    }

    /**
     * Inserts the given feature instance into the datastore.
     * 
     * @param features
     *            (which have a MappedFeatureType as feature type)
     * @return feature ids of inserted (root) feature instances
     * @throws DatastoreException
     *             if the insert could not be performed
     */
    public List<FeatureId> performInsert( List<Feature> features )
                            throws DatastoreException {

        List<FeatureId> fids = new ArrayList<FeatureId>();

        for ( int i = 0; i < features.size(); i++ ) {
            Feature feature = features.get( i );
            MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();
            if ( feature.getId().startsWith( FeatureIdAssigner.EXISTS_MARKER ) ) {
                String msg = "feature already exists " + feature.getName() + " id=" + feature.getId().substring( 1 );
                throw new TransactionException( msg );
            }
            LOG.logDebug( "Inserting root feature '" + feature.getId() + "'..." );
            insertFeature( feature );
            FeatureId fid = new FeatureId( ft, feature.getId() );
            fids.add( fid );
        }

        // merge inserts rows that are identical (except their pks)
        this.insertRows = mergeInsertRows( this.insertRows );

        // add featureRows to insertRows
        Iterator<FeatureRow> iter = this.featuresInInsertion.values().iterator();
        while ( iter.hasNext() ) {
            this.insertRows.add( iter.next() );
        }

        // try to sort the insert rows topologically (but continue in original order, if not topological order is
        // possible)
        List<InsertRow> sortedInserts = InsertRow.getInsertOrder( this.insertRows );

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            Iterator<InsertRow> iter2 = sortedInserts.iterator();
            LOG.logDebug( sortedInserts.size() + " rows to be inserted: " );
            while ( iter2.hasNext() ) {
                LOG.logDebug( iter2.next().toString() );
            }
        }

        executeInserts( sortedInserts );

        return fids;
    }

    /**
     * Builds the <code>InsertRows</code> that are necessary to insert the given feature instance (including all
     * properties + subfeatures).
     * 
     * @param feature
     * @return the row of the given feature
     * @throws TransactionException
     */
    private FeatureRow insertFeature( Feature feature )
                            throws TransactionException {

        MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();
        if ( !ft.isInsertable() ) {
            String msg = "featuretype can't be inserted " + ft.getName();
            throw new TransactionException( msg );
        }

        LOG.logDebug( "Creating InsertRow for feature with type '" + ft.getName() + "' and id: '" + feature.getId()
                      + "'." );

        // extract feature id column value
        MappingField[] fidFields = ft.getGMLId().getIdFields();
        if ( fidFields.length > 1 ) {
            throw new TransactionException( "Insertion of features with compound feature ids is not " + "supported." );
        }
        Object fidValue = null;
        try {
            fidValue = FeatureId.removeFIDPrefix( feature.getId(), ft.getGMLId() );
        } catch ( DatastoreException e ) {
            e.printStackTrace();
            throw new TransactionException( e.getMessage(), e );
        }
        FeatureId fid = new FeatureId( ft, new Object[] { fidValue } );

        // check if the feature id is already being inserted (happens for cyclic features)
        FeatureRow insertRow = this.featuresInInsertion.get( fid );
        if ( insertRow != null ) {
            return insertRow;
        }

        insertRow = new FeatureRow( ft.getTable() );
        this.featuresInInsertion.put( fid, insertRow );

        // add column value for fid (primary key)
        String fidColumn = fidFields[0].getField();
        insertRow.setColumn( fidColumn, fidValue, ft.getGMLId().getIdFields()[0].getType(), true );

        // process properties
        FeatureProperty[] properties = feature.getProperties();
        for ( int i = 0; i < properties.length; i++ ) {
            FeatureProperty property = properties[i];
            MappedPropertyType propertyType = (MappedPropertyType) ft.getProperty( property.getName() );
            if ( propertyType == null ) {
                String msg = "Unknown propertytype " + property.getName();
                LOG.logDebug( msg );
                throw new TransactionException( msg );
            }
            insertProperty( property, propertyType, insertRow );
        }
        return insertRow;
    }

    /**
     * Builds the <code>InsertRow</code>s that are necessary to insert the given property instance (including all it's
     * subfeatures).
     * 
     * @param property
     *            property instance to be inserted
     * @param propertyType
     *            property type of the property
     * @param featureRow
     *            table row of the parent feature instance
     * @throws TransactionException
     */
    private void insertProperty( FeatureProperty property, MappedPropertyType propertyType, InsertRow featureRow )
                            throws TransactionException {

        if ( propertyType instanceof SimplePropertyType ) {
            String msg = StringTools.concat( 300, "- Simple property '", propertyType.getName(),
                                             "', value='" + getPropertyValue( property ), "'." );
            LOG.logDebug( msg );
            insertProperty( (MappedSimplePropertyType) propertyType, property, featureRow );
        } else if ( propertyType instanceof GeometryPropertyType ) {
            LOG.logDebug( "- Geometry property: '" + propertyType.getName() + "'" );
            insertProperty( (MappedGeometryPropertyType) propertyType, property, featureRow );
        } else if ( propertyType instanceof FeaturePropertyType ) {
            LOG.logDebug( "- Feature property: '" + propertyType.getName() + "'" );
            insertProperty( (MappedFeaturePropertyType) propertyType, property, featureRow );
        } else {
            throw new TransactionException( "Unhandled property type '" + propertyType.getClass().getName() + "'." );
        }
    }

    /**
     * Inserts the given simple property (stored in feature table or in related table).
     * 
     * @param pt
     * @param property
     * @param featureRow
     * @throws TransactionException
     */
    private void insertProperty( MappedSimplePropertyType pt, FeatureProperty property, InsertRow featureRow )
                            throws TransactionException {

        SimpleContent content = pt.getContent();
        if ( content.isUpdateable() ) {
            if ( content instanceof MappingField ) {
                MappingField mf = (MappingField) content;
                String propertyColumn = mf.getField();
                Object propertyValue = property.getValue();
                int propertyType = mf.getType();
                TableRelation[] relations = pt.getTableRelations();
                insertProperty( propertyColumn, propertyValue, propertyType, relations, featureRow );
            }
        }
    }

    /**
     * Inserts the given geometry property (stored in feature table or in related table).
     * 
     * @param pt
     * @param property
     * @param featureRow
     * @throws TransactionException
     */
    private void insertProperty( MappedGeometryPropertyType pt, FeatureProperty property, InsertRow featureRow )
                            throws TransactionException {

        String propertyColumn = pt.getMappingField().getField();

        Geometry deegreeGeometry = (Geometry) property.getValue();
        Object dbGeometry;

        try {
            dbGeometry = this.datastore.convertDegreeToDBGeometry( deegreeGeometry );
        } catch ( DatastoreException e ) {
            throw new TransactionException( e.getMessage(), e );
        }

        int propertyType = pt.getMappingField().getType();

        TableRelation[] relations = pt.getTableRelations();
        insertProperty( propertyColumn, dbGeometry, propertyType, relations, featureRow );
    }

    /**
     * Inserts the given simple / geometry property (stored in feature table or in related table).
     * 
     * @param propertyColumn
     * @param propertyValue
     * @param propertyType
     * @param featureRow
     * @throws TransactionException
     */
    private void insertProperty( String propertyColumn, Object propertyValue, int propertyType,
                                 TableRelation[] relations, InsertRow featureRow )
                            throws TransactionException {

        if ( relations == null || relations.length == 0 ) {
            // property is stored in feature table
            featureRow.setColumn( propertyColumn, propertyValue, propertyType, false );
        } else {
            // property is stored in related table
            if ( relations.length > 1 ) {
                throw new TransactionException( "properties in related tables are not allowed here" );
            }

            if ( !relations[0].isFromFK() ) {
                // fk is in property table
                MappingField[] pkFields = relations[0].getFromFields();
                MappingField[] fkFields = relations[0].getToFields();

                for ( int i = 0; i < pkFields.length; i++ ) {
                    InsertField pkField = featureRow.getColumn( pkFields[i].getField() );
                    if ( pkField == null ) {
                        String msg = "Missing foreign key " + pkFields[i].getField() + " / " + pkFields[i].getTable();
                        throw new TransactionException( msg );
                    }
                    int pkColumnType = pkField.getSQLType();
                    int fkColumnType = fkFields[i].getType();
                    if ( pkColumnType != fkColumnType ) {
                        String msg = "FK_PK_TYPE_MISMATCH";
                        throw new TransactionException( msg );
                    }
                    InsertRow insertRow = new InsertRow( relations[0].getToTable() );
                    insertRow.linkColumn( fkFields[i].getField(), pkField );
                    insertRow.setColumn( propertyColumn, propertyValue, propertyType, false );
                    this.insertRows.add( insertRow );
                }
            } else {
                // fk is in feature table
                MappingField[] pkFields = relations[0].getToFields();
                MappingField[] fkFields = relations[0].getFromFields();

                // generate necessary primary key value
                InsertField pkField = null;
                try {
                    Object pk = null;
                    // TODO remove hack!!!
                    if ( relations[0].getIdGenerator() instanceof ParentIDGenerator ) {
                        InsertField field = featureRow.getColumn( "ID" );
                        if ( field == null ) {
                            throw new TransactionException( "No value for ID available!" );
                        }
                        pk = field.getValue();
                    } else {
                        pk = relations[0].getNewPK( this.dsTa );
                    }
                    InsertRow insertRow = findOrCreateRow( relations[0].getToTable(), pkFields[0].getField(), pk );
                    pkField = insertRow.setColumn( pkFields[0].getField(), pk, pkFields[0].getType(), true );
                    insertRow.setColumn( propertyColumn, propertyValue, propertyType, false );
                } catch ( IdGenerationException e ) {
                    throw new TransactionException( e.getMessage(), e );
                }
                featureRow.linkColumn( fkFields[0].getField(), pkField );
            }
        }
    }

    /**
     * Inserts the given feature property.
     * 
     * @param pt
     * @param property
     * @param featureRow
     * @throws TransactionException
     */
    private void insertProperty( MappedFeaturePropertyType pt, FeatureProperty property, InsertRow featureRow )
                            throws TransactionException {

        // find (concrete) subfeature type for the given property instance
        MappedFeatureType propertyFeatureType = pt.getFeatureTypeReference().getFeatureType();
        MappedFeatureType[] substitutions = propertyFeatureType.getConcreteSubstitutions();
        Feature subFeature = (Feature) property.getValue();
        MappedFeatureType subFeatureType = null;
        for ( int i = 0; i < substitutions.length; i++ ) {
            if ( substitutions[i].getName().equals( subFeature.getName() ) ) {
                subFeatureType = substitutions[i];
                break;
            }
        }
        if ( subFeatureType == null ) {
            String msg = "ERROR_FEATURE_NOT_SUBSTITUTABLE " + propertyFeatureType.getName() + "->"
                         + subFeature.getName();
            throw new TransactionException( msg );
        }
        boolean ftIsAbstract = propertyFeatureType.isAbstract();

        TableRelation[] relations = pt.getTableRelations();
        if ( relations == null || relations.length < 1 ) {
            throw new TransactionException( "Invalid feature property definition, feature property "
                                            + "mappings must use at least one 'TableRelation' element." );
        }

        // workaround for links to dummy InsertRows (of already stored features)
        boolean cutLink = subFeature.getId().startsWith( FeatureIdAssigner.EXISTS_MARKER );
        InsertRow subFeatureRow = null;
        if ( cutLink ) {
            try {
                Object fidValue = FeatureId.removeFIDPrefix( subFeature.getId().substring( 1 ),
                                                             subFeatureType.getGMLId() );
                subFeatureRow = new FeatureRow( subFeatureType.getTable() );
                // add column value for fid (primary key)
                String fidColumn = subFeatureType.getGMLId().getIdFields()[0].getField();
                subFeatureRow.setColumn( fidColumn, fidValue, subFeatureType.getGMLId().getIdFields()[0].getType(),
                                         true );
            } catch ( DatastoreException e ) {
                throw new TransactionException( e );
            }
        } else {
            // insert sub feature (if it is not already stored)
            subFeatureRow = insertFeature( subFeature );
        }

        if ( relations.length == 1 ) {

            if ( relations[0].isFromFK() ) {
                // fk is in feature table
                MappingField[] pkFields = relations[0].getToFields();
                MappingField[] fkFields = relations[0].getFromFields();

                for ( int i = 0; i < pkFields.length; i++ ) {
                    InsertField pkField = subFeatureRow.getColumn( pkFields[i].getField() );
                    if ( pkField == null ) {
                        String msg = "Missing foreign key.";
                        throw new TransactionException( msg );
                    }
                    int pkColumnType = pkField.getSQLType();
                    int fkColumnType = fkFields[i].getType();
                    if ( pkColumnType != fkColumnType ) {
                        String msg = "ERROR_FK_PK_TYPE_MISMATCH";
                        throw new TransactionException( msg );
                    }

                    if ( !cutLink ) {
                        featureRow.linkColumn( fkFields[i].getField(), pkField );
                    } else {
                        featureRow.setColumn( fkFields[i].getField(), pkField.getValue(), pkField.getSQLType(), false );
                    }

                }

                if ( ftIsAbstract ) {
                    String typeField = FT_PREFIX + relations[0].getToTable();
                    featureRow.setColumn( typeField, subFeatureType.getName().getLocalName(), Types.VARCHAR, false );
                }
            } else {
                // fk is in subfeature table
                MappingField[] pkFields = relations[0].getFromFields();
                MappingField[] fkFields = relations[0].getToFields();

                InsertField pkField = featureRow.getColumn( pkFields[0].getField() );

                if ( pkField == null ) {
                    String msg = "Missing foreign key.";
                    throw new TransactionException( msg );
                }
                int pkColumnType = pkField.getSQLType();
                int fkColumnType = fkFields[0].getType();
                if ( pkColumnType != fkColumnType ) {
                    String msg = "ERROR_FK_PK_TYPE_MISMATCH";
                    throw new TransactionException( msg );
                }

                if ( !cutLink ) {
                    subFeatureRow.linkColumn( fkFields[0].getField(), pkField );
                } else {
                    featureRow.setColumn( fkFields[0].getField(), pkField.getValue(), pkField.getSQLType(), false );
                }
            }
        } else if ( relations.length == 2 ) {

            // insert into join table
            String joinTable = relations[0].getToTable();
            MappingField[] leftKeyFields = relations[0].getToFields();
            MappingField[] rightKeyFields = relations[1].getFromFields();

            InsertRow jtRow = new InsertRow( joinTable );
            if ( ftIsAbstract ) {
                jtRow.setColumn( FT_COLUMN, subFeatureType.getName().getLocalName(), Types.VARCHAR, false );
            }

            if ( !relations[0].isFromFK() ) {
                // left key field in join table is fk
                MappingField[] pkFields = relations[0].getFromFields();
                InsertField pkField = featureRow.getColumn( pkFields[0].getField() );
                if ( pkField == null ) {
                    throw new TransactionException( "Insertion of feature property using join table failed: "
                                                    + "no value for join table key column." );
                }
                jtRow.linkColumn( leftKeyFields[0].getField(), pkField );
            } else {
                // left key field in join table is pk
                MappingField[] pkFields = relations[0].getToFields();
                // generate necessary primary key value
                InsertField pkField = null;
                try {
                    Object pk = relations[0].getNewPK( this.dsTa );
                    pkField = jtRow.setColumn( pkFields[0].getField(), pk, pkFields[0].getType(), true );
                } catch ( IdGenerationException e ) {
                    throw new TransactionException( e.getMessage(), e );
                }
                featureRow.linkColumn( relations[0].getFromFields()[0].getField(), pkField );
            }

            if ( relations[1].isFromFK() ) {
                // right key field in join table is fk
                MappingField[] pkFields = relations[1].getToFields();
                InsertField pkField = subFeatureRow.getColumn( pkFields[0].getField() );
                if ( pkField == null ) {
                    throw new TransactionException( "Insertion of feature property using join table failed: "
                                                    + "no value for join table key column." );
                }
                if ( !cutLink ) {
                    jtRow.linkColumn( rightKeyFields[0].getField(), pkField );
                } else {
                    jtRow.setColumn( rightKeyFields[0].getField(), pkField.getValue(), pkField.getSQLType(), false );
                }
            } else {
                // right key field in join table is pk
                MappingField[] pkFields = relations[1].getFromFields();
                // generate necessary primary key value
                InsertField pkField = null;
                try {
                    Object pk = relations[1].getNewPK( this.dsTa );
                    pkField = jtRow.setColumn( pkFields[0].getField(), pk, pkFields[0].getType(), true );
                } catch ( IdGenerationException e ) {
                    throw new TransactionException( e.getMessage(), e );
                }
                if ( !cutLink ) {
                    subFeatureRow.linkColumn( relations[1].getToFields()[0].getField(), pkField );
                }
            }

            this.insertRows.add( jtRow );
        } else {
            throw new TransactionException( "Insertion of feature properties stored in related tables "
                                            + "connected via more than one join table is not supported." );
        }
    }

    /**
     * Checks whether the feature that corresponds to the given FeatureRow is already stored in the database.
     * 
     * @param featureRow
     * @return true if the feature exists
     * @throws TransactionException
     */
    private boolean doesFeatureExist( FeatureRow featureRow )
                            throws TransactionException {

        boolean exists = false;

        InsertField pkField = featureRow.getPKColumn();

        SeQuery stmt = null;
        try {
            stmt = buildFeatureSelect( pkField.getColumnName(), pkField.getValue(), featureRow.getTable() );
            stmt.execute();
            SeRow row = stmt.fetch();
            if ( null != row ) {
                exists = true;
            }
            row = stmt.fetch();
            if ( null != row ) {
                String msg = "ERROR_FEATURE_QUERY_MORE_THAN_ONE_RESULT";
                LOG.logError( msg );
                throw new TransactionException( msg );
            }
        } catch ( Exception e ) {
            throw new TransactionException( e );
        } finally {
            try {
                stmt.close();
            } catch ( Exception e ) {
                LOG.logDebug( "Error in SDE command", e );
            }
        }
        return exists;
    }

    /**
     * Builds a SELECT statement that checks for the existence of a feature with the given id.
     * 
     * @param fidColumn
     * @param fidValue
     * @param table
     * @return the statement
     */
    private SeQuery buildFeatureSelect( String fidColumn, Object fidValue, String table ) {
        SeQuery query = null;
        try {
            SeSqlConstruct constr = new SeSqlConstruct( table, fidColumn + "='" + fidValue.toString() + "'" );
            String[] columns = new String[1];
            columns[0] = fidColumn;
            query = new SeQuery( getConnection().getConnection(), columns, constr );
            query.setState( getConnection().getState().getId(), new SeObjectId( SeState.SE_NULL_STATE_ID ),
                            SeState.SE_STATE_DIFF_NOCHECK );
            query.prepareQuery();
        } catch ( Exception e ) {
            LOG.logError( "Error building featureSelect", e );
        }
        return query;
    }

    private InsertRow findOrCreateRow( String table, String pkColumn, Object value ) {
        Iterator<InsertRow> rowIter = this.insertRows.iterator();
        boolean found = false;
        InsertRow row = null;
        while ( rowIter.hasNext() ) {
            row = rowIter.next();
            if ( row.getTable().equals( table ) ) {
                InsertField field = row.getColumn( pkColumn );
                if ( value.equals( field.getValue() ) ) {
                    found = true;
                    LOG.logDebug( "Found matching row " + row );
                    break;
                }
            }
        }
        if ( !found ) {
            row = new InsertRow( table );
            this.insertRows.add( row );
        }
        return row;
    }

    private String getPropertyValue( FeatureProperty property ) {
        Object value = property.getValue();
        StringBuffer sb = new StringBuffer();
        if ( value instanceof Object[] ) {
            Object[] objects = (Object[]) value;
            for ( int i = 0; i < objects.length; i++ ) {
                sb.append( objects[i] );
            }
        } else {
            sb.append( value );
        }
        return sb.toString();
    }

    /**
     * Transforms the given <code>List</code> of <code>InsertRows</code> into SQL INSERT statements and executes them
     * using the underlying JDBC connection.
     * 
     * @param inserts
     * @throws TransactionException
     *             if an SQL error occurs
     */
    private void executeInserts( List<InsertRow> inserts )
                            throws TransactionException {

        SeInsert stmt = null;

        for ( InsertRow row : inserts ) {
            if ( row instanceof FeatureRow ) {
                if ( doesFeatureExist( (FeatureRow) row ) ) {
                    LOG.logDebug( "Skipping feature row. Already present in db." );
                    continue;
                }
            }
            try {
                stmt = createStatement( row );
                stmt.execute();
            } catch ( Exception e ) {
                String msg = "Error performing insert: " + e.getMessage();
                LOG.logError( msg, e );
                throw new TransactionException( msg, e );
            } finally {
                if ( stmt != null ) {
                    try {
                        stmt.close();
                    } catch ( Exception e ) {
                        String msg = "Error closing statement: " + e.getMessage();
                        LOG.logError( msg, e );
                    }
                }
            }
        }
    }

    private SeInsert createStatement( InsertRow row )
                            throws SeException {
        SeInsert inserter = new SeInsert( conn.getConnection() );
        inserter.setState( conn.getState().getId(), new SeObjectId( SeState.SE_NULL_STATE_ID ),
                           SeState.SE_STATE_DIFF_NOCHECK );
        Collection<InsertField> fields = row.getColumns();
        String[] columns = new String[fields.size()];
        int i = 0;
        for ( Iterator<InsertField> iter = fields.iterator(); iter.hasNext(); i++ ) {
            InsertField field = iter.next();
            columns[i] = field.getColumnName();
        }
        inserter.intoTable( row.getTable(), columns );
        SeRow insertRow = inserter.getRowToSet();
        for ( i = 0; i < columns.length; i++ ) {
            InsertField field = row.getColumn( columns[i] );
            SDEAdapter.setRowValue( insertRow, i, field.getValue(), SDEAdapter.mapSQL2SDE( field.getSQLType() ) );
        }
        return inserter;
    }

    /**
     * Merges the given <code>InsertRow</code>s by eliminating rows that have identical content (except for their
     * primary keys).
     * <p>
     * This only applies to non-FeatureRows: there are never two FeatureRows that may be treated as identical, because
     * unique feature ids have been assigned to them before.
     * 
     * @see FeatureIdAssigner
     * 
     * @param insertRows
     * @return the cleaned up list
     */
    private List<InsertRow> mergeInsertRows( List<InsertRow> insertRows ) {

        List<InsertRow> result = new ArrayList<InsertRow>();

        // keys: table names, values: inserts into the table
        Map<String, Collection<InsertRow>> tableMap = new HashMap<String, Collection<InsertRow>>();

        // build table lookup map
        Iterator<InsertRow> iter = insertRows.iterator();
        while ( iter.hasNext() ) {
            InsertRow insertRow = iter.next();
            Collection<InsertRow> tableInserts = tableMap.get( insertRow.getTable() );
            if ( tableInserts == null ) {
                tableInserts = new ArrayList<InsertRow>();
                tableMap.put( insertRow.getTable(), tableInserts );
            }
            tableInserts.add( insertRow );
        }

        iter = insertRows.iterator();
        while ( iter.hasNext() ) {
            InsertRow insertRow = iter.next();
            boolean insert = true;
            if ( !( insertRow instanceof FeatureRow ) ) {
                Collection<InsertRow> tableInserts = tableMap.get( insertRow.getTable() );
                Iterator<InsertRow> candidatesIter = tableInserts.iterator();
                while ( candidatesIter.hasNext() ) {
                    InsertRow candidate = candidatesIter.next();
                    if ( insertRow != candidate ) {
                        if ( compareInsertRows( insertRow, candidate ) ) {
                            LOG.logDebug( "Removing InsertRow: " + insertRow.hashCode() + " " + insertRow
                                          + " - duplicate of: " + candidate );
                            replaceInsertRow( insertRow, candidate );
                            insert = false;
                            tableInserts.remove( insertRow );
                            break;
                        }
                    }
                }
            }
            if ( insert ) {
                result.add( insertRow );
            }
        }
        return result;
    }

    private boolean compareInsertRows( InsertRow row1, InsertRow row2 ) {
        Collection<InsertField> fields1 = row1.getColumns();
        Iterator<InsertField> iter = fields1.iterator();
        while ( iter.hasNext() ) {
            InsertField field1 = iter.next();
            if ( !field1.isPK() ) {
                InsertField field2 = row2.getColumn( field1.getColumnName() );
                Object value1 = field1.getValue();
                Object value2 = null;
                if ( field2 != null )
                    value2 = field2.getValue();
                if ( value1 == null ) {
                    if ( value2 == null ) {
                        continue;
                    }
                    return false;
                }
                if ( !value1.equals( value2 ) ) {
                    return false;
                }
            }
        }
        return true;
    }

    private void replaceInsertRow( InsertRow oldRow, InsertRow newRow ) {

        Collection<InsertField> oldFields = oldRow.getColumns();
        for ( InsertField field : oldFields ) {
            InsertField toField = field.getReferencedField();
            if ( toField != null ) {
                LOG.logDebug( "Removing reference to field '" + toField + "'" );
                toField.removeReferencingField( field );
            }
        }

        Collection<InsertField> referencingFields = oldRow.getReferencingFields();
        for ( InsertField fromField : referencingFields ) {
            LOG.logDebug( "Replacing reference for field '" + fromField + "'" );
            InsertField field = newRow.getColumn( fromField.getReferencedField().getColumnName() );
            LOG.logDebug( "" + field );
            fromField.relinkField( field );
        }
    }
}