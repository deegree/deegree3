//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2011 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.sqldialect.oracle.sdo;

import java.sql.SQLException;

import org.deegree.sqldialect.oracle.sdo.SDOGeometryConverter.GeomHolder;

import oracle.jdbc.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.Datum;
import oracle.sql.NUMBER;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

/**
 * Helper class to convert between Oracle STRUCT JDBC and Java primitives
 * 
 * <p>
 * The code of these classes is partial inspired from the Geotools Oracle Plugin, which uses the same approach and is
 * also licensed under GNU LGPL 2.1.
 * </p>
 * 
 * @see http://geotools.org (C) 2003-2008, Open Source Geospatial Foundation (OSGeo), Plugin jdbc-oracle
 *      org.geotools.data.oracle.sdo.GeometryConverter
 * 
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * 
 * @version $Revision$, $Date$
 */
public class OracleObjectTools {

    protected static int fromInteger( Datum data, int defaultValue )
                            throws SQLException {
        if ( data == null )
            return defaultValue;
        return ( (NUMBER) data ).intValue();
    }

    protected static int[] fromIntegerArray( ARRAY data )
                            throws SQLException {
        if ( data == null )
            return null;

        return data.getIntArray();
    }

    protected static double[] fromDoubleArray( ARRAY data, final double defaultValue )
                            throws SQLException {
        if ( data == null )
            return null;
        if ( defaultValue == 0 )
            return data.getDoubleArray();
        return fromDoubleArray( data.getOracleArray(), defaultValue );
    }

    protected static double[] fromDoubleArray( Datum data[], final double defaultValue )
                            throws SQLException {
        if ( data == null )
            return null;
        double res[] = new double[data.length];
        for ( int i = 0; i < data.length; i++ ) {
            res[i] = fromDouble( data[i], defaultValue );
        }
        return res;
    }

    protected static double fromDouble( Datum data, final double defaultValue )
                            throws SQLException {
        if ( data == null )
            return defaultValue;
        return ( (NUMBER) data ).doubleValue();
    }

    protected static double[] fromDoubleArray( STRUCT struct, final double defaultValue )
                            throws SQLException {
        if ( struct == null )
            return null;
        return fromDoubleArray( struct.getOracleAttributes(), defaultValue );
    }

    protected static STRUCT toSDOGeometry( GeomHolder h, OracleConnection conn ) 
                            throws SQLException {
        return toSDOGeometry( h.gtype, h.srid, h.elem_info, h.ordinates, conn);
    }
    
    /**
     * Convert SDO_Geometry informations into Oracle JDBC STRUCT element
     */
    protected static STRUCT toSDOGeometry( int gtype, int srid, int[] elemInfo, double[] ordinates,
                                           OracleConnection conn )
                            throws SQLException {
        NUMBER sdoGtype = toNumber( gtype );
        NUMBER sdoSrid = toNumber( srid );
        STRUCT sdoPoint = null;
        ARRAY sdoElemInfo = null;
        ARRAY sdoOrdinates = null;

        /*
         * a single 2/3D point will be stored optimized as SDO_POINT_TYPE which is preferred by oracle
         */

        if ( elemInfo.length == 3 && elemInfo[0] == 1 && elemInfo[1] == 1 && elemInfo[2] == 1 && ordinates.length > 2
             && ordinates.length < 4 ) {
            NUMBER z = null;
            if ( ordinates.length > 2 )
                z = toNumber( ordinates[2] );
            Datum elements[] = new Datum[] { toNumber( ordinates[0] ), toNumber( ordinates[1] ), z, };
            sdoPoint = toStruct( elements, "MDSYS.SDO_POINT_TYPE", conn );
        } else {
            sdoElemInfo = toArray( elemInfo, "MDSYS.SDO_ELEM_INFO_ARRAY", conn );
            sdoOrdinates = toArray( ordinates, "MDSYS.SDO_ORDINATE_ARRAY", conn );
        }

        Datum elements[] = new Datum[] { sdoGtype, // SDO_GTYPE
                                        sdoSrid, // SDO_SRID
                                        sdoPoint, // SDO_POINT_TYPE
                                        sdoElemInfo, // SDO_ELEM_INFO_ARRAY
                                        sdoOrdinates // SDO_ORDINATE_ARRAY
        };
        return toStruct( elements, "MDSYS.SDO_GEOMETRY", conn );
    }

    protected static final NUMBER toNumber( int value )
                            throws SQLException {
        return new NUMBER( value );
    }

    protected static final NUMBER toNumber( double value )
                            throws SQLException {
        if ( Double.isNaN( value ) )
            return null;
        else
            return new NUMBER( value );
    }

    protected static final STRUCT toStruct( Datum elements[], String type, OracleConnection conn )
                            throws SQLException {
        StructDescriptor descr = StructDescriptor.createDescriptor( type, conn );

        return new STRUCT( descr, conn, elements );
    }

    protected static final ARRAY toArray( double elements[], String type, OracleConnection conn )
                            throws SQLException {
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor( type, conn );

        return new ARRAY( descriptor, conn, elements );
    }

    protected static final ARRAY toArray( int elements[], String type, OracleConnection conn )
                            throws SQLException {
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor( type, conn );

        return new ARRAY( descriptor, conn, elements );
    }

}
