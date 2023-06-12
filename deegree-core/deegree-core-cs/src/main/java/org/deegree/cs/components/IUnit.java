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
package org.deegree.cs.components;

import org.deegree.cs.CRSResource;

/**
 * Interface describing a Unit
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public interface IUnit extends CRSResource {

	/**
	 * Check if amount of the specified unit can be converted into amount of this unit.
	 * @param other
	 * @return true if this unit can be converted into the other unit
	 */
	boolean canConvert(final IUnit other);

	/**
	 * Convert a value in this unit to the given unit if possible.
	 * @param value to be converted
	 * @param targetUnit to convert to
	 * @return the converted value or the same value if this unit equals given unit.
	 * @throws IllegalArgumentException if no conversion can be applied.
	 */
	double convert(final double value, final IUnit targetUnit);

	/**
	 * Convert a value in this unit to the base unit, e.g. degree->radians
	 * @param value to be converted
	 * @return the converted value or the same value if this unit is a base unit.
	 */
	double toBaseUnits(final double value);

	/**
	 * @return the scale to convert to the base unit.
	 */
	double getScale();

	/**
	 * @return the base unit.
	 */
	IUnit getBaseType();

	/**
	 * @return true if this is a base type
	 */
	boolean isBaseType();

}