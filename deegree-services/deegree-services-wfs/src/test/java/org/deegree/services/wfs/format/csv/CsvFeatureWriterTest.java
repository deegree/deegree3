package org.deegree.services.wfs.format.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CsvFeatureWriterTest {

    public static final String[] HEADEERS = "{http://www.opengis.net/gml/3.2}description,{http://www.opengis.net/gml/3.2}name,{http://www.deegree.org/app}id,{http://www.deegree.org/app}name,{http://www.deegree.org/app}sex,{http://www.deegree.org/app}subject,{http://www.deegree.org/app}dateOfBirth,{http://www.deegree.org/app}placeOfBirth,{http://www.deegree.org/app}dateOfDeath,{http://www.deegree.org/app}placeOfDeath,CRS".split( "," );

    @Test
    public void testWrite_DataCrs()
                            throws Exception {
        Feature cadastralZoning = parseFeature( "PhilosopherForCsv.gml" );
        FeatureType featureType = cadastralZoning.getType();
        StringWriter featureAsCsv = new StringWriter();
        CsvFeatureWriter csvFeatureWriter = new CsvFeatureWriter( featureAsCsv, null, featureType );

        csvFeatureWriter.write( cadastralZoning );

        CSVParser parser = new CSVParser( new StringReader( featureAsCsv.toString() ),
                                          CSVFormat.DEFAULT.withHeader( HEADEERS ) );
        List<CSVRecord> records = parser.getRecords();
        assertThat( records.size(), is( 2 ) );

        CSVRecord header = records.get( 0 );
        assertThat( header.size(), is( 11 ) );
        CSVRecord record = records.get( 1 );
        assertThat( record.size(), is( 11 ) );

        assertThat( record.get( "{http://www.deegree.org/app}id" ), is( "1" ) );
        assertThat( record.get( "{http://www.deegree.org/app}name" ), is( "Karl Marx" ) );
        assertThat( record.get( "{http://www.deegree.org/app}sex" ), is( "m" ) );
        assertThat( record.get( "{http://www.deegree.org/app}subject" ), is( "capital | economy | labour" ) );
        assertThat( record.get( "{http://www.deegree.org/app}dateOfBirth" ), is( "1818-05-05" ) );
        assertThat( record.get( "{http://www.deegree.org/app}placeOfBirth" ), is( "POLYGON ((8.678595 47.693344,8.673953 47.702854,8.705485 47.711037,8.710255 47.696808,8.678595 47.693344))" ) );
        assertThat( record.get( "{http://www.deegree.org/app}dateOfDeath" ), is( "1883-03-14" ) );
        assertThat( record.get( "{http://www.deegree.org/app}placeOfDeath" ), is( "POLYGON ((-0.835000 60.673332,-0.935556 60.674438,-0.962083 60.685272,-0.959722 60.711388,-0.938889 60.794441,-0.880695 60.843330,-0.806111 60.840553,-0.770278 60.829998,-0.757639 60.815830,-0.763611 60.793327,-0.819722 60.688889,-0.835000 60.673332))" ) );
        assertThat( record.get( "CRS" ), is( "" ) );
    }

    @Test
    public void testWrite_OtherCrs()
                            throws Exception {
        ICRS crs = CRSManager.lookup( "EPSG:4326" );
        Feature cadastralZoning = parseFeature( "PhilosopherForCsv.gml" );
        FeatureType featureType = cadastralZoning.getType();
        StringWriter featureAsCsv = new StringWriter();
        CsvFeatureWriter csvFeatureWriter = new CsvFeatureWriter( featureAsCsv, crs, featureType );

        csvFeatureWriter.write( cadastralZoning );

        CSVParser parser = new CSVParser( new StringReader( featureAsCsv.toString() ),
                                          CSVFormat.DEFAULT.withHeader( HEADEERS ) );
        List<CSVRecord> records = parser.getRecords();
        assertThat( records.size(), is( 2 ) );

        CSVRecord header = records.get( 0 );
        assertThat( header.size(), is( 11 ) );
        CSVRecord record = records.get( 1 );
        assertThat( record.size(), is( 11 ) );

        assertThat( record.get( "{http://www.deegree.org/app}id" ), is( "1" ) );
        assertThat( record.get( "{http://www.deegree.org/app}name" ), is( "Karl Marx" ) );
        assertThat( record.get( "{http://www.deegree.org/app}sex" ), is( "m" ) );
        assertThat( record.get( "{http://www.deegree.org/app}subject" ), is( "capital | economy | labour" ) );
        assertThat( record.get( "{http://www.deegree.org/app}dateOfBirth" ), is( "1818-05-05" ) );
        assertThat( record.get( "{http://www.deegree.org/app}placeOfBirth" ), is( "POLYGON ((8.678595 47.693344,8.673953 47.702854,8.705485 47.711037,8.710255 47.696808,8.678595 47.693344))" ) );
        assertThat( record.get( "{http://www.deegree.org/app}dateOfDeath" ), is( "1883-03-14" ) );
        assertThat( record.get( "{http://www.deegree.org/app}placeOfDeath" ), is( "POLYGON ((-0.835000 60.673332,-0.935556 60.674438,-0.962083 60.685272,-0.959722 60.711388,-0.938889 60.794441,-0.880695 60.843330,-0.806111 60.840553,-0.770278 60.829998,-0.757639 60.815830,-0.763611 60.793327,-0.819722 60.688889,-0.835000 60.673332))" ) );
        assertThat( record.get( "CRS" ), is( "epsg:4326" ) );
    }

    private Feature parseFeature( String resourceName )
                            throws Exception {
        URL testResource = CsvFeatureWriterTest.class.getResource( resourceName );
        GMLStreamReader reader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, testResource );
        return reader.readFeature();
    }

}
