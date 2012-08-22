//$HeadURL$
/* **********************************************************************
 *
 *  BBN Corporation
 *  10 Moulton St.
 *  Cambridge, MA 02138
 *  (617) 873-2000
 *
 *  Copyright (C) 1998
 *  This software is subject to copyright protection under the laws of
 *  the United States and other countries.
 *
 * **********************************************************************
 *
 * $Source$
 * $RCSfile$
 * $Revision$
 * $Date$
 * $Author$
 *
 * **********************************************************************
 */

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

package org.deegree.model.spatialschema;

/**
 * Utilities for reading and writing the components of binary files.
 *
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision$ $Date$ modified
 *
 * <B>Last changes<B>:<BR>
 * 25.11.1999 ap: memory allocation dynaminized<BR>
 * 17.01.2000 ap: method SHPPoint readPoint(byte[] b, int off) modified<BR>
 * 17.01.2000 ap: method SHPEnvelope readBox(byte[] b, int off) modified<BR>
 * 17.01.2000 ap: method writePoint(..) modified<BR>
 * 25.01.2000 ap: method writeBELong(..) added<BR>
 * 25.01.2000 ap: method writeBEDouble(..) added<BR>
 * 25.01.2000 ap: method readBELong(..) added<BR>
 * 25.01.2000 ap: method readBEDouble(..) added<BR>
 * 22.04.2000 ap: method readBEShort(byte[] b, int off) added<BR>
 * 22.04.2000 ap: method readLEShort(byte[] b, int off) added<BR>
 * 22.04.2000 ap: method writeBEShort(byte[] b, int off) added<BR>
 * 22.04.2000 ap: method writeLEShort(byte[] b, int off) added<BR>
 *
 * <p>
 * ----------------------------------------------------------------------------
 * </p>
 *
 * @author Andreas Poth
 * @version $Revision$ $Date$
 *          <p>
 */

public class ByteUtils {

    /**
     * method: readBEShort(byte[] b, int off)<BR>
     * Reads a big endian small integer.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     *
     * not tested!
     */
    public static int readBEShort( byte[] b, int off ) {

        return ( ( ( b[off + 0] & 0xff ) << 8 ) | ( ( b[off + 1] & 0xff ) ) );

    }

    /**
     * method: readLEShort(byte[] b, int off)<BR>
     * Reads a little endian small integer.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     *
     * not tested!
     */
    public static int readLEShort( byte[] b, int off ) {

        return ( ( ( b[off + 1] & 0xff ) << 8 ) | ( ( b[off + 0] & 0xff ) ) );

    }

    /**
     * Reads a big endian integer.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     */
    public static int readBEInt( byte[] b, int off ) {

        return ( ( ( b[off + 0] & 0xff ) << 24 ) | ( ( b[off + 1] & 0xff ) << 16 ) | ( ( b[off + 2] & 0xff ) << 8 ) | ( ( b[off + 3] & 0xff ) ) );

    }

    /**
     * Reads a little endian integer.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     */
    public static int readLEInt( byte[] b, int off ) {

        return ( ( ( b[off + 3] & 0xff ) << 24 ) | ( ( b[off + 2] & 0xff ) << 16 ) | ( ( b[off + 1] & 0xff ) << 8 ) | ( ( b[off + 0] & 0xff ) ) );

    }

    /**
     * method: readLELong(byte[] b, int off)<BR>
     * Reads a little endian 8 byte integer.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the long resides
     * @return the long read from the buffer at the offset location
     */
    public static long readLELong( byte[] b, int off ) {

        return ( ( ( b[off + 0] & 0xffL ) ) | ( ( b[off + 1] & 0xffL ) << 8 ) | ( ( b[off + 2] & 0xffL ) << 16 )
                 | ( ( b[off + 3] & 0xffL ) << 24 ) | ( ( b[off + 4] & 0xffL ) << 32 )
                 | ( ( b[off + 5] & 0xffL ) << 40 ) | ( ( b[off + 6] & 0xffL ) << 48 ) | ( ( b[off + 7] & 0xffL ) << 56 ) );

    }

    /**
     * method: readBELong(byte[] b, int off)<BR>
     * Reads a little endian 8 byte integer.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the long resides
     * @return the long read from the buffer at the offset location
     */
    public static long readBELong( byte[] b, int off ) {

        return ( ( ( b[off + 7] & 0xffL ) ) | ( ( b[off + 6] & 0xffL ) << 8 ) | ( ( b[off + 5] & 0xffL ) << 16 )
                 | ( ( b[off + 4] & 0xffL ) << 24 ) | ( ( b[off + 3] & 0xffL ) << 32 )
                 | ( ( b[off + 2] & 0xffL ) << 40 ) | ( ( b[off + 1] & 0xffL ) << 48 ) | ( ( b[off + 0] & 0xffL ) << 56 ) );

    }

    /**
     * Reads a little endian float.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the float resides
     * @return the float read from the buffer at the offset location
     */
    public static float readLEFloat( byte[] b, int off ) {

        float result = Float.intBitsToFloat( readLEInt( b, off ) );

        return result;

    }

    /**
     * Reads a big endian float.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the float resides
     * @return the float read from the buffer at the offset location
     */
    public static float readBEFloat( byte[] b, int off ) {

        float result = Float.intBitsToFloat( readBEInt( b, off ) );

        return result;

    }

