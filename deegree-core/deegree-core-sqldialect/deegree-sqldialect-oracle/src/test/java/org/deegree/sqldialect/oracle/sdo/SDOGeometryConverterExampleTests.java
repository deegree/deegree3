package org.deegree.sqldialect.oracle.sdo;

import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.gml.geometry.GMLGeometryVersionHelper.getGeometryReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.geometry.GMLGeometryReader;
import org.deegree.sqldialect.oracle.sdo.SDOGeometryConverter.GeomHolder;
import org.deegree.sqldialect.utils.XMLMemoryStreamWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class SDOGeometryConverterExampleTests {

    private static final Logger LOG = LoggerFactory.getLogger( SDOGeometryConverterExampleTests.class );

    private static File TEST_DIR = new File( "src/test/resources/test/oracle/sdo" );

    private static final String NULL = "NULL";

    private static final String SDO_GEOM_PAT_1 = "(?:MDSYS\\.)?SDO_GEOMETRY\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([^,\\s]+)\\s*,\\s*";

    private static final String SDO_GEOM_PAT_2 = "(?:(?:(?:MDSYS\\.)?SDO_POINT_TYPE\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([^,\\s]+)\\s*,\\s*([^,\\s\\)]+)\\s*\\))|(NULL))\\s*,\\s*";

    private static final String SDO_GEOM_PAT_3 = "(?:(?:(?:(?:MDSYS\\.)?SDO_ELEM_INFO_ARRAY\\s*\\(([^\\)]*)\\))|(NULL)))\\s*,\\s*";

    private static final String SDO_GEOM_PAT_4 = "(?:(?:(?:(?:MDSYS\\.)?SDO_ORDINATE_ARRAY\\s*\\(([^\\)]*)\\))|(NULL)))\\s*\\)\\s*";

    private static final Pattern SDO_PAT = Pattern.compile( SDO_GEOM_PAT_1 + SDO_GEOM_PAT_2 + SDO_GEOM_PAT_3
                                                            + SDO_GEOM_PAT_4, Pattern.CASE_INSENSITIVE );

    final SDOGeometryConverter converter = new SDOGeometryConverter();

    @Parameter(0)
    public File sdoFile;

    @Parameter(1)
    public File gmlFile;

    @Parameter(2)
    public String testName;

    @Parameter(3)
    public boolean skipSdoSdo;

    @Parameter(4)
    public boolean skipGmlSdo;

    @Parameter(5)
    public boolean skipSdoGml;

    @Parameters(name = "{index}: {2}")
    public static Collection<Object[]> getFiles()
                            throws IOException {
        Collection<Object[]> params = new LinkedList<Object[]>();
        String baseName = TEST_DIR.getAbsolutePath();

        for ( File fSDO : FileUtils.findFilesForExtensions( TEST_DIR, true, ".sdo" ) ) {
            String base = FileUtils.getBasename( fSDO.getAbsoluteFile() );
            String name = base;
            if ( name.startsWith( baseName ) )
                name = name.substring( baseName.length() + 1 );

            File fGML = new File( base + ".gml" );
            boolean skipSdoSdo = new File( base + ".skip-sdo-sdo" ).exists();
            boolean skipGmlSdo = new File( base + ".skip-gml-sdo" ).exists();
            boolean skipSdoGml = new File( base + ".skip-sdo-gml" ).exists();

            if ( fSDO.isFile() && fGML.isFile() ) {
                params.add( new Object[] { fSDO, fGML, name, skipSdoSdo, skipGmlSdo, skipSdoGml } );
            } else {
                LOG.warn( "Could not find test data same {}.sdo/.gml", name );
            }
        }
        return params;
    }

    @Test
    public void testConvertSdoGeometryAndBack()
                            throws Exception {
        GeomHolder sdo = loadFromFile( sdoFile );
        // inspector.reset();
        Geometry geom = readGMLGeometry( gmlFile );

        // Test SDO -> Geom -> SDO
        // inspector.reset();
        Geometry sdoGeom = converter.toGeometry( sdo, null );
        GeomHolder sdoGeomSdo = converter.fromGeometry( sdo.srid, sdoGeom, false );

        String sdoGeomString = writeGMLGeometry( sdoGeom );
        String sdoString = toString( sdo );
        String sdoGeomSdoString = toString( sdoGeomSdo );
        assertNotNull( "Convertable 1", sdoString );
        assertNotNull( "Convertable 2", sdoGeomSdoString );
        if ( !skipSdoSdo ) {
            assertEquals( "SDO -> Geom -> SDO", sdoString, sdoGeomSdoString );
        }

        // Test Geom -> SDO -> Geom
        GeomHolder geomSdo = converter.fromGeometry( sdo.srid, geom, false );
        // inspector.reset();
        Geometry geomSdoGeom = converter.toGeometry( geomSdo, null );

        String geomSdoString = toString( geomSdo );
        String geomString = writeGMLGeometry( geom );
        String geomSdoGeomString = writeGMLGeometry( geomSdoGeom );
        assertNotNull( "Convertable 3", geomString );
        assertNotNull( "Convertable 4", geomSdoGeomString );
        assertEquals( "Geom -> SDO -> Geom", geomString, geomSdoGeomString );

        // Sample matches
        if ( !skipGmlSdo ) {
            assertEquals( ".gml matches .sdo", sdoString, geomSdoString );
        }
        if ( !skipSdoGml ) {
            assertEquals( ".sdo matches .gml", geomString, sdoGeomString );
        }
    }

    private String replaceGmlIDs( String text ) {
        StringBuffer result = new StringBuffer();
        Pattern pattern = Pattern.compile( "gml:id=\"[^\"]+\"" );
        Matcher matcher = pattern.matcher( text );
        int id = 0;
        while ( matcher.find() ) {
            matcher.appendReplacement( result, "gml:id=\"ID_" + ( ++id ) + "\"" );
        }
        matcher.appendTail( result );
        return result.toString();
    }

    private Geometry readGMLGeometry( File file )
                            throws Exception {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( file.toURI().toURL() );
        xmlReader.next();

        GMLGeometryReader rdr = getGeometryReader( xmlReader.getName(), xmlReader );
        return rdr.parse( xmlReader );
    }

    private String writeGMLGeometry( Geometry geom )
                            throws Exception {
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter( memoryWriter.getXMLStreamWriter() );
        GMLStreamWriter exporter = createGMLStreamWriter( GML_32, xmlWriter );

        exporter.write( geom );
        return replaceGmlIDs( memoryWriter.toString() );
    }

    private GeomHolder loadFromFile( File f )
                            throws IOException {
        @SuppressWarnings("resource")
        String output = new Scanner( f ).useDelimiter( "\\Z" ).next().replaceAll( "[\r\n]+", " " );
        Matcher m = SDO_PAT.matcher( output );

        if ( m.matches() ) {
            int gtype;
            int srid;
            double point[];
            int[] elem_info;
            double[] ordinates;

            gtype = Integer.parseInt( m.group( 1 ) );
            srid = NULL.equalsIgnoreCase( m.group( 2 ) ) ? -1 : Integer.parseInt( m.group( 2 ) );
            if ( NULL.equalsIgnoreCase( m.group( 6 ) ) ) {
                point = null;
            } else {
                point = new double[] { NULL.equalsIgnoreCase( m.group( 3 ) ) ? null
                                                                             : Double.parseDouble( m.group( 3 ) ),
                                       NULL.equalsIgnoreCase( m.group( 4 ) ) ? null
                                                                             : Double.parseDouble( m.group( 4 ) ),
                                       NULL.equalsIgnoreCase( m.group( 5 ) ) ? null
                                                                             : Double.parseDouble( m.group( 5 ) ) };
            }
            if ( NULL.equalsIgnoreCase( m.group( 8 ) ) ) {
                elem_info = null;
            } else {
                List<Integer> lst = new ArrayList<Integer>();

                for ( String se : m.group( 7 ).split( "\\s*,\\s*" ) ) {
                    lst.add( Integer.parseInt( se.trim() ) );
                }
                elem_info = new int[lst.size()];
                for ( int i = 0; i < elem_info.length; i++ ) {
                    elem_info[i] = lst.get( i );
                }
            }

            if ( NULL.equalsIgnoreCase( m.group( 10 ) ) ) {
                ordinates = null;
            } else {
                List<Double> lst = new ArrayList<Double>();

                for ( String se : m.group( 9 ).split( "\\s*,\\s*" ) ) {
                    lst.add( Double.parseDouble( se.trim() ) );
                }
                ordinates = new double[lst.size()];
                for ( int i = 0; i < ordinates.length; i++ ) {
                    ordinates[i] = lst.get( i );
                }
            }
            return new GeomHolder( gtype, srid, point, elem_info, ordinates, null );
        }
        return null;
    }

    private String toString( GeomHolder h ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "MDSYS.SDO_GEOMETRY( " );
        sb.append( h.gtype ).append( ", " );
        sb.append( h.srid < 0 ? NULL : h.srid ).append( ", " );
        sb.append( NULL ).append( ", " );
        sb.append( "MDSYS.SDO_ELEM_INFO_ARRAY( " );
        for ( int i = 0; i < h.elem_info.length; i++ ) {
            if ( i > 0 ) {
                sb.append( ", " );
            }
            sb.append( h.elem_info[i] );
        }
        sb.append( "), " );
        sb.append( "MDSYS.SDO_ORDINATE_ARRAY( " );
        for ( int i = 0; i < h.ordinates.length; i++ ) {
            if ( i > 0 ) {
                sb.append( ", " );
            }
            sb.append( h.ordinates[i] );
        }
        sb.append( ") " );
        sb.append( " )" );
        return sb.toString();
    }
}
