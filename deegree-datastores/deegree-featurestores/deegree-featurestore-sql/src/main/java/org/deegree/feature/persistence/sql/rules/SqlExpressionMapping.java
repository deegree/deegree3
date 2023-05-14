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
package org.deegree.feature.persistence.sql.rules;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.feature.persistence.sql.jaxb.CustomConverterJAXB;
import org.deegree.filter.expression.ValueReference;

/**
 * {@link Mapping} of a particle to an SQL expression (e.g. a constant or a CASE
 * statement).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class SqlExpressionMapping<T extends TypedObjectNode> extends Mapping {

	private final String sql;

	/**
	 * Creates a new {@link SqlExpressionMapping} instance.
	 * @param path relative xpath expression, must not be <code>null</code>
	 * @param sql the SQL expression, must not be <code>null</code>
	 */
	public SqlExpressionMapping(ValueReference path, final String sql, CustomConverterJAXB converter) {
		super(path, false, null, converter);
		this.sql = sql;
	}

	public SqlExpressionMapping(ValueReference path, final String sql) {
		super(path, false, null, null);
		this.sql = sql;
	}

}
