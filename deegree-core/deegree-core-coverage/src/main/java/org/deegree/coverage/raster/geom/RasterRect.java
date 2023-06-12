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
package org.deegree.coverage.raster.geom;

import java.awt.Rectangle;

/**
 * Simple data structure for a raster rectangle. Stores upper-left pixel coordinate and
 * width and height.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class RasterRect {

	/**
	 * The x pixel position.
	 */
	public int x;

	/**
	 * The y pixel position.
	 */
	public int y;

	/**
	 * The width in pixel.
	 */
	public int width;

	/**
	 * The height in pixel.
	 */
	public int height;

	/**
	 * Creates a new RasterRect
	 * @param x upper-left pixel
	 * @param y upper-left pixel
	 * @param width width of rectangle
	 * @param height height of rectangle
	 */
	public RasterRect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a new RasterRect with position 0, 0 and size 0, 0.
	 */
	public RasterRect() {
		this(0, 0, 0, 0);
	}

	/**
	 * @param rect to copy data of.
	 */
	public RasterRect(Rectangle rect) {
		this.x = rect.x;
		this.y = rect.y;
		this.width = rect.width;
		this.height = rect.height;
	}

	/**
	 * @param rasterRect to copy data of
	 */
	public RasterRect(RasterRect rasterRect) {
		this(rasterRect.x, rasterRect.y, rasterRect.width, rasterRect.height);
	}

	@Override
	public String toString() {
		return "{x=" + x + ",y=" + y + ", width=" + width + ", height=" + height + "}";
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof RasterRect) {
			final RasterRect that = (RasterRect) other;
			return x == that.x && y == that.y && width == that.width && height == that.height;
		}
		return false;
	}

	/**
	 * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001),
	 * which supplies an even distribution and is relatively fast. It is created from
	 * field <b>f</b> as follows:
	 * <ul>
	 * <li>boolean -- code = (f ? 0 : 1)</li>
	 * <li>byte, char, short, int -- code = (int)f</li>
	 * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
	 * <li>float -- code = Float.floatToIntBits(f);</li>
	 * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt;
	 * 32))</li>
	 * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code
	 * = f.hashCode(&nbsp;)</li>
	 * <li>Array -- Apply above rules to each element</li>
	 * </ul>
	 * <p>
	 * Combining the hash code(s) computed above: result = 37 * result + code;
	 * </p>
	 * @return (int) ( result >>> 32 ) ^ (int) result;
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// the 2nd millionth prime, :-)
		long result = 32452843;
		result = result * 37 + x;
		result = result * 37 + y;
		result = result * 37 + width;
		result = result * 37 + height;
		return (int) (result >>> 32) ^ (int) result;
	}

	/**
	 * Create an intersection of the given RasterRects, if the given rasterects do not
	 * intersect, null will be returned.
	 * @param first
	 * @param second
	 * @return the intersection or <code>null</code> if the given rectangles do not
	 * intersect.
	 */
	public static final RasterRect intersection(RasterRect first, RasterRect second) {
		int fmaxX = first.x + first.width;
		int smaxX = second.x + second.width;
		int fmaxY = first.y + first.height;
		int smaxY = second.y + second.height;
		if (second.x >= fmaxX || smaxX <= first.x || second.y >= fmaxY || smaxY <= first.y) {
			/* right outside || left outside || bottom outside || top outside */
			return null;
		}

		int x = Math.max(first.x, second.x);
		int y = Math.max(first.y, second.y);
		int width = Math.min(smaxX, fmaxX) - x;
		int height = Math.min(smaxY, fmaxY) - y;

		// y values

		return new RasterRect(x, y, width, height);
	}

}
