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
package org.deegree.feature.persistence.sql.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.filter.expression.ValueReference;
import org.deegree.sqldialect.filter.UnmappableException;
import org.jaxen.expr.AllNodeStep;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.TextNodeStep;
import org.jaxen.saxpath.Axis;

/**
 * XPath <code>NameStep</code>.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class MappableStep {

	/**
	 * Checks and extracts the steps from the given {@link ValueReference}.
	 * @param propName property name, must not be <code>null</code>
	 * @return steps, never <code>null</code>
	 * @throws UnmappableException if unsupported expressions / axes / predicates are
	 * encountered
	 */
	public static List<MappableStep> extractSteps(ValueReference propName) throws UnmappableException {

		List<MappableStep> steps = new ArrayList<MappableStep>();

		Expr xpath = propName.getAsXPath();
		if (!(xpath instanceof LocationPath)) {
			String msg = "Unable to map PropertyName '" + propName.getAsText() + "': not a LocationPath.";
			throw new UnmappableException(msg);
		}
		for (Object step : ((LocationPath) xpath).getSteps()) {
			if (step instanceof AllNodeStep) {
				// nothing to do (/.)
			}
			else if (step instanceof TextNodeStep) {
				steps.add(new TextStep());
			}
			else if (!(step instanceof NameStep)) {
				String msg = "Unable to map PropertyName '" + propName.getAsText()
						+ "': contains a step that is not a NameStep.";
				throw new UnmappableException(msg);
			}
			else {
				NameStep namestep = (NameStep) step;
				String prefix = namestep.getPrefix();
				String localPart = namestep.getLocalName();
				String namespace = propName.getNsContext().translateNamespacePrefixToUri(prefix);
				QName nodeName = new QName(namespace, localPart, prefix);

				if (namestep.getAxis() == Axis.ATTRIBUTE) {
					if (namestep.getPredicates() != null && !namestep.getPredicates().isEmpty()) {
						String msg = "Unable to map PropertyName '" + propName.getAsText()
								+ "': contains an attribute NameStep with a predicate.";
						throw new UnmappableException(msg);
					}
					steps.add(new AttrStep(nodeName));
				}
				else if (namestep.getAxis() == Axis.CHILD) {
					if (namestep.getPredicates() != null && !namestep.getPredicates().isEmpty()) {
						String msg = "Unable to map PropertyName '" + propName.getAsText()
								+ "': contains an element NameStep with a predicate (needs implementation).";
						throw new UnmappableException(msg);
					}
					steps.add(new ElementStep(nodeName, -1));
				}
				else {
					String msg = "Unable to map PropertyName '" + propName.getAsText()
							+ "': only child and attribute steps are supported.";
					throw new UnmappableException(msg);
				}
			}
		}
		return steps;
	}

}