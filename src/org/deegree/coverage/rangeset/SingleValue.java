//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wcs/model/SingleValue.java $
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

package org.deegree.coverage.rangeset;

/**
 * The <code>SingleValue</code> denotes a single typed value in a range set.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: 19041 $, $Date: 2009-08-11 17:04:57 +0200 (Di, 11 Aug 2009) $
 * @param <T>
 *            type of the value
 * 
 */
public class SingleValue<T extends Comparable<T>> {

    /** the value */
    public final T value;

    /** the type */
    public final ValueType type;

    /**
     * @param type
     *            describing the type of the value.
     * @param value
     *            the actual value
     */
    public SingleValue( ValueType type, T value ) {
        this.value = value;
        this.type = type;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof SingleValue ) {
            SingleValue<?> that = (SingleValue<?>) other;
            if ( this.type != that.type ) {
                if ( that.type != ValueType.Void ) {
                    // types do not match
                    return false;
                }
                // the type was not known, try to convert it to the given type.
                try {
                    that = createFromString( this.type.name(), that.value.toString() );
                } catch ( NumberFormatException nfe ) {
                    // the value is not of this type, hence it cannot be equal.
                    return false;
                }
            }
            return ( value == null ) ? that.value == null : value.equals( that.value );
        }
        return false;
    }

    /**
     * @param type
     * @param value
     * @return the typed SingleValue.
     * @throws NumberFormatException
     */
    public static SingleValue<?> createFromString( String type, String value )
                            throws NumberFormatException {
        ValueType determined = ValueType.fromString( type );
        SingleValue<?> result = null;
        switch ( determined ) {
        case Byte:
            byte b = Byte.valueOf( value );
            result = new SingleValue<Byte>( determined, b );
        case Short:
            short s = Short.valueOf( value );
            result = new SingleValue<Short>( determined, s );
            break;
        case Integer:
            int i = Integer.valueOf( value );
            result = new SingleValue<Integer>( determined, i );
            break;
        case Long:
            long l = Long.valueOf( value );
            result = new SingleValue<Long>( determined, l );
            break;
        case Double:
            double d = Double.valueOf( value );
            result = new SingleValue<Double>( determined, d );
            break;
        case Float:
            float f = Float.valueOf( value );
            result = new SingleValue<Float>( determined, f );
            break;
        default:
            result = new SingleValue<String>( determined, value );
        }
        return result;
    }

    /**
     * 
     * The <code>ValueType</code> class defines simple types for single values.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: $, $Date: $
     * 
     */
    public enum ValueType {
        /***/
        Byte,
        /***/
        Short,
        /***/
        Integer,
        /***/
        Long,
        /***/
        Double,
        /***/
        Float,
        /***/
        String,
        /** not known */
        Void;

        /**
         * 
         * @param type
         * @return the value type of string if the given type is not known.
         */
        static ValueType fromString( String type ) {
            ValueType result = Void;
            if ( !( type == null || "".equals( type.trim() ) || "unknown".equalsIgnoreCase( type ) ) ) {
                String determine = type.trim().toLowerCase();
                if ( determine.contains( "byte" ) ) {
                    result = Byte;
                } else if ( determine.contains( "short" ) ) {
                    result = Short;
                } else if ( determine.contains( "int" ) ) {
                    result = Integer;
                } else if ( determine.contains( "long" ) ) {
                    result = Long;
                } else if ( determine.contains( "float" ) ) {
                    result = Float;
                } else if ( determine.contains( "double" ) ) {
                    result = Double;
                } else if ( determine.contains( "string" ) ) {
                    result = String;
                }
            }
            return result;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }

    }
}
