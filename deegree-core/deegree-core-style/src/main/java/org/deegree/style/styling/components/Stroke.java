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

package org.deegree.style.styling.components;

import static org.deegree.commons.utils.JavaUtils.generateToString;

import java.awt.Color;

import org.deegree.style.styling.Copyable;

/**
 * <code>Stroke</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class Stroke implements Copyable<Stroke> {

	/**
	 * Default is gray (#808080).
	 */
	public Color color = new Color(128, 128, 128, 255);

	/**
	 * Default is 1.
	 */
	public double width = 1;

	/**
	 * Default is backend specific.
	 */
	public LineJoin linejoin;

	/**
	 * Default is backend specific.
	 */
	public LineCap linecap;

	/**
	 * Default is null.
	 */
	public double[] dasharray;

	/**
	 * Default is 0.
	 */
	public double dashoffset;

	/**
	 * Default is null.
	 */
	public Graphic stroke;

	/**
	 * Default is 0.
	 */
	public double strokeGap;

	/**
	 * Default is 0.
	 */
	public double strokeInitialGap;

	/** Default is -1 == not to use it. */
	public double positionPercentage = -1;

	/** Default is rotate to line if position percentage is used */
	public boolean positionRotation = true;

	/**
	 * Default is null.
	 */
	public Graphic fill;

	/**
	 * <code>LineJoin</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static enum LineJoin {

		/** * */
		MITRE,
		/** * */
		ROUND,
		/** * */
		BEVEL

	}

	/**
	 * <code>LineCap</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static enum LineCap {

		/** * */
		BUTT,
		/** * */
		ROUND,
		/** * */
		SQUARE

	}

	@Override
	public Stroke copy() {
		Stroke copy = new Stroke();
		copy.color = color;
		copy.width = width;
		copy.linejoin = linejoin;
		copy.linecap = linecap;
		copy.dasharray = dasharray;
		copy.dashoffset = dashoffset;
		copy.stroke = stroke == null ? null : stroke.copy();
		copy.strokeGap = strokeGap;
		copy.strokeInitialGap = strokeInitialGap;
		copy.fill = fill == null ? null : fill.copy();
		copy.positionPercentage = positionPercentage;
		copy.positionRotation = positionRotation;
		return copy;
	}

	@Override
	public String toString() {
		return generateToString(this);
	}

	/**
	 * Checks if the Stroke would result into a complete transparent result
	 * @return <code>true</code> if no graphics are configured and the color has an alpha
	 * value of zero (fully transparent)
	 */
	public boolean isInvisible() {
		return stroke == null && fill == null && color != null && color.getAlpha() == 0;
	}

}
