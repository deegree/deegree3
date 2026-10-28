/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2004 Paul Ramsey, pramsey@refractions.net
 Copyright (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 Copyright (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 Copyright (C) 2018-2023 Sebastian Baumhekel, sebastian.baumhekel@gmail.com
 Copyright (C) 2026 grit graphische Informationstechnik Beratungsgesellschaft mbH

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

 e-mail: info@deegree.org

 website: https://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.geometry.io.ewkb;

/**
 * A base class for value setters.
 *
 * @author Sebastian Baumhekel
 */
public abstract class ValueSetter {

	private final Axis axis;

	private final int srid;

	/**
	 * Constructs an instance.
	 */
	public ValueSetter(final Axis axis, int srid) {
		this.axis = axis;
		this.srid = srid;
	}

	/**
	 * Get the count of dimensions
	 * @return
	 */
	public Axis getAxis() {
		return axis;
	}

	/**
	 * Get the srid to be written
	 * @return PostGIS spatial reference identifier, is greater or equals zero if it
	 * should be written
	 */
	public int getSrid() {
		return srid;
	}

	/**
	 * Sets a byte.
	 * @param b byte value to set with
	 */
	public abstract void setByte(byte b);

	/**
	 * Writes a double.
	 * @param data double value to be set with
	 */
	public void setDouble(double data) {
		setLong(Double.doubleToLongBits(data));
	}

	/**
	 * Sets a 32-Bit integer
	 * @param value int value to be set with
	 */
	public void setInt(int value) {
		setByte((byte) value);
		setByte((byte) (value >> 8));
		setByte((byte) (value >> 16));
		setByte((byte) (value >> 24));
	}

	/**
	 * Sets a long value. This is not needed directly, but as a nice side-effect from
	 * setDouble.
	 * @param value value value to be set with
	 */
	public void setLong(long value) {
		setByte((byte) value);
		setByte((byte) (value >> 8));
		setByte((byte) (value >> 16));
		setByte((byte) (value >> 24));
		setByte((byte) (value >> 32));
		setByte((byte) (value >> 40));
		setByte((byte) (value >> 48));
		setByte((byte) (value >> 56));
	}

}
