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
package org.deegree.metadata.persistence.iso;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.CoreTstProperties;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.publication.DeleteTransaction;
import org.deegree.metadata.publication.InsertTransaction;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataStoreTest {

    private static final Logger LOG = getLogger( ISOMetadataStoreTest.class );

    private ISOMetadataStore store;

    private String jdbcURL;

    private String jdbcUser;

    private String jdbcPass;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        jdbcURL = CoreTstProperties.getProperty( "iso_store_url" );
        jdbcUser = CoreTstProperties.getProperty( "iso_store_user" );
        jdbcPass = CoreTstProperties.getProperty( "iso_store_pass" );
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            Set<String> connIds = ConnectionManager.getConnectionIds();
            LOG.info( "publish the connectionIDs: " + connIds + " " );
            if ( connIds.contains( "iso_pg_set_up_tables" ) ) {
                // skip new creation of the connection
                Connection connDeleteTables = null;
                try {
                    connDeleteTables = ConnectionManager.getConnection( "iso_pg_set_up_tables" );

                    deleteFromTables( connDeleteTables );
                } finally {
                    connDeleteTables.close();
                }

            } else {
                ConnectionManager.addConnection( "iso_pg_set_up_tables", jdbcURL, jdbcUser, jdbcPass, 5, 20 );
                Connection connSetUpTables = null;

                try {
                    connSetUpTables = ConnectionManager.getConnection( "iso_pg_set_up_tables" );

                    setUpTables( connSetUpTables );

                } finally {
                    connSetUpTables.close();
                }

            }

        }
    }

    private void setUpTables( Connection conn )
                            throws SQLException, UnsupportedEncodingException, IOException, MetadataStoreException {

        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            for ( String sql : new ISOMetadataStoreProvider().getDropStatements( TstConstants.configURL ) ) {
                stmt.executeUpdate( sql );
            }

            for ( String sql : new ISOMetadataStoreProvider().getCreateStatements( TstConstants.configURL ) ) {

                stmt.execute( sql );
            }

        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private void deleteFromTables( Connection conn )
                            throws SQLException, UnsupportedEncodingException, IOException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String sql = "DELETE from datasets;";
            stmt.executeUpdate( sql );

        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

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
        String test_folder = "/home/thomas/inserts_bkg/";// CoreTstProperties.getProperty(
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
        List<String> ids = insertMetadata( store, ta, urlArray );

        // TODO test various queries

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
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_12 );
        MetadataResultSet resultSet = store.getRecordsById( ids );

        // create the is output
        // String file = "/home/thomas/Desktop/zTestBrief.xml";
        String file = null;
        StringBuilder streamActual = stringBuilderFromResultSet( resultSet, ReturnableElement.brief, file,
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
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_9, TstConstants.tst_10, TstConstants.tst_11,
                                           TstConstants.tst_1 );

        LOG.info( "Inserted records with ids: " + ids + ". Now: delete them..." );
        String fileString = TstConstants.propEqualToID.getFile();
        if ( fileString == null ) {
            LOG.warn( "Skipping test (file with filterExpression not found)." );
            return;
        }

        // test the deletion
        XMLStreamReader xmlStreamFilter = readXMLStream( fileString );
        Filter constraintDelete = Filter110XMLDecoder.parse( xmlStreamFilter );
        xmlStreamFilter.close();
        DeleteTransaction delete = new DeleteTransaction( "delete", null, constraintDelete );
        ta.performDelete( delete );
        ta.commit();
        // test query
        MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 10, 1 );
        MetadataResultSet resultSet = store.getRecords( query );
        Assert.assertEquals( 1, resultSet.getMembers().size() );

    }

    /**
     * If the fileIdentifier should be generated automaticaly if not set.
     * <p>
     * 1.xml has no fileIdentifier<br>
     * 2.xml has a fileIdentifier
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */

    @Test
    public void testIdentifierRejectFalse()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration generates the identifier automaticaly. (Reject FALSE)" );
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_REJECT_FI_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_1, TstConstants.tst_2 );

        MetadataResultSet resultSet = store.getRecordsById( ids );

        Assert.assertEquals( 2, resultSet.getMembers().size() );

    }

    @Test
    public void testCouplingConsistencyErrorFALSE()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the the coupling of data and service metadata is correct and no exception will be thrown. " );
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_COUPLING_ACCEPT );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_12, TstConstants.tst_12_2, TstConstants.tst_13 );

        MetadataResultSet resultSet = store.getRecordsById( ids );

        Assert.assertEquals( 3, resultSet.getMembers().size() );

    }

    @Test
    public void testCouplingConsistencyErrorFALSE_NO_CONSISTENCY()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the the coupled service metadata will be inserted without any coupling but no exception will be thrown. " );
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_COUPLING_ACCEPT );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_11, TstConstants.tst_13 );

        MetadataResultSet resultSet = store.getRecordsById( ids );

        Assert.assertEquals( 2, resultSet.getMembers().size() );

    }

    @Test
    public void testCouplingConsistencyErrorTRUE_NO_Exception()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the the coupling of data and service metadata is correct and no exception will be thrown. " );
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_COUPLING_Ex_AWARE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_12, TstConstants.tst_12_2, TstConstants.tst_13 );

        MetadataResultSet resultSet = store.getRecordsById( ids );

        Assert.assertEquals( 3, resultSet.getMembers().size() );

    }

    /**
     * If the fileIdentifier shouldn't be generated automaticaly if not set.
     * <p>
     * 1.xml has no fileIdentifier but with one ResourceIdentifier -> insert<br>
     * 2.xml has a fileIdentifier -> insert Output: 2 because 1.xml has a resourceIdentifier which can be taken
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */

    @Test
    public void testIdentifierRejectTrue()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration rejects the insert of the missing identifier. (Reject TRUE)" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_REJECT_FI_TRUE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_1, TstConstants.tst_2 );

        MetadataResultSet resultSet = store.getRecordsById( ids );

        Assert.assertEquals( 2, resultSet.getMembers().size() );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is RS_ID set and id-attribute set
     * <p>
     * Output should be 1
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */

    @Test
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_Equals()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration inserts the right ResourceIdentifier-combination while there is no automatic generating." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_6 );
        MetadataResultSet resultSet = store.getRecordsById( ids );

        Assert.assertEquals( 1, resultSet.getMembers().size() );

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
        List<String> ids = insertMetadata( store, ta, TstConstants.fullRecord );
        MetadataResultSet resultSet = store.getRecordsById( ids );

        XMLStreamReader xmlStreamActual = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                               TstConstants.briefRecord.openStream() );

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
        List<String> ids = insertMetadata( store, ta, TstConstants.fullRecord );
        MetadataResultSet resultSet = store.getRecordsById( ids );

        XMLStreamReader xmlStreamActual = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                               TstConstants.summaryRecord.openStream() );

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

    @Test
    public void testAnyTextElement_ALL()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: test anyText element 'ALL' for one metadataRecord " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_ANYTEXT_ALL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_10 );
        if ( ids != null ) {
            // test query
            MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 10, 1 );
            MetadataResultSet resultSet = store.getRecords( query );
            String anyText = null;
            for ( MetadataRecord m : resultSet.getMembers() ) {
                anyText = m.getAnyText();
            }

            String compare = "d0e5c36eec7f473b91b8b249da87d522 eng UTF 8 dataset European Commission, Joint Research Centre cid-contact@jrc.ec.europa.eu pointOfContact 2007-01-23 ISO 19115:2003/19139 1.0 2 6000 6000 true false 10.2295939511158 52.6984540463519 10.4685111911662 53.2174450795883 9.34255616300099 52.8445914851784 9.57111840348035 53.3646726482873 center 4326 EPSG SPOT 5 RAW 2007-01-23T10:25:14 2007-01-23T10:25:14 d0e5c36eec7f473b91b8b249da87d522 Raw (source) image from CwRS campaigns. European Commission, Joint Research Centre, IPSC, MARS Unit cid-contact@jrc.ec.europa.eu pointOfContact SPOT 5 PATH 50 ROW 242 Orthoimagery GEMET - INSPIRE themes, version 1.0 2008-06-01 publication http://cidportal.jrc.ec.europa.eu/home/idp/info/license/ec-jrc-fc251603/ otherRestrictions (e) intellectual property rights; license unclassified 10.0 eng imageryBaseMapsEarthCover 9.342556163 10.4685111912 52.6984540464 53.3646726483 9.57111840348035 53.3646726482873 9.34255616300099 52.8445914851784 10.2295939511158 52.6984540463519 10.4685111911662 53.2174450795883 9.57111840348035 53.3646726482873 2007-01-23T10:25:14 2007-01-23T10:25:14 Detailed image characteristics XS1 8 XS2 8 XS3 8 SWIR 8 16.129405 163.631838 0.0 RAW RAW N/A ECW N/A http://cidportal.jrc.ec.europa.eu/imagearchive/ Raw (Source) image as delivered by image provider. ";

            Assert.assertEquals( "anyText ALL: ", compare, anyText );

        } else {
            throw new MetadataStoreException( "something went wrong in creation of the metadataRecord" );
        }
    }

    @Test
    public void testAnyTextElement_CORE()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: test anyText element 'CORE' for one metadataRecord " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_ANYTEXT_CORE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_10 );
        if ( ids != null ) {
            // test query
            MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 10, 1 );
            MetadataResultSet resultSet = store.getRecords( query );
            String anyText = null;
            for ( MetadataRecord m : resultSet.getMembers() ) {
                anyText = m.getAnyText();
            }

            String compare = "Raw (source) image from CwRS campaigns. RAW ECW d0e5c36eec7f473b91b8b249da87d522 eng Tue Jan 23 00:00:00 CET 2007 SPOT 5 RAW 2007-01-23T10:25:14 dataset SPOT 5 PATH 50 ROW 242 Orthoimagery imageryBaseMapsEarthCover true otherRestrictions license Raw (Source) image as delivered by image provider. ";

            Assert.assertEquals( "anyText CORE: ", compare, anyText );

        } else {
            throw new MetadataStoreException( "something went wrong in creation of the metadataRecord" );
        }
    }

    @Test
    public void testAnyTextElement_CUSTOM()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException {
        LOG.info( "START Test: test anyText element 'CUSTOM' for one metadataRecord " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_ANYTEXT_CUSTOM );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_10 );
        if ( ids != null ) {
            // test query
            MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 10, 1 );
            MetadataResultSet resultSet = store.getRecords( query );
            String anyText = null;
            for ( MetadataRecord m : resultSet.getMembers() ) {
                anyText = m.getAnyText();
            }

            String compare = "d0e5c36eec7f473b91b8b249da87d522 SPOT 5 PATH 50 ROW 242 Orthoimagery ";

            Assert.assertEquals( "anyText CUSTOM: ", compare, anyText );

        } else {
            throw new MetadataStoreException( "something went wrong in creation of the metadataRecord" );
        }
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
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_10 );
        if ( ids != null ) {
            // test query
            MetadataQuery query = new MetadataQuery( null, null, ResultType.results, 10, 1 );
            MetadataResultSet resultSet = store.getRecords( query );
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
            for ( MetadataRecord m : resultSet.getMembers() ) {
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

    /**
     * Metadata that is false regarding the ResourceIdentifier
     * <p>
     * 3.xml has got an valid combination -> autmatic generating<br>
     * 4.xml has no valid combination -> autmatic generating <br>
     * 5.xml has no valid combination -> autmatic generating <br>
     * 6.xml has a valid combination -> so nothing should be generated <br>
     * 7.xml has no valid combination -> auotmatic generating <br>
     * Output should be 5 valid metadataRecords in backend
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */
    @Test
    public void testResourceIdentifierGenerateTRUE()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test for automaticaly generated ResourceIdentifier-combination." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_RS_GEN_TRUE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_3, TstConstants.tst_4, TstConstants.tst_5,
                                           TstConstants.tst_6, TstConstants.tst_7, TstConstants.tst_8 );

        MetadataResultSet resultSet = store.getRecordsById( ids );

        Assert.assertEquals( 6, resultSet.getMembers().size() );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is no neither RS_ID not id-attribute set
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_NO_RS_ID()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataStoreException( "skipping test (needs configuration)" );
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_2 );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * there is no RS_ID set but id-attribute set
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataStoreException( "skipping test (needs configuration)" );
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_4 );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * there is no RS_ID set but id-attribute and uuid-attribute set
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_UUID_Attrib()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataStoreException( "skipping test (needs configuration)" );
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_5 );

    }

    /**
     * If the fileIdentifier shouldn't be generated automaticaly if not set.
     * <p>
     * 1.xml has no fileIdentifier and no ResourceIdentifier -> reject<br>
     * 3.xml has a fileIdentifier -> insert <br>
     * Output should be a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testIdentifierRejectTrue2()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration rejects the insert of the missing identifier. (Reject TRUE)" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_REJECT_FI_TRUE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataStoreException( "skipping test (needs configuration)" );
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_1, TstConstants.tst_3 );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is RS_ID set and id-attribute set and equals but not uuid compliant
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_NOT_Equals_NO_UUID()
                            throws MetadataStoreException, MetadataInspectorException {
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataStoreException( "skipping test (needs configuration)" );
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_8 );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is RS_ID set and id-attribute set but not equals
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_NOT_Equals()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataStoreException( "skipping test (needs configuration)" );
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_7 );

    }

    @Ignore
    @Test(expected = MetadataInspectorException.class)
    public void testCouplingConsistencyErrorTRUE_WITH_Exception()
                            throws MetadataStoreException, MetadataInspectorException {
        LOG.info( "START Test: test if an exception will be thrown if there is an insert of the service metadata. " );
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( TstConstants.configURL_COUPLING_Ex_AWARE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        MetadataStoreTransaction ta = store.acquireTransaction();
        List<String> ids = insertMetadata( store, ta, TstConstants.tst_11, TstConstants.tst_13 );

    }

    private List<String> insertMetadata( ISOMetadataStore store, MetadataStoreTransaction ta, URL... URLInput )
                            throws MetadataStoreException, MetadataInspectorException {

        List<OMElement> records = new ArrayList<OMElement>();

        List<String> ids = new ArrayList<String>();
        for ( URL file : URLInput ) {

            OMElement record = new XMLAdapter( file ).getRootElement();
            LOG.info( "inserting filename: " + file.getFile() );
            records.add( record );

        }

        int countInserted = 0;
        int countInsert = 0;
        countInsert = URLInput.length;

        InsertTransaction insert = new InsertTransaction( records, records.get( 0 ).getQName(), "insert" );
        ids = ta.performInsert( insert );

        ta.commit();

        if ( !ids.isEmpty() ) {
            countInserted += ids.size();
        }

        LOG.info( countInserted + " from " + countInsert + " Metadata inserted." );
        return ids;
    }

    private XMLStreamReader readXMLStream( String fileString )
                            throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                         new FileInputStream(
                                                                                                              new File(
                                                                                                                        fileString ) ) );
        StAXParsingHelper.skipStartDocument( xmlStream );
        return xmlStream;
    }

    private StringBuilder stringBuilderFromResultSet( MetadataResultSet resultSet, ReturnableElement returnableElement,
                                                      String file, int searchEvent )
                            throws XMLStreamException, FileNotFoundException {
        OutputStream fout = null;
        if ( file == null ) {
            fout = new ByteArrayOutputStream();
        } else {
            fout = new FileOutputStream( file );
        }

        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( fout );

        for ( MetadataRecord m : resultSet.getMembers() ) {
            m.serialize( writer, returnableElement );
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

    private StringBuilder stringBuilderFromXMLStream( XMLStreamReader xmlStreamThis )
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