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
package org.deegree.metadata;

import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.StAXParsingHelper;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class NamedElementCreator implements XMLStreamReader {
    private final XMLStreamReader input;

    private final Set<QName> removeElements;

    public NamedElementCreator( XMLStreamReader input, Set<QName> removeElements ) {
        this.input = input;
        this.removeElements = removeElements;
    }

    public void close()
                            throws XMLStreamException {
        input.close();
    }

    public int getAttributeCount() {
        return input.getAttributeCount();
    }

    public String getAttributeLocalName( int index ) {
        return input.getAttributeLocalName( index );
    }

    public QName getAttributeName( int index ) {
        return input.getAttributeName( index );
    }

    public String getAttributeNamespace( int index ) {
        return input.getAttributeNamespace( index );
    }

    public String getAttributePrefix( int index ) {
        return input.getAttributePrefix( index );
    }

    public String getAttributeType( int index ) {
        return input.getAttributeType( index );
    }

    public String getAttributeValue( int index ) {
        return input.getAttributeValue( index );
    }

    public String getAttributeValue( String namespaceURI, String localName ) {
        return input.getAttributeValue( namespaceURI, localName );
    }

    public String getCharacterEncodingScheme() {
        return input.getCharacterEncodingScheme();
    }

    public String getElementText()
                            throws XMLStreamException {
        return input.getElementText();
    }

    public String getEncoding() {
        return input.getEncoding();
    }

    public int getEventType() {
        return input.getEventType();
    }

    public String getLocalName() {
        return input.getLocalName();
    }

    public Location getLocation() {
        return input.getLocation();
    }

    public QName getName() {
        return input.getName();
    }

    public NamespaceContext getNamespaceContext() {
        return input.getNamespaceContext();
    }

    public int getNamespaceCount() {
        return input.getNamespaceCount();
    }

    public String getNamespacePrefix( int index ) {
        return input.getNamespacePrefix( index );
    }

    public String getNamespaceURI() {
        return input.getNamespaceURI();
    }

    public String getNamespaceURI( int index ) {
        return input.getNamespaceURI( index );
    }

    public String getNamespaceURI( String prefix ) {
        return input.getNamespaceURI( prefix );
    }

    public String getPIData() {
        return input.getPIData();
    }

    public String getPITarget() {
        return input.getPITarget();
    }

    public String getPrefix() {
        return input.getPrefix();
    }

    public Object getProperty( String name )
                            throws IllegalArgumentException {
        return input.getProperty( name );
    }

    public String getText() {
        return input.getText();
    }

    public char[] getTextCharacters() {
        return input.getTextCharacters();
    }

    public int getTextCharacters( int sourceStart, char[] target, int targetStart, int length )
                            throws XMLStreamException {
        return input.getTextCharacters( sourceStart, target, targetStart, length );
    }

    public int getTextLength() {
        return input.getTextLength();
    }

    public int getTextStart() {
        return input.getTextStart();
    }

    public String getVersion() {
        return input.getVersion();
    }

    public boolean hasName() {
        return input.hasName();
    }

    public boolean hasNext()
                            throws XMLStreamException {
        return input.hasNext();
    }

    public boolean hasText() {
        return input.hasText();
    }

    public boolean isAttributeSpecified( int index ) {
        return input.isAttributeSpecified( index );
    }

    public boolean isCharacters() {
        return input.isCharacters();
    }

    public boolean isEndElement() {
        return input.isEndElement();
    }

    public boolean isStandalone() {
        return input.isStandalone();
    }

    public boolean isStartElement() {
        return input.isStartElement();
    }

    public boolean isWhiteSpace() {
        return input.isWhiteSpace();
    }

    public int next()
                            throws XMLStreamException {
        int event = input.next();
        while ( ( event == XMLStreamConstants.START_ELEMENT || event == XMLStreamConstants.END_ELEMENT )
                && !removeElements.contains( getName() ) ) {
            StAXParsingHelper.skipElement( input );
            event = StAXParsingHelper.nextElement( input );
        }

        return event;
    }

    public int nextTag()
                            throws XMLStreamException {
        int event = input.nextTag();
        while ( ( event == XMLStreamConstants.START_ELEMENT || event == XMLStreamConstants.END_ELEMENT )
                && !removeElements.contains( getName() ) ) {
            StAXParsingHelper.skipElement( input );
            event = StAXParsingHelper.nextElement( input );
        }
        return event;
    }

    public void require( int type, String namespaceURI, String localName )
                            throws XMLStreamException {
        input.require( type, namespaceURI, localName );
    }

    public boolean standaloneSet() {
        return input.standaloneSet();
    }
}
