/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

package org.deegree.style.styling;

import static org.deegree.commons.utils.JavaUtils.generateToString;
import static org.deegree.style.styling.components.UOM.Pixel;

import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.UOM;

/**
 * <code>PointStyling</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class PointStyling implements Styling<PointStyling> {

	/**
	 * The unit of measure for all values.
	 */
	public UOM uom = Pixel;

	/**
	 * Default is a SQUARE mark graphic.
	 */
	public Graphic graphic = new Graphic();

	@Override
	public PointStyling copy() {
		PointStyling other = new PointStyling();
		other.graphic = graphic.copy();
		other.uom = uom;
		return other;
	}

	@Override
	public String toString() {
		return generateToString(this);
	}

}
