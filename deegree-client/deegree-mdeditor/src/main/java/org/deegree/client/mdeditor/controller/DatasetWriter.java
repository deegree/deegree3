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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.client.mdeditor.config.Configuration;
import org.deegree.client.mdeditor.model.FormField;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DatasetWriter {

    private static final Logger LOG = getLogger( DatasetWriter.class );

    public static void writeElements( Map<String, FormField> elements ) {
        LOG.debug( "Start writing the " + elements.size() + " values." );

        try {
            // TODO
            String title = "title";
            String file = Configuration.getFilesDirURL() + title + ".xml";
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            FileOutputStream fos = new FileOutputStream( file );
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( fos );

            writer.writeStartDocument();
            writer.writeStartElement( "Dataset" );

            for ( String id : elements.keySet() ) {
                Object value = elements.get( id ).getValue();
                if ( value != null ) {
                    writer.writeStartElement( "Element" );
                    writer.writeStartElement( "id" );
                    writer.writeCharacters( id );
                    writer.writeEndElement();
                    if ( value instanceof List<?> ) {
                        for ( Object o : (List<?>) value ) {
                            writeValue( writer, String.valueOf( o ) );
                        }
                    } else if ( value instanceof Object[] ) {
                        Object[] array = (Object[]) value;
                        for ( int i = 0; i < array.length; i++ ) {
                            writeValue( writer, String.valueOf( array[i] ) );
                        }
                    } else {
                        writeValue( writer, String.valueOf( value ) );
                    }
                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            writer.close();
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void writeValue( XMLStreamWriter writer, String value )
                            throws XMLStreamException {
        writer.writeStartElement( "value" );
        writer.writeCharacters( value );
        writer.writeEndElement();
    }
}
