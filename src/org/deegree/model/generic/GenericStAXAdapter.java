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
package org.deegree.model.generic;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class GenericStAXAdapter {

    public static void append( XMLStreamWriter writer, StructuredObject structuredObject )
                            throws XMLStreamException {

        QName elementName = structuredObject.getName();
        writer.writeStartElement( elementName.getNamespaceURI(), elementName.getLocalPart() );

        // write attributes
        for ( AttributeNode attribute : structuredObject.getAttributes() ) {
            QName attributeName = attribute.getName();
            writer.writeAttribute( attributeName.getNamespaceURI(), attributeName.getLocalPart(), attribute.getValue() );
        }
        
        // write contents
        for ( ObjectNode contentNode : structuredObject.getContents() ) {
            if (contentNode instanceof StructuredObject) {
                append (writer, (StructuredObject) contentNode);
            } else {
                // must be ValueNode (TODO find a save way to do this)
                writer.writeCharacters( ((ValueNode) contentNode).getValue());               
            }
        }
        
        writer.writeEndElement();
    }
}
