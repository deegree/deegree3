//$HeadURL:svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/coverage/raster/data/DataType.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.model.coverage.raster.data;

import java.awt.image.DataBuffer;

/**
 * Enumeration for all supported data types.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author:rbezema $
 *
 * @version $Revision:11404 $, $Date:2008-04-23 15:38:27 +0200 (Mi, 23 Apr 2008) $
 * 
 */
public enum DataType {

    /**
     * 
     */
    BYTE( 1 ), 
    /**
     * 
     */
    INT( 4 ), 
    /**
     * 
     */
    USHORT( 2 ), 
    /**
     * 
     */
    SHORT( 2 ), 
    /**
     * 
     */
    FLOAT( 4 ),
    /**
     * 
     */
    DOUBLE( 8 ), 
    /**
     * 
     */
    UNDEFINED( 0 );

    private final int size;

    DataType( int size ) {
        this.size = size;
    }

    /**
     * @return size of data type in bytes
     */
    public final int getSize() {
        return size;
    }

    /**
     * Convert from {@link DataBuffer}-Types to {@link DataType}s.
     * 
     * @param type The {@link DataBuffer}-Type (eg. TYPE_BYTE, etc.)
     * @return The according DataType
     */
    public static DataType fromDataBufferType( int type ) {
        switch ( type ) {
        case DataBuffer.TYPE_BYTE:
            return BYTE;
        case DataBuffer.TYPE_USHORT:
            return USHORT;
        case DataBuffer.TYPE_SHORT:
            return SHORT;
        case DataBuffer.TYPE_INT:
            return INT;
        case DataBuffer.TYPE_FLOAT:
            return FLOAT;
        case DataBuffer.TYPE_DOUBLE:
            return DOUBLE;
        default:
            return UNDEFINED;
        }
    }

    /**
     * Convert from {@link DataType}s to {@link DataBuffer}-Types.
     * 
     * @param type The DataType.
     * @return The according {@link DataBuffer}-Type (eg. TYPE_BYTE, etc.)
     */
    public static int toDataBufferType( DataType type ) {
        if ( type == BYTE )
            return DataBuffer.TYPE_BYTE;
        if ( type == SHORT )
            return DataBuffer.TYPE_SHORT;
        if ( type == USHORT )
            return DataBuffer.TYPE_USHORT;
        if ( type == INT )
            return DataBuffer.TYPE_INT;
        if ( type == FLOAT )
            return DataBuffer.TYPE_FLOAT;
        if ( type == DOUBLE )
            return DataBuffer.TYPE_DOUBLE;
        return DataBuffer.TYPE_UNDEFINED;
    }
}