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
 * Allows writing values as a string in little endian encoding and hex format.
 *
 * @author Sebastian Baumhekel
 */
public class StringValueSetter extends ValueSetter {

	private final StringBuilder sb = new StringBuilder();

	/**
	 * Constructs an instance.
	 * @param axis
	 * @param srid PostGIS spatial reference identifier, must not be greater or equals
	 * zero to be written
	 */
	public StringValueSetter(Axis axis, int srid) {
		super(axis, srid);
	}

	/**
	 * Constructs an instance.
	 */

	/**
	 * Gets the written value.
	 * @return value
	 */
	public String getValue() {
		return sb.toString();
	}

	@Override
	public void setByte(byte b) {
		sb.append(EWKBUtils.HEX_CHAR[(b >> 4) & 0xF]);
		sb.append(EWKBUtils.HEX_CHAR[b & 0xF]);
	}

}
