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

import static javax.xml.stream.XMLStreamConstants.ATTRIBUTE;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.i18n.Messages;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLParsingException;
import org.slf4j.Logger;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StAXParsingHelper {

    private static final Logger LOG = getLogger( StAXParsingHelper.class );

    /**
     * Creates printable (debug) information about the event that the cursor of the given <code>XMLStreamReader</code>
     * currently points at.
     * 
     * @param xmlStream
     * @return printable information
     */
    public static final String getCurrentEventInfo( XMLStreamReader xmlStream ) {
        String s = getEventTypeString( xmlStream.getEventType() );
        if ( xmlStream.getEventType() == START_ELEMENT || xmlStream.getEventType() == END_ELEMENT ) {
            s += ": " + xmlStream.getName();
        }
        Location location = xmlStream.getLocation();
        s += " at line " + location.getLineNumber() + ", column " + location.getColumnNumber() + " (character offset "
             + xmlStream.getLocation().getCharacterOffset() + ")";
        return s;
    }

    /**
     * Skips all events that belong to the current element (including descendant elements), so that the
     * <code>XMLStreamReader</code> cursor points at the corresponding <code>END_ELEMENT</code> event.
     * 
     * @param xmlStream
     * @throws XMLStreamException
     */
    public static void skipElement( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        if ( xmlStream.isEndElement() ) {
            return;
        }

        int openElements = 1;
        while ( openElements > 0 ) {
            // this should not be necessary, but IS
            if ( !xmlStream.hasNext() ) {
                throw new NoSuchElementException();
            }
            int event = xmlStream.next();
            if ( event == END_ELEMENT ) {
                openElements--;
            } else if ( event == START_ELEMENT ) {
                openElements++;
            }
        }
    }

    /**
     * @param url
     * @param in
     * @return a resolved URL against the systemid of the reader
     * @throws MalformedURLException
     */
    public static URL resolve( String url, XMLStreamReader in )
                            throws MalformedURLException {
        String systemId = in.getLocation().getSystemId();
        if ( systemId == null ) {
            LOG.warn( "SystemID was null, cannot resolve '{}', trying to use it as absolute URL.", url );
            return new URL( url );
        }

        LOG.debug( "Resolving URL '" + url + "' against SystemID '" + systemId + "'." );

        // check if url is an absolute path
        File file = new File( url );
        if ( file.isAbsolute() ) {
            return file.toURI().toURL();
        }

        URL resolvedURL = new URL( new URL( systemId ), url );
        LOG.debug( "-> resolvedURL: '" + resolvedURL + "'" );
        return resolvedURL;
    }

    /**
     * @param xmlStream
     * @param s
     *            may not be null
     * @return a parsed qname
     */
    public static QName asQName( XMLStreamReader xmlStream, String s ) {
        QName result = null;
        int colonIdx = s.indexOf( ':' );
        if ( colonIdx < 0 ) {
            result = new QName( s );
        } else if ( colonIdx == s.length() - 1 ) {
            throw new XMLParsingException( xmlStream, "Invalid QName '" + s + "': no local name." );
        } else {
            String prefix = s.substring( 0, colonIdx );
            String localPart = s.substring( colonIdx + 1 );
            String nsUri = xmlStream.getNamespaceURI( prefix );
            if ( nsUri == null ) {
                throw new XMLParsingException( xmlStream, "Invalid QName '" + s + "': prefix '" + prefix
                                                          + "' is unbound." );
            }
            result = new QName( nsUri, localPart, prefix );
        }
        return result;
    }

    public static void skipStartDocument( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        if ( xmlStream.getEventType() == START_DOCUMENT ) {
            xmlStream.nextTag();
        }
    }

    public static String getAttributeValue( XMLStreamReader xmlStream, String localName )
                            throws XMLParsingException {
        return xmlStream.getAttributeValue( null, localName );
    }

    public static String getRequiredAttributeValue( XMLStreamReader xmlStream, String localName )
                            throws XMLParsingException {
        return getRequiredAttributeValue( xmlStream, null, localName );
    }

    public static String getRequiredAttributeValue( XMLStreamReader xmlStream, String namespaceURI, String localName )
                            throws XMLParsingException {
        String s = xmlStream.getAttributeValue( namespaceURI, localName );
        if ( s == null ) {
            throw new XMLParsingException( xmlStream, "No attribute with name {" + namespaceURI + "}" + localName + "." );
        }
        return s;
    }

    public static QName getAttributeValueAsQName( XMLStreamReader xmlStream, String namespaceURI, String localName )
                            throws XMLParsingException {
        QName result = null;
        String s = xmlStream.getAttributeValue( namespaceURI, localName );
        if ( s == null ) {
            throw new XMLParsingException( xmlStream, "No attribute with name {" + namespaceURI + "}" + localName + "." );
        }
        return asQName( xmlStream, s );
    }

    private static boolean parseAsBoolean( XMLStreamReader xmlStream, String s ) {
        if ( "true".equals( s ) || "1".equals( s ) ) {
            return true;
        } else if ( "false".equals( s ) || "0".equals( s ) ) {
            return false;
        } else {
            String msg = Messages.getMessage( "XML_SYNTAX_ERROR_BOOLEAN", s );
            throw new XMLParsingException( xmlStream, msg );
        }
    }

    /**
     * @param xmlStream
     * @return the element text as boolean
     * @throws XMLStreamException
     */
    public static boolean getElementTextAsBoolean( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        return parseAsBoolean( xmlStream, xmlStream.getElementText() );
    }

    /**
     * Post: reader will be unchanged or at {@link XMLStreamConstants#END_ELEMENT} of the matching element or at
     * {@link XMLStreamConstants #START_ELEMENT} of the next element if requested.
     * 
     * @param reader
     * @param elementName
     * @param defaultValue
     * @param nextElemOnSucces
     *            if true the reader will be moved to the next tag if the retrieval was successful.
     * @return the element text as boolean
     * @throws XMLStreamException
     */
    public static boolean getElementTextAsBoolean( XMLStreamReader reader, QName elementName, boolean defaultValue,
                                                   boolean nextElemOnSucces )
                            throws XMLStreamException {
        boolean res = defaultValue;
        if ( elementName.equals( reader.getName() ) ) {
            res = parseAsBoolean( reader, reader.getElementText() );
            if ( nextElemOnSucces ) {
                nextElement( reader );
            }
        }
        return res;
    }

    public static boolean getAttributeValueAsBoolean( XMLStreamReader xmlStream, String namespaceURI, String localName,
                                                      boolean defaultValue )
                            throws XMLParsingException {

        boolean result = defaultValue;
        String s = xmlStream.getAttributeValue( namespaceURI, localName );
        if ( s != null ) {
            result = parseAsBoolean( xmlStream, s );
        }
        return result;
    }

    public static QName getElementTextAsQName( XMLStreamReader xmlStream )
                            throws XMLParsingException, XMLStreamException {
        QName result = null;
        String s = xmlStream.getElementText();
        if ( s == null ) {
            throw new XMLParsingException( xmlStream, "No element text, but QName expected." );
        }
        return asQName( xmlStream, s );
    }

    public static void requireStartElement( XMLStreamReader xmlStream, Collection<QName> expectedElements )
                            throws XMLParsingException {
        if ( xmlStream.getEventType() != START_ELEMENT ) {
            String msg = Messages.getMessage( "XML_EXPECTED_ELEMENT_1", getEventTypeString( xmlStream.getEventType() ),
                                              ArrayUtils.join( ",", expectedElements ) );
            throw new XMLParsingException( xmlStream, msg );
        }
        if ( !expectedElements.contains( xmlStream.getName() ) ) {
            String msg = Messages.getMessage( "XML_EXPECTED_ELEMENT_2", xmlStream.getName(),
                                              ArrayUtils.join( ",", expectedElements ) );
            throw new XMLParsingException( xmlStream, msg );
        }
    }

    public static void require( XMLStreamReader xmlStream, int eventType )
                            throws XMLParsingException {
        if ( xmlStream.getEventType() != eventType ) {
            String msg = Messages.getMessage( "XML_UNEXPECTED_TYPE", getEventTypeString( xmlStream.getEventType() ),
                                              getEventTypeString( eventType ) );
            throw new XMLParsingException( xmlStream, msg );
        }
    }

    public static void requireNextTag( XMLStreamReader xmlStream, int eventType )
                            throws XMLParsingException, XMLStreamException {
        if ( xmlStream.nextTag() != eventType ) {
            String msg = Messages.getMessage( "XML_UNEXPECTED_TYPE", getEventTypeString( xmlStream.getEventType() ),
                                              getEventTypeString( eventType ) );
            throw new XMLParsingException( xmlStream, msg );
        }
    }

    public static final String getEventTypeString( int eventType ) {
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

    public static NamespaceContext getDeegreeNamespaceContext( XMLStreamReader xmlStream ) {
        return new NamespaceContext( xmlStream.getNamespaceContext() );
    }

    public static boolean getRequiredAttributeValueAsBoolean( XMLStreamReader xmlStream, String namespaceURI,
                                                              String localName ) {
        return parseAsBoolean( xmlStream, getRequiredAttributeValue( xmlStream, namespaceURI, localName ) );
    }

    /**
     * Get the text of the element or if the reader does not match the given elementName the default text will be
     * returned. Post: reader will be unchanged or at {@link XMLStreamConstants#END_ELEMENT} of the matching element or
     * at {@link XMLStreamConstants #START_ELEMENT} of the next element if requested.
     * 
     * 
     * @param reader
     * @param elemName
     * @param defaultText
     * @param nextElemOnSucces
     *            if true the reader will be moved to the next tag if the retrieval was successful.
     * @return the text of the element or if the reader does not match the given elementName the default text will be
     *         returned.
     * @throws XMLStreamException
     */
    public static String getText( XMLStreamReader reader, QName elemName, String defaultText, boolean nextElemOnSucces )
                            throws XMLStreamException {
        String value = defaultText;
        if ( reader.isStartElement() && reader.getName().equals( elemName ) ) {
            value = reader.getElementText();
            if ( nextElemOnSucces ) {
                nextElement( reader );
            }
        }
        return value;
    }

    /**
     * Skips to the next element if the reader points the required element. Post: reader will be at
     * {@link XMLStreamConstants#START_ELEMENT} of the next element.
     * 
     * @param reader
     * @param elementName
     * @throws XMLStreamException
     */
    public static void skipRequiredElement( XMLStreamReader reader, QName elementName )
                            throws XMLStreamException {
        if ( reader.isStartElement() && reader.getName().equals( elementName ) ) {
            nextElement( reader );
            if ( reader.isEndElement() && reader.getName().equals( elementName ) ) {
                nextElement( reader );
            }
            return;
        }
        throw new XMLParsingException( reader, "Required element " + elementName
                                               + " was not found at given stream position." );
    }

    /**
     * Get the text of the given element which must be an element with given name. Post: reader will be at
     * {@link XMLStreamConstants#END_ELEMENT} of matching element or at {@link XMLStreamConstants #START_ELEMENT} of the
     * next element if requested.
     * 
     * @param reader
     * @param elementName
     * @param nextElemOnSucces
     *            if true the reader will be moved to the next tag if the retrieval was successful.
     * @return the text of the current 'required' element.
     * @throws XMLStreamException
     */
    public static String getRequiredText( XMLStreamReader reader, QName elementName, boolean nextElemOnSucces )
                            throws XMLStreamException {
        if ( reader.isStartElement() && reader.getName().equals( elementName ) ) {
            String val = reader.getElementText();
            if ( nextElemOnSucces ) {
                nextElement( reader );
            }
            return val;
        }
        throw new XMLParsingException( reader, "Required element " + elementName
                                               + " was not found at given stream position." );
    }

    /**
     * The reader must be on a StartElement, any attributes will be skipped. Post: reader will be unchanged or at
     * {@link XMLStreamConstants#START_ELEMENT } of the first element after the last matching element
     * 
     * @param reader
     * @param name
     *            of the elements
     * @return an array of strings, denoting the elements with the given name.
     * @throws XMLStreamException
     */
    public static String[] getSimpleUnboundedAsStrings( XMLStreamReader reader, QName name )
                            throws XMLStreamException {
        List<String> values = new LinkedList<String>();
        if ( reader.isStartElement() ) {
            while ( name.equals( reader.getName() ) ) {
                String text = reader.getElementText();
                if ( text != null ) {
                    values.add( text );
                }
                // move beyond the end tag.
                nextElement( reader );
            }
        }
        return values.toArray( new String[values.size()] );
    }

    /**
     * Move the reader to the next {@link XMLStreamConstants #START_ELEMENT} or {@link XMLStreamConstants #END_ELEMENT}
     * event.
     * 
     * @param xmlReader
     * @return event type
     * @throws XMLStreamException
     * @throws NoSuchElementException
     *             if the end of the document is reached
     */
    public static int nextElement( XMLStreamReader xmlReader )
                            throws XMLStreamException, NoSuchElementException {
        xmlReader.next();
        while ( xmlReader.getEventType() != END_DOCUMENT && !xmlReader.isStartElement() && !xmlReader.isEndElement() ) {
            xmlReader.next();
        }
        if ( xmlReader.getEventType() == END_DOCUMENT ) {
            throw new NoSuchElementException();
        }
        return xmlReader.getEventType();
    }

    /**
     * Post: reader will be unchanged or on success at {@link XMLStreamConstants #END_ELEMENT} of the matching element
     * or at {@link XMLStreamConstants #START_ELEMENT} of the next element if requested.
     * 
     * @param reader
     *            pointing to the current element.
     * @param elementName
     *            of the current element.
     * @param defaultValue
     *            to return if the current name was not the one given or the value could not be parsed as a double.
     * @param nextElemOnSucces
     *            if true the reader will be moved to the next tag if the retrieval was successful.
     * @return the text of the current element (which should have element name) parsed as a double.
     * @throws XMLStreamException
     *             from {@link XMLStreamReader#getElementText()}.
     */
    public static double getElementTextAsDouble( XMLStreamReader reader, QName elementName, double defaultValue,
                                                 boolean nextElemOnSucces )
                            throws XMLStreamException {
        double value = defaultValue;
        if ( elementName.equals( reader.getName() ) && reader.isStartElement() ) {
            String s = reader.getElementText();
            if ( s != null ) {
                try {
                    value = Double.parseDouble( s );
                    if ( nextElemOnSucces ) {
                        nextElement( reader );
                    }
                } catch ( NumberFormatException nfe ) {
                    LOG.debug( reader.getLocation() + ") Value " + s + " in element: " + elementName
                               + " was not a parsable double, returning double value: " + defaultValue );
                }
            }
        }
        return value;
    }

    /**
     * Post: reader will be unchanged or on success at {@link XMLStreamConstants #END_ELEMENT} of the matching element
     * or at {@link XMLStreamConstants #START_ELEMENT} of the next element if requested.
     * 
     * @param reader
     *            pointing to the current element.
     * @param elementName
     *            of the current element.
     * @param defaultValue
     *            to return if the current name was not the one given or the value could not be parsed as a integer.
     * @param nextElemOnSucces
     *            if true the reader will be moved to the next tag if the retrieval was successful.
     * @return the text of the current element (which should have element name) parsed as a integer.
     * @throws XMLStreamException
     *             from {@link XMLStreamReader#getElementText()}.
     */
    public static int getElementTextAsInteger( XMLStreamReader reader, QName elementName, int defaultValue,
                                               boolean nextElemOnSucces )
                            throws XMLStreamException {
        int value = defaultValue;
        if ( elementName.equals( reader.getName() ) && reader.isStartElement() ) {
            String s = reader.getElementText();
            if ( s != null ) {
                try {
                    value = Integer.parseInt( s );
                    if ( nextElemOnSucces ) {
                        nextElement( reader );
                    }
                } catch ( NumberFormatException nfe ) {
                    LOG.debug( reader.getLocation() + ") Value " + s + " in element: " + elementName
                               + " was not a parsable integer, returning integer value: " + defaultValue );
                }
            }
        }
        return value;
    }

    /**
     * Returns the text in the required element as a double. If the name of the reader does not match the given qName,
     * an exception will be thrown. If the value is not a double, an exception will be thrown. Post: reader will be
     * unchanged or at {@link XMLStreamConstants #END_ELEMENT} of the matching element or at
     * {@link XMLStreamConstants #START_ELEMENT} of the next element if requested.
     * 
     * @param reader
     * @param elementName
     * @param nextElemOnSucces
     *            if true the reader will be move to the next element if the operation was successful.
     * @return the double value of the required element.
     * @throws XMLStreamException
     */
    public static double getRequiredElementTextAsDouble( XMLStreamReader reader, QName elementName,
                                                         boolean nextElemOnSucces )
                            throws XMLStreamException {
        if ( !elementName.equals( reader.getName() ) ) {
            throw new XMLParsingException( reader, "The current element: " + reader.getName() + " is not expected: "
                                                   + elementName );
        }
        double result = getElementTextAsDouble( reader, elementName, Double.NaN, nextElemOnSucces );
        if ( Double.isNaN( result ) ) {
            throw new XMLParsingException( reader, "The element " + elementName + " does not specify a double value." );
        }
        return result;
    }

    /**
     * Move the reader to the first element which matches the given name. The reader will be positioned on the
     * {@link XMLStreamConstants#START_ELEMENT} event or after the {@link XMLStreamConstants#END_DOCUMENT} which ever
     * comes first.
     * 
     * @param reader
     *            to position
     * @param elementName
     *            name of the element to move forward to.
     * @return true if the reader is on the given element, false otherwise.
     * @throws XMLStreamException
     * 
     */
    public static boolean moveReaderToFirstMatch( XMLStreamReader reader, QName elementName )
                            throws XMLStreamException {
        if ( elementName == null ) {
            return true;
        }
        Set<QName> allowed = new HashSet<QName>( 1 );
        allowed.add( elementName );
        return moveReaderToFirstMatch( reader, allowed );
    }

    /**
     * Move the reader to the first element which matches one of the given name(s). The reader will be positioned on the
     * {@link XMLStreamConstants#START_ELEMENT} event or after the {@link XMLStreamConstants#END_DOCUMENT} which ever
     * comes first.
     * 
     * @param reader
     *            to position
     * @param alowedElements
     *            name of the element to move forward to.
     * @return true if the reader is on the given element, false otherwise.
     * @throws XMLStreamException
     * 
     */
    public static boolean moveReaderToFirstMatch( XMLStreamReader reader, Collection<QName> alowedElements )
                            throws XMLStreamException {
        boolean hasMoreElements = true;
        do {
            if ( reader.isStartElement() && alowedElements.contains( reader.getName() ) ) {
                return true;
            }
            try {
                if ( LOG.isDebugEnabled() ) {
                    if ( reader.isStartElement() || reader.isEndElement() ) {
                        LOG.debug( "Skipping element: " + reader.getName() );
                    }
                }
                nextElement( reader );
            } catch ( NoSuchElementException e ) {
                // end of file
                hasMoreElements = false;
            }

        } while ( hasMoreElements );
        return false;
    }
}
