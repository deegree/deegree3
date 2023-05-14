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
package org.deegree.protocol.wfs.query;

import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;

/**
 * Represents a <code>Query</code> operation as a part of a
 * {@link GetFeature}/{@link GetFeatureWithLock}/ {@link GetPropertyValue} request.
 *
 * @see GetFeature
 * @see GetFeatureWithLock
 * @see GetPropertyValue
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class Query {

	private final String handle;

	/**
	 * Creates a new {@link Query} instance.
	 * @param handle client-generated query identifier, may be <code>null</code>
	 */
	protected Query(String handle) {
		this.handle = handle;
	}

	/**
	 * Returns the client-generated identifier supplied with the query.
	 * @return the client-generated identifier, may be <code>null</code>
	 */
	public String getHandle() {
		return handle;
	}

}
