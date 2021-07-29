package org.deegree.services.wfs.format.geojson;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.net.URL;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.geojson.GeoJsonWriter;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeoJsonFeatureWriterTest {

    @Test
    public void testWrite()
                            throws Exception {
        StringWriter featureAsJson = new StringWriter();
        GeoJsonWriter geoJsonFeatureWriter = new GeoJsonWriter( featureAsJson, null );
        Feature cadastralZoning = parseFeature( "CadastralZoning.gml" );

        geoJsonFeatureWriter.startFeatureCollection();
        geoJsonFeatureWriter.write( cadastralZoning );
        geoJsonFeatureWriter.endFeatureCollection();

        String featureCollection = featureAsJson.toString();

        assertThat( featureCollection, isJson() );
        assertThat( featureCollection, hasJsonPath( "$.type", is( "FeatureCollection" ) ) );
        assertThat( featureCollection, hasJsonPath( "$.features.length()", is( 1 ) ) );
        assertThat( featureCollection, hasJsonPath( "$.features[0].properties.originalMapScaleDenominator", is( 10 ) ) );
        assertThat( featureCollection, hasNoJsonPath( "$.features[0].srsName" ) );
    }

    @Test
    public void testWriteWithCrs()
                            throws Exception {
        StringWriter featureAsJson = new StringWriter();
        ICRS crs = CRSManager.lookup( "EPSG:25832" );
        GeoJsonWriter geoJsonFeatureWriter = new GeoJsonWriter( featureAsJson, crs );
        Feature cadastralZoning = parseFeature( "CadastralZoning.gml" );

        geoJsonFeatureWriter.startFeatureCollection();
        geoJsonFeatureWriter.write( cadastralZoning );
        geoJsonFeatureWriter.endFeatureCollection();

        String featureCollection = featureAsJson.toString();
        assertThat( featureCollection, isJson() );
        assertThat( featureCollection, hasJsonPath( "$.type", is( "FeatureCollection" ) ) );
        assertThat( featureCollection, hasJsonPath( "$.features.length()", is( 1 ) ) );
        assertThat( featureCollection, hasJsonPath( "$.features[0].properties.originalMapScaleDenominator", is( 10 ) ) );
        assertThat( featureCollection, hasJsonPath( "$.features[0].srsName", is( crs.getAlias() ) ) );
    }

    private Feature parseFeature( String resourceName )
                            throws Exception {
        URL testResource = GeoJsonFeatureWriterTest.class.getResource( resourceName );
        GMLStreamReader reader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, testResource );
        return reader.readFeature();
    }

}
