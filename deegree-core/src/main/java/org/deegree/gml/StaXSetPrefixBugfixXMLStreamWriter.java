//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/branches/3.0/deegree-core/src/main/java/org/deegree/gml/GMLOutputFactory.java $
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
package org.deegree.gml;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class StaXSetPrefixBugfixXMLStreamWriter implements XMLStreamWriter {

    private final XMLStreamWriter xmlWriter;

    private final Set<String> prefixes = new HashSet<String>();

    public StaXSetPrefixBugfixXMLStreamWriter( XMLStreamWriter xmlWriter ) {
        this.xmlWriter = xmlWriter;
    }

    public void writeStartElement( String localName )
                            throws XMLStreamException {
        xmlWriter.writeStartElement( localName );
        prefixes.clear();
    }

    public void writeStartElement( String namespaceURI, String localName )
                            throws XMLStreamException {
        xmlWriter.writeStartElement( namespaceURI, localName );
        prefixes.clear();
    }

    public void writeStartElement( String prefix, String localName, String namespaceURI )
                            throws XMLStreamException {
        xmlWriter.writeStartElement( prefix, localName, namespaceURI );
        prefixes.clear();
    }

    public void writeEmptyElement( String namespaceURI, String localName )
                            throws XMLStreamException {
        xmlWriter.writeEmptyElement( namespaceURI, localName );
        prefixes.clear();
    }

    public void writeEmptyElement( String prefix, String localName, String namespaceURI )
                            throws XMLStreamException {
        xmlWriter.writeEmptyElement( prefix, localName, namespaceURI );
        prefixes.clear();
    }

    public void writeEmptyElement( String localName )
                            throws XMLStreamException {
        xmlWriter.writeEmptyElement( localName );
        prefixes.clear();
    }

    public void writeEndElement()
                            throws XMLStreamException {
        xmlWriter.writeEndElement();
    }

    public void writeEndDocument()
                            throws XMLStreamException {
        xmlWriter.writeEndDocument();
    }

    public void close()
                            throws XMLStreamException {
        xmlWriter.close();
    }

    public void flush()
                            throws XMLStreamException {
        xmlWriter.flush();
    }

    public void writeAttribute( String localName, String value )
                            throws XMLStreamException {
        xmlWriter.writeAttribute( localName, value );
    }

    public void writeAttribute( String prefix, String namespaceURI, String localName, String value )
                            throws XMLStreamException {
        xmlWriter.writeAttribute( prefix, namespaceURI, localName, value );
    }

    public void writeAttribute( String namespaceURI, String localName, String value )
                            throws XMLStreamException {
        xmlWriter.writeAttribute( namespaceURI, localName, value );
    }

    public void writeNamespace( String prefix, String namespaceURI )
                            throws XMLStreamException {
        xmlWriter.writeNamespace( prefix, namespaceURI );
    }

    public void writeDefaultNamespace( String namespaceURI )
                            throws XMLStreamException {
        xmlWriter.writeDefaultNamespace( namespaceURI );
    }

    public void writeComment( String data )
                            throws XMLStreamException {
        xmlWriter.writeComment( data );
    }

    public void writeProcessingInstruction( String target )
                            throws XMLStreamException {
        xmlWriter.writeProcessingInstruction( target );
    }

    public void writeProcessingInstruction( String target, String data )
                            throws XMLStreamException {
        xmlWriter.writeProcessingInstruction( target, data );
    }

    public void writeCData( String data )
                            throws XMLStreamException {
        xmlWriter.writeCData( data );
    }

    public void writeDTD( String dtd )
                            throws XMLStreamException {
        xmlWriter.writeDTD( dtd );
    }

    public void writeEntityRef( String name )
                            throws XMLStreamException {
        xmlWriter.writeEntityRef( name );
    }

    public void writeStartDocument()
                            throws XMLStreamException {
        xmlWriter.writeStartDocument();
    }

    public void writeStartDocument( String version )
                            throws XMLStreamException {
        xmlWriter.writeStartDocument( version );
    }

    public void writeStartDocument( String encoding, String version )
                            throws XMLStreamException {
        xmlWriter.writeStartDocument( encoding, version );
    }

    public void writeCharacters( String text )
                            throws XMLStreamException {
        xmlWriter.writeCharacters( text );
    }

    public void writeCharacters( char[] text, int start, int len )
                            throws XMLStreamException {
        xmlWriter.writeCharacters( text, start, len );
    }

    public String getPrefix( String uri )
                            throws XMLStreamException {
        return xmlWriter.getPrefix( uri );
    }

    public void setPrefix( String prefix, String uri )
                            throws XMLStreamException {
        if ( !prefixes.contains( prefix ) ) {
            xmlWriter.setPrefix( prefix, uri );
        }
        prefixes.add( prefix );
    }

    public void setDefaultNamespace( String uri )
                            throws XMLStreamException {
        xmlWriter.setDefaultNamespace( uri );
    }

    public void setNamespaceContext( NamespaceContext context )
                            throws XMLStreamException {
        xmlWriter.setNamespaceContext( context );
    }

    public NamespaceContext getNamespaceContext() {
        return xmlWriter.getNamespaceContext();
    }

    public Object getProperty( String name )
                            throws IllegalArgumentException {
        return xmlWriter.getProperty( name );
    }
}