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
package org.deegree.client.mdeditor.configuration;

import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredAttributeValue;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.model.FormConfigurationDescription;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConfigurationParser extends Parser {

    private static final Logger LOG = getLogger( ConfigurationParser.class );

    private static QName ROOT = new QName( NS, "MDEditorConfiguration" );

    private static QName DATADIR = new QName( NS, "dataDirectory" );

    private static QName EXPORTDIR = new QName( NS, "exportDirectory" );

    private static QName CONFIGURATIONS = new QName( NS, "FormConfigurations" );

    private static QName GLOBAL = new QName( NS, "GlobalConfigurations" );

    private static QName CODELISTS = new QName( NS, "CodeLists" );

    public static Configuration parseConfiguration( File configuration )
                            throws ConfigurationException {
        LOG.debug( "parse configuration from " + configuration.getPath() );
        try {
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                             configuration.getPath(),
                                                                                             new FileInputStream(
                                                                                                                  configuration ) );

            if ( !moveReaderToFirstMatch( xmlStream, ROOT ) ) {
                throw new ConfigurationException( "could not parse configuration" + xmlStream.getLocation()
                                                  + ": root element does not exist" );
            }
            URL dataDirUrl = null;
            URL exportDirUrl = null;
            List<FormConfigurationDescription> formConfigurations = new ArrayList<FormConfigurationDescription>();
            List<FormConfigurationDescription> globalConfigurations = new ArrayList<FormConfigurationDescription>();
            List<URL> codeLists = new ArrayList<URL>();
            while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( ROOT ) ) ) {
                QName elementName = xmlStream.getName();
                if ( DATADIR.equals( elementName ) ) {
                    String dataDir = null;
                    try {
                        dataDir = getText( xmlStream, DATADIR, null, true );
                        dataDirUrl = resolve( dataDir, xmlStream );
                    } catch ( MalformedURLException e ) {
                        LOG.debug( "dataDirectory " + dataDir + " is not a valid url!", e );
                        throw new ConfigurationException( "could not parse configuration" + xmlStream.getLocation()
                                                          + ": dataDirectory " + dataDir + " is not a valid url!" );
                    }
                } else if ( EXPORTDIR.equals( elementName ) ) {
                    String exportDir = null;
                    try {
                        exportDir = getText( xmlStream, EXPORTDIR, null, true );
                        exportDirUrl = resolve( exportDir, xmlStream );
                    } catch ( MalformedURLException e ) {
                        LOG.debug( "exportDirectory " + exportDir + " is not a valid url!", e );
                        throw new ConfigurationException( "could not parse configuration" + xmlStream.getLocation()
                                                          + ": exportDirectory " + exportDir + " is not a valid url!" );
                    }
                } else if ( CONFIGURATIONS.equals( elementName ) ) {
                    formConfigurations = parseConfigurations( xmlStream, CONFIGURATIONS );
                } else if ( GLOBAL.equals( elementName ) ) {
                    globalConfigurations = parseConfigurations( xmlStream, GLOBAL );
                } else if ( CODELISTS.equals( elementName ) ) {
                    codeLists = parseCodeLists( xmlStream );
                } else {
                    nextElement( xmlStream );
                }
            }
            return new Configuration( dataDirUrl, exportDirUrl, formConfigurations, globalConfigurations, codeLists );
        } catch ( Exception e ) {
            LOG.debug( "could not parse configuration " + configuration, e );
            throw new ConfigurationException( "could not parse configuration " + configuration + ": " + e.getMessage() );
        }
    }

    private static List<URL> parseCodeLists( XMLStreamReader xmlStream )
                            throws NoSuchElementException, XMLStreamException, ConfigurationException {
        List<URL> codeLists = new ArrayList<URL>();
        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( CODELISTS ) ) ) {
            QName cl = new QName( NS, "codeList" );
            if ( cl.equals( xmlStream.getName() ) && xmlStream.isStartElement() ) {
                String codelist = null;
                try {
                    codelist = getRequiredAttributeValue( xmlStream, "href" );
                    codeLists.add( resolve( codelist, xmlStream ) );
                    nextElement( xmlStream );
                } catch ( MalformedURLException e ) {
                    LOG.debug( "codeList " + codelist + " is not a valid url!", e );
                    throw new ConfigurationException( "codeList " + codelist + " is not a valid url!" );
                }
            } else {
                nextElement( xmlStream );
            }
        }
        nextElement( xmlStream );
        return codeLists;
    }

    private static List<FormConfigurationDescription> parseConfigurations( XMLStreamReader xmlStream, QName endElement )
                            throws NoSuchElementException, XMLStreamException, ConfigurationException {
        nextElement( xmlStream );
        List<FormConfigurationDescription> confs = new ArrayList<FormConfigurationDescription>();
        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( endElement ) ) ) {
            if ( xmlStream.isStartElement() && new QName( NS, "Configuration" ).equals( xmlStream.getName() ) ) {
                String url = getRequiredAttributeValue( xmlStream, "href" );
                String id = getRequiredAttributeValue( xmlStream, "id" );
                try {
                    URL confUrl = resolve( url, xmlStream );
                    nextElement( xmlStream );
                    String title = getText( xmlStream, new QName( NS, "title" ), null, true );
                    String description = getText( xmlStream, new QName( NS, "description" ), null, true );
                    confs.add( new FormConfigurationDescription( id, confUrl, title, description ) );
                    nextElement( xmlStream );
                } catch ( MalformedURLException e ) {
                    LOG.debug( "Configuration " + url + " contains an invalid url!", e );
                    throw new ConfigurationException( "could not parse configuration" + xmlStream.getLocation()
                                                      + ": Configuration " + url + " contains an invalid url!" );
                }
            } else {
                nextElement( xmlStream );
            }
        }
        nextElement( xmlStream );
        return confs;
    }
}
