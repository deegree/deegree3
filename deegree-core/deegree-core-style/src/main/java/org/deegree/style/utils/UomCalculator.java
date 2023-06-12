/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.style.utils;

import org.deegree.style.styling.components.UOM;

/**
 * Calculates pixels from any value in a specific unit of measure.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class UomCalculator {

	private final double pixelSize, resolution;

	public UomCalculator(double pixelSize, double resolution) {
		this.pixelSize = pixelSize;
		this.resolution = resolution;
	}

	public final double considerUOM(final double in, final UOM uom) {
		switch (uom) {
			case Pixel:
				return in * 0.28 / pixelSize;
			case Foot:
				// Note: Use 1 foot as 12 inches => 30,48 cm
				// @see http://en.wikipedia.org/wiki/Foot_%28unit%29
				return in * 0.3048d / resolution;
			case Metre:
				return in / resolution;
			case mm:
				return in / pixelSize;
		}
		return in;
	}

}
