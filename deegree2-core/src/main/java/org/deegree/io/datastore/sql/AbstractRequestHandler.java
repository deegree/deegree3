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
package org.deegree.io.datastore.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.LockManager;
import org.deegree.io.datastore.MissingLockIdException;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLId;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.Filter;

/**
 * This abstract class implements some common SQL functionality needed by request handlers for SQL based datastores.
 *
 * @see QueryHandler
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class AbstractRequestHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractRequestHandler.class );

    /**
     * Column used for disambiguation of feature properties that contain features that have more than one concrete type.
     */
    protected static final String FT_COLUMN = "featuretype";

    /**
     * Column prefix used for disambiguation of feature properties that contain features that have more than one
     * concrete type.
     */
    protected static final String FT_PREFIX = "FT_";

    protected AbstractSQLDatastore datastore;

    protected TableAliasGenerator aliasGenerator;

    protected Connection conn;

    /**
     * Creates a new instance of <code>AbstractRequestHandler</code> from the given parameters.
     *
     * @param ds
     * @param aliasGenerator
     * @param conn
     */
    public AbstractRequestHandler( AbstractSQLDatastore ds, TableAliasGenerator aliasGenerator, Connection conn ) {
        this.datastore = ds;
        this.aliasGenerator = aliasGenerator;
        this.conn = conn;
    }

    /**
     * Determines the feature ids that are matched by the given filter.
     *
     * @param ft
     *            non-abstract feature type
     * @param filter
     *            constraints the feature instances
     * @return the feature ids that are matched by the given filter
     * @throws DatastoreException
     */
    public List<FeatureId> determineAffectedFIDs( MappedFeatureType ft, Filter filter )
                            throws DatastoreException {

        assert !ft.isAbstract();

        TableAliasGenerator aliasGenerator = new TableAliasGenerator();
        VirtualContentProvider vcProvider = new VirtualContentProvider( filter, this.datastore, this.conn );
        WhereBuilder whereBuilder = this.datastore.getWhereBuilder( new MappedFeatureType[] { ft }, null, filter, null,
                                                                    aliasGenerator, vcProvider );

        // if no filter is given
        StatementBuffer query = buildInitialFIDSelect( ft, whereBuilder );
        LOG.logDebug( "Determine affected feature id query: '" + query + "'" );

        List<FeatureId> fids = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            rs = stmt.executeQuery();
            fids = extractFeatureIds( rs, ft );
        } catch ( SQLException e ) {
            throw new DatastoreException( "Error while determining affected features of type: '" + ft.getName() + "': "
                                          + e.getMessage() );
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
        return fids;
    }

    /**
     * Determines the feature ids that are matched by the given filter and that are either not locked or locked by the
     * specified lockId.
     *
     * @param ft
     *            non-abstract feature type
     * @param filter
     *            constraints the feature instances
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return the feature ids that are matched by the given filter
     * @throws DatastoreException
     */
    public List<FeatureId> determineAffectedAndModifiableFIDs( MappedFeatureType ft, Filter filter, String lockId )
                            throws DatastoreException {

        List<FeatureId> affectedFids = determineAffectedFIDs( ft, filter );
        List<FeatureId> modifiableFids = new ArrayList<FeatureId>( affectedFids.size() );
        for ( FeatureId fid : affectedFids ) {
            String lockedBy = LockManager.getInstance().getLockId( fid );
            if ( lockedBy != null && !lockedBy.equals( lockId ) ) {
                String msg = Messages.getMessage( "DATASTORE_FEATURE_NOT_MODIFIABLE", fid, lockedBy );
                throw new MissingLockIdException( msg );
            }

            modifiableFids.add( fid );
        }
        return modifiableFids;
    }

    /**
     * Determines all complex properties and contained subfeature ids for a certain feature.
     *
     * @param fid
     *            id of the feature
     * @return all complex properties and contained subfeature ids of the feature
     * @throws DatastoreException
     */
    public Map<MappedFeaturePropertyType, List<FeatureId>> determineSubFeatures( FeatureId fid )
                            throws DatastoreException {

        LOG.logDebug( "Determining sub features of feature '" + fid + "'..." );
        Map<MappedFeaturePropertyType, List<FeatureId>> ptToSubFids = new HashMap<MappedFeaturePropertyType, List<FeatureId>>();
        PropertyType[] properties = fid.getFeatureType().getProperties();
        for ( PropertyType property : properties ) {
            MappedPropertyType pt = (MappedPropertyType) property;
            if ( pt instanceof MappedFeaturePropertyType ) {
                LOG.logDebug( "Complex property '" + pt.getName() + "'..." );
                MappedFeaturePropertyType fPt = (MappedFeaturePropertyType) pt;
                List<FeatureId> subFids = determineSubFIDs( fid, fPt );
                ptToSubFids.put( fPt, subFids );
            }
        }
        return ptToSubFids;
    }

    /**
     * Determines the {@link FeatureId}s of the subfeatures contained in a specified feature's property.
     *
     * @param fid
     *            id of the feature (for which the subfeatures will be determined)
     * @param pt
     *            property type of the feature (that contains the subfeatures)
     * @return the matched subfeature's ids (with concrete feature types)
     * @throws DatastoreException
     */
    private List<FeatureId> determineSubFIDs( FeatureId fid, MappedFeaturePropertyType pt )
                            throws DatastoreException {

        LOG.logDebug( "Determining sub feature ids for feature: " + fid + " and property " + pt.getName() );

        List<FeatureId> subFids = null;
        MappedFeatureType containedFt = pt.getFeatureTypeReference().getFeatureType();
        MappedFeatureType[] concreteFts = containedFt.getConcreteSubstitutions();
        if ( concreteFts.length > 1 ) {
            subFids = determineSubFIDs( fid, pt, concreteFts );
        } else {
            subFids = determineSubFIDs( fid, pt, containedFt );
        }
        return subFids;
    }

    /**
     * Determines all super features (as {@link FeatureId} instances) for a certain feature.
     *
     * @param fid
     *            id of the feature
     * @return all super feature ids of the feature
     * @throws DatastoreException
     */
    public Set<FeatureId> determineSuperFeatures( FeatureId fid )
                            throws DatastoreException {

        LOG.logDebug( "Determining super features of feature " + fid.getAsString() );
        Set<FeatureId> superFeatures = new HashSet<FeatureId>();
        MappedFeatureType subFt = fid.getFeatureType();
        Set<FeatureType> substitutableFts = subFt.getGMLSchema().getSubstitutables( subFt );
        Set<MappedFeatureType> superFts = determineSuperFeatureTypes( substitutableFts );

        for ( MappedFeatureType superFt : superFts ) {
            List<MappedFeaturePropertyType> featureProps = determineProperties( superFt, subFt );
            for ( MappedFeaturePropertyType featureProp : featureProps ) {
                superFeatures.addAll( determineSuperFids( superFt, featureProp, fid ) );
            }
        }
        return superFeatures;
    }

    /**
     * Determines all concrete feature types that can contain one or more of the given feature types inside a property.
     *
     * @param subFts
     * @return all concrete feature types that can contain the given feature type
     */
    private Set<MappedFeatureType> determineSuperFeatureTypes( Set<FeatureType> subFts ) {
        Set<MappedFeatureType> superFts = new HashSet<MappedFeatureType>();
        for ( FeatureType subFt : subFts ) {
            superFts.addAll( determineSuperFeatureTypes( (MappedFeatureType) subFt ) );
        }
        return superFts;
    }

    /**
     * Determines all concrete feature types that can contain the given feature type inside a property.
     *
     * @param subFt
     * @return all concrete feature types that can contain the given feature type
     */
    private Set<MappedFeatureType> determineSuperFeatureTypes( MappedFeatureType subFt ) {
        Set<MappedFeatureType> superFts = new HashSet<MappedFeatureType>();
        MappedGMLSchema schema = subFt.getGMLSchema();
        FeatureType[] fts = schema.getFeatureTypes();
        for ( int i = 0; i < fts.length; i++ ) {
            MappedFeatureType ft = (MappedFeatureType) fts[i];
            if ( !ft.isAbstract() ) {
                PropertyType[] properties = ft.getProperties();
                for ( int j = 0; j < properties.length; j++ ) {
                    MappedPropertyType property = (MappedPropertyType) properties[j];
                    if ( property instanceof MappedFeaturePropertyType ) {
                        MappedFeaturePropertyType ftProperty = (MappedFeaturePropertyType) property;
                        if ( ftProperty.getFeatureTypeReference().getName().equals( subFt.getName() ) ) {
                            superFts.add( ft );
                        }
                    }
                }
            }

        }
        return superFts;
    }

    /**
     * Determines all {@link MappedFeaturePropertyType} instances that the super feature type has and which contain
     * features that may be substituted for features of the given sub feature type.
     *
     * @param superFt
     * @param subFt
     * @return corresponding property types
     */
    private List<MappedFeaturePropertyType> determineProperties( MappedFeatureType superFt, MappedFeatureType subFt ) {
        List<MappedFeaturePropertyType> featureProps = new ArrayList<MappedFeaturePropertyType>();
        PropertyType[] properties = superFt.getProperties();
        for ( PropertyType property : properties ) {
            if ( property instanceof MappedFeaturePropertyType ) {
                MappedFeaturePropertyType featureProperty = (MappedFeaturePropertyType) property;
                MappedFeatureType containedFt = featureProperty.getFeatureTypeReference().getFeatureType();
                if ( subFt.getGMLSchema().isValidSubstitution( containedFt, subFt ) ) {
                    featureProps.add( featureProperty );
                }
            }
        }
        return featureProps;
    }

    /**
     * Determines all features (as {@link FeatureId}s) of the super feature type which contain the given feature
     * instance in the also specified property.
     *
     * @param superFt
     * @param featureProp
     * @param subFid
     * @return corresponding <code>DeleteNodes</code>
     */
    private List<FeatureId> determineSuperFids( MappedFeatureType superFt, MappedFeaturePropertyType featureProp,
                                                FeatureId subFid )
                            throws DatastoreException {
        this.aliasGenerator.reset();
        TableRelation[] relations = featureProp.getTableRelations();

        String superFtAlias = this.aliasGenerator.generateUniqueAlias();
        String[] joinTableAliases = this.aliasGenerator.generateUniqueAliases( relations.length );
        String subFtAlias = joinTableAliases[joinTableAliases.length - 1];

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT DISTINCT " );
        appendFeatureIdColumns( superFt, superFtAlias, query );
        query.append( " FROM " );
        query.append( superFt.getTable() );
        query.append( " " );
        query.append( superFtAlias );
        String fromAlias = superFtAlias;
        for ( int i = 0; i < relations.length; i++ ) {
            String toAlias = joinTableAliases[i];
            query.append( " JOIN " );
            if ( i == relations.length - 1 ) {
                query.append( subFid.getFeatureType().getTable() );
            } else {
                query.append( relations[i].getToTable() );
            }
            query.append( " " );
            query.append( toAlias );
            query.append( " ON " );
            appendJoinCondition( relations[i], fromAlias, toAlias, query );
            fromAlias = toAlias;
        }

        query.append( " WHERE " );
        MappedGMLId gmlId = subFid.getFidDefinition();
        MappingField[] idFields = gmlId.getIdFields();
        for ( int i = 0; i < idFields.length; i++ ) {
            query.append( subFtAlias );
            query.append( '.' );
            query.append( idFields[i].getField() );
            query.append( "=?" );
            query.addArgument( subFid.getValue( i ), idFields[i].getType() );
            if ( i != idFields.length - 1 ) {
                query.append( " AND " );
            }
        }

        List<FeatureId> fids = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Performing: " + query );
            rs = stmt.executeQuery();

            // TODO workaround for RO-Online
            fids = extractFeatureIdsForceUnique( rs, superFt );
        } catch ( SQLException e ) {
            LOG.logInfo( e.getMessage(), e );
            throw new DatastoreException( "Error in determineSuperFeatures(): " + e.getMessage() );
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
        return fids;
    }

    /**
     * Determines the {@link FeatureId}s of the subfeatures contained in the given feature property.
     *
     * @param fid
     *            id of the feature
     * @param pt
     *            table relation from the feature table to the subfeature table
     * @param concreteFt
     *            concrete (non-abstract) type that is contained in the feature property
     * @return the <code>FeatureId</code> or null (if there is no such subfeature)
     * @throws DatastoreException
     */
    private List<FeatureId> determineSubFIDs( FeatureId fid, MappedFeaturePropertyType pt, MappedFeatureType concreteFt )
                            throws DatastoreException {

        TableRelation[] relations = pt.getTableRelations();

        this.aliasGenerator.reset();
        String[] aliases = this.aliasGenerator.generateUniqueAliases( relations.length + 1 );

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );
        appendFeatureIdColumns( concreteFt, aliases[aliases.length - 1], query );
        query.append( " FROM " );
        query.append( relations[0].getFromTable() );
        query.append( " " );
        query.append( aliases[0] );

        // append JOINs
        String fromAlias = aliases[0];
        for ( int i = 0; i < relations.length; i++ ) {
            String toAlias = aliases[i + 1];
            query.append( " JOIN " );
            if ( i == relations.length - 1 ) {
                query.append( concreteFt.getTable() );
            } else {
                query.append( relations[i].getToTable() );
            }
            query.append( " " );
            query.append( toAlias );
            query.append( " ON " );
            appendJoinCondition( relations[i], fromAlias, toAlias, query );
            fromAlias = toAlias;
        }

        query.append( " WHERE " );
        appendFeatureIdConstraint( query, fid, aliases[0] );

        List<FeatureId> subFids = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Determining subfeature ids: " + query );
            rs = stmt.executeQuery();
            subFids = extractFeatureIds( rs, concreteFt );
        } catch ( SQLException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new DatastoreException( "Error in #determineSubFIDs(): " + e.getMessage() );
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
     * Determines the feature ids of the subfeatures contained in the given feature property (that may contain features
     * of different concrete types).
     *
     * @param fid
     *            id of the feature
     * @param pt
     *            complex property that contains the subfeatures
     * @param concreteSubFts
     *            all possible non-abstract feature types of the subfeatures
     * @return the ids of the subfeatures
     * @throws DatastoreException
     */
    private List<FeatureId> determineSubFIDs( FeatureId fid, MappedFeaturePropertyType pt,
                                              MappedFeatureType[] concreteSubFts )
                            throws DatastoreException {

        List<FeatureId> subFids = null;

        TableRelation[] relations = pt.getTableRelations();
        LOG.logDebug( "Determining sub feature ids for feature " + fid + ": relations.length: " + relations.length );

        switch ( relations.length ) {
        case 1: {
            // subfeature disambiguator in feature table (only zero or one subfeatures)
            MappedFeatureType concreteSubFt = determineSubFt( fid, pt, concreteSubFts );
            subFids = new ArrayList<FeatureId>( 1 );
            if ( concreteSubFt != null ) {
                FeatureId subFid = determineSubFID( fid, relations[0], concreteSubFt );
                if ( subFid != null ) {
                    subFids.add( subFid );
                }
            }
            break;
        }
        case 2: {
            // subfeature disambiguator in join table (any number of subfeatures)
            subFids = determineSubFIDs( fid, pt, concreteSubFts, relations );
            break;
        }
        default: {
            String msg = Messages.getMessage( "DATASTORE_SUBFT_TOO_MANY_RELATIONS", fid.getFeatureType().getName(),
                                              pt.getName() );
            throw new DatastoreException( msg );
        }
        }
        return subFids;
    }

    /**
     * Determine the concrete type of the subfeature that is stored in the specified property of a certain feature.
     * <p>
     * The relation to the sub feature table must be specified via a single step (join).
     *
     * @param fid
     *            id of the feature for which the concrete subfeature type is needed
     * @param pt
     *            property of the feature that contains the subfeature
     * @param concreteSubFts
     *            concrete types that may be contained in the property
     * @return concrete type of the subfeature, or null if feature has no such property
     * @throws DatastoreException
     */
    private MappedFeatureType determineSubFt( FeatureId fid, MappedFeaturePropertyType pt,
                                              MappedFeatureType[] concreteSubFts )
                            throws DatastoreException {

        assert ( pt.getTableRelations().length == 1 );
        TableRelation relation = pt.getTableRelations()[0];

        assert ( relation.getFromFields().length == 1 );
        String fkColumn = relation.getFromFields()[0].getField();
        String subFtColumn = FT_PREFIX + fkColumn;

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );
        query.append( subFtColumn );
        query.append( " FROM " );
        query.append( fid.getFeatureType().getTable() );
        query.append( " WHERE " );
        appendFeatureIdConstraint( query, fid );

        String localSubFtName = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Determining concrete subfeature type: " + query );
            rs = stmt.executeQuery();
            rs.next();
            localSubFtName = rs.getString( 1 );
        } catch ( SQLException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new DatastoreException( "Error in determineConcreteSubFt() " + e.getMessage() );
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

        MappedFeatureType concreteSubFt = null;

        if ( localSubFtName != null ) {
            for ( MappedFeatureType type : concreteSubFts ) {
                if ( type.getName().getLocalName().equals( localSubFtName ) ) {
                    concreteSubFt = fid.getFeatureType().getGMLSchema().getFeatureType( localSubFtName );
                    break;
                }
            }
            if ( concreteSubFt == null ) {
                String msg = Messages.getMessage( "DATASTORE_FEATURE_TYPE_INFO_INCONSISTENT", pt.getName(), fid,
                                                  subFtColumn, localSubFtName, pt.getFeatureTypeReference().getName() );
                throw new DatastoreException( msg );
            }
        }

        return concreteSubFt;
    }

    /**
     * Determines the {@link FeatureId} of the subfeature contained in the given feature property (if the feature has
     * such a subfeature).
     *
     * @param fid
     *            id of the feature
     * @param relation
     *            table relation from the feature table to the subfeature table
     * @param concreteFt
     *            concrete (non-abstract) type that is contained in the feature property
     * @return the <code>FeatureId</code> or null (if there is no such subfeature)
     * @throws DatastoreException
     */
    private FeatureId determineSubFID( FeatureId fid, TableRelation relation, MappedFeatureType concreteFt )
                            throws DatastoreException {

        this.aliasGenerator.reset();
        String fromAlias = this.aliasGenerator.generateUniqueAlias();
        String toAlias = this.aliasGenerator.generateUniqueAlias();

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );
        appendFeatureIdColumns( concreteFt, toAlias, query );
        query.append( " FROM " );
        query.append( relation.getFromTable() );
        query.append( " " );
        query.append( fromAlias );
        query.append( " JOIN " );
        query.append( concreteFt.getTable() );
        query.append( " " );
        query.append( toAlias );
        query.append( " ON " );
        appendJoinCondition( relation, fromAlias, toAlias, query );
        query.append( " WHERE " );
        appendFeatureIdConstraint( query, fid, fromAlias );

        FeatureId subFid = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Determining subfeature id: " + query );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                subFid = extractFeatureId( rs, concreteFt );
            }
        } catch ( SQLException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new DatastoreException( "Error in #determineSubFID(): " + e.getMessage() );
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
        return subFid;
    }

    /**
     * Determines the feature ids of the subfeatures contained in the given feature property (that may contain features
     * of different concrete types and is connected via a join table with feature type disambiguation column).
     *
     * @param fid
     * @param pt
     * @return the matched subfeatures' ids
     * @throws DatastoreException
     */
    private List<FeatureId> determineSubFIDs( FeatureId fid, MappedFeaturePropertyType pt,
                                              MappedFeatureType[] concreteSubFts, TableRelation[] relations )
                            throws DatastoreException {
        this.aliasGenerator.reset();
        String fromAlias = this.aliasGenerator.generateUniqueAlias();
        String jtAlias = this.aliasGenerator.generateUniqueAlias();

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );

        // select feature type disambiguation column and from fields of second table relation
        appendQualifiedColumn( query, jtAlias, FT_COLUMN );
        MappingField[] fromFields = relations[1].getFromFields();
        for ( int i = 0; i < fromFields.length; i++ ) {
            query.append( ',' );
            appendQualifiedColumn( query, jtAlias, fromFields[i].getField() );
        }

        query.append( " FROM " );
        query.append( relations[0].getFromTable() );
        query.append( " " );
        query.append( fromAlias );
        query.append( " JOIN " );
        query.append( relations[0].getToTable() );
        query.append( " " );
        query.append( jtAlias );
        query.append( " ON " );
        appendJoinCondition( relations[0], fromAlias, jtAlias, query );
        query.append( " WHERE " );
        appendFeatureIdConstraint( query, fid, fromAlias );

        List<FeatureId> subFids = new ArrayList<FeatureId>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Determining concrete subfeature types and join keys: " + query );
            rs = stmt.executeQuery();
            Object[] keyComponents = new Object[relations[1].getFromFields().length];
            while ( rs.next() ) {
                String localSubFtName = rs.getString( 1 );
                for ( int i = 0; i < keyComponents.length; i++ ) {
                    keyComponents[i] = rs.getObject( i + 2 );
                }
                MappedFeatureType concreteSubFt = null;
                for ( MappedFeatureType type : concreteSubFts ) {
                    if ( type.getName().getLocalName().equals( localSubFtName ) ) {
                        concreteSubFt = fid.getFeatureType().getGMLSchema().getFeatureType( localSubFtName );
                        break;
                    }
                }
                if ( concreteSubFt == null ) {
                    String msg = Messages.getMessage( "DATASTORE_FEATURE_TYPE_INFO_INCONSISTENT", pt.getName(), fid,
                                                      FT_COLUMN, localSubFtName, pt.getFeatureTypeReference().getName() );
                    throw new DatastoreException( msg );
                }

                subFids.add( determineSubFID( concreteSubFt, relations[1], keyComponents ) );
            }
        } catch ( SQLException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new DatastoreException( "Error in #determineSubFIDs(): " + e.getMessage() );
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
     * Determines the {@link FeatureId} of the subfeature referenced by the given {@link TableRelation}.
     *
     * @param concreteSubFt
     *            concrete (non-abstract) type that is contained in the feature property
     * @param relation
     *            table relation from the join table to the subfeature table
     * @param keyComponents
     * @return the <code>FeatureId</code> or null (if there is no such subfeature)
     * @throws DatastoreException
     */
    private FeatureId determineSubFID( MappedFeatureType concreteSubFt, TableRelation relation, Object[] keyComponents )
                            throws DatastoreException {
        this.aliasGenerator.reset();
        String fromAlias = this.aliasGenerator.generateUniqueAlias();
        String toAlias = this.aliasGenerator.generateUniqueAlias();

        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );
        appendFeatureIdColumns( concreteSubFt, toAlias, query );
        query.append( " FROM " );
        query.append( relation.getFromTable() );
        query.append( " " );
        query.append( fromAlias );
        query.append( " JOIN " );
        query.append( concreteSubFt.getTable() );
        query.append( " " );
        query.append( toAlias );
        query.append( " ON " );
        appendJoinCondition( relation, fromAlias, toAlias, query );
        query.append( " WHERE " );
        for ( int i = 0; i < keyComponents.length; i++ ) {
            appendQualifiedColumn( query, fromAlias, relation.getFromFields()[i].getField() );
            query.append( "=?" );
            query.addArgument( keyComponents[i], relation.getFromFields()[i].getType() );
            if ( i != keyComponents.length - 1 ) {
                query.append( " AND " );
            }
        }
        FeatureId subFid = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.datastore.prepareStatement( conn, query );
            LOG.logDebug( "Determining subfeature id: " + query );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                subFid = extractFeatureId( rs, concreteSubFt );
            }
        } catch ( SQLException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new DatastoreException( "Error in #determineSubFID(): " + e.getMessage() );
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
        return subFid;
    }

    /**
     * Builds the initial SELECT statement that retrieves the feature ids that are matched by the given
     * <code>WhereBuilder</code>.
     * <p>
     * The statement is structured like this:
     * <ul>
     * <li><code>SELECT</code></li>
     * <li>comma-separated list of qualified fid fields</li>
     * <li><code>FROM</code></li>
     * <li>comma-separated list of tables and their aliases (this is needed to constrain the paths to selected
     * XPath-PropertyNames)</li>
     * <li><code>WHERE</code></li>
     * <li>SQL representation of the Filter expression</li>
     * </ul>
     *
     * @param rootFt
     * @param whereBuilder
     * @return initial SELECT statement to retrieve the feature ids
     * @throws DatastoreException
     */
    private StatementBuffer buildInitialFIDSelect( MappedFeatureType rootFt, WhereBuilder whereBuilder )
                            throws DatastoreException {

        String tableAlias = whereBuilder.getRootTableAlias( 0 );
        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT " );
        appendFeatureIdColumns( rootFt, tableAlias, query );
        query.append( " FROM " );
        whereBuilder.appendJoinTableList( query );
        whereBuilder.appendWhereCondition( query );
        return query;
    }

    /**
     * Appends the alias qualified columns that make up the feature id to the given query.
     *
     * @param featureType
     * @param tableAlias
     * @param query
     */
    protected void appendFeatureIdColumns( MappedFeatureType featureType, String tableAlias, StatementBuffer query ) {
        MappingField[] fidFields = featureType.getGMLId().getIdFields();
        for ( int i = 0; i < fidFields.length; i++ ) {
            query.append( tableAlias );
            query.append( '.' );
            query.append( fidFields[i].getField() );
            if ( i != fidFields.length - 1 ) {
                query.append( ',' );
            }
        }
    }

    /**
     * Extracts the FeatureId in the current row of the given {@link ResultSet}.
     *
     * @param rs
     * @param ft
     *            feature type (may not be abstract)
     * @return feature ids
     * @throws SQLException
     * @throws DatastoreException
     */
    protected FeatureId extractFeatureId( ResultSet rs, MappedFeatureType ft )
                            throws SQLException, DatastoreException {

        MappedGMLId gmlId = ft.getGMLId();
        MappingField[] idFields = gmlId.getIdFields();

        Object[] idValues = new Object[idFields.length];
        for ( int i = 0; i < idValues.length; i++ ) {
            Object idValue = rs.getObject( i + 1 );
            if ( idValue == null ) {
                String msg = Messages.getMessage( "DATASTORE_FEATURE_ID_NULL", ft.getTable(), ft.getName(),
                                                  idFields[i].getField() );
                throw new DatastoreException( msg );
            }
            idValues[i] = idValue;
        }

        return new FeatureId( ft, idValues );
    }

    /**
     * Extracts the feature ids in the given {@link ResultSet} as a List of FeatureIds.
     * <p>
     * If the given feature type is abstract, it is expected that the first column of the result set contains the local
     * name of the feature type.
     *
     * @param rs
     * @param ft
     *            feature type
     * @return feature ids
     * @throws SQLException
     * @throws DatastoreException
     */
    protected List<FeatureId> extractFeatureIdsForceUnique( ResultSet rs, MappedFeatureType ft )
                            throws SQLException, DatastoreException {
        List<FeatureId> featureIdList = new ArrayList<FeatureId>();
        MappedGMLId gmlId = ft.getGMLId();
        MappingField[] idFields = gmlId.getIdFields();

        boolean needsDisambiguation = false;

        while ( rs.next() ) {
            int offset = 1;
            if ( needsDisambiguation ) {
                String localFtName = rs.getString( 1 );
                ft = ft.getGMLSchema().getFeatureType( localFtName );
                gmlId = ft.getGMLId();
                idFields = gmlId.getIdFields();
                offset = 2;
            }
            Object[] idValues = new Object[idFields.length];
            for ( int i = 0; i < idValues.length; i++ ) {
                Object idValue = rs.getObject( i + offset );
                if ( idValue == null ) {
                    String msg = Messages.getMessage( "DATASTORE_FEATURE_ID_NULL", ft.getTable(), ft.getName(),
                                                      idFields[i].getField() );
                    throw new DatastoreException( msg );
                }
                idValues[i] = idValue;
            }
            featureIdList.add( new FeatureId( ft, idValues ) );
        }
        return featureIdList;
    }

    /**
     * Extracts the feature ids in the given {@link ResultSet} as a List of FeatureIds.
     * <p>
     * If the given feature type is abstract, it is expected that the first column of the result set contains the local
     * name of the feature type.
     *
     * @param rs
     * @param ft
     *            feature type (may be abstract)
     * @return feature ids
     * @throws SQLException
     * @throws DatastoreException
     */
    protected List<FeatureId> extractFeatureIds( ResultSet rs, MappedFeatureType ft )
                            throws SQLException, DatastoreException {
        List<FeatureId> featureIdList = new ArrayList<FeatureId>();
        MappedGMLId gmlId = ft.getGMLId();
        MappingField[] idFields = gmlId.getIdFields();

        boolean needsDisambiguation = ft.hasSeveralImplementations();

        while ( rs.next() ) {
            int offset = 1;
            if ( needsDisambiguation ) {
                String localFtName = rs.getString( 1 );
                ft = ft.getGMLSchema().getFeatureType( localFtName );
                gmlId = ft.getGMLId();
                idFields = gmlId.getIdFields();
                offset = 2;
            }
            Object[] idValues = new Object[idFields.length];
            for ( int i = 0; i < idValues.length; i++ ) {
                Object idValue = rs.getObject( i + offset );
                if ( idValue == null ) {
                    String msg = Messages.getMessage( "DATASTORE_FEATURE_ID_NULL", ft.getTable(), ft.getName(),
                                                      idFields[i].getField() );
                    throw new DatastoreException( msg );
                }
                idValues[i] = idValue;
            }
            featureIdList.add( new FeatureId( ft, idValues ) );
        }
        return featureIdList;
    }

    protected void appendJoins( TableRelation[] tableRelation, String fromAlias, String[] toAliases,
                                StatementBuffer query ) {
        for ( int i = 0; i < toAliases.length; i++ ) {
            String toAlias = toAliases[i];
            appendJoin( tableRelation[i], fromAlias, toAlias, query );
            fromAlias = toAlias;
        }
    }

    private void appendJoin( TableRelation tableRelation, String fromAlias, String toAlias, StatementBuffer query ) {
        query.append( " JOIN " );
        query.append( tableRelation.getToTable() );
        query.append( " " );
        query.append( toAlias );
        query.append( " ON " );
        appendJoinCondition( tableRelation, fromAlias, toAlias, query );
    }

    protected void appendJoinCondition( TableRelation tableRelation, String fromAlias, String toAlias,
                                        StatementBuffer query ) {

        MappingField[] fromFields = tableRelation.getFromFields();
        MappingField[] toFields = tableRelation.getToFields();
        for ( int i = 0; i < fromFields.length; i++ ) {
            query.append( toAlias );
            query.append( "." );
            query.append( toFields[i].getField() );
            query.append( "=" );
            query.append( fromAlias );
            query.append( "." );
            query.append( fromFields[i].getField() );
            if ( i != fromFields.length - 1 ) {
                query.append( " AND " );
            }
        }
    }

    protected void appendFeatureIdConstraint( StatementBuffer query, FeatureId fid ) {
        MappingField[] idFields = fid.getFidDefinition().getIdFields();
        for ( int i = 0; i < idFields.length; i++ ) {
            query.append( idFields[i].getField() );
            query.append( "=?" );
            query.addArgument( fid.getValue( i ), idFields[i].getType() );
            if ( i < idFields.length - 1 ) {
                query.append( " AND " );
            }
        }
    }

    protected void appendFeatureIdConstraint( StatementBuffer query, FeatureId fid, String tableAlias ) {
        MappingField[] idFields = fid.getFidDefinition().getIdFields();
        for ( int i = 0; i < idFields.length; i++ ) {
            query.append( tableAlias );
            query.append( '.' );
            query.append( idFields[i].getField() );
            query.append( "=?" );
            query.addArgument( fid.getValue( i ), idFields[i].getType() );
            if ( i < idFields.length - 1 ) {
                query.append( " AND " );
            }
        }
    }

    /**
     * Appends the specified columns as a comma-separated list to the given query.
     *
     * @param query
     *            StatementBuffer that the list is appended to
     * @param columns
     *            array of column names
     */
    public void appendColumnsList( StatementBuffer query, String[] columns ) {
        for ( int i = 0; i < columns.length; i++ ) {
            if ( columns[i].indexOf( '$' ) != -1 ) {
                // function call
                String column = columns[i];
                column = column.replaceAll( "\\$\\.", "" );
                query.append( column );

            } else {
                query.append( columns[i] );
            }

            if ( i != columns.length - 1 ) {
                query.append( ',' );
            }
        }
    }

    /**
     * Appends the specified columns as alias-qualified, comma-separated list to the given query.
     *
     * @param query
     *            StatementBuffer that the list is appended to
     * @param tableAlias
     *            alias to use as qualifier (alias.field)
     * @param columns
     *            array of column names
     */
    public void appendQualifiedColumnsList( StatementBuffer query, String tableAlias, String[] columns ) {
        for ( int i = 0; i < columns.length; i++ ) {
            appendQualifiedColumn( query, tableAlias, columns[i] );
            if ( i != columns.length - 1 ) {
                query.append( ',' );
            }
        }
    }

    /**
     * Appends the specified column to the given query.
     *
     * @param query
     *            StatementBuffer that the list is appended to
     * @param tableAlias
     *            alias to use as qualifier (alias.field)
     * @param column
     *            column name
     */
    public void appendQualifiedColumn( StatementBuffer query, String tableAlias, String column ) {
        query.append( tableAlias );
        query.append( '.' );
        query.append( column );
    }
}
