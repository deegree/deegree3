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
package org.deegree.commons.xml.schema;

import org.deegree.commons.utils.Pair;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.util.List;

import static org.deegree.commons.xml.CommonNamespaces.XSNS;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SchemaUtils {

    /**
     * Writes a wrapper schema document for the given namespace imports.
     *
     * @param writer
     *            xml stream to write to, must not be <code>null</code>
     * @param targetNamespace
     *            target namespace of the document, must not be <code>null</code>
     * @param nsImports
     *            namespace imports, must not be <code>null</code>
     * @throws XMLStreamException
     */
    public static void writeWrapperDoc( XMLStreamWriter writer, String targetNamespace,
                                        List<Pair<String, String>> nsImports )
                            throws XMLStreamException {

        writer.setDefaultNamespace( XSNS );
        writer.writeStartElement( XSNS, "schema" );
        writer.writeDefaultNamespace( XSNS );
        writer.writeAttribute( "attributeFormDefault", "unqualified" );
        writer.writeAttribute( "elementFormDefault", "qualified" );
        writer.writeAttribute( "targetNamespace", targetNamespace );

        for ( Pair<String, String> nsImport : nsImports ) {
            if ( nsImport.first.equals( targetNamespace ) ) {
                writer.writeEmptyElement( "include" );
            } else {
                writer.writeEmptyElement( "import" );
                writer.writeAttribute( "namespace", nsImport.first );
            }
            writer.writeAttribute( "schemaLocation", nsImport.second );
        }

        // end 'xs:schema'
        writer.writeEndElement();
    }

    public static void copy( XMLStreamReader reader, XMLStreamWriter writer )
                    throws XMLStreamException {
        while ( reader.hasNext() ) {
            write( reader, writer );
            reader.next();
        }
        write( reader, writer ); // write the last element
        writer.flush();
    }

    public static void write( XMLStreamReader reader, XMLStreamWriter writer )
                    throws XMLStreamException {
        switch ( reader.getEventType() ) {
        case XMLEvent.START_ELEMENT:
            writeStartElement( reader, writer );
            break;
        case XMLEvent.END_ELEMENT:
            writer.writeEndElement();
            break;
        case XMLEvent.SPACE:
        case XMLEvent.CHARACTERS:
            writer.writeCharacters( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength() );
            break;
        case XMLEvent.PROCESSING_INSTRUCTION:
            writer.writeProcessingInstruction( reader.getPITarget(), reader.getPIData() );
            break;
        case XMLEvent.CDATA:
            writer.writeCData( reader.getText() );
            break;

        case XMLEvent.COMMENT:
            writer.writeComment( reader.getText() );
            break;
        case XMLEvent.ENTITY_REFERENCE:
            writer.writeEntityRef( reader.getLocalName() );
            break;
        case XMLEvent.START_DOCUMENT:
            writeStartDocument( reader, writer );
            break;
        case XMLEvent.END_DOCUMENT:
            writer.writeEndDocument();
            break;
        case XMLEvent.DTD:
            writer.writeDTD( reader.getText() );
            break;
        }
    }

    private static void writeStartDocument( XMLStreamReader reader, XMLStreamWriter writer )
                    throws XMLStreamException {
        String encoding = reader.getCharacterEncodingScheme();
        String version = reader.getVersion();

        if ( encoding != null && version != null )
            writer.writeStartDocument( encoding, version );
        else if ( version != null )
            writer.writeStartDocument( reader.getVersion() );
    }

    private static void writeStartElement( XMLStreamReader reader, XMLStreamWriter writer )
                    throws XMLStreamException {
        String localName = reader.getLocalName();
        String namespaceURI = reader.getNamespaceURI();
        if ( namespaceURI != null && namespaceURI.length() > 0 ) {
            String prefix = reader.getPrefix();
            if ( prefix != null )
                writer.writeStartElement( prefix, localName, namespaceURI );
            else
                writer.writeStartElement( namespaceURI, localName );
        } else {
            writer.writeStartElement( localName );
        }

        for ( int i = 0, len = reader.getNamespaceCount(); i < len; i++ ) {
            writer.writeNamespace( reader.getNamespacePrefix( i ), reader.getNamespaceURI( i ) );
        }

        for ( int i = 0, len = reader.getAttributeCount(); i < len; i++ ) {
            String attUri = reader.getAttributeNamespace( i );
            if ( attUri != null && !attUri.isEmpty() )
                writer.writeAttribute( attUri, reader.getAttributeLocalName( i ), reader.getAttributeValue( i ) );
            else
                writer.writeAttribute( reader.getAttributeLocalName( i ), reader.getAttributeValue( i ) );
        }
    }
}