//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.commons.utils;

import static java.lang.Double.longBitsToDouble;

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

}
