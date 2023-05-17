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
package org.deegree.metadata.iso.types;

/**
 * Specifies the tightly coupled dataset relation in ISO profile 1.0. This is defined in
 * OGC 07-045 document.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class OperatesOnData {

	private final String operatesOnId;

	private final String operatesOnIdentifier;

	private final String operationName;

	/**
	 * Creates a new {@link OperatesOnData} instance.
	 * @param scopedName
	 * @param operatesOnIdentifier identifier of a tightly coupled dataset on which the
	 * service operates on.
	 * @param operatesOnName name of an operation with which the service operates on a
	 * tightly coupled dataset.
	 *
	 */
	public OperatesOnData(String operatesOnId, String operatesOnIdentifier, String operatesOnName) {
		this.operatesOnId = operatesOnId;
		this.operatesOnIdentifier = operatesOnIdentifier;
		this.operationName = operatesOnName;
	}

	/**
	 * @return the operatesOn
	 */
	public String getOperatesOnId() {
		return operatesOnId;
	}

	/**
	 * @return the operatesOnIdentifier
	 */
	public String getOperatesOnIdentifier() {
		return operatesOnIdentifier;
	}

	/**
	 * @return the operatesOnName
	 */
	public String getOperatesOnName() {
		return operationName;
	}

}
