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
package org.deegree.model.generic.xsd;

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
