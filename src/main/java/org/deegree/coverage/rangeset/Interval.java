//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wcs/model/Interval.java $
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
 * The <code>Interval</code> an intervall.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: 19041 $, $Date: 2009-08-11 17:04:57 +0200 (Di, 11 Aug 2009) $
 * @param <T>
 *            of the values in this interval
 * @param <R>
 *            type of the Resolution
 * 
 */
public class Interval<T extends Comparable<T>, R extends Comparable<R>> {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( Interval.class );

    private final SingleValue<T> min;

    private final SingleValue<T> max;

    private final Closure closure;

    private final String semantic;

    private final SingleValue<R> spacing;

    // only boundaries or internal values as well?
    private final boolean atomic;

    /**
     * 
     * The <code>Closure</code> of an interval
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: 19041 $, $Date: 2009-08-11 17:04:57 +0200 (Di, 11 Aug 2009) $
     * 
     */
    public enum Closure {
        /** closed */
        closed( "(", ")" ),
        /** open */
        open( "[", "]" ),
        /** open-closed */
        open_closed( "[", ")" ),
        /** closed-open */
        closed_open( "(", "]" );

        /** simple boundary representation of the beginning of an interval */
        public String begin;

        /** simple boundary representation of the end of an interval */
        public String end;

        private Closure( String begin, String end ) {
            this.begin = begin;
            this.end = end;
        }

        /**
         * @param closureValue
         *            to be mapped, '-' will be replaced with '_'
         * @return the closure value or closed.
         */
        public static Closure fromString( String closureValue ) {
            Closure result = Closure.closed;
            if ( closureValue != null && !"".equals( closureValue ) ) {
                String mapped = closureValue.replaceAll( "-", "_" );
                try {
                    result = Closure.valueOf( mapped.toLowerCase() );
                } catch ( NullPointerException e ) {
                    // nothing, just use closed as a default.
                }
            }
            return result;
        }

    }

