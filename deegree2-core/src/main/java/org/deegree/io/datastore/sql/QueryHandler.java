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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.wherebuilder.QueryTableTree;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.FeatureTupleCollection;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;

/**
 * Handles {@link Query} requests to SQL backed datastores.
 * 
 * @see FeatureFetcher
 * @see AbstractSQLDatastore
 * @see QueryTableTree
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class QueryHandler extends FeatureFetcher {

    private static final ILogger LOG = LoggerFactory.getLogger( QueryHandler.class );

    private Query query;

    // targeted feature types, more than one implies that a join of feature types is requested
    private MappedFeatureType[] rootFts;

    // used to build the initial SELECT (especially the WHERE-clause)
    private WhereBuilder whereBuilder;

    // TODO hack for making gml:boundedBy available even if the geometry properties are not queried
    private List<PropertyName> augmentedGeoProps;

    /**
     * Creates a new instance of <code>QueryHandler</code> from the given parameters.
     * 
     * @param ds
     *            datastore that spawned this QueryHandler
     * @param aliasGenerator
     *            used to generate unique aliases for the tables in the SELECT statements
     * @param conn
     *            JDBCConnection to execute the generated SELECT statements against
     * @param rootFts
     *            the root feature types that are queried, more than one type means that the types are joined
     * @param query
     *            query to perform
     * @throws DatastoreException
     */
    public QueryHandler( AbstractSQLDatastore ds, TableAliasGenerator aliasGenerator, Connection conn,
                         MappedFeatureType[] rootFts, Query query ) throws DatastoreException {

        super( ds, aliasGenerator, conn, query );

        this.query = query;
        this.rootFts = rootFts;
        this.vcProvider = new VirtualContentProvider( query.getFilter(), ds, conn );
        this.whereBuilder = this.datastore.getWhereBuilder( rootFts, query.getAliases(), query.getFilter(),
                                                            query.getSortProperties(), aliasGenerator, this.vcProvider );
        this.aliasGenerator = aliasGenerator;
    }

    /**
     * Performs the associated {@link Query} against the datastore.
     * 
     * @return collection of requested features
     * @throws SQLException
     *             if a JDBC error occurs
     * @throws DatastoreException
     * @throws UnknownCRSException
     */
    public FeatureCollection performQuery()
                            throws SQLException, DatastoreException, UnknownCRSException {

        long start = -1;
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            start = System.currentTimeMillis();
        }

        FeatureCollection result = null;

        if ( this.query.getResultType() == RESULT_TYPE.HITS ) {
            result = performHitsQuery();
        } else {
            result = performResultsQuery();
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            long elapsed = System.currentTimeMillis() - start;
            LOG.logDebug( "Performing of query took " + elapsed + " milliseconds." );
        }

        return result;
    }

    /**
     * Performs a query for the feature instances that match the filter constraints. This corresponds to a query with
     * resultType=RESULTS.
     * 
     * @return collection of requested features
     * @throws PropertyPathResolvingException
     * @throws SQLException
     * @throws DatastoreException
     * @throws UnknownCRSException
     */
    private FeatureCollection performResultsQuery()
                            throws PropertyPathResolvingException, SQLException, DatastoreException,
                            UnknownCRSException {

        FeatureCollection result = null;

        SelectManager selectManager = new SelectManager( query, this.rootFts, this );
        LOG.logDebug( "SelectManager: " + selectManager );

        // build initial SQL query
        StatementBuffer querybuf = buildInitialSelect( selectManager );
        LOG.logDebug( "Initial query: '" + querybuf + "'" );

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.datastore.prepareStatement( this.conn, querybuf );
            rs = stmt.executeQuery();
            if ( this.rootFts.length == 1 ) {
                result = performSimpleResultsQuery( rs, selectManager );
            } else {
                result = performJoinResultsQuery( rs, selectManager );
            }
        } finally {
            try {
                if ( rs != null ) {
                    rs.close();
                }
            } finally {
                if ( stmt != null ) {
                    stmt.close();
                }
            }
        }

        resolveFeatureReferences();
        result.setAttribute( "numberOfFeatures", "" + result.size() );
        return result;
    }

    private FeatureCollection performSimpleResultsQuery( ResultSet rs, SelectManager selectManager )
                            throws DatastoreException, SQLException, UnknownCRSException {

        MappedFeatureType rootFt = this.rootFts[0];
        Map<MappedPropertyType, Collection<PropertyPath>> requestedPropertyMap = selectManager.getAllFetchProps()[0];
        Map<SimpleContent, Integer> resultPosMap = selectManager.getResultPosMaps()[0];

        FeatureCollection result = FeatureFactory.createFeatureCollection( "ID", 10000 );
        Object[] resultValues = new Object[selectManager.getFetchContentCount()];

        // used to handle that a feature may occur several times in result set
        Set<FeatureId> rootFeatureIds = new HashSet<FeatureId>();

        // skip features in resultSet (startPosition is first feature to be included)
        int startPosition = this.query.getStartPosition();
        Set<FeatureId> skippedFeatures = new HashSet<FeatureId>();
        while ( skippedFeatures.size() < startPosition - 1 && rs.next() ) {
            LOG.logDebug( "Skipping result row." );
            // collect result values
            for ( int i = 0; i < resultValues.length; i++ ) {
                resultValues[i] = rs.getObject( i + 1 );
            }
            FeatureId fid = extractFeatureId( rootFt, selectManager.getResultPosMaps()[0], resultValues );
            skippedFeatures.add( fid );
        }

        int maxFeatures = this.query.getMaxFeatures();
        while ( rs.next() ) {

            // already maxFeature features extracted?
            if ( maxFeatures != -1 && rootFeatureIds.size() == maxFeatures ) {
                break;
            }

            // collect result values
            for ( int i = 0; i < resultValues.length; i++ ) {
                resultValues[i] = rs.getObject( i + 1 );
            }

            FeatureId fid = extractFeatureId( rootFt, resultPosMap, resultValues );

            // skip it if this root feature has already been fetched or if it is a feature
            // (again) that has been skipped
            if ( !rootFeatureIds.contains( fid ) && !skippedFeatures.contains( fid ) ) {

                rootFeatureIds.add( fid );

                // feature may have been fetched as a subfeature already
                Feature feature = this.featureMap.get( fid );
                if ( feature == null ) {
                    feature = extractFeature( fid, requestedPropertyMap, resultPosMap, resultValues );
                    // hack that ensures that the boundedBy information is correct
                    if ( !selectManager.augmentedGeometryProps.isEmpty() ) {
                        try {
                            Envelope boundedBy = feature.getBoundedBy();
                            for ( PropertyPath unqueriedGeoProp : selectManager.augmentedGeometryProps ) {
                                LOG.logDebug( "Removing " + unqueriedGeoProp + " from feature instance." );
                                feature.removeProperty( unqueriedGeoProp.getStep( 1 ).getPropertyName() );
                            }
                            feature.setProperty(
                                                 FeatureFactory.createFeatureProperty(
                                                                                       new QualifiedName( "boundedBy" ),
                                                                                       boundedBy ), 1 );
                        } catch ( GeometryException e ) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }
                result.add( feature );
            }
        }
        return result;
    }

    private FeatureTupleCollection performJoinResultsQuery( ResultSet rs, SelectManager selectManager )
                            throws DatastoreException, SQLException, UnknownCRSException {

        List<Feature[]> resultTuples = new ArrayList<Feature[]>();

        // used to handle that a feature may occur several times in result set
        Set<String> rootFeatureIds = new HashSet<String>( 1000 );

        Object[] resultValues = new Object[selectManager.getFetchContentCount()];
        int maxFeatures = this.query.getMaxFeatures();

        int[] resultFtIdx = selectManager.getIncludedFtIdx();
        for ( int i = 0; i < resultFtIdx.length; i++ ) {
            LOG.logDebug( "Included in result set: " + resultFtIdx[i] );
        }

        while ( rs.next() ) {

            Feature[] resultTuple = new Feature[resultFtIdx.length];

            // already maxFeature features extracted?
            if ( maxFeatures != -1 && resultTuples.size() == maxFeatures ) {
                break;
            }

            // collect result values
            for ( int i = 0; i < resultValues.length; i++ ) {
                resultValues[i] = rs.getObject( i + 1 );
            }

            FeatureId[] fids = new FeatureId[resultFtIdx.length];
            StringBuffer combinedFid = new StringBuffer();

            // build combined fid to identify unique "features" (actually these are feature tuples)
            for ( int i = 0; i < resultFtIdx.length; i++ ) {
                int idx = resultFtIdx[i];
                MappedFeatureType rootFt = this.rootFts[idx];
                Map<SimpleContent, Integer> resultPosMap = selectManager.getResultPosMaps()[idx];
                fids[i] = extractFeatureId( rootFt, resultPosMap, resultValues );
                combinedFid.append( fids[i].getAsString() );
            }
            LOG.logDebug( "CombinedFID: " + combinedFid );

            // if tuple has not been added to result yet, extract and add it
            if ( !rootFeatureIds.contains( combinedFid.toString() ) ) {
                for ( int i = 0; i < resultFtIdx.length; i++ ) {
                    int ftIdx = resultFtIdx[i];
                    FeatureId fid = fids[i];
                    Map<MappedPropertyType, Collection<PropertyPath>> requestedPropertyMap = selectManager.getAllFetchProps()[ftIdx];
                    Map<SimpleContent, Integer> resultPosMap = selectManager.getResultPosMaps()[ftIdx];

                    // feature may have been fetched already
                    Feature feature = this.featureMap.get( fid );
                    if ( feature == null ) {
                        feature = extractFeature( fid, requestedPropertyMap, resultPosMap, resultValues );
                    }
                    resultTuple[i] = ( feature );
                }
                resultTuples.add( resultTuple );
                rootFeatureIds.add( combinedFid.toString() );
            }
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            for ( int i = 0; i < resultTuples.size(); i++ ) {
                Feature[] resultTuple = resultTuples.get( i );
                StringBuffer sb = new StringBuffer();
                for ( int j = 0; j < resultFtIdx.length; j++ ) {
                    int idx = resultFtIdx[j];
                    sb.append( resultTuple[j].getId() );
                    if ( idx != this.rootFts.length - 1 ) {
                        sb.append( ',' );
                    }
                }
                LOG.logDebug( sb.toString() );
            }
        }

        FeatureTupleCollection result = FeatureFactory.createFeatureCollection( "id", resultTuples, this.rootFts.length );
        return result;
    }

    private void resolveFeatureReferences() {
        for ( FeatureId fid : this.fidToPropertyMap.keySet() ) {
            Feature feature = this.featureMap.get( fid );
            assert feature != null;
            for ( FeatureProperty property : this.fidToPropertyMap.get( fid ) ) {
                property.setValue( feature );
            }
        }
    }

    /**
     * Performs a query for the number feature instances that match the query constraints. This corresponds to a query
     * with resultType=HITS.
     * 
     * @return a feature collection containing number of features that match the query constraints
     * @throws SQLException
     * @throws DatastoreException
     */
    private FeatureCollection performHitsQuery()
                            throws SQLException, DatastoreException {

        FeatureCollection result = FeatureFactory.createFeatureCollection( "ID", 2 );

        String tableAlias = this.whereBuilder.getRootTableAlias( 0 );
        String field = this.rootFts[0].getGMLId().getIdFields()[0].getField();
        StatementBuffer query = new StatementBuffer();
        query.append( "SELECT COUNT( DISTINCT " );
        query.append( tableAlias + '.' + field );
        query.append( ") FROM " );

        whereBuilder.appendJoinTableList( query );
        whereBuilder.appendWhereCondition( query );
        LOG.logDebug( "Count query: '" + query + "'" );

        ResultSet rs = null;
        PreparedStatement stmt = this.datastore.prepareStatement( this.conn, query );
        try {
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                result.setAttribute( "numberOfFeatures", rs.getObject( 1 ).toString() );
            } else {
                LOG.logError( "Internal error. Count result is empty (no rows)." );
                throw new SQLException();
            }
        } catch ( SQLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new SQLException( "Error performing count (HITS) query: " + query );
        } finally {
            try {
                if ( rs != null ) {
                    rs.close();
                }
            } finally {
                if ( stmt != null ) {
                    stmt.close();
                }
            }
        }
        return result;
    }

    /**
     * Builds the initial SELECT statement.
     * <p>
     * This statement determines all feature ids that are affected by the filter, but also SELECTs all properties that
     * are stored in the root feature types' tables (to improve efficiency).
     * </p>
     * <p>
     * The statement is structured like this:
     * <ul>
     * <li><code>SELECT</code></li>
     * <li>comma-separated list of qualified columns/functions to fetch from root tables</li>
     * <li><code>FROM</code></li>
     * <li>comma-separated list of tables and their aliases (this is needed to constrain the paths to selected
     * XPath-PropertyNames)</li>
     * <li><code>WHERE</code></li>
     * <li>SQL representation of the filter expression</li>
     * <li><code>ORDER BY</code></li>
     * <li>qualified sort criteria columns/functions</li>
     * </ul>
     * </p>
     * 
     * @param selectManager
     *            associated <code>SelectManager</code>
     * @return initial select statement
     * @throws DatastoreException
     */
    protected StatementBuffer buildInitialSelect( SelectManager selectManager )
                            throws DatastoreException {

        List<List<SimpleContent>>[] fetchContents = selectManager.getAllFetchContents();
        StatementBuffer stmt = new StatementBuffer();

        stmt.append( "SELECT " );

        String tableAlias = this.whereBuilder.getRootTableAlias( 0 );
        List<List<SimpleContent>> ftFetchContents = fetchContents[0];
        appendQualifiedContentList( stmt, tableAlias, ftFetchContents );

        boolean first = ftFetchContents.size() == 0;
        for ( int i = 1; i < this.rootFts.length; i++ ) {
            ftFetchContents = fetchContents[i];
            if ( ftFetchContents.size() > 0 ) {
                if ( !first ) {
                    stmt.append( ',' );
                    first = false;
                }
                tableAlias = this.whereBuilder.getRootTableAlias( i );
                appendQualifiedContentList( stmt, tableAlias, ftFetchContents );
            }
        }

        stmt.append( " FROM " );

        whereBuilder.appendJoinTableList( stmt );
        whereBuilder.appendWhereCondition( stmt );
        whereBuilder.appendOrderByCondition( stmt );

        return stmt;
    }
}
