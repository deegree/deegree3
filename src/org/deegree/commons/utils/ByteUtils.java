//$HeadURL$
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

package org.deegree.commons.utils;

import static java.lang.Double.longBitsToDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.toHexString;

import java.io.DataInput;
import java.io.IOException;

/**
 * <code>ByteUtils</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ByteUtils {

    /**
     * @param in
     * @return the int
     * @throws IOException
     */
    public static int readLEInt( DataInput in )
                            throws IOException {
        return in.readUnsignedByte() + ( in.readUnsignedByte() << 8 ) + ( in.readUnsignedByte() << 16 )
               + ( in.readUnsignedByte() << 24 );
    }

    /**
     * @param in
     * @return the long
     * @throws IOException
     */
    public static long readLELong( DataInput in )
                            throws IOException {
        return in.readUnsignedByte() + ( (long) in.readUnsignedByte() << 8 ) + ( (long) in.readUnsignedByte() << 16 )
               + ( (long) in.readUnsignedByte() << 24 ) + ( (long) in.readUnsignedByte() << 32 )
               + ( (long) in.readUnsignedByte() << 40 ) + ( (long) in.readUnsignedByte() << 48 )
               + ( (long) in.readUnsignedByte() << 56 );
    }

    /**
     * @param in
     * @return the double
     * @throws IOException
     */
    public static double readLEDouble( DataInput in )
                            throws IOException {
        return longBitsToDouble( readLELong( in ) );
    }

    /**
     * @param str
     *            a hex string with a sequence of byte values
     * @return a byte array with the values
     */
    public static byte[] decode( String str ) {
        byte[] res = new byte[str.length() / 2];
        int idx = -1;
        while ( str.length() > 1 ) {
            String sub = str.substring( 0, 2 );
            str = str.substring( 2 );
            res[++idx] = (byte) parseInt( sub, 16 );
        }
        return res;
    }

    /**
     * @param bs
     * @return a hex string with a sequence of byte values
     */
    public static String encode( byte[] bs ) {
        StringBuilder sb = new StringBuilder();
        for ( byte b : bs ) {
            if ( b < 0x10 && b >= 0 ) {
                sb.append( "0" ).append( toHexString( b ) );
            } else {
                sb.append( toHexString( b & 0xff ) );
            }
        }
        return sb.toString();
    }

}
