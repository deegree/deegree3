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
package org.deegree.protocol.wps.client.process.execute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.client.param.ComplexFormat;

/**
 * Encapsulates the requested settings for an output parameter.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OutputFormat {

    private CodeType id;

    private String uom;

    private ComplexFormat complexAttributes;

    private boolean asRef;

    /**
     * Creates a new {@link OutputFormat} instance.
     * 
     * @param id
     *            parameter identifier, must not be <code>null</code>
     * @param uom
     *            requested unit-of-measure, can be <code>null</code>
     * @param asRef
     *            true, if the output should be returned as reference, false otherwise
     * @param mimeType
     *            requested mime type for the output, can be <code>null</code>
     * @param encoding
     *            requested encoding for the output, can be <code>null</code>
     * @param schema
     *            requested XML schema for the output, can be <code>null</code>
     */
    public OutputFormat( CodeType id, String uom, boolean asRef, String mimeType, String encoding, String schema ) {
        this.id = id;
        this.uom = uom;
        this.asRef = asRef;
        this.complexAttributes = new ComplexFormat( mimeType, encoding, schema );
    }

    /**
     * Returns the parameter identifier.
     * 
     * @return parameter identifier, never <code>null</code>
     */
    public CodeType getId() {
        return id;
    }

    /**
     * Returns the requested unit-of-measure (only applies to bounding box outputs).
     * 
     * @return requested unit-of-measure, can be <code>null</code>
     */
    public String getUom() {
        return uom;
    }

    /**
     * Returns the requested complex format (only applies to complex outputs).
     * 
     * @return requested complex format, can be <code>null</code>
     */
    public ComplexFormat getComplexAttributes() {
        return complexAttributes;
    }

    /**
     * Returns whether the output should be returned as reference.
     * 
     * @return true, if the output should be returned as reference, false otherwise
     */
    public boolean isReference() {
        return asRef;
    }
}
