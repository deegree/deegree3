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

import org.deegree.commons.utils.CloseableIterator;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.protocol.wfs.lockfeature.LockOperation;

/**
 * Keeps track of the lock state of the features stored in a {@link FeatureStore}.
 * <p>
 * Locked features cannot be updated or deleted except by transactions that specify their lock identifier.
 * </p>
 * <p>
 * Implementations must ensure that the active locks survive a restart of the VM (e.g. by persisting them in a
 * database).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface LockManager {

    /**
     * Acquires a lock for the specified features instances.
     * <p>
     * If <code>mustLockAll</code> is true and not all of the specified features can be locked, a
     * {@link FeatureStoreException} is thrown.
     * </p>
     * <p>
     * If no features have been locked at all, a lock will be issued, but the lock is not registered (as requested by
     * the WFS spec.).
     * </p>
     * 
     * @param lockRequests
     *            lock requests to be executed, must not be <code>null</code>
     * @param mustLockAll
     *            if true, a {@link FeatureStoreException} is thrown if any of the requested feature instances could not
     *            be locked
     * @param expireTimeout
     *            number of milliseconds before the lock is automatically released
     * @return lock identifier, never <code>null</code>
     * @throws FeatureStoreException
     *             if an internal error occurs or if <code>mustLockAll</code> is <code>true</code> and at least one
     *             feature could not be locked
     */
    public Lock acquireLock( LockOperation[] lockRequests, boolean mustLockAll, long expireTimeout )
                            throws FeatureStoreException;

    /**
     * Returns the active lock with the given id.
     * 
     * @param lockId
     * @return the active lock with the given id
     * @throws FeatureStoreException
     */
    public Lock getLock( String lockId )
                            throws FeatureStoreException;

    /**
     * Returns all active locks.
     * <p>
     * NOTE: The caller <b>must</b> invoke {@link CloseableIterator#close()} after it's not needed anymore -- otherwise,
     * backing resources (such as database connections) may not be freed.
     * </p>
     * 
     * @return an iterator for all locks
     * @throws FeatureStoreException
     */
    public CloseableIterator<Lock> getActiveLocks()
                            throws FeatureStoreException;

    /**
     * Returns whether an active lock on the specified feature exists.
     * 
     * @param fid
     *            id of the feature
     * @return true, if an active lock on the feature exists, false otherwise
     * @throws FeatureStoreException
     */
    public boolean isFeatureLocked( String fid )
                            throws FeatureStoreException;

    /**
     * Returns whether the specified feature is modifiable for the owner of the specified lock.
     * 
     * @param fid
     *            id of the feature, must not be <code>null</code>
     * @param lockId
     *            if of the lock, may be null (in this case the feature is only modifiable if the feature is not locked
     *            at all)
     * @return true, if the feature is not locked at all or the specified lock matches the feature's lock, false
     *         otherwise
     * @throws FeatureStoreException
     */
    public boolean isFeatureModifiable( String fid, String lockId )
                            throws FeatureStoreException;
}
