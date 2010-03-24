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
package org.deegree.gml.dictionary;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.props.GMLStdPropsReader;
import org.deegree.gml.props.GMLStdProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based reader for GML dictionaries and definitions.
 * 
 * @see GMLStreamReader
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLDictionaryReader {

    private static final Logger LOG = LoggerFactory.getLogger( GMLDictionaryReader.class );

    private final GMLVersion version;

    private final XMLStreamReader xmlStream;

    private final GMLDocumentIdContext idContext;

    private final GMLStdPropsReader propsReader;

    private final String gmlNs;

    public GMLDictionaryReader( GMLVersion version, XMLStreamReader xmlStream, GMLDocumentIdContext idContext ) {
        this.version = version;
        this.xmlStream = xmlStream;
        this.idContext = idContext;
        propsReader = new GMLStdPropsReader( version );
        gmlNs = version.getNamespace();
    }

    public Definition read()
                            throws XMLStreamException {
        Definition definition = null;
        QName elName = xmlStream.getName();
        if ( new QName( gmlNs, "Dictionary" ).equals( elName )
             || new QName( gmlNs, "DefinitionCollection" ).equals( elName ) ) {
            definition = readDictionary();
        } else if ( new QName( gmlNs, "Definition" ).equals( elName ) ) {
            definition = readDefinition();
        } else {
            String msg = "Invalid gml:Definition element: " + xmlStream.getName()
                         + "' is not a GML definition (or dictionary or definition collection) element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return definition;
    }

    private Definition readDefinition()
                            throws XMLStreamException {
        String id = xmlStream.getAttributeValue( gmlNs, "id" );
        GMLStdProps standardProps = propsReader.read( xmlStream );
        xmlStream.require( XMLStreamConstants.END_ELEMENT, gmlNs, "Definition" );
        Definition def = new GenericDefinition( id, standardProps );
        idContext.addObject( def );
        return def;
    }

    public Dictionary readDictionary()
                            throws XMLStreamException {

        boolean isDefinitionCollection = xmlStream.getName().equals( new QName( gmlNs, "DefinitionCollection" ) );

        String id = xmlStream.getAttributeValue( gmlNs, "id" );
        GMLStdProps standardProps = propsReader.read( xmlStream );

        List<Definition> members = new LinkedList<Definition>();

        while ( xmlStream.getEventType() == START_ELEMENT ) {
            QName elName = xmlStream.getName();
            if ( new QName( gmlNs, "dictionaryEntry" ).equals( elName )
                 || new QName( gmlNs, "definitionMember" ).equals( elName ) ) {
                xmlStream.nextTag();
                members.add( read() );
                xmlStream.nextTag();
            } else if ( new QName( gmlNs, "indirectEntry" ).equals( elName ) ) {
                String msg = "Handling of 'indirectEntry' is not implemented yet.";
                throw new XMLStreamException( msg );
            } else {
                String msg = "Unexpected element '" + elName + "'.";
                throw new XMLStreamException( msg );
            }
            xmlStream.nextTag();
        }

        StAXParsingHelper.require( xmlStream, END_ELEMENT );
        Dictionary def = new GenericDictionary( id, standardProps, members, isDefinitionCollection );
        idContext.addObject( def );
        return def;
    }
}
