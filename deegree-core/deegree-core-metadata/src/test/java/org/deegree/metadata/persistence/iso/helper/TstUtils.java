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
package org.deegree.metadata.persistence.iso.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.iso.persistence.ISOMetadataStore;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TstUtils {

    private static Logger LOG = LoggerFactory.getLogger( TstUtils.class );

    public static List<String> insertMetadata( ISOMetadataStore store, URL... URLInput )
                            throws MetadataStoreException, MetadataInspectorException {

        List<ISORecord> records = null;
        InsertOperation insert = null;
        MetadataStoreTransaction ta = null;

        List<String> ids = new ArrayList<String>();
        int countInserted = 0;
        int countInsert = 0;
        countInsert = URLInput.length;

        for ( URL file : URLInput ) {
            records = new ArrayList<ISORecord>();
            ta = store.acquireTransaction();
            OMElement record = new XMLAdapter( file ).getRootElement();
            LOG.info( "inserting filename: " + file.getFile() );
            records.add( new ISORecord( record ) );
            try {
                if ( countInsert > 0 ) {
                    insert = new InsertOperation( records, records.get( 0 ).getAsOMElement().getQName(), "insert" );
                    ids.addAll( ta.performInsert( insert ) );
                    ta.commit();
                }
            } catch ( MetadataStoreException e ) {
                String msg = "Error while commit the statement!";
                if ( ta != null ) {
                    ta.rollback();
                    LOG.info( msg );
                    e.printStackTrace();
                    // throw new MetadataInspectorException();
                }
            } catch ( MetadataInspectorException e ) {
                String msg = "Error while insert/inspect metadataRecord!";
                if ( ta != null ) {
                    ta.rollback();
                    LOG.info( msg );
                    throw new MetadataInspectorException();
                }
            }
        }

        if ( !ids.isEmpty() ) {
            countInserted += ids.size();
        }

        LOG.info( countInserted + " from " + countInsert + " Metadata inserted." );
        return ids;
    }

    public static XMLStreamReader readXMLStream( String fileString )
                            throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( new FileInputStream(
                                                                                                              new File(
                                                                                                                        fileString ) ) );
        StAXParsingHelper.skipStartDocument( xmlStream );
        return xmlStream;
    }

    public static StringBuilder stringBuilderFromResultSet( MetadataResultSet resultSet,
                                                            ReturnableElement returnableElement, String file,
                                                            int searchEvent )
                            throws XMLStreamException, FileNotFoundException, MetadataStoreException {
        OutputStream fout = null;
        if ( file == null ) {
            fout = new ByteArrayOutputStream();
        } else {
            fout = new FileOutputStream( file );
        }

        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( fout );

        while ( resultSet.next() ) {
            resultSet.getRecord().serialize( writer, returnableElement );
        }
        writer.flush();

        StringBuilder streamThat = new StringBuilder();
        if ( fout instanceof FileOutputStream ) {
            LOG.warn( "The output is written into a file: " + file );
            return null;
        } else if ( fout instanceof ByteArrayOutputStream ) {
            InputStream in = new ByteArrayInputStream( ( (ByteArrayOutputStream) fout ).toByteArray() );
            XMLStreamReader xmlStreamThat = XMLInputFactory.newInstance().createXMLStreamReader( in );
            // xmlStreamThat.nextTag();
            while ( xmlStreamThat.hasNext() ) {
                xmlStreamThat.next();

                if ( xmlStreamThat.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                    if ( searchEvent == XMLStreamConstants.START_ELEMENT ) {
                        streamThat.append( xmlStreamThat.getName() ).append( ' ' );
                    } else if ( searchEvent == XMLStreamConstants.NAMESPACE ) {
                        // copy all namespace bindings
                        for ( int i = 0; i < xmlStreamThat.getNamespaceCount(); i++ ) {
                            String nsPrefix = xmlStreamThat.getNamespacePrefix( i );
                            String nsURI = xmlStreamThat.getNamespaceURI( i );
                            streamThat.append( nsPrefix ).append( '=' ).append( nsURI ).append( ' ' );
                        }
                    }
                }
            }
        }

        return streamThat;
    }

    public static StringBuilder stringBuilderFromXMLStream( XMLStreamReader xmlStreamThis )
                            throws XMLStreamException {
        StringBuilder streamThis = new StringBuilder();
        while ( xmlStreamThis.hasNext() ) {
            xmlStreamThis.next();
            if ( xmlStreamThis.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                streamThis.append( xmlStreamThis.getName() ).append( ' ' );
            }
        }

        return streamThis;
    }

}
