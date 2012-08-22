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

package org.deegree.io.dbaseapi;

/**
 * Class representing a field descriptor of a dBase III/IV file
 *
 * @version 28.04.2000
 * @author Andreas Poth
 */

public class FieldDescriptor {

    /**
     * fieldinformation as byte array
     */
    private byte[] data = null;

    /**
     * constructor recieves name and type of the field, the length of the field in bytes and the
     * decimalcount. the decimalcount is only considered if type id "N" or "F", it's maxvalue if
     * fieldlength - 2!
     */
    public FieldDescriptor( String name, String type, byte fieldlength, byte decimalcount ) throws DBaseException {

        if ( ( !type.equalsIgnoreCase( "C" ) ) && ( !type.equalsIgnoreCase( "D" ) ) && ( !type.equalsIgnoreCase( "F" ) )
             && ( !type.equalsIgnoreCase( "N" ) ) && ( !type.equalsIgnoreCase( "M" ) )
             && ( !type.equalsIgnoreCase( "L" ) ) )
            throw new DBaseException( "data type is not supported" );

        data = new byte[32];

        // fill first 11 bytes with ASCII zero
        for ( int i = 0; i <= 10; i++ )
            data[i] = 0x0;

        // copy name into the first 11 bytes
        byte[] dum = name.getBytes();

        int cnt = dum.length;

        if ( cnt > 11 )
            cnt = 11;

        for ( int i = 0; i < cnt; i++ )
            data[i] = dum[i];

        byte[] b = type.getBytes();

        data[11] = b[0];

        // set fieldlength
        data[16] = fieldlength;

        // set decimalcount
        if ( type.equalsIgnoreCase( "N" ) || type.equalsIgnoreCase( "F" ) )
            data[17] = decimalcount;
        else
            data[17] = 0;

        // throw DBaseException if the decimalcount is larger then the
        // number off fields required for plotting a float number
        // as string
        if ( data[17] > data[16] - 2 )
            throw new DBaseException( "invalid fieldlength and/or decimalcount" );

        // work area id (don't know if it should be 1)
        data[20] = 1;

        // has no index tag in a MDX file
        data[31] = 0x00;

        // all other fields are reserved!

    }

    /**
     * method: public byte[] getFieldDescriptor() returns the field descriptor as byte array
     */
    public byte[] getFieldDescriptor() {

        return data;

    }

}
