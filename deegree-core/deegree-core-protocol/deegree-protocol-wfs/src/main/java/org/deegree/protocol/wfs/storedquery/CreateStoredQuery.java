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
package org.deegree.protocol.wfs.storedquery;

import static java.util.Collections.emptyList;

import java.util.List;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

/**
 * Represents a <code>CreateStoredQuery</code> request to a WFS.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class CreateStoredQuery extends AbstractWFSRequest {

	private final List<StoredQueryDefinition> queryDefinitions;

	/**
	 * Creates a new {@link CreateStoredQuery} instance.
	 * @param version protocol version, must not be <code>null</code>
	 * @param handle client-generated identifier, may be <code>null</code>
	 * @param queryDefinitions query definitions to add, may be <code>null</code>
	 */
	public CreateStoredQuery(Version version, String handle, List<StoredQueryDefinition> queryDefinitions) {
		super(version, handle);
		if (queryDefinitions == null) {
			this.queryDefinitions = emptyList();
		}
		else {
			this.queryDefinitions = queryDefinitions;
		}
	}

	/**
	 * Returns the {@link StoredQueryDefinition}s to be added.
	 * @return query definitions to be added, never <code>null</code>
	 */
	public List<StoredQueryDefinition> getQueryDefinitions() {
		return queryDefinitions;
	}

}
