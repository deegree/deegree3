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

package org.deegree.io.datastore;

import java.io.Serializable;
import java.util.Set;

import org.deegree.ogcwebservices.wfs.operation.GetFeatureWithLock;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;

/**
 * Represents a lock that has been acquired by a {@link LockFeature} or a {@link GetFeatureWithLock}
 * request.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Lock implements Serializable {

    private static final long serialVersionUID = 9140063407823707226L;

    private String lockId;

    private Set<String> lockedFids;

    private long expiryTime;

    /**
     * Creates a new <code>Lock</code> from the given parameters.
     *
     * @param lockId
     *            id of the lock (must be unique)
     * @param lockedFids
     *            locked feature ids
     * @param expiryTime
     *            point in time when the <code>Lock</code> expires automatically
     */
    Lock( String lockId, Set<String> lockedFids, long expiryTime ) {
        this.lockId = lockId;
        this.lockedFids = lockedFids;
        this.expiryTime = expiryTime;
    }

    /**
     * Returns the unique lock identifier.
     *
     * @return the unique lock identifier
     */
    public String getId() {
        return this.lockId;
    }

    /**
     * Returns the ids of the features that are locked by this lock.
     *
     * @return the ids of the locked features
     */
    public Set<String> getLockedFids() {
        return this.lockedFids;
    }

    /**
     * Returns the point in time when this lock will automatically expire.
     *
     * @return the point in time when this lock will automatically expire
     */
    long getExpiryTime() {
        return this.expiryTime;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "lock " + lockId + ": " );
        int i = this.lockedFids.size();
        for ( String fid : this.lockedFids ) {
            sb.append( fid );
            if ( --i != 0 ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }
}
