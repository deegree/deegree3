//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.tools.migration;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.services.controller.WebServicesConfiguration;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.theme.persistence.ThemeManager;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ThemeExtractor {

    private final DeegreeWorkspace workspace;

    private Transformer transformer;

    public ThemeExtractor( DeegreeWorkspace workspace ) throws TransformerConfigurationException {
        this.workspace = workspace;
        TransformerFactory fac = TransformerFactory.newInstance();
        InputStream xsl = ThemeExtractor.class.getResourceAsStream( "extracttheme.xsl" );
        this.transformer = fac.newTransformer( new StreamSource( xsl ) );
    }

    public void transform()
                            throws TransformerException, XMLStreamException {
        XMLInputFactory infac = XMLInputFactory.newInstance();
        XMLOutputFactory outfac = XMLOutputFactory.newInstance();

        WebServicesConfiguration mgr = workspace.getSubsystemManager( WebServicesConfiguration.class );
        ThemeManager tmgr = workspace.getSubsystemManager( ThemeManager.class );
        ResourceState<?>[] states = mgr.getStates();
        for ( ResourceState<?> s : states ) {
            if ( s.getResource() instanceof WMSController ) {
                File loc = s.getConfigLocation();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                transformer.transform( new StreamSource( loc ), new StreamResult( bos ) );

                ByteArrayInputStream bin = new ByteArrayInputStream( bos.toByteArray() );
                XMLStreamReader reader = infac.createXMLStreamReader( bin );
                XMLStreamWriter writer = new IndentingXMLStreamWriter(
                                                                       outfac.createXMLStreamWriter( bos = new ByteArrayOutputStream() ) );
                copy( reader, writer );
                reader.close();
                writer.close();

                tmgr.createResource( s.getId(), new ByteArrayInputStream( bos.toByteArray() ) );
                tmgr.activate( s.getId() );
            }
        }
    }

    private static void copy( XMLStreamReader inStream, XMLStreamWriter writer )
                            throws XMLStreamException {
        int layerCount = 0;
        int openElements = 0;
        boolean firstRun = true;
        while ( firstRun || openElements > 0 ) {
            firstRun = false;
            int eventType = inStream.getEventType();

            switch ( eventType ) {
            case START_DOCUMENT: {
                writer.writeStartDocument();
                inStream.next();
                firstRun = true;
                continue;
            }
            case CDATA: {
                writer.writeCData( inStream.getText() );
                break;
            }
            case CHARACTERS: {
                String str = new String( inStream.getTextCharacters(), inStream.getTextStart(),
                                         inStream.getTextLength() );
                if ( str.equals( "LogicalLayer" ) ) {
                    str += "_" + ++layerCount;
                }
                writer.writeCharacters( str );
                break;
            }
            case END_ELEMENT: {
                writer.writeEndElement();
                openElements--;
                break;
            }
            case START_ELEMENT: {
                String prefix = inStream.getPrefix();
                String namespaceURI = inStream.getNamespaceURI();
                if ( namespaceURI == NULL_NS_URI && ( prefix == DEFAULT_NS_PREFIX || prefix == null ) ) {
                    writer.writeStartElement( inStream.getLocalName() );
                } else {
                    if ( prefix != null && writer.getNamespaceContext().getPrefix( prefix ) == XMLConstants.NULL_NS_URI ) {
                        // TODO handle special cases for prefix binding, see
                        // http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
                        writer.setPrefix( prefix, namespaceURI );
                    }
                    if ( prefix == null ) {
                        writer.writeStartElement( XMLConstants.NULL_NS_URI, inStream.getLocalName(), namespaceURI );
                    } else {
                        writer.writeStartElement( prefix, inStream.getLocalName(), namespaceURI );
                    }
                }
                // copy all namespace bindings
                for ( int i = 0; i < inStream.getNamespaceCount(); i++ ) {
                    String nsPrefix = inStream.getNamespacePrefix( i );
                    String nsURI = inStream.getNamespaceURI( i );
                    if ( nsPrefix != null && nsURI != null ) {
                        writer.writeNamespace( nsPrefix, nsURI );
                    } else if ( nsPrefix == null ) {
                        writer.writeDefaultNamespace( nsURI );
                    }
                }

                // copy all attributes
                for ( int i = 0; i < inStream.getAttributeCount(); i++ ) {
                    String localName = inStream.getAttributeLocalName( i );
                    String nsPrefix = inStream.getAttributePrefix( i );
                    String value = inStream.getAttributeValue( i );
                    String nsURI = inStream.getAttributeNamespace( i );
                    if ( nsURI == null ) {
                        writer.writeAttribute( localName, value );
                    } else {
                        writer.writeAttribute( nsPrefix, nsURI, localName, value );
                    }
                }

                openElements++;
                break;
            }
            default: {
                break;
            }
            }
            if ( openElements > 0 ) {
                inStream.next();
            }
        }
    }

}
