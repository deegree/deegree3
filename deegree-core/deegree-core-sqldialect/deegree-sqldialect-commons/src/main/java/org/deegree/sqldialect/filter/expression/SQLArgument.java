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

import static org.deegree.commons.tom.primitive.BaseType.STRING;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SQLExpression} that represents a constant argument value, e.g. a string, a
 * number or a geometry.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SQLArgument implements SQLExpression {

	private static Logger LOG = LoggerFactory.getLogger(SQLArgument.class);

	private TypedObjectNode value;

	private ParticleConverter<? extends TypedObjectNode> converter;

	private PrimitiveType pt;

	private boolean isSpatial;

	/**
	 * Creates a new primitive valued {@link SQLArgument}.
	 * @param value value, can be <code>null</code>
	 * @param converter converter, can be <code>null</code>
	 */
	public SQLArgument(PrimitiveValue value, PrimitiveParticleConverter converter) {
		this.value = value;
		if (converter != null) {
			this.pt = value.getType();
		}
		this.converter = converter;
		this.isSpatial = false;
	}

	/**
	 * Creates a new spatial valued {@link SQLArgument}.
	 * @param value value, can be <code>null</code>
	 * @param converter converter, can be <code>null</code>
	 */
	public SQLArgument(Geometry value, GeometryParticleConverter converter) {
		this.value = value;
		this.converter = converter;
		this.isSpatial = true;
	}

	public void setArgument(PreparedStatement stmt, int paramIndex) throws SQLException {

		if (converter == null) {
			LOG.warn("No inferred particle converter. Treating as STRING value.");
			new DefaultPrimitiveConverter(new PrimitiveType(STRING), null, false).setParticle(stmt,
					(PrimitiveValue) value, paramIndex);
		}
		else {
			((ParticleConverter<TypedObjectNode>) this.converter).setParticle(stmt, value, paramIndex);
		}
	}

	@Override
	public boolean isSpatial() {
		return isSpatial;
	}

	@Override
	public boolean isMultiValued() {
		return false;
	}

	@Override
	public String toString() {
		return "'" + value + "'";
	}

	@Override
	public List<SQLArgument> getArguments() {
		return Collections.singletonList(this);
	}

	@Override
	public StringBuilder getSQL() {
		String sql = null;
		if (converter == null) {
			LOG.warn("No inferred particle converter. Treating as STRING value.");
			sql = new DefaultPrimitiveConverter(new PrimitiveType(STRING), null, false)
				.getSetSnippet((PrimitiveValue) value);
		}
		else {
			sql = ((ParticleConverter<TypedObjectNode>) this.converter).getSetSnippet(value);
		}
		return new StringBuilder(sql);
	}

	@Override
	public ICRS getCRS() {
		return isSpatial ? ((GeometryParticleConverter) converter).getCrs() : null;
	}

	@Override
	public String getSRID() {
		return isSpatial ? ((GeometryParticleConverter) converter).getSrid() : null;
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		return pt;
	}

	@Override
	public void cast(SQLExpression expr) {
		ParticleConverter<?> converter = expr.getConverter();
		if (converter instanceof PrimitiveParticleConverter) {
			PrimitiveParticleConverter ppc = (PrimitiveParticleConverter) converter;
			this.pt = ppc.getType();
			this.converter = converter;
			if (value != null) {
				value = new PrimitiveValue(value.toString(), pt);
			}
		}
		else {
			LOG.warn("Type casts for non-primitive values shouldn't occur.");
		}
	}

	@Override
	public ParticleConverter<? extends TypedObjectNode> getConverter() {
		return converter;
	}

	public TypedObjectNode getValue() {
		return value;
	}

}