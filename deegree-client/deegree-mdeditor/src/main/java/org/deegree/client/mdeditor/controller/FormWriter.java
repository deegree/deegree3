//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.controller;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
abstract class FormWriter {

    static final String FG_ELEM = "FormGroup";

    static final String DS_ELEM = "Dataset";

    static void appendFormGroup( XMLStreamWriter writer, FormGroup fg )
                            throws XMLStreamException {
        // writer.writeStartElement( FG_ELEM );
        // writer.writeAttribute( "id", fg.getId() );

        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormField ) {
                FormField ff = (FormField) fe;
                Object value = ff.getValue();
                if ( value != null ) {

                    if ( value instanceof List<?> && ( (List<?>) value ).size() > 0 ) {
                        writer.writeStartElement( "Element" );
                        writeId( writer, ff.getPath().toString() );
                        for ( Object o : (List<?>) value ) {
                            writeValue( writer, String.valueOf( o ) );
                        }
                        writer.writeEndElement();
                    } else if ( value instanceof Object[] && ( (Object[]) value ).length > 0 ) {
                        writer.writeStartElement( "Element" );
                        writeId( writer, ff.getPath().toString() );
                        Object[] array = (Object[]) value;
                        for ( int i = 0; i < array.length; i++ ) {
                            writeValue( writer, String.valueOf( array[i] ) );
                        }
                        writer.writeEndElement();
                    } else if ( ( String.valueOf( value ) ).length() > 0 ) {
                        writer.writeStartElement( "Element" );
                        writeId( writer, ff.getPath().toString() );
                        writeValue( writer, String.valueOf( value ) );
                        writer.writeEndElement();
                    }
                }
            } else if ( fe instanceof FormGroup ) {
                appendFormGroup( writer, (FormGroup) fe );
            }
        }
    }

    private static void writeValue( XMLStreamWriter writer, String value )
                            throws XMLStreamException {
        writer.writeStartElement( "value" );
        writer.writeCharacters( value );
        writer.writeEndElement();
    }

    private static void writeId( XMLStreamWriter writer, String path )
                            throws XMLStreamException {
        writer.writeStartElement( "id" );
        writer.writeCharacters( path );
        writer.writeEndElement();
    }
}
