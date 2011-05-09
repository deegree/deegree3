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
package org.deegree.metadata.iso.persistence;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.csw.MetadataStoreException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ISORecordSerializeTest extends AbstractISOTest {

    private static final Logger LOG = getLogger( ISORecordSerializeTest.class );

    @Test
    public void testNamespaces()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException, ResourceInitException {
        LOG.info( "START Test: testNamespaces" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }

        List<String> ids = TstUtils.insertMetadata( store, TstConstants.tst_12 );
        resultSet = store.getRecordById( ids );

        // create the is output
        // String file = "/home/thomas/Desktop/zTestBrief.xml";
        String file = null;
        StringBuilder streamActual = stringBuilderFromResultSet( resultSet, ReturnableElement.brief, file,
                                                                 XMLStreamConstants.NAMESPACE );
        if ( streamActual == null ) {
            return;
        }
        StringBuilder streamExpected = new StringBuilder();
        streamExpected.append( "=http://www.isotc211.org/2005/gmd" ).append( ' ' );
        streamExpected.append( "gmd=http://www.isotc211.org/2005/gmd" ).append( ' ' );
        streamExpected.append( "gco=http://www.isotc211.org/2005/gco" ).append( ' ' );
        streamExpected.append( "srv=http://www.isotc211.org/2005/srv" ).append( ' ' );
        streamExpected.append( "gml=http://www.opengis.net/gml" ).append( ' ' );
        streamExpected.append( "gts=http://www.isotc211.org/2005/gts" ).append( ' ' );
        streamExpected.append( "xsi=http://www.w3.org/2001/XMLSchema-instance" ).append( ' ' );

        LOG.info( "streamThis: " + streamExpected.toString() );
        LOG.info( "streamThat: " + streamActual.toString() );
        System.out.println( streamActual );
        Assert.assertEquals( streamExpected.toString(), streamActual.toString() );

    }

    /**
     * Tests if the output is in summary representation
     * <p>
     * Output should be 1
     * 
     * @throws MetadataStoreException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws IOException
     * @throws MetadataInspectorException
     * @throws ResourceInitException
     */
    @Test
    public void testOutputBrief()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException, ResourceInitException {
        LOG.info( "START Test: is output ISO brief? " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        List<String> ids = TstUtils.insertMetadata( store, TstConstants.fullRecord );
        resultSet = store.getRecordById( ids );

        XMLStreamReader xmlStreamActual = XMLInputFactory.newInstance().createXMLStreamReader( TstConstants.briefRecord.openStream() );

        // create the should be output
        StringBuilder streamActual = stringBuilderFromXMLStream( xmlStreamActual );

        // create the is output
        // String file = "/home/thomas/Desktop/zTestBrief.xml";
        String file = null;
        StringBuilder streamExpected = stringBuilderFromResultSet( resultSet, ReturnableElement.brief, file,
                                                                   XMLStreamConstants.START_ELEMENT );
        if ( streamExpected == null ) {
            return;
        }
        LOG.info( "streamThis: " + streamActual.toString() );
        LOG.info( "streamThat: " + streamExpected.toString() );
        Assert.assertEquals( streamActual.toString(), streamExpected.toString() );

    }

    /**
     * Tests if the output is in summary representation
     * <p>
     * Output should be 1
     * 
     * @throws MetadataStoreException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     * @throws IOException
     * @throws MetadataInspectorException
     * @throws ResourceInitException
     */

    @Test
    public void testOutputSummary()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException, ResourceInitException {
        LOG.info( "START Test: is output ISO summary? " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        List<String> ids = TstUtils.insertMetadata( store, TstConstants.fullRecord );
        resultSet = store.getRecordById( ids );

        XMLStreamReader xmlStreamActual = XMLInputFactory.newInstance().createXMLStreamReader( TstConstants.summaryRecord.openStream() );

        // create the should be output
        StringBuilder streamActual = stringBuilderFromXMLStream( xmlStreamActual );

        // create the is output
        // String file = "/home/thomas/Desktop/zTestSummary.xml";
        String file = null;
        StringBuilder streamExpected = stringBuilderFromResultSet( resultSet, ReturnableElement.summary, file,
                                                                   XMLStreamConstants.START_ELEMENT );
        if ( streamExpected == null ) {
            return;
        }
        LOG.debug( "streamThis: " + streamActual.toString() );
        LOG.debug( "streamThat: " + streamExpected.toString() );
        Assert.assertEquals( streamActual.toString(), streamExpected.toString() );

    }

    private static StringBuilder stringBuilderFromResultSet( MetadataResultSet<?> resultSet,
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

    private static StringBuilder stringBuilderFromXMLStream( XMLStreamReader xmlStreamThis )
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
