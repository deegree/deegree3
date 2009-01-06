//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.model.generic.schema.ObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DeegreeObjectXMLAdapter extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( DeegreeObjectXMLAdapter.class );

    private ApplicationSchema schema;

    /**
     * Constructs a new <code>DeegreeObjectXMLAdapter</code> for the object types that are defined in the given
     * application schema.
     * 
     * @param schema
     *            application schema that provides the binding between element names and object types
     */
    public DeegreeObjectXMLAdapter( ApplicationSchema schema ) {
        this.schema = schema;
    }

    public DeegreeObject parse() {

        LOG.info( "Parsing root element '" + rootElement.getQName() + "' of application document." );
        ObjectType ot = schema.getObjectType( rootElement.getQName() );

        return null;
    }

    public static void export( XMLStreamWriter xmlWriter, DeegreeObject o )
                            throws XMLStreamException {

        QName elementName = o.getName();
        xmlWriter.writeStartElement( elementName.getNamespaceURI(), elementName.getLocalPart() );

        // attributes
        for ( Attribute attr : o.getAttributes() ) {
            QName attrName = attr.getName();
            xmlWriter.writeAttribute( attrName.getNamespaceURI(), attrName.getLocalPart(), attr.getValue() );
        }

        // contents (DeegreeObject or Text instances)
        for (Node content : o.getContents()) {
            if (content instanceof Text) {
                xmlWriter.writeCharacters( ((Text) content).getValue());
            } else if (content instanceof DeegreeObject) {
                export( xmlWriter, o );
            } else {
                throw new XMLStreamException ("Unexpected node type: " + content.getClass().getName());
            }
        }
        
        xmlWriter.writeEndElement();
    }
}
