/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.feature.persistence.sql.id;

import java.util.List;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;

/**
 * Defines the mapping between feature ids and a relational model.
 *
 * @see FeatureTypeMapping
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class FIDMapping {

	private final String prefix;

	private final String delimiter;

	private final List<Pair<SQLIdentifier, BaseType>> columns;

	private final IDGenerator generator;

	/**
	 * Creates a new {@link FIDMapping} instance.
	 * @param prefix static prefix for all feature ids, must not be <code>null</code> (but
	 * can be empty)
	 * @param delimiter delimiter that separates the values of the individual columns,
	 * must not be <code>null</code> (but can be empty)
	 * @param columns database columns that the feature ids are mapped to, must not be
	 * <code>null</code> (and contain at least one entry)
	 * @param generator generator for determining new ids, can be <code>null</code> (in
	 * this case, no inserts are possible)
	 */
	public FIDMapping(String prefix, String delimiter, List<Pair<SQLIdentifier, BaseType>> columns,
			IDGenerator generator) {
		this.prefix = prefix;
		this.delimiter = delimiter;
		this.columns = columns;
		this.generator = generator;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public List<Pair<SQLIdentifier, BaseType>> getColumns() {
		return columns;
	}

	public IDGenerator getIdGenerator() {
		return generator;
	}

	@Deprecated
	public String getColumn() {
		if (columns.size() == 0) {
			throw new IllegalArgumentException();
		}
		return columns.get(0).first.toString();
	}

	@Deprecated
	public BaseType getColumnType() {
		if (columns.size() == 0) {
			throw new IllegalArgumentException();
		}
		return columns.get(0).second;
	}

}