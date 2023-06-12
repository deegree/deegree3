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
package org.deegree.sqldialect.filter;

import static java.util.Collections.emptyList;

import java.util.List;

import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.filter.expression.ValueReference;

/**
 * A {@link ValueReference} that's mapped to database column(s).
 *
 * @see AbstractWhereBuilder
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PropertyNameMapping {

	private final ParticleConverter<?> converter;

	private final List<Join> joins;

	private String column;

	private String tableAlias;

	/**
	 * Creates a new {@link PropertyNameMapping} instance.
	 * @param converter converter, must not be <code>null</code>
	 * @param joins joins that are required to connect the root table to the tables where
	 * the targeted SQL particles are, can also be emtpy or <code>null</code>
	 * @param column may be null
	 * @param tableAlias may be null
	 */
	public PropertyNameMapping(ParticleConverter<?> converter, List<Join> joins, String column, String tableAlias) {
		this.converter = converter;
		this.column = column;
		this.tableAlias = tableAlias;
		if (joins == null) {
			this.joins = emptyList();
		}
		else {
			this.joins = joins;
		}
	}

	/**
	 * Returns the joins that are required to connect the root table to the tables where
	 * the targeted SQL particles are stored.
	 * @return joins, can be emtpy, but never <code>null</code>
	 */
	public List<Join> getJoins() {
		return joins;
	}

	/**
	 * Returns the converter for transforming corresponding argument values to SQL
	 * argument values.
	 * @return converter, never <code>null</code>
	 */
	public ParticleConverter<?> getConverter() {
		return converter;
	}

	public String getColumn() {
		return column;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	@Override
	public String toString() {
		String s = "";
		for (Join join : joins) {
			s += join;
			s += ",";
		}
		s += converter;
		return s;
	}

}