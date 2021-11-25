package org.deegree.feature.persistence.sql.mapper;

import org.apache.commons.io.IOUtils;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AppSchemaMapperTest {

    private static final QName FEATURE_A = new QName( "http://test.de/schema", "FeatureA" );

    private static final QName FEATURE_B = new QName( "http://test.de/schema", "FeatureB" );

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File schemaWithSimpleCycle;

    private File schemaWithCycle1;

    private File schemaWithCycle2;

    private File schemaWithCycle3;

    private File schemaWithTwoCycles;

    private File schemaWithTwoSelfDependentCycles;

    private File schemaForSampleValues;

    @Before
    public void copySchemas()
                            throws IOException {
        this.schemaWithCycle1 = copyToTmpFolder( "schemaWithCycle1.xsd" );
        this.schemaWithCycle2 = copyToTmpFolder( "schemaWithCycle2.xsd" );
        this.schemaWithCycle3 = copyToTmpFolder( "schemaWithCycle3.xsd" );
        this.schemaWithSimpleCycle = copyToTmpFolder( "schemaWithSimpleCycle.xsd" );
        this.schemaWithTwoCycles = copyToTmpFolder( "schemaWithTwoCycles.xsd" );
        this.schemaWithTwoSelfDependentCycles = copyToTmpFolder( "schemaWithTwoSelfDependentCycles.xsd" );
        this.schemaForSampleValues = copyToTmpFolder( "schemaForSampleValues.xsd" );
    }

    @Test
    public void testWithSimpleCycle_Depth0()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithSimpleCycle );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
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

    @Test
    public void testWithSimpleCycle_Depth0_default()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithSimpleCycle );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
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
        assertThat( featureCDepth0Mapping.size(), is( 2 ) );

        // Depth 1
        CompoundMapping featureCDepth1 = getFeatureC( featureCDepth0Mapping );
        assertThat( featureCDepth1, is( nullValue() ) );
    }

    @Test
    public void testWithSimpleCycle_Depth1()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithSimpleCycle );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 1 );

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

    @Test
    public void testWithSimpleCycle_Depth2()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithSimpleCycle );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
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

    @Test
    public void testWithCycle1_Depth2()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle1 );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();

        assertThat( mappings.size(), is( 5 ) );

        // Depth 0 - Cycle 0
        CompoundMapping featureCDepth0 = getFeatureC( mappings );
        List<Mapping> featureCDepth0Mapping = featureCDepth0.getParticles();
        assertThat( featureCDepth0Mapping.size(), is( 3 ) );
        CompoundMapping featureDDepth0 = getFeatureD( mappings );
        assertThat( featureDDepth0, is( nullValue() ) );

        // Depth 1 - Cycle 1
        CompoundMapping featureCDepth1 = getFeatureC( featureCDepth0Mapping );
        assertThat( featureCDepth1, is( nullValue() ) );
        CompoundMapping featureDDepth1 = getFeatureD( featureCDepth0Mapping );
        List<Mapping> featureDDepth1Mapping = featureDDepth1.getParticles();
        assertThat( featureDDepth1Mapping.size(), is( 3 ) );

        // Depth 2 - Cycle 2
        CompoundMapping featureCDepth2 = getFeatureC( featureDDepth1Mapping );
        List<Mapping> featureCDepth2Mapping = featureCDepth2.getParticles();
        assertThat( featureCDepth2Mapping.size(), is( 3 ) );
        CompoundMapping featureDDepth2 = getFeatureD( mappings );
        assertThat( featureDDepth2, is( nullValue() ) );

        // Depth 3 - End
        CompoundMapping featureCDepth3 = getFeatureC( featureCDepth2Mapping );
        assertThat( featureCDepth3, is( nullValue() ) );
        CompoundMapping featureDDepth3 = getFeatureD( featureCDepth2Mapping );
        List<Mapping> featureDDepth3Mapping = featureDDepth3.getParticles();
        assertThat( featureDDepth3Mapping.size(), is( 3 ) );
    }

    @Test
    public void testWithCycle1_assertNoLoop()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle1 );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );
    }

    @Test
    public void testWithCycle2_assertNoLoop()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle2 );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );
    }

    @Test
    public void testWithCycle3_assertNoLoop()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithCycle3 );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );
    }

    @Test
    public void testWithTwoCycles()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithTwoCycles );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 2 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();
        assertThat( mappings.size(), is( 6 ) );

        // Depth 0 - Cycle 0
        CompoundMapping featureCDepth0 = getFeatureC( mappings );
        List<Mapping> featureCDepth0Mapping = featureCDepth0.getParticles();
        assertThat( featureCDepth0Mapping.size(), is( 3 ) );

        CompoundMapping featureEDepth0 = getFeatureE( mappings );
        List<Mapping> featureEDepth0Mapping = featureEDepth0.getParticles();
        assertThat( featureEDepth0Mapping.size(), is( 3 ) );

        // Depth 1
        CompoundMapping featureDDepth1 = getFeatureD( featureCDepth0Mapping );
        List<Mapping> featureDDepth1Mapping = featureDDepth1.getParticles();
        assertThat( featureDDepth1Mapping.size(), is( 3 ) );

        CompoundMapping featureFDepth1 = getFeatureF( featureEDepth0Mapping );
        List<Mapping> featureFDepth1Mapping = featureFDepth1.getParticles();
        assertThat( featureFDepth1Mapping.size(), is( 3 ) );

        // Depth 2 - Cycle 1
        CompoundMapping featureCDepth2 = getFeatureC( mappings );
        List<Mapping> featureCDepth2Mapping = featureCDepth2.getParticles();
        assertThat( featureCDepth2Mapping.size(), is( 3 ) );

        CompoundMapping featureEDepth2 = getFeatureE( mappings );
        List<Mapping> featureEDepth2Mapping = featureEDepth2.getParticles();
        assertThat( featureEDepth2Mapping.size(), is( 3 ) );

        // Depth 3
        CompoundMapping featureDDepth3 = getFeatureD( featureCDepth2Mapping );
        List<Mapping> featureDDepth3Mapping = featureDDepth3.getParticles();
        assertThat( featureDDepth3Mapping.size(), is( 3 ) );

        CompoundMapping featureFDepth3 = getFeatureF( featureEDepth2Mapping );
        List<Mapping> featureFDepth3Mapping = featureFDepth3.getParticles();
        assertThat( featureFDepth3Mapping.size(), is( 3 ) );

        // Depth 4 - Cycle 1
        CompoundMapping featureCDepth4 = getFeatureC( featureDDepth3Mapping );
        List<Mapping> featureCDepth4Mapping = featureCDepth4.getParticles();
        assertThat( featureCDepth4Mapping.size(), is( 3 ) );

        CompoundMapping featureEDepth4 = getFeatureE( featureFDepth3Mapping );
        List<Mapping> featureEDepth4Mapping = featureEDepth4.getParticles();
        assertThat( featureEDepth4Mapping.size(), is( 3 ) );

        // Depth 5
        CompoundMapping featureDDepth5 = getFeatureD( featureCDepth4Mapping );
        List<Mapping> featureDDepth5Mapping = featureDDepth5.getParticles();
        assertThat( featureDDepth5Mapping.size(), is( 3 ) );

        CompoundMapping featureFDepth5 = getFeatureF( featureEDepth4Mapping );
        List<Mapping> featureFDepth5Mapping = featureFDepth5.getParticles();
        assertThat( featureFDepth5Mapping.size(), is( 3 ) );

        // Depth 6 - Cycle 2
        CompoundMapping featureCDepth6 = getFeatureC( featureDDepth5Mapping );
        List<Mapping> featureCDepth6Mapping = featureCDepth6.getParticles();
        assertThat( featureCDepth6Mapping.size(), is( 3 ) );

        CompoundMapping featureEDepth6 = getFeatureE( featureFDepth5Mapping );
        List<Mapping> featureEDepth6Mapping = featureEDepth6.getParticles();
        assertThat( featureEDepth6Mapping.size(), is( 3 ) );

        // Depth 7
        CompoundMapping featureDDepth7 = getFeatureD( featureCDepth6Mapping );
        List<Mapping> featureDDepth7Mapping = featureDDepth7.getParticles();
        assertThat( featureDDepth7Mapping.size(), is( 2 ) );

        CompoundMapping featureFDepth7 = getFeatureF( featureEDepth6Mapping );
        List<Mapping> featureFDepth7Mapping = featureFDepth7.getParticles();
        assertThat( featureFDepth7Mapping.size(), is( 2 ) );

        // Depth 8 - End
        assertThat( getFeatureD( featureDDepth7Mapping ), is( nullValue() ) );
        assertThat( getFeatureF( featureFDepth7Mapping ), is( nullValue() ) );
    }

    @Test
    public void testWithTwoSelfDependentCycles()
                            throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaWithTwoSelfDependentCycles );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 1 );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();
        assertThat( mappings.size(), is( 5 ) );

        // Depth 0 - Cycle 0
        CompoundMapping featureCDepth0 = getFeatureC( mappings );
        List<Mapping> featureCDepth0Mapping = featureCDepth0.getParticles();
        assertThat( featureCDepth0Mapping.size(), is( 4 ) );

        // Depth 1
        CompoundMapping featureDDepth1 = getFeatureD( featureCDepth0Mapping );
        List<Mapping> featureDDepth1Mapping = featureDDepth1.getParticles();
        assertThat( featureDDepth1Mapping.size(), is( 3 ) );

        CompoundMapping featureEDepth1 = getFeatureE( featureCDepth0Mapping );
        List<Mapping> featureEDepth1Mapping = featureEDepth1.getParticles();
        assertThat( featureEDepth1Mapping.size(), is( 3 ) );

        // Depth 2 - Cycle 1
        CompoundMapping featureCDepth2 = getFeatureC( featureDDepth1Mapping );
        List<Mapping> featureCDepth2Mapping = featureCDepth2.getParticles();
        assertThat( featureCDepth2Mapping.size(), is( 4 ) );

        CompoundMapping featureEDepth2 = getFeatureC( featureEDepth1Mapping );
        List<Mapping> featureEDepth2Mapping = featureEDepth2.getParticles();
        assertThat( featureEDepth2Mapping.size(), is( 4 ) );

        // Depth 3
        CompoundMapping featureDofCDepth2Depth3 = getFeatureD( featureCDepth2Mapping );
        List<Mapping> featureDofCDepth2Depth3Mapping = featureDofCDepth2Depth3.getParticles();
        assertThat( featureDofCDepth2Depth3Mapping.size(), is( 2 ) );

        CompoundMapping featureEofCDepth2Depth3 = getFeatureE( featureCDepth2Mapping );
        List<Mapping> featureEofCDepth2Depth3Mapping = featureEofCDepth2Depth3.getParticles();
        assertThat( featureEofCDepth2Depth3Mapping.size(), is( 2 ) );

        CompoundMapping featureDofEDepth2Depth3 = getFeatureD( featureEDepth2Mapping );
        List<Mapping> featureDofEDepth2Depth3Mapping = featureDofEDepth2Depth3.getParticles();
        assertThat( featureDofEDepth2Depth3Mapping.size(), is( 2 ) );

        CompoundMapping featureEofEDepth2Depth3 = getFeatureE( featureEDepth2Mapping );
        List<Mapping> featureEofEDepth2Depth3Mapping = featureEofEDepth2Depth3.getParticles();
        assertThat( featureEofEDepth2Depth3Mapping.size(), is( 2 ) );

        // Depth 4 - End
        assertThat( getFeatureC( featureDofCDepth2Depth3Mapping ), is( nullValue() ) );
        assertThat( getFeatureC( featureEofCDepth2Depth3Mapping ), is( nullValue() ) );
        assertThat( getFeatureC( featureDofEDepth2Depth3Mapping ), is( nullValue() ) );
        assertThat( getFeatureC( featureEofEDepth2Depth3Mapping ), is( nullValue() ) );
    }

    @Test
    public void testWithoutReferenceData()
                    throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaForSampleValues );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 0, null );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 2 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();
        assertThat( mappings.size(), is( 5 ) );
        assertThat( getPrimitive( mappings, "prop_A1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A3" ).getJoinedTable(), is( notNullValue() ) );
    }

    @Test
    public void testWithReferenceData()
                    throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaForSampleValues );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        QName featureTypeName = new QName( "http://test.de/schema", "FeatureA", "te" );
        ReferenceData referenceData = mock( ReferenceData.class );
        when( referenceData.shouldFeatureTypeMapped( featureTypeName ) ).thenReturn( true );
        QName propA1 = new QName( "http://test.de/schema", "prop_A1", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propA1 ) ) ).thenReturn( false );
        QName propA3 = new QName( "http://test.de/schema", "prop_A3", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propA3 ) ) ).thenReturn( true );

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 0, referenceData );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = featureA.getMappings();
        assertThat( mappings.size(), is( 5 ) );
        assertThat( getPrimitive( mappings, "prop_A1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A3" ).getJoinedTable(), is( nullValue() ) );
    }

    @Test
    public void testWithReferenceData_ComplexData_ComplexN()
                    throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaForSampleValues );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        QName featureTypeName = new QName( "http://test.de/schema", "FeatureA", "te" );
        ReferenceData referenceData = mock( ReferenceData.class );
        when( referenceData.shouldFeatureTypeMapped( featureTypeName ) ).thenReturn( true );
        QName propA1 = new QName( "http://test.de/schema", "prop_A1", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propA1 ) ) ).thenReturn( false );
        QName propA3 = new QName( "http://test.de/schema", "prop_A3", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propA3 ) ) ).thenReturn( true );
        QName propComplexA4 = new QName( "http://test.de/schema", "complex_A4", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propComplexA4 ) ) ).thenReturn( false );
        List<QName> complexA4_1 = new ArrayList<>();
        complexA4_1.add( new QName( "http://test.de/schema", "complex_A4", "te" ) );
        complexA4_1.add( new QName( "http://test.de/schema", "prop_A4_1", "te" ) );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, complexA4_1 ) ).thenReturn( false );
        List<QName> complexA4_2 = new ArrayList<>();
        complexA4_2.add( new QName( "http://test.de/schema", "complex_A4", "te" ) );
        complexA4_2.add( new QName( "http://test.de/schema", "prop_A4_3", "te" ) );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, complexA4_2 ) ).thenReturn( true );

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 0, referenceData );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping feature = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = feature.getMappings();
        assertThat( mappings.size(), is( 5 ) );
        assertThat( getPrimitive( mappings, "prop_A1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A3" ).getJoinedTable(), is( nullValue() ) );
        CompoundMapping complexMapping = getCompound( mappings, "complex_A4" );
        assertThat( complexMapping.getJoinedTable(), is( notNullValue() ) );
        List<Mapping> complexMappings = complexMapping.getParticles();
        assertThat( getPrimitive( complexMappings, "prop_A4_1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( complexMappings, "prop_A4_2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( complexMappings, "prop_A4_3" ).getJoinedTable(), is( nullValue() ) );
    }

    @Test
    public void testWithReferenceData_ComplexData_Complex1()
                    throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaForSampleValues );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        QName featureTypeName = new QName( "http://test.de/schema", "FeatureA", "te" );
        ReferenceData referenceData = mock( ReferenceData.class );
        when( referenceData.shouldFeatureTypeMapped( featureTypeName ) ).thenReturn( true );
        QName propA1 = new QName( "http://test.de/schema", "prop_A1", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propA1 ) ) ).thenReturn( false );
        QName propA3 = new QName( "http://test.de/schema", "prop_A3", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propA3 ) ) ).thenReturn( true );
        QName propComplexA4 = new QName( "http://test.de/schema", "complex_A4", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, Collections.singletonList( propComplexA4 ) ) ).thenReturn( true );
        List<QName> complexA4_1 = new ArrayList<>();
        complexA4_1.add( new QName( "http://test.de/schema", "complex_A4", "te" ) );
        complexA4_1.add( new QName( "http://test.de/schema", "prop_A4_1", "te" ) );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, complexA4_1 ) ).thenReturn( false );
        List<QName> complexA4_2 = new ArrayList<>();
        complexA4_2.add( new QName( "http://test.de/schema", "complex_A4", "te" ) );
        complexA4_2.add( new QName( "http://test.de/schema", "prop_A4_3", "te" ) );
        when( referenceData.hasZeroOrOneProperty( featureTypeName, complexA4_2 ) ).thenReturn( true );

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 0, referenceData );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 1 ) );

        FeatureTypeMapping feature = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappings = feature.getMappings();
        assertThat( mappings.size(), is( 5 ) );
        assertThat( getPrimitive( mappings, "prop_A1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappings, "prop_A3" ).getJoinedTable(), is( nullValue() ) );
        CompoundMapping complexMapping = getCompound( mappings, "complex_A4" );
        assertThat( complexMapping.getJoinedTable(), is( nullValue() ) );
        List<Mapping> complexMappings = complexMapping.getParticles();
        assertThat( getPrimitive( complexMappings, "prop_A4_1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( complexMappings, "prop_A4_2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( complexMappings, "prop_A4_3" ).getJoinedTable(), is( nullValue() ) );
    }

    @Test
    public void testWithReferenceData_ReferencedFeature_RefN()
                    throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaForSampleValues );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        QName featureTypeNameB = new QName( "http://test.de/schema", "FeatureB", "te" );
        ReferenceData referenceData = mock( ReferenceData.class );
        when( referenceData.shouldFeatureTypeMapped( featureTypeNameB ) ).thenReturn( true );
        QName propB1 = new QName( "http://test.de/schema", "prop_B1", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameB, Collections.singletonList( propB1 ) ) ).thenReturn( false );
        QName propB3 = new QName( "http://test.de/schema", "prop_B3", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameB, Collections.singletonList( propB3 ) ) ).thenReturn( true );
        QName propFfeatureA = new QName( "http://test.de/schema", "featureA", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameB, Collections.singletonList( propFfeatureA ) ) ).thenReturn( false );

        QName featureTypeNameA = new QName( "http://test.de/schema", "FeatureA", "te" );
        QName propA1 = new QName( "http://test.de/schema", "prop_A1", "te" );
        when( referenceData.shouldFeatureTypeMapped( featureTypeNameA ) ).thenReturn( true );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameA, Collections.singletonList( propA1 ) ) ).thenReturn( false );
        QName propA3 = new QName( "http://test.de/schema", "prop_A3", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameA, Collections.singletonList( propA3 ) ) ).thenReturn( true );

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 0, referenceData );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 2 ) );

        FeatureTypeMapping featureB = mappedSchema.getFtMapping( FEATURE_B );
        List<Mapping> mappingsB = featureB.getMappings();
        assertThat( mappingsB.size(), is( 5 ) );
        assertThat( getPrimitive( mappingsB, "prop_B1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappingsB, "prop_B2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappingsB, "prop_B3" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getCompound( mappingsB, "featureA" ).getJoinedTable(), is( notNullValue() ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappingsA = featureA.getMappings();
        assertThat( mappingsA.size(), is( 5 ) );
        assertThat( getPrimitive( mappingsA, "prop_A1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappingsA, "prop_A2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappingsA, "prop_A3" ).getJoinedTable(), is( nullValue() ) );
    }

    @Test
    public void testWithReferenceData_ReferencedFeature_Ref1()
                    throws Exception {
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaForSampleValues );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        QName featureTypeNameB = new QName( "http://test.de/schema", "FeatureB", "te" );
        ReferenceData referenceData = mock( ReferenceData.class );
        when( referenceData.shouldFeatureTypeMapped( featureTypeNameB ) ).thenReturn( true );
        QName propB1 = new QName( "http://test.de/schema", "prop_B1", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameB, Collections.singletonList( propB1 ) ) ).thenReturn( false );
        QName propB3 = new QName( "http://test.de/schema", "prop_B3", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameB, Collections.singletonList( propB3 ) ) ).thenReturn( true );
        QName propFfeatureA = new QName( "http://test.de/schema", "featureA", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameB, Collections.singletonList( propFfeatureA ) ) ).thenReturn( true );

        QName featureTypeNameA = new QName( "http://test.de/schema", "FeatureA", "te" );
        when( referenceData.shouldFeatureTypeMapped( featureTypeNameA ) ).thenReturn( true );
        QName propA1 = new QName( "http://test.de/schema", "prop_A1", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameA, Collections.singletonList( propA1 ) ) ).thenReturn( false );
        QName propA3 = new QName( "http://test.de/schema", "prop_A3", "te" );
        when( referenceData.hasZeroOrOneProperty( featureTypeNameA, Collections.singletonList( propA3 ) ) ).thenReturn( true );

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, "0", DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, geometryParams, 63, true, true, 0, referenceData );

        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        Map<QName, FeatureTypeMapping> ftMappings = mappedSchema.getFtMappings();
        assertThat( ftMappings.size(), is( 2 ) );

        FeatureTypeMapping featureB = mappedSchema.getFtMapping( FEATURE_B );
        List<Mapping> mappingsB = featureB.getMappings();
        assertThat( mappingsB.size(), is( 5 ) );
        assertThat( getPrimitive( mappingsB, "prop_B1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappingsB, "prop_B2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappingsB, "prop_B3" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getCompound( mappingsB, "featureA" ).getJoinedTable(), is( nullValue() ) );

        FeatureTypeMapping featureA = mappedSchema.getFtMapping( FEATURE_A );
        List<Mapping> mappingsA = featureA.getMappings();
        assertThat( mappingsA.size(), is( 5 ) );
        assertThat( getPrimitive( mappingsA, "prop_A1" ).getJoinedTable(), is( notNullValue() ) );
        assertThat( getPrimitive( mappingsA, "prop_A2" ).getJoinedTable(), is( nullValue() ) );
        assertThat( getPrimitive( mappingsA, "prop_A3" ).getJoinedTable(), is( nullValue() ) );
    }

    private CompoundMapping getFeatureC( List<Mapping> mappings ) {
        return getCompound( mappings, "FeatureC" );
    }

    private CompoundMapping getFeatureD( List<Mapping> mappings ) {
        return getCompound( mappings, "FeatureD" );
    }

    private CompoundMapping getFeatureE( List<Mapping> mappings ) {
        return getCompound( mappings, "FeatureE" );
    }

    private CompoundMapping getFeatureF( List<Mapping> mappings ) {
        return getCompound( mappings, "FeatureF" );
    }

    private CompoundMapping getCompound( List<Mapping> mappings, String name ) {
        for ( Mapping mapping : mappings ) {
            if ( mapping.getPath().getAsQName().getLocalPart().equals( name ) ) {
                return (CompoundMapping) mapping;
            }
        }
        return null;
    }

    private PrimitiveMapping getPrimitive( List<Mapping> mappings, String name ) {
        for ( Mapping mapping : mappings ) {
            if ( mapping.getPath().getAsQName().getLocalPart().equals( name ) ) {
                return (PrimitiveMapping) mapping;
            }
        }
        return null;
    }

    private File copyToTmpFolder( String resourceName )
                            throws IOException {
        InputStream resource = AppSchemaMapperTest.class.getResourceAsStream( resourceName );
        File schema = folder.newFile( resourceName );
        IOUtils.copy( resource, new FileOutputStream( schema ) );
        return schema;
    }

}
