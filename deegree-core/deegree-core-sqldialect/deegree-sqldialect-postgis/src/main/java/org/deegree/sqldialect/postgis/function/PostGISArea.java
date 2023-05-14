/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.sqldialect.postgis.function;

import static java.sql.Types.VARCHAR;

import java.util.List;

import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.expression.SQLExpression;
import org.deegree.sqldialect.filter.expression.SQLOperationBuilder;
import org.deegree.sqldialect.filter.function.SQLFunctionProvider;
import org.deegree.workspace.Workspace;

/**
 * {@link SQLFunctionProvider} for the <code>Area</code> function.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PostGISArea implements SQLFunctionProvider {

	private static final String NAME = "Area";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SQLExpression toProtoSQL(List<SQLExpression> args, SQLDialect dialect) {
		if (args.size() != 1) {
			throw new IllegalArgumentException(
					"Unable to map function '" + NAME + "' to SQL. Expected a single argument.");
		}

		SQLExpression arg = args.get(0);

		// TODO infer type information on arguments
		// arg.cast( expr );

		SQLOperationBuilder builder = new SQLOperationBuilder(VARCHAR);
		builder.add("area(");
		builder.add(arg);
		builder.add(")");
		return builder.toOperation();
	}

	@Override
	public void init(Workspace ws) {
		// nothing to do
	}

	@Override
	public void destroy() {
		// nothing to do
	}

}
