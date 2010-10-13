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
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException {
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

    /**
     * Tests if 3 records will be inserted and 2 delete so the output should be 1 <br>
     * The request-query tests after getAllRecords
     * 
     * 
     * @throws MetadataStoreException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testDelete()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException {
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
     */

    @Test
    public void testIdentifierRejectFalse()
                            throws MetadataStoreException {
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

    /**
     * If the fileIdentifier shouldn't be generated automaticaly if not set.
     * <p>
     * 1.xml has no fileIdentifier but with one ResourceIdentifier -> insert<br>
     * 2.xml has a fileIdentifier -> insert Output: 2 because 1.xml has a resourceIdentifier which can be taken
     * 
     * @throws MetadataStoreException
     */

    @Test
    public void testIdentifierRejectTrue()
                            throws MetadataStoreException {
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
     */

    @Test
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_Equals()
                            throws MetadataStoreException {
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
     */
    @Test
    public void testOutputBrief()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException {
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

        XMLStreamReader xmlStreamThis = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                             TstConstants.briefRecord.openStream() );

        // create the should be output
        StringBuilder streamThis = stringBuilderFromXMLStream( xmlStreamThis );

        // create the is output
        String file = "/home/thomas/Desktop/zTestBrief.xml";
        StringBuilder streamThat = stringBuilderFromResultSet( resultSet, ReturnableElement.brief, file );
        if ( streamThat == null ) {
            return;
        }
        LOG.info( "streamThis: " + streamThis.toString() );
        LOG.info( "streamThat: " + streamThat.toString() );
        Assert.assertEquals( streamThis.toString(), streamThat.toString() );

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
     */

    @Test
    public void testOutputSummary()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException {
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

        XMLStreamReader xmlStreamThis = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                             TstConstants.summaryRecord.openStream() );

        // create the should be output
        StringBuilder streamThis = stringBuilderFromXMLStream( xmlStreamThis );

        // create the is output
        String file = "/home/thomas/Desktop/zTestSummary.xml";
        StringBuilder streamThat = stringBuilderFromResultSet( resultSet, ReturnableElement.summary, file );
        if ( streamThat == null ) {
            return;
        }
        LOG.debug( "streamThis: " + streamThis.toString() );
        LOG.debug( "streamThat: " + streamThat.toString() );
        Assert.assertEquals( streamThis.toString(), streamThat.toString() );

    }

    @Test
    public void testVariousElements()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException {
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
     * 7.xml has no valid combination -> autmatic generating <br>
     * Output should be 5 valid metadataRecords in backend
     * 
     * @throws MetadataStoreException
     */
    @Test
    public void testResourceIdentifierGenerateTRUE()
                            throws MetadataStoreException {
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
     */
    @Test(expected = MetadataStoreException.class)
    public void testResourceIdentifierGenerateFALSE_NO_RS_ID()
                            throws MetadataStoreException {
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
     */
    @Test(expected = MetadataStoreException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib()
                            throws MetadataStoreException {
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
     */
    @Test(expected = MetadataStoreException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_UUID_Attrib()
                            throws MetadataStoreException {
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
     */
    @Test(expected = MetadataStoreException.class)
    public void testIdentifierRejectTrue2()
                            throws MetadataStoreException {
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
     */
    @Test(expected = MetadataStoreException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_NOT_Equals_NO_UUID()
                            throws MetadataStoreException {
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
     */
    @Test(expected = MetadataStoreException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_NOT_Equals()
                            throws MetadataStoreException {
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

    private List<String> insertMetadata( ISOMetadataStore store, MetadataStoreTransaction ta, URL... URLInput )
                            throws MetadataStoreException {

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
                                                      String file )
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
            xmlStreamThat.nextTag();
            while ( xmlStreamThat.hasNext() ) {
                xmlStreamThat.next();
                if ( xmlStreamThat.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                    streamThat.append( xmlStreamThat.getName() ).append( ' ' );
                }
            }
        }

        return streamThat;
    }

    private StringBuilder stringBuilderFromXMLStream( XMLStreamReader xmlStreamThis )
                            throws XMLStreamException {
        StringBuilder streamThis = new StringBuilder();
        xmlStreamThis.nextTag();
        while ( xmlStreamThis.hasNext() ) {
            xmlStreamThis.next();
            if ( xmlStreamThis.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                streamThis.append( xmlStreamThis.getName() ).append( ' ' );
            }
        }

        return streamThis;
    }

    // private MetadataRecord loadRecord( URL url )
    // throws XMLStreamException, FactoryConfigurationError, IOException {
    // XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.openStream() );
    // return new ISORecord( xmlStream );
    // }
}