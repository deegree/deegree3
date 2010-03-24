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
package org.deegree.commons.xml.stax;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Wraps an <code>XMLStreamReader</code> that points at a START_ELEMENT event, so the sequence of events starts with
 * START_DOCUMENT.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLStreamReaderDoc implements XMLStreamReader {

    private XMLStreamReader xmlStream;

    private boolean isFirst = true;

    /**
     * Creates a new {@link XMLStreamReaderDoc} instance.
     * 
     * @param xmlStream
     */
    public XMLStreamReaderDoc( XMLStreamReader xmlStream ) {
        this.xmlStream = xmlStream;
    }

    public void close()
                            throws XMLStreamException {
        xmlStream.close();
    }

    public int getAttributeCount() {
        return xmlStream.getAttributeCount();
    }

    public String getAttributeLocalName( int index ) {
        return xmlStream.getAttributeLocalName( index );
    }

    public QName getAttributeName( int index ) {
        return xmlStream.getAttributeName( index );
    }

    public String getAttributeNamespace( int index ) {
        return xmlStream.getAttributeNamespace( index );
    }

    public String getAttributePrefix( int index ) {
        return xmlStream.getAttributePrefix( index );
    }

    public String getAttributeType( int index ) {
        return xmlStream.getAttributeType( index );
    }

    public String getAttributeValue( int index ) {
        return xmlStream.getAttributeValue( index );
    }

    public String getAttributeValue( String namespaceURI, String localName ) {
        return xmlStream.getAttributeValue( namespaceURI, localName );
    }

    public String getCharacterEncodingScheme() {
        return xmlStream.getCharacterEncodingScheme();
    }

    public String getElementText()
                            throws XMLStreamException {
        return xmlStream.getElementText();
    }

    public String getEncoding() {
        return xmlStream.getEncoding();
    }

    public int getEventType() {
        if ( isFirst ) {
            return START_DOCUMENT;
        }
        return xmlStream.getEventType();
    }

    public String getLocalName() {
        return xmlStream.getLocalName();
    }

    public Location getLocation() {
        return xmlStream.getLocation();
    }

    public QName getName() {
        return xmlStream.getName();
    }

    public NamespaceContext getNamespaceContext() {
        return xmlStream.getNamespaceContext();
    }

    public int getNamespaceCount() {
        return xmlStream.getNamespaceCount();
    }

    public String getNamespacePrefix( int index ) {
        return xmlStream.getNamespacePrefix( index );
    }

    public String getNamespaceURI() {
        return xmlStream.getNamespaceURI();
    }

    public String getNamespaceURI( int index ) {
        return xmlStream.getNamespaceURI( index );
    }

    public String getNamespaceURI( String prefix ) {
        return xmlStream.getNamespaceURI( prefix );
    }

    public String getPIData() {
        return xmlStream.getPIData();
    }

    public String getPITarget() {
        return xmlStream.getPITarget();
    }

    public String getPrefix() {
        return xmlStream.getPrefix();
    }

    public Object getProperty( String name )
                            throws IllegalArgumentException {
        return xmlStream.getProperty( name );
    }

    public String getText() {
        return xmlStream.getText();
    }

    public char[] getTextCharacters() {
        return xmlStream.getTextCharacters();
    }

    public int getTextCharacters( int sourceStart, char[] target, int targetStart, int length )
                            throws XMLStreamException {
        return xmlStream.getTextCharacters( sourceStart, target, targetStart, length );
    }

    public int getTextLength() {
        return xmlStream.getTextLength();
    }

    public int getTextStart() {
        return xmlStream.getTextStart();
    }

    public String getVersion() {
        return xmlStream.getVersion();
    }

    public boolean hasName() {
        return xmlStream.hasName();
    }

    public boolean hasNext()
                            throws XMLStreamException {
        return xmlStream.hasNext();
    }

    public boolean hasText() {
        return xmlStream.hasText();
    }

    public boolean isAttributeSpecified( int index ) {
        return xmlStream.isAttributeSpecified( index );
    }

    public boolean isCharacters() {
        return xmlStream.isCharacters();
    }

    public boolean isEndElement() {
        return xmlStream.isEndElement();
    }

    public boolean isStandalone() {
        return xmlStream.isStandalone();
    }

    public boolean isStartElement() {
        return xmlStream.isStartElement();
    }

    public boolean isWhiteSpace() {
        return xmlStream.isWhiteSpace();
    }

    public int next()
                            throws XMLStreamException {
        if ( isFirst ) {
            isFirst = false;
            return START_ELEMENT;
        }
        return xmlStream.next();
    }

    public int nextTag()
                            throws XMLStreamException {
        if ( isFirst ) {
            isFirst = false;
            return START_ELEMENT;
        }
        return xmlStream.nextTag();
    }

    public void require( int type, String namespaceURI, String localName )
                            throws XMLStreamException {
        if ( isFirst && type != START_DOCUMENT ) {
            throw new XMLStreamException();
        }
        xmlStream.require( type, namespaceURI, localName );
    }

    public boolean standaloneSet() {
        return xmlStream.standaloneSet();
    }
}
