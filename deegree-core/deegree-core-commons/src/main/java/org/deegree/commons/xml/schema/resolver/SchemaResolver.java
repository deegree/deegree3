/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2026 by:
 - Department of Geography, University of Bonn -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -
 and others

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

 e-mail: info@deegree.org

 website: https://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.xml.schema.resolver;

import java.net.URL;

public interface SchemaResolver {

	String META_INF_SCHEMA_CACHE = "/META-INF/schema-cache";

	/**
	 * Useful constant for the highest precedence value.
	 *
	 * @see java.lang.Integer#MIN_VALUE
	 */
	int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

	/**
	 * Useful constant for the lowest precedence value.
	 *
	 * @see java.lang.Integer#MAX_VALUE
	 */
	int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

	/**
	 * Get the order value of this object.
	 * <p>
	 * Higher values are interpreted as lower priority. As a consequence, the object with
	 * the lowest value has the highest priority (somewhat analogous to Servlet
	 * {@code load-on-startup} values).
	 * <p>
	 * Same order values will result in arbitrary sort positions for the affected objects.
	 * @return the order value
	 * @see #HIGHEST_PRECEDENCE
	 * @see #LOWEST_PRECEDENCE
	 */
	default int getOrder() {
		return 0;
	}

	/**
	 * Allow the SchemaResolver to be initialized
	 */
	default void init() {

	}

	/**
	 * Redirect the given entity URL, can return an appropriate replacement location
	 * @param systemId entity URL, must not be <code>null</code>
	 * @return redirected URL, identical to input if it cannot be redirected, never
	 * <code>null</code>
	 */
	default String redirect(String systemId) {
		return systemId;
	}

	/**
	 * Resolves the given entity URL, returning a local URL if available
	 * @param systemId entity URL, must not be <code>null</code>
	 * @return redirected URL, <code>null</code> if the resolver is not responsible and
	 * the next should be tried
	 * @throws IllegalArgumentException If an entity URL is known but not allowed to be
	 * accessed
	 */
	default URL resolve(URL systemId) throws IllegalArgumentException {
		return null;
	}

}
