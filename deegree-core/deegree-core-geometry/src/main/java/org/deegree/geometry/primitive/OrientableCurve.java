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
package org.deegree.geometry.primitive;

/**
 * An <code>OrientableCurve</code> consists of a wrapped base {@link Curve} and an
 * additional orientation.
 * <p>
 * If the orientation is *not* reversed, then the <code>OrientableCurve</code> is
 * identical to the base curve. If the orientation is reversed, then the
 * <code>OrientableCurve</code> is related to the base curve with a parameterization that
 * reverses the sense of the curve traversal.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public interface OrientableCurve extends Curve {

	/**
	 * Must always return {@link Curve.CurveType#OrientableCurve}.
	 * @return {@link Curve.CurveType#OrientableCurve}
	 */
	@Override
	public CurveType getCurveType();

	/**
	 * Returns whether the orientation of this curve is reversed compared to the base
	 * curve.
	 * @return true, if the orientation is reversed, false otherwise
	 */
	public boolean isReversed();

	/**
	 * Returns the {@link Curve} that this <code>OrientableCurve</code> is based on.
	 * @return the base <code>Curve</code>
	 */
	public Curve getBaseCurve();

}
