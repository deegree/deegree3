package org.deegree.time.operator;

import org.deegree.commons.tom.datetime.Temporal;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;

public class TimeCompareUtils {

    public static int compareBegin( final TimeGeometricPrimitive a, final TimeGeometricPrimitive b ) {
        final Temporal beginA = new TemporalConverter().convert( begin( a ) );
        final Temporal beginB = new TemporalConverter().convert( begin( b ) );
        if ( beginA == null && beginB == null ) {
            return 0;
        } else if ( beginA == null ) {
            return -1;
        } else if ( beginB == null ) {
            return 1;
        }
        return beginA.compareTo( beginB );
    }

    public static int compareEnd( final TimeGeometricPrimitive a, final TimeGeometricPrimitive b ) {
        final Temporal endA = new TemporalConverter().convert( end( a ) );
        final Temporal endB = new TemporalConverter().convert( end( b ) );
        if ( endA == null && endB == null ) {
            return 0;
        } else if ( endA == null ) {
            return 1;
        } else if ( endB == null ) {
            return -1;
        }
        final int compared = endA.compareTo( endB );
        if ( compared == 0 ) {
            if ( a instanceof TimePeriod && b instanceof TimeInstant ) {
                return -1;
            }
            if ( a instanceof TimeInstant && b instanceof TimePeriod ) {
                return 1;
            }
        }
        return compared;
    }

    public static int compareBeginWithEnd( final TimeGeometricPrimitive a, final TimeGeometricPrimitive b ) {
        final Temporal beginA = new TemporalConverter().convert( begin( a ) );
        final Temporal endB = new TemporalConverter().convert( end( b ) );
        if ( beginA == null && endB == null ) {
            return -1;
        } else if ( beginA == null ) {
            return -1;
        } else if ( endB == null ) {
            return -1;
        }
        int compared = beginA.compareTo( endB );
        if ( compared == 0 && b instanceof TimeInstant ) {
            return -1;
        }
        return compared;
    }

    public static TimePosition begin( final TimeGeometricPrimitive t ) {
        if ( t instanceof TimeInstant ) {
            return ( (TimeInstant) t ).getPosition();
        }
        return ( (TimePeriod) t ).getBeginPosition();
    }

    public static TimePosition end( final TimeGeometricPrimitive t ) {
        if ( t instanceof TimeInstant ) {
            return ( (TimeInstant) t ).getPosition();
        }
        return ( (TimePeriod) t ).getEndPosition();
    }

}
