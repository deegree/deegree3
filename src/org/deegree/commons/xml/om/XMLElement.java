//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.commons.xml.om;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.xml.xsom.XSElementDecl;

//import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;

/**
 * {@link XMLNode} that represents a generic XML element with XML schema type information.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLElement implements XMLNode {

    private QName name;

    private LinkedHashMap<QName, XMLPrimitive> attributes;

    private List<XMLNode> children;

    private XSElementDecl type;

    public XMLElement( QName name, XSElementDecl type ) {
        this.name = name;
        this.type = type;
    }

    public QName getName() {
        return name;
    }

    public LinkedHashMap<QName, XMLPrimitive> getAttributes() {
        return attributes;
    }

    public LinkedHashMap<QName, XMLPrimitive> getChildren() {
        return attributes;
    }

    public XSElementDecl getXSType() {
        return type;
    }

    public void setAttribute( QName name, XMLPrimitive value ) {
        if ( attributes == null ) {
            attributes = new LinkedHashMap<QName, XMLPrimitive>();
        }
        attributes.put( name, value );
    }

    public void addChild( XMLNode node ) {
        if ( children == null ) {
            children = new ArrayList<XMLNode>();
        }
        children.add( node );
    }
}
