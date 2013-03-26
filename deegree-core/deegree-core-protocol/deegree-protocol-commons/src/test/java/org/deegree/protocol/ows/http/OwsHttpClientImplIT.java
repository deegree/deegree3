package org.deegree.protocol.ows.http;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

public class OwsHttpClientImplIT {
    
    @Test
    public void testDoGetHttp()
                            throws Exception {
        OwsHttpClient client = new OwsHttpClientImpl();
        URL url = new URL(
                           "http://demo.deegree.org/deegree-wms/services?request=GetCapabilities&version=1.1.1&service=WMS" );
        OwsHttpResponse response = client.doGet( url, null, null );
        assertEquals(response.getAsHttpResponse().getStatusLine().getStatusCode(),200);
    }

}
