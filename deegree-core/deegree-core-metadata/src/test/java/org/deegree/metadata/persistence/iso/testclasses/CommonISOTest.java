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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.iso.ISOMetadataStore;
import org.deegree.metadata.persistence.iso.ISOMetadataStoreProvider;
import org.deegree.metadata.persistence.iso.helper.AbstractISOTest;
import org.deegree.metadata.persistence.iso.helper.TstConstants;
import org.deegree.metadata.persistence.iso.helper.TstUtils;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.MetadataProperty;
import org.deegree.metadata.persistence.transaction.UpdateOperation;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.csw.MetadataStoreException;
import org.jaxen.JaxenException;
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

    protected static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

    @Test
    public void testInsert()
                            throws MetadataStoreException, FactoryConfigurationError, IOException,
                            MetadataInspectorException, ResourceInitException, URISyntaxException {
        LOG.info( "START Test: testInsert" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        String test_folder = "/home/lyn/Dokumente/metadata/";
        // URL data_folder = CommonISOTest.class.getResource( "../metadatarecords" );

        // String test_folder = "/home/thomas/Dokumente/metadata/test/";// CoreTstProperties.getProperty(
        // "iso_metadata_insert_test_folder"
        // );
        // if ( test_folder == null ) {
        // LOG.warn( "Skipping test (no testCase folder found)" );
        // return;
        // }

        File folder = new File( test_folder );
        File[] fileArray = folder.listFiles();
        // LOG.info( "" + fileArray.length );
        if ( fileArray == null ) {
            LOG.error( "test folder does not exist: " + test_folder );
            return;
        }

        URL[] urlArray = new URL[fileArray.length];
        int counter = 0;
        for ( File f : fileArray ) {
            urlArray[counter++] = new URL( "file:" + f.getAbsolutePath() );
        }

        TstUtils.insertMetadata( store, urlArray );

        MetadataQuery query = new MetadataQuery( null, null, 1, 10 );
        resultSet = store.getRecords( query );
        int size = 0;
        while ( resultSet.next() ) {
            size++;
        }
        Assert.assertEquals( 1, size );
        // TODO test various queries
    }

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
        StringBuilder streamActual = TstUtils.stringBuilderFromResultSet( resultSet, ReturnableElement.brief, file,
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
     * Tests if 3 records will be inserted and 2 delete so the output should be 1 <br>
     * The request-query tests after getAllRecords
     * 
     * 
     * @throws MetadataStoreException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws MetadataInspectorException
     * @throws ResourceInitException
     */
    @Test
    public void testDelete()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            MetadataInspectorException, ResourceInitException {
        LOG.info( "START Test: testDelete" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }

        List<String> ids = TstUtils.insertMetadata( store, TstConstants.tst_9, TstConstants.tst_10, TstConstants.tst_1 );

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
        DeleteOperation delete = new DeleteOperation( "delete", null, constraintDelete );
        taDel.performDelete( delete );
        taDel.commit();
        // test query
        MetadataQuery query = new MetadataQuery( null, null, 1, 10 );
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
                            throws MetadataStoreException, FactoryConfigurationError, MetadataInspectorException,
                            ResourceInitException {
        LOG.info( "START Test: test various elements for one metadataRecord " );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        List<String> ids = TstUtils.insertMetadata( store, TstConstants.tst_10 );
        if ( ids != null ) {
            // test query
            MetadataQuery query = new MetadataQuery( null, null, 1, 10 );
            resultSet = store.getRecords( query );
            // identifier
            String identifier = null;
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
            s_ident.append( identifier );
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
                s_b.append( e.getCoordinateSystem().getAlias() );
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

    @Test
    public void testUpdateString()
                            throws MetadataStoreException, MetadataInspectorException, FactoryConfigurationError,
                            ResourceInitException {
        String idToUpdate = prepareUpdate();
        if ( idToUpdate == null ) {
            return;
        }

        // constraint
        Operator op = new PropertyIsEqualTo( new PropertyName( "apiso:identifier", nsContext ),
                                             new Literal<PrimitiveValue>( idToUpdate ), true );
        Filter constraint = new OperatorFilter( op );

        // create recordProperty
        List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
        String xPath = "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
        PropertyName name = new PropertyName( xPath, nsContext );
        String value = "UPDATED ORGANISATIONNAME";
        recordProperties.add( new MetadataProperty( name, value ) );

        // update!
        MetadataStoreTransaction mst = store.acquireTransaction();
        UpdateOperation update = new UpdateOperation( null, null, null, constraint, recordProperties );
        mst.performUpdate( update );
        mst.commit();

        // get record which should be updated
        MetadataQuery query = new MetadataQuery( constraint, null, 1, 10 );
        resultSet = store.getRecords( query );
        assertNotNull( resultSet );
        assertTrue( resultSet.next() );

        MetadataRecord m = resultSet.getRecord();
        assertNotNull( m );
        assertTrue( m instanceof ISORecord );
        String identifier = m.getIdentifier();

        // test if the updated was successfull
        if ( identifier.equals( idToUpdate ) ) {
            String updatedString = ( (ISORecord) m ).getStringFromXPath( new XPath( xPath, nsContext ) );
            Assert.assertEquals( value, updatedString );
        }
    }

    @Test
    public void testUpdateStringWithCQP()
                            throws MetadataStoreException, MetadataInspectorException, FactoryConfigurationError,
                            ResourceInitException {
        String idToUpdate = prepareUpdate();
        if ( idToUpdate == null ) {
            return;
        }

        // constraint
        Operator op = new PropertyIsEqualTo( new PropertyName( "apiso:identifier", nsContext ),
                                             new Literal<PrimitiveValue>( idToUpdate ), true );
        Filter constraint = new OperatorFilter( op );

        // create recordProperty
        List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
        String xPath = "/apiso:Modified";
        PropertyName name = new PropertyName( xPath, nsContext );
        String value = "3333-11-22";
        recordProperties.add( new MetadataProperty( name, value ) );

        // update!
        MetadataStoreTransaction mst = store.acquireTransaction();
        UpdateOperation update = new UpdateOperation( null, null, null, constraint, recordProperties );
        mst.performUpdate( update );
        mst.commit();

        // get record which should be updated
        MetadataQuery query = new MetadataQuery( constraint, null, 1, 10 );
        resultSet = store.getRecords( query );
        assertNotNull( resultSet );
        assertTrue( resultSet.next() );

        MetadataRecord m = resultSet.getRecord();
        assertNotNull( m );
        assertTrue( m instanceof ISORecord );
        String identifier = m.getIdentifier();

        // test if the updated was successfull
        if ( identifier.equals( idToUpdate ) ) {
            String updatedString = ( (ISORecord) m ).getStringFromXPath( new XPath(
                                                                                    "/gmd:MD_Metadata/gmd:dateStamp/gco:Date",
                                                                                    nsContext ) );
            Assert.assertEquals( value, updatedString );
        }
    }

    @Test
    public void testUpdateOMElementReplace()
                            throws FactoryConfigurationError, MetadataStoreException, MetadataInspectorException,
                            JaxenException, ResourceInitException {
        String idToUpdate = prepareUpdate();
        if ( idToUpdate == null ) {
            return;
        }

        // constraint
        Operator op = new PropertyIsEqualTo( new PropertyName( "apiso:identifier", nsContext ),
                                             new Literal<PrimitiveValue>( idToUpdate ), true );
        Filter constraint = new OperatorFilter( op );

        // create recordProperty
        List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
        String xPath = "/gmd:MD_Metadata/gmd:contact";
        PropertyName name = new PropertyName( xPath, nsContext );
        InputStream is = CommonISOTest.class.getResourceAsStream( "../update/replace.xml" );
        XMLAdapter a = new XMLAdapter( is );
        OMElement value = a.getRootElement();
        recordProperties.add( new MetadataProperty( name, value ) );

        // update!
        MetadataStoreTransaction mst = store.acquireTransaction();
        UpdateOperation update = new UpdateOperation( null, null, null, constraint, recordProperties );
        mst.performUpdate( update );
        mst.commit();

        // get record which should be updated
        MetadataQuery query = new MetadataQuery( constraint, null, 1, 10 );
        resultSet = store.getRecords( query );
        assertNotNull( resultSet );
        assertTrue( resultSet.next() );

        MetadataRecord m = resultSet.getRecord();
        assertNotNull( m );
        assertTrue( m instanceof ISORecord );
        String identifier = m.getIdentifier();

        // test if the updated was successfull
        if ( identifier.equals( idToUpdate ) ) {
            String testXpath = xPath + "/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString";
            OMElement updatedNode = ( (ISORecord) m ).getNodeFromXPath( new XPath( testXpath, nsContext ) );

            AXIOMXPath p = new AXIOMXPath( testXpath );
            p.setNamespaceContext( nsContext );
            Object valueNode = p.selectSingleNode( value );
            Assert.assertEquals( ( (OMElement) valueNode ).getText(), updatedNode.getText() );
        }

    }

    @Test
    public void testUpdateOMElementRemove()
                            throws FactoryConfigurationError, MetadataStoreException, MetadataInspectorException,
                            ResourceInitException {
        String idToUpdate = prepareUpdate();
        if ( idToUpdate == null ) {
            return;
        }

        // constraint
        Operator op = new PropertyIsEqualTo( new PropertyName( "apiso:identifier", nsContext ),
                                             new Literal<PrimitiveValue>( idToUpdate ), true );
        Filter constraint = new OperatorFilter( op );

        // create recordProperty
        List<MetadataProperty> recordProperties = new ArrayList<MetadataProperty>();
        String xPath = "/gmd:MD_Metadata/gmd:dataQualityInfo";
        PropertyName name = new PropertyName( xPath, nsContext );
        recordProperties.add( new MetadataProperty( name, null ) );

        // get record which should be updated
        MetadataQuery query = new MetadataQuery( constraint, null, 1, 10 );
        resultSet = store.getRecords( query );
        assertNotNull( resultSet );
        assertTrue( resultSet.next() );

        MetadataRecord m = resultSet.getRecord();
        assertNotNull( m );

        OMElement updatedNode = ( (ISORecord) m ).getNodeFromXPath( new XPath( xPath, nsContext ) );
        assertNotNull( updatedNode );

        // update!
        MetadataStoreTransaction mst = store.acquireTransaction();
        UpdateOperation update = new UpdateOperation( null, null, null, constraint, recordProperties );
        mst.performUpdate( update );
        mst.commit();

        // get record which should be updated
        resultSet = store.getRecords( query );
        assertNotNull( resultSet );
        assertTrue( resultSet.next() );

        m = resultSet.getRecord();
        assertNotNull( m );
        assertTrue( m instanceof ISORecord );
        String identifier = m.getIdentifier();

        // test if the updated was successfull
        if ( identifier.equals( idToUpdate ) ) {
            updatedNode = ( (ISORecord) m ).getNodeFromXPath( new XPath( xPath, nsContext ) );
            assertNull( updatedNode );
        }
    }

    @Test
    public void updateCompleteWithoutConstraint()
                            throws MetadataStoreException, MetadataInspectorException, JaxenException,
                            ResourceInitException {
        String idToUpdate = prepareUpdate();
        if ( idToUpdate == null ) {
            return;
        }

        // md to update
        InputStream is = CommonISOTest.class.getResourceAsStream( "../update/9update.xml" );
        XMLAdapter a = new XMLAdapter( is );
        ISORecord value = new ISORecord( a.getRootElement() );

        // update!
        MetadataStoreTransaction mst = store.acquireTransaction();
        UpdateOperation update = new UpdateOperation( null, value, null, null, null );
        int noOfUp = mst.performUpdate( update );
        assertEquals( 1, noOfUp );
        mst.commit();

        // get record which should be updated
        Operator op = new PropertyIsEqualTo( new PropertyName( "apiso:identifier", nsContext ),
                                             new Literal<PrimitiveValue>( idToUpdate ), true );
        MetadataQuery query = new MetadataQuery( new OperatorFilter( op ), null, 1, 10 );
        resultSet = store.getRecords( query );
        assertNotNull( resultSet );
        assertTrue( resultSet.next() );

        MetadataRecord m = resultSet.getRecord();
        assertNotNull( m );
        assertTrue( m instanceof ISORecord );
        String identifier = m.getIdentifier();

        // test if the updated was successfull
        if ( identifier.equals( idToUpdate ) ) {
            String testXpath = "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
            OMElement updatedNode = ( (ISORecord) m ).getNodeFromXPath( new XPath( testXpath, nsContext ) );

            AXIOMXPath p = new AXIOMXPath( testXpath );
            p.setNamespaceContext( nsContext );
            Object valueNode = p.selectSingleNode( value.getAsOMElement() );
            Assert.assertEquals( ( (OMElement) valueNode ).getText(), updatedNode.getText() );
        }
    }

    @Test
    public void updateCompleteWithConstraint()
                            throws MetadataStoreException, MetadataInspectorException, JaxenException,
                            ResourceInitException {
        String idToUpdate = prepareUpdate();
        if ( idToUpdate == null ) {
            return;
        }

        // constraint
        Operator op = new PropertyIsEqualTo( new PropertyName( "apiso:identifier", nsContext ),
                                             new Literal<PrimitiveValue>( idToUpdate ), true );
        Filter constraint = new OperatorFilter( op );

        // md to update
        InputStream is = CommonISOTest.class.getResourceAsStream( "../update/9update.xml" );
        XMLAdapter a = new XMLAdapter( is );
        ISORecord value = new ISORecord( a.getRootElement() );

        // update!
        MetadataStoreTransaction mst = store.acquireTransaction();
        UpdateOperation update = new UpdateOperation( null, value, null, constraint, null );
        int noOfUp = mst.performUpdate( update );
        assertEquals( 1, noOfUp );
        mst.commit();

        // get record which should be updated
        MetadataQuery query = new MetadataQuery( constraint, null, 1, 10 );
        resultSet = store.getRecords( query );
        assertNotNull( resultSet );
        assertTrue( resultSet.next() );

        MetadataRecord m = resultSet.getRecord();
        assertNotNull( m );
        assertTrue( m instanceof ISORecord );
        String identifier = m.getIdentifier();

        // test if the updated was successfull
        if ( identifier.equals( idToUpdate ) ) {
            String testXpath = "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
            OMElement updatedNode = ( (ISORecord) m ).getNodeFromXPath( new XPath( testXpath, nsContext ) );

            AXIOMXPath p = new AXIOMXPath( testXpath );
            p.setNamespaceContext( nsContext );
            Object valueNode = p.selectSingleNode( value.getAsOMElement() );
            Assert.assertEquals( ( (OMElement) valueNode ).getText(), ( (OMElement) updatedNode ).getText() );
        }
    }

    @Test
    public void updateNotExistingRecord()
                            throws MetadataStoreException, MetadataInspectorException, JaxenException,
                            ResourceInitException {
        String idToUpdate = prepareUpdate();
        if ( idToUpdate == null ) {
            return;
        }

        // constraint
        Operator op = new PropertyIsEqualTo( new PropertyName( "apiso:identifier", nsContext ),
                                             new Literal<PrimitiveValue>( "dummyDoesNotExist" ), true );
        Filter constraint = new OperatorFilter( op );

        // md to update
        InputStream is = CommonISOTest.class.getResourceAsStream( "../update/9update.xml" );
        XMLAdapter a = new XMLAdapter( is );
        ISORecord value = new ISORecord( a.getRootElement() );

        // update!
        MetadataStoreTransaction mst = store.acquireTransaction();
        UpdateOperation update = new UpdateOperation( null, value, null, constraint, null );
        int noOfUp = mst.performUpdate( update );
        assertEquals( 0, noOfUp );
        mst.commit();
    }

    public String prepareUpdate()
                            throws MetadataStoreException, MetadataInspectorException, ResourceInitException {
        LOG.info( "START Test: testUpdate" );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return null;
        }

        List<String> ids = TstUtils.insertMetadata( store, TstConstants.tst_9 );
        LOG.info( "Inserted records with ids: " + ids + ". Now: update " + ids );

        assertNotNull( ids );
        assertTrue( ids.size() > 0 );

        return ids.get( 0 );
    }
}