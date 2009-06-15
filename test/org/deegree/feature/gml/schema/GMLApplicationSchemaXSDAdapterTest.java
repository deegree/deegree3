package org.deegree.feature.gml.schema;

import org.deegree.feature.types.FeatureType;
import org.junit.Test;

public class GMLApplicationSchemaXSDAdapterTest {

    @Test
    public void testParsing()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

         String schemaURL = this.getClass().getResource( "Philosopher_typesafe.xsd" ).toString();
//        String schemaURL = "file:///home/schneider/workspace/lkee_xplanung/resources/schema/XPlanung-Operationen.xsd";
        // String schemaURL =
        // "file:///home/schneider/workspace/lkee_xplanung/resources/schema/XPlanung-Operationen.xsd";
        ApplicationSchemaXSDAdapter adapter = new ApplicationSchemaXSDAdapter( schemaURL, GMLVersion.GML_31 );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        for ( FeatureType ft : fts ) {
            System.out.println( ft );
        }
    }
}
