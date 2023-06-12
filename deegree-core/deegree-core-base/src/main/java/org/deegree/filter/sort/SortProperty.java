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

package org.deegree.filter.sort;

import org.deegree.filter.expression.ValueReference;

/**
 * A sort criterion that consist of a property name plus sort order (ascending or
 * descending).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SortProperty {

	private ValueReference propName;

	private boolean sortAscending;

	/**
	 * Creates a new {@link SortProperty}.
	 * @param propName property that acts as the sort criterion
	 * @param sortAscending true: sort ascending, false: descending
	 */
	public SortProperty(ValueReference propName, boolean sortAscending) {
		this.propName = propName;
		this.sortAscending = sortAscending;
	}

	/**
	 * Returns the property that acts as the sort criterion.
	 * @return the property that acts as the sort criterion
	 */
	public ValueReference getSortProperty() {
		return this.propName;
	}

	/**
	 * Returns the sort order.
	 * @return true, if the sort order is ascending, false if it is descending
	 */
	public boolean getSortOrder() {
		return this.sortAscending;
	}

}
