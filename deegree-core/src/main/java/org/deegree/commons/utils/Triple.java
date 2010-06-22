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

package org.deegree.commons.utils;

/**
 * <code>Triple</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 * @param <U>
 * @param <V>
 */
public final class Triple<T, U, V> {

    /** * */
    public T first;

    /** * */
    public U second;

    /** * */
    public V third;

    /** * */
    public Triple() {
        // null values
    }

    /**
     * @param t
     * @param u
     * @param v
     */
    public Triple( T t, U u, V v ) {
        first = t;
        second = u;
        third = v;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof Triple ) {
            // what ever, unchecked.
            final Triple<?, ?, ?> that = (Triple<?, ?, ?>) other;
            return ( first == null ? that.first == null : first.equals( that.first ) )
                   && ( second == null ? that.second == null : second.equals( that.second ) )
                   && ( third == null ? that.third == null : third.equals( that.third ) );
        }
        return false;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ", " + third + ">";
    }

}
