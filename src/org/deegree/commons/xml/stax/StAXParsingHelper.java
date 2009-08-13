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

import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.i18n.Messages;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Expression;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StAXParsingHelper {

    public static void skipStartDocument( XMLStreamReader xmlStream ) throws XMLStreamException {
        if ( xmlStream.getEventType() == START_DOCUMENT ) {
            xmlStream.nextTag();
        }
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

    public static boolean getAttributeValueAsBoolean( XMLStreamReader xmlStream, String namespaceURI, String localName,
                                                      boolean defaultValue )
                            throws XMLParsingException {

        boolean result = defaultValue;
        String s = xmlStream.getAttributeValue( namespaceURI, localName );
        if ( s != null ) {
            if ( "true".equals( s ) || "1".equals( s ) ) {
                result = true;
            } else if ( "false".equals( s ) || "0".equals( s ) ) {
                result = false;
            } else {
                String msg = Messages.getMessage( "XML_SYNTAX_ERROR_BOOLEAN", s );
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        return result;
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
}
