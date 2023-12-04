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
package org.deegree.feature.persistence.shape;

import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.STRING;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLArgument;
import org.deegree.sqldialect.filter.expression.SQLColumn;
import org.deegree.sqldialect.filter.expression.SQLExpression;
import org.deegree.sqldialect.filter.expression.SQLOperation;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class H2WhereBuilder extends AbstractWhereBuilder {

	/**
	 * @param dialect SQL dialect, can be <code>null</code> (TODO refactor code, so not
	 * null is always used)
	 * @param filter
	 * @param sort
	 * @throws FilterEvaluationException
	 */
	public H2WhereBuilder(SQLDialect dialect, OperatorFilter filter, SortProperty[] sort)
			throws FilterEvaluationException {
		super(dialect, null, filter, sort, null);
		try {
			build(true);
		}
		catch (UnmappableException e) {
			// can not happen
		}
	}

	@Override
	protected SQLOperation toProtoSQL(SpatialOperator op) throws UnmappableException, FilterEvaluationException {
		throw new UnmappableException("Spatial operators are currently not mappable in h2.");
	}

	@Override
	protected SQLExpression toProtoSQL(ValueReference expr) throws UnmappableException, FilterEvaluationException {
		// TODO
		PrimitiveType pt = new PrimitiveType(STRING);
		PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(pt,
				expr.getAsQName().getLocalPart().toLowerCase(), false);
		return new SQLColumn(null, expr.getAsQName().getLocalPart().toLowerCase(), converter);
	}

	// avoid setting a Date on fields which are strings just containing ISO dates...
	@Override
	protected SQLExpression toProtoSQL(Literal<?> literal) throws UnmappableException, FilterEvaluationException {
		if (literal.getValue().toString().equals("true") || literal.getValue().toString().equals("false")) {
			PrimitiveType pt = new PrimitiveType(BOOLEAN);
			PrimitiveValue value = new PrimitiveValue(literal.getValue().toString(), pt);
			PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(pt, null, false);
			return new SQLArgument(value, converter);
		}
		PrimitiveType pt = new PrimitiveType(BaseType.STRING);
		PrimitiveValue value = new PrimitiveValue(literal.getValue().toString(), pt);
		PrimitiveParticleConverter converter = new DefaultPrimitiveConverter(pt, null, false);
		return new SQLArgument(value, converter);
	}

}