package org.deegree.aixm;

import static org.deegree.gml.GMLVersion.GML_32;

import java.net.URL;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.AppSchemaXSDDecoder;

public class AIXMLab {

    private static final String AIXM_SCHEMA = "file:/home/schneider/workspace/deegree-aixm-demo/src/main/webapp/WEB-INF/workspace/datasources/feature/schemas/aixm/message/AIXM_BasicMessage.xsd";

    private static final String AIXM_DATASET = "file:/home/schneider/workspace/deegree-aixm-demo/src/main/webapp/WEB-INF/workspace/datasources/feature/datasets/baseline.xml";

    public static void main( String[] args )
                            throws Exception {

        System.out.print( "Loading GML schema from URL '" + AIXM_SCHEMA + "'..." );
        AppSchemaXSDDecoder decoder = new AppSchemaXSDDecoder( GML_32, null, AIXM_SCHEMA );
        AppSchema schema = decoder.extractFeatureTypeSchema();
        System.out.println( "done.\nDetected " + schema.getFeatureTypes().length + " feature types." );

        System.out.println( "Loading GML dataset from URL '" + AIXM_DATASET + "'..." );
        GMLVersion gmlVersion = GML_32;
        GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader( gmlVersion, new URL( AIXM_DATASET ) );
        gmlStream.setApplicationSchema( schema );
        FeatureCollection fc = gmlStream.readFeatureCollection();
        gmlStream.close();
        gmlStream.getIdContext().resolveLocalRefs();
        System.out.println( "done.\nLoaded " + fc.size() + " features." );
    }
}