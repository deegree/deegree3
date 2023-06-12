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
package org.deegree.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link Filter} that matches resources by {@link ResourceId}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class IdFilter implements Filter {

	private final List<ResourceId> selectedIds;

	private final Set<String> stringIds = new LinkedHashSet<String>();

	/**
	 * Creates a new {@link IdFilter} that selects the resources with the given ids.
	 * @param ids ids of the resources that the filter will selects, must not be
	 * <code>null</code>
	 */
	public IdFilter(String... selectedIds) {
		this.selectedIds = new ArrayList<ResourceId>(selectedIds.length);
		for (String id : selectedIds) {
			this.selectedIds.add(new ResourceId(id, null, null, null, null));
			stringIds.add(id);
		}
	}

	/**
	 * Creates a new {@link IdFilter} that selects the resources with the given ids.
	 * @param ids ids of the resources that the filter will selects, must not be
	 * <code>null</code>
	 */
	public IdFilter(Collection<String> selectedIds) {
		this.selectedIds = new ArrayList<ResourceId>(selectedIds.size());
		for (String id : selectedIds) {
			this.selectedIds.add(new ResourceId(id, null, null, null, null));
			stringIds.add(id);
		}
	}

	/**
	 * Creates a new {@link IdFilter} that selects the resources with the given ids.
	 * @param selectedIds ids of the resources that the filter will selects, must not be
	 * <code>null</code>
	 */
	public IdFilter(List<ResourceId> selectedIds) {
		this.selectedIds = selectedIds;
		for (ResourceId id : selectedIds) {
			stringIds.add(id.getRid());
		}
	}

	/**
	 * Always returns {@link Filter.Type#ID_FILTER} (for {@link IdFilter} instances).
	 * @return {@link Filter.Type#ID_FILTER}
	 */
	@Override
	public Type getType() {
		return Type.ID_FILTER;
	}

	/**
	 * Returns the ids of the resources that this filter selects.
	 * @return the ids of the resources that this filter selects
	 */
	public List<ResourceId> getSelectedIds() {
		return selectedIds;
	}

	/**
	 * Returns the ids of the objects that this filter matches.
	 * @return the ids of the objects that this filter matches
	 * @deprecated use {@link #getSelectedIds()} instead
	 */
	public Set<String> getMatchingIds() {
		return stringIds;
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {

		String id = xpathEvaluator.getId(obj);
		if (id != null) {
			return stringIds.contains(id);
		}
		return false;
	}

}
