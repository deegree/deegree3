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

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.workspace.Resource;

/**
 * Base interface of the {@link Feature} persistence layer, provides access to stored {@link Feature} instances.
 * <p>
 * Note that a {@link FeatureStore} instance is always associated with exactly one {@link AppSchema} instance.
 * </p>
 * <p>
 * NOTE: Implementations must be thread-safe, as {@link FeatureStore} instances are usually used in multiple threads
 * concurrently.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface FeatureStore extends Resource {

    /**
     * Returns whether the store is currently able to perform operations.
     * 
     * @return true, if the store is functional, false otherwise
     */
    boolean isAvailable();

    /**
     * Returns the application schema that this {@link FeatureStore} serves.
     * 
     * @return the served application schema, never <code>null</code>
     */
    AppSchema getSchema();

    /**
     * Returns whether the specified feature type is actually mapped in the backend.
     * 
     * @param ftName
     *            feature type name, must not be <code>null</code>
     * @return <code>true</code>, if the feature type is mapped, <code>false</code> otherwise
     */
    boolean isMapped( QName ftName );

    /**
     * Returns whether the store supports evaluation of maxFeatures and startIndex.
     *
     * @param queries
     *            the queries to check if evaluation of maxFeatures and startIndex is applicable, must not be <code>null</code>
     * @return <code>true</code>, if evaluation of maxFeatures and startIndex is applicable, <code>false</code> otherwise
     */
    boolean isMaxFeaturesAndStartIndexApplicable( Query[] queries );

    /**
     * Returns the envelope for all stored features of the given type.
     * <p>
     * NOTE: This method may return incorrect (cached) results. Use {@link #calcEnvelope(QName)} to force the
     * recalculation of the {@link Envelope}.
     * </p>
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code> and must be served by this store
     * @return the envelope (using the storage CRS), or <code>null</code> if the feature type does not have an envelope
     *         (no geometry properties or no instances)
     * @throws FeatureStoreException
     */
    Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException;

    /**
     * Recalculates the envelope for all stored features of the given type.
     * <p>
     * NOTE: This method may potentially be expensive. Depending on the implementation, it may involve fetching all
     * features of the specified type.
     * </p>
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code> and must be served by this store
     * @return the envelope (using the storage CRS), or <code>null</code> if the feature type does not have an envelope
     *         (no geometry properties or no instances)
     * @throws FeatureStoreException
     */
    Envelope calcEnvelope( QName ftName )
                            throws FeatureStoreException;

    /**
     * Performs the given query and returns the matching features as a {@link FeatureInputStream}.
     * 
     * @param query
     *            query to be performed, must not be <code>null</code>
     * @return matching features, never <code>null</code>
     * @throws FeatureStoreException
     *             if the query could not be performed
     * @throws FilterEvaluationException
     *             if the filter contained in the query could not be evaluated
     */
    FeatureInputStream query( Query query )
                            throws FeatureStoreException, FilterEvaluationException;

    /**
     * Performs the given queries and returns the matching features as a {@link FeatureInputStream}.
     * 
     * @param queries
     *            queries to be performed, must not be <code>null</code> and contain at least one entry
     * @return matching features, never <code>null</code>
     * @throws FeatureStoreException
     *             if the query could not be performed
     * @throws FilterEvaluationException
     *             if the filter contained in the query could not be evaluated
     */
    FeatureInputStream query( Query[] queries )
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
    int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException;

    /**
     * Returns the number of features that are matched by the given queries.
     * 
     * @param queries
     *            queries to be performed, must not be <code>null</code> and contain at least one entry
     * @return number of matching features, one entry per query
     * @throws FeatureStoreException
     *             if the query could not be performed
     * @throws FilterEvaluationException
     *             if the filter contained in the query could not be evaluated
     */
    int[] queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException;

    /**
     * Retrieves the stored object with a certain id.
     * 
     * @param id
     *            identifier of the object to be retrieved
     * @return the stored object (currently either a {@link Feature} or a {@link Geometry}) or <code>null</code> if no
     *         object with the given id is known
     * @throws FeatureStoreException
     *             if the query could not be performed
     */
    GMLObject getObjectById( String id )
                            throws FeatureStoreException;

    /**
     * Acquires transactional access to the feature store.
     * 
     * @return transaction object that allows to perform transactions operations on the datastore, never
     *         <code>null</code>
     * @throws FeatureStoreException
     *             if the transactional access could not be acquired or is not implemented for this {@link FeatureStore}
     */
    FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException;

    /**
     * Returns the associated {@link LockManager}.
     * 
     * @return the associated {@link LockManager} instance, or <code>null</code> if the {@link FeatureStore} does not
     *         implement locking
     * @throws FeatureStoreException
     *             if the lock manager could not be acquired
     */
    LockManager getLockManager()
                            throws FeatureStoreException;

}
