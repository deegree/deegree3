package org.deegree.services.wfs.format.geojson;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.net.URL;

import org.deegree.feature.Feature;
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
        GeoJsonWriter geoJsonFeatureWriter = new GeoJsonWriter( featureAsJson );
        Feature cadastralZoning = parseFeature( "CadastralZoning.gml" );

        geoJsonFeatureWriter.startFeatureCollection();
        geoJsonFeatureWriter.write( cadastralZoning );
        geoJsonFeatureWriter.endFeatureCollection();

        String featureCollection = featureAsJson.toString();
        assertThat( featureCollection, isJson() );
        assertThat( featureCollection, hasJsonPath( "$.type", is( "FeatureCollection" ) ) );
        assertThat( featureCollection, hasJsonPath( "$.features.length()", is( 1 ) ) );
        assertThat( featureCollection, hasJsonPath( "$.features[0].properties.originalMapScaleDenominator", is( 10 ) ) );
    }

    private Feature parseFeature( String resourceName )
                            throws Exception {
        URL testResource = GeoJsonFeatureWriterTest.class.getResource( resourceName );
        GMLStreamReader reader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, testResource );
        return reader.readFeature();
    }

}
