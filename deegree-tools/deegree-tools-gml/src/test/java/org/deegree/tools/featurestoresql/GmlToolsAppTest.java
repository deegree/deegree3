package org.deegree.tools.featurestoresql;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlToolsAppTest {

    @Test
    public void testMain_Empty() {
        String[] args = new String[] {};
        GmlToolsApp.main( args );
    }

    @Test
    public void testMain_H() {
        String[] args = new String[] { "-h" };
        GmlToolsApp.main( args );
    }

    @Test
    public void testMain_Help() {
        String[] args = new String[] { "-help" };
        GmlToolsApp.main( args );
    }

    @Test
    public void testMain_Help2() {
        String[] args = new String[] { "--help" };
        GmlToolsApp.main( args );
    }

    @Test
    public void testMain_GmlLoader() {
        String[] args = new String[] { "gmlLoader", "--help" };
        GmlToolsApp.main( args );
    }

    @Test
    public void testMain_FeatureStoreConfigLoader() {
        String[] args = new String[] { "sqlFeatureStoreConfigCreator", "--help" };
        GmlToolsApp.main( args );
    }
}