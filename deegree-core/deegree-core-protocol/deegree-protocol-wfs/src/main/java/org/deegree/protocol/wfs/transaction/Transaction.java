/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wfs.transaction;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

/**
 * Represents a <code>Transaction</code> request to a WFS.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 1.0.0</li>
 * <li>WFS 1.1.0</li>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 *
 * @see TransactionAction
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Transaction extends AbstractWFSRequest {

	private final Iterable<TransactionAction> actions;

	private final String lockId;

	private final ReleaseAction releaseAction;

	private final String srsName;

	/**
	 * Creates a new {@link Transaction} request.
	 * @param version protocol version, must not be <code>null</code>
	 * @param handle client-generated identifier, can be <code>null</code>
	 * @param lockId lockd id, can be <code>null</code>
	 * @param releaseAction controls how to treat locked features when the transaction has
	 * been completed, can be <code>null</code> (unspecified)
	 * @param actions actions to be performed as parts of the transaction, must not be
	 * <code>null</code>
	 * @param srsName identifier of the coordinate reference system for contained
	 * geometries, can be <code>null</code>
	 */
	public Transaction(Version version, String handle, String lockId, ReleaseAction releaseAction,
			Iterable<TransactionAction> actions, String srsName) {
		super(version, handle);
		this.lockId = lockId;
		this.releaseAction = releaseAction;
		this.actions = actions;
		this.srsName = srsName;
	}

	/**
	 * Returns the lock identifier provided with this transaction.
	 * @return the lock identifier provided with this transaction, or <code>null</code> if
	 * it is unspecified
	 */
	public String getLockId() {
		return lockId;
	}

	/**
	 * Returns the release action mode to be applied after the transaction has been
	 * executed successfully.
	 * @return the release action mode to be applied after the transaction has been
	 * executed successfully, or <code>null</code> if it is unspecified
	 */
	public ReleaseAction getReleaseAction() {
		return releaseAction;
	}

	/**
	 * Returns the identifier of the coordinate reference system for contained geometries.
	 * @return identifier of the coordinate reference system, can be <code>null</code>
	 */
	public String getSrsName() {
		return srsName;
	}

	/**
	 * Returns the sequence of {@link TransactionAction}s that are contained in the
	 * transaction.
	 * @return sequence of actions, can be empty, but never <code>null</code>
	 */
	public Iterable<TransactionAction> getActions() {
		return actions;
	}

}
