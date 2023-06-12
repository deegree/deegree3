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

import java.math.BigInteger;
import java.util.List;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;
import org.deegree.protocol.wfs.query.Query;

/**
 * Represents a <code>LockFeature</code> request to a WFS.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 1.0.0</li>
 * <li>WFS 1.1.0</li>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class LockFeature extends AbstractWFSRequest {

	private final List<Query> queries;

	private final BigInteger expiry;

	private final Boolean lockAll;

	private final String existingLockId;

	/**
	 * Creates a new {@link LockFeature} request.
	 * @param version protocol version, may not be null
	 * @param handle client-generated identifier, may be null
	 * @param queries queries that select the features to be locked, must not be null and
	 * contain at least one entry
	 * @param expiry expiry time (in minutes) before the features are unlocked
	 * automatically, may be null (unspecified)
	 * @param lockAll true means that the request should fail if not all requested locks
	 * can be acquired, may be null (unspecified)
	 * @param existingLockId identifier of an existing lock for the purpose of resetting
	 * the lock expiry, can be <code>null</code>
	 */
	public LockFeature(Version version, String handle, List<Query> queries, BigInteger expiry, Boolean lockAll,
			String existingLockId) {
		super(version, handle);
		this.queries = queries;
		this.expiry = expiry;
		this.lockAll = lockAll;
		this.existingLockId = existingLockId;
	}

	/**
	 * Returns the queries that select the features to be locked.
	 * @return the queries that select the features to be locked, never null and always
	 * contains at least one entry
	 */
	public List<Query> getQueries() {
		return queries;
	}

	/**
	 * Returns the expiry time for the acquired locks.
	 * @return the expiry time for the acquired locks, can be null (unspecified)
	 */
	public BigInteger getExpiryInSeconds() {
		return expiry;
	}

	/**
	 * Returns whether the request should fail if not all specified features can be
	 * locked.
	 * <p>
	 * This corresponds to the lockAction parameter (lockAction = SOME/ALL).
	 * </p>
	 * @return true, if the request should fail, can be null (unspecified)
	 */
	public Boolean getLockAll() {
		return lockAll;
	}

	/**
	 * Returns the identifier of an existing lock that this request refers to.
	 * @return identifier of an existing lock, can be <code>null</code> (not referring to
	 * an existing lock)
	 */
	public String getExistingLockId() {
		return existingLockId;
	}

	@Override
	public String toString() {
		String s = "{version=" + getVersion() + ",handle=" + getHandle();
		s += "}";
		return s;
	}

}
