//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.feature.types;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.dv.XSSimpleType;

public class SimplePropertyType implements PropertyType {

    private QName name;

    private int maxOccurs;

    private int minOccurs;

    private QName xsdType;

    private XSSimpleType typeDef;

    public SimplePropertyType( QName name, int minOccurs, int maxOccurs, XSSimpleType typeDef ) {
        this.name = name;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.typeDef = typeDef;
    }

    public SimplePropertyType( QName name, int minOccurs, int maxOccurs, QName xsdType ) {
        this.name = name;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.xsdType = xsdType;
    }

    @Override
    public QName getName() {
        return name;
    }

    @Override
    public int getMaxOccurs() {
        return maxOccurs;
    }

    @Override
    public int getMinOccurs() {
        return minOccurs;
    }

    @Override
    public QName getXSDValueType() {
        return xsdType;
    }

    @Override
    public String toString() {
        String s = "- property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
                   + ", xsdType: ";
        if (xsdType != null) {
            s += xsdType;
        } else {
            s += typeDef;
        }
        return s;
    }
}
