package org.deegree.feature.persistence.sql.mapper;

import org.apache.commons.io.IOUtils;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AppSchemaMapperTest {

    private static final QName FEATURE_A = new QName( "http://test.de/schema", "FeatureA" );

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    private File schemaWithCycle;

    private File schemaWithCycle2;

    private File schemaWithCycle3;

    @Before public void copySchemas()
                            throws IOException {
        InputStream resourceAsStream = AppSchemaMapperTest.class.getResourceAsStream( "schemaWithCycle.xsd" );
        this.schemaWithCycle = folder.newFile( "schemaWithCycle.xsd" );
        IOUtils.copy( resourceAsStream, new FileOutputStream( schemaWithCycle ) );

        InputStream resourceAsStream2 = AppSchemaMapperTest.class.getResourceAsStream( "schemaWithCycle_2.xsd" );
        this.schemaWithCycle2 = folder.newFile( "schemaWithCycle_2.xsd" );
        IOUtils.copy( resourceAsStream2, new FileOutputStream( schemaWithCycle2 ) );

        InputStream resourceAsStream3 = AppSchemaMapperTest.class.getResourceAsStream( "schemaWithCycle_3.xsd" );
        this.schemaWithCycle3 = folder.newFile( "schemaWithCycle_3.xsd" );
        IOUtils.copy( resourceAsStream3, new FileOutputStream( schemaWithCycle3 ) );
    }

    @Test public void testWithDepth0()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, String.valueOf( "0" ), DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 0 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();
        assertThat( mappings.size(), is( 5 ) );

        // Depth 0
        CompoundMapping featureCDepth0 = getFeatureC( mappings );
        List<Mapping> featureCDepth0Mapping = featureCDepth0.getParticles();
        assertThat( featureCDepth0Mapping.size(), is( 2 ) );

        // Depth 1
        CompoundMapping featureCDepth1 = getFeatureC( featureCDepth0Mapping );
        assertThat( featureCDepth1, is( nullValue() ) );
    }

    @Test public void testWithDepth1_default()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, String.valueOf( "0" ), DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();
        assertThat( mappings.size(), is( 5 ) );

        // Depth 0
        CompoundMapping featureCDepth0 = getFeatureC( mappings );
        List<Mapping> featureCDepth0Mapping = featureCDepth0.getParticles();
        assertThat( featureCDepth0Mapping.size(), is( 3 ) );

        // Depth 1
        CompoundMapping featureCDepth1 = getFeatureC( featureCDepth0Mapping );
        List<Mapping> featureCDepth1Mapping = featureCDepth1.getParticles();
        assertThat( featureCDepth1Mapping.size(), is( 2 ) );

        // Depth 2
        CompoundMapping featureCDepth2 = getFeatureC( featureCDepth1Mapping );
        assertThat( featureCDepth2, is( nullValue() ) );
    }

    @Test public void testWithDepth2()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, String.valueOf( "0" ), DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();

        assertThat( mappings.size(), is( 5 ) );

        // Depth 0
        CompoundMapping featureCDepth0 = getFeatureC( mappings );
        List<Mapping> featureCDepth0Mapping = featureCDepth0.getParticles();
        assertThat( featureCDepth0Mapping.size(), is( 3 ) );

        // Depth 1
        CompoundMapping featureCDepth1 = getFeatureC( featureCDepth0Mapping );
        List<Mapping> featureCDepth1Mapping = featureCDepth1.getParticles();
        assertThat( featureCDepth1Mapping.size(), is( 3 ) );

        // Depth 2
        CompoundMapping featureCDepth2 = getFeatureC( featureCDepth1Mapping );
        List<Mapping> featureCDepth2Mapping = featureCDepth2.getParticles();
        assertThat( featureCDepth2Mapping.size(), is( 2 ) );

        // Depth 3
        CompoundMapping featureCDepth3 = getFeatureC( featureCDepth2Mapping );
        assertThat( featureCDepth3, is( nullValue() ) );
    }

    @Test public void test_assertNoLoop_Schema2()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle2 );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, String.valueOf( "0" ), DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );
    }

    @Test public void test_assertNoLoop_Schema3()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle3 );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, String.valueOf( "0" ), DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );
    }

    private CompoundMapping getFeatureC( List<Mapping> mappings ) {
        for ( Mapping mapping : mappings ) {
            if ( mapping.getPath().getAsQName().getLocalPart().equals( "FeatureC" ) ) {
                return (CompoundMapping) mapping;
            }
        }
        return null;
    }
}
