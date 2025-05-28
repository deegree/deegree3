/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2025 by:
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
package org.deegree.cql2;

import javax.xml.namespace.QName;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Operator;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public final class CQL2FilterParser {

	private CQL2FilterParser() {
	}

	public static Operator parseCql2Filter(String filter, ICRS crs, Set<QName> availableProperties) {
		CharStream input = CharStreams.fromString(filter);
		Cql2Lexer lexer = new Cql2Lexer(input);
		CommonTokenStream cts = new CommonTokenStream(lexer);
		cts.fill();
		Cql2Parser parser = new Cql2Parser(cts);
		parser.removeErrorListeners();
		parser.addErrorListener(new Cql2ErrorListener());
		Cql2Parser.BooleanExpressionContext cql2 = parser.booleanExpression();
		Cql2FilterVisitor visitor = new Cql2FilterVisitor(crs, availableProperties);
		return (Operator) visitor.visit(cql2);
	}

}