    /**
     * @param min
     * @param max
     * @param closure
     * @param semantic
     * @param atomic
     * @param spacing
     *            may be null
     * @throws IllegalArgumentException
     *             if the types of the min and max are not equal.
     */
    public Interval( SingleValue<T> min, SingleValue<T> max, Closure closure, String semantic, boolean atomic,
                     SingleValue<R> spacing ) throws IllegalArgumentException {
        if ( ( min == null || max == null ) || ( min.type != max.type ) ) {
            throw new IllegalArgumentException( "The types of min, max and interal differ, this may not be." );
        }

        if ( min.value.compareTo( max.value ) >= 0 ) {
            LOG.warn( "Min must be smaller than max, the values of the interval are not correct, swapping them." );
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
        this.closure = closure;
        this.semantic = semantic;
        this.atomic = atomic;
        this.spacing = spacing;

    }

    /**
     * @return the min
     */
    public final SingleValue<T> getMin() {
        return min;
    }

    /**
     * @return the max
     */
    public final SingleValue<T> getMax() {
        return max;
    }

    /**
     * @return the closure
     */
    public final Closure getClosure() {
        return closure;
    }

    /**
     * @return the semantic
     */
    public final String getSemantic() {
        return semantic;
    }

    /**
     * @return the atomic is true if only interval end are supported.
     */
    public final boolean isAtomic() {
        return atomic;
    }

    /**
     * @return the spacing
     */
    public final SingleValue<R> getSpacing() {
        return spacing;
    }

    /**
     * @param inter
     *            to check against
     * @return true if this interval is in the bounds of the given interval.
     */
    @SuppressWarnings("unchecked")
    public boolean isInBounds( Interval<?, ?> inter ) {
        // unchecked is suppressed because it is actually checked.
        boolean result = false;
        if ( inter != null ) {
            if ( inter.min.type.isCompatible( min.type ) ) {
                int comp = min.value.compareTo( (T) inter.min.value );
                // min fit true if this min value > given min value
                result = match( comp, inter.closure, true );
                if ( result ) {
                    // min is in the interval if the this min value < max value
                    comp = min.value.compareTo( (T) inter.max.value );
                    result = match( comp, inter.closure, false );
                    if ( result ) {
                        // so min values match, lets check the max value against max (assuming min > max )
                        comp = max.value.compareTo( (T) inter.max.value );
                        result = match( comp, inter.closure, false );
                    }
                }
            }
        }
        return result;
    }

    private boolean match( int comparedValue, Closure closure, boolean compareWithMin ) {
        int test = comparedValue * ( compareWithMin ? 1 : -1 );

        boolean closed = ( closure == Closure.closed )
                         || ( compareWithMin ? closure == Closure.closed_open : closure == Closure.open_closed );

        return ( closed ) ? test > 0 : test >= 0;
    }

    /**
     * 
     * @param <RS>
     *            type of the resolution.
     * @param type
     *            of the Interval
     * @param min
     *            will be the min value, not <code>null</code>.
     * @param max
     *            will be the max value, not <code>null</code>..
     * @param closure
     *            of the interval, if <code>null</code> defaults to {@link Closure#closed}
     * @param semantic
     *            describing the interval, may be <code>null</code>
     * @param atomic
     * @param resolution
     *            of the steps, may be <code>null</code>.
     * @return the Interval instantiated with the given min, max and optional resolution
     */
    public static <RS extends Comparable<RS>> Interval<?, RS> createFromStrings( String type, String min, String max,
                                                                                 Closure closure, String semantic,
                                                                                 boolean atomic,
                                                                                 SingleValue<RS> resolution ) {
        ValueType determined = ValueType.fromString( type );
        Interval<?, RS> result = null;
        switch ( determined ) {
        case Byte:
        case Short:
            short smin = Short.valueOf( min );
            short smax = Short.valueOf( max );
            result = new Interval<Short, RS>( new SingleValue<Short>( determined, smin ),
                                              new SingleValue<Short>( determined, smax ), closure, semantic, atomic,
                                              resolution );
            break;
        case Integer:
            int imin = Integer.valueOf( min );
            int imax = Integer.valueOf( max );
            result = new Interval<Integer, RS>( new SingleValue<Integer>( determined, imin ),
                                                new SingleValue<Integer>( determined, imax ), closure, semantic,
                                                atomic, resolution );
            break;
        case Long:
            long lmin = Long.valueOf( min );
            long lmax = Long.valueOf( max );
            result = new Interval<Long, RS>( new SingleValue<Long>( determined, lmin ),
                                             new SingleValue<Long>( determined, lmax ), closure, semantic, atomic,
                                             resolution );
            break;
        case Double:
            Double dmin = Double.valueOf( min );
            Double dmax = Double.valueOf( max );
            result = new Interval<Double, RS>( new SingleValue<Double>( determined, dmin ),
                                               new SingleValue<Double>( determined, dmax ), closure, semantic, atomic,
                                               resolution );
            break;
        case Float:
            Float fmin = Float.valueOf( min );
            Float fmax = Float.valueOf( max );
            result = new Interval<Float, RS>( new SingleValue<Float>( determined, fmin ),
                                              new SingleValue<Float>( determined, fmax ), closure, semantic, atomic,
                                              resolution );
            break;
        default:
            result = new Interval<String, RS>( new SingleValue<String>( determined, min ),
                                               new SingleValue<String>( determined, max ), closure, semantic, atomic,
                                               resolution );
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "{" ).append( min.type.toString() ).append( "}" );
        sb.append( closure.begin ).append( min.value.toString() ).append( "/" ).append( max.value.toString() ).append(
                                                                                                                       closure.end );
        if ( spacing != null ) {
            sb.append( "/" ).append( spacing.toString() );
        }
        return sb.toString();
    }

    /**
     * @param value
     * @return true if the given value lies within the bounds of this interval.
     */
    public boolean liesWithin( T value ) {
        return match( -1 * ( min.value.compareTo( value ) ), closure, true )
               && match( -1 * ( max.value.compareTo( value ) ), closure, false );
    }
}
