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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSTypeDefinition;

/**
 * {@link ObjectNode} that represents a generic XML element with associated XML schema type information.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenericXMLElement extends GenericXMLElementContent {

    private QName name;

    private Map<QName, PrimitiveValue> attrs;

    private List<ObjectNode> children;

    private XSTypeDefinition type;

    public GenericXMLElement( QName name, XSTypeDefinition type, Map<QName, PrimitiveValue> attrs, List<ObjectNode> children ) {
        super (type, attrs, children);
        this.name = name;
    }

    public QName getName() {
        return name;
    }

    @Override
    public String toString() {
        String s = name + "{";
        s += "type=" + type;
        if ( attrs != null ) {
            s += ",attributes={";
            for ( Entry<QName, PrimitiveValue> attr : attrs.entrySet() ) {
                s += attr.getKey() + "=" + attr.getValue();
            }
        }
        if ( children != null ) {
            s += "},children={";
            for ( ObjectNode child : children ) {
                s += child;
            }
        }
        s += "}}";
        return s;
    }
}
