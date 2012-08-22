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

package org.deegree.ogcwebservices.csw.configuration;

/**
 * Specialized implementation of the 'Value'-element used for specifying the "outputSchema"
 * parameter of the "GetRecords"-operation.
 * <p>
 * Every value has 3 additional attributes:
 * <ul>
 * <li>deegree:input</li>
 * <li>deegree:output</li>
 * <li>deegree:schema</li>
 * </ul>
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class CatalogueOutputSchemaValue {

    private String value;

    private String inXsl;

    private String outXsl;

    /**
     * @param value
     * @param inputXsl
     * @param outputXsl
     */
    public CatalogueOutputSchemaValue( String value, String inputXsl, String outputXsl ) {
        this.value = value;
        this.inXsl = inputXsl;
        this.outXsl = outputXsl;
    }

    /**
     * @return Returns the inXsl.
     *
     */
    public String getInXsl() {
        return inXsl;
    }

    /**
     * @return Returns the value.
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * @return Returns the outXsl.
     *
     */
    public String getOutXsl() {
        return outXsl;
    }

}
