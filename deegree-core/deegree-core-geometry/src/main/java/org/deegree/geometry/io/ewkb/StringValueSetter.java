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
 * Allows writing values as a string in little endian encoding and hex format.
 * @author Sebastian Baumhekel
 */
public class StringValueSetter extends ValueSetter
{
	private final StringBuilder sb = new StringBuilder();

	/**
	 * Constructs an instance.
	 */
	public StringValueSetter()
	{
	}

	/**
	 * Gets the written value.
	 * @return value
	 */
	public String getValue()
	{
		return sb.toString();
	}

	@Override
	public void setByte(byte b)
	{
		sb.append(PostGisUtil.HEX_CHAR[(b >> 4) & 0xF]);
		sb.append(PostGisUtil.HEX_CHAR[b & 0xF]);
	}

}
