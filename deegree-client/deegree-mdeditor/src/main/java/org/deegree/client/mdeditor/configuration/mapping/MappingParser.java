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

import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.Parser;
import org.deegree.client.mdeditor.model.mapping.MappingElement;
import org.deegree.client.mdeditor.model.mapping.MappingGroup;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
import org.deegree.commons.xml.NamespaceContext;
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

    private static QName MAPPING_GROUP = new QName( NS, "MappingGroup" );

    private static QName MAPPING_ELEMENT = new QName( NS, "MappingElement" );

    private static QName id = new QName( NS, "id" );

    private static QName name = new QName( NS, "name" );

    private static QName version = new QName( NS, "version" );

    private static QName describtion = new QName( NS, "describtion" );

    private static QName schema = new QName( NS, "schema" );

    private static QName formFieldPath = new QName( NS, "formFieldPath" );

    private static QName schemaPath = new QName( NS, "schemaPath" );

    private static QName namespaceDef = new QName( NS, "NamespaceDefinitions" );

    private static QName namespace = new QName( NS, "Namespace" );

    private static QName pref = new QName( NS, "prefix" );

    private static QName ns = new QName( NS, "namespace" );

    public static MappingInformation parseMapping( URL mappingURL )
                            throws ConfigurationException {
        MappingInformation mappingInformation = null;
        try {
            File f = new File( mappingURL.toURI() );
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( f ) );

            if ( !moveReaderToFirstMatch( xmlStream, ROOT ) ) {
                throw new ConfigurationException( "could not parse mapping" + f.getAbsolutePath()
                                                  + ": root element does not exist" );
            }

            while ( !( xmlStream.isEndElement() && ROOT.equals( xmlStream.getName() ) ) ) {
                if ( SCHEMA.equals( xmlStream.getName() ) ) {
                    mappingInformation = parseSchemaInformation( xmlStream );
                    LOG.debug( "parsed mapping information: " + mappingInformation.toString() );
                } else if ( MAPPING.equals( xmlStream.getName() ) ) {
                    parseMapping( xmlStream, mappingInformation );
                } else {
                    nextElement( xmlStream );
                }
            }
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
        nextElement( xmlStream );
        String nameValue = null;
        String versionValue = null;
        String describtionValue = null;
        String schemaValue = null;

        String idValue = getRequiredText( xmlStream, id, true );
        while ( !( xmlStream.isEndElement() && SCHEMA.equals( xmlStream.getName() ) ) ) {
            QName elementName = xmlStream.getName();
            if ( name.equals( elementName ) ) {
                nameValue = getText( xmlStream, name, null, true );
            } else if ( version.equals( elementName ) ) {
                versionValue = getText( xmlStream, version, null, true );
            } else if ( describtion.equals( elementName ) ) {
                describtionValue = getText( xmlStream, describtion, null, true );
            } else if ( schema.equals( elementName ) ) {
                schemaValue = getText( xmlStream, schema, null, true );
            } else {
                nextElement( xmlStream );
            }
        }
        if ( schemaValue == null || schemaValue.length() < 1 ) {
            throw new ConfigurationException( "Schema URL of the mapping with id " + id + " is invalid: " + schema );
        }
        nextElement( xmlStream );
        return new MappingInformation( idValue, nameValue, versionValue, describtionValue, schemaValue );
    }

    private static void parseMapping( XMLStreamReader xmlStream, MappingInformation mappingInformation )
                            throws XMLStreamException, ConfigurationException {
        nextElement( xmlStream );
        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( MAPPING ) ) ) {
            if ( namespaceDef.equals( xmlStream.getName() ) ) {
                mappingInformation.setNsContext( parseNamespaceDefinitions( xmlStream ) );
            } else if ( MAPPING_ELEMENT.equals( xmlStream.getName() ) ) {
                mappingInformation.addMappingElement( parseMappingElement( xmlStream ) );
            } else if ( MAPPING_GROUP.equals( xmlStream.getName() ) ) {
                nextElement( xmlStream );
                String fPath = getText( xmlStream, formFieldPath, null, true );
                String sPath = getText( xmlStream, schemaPath, null, true );
                List<MappingElement> mappings = new ArrayList<MappingElement>();
                while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( MAPPING_GROUP ) ) ) {
                    if ( MAPPING_ELEMENT.equals( xmlStream.getName() ) ) {
                        mappings.add( parseMappingElement( xmlStream ) );
                    }
                }
                if ( fPath != null && sPath != null ) {
                    fPath = fPath.trim();
                    sPath = sPath.trim();
                    LOG.debug( "found mapping group: " + fPath + " - " + sPath );
                    MappingElement mapping = new MappingGroup( fPath, sPath, mappings );
                    mappingInformation.addMappingElement( mapping );
                } else {
                    LOG.info( " Found invalid mapping group with formFieldPath " + fPath + " and schemaPath " + sPath
                              + " ignore this element." );
                }
                nextElement( xmlStream );
            }
        }
        nextElement( xmlStream );
    }

    private static MappingElement parseMappingElement( XMLStreamReader xmlStream )
                            throws XMLStreamException, ConfigurationException {
        MappingElement mappingSingle = null;
        nextElement( xmlStream );

        String fPath = getText( xmlStream, formFieldPath, null, true );
        String sPath = getText( xmlStream, schemaPath, null, true );

        if ( fPath != null && sPath != null ) {
            fPath = fPath.trim();
            sPath = sPath.trim();
            LOG.debug( "found mapping element: " + fPath + " - " + sPath );
            mappingSingle = new MappingElement( fPath, sPath );
        } else {
            LOG.info( " Found invalid mapping element with formFieldPath " + fPath + " and schemaPath " + sPath
                      + " ignore this element." );
        }
        nextElement( xmlStream );
        return mappingSingle;
    }

    private static NamespaceContext parseNamespaceDefinitions( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        NamespaceContext nsContext = new NamespaceContext();
        nextElement( xmlStream );

        while ( !( xmlStream.isEndElement() && namespaceDef.equals( xmlStream.getName() ) ) ) {
            if ( namespace.equals( xmlStream.getName() ) ) {
                nextElement( xmlStream );
                String prefix = getText( xmlStream, pref, null, true );
                String namespaceUrl = getText( xmlStream, ns, null, true );
                if ( prefix != null && namespaceUrl != null ) {
                    nsContext.addNamespace( prefix, namespaceUrl );
                }
                nextElement( xmlStream );
            } else {
                nextElement( xmlStream );
            }
        }
        nextElement( xmlStream );
        return nsContext;
    }
}
