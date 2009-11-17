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

import java.util.List;

import javax.xml.namespace.QName;

/**
 * {@link PropertyType} that defines a property with a user-defined value type.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class CustomPropertyType extends AbstractPropertyType {

    private QName xsdType;

    public CustomPropertyType( QName name, int minOccurs, int maxOccurs, QName xsdType, boolean isAbstract,
                               List<PropertyType<?>> substitutions ) {
        super( name, minOccurs, maxOccurs, isAbstract, substitutions );
        this.xsdType = xsdType;
    }

    public QName getXSDValueType() {
        return xsdType;
    }

    @Override
    public String toString() {
        String s = "- custom property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
                   + ", content xsd type: " + xsdType;
        return s;
    }
}
