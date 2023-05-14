/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.sqldialect.filter.expression;

import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.utils.GeometryParticleConverter;

/**
 * {@link SQLExpression} that represents a table column.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SQLColumn implements SQLExpression {

	private final String table;

	private final String column;

	private PrimitiveType pt;

	private ParticleConverter<?> converter;

	private boolean isConcatenated;

	private boolean isSpatial;

	private String srid;

	private ICRS crs;

	public SQLColumn(String tableAlias, String column, ParticleConverter<?> converter) {
		this.table = tableAlias;
		this.column = column;
		this.converter = converter;
		if (converter instanceof PrimitiveParticleConverter) {
			pt = ((PrimitiveParticleConverter) converter).getType();
			isConcatenated = ((PrimitiveParticleConverter) converter).isConcatenated();
		}
		else if (converter instanceof GeometryParticleConverter) {
			isSpatial = true;
			srid = ((GeometryParticleConverter) converter).getSrid();
			crs = ((GeometryParticleConverter) converter).getCrs();
		}
	}

	@Override
	public ICRS getCRS() {
		return crs;
	}

	@Override
	public String getSRID() {
		return srid;
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return pt;
	}

	@Override
	public void cast(SQLExpression expr) {
		ParticleConverter<?> converter = expr.getConverter();
		if (!(converter instanceof PrimitiveParticleConverter)
				|| ((PrimitiveParticleConverter) converter).getType().getBaseType() != this.pt.getBaseType()) {
			throw new UnsupportedOperationException("Column type casts are not implemented yet.");
		}
	}

	@Override
	public ParticleConverter<?> getConverter() {
		return converter;
	}

	@Override
	public boolean isSpatial() {
		return isSpatial;
	}

	@Override
	public boolean isMultiValued() {
		return isConcatenated;
	}

	@Override
	public String toString() {
		return table == null ? column : (table + "." + column);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SQLArgument> getArguments() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public StringBuilder getSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append(table == null ? column : (table + "." + column));
		return sb;
	}

}