    /**
     * method: readLEDouble(byte[] b, int off)<BR>
     * Reads a little endian double.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the double resides
     * @return the double read from the buffer at the offset location
     */
    public static double readLEDouble( byte[] b, int off ) {

        double result = Double.longBitsToDouble( readLELong( b, off ) );

        return result;

    }

    /**
     * method: readBEDouble(byte[] b, int off)<BR>
     * Reads a big endian double.
     *
     * @param b
     *            the raw data buffer
     * @param off
     *            the offset into the buffer where the double resides
     * @return the double read from the buffer at the offset location
     */
    public static double readBEDouble( byte[] b, int off ) {

        double result = Double.longBitsToDouble( readBELong( b, off ) );

        return result;

    }

    /**
     * method: writeBEShort(byte[] b, int off, int val)<BR>
     * Writes the given short to the given buffer at the given location in big endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the short to write
     * @return the number of bytes written
     *
     * not tested!
     */
    public static int writeBEShort( byte[] b, int off, int val ) {

        b[off + 0] = (byte) ( ( val >> 8 ) & 0xff );
        b[off + 1] = (byte) ( ( val ) & 0xff );

        return 2;

    }

    /**
     * method: writeLEShort(byte[] b, int off, int val)<BR>
     * Writes the given short to the given buffer at the given location in big endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the short to write
     * @return the number of bytes written
     *
     * not tested!
     */
    public static int writeLEShort( byte[] b, int off, int val ) {

        b[off + 0] = (byte) ( ( val ) & 0xff );
        b[off + 1] = (byte) ( ( val >> 8 ) & 0xff );

        return 2;

    }

    /**
     * method: writeBEInt(byte[] b, int off, int val)<BR>
     * Writes the given integer to the given buffer at the given location in big endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the integer to write
     * @return the number of bytes written
     */
    public static int writeBEInt( byte[] b, int off, int val ) {

        b[off + 0] = (byte) ( ( val >> 24 ) & 0xff );
        b[off + 1] = (byte) ( ( val >> 16 ) & 0xff );
        b[off + 2] = (byte) ( ( val >> 8 ) & 0xff );
        b[off + 3] = (byte) ( ( val ) & 0xff );

        return 4;

    }

    /**
     * method: writeLEInt(byte[] b, int off, int val)<BR>
     * Writes the given integer to the given buffer at the given location in little endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the integer to write
     * @return the number of bytes written
     */
    public static int writeLEInt( byte[] b, int off, int val ) {

        b[off + 0] = (byte) ( ( val ) & 0xff );
        b[off + 1] = (byte) ( ( val >> 8 ) & 0xff );
        b[off + 2] = (byte) ( ( val >> 16 ) & 0xff );
        b[off + 3] = (byte) ( ( val >> 24 ) & 0xff );

        return 4;

    }

    /**
     * method: writeLELong(byte[] b, int off, long val)<BR>
     * Writes the given long to the given buffer at the given location in little endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the long to write
     * @return the number of bytes written
     */
    public static int writeLELong( byte[] b, int off, long val ) {

        b[off + 0] = (byte) ( ( val ) & 0xff );
        b[off + 1] = (byte) ( ( val >> 8 ) & 0xff );
        b[off + 2] = (byte) ( ( val >> 16 ) & 0xff );
        b[off + 3] = (byte) ( ( val >> 24 ) & 0xff );
        b[off + 4] = (byte) ( ( val >> 32 ) & 0xff );
        b[off + 5] = (byte) ( ( val >> 40 ) & 0xff );
        b[off + 6] = (byte) ( ( val >> 48 ) & 0xff );
        b[off + 7] = (byte) ( ( val >> 56 ) & 0xff );

        return 8;

    }

    /**
     * method: writeBELong(byte[] b, int off, long val)<BR>
     * Writes the given long to the given buffer at the given location in big endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the long to write
     * @return the number of bytes written
     */
    public static int writeBELong( byte[] b, int off, long val ) {

        b[off + 0] = (byte) ( ( val >> 56 ) & 0xff );
        b[off + 1] = (byte) ( ( val >> 48 ) & 0xff );
        b[off + 2] = (byte) ( ( val >> 40 ) & 0xff );
        b[off + 3] = (byte) ( ( val >> 32 ) & 0xff );
        b[off + 4] = (byte) ( ( val >> 24 ) & 0xff );
        b[off + 5] = (byte) ( ( val >> 16 ) & 0xff );
        b[off + 6] = (byte) ( ( val >> 8 ) & 0xff );
        b[off + 7] = (byte) ( ( val ) & 0xff );

        return 8;

    }

    /**
     * method: writeLEDouble(byte[] b, int off, double val)<BR>
     * Writes the given double to the given buffer at the given location in little endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the double to write
     * @return the number of bytes written
     */
    public static int writeLEDouble( byte[] b, int off, double val ) {

        return writeLELong( b, off, Double.doubleToLongBits( val ) );

    }

    /**
     * method: writeBEDouble(byte[] b, int off, double val)<BR>
     * Writes the given double to the given buffer at the given location in big endian format.
     *
     * @param b
     *            the data buffer
     * @param off
     *            the offset into the buffer where writing should occur
     * @param val
     *            the double to write
     * @return the number of bytes written
     */
    public static int writeBEDouble( byte[] b, int off, double val ) {

        return writeBELong( b, off, Double.doubleToLongBits( val ) );

    }

}
