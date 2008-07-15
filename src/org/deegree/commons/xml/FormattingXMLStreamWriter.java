//$HeadURL:$
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
 53177 Bonn
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
package org.deegree.commons.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * 
 * This class is a pretty print wrapper for XMLStreamWriter.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class FormattingXMLStreamWriter implements XMLStreamWriter {

    private final String indent;

    private final XMLStreamWriter s;

    private int level = 0;

    // if the last write element call was a start, print end element on the same line
    private boolean lastWasStart = false;

    /**
     * Create a new wrapper for XMLStreamWriter that formats the xml output.
     * 
     * @param xmlStreamWriter
     */
    public FormattingXMLStreamWriter( XMLStreamWriter xmlStreamWriter ) {
        this.s = xmlStreamWriter;
        this.indent = "  ";
    }

    /**
     * Create a new wrapper for XMLStreamWriter that formats the xml output.
     * 
     * @param xmlStreamWriter
     * @param indent
     *            the indent string for each indent level
     */
    public FormattingXMLStreamWriter( XMLStreamWriter xmlStreamWriter, String indent ) {
        this.s = xmlStreamWriter;
        this.indent = indent;
    }
    
    @Override
    public void close()
                            throws XMLStreamException {
        s.close();
    }

    @Override
    public void flush()
                            throws XMLStreamException {
        s.flush();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return s.getNamespaceContext();
    }

    @Override
    public String getPrefix( String uri )
                            throws XMLStreamException {
        return s.getPrefix( uri );
    }

    @Override
    public Object getProperty( String name )
                            throws IllegalArgumentException {
        return s.getProperty( name );
    }

    @Override
    public void setDefaultNamespace( String uri )
                            throws XMLStreamException {
        s.setDefaultNamespace( uri );
    }

    @Override
    public void setNamespaceContext( NamespaceContext context )
                            throws XMLStreamException {
        s.setNamespaceContext( context );
    }

    @Override
    public void setPrefix( String prefix, String uri )
                            throws XMLStreamException {
        s.setPrefix( prefix, uri );
    }

    @Override
    public void writeAttribute( String localName, String value )
                            throws XMLStreamException {
        s.writeAttribute( localName, value );
    }

    @Override
    public void writeAttribute( String namespaceURI, String localName, String value )
                            throws XMLStreamException {
        s.writeAttribute( namespaceURI, localName, value );
    }

    @Override
    public void writeAttribute( String prefix, String namespaceURI, String localName, String value )
                            throws XMLStreamException {
        s.writeAttribute( prefix, namespaceURI, localName, value );
    }

    @Override
    public void writeCData( String data )
                            throws XMLStreamException {
        s.writeCData( data );
    }

    @Override
    public void writeCharacters( String text )
                            throws XMLStreamException {
        s.writeCharacters( text );
    }

    @Override
    public void writeCharacters( char[] text, int start, int len )
                            throws XMLStreamException {
        s.writeCharacters( text, start, len );
    }

    @Override
    public void writeComment( String data )
                            throws XMLStreamException {
        s.writeComment( data );
    }

    @Override
    public void writeDTD( String dtd )
                            throws XMLStreamException {
        s.writeDTD( dtd );
    }

    @Override
    public void writeDefaultNamespace( String namespaceURI )
                            throws XMLStreamException {
        s.writeDefaultNamespace( namespaceURI );
    }

    @Override
    public void writeEmptyElement( String localName )
                            throws XMLStreamException {
        s.writeEmptyElement( localName );
    }

    @Override
    public void writeEmptyElement( String namespaceURI, String localName )
                            throws XMLStreamException {
        s.writeEmptyElement( namespaceURI, localName );
    }

    @Override
    public void writeEmptyElement( String prefix, String localName, String namespaceURI )
                            throws XMLStreamException {
        s.writeEmptyElement( prefix, localName, namespaceURI );
    }

    @Override
    public void writeEntityRef( String name )
                            throws XMLStreamException {
        s.writeEntityRef( name );
    }

    @Override
    public void writeNamespace( String prefix, String namespaceURI )
                            throws XMLStreamException {
        s.writeNamespace( prefix, namespaceURI );
    }

    @Override
    public void writeProcessingInstruction( String target )
                            throws XMLStreamException {
        s.writeProcessingInstruction( target );
    }

    @Override
    public void writeProcessingInstruction( String target, String data )
                            throws XMLStreamException {
        s.writeProcessingInstruction( target, data );
    }

    @Override
    public void writeStartDocument()
                            throws XMLStreamException {
        s.writeStartDocument();
        s.writeCharacters( "\n" );
    }

    @Override
    public void writeStartDocument( String version )
                            throws XMLStreamException {
        s.writeStartDocument( version );
        s.writeCharacters( "\n" );
    }

    @Override
    public void writeStartDocument( String encoding, String version )
                            throws XMLStreamException {
        s.writeStartDocument( encoding, version );
        s.writeCharacters( "\n" );
    }

    @Override
    public void writeStartElement( String localName )
                            throws XMLStreamException {
        indent();
        s.writeStartElement( localName );
    }

    @Override
    public void writeStartElement( String namespaceURI, String localName )
                            throws XMLStreamException {
        indent();
        s.writeStartElement( namespaceURI, localName );
    }

    @Override
    public void writeStartElement( String prefix, String localName, String namespaceURI )
                            throws XMLStreamException {
        indent();
        s.writeStartElement( prefix, localName, namespaceURI );
    }

    @Override
    public void writeEndDocument()
                            throws XMLStreamException {
        s.writeEndDocument();
        s.writeCharacters( "\n" );
    }

    @Override
    public void writeEndElement()
                            throws XMLStreamException {
        unindent();
        s.writeEndElement();
    }

    private final void unindent()
                            throws XMLStreamException {
        level -= 1;
        if ( !lastWasStart ) {
            writeIndent( level );
        }
        if ( level == 0 ) {
            s.writeCharacters( "\n" );
        }
        lastWasStart = false;
    }

    private final void indent()
                            throws XMLStreamException {
        lastWasStart = true;
        writeIndent( level );
        level += 1;
    }

    private final void writeIndent( int level )
                            throws XMLStreamException {
        if ( level > 0 ) {
            StringBuilder b = new StringBuilder( level + 1 );
            b.append( '\n' );
            for ( int i = 0; i < level; i++ ) {
                b.append( indent );
            }
            s.writeCharacters( b.toString() );    
        }
    }

}
