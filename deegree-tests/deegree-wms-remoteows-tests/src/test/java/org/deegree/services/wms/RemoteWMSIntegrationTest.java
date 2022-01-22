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

import org.apache.commons.io.IOUtils;
import org.deegree.commons.utils.test.IntegrationTestUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.deegree.commons.utils.io.Utils.determineSimilarity;
import static org.deegree.commons.utils.net.HttpUtils.IMAGE;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <code>RemoteWMSIntegrationTest</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

@RunWith(Parameterized.class)
@Ignore
public class RemoteWMSIntegrationTest {

    private static final Logger LOG = getLogger( RemoteWMSIntegrationTest.class );

    private static int numFailed = 0;

    private final String resourceName;

    private final String request;

    private final BufferedImage expected;

    public RemoteWMSIntegrationTest( String resourceName )
                    throws IOException {
        this.resourceName = resourceName;
        this.request = IOUtils.toString(
                        RemoteWMSIntegrationTest.class.getResourceAsStream(
                                        "/requests/" + resourceName + ".kvp" ) );
        this.expected = ImageIO.read(
                        RemoteWMSIntegrationTest.class.getResourceAsStream(
                                        "/requests/" + resourceName + ".response" ) );
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> getParameters() {
        List<Object[]> requests = new ArrayList<>();
        requests.add( new Object[] { "featureinfofromdeegree" } );
        requests.add( new Object[] { "multiple" } );
        requests.add( new Object[] { "optionsmultiple" } );
        requests.add( new Object[] { "optionssingle" } );
        requests.add( new Object[] { "parameters" } );
        requests.add( new Object[] { "parametersext" } );
        requests.add( new Object[] { "single" } );
        requests.add( new Object[] { "timeout" } );
        requests.add( new Object[] { "transformedgif" } );
        return requests;
    }

    @Test
    public void testSimilarity()
                    throws IOException {
        String request = createRequest();
        LOG.info( "Requesting {}", request );
        BufferedImage actual = retrieve( IMAGE, request );
        double sim = determineSimilarity( actual, expected );
        if ( Math.abs( 1.0 - sim ) > 0.01 ) {
            System.out.println( "Trying to store request/response for " + resourceName
                                + " in " + System.getProperty( "java.io.tmpdir" ) + ": remoteows_expected_"
                                + ++numFailed
                                + "_" + ".png/remoteows_response_" + numFailed + ".png" );
            try {
                ImageIO.write( actual, "png", new FileOutputStream( System.getProperty( "java.io.tmpdir" )
                                                                    + "/remoteows_expected_" + numFailed + "_"
                                                                    + ".png" ) );
                ImageIO.write( expected, "png", new FileOutputStream( System.getProperty( "java.io.tmpdir" )
                                                                      + "/remoteows_response_" + numFailed + ".png" ) );

                System.out.println(
                                "Result returned for " + resourceName + " (base64 -d encoded.dat > failed-test.zip)" );
                System.out.println( IntegrationTestUtils.toBase64Zip( parseAsBytes( actual ), resourceName + ".png" ) );
            } catch ( Throwable t ) {
            }
            assertEquals( "Images are not similar enough for " + resourceName + " (" + request + ").", 1.0, sim, 0.01 );
        }
    }

    private String createRequest() {
        StringBuffer sb = new StringBuffer();
        sb.append( "http://localhost:" );
        sb.append( System.getProperty( "portnumber", "8080" ) );
        sb.append( "/" );
        sb.append( System.getProperty( "deegree-wms-remoteows-webapp", "deegree-wms-remoteows-tests" ) );
        sb.append( "/services/wms" );
        if ( !request.startsWith( "?" ) )
            sb.append( "?" );
        sb.append( request );
        return sb.toString();
    }

    private byte[] parseAsBytes( RenderedImage actual )
                    throws IOException {
        ByteArrayOutputStream bosActual = new ByteArrayOutputStream();
        ImageIO.write( actual, "png", bosActual );
        bosActual.flush();
        bosActual.close();
        return bosActual.toByteArray();
    }
}
