package org.deegree.protocol.wms.ops;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetMapTest {

    @Test
    public void testTransparent_True()
                    throws Exception {
        Map<String, String> kvp = createRequest(
                        "BBOX=228152.00000000%2C5690412.00000000%2C493382.00000000%2C5939023.00000000&CRS=EPSG%3A25833&FORMAT=image%2Fpng&HEIGHT=500&LAYERS=EF.EnvironmentalMonitoringProgrammes&REQUEST=GetMap&SERVICE=WMS&STYLES=EF.EnvironmentalMonitoringProgrammes.Default.Point&TRANSPARENT=tRue&VERSION=1.3.0&WIDTH=500" );
        GetMap getMap = new GetMap( kvp, Version.parseVersion( "1.3.0" ), null, true );
        assertTrue( getMap.getTransparent() );
    }

    @Test
    public void testTransparent_False()
                    throws Exception {
        Map<String, String> kvp = createRequest(
                        "BBOX=228152.00000000%2C5690412.00000000%2C493382.00000000%2C5939023.00000000&CRS=EPSG%3A25833&FORMAT=image%2Fpng&HEIGHT=500&LAYERS=EF.EnvironmentalMonitoringProgrammes&REQUEST=GetMap&SERVICE=WMS&STYLES=EF.EnvironmentalMonitoringProgrammes.Default.Point&TRANSPARENT=fAlSe&VERSION=1.3.0&WIDTH=500" );
        GetMap getMap = new GetMap( kvp, Version.parseVersion( "1.3.0" ), null, true );
        assertFalse( getMap.getTransparent() );
    }

    @Test(expected = OWSException.class)
    public void testTransparent_Invalid()
                    throws Exception {
        Map<String, String> kvp = createRequest(
                        "BBOX=228152.00000000%2C5690412.00000000%2C493382.00000000%2C5939023.00000000&CRS=EPSG%3A25833&FORMAT=image%2Fpng&HEIGHT=500&LAYERS=EF.EnvironmentalMonitoringProgrammes&REQUEST=GetMap&SERVICE=WMS&STYLES=EF.EnvironmentalMonitoringProgrammes.Default.Point&TRANSPARENT=zzzz&VERSION=1.3.0&WIDTH=500" );
        new GetMap( kvp, Version.parseVersion( "1.3.0" ), null, true );
    }

    @Test
    public void testTransparent_Invalid_ParseLax()
                    throws Exception {
        Map<String, String> kvp = createRequest(
                        "BBOX=228152.00000000%2C5690412.00000000%2C493382.00000000%2C5939023.00000000&CRS=EPSG%3A25833&FORMAT=image%2Fpng&HEIGHT=500&LAYERS=EF.EnvironmentalMonitoringProgrammes&REQUEST=GetMap&SERVICE=WMS&STYLES=EF.EnvironmentalMonitoringProgrammes.Default.Point&TRANSPARENT=zzzz&VERSION=1.3.0&WIDTH=500" );
        GetMap getMap = new GetMap( kvp, Version.parseVersion( "1.3.0" ), null, false );
        assertFalse( getMap.getTransparent() );
    }

    private Map<String, String> createRequest( String queryParams )
                    throws IOException {
        return KVPUtils.getNormalizedKVPMap( queryParams, "UTF-8" );
    }

}
