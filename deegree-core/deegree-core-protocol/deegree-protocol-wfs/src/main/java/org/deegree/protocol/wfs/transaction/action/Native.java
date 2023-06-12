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

import javax.xml.stream.XMLStreamReader;

import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionActionType;

/**
 * Represents a WFS <code>Native</code> operation (part of a {@link Transaction} request).
 *
 * @see Transaction
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class Native extends AbstractTransactionAction {

	private final String vendorId;

	private final boolean safeToIgnore;

	private final XMLStreamReader vendorSpecificData;

	/**
	 * Creates a new {@link Native} instance.
	 * @param handle identifier for the operation, can be <code>null</code>
	 * @param vendorId vendor identifier, can be <code>null</code>
	 * @param safeToIgnore <code>true</code>, if the operation may be ignored without
	 * problems, <code>false</code> if the surrounding request depends on it (and must
	 * fail if the native operation cannot be executed)
	 * @param vendorSpecificData provides access to the XML encoded vendor specific data,
	 * cursor must point at the <code>START_ELEMENT</code> event of the
	 * <code>wfs:Native</code> element, must not be <code>null</code>
	 */
	public Native(String handle, String vendorId, boolean safeToIgnore, XMLStreamReader vendorSpecificData) {
		super(handle);
		this.vendorSpecificData = vendorSpecificData;
		this.vendorId = vendorId;
		this.safeToIgnore = safeToIgnore;
	}

	/**
	 * Always returns {@link TransactionActionType#NATIVE}.
	 * @return {@link TransactionActionType#NATIVE}
	 */
	@Override
	public TransactionActionType getType() {
		return TransactionActionType.NATIVE;
	}

	/**
	 * Returns the vendor identifier.
	 * @return the vendor identifier, may be <code>null</code>
	 */
	public String getVendorId() {
		return vendorId;
	}

	/**
	 * Returns whether the whole transaction request should fail if the operation can not
	 * be executed.
	 * @return <code>true</code>, if the operation may be ignored safely,
	 * <code>false</code> otherwise
	 */
	public boolean isSafeToIgnore() {
		return safeToIgnore;
	}

	/**
	 * Returns an <code>XMLStreamReader</code> that provides access to the vendor specific
	 * data.
	 * <p>
	 * NOTE: The client <b>must</b> read this stream exactly once and exactly up to the
	 * next tag event after the closing element, i.e. up to the <code>END_ELEMENT</code>
	 * of the surrounding <code>Native</code> element.
	 * </p>
	 * @return XML encoded vendor specific data, cursor points at the
	 * <code>START_ELEMENT</code> event of the <code>wfs:Native</code> element, never
	 * <code>null</code>
	 */
	public XMLStreamReader getVendorSpecificData() {
		return vendorSpecificData;
	}

}
