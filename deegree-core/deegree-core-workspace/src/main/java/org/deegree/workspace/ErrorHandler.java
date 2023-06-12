/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Can be used to obtain and log more detailed error messages related to resource
 * initialization.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class ErrorHandler {

	private Map<ResourceIdentifier<? extends Resource>, List<String>> errors;

	public ErrorHandler() {
		errors = new HashMap<ResourceIdentifier<? extends Resource>, List<String>>();
	}

	public void registerError(ResourceIdentifier<? extends Resource> id, String error) {
		List<String> list = errors.get(id);
		if (list == null) {
			list = new ArrayList<String>();
			errors.put(id, list);
		}
		list.add(error);
	}

	public List<String> getErrors(ResourceIdentifier<? extends Resource> id) {
		List<String> list = errors.get(id);
		if (list == null) {
			return Collections.emptyList();
		}
		return list;
	}

	public void clear() {
		errors.clear();
	}

	public void clear(ResourceIdentifier<? extends Resource> id) {
		errors.remove(id);
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

}
