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
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class Closure implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Comment for <code>CLOSED</code>
     */
    public static final String CLOSED = "closed";

    /**
     * Comment for <code>OPENED</code>
     */
    public static final String OPENED = "open";

    /**
     * Comment for <code>OPENED-CLOSED</code>
     */
    public static final String OPENED_CLOSED = "open-closed";

    /**
     * Comment for <code>CLOSED-OPENED</code>
     */
    public static final String CLOSED_OPENED = "closed-open";

    /**
     * Comment for <code>value</code>
     */
    public String value = CLOSED;

    /**
     * default = CLOSED
     */
    public Closure() {
        //default = CLOSED
    }

    /**
     * @param value
     */
    public Closure( String value ) {
        this.value = value;
    }

    /**
     * Compares the specified object with this enum for equality.
     */
    @Override
    public boolean equals( Object object ) {
        if ( object != null && getClass().equals( object.getClass() ) ) {
            return ( (Closure) object ).value.equals( value );
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final long longCode = value.hashCode();
        return ( ( (int) ( longCode >>> 32 ) ) ^ (int) longCode ) + 37 * super.hashCode();
    }

}
