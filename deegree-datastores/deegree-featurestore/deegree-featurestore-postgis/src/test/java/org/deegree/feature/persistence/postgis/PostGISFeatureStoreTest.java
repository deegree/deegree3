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
import org.deegree.CoreTstProperties;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.StreamFeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.deegree.protocol.wfs.getfeature.TypeName;
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
public class PostGISFeatureStoreTest {

    private static Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreTest.class );

    private static final boolean enable = false;

    @Test
    public void testMappingInspireAU()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, IOException, XMLStreamException, FactoryConfigurationError,
                            FeatureStoreException {

        ApplicationSchema appSchema = getInspireSchemaAU();
        if ( appSchema == null ) {
            return;
        }

        AppSchemaMapper mapper = new AppSchemaMapper( appSchema );
        MappedApplicationSchema mappedSchema = new AppSchemaMapper( appSchema ).getMappedSchema();

        PostGISFeatureStoreConfigWriter configWriter = new PostGISFeatureStoreConfigWriter( mappedSchema );
        File file = File.createTempFile( "inspire-au", ".xml" );
        FileOutputStream fos = new FileOutputStream( file );
        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( fos );
        xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
        configWriter.writeConfig( xmlWriter, "EPSG:4258", null, "testconn", Collections.singletonList( "bla.xsd" ) );
        xmlWriter.close();
        IOUtils.closeQuietly( fos );
        System.out.println( "Wrote to file " + file );

//        String[] createStmts = new PostGISFeatureStoreProvider().getDDL( file.toURI().toURL() );
//        for ( String stmt : createStmts ) {
//            System.out.println( stmt );
//        }
    }

    @Test
    public void testInstantiation()
                            throws FeatureStoreException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
                                             "deegreetest", "deegreetest", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            ApplicationSchema schema = fs.getSchema();
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
                            throws FeatureStoreException {

        if ( enable ) {
            ConnectionManager.addConnection( "inspire", "jdbc:postgresql://macchiato:5432/inspire", "postgres",
                                             "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "inspire-hybrid.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            MappedApplicationSchema schema = fs.getSchema();
            Assert.assertEquals( 75, schema.getFeatureTypes().length );

            FeatureType ft = schema.getFeatureType( QName.valueOf( "{urn:x-inspire:specification:gmlas:Addresses:3.0}Address" ) );
            Assert.assertNotNull( ft );
            Assert.assertEquals( 13, ft.getPropertyDeclarations().size() );
            FeatureTypeMapping mapping = schema.getMapping( ft.getName() );
            Assert.assertNotNull( mapping );

            Mapping propMapping = mapping.getMapping( QName.valueOf( "{urn:x-inspire:specification:gmlas:Addresses:3.0}inspireId" ) );
            System.out.println( propMapping );
        }
    }

    @Test
    public void testInspireDDL()
                            throws FeatureStoreException, MalformedURLException {

        if ( enable ) {
            ConnectionManager.addConnection( "inspire", "jdbc:postgresql://macchiato:5432/inspire", "postgres",
                                             "postgres", 1, 10 );

            URL configURL = new URL( "file:/tmp/config.xml" );
            String[] createStmts = new PostGISFeatureStoreProvider().getDDL( configURL );

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
                            XMLParsingException, UnknownCRSException {
        if ( enable ) {
            ConnectionManager.addConnection( "inspire", "jdbc:postgresql://macchiato:5432/inspire", "postgres",
                                             "postgres", 1, 10 );
            URL configURL = PostGISFeatureStoreTest.class.getResource( "inspire-hybrid.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );

            URL datasetURL = PostGISFeatureStoreTest.class.getResource( "../../../gml/feature/testdata/features/inspire_addresses1.gml" );
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, datasetURL );
            gmlReader.setApplicationSchema( fs.getSchema() );
            gmlReader.getIdContext().resolveLocalRefs();

            StreamFeatureCollection fc = gmlReader.readStreamFeatureCollection();
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
                            FactoryConfigurationError, UnknownCRSException, TransformationException {

        String jdbcURL = CoreTstProperties.getProperty( "postgis_philosopher_db" );
        String jdbcUser = CoreTstProperties.getProperty( "postgis_philosopher_user" );
        String jdbcPass = CoreTstProperties.getProperty( "postgis_philosopher_pass" );
        if ( jdbcURL == null ) {
            return;
        }

        try {
            ConnectionManager.addConnection( "philosopher-db", jdbcURL, jdbcUser, jdbcPass, 1, 10 );
            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            QName ftName = QName.valueOf( "{http://www.deegree.org/app}Country" );
            TypeName[] typeNames = new TypeName[] { new TypeName( ftName, null ) };
            Query query = new Query( typeNames, null, null, null, null );
            FeatureResultSet rs = fs.query( query );
            FeatureCollection fc = rs.toCollection();
            print( fc );
        } finally {
            ConnectionManager.destroy();
        }
    }

    @Test
    public void testQueryCountryWithFilter()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://192.168.1.2:5432/deegreetest",
                                             "postgres", "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            TypeName[] typeNames = new TypeName[] { new TypeName(
                                                                  QName.valueOf( "{http://www.deegree.org/app}Country" ),
                                                                  null ) };
            PropertyName propName = new PropertyName( QName.valueOf( "{http://www.deegree.org/app}name" ) );
            Literal literal = new Literal( "United Kingdom" );

            PropertyIsEqualTo propIsEqualTo = new PropertyIsEqualTo( propName, literal, false );
            Filter filter = new OperatorFilter( propIsEqualTo );
            Query query = new Query( typeNames, filter, null, null, null );
            FeatureResultSet rs = fs.query( query );
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
                            FactoryConfigurationError, UnknownCRSException, TransformationException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
                                             "deegreetest", "deegreetest", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            TypeName[] typeNames = new TypeName[] { new TypeName( QName.valueOf( "{http://www.deegree.org/app}Place" ),
                                                                  null ) };
            Query query = new Query( typeNames, null, null, null, null );
            FeatureResultSet rs = fs.query( query );
            try {
                FeatureCollection fc = rs.toCollection();
                XMLStreamWriter xmlStream = new IndentingXMLStreamWriter(
                                                                          XMLOutputFactory.newInstance().createXMLStreamWriter( System.out ) );
                GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, xmlStream );
                gmlStream.setRemoteXLinkTemplate( "http://bla?fid={}" );
                gmlStream.setXLinkDepth( -1 );
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
                            FactoryConfigurationError, UnknownCRSException, TransformationException, IOException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/d3_philosopher",
                                             "postgres", "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            Filter filter = parse( "filter1.xml" );
            System.out.println( filter );

            TypeName[] typeNames = new TypeName[] { new TypeName(
                                                                  QName.valueOf( "{http://www.deegree.org/app}Philosopher" ),
                                                                  null ) };
            Query query = new Query( typeNames, filter, null, null, null );
            FeatureResultSet rs = fs.query( query );
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
        gmlStream.setXLinkDepth( -1 );
        gmlStream.write( fc );
        gmlStream.close();
    }

    private Filter parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL url = PostGISFeatureStoreTest.class.getResource( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        xmlStream.nextTag();
        return Filter110XMLDecoder.parse( xmlStream );
    }

    private ApplicationSchema getInspireSchemaAU()
                            throws MalformedURLException, ClassCastException, UnsupportedEncodingException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        String inspireDir = TstSettings.getProperty( "inspire_annex1_schemas_root" );
        if ( inspireDir == null ) {
            return null;
        }
        File addressesFile = new File( inspireDir, "AdministrativeUnits.xsd" );
        URL url = addressesFile.toURI().toURL();
        ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( GML_32, null, addressesFile );
        return decoder.extractFeatureTypeSchema();
    }
}