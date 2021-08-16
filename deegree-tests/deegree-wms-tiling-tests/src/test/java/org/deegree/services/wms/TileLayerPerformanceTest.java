//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms;

import static org.deegree.commons.utils.io.Utils.DEV_NULL;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.wms.client.WMSClient;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * <code>TileLayerPT</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class TileLayerPerformanceTest {

    @Test
    public void testPerformance()
                            throws IOException, InterruptedException, OWSExceptionReport, XMLStreamException {
        String base = "http://localhost:" + System.getProperty( "portnumber", "8080" );
        base += "/deegree-wms-tiling-tests/services";
        WMSClient client = new WMSClient( new URL( base + "?request=GetCapabilities&service=WMS&version=1.1.1" ) );

        // skip test if layer is not available, then we probably don't have the huge file available
        Assume.assumeTrue( client.hasLayer( "performance" ) );

        String crs = client.getCoordinateSystems( "performance" ).getFirst();

        Envelope envelope = client.getBoundingBox( crs, "performance" );
        double minx = envelope.getMin().get0();
        double miny = envelope.getMin().get1();
        double res = 0.14;
        double spanx = res * 800;
        double spany = res * 600;
        base += "?request=GetMap&service=WMS&version=1.1.1&layers=performance&styles=&width=800&height=600&";
        base += "format=image/png&transparent=true&srs=" + crs + "&bbox=";

        List<Callable<Object>> list = new ArrayList<Callable<Object>>();

        for ( int i = 0; i < 100; ++i ) {
            String url = base + minx + "," + miny + ",";
            minx += spanx;
            miny += spany;
            url += minx + "," + miny;
            list.add( new Fetcher( url ) );
        }

        ExecutorService service = Executors.newFixedThreadPool( 10 );

        long t1 = System.currentTimeMillis();
        service.invokeAll( list );
        t1 = System.currentTimeMillis() - t1;
        System.out.println( "Requested 100 images, 10 in parallel, took " + ( t1 / 1000 ) + " seconds." );
        double avg = ( t1 / 100d ) / 1000d;
        System.out.println( "Average secs/request: " + avg );
        Assert.assertTrue( "Average response time was too high.", avg < 5 );
    }

    static class Fetcher implements Callable<Object> {
        private String url;

        Fetcher( String url ) {
            this.url = url;
        }

        @Override
        public Object call()
                                throws Exception {
            InputStream in = retrieve( STREAM, url );
            IOUtils.copy( in, DEV_NULL );
            in.close();
            return null;
        }
    }

}
