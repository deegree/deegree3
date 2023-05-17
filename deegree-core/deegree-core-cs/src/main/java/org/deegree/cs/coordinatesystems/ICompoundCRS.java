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
package org.deegree.cs.coordinatesystems;

import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IUnit;

/**
 * Interface describing a CompundCRS
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public interface ICompoundCRS extends ICRS {

	/**
	 * @return the heightAxis.
	 */
	IAxis getHeightAxis();

	/**
	 * @return the units of the heightAxis.
	 */
	IUnit getHeightUnits();

	/**
	 * @return the geographic Axis and the heightAxis as the third component.
	 */
	IAxis[] getAxis();

	/**
	 * @return the underlyingCRS.
	 */
	ICRS getUnderlyingCRS();

	/**
	 * @return the defaultHeight or 0 if it was not set.
	 */
	double getDefaultHeight();

}
