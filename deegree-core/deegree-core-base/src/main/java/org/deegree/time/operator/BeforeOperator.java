/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.time.operator;

import static org.deegree.time.operator.TimeCompareUtils.compareEndWithBegin;

import org.deegree.time.primitive.TimeGeometricPrimitive;

/**
 * Time operator to evaluate 'Before'.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class BeforeOperator {

	/**
	 * Evaluates if self is before other or not. TimeInstant and TimePeriods are allowed
	 * and handled as followed:
	 *
	 * <ul>
	 * <li>self.position < other.position</li>
	 * <li>self.position < other.begin.position</li>
	 * <li>self.end.position < other.position</li>
	 * <li>self.end.position < other.begin.position</li>
	 * </ul>
	 * @param self may be <code>null</code> (evaluation results in <code>false</code>)
	 * @param other may be <code>null</code> (evaluation results in <code>false</code>)
	 * @return <code>true</code> if self is temporal before other, <code>false</code> if
	 * self is after or equal to other or self and/or other are <code>null</code>
	 */
	public boolean evaluate(final TimeGeometricPrimitive self, final TimeGeometricPrimitive other) {
		if (self == null || other == null)
			return false;
		int compareBeginWithEnd = compareEndWithBegin(self, other);
		return compareBeginWithEnd < 0;
	}

}