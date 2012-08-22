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

import org.deegree.owscommon.OWSDomainType;
import org.deegree.owscommon.OWSMetadata;

/**
 * Specialized implementation of the <code>OWSDomainType</code> used for specifying the "typeName"
 * parameter of the "GetRecords"-operation.
 * <p>
 * Every value has 1 additional attribute:
 * <ul>
 * <li>deegree:schema</li>
 * </ul>
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CatalogueTypeNameSchemaParameter extends OWSDomainType {

    private CatalogueTypeNameSchemaValue[] values;

    /**
     *
     * @param name
     * @param values
     * @param metadata
     */
    public CatalogueTypeNameSchemaParameter( String name, CatalogueTypeNameSchemaValue[] values, OWSMetadata[] metadata ) {
        super( name, metadata );
        this.values = values;
    }

    /**
     * returns the
     *
     * @return the values
     */
    public CatalogueTypeNameSchemaValue[] getSpecializedValues() {
        return values;
    }

}
