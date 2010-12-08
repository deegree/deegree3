//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

package org.deegree.feature.persistence.lock;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.CloseableIterator;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.filter.Filter;

/**
 * Represents a lock of a {@link LockManager}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Lock {

    /**
     * Returns the lock identifier.
     * 
     * @return the lock identifier
     */
    public String getId();

    /**
     * Returns the number of locked features.
     * 
     * @return the number of locked features
     */
    public int getNumLocked();

    /**
     * Returns the number of features that have been requested to be locked, but which couldn't.
     * 
     * @return the number of features that have been requested to be locked, but which couldn't
     */
    public int getNumFailedToLock();

    /**
     * Returns the ids of all locked features.
     * <p>
     * NOTE: The caller <b>must</b> invoke {@link CloseableIterator#close()} after it's not needed anymore -- otherwise,
     * backing resources (such as database connections) may not be freed.
     * </p>
     * 
     * @return an iterator for all locked feature ids
     * @throws FeatureStoreException
     */
    public CloseableIterator<String> getLockedFeatures()
                            throws FeatureStoreException;

    /**
     * Returns the ids of all features that have been requested to be locked, but which couldn't.
     * <p>
     * NOTE: The caller <b>must</b> invoke {@link CloseableIterator#close()} after it's not needed anymore -- otherwise,
     * backing resources (such as database connections) may not be freed.
     * </p>
     * 
     * @return an iterator for all locked feature ids
     * @throws FeatureStoreException
     */
    public CloseableIterator<String> getFailedToLockFeatures()
                            throws FeatureStoreException;

    /**
     * Returns whether this {@link Lock} involves the specified feature.
     * 
     * @param fid
     *            id of the feature
     * @return true, if the feature is involved, false otherwise
     * @throws FeatureStoreException
     */
    public boolean isLocked( String fid )
                            throws FeatureStoreException;

    /**
     * Releases all locked features (invalidates the lock).
     * 
     * @throws FeatureStoreException
     */
    public void release()
                            throws FeatureStoreException;

    /**
     * Releases the specified feature from the lock (if it was locked).
     * 
     * @param fid
     *            id of the feature
     * @throws FeatureStoreException
     */
    public void release( String fid )
                            throws FeatureStoreException;

    /**
     * Releases the specified features from the lock.
     * 
     * @param ftName
     * @param filter
     * @throws FeatureStoreException
     */
    public void release( QName ftName, Filter filter )
                            throws FeatureStoreException;
}
