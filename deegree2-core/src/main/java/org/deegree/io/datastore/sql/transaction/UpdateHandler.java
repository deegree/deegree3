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
package org.deegree.io.datastore.sql.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.TransactionException;
import org.deegree.io.datastore.idgenerator.FeatureIdAssigner;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.TableRelation.FK_INFO;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.AbstractRequestHandler;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.transaction.delete.DeleteHandler;
import org.deegree.io.datastore.sql.transaction.insert.InsertHandler;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeaturePropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.ElementStep;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wfs.operation.transaction.Update;

/**
 * Handler for {@link Update} operations (usually contained in {@link Transaction} requests).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class UpdateHandler extends AbstractRequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( UpdateHandler.class );

    private SQLTransaction dsTa;

    private String lockId;

    /**
     * Creates a new <code>UpdateHandler</code> from the given parameters.
     * 
     * @param dsTa
     * @param aliasGenerator
     * @param conn
     * @param lockId
     *            optional id of associated lock (may be null)
     */
    public UpdateHandler( SQLTransaction dsTa, TableAliasGenerator aliasGenerator, Connection conn, String lockId ) {
        super( dsTa.getDatastore(), aliasGenerator, conn );
        this.dsTa = dsTa;
        this.lockId = lockId;
    }

    /**
     * Performs an update operation against the associated datastore.
     * 
     * @param ft
     * @param replacementProps
     * @param filter
     * @return number of updated (root) feature instances
     * @throws DatastoreException
     */
    public int performUpdate( MappedFeatureType ft, Map<PropertyPath, FeatureProperty> replacementProps, Filter filter )
                            throws DatastoreException {

        List<FeatureId> fids = determineAffectedAndModifiableFIDs( ft, filter, this.lockId );

        LOG.logDebug( "Updating: " + ft );
        for ( FeatureId fid : fids ) {
            Hashtable<String, Hashtable<FeatureId, StatementBuffer>> tableToFeatureUpdate = new Hashtable<String, Hashtable<FeatureId, StatementBuffer>>();

            for ( PropertyPath property : replacementProps.keySet() ) {
                LOG.logDebug( "Updating feature: " + fid );
                FeatureProperty propertyValue = replacementProps.get( property );
                performUpdate( fid, ft, property, propertyValue, tableToFeatureUpdate );
            }

            for ( Hashtable<FeatureId, StatementBuffer> featureStatementBuffers : tableToFeatureUpdate.values() ) {
                for ( Enumeration<FeatureId> it = featureStatementBuffers.keys(); it.hasMoreElements(); ) {
                    FeatureId statementFid = it.nextElement();
                    StatementBuffer query = featureStatementBuffers.get( statementFid );

                    query.append( " WHERE " );
                    appendFIDWhereCondition( query, statementFid );

                    PreparedStatement stmt = null;
                    LOG.logDebug( "Performing aggregate update of in-table properties: " + query.getQueryString() );
                    try {
                        stmt = this.datastore.prepareStatement( conn, query );
                        stmt.execute();
                    } catch ( SQLException e ) {
                        throw new DatastoreException( "Error in performUpdate(): " + e.getMessage() );
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
            }
        }
        return fids.size();
    }

    /**
     * Performs an update operation (replace-style) against the associated datastore.
     * <p>
     * All features matched by the given filter are altered, so their properties are identical to those of the specified
     * replacement feature.
     * </p>
     * <p>
     * NOTE: Currently, the contained feature must not contain any feature-valued properties or multi-properties.
     * </p>
     * 
     * @param mappedFeatureType
     * @param replacementFeature
     * @param filter
     * @return number of updated (root) feature instances
     * @throws DatastoreException
     */
    public int performUpdate( MappedFeatureType mappedFeatureType, Feature replacementFeature, Filter filter )
                            throws DatastoreException {
        LOG.logDebug( "Updating (replace): " + mappedFeatureType );
        if ( filter != null ) {
            LOG.logDebug( " filter: " + filter.to110XML() );
        }

        PropertyType[] replPropertyTypes = replacementFeature.getFeatureType().getProperties();
        int result = 0;
        // rb: updating complex features is a needed feature, gml:id reservation as well, therefore let's see if we are
        // updating a complex feature, we do the old school, delete->insert (loosing gml:id) if updating a simple
        // feature, do a 'new school' update.
        if ( replPropertyTypes != null ) {
            boolean oldSchool = false;
            for ( int i = 0; i < replPropertyTypes.length && !oldSchool; ++i ) {
                PropertyType rpt = replPropertyTypes[i];
                oldSchool = rpt != null
                            && ( rpt instanceof FeaturePropertyType || rpt instanceof MappedFeaturePropertyType );

            }
            if ( oldSchool ) {
                LOG.logWarning( "The given featuretype, is a complex feature type, updating this feature will result in the loss of gml:id's in the update feature and it's references." );
                result = performUpdateWithFeatures( mappedFeatureType, replacementFeature, filter );
            } else {
                LOG.logDebug( "Updating feature with correct gml:id handling (replace): " + mappedFeatureType );
                result = performUpdateCorrectForProperties( mappedFeatureType, replacementFeature, filter );
            }
        }
        return result;
    }

    /**
     * Performs an update operation (replace-style) against the associated datastore.
     * <p>
     * All features matched by the given filter are altered, so their properties are identical to those of the specified
     * replacement feature.
     * </p>
     * <p>
     * NOTE: Currently, the contained feature must not contain any feature-valued properties or multi-properties.
     * </p>
     * 
     * @param mappedFeatureType
     * @param replacementFeature
     * @param filter
     * @return number of updated (root) feature instances
     * @throws DatastoreException
     */
    private int performUpdateCorrectForProperties( MappedFeatureType mappedFeatureType, Feature replacementFeature,
                                                   Filter filter )
                            throws DatastoreException {

        Map<PropertyPath, FeatureProperty> replaceProperties = new LinkedHashMap<PropertyPath, FeatureProperty>();
        FeatureProperty[] featureProps = replacementFeature.getProperties();
        for ( FeatureProperty featureProperty : featureProps ) {
            List<PropertyPathStep> steps = new ArrayList<PropertyPathStep>( 1 );
            steps.add( new ElementStep( featureProperty.getName() ) );
            PropertyPath path = new PropertyPath( steps );
            replaceProperties.put( path, featureProperty );
        }
        return performUpdate( mappedFeatureType, replaceProperties, filter );
    }

    /**
     * Performs an update operation against the associated datastore.
     * <p>
     * The filter must match exactly one feature instance (or none) which is then replaced by the specified replacement
     * feature.
     * 
     * @param mappedFeatureType
     * @param replacementFeature
     * @param filter
     * @return number of updated (root) feature instances (0 or 1)
     * @throws DatastoreException
     */
    private int performUpdateWithFeatures( MappedFeatureType mappedFeatureType, Feature replacementFeature,
                                           Filter filter )
                            throws DatastoreException {

        List<FeatureId> fids = determineAffectedAndModifiableFIDs( mappedFeatureType, filter, this.lockId );

        if ( fids.size() > 1 ) {
            String msg = Messages.getMessage( "DATASTORE_MORE_THAN_ONE_FEATURE" );
            throw new DatastoreException( msg );
        }
        DeleteHandler deleteHandler = new DeleteHandler( this.dsTa, this.aliasGenerator, this.conn, this.lockId );
        deleteHandler.performDelete( mappedFeatureType, filter );

        // identify stored subfeatures / assign feature ids
        FeatureIdAssigner fidAssigner = new FeatureIdAssigner( Insert.ID_GEN.GENERATE_NEW );
        fidAssigner.assignFID( replacementFeature, this.dsTa );
        // TODO remove this hack
        fidAssigner.markStoredFeatures();

        InsertHandler insertHandler = new InsertHandler( this.dsTa, this.aliasGenerator, this.conn );
        List<Feature> features = new ArrayList<Feature>();
        features.add( replacementFeature );
        insertHandler.performInsert( features );

        return fids.size();
    }

    /**
     * Performs the update (replacing of a property) of the given feature instance.
     * <p>
     * If the selected property is a direct property of the feature, the root feature is updated, otherwise the targeted
     * subfeatures have to be determined first.
     * 
     * @param fid
     * @param ft
     * @param propertyName
     * @param replacementProperty
     * @param statementBuffers
     * @throws DatastoreException
     */
    private void performUpdate( FeatureId fid, MappedFeatureType ft, PropertyPath propertyName,
                                FeatureProperty replacementProperty,
                                Hashtable<String, Hashtable<FeatureId, StatementBuffer>> statementBuffers )
                            throws DatastoreException {

        Object replacementValue = replacementProperty.getValue();
        LOG.logDebug( "Updating fid: " + fid + ", propertyName: " + propertyName + " -> " + replacementValue );

        int steps = propertyName.getSteps();
        QualifiedName propName = propertyName.getStep( steps - 1 ).getPropertyName();
        if ( steps > 2 ) {
            QualifiedName subFtName = propertyName.getStep( steps - 2 ).getPropertyName();
            MappedFeatureType subFt = this.datastore.getFeatureType( subFtName );
            MappedPropertyType pt = (MappedPropertyType) subFt.getProperty( propName );
            List<TableRelation> tablePath = getTablePath( ft, propertyName );
            List<FeatureId> subFids = determineAffectedFIDs( fid, subFt, tablePath );
            for ( FeatureId subFid : subFids ) {
                updateProperty( subFid, subFt, pt, replacementValue, statementBuffers );
            }
        } else {
            MappedPropertyType pt = (MappedPropertyType) ft.getProperty( propName );
            updateProperty( fid, ft, pt, replacementValue, statementBuffers );
        }
    }

    /**
     * Determines the subfeature instances that are targeted by the given PropertyName.
     * 
     * @param fid
     * @param subFt
     * @param path
     * @return the matched feature ids
     * @throws DatastoreException
     */
    private List<FeatureId> determineAffectedFIDs( FeatureId fid, MappedFeatureType subFt, List<TableRelation> path )
                            throws DatastoreException {

        List<FeatureId> subFids = new ArrayList<FeatureId>();

        this.aliasGenerator.reset();
        String[] tableAliases = this.aliasGenerator.generateUniqueAliases( path.size() + 1 );
        String toTableAlias = tableAliases[tableAliases.length - 1];
        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );
        appendFeatureIdColumns( subFt, toTableAlias, query );
        query.append( " FROM " );
        query.append( path.get( 0 ).getFromTable() );
        query.append( " " );
        query.append( tableAliases[0] );
        // append joins
        for ( int i = 0; i < path.size(); i++ ) {
            query.append( " JOIN " );
            query.append( path.get( i ).getToTable() );
            query.append( " " );
            query.append( tableAliases[i + 1] );
            query.append( " ON " );
            MappingField[] fromFields = path.get( i ).getFromFields();
            MappingField[] toFields = path.get( i ).getToFields();
            for ( int j = 0; j < fromFields.length; j++ ) {
                query.append( tableAliases[i] );
                query.append( '.' );
                query.append( fromFields[j].getField() );
                query.append( '=' );
                query.append( tableAliases[i + 1] );
                query.append( '.' );
                query.append( toFields[j].getField() );
            }
        }
        query.append( " WHERE " );
        MappingField[] fidFields = fid.getFidDefinition().getIdFields();
        for ( int i = 0; i < fidFields.length; i++ ) {
            query.append( tableAliases[0] );
            query.append( '.' );
            query.append( fidFields[i].getField() );
            query.append( "=?" );
            query.addArgument( fid.getValue( i ), fidFields[i].getType() );
            if ( i != fidFields.length - 1 ) {
                query.append( " AND " );
            }
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            rs = stmt.executeQuery();
            subFids = extractFeatureIds( rs, subFt );
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error in determineAffectedFIDs(): " + e.getMessage() );
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
        return subFids;
    }

    /**
     * Returns the relations (the "path") that lead from the feature type's table to the subfeature table which is
     * targeted by the specified property name.
     * 
     * @param ft
     *            source feature type
     * @param path
     *            property name
     * @return relations that lead from the feature type's table to the subfeature table
     */
    private List<TableRelation> getTablePath( MappedFeatureType ft, PropertyPath path ) {
        List<TableRelation> relations = new ArrayList<TableRelation>();
        for ( int i = 1; i < path.getSteps() - 2; i += 2 ) {
            QualifiedName propName = path.getStep( i ).getPropertyName();
            MappedFeaturePropertyType pt = (MappedFeaturePropertyType) ft.getProperty( propName );
            TableRelation[] tableRelations = pt.getTableRelations();
            for ( int j = 0; j < tableRelations.length; j++ ) {
                relations.add( tableRelations[j] );
            }
            ft = pt.getFeatureTypeReference().getFeatureType();
        }
        return relations;
    }

    /**
     * Replaces the specified feature's property with the given value.
     * 
     * @param fid
     * @param ft
     * @param pt
     * @param replacementValue
     * @throws DatastoreException
     */
    private void updateProperty( FeatureId fid, MappedFeatureType ft, MappedPropertyType pt, Object replacementValue,
                                 Hashtable<String, Hashtable<FeatureId, StatementBuffer>> statementBuffers )
                            throws DatastoreException {

        LOG.logDebug( "Updating property '" + pt.getName() + "' of feature '" + fid + "'." );
        if ( !ft.isUpdatable() ) {
            String msg = Messages.getMessage( "DATASTORE_FT_NOT_UPDATABLE", ft.getName() );
            throw new DatastoreException( msg );
        }
        TableRelation[] tablePath = pt.getTableRelations();
        if ( pt instanceof MappedSimplePropertyType ) {
            SimpleContent content = ( (MappedSimplePropertyType) pt ).getContent();
            if ( content.isUpdateable() ) {
                if ( content instanceof MappingField ) {
                    updateSimpleProperty( fid, tablePath, (MappingField) content, replacementValue, statementBuffers );
                }
            } else {
                LOG.logInfo( "Ignoring property '" + pt.getName() + "' in update - content is virtual." );
            }
        } else if ( pt instanceof MappedGeometryPropertyType ) {
            MappedGeometryPropertyType geomPt = (MappedGeometryPropertyType) pt;
            MappingGeometryField dbField = geomPt.getMappingField();
            Geometry deegreeGeometry = (Geometry) replacementValue;
            Object dbGeometry;

            int createSrsCode = dbField.getSRS();
            int targetSrsCode = -1;

            if ( deegreeGeometry.getCoordinateSystem() == null ) {
                LOG.logDebug( "No SRS information for geometry available. Assuming '" + geomPt.getSRS() + "'." );
            } else if ( !geomPt.getSRS().toString().equals( deegreeGeometry.getCoordinateSystem().getIdentifier() ) ) {
                String msg = "Insert-Transformation: geometry srs: "
                             + deegreeGeometry.getCoordinateSystem().getIdentifier() + " -> property srs: "
                             + geomPt.getSRS();
                LOG.logDebug( msg );
                if ( createSrsCode == -1 ) {
                    msg = Messages.getMessage( "DATASTORE_SRS_NOT_SPECIFIED", pt.getName(),
                                               deegreeGeometry.getCoordinateSystem(), geomPt.getSRS() );
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

            // TODO remove this Oracle hack
            if ( this.datastore.getClass().getName().contains( "OracleDatastore" ) ) {
                dbField = new MappingGeometryField( dbField.getTable(), dbField.getField(), Types.STRUCT,
                                                    dbField.getSRS() );
            }

            updateGeometryProperty( fid, tablePath, dbField, dbGeometry, targetSrsCode, statementBuffers );
        } else if ( pt instanceof FeaturePropertyType ) {
            updateProperty( fid, ft, (MappedFeaturePropertyType) pt, (Feature) replacementValue );
        } else {
            throw new DatastoreException( "Internal error: Properties with type '" + pt.getClass()
                                          + "' are not handled in UpdateHandler." );
        }
    }

    /**
     * Updates a simple property of the specified feature.
     * <p>
     * Three cases are distinguished (which all have to be handled differently):
     * <ol>
     * <li>property value stored in feature table</li>
     * <li>property value stored in property table, fk in property table</li>
     * <li>property value stored in property table, fk in feature table</li>
     * </ol>
     * 
     * @param fid
     * @param tablePath
     * @param dbField
     * @param replacementValue
     * @throws DatastoreException
     */
    private void updateSimpleProperty( FeatureId fid, TableRelation[] tablePath, MappingField dbField,
                                       Object replacementValue,
                                       Hashtable<String, Hashtable<FeatureId, StatementBuffer>> statementBuffers )
                            throws DatastoreException {

        if ( tablePath.length == 0 ) {
            updateSimpleProperty( fid, dbField, replacementValue, statementBuffers );
        } else if ( tablePath.length == 1 ) {
            TableRelation relation = tablePath[0];
            if ( tablePath[0].getFKInfo() == FK_INFO.fkIsToField ) {
                Object[] keyValues = determineKeyValues( fid, relation );
                if ( keyValues != null ) {
                    deletePropertyRows( relation, keyValues );
                }
                if ( replacementValue != null ) {
                    insertPropertyRow( relation, keyValues, dbField, replacementValue );
                }
            } else {
                Object[] oldKeyValues = determineKeyValues( fid, relation );
                Object[] newKeyValues = findOrInsertPropertyRow( relation, dbField, replacementValue );
                updateFeatureRow( fid, relation, newKeyValues );
                if ( oldKeyValues != null ) {
                    deleteOrphanedPropertyRows( relation, oldKeyValues );
                }
            }
        } else {
            throw new DatastoreException( "Updating of properties that are stored in "
                                          + "related tables using join tables is not " + "supported." );
        }
    }

    /**
     * Updates a geometry property of the specified feature.
     * <p>
     * Three cases are distinguished (which all have to be handled differently):
     * <ol>
     * <li>property value stored in feature table</li>
     * <li>property value stored in property table, fk in property table</li>
     * <li>property value stored in property table, fk in feature table</li>
     * </ol>
     * 
     * @param fid
     * @param tablePath
     * @param dbField
     * @param replacementValue
     * @param targetSrsCode
     * @throws DatastoreException
     */
    private void updateGeometryProperty( FeatureId fid, TableRelation[] tablePath, MappingGeometryField dbField,
                                         Object replacementValue, int targetSrsCode,
                                         Hashtable<String, Hashtable<FeatureId, StatementBuffer>> statementBuffers )
                            throws DatastoreException {

        if ( tablePath.length == 0 ) {
            updateGeometryProperty( fid, dbField, replacementValue, targetSrsCode, statementBuffers );
        } else if ( tablePath.length == 1 ) {
            throw new DatastoreException( "Updating of geometry properties that are stored in "
                                          + "related tables is not supported." );
        } else {
            throw new DatastoreException( "Updating of properties that are stored in "
                                          + "related tables using join tables is not " + "supported." );
        }
    }

    private void updateFeatureRow( FeatureId fid, TableRelation relation, Object[] newKeyValues )
                            throws DatastoreException {

        StatementBuffer query = new StatementBuffer();
        query.append( "UPDATE " );
        query.append( relation.getFromTable() );
        query.append( " SET " );
        MappingField[] fromFields = relation.getFromFields();
        for ( int i = 0; i < newKeyValues.length; i++ ) {
            query.append( fromFields[i].getField() );
            query.append( "=?" );
            query.addArgument( newKeyValues[i], fromFields[i].getType() );
        }
        query.append( " WHERE " );
        appendFIDWhereCondition( query, fid );

        LOG.logDebug( "Performing update: " + query.getQueryString() );

        PreparedStatement stmt = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            stmt.execute();
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error in performUpdate(): " + e.getMessage() );
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

    /**
     * Updates a simple property of the specified feature.
     * <p>
     * This method handles the case where the property is stored in the feature table itself, so a single UPDATE
     * statement is sufficient.
     * 
     * If there is already another UPDATE statement created for this feature and table than a column=value fragment is
     * added to this statement.
     * 
     * @param fid
     * @param dbField
     * @param replacementValue
     * @throws DatastoreException
     */
    private void updateSimpleProperty( FeatureId fid, MappingField dbField, Object replacementValue,
                                       Hashtable<String, Hashtable<FeatureId, StatementBuffer>> statementBuffers ) {

        Hashtable<FeatureId, StatementBuffer> featureStatementBuffers = statementBuffers.get( dbField.getTable() );
        StatementBuffer query = null;
        if ( featureStatementBuffers == null ) {
            featureStatementBuffers = new Hashtable<FeatureId, StatementBuffer>();
            statementBuffers.put( dbField.getTable(), featureStatementBuffers );
        } else {
            query = featureStatementBuffers.get( fid );
        }

        if ( query == null ) {
            query = new StatementBuffer();
            query.append( "UPDATE " );
            query.append( dbField.getTable() );
            query.append( " SET " );

            featureStatementBuffers.put( fid, query );
        } else {
            query.append( ", " );
        }

        query.append( dbField.getField() );
        query.append( "=?" );
        query.addArgument( replacementValue, dbField.getType() );
    }

    /**
     * Updates a geometry property of the specified feature.
     * <p>
     * This method handles the case where the property is stored in the feature table itself, so a single UPDATE
     * statement is sufficient.
     * 
     * If there is already another UPDATE statement created for this feature and table than a column=value fragment is
     * added to this statement.
     * 
     * @param fid
     * @param dbField
     * @param replacementValue
     * @param targetSrsCode
     * @throws DatastoreException
     */
    private void updateGeometryProperty( FeatureId fid, MappingGeometryField dbField, Object replacementValue,
                                         int targetSrsCode,
                                         Hashtable<String, Hashtable<FeatureId, StatementBuffer>> statementBuffers )
                            throws DatastoreException {

        Hashtable<FeatureId, StatementBuffer> featureStatementBuffers = statementBuffers.get( dbField.getTable() );
        StatementBuffer query = null;
        if ( featureStatementBuffers == null ) {
            featureStatementBuffers = new Hashtable<FeatureId, StatementBuffer>();
            statementBuffers.put( dbField.getTable(), featureStatementBuffers );
        } else {
            query = featureStatementBuffers.get( fid );
        }

        if ( query == null ) {
            query = new StatementBuffer();
            query.append( "UPDATE " );
            query.append( dbField.getTable() );
            query.append( " SET " );

            featureStatementBuffers.put( fid, query );
        } else {
            query.append( ", " );
        }

        query.append( dbField.getField() );
        query.append( "=" );
        String placeHolder = "?";
        if ( targetSrsCode != -1 ) {
            placeHolder = this.datastore.buildSRSTransformCall( "?", targetSrsCode );
        }
        query.append( placeHolder );
        query.addArgument( replacementValue, dbField.getType() );
    }

    /**
     * Determines the values for the key columns that are referenced by the given table relation (as from fields).
     * 
     * @param fid
     * @param relation
     * @return the values for the key columns
     * @throws DatastoreException
     */
    private Object[] determineKeyValues( FeatureId fid, TableRelation relation )
                            throws DatastoreException {

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );
        MappingField[] fromFields = relation.getFromFields();
        for ( int i = 0; i < fromFields.length; i++ ) {
            query.append( fromFields[i].getField() );
            if ( i != fromFields.length - 1 ) {
                query.append( ',' );
            }
        }
        query.append( " FROM " );
        query.append( relation.getFromTable() );
        query.append( " WHERE " );
        appendFIDWhereCondition( query, fid );

        Object[] keyValues = new Object[fromFields.length];
        LOG.logDebug( "determineKeyValues: " + query.getQueryString() );
        PreparedStatement stmt = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                for ( int i = 0; i < keyValues.length; i++ ) {
                    Object value = rs.getObject( i + 1 );
                    if ( value != null ) {
                        keyValues[i] = value;
                    } else {
                        keyValues = null;
                        break;
                    }
                }
            } else {
                LOG.logError( "Internal error. Result is empty (no rows)." );
                throw new SQLException();
            }
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error in performUpdate(): " + e.getMessage() );
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.logError( "Error closing statement: '" + e.getMessage() + "'.", e );
                }
            }
        }
        return keyValues;
    }

    private void deletePropertyRows( TableRelation relation, Object[] keyValues )
                            throws DatastoreException {

        StatementBuffer query = new StatementBuffer();
        query.append( "DELETE FROM " );
        query.append( relation.getToTable() );
        query.append( " WHERE " );
        MappingField[] toFields = relation.getToFields();
        for ( int i = 0; i < toFields.length; i++ ) {
            query.append( toFields[i].getField() );
            query.append( "=?" );
            query.addArgument( keyValues[i], toFields[i].getType() );
            if ( i != toFields.length - 1 ) {
                query.append( " AND " );
            }
        }

        PreparedStatement stmt = null;
        LOG.logDebug( "deletePropertyRows: " + query.getQueryString() );
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            stmt.execute();
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error in performUpdate(): " + e.getMessage() );
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

    private void insertPropertyRow( TableRelation relation, Object[] keyValues, MappingField dbField,
                                    Object replacementValue )
                            throws DatastoreException {

        if ( keyValues == null ) {
            if ( relation.getFromFields().length > 1 ) {
                throw new DatastoreException( "Key generation for compound keys is not supported." );
            }
            // generate new primary key
            keyValues = new Object[1];
            keyValues[0] = relation.getIdGenerator().getNewId( dsTa );
        }

        StatementBuffer query = new StatementBuffer();
        query.append( "INSERT INTO " );
        query.append( relation.getToTable() );
        query.append( " (" );
        MappingField[] toFields = relation.getToFields();
        for ( int i = 0; i < toFields.length; i++ ) {
            query.append( toFields[i].getField() );
            query.append( ',' );
        }
        query.append( dbField.getField() );
        query.append( ") VALUES (" );
        for ( int i = 0; i < toFields.length; i++ ) {
            query.append( '?' );
            query.addArgument( keyValues[i], toFields[i].getType() );
            query.append( ',' );
        }
        query.append( "?)" );
        query.addArgument( replacementValue, dbField.getType() );

        PreparedStatement stmt = null;
        LOG.logDebug( "insertPropertyRow: " + query.getQueryString() );
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            stmt.execute();
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error in performUpdate(): " + e.getMessage() );
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

    /**
     * Returns the foreign key value(s) for the row that stores the given property.
     * <p>
     * If the row already exists, the existing key is returned, otherwise a new row for the property is inserted first.
     * 
     * @param relation
     * @param dbField
     * @param replacementValue
     * @return foreign key value(s) for the row that stores the given property
     * @throws DatastoreException
     */
    private Object[] findOrInsertPropertyRow( TableRelation relation, MappingField dbField, Object replacementValue )
                            throws DatastoreException {

        Object[] keyValues = null;

        if ( dbField.getType() != Types.GEOMETRY ) {
            StatementBuffer query = new StatementBuffer();
            query.append( "SELECT " );
            MappingField[] toFields = relation.getToFields();
            for ( int i = 0; i < toFields.length; i++ ) {
                query.append( toFields[i].getField() );
                if ( i != toFields.length - 1 ) {
                    query.append( ',' );
                }
            }
            query.append( " FROM " );
            query.append( relation.getToTable() );
            query.append( " WHERE " );
            query.append( dbField.getField() );
            query.append( "=?" );
            query.addArgument( replacementValue, dbField.getType() );

            PreparedStatement stmt = null;
            LOG.logDebug( "findOrInsertPropertyRow: " + query.getQueryString() );
            try {
                stmt = this.datastore.prepareStatement( conn, query );
                ResultSet rs = stmt.executeQuery();
                if ( rs.next() ) {
                    keyValues = new Object[toFields.length];
                    for ( int i = 0; i < toFields.length; i++ ) {
                        keyValues[i] = rs.getObject( i + 1 );
                    }
                }
            } catch ( SQLException e ) {
                throw new DatastoreException( "Error in findOrInsertPropertyRow(): " + e.getMessage() );
            } finally {
                if ( stmt != null ) {
                    try {
                        stmt.close();
                    } catch ( SQLException e ) {
                        LOG.logError( "Error closing statement: '" + e.getMessage() + "'.", e );
                    }
                }
            }
            if ( keyValues != null ) {
                return keyValues;
            }
        }

        if ( relation.getToFields().length > 1 ) {
            throw new DatastoreException( "Key generation for compound keys is not supported." );
        }

        // property does not yet exist (or it's a geometry)
        keyValues = new Object[1];
        // generate new PK
        keyValues[0] = relation.getNewPK( this.dsTa );
        insertPropertyRow( relation, keyValues, dbField, replacementValue );

        return keyValues;
    }

    private void deleteOrphanedPropertyRows( TableRelation relation, Object[] keyValues )
                            throws DatastoreException {
        DeleteHandler deleteHandler = new DeleteHandler( this.dsTa, this.aliasGenerator, this.conn, this.lockId );
        deleteHandler.deleteOrphanedPropertyRows( relation, keyValues );
    }

    private void updateProperty( @SuppressWarnings("unused")
    FeatureId fid, @SuppressWarnings("unused")
    MappedFeatureType ft, @SuppressWarnings("unused")
    MappedFeaturePropertyType pt, @SuppressWarnings("unused")
    Feature replacementFeature ) {
        throw new UnsupportedOperationException( "Updating of feature properties is not implemented yet." );
    }

    private void appendFIDWhereCondition( StatementBuffer query, FeatureId fid ) {
        MappingField[] fidFields = fid.getFidDefinition().getIdFields();
        for ( int i = 0; i < fidFields.length; i++ ) {
            query.append( fidFields[i].getField() );
            query.append( "=?" );
            query.addArgument( fid.getValue( i ), fidFields[i].getType() );
            if ( i != fidFields.length - 1 ) {
                query.append( " AND " );
            }
        }
    }
}
