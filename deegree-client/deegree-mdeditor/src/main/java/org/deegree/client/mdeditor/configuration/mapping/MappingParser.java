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
package org.deegree.client.mdeditor.configuration.mapping;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.Parser;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
import org.deegree.commons.xml.XMLParsingException;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MappingParser extends Parser {

    private static final Logger LOG = getLogger( MappingParser.class );

    private static QName ROOT = new QName( NS, "GuiSchemaMapping" );

    private static QName SCHEMA = new QName( NS, "Schema" );

    private static QName MAPPING = new QName( NS, "Mapping" );

    private static QName MAPPING_ELEMENT = new QName( NS, "MappingElement" );

    public static MappingInformation parseMapping( URL mappingURL )
                            throws ConfigurationException {
        MappingInformation mappingInformation = null;
        try {
            File f = new File( mappingURL.toURI() );
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( f ) );

            if ( xmlStream.getEventType() == START_DOCUMENT ) {
                xmlStream.nextTag();
            }
            xmlStream.require( START_ELEMENT, NS, ROOT.getLocalPart() );
            xmlStream.nextTag();
            if ( xmlStream.getEventType() != START_ELEMENT ) {
                throw new XMLParsingException( xmlStream, "Empty GuiSchemaMapping" );
            }
            while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( ROOT ) ) ) {
                if ( SCHEMA.equals( xmlStream.getName() ) ) {
                    mappingInformation = parseSchemaInformation( xmlStream );
                    LOG.debug( "parsed mapping information: " + mappingInformation.toString() );
                } else if ( MAPPING.equals( xmlStream.getName() ) ) {
                    parseMapping( xmlStream, mappingInformation );
                }
            }
            xmlStream.require( END_ELEMENT, NS, ROOT.getLocalPart() );

        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( URISyntaxException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mappingInformation;
    }

    private static MappingInformation parseSchemaInformation( XMLStreamReader xmlStream )
                            throws XMLStreamException, ConfigurationException {
        xmlStream.nextTag();
        String name = null;
        String version = null;
        String describtion = null;
        String schema = null;
        xmlStream.require( START_ELEMENT, NS, "id" );
        String id = getElementText( xmlStream, "id", null );

        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( SCHEMA ) ) ) {
            String elementName = xmlStream.getLocalName();
            if ( "name".equals( elementName ) ) {
                name = getElementText( xmlStream, "name", null );
            } else if ( "version".equals( elementName ) ) {
                version = getElementText( xmlStream, "version", null );
            } else if ( "describtion".equals( elementName ) ) {
                describtion = getElementText( xmlStream, "describtion", null );
            } else if ( "schema".equals( elementName ) ) {
                schema = getElementText( xmlStream, "schema", null );
            } else {
                xmlStream.nextTag();
            }
        }
        if ( schema == null || schema.length() < 1 ) {
            throw new ConfigurationException( "Schema URL of the mapping with id " + id + " is invalid: " + schema );
        }
        xmlStream.nextTag();
        return new MappingInformation( id, name, version, describtion, schema );
    }

    private static void parseMapping( XMLStreamReader xmlStream, MappingInformation mappingInformation )
                            throws XMLStreamException, ConfigurationException {
        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( MAPPING ) ) ) {
            if ( "MappingElement".equals( xmlStream.getLocalName() ) ) {
                String fieldPath = null;
                String schemaPath = null;
                while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( MAPPING_ELEMENT ) ) ) {
                    if ( "formFieldPath".equals( xmlStream.getLocalName() ) ) {
                        fieldPath = getElementText( xmlStream, "formFieldPath", null );
                    } else if ( "schemaPath".equals( xmlStream.getLocalName() ) ) {
                        schemaPath = getElementText( xmlStream, "schemaPath", null );
                    } else {
                        xmlStream.nextTag();
                    }
                }
                xmlStream.nextTag();
                if ( fieldPath != null && schemaPath != null ) {
                    LOG.debug( "found mapping element: " + fieldPath + " - " + schemaPath );
                    mappingInformation.addMappingElement( fieldPath, schemaPath );
                } else {
                    LOG.info( " Found invalid mapping element with formFieldPath " + fieldPath + " and schemaPath "
                              + schemaPath + " ignore this element." );
                }
            } else if ( "template".equals( xmlStream.getLocalName() ) ) {
                mappingInformation.setTemplate( getElementText( xmlStream, "template", null ) );
            } else {
                xmlStream.nextTag();
            }
        }
        xmlStream.nextTag();
    }
}
