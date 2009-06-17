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
package org.deegree.feature.gml.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Represents either a <code>xs:simpleType</code> definition or a <code>xs:complexType</code> definition.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
abstract class TypeDefinition {

    // null means: anonymous type
    protected QName name;

    // can be null (if it is the root of a type hierarchy)
    protected TypeDefinition baseType;

    protected List<TypeDefinition> directDerivations = new ArrayList<TypeDefinition>();

    protected TypeDefinition( QName name, TypeDefinition baseType ) {
        this.name = name;
        this.baseType = baseType;
    }

    QName getName () {
        return name;
    }

    boolean isAnonymous() {
        return name == null;
    }

    TypeDefinition getBaseType() {
        return baseType;
    }

    void addDerivedType( TypeDefinition type ) {
        directDerivations.add( type );
    }

    List<TypeDefinition> getDirectDerivations() {
        return directDerivations;
    }

    @Override
    public String toString () {
        return toString ("");
    }

    String toString (String indent) {
        String s = indent + "- type: " + name + "\n";
        for ( TypeDefinition childType : directDerivations ) {
            s += childType.toString( "  " + indent );
        }
        return s;
    }
}
