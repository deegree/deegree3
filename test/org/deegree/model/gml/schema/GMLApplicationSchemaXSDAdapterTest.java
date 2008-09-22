package org.deegree.model.gml.schema;


import org.deegree.model.feature.types.FeatureType;
import org.junit.Test;

public class GMLApplicationSchemaXSDAdapterTest {

    @Test
    public void testParsing () throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {

//        String schemaURL = this.getClass().getResource( "Philosopher_typesafe.xsd").toString();
        String schemaURL = "file:///home/schneider/workspace/vrom-roonline/resources/schema/1.1RC-adapted/IMRO2006.xsd";
        GMLApplicationSchemaXSDAdapter adapter = new GMLApplicationSchemaXSDAdapter (schemaURL, GMLVersion.VERSION_31);
        FeatureType [] fts = adapter.extractFeatureTypes();
        for ( FeatureType ft : fts ) {
//            System.out.println (ft.getName().getNamespaceURI());
            if (!ft.getName().getNamespaceURI().equals( "http://www.opengis.net/gml" )) {
            System.out.println (ft);
            }
        }
    }
}
