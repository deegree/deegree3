//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wfs.query;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.services.wfs.query.StoredQueryHandler.GET_FEATURE_BY_ID;
import static org.deegree.services.wfs.query.StoredQueryHandler.GET_FEATURE_BY_TYPE;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.feature.types.FeatureType;
import org.deegree.protocol.wfs.storedquery.CreateStoredQuery;
import org.deegree.protocol.wfs.storedquery.DropStoredQuery;
import org.deegree.protocol.wfs.storedquery.QueryExpressionText;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.WfsFeatureStoreManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StoredQueryHandlerTest {

    private static final NamespaceBindings NS_CONTEXT = new NamespaceBindings();

    private static File managedStoredQueries;

    @BeforeClass
    public static void initNamespaceContext()
                            throws IOException {
        NS_CONTEXT.addNamespace( "wfs", WFS_200_NS );
    }

    @Before
    public void createManagedStoredQueriesDirectory()
                            throws IOException {
        managedStoredQueries = Files.createTempDirectory( "managedStoredQueries" ).toFile();
        managedStoredQueries.deleteOnExit();
    }

    @Test
    public void testCollectAndSortFeatureTypesToExport_AllFeatureTypes() {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        List<QName> configuredFeatureTypeNames = Collections.emptyList();
        List<QName> featureTypeNamesToExport = storedQueryHandler.collectAndSortFeatureTypesToExport( configuredFeatureTypeNames );

        assertThat( featureTypeNamesToExport.size(), is( featureTypes.size() ) );
        for ( FeatureType featureType : featureTypes ) {
            assertThat( featureTypeNamesToExport, hasItems( featureType.getName() ) );
        }
    }

    @Test
    public void testCollectAndSortFeatureTypesToExport_EmptyFeatureTypeList() {
        List<FeatureType> featureTypes = Collections.emptyList();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        List<QName> configuredFeatureTypeNames = Collections.emptyList();
        List<QName> featureTypeNamesToExport = storedQueryHandler.collectAndSortFeatureTypesToExport( configuredFeatureTypeNames );

        assertThat( featureTypeNamesToExport.size(), is( 0 ) );
    }

    @Test
    public void testCollectAndSortFeatureTypesToExport_LimitedConfiguredFeatureTypes() {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        List<QName> configuredFeatureTypeNames = configuredFeatureTypeNames();
        List<QName> featureTypeNamesToExport = storedQueryHandler.collectAndSortFeatureTypesToExport( configuredFeatureTypeNames );

        assertThat( featureTypeNamesToExport.size(), is( 1 ) );

        QName featureTypeNameToExport = featureTypeNamesToExport.get( 0 );
        assertThat( featureTypeNameToExport.getLocalPart(), is( "one" ) );
        assertThat( featureTypeNameToExport.getNamespaceURI(), is( "" ) );
        assertThat( featureTypeNameToExport.getPrefix(), is( "" ) );
    }

    @Test
    public void testInitManagedContainsFixedStoredQueries() {

        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        assertThat( storedQueryHandler.hasStoredQuery( GET_FEATURE_BY_ID ), is( true ) );
        assertThat( storedQueryHandler.hasStoredQuery( GET_FEATURE_BY_TYPE ), is( true ) );
    }

    @Test
    public void testInitManagedWitNullManagedStoredQueryDirectory() {
        List<FeatureType> featureTypes = featureTypes();

        new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(), null );
    }

    @Test
    public void testInitManagedWithoutExistingManagedStoredQueryDirectoryThrowsException() {
        List<FeatureType> featureTypes = featureTypes();

        new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                new File( "this/directory/does/not/exist" ) );
    }

    @Test
    public void testInitManagedStoredQueries()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();

        File managedStoredQueries = Files.createTempDirectory( "managedStoredQueries" ).toFile();
        OutputStream output = new FileOutputStream( new File( managedStoredQueries, "storedQuery_byName.xml" ) );
        InputStream resourceAsStream = StoredQueryHandlerTest.class.getResourceAsStream( "storedQuery_byName.xml" );
        IOUtils.copy( resourceAsStream, output );
        resourceAsStream.close();
        output.close();

        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        assertThat( storedQueryHandler.hasStoredQuery( GET_FEATURE_BY_ID ), is( true ) );
        assertThat( storedQueryHandler.hasStoredQuery( GET_FEATURE_BY_TYPE ), is( true ) );
        assertThat( storedQueryHandler.hasStoredQuery( "ByName" ), is( true ) );
    }

    @Test
    public void testInitErroneousManagedStoredQueries()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();

        File managedStoredQueries = Files.createTempDirectory( "managedStoredQueries" ).toFile();
        OutputStream output = new FileOutputStream( new File( managedStoredQueries, "storedQuery_erroneous.xml" ) );
        InputStream resourceAsStream = StoredQueryHandlerTest.class.getResourceAsStream( "storedQuery_erroneous.xml" );
        IOUtils.copy( resourceAsStream, output );
        resourceAsStream.close();
        output.close();

        new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(), managedStoredQueries );
    }

    @Test
    public void testDoCreateStoredQuery()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        String id = "mangedStoredQuery";
        CreateStoredQuery request = createStoredQuery( id );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        storedQueryHandler.doCreateStoredQuery( request, mockHttpResponseBuffer( xmlStreamWriter ) );
        xmlStreamWriter.close();

        assertThat( storedQueryHandler.hasStoredQuery( id ), is( true ) );
        assertThat( xml( outStream.toString() ),
                    hasXPath( "/wfs:CreateStoredQueryResponse[@status='OK']", NS_CONTEXT ) );
    }

    @Test(expected = OWSException.class)
    public void testDoCreateStoredQuery_NullManagedStoredQueryDirectory()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        null );

        String id = "mangedStoredQuery";
        CreateStoredQuery request = createStoredQuery( id );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        storedQueryHandler.doCreateStoredQuery( request, mockHttpResponseBuffer( xmlStreamWriter ) );
        xmlStreamWriter.close();
    }

    @Test(expected = OWSException.class)
    public void testDoCreateStoredQuery_NotExistingManagedStoredQueryDirectory()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        new File( "this/directory/does/not/exist" ) );

        String id = "mangedStoredQuery";
        CreateStoredQuery request = createStoredQuery( id );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        storedQueryHandler.doCreateStoredQuery( request, mockHttpResponseBuffer( xmlStreamWriter ) );
        xmlStreamWriter.close();
    }

    @Test(expected = OWSException.class)
    public void testDoCreateStoredQuery_DuplicateId()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        String id = "mangedStoredQuery";
        CreateStoredQuery request = createStoredQuery( id );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        try {
            storedQueryHandler.doCreateStoredQuery( request, mockHttpResponseBuffer( xmlStreamWriter ) );
            storedQueryHandler.doCreateStoredQuery( request, mockHttpResponseBuffer( xmlStreamWriter ) );
        } finally {
            xmlStreamWriter.close();
        }
    }

    @Test(expected = OWSException.class)
    public void testDoCreateStoredQuery_UnsupportedLanguage()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        String id = "mangedStoredQuery";
        CreateStoredQuery request = createStoredQuery( id, "http://qry.example.org" );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        storedQueryHandler.doCreateStoredQuery( request, mockHttpResponseBuffer( xmlStreamWriter ) );
        xmlStreamWriter.close();
    }

    @Test
    public void testDoDropStoredQuery()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        String id = "mangedStoredQuery";
        insertStoredQuery( storedQueryHandler, id );

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        storedQueryHandler.doDropStoredQuery( dropStoredQuery( id ), mockHttpResponseBuffer( xmlStreamWriter ) );
        xmlStreamWriter.close();

        assertThat( storedQueryHandler.hasStoredQuery( id ), is( false ) );
        assertThat( xml( outStream.toString() ), hasXPath( "/wfs:DropStoredQueryResponse[@status='OK']", NS_CONTEXT ) );
    }

    @Test
    public void testDoDropStoredQuery_Unremovable()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
            storedQueryHandler.doDropStoredQuery( dropStoredQuery( GET_FEATURE_BY_ID ),
                                                  mockHttpResponseBuffer( xmlStreamWriter ) );
            xmlStreamWriter.close();
        } catch ( Exception e ) {
        }
        assertThat( storedQueryHandler.hasStoredQuery( GET_FEATURE_BY_ID ), is( true ) );
    }

    @Test(expected = OWSException.class)
    public void testDoDropStoredQuery_Unremovable_Exception()
                            throws Exception {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>(),
                                                                        managedStoredQueries );

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        storedQueryHandler.doDropStoredQuery( dropStoredQuery( GET_FEATURE_BY_ID ),
                                              mockHttpResponseBuffer( xmlStreamWriter ) );
        xmlStreamWriter.close();

        assertThat( storedQueryHandler.hasStoredQuery( GET_FEATURE_BY_ID ), is( true ) );
    }

    private void insertStoredQuery( StoredQueryHandler storedQueryHandler, String id )
                            throws Exception {
        CreateStoredQuery request = createStoredQuery( id );
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( outStream );
        storedQueryHandler.doCreateStoredQuery( request, mockHttpResponseBuffer( xmlStreamWriter ) );
        xmlStreamWriter.close();
    }

    private CreateStoredQuery createStoredQuery( String id ) {
        return createStoredQuery( id, StoredQueryHandler.LANGUAGE_WFS_QUERY_EXPRESSION );
    }

    private CreateStoredQuery createStoredQuery( String id, String language ) {
        List<StoredQueryDefinition> queryDefinitions = new ArrayList<StoredQueryDefinition>();
        StoredQueryDefinition queryDefinition = mock( StoredQueryDefinition.class );
        when( queryDefinition.getId() ).thenReturn( id );
        queryDefinitions.add( queryDefinition );
        List<QueryExpressionText> queryExpressionTexts = new ArrayList<QueryExpressionText>();
        QueryExpressionText queryExpressionText = mock( QueryExpressionText.class );
        when( queryExpressionText.getLanguage() ).thenReturn( language );
        queryExpressionTexts.add( queryExpressionText );
        when( queryDefinition.getQueryExpressionTextEls() ).thenReturn( queryExpressionTexts );
        return new CreateStoredQuery( VERSION_200, "handle", queryDefinitions );
    }

    private DropStoredQuery dropStoredQuery( String id ) {
        return new DropStoredQuery( VERSION_200, "handle", id );
    }

    private List<FeatureType> featureTypes() {
        List<FeatureType> featureTypes = new ArrayList<FeatureType>();
        featureTypes.add( mockFeatureType( "one" ) );
        featureTypes.add( mockFeatureType( "two" ) );
        return featureTypes;
    }

    private List<QName> configuredFeatureTypeNames() {
        List<QName> configuredFeatureTypes = new ArrayList<QName>();
        configuredFeatureTypes.add( new QName( "one" ) );
        return configuredFeatureTypes;
    }

    private FeatureType mockFeatureType( String name ) {
        FeatureType mockedFeatureType = mock( FeatureType.class );
        QName qName = new QName( name );
        when( mockedFeatureType.getName() ).thenReturn( qName );
        return mockedFeatureType;
    }

    private WebFeatureService mockWFS( Collection<FeatureType> featureTypes ) {
        WebFeatureService mockedWfs = mock( WebFeatureService.class );
        WfsFeatureStoreManager mockedStoreManager = mockStoreManager( featureTypes );
        when( mockedWfs.getStoreManager() ).thenReturn( mockedStoreManager );
        return mockedWfs;
    }

    private WfsFeatureStoreManager mockStoreManager( Collection<FeatureType> featureTypes ) {
        WfsFeatureStoreManager mockedStoreManager = mock( WfsFeatureStoreManager.class );
        when( mockedStoreManager.getFeatureTypes() ).thenReturn( featureTypes );
        return mockedStoreManager;
    }

    private HttpResponseBuffer mockHttpResponseBuffer( XMLStreamWriter xmlStreamWriter )
                            throws Exception {
        HttpResponseBuffer mockedResponse = mock( HttpResponseBuffer.class );
        when( mockedResponse.getXMLWriter( anyBoolean() ) ).thenReturn( xmlStreamWriter );
        return mockedResponse;
    }

}