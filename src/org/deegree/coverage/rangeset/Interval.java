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
 * 
 */
public class Interval<T extends Comparable<T>> {

    private final SingleValue<T> min;

    private final SingleValue<T> max;

    private final Closure closure;

    private final String semantic;

    private final SingleValue<T> spacing;

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
        closed,
        /** open */
        open,
        /** open-closed */
        open_closed,
        /** closed-open */
        closed_open;

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
     * @param type
     * @param semantic
     * @param atomic
     * @param spacing
     *            may be null
     */
    public Interval( SingleValue<T> min, SingleValue<T> max, Closure closure, String type, String semantic,
                     boolean atomic, SingleValue<T> spacing ) {
        this.min = min;
        this.max = max;
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
    public final SingleValue<T> getSpacing() {
        return spacing;
    }

    /**
     * @param inter
     *            to check against
     * @return true if this interval is in the bounds of the given interval.
     */
    @SuppressWarnings("unchecked")
    public boolean isInBounds( Interval<?> inter ) {
        // unchecked is suppressed because it is actually checked.
        boolean result = false;
        if ( inter != null && inter.getMin().type == min.type ) {
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
                    result = match( comp, inter.closure, true );
                }
            }
        }
        return result;
    }

    private boolean match( int comparedValue, Closure closure, boolean compareWithMin ) {
        int test = comparedValue * ( compareWithMin ? 1 : -1 );
        Closure testClosure = compareWithMin ? Closure.closed_open : Closure.open_closed;
        return ( closure == Closure.closed || closure == testClosure ) ? test > 0 : test >= 0;

    }
}
