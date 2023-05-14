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
package org.deegree.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Determines a topological order for collections of {@link PostRelation}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SortUtils {

	/**
	 * @param <T>
	 * @param vertices
	 * @param postRelation
	 * @return
	 */
	public static <T> List<T> sortTopologically(Collection<T> vertices, PostRelation<T> postRelation) {

		List<T> sorted = new ArrayList<T>(vertices.size());

		Map<T, Integer> vertexToInEdges = new HashMap<T, Integer>();

		for (T vertex : vertices) {
			vertexToInEdges.put(vertex, 0);
		}
		for (T vertex : vertices) {
			List<T> post = postRelation.getPost(vertex);
			if (post != null) {
				for (T t : post) {
					int current = vertexToInEdges.get(t);
					vertexToInEdges.put(t, current++);
				}
			}
		}

		// queue that tracks vertices with an inbound edge degree of zero
		LinkedList<T> zeros = new LinkedList<T>();
		for (T vertex : vertices) {
			if (vertexToInEdges.get(vertex) == 0) {
				zeros.add(vertex);
			}
		}

		while (!zeros.isEmpty()) {
			T vertex = zeros.remove(0);
			sorted.add(vertex);
			List<T> post = postRelation.getPost(vertex);
			if (post != null) {
				for (T t : post) {
					int current = vertexToInEdges.get(t);
					if (--current == 0) {
						zeros.add(t);
					}
					vertexToInEdges.put(t, current);
				}
			}
		}

		return sorted;
	}

}