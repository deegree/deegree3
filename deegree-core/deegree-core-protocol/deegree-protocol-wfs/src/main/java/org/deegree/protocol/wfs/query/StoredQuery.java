/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.query;

import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;

/**
 * A {@link Query} that provides the id of a {@link StoredQueryDefinition} template and
 * parameter values.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class StoredQuery extends Query {

	private final String id;

	private final Map<String, OMElement> paramNameToValue;

	/**
	 * Creates a new {@link StoredQuery} instance.
	 * @param handle client-generated query identifier, may be <code>null</code>
	 * @param id identifier of the stored query to be invoked, must not be
	 * <code>null</code>
	 * @param paramNameToValue parameters, must not be <code>null</code>
	 */
	public StoredQuery(String handle, String id, Map<String, OMElement> paramNameToValue) {
		super(handle);
		this.id = id;
		this.paramNameToValue = paramNameToValue;
	}

	/**
	 * Returns the identifier of the stored query definition.
	 * @return identifier of the stored query definition, never <code>null</code>
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the parameter name to value mapping.
	 * @return parameter name to value mapping, never <code>null</code>
	 */
	public Map<String, OMElement> getParams() {
		return paramNameToValue;
	}

}
