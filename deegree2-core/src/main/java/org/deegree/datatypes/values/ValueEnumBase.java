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
package org.deegree.datatypes.values;

import java.io.Serializable;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public abstract class ValueEnumBase implements Serializable {

    private TypedLiteral[] singleValue = null;

    private Interval[] interval = null;

    /**
     * @param singleValue
     * @throws IllegalArgumentException
     */
    public ValueEnumBase( TypedLiteral[] singleValue ) throws IllegalArgumentException {
        setSingleValue( singleValue );
    }

    /**
     * @param interval
     * @throws IllegalArgumentException
     */
    public ValueEnumBase( Interval[] interval ) throws IllegalArgumentException {
        setInterval( interval );
    }

    /**
     * @param singleValue
     * @param interval
     * @throws IllegalArgumentException
     */
    public ValueEnumBase( Interval[] interval, TypedLiteral[] singleValue ) throws IllegalArgumentException {
        setSingleValue( singleValue );
        setInterval( interval );
    }

    /**
     * @return Returns the interval.
     *
     */
    public Interval[] getInterval() {
        return interval;
    }

    /**
     * @param interval
     *            The interval to set.
     * @throws IllegalArgumentException
     *             if the interval is <code>null</code> or the singleValue is <code>null</code>
     *
     */
    public void setInterval( Interval[] interval )
                            throws IllegalArgumentException {
        if ( interval == null && singleValue == null ) {
            throw new IllegalArgumentException( "at least interval or singleValue must "
                                                + "be <> null in ValueEnumBase" );
        }
        this.interval = interval;
    }

    /**
     * @return Returns the singleValue.
     *
     */
    public TypedLiteral[] getSingleValue() {
        return singleValue;
    }

    /**
     * @param singleValue
     *            The singleValue to set.
     * @throws IllegalArgumentException
     *             if the interval is <code>null</code> or the singleValue is <code>null</code>
     *
     */
    public void setSingleValue( TypedLiteral[] singleValue )
                            throws IllegalArgumentException {
        if ( interval == null && singleValue == null ) {
            throw new IllegalArgumentException( "at least interval or singleValue must "
                                                + "be <> null in ValueEnumBase" );
        }
        this.singleValue = singleValue;
    }

}
