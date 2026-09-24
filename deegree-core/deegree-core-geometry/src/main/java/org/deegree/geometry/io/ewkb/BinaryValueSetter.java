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

import java.io.ByteArrayOutputStream;

/**
 * Allows writing values to a byte array in little endian format.
 * @author Sebastian Baumhekel
 */
public class BinaryValueSetter extends ValueSetter
{
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	/**
	 * Gets the written value.
	 * @return value
	 */
	public byte[] getValue()
	{
		return out.toByteArray();
	}

	@Override
	public void setByte(byte b)
	{
		out.write(b);
	}

	@Override
	public void setInt(int value)
	{
		out.write((value >>> 0) & 0xFF);
		out.write((value >>> 8) & 0xFF);
		out.write((value >>> 16) & 0xFF);
		out.write((value >>> 24) & 0xFF);
	}

	@Override
	public void setLong(long value)
	{
		out.write((int) ((value >>> 0) & 0xFF));
		out.write((int) ((value >>> 8) & 0xFF));
		out.write((int) ((value >>> 16) & 0xFF));
		out.write((int) ((value >>> 24) & 0xFF));
		out.write((int) ((value >>> 32) & 0xFF));
		out.write((int) ((value >>> 40) & 0xFF));
		out.write((int) ((value >>> 48) & 0xFF));
		out.write((int) ((value >>> 56) & 0xFF));
	}

}
