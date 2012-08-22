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
package org.deegree.ogcwebservices.wfs.operation;

import org.deegree.framework.xml.XMLFragment;

/**
 * Represents the response to a {@link DescribeFeatureType} request.
 * <p>
 * In response to a {@link DescribeFeatureType} request, where the output format has been specified
 * as XMLSCHEMA. A WFS may support different formats for formatting its responses. Only GML is
 * mandatory.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureTypeDescription {

    private XMLFragment schemaDoc;

    /**
     * Creates a new instance of <code>FeatureTypeDescription</code>.
     *
     * @param schemaDoc
     */
    public FeatureTypeDescription( XMLFragment schemaDoc ) {
        this.schemaDoc = schemaDoc;
    }

    /**
     * Returns the contained schema document.
     *
     * @return the contained schema document
     */
    public XMLFragment getFeatureTypeSchema() {
        return schemaDoc;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String ret = this.getClass().getName()
            + ":\n";
        ret += "featureTypeSchema: "
            + schemaDoc + "\n";
        return ret;
    }
}
