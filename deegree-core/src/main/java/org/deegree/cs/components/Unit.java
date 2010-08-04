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
package org.deegree.cs.components;

import static org.deegree.cs.utilities.MappingUtils.matchEPSGString;
import static org.deegree.cs.utilities.ProjectionUtils.DTR;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;

/**
 * The <code>Unit</code> class defines a mechanism to convert between different measurements units, such as british_yard
 * and meter.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public final class Unit extends CRSIdentifiable {

    /**
     * Unit of angle.
     */
    public static final Unit RADIAN = new Unit( "rad", "Radian" );

    /**
     * Unit of angle.
     */
    public static final Unit DEGREE = new Unit( "°", "Degree", DTR, RADIAN );

    /**
     * 1 degree minute second hemisphere = 1 degree
     */
    public static final Unit DMSH = DEGREE;

    /**
     * Unit of angle, which is defined to be 1/3600 of a degree, or PI/(180*3600) Radian.
     */
    public static final Unit ARC_SEC = new Unit( "\"", "Arcsecond", DTR * ( 1. / 3600 ), RADIAN );

    /**
     * Base unit of length.
     */
    public static final Unit METRE = new Unit( "m", "Metre" );

    /**
     * British yard; unit of length.
     */
    public static final Unit BRITISHYARD = new Unit( "y", "britishyard", 0.9144, METRE );

    /**
     * foot; unit of length, with base unit of 0.304 meter.
     */
    public static final Unit FOOT = new Unit( "ft", "foot", 0.3048, METRE );

    /**
     * US foot; unit of length, with base unit of 0.304 meter.
     */
    public static final Unit USFOOT = new Unit( "ft", "usfoot", 0.3048006096012192, METRE );

    /**
     * Clarkse foot; unit of length, with base unit of 0.304797 meter.
     */
    public static final Unit CLARK_FOOT = new Unit( "ftCla", "cfoot", 0.3047972654, METRE );

    /**
     * Indian foot; unit of length, with base unit of 0.304799 meter.
     */
    public static final Unit INDIAN_FOOT = new Unit( "ftCla", "cfoot", 0.30479951024814694, METRE );

    /**
     * Base unit of time.
     */
    public static final Unit SECOND = new Unit( "s", "Second" );

    /**
     * Unit of time.
     */
    public static final Unit MILLISECOND = new Unit( "ms", "milli second", 0.001, SECOND );

    /**
     * Unit of time.
     */
    public static final Unit DAY = new Unit( "day", "day", 24 * 60 * 60, SECOND );

    /**
     * The unit's symbol.
     */
    private final String symbol;

    /**
     * The scale factor.
     */
    private final double scale;

    /**
     * Base unit, or <code>this</code> if none.
     */
    private final Unit baseType;

    /**
     * Unit constructor.
     * 
     * @param symbol
     * @param name
     *            of the unit, e.g. metre
     * @param code
     *            of the unit
     */
    public Unit( final String symbol, final String name, final CRSCodeType code ) {
        super( new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null, null ) );
        this.symbol = symbol;
        this.scale = 1;
        this.baseType = this;
    }

    /**
     * Unit constructor.
     * 
     * @param symbol
     * @param name
     *            of the unit, e.g. metre
     */
    public Unit( final String symbol, final String name ) {
        this( symbol, name, CRSCodeType.valueOf( name ) );
    }

    /**
     * Unit constructor, which uses the name as the id.
     * 
     * @param symbol
     *            of the units, e.g. 'm'
     * @param name
     *            human readable name, e.g. metre
     * @param scale
     *            to convert to the base type.
     * @param baseType
     *            the baseType
     */
    public Unit( final String symbol, String name, final double scale, final Unit baseType ) {
        this( symbol, name, CRSCodeType.valueOf( name ), scale, baseType );
    }

    /**
     * Unit constructor.
     * 
     * @param symbol
     *            of the units, e.g. 'm'
     * @param name
     *            human readable name, e.g. metre
     * @param code
     *            of the unit.
     * @param scale
     *            to convert to the base type.
     * @param baseType
     *            the baseType
     */
    public Unit( final String symbol, String name, CRSCodeType code, final double scale, final Unit baseType ) {
        super( new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null, null ) );
        this.symbol = symbol;
        this.scale = scale;
        this.baseType = baseType;
    }

    /**
     * Will create a unit from the given String. If no appropriate unit was found <code>null<code> will be returned.
     * 
     * @param unit
     *            to convert to an actual unit.
     * @return a unit or <code>null</code>
     */
    public static Unit createUnitFromString( final String unit ) {
        if ( unit != null && !"".equals( unit.trim() ) ) {
            String t = unit.trim().toUpperCase();
            if ( "METRE".equals( t ) || "METER".equals( t ) || "M".equals( t ) || matchEPSGString( unit, "uom", "9001" ) ) {
                return METRE;
            } else if ( "BRITISHYARD".equals( t ) || "Y".equals( t ) || matchEPSGString( unit, "uom", "9060" ) ) {
                return BRITISHYARD;
            } else if ( "CLARKFOOT".equals( t ) || "FTCLA".equals( t ) || matchEPSGString( unit, "uom", "9005" ) ) {
                return CLARK_FOOT;
            } else if ( "INDIANFOOT".equals( t ) || "FTIND".equals( t ) || matchEPSGString( unit, "uom", "9080" ) ) {
                return INDIAN_FOOT;
            } else if ( "FOOT".equals( t ) || matchEPSGString( unit, "uom", "9002" ) ) {
                return FOOT;
            } else if ( "USFOOT".equals( t ) || "FT".equals( t ) || matchEPSGString( unit, "uom", "9003" ) ) {
                return USFOOT;
            } else if ( "DEGREE".equals( t ) || "°".equals( t ) || matchEPSGString( unit, "uom", "9102" )
                        || matchEPSGString( unit, "uom", "9122" ) ) {
                return DEGREE;
            } else if ( "RADIAN".equals( t ) || "rad".equals( t ) || matchEPSGString( unit, "uom", "9101" ) ) {
                return RADIAN;
            } else if ( "SECOND".equals( t ) || "S".equals( t ) ) {
                return SECOND;
            } else if ( "MILLISECOND".equals( t ) || "MS".equals( t ) ) {
                return MILLISECOND;
            } else if ( "DAY".equals( t ) || "D".equals( t ) ) {
                return DAY;
            } else if ( "Arcsecond".equalsIgnoreCase( t ) || matchEPSGString( unit, "uom", "9104" ) ) {
                return ARC_SEC;
            }

        }
        return null;
    }

    /**
     * Check if amount of the specified unit can be converted into amount of this unit.
     * 
     * @param other
     * @return true if this unit can be converted into the other unit
     */
    public boolean canConvert( final Unit other ) {
        return ( baseType == other.baseType ) || ( baseType != null && baseType.equals( other.baseType ) );
    }

    /**
     * Convert a value in this unit to the given unit if possible.
     * 
     * @param value
     *            to be converted
     * @param targetUnit
     *            to convert to
     * @return the converted value or the same value if this unit equals given unit.
     * @throws IllegalArgumentException
     *             if no conversion can be applied.
     */
    public final double convert( final double value, final Unit targetUnit ) {
        if ( this.equals( targetUnit ) ) {
            return value;
        }
        if ( canConvert( targetUnit ) ) {
            return ( value * scale ) / targetUnit.scale;
        }
        throw new IllegalArgumentException( "Can't convert from \"" + this + "\" to \"" + targetUnit + "\"." );
    }

    /**
     * Convert a value in this unit to the base unit, e.g. degree->radians
     * 
     * @param value
     *            to be converted
     * @return the converted value or the same value if this unit is a base unit.
     */
    public final double toBaseUnits( final double value ) {
        if ( isBaseType() ) {
            return value;
        }
        return value * scale;
    }

    /**
     * @return the symbol of this unit.
     */
    @Override
    public String toString() {
        return symbol;
    }

    /**
     * Compare this unit symbol with the specified object for equality. Only symbols are compared; other parameters are
     * ignored.
     */
    @Override
    public boolean equals( final Object object ) {
        if ( object != null && object instanceof Unit ) {
            final Unit that = (Unit) object;
            return symbol.equals( that.symbol ) && ( Math.abs( this.scale - that.scale ) < 1E-10 );
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
        long code = 32452843;
        code = code * 37 + symbol.hashCode();
        long ll = Double.doubleToLongBits( scale );
        code = code * 37 + (int) ( ll ^ ( ll >>> 32 ) );
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    /**
     * @return the scale to convert to the base unit.
     */
    public final double getScale() {
        return scale;
    }

    /**
     * @return true if this is a base type
     */
    public final boolean isBaseType() {
        return this.equals( this.baseType );
    }

}
