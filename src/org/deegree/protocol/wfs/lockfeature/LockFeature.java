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
package org.deegree.protocol.wfs.lockfeature;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

/**
 * Represents a <code>LockFeature</code> request to a WFS.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LockFeature extends AbstractWFSRequest {

    private LockOperation[] locks;

    private Integer expiry;

    private Boolean lockAll;

    /**
     * Creates a new {@link LockFeature} request.
     * 
     * @param version
     *            protocol version, may not be null
     * @param handle
     *            client-generated identifier, may be null
     * @param locks
     *            locks to be acquired, must not be null and contain at least one entry
     * @param expiry
     *            expiry time (in minutes) before the features are unlocked automatically, may be null (unspecified)
     * @param lockAll
     *            true means that the request should fail if not all requested locks can be acquired, may be null
     *            (unspecified)
     */
    public LockFeature( Version version, String handle, LockOperation[] locks, Integer expiry, Boolean lockAll ) {
        super( version, handle );
        this.locks = locks;
        this.expiry = expiry;
        this.lockAll = lockAll;
    }

    /**
     * Returns the locks to be acquired.
     * 
     * @return the locks to be acquired, never null and always contains at least one entry
     */
    public LockOperation[] getLocks() {
        return locks;
    }

    /**
     * Returns the expiry time for the acquired locks.
     * 
     * @return the expiry time for the acquired locks, can be null (unspecified)
     */
    public Integer getExpiry() {
        return expiry;
    }

    /**
     * Returns whether the request should fail if not all specified features can be locked.
     * 
     * @return true, if the request should fail, can be null (unspecified)
     */
    public Boolean getLockAll() {
        return lockAll;
    }

    @Override
    public String toString() {
        String s = "{version=" + getVersion() + ",handle=" + getHandle();
        // if (typeNames != null ) {
        // s += "{";
        // for ( int i = 0; i < typeNames.length; i++ ) {
        // s += typeNames [i];
        // if (i != typeNames.length -1) {
        // s += ",";
        // }
        // }
        // s += "}";
        // } else {
        // s += "null";
        // }
        s += "}";
        return s;
    }
}
