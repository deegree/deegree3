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
package org.deegree.filter;

import org.deegree.commons.tom.datetime.DateTime;

/**
 * Identifies a resource within an {@link IdFilter}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ResourceId {

	private final String rid;

	private final String previousRid;

	private final String version;

	private final DateTime startDate;

	private final DateTime endDate;

	/**
	 * Creates a new {@link ResourceId} instance.
	 * @param rid the id of the selected resource, must not be <code>null</code>
	 * @param previousRid TODO
	 * @param version TODO
	 * @param startDate TODO
	 * @param endDate TODO
	 */
	public ResourceId(String rid, String previousRid, String version, DateTime startDate, DateTime endDate) {
		this.rid = rid;
		this.previousRid = previousRid;
		this.version = version;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/**
	 * Returns the id of the resource that shall be selected.
	 * @return the id of the resource, never <code>null</code>
	 */
	public String getRid() {
		return rid;
	}

}
