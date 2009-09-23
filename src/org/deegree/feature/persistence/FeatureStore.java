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

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.geometry.Geometry;
import org.deegree.protocol.wfs.getfeature.Query;

/**
 * Base interface of the {@link Feature} persistence layer, provides access to stored {@link Feature} instances.
 * <p>
 * Note that a {@link FeatureStore} instance is always associated with exactly one {@link ApplicationSchema} instance.
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
     */
    public void init();

    /**
     * Called by the container to indicate that this {@link FeatureStore} instance is being taken out of service.
     */
    public void destroy();

    /**
     * Returns the application schema that this {@link FeatureStore} serves.
     * 
     * @return the served application schema
     */
    public ApplicationSchema getSchema();

    /**
     * Performs the given {@link Query} and returns the matching features as a {@link FeatureCollection}.
     * 
     * @param query
     *            query to be performed
     * @return matching features
     */
    public FeatureCollection performQuery( Query query );

    /**
     * Returns the number of features that are matched by the given {@link Query}.
     * 
     * @param query
     *            query to be performed
     * @return number of matching features
     */
    public int performHitsQuery( Query query );

    /**
     * Retrieves the stored object with a certain id.
     * 
     * TODO check if a common interface for returned objects should be used here (instead of <code>Object</code>)
     * 
     * @param id
     *            identifier of the object to be retrieved
     * @return the stored object (either a {@link Feature} or a {@link Geometry}) or null if no object with the given id
     *         is known
     */
    public Object getObjectById( String id );

    /**
     * Acquires transactional access to the feature store.
     * 
     * @return transaction object that allows to perform transactions operations on the datastore, never null
     * @throws FeatureStoreException
     *             if the transactional access could not be acquired or is not implemented for this {@link FeatureStore}
     */
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException;
    
    /**
     * Returns the associated {@link LockManager}.
     * 
     * @return the associated {@link LockManager} instance, never null
     * @throws FeatureStoreException
     *             if the {@link FeatureStore} does not implement locking 
     */
    public LockManager getLockManager()
                            throws FeatureStoreException;    
}
