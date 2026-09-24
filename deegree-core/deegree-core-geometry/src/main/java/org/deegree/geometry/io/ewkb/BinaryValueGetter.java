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

/**
 * Allows reading values from a byte array.
 * @author Sebastian Baumhekel
 */
public class BinaryValueGetter extends ValueGetter
{
	private int position;
	private final byte[] value;

	/**
	 * Constructs an instance.
	 * @param value value
	 * @param offset offset to use
	 */
	public BinaryValueGetter(byte[] value, int offset)
	{
		this.value = value;
		this.position = offset;
	}

	@Override
	public int getInt()
	{
		// get current position and advance it
		int index = position;
		position += 4;
		// be sure to use ints as Java byte is signed
		int i1 = (value[index]) & 0xFF;
		int i2 = (value[index + 1]) & 0xFF;
		int i3 = (value[index + 2]) & 0xFF;
		int i4 = (value[index + 3]) & 0xFF;
		// optimize by not calling getNextByte() for every byte, but just taking them directly from the array
		return funcInt.getInt(i1, i2, i3, i4);
	}

	@Override
	public long getLong()
	{
		// get current position and advance it
		int index = position;
		position += 8;
		// be sure to use ints as Java byte is signed
		int i1 = (value[index]) & 0xFF;
		int i2 = (value[index + 1]) & 0xFF;
		int i3 = (value[index + 2]) & 0xFF;
		int i4 = (value[index + 3]) & 0xFF;
		int i5 = (value[index + 4]) & 0xFF;
		int i6 = (value[index + 5]) & 0xFF;
		int i7 = (value[index + 6]) & 0xFF;
		int i8 = (value[index + 7]) & 0xFF;
		// optimize by not calling getNextByte() for every byte, but just taking them directly from the array
		return funcLong.getLong(i1, i2, i3, i4, i5, i6, i7, i8);
	}

	@Override
	protected int getNextByte()
	{
		// get current position and advance it to the next byte
		int index = position;
		position++;
		// make sure the signed byte in the array gets converted to an unsigned value
		return (value[index]) & 0xFF;
	}

}
