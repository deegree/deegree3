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
package org.deegree.feature.property;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.types.property.SimplePropertyType;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SimpleProperty implements Property {

    private SimplePropertyType pt;

    private PrimitiveValue value;

    public SimpleProperty( SimplePropertyType pt, String value, PrimitiveType type ) {
        this.pt = pt;
        this.value = new PrimitiveValue( value, type );
    }

    public SimpleProperty( SimplePropertyType pt, String value, XSSimpleTypeDefinition xsdType ) {
        this.pt = pt;
        this.value = new PrimitiveValue( value, xsdType );
    }

    @Override
    public QName getName() {
        return pt.getName();
    }

    @Override
    public boolean isNilled() {
        return value == null;
    }

    @Override
    public PrimitiveValue getValue() {
        return value;
    }

    @Override
    public void setValue( TypedObjectNode value ) {
        this.value = (PrimitiveValue) value;
    }

    @Override
    public SimplePropertyType getType() {
        return pt;
    }

    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }
}
