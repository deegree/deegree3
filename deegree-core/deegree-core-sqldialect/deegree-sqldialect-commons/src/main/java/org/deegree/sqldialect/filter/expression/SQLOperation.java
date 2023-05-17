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

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.cs.coordinatesystems.CRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SQLExpression} that represents an operation, e.g. an addition, an intersects
 * predicate or a distance calculation.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SQLOperation implements SQLExpression {

	private static final Logger LOG = LoggerFactory.getLogger(SQLOperation.class);

	private List<Object> particles;

	public SQLOperation(List<Object> particles) {
		this.particles = particles;
	}

	@Override
	public boolean isSpatial() {
		return true;
	}

	@Override
	public boolean isMultiValued() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Object particle : particles) {
			sb.append(particle);
		}
		return sb.toString();
	}

	@Override
	public List<SQLArgument> getArguments() {
		List<SQLArgument> literals = new ArrayList<SQLArgument>();
		for (Object particle : particles) {
			if (particle instanceof SQLExpression) {
				if (particle instanceof SQLArgument) {
					literals.add((SQLArgument) particle);
				}
				else {
					literals.addAll(((SQLExpression) particle).getArguments());
				}
			}
		}
		return literals;
	}

	@Override
	public StringBuilder getSQL() {
		StringBuilder sb = new StringBuilder();
		for (Object particle : particles) {
			if (particle instanceof SQLExpression) {
				sb.append(((SQLExpression) particle).getSQL());
			}
			else {
				sb.append(particle);
			}
		}
		return sb;
	}

	@Override
	public CRS getCRS() {
		return null;
	}

	@Override
	public String getSRID() {
		return null;
	}

	@Override
	public PrimitiveType getPrimitiveType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cast(SQLExpression expr) {
		LOG.warn("Operation casts are not implemented yet. Depending on DB type handling.");
	}

	@Override
	public ParticleConverter getConverter() {
		throw new UnsupportedOperationException();
	}

}
