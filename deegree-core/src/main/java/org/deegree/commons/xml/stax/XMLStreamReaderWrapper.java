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
package org.deegree.commons.xml.stax;

import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class XMLStreamReaderWrapper implements XMLStreamReader {

    private XMLStreamReader reader;

    private String systemId;

    public XMLStreamReaderWrapper( URL docURL ) throws XMLStreamException, FactoryConfigurationError, IOException {
        systemId = docURL.toExternalForm();
        reader = XMLInputFactory.newInstance().createXMLStreamReader( systemId, docURL.openStream() );
    }

    public XMLStreamReaderWrapper( XMLStreamReader reader, String systemId ) {
        this.reader = reader;
        this.systemId = systemId;
    }

    public String getSystemId() {
        return systemId;
    }

    /**
     * Creates printable (debug) information about the event that the cursor of the given <code>XMLStreamReader</code>
     * currently points at.
     * 
     * @return printable information
     */
    public final String getCurrentEventInfo() {
        String s = getEventTypeString( getEventType() );
        if ( getEventType() == START_ELEMENT || getEventType() == END_ELEMENT ) {
            s += ": " + getName();
        }
        Location location = getLocation();
        s += " at line " + location.getLineNumber() + ", column " + location.getColumnNumber() + " (character offset "
             + getLocation().getCharacterOffset() + ")";
        return s;
    }

    private final String getEventTypeString( int eventType ) {
        switch ( eventType ) {
        case START_ELEMENT:
            return "START_ELEMENT";
        case END_ELEMENT:
            return "END_ELEMENT";
        case PROCESSING_INSTRUCTION:
            return "PROCESSING_INSTRUCTION";
        case CHARACTERS:
            return "CHARACTERS";
        case COMMENT:
            return "COMMENT";
        case START_DOCUMENT:
            return "START_DOCUMENT";
        case END_DOCUMENT:
            return "END_DOCUMENT";
        case ENTITY_REFERENCE:
            return "ENTITY_REFERENCE";
        case ATTRIBUTE:
            return "ATTRIBUTE";
        case DTD:
            return "DTD";
        case CDATA:
            return "CDATA";
        case SPACE:
            return "SPACE";
        }
        return "UNKNOWN_EVENT_TYPE , " + eventType;
    }

    /**
     * Skips all events that belong to the current element (including descendant elements), so that the
     * <code>XMLStreamReader</code> cursor points at the corresponding <code>END_ELEMENT</code> event.
     * 
     * @throws XMLStreamException
     */
    public void skipElement()
                            throws XMLStreamException {

        int openElements = 1;
        while ( openElements > 0 ) {
            // this should not be necessary, but IS
            if ( !hasNext() ) {
                throw new NoSuchElementException();
            }
            int event = next();
            if ( event == END_ELEMENT ) {
                openElements--;
            } else if ( event == START_ELEMENT ) {
                openElements++;
            }
        }
    }

    public String getAttributeValueWDefault( String localName, String defaultValue ) {
        String attrValue = getAttributeValue( null, localName );
        if ( attrValue == null || attrValue.length() == 0 ) {
            attrValue = defaultValue;
        }
        return attrValue;
    }

    public String getAttributeValueWDefault( QName name, String defaultValue ) {
        String attrValue = getAttributeValue( name.getNamespaceURI(), name.getLocalPart() );
        if ( attrValue == null || attrValue.length() == 0 ) {
            attrValue = defaultValue;
        }
        return attrValue;
    }

    public QName getElementTextAsQName()
                            throws XMLParsingException {
        QName result = null;
        try {
            String s = getElementText();
            int colonIdx = s.indexOf( ':' );
            if ( colonIdx < 0 ) {
                result = new QName( s );
            } else if ( colonIdx == s.length() - 1 ) {
                throw new XMLParsingException( this, "Invalid QName '" + s + "': no local name." );
            } else {
                String prefix = s.substring( 0, colonIdx );
                String localPart = s.substring( colonIdx + 1 );
                String nsUri = getNamespaceURI( prefix );
                if ( nsUri == null ) {
                    throw new XMLParsingException( this, "Invalid QName '" + s + "': prefix '" + prefix
                                                         + "' is unbound." );
                }
                result = new QName( nsUri, localPart, prefix );
            }
        } catch ( XMLStreamException e ) {
            throw new XMLParsingException( this, e.getMessage() );
        }
        return result;
    }

    public double getElementTextAsDouble()
                            throws XMLParsingException {
        String s = null;
        try {
            s = getElementText();
            return Double.parseDouble( s );
        } catch ( NumberFormatException e ) {
            String msg = "Expected a double value, but found '" + s + "'.";
            throw new XMLParsingException( this, msg );
        } catch ( XMLStreamException e ) {
            throw new XMLParsingException( this, e.getMessage() );
        }
    }

    public int getElementTextAsPositiveInteger()
                            throws XMLParsingException {

        int i = 0;
        String s = null;
        try {
            s = getElementText();
            i = Integer.parseInt( s );
        } catch ( NumberFormatException e ) {
            String msg = "Expected a positive integer value, but found '" + s + "'.";
            throw new XMLParsingException( this, msg );
        } catch ( XMLStreamException e ) {
            throw new XMLParsingException( this, e.getMessage() );
        }

        if ( i <= 0 ) {
            String msg = "Expected a positive integer value, but found '" + i + "'.";
            throw new XMLParsingException( this, msg );
        }
        return i;
    }

    public double getElementTextAsDouble( String namespaceURI, String localName )
                            throws XMLParsingException, XMLStreamException {
        require( START_ELEMENT, namespaceURI, localName );
        return getElementTextAsDouble();
    }

    public int getElementTextAsPositiveInteger( String namespaceURI, String localName )
                            throws XMLParsingException, XMLStreamException {
        require( START_ELEMENT, namespaceURI, localName );
        return getElementTextAsPositiveInteger();
    }

    @Override
    public void require( int type, String namespaceURI, String localName )
                            throws XMLStreamException {
        if ( getEventType() != type ) {
            String msg = "Expected event type " + type + ", but found " + getCurrentEventInfo();
            throw new XMLParsingException( this, msg );
        }

        if ( type == START_ELEMENT || type == END_ELEMENT ) {
            if ( !getName().equals( new QName( namespaceURI, localName ) ) ) {
                String msg = "Expected {" + namespaceURI + "}" + localName + ", but found: " + getCurrentEventInfo();
                throw new XMLParsingException( this, msg );
            }
        }
    }

    // -----------------------------------------------------------------------
    // wrapped standard methods of XMLStreamReader
    // -----------------------------------------------------------------------

    @Override
    public void close()
                            throws XMLStreamException {
        reader.close();
    }

    @Override
    public int getAttributeCount() {
        return reader.getAttributeCount();
    }

    @Override
    public String getAttributeLocalName( int index ) {
        return reader.getAttributeLocalName( index );
    }

    @Override
    public QName getAttributeName( int index ) {
        return reader.getAttributeName( index );
    }

    @Override
    public String getAttributeNamespace( int index ) {
        return reader.getAttributeNamespace( index );
    }

    @Override
    public String getAttributePrefix( int index ) {
        return reader.getAttributePrefix( index );
    }

    @Override
    public String getAttributeType( int index ) {
        return reader.getAttributeType( index );
    }

    @Override
    public String getAttributeValue( int index ) {
        return reader.getAttributeValue( index );
    }

    @Override
    public String getAttributeValue( String namespaceURI, String localName ) {
        return reader.getAttributeValue( namespaceURI, localName );
    }

    public QName getAttributeValueAsQName( String namespaceURI, String localName )
                            throws XMLParsingException {
        QName result = null;
        String s = reader.getAttributeValue( namespaceURI, localName );
        if ( s == null ) {
            throw new XMLParsingException( this, "No attribute with name {" + namespaceURI + "}" + localName + "." );
        }
        int colonIdx = s.indexOf( ':' );
        if ( colonIdx < 0 ) {
            result = new QName( s );
        } else if ( colonIdx == s.length() - 1 ) {
            throw new XMLParsingException( this, "Invalid QName '" + s + "': no local name." );
        } else {
            String prefix = s.substring( 0, colonIdx );
            String localPart = s.substring( colonIdx + 1 );
            String nsUri = getNamespaceURI( prefix );
            if ( nsUri == null ) {
                throw new XMLParsingException( this, "Invalid QName '" + s + "': prefix '" + prefix + "' is unbound." );
            }
            result = new QName( nsUri, localPart, prefix );
        }
        return result;
    }

    @Override
    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }

    /**
     * Not piped to wrapped reader, because AXIOM provided reader (withoutCaching) behaves strange here.
     */
    @Override
    public String getElementText()
                            throws XMLStreamException {
        if ( getEventType() != XMLStreamConstants.START_ELEMENT ) {
            throw new XMLStreamException( "parser must be on START_ELEMENT to read next text", getLocation() );
        }
        int eventType = next();
        StringBuilder content = new StringBuilder();
        while ( eventType != XMLStreamConstants.END_ELEMENT ) {
            if ( eventType == XMLStreamConstants.CHARACTERS || eventType == XMLStreamConstants.CDATA
                 || eventType == XMLStreamConstants.SPACE || eventType == XMLStreamConstants.ENTITY_REFERENCE ) {
                content.append( getText() );
            } else if ( eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                        || eventType == XMLStreamConstants.COMMENT ) {
                // skipping
            } else if ( eventType == XMLStreamConstants.END_DOCUMENT ) {
                throw new XMLStreamException( "unexpected end of document when reading element text content",
                                              getLocation() );
            } else if ( eventType == XMLStreamConstants.START_ELEMENT ) {
                throw new XMLStreamException( "element text content may not contain START_ELEMENT", getLocation() );
            } else {
                throw new XMLStreamException( "Unexpected event type " + eventType, getLocation() );
            }
            eventType = next();
        }
        return content.toString();
    }

    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }

    @Override
    public int getEventType() {
        return reader.getEventType();
    }

    @Override
    public String getLocalName() {
        return reader.getLocalName();
    }

    @Override
    public Location getLocation() {
        return reader.getLocation();
    }

    @Override
    public QName getName() {
        return reader.getName();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return reader.getNamespaceContext();
    }

    @Override
    public int getNamespaceCount() {
        return reader.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix( int index ) {
        return reader.getNamespacePrefix( index );
    }

    @Override
    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }

    @Override
    public String getNamespaceURI( String prefix ) {
        return reader.getNamespaceURI( prefix );
    }

    @Override
    public String getNamespaceURI( int index ) {
        return reader.getNamespaceURI( index );
    }

    @Override
    public String getPIData() {
        return reader.getPIData();
    }

    @Override
    public String getPITarget() {
        return reader.getPITarget();
    }

    @Override
    public String getPrefix() {
        return reader.getPrefix();
    }

    @Override
    public Object getProperty( String name )
                            throws IllegalArgumentException {
        return reader.getProperty( name );
    }

    @Override
    public String getText() {
        return reader.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return reader.getTextCharacters();
    }

    @Override
    public int getTextCharacters( int sourceStart, char[] target, int targetStart, int length )
                            throws XMLStreamException {
        return reader.getTextCharacters( sourceStart, target, targetStart, length );
    }

    @Override
    public int getTextLength() {
        return reader.getTextLength();
    }

    @Override
    public int getTextStart() {
        return reader.getTextStart();
    }

    @Override
    public String getVersion() {
        return reader.getVersion();
    }

    @Override
    public boolean hasName() {
        return reader.hasName();
    }

    @Override
    public boolean hasNext()
                            throws XMLStreamException {
        return reader.hasNext();
    }

    @Override
    public boolean hasText() {
        return reader.hasText();
    }

    @Override
    public boolean isAttributeSpecified( int index ) {
        return reader.isAttributeSpecified( index );
    }

    @Override
    public boolean isCharacters() {
        return reader.isCharacters();
    }

    @Override
    public boolean isEndElement() {
        return reader.isEndElement();
    }

    @Override
    public boolean isStandalone() {
        return reader.isStandalone();
    }

    @Override
    public boolean isStartElement() {
        return reader.isStartElement();
    }

    @Override
    public boolean isWhiteSpace() {
        return reader.isWhiteSpace();
    }

    @Override
    public int next()
                            throws XMLStreamException {
        return reader.next();
    }

    @Override
    public int nextTag()
                            throws XMLStreamException {
        return reader.nextTag();
    }

    @Override
    public boolean standaloneSet() {
        return reader.standaloneSet();
    }
}
