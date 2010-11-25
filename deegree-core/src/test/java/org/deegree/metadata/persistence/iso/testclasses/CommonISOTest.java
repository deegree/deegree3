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
package org.deegree.metadata.persistence.iso.testclasses;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.iso.ISOMetadataStore;
import org.deegree.metadata.persistence.iso.ISOMetadataStoreProvider;
import org.deegree.metadata.persistence.iso.helper.AbstractISOTest;
import org.deegree.metadata.persistence.iso.helper.TstConstants;
import org.deegree.metadata.persistence.iso.helper.TstUtils;
import org.deegree.metadata.publication.DeleteTransaction;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.junit.Assert;
import org.junit.Test;
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
public class CommonISOTest extends AbstractISOTest {
    private static Logger LOG = LoggerFactory.getLogger( CommonISOTest.class );

    @Test
    public void testInsert()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: testInsert" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        String test_folder = "/home/thomas/Dokumente/metadata/testCases/";
        // String test_folder = "/home/thomas/Dokumente/metadata/test/";// CoreTstProperties.getProperty(
        // "iso_metadata_insert_test_folder"
        // );
        if ( test_folder == null ) {
            LOG.warn( "Skipping test (no testCase folder found)" );
            return;
        }

        File folder = new File( test_folder );
        File[] fileArray = folder.listFiles();
        LOG.info( "" + fileArray.length );
        URL[] urlArray = null;
        if ( fileArray != null ) {
            urlArray = new URL[fileArray.length];
            int counter = 0;
            for ( File f : fileArray ) {
                urlArray[counter++] = new URL( "file:" + f.getAbsolutePath() );

            }

        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = TstUtils.insertMetadata( store, ta, urlArray );

        MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 1 );
        resultSet = store.getRecords( query );
        int size = 0;
        while ( resultSet.next() ) {
            size++;
        }

        Assert.assertEquals( 2, size );

