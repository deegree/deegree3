/*
 * PostGIS extension for PostgreSQL JDBC driver
 *
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 * (C) 2018-2023 Sebastian Baumhekel, sebastian.baumhekel@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.sebasbaumh.postgis.binary;

import io.github.sebasbaumh.postgis.PostGisUtil;

/**
 * Allows reading values from a string.
 * @author Sebastian Baumhekel
 */
public class StringValueGetter extends ValueGetter
{
	private int position;
	private final String value;

	/**
	 * Constructs an instance.
	 * @param value value as hex string
	 */
	public StringValueGetter(String value)
	{
		this.value = value;
	}

	@Override
	protected int getNextByte()
	{
		// get current position (and respect that every byte consists of 2 hex characters)
		int index = position * 2;
		// then advance the position to the next byte
		position++;
		return ((PostGisUtil.toHexByte(value.charAt(index)) << 4) | PostGisUtil.toHexByte(value.charAt(index + 1)));
	}

}
