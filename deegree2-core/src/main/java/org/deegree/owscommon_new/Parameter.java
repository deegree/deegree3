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
package org.deegree.owscommon_new;

/**
 * <code>Parameter</code> is an abstraction of <code>DomainType</code> according to the OWS
 * common specification 1.0.0. It defines some elementary methods that are self explanatory.
 *
 * @see DomainType
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public abstract class Parameter {

    private boolean optional = false;

    private boolean repeatable = false;

    private String description = null;

    private int direction = 0;

    /**
     * Standard constructor that initializes all encapsulated data.
     *
     * @param optional
     * @param repeatable
     * @param description
     * @param direction
     */
    public Parameter( boolean optional, boolean repeatable, String description, int direction ) {
        this.optional = optional;
        this.repeatable = repeatable;
        this.description = description;
        this.direction = direction;
    }

    /**
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the direction.
     */
    public int getDirection() {
        return direction;
    }

    /**
     * @return whether this parameter is optional.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @return whether this parameter is repeatable (?).
     */
    public boolean isRepeatable() {
        return repeatable;
    }

}
