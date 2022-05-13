package org.deegree.tools.featurestoresql.loader;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlLoaderAppTest {

    @Test
    public void testMain_Empty() {
        String[] args = new String[] { "gmlLoader" };
        GmlLoaderApp.run( args );
    }

    @Test
    public void testMain_H() {
        String[] args = new String[] { "gmlLoader", "-h" };
        GmlLoaderApp.run( args );
    }

    @Test
    public void testMain_Help() {
        String[] args = new String[] { "gmlLoader", "-help" };
        GmlLoaderApp.run( args );
    }

    @Test
    public void testMain_Help2() {
        String[] args = new String[] { "gmlLoader", "--help" };
        GmlLoaderApp.run( args );
    }

}