/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2026 by:
 - Department of Geography, University of Bonn -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -
 and others

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

public class EWKBUtils {

	/**
	 * Big endian encoding.
	 */
	public static final byte BIG_ENDIAN = 0;

	/**
	 * Little endian encoding.
	 */
	public static final byte LITTLE_ENDIAN = 1;

	/**
	 * Characters for converting data to hex strings.
	 */
	public static final char[] HEX_CHAR = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F' };

	/**
	 * Converts the given hexadecimal character to its byte representation. i.e.
	 * 'A'-&gt;10
	 * @param c character
	 * @return byte value
	 * @throws IllegalArgumentException if character is not '0'-'9', 'a'-'f' or 'A'-'F'
	 */
	public static int toHexByte(char c) {
		// '0'
		if (c >= 0x30) {
			// '9'
			if (c <= 0x39) {
				return c - 0x30;
			}
			// 'A'
			else if (c >= 0x41) {
				// 'F'
				if (c <= 0x46) {
					return c - 0x37;
				} // 'a' - 'f'
				else if ((c >= 0x61) && (c <= 0x66)) {
					return c - 0x57;
				}
			}
		}
		throw new IllegalArgumentException("character is no hexadecimal digit: " + c);
	}

}
