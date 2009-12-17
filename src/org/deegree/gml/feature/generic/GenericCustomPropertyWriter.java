//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.gml.feature.generic;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.feature.types.GenericCustomPropertyValue;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenericCustomPropertyWriter {

    /**
     * Exports the given {@link GenericCustomPropertyValue} to the XML stream.
     * 
     * @param value
     *            value to be exported
     * @param xmlWriter
     *            xml stream to write the XML to
     * @throws XMLStreamException
     */
    public static void export( GenericCustomPropertyValue value, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {

        // write root element
        QName elName = value.getName();
        xmlWriter.writeStartElement( elName.getPrefix(), elName.getLocalPart(), elName.getNamespaceURI() );

        // write all attributes
        Map<QName, String> attrs = value.getAttributes();
        for ( Entry<QName, String> attribute : attrs.entrySet() ) {
            QName attrName = attribute.getKey();
            if ( XMLConstants.NULL_NS_URI.equals( attrName.getNamespaceURI() != null ) ) {
                xmlWriter.writeAttribute( attrName.getNamespaceURI(), attrName.getLocalPart(), attribute.getValue() );
            } else {
                xmlWriter.writeAttribute( attrName.getLocalPart(), attribute.getValue() );
            }
        }

        // write all child nodes (text / elements) in order
        for ( Object child : value.getChildNodesAll() ) {
            if ( child instanceof String ) {
                xmlWriter.writeCharacters( (String) child );
            } else if ( child instanceof GenericCustomPropertyValue ) {
                export( (GenericCustomPropertyValue) child, xmlWriter );
            }
        }

        // close root element
        xmlWriter.writeEndElement();
    }
}