        // TODO test various queries

    }

    @Test
    public void testInsertMetametadata()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: testInsertMetametadata" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        store.setupMetametadata();
        MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 1 );
        resultSet = store.getRecords( query );
        int size = 0;
        while ( resultSet.next() ) {
            size++;
        }
        Assert.assertEquals( 1, size );

    }

    @Test
    public void testNamespaces()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: testNamespaces" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }

        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = TstUtils.insertMetadata( store, ta, TstConstants.tst_12 );
        resultSet = store.getRecordById( ids );

        // create the is output
        // String file = "/home/thomas/Desktop/zTestBrief.xml";
        String file = null;
        StringBuilder streamActual = TstUtils.stringBuilderFromResultSet( resultSet, ReturnableElement.brief, file,
                                                                          XMLStreamConstants.NAMESPACE );
        if ( streamActual == null ) {
            return;
        }
        StringBuilder streamExpected = new StringBuilder();
        streamExpected.append( "null=http://www.isotc211.org/2005/gmd" ).append( ' ' );
        streamExpected.append( "gmd=http://www.isotc211.org/2005/gmd" ).append( ' ' );
        streamExpected.append( "gco=http://www.isotc211.org/2005/gco" ).append( ' ' );
        streamExpected.append( "srv=http://www.isotc211.org/2005/srv" ).append( ' ' );
        streamExpected.append( "gml=http://www.opengis.net/gml" ).append( ' ' );
        streamExpected.append( "gts=http://www.isotc211.org/2005/gts" ).append( ' ' );
        streamExpected.append( "xsi=http://www.w3.org/2001/XMLSchema-instance" ).append( ' ' );

        LOG.info( "streamThis: " + streamExpected.toString() );
        LOG.info( "streamThat: " + streamActual.toString() );
        Assert.assertEquals( streamExpected.toString(), streamActual.toString() );

    }

    /**
     * Tests if 3 records will be inserted and 2 delete so the output should be 1 <br>
     * The request-query tests after getAllRecords
     * 
     * 
     * @throws MetadataStoreException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws MetadataInspectorException
     */
    @Test
    public void testDelete()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: testDelete" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }

        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = TstUtils.insertMetadata( store, ta, TstConstants.tst_9, TstConstants.tst_10,
                                                    TstConstants.tst_1 );

        LOG.info( "Inserted records with ids: " + ids + ". Now: delete them..." );
        String fileString = TstConstants.propEqualToID.getFile();
        if ( fileString == null ) {
            LOG.warn( "Skipping test (file with filterExpression not found)." );
            return;
        }

        // test the deletion
        XMLStreamReader xmlStreamFilter = TstUtils.readXMLStream( fileString );
        Filter constraintDelete = Filter110XMLDecoder.parse( xmlStreamFilter );
        xmlStreamFilter.close();

        MetadataStoreTransaction taDel = store.acquireTransaction();
        DeleteTransaction delete = new DeleteTransaction( "delete", null, constraintDelete );
        taDel.performDelete( delete );
        taDel.commit();
        // test query
        MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 1 );
        resultSet = store.getRecords( query );
        int size = 0;
        while ( resultSet.next() ) {
            size++;
        }

        Assert.assertEquals( 1, size );

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
     */
    @Test
    public void testOutputBrief()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: is output ISO brief? " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = TstUtils.insertMetadata( store, ta, TstConstants.fullRecord );
        resultSet = store.getRecordById( ids );

        XMLStreamReader xmlStreamActual = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                               TstConstants.briefRecord.openStream() );

        // create the should be output
        StringBuilder streamActual = TstUtils.stringBuilderFromXMLStream( xmlStreamActual );

        // create the is output
        // String file = "/home/thomas/Desktop/zTestBrief.xml";
        String file = null;
        StringBuilder streamExpected = TstUtils.stringBuilderFromResultSet( resultSet, ReturnableElement.brief, file,
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
     */

    @Test
    public void testOutputSummary()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: is output ISO summary? " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = TstUtils.insertMetadata( store, ta, TstConstants.fullRecord );
        resultSet = store.getRecordById( ids );

        XMLStreamReader xmlStreamActual = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                               TstConstants.summaryRecord.openStream() );

        // create the should be output
        StringBuilder streamActual = TstUtils.stringBuilderFromXMLStream( xmlStreamActual );

        // create the is output
        // String file = "/home/thomas/Desktop/zTestSummary.xml";
        String file = null;
        StringBuilder streamExpected = TstUtils.stringBuilderFromResultSet( resultSet, ReturnableElement.summary, file,
                                                                            XMLStreamConstants.START_ELEMENT );
        if ( streamExpected == null ) {
            return;
        }
        LOG.debug( "streamThis: " + streamActual.toString() );
        LOG.debug( "streamThat: " + streamExpected.toString() );
        Assert.assertEquals( streamActual.toString(), streamExpected.toString() );

    }

    @Test
    public void testVariousElements()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: test various elements for one metadataRecord " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = TstUtils.insertMetadata( store, ta, TstConstants.tst_10 );
        if ( ids != null ) {
            // test query
            MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 1 );
            resultSet = store.getRecords( query );
            // identifier
            String[] identifier = null;
            String[] title = null;
            String type = null;
            String[] subject = null;
            String[] format = null;
            String[] _abstract = null;
            String[] rights = null;
            String source = null;
            Envelope[] bbox = null;
            while ( resultSet.next() ) {
                MetadataRecord m = resultSet.getRecord();
                identifier = m.getIdentifier();
                title = m.getTitle();
                type = m.getType();
                subject = m.getSubject();
                format = m.getFormat();
                _abstract = m.getAbstract();
                rights = m.getRights();
                source = m.getSource();
                bbox = m.getBoundingBox();
            }
            StringBuilder s_ident = new StringBuilder();
            for ( String id : identifier ) {
                s_ident.append( id );
            }
            StringBuilder s_title = new StringBuilder();
            for ( String t : title ) {
                s_title.append( t );
            }
            StringBuilder s_sub = new StringBuilder();
            for ( String sub : subject ) {
                s_sub.append( sub ).append( ' ' );
            }
            StringBuilder s_form = new StringBuilder();
            for ( String f : format ) {
                s_form.append( f ).append( ' ' );
            }
            StringBuilder s_ab = new StringBuilder();
            for ( String a : _abstract ) {
                s_ab.append( a );
            }
            StringBuilder s_ri = new StringBuilder();
            for ( String r : rights ) {
                s_ri.append( r ).append( ' ' );
            }
            StringBuilder s_b = new StringBuilder();
            for ( Envelope e : bbox ) {
                s_b.append( e.getMin().get0() ).append( ' ' ).append( e.getMin().get1() ).append( ' ' );
                s_b.append( e.getMax().get0() ).append( ' ' ).append( e.getMax().get1() ).append( ' ' );
                s_b.append( e.getCoordinateSystem().getName() );
                LOG.debug( "boundingBox: " + s_b.toString() );
            }

            Assert.assertEquals( "identifier: ", "d0e5c36eec7f473b91b8b249da87d522", s_ident.toString() );
            Assert.assertEquals( "title: ", "SPOT 5 RAW 2007-01-23T10:25:14", s_title.toString() );
            Assert.assertEquals( "type: ", "dataset", type.toString() );
            Assert.assertEquals( "subjects: ", "SPOT 5 PATH 50 ROW 242 Orthoimagery imageryBaseMapsEarthCover ",
                                 s_sub.toString() );
            Assert.assertEquals( "formats: ", "RAW ECW ", s_form.toString() );
            Assert.assertEquals( "abstract: ", "Raw (source) image from CwRS campaigns.", s_ab.toString() );
            Assert.assertEquals( "rights: ", "otherRestrictions license ", s_ri.toString() );
            Assert.assertEquals( "source: ", "Raw (Source) image as delivered by image provider.", source.toString() );
            Assert.assertEquals( "bbox: ", "9.342556163 52.6984540464 10.4685111912 53.3646726483 epsg:4326",
                                 s_b.toString() );
        } else {
            throw new MetadataStoreException( "something went wrong in creation of the metadataRecord" );
        }
    }

}