package org.deegree.tools.featurestoresql.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.deegree.feature.Feature;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlReaderTest {

    @Test
    public void testRead()
                            throws Exception {
        GmlReader gmlReader = new GmlReader( null );
        Resource resource = new InputStreamResource( GmlReaderTest.class.getResourceAsStream( "cadastralparcels.xml" ) );
        gmlReader.setResource( resource );
        gmlReader.open( new ExecutionContext() );

        int numberOfFeatures = 0;
        Feature feature;
        do {
            feature = gmlReader.read();
            if ( feature != null ) {
                numberOfFeatures++;
            }
        } while ( feature != null );

        assertThat( numberOfFeatures, is( 5 ) );
    }

    @Test
    public void testRead_WFS_FeatureCollection()
                            throws Exception {
        GmlReader gmlReader = new GmlReader( null );
        Resource resource = new InputStreamResource(
                                                     GmlReaderTest.class.getResourceAsStream( "cadastralparcels_wfsFC.xml" ) );
        gmlReader.setResource( resource );
        gmlReader.open( new ExecutionContext() );

        int numberOfFeatures = 0;
        Feature feature;
        do {
            feature = gmlReader.read();
            if ( feature != null ) {
                numberOfFeatures++;
            }
        } while ( feature != null );

        assertThat( numberOfFeatures, is( 5 ) );
    }

    @Test
    public void testRead_WFS_20_FeatureCollection()
                            throws Exception {
        GmlReader gmlReader = new GmlReader( null );
        Resource resource = new InputStreamResource(
                                                     GmlReaderTest.class.getResourceAsStream( "cadastralparcels_wfs20FC.xml" ) );
        gmlReader.setResource( resource );
        gmlReader.open( new ExecutionContext() );

        int numberOfFeatures = 0;
        Feature feature;
        do {
            feature = gmlReader.read();
            if ( feature != null ) {
                numberOfFeatures++;
            }
        } while ( feature != null );

        assertThat( numberOfFeatures, is( 5 ) );
    }

    @Test
    public void testRead_unresolvableReferences()
                            throws Exception {
        GmlReader gmlReader = new GmlReader( null );
        Resource resource = new InputStreamResource(
                                                     GmlReaderTest.class.getResourceAsStream( "cadastralzonings-withUnresolvableReferences.xml" ) );
        gmlReader.setResource( resource );
        gmlReader.open( new ExecutionContext() );

        int numberOfFeatures = 0;
        Feature feature;
        do {
            feature = gmlReader.read();
            if ( feature != null ) {
                numberOfFeatures++;
            }
        } while ( feature != null );

        assertThat( numberOfFeatures, is( 3 ) );
    }

}