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
 * <code>Pair</code> is a convenience class, which pairs two objects. For a pair of <code>String</code>s see
 * {@link StringPair}.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @param <T>
 *            the first Object of the pair
 * @param <U>
 *            the second Object of the pair
 *
 */
public class Pair<T, U> {
    /**
     * first value of the pair.
     */
    public T first;

    /**
     * second value of the pair.
     */
    public U second;

    /**
     * @param first
     *            value of the pair.
     * @param second
     *            value of the pair.
     */
    public Pair( T first, U second ) {
        this.first = first;
        this.second = second;
    }

    /**
     * Create a pair with null objects.
     */
    public Pair() {
        // nothing to do here
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof Pair ) {
            // what ever, unchecked.
            final Pair that = (Pair) other;
            return ( first == null ? that.first == null : first.equals( that.first ) )
                   && ( second == null ? that.second == null : second.equals( that.second ) );
        }
        return false;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }

}
