//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.feature.persistence.postgis;

import static org.deegree.commons.tom.ResolveMode.LOCAL;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreProvider;
import org.deegree.feature.persistence.sql.config.SQLFeatureStoreConfigWriter;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.ResolveState;
import org.deegree.gml.feature.StreamFeatureCollection;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreTst {

    private static Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreTst.class );

    private static final boolean enable = false;

    // @Test
    // public void testMappingInspireAU()
    // throws ClassCastException, ClassNotFoundException, InstantiationException,
    // IllegalAccessException, IOException, XMLStreamException, FactoryConfigurationError {
    //
    // ApplicationSchema appSchema = getInspireSchemaAU();
    // if ( appSchema == null ) {
    // return;
    // }
    //
    // AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true,
    // new GeometryStorageParams( CRSUtils.EPSG_4326, "-1", DIM_2 ), -1, true, true );
    // MappedApplicationSchema mappedSchema = mapper.getMappedSchema();
    // SQLFeatureStoreConfigWriter configWriter = new SQLFeatureStoreConfigWriter( mappedSchema );
    // File file = new File( "/tmp/inspire-ad.xml" );
    // FileOutputStream fos = new FileOutputStream( file );
    // XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( fos );
    // xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
    // configWriter.writeConfig( xmlWriter,
    // "testconn",
    // Collections.singletonList(
    // "file:/home/schneider/.deegree/inspire-test/schemas/inspire/annex1/AdministrativeUnits.xsd" ) );
    // xmlWriter.close();
    // IOUtils.closeQuietly( fos );
    // System.out.println( "Wrote to file " + file );
    //
    // file = new File( "/tmp/inspire-ad.sql" );
    // PrintWriter writer = new PrintWriter( file );
    // String[] createStmts = new PostGISDDLCreator( mappedSchema ).getDDL();
    // for ( String stmt : createStmts ) {
    // writer.println( stmt + ";" );
    // }
    // IOUtils.closeQuietly( writer );
    // System.out.println( "Wrote to file " + file );
    // }

    // @Test
    // public void testMappingBoreholeML()
    // throws ClassCastException, ClassNotFoundException, InstantiationException,
    // IllegalAccessException, IOException, XMLStreamException, FactoryConfigurationError {
    //
    // File addressesFile = new File(
    // "/home/schneider/workspaces/projekte/bgrbmlwfs-trunk/modules/bgrbmlwfs-workspace/src/main/workspace/appschemas/boreholeml/BoreholeML.xsd"
    // );
    // URL url = addressesFile.toURI().toURL();
    // ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( GML_32, null, addressesFile );
    // ApplicationSchema appSchema = decoder.extractFeatureTypeSchema();
    //
    // AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, CRSManager.getCRSRef( "EPSG:7432" ), "-1"
    // );
    // MappedApplicationSchema mappedSchema = mapper.getMappedSchema();
    // PostGISFeatureStoreConfigWriter configWriter = new PostGISFeatureStoreConfigWriter( mappedSchema );
    // File file = new File( "/tmp/boreholeml.xml" );
    // FileOutputStream fos = new FileOutputStream( file );
    // XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( fos );
    // xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
    // configWriter.writeConfig( xmlWriter,
    // "testconn",
    // Collections.singletonList(
    // "file:/home/schneider/.deegree/inspire-test/schemas/inspire/annex1/AdministrativeUnits.xsd" ) );
    // xmlWriter.close();
    // IOUtils.closeQuietly( fos );
    // System.out.println( "Wrote to file " + file );
    //
    // file = new File( "/tmp/boreholeml.sql" );
    // PrintWriter writer = new PrintWriter( file );
    // String[] createStmts = new PostGISDDLCreator( mappedSchema ).getDDL();
    // for ( String stmt : createStmts ) {
    // writer.println( stmt + ";" );
    // }
    // IOUtils.closeQuietly( writer );
    // System.out.println( "Wrote to file " + file );
    // }

    @Test
    public void testInsertInspireAU()
                            throws Throwable {

        AppSchema appSchema = getInspireSchemaAU();
        if ( appSchema == null ) {
            return;
        }

        String jdbcURL = TestProperties.getProperty( "testdb.postgis.url" );
        String jdbcUser = TestProperties.getProperty( "testdb.postgis.user" );
        String jdbcPass = TestProperties.getProperty( "testdb.postgis.pass" );
        if ( jdbcURL == null ) {
            return;
        }

        ConnectionManager.addConnection( "inspire-au", jdbcURL, jdbcUser, jdbcPass, 1, 10 );
        SQLFeatureStoreProvider provider = new SQLFeatureStoreProvider();
        FeatureStore fs = provider.create( new URL(
                                                    "file:/home/schneider/.deegree/inspire-test/datasources/feature/inspire-au.xml" ) );
        Assert.assertNotNull( fs );

        MappedAppSchema mappedSchema = (MappedAppSchema) fs.getSchema();
        Assert.assertNotNull( mappedSchema );

        SQLFeatureStoreConfigWriter configWriter = new SQLFeatureStoreConfigWriter( mappedSchema );
        File file = new File( "/tmp/inspire-au.xml" );
        FileOutputStream fos = new FileOutputStream( file );
        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( fos );
        xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
        configWriter.writeConfig( xmlWriter,
                                  "testconn",
                                  Collections.singletonList( "file:/home/schneider/.deegree/inspire-test/schemas/inspire/annex1/AdministrativeUnits.xsd" ) );
        xmlWriter.close();
        IOUtils.closeQuietly( fos );
        System.out.println( "Wrote to file " + file );

        URL datasetURL = new URL( "file:/home/schneider/geodata/inspire/au-spatial-ds/au-provincies.gml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_32, datasetURL );
        gmlReader.setApplicationSchema( fs.getSchema() );
        FeatureStoreTransaction ta = null;
        try {
            ta = fs.acquireTransaction();
            FeatureCollection fc = gmlReader.readFeatureCollection();
            ta.performInsert( fc, IDGenMode.USE_EXISTING );
            ta.commit();
        } catch ( Throwable t ) {
            if ( ta != null ) {
                ta.rollback();
            }
            throw t;
        } finally {
            gmlReader.close();
        }
    }

    @Test
    public void testQueryInspireAU()
                            throws Throwable {
        AppSchema appSchema = getInspireSchemaAU();
        if ( appSchema == null ) {
            return;
        }

        String jdbcURL = TestProperties.getProperty( "testdb.postgis.url" );
        String jdbcUser = TestProperties.getProperty( "testdb.postgis.user" );
        String jdbcPass = TestProperties.getProperty( "testdb.postgis.pass" );
        if ( jdbcURL == null ) {
            return;
        }

        ConnectionManager.addConnection( "testconn", jdbcURL, jdbcUser, jdbcPass, 1, 10 );
        SQLFeatureStoreProvider provider = new SQLFeatureStoreProvider();
        FeatureStore fs = provider.create( new URL(
                                                    "file:/home/schneider/.deegree/deegree-inspire-node-1.1/datasources/feature/inspire-au.xml" ) );

        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( new FileOutputStream(
                                                                                                                "/tmp/out.xml" ) );
        xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
        GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( GML_32, xmlWriter );
        gmlWriter.setNamespaceBindings( fs.getSchema().getNamespaceBindings() );

        QName countryName = QName.valueOf( "{urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0}AdministrativeUnit" );
        Query query = new Query( countryName, null, -1, -1, -1.0 );
        FeatureInputStream rs = fs.query( query );
        gmlWriter.write( rs.iterator().next() );
        gmlWriter.close();
    }

    @Test
    public void testInspireAUEnvelope()
                            throws Throwable {

        AppSchema appSchema = getInspireSchemaAU();
        if ( appSchema == null ) {
            return;
        }

        String jdbcURL = TestProperties.getProperty( "testdb.postgis.url" );
        String jdbcUser = TestProperties.getProperty( "testdb.postgis.user" );
        String jdbcPass = TestProperties.getProperty( "testdb.postgis.pass" );
        if ( jdbcURL == null ) {
            return;
        }

        ConnectionManager.addConnection( "testconn", jdbcURL, jdbcUser, jdbcPass, 1, 10 );
        SQLFeatureStoreProvider provider = new SQLFeatureStoreProvider();
        FeatureStore fs = provider.create( new URL(
                                                    "file:/home/schneider/.deegree/inspire-test/datasources/feature/inspire-au.xml" ) );
        QName countryName = QName.valueOf( "{urn:x-inspire:specification:gmlas:AdministrativeUnits:3.0}AdministrativeUnit" );
        Envelope env = fs.getEnvelope( countryName );
    }

    @Test
    public void testInstantiation()
                            throws FeatureStoreException, ResourceInitException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
                                             "deegreetest", "deegreetest", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
            SQLFeatureStore fs = (SQLFeatureStore) workspace.getSubsystemManager( FeatureStoreManager.class ).create( "philosopher",
                                                                                                                      configURL );
            fs.init( workspace );

            AppSchema schema = fs.getSchema();
            Assert.assertEquals( 4, schema.getFeatureTypes().length );

            FeatureType ft = schema.getFeatureTypes()[0];
            System.out.println( ft );

            QName countryName = QName.valueOf( "{http://www.deegree.org/app}Country" );
            Envelope env = fs.getEnvelope( countryName );
            System.out.println( env );
        }
    }

    @Test
    public void testInstantiationInspire()
                            throws ResourceInitException {

        if ( enable ) {
            ConnectionManager.addConnection( "inspire", "jdbc:postgresql://macchiato:5432/inspire", "postgres",
                                             "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "inspire-hybrid.xml" );
            DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
            SQLFeatureStore fs = (SQLFeatureStore) workspace.getSubsystemManager( FeatureStoreManager.class ).create( "inspire-hybrid",
                                                                                                                      configURL );
            fs.init( workspace );

            MappedAppSchema schema = fs.getSchema();
            Assert.assertEquals( 75, schema.getFeatureTypes().length );

            FeatureType ft = schema.getFeatureType( QName.valueOf( "{urn:x-inspire:specification:gmlas:Addresses:3.0}Address" ) );
            Assert.assertNotNull( ft );
            Assert.assertEquals( 13, ft.getPropertyDeclarations().size() );
            FeatureTypeMapping mapping = schema.getFtMapping( ft.getName() );
            Assert.assertNotNull( mapping );
        }
    }

    @Test
    public void testInspireDDL()
                            throws ResourceInitException, MalformedURLException {

        if ( enable ) {
            ConnectionManager.addConnection( "inspire", "jdbc:postgresql://macchiato:5432/inspire", "postgres",
                                             "postgres", 1, 10 );

            URL configURL = new URL(
                                     "file:/home/schneider/.deegree/deegree-inspire-node-1.1/datasources/feature/inspire-au.xml" );

            SQLFeatureStoreProvider provider = new SQLFeatureStoreProvider();
            SQLFeatureStore fs = (SQLFeatureStore) provider.create( configURL );
            try {
                fs.init( null );
            } catch ( Throwable t ) {
                t.printStackTrace();
            }

            String[] createStmts = DDLCreator.newInstance( fs.getSchema(), fs.getDialect() ).getDDL();

            Connection conn = null;
            Statement stmt = null;
            try {
                conn = ConnectionManager.getConnection( "inspire" );
                stmt = conn.createStatement();
                for ( String string : createStmts ) {
                    System.out.println( string );
                    stmt.execute( string );
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            } finally {
                JDBCUtils.close( conn );
            }
        }
    }

    @Test
    public void testInsertInspireAddresses()
                            throws FeatureStoreException, XMLStreamException, FactoryConfigurationError, IOException,
                            XMLParsingException, UnknownCRSException, ResourceInitException {
        if ( enable ) {
            ConnectionManager.addConnection( "inspire", "jdbc:postgresql://macchiato:5432/inspire", "postgres",
                                             "postgres", 1, 10 );
            URL configURL = PostGISFeatureStoreTst.class.getResource( "inspire-hybrid.xml" );
            DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
            SQLFeatureStore fs = (SQLFeatureStore) workspace.getSubsystemManager( FeatureStoreManager.class ).create( "inspire-hybrid",
                                                                                                                      configURL );

            URL datasetURL = PostGISFeatureStoreTst.class.getResource( "../../../gml/feature/testdata/features/inspire_addresses1.gml" );
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, datasetURL );
            gmlReader.setApplicationSchema( fs.getSchema() );
            gmlReader.getIdContext().resolveLocalRefs();

            StreamFeatureCollection fc = gmlReader.readFeatureCollectionStream();
            FeatureStoreTransaction ta = fs.acquireTransaction();
            Feature f = null;
            // while ((f = fc.read()) != null) {
            // ta.performInsert( f, IDGenMode.GENERATE_NEW );
            // }
            ta.commit();
        }
    }

    @Test
    public void testSimpleFts()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException {

        // String jdbcURL = CoreTstProperties.getProperty( "postgis_simple_feature_db" );
        // String jdbcUser = CoreTstProperties.getProperty( "postgis_simple_feature_user" );
        // String jdbcPass = CoreTstProperties.getProperty( "postgis_simple_feature_pass" );
        // if ( jdbcURL == null ) {
        // return;
        // }
        //
        // try {
        // ConnectionManager.addConnection( "simple-feature", jdbcURL, jdbcUser, jdbcPass, 1, 10 );
        // URL configURL = this.getClass().getResource( "simple_feature.xml" );
        // PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
        // fs.init();
        //
        // QName ftName = QName.valueOf( "{http://www.deegree.org/app}Schleusen" );
        // TypeName[] typeNames = new TypeName[] { new TypeName( ftName, null ) };
        // Query query = new Query( typeNames, null, null, null, null );
        // FeatureResultSet rs = fs.query( query );
        // FeatureCollection fc = rs.toCollection();
        // print( fc );
        //
        // } finally {
        // // ConnectionManager.destroy();
        // }
    }

    @Test
    public void testQueryCountry()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException,
                            ResourceInitException {

        String jdbcURL = TestProperties.getProperty( "postgis_philosopher_db" );
        String jdbcUser = TestProperties.getProperty( "postgis_philosopher_user" );
        String jdbcPass = TestProperties.getProperty( "postgis_philosopher_pass" );
        if ( jdbcURL == null ) {
            return;
        }

        try {
            ConnectionManager.addConnection( "philosopher-db", jdbcURL, jdbcUser, jdbcPass, 1, 10 );
            URL configURL = this.getClass().getResource( "philosopher.xml" );
            DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
            SQLFeatureStore fs = (SQLFeatureStore) workspace.getSubsystemManager( FeatureStoreManager.class ).create( "philosopher",
                                                                                                                      configURL );
            fs.init( workspace );

            QName ftName = QName.valueOf( "{http://www.deegree.org/app}Country" );
            TypeName[] typeNames = new TypeName[] { new TypeName( ftName, null ) };
            Query query = new Query( typeNames, null, null, null, null );
            FeatureInputStream rs = fs.query( query );
            FeatureCollection fc = rs.toCollection();
            print( fc );
        } finally {
            ConnectionManager.destroy( "philosopher-db" );
        }
    }

    @Test
    public void testQueryCountryWithFilter()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException,
                            ResourceInitException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://192.168.1.2:5432/deegreetest",
                                             "postgres", "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
            SQLFeatureStore fs = (SQLFeatureStore) workspace.getSubsystemManager( FeatureStoreManager.class ).create( "philosopher",
                                                                                                                      configURL );
            fs.init( workspace );

            TypeName[] typeNames = new TypeName[] { new TypeName(
                                                                  QName.valueOf( "{http://www.deegree.org/app}Country" ),
                                                                  null ) };
            ValueReference propName = new ValueReference( QName.valueOf( "{http://www.deegree.org/app}name" ) );
            Literal literal = new Literal( "United Kingdom" );

            PropertyIsEqualTo propIsEqualTo = new PropertyIsEqualTo( propName, literal, false, null );
            Filter filter = new OperatorFilter( propIsEqualTo );
            Query query = new Query( typeNames, filter, null, null, null );
            FeatureInputStream rs = fs.query( query );
            try {
                FeatureCollection fc = rs.toCollection();
                XMLStreamWriter xmlStream = new IndentingXMLStreamWriter(
                                                                          XMLOutputFactory.newInstance().createXMLStreamWriter( System.out ) );
                GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, xmlStream );
                gmlStream.write( fc );
                gmlStream.close();
            } finally {
                rs.close();
            }
        }
    }

    @Test
    public void testQueryPlace()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException,
                            ResourceInitException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
                                             "deegreetest", "deegreetest", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
            SQLFeatureStore fs = (SQLFeatureStore) workspace.getSubsystemManager( FeatureStoreManager.class ).create( "philosopher",
                                                                                                                      configURL );
            fs.init( workspace );

            TypeName[] typeNames = new TypeName[] { new TypeName( QName.valueOf( "{http://www.deegree.org/app}Place" ),
                                                                  null ) };
            Query query = new Query( typeNames, null, null, null, null );
            FeatureInputStream rs = fs.query( query );
            try {
                FeatureCollection fc = rs.toCollection();
                XMLStreamWriter xmlStream = new IndentingXMLStreamWriter(
                                                                          XMLOutputFactory.newInstance().createXMLStreamWriter( System.out ) );
                GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, xmlStream );
                gmlStream.setRemoteXLinkTemplate( "http://bla?fid={}" );
                gmlStream.setInitialResolveState( new ResolveState( null, -1, 0, LOCAL, 0 ) );
                gmlStream.write( fc );
                gmlStream.close();
            } finally {
                rs.close();
            }
        }
    }

    @Test
    public void testQueryPhilosopher()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException, IOException,
                            ResourceInitException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/d3_philosopher",
                                             "postgres", "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();
            SQLFeatureStore fs = (SQLFeatureStore) workspace.getSubsystemManager( FeatureStoreManager.class ).create( "philosopher",
                                                                                                                      configURL );
            fs.init( workspace );

            Filter filter = parse( "filter1.xml" );
            System.out.println( filter );

            TypeName[] typeNames = new TypeName[] { new TypeName(
                                                                  QName.valueOf( "{http://www.deegree.org/app}Philosopher" ),
                                                                  null ) };
            Query query = new Query( typeNames, filter, null, null, null );
            FeatureInputStream rs = fs.query( query );
            FeatureCollection fc = rs.toCollection();

            print( fc );
        }
    }

    private void print( FeatureCollection fc )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        XMLOutputFactory xmlFac = XMLOutputFactory.newInstance();
        xmlFac.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, true );
        XMLStreamWriter xmlStream = new IndentingXMLStreamWriter( xmlFac.createXMLStreamWriter( System.out ) );
        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, xmlStream );
        gmlStream.setRemoteXLinkTemplate( "http://bla?fid={}" );
        gmlStream.setInitialResolveState( new ResolveState( null, -1, 0, LOCAL, 0 ) );
        gmlStream.write( fc );
        gmlStream.close();
    }

    private Filter parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL url = PostGISFeatureStoreTst.class.getResource( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        xmlStream.nextTag();
        return Filter110XMLDecoder.parse( xmlStream );
    }

    private AppSchema getInspireSchemaAU()
                            throws MalformedURLException, ClassCastException, UnsupportedEncodingException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {
        return null;
        // File addressesFile = new File(
        // "/home/schneider/.deegree/deegree-workspace-inspire-3.1-pre4-SNAPSHOT/appschemas/inspire/annex1",
        // "AdministrativeUnits.xsd" );
        // URL url = addressesFile.toURI().toURL();
        // ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( GML_32, null, addressesFile );
        // return decoder.extractFeatureTypeSchema();
    }
}