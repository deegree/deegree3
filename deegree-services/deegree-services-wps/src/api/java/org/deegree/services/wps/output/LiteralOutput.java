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

package org.deegree.services.wps.output;

import org.deegree.services.wps.Processlet;

/**
 * Identifies this {@link ProcessletOutput} parameter to be literal data of a simple quantity (e.g., one number), and
 * provides a method for setting it.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 *
 */
public interface LiteralOutput extends ProcessletOutput {

    /**
     * Sets the value for this output parameter of the {@link Processlet} execution.
     *
     * @see #getRequestedUOM()
     * @param value
     *            value to be set (in the requested UOM)
     */
    public void setValue( String value );

    /**
     * Returns the requested UOM (unit-of-measure) for the literal value, it is guaranteed that this UOM is supported
     * for this parameter (according to the process description).
     *
     * @return the requested UOM (unit-of-measure) for the literal value, may be null
     */
    public String getRequestedUOM();

    /**
     * Returns the announced literal data type from the process definition (e.g. integer, real, etc) as an URI, such as
     * <code>http://www.w3.org/TR/xmlschema-2/#integer</code>.
     *
     * @return the data type, or null if not specified in the process definition
     */
    public String getDataType();
}
