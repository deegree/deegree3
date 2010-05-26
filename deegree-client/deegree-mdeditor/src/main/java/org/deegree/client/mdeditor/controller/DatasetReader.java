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
package org.deegree.client.mdeditor.controller;

import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.config.Configuration;
import org.deegree.commons.xml.XMLParsingException;
import org.slf4j.Logger;

/**
 * 
 * reading a dataset or single form group
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DatasetReader {

    private static final Logger LOG = getLogger( DatasetReader.class );

    /**
     * @param id
     *            the id of the dataset to read
     * @return the dataset with the given id
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws FactoryConfigurationError
     */
    public static Map<String, Object> readDataset( String id )
                            throws XMLStreamException, FileNotFoundException, FactoryConfigurationError {
        String file = Configuration.getFilesDirURL() + id + ".xml";
        return read( file );
    }

    /**
     * @param file
     *            the file to read as complete path
     * @return the values of the form group stored in the given file
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws FactoryConfigurationError
     */
    public static Map<String, Object> read( String file )
                            throws XMLStreamException, FileNotFoundException, FactoryConfigurationError {
        LOG.debug( "Read dataset form " + file );
        return read( XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( file ) ) );
    }

    /**
     * 
     * @param file
     *            the file to read
     * @return the values of the form group stored in the given file
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws FactoryConfigurationError
     */
    public static Map<String, Object> read( File file )
                            throws XMLStreamException, FileNotFoundException, FactoryConfigurationError {
        LOG.debug( "Read dataset from file " + file.getAbsolutePath() );
        return read( XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( file ) ) );
    }

    private static Map<String, Object> read( XMLStreamReader xmlStream )
                            throws XMLStreamException, FileNotFoundException, FactoryConfigurationError {
        Map<String, Object> result = new HashMap<String, Object>();

        if ( xmlStream.getEventType() == START_DOCUMENT ) {
            xmlStream.nextTag();
        }

        xmlStream.require( START_ELEMENT, null, "Dataset" );
        xmlStream.nextTag();
        if ( xmlStream.getEventType() != START_ELEMENT ) {
            throw new XMLParsingException( xmlStream, "Empty dataset" );
        }

        while ( !( xmlStream.isEndElement() && "Dataset".equals( xmlStream.getLocalName() ) ) ) {
            if ( "Element".equals( xmlStream.getLocalName() ) ) {
                xmlStream.nextTag();

                xmlStream.require( START_ELEMENT, null, "id" );
                String path = xmlStream.getElementText();
                xmlStream.nextTag();

                if ( path == null ) {
                    LOG.info( "found element without, continue reading" );
                } else {
                    List<String> values = new ArrayList<String>();

                    while ( xmlStream.isStartElement() && "value".equals( xmlStream.getLocalName() ) ) {
                        values.add( xmlStream.getElementText() );
                        xmlStream.nextTag();
                    }

                    Object value = null;
                    if ( values.size() > 0 ) {
                        value = values.size() > 1 ? values : values.get( 0 );
                        result.put( path, value );
                    }
                }
            }
            xmlStream.nextTag();
        }

        LOG.debug( "Found " + result.size() + " elements" );
        return result;
    }

}
