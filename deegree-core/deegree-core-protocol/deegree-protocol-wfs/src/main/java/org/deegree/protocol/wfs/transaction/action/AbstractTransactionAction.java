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
package org.deegree.protocol.wfs.transaction.action;

import org.deegree.protocol.wfs.transaction.TransactionActionType;
import org.deegree.protocol.wfs.transaction.TransactionAction;

/**
 * Abstract base class for implementations of {@link TransactionAction}.
 *
 * @see TransactionAction
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public abstract class AbstractTransactionAction implements TransactionAction {

	private final String handle;

	/**
	 * Creates a new {@link AbstractTransactionAction} with an optional handle.
	 * @param handle identifier for the operation, may be <code>null</code>
	 */
	protected AbstractTransactionAction(String handle) {
		this.handle = handle;
	}

	/**
	 * Returns the type of operation. Use this to safely determine the subtype of
	 * {@link AbstractTransactionOperation}.
	 * @return type of operation, never <code>null</code>
	 */
	public abstract TransactionActionType getType();

	/**
	 * Returns the idenfifier of the operation.
	 * @return the idenfifier of the operation, or <code>null</code> if it is unspecified
	 */
	public String getHandle() {
		return handle;
	}

}
