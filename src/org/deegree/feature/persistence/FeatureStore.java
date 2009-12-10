//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.feature.persistence;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLObject;

/**
 * Base interface of the {@link Feature} persistence layer, provides access to stored {@link Feature} instances.
 * <p>
 * Note that a {@link FeatureStore} instance is always associated with exactly one {@link ApplicationSchema} instance.
 * </p>
 * <p>
 * <h4>Implementation requirements</h4>
 * Implementations must be thread-safe, because {@link FeatureStore} instances are usually used in multiple threads
 * concurrently.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface FeatureStore {

    /**
     * Called by the container to indicate that this {@link FeatureStore} instance is being placed into service.
     * 
     * @throws FeatureStoreException
     *             if the initialization fails
     */
    public void init()
                            throws FeatureStoreException;

    /**
     * Called by the container to indicate that this {@link FeatureStore} instance is being taken out of service.
     */
    public void destroy();

    /**
     * Returns whether the store is currently able to perform operations.
     * 
     * @return true, if the store is functional, false otherwise
     */
    public boolean isAvailable();

    /**
     * Returns the application schema that this {@link FeatureStore} serves.
     * 
     * @return the served application schema, never <code>null</code>
     */
    public ApplicationSchema getSchema();

    /**
     * Returns metadata on the specified feature type.
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code> and must be served by this store
     * @return metadata for the feature type, or <code>null</code> if the feature type is not known
     */
    public StoredFeatureTypeMetadata getMetadata( QName ftName );

    /**
     * Returns the envelope for all stored features of the given type.
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code> and must be served by this store
     * @return the envelope (using the native CRS), or <code>null</code> if the feature type is not known
     */
    public Envelope getEnvelope( QName ftName );

    /**
     * Performs the given query and returns the matching features as a {@link FeatureResultSet}.
     * 
     * @param query
     *            query to be performed, must not be <code>null</code>
     * @return matching features, never <code>null</code>
     * @throws FeatureStoreException
     *             if the query could not be performed
     * @throws FilterEvaluationException
     *             if the filter contained in the query could not be evaluated
     */
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException;

    /**
     * Performs the given queries and returns the matching features as a {@link FeatureResultSet}.
     * 
     * @param queries
     *            queries to be performed, must not be <code>null</code> and contain at least one entry
     * @return matching features, never <code>null</code>
     * @throws FeatureStoreException
     *             if the query could not be performed
     * @throws FilterEvaluationException
     *             if the filter contained in the query could not be evaluated
     */
    public FeatureResultSet query( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException;

    /**
     * Returns the number of features that are matched by the given query.
     * 
     * @param query
     *            query to be performed, must not be <code>null</code>
     * @return number of matching featuress
     * @throws FeatureStoreException
     *             if the query could not be performed
     * @throws FilterEvaluationException
     *             if the filter contained in the query could not be evaluated
     */
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException;

    /**
     * Returns the number of features that are matched by the given queries.
     * 
     * @param queries
     *            queries to be performed, must not be <code>null</code> and contain at least one entry
     * @return number of matching features
     * @throws FeatureStoreException
     *             if the query could not be performed
     * @throws FilterEvaluationException
     *             if the filter contained in the query could not be evaluated
     */
    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException;

    /**
     * Retrieves the stored object with a certain id.
     * 
     * TODO check if a common interface for returned objects should be used here (instead of <code>Object</code>)
     * 
     * @param id
     *            identifier of the object to be retrieved
     * @return the stored object (either a {@link Feature} or a {@link Geometry}) or <code>null</code> if no object with
     *         the given id is known
     * @throws FeatureStoreException
     *             if the query could not be performed
     */
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException;

    /**
     * Acquires transactional access to the feature store.
     * 
     * @return transaction object that allows to perform transactions operations on the datastore, never
     *         <code>null</code>
     * @throws FeatureStoreException
     *             if the transactional access could not be acquired or is not implemented for this {@link FeatureStore}
     */
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException;

    /**
     * Returns the associated {@link LockManager}.
     * 
     * @return the associated {@link LockManager} instance, never <code>null</code>
     * @throws FeatureStoreException
     *             if the {@link FeatureStore} does not implement locking
     */
    public LockManager getLockManager()
                            throws FeatureStoreException;
}
