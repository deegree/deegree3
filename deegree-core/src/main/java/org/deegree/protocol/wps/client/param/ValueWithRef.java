//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wps.client.param;

import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.type.LiteralOutputType;

/**
 * Encapsulates a value and an optional reference.
 * 
 * @see LiteralInputType
 * @see LiteralOutputType
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ValueWithRef {

    private String value;

    private String ref;

    /**
     * Creates a new {@link ValueWithRef} instance.
     * 
     * @param value
     *            value, must not be <code>null</code>
     * @param ref
     *            reference, can be <code>null</code>
     */
    public ValueWithRef( String value, String ref ) {
        this.value = value;
        this.ref = ref;
    }

    /**
     * Returns the value.
     * 
     * @return the value, never <code>null</code>
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the reference.
     * 
     * @return the reference, may be <code>null</code>
     */
    public String getRef() {
        return ref;
    }
}
