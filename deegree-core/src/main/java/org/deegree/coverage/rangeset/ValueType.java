package org.deegree.coverage.rangeset;

/**
 * 
 * The <code>ValueType</code> class defines simple types for single and interval values.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
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
    public static ValueType fromString( String type ) {
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

    /**
     * @param type
     * @return true if the given types are compatible, e.g. if they are substitutable, byte and short are equals.
     */
    public boolean isCompatible( ValueType type ) {
        return this == type || ( ( this == Short || this == Byte ) && ( type == Short || type == Byte ) );
    }

}