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

    /**
     * @param other
     *            to test against
     * @return true if the given singlevalue matches this one, e.g. the value and types are equal.
     */
    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof SingleValue<?> ) {
            SingleValue<?> that = (SingleValue<?>) other;
            if ( this.type.isCompatible( that.type ) ) {
                return ( value == null ) ? that.value == null : value.equals( that.value );
            }
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long result = 32452843;
        result = result * 37 + this.type.hashCode();
        result = result * 37 + this.value.hashCode();
        return (int) ( result >>> 32 ) ^ (int) result;
    }

    /**
     * @param type
     * @param value
     * @return the typed SingleValue.
     * @throws NumberFormatException
     */
    public static SingleValue<?> createFromValue( String type, Number value )
                            throws NumberFormatException {
        ValueType determined = ValueType.fromString( type );
        SingleValue<?> result = null;
        switch ( determined ) {
        case Byte:
        case Short:
            result = new SingleValue<Short>( determined, value.shortValue() );
            break;
        case Integer:
            result = new SingleValue<Integer>( determined, value.intValue() );
            break;
        case Long:
            result = new SingleValue<Long>( determined, value.longValue() );
            break;
        case Double:
            result = new SingleValue<Double>( determined, value.doubleValue() );
            break;
        case Float:
            result = new SingleValue<Float>( determined, value.floatValue() );
            break;
        default:
            result = new SingleValue<String>( determined, value.toString() );
        }
        return result;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "{" ).append( type.toString() ).append( "}" );
        sb.append( value.toString() );
        return sb.toString();
    }
}
