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
package org.deegree.client.csw;

import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static javax.xml.stream.XMLStreamConstants.*;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WebXmlInjector {
    private static Logger LOG = getLogger( WebXmlInjector.class );

    private static final String CLASSPATH_ROOT = WebXmlInjector.class.getResource( "/" ).getPath();

    private static final String PROJECT_ROOT = new File( new File( CLASSPATH_ROOT ).getParent() ).getParent();

    private static final String WEB_XML_PATH = PROJECT_ROOT + "/src/main/webapp/WEB-INF/web.xml";

    private static final String NEW_WEB_XML_PATH = PROJECT_ROOT + "/src/main/webapp/WEB-INF/newweb.xml";

    /**
     * @param args
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     */
    public static void main( String[] args )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        File webxml = new File( WEB_XML_PATH );

        File newwebxml = new File( NEW_WEB_XML_PATH );
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( new FileWriter( newwebxml ) );
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( webxml ) );

        int openElements = 0;
        boolean firstRun = true;
        while ( firstRun || openElements > 0 ) {
            firstRun = false;
            int eventType = reader.getEventType();

            switch ( eventType ) {
            case START_DOCUMENT: {
                writer.writeStartDocument();
                openElements++;
                break;
            }
            case END_DOCUMENT: {
                writer.writeEndDocument();
                openElements--;
                break;
            }
            case CDATA: {
                writer.writeCData( reader.getText() );
                break;
            }
            case CHARACTERS: {
                writer.writeCharacters( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength() );
                break;
            }
            case END_ELEMENT: {

                writer.writeEndElement();
                openElements--;
                break;
            }
            case START_ELEMENT: {
                QName elName = reader.getName();
                if ( "welcome-file-list".equals( elName.getLocalPart() ) ) {
                    //skip it
                    openElements--;
                } else if ( "welcome-file".equals( elName.getLocalPart() ) ) {
                    //skip that elements and replace just one new
                    writer.writeStartElement( "welcome-file-list" );
                    writer.writeStartElement( "welcome-file" );
                    writer.writeCharacters( "console.xhtml" );
                    writer.writeEndElement();
                    writer.writeEndElement();
                    writer.writeEndDocument();
                    openElements--;
                } else {

                    writer.writeStartElement( reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI() );
                    // copy all namespace bindings
                    for ( int i = 0; i < reader.getNamespaceCount(); i++ ) {
                        String nsPrefix = reader.getNamespacePrefix( i );
                        String nsURI = reader.getNamespaceURI( i );
                        writer.writeNamespace( nsPrefix, nsURI );
                    }

                    // copy all attributes
                    for ( int i = 0; i < reader.getAttributeCount(); i++ ) {
                        String localName = reader.getAttributeLocalName( i );
                        String nsPrefix = reader.getAttributePrefix( i );
                        String value = reader.getAttributeValue( i );
                        String nsURI = reader.getAttributeNamespace( i );
                        if ( nsURI == null ) {
                            writer.writeAttribute( localName, value );
                        } else {
                            writer.writeAttribute( nsPrefix, nsURI, localName, value );
                        }
                    }

                    openElements++;
                }
                break;
            }
            default: {
                break;
            }
            }
            if ( openElements > 0 ) {
                reader.next();
            }
        }

        reader.close();
        writer.close();

    }

}
