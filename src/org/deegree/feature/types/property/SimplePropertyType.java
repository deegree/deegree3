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
package org.deegree.feature.types.property;

import javax.xml.namespace.QName;

/**
 * A {@link PropertyType} that defines a property with a primitive value, e.g. a string or a number.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class SimplePropertyType extends AbstractPropertyType {

    /**
     * Known primitive types.
     * <p>
     * TODO add missing types
     * </p>
     *
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     *
     * @version $Revision: $, $Date: $
     */
    public enum PrimitiveType {
        STRING,
        BOOLEAN,
        NUMBER,
        DATE,
        DATE_TIME,
        TIME
    }

    private PrimitiveType primitiveType;

    public SimplePropertyType( QName propName, int minOccurs, int maxOccurs, PrimitiveType type ) {
        super( propName, minOccurs, maxOccurs );
        this.primitiveType = type;
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    @Override
    public String toString() {
        String s = "- simple property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
                   + ", type: " + primitiveType;
        return s;
    }
}
