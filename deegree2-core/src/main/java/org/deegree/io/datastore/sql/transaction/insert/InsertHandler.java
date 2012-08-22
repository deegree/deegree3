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
package org.deegree.io.datastore.sql.transaction.insert;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
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
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.AbstractRequestHandler;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.transaction.SQLTransaction;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeaturePropertyType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;

/**
 * Handler for {@link Insert} operations (usually contained in {@link Transaction} requests).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InsertHandler extends AbstractRequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( InsertHandler.class );

    // features that are currently being processed
    private Map<FeatureId, FeatureRow> featuresInInsertion = new HashMap<FeatureId, FeatureRow>();

    // contains only property rows and join table rows (but no feature rows)
    private List<InsertRow> insertRows = new ArrayList<InsertRow>();

    private SQLTransaction dsTa;

    /**
     * Creates a new <code>InsertHandler</code> from the given parameters.
     *
     * @param dsTa
     * @param aliasGenerator
     * @param conn
     */
    public InsertHandler( SQLTransaction dsTa, TableAliasGenerator aliasGenerator, Connection conn ) {
        super( dsTa.getDatastore(), aliasGenerator, conn );
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
                String msg = Messages.getMessage( "DATASTORE_FEATURE_EXISTS", feature.getName(),
                                                  feature.getId().substring( 1 ) );
                throw new TransactionException( msg );
            }
            LOG.logDebug( "Inserting root feature '" + feature.getId() + "'..." );
            insertFeature( feature );
            FeatureId fid = new FeatureId( ft, feature.getId() );
            fids.add( fid );

        }

        LOG.logDebug( "Finished creating insert rows." );

        // Free the feature objects that are to be inserted, from now on, only the InsertRows are needed.
        // This is done to free memory, because the excecution of an insert statement sometimes needs large amounts of
        // memory
        // TODO make this unnecessary
        features.clear();
        System.gc();

        // merge inserts rows that are identical (except for their pks)
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
            LOG.logDebug( sortedInserts.size() + " rows to be inserted: " );
            for ( InsertRow row : sortedInserts ) {
                LOG.logDebug( row.toString() );
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
     * @return InsertRows that are necessary to insert the given feature instance
     * @throws TransactionException
     */
    private FeatureRow insertFeature( Feature feature )
                            throws TransactionException {

        MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();
        if ( !ft.isInsertable() ) {
            String msg = Messages.getMessage( "DATASTORE_FT_NOT_INSERTABLE", ft.getName() );
            throw new TransactionException( msg );
        }

        LOG.logDebug( "Creating InsertRow for feature with type '" + ft.getName() + "' and id: '" + feature.getId()
                      + "'." );

        // extract feature id column value
        MappingField[] fidFields = ft.getGMLId().getIdFields();
        if ( fidFields.length > 1 ) {
            throw new TransactionException( "Insertion of features with compound feature " + "ids is not supported." );
        }
        FeatureId fid = null;
        try {
            fid = new FeatureId( ft, feature.getId() );
        } catch ( IdGenerationException e ) {
            throw new TransactionException( e.getMessage(), e );
        }

        // check if the feature id is already being inserted (happens for cyclic features)
        FeatureRow insertRow = this.featuresInInsertion.get( fid );
        if ( insertRow != null ) {
            return insertRow;
        }

        insertRow = new FeatureRow( ft.getTable() );
        this.featuresInInsertion.put( fid, insertRow );

        // add column value for fid (primary key)
        String fidColumn = fidFields[0].getField();
        insertRow.setColumn( fidColumn, fid.getValue( 0 ), ft.getGMLId().getIdFields()[0].getType(), true );

        // process properties
        FeatureProperty[] properties = feature.getProperties();
        for ( int i = 0; i < properties.length; i++ ) {
            FeatureProperty property = properties[i];
            MappedPropertyType propertyType = (MappedPropertyType) ft.getProperty( property.getName() );
            if ( propertyType == null ) {
                String msg = Messages.getMessage( "DATASTORE_PROPERTY_TYPE_NOT_KNOWN", property.getName() );
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
            LOG.logDebug( "- Simple property '" + propertyType.getName() + "'" );
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
        MappingGeometryField dbField = pt.getMappingField();
        Geometry deegreeGeometry = (Geometry) property.getValue();
        Object dbGeometry;

        int createSrsCode = dbField.getSRS();
        int targetSrsCode = -1;
        if ( deegreeGeometry.getCoordinateSystem() == null ) {
            LOG.logDebug( "No SRS information for geometry available. Assuming '" + pt.getSRS() + "'." );
        } else if ( !pt.getSRS().toString().equals( deegreeGeometry.getCoordinateSystem().getIdentifier() ) ) {
            String msg = "Insert-Transformation: geometry srs: "
                         + deegreeGeometry.getCoordinateSystem().getIdentifier() + " -> property srs: " + pt.getSRS();
            LOG.logDebug( msg );
            if ( createSrsCode == -1 ) {
                msg = Messages.getMessage( "DATASTORE_SRS_NOT_SPECIFIED", pt.getName(),
                                           deegreeGeometry.getCoordinateSystem(), pt.getSRS() );
                throw new TransactionException( msg );
            }
            try {
                createSrsCode = datastore.getNativeSRSCode( deegreeGeometry.getCoordinateSystem().getIdentifier() );
            } catch ( DatastoreException e ) {
                throw new TransactionException( e.getMessage(), e );
            }
            targetSrsCode = dbField.getSRS();
        }

        try {
            dbGeometry = this.datastore.convertDeegreeToDBGeometry( deegreeGeometry, createSrsCode, this.conn );
        } catch ( DatastoreException e ) {
            throw new TransactionException( e.getMessage(), e );
        }

        int propertyType = pt.getMappingField().getType();

        // TODO remove this Oracle hack
        if ( this.datastore.getClass().getName().contains( "OracleDatastore" ) ) {
            propertyType = Types.STRUCT;
        }

        TableRelation[] relations = pt.getTableRelations();
        insertProperty( propertyColumn, dbGeometry, propertyType, relations, featureRow, targetSrsCode );
    }

    /**
     * Inserts the given simple property (stored in feature table or in related table).
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
                throw new TransactionException( Messages.getMessage( "DATASTORE_SIMPLE_PROPERTY_JOIN" ) );
            }

            if ( !relations[0].isFromFK() ) {
                // fk is in property table
                MappingField[] pkFields = relations[0].getFromFields();
                MappingField[] fkFields = relations[0].getToFields();

                for ( int i = 0; i < pkFields.length; i++ ) {
                    InsertField pkField = featureRow.getColumn( pkFields[i].getField() );
                    if ( pkField == null ) {
                        String msg = Messages.getMessage( "DATASTORE_NO_FK_VALUE", pkFields[i].getField(),
                                                          pkFields[i].getTable() );
                        throw new TransactionException( msg );
                    }
                    int pkColumnType = pkField.getSQLType();
                    int fkColumnType = fkFields[i].getType();
                    if ( pkColumnType != fkColumnType ) {
                        String fkType = "" + fkColumnType;
                        String pkType = "" + pkColumnType;
                        try {
                            fkType = Types.getTypeNameForSQLTypeCode( fkColumnType );
                            pkType = Types.getTypeNameForSQLTypeCode( pkColumnType );
                        } catch ( UnknownTypeException e ) {
                            LOG.logError( e.getMessage(), e );
                        }
                        Object[] params = new Object[] { relations[0].getToTable(), fkFields[i].getField(), fkType,
                                                        featureRow.getTable(), pkFields[i].getField(), pkType };
                        String msg = Messages.getMessage( "DATASTORE_FK_PK_TYPE_MISMATCH", params );
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
     * Inserts the given geometry property (stored in feature table or in related table).
     *
     * @param propertyColumn
     * @param propertyValue
     * @param propertyType
     * @param featureRow
     * @throws TransactionException
     */
    private void insertProperty( String propertyColumn, Object propertyValue, int propertyType,
                                 TableRelation[] relations, InsertRow featureRow, int targetSrsCode )
                            throws TransactionException {

        if ( relations == null || relations.length == 0 ) {
            // property is stored in feature table
            featureRow.setGeometryColumn( propertyColumn, propertyValue, propertyType, false, targetSrsCode );
        } else {
            // property is stored in related table
            if ( relations.length > 1 ) {
                throw new TransactionException( Messages.getMessage( "DATASTORE_SIMPLE_PROPERTY_JOIN" ) );
            }

            if ( !relations[0].isFromFK() ) {
                // fk is in property table
                MappingField[] pkFields = relations[0].getFromFields();
                MappingField[] fkFields = relations[0].getToFields();

                for ( int i = 0; i < pkFields.length; i++ ) {
                    InsertField pkField = featureRow.getColumn( pkFields[i].getField() );
                    if ( pkField == null ) {
                        String msg = Messages.getMessage( "DATASTORE_NO_FK_VALUE", pkFields[i].getField(),
                                                          pkFields[i].getTable() );
                        throw new TransactionException( msg );
                    }
                    int pkColumnType = pkField.getSQLType();
                    int fkColumnType = fkFields[i].getType();
                    if ( pkColumnType != fkColumnType ) {
                        String fkType = "" + fkColumnType;
                        String pkType = "" + pkColumnType;
                        try {
                            fkType = Types.getTypeNameForSQLTypeCode( fkColumnType );
                            pkType = Types.getTypeNameForSQLTypeCode( pkColumnType );
                        } catch ( UnknownTypeException e ) {
                            LOG.logError( e.getMessage(), e );
                        }
                        Object[] params = new Object[] { relations[0].getToTable(), fkFields[i].getField(), fkType,
                                                        featureRow.getTable(), pkFields[i].getField(), pkType };
                        String msg = Messages.getMessage( "DATASTORE_FK_PK_TYPE_MISMATCH", params );
                        throw new TransactionException( msg );
                    }
                    InsertRow insertRow = new InsertRow( relations[0].getToTable() );
                    insertRow.linkColumn( fkFields[i].getField(), pkField );
                    insertRow.setGeometryColumn( propertyColumn, propertyValue, propertyType, false, targetSrsCode );
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
                    insertRow.setGeometryColumn( propertyColumn, propertyValue, propertyType, false, targetSrsCode );
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
        Object val = property.getValue();

        if ( val instanceof Feature ) {
            Feature subFeature = (Feature) val;
            MappedFeatureType subFeatureType = null;
            for ( int i = 0; i < substitutions.length; i++ ) {
                if ( substitutions[i].getName().equals( subFeature.getName() ) ) {
                    subFeatureType = substitutions[i];
                    break;
                }
            }
            if ( subFeatureType == null ) {
                String msg = Messages.getMessage( "DATASTORE_FEATURE_NOT_SUBSTITUTABLE", propertyFeatureType.getName(),
                                                  subFeature.getName() );
                throw new TransactionException( msg );
            }
            boolean needsDisambiguation = propertyFeatureType.hasSeveralImplementations();

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
                            String msg = Messages.getMessage( "DATASTORE_NO_FK_VALUE", pkFields[i].getField(),
                                                              pkFields[i].getTable() );
                            throw new TransactionException( msg );
                        }
                        int pkColumnType = pkField.getSQLType();
                        int fkColumnType = fkFields[i].getType();
                        if ( pkColumnType != fkColumnType ) {
                            String fkType = "" + fkColumnType;
                            String pkType = "" + pkColumnType;
                            try {
                                fkType = Types.getTypeNameForSQLTypeCode( fkColumnType );
                                pkType = Types.getTypeNameForSQLTypeCode( pkColumnType );
                            } catch ( UnknownTypeException e ) {
                                LOG.logError( e.getMessage(), e );
                            }
                            Object[] params = new Object[] { featureRow.getTable(), fkFields[i].getField(), fkType,
                                                            subFeatureRow.getTable(), pkFields[i].getField(), pkType };
                            String msg = Messages.getMessage( "DATASTORE_FK_PK_TYPE_MISMATCH", params );
                            throw new TransactionException( msg );
                        }

                        if ( !cutLink ) {
                            featureRow.linkColumn( fkFields[i].getField(), pkField );
                        } else {
                            featureRow.setColumn( fkFields[i].getField(), pkField.getValue(), pkField.getSQLType(),
                                                  false );
                        }
                    }

                    if ( needsDisambiguation ) {
                        String typeField = FT_PREFIX + relations[0].getFromFields()[0].getField();
                        featureRow.setColumn( typeField, subFeatureType.getName().getLocalName(), Types.VARCHAR, false );
                    }
                } else {
                    // fk is in subfeature table
                    MappingField[] pkFields = relations[0].getFromFields();
                    MappingField[] fkFields = relations[0].getToFields();

                    if ( pkFields[0] != null ) {
                        LOG.logDebug( "Getting column " + pkFields[0].getField() + "from table: "
                                      + pkFields[0].getTable() + " of the featureRow: " + featureRow.getTable() );
                    }

                    InsertField pkField = featureRow.getColumn( pkFields[0].getField() );

                    if ( pkField == null ) {
                        String msg = null;

                        if ( pkFields[0] != null ) {
                            msg = Messages.getMessage( "DATASTORE_NO_FK_VALUE", pkFields[0].getField(),
                                                       pkFields[0].getTable() );
                        } else {
                            if ( relations[0] != null ) {
                                msg = Messages.getMessage( "DATASTORE_NO_FK_VALUE",
                                                           "unknown primary keys in 'from'-fields",
                                                           relations[0].getFromTable() );
                            } else {
                                msg = Messages.getMessage( "DATASTORE_NO_FK_VALUE",
                                                           "unknown primary keys in 'from'-fields",
                                                           "unknown 'from'-table" );
                            }
                        }

                        throw new TransactionException( msg );
                    }
                    int pkColumnType = pkField.getSQLType();
                    int fkColumnType = fkFields[0].getType();
                    if ( pkColumnType != fkColumnType ) {
                        String fkType = "" + fkColumnType;
                        String pkType = "" + pkColumnType;
                        try {
                            fkType = Types.getTypeNameForSQLTypeCode( fkColumnType );
                            pkType = Types.getTypeNameForSQLTypeCode( pkColumnType );
                        } catch ( UnknownTypeException e ) {
                            LOG.logError( e.getMessage(), e );
                        }
                        Object[] params = new Object[] { subFeatureRow.getTable(), fkFields[0].getField(), fkType,
                                                        featureRow.getTable(), pkField.getColumnName(), pkType };
                        String msg = Messages.getMessage( "DATASTORE_FK_PK_TYPE_MISMATCH", params );
                        throw new TransactionException( msg );
                    }

                    if ( !cutLink ) {
                        subFeatureRow.linkColumn( fkFields[0].getField(), pkField );
                    } else {
                        subFeatureRow.setColumn( fkFields[0].getField(), pkField.getValue(), pkField.getSQLType(),
                                                 false );
                    }
                }
            } else if ( relations.length == 2 ) {

                // insert into join table
                String joinTable = relations[0].getToTable();
                MappingField[] leftKeyFields = relations[0].getToFields();
                MappingField[] rightKeyFields = relations[1].getFromFields();

                InsertRow jtRow = new InsertRow( joinTable );
                if ( needsDisambiguation ) {
                    jtRow.setColumn( FT_COLUMN, subFeatureType.getName().getLocalName(), Types.VARCHAR, false );
                }

                if ( !relations[0].isFromFK() ) {
                    // left key field in join table is fk
                    MappingField[] pkFields = relations[0].getFromFields();
                    InsertField pkField = featureRow.getColumn( pkFields[0].getField() );
                    if ( pkField == null ) {
                        String columnName = null;
                        if ( pkFields[0] != null ) {
                            columnName = pkFields[0].getField();
                        } else {
                            columnName = "unknown primary keys in 'from'-fields";
                        }
                        throw new TransactionException( "Insertion of feature property using join table failed: "
                                                        + "no value for join table key column '" + columnName + "'." );
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
                                                        + "no value for join table key column '"
                                                        + pkFields[0].getField() + "'." );
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
        } else {
            // it's an external XLink. TODO implement all of the cases here!
            String url = ( (URL) val ).toExternalForm();
            String name = pt.getTableRelations()[0].getFromFields()[0].getField();
            featureRow.setColumn( name + "_external", url, Types.VARCHAR, false );
        }
    }

    /**
     * Checks whether the feature that corresponds to the given FeatureRow is already stored in the database.
     *
     * @param featureRow
     * @return true, if feature is already stored, false otherwise
     * @throws DatastoreException
     */
    private boolean doesFeatureExist( FeatureRow featureRow )
                            throws DatastoreException {

        boolean exists = false;

        InsertField pkField = featureRow.getPKColumn();

        StatementBuffer query = buildFeatureSelect( pkField.getColumnName(), pkField.getSQLType(), pkField.getValue(),
                                                    featureRow.getTable() );
        LOG.logDebug( "Feature existence query: '" + query + "'" );

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( this.conn, query );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                exists = true;
            }
            if ( rs.next() ) {
                String msg = Messages.getMessage( "DATASTORE_FEATURE_QUERY_MORE_THAN_ONE_RESULT",
                                                  query.getQueryString() );
                LOG.logError( msg );
                throw new TransactionException( msg );
            }
        } catch ( SQLException e ) {
            throw new TransactionException( e );
        } finally {
            try {
                if ( rs != null ) {
                    rs.close();
                }
            } catch ( SQLException e ) {
                throw new TransactionException( e );
            } finally {
                if ( stmt != null ) {
                    try {
                        stmt.close();
                    } catch ( SQLException e ) {
                        throw new TransactionException( e );
                    }
                }
            }
        }
        return exists;
    }

    /**
     * Builds a SELECT statement that checks for the existence of a feature with the given id.
     *
     * @param fidColumn
     * @param typeCode
     * @param fidValue
     * @param table
     * @return the statement
     */
    private StatementBuffer buildFeatureSelect( String fidColumn, int typeCode, Object fidValue, String table ) {

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT * FROM " );
        query.append( table );
        query.append( " WHERE " );

        // append feature id constraints
        query.append( fidColumn );
        query.append( "=?" );
        query.addArgument( fidValue, typeCode );
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

    /**
     * Transforms the given <code>List</code> of <code>InsertRows</code> into SQL INSERT statements and executes them
     * using the associated JDBC connection.
     *
     * @param inserts
     * @throws DatastoreException
     */
    private void executeInserts( List<InsertRow> inserts )
                            throws DatastoreException {

        PreparedStatement stmt = null;

        for ( InsertRow row : inserts ) {
            if ( row instanceof FeatureRow ) {
                if ( doesFeatureExist( (FeatureRow) row ) ) {
                    LOG.logDebug( "Skipping feature row. Already present in db." );
                    continue;
                }
            }
            try {
                stmt = null;
                StatementBuffer insert = createStatementBuffer( row );
                LOG.logDebug( insert.toString() );

                LOG.logDebug( "Before prepareStatement(): free=" + Runtime.getRuntime().freeMemory() / 1024 / 1024
                              + ", total=" + Runtime.getRuntime().totalMemory() / 1024 / 1024 );

                stmt = this.datastore.prepareStatement( this.conn, insert );
                LOG.logDebug( "After prepareStatement(): free=" + Runtime.getRuntime().freeMemory() / 1024 / 1024
                              + ", total=" + Runtime.getRuntime().totalMemory() / 1024 / 1024 );
                stmt.execute();
            } catch ( SQLException e ) {
                String msg = "Error performing insert: " + e.getMessage();
                LOG.logError( msg, e );
                throw new TransactionException( msg, e );
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
    }

    private StatementBuffer createStatementBuffer( InsertRow row )
                            throws DatastoreException {
        StatementBuffer insert = new StatementBuffer();
        insert.append( "INSERT INTO " );
        insert.append( row.table );
        insert.append( " (" );
        Iterator<InsertField> columnsIter = row.getColumns().iterator();
        while ( columnsIter.hasNext() ) {
            insert.append( columnsIter.next().getColumnName() );
            if ( columnsIter.hasNext() ) {
                insert.append( ',' );
            }
        }
        insert.append( ") VALUES(" );
        columnsIter = row.getColumns().iterator();
        while ( columnsIter.hasNext() ) {
            String placeHolder = "?";
            InsertField field = columnsIter.next();
            if ( field instanceof InsertGeometryField ) {
                int targetSrsCode = ( (InsertGeometryField) field ).getTargetSrsCode();
                if ( targetSrsCode != -1 ) {
                    placeHolder = this.datastore.buildSRSTransformCall( "?", targetSrsCode );
                }
            }
            insert.append( placeHolder );
            insert.addArgument( field.getValue(), field.getSQLType() );
            if ( columnsIter.hasNext() ) {
                insert.append( ',' );
            }
        }
        insert.append( ")" );
        return insert;
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
     * @return merged List of InsertRows
     */
    private List<InsertRow> mergeInsertRows( List<InsertRow> insertRows ) {

        List<InsertRow> result = new ArrayList<InsertRow>();

        // keys: table names, values: inserts into the table
        Map<String, List<InsertRow>> tableMap = new HashMap<String, List<InsertRow>>();

        // build table lookup map
        Iterator<InsertRow> iter = insertRows.iterator();
        while ( iter.hasNext() ) {
            InsertRow insertRow = iter.next();
            List<InsertRow> tableInserts = tableMap.get( insertRow.getTable() );
            if ( tableInserts == null ) {
                tableInserts = new ArrayList<InsertRow>();
                tableMap.put( insertRow.getTable(), tableInserts );
            }
            tableInserts.add( insertRow );
        }

        // merge rows for each table
        for ( String table : tableMap.keySet() ) {
            List<InsertRow> rows = tableMap.get( table );
            LOG.logDebug( "Merging " + rows.size() + " rows for table '" + table + "'" );
            for ( int i = 0; i < rows.size(); i++ ) {
                InsertRow row1 = rows.get( i );
                boolean insert = true;
                if ( !( row1 instanceof FeatureRow ) ) {
                    for ( int j = i + 1; j < rows.size(); j++ ) {
                        InsertRow row2 = rows.get( j );
                        if ( row1 != row2 && !( row2 instanceof FeatureRow ) ) {
                            if ( compareInsertRows( row1, row2 ) ) {
                                LOG.logDebug( "Skipping InsertRow: " + row1.hashCode() + " " + row1
                                              + " - duplicate of: " + row2 );
                                replaceInsertRow( row1, row2 );
                                insert = false;
                                break;
                            }
                        }
                    }
                }
                if ( insert ) {
                    result.add( row1 );
                }
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
                try {
                    if ( !value1.equals( value2 ) ) {
                        return false;
                    }
                } catch ( NullPointerException e ) {
                    LOG.logWarning( "A null pointer exception occurred while comparing features/attributes. Assuming they're not equal..." );
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
