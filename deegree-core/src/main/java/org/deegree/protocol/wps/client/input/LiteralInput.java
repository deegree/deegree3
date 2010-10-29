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
package org.deegree.protocol.wps.client.input;

import org.deegree.commons.tom.ows.CodeType;

/**
 * {@link ExecutionInput} that contains a literal value with optional data type and unit-of-measure information.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LiteralInput extends ExecutionInput {

    private final String value;

    private final String dataType;

    private final String uom;

    /**
     * Creates a new {@link LiteralInput} instance.
     * 
     * @param id
     *            input parameter identifier, must not be <code>null</code>
     * @param value
     *            literal value, must not be <code>null</code>
     * @param dataType
     *            literal data type, can be <code>null</code> (unspecified)
     * @param uom
     *            unit-of-measure, can be <code>null</code> (unspecified)
     */
    public LiteralInput( CodeType id, String value, String dataType, String uom ) {
        super( id );
        this.value = value;
        this.dataType = dataType;
        this.uom = uom;
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
     * Returns the data type.
     * 
     * @return the data type, may be <code>null</code> (unspecified)
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Returns the unit-of-measure.
     * 
     * @return the unit-of-measure, may be <code>null</code> (unspecified)
     */
    public String getUom() {
        return uom;
    }
}
