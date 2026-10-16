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
